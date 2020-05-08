 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.jmxdatamart.Extractor;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import java.io.*;
 import java.util.*;
 
 
 
 /**
  * This class contains settings for the extractor : polling rate, output file(s)
  * location, JMX server URL and what bean to collect.
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 
 public class Settings {
 
     private long pollingRate;
     private String folderLocation;
     private String url;
     private List<BeanData> beans;
 
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
     public List<BeanData> getBeans() {
         return beans;
     }
 
     /**
      * @param beans the beans to set
      */
     public void setBeans(List<BeanData> beans) {
         this.beans = beans;
     }
     
     public Settings() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", BeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", BeanData.class);
         xstream.alias("Attribute", Attribute.class);
     }
     
     public void sanitize() {
         for (BeanData bd : getBeans()) {
             if ("".equals(bd.getAlias())) {
                 bd.setAlias(bd.getName());
             }
             for (Attribute a : bd.getAttributes()) {
                 if ("".equals(a.getAlias())) {
                     a.setAlias(a.getName());
                 }
             }
         }
     }
     
     public String toXML() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", BeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", BeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         return xstream.toXML(this);
     }
     
     public static Settings fromXML(String s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", BeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", BeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         Settings settings = (Settings)xstream.fromXML(s);
         settings.sanitize();
         return settings;
         
     }
     
     public static Settings fromXML(InputStream s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", BeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", BeanData.class);
         xstream.alias("Attribute", Attribute.class);
 
         Settings settings = (Settings)xstream.fromXML(s);
         settings.sanitize();
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
     
     public static void main( String[] args ) throws IOException
     {
         Settings s = new Settings();
         s.setFolderLocation("\\project\\");
         s.setPollingRate(5);
         s.setUrl("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
         s.setBeans(new ArrayList<BeanData>());
         
         BeanData bd = new BeanData("com.example:type=Hello","", new ArrayList<Attribute>());
         bd.getAttributes().add(new Attribute("Name","", DataType.STRING));
         bd.getAttributes().add(new Attribute("CacheSize", "", DataType.INT));
         s.getBeans().add(bd);
         s.sanitize();
       System.out.println(s.toString());
         
         String sXML = s.toXML();
         System.out.println(sXML);
        //FileWriter out = new FileWriter("settings.cfg");
        //out.write(sXML);
        //out.close();
     }
 }
