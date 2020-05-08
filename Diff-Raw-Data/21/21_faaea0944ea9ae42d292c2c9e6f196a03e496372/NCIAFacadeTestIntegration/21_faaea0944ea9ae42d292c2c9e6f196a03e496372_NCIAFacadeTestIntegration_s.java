 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.external.ncia;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import gov.nih.nci.caintegrator.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator.external.ConnectionException;
 import gov.nih.nci.caintegrator.external.InvalidImagingCollectionException;
 import gov.nih.nci.caintegrator.external.ServerConnectionProfile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Tests integration with the NCIA grid service.
  *
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:integration-test-config.xml")
 public class NCIAFacadeTestIntegration {
     private static final Logger LOGGER = Logger.getLogger(NCIAFacadeTestIntegration.class);
     @Autowired
     private NCIAFacade nciaFacade;
     private ServerConnectionProfile connection;
 
     /**
      * Set up the integration test.
      *
      * @throws Exception on expected error
      */
     @Before
     public void setUp() throws Exception {
         connection = new ServerConnectionProfile();
        connection.setUrl("http://imaging.nci.nih.gov/wsrf/services/cagrid/NCIACoreService");
     }
 
     /**
      * Tests retrieval of all NCIA collection names.
      *
      * @throws ConnectionException on connection error
      */
     @Test
     public void testGetAllCollectionNameProjects() throws ConnectionException {
         List<String> allProjects = nciaFacade.getAllCollectionNameProjects(connection);
         if (!allProjects.isEmpty()) {
             LOGGER.info("Retrieve Projects PASSED - " + allProjects.size() + " projects found.");
         } else {
             LOGGER.error("Retrieve Projects FAILED, might be a connection error!");
             fail();
         }
     }
 
     /**
      * Tests retrieval of DICOM files.
      *
      * @throws ConnectionException on a connection error
      * @throws IOException on an IO error
      */
    @Ignore("Disabled until the series instance UID can be determined.")
     @Test
     public void testRetrieveDicomFiles() throws ConnectionException, IOException {
        //String seriesInstanceUID = "2.16.124.113543.6003.2770482660.6726.18091.1680495542";
        String seriesInstanceUID = "1.3.6.1.4.1.9328.50.45.271121120485314117150046084494551250041";
         String studyInstanceUID = "2.16.124.113543.6003.1.857.80828.1120.9007593.726.1";
        NCIADicomJob job = new NCIADicomJob();
         job.getImageSeriesIDs().add(seriesInstanceUID);
         job.getImageStudyIDs().add(studyInstanceUID);
        job.setServerConnection(connection);
        job.setImageAggregationType(NCIAImageAggregationTypeEnum.IMAGESERIES);
         File retrievedZip = nciaFacade.retrieveDicomFiles(job);
         File expectedZip = new File(System.getProperty("java.io.tmpdir") + File.separator + "tmpDownload"
                                      + File.separator + "DICOM_JOB_1" + File.separator + "nciaDicomFiles.zip");
         retrievedZip.deleteOnExit();
         assertEquals(expectedZip.getCanonicalPath(), retrievedZip.getCanonicalPath());
 
         // Now test Image Study.
         job.setImageAggregationType(NCIAImageAggregationTypeEnum.IMAGESTUDY);
         File retrievedZip2 = nciaFacade.retrieveDicomFiles(job);
         File expectedZip2 = new File(System.getProperty("java.io.tmpdir") + File.separator + "tmpDownload"
                                      + File.separator + "DICOM_JOB_2" + File.separator + "nciaDicomFiles.zip");
         retrievedZip2.deleteOnExit();
         assertEquals(expectedZip2.getCanonicalPath(), retrievedZip2.getCanonicalPath());
     }
 
     /**
      * Tests retrieval of image series from NCIA.
      *
      * @throws ConnectionException on a connection error
      * @throws InvalidImagingCollectionException on an unexpected invalid image collection error
      */
     @Test
     public void testGetImageSeriesAcquisition() throws ConnectionException, InvalidImagingCollectionException {
         String collectionNameProject = "NCRI";
         List<ImageSeriesAcquisition> imageSeriesAcquisitions =
                 nciaFacade.getImageSeriesAcquisitions(collectionNameProject, connection);
         if (!imageSeriesAcquisitions.isEmpty()) {
             LOGGER.info("Retrieve ImageSeriesAcquisition PASSED - " + imageSeriesAcquisitions.size() + " were found.");
         } else {
             LOGGER.error("Retrieve ImageSeriesAcquisition FAILED, might be a connection error!");
         }
     }

 }
