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
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Map;
 
 public class FileBlock implements Block {
 
 	private static final int DATASIZE = MultiFile.BLOCKSIZE - BlockHeader.BLOCKHEADERSIZE;
 	
 	BlockHeader header;
 	long startOffset;
 	
 	public FileBlock(long blockOffset) {
 		header = new BlockHeader(BlockType.FILE, 0, 0);
 		startOffset = blockOffset;
 	}
 	
 	@Override
 	public void write(RandomAccessFile raFile) throws IOException {
 		raFile.seek(startOffset);
 		header.write(raFile);
 		
 		if (raFile.getFilePointer() == raFile.length()) {
 			raFile.setLength(startOffset + MultiFile.BLOCKSIZE);
 		}
 	}
 
 	@Override
 	public void read(RandomAccessFile raFile) throws IOException {
 		raFile.seek(startOffset);
 		header = new BlockHeader();
 		header.read(raFile);
 	}
 
 	public int getSize() {
 		return header.getSize();
 	}
 	
 	@Override
 	public long getNextOffset() {
 		return header.getNextBlock();
 	}
 
 	@Override
 	public void setNextOffset(long offset) {
 		header.setNextBlock(offset);
 	}
 	
 	public int writeStream(RandomAccessFile raFile, byte[] data, int offset, int length) throws IOException {
 		int oldSize = header.getSize();
 		int growSize = oldSize + length;
 		
 		if (growSize > DATASIZE) {
 			header.setSize(DATASIZE);
 		} else {
 			header.setSize(growSize);
 		}
 		
 		raFile.seek(startOffset);
 		header.write(raFile);
 		int writeSize = Math.min(DATASIZE - oldSize, length);
 		raFile.seek(startOffset + BlockHeader.BLOCKHEADERSIZE + oldSize);
 		raFile.write(data, offset, writeSize);
 		
 		if (raFile.getFilePointer() == raFile.length()) {
			raFile.setLength(offset + MultiFile.BLOCKSIZE);
 		}
 		return writeSize;
 	}
 	
 	public String toString() {
 		return "FileBlock[OFFSET: " + startOffset + " SIZE: " + header.getSize() + " NEXT: " + header.getNextBlock() + "]";
 	}
 }
