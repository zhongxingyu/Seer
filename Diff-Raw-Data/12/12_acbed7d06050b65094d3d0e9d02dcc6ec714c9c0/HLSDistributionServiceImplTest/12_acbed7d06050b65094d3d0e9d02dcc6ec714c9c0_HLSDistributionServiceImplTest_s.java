 /**
  *  Copyright 2009, 2010 The Regents of the University of California
  *  Licensed under the Educational Community License, Version 2.0
  *  (the "License"); you may not use this file except in compliance
  *  with the License. You may obtain a copy of the License at
  *
  *  http://www.osedu.org/licenses/ECL-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an "AS IS"
  *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  *  or implied. See the License for the specific language governing
  *  permissions and limitations under the License.
  *
  */
 package org.opencastproject.distribution.hls;
 
 import junit.framework.Assert;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.opencastproject.job.api.Job;
 import org.opencastproject.job.api.JobBarrier;
 import org.opencastproject.mediapackage.DefaultMediaPackageSerializerImpl;
 import org.opencastproject.mediapackage.MediaPackage;
 import org.opencastproject.mediapackage.MediaPackageElement;
 import org.opencastproject.mediapackage.MediaPackageBuilder;
 import org.opencastproject.mediapackage.MediaPackageBuilderFactory;
 import org.opencastproject.mediapackage.MediaPackageElementParser;
 import org.opencastproject.mediapackage.MediaPackageParser;
 import org.opencastproject.security.api.DefaultOrganization;
 import org.opencastproject.security.api.Organization;
 import org.opencastproject.security.api.OrganizationDirectoryService;
 import org.opencastproject.security.api.SecurityService;
 import org.opencastproject.security.api.TrustedHttpClient;
 import org.opencastproject.security.api.User;
 import org.opencastproject.security.api.UserDirectoryService;
 import org.opencastproject.serviceregistry.api.ServiceRegistry;
 import org.opencastproject.serviceregistry.api.ServiceRegistryInMemoryImpl;
 import org.opencastproject.util.MimeType;
 import org.opencastproject.util.PathSupport;
 import org.opencastproject.util.UrlSupport;
 import org.opencastproject.workspace.api.Workspace;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.Map;
 import java.util.HashMap;
 
 public class HLSDistributionServiceImplTest {
 
   private HLSDistributionServiceImpl service = null;
   private MediaPackage mp = null;
   private File distributionRoot = null;
   private ServiceRegistry serviceRegistry = null;
 
   @Before
   public void setUp() throws Exception {
     File mediaPackageRoot = new File(getClass().getResource("/mediapackage.xml").toURI()).getParentFile();
     MediaPackageBuilder builder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();
     builder.setSerializer(new DefaultMediaPackageSerializerImpl(mediaPackageRoot));
     InputStream is = null;
     try {
       is = getClass().getResourceAsStream("/mediapackage.xml");
       mp = builder.loadFromXml(is);
     } finally {
       IOUtils.closeQuietly(is);
     }
 
     distributionRoot = new File(mediaPackageRoot, "static");
     service = new HLSDistributionServiceImpl();
 
     StatusLine statusLine = EasyMock.createNiceMock(StatusLine.class);
     EasyMock.expect(statusLine.getStatusCode()).andReturn(HttpServletResponse.SC_OK).anyTimes();
     EasyMock.replay(statusLine);
 
     HttpResponse response = EasyMock.createNiceMock(HttpResponse.class);
     EasyMock.expect(response.getStatusLine()).andReturn(statusLine).anyTimes();
     EasyMock.replay(response);
 
     TrustedHttpClient httpClient = EasyMock.createNiceMock(TrustedHttpClient.class);
     EasyMock.expect(httpClient.execute((HttpUriRequest) EasyMock.anyObject())).andReturn(response).anyTimes();
     EasyMock.replay(httpClient);
 
     User anonymous = new User("anonymous", DefaultOrganization.DEFAULT_ORGANIZATION_ID,
             new String[] { DefaultOrganization.DEFAULT_ORGANIZATION_ANONYMOUS });
     UserDirectoryService userDirectoryService = EasyMock.createMock(UserDirectoryService.class);
     EasyMock.expect(userDirectoryService.loadUser((String) EasyMock.anyObject())).andReturn(anonymous).anyTimes();
     EasyMock.replay(userDirectoryService);
     service.setUserDirectoryService(userDirectoryService);
 
     Organization organization = new DefaultOrganization();
     OrganizationDirectoryService organizationDirectoryService = EasyMock.createMock(OrganizationDirectoryService.class);
     EasyMock.expect(organizationDirectoryService.getOrganization((String) EasyMock.anyObject()))
             .andReturn(organization).anyTimes();
     EasyMock.replay(organizationDirectoryService);
     service.setOrganizationDirectoryService(organizationDirectoryService);
 
     SecurityService securityService = EasyMock.createNiceMock(SecurityService.class);
     EasyMock.expect(securityService.getUser()).andReturn(anonymous).anyTimes();
     EasyMock.expect(securityService.getOrganization()).andReturn(organization).anyTimes();
     EasyMock.replay(securityService);
     service.setSecurityService(securityService);
 
     serviceRegistry = new ServiceRegistryInMemoryImpl(service, securityService, userDirectoryService,
             organizationDirectoryService);
     service.setServiceRegistry(serviceRegistry);
     service.setTrustedHttpClient(httpClient);
     service.distributionDirectory = distributionRoot;
     service.serviceUrl = UrlSupport.DEFAULT_BASE_URL;
     Workspace workspace = EasyMock.createNiceMock(Workspace.class);
     service.setWorkspace(workspace);
 
     EasyMock.expect(workspace.get((URI) EasyMock.anyObject())).andReturn(new File(mediaPackageRoot, "media.mov"));
     EasyMock.expect(workspace.get((URI) EasyMock.anyObject())).andReturn(new File(mediaPackageRoot, "dublincore.xml"));
     EasyMock.expect(workspace.get((URI) EasyMock.anyObject())).andReturn(new File(mediaPackageRoot, "mpeg7.xml"));
     EasyMock.expect(workspace.get((URI) EasyMock.anyObject())).andReturn(new File(mediaPackageRoot, "attachment.txt"));
     EasyMock.replay(workspace);
   }
 
   @After
   public void tearDown() throws Exception {
//    FileUtils.deleteDirectory(distributionRoot);
     ((ServiceRegistryInMemoryImpl) serviceRegistry).dispose();
   }
 
   @Test
   public void testGetDistributionFile() throws Exception {
     File destFile = service.getDistributionFile(mp, mp.getElementById("track-h264"));
     String expectedPath = PathSupport.concat(new String[] { distributionRoot.getAbsolutePath(),
             mp.getIdentifier().compact(), "track-h264", "media.mov.m3u8" });
 
     Assert.assertEquals(new File(expectedPath), destFile);
   }
 
   @Test
   public void testGetDistributionUri() throws Exception {
     URI distUri = service.getDistributionUri(mp.getIdentifier().compact(), mp.getElementById("track-h264"));
     String expectedUri = UrlSupport.concat(service.serviceUrl, mp.getIdentifier().compact(), "track-h264", "media.mov.m3u8");
 
     Assert.assertEquals(new URI(expectedUri), distUri);
   }
 
   @Test
   public void testAcceptsH264Track() throws Exception {
     MediaPackageElement element = mp.getElementById("track-h264");
     Assert.assertTrue(service.accepts(element));
     element = mp.getElementById("track-avc");
     Assert.assertTrue(service.accepts(element));
   }
 
   @Test
   public void testAcceptsAACTrack() throws Exception {
     MediaPackageElement element = mp.getElementById("track-aac");
     Assert.assertTrue(service.accepts(element));
   }
 
   @Test
   public void testAcceptsNonH264Track() throws Exception {
     MediaPackageElement element = mp.getElementById("track-h263");
     Assert.assertFalse(service.accepts(element));
   }
 
   @Test
   public void testAcceptsNonAACTrack() throws Exception {
     MediaPackageElement element = mp.getElementById("track-pcm");
     Assert.assertFalse(service.accepts(element));
   }
 
   @Test
   public void testAudioDistribution() throws Exception {
     
     // Distribute only some of the elements in the mediapackage
     Job job1 = service.distribute(mp, "track-aac"); // "track-aac" should be distributed
     JobBarrier jobBarrier = new JobBarrier(serviceRegistry, 500, job1);
     jobBarrier.waitForJobs();
 
     File mpDir = new File(distributionRoot, mp.getIdentifier().compact());
     Assert.assertTrue(mpDir.exists());
     File mediaDir = new File(mpDir, "track-aac");
     Assert.assertTrue(mediaDir.exists());
     Assert.assertTrue(new File(mediaDir, "media.aac.m3u8").exists()); // HLS playlist should have been created
     Assert.assertTrue(new File(mediaDir, "media.aac-000.ts").exists()); // HLS segment files should have been created
 
     //Test that the HLS playlist was added as a delivery track
     MediaPackageElement mpe = MediaPackageElementParser.getFromXml(job1.getPayload());
     Assert.assertEquals(MimeType.mimeType("application","x-mpegURL"), mpe.getMimeType());
     Assert.assertEquals(MediaPackageElement.Type.Track, mpe.getElementType());
     Assert.assertEquals(new URI(UrlSupport.concat(service.serviceUrl, mp.getIdentifier().compact(), "track-aac", "media.aac.m3u8")), mpe.getURI());
   }
 
   @Test
   public void testVideoTrackDistribution() throws Exception {
 
     // Distribute only some of the elements in the mediapackage
     Job job1 = service.distribute(mp, "track-h264"); // "track-h264" should be distributed
     JobBarrier jobBarrier = new JobBarrier(serviceRegistry, 500, job1);
     jobBarrier.waitForJobs();
 
     File mpDir = new File(distributionRoot, mp.getIdentifier().compact());
     Assert.assertTrue(mpDir.exists());
     File mediaDir = new File(mpDir, "track-h264");
     Assert.assertTrue(mediaDir.exists());
     Assert.assertTrue(new File(mediaDir, "media.mov.m3u8").exists()); // HLS playlist should have been created
     Assert.assertTrue(new File(mediaDir, "media.mov-000.ts").exists()); // HLS segment files should have been created
 
     //Test that the HLS playlist was added as a delivery track
     MediaPackageElement mpe = MediaPackageElementParser.getFromXml(job1.getPayload());
     Assert.assertEquals(MimeType.mimeType("application","x-mpegURL"), mpe.getMimeType());
     Assert.assertEquals(MediaPackageElement.Type.Track, mpe.getElementType());
     Assert.assertEquals(new URI(UrlSupport.concat(service.serviceUrl, mp.getIdentifier().compact(), "track-h264", "media.mov.m3u8")), mpe.getURI());
   }
 
   public void testTrackRetract() throws Exception {
     int elementCount = mp.getElements().length;
 
     // Distribute the mediapackage and all of its elements
     Job job1 = service.distribute(mp, "track-h264");
     JobBarrier jobBarrier = new JobBarrier(serviceRegistry, 500, job1);
     jobBarrier.waitForJobs();
 
     // Add the new elements to the mediapackage
     MediaPackageElement element = MediaPackageElementParser.getFromXml(job1.getPayload());
     mp.add(element);
     System.out.println(MediaPackageElementParser.getAsXml(element));
 
     File mpDir = new File(distributionRoot, mp.getIdentifier().compact());
     File mediaDir = new File(mpDir, "track-h264");
     Assert.assertTrue(mediaDir.exists());
     Assert.assertTrue(new File(mediaDir, "media.mov.m3u8").exists()); // HLS playlist should have been created
     Assert.assertTrue(new File(mediaDir, "media.mov-000.ts").exists()); // HLS segment files should have been created
 
     // Now retract the mediapackage and ensure that the distributed files have been removed
     Job job2 = service.retract(mp, element.getIdentifier());
     jobBarrier = new JobBarrier(serviceRegistry, 500, job2);
     jobBarrier.waitForJobs();
     
     System.out.println("Element ref: " + element.getReference());
     System.out.println("DistributedDir: " + service.getDistributedFile(mp, element).getAbsolutePath());
 
     // Remove the distributed elements from the mediapackage
     mp.remove(element);
 
     Assert.assertEquals(elementCount, mp.getElements().length);
     Assert.assertNotNull(mp.getElementById("track-h264"));
     System.out.println("ElementDir: " + service.getDistributionFile(mp, mp.getElementById("track-h264")).getAbsolutePath());
     
     Assert.assertFalse(service.getDistributionFile(mp, mp.getElementById("track-h264")).isFile());
     Assert.assertFalse(new File(mediaDir, "media.mov.m3u8").exists()); // HLS playlist should have been retracted
     Assert.assertFalse(new File(mediaDir, "media.mov-000.ts").exists()); // HLS segment files should have been retracted
   }
 
   @Test
   public void testOnlyTrackDistribution() throws Exception {
     // Distribute only track elements in the mediapackage
     Job job = service.distribute(mp, "catalog-1"); // "catalog-1" should NOT be distributed
     JobBarrier jobBarrier = new JobBarrier(serviceRegistry, 500, job);
     jobBarrier.waitForJobs();
 
     File mpDir = new File(distributionRoot, mp.getIdentifier().compact());
     Assert.assertFalse(mpDir.exists());
     File metadataDir = new File(mpDir, "catalog-1");
     Assert.assertFalse(metadataDir.exists());
 
     Assert.assertFalse(new File(metadataDir, "dublincore.xml").exists());
   }
 }
