 package org.geoserver.wcs;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 
 import org.geoserver.wcs.test.WCSTestSupport;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.data.DataSourceException;
 import org.geotools.gce.geotiff.GeoTiffReader;
 import org.w3c.dom.Document;
 
 import com.mockrunner.mock.web.MockHttpServletResponse;
 import com.sun.org.apache.xpath.internal.XPathAPI;
 
 public class GetCoverageEncodingTest extends WCSTestSupport {
 
     @Override
     protected String getDefaultLogConfiguration() {
         return "/DEFAULT_LOGGING.properties";
     }
 
     public void testKvpBasic() throws Exception {
         String request = "?service=WCS&version=1.1.1&request=GetCoverage" + "&identifier="
                 + layerId(WCSTestSupport.TASMANIA_BM)
                 + "&BoundingBox=-90,-180,90,180,urn:ogc:def:crs:EPSG:4326"
                 + "&GridBaseCRS=urn:ogc:def:crs:EPSG:4326" + "&format=geotiff";
         MockHttpServletResponse response = getAsServletResponse(request);
         // make sure we got a multipart
         assertTrue(response.getContentType().matches("multipart/mixed;\\s*boundary=\".*\""));
 
         // parse the multipart, check there are two parts
         Multipart multipart = getMultipart(response);
         assertEquals(2, multipart.getCount());
 
         // now check the first part is a proper description
         BodyPart coveragesPart = multipart.getBodyPart(0);
         assertEquals("text/xml", coveragesPart.getContentType());
         System.out.println("Coverages part: " + coveragesPart.getContent());
         assertEquals("<urn:ogc:wcs:1.1:coverages>", coveragesPart.getHeader("Content-ID")[0]);
         // read the xml document into a dom
         List<Exception> validationErrors = new ArrayList<Exception>();
         Document dom = dom(coveragesPart.getDataHandler().getInputStream(), validationErrors);
         checkValidationErrors(validationErrors);
         assertEquals(WCSTestSupport.TASMANIA_BM.getLocalPart(), XPathAPI.selectSingleNode(dom,
                 "wcs:Coverages/wcs:Coverage/ows:Title").getTextContent());
 
         // the second part is the actual coverage
         BodyPart coveragePart = multipart.getBodyPart(1);
         assertEquals("image/tiff", coveragePart.getContentType());
         assertEquals("<theCoverage>", coveragePart.getHeader("Content-ID")[0]);
 
         // make sure we can read the coverage back
         InputStream is = coveragePart.getDataHandler().getInputStream();
         readCoverage(is);
     }
 
     private Multipart getMultipart(MockHttpServletResponse response) throws MessagingException,
             IOException {
         String content = response.getOutputStreamContent();
         MimeMessage body = new MimeMessage((Session) null, new ByteArrayInputStream(content.getBytes()));
         Multipart multipart = (Multipart) body.getContent();
         return multipart;
     }
 
     private GridCoverage2D readCoverage(InputStream is) throws IOException, FileNotFoundException,
             DataSourceException {
         // for some funny reason reading directly from the input stream does not
         // work,
         // we have to create a temp file instead
         File f = File.createTempFile("kvpBasic", ".tiff", dataDirectory.getDataDirectoryRoot());
         FileOutputStream fos = new FileOutputStream(f);
         byte[] buffer = new byte[4096];
         int read = 0;
         while ((read = is.read(buffer)) > 0)
             fos.write(buffer, 0, read);
         fos.close();
         GeoTiffReader reader = new GeoTiffReader(f);
         GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
         reader.dispose();
         return coverage;
     }
 
     public void testGeotiffNamesGalore() throws Exception {
         String requestBase = "?service=WCS&version=1.1.1&request=GetCoverage" + "&identifier="
                 + layerId(WCSTestSupport.TASMANIA_BM)
                 + "&BoundingBox=-90,-180,90,180,urn:ogc:def:crs:EPSG:4326"
                 + "&GridBaseCRS=urn:ogc:def:crs:EPSG:4326";
         ensureTiffFormat(getAsServletResponse(requestBase + "&format=geotiff"));
         ensureTiffFormat(getAsServletResponse(requestBase + "&format=geotiff"));
         ensureTiffFormat(getAsServletResponse(requestBase + "&format=image/geotiff"));
         ensureTiffFormat(getAsServletResponse(requestBase + "&format=GEotIFF"));
         ensureTiffFormat(getAsServletResponse(requestBase
                 + "&format=image/tiff;subtype%3D\"geotiff\""));
     }
 
     private void ensureTiffFormat(MockHttpServletResponse response) throws MessagingException,
             IOException {
         // make sure we got a multipart
         assertTrue("Content type not mulipart but " + response.getContentType(), response
                 .getContentType().matches("multipart/mixed;\\s*boundary=\".*\""));
 
         // parse the multipart, check the second part is a geotiff
         Multipart multipart = getMultipart(response);
         BodyPart coveragePart = multipart.getBodyPart(1);
         assertEquals("image/tiff", coveragePart.getContentType());
     }
 }
