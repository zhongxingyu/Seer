 package net.yeputons.cscenter.dbfall2013.engines;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: e.suvorov
  * Date: 05.10.13
  * Time: 20:59
  * To change this template use File | Settings | File Templates.
  */
 public class HashTrieEngine extends SimpleEngine {
     protected File storage;
     protected RandomAccessFile data;
     protected final static MessageDigest md;
     protected final int MD_LEN = 160 / 8;
     protected int currentSize;
 
     static {
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void openStorage() throws IOException {
         data = new RandomAccessFile(storage, "rw");
         if (data.length() == 0) {
             data.writeInt(0);
             for (int i = 0; i < 256; i++)
                 data.writeInt(0);
         }
         currentSize = keySet().size();
     }
 
     public HashTrieEngine(File storage) throws IOException {
         this.storage = storage;
         openStorage();
     }
     public void flush() throws IOException {
         data.getFD().sync();
     }
     public void close() throws IOException {
         data.close();
     }
 
     @Override
     public void clear() {
         try {
             close();
             storage.delete();
             openStorage();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void keySet(int ptr, int depth, Set<ByteBuffer> keySet) throws IOException {
         data.seek(ptr);
         if (depth == MD_LEN) {
             int valLen = data.readInt();
             if (valLen == -1) return;
             data.skipBytes(valLen);
 
             int keyLen = data.readInt();
             byte[] key = new byte[keyLen];
             data.read(key);
             keySet.add(ByteBuffer.wrap(key));
             return;
         }
 
         int[] ptrs = new int[256];
         for (int i = 0; i < ptrs.length; i++) {
             ptrs[i] = data.readInt();
         }
         for (int i = 0; i < ptrs.length; i++)
             if (ptrs[i] != 0)
                 keySet(ptrs[i], depth + 1, keySet);
     }
 
     @Override
     public Set<ByteBuffer> keySet() {
         Set<ByteBuffer> keySet = new HashSet<ByteBuffer>();
         try {
             keySet(4, 0, keySet);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return keySet;
     }
 
     @Override
     public int size() {
         return currentSize;
     }
 
     protected int getNode(ByteBuffer key) {
         int ptr = 4;
         byte[] hash = md.digest(key.array());
         assert hash.length == MD_LEN;
 
         try {
             for (int i = 0; i < hash.length; i++) {
                 int cur = hash[i] & 0xFF;
                 data.seek(ptr + 4 * cur);
                 int nptr = data.readInt();
                 if (nptr == 0) throw new NoSuchElementException();
                 ptr = nptr;
             }
 
             data.seek(ptr);
             if (data.readInt() == -1)
                 throw new NoSuchElementException();
             return ptr;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ByteBuffer get(Object _key) {
         if (size() == 0) return null;
         ByteBuffer key = (ByteBuffer)_key;
 
         try {
             int ptr = getNode(key);
             data.seek(ptr);
             byte[] res = new byte[data.readInt()];
             data.read(res);
             return ByteBuffer.wrap(res);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (NoSuchElementException e) {
             return null;
         }
     }
 
     @Override
     public ByteBuffer put(ByteBuffer key, ByteBuffer value) {
         if (key == null || value == null)
             throw new NullPointerException("key and value should not be nulls");
 
         byte[] hash = md.digest(key.array());
         assert hash.length == MD_LEN;
         int ptr = 4;
 
         try {
             for (int i = 0; i < hash.length; i++) {
                 int cur = hash[i] & 0xFF;
                 if (ptr == data.length()) {
                     data.seek(ptr);
                     for (int i2 = 0; i2 < 256; i2++)
                         data.writeInt(0);
                 }
                 data.seek(ptr + 4 * cur);
                 int nptr = data.readInt();
                 if (i + 1 < hash.length) {
                     if (nptr == 0) {
                         nptr = (int)data.length();
                         data.seek(ptr + 4 * cur);
                         data.writeInt(nptr);
                     }
                     ptr = nptr;
                 } else {
                     ByteBuffer oldVal = null;
                    if (nptr > 0 && nptr < data.length()) {
                        data.seek(nptr);
                        int valueLen = data.readInt();
                        if (valueLen != -1) {
                            byte[] buf = new byte[valueLen];
                            data.read(buf);
                            oldVal = ByteBuffer.wrap(buf);
                        }
                     }
 
                     nptr = (int)data.length();
                     data.seek(ptr + 4 * cur);
                     data.writeInt(nptr);
                     data.seek(nptr);
                     data.writeInt(value.array().length);
                     data.write(value.array());
                     data.writeInt(key.array().length);
                     data.write(key.array());
 
                    if (oldVal == null) {
                        currentSize += 1;
                        data.seek(0);
                        data.writeInt(currentSize);
                    }
                     return oldVal;
                 }
             }
             throw new AssertionError("Reached end of function");
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ByteBuffer remove(Object _key) {
         ByteBuffer key = (ByteBuffer)_key;
         try {
             int ptr = getNode(key);
             data.seek(ptr);
             byte[] old = new byte[data.readInt()];
             data.read(old);
 
             data.seek(ptr);
             data.writeInt(-1);
 
             currentSize -= 1;
             data.seek(0);
             data.writeInt(currentSize);
             return ByteBuffer.wrap(old);
         } catch (IOException e){
             throw new RuntimeException(e);
         } catch (NoSuchElementException e) {
             return null;
         }
     }
 }
