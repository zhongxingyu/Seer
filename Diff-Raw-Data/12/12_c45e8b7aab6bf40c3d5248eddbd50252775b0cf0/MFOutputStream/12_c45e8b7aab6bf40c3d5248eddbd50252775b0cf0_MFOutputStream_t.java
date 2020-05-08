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
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 
 class MFOutputStream extends OutputStream {
 
 	private RandomAccessFile raFile;
 	private long startOffset;
 	private long currentBlockOffset;
 	
 	public MFOutputStream(RandomAccessFile file, long offset) throws IOException {
 		raFile = file;
 		startOffset = offset;
 		currentBlockOffset = offset;
 		FileBlock block = new FileBlock(startOffset);
 		block.write(raFile);
 	}
 	
 	@Override
 	public void write(int b) throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		write(new byte[] { (byte) b }, 0, 1);
 	}
 
 	@Override
 	public void write(byte[] b) throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		write(b, 0, b.length);
 	}
 
 	@Override
 	public void write(byte[] b, int off, int len) throws IOException {
 		if (raFile == null) {
 			throw new IOException("Stream already closed");
 		}
 		
 		FileBlock block = new FileBlock(currentBlockOffset);
 		block.read(raFile);
 		int writeLen = block.writeStream(raFile, b, off, len);
 		off += writeLen;
 		len -= writeLen;
 		
 		while (len > 0) {
			currentBlockOffset = raFile.length();
 			block.setNextOffset(currentBlockOffset);
 			block.write(raFile);
			FileBlock newBlock = new FileBlock(currentBlockOffset);
 			writeLen = newBlock.writeStream(raFile, b, off, len);
 			off += writeLen;
 			len -= writeLen;
 		}
 	}
 
 	@Override
 	public void flush() throws IOException {
 	}
 
 	@Override
 	public void close() throws IOException {
 		raFile = null;
 	}
 }
