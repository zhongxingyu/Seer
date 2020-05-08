 /*
  * Copyright (C) 2008 Laurent Caillette
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
 
 package novelang.batch;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import novelang.configuration.ConfigurationTools;
 import novelang.configuration.ServerConfiguration;
 import novelang.model.common.Problem;
 import novelang.produce.DocumentProducer;
 import novelang.produce.DocumentRequest;
 import novelang.produce.RequestTools;
 import novelang.rendering.HtmlProblemPrinter;
 
 /**
  * The main class for running in command-line mode.
  *
  * @author Laurent Caillette
  */
 public class Main {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( Main.class ) ;
   private static final String PROBLEMS_FILENAME = "problems.html";
 
   /**
    * Set the {@value #LOG_DIR_SYSTEMPROPERTYNAME} system property and use it
    * in Logback configuration to log in another place than current directory.
    */
   public static final String LOG_DIR_SYSTEMPROPERTYNAME = "novelang.log.dir" ;
 
   public static void main( String[] args ) throws Exception {
 
     LOGGER.debug( "Starting {} with arguments {}",
         ClassUtils.getShortClassName( Main.class ), asString( args ) ) ;
     LOGGER.debug( "System property {}='{}'.",
         LOG_DIR_SYSTEMPROPERTYNAME,
         System.getProperty( LOG_DIR_SYSTEMPROPERTYNAME )
     ) ;
 
     try {
       final BatchParameters parameters = BatchParameters.parse( args ) ;
       final File targetDirectory = parameters.getTargetDirectory() ;
       LOGGER.info( "Successfully parsed: " + parameters ) ;
 
       final ServerConfiguration configuration = ConfigurationTools.buildServerConfiguration() ;
       resetTargetDirectory( targetDirectory ) ;
       final DocumentProducer documentProducer = new DocumentProducer( configuration ) ;
       final List< Problem > allProblems = Lists.newArrayList() ;
 
       for( final String unparsedRequest : parameters.getDocuments() ) {
         Iterables.addAll(
             allProblems,
             processUnparsedRequest(
                 unparsedRequest,
                 targetDirectory,
                 documentProducer
             )
         ) ;
       }
       if( ! allProblems.isEmpty() ) {
         reportProblems( targetDirectory, allProblems ) ;
         System.err.println(
             "There were problems. See " + targetDirectory + "/" + PROBLEMS_FILENAME ) ;
       }
 
     } catch( ParametersException e ) {
       LOGGER.error( "Parameters exception {}, printing help and exiting.", e.getMessage() ) ;
       System.err.println( e.getMessage() ) ;
       System.err.println( BatchParameters.HELP ) ;
       System.exit( -1 ) ;
     } catch( Exception e ) {
       LOGGER.error( "Fatal", e ) ;
       throw e ;
     }
   }
 
   // TODO does ToStringBuilder save from doing this?
   private static String asString( String[] args ) {
     final StringBuffer buffer = new StringBuffer( "[" ) ;
     boolean first = true ;
     for( int i = 0 ; i < args.length ; i++ ) {
       final String arg = args[ i ] ;
       if( first ) {
         first = false ;
       } else {
         buffer.append( "," ) ;
       }
       buffer.append( arg ) ;
     }
     buffer.append( "]" ) ;
     return buffer.toString() ;
   }
 
   private static void reportProblems(
       File targetDirectory,
       Iterable< Problem > allProblems
   ) throws IOException {
     final File problemFile = new File( targetDirectory, PROBLEMS_FILENAME ) ;
     final OutputStream outputStream = new FileOutputStream( problemFile ) ;
     // TODO: use some text-oriented thing to avoid meaningless links inside generated file.
     final HtmlProblemPrinter problemPrinter = new HtmlProblemPrinter() ;
     problemPrinter.printProblems( outputStream, allProblems, "<not available>" ) ;
     outputStream.flush() ;
     outputStream.close() ;
   }
 
   private static Iterable< Problem > processUnparsedRequest(
       String unparsedRequest,
       File targetDirectory,
       DocumentProducer documentProducer
   ) throws IOException {
     final DocumentRequest documentRequest = RequestTools.createDocumentRequest( unparsedRequest ) ;
     final File outputFile = createOutputFile(
         targetDirectory,
         documentRequest
     ) ;
     LOGGER.info( "Generating document file '{}'...", outputFile.getAbsolutePath() ) ;
     final FileOutputStream outputStream = new FileOutputStream( outputFile ) ;
     final Iterable< Problem > problems = documentProducer.produce( documentRequest, outputStream ) ;
     outputStream.flush() ;
     outputStream.close() ;
     return problems ;
   }
 
   private static void resetTargetDirectory( File targetDirectory ) throws IOException {
     if( targetDirectory.exists() ) {
       LOGGER.info( "Deleting '{}'...", targetDirectory.getAbsolutePath() ) ;
     }
     FileUtils.deleteDirectory( targetDirectory ) ;
     FileUtils.forceMkdir( targetDirectory ) ;
     LOGGER.info( "Created '{}'...", targetDirectory.getAbsolutePath() ) ;
   }
 
   public static File createOutputFile( File targetDir, DocumentRequest documentRequest )
       throws IOException
   {
     final String relativeFileName = documentRequest.getDocumentSourceName() +
         "." + documentRequest.getRenditionMimeType().getFileExtension() ;
     LOGGER.debug( "Resolved output file name '{}'", relativeFileName ) ;
     final File outputFile =  new File( targetDir, relativeFileName ) ;
     FileUtils.forceMkdir( outputFile.getParentFile() );
     return outputFile ;
   }
 
 
 
 }
