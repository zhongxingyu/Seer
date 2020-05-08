 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.novelang.daemon;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.mortbay.jetty.Request;
 import org.novelang.common.Problem;
 import org.novelang.common.Renderable;
 import org.novelang.configuration.ProducerConfiguration;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.produce.DocumentProducer;
 import org.novelang.produce.DocumentRequest;
 import org.novelang.produce.StreamDirector;
 import org.novelang.rendering.HtmlProblemPrinter;
 import org.novelang.produce.AnyRequest;
 import org.novelang.produce.GenericRequest;
 import org.novelang.produce.MalformedRequestException;
 
 /**
  * Serves rendered content.
  *
  * By now, it re-creates a whole document when an error report is requested.
  * This is because it is not possible to change the type of a requested document without
  * an HTTP redirect. 
  * <p>
  * Solutions:
  * <ol>
  * <li> Cache the Problems in the session.
  * <li> Cache all the Parts and Books and whatever.
  * </ol>
  *
  * Solution 1 sounds better as it keeps error display away from complex caching stuff.
  *
  * @author Laurent Caillette
  */
 public class DocumentHandler extends GenericHandler {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( DocumentHandler.class );
 
   private final DocumentProducer documentProducer ;
   private final Charset renderingCharset ;
 
 
   public DocumentHandler( final ProducerConfiguration serverConfiguration ) {
     documentProducer = new DocumentProducer( serverConfiguration ) ;
     renderingCharset = serverConfiguration.getRenderingConfiguration().getDefaultCharset() ;
   }
 
 
   @Override
   protected void doHandle(
       final String target,
       final HttpServletRequest request,
       final HttpServletResponse response,
       final int dispatch
   ) throws IOException, ServletException {
     handle( request, response ) ;
   }
 
   private void handle( final HttpServletRequest request, final HttpServletResponse response )
       throws IOException, ServletException
   {
     LOGGER.info( "Handling request ", request.getRequestURI() ) ;
     
     final String rawRequest = request.getPathInfo() +
         ( StringUtils.isBlank( request.getQueryString() ) ? "" : "?" + request.getQueryString() )
     ;
 
     final AnyRequest someRequest;
     try {
       someRequest = GenericRequest.parse( rawRequest );
     } catch( MalformedRequestException e ) {
       throw new ServletException( e );
     }
 
     if( null == someRequest ) {
       return ;
     } else {
 
       final ServletOutputStream outputStream = response.getOutputStream();
 
       if( someRequest.isRendered() ) {
         final StopWatch stopWatch = new StopWatch() ;
         stopWatch.start() ;
 
         final DocumentRequest documentRequest = ( DocumentRequest ) someRequest ;
 
         final Renderable rendered ;
         try {
           rendered = documentProducer.createRenderable( documentRequest ) ;
         } catch( IOException e ) {
           renderProblems(
               Lists.newArrayList( Problem.createProblem( e ) ),
               someRequest.getOriginalTarget(),
               outputStream
           ) ;
           throw e ;
         }
 
         if( documentRequest.getDisplayProblems() ) {
 
           if( rendered.hasProblem() ) {
             renderProblemsAsRequested( documentRequest, rendered, outputStream ) ;
           } else {
             redirectToOriginalTarget( documentRequest, response ) ;
           }
 
         } else if( rendered.hasProblem() ) {
           LOGGER.warn(
               "Document had following problems: \n  ",
               Joiner.on( "\n  " ).join( rendered.getProblems() )  
           ) ;
           redirectToProblemPage( documentRequest, response ) ;
         } else {
 
           // Correct methods don't seem to work.
           // response.setCharacterEncoding( renderingCharset.name() ) ;
           // response.setContentType( documentRequest.getRenditionMimeType().getMimeName() ) ;
           response.addHeader( "Content-type", 
               documentRequest.getRenditionMimeType().getMimeName() ) ;
           response.addHeader( "Charset", renderingCharset.name() ) ;
 
           response.setStatus( HttpServletResponse.SC_OK ) ;
           try {
             documentProducer.produce(
                 documentRequest,
                 rendered,
                 StreamDirector.forExistingStream( outputStream )
             ) ;
           } catch( Exception e ) {
             throw new ServletException( e ) ;
           }
 //          response.setContentType( documentRequest.getRenditionMimeType().getMimeName() ) ;
 
         }
 
         ( ( Request ) request ).setHandled( true ) ;
         LOGGER.info( "Handled request ", request.getRequestURI(),
             " in ", formatDuration( stopWatch.getTime() ), "." ) ;
       }
 
     }
   }
 
 
   private static String formatDuration( final long milliseconds ) {
     final long seconds = milliseconds / 1000 ;
     return String.format( "%d.%03d", seconds, ( milliseconds % 1000 ) )
         + " second" + ( seconds > 1 ? "s" : "" ) ;
   }
 
   private static void redirectToProblemPage(
       final DocumentRequest documentRequest,
       final HttpServletResponse response
   ) throws IOException {
     final String redirectionTarget =
         documentRequest.getOriginalTarget() + GenericRequest.ERRORPAGE_SUFFIX ;
     response.sendRedirect( redirectionTarget ) ;
     response.setStatus( HttpServletResponse.SC_FOUND ) ;
     LOGGER.info( "Redirected to '", redirectionTarget, "'" );
   }
 
   private static void redirectToOriginalTarget(
       final DocumentRequest documentRequest,
       final HttpServletResponse response
   ) throws IOException {
     final String redirectionTarget = documentRequest.getOriginalTarget() ;
     response.sendRedirect( redirectionTarget ) ;
     response.setStatus( HttpServletResponse.SC_FOUND ) ;
     response.setContentType( documentRequest.getRenditionMimeType().getMimeName() ) ;
     LOGGER.info( "Redirected to '", redirectionTarget, "'" ) ;
 
   }
 
   private void renderProblemsAsRequested(
       final DocumentRequest documentRequest,
       final Renderable rendered,
       final ServletOutputStream outputStream
   ) throws IOException {
     renderProblems( rendered.getProblems(), documentRequest.getOriginalTarget(), outputStream ) ;
   }
 
   private static void renderProblems(
       final Iterable< Problem > problems,
       final String originalTarget,
       final OutputStream outputStream
   ) throws IOException {
     final HtmlProblemPrinter problemPrinter = new HtmlProblemPrinter() ;
     problemPrinter.printProblems(
         outputStream,
         problems,
         originalTarget
     ) ;
     LOGGER.info( "Served error request '", originalTarget, "'" ) ;
 
   }
 
 }
