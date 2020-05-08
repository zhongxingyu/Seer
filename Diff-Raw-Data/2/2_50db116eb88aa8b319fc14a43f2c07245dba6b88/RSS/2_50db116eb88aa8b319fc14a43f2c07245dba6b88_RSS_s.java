 /*
  * Copyright (c) 2009, Shun "Nazotoko" Watanabe <nazotoko@gmail.com>
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the OpenStreetMap <www.openstreetmap.org> nor the
  *    names of its contributors may be used to endorse or promote products
  *    derived from this software
  *    without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.openstreetmap.mappinonosm.database;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.ext.LexicalHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author nazo
  */
 public class RSS extends XML implements LexicalHandler, ContentHandler {
 
     /** for SAX reader */
     private Stack <String> stack;
     private String textBuffer;
     private boolean inCDATA = false;
     private String contextEncoded = null;
     private boolean contextCDATA = false;
     private String description = null;
     private boolean descriptionCDATA = false;
     static private SimpleDateFormat rssDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.UK);
     static private String[] startHock = {"title","media:thumbnail","media:content"};
     static private String[] endHock = {"link","category"};
 
     /**
      * This function is called from only XML.getInstance()
      * @param uri URI indicates the file exing there. 
      */
     protected RSS(URI uri) {
         super(uri);
     }
 
     /**
      * This function is called from only XML.read()
      * @param id ID number to be set. 
      */
     protected RSS(int id) {
         super(id);
     }
 
 
     private URL [] getImages(String context, boolean cdata){
         ArrayList<URL> urls=new ArrayList<URL>();
         if(!cdata){
             context=context.replaceAll("&lt;","<").replaceAll("&gt;", ">");
         }
         int astart=0,start=0,end=0,aend=0;
         String before;
         boolean thumb=false;
         while((start=context.indexOf("<img ",end))>0){
             before=context.substring(end, start);
             if((astart = before.lastIndexOf("<a "))>0 && before.indexOf("</a>",astart+3)<0){
                 if((aend = before.indexOf("href=", astart + 3)) > 0){
                     if(before.charAt(aend + 5) == '"'){
                         astart = aend + 6;
                         aend = before.indexOf('"', astart);
                         String s = before.substring(astart, aend);
                         try {
                             urls.add(new URL(s));
                             thumb=true;
                         } catch(MalformedURLException ex) {
                             System.out.println("Illigal URL for <a>: " + s);
                         }
                     }
                 }
             }
             if((end = context.indexOf("src=", start + 5))>0){
                 if(context.charAt(end + 4) == '"'){
                     start = end + 5;
                     end = context.indexOf('"',start);
                     String s=context.substring(start,end);
                     try {
                         urls.add(new URL(s));
                     } catch(MalformedURLException ex) {
                         System.out.println("Illigal URL for <img>: "+s);
                     }
                 }
             }
         }
         return urls.toArray(new URL[urls.size()]);
     }
 
     void read(){
         XMLReader parser;
         try {
             parser = XMLReaderFactory.createXMLReader();
             parser.setContentHandler(this);
             parser.parse(uri.toASCIIString());
         } catch(SAXException ex) {
             System.out.println("cannot read");
         } catch(IOException ex) {
             System.out.println("IOException");
         }
     }
     @Override
     public void startDocument() throws SAXException {
         if(photoTable == null){
             System.err.println("Program's Error: PhotoBase is not set.");
             System.exit(1);
         }
         if(uri == null){
             System.err.println("Program's Error: URL of RSS is not set.");
             System.exit(1);
         }
         System.out.println("RSS: "+uri);
         stack = new Stack<String>();
     }
 
     @Override
     public void startElement(String uri, String name, String qualifiedName, Attributes attributes) {
         if(photo==null){
             if (qualifiedName.equals("item")) {
                 photo = new Photo();
                 photo.setXML(this);
                 System.out.println("<item> start:");
                 contextCDATA=false;
                 contextEncoded=null;
                 descriptionCDATA=false;
                 description=null;
             }
         } else {
             if (qualifiedName.equals("media:thumbnail") && photo.getThumnale() == null) {
                 photo.setThumbnale(attributes.getValue("url"));
             } else if(qualifiedName.equals("media:content")){
                 photo.setOriginal(attributes.getValue("url"));
             }
         }
         textBuffer="";
         stack.push(qualifiedName);
     }
 
     @Override
     public void characters(char[] ch, int start, int length) {
         textBuffer += new String(ch, start, length);
     }
 
     @Override
     public void endElement(String uri, String name, String qualifiedName) {
         String fromStack=stack.pop();
         if(photo==null){
             if(qualifiedName.equals("title")){
                 title=entity(textBuffer);
                 System.out.println("RSS title: " + title);
             }
             if(qualifiedName.equals("link")){
                 try {
                     link = new URL(textBuffer);
                 } catch(MalformedURLException ex) {
                     link = null;
                 }
             }
         } else {
             if(qualifiedName.equals("item")){
                 photo.setReadDate(new Date());
                 if(photo.getOriginal() ==null){// if media tag doesn't exist.
                     URL[] urls;
                     if(contextEncoded != null){
                         urls = getImages(contextEncoded, contextCDATA);
                     } else {
                         urls = getImages(description, descriptionCDATA);
                     }
 /*                    for(URL urlPhoto: urls){
                         System.out.println("\timage: " + urlPhoto);
                     }*/
                     if(urls.length==1){
                         photo.setOriginal(urls[0]);
                     } else if(urls.length == 2){
                         photo.setOriginal(urls[0]);
                         photo.setThumbnale(urls[1]);
                     }
                 }
                 if(photoTable.add(photo) == false){
                     Photo oldPhoto = photoTable.get(photo);
                     if(oldPhoto.getReadDate().before(photo.getPublishedDate())){
                         photo.setId(oldPhoto.getId());
                         photoTable.remove(oldPhoto);
                         photoTable.add(photo);
                         photo.setReread(true);
                         photo.getEXIF();
                         photo.setReread(true);
                         System.out.println("\tThe JPEG is replaced! photo ID: " + photo.getId());
                     } else {
                         oldPhoto.upDate(photo);
                         System.out.println("\tphoto ID: " + oldPhoto.getId());
                     }
                 } else {// This means new photo.
                     photo.setNewPhoto(true);
                     photo.getEXIF();
                     System.out.println("\tnew photo ID: " + photo.getId());
                 }
                 photo = null;
             } else if(qualifiedName.equals("pubDate")){
                 try {
                     Date photoPublishedDate = rssDateFormat.parse(textBuffer);
                     photo.setPublishedDate(photoPublishedDate);
                    if(photoPublishedDate.after(readDate)){
                         readDate = photoPublishedDate;
                     }
                     System.out.println("\tdate:" + photo.getPublishedDate());
                 } catch (ParseException ex) {
                     System.out.println("\tfail to parse date! original is :"+textBuffer);
                     photo.setPublishedDate(new Date());
                 }
             }else if(qualifiedName.equals("gml:pos")&&stack.search("georss:where")==2){
                 String[] st = textBuffer.split("\\s");
                 photo.setLat(Double.parseDouble(st[0]));
                 photo.setLon(Double.parseDouble(st[1]));
             } else if(qualifiedName.equals("georss:point")){
                 String[] st = textBuffer.split("\\s");
                 photo.setLat(Double.parseDouble(st[0]));
                 photo.setLon(Double.parseDouble(st[1]));
             } else if(qualifiedName.equals("description")){
                 description=textBuffer;
             } else if(qualifiedName.equals("content:encoded")){
                 contextEncoded=textBuffer;
             } else if(qualifiedName.equals("title")){
                 photo.setTitle(entity(textBuffer));
                 System.out.println("\ttitle: " + photo.getTitle());
             } else if(qualifiedName.equals("link")){
                 photo.setLink(textBuffer);
                 System.out.println("\tlink: " + photo.getLink());
             } else if(qualifiedName.equals("media:keywords")){
                 System.out.println("\tmedia:keywords: "+textBuffer);
                 String [] delimited=textBuffer.split(", ");
                 machineTags(delimited);
             } else if(qualifiedName.equals("media:category")){
                 System.out.println("\tmedia:category: "+textBuffer);
                 String [] delimited=textBuffer.split(" ");
                 machineTags(delimited);
             } else if(qualifiedName.equals("category")){
                 System.out.println("\tcategory: "+textBuffer);
                 machineTags(textBuffer);
             }
         }
         textBuffer="";
     }
 
     @Override
     public void endDocument() throws SAXException {
         System.out.println("RSS Done");
 //        readDate = new Date();
     }
 
     @Override
     public void startCDATA(){
         this.inCDATA = true;
         String s;
         if((s=stack.peek()).equals("content:encoded")){
             contextCDATA=true;
         } else if(s.equals("description")){
             descriptionCDATA=true;
         }
     }
 
     @Override
     public void endCDATA(){
         this.inCDATA = false;
     }
 
     public void startDTD(String name, String publicId, String systemId) throws SAXException {
  
     }
 
     public void endDTD() throws SAXException {
  
     }
 
     public void startEntity(String name) throws SAXException {
     
     }
 
     public void endEntity(String name) throws SAXException {
  
     }
 
     public void comment(char[] ch, int start, int length) throws SAXException {
    
     }
 
     public void setDocumentLocator(Locator locator) {
   
     }
 
     public void startPrefixMapping(String prefix, String uri) throws SAXException {
  
     }
 
     public void endPrefixMapping(String prefix) throws SAXException {
       
     }
 
     public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
     
     }
 
     public void processingInstruction(String target, String data) throws SAXException {
       
     }
 
     public void skippedEntity(String name) throws SAXException {
    
     }
 
 }
