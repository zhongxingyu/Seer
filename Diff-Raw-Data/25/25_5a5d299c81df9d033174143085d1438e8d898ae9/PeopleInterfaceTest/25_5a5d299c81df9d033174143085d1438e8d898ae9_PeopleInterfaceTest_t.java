 /* Copyright 2004, Aetrion LLC.  All Rights Reserved. */
 
 package com.aetrion.flickr;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.TestCase;
 
 import org.xml.sax.SAXException;
 
 import com.aetrion.flickr.auth.Auth;
 import com.aetrion.flickr.auth.AuthInterface;
 import com.aetrion.flickr.people.PeopleInterface;
 import com.aetrion.flickr.people.User;
 import com.aetrion.flickr.photos.Photo;
 import com.aetrion.flickr.photos.PhotoList;
 import com.aetrion.flickr.util.IOUtilities;
 
 /**
  * @author Anthony Eden
 * @version $Id: PeopleInterfaceTest.java,v 1.8 2007/02/28 18:57:15 x-mago Exp $
  */
 public class PeopleInterfaceTest extends TestCase {
 
     Flickr flickr = null;
     Properties properties = null;
 
     public void setUp() throws ParserConfigurationException, IOException, FlickrException, SAXException {
         //Flickr.debugStream = true;
 
         InputStream in = null;
         try {
             in = getClass().getResourceAsStream("/setup.properties");
             properties = new Properties();
             properties.load(in);
 
             REST rest = new REST();
             rest.setHost(properties.getProperty("host"));
 
             flickr = new Flickr(properties.getProperty("apiKey"), rest);
 
             RequestContext requestContext = RequestContext.getRequestContext();
             requestContext.setSharedSecret(properties.getProperty("secret"));
 
             AuthInterface authInterface = flickr.getAuthInterface();
             Auth auth = authInterface.checkToken(properties.getProperty("token"));
             requestContext.setAuth(auth);
         } finally {
             IOUtilities.close(in);
         }
     }
 
     public void testFindByEmail() throws FlickrException, IOException, SAXException {
         PeopleInterface iface = flickr.getPeopleInterface();
         User person = iface.findByEmail(properties.getProperty("email"));
         assertNotNull(person);
         assertEquals(person.getId(), properties.getProperty("nsid"));
         assertEquals(person.getUsername(), properties.getProperty("username"));
     }
 
     public void testFindByUsername() throws FlickrException, IOException, SAXException {
         PeopleInterface iface = flickr.getPeopleInterface();
         User person = iface.findByUsername(properties.getProperty("username"));
         assertNotNull(person);
         assertEquals(properties.getProperty("nsid"), person.getId());
         assertEquals(properties.getProperty("username"), person.getUsername());
     }
 
     public void testGetInfo() throws FlickrException, IOException, SAXException {
         PeopleInterface iface = flickr.getPeopleInterface();
         User person = iface.getInfo(properties.getProperty("nsid"));
         assertNotNull(person);
         assertEquals(properties.getProperty("nsid"), person.getId());
         assertEquals(properties.getProperty("username"), person.getUsername());
     }
 
     public void testGetPublicGroups() throws FlickrException, IOException, SAXException {
         PeopleInterface iface = flickr.getPeopleInterface();
         Collection groups = iface.getPublicGroups(properties.getProperty("nsid"));
         assertNotNull(groups);
        assertTrue(groups.size() > 40);
     }
 
     public void testGetPublicPhotos() throws FlickrException, IOException, SAXException {
         PeopleInterface iface = flickr.getPeopleInterface();
         PhotoList photos = iface.getPublicPhotos(properties.getProperty("nsid"), 0, 0);
         assertNotNull(photos);
         assertTrue(photos.size() > 60);
 	    Photo photo = (Photo) photos.get(0);
         assertTrue(photo.getOriginalUrl().endsWith("jpg"));
         assertEquals("jpg", photo.getOriginalFormat());
 	    photo = (Photo) photos.get(50);
         assertTrue(photo.getOriginalUrl().endsWith("jpg"));
         assertEquals("jpg", photo.getOriginalFormat());
     }
 
     public void testGetUploadStatus() {
 
     }
 
 }
