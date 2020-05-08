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
 package org.araqne.logstorage.file;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.araqne.logstorage.Crypto;
 import org.araqne.storage.api.StorageInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DataBlockV3 {
 	private Logger logger = LoggerFactory.getLogger("log-file-reader-v3-data-block");
 
 	final int BLOCK_LENGTH = 4;
 	final int BLOCK_VERSION = 1;
 	final int BLOCK_FLAG = 1;
 	final int MIN_TIME = 8;
 	final int MAX_TIME = 8;
 	final int MIN_ID = 8;
 	final int MAX_ID = 8;
 	final int ORIGINAL_SIZE = 4;
 	final int COMPRESS_SIZE = 4;
 	final int LENGTH_BLOCK_LENGTH = 4;
 	final int FIXED_DATA_HEADER_SIZE = BLOCK_LENGTH + BLOCK_VERSION + BLOCK_FLAG + MIN_TIME + MAX_TIME + MIN_ID + MAX_ID
 			+ ORIGINAL_SIZE + COMPRESS_SIZE + LENGTH_BLOCK_LENGTH;
 
 	byte version;
 	byte flag;
 	long minTime;
 	long maxTime;
 	long minId;
 	long maxId;
 	int originalSize;
 	int compressedSize;
 
 	int[] logOffsets;
 	byte[] iv;
 	byte[] signature;
 	
 	ByteBuffer dataBuffer;
 	
 	byte[] compressedBuffer;
 
 	long dataFp;
 
 	String compressionMethod;
 	DataBlockV3Params params;
 	
 	public DataBlockV3(DataBlockV3Params params)
 			throws IOException {
 		int length = 0;
 		int pos = 0;
 		try {
 			this.params = params;
 			dataFp = params.indexHeader.dataFp;
 			this.compressionMethod = params.compressionMethod;
 
 			ByteBuffer block = null;
 
 			if (block == null) {
 				ByteBuffer blockToCache = null;
 				StorageInputStream dataStream = params.dataStream;
 				synchronized (dataStream) {
 					dataStream.seek(dataFp);
 					length = dataStream.readInt();
 
 					blockToCache = ByteBuffer.allocate(length - 4);
 					dataStream.readBestEffort(blockToCache);
 				}
 				blockToCache.flip();
 				// potentionally buffer underflow
 				if (blockToCache.remaining() != length - 4) {
 					logger.warn("disk read underflow: expected: {}, read: {}", length - 4, blockToCache.remaining());
 				}
 				block = blockToCache;
 
 			}
 
 			ByteBuffer bb = ByteBuffer.allocate(FIXED_DATA_HEADER_SIZE - 4);
 			block.get(bb.array());
 
 			version = bb.get();
 			flag = bb.get();
 			minTime = bb.getLong();
 			maxTime = bb.getLong();
 			minId = bb.getLong();
 			maxId = bb.getLong();
 			originalSize = bb.getInt();
 			compressedSize = bb.getInt();
 			
 			// check fixed block
 			if (isFixed()) {
 				if ((originalSize & 0x80000000) != 0x80000000) {
 					logger.warn("logpresso logstorage: data block has been fixed. please check [{} : {}]",
 							params.dataPath.getAbsolutePath(), params.indexHeader.toString());
 				}
 				return;
 			}
 
 			int lengthBlockSize = bb.getInt();
 			byte[] lengthBytes = new byte[lengthBlockSize];
 			block.get(lengthBytes);
 
 			int logCount = (int) (maxId - minId + 1);
 			logOffsets = new int[logCount];
 
 			int bufpos = 0;
 			int acc = 0;
 			for (int i = 0; i < logCount; i++) {
 				logOffsets[i] = acc;
 
 				// read number (manual inlining)
 				long n = 0;
 				byte b;
 				do {
 					n <<= 7;
 					b = lengthBytes[bufpos++];
 					n |= b & 0x7f;
 				} while ((b & 0x80) != 0);
 
 				acc += n;
 			}
 
 			// cipher extension
 			if ((flag & 0x80) == 0x80) {
 				iv = new byte[16];
 				signature = new byte[32];
 				block.get(iv);
 				block.get(signature);
 			}
 
 			dataBuffer = null;
 			compressedBuffer = null;
 
 			pos = block.position();
 			if (compressionMethod != null) {
 				compressedBuffer = new byte[compressedSize];
 				block.get(compressedBuffer);
 			} else {
 				dataBuffer = ByteBuffer.allocate(originalSize);
 				block.get(dataBuffer.array(), 0, originalSize);
 			}
 		} catch (Throwable t) {
 			throw new IllegalStateException("exception on " + params.dataPath.getAbsolutePath() +" : block length - " + Integer.toString(length) + ", pos - " + Integer.toString(pos) + ", compressedSize - " + Integer.toString(compressedSize), t);
 		} 
 	}
 	
 	public boolean isFixed() {
 		return (flag & 0x40) == 0x40;
 	}
 
 	public void uncompress() throws IOException {
		if (dataBuffer == null && compressedBuffer != null) {
			if (params.cipher != null) {
				try {
					compressedBuffer = Crypto.decrypt(compressedBuffer, params.cipher, params.cipherKey, iv);
				} catch (Throwable t) {
					throw new IOException("cannot decrypt block", t);
				}
 			}
 		}
 
 		if (compressionMethod != null && dataBuffer == null) {
 			if ((this.flag & 0x20) == 0) {
 				Compression compression = newCompression();
 				dataBuffer = ByteBuffer.allocate(originalSize);
 				compression.uncompress(dataBuffer.array(), compressedBuffer, 0, compressedBuffer.length);
 				compression.close();
 				compressedBuffer = null;
 			} else {
 				dataBuffer = ByteBuffer.wrap(compressedBuffer, 0, compressedBuffer.length);
 			}
 		}
 	}
 
 	private Compression newCompression() {
 		if (compressionMethod.equals("snappy"))
 			return new SnappyCompression();
 		else
 			return new DeflaterCompression();
 	}
 
 	public byte getVersion() {
 		return version;
 	}
 
 	public byte getFlag() {
 		return flag;
 	}
 
 	public int getOriginalSize() {
 		return originalSize;
 	}
 
 	public byte[] getIv() {
 		return iv;
 	}
 
 	public byte[] getSignature() {
 		return signature;
 	}
 
 	public long getDataFp() {
 		return dataFp;
 	}
 
 	public long getMinId() {
 		return minId;
 	}
 
 	public long getMaxId() {
 		return maxId;
 	}
 
 	public int getCompressedSize() {
 		return compressedSize;
 	}
 	
 	public int getLogOffsetCount() {
 		return logOffsets.length;
 	}
 
 	public int getLogOffset(int idx) {
 		return logOffsets[idx];
 	}
 
 	public ByteBuffer getDataBuffer() {
 		return dataBuffer;
 	}
 
 	public byte[] getCompressedBuffer() {
 		return compressedBuffer;
 	}
 	
 	public String getDataHash() {
 		byte[] data = compressedBuffer;
 		if (compressionMethod == null) {
 			data = dataBuffer.array();
 		}
 		
         try {
 			return String.format("%X", new BigInteger(1, MessageDigest.getInstance("MD5").digest(data)));
 		} catch (NoSuchAlgorithmException e) {
 			// It is impossible there is no MD5 algorithm
 			throw new IllegalStateException(e);
 		}		
 	}
 
 }
