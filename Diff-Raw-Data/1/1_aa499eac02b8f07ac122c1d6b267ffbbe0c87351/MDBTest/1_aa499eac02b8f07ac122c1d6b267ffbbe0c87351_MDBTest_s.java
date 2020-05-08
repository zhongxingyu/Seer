 /*
  * Copyright (c) 2011 Alexander Dovzhikov <alexander.dovzhikov@gmail.com>.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY ALEXANDER DOVZHIKOV ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL dovzhikov OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * MDBTest.java
  *
  * Created on 12.10.2011 15:57:07
  */
 
 package dan.vjtest.sandbox.mdb;
 
 import dan.vjtest.sandbox.swing.util.UsualApp;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Alexander Dovzhikov
  */
 public class MDBTest {
     private static final Logger log = LoggerFactory.getLogger(MDBTest.class);
 
     private static final Pattern ALBUM_PATTERN = Pattern.compile("(\\d{4})\\s+(.*)");
 
     private final File path;
     private JProgressBar buildProgress;
     private JProgressBar artistAnalyzeProgress;
 
     public MDBTest(String path) {
         this.path = new File(path);
     }
 
     public void go() {
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 @Override
                 public void run() {
                     JPanel content = new JPanel(new GridLayout(5, 1));
                     buildProgress = new JProgressBar();
                     buildProgress.setStringPainted(true);
                     buildProgress.setValue(0);
                     buildProgress.setIndeterminate(false);
                     artistAnalyzeProgress = new JProgressBar();
                     artistAnalyzeProgress.setStringPainted(true);
                     artistAnalyzeProgress.setValue(0);
                     artistAnalyzeProgress.setIndeterminate(false);
 
                     content.add(buildProgress);
                     content.add(artistAnalyzeProgress);
 
                     UsualApp app = new UsualApp("MDB Test");
                     app.setContent(content);
                     app.start();
                 }
             });
         } catch (InterruptedException | InvocationTargetException e) {
             log.error(e.getMessage(), e);
         }
 
         FSFactory factory = new FSFactory();
 
         long start = System.currentTimeMillis();
         DirectoryInfo dirInfo = (DirectoryInfo) factory.createFSEntry(path, new ProgressReporter(buildProgress));
         long end = System.currentTimeMillis();
         log.info("Directory info built in: {} ms", end - start);
 
         List<DirectoryInfo> dirs = new ArrayList<DirectoryInfo>();
 
         for (FSEntry fsEntry : dirInfo.children()) {
             if (fsEntry.isDirectory()) {
                 dirs.add((DirectoryInfo) fsEntry);
             } else {
                 log.warn("Unexpected file: {}", fsEntry);
             }
         }
 
         factory.analyzeArtists(dirs, new ProgressReporter(artistAnalyzeProgress));
     }
 
     public void go2() {
         List<File> dirs = new ArrayList<File>();
         List<File> files = new ArrayList<File>();
 
         for (File f : path.listFiles()) {
             if (f.isDirectory()) {
                 dirs.add(f);
             } else if (f.isFile()) {
                 files.add(f);
             }
         }
 
         for (File f : files) {
             log.warn("Unexpected file: {}", f);
         }
 
         int tempCounter = 0;
 
         for (File dir : dirs) {
 //            if (tempCounter++ > 0)
 //                return;
 
             processArtist(dir);
         }
     }
 
     private void processArtist(File dir) {
         log.info("Processing directory: {}", dir);
 
         String artist = dir.getName();
 
         if (isSpecialArtistDir(artist)) {
             log.debug("Skipping special dir: {}", artist);
         } else {
             log.debug("Artist: {}", artist);
 
             int tempCounter = 0;
 
             for (File f : dir.listFiles()) {
 //                if (tempCounter++ > 0)
 //                    return;
 
                 processAlbumDir(f, artist);
             }
         }
 
     }
 
     private void processAlbumDir(File albumDir, String artist) {
         // album name must match %year %title
         String albumDirName = albumDir.getName();
         log.debug("Trying to check album '{}'", albumDirName);
 
         Matcher matcher = ALBUM_PATTERN.matcher(albumDirName);
 
         if (matcher.matches()) {
             MatchResult matchResult = matcher.toMatchResult();
             int groupCount = matchResult.groupCount();
             log.trace("group count: {}", groupCount);
 
             if (groupCount == 2) {
                 String yearText = matchResult.group(1);
                 String title = matchResult.group(2);
 
                 try {
                     int year = Integer.parseInt(yearText);
 
                     log.debug("Year: {}, title: '{}'", year, title);
                 } catch (NumberFormatException e) {
                     log.error("Incorrect album year: {}", yearText);
                 }
             } else {
                 log.error("Invalid group count: {}", groupCount);
             }
         } else {
             log.error("Invalid album directory: {}", albumDirName);
         }
     }
 
     private boolean isSpecialArtistDir(String dirName) {
         return "__NEW".equals(dirName) || "Various Artists".equals(dirName);
     }
 
     public static void main(String[] args) {
         if (args.length < 1) {
             log.error("Provide an argument: path");
             System.exit(-1);
         }
 
         log.debug("Current thread name: {}", Thread.currentThread().getName());
 
 /*
         LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
         StatusPrinter.print(lc);
 */
 
         MDBTest app = new MDBTest(args[0]);
         app.go();
     }
 
 }
