 /**
  * 
  * $Id: BaseModelNeededTest.java,v 1.1 2009-07-13 17:45:12 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.1  2009/07/07 17:46:54  pandyas
  * modified according to the recommended directory layout for BDA
  *
  * Revision 1.7  2008/10/01 23:54:12  schroedn
  * Junit test fixes for caMOD 2.5 - revision 1
  *
  * Revision 1.6  2006/09/20 18:29:52  georgeda
  * Modified PI assignment for dev server
  *
  * Revision 1.5  2006/05/08 14:23:11  georgeda
  * Reformat and clean up warnings
  *
  * Revision 1.4  2006/04/27 15:08:33  pandyas
  * Modified while testing caMod 2.1
  *
  * Revision 1.3  2006/04/17 19:37:32  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.2  2005/12/13 19:17:51  pandyas
  * added JavaDocs
  *
  * 
  */
 
 package unit.web.base;
 
 import gov.nih.nci.camod.util.GUIDGenerator;
 
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebLink;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 /** 
  * This is a base class for any test that needs a model. * 
  */
 public class BaseModelNeededTest extends BaseHttpTest
 {
     protected static String myModelName = null;
 
     public BaseModelNeededTest(String testName)
     {
         super(testName);
     }
 
     protected void createModel() throws Exception
     {
     	System.out.println( "<createModel> Creating a Model" );
     	
         // Assume we are logged in. Click the SUBMIT button
         WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "SUBMIT MODELS");
         assertNotNull("Couldn't find link to submission page", theLink);
         theLink.click();
 
         // We may or may not have to hit the agreement link
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "Add New Model");
         assertNotNull("Couldn't find link to add new model", theLink);
         WebResponse theCurrentPage = theLink.click();
         
         assertCurrentPageContains("Model Characteristics");
                
         // Fill in the values and hit submit; Should fail the first time and the
         // populate will fill in the ethnicity strain values
         WebForm theForm = theCurrentPage.getFormWithName("modelCharacteristicsForm");
 
         myModelName = "_" + GUIDGenerator.getInstance().genNewGuid();
         theForm.setParameter("modelDescriptor", myModelName);
         theForm.setParameter("principalInvestigator", "abrahamd");
         theForm.setParameter("scientificName", "Mus musculus");
         theForm.setParameter("description", "Test Phenotype");
         theForm.setParameter("genotype", "Test Genotype");
         theForm.setParameter("nomenclature", "Test Nomenclature");
         theForm.setParameter("experimentDesign", "Test Experimental Design"); 
         //theForm.setParameter("type", "Male Only");
         theForm.setParameter("breedingNotes", "Test Breeding Notes");
         theForm.setParameter("url", "http://www.google.com"); 
         theForm.setParameter("comments", "Test Comment");         
         theCurrentPage = theForm.submit();
                 
         // Set the ethnicity strain and submit again
         theForm = theCurrentPage.getFormWithName("modelCharacteristicsForm");
         theForm.setParameter("ethinicityStrain", "129");
         theCurrentPage = theForm.submit();
 
         System.out.println( "Current URL: " + theCurrentPage.getURL() );
         assertCurrentPageContains("You have successfully created a new model");        
     }
 
     protected void deleteModel() throws Exception
     {
         // Assume we are logged in. Click the SUBMIT button
         WebLink theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "SUBMIT MODELS");
 
         theLink.click();
 
         String theModelId = findModelIdOnPage("duplicate this record (" + myModelName, "delete this record (" + myModelName);
 
         // We may or may not have to hit the agreement link
         theLink = myWebConversation.getCurrentPage().getFirstMatchingLink(WebLink.MATCH_URL_STRING, "method=delete&" + theModelId);
 
         assertNotNull("Couldn't find link to delete model", theLink);
 
         theLink.click();
 
         assertCurrentPageDoesNotContain(myModelName);
     }
 }
