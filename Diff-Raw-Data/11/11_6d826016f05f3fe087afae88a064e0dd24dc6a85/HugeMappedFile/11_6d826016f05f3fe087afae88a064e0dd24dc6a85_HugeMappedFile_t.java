 package net.yeputons.cscenter.dbfall2013.util;
 
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: e.suvorov
  * Date: 11.10.13
  * Time: 21:21
  * To change this template use File | Settings | File Templates.
  */
 public class HugeMappedFile {
     static final int MAP_STEP_K = 30;
     static final int MAP_STEP = 1 << MAP_STEP_K;
     static final int MAP_STEP_MSK = MAP_STEP - 1;
     protected FileChannel ch;
     protected MappedByteBuffer[] bufs;
     protected long currentPosition;
 
     public HugeMappedFile(FileChannel ch) throws IOException {
         this.ch = ch;
         updateMapping();
         currentPosition = 0;
     }
 
     void updateMapping() throws IOException {
         bufs = new MappedByteBuffer[(int)((ch.size() + MAP_STEP - 1) / MAP_STEP)];
         int ptr = 0;
         for (long i = 0; i < ch.size();) {
             long size = Math.min(MAP_STEP, ch.size() - i);
             bufs[ptr] = ch.map(FileChannel.MapMode.READ_WRITE, i, size);
             i += size;
             ptr++;
         }
         assert ptr == bufs.length;
     }
 
     public void force() {
         for (int i = 0; i < bufs.length; i++)
             bufs[0].force();
     }
 
     protected MappedByteBuffer getMap(long position) {
         return bufs[(int)(position >> MAP_STEP_K)];
     }
 
     public byte get(long position) {
         return getMap(position).get((int)(position & MAP_STEP_MSK));
     }
     public void put(long position, byte value) {
         getMap(position).put((int)(position & MAP_STEP_MSK), value);
     }
 
     public void get(long position, byte[] value) {
         int i = 0;
         while (i < value.length) {
             long startPos = position;
             long endPos = Math.min(startPos + value.length - i, (startPos | MAP_STEP_MSK) + 1);
            MappedByteBuffer buf = getMap(startPos);
             buf.position((int)(position & MAP_STEP_MSK));
             buf.get(value, i, (int)(endPos - startPos));
             i += endPos - startPos;
            position = endPos;
         }
     }
     public void put(long position, byte[] value) {
         int i = 0;
         while (i < value.length) {
             long startPos = position;
             long endPos = Math.min(startPos + value.length - i, (startPos | MAP_STEP_MSK) + 1);
            MappedByteBuffer buf = getMap(startPos);
            buf.position((int)(startPos & MAP_STEP_MSK));
             buf.put(value, i, (int)(endPos - startPos));
             i += endPos - startPos;
            position = endPos;
         }
     }
 
     public int getInt(long position) {
         int res = 0;
         res |= (get(position + 0) & 0xFF) << 24;
         res |= (get(position + 1) & 0xFF) << 16;
         res |= (get(position + 2) & 0xFF) <<  8;
         res |= (get(position + 3) & 0xFF) <<  0;
         return res;
     }
     public void putInt(long position, int value) {
         put(position + 0, (byte)((value >> 24) & 0xFF));
         put(position + 1, (byte)((value >> 16) & 0xFF));
         put(position + 2, (byte)((value >>  8) & 0xFF));
         put(position + 3, (byte)((value >>  0) & 0xFF));
     }
 
     public long getLong(long position) {
         long res = 0;
         res |= (get(position + 0) & 0xFF) << 56;
         res |= (get(position + 1) & 0xFF) << 48;
         res |= (get(position + 2) & 0xFF) << 40;
         res |= (get(position + 3) & 0xFF) << 32;
         res |= (get(position + 4) & 0xFF) << 24;
         res |= (get(position + 5) & 0xFF) << 16;
         res |= (get(position + 6) & 0xFF) <<  8;
         res |= (get(position + 7) & 0xFF) <<  0;
         return res;
     }
     public void putLong(long position, long value) {
         put(position + 0, (byte)((value >> 56) & 0xFF));
         put(position + 1, (byte)((value >> 48) & 0xFF));
         put(position + 2, (byte)((value >> 40) & 0xFF));
         put(position + 3, (byte)((value >> 32) & 0xFF));
         put(position + 4, (byte)((value >> 24) & 0xFF));
         put(position + 5, (byte)((value >> 16) & 0xFF));
         put(position + 6, (byte)((value >>  8) & 0xFF));
         put(position + 7, (byte)((value >>  0) & 0xFF));
     }
 
     // Methods which use currentPosition
     public void position(long newPosition) {
         currentPosition = newPosition;
     }
 
     public int getInt() {
         int res = getInt(currentPosition);
         currentPosition += 4;
         return res;
     }
 
     public void putInt(int value) {
         putInt(currentPosition, value);
         currentPosition += 4;
     }
 
     public long getLong() {
         long res = getLong(currentPosition);
         currentPosition += 8;
         return res;
     }
 
     public void putLong(long value) {
         putLong(currentPosition, value);
         currentPosition += 8;
     }
 
     public byte get() {
         byte res = get(currentPosition);
         currentPosition += 1;
         return res;
     }
     public void put(byte value) {
         put(currentPosition, value);
         currentPosition += 1;
     }
 
     public void get(byte[] value) {
         get(currentPosition, value);
         currentPosition += value.length;
     }
 
     public void put(byte[] value) {
         put(currentPosition, value);
         currentPosition += value.length;
     }
 }
