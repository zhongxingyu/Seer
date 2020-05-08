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
 
 package net.sf.okapi.applications.rainbow.utilities.textrewriting;
 
 import net.sf.okapi.applications.rainbow.utilities.BaseFilterDrivenUtility;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterEvent;
 import net.sf.okapi.common.resource.IResource;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.lib.segmentation.SRXDocument;
 import net.sf.okapi.lib.segmentation.Segmenter;
 import net.sf.okapi.lib.translation.QueryResult;
 import net.sf.okapi.tm.simpletm.SimpleTMConnector;
 
 public class Utility extends BaseFilterDrivenUtility {
 
 	private static final char TMPSTARTSEG = '\u0002';
 	private static final char TMPENDSEG = '\u0003';
 	private static final char STARTSEG = '[';
 	private static final char ENDSEG = ']';
 	
 	private Parameters params;
 	private Segmenter srcSeg;
 	private Segmenter trgSeg;
 	private SimpleTMConnector tmQ;
 	
 	public Utility () {
 		params = new Parameters();
 	}
 	
 	public String getName () {
 		return "oku_textrewriting";
 	}
 	
 	public void preprocess () {
 		// Load the segmentation rules
 		if ( params.segment ) {
 			SRXDocument doc = new SRXDocument();
 			doc.loadRules(params.sourceSrxPath);
 			if ( doc.hasWarning() ) logger.warn(doc.getWarning());
 			srcSeg = doc.applyLanguageRules(srcLang, null);
 			if ( !params.sourceSrxPath.equals(params.targetSrxPath) ) {
 				doc.loadRules(params.targetSrxPath);
 				if ( doc.hasWarning() ) logger.warn(doc.getWarning());
 			}
 			trgSeg = doc.applyLanguageRules(trgLang, null);
 		}
 		
 		if ( params.type == Parameters.TYPE_TRANSLATEEXACTMATCHES ) {
 			tmQ = new SimpleTMConnector();
 			tmQ.open(params.tmPath);
 		}
 	}
 	
 	public void postprocess () {
 		if ( tmQ != null ) {
 			tmQ.close();
 			tmQ = null;
 		}
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasParameters () {
 		return true;
 	}
 
 	public boolean needsRoots () {
 		return false;
 	}
 
 	public void setParameters (IParameters paramsObject) {
 		params = (Parameters)paramsObject;
 	}
 
 	public boolean isFilterDriven () {
 		return true;
 	}
 
 	public int requestInputCount () {
 		return 1;
 	}
 
 	public FilterEvent handleEvent (FilterEvent event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			processStartDocument((StartDocument)event.getResource());
 			break;
 		case TEXT_UNIT:
 			processTextUnit((TextUnit)event.getResource());
 			break;
 		}
 		return event;
 	}
 	
     private void processStartDocument (StartDocument resource) {
     	if ( tmQ != null ) {
     		tmQ.setAttribute("FileName", Util.getFilename(resource.getName(), true));
     	}
     }
 	
 	private void processTextUnit (TextUnit tu) {
 		// Skip non-translatable
 		if ( !tu.isTranslatable() ) return;
 		// Skip if already translate (only if required)
 		if ( !params.applyToExistingTarget && tu.hasTarget(trgLang) ) return;
 		
 		// Apply the segmentation and/or segment marks if requested
 		if ( params.segment || params.markSegments ) {
 			if ( tu.hasTarget(trgLang) ) {
 				if ( params.segment ) {
 					trgSeg.computeSegments(tu.getTarget(trgLang));
 					tu.getTarget(trgLang).createSegments(trgSeg.getSegmentRanges());
 				}
 			}
 			else {
 				if ( params.segment ) {
 					srcSeg.computeSegments(tu.getSource());
 					tu.getSource().createSegments(srcSeg.getSegmentRanges());
 				}
 			}
 		}
 		
 		// Else: do the requested modifications
 		// Make sure we have target content
 		tu.createTarget(trgLang, false, IResource.COPY_ALL);
 
 		// Translate is done before we merge possible segments
 		if ( params.type == Parameters.TYPE_TRANSLATEEXACTMATCHES ) {
 			translate(tu);
 		}
 
 		// Merge all segments if needed
 		if ( params.segment || params.markSegments ) {
 			mergeSegments(tu.getTarget(trgLang));
 		}
 
 		// Other text modification are done after merging all segments
 		switch ( params.type ) {
 		case Parameters.TYPE_XNREPLACE:
 			replaceWithXN(tu);
 			break;
 		case Parameters.TYPE_KEEPINLINE:
 			removeText(tu);
 			break;
 		}
 	
		if ( params.addPrefix || params.addSuffix || params.addName || params.addID ) {
 			addText(tu);
 		}
 	}
 
 	/**
 	 * Removes the text but leaves the inline code.
 	 * @param tu the text unit to process.
 	 */
 	private void removeText (TextUnit tu) {
 		String result = tu.getTarget(trgLang).getCodedText();
 		StringBuilder sb = new StringBuilder();
 		
 		for ( int i=0; i<result.length(); i++ ) {
 			switch ( result.charAt(i) ) {
 			    case TextFragment.MARKER_OPENING:
 				case TextFragment.MARKER_CLOSING:
 				case TextFragment.MARKER_ISOLATED:
 				case TextFragment.MARKER_SEGMENT:
 					sb.append(result.charAt(i));
 					sb.append(result.charAt(++i));
 					break;
 				case TMPSTARTSEG: // Keep segment marks if needed
 					if ( params.markSegments ) sb.append(STARTSEG);
 					break;
 				case TMPENDSEG: // Keep segment marks if needed
 					if ( params.markSegments ) sb.append(ENDSEG);
 					break;					
 				default: // Do not keep other characters
 					break;
 			}
 		}
 		TextContainer cnt = tu.getTarget(trgLang);
 		cnt.setCodedText(sb.toString());
 	}	
 	
 	/**
 	 * Merges back segments into a single content, while optionally adding
 	 * brackets to denote the segments. If the unit is not segmented, brackets
 	 * are added if required. 
 	 * @param container The TextContainer object to un-segment.
 	 */
 	private void mergeSegments (TextContainer container) {
 		// Set variables
 		StringBuilder text = new StringBuilder(container.getCodedText());
 		char start = STARTSEG;
 		char end = ENDSEG;
 		if ( params.type == Parameters.TYPE_KEEPINLINE ) {
 			// Use temporary marks if we need to remove the text after
 			// This way '[' and ']' is real text get removed too
 			start = TMPSTARTSEG;
 			end = TMPENDSEG;
 		}
 		
 		if ( !container.isSegmented() ) {
 			if ( params.markSegments ) {
 				container.setCodedText(start+text.toString()+end);
 			}
 			return;
 		}
 		
 		// Add the markers if needed
 		if ( params.markSegments ) {
 			// Insert the segment marks if requested
 			for ( int i=0; i<text.length(); i++ ) {
 				switch ( text.charAt(i) ) {
 				case TextContainer.MARKER_OPENING:
 				case TextContainer.MARKER_CLOSING:
 				case TextContainer.MARKER_ISOLATED:
 					i++; // Normal skip
 					break;
 				case TextContainer.MARKER_SEGMENT:
 					text.insert(i, start);
 					i += 3; // The bracket, the marker, the code index
 					text.insert(i, end);
 					// Next i increment will skip over the closing mark
 					break;
 				}
 			}
 			// Replace the original coded text by the one with markers
 			container.setCodedText(text.toString());
 		}
 		// Merge all segments
 		container.mergeAllSegments();
 	}
 
 	private void translate (TextUnit tu) {
 		try {
 			// Target is set if needed
 			QueryResult qr;
 			tmQ.setAttribute("GroupName", tu.getName());
 			TextContainer tc = tu.getTarget(trgLang);
 			if ( tc.isSegmented() ) {
 				int seg = 0;
 				for ( TextFragment tf : tc.getSegments() ) {
 					if ( tmQ.query(tf) == 1 ) {
 						qr = tmQ.next();
 						tc.getSegments().set(seg, qr.target);
 					}
 					seg++;
 				}
 			}
 			else {
 				if ( tmQ.query(tc) == 1 ) {
 					qr = tmQ.next();
 					tc = new TextContainer();
 					tc.append(qr.target);
 					tu.setTarget(trgLang, tc);
 				}
 			}
 			
 		}
 		catch ( Throwable e ) {
 			logger.warn("Error while translating: ", e);
 		}
 	}
 	
 	/**
 	 * Replaces letters with Xs and digits with Ns.
 	 * @param tu the text unit to process.
 	 */
 	private void replaceWithXN (TextUnit tu) {
 		String tmp = null;
 		try {
 			tmp = tu.getTarget(trgLang).getCodedText().replaceAll("\\p{Lu}|\\p{Lo}", "X");
 			tmp = tmp.replaceAll("\\p{Ll}", "x");
 			tmp = tmp.replaceAll("\\d", "N");
 			TextContainer cnt = tu.getTarget(trgLang); 
 			cnt.setCodedText(tmp, tu.getSourceContent().getCodes(), false);
 		}
 		catch ( Throwable e ) {
 			logger.warn("Error when updating content: '"+tmp+"'", e);
 		}
 	}
 	
 	/**
 	 * Adds prefix and/or suffix to the target. This method assumes that
 	 * the item has gone through the first transformation already.
 	 * @param tu The text unit to process.
 	 */
 	private void addText (TextUnit tu) {
 		String tmp = null;
 		try {
 			// Use the target as the text to change.
 			tmp = tu.getTarget(trgLang).getCodedText();
 			if ( params.addPrefix ) {
 				tmp = params.prefix + tmp;
 			}
 			if ( params.addName ) {
 				String name = tu.getName();
 				if (( name != null ) && ( name.length() > 0 )) tmp += "_"+name;
 				else tmp += "_"+tu.getId();
 			}
 			if ( params.addID ) {
 				tmp += "_"+tu.getId();
 			}
 			if ( params.addSuffix ) {
 				tmp += params.suffix;
 			}
 			TextContainer cnt = tu.getTarget(trgLang); 
 			cnt.setCodedText(tmp, tu.getSourceContent().getCodes(), false);
 		}
 		catch ( Throwable e ) {
 			logger.warn("Error when adding text to '"+tmp+"'", e);
 		}
 	}
 
 }
