 package edu.x3m.kas.core;
 
 
 import edu.x3m.kas.core.iface.IProgressable;
 import edu.x3m.kas.core.structures.SimpleNode;
 import edu.x3m.kas.io.HuffmannBinaryOutputStream;
 import edu.x3m.kas.io.HuffmannInputStream;
 import edu.x3m.kas.monitors.IHuffmannMonitor;
 import edu.x3m.kas.utils.BinaryUtil;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 
 /**
  * Class for encoding files using Huffmann's algorithm
  *
  * @author Hans
  */
 public class HuffmannEncoder implements IProgressable {
 
 
     public static final String ALPHABET_CREATION = "Alphabet creation";
     public static final String TREE_CREATION = "Tree creation";
     public static final String ENCODING = "Encoding";
     //
     public static final String PREQUEL = "x3m-huff";
     protected final SimpleNode[] ABC = new SimpleNode[256];
     protected final File sourceFile;
     protected final File destFile;
     //
     protected HuffmannInputStream his;
     protected HuffmannBinaryOutputStream hos;
     protected IHuffmannMonitor huffmannMonitor;
 
 
 
     /**
      * Creates HuffmannEncoder which encodes given file into given file;
      *
      * @param sourceFile source file
      * @param destFile   destination file
      */
     public HuffmannEncoder (File sourceFile, File destFile) {
         this.sourceFile = sourceFile;
         this.destFile = destFile;
     }
 
 
 
     /**
      * Creates HuffmannEncoder which encodes given file into given file + "x3m.huff" file
      *
      * @param sourceFile source file
      */
     public HuffmannEncoder (File sourceFile) {
         this (sourceFile, new File (sourceFile.getPath () + ".x3m.huff"));
     }
 
 
 
     /**
      * Method encodes file specified in constructor into file specified in constructor.
      *
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void encode () throws FileNotFoundException, IOException {
         //# reading file and creating alphabet
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (ALPHABET_CREATION);
         readFileAndCreateFreqs ();
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (ALPHABET_CREATION);
 
 
         //# creating binary tree
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (TREE_CREATION);
         Huffmann.createBinaryTree (ABC);
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (TREE_CREATION);
 
 
         //# encoding
         if (huffmannMonitor != null) huffmannMonitor.onSectionStart (ENCODING);
         readAndWrite ();
         if (huffmannMonitor != null) huffmannMonitor.onSectionEnd (ENCODING);
     }
     //--------------------------------------
     //# Privates
     //--------------------------------------
 
 
 
     private void readFileAndCreateFreqs () throws FileNotFoundException, IOException {
         his = new HuffmannInputStream (sourceFile);
         byte[] buffer;
         int ch;
 
         while ((buffer = his.getNextNormalBuffer ()) != null) {
             for (int i = 0; i < buffer.length; i++) {
                 //# conversion
                 ch = (int) (buffer[i] + 256) % 256;
 
                 if (ABC[ch] == null) ABC[ch] = new SimpleNode (ch);
                 else ABC[ch].inc ();
             }
         }
         his.close ();
     }
 
 
 
     private void readAndWrite () throws FileNotFoundException, IOException {
         hos = new HuffmannBinaryOutputStream (destFile);
 
         //# writing header
         hos.writePrequel ();
         hos.writeAlphabet (ABC);
 
         //#writing data
         writeData ();
     }
 
 
 
     private void writeData () throws FileNotFoundException, IOException {
 
         his = new HuffmannInputStream (sourceFile);
 
         String reminder = "", codeSequence;
         StringBuilder tmp;
         byte[] buffer;
         byte[] bytesToWrite;
         int ch, size;
         int bytesLoaded = 0;
        int prcCur, prcPrev = 0;
 
         while ((buffer = his.getNextNormalBuffer ()) != null) {
 
             //# add previous rest
             tmp = new StringBuilder (reminder);
 
             //# joining codes
             for (int i = 0; i < buffer.length; i++) {
                 //# conversion
                 ch = (int) (buffer[i] + 256) % 256;
                 tmp.append (ABC[ch].finalCode);
             }
 
             size = tmp.length () / 8;
             reminder = tmp.substring (size * 8, tmp.length ());
 
 
             codeSequence = new String (tmp).substring (0, size * 8);
 
             bytesToWrite = BinaryUtil.convert10ToInt (codeSequence.getBytes ());
             hos.write (bytesToWrite);
 
 
             //# monitoring
             if (huffmannMonitor != null) {
                 bytesLoaded += buffer.length;
                prcCur = (100 * bytesLoaded / (int) his.bytesTotal);
                 if (prcCur != prcPrev)
                    huffmannMonitor.onSectionProgress (ENCODING, prcPrev = prcCur);
             }
         }
 
         if (!reminder.isEmpty ())
             hos.writeLastByte (reminder);
 
 
         hos.writeByte (reminder.length ());
         his.close ();
         hos.close ();
     }
 
 
 
     @Override
     public void setHuffmannMonitor (IHuffmannMonitor huffmannMonitor) {
         this.huffmannMonitor = huffmannMonitor;
     }
 
 
 
     @Override
     public IHuffmannMonitor getHuffmannMonitor () {
         return huffmannMonitor;
     }
 }
