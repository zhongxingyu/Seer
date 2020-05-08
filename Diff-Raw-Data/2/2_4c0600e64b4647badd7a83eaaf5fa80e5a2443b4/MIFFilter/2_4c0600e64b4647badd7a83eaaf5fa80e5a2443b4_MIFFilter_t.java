 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.mif;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.InputResource;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 public class MIFFilter implements IFilter {
 	
 	private static final String MIMETYPE = "text/x-mif"; // TODO: check value
 	private static final Hashtable<String, Character> charTable = initCharTable();
 
 	private String docName;
 	private BufferedReader reader;
 	private StringBuilder tagBuffer;
 	private StringBuilder strBuffer;
 	private int inPara;
 	private int inString;
 	private int tuId;
 	private int otherId;
 	private int level;
 	private TextContainer cont;
 	private boolean canceled;
 	private LinkedList<Event> queue;
 	private String srcLang;
 	private GenericSkeleton skel;
 	private boolean hasNext;
 	
 	private static Hashtable<String, Character> initCharTable () {
 		Hashtable<String, Character> table = new Hashtable<String, Character>();
 		table.put("HardSpace", '\u00a0');
 		table.put("DiscHyphen", '\u00ad');
 		table.put("NoHyphen", '\u200d');
 		table.put("Tab", '\t');
 		table.put("Cent", '\u00a2');
 		table.put("Pound", '\u00a3');
 		table.put("Yen", '\u00a5');
 		table.put("EnDash", '\u2013');
 		table.put("Dagger", '\u2020');
 		table.put("EmDash", '\u2014');
 		table.put("DoubleDagger", '\u2021');
 		table.put("Bullet", '\u2022');
 		table.put("NumberSpace", '\u2007');
 		table.put("ThinSpace", '\u2009');
 		table.put("EnSpace", '\u2002');
 		table.put("EmSpace", '\u2003');
 		table.put("HardReturn", '\r');
 		return table;
 	}
 
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		try {
 			if ( reader != null ) {
 				reader.close();
 				reader = null;
 			}
 			hasNext = false;
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public String getName () {
 		return "MIFFilter";
 	}
 	
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public IParameters getParameters () {
 		return null;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public void open (InputResource input) {
 		open(input, true);
 	}
 	
 	public void open (InputResource input,
 		boolean generateSkeleton)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new RuntimeException("InputResource has no input defined.");
 		}
 	}
 	
 	private void setOptions (String sourceLanguage,
 		String targetLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		srcLang = sourceLanguage;
 	}
 
 	private void open (InputStream input) {
 		try {
 			close();
 			// Detect encoding
 			String encoding = guessEncoding(input);
 			reader = new BufferedReader(
 				new InputStreamReader(input, encoding));
 			tagBuffer = new StringBuilder();
 			strBuffer = new StringBuilder();
 			level = 0;
 			inPara = -1;
 			inString = -1;
 			tuId = 0;
 			otherId = 0;
 			canceled = false;
 			hasNext = true;
 			
 			queue = new LinkedList<Event>();
 			StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 			startDoc.setName(docName);
 			startDoc.setLineBreak("\n");
 			startDoc.setEncoding(encoding, false); //TODO: UTF8 BOM detection
 			startDoc.setLanguage(srcLang);
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setType(getMimeType());
 			startDoc.setMimeType(getMimeType());
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void open (URI inputURI) {
 		try {
 			docName = inputURI.getPath();
 			open(inputURI.toURL().openStream());
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void open (CharSequence inputText) {
 		// Not supported with MIF filter
 		throw new OkapiNotImplementedException();
 	}
 
 	public void setParameters (IParameters params) {
 	}
 
 	public Event next () {
 		// Treat cancel
 		if ( canceled ) {
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 			hasNext = false;
 		}
 		// Fill the queue if it's empty
 		if ( queue.isEmpty() ) {
 			read();
 		}
 		// Update hasNext flag on the FINISHED event
 		if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 			hasNext = false;
 		}
 		// Return the head of the queue
 		return queue.poll();
 	}
 
 	public ISkeletonWriter createSkeletonWriter () {
 		return new GenericSkeletonWriter();
 	}
 	
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	private void read () {
 		try {
 			// Process other calls
 			skel = new GenericSkeleton();
 			int c;
 			while ( (c = reader.read()) != -1 ) {
 				switch ( c ) {
 				case '#':
 					skel.append((char)c);
 					readComment();
 					break;
 				case '<': // Start of statement
 					level++;
 					//skel.append((char)c);
 					String tag = readTag();
 					if ( "Para".equals(tag) ) {
 						inPara = level;
 						cont = new TextContainer();
 					}
 					else if ( "String".equals(tag) ) {
 						inString = level;
 					}
 					//TODO: inline
 					break;
 				case '>': // End of statement
 					if ( inString == level ) {
 						inString = -1;
 					}
 					else if ( inPara == level ) {
 						inPara = -1;
 						if ( !cont.isEmpty() ) {
 							TextUnit tu = new TextUnit(String.valueOf(++tuId));
 							tu.setSource(cont);
 							tu.setMimeType("text/x-mif");
 							skel.addContentPlaceholder(tu);
 							tu.setSkeleton(skel);
 							queue.add(new Event(EventType.TEXT_UNIT, tu));
 							return;
 						}
 					}
 					skel.append((char)c);
 					level--;
 					// Return skeleton
 					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel); 
 					queue.add(new Event(EventType.DOCUMENT_PART, dp));
 					return;
 				case '`':
 					if (( inPara > -1 ) && ( level == inString )) {
 						cont.append(processString());
 					}
 					else {
 						skel.append((char)c); // Store '`'
 						copyStringToStorage();
 					}
 					break;
 				default:
 					skel.append((char)c);
 					break;
 				}
 			}
 			
 			Ending ending = new Ending(String.valueOf(++otherId)); 
 			queue.add(new Event(EventType.END_DOCUMENT, ending));
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 
 		// Else: we are done
 		queue.add(new Event(EventType.END_DOCUMENT,
 			new Ending(String.valueOf(++otherId))));
 	}
 
 	private void readComment () throws IOException {
 		int c;
 		while ( (c = reader.read()) != -1 ) {
 			skel.append((char)c);
 			switch ( c ) {
 			case '\r':
 			case '\n':
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Reads a tag name.
 	 * @return The name of the tag.
 	 * @throws IOException
 	 */
 	private String readTag () throws IOException {
 		tagBuffer.setLength(0);
 		int c;
 		boolean leadingWSDone = false;
 		// Skip and whitespace between '<' and the name
 		do {
 			switch ( c = reader.read() ) {
 			case ' ':
 			case '\t':
 			case '\r':
 			case '\n':
 				// Let go for now
 				//buffer.append((char)c);
 				break;
 			case -1:
 			default:
 				leadingWSDone = true;
 				break;
 			}
 		}
 		while ( !leadingWSDone );
 		
 		// Now read the name
 		while ( true ) {
 			switch ( c ) {
 			case ' ':
 			case '\t':
 			case '\r':
 			case '\n':
 				skel.append(tagBuffer.toString());
 				skel.append((char)c);
 				return tagBuffer.toString();
 			case -1:
 				throw new RuntimeException("Unexpected end of input.");
 			default:
 				tagBuffer.append((char)c);
 				break;
 			}
 			c = reader.read();
 		}
 	}
 	
 	void copyStringToStorage () throws IOException {
 		int c;
 		boolean inEscape = false;
 		while ( (c = reader.read()) != -1 ) {
 			skel.append((char)c);
 			if ( inEscape ) {
 				inEscape = false;
 			}
 			else {
 				if ( c == '\'' ) return;
 			}
 		}
 		// Else: Missing end of string error
 		throw new RuntimeException("End of string is missing.");
 	}
 	
 	String processString () throws IOException {
 		strBuffer.setLength(0);
 		int c;
 		boolean inEscape = false;
 		while ( (c = reader.read()) != -1 ) {
 			if ( inEscape ) {
 				switch ( c ) {
 				case '\\':
 				case '>':
 					strBuffer.append((char)c);
 					break;
 				case 't':
 					strBuffer.append('\t');
 					break;
 				case 'Q':
 					strBuffer.append('`');
 					break;
 				case 'q':
 					strBuffer.append('\'');
 					break;
 				case 'u':
 				case 'x':
 					//TODO: parse escaped U and X styled chars
 					break;
 				}
 				inEscape = false;
 			}
 			else {
 				switch ( c ) {
 				case '\'': // End of string
 					return strBuffer.toString();
 				case '\\':
 					inEscape = true;
 					break;
 				default:
 					strBuffer.append((char)c);
 					break;
 				}
 			}
 		}
 		// Else: Missing end of string error
 		throw new RuntimeException("End of string is missing.");
 	}
 
 	private String guessEncoding (InputStream input) {
 		try {
 			// Open the file for byte reading
 		
 			// Read the start of the file to an array of bytes
 		
 			// Try to match the file signature with the pre-defined signatures
 		}
 		finally {
 			
 		}
 		return "UTF-8"; // No encoding detected: use UTF-8
 	}
 
 }
