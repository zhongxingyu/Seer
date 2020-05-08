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
 import java.util.logging.Level;
 
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.SkeletonUtil;
 import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
 import net.sf.okapi.lib.extra.filters.TextProcessingResult;
 
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
 		setDisplayName("Plain Text Filter (BETA)");
 		setMimeType(FILTER_MIME);
 		setMultilingual(false);
 		
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
 
 		super.component_init();
 		
 		// Initialization
 		if (params.useCodeFinder && codeFinder != null) {
 			
 			codeFinder.reset();
 			List<String> rules = ListUtil.stringAsList(params.codeFinderRules, "\n");
 			
 			for (String rule : rules)
 				codeFinder.addRule(rule);
 			
 			codeFinder.compile();
 		}
 	}
 	
 //	/**
 //	 * @param textUnit
 //	 * @param source
 //	 * @param skel
 //	 */
 //	private void trimTU(TextUnit textUnit) {
 //		
 //		if (textUnit == null) return;
 //		
 //		TextContainer source = textUnit.getSource();
 //		GenericSkeleton skel = (GenericSkeleton) textUnit.getSkeleton();
 //		
 //		if (params.trimLeading)						
 //			TextUnitUtil.trimLeading(source, skel);
 //		
 ////		if (!TextUnitUtil.hasContentPlaceholder(skel))
 //			skel.addContentPlaceholder(textUnit);
 //					
 //		if (params.trimTrailing) 
 //			TextUnitUtil.trimTrailing(source, skel);
 //	}
 	
 	protected final TextProcessingResult sendAsSource(TextUnit textUnit) {
 		return sendAsSource(textUnit, true);
 	}
 	
 	protected final TextProcessingResult sendAsSource(TextUnit textUnit, boolean rejectEmpty) {
 		
 		if (textUnit == null) return TextProcessingResult.REJECTED;
 		TextUnitUtil.forceSkeleton(textUnit);
 		
 		if (!processTU(textUnit)) return TextProcessingResult.REJECTED;
 		
 		if (rejectEmpty && !TextUnitUtil.hasSource(textUnit)) return TextProcessingResult.REJECTED;
 		
 		sendEvent(EventType.TEXT_UNIT, textUnit);
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected final TextProcessingResult sendAsSource(TextContainer textContainer) {
 		if (textContainer == null) return TextProcessingResult.REJECTED;
 		return sendAsSource(TextUnitUtil.buildTU(null, "", textContainer, null, LocaleId.EMPTY, ""));
 	}
 	
 	protected final TextProcessingResult sendAsTarget(TextUnit target,
 		TextUnit source,
 		LocaleId language)
 	{
 		if ( target == null ) return TextProcessingResult.REJECTED;
 		if ( source == null ) return TextProcessingResult.REJECTED;
 		if ( language == null ) return TextProcessingResult.REJECTED;
 		
 		GenericSkeleton skel = getActiveSkeleton();
 		if (skel == null) return TextProcessingResult.REJECTED;
 		
 		//boolean selfRef = (source.getSkeleton() == skel); 
 		
 		GenericSkeleton targetSkel = TextUnitUtil.forceSkeleton(target);
 		if ( targetSkel == null ) return TextProcessingResult.REJECTED;
 		if ( !processTU(target) ) return TextProcessingResult.REJECTED;
 		
 		source.setTarget(language, target.getSource());
 	
 		int index = SkeletonUtil.findTuRefInSkeleton(targetSkel);
 //		if ( index != -1 ) {
 //			// Replace target tu reference with a source reference
 //			GenericSkeleton tempSkel = new GenericSkeleton();
 //			if (selfRef)
 //				tempSkel.addContentPlaceholder(source, language);
 //			else {
 //				source.setIsReferent(true);
 //				tempSkel.addReference(source);
 //			}				
 //			SkeletonUtil.replaceSkeletonPart(targetSkel, index, tempSkel);
 //		}
 		
 		if ( index != -1 ) {
 			GenericSkeleton tempSkel = new GenericSkeleton();
 			tempSkel.addContentPlaceholder(source, language);
 			SkeletonUtil.replaceSkeletonPart(targetSkel, index, tempSkel);
 		}
 		skel.add(targetSkel);
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected final TextProcessingResult sendAsSkeleton(TextUnit textUnit) {
 //		if (!processTextUnit(textUnit)) return TextProcessingResult.REJECTED;
 		
 //		if (parentSkeleton == null)
 //			parentSkeleton = (GenericSkeleton) textUnit.getSkeleton();
 		
 		GenericSkeleton parentSkeleton = getActiveSkeleton();
 		if (parentSkeleton == null) return TextProcessingResult.REJECTED;
 		
 		//if (!processTU(textUnit)) return TextProcessingResult.REJECTED;
		processTU(textUnit); // Can have an empty source, no problem
 		
 		parentSkeleton.add(TextUnitUtil.convertToSkeleton(textUnit));
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected final TextProcessingResult sendAsSkeleton(GenericSkeleton skelPart) {
 		
 		if (skelPart == null) return TextProcessingResult.REJECTED;
 		
 		GenericSkeleton activeSkel = getActiveSkeleton();
 		if (activeSkel == null) return TextProcessingResult.REJECTED;
 		
 		activeSkel.add(skelPart);
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 	
 	protected final TextProcessingResult sendAsSkeleton(String skelPart) {
 		
 		if (skelPart == null) return TextProcessingResult.REJECTED;
 		
 		GenericSkeleton activeSkel = getActiveSkeleton();
 		if (activeSkel == null) return TextProcessingResult.REJECTED;
 		
 		activeSkel.add(skelPart);
 		
 		return TextProcessingResult.ACCEPTED;
 	}
 
 //	protected boolean processTU(TextUnit textUnit) {
 //		
 //		return processTU(textUnit, null, null, TextUnitUtil.forseSkeleton(textUnit));
 //	}
 //	
 //	protected boolean processTU(TextUnit textUnit, TextUnit srcRef, String language, GenericSkeleton skel) {
 //
 //		if (textUnit == null) return false;
 //		TextContainer source = textUnit.getSource();
 //		if (source == null) return false;		
 //		
 //		//GenericSkeleton skel = TextUnitUtil.forseSkeleton(textUnit);
 //		
 //		if (!checkTU(source)) return false;
 //		if (source.isEmpty()) return false;
 //		
 //		if (params.unescapeSource) _unescape(source);
 //		
 //		//------------------------------
 //		// The cell can already have something in the skeleton (for instance, a gap after the source)
 //		
 //		if (params.trimLeading || params.trimTrailing) {
 //			
 //			List<GenericSkeletonPart> list = skel.getParts();
 //			
 //			int index = -1;
 //			String tuRef = TextFragment.makeRefMarker("$self$");
 //			
 //			for (int i = 0; i < list.size(); i++) {
 //				
 //				GenericSkeletonPart part = list.get(i);				
 //				String st = part.toString();
 //				
 //				if (Util.isEmpty(st)) continue;
 //				if (st.equalsIgnoreCase(tuRef)) {
 //					index = i;
 //					break;
 //				}
 //			}
 //			
 ////			index = list.indexOf(tuRef); 
 //			if (index > -1) { // tu ref was found in the skeleton
 //				
 //				//List<Object> list2 = (List<Object>) ListUtil.moveItems(list); // clears the original list
 //				List<GenericSkeletonPart> list2 = (List<GenericSkeletonPart>) ListUtil.moveItems(list); // clears the original list
 //								
 //				GenericSkeleton skel2 = new GenericSkeleton();				
 //				trimTU(textUnit, srcRef, language, skel2);
 //			
 //				for (int i = 0; i < list2.size(); i++) {
 //					
 //					if (i == index)						
 //						skel.add(skel2);
 //					else
 //						list.add(list2.get(i));										
 //				}				
 //			}
 //			else {		
 //				trimTU(textUnit, srcRef, language, skel);
 //			}
 //			
 //		}
 //		else {
 //			trimTU(textUnit, srcRef, language, skel);
 //		}
 //							
 //		//------------------------------
 //		
 ////		Set<String> languages = textUnit.getTargetLanguages();
 //
 //		textUnit.setMimeType(getMimeType());
 //		textUnit.setPreserveWhitespaces(params.preserveWS);
 //		
 //		if (!params.preserveWS ) {
 //			// Unwrap the content
 //			TextFragment.unwrap(source);
 //			
 ////			for (String lng : languages)
 ////				TextFragment.unwrap(textUnit.getTargetContent(lng));				
 //		}
 //		
 //		// Automatically replace text fragments with in-line codes (based on regex rules of codeFinder)
 //		if (params.useCodeFinder && codeFinder != null) {
 //			
 //			codeFinder.process(source);
 //			
 ////			for (String lng : languages)
 ////				codeFinder.process(textUnit.getTargetContent(lng));
 //		}
 //		
 //		return true;
 //	}
 
 	protected boolean processTU(TextUnit textUnit) {
 		
 		if (textUnit == null) return false;
 		TextContainer source = textUnit.getSource();
 		if (source == null) return false;		
 		
 		// GenericSkeleton skel = TextUnitUtil.forseSkeleton(textUnit);
 		
 		if (!checkTU(source)) return false;
 		if (source.isEmpty()) return false;
 		
 		// We can use getFirstPartContent() because nothing is segmented yet
 		if (params.unescapeSource) _unescape(source.getFirstContent());
 		
 		//------------------------------
 		// The cell can already have something in the skeleton (for instance, a gap after the source)
 
 		TextUnitUtil.trimTU(textUnit, params.trimLeading, params.trimTrailing);
 		
 //		if (params.trimLeading || params.trimTrailing) {
 //			
 //			int index = TextUnitUtil.findTuRefInSkeleton(textUnit); 
 //			if (index > -1) { // tu ref was found in the skeleton
 //
 //				List<GenericSkeletonPart> list = skel.getParts();
 //				List<GenericSkeletonPart> list2 = (List<GenericSkeletonPart>) ListUtil.moveItems(list); // clears the original list
 //								
 //				GenericSkeleton skel2 = new GenericSkeleton();				
 //				//trimTU(textUnit, skel2);
 //				TextUnitUtil.trimTU(textUnit, params.trimLeading, params.trimTrailing);
 //			
 ////				for (int i = 0; i < list2.size(); i++) {
 ////					
 ////					if (i == index)						
 ////						skel.add(skel2);
 ////					else
 ////						list.add(list2.get(i));										
 ////				}				
 //			}
 //			else {		
 //				//trimTU(textUnit, skel);
 //				TextUnitUtil.trimTU(textUnit, params.trimLeading, params.trimTrailing);
 //			}
 //			
 //		}
 //		else {
 //			trimTU(textUnit, skel); // Just adds the src ref, no trimming is performed
 //		}
 							
 		//------------------------------
 		
 //		Set<String> languages = textUnit.getTargetLanguages();
 
 		textUnit.setMimeType(getMimeType());
 		textUnit.setPreserveWhitespaces(params.preserveWS);
 		
 		if (!params.preserveWS ) {
 			// Unwrap the content
 			source.unwrap(true, true);
 			
 //			for (String lng : languages)
 //				TextFragment.unwrap(textUnit.getTargetContent(lng));				
 		}
 		
 		// Automatically replace text fragments with in-line codes (based on regex rules of codeFinder)
 		if (params.useCodeFinder && codeFinder != null) {
 			// We can use getFirstPartContent() because nothing is segmented yet
 			codeFinder.process(source.getFirstContent());
 			
 //			for (String lng : languages)
 //				codeFinder.process(textUnit.getTargetContent(lng));
 		}
 		
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
 	private void _unescape (TextFragment textFrag) {
 		// Cannot be static because of the logger
 		
 		final String INVALID_UESCAPE = "Invalid Unicode escape sequence '%s'";
 		
 		if (textFrag == null) return;
 		
 		String text = textFrag.getCodedText(); 
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
 		
 		textFrag.setCodedText(tmpText.toString());
 	}
 
 	@Override
 	protected void component_done() {
 		
 	}
 
 }	
 	
