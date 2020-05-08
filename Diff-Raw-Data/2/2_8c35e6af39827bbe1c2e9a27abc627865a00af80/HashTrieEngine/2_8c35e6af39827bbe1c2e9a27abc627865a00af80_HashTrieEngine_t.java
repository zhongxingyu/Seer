 package net.yeputons.cscenter.dbfall2013.engines;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.InvalidParameterException;
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
 public class HashTrieEngine extends SimpleEngine implements FileStorableDbEngine {
     protected File storage;
     protected RandomAccessFile data;
     protected int dataUsedLength;
 
     protected final static MessageDigest md;
     protected final int MD_LEN = 160 / 8;
     protected int currentSize;
 
     protected final int SIGNATURE_OFFSET = 0;
     protected final int USED_LENGTH_OFFSET = 4;
     protected final int ROOT_NODE_OFFSET = 8;
 
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
             data.writeInt(1);
             data.writeInt(0);
             InnerNode.writeToFile(data, (int)data.length());
 
             dataUsedLength = (int)data.length();
             data.seek(USED_LENGTH_OFFSET);
             data.writeInt(dataUsedLength);
         } else {
             int ver = data.readInt();
             if (ver != 1)
                 throw new IOException("Invalid DB version: expected 1, found " + ver);
             dataUsedLength = data.readInt();
         }
         if (dataUsedLength > data.length())
             throw new IOException("Invalid DB: used length is greater than file length");
         currentSize = keySet().size();
     }
 
     public HashTrieEngine(File storage) throws IOException {
         this.storage = storage;
         openStorage();
     }
     @Override
     public void flush() throws IOException {
         data.getFD().sync();
     }
     @Override
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
 
     private void keySet(int ptr, Set<ByteBuffer> keySet) throws IOException {
         TrieNode node = TrieNode.createFromFile(data, ptr);
         if (node instanceof LeafNode) {
             LeafNode leaf = (LeafNode)node;
             if (leaf.value == null) return;
             keySet.add(leaf.key);
             return;
         }
 
         InnerNode inner = (InnerNode)node;
         for (int i = 0; i < inner.next.length; i++)
             if (inner.next[i] != 0)
                 keySet(inner.next[i], keySet);
     }
 
     @Override
     public Set<ByteBuffer> keySet() {
         Set<ByteBuffer> keySet = new HashSet<ByteBuffer>();
         try {
             keySet(ROOT_NODE_OFFSET, keySet);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return keySet;
     }
 
     @Override
     public int size() {
         return currentSize;
     }
 
     protected LeafNode getNode(ByteBuffer key) {
         byte[] hash = md.digest(key.array());
         assert hash.length == MD_LEN;
 
         try {
             TrieNode node = TrieNode.createFromFile(data, ROOT_NODE_OFFSET);
 
             int hashPtr = 0;
             while (!(node instanceof LeafNode)) {
                 int ptr = ((InnerNode)node).next[hash[hashPtr] & 0xFF];
                 if (ptr == 0) throw new NoSuchElementException();
                 node = TrieNode.createFromFile(data, ptr);
                 hashPtr++;
             }
 
             LeafNode leaf = (LeafNode)node;
             if (leaf.value == null)
                 throw new NoSuchElementException();
 
             if (!leaf.key.equals(key)) {
                 byte[] hash2 = md.digest(leaf.key.array());
                 for (int i = 0; i < hashPtr; i++)
                     assert(hash[i] == hash2[i]);
                 if (hashPtr < hash.length) throw new NoSuchElementException();
                 assert hash.equals(hash2);
                 throw new RuntimeException("SHA-1 collision!");
             }
             return leaf;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ByteBuffer get(Object _key) {
         if (size() == 0) return null;
         ByteBuffer key = (ByteBuffer)_key;
 
         try {
             LeafNode leaf = getNode(key);
             return leaf.value;
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
 
         try {
             TrieNode node = TrieNode.createFromFile(data, ROOT_NODE_OFFSET);
             InnerNode parent = null;
             int parentC = -1;
             int hashPtr = 0;
             while (!(node instanceof LeafNode)) {
                 InnerNode inner = (InnerNode)node;
                 int c = hash[hashPtr] & 0xFF;
 
                 parent = inner;
                 parentC = c;
                 if (inner.next[c] != 0) {
                     node = TrieNode.createFromFile(data, inner.next[c]);
                 } else {
                     int offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode leaf = LeafNode.writeToFile(data, offset, key, value);
                     inner.setNext(c, leaf.offset);
                     node = leaf;
                     currentSize += 1;
                     return null;
                 }
                 hashPtr++;
             }
 
             LeafNode leaf = (LeafNode)node;
             if (leaf.key.equals(key)) {
                 ByteBuffer oldValue = leaf.value;
                 try {
                     leaf.setValue(value);
                 } catch (ValueIsBiggerThanOldException e) {
                     int offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode newLeaf = LeafNode.writeToFile(data, offset, key, value);
                     parent.setNext(parentC, newLeaf.offset);
                 }
                 if (oldValue == null) {
                     currentSize += 1;
                 }
                 return oldValue;
             } else {
                 if (hashPtr >= hash.length)
                     throw new RuntimeException("SHA-1 collision!");
 
                 currentSize += 1;
                 if (leaf.value == null) {
                     int offset = appendToStorage(LeafNode.estimateSize(key, value));
                     LeafNode newLeaf = LeafNode.writeToFile(data, offset, key, value);
                     parent.setNext(parentC, newLeaf.offset);
                     return null;
                 }
                 {
                     LeafNode tmp = getNode(leaf.key);
                     assert(tmp.value.equals(leaf.value));
                 }
 
                 byte[] hash2 = md.digest(leaf.key.array());
                 for (int i = 0; i < hashPtr; i++)
                     assert(hash[i] == hash2[i]);
                 while (true) {
                     int offset = appendToStorage(InnerNode.estimateSize());
                     InnerNode newInner = InnerNode.writeToFile(data, offset);
                     parent.setNext(parentC, newInner.offset);
 
                     parent = newInner;
                     int c1 = hash[hashPtr] & 0xFF, c2 = hash2[hashPtr] & 0xFF;
                     if (c1 != c2) break;
 
                     parentC = c1;
                     hashPtr++;
                 }
                 parent.setNext(hash2[hashPtr] & 0xFF, leaf.offset);
 
                 int offset = appendToStorage(LeafNode.estimateSize(key, value));
                 LeafNode newLeaf = LeafNode.writeToFile(data, offset, key, value);
                 parent.setNext(hash[hashPtr] & 0xFF, newLeaf.offset);
 
                 LeafNode tmp = getNode(key);
                 assert(tmp.value.equals(value));
 
                 tmp = getNode(leaf.key);
                 assert(tmp.value.equals(leaf.value));
                 return null;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private int appendToStorage(int size) throws IOException {
         int res = dataUsedLength;
        while (dataUsedLength + size > data.length()) {
             data.setLength(2 * data.length());
         }
         dataUsedLength += size;
         data.seek(USED_LENGTH_OFFSET);
         data.writeInt(dataUsedLength);
         return res;
     }
 
     @Override
     public ByteBuffer remove(Object _key) {
         ByteBuffer key = (ByteBuffer)_key;
         try {
             LeafNode leaf = getNode(key);
             ByteBuffer oldValue = leaf.value;
             leaf.setValue(null);
             currentSize -= 1;
             return oldValue;
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (NoSuchElementException e) {
             return null;
         }
     }
 }
