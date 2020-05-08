 package eionet.meta.web.action;
 
 import net.sourceforge.stripes.mock.MockRoundtrip;
 import net.sourceforge.stripes.mock.MockServletContext;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 import eionet.DDDatabaseTestCase;
 import eionet.meta.ActionBeanUtils;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.web.action.VocabularyFolderActionBean;
 
 /**
  * Tests for VocabularyConteptActionBean.
  *
  * @author Kaido Laine
  */
 public class VocabularyFolderActionBeanTest extends DDDatabaseTestCase {
 
     /**
      * test if RDF output contains collection resource for a folder.
      *
      * @throws Exception
      *             if test fails
      */
     @Test
     public void testCsvContainsElements() throws Exception {
 
         MockServletContext ctx = ActionBeanUtils.getServletContext();
         MockRoundtrip trip = new MockRoundtrip(ctx, VocabularyFolderActionBean.class);
         trip.addParameter("vocabularyFolder.folderName", "wise");
         trip.addParameter("vocabularyFolder.identifier", "BWClosed");
         trip.execute("csv");
 
         String output = trip.getOutputString();
 
         String baseUri = Props.getRequiredProperty(PropsIF.DD_URL);
 
         String expectedRelatedInternal = baseUri + "/vocabulary/wise/BWClosed/YP";
         // escapeIRI(contextRoot + elem.getRelatedConceptIdentifier()
 
         Assert.assertTrue("Incorrect size of binded elements",
                 StringUtils.contains(output, "\"skos:relatedMatch\",\"skos:relatedMatch\",\"skos:relatedMatch\""));
         Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                 StringUtils.contains(output, "\"http://url1.com\",\"http://url2.com\",\"\""));
         Assert.assertTrue("Output does not contain correct SKOSRelatedMatch",
                StringUtils.contains(output, "\"http://url3.com\",\"http://url3.com\",\"http://url4.com\""));
 
        Assert.assertTrue("Output does not contain correct geo:lat", StringUtils.contains(output, "\"1\",\"2.2\",\"3\",\"4.5\""));
         Assert.assertTrue("Incorrect size of binded elements",
                 StringUtils.contains(output, "\"geo:lat\",\"geo:lat\",\"geo:lat\",\"geo:lat\""));
 
         Assert.assertTrue("Incorrect related element url", StringUtils.contains(output, expectedRelatedInternal));
 
     }
 
     @Override
     protected String getSeedFilename() {
         return "seed-vocabularycsv.xml";
     }
 
 }
