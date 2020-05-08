 /*
  * $Id$
  *
  * Copyright © 2008,2009 Bjørn Øivind Bjørnsen
  *
  * This file is part of Quash.
  *
  * Quash is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Quash is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Quash. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.tracker.backend.webinterface;
 
 import com.tracker.backend.Bencode;
 import com.tracker.backend.StringUtils;
 import com.tracker.backend.entity.Torrent;
 import com.tracker.backend.webinterface.entity.TorrentContent;
 import com.tracker.backend.webinterface.entity.TorrentData;
 import com.tracker.backend.webinterface.entity.TorrentFile;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 
 /**
  * Responsible for adding torrents.
  * <p>May have to rewrite the torrentfile to accommodate for things like wrong
  * announce or the private flag being set. In these cases, the user should be
  * told to redownload the torrentfile before {s,}he starts seeding.</p>
  * <p>This class does not do any thorough validation of the torrentfile, beyond
  * some simple checks to see if some of the necessary data is there.</p>
  * <p>This class requires the Apache Commons FileUpload package.</p>
  * @author bo
  */
 public class TorrentUpload {
     static Logger log = Logger.getLogger(TorrentUpload.class.getName());
     static EntityManagerFactory emf = Persistence.createEntityManagerFactory("QuashPU");
 
     /**
      * A simple convenience class to make it easier to parse the Servlet request.
      * Static to illustrate that it is more or less independent of the outer
      * class.
      */
     public static class UnparsedTorrentData {
         public String name;
         public String description;
 
         public Map decodedTorrent;
     }
 
     /**
      * A convenience method for reconstructing the URL of the application. Used
      * to check against the Announce entries of the uploaded torrents.
      * We use this method instead of request.getRequestURL because it is simpler
      * than having to remove all the /TakeUpload bits.
      * @param request the request to reconstruct the URL of this web-app from.
      */
     static public String getURL(HttpServletRequest request) {
         String scheme = request.getScheme();             // http
         String serverName = request.getServerName();     // hostname.com
         int serverPort = request.getServerPort();        // 8080, if 80, disregard
         String contextPath = request.getContextPath();   // /mywebapp
 
         String URL = scheme + "://" + serverName;
         // disregard the port if it's 80, no need to add it
         // TODO: accept :80 in announce also?
         if(serverPort != 80) {
             URL += ":" + serverPort;
         }
 
         URL += contextPath;
 
         return URL;
     }
 
     /**
      * A convenience method to separate out the interesting information from
      * a HttpServletRequest.
      * @param request the HttpServletRequest to parse the information from.
      * The information sought is "torrentName", "torrentDescription" and the
      * torrent file itself.
      * @return a UnparsedTorrentData object containing the information parsed
      * from the given request.
      * @throws java.lang.Exception if the request is not a multipart request.
      */
     public UnparsedTorrentData getDataFromRequest(HttpServletRequest request) throws Exception {
         if(!ServletFileUpload.isMultipartContent(request)) {
             log.log(Level.SEVERE, "Request received by getDataFromRequest() not a multi-part request?");
             throw new Exception("Request not multi-part?");
         }
 
         UnparsedTorrentData data = new UnparsedTorrentData();
         ServletFileUpload upload = new ServletFileUpload();
         FileItemIterator itr = upload.getItemIterator(request);
 
         // set the maximum size of the torrentfile, avoid loading hundreds of
         // megs into memory. A size limit of 10MB seems decent enough (the
         // biggest .torrent files I could find through some quick searching were
         // just below 2MB.
         // TODO: read from config?
         upload.setFileSizeMax(10485760L);
 
         while(itr.hasNext()) {
             FileItemStream item = itr.next();
             String name = item.getFieldName();
             InputStream stream = item.openStream();
 
             // is it the name or description?
             if(item.isFormField()) {
                 if(name.equalsIgnoreCase("torrentName")) {
                     // grab content of field
                     data.name = Streams.asString(stream);
                 }
                 else if(name.equalsIgnoreCase("torrentDescription")) {
                     data.description = Streams.asString(stream);
                 }
                 // ignore the submit button, no logging for you!
                 else if(name.equalsIgnoreCase("torrentSubmit")) {}
                 // some weird data?
                 else {
                     log.log(Level.INFO, "Unknown field (" + name + ") in upload request?");
                 }
             }
             // file field
             else {
                 // This MUST be processed before hasNext is called next
                 data.decodedTorrent = (Map) Bencode.decode(stream).get(0);
             }
         }
 
         return data;
     }
 
     /**
      * Reads a torrent given in the request, makes changes if necessary, then
      * adds it to the database of tracked torrents.
      * @param decodedTorrent the torrent decoded from original bencoded form.
      * @param torrentDescription the torrent description to persist in the database.
      * @param torrentName the torrent name to persist in the database.
      * @param contextPath the context path of the running servlet. Used for
      * checking the announce keys in the torrent file.
      * @return a TreeMap populated with the result of the operation, plus
      * eventual warnings or error messages.
      * <p>The keys this may contain is:
      * <ul>
      * <li><b>"warning reason"</b>: a human readable string describing a warning
      * encountered while adding the torrentfile (wrong announce for example).
      * This is left empty if there is no warning given.</li>
      * <li><b>"error reason"</b>: a human readable description of why this torrent
      * could not be added to the database. This is left empty if there is no
      * errors.</li>
      * <li><b>"redownload"</b>: equals to "true" if the client needs to redownload
      * the torrentfile before {s,}he can begin seeding the torrent (for example
      * if the torrentfile had an incorrect announce URL, and was changed).
      * Equals to "false" if there is no reason to redownload the torrentfile.</li>
      * </ul>
      * @throws java.lang.Exception if there is some problem with the given
      * torrentfile or the persisting operation.
      */
     public static Map<String,String> addTorrent(Map decodedTorrent,
             String torrentName, String torrentDescription, String contextPath) throws Exception {
         Long torrentLength = Long.valueOf(0);
         Torrent t;
         TorrentData tData;
         TorrentFile tFile;
 
         // list of files and their lengths
         Vector<TorrentContent> torrentFiles = new Vector<TorrentContent>();
 
         // the URL of this trackers announce, used for comparison with the
         // URL given in the torrentfile.
         String ourAnnounce = contextPath + "/Announce";
 
         TreeMap<String,String> response = new TreeMap<String,String>();
         // set some default replies
         response.put("warning reason", "");
         response.put("error reason", "");
         response.put("redownload", "false");
 
         // set as null to avoid some nonsense when persisting, will be set
         // by the decoded torrent
         Map infoDictionary = null;
 
         // for generating the info_hash
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         
         // process the input stream
         try {
             /*
              * The torrent file layout is roughly like this:
              * Mandatory:
              * String       announce        (the URL of the tracker)
              * Dictionary   info            (describes the files of the torrent)
              *      Mandatory:
              *      Integer piece length    (number of bytes in each piece)
              *      String  pieces          (the SHA1 hashes of all the pieces)
              *
              *      Optional:
              *      Integer private         (determines if the torrent is private)
              *
              *      If this is a single-file torrent:
              *      Mandatory:
              *      String  name            (filename of the file)
              *      Integer length          (length of the file in bytes)
              *      Optional:
              *      String  md5sum          (MD5 sum of the file)
              *
              *      If this is a multiple-file torrent:
              *      Mandatory:
              *      String  name            (the name of the directory to store the files in)
              *      List of Dictionaries files (one for each file)
              *          Mandatory:
              *          Integer length      (length of the file in bytes)
              *          List    path        (a list of strings giving the path of the file)
              *          Optional:
              *          String  md5sum      (MD5 sum of the file)
              *      end of files
              * end of info
              * Optional:
              * List         announce-list   (list of list of trackers)
              * Integer      creation date   (the creation time of the torrent)
              * String       comment         (a comment to the torrent)
              * String       created-by      (gives the program used to create the torrent)
              *
              * The info hash used when tracking is the SHA1 hash of the
              * _value_ of the info key, in other words, a bencoded
              * dictionary.
              *
             * see: 
             *  - http://wiki.theory.org/BitTorrentSpecification
             *  - http://bittorrent.org/beps/bep_0003.html (bittorrent specs)
             *  - http://bittorrent.org/beps/bep_0005.html (DHT specs)
             *  - http://bittorrent.org/beps/bep_0012.html (multitracker specs)
              * for more information and links on optional keys like the
              * announce-list.
              */
 
             String announceURL;
 
             // make sure that the torrentfile contains a bare minimum
             // of data.
            if(!decodedTorrent.containsKey("info")) {
                 log.log(Level.WARNING, "Malformed torrentfile received." +
                        " missing 'info' dictionary.");
                 throw new Exception("The Torrentfile given in upload is malformed.");
             }
 
            // do we have an announce key?
            if(!decodedTorrent.containsKey("announce")) {
                // add the announce key and rewrite it below
                log.log(Level.FINE, "Uploaded torrent missing announce key");
                decodedTorrent.put("announce", "");
            }

             // make sure that we are the ones that track this torrent.
             announceURL = (String) decodedTorrent.get((String)"announce");
             if(!announceURL.equalsIgnoreCase(ourAnnounce) ||
                     decodedTorrent.containsKey("announce-list")) {
                 // check for the optional announce-list, if we are not
                 // given there either, or if this is not set, issue a
                 // warning and change the torrentfile.
                 if(!decodedTorrent.containsKey("announce-list")) {
                     // rewrite the standard announce key
                     log.log(Level.WARNING, "Uploaded torrent does not have" +
                         "our announce (given was: " + announceURL + ").");
                     decodedTorrent.put("announce", ourAnnounce);
 
                     // TODO: apply i10n here
                     response.put("warning reason", "The torrentfile did " +
                             "not contain the correct announce URL, this " +
                             "has been changed.\n");
                     response.put("redownload", "true");
                 }
                 // announce-list found. Makes the "announce" key
                 // irrelevant. See this for more info:
                 // http://home.elp.rr.com/tur/multitracker-spec.txt
                 else {
                     // does it contain our announce string?
                     boolean valid = false;
                     // grab the announce-list and check every element
                     // for our announce URL.
                     Vector<Vector<String> > announceList =
                             (Vector<Vector<String> >) decodedTorrent.get("announce-list");
                     Iterator announceListItr = announceList.iterator();
 
                     while(announceListItr.hasNext()) {
                         Vector<String> innerList =
                                 (Vector<String>) announceListItr.next();
                         Iterator innerItr = innerList.iterator();
 
                         while(innerItr.hasNext()) {
                             String announce = (String) innerItr.next();
                             if(announce.equalsIgnoreCase(ourAnnounce)) {
                                 valid = true;
                                 break;
                             }
                         }
 
                         if(valid)
                             break;
                     }
                     if(!valid) {
                         log.log(Level.WARNING, "Uploaded torrent does not " +
                                 "have our announce in announce-list (given " +
                                 "was: " + announceList.toString() +").");
                         // add our announce URL to the top of the list
                         Vector<String> prependedAnnounce = new Vector<String>();
                         prependedAnnounce.add(ourAnnounce);
                         announceList.add(0, prependedAnnounce);
 
                         // TODO: apply i10n
                         response.put("warning reason", "The torrentfile " +
                                 "did not contain the correct announce URL " +
                                 "in it's announce list. This has been changed.\n");
                         response.put("redownload", "true");
                     }
                 }
             } // if wrong announce or announce-list
 
             // check for the private setting
             infoDictionary = (Map) decodedTorrent.get("info");
 
             if(infoDictionary.containsKey("private")) {
                 Long privateField = (Long) infoDictionary.get("private");
                 // is private enabled?
                 if(privateField == 1) {
                     // remove the private key and set appropriate warnings
                     infoDictionary.remove("private");
 
                     log.log(Level.WARNING, "Torrent uploaded with private" +
                             "key enabled.");
                     String warningReason = response.get("warning reason");
                     warningReason += "The torrentfile was set as private, " +
                             "this has been removed.";
                     response.put("warning reason", warningReason);
                     response.put("redownload", "true");
                 }
             }
 
             // set the torrentlength
             // this is different if this is a single-file torrent or a
             // multi-file torrent. Test for both.
 
             // multi-file torrent - see diagram above
             if(infoDictionary.containsKey("files")) {
                 Vector<Map> files = (Vector<Map>) infoDictionary.get((String)"files");
                 Iterator fileItr = files.iterator();
 
                 while(fileItr.hasNext()) {
                     Map f = (Map) fileItr.next();
 
                     // grab length
                     Long length = (Long) f.get((String)"length");
                     torrentLength += length;
 
                     // grab path
                     Vector<String> filePath = (Vector<String>) f.get((String)"path");
                     Iterator pathItr = filePath.iterator();
                     StringBuilder path = new StringBuilder((String) infoDictionary.get((String)"name"));
 
                     // build up the path
                     while(pathItr.hasNext()) {
                         path.append('/');
                         path.append(pathItr.next());
                     }
 
                     TorrentContent c = new TorrentContent();
                     c.setFileName(path.toString());
                     c.setFileSize(length);
 
                     // populate the list
                     torrentFiles.add(c);
                 }
             }
             // single file torrent - see diagram above
             else {
                 String name = (String) infoDictionary.get((String)"name");
                 Long length = (Long) infoDictionary.get((String)"length");
 
                 torrentLength = length;
 
                 TorrentContent c = new TorrentContent();
                 c.setFileName(name);
                 c.setFileSize(length);
 
                 // populate the list
                 torrentFiles.add(c);
             }
         }
         catch(Exception ex) {
             log.log(Level.WARNING, "Error when decoding given torrent.", ex);
             throw new Exception("Error when decoding torrent given in upload", ex);
         }
 
         // persist!
         // add the torrent to the database.
         EntityManager em = emf.createEntityManager();
         // grab the SHA1 hash of the (bencoded) info dictionary.
         // the simplest way is to simply encode the info dictionary again
         // (things may have changed), then do a SHA1-hash of the result.
         String info = Bencode.encode(infoDictionary);
         byte[] rawInfo = new byte[info.length()];
         byte[] rawInfoHash = new byte[20];
         for (int i = 0; i < rawInfo.length; i++) {
             rawInfo[i] = (byte) info.charAt(i);
         }
         md.update(rawInfo);
         rawInfoHash = md.digest();
 
         String infoHash = StringUtils.getHexString(rawInfoHash);
         
         // look for the info hash in the database to avoid duplicates,
         // we can't have more than one torrent with a given info hash.
         Query q = em.createQuery("SELECT COUNT(t) FROM Torrent t WHERE " +
                 "t.infoHash = '" + infoHash + "'");
 
         Long numElements = (Long) q.getSingleResult();
         if(numElements >= 1) {
             response.put("error reason", "We are already tracking this torrent! " +
                     "Please download and seed the existing torrent instead.");
             return response;
         }
 
         try {
             t = new Torrent();
             tData = new TorrentData();
             tFile = new TorrentFile();
             
             // set the info hash.
             t.setInfoHash(infoHash);
             // num seeders and all that is set by the Torrent constructor.
             
             tData.setName(torrentName);
             tData.setDescription(torrentDescription);
             tData.setAdded(Calendar.getInstance().getTime());
             tData.setTorrentSize(torrentLength);
             tData.setTorrentContent(torrentFiles);
 
             t.setTorrentData(tData);
 
             // set the torrentfile
             String bencodedTorrent = Bencode.encode(decodedTorrent);
             tFile.setTorrentFile(bencodedTorrent);
 
             t.setTorrentFile(tFile);
 
             // persist this
             em.getTransaction().begin();
             em.persist(t);
             // torrentData, torrentFile and torrentContent is automatically
             // persisted through the Cascade operations specified in the
             // entity classes.
 
             em.getTransaction().commit();
         }
         catch(Exception ex) {
             if(em.getTransaction().isActive()) {
                 em.getTransaction().rollback();
             }
             log.log(Level.WARNING, "Error when persisting the uploaded torrent.", ex);
             throw new Exception("Error when persising torrent given in upload", ex);
         }
         finally {
             em.close();
         }
 
         return response;
     }
 }
