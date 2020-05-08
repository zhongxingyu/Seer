 package com.solidstategroup.radar.test.web;
 
 import com.solidstategroup.radar.web.RadarApplication;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
 
 public abstract class BasePageTest {
 
     protected WicketTester tester;
 
     @Before
     public void setUp() {
        tester = new WicketTester(new RadarApplication());
     }
 
 }
