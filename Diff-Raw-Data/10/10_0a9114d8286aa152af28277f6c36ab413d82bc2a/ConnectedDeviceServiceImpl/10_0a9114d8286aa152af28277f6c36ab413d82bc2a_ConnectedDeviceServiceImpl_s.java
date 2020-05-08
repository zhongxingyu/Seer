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
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Inject;
 
 import uk.co.unclealex.music.configuration.Device;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.process.builder.ProcessRequestBuilder;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Splitter;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 
 /**
  * The default implementation of {@link ConnectedDeviceService}.
  * 
  * @author alex
  * 
  */
 public class ConnectedDeviceServiceImpl implements ConnectedDeviceService {
 
   /**
    * The list of known users.
    */
   private final List<User> users;
 
   /**
    * Instantiates a new connected device service impl.
    * 
    * @param users
    *          the users
    * @param processRequestBuilder
    *          the process request builder
    */
   @Inject
   public ConnectedDeviceServiceImpl(List<User> users, ProcessRequestBuilder processRequestBuilder) {
     super();
     this.users = users;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Multimap<User, Device> listConnectedDevices() throws IOException {
     HashMultimap<User, Device> connectedDevices = HashMultimap.create();
     List<String> mountedDevices = list(1, Paths.get("/etc", "mtab"));
     for (User user : getUsers()) {
       for (Device device : user.getDevices()) {
         if (isDeviceConnected(device, mountedDevices)) {
           connectedDevices.put(user, device);
         }
       }
     }
     return connectedDevices;
   }
 
   /**
    * Check to see if a device is connected.
    * 
    * @param device
    *          The device to check.
    * @param connectedUsbDevices
    *          The list of USB IDs connected to this machine.
    * @param mountedDevices
    *          The list of mount points where physical devices are connected.
    * @return True if the device is connected, false otherwise.
    */
   protected boolean isDeviceConnected(Device device, final List<String> mountedDevices) {
     return mountedDevices.contains(device.getMountPoint().toString());
   }
 
   /**
    * Read a file and return a column from its output.
    * 
    * @param columnToReturn
    *          The number of the column to return.
    * @param path
    *          The path to read.
    * @return A list of strings, each containing a column from the command's
    *         output.
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   protected List<String> list(final int columnToReturn, Path path) throws IOException {
     List<String> cells = Lists.newArrayList();
     for (String line : readFile(path)) {
       String cell =
           Iterables.get(
               Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults().split(line),
               columnToReturn,
               null);
       if (cell != null) {
         cells.add(processOctal(cell));
       }
     }
     return cells;
   }
 
   protected List<String> readFile(Path path) throws IOException {
     return Files.readAllLines(path, Charset.defaultCharset());
   }
 
   /**
    * Convert octal escaped characters in strings.
    * 
    * @param line
    *          The line to convert.
    * @return A new string that contains normal characters
    */
   protected String processOctal(String line) {
     Pattern octalPattern = Pattern.compile("\\\\([0-7]{3})");
     StringBuffer sb = new StringBuffer();
     Matcher matcher = octalPattern.matcher(line);
     while (matcher.find()) {
      char ch = (char) Integer.parseInt(matcher.group(1), 8);
      matcher.appendReplacement(sb, new String(new char[] { ch }));
     }
     matcher.appendTail(sb);
     return sb.toString();
   }
 
   /**
    * Gets the list of known users.
    * 
    * @return the list of known users
    */
   public List<User> getUsers() {
     return users;
   }
 }
