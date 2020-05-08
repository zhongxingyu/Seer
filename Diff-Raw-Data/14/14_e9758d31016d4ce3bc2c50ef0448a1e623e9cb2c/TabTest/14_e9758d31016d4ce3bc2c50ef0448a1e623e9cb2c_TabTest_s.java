 package gov.nih.nci.cabig.ctms.web.tabs;
 
 import junit.framework.TestCase;
 
 /**
  * @author Rhett Sutphin
  */
 public class TabTest extends TestCase {
     private Flow<Object> flow;
     private Tab<Object> tab3;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         flow = new Flow<Object>("Test flow");
         flow.addTab(new Tab<Object>());
         flow.addTab(new Tab<Object>());
         flow.addTab(new Tab<Object>());
         flow.addTab(new Tab<Object>());
         flow.addTab(new Tab<Object>());
 
         tab3 = flow.getTab(3);
     }
 
     public void testDefaultRefData() throws Exception {
         assertNotNull("Default refdata null", tab3.referenceData());
         assertEquals("Default refdata not empty", 0, tab3.referenceData().size());
     }
 
     public void testDefaultTarget() throws Exception {
         assertEquals("Default target not next", 4, tab3.getTargetNumber());
     }
 
     public void testDefaultTargetTab() throws Exception {
         assertSame(flow.getTab(4), tab3.getTargetTab());
     }
 }
