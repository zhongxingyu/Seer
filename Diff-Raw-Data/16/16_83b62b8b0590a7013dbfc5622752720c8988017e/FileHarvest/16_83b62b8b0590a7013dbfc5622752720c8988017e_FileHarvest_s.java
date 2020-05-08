 /**
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   *Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
  
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
  
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file FileHarvest.java
  * \brief The FileHarvest class
  */
 
 
 package dk.dbc.opensearch.components.harvest;
 
 
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IIdentifier;
 import dk.dbc.opensearch.common.types.TaskInfo;
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.config.HarvesterConfig;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.os.StreamHandler;
 import dk.dbc.opensearch.components.datadock.DatadockJobsMap;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Vector;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 
 /**
  * FileHarvest class. Implements the IHarvester interface and acts as a
  * fileharvester for the datadock. It implements the methods start,
  * shutdown and getJobs. It is an eventdriven class.
  *
  * This file harvester assumes some things about path given as an argument:
  *
  * The path has to be a directory with the following structure:
  *
  * polling path
  * | - submitter1
  * | | -format1
  * | | | - job1
  * | | | - job2
  * | | -format2
  * | | | - job3
  * | - submitter2
  * .
  * .
  * .
  *
  * There are no restrictions on the number of submitters, formats or
  * jobs - and the jobs can be files or directorys.
  *
  * The harvester only returns a job after the second consecutive time
  * it has been found and its filesize is unchanged.
  */
 public final class FileHarvest implements IHarvest
 {
     static Logger log = Logger.getLogger( FileHarvest.class );
 
 
     private ArrayList<File> submitters;
     private ArrayList<File> formats;
     private Vector< Pair< String, String > > submittersFormatsVector;
     private String datadockJobsFilePath;
     private String toHarvestFolder;
     private String harvestProgressFolder;
     private String harvestDoneFolder;
     private String harvestFailureFolder;
     private int max;
     private File toHarvestFile;
     private File harvestDoneFile;
     private File harvestProgressFile;
     private File harvestFailureFile;
 
 
     /**
      * Constructs the FileHarvest class, and starts polling the given path for
      * files and subsequent file-changes.
      *
      * @throws IllegalArgumentException if the path given is not a directory.
      * @throws IOException
      * @throws SAXException
      * @throws ParserConfigurationException
      * @throws ConfigurationException
      */
     public FileHarvest() throws IllegalArgumentException, SAXException, IOException, ConfigurationException, FileNotFoundException
     {
         this.submitters = new ArrayList<File>();
         this.formats = new ArrayList<File>();
 
         // Getting path for the jobs file for the building of the submitterformatvector
         datadockJobsFilePath = DatadockConfig.getPath();
 
         setHarvestFolders();
 
         max = HarvesterConfig.getMaxToHarvest();
     }
 
 
     private void setHarvestFolders() throws ConfigurationException, FileNotFoundException
     {
         toHarvestFolder = HarvesterConfig.getFolder();
         toHarvestFile = FileHandler.getFile( toHarvestFolder );
         if ( ! toHarvestFile.exists() )
         {
             String errMsg = String.format( "Harvest folder '%s' does not exist!", toHarvestFile );
             log.error( "FileHarvest: " + errMsg );
             throw new FileNotFoundException( errMsg );
         }
 
         harvestProgressFolder = HarvesterConfig.getProgressFolder();
         harvestProgressFile = FileHandler.getFile( harvestProgressFolder );
         if ( ! harvestProgressFile.exists() )
         {
             String errMsg = String.format( "Harvest folder '%s' does not exist!", harvestProgressFile );
             log.error( "FileHarvest: " + errMsg );
             throw new FileNotFoundException( errMsg );
         }
 
         harvestDoneFolder = HarvesterConfig.getDoneFolder();
         harvestDoneFile = FileHandler.getFile( harvestDoneFolder );
         if ( !harvestDoneFile.exists() )
         {
             String errMsg = String.format( "'Harvest done folder' '%s' does not exist!", harvestDoneFile );
             log.error( "FileHarvest: " + errMsg );
             throw new FileNotFoundException( errMsg );
         }
 
         harvestFailureFolder = HarvesterConfig.getFailureFolder();
         harvestFailureFile = FileHandler.getFile( harvestFailureFolder );
         if ( ! harvestFailureFile.exists() )
         {
             String errMsg = String.format( "Harvest folder '%s' does not exist!", harvestFailureFile );
             log.error( "FileHarvest: " + errMsg );
             throw new FileNotFoundException( errMsg );
         }
     }
 
 
     /**
      * Starts The datadock. It initializes vectors and add found jobs to the application vector.
      */
     @Override
     public void start() //throws ParserConfigurationException, SAXException, IOException
     {
         log.debug( "start() called" );
         initVectors();
         log.debug( "Vectors initialized" );
     }
 
 
     /**
      * Shuts down the fileharvester
      */
     @Override
     public void shutdown()
     {
         log.debug( "shutdown() called" );
     }
 
 
     /**
      * Private method to initialize the local vectors representing the
      * polling directory.
      */
     private void initVectors() // throws ParserConfigurationException, SAXException, IOException
     {
         log.debug( "initvectors() called" );
 
         File datadockJobsFile = FileHandler.getFile( datadockJobsFilePath );
         NodeList jobNodeList = null;
         try
         {
             jobNodeList = XMLUtils.getNodeList( datadockJobsFile, "job" );
         }
         catch( ParserConfigurationException pce )
         {
             log.error( String.format( "Could not get joblist from configurationfile %s", datadockJobsFilePath ) );
         }
         catch( SAXException se )
         {
             log.error( String.format( "Could not read xml in configurationfile %s", datadockJobsFilePath ) );
         }
         catch( IOException ioe )
         {
             log.error( String.format( "Could not open file '%s' for reading", datadockJobsFilePath ) );
         }
 
         submittersFormatsVector = new Vector<Pair<String, String>>();
         if( jobNodeList == null )
         {
             throw new IllegalStateException( String.format( "The job list has not been initialized. I error." ) );
         }
 
         int jobNodeListLength = jobNodeList.getLength();
         for( int i = 0; i < jobNodeListLength; i++ )
         {
             Element pluginElement = (Element) jobNodeList.item( i );
             String formatAtt = pluginElement.getAttribute( "format" );
             String submitterAtt = pluginElement.getAttribute( "submitter" );
             Pair<String, String> submitterFormatPair = new Pair<String, String>( submitterAtt, formatAtt );
             if( !submittersFormatsVector.contains( submitterFormatPair ) )
             {
                 log.debug( String.format( "Adding submitter and format to Vector submitterFormatPair: %s and %s", submitterAtt, formatAtt ) );
                 submittersFormatsVector.add( submitterFormatPair );
             }
             else
             {
                 log.warn( String.format( "The format: %s with submitter; %s was not added to the vector, the jobs file contains redundant/erronous information", formatAtt, submitterAtt ) );
             }
         }
 
         log.debug( "submitterFormatsVector: \n" + submittersFormatsVector.toString() );
         log.debug( "Submitters:" );
 
         for( File submitter : toHarvestFile.listFiles() )
         {
             if ( submitter.isDirectory() )
             {
                 log.debug( String.format( "adding submitter: path='%s'", submitter.getAbsolutePath() ) );
                 submitters.add( submitter );
             }
         }
 
         log.debug( "formats:" );
         for ( File submitterFile : submitters )
         {
             for ( File format : submitterFile.listFiles() )
             {
                 if ( checkSubmitterFormat( submitterFile, format ) )
                 {
                     log.debug( String.format( "format: path='%s'", format.getAbsolutePath() ) );
                     formats.add( format );
                 }
             }
         }
     }
 
 
     private boolean checkSubmitterFormat( File submitterFile, File formatFile )
     {
         String submitterFilePath = submitterFile.getAbsolutePath().substring( submitterFile.getAbsolutePath().lastIndexOf( "/" ) + 1 );
         log.debug( "FileHarvest.checkSubmitterFormat -> submitter: " + submitterFilePath );
         String formatFilePath = formatFile.getAbsolutePath().substring( formatFile.getAbsolutePath().lastIndexOf( "/" ) + 1 );
         log.debug( "FileHarvest.checkSubmitterFormat -> format: " + formatFilePath );
 
         Pair<String, String> pair = new Pair<String, String>( submitterFilePath, formatFilePath );
         boolean contains = submittersFormatsVector.contains( pair );
         log.debug( "FileHarvest.checkSubmitterFormat -> contains: " + contains );
         if( contains )
         {
             return true;
         }
         else
         {
             log.debug( "FileHarvest.checkSubmitterFormat -> Vector: " + submittersFormatsVector.toString() );
             return false;
         }
     }
 
 
     /**
      * getJobs. Creates an array of TaskInfo from the info gained from getNewJobs method. 
      * The max amount of files returned from getNewJobs is the maxAmount argument
      * vector, and generate a new snapshot of the harvest directory.
      * @throws ConfigurationException
      *
      * @return A vector of Datadockjobs containing the necessary information to process the jobs.
      */
     @Override
     public List< TaskInfo > getJobs( int maxAmount ) //throws FileNotFoundException, IOException, ConfigurationException
     {
         max = maxAmount;
         List< TaskInfo > jobs = new ArrayList< TaskInfo >();
         List< File > newJobs = new ArrayList< File >();
         try
         {
             newJobs = getNewJobs();
         }
         catch( FileNotFoundException fnfe )
         {
             log.error( fnfe.toString() );
         }
         catch( IOException ioe )
         {
             log.error( ioe.toString() );
         }
         catch( ConfigurationException ce )
         {
             log.error( ce.toString() );
         }
 
         for( File job : newJobs )
         {
             URI uri = job.toURI();
             String submitter = job.getParentFile().getParentFile().getName();
             String format = job.getParentFile().getName();
             FileIdentifier identifier = new FileIdentifier( uri );
             TaskInfo theJob = buildTheJob( identifier, submitter, format );
             log.debug( String.format( "found new job: path=%s, submitter=%s, format=%s ", theJob.getIdentifier(), submitter, format ) );
             jobs.add( theJob );
         }
 
         return jobs;
     }
 
 
     /**
      * Wrapper to setStatus.
      * Notice that the PID is ignored. 
      */
     public void setStatusSuccess( IIdentifier Id, String PID ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
         // Ignoring the PID!
         setStatus( Id, JobStatus.SUCCESS );
     }
 
 
     /**
      * Wrapper to setStatus.
      * Notice that the failureDiagnostic is ignored. 
      */
     public void setStatusFailure( IIdentifier Id, String failureDiagnostic ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
         // Ignoring the failureDiagnostic!
         setStatus( Id, JobStatus.FAILURE );
     }
 
     
     // There are no current scenarios for using setStatusRetry. If this comment is read after february 28th, delete this comment and the commented method below
     /**
      * Wrapper to setStatus.
      */
     // public void setStatusRetry( IIdentifier Id ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     // {
     //     setStatus( Id, JobStatus.RETRY );
     // }
 
 
     /**
      * Implements the setStatus method, but does nothing but log the file and
      * the status it is set to. So all status are treated the same and wont have any
      * effect on the further execution.
      */
     private void setStatus( IIdentifier jobId, JobStatus status ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
         FileIdentifier fi = (FileIdentifier)jobId;
         URI uri = fi.getURI();
         File file = new File( uri );
         
         log.trace( String.format( "STATUS: '%s'. URI for jobId '%s': %s", status.toString(), jobId, uri ) );
 
         switch ( status )
         {
             case SUCCESS:
                 try
                 {
                     move( file, harvestProgressFolder, harvestDoneFolder );
                 }
                 catch ( IOException ioe )
                 {
                     throw new HarvesterUnknownIdentifierException( "IOException caught", ioe );
                 }
                 break;
             case FAILURE:
                 try
                 {
                     move( file, harvestProgressFolder, harvestFailureFolder );
                 }
                 catch ( IOException ioe )
                 {
                     throw new HarvesterUnknownIdentifierException( "IOException caught", ioe );
                 }
                 break;
             default:
         }
         //log.trace( String.format( "the File %s had its status set to %s", jobId.toString(), status.getDescription() ) );
         log.trace( String.format( "the File %s had its status set to %s", jobId.toString(), status ) );
     }
 
 
     /**
      * Implements the getData method. It returns the requested file as an array of bytes
      *
      *  @deprecated This function is replaced with {@link #getCargoContainer}.
      */
     private byte[] getData( IIdentifier jobId ) throws HarvesterUnknownIdentifierException
     {
         FileIdentifier theJobId = (FileIdentifier) jobId;
         byte[] data;
         InputStream ISdata;
 
         try
         {
             ISdata = FileHandler.readFile( theJobId.getURI().getRawPath() );
         }
         catch( FileNotFoundException fnfe )
         {
             throw new HarvesterUnknownIdentifierException( String.format( "File for path: %s couldnt be read", theJobId.getURI().getRawPath() ) );
         }
         try
         {
             data = StreamHandler.bytesFromInputStream( ISdata, 0 );
         }
         catch( IOException ioe )
         {
             throw new HarvesterUnknownIdentifierException( String.format( "Could not construct byte[] from InputStream for file %s ", theJobId.getURI().getRawPath() ) );
         }
         return data;
     }
 
     public CargoContainer getCargoContainer( IIdentifier jobId ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
         CargoContainer returnCargo =  new CargoContainer();
         byte[] data = getData( jobId );
         FileIdentifier theJobId = (FileIdentifier) jobId;
         //retrieve format and submitter from the URI 
         // "something/submitter/format/file"
         String theRawPath = theJobId.getURI().getRawPath();
         int fileSlash = theRawPath.lastIndexOf( "/");
         String noFileString = theRawPath.substring( 0, fileSlash);
         int formatSlash = noFileString.lastIndexOf( "/" );
         String format = noFileString.substring( formatSlash + 1 );
         String noFormatString = theRawPath.substring( 0, formatSlash );
         int submitterSlash = noFormatString.lastIndexOf( "/" );
         String submitter = noFormatString.substring( submitterSlash + 1 );
         //String errMsg = "Could not retrive indexingAlias from map";
         
         // try
         // {
         // alias = DatadockJobsMap.getIndexingAlias( submitter, format);
         // }
         // catch( ConfigurationException ce)
         // {
         //     log.error( errMsg, ce );
         //     throw new HarvesterIOException( errMsg, ce );
         // }
         // catch( IOException ioe )
         // {
         //     log.error( errMsg, ioe );
         //     throw new HarvesterIOException( errMsg, ioe );
         // }
         // catch( ParserConfigurationException pce )
         // {
         //     log.error( errMsg, pce );
         //     throw new HarvesterIOException( errMsg, pce );
         // }
         // catch( SAXException saxe )
         // {
         //     log.error( errMsg, saxe );
         //     throw new HarvesterIOException( errMsg, saxe );
         // }
 
         // if( alias == null )
         // {
         //     log.error( String.format( "got null back when asked for alias with values submitter: %s format: %s ", submitter, format ) );
         // }
         
  
         log.debug( String.format("constructing datadock with values: format = %s submitter = %s ", format, submitter ) );
         
         try
         {
             returnCargo.add( DataStreamType.OriginalData, format, submitter, "DA", "text/xml", data );
         }
         catch ( IOException ioe )
         {
             String errorMsg = new String( "Could not add OriginalData to CargoContainer" );
             log.fatal( errorMsg, ioe );
             throw new HarvesterIOException( errorMsg, ioe );
         }
         return returnCargo;
     }
 
     /**
      *  This function is not implemented in FileHarvester
      *
     *  @param taskInfo unused.
      */
     @Override
     public void releaseJob( IIdentifier jobId ) throws HarvesterIOException
     {
 	FileIdentifier id = (FileIdentifier)jobId;
 	log.debug( String.format( "(Empty function) Releasing job: %s", id.toString() ) );
     }
 
 
     /**
      * Returns a HashSet of SimplePairs with file objects. The size of
      * the HashSet is determined by the max which is set by the getJobs method
      * A file object is moved from the toHarvestFolder into the harvestProgessFolder 
      *
      * @return
      */
     private ArrayList< File > getNewJobs() throws FileNotFoundException, IOException, ConfigurationException
     {
         log.debug( "Calling FileHarvest.getNewJobs" );
         ArrayList< File > jobs = new ArrayList< File >();
 
         log.debug( "FileHarvest.getNewJobs: ArrayList formats: " + formats.toString() );
         for ( File format : formats )
         {
             if ( format != null )
             {
                 File[] files = format.listFiles();
                 
                 if ( files != null)
                 {
                     int l = files.length;
                     int i = 0;
                     while( i < l && i < max )
                     {
                         File job = files[i];
                         File dest = move( job, toHarvestFolder, harvestProgressFolder );
                         jobs.add( dest );
                         i++;
                     }
                 }
                 else
                 {
                     log.error( String.format( "the File[] created from listFiles on format: %s is null", format.toString() ) );
                 }
             }
             else
             {
                 log.error( "format is null, the formats vector is corrupt" );
             }
         }
 
         log.debug( "FileHarvest.getNewsJobs done harvesting first files max: " + max );
 
         return jobs;
     }
 
 
     private File move( File src, String fromFldr, String toFldr ) throws FileNotFoundException, IOException
     {
         String srcPath = src.getPath();
         String newPath = srcPath.replace( fromFldr, toFldr );
         String destFldrStr = newPath.substring( 0, newPath.lastIndexOf( "/" ) );
         File destFldr = FileHandler.getFile( destFldrStr );
         File dest = FileHandler.getFile( newPath );
 
         log.debug( "Creating new destFldr: " + destFldr.getAbsolutePath().toString() );
         boolean ok = false;
         if ( ! destFldr.exists() )
         {
             ok = destFldr.mkdirs();
         }
         else
         {
             ok = true;
         }
 
         if ( ok )
         {
             log.debug( "destFldr created: " + destFldr.getPath().toString() );
             ok = src.renameTo( dest );
             if( ! ok )
             {
                 log.warn( String.format( "Could not rename file: %s to %s", src.getAbsolutePath().toString(), dest.getAbsolutePath().toString() ) );
                 throw new IOException( "IOException thrown in FileHarvest.move: Could not create new file: " + src.getAbsolutePath().toString() );
             }
         }
         else
         {
             log.warn( "Could not create destination folder for old files: " + destFldr.getAbsolutePath().toString() );
             throw new IOException( "IOException thrown in FileHarvest move: Could not create destination folder for old files:" + destFldr.getAbsolutePath().toString() );
 
         }
         
         log.trace( String.format( "File: %s is moved to: %s", src.getAbsolutePath() , dest.getAbsolutePath() ) );
         return dest;
     }
 
 
     private TaskInfo buildTheJob( FileIdentifier identifier, String submitter, String format )
     {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = null;
         try
         {
             builder = factory.newDocumentBuilder();
         }
         catch( ParserConfigurationException pce )
         {
             log.error( pce.getMessage() );
         }
 
         Document refStream = builder.newDocument();
         Element root = refStream.createElement( "referencedata" );
         Element info = refStream.createElement( "info" );
         info.setAttribute( "submitter", submitter );
         info.setAttribute( "format", format );
         root.appendChild( (Node) info );
         refStream.appendChild( root );
 
         try
         {
             log.trace( String.format( "Job referencedata = %s", XMLUtils.xmlToString( refStream ) ) );
         }
         catch( TransformerException ex )
         {
             log.error( ex.getMessage() );
         }
 
         TaskInfo theJob = new TaskInfo( identifier, refStream );
 
         return theJob;
     }
 }
