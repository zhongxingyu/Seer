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
 
 import static org.hamcrest.Matchers.containsInAnyOrder;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.junit.Test;
 
 import uk.co.unclealex.music.configuration.Device;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.music.configuration.json.FileSystemDeviceBean;
 import uk.co.unclealex.music.configuration.json.IpodDeviceBean;
 import uk.co.unclealex.music.configuration.json.UserBean;
 
 import com.google.common.collect.Multimap;
 import com.mycila.inject.internal.guava.collect.Maps;
 
 /**
  * @author alex
  * 
  */
 public class ConnectedDeviceServiceImplTest {
 
   List<String> mtabOutput =
       Arrays
           .asList(
               "none /run/user tmpfs rw,noexec,nosuid,nodev,size=104857600,mode=0755 0 0",
               "/dev/sda7 /home ext4 rw 0 0",
               "binfmt_misc /proc/sys/fs/binfmt_misc binfmt_misc rw,noexec,nosuid,nodev 0 0",
               "rpc_pipefs /run/rpc_pipefs rpc_pipefs rw 0 0",
               "gvfsd-fuse /run/user/alex/gvfs fuse.gvfsd-fuse rw,nosuid,nodev,user=alex 0 0",
               "remote:/mnt/music /mnt/multimedia nfs rw,vers=4,addr=192.168.7.14,clientaddr=192.168.7.15 0 0",
               "/dev/sdb2 /media/brian/BRIAN'S\\040IPOD vfat rw,nosuid,nodev,uid=1000,gid=1000,shortname=mixed,dmask=0077,utf8=1,showexec,flush,uhelper=udisks2 0 0",
               "/dev/sda1 /media/WALKMAN vfat rw,nosuid,nodev,uid=1000,gid=1000,shortname=mixed,dmask=0077,utf8=1,showexec,flush,uhelper=udisks2 0 0",
               "remote:/mnt/home /mnt/home nfs rw,vers=4,addr=192.168.7.14,clientaddr=192.168.7.15 0 0"
 
           );
 
   @Test
   public void testConnectedDevices() throws IOException {
     IpodDeviceBean briansIpod = new IpodDeviceBean(Paths.get("/media", "brian", "BRIAN'S IPOD"));
     FileSystemDeviceBean briansFs = new FileSystemDeviceBean("dv", Paths.get("/mnt/multimedia"), Paths.get("Music"));
     User brian =
         new UserBean("brian", "bmay", "queen", Arrays.asList(new Device[] {
             briansIpod,
             new FileSystemDeviceBean("walkman", Paths.get("/mnt/brian"), Paths.get("MUSIC")),
             briansFs }));
     FileSystemDeviceBean freddiesWalkman = new FileSystemDeviceBean("walkman", Paths.get("/media/WALKMAN"), Paths.get("MUSIC"));
     FileSystemDeviceBean freddiesFs = new FileSystemDeviceBean("dvb", Paths.get("/mnt/home"), Paths.get("Music"));
     User freddie =
         new UserBean("freddie", "fmercury", "queen", Arrays.asList(new Device[] {
             new IpodDeviceBean(Paths.get("/media", "freddie", "FRED'S IPOD")),
             freddiesWalkman,
             freddiesFs }));
     List<User> users = Arrays.asList(brian, freddie);
     ConnectedDeviceService connectedDeviceService = new ConnectedDeviceServiceImpl(users, null) {
       /**
        * {@inheritDoc}
        */
       @Override
       protected List<String> readFile(Path path) throws IOException {
         if (Paths.get("/etc", "mtab").equals(path)) {
           return mtabOutput;
         }
         else {
           fail("An invalid native command was sent: " + path);
           return null;
         }
       }
     };
     Multimap<User, Device> connectedDevices = connectedDeviceService.listConnectedDevices();
     Map<User, Device[]> expectedDevices = Maps.newHashMap();
     expectedDevices.put(brian, new Device[] { briansIpod, briansFs });
     expectedDevices.put(freddie, new Device[] { freddiesWalkman, freddiesFs });
     assertThat("The wrong users were returned.", connectedDevices.keySet(), containsInAnyOrder(brian, freddie));
     for (Entry<User, Collection<Device>> entry : connectedDevices.asMap().entrySet()) {
       assertThat(
           "User " + entry.getKey().getName() + " has the wrong connected devices.",
           entry.getValue(),
           containsInAnyOrder(expectedDevices.get(entry.getKey())));
     }
   }
 
 }
