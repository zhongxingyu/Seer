 package com.atlassian.plugin.impl;
 
 import junit.framework.TestCase;
 
 public class TestAbstractPlugin extends TestCase
 {
     public void testCompareTo()
     {
         AbstractPlugin p1 = new StaticPlugin(){};
         p1.setKey("foo");
         AbstractPlugin p2 = new StaticPlugin(){};
         p2.setKey("bar");
 
         assertTrue(p1.compareTo(p2) == -1 * p2.compareTo(p1));
     }

    public void testCompareToWithNullPluginInformation()
    {
        AbstractPlugin p1 = new StaticPlugin(){};
        p1.setKey("foo");
        p1.setPluginInformation(null);
        AbstractPlugin p2 = new StaticPlugin(){};
        p2.setKey("foo");

        assertTrue(p1.compareTo(p2) == -1 * p2.compareTo(p1));

        p1 = new StaticPlugin(){};
        p1.setKey("foo");
        p2 = new StaticPlugin(){};
        p2.setKey("foo");
        p2.setPluginInformation(null);

        assertTrue(p1.compareTo(p2) == -1 * p2.compareTo(p1));
    }
 }
