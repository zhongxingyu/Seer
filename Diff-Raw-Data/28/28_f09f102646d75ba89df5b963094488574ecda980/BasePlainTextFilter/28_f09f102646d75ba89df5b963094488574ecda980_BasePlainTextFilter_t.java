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
 
 package net.sf.okapi.filters.plaintext.base;
 
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.ListUtils;
 import net.sf.okapi.filters.plaintext.common.AbstractLineFilter;
 import net.sf.okapi.filters.plaintext.common.TextProcessingResult;
 import net.sf.okapi.filters.plaintext.common.TextUnitUtils;
 
 /**
  * <code>PlainTextFilter</code> extracts lines of input text, separated by line terminators.
  * 
  * @version 0.1, 09.06.2009
  */
 
 public class BasePlainTextFilter extends AbstractLineFilter {	
 	
 	public static final String FILTER_NAME				= "okf_plaintext";
 	public static final String FILTER_MIME				= MimeTypeMapper.PLAIN_TEXT_MIME_TYPE;	
 	public static final String FILTER_CONFIG			= "okf_plaintext";
 	public static final String FILTER_CONFIG_TRIM_TRAIL	= "okf_plaintext_trim_trail";
 	public static final String FILTER_CONFIG_TRIM_ALL	= "okf_plaintext_trim_all";
 	
 	private Parameters params; // Base Plain Text Filter parameters
 	private InlineCodeFinder codeFinder;
 		
 //	protected void component_create() {
 	public BasePlainTextFilter() {
 		
 		codeFinder = new InlineCodeFinder();
 		
 		setName(FILTER_NAME);
 		setMimeType(FILTER_MIME);
 		
 		addConfiguration(true, 
 				FILTER_CONFIG,
 				"Plain Text",
				"Plain text files.", 
 				null);
 		
 		addConfiguration(false, 
 				FILTER_CONFIG_TRIM_TRAIL,
 				"Plain Text (Trim Trail)",
				"Text files; trailing spaces and tabs removed from extracted lines.", 
 				"okf_plaintext_trim_trail.fprm");
 		
 		addConfiguration(false, 
 				FILTER_CONFIG_TRIM_ALL,
 				"Plain Text (Trim All)",
				"Text files; leading and trailing spaces and tabs removed from extracted lines.", 
 				"okf_plaintext_trim_all.fprm");
 		
 		setParameters(new Parameters());	// Base Plain Text Filter parameters
 	}
 	
 	@Override
 	protected void component_init() {
 		
 		// Commons, should be included in all descendants introducing own params
 		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException		
 		
 		// Initialization
 		if (params.useCodeFinder && codeFinder != null) {
 			
 			codeFinder.reset();
 			List<String> rules = ListUtils.stringAsList(params.codeFinderRules, "\n");
 			
 			for (String rule : rules)
 				codeFinder.addRule(rule);
 			
 			codeFinder.compile();
 		}
 	}
 	
 	/**
 	 * @param textUnit
 	 * @param source
 	 * @param skel
 	 */
 	private void trimTU(TextUnit textUnit, TextContainer source, GenericSkeleton skel) {
 		
 		if (params.trimLeading) TextUnitUtils.trimLeading(source, skel);							
 		skel.addContentPlaceholder(textUnit);		
 		if (params.trimTrailing) TextUnitUtils.trimTrailing(source, skel);
 	}
 	
 	protected TextProcessingResult sendAsSource(TextUnit textUnit) {
 		
 		if (!processTextUnit(textUnit)) return TextProcessingResult.REJECTED;
 		
 		if (!TextUnitUtils.hasSource(textUnit)) return TextProcessingResult.REJECTED;
 		
 		sendEvent(EventType.TEXT_UNIT, textUnit);
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected TextProcessingResult sendAsTarget(TextUnit target, TextUnit source, String language, GenericSkeleton parentSkeleton) {
 		
 		if (target == null) return TextProcessingResult.REJECTED;
 		if (source == null) return TextProcessingResult.REJECTED;
 		
 		if (parentSkeleton == null)
 			parentSkeleton = (GenericSkeleton) source.getSkeleton();
 		
 		if (!processTextUnit(target)) return TextProcessingResult.REJECTED;
 		
 		TextContainer trg = new TextContainer(TextUnitUtils.getSourceText(target));					
 		source.setTarget(language, trg);
 		
 		parentSkeleton.add(TextUnitUtils.convertToSkeleton(target));
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected TextProcessingResult sendAsSkeleton(TextUnit textUnit, GenericSkeleton parentSkeleton) {
 		
 //		if (!processTextUnit(textUnit)) return TextProcessingResult.REJECTED;
 		
 		if (parentSkeleton == null)
 			parentSkeleton = (GenericSkeleton) textUnit.getSkeleton();
 		
 		if (parentSkeleton == null) return TextProcessingResult.REJECTED;
 		
 		parentSkeleton.add(TextUnitUtils.convertToSkeleton(textUnit));
 		return TextProcessingResult.ACCEPTED;
 	}
 
 	@SuppressWarnings("unchecked")
 	private boolean processTextUnit(TextUnit textUnit) {
 
 		if (textUnit == null) return false;
 		TextContainer source = textUnit.getSource();
 		if (source == null) return false;		
 		
 		GenericSkeleton skel = TextUnitUtils.forseSkeleton(textUnit);
 		
 		if (!checkTU(source)) return false;
 		if (source.isEmpty()) return false;
 		
 		if (params.unescapeSource) _unescape(source);
 		
 		//------------------------------
 		// The cell can already have something in the skeleton (for instance, a gap after the source)
 		
 		if (params.trimLeading || params.trimTrailing) {
 			
 			List<?> temp = skel.getParts();
 			List<Object> list = (List<Object>) temp;
 			
 			int index = -1;
 			String tuRef = TextFragment.makeRefMarker("$self$");
 			
 			for (int i = 0; i < list.size(); i++) {
 				
 				Object obj = list.get(i);
 				if (obj == null) continue;
 				String st = obj.toString();
 				
 				if (Util.isEmpty(st)) continue;
 				if (st.equalsIgnoreCase(tuRef)) {
 					index = i;
 					break;
 				}
 			}
 			
 			if (index > -1) { // tu ref was found in the skeleton
 				
 				List<Object> list2 = (List<Object>) ListUtils.moveItems(list); // clears the original list
 								
 				GenericSkeleton skel2 = new GenericSkeleton();				
 				trimTU(textUnit, source, skel2);
 			
 				for (int i = 0; i < list2.size(); i++) {
 					
 					if (i == index)						
 						skel.add(skel2);
 					else
 						list.add(list2.get(i));										
 				}				
 			}
 			else {		
 				trimTU(textUnit, source, skel);
 			}
 			
 		}
 		else {
 			trimTU(textUnit, source, skel);
 		}
 							
 		//------------------------------
 		
 		Set<String> languages = textUnit.getTargetLanguages();
 
 		textUnit.setMimeType(getMimeType());
 		textUnit.setPreserveWhitespaces(params.preserveWS);
 		
 		if (!params.preserveWS ) {
 			// Unwrap the content
 			TextFragment.unwrap(source);
 			
 			for (String language : languages)
 				TextFragment.unwrap(textUnit.getTargetContent(language));				
 		}
 		
 		// Automatically replace text fragments with in-line codes (based on regex rules of codeFinder)
 		if (params.useCodeFinder && codeFinder != null) {
 			
 			codeFinder.process(source);
 			
 			for (String language : languages)
 				codeFinder.process(textUnit.getTargetContent(language));
 		}
 		
 		return true;
 	}
 
 	protected TextProcessingResult sendAsSource(TextContainer textContainer) {
 		
 		if (textContainer == null) return TextProcessingResult.REJECTED;
 		
 		return sendAsSource(TextUnitUtils.buildTU(null, "", textContainer, null, "", ""));
 	}
 	
 	protected boolean sendSkeletonPart(GenericSkeleton skelPart) {
 		
 		if (skelPart == null) return false;
 		
 		GenericSkeleton activeSkel = getActiveSkeleton();
 		if (activeSkel == null) return false;
 		
 		activeSkel.add(skelPart);
 		
 		return true;
 	}
 	
 	protected boolean sendSkeletonPart(String skelPart) {
 		
 		if (skelPart == null) return false;
 		
 		GenericSkeleton activeSkel = getActiveSkeleton();
 		if (activeSkel == null) return false;
 		
 		activeSkel.add(skelPart);
 		
 		return true;
 	}
 	
 	protected boolean checkTU(TextContainer tuSource) {
 		// Can be overridden in descendant classes
 		
 		return true;		
 	}	
 	
 	@Override
 	protected TextProcessingResult component_exec(TextContainer lineContainer) {
 				
 		return sendAsSource(lineContainer);		
 	}
 	
 // Helpers	
 
 	/**
 	 * Unescapes slash-u+HHHH characters in a string.
 	 * @param text The string to convert.
 	 * @return The converted string.
 	 */
 	private void _unescape (TextContainer textContainer) {
 		// Cannot be static because of the logger
 		
 		final String INVALID_UESCAPE = "Invalid Unicode escape sequence '%s'";
 		
 		if (textContainer == null) return;
 		
 		String text = textContainer.getCodedText(); 
 		if (Util.isEmpty(text)) return;
 		
 		if ( text.indexOf('\\') == -1 ) return; // Nothing to unescape
 		
 		StringBuilder tmpText = new StringBuilder();
 		
 		for ( int i = 0; i < text.length(); i++ ) {
 			if ( text.charAt(i) == '\\' ) {
 				switch (Util.getCharAt(text, i+1)) {
 				
 				case 'u':
 					if ( i+5 < text.length() ) {
 						try {
 							int nTmp = Integer.parseInt(text.substring(i+2, i+6), 16);
 							tmpText.append((char)nTmp);
 						}
 						catch ( Exception e ) {
 							logMessage(Level.WARNING,
 								String.format(INVALID_UESCAPE, text.substring(i+2, i+6)));
 						}
 						i += 5;
 						continue;
 					}
 					else {
 						logMessage(Level.WARNING,
 							String.format(INVALID_UESCAPE, text.substring(i+2)));
 					}
 					break;
 				case '\\':
 					tmpText.append("\\\\");
 					i++;
 					continue;
 				case 'n':
 					tmpText.append("\n");
 					i++;
 					continue;
 				case 't':
 					tmpText.append("\t");
 					i++;
 					continue;
 				}
 			}
 			else tmpText.append(text.charAt(i));
 		}
 		
 		textContainer.setCodedText(tmpText.toString());
 	}
 
 	@Override
 	protected void component_done() {
 		
 	}
 
 }	
 	
