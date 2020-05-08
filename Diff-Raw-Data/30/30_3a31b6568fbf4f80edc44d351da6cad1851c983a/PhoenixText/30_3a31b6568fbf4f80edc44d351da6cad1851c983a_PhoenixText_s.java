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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.joda.time.DateTime;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 import de.phoenix.filter.EduFilter;
 import de.phoenix.filter.HTMLFilter;
 import de.phoenix.filter.TextFilter;
 import de.phoenix.rs.key.Key;
 import de.phoenix.rs.key.PhoenixEntity;
 
 /**
  * Wrapper class for a Text to communicate with the phoenix RS service.
  */
 public class PhoenixText implements PhoenixEntity {
 
     /** URI of the text resource */
     public static final String WEB_RESOURCE_ROOT = "text";
 
     /** Sub URI of the text resource to update a single text */
     public static final String WEB_RESOURCE_UPDATE = "update";
 
     /** Sub URI of the text resource to delete a single text */
     public static final String WEB_RESOURCE_DELETE = "delete";
 
     private String text;
     @Key
     private DateTime creationDate;
     @Key
     private String name;
     @Key
     private String type;
 
     /**
      * Empty constructor for json transport
      */
     protected PhoenixText() {
 
     }
 
     /**
      * Constructor for server side
      * 
      * @param text
      * @param creationDate
      * @param name
      * @param type
      */
     public PhoenixText(String text, DateTime creationDate, String name, String type) {
         this.text = text;
         this.creationDate = creationDate;
         this.name = name;
         this.type = type;
     }
 
     /**
      * Constructor for client side. Creationdate is not saved in unix files, so
      * you have to give it manually
      * 
      * @param file
      *            The file containing the text
      * @param creationDate
      *            Creation date of the file
      * @throws IOException
      * 
      * 
      * @deprecated Use {@link #PhoenixText(File, String)} instead to avoid wrong
      *             filenames
      */
     public PhoenixText(File file) throws IOException {
         read(file);
 
         this.name = file.getName();
         int fileSeperator = name.lastIndexOf('.');
         if (fileSeperator == -1) {
             this.type = "";
         } else {
             this.type = name.substring(fileSeperator + 1);
             name = name.substring(0, fileSeperator);
         }
     }
 
     /**
      * Constructor for client. The date is set by the server!
      * 
      * @param text
      *            The content of this text
      * @param name
      *            The name of the text file
      * @param type
      *            The file ending
      * 
      * 
      * @deprecated Use {@link #PhoenixText(File, String)} instead to avoid wrong
      *             filenames and wrong types
      */
     public PhoenixText(String text, String name, String type) {
         this.text = text;
         this.name = name;
         this.type = type;
     }
 
     /**
      * Constructor for client. Creates an phoenix text and capsulates a text
      * file
      * 
      * @param text
      *            The content of the {@link PhoenixText}
      * @param fileName
      *            The name of this text
      */
     public PhoenixText(String text, String fileName) {
         this.text = text;
         readFilenameAndType(fileName);
     }
 
     /**
      * Constructor for client. Creates an phoenix text and capsulates a text
      * file
      * 
      * @param File
      *            File containing the text
      * @param fileName
      *            The name of this text
      * @throws IOException
      */
     public PhoenixText(File file, String fileName) throws IOException {
         this.text = read(file);
         readFilenameAndType(fileName);
     }
 
     private String read(File file) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         byte[] tmp = new byte[(int) file.length()];
         fis.read(tmp);
         fis.close();
 
         return new String(tmp);
     }
 
     private void readFilenameAndType(String fileName) {
         int fileSeperator = fileName.lastIndexOf('.');
         if (fileSeperator == -1) {
             this.type = "";
         } else {
             this.type = fileName.substring(fileSeperator + 1);
             this.name = fileName.substring(0, fileSeperator);
         }
     }
 
     /**
      * @return The text of the file
      */
     public String getText() {
         return text;
     }
 
     /**
      * Filter the text using {@link TextFilter}. <br>
      * For example the {@link HTMLFilter} removes all html tags <br>
      * Use for Edu content the {@link EduFilter} <br>
      * This remove a new text and does not change this text!
      * 
      * @param remover
      *            Filter to apply on this text
      * @return The filtered text
      */
     public String filterText(TextFilter... remover) {
         String original = text;
         for (int i = 0; i < remover.length; i++) {
             TextFilter stringRemover = remover[i];
             original = stringRemover.filter(original);
         }
         return original;
     }
 
     /**
      * Creates an temporary file containing text of the attachment. The file
      * will deleted automatically when the VM terminates
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
         fout.write(text.getBytes());
         fout.close();
         return f;
     }
 
     /**
      * @return The name of the text
      */
     public String getName() {
         return name;
     }
 
     /**
      * @return The type ending of the text
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
      * @return The creation date of the text
      */
     public DateTime getCreationDate() {
         return creationDate;
     }
 
     public static WebResource updateResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_UPDATE);
     }
 
     public static WebResource deleteResource(Client client, String baseURL) {
         return base(client, baseURL).path(WEB_RESOURCE_DELETE);
     }
 
     private static WebResource base(Client client, String baseURL) {
         return client.resource(baseURL).path(WEB_RESOURCE_ROOT);
     }
 
     @Override
     public String toString() {
        return String.format("PhoenixText={Name=%s;Type=%s;CreationDate=%s;Text=%i}", name, type, creationDate, text);
     }
 }
