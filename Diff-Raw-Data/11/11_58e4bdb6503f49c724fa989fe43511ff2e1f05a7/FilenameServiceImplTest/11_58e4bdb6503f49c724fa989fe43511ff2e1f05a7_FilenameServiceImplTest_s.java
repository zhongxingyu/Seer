 /**
  * Copyright 2011 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.files;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.junit.Assert;
 
 import org.junit.Test;
 
 import static uk.co.unclealex.music.files.Extension.*;
 import uk.co.unclealex.music.MusicFile;
 import uk.co.unclealex.music.MusicFileBean;
 import uk.co.unclealex.music.files.Extension;
 import uk.co.unclealex.music.files.FilenameServiceImpl;
 
 /**
  * @author alex
  * 
  */
 public class FilenameServiceImplTest {
 
   @Test
   public void testSingleDisc() {
     runTest("Mötörhead", "Good - Stuff", 1, 1, 2, "The Ace of Spades", FLAC,
         Paths.get("M", "Motorhead", "Good Stuff", "02 The Ace of Spades.flac"));
   }
 
   @Test
   public void testMultipleDiscs() {
    runTest("Mötörhead", "Good - Stuff", 2, 2, 2, "The Ace of Spades", FLAC,
        Paths.get("M", "Motorhead", "Good Stuff 02", "02 The Ace of Spades.flac"));
   }
   
   protected void runTest(String albumArtistSort, String album, int discNumber, int totalDiscs, int trackNumber, String title, Extension extension, Path expectedPath) {
     MusicFile musicFile = new MusicFileBean();
     musicFile.setAlbumArtistSort(albumArtistSort);
     musicFile.setAlbum(album);
     musicFile.setDiscNumber(discNumber);
     musicFile.setTotalDiscs(totalDiscs);
     musicFile.setTrackNumber(trackNumber);
     musicFile.setTitle(title);
     Path actualPath = new FilenameServiceImpl().toPath(musicFile, extension);
     Assert.assertEquals("The wrong path was generated.", expectedPath, actualPath);
   }
 }
