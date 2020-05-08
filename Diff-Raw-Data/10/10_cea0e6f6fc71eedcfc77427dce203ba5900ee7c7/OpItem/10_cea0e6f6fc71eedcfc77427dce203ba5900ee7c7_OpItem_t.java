 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License.
  *
  */
 package com.taobao.common.store.journal;
 
 import com.google.common.base.Objects;
 
 import java.nio.ByteBuffer;
 
 
 /**
  * 一个日志记录 操作+数据key+数据文件编号+偏移量+长度
  *
  * @author dogun (yuexuqiang at gmail.com)
  */
 public class OpItem {
     public static final byte OP_ADD = 1;
     public static final byte OP_DEL = 2;
 
     public static final int KEY_LENGTH = 16;
     public static final int LENGTH = KEY_LENGTH + 1 + 4 + 8 + 4;
 
     byte op;
     byte[] key;
     int fileSerialNumber;
     volatile long offset;
     int length;
 
     public OpItem() {
     }
 
     public OpItem(byte op, byte[] key, int fileSerialNumber, long offset, int length) {
         this.op = op;
         this.key = key;
         this.fileSerialNumber = fileSerialNumber;
         this.offset = offset;
         this.length = length;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(op, key, fileSerialNumber, offset, length);
     }
 
 
     @Override
     public boolean equals(Object obj) {
        if (!(obj instanceof OpItem)) return false;
        OpItem that = (OpItem) obj;
        return Objects.equal(this.op, that.op) &&
                Objects.equal(this.key, that.key) &&
                Objects.equal(this.fileSerialNumber, that.fileSerialNumber) &&
                Objects.equal(this.offset, that.offset) &&
                Objects.equal(this.length, that.length);
     }
 
     /**
      * 将一个操作转换成字节数组
      *
      * @return 字节数组
      */
     public byte[] toByte() {
         byte[] data = new byte[LENGTH];
         ByteBuffer bf = ByteBuffer.wrap(data);
         bf.put(this.key);
         bf.put(this.op);
         bf.putInt(this.fileSerialNumber);
         bf.putLong(this.offset);
         bf.putInt(this.length);
         bf.flip();
         return bf.array();
     }
 
 
     public byte getOp() {
         return this.op;
     }
 
 
     public void setOp(byte op) {
         this.op = op;
     }
 
 
     public byte[] getKey() {
         return this.key;
     }
 
 
     public void setKey(byte[] key) {
         this.key = key;
     }
 
 
     public int getFileSerialNumber() {
         return this.fileSerialNumber;
     }
 
 
     public void setFileSerialNumber(int fileSerialNumber) {
         this.fileSerialNumber = fileSerialNumber;
     }
 
 
     public long getOffset() {
         return this.offset;
     }
 
 
     public void setOffset(long offset) {
         this.offset = offset;
     }
 
 
     public int getLength() {
         return this.length;
     }
 
 
     public void setLength(int length) {
         this.length = length;
     }
 
 
     /**
      * 通过字节数组构造成一个操作日志
      *
      * @param data
      */
     public void parse(byte[] data) {
         this.parse(data, 0, data.length);
     }
 
 
     public void parse(byte[] data, int offset, int length) {
         ByteBuffer bf = ByteBuffer.wrap(data, offset, length);
         this.key = new byte[16];
         bf.get(this.key);
         this.op = bf.get();
         this.fileSerialNumber = bf.getInt();
         this.offset = bf.getLong();
         this.length = bf.getInt();
     }
 
 
     public void parse(ByteBuffer bf) {
         this.key = new byte[16];
         bf.get(this.key);
         this.op = bf.get();
         this.fileSerialNumber = bf.getInt();
         this.offset = bf.getLong();
         this.length = bf.getInt();
     }
 
 
     @Override
     public String toString() {
         return "OpItem fileSerialNumber:" + this.fileSerialNumber + ", op:" + (int) this.op + ", offset:" + this.offset + ", length:"
                 + this.length;
     }
 }
