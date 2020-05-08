 /*===========================================================================
   Copyright (C) 2008 by the Okapi Framework contributors
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
 ============================================================================*/
 
 package net.sf.okapi.common.resource;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * This class implements the methods for creating and manipulating a pre-parsed
  * flat representation of a content with in-line codes.
  * 
  * <p>The model uses two objects to store the data:
  * <ul><li>a coded text string
  * <li>a list of {@link Code} object.</ul>
  * 
  * <p>The coded text string is composed of normal characters and <b>markers</b>.
  * 
  * <p>A marker is a sequence of two special characters (in the Unicode PUA)
  * that indicate the type of underlying code (opening, closing, isolated), and an index
  * pointing to its corresponding Code object where more information can be found.
  * The value of the index is encoded as a Unicode PUA character. You can use the
  * {@link #toChar(int)} and {@link #toIndex(char)} methods to encoded and decode
  * the index value.
  * 
  * <p>To get the coded text of a TextFragment object use {@link #getCodedText()}, and
  * to get its list of codes use {@link #getCodes()}.
  * 
  * <p>You can modify directly the coded text or the codes and re-apply them to the
  * TextFragment object using {@link #setCodedText(String)} and
  * {@link #setCodedText(String, List)}.
  *
  * <p>Adding a code to the coded text can be done by:
  * <ul><li>appending the code with {@link #append(TagType, String, String)}
  * <li>changing a section of existing text to code with
  * {@link #changeToCode(int, int, TagType, String)}<ul>
  */
 public class TextFragment implements Comparable<Object> {
 	
 	public static final int MARKER_OPENING  = 0xE101;
 	public static final int MARKER_CLOSING  = 0xE102;
 	public static final int MARKER_ISOLATED = 0xE103;
 	public static final int MARKER_SEGMENT  = 0xE104;
 	public static final int CHARBASE        = 0xE110;
 
 	public static final String REFMARKER_START = "[#$";
 	public static final String REFMARKER_END   = "]";
 	public static final String REFMARKER_SEP   = "@%";
 
 	static public final String CODETYPE_SEGMENT  = "$seg$";
 
 	/**
 	 * List of the types of tag usable for in-line codes.
 	 */
 	public static enum TagType {
 		OPENING,
 		CLOSING,
 		PLACEHOLDER,
 		SEGMENTHOLDER
 	};
 	
 	protected StringBuilder text;
 	protected ArrayList<Code> codes;
 	protected boolean isBalanced;
 	protected int lastCodeID;
 
 	/**
 	 * Helper method to convert a marker index to its character value in the
 	 * coded text string.
 	 * @param index The index value to encode.
 	 * @return The corresponding character value.
 	 */
 	public static char toChar (int index) {
 		return (char)(index+CHARBASE);
 	}
 
 	/**
 	 * Helper method to convert the index-coded-as-character part of a marker into 
 	 * its index value.
 	 * @param index The character to decode.
 	 * @return The corresponding index value.
 	 */
 	public static int toIndex (char index) {
 		return ((int)index)-CHARBASE;
 	}
 	
 	public static String makeRefMarker (String id) {
 		return REFMARKER_START+id+REFMARKER_END;
 	}
 	
 	public static String makeRefMarker (String id,
 		String propertyName)
 	{
 		return REFMARKER_START+id+REFMARKER_SEP+propertyName+REFMARKER_END;
 	}
 	
 	public static Object[] getRefMarker (StringBuilder text) {
 		int start = text.indexOf(REFMARKER_START);
 		if ( start == -1 ) return null; // No marker
 		int end = text.indexOf(REFMARKER_END, start);
 		if ( end == -1 ) return null; // No ending found, we assume it's not a marker
 		String id = text.substring(start+REFMARKER_START.length(), end);
 		Object[] result = new Object[4];
 		result[1] = start;
 		result[2] = end+REFMARKER_END.length();
 		// Check for property name
 		int sep = id.indexOf(REFMARKER_SEP);
 		if ( sep > -1 ) {
 			String propName = id.substring(sep+REFMARKER_SEP.length());
 			id = id.substring(0, sep);
 			result[3] = propName;
 		}
 		// Else: result[3] is null: it's not a property marker
 		result[0] = id;
 		return result;
 	}
 
 	/**
 	 * Helper method to find, from the back, the first non-whitespace character
 	 * of a coded text, starting at a given position and no farther than another
 	 * given position.
 	 * @param codedText The coded text to process.
 	 * @param fromIndex The first position to check (must be greater or equal to
 	 * untilIndex). Use -1 to point to the last position of the text.
 	 * @param untilIndex The last position to check (must be lesser or equal to
 	 * fromIndex).
 	 * @param openingMarkerIsWS Indicates if opening markers count as whitespace.
 	 * @param closingMarkerIsWS Indicates if closing markers count as whitespace.
 	 * @param isolatedMarkerIsWS Indicates if isolated markers count as whitespace.
 	 * @param whitespaceIsWS Indicates if whitespace characters count as whitespace.
 	 * @return The first non-whitespace character position from the back, given the parameters.
 	 */
 	public static int getLastNonWhitespacePosition (String codedText,
 		int fromIndex,
 		int untilIndex,
 		boolean openingMarkerIsWS,
 		boolean closingMarkerIsWS,
 		boolean isolatedMarkerIsWS,
 		boolean whitespaceIsWS)
 	{
 		// Empty text
 		if (( codedText == null ) || ( codedText.length() == 0 )) return -1;
 		
 		// Set variables
 		if ( fromIndex == -1 ) fromIndex = codedText.length()-1;
 		int textEnd = fromIndex;
 		boolean done = false;
 
 		while ( !done ) {
 			switch ( codedText.charAt(textEnd) ) {
 			case TextFragment.MARKER_OPENING:
 				if ( !openingMarkerIsWS ) {
 					textEnd += 2;
 					done = true;
 				}
 				break;
 			case TextFragment.MARKER_CLOSING:
 				if ( !closingMarkerIsWS ) {
 					textEnd += 2;
 					done = true;
 				}
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
 				if ( !isolatedMarkerIsWS ) {
 					textEnd += 2;
 					done = true;
 				}
 				break;
 			default:
 				if ( whitespaceIsWS && Character.isWhitespace(codedText.charAt(textEnd)) ) break;
 				done = true; // Else: Probably done
 				// But check if it's the index of a marker
 				if ( textEnd > 1 ) {
 					switch ( codedText.charAt(textEnd-1) ) {
 					case TextFragment.MARKER_OPENING:
 					case TextFragment.MARKER_CLOSING:
 					case TextFragment.MARKER_ISOLATED:
 					case TextFragment.MARKER_SEGMENT:
 						done = false; // Not done yet
 						break;
 					}
 				}
 				break;
 			}
 			if ( !done ) {
 				if ( textEnd-1 < untilIndex ) break;
 				textEnd--;
 			}
 		}
 		return textEnd;
 	}
 
 	/**
 	 * Helper method to find the first non-whitespace character
 	 * of a coded text, starting at a given position and no farther than another
 	 * given position.
 	 * @param codedText The coded text to process.
 	 * @param fromIndex The first position to check (must be lesser or equal to
 	 * untilIndex).
 	 * @param untilIndex The last position to check (must be greater or equal to
 	 * fromIndex). Use -1 to point to the last position of the text.
 	 * @param openingMarkerIsWS Indicates if opening markers count as whitespace.
 	 * @param closingMarkerIsWS Indicates if closing markers count as whitespace.
 	 * @param isolatedMarkerIsWS Indicates if isolated markers count as whitespace.
 	 * @param whitespaceIsWS Indicates if whitespace characters count as whitespace.
 	 * @return The first non-whitespace character position, given the parameters.
 	 */
 	public static int getFirstNonWhitespacePosition (String codedText,
 		int fromIndex,
 		int untilIndex,
 		boolean openingMarkerIsWS,
 		boolean closingMarkerIsWS,
 		boolean isolatedMarkerIsWS,
 		boolean whitespaceIsWS)
 	{
 		// Empty text
 		if (( codedText == null ) || ( codedText.length() == 0 )) return -1;
 		
 		// Set variables
 		if ( untilIndex == -1 ) untilIndex = codedText.length()-1;
 		int textStart = fromIndex;
 		boolean done = false;
 
 		while ( !done ) {
 			switch ( codedText.charAt(textStart) ) {
 			case TextFragment.MARKER_OPENING:
 				if ( openingMarkerIsWS ) textStart++;
 				else done = true;
 				break;
 			case TextFragment.MARKER_CLOSING:
 				if ( closingMarkerIsWS ) textStart++;
 				else done = true;
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
 				if ( isolatedMarkerIsWS ) textStart++;
 				else done = true;
 				break;
 			default:
 				if ( whitespaceIsWS
 					&& Character.isWhitespace(codedText.charAt(textStart)) ) break;
 				done = true;
 				break;
 			}
 			if ( !done ) {
 				if ( textStart == untilIndex ) break;
 				else textStart++;
 			}
 		}
 		return textStart;
 	}
 
 	/**
 	 * Creates an empty TextFragment with a given parent.
 	 * @param parent The parent of this TextFragment. You can use a null parent,
 	 * but then you won't be able to use in-line codes with references.
 	 */
 	public TextFragment () {
 		//this.parent = parent;
 		text = new StringBuilder();
 	}
 
 	/**
 	 * Creates a TextFragment with a given text.
 	 * @param parent The parent of this TextFragment. You can use a null parent,
 	 * but then you won't be able to use in-line codes with references.
 	 * @param text The text to use.
 	 */
 	public TextFragment (String text) {
 		this.text = new StringBuilder((text==null) ? "" : text);
 	}
 
 	/**
 	 * Creates a TextFragment with the content of a given TextFragment.
 	 * @param parent The parent of this TextFragment. You can use a null parent,
 	 * but then you won't be able to use in-line codes with references.
 	 * @param fragment The content to use.
 	 */
 	public TextFragment (TextFragment fragment) {
 		//this.parent = parent;
 		text = new StringBuilder();
 		insert(-1, fragment);
 	}
 	
 	/**
 	 * Creates a TextFragment with the content made of a given coded text
 	 * and a list of codes.
 	 * @param parent The parent of this TextFragment. You can use a null parent,
 	 * but then you won't be able to use in-line codes with references.
 	 * @param newCodedText The new coded text.
 	 * @param newCodes The list of codes.
 	 */
 	public TextFragment (String newCodedText,
 		List<Code> newCodes)
 	{
 		//this.parent = parent;
 		setCodedText(newCodedText, newCodes, false);
 	}
 	
 	@Override
 	public TextFragment clone () {
 		TextFragment tf = new TextFragment();
 		tf.setCodedText(getCodedText(), getCodes(), false);
 		tf.lastCodeID = lastCodeID;
 		return tf;
 	}
 	
 	public boolean hasReference () {
 		if ( codes == null ) return false;
 		for ( Code code : codes ) {
 			if ( code.hasReference ) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Appends a character to the fragment.
 	 * @param value The character to append.
 	 */
 	public void append (char value) {
 		text.append(value);
 	}
 
 	/**
 	 * Appends a string to the fragment. If the string is null, it is ignored.
 	 * @param text The string to append.
 	 */
 	public void append (String text) {
 		if ( text == null ) return;
 		this.text.append(text);
 	}
 
 	/**
 	 * Appends a TextFragment object to this fragment. If the fragment is null,
 	 * it is ignored.
 	 * @param fragment The TextFragment to append.
 	 */
 	public void append (TextFragment fragment) {
 		insert(-1, fragment);
 	}
 	
 	/**
 	 * Helper method to add existing code.
 	 */
 	public Code append (Code code) {
		if ( codes == null ) codes = new ArrayList<Code>();
		// Append the code marker
		switch ( code.tagType ) {
		case OPENING:
			append(""+((char)MARKER_OPENING)+toChar(codes.size()));
			break;
		case CLOSING:
			append(""+((char)MARKER_CLOSING)+toChar(codes.size()));
			break;
		case PLACEHOLDER:
			append(""+((char)MARKER_ISOLATED)+toChar(codes.size()));
			break;
		case SEGMENTHOLDER:
			append(""+((char)MARKER_SEGMENT)+toChar(codes.size()));
			break;
		}
		// Create the code
		codes.add(code); //Code newCode = code.clone();
		if ( code.tagType != TagType.CLOSING ) codes.get(codes.size()-1).id = ++lastCodeID;
		if (( code.tagType != TagType.PLACEHOLDER )
			&& ( code.tagType != TagType.SEGMENTHOLDER )) isBalanced = false;
		return code;
 	}
 	
 	/**
 	 * Appends a new code to the text.
 	 * @param tagType The tag type of the code (e.g. TagType.OPENING).
 	 * @param type The type of the code (e.g. "bold").
 	 * @param data The raw code itself. (e.g. "&lt;b>").
 	 * @return The new code that was added to the text.
 	 */
 	public Code append (TagType tagType,
 		String type,
 		String data)
 	{
 		// Create the list of codes if needed
 		if ( codes == null ) codes = new ArrayList<Code>();
 		// Append the code marker
 		switch ( tagType ) {
 		case OPENING:
 			append(""+((char)MARKER_OPENING)+toChar(codes.size()));
 			break;
 		case CLOSING:
 			append(""+((char)MARKER_CLOSING)+toChar(codes.size()));
 			break;
 		case PLACEHOLDER:
 			append(""+((char)MARKER_ISOLATED)+toChar(codes.size()));
 			break;
 		case SEGMENTHOLDER:
 			append(""+((char)MARKER_SEGMENT)+toChar(codes.size()));
 			break;
 		}
 		// Create the code
 		codes.add(new Code(tagType, type, data));
 		if ( tagType != TagType.CLOSING ) codes.get(codes.size()-1).id = ++lastCodeID;
 		if (( tagType != TagType.PLACEHOLDER )
 			&& ( tagType != TagType.SEGMENTHOLDER )) isBalanced = false;
 		return codes.get(codes.size()-1);
 	}
 
 	/**
 	 * Inserts a TextFragment object to this fragment.
 	 * @param offset Position in the coded text where to insert the new fragment.
 	 * You can use -1 to append at the end of the current content.
 	 * @param fragment The TextFragment to insert.
 	 * @throws InvalidPositionException When offset points inside a marker.
 	 */
 	public void insert (int offset,
 		TextFragment fragment)
 	{
 		if ( fragment == null ) return;
 		checkPositionForMarker(offset);
 		StringBuilder tmp = new StringBuilder(fragment.getCodedText());
 		List<Code> newCodes = fragment.getCodes();
 		if (( codes == null ) && ( newCodes.size() > 0 )) {
 			codes = new ArrayList<Code>();
 		}
 
 		// Update the coded text to use new code indices
 		for ( int i=0; i<tmp.length(); i++ ) {
 			switch ( tmp.charAt(i) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				codes.add(newCodes.get(toIndex(tmp.charAt(++i))).clone());
 				tmp.setCharAt(i, toChar(codes.size()-1));
 				break;
 			}
 		}
 
 		// Insert the new text in one chunk
 		if ( offset < 0 ) text.append(tmp);
 		else text.insert(offset, tmp);
 		// If there was new codes we will need to re-balance
 		if ( newCodes.size() > 0 ) isBalanced = false;
 	}
 
 	/**
 	 * Clears the fragment of all content. The parent is not modified.
 	 */
 	public void clear () {
 		text = new StringBuilder();
 		codes = null;
 		lastCodeID = 0;
 		isBalanced = true;
 	}
 
 	/**
 	 * Gets the coded text representation of the fragment.
 	 * @return The coded text for the fragment.
 	 */
 	public String getCodedText () {
 		if ( !isBalanced ) balanceMarkers();
 		return text.toString();
 	}
 
 	/**
 	 * Gets the portion of coded text for a given section of the coded text.
 	 * @param start The position of the first character or marker of the section
 	 * (in the coded text representation).
 	 * @param end The position just after the last character or marker of the section
 	 * (in the coded text representation).
 	 * You can use -1 for ending the section at the end of the fragment.
 	 * @return The portion of coded text for the given range. It can be 
 	 * empty but never null.
 	 * @throws InvalidPositionException When start or end points inside a marker.
 	 */
 	public String getCodedText (int start,
 		int end)
 	{
 		if ( end == -1 ) end = text.length();
 		checkPositionForMarker(start);
 		checkPositionForMarker(end);
 		if ( !isBalanced ) balanceMarkers();
 		return text.substring(start, end);
 	}
 
 	/**
 	 * Gets the code for a given index formatted as character (the second
 	 * special character in a marker in a coded text string).
 	 * @param indexAsChar The index value coded as character.
 	 * @return The corresponding code.
 	 */
 	public Code getCode (char indexAsChar) {
 		return codes.get(toIndex(indexAsChar)); 
 	}
 	
 	/**
 	 * Gets the code for a given index.
 	 * @param index the index of the code.
 	 * @return The code for the given index.
 	 */
 	public Code getCode (int index) {
 		return codes.get(index);
 	}
 	
 	/**
 	 * Gets the list of all codes for the fragment.
 	 * @return The list of all codes for the fragment. If there is no code, an empty
 	 * list is returned.
 	 */
 	public List<Code> getCodes () {
 		if ( codes == null ) codes = new ArrayList<Code>();
 		if ( !isBalanced ) balanceMarkers();
 		return Collections.unmodifiableList(codes);
 	}
 
 	/**
 	 * Gets a copy of the list of the codes that are within a given section of
 	 * coded text.
 	 * @param start The position of the first character or marker of the section
 	 * (in the coded text representation).
 	 * @param end The position just after the last character or marker of the section
 	 * (in the coded text representation).
 	 * @return A new list of all codes within the given range.
 	 * @throws InvalidPositionException When start or end points inside a marker.
 	 */
 	public List<Code> getCodes (int start,
 		int end)
 	{
 		ArrayList<Code> tmpCodes = new ArrayList<Code>();
 		if ( codes == null ) return tmpCodes;
 		if ( codes.isEmpty() ) return tmpCodes;
 		checkPositionForMarker(start);
 		checkPositionForMarker(end);
 
 		for ( int i=start; i<end; i++ ) {
 			switch ( text.charAt(i) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				Code ori = codes.get(toIndex(text.charAt(++i)));
 				tmpCodes.add(ori.clone());
 				break;
 			}
 		}
 	
 		return tmpCodes;
 	}
 	
 	/**
 	 * Gets the index value for the first in-line code (in the codes list)
 	 * with a given ID.
 	 * @param id The ID to look for.
 	 * @return The index of the found code, or -1 if none is found. 
 	 */
 	public int getIndex (int id) {
 		if ( codes == null ) return -1;
 		int i = 0;
 		for ( Code code : codes ) {
 			if ( code.id == id ) return i;
 			i++;
 		}
 		return -1;
 	}
 
 	/**
 	 * Indicates if the fragment is empty (no text and no codes).
 	 * @return True if the fragment is empty.
 	 */
 	public boolean isEmpty () {
 		return (text.length()==0);
 	}
 	
 	/**
 	 * Indicates if the fragment contains at least one character (markers do not
 	 * count as characters).
 	 * @param whiteSpacesAreText Indicates if white-spaces should be considered 
 	 * characters or not for the purpose of checking if this fragment is empty.
 	 * @return True if the fragment contains at least one character.
 	 */
 	public boolean hasText (boolean whiteSpacesAreText) {
 		for ( int i=0; i<text.length(); i++ ) {
 			switch (text.charAt(i)) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				i++; // Skip over the marker, they are not text
 				continue;
 			}
 			// Not a marker
 			// If we count ws as text, then we have text
 			if ( whiteSpacesAreText ) return true;
 			// Otherwise we have text if it's not a whitespace
 			if ( !Character.isWhitespace(text.charAt(i)) ) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Indicates if the fragment contains at least one code.
 	 * @return True if the fragment contains at least one code.
 	 */
 	public boolean hasCode () {
 		if ( codes == null ) return false;
 		return (codes.size()>0);
 	}
 
 	/**
 	 * Removes a section of the fragment (including its codes).
 	 * @param start The position of the first character or marker of the section
 	 * (in the coded text representation).
 	 * @param end The position just after the last character or marker of the section
 	 * (in the coded text representation).
 	 * @throws InvalidPositionException When start or end points inside a marker.
 	 */
 	public void remove (int start,
 		int end)
 	{
 		// TODO: Check if there is a better way to do this,
 		// as this is quite expensive.
 		checkPositionForMarker(start);
 		checkPositionForMarker(end);
 		// Remove the coded text to delete
 		text.replace(start, end, "");
 		if (( codes == null ) || ( codes.size()==0 )) return;
 		// Make a list of all remaining codes
 		ArrayList<Code> remaining = new ArrayList<Code>();
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				// Copy the remaining codes into the new list
 				remaining.add(codes.get(toIndex(text.charAt(++i))));
 				// And update the index in the coded text
 				text.setCharAt(i, toChar(remaining.size()-1));
 				break;
 			}
 		}
 		codes.clear();
 		codes = remaining; // The new list is the remaining codes
 		isBalanced = false;
 	}
 
 	/**
 	 * Gets a copy of a sub-sequence of this object.
 	 * @param start The position of the first character or marker of the section
 	 * (in the coded text representation).
 	 * @param end The position just after the last character or marker of the section
 	 * (in the coded text representation).
 	 * You can use -1 for ending the section at the end of the fragment.
 	 * @return A new TextContainer object with a copy of the given sub-sequence.
 	 */
 	public TextFragment subSequence (int start,
 		int end)
 	{
 		TextFragment sub = new TextFragment();
 		if ( isEmpty() ) return sub;
 		StringBuilder tmpText = new StringBuilder(getCodedText(start, end));
 		ArrayList<Code> tmpCodes = null;
 	
 		// Get the codes and adjust indices if needed
 		if (( codes != null ) && ( codes.size() > 0 )) {
 			tmpCodes = new ArrayList<Code>(); 
 			for ( int i=0; i<tmpText.length(); i++ ) {
 				switch ( tmpText.charAt(i) ) {
 				case MARKER_OPENING:
 				case MARKER_CLOSING:
 				case MARKER_ISOLATED:
 				case MARKER_SEGMENT:
 					tmpCodes.add(codes.get(toIndex(tmpText.charAt(++i))).clone());
 					tmpText.setCharAt(i, toChar(tmpCodes.size()-1));
 					break;
 				}
 			}
 		}
 		sub.setCodedText(tmpText.toString(), tmpCodes, false);
 		sub.lastCodeID = lastCodeID;
 		return sub;
 	}
 	
 	/**
 	 * Sets the coded text of the fragment, using its the existing codes. The coded
 	 * text must be valid for the existing codes.
 	 * @param newCodedText The coded text to apply.
 	 * @throws InvalidContentException When the coded text is not valid, or does
 	 * not correspond to the existing codes.
 	 */
 	public void setCodedText (String newCodedText)
 	{
 		setCodedText(newCodedText, codes, false);
 	}
 
 	/**
 	 * Sets the coded text of the fragment, using its the existing codes. The coded
 	 * text must be valid for the existing codes.
 	 * @param newCodedText The coded text to apply.
 	 * @param allowCodeDeletion True when missing in-line codes in the coded text
 	 * means the corresponding codes should be deleted from the fragment.
 	 * @throws InvalidContentException When the coded text is not valid, or does
 	 * not correspond to the existing codes.
 	 */
 	public void setCodedText (String newCodedText,
 		boolean allowCodeDeletion)
 	{
 		setCodedText(newCodedText, codes, allowCodeDeletion);
 	}
 
 	/**
 	 * Sets the coded text of the fragment and its corresponding codes.
 	 * @param newCodedText The coded text to apply.
 	 * @param newCodes The list of the corresponding codes.
 	 * @throws InvalidContentException When the coded text is not valid or does 
 	 * not correspond to the new codes.
 	 */
 	public void setCodedText (String newCodedText,
 		List<Code> newCodes)
 	{
 		setCodedText(newCodedText, newCodes, false);
 	}
 	
 	/**
 	 * Sets the coded text of the fragment and its corresponding codes.
 	 * @param newCodedText The coded text to apply.
 	 * @param newCodes The list of the corresponding codes.
 	 * @param allowCodeDeletion True when missing in-line codes in the coded text
 	 * means the corresponding codes should be deleted from the fragment.
 	 * @throws InvalidContentException When the coded text is not valid or does 
 	 * not correspond to the new codes.
 	 */
 	public void setCodedText (String newCodedText,
 		List<Code> newCodes,
 		boolean allowCodeDeletion)
 	{
 		isBalanced = false;
 		text = new StringBuilder(newCodedText);
 		if ( newCodes == null ) codes = null;
 		else codes = new ArrayList<Code>(newCodes);
 		if (( codes == null ) || ( codes.size() == 0 )) {
 			lastCodeID = 0;
 			return; // No codes, all done.
 		}
 		//TODO: do we need to reset the lastCodeID?
 		ArrayList<Code> activeCodes = new ArrayList<Code>();
 		
 		// Validate the codes and coded text
 		int j = 0;
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				j++;
 				if ( codes == null ) {
 					throw new InvalidContentException("Invalid index for marker "+j);
 				}
 				try {
 					// Just try to access the code
 					activeCodes.add(codes.get(toIndex(text.charAt(++i))));
 				}
 				catch ( IndexOutOfBoundsException e ) {
 					throw new InvalidContentException("Invalid index for marker "+j);
 				}
 				break;
 			}
 		}
 		
 		if ( allowCodeDeletion ) {
 			codes.retainAll(activeCodes);
 		}
 		else { // No deletion allowed: check the numbers
 			if ( j > 0 ) {
 				if (( codes == null ) || ( j < codes.size() )) {
 					throw new InvalidContentException(
 						String.format("Markers in coded text: %d. Listed codes: %d ", j, codes.size()));
 				}
 			}
 		}
 	}
 	
 	@Override
 	public String toString () {
 		if (( codes == null ) || ( codes.size() == 0 )) return text.toString();
 		if ( !isBalanced ) balanceMarkers();
 		StringBuilder tmp = new StringBuilder();
 		Code code;
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
 			case MARKER_OPENING:
 				code = codes.get(toIndex(text.charAt(++i)));
 				tmp.append(code.data);
 				break;
 			case MARKER_CLOSING:
 				code = codes.get(toIndex(text.charAt(++i)));
 				tmp.append(code.data);
 				break;
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				code = codes.get(toIndex(text.charAt(++i)));
 				tmp.append(code.data);
 				break;
 			default:
 				tmp.append(text.charAt(i));
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 
 	/**
 	 * Compares and object with this TextFragment. If the object is also a TextFragment,
 	 * the methods returns the comparison between the coded text string of both TextFragments.
 	 * If the object is not a TextFragment, the method returns the comparison between the two
 	 * toString() results of the two objects.
 	 * @return A value 0 if the objects are the same.
 	 */
 	public int compareTo (Object object) {
 		if ( object == null ) return -1;
 		if ( object instanceof TextFragment ) {
 			return getCodedText().compareTo(((TextFragment)object).getCodedText());
 		}
 		// Else, compare string representation
 		return toString().compareTo(object.toString());
 	}
 
 	@Override
 	public boolean equals (Object object) {
 		if ( object == null ) return false;
 		return (compareTo(object)==0);
 	}
 
 	/**
 	 * Changes a section of the coded text into a single code. Any code already
 	 * existing that is within the range will be included in the new code.
 	 * @param start The position of the first character or marker of the section
 	 * (in the coded text representation).
 	 * @param end The position just after the last character or marker of the section
 	 * (in the coded text representation).
 	 * @param tagType Tag type of the new code.
 	 * @param type Type of the new code.
 	 * @return The different between the coded text length before and after 
 	 * the operation. This value can be used to adjust further start and end positions
 	 * that have been calculated on the coded text before the changes are applied.
 	 * @throws InvalidPositionException When start or end points inside a marker.
 	 */
 	public int changeToCode (int start,
 		int end,
 		TagType tagType,
 		String type)
 	{
 		// Get the subsequence
 		TextFragment sub = subSequence(start, end);
 		// Store the length of the coded text before the operation
 		int before = text.length();
 		// Create the new code, using the text of the subsequence as the data
 		Code code = new Code(tagType, type, sub.toString());
 		if ( codes == null ) codes = new ArrayList<Code>();
 		// Remove the section that will be code, this takes care of the codes too
 		remove(start, end);
 		// Create the new marker
 		String marker = null;
 		switch ( tagType ) {
 		case OPENING:
 			marker = ""+((char)MARKER_OPENING)+toChar(codes.size());
 			code.id = ++lastCodeID;
 			break;
 		case CLOSING:
 			marker = ""+((char)MARKER_CLOSING)+toChar(codes.size());
 			// The id stays -1
 			break;
 		case PLACEHOLDER:
 			marker = ""+((char)MARKER_ISOLATED)+toChar(codes.size());
 			code.id = ++lastCodeID;
 			break;
 		case SEGMENTHOLDER:
 			marker = ""+((char)MARKER_SEGMENT)+toChar(codes.size());
 			code.id = ++lastCodeID;
 			break;
 		}
 		// Insert the new marker into the coded text
 		text.insert(start, marker);
 		// Add the new code
 		codes.add(code);
 		isBalanced = false;
 		return text.length()-before;
 	}
 
 	/**
 	 * Renumbers the IDs of the codes in the fragment.
 	 */
 	public void renumberCodes () {
 		lastCodeID = 0;
 		if ( codes == null ) return;
 		for ( Code code : codes ) {
 			if ( code.tagType != TagType.CLOSING ) code.id = ++lastCodeID;
 		}
 		isBalanced = false;
 	}
 	
 	/**
 	 * Verifies if a given position in the coded text is on the second special
 	 * character of a marker sequence.
 	 * @param position The position to text.
 	 * @throws InvalidPositionException When position points inside a marker.
 	 */
 	private void checkPositionForMarker (int position) {
 		if ( position > 0 ) {
 			switch ( text.charAt(position-1) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				throw new InvalidPositionException (
 					String.format("Position %d is inside a marker.", position));
 			}
 		}
 	}
 	
 	/**
 	 * Balances the markers based on the tag type of the codes.
 	 */
 	private void balanceMarkers () {
 		if ( codes == null ) return;
 		for ( Code item : codes ) {
 			// Void all IDs of closing codes
 			if ( item.tagType == TagType.CLOSING ) item.id = -1;
 		}
 		// Process the markers
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
 			case MARKER_OPENING:
 			case MARKER_CLOSING:
 			case MARKER_ISOLATED:
 			case MARKER_SEGMENT:
 				int index = toIndex(text.charAt(i+1));
 				//TODO: fix this: index is not codes sequence, so the loop to find the sibling close may not work
 				// because it start after the code (e.g. if the code was inserted after)
 				Code code = codes.get(index);
 				switch ( code.tagType ) {
 				case SEGMENTHOLDER:
 					text.setCharAt(i, (char)MARKER_SEGMENT);
 					break;
 				case PLACEHOLDER:
 					text.setCharAt(i, (char)MARKER_ISOLATED);
 					break;
 				case OPENING:
 					// Search for corresponding closing code
 					boolean found = false;
 					int stack = 1;
 					for ( int j=index+1; j<codes.size(); j++ ) {
 						if ( codes.get(j).type.equals(code.type) ) {
 							if ( codes.get(j).tagType == TagType.OPENING ) {
 								stack++;
 							}
 							else if ( codes.get(j).tagType == TagType.CLOSING ) {
 								if ( --stack == 0 ) {
 									codes.get(j).id = code.id;
 									found = true;
 									break;
 								}
 							}
 						}
 					}
 					if ( found ) text.setCharAt(i, (char)MARKER_OPENING);
 					else text.setCharAt(i, (char)MARKER_ISOLATED);
 					break;
 				case CLOSING:
 					// If Id is -1, this closing code has no corresponding opening
 					// otherwise its ID is already set
 					if ( code.id == -1 ) {
 						text.setCharAt(i, (char)MARKER_ISOLATED);
 						code.id = ++lastCodeID;
 					}
 					else text.setCharAt(i, (char)MARKER_CLOSING);
 				}
 				i++; // Skip index part of the index
 				break;
 			}
 		}
 		isBalanced = true;
 	}
 
 }
