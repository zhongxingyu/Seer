 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.BaseParameters;
 
 public class Parameters extends BaseParameters {
 	
 	public static final String FILE_EXTENSION = ".qccfg";
 
 	public static final int SCOPE_ALL = 0;
 	public static final int SCOPE_APPROVEDONLY = 1;
 	public static final int SCOPE_NOTAPPROVEDONLY = 2;
 	
 	private static final String OUTPUTPATH = "outputPath";
 	private static final String AUTOOPEN = "autoOpen";
 	private static final String LEADINGWS = "leadingWS";
 	private static final String TRAILINGWS = "trailingWS";
 	private static final String EMPTYTARGET = "emptyTarget";
 	private static final String EMPTYSOURCE = "emptySource";
 	private static final String TARGETSAMEASSOURCE = "targetSameAsSource";
 	private static final String TARGETSAMEASSOURCE_WITHCODES = "targetSameAsSourceWithCodes";
 	private static final String CODEDIFFERENCE = "codeDifference";
 	private static final String CHECKPATTERNS = "checkPatterns";
 	private static final String PATTERNCOUNT = "patternCount";
 	private static final String USEPATTERN = "usePattern";
 	private static final String FROMSOURCEPATTERN = "fromSourcePattern";
 	private static final String SEVERITYPATTERN = "severityPattern";
 	private static final String SOURCEPATTERN = "sourcePattern";
 	private static final String TARGETPATTERN = "targetPattern";
 	private static final String DESCPATTERN = "descPattern";
 	private static final String CHECKWITHLT = "checkWithLT";
 	private static final String SERVERURL = "serverURL";
 	private static final String TRANSLATELTMSG = "translateLTMsg";
 	private static final String LTTRANSLATIONSOURCE = "ltTranslationSource";
 	private static final String LTTRANSLATIONTARGET = "ltTranslationTarget";
 	private static final String SAVESESSION = "saveSession";
 	private static final String SESSIONPATH = "sessionPath";
 	private static final String DOUBLEDWORD = "doubledWord";
 	private static final String DOUBLEDWORDEXCEPTIONS = "doubledWordExceptions";
 	private static final String CHECKMAXCHARLENGTH = "checkMaxCharLength";
 	private static final String MAXCHARLENGTHBREAK = "maxCharLengthBreak";
 	private static final String MAXCHARLENGTHABOVE = "maxCharLengthAbove";
 	private static final String MAXCHARLENGTHBELOW = "maxCharLengthBelow";
 	private static final String CHECKMINCHARLENGTH = "checkMinCharLength";
 	private static final String MINCHARLENGTHBREAK = "minCharLengthBreak";
 	private static final String MINCHARLENGTHABOVE = "minCharLengthAbove";
 	private static final String MINCHARLENGTHBELOW = "minCharLengthBelow";
 	private static final String CHECKCHARACTERS = "checkCharacters";
 	private static final String CHARSET = "charset";
 	private static final String EXTRACHARSALLOWED = "extraCharsAllowed";
 	private static final String CORRUPTEDCHARACTERS = "corruptedCharacters";
 	private static final String SCOPE = "scope";
 	private static final String EXTRACODESALLOWED = "extraCodesAllowed";
 	private static final String MISSINGCODESALLOWED = "missingCodesAllowed";
 	
 	private static final String CHECKTERMS = "checkTerms";
 	private static final String TERMSPATH = "termsPath";
 
 	String outputPath;
 	boolean autoOpen;
 	boolean leadingWS;
 	boolean trailingWS;
 	boolean emptyTarget;
 	boolean emptySource;
 	boolean targetSameAsSource;
 	boolean targetSameAsSourceWithCodes;
 	boolean codeDifference;
 	boolean checkPatterns;
 	List<PatternItem> patterns;
 	boolean checkWithLT;
 	String serverURL;
 	boolean translateLTMsg;
 	String ltTranslationSource;
 	String ltTranslationTarget;
 	boolean saveSession;
 	String sessionPath;
 	boolean doubledWord;
 	String doubledWordExceptions;
 	boolean checkMaxCharLength;
 	int maxCharLengthBreak;
 	int maxCharLengthAbove;
 	int maxCharLengthBelow;
 	boolean checkMinCharLength;
 	int minCharLengthBreak;
 	int minCharLengthAbove;
 	int minCharLengthBelow;
 	boolean checkCharacters;
 	String charset;
 	String extraCharsAllowed;
 	boolean corruptedCharacters;
 	int scope;
 	List<String> extraCodesAllowed;
 	List<String> missingCodesAllowed;
 	boolean checkTerms;
 	String termsPath;
 
 	public Parameters () {
 		reset();
 	}
 
 	public List<String> getExtraCodesAllowed () {
 		return extraCodesAllowed;
 	}
 
 	public List<String> getMissingCodesAllowed () {
 		return missingCodesAllowed;
 	}
 
 	public int getScope () {
 		return scope;
 	}
 
 	public void setScope (int scope) {
 		this.scope = scope;
 	}
 
 	public boolean getCorruptedCharacters () {
 		return corruptedCharacters;
 	}
 
 	public void setCorruptedCharacters (boolean corruptedCharacters) {
 		this.corruptedCharacters = corruptedCharacters;
 	}
 
 	public boolean getCheckCharacters () {
 		return checkCharacters;
 	}
 
 	public void setCheckCharacters (boolean checkCharacters) {
 		this.checkCharacters = checkCharacters;
 	}
 
 	public String getCharset () {
 		return charset;
 	}
 
 	public void setCharset (String charset) {
 		this.charset = charset;
 	}
 
 	public String getExtraCharsAllowed () {
 		return extraCharsAllowed;
 	}
 
 	public void setExtraCharsAllowed (String extraCharsAllowed) {
 		this.extraCharsAllowed = extraCharsAllowed;
 	}
 
 	public boolean getCheckMaxCharLength () {
 		return checkMaxCharLength;
 	}
 
 	public void setCheckMaxCharLength (boolean checkMaxCharLength) {
 		this.checkMaxCharLength = checkMaxCharLength;
 	}
 
 	public int getMaxCharLengthBreak () {
 		return maxCharLengthBreak;
 	}
 
 	public void setMaxCharLengthBreak (int maxCharLengthBreak) {
 		this.maxCharLengthBreak = maxCharLengthBreak;
 	}
 
 	public int getMaxCharLengthAbove () {
 		return maxCharLengthAbove;
 	}
 
 	public void setMaxCharLengthAbove (int maxCharLengthAbove) {
 		this.maxCharLengthAbove = maxCharLengthAbove;
 	}
 
 	public int getMaxCharLengthBelow () {
 		return maxCharLengthBelow;
 	}
 
 	public void setMaxCharLengthBelow (int maxCharLengthBelow) {
 		this.maxCharLengthBelow = maxCharLengthBelow;
 	}
 
 	public boolean getCheckMinCharLength () {
 		return checkMinCharLength;
 	}
 
 	public void setCheckMinCharLength (boolean checkMinCharLength) {
 		this.checkMinCharLength = checkMinCharLength;
 	}
 
 	public int getMinCharLengthBreak () {
 		return minCharLengthBreak;
 	}
 
 	public void setMinCharLengthBreak (int minCharLengthBreak) {
 		this.minCharLengthBreak = minCharLengthBreak;
 	}
 
 	public int getMinCharLengthAbove () {
 		return minCharLengthAbove;
 	}
 
 	public void setMinCharLengthAbove (int minCharLengthAbove) {
 		this.minCharLengthAbove = minCharLengthAbove;
 	}
 
 	public int getMinCharLengthBelow () {
 		return minCharLengthBelow;
 	}
 
 	public void setMinCharLengthBelow (int minCharLengthBelow) {
 		this.minCharLengthBelow = minCharLengthBelow;
 	}
 
 	public boolean getDoubledWord () {
 		return doubledWord;
 	}
 
 	public void setDoubledWord (boolean doubledWord) {
 		this.doubledWord = doubledWord;
 	}
 
 	public String getDoubledWordExceptions () {
 		return doubledWordExceptions;
 	}
 
 	public void setDoubledWordExceptions (String doubledWordExceptions) {
 		this.doubledWordExceptions = doubledWordExceptions;
 	}
 
 	public boolean getSaveSession () {
 		return saveSession;
 	}
 
 	public void setSaveSession (boolean saveSession) {
 		this.saveSession = saveSession;
 	}
 
 	public String getSessionPath () {
 		return sessionPath;
 	}
 
 	public void setSessionPath (String sessionPath) {
 		this.sessionPath = sessionPath;
 	}
 
 	public String getOutputPath () {
 		return outputPath;
 	}
 
 	public void setOutputPath (String outputPath) {
 		this.outputPath = outputPath;
 	}
 
 	public boolean getAutoOpen () {
 		return autoOpen;
 	}
 
 	public void setAutoOpen (boolean autoOpen) {
 		this.autoOpen = autoOpen;
 	}
 
 	public boolean getLeadingWS () {
 		return leadingWS;
 	}
 
 	public void setLeadingWS (boolean leadingWS) {
 		this.leadingWS = leadingWS;
 	}
 
 	public boolean getTrailingWS () {
 		return trailingWS;
 	}
 
 	public void setTrailingWS (boolean trailingWS) {
 		this.trailingWS = trailingWS;
 	}
 
 	public boolean getEmptyTarget () {
 		return emptyTarget;
 	}
 
 	public void setEmptyTarget (boolean emptyTarget) {
 		this.emptyTarget = emptyTarget;
 	}
 
 	public boolean getEmptySource () {
 		return emptySource;
 	}
 
 	public void setEmptySource (boolean emptySource) {
 		this.emptySource = emptySource;
 	}
 
 	public boolean getTargetSameAsSource () {
 		return targetSameAsSource;
 	}
 
 	public void setTargetSameAsSource (boolean targetSameAsSource) {
 		this.targetSameAsSource = targetSameAsSource;
 	}
 
 	public boolean getTargetSameAsSourceWithCodes () {
 		return targetSameAsSourceWithCodes;
 	}
 
 	public void setTargetSameAsSourceWithCodes (boolean targetSameAsSourceWithCodes) {
 		this.targetSameAsSourceWithCodes = targetSameAsSourceWithCodes;
 	}
 
 	public boolean getCodeDifference () {
 		return codeDifference;
 	}
 
 	public void setCodeDifference (boolean codeDifference) {
 		this.codeDifference = codeDifference;
 	}
 
 	public boolean getCheckPatterns () {
 		return checkPatterns;
 	}
 
 	public void setCheckPatterns (boolean patterns) {
 		this.checkPatterns = patterns;
 	}
 	
 	public List<PatternItem> getPatterns () {
 		return this.patterns;
 	}
 	
 	public void setPatterns (List<PatternItem> patterns) {
 		this.patterns = patterns;
 	}
 	
 	public boolean getCheckWithLT () {
 		return this.checkWithLT;
 	}
 
 	public void setCheckWithLT (boolean checkWithLT) {
 		this.checkWithLT = checkWithLT;
 	}
 	
 	public String getServerURL () {
 		return this.serverURL;
 	}
 	
 	public void setServerURL (String serverURL) {
 		this.serverURL = serverURL;
 	}
 	
 	public boolean getTranslateLTMsg () {
 		return translateLTMsg;
 	}
 
 	public void setTranslateLTMsg (boolean translateLTMsg) {
 		this.translateLTMsg = translateLTMsg;
 	}
 
 	public String getLtTranslationSource () {
 		return ltTranslationSource;
 	}
 
 	public void setLtTranslationSource (String ltTranslationSource) {
 		this.ltTranslationSource = ltTranslationSource;
 	}
 
 	public String getLtTranslationTarget () {
 		return ltTranslationTarget;
 	}
 
 	public void setLtTranslationTarget (String ltTranslationTarget) {
 		this.ltTranslationTarget = ltTranslationTarget;
 	}
 
 	public boolean getCheckTerms () {
 		return checkTerms;
 	}
 
 	public void setCheckTerms (boolean checkTerms) {
 		this.checkTerms = checkTerms;
 	}
 	
 	public String getTermsPath () {
 		return termsPath;
 	}
 
 	public void setTermsPath (String termsPath) {
 		this.termsPath = termsPath;
 	}
 	
 	@Override
 	public void reset () {
 		outputPath = "${rootDir}/qa-report.html";
 		autoOpen = true;
 		leadingWS = true;
 		trailingWS = true;
 		emptyTarget = true;
 		emptySource = true;
 		targetSameAsSource = true;
 		targetSameAsSourceWithCodes = true;
 		codeDifference = true;
 		checkPatterns = true;
 		checkWithLT = false;
 		serverURL = "http://localhost:8081/"; // Default
 		translateLTMsg = false;
 		ltTranslationSource = "";
 		ltTranslationTarget = "en";
 		saveSession = true;
 		sessionPath = "${rootDir}/qa-session"+QualityCheckSession.FILE_EXTENSION;
 		doubledWord = true;
 		doubledWordExceptions = "sie;vous";
 		corruptedCharacters = true;
 		scope = SCOPE_ALL;
 		
 		checkMaxCharLength = true;
 		maxCharLengthBreak = 20;
 		maxCharLengthAbove = 200;
 		maxCharLengthBelow = 350;
 		checkMinCharLength = true;
 		minCharLengthBreak = 20;
 		minCharLengthAbove = 45;
 		minCharLengthBelow = 30;
 		
 		checkCharacters = false;
 		charset = "ISO-8859-1";
 		extraCharsAllowed = "";
 		
 		checkTerms = false;
 		termsPath = "";
 
 		patterns = new ArrayList<PatternItem>();
 		
 		// Parentheses
 		patterns.add(new PatternItem(
 			"[\\(\\)]", "<same>",
 			true, Issue.SEVERITY_LOW, "Parentheses"));
 		
 		// Bracketing characters (except parentheses)
 		patterns.add(new PatternItem(
 			"[\\p{Ps}\\p{Pe}&&[^\\(\\)]]", "<same>",
 			true, Issue.SEVERITY_LOW, "Bracketing characters (except parentheses)"));
 		
 		// Email addresses
 		patterns.add(new PatternItem(
 			"[\\w\\.\\-]+@[\\w\\.\\-]+", "<same>",
 			true, Issue.SEVERITY_MEDIUM, "Email addresses"));
 		
 		// URLs
 		patterns.add(new PatternItem(
			//"((http|https|ftp|sftp)\\:\\/\\/([-_a-z0-9]+\\@)?)?(([-_a-z0-9]+\\.)+[-_a-z0-9]+(\\:[0-9]+)?)((\\/([-_.:;+~%#$?=&,()\\w]*[\\w])?))*", "<same>",
			"https?:[\\w/\\.:;+\\-~\\%#\\$?=&,()]+[\\w/:;+\\-~\\%#\\$?=&,()]+|www\\.[\\w/\\.:;+\\-~\\%#\\$?=&,()]+|ftp:[\\w/\\.:;+\\-~\\%#?=&,]+", "<same>",
 			true, Issue.SEVERITY_MEDIUM, "URLs"));
 		
 		// IP addresses
 		patterns.add(new PatternItem(
 			"\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "<same>",
 			true, Issue.SEVERITY_HIGH, "IP addresses"));
 		
 		// C-style printf 
 		patterns.add(new PatternItem(
 			"%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]", "<same>",
 			true, Issue.SEVERITY_HIGH, "C-style printf codes"));
 		
 		// Triple letter
 		PatternItem item = new PatternItem(
 			"<same>", "([\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}])\\1\\1",
 			true, Issue.SEVERITY_MEDIUM, "Tripled letter");
 		item.fromSource = false;
 		patterns.add(item);
 		
 		extraCodesAllowed = new ArrayList<String>();
 		missingCodesAllowed = new ArrayList<String>();
 	}
 
 	@Override
 	public void fromString (String data) {
 		reset();
 		buffer.fromString(data);
 		outputPath = buffer.getString(OUTPUTPATH, outputPath);
 		autoOpen = buffer.getBoolean(AUTOOPEN, autoOpen);
 		leadingWS = buffer.getBoolean(LEADINGWS, leadingWS);
 		trailingWS = buffer.getBoolean(TRAILINGWS, trailingWS);
 		emptyTarget = buffer.getBoolean(EMPTYTARGET, emptyTarget);
 		emptySource = buffer.getBoolean(EMPTYSOURCE, emptySource);
 		targetSameAsSource = buffer.getBoolean(TARGETSAMEASSOURCE, targetSameAsSource);
 		targetSameAsSourceWithCodes = buffer.getBoolean(TARGETSAMEASSOURCE_WITHCODES, targetSameAsSourceWithCodes);
 		codeDifference = buffer.getBoolean(CODEDIFFERENCE, codeDifference);
 		checkWithLT = buffer.getBoolean(CHECKWITHLT, checkWithLT);
 		serverURL = buffer.getString(SERVERURL, serverURL);
 		translateLTMsg = buffer.getBoolean(TRANSLATELTMSG, translateLTMsg);
 		ltTranslationSource = buffer.getString(LTTRANSLATIONSOURCE, ltTranslationSource);
 		ltTranslationTarget = buffer.getString(LTTRANSLATIONTARGET, ltTranslationTarget);
 		saveSession = buffer.getBoolean(SAVESESSION, saveSession);
 		sessionPath = buffer.getString(SESSIONPATH, sessionPath);
 		doubledWord = buffer.getBoolean(DOUBLEDWORD, doubledWord);
 		doubledWordExceptions = buffer.getString(DOUBLEDWORDEXCEPTIONS, doubledWordExceptions);
 		corruptedCharacters = buffer.getBoolean(CORRUPTEDCHARACTERS, corruptedCharacters);
 		scope = buffer.getInteger(SCOPE, scope);
 		// Length
 		checkMaxCharLength = buffer.getBoolean(CHECKMAXCHARLENGTH, checkMaxCharLength);
 		maxCharLengthBreak = buffer.getInteger(MAXCHARLENGTHBREAK, maxCharLengthBreak);
 		maxCharLengthAbove = buffer.getInteger(MAXCHARLENGTHABOVE, maxCharLengthAbove);
 		maxCharLengthBelow = buffer.getInteger(MAXCHARLENGTHBELOW, maxCharLengthBelow);
 		checkMinCharLength = buffer.getBoolean(CHECKMINCHARLENGTH, checkMinCharLength);
 		minCharLengthBreak = buffer.getInteger(MINCHARLENGTHBREAK, minCharLengthBreak);
 		minCharLengthAbove = buffer.getInteger(MINCHARLENGTHABOVE, minCharLengthAbove);
 		minCharLengthBelow = buffer.getInteger(MINCHARLENGTHBELOW, minCharLengthBelow);
 		// Characters
 		checkCharacters = buffer.getBoolean(CHECKCHARACTERS, checkCharacters);
 		charset = buffer.getString(CHARSET, charset);
 		extraCharsAllowed = buffer.getString(EXTRACHARSALLOWED, extraCharsAllowed);
 		// Terms
 		checkTerms = buffer.getBoolean(CHECKTERMS, checkTerms);
 		termsPath = buffer.getString(TERMSPATH, termsPath);
 		
 		// Patterns
 		checkPatterns = buffer.getBoolean(CHECKPATTERNS, checkPatterns);
 		int count = buffer.getInteger(PATTERNCOUNT, 0);
 		if ( count > 0 ) patterns.clear(); // Clear the defaults
 		for ( int i=0; i<count; i++ ) {
 			boolean enabled = buffer.getBoolean(String.format("%s%d", USEPATTERN, i), true);
 			int severity = buffer.getInteger(String.format("%s%d", SEVERITYPATTERN, i), Issue.SEVERITY_MEDIUM);
 			boolean fromSource = buffer.getBoolean(String.format("%s%d", FROMSOURCEPATTERN, i), true);
 			String source = buffer.getString(String.format("%s%d", SOURCEPATTERN, i), "");
 			String target = buffer.getString(String.format("%s%d", TARGETPATTERN, i), PatternItem.SAME);
 			String desc = buffer.getString(String.format("%s%d", DESCPATTERN, i), "");
 			patterns.add(new PatternItem(source, target, enabled, severity, fromSource, desc));
 		}
 		
 		// Allowed extra codes
 		count = buffer.getInteger(EXTRACODESALLOWED, 0);
 		if ( count > 0 ) extraCodesAllowed.clear();
 		for ( int i=0; i<count; i++ ) {
 			extraCodesAllowed.add(buffer.getString(String.format("%s%d", EXTRACODESALLOWED, i), ""));
 		}
 		// Allowed missing codes
 		count = buffer.getInteger(MISSINGCODESALLOWED, 0);
 		if ( count > 0 ) missingCodesAllowed.clear();
 		for ( int i=0; i<count; i++ ) {
 			missingCodesAllowed.add(buffer.getString(String.format("%s%d", MISSINGCODESALLOWED, i), ""));
 		}
 	}
 
 	@Override
 	public String toString() {
 		buffer.reset();
 		buffer.setString(OUTPUTPATH, outputPath);
 		buffer.setBoolean(AUTOOPEN, autoOpen);
 		buffer.setBoolean(LEADINGWS, leadingWS);
 		buffer.setBoolean(TRAILINGWS, trailingWS);
 		buffer.setBoolean(EMPTYTARGET, emptyTarget);
 		buffer.setBoolean(EMPTYSOURCE, emptySource);
 		buffer.setBoolean(TARGETSAMEASSOURCE, targetSameAsSource);
 		buffer.setBoolean(TARGETSAMEASSOURCE_WITHCODES, targetSameAsSourceWithCodes);
 		buffer.setBoolean(CODEDIFFERENCE, codeDifference);
 		buffer.setBoolean(CHECKWITHLT, checkWithLT);
 		buffer.setString(SERVERURL, serverURL);
 		buffer.setBoolean(TRANSLATELTMSG, translateLTMsg);
 		buffer.setString(LTTRANSLATIONSOURCE, ltTranslationSource);
 		buffer.setString(LTTRANSLATIONTARGET, ltTranslationTarget);
 		buffer.setBoolean(SAVESESSION, saveSession);
 		buffer.setString(SESSIONPATH, sessionPath);
 		buffer.setBoolean(DOUBLEDWORD, doubledWord);
 		buffer.setString(DOUBLEDWORDEXCEPTIONS, doubledWordExceptions);
 		buffer.setBoolean(CORRUPTEDCHARACTERS, corruptedCharacters);
 		buffer.setInteger(SCOPE, scope);
 		// Length
 		buffer.setBoolean(CHECKMAXCHARLENGTH, checkMaxCharLength);
 		buffer.setInteger(MAXCHARLENGTHBREAK, maxCharLengthBreak);
 		buffer.setInteger(MAXCHARLENGTHABOVE, maxCharLengthAbove);
 		buffer.setInteger(MAXCHARLENGTHBELOW, maxCharLengthBelow);
 		buffer.setBoolean(CHECKMINCHARLENGTH, checkMinCharLength);
 		buffer.setInteger(MINCHARLENGTHBREAK, minCharLengthBreak);
 		buffer.setInteger(MINCHARLENGTHABOVE, minCharLengthAbove);
 		buffer.setInteger(MINCHARLENGTHBELOW, minCharLengthBelow);
 		// Characters
 		buffer.setBoolean(CHECKCHARACTERS, checkCharacters);
 		buffer.setString(CHARSET, charset);
 		buffer.setString(EXTRACHARSALLOWED, extraCharsAllowed);
 		// Terms
 		buffer.setBoolean(CHECKTERMS, checkTerms);
 		buffer.setString(TERMSPATH, termsPath);
 		// Patterns
 		buffer.setBoolean(CHECKPATTERNS, checkPatterns);
 		buffer.setInteger(PATTERNCOUNT, patterns.size());
 		for ( int i=0; i<patterns.size(); i++ ) {
 			buffer.setBoolean(String.format("%s%d", USEPATTERN, i), patterns.get(i).enabled);
 			buffer.setBoolean(String.format("%s%d", FROMSOURCEPATTERN, i), patterns.get(i).fromSource);
 			buffer.setInteger(String.format("%s%d", SEVERITYPATTERN, i), patterns.get(i).severity);
 			buffer.setString(String.format("%s%d", SOURCEPATTERN, i), patterns.get(i).source);
 			buffer.setString(String.format("%s%d", TARGETPATTERN, i), patterns.get(i).target);
 			buffer.setString(String.format("%s%d", DESCPATTERN, i), patterns.get(i).description);
 		}
 		
 		buffer.setInteger(EXTRACODESALLOWED, extraCodesAllowed.size());
 		for ( int i=0; i<extraCodesAllowed.size(); i++ ) {
 			buffer.setString(String.format("%s%d", EXTRACODESALLOWED, i), extraCodesAllowed.get(i));
 		}		
 		buffer.setInteger(MISSINGCODESALLOWED, missingCodesAllowed.size());
 		for ( int i=0; i<missingCodesAllowed.size(); i++ ) {
 			buffer.setString(String.format("%s%d", MISSINGCODESALLOWED, i), missingCodesAllowed.get(i));
 		}		
 		
 		return buffer.toString();
 	}
 	
 }
