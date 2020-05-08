 package com.ecyrd.jspwiki.auth.modules;
 
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.commons.configuration.Configuration;
 
 import com.ecyrd.jspwiki.TestEngine;
 import com.ecyrd.jspwiki.auth.UserManager;
 import com.ecyrd.jspwiki.auth.UserProfile;
 import com.ecyrd.jspwiki.auth.WikiGroup;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $author$
  * @version $Revision$
  */
 public class WikiDatabaseTest
         extends TestCase
 {
     /** DOCUMENT ME! */
     TestEngine m_engine;
 
     /**
      * Creates a new WikiDatabaseTest object.
      *
      * @param s DOCUMENT ME!
      */
     public WikiDatabaseTest(String s)
     {
         super(s);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void setUp()
             throws Exception
     {
        Configuration conf = TestEngine.getConfiguration("/jspwiki_auth.properties");
 
         // props.setProperty(WikiProperties.PROP_CLASS_USERDATABASE, "WikiDatabase");
         m_engine = new TestEngine(conf);
 
         String text1 = "Foobar.\n\n[{SET members=Alice, Bob, Charlie}]\n\nBlood.";
         String text2 = "[{SET members=Bob}]";
 
         m_engine.saveText("TestGroup", text1);
         m_engine.saveText("TestGroup2", text2);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void tearDown()
     {
         m_engine.cleanup();
     }
 
     private boolean containsGroup(List l, String name)
     {
         for (Iterator i = l.iterator(); i.hasNext();)
         {
             WikiGroup group = (WikiGroup) i.next();
 
             if (group.getName().equals(name))
             {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testGroupFormation()
             throws Exception
     {
         UserManager mgr = m_engine.getUserManager();
 
         UserProfile p = new UserProfile();
         p.setName("Alice");
         p.setLoginStatus(UserProfile.PASSWORD);
 
         List l = mgr.getGroupsForPrincipal(p);
 
         assertTrue("Alice is in the wrong group (0)", containsGroup(l, "TestGroup"));
         assertTrue("Alice is in the wrong group (1)", containsGroup(l, "Guest"));
         assertTrue("Alice is in the wrong group (2)", containsGroup(l, "NamedGuest"));
         assertTrue("Alice is in the wrong group (3)", containsGroup(l, "KnownPerson"));
 
         assertEquals("Alice has too many groups", 4, l.size());
 
         p.setName("Bob");
         l = mgr.getGroupsForPrincipal(p);
 
         assertTrue("Bob is in the wrong group (0)", containsGroup(l, "TestGroup"));
         assertTrue("Bob is in the wrong group (1)", containsGroup(l, "TestGroup2"));
         assertTrue("Bob is in the wrong group (2)", containsGroup(l, "Guest"));
 
         assertTrue("Bob is in the wrong group (3)", containsGroup(l, "NamedGuest"));
         assertTrue("Bob is in the wrong group (4)", containsGroup(l, "KnownPerson"));
 
         assertEquals("Bob has too many groups", 5, l.size());
 
         p.setName("David");
         p.setLoginStatus(UserProfile.NONE);
         l = mgr.getGroupsForPrincipal(p);
 
         assertEquals("David has too many groups", 1, l.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public static Test suite()
     {
         return new TestSuite(WikiDatabaseTest.class);
     }
 }
