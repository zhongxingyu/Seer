 /*===========================================================================
   Copyright (C) 2011-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.xliff;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.QName;
 
 import org.oasisopen.xliff.v2.ICMarker;
 import org.oasisopen.xliff.v2.IDataStore;
import org.oasisopen.xliff.v2.IExtendedChild;
 import org.oasisopen.xliff.v2.IFragment;
 import org.oasisopen.xliff.v2.INote;
 import org.oasisopen.xliff.v2.IPart;
 import org.oasisopen.xliff.v2.IWithCandidates;
 import org.oasisopen.xliff.v2.Names;
 import org.oasisopen.xliff.v2.OriginalDataStyle;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.FileUtil;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextPart;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 import net.sf.okapi.lib.xliff.Candidate;
 import net.sf.okapi.lib.xliff.ExtendedAttribute;
 import net.sf.okapi.lib.xliff.ExtendedElement;
 import net.sf.okapi.lib.xliff.Fragment;
 import net.sf.okapi.lib.xliff.Note;
 import net.sf.okapi.lib.xliff.Unit;
 import net.sf.okapi.lib.xliff.XLIFFWriter;
 import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;
 
 public class XLIFF2PackageWriter extends BasePackageWriter {
 
 	public static final String POBJECTS_DIR = "pobjects";
 
 	private static final String TU_PREFIX = "$tu$";
 	private static final Logger LOGGER = Logger.getLogger(XLIFF2PackageWriter.class.getName());
 	
 	private XLIFFWriter writer;
 	private LinkedHashMap<String, String> referents;
 	private XLIFF2Options options;
 	private String rawDocPath;
 
 	public XLIFF2PackageWriter () {
 		super(Manifest.EXTRACTIONTYPE_XLIFF2);
 	}
 
 	@Override
 	protected void processStartBatch () {
 		// Get the options from the parameters
 		options = new XLIFF2Options();
 		if ( !Util.isEmpty(params.getWriterOptions()) ) {
 			options.fromString(params.getWriterOptions());
 		}
 
 		if ( options.getCreateTipPackage() ) {
 			manifest.setGenerateTIPManifest(true);
 			manifest.setSubDirectories(POBJECTS_DIR+"/input", POBJECTS_DIR+"/bilingual", POBJECTS_DIR+"/bilingual",
 				POBJECTS_DIR+"/output", POBJECTS_DIR+"/tm", false);
 		}
 		else {
 			manifest.setSubDirectories("original", "work", "work", "done", null, false);
 		}
 
 		// Create TM only for TIP package
 		setTMXInfo(options.getCreateTipPackage(), null, false, false);
 		super.processStartBatch();
 	}
 	
 	// For final zip 
 	public boolean getCreeatTipPackage () {
 		return options.getCreateTipPackage();
 	}
 	
 	@Override
 	protected void processEndBatch () {
 		// Base process
 		super.processEndBatch();
 		
 		// TIP-specific process
 		if ( options.getCreateTipPackage() ) {
 			// Gather the list of TMs created
 			ArrayList<String> tms = new ArrayList<String>();
 			if ( tmxWriterApproved != null ) {
 				if ( tmxWriterApproved.getItemCount() > 0 ) {
 					tms.add(Util.getFilename(tmxPathApproved, true));
 				}
 			}
 			if ( tmxWriterAlternates != null ) {
 				if ( tmxWriterAlternates.getItemCount() > 0 ) {
 					tms.add(Util.getFilename(tmxPathAlternates, true));
 				}
 			}
 			if ( tmxWriterLeverage != null ) {
 				if ( tmxWriterLeverage.getItemCount() > 0 ) {
 					tms.add(Util.getFilename(tmxPathLeverage, true));
 				}
 			}
 			if ( tmxWriterUnApproved != null ) {
 				if ( tmxWriterUnApproved.getItemCount() > 0 ) {
 					tms.add(Util.getFilename(tmxPathUnApproved, true));
 				}
 			}
 
 			// Save the TIP manifest
 			manifest.saveTIPManifest(manifest.getTempPackageRoot(), tms);
 
 			// Zip the project files
 			String dir = manifest.getTempPackageRoot()+POBJECTS_DIR;
 
 			FileUtil.zipDirectory(dir, ".zip");
 			
 			// Delete the original
 			Util.deleteDirectory(dir, false);
 			// The creation of the .tipp file is done at the step level
 			// otherwise to be done after the directory is freed from locks
 		}
 	}
 	
 	@Override
 	protected void processStartDocument (Event event) {
 		super.processStartDocument(event);
 		
 		writer = new XLIFFWriter();
 		referents = new LinkedHashMap<String, String>();
 
 		MergingInfo item = manifest.getItem(docId);
 		rawDocPath = manifest.getTempSourceDirectory() + item.getRelativeInputPath() + ".xlf";
 		// Set the writer's options
 		writer.setInlineStyle(OriginalDataStyle.fromInteger(options.getInlineStyle()));
 		writer.setUseIndentation(true);
 		// Create the writer
 		writer.create(new File(rawDocPath), manifest.getSourceLocale().toBCP47(),
 			manifest.getTargetLocale().toBCP47());
 		writer.writeStartDocument(null, "EXPERIMENTAL OUTPUT ONLY!");
 	}
 	
 	@Override
 	protected Event processEndDocument (Event event) {
 		writer.writeEndDocument();
 		writer.close();
 		writer = null;
 		referents.clear();
 		referents = null;
 		
 		if ( params.getSendOutput() ) {
 			return super.creatRawDocumentEventSet(rawDocPath, "UTF-8",
 				manifest.getSourceLocale(), manifest.getTargetLocale());
 		}
 		else {
 			return event;
 		}
 	}
 
 	@Override
 	protected void processStartSubDocument (Event event) {
 		// Do not start one explicitly
 		// Let the first unit to trigger the start of the file
 		// otherwise we may get empty file elements
 		// writer.writeStartFile();
 	}
 	
 	@Override
 	protected void processEndSubDocument (Event event) {
 		// Safe to call even if writestartFile(0 was not called
 		writer.writeEndFile();
 	}
 	
 	@Override
 	protected void processStartGroup (Event event) {
 		writer.writeStartGroup(null);
 	}
 	
 	@Override
 	protected void processEndGroup (Event event) {
 		writer.writeEndGroup();
 	}
 	
 	@Override
 	protected void processTextUnit (Event event) {
 		ITextUnit tu = event.getTextUnit();
 		if ( tu.isReferent() ) {
 			storeReferent(tu);
 		}
 		Unit unit = toXLIFF2Unit(tu);
 		writer.writeUnit(unit);
 		writeTMXEntries(event.getTextUnit());
 	}
 	
 	@Override
 	protected void processDocumentPart (Event event) {
 		DocumentPart dp = event.getDocumentPart();
 		if ( dp.isReferent() ) {
 			storeReferent(dp);
 		}
 	}
 
 	private void storeReferent (IResource res) {
 		ISkeleton skel = res.getSkeleton();
 		if ( skel == null ) return;
 		if ( res instanceof ITextUnit ) {
 			referents.put(res.getId(), TU_PREFIX+skel.toString());
 		}
 		else {
 			referents.put(res.getId(), skel.toString());
 		}
 	}
 
 	/**
 	 * Gets the text unit id of the referenced objects.
 	 * @param text the initial skeleton string.
 	 * @return a list of IDs or empty
 	 */
 	private String getReferences (String text) {
 		if ( text == null ) return null;
 		StringBuilder tmp = new StringBuilder();
 		StringBuilder data = new StringBuilder(text);
 		Object[] res = null;
 		do {
 			// Check if that data has a reference marker
 			res = TextFragment.getRefMarker(data);
 			if ( res != null ) {
 				String refId = (String)res[0];
 				if ( !refId.equals("$self$") ) {
 					String skel = referents.get(refId);
 					if ( skel != null ) {
 						if ( !skel.startsWith(TU_PREFIX) ) {
 							String refs = getReferences(skel);
 							if ( refs != null ) {
 								tmp.append(refs+" ");
 							}
 						}
 						else { // text unit
 							tmp.append(refId+" ");
 						}
 						//referents.remove(refId); // Clean up
 					}
 				}
 				// Remove this and check for next
 				data.delete((Integer)res[1], (Integer)res[2]);
 			}
 		}
 		while ( res != null );
 		return tmp.toString().trim(); 
 	}
 	
 	@Override
 	public void close () {
 		if ( writer != null ) {
 			writer.close();
 			writer = null;
 		}
 	}
 
 	@Override
 	public String getName () {
 		return getClass().getName();
 	}
 
 	private Unit toXLIFF2Unit (ITextUnit tu) {
 		Unit unit = new Unit(tu.getId());
 
 		TextContainer srcTc = tu.getSource();
 		TextContainer trgTc = null;
 		if ( tu.hasTarget(manifest.getTargetLocale()) ) {
 			trgTc = tu.getTarget(manifest.getTargetLocale());
 			if ( trgTc.getSegments().count() != srcTc.getSegments().count() ) {
 				// Use un-segmented entry if we have different number of segments
 				LOGGER.warning(String.format("Text unit id='%s' has different number of segments in source and target.\n"
 					+"This entry will be output un-segmented.", tu.getId()));
 				srcTc = tu.getSource().clone(); srcTc.joinAll();
 				trgTc = tu.getTarget(manifest.getTargetLocale()).clone(); trgTc.joinAll();
 			}
 		}
 
 		// Add trans-unit level note if needed
 		if ( tu.hasProperty(Property.NOTE) ) {
 			unit.addNote(new Note(tu.getProperty(Property.NOTE).getValue()));
 		}
 		// Add trans-unit level translator note if needed 
 		if ( tu.hasProperty(Property.TRANSNOTE) ) {
 			unit.addNote(new Note("From Translator: "+tu.getProperty(Property.TRANSNOTE).getValue()));
 		}
 		// Add source notes
 		if ( tu.hasSourceProperty(Property.NOTE) ) {
 			unit.addNote(new Note(tu.getSourceProperty(Property.NOTE).getValue(), INote.AppliesTo.SOURCE));
 		}
 		// Add target notes
 		if ( tu.hasTargetProperty(manifest.getTargetLocale(), Property.NOTE) ) {
 			unit.addNote(new Note(tu.getTargetProperty(manifest.getTargetLocale(), Property.NOTE).getValue(), INote.AppliesTo.TARGET));
 		}
 		
 		
 		// External resource reference
 		if ( tu.hasProperty(Property.ITS_EXTERNALRESREF) ) {
			unit.getExtendedAttributes().setAttribute(Names.NS_XLIFFOKAPI, "itsExternalResource",
 				tu.getProperty(Property.ITS_EXTERNALRESREF).getValue());
 		}
 		// Domains
 		if ( tu.hasProperty(Property.ITS_DOMAINS) ) {
 			ExtendedElement domains = new ExtendedElement(new QName(Names.NS_XLIFFOKAPI, "itsDomains", Names.PREFIX_XLIFFOKAPI), null);
 			domains.getAttributes().setNamespace("okp", Names.NS_XLIFFOKAPI);
 			//Not used for now domains.getAttributes().setNamespace("dc", "http://purl.org/dc/elements/1.1/");
 			for ( String value : tu.getProperty(Property.ITS_DOMAINS).getValue().split("\t", 0) ) {
 				ExtendedElement item = new ExtendedElement(new QName(Names.NS_XLIFFOKAPI, "item", Names.PREFIX_XLIFFOKAPI), null);
 				item.getAttributes().setAttribute("http://purl.org/dc/elements/1.1/", "subject", value);
 				domains.addChild(item);
 			}
 			unit.getExtendedElements().add(domains);
 		}
 		
 		
 		// Go through the parts: Use the source to drive the order
 		// But match on segment ids
 		TextPart part;
 		ISegments trgSegs = null;
 		if ( trgTc != null ) {
 			trgSegs = trgTc.getSegments();
 		}
 		int srcSegIndex = -1;
 		for ( int i=0; i<srcTc.count(); i++ ) {
 			part = srcTc.get(i);
 			if ( part.isSegment() ) {
 				Segment srcSeg = (Segment)part;
 				srcSegIndex++;
 				org.oasisopen.xliff.v2.ISegment xSeg = unit.appendNewSegment();
 				xSeg.setSource(toXLIFF2Fragment(srcSeg.text, unit.getDataStore(), false));
 				xSeg.setId(srcSeg.getId());
 				
 				// Applies TU-level translatable property to each segment
 				xSeg.setTranslate(tu.isTranslatable());
 				
 				// Target
 				if ( trgSegs != null ) {
 					Segment trgSeg = trgSegs.get(xSeg.getId());
 					if ( trgSeg != null ) {
 						xSeg.setTarget(toXLIFF2Fragment(trgSeg.text, unit.getDataStore(), true));
 						// Check if the order is the same as the source
 						int trgSegIndex = trgSegs.getIndex(xSeg.getId());
 						if ( srcSegIndex != trgSegIndex ) {
 							// Target is cross-aligned
 							int trgPartIndex = trgSegs.getPartIndex(trgSegIndex);
 							xSeg.setTargetOrder(trgPartIndex+1);
 						}
 					}
 					// Alt-trans annotation?
 					AltTranslationsAnnotation ann = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
 					if ( ann != null ) {
 						for ( AltTranslation alt : ann ) {
 							copyData(alt, srcSeg.text, xSeg);
 						}
 					}
 				}
 			}
 			else { // Non-segment part
 				IPart xPart = unit.appendNewIgnorable();
 				xPart.setSource(toXLIFF2Fragment(part.text, unit.getDataStore(), false));
 				// Target
 				if ( trgTc != null ) {
 //todo				trcTc.get
 				}
 			}
 		}
 		
 		if ( trgTc != null ) {
 			// Text-unit level: add possible Alt-trans annotation
 			AltTranslationsAnnotation ann = trgTc.getAnnotation(AltTranslationsAnnotation.class);
 			if ( ann != null ) {
 				TextFragment tf = tu.getSource().getUnSegmentedContentCopy();
 				for ( AltTranslation alt : ann ) {
 					copyData(alt, tf, unit);
 				}
 			}
 			
 		}
 		
 		return unit;
 	}
 	
 	private void copyData (AltTranslation alt,
 		TextFragment oriSource,
 		IWithCandidates xObject)
 	{
 		Candidate xAlt = new Candidate();
 		IDataStore cs = xAlt.getDataStore();
 		if ( alt.getSource().isEmpty() ) { // Same as the base source
 			xAlt.setSource(toXLIFF2Fragment(oriSource, cs, false));
 		}
 		else {
 			xAlt.setSource(toXLIFF2Fragment(alt.getSource().getFirstContent(), cs, false));
 			
 		}
 		xAlt.setTarget(toXLIFF2Fragment(alt.getTarget().getFirstContent(), cs, true));
 		xAlt.setSimilarity(alt.getFuzzyScore());
 		xAlt.setQuality(alt.getQualityScore());
 		xObject.addCandidate(xAlt);
 	}
 	
 	private IFragment toXLIFF2Fragment (TextFragment tf,
 		IDataStore store,
 		boolean isTarget)
 	{
 		// Fast track for content without codes
 		if ( !tf.hasCode() ) {
 			return new Fragment(store, isTarget, tf.getCodedText());
 		}
 		
 		// Otherwise: we map the codes
 		IFragment xFrag = new Fragment(store, isTarget);
 		String ctext = tf.getCodedText();
 		List<Code> codes = tf.getCodes();
 
 		int index;
 		Code code;
 		boolean mayOverlapDefault = false; // Most spanning codes may not overlap
 		for ( int i=0; i<ctext.length(); i++ ) {
 			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
 				index = TextFragment.toIndex(ctext.charAt(++i));
 				code = codes.get(index);
 				ICMarker xCode;
 				switch ( code.getTagType() ) {
 				case OPENING:
 					xCode = xFrag.append(org.oasisopen.xliff.v2.MarkerType.OPENING,
 						String.valueOf(code.getId()), code.getData(), mayOverlapDefault);
 					break;
 				case CLOSING:
 					xCode = xFrag.append(org.oasisopen.xliff.v2.MarkerType.CLOSING,
 						String.valueOf(code.getId()), code.getData(), mayOverlapDefault);
 					break;
 				case PLACEHOLDER:
 				default:
 					xCode = xFrag.appendPlaceholder(String.valueOf(code.getId()), code.getData());
 					break;
 				}
 				if ( code.hasReference() ) {
 					String data = getReferences(code.getData());
 					if ( data != null ) {
 						xCode.setSubFlows(data);
 					}
 				}
 				
 			}
 			else {
 				xFrag.append(ctext.charAt(i));
 			}
 		}
 
 		return xFrag;
 	}
 
 }
