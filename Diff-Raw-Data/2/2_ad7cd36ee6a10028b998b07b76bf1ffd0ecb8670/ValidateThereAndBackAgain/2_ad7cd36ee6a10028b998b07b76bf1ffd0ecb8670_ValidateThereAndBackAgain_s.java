 /*
 * ExampleThereAndBackAgain.java
  *
  * Copyright (c) 2008-2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.converter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import junit.framework.TestCase;
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 import quill.textbase.DataLayer;
 
 /**
  * <p>
  * I was watching <i>The Fellowship of the Ring</i> as I started writing this
  * test.
  * 
  * @author Andrew Cowie
  */
 public class ValidateThereAndBackAgain extends TestCase
 {
     public void testRoundTrip() throws IOException, ValidityException, ParsingException {
         final DataLayer data;
         final File source, target;
         final FileOutputStream out;
         final String msg;
         final String sum1, sum2;
 
         source = new File("tests/ExampleProgram.xml");
         assertTrue(source.exists());
 
         data = new DataLayer();
         data.loadDocument(source.getPath());
 
         target = new File("tmp/unittests/quill/converter/ValidateThereAndBackAgain.xml");
         target.getParentFile().mkdirs();
         out = new FileOutputStream(target);
         data.saveDocument(out);
 
         /*
          * Now run an hashing algorithm over both files to figure out if
          * they're different.
          */
 
         sum1 = hash(source);
         sum2 = hash(target);
 
         msg = "\nLoading example source file\n" + source + "\nand round-tripping through to target\n"
                 + target + "\nresulted in different output; hashes\n" + sum1 + " and\n" + sum2;
 
         assertTrue(msg, sum1.equals(sum2));
     }
 
     public static void main(String[] args) throws IOException, ValidityException, ParsingException {
         final DataLayer data;
         int i;
 
         data = new DataLayer();
         data.loadDocument("tests/ExampleProgram.xml");
 
         for (i = 1; i <= 70; i++) {
             System.err.print(i / 10);
         }
         System.err.println();
         for (i = 1; i <= 70; i++) {
             System.err.print(i % 10);
         }
         System.err.println("\n");
         System.err.flush();
 
         data.saveDocument(System.out);
     }
 
     /**
      * Take the md5 sum of the given file.
      */
     /*
      * Output conversion code from http://www.spiration.co.uk/post/1199
      */
     static String hash(File file) throws FileNotFoundException, IOException {
         final MessageDigest md;
         final FileInputStream in;
         final StringWriter str;
         final PrintWriter out;
         byte buf[];
 
         try {
             md = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException nsae) {
             throw new Error("How can there be no md5 algorithm?");
         }
         md.reset();
 
         buf = new byte[1024];
 
         in = new FileInputStream(file);
         while (in.read(buf) != -1) {
             md.update(buf);
         }
         in.close();
 
         str = new StringWriter();
         out = new PrintWriter(str);
 
         for (byte b : md.digest()) {
             out.printf("%02x", 0xFF & b);
         }
         return str.toString();
     }
 }
