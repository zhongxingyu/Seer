 package xelat.easyscroll;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.content.res.Resources;
 import android.util.Log;
 
 public class VerseWrapper {
 
     int bookid, chapter, verse;
     static Resources res;
 
     VerseWrapper(int bookid, int chapter, int verse) {
         this.bookid = bookid;
         this.chapter = chapter;
         this.verse = verse;
     }
     
     static void setResources(Resources r) {res = r;}
 
     private String getURL(String version) {
         if (version.equals("BBE") || version.equals("NB5") || version.equals("KJV") || version.equals("HB5") || version.equals("CB5") || version.equals("LZ5")) {
             // TODO: convert this to do programmatically
             String bookurl = this.getBookURL().toLowerCase();
             bookurl = bookurl.replaceAll("%20", "");
             bookurl = bookurl.substring(0, 3);
             return "http://bible.ccim.org/cgi-user/bible/ob?version=" + version.toLowerCase() + "&book=" + bookurl + "&chapter=" + this.getChapter();
         } else {
             //  Generate the biblegateway url in the form of http://www.biblegateway.com/passage/?search=Ephesians%206&version=CUVMPT
             return "http://www.biblegateway.com/passage/?search=" + this.getBookURL() + "+" + this.getChapter() + "&version=" + version;
         }
     }
 
 
     int downloadPassage(String version) {
         try {
             Log.d("VerseWrapper downloadPassage", "getURL: " + getURL(version));
             InputStream stream = (new URL(getURL(version))).openStream();
             BufferedReader br = null;
             String singleLine, bookVersionName = "", s;
             String[] array;
             boolean bookVersionNameDone=false, parseFinish=false;
 //            int lineCounter = 0;
             int i, verse;
             MyObject p = null;
             
             if (version.equals("CUV")) {
                 br = new BufferedReader(new InputStreamReader(stream, "UTF8"), 8192);
                 // While the BufferedReader readLine is not null  
                 while (true) {
                     singleLine = br.readLine();
 //Log.d("VerseWrapper", "CUV lineCounter: " + lineCounter + "; singleLine: " + singleLine);
                     if (singleLine==null) {break;}
 //                    lineCounter++;
 //                    if (lineCounter<res.getInteger(R.integer.BIBLEGATEWAY_LINE_SKIP)) {continue;} // skip the first xxx lines to speed up
                     
                     // get the book version name
                     if (singleLine.indexOf("<div class='heading passage-class-0'")>=0) {
                         singleLine = singleLine.substring(singleLine.indexOf("<div class='heading passage-class-0'"));
                         bookVersionName = singleLine.substring(singleLine.indexOf("<h3>")+4, singleLine.indexOf("</h3>")).trim();
                         array = bookVersionName.split(" ");
                         bookVersionName = bookVersionName.substring(0, bookVersionName.indexOf(array[array.length-1]));
     
                         if (singleLine.indexOf("<sup class=\"versenum\">")==-1) {
                             singleLine = br.readLine();
                             if (singleLine==null) {
                                 Log.e("VerseWrapper", "Error during reading the passage");
                             }
                         }
                         // get the passage
                         parseCUV(version, singleLine.split("<sup class=\"versenum\">"), bookVersionName);
                         break;
                     }
                 }
 //Log.d("VerseWrapper", "CUV lineCounter: " + lineCounter + "; singleLine==null: " + (singleLine==null));
             } else if (version.equals("CUVMPT")) {
                 br = new BufferedReader(new InputStreamReader(stream, "UTF8"), 8192);
                 // While the BufferedReader readLine is not null  
                 while (true) {
                     singleLine = br.readLine();
                     if (singleLine==null) {break;}
 //                    lineCounter++;
 //                    if (lineCounter<res.getInteger(R.integer.BIBLEGATEWAY_LINE_SKIP)) {continue;} // skip the first xxx lines to speed up
                     
                     // get the book version name
                     if (singleLine.indexOf("<div class='heading passage-class-0'")>=0) {
                         bookVersionName = singleLine.substring(singleLine.indexOf("<h3>")+4, singleLine.indexOf("</h3>")).trim();
                         array = bookVersionName.split(" ");
                         bookVersionName = bookVersionName.substring(0, bookVersionName.indexOf(array[array.length-1]));
     
                         if (singleLine.indexOf("<sup class=\"versenum\">")==-1) {
                             singleLine = br.readLine();
                             if (singleLine==null) {
                                 Log.e("VerseWrapper", "Error during reading the passage");
                             }
                         }
                         // get the passage
                         parseCUVMPT(version, singleLine.split("<sup class=\"versenum\">"), bookVersionName);
                         break;
                     }
                 }
             } else if (version.equals("NIV")) {
                 br = new BufferedReader(new InputStreamReader(stream, "UTF8"), 8192);
                 // While the BufferedReader readLine is not null  
                 while (true) {
                     singleLine = br.readLine();
                     if (singleLine==null) {break;}
                     // drop everything after "Cross references:"
                     if (singleLine.indexOf("Cross references:")>=0) {
                         singleLine = singleLine.substring(0, singleLine.indexOf("Cross references:"));
                     }
                     
 //                    lineCounter++;
 //                    if (lineCounter<res.getInteger(R.integer.BIBLEGATEWAY_LINE_SKIP)) {continue;} // skip the first xxx lines to speed up
                     
                     // get the book version name
                     if (singleLine.indexOf("<div class='heading passage-class-0'")>=0) {
                         singleLine = singleLine.substring(singleLine.indexOf("<div class='heading passage-class-0'"));
                         bookVersionName = singleLine.substring(singleLine.indexOf("<h3>")+4, singleLine.indexOf("</h3>")).trim();
                         array = bookVersionName.split(" ");
                         bookVersionName = bookVersionName.substring(0, bookVersionName.indexOf(array[array.length-1]));
     
                         if (singleLine.indexOf("<sup class=\"versenum\">")==-1) {
                             singleLine = br.readLine();
                             if (singleLine==null) {
                                 Log.e("VerseWrapper", "Error during reading the passage");
                             }
                         }
                         // get the passage
                         parseNIV(version, singleLine.split("<sup class=\"versenum\">"), bookVersionName);
                         break;
                     }
                 }
             } else if (version.equals("BBE") || version.equals("KJV") || version.equals("HB5") || version.equals("NB5") || version.equals("CB5") || version.equals("LZ5")) {
                 br = new BufferedReader(new InputStreamReader(stream, "Big5"), 8192);
 //Log.d("VerseWrapper", "inside BBE");
                 // While the BufferedReader readLine is not null
                 verse = 1;
                 while (true) {
                     singleLine = br.readLine();
                     if (singleLine==null || parseFinish) {break;}
                     
 /*
                     lineCounter++;
 Log.d("VerseWrapper", "lineCounter: " + lineCounter + "; singleLine: " + singleLine);
 */
                     // get the book version name
                     // TODO: move the hardcoded string to variable
                     if (!bookVersionNameDone && singleLine.indexOf("<h1 align=center>")>=0) {
                         singleLine = singleLine.substring(singleLine.indexOf("<h1 align=center>"));
                         bookVersionName = singleLine.substring(17, singleLine.indexOf("</h1>")); // TODO: move 17 to function
 //Log.d("VerseWrapper", "bookVersionName: " + bookVersionName + "; singleLine: " + singleLine);
                         singleLine = singleLine.substring(singleLine.indexOf("</h1>"));
                         bookVersionNameDone = true;
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                     }
                     
                     if (bookVersionNameDone) {
                         if (singleLine.indexOf("<tr><td valign=top><font size=-1>")>=0) {
                             array = singleLine.split("<tr><td valign=top><font size=-1>");
                             for (i=0; i<array.length; i++) {
                                 if (array[i].lastIndexOf("<td>")>0) { // it must also not be at 0
                                     p.getVerseWrapper().setVerse(verse);
 //Log.d("VerseWrapper downloadPassage", "i: " + i + "; verse: " + verse);
 									verse++;
                                     if (array[i].indexOf("<br></td></tr>")>=0) {
                                         s = array[i].substring(array[i].lastIndexOf("<td>")+4, array[i].indexOf("<br></td></tr>"));
                                     } else {
                                         s = array[i].substring(array[i].lastIndexOf("<td>")+4); // get the remaining of the line
                                     }
                                     if (s.indexOf("not available")>=0) {
                                         // strip off those lines than says not available, such as Jonah 1:17
                                         s = s.substring(0, s.indexOf("not available"));
                                     }
                                     p.setText(s);
     //Log.d("VerseWrapper", "p: " + p);
                                     MyDB.insert(p);
                                 } else if (array[i].indexOf("</table>")>=0) {
                                     parseFinish = true;
                                     break;
                                 }
                             }
                         }
                     }
                     
                 }
 //Log.d("VerseWrapper", "lineCounter: " + lineCounter + "; singleLine==null: " + (singleLine==null));
 /*
             } else if (version.equals("HB5") || version.equals("NB5") || version.equals("CB5") || version.equals("LZ5")) {
                 // While the BufferedReader readLine is not null
                 while (true) {
                     singleLine = br.readLine();
                     if (singleLine==null) {break;}
                     
                     // get the book version name
                     // TODO: move the hardcoded string to variable
                     if (singleLine.indexOf("<h1 align=center>")>=0) {
                         singleLine = singleLine.substring(singleLine.indexOf("<h1 align=center>"));
                         bookVersionName = singleLine.substring(17, singleLine.indexOf("</h1>")); // TODO: move 17 to function
 Log.d("VerseWrapper", "lineCounter: " + lineCounter + "; bookVersionName: " + bookVersionName + "; singleLine: " + singleLine);
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                         
                         array = singleLine.split("<tr><td valign=top><font size=-1>");
                         for (verse=1; verse<array.length; verse++) {
                             if (array[verse].lastIndexOf("<td>")>=0 && array[verse].indexOf("<br></td></tr>")>=0) {
                                 p.getVerseWrapper().setVerse(verse);
                                 String s = array[verse].substring(array[verse].lastIndexOf("<td>")+4, array[verse].indexOf("<br></td></tr>"));
                                 p.setText(s);
 Log.d("VerseWrapper", "p: " + p);
                                 MyDB.insert(p);
                             }
                         }
                     }
                 }
 */
             }
         
             // Close the InputStream and BufferedReader 
             stream.close();
             if (br!=null) {
             	br.close(); 
             }
      
         } catch (UnsupportedEncodingException e2) {
             e2.printStackTrace();
             Log.e("MyList", "UnsupportedEncodingException");
             return R.integer.ERROR_UNSUPPORTED_ENCODING;
         } catch (MalformedURLException e3) {
             e3.printStackTrace();
             Log.e("MyList", "MalformedURLException");
             return R.integer.ERROR_MALFORMED_EXCEPTION;
         } catch (IOException e) {
             e.printStackTrace();
             Log.e("MyList", "IOException");
             return R.integer.ERROR_IOEXCEPTION;
         }
         return R.integer.DOWNLOAD_SUCCESSFUL;
     }
     
     void parseCUV(String version, String[] array, String bookVersionName) {
         int v, chapterNumIndex;
         String s;
         MyObject p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
         for (v=0; v<array.length; v++) {
             p.getVerseWrapper().setVerse(v+1);
             chapterNumIndex = array[v].indexOf("class=\"chapternum\">");
             if (chapterNumIndex>=0) {
                 // this is the case of chapternum...
                 s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                 p.setText(stripCUVHtml(array[v], chapterNumIndex+19+s.indexOf("")+1));
             } else {
                 // this is the case of 2 ... with no chapternum
                 p.setText(stripCUVHtml(array[v], array[v].indexOf("")+1));
             }
             MyDB.insert(p);
             p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
         }
     }
     
     void parseCUVMPT(String version, String[] array, String bookVersionName) {
         int v, h3Index, chapterNumIndex, h3NextIndex;
         String s;
         MyObject p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
         for (v=0; v<array.length; v++) {
             p.getVerseWrapper().setVerse(v+1);
             h3Index = array[v].indexOf("<h3>");
             if (h3Index>=0) {
                 chapterNumIndex = array[v].indexOf("class=\"chapternum\">");
                 if (chapterNumIndex>=0) {
                     if (chapterNumIndex < h3Index) {
                         // this is the case of chapternum ... <h3>...</h3>
                         s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                         p.setText(stripCUVHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, h3Index));
                         MyDB.insert(p);
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                         p.setHeader(stripCUVHtml(array[v], h3Index+4));
                     } else {
                         p.setHeader(stripCUVHtml(array[v], h3Index, array[v].indexOf("</h3>")));
                         h3NextIndex = array[v].substring(chapterNumIndex).indexOf("<h3>");
                         s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                         if (h3NextIndex>=0) {
                             // this is the case of <h3>...</h3>... chapternum ... <h3>...</h3>
                             p.setText(stripCUVHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, chapterNumIndex+h3NextIndex));
                         } else {
                             // this is the case of <h3>...</h3>... chapternum
                             p.setText(stripCUVHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, array[v].length()));
                         }
                         MyDB.insert(p);
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                         if (h3NextIndex>=0) {
                             p.setHeader(stripCUVHtml(array[v], h3NextIndex+4));
                         }
                     }
                 } else {
                     // this is the case of 2 ... <h3>...</h3> with no chapternum
                     p.setText(stripCUVHtml(array[v], array[v].indexOf("")+1, h3Index));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                     p.setHeader(stripCUVHtml(array[v], h3Index+4));
                 }
             } else {
                 chapterNumIndex = array[v].indexOf("class=\"chapternum\">");
                 if (chapterNumIndex>=0) {
                     // this is the case of chapternum...
                     s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                     p.setText(stripCUVHtml(array[v], chapterNumIndex+19+s.indexOf("")+1));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                 } else {
                     // this is the case of 2 ... with no chapter no <h3>
                     p.setText(stripCUVHtml(array[v], array[v].indexOf("")+1));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                 }
             }
         }
     }
     
     void parseNIV(String version, String[] array, String bookVersionName) {
         int v, h3Index, chapterNumIndex, h3NextIndex;
         String s;
         MyObject p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
         for (v=0; v<array.length; v++) {
             p.getVerseWrapper().setVerse(v+1);
             h3Index = array[v].indexOf("<h3>");
             if (h3Index>=0) {
                 chapterNumIndex = array[v].indexOf("class=\"chapternum\">");
                 if (chapterNumIndex>=0) {
                     if (chapterNumIndex < h3Index) {
                         // this is the case of chapternum ... <h3>...</h3>
                         s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                         p.setText(stripHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, h3Index));
                         MyDB.insert(p);
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                         p.setHeader(stripHtml(array[v], h3Index+4));
                     } else {
                         p.setHeader(stripHtml(array[v], h3Index, array[v].indexOf("</h3>")));
                         h3NextIndex = array[v].substring(chapterNumIndex).indexOf("<h3>");
                         s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                         if (h3NextIndex>=0) {
                             // this is the case of <h3>...</h3>... chapternum ... <h3>...</h3>
                             p.setText(stripHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, chapterNumIndex+h3NextIndex));
                         } else {
                             // this is the case of <h3>...</h3>... chapternum
                             p.setText(stripHtml(array[v], chapterNumIndex+19+s.indexOf("")+1, array[v].length()));
                         }
                         MyDB.insert(p);
                         p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                         if (h3NextIndex>=0) {
                             p.setHeader(stripHtml(array[v], h3NextIndex+4));
                         }
                     }
                 } else {
                     // this is the case of 2 ... <h3>...</h3> with no chapternum
                     p.setText(stripHtml(array[v], array[v].indexOf("")+1, h3Index));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                     p.setHeader(stripHtml(array[v], h3Index+4));
                 }
             } else {
                 chapterNumIndex = array[v].indexOf("class=\"chapternum\">");
                 if (chapterNumIndex>=0) {
                     // this is the case of chapternum...
                     s = array[v].substring(chapterNumIndex + 19); // TODO: convert the 19 to setting
                     p.setText(stripHtml(array[v], chapterNumIndex+19+s.indexOf("")+1));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                 } else {
                     // this is the case of 2 ... with no chapter no <h3>
                     p.setText(stripHtml(array[v], array[v].indexOf("")+1));
                     MyDB.insert(p);
                     p = new MyObject(getBookID(), bookVersionName, getChapter(), version);
                 }
             }
         }
     }
     
     String stripHtml(String input, int start) {return stripHtml(input, start, input.length());}
     String stripHtml(String input, int start, int end) {return stripHtml(input, start, end, false);}
     String stripHtml(String input, int start, int end, boolean debug) {
         String s = input.substring(start, end);
         
         s = s.replaceAll("<[^<>]+>", ""); // trim the first level of tags
         if (debug) {
             Log.d("MyList", "s after first strip: " + s);
         }
         s = s.replaceAll("<[^<>]+>", ""); // trim the second level of tags
         if (debug) {
             Log.d("MyList", "s after second strip: " + s);
         }
 //        s = s.replaceAll("[[^[]]+]", ""); // strip the [xxx]
         if (debug) {
 //            Log.d("MyList", "s after [] strip: " + s);
         }
         s = s.replaceAll("&nbsp;", " "); // convert space to real space
         if (debug) {
             Log.d("MyList", "s after &nbsp strip: " + s);
         }
         s = s.replaceAll("\\[[^]]*]", ""); // trim [xx]
         s = s.replaceAll("\\s+", " "); // convert multiple space to single
         
         return s;
     }
     String stripCUVHtml(String input, int start) {return stripCUVHtml(input, start, input.length());}
     String stripCUVHtml(String input, int start, int end) {
         String s = input.substring(start, end);
         s = s.replaceAll("<[^<>]+>", ""); // trim the first level of tags
         s = s.replaceAll("&nbsp;", " "); // convert space to real space
         return s.replaceAll("\\s+", " "); // convert multiple space to single
     }
        
     // returns the next chapter
     VerseWrapper nextChapter() {
         int chapterCount = this.getChapterCount();
         if (this.chapter<chapterCount) {
             // go to the next chapter
             return new VerseWrapper(this.bookid, this.chapter+1, 1);
         }
         // go to next book
         VerseWrapper w = new VerseWrapper(this.bookid+1, 1, 1);
         if (w.getChapterCount()>0) {return w;}
         // already the last book
         return null;
     }
 
     // returns the previous chapter
     VerseWrapper prevChapter() {
         if (this.chapter>1) {
             // go to the previous chapter
            return new VerseWrapper(this.bookid, chapter-1, 1);
         }
         // get the previous book
         VerseWrapper w = new VerseWrapper(this.bookid-1, 1, 1);
         int count = w.getChapterCount();
         if (count>0) {
             // go to the last chapter 
             w.setChapter(count);
             return w;
         }
         // already the last book
         return null;
     }
 
     public int getBookID() {
         return bookid;
     }
     
     public String getBookURL() {
         return MyDB.getBookURLName(this.bookid);
     }
 
     public int getChapterCount() {
         return MyDB.getChapterCount(this.bookid);
     }
 
     public int getVerseCount() {
         return MyDB.getVerseCount(this.bookid, this.chapter);
     }
 
     public void setBookID(int bookid) {
         this.bookid = bookid;
     }
 
     public int getChapter() {
         return this.chapter;
     }
 
     public void setChapter(int chapter) {
         this.chapter = chapter;
     }
 
     public int getVerse() {
         return this.verse;
     }
 
     public void setVerse(int verse) {
         this.verse = verse;
     }
     
     public void updateTo(VerseWrapper v) {
         this.setBookID(v.getBookID());
         this.setChapter(v.getChapter());
         this.setVerse(v.getVerse());
     }
 
     @Override
     public String toString() {
         return "Verse [bookid=" + bookid + ", chapter=" + chapter + ", verse="
                 + verse + "]";
     }
 
     
 }
