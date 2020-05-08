 package nachos.filesys;
 
 import nachos.machine.Lib;
 
 public class Directory extends INode {
     private boolean primary;
     private int validCount;
     private int parent;
     private int next;
 
     private String[] subDirs;
     private int[] types;
     private int[] links;
 
     private static final int maxLinkCount = 15;
 
     private Directory() {
         super(1);
         subDirs = new String[maxLinkCount];
         types = new int[maxLinkCount];
         links = new int[maxLinkCount];
     }
 
     public static Directory load(int block) {
         Directory dir = new Directory();
         dir.block = block;
 
         byte[] buffer = new byte[DiskHelper.getBlockSize()];
         DiskHelper.getInstance().readBlock(block, 1, buffer);
 
         Lib.assertTrue(dir.getType() == Lib.bytesToInt(buffer, 0), "Directory inode type doesn't match");
 
         if (Lib.bytesToInt(buffer, 4) == 0)
             dir.primary = false;
         else
             dir.primary = true;
 
         dir.validCount = Lib.bytesToInt(buffer, 8);
         Lib.assertTrue(dir.validCount >= 0 && dir.validCount <= maxLinkCount, "Directory valid count is out of range");
 
         dir.parent = Lib.bytesToInt(buffer, 12);
 
         dir.next = Lib.bytesToInt(buffer, 16);
 
         for (int i = 0; i < dir.validCount; ++i) {
             int len = Lib.bytesToInt(buffer, 20 + i * 268);
             Lib.assertTrue(len <= 255, "Sub-directory name too long");
             dir.subDirs[i] = new String(buffer, 24 + i * 268, len);
             dir.types[i] = Lib.bytesToInt(buffer, 280 + i * 268);
             dir.links[i] = Lib.bytesToInt(buffer, 284 + i * 268);
         }
 
         return dir;
     }
 
     public Entry find(String fileName) {
         if (fileName.equals("."))
             return new Entry(Entry.DIRECTORY, block, ".");
         if (fileName.equals("..")) {
             if (parent == -1)
                 return new Entry(Entry.DIRECTORY, block, "..");
             else
                 return new Entry(Entry.DIRECTORY, parent, "..");
         }
 
         int pos = findPos(fileName);
         if (pos == -1)
             return null;
         else
             return getEntry(pos);
     }
 
     public int findPos(String name) {
        for (int i = 0; i < validCount; ++i)
             if (subDirs[i].equals(name))
                 return i;
         return -1;
     }
 
     public Entry getEntry(int i) {
         Lib.assertTrue(i < validCount);
         return new Entry(types[i], links[i], subDirs[i]);
     }
 
     public int getValidCount() {
         return validCount;
     }
 
     public void save() {
         byte[] data = new byte[DiskHelper.getBlockSize()];
         Lib.bytesFromInt(data, 0, getType());
         Lib.bytesFromInt(data, 4, primary ? 1 : 0);
         Lib.bytesFromInt(data, 8, validCount);
         Lib.bytesFromInt(data, 12, parent);
         Lib.bytesFromInt(data, 16, next);
         for (int i = 0; i < validCount; ++i) {
             Lib.assertTrue(subDirs[i] != null);
 
             byte[] string = subDirs[i].getBytes();
             Lib.bytesFromInt(data, 20 + i * 268, string.length);
             System.arraycopy(string, 0, data, 24 + i * 268, string.length);
             Lib.bytesFromInt(data, 280 + i * 268, types[i]);
             Lib.bytesFromInt(data, 284 + i * 268, links[i]);
         }
 
         DiskHelper.getInstance().writeBlock(block, 1, data);
     }
 
     public static Directory create(int block, int parent) {
         Directory dir = new Directory();
         dir.block = block;
 
         dir.primary = true;
         dir.validCount = 0;
         dir.parent = parent;
         dir.next = -1;
 
         dir.save();
 
         return dir;
     }
 
     public boolean addEntry(String name, int type, int block) {
         if (validCount == maxLinkCount)
             return false;
 
         Lib.assertTrue(name.indexOf("/") == -1);
         subDirs[validCount] = name;
         types[validCount] = type;
         links[validCount] = block;
 
         ++validCount;
 
         save();
 
         return true;
     }
 
     public boolean removeLastEntry() {
         if (next != -1)
             return false;
 
         --validCount;
         return true;
     }
 
     public boolean replaceEntry(int index, String name, int type, int block) {
         if (index >= validCount)
             return false;
 
         subDirs[index] = name;
         types[index] = type;
         links[index] = block;
         return true;
     }
 
     public int getParent() {
         return parent;
     }
 
     public void setNext(int block) {
         next = block;
 
         save();
     }
 
     public boolean hasNext() {
         return next != -1;
     }
 
     public Directory loadNext() {
         Lib.assertTrue(hasNext());
 
         return Directory.load(next);
     }
 }
