 /*
  * Created on Wednesday, June 01 2011 03:46
  */
 package com.mbien.opencl.net.util;
 
 import com.jogamp.common.nio.NativeBuffer;
 import java.io.IOException;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.DoubleBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.LongBuffer;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 
 import static com.jogamp.common.nio.Buffers.*;
 
 /**
  * Buffer utilities primary intended for buffer serialization.
  * @author Michael Bien
  */
 public class NetBuffers {
 
     private NetBuffers() { }
 
 
     public static byte readByte(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         dest.limit(SIZEOF_BYTE);
         channel.read(dest);
         dest.rewind();
         return dest.get(0);
     }
 
     public static int readInt(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         dest.limit(SIZEOF_INT);
         channel.read(dest);
         dest.rewind();
         return dest.getInt(0);
     }
 
     public static long readLong(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         dest.limit(SIZEOF_LONG);
         channel.read(dest);
         dest.rewind();
         return dest.getLong(0);
     }
 
     public static float readFloat(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         dest.limit(SIZEOF_FLOAT);
         channel.read(dest);
         dest.rewind();
         return dest.getFloat(0);
     }
 
     public static double readDouble(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         dest.limit(SIZEOF_DOUBLE);
         channel.read(dest);
         dest.rewind();
         return dest.getDouble(0);
     }
 
 
     public static ByteBuffer readBuffer(ReadableByteChannel channel, ByteBuffer dest) throws IOException {
         channel.read(dest);
         dest.clear();
         return dest;
     }
 
     public static <T extends NativeBuffer> T readBuffer(ReadableByteChannel channel, T dest) throws IOException {
         readBuffer(channel, dest.getBuffer());
         return dest;
     }
     
     public static IntBuffer readBuffer(ReadableByteChannel channel, IntBuffer dest, ByteBuffer temp) throws IOException {
         temp.rewind();
        temp.limit(dest.remaining()*SIZEOF_INT);
         channel.read(temp);
        for(int i = 0; i < dest.remaining(); i++) {
             dest.put(temp.getInt(i*SIZEOF_INT));
         }
         return dest;
     }
 
     public static LongBuffer readBuffer(ReadableByteChannel channel, LongBuffer dest, ByteBuffer temp) throws IOException {
         temp.rewind();
        temp.limit(dest.remaining()*SIZEOF_LONG);
         channel.read(temp);
        for(int i = 0; i < dest.remaining(); i++) {
             dest.put(temp.getInt(i*SIZEOF_LONG));
         }
         return dest;
     }
 
     public static FloatBuffer readBuffer(ReadableByteChannel channel, FloatBuffer dest, ByteBuffer temp) throws IOException {
         temp.rewind();
        temp.limit(dest.remaining()*SIZEOF_FLOAT);
         channel.read(temp);
        for(int i = 0; i < dest.remaining(); i++) {
             dest.put(temp.getInt(i*SIZEOF_FLOAT));
         }
         return dest;
     }
 
     public static DoubleBuffer readBuffer(ReadableByteChannel channel, DoubleBuffer dest, ByteBuffer temp) throws IOException {
         temp.rewind();
        temp.limit(dest.remaining()*SIZEOF_DOUBLE);
         channel.read(temp);
        for(int i = 0; i < dest.remaining(); i++) {
             dest.put(temp.getInt(i*SIZEOF_DOUBLE));
         }
         return dest;
     }
 
     public static <T extends Buffer> T readBuffer(ReadableByteChannel channel, T dest, ByteBuffer temp) throws IOException {
         if(dest instanceof ByteBuffer) {
             return (T) readBuffer(channel, (ByteBuffer)dest);
         }else if(dest instanceof IntBuffer) {
             return (T) readBuffer(channel, (IntBuffer)dest, temp);
         }else if(dest instanceof LongBuffer) {
             return (T) readBuffer(channel, (LongBuffer)dest, temp);
         }else if(dest instanceof FloatBuffer) {
             return (T) readBuffer(channel, (FloatBuffer)dest, temp);
         }else if(dest instanceof DoubleBuffer) {
             return (T) readBuffer(channel, (DoubleBuffer)dest, temp);
         }else{
             throw new IllegalArgumentException("unsupported buffer "+dest);
         }
     }
 
     public static String readString(ReadableByteChannel channel, int length) throws IOException {
         if(length < 0) {
             return null;
         }else if(length == 0) {
             return "";
         }
 
         ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.nativeOrder());
         channel.read(buffer);
         buffer.rewind();
         return new String(buffer.array());
     }
 
     public static byte[] readArray(ReadableByteChannel channel, byte[] array, ByteBuffer temp) throws IOException {
         temp.rewind();
         temp.limit(array.length*SIZEOF_BYTE);
         channel.read(temp);
         for (int i = 0; i < array.length; i++) {
             array[i] = temp.get(i*SIZEOF_BYTE);
         }
         return array;
     }
 
     public static int[] readArray(ReadableByteChannel channel, int[] array, ByteBuffer temp) throws IOException {
         temp.rewind();
         temp.limit(array.length*SIZEOF_INT);
         channel.read(temp);
         for (int i = 0; i < array.length; i++) {
             array[i] = temp.getInt(i*SIZEOF_INT);
         }
         return array;
     }
 
     public static long[] readArray(ReadableByteChannel channel, long[] array, ByteBuffer temp) throws IOException {
         temp.rewind();
         temp.limit(array.length*SIZEOF_LONG);
         channel.read(temp);
         for (int i = 0; i < array.length; i++) {
             array[i] = temp.getLong(i*SIZEOF_LONG);
         }
         return array;
     }
 
     public static float[] readArray(ReadableByteChannel channel, float[] array, ByteBuffer temp) throws IOException {
         temp.rewind();
         temp.limit(array.length*SIZEOF_FLOAT);
         channel.read(temp);
         for (int i = 0; i < array.length; i++) {
             array[i] = temp.getFloat(i*SIZEOF_FLOAT);
         }
         return array;
     }
 
     public static double[] readArray(ReadableByteChannel channel, double[] array, ByteBuffer temp) throws IOException {
         temp.rewind();
         temp.limit(array.length*SIZEOF_DOUBLE);
         channel.read(temp);
         for (int i = 0; i < array.length; i++) {
             array[i] = temp.getDouble(i*SIZEOF_DOUBLE);
         }
         return array;
     }
 
     public static void writeInt(WritableByteChannel channel, ByteBuffer temp, int value) throws IOException {
         temp.limit(SIZEOF_INT);
         temp.putInt(0, value);
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeLong(WritableByteChannel channel, ByteBuffer temp, long value) throws IOException {
         temp.limit(SIZEOF_LONG);
         temp.putLong(0, value);
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeArray(WritableByteChannel channel, ByteBuffer temp, byte[] value) throws IOException {
         temp.limit(SIZEOF_BYTE*value.length);
         for (int i = 0; i < value.length; i++) {
             temp.put(value[i]);
         }
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeArray(WritableByteChannel channel, ByteBuffer temp, int[] value) throws IOException {
         temp.limit(SIZEOF_INT*value.length);
         for (int i = 0; i < value.length; i++) {
             temp.putInt(value[i]);
         }
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeArray(WritableByteChannel channel, ByteBuffer temp, long[] value) throws IOException {
         temp.limit(SIZEOF_LONG*value.length);
         for (int i = 0; i < value.length; i++) {
             temp.putLong(value[i]);
         }
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeArray(WritableByteChannel channel, ByteBuffer temp, float[] value) throws IOException {
         temp.limit(SIZEOF_FLOAT*value.length);
         for (int i = 0; i < value.length; i++) {
             temp.putFloat(value[i]);
         }
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void writeArray(WritableByteChannel channel, ByteBuffer temp, double[] value) throws IOException {
         temp.limit(SIZEOF_DOUBLE*value.length);
         for (int i = 0; i < value.length; i++) {
             temp.putDouble(value[i]);
         }
         temp.rewind();
         channel.write(temp);
         temp.clear();
     }
 
     public static void putBuffer(ByteBuffer dest, Buffer source) {
         if(source == null) {
             putBuffer(dest, (ByteBuffer)null);
         }else if(source instanceof ByteBuffer) {
             putBuffer(dest, (ByteBuffer)source);
         }else if(source instanceof IntBuffer) {
             putBuffer(dest, (IntBuffer)source);
         }else if(source instanceof LongBuffer) {
             putBuffer(dest, (LongBuffer)source);
         }else if(source instanceof FloatBuffer) {
             putBuffer(dest, (FloatBuffer)source);
         }else if(source instanceof DoubleBuffer) {
             putBuffer(dest, (DoubleBuffer)source);
         }else{
             throw new IllegalArgumentException("unsupported buffer "+source);
         }
     }
 
     public static void putBuffer(ByteBuffer dest, NativeBuffer source) {
         if(source == null) {
             putBuffer(dest, (ByteBuffer)null);
         }else{
             putBuffer(dest, source.getBuffer());
         }
     }
 
     public static void putBuffer(ByteBuffer dest, ByteBuffer source) {
         if(source == null) {
             dest.putInt(0);
         }else{
             dest.putInt(source.remaining());
 
             int sourcepos = source.position();
             for (int i = 0; i < source.remaining(); i++) {
                 dest.put(source.get(sourcepos + i));
             }
         }
     }
 
     public static void putBuffer(ByteBuffer dest, IntBuffer source) {
         if(source == null) {
             dest.putInt(0);
         }else{
             dest.putInt(source.remaining() * SIZEOF_INT);
 
             int sourcepos = source.position();
             for (int i = 0; i < source.remaining(); i++) {
                 dest.putInt(source.get(sourcepos + i));
             }
         }
     }
 
     public static void putBuffer(ByteBuffer dest, LongBuffer source) {
         if(source == null) {
             dest.putInt(0);
         }else{
             dest.putInt(source.remaining() * SIZEOF_LONG);
 
             int sourcepos = source.position();
             for (int i = 0; i < source.remaining(); i++) {
                 dest.putLong(source.get(sourcepos + i));
             }
         }
     }
 
     public static void putBuffer(ByteBuffer dest, FloatBuffer source) {
         if(source == null) {
             dest.putInt(0);
         }else{
             dest.putInt(source.remaining() * SIZEOF_FLOAT);
 
             int sourcepos = source.position();
             for (int i = 0; i < source.remaining(); i++) {
                 dest.putFloat(source.get(sourcepos + i));
             }
         }
     }
 
     public static void putBuffer(ByteBuffer dest, DoubleBuffer source) {
         if(source == null) {
             dest.putInt(0);
         }else{
             dest.putInt(source.remaining() * SIZEOF_DOUBLE);
 
             int sourcepos = source.position();
             for (int i = 0; i < source.remaining(); i++) {
                 dest.putDouble(source.get(sourcepos + i));
             }
         }
     }
 
     public static void putString(ByteBuffer dest, String string) {
         if(string == null) {
             dest.putInt(-1);
         }else if(string.length() == 0) {
             dest.putInt(0);
         }else{
             byte[] bytes = string.getBytes();
             dest.putInt(bytes.length);
             dest.put(bytes);
         }
     }
 
 }
