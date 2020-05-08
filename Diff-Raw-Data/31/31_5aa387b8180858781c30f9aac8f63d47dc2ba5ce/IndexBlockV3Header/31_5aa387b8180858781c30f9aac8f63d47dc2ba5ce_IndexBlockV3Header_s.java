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
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 
 import org.araqne.storage.api.StorageUtil;
 import org.araqne.storage.filepair.IndexBlock;
 
 public class IndexBlockV3Header extends IndexBlock<IndexBlockV3Header>{
 	public static final int ITEM_SIZE = 28;
 	private int id;
 	
 	private boolean isReserved;
 	
 	// index header file offset
 	public long fp;
 	public long firstId;
 
 	public long dataFp;
 	public long minTime;
 	public long maxTime;
 	public int logCount;
 
 	// except this block's log count
 	public long ascLogCount;
 	public long dscLogCount;
 	private long dataBlockLen;
 
 	// for unserialize
 	public IndexBlockV3Header() {
 	}
 	
 	public IndexBlockV3Header(int id, long datafp, long minTime, long maxTime, int logCount, long firstId) {
 		this(id, datafp, minTime, maxTime, logCount, firstId, false);
 	}
 	
 	public IndexBlockV3Header(int id, long datafp, long minTime, long maxTime, int logCount, long firstId, boolean isReserved) {
 		this.id = id;
 		this.isReserved = isReserved;
 		this.dataFp = Math.abs(datafp);
 		this.minTime = minTime;
 		this.maxTime = maxTime;
 		this.logCount = logCount;
 		this.firstId = firstId;
 	}
 
 	@Override
 	public String toString() {
 		return "index block header, fp=" + fp + ", first_id=" + firstId + ", count=" + logCount + ", asc=" + ascLogCount
 				+ ", dsc=" + dscLogCount + "]";
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public boolean isReserved() {
 		return isReserved;
 	}
 
 	@Override
 	public long getPosOnData() {
 		return dataFp;
 	}
 
 	@Override
 	public int getBlockSize() {
 		return ITEM_SIZE;
 	}
 
 	@Override
 	public void serialize(OutputStream os) throws IOException {
 		byte[] longbuf = new byte[8];
 		byte[] intbuf = new byte[4];
 		
 		long datFp = isReserved? (-1 * dataFp): dataFp;
 		prepareLong(datFp, longbuf);
 		os.write(longbuf);
 		prepareLong(minTime, longbuf);
 		os.write(longbuf);
 		prepareLong(maxTime, longbuf);
 		os.write(longbuf);
 		prepareInt(logCount, intbuf);
 		os.write(intbuf);
 	}
 
 	@Override
 	public IndexBlockV3Header unserialize(int blockId, InputStream is) throws IOException {
 		ByteBuffer bb = ByteBuffer.allocate(IndexBlockV3Header.ITEM_SIZE);
 		StorageUtil.readFully(is, bb);
 		long dataFp = bb.getLong();
 		boolean isReserved = dataFp < 0;
 		return new IndexBlockV3Header(blockId, Math.abs(dataFp), bb.getLong(), bb.getLong(), bb.getInt(), -1, isReserved);
 	}
 
 	@Override
 	public long getDataBlockLen() {
 		return dataBlockLen;
 	}
 
 	@Override
 	public IndexBlockV3Header newReservedBlock() {
		return new IndexBlockV3Header(id, dataFp, minTime, maxTime, logCount, firstId, true);
 	}
 
 	@Override
 	public void setDataBlockLen(long dataBlockLen) {
 		this.dataBlockLen = dataBlockLen;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		IndexBlockV3Header other = (IndexBlockV3Header) obj;
 		if (id != other.id)
 			return false;
 		if (minTime != other.minTime)
 			return false;
 		if (maxTime != other.maxTime)
 			return false;
 		if (dataFp != other.dataFp)
 			return false;
 		if (dataBlockLen != other.dataBlockLen)
 			return false;
 		return true;
 	}
 
 	static void prepareInt(int l, byte[] b) {
 		for (int i = 0; i < 4; i++)
 			b[i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
 	}
 
 	static void prepareLong(long l, byte[] b) {
 		for (int i = 0; i < 8; i++)
 			b[i] = (byte) ((l >> ((7 - i) * 8)) & 0xff);
 	}
 
 }
