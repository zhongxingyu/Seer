 /*
  * Copyright 2013 Ralph Jones
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.totalchange.bunman.jb7;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.totalchange.bunman.Catalogue;
 import com.totalchange.bunman.CatalogueSongListener;
 import com.totalchange.bunman.cddb.CddbQuerier;
 import com.totalchange.bunman.cddb.CddbResult;
 
 final class Jb7Catalogue implements Catalogue {
     private static final String CDDB_DATA_CATEGORY = "data";
     private static final String SPLITTER_CDDB = "/";
     private static final String SPLITTER_DIRNAME = "  ";
 
     private static final Logger logger = LoggerFactory
             .getLogger(Jb7Catalogue.class);
 
     private IdnFileAlbumCache cache;
     private CddbQuerier querier;
     private File root;
 
     public Jb7Catalogue(IdnFileAlbumCache cache, CddbQuerier querier, File root) {
         this.cache = cache;
         this.querier = querier;
         this.root = root;
     }
 
     private void processAlbumData(CatalogueSongListener listener, Album album,
             boolean ignoreTrackNum, File dir, File... ignored) {
         FileFinder fileFinder = new FileFinder(dir.listFiles(), ignored);
         for (int num = 0; num < album.getTracks().size(); num++) {
             String track = album.getTracks().get(num);
 
             File file = fileFinder.findTrackFile(track);
             if (file != null) {
                 int trackNum = -1;
                 if (!ignoreTrackNum) {
                     trackNum = num + 1;
                 }
                 listener.yetAnotherSong(new Jb7Song(album, trackNum, track,
                         file));
             } else {
                 // TODO: Internationalise
                 listener.warn("Couldn't find a file for " + "track '" + track
                         + "' from album '" + album.getAlbum() + "' by artist '"
                         + album.getArtist() + "' in directory "
                         + dir.getAbsolutePath());
             }
         }
     }
 
     private void processIdDir(File dir, File idFile,
             CatalogueSongListener listener) {
         try {
             IdFileAlbum idf = new IdFileAlbum(idFile);
             processAlbumData(listener, idf, false, dir, idFile);
         } catch (IOException ioEx) {
             listener.warn("Couldn''t read ID file " + idFile + ": "
                     + ioEx.getLocalizedMessage());
         }
     }
 
     private String readIdValue(File file) throws IOException {
         logger.trace("Reading id string from file {}", file);
 
         BufferedReader in = new BufferedReader(new FileReader(file));
         try {
             String id = in.readLine();
             logger.trace("Raw id value is {}", id);
 
             if (id != null) {
                 id = id.trim();
             }
 
             if (id != null && id.length() <= 0) {
                 id = null;
             }
 
             logger.trace("Parsed id value is {}", id);
             return id;
         } finally {
             in.close();
         }
     }
 
     private void addItemToQueueAndCacheIt(CatalogueSongListener listener,
             File file, String id, IdnFileAlbum idnf) {
         cache.putFileIntoCache(id, file.getParentFile().getName(), idnf);
         processAlbumData(listener, idnf, false, file.getParentFile(), file);
     }
 
     private boolean equalsIgnoreCase(String[] arr1, String[] arr2) {
         if (arr1.length != arr2.length) {
             return false;
         }
 
         for (int num = 0; num < arr1.length; num++) {
             if (!arr1[num].equalsIgnoreCase(arr2[num])) {
                 return false;
             }
         }
 
         return true;
     }
 
     private void processQueryResults(CatalogueSongListener listener, File file,
             String id, List<CddbResult> results) {
         if (logger.isTraceEnabled()) {
             logger.trace("Got CDDB results for file " + file + ", id " + id
                     + ": " + results);
         }
 
         if (results.size() <= 0) {
             logger.trace("No results - need to report as a problem");
             // TODO: Internationalise
             listener.warn("Failed to find any CDDB info for id " + id
                     + " in directory " + file.getParent());
             return;
         }
 
         if (results.size() == 1) {
             logger.trace("Only one result, adding it to the queue");
             addItemToQueueAndCacheIt(listener, file, id, new IdnFileAlbum(file,
                     results.get(0)));
             return;
         }
 
         // Got more than 1 result - remove any that don't match on artist and
         // title (and skip any in the "data" category).
         String[] dirSplit = Jb7Utils.splitArtistAlbumBits(file.getParentFile()
                 .getName(), SPLITTER_DIRNAME);
         List<CddbResult> matches = new ArrayList<CddbResult>();
         for (CddbResult cddb : results) {
             String[] resSplit = Jb7Utils.splitArtistAlbumBits(cddb.getTitle(),
                     SPLITTER_CDDB);
             if (!cddb.getCategory().equalsIgnoreCase(CDDB_DATA_CATEGORY)
                     && equalsIgnoreCase(dirSplit, resSplit)) {
                 matches.add(cddb);
             }
         }
 
         if (matches.size() >= 1) {
             logger.trace("Found a match for {}: {}", (Object[]) dirSplit,
                     matches);
             addItemToQueueAndCacheIt(listener, file, id, new IdnFileAlbum(file,
                     matches.get(0)));
             return;
         }
 
         // Bums
        // TODO: sort out a way of flagging multiple possibilities to the user
         // TODO: Internationalise
         listener.warn("Couldn't find any suitable CDDB info " + "for id " + id
                 + " in directory " + file.getParent()
                 + " out of possible matches " + results
                 + ". Fallen back to file names.");
         processAlbumData(listener, new NoFileAlbum(file.getParentFile()), true,
                 file.getParentFile(), file);
     }
 
     private void processIdnFile(final File file,
             final CatalogueSongListener listener) {
         logger.trace("Processing idn file {}", file);
 
         final String id;
         try {
             id = readIdValue(file);
         } catch (IOException ioEx) {
             // TODO: Internationalise
             listener.warn("Failed to read an ID value from " + file
                     + " with error " + ioEx.getMessage());
             return;
         }
 
         if (id == null) {
             // TODO: Internationalise
             listener.warn(file + " does not contain an ID value");
             return;
         }
 
         logger.trace("Looking up from cache based on id {}", id);
         IdnFileAlbum idnf = cache.getFileFromCache(id, file.getParentFile()
                 .getName());
         if (idnf != null) {
             logger.trace("Got result {} from cache", idnf);
             idnf.setIdnFile(file);
             processAlbumData(listener, idnf, false, file.getParentFile(), file);
         }
 
         logger.trace("Querying for CDDB results for id {}", id);
         querier.query(id, new CddbQuerier.Listener() {
             public void response(List<CddbResult> results) {
                 processQueryResults(listener, file, id, results);
             }
 
             public void error(IOException exception) {
                 listener.warn(exception.getMessage());
             }
         });
     }
 
     private void recurseForIdFiles(File dir, CatalogueSongListener listener) {
         File idFile = new File(dir, "id");
         if (idFile.exists()) {
             processIdDir(dir, idFile, listener);
         } else {
             File idnFile = new File(dir, "idn");
             if (idnFile.exists()) {
                 processIdnFile(idnFile, listener);
             }
         }
 
         for (File subDir : dir.listFiles()) {
             if (subDir.isDirectory()) {
                 recurseForIdFiles(subDir, listener);
             }
         }
     }
 
     public void listAllSongs(CatalogueSongListener listener) {
         recurseForIdFiles(root, listener);
 
         // Wait for IDN factory to finish any background processing - shouldn't
         // really throw an error...
         try {
             querier.close();
         } catch (IOException ioEx) {
             listener.warn(ioEx.getMessage());
             logger.warn("A problem occurred waiting for the CDDB querier to "
                     + "close", ioEx);
         }
     }
 }
