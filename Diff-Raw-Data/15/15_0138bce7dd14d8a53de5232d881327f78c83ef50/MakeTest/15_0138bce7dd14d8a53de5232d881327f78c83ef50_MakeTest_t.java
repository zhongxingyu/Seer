 package com.vhly.epubmaker;
 
 import com.vhly.epubmaker.epub.EPubFile;
 import com.vhly.epubmaker.epub.Item;
 import com.vhly.epubmaker.epub.content.Chapter;
 import net.dratek.browser.util.StreamUtil;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: vhly[FR]
  * Date: 11-12-2
  * Email: vhly@163.com
  */
 public class MakeTest {
     public static void main(String[] args) {
         EPubFile file = new EPubFile();
         file.setTitle("Test");
         file.setAuthor("vhly[FR]");
         file.setUUID("998482814");
         file.setDescript("This is a Test");
 
         File f = new File("./cover.jpg");
         FileInputStream fin = null;
         try {
             fin = new FileInputStream(f);
             byte[] bytes = StreamUtil.readStream(fin);
             file.setCover("cover.jpg", bytes, "image/jpeg");
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } finally {
             StreamUtil.close(fin);
         }
         // TODO In this implements, title must setting with ascii char, not support other char now.
        Chapter ch = loadChapter("测试", "c001.xhtml", "./res/book1/c001.xhtml");
         if (ch != null) {
             file.addChapter(ch);
         }
         file.save("./MakeTest.epub");
     }
 
     /**
      * load file from file sytem and set chapter
      *
      * @param title Chpater title
      * @param ename ename for epub zip entry
      * @param fPath real path
      * @return Chapter will create
      */
     private static Chapter loadChapter(String title, String ename, String fPath) {
         Chapter ret = null;
         if (title != null && ename != null && fPath != null) {
             File f = new File(fPath);
             if (f.exists() && f.canRead()) {
                 FileInputStream fin = null;
                 byte[] content = null;
                 try {
                     fin = new FileInputStream(f);
                     content = StreamUtil.readStream(fin);
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 } finally {
                     StreamUtil.close(fin);
                 }
                 if (content != null && content.length > 0) {
                     ret = new Chapter();
                     ret.setTitle(title);
                     ret.setEntryName(ename);
                     ret.setContent(content);
                     Item chapterItem = new Item(ename, ename);
                     ret.setChapterItem(chapterItem);
                 }
             }
         }
         return ret;
     }
 }
