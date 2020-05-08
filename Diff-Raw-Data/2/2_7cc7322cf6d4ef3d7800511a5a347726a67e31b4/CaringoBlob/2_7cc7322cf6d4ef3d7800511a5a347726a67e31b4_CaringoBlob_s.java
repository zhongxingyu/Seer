 package edu.illinois.medusa;
 
 import org.akubraproject.Blob;
 import org.akubraproject.DuplicateBlobException;
 import org.akubraproject.MissingBlobException;
 import org.akubraproject.impl.AbstractBlob;
 import org.akubraproject.impl.StreamManager;
 import org.apache.commons.io.IOUtils;
 
 import java.io.*;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Implement the Akubra AbstractBlob for connection to a Caringo server
  *
  * @author Howard Ding - hding2@illinois.edu
  */
 public class CaringoBlob extends AbstractBlob {
 
     /**
      * The owning BlobStoreConnection
      */
     protected CaringoBlobStoreConnection owner;
     /**
      * The response from the last interaction with Caringo storage.
      */
     protected CaringoAbstractResponse response;
 
     /**
      * Construct from connection and id for blob.
      *
     * @param owner Owning BlboStoreConnection
      * @param id    ID of blob
      */
     protected CaringoBlob(CaringoBlobStoreConnection owner, URI id) {
         super(owner, id);
         this.owner = owner;
         this.response = null;
     }
 
     /**
      * Get StreamManager responsible for this blob
      *
      * @return The StreamManager of the owning connection.
      */
     protected StreamManager getStreamManager() {
         return this.owner.getStreamManager();
     }
 
     /**
      * Determine whether this blob exists in storage
      *
      * @return Whether the blob exists
      * @throws IOException If there is an error interacting with storage or an unexpected return status from storage.
      */
     public boolean exists() throws IOException {
         response = this.info();
         if (response.notFound())
             return false;
         if (response.ok())
             return true;
         throw new IOException();
     }
 
     /**
      * Determine the size of the blob.
      *
      * @return The size of the blob
      * @throws MissingBlobException If the blob is not found
      * @throws IOException          If there is an unexpected return value,
      *                              or there is an error in the interaction with the storage.
      */
     public long getSize() throws MissingBlobException, IOException {
         CaringoInfoResponse infoResponse = this.info();
         response = infoResponse;
         if (response.notFound()) {
             throw new MissingBlobException(this.id);
         }
         if (!response.ok()) {
             throw new IOException();
         }
         return infoResponse.contentLength();
     }
 
     /**
      * Do an info request via the owning connection. This allows determination of the existence and size of the blob.
      *
      * @return Response from info request
      * @throws IOException If there is an error conducting the info request
      */
     private CaringoInfoResponse info() throws IOException {
         return this.info(5);
     }
 
     private CaringoInfoResponse info(int retries) throws IOException {
         CaringoInfoResponse infoResponse = this.owner.info(this.id);
         if (!infoResponse.serverError()) {
             return infoResponse;
         }
         if (infoResponse.serverError() && retries > 0) {
             logRetryAndSleep(retries);
             return info(retries - 1);
         }
         //If we get here we've failed and are out of retries
         throw new IOException(outOfRetriesErrorMessage(infoResponse));
     }
 
     /**
      * Open an input stream on an existing blob from storage. This stream is managed by the blob's stream manager.
      *
      * @return InputStream on the blob's contents
      * @throws MissingBlobException If the blob is not found
      * @throws IOException          If there is an error interacting with storage or an unexpected return value.
      */
     public InputStream openInputStream() throws MissingBlobException, IOException {
         return openInputStream(5);
     }
 
     /**
      * Open an input stream on an existing blob from storage. This stream is managed by the blob's stream manager.
      *
      * @return InputStream on the blob's contents
      * @throws MissingBlobException If the blob is not found
      * @throws IOException          If there is an error interacting with storage or an unexpected return value.
      */
     public InputStream openInputStream(int retries) throws MissingBlobException, IOException {
         /*
             TODO
             I wonder if we could do better on this. As is, it pulls the entire stream from Caringo, writes
             to a temporary file, and the provided input stream is an InputStream on that file.
             One possibility (depending on what the requirements of this InputStream are) might be to
             use the ability to fetch pieces of a datastream (does the CaringoSDK support this) and expose
             them via a circular buffer or piped input stream. So maybe create a piped input stream, hook it
             to a piped output stream. Spin off a thread that does an info on the object to get the size then
             reads it piece by piece and writes to the pipe. Overall this might be less efficient, but for
             large files data would start to appear earlier.
 
             Probably the corresponding idea for writing to Caringo is less important, since there's not really
             any possible visible result until the entire operation is complete.
          */
 
         CaringoReadResponse readResponse = this.owner.read(this.id);
         response = readResponse;
         if (readResponse.ok()) {
             CaringoInputStream input = new CaringoInputStream(readResponse.getFile());
             return this.getStreamManager().manageInputStream(this.owner, new BufferedInputStream(input));
         }
         if (readResponse.notFound()) {
             readResponse.cleanupFile();
             throw new MissingBlobException(this.id);
         }
         if (readResponse.serverError() && retries > 0) {
             logRetryAndSleep(retries);
             return openInputStream(retries - 1);
         }
         readResponse.cleanupFile();
         throw new IOException(outOfRetriesErrorMessage(readResponse));
     }
 
     private void logRetryAndSleep(int retries) {
         System.err.println("Error getting: " + this.getId().toString() + " - " + (retries - 1) + " retries left.");
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
             //do nothing
         }
     }
 
     private String outOfRetriesErrorMessage(CaringoAbstractResponse response) {
         String errorString = "RESPONSE_CODE: " + response.response.getHttpStatusCode();
         errorString += "\nID: " + this.getId().toString();
         errorString += "\nRESPONSE: " + response.response.getResponseBody();
         HashMap<String, ArrayList<String>> headers = response.response.getResponseHeaders().getHeaderMap();
         for (String header : headers.keySet()) {
             for (String value : headers.get(header)) {
                 errorString += "\nHEADER: " + header + "\t" + value;
             }
         }
         return errorString;
     }
 
     /**
      * Return an output stream for writing to this blob. This stream is managed by the blob's connection manager. The
      * actual write occurs after the output stream is closed.
      *
      * @param estimated_length An estimate for the amount of data - ignored by this method's implementation
      * @param overwrite        Whether this should overwrite existing data in the blob
      * @return An OutputStream to write to this blob
      * @throws DuplicateBlobException If overwrite is false and this blob already exists
      * @throws IOException            If there is a problem making the output stream
      */
     public OutputStream openOutputStream(long estimated_length, boolean overwrite) throws DuplicateBlobException, IOException {
         if (!overwrite && this.exists()) {
             throw new DuplicateBlobException(this.id);
         }
         File tempFile = File.createTempFile("fedora-out", ".blob");
         tempFile.deleteOnExit();
         CaringoOutputStream outputStream = new CaringoOutputStream(estimated_length, overwrite, this, tempFile);
         return this.getStreamManager().manageOutputStream(this.owner, new BufferedOutputStream(outputStream));
     }
 
     /**
      * Delete the blob from storage. If the blob does not exist no error is reported.
      *
      * @throws IOException If there is an error interacting with storage or an unexpected response code.
      */
     public void delete() throws IOException {
         CaringoDeleteResponse deleteResponse = this.owner.delete(this.id);
         response = deleteResponse;
         if (!deleteResponse.ok() && !deleteResponse.notFound())
             throw new IOException();
     }
 
     /**
      * Move this blob to a different location. Writes the new blob and then deletes this one.
      *
      * @param uri   Location to which to move this blob
      * @param hints Hints map - unused
      * @return The new blob
      * @throws DuplicateBlobException        If the target blob already exists in storage
      * @throws MissingBlobException          If this blob does not exist in storage
      * @throws UnsupportedOperationException If a target URI is not provided (i.e. if it is null)
      * @throws IOException                   If there is an error interacting with storage, creating the new blob, deleting this blob,
      *                                       or any other error in performing this operation.
      */
     public Blob moveTo(URI uri, Map<String, String> hints) throws UnsupportedOperationException, DuplicateBlobException,
             IOException, MissingBlobException {
         /* TODO this might benefit from some bulletproofing. I'm not sure the Fedora actually uses this method though,
             so it can probably wait. Honestly I'm not sure how far it is possible to bulletproof. E.g. suppose we save
             the new blob and encounter an error deleting the old one. Given how everything operates, what is the reasonable
             thing to do here? And how do we ensure that _it_ succeeds? Since the storage really has nothing like transactions
             it's unclear that a solution is even possible.
          */
         //perform some initial checks
         if (!this.exists())
             throw new MissingBlobException(this.id);
         if (uri == null)
             throw new UnsupportedOperationException();
         CaringoBlob newBlob = this.owner.getBlob(uri, null);
         if (newBlob.exists())
             throw new DuplicateBlobException(uri);
 
         //store content in new blob
         InputStream input = this.openInputStream();
         OutputStream output = newBlob.openOutputStream(1024, false);
         preprocessMoveTo(newBlob);
         IOUtils.copyLarge(input, output);
         output.close();
         input.close();
 
         //remove old blob
         this.delete();
         return newBlob;
     }
 
     /**
      * Hook method meant for overriding in subclasses to do any operations needed before writing the new blob in a moveTo
      * operation.
      *
      * @param newBlob The blob being moved to.
      */
     protected void preprocessMoveTo(CaringoBlob newBlob) {
         //overwrite in subclass to do something in a moveTo action between opening the stream on the existing blob and
         //opening the output stream on the new one.
     }
 
     /**
      * This performs the actual write of a blob once the OutputStream used to write it is closed.
      *
      * @param content   The output stream being written
      * @param overwrite Whether we can overwrite this blob if it already exists in storage
      * @throws DuplicateBlobException If this blob already exists in storage and overwrite is false
      * @throws IOException            If the blob is not created on write or there is an error interacting with storage.
      */
     protected void write(CaringoOutputStream content, boolean overwrite) throws DuplicateBlobException, IOException {
         if (!overwrite && this.exists()) {
             throw new DuplicateBlobException(this.id);
         }
         preprocessWrite(content);
         CaringoWriteResponse writeResponse = sendWrite(content, overwrite);
         response = writeResponse;
         if (!writeResponse.created())
             throw new IOException();
         postprocessWrite();
     }
 
     /**
      * Interact with the owning connection to attempt to write this blob. To be overridden in a subclass if it
      * needs to do something different.
      *
      * @param content   OutputStream to be written
      * @param overwrite Whether or not it is permissible to overwrite this blob if it is already in storage
      * @return Response from storage server
      * @throws IOException If there is an error interacting with storage
      */
     //override to change how the write is sent to the BlobStoreConnection
     protected CaringoWriteResponse sendWrite(CaringoOutputStream content, boolean overwrite) throws IOException {
         return this.owner.write(this.id, content, overwrite);
     }
 
     /**
      * Hook for subclasses to take action before trying to write to the storage server.
      *
      * @param content The OutputStream with the bytes to be written
      */
     protected void preprocessWrite(CaringoOutputStream content) {
 
     }
 
     /**
      * Hook for subclasses to take action after a successful write to the storage server.
      */
     protected void postprocessWrite() {
 
     }
 
     /**
      * The response from the last interaction of this blob with the storage server.
      *
      * @return Response from the last interaction with storage
      */
     public CaringoAbstractResponse response() {
         return response;
     }
 
 }
