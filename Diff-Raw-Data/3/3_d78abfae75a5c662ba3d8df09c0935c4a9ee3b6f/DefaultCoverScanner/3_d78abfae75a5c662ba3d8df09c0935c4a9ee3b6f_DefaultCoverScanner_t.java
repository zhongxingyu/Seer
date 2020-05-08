 package com.quiltplayer.core.scanner.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.quiltplayer.core.scanner.CoverScanner;
 import com.quiltplayer.core.scanner.ScanningEvent;
 import com.quiltplayer.core.scanner.ScanningEvent.Scanner;
 import com.quiltplayer.core.scanner.ScanningEvent.Status;
 import com.quiltplayer.core.storage.ArtistStorage;
 import com.quiltplayer.core.storage.Storage;
 import com.quiltplayer.external.covers.DiscogsScanner;
 import com.quiltplayer.external.covers.exception.RequestOverFlowException;
 import com.quiltplayer.external.covers.model.LocalImage;
 import com.quiltplayer.model.Album;
 import com.quiltplayer.properties.Configuration;
 import com.quiltplayer.view.swing.listeners.ScanningListener;
 
 /**
  * Default implementation of CollectionScanner. Scans the whole collection. TODO Refactor and split
  * so the pool is in a other class with ability to know when all threads are done.
  * 
  * @author Vlado Palczynski
  */
 @Component
 public class DefaultCoverScanner implements CoverScanner {
 
     private Logger log = Logger.getLogger(DefaultCoverScanner.class);
 
     private static final int THREADS = 10;
 
     private static List<Thread> threadPool;
 
     @Autowired
     private DiscogsScanner discogsScanner;
 
     @Autowired
     private Storage storage;
 
     @Autowired
     private ArtistStorage artistStorage;
 
     @Autowired
     private ScanningListener scanningListener;
 
     protected static Stack<Album> albumsToScan;
 
     /*
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run() {
         boolean b = true;
         Album album = null;
 
         while (b) {
             album = getAlbum();
 
             if (album != null) {
                 // Only scan albums without covers, the other should be scanned
                 // individually
                 if (album.getImages() != null && album.getImages().size() < 2) {
                     try {
 
                         log.debug("Searching for images....");
 
                         com.quiltplayer.external.covers.discogs.Album discogsAlbum = discogsScanner
                                 .scanForAlbum(album.getArtist().getArtistName().getName(), album
                                         .getTitle(), album.getSongCollection().getSongs().size(),
                                         new File(Configuration.ALBUM_COVERS_PATH));
 
                         if (discogsAlbum != null)
                             assemble(album, discogsAlbum);
 
                     }
                     catch (RequestOverFlowException e) {
                         /* Quit */
                         log.debug("Maximum requests, interrupting thread...");
                         b = false;
                     }
                     catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
             else
                 b = false;
         }
     }
 
     /**
 	 * 
 	 */
     private void assemble(Album album, com.quiltplayer.external.covers.discogs.Album discogsAlbum) {
         try {
             bindImagesToAlbum(album, discogsAlbum);
         }
         catch (MalformedURLException e) {
             e.printStackTrace();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         album.setYear(discogsAlbum.getYear());
 
         if (discogsAlbum.getLabels() != null && discogsAlbum.getLabels().size() > 0)
             album.setLabel(discogsAlbum.getLabels().get(0).getName());
     }
 
     /**
      * Create images.
      * 
      * @param album
      * @param selectedRelease
      * @throws MalformedURLException
      * @throws IOException
      */
     private void bindImagesToAlbum(final Album album,
             final com.quiltplayer.external.covers.discogs.Album discogsAlbum)
             throws MalformedURLException, IOException {
 
         if (discogsAlbum.getImages() != null || discogsAlbum.getImages().size() > 0) {
             album.deleteImages();
 
             for (LocalImage localImage : discogsAlbum.getImages()) {
 
                 // Indexing in neo split on some chars.
                 String fileName = localImage.getLargeImage().getName().replace("-", "").replace(
                         ".", "");
 
                 LocalImage storageImage = storage.getLocalImage(fileName);
 
                 if (storageImage == null) {
                     storageImage = storage.createLocalImage(album, fileName, localImage);
                 }
             }
         }
     }
 
     /*
      * @see org.coverrock.CollectionScanner#scanCollection()
      */
     @Override
     public void scanCovers() {
         albumsToScan = storage.getAlbumsAsStack(artistStorage.getArtists());
 
         threadPool = new ArrayList<Thread>();
 
         scanningListener.scannerEvent(new ScanningEvent(Status.STARTED, Scanner.COVERS));
 
         for (int i = 0; i < THREADS; i++) {
 
             DefaultCoverScanner das = new DefaultCoverScanner();
             das.artistStorage = artistStorage;
             das.discogsScanner = discogsScanner;
             das.storage = storage;
 
             Thread thread = new Thread(das);
             threadPool.add(thread);
             thread.start();
 
             log.debug("Thread " + thread.getId() + " is starting...");
         }
     }
 
     /*
      * @see org.coverrock.CollectionScanner#scanCollection()
      */
     @Override
     public void scanCovers(Album album) {
 
         albumsToScan = new Stack<Album>();
         albumsToScan.push(album);
 
         threadPool = new ArrayList<Thread>();
 
         for (int i = 0; i < 1; i++) {
 
             DefaultCoverScanner dcs = new DefaultCoverScanner();
            dcs.artistStorage = artistStorage;
            dcs.discogsScanner = discogsScanner;
            dcs.storage = storage;
 
             Thread thread = new Thread(dcs);
             threadPool.add(thread);
             thread.start();
 
             log.debug("Thread " + thread.getId() + " is starting...");
         }
     }
 
     private synchronized Album getAlbum() {
         if (!albumsToScan.isEmpty())
             return albumsToScan.pop();
 
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.quiltplayer.core.scanner.CoverScanner#cancelScanCovers()
      */
     @Override
     public void cancelScanCovers() {
         scanningListener.scannerEvent(new ScanningEvent(Status.DONE, Scanner.COVERS));
 
         for (Thread thread : threadPool) {
             thread.stop();
 
             System.out.println("Thread " + thread.getId() + " is stopped.");
         }
     }
 
     /**
      * @param albumsToScan
      *            the albumsToScan to set
      */
     public final void setAlbumsToScan(Stack<Album> albumsToScan) {
         DefaultCoverScanner.albumsToScan = albumsToScan;
     }
 }
