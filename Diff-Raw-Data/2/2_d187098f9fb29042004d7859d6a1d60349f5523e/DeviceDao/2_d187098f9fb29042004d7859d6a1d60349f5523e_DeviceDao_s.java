 /*
  * Copyright 2011 David Simmons
  * http://cafbit.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cafbit.motelib.dao;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CodingErrorAction;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.cafbit.motelib.MoteContext;
 import com.cafbit.motelib.model.Device;
 import com.cafbit.motelib.model.DeviceClass;
 import com.cafbit.motelib.model.Profile;
 import com.cafbit.xmlfoo.XmlFooException;
 
 public class DeviceDao {
     
     public static final String DEFAULT_PROFILE_FILENAME = "profile-default.xml";
     
     public static final String PREFS_NAME_SETUP = "setup";
     public static final String PREFS_KEY_DEVICES = "devices";
     
     private Context context;
     private MoteContext moteContext;
     private SharedPreferences prefsSetup;
     private List<Device> deviceCache = null;
     private int nextId = 1;
     
     public DeviceDao(Context context, MoteContext moteContext) {
         this.context = context;
         this.moteContext = moteContext;
         if (context instanceof ContextWrapper) {
             ContextWrapper cw = (ContextWrapper) context;
             Context baseContext = cw.getBaseContext();
             System.out.println("context: "+cw+" baseContext: "+baseContext);
         }
         prefsSetup = context.getSharedPreferences(PREFS_NAME_SETUP, 0);
     }
 
     public List<DeviceClass> getAllDeviceClasses() {
         return moteContext.getAllDeviceClasses();
     }
     
     public DeviceClass getDeviceClassByName(String deviceClassName) {
         for (DeviceClass deviceClass : moteContext.getAllDeviceClasses()) {
             if (deviceClass.getClass().getName().equals(deviceClassName)) {
                 return deviceClass;
             }
         }
         return null;
     }
     
     public List<Device> getAllDevices() {
         loadDevices();
         return deviceCache;
     }
     
     public Device getDeviceById(int id) {
         loadDevices();
         for (Device d : deviceCache) {
             if (d.id == id) {
                 return d;
             }
         }
         return null;
     }
     
     public void addDevice(Device device) {
         // check for duplicates
         for (Device d : getAllDevices()) {
            if (device.address.equals(d.address) && device.deviceClass.equals(d.deviceClass)) {
                 throw new RuntimeException("Another "+moteContext.getDeviceWord(device.deviceClass, false, false)+" is already configured at this address.");
             }
         }
 
         // add the device to the list
         loadDevices();
         device.id = nextId++;
         deviceCache.add(device);
         
         // save the device
         saveDeviceCache();
     }
 
     public void updateDevice(Device device) {
         loadDevices();
 
         // find the existing device entry and remove it
         int listIndex = -1;
         for (int i=0; i<deviceCache.size(); i++) {
             Device d = deviceCache.get(i);
             if (device.id == d.id) { 
                 listIndex = i;
                 break;
             }
         }
         if (listIndex == -1) {
             throw new RuntimeException("Cannot find the existing "+moteContext.getDeviceWord(device.deviceClass, false, false)+" configuration!");
         }
 
         // replace the device entry
         deviceCache.set(listIndex, device);
 
         // save the device
         saveDeviceCache();
     }
 
     public void removeDevice(Device device) {
 
         // add the device to the list
         loadDevices();
         ListIterator<Device> deviceIter = getAllDevices().listIterator();
         while (deviceIter.hasNext()) {
             Device d = deviceIter.next();
             if (d.equals(device)) {
                 deviceIter.remove();
             }
         }
 
         // save the device
         saveDeviceCache();
     }
 
     public void coldReset() {
         loadDevices();
         deviceCache.clear();
         Editor prefsSetupEditor = prefsSetup.edit();
         prefsSetupEditor.putString(PREFS_KEY_DEVICES, null);
         boolean success = prefsSetupEditor.commit();
         if (! success) {
             deviceCache = null; // invalidate
             throw new RuntimeException("Cannot reset devices.");
         }       
     }
     
     private void loadDevices() {
         if (deviceCache != null) {
             return;
         }
         
         // overly complex code to read a UTF-8 stream into a String.
         
         FileInputStream fis;
         try {
             fis = context.openFileInput(DEFAULT_PROFILE_FILENAME);
         } catch (FileNotFoundException e) {
             // no profile xml yet...
             deviceCache = new LinkedList<Device>();
             return;
         }
         StringBuilder stringBuilder = new StringBuilder();
         FileChannel fileChannel = fis.getChannel();
         final int BUFFER_SIZE = 7;
         ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
         CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);
         char[] charArray = charBuffer.array();
         CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
         decoder.onMalformedInput(CodingErrorAction.IGNORE);
         decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
         int nbytes;
         try {
             while ((nbytes = fileChannel.read(byteBuffer)) != -1) {
                 if (nbytes == 0) continue;
                 byteBuffer.flip(); // drain
                 decoder.decode(byteBuffer, charBuffer, false);
                 charBuffer.flip(); // drain
                 stringBuilder.append(charArray, charBuffer.position(), charBuffer.remaining());
                 
                 byteBuffer.compact();
                 charBuffer.clear();
             }
         } catch (IOException e1) {
             // give up
             e1.printStackTrace();
             deviceCache = new LinkedList<Device>();
             return;
         }
         byteBuffer.flip(); // final drain
         decoder.decode(byteBuffer, charBuffer, true);
         decoder.flush(charBuffer);
         charBuffer.flip();
         stringBuilder.append(charArray, charBuffer.position(), charBuffer.remaining());
         String xml = stringBuilder.toString();
 
         // deserialize
         List<Device> devices;
         try {
             Profile profile = (Profile) moteContext.getXmlFoo().deserialize(xml, Profile.class);
             devices = profile.devices;
         } catch (XmlFooException e) {
             e.printStackTrace();
             return;
         }
         
         // assign ids if needed
         for (Device d : devices) {
             if (d.id >= nextId) {
                 nextId = d.id + 1;
             }
         }
         for (Device d : devices) {
             if (d.id == 0) {
                 d.id = nextId++;
             }
         }
         
         deviceCache = devices;
     }
 
     private void saveDeviceCache() {
         
         Profile profile = new Profile();
         profile.devices = deviceCache;
         
         String xml;
         try {
             xml = moteContext.getXmlFoo().serialize("profile", null, profile, Profile.class);
         } catch (XmlFooException e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
         try {
             FileOutputStream fos = context.openFileOutput(DEFAULT_PROFILE_FILENAME, Context.MODE_PRIVATE);
             fos.write(xml.getBytes("UTF-8"));
             fos.write("\n".getBytes("UTF-8"));
             fos.close();
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }       
     }
 }
