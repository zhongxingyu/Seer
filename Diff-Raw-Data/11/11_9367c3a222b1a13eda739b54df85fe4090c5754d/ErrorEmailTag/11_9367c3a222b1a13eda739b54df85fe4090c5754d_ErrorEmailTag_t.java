 /*
 $Revision: 1.3 $
 $Date: 2009-01-14 16:01:52 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the disclaimer of Article 3, below. Redistributions in
 binary form must reproduce the above copyright notice, this list of conditions
 and the following disclaimer in the documentation and/or other materials
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall
 appear in the software itself, wherever such third-party acknowledgments
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI",
 Research Triangle Institute, and "RTI" must not be used to endorse or promote
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any
 proprietary programs. This license does not authorize the recipient to use any
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webgenome.webui.taglib;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import org.apache.log4j.Logger;
 import org.rti.webgenome.domain.Principal;
 import org.rti.webgenome.service.session.SessionMode;
 import org.rti.webgenome.util.Email;
 import org.rti.webgenome.util.SystemUtils;
 import org.rti.webgenome.webui.SessionTimeoutException;
 import org.rti.webgenome.webui.util.Attribute;
 import org.rti.webgenome.webui.util.PageContext;
 
 /**
  * JSP Tag to control the sending of an Exception email to configured recipients
  * and also present a suitable display to users.
  *
  * @author djackman
  *
  */
 public class ErrorEmailTag extends TagSupport {
 
     /** Serlialized version ID. */
     private static final long serialVersionUID =
         SystemUtils.getLongApplicationProperty("serial.version.uid");
 
     private static final Logger LOGGER = Logger.getLogger(ErrorEmailTag.class);
 
     private String hideMessage = null ;
     private String exceptionMsg = null ;
 
     public String getHideMessage ( ) { return this.hideMessage ; }
     public boolean hideMessage ( ) {
         return hideMessage != null ;
     }
    public void   setHideMessage ( String hideMessage ) {
         if ( "true".equalsIgnoreCase( hideMessage ) )
                 this.hideMessage = hideMessage;
     }
 
     public String getExceptionMsg ( ) { return this.exceptionMsg ; }
     public void   setExceptionMsg ( String exceptionMsg ) {
         this.exceptionMsg = exceptionMsg ;
     }
 
     /**
      * Start tag
      * @return Action to perform after processing
      * @throws JspException
      */
     public int doStartTag() throws JspException {
 
         Exception ex = (Exception)pageContext.findAttribute(Attribute.EXCEPTION);
 
         if ( ex != null || this.getExceptionMsg() != null ) {
 
             // Get email subject and email recipients
             String subject = SystemUtils.getApplicationProperty( "error.page.email.subject" ) ;
             String to = SystemUtils.getApplicationProperty ( "error.email.distribution.list" ) ;
             // Get email reporting boolean
             String errorEmailReportingEnabled = SystemUtils.getApplicationProperty ( "error.email.reporting.enabled" ) ;
 
            // Only send emails, if email reporting is enabled
             boolean emailed = false;
             if(errorEmailReportingEnabled != null) if(errorEmailReportingEnabled.trim().toLowerCase().equals("true")) {
 
 	            // Collate Message
 	            StringBuffer message = new StringBuffer("The following Exception was caught by webGenome:\n\n" );
 	            if ( this.getExceptionMsg() != null )
 	                message.append( this.getExceptionMsg() + "\n") ;
 	            else
 	                message.append ( ex.getMessage() + "\n" ) ;
 
 	            // Get the Stack Trace, if its available
 	            if ( ex != null && ex.getStackTrace() != null && ex.getStackTrace().length > 0 ) {
 	                StringWriter sw = new StringWriter();
 	                PrintWriter pw = new PrintWriter(sw);
 	                ex.printStackTrace(pw);
 	                message.append( "\n\nStackTrace follows:\n" + sw.toString() ) ;
 	            }
 
 	            message.append ( "\n" ) ;
 
 	            //
 	            // Get additional information which might be helpful
 	            //
 	            message.append ( "\n\nADDITIONAL INFORMATION (Not part of Error Message or StackTrace):\n" ) ;
 
 	            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest() ;
 	            message.append ( getURL (request ) ) ;
 	            message.append ( getHeaderInfo ( request ) ) ;
 	            message.append ( getSystemProperties() ) ;
 	            message.append ( getApplicationProperties() ) ;
 	            message.append ( getOtherSettings( request ) ) ;
 	            message.append ( getMemoryInformation () ) ;
 
 
 	            // Dispatch the Exception Email
 	            Email email = new Email() ;
 	            emailed = email.send ( to, subject, message.toString() ) ;
             }
 
             // Display the results, if no directive to hide this message has
             // been given.
             if ( ! hideMessage() ) {
                 //
                 //    Show the problem
                 //
                 PrintWriter out = new PrintWriter(pageContext.getOut());
 
 
                 if ( emailed ) {
                     out.println( "<p>The error was emailed to the System Administrator who will " +
                                 "investigate the problem.</p>" ) ;
                 }
                 else {
                     out.println( "<p>The error was unable to be sent to the System Administrator. " +
                                 "Please help us to resolve the problem by cutting and pasting " +
                                  "the error message and emailing it to " +
                                  "<a href=\"mailto:" + to + "\">" + to + "</a></p>" ) ;
                 }
                 out.println ( "<p>We apologize for the inconvenience.</p>" ) ;
                 out.flush();
             }
         }
 
         return SKIP_BODY;
     }
 
     //
     //    P R I V A T E    M E T H O D S
     //
 
     /**
      * Collates the calling URL - in its entirety
      */
     private String getURL (HttpServletRequest req) {
         StringBuffer returnValue = new StringBuffer ( "REQUEST URL: " ) ;
         String reqUrl = req.getRequestURL().toString();
         String queryString = req.getQueryString();
         if (queryString != null) {
             reqUrl += "?"+queryString;
         }
         returnValue.append ( reqUrl + "\n\n" ) ;
         return returnValue.toString();
     }
 
     /**
      * Collate a list of the system properties - formatted for error reporting.
      */
     private String getSystemProperties ( ) {
         StringBuffer returnValue = new StringBuffer() ;
 
         returnValue.append( "GENERAL SYSTEM PROPERTIES:\n" ) ;
         // Get all system properties
         Properties props = System.getProperties();
 
         // Enumerate all system properties
         Enumeration propEnum = props.propertyNames();
         for (; propEnum.hasMoreElements(); ) {
             // Get property name
             String propName = (String)propEnum.nextElement();
 
             // Get property value
             String propValue = (String)props.get(propName);
             returnValue.append( "  " + propName + "=" + propValue + "\n" ) ;
         }
         returnValue.append ( "\n\n" ) ;
         return returnValue.toString() ;
     }
 
     /**
      * Collate a list of all the headers in the Http Request.
      * @param request
      * @return
      */
     private String getHeaderInfo ( HttpServletRequest request ) {
         StringBuffer returnValue = new StringBuffer() ;
         returnValue.append( "REQUEST HEADER INFORMATION:\n" ) ;
         Enumeration headers = request.getHeaderNames() ;
         while ( headers.hasMoreElements() ) {
             String headerName = (String) headers.nextElement() ;
             String headerValue = request.getHeader ( headerName ) ;
             returnValue.append ( "  " + headerName + "=" + headerValue + "\n" ) ;
         }
         returnValue.append ( "\n\n" ) ;
         return returnValue.toString() ;
     }
 
     /**
      * Return the application properties for webGenome.
      * @return String
      */
     private String getApplicationProperties ( ) {
         StringBuffer returnValue = new StringBuffer () ;
 
         returnValue.append ( "APPLICATION PROPERTIES (webGenome application properties):\n" ) ;
 
         Properties appProps = SystemUtils.getApplicationProperties() ;
         if ( appProps != null ) {
             Enumeration appEnums = appProps.keys() ;
             while ( appEnums.hasMoreElements() ) {
                 String key = (String) appEnums.nextElement() ;
                 String value = appProps.getProperty( key ) ;
                 returnValue.append( "  " + key + "=" + value + "\n" ) ;
             }
         }
         returnValue.append ( "\n\n" ) ;
 
         return returnValue.toString() ;
     }
 
     /**
      * Report on miscellaneous settings.
      * @param request
      * @return String ~ miscellaneous settings which might be helpful for solving
      * problems reported.
      */
     private String getOtherSettings ( HttpServletRequest request ) {
         StringBuffer returnValue = new StringBuffer ( ) ;
         returnValue.append ( "OTHER SETTINGS:\n" ) ;
         returnValue.append ( "            Session Mode: " ) ;
         try {
             SessionMode mode = PageContext.getSessionMode( request ) ;
             returnValue.append( mode.toString() ) ;
         }
         catch ( SessionTimeoutException ignored ) {
             returnValue.append( "Unavailable/unknown" ) ;
         }
         returnValue.append ( "\n" ) ;
         returnValue.append ( "Principal (current user): " ) ;
         try {
             Principal principal = PageContext.getPrincipal( request ) ;
             returnValue.append ( principal.getEmail() ) ;
             returnValue.append ( " (is Admin user? " + principal.isAdmin() + ")" ) ;
         }
         catch ( SessionTimeoutException ignored ) {
             returnValue.append( "no user logged in" ) ;
         }
         returnValue.append ( "\n\n" ) ;
 
         return returnValue.toString() ;
     }
 
     /**
      * Grab the memory information, pretty useless stuff to report, but you never know
      * it might come in handy for solving some problems reported.
      *
      * @return String ~ collated information about JVM heap memory
      */
     private String getMemoryInformation ( ) {
         StringBuffer returnValue = new StringBuffer ( "MEMORY INFORMATION:\n" ) ;
 
         // Get current size of heap in bytes
         long heapSize = Runtime.getRuntime().totalMemory();
 
         // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
         // Any attempt will result in an OutOfMemoryException.
         long heapMaxSize = Runtime.getRuntime().maxMemory();
 
         // Get amount of free memory within the heap in bytes. This size will increase
         // after garbage collection and decrease as new objects are created.
         long heapFreeSize = Runtime.getRuntime().freeMemory();
 
         float percentUsed = ((float) heapSize) / ((float) heapMaxSize) * (float) 100.00 ;
 
         returnValue.append ( "       Heap Size (bytes): " + heapSize + "\n" ) ;
         returnValue.append ( "   Heap Max Size (bytes): " + heapMaxSize + "\n" ) ;
         returnValue.append ( "Free Heap Memory (bytes): " + heapFreeSize + "\n" ) ;
         returnValue.append ( "             Memory Used: " +  String.format ( "%4.2f", percentUsed ) + " %\n" ) ;
 
         returnValue.append ( "\n" ) ;
 
         return returnValue.toString() ;
     }
 }
