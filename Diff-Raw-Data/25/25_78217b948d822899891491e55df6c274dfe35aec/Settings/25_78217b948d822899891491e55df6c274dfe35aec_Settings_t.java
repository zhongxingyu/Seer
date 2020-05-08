 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jmxdatamart.Extractor;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import org.jmxdatamart.common.DataType;
 import java.io.*;
 import java.util.*;
 import javax.management.MalformedObjectNameException;
 import org.slf4j.LoggerFactory;
 
 
 
 /**
  * This class contains settings for the extractor : polling rate, output file(s)
  * location, JMX server URL and what bean to collect.
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 
 public class Settings {
     private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Extractor.class);
     private long pollingRate;
     private String folderLocation;
     private String url;
     private List<MBeanData> beans;
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (int) (this.pollingRate ^ (this.pollingRate >>> 32));
         hash = 67 * hash + (this.folderLocation != null ? this.folderLocation.hashCode() : 0);
         hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
         hash = 67 * hash + (this.beans != null ? this.beans.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Settings other = (Settings) obj;
         if (this.pollingRate != other.pollingRate) {
             return false;
         }
         if ((this.folderLocation == null) ? (other.folderLocation != null) : !this.folderLocation.equals(other.folderLocation)) {
             return false;
         }
         if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
             return false;
         }
         if (this.beans != other.beans && (this.beans == null || !this.beans.equals(other.beans))) {
             return false;
         }
         return true;
     }
     
     
     /**
      * @return the pollingRate
      */
     public long getPollingRate() {
         return pollingRate;
     }
 
     /**
      * @param pollingRate the pollingRate to set
      */
     public void setPollingRate(long pollingRate) {
         this.pollingRate = pollingRate;
     }
 
     /**
      * @return the folderLocation
      */
     public String getFolderLocation() {
         return folderLocation;
     }
 
     /**
      * @param folderLocation the folderLocation to set
      */
     public void setFolderLocation(String folderLocation) {
         this.folderLocation = folderLocation;
     }
 
     /**
      * @return the url
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url the url to set
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      * @return the beans
      */
     public List<MBeanData> getBeans() {
         return beans;
     }
 
     /**
      * @param beans the beans to set
      */
     public void setBeans(List<MBeanData> beans) {
         this.beans = beans;
     }
     
     public Settings() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
     }
     
     /**
      * Check if the setting is well formatted.
      * @return true if setting is well formatted, false if not
      * @throws MalformedObjectNameException if a MBean name in setting is malformated
      */
     // Task 3041
 	/*
     public boolean checkForValidAlias() throws MalformedObjectNameException {
         for (MBeanData mbd : this.beans) {
             if (!mbd.checkForValidAlias()) {
                 return false;
             }
             mbd.isPattern();
         }
         return true;
     }
     */
     public String toXML() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         return xstream.toXML(this);
     }
     
     public static Settings fromXML(String s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         Settings settings = (Settings)xstream.fromXML(s);
         try {
             settings.check();
         } catch (MalformedObjectNameException ex) {
             LoggerFactory.getLogger(Settings.class).error("Setting is malformated", ex);
             throw new RuntimeException(ex);
         }
         return settings;
         
     }
     
     public static Settings fromXML(InputStream s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
 
         Settings settings = (Settings)xstream.fromXML(s);
         try {
             settings.check();
         } catch (MalformedObjectNameException ex) {
             LoggerFactory.getLogger(Settings.class).error("Setting is malformated", ex);
             throw new RuntimeException(ex);
         }
         return settings;
     }
     
     @Override
     public String toString() {
         String nl = System.getProperty("line.separator");
         return  "Rate = " + pollingRate + nl +
                 "Loc = " + folderLocation + nl +
                 "URL = " + url + nl +
                 beans.toString();
         
     }

 
 /* Author: Aya Yakura
      * Settings.check(), Settings.checkAlphanumeric()
      * Checks to see if:
      * 1. alias is unique within the same bean
      * 2. bean's alias is unique within the same setting
      * 3. Alias must be alphanumeric only and start with a letter
      * (4. Log message and throw runtime exception if they doesn't meet the requirements)
      */
     public void check() throws MalformedObjectNameException{
         //create linked lists and variables that holds values we need to check
         LinkedList beanAlias = new LinkedList();
         LinkedList attriAlias = new LinkedList();
         String compString;
         int beanSize;
         int attriSize;
         
         //gather the data needed and check the values
         //check the the attributes within each bean as it iterates through the Settings
         for(MBeanData bData : getBeans()){ //iterates through all the beans
             String bDataAlias = bData.getAlias(); //gets the alias of this bean
             
             // *** ignore null or empty strings ("") while making this linked list ***
             // *** because it's valid but there's no need to check for duplicates/alphanumeric for these cases ***
             if (bDataAlias != null && !bDataAlias.equals("")){
                 beanAlias.add(bDataAlias); //append this string to the end of bean alias linked list
             }
             
             
             for (Attribute atData : bData.getAttributes()){ //iterates and gets all attributes in the bean                            
                 String atDataAlias = atData.getAlias(); //gets the alias of this attrubute within this bean
                 
                 // *** ignore null or empty strings ("") while making this linked list ***
                 // *** Same reason as in Bean alias above ***
                 if (atDataAlias != null && !atDataAlias.equals("")){                   
                     attriAlias.add(atDataAlias); //append this string to the end of attribute alias linked list 
                 }          
             }
             
             //after the atriAlias list is constructed, look for duplicates
             //size-1 because no need to check duplicates of the last element
             //break out if the list is empty at this point
             attriSize = attriAlias.size();
             if (attriSize == 0)
                 break;
             
             for (int i=0; i<attriSize-1; i++){
                 //check duplicates in the list one by one.
                 //take the head and check with the rest of the list.
                 
                 //remove the head before checking and store it.                
                 compString = (String)attriAlias.remove();
                 
                 //print for debugging purposes
                 /*
                 System.out.println(attriAlias.size());
                 System.out.println(compString);
                 System.out.println(attriAlias);
                 System.out.println();
                 //System.out.println(compString);
                 */
                 if (attriAlias.contains(compString)){//check duplicates in the list
                     //System.out.println("duplicate attribute found within bean: " + bDataAlias + ": " + compString); //just for debugging
                     throw new MalformedObjectNameException("duplicate attribute found within bean: " + bDataAlias + ": " + compString);//***throw exception
                 }
                 
                 //check if this alias is alphanumeric only and starts with a letter
                 checkAlphanumeric(compString);
             }
             //check the last alias for alphanumeric & starts with a letter, 
             //because it got skipped in the for loop
             compString = (String)attriAlias.remove();
             checkAlphanumeric(compString);
             
             
             //clear the list for the next iteration (don't need to because its cleared already)
             //attriAlias.clear();
         }
         //Similarly, check for duplicates for beanAlias list after beanAlias list is constructed
         //No need to do anything else if size is 0.
         beanSize = beanAlias.size();
         if (beanSize == 0){
             return;
         }
         for (int i=0; i<beanSize-1; ++i){
             compString = (String)beanAlias.remove();
             
             //print for debugging purposes
             /*
             System.out.println(beanAlias.size());
             System.out.println(compString);
             System.out.println(beanAlias);
             System.out.println();
             * */
                       
             if(beanAlias.contains(compString)){
                 //System.out.println("duplicate attribute found within this setting: " + compString); //just for debugging
                 throw new MalformedObjectNameException("duplicate attribute found within this setting: " + compString);//***throw exception
             }
             
             //check if this alias is alphanumeric only and starts with a letter
             checkAlphanumeric(compString);
         }
         //check the last alias for alphanumeric & starts with a letter, 
         //because it got skipped in the for loop
         compString = (String)beanAlias.remove();
         checkAlphanumeric(compString);
     }
     
     public static void checkAlphanumeric(String compString) throws MalformedObjectNameException{
         char[] compCharArray = compString.toCharArray(); //convert to char array
                 
         //checks the first letter
         if(!Character.isLetter(compCharArray[0])){
             //System.out.println("Attribute " + compString + " does not start with a letter."); //just for debugging
             throw new MalformedObjectNameException("Attribute " + compString + " does not start with a letter.");//***throw exception
         }
         //check if all characters are alphanumeric
         for(char c : compCharArray){ 
             if (!Character.isLetterOrDigit(c)){
                 //System.out.println("Attribute " + compString + " contains non-alphanumeric character."); 
                 throw new MalformedObjectNameException("Attribute " + compString + " contains non-alphanumeric character."); //***throw exception
                 //break; //*comment this out if throwing exception* if there was one non-alphanumeric character, no need to check the rest of the string    
             }
         }
     }

     /*
      * Code by Aya Yakura ends here
      */
     
     
     public static void main( String[] args ) throws IOException
     {
         //Test reading file
         Settings s1 = Settings.fromXML(new FileInputStream("Settings.xml"));
         System.out.println(s1.toString());
         System.out.println("Read xml settings complete");
         
         Settings s = new Settings();
         s.setFolderLocation("\\project\\");
         s.setPollingRate(5);
         s.setUrl("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
        s.setBeans(new ArrayList<MBeanData>());
         
         MBeanData bd = new MBeanData("com.example:type=Hello","Hello", new ArrayList<Attribute>(), true);
         bd.getAttributes().add(new Attribute("Name", "nameA", DataType.STRING));
         bd.getAttributes().add(new Attribute("CacheSize", "cacheB", DataType.INT));
         s.getBeans().add(bd);
 
         System.out.println(s.toString());
               
         //Adding a new bean, just testing...
         
         bd = new MBeanData("com.example:type=Hello","HelloA", new ArrayList<Attribute>(), true);
         bd.getAttributes().add(new Attribute("", "", DataType.STRING));
         bd.getAttributes().add(new Attribute(null, "", DataType.INT));
         bd.getAttributes().add(new Attribute("Time", "timeA", DataType.INT));
         bd.getAttributes().add(new Attribute("Time022", "timeB", DataType.STRING));
         bd.getAttributes().add(new Attribute("Other", "otherA", DataType.STRING));
         s.getBeans().add(bd);
         
         
         System.out.println(s.toString());
         
         String sXML = s.toXML();
         System.out.println(sXML);
         FileWriter out = new FileWriter("settings.xml");
         out.write(sXML);
         out.close();
         
         try{
             s.check();
         } catch (MalformedObjectNameException e){
             System.out.println(e.getMessage());
             System.exit(1); //exit code?
         }
         
     }
}
