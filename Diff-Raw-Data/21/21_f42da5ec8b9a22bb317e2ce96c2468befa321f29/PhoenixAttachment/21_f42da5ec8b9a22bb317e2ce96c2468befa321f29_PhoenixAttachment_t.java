 /*
  * Copyright (C) 2013 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.rs.entity;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Date;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 /**
  * Wrapper class for a Attachment to communicate with the phoenix RS service.
  */
 public class PhoenixAttachment {
 
     private byte[] content;
     private Date creationDate;
     private String name;
     private String type;
 
     /**
      * Empty constructor for json transport
      */
     protected PhoenixAttachment() {
 
     }
 
     /**
      * Constructor for server
      * 
      * @param content
      * @param creationDate
      * @param name
      * @param type
      */
     public PhoenixAttachment(byte[] content, Date creationDate, String name, String type) {
         this.content = content;
         this.creationDate = creationDate;
         this.name = name;
         this.type = type;
     }
 
     /**
      * Constructor for the client.
      * 
      * @param file
      *            The file containing binary data
      * @param creationDate
      *            The creation date of the file
      * @throws IOException
      * @deprecated Use {@link #PhoenixAttachment(File, String)} instead to avoid
      *             wrong filenames
      */
     public PhoenixAttachment(File file) throws IOException {
         this(file, file.getName());
     }
 
     /**
      * Constructor for client. Creates an phoenix attachment and capsulates a
      * binary file
      * 
      * @param file
      *            The file containing bytes
      * @param name
      *            The name of the attachment
      * @throws IOException
      */
     public PhoenixAttachment(File file, String name) throws IOException {
         read(file);
 
         this.name = name;
         int fileSeperator = name.lastIndexOf('.');
         if (fileSeperator == -1) {
             this.type = "";
         } else {
             this.type = name.substring(fileSeperator + 1);
             this.name = name.substring(0, fileSeperator);
         }
     }
 
     private void read(File file) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         this.content = new byte[(int) file.length()];
         fis.read(this.content);
         fis.close();
     }
 
     /**
     * Creates an temporary file containing the bytes of the attachment. The
     * file will deleted automatically when the VM terminates
      * 
      * @return A temporary file
      * @throws IOException
      */
     @JsonIgnore
     public File getFile() throws IOException {
        // Temporary files must be at least 3 chars long
        String name = this.name;
        for (int i = name.length(); i < 3; ++i) {
            name = name + "a";
        }
         File f = File.createTempFile(name, type);
         f.deleteOnExit();
         FileOutputStream fout = new FileOutputStream(f);
         fout.write(content);
         fout.close();
         return f;
     }
 
     /**
      * @return A stream on the bytes
      */
     @JsonIgnore
     public InputStream getStream() {
         return new ByteArrayInputStream(content);
     }
 
     /**
      * @return The name of the attachment
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return The type ending of the attachment
      */
     public String getType() {
         return type;
     }
 
     /**
      * @return The name concated with its type
      */
     @JsonIgnore
     public String getFullname() {
         if (type == null || type.isEmpty())
             return getName();
         else
             return name + "." + type;
     }
 
     /**
      * @return Attachments creation date
      */
     public Date getCreationDate() {
         return creationDate;
     }
 
     /**
      * WARNING! This will COPY the content! Depending on the size of the
      * attachments, this will take a while!
      * 
      * @return A copy of the content
      */
     public byte[] getContent() {
         return Arrays.copyOf(content, content.length);
     }
 
     /**
      * 
      * @return The size of the content
      */
     @JsonIgnore
     public int getContentSize() {
         return content.length;
     }
 }
