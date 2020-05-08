 /**
  * Copyright 2012 Alex Jones
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
 
 package uk.co.unclealex.music.command.checkin.process;
 
 import java.io.IOException;
 import java.util.SortedMap;
 
 import uk.co.unclealex.music.MusicFile;
 import uk.co.unclealex.music.action.Actions;
 import uk.co.unclealex.music.action.FailureAction;
 import uk.co.unclealex.music.files.FileLocation;
 
 /**
  * An interface for classes that map FLAC files to their {@link MusicFile}
  * representation. This is the first stage of checking in FLAC files.
  * 
  * @author alex
  * 
  */
 public interface MappingService {
 
   /**
   * Map each FLAC {@link FileLocation} to its {@link MusicFile} representation.
    * 
    * @param actions
    *          An initial {@link Actions} object.
    * @param fileLocations
    *          The FLAC file locations to read
    * @param musicFilesByFileLocation
    *          The map of FLAC file locations and their {@link MusicFile}
    *          representations to populate.
    * @return An {@link Actions} object containing {@link FailureAction}s for any
    *         invalid files.
    * @throws IOException
    */
   public Actions mapPathsToMusicFiles(
       Actions actions,
       Iterable<FileLocation> fileLocations,
       SortedMap<FileLocation, MusicFile> musicFilesByFileLocation) throws IOException;
 }
