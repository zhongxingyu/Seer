 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.openxml;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.exceptions.*;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 import net.sf.okapi.common.skeleton.ZipSkeleton;
 import net.sf.okapi.filters.markupfilter.Parameters;
 
 /**
  * <p>Filters Microsoft Office Word, Excel, and Powerpoint Documents.
  * OpenXML is the format of these documents.
  * 
  * <p>Since OpenXML files are Zip files that contain XML documents,
  * this filter handles opening and processing the zip file, and
  * instantiates <b>OpenXMLContentFilter</b> to process the XML documents.
  * 
  * <p>A call to createFilterWriter returns OpenXMLZipFilterWriter, which is
  * the associated writer for this filter.  OpenXMLZipFilterWriter instantiates
  * OpenXMLContentSkeletonWriter. 
  */
 
 public class OpenXMLFilter implements IFilter {
 	
 	private Logger LOGGER = Logger.getLogger(OpenXMLFilter.class.getName());
 
 	private enum NextAction {
 		OPENZIP, NEXTINZIP, NEXTINSUBDOC, DONE
 	}
 
 	public final static int MSWORD=1;
 	public final static int MSEXCEL=2;
 	public final static int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
 	public final static int MSEXCELCOMMENT=5; // DWH 5-13-09
 	public final static int MSWORDDOCPROPERTIES=6; // DWH 5-25-09
 	private final String MIMETYPE = "text/xml";
 	private final String docId = "sd";
 	
 	private ZipFile zipFile;
 	private ZipEntry entry;
 	private NextAction nextAction;
 	private URI docURI;
 	private Enumeration<? extends ZipEntry> entries;
 	private int subDocId;
 	private LinkedList<Event> queue;
 	private String srcLang;
 	private OpenXMLContentFilter openXMLContentFilter;
 	private IParameters params=null; // DWH 6-15-09 was net.sf.okapi.filters.markupfilter.Parameters
 	private ConditionalParameters cparams=null; // DWH 6-16-09
 	private int nZipType=MSWORD;
 	private int nFileType=MSWORD; // DWH 4-16-09
 	private Level nLogLevel=Level.FINE;
 	private boolean bSquishable=true;
 	private ITranslator translator=null;
 	private String sOutputLanguage="en-US";
 	private boolean canceled = false;
 	private boolean bPreferenceTranslateDocProperties = true;
 	private boolean bPreferenceTranslateComments = true;
 	private boolean bPreferenceTranslatePowerpointNotes = true; // DWH 5-26-09 preferences
 	private boolean bPreferenceTranslatePowerpointMasters = true; // DWH 5-26-09 preferences
 	private boolean bPreferenceTranslateWordHeadersFooters = true; // DWH 5-26-09 preferences
 	private boolean bPreferenceTranslateWordAllStyles = true; // DWH 5-28-09 if false, exclude a given list
 	private boolean bPreferenceTranslateWordHidden = true; // DWH 5-28-09
 	private boolean bMinedHiddenStyles = true; // DWH 5-28-09
 	private boolean bPreferenceTranslateExcelExcludeColors = false;
 	  // DWH 6-12-09 don't translate text in Excel in some colors 
 	private boolean bPreferenceTranslateExcelExcludeColumns = false;
 	  // DWH 6-12-09 don't translate text in Excel in some specified columns
 	private TreeSet tsExcludeWordStyles = null; // DWH 5-28-09 set of styles to exclude from translation
 	private TreeSet<String> tsExcelExcludedColors; // DWH 6-12-09
 	private TreeSet<String> tsExcelExcludedStyles=null; // DWH 6-12-09 
 	private TreeSet<String> tsExcelExcludedColumns; // DWH 6-12-09 
 	private int nExcelOriginalSharedStringCount; // DWH 6-12-09
 	private boolean bProcessedExcelSheets=true; // DWH 6-13-09 Excel options
 	private String sCurrentExcelSheet=""; // DWH 6-25-09 current sheet number
 
 	public OpenXMLFilter () {
 		cparams = new ConditionalParameters(); // DWH 6-16-09
 		params = cparams; // DWH 6-16-09 conditional params can be set by user interface
 		readParams();
 	}
 	
 	/**
 	 * Creating the class with these two parameters allows automatic
 	 * manipulation of text within TextUnits.  A copy of a source
 	 * TextFragment is the parameter to the translator, and it
 	 * can change the text.  The new text fragment is added to the
 	 * TextUnit in the specified output language.
 	 * @param translator the class that translates the text of a text fragment
 	 * @param sOutputLanguage the locale of the output language, in the form en-US
 	 */
 	public OpenXMLFilter(ITranslator translator, String sOutputLanguage) {
 		this.translator = translator;
 		this.sOutputLanguage = sOutputLanguage;
 		cparams = new ConditionalParameters(); // DWH 6-16-09
 		params = cparams; // DWH 6-16-09 conditional params can be set by user interface
 		readParams();
 	}
 
 	private void readParams()
 	{
 		try
 		{
 			ConditionalParameters ooparams=(ConditionalParameters) params;
 			bPreferenceTranslateDocProperties = ooparams.bPreferenceTranslateDocProperties;
 			bPreferenceTranslateComments = ooparams.bPreferenceTranslateComments;
 			bPreferenceTranslatePowerpointNotes = ooparams.bPreferenceTranslatePowerpointNotes;
 			bPreferenceTranslatePowerpointMasters = ooparams.bPreferenceTranslatePowerpointMasters;
 			bPreferenceTranslateWordHeadersFooters = ooparams.bPreferenceTranslateWordHeadersFooters;
 			bPreferenceTranslateWordAllStyles = ooparams.bPreferenceTranslateWordAllStyles;
 			bPreferenceTranslateWordHidden = ooparams.bPreferenceTranslateWordHidden;
 			bPreferenceTranslateExcelExcludeColors = ooparams.bPreferenceTranslateExcelExcludeColors;
 			bPreferenceTranslateExcelExcludeColumns = ooparams.bPreferenceTranslateExcelExcludeColumns;
 			tsExcelExcludedColors = ooparams.tsExcelExcludedColors;
 			tsExcelExcludedColumns = ooparams.tsExcelExcludedColumns;
 			tsExcludeWordStyles = ooparams.tsExcludeWordStyles;
 			nFileType = ooparams.nFileType; // DWH 6-27-09
 			cparams = ooparams; // DWH 6-27-09
 		}
 		catch(Exception e) {};
 	}
 	
 	/**
 	 * Closes the input zip file and completes the filter.
 	 */
 	public void close () {
 		try {
 			nextAction = NextAction.DONE;
 			if ( zipFile != null ) {
 				zipFile.close();
 				zipFile = null;
 			}
 		}
 		catch (IOException e) {
 			LOGGER.log(Level.SEVERE,"Error closing zipped output file.");
 			throw new OkapiIOException("Error closing zipped output file.");
 		}
 	}
 
 	/**
 	 * Sets the java.util.logging.Logger log level.
 	 * @param nLogLevel a java.util.logging.Level constant
 	 * Level.SEVERE Errors the end user should see. 
 	 * Level.WARNING Important alert messages the end user should see. 
 	 * Level.INFO Additional log information, progress, etc. These messages are also shown to the user. 
 	 * Level.FINE Extra, less important information. The end user may choose to see them. 
 	 * Level.FINER Debug information. For developers. 
 	 * Level.FINEST Debug information. For developers. 
 	 */
 	public void setNLogLevel(Level nLogLevel) // set debug level
 	{
 		this.nLogLevel = nLogLevel;
 	}
 	
 	/**
 	 * Creates the skeleton writer for use with this filter.
 	 * Null return means implies GenericSkeletonWriter. 
 	 * @return the skeleton writer
 	 */
 	public ISkeletonWriter createSkeletonWriter () {
 		return null; // There is no corresponding skeleton writer
 	}
 	
 	/**
 	 * Creates the filter writer for use with this filter.
 	 * @return the filter writer
 	 */
 	public IFilterWriter createFilterWriter () {
 		return new OpenXMLZipFilterWriter();
 	}
 
 	/**
 	 * Returns a name for this filter to be used in a user interface.
 	 * @return the filter name
 	 */
 	public String getName () {
 		return "okf_openxml";
 	}
 
 	/**
 	 * Returns the current mimetype
 	 * @return the current mimetype
 	 */
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MIMETYPE,
 			getClass().getName(),
 			"Microsoft Office Document",
 			"Microsoft Office documents (DOCX, XLSX, PPTX)."));
 		return list;
 	}
 
 	/**
 	 * Returns the current IParameters object.
 	 * @return the current IParameters object
 	 */
 	public IParameters getParameters () {
 		return params;
 	}
 
 	/**
 	 * Returns true if the filter has a next event.
 	 * @return whether or not the filter has a next event
 	 */
 	public boolean hasNext () {
 		return ((( queue != null ) && ( !queue.isEmpty() )) || ( nextAction != NextAction.DONE ));
 	}
 
 	/**
 	 * Returns the next zip filter event.
 	 * @return the next zip filter event
 	 */
 	public Event next () {
 		// Send remaining event from the queue first
 		if ( queue.size() > 0 ) {
 			return queue.poll();
 		}
 		
 		// When the queue is empty: process next action
 		switch ( nextAction ) {
 		case OPENZIP:
 			return openZipFile();
 		case NEXTINZIP:
 			return nextInZipFile();
 		case NEXTINSUBDOC:
 			return nextInSubDocument();
 		default:
 			throw new RuntimeException("Invalid next() call.");
 		}
 	}
 
 	/**
 	 * Opens a RawDocument for filtering, defaulting to generating the skeleton
 	 * @param input a Raw Document to open and filter
 	 */
 	public void open (RawDocument input) {
 		if (input==null)
 			throw new RuntimeException("RawDocument is null");
 		open(input, true);
 	}
 	
 	/**
 	 * Opens a RawDocument for filtering
 	 * @param input a Raw Document to open and filter
 	 * @param generateSkeleton true if a skeleton should be generated
 	 */
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		if (input==null)
 			throw new RuntimeException("RawDocument is null");
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 			LOGGER.log(Level.FINER,"\nOpening "+input.getInputURI().toString());
 		}
 		else if ( input.getStream() != null ) {
 			open(input.getStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 	
 	/**
 	 * Opens a RawDocument for filtering
 	 * @param input a Raw Document to open and filter
 	 * @param generateSkeleton true if a skeleton should be generated
 	 * @param bSquishable true if file should be optimized by combining compatible
 	 *        text runs
 	 */
 	public void open (RawDocument input,
 			boolean generateSkeleton, boolean bSquishable)
 	{
 		if (input==null)
 			throw new RuntimeException("RawDocument is null");
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI(),bSquishable);
 			LOGGER.log(Level.FINER,"\nOpening "+input.getInputURI().toString());
 		}
 		else if ( input.getStream() != null ) {
 			open(input.getStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 
 	/**
 	 * Opens a RawDocument for filtering
 	 * @param input a Raw Document to open and filter
 	 * @param generateSkeleton true if a skeleton should be generated
 	 * @param bSquishable true if file should be optimized by combining compatible
 	 *        text runs
 	 * @param nLogLevel a java.util.logging.Level constant
 	 * Level.SEVERE Errors the end user should see. 
 	 * Level.WARNING Important alert messages the end user should see. 
 	 * Level.INFO Additional log information, progress, etc. These messages are also shown to the user. 
 	 * Level.FINE Extra, less important information. The end user may choose to see them. 
 	 * Level.FINER Debug information. For developers. 
 	 * Level.FINEST Debug information. For developers. 
 	 */
 	public void open (RawDocument input,
 			boolean generateSkeleton, boolean bSquishable, Level nLogLevel)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI(),bSquishable,nLogLevel);
 			LOGGER.log(Level.FINER,"\nOpening "+input.getInputURI().toString());
 		}
 		else if ( input.getStream() != null ) {
 			open(input.getStream());
 			LOGGER.log(Level.FINER,"\nOpening ");
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 
 	/**
 	 * Opens an input stream for filtering
 	 * @param input an input stream to open and filter
 	 */
 	public void open (InputStream input) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException(
 			"Method is not supported for this filter.");
 	}
 
 	/**
 	 * Opens a character sequence for filtering
 	 * @param a character sequence to open and filter
 	 */
 	private void open (CharSequence inputText) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException(
 			"Method is not supported for this filter.");
 	}
 
 	/**
 	 * Opens a URI for filtering
 	 * @param a cURI to open and filter
 	 */
 	private void open (URI inputURI) {
 		open(inputURI,true,Level.FINE); // DWH 2-26-09 just a default
 	}
 	
 	/**
 	 * Opens a URI for filtering
 	 * @param a cURI to open and filter
 	 * @param bSquishable true if file should be optimized by combining compatible
 	 *        text runs
 	 */
 	public void open (URI inputURI, boolean bSquishable) {
 		open(inputURI, bSquishable, Level.FINE);
 	}
 	
 	/**
 	 * Opens a URI for filtering
 	 * @param a cURI to open and filter
 	 * @param bSquishable true if file should be optimized by combining compatible
 	 *        text runs
 	 * @param nLogLevel a java.util.logging.Level constant
 	 * Level.SEVERE Errors the end user should see. 
 	 * Level.WARNING Important alert messages the end user should see. 
 	 * Level.INFO Additional log information, progress, etc. These messages are also shown to the user. 
 	 * Level.FINE Extra, less important information. The end user may choose to see them. 
 	 * Level.FINER Debug information. For developers. 
 	 * Level.FINEST Debug information. For developers. 
 	 */
 	public void open (URI inputURI, boolean bSquishable, Level nLogLevel) {
 		close();
 		docURI = inputURI;
 		nextAction = NextAction.OPENZIP;
 		queue = new LinkedList<Event>();
 		openXMLContentFilter = new OpenXMLContentFilter();
 		this.nLogLevel = nLogLevel;
 		openXMLContentFilter.setLogger(LOGGER);
 		this.bSquishable = bSquishable;
 		LOGGER.log(Level.FINE,"\nOpening "+inputURI.toString());
 	}
 
 	/**
 	 * Sets language, encoding, and generation options for the filter.
 	 * @param sourceLanguage source language in en-US format
 	 * @param defaultEncoding encoding, such as "UTF-8"
 	 * @param generateSkeleton true if skeleton should be generated
 	 */
 	public void setOptions (String sourceLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		setOptions(sourceLanguage, null, defaultEncoding, generateSkeleton);
 	}
 
 	/**
 	 * Sets language, encoding, and generation options for the filter.
 	 * @param sourceLanguage source language in en-US format
 	 * @param targetLanguage target language in de-DE format
 	 * @param defaultEncoding encoding, such as "UTF-8"
 	 * @param generateSkeleton true if skeleton should be generated
 	 */
 	public void setOptions (String sourceLanguage,
 		String targetLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		srcLang = sourceLanguage;
 	}
 
 	/**
 	 * Sets the parameters
 	 * @param params IParameters object
 	 */
 	public void setParameters (IParameters params) {
 		this.params = (IParameters)params; // DWH 6-25-09 was net.sf.okapi.filters.markupfilter.Parameters
 		readParams(); // DWH 6-19-09
 	}
 
 	/**
 	 * Opens the document at the URI specified in the call to open(..),
 	 * looks through the names of the XML files inside to determine
 	 * the type, and creates a StartDocument Event.
 	 */
 	private Event openZipFile () {
 		File fZip;
 		String sEntryName,sZipType,sDocType; // DWH 5-28-09
 		int iCute;
 		try
 		{
 			fZip = new File(docURI.getPath());
 			zipFile = new ZipFile(fZip);
 			entries = zipFile.entries();
 
 			nZipType = -1;
 			while( entries.hasMoreElements() )
 			{ // note that [Content_Types].xml is always first
 				entry = entries.nextElement();
 				sEntryName = entry.getName();
 				iCute = 0;
 			    iCute = sEntryName.indexOf("/");
 			    if (iCute>0)
 			    {
 				    sZipType = sEntryName.substring(0,iCute);
 				    if (sZipType.equals("xl"))
 				    	nZipType = MSEXCEL;
 				    else if (sZipType.equals("word"))
 				    	nZipType = MSWORD;
 				    else if (sZipType.equals("ppt"))
 				    	nZipType = MSPOWERPOINT;
 				    else
 				    	continue;
 				    break;
 			    }
 			}
 			if (nZipType==-1)
 			{
 				LOGGER.log(Level.SEVERE,"MS Office 2007 filter tried to open a file that is not aMicrosoft Office 2007 Word, Excel, or Powerpoint file.");
 				throw new OkapiBadFilterInputException("MS Office 2007 filter tried to open a file that is not aMicrosoft Office 2007 Word, Excel, or Powerpoint file.");
 			}
 //			openXMLContentFilter.setUpConfig(nZipType);
 			  // DWH 3-4-09 sets Parameters inside OpenXMLContentFilter based on file type
 //			params = (net.sf.okapi.filters.markupfilter.Parameters)openXMLContentFilter.getParameters();
 			  // DWH 3-4-09 params for OpenXMLFilter
 			
 			if (nZipType==MSEXCEL &&
 					(bPreferenceTranslateExcelExcludeColors || bPreferenceTranslateExcelExcludeColumns))
 				// DWH 6-13-09 Excel options
 			{
 				ExcelAnalyzer ea = new ExcelAnalyzer(zipFile);
 //				ea.analyzeExcelGetSheetSizes();
 //				tsExcelExcludedColors = ea.analyzeExcelGetNonThemeColors(); // DWH 6-13-09 this should change when the UI is ready
 				tsExcelExcludedStyles = ea.analyzeExcelGetStylesOfExcludedColors(tsExcelExcludedColors);
 				openXMLContentFilter.setTsExcelExcludedStyles(tsExcelExcludedStyles);
 				nExcelOriginalSharedStringCount = ea.analyzeExcelGetSharedStringsCount();
 				openXMLContentFilter.initTmSharedStrings(nExcelOriginalSharedStringCount);
 				bProcessedExcelSheets = false;
 			}
 			if (nZipType==MSWORD && !bPreferenceTranslateWordHidden)
 				bMinedHiddenStyles = false; // DWH 5-28-09 so mine hidden styles first
 			entries = zipFile.entries();
 			openXMLContentFilter.initFileTypes(); // new HashTable for file types in zip file
 			openXMLContentFilter.setBPreferenceTranslateWordHidden(bPreferenceTranslateWordHidden);
 				// DWH 5-29-09 whether or not to translate hidden text
 			subDocId = 0;
 			nextAction = NextAction.NEXTINZIP;
 			
 			StartDocument startDoc = new StartDocument(docId);
 			startDoc.setName(docURI.getPath());
 			startDoc.setLanguage(srcLang);
 			startDoc.setMimeType(MIMETYPE);
 			startDoc.setFilterWriter(createFilterWriter());
 			startDoc.setLineBreak("\n");
 			ZipSkeleton skel = new ZipSkeleton(zipFile);
 			return new Event(EventType.START_DOCUMENT, startDoc, skel);
 		}
 		catch ( ZipException e )
 		{
 			LOGGER.log(Level.SEVERE,"Error opening zipped input file.");
 			throw new OkapiIOException("Error opening zipped input file.");
 		}
 		catch ( IOException e )
 		{
 			LOGGER.log(Level.SEVERE,"Error reading zipped input file.");
 			throw new OkapiIOException("Error reading zipped input file.");
 		}
 	}
 	
 	/**
 	 * Opens the next file in the zip fle, determines its type based on its name,
 	 * reads the yaml configuration file and sets the parameters, then creates
 	 * a DocumentPart Event if this file is to pass through unaltered, or 
 	 * subdocument Events otherwise
 	 * @return an appropriate Event for this XML file in the zip file
 	 */
 	private Event nextInZipFile () {
 		String sEntryName; // DWH 2-26-09
 		String sDocType; // DWH 2-26-09
 		int iCute; // DWH 2-26-09
 		boolean bInMainFile; // DWH 4-15-09
 		while( entries.hasMoreElements() ) { // note that [Content_Types].xml is always first
 			entry = entries.nextElement();
 			sEntryName = entry.getName();
 			sDocType = openXMLContentFilter.getContentType("/"+sEntryName);
 		    iCute = sDocType.lastIndexOf('.', sDocType.length()-1);
 		    if (iCute>0)
 			    sDocType = sDocType.substring(iCute+1);
 			if (nZipType==MSEXCEL)
 			{
 				openXMLContentFilter.setBPreferenceTranslateExcelExcludeColors(bPreferenceTranslateExcelExcludeColors);
 				openXMLContentFilter.setBPreferenceTranslateExcelExcludeColumns(bPreferenceTranslateExcelExcludeColumns);
 				if (bPreferenceTranslateExcelExcludeColors)
 					openXMLContentFilter.setTsExcelExcludedStyles(tsExcelExcludedStyles);
 				if (bPreferenceTranslateExcelExcludeColumns)
 					openXMLContentFilter.setTsExcelExcludedColumns(tsExcelExcludedColumns);
 				if (bPreferenceTranslateExcelExcludeColors || bPreferenceTranslateExcelExcludeColumns)
 				{ // do we exclude comment text in colors?
 					if (!bProcessedExcelSheets && !sEntryName.equals("[Content_Types].xml") &&
 							!sDocType.equals("worksheet+xml"))
 					{
 						resetExcel();
 						continue;
 					}
 					else if (bProcessedExcelSheets && (sEntryName.equals("[Content_Types].xml") ||
 													   sDocType.equals("worksheet+xml")))
 						continue;
 					if (sDocType.equals("worksheet+xml"))
 					{
 						iCute = sEntryName.indexOf(".xml");
 						if (iCute>-1)
 						{
 							sCurrentExcelSheet = sEntryName.substring(0,iCute); // DWH 6-25-09 current sheet number
 							iCute = sCurrentExcelSheet.indexOf("worksheets/sheet");
 							if (iCute>-1)
 							{
 								sCurrentExcelSheet = sCurrentExcelSheet.substring(iCute+16); // should leave only the number
 								openXMLContentFilter.setSCurrentExcelSheet(sCurrentExcelSheet);
 							}
 							else
 								openXMLContentFilter.setSCurrentExcelSheet("");
 						}
 						else
 							openXMLContentFilter.setSCurrentExcelSheet("");
 					}
 				}
 			}
 		    if (nZipType==MSWORD)
 		    {
 			    if (!bMinedHiddenStyles) // DWH 5-28-09 find styles for hidden text
 			    {
 			    	if (sDocType.equals("styles+xml"))
 			    	{
 			    		bMinedHiddenStyles = true;
 			    		entries = zipFile.entries(); // reset to go through all of them except styles and Content_Types
 			    	}
 			    	else if (!sEntryName.equals("[Content_Types].xml"))
 			    		continue;                    // save all but styles and Content_Types for 2nd go around
 			    }
 			    else if (!bPreferenceTranslateWordHidden &&
 			    		 (sEntryName.equals("[Content_Types].xml") || // but don't do Content_Types
 			    		  sDocType.equals("styles+xml")))             // and styles a second time
 			    	continue;
 			          // DWH 5-29-09 these two files have already been added to the zip, so don't add them again
 		    }
 			bInMainFile = (sEntryName.endsWith(".xml") &&
 			    		  (nZipType==MSWORD && sDocType.equals("main+xml") ||
 			    		   nZipType==MSPOWERPOINT && sDocType.equals("slide+xml")));
 		                    // DWH 5-26-09 translate if translating Powerpoint master slides
 			openXMLContentFilter.setBInMainFile(bInMainFile); // DWH 4-15-09 only allow blank text in main files
 			if (bInMainFile && bSquishable)
 			{
 				LOGGER.log(Level.FINER,"\n\n<<<<<<< "+sEntryName+" : "+sDocType+" >>>>>>>");
 				nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (net.sf.okapi.filters.markupfilter.Parameters)openXMLContentFilter.getParameters();
 					// DWH 6-15-09 fully specified Parameters
 				Event ually = openSubDocument(true); // DWH 6-25-09 save the event
 				resetExcel(); // DWH 6-25-09 if Excel and excluding colors or columns, start through zips again if done with worksheets
 				return ually; // DWH 6-25-09 now return the event
 			}
 			else if ( sEntryName.equals("[Content_Types].xml") ||
 				   (sEntryName.endsWith(".xml") &&
 				    ((nZipType==MSWORD &&
 				    	   (sDocType.equals("main+xml") ||
 			   				sDocType.equals("footnotes+xml") ||
 			   				sDocType.equals("endnotes+xml") ||
 		                    (sDocType.equals("header+xml") && bPreferenceTranslateWordHeadersFooters) ||
 		                    (sDocType.equals("footer+xml") && bPreferenceTranslateWordHeadersFooters) ||
 		                    (sDocType.equals("comments+xml") && bPreferenceTranslateComments) ||
 		                      // DWH 5-25-09 translate if translating comments
 		                    sDocType.equals("chart+xml") ||
 		                    (sDocType.equals("styles+xml") && !bPreferenceTranslateWordHidden) ||
 		                    sDocType.equals("settings+xml") ||
 		                    (sDocType.equals("core-properties+xml") && bPreferenceTranslateDocProperties) ||
 		                      // DWH 5-25-09 translate if translating document properties
 		                    sDocType.equals("glossary+xml"))) ||
 		             (nZipType==MSEXCEL &&
 		            	   (sDocType.equals("sharedStrings+xml") ||
 		            	    (sDocType.equals("worksheet+xml") &&
 		            	      (bPreferenceTranslateExcelExcludeColors || bPreferenceTranslateExcelExcludeColumns)) ||
 		            	//	sDocType.equals("main+xml") || DWH 5-15-09 workbook.xml has nothing translatable
 		            		(sDocType.equals("comments+xml") && bPreferenceTranslateDocProperties) ||
 		                      // DWH 5-25-09 translate if translating comments
 		            	    sDocType.equals("table+xml"))
 				   			) ||
 				   	 (nZipType==MSPOWERPOINT &&
 				   	       (((sDocType.equals("notesSlide+xml") && bPreferenceTranslatePowerpointNotes)) ||
 				   	    	  sDocType.equals("slideMaster+xml") && bPreferenceTranslatePowerpointMasters))))) {
 						     // DWH 5-26-09 translate if translating Powerpoint notes
 				if (nZipType==MSWORD && sDocType.equals("chart+xml")) // DWH 4-16-09
 					nFileType = MSWORDCHART;
 				else if (nZipType==MSWORD && sDocType.equals("core-properties+xml"))
 					nFileType = MSWORDDOCPROPERTIES; // DWH 5-25-09
 				else if (nZipType==MSEXCEL && sDocType.equals("comments+xml")) // DWH 5-13-09
 					nFileType = MSEXCELCOMMENT;
 				else
 					nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (net.sf.okapi.filters.markupfilter.Parameters)openXMLContentFilter.getParameters();
 				  // DWH 6-15-09 fully specified Parameters
 				LOGGER.log(Level.FINER,"<<<<<<< "+sEntryName+" : "+sDocType+" >>>>>>>");
 				Event ually = openSubDocument(false); // DWH 6-25-09 save the event
 				resetExcel(); // DWH 6-25-09 if Excel and excluding colors or columns, start through zips again if done with worksheets
 				return ually; // DWH 6-25-09 now return the event
 			}
 			else {
 				nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (net.sf.okapi.filters.markupfilter.Parameters)openXMLContentFilter.getParameters();
 				  // DWH 6-15-09 fully specified Parameters
 				DocumentPart dp = new DocumentPart(entry.getName(), false);
 				ZipSkeleton skel = new ZipSkeleton(entry);
 				Event ually = new Event(EventType.DOCUMENT_PART, dp, skel); // DWH 6-25-09 save the event
 				resetExcel(); // DWH 6-25-09 if Excel and excluding colors or columns, start through zips again if done with worksheets
 				return ually; // DWH 6-25-09 now return the event
 			}
 		}
 
 		// No more sub-documents: end of the ZIP document
 		close();
 		Ending ending = new Ending("ed");
 		return new Event(EventType.END_DOCUMENT, ending);
 	}
 	
 	/**
 	 * Opens an XML file in the zip file, sets its parameters, combines
 	 * compatible contiguous text runs if desired, and creates a 
 	 * START_SUBDOCUMENT event
 	 * @param bSquishing true to combine compatible contiguous text runs
 	 * @return a START_SUBDOCUMENT event
 	 */
 	private Event openSubDocument (boolean bSquishing) {
 		PipedInputStream squishedInputStream;
 		PipedOutputStream pios=null;
 		BufferedInputStream bis; // DWH 3-5-09
 		InputStream isInputStream;
 		openXMLContentFilter.close(); // Make sure the previous is closed
 		openXMLContentFilter.setParameters((net.sf.okapi.filters.markupfilter.Parameters)params);
 		//YS openXMLContentFilter.setOptions(srcLang, "UTF-8", true);
 		Event event;
 		try
 		{
 			isInputStream = zipFile.getInputStream(entry);
 			if (bSquishing)
 			{
 				if (pios!=null)
 				{
 					try
 					{
 						pios.close();
 					} catch (IOException e) {
 						// e.printStackTrace();
 						LOGGER.log(Level.SEVERE,"Error closing input stream.");
 						throw new OkapiIOException("Error closing input stream.");
 					}
 				}
 				pios = new PipedOutputStream(); // DWH 2-19-09 this may need to be final
 				squishedInputStream = (PipedInputStream)openXMLContentFilter.combineRepeatedFormat(isInputStream,pios); // DWH 2-3-09
 //				openXMLContentFilter.open(squishedInputStream); // DWH 2-3-09 was isInputStream
 				bis = new BufferedInputStream(squishedInputStream); // DWH 3-5-09 allows mark and reset
 			}
 			else
 			{
 //				openXMLContentFilter.open(isInputStream);
 				bis = new BufferedInputStream(isInputStream); // DWH 3-5-09 allows mark and reset
 			}
 
 			openXMLContentFilter.open(new RawDocument(bis, "UTF-8", srcLang)); // YS 4-7-09 // DWH 3-5-09
 			if (!bPreferenceTranslateWordHidden || !bPreferenceTranslateWordAllStyles)
 				  // DWH 5-28-09 list of styles to exclude
 				openXMLContentFilter.setTsExcludeWordStyles(tsExcludeWordStyles);
 			//			openXMLContentFilter.next(); // START
 			event = openXMLContentFilter.next(); // START_DOCUMENT
 			LOGGER.log(Level.FINEST,openXMLContentFilter.getParameters().toString());
 			  // DWH 4-22-09 This lists what YAML actually read out of the configuration file
 		}
 		catch (IOException e) {
 			LOGGER.log(Level.SEVERE,"Error streaming input.");
 			throw new OkapiIOException("Error streaming input.");
 		}
 		
 		// Change the START_DOCUMENT event to START_SUBDOCUMENT
 		StartSubDocument sd = new StartSubDocument(docId, String.valueOf(++subDocId));
 		sd.setName(entry.getName());
 		cparams.nFileType = nFileType; // DWH 6-27-09 record File Type for the OpenXMLContentSkeletonWriter
 		sd.setFilterParameters(cparams); // DWH 6-27-09 StartSubdocument will have conditional parameter info
 		nextAction = NextAction.NEXTINSUBDOC;
 		ZipSkeleton skel = new ZipSkeleton(
 			(GenericSkeleton)event.getResource().getSkeleton(), entry);
 		return new Event(EventType.START_SUBDOCUMENT, sd, skel);
 	}
 	
 	/**
 	 * Returns the next subdocument event.  If it is a TEXT_UNIT event,
 	 * it invokes the translator to manipulate the text before sending
 	 * on the event.  If it is an END_DOCUMENT event, it sends on
 	 * an END_SUBDOCUMENT event instead.
 	 * @return a subdocument event
 	 */
 	private Event nextInSubDocument () {
 		Event event;
 		while ( openXMLContentFilter.hasNext() ) {
 			event = openXMLContentFilter.next();
 			switch ( event.getEventType() ) {
 				case TEXT_UNIT:
 					if (translator!=null)
 					{
 						TextUnit tu = (TextUnit)event.getResource();
 						TextFragment tfSource = tu.getSourceContent();
 						String torg = translator.translate(tfSource,LOGGER,nFileType); // DWH 5-7-09 nFileType
 						TextFragment tfTarget = tfSource.clone();
 //						tfTarget.setCodedText(torg);
 						tfTarget.setCodedText(/*"GLUNK "+*/torg); // DWH 4-8-09 testing 1 2 3 
 						TextContainer tc = new TextContainer();
 						tc.setContent(tfTarget);
 						tu.setTarget(sOutputLanguage, tc);
 						tfSource = null;
 					}
 					openXMLContentFilter.displayOneEvent(event);
 					return event;
 				case END_DOCUMENT:
 					// Read the FINISHED event
 	//				openXMLContentFilter.next();
 					// Change the END_DOCUMENT to END_SUBDOCUMENT
 					if (!bPreferenceTranslateWordHidden) // DWH 5-28-09 save mined styles
 						tsExcludeWordStyles = openXMLContentFilter.getTsExcludeWordStyles();
 					Ending ending = new Ending(String.valueOf(subDocId));
 					nextAction = NextAction.NEXTINZIP;
 					ZipSkeleton skel = new ZipSkeleton(
 						(GenericSkeleton)event.getResource().getSkeleton(), entry);
 					return new Event(EventType.END_SUBDOCUMENT, ending, skel);
 				
 				default: // Else: just pass the event through
 					openXMLContentFilter.displayOneEvent(event);
 					return event;
 			}
 		}
 		return null; // Should not get here
 	}
 	/**
 	 * Returns the type of zip file.
 	 * @return the type of zip file
 	 */
 	public int getNZipType() // DWH 4-13-09
 	{
 		return nZipType;
 	}
 	/**
 	 * Returns the OpenXMLContentFilter.
 	 * @return the OpenXMLContentFilter
 	 */
 	public OpenXMLContentFilter getOpenXMLContentFilter()
 	{
 		return openXMLContentFilter;
 	}
 	/**
 	 * In Excel, if some colors or columns are excluded, this checks to see that [Content_Types] and all worksheets have been processed,
 	 * then runs through the rest
 	 */
 	private void resetExcel()
 	{
 		if (!bProcessedExcelSheets && !entries.hasMoreElements()) // DWH 6-13-09 Excel options
 		{ // this only happens if bPreferenceTranslateExcelExcludeColors || bPreferenceTranslateExcelExcludeColumns
 			entries = zipFile.entries();  // after going through all the sheets, reset to go through the rest
 			bProcessedExcelSheets = true; // and indicate you have already gone through the sheets
 		}
 	}
 	public void cancel() {
 		// TODO Auto-generated method stub		
 	}
 	/**
 	 * Sets the java.util.logging.Logger.
 	 */
 	public void setLogger(Logger lgr)
 	{
 		LOGGER = lgr;
 		if (openXMLContentFilter!=null)
 			openXMLContentFilter.setLogger(lgr);
 	}
 	/**
 	 * Returns the java.util.logging.Logger.
 	 * @return the java.util.logging.Logger
 	 */
 	public Logger getLogger()
 	{
 		return LOGGER;
 	}
 	/**
 	 * Returns the current Log Level.
 	 * @return he current Log Level
 	 */
 	public void setLogLevel(Level lvl)
 	{
 		nLogLevel = lvl;
 		LOGGER.setLevel(lvl);
 	}
 	public void setBPreferenceTranslateDocProperties(boolean bPreferenceTranslateDocProperties)
 	{
 		this.bPreferenceTranslateDocProperties = bPreferenceTranslateDocProperties;
 	}
 	public boolean getBPreferenceTranslateDocProperties()
 	{
 		return bPreferenceTranslateDocProperties;
 	}
 	public void setBPreferenceTranslateComments(boolean bPreferenceTranslateComments)
 	{
 		this.bPreferenceTranslateComments = bPreferenceTranslateComments;
 	}
 	public boolean getBPreferenceTranslateComments()
 	{
 		return bPreferenceTranslateComments;
 	}
 	public void setBPreferenceTranslatePowerpointNotes(boolean bPreferenceTranslatePowerpointNotes)
 	{
 		this.bPreferenceTranslatePowerpointNotes = bPreferenceTranslatePowerpointNotes;
 	}
 	public boolean getBPreferenceTranslatePowerpointNotes()
 	{
 		return bPreferenceTranslatePowerpointNotes;
 	}
 	public void setBPreferenceTranslatePowerpointMasters(boolean bPreferenceTranslatePowerpointMasters)
 	{
 		this.bPreferenceTranslatePowerpointMasters = bPreferenceTranslatePowerpointMasters;
 	}
 	public boolean getBPreferenceTranslatePowerpointMasters()
 	{
 		return bPreferenceTranslatePowerpointMasters;
 	}
 	public void setBPreferenceTranslateWordHeadersFooters(boolean bPreferenceTranslateWordHeadersFooters)
 	{
 		this.bPreferenceTranslateWordHeadersFooters = bPreferenceTranslateWordHeadersFooters;
 	}
 	public boolean getBPreferenceTranslateWordHeadersFooters()
 	{
 		return bPreferenceTranslateWordHeadersFooters;
 	}
 	public void setBPreferenceTranslateWordHidden(boolean bPreferenceTranslateWordHidden)
 	{
 		this.bPreferenceTranslateWordHidden = bPreferenceTranslateWordHidden;
 	}
 	public boolean getBPreferenceTranslateWordHidden()
 	{
 		return bPreferenceTranslateWordHidden;
 	}
 	public void setBPreferenceTranslateWordAllStyles(boolean bPreferenceTranslateWordAllStyles)
 	{
 		this.bPreferenceTranslateWordAllStyles = bPreferenceTranslateWordAllStyles;
 	}
 	public boolean getBPreferenceTranslateWordAllStyles()
 	{
 		return bPreferenceTranslateWordAllStyles;
 	}
 }
