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
 
 package net.sf.okapi.common.filterwriter;
 
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 
 /**
  * Writer for TMX documents.
  */
 public class TMXWriter {
 
     private static final String ATTR_NAMES = ";lang;tuid;o-encoding;datatype;usagecount;"
     	+ "lastusagedate;creationtool;creationtoolversion;creationdate;creationid;"
     	+ "changedate;segtype;changeid;o-tmf;srclang;";
 
     private static final String CREATIONID = "creationid";
     private static final String FROMALTERNATE = "FromAlternate!";
     
     private XMLWriter writer;
     private TMXContent tmxCont = new TMXContent();
     private LocaleId srcLoc;
     private LocaleId trgLoc;
     private int itemCount;
     private Pattern exclusionPattern = null;
     private Pattern altTransInclusionPattern = null;
     private Hashtable<String, String> mtAttribute;
     private Hashtable<String, String> altAttribute;
     private boolean useMTPrefix = true;
     private boolean writeAllPropertiesAsAttributes;
 
     /**
      * Creates a new TMXWriter object.
      * Creates a new TMX document.
      * @param path The full path of the TMX document to create.
      * If another document exists already it will be overwritten.
      */
     public TMXWriter (String path) {
     	if ( path == null ) {
     		throw new IllegalArgumentException("path must be set");
     	}
     	this.setWriteAllPropertiesAsAttributes(false);
     	writer = new XMLWriter(path);
     	mtAttribute = new Hashtable<String, String>();
     	mtAttribute.put(CREATIONID, Util.MTFLAG);
     	altAttribute = new Hashtable<String, String>();
     	altAttribute.put(CREATIONID, FROMALTERNATE);
     }
 
     /**
      * Creates a new TMXWriter object.
      * Creates a new TMX document.
      * @param writer an instance of an XMLWriter to use.
      * If another document exists already it will be overwritten.
      */
     public TMXWriter (XMLWriter writer) {
     	this.setWriteAllPropertiesAsAttributes(false);
     	this.writer = writer;
     }
 
     /**
      * Closes the current output document if one is opened.
      */
     public void close () {
     	if ( writer != null ) {
     		writer.close();
     		writer = null;
     	}
     }
 
     /**
      * Gets the number of TU elements that have been written in the current output document.
      * @return The number of TU elements written in the current output document.
      */
     public int getItemCount () {
     	return itemCount;
     }
 
     /**
      * Sets the flag indicating whether the writer should output
      * workaround codes specific for Trados.
      * @param value true to output Trados-specific workarounds. False otherwise.
      */
     public void setTradosWorkarounds (boolean value) {
     	tmxCont.setTradosWorkarounds(value);
     }
 
     /**
      * Sets the flag indicating whether the writer should output
      * letter-coded content (e.g. to work for OmegaT).
      * @param value true to output letter-coded content. False otherwise.
      * @param zeroBased true to have 0-based code, false for unaltered IDs.
      */
     public void setLetterCodedMode (boolean value,
     	boolean zeroBased)
     {
     	tmxCont.setLetterCodedMode(value, zeroBased);
     }
     
     /**
      * Sets a pattern of content to not output. The given pattern is matched against
      * the source content of each item, if it matches, the item is not written.
      * @param pattern the regular expression pattern of the contents to not output.
      */
     public void setExclusionOption (String pattern) {
     	if ( Util.isEmpty(pattern) ) {
     		exclusionPattern = null;
     	} 
     	else {
     		exclusionPattern = Pattern.compile(pattern);
     	}
     }
     
     /**
      * Sets a pattern used to indicate which entries to include when using the
      * {@link #writeAlternate(AltTranslation, TextFragment)} method. When this pattern is set to null (the default)
      * all entries are included.
      * @param pattern the regular expression of the origin(s) to include. Use null to include all entries. 
      */
     public void setAltTranslationOption (String pattern) {
     	if ( Util.isEmpty(pattern) ) {
     		this.altTransInclusionPattern = null;
     	} 
     	else {
     		this.altTransInclusionPattern = Pattern.compile(pattern);
     	}
     }
 
     /**
      * Sets the default quote mode to use in escaping the TMX segment content (1 is the default).
      * @param quoteMode 0=no quote escaped, 1=apos and quot, 2=#39 and quot,
      * and 3=quot only.
      */
     public void setQuoteMode (int quoteMode) {
     	tmxCont.setQuoteMode(quoteMode);
     }
     
     /**
      * Sets the flag indicating if entries originating from MT should be written with a special
      * prefix in the source text (forcing a fuzzy match). An entry is deemed originating from
      * MT when its <code>creationid</code> attribute is set to <code>{@link Util#MTFLAG}</code>. 
      * @param useMTPrefix true to use a prefix for entries originating from MT.
      */
     public void setUseMTPrefix (boolean useMTPrefix) {
    	 this.useMTPrefix = useMTPrefix;
     }
 
     /**
      * Writes the start of the TMC document.
      * @param sourceLocale The source locale (must be set).
      * @param targetLocale The target locale (must be set).
      * @param creationTool The identifier of the creation tool (can be null).
      * @param creationToolVersion The version of the creation tool (can be null).
      * @param segType The type of segments in the output.
      * @param originalTMFormat The identifier for the original TM engine (can be null).
      * @param dataType The type of data to output.
      */
     public void writeStartDocument(LocaleId sourceLocale,
    		LocaleId targetLocale,
    		String creationTool,
    		String creationToolVersion,
    		String segType,
    		String originalTMFormat,
    		String dataType)
     {
     	if ( sourceLocale == null ) {
     		throw new NullPointerException("sourceLocale null");
     	}
     	if ( targetLocale == null ) {
     		throw new NullPointerException("targetLocale null");
     	}
     	this.srcLoc = sourceLocale;
     	this.trgLoc = targetLocale;
 
     	if ( tmxCont.getLetterCodedMode() ) {
     		// If letter-coded mode is set, we need to overwrite the creationtool attribute
     		creationTool = "OmegaT";
     	}
 
     	writer.writeStartDocument();
     	writer.writeStartElement("tmx");
     	writer.writeAttributeString("version", "1.4");
 
     	writer.writeStartElement("header");
     	writer.writeAttributeString("creationtool",
     		(creationTool == null) ? "unknown" : creationTool);
     	writer.writeAttributeString("creationtoolversion",
     		(creationToolVersion == null) ? "unknown" : creationToolVersion);
     	writer.writeAttributeString("segtype",
     		(segType == null) ? "paragraph" : segType);
     	writer.writeAttributeString("o-tmf",
     		(originalTMFormat == null) ? "unknown" : originalTMFormat);
     	writer.writeAttributeString("adminlang", "en");
     	writer.writeAttributeString("srclang", srcLoc.toBCP47());
     	writer.writeAttributeString("datatype",
     		(dataType == null) ? "unknown" : dataType);
     	writer.writeEndElement(); // header
 
     	writer.writeStartElement("body");
     	writer.writeLineBreak();
     }
 
     /**
      * Writes the end of the TMX document.
      */
     public void writeEndDocument () {
     	writer.writeEndElementLineBreak(); // body
     	writer.writeEndElementLineBreak(); // tmx
     	writer.writeEndDocument();
     }
 
     /**
      * Writes a given text unit. One TMX TU per segment if the text unit is segment,
      * or one TU for the full content, if the text unit is not segmented.
      * @param tu The text unit to output.
      * @param attributes The optional set of attribute to put along with the entry.
      */
     public void writeItem (ITextUnit tu,
     	Map<String, String> attributes)
     {
     	if ( !tu.hasTarget(trgLoc) ) {
     		return; // No target
     	}
     	ISegments srcSegs = tu.getSourceSegments();
     	ISegments trgSegs = tu.getTargetSegments(trgLoc);
 
     	// Output each segment (handles single-segment entry)
     	String tuId = tu.getId();
     	for ( Segment srcSeg : srcSegs ) {
     		Segment trgSeg = trgSegs.get(srcSeg.id);
     		if (( trgSeg == null ) || trgSeg.text.isEmpty() ) continue; // No target
     		// Else: output
     		writeTU(srcSeg.text, trgSeg.text,
 				String.format("%s_s%s", tuId, srcSeg.id), attributes);
     	}
     }
 
 // Removed deprecated method    
 //    /**
 //     * Writes a given text unit.
 //     * @param item the text unit to output.
 //     * @param attributes the optional set of attribute to put along with the entry.
 //     * @param alternate indicates if this item is an 'alternate'. If it is an alternate, if the
 //     * target locale does not have any entry in this item, the first found entry is used
 //     * instead. This is to allow getting for example FR-CA translations for an FR project.
 //     * @deprecated This method will be removed soon, to write alternates use
 //     * {@link #writeAlternate(AltTranslation, TextFragment)}.
 //     */
 //    public void writeItem (TextUnit item,
 //   		Map<String, String> attributes,
 //   		boolean alternate)
 //    {
 //    	String tuid = item.getName();
 //    	if ( Util.isEmpty(tuid) ) {
 //    		// itemCount will be incremented in writeTU, so do a +1 here to take that in account
 //    		tuid = String.format("autoID%d", itemCount + 1);
 //    	}
 //
 //    	TextContainer srcTC = item.getSource();
 //    	TextContainer trgTC = item.getTarget(trgLoc);
 //
 //    	if (( trgTC == null ) && alternate ) {
 //    		// If we don't have a target but are in alternate mode: get the first
 //    		// available locale in the list
 //    		Iterator<LocaleId> iter = item.getTargetLocales().iterator();
 //    		if ( iter.hasNext() ) {
 //    			trgTC = item.getTarget(iter.next());
 //    		}
 //    	}
 //
 //    	AltTranslationsAnnotation atAnn = null;
 //		for ( Segment srcSeg : srcTC.getSegments() ) {
 //    		TextFragment tf = srcSeg.text;
 //			Segment trgSeg = null;
 //			if ( trgTC != null ) {
 //				trgSeg = trgTC.getSegments().get(srcSeg.id);
 //			}
 //			if ( trgSeg == null ) {
 //	       		// No target segment
 //				continue;
 //			}
 //			// Get annotation
 //			AltTranslation at = null;
 //			atAnn = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
 //			if ( atAnn == null ) {
 //				continue;
 //			}
 //			at = atAnn.getFirst();
 //    		if ( at != null ) {
 //				// Now see if we need to alter the source text
 //				if ( at.fromMT() ) {
 //					// Set the MT flag attribute
 //		        	if ( attributes != null ) {
 //		        		if ( !attributes.containsKey(CREATIONID) ) {
 //		        			attributes.put(CREATIONID, Util.MTFLAG);
 //		        		}
 //		        	}
 //		        	else {
 //		        		attributes = MTattribute;
 //		        	}
 //		        	// Add the flag prefix if requested (that's why we clone)
 //		        	if ( useMTPrefix ) {
 //		        		tf = srcSeg.text.clone();
 //		        		tf.setCodedText(Util.MTFLAG+" "+tf.getCodedText());
 //		        	}
 //				}
 //    		}
 //    		if ( trgSeg.text.isEmpty() ) continue; // Skip possible empty entries
 //    		// Write out the segment
 //       		writeTU(tf, trgSeg.text,
 //   				String.format("%s_s%s", tuid, srcSeg.id), attributes);
 //    	}
 //    }
 
 //    /**
 //     * Writes a given text unit.
 //     * @param item the text unit to output.
 //     * @param attributes the optional set of attribute to put along with the entry.
 //     * @param alternate indicates if this item is an 'alternate'. If it is an alternate, if the
 //     * target locale does not have any entry in this item, the first found entry is used
 //     * instead. This is to allow getting for example FR-CA translations for an FR project.
 //     * @deprecated This method will be removed soon, to write alternates use
 //     * {@link #writeAlternate(AltTranslation, TextFragment)}.
 //     */
 //    public void writeItem_OLD (TextUnit item,
 //   		Map<String, String> attributes,
 //   		boolean alternate)
 //    {
 //    	String tuid = item.getName();
 //    	if ( Util.isEmpty(tuid) ) {
 //    		// itemCount will be incremented in writeTU, so do a +1 here to take that in account
 //    		tuid = String.format("autoID%d", itemCount + 1);
 //    	}
 //
 //    	TextContainer srcTC = item.getSource();
 //    	TextContainer trgTC = item.getTarget(trgLoc);
 //
 //    	if (( trgTC == null ) && alternate ) {
 //    		// If we don't have a target but are in alternate mode: get the first
 //    		// available locale in the list
 //    		Iterator<LocaleId> iter = item.getTargetLocales().iterator();
 //    		if ( iter.hasNext() ) {
 //    			trgTC = item.getTarget(iter.next());
 //    		}
 //    	}
 //
 //    	//TODO: Output only the items with real match or translations (not copy of source)		
 //    	ScoresAnnotation scores = null;
 //    	if ( trgTC != null ) {
 //    		scores = trgTC.getAnnotation(ScoresAnnotation.class);
 //    	}
 //
 //    	int i = -1;
 //		for ( Segment srcSeg : srcTC.getSegments() ) {
 //    		i++;
 //    		TextFragment tf = srcSeg.text;
 //			Segment trgSeg = null;
 //			if ( trgTC != null ) {
 //				trgSeg = trgTC.getSegments().get(srcSeg.id);
 //			}
 //			if ( trgSeg == null ) {
 //	       		writeTU(tf, null, String.format("%s_s%s", tuid, srcSeg.id), attributes);
 //				continue;
 //			}
 //    		if ( scores != null ) { // If the TU is scored
 //    			ScoreInfo si = scores.get(i); // Try to get the score for this segment
 //				if ( si.score == 0 ) {
 //					// Not score: in a scored-TU: skip this segment
 //					continue;
 //				}
 //				// Scored properly. Now see if we need to alter the source text
 //				if (( si.origin != null ) && si.origin.equals(Util.MTFLAG) ) {
 //					// Set the MT flag attribute
 //		        	if ( attributes != null ) {
 //		        		if ( !attributes.containsKey(CREATIONID) ) {
 //		        			attributes.put(CREATIONID, Util.MTFLAG);
 //		        		}
 //		        	}
 //		        	else {
 //		        		attributes = MTattribute;
 //		        	}
 //		        	
 //		        	// Add the flag prefix if requested (that's why we clone)
 //		        	if ( useMTPrefix ) {
 //		        		tf = srcSeg.text.clone();
 //		        		tf.setCodedText(Util.MTFLAG+" "+tf.getCodedText());
 //		        	}
 //				}
 //    		}
 //    		// Write out the segment
 //       		writeTU(tf, trgSeg.text,
 //   				String.format("%s_s%s", tuid, srcSeg.id), attributes);
 //    	}
 //    }
 
     /**
      * Writes the entries of an {@link AltTranslationsAnnotation}
      * annotation(s) of a given text unit to the TMX document.
      * @param tu text unit to use.
      * @param trgLoc target locale.
      */
     public void writeAlternates (ITextUnit tu,
     	LocaleId trgLoc)
     {
     	TextContainer tc = tu.getTarget(trgLoc);
     	if ( tc == null ) return; // No target
     	AltTranslationsAnnotation atAnn;
     	
     	// Treat case of un-segmented entry
     	if ( !tc.hasBeenSegmented() ) {
     		atAnn = tc.getAnnotation(AltTranslationsAnnotation.class);
     		if ( atAnn != null ) {
     			for ( AltTranslation at : atAnn ) {
     				TextFragment srcFrag = at.getSource().getFirstContent();
     				if ( srcFrag.isEmpty() ) {
     					srcFrag = tu.getSource().getFirstContent();
     				}
     				TextFragment trgFrag = at.getTarget().getFirstContent();
     				if ( trgFrag.isEmpty() ) continue; // Skip possible empty entries
     				// Write out the segment
     				writeTU(srcFrag, trgFrag, null, null, trgLoc);
     			}
     		}
     		return; // Done
     	}
     	
    		// Else: Treat case of segmented 
     	for ( Segment seg : tc.getSegments() ) {
     		// Check for the annotation
     		atAnn = seg.getAnnotation(AltTranslationsAnnotation.class);
     		if ( atAnn == null ) continue;
     		// If available: process it
     		for ( AltTranslation at : atAnn ) {
     			// Alternates are expected to be un-segmented
     	    	TextFragment srcFrag = at.getSource().getFirstContent();
     	    	// If we have no source it's because the source is the same as the content where
     	    	// the annotation is attached too.
     	    	if ( srcFrag.isEmpty() ) {
     	    		Segment srcSeg = tu.getSource().getSegments().get(seg.id);
     	    		if ( srcSeg == null ) continue; // No source: skip it
     	   			srcFrag = srcSeg.text;
     	    	}
     	    	TextFragment trgFrag = at.getTarget().getFirstContent();
     	    	if ( trgFrag.isEmpty() ) continue; // Skip possible empty entries
     			// Write out the segment
     	   		writeTU(srcFrag, trgFrag, null, null, trgLoc);
     		}
     	}
     }
     
     /**
      * Writes the data of an {@link AltTranslation} to this TMX output.
      * <p>Which entries will be output also depends on what patterns have been set with
      * {@link #setAltTranslationOption(String)} and {@link #setExclusionOption(String)}.
      * @param alt the alternate translation.
      * @param srcOriginal the default source (coming from the segment or container where
      * the annotation was attached to).
      */
     public void writeAlternate (AltTranslation alt,
     	TextFragment srcOriginal)
     {
     	// Check if this entry should be included or not
     	if ( altTransInclusionPattern != null ) {
     		String ori = alt.getOrigin();
     		if ( ori == null ) ori = ""; // Make sure null are treated
     		if ( !altTransInclusionPattern.matcher(ori).matches() ) {
     			// The origin value does not match: do not include this entry in the output
     			return;
     		}
     	}
 
     	// Alternates are expected to be un-segmented
     	TextFragment srcFrag = alt.getSource().getFirstContent();
     	if ( srcFrag.isEmpty() ) {
    			srcFrag = srcOriginal;
     	}
 
     	// Add an MT prefix if requested, use a clone for this
     	if ( useMTPrefix && alt.fromMT() ) {
    			if ( !srcFrag.getCodedText().startsWith(Util.MTFLAG) ) {
    				srcFrag = srcFrag.clone();
    				srcFrag.setCodedText(Util.MTFLAG+" "+srcFrag.getCodedText());
     		}
     	}
     	
     	TextFragment trgFrag = alt.getTarget().getFirstContent();
 		// Write out the segment
    		writeTU(srcFrag, trgFrag, null,
    			(alt.fromMT()
    				? mtAttribute
    				: (alt.getFromOriginal()
    					? altAttribute
    					: null)),
    			alt.getTargetLocale());
     }
     
     /**
      * Writes a TMX TU element.
      * @param source the fragment for the source text.
      * @param target the fragment for the target text.
      * @param tuid the TUID attribute (can be null).
      * @param attributes the optional set of attribute to put along with the entry.
      */
     public void writeTU (TextFragment source,
    		TextFragment target,
    		String tuid,
    		Map<String, String> attributes)
     {
     	writeTU(source, target, tuid, attributes, null);
     }
     
     /**
      * Writes a TMX TU element.
      * @param source the fragment for the source text.
      * @param target the fragment for the target text.
      * @param tuid the TUID attribute (can be null).
      * @param attributes the optional set of attribute to put along with the entry.
      * @param altTrgLoc the target locale id to use (in case it is different from
      * the default one, use null to get the default).
      */
     public void writeTU (TextFragment source,
    		TextFragment target,
    		String tuid,
    		Map<String, String> attributes,
    		LocaleId altTrgLoc)
     {
     	// Check if this source entry should be excluded from the output
     	if ( exclusionPattern != null ) {
     		if ( exclusionPattern.matcher(source.getCodedText()).matches() ) {
     			// The source coded text matches: do not include this entry in the output
     			return;
     		}
     	}
 
     	itemCount++;
     	writer.writeStartElement("tu");
     	if ( !Util.isEmpty(tuid) ) {
     		writer.writeAttributeString("tuid", tuid);
     	}
     	writer.writeLineBreak();
 
     	// Write properties
     	if (( attributes != null ) && ( attributes.size() > 0 )) {
     		for ( String name : attributes.keySet() ) {    			
     			// Filter out standard attributes
     			if ( ATTR_NAMES.contains(";"+name+";") ) {
     				continue;
     			}
     			// Write out the property
     			writer.writeStartElement("prop");
     			writer.writeAttributeString("type", name);
     			writer.writeString(attributes.get(name));
     			writer.writeEndElementLineBreak(); // prop
     		}
     	}
 
     	writer.writeStartElement("tuv");
     	writer.writeAttributeString("xml:lang", srcLoc.toBCP47());
     	writer.writeStartElement("seg");
     	writer.writeRawXML(tmxCont.setContent(source).toString());
     	writer.writeEndElement(); // seg
     	writer.writeEndElementLineBreak(); // tuv
 
     	if ( target != null ) {
     		writer.writeStartElement("tuv");
    			writer.writeAttributeString("xml:lang",
    				((altTrgLoc != null) ? altTrgLoc.toBCP47() : trgLoc.toBCP47()));
         	// Write creationid if available
         	if ( attributes != null ) {
         		if ( attributes.containsKey(CREATIONID) ) {
         			writer.writeAttributeString(CREATIONID, attributes.get(CREATIONID));
         		}
         	}
     		writer.writeStartElement("seg");
     		writer.writeRawXML(tmxCont.setContent(target).toString());
     		writer.writeEndElement(); // seg
     		writer.writeEndElementLineBreak(); // tuv
     	}
 
     	writer.writeEndElementLineBreak(); // tu
     }
 
     /**
      * Writes a TextUnit (all targets) with all the properties associated to it.
      * @param item The text unit to write.
      */
     public void writeTUFull (ITextUnit item) {
     	if ( item == null ) {
     		throw new NullPointerException();
     	}
     	itemCount++;
 
     	// In a TU, each target may have a different source-corresponding segmentation
     	// The way TMX is written here, we assume that all targets match the current source segmentation
 
     	String tuid = item.getName();
     	if ( Util.isEmpty(tuid) ) {
     		tuid = String.format("autoID%d", itemCount);
     	}
 
     	TextContainer srcCont = item.getSource();
     	Set<LocaleId> locales = item.getTargetLocales();
 		Set<String> names = item.getPropertyNames();
 
 		// For each segment: write a separate TU
 		for ( Segment srcSeg : srcCont.getSegments() ) {
     		
     		// Write start TU
     		writer.writeStartElement("tu");
     		if ( srcCont.contentIsOneSegment() ) {
     			writer.writeAttributeString("tuid", tuid);
     		}
     		else {
     			writer.writeAttributeString("tuid", String.format("%s_%s", tuid, srcSeg.id));
     		}
     		
     		if (isWriteAllPropertiesAsAttributes()) {
     			writeAllPropertiesAsAttibutes(writer, names, item);
     		}
     		
     		writer.writeLineBreak();
 
     		// Write any resource-level properties
     		for ( String name : names ) {    			
     			
     			// Filter out attributes (temporary solution)
     			if ( ATTR_NAMES.contains(";"+name+";") ) continue;
     			
     			// Write out the property
     			writer.writeStartElement("prop");
     			writer.writeAttributeString("type", name);
     			writer.writeString(item.getProperty(name).getValue());
     			writer.writeEndElementLineBreak(); // prop
     		}
 
     		// Write the source TUV
     		writeTUV(srcSeg.text, srcLoc, srcCont);
 		
     		// Write each target TUV
     		for ( LocaleId loc : locales ) {
     			Segment trgSeg = null;
         		TextContainer trgCont = item.getTarget(loc);
         		if ( trgCont != null ) {
         			trgSeg = trgCont.getSegments().get(srcSeg.id);
         		}
         		// Write target only if we have one corresponding to the source segment
         		if ( trgSeg != null ) {
         			writeTUV(trgSeg.text, loc, trgCont);
         		}
     		}
 
     		// Write end TU
     		writer.writeEndElementLineBreak(); // tu
 
     	} // End of the segments loop
     	
     }
 
     private void writeAllPropertiesAsAttibutes (XMLWriter writer, 
     	Set<String> names, 
     	ITextUnit item)
     {
     	// Write any TU-level properties as attributes (but only standard attributes)
 		for ( String name : names ) {
			if ( ATTR_NAMES.contains(";"+name+";") && !name.equals("tuid")) {
 				writer.writeAttributeString(name, item.getProperty(name).getValue());
 			}
 		}
     }
     
     /**
      * Writes a TUV element.
      * @param frag the TextFragment for the content of this TUV. This can be
      * a segment of a TextContainer.
      * @param locale the locale for this TUV.
      * @param contForProp the TextContainer that has the properties to write for
      * this TUV, or null for no properties.
      */
     private void writeTUV (TextFragment frag,
    		LocaleId locale,
    		TextContainer contForProp)
     {
     	writer.writeStartElement("tuv");
     	writer.writeAttributeString("xml:lang", locale.toBCP47());
     	writer.writeStartElement("seg");
     	writer.writeRawXML(tmxCont.setContent(frag).toString());
     	writer.writeEndElement(); // seg
 
     	if ( contForProp != null ) {
     		boolean propWritten = false;
     		Set<String> names = contForProp.getPropertyNames();
     		for ( String name : names ) {
     			// Filter out attributes (temporary solution)
     			if ( ATTR_NAMES.contains(";"+name+";") ) continue;
     			// Write out the property
     			writer.writeLineBreak();
     			writer.writeStartElement("prop");
     			writer.writeAttributeString("type", name);
     			writer.writeString(contForProp.getProperty(name).getValue());
     			writer.writeEndElement(); // prop
     			propWritten = true;
     		}
     		if ( propWritten ) {
     			writer.writeLineBreak();
     		}
     	}
     	writer.writeEndElementLineBreak(); // tuv
     }
     
     /**
 	 * If true then all TU level properties will be written as TMX attributes.
 	 * @param writeAllPropertiesAsAttributes true to write out all TU level properties as TMX attributes. 
 	 */
 	public void setWriteAllPropertiesAsAttributes(boolean writeAllPropertiesAsAttributes) {
 		this.writeAllPropertiesAsAttributes = writeAllPropertiesAsAttributes;
 	}
 
 	/**
 	 * Write all TU level properties as TMX attributes?
 	 * @return true if all TU level properties will be written, false otherwise.
 	 */
 	public boolean isWriteAllPropertiesAsAttributes() {
 		return writeAllPropertiesAsAttributes;
 	}
 }
