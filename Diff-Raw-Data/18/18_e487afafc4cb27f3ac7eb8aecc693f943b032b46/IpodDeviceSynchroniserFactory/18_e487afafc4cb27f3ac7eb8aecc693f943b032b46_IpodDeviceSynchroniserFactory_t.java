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
 
 package uk.co.unclealex.music.sync;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 import javax.inject.Inject;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.unclealex.music.audio.AudioMusicFileFactory;
 import uk.co.unclealex.music.configuration.IpodDevice;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.music.devices.DeviceService;
 import uk.co.unclealex.music.files.DirectoryService;
 import uk.co.unclealex.music.files.FileLocation;
 import uk.co.unclealex.music.message.MessageService;
 import uk.co.unclealex.process.builder.ProcessRequestBuilder;
 import uk.co.unclealex.process.packages.PackagesRequired;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicates;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 /**
  * The {@link SynchroniserFactory} for iPods.
  * 
  * @author alex
  * 
  */
 @PackagesRequired({ "python-gpod", "python-eyed3", "python-gtk2", "pmount" })
 public class IpodDeviceSynchroniserFactory extends MountedDeviceSynchroniserFactory<IpodDevice> {
 
   /**
    * The number of milliseconds in an hour.
    */
   private static final long MILLISECONDS_IN_HOUR = 60 * 60 * 1000;
 
   /**
    * The logger for this class.
    */
   private final Logger log = LoggerFactory.getLogger(IpodDeviceSynchroniserFactory.class);
 
   /**
    * The {@link MessageService} used to relay messages to the user.
    */
   private final MessageService messageService;
 
   /**
    * The {@link DirectoryService} used to search for MP3 files.
    */
   private final DirectoryService directoryService;
 
   /**
    * The {@link AudioMusicFileFactory} used to find a track's MusicBrainz ID.
    */
   private final AudioMusicFileFactory audioMusicFileFactory;
 
   /**
    * The {@link DeviceService} used to find the device's repository.
    */
   private final DeviceService deviceService;
 
   @Inject
   public IpodDeviceSynchroniserFactory(
       ProcessRequestBuilder processRequestBuilder,
       AudioMusicFileFactory audioMusicFileFactory,
       DeviceService deviceService,
       DirectoryService directoryService,
       MessageService messageService) {
     super(processRequestBuilder);
     this.audioMusicFileFactory = audioMusicFileFactory;
     this.deviceService = deviceService;
     this.directoryService = directoryService;
     this.messageService = messageService;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected Path createOrFindMountPoint(IpodDevice device) throws IOException {
     return device.getMountPoint();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Synchroniser createSynchroniser(User owner, IpodDevice device) {
     return new IpodSynchroniser(owner, device);
   }
 
   class IpodSynchroniser implements Synchroniser {
 
     /**
      * The {@link User} owner of the iPod.
      */
     private final User owner;
 
     /**
      * The {@link IpodDevice} being synchronised.
      */
     private final IpodDevice ipodDevice;
 
     /**
      * The {@link GtkPodExecutor} used to talk to the iPOD.
      */
     private final GtkPodExecutor gtkPodExecutor = new GtkPodExecutor(Charsets.UTF_8);
 
     public IpodSynchroniser(User owner, IpodDevice ipodDevice) {
       super();
       this.owner = owner;
       this.ipodDevice = ipodDevice;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void synchronise() throws IOException {
       MessageService messageService = getMessageService();
       GtkPodExecutor gtkPodExecutor = getGtkPodExecutor();
       getProcessRequestBuilder()
           .forResource("sync.py")
           .withArguments("ipod", getIpodDevice().getMountPoint().toString())
           .withStandardInputSupplier(gtkPodExecutor)
           .execute();
       try {
         Path deviceRepositoryBase = getDeviceService().getDeviceRepositoryBase(getOwner());
         Set<DeviceFile> deviceFiles = listDeviceFiles();
         SortedSet<FileLocation> fileLocations = getDirectoryService().listFiles(deviceRepositoryBase);
         Function<DeviceFile, String> deviceRelativePathFunction = new Function<DeviceFile, String>() {
           public String apply(DeviceFile deviceFile) {
             return deviceFile.getRelativePath();
           }
         };
        Map<String, DeviceFile> deviceFilesByRelativePath = Maps.newHashMap(Maps.uniqueIndex(deviceFiles, deviceRelativePathFunction));
         Function<FileLocation, String> fileLocationRelativePathFunction = new Function<FileLocation, String>() {
           public String apply(FileLocation fileLocation) {
             return fileLocation.getRelativePath().toString();
           }
         };
         Map<String, FileLocation> fileLocationsByRelativePath =
             Maps.uniqueIndex(fileLocations, fileLocationRelativePathFunction);
         SortedSet<DeviceFile> deviceFilesToRemove = Sets.newTreeSet();
         SortedSet<FileLocation> localFilesToAdd = Sets.newTreeSet();
         for (Map.Entry<String, FileLocation> entry : fileLocationsByRelativePath.entrySet()) {
           String relativePath = entry.getKey();
           FileLocation fileLocation = entry.getValue();
           DeviceFile deviceFile = deviceFilesByRelativePath.get(relativePath);
           if (deviceFile == null
               || laterThan(Files.getLastModifiedTime(fileLocation.resolve()).toMillis(), deviceFile.getLastModified())) {
             if (deviceFile != null) {
               deviceFilesToRemove.add(deviceFile);
               deviceFilesByRelativePath.remove(relativePath);
             }
             localFilesToAdd.add(fileLocation);
           }
           else if (deviceFile != null) {
             messageService.printMessage(MessageService.SYNC_KEEP, deviceFile.getRelativePath());
             deviceFilesByRelativePath.remove(relativePath);
           }
         }
         deviceFilesToRemove.addAll(deviceFilesByRelativePath.values());
         for (DeviceFile deviceFile : deviceFilesToRemove) {
           messageService.printMessage(MessageService.SYNC_REMOVE, deviceFile.getRelativePath());
           remove(deviceFile);
         }
         for (FileLocation fileLocation : localFilesToAdd) {
           messageService.printMessage(MessageService.SYNC_ADD, fileLocation.getRelativePath());
           add(fileLocation);
         }
       }
       catch (RuntimeException e) {
         log.error("There was an unexpected error trying to synchronise " + getIpodDevice(), e);
       }
       finally {
         gtkPodExecutor.finish();
         closeDevice();
       }
     }
 
     /**
      * Test if the lhs is later than the rhs but also that there is not exactly
      * an hour's difference.
      * 
      * @param lhs
      * @param rhs
      * @return
      */
     protected boolean laterThan(long lhs, long rhs) {
       if (Math.abs(lhs - rhs) == MILLISECONDS_IN_HOUR) {
         return false;
       }
       else {
         return lhs > rhs;
       }
     }
 
     public Set<DeviceFile> listDeviceFiles() throws IOException {
       List<String> deviceFileStrings = executeCommand("LIST");
       Function<String, DeviceFile> deviceFileParserFunction = new Function<String, DeviceFile>() {
         @Override
         public DeviceFile apply(String str) {
           if (str.trim().isEmpty() || str.startsWith("**")) {
             return null;
           }
           else {
             DateTimeFormatter formatter = ISODateTimeFormat.dateHourMinuteSecond();
             List<String> deviceFileParts = Lists.newArrayList(Splitter.on('|').split(str));
             DateTime dateTime = formatter.parseDateTime(deviceFileParts.get(2));
             return new DeviceFile(deviceFileParts.get(0), deviceFileParts.get(1), dateTime.getMillis());
           }
         }
       };
       return Sets.newTreeSet(Iterables.filter(
           Iterables.transform(deviceFileStrings, deviceFileParserFunction),
           Predicates.notNull()));
     }
 
     public void add(FileLocation fileloLocation) throws IOException {
       executeCommand("ADD", fileloLocation.getRelativePath(), fileloLocation.resolve());
     }
 
     public void remove(DeviceFile deviceFile) throws IOException {
       executeCommand("REMOVE", deviceFile.getId());
     }
 
     public void closeDevice() throws IOException {
       executeCommand("QUIT");
     }
 
     protected List<String> executeCommand(Object... command) throws IOException {
       String fullCommand = Joiner.on('|').join(command);
       return getGtkPodExecutor().executeCommand(fullCommand);
     }
 
     public IpodDevice getIpodDevice() {
       return ipodDevice;
     }
 
     public User getOwner() {
       return owner;
     }
 
     public GtkPodExecutor getGtkPodExecutor() {
       return gtkPodExecutor;
     }
   }
 
   public AudioMusicFileFactory getAudioMusicFileFactory() {
     return audioMusicFileFactory;
   }
 
   public DeviceService getDeviceService() {
     return deviceService;
   }
 
   public DirectoryService getDirectoryService() {
     return directoryService;
   }
 
   public MessageService getMessageService() {
     return messageService;
   }
 }
