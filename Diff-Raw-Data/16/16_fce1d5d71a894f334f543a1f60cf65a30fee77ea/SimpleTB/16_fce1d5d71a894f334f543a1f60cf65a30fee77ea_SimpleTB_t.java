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
 
 package net.sf.okapi.lib.terminology.simpletb;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Range;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.lib.terminology.ConceptEntry;
 import net.sf.okapi.lib.terminology.IGlossaryReader;
 import net.sf.okapi.lib.terminology.LangEntry;
 import net.sf.okapi.lib.terminology.TermEntry;
 import net.sf.okapi.lib.terminology.TermHit;
 import net.sf.okapi.lib.terminology.csv.CSVReader;
 import net.sf.okapi.lib.terminology.tbx.TBXReader;
 import net.sf.okapi.lib.terminology.tsv.TSVReader;
 
 /**
  * Very basic memory-only simple termbase.
  * This is used for prototyping the terminology interface.
  */
 public class SimpleTB {
 	
 	private static final String SIGNATURE = "SimpleTB-v1";
 	
 	LocaleId srcLoc;
 	LocaleId trgLoc;
 	private List<Entry> entries;
 	private boolean betweenCodes;
 	
 	public SimpleTB (LocaleId srcLoc,
 		LocaleId trgLoc)
 	{
 		this.srcLoc = srcLoc;
 		this.trgLoc = trgLoc;
 		reset();
 	}
 	
 	private void reset () {
 		entries = new ArrayList<Entry>();
 	}
 	
 	public void initialize (boolean stringSearch,
 		boolean betweenCodes)
 	{
 		this.betweenCodes = betweenCodes;
 		// In case of a string-based search: we sort the source terms: longer first
 		if ( stringSearch ) {
 			Collections.sort(entries);
 		}
 	}
 	
 	public void guessAndImport (File file) {
 		String ext = Util.getExtension(file.getPath());
 		if ( ext.equalsIgnoreCase(".tbx") ) {
 			importTBX(file);
 		}
 		if ( ext.equalsIgnoreCase(".csv") ) {
 			importCSV(file);
 		}
 		else { // Try tab-delimited
 			importTSV(file);
 		}
 	}
 	
 	public void importTBX (File file) {
 		importGlossary(new TBXReader(), file);
 	}
 	
 	public void importCSV (File file) {
 		importGlossary(new CSVReader(srcLoc, trgLoc), file);
 	}
 	
 	public void importTSV (File file) {
 		importGlossary(new TSVReader(srcLoc, trgLoc), file);
 	}
 	
 	private void importGlossary (IGlossaryReader reader,
 		File file)
 	{
 		try {
 			reader.open(file);
 			while ( reader.hasNext() ) {
 				ConceptEntry cent = reader.next();
 				if ( !cent.hasLocale(srcLoc) || !cent.hasLocale(trgLoc) ) continue;
 				LangEntry srcLent = cent.getEntries(srcLoc);
 				LangEntry trgLent = cent.getEntries(trgLoc);
 				if ( !srcLent.hasTerm() || !trgLent.hasTerm() ) continue;
 				Entry ent = new Entry(srcLent.getTerm(0).getText());
 				ent.setTargetTerm(trgLent.getTerm(0).getText());
 				entries.add(ent);
 			}
 		}
 		finally {
 			if ( reader != null ) reader.close();
 		}
 	}
 	
 	public void removeAll () {
 		entries.clear();
 	}
 
 	public Entry addEntry (String srcTerm,
 		String trgTerm)
 	{
 		Entry ent = new Entry(srcTerm);
 		ent.setTargetTerm(trgTerm);
 		entries.add(ent);
 		return ent;
 	}
 
 	public List<TermHit> getExistingStrings (TextFragment frag,
 		LocaleId fragmentLoc,
 		LocaleId otherLoc)
 	{
 		List<TermHit> res = new ArrayList<TermHit>();
 		
 		// Determine if the termbase has the searched locale
 		boolean searchSource = fragmentLoc.equals(srcLoc);
 		if ( !searchSource ) {
 			if ( !fragmentLoc.equals(trgLoc) ) {
 				return res; // Nothing
 			}
 		}
 
 		String stringToMatch;
 		String otherString;
 		StringBuilder text = new StringBuilder(frag);
 		Range location = new Range(0, 0);
 		
 		// For each term in the list
 		for ( Entry ent : entries ) {
 			// Select the source and target terms to search for
 			if ( searchSource ) {
 				stringToMatch = ent.getSourceTerm();
 				otherString = ent.getTargetTerm();
 			}
 			else {
 				stringToMatch = ent.getTargetTerm();
 				otherString = ent.getSourceTerm();
 			}
 			
 			if (( stringToMatch == null ) || ( otherString == null )) continue;
 			
 			while ( true ) {
 				if ( !isValidMatch(text, stringToMatch, location, betweenCodes) ) break;
 				// Else: Save the term
 				TermHit th = new TermHit();
 				th.sourceTerm = new TermEntry(stringToMatch);
 				th.targetTerm = new TermEntry(otherString);
 				th.range = new Range(location.start, location.end);
 				res.add(th);
 				// Obliterate the match so we don't re-match it 
 				for ( int i=location.start; i<location.end; i++ ) {
 					text.setCharAt(i, '`');
 				}
 			}
 		}
 		
 		return res;
 	}
 
 	/**
 	 * Searches for a given string in a text. The location parameter is updated with the position of the first 
 	 * character and the one of the character after the last one.
 	 * @param text Text where to search the strings.
 	 * @param stringToMatch the string to search for.
 	 * @param location location of the term.
 	 * @return true if it's a match, false otherwise. If the return is true, the values in location are updated.
 	 */
 	public static boolean isValidMatch (StringBuilder text,
 		String stringToMatch,
 		Range location,
 		boolean betweenCodes)
 	{
 		int n = text.indexOf(stringToMatch);
 		if ( n == -1 ) return false; // No more of that term
 		// Check "word boundaries"
 		if ( n > 0 ) {
 			int cp = text.codePointAt(n-1);
 			if (( Character.getType(cp) == Character.LOWERCASE_LETTER ) ||
 				( Character.getType(cp) == Character.UPPERCASE_LETTER ) ||
 				( Character.getType(cp) == Character.TITLECASE_LETTER ) ||
 				( Character.getType(cp) == Character.DECIMAL_DIGIT_NUMBER ))
 			{
 				// If the preceding character is a letter, it's not a "word"
 				return false;
 			}
 		}
 		if ( betweenCodes ) {
 			// If it must be between codes: check the previous previous char, it should be a marker.
 			if (( n <= 1 ) || !TextFragment.isMarker(text.charAt(n-2)) ) {
 				return false;
 			}
 		}
 		
 		int last = n+stringToMatch.length();
 		if ( last < text.length() ) {
 			int cp = text.codePointAt(last);
 			if (( Character.getType(cp) == Character.LOWERCASE_LETTER ) ||
 				( Character.getType(cp) == Character.UPPERCASE_LETTER ) ||
 				( Character.getType(cp) == Character.TITLECASE_LETTER ) ||
 				( Character.getType(cp) == Character.DECIMAL_DIGIT_NUMBER ))
 			{
 				// If the following character is a letter, it's not a "word"
 				return false;
 			}
 		}
 		if ( betweenCodes ) {
 			// If must be between codes: check the next character
 			if (( last+1 > text.length() ) || !TextFragment.isMarker(text.charAt(last)) ) {
 				return false;
 			}
 		}
 		location.start = n;
 		location.end = last;
 		return true;
 	}
 	
 	/*
 	 * Very crude implementation of the search terms function.
 	 */
 	public List<TermHit> getExistingTerms (TextFragment frag,
 		LocaleId fragmentLoc,
 		LocaleId otherLoc)
 	{
		String text = TextUnitUtil.getText(frag).toLowerCase(); // Strip inline codes and convert to lowercase
 		List<String> parts = Arrays.asList(text.split("\\s"));
 		List<TermHit> res = new ArrayList<TermHit>();
 	
 		// Determine if the termbase has the searched locale
 		boolean searchSource = fragmentLoc.equals(srcLoc);
 		if ( !searchSource ) {
 			if ( !fragmentLoc.equals(trgLoc) ) {
 				return res; // Nothing
 			}
 		}
 
 		String termToMatch;
 		String otherTerm;
 		for ( Entry ent : entries ) {
 			if ( searchSource ) {
 				termToMatch = ent.getSourceTerm();
 				otherTerm = ent.getTargetTerm();
 			}
 			else {
 				termToMatch = ent.getTargetTerm();
 				otherTerm = ent.getSourceTerm();
 			}
 			if (( termToMatch == null ) || ( otherTerm == null )) continue;
 			if ( parts.contains(termToMatch.toLowerCase()) ) {
 				TermHit th = new TermHit();
 				th.sourceTerm = new TermEntry(termToMatch);
 				th.targetTerm = new TermEntry(otherTerm);
 				res.add(th);
 			}
 		}
 		
 		return res;
 	}
 
 	public void save (String path) {
 		DataOutputStream dos = null;
 		try {
 			dos = new DataOutputStream(new FileOutputStream(path));
 			// Version (just in case)
 			dos.writeUTF(SIGNATURE);
 			
 			// Locales
 			dos.writeUTF(srcLoc.toString());
 			dos.writeUTF(trgLoc.toString());
 			
 			// Entries
 			dos.writeInt(entries.size());
 			for ( Entry ent : entries ) {
 				dos.writeUTF(ent.srcTerm);
 				dos.writeUTF(ent.trgTerm);
 				dos.writeUTF(ent.definition);
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error while saving.", e);
 		}
 		finally {
 			if ( dos != null ) {
 				try {
 					dos.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException("Error closing file.", e);
 				}
 			}
 		}
 	}
 	
 	public void load (String path) {
 		reset();
 		// Temporary code, waiting for DB
 		DataInputStream dis = null;
 		try {
 			dis = new DataInputStream(new FileInputStream(path));
 
 			// version
 			String tmp = dis.readUTF();
 			if ( !tmp.equals(SIGNATURE) ) {
 				throw new OkapiIOException("Invalid signature: This file is not a SimpleTB files, or is corrupted.");
 			}
 
 			// Locales
 			tmp = dis.readUTF(); // Source
 			srcLoc = LocaleId.fromString(tmp);
 			tmp = dis.readUTF(); // Target
 			trgLoc = LocaleId.fromString(tmp);
 			
 			// Entries
 			int count = dis.readInt();
 			for ( int i=0; i<count; i++ ) {
 				Entry ent = new Entry(dis.readUTF());
 				ent.setTargetTerm(dis.readUTF());
 				ent.setdefinition(dis.readUTF());
 				entries.add(ent);
 			}
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error reading.\n"+e.getMessage(), e);
 		}
 		finally {
 			if ( dis != null ) {
 				try {
 					dis.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException("Error closing file.", e);
 				}
 			}
 		}
 	}
 
 }
