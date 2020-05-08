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
 
 package net.sf.okapi.filters.common.utils;
 
 import java.util.List;
 
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 
 /**
  * 
  * 
  * @version 0.1, 09.06.2009
  */
 
 public class TextUnitUtils {
 	
 	/**
 	 * 
 	 * @param textFragment
 	 */
 	public static void trimLeading(TextFragment textFragment) {
 		trimLeading(textFragment, null);
 	}
 	
 	/**
 	 * 
 	 * @param textFragment
 	 * @param skel
 	 */
 	public static void trimLeading(TextFragment textFragment, GenericSkeleton skel) {
 		
 		if (textFragment == null) return;
 		String st = textFragment.getCodedText();
 		TextFragment skelTF;
 		
 		int pos = TextFragment.indexOfFirstNonWhitespace(st, 0, -1, true, true, true, true);		
 		if (pos == -1) { // Whole string is whitespaces
 			skelTF = new TextFragment(st);
 			textFragment.setCodedText("");			
 		}
 		else {
 			skelTF = textFragment.subSequence(0, pos);
 			textFragment.setCodedText(st.substring(pos));			
 		}
 			
 		if (skel == null) return;
 		if (skelTF == null) return;
 		
 		skel.append(skelTF.toString());  // Codes get removed
 	}
 	
 	/**
 	 * 
 	 * @param textFragment
 	 */
 	public static void trimTrailing(TextFragment textFragment) {
 		trimTrailing(textFragment, null);
 	}
 	
 	/**
 	 * 
 	 * @param textFragment
 	 * @param skel
 	 */
 	public static void trimTrailing(TextFragment textFragment, GenericSkeleton skel) {
 		
 		if (textFragment == null) return;
 		
 		String st = textFragment.getCodedText();
 		TextFragment skelTF;
 		
 		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
 		if (pos == -1) { // Whole string is whitespaces
 			skelTF = new TextFragment(st);
 			textFragment.setCodedText("");			
 		}
 		else {
 			skelTF = textFragment.subSequence(pos + 1, st.length());
 			textFragment.setCodedText(st.substring(0, pos + 1));			
 		}
 						
 		if (skel == null) return;
 		if (skelTF == null) return;
 		
 		skel.append(skelTF.toString());  // Codes get removed);
 	}
 
 	/**
 	 * !!! Trailing spaces are not counted. 
 	 * @param textFragment
 	 * @param substr
 	 * @return
 	 */
 	public static boolean endsWith(TextFragment textFragment, String substr) {
 		
 		if (textFragment == null) return false;
 		if (Util.isEmpty(substr)) return false;
 		
 		String st = textFragment.getCodedText();
 		
 		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
 		if (pos == -1) return false;
 		
 		return st.lastIndexOf(substr) == pos - substr.length() + 1;
 	}
 
 	public static boolean isEmpty(TextUnit textUnit) {
 		
 		return ((textUnit == null) || Util.isEmpty(getSourceText(textUnit)));
 	}
 	
 	public static boolean hasSource(TextUnit textUnit) {
 		
 		return !isEmpty(textUnit, true);
 	}
 	
 	public static boolean isEmpty(TextUnit textUnit, boolean ignoreWS) {
 		
 		return ((textUnit == null) || Util.isEmpty(getSourceText(textUnit), ignoreWS));
 	}
 	
 	public static String getSourceText(TextUnit textUnit) {
 		
 		if (textUnit == null) return "";
 		
 		return getCodedText(textUnit.getSourceContent());
 	}
 	
 	public static String getTargetText(TextUnit textUnit, String language) {
 		
 		if (textUnit == null) return "";
 		
 		return getCodedText(textUnit.getTargetContent(language));
 	}
 	
 	public static String getCodedText(TextFragment textFragment) {
 		
 		if (textFragment == null) return "";
 		
 		return textFragment.getCodedText();
 	}
 	
 	/**
 	 * Extracts text from the given text fragment. Used to create a copy of the original string but without code markers.
 	 * The original string is not stripped of code markers, and remains intact.
 	 * @param textFragment TextFragment object with possible codes inside
 	 * @param markerPositions List to store initial positions of removed code markers 
 	 * @return The copy of the string, contained in TextFragment, but w/o code markers
 	 */
 	public static String getText(TextFragment textFragment, List<Integer> markerPositions) {		
 		
 		if (textFragment == null) return "";
 				
 		String res = textFragment.getCodedText();
 		
 		StringBuilder sb = new StringBuilder();
 		
 		if (markerPositions != null) 			
 			markerPositions.clear();
 			
 			// Collect marker positions & remove markers			
 			int startPos = 0;
 			
 			for (int i = 0; i < res.length(); i++) {
 				
 				switch (res.charAt(i) ) {
 				
 				case TextFragment.MARKER_OPENING:
 				case TextFragment.MARKER_CLOSING:
 				case TextFragment.MARKER_ISOLATED:
 				case TextFragment.MARKER_SEGMENT:
 				
 					if (markerPositions != null)
 						markerPositions.add(i);
 				
 					if (i > startPos)
 						sb.append(res.substring(startPos, i));
 					
 					startPos = i + 2;
 					i = startPos;
 				}
 			
 			}
 				
 		return sb.toString();
 	}
 	
 	/**
 	 * Extracts text from the given text fragment. Used to create a copy of the original string but without code markers.
 	 * The original string is not stripped of code markers, and remains intact.
 	 * @param textFragment TextFragment object with possible codes inside
 	 * @return The copy of the string, contained in TextFragment, but w/o code markers
 	 */
 	public static String getText(TextFragment textFragment) {
 		
 		return getText(textFragment, null);
 	}
 	
 	/**
 	 * 
 	 * @param textFragment
 	 * @return
 	 */
 	public static char getLastChar(TextFragment textFragment) {
 		
 		if (textFragment == null) return '\0';
 		
 		String st = textFragment.getCodedText();
 		
 		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
 		if (pos == -1) return '\0';
 		
 		return st.charAt(pos);
 	}
 
 	/**
 	 * 
 	 * @param textFragment
 	 */
 	public static void deleteLastChar(TextFragment textFragment) {
 		
 		if (textFragment == null) return;
 		String st = textFragment.getCodedText();
 		
 		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
 		if (pos == -1) return;
 		
 		textFragment.remove(pos, pos + 1);
 	}
 		
 	/**
 	 * 
 	 * @param textFragment
 	 * @param findWhat
 	 * @return
 	 */
 	public static int lastIndexOf(TextFragment textFragment, String findWhat) {
 		
 		if (textFragment == null) return -1;
 		if (Util.isEmpty(findWhat)) return -1;
 		if (Util.isEmpty(textFragment.getCodedText())) return -1;
 		
 		return (textFragment.getCodedText()).lastIndexOf(findWhat);
 	}
 				
 	public static boolean isEmpty(TextFragment textFragment) {
 		
 		return (textFragment == null || (textFragment != null && textFragment.isEmpty()));		
 	}
 	
 	public static TextUnit buildTU (TextContainer source) {
 			
 		return buildTU(null, "", source, null, "", "");
 	}
 	
 	public static TextUnit buildTU (String source) {
 		
 		return buildTU(new TextContainer(source));
 	}
 	
 	/**
 	 * @param srcPart
 	 * @param skelPart
 	 * @return
 	 */
 	public static TextUnit buildTU(String srcPart, String skelPart) {
 		
 		TextUnit res = buildTU(srcPart);
 		if (res == null) return null;
 		
 		GenericSkeleton skel = (GenericSkeleton) res.getSkeleton();
 		if (skel == null) return null;
 				
 		skel.addContentPlaceholder(res);
 		skel.append(skelPart);
 		
 		return res;
 	}	
 	
 	public static TextUnit buildTU(
 			TextUnit textUnit, 
 			String name, 
 			TextContainer source, 
 			TextContainer target, 
 			String language, 
 			String comment) {
 		
 		if (textUnit == null) {
 			
 			textUnit = new TextUnit("");			
 		}
 		
 		if (textUnit.getSkeleton() == null) {
 			
 			GenericSkeleton skel = new GenericSkeleton();
 			textUnit.setSkeleton(skel);
 		}		
 		
 		if (!Util.isEmpty(name))
 			textUnit.setName(name);
 		
 		if (source != null)
 			textUnit.setSource(source);
 		
 		if (target != null && !Util.isEmpty(language))
 			textUnit.setTarget(language, target);
 		
		if (comment != null)
 			textUnit.setProperty(new Property(Property.NOTE, comment));
 		
 		return textUnit;
 	}
 
 	public static GenericSkeleton forseSkeleton(TextUnit tu) {
 		
 		if (tu == null) return null;
 		
 		ISkeleton res = tu.getSkeleton();
 		if (res == null) {
 			
 			res = new GenericSkeleton();
 			tu.setSkeleton(res);
 		}
 		
 		return (GenericSkeleton) res;
 	}
 
 	/**
 	 * 
 	 * @param textUnit
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static GenericSkeleton convertToSkeleton(TextUnit textUnit) {
 		
 		if (textUnit == null) return null;
 		
 		GenericSkeleton skel = (GenericSkeleton) textUnit.getSkeleton();
 		
 		List<?> temp = skel.getParts();
 		List<Object> list = (List<Object>) temp;
 		
 		String tuRef = TextFragment.makeRefMarker("$self$");
 		String srcText = TextUnitUtils.getSourceText(textUnit);
 				
 		GenericSkeleton res = new GenericSkeleton();
 		
 		List<?> temp2 = res.getParts();
 		List<Object> list2 = (List<Object>) temp2;
 		
 		for (int i = 0; i < list.size(); i++) {
 			
 			Object obj = list.get(i);
 			if (obj == null) continue;
 			String st = obj.toString();
 			
 			if (Util.isEmpty(st)) continue;
 			if (st.equalsIgnoreCase(tuRef)) {
 				
 				res.add(srcText);
 				continue;
 			}
 			
 			list2.add(list.get(i));			
 		}
 		
 		return res;
 	}
 }
