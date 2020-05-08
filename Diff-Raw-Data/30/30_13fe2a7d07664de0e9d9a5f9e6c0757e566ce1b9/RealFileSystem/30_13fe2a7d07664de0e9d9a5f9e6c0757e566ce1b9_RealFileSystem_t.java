 package nachos.filesys;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import nachos.machine.FileSystem;
 import nachos.machine.Lib;
 import nachos.machine.Machine;
 import nachos.machine.OpenFile;
 
 /**
  * RealFileSystem provide necessary methods for filesystem syscall. The
  * FileSystem interface already define two basic methods, you should implement
  * your own to adapt to your task.
  * 
  * @author starforever
  */
 public class RealFileSystem implements FileSystem {
     private static class CachedNormalFile {
         private boolean dirty;
         private int useCount;
         private NormalFile normalFile;
 
         public CachedNormalFile(NormalFile nf) {
             normalFile = nf;
             dirty = false;
             useCount = 0;
         }
 
         public void increaseUseCount() {
             ++useCount;
         }
 
         public void decreaseUseCount() {
             Lib.assertTrue(useCount >= 1);
             --useCount;
         }
 
         public boolean isUsing() {
             return useCount > 0;
         }
 
         public void setDirty() {
             dirty = true;
         }
 
         public NormalFile normalFile() {
             return normalFile;
         }
 
         public void save() {
             if (dirty) {
                 normalFile.save();
                 dirty = false;
             }
         }
     }
 
     public class File extends OpenFile {
         CachedNormalFile cnf;
         boolean open;
         int pointer;
 
         private File(CachedNormalFile cnf, String fileName) {
             super(RealFileSystem.this, fileName);
             this.cnf = cnf;
             open = true;
             pointer = 0;
         }
 
         private int getBlockCount() {
             if (length() == 0)
                 return 0;
             else
                 return ((length() - 1) / DiskHelper.getBlockSize()) + 1;
         }
 
         private int readBlock(int block, byte[] buffer, int offset) {
             if (block >= getBlockCount())
                 return 0;
 
             NormalFile current = cnf.normalFile();
             while (block >= current.getValidCount()) {
                 block -= current.getValidCount();
                 current = current.loadNext();
             }
 
             current.read(block, buffer, offset);
 
             int r = 0;
             if (block == getBlockCount() - 1)
                 r = length() % DiskHelper.getBlockSize();
             if (r == 0)
                 r = DiskHelper.getBlockSize();
 
             return r;
         }
 
         private int writeBlock(int block, byte[] buffer, int offset) {
             if (block >= getBlockCount())
                 return 0;
 
             NormalFile current = cnf.normalFile();
             while (block >= current.getValidCount()) {
                 block -= current.getValidCount();
                 current = current.loadNext();
             }
 
             current.write(block, buffer, offset);
 
             int r = 0;
             if (block == getBlockCount() - 1)
                 r = length() % DiskHelper.getBlockSize();
             if (r == 0)
                 r = DiskHelper.getBlockSize();
 
             return r;
         }
 
         public int read(int pos, byte[] buf, int offset, int length) {
             Lib.debug(dbgFilesys, "Reading " + getName());
 
             if (!open)
                 return -1;
             if (pos < 0 || length < 0)
                 return -1;
             if (length == 0 || length() == 0)
                 return 0;
 
             int firstBlock = pos / DiskHelper.getBlockSize();
             byte[] tmp = new byte[DiskHelper.getBlockSize()];
 
             int r = 0, i = firstBlock;
             while (r < length) {
                 int begin = (i == firstBlock) ? (pos % DiskHelper
                         .getBlockSize()) : 0;
                 int read = readBlock(i, tmp, 0);
                 int c = Math.min(read - begin, length - r);
                 if (c <= 0)
                     break;
                 System.arraycopy(tmp, begin, buf, offset + r, c);
                 r += c;
 
                 if (read < DiskHelper.getBlockSize())
                     break;
                 else
                     ++i;
             }
 
             Lib.assertTrue(r <= length);
 
             return r;
         }
 
         private boolean expand(int newSize) {
             Lib.debug(dbgFilesys, "Expanding " + getName() + " to " + newSize);
 
             cnf.setDirty();
 
             NormalFile inode = cnf.normalFile();
             int capacity = getBlockCount() * DiskHelper.getBlockSize();
             if (capacity < newSize) {
                 int d = (newSize - capacity - 1) / DiskHelper.getBlockSize()
                         + 1;
                 Lib.assertTrue(d > 0);
 
                 LinkedList<Integer> places = new LinkedList<Integer>();
                 for (int i = 0; i < d; ++i) {
                     int t = freeList.occupy();
                     if (t == -1) {
                         for (Integer p : places)
                             freeList.free(p.intValue());
                         return false;
                     } else
                         places.add(new Integer(t));
                 }
 
                 NormalFile current = inode;
                 while (!places.isEmpty()) {
                     int block = places.remove().intValue();
 
                     if (current.addLink(block) == false) {
                         int t = freeList.occupy();
                         if (t == -1) {
                             Lib.debug(
                                     dbgFilesys,
                                     "Fatal error: not enough disk space to store inode; may cause filesys inconsistent");
                             return false;
                         }
                         NormalFile next = NormalFile.create(t, false, 0);
                         current.setNext(t);
                         if (current != inode)
                             current.save();
                         current = next;
 
                         Lib.assertTrue(current.addLink(block));
                     }
                 }
             }
 
             inode.setSize(newSize);
 
             return true;
         }
 
         public int write(int pos, byte[] buf, int offset, int length) {
             Lib.debug(dbgFilesys, "Writing " + getName() + ", pos=" + pos
                     + ", len=" + length);
             if (!open)
                 return -1;
             if (pos < 0 || length < 0)
                 return -1;
             if (length == 0)
                 return 0;
 
             if (length() < pos + length && !expand(pos + length))
                 return -1;
 
             int firstBlock = pos / DiskHelper.getBlockSize();
             byte[] tmp = new byte[DiskHelper.getBlockSize()];
 
             int r = 0, i = firstBlock;
             while (r < length) {
                 int begin = (i == firstBlock) ? (pos % DiskHelper
                         .getBlockSize()) : 0;
                 int c = Math.min(DiskHelper.getBlockSize() - begin, length - r);
                 if (c < DiskHelper.getBlockSize())
                     readBlock(i, tmp, 0);
                 System.arraycopy(buf, offset + r, tmp, begin, c);
                 writeBlock(i, tmp, 0);
 
                 r += c;
                 ++i;
             }
 
             Lib.assertTrue(r <= length);
 
             return r;
         }
 
         public int length() {
             return cnf.normalFile().getSize();
         }
 
         public void close() {
             Lib.debug(dbgFilesys, "Closing " + getName());
             open = false;
             cnf.decreaseUseCount();
 
             if (!cnf.isUsing()) {
                 int block = cnf.normalFile().getBlock();
                 if (removingFiles.contains(new Integer(block)))
                     doRemoveFile(block);
             }
         }
 
         public void seek(int pos) {
             pointer = pos;
         }
 
         public int tell() {
             return pointer;
         }
 
         public int read(byte[] buf, int offset, int length) {
             int r = read(pointer, buf, offset, length);
             if (r != -1)
                 pointer += r;
             return r;
         }
 
         public int write(byte[] buf, int offset, int length) {
             int r = write(pointer, buf, offset, length);
             if (r != -1)
                 pointer += r;
             return r;
         }
     }
 
     private FreeList freeList;
 
     private Map<Integer, CachedNormalFile> cachedFiles;
     private Set<Integer> removingFiles;
 
     private static final int rootBlock = 1, freeListBlock = 2, filesysHead = 2;
 
     /**
      * initialize the file system
      * 
      * @param format
      *            whether to format the file system
      */
     public void init(boolean format) {
         cachedFiles = new HashMap<Integer, CachedNormalFile>();
         removingFiles = new HashSet<Integer>();
 
         if (format) {
             createFilesys();
             importStub();
         } else {
             SuperBlock sb = SuperBlock.load();
             if (sb == null) {
                 Lib.debug(dbgFilesys, "Broken filesys detected; formatting");
                 createFilesys();
             } else {
                 freeList = freeList.load(sb.freeList);
             }
         }
     }
 
     public void createFilesys() {
         SuperBlock.create(rootBlock, freeListBlock);
 
         Directory.create(rootBlock, -1);
         freeList = FreeList.create(freeListBlock, filesysHead);
     }
 
     private String containingPath(String fileName) {
         if (!fileName.startsWith("/"))
             return null;
         if (fileName.endsWith("/"))
             fileName = fileName.substring(0, fileName.length() - 1);
         int pos = fileName.lastIndexOf("/");
         return fileName.substring(0, pos + 1);
     }
 
     private String fileName(String fileName) {
         if (!fileName.startsWith("/"))
             return null;
         if (fileName.endsWith("/"))
             fileName = fileName.substring(0, fileName.length() - 1);
         int pos = fileName.lastIndexOf("/");
         return fileName.substring(pos + 1);
     }
 
     private Entry findSingleEntry(Directory parent, String fileName) {
         Lib.assertTrue(parent != null);
 
         Entry entry = parent.find(fileName);
        Lib.debug(dbgFilesys, "reached2" + fileName);

         if (entry != null)
             return entry;
         else if (parent.hasNext())
             return findSingleEntry(parent.loadNext(), fileName);
         else
             return null;
     }
 
     private String findEntryName(Directory parent, int block) {
         for (int i = 0; i < parent.getValidCount(); ++i) {
             Entry e = parent.getEntry(i);
             if (e.block == block)
                 return e.name;
         }
 
         if (parent.hasNext())
             return findEntryName(parent.loadNext(), block);
         else
             return null;
     }
 
     private Entry findEntryRaw(String absoluteFileName, boolean symbolic,
             int depth) {
         if (depth < 0)
             return null;
 
         if (!absoluteFileName.startsWith("/"))
             return null;
 
         if (absoluteFileName.endsWith("/"))
             absoluteFileName = absoluteFileName.substring(0,
                     absoluteFileName.length() - 1);
 
         int token = absoluteFileName.indexOf("/");
         Directory current = Directory.load(rootBlock);
         if (token == -1)
             return new Entry(Entry.DIRECTORY, rootBlock, "/");
 
         while (true) {
             int nextToken = absoluteFileName.indexOf("/", token + 1);
 
             String e;
             if (nextToken != -1)
                 e = absoluteFileName.substring(token + 1, nextToken);
             else
                 e = absoluteFileName.substring(token + 1);
 
             Entry entry = findSingleEntry(current, e);
            Lib.debug(dbgFilesys, "reached1");
             if (entry == null)
                 return null;
             else {
                 if (entry.type == Entry.SYMBOLIC_LINK) {
                     SymbolicLink sl = (SymbolicLink) entry.load();
 
                     String s;
                     if (nextToken != -1) {
                         if (sl.getTarget().endsWith("/"))
                             s = sl.getTarget()
                                     + absoluteFileName.substring(nextToken + 1);
                         else
                             s = sl.getTarget() + "/"
                                     + absoluteFileName.substring(nextToken + 1);
                     } else {
                         if (symbolic)
                             s = sl.getTarget();
                         else
                             return entry;
                     }
 
                     return findEntryRaw(s, symbolic, depth - 1);
                 } else {
                     if (nextToken == -1)
                         return entry;
                     else {
                         if (entry.type != Entry.DIRECTORY)
                             return null;
                         else
                             current = (Directory) entry.load();
                     }
                 }
             }
 
             token = nextToken;
         }
     }
 
     private Entry findEntry(String absoluteFileName, boolean symbolic) {
         return findEntryRaw(absoluteFileName, symbolic, 128);
     }
 
     private Entry findFileEntryRaw(String absoluteFileName, boolean parent,
             int depth) {
         if (depth < 0)
             return null;
         if (absoluteFileName.endsWith("/"))
             return null;
 
         String path = containingPath(absoluteFileName), file = fileName(absoluteFileName);
         if (path == null || file == null)
             return null;
        // Lib.debug(dbgFilesys, path);
         Entry eFolder = findEntry(path, true);
 
         if (eFolder == null || eFolder.type != Entry.DIRECTORY)
             return null;
 
         Directory folder = (Directory) eFolder.load();
         Entry f = findSingleEntry(folder, file);
 
         if (f != null && f.type == Entry.NORMAL_FILE)
             return f;
         else if (f != null && f.type == Entry.SYMBOLIC_LINK) {
             SymbolicLink sl = (SymbolicLink) f.load();
             return findFileEntryRaw(sl.getTarget(), false, depth - 1);
         } else if (f != null) {
             return null;
         } else if (parent) {
             return eFolder;
         } else
             return null;
     }
 
     private Entry findFileEntry(String absoluteFileName, boolean parent) {
         return findFileEntryRaw(absoluteFileName, parent, 128);
     }
 
     public void finish() {
         Lib.debug(dbgFilesys, "Saving file system");
         for (CachedNormalFile cnf : cachedFiles.values())
             cnf.save();
         for (int file : removingFiles)
             doRemoveFile(file);
         freeList.save();
     }
 
     /** import from stub filesystem */
     private void importStub() {
         FileSystem stubFS = Machine.stubFileSystem();
         FileSystem realFS = this;
         String[] file_list = Machine.stubFileList();
         for (int i = 0; i < file_list.length; ++i) {
             if (!file_list[i].endsWith(".coff"))
                 continue;
             OpenFile src = stubFS.open(file_list[i], false);
             if (src == null) {
                 continue;
             }
             OpenFile dst = realFS.open("/" + file_list[i], true);
             int size = src.length();
             byte[] buffer = new byte[size];
             src.read(0, buffer, 0, size);
             dst.write(0, buffer, 0, size);
             src.close();
             dst.close();
         }
     }
 
     private CachedNormalFile getFile(int block) {
         if (!cachedFiles.containsKey(new Integer(block))) {
             NormalFile nf = NormalFile.load(block);
             Lib.assertTrue(nf != null);
 
             CachedNormalFile cnf = new CachedNormalFile(nf);
             cachedFiles.put(new Integer(block), cnf);
         }
 
         return cachedFiles.get(new Integer(block));
     }
 
     private void closeFile(int block) {
         CachedNormalFile cnf = cachedFiles.get(new Integer(block));
         Lib.assertTrue(cnf != null);
         cnf.decreaseUseCount();
     }
 
     private boolean addEntry(Directory folder, String name, int type, int block) {
         Directory current = folder;
         while (current.addEntry(name, type, block) == false) {
             if (current.hasNext())
                 current = current.loadNext();
             else {
                 int dirPos = freeList.occupy();
                 if (dirPos == -1)
                     return false;
                 Directory next = Directory.create(dirPos, current.getParent());
                 current.setNext(dirPos);
                 current = next;
             }
         }
         return true;
     }
 
     private NormalFile createFile(Directory folder, String fileName) {
         Lib.debug(dbgFilesys, "Creating file " + fileName);
 
         int pos = freeList.occupy();
         if (pos == -1)
             return null;
 
         Lib.debug(dbgFilesys, "\tinode=" + pos);
 
         NormalFile nf = NormalFile.create(pos, true, 1);
 
         if (addEntry(folder, fileName, Entry.NORMAL_FILE, pos) == false) {
             freeList.free(pos);
             return null;
         }
 
         return nf;
     }
 
     private void ls(Directory folder) {
         while (true) {
             for (int i = 0; i < folder.getValidCount(); ++i) {
                 Entry e = folder.getEntry(i);
                 Lib.debug(dbgFilesys, e.name);
             }
 
             if (folder.hasNext())
                 folder = folder.loadNext();
             else
                 break;
         }
         Lib.debug(dbgFilesys, "");
     }
 
     public OpenFile open(String name, boolean create) {
         Lib.debug(dbgFilesys, "Opening " + name);
 
         Entry e = findFileEntry(name, create);
         if (e == null) {
             Lib.debug(dbgFilesys, "\t" + name + " not found");
             return null;
         } else if (e.type == Entry.NORMAL_FILE) {
             Lib.debug(dbgFilesys, "\tinode=" + e.block);
 
             CachedNormalFile cnf = getFile(e.block);
             cnf.increaseUseCount();
             return new File(cnf, name);
         } else {
             Lib.assertTrue(e.type == Entry.DIRECTORY);
 
             String fileName = fileName(name);
             if (fileName == null)
                 return null;
 
             Directory dir = Directory.load(e.block);
             NormalFile nf = createFile(dir, fileName);
             if (nf == null)
                 return null;
             else {
                 CachedNormalFile cnf = getFile(nf.block);
                 cnf.increaseUseCount();
                 return new File(cnf, name);
             }
         }
     }
 
     public boolean remove(String fileName) {
         Lib.debug(dbgFilesys, "Removing " + fileName);
 
         String path = containingPath(fileName), file = fileName(fileName);
         if (path == null || file == null)
             return false;
 
         Entry eFolder = findEntry(path, true);
        Lib.debug(dbgFilesys, String.valueOf(eFolder == null));
         if (eFolder == null || eFolder.type != Entry.DIRECTORY)
             return false;
         Directory folder = (Directory) eFolder.load();
         Lib.assertTrue(folder != null);
 
         Entry entry = findSingleEntry(folder, file);
         if (entry == null || entry.type == Entry.DIRECTORY)
             return false;
 
         Lib.assertTrue(removeEntry(folder, file));
 
         if (entry.type == Entry.NORMAL_FILE) {
             CachedNormalFile cnf = getFile(entry.block);
             cnf.normalFile().decreaseLinkCount();
             if (!doRemoveFile(entry.block))
                 removingFiles.add(new Integer(entry.block));
         } else
             freeList.free(entry.block);
 
         return true;
     }
 
     private boolean removeEntry(Directory folder, String name) {
         Directory current = folder, last = null, target = null;
         Entry supplant = null;
         int pos = -1;
         while (true) {
             if (pos == -1) {
                 pos = current.findPos(name);
                 if (pos != -1)
                     target = current;
             }
 
             if (current.hasNext()) {
                 last = current;
                 current = current.loadNext();
                 Lib.assertTrue(current != null);
             } else {
                 if (current.getValidCount() == 0) {
                     Lib.assertTrue(target == null);
                     return false;
                 } else {
                     supplant = current.getEntry(current.getValidCount() - 1);
 
                     if (target != null) {
                         Lib.assertTrue(pos != -1);
 
                         target.replaceEntry(pos, supplant.name, supplant.type,
                                 supplant.block);
                         Lib.assertTrue(current.removeLastEntry());
 
                         if (current.getValidCount() == 0 && last != null) {
                             last.setNext(-1);
                             freeList.free(current.getBlock());
                         } else {
                             target.save();
                             if (target.getBlock() != current.getBlock())
                                 current.save();
                         }
                     } else
                         return false;
                 }
 
                 break;
             }
         }
         return true;
     }
 
     private void freeFile(NormalFile nf) {
         int block = nf.getBlock();
         cachedFiles.remove(new Integer(block));
 
         NormalFile current = nf;
         while (current != null) {
             for (int i = 0; i < current.getValidCount(); ++i)
                 freeList.free(current.getLink(i));
 
             int t = current.getBlock();
             if (current.hasNext())
                 current = current.loadNext();
             else
                 current = null;
             freeList.free(t);
         }
 
         cachedFiles.remove(new Integer(block));
     }
 
     private boolean doRemoveFile(int block) {
         CachedNormalFile cnf = getFile(block);
         if (!cnf.isUsing()) {
             if (cnf.normalFile().getLinkCount() == 0)
                 freeFile(cnf.normalFile());
 
             if (removingFiles.contains(new Integer(block)))
                 removingFiles.remove(new Integer(block));
 
             return true;
         } else
             return false;
 
     }
 
     public boolean createFolder(String name) {
         Lib.debug(dbgFilesys, "Make dir: " + name);
 
         String path = containingPath(name), dir = fileName(name);
         if (path == null || dir == null || dir.length() == 0)
             return false;
 
         Entry entry = findEntry(path, true);
         if (entry == null || entry.type != Entry.DIRECTORY)
             return false;
 
         Directory folder = (Directory) entry.load();
         if (findSingleEntry(folder, dir) != null)
             return false;
 
         int block = freeList.occupy();
         if (block == -1)
             return false;
 
         Directory.create(block, entry.block);
 
         if (!addEntry(folder, dir, Entry.DIRECTORY, block)) {
             freeList.free(block);
             return false;
         }
 
         return true;
     }
 
     public boolean removeFolder(String name) {
         Lib.debug(dbgFilesys, "Rmdir " + name);
         String path = containingPath(name), fileName = fileName(name);
         if (path == null || fileName == null || fileName.length() == 0)
             return false;
 
         Entry entry = findEntry(path, true);
         if (entry == null || entry.type != Entry.DIRECTORY)
             return false;
 
         Directory folder = (Directory) entry.load();
 
         Entry t = findSingleEntry(folder, fileName);
         if (t == null || t.type == Entry.NORMAL_FILE)
             return false;
 
         if (t.type == Entry.SYMBOLIC_LINK) {
             removeEntry(folder, fileName);
             freeList.free(t.block);
         } else {
             Lib.assertTrue(t.type == Entry.DIRECTORY);
 
             Directory dt = (Directory) t.load();
             if (dt.getValidCount() > 0 || dt.hasNext())
                 return false;
 
             removeEntry(folder, fileName);
             freeList.free(t.block);
         }
 
         return true;
     }
 
     public String[] readDir(String name) {
         return null;
     }
 
     public FileStat getStat(String name) {
         Entry entry = findEntry(name, false);
         if (entry == null)
             return null;
         if (entry.type == Entry.NORMAL_FILE) {
             NormalFile nf = getFile(entry.block).normalFile();
             return new FileStat(name, nf.getSize(), Entry.NORMAL_FILE, 0, 0,
                     nf.getLinkCount());
         } else if (entry.type == Entry.SYMBOLIC_LINK) {
             return new FileStat(name, 0, Entry.SYMBOLIC_LINK, 0, 0, 0);
         } else {
             Lib.assertTrue(entry.type == Entry.DIRECTORY);
             return new FileStat(name, 0, Entry.DIRECTORY, 0, 0, 0);
         }
     }
 
     public boolean createLink(String src, String dst) {
         Lib.debug(dbgFilesys, "Linking " + src + " and " + dst);
         String path = containingPath(dst), name = fileName(dst);
         if (path == null || name == null || name.length() == 0)
             return false;
 
         Entry eSrc = findEntry(src, true);
         if (eSrc == null || eSrc.type != Entry.NORMAL_FILE)
             return false;
 
         CachedNormalFile cnf = getFile(eSrc.block);
 
         Entry eDst = findEntry(path, true);
         if (eDst == null || eDst.type != Entry.DIRECTORY)
             return false;
 
         Directory fDst = (Directory) eDst.load();
         if (findSingleEntry(fDst, name) != null)
             return false;
 
         cnf.setDirty();
         cnf.normalFile().increaseLinkCount();
         if (addEntry(fDst, name, Entry.NORMAL_FILE, eSrc.block) == false) {
             cnf.normalFile().decreaseLinkCount();
             return false;
         }
 
         return true;
     }
 
     public boolean createSymlink(String src, String dst) {
         Lib.debug(dbgFilesys, "Symbolicly linking " + src + " and " + dst);
         String path = containingPath(dst), name = fileName(dst);
         if (path == null || name == null || name.length() == 0)
             return false;
 
         Entry eDst = findEntry(path, true);
         if (eDst == null || eDst.type != Entry.DIRECTORY)
             return false;
 
         Directory fDst = (Directory) eDst.load();
         if (findSingleEntry(fDst, name) != null)
             return false;
 
         int block = freeList.occupy();
         SymbolicLink sl = SymbolicLink.create(block, src);
         if (addEntry(fDst, name, Entry.SYMBOLIC_LINK, block) == false) {
             freeList.free(block);
             return false;
         }
 
         return true;
     }
 
     private String constructPathName(int block, int lower) {
         Directory dir = null;
         String name = "";
         if (block != rootBlock || lower != -1)
             dir = Directory.load(block);
         if (lower != -1) {
             name = findEntryName(dir, lower) + "/";
             Lib.assertTrue(name != null);
         }
 
         if (block == rootBlock)
             return "/" + name;
         else
             return constructPathName(dir.getParent(), block) + name;
     }
 
     public String getCanonicalPathName(String name) {
         Entry entry = findEntry(name, true);
         if (entry == null || entry.type != Entry.DIRECTORY)
             return null;
 
         return constructPathName(entry.block, -1);
     }
 
     public int getFreeSize() {
         return 0;
     }
 
     public int getSwapFileSectors() {
         return 0;
     }
 
     private static final char dbgFilesys = 'f';
 }
