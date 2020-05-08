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
 
 package uk.co.unclealex.music.command.sync;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.unclealex.music.configuration.Device;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.music.devices.DeviceService;
 import uk.co.unclealex.music.files.DirectoryService;
 import uk.co.unclealex.music.files.FileLocation;
 import uk.co.unclealex.music.message.MessageService;
 import uk.co.unclealex.music.sync.Synchroniser;
 import uk.co.unclealex.music.sync.SynchroniserFactory;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 
 /**
  * The default implementation of {@link SynchroniserService}.
  * 
  * @author alex
  * 
  */
 public class SynchroniserServiceImpl implements SynchroniserService {
 
   /**
    * The logger for logging errors.
    */
   private static final Logger log = LoggerFactory.getLogger(SynchroniserServiceImpl.class);
   
   /**
    * The {@link ExecutorService} used to create threads to synchronise devices.
    */
   private final ExecutorService executorService;
 
   /**
    * The {@link DirectoryService} used to list files in the devices
    * repositories.
    */
   private final DirectoryService directoryService;
 
   /**
    * The {@link SynchroniserFactory} used to create {@link Synchroniser}s.
    */
   private final SynchroniserFactory<Device> synchroniserFactory;
 
   /**
    * The {@link MessageService} used to print messages to the user.
    */
   private final MessageService messageService;
 
   /**
    * The {@link DeviceService} used to query devices.
    */
   private final DeviceService deviceService;
 
   /**
    * Instantiates a new synchroniser service impl.
    * 
    * @param executorService
    *          the executor service
    * @param directoryService
    *          the directory service
    * @param synchroniserFactory
    *          the synchroniser factory
    * @param deviceService
    *          the device service
    * @param messageService
    *          the message service
    */
   @Inject
   public SynchroniserServiceImpl(
       ExecutorService executorService,
       DirectoryService directoryService,
       SynchroniserFactory<Device> synchroniserFactory,
       DeviceService deviceService,
       MessageService messageService) {
     super();
     this.executorService = executorService;
     this.synchroniserFactory = synchroniserFactory;
     this.directoryService = directoryService;
     this.deviceService = deviceService;
     this.messageService = messageService;
   }
 
   /**
    * {@inheritDoc}
    * 
    */
   @Override
   public void synchronise(Multimap<User, Device> connectedDevices) throws IOException {
     Multimap<User, FileLocation> fileLocationsByOwner = HashMultimap.create();
     for (User owner : connectedDevices.keySet()) {
       Path deviceRepositoryBase = getDeviceService().getDeviceRepositoryBase(owner);
       fileLocationsByOwner.putAll(owner, getDirectoryService().listFiles(deviceRepositoryBase));
     }
     ExecutorService executorService = getExecutorService();
     List<Future<Void>> futures = Lists.newArrayList();
     for (Entry<User, Device> entry : connectedDevices.entries()) {
       User owner = entry.getKey();
       Device device = entry.getValue();
       futures.add(executorService.submit(new SynchronisingTask(owner, device, fileLocationsByOwner)));
     }
     for (Future<Void> future : futures) {
       try {
         future.get();
       }
       catch (InterruptedException | ExecutionException e) {
         log.error("An error occurred during synchronisation of a device.", e);
       }
     }
   }
 
   /**
    * A {@link Callable} that synchronises a device.
    */
   class SynchronisingTask implements Callable<Void> {
 
     /**
      * The owner of the device to be synchronised.
      */
     private final User owner;
 
     /**
      * The device to be synchronised.
      */
     private final Device device;
 
     /**
      * A set of device music file locations for each owner.
      */
     private final Multimap<User, FileLocation> fileLocationsByOwner;
 
     /**
      * Instantiates a new synchronising task.
      * 
      * @param owner
      *          the owner
      * @param device
      *          the device
      * @param fileLocationsByOwner
      *          the file locations by owner
      */
     public SynchronisingTask(User owner, Device device, Multimap<User, FileLocation> fileLocationsByOwner) {
       super();
       this.owner = owner;
       this.device = device;
       this.fileLocationsByOwner = fileLocationsByOwner;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Void call() throws IOException {
       printMessage(MessageService.SYNCHRONISING);
       Synchroniser synchroniser = getSynchroniserFactory().createSynchroniser(getOwner(), getDevice());
       synchroniser.synchronise(getFileLocationsByOwner());
       printMessage(MessageService.DEVICE_SYNCHRONISED);
       return null;
     }
 
     protected void printMessage(String template) {
      getMessageService().printMessage(template, getOwner(), getDevice());
     }
     
     /**
      * Gets the owner of the device to be synchronised.
      * 
      * @return the owner of the device to be synchronised
      */
     public User getOwner() {
       return owner;
     }
 
     /**
      * Gets the device to be synchronised.
      * 
      * @return the device to be synchronised
      */
     public Device getDevice() {
       return device;
     }
 
     /**
      * Gets the a set of device music file locations for each owner.
      * 
      * @return the a set of device music file locations for each owner
      */
     public Multimap<User, FileLocation> getFileLocationsByOwner() {
       return fileLocationsByOwner;
     }
 
   }
 
   /**
    * Gets the {@link ExecutorService} used to create threads to synchronise
    * devices.
    * 
    * @return the {@link ExecutorService} used to create threads to synchronise
    *         devices
    */
   public ExecutorService getExecutorService() {
     return executorService;
   }
 
   /**
    * Gets the {@link SynchroniserFactory} used to create {@link Synchroniser}s.
    * 
    * @return the {@link SynchroniserFactory} used to create {@link Synchroniser}
    *         s
    */
   public SynchroniserFactory<Device> getSynchroniserFactory() {
     return synchroniserFactory;
   }
 
   /**
    * Gets the {@link MessageService} used to print messages to the user.
    * 
    * @return the {@link MessageService} used to print messages to the user
    */
   public MessageService getMessageService() {
     return messageService;
   }
 
   /**
    * Gets the {@link DirectoryService} used to list files in the devices
    * repositories.
    * 
    * @return the {@link DirectoryService} used to list files in the devices
    *         repositories
    */
   public DirectoryService getDirectoryService() {
     return directoryService;
   }
 
   /**
    * Gets the {@link DeviceService} used to query devices.
    * 
    * @return the {@link DeviceService} used to query devices
    */
   public DeviceService getDeviceService() {
     return deviceService;
   }
 }
