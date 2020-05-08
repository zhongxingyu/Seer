 /*
  * Copyright (c) 2006, 2007 Oliver Stewart.  All Rights Reserved.
  *
  * This file is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * This file is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.trailmagic.image.util;
 
//import com.sun.org.apache.xml.internal.resolver.Catalog;
//import com.sun.org.apache.xml.internal.resolver.CatalogManager;
//import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;
 import com.trailmagic.image.HeavyImageManifestation;
 import com.trailmagic.image.Image;
 import com.trailmagic.image.ImageFrame;
 import com.trailmagic.image.ImageGroup;
 import com.trailmagic.image.ImageGroupRepository;
 import com.trailmagic.image.ImageService;
 import com.trailmagic.image.Photo;
 import com.trailmagic.user.UserFactory;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 @Transactional(propagation=Propagation.REQUIRED,readOnly=false)
 public class ImagesParserImpl extends DefaultHandler implements ImagesParser {
 
     private static final String METADATA_FILENAME = "image-data.xml";
     /** ISO 8601 date format **/
     public static final String DATE_PATTERN = "YYYY-MM-DD";
 
     private static Logger s_logger = Logger.getLogger(ImagesParserImpl.class);
 
     private Image m_image;
     private ImageGroup m_roll;
     private HeavyImageManifestation m_manifestation;
     private boolean m_inImage;
     private boolean m_inRoll;
     private boolean m_inManifestation;
     private boolean m_inPhotoData;
     private ImageGroup m_photoRoll;
     private String m_photoFrameNum;
     private File m_baseDir;
     private StringBuffer m_characterData;
         private ImageGroupRepository imageGroupRepository;
     private UserFactory userFactory;
     private HibernateTemplate hibernateTemplate;
     private ImageService imageService;
 
     public ImagesParserImpl(ImageGroupRepository imageGroupRepository,
             UserFactory userFactory, HibernateTemplate hibernateTemplate,
             ImageService imageService) {
         super();
         this.imageGroupRepository = imageGroupRepository;
         this.userFactory = userFactory;
         this.hibernateTemplate = hibernateTemplate;
         this.imageService = imageService;
     }
 
     public File getBaseDir() {
         return m_baseDir;
     }
 
     public void setBaseDir(File baseDir) {
         this.m_baseDir = baseDir;
     }
 
     public void startDocument() {
         s_logger.debug("beginning parse");
 
         m_inImage = false;
         m_inRoll = false;
         m_inManifestation = false;
         m_inPhotoData = false;
         m_photoRoll = null;
         m_photoFrameNum = null;
     }
 
     public void endDocument() {
         // nothing to do ...transaction MUST be committed or rolled back
         // by an interceptor
     }
 
     public void error(SAXParseException e) throws SAXException {
         throw e;
     }
 
     public void fatalError(SAXParseException e) throws SAXException {
         throw e;
     }
 
     public void startElement(String uri, String localName, String qName,
                              Attributes attributes) {
         String eltName = qName;
 
         m_characterData = new StringBuffer();
 
         if ("image".equals(eltName)) {
             startImage();
         } else if ("roll".equals(eltName)) {
             startRoll();
         } else if ("image-manifestation".equals(eltName)) {
             startManifestation();
         } else if ("photo-data".equals(eltName)) {
             startPhotoData();
         }
     }
 
     public void endElement(String uri, String localName, String qName)
         throws SAXException {
 
         String eltName = qName;
 
         processCharacterData(m_characterData.toString(), eltName);
 
         if ("image".equals(eltName)) {
             endImage();
         } else if ("roll".equals(eltName)) {
             endRoll();
         } else if ("image-manifestation".equals(eltName)) {
             endManifestation();
         } else if ("photo-data".equals(eltName)) {
             endPhotoData();
         }
     }
 
     public void startImage() {
         m_image = new Image();
         m_inImage = true;
     }
 
     public void endImage() {
         s_logger.debug("endImage() called");
 //        imageService.saveNewImage(m_image);
 
         synchronized (hibernateTemplate) {
             hibernateTemplate.flush();
             hibernateTemplate.clear();
         }
 
         s_logger.debug("Image saved: " + m_image.getName() + " ("
                        + m_image.getId() + ")  Session cleared.");
         m_image = null;
         m_inImage = false;
     }
 
     public void startManifestation() {
         m_manifestation = new HeavyImageManifestation();
         m_manifestation.setImage(m_image);
         m_inManifestation = true;
     }
 
     public void endManifestation() {
         s_logger.debug("saving ImageManifestation: "
                        + "name: " + m_manifestation.getName()
                        + "height: " + m_manifestation.getHeight()
                        + "width: " + m_manifestation.getWidth()
                        + "format: " + m_manifestation.getFormat()
                        + "original: " + m_manifestation.isOriginal());
 
         importManifestation();
         m_manifestation = null;
         m_inManifestation = false;
     }
 
     public void startRoll() {
         m_roll = new ImageGroup();
         m_roll.setType(ImageGroup.Type.ROLL);
         m_roll.setSupergroup(null);
         m_roll.setUploadDate(new Date());
         m_inRoll = true;
     }
 
 
     public void endRoll() {
         s_logger.debug("endRoll() called");
         imageService.saveNewImageGroup(m_roll);
         m_inRoll = false;
     }
 
     public void startPhotoData() {
         m_inPhotoData = true;
         s_logger.debug("Processing photo data for image " + m_image.getId());
         // switch type to Photo.  this works because it hasn't been saved yet
         m_image = new Photo(m_image);
     }
 
     public void endPhotoData() {
         // process the roll information
         // add the photo to the m_photoRollName roll with frame number
         // m_photoFrameNum
 
         imageService.saveNewImage(m_image);
         ImageFrame frame = new ImageFrame();
         frame.setImageGroup(m_photoRoll);
         frame.setImage(m_image);
         frame.setPosition(Integer.parseInt(m_photoFrameNum));
 
         s_logger.debug("About to save frame: " + frame);
 
         imageService.saveNewImageFrame(frame);
 
         s_logger.debug("After save, frame id is: " + frame.getId());
 
         synchronized (hibernateTemplate) {
             hibernateTemplate.flush();
             hibernateTemplate.evict(frame);
         }
 
         s_logger.debug("Finished processing photo data for image "
                        + m_image.getId());
 
         m_photoFrameNum = null;
         m_photoRoll = null;
         m_inPhotoData = false;
     }
 
     public void characters(char ch[], int start, int length) {
         m_characterData.append(ch, start, length);
     }
 
     private void processCharacterData(String characterData,
                                       String currentElt) throws SAXException {
         if (m_inImage) {
             if (m_inManifestation) {
                 if ("name".equals(currentElt)) {
                     m_manifestation.setName(characterData);
                 } else if ("height".equals(currentElt)) {
                     m_manifestation.setHeight(Integer
                                               .parseInt(characterData));
                 } else if ("width".equals(currentElt)) {
                     m_manifestation.setWidth(Integer
                                              .parseInt(characterData));
                 } else if ("format".equals(currentElt)) {
                     m_manifestation.setFormat(characterData);
                 } else if ("original".equals(currentElt)) {
                     m_manifestation.setOriginal(new Boolean(characterData)
                                                 .booleanValue());
                 }
             } else if (m_inPhotoData) {
                 Photo photo = (Photo)m_image;
                 if ("roll-name".equals(currentElt)) {
                     //                    photo.setRoll(getRollByName(characterData));
                     // XXX: we should just use ImageGroup for this normally
                     // so make sure the roll exists as an IG, then
                     // XXX: borked if we don't have owner yet
                     synchronized (hibernateTemplate) {
                         hibernateTemplate.flush();
                     }
 
                     m_photoRoll =
                         imageGroupRepository.getRollByOwnerAndName(m_image.getOwner(),
                                                  characterData.trim());
                     if ( m_photoRoll == null ) {
                         s_logger.info("No roll by the given name and owner "
                                       + "found processing photo data, throwing"
                                       + " exception.");
                         throw new SAXException("Invalid or no roll name "
                                                + "specified: " + characterData
                                                + " (for owner "
                                                + m_image.getOwner()
                                                + ")");
                     }
 
                 } else if ("frame-number".equals(currentElt)) {
                     m_photoFrameNum = characterData;
                 } else if ("notes".equals(currentElt)) {
                     photo.setNotes(characterData);
                 } else if ("capture-date".equals(currentElt)) {
                     // XXX: use DateFormat
                     SimpleDateFormat format =
                         new SimpleDateFormat(DATE_PATTERN);
                     try {
                         photo.setCaptureDate(format.parse(characterData));
                     } catch (ParseException e) {
                         s_logger.warn("Error parsing capture-date: "
                                       + e.getMessage());
                     }
                 }
             } else {
                 if ( "name".equals(currentElt) ) {
                     m_image.setName(characterData);
                 } else if ( "display-name".equals(currentElt) ) {
                     m_image.setDisplayName(characterData);
                 } else if ( "caption".equals(currentElt) ) {
                     m_image.setCaption(characterData);
                 } else if ( "copyright".equals(currentElt) ) {
                     m_image.setCopyright(characterData);
                 } else if ( "creator".equals(currentElt) ) {
                     m_image.setCreator(characterData);
                 } else if ( "owner".equals(currentElt) ) {
                     String ownerName = characterData;
                     m_image.setOwner(userFactory.getByScreenName(ownerName));
                 } else if ( "number".equals(currentElt) ) {
                     m_image.setNumber(new Integer(characterData));
                 }
             }
         } else if (m_inRoll) {
             if ( "name".equals(currentElt) ) {
                 m_roll.setName(characterData);
             } else if ( "display-name".equals(currentElt) ) {
                 m_roll.setDisplayName(characterData);
             } else if ( "description".equals(currentElt) ) {
                 m_roll.setDescription(characterData);
             } else if ( "owner".equals(currentElt) ) {
                 String ownerName = characterData;
                 m_roll.setOwner(userFactory.getByScreenName(ownerName));
                 s_logger.debug("set roll " + m_roll.getName() + " owner to: "
                                + m_roll.getOwner());
             }
         }
     }
 
     public static void printUsage() {
         System.err.println("Usage: ImagesParser <base_dir>");
     }
     /*
     public static void importManifestation(File baseDir,
                                            ImageManifestation mf) {
         try {
             File srcFile = new File(baseDir, mf.getName());
             s_logger.info("Importing " + srcFile.getPath() + "...");
             if ( srcFile.length() > Integer.MAX_VALUE ) {
                 s_logger.warn("File is too big...skipping.");
                 return;
             }
             byte[] data = new byte[(int)srcFile.length()];
             FileInputStream fis = new FileInputStream(srcFile);
             fis.read(data);
             mf.setData(data);
             fis.close();
             s_logger.info("done");
         } catch (IOException e) {
             s_logger.error("Error: " + e.getMessage());
         }
     }
     */
 
     private void importManifestation() throws HibernateException {
         try {
             File srcFile = new File(m_baseDir, m_manifestation.getName());
             s_logger.info("Importing " + srcFile.getPath());
             if ( srcFile.length() > Integer.MAX_VALUE ) {
                 s_logger.info("File is too big...skipping "
                               + srcFile.getPath());
                 return;
             }
             FileInputStream fis = new FileInputStream(srcFile);
 
             m_manifestation.setData(Hibernate.createBlob(fis));
             imageService.saveNewImageManifestation(m_manifestation);
 
             fis.close();
             s_logger.info("Finished importing " + srcFile.getPath());
         } catch (IOException e) {
             s_logger.error("Error: " + e.getMessage());
             // I hope this causes a rollback
             throw new RuntimeException(e);
         }
     }
     
     @Transactional(propagation=Propagation.REQUIRED,readOnly=false)
     public void parseFile(File metadataFile) {
         try {
 
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser parser = factory.newSAXParser();
 
             s_logger.info("Parsing " + metadataFile.getPath() + "...");
             parser.parse(metadataFile, this);
             s_logger.info("done");
         } catch (Throwable t) {
             t.printStackTrace();
             System.exit(1);
         }
         
     }
 
     @Transactional(propagation=Propagation.REQUIRED,readOnly=false)
     public void parseFile(File metadataFile, File baseDir) {
         setBaseDir(baseDir);
         parseFile(metadataFile);
     }
 
     @Transactional(propagation=Propagation.REQUIRED,readOnly=false)
     public void parse(InputStream inputStream) {
         try {
 
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser parser = factory.newSAXParser();
             XMLReader reader = parser.getXMLReader();
             
//            CatalogManager cm = new CatalogManager();
//            Catalog catalog = cm.getCatalog();
//            catalog.parseCatalog(getClass().getClassLoader().getResource("CatalogManager.properties"));
//            reader.setEntityResolver(new CatalogResolver(cm));
 
 
             s_logger.info("Parsing input stream...");
             parser.parse(inputStream, this);
             s_logger.info("done");
         } catch (Throwable t) {
             t.printStackTrace();
             System.exit(1);
         }
     }
     
     public void parse(InputStream inputStream, File baseDir) {
         setBaseDir(baseDir);
         parse(inputStream);
     }
 
     public static final void main(String[] args) {
         File baseDir = new File(args[0]);
         if (!baseDir.isDirectory()) {
             printUsage();
             System.exit(1);
         }
 
         File metadataFile = new File(baseDir, METADATA_FILENAME);
         if (!metadataFile.isFile()) {
             s_logger.error("Error: Couldn't find " + baseDir
                                + File.separator
                                + METADATA_FILENAME);
             System.exit(1);
         }
 
         s_logger.info("Starting context...");
 
         ClassPathXmlApplicationContext appContext =
             new ClassPathXmlApplicationContext(new String[]
                 {"applicationContext-global.xml",
                  "applicationContext-user.xml",
                  "applicationContext-imagestore.xml",
                  "applicationContext-imagestore-authorization.xml",
                  "applicationContext-standalone.xml"});
         ImagesParser handler =
             (ImagesParser)appContext.getBean("imagesParser");
 
         s_logger.info("Beginning parse.");
 
         try {
             handler.parseFile(baseDir, metadataFile);
             s_logger.info("Parse completed.");
         } catch (Throwable t) {
             s_logger.error("Parse Failed!", t);
         }
 
         System.exit(0);
     }
 }
