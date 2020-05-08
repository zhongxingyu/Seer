 package com.ecyrd.jspwiki.auth;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationConverter;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.ecyrd.jspwiki.TestEngine;
 import com.ecyrd.jspwiki.util.TextUtil;
 
 
 /**
  * Tests the UserProfile class.
  *
  * @author Janne Jalkanen
  */
 public class UserProfileTest
         extends TestCase
 {
     /**
      * Creates a new UserProfileTest object.
      *
      * @param s DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public UserProfileTest(String s)
             throws Exception
     {
         super(s);
 
        Configuration conf = TestEngine.getConfiguration("/jspwiki_auth.properties");
         PropertyConfigurator.configure(ConfigurationConverter.getProperties(conf));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void setUp()
             throws Exception
     {
     }
 
     /**
      * DOCUMENT ME!
      */
     public void tearDown()
     {
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testEquals()
     {
         UserProfile p = new UserProfile();
         UserProfile p2 = new UserProfile();
 
         p.setName("Alice");
         p2.setName("Bob");
 
         assertFalse(p.equals(p2));
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testEquals2()
     {
         UserProfile p = new UserProfile();
         UserProfile p2 = new UserProfile();
 
         p.setName("Alice");
         p2.setName("Alice");
 
         assertTrue(p.equals(p2));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testStringRepresentation()
             throws Exception
     {
         UserProfile p = UserProfile.parseStringRepresentation("username=JanneJalkanen");
 
         assertEquals("name", "JanneJalkanen", p.getName());
     }
 
     /**
      * Sometimes not all servlet containers offer you correctly decoded cookies.  Reported by
      * KalleKivimaa.
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testBrokenStringRepresentation()
             throws Exception
     {
         UserProfile p = UserProfile.parseStringRepresentation("username%3DJanneJalkanen");
 
         assertEquals("name", "JanneJalkanen", p.getName());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testUTFStringRepresentation()
             throws Exception
     {
         UserProfile p = new UserProfile();
 
         p.setName("M��m��");
 
         String s = p.getStringRepresentation();
 
         UserProfile p2 = UserProfile.parseStringRepresentation(s);
         assertEquals("name", "M��m��", p2.getName());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testUTFURLStringRepresentation()
             throws Exception
     {
         UserProfile p =
             UserProfile.parseStringRepresentation("username=" + TextUtil.urlEncodeUTF8("M��m��"));
 
         assertEquals("name", "M��m��", p.getName());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public static Test suite()
     {
         return new TestSuite(UserProfileTest.class);
     }
 }
