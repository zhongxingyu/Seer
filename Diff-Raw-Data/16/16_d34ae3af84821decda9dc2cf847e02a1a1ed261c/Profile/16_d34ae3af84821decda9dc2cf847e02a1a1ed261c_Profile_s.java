 /*
  * Copyright 2014 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.launcher;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 /**
  * Profile is a list of config files for ServiceStarter and another list for Rio Monitor
  *
  * @author Rafał Krupiński
  */
 public class Profile {
     private String[] sorcerConfigPaths;
     private String[] monitorConfigPaths;
 
     public String[] getSorcerConfigPaths() {
         return sorcerConfigPaths;
     }
 
     public void setSorcerConfigPaths(String[] sorcerConfigPaths) {
         this.sorcerConfigPaths = sorcerConfigPaths;
     }
 
     public String[] getMonitorConfigPaths() {
         return monitorConfigPaths;
     }
 
     public void setMonitorConfigPaths(String[] monitorConfigPaths) {
         this.monitorConfigPaths = monitorConfigPaths;
     }
 
     public static void main(String[] args) {
         String sorcerConfigFilesArg = args[0];
         String monitorConfigFilesArg = args.length == 2 ? args[1] : "";
 
         Profile profile = new Profile();
         profile.setSorcerConfigPaths(sorcerConfigFilesArg.split(","));
         profile.setMonitorConfigPaths(monitorConfigFilesArg.split(","));
 
         XMLEncoder xe = new XMLEncoder(System.out);
         xe.writeObject(profile);
         xe.close();
     }
 
     public static Profile load(URL url) throws IOException {
         XMLDecoder xd = null;
         InputStream in = null;
         try {
             in = url.openStream();
             xd = new XMLDecoder(in);
             return (Profile) xd.readObject();
         } finally {
             if (xd != null)
                 xd.close();
             if (in != null)
                 in.close();
         }
     }
 
     public static Profile loadBuiltin(String name) throws IOException {
         URL url = Profile.class.getClassLoader().getResource(name + ".xml");
        if (url == null) throw new IllegalAccessError("Profile not found" + name);
         return load(url);
     }
 
     /**
      * load the default profile from class loader resource sorcer.xml
      *
      * @throws java.lang.IllegalStateException if the default profile was not found
      */
     public static Profile loadDefault() {
         try {
             return loadBuiltin("sorcer");
         } catch (IOException e) {
             throw new IllegalStateException("Couldn't find the default profile resource", e);
         }
     }
 }
