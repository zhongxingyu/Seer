 // 
 //  WaveRecipe.java
 //  CalFitWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-01-24.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waverecipe;
 
 import edu.berkeley.androidwave.waveexception.*;
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.WaveRecipeAlgorithm;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Xml;
 import java.io.InputStream;
 import java.security.cert.Certificate;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.jar.JarFile;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * WaveRecipe
  * 
  * In memory representation of the recipe as provided by an authority.
  * 
  * Recipes are distributed as jarfiles, as they can be
  * readily signed with the jarsigner application.
  *
  * The structure of a .waverecipe is as follows:
  *          MyRecipe.waverecipe -*- description.xml
  *                               - classes.dex
  *
  * description.xml indicates, among other things, the names of specific
  * classes to be loaded in the classes.dex
  * 
  * http://doandroids.com/blogs/2010/6/10/android-classloader-dynamic-loading-of/
  * http://yenliangl.blogspot.com/2009/11/dynamic-loading-of-classes-in-your.html
  * http://download.oracle.com/javase/1.3/docs/tooldocs/win32/jarsigner.html
  */
 public class WaveRecipe implements Parcelable {
     
     private static final String DESCRIPTION_XML_PATH = "assets/description.xml";
     
     protected String recipeId;
     protected Date version;
     protected String name;
     protected String description;
     
     protected Class<?> algorithmMainClass;
     
     /**
      * createFromUID
      * 
      * This should check for a cached version of the recipe, downloading it if
      * necessary, then instantiating from the downloaded version.
      */
     public static WaveRecipe createFromID(String recipeID, int version) {
         // null implementation
         return null;
     }
     
     /**
      * createFromDisk
      *
      * instantiate and return a WaveRecipe from an on disk location.  Should
      * throw an exception if the .waverecipe signature is invalid.
      */
     protected static WaveRecipe createFromDisk(String recipePath)
         throws Exception {
         
         // recipePath should point to an apk.  We need to examine the
         // signature of the apk somehow
         
         // for now we just see if it has a signature
         JarFile recipeApk = new JarFile(recipePath, true);
         if (recipeApk == null) {
             throw new InvalidSignatureException();
         }
         
         WaveRecipe recipe = new WaveRecipe();
         
         // Create a loader for this apk
        // http://yenliangl.blogspot.com/2009/11/dynamic-loading-of-classes-in-your.html
        // http://www.mail-archive.com/android-developers@googlegroups.com/msg07714.html
         dalvik.system.PathClassLoader recipePathClassLoader =
             new dalvik.system.PathClassLoader(recipePath, ClassLoader.getSystemClassLoader());
         
         // try to load the description.xml
         InputStream descriptionInputStream = recipePathClassLoader.getResourceAsStream(DESCRIPTION_XML_PATH);
         WaveRecipeXmlContentHandler contentHandler = new WaveRecipeXmlContentHandler(recipe);
         Xml.parse(descriptionInputStream, Xml.Encoding.UTF_8, contentHandler);
         
         String implementationClassName = contentHandler.getAlgorithmClassName();
         
         // try to load the WaveRecipeAlgorithm implementation
         try {
             recipe.algorithmMainClass = Class.forName(implementationClassName, true, recipePathClassLoader);
         } catch (ClassNotFoundException cnfe) {
             throw new Exception("Could not find main recipe class "+implementationClassName);
         }
         
         return recipe;
     }
     
     /**
      * retrieveRecipe
      * 
      * Retrieve a recipe from a recipe authority.  Note that we need a
      * running recipe server for this.  This should cache the recipe locally.
      */
     protected static boolean retreiveRecipe(String recipeUID)
         throws InvalidSignatureException {
         // null implementation
         return false;
     }
     
     /**
      * -------------------------- Instance Methods ---------------------------
      */
     
     /**
      * WaveRecipe
      * 
      * Constructor
      */
     public WaveRecipe() {
         // initialize any complex fields
     }
     
     /**
      * getID
      */
     public String getID() {
         return recipeId;
     }
     
     /**
      * getVersion
      */
     public Date getVersion() {
         return version;
     }
     
     /**
      * getName
      */
     public String getName() {
         return name;
     }
     
     /**
      * getDescription
      */
     public String getDescription() {
         return description;
     }
     
     /**
      * toString
      */
     @Override
     public String toString() {
         return String.format("%s(%s-%s)", this.getClass().getSimpleName(), recipeId, version);
     }
     
     /**
      * Parcelable Methods
      */
     public int describeContents() {
         return 0;
     }
     
     public void writeToParcel(Parcel dest, int flags) {
         
     }
     
     public static final Parcelable.Creator<WaveRecipe> CREATOR = new Parcelable.Creator<WaveRecipe>() {
         public WaveRecipe createFromParcel(Parcel in) {
             return new WaveRecipe(in);
         }
         
         public WaveRecipe[] newArray(int size) {
             return new WaveRecipe[size];
         }
     };
     
     private WaveRecipe(Parcel in) {
         
     }
 }
 
 
 class WaveRecipeXmlContentHandler extends DefaultHandler {
     private WaveRecipe recipe;
     protected String algorithmClassName;
     
     private String text;
     boolean inRecipe;
     
     public enum SubTag { NONE, SENSORS, OUTPUT, TABLE, ALG };
     SubTag stag = SubTag.NONE;
     
     protected static Date dateFromXmlString(String s)
         throws SAXException {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
         Date d = formatter.parse(s,new ParsePosition(0));
         if (d == null) {
             throw new SAXException("Error parsing date \""+s+"\"");
         }
         return d;
     }
     
     public WaveRecipeXmlContentHandler(WaveRecipe r) {
         recipe = r;
     }
     
     public String getAlgorithmClassName() {
         return algorithmClassName;
     }
     
     /**
      * ContentHandler methods
      */
     @Override
     public void startDocument() throws SAXException {
         inRecipe = false;
         algorithmClassName = null;
     }
     @Override
     public void startElement(String uri, String localName, String qName, Attributes atts)
             throws SAXException {
         
         System.out.println(String.format("startElement(%s, %s, %s, %s)", uri, localName, qName, atts));
         
         if (inRecipe) {
             if (localName.equalsIgnoreCase("recipe")) {
                 throw new SAXException("Nested recipe - " + qName);
             } else if (stag == SubTag.NONE) {
                 if (localName.equalsIgnoreCase("name")) {
                     // wait for tag close
                 } else if (localName.equalsIgnoreCase("description")) {
                     // wait for tag close
                 } else if (localName.equalsIgnoreCase("sensors")) {
                     stag = SubTag.SENSORS;
                 } else if (localName.equalsIgnoreCase("output")) {
                     stag = SubTag.OUTPUT;
                 } else if (localName.equalsIgnoreCase("granularity-table")) {
                     stag = SubTag.TABLE;
                 } else if (localName.equalsIgnoreCase("algorithm")) {
                     stag = SubTag.ALG;
                 }
             } else if (stag == SubTag.SENSORS) {
                 if (localName.equalsIgnoreCase("accelerometer")) {
                     // TODO: parse sensor tag
                 }
             } else if (stag == SubTag.OUTPUT) {
                 
             } else if (stag == SubTag.TABLE) {
                 
             } else if (stag == SubTag.ALG) {
                 if (localName.equalsIgnoreCase("class")) {
                     if (atts.getValue("interface").equals("WaveRecipeAlgorithm")) {
                         algorithmClassName = atts.getValue("name");
                     }
                 }
             }
         } else {
             if (localName.equalsIgnoreCase("recipe")) {
                 recipe.recipeId = atts.getValue("id");
                 recipe.version = dateFromXmlString(atts.getValue("version"));
                 inRecipe = true;
             } else {
                 throw new SAXException("Root element "+localName+" is not a recipe");
             }
         }
     }
 
     @Override
     public void characters(char[] ch, int start, int length)
             throws SAXException {
         /* Gets called every time in between an opening tag and
          * a closing tag if characters are encountered. */
         text = new String(ch, start, length);
     }
 
     @Override
     public void endElement(String uri, String localName, String qName)
             throws SAXException {
         System.out.println(String.format("endElement(%s, %s, %s)", uri, localName, qName));
         // Gets called every time a closing tag is encountered.
         if (inRecipe) {
             if (stag == SubTag.NONE) {
                 if (localName.equalsIgnoreCase("recipe")) {
                     inRecipe = false;
                 } else if (localName.equalsIgnoreCase("name")) {
                     recipe.name = text;
                 }
             } else if (stag == SubTag.SENSORS) {
                 if (localName.equalsIgnoreCase("sensors")) {
                     stag = SubTag.NONE;
                 } else if (localName.equalsIgnoreCase("accelerometer")) {
                     // should create an accelerometer object for the recipe
                 } else {
                     throw new SAXException("Bad structure");
                 }
             } else if (stag == SubTag.OUTPUT) {
                 if (localName.equalsIgnoreCase("output")) {
                     stag = SubTag.NONE;
                 } else if (localName.equalsIgnoreCase("accelerometer")) {
                     // should create an accelerometer object for the recipe
                 } else {
                     // need to handle calibration tags inside
                     //throw new SAXException("Bad structure");
                 }
             } else if (stag == SubTag.TABLE) {
                 if (localName.equalsIgnoreCase("granularity-table")) {
                     stag = SubTag.NONE;
                 }
             } else if (stag == SubTag.ALG) {
                 if (localName.equalsIgnoreCase("algorithm")) {
                     stag = SubTag.NONE;
                 }
             }
         } else {
             throw new SAXException("Root element is not a recipe");
         }
     }
 
     @Override
     public void endDocument() throws SAXException {
         /* You can perform some action in this method
          * for example to reset some sort of Collection
          * or any other variable you want. It gets called
          * every time a document end is reached. */
     }
 }
