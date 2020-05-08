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
 import java.text.Normalizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import uk.co.unclealex.music.MusicFile;
 
 /**
  * The default implementation of {@link FilenameService}.
  * 
  * @author alex
  * 
  */
 public class FilenameServiceImpl implements FilenameService {
 
   /**
    * {@inheritDoc} firstLetterOfSortedAlbumArtist/sortedAlbumArtist/album
    * (diskNumber)/trackNumber title.ext
    */
   @Override
   public Path toPath(MusicFile musicFile, Extension extension) {
     String albumArtistSort = musicFile.getAlbumArtistSort();
     String firstLetter = albumArtistSort.substring(0, 1);
     StringBuilder album = new StringBuilder(musicFile.getAlbum());
     int totalDiscs = musicFile.getTotalDiscs().intValue();
     if (totalDiscs != 1) {
      album.append(String.format(" %02d", musicFile.getDiscNumber()));
     }
     String title = String.format("%02d %s", musicFile.getTrackNumber(), musicFile.getTitle());
     return Paths.get(normalise(firstLetter), normalise(albumArtistSort), normalise(album), normalise(title) + "." + extension.getFileExtension());
   }
 
   protected String normalise(CharSequence charSequence) {
     String nfdNormalizedString = Normalizer.normalize(charSequence, Normalizer.Form.NFD);
     Pattern accentPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
     String nonAccentedString = accentPattern.matcher(nfdNormalizedString).replaceAll("");
     Pattern validCharactersPattern = Pattern.compile("(?:[a-zA-Z0-9]|\\s)+");
     StringBuilder normalisedSequence = new StringBuilder();
     Matcher m = validCharactersPattern.matcher(nonAccentedString);
     while (m.find()) {
       normalisedSequence.append(m.group());
     }
     return normalisedSequence.toString().replaceAll("\\s+", " ");
   }
 }
