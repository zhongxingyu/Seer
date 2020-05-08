 package edu.x3m.kas.core;
 
 
 import edu.x3m.kas.core.iface.IProgressable;
 import edu.x3m.kas.core.structures.GroupNode;
 import edu.x3m.kas.core.structures.SimpleNode;
 import edu.x3m.kas.io.HuffmannBinaryInputStream;
 import edu.x3m.kas.io.HuffmannOutputStream;
 import edu.x3m.kas.monitors.IHuffmannMonitor;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 
 /**
  * Class for decoding encoded file which used Huffmann's algorithm
  *
  * @author Hans
  */
 public class HuffmannDecoder implements IProgressable {
 
 
     public static final String ALPHABET_CREATION = "Alphabet creation";
     public static final String TREE_CREATION = "Tree creation";
     public static final String DECODING = "Decoding";
     //
     protected SimpleNode[] ABC;
     protected final File sourceFile;
     protected final File destFile;
     //
     protected HuffmannBinaryInputStream his;
     protected HuffmannOutputStream hos;
     private GroupNode root;
     private ParseResult parseResult;
     private IHuffmannMonitor huffmannMonitor;
 
 
 
     /**
      * Creates HuffmannDecoder which decodes given file into given file;
      *
      * @param sourceFile source file
      * @param destFile   destination file
      */
     public HuffmannDecoder (File sourceFile, File destFile) {
         this.sourceFile = sourceFile;
         this.destFile = destFile;
     }
 
 
 
     /**
      * Creates HuffmannDecoder which decodes given file into given file + "x3m.huff" file
      *
      * @param sourceFile source file
      */
     public HuffmannDecoder (File sourceFile) {
         this (sourceFile, new File (sourceFile.getPath () + ".x3m.huff"));
     }
 
 
 
     /**
      * Method decodes file specified in constructor into file specified in constructor.
      *
      * @throws UnsupportedFileException when file is not supported
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void decode () throws FileNotFoundException, IOException, UnsupportedFileException {
         his = new HuffmannBinaryInputStream (sourceFile);
         if (!his.isSupported ())
             throw new UnsupportedFileException ();
         hos = new HuffmannOutputStream (destFile);
 
 
         //# reading alphabet
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (ALPHABET_CREATION);
         ABC = his.readAlphabet ();
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (ALPHABET_CREATION);
 
 
         //# creating binary tree
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (TREE_CREATION);
         root = Huffmann.createBinaryTree (ABC);
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (TREE_CREATION);
 
 
         //# decoding
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (DECODING);
         readAndWrite ();
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (DECODING);
 
     }
 
 
 
     private void readAndWrite () throws IOException {
         parseResult = new ParseResult (0, 0);
         byte[] buffer;
         int reminder;
         int validBits;
         byte[] result;
         int bytesLoaded = 0;
         int prcCur, prcPrev = 0;
 
 
         while ((buffer = his.readNextBinaryBuffer ()) != null) {
 
             //# getting valid bits count
             if (his.hasNext ()) validBits = buffer.length;
             else validBits = buffer.length - 8 - (8 - his.getValidBitsInLastByte ());
 
             //# creates needed data holders and searching for prefixes
             result = new byte[buffer.length];
             parseBytes (buffer, result, validBits);
             reminder = parseResult.reminderLength;
             his.setReminderLength (reminder);
 
             //# writing to out file
             hos.write (result, 0, parseResult.validBytes);
 
 
             //# monitoring
             if (huffmannMonitor != null) {
                 bytesLoaded += buffer.length / 8;
                prcCur = (int) (((double) bytesLoaded / his.bytesTotal) * 100);
                 if (prcCur != prcPrev)
                     huffmannMonitor.onSectionProgress (DECODING, prcPrev = prcCur);
             }
         }
 
         hos.close ();
     }
 
     //--------------------------------------
     //# Privates
     //--------------------------------------
 
 
     /**
      * Prefix search.
      *
      * @param binary    source array
      * @param result    final array
      * @param validBits number of valid bites
      */
     private void parseBytes (byte[] binary, byte[] result, int validBits) {
         int from = 0;
         int validBytes = 0;
         SimpleNode node;
 
 
         if (validBits != binary.length) {
             byte[] tmp = new byte[validBits];
             System.arraycopy (binary, 0, tmp, 0, validBits);
             binary = tmp;
         }
 
         while ((node = root.find (binary, from, 0)) != null) {
             from += node.codeLength;
             result[validBytes++] = (byte) node.character;
         }
         parseResult.reminderLength = (binary.length - from);
         parseResult.validBytes = validBytes;
     }
 
 
 
     @Override
     public void setHuffmannMonitor (IHuffmannMonitor huffmannMonitor) {
         this.huffmannMonitor = huffmannMonitor;
     }
 
 
 
     @Override
     public IHuffmannMonitor getHuffmannMonitor () {
         return huffmannMonitor;
     }
 
 
     class ParseResult {
 
 
         public int reminderLength, validBytes;
 
 
 
         public ParseResult (int reminderLength, int validBytes) {
             this.reminderLength = reminderLength;
             this.validBytes = validBytes;
         }
     }
 }
