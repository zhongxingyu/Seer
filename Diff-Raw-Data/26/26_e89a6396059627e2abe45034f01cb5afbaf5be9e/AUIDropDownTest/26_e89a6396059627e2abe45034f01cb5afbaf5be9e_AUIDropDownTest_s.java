 package it.com.atlassian.aui.javascript.integrationTests;
 
 public class AUIDropDownTest extends AbstractAUISeleniumTestCase
 {
     private static final String TEST_PAGE = "test-pages/dropdown/dropdown-test.html";
 
     //Test to make sure dropdowns show correctly after being clicked.
     public void testAUIDropDownShow()
     {
         openTestPage(TEST_PAGE);
 
        client.click("css=div#dropDown-standard .aui-dd-trigger");
        assertThat.elementVisible("css=div#dropDown-standard .aui-dropdown");
     }
 
     //test to make sure dropdowns hide after a click on the body
     public void testAUIDropDownHide()
     {
         openTestPage(TEST_PAGE);
         
        client.click("css=div#dropDown-standard .aui-dd-trigger");
        assertThat.elementVisible("css=div#dropDown-standard .aui-dropdown");
 
         client.click("css=body");
        assertThat.elementNotVisible("css=div#dropDown-standard .aui-dropdown");
     }
 
     public void testDropdownLeftAlign(){
 
         openTestPage(TEST_PAGE);
 
        client.click("css=div#dropDown-left .aui-dd-trigger");
 
        assertThat.elementVisible("css=div#dropDown-left .aui-dropdown");
        assertEquals("left-aligned dropdown is not left-aligned", "0px", getCss("left", "#dropDown-left .aui-dropdown"));
     }
 
     public void testDropdownRightAlign(){
 
         openTestPage(TEST_PAGE);
 
        client.click("css=div#dropDown-right .aui-dd-trigger");
        assertThat.elementVisible("css=div#dropDown-right .aui-dropdown");
         assertEquals("right-aligned dropdown is not right-aligned", "0px", getCss("right", "#dropDown-right .aui-dropdown"));
     }
 
 }
