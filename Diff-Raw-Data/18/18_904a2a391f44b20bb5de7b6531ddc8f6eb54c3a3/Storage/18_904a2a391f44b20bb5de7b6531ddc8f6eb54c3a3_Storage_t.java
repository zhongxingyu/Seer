 /*
  * utils.graph - Storage.java - Copyright © 2011 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.storage;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import net.pterodactylus.util.io.Closer;
 
 /**
  * Storage for {@link Storable}s. The storage persists the objects to a data
  * file, using a second index file to keep track of allocated blocks.
  *
  * @param <T>
  *            The type of the stored object
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Storage<T extends Storable> implements Closeable {
 
 	/** The internal block size. */
 	private final int blockSize;
 
 	/** Factory for {@link Storable}s. */
 	private final Factory<T> factory;
 
 	/** The index file. */
 	private final RandomAccessFile indexFile;
 
 	/** The data file. */
 	private final RandomAccessFile dataFile;
 
 	/** Lock for synchronization. */
 	private final ReadWriteLock lock = new ReentrantReadWriteLock();
 
 	/** The directory entries in on-disk order. */
 	private final List<Allocation> directoryEntries = new ArrayList<Allocation>();
 
 	/** Maps Storables’ IDs to directory indexes. */
 	private final Map<Long, Integer> idDirectoryIndexes = new HashMap<Long, Integer>();
 
 	/** Keeps track of empty directory entries. */
 	private final BitSet emptyDirectoryEntries = new BitSet();
 
 	/** Keeps track of allocated data blocks. */
 	private final BitSet allocations = new BitSet();
 
 	/** Whether the store is opened. */
 	private boolean opened;
 
 	/**
 	 * Creates a new storage with a default block size of 512 bytes.
 	 *
 	 * @param factory
 	 *            The factory that creates the objects
 	 * @param directory
 	 *            The directory to store the files in
 	 * @param name
 	 *            The base name of the files
 	 * @throws FileNotFoundException
 	 *             if the directory can not be found
 	 */
 	public Storage(Factory<T> factory, File directory, String name) throws FileNotFoundException {
 		this(512, factory, directory, name);
 	}
 
 	/**
 	 * Creates a new storage.
 	 *
 	 * @param blockSize
 	 *            The block size
 	 * @param factory
 	 *            The factory that creates the objects
 	 * @param directory
 	 *            The directory to store the files in
 	 * @param name
 	 *            The base name of the files
 	 * @throws FileNotFoundException
 	 *             if the directory can not be found
 	 */
 	public Storage(int blockSize, Factory<T> factory, File directory, String name) throws FileNotFoundException {
 		this.blockSize = blockSize;
 		this.factory = factory;
 		indexFile = new RandomAccessFile(new File(directory, name + ".idx"), "rws");
 		dataFile = new RandomAccessFile(new File(directory, name + ".dat"), "rws");
 	}
 
 	//
 	// ACTIONS
 	//
 
 	/**
 	 * Opens this storage.
 	 *
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void open() throws IOException {
 		lock.writeLock().lock();
 		try {
 			if (opened) {
 				throw new IllegalStateException("Storage already opened.");
 			}
 			long indexLength = indexFile.length();
 			if ((indexLength % 16) != 0) {
 				throw new IOException("Invalid Index Length: " + indexLength);
 			}
 			for (int directoryIndex = 0; directoryIndex < (indexLength / 16); ++directoryIndex) {
 				byte[] allocationBuffer = new byte[16];
 				indexFile.readFully(allocationBuffer);
 				Allocation allocation = Allocation.FACTORY.restore(allocationBuffer);
 				if ((allocation.getId() == 0) && (allocation.getPosition() == 0) && (allocation.getSize() == 0)) {
 					emptyDirectoryEntries.set(directoryIndex);
 					directoryEntries.add(null);
 				} else {
 					directoryEntries.add(allocation);
 					idDirectoryIndexes.put(allocation.getId(), directoryIndex);
 				}
 				++directoryIndex;
 			}
 			opened = true;
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	/**
 	 * Adds a storable to this storage.
 	 *
 	 * @param storable
 	 *            The storable to store
 	 * @throws StorageException
 	 *             if a store error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void add(T storable) throws StorageException, IOException {
 		lock.writeLock().lock();
 		try {
 			if (!opened) {
 				throw new IllegalStateException("Storage not opened!");
 			}
 			byte[] storableBytes = storable.getBuffer();
 			int storableLength = storableBytes.length;
 			int blocks = getBlocks(storableLength);
 			int position = findFreeRegion(blocks);
 
 			/* first, write data. */
 			allocations.set(position, position + blocks);
 			if (dataFile.length() < (position * blockSize + storableLength)) {
 				dataFile.setLength(position * blockSize + storableLength);
 			}
 			dataFile.seek(position * blockSize);
 			dataFile.write(storableBytes);
 
 			/* now directory entry. */
 			int oldIndex = -1;
 			Allocation allocation = new Allocation(storable.getId(), position, storableLength);
 			int directoryIndex = emptyDirectoryEntries.nextSetBit(0);
 			if (directoryIndex == -1) {
 				/* append. */
 				directoryIndex = directoryEntries.size();
 				directoryEntries.add(allocation);
 			} else {
 				directoryEntries.set(directoryIndex, allocation);
 				emptyDirectoryEntries.clear(directoryIndex);
 			}
 			if (idDirectoryIndexes.containsKey(storable.getId())) {
 				oldIndex = idDirectoryIndexes.get(storable.getId());
 				Allocation oldAllocation = directoryEntries.set(oldIndex, null);
 				emptyDirectoryEntries.set(oldIndex);
 				allocations.clear(oldAllocation.getPosition(), oldAllocation.getPosition() + getBlocks(oldAllocation.getSize()));
 			}
 			emptyDirectoryEntries.clear(directoryIndex);
 			idDirectoryIndexes.put(storable.getId(), directoryIndex);
 
 			/* now write directory to disk. */
 			indexFile.seek(directoryIndex * 16);
 			indexFile.write(allocation.getBuffer());
 
 			/* if an old index was deleted, wipe it. */
 			if (oldIndex > -1) {
 				indexFile.seek(oldIndex * 16);
 				indexFile.write(new byte[16]);
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	/**
 	 * Returns the number of storables in this storage.
 	 *
 	 * @return The number of storables
 	 */
 	public int size() {
 		lock.readLock().lock();
 		try {
 			return directoryEntries.size() - emptyDirectoryEntries.cardinality();
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Loads a storable.
 	 *
 	 * @param id
 	 *            The ID of the storable to load
 	 * @return The storable
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public T load(long id) throws IOException {
 		lock.readLock().lock();
		Allocation allocation;
 		try {
 			Integer directoryIndex = idDirectoryIndexes.get(id);
 			if (directoryIndex == null) {
 				return null;
 			}
			allocation = directoryEntries.get(directoryIndex);
		} finally {
			lock.readLock().unlock();
		}
		byte[] buffer = new byte[allocation.getSize()];
		lock.writeLock().lock();
		try {
 			dataFile.seek(allocation.getPosition() * blockSize);
 			dataFile.readFully(buffer);
 		} finally {
			lock.writeLock().unlock();
 		}
		return factory.restore(buffer);
 	}
 
 	/**
 	 * Returns the size of the directory. Note that this can be larger than
 	 * {@link #size()} because it also includes empty directory entries!
 	 *
 	 * @return The size of the directory
 	 */
 	public int getDirectorySize() {
 		lock.readLock().lock();
 		try {
 			return directoryEntries.size();
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Returns the allocation at the given directory index.
 	 *
 	 * @param directoryIndex
 	 *            The directory index
 	 * @return The allocation at the given index, or {@code null} if there is no
 	 *         allocation at the given index
 	 */
 	public Allocation getAllocation(int directoryIndex) {
 		lock.readLock().lock();
 		try {
 			return directoryEntries.get(directoryIndex);
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Removes a storable.
 	 *
 	 * @param storable
 	 *            The storable to remove
 	 */
 	public void remove(T storable) {
 		/* TODO */
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void close() {
 		lock.writeLock().lock();
 		try {
 			Closer.close(indexFile);
 			Closer.close(dataFile);
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Locates the first free region that is large enough to hold the given
 	 * number of blocks sequentially.
 	 *
 	 * @param blocks
 	 *            The number of blocks
 	 * @return The index at which the blocks can be stored
 	 */
 	public int findFreeRegion(int blocks) {
 		int currentBlock = -1;
 		while (true) {
 			int nextUsedBlock = allocations.nextSetBit(currentBlock + 1);
 			if ((nextUsedBlock == -1) || ((nextUsedBlock - currentBlock - 1) >= blocks)) {
 				return currentBlock + 1;
 			}
 			currentBlock = nextUsedBlock;
 		}
 	}
 
 	//
 	// STATIC METHODS
 	//
 
 	/**
 	 * Returns the number of blocks for the given number of bytes.
 	 *
 	 * @param size
 	 *            The size of the data
 	 * @return The numbers of blocks
 	 */
 	private int getBlocks(long size) {
 		if (size == 0) {
 			return 1;
 		}
 		return (int) ((size - 1) / blockSize + 1);
 	}
 
 }
