 package it.com.atlassian.plugin.refimpl;
 
 public class TestIndex extends AbstractRefappTestCase
 {
     public TestIndex(String name)
     {
         super(name);
     }
 
     public void testIndex()
     {
         beginAt("/index.jsp");
         assertTextPresent("com.atlassian.plugin.osgi.bridge");
 
         assertTextNotPresent("Installed");
        assertTextPresent("Atlassian Plugins - ");
     }
 }
