 package com.trailmagic.image.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import net.sf.hibernate.Hibernate;
 import net.sf.hibernate.HibernateException;
 import net.sf.hibernate.Session;
 import net.sf.hibernate.SessionFactory;
 import net.sf.hibernate.Transaction;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.orm.hibernate.SessionFactoryUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.trailmagic.image.*;
 import com.trailmagic.user.User;
 import com.trailmagic.user.UserFactory;
 
 public class ImagesParser extends DefaultHandler
     implements ApplicationContextAware {
 
     private static final String USER_FACTORY_BEAN = "userFactory";
     private static final String IMAGE_GROUP_FACTORY_BEAN = "imageGroupFactory";
     private static final String METADATA_FILENAME = "image-data.xml";
     /** ISO 8601 date format **/
     public static final String DATE_PATTERN = "YYYY-MM-DD";
 
     private static Logger s_logger = Logger.getLogger(ImagesParser.class);
 
     private String m_inElement;
     private String m_inSubElement;
     private Image m_image;
     private ImageGroup m_roll;
     private HeavyImageManifestation m_manifestation;
     private Session m_session;
     private SessionFactory m_sessionFactory;
     private Transaction m_transaction;
     private boolean m_inImage;
     private boolean m_inRoll;
     private boolean m_inManifestation;
     private boolean m_inPhotoData;
     private boolean m_closeSession;
     private ImageGroup m_photoRoll;
     private String m_photoRollName;
     private String m_photoFrameNum;
     private File m_baseDir;
     private StringBuffer m_characterData;
 
     private ApplicationContext m_appContext;
 
     public ImagesParser() {
         this(true);
     }
 
     public ImagesParser(boolean closeSession) {
         if ( closeSession ) {
             s_logger.debug("New ImagesParser: session closing enabled");
         } else {
             s_logger.debug("New ImagesParser: session closing disabled");
         }
         m_closeSession = closeSession;
     }
 
     public SessionFactory getSessionFactory() {
         return m_sessionFactory;
     }
 
     public void setSessionFactory(SessionFactory sf) {
         m_sessionFactory = sf;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) {
         m_appContext = applicationContext;
     }
 
     public File getBaseDir() {
         return m_baseDir;
     }
 
     public void setBaseDir(File baseDir) {
         m_baseDir = baseDir;
     }
 
     public void startDocument() {
         try {
             s_logger.debug("beginning parse");
 
             m_session =
                 SessionFactoryUtils.getSession(m_sessionFactory, false);
             m_transaction = m_session.beginTransaction();
             m_inImage = false;
             m_inRoll = false;
             m_inManifestation = false;
             m_inPhotoData = false;
             m_photoRoll = null;
             m_photoRollName = null;
             m_photoFrameNum = null;
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
     }
 
     public void endDocument() {
         try {
             m_transaction.commit();
             s_logger.debug("ImagesParser: committed transaction.");
             if ( m_closeSession ) {
                 SessionFactoryUtils.releaseSession(m_session,
                                                    m_sessionFactory);
             }
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
     }
 
     private void abort() {
         s_logger.warn("aborting");
 
         try {
             m_transaction.rollback();
             if (m_closeSession) {
                 SessionFactoryUtils.releaseSession(m_session,
                                                    m_sessionFactory);
             }
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
     }
     public void error(SAXParseException e) throws SAXException {
         throw e;
     }
 
     public void fatalError(SAXParseException e) throws SAXException {
         abort();
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
         try {
             s_logger.debug("endImage() called");
             m_session.saveOrUpdate(m_image);
             synchronized (m_session) {
                 m_session.flush();
                 m_session.clear();
             }
             s_logger.debug("Image saved: " + m_image.getName() + " ("
                            + m_image.getId() + ")  Session cleared.");
             System.gc();
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
         m_image = null;
         m_inImage = false;
     }
 
     public void startManifestation() {
         m_manifestation = new HeavyImageManifestation();
         m_manifestation.setImage(m_image);
         m_inManifestation = true;
     }
 
     public void endManifestation() {
         try {
             s_logger.debug("saving ImageManifestation: "
                            + "name: " + m_manifestation.getName()
                            + "height: " + m_manifestation.getHeight()
                            + "width: " + m_manifestation.getWidth()
                            + "format: " + m_manifestation.getFormat()
                            + "original: " + m_manifestation.isOriginal());
             
             importManifestation();
             m_manifestation = null;
             m_inManifestation = false;
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
     }
 
     public void startRoll() {
         m_roll = new ImageGroup();
         m_roll.setType(ImageGroup.ROLL_TYPE);
         m_roll.setSupergroup(null);
         m_inRoll = true;
     }
 
 
     public void endRoll() {
         try {
             s_logger.debug("endRoll() called");
             m_session.saveOrUpdate(m_roll);
             s_logger.debug("Roll saved: " + m_roll.getName() + " ("
                            + m_roll.getId() + ")");
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
         }
         m_roll = null;
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
         
         ImageFrame frame = new ImageFrame();
         frame.setImageGroup(m_photoRoll);
         frame.setImage(m_image);
         frame.setPosition(Integer.parseInt(m_photoFrameNum));
 
         try {
             // XXX: have to save the image first
             m_session.saveOrUpdate(m_image);
             m_session.saveOrUpdate(frame);
             synchronized (m_session) {
                 m_session.flush();
                 m_session.evict(frame);
             }
         } catch (HibernateException e) {
             throw SessionFactoryUtils.convertHibernateAccessException(e);
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
                     ImageGroupFactory gf =
                         (ImageGroupFactory)
                         m_appContext.getBean(IMAGE_GROUP_FACTORY_BEAN);
                     // XXX: borked if we don't have owner yet
                     try {
                         synchronized (m_session) {
                             m_session.flush();
                         }
                     } catch (HibernateException e) {
                         s_logger.error("Error flushing session before "
                                        + "roll query.", e);
                     }
                     m_photoRoll =
                         gf.getRollByOwnerAndName(m_image.getOwner(),
                                                  characterData);
                     if ( m_photoRoll == null ) {
                         s_logger.info("No roll by the given name and owner "
                                       + "found processing photo data, throwing"
                                       + " exception.");
                         throw new SAXException("Invalid or no roll name "
                                                + "specified: " + characterData
                                                + " (for owner "
                                                + m_image.getOwner()
                                                  .getScreenName()
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
                     UserFactory uf =
                         (UserFactory)m_appContext.getBean(USER_FACTORY_BEAN);
                     m_image.setOwner(uf.getByScreenName(ownerName));
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
                 UserFactory uf =
                     (UserFactory)m_appContext.getBean(USER_FACTORY_BEAN);
                 m_roll.setOwner(uf.getByScreenName(ownerName));
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
             System.out.print("Importing " + srcFile.getPath() + "...");
             if ( srcFile.length() > Integer.MAX_VALUE ) {
                 s_logger.warn("File is too big...skipping.");
                 return;
             }
             byte[] data = new byte[(int)srcFile.length()];
             FileInputStream fis = new FileInputStream(srcFile);
             fis.read(data);
             mf.setData(data);
             fis.close();
             System.out.println("done");
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
             m_session.saveOrUpdate(m_manifestation);
 
             s_logger.info("ImageManifestation saved: "
                           + m_manifestation.getName()
                           + " (" + m_manifestation.getId() + ")"
                           + "...flushing session and evicting manifestation.");
 
             synchronized (m_session) {
                 m_session.flush();
                 m_session.evict(m_manifestation);
             }
             
             fis.close();
             s_logger.info("Finished importing " + srcFile.getPath());
         } catch (IOException e) {
             s_logger.error("Error: " + e.getMessage());
         }
     }
 
     public static final void main(String[] args) {
         File baseDir = new File(args[0]);
         if (!baseDir.isDirectory()) {
             printUsage();
             System.exit(1);
         }
 
         File metadataFile = new File(baseDir, METADATA_FILENAME);
         if (!metadataFile.isFile()) {
             System.err.println("Error: Couldn't find " + baseDir
                                + File.separator
                                + METADATA_FILENAME);
             System.exit(1);
         }
 
         ClassPathXmlApplicationContext appContext =
             new ClassPathXmlApplicationContext(new String[]
                 {"applicationContext-standalone.xml"});
         ImagesParser handler =
             (ImagesParser)appContext.getBean("imagesParser");
         //        handler.setApplicationContext(appContext);
         try {
             // make sure there's a session bound to the thread
             Session session =
                 SessionFactoryUtils.getSession(handler.getSessionFactory(),
                                                true);
             assert (session != null);
 
             handler.setBaseDir(baseDir);
 
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser parser = factory.newSAXParser();
 
             System.out.print("Parsing " + metadataFile.getPath() + "...");
             System.out.flush();
             parser.parse(metadataFile, handler);
             System.out.println("done");
 
             /*
             System.out.println("Importing image files:");
             List manifestations = handler.getManifestations();
             Iterator iter = manifestations.iterator();
             while (iter.hasNext()) {
                 importManifestation(baseDir, (ImageManifestation)iter.next());
             }
             System.out.println("All done.");
             */
         } catch (Throwable t) {
             t.printStackTrace();
             System.exit(1);
         }
         System.exit(0);
     }
 
 }
