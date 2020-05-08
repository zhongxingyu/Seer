 /**
  * 
 * $Id: SearchPopulateGeneticDescriptionTest.java,v 1.6 2007-06-11 14:18:08 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.5  2007/06/07 18:33:16  pandyas
  * Modified script - hide mgi testing since the jsp now determines species first and displays mgi, or zfin, or rgd
  *
  * Revision 1.4  2006/10/11 15:47:55  pandyas
  * changes while testing 2.1.1
  *
  * Revision 1.3  2006/04/27 15:08:43  pandyas
  * Modified while testing caMod 2.1
  *
  * Revision 1.2  2006/04/17 19:37:34  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.1  2006/01/09 16:36:42  pandyas
  * Modified to include methods to test if the populate method returns complete and correct data - initial modifications
  *
  * Revision 1.8  2006/01/03 17:36:05  pandyas
  * Added Test for Assoc Expression
  *
  * Revision 1.7  2005/12/29 20:13:20  pandyas
  * Removed disabled="true" for methodOfObservation, disable done in javascript
  *
  * Revision 1.6  2005/12/29 19:44:55  pandyas
  * Fixed Defect# 325:  Spontaneous Mutation does not show up in search under Genetic Description unless other GD is entered and modified unit test.
  *
  * Revision 1.4  2005/12/27 19:04:19  pandyas
  * Added code setparameter for string array for modification type.  Added the rest of the testing for other fields.
  *
  * Revision 1.3  2005/12/22 19:16:43  pandyas
  * Modified test for EngineeredTransgene and added one for other fields.  Many of the files were modified to get the disabled='true' out of the jsp
  *
  * Revision 1.2  2005/12/13 22:01:50  pandyas
  * added JavaDocs
  *
  * 
  */
 
 package web.search;
 
 import gov.nih.nci.camod.webapp.form.AssociatedExpressionForm;
 import gov.nih.nci.camod.webapp.form.EngineeredTransgeneForm;
 import gov.nih.nci.camod.webapp.form.GenomicSegmentForm;
 import gov.nih.nci.camod.webapp.form.InducedMutationForm;
 import gov.nih.nci.camod.webapp.form.SpontaneousMutationForm;
 import gov.nih.nci.camod.webapp.form.TargetedModificationForm;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import web.util.TestUtil;
 import web.base.BaseModelNeededTest;
 
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebResponse;
 
 /** This is a simple example of using HttpUnit to read and understand web pages. * */
 public class SearchPopulateGeneticDescriptionTest extends BaseModelNeededTest {
 
 	public SearchPopulateGeneticDescriptionTest(String testName) {
 		super(testName);
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
 		TestSuite suite = new TestSuite(SearchPopulateGeneticDescriptionTest.class);
 		return suite;
 	}
 
 	public void testPopulateEngineeredTransgeneWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Transgene");
 		assertNotNull("Unable to find link to enter an Engineered Transgene", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Transgene (coding sequence only):");
 		WebForm theWebForm = theCurrentPage.getFormWithName("engineeredTransgeneForm");
 		//had to be set explicitly so it didn't fail validation
 		theWebForm.setParameter("isRandom", "yes");
 
         EngineeredTransgeneForm theForm = new EngineeredTransgeneForm();
         theForm.setName("TESTENGINEEREDGENE");
         //set conditionedBy here and skipped description below
         theForm.setConditionedBy("Not Conditional");		
 		
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
         theParamsToIgnore.add("locationOfIntegration");
         //Not on EngineeredTransgeneForm
         theParamsToIgnore.add("constructSequence");
         theParamsToIgnore.add("mgiId");    
         theParamsToIgnore.add("zfinId");    
         theParamsToIgnore.add("rgdId");            
 		
         // Add parameters found on submit screen but not displayed on search screen  */
         List<String> theParamsToSkip = new ArrayList<String>();     
         theParamsToSkip.add("description");
         theParamsToSkip.add("locationOfIntegration");
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("transgeneId");
         theParamsToSkip.add("title"); 
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Engineered Transgene to this model!");
 
 		// Verify that populate method returns complete and correct data
 		navigateToModelForEditing(myModelName);
 
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"TESTENGINEEREDGENE");
 		assertNotNull("Unable to find link to populate the Engineered Transgene", theLink);		
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("Transgene (coding sequence only):");
 		theWebForm = theCurrentPage.getFormWithName("engineeredTransgeneForm");
 		
 		//Add parameters found behind scene but not on screen
 		theParamsToSkip = new ArrayList<String>();
 		theParamsToSkip.add("transgeneId");
 		theParamsToSkip.add("title");
 		theParamsToSkip.add("descriptionOfConstruct");
 		theParamsToSkip.add("submitAction");
 		theParamsToSkip.add("fileLocation");
 		theParamsToSkip.add("description");	
 		theParamsToSkip.add("locationOfIntegration");
         theParamsToSkip.add("constructSequence");
 		
 		verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 	}		
 	
 	public void testSearchForEngineeredTransgene() throws Exception {
 
 		/* Search for Engineered Transgene  */
 		navigateToModelForEditing(myModelName);
 
 		// Adding Engineered Transgene
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Transgene");
 		assertNotNull("Unable to find link to enter an Engineered Transgene", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Transgene (coding sequence only):");
 		WebForm theWebForm = theCurrentPage.getFormWithName("engineeredTransgeneForm");
 		//had to be set explicitly so it didn't fail validation
         theWebForm.setParameter("isRandom", "yes");
 
         EngineeredTransgeneForm theForm = new EngineeredTransgeneForm();
         //set conditionedBy here and skipped description below
         theForm.setConditionedBy("Not Conditional");        
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
 		
 		// Add parameters found on submit screen but not displayed on search screen  
 		List<String> theParamsToSkip = new ArrayList<String>();		
 		theParamsToSkip.add("description");       
         theParamsToSkip.add("isRandom");
         theParamsToSkip.add("locationOfIntegration");
         // does not show on view page
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
         
         
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Engineered Transgene to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 		
 		
 		/* Adding Associated Expression to Engineered Transgene  		*/
 		navigateToModelForEditing(myModelName);
 
 		// Adding Assoc Expression
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Assoc Expression");
 		assertNotNull("Unable to find link to enter an Assoc Expression", theLink);
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("Expression Level:");
 		theWebForm = theCurrentPage.getFormWithName("associatedExpressionForm");
 
 		AssociatedExpressionForm theAEForm = new AssociatedExpressionForm();
 		theAEForm.setOrgan("Heart");
 		theAEForm.setOrganTissueName("Heart");		
 		theAEForm.setOrganTissueCode("C22498");
 
 		/* Add parameters found on submit screen but not displayed on search screen  */
 		theParamsToSkip = new ArrayList<String>();		
 		theParamsToSkip.add("organTissueCode");
 		theParamsToSkip.add("organTissueName");
 		theParamsToSkip.add("aEngineeredTransgeneID");
 		
 		TestUtil.setRandomValues(theAEForm, theWebForm, false);
 		TestUtil.setValuesOnForm(theAEForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Associated Expression Transgene to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);		
 
 		
 	}
 	
 	public void testSearchForEngineeredTransgeneWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Transgene");
 		assertNotNull("Unable to find link to enter an Engineered Transgene", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Transgene (coding sequence only):");
 		WebForm theWebForm = theCurrentPage.getFormWithName("engineeredTransgeneForm");
         //had to be set explicitly so it didn't fail validation
         theWebForm.setParameter("isRandom", "yes");
 
         EngineeredTransgeneForm theForm = new EngineeredTransgeneForm();
         //set conditionedBy here and skipped description below
         theForm.setConditionedBy("Not Conditional");        
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
 		
         // Add parameters found on submit screen but not displayed on search screen  */
         List<String> theParamsToSkip = new ArrayList<String>();     
         theParamsToSkip.add("description");
         theParamsToSkip.add("locationOfIntegration");
         // Fix if possible so these are tested
         theParamsToSkip.add("transcriptional1_otherSpecies");
         theParamsToSkip.add("transcriptional2_otherSpecies");
         theParamsToSkip.add("transcriptional3_otherSpecies");
         theParamsToSkip.add("spliceSites_otherSpecies");
         theParamsToSkip.add("otherScientificName");
         theParamsToSkip.add("polyASignal_otherSpecies");
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
         
 		TestUtil.setRandomValues(theForm, theWebForm, true, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Engineered Transgene to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}	
 
 	public void testPopulateGenomicSegmentWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Genomic Segment");
 		assertNotNull("Unable to find link to enter a Genomic Segment", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("-if category you are looking for is not listed");
 		WebForm theWebForm = theCurrentPage.getFormWithName("genomicSegmentForm");
         //had to be set explicitly so it didn't fail validation
         theWebForm.setParameter("isRandom", "yes");
 		
         GenomicSegmentForm theForm = new GenomicSegmentForm();
         theForm.setCloneDesignator("TESTCLONEDESIGNATOR");
         theForm.setIsRandom("yes");
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
         theParamsToIgnore.add("mgiId");    
         theParamsToIgnore.add("zfinId");    
         theParamsToIgnore.add("rgdId");         
         
         // Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();     
         theParamsToSkip.add("description");
         theParamsToSkip.add("segmentId");
         theParamsToSkip.add("segmentName");         
         theParamsToSkip.add("locationOfIntegration");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Genomic Segment to this model! ");
 
 		// Verify that populate method returns complete and correct data
 		navigateToModelForEditing(myModelName);
 
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"TESTCLONEDESIGNATOR");
 		assertNotNull("Unable to find link to populate the Genomic Segment", theLink);		
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("-if category you are looking for is not listed");
 		theWebForm = theCurrentPage.getFormWithName("genomicSegmentForm");
 		
         //Add parameters found behind scene but not on screen
         theParamsToSkip = new ArrayList<String>();
         theParamsToSkip.add("transgeneId");
         theParamsToSkip.add("title");
         theParamsToSkip.add("descriptionOfConstruct");
         theParamsToSkip.add("submitAction");
         theParamsToSkip.add("fileLocation");
         theParamsToSkip.add("description"); 
         theParamsToSkip.add("locationOfIntegration");
         //TODO fix comment this line and fix issue with mismatching data
         theParamsToSkip.add("segmentId");
         theParamsToSkip.add("comments");
         theParamsToSkip.add("constructSequence");
 		
 		verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 	}	
 	
 	public void testSearchForGenomicSegment() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Genomic Segment");
 		assertNotNull("Unable to find link to enter an Genomic Segment", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("-if category you are looking for is not listed");
 		WebForm theWebForm = theCurrentPage.getFormWithName("genomicSegmentForm");
         //had to be set explicitly so it didn't fail validation
         theWebForm.setParameter("isRandom", "yes");
 		
         GenomicSegmentForm theForm = new GenomicSegmentForm();
         theForm.setLocationOfIntegration("Random");
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
         
         // Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();     
         theParamsToSkip.add("description");
         theParamsToSkip.add("segmentId");
         //Fix so we test as many of these fields
         theParamsToSkip.add("segmentName");
         theParamsToSkip.add("comments");
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Genomic Segment to this model! ");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}
 
 	public void testSearchForGenomicSegmentWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Genomic Segment");
 		assertNotNull("Unable to find link to enter an Genomic Segment", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("-if category you are looking for is not listed");
 		WebForm theWebForm = theCurrentPage.getFormWithName("genomicSegmentForm");
         //had to be set explicitly so it didn't fail validation
         theWebForm.setParameter("isRandom", "yes");
 		
         GenomicSegmentForm theForm = new GenomicSegmentForm();
         theForm.setIsRandom("yes");
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
         
         // Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();     
         theParamsToSkip.add("description");
         theParamsToSkip.add("segmentId");
         //Fix these so the fields are tested
         theParamsToSkip.add("segmentName");
         theParamsToSkip.add("isRandom");
         theParamsToSkip.add("locationOfIntegration");
         theParamsToSkip.add("comments");
         //model must have an image to test properly
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Genomic Segment to this model! ");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}
 
 	public void testPopulateTargetedMutationWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding Engineered Transgene
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Targeted Modification");
 		assertNotNull("Unable to find link to enter a Targeted Modification", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Targeted Gene/Locus:");
 
 		WebForm theWebForm = theCurrentPage.getFormWithName("targetedModificationForm");
 		
 		//create string array for modification type
 		String[] testArray = theWebForm.getOptionValues("modificationType");
 		//set the other and second option from dropdown 
 		theWebForm.setParameter("modificationType" ,new String[]{testArray[0],testArray[1]});
 		theWebForm.setParameter("otherModificationType", "Testing otherModificationType");
 
 		TargetedModificationForm theForm = new TargetedModificationForm();
 		theForm.setName("TESTTARGETEDMODIFICATION");
 		//set conditionedBy here and skip description below
 		theForm.setConditionedBy("Not Conditional");	
 		
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("transgeneId");
         theParamsToIgnore.add("fileLocation");
         theParamsToIgnore.add("fileServerLocation");
         theParamsToIgnore.add("title");
         theParamsToIgnore.add("descriptionOfConstruct");
         theParamsToIgnore.add("mgiId");    
         theParamsToIgnore.add("zfinId");    
         theParamsToIgnore.add("rgdId");         
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>(); 	
 		theParamsToSkip.add("description");
 		theParamsToSkip.add("modificationId");
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
 		//other value set above so send in false here
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		assertCurrentPageContains("You have successfully added a Targeted Modification to this model!");
 
 		// Verify that populate method returns complete and correct data
 		navigateToModelForEditing(myModelName);
 
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"TESTTARGETED");
 		assertNotNull("Unable to find link to populate the Targeted Modificatio", theLink);		
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("Targeted Gene/Locus:");
 		theWebForm = theCurrentPage.getFormWithName("targetedModificationForm");
 		
 		//Add parameters found behind scene but not on screen
         theParamsToSkip = new ArrayList<String>();
 		theParamsToSkip.add("transgeneId");
 		theParamsToSkip.add("title");
 		theParamsToSkip.add("descriptionOfConstruct");
 		theParamsToSkip.add("submitAction");
 		theParamsToSkip.add("fileLocation");
 		theParamsToSkip.add("description");
 		//TODO:  uncomment and fix data mismatch
 		theParamsToSkip.add("modificationId");
 		theParamsToSkip.add("modificationType");		
 		theParamsToSkip.add("otherModificationType");
         theParamsToSkip.add("constructSequence");
 		
 		verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 	}
 	
 	public void testSearchForTargetedMutationWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding Engineered Transgene
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Targeted Modification");
 		assertNotNull("Unable to find link to enter a Targeted Modification", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Targeted Gene/Locus:");
 
 		WebForm theWebForm = theCurrentPage.getFormWithName("targetedModificationForm");
 		
 		//create string array for modification type
 		String[] testArray = theWebForm.getOptionValues("modificationType");
 		//set the other and second option from dropdown 
 		theWebForm.setParameter("modificationType" ,new String[]{testArray[0],testArray[1]});
 		theWebForm.setParameter("otherModificationType", "Testing otherModificationType");
 
 		TargetedModificationForm theForm = new TargetedModificationForm();
 		//set conditionedBy here and skip description below
 		theForm.setConditionedBy("Not Conditional");	
 		
         List<String> theParamsToIgnore = new ArrayList<String>();
 		theParamsToIgnore.add("transgeneId");
 		theParamsToIgnore.add("fileLocation");
 		theParamsToIgnore.add("fileServerLocation");
 		theParamsToIgnore.add("title");
 		theParamsToIgnore.add("descriptionOfConstruct");
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();		
 		theParamsToSkip.add("description");
 		theParamsToSkip.add("modificationId");
         // Fix as many of these fields to be tested
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("otherModificationType");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
         
 		//other value set above so send in false here
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Targeted Modification to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}	
 	
 	public void testSearchForTargetedMutation() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding Engineered Transgene
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Targeted Modification");
 		assertNotNull("Unable to find link to enter a Targeted Modification", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Targeted Gene/Locus:");
 
 		WebForm theWebForm = theCurrentPage.getFormWithName("targetedModificationForm");
 		
 		//create string array for modification type
 		String[] testArray = theWebForm.getOptionValues("modificationType");
 		//set the second and third option from dropdown (other is first)
 		theWebForm.setParameter("modificationType" ,new String[]{testArray[1],testArray[2]});
 
 		TargetedModificationForm theForm = new TargetedModificationForm();
 		//set conditionedBy here and skip description below
 		theForm.setConditionedBy("Not Conditional");	
 		
         List<String> theParamsToIgnore = new ArrayList<String>();
 		theParamsToIgnore.add("transgeneId");
 		theParamsToIgnore.add("fileLocation");
 		theParamsToIgnore.add("fileServerLocation");
 		theParamsToIgnore.add("title");
 		theParamsToIgnore.add("descriptionOfConstruct");
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();		
 		theParamsToSkip.add("description");
 		theParamsToSkip.add("modificationId");
         // Fix so field is tested
         theParamsToSkip.add("constructSequence");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Targeted Modification to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}
 
 	public void testPopulateInducedMutationWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Induced Mutation");
 		assertNotNull("Unable to find link to enter an Induced Mutation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Inducing Agent Category:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("inducedMutationForm");
 		
 		InducedMutationForm theForm = new InducedMutationForm();
 		theForm.setName("TESTINDUCEDMUTATION");
 		theForm.setObservation("Test Observation");
 		theForm.setMethodOfObservation("Test Method of Observation");
 		// unless set Chemical, CAS# is not displayed on search screen
 		theForm.setType("Radiation");	
         
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("mgiId");    
         theParamsToIgnore.add("zfinId");    
         theParamsToIgnore.add("rgdId");           
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>(); 		
 		theParamsToSkip.add("type");
 		theParamsToSkip.add("casNumber");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
		TestUtil.setRandomValues(theForm, theWebForm, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Induced Mutation to this model!");
 
 		// Verify that populate method returns complete and correct data
 		navigateToModelForEditing(myModelName);
 
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"TESTINDUCEDMUTATION");
 		assertNotNull("Unable to find link to populate the Induced Mutation", theLink);		
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("Inducing Agent Category:");
 		theWebForm = theCurrentPage.getFormWithName("inducedMutationForm");
 		
 		//Add parameters found behind scene but not on screen
 		theParamsToSkip = new ArrayList<String>();
 		theParamsToSkip.add("aInducedMutationID");		
 		theParamsToSkip.add("submitAction");
 		
 		verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 	}		
 
 	public void testSearchForInducedMutation() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Induced Mutation");
 		assertNotNull("Unable to find link to enter an Induced Mutation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Inducing Agent Category:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("inducedMutationForm");
 		
 		InducedMutationForm theForm = new InducedMutationForm();
 		theForm.setObservation("Test Observation");
 		theForm.setMethodOfObservation("Test Method of Observation");
 		// unless set Chemical, CAS# is not displayed on search screen
 		theForm.setType("Radiation");		
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>(); 		
 		theParamsToSkip.add("type");
 		theParamsToSkip.add("casNumber");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Induced Mutation to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}
 	
 	public void testSearchForInducedMutationWithOthers() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Induced Mutation");
 		assertNotNull("Unable to find link to enter an Induced Mutation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Inducing Agent Category:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("inducedMutationForm");
 		
 		InducedMutationForm theForm = new InducedMutationForm();
 		theForm.setObservation("Test Observation");
 		theForm.setMethodOfObservation("Test Method of Observation");
 		// unless set Chemical, CAS# is not displayed on search screen
 		theForm.setType("Radiation");		
 		
 		// Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>(); 		
 		theParamsToSkip.add("type");
 		theParamsToSkip.add("casNumber");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");           
 		
 		TestUtil.setRandomValues(theForm, theWebForm, true);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		//TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added an Induced Mutation to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}	
 
 	public void testPopulateSpontaneousMutation() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Spontaneous Mutation");
 		assertNotNull("Unable to find link to enter a Spontaneous Mutation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Gene Name:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("spontaneousMutationForm");
 		
 		SpontaneousMutationForm theForm = new SpontaneousMutationForm();
 		theForm.setName("TESTSPONTANEOUSMUTATION");
 		
 		TestUtil.setRandomValues(theForm, theWebForm, false);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Spontaneous Mutation to this model!");
 
 		// Verify that populate method returns complete and correct data
 		navigateToModelForEditing(myModelName);
 
 		theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"TESTSPONTANEOUSMUTATION");
 		assertNotNull("Unable to find link to populate the Spontaneous Mutation", theLink);		
 		theCurrentPage = theLink.click();
 		assertCurrentPageContains("Gene Name:");
 		theWebForm = theCurrentPage.getFormWithName("spontaneousMutationForm");
 
         List<String> theParamsToIgnore = new ArrayList<String>();
         theParamsToIgnore.add("mgiId");    
         theParamsToIgnore.add("zfinId");    
         theParamsToIgnore.add("rgdId");          
         
 		//Add parameters found behind scene but not on screen
         List<String> theParamsToSkip = new ArrayList<String>(); 
 		theParamsToSkip.add("aSpontaneousMutationID");
 		theParamsToSkip.add("submitAction");
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
 		verifyValuesOnPopulatePage(theWebForm, theParamsToSkip);
 	}	
 	
 	public void testSearchForSpontaneousMutation() throws Exception {
 
 		navigateToModelForEditing(myModelName);
 
 		// Adding
 		WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT,
 				"Enter Spontaneous Mutation");
 		assertNotNull("Unable to find link to enter a Spontaneous Mutation", theLink);
 		WebResponse theCurrentPage = theLink.click();
 		assertCurrentPageContains("Gene Name:");
 		WebForm theWebForm = theCurrentPage.getFormWithName("spontaneousMutationForm");
 		
 		SpontaneousMutationForm theForm = new SpontaneousMutationForm();
         
         // Add parameters found on submit screen but not displayed on search screen  
         List<String> theParamsToSkip = new ArrayList<String>();         
         theParamsToSkip.add("mgiId");     
         theParamsToSkip.add("zfinId");
         theParamsToSkip.add("rgdId");          
 		
		TestUtil.setRandomValues(theForm, theWebForm, theParamsToIgnore);
 		TestUtil.setValuesOnForm(theForm, theWebForm);
 		
 		theCurrentPage = theWebForm.submit();
 		TestUtil.getTextOnPage(theCurrentPage, "Error: Bad or missing data", "* indicates a required field");
 		
 		assertCurrentPageContains("You have successfully added a Spontaneous Mutation to this model!");
 
 		TestUtil.moveModelToEditedApproved(myModelName);
 
 		navigateToSpecificSearchPage(myModelName,"GENETIC DESCRIPTION");
 		
 		verifyValuesOnPage(theWebForm, theParamsToSkip);
 	}
 	
 }
