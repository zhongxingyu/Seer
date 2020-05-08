 /*
  Copyright (c) 2011 Bit Bunker
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
  */
 package com.bitbunker;
 
 import com.bitbunker.api.Client;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Date;
import javax.smartcardio.ATR;
 
 /**
  * File represents a file stored in BitBunker
  *
  * @author Chris Umbel
  * @version     %I%, %G%
  *
  */
 public class File extends BitBunkerObject {
     public enum Status {
         QUEUED,
         PULLING,
         RECEIVED,
         PULL_FAILED,
         UNKNOWN
     };
 
     public enum Durability {
         PARTIAL,
         COMPLETE,
         UNKOWN
     };
 
     private Date created;
     private boolean received;
     private boolean inCache;
     private Durability durability;
     private Status status;
 
     /**
      * create a File object representing a file in BitBunker and connect it
      * to the service
      *
      * @param path          path to the file in BitBunker
      * @param client        client used for authentication and connectivity
      * 
      */
     public File(String path, Client client) {
         this(path);
         this.setClient(client);
     }
 
     /**
      * create a File object representing a file in BitBunker at a given path
      *
      * @param path          path to the file in BitBunker
      * 
      */
     public File(String path) {
         super(path);
         this.name = new java.io.File(path).getName();
     }
 
     /**
      * create a File object representing a file in BitBunker
      */
     public File() {
         super();
     }
 
     /**
      * set the transfer status of the File
      *
      * @param statusString   transfer status of the file. parsed into an item
      *                       from the File.Status enum
      */
     public void setStatus(String statusString) {
         this.setStatus(File.parseStatus(statusString));
     }
 
     /**
      * parse a transfer status string into an item from the File.Status enum enum
      *
      * @param statusString      string representation of a transfer status
      * @return an item from the File.Status enum enum
      */
     public static File.Status parseStatus(String statusString) {
         if(statusString.equals("Received"))
             return File.Status.RECEIVED;
         else if(statusString.equals("PullFailed"))
             return File.Status.PULL_FAILED;
         else if(statusString.equals("Pulling"))
             return File.Status.PULLING;
         else if(statusString.equals("Queued"))
             return File.Status.QUEUED;
         else
             return File.Status.UNKNOWN;
     }
 
     private boolean handleStatusAttribute(StringBuilder attributes) {
         switch (this.getStatus()) {
             case QUEUED:
                 attributes.append("Q");
                 break;
             case PULLING:
                 attributes.append("P");
                 break;
             case RECEIVED:
                 attributes.append("R");
                 break;
             default:
                 attributes.append("err");
                 return false;
         }
 
         return true;
     }
 
     private void handleDurabilityAttribute(StringBuilder attributes) {
         switch(this.getDurability()) {
             case COMPLETE:
                 attributes.append("D");
                 break;
             case PARTIAL:
                 attributes.append("d");
                 break;
             default:
                 attributes.append("-");
         }
     }
 
     /**
      * get a summary string of the attributes of a file
      *
      */
     public String getAttributes() {
         StringBuilder attributes = new StringBuilder();
         
         if(this.handleStatusAttribute(attributes)) {
             attributes.append(this.isInCache() ? "C" : "-");
             this.handleDurabilityAttribute(attributes);
         }
 
         return attributes.toString();
     }
 
     /**
      * see if the file is stored in ephemeral disk cache
      *
      * @return weather the file is stored in ephemeral disk cache
      * 
      */
     public boolean isInCache() {
         return inCache;
     }
 
     /**
      * see if the file is stored in ephemeral disk cache
      *
      * @return weather the file is stored in ephemeral disk cache
      *
      */
     public void setInCache(boolean inCache) {
         this.inCache = inCache;
     }
 
     /**
      * get the transfer status of the file
      *
      * @return the transfer status of the file
      * 
      */
     public Status getStatus() {
         return status;
     }
 
     /**
      * get a string representing the transfer status of a file
      *
      * @return a string representing the transfer status of a file
      * 
      */
     public String getStatusString() {
         switch(status) {
             case QUEUED:
                 return "Queued";
             case RECEIVED:
                 return "Received";
             case PULLING:
                 return "Pulling";
             case PULL_FAILED:
                 return "PullFailed";
         }
         
         return "Unkown";
     }
 
     /**
      * set the transfer status of a File
      *
      * @param status        transfer status of the file
      */
     public void setStatus(Status status) {
         this.status = status;
     }
 
     /**
      * determine weather or not the file achieved the desired durability (has
      * it been copied to as many volumes as the user desired)
      * 
      * @return weather or not the file achieved the desired durability if known
      * 
      */
     public Durability getDurability() {
         return this.durability;
     }
 
     /**
      * set weather or not the file achieved the desired durability (has
      * it been copied to as many volumes as the user desired)
      *
      * @param durability            weather or not the file achieved the desired
      *                              durability if known
      */
     public void setDurability(Durability durability) {
         this.durability = durability;
     }
 
     /**
      * determine if the file was received over the network from the user
      *
      * @return  if the file was received over the network from the user
      * 
      */
     public boolean isReceived() {
         return received;
     }
 
     /**
      * if the file was received over the network from the user
      *
      * @param received          if the file was received over the network
      *                          from the user
      */
     public void setReceived(boolean received) {
         this.received = received;
     }
 
     /**
      * get the date/time the file was created in BitBunker
      *
      * @return the date/time the file was created in BitBunker
      * 
      */
     public Date getCreated() {
         return created;
     }
 
     /**
      * set the date/time the file was created in BitBunker
      *
      * @param created           the date/time the file was created in BitBunker
      *
      */
     public void setCreated(Date created) {
         this.created = created;
     }
 
     /**
      * get the file form BitBunker and store it on the local filesystem at a
      * specified path
      *
      * @param localPath             location on the local filesystem to store the
      *                              file
      * @throws BitBunkerException
      * 
      */
     public void get(String localPath) throws BitBunkerException {
         checkConnected();
         this.getClient().get(this, localPath);
     }
 
     /**
      * get the file from BitBunker and hand off a stream to caller to handle
      * the data transmission
      *
      * @return                      stream of the file's data form BitBunker
      * @throws BitBunkerException
      * 
      */
     public InputStream openStream() throws BitBunkerException {
         checkConnected();
         return this.getClient().getStream(this);
     }
 
     /**
      * get the file from BitBunker, save it, and return a java.io.File pointer
      * to it
      *
      * @param localPath             location on the local filesystem to store
      *                              the file
      * @return                      <code>java.io.File</code> pointer to the file
      * @throws BitBunkerException
      * 
      */
     public java.io.File getfile(String localPath) throws BitBunkerException {
         checkConnected();
         return this.getClient().getFile(this, localPath);
     }
 
     /**
      * get the file from BitBunker and store it as the specified remote location
      *
      * @param address               remote address at which to store the file
      * @throws BitBunkerException
      * 
      */
     public void getTo(String address) throws BitBunkerException {
         checkConnected();
         this.getClient().getTo(this, address);
     }
 
     /**
      * get the file from BitBunker and store it as the specified remote location
      *
      * @param url                   remote address at which to store the file
      * @throws BitBunkerException
      *
      */
     public void getTo(URL url) throws BitBunkerException {
         checkConnected();
         this.getClient().getTo(this, url);
     }
 
     /**
      * delete the file from BitBunker
      *
      * @throws BitBunkerException
      * 
      */
     public void delete() throws BitBunkerException {
         checkConnected();
         this.getClient().delete(this);
     }
 
     /**
      * put a file into BitBunker
      *
      * @param localPath             location on the local filesystem of the file
      *                              to store in BitBunker
      * @throws BitBunkerException
      * 
      */
     public void put(String localPath) throws BitBunkerException {
         checkConnected();
         this.getClient().put(localPath, this);
     }
 
     /**
      * put a file into BitBunker
      *
      * @param localFile             file on the local filesystem to store in
      *                              BitBunker.
      * @throws BitBunkerException
      * 
      */
     public void put(java.io.File localFile) throws BitBunkerException {
         checkConnected();
         this.getClient().put(localFile, this);
     }
 
     /**
      * put a file into BitBunker
      *
      * @param is                    stream of data to store in BitBunker
      * @throws BitBunkerException
      * 
      */
     public void put(InputStream is) throws BitBunkerException {
         checkConnected();
         this.getClient().put(is, this);
     }
 
     /**
      * put a file into BitBunker from a remote location
      *
      * @param address               URL of remote file to store in BitBunker
      * @throws BitBunkerException
      *
      */
     public void putFrom(String address) throws BitBunkerException {
         checkConnected();
         this.getClient().putFrom(address, this);
     }
 
     /**
      * put a file into BitBunker from a remote location
      *
      * @param url                   URL of remote file to store in BitBunker
      * @throws BitBunkerException
      * 
      */
     public void putFrom(URL url) throws BitBunkerException {
         checkConnected();
         this.getClient().putFrom(url, this);
     }
 }
