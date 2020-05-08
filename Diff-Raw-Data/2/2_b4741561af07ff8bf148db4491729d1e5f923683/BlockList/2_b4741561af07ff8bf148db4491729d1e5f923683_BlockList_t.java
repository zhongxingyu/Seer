 /*
 * BlockList an alternative java.util.List
  * Copyright 2011 MeBigFatGuy.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.blocklist;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 @SuppressWarnings("unchecked")
 public class BlockList<E> implements List<E>, Externalizable {
 
 	private static final long serialVersionUID = -2221663525758235084L;
 	public static final int DEFAULT_BLOCK_COUNT = 1;
 	public static final int DEFAULT_BLOCK_SIZE = 16;
 
 	private Block<E>[] blocks;
 	private int blockSize;
 	private int size;
 	private int revision;
 
 	public BlockList() {
 		this(DEFAULT_BLOCK_SIZE);
 	}
 
 	public BlockList(int blockSize) {
 		this(DEFAULT_BLOCK_COUNT, blockSize);
 	}
 
 	public BlockList(int initialBlkCount, int blkSize) {
 		blocks = new Block[initialBlkCount];
 		blockSize = blkSize;
 		size = 0;
 		for (int b = 0; b < blocks.length; b++) {
 			blocks[b] = new Block<E>();
 		}
 		revision = 0;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof BlockList) {
 			BlockList<E> that = (BlockList<E>) o;
 			if (this.size != that.size) {
 				return false;
 			}
 
 			for (int i = 0; i < size; i++) {
 				Object thisItem = get(i);
 				Object thatItem = that.get(i);
 
 				if (thisItem == null) {
 					if (thatItem != null) {
 						return false;
 					}
 				}
 
 				if (thatItem == null) {
 					return false;
 				}
 
 				if (!thisItem.equals(thatItem)) {
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		return size;
 	}
 
 	@Override
 	public boolean add(E element) {
 		long blockPtr = findBlock(size, true);
 		if (blockPtr < 0) {
 			grow();
 			blockPtr = ((long)(blocks.length - 1)) << 32;
 		}
 
 		int blkIndex = (int)(blockPtr >> 32);
 		Block<E> blk = blocks[blkIndex];
 		blk.slots[blk.emptyPos++] = element;
 		size++;
 		revision++;
 		return true;
 	}
 
 	@Override
 	public void add(int index, E element) {
 		long blockPtr = findBlock(index, true);
 		if (blockPtr < 0) {
 			grow();
 			blockPtr = ((long)(blocks.length - 1)) << 32;
 		}
 
 		int blkIndex = (int)(blockPtr >> 32);
 		int blkOffset = (int)blockPtr;
 
 		Block<E> blk = blocks[blkIndex];
 		if (blk.emptyPos == blockSize){
 			splitBlock(blkIndex, blkOffset);
 		} else if (blkOffset < blk.emptyPos) {
 			System.arraycopy(blk.slots, blkOffset, blk.slots, blkOffset + 1, blk.emptyPos - blkOffset);
 		}
 
 		blk.slots[blkOffset] = element;
 		blk.emptyPos++;
 		size++;
 		revision++;
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends E> elements) {
 		for (E e : elements) {
 			add(e);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean addAll(int index, Collection<? extends E> elements) {
 		for (E e : elements) {
 			add(index++, e);
 		}
 		return true;
 	}
 
 	@Override
 	public void clear() {
 		blocks = new Block[0];
 		size = 0;
 		for (int b = 0; b < blocks.length; b++) {
 			blocks[b] = new Block<E>();
 		}
 		revision++;
 	}
 
 	@Override
 	public boolean contains(Object element) {
 		if (element == null) {
 			return false;
 		}
 
 		for (Block<E> blk : blocks) {
 			for (int s = 0; s < blk.emptyPos; s++) {
 				if (element.equals(blk.slots[s])) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> elements) {
 		for (Object o : elements) {
 			if (!contains(o)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public E get(int index) {
 		long blockPtr = findBlock(index, false);
 		if (blockPtr < 0) {
 			throw new IndexOutOfBoundsException("Index (" + index + ") is out of bounds [0 <= i < " + size + "]");
 		}
 
 		int blkIndex = (int)(blockPtr >> 32);
 		int blkOffset = (int)blockPtr;
 
 		Block<E> blk = blocks[blkIndex];
 		return blk.slots[blkOffset];
 	}
 
 	@Override
 	public int indexOf(Object element) {
 		if (element == null) {
 			return -1;
 		}
 
 		int pos = 0;
 		for (Block<E> blk : blocks) {
 			for (int s = 0; s < blk.emptyPos; s++) {
 				if (element.equals(blk.slots[s])) {
 					return pos;
 				}
 				pos++;
 			}
 		}
 
 		return -1;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	@Override
 	public Iterator<E> iterator() {
 		return new BlockListIterator();
 	}
 
 	@Override
 	public int lastIndexOf(Object element) {
 		if (element == null) {
 			return -1;
 		}
 
 		int pos = size - 1;
 		for (int b = blocks.length - 1; b >= 0; b--) {
 			Block<E> blk = blocks[b];
 			for (int s = blk.emptyPos-1; s>= 0; s--) {
 				if (element.equals(blk.slots[s])) {
 					return pos;
 				}
 				pos--;
 			}
 		}
 
 		return -1;
 	}
 
 	@Override
 	public ListIterator<E> listIterator() {
 		throw new UnsupportedOperationException("listIterator");
 	}
 
 	@Override
 	public ListIterator<E> listIterator(int index) {
 		throw new UnsupportedOperationException("listIterator");
 	}
 
 	@Override
 	public boolean remove(Object element) {
 		int pos = indexOf(element);
 		if (pos < 0) {
 			revision++;
 			return false;
 		}
 
 		remove(pos);
 		return true;
 	}
 
 	@Override
 	public E remove(int index) {
 		long blockPtr = findBlock(index, false);
 		if (blockPtr < 0) {
 			revision++;
 			throw new IndexOutOfBoundsException("Index (" + index + ") is out of bounds [0 <= i < " + size + "]");
 		}
 
 		int blkIndex = (int)(blockPtr >> 32);
 		int blkOffset = (int)blockPtr;
 
 		return remove(blkIndex, blkOffset);
 	}
 
 	protected E remove(int blkIndex, int blkOffset) {
 		Block<E> blk = blocks[blkIndex];
 		E e = blk.slots[blkOffset];
 		if (blk.emptyPos == 1) {
 			System.arraycopy(blocks, blkIndex+1, blocks, blkIndex, blocks.length - blkIndex - 1);
 			blocks[blocks.length - 1] = blk;
 		} else {
 			System.arraycopy(blk.slots, blkOffset + 1, blk.slots, blkOffset, blk.emptyPos - blkOffset - 1);
 
 		}
 		blk.slots[blk.emptyPos-1] = null;
 		blk.emptyPos--;
 		size--;
 		revision++;
 		return e;
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> elements) {
 		boolean removed = false;
 		for (Object e : elements) {
 			removed |= remove(e);
 		}
 		return removed;
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> elements) {
 		boolean changed = false;
 
 		int pos = 0;
 		for (int b = 0; b < blocks.length; b++) {
 			Block<E> blk = blocks[b];
 			for (int s = 0; s < blk.emptyPos; s++) {
 
 				if (!elements.contains(blk.slots[s])) {
 					boolean blockRemoved = (blk.emptyPos == 1);
 					remove(pos);
 					changed = true;
 					s--;
 					if (blockRemoved) {
 						b--;
 					}
 				} else {
 					pos++;
 				}
 			}
 		}
 
 		revision++;
 		return changed;
 	}
 
 	@Override
 	public E set(int index, E element) {
 		long blockPtr = findBlock(index, false);
 		if (blockPtr < 0) {
 			throw new IndexOutOfBoundsException("Index (" + index + ") is out of bounds [0 <= i < " + size + "]");
 		}
 
 		int blkIndex = (int)(blockPtr >> 32);
 		int blkOffset = (int)blockPtr;
 
 		Block<E> blk = blocks[blkIndex];
 		E oldValue = blk.slots[blkOffset];
 		blk.slots[blkOffset] = element;
 		return oldValue;
 	}
 
 	@Override
 	public int size() {
 		return size;
 	}
 
 	@Override
 	public List<E> subList(int index, int length) {
 		throw new UnsupportedOperationException("BlockList.subList");
 	}
 
 	@Override
 	public Object[] toArray() {
 		Object[] o = new Object[size];
 		int pos = 0;
 		for (Block<E> blk : blocks) {
 			System.arraycopy(blk.slots, 0, o, pos, blk.emptyPos);
 			pos += blk.emptyPos;
 		}
 		return o;
 	}
 
 	@Override
 	public <AE> AE[] toArray(AE[] proto) {
 		if (proto.length < size) {
 			Class<?> cls = proto.getClass().getComponentType();
 			proto = (AE[])Array.newInstance(cls, size);
 		}
 
 		int pos = 0;
 		for (Block<E> blk : blocks) {
 			System.arraycopy(blk.slots, 0, proto, pos, blk.emptyPos);
 			pos += blk.emptyPos;
 		}
 		return proto;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder(size * 10);
 		String cr = "\n";
 		String sep = "";
 		for (Block<E> blk : blocks) {
 			sb.append(sep);
 			sb.append(blk);
 			sep = cr;
 		}
 		return sb.toString();
 	}
 
 	private long findBlock(int index, boolean forAdd) {
 		int offset = 0;
 
 		for (int b = 0; b < blocks.length; b++) {
 			Block<E> blk = blocks[b];
 			int nextOffset = offset + blk.emptyPos;
 			if ((index < nextOffset) || (forAdd && ((index == nextOffset) && (blk.emptyPos < blockSize)))) {
 				return (((long) b) << 32) | (index - offset);
 			}
 			offset = nextOffset;
 		}
 		return -1L;
 	}
 
 	private void grow() {
 		Block<E>[] newBlocks = new Block[blocks.length+1];
 		System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
 		newBlocks[blocks.length] = new Block<E>();
 		blocks = newBlocks;
 	}
 
 	private void splitBlock(int blockIndex, int blockOffset) {
 		Block<E>[] newBlocks = new Block[blocks.length+1];
 		System.arraycopy(blocks, 0, newBlocks, 0, blockIndex + 1);
 		System.arraycopy(blocks, blockIndex + 1, newBlocks, blockIndex+2, blocks.length - blockIndex - 1);
 		newBlocks[blockIndex+1] = new Block<E>();
 		System.arraycopy(newBlocks[blockIndex].slots, blockOffset, newBlocks[blockIndex+1].slots, 0, newBlocks[blockIndex].emptyPos - blockOffset);
 		newBlocks[blockIndex+1].emptyPos = newBlocks[blockIndex].emptyPos - blockOffset;
 		newBlocks[blockIndex].emptyPos = blockIndex;
 		blocks = newBlocks;
 	}
 
 	private class Block<B> implements Serializable {
 
 		private static final long serialVersionUID = 6572073165518926427L;
 
 		B[] slots;
 		int emptyPos;
 
 		Block() {
 			slots = (B[])new Object[blockSize];
 			emptyPos = 0;
 		}
 
 		@Override
 		public String toString() {
 			return (emptyPos) + "|" + Arrays.toString(slots);
 		}
 	}
 
 	private class BlockListIterator implements Iterator<E> {
 
 		private int pos = 0;
 		private int iteratorRevision = revision;
 
 
 		@Override
 		public boolean hasNext() {
 			if (revision != iteratorRevision) {
 				throw new ConcurrentModificationException();
 			}
 
 			return pos < size;
 		}
 
 		@Override
 		public E next() {
 			if (revision != iteratorRevision) {
 				throw new ConcurrentModificationException();
 			}
 
 			if (pos >= size) {
 				throw new IndexOutOfBoundsException("");
 			}
 
 			return get(pos++);
 		}
 
 		@Override
 		public void remove() {
 			if (revision != iteratorRevision) {
 				throw new ConcurrentModificationException();
 			}
 
 			BlockList.this.remove(pos);
 			iteratorRevision = revision;
 		}
 
 	}
 
 	@Override
 	public void writeExternal(ObjectOutput out) throws IOException {
 		out.writeInt(blockSize);
 		out.writeInt(size);
 
 		for (Block<E> blk : blocks) {
 			for (int s = 0; s < blk.emptyPos; s++) {
 				out.writeObject(blk.slots[s]);
 			}
 		}
 	}
 
 	@Override
 	public void readExternal(ObjectInput in) throws IOException,
 			ClassNotFoundException {
 
 		blockSize = in.readInt();
 		size = in.readInt();
 
 		int numBlocks = (size + (blockSize - 1)) / blockSize;
 		if (numBlocks > DEFAULT_BLOCK_COUNT) {
 			blocks = new Block[numBlocks];
 		}
 
 		int pos = 0;
 		for (int i = 0; i < numBlocks; i++) {
 			Block<E> block = new Block<E>();
 			int j;
 			for (j = 0; j < blockSize; j++) {
 				if (pos++ >= size) {
 					break;
 				}
 
 				block.slots[j] = (E)in.readObject();
 			}
 			block.emptyPos = j;
 			blocks[i] = block;
 		}
 	}
 }
