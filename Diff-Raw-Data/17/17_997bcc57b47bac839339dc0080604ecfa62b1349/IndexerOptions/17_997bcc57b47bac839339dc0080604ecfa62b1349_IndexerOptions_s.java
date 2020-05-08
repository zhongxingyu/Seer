 package com.agroknow.indexer;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.Arrays;
import net.zettadata.simpleparser.SimpleMetadataFactory;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author aggelos
  */
 public class IndexerOptions {
 
    private static final String[] SUPPORTED_FILE_FORMATS = { SimpleMetadataFactory.AKIF, SimpleMetadataFactory.AGRIF };
 
     public String fileFormat;
     public String rootDirectory;
     public String runtimeDirectory;
     public String charset;
     public int bulkSize;
 
     public void validate() throws ParseException {
         // check that file format is valid
         Arrays.sort(SUPPORTED_FILE_FORMATS);
         if (this.fileFormat != null && Arrays.binarySearch(SUPPORTED_FILE_FORMATS, this.fileFormat) < 0) {
             throw new ParseException(String.format("File format %s is not supported. Select one of %s to proceed", this.fileFormat, SUPPORTED_FILE_FORMATS));
         }
 
         // check that root directory exists
         if(this.rootDirectory != null) {
             File rootDirectoryFile = FileUtils.getFile(this.rootDirectory);
             if (rootDirectoryFile == null || !rootDirectoryFile.isDirectory()) {
                 throw new ParseException("The specified root-directory does not exist or is not a directory");
             }
         }
 
         // check that runtime directory exists, if not create it
         if(this.runtimeDirectory != null) {
             File runtimeDirectoryFile = FileUtils.getFile(this.runtimeDirectory);
             if (!runtimeDirectoryFile.isDirectory()) {
                 runtimeDirectoryFile.mkdirs();
             }
         }
 
         // check that charset is valid
         try {
             Charset.forName(this.charset);
         } catch(Exception ex) {
             throw new ParseException("The specified charset is not valid. See Java docs for Charset [http://docs.oracle.com/javase/6/docs/api/java/nio/charset/Charset.html].");
         }
 
         // check the bulkSize number range
         if ( this.bulkSize < 1 || 1000 < this.bulkSize) {
             throw new ParseException("The bulk-size number must be between 1 and 1000");
         }
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("IndexerOptions [");
         if (fileFormat != null) {
             builder.append("fileFormat=");
             builder.append(fileFormat);
             builder.append(", ");
         }
         if (rootDirectory != null) {
             builder.append("rootDirectory=");
             builder.append(rootDirectory);
             builder.append(", ");
         }
         if (runtimeDirectory != null) {
             builder.append("runtimeDirectory=");
             builder.append(runtimeDirectory);
         }
         builder.append("]");
         return builder.toString();
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 89 * hash + (this.fileFormat != null ? this.fileFormat.hashCode() : 0);
         hash = 89 * hash + (this.rootDirectory != null ? this.rootDirectory.hashCode() : 0);
         hash = 89 * hash + (this.runtimeDirectory != null ? this.runtimeDirectory.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final IndexerOptions other = (IndexerOptions) obj;
         if ((this.fileFormat == null) ? (other.fileFormat != null) : !this.fileFormat.equals(other.fileFormat)) {
             return false;
         }
         if ((this.rootDirectory == null) ? (other.rootDirectory != null) : !this.rootDirectory.equals(other.rootDirectory)) {
             return false;
         }
         if ((this.runtimeDirectory == null) ? (other.runtimeDirectory != null) : !this.runtimeDirectory.equals(other.runtimeDirectory)) {
             return false;
         }
         return true;
     }
 }
