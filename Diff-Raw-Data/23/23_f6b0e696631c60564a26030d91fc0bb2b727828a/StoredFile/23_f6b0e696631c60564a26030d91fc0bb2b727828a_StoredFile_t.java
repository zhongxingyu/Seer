 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.web.service.file;
 
 import com.google.common.base.Objects;
 import java.io.IOException;
 import java.io.Serializable;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Represents a stored user file.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class StoredFile implements Serializable {
 
     /**
      * Serialization version.
      */
     private static final long serialVersionUID = 1L;
     /**
      * Encoding of the file content.
      */
     private final String encoding;
     /**
      * Type of file content.
      */
     private final String contentType;
     /**
      * Full path where file is saved.
      */
     private final String fileName;
     /**
      * File content itself.
      *
      * This field is lazy loaded.
      */
    private transient byte[] fileContent = null;
 
     /**
      * Initializes {@link #fileContent} with {@code null}.
      *
      * @param encoding must not be {@code null} or empty
      * @param contentType must not be {@code null} or empty
      * @param fileName must not be {@code null} or empty
      */
     public StoredFile(final String encoding, final String contentType, final String fileName) {
         this(encoding, contentType, fileName, new byte[]{});
     }
 
     /**
      * Dedicated constructor.
      *
      * @param encoding must not be {@code null} or empty
      * @param contentType must not be {@code null} or empty
      * @param fileName must not be {@code null} or empty
      * @param fileContent may be {@code null}
      */
     public StoredFile(
             final String encoding,
             final String contentType,
             final String fileName,
             final byte[] fileContent) {
         super();
         Validate.notNull(encoding, "Encoding must not be empty!");
         this.encoding = encoding;
         Validate.notEmpty(contentType, "Content type must not be empty!");
         this.contentType = contentType;
         Validate.notEmpty(fileName, "File name must not be empty!");
         this.fileName = fileName;
         this.fileContent = fileContent.clone();
     }
 
     /**
      * Get the file content encoding.
      *
      * @return never {@code null}
      */
     public String getEncoding() {
         return encoding;
     }
 
     /**
      * Get the file content type.
      *
      * @return never {@code null}
      */
     public String getContentType() {
         return contentType;
     }
 
     /**
      * Get the file content.
      *
      * Try to load the file content from disk if {@link #fileContent} is {@code null}.
      *
      * @return never {@code null}, may be empty array
      * @throws IOException if content can't be read from disk
      */
     public synchronized byte[] getFileContent() throws IOException {
         if (null == fileContent) {
             loadFileContent();
         }
 
         return null == fileContent ? new byte[]{} : fileContent.clone();
     }
 
     /**
      * Saves file content to disk.
      *
      * @throws IOException on any IO error
      */
     synchronized void saveFileContent() throws IOException {
         if (null == fileContent) {
             throw new IllegalStateException(String.format("No file data to save in '%s'!", fileName));
         }
 
         final Path file = createPath();
         final Path userDir = file.getParent();
 
         if (!Files.exists(userDir)) {
             Files.createDirectories(userDir);
         }
 
         Files.write(file, fileContent, StandardOpenOption.CREATE);
     }
 
     /**
      * Load file content from disk.
      *
      * @throws IOException on any IO error
      */
     private void loadFileContent() throws IOException {
         fileContent = Files.readAllBytes(createPath());
     }
 
     /**
      * Delete file content from disk.
      *
      * @throws IOException on any IO error
      */
     synchronized void deleteFileContent() throws IOException {
         Files.deleteIfExists(createPath());
     }
 
     /**
      * Create path to save/load/delete from.
      *
      * @return the resulting {@code Path}
      */
     private Path createPath() {
         return FileSystems.getDefault().getPath(fileName);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(encoding, contentType, fileName);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof StoredFile)) {
             return false;
         }
 
         final StoredFile other = (StoredFile) obj;
         return Objects.equal(encoding, other.encoding)
                 && Objects.equal(contentType, other.contentType)
                 && Objects.equal(fileName, other.fileName);
     }
 
 }
