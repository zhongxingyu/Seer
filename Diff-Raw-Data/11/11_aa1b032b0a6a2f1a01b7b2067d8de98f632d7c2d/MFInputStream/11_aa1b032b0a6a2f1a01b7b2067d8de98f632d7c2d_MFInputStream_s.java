 /*
  * MultiFile - A single file store of multiple streams
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
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
 package com.mebigfatguy.multifile;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 
 class MFInputStream extends InputStream {
 
 	RandomAccessFile raFile;
 	long currentOffset;
 	long mark;
 	
 	public MFInputStream(RandomAccessFile file, long offset) {
 		raFile = file;
 		currentOffset = offset + BlockHeader.BLOCKHEADERSIZE;
 		mark = 0;
 	}
 	
 	@Override
 	public int read() throws IOException {	
 		byte[] data = new byte[1];	
 		int len = read(data, 0, 1);
 		if (len == 0)
 			return -1;
 		
 		return data[0] & 0x00FF;
 	}
 
 	@Override
 	public int read(byte[] b) throws IOException {
 		return read(b, 0, b.length);
 	}
 
 	@Override
 	public int read(byte[] b, int off, int len) throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		if (currentOffset == 0) {
 			throw new EOFException("End of file reached");
 		}
 		
 		long currentBlockOffset = (currentOffset / MultiFile.BLOCKSIZE) * MultiFile.BLOCKSIZE;
 		FileBlock block = new FileBlock(currentBlockOffset);
 		block.read(raFile);
 		
 		int availableInBlock = (int)(block.getSize() - (currentOffset - currentBlockOffset - BlockHeader.BLOCKHEADERSIZE));
 		int readLen = Math.min(len, availableInBlock);
 		
 		raFile.seek(currentOffset);
 		raFile.read(b, off, readLen);
 		currentOffset += readLen;
 		
 		if (currentOffset == (currentBlockOffset + MultiFile.BLOCKSIZE)) {
 			long next = block.getNextOffset();
 			if (next == 0) {
 				currentOffset = 0;
 			} else {
				currentOffset = block.getNextOffset() + MultiFile.BLOCKSIZE;
 			}
 		}
 		return readLen;
 	}
 
 	@Override
 	public long skip(long n) throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int available() throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void close() throws IOException {
 		raFile = null;
 	}
 
 	@Override
 	public synchronized void mark(int readlimit) {
 		mark = currentOffset;
 	}
 
 	@Override
 	public synchronized void reset() throws IOException {
 		if (mark == 0) {
 			throw new IOException("Mark never set");
 		}
 		
 		currentOffset = mark;
 	}
 
 	@Override
 	public boolean markSupported() {
 		return true;
 	}
 }
