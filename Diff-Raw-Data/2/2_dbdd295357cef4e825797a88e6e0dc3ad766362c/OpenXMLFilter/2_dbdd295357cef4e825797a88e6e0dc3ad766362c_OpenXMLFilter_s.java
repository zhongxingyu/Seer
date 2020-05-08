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
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.exceptions.*;
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
 import net.sf.okapi.common.filterwriter.ZipFilterWriter;
 import net.sf.okapi.filters.markupfilter.Parameters;
 
 public class OpenXMLFilter implements IFilter {
 	
 	private Logger LOGGER = Logger.getLogger(OpenXMLFilter.class.getName());
 
 	private enum NextAction {
 		OPENZIP, NEXTINZIP, NEXTINSUBDOC, DONE
 	}
 
 	public final static int MSWORD=1;
 	public final static int MSEXCEL=2;
 	public final static int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
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
 	private Parameters params=null;
 	private int nZipType=MSWORD;
 	private int nFileType=MSWORD; // DWH 4-16-09
 	private Level nLogLevel=Level.FINE;
 	private boolean bSquishable=true;
 	private ITranslator translator=null;
 	private String sOutputLanguage="en-US";
 	private boolean canceled = false;
 
 	public OpenXMLFilter () {
 	}
 	
 	public OpenXMLFilter(ITranslator translator, String sOutputLanguage) {
 		this.translator = translator;
 		this.sOutputLanguage = sOutputLanguage;
 	}
 
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
 
 	public void setNLogLevel(Level nLogLevel) // set debug level
 	{
 		this.nLogLevel = nLogLevel;
 	}
 	
 	public ISkeletonWriter createSkeletonWriter () {
 		return null; // There is no corresponding skeleton writer
 	}
 	
 	public IFilterWriter createFilterWriter () {
		return new ZipFilterWriter();
 	}
 
 	public String getName () {
 		return "okf_openxml";
 	}
 
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return ((( queue != null ) && ( !queue.isEmpty() )) || ( nextAction != NextAction.DONE ));
 	}
 
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
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 			LOGGER.log(Level.FINER,"\nOpening "+input.getInputURI().toString());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 	
 	public void open (RawDocument input,
 			boolean generateSkeleton, boolean bSquishable)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI(),bSquishable);
 			LOGGER.log(Level.FINER,"\nOpening "+input.getInputURI().toString());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 
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
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 
 	public void open (InputStream input) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException(
 			"Method is not supported for this filter.");
 	}
 
 	private void open (CharSequence inputText) {
 		// Not supported for this filter
 		throw new UnsupportedOperationException(
 			"Method is not supported for this filter.");
 	}
 
 	private void open (URI inputURI) {
 		open(inputURI,true,Level.FINE); // DWH 2-26-09 just a default
 	}
 	
 	public void open (URI inputURI, boolean bSquishable) {
 		open(inputURI, bSquishable, Level.FINE);
 	}
 	
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
 
 	public void setOptions (String sourceLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		setOptions(sourceLanguage, null, defaultEncoding, generateSkeleton);
 	}
 
 	public void setOptions (String sourceLanguage,
 		String targetLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		srcLang = sourceLanguage;
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	private Event openZipFile () {
 		File fZip;
 		String sEntryName,sZipType;
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
 				throw new BadFilterInputException("MS Office 2007 filter tried to open a file that is not aMicrosoft Office 2007 Word, Excel, or Powerpoint file.");
 			}
 //			openXMLContentFilter.setUpConfig(nZipType);
 			  // DWH 3-4-09 sets Parameters inside OpenXMLContentFilter based on file type
 //			params = (Parameters)openXMLContentFilter.getParameters();
 			  // DWH 3-4-09 params for OpenXMLFilter
 			
 			entries = zipFile.entries();
 			openXMLContentFilter.initFileTypes(); // new HashTable for file types in zip file
 			subDocId = 0;
 			nextAction = NextAction.NEXTINZIP;
 			
 			StartDocument startDoc = new StartDocument(docId);
 			startDoc.setName(docURI.getPath());
 			startDoc.setLanguage(srcLang);
 			startDoc.setMimeType(MIMETYPE);
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
 			bInMainFile = (sEntryName.endsWith(".xml") &&
 				    		((nZipType==MSWORD && sDocType.equals("main+xml")) ||
 				    		 (nZipType==MSPOWERPOINT && sDocType.equals("slide+xml"))));
 			openXMLContentFilter.setBInMainFile(bInMainFile); // DWH 4-15-09 only allow blank text in main files
 			if (bInMainFile && bSquishable)
 			{
 				LOGGER.log(Level.FINER,"\n\n<<<<<<< "+sEntryName+" : "+sDocType+" >>>>>>>");
 				nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (Parameters)openXMLContentFilter.getParameters();
 				return openSubDocument(true);
 			}
 			else if ( sEntryName.equals("[Content_Types].xml") ||
 				   (sEntryName.endsWith(".xml") &&
 				    ((nZipType==MSWORD &&
 				    	   (sDocType.equals("main+xml") ||
 			   				sDocType.equals("footnotes+xml") ||
 			   				sDocType.equals("endnotes+xml") ||
 		                    sDocType.equals("header+xml") ||
 		                    sDocType.equals("footer+xml") ||
 		                    sDocType.equals("comments+xml") ||
 		                    sDocType.equals("chart+xml") ||
 		                    sDocType.equals("settings+xml") ||
 		                    sDocType.equals("glossary+xml"))) ||
 		             (nZipType==MSEXCEL &&
 		            	   (sDocType.equals("main+xml") ||
 				   	  	    sDocType.equals("worksheet+xml") ||
 				   			sDocType.equals("sharedStrings+xml") ||
 				   			sDocType.equals("table+xml") ||
 				   			sDocType.equals("comments+xml"))) ||
 				   	 (nZipType==MSPOWERPOINT &&
 				   	       (sDocType.equals("slide+xml") ||
 				   	        sDocType.equals("notesSlide+xml")))))) {
 				if (nZipType==MSWORD && sDocType.equals("chart+xml")) // DWH 4-16-09
 					nFileType = MSWORDCHART;
 				else
 					nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (Parameters)openXMLContentFilter.getParameters();
 				LOGGER.log(Level.FINER,"<<<<<<< "+sEntryName+" : "+sDocType+" >>>>>>>");
 				return openSubDocument(false);
 			}
 			else {
 				nFileType = nZipType;
 				openXMLContentFilter.setUpConfig(nFileType);
 				params = (Parameters)openXMLContentFilter.getParameters();
 				DocumentPart dp = new DocumentPart(entry.getName(), false);
 				ZipSkeleton skel = new ZipSkeleton(entry);
 				return new Event(EventType.DOCUMENT_PART, dp, skel);
 			}
 		}
 
 		// No more sub-documents: end of the ZIP document
 		close();
 		Ending ending = new Ending("ed");
 		return new Event(EventType.END_DOCUMENT, ending);
 	}
 	
 	private Event openSubDocument (boolean bSquishing) {
 		PipedInputStream squishedInputStream;
 		PipedOutputStream pios=null;
 		BufferedInputStream bis; // DWH 3-5-09
 		InputStream isInputStream;
 		openXMLContentFilter.close(); // Make sure the previous is closed
 		openXMLContentFilter.setParameters(params);
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
 		nextAction = NextAction.NEXTINSUBDOC;
 		ZipSkeleton skel = new ZipSkeleton(
 			(GenericSkeleton)event.getResource().getSkeleton(), entry);
 		return new Event(EventType.START_SUBDOCUMENT, sd, skel);
 	}
 	
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
 						String torg = translator.translate(tfSource);
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
 	public int getNZipType() // DWH 4-13-09
 	{
 		return nZipType;
 	}
 	public OpenXMLContentFilter getOpenXMLContentFilter()
 	{
 		return openXMLContentFilter;
 	}
 
 	public void cancel() {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setLogger(Logger lgr)
 	{
 		LOGGER = lgr;
 		if (openXMLContentFilter!=null)
 			openXMLContentFilter.setLogger(lgr);
 	}
 	public Logger getLogger()
 	{
 		return LOGGER;
 	}
 	public void setLogLevel(Level lvl)
 	{
 		nLogLevel = lvl;
 		LOGGER.setLevel(lvl);
 	}
 }
