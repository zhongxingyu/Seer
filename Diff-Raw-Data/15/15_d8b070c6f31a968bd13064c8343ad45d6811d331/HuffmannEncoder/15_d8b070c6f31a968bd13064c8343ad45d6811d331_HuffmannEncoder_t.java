 package edu.x3m.kas.core;
 
 
 import edu.x3m.kas.core.structures.AbstractNode;
 import edu.x3m.kas.core.structures.GroupNode;
 import edu.x3m.kas.core.structures.SimpleNode;
 import edu.x3m.kas.io.HuffmannInputStream;
 import edu.x3m.kas.io.HuffmannOutputStream;
 import edu.x3m.kas.utils.BinaryUtil;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.PriorityQueue;
 
 
 /**
  * @author Hans
  */
 public class HuffmannEncoder {
 
     public static final String PREQUEL = "x3m-huff";
     
 
     protected final SimpleNode[] ABC = new SimpleNode[256];
     protected final File sourceFile;
     protected final File destFile;
     //
     protected HuffmannInputStream his;
     protected HuffmannOutputStream hos;
 
 
 
     public HuffmannEncoder (File sourceFile, File destFile) {
         this.sourceFile = sourceFile;
         this.destFile = destFile;
     }
 
 
 
     public HuffmannEncoder (File sourceFile) {
         this (sourceFile, new File (sourceFile.getPath () + ".x3m.huff"));
     }
 
 
 
     public void encode () throws FileNotFoundException, IOException {
         readFileAndCreateFreqs ();
        endCodes ();
         createBinaryTree ();
         readAndWrite ();
     }
 
 
 
     public void printAlphabet () {
         SimpleNode node;
         for (int i = 0; i < ABC.length; i++) {
             node = ABC[i];
             if (node != null)
                 System.out.println ((char) node.character + " = " + node.getCode ().reverse ().toString ());
 
         }
     }
 
 
 
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
 
 
 
     private void createBinaryTree () {
         PriorityQueue<AbstractNode> items = new PriorityQueue<> ();
         int mergesLeft;
 
         //# pushing only used items to array
         for (int i = 0; i < ABC.length; i++) {
             if (ABC[i] == null) continue;
             items.add (ABC[i]);
         }
 
         //System.out.println (items);
 
         //# defining how many steps it'll take to created weighted tree
         mergesLeft = items.size () - 1;
 
 
         //# no tree, so it's easy
         if (mergesLeft == 0) {
             items.poll ().append ('1');
         } else {
 
             //# removing two last items, and add new one
             while (mergesLeft-- > 0)
                 items.add (new GroupNode (items.poll (), items.poll ()));
         }
     }
 
 
 
     private void readAndWrite () throws FileNotFoundException, IOException {
         hos = new HuffmannOutputStream (destFile);
         hos.writePrequel ();
         hos.writeAlphabet (ABC);
         writeData ();
     }
 
 
 
     private void writeData () throws FileNotFoundException, IOException {
 
         his = new HuffmannInputStream (sourceFile);
 
         String reminder = "", codeSequence;
         StringBuilder tmp;
         byte[] buffer;
         byte[] bytesToWrite;
         int ch, size;
 
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
         }
 
         if (!reminder.isEmpty ()) {
             hos.writeLastByte (reminder);
         }
         his.close ();
         hos.close ();
     }



    private void endCodes () {
        for (int i = 0; i < ABC.length; i++) {
            SimpleNode node = ABC[i];
            if (node != null) node.endCode ();
            
        }
    }
 }
