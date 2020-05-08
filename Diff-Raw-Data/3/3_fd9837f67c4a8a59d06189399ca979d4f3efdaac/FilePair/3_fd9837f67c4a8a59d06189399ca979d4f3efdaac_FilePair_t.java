 /*
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.storage.filepair;
 
 import java.io.ByteArrayInputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.security.DigestException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageInputStream;
 import org.araqne.storage.api.StorageOutputStream;
 import org.araqne.storage.localfile.LocalFileOutputStream;
 
 public abstract class FilePair<IB extends IndexBlock<IB>, RDB extends RawDataBlock<RDB>> {
 	protected FilePath ifile;
 	protected FilePath dfile;
 	
 	public FilePair(FilePath indexFile, FilePath dataFile) {
 		this.ifile = indexFile;
 		this.dfile = dataFile;
 	}
 
 	public FilePath getIndexFile() {
 		return ifile;
 	}
 
 	public FilePath getDataFile() {
 		return dfile;
 	}
 
 	public abstract void writeIndexFileHeader(OutputStream os) throws IOException;
 
 	public abstract void writeDataFileHeader(OutputStream os) throws IOException;
 	
 	// may be same with body offset
 	public abstract long getIndexFileHeaderLength() throws IOException;
 	
 	public abstract long getDataFileHeaderLength() throws IOException;
 	
 	public abstract int getIndexBlockCount() throws IOException;
 	
 	public abstract IB getIndexBlock(int id) throws IOException;
 
 	public CloseableEnumeration<IB> getIndexBlocks(Class<IB> ibClass) throws IOException {
 		return new IndexBlockEnumeration(ibClass);
 	}
 
 	public abstract RDB getRawDataBlock(IB indexBlock) throws IOException;
 
 	public void reserveBlocks(List<IB> addedBlocks) throws IOException {
 		StorageOutputStream indexStream = null;
 		StorageOutputStream dataStream = null;
 		try {
 			indexStream = ifile.newOutputStream(true);
 			dataStream = dfile.newOutputStream(true);
 
 			long dflen = dfile.length();
 
 			if (dataStream instanceof LocalFileOutputStream) {
 				LocalFileOutputStream lDataStream = (LocalFileOutputStream) dataStream;
 				for (IB block : addedBlocks) {
 					IB reservedBlock = block.newReservedBlock();
 
 					lDataStream.setLength(dflen + reservedBlock.getDataBlockLen());
 					dflen += reservedBlock.getDataBlockLen();
 					reservedBlock.serialize(indexStream);
 				}
 			} else {
 				throw new UnsupportedOperationException("the operation for non-local file is not supported yet");
 			}
 		} finally {
 			ensureClose(indexStream);
 			ensureClose(dataStream);
 		}
 	}
 
 	public void replaceBlock(IB indexBlock, RDB rawDataBlock) throws IOException {
 		IB reservedIndexBlock = getIndexBlock(indexBlock.getId());
 		if (!reservedIndexBlock.isReserved())
 			throw new IllegalArgumentException("the block is not reserved block");
 		
 		if (reservedIndexBlock.getPosOnData() != indexBlock.getPosOnData())
 			throw new IllegalArgumentException("pos on data file is different");
 
 		StorageOutputStream dataStream = null;
 		StorageOutputStream indexStream = null;
 		try {
 			dataStream = dfile.newOutputStream(false);
 			indexStream = ifile.newOutputStream(false);
 
 			if (dataStream instanceof LocalFileOutputStream && indexStream instanceof LocalFileOutputStream) {
 				LocalFileOutputStream lDataStream = (LocalFileOutputStream) dataStream;
 				LocalFileOutputStream lIndexStream = (LocalFileOutputStream) indexStream;
 				lDataStream.seek(indexBlock.getPosOnData());
 				rawDataBlock.serialize(lDataStream);
 
 				lIndexStream.seek(getIndexFileHeaderLength() + indexBlock.getBlockSize() * indexBlock.getId());
 				indexBlock.serialize(lIndexStream);
 			} else {
 				throw new UnsupportedOperationException("the operation for non-local file is not supported yet");
 			}
 
 		} finally {
 			ensureClose(dataStream);
 			ensureClose(indexStream);
 		}
 
 	}
 	
 	public static String calcHash(ByteBuffer bb) {
 		ByteBuffer buf = bb.asReadOnlyBuffer();
 		try {
 			if (buf.hasArray()) {
 				try {
 					MessageDigest md5 = MessageDigest.getInstance("MD5");
 					md5.digest(buf.array(), buf.position(), buf.remaining());
 
 					return String.format("%X", new BigInteger(1, md5.digest()));
 				} catch (DigestException e) {
 					return null;
 				}
 			} else {
 				MessageDigest md5 = MessageDigest.getInstance("MD5");
 				ByteBuffer b = buf.asReadOnlyBuffer();
 				byte[] array = new byte[b.remaining()];
 				b.get(array);
 				return String.format("%X", new BigInteger(1, md5.digest(array)));
 			}
 		} catch (NoSuchAlgorithmException e1) {
 			return null;
 		}
 	}
 
 	public static void ensureClose(Closeable stream) {
 		try {
 			if (stream != null)
 				stream.close();
 		} catch (IOException e) {
 		}
 	}
 	
 	private class IndexBlockEnumeration implements CloseableEnumeration<IB> {
 		private StorageInputStream stream;
 		private long fileLength;
 		private long dataStreamLength;
 		private long segCount;
 		private int currentSegId;
 		private IB next;
 		private IB prefetched;
 		private Class<IB> ibClass;
 		private IB ib;
 
 		public IndexBlockEnumeration(Class<IB> ibClass) throws IOException {
 			try {
 				this.stream = ifile.newInputStream();
 				this.dataStreamLength = dfile.length();
 				this.fileLength = stream.length();
 				this.ib = ibClass.newInstance();
 				this.ibClass = ibClass;
 				this.segCount = (fileLength - getIndexFileHeaderLength()) / ib.getBlockSize();
 
 				this.stream.seek(getIndexFileHeaderLength());
 
 				this.currentSegId = 0;
 			} catch (InstantiationException e) {
 				throw new IllegalArgumentException("ibClass is not class instance of IB", e);
 			} catch (IllegalAccessException e) {
 				throw new IllegalArgumentException("ibClass is not class instance of IB", e);
 			}
 		}
 
 		@Override
 		public boolean hasMoreElements() {
 			if (this.stream == null)
 				return false;
 
 			if (next != null)
 				return true;
 
			if (currentSegId == segCount)
				return false;
			
 			try {
 				if (currentSegId == 0) {
 					ByteBuffer b = ByteBuffer.allocate(ib.getBlockSize());
 					readIndexBlock(b);
 					
 					IB newInstance = (IB) ibClass.newInstance();
 					next = newInstance.unserialize(currentSegId, new ByteArrayInputStream(b.array()));
 				} else {
 					next = prefetched;
 				}
 
 				if (currentSegId < segCount - 1) {
 					ByteBuffer b = ByteBuffer.allocate(ib.getBlockSize());
 					readIndexBlock(b);
 					prefetched = ibClass.newInstance().unserialize(currentSegId + 1, new ByteArrayInputStream(b.array()));
 					next.setDataBlockLen(prefetched.getPosOnData() - next.getPosOnData());
 				} else {
 					next.setDataBlockLen(dataStreamLength - next.getPosOnData());
 				}
 
 				boolean hasNext = currentSegId < segCount;
 
 				currentSegId += 1;
 
 				return hasNext;
 			} catch (Throwable t) {
 				throw new IllegalStateException(t);
 			}
 		}
 
 		@Override
 		public IB nextElement() {
 			IB next = this.next;
 
 			this.next = null;
 
 			return next;
 		}
 
 		private void readIndexBlock(ByteBuffer pfb) throws IOException {
 			this.stream.readBestEffort(pfb);
 			pfb.flip();
 			pfb.order(ByteOrder.BIG_ENDIAN);
 		}
 
 		@Override
 		public void close() throws IOException {
 			if (this.stream != null)
 				ensureClose(this.stream);
 			this.stream = null;
 		}
 	}
 }
