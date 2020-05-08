 package com.vhly.epubmaker.epub;
 
 import com.vhly.epubmaker.epub.content.Chapter;
 import com.vhly.epubmaker.epub.toc.NavPoint;
 import net.dratek.browser.util.StreamUtil;
 
 import java.io.*;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: vhly[FR]
  * EMail: vhly@163.com
  * Date: 2011-9-2
  * Time: 0:11:53
  */
 
 /**
  * EPub File, .epub file is a zip file actually.
  */
 public class EPubFile {
     /**
      * Default book content folder.
      */
     private String defaultFolder = "OEBPS";
 
     /**
      * MIME Type
      * Default type is application/epub+zip
      */
     private String mimetype = "application/epub+zip";
 
     /**
      * Content container, all enter pointer defines in here.
      */
     private Container container;
     /**
      * Chapter store;
      */
     private Vector<Chapter> chapters;
 
     private ZipFile zipFile;
 
     public EPubFile() {
         initFile();
     }
 
     /**
      * Constructor with File path
      *
      * @param name File name
      * @throws IOException IOE
      */
     public EPubFile(String name) throws IOException {
         zipFile = new ZipFile(name);
         initFile();
     }
 
     /**
      * Constructor with File path
      *
      * @param file File object
      * @param mode file operation mode
      * @throws IOException IOE
      */
     public EPubFile(File file, int mode) throws IOException {
         zipFile = new ZipFile(file, mode);
         initFile();
     }
 
     /**
      * Constructor with File path
      *
      * @param file File object
      * @throws ZipException Zip Exception
      * @throws IOException  IOE
      */
     public EPubFile(File file) throws ZipException, IOException {
         zipFile = new ZipFile(file);
         initFile();
     }
 
     private void initFile() {
         chapters = new Vector<Chapter>();
         container = new Container();
     }
 
     /**
      * Get MIME Type<br/>
      * If no set this field, return default mimetype
      *
      * @return String
      */
     public String getMimetype() {
         return mimetype;
     }
 
     /**
      * Set MIME type<br/>
      * Default MIME type is application/epub+zip
      *
      * @param mimetype String
      */
     public void setMimetype(String mimetype) {
         this.mimetype = mimetype;
     }
 
     public void addChapter(Chapter ch) {
         chapters.add(ch);
         Item item = ch.getChapterItem();
         if (item != null) {
             addItem(item);
             addItemRefByItem(item);
             addNavPointForChapter(ch);
         }
     }
 
     private void addNavPointForChapter(Chapter ch) {
         if (ch != null) {
             Item item = ch.getChapterItem();
             if (item != null) {
                 OPF opf = container.getPackageFile();
                 NCX ncx = opf.getToc();
 
                 NavPoint np = new NavPoint();
                 np.id = item.id;
                 np.label = ch.getTitle();
                 np.content = item.href;
 
                 ncx.addNavPoint(np);
             }
         }
     }
 
     private void addItemRefByItem(Item item) {
         if (item != null) {
             if (item.id != null && item.href != null && item.mediatype != null) {
                 OPF opf = container.getPackageFile();
                 if (opf != null) {
                     Spine spine = opf.getSpine();
                     ItemRef ref = new ItemRef();
                     ref.idref = item.id;
                     spine.addRef(ref);
                 }
             } else {
                 throw new RuntimeException("Item's information must set");
             }
         }
     }
 
     /**
      * Add Item adapter for epub file setting.
      *
      * @param it Item
      */
     private void addItem(Item it) {
         if (it != null) {
             if (it.id != null && it.href != null && it.mediatype != null) {
                 OPF opf = container.getPackageFile();
                 if (opf != null) {
                     Manifest manifest = opf.getManifest();
                     if (manifest != null) {
                         manifest.addItem(it);
                     }
                 }
             } else {
                 throw new RuntimeException("Item's information must set");
             }
         }
     }
 
     /**
      * Load all content from self zip entries
      *
      * @return boolean load ok?
      */
     public boolean load() {
         boolean bret = false;
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
         if (entries != null) {
             ZipEntry entry = null;
             String ename;
 
             entry = zipFile.getEntry("mimetype");
             if (entry != null) {
                 String s = readMIMEType(entry);
                 if (s != null) {
                     mimetype = s;
                 }
             }
 
             entry = zipFile.getEntry("META-INF/container.xml");
             if (entry != null) {
                 Container cc = readContainer(entry);
                 if (cc != null) {
                     container = cc;
                 }
             }
 
             if (container != null) {
                 OPF opf = container.getPackageFile();
                 String entryName = opf.getEntryName();
                 entry = zipFile.getEntry(entryName);
                 if (entry != null) {
                     byte[] buf = readEntryData(entry);
                     if (buf != null && buf.length > 0) {
                         opf.parse(buf);
                     }
                 }
                 String tocHref = opf.getTocHref();
                 if (tocHref != null) {
                     int index = entryName.lastIndexOf('/');
                     if (index != -1) {
                         String s = entryName.substring(0, index + 1);
                         tocHref = s + tocHref;
                     }
                     ZipEntry en = zipFile.getEntry(tocHref);
                     if (en != null) {
                         byte[] buf = readEntryData(en);
                         if (buf != null && buf.length > 0) {
                             NCX ncx = new NCX(opf);
                             ncx.setEntryName(tocHref);
                             if (ncx.parse(buf)) {
                                 opf.setToc(ncx);
                             }
                         }
                     }
                 }
 
                 Spine spine = opf.getSpine();
                 Manifest manifest = opf.getManifest();
                 if (spine != null && manifest != null) {
                     // TODO Load Spine itemrefs for EPub chapter load.
                     ItemRef[] refs = spine.getAllItemRefs();
                     if (refs != null) {
                         int len = refs.length;
                         int index = entryName.lastIndexOf('/');
                         String parentPath = "";
                         if (index != -1) {
                             parentPath = entryName.substring(0, index + 1);
                         }
                         if (len > 0) {
                             ItemRef ret;
                             Item item;
                             String ipath;
                             for (int i = 0; i < len; i++) {
                                 ret = refs[i];
                                 String idref = ret.idref;
                                 item = manifest.getItem(idref);
                                 if (item != null) {
                                     ipath = parentPath + item.href;
                                     ZipEntry en = zipFile.getEntry(ipath);
                                     if (en == null) {
                                         en = zipFile.getEntry(item.href);
                                     }
                                     if (en != null) {
                                         byte[] data = readEntryData(en);
                                         if (data != null) {
                                             Chapter chapter = new Chapter();
                                             chapter.setContent(data);
                                             chapter.setChapterItem(item);
                                             chapters.add(chapter);
                                         }
                                     }
                                 }
                             }
                             bret = true;
                         }
                     }
                 }
 
             }
         }
         return bret;
     }
 
     /**
      * read entry to byte[]
      *
      * @param entry ZipEntry
      * @return byte[] entry's data
      */
     public byte[] readEntryData(ZipEntry entry) {
         InputStream in = null;
         byte[] buf = null;
         try {
             in = zipFile.getInputStream(entry);
             buf = StreamUtil.readStream(in);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             StreamUtil.close(in);
         }
         return buf;
     }
 
     /**
      * Get Chapters<br/>
      * This method return chapters by manifest info, not NCX,<br/>
      * if use ncx info, will show all list contents.
      *
      * @return Chapters
      */
     public Chapter[] getChapters() {
         Chapter[] ret = null;
         if (chapters != null && !chapters.isEmpty()) {
             int size = chapters.size();
             ret = new Chapter[size];
             chapters.copyInto(ret);
         }
         return ret;
     }
 
     /**
      * Read container entry and return it.
      *
      * @param entry ZipEntry in self
      * @return Container
      */
     private Container readContainer(ZipEntry entry) {
         Container ret = null;
         if (entry != null) {
             InputStream inputStream = null;
             try {
                 inputStream = zipFile.getInputStream(entry);
                 byte[] buf = StreamUtil.readStream(inputStream);
                 if (buf != null && buf.length > 0) {
                     // Parse buf
                     Container c = new Container();
                     c.setEntryName(entry.getName());
                     if (c.parse(buf)) {
                         ret = c;
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 StreamUtil.close(inputStream);
             }
         }
         return ret;
     }
 
     /**
      * Find mimetype entry and load
      *
      * @param entry entry which name is mimetype
      * @return String mimetype
      */
     private String readMIMEType(ZipEntry entry) {
         String ret = null;
         if (entry != null) {
             InputStream inputStream = null;
             try {
                 inputStream = zipFile.getInputStream(entry);
                 byte[] buf = StreamUtil.readStream(inputStream);
                 if (buf != null && buf.length > 0) {
                     ret = new String(buf, "UTF-8");
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 StreamUtil.close(inputStream);
             }
 
         }
         return ret;
     }
 
     /**
      * @param path file to save
      * @return boolean save ok?
      */
     public boolean save(String path) {
         boolean bret = false;
         if (path != null) {
             File f = new File(path);
             File pf = f.getParentFile();
             boolean bok = true;
             if (!pf.exists()) {
                 bok = pf.mkdirs();
             }
             if (bok) {
                 // create parent ok or parent exists
                 FileOutputStream fout = null;
                 ZipOutputStream zout = null;
                 DataOutputStream dout = null;
                 try {
                     fout = new FileOutputStream(f);
                     zout = new ZipOutputStream(fout);
                     dout = new DataOutputStream(zout);
                    zout.setMethod(ZipOutputStream.DEFLATED);
                     zout.setComment("EPubMaker");
                     zout.setLevel(ZipOutputStream.DEFLATED);
                    ZipEntry ze = new ZipEntry("mimetype");
                    long tt = System.currentTimeMillis();
                    ze.setTime(tt);
                    zout.putNextEntry(ze);
                     dout.write(mimetype.getBytes());
                     zout.closeEntry();
                     String en = container.getEntryName();
                    ze = new ZipEntry(en);
                    ze.setTime(tt);
                    zout.putNextEntry(ze);
                     container.save(dout);
                     zout.closeEntry();
                     OPF opf = container.getPackageFile();
                    en = opf.getEntryName();
                    ze = new ZipEntry(en);
                    ze.setTime(tt);
                    zout.putNextEntry(ze);
                     opf.save(dout);
                     zout.closeEntry();
 
                     NCX toc = opf.getToc();
                    en = toc.getEntryName();
                    ze = new ZipEntry(en);
                    ze.setTime(tt);
                    zout.putNextEntry(ze);
                     toc.save(dout);
                     zout.closeEntry();
 
                     for (Chapter ch : chapters) {
                         String entryName = ch.getEntryName();
                        ze = new ZipEntry(entryName);
                        ze.setTime(tt);
                        zout.putNextEntry(ze);
                         ch.save(dout);
                         zout.closeEntry();
                     }
 
                     zout.finish();
 
                 } catch (IOException e) {
                     e.printStackTrace();
                 } finally {
                     StreamUtil.close(dout);
                     StreamUtil.close(zout);
                     StreamUtil.close(fout);
                 }
             }
         }
         return bret;
     }
 
     /**
      * Set EPub File's title
      * @param title String title
      */
     public void setTitle(String title) {
         if (title != null) {
             OPF opf = container.getPackageFile();
             Metadata metadata = opf.getMetadata();
             metadata.setTitle(title);
             metadata.setSubject(title);
             metadata.setDescription(title);
 
             NCX toc = opf.getToc();
             toc.setDocTitle(title);
         }
     }
 
     /**
      * Set EPub File's title
      * @param desc String title
      */
     public void setDescript(String desc) {
         if (desc != null) {
             OPF opf = container.getPackageFile();
             Metadata metadata = opf.getMetadata();
             metadata.setDescription(desc);
         }
     }
 
     public void setAuthor(String author) {
         if(author != null){
             OPF opf = container.getPackageFile();
             Metadata metadata = opf.getMetadata();
             metadata.setCreator(author);
 
             NCX toc = opf.getToc();
             toc.setDocAuthor(author);
         }
     }
 
     public void setUUID(String uuid) {
         if(uuid != null){
             OPF opf = container.getPackageFile();
             Metadata metadata = opf.getMetadata();
             metadata.setIdentifier(uuid);
         }
     }
 }
