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
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import uk.co.unclealex.music.configuration.IpodDevice;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.music.devices.DeviceService;
 import uk.co.unclealex.music.files.DirectoryService;
 import uk.co.unclealex.music.files.FileLocation;
 import uk.co.unclealex.music.message.MessageService;
 import uk.co.unclealex.process.builder.ProcessRequestBuilder;
 import uk.co.unclealex.process.packages.PackagesRequired;
 import uk.co.unclealex.process.stream.Executor;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicates;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.inject.assistedinject.Assisted;
 
 /**
  * The {@link Synchroniser} for iPods.
  * 
  * @author alex
  * 
  */
 @PackagesRequired({ "python-gpod", "python-eyed3", "python-gtk2", "pmount" })
 public class IpodSynchroniser extends AbstractSynchroniser<IpodDevice> {
 
     /**
      * The {@link Executor} used to talk to the iPOD.
      */
     private final Executor executor = new Executor("OK", Charsets.UTF_8);
 
     /**
      * Instantiates a new ipod synchroniser.
      *
      * @param messageService the message service
      * @param directoryService the directory service
      * @param deviceService the device service
      * @param processRequestBuilder the process request builder
      * @param owner the owner
      * @param device the device
      */
     @Inject
     public IpodSynchroniser(
         MessageService messageService,
         DirectoryService directoryService,
         DeviceService deviceService,
         ProcessRequestBuilder processRequestBuilder,
         @Assisted User owner,
         @Assisted IpodDevice device) {
       super(messageService, directoryService, deviceService, processRequestBuilder, owner, device);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void initialise() throws IOException {
       Executor executor = getExecutor();
       String mountPoint = getDevice().getMountPoint().toString();
       getProcessRequestBuilder()
           .forResource("sync.py")
           .withArguments("ipod", mountPoint)
           .withStandardInputSupplier(executor)
           .execute();
     }
 
     /**
      * List device files.
      *
      * @return the sets the
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public Set<DeviceFile> listDeviceFiles() throws IOException {
       List<String> deviceFileStrings = executeCommand("LIST");
       Function<String, DeviceFile> deviceFileParserFunction = new Function<String, DeviceFile>() {
         @Override
         public DeviceFile apply(String str) {
           str = str.trim();
           if (str.isEmpty() || str.startsWith("**")) {
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
 
     /**
      * Adds the.
      *
      * @param fileloLocation the filelo location
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public void add(FileLocation fileloLocation) throws IOException {
       executeCommand("ADD", fileloLocation.getRelativePath(), fileloLocation.resolve());
     }
 
     /**
      * Removes the.
      *
      * @param deviceFile the device file
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public void remove(DeviceFile deviceFile) throws IOException {
       executeCommand("REMOVE", deviceFile.getId());
     }
 
     /**
      * Close device.
      *
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public void closeDevice() throws IOException {
       executeCommand("QUIT");
     }
 
     /**
      * Execute command.
      *
      * @param command the command
      * @return the list
      * @throws IOException Signals that an I/O exception has occurred.
      */
     protected List<String> executeCommand(Object... command) throws IOException {
       String fullCommand = Joiner.on('|').join(command);
       return getExecutor().executeCommand(fullCommand);
     }
 
     /**
      * Gets the {@link Executor} used to talk to the iPOD.
      *
      * @return the {@link Executor} used to talk to the iPOD
      */
     public Executor getExecutor() {
       return executor;
     }
 
 }
