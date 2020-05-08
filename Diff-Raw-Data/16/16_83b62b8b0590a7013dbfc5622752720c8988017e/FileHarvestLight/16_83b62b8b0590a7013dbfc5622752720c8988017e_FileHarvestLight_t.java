 /**
  *
  *This file is part of opensearch.
  *Copyright © 2009, Dansk Bibliotekscenter a/s,
  *Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
  *
  *opensearch is free software: you can redistribute it and/or modify
  *it under the terms of the GNU General Public License as published by
  *the Free Software Foundation, either version 3 of the License, or
  *(at your option) any later version.
  *
  *opensearch is distributed in the hope that it will be useful,
  *but WITHOUT ANY WARRANTY; without even the implied warranty of
  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *GNU General Public License for more details.
  *
  *You should have received a copy of the GNU General Public License
  *along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * \file FileHarvestLight.java
  * \brief The FileHarvestLight class
  */
 
 package dk.dbc.opensearch.components.harvest;
 
 import dk.dbc.opensearch.common.config.HarvesterConfig;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IIdentifier;
 import dk.dbc.opensearch.common.types.TaskInfo;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.os.StreamHandler;
 import dk.dbc.opensearch.common.os.NoRefFileFilter;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.NoSuchElementException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 /**
  * this is a class for testuse!
  * It looks in the directory named harvest at the root of execution and reads
  * files from there. It starts by reading all files but the .ref files. The .ref files
  * contains referencedata about the other files so that xyz.ref descripes
  * the file xyz.someformat. Files without an associated .ref file will not be read.
  */
 public final class FileHarvestLight implements IHarvest
 {
     static Logger log = Logger.getLogger( FileHarvestLight.class );
 
     
     private List<String> FileList;
     private Iterator iter;
     private final FilenameFilter[] filterArray;
     // Some default values:
     private final String harvesterDirName;
     private final String successDirName;
     private final String failureDirName;
     //    private final File dataFile;
     private final File successDir;
     private final File failureDir;
 
     /**
      *
      */
     public FileHarvestLight() throws HarvesterIOException
     {
         filterArray = new FilenameFilter[] { new NoRefFileFilter() };
        	
 	String harvesterConfigDir;
 	String successConfigDir;
 	String failureConfigDir;
 	try
 	{
 	    harvesterConfigDir = HarvesterConfig.getFolder();
 	    successConfigDir   = HarvesterConfig.getDoneFolder();
 	    failureConfigDir   = HarvesterConfig.getFailureFolder();
 	}
 	catch ( ConfigurationException ce)
 	{
 	    String errMsg = "Could not handle on of the following in HarvesterConfig: getFolder(), getDoneFolder(), getFailurefolder()";
 	    log.error( errMsg );
 	    throw new HarvesterIOException( errMsg, ce );
 	}
 
 	// Set folders from config, or set default names:
 	harvesterDirName = harvesterConfigDir.isEmpty() ? "Harvest" : harvesterConfigDir;
 	successDirName = successConfigDir.isEmpty() ? "success" : successConfigDir;
 	failureDirName = failureConfigDir.isEmpty() ? "failure" : failureConfigDir;
 
         File dataFile = FileHandler.getFile( harvesterDirName );
         if ( ! dataFile.exists() )
         {
             String errMsg = String.format( "Harvest folder %s does not exist!", dataFile );
             log.fatal( "FileHarvestLight: " + errMsg );
             throw new HarvesterIOException( errMsg );
         }
 
 	// Notice we do not create the Harvest dir, since this is where the 
 	// data/ref-files are supposed to be. No dir => no files => no need to do anything.
 	successDir = createDirectoryIfNotExisting( successDirName );
 	failureDir = createDirectoryIfNotExisting( failureDirName );
     }
 
 
     public void start()
     {
         //get the files in the dir
         FileList = FileHandler.getFileList( harvesterDirName , filterArray, false );
         iter = FileList.iterator();
     }
 
 
     public void shutdown()
     {
     }
 
     private Document createReferenceDataDocument( FileIdentifier id ) 
     {
         Document doc = null;
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
         String file = id.getURI().getRawPath();
 	File refFile = createRefFile( new File( file ) );
 
 	if ( !refFile.exists() )
         {
 	    log.error( String.format( "The reference-file '%s' does not exists", refFile.getName() ) );
 	    return null;
 	}
 
         boolean DocOK = true; // The Doc structure has no problems
         try
         {
             DocumentBuilder builder = factory.newDocumentBuilder();
 	    doc = builder.parse( refFile );
         }
         catch( ParserConfigurationException pce )
         {
             log.error( String.format( "Caught error while trying to instantiate documentbuilder '%s'", pce.getMessage() ) );
             DocOK = false;
         }
         catch( SAXException se )
         {
             log.error( String.format( "Could not parse data: '%s'", se.getMessage() ) );
             DocOK = false;
         }
         catch( IOException ioe )
         {
             log.error( String.format( "Could not read the ref-file: '%s'", ioe.getMessage() ) );
             DocOK = false;
         }
 
         if ( DocOK )
         {
             return doc;
         }
         else
         {
             try
             {
                 setStatusFailure( id, "The referencedata contains malformed XML" );
             }
             catch ( HarvesterUnknownIdentifierException huie )
             {
                 log.error( String.format( "Error when changing JobStatus (unknown identifier) ID: %s Msg: %s", id, huie.getMessage() ), huie );
             }
             catch ( HarvesterInvalidStatusChangeException hisce )
             {
                 log.error( String.format( "Error when changing JobStatus (invalid status) ID: %s Msg: %s ", id, hisce.getMessage() ), hisce );
             }
         }
 
         return doc;
     }
 
 
 
     public List< TaskInfo > getJobs( int maxAmount )
     {
         List< TaskInfo > list = new ArrayList< TaskInfo >();
         for( int i = 0; i < maxAmount && iter.hasNext() ; i++ )
         {
             String fileName = (String)iter.next();
 	    File theFile = FileHandler.getFile( fileName );
 	    FileIdentifier fid = new FileIdentifier( theFile.toURI() );
 	    
 	    Document doc = createReferenceDataDocument( fid );
 	    
 	    if ( doc != null )
 	    {
                 list.add( new TaskInfo( fid, doc ) );
 	    }
 	    else
 	    {
                 log.warn( String.format( "the file: %s has no .ref file", fileName ) );
                 i--;
 	    }
 
         }
         return list;
 
     }
 
 
     /**
      *
      */
     public CargoContainer getCargoContainer( IIdentifier ID ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
 	FileIdentifier jobId = (FileIdentifier)ID;
 
 	log.trace( String.format( "Called with FileIdentifier: %s", jobId.getURI() ) );
 
 	Document doc = createReferenceDataDocument( jobId );
 	TaskInfo job = new TaskInfo( jobId, doc );
 
 
 	// Open and read the data-file:
         InputStream ISdata = null;
         try
         {
             ISdata = FileHandler.readFile( jobId.getURI().getRawPath() );
         }
         catch( FileNotFoundException fnfe )
         {
             throw new HarvesterUnknownIdentifierException( String.format( "File for path: %s couldnt be read", jobId.getURI().getRawPath() ) );
         }
 
 	byte[] data = null;
         try
 	{
 	    data = StreamHandler.bytesFromInputStream( ISdata, 0 );
 	}
         catch( IOException ioe )
 	{
 	    throw new HarvesterUnknownIdentifierException( String.format( "Could not construct byte[] from InputStream for file %s ", jobId.getURI().getRawPath() ) );
 	}
 	
 
 	log.debug( "Create CargoContainer");
 	CargoContainer cargo = new CargoContainer();
         try
         {
             // returnCargo.add( DataStreamType.OriginalData, format, submitter, language, "text/xml", data );
 	    cargo.add( DataStreamType.OriginalData, job.getFormat(), job.getSubmitter(), job.getLanguage(), job.getMimeType(), data );
         }
         catch ( IOException ioe )
         {
             String errorMsg = new String( "Could not add OriginalData to CargoContainer" );
             log.fatal( errorMsg, ioe );
             throw new HarvesterIOException( errorMsg, ioe );
         }
         
         return cargo;
     }
 
 
 
     /**
      * Wrapper to setStatus.
      * Notice that the PID is ignored. 
      */
     public void setStatusSuccess( IIdentifier Id, String PID ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
 	// Ignoring the PID!
 	FileIdentifier id = (FileIdentifier)Id;
 	log.info( String.format("the file %s was handled successfully", id.getURI().getRawPath() ) );
 
 	File dataFile = FileHandler.getFile( id.getURI().getRawPath() );
 
 	setStatus( dataFile, successDir );
 
     }
    
     /**
      * Wrapper to setStatus.
      */
     public void setStatusFailure( IIdentifier Id, String failureDiagnostic ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
 	FileIdentifier id = (FileIdentifier)Id;
 	log.info( String.format("the file %s was handled unsuccessfully", id.getURI().getRawPath() ) );
 	log.info( String.format("FailureDiagnostic: %s", failureDiagnostic ) );
 
 	File dataFile = FileHandler.getFile( id.getURI().getRawPath() );
 
 	setStatus( dataFile, failureDir );
 
         try
         {
 	    createAndPlaceDiagFile( dataFile, failureDiagnostic );
         }
         catch( FileNotFoundException fnfe )
         {
             log.error( "method createAndPlaceDiagFile cannot find the file when trying to open an FileOutputStream to it", fnfe );
         }
         catch( IOException ioe )
         {
             log.error( "method createAndPlaceDiagFile has problems either writng to or closing the FileOutputStream to the diag file" );
         }
     }
 
 
     /**
      *  Releasing a job
      *  Since the jobs stays in the Harvester list when inProgress, this function does
      *  nothing since the job are neither locked or needs to be moved.
      *
     *  @param jobId unused.
      */
     @Override
     public void releaseJob( IIdentifier jobId ) throws HarvesterIOException
     {
 	FileIdentifier id = (FileIdentifier)jobId;
 	log.debug( String.format( "(Empty function) Releasing job: %s", id.toString() ) );
     }
 
     /**
      *  setStatus
      */
     private void setStatus( File dataFile, File destDir ) throws HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException
     {
 	File refFile = createRefFile( dataFile );
 
 	log.trace( String.format( "dataFile absolute path: %s", dataFile.getAbsolutePath() ) );
 	log.trace( String.format( "refFile absolute path : %s", refFile.getAbsolutePath() ) );
 
 	moveFile( refFile, destDir );
 	moveFile( dataFile, destDir );
     }
 
     private void moveFile( File f, File toDir )
     {
 
 	log.trace( String.format( "Called with filename: [%s]", f.getName() ) );
 	log.trace( String.format( "Called with destination directory: [%s]", toDir.getName() ) );
 
 	// Some tests for validity:
 	if ( ! f.exists() )
 	{
 	    log.error( String.format( "The file: [%s] does not exist.", f.getAbsolutePath() ) );
 	    return;
 	}
 	if ( ! f.isFile() ) 
 	{
 	    log.error( String.format( "[%s] is not a file.", f.getAbsolutePath() ) );
 	    return;
 	}
 	if ( ! toDir.exists() )
 	{
 	    log.error( String.format( "The directory: [%s] does not exist.", toDir.getAbsolutePath() ) );
 	    return;
 	}
 	if ( ! toDir.isDirectory() ) 
 	{
 	    log.error( String.format( "[%s] is not a directory.", toDir.getAbsolutePath() ) );
 	    return;
 	}
 	
 
 	boolean res = f.renameTo( new File( toDir, f.getName() ) );
 	if (res) {
 	    log.info( String.format( "File successfully moved: [%s]", f.getName() ) );
 	} else {
 	    log.error( String.format( "Could not move the file: [%s]", f.getName() ) );
 	}
     }
 
     /**
      *  Private function for creating reference filenames from existing (currently xml) filenames.
      *  \note: This function has a problem: It searches for the last index of . (dot), it will
      *  therefore not correctly handle filnames as 'filename.tar.gz'.
      */
     private File createRefFile( File f )
     {
 	final String refExtension = ".ref";
 
 	String origFileName = f.getName();
 	int dotPos = origFileName.lastIndexOf( "." );
 	String strippedFileName = origFileName.substring( 0, dotPos ); // filename without extension, and without the dot!
 
 	return FileHandler.getFile( new String( harvesterDirName + File.separator + strippedFileName + refExtension ) );
     }
 
     /**
      * Private method for creating a file that contains the diagnositcs 
      * of a failed file and placing it in the same dir as the failed file.
      * the name of this diagnostic file is filename.diag.
      * Its only meant to be called from the setStatusFailure method.
      */
     private void createAndPlaceDiagFile( File dataFile, String diagnostic ) throws FileNotFoundException, IOException
     {
         FileOutputStream fopStream;
         final String diagExtension = ".diag";
         String origFileName = dataFile.getName();
         int dotPos = origFileName.lastIndexOf( "." );
         String strippedFileName = origFileName.substring( 0, dotPos ); // filename without extension, and without the dot!
 
         //create the file
         File diagFile = FileHandler.getFile( new String( failureDir + File.separator + strippedFileName + diagExtension ) );
         byte[] diagData = diagnostic.getBytes();
         
         //fill the diagnostic in to it
         fopStream = new FileOutputStream( diagFile );
         fopStream.write( diagData );
         fopStream.close();
     }
 
     /*
      *  \todo: I'm not sure this is the right location for this function
      */
     private File createDirectoryIfNotExisting( String dirName ) throws HarvesterIOException
     {
 	File path = FileHandler.getFile( dirName );
 	if ( !path.exists() )
 	{
 	    log.info( String.format( "Creating directory: %s", dirName ) );
 	    // create path:
 	    if ( !path.mkdir() )
 	    {
 		String errMsg = String.format( "Could not create necessary directory: %s", dirName );
 		log.error( errMsg );
 		throw new HarvesterIOException( errMsg );
 	    }
 	}
 	
 	return path;
     }
 
 
 }
