 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.rtf;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 public class RTFFilter implements IFilter {
 
 	public static final String PROP_HASHIDDENTEXT = "hashiddentext";
 
 	public static final int TOKEN_CHAR           = 0;
 	public static final int TOKEN_STARTGROUP     = 1;
 	public static final int TOKEN_ENDGROUP       = 2;
 	public static final int TOKEN_ENDINPUT       = 3;
 	public static final int TOKEN_CTRLWORD       = 4;
 
 	// Do not use 0. Reserved for parsing
 	public static final int CW_ANSI              = 1;
 	public static final int CW_F                 = 2;
 	public static final int CW_U                 = 3;
 	public static final int CW_ANSICPG           = 4;
 	public static final int CW_LQUOTE            = 5;
 	public static final int CW_RQUOTE            = 6;
 	public static final int CW_LDBLQUOTE         = 7;
 	public static final int CW_RDBLQUOTE         = 8;
 	public static final int CW_BULLET            = 9;
 	public static final int CW_ENDASH            = 10;
 	public static final int CW_EMDASH            = 11;
 	public static final int CW_ZWJ               = 12;
 	public static final int CW_ZWNJ              = 13;
 	public static final int CW_LTRMARK           = 14;
 	public static final int CW_RTLMARK           = 15;
 	public static final int CW_UC                = 16;
 	public static final int CW_CPG               = 17;
 	public static final int CW_FONTTBL           = 18;
 	public static final int CW_FCHARSET          = 19;
 	public static final int CW_PAR               = 20;
 	public static final int CW_PAGE              = 21;
 	public static final int CW_STYLESHEET        = 22;
 	public static final int CW_COLORTBL          = 23;
 	public static final int CW_SPECIAL           = 24;
 	public static final int CW_FOOTNOTE          = 25;
 	public static final int CW_TAB               = 26;
 	public static final int CW_V                 = 27;
 	public static final int CW_XE                = 28;
 	public static final int CW_CCHS              = 29;
 	public static final int CW_PICT              = 30;
 	public static final int CW_SHPTXT            = 31;
 	public static final int CW_LINE              = 32;
 	public static final int CW_INDEXSEP          = 33;
 	public static final int CW_ULDB              = 34;
 	public static final int CW_TITLE             = 35;
 	public static final int CW_TROWD             = 36;
 	public static final int CW_CELL              = 37;
 	public static final int CW_BKMKSTART         = 38;
 	public static final int CW_ROW               = 39;
 	public static final int CW_UL                = 40;
 	public static final int CW_PARD              = 41;
 	public static final int CW_NONSHPPICT        = 42;
 	public static final int CW_INFO              = 43;
 	public static final int CW_CS                = 44;
 	public static final int CW_DELETED           = 45;
 	public static final int CW_PLAIN             = 46;
 	public static final int CW_BKMKEND           = 47;
 	public static final int CW_ANNOTATION        = 48;
 	public static final int CW_MAC               = 49;
 	public static final int CW_PC                = 50;
 	public static final int CW_PCA               = 51;
 	public static final int CW_FTNSEP            = 52;
 	public static final int CW_FTNSEPC           = 53;
 	public static final int CW_AFTNSEP           = 54;
 	public static final int CW_AFTNSEPC          = 55;
 	public static final int CW_RTF               = 56;
 	public static final int CW_FLDINST           = 57;
 	public static final int CW_XMLOPEN           = 58;
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	// Tags from S-Tagger that are open/close, all other are isolated
 	private static final String PAIREDTAGS = ":fc:cs:b:i:s:bi:c:c1:c2:cns:el:elf:ti:";
 	
 	private BufferedReader reader;
 	private boolean canceled;
 	private LocaleId trgLang;
 	private String passedEncoding;
 	private Hashtable<Integer, String> winCharsets;
 	private Hashtable<Integer, String> winCodepages;
 	private Hashtable<String, Integer> controlWords;
 	private Hashtable<Integer, RTFFont> fonts;
 	private char chCurrent;
 	private char chPrevTextChar;
 	private int value;
 	private Stack<RTFContext> ctxStack;
 	private int inFontTable;
 	private String defaultEncoding;
 	private boolean setCharset0ToDefault;
 	private StringBuilder word;
 	private boolean reParse;
 	private char chReParseChar;
 	private int skip;
 	private int code;
 	private int group;
 	private int noReset;
 	private byte byteData;
 	private char uChar;
 	private int internalStyle;
 	private boolean rtfDetected;
 //	private int doNotTranslateStyle;
 	private CharsetDecoder currentCSDec;
 	private int currentDBCSCodepage;
 	private String currentCSName;
 	private ByteBuffer byteBuffer;
 	private LinkedList<Event> queue;
 	private boolean hasNext;
 	private String docName;
 	private int tuId;
 	private EncoderManager encoderManager;
 	
 	public RTFFilter () {
 		winCharsets = new Hashtable<Integer, String>();
 		winCharsets.put(0, "windows-1252");
 		winCharsets.put(1, Charset.defaultCharset().name());
 		winCharsets.put(2, "symbol");
 		winCharsets.put(3, "invalid-fcharset");
 		winCharsets.put(77, "MacRoman");
 		winCharsets.put(128, "Shift_JIS"); // Japanese, DBCS
 		winCharsets.put(129, "windows949"); // Korean (no dash)
 		winCharsets.put(130, "johab"); // Korean, DBCS
 		winCharsets.put(134, "windows-936");  // Simplified Chinese
 		winCharsets.put(136, "Big5"); // Traditional Chinese
 		winCharsets.put(161, "windows-1253"); // Greek
 		winCharsets.put(162, "windows-1254"); // Turkish
 		winCharsets.put(163, "windows-1258"); // Vietnamese
 		winCharsets.put(177, "windows-1255"); // Hebrew
 		winCharsets.put(178, "windows-1256"); // Arabic
 		winCharsets.put(179, "windows-1256"); // Arabic Traditional
 		winCharsets.put(180, "arabic-user"); // Arabic User
 		winCharsets.put(181, "hebrew-user"); // hebrew User
 		winCharsets.put(186, "windows-1257"); // Baltic
 		winCharsets.put(204, "windows-1251"); // Russian
 		winCharsets.put(222, "windows-874"); // Thai
 		winCharsets.put(238, "windows-1250"); // Eastern European
 		winCharsets.put(254, "ibm437"); // PC-437
 		winCharsets.put(255, "oem"); // OEM
 		
 		winCodepages = new Hashtable<Integer, String>();
 		winCodepages.put(437, "IBM437");
 		winCodepages.put(850, "IBM850");
 		winCodepages.put(852, "IBM852");
 		winCodepages.put(932, "Shift_JIS");
 		winCodepages.put(936, "windows-936");
 		winCodepages.put(949, "windows949"); // (No dash)
 		winCodepages.put(950, "Big5");
 		winCodepages.put(1250, "windows-1250");
 		winCodepages.put(1251, "windows-1251");
 		winCodepages.put(1252, "windows-1252");
 		winCodepages.put(1253, "windows-1253");
 		winCodepages.put(1254, "windows-1254");
 		winCodepages.put(1255, "windows-1255");
 		winCodepages.put(1256, "windows-1256");
 		winCodepages.put(1257, "windows-1257");
 		winCodepages.put(1258, "windows-1258");
 /* 
 		708 Arabic (ASMO 708) 
 		709 Arabic (ASMO 449+, BCON V4) 
 		710 Arabic (transparent Arabic) 
 		711 Arabic (Nafitha Enhanced) 
 		720 Arabic (transparent ASMO) 
 		819 Windows 3.1 (United States and Western Europe) 
 		860 Portuguese 
 		862 Hebrew 
 		863 French Canadian 
 		864 Arabic 
 		865 Norwegian 
 		866 Soviet Union 
 		874 Thai 
 		932 Japanese 
 		936 Simplified Chinese 
 		949 Korean 
 		950 Traditional Chinese 
 		1361 Johab 
 		*/
 
 		controlWords = new Hashtable<String, Integer>();
 		controlWords.put("par", CW_PAR);
 		controlWords.put("pard", CW_PARD);
 		controlWords.put("f", CW_F);
 		controlWords.put("u", CW_U);
 		controlWords.put("plain", CW_PLAIN);
 		controlWords.put("page", CW_PAGE);
 		controlWords.put("lquote", CW_LQUOTE);
 		controlWords.put("rquote", CW_RQUOTE);
 		controlWords.put("cell", CW_CELL);
 		controlWords.put("trowd", CW_TROWD);
 		controlWords.put("tab", CW_TAB);
 		controlWords.put("endash", CW_ENDASH);
 		controlWords.put("emdash", CW_EMDASH);
 		controlWords.put("ldblquote", CW_LDBLQUOTE);
 		controlWords.put("rdblquote", CW_RDBLQUOTE);
 		controlWords.put("v", CW_V);
 		controlWords.put("xe", CW_XE);
 		controlWords.put("cchs", CW_CCHS);
 		controlWords.put("bkmkstart", CW_BKMKSTART);
 		controlWords.put("row",CW_ROW);
 		controlWords.put("uc", CW_UC);
 		controlWords.put("*", CW_SPECIAL);
 		controlWords.put("pict", CW_PICT);
 		controlWords.put(":", CW_INDEXSEP);
 		controlWords.put("shptxt", CW_SHPTXT);
 		controlWords.put("fcharset", CW_FCHARSET);
 		controlWords.put("footnote", CW_FOOTNOTE);
 		controlWords.put("uldb", CW_ULDB);
 		controlWords.put("ul", CW_UL);
 		controlWords.put("bullet", CW_BULLET);
 		controlWords.put("ltrmark", CW_LTRMARK);
 		controlWords.put("rtlmark", CW_RTLMARK);
 		controlWords.put("line",CW_LINE);
 		controlWords.put("zwj", CW_ZWJ);
 		controlWords.put("zwnj", CW_ZWNJ);
 		controlWords.put("cpg", CW_CPG);
 		controlWords.put("ansi", CW_ANSI);
 		controlWords.put("fonttbl", CW_FONTTBL);
 		controlWords.put("stylesheet", CW_STYLESHEET);
 		controlWords.put("colortbl", CW_COLORTBL);
 		controlWords.put("info", CW_INFO);
 		controlWords.put("title", CW_TITLE);
 		controlWords.put("nonshppict", CW_NONSHPPICT);
 		controlWords.put("ansicpg", CW_ANSICPG);
 		controlWords.put("cs", CW_CS);
 		controlWords.put("deleted", CW_DELETED);
 		controlWords.put("bkmkend", CW_BKMKEND);
 		controlWords.put("annotation", CW_ANNOTATION);
 		controlWords.put("mac", CW_MAC);
 		controlWords.put("pc", CW_PC);
 		controlWords.put("pca", CW_PCA);
 		controlWords.put("ftnsep", CW_FTNSEP);
 		controlWords.put("ftnsepc", CW_FTNSEPC);
 		controlWords.put("aftnsep", CW_AFTNSEP);
 		controlWords.put("aftnsepc", CW_AFTNSEPC);
 		controlWords.put("rtf", CW_RTF);
 		controlWords.put("fldinst", CW_FLDINST);
 		controlWords.put("xmlopen", CW_XMLOPEN);
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
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public String getName () {
 		return "okf_tradosrtf";
 	}
 	
 	public String getDisplayName () {
 		return "Trados-Tagged RTF Filter - READING ONLY";
 	}
 
 	public String getMimeType () {
 		return MimeTypeMapper.RTF_MIME_TYPE;
 	}
 	
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.RTF_MIME_TYPE,
 			getClass().getName(),
 			"Trados-Tagged RTF",
 			"Configuration for Trados-tagged RTF files - READING ONLY.",
 			null,
 			".rtf;"));
 		return list;
 	}
 	
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			// No specific mapping for RTF
 		}
 		return encoderManager;
 	}
 	
 	public IParameters getParameters () {
 		return null;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		// Check for cancellation first
 		if ( canceled ) {
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 			hasNext = false;
 		}
 		
 		// Parse next if nothing in the queue
 		if ( queue.size() == 0 ) {
 			parseNext();
 		}
 		
 		// Return the head of the queue
 		if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 			hasNext = false;
 		}
 		return queue.poll();
 	}
 
 	public void parseNext () {
 		ITextUnit textUnit = new TextUnit(String.valueOf(++tuId));
 		if ( !getSegment(textUnit) ) {
 			// Send the end-document event
 			queue.add(new Event(EventType.END_DOCUMENT,
 				new Ending(String.valueOf("ed"))));
 		}
 		else {
 			queue.add(new Event(EventType.TEXT_UNIT, textUnit));
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		try {
 			passedEncoding = input.getEncoding();
 			trgLang = input.getTargetLocale();
 			if ( input.getInputURI() != null ) {
 				docName = input.getInputURI().getPath();
 			}
 			
 			reset(passedEncoding);
 			reader = new BufferedReader(
 				new InputStreamReader(input.getStream(), passedEncoding));
 	
 			StartDocument startDoc = new StartDocument("sd");
 			startDoc.setName(docName);
 			startDoc.setEncoding(passedEncoding, false);
 			startDoc.setLocale(input.getSourceLocale());
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setFilterWriter(createFilterWriter());
 			startDoc.setType(MimeTypeMapper.RTF_MIME_TYPE);
 			startDoc.setMimeType(MimeTypeMapper.RTF_MIME_TYPE);
 			startDoc.setMultilingual(true);
 			startDoc.setLineBreak(Util.LINEBREAK_DOS);
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(e);
 		}
 	}
 
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	public void setParameters (IParameters params) {
 	}
 	
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	private void reset (String defaultEncoding) {
 		close();
 		canceled = false;
 		
 		rtfDetected = false;
 		chCurrent = (char)0;
 		reParse = false;
 		chReParseChar = (char)0;
 		group = 0;
 		skip = 0;
 		byteData = (byte)0;
 		code = 0;
 		word = new StringBuilder();
 		value = 0;
 		chPrevTextChar = (char)0;
 		uChar = (char)0;
 		byteBuffer = ByteBuffer.allocate(10);
 
 		fonts = new Hashtable<Integer, RTFFont>();
 		inFontTable = 0;
 		noReset = 0;
 		internalStyle = 6;
 //		doNotTranslateStyle = 8;
 
 		RTFContext ctx = new RTFContext();
 		ctx.uniCount = 1;
 		ctx.inText = true;
 		ctx.font = 0;
 		this.defaultEncoding = defaultEncoding;
 		ctx.encoding = defaultEncoding;
 		ctxStack = new Stack<RTFContext>();
 		ctxStack.push(ctx);
 		loadEncoding(ctx.encoding);
 
 		hasNext = true;
 		tuId = 0;
 		queue = new LinkedList<Event>();
 	}
 
 	public boolean getSegment (ITextUnit tu) {
 		int nState = 0;
 		String sTmp = "";
 		String sCode = "";
 		int nGrp = 0;
 		int nStyle = 0;
 		int nCode = 0;
 		int startHidden = -1;
 		int endHidden = -1;
 		// groups to skip
 		int fldinst = 0;
 		int xmlopen = 0;
 
 		TextFragment srcFrag = tu.setSourceContent(new TextFragment());
 		TextFragment trgFrag = new TextFragment();
 		TextFragment currentFrag = null;
 		
 		while ( true ) {
 			switch ( getNextToken() ) {
 			case TOKEN_ENDINPUT:
 				if ( !rtfDetected ) {
 					logger.warning("The input does not seem to be an RTF document.");
 				}
 				return false;
 
 			case TOKEN_CHAR:
 				if ( ctxStack.peek().inText ) {
 					if (( startHidden > -1 ) && ( endHidden == -1 )) {
 						endHidden = trgFrag.length();
 					}
 				}
 				else {
 					if ( nStyle > 0 ) {
 						// Get style name
 						sTmp += chCurrent;
 					}
 					// Warn if hidden text is detected in target content
 					if (( nState == 7 ) && ( startHidden < 0 )) {
 						startHidden = trgFrag.length();
 					}
 				}
 				switch ( nState ) {
 				case 0: // Wait for { in {0>
 					if ( chCurrent == '{' ) nState = 1;
 					break;
 
 				case 1: // Wait for 0 in {0>
 					if ( chCurrent == '0' ) nState = 2;
 					else if ( chCurrent != '{' ) nState = 0;
 					break;
 
 				case 2: // Wait for > in {0>
 					if ( chCurrent == '>' ) {
 						nState = 3;
 						currentFrag = srcFrag;
 					}
 					else if ( chCurrent == '{' ) nState = 1; // Is {0{
 					else nState = 0; // Is {0x
 					break;
 
 				case 3: // After {0>, wait for <}n*{>
 					if (( fldinst > 0 ) || ( xmlopen > 0 )) {
 						// Skip
 						break;
 					}
 					if ( chCurrent == '<' ) {
 						sTmp = "";
 						nState = 4;
 					}
 					else {
 						if ( nGrp > 0 ) sCode += chCurrent;
 						else srcFrag.append(chCurrent);
 					}
 					break;
 
 				case 4: // After < in <}n*{>
 					if ( chCurrent == '}' ) {
 						nState = 5;
 					}
 					else if ( chCurrent == '<' ) {
 						if ( nGrp > 0 ) sCode += chCurrent;
 						else srcFrag.append(chCurrent);
 					}
 					else {
 						if ( nGrp > 0 ) {
 							sCode += '<';
 							sCode += chCurrent;
 						}
 						else {
 							srcFrag.append('<');
 							srcFrag.append(chCurrent);
 						}
 						nState = 3;
 					}
 					break;
 
 				case 5: // After <} in <}n*{>
 					if ( chCurrent == '{' ) nState = 6;
 					else if ( !Character.isDigit(chCurrent) ) {
 						if ( nGrp > 0 ) {
 							sCode += "<}";
 							sCode += sTmp;
 							sCode += chCurrent;
 						}
 						else {
 							srcFrag.append("<}");
 							srcFrag.append(sTmp);
 							srcFrag.append(chCurrent);
 						}
 						nState = 3;
 					}
 					else { // Else: number, keep waiting (and preserve text)
 						sTmp += chCurrent;
 					}
 					break;
 
 				case 6: // After <}n*{ in <}n*{>
 					if ( chCurrent == '>' ) {
 						currentFrag = trgFrag; // Starting target text
 						nState = 7;
 					}
 					else {
 						throw new OkapiIllegalFilterOperationException("Expecting: '>' while parsing Trados markup.");
 					}
 					break;
 
 				case 7: // After <}n*{> wait for <0}
 					if ( chCurrent == '<' ) nState = 8;
 					else {
 						if ( nGrp > 0 ) sCode += chCurrent;
 						else {
 							if (( fldinst == 0 ) && ( xmlopen == 0 )) { // If not inside spans we skip
 								trgFrag.append(chCurrent);
 							}
 						}
 					}
 					break;
 
 				case 8: // After < in <0}
 					if ( chCurrent == '0' ) nState = 9;
 					else if ( chCurrent == '<' ) {
 						// 2 sequential <, stay in the state
 						if ( nGrp > 0 ) sCode += chCurrent;
 						else trgFrag.append(chCurrent);
 					}
 					else {
 						if ( nGrp > 0 ) {
 							sCode += '<';
 							sCode += chCurrent;
 						}
 						else {
 							trgFrag.append('<');
 							trgFrag.append(chCurrent);
 						}
 						nState = 7;
 					}
 					break;
 
 				case 9: // After <0 in <0}
 					if ( chCurrent == '}' ) {
 						// Segment is done
 						if ( !trgFrag.isEmpty() ) {
 							tu.setTargetContent(trgLang, trgFrag);
 							if ( startHidden > -1 ) {
 								// Adjust the ending if needed
 								if ( endHidden == -1 ) {
 									endHidden = trgFrag.length();
 								}
 								if ( endHidden-startHidden > 0 ) {
 									logger.warning(String.format("Hidden text detected in target content of text unit '%s'\nTarget=\"%s\"",
 										tu.getId(), trgFrag.toText()));
 									tu.setTargetProperty(trgLang, new Property(PROP_HASHIDDENTEXT,
 										String.format("%d;%d", startHidden, endHidden)));
 								}
 							}
 							
 						}
 						return true;
 					}
 					else if ( chCurrent == '<' ) {
 						// <0< sequence
 						if ( nGrp > 0 ) sCode += "<0";
 						else trgFrag.append("<0");
 						nState = 8;
 					}
 					else {
 						if ( nGrp > 0 ) {
 							sCode += '<';
 							sCode += chCurrent;
 						}
 						else {
 							trgFrag.append('<');
 							trgFrag.append(chCurrent);
 						}
 						nState = 8;
 					}
 					break;
 				}
 				break; // End of case TOKEN_CHAR:
 
 			case TOKEN_STARTGROUP:
 				break;
 
 			case TOKEN_ENDGROUP:
 				if ( nStyle > 0 ) {
 					if ( nStyle < ctxStack.size()+1 ) {
 						// Is it the tw4winInternal style?
 						if ( "tw4winInternal;".compareTo(sTmp) == 0 ) {
 							internalStyle = nCode;
 						}
 //						else if ( "DO_NOT_TRANSLATE;".compareTo(sTmp) == 0 ) {
 //							doNotTranslateStyle = nCode;
 //						}
 					}
 					else {
 						nStyle = 0;
 					}
 					break;
 				}
 				if ( nGrp > 0 ) {
 					if ( nGrp == ctxStack.size()+1 ) {
 						if ( currentFrag != null ) {
 							addInlineCode(currentFrag, sCode);
 						}
 						nGrp = 0;
 					}
 					break;
 				}
 				if ( fldinst > 0 ) {
 					if ( fldinst == ctxStack.size()+1 ) {
 						fldinst = 0;
 					}
 				}
 				if ( xmlopen > 0 ) {
 					if ( xmlopen == ctxStack.size()+1 ) {
 						xmlopen = 0;
 					}
 				}
 				break;
 
 			case TOKEN_CTRLWORD:
 				switch ( code ) {
 				case CW_V:
 					ctxStack.peek().inText = (value == 0);
 					break;
 				case CW_CS:
 					if ( nStyle > 0 ) {
 						nCode = value;
 						sTmp = "";
 						break;
 					}
 					if ( value == internalStyle ) {
 						sCode = "";
 						nGrp = ctxStack.size();
 						break;
 					}
 					// Else: not in source or target
 					break;
 				case CW_STYLESHEET:
 					nStyle = ctxStack.size();
 					break;
 				case CW_FLDINST:
 					fldinst = ctxStack.size();
 					break;
 				case CW_XMLOPEN:
 					xmlopen = ctxStack.size();
 					break;
 				}
 				break; // End of case TOKEN_CTRLWORD:
 
 			default:
 				// Should never get here
 				break;
 			} // End of switch ( getNextToken() )
 		} // End of while
 	}
 	
 	/**
 	 * Tries to guess if an in-line code is an opening/closing XML/HTML tag.
 	 * @param data The text of the in-line code.
 	 * @return The guessed TagType for the given in-line code.
 	 */
 	private void addInlineCode (TextFragment frag,
 		String oriData)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		// Check if we have several possible codes in one run
 		int pos = oriData.indexOf("><");
 		if ( pos > -1 ) {
 			int start = 0;
 			while ( pos > -1 ) {
 				list.add(oriData.substring(start, pos+1));
 				start = pos+1;
 				pos = oriData.indexOf("><", start);
 			}
 			list.add(oriData.substring(start));
 		}
 		else { // Just one entry
 			list.add(oriData);
 		}
 		
 		// Check if previous this is the next part of an incomplete previous code
 		// Do we have a code just before
 		String ct = frag.getCodedText();
 		if ( ct.length() >= 2 ) {
 			// Is the last part of the fragment is a code?
 			if ( TextFragment.isMarker(ct.charAt(ct.length()-2)) ) {
 				Code code = frag.getCode(TextFragment.toIndex(ct.charAt(ct.length()-1)));
 				if ( code.getData().startsWith("<") && !code.getData().endsWith(">") ) {
 					// Start with a start-tag looking char, but does not end like a tag
 					// We assume it's an incomplete code
 					// And add the code to the previous code
 					code.setData(code.getData()+list.get(0));
 					list.remove(0);
 					// Remove that code from the list and move on to process the remain of the list
 				}
 			}
 		}
 		
 		// Process all entries
 		for ( String data : list ) {
 			// By default we assume it's a place-holder
 			String type = null;
 			TagType tagType = TagType.PLACEHOLDER;
 			int last = data.length()-1;
 			int extra = 0;
 			boolean detected = false;
 			
 			// Long enough to be an XML/HTML tag? (3 chars at least)
 			if ( last > 1 ) {
 				
 				// Check S-tagger cases "<:[/]tag[ ...]>"
 				if (( last > 3 ) && data.startsWith("<:") ) {
 					if ( data.charAt(2) == '/' ) extra = 1;
 					// Get end of tag
 					int n = data.indexOf(' ');
 					if ( n == -1 ) n = data.indexOf('>');
 					// Handle the tag name
 					if ( n > -1 ) {
 						String tag = ":"+data.substring(2+extra, n)+":";
 						if ( PAIREDTAGS.contains(tag) ) {
 							if ( extra == 0 ) {
 								tagType = TagType.OPENING;
 								extra = 1;
 							}
 							else { // Extra == 1: closing
 								tagType = TagType.CLOSING;
 								extra = 2;
 							}
 						}
 						detected = true;
 					}
 				}
 				
 				// Continue trying to detect the type if we have still a placeholder
 				// Starts with the proper character?
 				if ( !detected && ( data.charAt(0) == '<' )) {
 					// Ends with the proper character?
 					if ( data.charAt(last) == '>' ) {
 						// Has no more than one tag?
 						if ( data.indexOf('<', 1) == -1 ) {
 							// Is like "</...>", but not "</>", so that's a closing tag
 							if (( data.charAt(1) == '/' ) && ( last > 2 )) {
 								tagType = TagType.CLOSING;
 								extra = 1;
 							}
 							else if ( data.charAt(last-1) != '/' ) {
 								// Is like "<...>, that's an opening tag
 								tagType = TagType.OPENING;
 							}
 							// Else it's likely a empty tag, or it can also be something
 							// non XML/HTML, so it's a place-holder
 						}
 					}
 				}
 			}
 	
 			// Set the type of opening and closing tags
 			if ( tagType != TagType.PLACEHOLDER ) {
 				int n = data.indexOf(' ');
 				if ( n > -1 ) type = data.substring(1+extra, n);
 				else type = data.substring(1+extra, last);
 			}
 			// Add the guessed in-line code
 			frag.append(tagType, type, data);
 		}
 	}
 	
 	/**
 	 * Gets the text content until a specified condition is reached.
 	 * @param cwCode the control word to stop on. Use -1 for either CW_PAR or CW_LINE.
 	 * @param errorCwCode the control word to stop on and return an error. Use 0 for none.
 	 * @return 0: OK, 1: error, 2: stop was due to no more text 
 	 */
 	public int getTextUntil (StringBuilder text,
 		int cwCode,
 		int errorCwCode)
 	{
 		text.setLength(0);
 		//boolean bParOrLine = false;
 
 		while ( true ) {
 			switch ( getNextToken() ) {
 			case TOKEN_ENDINPUT:
 				// Not found, EOF
 				if ( text.length() > 0 ) return 0;
 				// Else: no more text: end-of-input
 				return 2;
 
 			case TOKEN_CHAR:
 				if ( ctxStack.peek().inText ) {
 					text.append(chCurrent);
 				}
 				break;
 
 			case TOKEN_STARTGROUP:
 			case TOKEN_ENDGROUP:
 				break;
 
 			case TOKEN_CTRLWORD:
 				if ( cwCode == -1 ) {
 					if (( code == CW_PAR ) || ( code == CW_LINE )) {
 						//bParOrLine = true;
 						if ( ctxStack.peek().inText ) return 0;
 					}
 				}
 				else if ( code == cwCode ) {
 					if (( code == CW_PAR ) || ( code == CW_LINE )) {
 						//bParOrLine = true;
 					}
 					return 0;
 				}
 				if ( code == errorCwCode ) {
 					return 1;
 				}
 				break;
 			}
 		} // End of while
 	}
 	
 	private int readChar () {
 		try {
 			while ( true ) {
 				int nRes = reader.read();
 				if ( nRes == -1 ) {
 					chCurrent = (char)0;
 					return TOKEN_ENDINPUT;
 				}
 				// Strip out null char in RTF (to read some malformed RTfs)
 				if ( nRes == 0 ) {
 					continue;
 				}
 				// Else get the character
 				chCurrent = (char)nRes;
 				return TOKEN_CHAR;
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private int getNextToken () {
 		int nRes;
 		boolean waitingSecondByte = false;
 
 		while ( true ) {
 			// Get the next character
 			if ( reParse ) { // Re-used last parsed char if available
 				// Use the last parsed character
 				nRes = TOKEN_CHAR;
 				chCurrent = chReParseChar;
 				chReParseChar = (char)0;
 				reParse = false;
 			}
 			else { // Or read a new one
 				nRes = readChar();
 			}
 
 			switch ( nRes ) {
 			case TOKEN_CHAR:
 				switch ( chCurrent ) {
 				case '{':
 					group++;
 					if ( inFontTable > 0 ) inFontTable++;
 					if ( noReset > 0 ) noReset++;
 					ctxStack.push(new RTFContext(ctxStack.peek()));
 					return TOKEN_STARTGROUP;
 
 				case '}':
 					group--;
 					if ( inFontTable > 0 ) inFontTable--;
 					if ( noReset > 0 ) noReset--;
 					if ( ctxStack.size() > 1 ) {
 						ctxStack.pop();
 						loadEncoding(ctxStack.peek().encoding);
 					}
 					return TOKEN_ENDGROUP;
 
 				case '\r':
 				case '\n':
 					// Skip
 					continue;
 
 				case '\\':
 					nRes = parseAfterBackSlash();
 					// -1: it's a 'hh conversion to do, otherwise return it
 					if (( nRes != -1 ) && ( skip == 0 )) return nRes;
 					if ( nRes != -1 ) {
 						// Skip only: has to be uDDD
 						continue;
 					}
 					if ( waitingSecondByte ) {
 						// We were waiting for the second byte
 						waitingSecondByte = false;
 						// Convert the DBCS char into Unicode
 						//TODO: verify that byte-order works
 						byteBuffer.put(1, byteData);
 						CharBuffer charBuf;
 						try {
 							charBuf = currentCSDec.decode(byteBuffer);
 							chCurrent = charBuf.get(0);
 						}
 						catch (CharacterCodingException e) {
							logger.warning(e.getLocalizedMessage());
 							chCurrent = '?';
 						}
 					}
 					else {
 						if ( skip == 0 ) { // Avoid conversion when skipping
 							if ( isLeadByte(byteData) ) {
 								waitingSecondByte = true;
 								// It's a lead byte. Store it and get the next byte
 								byteBuffer.clear();
 								byteBuffer.put(0, byteData);
 								break;
 							}
 							else { // SBCS
 								byteBuffer.clear();
 								byteBuffer.put(0, byteData);
 								CharBuffer charBuf;
 								try {
 									charBuf = currentCSDec.decode(byteBuffer);
 									chCurrent = charBuf.get(0);
 								}
 								catch (CharacterCodingException e) {
									logger.warning(e.getLocalizedMessage());
 									chCurrent = '?';
 								}
 							}
 						}
 					}
 					// Fall thru to process the character
 
 				default:
 					if ( skip > 0 ) {
 						skip--;
 						if ( skip > 0 ) continue;
 						// Else, no more char to skip: fall thru and 
 						// return char of uDDD
 						chCurrent = uChar;
 					}
 					if ( waitingSecondByte ) {
 						// We were waiting for the second byte
 						waitingSecondByte = false;
 						// Convert the DBCS char into Unicode
 						byteBuffer.put(1, (byte)chCurrent);
 						CharBuffer charBuf;
 						try {
 							charBuf = currentCSDec.decode(byteBuffer);
 							chCurrent = charBuf.get(0);
 						}
 						catch (CharacterCodingException e) {
 							logger.warning(e.getLocalizedMessage());
 							chCurrent = '?';
 						}
 					}
 					// Text: chCurrent has the Unicode value
 					chPrevTextChar = chCurrent;
 					return TOKEN_CHAR;
 
 				}
 				break;
 		
 			case TOKEN_ENDINPUT:
 				if ( group > 0 ) {
 					// Missing '{'
 					logger.warning(String.format("Missing '{' = %d.", group));
 				}
 				else if ( group < 0 ) {
 					// Extra '}'
 					logger.warning(String.format("Extra '}' = %d.", group));
 				}
 				return nRes;
 
 			default:
 				return nRes;
 			}
 		}
 	}
 	
 	private boolean isLeadByte (byte byteValue) {
 		switch ( currentDBCSCodepage ) {
 		// Make sure to cast to (byte) to get the signed value!
 		case 932: // Shift-JIS
 			if (( byteValue >= (byte)0x81 ) && ( byteValue <= (byte)0x9F )) return true;
 			if (( byteValue >= (byte)0xE0 ) && ( byteValue <= (byte)0xEE )) return true;
 			if (( byteValue >= (byte)0xFA ) && ( byteValue <= (byte)0xFC )) return true;
 			break;
 		case 936: // Chinese Simplified
 			if (( byteValue >= (byte)0xA1 ) && ( byteValue <= (byte)0xA9 )) return true;
 			if (( byteValue >= (byte)0xB0 ) && ( byteValue <= (byte)0xF7 )) return true;
 			break;
 		case 949: // Korean
 			if (( byteValue >= (byte)0x81 ) && ( byteValue <= (byte)0xC8 )) return true;
 			if (( byteValue >= (byte)0xCA ) && ( byteValue <= (byte)0xFD )) return true;
 			break;
 		case 950: // Chinese Traditional
 			if (( byteValue >= (byte)0xA1 ) && ( byteValue <= (byte)0xC6 )) return true;
 			if (( byteValue >= (byte)0xC9 ) && ( byteValue <= (byte)0xF9 )) return true;
 			break;
 		}
 		// All other encoding: No lead bytes
 		return false;
 	}
 
 	private int parseAfterBackSlash () {
 		boolean inHexa = false;
 		int count = 0;
 		String sBuf = "";
 
 		while ( true ) {
 			if ( readChar() == TOKEN_CHAR ) {
 				if ( inHexa ) {
 					sBuf += chCurrent;
 					if ( (++count) == 2 ) {
 						byteData = (byte)(Integer.parseInt(sBuf, 16)); 
 						return(-1); // Byte to process
 					}
 					continue;
 				}
 				else {
 					switch ( chCurrent ) {
 					case '\'':
 						inHexa = true;
 						break;
 					case '\r':
 					case '\n':
 						// equal to a \par
 						code = CW_PAR;
 						return TOKEN_CTRLWORD;
 					case '\\':
 					case '{':
 					case '}':
 						// Escaped characters
 						return TOKEN_CHAR;
 					case '~':
 					case '|':
 					case '-':
 					case '_':
 					case ':':
 						return parseControlSymbol();
 					case '*':
 					default:
 						return parseControlWord();
 					}
 				}
 			}
 			else {
 				// Unexpected end of input
 				throw new OkapiIllegalFilterOperationException("Unexcpected end of input.");
 			}
 		}
 	}
 	
 	private int parseControlWord () {
 		int nRes;
 		int nState = 0;
 		String sBuf = "";
 		word.setLength(0);
 		word.append(chCurrent);
 		value = 1; // Default value
 		
 		while ( true ) {
 			if ( (nRes = readChar()) == TOKEN_CHAR ) {
 				switch ( nState ) {
 				case 0: // Normal
 					switch ( chCurrent ) {
 					case ' ':
 						return getControlWord();
 					case '\r':
 					case '\n':
 						// According RTF spec 1.6 CR/LF should be ignored
 						//continue;
 						// According RTF spec 1.9 CR/LF should be ignored
 						// ... when "found in clear text segments"
 						return getControlWord();
 					default:
 						// keep adding to the word
 						if ( Character.isLetter(chCurrent) ) {
 							word.append(chCurrent);
 							continue;
 						}
 						// End by a value
 						if ( Character.isDigit(chCurrent) || ( chCurrent == '-' )) {
 							// Value
 							nState = 1;
 							sBuf = String.valueOf(chCurrent);
 							continue;
 						}
 						// End by a no-alpha char
 						reParse = true;
 						chReParseChar = chCurrent;
 						break;
 					} // End switch m_chCurrent
 					return getControlWord();
 					
 				case 1: // Get a value
 					if ( Character.isDigit(chCurrent) ) {
 						sBuf += chCurrent;
 					}
 					else {
 						// End of value
 						if ( chCurrent != ' ' ) {
 							reParse = true;
 							chReParseChar = chCurrent;
 						}
 						value = Integer.parseInt(sBuf);
 						return getControlWord();
 					}
 					break;
 				}
 			}
 			else if ( nRes == TOKEN_ENDINPUT ) {
 				return getControlWord();
 			}
 			else {
 				// Unexpected end of input
 				throw new OkapiIllegalFilterOperationException("Unexcpected end of input.");
 			}
 		}
 	}
 
 	private int getControlWord () {
 		if ( controlWords.containsKey(word.toString()) ) {
 			code = controlWords.get(word.toString());
 		}
 		else {
 			code = -1;
 		}
 		return processControlWord();
 	}
 
 	private int processControlWord () {
 		RTFFont tmpFont;
 		switch ( code ) {
 		case CW_U:
 			// If the value is negative it's a RTF weird typing casting issue:
 			// make it positive by using the complement
 			if ( chCurrent < 0 ) chCurrent = (char)(65536+value);
 			else chCurrent = (char)value;
 			skip = ctxStack.peek().uniCount;
 			uChar = chCurrent; // Preserve char while skipping
 			return TOKEN_CHAR;
 
 		case CW_UC:
 			ctxStack.peek().uniCount = value;
 			break;
 
 		case CW_FONTTBL:
 			inFontTable = 1;
 			ctxStack.peek().inText = false;
 			break;
 
 		case CW_FCHARSET:
 			if ( inFontTable > 0 ) {
 				int nFont = ctxStack.peek().font;
 				if ( fonts.containsKey(nFont) ) {
 					tmpFont = fonts.get(nFont);
 					if ( setCharset0ToDefault && ( value == 0 )) {
 						tmpFont.encoding = defaultEncoding;
 					}
 					else {
 						// Else: Look at table
 						if ( winCharsets.containsKey(value) ) {
 							tmpFont.encoding = winCharsets.get(value);
 						}
 						else {
 							tmpFont.encoding = defaultEncoding;
 						}
 					}
 				}
 				// Else: should not happen (\fcharset is always after \f)
 			}
 			// Else: should not happen (\fcharset is always in font table)
 			break;
 
 		case CW_PARD: // Reset properties
 			// TODO?
 			break;
 			
 		case CW_RTF:
 			rtfDetected = true;
 			break;
 
 		case CW_F:
 			if ( inFontTable > 0 ) {
 				// Define font
 				ctxStack.peek().font = value;
 				if ( !fonts.containsKey(value) ) {
 					tmpFont = new RTFFont();
 					fonts.put(value, tmpFont);
 				}
 			}
 			else {
 				// Switch font
 				ctxStack.peek().font = value;
 				// Search font definition
 				if ( fonts.containsKey(value) ) {
 					// Switch to the encoding of the font
 					tmpFont = fonts.get(value);
 					// Check if the encoding is set, if not, use the default encoding
 					if ( tmpFont.encoding == null ) {
 						tmpFont.encoding = defaultEncoding;
 					}
 					// Set the current encoding in the stack
 					ctxStack.peek().encoding = tmpFont.encoding;
 					// Load the new encoding if needed
 					loadEncoding(tmpFont.encoding);
 				}
 				else {
 					// Font undefined: Switch to the default encoding
 					logger.warning(String.format("The font '%d' is undefined. The encoding '%s' is used by default instead.", 
 						value, defaultEncoding));
 					loadEncoding(defaultEncoding);
 				}
 			}
 			break;
 
 		case CW_TAB:
 			chCurrent = '\t';
 			return TOKEN_CHAR;
 
 		case CW_BULLET:
 			chCurrent = '\u2022';
 			return TOKEN_CHAR;
 
 		case CW_LQUOTE:
 			chCurrent = '\u2018';
 			return TOKEN_CHAR;
 
 		case CW_RQUOTE:
 			chCurrent = '\u2019';
 			return TOKEN_CHAR;
 
 		case CW_LDBLQUOTE:
 			chCurrent = '\u201c';
 			return TOKEN_CHAR;
 
 		case CW_RDBLQUOTE:
 			chCurrent = '\u201d';
 			return TOKEN_CHAR;
 
 		case CW_ENDASH:
 			chCurrent = '\u2013';
 			return TOKEN_CHAR;
 
 		case CW_EMDASH:
 			chCurrent = '\u2014';
 			return TOKEN_CHAR;
 
 		case CW_ZWJ:
 			chCurrent = '\u200d';
 			return TOKEN_CHAR;
 
 		case CW_ZWNJ:
 			chCurrent = '\u200c';
 			return TOKEN_CHAR;
 
 		case CW_LTRMARK:
 			chCurrent = '\u200e';
 			return TOKEN_CHAR;
 
 		case CW_RTLMARK:
 			chCurrent = '\u200f';
 			return TOKEN_CHAR;
 
 		case CW_CCHS:
 			if ( setCharset0ToDefault && ( value == 0 )) {
 				ctxStack.peek().encoding = defaultEncoding;
 			}
 			else {
 				// Else: Look up
 				if ( winCharsets.containsKey(value) ) {
 					ctxStack.peek().encoding = winCharsets.get(value);
 				}
 				else {
 					ctxStack.peek().encoding = defaultEncoding;
 				}
 			}
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_CPG:
 		case CW_ANSICPG:
 			String name = winCodepages.get(value);
 			if ( name == null ) {
 				logger.warning(String.format("The codepage '%d' is undefined. The encoding '%s' is used by default instead.",
 					value, defaultEncoding));
 				ctxStack.peek().encoding = defaultEncoding;
 			}
 			else {
 				ctxStack.peek().encoding = name;
 			}
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_ANSI:
 			if ( setCharset0ToDefault ) {
 				ctxStack.peek().encoding = defaultEncoding;
 			}
 			else {
 				ctxStack.peek().encoding = "windows-1252";
 			}
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_MAC:
 			ctxStack.peek().encoding = "MacRoman";
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_PC:
 			ctxStack.peek().encoding = "ibm437";
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_PCA:
 			ctxStack.peek().encoding = "ibm850";
 			loadEncoding(ctxStack.peek().encoding);
 			break;
 
 		case CW_FOOTNOTE:
 			if ( "#+!>@".indexOf(chPrevTextChar) != -1 ) {
 				ctxStack.peek().inText = false;
 			}
 			break;
 
 		case CW_V:
 			ctxStack.peek().inText = (value == 0);
 			break;
 
 		case CW_XE:
 		case CW_SHPTXT:
 			ctxStack.peek().inText = true;
 			break;
 
 		case CW_SPECIAL:
 		case CW_PICT:
 		case CW_STYLESHEET:
 		case CW_COLORTBL:
 		case CW_INFO:
 			ctxStack.peek().inText = false;
 			break;
 		case CW_FTNSEP:
 		case CW_FTNSEPC:
 		case CW_AFTNSEP:
 		case CW_AFTNSEPC:
 			noReset = 1;
 			ctxStack.peek().inText = false;
 			break;
 
 		case CW_ANNOTATION:
 			noReset = 1;
 			ctxStack.peek().inText = false;
 			break;
 
 		case CW_DELETED:
 			ctxStack.peek().inText = (value == 0);
 			break;
 
 		case CW_PLAIN:
 			// Reset to default
 			if ( noReset == 0 ) {
 				ctxStack.peek().inText = true;
 			}
 			break;
 		}
 
 		return TOKEN_CTRLWORD;
 	}
 
 	private void loadEncoding (String encodingName) {
 		if ( currentCSDec != null ) {
 			if ( currentCSName.compareToIgnoreCase(encodingName) == 0 )
 				return; // Same encoding: No change needed
 		}
 		// check for special cases
 		if (( "symbol".compareTo(encodingName) == 0 )
 			|| ( "oem".compareTo(encodingName) == 0 )
 			|| ( "arabic-user".compareTo(encodingName) == 0 )
 			|| ( "hebrew-user".compareTo(encodingName) == 0 ))
 		{
 			logger.warning(String.format("The encoding '%s' is unsupported. The encoding '%s' is used by default instead.", 
 				encodingName, defaultEncoding));
 			encodingName = defaultEncoding;
 		}
 		// Load the new encoding
 		currentCSDec = Charset.forName(encodingName).newDecoder();
 		currentCSName = encodingName.toLowerCase();
 		if ( currentCSName.compareTo("shift_jis") == 0 ) {
 			currentDBCSCodepage = 932;
 		}
 		else if ( currentCSName.compareTo("windows949") == 0 ) {
 			currentDBCSCodepage = 949;
 		}
 		else if ( currentCSName.compareTo("gbk") == 0 ) {
 			currentDBCSCodepage = 936;
 		}
 		else if ( currentCSName.compareTo("windows-936") == 0 ) {
 			currentDBCSCodepage = 936;
 		}
 		else if ( currentCSName.compareTo("big5") == 0 ) {
 			currentDBCSCodepage = 950;
 		}
 		else {
 			currentDBCSCodepage = 0; // Not DBCS
 		}
 	}
 
 	private int parseControlSymbol () {
 		switch ( chCurrent ) {
 		case '~': // Non-breaking space
 			chCurrent = '\u00a0';
 			return TOKEN_CHAR;
 		case '_': // Non-Breaking hyphen
 			chCurrent = '\u2011';
 			return TOKEN_CHAR;
 		case '-': // Non-required hyphen (should we convert to Soft-hyphen?? U+00AD?)
 		case '|': // Formula character
 		case ':': // Sub-entry in index entry
 			return TOKEN_CTRLWORD;
 		default: // Should not get here
 			throw new OkapiIllegalFilterOperationException(String.format("Unknown control symbol '%c'", chCurrent));
 		}
 	}
 
 }
