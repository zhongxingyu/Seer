 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.service.filestore;
 
 import com.kloudtek.systyrant.STContext;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * A {@link FileStore} file definition.
  */
 public class FileDefinition {
     private String path;
     private String url;
     private String sha1;
     private boolean retrievable;
     private boolean template;
     private String encoding = "UTF-8";
     private String sourceUrl;
 
     public FileDefinition() {
         sourceUrl = STContext.get().getSourceUrl();
     }
 
     public FileDefinition(String path) {
         this();
         this.path = path;
     }
 
     public FileDefinition(String path, boolean template) {
         this(path);
         this.template = template;
     }
 
     public FileDefinition(String path, String url, String sha1, boolean retrievable, boolean template) {
         this(path, template);
         this.url = url;
         this.sha1 = sha1;
         this.retrievable = retrievable;
     }
 
     public FileDefinition(String path, String url, String sha1, boolean retrievable, boolean template, String encoding) {
         this(path, url, sha1, retrievable, template);
         this.encoding = encoding;
     }
 
     /**
      * Get the file's path with the {@link FileStore}. This field is mandatory.
      *
      * @return DataFile path.
      */
     public String getPath() {
         return path;
     }
 
     public void setPath(@NotNull String path) {
         this.path = path;
     }
 
     /**
      * Returns an url where the file can be retrieved from
      *
      * @return DataFile URL or null if the file is not available remotely.
      */
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      * Return the Hex encoded ({@link org.apache.commons.codec.binary.Hex#encodeHex(byte[]))}) SHA1 checksum hash of the file.
      *
      * @return Checksum hash or null if not set.
      */
     public String getSha1() {
         return sha1;
     }
 
     public void setSha1(String sha1) {
         this.sha1 = sha1;
     }
 
     /**
      * <p>Returns a flag indicating if the file is automatically retrievable using the URL.</p>
      * This is normally used for files which are downloadable from the internet (so which have a URL), but where they
      * cannot be downloaded automatically by SysTyrant (for example due to license distribution restrictions).
      *
      * @return True if the file can be retrieved automatically when a valid URL is set.
      */
     public boolean isRetrievable() {
         return retrievable;
     }
 
     /**
      * Set the retrievable flag (see {@link #isRetrievable()})
      *
      * @param retrievable True if the file can be retrieved automatically when a valid URL is set.
      */
     public void setRetrievable(boolean retrievable) {
         this.retrievable = retrievable;
     }
 
     /**
      * Flag indicating if the file should be handled as a freemaker template
      *
      * @return True if the file is a freemarker template
      */
     public boolean isTemplate() {
         return template;
     }
 
     public void setTemplate(boolean template) {
         this.template = template;
     }
 
     /**
      * Specify the file character encoding. This is generally only needed if the file is a template.
      *
      * @return File encoding.
      */
     public String getEncoding() {
         return encoding;
     }
 
     public void setEncoding(String encoding) {
         this.encoding = encoding;
     }
 
     public String getSourceUrl() {
         return sourceUrl;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof FileDefinition)) return false;
 
         FileDefinition that = (FileDefinition) o;
 
         if (retrievable != that.retrievable) return false;
         if (template != that.template) return false;
         if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;
         if (path != null ? !path.equals(that.path) : that.path != null) return false;
         if (sha1 != null ? !sha1.equals(that.sha1) : that.sha1 != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
 
        return true;
     }
 
     @Override
     public int hashCode() {
         int result = path != null ? path.hashCode() : 0;
         result = 31 * result + (url != null ? url.hashCode() : 0);
         result = 31 * result + (sha1 != null ? sha1.hashCode() : 0);
         result = 31 * result + (retrievable ? 1 : 0);
         result = 31 * result + (template ? 1 : 0);
         result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
         return result;
     }
 }
