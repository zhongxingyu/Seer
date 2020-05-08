 package com.atlassian.javascript.ajs.selenium;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class AUIInlineDialogTest extends AUISeleniumTestCase
 {
 
     //Test to make sure InlineDialogs can be created correctly
     public void testAUIInlineDialogCreate()
     {
         openTestPage("test.html");  //open test page
         String creationString1 = createInlineDialog("#inlineDialog1","1","'dialog-content.html'", null);    //create inline-dialog 1
         String creationString2 = createInlineDialog("#inlineDialog2","2","'dialog-content.html'", null);    //create inline-dialog 2
 
         assertTrue("InlineDialog1 not created successfully: " + creationString1 , !creationString1.isEmpty());
         assertTrue("InlineDialog2 not created successfully: " + creationString2 , !creationString2.isEmpty());
     }
 
     public void testAUIInlineDialogNoBind()
     {
         openTestPage("test.html");  //open test page
         String creationString = createInlineDialog("#inlineDialog1","1","'dialog-content.html'", "{noBind:'true'}");    //create inline-dialog 1
 
         assertTrue("InlineDialog1 not created successfully: " + creationString, !creationString.isEmpty());
     }
 
//    public void testAUIInlineDialogFunctionPass()
//    {
//        openTestPage("test.html");  //open test page
//        createInlineDialog("#inlineDialog1","1","function(content, trigger, showPopup){window.AJS.$(trigger).append('<div id= \"testFunction\"></div>');}", null);    //create inline-dialog 1
//        client.click("inlineDialog1");
//        client.waitForCondition("selenium.isElementPresent('testFunction')");
//        assertThat.elementPresent("testFunction");
//
//    }
 
     private String createInlineDialog(String element, String number, String contents, String arguments)
     {
         String runString = "window.AJS.InlineDialog(window.AJS.$('"+element+"'), "+number+","+contents;
         if(arguments != null)
         {
             runString = runString + ", " + arguments + ");";
         } else
         {
             runString = runString+");";
         }
         System.out.println(runString);
         return client.getEval(runString);
     }
 }
