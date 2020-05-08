 package com.atlassian.javascript.ajs.selenium;
 
 /**
  * Selenium test for events.js
  */
 public class AUIEventTest extends AUISeleniumTestCase
 {
     public void onSetUp()
     {
         openTestPage("test.html");  //open test page
     }
 
     /**
      * Make sure a simple bind and trigger works
      */
     public void testAJSeventBindsAndTriggers()
     {
        client.getEval("window.AJS.bind('abc-event', null, function(data) { testResult = true; });");
         client.getEval("window.AJS.trigger('abc-event');");
         assertEquals("Trigger should be fired", "true", client.getEval("testResult"));
     }
     
     /**
      * Make sure a simple bind, unbind and trigger works
      */
     public void testAJSeventBindsAndTriggersWithUnbind()
     {
         client.getEval("testResult = 0; window.AJS.bind('abc-event', function(data) { testResult++; });");
         client.getEval("window.AJS.trigger('abc-event');");
         assertEquals("Trigger should be fired", "1", client.getEval("testResult"));
         client.getEval("window.AJS.unbind('abc-event');");
         client.getEval("window.AJS.trigger('abc-event');");
         assertEquals("Trigger should be fired only once", "1", client.getEval("testResult"));
     }
 
 
 }
