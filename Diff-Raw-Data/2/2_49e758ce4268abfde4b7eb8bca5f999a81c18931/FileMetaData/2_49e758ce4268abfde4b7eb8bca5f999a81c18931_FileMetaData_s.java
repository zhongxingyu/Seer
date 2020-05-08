 package edu.berkeley.icsi.cdfs.namenode;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.hadoop.fs.Path;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.KryoSerializable;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 final class FileMetaData implements KryoSerializable, Comparable<FileMetaData> {
 
 	private Path path;
 
 	private final List<BlockMetaData> blocks = new ArrayList<BlockMetaData>();
 
 	private long length;
 
 	private long modificationTime;
 
 	FileMetaData(final Path path) {
 		this.path = path;
 		this.length = 0L;
 		this.modificationTime = System.currentTimeMillis();
 	}
 
 	@SuppressWarnings("unused")
 	private FileMetaData() {
 		this.path = null;
 		this.length = 0L;
 		this.modificationTime = 0L;
 	}
 
 	Path getPath() {
 		return this.path;
 	}
 
 	BlockMetaData[] getBlockMetaData(final long start, final long len) {
 
 		if (start >= this.length) {
 			return null;
 		}
 
 		final ArrayList<BlockMetaData> blocks = new ArrayList<BlockMetaData>(this.blocks.size());
 		final Iterator<BlockMetaData> it = this.blocks.iterator();
 
 		final long end = start + len;
 
 		while (it.hasNext()) {
 
 			final BlockMetaData bmd = it.next();
 			if (overlap(start, end, bmd.getOffset(), bmd.getOffset() + bmd.getLength())) {
 				blocks.add(bmd);
 			}
 
 			if (bmd.getOffset() > end) {
 				break;
 			}
 		}
 
 		return blocks.toArray(new BlockMetaData[0]);
 	}
 
 	int getNumberOfBlocks() {
 
 		return this.blocks.size();
 	}
 
 	private static boolean overlap(final long startA, final long endA, final long startB, final long endB) {
 
 		if (startA == endA) {
 			return (startA >= startB && startA < endB);
 		}
 
 		if (startB == endB) {
 			return (startB >= startA && startB < endA);
 		}
 
 		return (startA < endB && startB < endA);
 	}
 
 	void addNewBlock(final Path hdfsPath, final int blockIndex, final int blockLength) {
 
 		// Sanity check
 		if (blockIndex != this.blocks.size()) {
 			throw new IllegalStateException("Expected block " + this.blocks.size() + ", but received " + blockIndex);
 		}
 
 		this.blocks.add(new BlockMetaData(blockIndex, hdfsPath, blockLength, this.length));
 
 		// Increase the length of the total file
 		this.length += blockLength;
 
 		// Update modification time
 		this.modificationTime = System.currentTimeMillis();
 	}
 
 	long getLength() {
 		return this.length;
 	}
 
 	long getModificationTime() {
 		return this.modificationTime;
 	}
 
 	BlockMetaData addCachedBlock(final int blockIndex, final String host, final boolean compressed) {
 
 		final BlockMetaData bmd = this.blocks.get(blockIndex);
 
 		if (bmd == null) {
 			throw new IllegalStateException("Cannot find block meta data for block " + blockIndex + " of " + this.path);
 		}
 
 		bmd.addCachedBlock(host, compressed);
 
 		return bmd;
 	}
 
 	boolean isCachedCompletely(final boolean compressed) {
 
 		final Iterator<BlockMetaData> it = this.blocks.iterator();
 
 		while (it.hasNext()) {
 
 			final BlockMetaData bmd = it.next();
 			if (!bmd.isCached(compressed)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	BlockMetaData removeCachedBlock(final int blockIndex, final String host, final boolean compressed) {
 
 		final BlockMetaData bmd = this.blocks.get(blockIndex);
 
 		if (bmd == null) {
 			throw new IllegalStateException("Cannot find block meta data for block " + blockIndex + " of " + this.path);
 		}
 
 		bmd.removeCachedBlock(host, compressed);
 
 		return bmd;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void write(final Kryo kryo, final Output output) {
 
 		output.writeString(this.path.toString());
 		output.writeLong(this.length);
 		output.writeLong(this.modificationTime);
 		output.writeInt(this.blocks.size());
 		for (final BlockMetaData bmd : this.blocks) {
 			kryo.writeObject(output, bmd);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void read(final Kryo kryo, final Input input) {
 
 		this.path = new Path(input.readString());
 		this.length = input.readLong();
 		this.modificationTime = input.readLong();
 		final int numberOfBlocks = input.readInt();
 		for (int i = 0; i < numberOfBlocks; ++i) {
 			this.blocks.add(kryo.readObject(input, BlockMetaData.class));
 		}
 	}
 
 	Iterator<BlockMetaData> getBlockIterator() {
 
 		return Collections.unmodifiableList(this.blocks).iterator();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int compareTo(final FileMetaData o) {
 
		final long diff = this.length - o.length;
 		if (diff < (long) Integer.MIN_VALUE) {
 			return Integer.MIN_VALUE;
 		}
 
 		if (diff > (long) Integer.MAX_VALUE) {
 			return Integer.MAX_VALUE;
 		}
 
 		return (int) diff;
 	}
 }
