 /**
  * @author pandyas
  * 
 * $Id: SearchCellLinesTest.java,v 1.3 2005-12-22 19:57:13 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.2  2005/12/16 17:27:34  pandyas
  * Added a cell line publication to script - there are two issues that need resolved - there is additional source code to upload for this to run
  *
  * Revision 1.1  2005/12/13 19:55:49  pandyas
  * JUnit test case for Search Cell Line
  *
  * 
  */
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.CellLineForm;
 import gov.nih.nci.camod.webapp.form.PublicationForm;
 
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
 
 public class SearchCellLinesTest extends BaseModelNeededTest {
 
 	public SearchCellLinesTest(String arg0) {
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
         TestSuite suite = new TestSuite(SearchCellLinesTest.class);
         return suite;
     }
     
 	public void testSearchForCellLines() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding a Cell Line
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Cell Lines");
 		assertNotNull("Unable to find link to enter a Cell Line", theLink);		
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Experiment:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("cellLineForm");
 
 		CellLineForm theForm = new CellLineForm();
 		theForm.setOrgan("Heart");
 		theForm.setOrganTissueName("Heart");		
 		theForm.setOrganTissueCode("C22498");
 
 		/* Add parameters found on submit screen but not displayed on search screen  */
 		List theParamsToSkip = new ArrayList();		
 		theParamsToSkip.add("organTissueCode");
 		theParamsToSkip.add("organTissueName");
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 
 		assertCurrentPageContains("You have successfully added a Cell Line to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"CELL LINES");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 		
 		/******* Adding Publication to cell line ***********/
 		navigateToModelForEditing(myModelName);
 
 		// Adding a Publication
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(
 				WebLink.MATCH_TEXT, "Enter Publication for Cell Line");
 		assertNotNull("Unable to find link to enter a Publication for Cell line", theLink);
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
 
 		//TODO: clean up the use of aCellID and ACellID, check ATherapyID and APubID also
 		List theParamsToIgnore = new ArrayList();
 		theParamsToIgnore.add("volume");
 		theParamsToIgnore.add("title");		
 		theParamsToIgnore.add("aCellID");
 		theParamsToIgnore.add("ACellID");		
 		theParamsToIgnore.add("APubID");
 		theParamsToIgnore.add("ATherapyID");		
 
 		//Add parameters found on submit screen but not displayed on search
 		theParamsToSkip = new ArrayList();
 		theParamsToSkip.add("firstTimeReported");
 		theParamsToSkip.add("aCellID");
 		theParamsToSkip.add("ACellID");		
 		theParamsToSkip.add("APubID");
 		theParamsToSkip.add("volume");
 		theParamsToSkip.add("title");
		theParamsToSkip.add("ATherapyID");		
 		
 
 		TestUtil.setRandomValues(thePubForm, theWebForm, false,	theParamsToIgnore);
 		TestUtil.setValuesOnForm(thePubForm, theWebForm);
 
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Publication to this model! ");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		// check if publication is on cell line page
 		navigateToSpecificSearchPage(myModelName, "CELL LINES");
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 		
 		/* check if publication is on publication search page
		* TODO:  This is a bug in the application, the pub only shows up
		* if a general publication exists for the model in addition
		* to the cell line publication
 		navigateToSpecificSearchPage(myModelName, "PUBLICATIONS");
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
		*/
 	}  
 
 }
