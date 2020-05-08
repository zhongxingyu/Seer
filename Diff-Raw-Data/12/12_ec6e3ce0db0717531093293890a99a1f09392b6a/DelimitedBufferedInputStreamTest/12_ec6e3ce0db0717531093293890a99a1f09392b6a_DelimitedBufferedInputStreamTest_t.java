 /**
  * 
  */
 package org.melati.util.test;
 
 import java.io.ByteArrayInputStream;
 
 import org.melati.util.DelimitedBufferedInputStream;
 
 import junit.framework.TestCase;
 
 /**
  * @author timp
  * @since 28 Jan 2008
  *
  */
 public class DelimitedBufferedInputStreamTest extends TestCase {
 
   byte[] sink = new byte[10];
   byte[] arr1 = {30, 31, 32, 33};
   byte[] arr2 = {32, 33};
   byte[] arr3 = {32, 34, 33};
   byte[] arr4 = {32, 33, 34};
 
   /**
    * @param name
    */
   public DelimitedBufferedInputStreamTest(String name) {
     super(name);
   }
 
   /** 
    * {@inheritDoc}
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /** 
    * {@inheritDoc}
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Test method for {@link org.melati.util.DelimitedBufferedInputStream#DelimitedBufferedInputStream(java.io.InputStream)}.
    */
  public void testDelimitedBufferedInputStreamInputStream() throws Exception {
     DelimitedBufferedInputStream testDBIS =
       new DelimitedBufferedInputStream(
               new ByteArrayInputStream(arr1));
     assertTrue("arr2 in arr1 (expect 2): " ,
             testDBIS.indexOf(arr1, arr2, 0) == 2);
     assertTrue("potentialMatch (expect -1): " ,
             testDBIS.getPotentialMatch() == -1);
     assertTrue("arr3 in arr1 (expect -1): " ,
             testDBIS.indexOf(arr1, arr3, 0) == -1);
     assertTrue("potentialMatch (expect -1): " ,
             testDBIS.getPotentialMatch() == -1);
     assertTrue("arr4 in arr1 (expect -1): ",
             testDBIS.indexOf(arr1, arr4, 0) == -1);
     assertTrue("potentialMatch (expect 2): ",
             testDBIS.getPotentialMatch() == 2);
    testDBIS.close();
   }
 
   /**
    * Test method for {@link org.melati.util.DelimitedBufferedInputStream#DelimitedBufferedInputStream(java.io.InputStream, int)}.
    */
   public void testDelimitedBufferedInputStreamInputStreamInt() throws Exception {
     DelimitedBufferedInputStream testDBIS =
         new DelimitedBufferedInputStream(
             new ByteArrayInputStream(arr1), 1);
      assertTrue("reading upto arr3 (expect 4): " ,
              testDBIS.readToDelimiter(sink, 0, 10, arr3) == 4);
     testDBIS.close();
   }
 
   /**
    * Test method for {@link org.melati.util.DelimitedBufferedInputStream#readToDelimiter(byte[], int, int, byte[])}.
    */
   public void testReadToDelimiter() throws Exception {
     DelimitedBufferedInputStream testDBIS =
       new DelimitedBufferedInputStream(
               new ByteArrayInputStream(arr1));
     assertTrue("reading upto arr2 (expect 2): ",
             testDBIS.readToDelimiter(sink, 0, 10, arr2) == 2);
     assertTrue("reading upto arr2 again (expect 0): " + 
             testDBIS.readToDelimiter(sink, 0, 10, arr2),
             testDBIS.readToDelimiter(sink, 0, 10, arr2) == 0);
     testDBIS = new DelimitedBufferedInputStream(
             new ByteArrayInputStream(arr1), 1);
     assertTrue("reading upto arr4 (expect 4): " , 
             testDBIS.readToDelimiter(sink, 0, 10, arr4) == 4);
     testDBIS = new DelimitedBufferedInputStream(
             new ByteArrayInputStream(arr1), 1);
     assertTrue("reading upto arr4 (expect 3): ", 
             testDBIS.readToDelimiter(sink, 0, 3, arr4) == 3);
     assertTrue("buf.length is now (expect 3): ", 
             testDBIS.getBufferLength() == 3);
 
   }
 
   /**
    * Test method for {@link org.melati.util.DelimitedBufferedInputStream#indexOf(byte[], byte[], int)}.
    */
   public void testIndexOfByteArrayByteArrayInt() {
   }
 
   /**
    * Test method for {@link org.melati.util.DelimitedBufferedInputStream#indexOf(byte[], byte[], int, int)}.
    */
   public void testIndexOfByteArrayByteArrayIntInt() {
   }
 
 }
