 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wcs.response;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.activation.DataHandler;
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import net.opengis.wcs.v1_1_1.GetCoverageType;
 
 import org.geoserver.ows.Response;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wcs.response.CoveragesHandler.CoveragesData;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.opengis.coverage.grid.GridCoverage;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.wcs.WcsException;
 import org.vfny.geoserver.wcs.responses.CoverageResponseDelegate;
 import org.vfny.geoserver.wcs.responses.CoverageResponseDelegateFactory;
 
 public class WCSMultipartResponse extends Response {
 
     MimeMultipart multipart;
 
     Data catalog;
 
     public WCSMultipartResponse(Data catalog) {
         super(GridCoverage[].class);
         this.catalog = catalog;
         this.multipart = new MimeMultipart();
     }
 
     @Override
     public String getMimeType(Object value, Operation operation) throws ServiceException {
         return multipart.getContentType();
     }
 
     @Override
    public String[][] getHeaders(Object value, Operation operation) throws ServiceException {
        final GetCoverageType request = (GetCoverageType) operation.getParameters()[0];
        final String identifier = request.getIdentifier().getValue();
        return new String[][] { { "Content-Disposition",
                "attachment;filename=\"" + identifier.replace(':', '_') + ".eml\"" } };
    }

    @Override
     public void write(Object value, OutputStream output, Operation operation) throws IOException,
             ServiceException {
         GridCoverage[] coverages = (GridCoverage[]) value;
 
         // grab the delegate for coverage encoding
         GetCoverageType request = (GetCoverageType) operation.getParameters()[0];
         String outputFormat = request.getOutput().getFormat();
         CoverageResponseDelegate delegate = CoverageResponseDelegateFactory
                 .encoderFor(outputFormat);
         if (delegate == null)
             throw new WcsException("Could not find encoder for output format " + outputFormat);
 
         // grab the coverage info for Coverages document encoding
         final GridCoverage2D coverage = (GridCoverage2D) coverages[0];
         CoverageInfo coverageInfo = catalog.getCoverageInfo(request.getIdentifier().getValue());
 
         // use javamail classes to actually encode the document
         try {
             // coverages xml structure (always set the headers after the data
             // handlers, setting
             // the data handlers kills some of them)
             BodyPart coveragesPart = new MimeBodyPart();
             final CoveragesData coveragesData = new CoveragesData(coverageInfo, request);
             coveragesPart.setDataHandler(new DataHandler(coveragesData, "geoserver/coverages"));
             coveragesPart.setHeader("Content-ID", "<urn:ogc:wcs:1.1:coverages>");
             coveragesPart.setHeader("Content-Type", "text/xml");
             multipart.addBodyPart(coveragesPart);
 
             // the actual coverage
             BodyPart coveragePart = new MimeBodyPart();
             delegate.prepare(outputFormat, coverage);
             coveragePart.setDataHandler(new DataHandler(delegate, "geoserver/coverageDelegate"));
             coveragePart.setHeader("Content-ID", "<theCoverage>");
             coveragePart.setHeader("Content-Type", delegate.getContentType(catalog.getGeoServer()));
             multipart.addBodyPart(coveragePart);
 
             // write out the multipart (we need to use mime message trying to
             // encode directly with multipart or BodyPart does not set properly
             // the encodings and binary files gets ruined
             MimeMessage message = new GeoServerMimeMessage();
             message.setContent(multipart);
             message.writeTo(output);
             output.flush();
         } catch (MessagingException e) {
             throw new WcsException("Error occurred while encoding the mime multipart response", e);
         }
     }
 
     /**
      * A special mime message that does not set any header other than the
      * content type
      * 
      * @author Andrea Aime - TOPP
      */
     private static class GeoServerMimeMessage extends MimeMessage {
         public GeoServerMimeMessage() {
             super((Session) null);
         }
 
         @Override
         protected void updateMessageID() throws MessagingException {
             removeHeader("Message-ID");
            // removeHeader("MIME-Version");
            // removeHeader("Content-Type");
         }
     }
 
 }
