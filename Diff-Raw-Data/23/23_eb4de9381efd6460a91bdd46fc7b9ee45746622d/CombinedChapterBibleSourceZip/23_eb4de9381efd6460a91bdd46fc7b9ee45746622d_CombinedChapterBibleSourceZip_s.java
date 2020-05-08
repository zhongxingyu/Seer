 package goBible.base;
 
 //
 //  CombinedChapterBibleSource.java
 //  GoBible
 //
 //	Go Bible is a Free Bible viewer application for Java mobile phones (J2ME MIDP 1.0 and MIDP 2.0).
 //	Copyright © 2003-2008 Jolon Faichney.
 //	Copyright © 2008-2009 CrossWire Bible Society.
 //
 //	This program is free software; you can redistribute it and/or
 //	modify it under the terms of the GNU General Public License
 //	as published by the Free Software Foundation; either version 2
 //	of the License, or (at your option) any later version.
 //
 //	This program is distributed in the hope that it will be useful,
 //	but WITHOUT ANY WARRANTY; without even the implied warranty of
 //	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //	GNU General Public License for more details.
 //
 //	You should have received a copy of the GNU General Public License
 //	along with this program; if not, write to the Free Software
 //	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 //
 import goBible.common.TranslationNotFoundException;
 import goBible.common.GBCToolkit;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import net.sf.jazzlib.ZipEntry;
 import net.sf.jazzlib.ZipFile;
 import org.albite.io.RandomReadingFile;
 
 public class CombinedChapterBibleSourceZip extends BibleSource {
 
     private String BIBLE_DATA = GoBible.BIBLE_DATA_ROOT + "FinPR92";
     // data root location inside zip file
     private String DATAFILES_ROOT = "Bible Data/";
     String bibleLocation = DATAFILES_ROOT + "Index";
     private GoBible goBible;
     
     // Current chapter loaded
     private int currentBookIndex = -1;
     private int currentFileIndex = -1;
     private int currentChapterIndex = -1;
     // Bible index
     private int numberOfBooks;
     private String[] bookNames;
     /**
      * Short book names are for file names as they are ensured to be 7-bit
      * ASCII. *
      */
     private String[] shortBookNames;
     // The chapter number that the book starts with, usually 1 but may be
     // a larger chapter if the book has been split
     private int[] startChapters;
     private int[] numberOfChapters;
     // Contains the file number and number of verses for each chapter.
     // Second index is numberOfChapters * 4 with the first entry being
     // the file number, the second entry being the offset into the verse
     // data where the chapter starts, the third entry being the number of bytes
     // of the verse data and the third entry being for the
     // number of verses.
     private int[][] combinedChapterIndex;
     // Book index
     private int[][] bookIndex;
     private char[] fileData;
     // Current chapter data
     private char[] verseData;
     private int verseDataSize;
     // lookup data
     private short bookLookups[] = {};
     private int verseLookups[] = {};
     // file which holds the bible data
     private ZipFile zipFile;
 
     public CombinedChapterBibleSourceZip(GoBible goBible) throws IOException, TranslationNotFoundException {
         this(goBible, goBible.getTranslation());
         this.goBible = goBible;
     }
 
     public CombinedChapterBibleSourceZip(GoBible goBible, String translationRoot) throws IOException, TranslationNotFoundException {
         super(goBible);
 
        BIBLE_DATA = "file://" + goBible.bookUrl + goBible.getTranslation();
 
         System.out.println("[CombinedChapterBibleSource.const] BIBLE_DATA = " + BIBLE_DATA);
         System.out.println("[CombinedChapterBibleSource.const] biblelocation = " + bibleLocation);
 
         RandomReadingFile rrf;
         try {
             rrf = new RandomReadingFile(BIBLE_DATA);
         } catch (Exception e) {
             throw new TranslationNotFoundException(GoBible.getString("UI-Translation-Not-Found"));
         }
 
         zipFile = null;
 
         try {
             zipFile = new ZipFile(rrf);
         } catch (IOException ioEx) {
             throw new IOException("Err opening conn: " + ioEx.getMessage() + ", filepath: " + bibleLocation);
         }
 
         ZipEntry ze = zipFile.getEntry(bibleLocation);
 
         boolean error = false;
 
         // Read in the main index
         DataInputStream input = null;
         try {
             input = new DataInputStream(zipFile.getInputStream(ze));
         } catch (IOException ioEx) {
 
             if (input == null) {
                 throw new TranslationNotFoundException(GoBible.getString("UI-Translation-Not-Found"));
             }
             throw new IOException("Err opening stream: " + ioEx.getMessage()
                     + ", url: " + rrf.getURL());
         }
 
         if (!error) {
             // Read in the number of books
 
             numberOfBooks = input.read();
 
             bookNames = new String[numberOfBooks];
             shortBookNames = new String[numberOfBooks];
             startChapters = new int[numberOfBooks];
             numberOfChapters = new int[numberOfBooks];
             combinedChapterIndex = new int[numberOfBooks][];
 
             for (int bookIndex = 0; bookIndex < numberOfBooks; bookIndex++) {
                 // Read in the name of the book
                 bookNames[bookIndex] = input.readUTF();
 
                 // Read in the short book name
                 shortBookNames[bookIndex] = input.readUTF();
                 startChapters[bookIndex] = input.readUnsignedShort();
 
                 // Read in the number of chapters in this book
                 int numberOfChapters = input.readUnsignedShort();
                 this.numberOfChapters[bookIndex] = numberOfChapters;
                 combinedChapterIndex[bookIndex] = new int[numberOfChapters << 2];
 
                 int previousFileNumber = 0;
                 int verseDataOffset = 0;
 
                 // Read in the file number, verse offset, and number of verses for each chapter
                 for (int chapterIndex = 0; chapterIndex < numberOfChapters; chapterIndex++) {
                     int fileNumber = input.read();
                     int allVersesLength = input.readInt();
 
                     combinedChapterIndex[bookIndex][chapterIndex << 2] = fileNumber;
 
                     if (fileNumber != previousFileNumber) {
                         verseDataOffset = 0;
                         previousFileNumber = fileNumber;
                     }
 
                     combinedChapterIndex[bookIndex][(chapterIndex << 2) + 1] = verseDataOffset;
                     combinedChapterIndex[bookIndex][(chapterIndex << 2) + 2] = allVersesLength;
                     combinedChapterIndex[bookIndex][(chapterIndex << 2) + 3] = input.read();
 
                     verseDataOffset += allVersesLength;
                 }
             }
 
             input.close();
 
             // Read in the reference lookup map
             String referenceLocation = DATAFILES_ROOT + "Reference Lookup";
 
             ze = null;
             ze = zipFile.getEntry(referenceLocation);
 
             InputStream refLookup = zipFile.getInputStream(ze);
 
             if (refLookup != null) {
                 input = new DataInputStream(refLookup);
                 readReference(input);
             }
         }
 
     }
 
     private void readReference(DataInputStream dis) {
         // read in a section_entry
         char c1 = 0, c2 = 0;
         int position, length;
         int bytesRead = 0;
 
         while (true) { // loop through every entry
             try {
                 c1 = (char) dis.readByte();
                 c2 = (char) dis.readByte();
             } catch (IOException ioe) {
                 System.err.println("Error while reading section name, " + bytesRead + ", " + c1 + c2);
                 break;
             }
             bytesRead += 2;
 
             System.err.println("Read in section " + c1 + c2);
 
             if (c1 == 0 && c2 == 0) { // terminating entry encountered
                 break;
             } else {
                 try {
                     position = dis.readInt(); // 4 bytes
                     length = dis.readInt(); // 4 bytes
                     bytesRead += 8;
 
                     System.err.println("Length " + length);
                     if (length == 0) {
                         continue;
                     }
 
                     if (c1 == 'b' && c2 == 'k') { // this is the book lookup entry
                         readBookLookup(dis, length);
                     } else if (c1 == 'v' && c2 == 'e') { // this is the verse lookup entry
                         readVerseLookup(dis, length);
                     } else { // unknown, but ignore
                         dis.skip(length);
                     }
                 } catch (IOException ioe) {
                     System.err.println("Error while reading position, length and section data");
                     break;
                 }
             }
         }
     }
 
     private void readBookLookup(DataInputStream dis, int length) throws IOException {
         int bytesRead = 0;
         short entry;
         int n = 0;
 
         bookLookups = new short[length / 2 - 1];
 
         while (bytesRead < length) {
             entry = dis.readShort();
             bytesRead += 2;
 
             System.err.println("New Book Mapping: "
                     + ((entry >> 8) & 0xFF) + ", "
                     + ((entry >> 0) & 0xFF) + "");
 
             if (entry == (short) 0xFFFF) { // terminator
                 break;
             }
 
             bookLookups[n] = entry;
 
             n++;
         }
 
         // now, sort list
         GBCToolkit.quicksort(bookLookups, 0, bookLookups.length);
 
         if (bytesRead == length) {
             return;
         }
 
         if (bytesRead < length) {
             dis.skip(length - bytesRead);
 
             // TODO: fill the rest with 0xFF entries
         }
 
         System.err.println("ASSERT (bytesRead == length) FALSE! Incorrect file format");
     }
 
     private void readVerseLookup(DataInputStream dis, int length) throws IOException {
         int bytesRead = 0;
         int entry;
         int n = 0;
 
         verseLookups = new int[length / 4 - 1];
 
         while (bytesRead < length) {
             entry = dis.readInt();
             bytesRead += 4;
 
             if (entry == -1) { // terminator
                 break;
             }
 
             verseLookups[n] = entry;
             n++;
         }
 
         GBCToolkit.quicksort(verseLookups, 0, verseLookups.length);
 
         if (bytesRead == length) {
             return;
         }
 
         if (bytesRead < length) {
             dis.skip(length - bytesRead);
         }
 
         System.err.println("ASSERT (bytesRead == length) FALSE! Incorrect file format");
     }
 
     public int getBookIdFromNumber(int bookNumber) {
         /**
          * TODO: change to some binary search
          */
         for (int i = 0; i < bookLookups.length; i++) {
             if ((bookLookups[i] & 0xFF) == bookNumber) {
                 return bookLookups[i] >>> 8;
             }
         }
         return bookNumber;
     }
 
     public int getBookNumberFromIndex(int bookIndex) {
         /**
          * TODO: change to some binary search
          */
         for (int i = 0; i < bookLookups.length; i++) {
             if ((bookLookups[i] >>> 8) == bookIndex) { // upper 8 bits match
                 return bookLookups[i] & 0xFF; // return lower 8 bits
             }
         }
         return bookIndex;
     }
 
     public int getVerseNumberFromIndex(int bookIndex, int chapterIndex, int verseIndex) {
         /**
          * TODO: change to some binary search
          */
         System.err.println("Requested number of index " + bookIndex + " " + chapterIndex + " " + verseIndex + " ");
 
         int reference = ((0xFF & bookIndex) << 16) | ((0xFF & chapterIndex) << 8) | (verseIndex & 0xFF);
 
         for (int i = 0; i < verseLookups.length; i++) {
             if ((verseLookups[i] >>> 8) == reference) { // upper 24 bits match
                 System.err.println("Found " + (verseLookups[i] & 0xFF));
                 return verseLookups[i] & 0xFF; // return lower 8 bits
             }
         }
         return verseIndex;
     }
 
     public int getVerseIndexFromNumber(int bookIndex, int chapterIndex, int verseNumber) {
         /**
          * TODO: change to some binary search
          */
         int reference = ((0xFF & bookIndex) << 24) | ((0xFF & chapterIndex) << 16) | (verseNumber & 0xFF);
 
 //        System.err.println("Requested index of number " + bookIndex + " " + chapterIndex+ " " +verseNumber + " ");
 
         for (int i = 0; i < verseLookups.length; i++) {
             if ((verseLookups[i] & 0xFFFF00FF) == reference) { // upper 1st, 2nd and 4th bytes match
                 System.err.println("Found " + ((verseLookups[i] >>> 8) & 0xFF));
                 return (verseLookups[i] >>> 8) & 0xFF; // return lower 8 bits
             }
         }
         return verseNumber;
     }
 
     public char[] getChapter(int bookIndex, int chapterIndex) throws IOException {
         // Load the chapter if it isn't loaded
         loadChapter(bookIndex, chapterIndex);
 
         return verseData;
     }
 
     public char[] getVerse(int bookIndex, int chapterIndex, int verseNumber) throws IOException {
         getChapter(bookIndex, chapterIndex);
 
         int verseIndex = getVerseIndexFromNumber(bookIndex, chapterIndex, verseNumber);
 
         // Load some info for requested verse
         int verseSize = 50; //getVerseIndexFromNumber(bookIndex, chapterIndex, verseNumber+1) - verseIndex;
         System.out.println("Verse size: " + verseSize + ", verse index: " + verseIndex);
 
         char[] verse = new char[verseSize];
 
         // Copy requested verse data out of verseData, change chapter if needed
         System.arraycopy(verseData, verseIndex, verse, 0, verseSize);
         System.err.println(verse);
         return verse;
     }
 
     public int[] getChapterIndex(int bookIndex, int chapterIndex) throws IOException {
         // Load the chapter if it isn't loaded
         loadChapter(bookIndex, chapterIndex);
 
         return this.bookIndex[chapterIndex];
     }
 
     private void loadChapter(int bookIndex, int chapterIndex) throws IOException {
         // If the chapter isn't loaded then load it
         if (currentBookIndex != bookIndex || currentChapterIndex != chapterIndex) {
             // If the book or file index have changed then load the new file
             if (currentBookIndex != bookIndex || combinedChapterIndex[bookIndex][chapterIndex << 2] != currentFileIndex) {
                 // If the book is different then we need to load its index
                 if (currentBookIndex != bookIndex) {
                     loadBookIndex(bookIndex);
                 }
                 System.err.println("[CombinedChapterBibleSource.loadChapter(" + bookIndex + ", " + chapterIndex + ")");
                 currentFileIndex = combinedChapterIndex[bookIndex][chapterIndex << 2];
 
                 // Load the chapter as it will be different if either the chapter or book changed
                 //start = System.currentTimeMillis();
 //                FileConnection con = (FileConnection) Connector.open(FILE_SEPARATOR)
 
                 String currentString = DATAFILES_ROOT + shortBookNames[bookIndex] + "/" + shortBookNames[bookIndex] + " " + currentFileIndex;
 
                 ZipEntry ze = zipFile.getEntry(currentString);
 
                 // Read in the main index
                 DataInputStream input = new DataInputStream(zipFile.getInputStream(ze));
 
                 int length = input.readInt();
 
                 byte[] byteArray = new byte[length];
 
                 input.readFully(byteArray, 0, length);
 
                 input.close();
 
                 // Do our own UTF-8 conversion
                 fileData = new char[length];
 
                 char currentChar = 0;
                 int charIndex = 0;
 
                 for (int i = 0; i < length; i++) {
                     byte currentByte = byteArray[i];
 
                     // If ASCII character then simply copy in
                     if ((currentByte & 0x80) == 0) {
                         fileData[charIndex] = (char) (currentByte & 0xff);
                     } else if ((currentByte & 0xe0) == 0xc0) // 2 byte sequence
                     {
                         fileData[charIndex] = (char) (((currentByte & 0x1f) << 6) | (byteArray[++i] & 0x3f));
                     } else if ((currentByte & 0xf0) == 0xe0) // 3 byte sequence
                     {
                         char c = (char) (((currentByte & 0x0f) << 12) | ((byteArray[++i] & 0x3f) << 6));
                         fileData[charIndex] = (char) (c | (byteArray[++i] & 0x3f));
                     } else if ((currentByte & 0xf8) == 0xf0) // 4 bytes sequence
                     {
                         // This was the old code. It correctly mapped from UTF8 to UTF32, but we can't
                         // process 32 bit characters. It needs to be broken up into a surrogate pair.
                         // That's what the new code below is doing. -dlh
                         // char c = (char) (((currentByte & 0x07) << 18) | ((byteArray[++i] & 0x3f) << 12));
                         // c |= (char) ((byteArray[++i] & 0x3f) << 6);
                         // fileData[charIndex] = (char) (c | (byteArray[++i] & 0x3f));
                         int codePoint = (((currentByte & 0x07) << 18) | ((byteArray[++i] & 0x3f) << 12));
                         codePoint |= ((byteArray[++i] & 0x3f) << 6) | (byteArray[++i] & 0x3f);
                         // if it's a valid 16 bit code point, just put it out
                         if (codePoint < 0x10000) {
                             fileData[charIndex] = (char) codePoint;
                         } else if (codePoint <= 0x10FFFF)// it needs to be a surrogate pair
                         {
                             int high = (((codePoint - 0x00010000) >> 10) + 0xd800);
                             int low = (((codePoint - 0x00010000) & 0x3ff) + 0xdc00);
                             fileData[charIndex] = (char) high;
                             fileData[++charIndex] = (char) low;
                         } else // >= 0x10FFFF, put out the standard REPLACEMENT CHARACTER
                         {
                             fileData[charIndex] = '\uFFDF';
                         }
                     } else if ((currentByte & 0xff) == 0xf8) // 5 byte sequence
                     {
                         // This is invalid also (5 bytes)
 //						char c = (char) (((currentByte & 0x03) << 24) | ((byteArray[++i] & 0x3f) << 18));
 //						c |= (char) ((byteArray[++i] & 0x3f) << 12);
 //						c |= (char) ((byteArray[++i] & 0x3f) << 6);
 //						fileData[charIndex] = (char) (c | (byteArray[++i] & 0x3f));
                         int codePoint = (((currentByte & 0x03) << 24) | ((byteArray[++i] & 0x3f) << 18));
                         codePoint |= ((byteArray[++i] & 0x3f) << 12) | ((byteArray[++i] & 0x3f) << 6);
                         codePoint |= (byteArray[++i] & 0x3f);
                         // if it's a valid 16 bit code point, just put it out
                         if (codePoint < 0x10000) {
                             fileData[charIndex] = (char) codePoint;
                         } else if (codePoint <= 0x10FFFF)// it needs to be a surrogate pair
                         {
                             int high = (((codePoint - 0x00010000) >> 10) + 0xd800);
                             int low = (((codePoint - 0x00010000) & 0x3ff) + 0xdc00);
                             fileData[charIndex] = (char) high;
                             fileData[++charIndex] = (char) low;
                         } else // >= 0x10FFFF, put out the standard REPLACEMENT CHARACTER
                         {
                             fileData[charIndex] = '\uFFDF';
                         }
                     }
 
                     charIndex++;
                 }
 
                 //fileData = input.readUTF().toCharArray();
 
                 //goBible.loadChapterTime = System.currentTimeMillis() - start;
             }
 
             currentChapterIndex = chapterIndex;
 
             // Load the chapter's verse data from the fileData
             verseDataSize = combinedChapterIndex[bookIndex][(chapterIndex << 2) + 2];
             verseData = new char[verseDataSize];
 
             // Copy verse data out of fileData
             System.arraycopy(fileData, combinedChapterIndex[bookIndex][(chapterIndex << 2) + 1], verseData, 0, verseDataSize);
         }
     }
 
     private void loadBookIndex(int bookIndex) throws IOException {
         int numberOfChapters = this.numberOfChapters[bookIndex];
         this.bookIndex = new int[numberOfChapters][];
 
         currentBookIndex = bookIndex;
 
         String booksIndex = DATAFILES_ROOT + shortBookNames[bookIndex] + "/Index";
         ZipEntry ze = zipFile.getEntry(booksIndex);
 
         // Read in the book index
         DataInputStream input = new DataInputStream(zipFile.getInputStream(ze));
 
         // Read each verse length in for each chapter
         for (int chapter = 0; chapter < numberOfChapters; chapter++) {
             // There are two verse entries (offset, length) for every verse
             int numberOfVerses = 2 * combinedChapterIndex[bookIndex][(chapter << 2) + 3];
             int[] chapterIndex = new int[numberOfVerses];
             this.bookIndex[chapter] = chapterIndex;
 
             int offset = 0;
 
             // Read in each verse length
             for (int verse = 0; verse < numberOfVerses;) {
                 chapterIndex[verse++] = offset;
                 offset += input.readUnsignedShort();
                 chapterIndex[verse++] = offset;
             }
         }
 
         input.close();
     }
 
     public String[] getBookNames() {
         return bookNames;
     }
 
     public String getBookName(int bookIndex) {
         return bookNames[bookIndex];
     }
 
     public int getNumberOfBooks() {
         return numberOfBooks;
     }
 
     public int getStartChapter(int bookIndex) {
         return startChapters[bookIndex];
     }
 
     public int getNumberOfChapters(int bookIndex) {
         return numberOfChapters[bookIndex];
     }
 
     public int getNumberOfVerses(int bookIndex, int chapterIndex) {
         return combinedChapterIndex[bookIndex][(chapterIndex << 2) + 3];
     }
 
     public int getVerseDataSize() {
         return verseDataSize;
     }
 }
