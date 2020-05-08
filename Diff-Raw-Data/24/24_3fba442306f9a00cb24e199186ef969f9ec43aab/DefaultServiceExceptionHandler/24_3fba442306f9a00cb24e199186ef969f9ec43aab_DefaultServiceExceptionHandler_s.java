 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows;
 
 import net.opengis.ows.ExceptionReportType;
 import net.opengis.ows.ExceptionType;
 import net.opengis.ows.OwsFactory;
 import org.apache.xml.serialize.OutputFormat;
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.ows.xml.v1_0.OWSConfiguration;
 import org.geoserver.platform.Service;
 import org.geoserver.platform.ServiceException;
 import org.geotools.xml.Encoder;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Collections;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * A default implementation of {@link ServiceExceptionHandler} which outputs
  * as service exception in a <code>ows:ExceptionReport</code> document.
  * <p>
  * This service exception handler will generate an OWS exception report,
  * see {@linkplain http://schemas.opengis.net/ows/1.0.0/owsExceptionReport.xsd}.
  * </p>
  *
  * @author Justin Deoliveira, The Open Planning Project
  *
  */
 public class DefaultServiceExceptionHandler extends ServiceExceptionHandler {
     /**
      * Constructor to be called if the exception is not for a particular service.
      *
      */
     public DefaultServiceExceptionHandler() {
         super(Collections.EMPTY_LIST);
     }
 
     /**
      * Constructor to be called if the exception is for a particular service.
      *
      * @param services List of services this handler handles exceptions for.
      */
     public DefaultServiceExceptionHandler(List services) {
         super(services);
     }
 
     /**
      * Writes out an OWS ExceptionReport document.
      */
     public void handleServiceException(ServiceException exception, Service service,
         HttpServletRequest request, HttpServletResponse response) {
         OwsFactory factory = OwsFactory.eINSTANCE;
 
         ExceptionType e = factory.createExceptionType();
 
         if (exception.getCode() != null) {
             e.setExceptionCode(exception.getCode());
         } else {
             //set a default
             e.setExceptionCode("NoApplicableCode");
         }
 
         e.setLocator(exception.getLocator());
 
         //add the message
         e.getExceptionText().add(exception.getMessage());
         e.getExceptionText().addAll(exception.getExceptionText());
 
        //add the entire stack trace
        //exception.
        ByteArrayOutputStream trace = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(trace));
        e.getExceptionText().add(new String(trace.toByteArray()));
 
         ExceptionReportType report = factory.createExceptionReportType();
         report.setVersion("1.0.0");
         report.getException().add(e);
 
         response.setContentType("application/xml");
 
         //response.setCharacterEncoding( "UTF-8" );
         OWSConfiguration configuration = new OWSConfiguration();
 
         OutputFormat format = new OutputFormat();
         format.setIndenting(true);
         format.setIndent(2);
         format.setLineWidth(60);
 
         Encoder encoder = new Encoder(configuration, configuration.schema());
         encoder.setOutputFormat(format);
 
         encoder.setSchemaLocation(org.geoserver.ows.xml.v1_0.OWS.NAMESPACE,
             RequestUtils.schemaBaseURL(request) + "ows/1.0.0/owsExceptionReport.xsd");
 
         try {
             encoder.encode(report, org.geoserver.ows.xml.v1_0.OWS.EXCEPTIONREPORT,
                 response.getOutputStream());
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         } finally {
             try {
                 response.getOutputStream().flush();
             } catch (IOException ioe) {
             }
         }
     }
 }
