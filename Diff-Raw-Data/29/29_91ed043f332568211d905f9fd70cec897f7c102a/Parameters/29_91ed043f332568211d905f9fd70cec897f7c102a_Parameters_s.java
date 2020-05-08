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
 
 package net.sf.okapi.filters.properties;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.common.filters.LocalizationDirectives;
 
 public class Parameters extends BaseParameters {
 
 	public boolean useCodeFinder;
 	public InlineCodeFinder codeFinder;
 	public boolean escapeExtendedChars;
 	public boolean useKeyCondition;
 	public boolean extractOnlyMatchingKey;
 	public String keyCondition;
 	public boolean extraComments;
 	public boolean commentsAreNotes;
 	public LocalizationDirectives locDir;
 
 	public Parameters () {
 		locDir = new LocalizationDirectives();
 		codeFinder = new InlineCodeFinder();
 		reset();
 		toString(); // fill the list
 	}
 	
 	public void reset () {
 		locDir.reset();
		escapeExtendedChars = true;
 
 		useCodeFinder = true;
 		codeFinder.reset();
 		codeFinder.setSample("%s, %d, {1}, \\n, \\r, \\t, etc.");
 		codeFinder.setUseAllRulesWhenTesting(true);
 		// Default in-line codes: special escaped-chars and printf-style variable
 		codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]");
 		codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
 		//TODO: Add Java-style variables. this is too basic
 		codeFinder.addRule("\\{\\d.*?\\}");
 		
 		useKeyCondition = false;
 		extractOnlyMatchingKey = true;
 		keyCondition = ".*text.*";
 
 		extraComments = false;
 		commentsAreNotes = true;
 	}
 
 	@Override
 	public String toString () {
 		buffer.reset();
 		buffer.setBoolean("useLD", locDir.useLD());
 		buffer.setBoolean("localizeOutside", locDir.localizeOutside());
 		buffer.setBoolean("useKeyCondition", useKeyCondition);
 		buffer.setBoolean("extractOnlyMatchingKey", extractOnlyMatchingKey);
 		buffer.setString("keyCondition", keyCondition);
 		buffer.setBoolean("extraComments", extraComments);
 		buffer.setBoolean("commentsAreNotes", commentsAreNotes);
 		buffer.setBoolean("escapeExtendedChars", escapeExtendedChars);
 		buffer.setBoolean("useCodeFinder", useCodeFinder);
 		buffer.setGroup("codeFinderRules", codeFinder.toString());
 		return buffer.toString();
 	}
 	
 	public void fromString (String data) {
 		reset();
 		buffer.fromString(data);
 		boolean tmpBool1 = buffer.getBoolean("useLD", locDir.useLD());
 		boolean tmpBool2 = buffer.getBoolean("localizeOutside", locDir.localizeOutside());
 		locDir.setOptions(tmpBool1, tmpBool2);
 		useKeyCondition = buffer.getBoolean("useKeyCondition", useKeyCondition);
 		extractOnlyMatchingKey = buffer.getBoolean("extractOnlyMatchingKey", extractOnlyMatchingKey);
 		keyCondition = buffer.getString("keyCondition", keyCondition);
 		extraComments = buffer.getBoolean("extraComments",extraComments);
 		commentsAreNotes = buffer.getBoolean("commentsAreNotes", commentsAreNotes);
 		escapeExtendedChars = buffer.getBoolean("escapeExtendedChars", escapeExtendedChars);
 		useCodeFinder = buffer.getBoolean("useCodeFinder", useCodeFinder);
 		codeFinder.fromString(buffer.getGroup("codeFinderRules", ""));
 	}
 
 }
