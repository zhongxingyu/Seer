 /*
  *
  *
  *    Copyright 2009 The MITRE Corporation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package hdatacore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.projecthdata.hdata.hrf.HRF;
 import org.projecthdata.hdata.hrf.ExtensionMissingException;
 import org.projecthdata.hdata.hrf.serialization.HRFFileSystemSerializer;
 import org.projecthdata.hdata.hrf.serialization.HRFSerialializationException;
 import org.projecthdata.hdata.hrf.util.hDataContentResolver;
 
 /**
  *
  * @author GBEUCHELT
  */
 public class Deserialize {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
 
 
         try {
             HRF hrf = null;
             HRFFileSystemSerializer des = new HRFFileSystemSerializer();
 
             String propertiesFile = "org/projecthdata/hdata/buildingblocks/ccd.properties";
 
             InputStream in = ClassLoader.getSystemResourceAsStream(propertiesFile);
             if (in == null) {
                 throw new FileNotFoundException();
             }
 
             Properties props = new java.util.Properties();
             props.load(in);
 
             hDataContentResolver cr = new hDataContentResolver(props);
 
             cr.registerExtension(des);
             try {
                 hrf = des.deserialize(new File("/tmp/hrf/patient"));
             } catch (HRFSerialializationException ex) {
                 Logger.getLogger(Deserialize.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             System.out.println();
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(Deserialize.class.getName()).log(Level.SEVERE, null, ex);
         } catch (URISyntaxException ex) {
             Logger.getLogger(Deserialize.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Deserialize.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ExtensionMissingException ex) {
             Logger.getLogger(Deserialize.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
