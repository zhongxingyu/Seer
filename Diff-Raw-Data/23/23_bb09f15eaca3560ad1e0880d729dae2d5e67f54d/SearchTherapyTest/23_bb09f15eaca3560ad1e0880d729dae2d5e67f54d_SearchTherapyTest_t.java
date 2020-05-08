 /**
  * @author pandyas
  * 
 * $Id: SearchTherapyTest.java,v 1.3 2005-12-29 18:43:48 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
 * Revision 1.2  2005/12/16 17:43:13  pandyas
 * Added a therapy publication to script - there are two issues that need resolved - there is additional source code to upload for this to run
 *
  * Revision 1.1  2005/12/13 20:39:14  pandyas
  * JUnit test case for Search Therapy - depends on other changes in main tree that will NOT be uploaded until we go to production
  *
  * 
  */
 
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.PublicationForm;
 import gov.nih.nci.camod.webapp.form.TherapyForm;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import web.base.BaseModelNeededTest;
 import web.util.TestUtil;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 public class SearchTherapyTest extends BaseModelNeededTest {
 
 	public SearchTherapyTest(String arg0) {
 		super(arg0);
 	}
 
 	protected void setUp() throws Exception {
 
         ResourceBundle theBundle = ResourceBundle.getBundle("test");
 
         String theUsername = theBundle.getString("username");
         String thePassword = theBundle.getString("password");
         
         loginToApplication(theUsername, thePassword);
         createModel();		
 	}
 
 	protected void tearDown() throws Exception {
         deleteModel();
         logoutOfApplication();
 	}
 	
     public static Test suite() {
         TestSuite suite = new TestSuite(SearchTherapyTest.class);
         return suite;
     }
     
 	public void testSearchForTherapy() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding a Therapy
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Therapy");
 		assertNotNull("Unable to find link to enter a Therapy", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Chemical Class:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("therapyForm");
 
 		TherapyForm theForm = new TherapyForm();
         theForm.setNSCNumber("33832"); 
         theForm.setCASNumber("50-81-7");
         
 		List theParamsToIgnore = new ArrayList();
 		theParamsToIgnore.add("chemicalClasses");
 		theParamsToIgnore.add("selectedChemicalClasses");
 		theParamsToIgnore.add("processes");
 		theParamsToIgnore.add("selectedProcesses");
 		theParamsToIgnore.add("targets");
 		theParamsToIgnore.add("selectedTargets");
 		theParamsToIgnore.add("administrativeRoute");		
 	
 		/* Add parameters found on submit screen but not displayed on search screen  */
 		List theParamsToSkip = new ArrayList();
 		theParamsToSkip.add("selectedChemicalClasses");
 		theParamsToSkip.add("selectedProcesses");
 		theParamsToSkip.add("selectedTargets");
 		
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		
 		assertCurrentPageContains("You have successfully added a Therapy to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"THERAPEUTIC APPROACHES");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 		
 		/******* Adding Publication to Therapy ***********/
 		navigateToModelForEditing(myModelName);
 
 		// Adding a Publication
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(
 				WebLink.MATCH_TEXT, "Enter Publication for Therapy");
 		assertNotNull("Unable to find link to enter a Publication for Therapy", theLink);
 		theCurrentPage = theLink.click();		
 		assertCurrentPageContains("For publications with a PubMed record");
 		theWebForm = theCurrentPage.getFormWithName("publicationForm");
		// set explicitly so validation works
		theWebForm.setParameter("firstTimeReported", "yes");		
 
 		PublicationForm thePubForm = new PublicationForm();
 		thePubForm.setFirstTimeReported("yes");
 		thePubForm.setPmid("16323327");
 		thePubForm.setYear("1999");
 		thePubForm.setStartPage("1111");
 		thePubForm.setEndPage("9999");		
 		thePubForm.setVolume("10");
 		thePubForm.setTitle("title");
 
		//TODO: clean up the use of aCellID and ACellID, ATherapyID vs aTherapyID and APubID vs aPubID
 		theParamsToIgnore = new ArrayList();
 		theParamsToIgnore.add("volume");
 		theParamsToIgnore.add("title");		
 		theParamsToIgnore.add("aCellID");
 		theParamsToIgnore.add("ACellID");		
 		theParamsToIgnore.add("APubID");
 		theParamsToIgnore.add("ATherapyID");
 		theParamsToIgnore.add("aTherapyID");		
 
 		//Add parameters found on submit screen but not displayed on search
 		theParamsToSkip = new ArrayList();
 		theParamsToSkip.add("firstTimeReported");
 		theParamsToSkip.add("aCellID");
 		theParamsToSkip.add("ACellID");		
 		theParamsToSkip.add("APubID");
 		theParamsToSkip.add("volume");
 		theParamsToSkip.add("title");
 		theParamsToSkip.add("ATherapyID");
 		theParamsToSkip.add("aTherapyID");		
 		
 
 		TestUtil.setRandomValues(thePubForm, theWebForm, false,	theParamsToIgnore);
 		TestUtil.setValuesOnForm(thePubForm, theWebForm);
 
 		theCurrentPage = theWebForm.submit();
		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Publication to this model! ");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		// check if publication is on cell line page
 		navigateToSpecificSearchPage(myModelName, "THERAPEUTIC APPROACHES");
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 		
 		/* check if publication is on publication search page
		* Fixed Defect #286 */ 
 		navigateToSpecificSearchPage(myModelName, "PUBLICATIONS");
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
				
 	}  
 
 }
