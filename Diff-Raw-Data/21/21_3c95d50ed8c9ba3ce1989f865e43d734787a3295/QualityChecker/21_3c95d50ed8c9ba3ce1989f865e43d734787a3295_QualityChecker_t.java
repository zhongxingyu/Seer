 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.lib.verification;
 
 import java.io.File;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.lib.terminology.simpletb.SimpleTB;
 
 class QualityChecker {
 
 	private LocaleId srcLoc;
 	private LocaleId trgLoc;
 	private List<PatternItem> patterns;
 	private LanguageToolConnector ltConn;
 	private TermChecker termChecker;
 	private Parameters params;
 	private List<Issue> issues;
 	private URI currentDocId;
 	private List<String> sigList;
 	private Pattern patDoubledWords;
 	private String doubledWordExceptions;
 	private CharsetEncoder encoder1;
 	private CharsetEncoder encoder2;
 	private Pattern extraCharsAllowed;
 	private Pattern corruption;
 	private boolean monolingual;
 	private Pattern itsAllowedChars;
 	private String itsAllowedCharsPattern;
 	
 	private final static Pattern WORDCHARS = Pattern.compile("[\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}]");
 
 	void startProcess (LocaleId sourceLocale,
 		LocaleId targetLocale,
 		Parameters params,
 		List<Issue> issues)
 	{
 		this.srcLoc = sourceLocale;
 		this.trgLoc = targetLocale;
 		this.params = params;
 		this.issues = issues;
 
 		// Compile the patterns
 		patterns = params.getPatterns();
 		for ( PatternItem item : patterns ) {
 			if ( item.enabled ) {
 				item.compile();
 			}
 		}
 
 		ltConn = null;
 		if ( params.getCheckWithLT() ) {
 			ltConn = new LanguageToolConnector();
 			ltConn.initialize(targetLocale, sourceLocale, params.getServerURL(), params.translateLTMsg,
 				params.ltBilingualMode, params.ltTranslationSource, params.ltTranslationTarget,
 				params.ltTranslationServiceKey);
 		}
 
 		// Expression for finding doubled words
 		// The expression: "\\b(\\w+)\\s+\\1\\b" does not work for extended chars (\w and \s are ASCII based)
 		// We have to use the Unicode equivalents
 		patDoubledWords = null;
 		if ( params.getDoubledWord() ) {
 			patDoubledWords = Pattern.compile("\\b([\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}]+)[\\t\\n\\f\\r\\p{Z}]+\\1\\b",
 				Pattern.CASE_INSENSITIVE);
 			// Construct the string of doubled-words that are not errors
 			// The working patter is the list like this: ";word1;word2;word3;"
 			doubledWordExceptions = ";"+params.getDoubledWordExceptions().toLowerCase()+";";
 		}
 		
 		// Pattern for corrupted characters
 		corruption = null;
 		if ( params.getCorruptedCharacters() ) {
 			// Some of the most frequent patterns of corrupted characters
 			corruption = Pattern.compile("\\u00C3[\\u00A4-\\u00B6]"
 				+ "|\\u00C3\\u201E"
 				+ "|\\u00C3\\u2026"
 				+ "|\\u00C3\\u2013"
 			);
 		}
 		
 		// Characters check
 		encoder1 = null;
 		extraCharsAllowed = null;
 		if ( params.getCheckCharacters() ) {
 			// Encoding
 			String charsetName = params.charset;
 			if ( !Util.isEmpty(charsetName) ) {
 				encoder1 = Charset.forName(charsetName).newEncoder();
 			}
 			// Extra characters allowed
 			if ( !params.getExtraCharsAllowed().isEmpty() ) {
 				extraCharsAllowed = Pattern.compile(params.getExtraCharsAllowed());
 			}
 		}
 		
 		encoder2 = null;
 		itsAllowedChars = null;
 		itsAllowedCharsPattern = "\u0000";
 
 		// Terminology check
 		termChecker = null;
 		if ( params.getCheckTerms() ) {
 			// Direct use of SimpleTB for now
 			termChecker = new TermChecker();
 			SimpleTB ta = new SimpleTB(srcLoc, trgLoc);
 			ta.guessAndImport(new File(params.getTermsPath()));
 			termChecker.initialize(ta, srcLoc, trgLoc, params.getStringMode(), params.getBetweenCodes());
 		}
 	}
 
 	void processStartDocument (StartDocument sd,
 		List<String> sigList)
 	{
 		currentDocId = (new File(sd.getName())).toURI();
 		this.sigList = sigList;
 		monolingual = !sd.isMultilingual();
 	}
 
 	/**
 	 * Indicates if we have at least one character that is part of the character set for a "word".
 	 * digits are considered part of a "word". 
 	 * @param frag the text fragment to look at.
 	 * @return true if a "word" is detected.
 	 */
 	private boolean hasMeaningfullText (TextFragment frag) {
 		return WORDCHARS.matcher(frag.getCodedText()).find();
 	}
 	
 	void processTextUnit (ITextUnit tu) {
 		// Skip non-translatable entries
 		if ( !tu.isTranslatable() ) return;
 		
 		// Get the containers
 		TextContainer srcCont = tu.getSource();
 		TextContainer trgCont = tu.getTarget(trgLoc);
 		
 		// Check ITS Storage size for source
 		if ( params.getCheckStorageSize() ) {
 			checkStorageSize(tu, srcCont, true);
 		}
 		if ( params.getCheckAllowedCharacters() ) {
 			checkITSAllowedChars(tu, srcCont, true);
 		}
 		
 		// Check if we have a target
 		if ( trgCont == null ) {
 			if ( !monolingual ) { // No report as error for monolingual files
 				// No translation available
 				reportIssue(IssueType.MISSING_TARGETTU, tu, null,
 					"Missing translation.",
 					0, -1, 0, -1, Issue.SEVERITY_HIGH, srcCont.toString(), "", null);
 			}
 			return;
 		}
 		
 		// Check ITS Storage size for target
 		if ( params.getCheckStorageSize() ) {
 			checkStorageSize(tu, trgCont, false);
 		}
 		if ( params.getCheckAllowedCharacters() ) {
			checkITSAllowedChars(tu, trgCont, false);
 		}
 
 		// Skip non-approved entries if requested
 		if ( params.getScope() != Parameters.SCOPE_ALL ) {
 			Property prop = trgCont.getProperty(Property.APPROVED);
 			if (( prop != null ) && prop.getValue().equals("yes") ) { // Approved
 				if ( params.getScope() == Parameters.SCOPE_NOTAPPROVEDONLY ) return;
 			}
 			else { // Not approved
 				if ( params.getScope() == Parameters.SCOPE_APPROVEDONLY ) return;
 			}
 		}
 
 		ISegments srcSegs = srcCont.getSegments();
 		ISegments trgSegs = trgCont.getSegments();
 		
 		// Check hidden text (e.g. RTF)
 		Property prop = trgCont.getProperty("hashiddentext");
 		if ( prop != null ) {
 			// There is a hidden section
 			Scanner scan = new Scanner(prop.getValue()).useDelimiter(";");
 			TextFragment tf = trgCont.getUnSegmentedContentCopy();
 			int start = fromFragmentToString(tf, scan.nextInt());
 			int end = fromFragmentToString(tf, scan.nextInt());
 			reportIssue(IssueType.SUSPECT_PATTERN, tu, null,
 				"Target content has at least one hidden part.",
 				0, -1, start, end, Issue.SEVERITY_HIGH, srcCont.toString(), trgCont.toString(), null);
 		}
 		
 		for ( Segment srcSeg : srcSegs ) {
 			Segment trgSeg = trgSegs.get(srcSeg.getId());
 			if ( trgSeg == null ) {
 				reportIssue(IssueType.MISSING_TARGETSEG, tu, srcSeg.getId(),
 					"The source segment has no corresponding target segment.",
 					0, -1, 0, -1, Issue.SEVERITY_HIGH, srcSeg.toString(), "", null);
 				continue; // Cannot go further for that segment
 			}
 			
 			// Check for empty target, if requested
 			if ( params.getEmptyTarget() ) {
 				if ( trgSeg.text.isEmpty() && !srcSeg.text.isEmpty() ) {
 					reportIssue(IssueType.EMPTY_TARGETSEG, tu, srcSeg.getId(),
 						"The target segment is empty, but its source is not empty.",
 						0, -1, 0, -1, Issue.SEVERITY_HIGH, srcSeg.toString(), "", null);
 					continue; // No need to check more if it's empty
 				}
 			}
 			// Check for empty source when target is not empty, if requested
 			if ( params.getEmptySource() ) {
 				if ( srcSeg.text.isEmpty() && !trgSeg.text.isEmpty() ) {
 					reportIssue(IssueType.EMPTY_SOURCESEG, tu, srcSeg.getId(),
 						"The target segment is not empty, but its source is empty.",
 						0, -1, 0, -1, Issue.SEVERITY_HIGH, srcSeg.toString(), "", null);
 					continue; // No need to check more if the source is empty
 				}
 			}
 			
 			// Check code differences, if requested
 			if ( params.getCodeDifference() ) {
 				checkInlineCodes(srcSeg, trgSeg, tu);
 			}
 
 			// Check for target is the same as source, if requested
 			if ( params.getTargetSameAsSource() ) {
 				if ( params.getTargetSameAsSourceForSameLanguage() || !srcLoc.sameLanguageAs(trgLoc) ) {
 					if ( hasMeaningfullText(srcSeg.text) ) {
 						if ( srcSeg.text.compareTo(trgSeg.text, params.getTargetSameAsSourceWithCodes()) == 0 ) {
 							// Is the string of the cases where target should be the same? (URL, etc.)
 							boolean warn = true;
 							if ( patterns != null ) {
 								for ( PatternItem item : patterns ) {
 									String ctext = srcSeg.text.getCodedText();
 									if ( item.enabled && item.target.equals(PatternItem.SAME) ) {
 										Matcher m = item.getSourcePattern().matcher(ctext);
 										if ( m.find() ) {
 											warn = !ctext.equals(m.group());
 											break;
 										}
 									}
 								}
 							}
 							if ( warn ) {
 								reportIssue(IssueType.TARGET_SAME_AS_SOURCE, tu, srcSeg.getId(),
 									"Translation is the same as the source.",
 									0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(), null);
 							}
 						}
 					}
 				}
 			}
 			
 			// Check for patterns, if requested
 			if ( params.getCheckPatterns() ) {
 				checkPatterns(srcSeg, trgSeg, tu);
 			}
 			
 			if ( termChecker != null ) {
 				if ( termChecker.verify(currentDocId, tu, srcSeg, trgSeg) > 0 ) {
 					for ( Issue issue : termChecker.getIssues() ) {
 						reportIssue(issue.issueType, tu, issue.segId, issue.message, issue.srcStart, issue.srcEnd,
 							issue.trgStart, issue.trgEnd, issue.severity, srcSeg.toString(), trgSeg.toString(), null);
 					}
 				}
 			}
 			
 			// Check segment length
 			if ( params.getCheckMaxCharLength() || params.getCheckMinCharLength() || params.getCheckAbsoluteMaxCharLength() ) {
 				checkLengths(srcSeg, trgSeg, tu);
 			}
 			
 			// Check all suspect patterns
 			checkSuspectPatterns(srcSeg, trgSeg, tu);
 			
 			// Run a check with LanguageTool connector
 			if ( ltConn != null ) {
 				if ( ltConn.checkSegment(currentDocId, srcSeg, trgSeg, tu) > 0 ) {
 					for ( Issue issue : ltConn.getIssues() ) {
 						reportIssue(issue.issueType, tu, issue.segId, issue.message, issue.srcStart, issue.srcEnd,
 							issue.trgStart, issue.trgEnd, issue.severity, srcSeg.toString(), trgSeg.toString(), null);
 						if ( issue.srcEnd == -99 ) {
 							// Special marker indicating a server error
 							ltConn = null; // Do not check it again until next re-processing
 						}
 					}
 				}
 			}
 		
 		}
 
 		// Check for orphan target segments
 		for ( Segment trgSeg : trgSegs ) {
 			Segment srcSeg = srcSegs.get(trgSeg.getId());
 			if ( srcSeg == null ) {
 				reportIssue(IssueType.EXTRA_TARGETSEG, tu, trgSeg.getId(),
 					String.format("Extra target segment (id=%s).", trgSeg.getId()),
 					0, -1, 0, -1, Issue.SEVERITY_HIGH, "", trgSeg.toString(), null);
 				continue; // Cannot go further for that segment
 			}
 		}
 		
 		String srcOri = null;
 		if ( srcCont.contentIsOneSegment() ) {
 			srcOri = srcCont.toString();
 		}
 		else {
 			srcOri = srcCont.getUnSegmentedContentCopy().toText();
 		}
 		
 		String trgOri = null;
 		if ( trgCont.contentIsOneSegment() ) {
 			trgOri = trgCont.toString();
 		}
 		else {
 			trgOri = trgCont.getUnSegmentedContentCopy().toText();
 		}
 
 		if ( params.getCorruptedCharacters() ) {
 			checkCorruptedCharacters(srcOri, trgOri, tu);
 		}
 		
 		checkWhiteSpaces(srcOri, trgOri, tu);
 		
 		if ( params.getCheckCharacters() ) {
 			checkCharacters(srcOri, trgOri, tu);
 		}
 	}
 	
 	private void checkCharacters (String srcOri,
 		String trgOri,
 		ITextUnit tu)
 	{
 		StringBuilder badChars = new StringBuilder();
 		int pos = -1;
 		int badChar = 0;
 		int count = 0;
 		
 		for ( int i=0; i<trgOri.length(); i++ ) {
 			char ch = trgOri.charAt(i);
 			
 			if ( encoder1 != null ) {
 				if ( encoder1.canEncode(ch) ) {
 					continue; // Allowed, move to the next character
 				}
 				else { // Not included in the target charset
 					// Check if it is included in the extra characters list
 					if ( extraCharsAllowed != null ) {
 						Matcher m = extraCharsAllowed.matcher(trgOri.subSequence(i, i+1));
 						if ( m.find() ) {
 							// Part of the extra character list: it's OK
 							continue; // Move to the next character
 						}
 						// Else: not allowed: fall thru
 					}
 				}
 			}
 			else { // Not charset defined, try just the extra characters list
 				if ( extraCharsAllowed != null ) {
 					Matcher m = extraCharsAllowed.matcher(trgOri.subSequence(i, i+1));
 					if ( m.find() ) {
 						// Part of the extra character list: it's OK
 						continue; // Move to the next character
 					}
 					// Else: not allowed: fall thru
 				}
 				// Else: not in charset, nor in extra characters list: not allowed
 			}
 		
 			// The character is not allowed: add the error
 			if ( ++count > 1 ) {
 				if ( badChars.indexOf(String.valueOf(ch)) == -1 ) { 
 					badChars.append(ch);
 				}
 			}
 			else {
 				pos = i;
 				badChar = ch;
 			}
 		}
 
 		// Do we have one or more errors?
 		if ( pos > -1 ) {
 			if ( count > 1 ) {
 				reportIssue(IssueType.ALLOWED_CHARACTERS, tu, null,
 					String.format("The character '%c' (U+%04X) is not allowed in the target text."
 						+ " Other forbidden characters found: ", badChar, (int)badChar)+badChars.toString(),
 						0, -1, pos, pos+1, Issue.SEVERITY_MEDIUM, srcOri, trgOri, null);
 			}
 			else {
 				reportIssue(IssueType.ALLOWED_CHARACTERS, tu, null,
 					String.format("The character '%c' (U+%04X) is not allowed in the target text.", badChar, (int)badChar),
 					0, -1, pos, pos+1, Issue.SEVERITY_MEDIUM, srcOri, trgOri, null);
 			}
 		}
 		
 	}
 	
 	// Create a copy of the codes and strip out any that has empty data.
 	// They correspond to process-only codes like <df> in TTX or <mrk> in XLIFF
 	private ArrayList<Code> stripNoiseCodes (Segment seg) {
 		ArrayList<Code> list = new ArrayList<Code>(seg.text.getCodes());
 		Iterator<Code> iter = list.iterator();
 		while ( iter.hasNext() ) {
 			Code code = iter.next();
 			if ( params.getTypesToIgnore().indexOf(code.getType()+";") != -1 ) {
 				iter.remove();
 			}
 		}
 		return list;
 	}
 	
 	private String buildCodeList (List<Code> list) {
 		StringBuilder tmp = new StringBuilder();
 		for ( Code code : list ) {
 			if ( tmp.length() > 0 ) tmp.append(", ");
 			if ( code.getData().isEmpty() ) {
 				tmp.append(code.getOuterData().replaceAll("></x>", "/>"));
 			}
 			else { // Show the content
 				tmp.append("\""+code.getData()+"\"");
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private void checkInlineCodes (Segment srcSeg,
 		Segment trgSeg,
 		ITextUnit tu)
 	{
 		ArrayList<Code> srcList = stripNoiseCodes(srcSeg);
 		ArrayList<Code> trgList = stripNoiseCodes(trgSeg);
 
 		// If no codes: don't check
 		if (( srcList.size() == 0 ) && ( trgList.size() == 0 )) return;
 		
 		// Prepare the verification of the open-close sequence
 		String srcOC = buildOpenCloseSequence(srcList);
 		String trgOC = buildOpenCloseSequence(trgList);
 		boolean checkOC = true;
 
 		// Check codes missing in target
 		Iterator<Code> srcIter = srcList.iterator();
 		while ( srcIter.hasNext() ) {
 			Code srcCode = srcIter.next();
 			Iterator<Code> trgIter = trgList.iterator();
 			while ( trgIter.hasNext() ) {
 				Code trgCode = trgIter.next();
 				if ( trgCode.getData().isEmpty() && srcCode.getData().isEmpty() ) {
 					if (( trgCode.getId() == srcCode.getId() ) && trgCode.getType().equals(srcCode.getType()) ) {
 						// Found: remove them from lists
 						trgIter.remove();
 						srcIter.remove();
 						break;
 					}
 				}
 				else if ( trgCode.getData().equals(srcCode.getData()) ) {
 					// Found: remove them from lists
 					trgIter.remove();
 					srcIter.remove();
 					break;
 				}
 			}
 		}
 
 		//--- Missing codes
 		// Check if any of the missing code is one of the code allowed to be missing
 		if ( !srcList.isEmpty() ) {
 			Iterator<Code> iter = srcList.iterator();
 			while ( iter.hasNext() ) {
 				if ( params.missingCodesAllowed.contains(iter.next().getData()) ) {
 					iter.remove();
 				}
 			}
 		}
 		// What is left in the source list are the codes missing in the target
 		if ( !srcList.isEmpty() ) {
 			reportIssue(IssueType.MISSING_CODE, tu, srcSeg.getId(),
 				"Missing codes in the target: "+buildCodeList(srcList),
 				0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(),
 				srcList);
 			checkOC = false;
 		}
 		
 		//--- Extra codes
 		// Check if any of the extra code is one of the code allowed to be extra
 		if ( !trgList.isEmpty() ) {
 			Iterator<Code> iter = trgList.iterator();
 			while ( iter.hasNext() ) {
 				if ( params.extraCodesAllowed.contains(iter.next().getData()) ) {
 					iter.remove();
 				}
 			}
 		}
 		// What is left in the target list are the extra codes in the target
 		if ( !trgList.isEmpty() ) {
 			reportIssue(IssueType.EXTRA_CODE, tu, srcSeg.getId(),
 				"Extra codes in the target: "+buildCodeList(trgList),
 				0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(),
 				trgList);
 			checkOC = false;
 		}
 		
 		// Check sequence issue in open-close codes
 		// This is checked only if we did not found already an error
 		if ( checkOC ) {
 			int j = 0;
 			boolean done = false;
 			for ( int i=0; i<srcOC.length(); i++ ) {
 				if ( srcOC.charAt(i) == 'p' ) continue;
 				// Else it's 'o' or 'c'
 				while ( true ) {
 					if ( trgOC.length() <= j ) {
 						// No more code of this type
 						reportIssue(IssueType.SUSPECT_CODE, tu, srcSeg.getId(),
 							"Suspect sequence of opening and closing target codes.",
 							0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(),
 							trgList);
 						done = true;
 						break;
 					}
 					// If it's a placeholder, move to the next code
 					if ( trgOC.charAt(j) == 'p' ) {
 						j++;
 						continue;
 					}
 					// Else: it's a 'o' or 'c'
 					if ( trgOC.charAt(j) != srcOC.charAt(i) ) {
 						// Error in sequence
 						reportIssue(IssueType.SUSPECT_CODE, tu, srcSeg.getId(),
 							String.format("Suspect sequence of opening and closing codes in the target (code %d).", i+1),
 							0, -1, 0, -1, Issue.SEVERITY_MEDIUM, srcSeg.toString(), trgSeg.toString(),
 							trgList);
 						done = true;
 						break;
 					}
 					j++;
 					break; // This code has been checekd
 				}
 				if ( done ) break;
 			}
 		}
 		
 	}
 	
 	private String buildOpenCloseSequence (ArrayList<Code> list) {
 		StringBuilder sb = new StringBuilder();
 		for ( Code code : list ) {
 			switch ( code.getTagType() ) {
 			case OPENING:
 				sb.append("o");
 				break;
 			case CLOSING:
 				sb.append("c");
 				break;
 			case PLACEHOLDER:
 				if ( true ) {
 					String tmp = code.getData();
 					char ch = 'p';
 					if ( !Util.isEmpty(tmp) && params.getGuessOpenClose() ) {
 						if ( tmp.startsWith("</") ) ch = 'c';
 						else if ( tmp.startsWith("<") ) ch = 'o';
 						// Make sure the open is not an empty
 						if ( tmp.endsWith("/>") ) ch = 'p';
 					}
 					// Now add only if it's an open or close
 					sb.append(ch);
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	private void checkCorruptedCharacters (String srcOri,
 		String trgOri,
 		ITextUnit tu)
 	{
 		Matcher m = corruption.matcher(trgOri);
 		if ( m.find() ) { // Getting one match is enough
 			reportIssue(IssueType.SUSPECT_PATTERN, tu, null,
 				String.format("Possible corrupted characters in the target (for example: \"%s\").", m.group()),
 				0, -1, m.start(), m.end(), Issue.SEVERITY_HIGH, srcOri, trgOri, null);
 		}
 	}
 
 	private boolean isSpaceWeCareAbout(char c) {
 		return Character.isWhitespace(c) || Character.isSpaceChar(c);
 	}
 	
 	private void checkWhiteSpaces (String srcOri,
 		String trgOri,
 		ITextUnit tu)
 	{
 		// Check for leading whitespaces
 		if ( params.getLeadingWS() ) {
 			
 			// Missing ones
 			for ( int i=0; i<srcOri.length(); i++ ) {
 				if ( isSpaceWeCareAbout(srcOri.charAt(i)) ) {
 					if ( srcOri.length() > i ) {
 						if ( trgOri.charAt(i) != srcOri.charAt(i) ) {
 							reportIssue(IssueType.MISSINGORDIFF_LEADINGWS, tu, null,
 								String.format("Missing or different leading white space at position %d.", i),
 								i, i+1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 							break;
 						}
 					}
 					else {
 						reportIssue(IssueType.MISSING_LEADINGWS, tu, null,
 							String.format("Missing leading white space at position %d.", i),
 							i, i+1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 					}
 				}
 				else break;
 			}
 
 			// Extra ones
 			for ( int i=0; i<trgOri.length(); i++ ) {
 				if ( isSpaceWeCareAbout(trgOri.charAt(i)) ) {
 					if ( srcOri.length() > i ) {
 						if ( srcOri.charAt(i) != trgOri.charAt(i) ) {
 							reportIssue(IssueType.EXTRAORDIFF_LEADINGWS, tu, null,
 								String.format("Extra or different leading white space at position %d.", i),
 								0, -1, i, i+1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 							break;
 						}
 					}
 					else {
 						reportIssue(IssueType.EXTRA_LEADINGWS, tu, null,
 							String.format("Extra leading white space at position %d.", i),
 							0, -1, i, i+1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 					}
 				}
 				else break;
 			}
 		}
 		
 		// Check for trailing whitespaces
 		if ( params.getTrailingWS() ) {
 
 			// Missing ones
 			int j = trgOri.length()-1;
 			for ( int i=srcOri.length()-1; i>=0; i-- ) {
 				if ( isSpaceWeCareAbout(srcOri.charAt(i)) ) {
 					if ( j >= 0 ) {
 						if ( trgOri.charAt(j) != srcOri.charAt(i) ) {
 							reportIssue(IssueType.MISSINGORDIFF_TRAILINGWS, tu, null,
 								String.format("Missing or different trailing white space at position %d", i),
 								i, i+1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 							break;
 						}
 					}
 					else {
 						reportIssue(IssueType.MISSING_TRAILINGWS, tu, null,
 							String.format("Missing trailing white space at position %d.", i),
 							i, i+1, 0, -1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 					}
 				}
 				else break;
 				j--;
 			}
 
 			// Extra ones
 			j = srcOri.length()-1;
 			for ( int i=trgOri.length()-1; i>=0; i-- ) {
 				if ( isSpaceWeCareAbout(trgOri.charAt(i)) ) {
 					if ( j >= 0 ) {
 						if ( srcOri.charAt(j) != trgOri.charAt(i) ) {
 							reportIssue(IssueType.EXTRAORDIFF_TRAILINGWS, tu, null,
 								String.format("Extra or different trailing white space at position %d.", i),
 								0, -1, i, i+1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 							break;
 						}
 					}
 					else {
 						reportIssue(IssueType.EXTRA_TRAILINGWS, tu, null,
 							String.format("Extra white trailing space at position %d.", i),
 							0, -1, i, i+1, Issue.SEVERITY_LOW, srcOri, trgOri, null);
 					}
 				}
 				else break;
 				j--;
 			}
 		}
 
 	}
 
 	private void checkLengths (Segment srcSeg,
 		Segment trgSeg,
 		ITextUnit tu)
 	{
 		// Strip inline code markers to look at text only
 		int srcLen = TextUnitUtil.getText(srcSeg.text, null).length();
 		int trgLen = TextUnitUtil.getText(trgSeg.text, null).length();
 		int n;
 
 		// Check absolute character length
 		if ( params.getCheckAbsoluteMaxCharLength() ) {
 			if ( trgLen > params.getAbsoluteMaxCharLength() ) {
 				n = trgLen-params.getAbsoluteMaxCharLength();
 				reportIssue(IssueType.TARGET_LENGTH, tu, srcSeg.getId(),
 					String.format("The target is longer than %d (by %d).", params.getAbsoluteMaxCharLength(), n),
 					0, -1, params.getAbsoluteMaxCharLength(), trgLen, Issue.SEVERITY_HIGH, 
 					srcSeg.toString(), trgSeg.toString(), null);
 			}
 		}
 		
 		if ( params.getCheckMaxCharLength() ) {
 			if ( srcLen <= params.getMaxCharLengthBreak() ) {
 				n = (srcLen==0 ? 0 : (int)((srcLen*params.getMaxCharLengthBelow())/100));
 			}
 			else {
 				n = (srcLen==0 ? 0 : (int)((srcLen*params.getMaxCharLengthAbove())/100));
 			}
 			if ( trgLen > n ) {
 				double d = (((float)trgLen)/(srcLen==0 ? 1.0 : ((float)srcLen)))*100.0;
 				reportIssue(IssueType.TARGET_LENGTH, tu, srcSeg.getId(),
 					String.format("The target is suspiciously longer than its source (%.2f%% of the source).", d),
 					0, -1, 0, -1, Issue.SEVERITY_LOW, 
 					srcSeg.toString(), trgSeg.toString(), null);
 			}
 		}
 
 		if ( params.getCheckMinCharLength() ) {
 			if ( srcLen <= params.getMinCharLengthBreak() ) {
 				n = (srcLen==0 ? 0 : (int)((srcLen*params.getMinCharLengthBelow())/100));
 			}
 			else {
 				n = (srcLen==0 ? 0 : (int)((srcLen*params.getMinCharLengthAbove())/100));
 			}
 			if ( trgSeg.text.getCodedText().length() < n ) {
 				double d = (((float)trgLen)/(srcLen==0 ? 1.0 : ((float)srcLen)))*100.0;
 				reportIssue(IssueType.TARGET_LENGTH, tu, srcSeg.getId(),
 					String.format("The target is suspiciously shorter than its source (%.2f%% of the source).", d),
 					0, -1, 0, -1, Issue.SEVERITY_LOW, 
 					srcSeg.toString(), trgSeg.toString(), null);
 			}
 		}
 	}
 
 	
 	private void checkStorageSize (ITextUnit tu,
 		TextContainer tc,
 		boolean isSource)
 	{
 		if ( tc == null ) return;
 		if ( tu.hasProperty(Property.ITS_STORAGESIZE) ) {
 			try {
 				String[] values = tu.getProperty(Property.ITS_STORAGESIZE).getValue().split("\t", -1);
 				if ( values[0].isEmpty() ) return; // No limits
 				int max = Integer.parseInt(values[0]);
 				if (( encoder2 == null ) || !encoder2.charset().name().equals(values[1]) ) {
 					encoder2 = Charset.forName(values[1]).newEncoder();
 				}
 				
 				// Get the plain text
 				TextFragment tf;
 				if ( tc.contentIsOneSegment() ) tf = tc.getFirstContent();
 				else tf = tc.getUnSegmentedContentCopy();
 				String  tmp = TextUnitUtil.getText(tf);
 				
 				// Compute the byte length
 				ByteBuffer buf = encoder2.encode(CharBuffer.wrap(tmp));
 				int len = buf.limit();
 				if ( len > max ) {
 					reportIssue(isSource ? IssueType.SOURCE_LENGTH : IssueType.TARGET_LENGTH, tu, null,
 						String.format("Number of bytes in the %s (using %s) is: %d. Number allowed: %d.",
 							(isSource ? "source" : "target"), values[1], len, max),
 						0, -1, 0, -1, Issue.SEVERITY_HIGH,
 						(isSource ? tc.toString() : "N/A"), (isSource ? "N/A" : tc.toString()), null);
 				}
 			}
 			catch ( Throwable e ) {
 				reportIssue(isSource ? IssueType.SOURCE_LENGTH : IssueType.TARGET_LENGTH, tu, null,
 					"Problem when trying use use ITS storage size property: "+e.getMessage(),
 					0, -1, 0, -1, Issue.SEVERITY_HIGH,
 					(isSource ? tc.toString() : "N/A"), (isSource ? "N/A" : tc.toString()), null);
 			}
 		}
 	}
 	
 	private void checkITSAllowedChars (ITextUnit tu,
 		TextContainer tc,
 		boolean isSource)
 	{
 		if ( tc == null ) return;
 		if ( !tu.hasProperty(Property.ITS_ALLOWEDCHARACTERS) ) return;
 		try {
 			String pattern = tu.getProperty(Property.ITS_ALLOWEDCHARACTERS).getValue();
 			// Re-set the compiled pattern if needed
 			if (( itsAllowedChars == null ) || !itsAllowedCharsPattern.equals(pattern) ) {
 				itsAllowedCharsPattern = pattern; // Remember for next time
 				// Invert the pattern to match on error (character NOT allowed)
 				if ( pattern.startsWith("[^") ) pattern = "["+pattern.substring(2);
 				else if ( pattern.startsWith("[") ) pattern = "[^"+pattern.substring(1);
 				else {
 					throw new RuntimeException("Pattern should start with '[' or '[^'.");
 				}
 				itsAllowedChars = Pattern.compile(pattern);
 			}
 			
 			// Get the plain text
 			TextFragment tf;
 			if ( tc.contentIsOneSegment() ) tf = tc.getFirstContent();
 			else tf = tc.getUnSegmentedContentCopy();
 			String tmp = TextUnitUtil.getText(tf);
 			
 			// Verify if we have a counter match
 			Matcher m = itsAllowedChars.matcher(tmp);
 			if ( !m.find() ) return; // No error
 			// Else, report the first character not allowed
//TODO: Update to make it work when there are inline codes			
			int ss = (isSource ? fromFragmentToString(tf, m.start()) : 0);
			int ts = (!isSource ? fromFragmentToString(tf, m.start()) : 0);
			int se = (isSource ? fromFragmentToString(tf, m.end()) : -1);
			int te = (!isSource ? fromFragmentToString(tf, m.end()) : -1);
 			reportIssue(IssueType.ALLOWED_CHARACTERS, tu, null,
				String.format("Character not allowed: '%s' (pattern: '%s'", m.group(), itsAllowedCharsPattern),
					ss, se, ts, te, Issue.SEVERITY_HIGH,
 					(isSource ? tc.toString() : "N/A"), (isSource ? "N/A" : tc.toString()), null);
 		}
 		catch ( Throwable e ) {
 			reportIssue(IssueType.ALLOWED_CHARACTERS, tu, null,
				String.format("Error when trying to check ITS allowed characters pattern '%s'. "+e.getMessage(), itsAllowedCharsPattern),
 				0, -1, 0, -1, Issue.SEVERITY_HIGH,
 				(isSource ? tc.toString() : "N/A"), (isSource ? "N/A" : tc.toString()), null);
 		}
 	}
 	
 	private void checkSuspectPatterns (Segment srcSeg,
 		Segment trgSeg,
 		ITextUnit tu)
 	{
 		String trgCText = trgSeg.text.getCodedText();
 		
 		if ( params.getDoubledWord() ) {
 			Matcher m = patDoubledWords.matcher(trgCText);
 			while ( m.find() ) {
 				// Check against the exceptions
 				// Use the lowercase of the word enclosed in ';' to match against the list
 				if ( doubledWordExceptions.indexOf(";"+m.group(1).toLowerCase()+";") == -1 ) {
 					// Not in the list: Not an exception, so we report it
 					reportIssue(IssueType.SUSPECT_PATTERN, tu, srcSeg.getId(),
 						String.format("Double word: \"%s\" found in the target.", m.group()),
 						0, -1,
 						fromFragmentToString(trgSeg.text, m.start()),
 						fromFragmentToString(trgSeg.text, m.end()),
 						Issue.SEVERITY_HIGH, 
 						srcSeg.toString(), trgSeg.toString(), null);
 				}
 			}
 		}
 	}
 
 	private void checkPatterns (Segment srcSeg,
 		Segment trgSeg,
 		ITextUnit tu)
 	{
 		//--- Source-based search
 		// Get the source text
 		String srcCText = srcSeg.text.getCodedText();
 		// Search for any enabled pattern in the source
 		for ( PatternItem item : patterns ) {
 			// Skip disabled items and items that use the target as the base
 			if ( !item.enabled || !item.fromSource ) continue;
 			
 			Matcher srcM = item.getSourcePattern().matcher(srcCText);
 			
 			// Use a copy for the target: it may get modified for the search
 			StringBuilder trgCTextCopy = new StringBuilder(trgSeg.text.getCodedText());
 
 			int from = 0;
 			while ( srcM.find(from) ) {
 				// Get the source text corresponding to the match
 				String srcPart = srcCText.substring(srcM.start(), srcM.end());
 				int start = 0;
 				int end = 0;
 				boolean found = false;
 				boolean expectSame = item.target.equals(PatternItem.SAME);
 				// Try to get the corresponding part in the target
 				if ( expectSame ) {
 					// If the target pattern is defined as being the same as the source
 					// Look for the same text in the source.
 					found = ((start = trgCTextCopy.indexOf(srcPart)) != -1);
 					end = start + srcPart.length();
 				}
 				else { // Target part has its own pattern
 					Matcher trgM = item.getTargetPattern().matcher(trgCTextCopy);
 					found = trgM.find();
 					if ( found ) {
 						start = trgM.start();
 						end = trgM.end();
 					}
 				}
 				// Process result
 				if ( found ) { // Remove that match in case source has several occurrences to match
 					trgCTextCopy.delete(start, end);
 				}
 				else { // Generate an issue
 					String msg;
 					if ( expectSame ) {
 						msg = String.format("The source part \"%s\" is not in the target", srcPart);
 					}
 					else {
 						msg = String.format("The source part \"%s\" has no correspondance in the target", srcPart);
 					}
 					// Add the description of the rule triggering the warning
 					if ( !Util.isEmpty(item.description) ) {
 						msg += " (from rule: "+item.description+").";
 					}
 					reportIssue(IssueType.UNEXPECTED_PATTERN, tu, srcSeg.getId(), msg,
 						fromFragmentToString(srcSeg.text, srcM.start()),
 						fromFragmentToString(srcSeg.text, srcM.end()),
 						0, -1, item.severity,
 						srcSeg.toString(), trgSeg.toString(), null);
 				}
 				
 				from = srcM.end();
 			}
 		}
 
 		//--- Target-based search
 		// Get the target text
 		String trgCText = trgSeg.text.getCodedText();
 		// Search for any enabled pattern in the source
 		for ( PatternItem item : patterns ) {
 			// Skip disabled items and items that use the source as the base
 			if ( !item.enabled || item.fromSource ) continue;
 			
 			Matcher trgM = item.getTargetPattern().matcher(trgCText);
 			
 			// Use a copy for the source: it may get modified for the search
 			StringBuilder srcCTextCopy = new StringBuilder(srcSeg.text.getCodedText());
 
 			while ( trgM.find() ) {
 				// Get the source text corresponding to the match
 				String trgPart = trgCText.substring(trgM.start(), trgM.end());
 				int start, end;
 				boolean found = false;
 				boolean expectSame = item.source.equals(PatternItem.SAME);
 				// Try to get the corresponding part in the source
 				if ( expectSame ) {
 					// If the source pattern is defined as being the same as the target
 					// Look for the same text in the source.
 					found = ((start = srcCTextCopy.indexOf(trgPart)) != -1);
 					end = start + trgPart.length();
 				}
 				else { // Source part has its own pattern
 					Matcher srcM = item.getSourcePattern().matcher(srcCTextCopy);
 					found = srcM.find();
 					start = srcM.start();
 					end = srcM.end();
 				}
 				// Process result
 				if ( found ) { // Remove that match in case target has several occurrences to match
 					srcCTextCopy.delete(start, end);
 				}
 				else { // Generate an issue
 					String msg;
 					if ( expectSame ) {
 						msg = String.format("The target part \"%s\" is not in the source.", trgPart);
 					}
 					else {
 						msg = String.format("The target part \"%s\" has no correspondance in the source.", trgPart);
 					}
 					reportIssue(IssueType.UNEXPECTED_PATTERN, tu, srcSeg.getId(), msg, 0, -1,
 						fromFragmentToString(trgSeg.text, trgM.start()),
 						fromFragmentToString(trgSeg.text, trgM.end()),
 						item.severity,
 						srcSeg.toString(), trgSeg.toString(), null);
 				}
 			}
 		}
 	}
 	
 	private void reportIssue (IssueType issueType,
 		ITextUnit tu,
 		String segId,
 		String message,
 		int srcStart,
 		int srcEnd,
 		int trgStart,
 		int trgEnd,
 		int severity,
 		String srcOri,
 		String trgOri,
 		Object extra)
 	{
 		Issue issue = new Issue(currentDocId, issueType, tu.getId(), segId, message,
 			srcStart, srcEnd, trgStart, trgEnd, severity, tu.getName());
 		issue.extra = extra;
 		issues.add(issue);
 		issue.enabled = true;
 		issue.oriSource = srcOri;
 		issue.oriTarget = trgOri;
 		
 		if ( sigList != null ) {
 			// Disable any issue for which we have the signature in the list
 			issue.enabled = !sigList.contains(issue.getSignature());
 		}
 	}
 
 	/**
 	 * Gets the position in the string representation of a fragment of a given
 	 * position in that fragment. 
	 * @param frag the fragment where the position is located.
 	 * @param pos the position.
 	 * @return the same position, but in the string representation of the fragment.
 	 */
 	public static int fromFragmentToString (TextFragment frag,
 		int pos)
 	{
 		// No codes means no correction
 		if ( !frag.hasCode() ) return pos;
 
 		// Else: correct the position
 		int len = 0;
 		String text = frag.getCodedText();
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( i >= pos ) {
 				return len;
 			}
 			if ( TextFragment.isMarker(text.charAt(i)) ) {
 				Code code = frag.getCode(text.charAt(++i));
 				len += code.getData().length();
 				continue;
 			}
 			else {
 				len++;
 			}
 		}
 		return len;
 	}
 }
