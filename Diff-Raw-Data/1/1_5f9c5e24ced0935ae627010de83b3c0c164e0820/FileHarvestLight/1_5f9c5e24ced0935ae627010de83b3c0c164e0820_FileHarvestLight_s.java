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
 import dk.dbc.opensearch.common.types.IJob;
 import dk.dbc.opensearch.common.types.IIdentifier;
 import dk.dbc.opensearch.common.os.FileHandler;
 import dk.dbc.opensearch.common.os.StreamHandler;
 import dk.dbc.opensearch.common.os.NoRefFileFilter;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 import dk.dbc.opensearch.components.datadock.DatadockJobsMap;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
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
 public class FileHarvestLight implements IHarvest
 {
     static Logger log = Logger.getLogger( FileHarvestLight.class );
 
     
     private Vector<String> FileVector;
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
         FileVector = FileHandler.getFileList( harvesterDirName , filterArray, false );
         iter = FileVector.iterator();
     }
 
 
     public void shutdown()
     {
     }
 
 
     public List< IJob > getJobs( int maxAmount )
     {
         //Element root = null;
         String fileName;
         String refFileName;
         URI fileURI;
         byte[] referenceData = null;
         InputStream ISrefData = null;
         DocumentBuilderFactory docBuilderFactory;
         DocumentBuilder docBuilder = null;
         Document doc;
 
         docBuilderFactory = DocumentBuilderFactory.newInstance();
         try
         {
             docBuilder = docBuilderFactory.newDocumentBuilder();
         }
         catch( ParserConfigurationException pce )
         {
             log.error( pce.getMessage() );
         }
         doc = docBuilder.newDocument();
 
         List<IJob> list = new ArrayList<IJob>();
         for( int i = 0; i < maxAmount && iter.hasNext() ; i++ )
         {
             fileName = (String)iter.next();
             refFileName = fileName.substring( 0, fileName.lastIndexOf( "." ) ) + ".ref";
             //System.out.println( String.format( "created ref name %s for file %s", refFileName, fileName ) );
             File refFile = FileHandler.getFile( refFileName );
             if ( refFile.exists() )
             {
                 try
                 {
                     ISrefData = FileHandler.readFile( refFileName );
                 }
                 catch( FileNotFoundException fnfe )
                 {
                     log.error( String.format( "File for path: %s couldnt be read", refFileName ) );
                 }
                 try
                 {
                     doc = XMLUtils.getDocument( new InputSource( ISrefData ) );
                 }
                 catch( ParserConfigurationException ex )
                 {
                     log.error( ex.getMessage() );
                 }
                 catch( SAXException ex )
                 {
                     log.error( ex.getMessage() );
                 }
                 catch( IOException ex )
                 {
                     log.error( ex.getMessage() );
                 }
 
                 File theFile = FileHandler.getFile( fileName );
 
                 list.add( (IJob) new Job( new FileIdentifier( theFile.toURI() ), doc ) );
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
      *  @deprecated This function is replaced with {@link #getCargoContainer}.
      */
     @Deprecated
     public byte[] getData( IIdentifier jobId ) throws HarvesterUnknownIdentifierException
     {
         FileIdentifier theJobId = (FileIdentifier)jobId;
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
 
     /**
      *
      */
     public CargoContainer getCargoContainer( IIdentifier jobId ) throws HarvesterUnknownIdentifierException, HarvesterIOException
     {
         DocumentBuilderFactory docBuilderFact;
         DocumentBuilder docBuilder = null;
 
         Document refDoc = null;
         CargoContainer returnCargo = new CargoContainer();
         FileIdentifier theJobId = (FileIdentifier)jobId;
         byte[] data;
         InputStream ISdata;
         InputStream refStream = null;
         
         //getting data
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
         
         
         //retrieve format and submitter from the .ref file
         String submitter = null;
         String format = null;
         String language = null;
         String filePath = theJobId.getURI().getRawPath();
         //create name of ref file form the name of the datafile
         String refFilePath = filePath.substring( 0, filePath.indexOf( "." ) ) + ".ref";
         File refFile = FileHandler.getFile( refFilePath );
         
         //FileInputStream refStream = null;
 
         if( refFile.exists() )
         {
             docBuilderFact = DocumentBuilderFactory.newInstance();
             try
             {
                 docBuilder = docBuilderFact.newDocumentBuilder();
 
             }
             catch( ParserConfigurationException pce )
             {
                 String error =  "Cannot build the documentBuilder";
                 log.error( error, pce );
                 throw new HarvesterIOException( error, pce );
             }
             
             try
             {
                 refDoc = docBuilder.parse( refFile );
             }
             catch( SAXException se )
             {
                 String error = String.format( "could not parse file: %s", refFile.toString() );
                 log.error( error, se );
                 throw new HarvesterIOException( error, se );
             }
             catch( IOException ioe )
             {
                 String error = String.format( "could not parse file: %s", refFile.toString() );
                 log.error( error, ioe );
                 throw new HarvesterIOException( error, ioe );
             }
             
             Element xmlRoot = refDoc.getDocumentElement();
             NodeList elementSet = xmlRoot.getElementsByTagName( "es:info" );
 
             if( elementSet.getLength() == 0 )
             {
                 elementSet = xmlRoot.getElementsByTagName( "info" );
                 if( elementSet.getLength() == 0 )
                 {
                     String error = "Failed to get either Document Element named 'info' or 'es:info' from referencedata";
                     log.error( error );
                     throw new IllegalArgumentException( error );
                 }
             }
 
             Node info = elementSet.item( 0 );
             NamedNodeMap attributes = info.getAttributes();
             format = attributes.getNamedItem( "format" ).getNodeValue();
             submitter = attributes.getNamedItem( "submitter" ).getNodeValue();
         
             try
             {
                 String lang = attributes.getNamedItem( "lang" ).getNodeValue();
                 if ( !lang.isEmpty() || lang == null)
                 {
                     language = lang;
                 }
                 else
                 {
                     language = "DA";
                 }
             }
             catch ( NullPointerException npe )
             {
                 language = "DA";
             }
             
         }
         else
         {
             String error =  String.format( "the file %s no longer has a .ref file, very strange", filePath );
             log.error( error );
             throw new IllegalArgumentException( error );
         }
 
         /**
          * made a comment until we get the "es" namespace defined or 
          rid of it, it causes a NoSuchElementException 
         
         if( refFile.exists() )
         {
             try
             {
                 refStream = (InputStream)FileHandler.readFile( refFilePath );
             }
             catch( FileNotFoundException fnfe )
             {
                 String error = String.format( "Could not open referencedata file %s", refFilePath );
                 log.fatal( error, fnfe );
                 throw new IllegalStateException( error, fnfe );
             }  
         }
         else
         {
             log.error( String.format( "the file %s no longer has a .ref file, very strange", filePath ) );
         }
         //The parsing of the file should be in the true part of 
         // the if case above...
         
         //go through the stream
         XMLInputFactory infac = XMLInputFactory.newInstance();
         //read the stream into the xmlEventReader
         try
         {
             XMLEventReader eventReader = infac.createXMLEventReader( refStream );
             XMLEvent event = null;
             while( eventReader.hasNext() )
             {
                 try
                 {
                     event = (XMLEvent) eventReader.next();
                 }
                 catch( NoSuchElementException nsee )
                 {
                     String error = String.format( "Could not parse incoming data, previously correctly parsed content from stream was: %s", event.toString() );
                     log.error( error, nsee );
                     throw new IllegalStateException( error, nsee );
                 }
                 
                 StartElement startElement;
                 
                 switch( event.getEventType() )
                 {
                 case XMLStreamConstants.START_ELEMENT:
                     startElement = event.asStartElement();
 
                     if( startElement.getName().getLocalPart().equals( "info" ) )
                     {
                         submitter = startElement.getAttributeByName( new QName( "submitter" ) ).getValue();
                         format = startElement.getAttributeByName( new QName( "format" ) ).getValue();
                         try
                         {
                         language = startElement.getAttributeByName( new QName( "lang" ) ).getValue();
                         }
                         catch( NullPointerException npe )
                         {
                             language = "DA";
                         }
                         if( language == null )
                         {
                             language = "DA";
                         }
 
                     }
                     break;
                 default:
                     log.trace( String.format( "didnt use: %s from the ref data", event.toString()));
                     break;
                 }
             }
         }
         catch( XMLStreamException xse )
         {
             String error = "could not create XMLEventReader";
             log.fatal( error, xse );
             throw new IllegalStateException( error, xse );
         }
 
         if( submitter == null || format == null )
         {
             String error = String.format("the reference data for %s is invalid", filePath );
             log.error( error );
             throw new IllegalArgumentException( error );
         }
         */
 
 
         String alias;
         String errMsg = "Could not retrive indexingAlias from map";
         
         //retrieving indexingAlias from DatadockJobsmap
         try
         {
             alias = DatadockJobsMap.getIndexingAlias( submitter, format );
         }
         catch( ConfigurationException ce)
         {
             log.error( errMsg, ce );
             throw new HarvesterIOException( errMsg, ce );
         }
         catch( IOException ioe )
         {
             log.error( errMsg, ioe );
             throw new HarvesterIOException( errMsg, ioe );
         }
         catch( ParserConfigurationException pce )
         {
             log.error( errMsg, pce );
             throw new HarvesterIOException( errMsg, pce );
         }
         catch( SAXException saxe )
         {
             log.error( errMsg, saxe );
             throw new HarvesterIOException( errMsg, saxe );
         }
 
         if( alias == null )
         {
             log.error( String.format( "got null back when asked for alias with values submitter: %s format: %s ", submitter, format ) );
         } 
 
         log.debug( String.format("constructing datadock with values: format = %s submitter = %s alias = %s", format, submitter, alias ) );
         
         try
         {
             returnCargo.add( DataStreamType.OriginalData, format, submitter, language, "text/xml", alias, data );
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
         createAndPlaceDiacFile( dataFile, failureDiagnostic );
         }
         catch( FileNotFoundException fnfe )
         {
             log.error( "method createAndPlaceDiacFile cannot find the file when trying to open an FileOutputStream to it", fnfe );
         }
         catch( IOException ioe )
         {
             log.error( "method createAndPlaceDiacFile has problems either writng to or closing the FileOutputStream to the diac file" );
         }
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
      * the name of this diagnostic file is filename.diac.
      * Its only meant to be called from the setStatusFailure method.
      */
     private void createAndPlaceDiacFile( File dataFile, String diagnostic ) throws FileNotFoundException, IOException
     {
         FileOutputStream fopStream;
         final String diacExtension = ".diac";
         String origFileName = dataFile.getName();
         int dotPos = origFileName.lastIndexOf( "." );
         String strippedFileName = origFileName.substring( 0, dotPos ); // filename without extension, and without the dot!
 
         //create the file
         File diacFile = FileHandler.getFile( new String( failureDir + File.separator + strippedFileName + diacExtension ) );
         byte[] diacData = diagnostic.getBytes();
         
         //fill the diagnostic in to it
         fopStream = new FileOutputStream( diacFile );
         fopStream.write( diacData );
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
