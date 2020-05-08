 package org.esidoc.core.utils.io;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlTransient;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 public final class Stream extends OutputStream {
 
     private static final Logger LOG = LoggerFactory.getLogger(Stream.class);
 
     private static final File DEFAULT_TEMP_DIR;
     private static final int DEFAULT_THRESHOLD;
 
     static {
         final String thresholdString = System.getProperty("Stream.Threshold", "-1");
         int threshold = Integer.parseInt(thresholdString);
         if(threshold <= 0) {
             threshold = 64 * 1024;
         }
         DEFAULT_THRESHOLD = threshold;
         final String outputDirectoryString = System.getProperty("Stream.OutputDirectory");
         if(outputDirectoryString != null) {
             final File outputDirectory = new File(outputDirectoryString);
             if(outputDirectory.exists() && outputDirectory.isDirectory()) {
                 DEFAULT_TEMP_DIR = outputDirectory;
             } else {
                 DEFAULT_TEMP_DIR = null;
             }
         } else {
             DEFAULT_TEMP_DIR = null;
         }
     }
 
     @XmlTransient
     private boolean outputLocked;
 
     @XmlTransient
     private OutputStream currentStream;
 
     @XmlTransient
     private long threshold = DEFAULT_THRESHOLD;
 
     @XmlTransient
     private long totalLength;
 
     @XmlTransient
     private boolean inMemory;
 
     @XmlTransient
     private boolean tempFileFailed;
 
     @XmlTransient
     private File tempFile;
 
     @XmlTransient
     private File outputDirectory = DEFAULT_TEMP_DIR;
 
     public Stream(final PipedInputStream stream) throws IOException {
         this.currentStream = new PipedOutputStream(stream);
         this.setInMemory(true);
     }
 
     public Stream() {
         this.currentStream = new LoadingByteArrayOutputStream(DEFAULT_THRESHOLD);
         this.setInMemory(true);
     }
 
     public Stream(final long threshold) {
         this.threshold = threshold;
         this.currentStream = new LoadingByteArrayOutputStream(DEFAULT_THRESHOLD);
         this.setInMemory(true);
     }
 
     public boolean isOutputLocked() {
         return outputLocked;
     }
 
     protected void setOutputLocked(final boolean outputLocked) {
         this.outputLocked = outputLocked;
     }
 
     public long getThreshold() {
         return threshold;
     }
 
     public void setThreshold(final long threshold) {
         this.threshold = threshold;
     }
 
     public boolean isInMemory() {
         return inMemory;
     }
 
     protected void setInMemory(final boolean inMemory) {
         this.inMemory = inMemory;
     }
 
     public void setOutputDirectory(final File outputDirectory) {
         this.outputDirectory = outputDirectory;
     }
 
     public File getTempFile() {
         return this.tempFile != null && this.tempFile.exists() ? this.tempFile : null;
     }
 
     /**
      * Returns the underlying output stream.
      *
      * @return the underlying output stream
      */
     public OutputStream getOutputStream() {
         return this.currentStream;
     }
 
     /**
      * Locks the output stream to prevent additional writes, but maintains a pointer to it so an InputStream can be
      * obtained
      */
     public void lock() throws IOException {
         if(this.isOutputLocked()) {
             return;
         }
         this.currentStream.flush();
         this.setOutputLocked(true);
     }
 
     @Override
     public void flush() throws IOException {
         this.currentStream.flush();
     }
 
     @Override
     public void close() throws IOException {
         this.currentStream.flush();
         this.setOutputLocked(true);
         this.currentStream.close();
         maybeDeleteTempFile();
     }
 
     /**
      * Replace the original stream with the new one, optionally copying the content of the old one into the new one.
      * When with Attachment, needs to replace the xml writer stream with the stream used by AttachmentSerializer or copy
      * the cached output stream to the "real" output stream, i.e. onto the wire.
      *
      * @param out            the new output stream
      * @param copyOldContent flag indicating if the old content should be copied
      */
     public void resetOut(OutputStream out, final boolean copyOldContent) throws IOException {
         if(out == null) {
             out = new ByteArrayOutputStream();
         }
         if(this.currentStream instanceof Stream) {
             final Stream ac = (Stream) this.currentStream;
             final InputStream in = ac.getInputStream();
             IOUtils.copyAndCloseInput(in, out);
         } else {
             if(isInMemory()) {
                 if(this.currentStream instanceof ByteArrayOutputStream) {
                     final ByteArrayOutputStream byteOut = (ByteArrayOutputStream) this.currentStream;
                     if(copyOldContent && byteOut.size() > 0) {
                         byteOut.writeTo(out);
                     }
                 } else if(this.currentStream instanceof PipedOutputStream) {
                     final PipedOutputStream pipeOut = (PipedOutputStream) this.currentStream;
                     IOUtils.copyAndCloseInput(new PipedInputStream(pipeOut), out);
                 } else {
                     throw new IOException("Unknown format of currentStream");
                 }
             } else {
                 // read the file
                 this.currentStream.close();
                 if(copyOldContent) {
                     final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
                     IOUtils.copyAndCloseInput(in, out);
                 }
                 final boolean sucessfull = this.tempFile.delete();
                 if(! sucessfull) {
                     if(LOG.isInfoEnabled()) {
                         LOG.info("Error on deleting temp file '" + tempFile.getName() + "'.");
                     }
                 }
                 this.tempFile = null;
                 this.setInMemory(true);
             }
         }
         this.currentStream = out;
         this.setOutputLocked(false);
     }
 
     public long size() {
         return this.totalLength;
     }
 
     public byte[] getBytes() throws IOException {
         flush();
         if(this.isInMemory()) {
             if(this.currentStream instanceof ByteArrayOutputStream) {
                 return ((ByteArrayOutputStream) this.currentStream).toByteArray();
             } else {
                 throw new IOException("Unknown format of currentStream");
             }
         } else {
             // read the file
             final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
             return IOUtils.readBytesFromStream(in);
         }
     }
 
     public void writeCacheTo(final OutputStream out) throws IOException {
         flush();
         if(isInMemory()) {
             if(this.currentStream instanceof ByteArrayOutputStream) {
                 ((ByteArrayOutputStream) this.currentStream).writeTo(out);
             } else {
                 throw new IOException("Unknown format of current stream.");
             }
         } else {
             // read the file
             final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
             IOUtils.copyAndCloseInput(in, out);
         }
     }
 
     public void writeCacheTo(final StringBuilder out, final int limit) throws IOException {
         writeCacheTo(out, "UTF-8", limit);
     }
 
     public void writeCacheTo(final StringBuilder out, final String charsetName, final int limit) throws IOException {
         flush();
         if(this.totalLength < limit || limit == - 1) {
             writeCacheTo(out);
             return;
         }
         if(this.isInMemory()) {
             if(this.currentStream instanceof ByteArrayOutputStream) {
                 final byte[] bytes = ((ByteArrayOutputStream) this.currentStream).toByteArray();
                 out.append(IOUtils.newStringFromBytes(bytes, charsetName, 0, limit));
             } else {
                 throw new IOException("Unknown format of current stream.");
             }
         } else {
             // read the file
             final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
             try {
                 final byte bytes[] = new byte[1024];
                 int x = in.read(bytes);
                 int count = 0;
                 while(x != - 1) {
                     if((count + x) > limit) {
                         x = limit - count;
                     }
                     out.append(IOUtils.newStringFromBytes(bytes, charsetName, 0, x));
                     count += x;
                     if(count >= limit) {
                         x = - 1;
                     } else {
                         x = in.read(bytes);
                     }
                 }
             } finally {
                 if(in != null) {
                     in.close();
                 }
             }
         }
     }
 
     public void writeCacheTo(final StringBuilder out) throws IOException {
         writeCacheTo(out, "UTF-8");
     }
 
     public void writeCacheTo(final StringBuilder out, final String charsetName) throws IOException {
         flush();
         if(isInMemory()) {
             if(this.currentStream instanceof ByteArrayOutputStream) {
                 final byte[] bytes = ((ByteArrayOutputStream) this.currentStream).toByteArray();
                 out.append(IOUtils.newStringFromBytes(bytes, charsetName));
             } else {
                 throw new IOException("Unknown format of currentStream");
             }
         } else {
             // read the file
             final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
             try {
                 final byte bytes[] = new byte[1024];
                 int x = in.read(bytes);
                 while(x != - 1) {
                     out.append(IOUtils.newStringFromBytes(bytes, charsetName, 0, x));
                     x = in.read(bytes);
                 }
             } finally {
                 in.close();
             }
         }
     }
 
 
     public void writeCacheTo(final StringBuffer out) throws IOException {
         writeCacheTo(out, "UTF-8");
     }
 
     public void writeCacheTo(final StringBuffer out, final String charsetName) throws IOException {
         flush();
         if(isInMemory()) {
             if(this.currentStream instanceof ByteArrayOutputStream) {
                 final byte[] bytes = ((ByteArrayOutputStream) this.currentStream).toByteArray();
                 out.append(IOUtils.newStringFromBytes(bytes, charsetName));
             } else {
                 throw new IOException("Unknown format of currentStream");
             }
         } else {
             // read the file
             final InputStream in = new BufferedInputStream(new FileInputStream(this.tempFile));
             try {
                 final byte bytes[] = new byte[1024];
                 int x = in.read(bytes);
                 while(x != - 1) {
                     out.append(IOUtils.newStringFromBytes(bytes, charsetName, 0, x));
                     x = in.read(bytes);
                 }
             } finally {
                 in.close();
             }
         }
     }
 
 
     @Override
     public void write(final byte[] b, final int off, final int len) throws IOException {
         if(! this.isOutputLocked()) {
             this.totalLength += len;
             if(isInMemory() && this.totalLength > this.threshold &&
                     this.currentStream instanceof ByteArrayOutputStream) {
                 createFileOutputStream();
             }
             this.currentStream.write(b, off, len);
         }
     }
 
     @Override
     public void write(final byte[] b) throws IOException {
         if(! this.isOutputLocked()) {
             this.totalLength += b.length;
             if(isInMemory() && this.totalLength > this.threshold &&
                     this.currentStream instanceof ByteArrayOutputStream) {
                 createFileOutputStream();
             }
             this.currentStream.write(b);
         }
     }
 
     @Override
     public void write(final int b) throws IOException {
         if(! this.isOutputLocked()) {
             this.totalLength++;
             if(isInMemory() && this.totalLength > this.threshold &&
                     this.currentStream instanceof ByteArrayOutputStream) {
                 createFileOutputStream();
             }
             this.currentStream.write(b);
         }
     }
 
     private void createFileOutputStream() {
         if(tempFileFailed) {
             return;
         }
         final ByteArrayOutputStream bout = (ByteArrayOutputStream) currentStream;
         try {
             if(this.outputDirectory == null) {
                 this.tempFile = FileUtils.createTempFile("ir", "tmp");
             } else {
                 this.tempFile = FileUtils.createTempFile("ir", "tmp", this.outputDirectory, false);
             }
             this.currentStream = new BufferedOutputStream(new FileOutputStream(this.tempFile));
             bout.writeTo(this.currentStream);
             this.setInMemory(false);
         } catch(final Exception e) {
             if(LOG.isWarnEnabled()) {
                 LOG.warn("Error creating temp file.", e);
             }
             // Could be IOException or SecurityException or other issues.
             // Don't care what, just keep it in memory.
             this.tempFileFailed = true;
             this.tempFile = null;
             this.setInMemory(true);
             this.currentStream = bout;
         }
     }
 
     public InputStream getInputStream() throws IOException {
         flush();
         if(isInMemory()) {
             if(this.currentStream instanceof LoadingByteArrayOutputStream) {
                 return ((LoadingByteArrayOutputStream) this.currentStream).createInputStream();
             } else if(this.currentStream instanceof ByteArrayOutputStream) {
                 return new ByteArrayInputStream(((ByteArrayOutputStream) this.currentStream).toByteArray());
             } else if(this.currentStream instanceof PipedOutputStream) {
                 return new PipedInputStream((PipedOutputStream) this.currentStream);
             } else {
                 return null;
             }
         } else {
             try {
                 return new BufferedInputStream(new FileInputStream(this.tempFile));
             } catch(final FileNotFoundException e) {
                 throw new IOException("Cached file was deleted, " + e.toString());
             }
         }
     }
 
     private void maybeDeleteTempFile() {
         if(! isInMemory() && this.tempFile != null) {
             if(this.currentStream != null) {
                 try {
                     this.currentStream.close();
                 } catch(Exception e) {
                     //ignore
                 }
             }
             final boolean sucessfull = this.tempFile.delete();
             if(! sucessfull) {
                 if(LOG.isInfoEnabled()) {
                     LOG.info("Error on deleting temp file '" + tempFile.getName() + "'.");
                 }
             }
             this.tempFile = null;
             this.currentStream = new LoadingByteArrayOutputStream(DEFAULT_THRESHOLD);
             this.setInMemory(true);
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         try {
             this.close();
             if(this.tempFile != null) {
                 if(!this.tempFile.delete()) {
                     if(LOG.isInfoEnabled()) {
                        LOG.info("Error on deleting temp file '" + tempFile.getName() + "' at finalize.");
                     }
                 }
             }
         } finally {
             super.finalize();
         }
     }
 
     @Override
     public String toString() {
         return "Stream{" + "inMemory=" + inMemory + ", threshold=" + threshold + ", totalLength=" + totalLength +
                 ", outputLocked=" + outputLocked + ", outputDirectory=" + outputDirectory + ", tempFile=" + tempFile +
                 ", tempFileFailed=" + tempFileFailed + '}';
     }
 }
