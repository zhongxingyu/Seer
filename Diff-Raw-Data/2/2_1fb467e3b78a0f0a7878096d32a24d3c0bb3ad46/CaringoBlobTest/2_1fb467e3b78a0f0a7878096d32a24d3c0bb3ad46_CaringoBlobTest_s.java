 package edu.illinois.medusa;
 
 import org.akubraproject.Blob;
 import org.akubraproject.DuplicateBlobException;
 import org.akubraproject.MissingBlobException;
 import org.testng.Assert;
 import org.testng.annotations.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: hading
  * Date: 7/19/11
  * Time: 11:00 AM
  * To change this template use File | Settings | File Templates.
  */
 public class CaringoBlobTest {
 
     private CaringoBlobStore store;
     private CaringoBlobStoreConnection connection;
 
     @BeforeClass
     private void getStore() {
         store = CaringoBlobStoreTest.newStore();
     }
 
    private void getConnection() {
         connection = store.openConnection();
     }
 
     private URI testBlobName() {
         return URI.create("test-blob");
     }
 
     private byte[] getTestBytes() {
         return "Test string".getBytes();
     }
 
     private void writeTestBlob(byte[] testBytes) throws Exception {
         writeBlob(testBytes, getTestBlob());
     }
 
     private void writeBlob(byte[] testBytes, Blob blob) throws Exception {
         if (testBytes == null)
             testBytes = getTestBytes();
         OutputStream out = blob.openOutputStream(100, true);
         out.write(testBytes);
         out.close();
     }
 
     private void deleteTestBlob() {
         deleteBlob(getTestBlob());
     }
 
     private void deleteBlob(Blob blob) {
         try {
             blob.delete();
         } catch (IOException e) {
         }
     }
 
     private CaringoBlob getTestBlob() {
         return connection.getBlob(testBlobName(), null);
     }
 
     @Test
     public void testWriteAndExistsAndDelete() throws Exception {
         try {
             getConnection();
             writeTestBlob(getTestBytes());
             CaringoBlob blob = getTestBlob();
             Assert.assertTrue(blob.exists());
             deleteTestBlob();
             Assert.assertFalse(blob.exists());
         } finally {
             deleteTestBlob();
         }
     }
 
     @Test
     //This operation should do nothing, but should still succeed and not throw an Exception
     public void testDeleteMissingBlob() throws Exception {
         getConnection();
         boolean succeeded;
         try {
             connection.getBlob(URI.create("never-created-blob"), null).delete();
             succeeded = true;
         } catch (IOException e) {
             succeeded = false;
         }
         Assert.assertTrue(succeeded);
     }
 
     @Test
     public void testNeverExisted() throws Exception {
         getConnection();
         Assert.assertFalse(connection.getBlob(URI.create("never-created-blob"), null).exists());
     }
 
     @Test
     public void testSize() throws Exception {
         try {
             getConnection();
             writeTestBlob(getTestBytes());
             CaringoBlob blob = getTestBlob();
             Assert.assertEquals(getTestBytes().length, blob.getSize());
         } finally {
             deleteTestBlob();
         }
     }
 
     @Test
     public void testSizeMissingBlob() throws Exception {
         getConnection();
         CaringoBlob blob = connection.getBlob(URI.create("never-created-blob"), null);
         boolean succeeded;
         try {
             blob.getSize();
             succeeded = true;
         } catch (MissingBlobException e) {
             succeeded = false;
         }
         Assert.assertFalse(succeeded);
     }
 
     @Test
     public void testBlobInputStreamContents() throws Exception {
         try {
             getConnection();
             byte[] bytes = "My test string".getBytes();
             writeTestBlob(bytes);
             Blob blob = getTestBlob();
             InputStream input = blob.openInputStream();
             byte[] new_bytes = new byte[1024];
             int read = input.read(new_bytes);
             Assert.assertEquals(bytes.length, read);
             for (int i = 0; i < read; i++) {
                 Assert.assertEquals(bytes[i], new_bytes[i]);
             }
             input.close();
         } finally {
             deleteTestBlob();
         }
     }
 
     @Test
     public void testMoveBlob() throws Exception {
         getConnection();
         writeTestBlob(null);
         Blob blob = getTestBlob();
         Blob new_blob = connection.getBlob(URI.create("moved-blob"), null);
         try {
             Assert.assertTrue(blob.exists());
             Assert.assertFalse(new_blob.exists());
             blob.moveTo(new_blob.getId(), null);
             Assert.assertFalse(blob.exists());
             Assert.assertTrue(new_blob.exists());
         } finally {
             deleteBlob(new_blob);
             deleteTestBlob();
         }
     }
 
     @Test
     public void testMoveMissingBlob() throws Exception {
         getConnection();
         Blob blob = connection.getBlob(URI.create("never-created-blob"), null);
         boolean succeeded;
         try {
             blob.moveTo(URI.create("target-blob"), null);
             succeeded = true;
             connection.getBlob(URI.create("target-blob"), null).delete();
         } catch (MissingBlobException e) {
             succeeded = false;
         }
         Assert.assertFalse(succeeded);
     }
 
     @Test
     public void testMoveBlobMustSpecifyURI() throws Exception {
         getConnection();
         writeTestBlob(null);
         boolean succeeded;
         try {
             getTestBlob().moveTo(null, null);
             succeeded = true;
         } catch (UnsupportedOperationException e) {
             succeeded = false;
         }
         deleteTestBlob();
         Assert.assertFalse(succeeded);
     }
 
     @Test
     public void testMoveBlobOverExistingBlobShouldFail() throws Exception {
         getConnection();
         writeTestBlob(null);
         Blob target = connection.getBlob(URI.create("target-blob"), null);
         writeBlob("Some Stuff".getBytes(), target);
         Assert.assertTrue(target.exists());
         boolean succeeded;
         try {
             getTestBlob().moveTo(target.getId(), null);
             succeeded = true;
         } catch (DuplicateBlobException e) {
             succeeded = false;
         } finally {
             target.delete();
             getTestBlob().delete();
         }
         Assert.assertFalse(succeeded);
     }
 
     @Test
     public void testDisallowOverwrite() throws Exception {
         getConnection();
         writeTestBlob(null);
         OutputStream out = null;
         boolean succeeded;
         try {
             out = getTestBlob().openOutputStream(100, false);
             out.write("New content".getBytes());
             out.close();
             succeeded = true;
         } catch (DuplicateBlobException e) {
             succeeded = false;
         } finally {
             deleteTestBlob();
         }
         Assert.assertFalse(succeeded);
     }
 
     @Test
     public void testLowLevelWriteDisallowsOverwrite() throws Exception {
         getConnection();
         writeTestBlob(null);
         File tmpFile = File.createTempFile("test", ".blob");
         tmpFile.deleteOnExit();
         CaringoOutputStream out = new CaringoOutputStream(100, false, getTestBlob(), tmpFile);
         out.write("Output".getBytes());
         boolean succeeded;
         try {
             out.close();
             succeeded = true;
         } catch (DuplicateBlobException e) {
             succeeded = false;
         } finally {
             deleteTestBlob();
             tmpFile.delete();
         }
         Assert.assertFalse(succeeded);
     }
 
     //This can be manually set to various sizes
     @Test
     public void testLargeObject() throws Exception {
         getConnection();
         Blob source = connection.getBlob(URI.create("large-source"), null);
         Blob target = connection.getBlob(URI.create("large-target"), null);
         final int bufferSize = 1024;
         //final int fileSize = 100 * 1024 * 1024;
         //final long fileSize = 1024L * 1024 * 1024;
         final long fileSize = 1 * 1024 * 1024;
         try {
             //Write a lot of information to source - 100MB
             byte[] bytes = new byte[bufferSize];
             for (Integer i = 0; i < bufferSize; i++) {
                 bytes[i] = i.byteValue();
             }
             OutputStream out = source.openOutputStream(fileSize, true);
             for (int i = 0; i < fileSize / bufferSize; i++) {
                 out.write(bytes);
             }
             out.close();
             //Check the size
             Assert.assertEquals(source.getSize(), fileSize);
 
             //Retrieve an input stream
             InputStream in = source.openInputStream();
             byte[] read_bytes = new byte[bufferSize];
             in.read(read_bytes);
             try {
                 for (int i = 0; i < bufferSize; i++) {
                     Assert.assertEquals(bytes[i], read_bytes[i]);
                 }
             } finally {
                 in.close();
             }
 
 
             //Try to copy to another blob
             source.moveTo(target.getId(), null);
             Assert.assertTrue(target.exists());
             Assert.assertEquals(target.getSize(), fileSize);
             Assert.assertFalse(source.exists());
         } finally {
             deleteBlob(source);
             deleteBlob(target);
         }
     }
 
     @Test
     public void testInputStreamForMissingBlob() throws Exception {
         getConnection();
         Blob blob = connection.getBlob(URI.create("never-created-blob"), null);
         Assert.assertFalse(blob.exists());
         boolean succeeded;
         InputStream input = null;
         try {
             input = blob.openInputStream();
             succeeded = true;
         } catch (MissingBlobException e) {
             succeeded = false;
         } finally {
             if (input != null)
                 input.close();
         }
         Assert.assertFalse(succeeded);
     }
 
 }
