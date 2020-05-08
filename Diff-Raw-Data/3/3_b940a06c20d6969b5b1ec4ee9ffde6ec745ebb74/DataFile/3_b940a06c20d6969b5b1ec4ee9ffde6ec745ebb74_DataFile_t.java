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
 package de.weltraumschaf.juberblog.model;
 
 import de.weltraumschaf.juberblog.Constants;
 import de.weltraumschaf.juberblog.Headline;
 import de.weltraumschaf.juberblog.Preprocessor;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Represents a data file.
  *
  * In JUberblog the data file is the central data storage. It is based on Markdown files with some additions. On of the
  * additions is the the file name convention.
  *
  * The file names are produced by this schema:
  * <pre>
  * filename := TIMESTAMP '.' SLUG '.md' ;
  * </pre>
  *
  * TIMESTAMP is the Unix time stamp when the file was created. SLUG is th slugged form of the title.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class DataFile {
 
     /**
      * Separates the timestamp from the beginning of the file base name.
      */
     private static final String TIMESTAMP_SEP = ".";
     /**
      * Signals that the {@link #creationTime} is not initialized.
      */
     private static final int UNITIALIZED = -1;
     private final DataProcessor processor;
     /**
      * The original absolute file name.
      */
     private final File file;
     /**
      * Lazy computed.
      */
     private String basename;
     /**
      * Lazy computed.
      */
     private long creationTime = UNITIALIZED;
     /**
      * Lazy computed.
      */
     private String slug;
 
     /**
      * Dedicated constructor.
      *
      * @param file must not be {@code null} or empty
      * @throws java.io.FileNotFoundException if file can't be read
      */
     public DataFile(final File file) throws FileNotFoundException {
         super();
         Validate.notNull(file, "Filename must not be null or empty!");
         this.file = file;
         this.processor = new DataProcessor(file);
     }
 
     /**
      * Get the absolute file name.
      *
      * @return never {@code null} or empty
      */
     public String getFilename() {
         return file.getName();
     }
 
     /**
      * Get the file base name.
      *
      * The base name is the part after the last OS dependent directory separator.
      *
      * @return never {@code null}
      */
     public String getBasename() {
         if (null == basename) {
             final int pos = getFilename().lastIndexOf(Constants.DIR_SEP.toString()) + 1;
             basename = getFilename().substring(pos);
         }
 
         return basename;
     }
 
     /**
      * Get the creation time stamp extracted from the file name.
      *
      * @return greater -1
      */
     public long getCreationTime() {
         if (UNITIALIZED == creationTime) {
             final String name = getBasename();
             final int pos = name.indexOf(TIMESTAMP_SEP);
             creationTime = Long.valueOf(name.substring(0, pos));
         }
 
         return creationTime;
     }
 
     /**
      * Get last modification day of file.
      *
      * @return non negative long number
      */
     public long getModificationTime() {
         return file.lastModified();
     }
 
     /**
      * Get the slug part of the file name.
      *
      * The slug part is the part between the timestamp and the file extension.
      *
      * @return never {@code null}
      */
     public String getSlug() {
         if (null == slug) {
             final String name = getBasename();
             final int start = name.indexOf(TIMESTAMP_SEP) + 1;
             final int stop = name.lastIndexOf(".");
             slug = name.substring(start, stop);
         }
 
         return slug;
     }
 
     /**
      * Get processed meta data of file.
      *
      * This method reads and parses the file content.
      *
      * @return never {@code  null}
      * @throws IOException if file can't be read
      */
     public MetaData getMetaData() throws IOException {
         return processor.getMetaData();
     }
 
     /**
      * Get raw Markdown of file.
      *
      * This method reads and parses the file content.
      *
      * @return never {@code  null}
      * @throws IOException if file can't be read
      */
     public String getMarkdown() throws IOException {
         return processor.getMarkdown();
     }
 
     /**
      * Get processed headline of file.
      *
      * This method reads and parses the file content.
      *
      * @return never {@code  null}
      * @throws IOException if file can't be read
      */
     public String getHeadline() throws IOException {
         return processor.getHeadline();
     }
 
     /**
      * Get processed keywords of file.
      *
      * This method reads and parses the file content.
      *
      * @return never {@code  null}
      * @throws IOException if file can't be read
      */
     public String getKeywords() throws IOException {
         return getMetaData().getKeywords();
     }
 
     /**
      * Get processed description of file.
      *
      * This method reads and parses the file content.
      *
      * @return never {@code  null}
      * @throws IOException if file can't be read
      */
     public String getDescription() throws IOException {
         return getMetaData().getDescription();
     }
 
     @Override
     public String toString() {
         return file.toString();
     }
 
     @Override
     public int hashCode() {
         return file.hashCode();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof DataFile)) {
             return false;
         }
 
         final DataFile other = (DataFile) obj;
         return getFilename().equals(other.getFilename());
     }
 
     /**
      * Parses meta data and Markdown out of a file.
      */
     private static class DataProcessor {
 
         /**
          * Parses meta data.
          */
         private final Preprocessor metaDataParser = new Preprocessor();
         /**
          * Extracts headline.
          */
         private final Headline headliner = new Headline();
         /**
          * To read data from.
          */
         private final InputStream input;
         /**
          * Lazy computed so never access directly, but use {@link #getMetaData()}.
          */
         private MetaData metaData;
         /**
          * Lazy computed so never access directly, but use {@link #getMarkdown()}.
          */
         private String markdown;
         /**
          * Lazy computed so never access directly, but use {@link #getHeadline()}.
          */
         private String headline;
 
         /**
          * Convenience constructor.
          *
         * @param file must not be {@code null}
          * @throws FileNotFoundException if file not found
          */
         public DataProcessor(final File file) throws FileNotFoundException {
             this(new FileInputStream(file));
             Validate.notNull(file, "File must not be null!");
         }
 
         /**
          * Dedicated constructor.
          *
          * @param in must not be {@literal null}
          */
         public DataProcessor(
                 final InputStream in) {
             super();
             Validate.notNull(in, "Data file must not be empty!");
             this.input = in;
         }
 
         /**
          * Get the processed meta data.
          *
          * @return never {@literal null}
          * @throws IOException on any read error of data or template file
          */
         public MetaData getMetaData() throws IOException {
             if (null == metaData) {
                 readFileContent();
             }
 
             return metaData;
         }
 
         /**
          * Read the plain file content and parsed the meta data and Markdown,
          *
          * Initializes the fields {@link #metaData} and {@link #markdown}.
          *
          * @throws IOException if content can't be read
          */
         private void readFileContent() throws IOException {
             final String raw = IOUtils.toString(input);
             markdown = metaDataParser.process(raw);
             metaData = metaDataParser.getMetaData();
             IOUtils.closeQuietly(input);
         }
 
         /**
          * Get the raw Markdown content.
          *
          * @return never {@code null}
          * @throws IOException if content can't be read
          */
         public String getMarkdown() throws IOException {
             if (null == markdown) {
                 readFileContent();
             }
 
             return markdown;
         }
 
         /**
          * Get the processed headline.
          *
          * @return never {@literal null}
          * @throws IOException on any read error of data or template file
          */
         public String getHeadline() throws IOException {
             if (null == headline) {
                 getMetaData();
                 headline = headliner.find(markdown);
             }
 
             return headline;
         }
     }
 }
