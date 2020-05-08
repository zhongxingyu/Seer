 package org.sonar.duplications.index;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.block.ByteArray;
 import org.sonar.duplications.detector.original.FastStringComparator;
 
 import com.google.common.collect.Lists;
 
 /**
  * Each object in Java has an overhead - see
  * <a href="http://devblog.streamy.com/2009/07/24/determine-size-of-java-object-class/">"HOWTO: Determine the size of a Java Object or Class"</a>.
  * So to optimize memory consumption, we use flat arrays, however this increases time of query.
  */
 public class PackedMemoryCloneIndex extends AbstractCloneIndex {
 
  private static int INITIAL_SIZE = 1024;
 
  private static int BLOCK_INTS = 3;
 
   private final int hashInts;
 
   private final int blockInts;
 
   private boolean sorted;
   private int size;
 
   private String[] resourceIds;
   private int[] blockData;
 
   private int[] resourceIdsIndex;
 
   public PackedMemoryCloneIndex() {
     this(8, INITIAL_SIZE);
   }
 
   /**
    * @param hashBytes size of hash in bytes
    * @param initialCapacity the initial capacity
    */
   public PackedMemoryCloneIndex(int hashBytes, int initialCapacity) {
     this.sorted = false;
     this.hashInts = hashBytes / 4;
     this.blockInts = hashInts + BLOCK_INTS;
     this.size = 0;
     this.resourceIds = new String[initialCapacity];
     this.blockData = new int[initialCapacity * blockInts];
     this.resourceIdsIndex = new int[initialCapacity];
   }
 
   public Collection<Block> getByResourceId(String resourceId) {
     ensureSorted();
 
     // TODO can be interned: resourceId = resourceId.intern();
 
     List<Block> result = Lists.newArrayList();
 
     resourceIds[size] = resourceId;
     resourceIdsIndex[size] = size;
 
     int index = DataUtils.binarySearch(byResourceId, size);
     // TODO can be used if strings interned: while (index < size && resourceIds[byResourceIndices[index]] == resourceId) {
     while (index < size && FastStringComparator.INSTANCE.compare(resourceIds[resourceIdsIndex[index]], resourceId) == 0) {
       result.add(extractBlock(resourceIdsIndex[index]));
       index++;
     }
     return result;
   }
 
   public Collection<Block> getBySequenceHash(ByteArray sequenceHash) {
     ensureSorted();
 
     int[] hash = DataUtils.byteToIntArray(sequenceHash.array());
     if (hash.length != hashInts) {
       throw new IllegalStateException("Expected " + hashInts + " ints in hash, but got " + hash.length);
     }
     int offset = size * blockInts;
     for (int i = 0; i < hashInts; i++) {
       blockData[offset++] = hash[i];
     }
 
     List<Block> result = Lists.newArrayList();
     int index = DataUtils.binarySearch(byBlockHash, size);
     while (index < size && !isLessByHash(size, index)) {
       result.add(extractBlock(index));
       index++;
     }
     return result;
   }
 
   public void insert(Block block) {
     sorted = false;
     ensureCapacity();
 
     // TODO can be interned: resourceIds[size] = block.getResourceId().intern();
     resourceIds[size] = block.getResourceId();
 
     int[] hash = DataUtils.byteToIntArray(block.getBlockHash().array());
     if (hash.length != hashInts) {
       throw new IllegalStateException("Expected " + hashInts + " ints in hash, but got " + hash.length);
     }
     int offset = size * blockInts;
     for (int i = 0; i < hashInts; i++) {
       blockData[offset++] = hash[i];
     }
     blockData[offset++] = block.getIndexInFile();
     blockData[offset++] = block.getFirstLineNumber();
     blockData[offset] = block.getLastLineNumber();
 
     size++;
   }
 
   /**
    * Increases the capacity, if necessary.
    */
   private void ensureCapacity() {
     if (size < resourceIds.length) {
       return;
     }
     int newCapacity = (resourceIds.length * 3) / 2 + 1;
     // Double size of resourceIds
     String[] oldResourceIds = resourceIds;
     resourceIds = new String[newCapacity];
     System.arraycopy(oldResourceIds, 0, resourceIds, 0, oldResourceIds.length);
     // Double size of blockData
     int[] oldBlockData = blockData;
     blockData = new int[newCapacity * blockInts];
     System.arraycopy(oldBlockData, 0, blockData, 0, oldBlockData.length);
     // Double size of byResourceIndices (no need to copy old, because would be restored in method ensureSorted)
     resourceIdsIndex = new int[newCapacity];
     sorted = false;
   }
 
   /**
    * Performs sorting, if necessary.
    */
   private void ensureSorted() {
     if (sorted) {
       return;
     }
 
     ensureCapacity();
 
     DataUtils.sort(byBlockHash);
     for (int i = 0; i < size; i++) {
       resourceIdsIndex[i] = i;
     }
     DataUtils.sort(byResourceId);
 
     sorted = true;
   }
 
   private boolean isLessByHash(int i, int j) {
     i *= blockInts;
     j *= blockInts;
     for (int k = 0; k < hashInts; k++, i++, j++) {
       if (blockData[i] < blockData[j]) {
         return true;
       }
       if (blockData[i] > blockData[j]) {
         return false;
       }
     }
     return false;
   }
 
   private Block extractBlock(int i) {
     String resourceId = resourceIds[i];
 
     int offset = i * blockInts;
     int[] hash = new int[hashInts];
     for (int j = 0; j < hashInts; j++) {
       hash[j] = blockData[offset++];
     }
 
     int indexInFile = blockData[offset++];
     int firstLineNumber = blockData[offset++];
     int lastLineNumber = blockData[offset];
 
     return new Block(resourceId, new ByteArray(DataUtils.intToByteArray(hash)), indexInFile, firstLineNumber, lastLineNumber);
   }
 
   private final DataUtils.Sortable byBlockHash = new DataUtils.Sortable() {
     public void swap(int i, int j) {
       String tmp = resourceIds[i];
       resourceIds[i] = resourceIds[j];
       resourceIds[j] = tmp;
 
       i *= blockInts;
       j *= blockInts;
       for (int k = 0; k < blockInts; k++, i++, j++) {
         int x = blockData[i];
         blockData[i] = blockData[j];
         blockData[j] = x;
       }
     }
 
     public boolean isLess(int i, int j) {
       return isLessByHash(i, j);
     }
 
     public int size() {
       return size;
     }
   };
 
   private final DataUtils.Sortable byResourceId = new DataUtils.Sortable() {
     public void swap(int i, int j) {
       int tmp = resourceIdsIndex[i];
       resourceIdsIndex[i] = resourceIdsIndex[j];
       resourceIdsIndex[j] = tmp;
     }
 
     public boolean isLess(int i, int j) {
       String s1 = resourceIds[resourceIdsIndex[i]];
       String s2 = resourceIds[resourceIdsIndex[j]];
       return FastStringComparator.INSTANCE.compare(s1, s2) < 0;
       // TODO can be used if strings interned: return System.identityHashCode(s1) < System.identityHashCode(s2);
     }
 
     public int size() {
       return size;
     }
   };
 
 }
