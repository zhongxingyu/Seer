 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.translationcomparison;
 
 import java.io.File;
 import java.net.URI;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.lib.translation.TextMatcher;
 
 @UsingParameters(Parameters.class)
 public class TranslationComparisonStep extends BasePipelineStep {
 
 	private Parameters params;
 	private IFilter filter2;
 	private IFilter filter3;
 	private TextMatcher matcher;
 	private XMLWriter writer;
 	private TMXWriter tmx;
 	private boolean isBaseMultilingual;
 	private boolean isInput2Multilingual;
 	private boolean isInput3Multilingual;
 	private String pathToOpen;
 	private int options;
 	private Property score1to2Prop;
 	private Property score1to3Prop;
 	private long scoreTotal;
 	private int itemCount;
 	private IFilterConfigurationMapper fcMapper;
 	private LocaleId targetLocale;
 	private LocaleId targetLocale2Extra;
 	private LocaleId targetLocale3Extra;
 	private LocaleId sourceLocale;
 	private URI inputURI;
 	private RawDocument rawDoc2;
 	private RawDocument rawDoc3;
 
 	public TranslationComparisonStep () {
 		params = new Parameters();
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 		this.fcMapper = fcMapper;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
 	public void setSourceLocale (LocaleId sourceLocale) {
 		this.sourceLocale = sourceLocale;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
 	public void setTargetLocale (LocaleId targetLocale) {
 		this.targetLocale = targetLocale;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
 	public void setInputURI (URI inputURI) {
 		this.inputURI = inputURI;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
 	public void setSecondInput (RawDocument secondInput) {
 		this.rawDoc2 = secondInput;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
 	public void setThirdInput (RawDocument thirdInput) {
 		this.rawDoc3 = thirdInput;
 	}
 	
 	@Override
 	public String getName () {
 		return "Translation Comparison";
 	}
 
 	@Override
 	public String getDescription () {
 		return "Compare the translated text units between several documents. "
 			+ "Expects: filter events. Sends back: filter events.";
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
  
 	@Override
 	protected Event handleStartBatch (Event event) {
 		// Both strings are in the target language.
 		matcher = new TextMatcher(targetLocale, targetLocale);
 		
 		if ( params.isGenerateHTML() ) {
 			writer = new XMLWriter(getOutputFilename());
 		}
 		// Start TMX writer (one for all input documents)
 		if ( params.isGenerateTMX() ) {
 			tmx = new TMXWriter(params.getTmxPath());
 			tmx.writeStartDocument(sourceLocale, targetLocale,
 				getClass().getName(), null, null, null, null);
 		}
 		pathToOpen = null;
 		score1to2Prop = new Property("Txt::Score", "", false);
 		targetLocale2Extra = LocaleId.fromString(targetLocale.toString()+params.getTarget2Suffix());
 		score1to3Prop = new Property("Txt::Score1to3", "", false);
 		targetLocale3Extra = LocaleId.fromString(targetLocale.toString()+params.getTarget3Suffix());
 		
 		options = 0;
 		if ( !params.isCaseSensitive() ) options |= TextMatcher.IGNORE_CASE;
 		if ( !params.isWhitespaceSensitive() ) options |= TextMatcher.IGNORE_WHITESPACES;
 		if ( !params.isPunctuationSensitive() ) options |= TextMatcher.IGNORE_PUNCTUATION;
 		
 		return event;
 	}
 	
 	@Override
 	protected Event handleEndBatch (Event event) {
 		matcher = null;
 		if ( params.isGenerateHTML() && ( writer != null )) {
 			writer.close();
 			writer = null;
 		}
 		if ( params.isGenerateTMX() && ( tmx != null )) {
 			tmx.writeEndDocument();
 			tmx.close();
 			tmx = null;
 		}
 		Runtime.getRuntime().gc();
 		if ( params.isAutoOpen() && ( pathToOpen != null )) {
 			Util.openURL((new File(pathToOpen)).getAbsolutePath());
 		}
 		
 		return event;
 	}
 	
 	@Override
 	protected Event handleStartDocument (Event event1) {
 		StartDocument startDoc1 = (StartDocument)event1.getResource();
 		initializeDocumentData();
 		isBaseMultilingual = startDoc1.isMultilingual();
 		
 		// Move to start document for second input
 		Event event2 = synchronize(filter2, EventType.START_DOCUMENT);
 		StartDocument startDoc2 = (StartDocument)event2.getResource();
 		isInput2Multilingual = startDoc2.isMultilingual();
 		
 		// Move to start document for third input
 		if ( filter3 != null ) {
 			Event event3 = synchronize(filter3, EventType.START_DOCUMENT);
 			StartDocument startDoc3 = (StartDocument)event3.getResource();
 			isInput3Multilingual = startDoc3.isMultilingual();
 		}
 		
 		scoreTotal = 0;
 		itemCount = 0;
 		
 		return event1;
 	}
 	
 	@Override
 	protected Event handleEndDocument (Event event) {
     	if ( filter2 != null ) {
     		filter2.close();
     	}
     	if ( filter3 != null ) {
     		filter3.close();
     	}
     	if ( params.isGenerateHTML() ) {
 			writer.writeEndElement(); // table
     		writer.writeElementString("p", String.format("", itemCount));
     		if ( itemCount > 0 ) {
     			writer.writeElementString("p", String.format("Number of items = %d. Average score = %.2f",
     				itemCount, (float)scoreTotal / itemCount));
     		}
 			writer.writeEndElement(); // body
 			writer.writeEndElement(); // html
     		writer.close();
     	}
     	
     	return event;
 	}
 	
 	@Override
 	protected Event handleTextUnit (Event event1) {
 		ITextUnit tu1 = event1.getTextUnit();
 		// Move to the next TU
 		Event event2 = synchronize(filter2, EventType.TEXT_UNIT);
 		Event event3 = null;
 		if ( filter3 != null ) {
 			event3 = synchronize(filter3, EventType.TEXT_UNIT);
 		}
 		// Skip non-translatable
 		if ( !tu1.isTranslatable() ) return event1;
 		
 		ITextUnit tu2 = event2.getTextUnit();
 		ITextUnit tu3 = null;
 		if ( event3 != null ) {
 			tu3 = event3.getTextUnit();
 		}
 
 		TextFragment srcFrag = null;
 		if ( isBaseMultilingual ) {
 			if ( tu1.getSource().contentIsOneSegment() ) {
 				srcFrag = tu1.getSource().getFirstContent();
 			}
 			else {
 				srcFrag = tu1.getSource().getUnSegmentedContentCopy();
 			}
 		}
 		else {
 			if ( isInput2Multilingual ) {
 				if ( tu2.getSource().contentIsOneSegment() ) {
 					srcFrag = tu2.getSource().getFirstContent();
 				}
 				else {
 					srcFrag = tu2.getSource().getUnSegmentedContentCopy();
 				}
 			}
 			else if (( tu3 != null ) && isInput3Multilingual ) {
 				if ( tu3.getSource().contentIsOneSegment() ) {
 					srcFrag = tu3.getSource().getFirstContent();
 				}
 				else {
 					srcFrag = tu3.getSource().getUnSegmentedContentCopy();
 				}
 			}
 		}
 		
 		// Get the text for the base translation
 		TextFragment trgFrag1;
 		if ( isBaseMultilingual ) {
 			if ( tu1.getTarget(targetLocale).contentIsOneSegment() ) {
 				trgFrag1 = tu1.getTarget(targetLocale).getFirstContent();
 			}
 			else {
 				trgFrag1 = tu1.getTarget(targetLocale).getUnSegmentedContentCopy();
 			}
 		}
 		else {
 			if ( tu1.getSource().contentIsOneSegment() ) {
 				trgFrag1 = tu1.getSource().getFirstContent();
 			}
 			else {
 				trgFrag1 = tu1.getSource().getUnSegmentedContentCopy();
 			}
 		}
 
 		// Get the text for the to-compare translation 1
 		TextFragment trgFrag2;
 		if ( isInput2Multilingual ) {
 			if ( tu2.getTarget(targetLocale).contentIsOneSegment() ) {
 				trgFrag2 = tu2.getTarget(targetLocale).getFirstContent();
 			}
 			else {
 				trgFrag2 = tu2.getTarget(targetLocale).getUnSegmentedContentCopy();
 			}
 		}
 		else {
 			if ( tu2.getSource().contentIsOneSegment() ) {
 				trgFrag2 = tu2.getSource().getFirstContent();
 			}
 			else {
 				trgFrag2 = tu2.getSource().getUnSegmentedContentCopy();
 			}
 		}
 		
 		// Get the text for the to-compare translation 2
 		TextFragment trgFrag3 = null;
 		if ( tu3 != null ) {
 			if ( isInput3Multilingual ) {
 				if ( tu3.getTarget(targetLocale).contentIsOneSegment() ) {
 					trgFrag3 = tu3.getTarget(targetLocale).getFirstContent();
 				}
 				else {
 					trgFrag3 = tu3.getTarget(targetLocale).getUnSegmentedContentCopy();
 				}
 			}
 			else {
 				if ( tu3.getSource().contentIsOneSegment() ) {
 					trgFrag3 = tu3.getSource().getFirstContent();
 				}
 				else {
 					trgFrag3 = tu3.getSource().getUnSegmentedContentCopy();
 				}
 			}
 		}
 		
 		// Do we have a base translation?
 		if ( trgFrag1 == null ) {
 			// No comparison if there is no base translation
 			return event1;
 		}
 		// Do we have a translation to compare to?
 		if ( trgFrag2 == null ) {
 			// Create and empty entry
 			trgFrag2 = new TextFragment();
 		}
 		if ( event3 != null ) {
 			if ( trgFrag3 == null ) {
 				// Create and empty entry
 				trgFrag3 = new TextFragment();
 			}
 		}
 		
 		// Compute the distance
 		int score1to2 = matcher.compare(trgFrag1, trgFrag2, options);
 		int score1to3 = -1;
 		int score2to3 = -1;
 		if ( event3 != null ) {
 			score1to3 = matcher.compare(trgFrag1, trgFrag3, options);
 			score2to3 = matcher.compare(trgFrag2, trgFrag3, options);
 		}
 		
 		// Store the scores for the average
 		scoreTotal += score1to2;
 		itemCount++;
 
 		// Output in HTML
 		if ( params.isGenerateHTML() ) {
 			writer.writeRawXML("<tr><td class='p'>"); //$NON-NLS-1$
 			// Output source if we have one
 			if ( srcFrag != null ) {
 				writer.writeString("Src:");
 				writer.writeRawXML("</td>"); //$NON-NLS-1$
 				writer.writeRawXML("<td class='p'>"); //$NON-NLS-1$
 				writer.writeString(srcFrag.toText());
 				writer.writeRawXML("</td></tr>\n"); //$NON-NLS-1$
 				writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
 			}
 			writer.writeString(params.getDocument1Label()+":");
 			writer.writeRawXML("</td>"); //$NON-NLS-1$
 			if ( srcFrag != null ) writer.writeRawXML("<td>"); //$NON-NLS-1$
 			else writer.writeRawXML("<td class='p'>"); //$NON-NLS-1$
 			writer.writeString(trgFrag1.toText());
 			writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
 			// T2
 			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
 			writer.writeString(params.getDocument2Label()+":");
 			writer.writeRawXML("</td><td>"); //$NON-NLS-1$
 			writer.writeString(trgFrag2.toText());
 			writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
 			// T3
 			if ( filter3 != null ) {
 				writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
 				writer.writeString(params.getDocument3Label()+":");
 				writer.writeRawXML("</td><td>"); //$NON-NLS-1$
 				writer.writeString(trgFrag3.toText());
 				writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
 			}
 			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
 			writer.writeString("Scores:");
 			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
 			writer.writeString(String.format("%s to %s = %d",
 				params.getDocument1Label(), params.getDocument2Label(), score1to2));
 			if ( score1to3 > -1 ) {
 				writer.writeString(String.format(",  %s to %s = %d",
 					params.getDocument1Label(), params.getDocument3Label(), score1to3));
 				writer.writeString(String.format(",  %s to %s = %d",
 					params.getDocument2Label(), params.getDocument3Label(), score2to3));
 			}
 			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
 		}
 
 		if ( params.isGenerateTMX() ) {
 			ITextUnit tmxTu = new TextUnit(tu1.getId());
 			// Set the source: Use the tu1 if possible
 			if ( isBaseMultilingual ) tmxTu.setSource(tu1.getSource());
 			else if ( srcFrag != null ) {
 				// Otherwise at least try to use the content of tu2
 				tmxTu.setSourceContent(srcFrag);
 			}
 			tmxTu.setTargetContent(targetLocale, trgFrag1);
 			tmxTu.setTargetContent(targetLocale2Extra, trgFrag2);
 			score1to2Prop.setValue(String.format("%03d", score1to2));
 			tmxTu.setTargetProperty(targetLocale2Extra, score1to2Prop);
 			if ( filter3 != null ) {
 				tmxTu.setTargetContent(targetLocale3Extra, trgFrag3);
 				score1to3Prop.setValue(String.format("%03d", score1to3));
 				tmxTu.setTargetProperty(targetLocale3Extra, score1to3Prop);
 			}
 			tmx.writeTUFull(tmxTu);
 		}
 		
 		return event1;
 	}
 
     private String getOutputFilename(){
        return inputURI.getPath() + ".html"; //$NON-NLS-1$
     }
 
 	private void initializeDocumentData () {
 		// Initialize the filter to read the translation 1 to compare
 		filter2 = fcMapper.createFilter(rawDoc2.getFilterConfigId(), filter2);
 		// Open the second input for this batch item
 		filter2.open(rawDoc2);
 
 		if ( rawDoc3 != null ) {
 			// Initialize the filter to read the translation 2 to compare
 			filter3 = fcMapper.createFilter(
 				rawDoc3.getFilterConfigId(), filter3);
 			// Open the third input for this batch item
 			filter3.open(rawDoc3);
 		}
 			
 		// Start HTML output
 		if ( writer != null ) writer.close();
 		if ( params.isGenerateHTML() ) {
 			// Use the to-compare file for the output name
 			if ( pathToOpen == null ) {
 				pathToOpen = getOutputFilename();
 			}
 			writer = new XMLWriter(getOutputFilename()); //$NON-NLS-1$
 			writer.writeStartDocument();
 			writer.writeStartElement("html"); //$NON-NLS-1$
 			writer.writeStartElement("head"); //$NON-NLS-1$
 			writer.writeRawXML("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"); //$NON-NLS-1$
 			writer.writeRawXML("<style>td { font-family: monospace } td { vertical-align: top; white-space: pre } td.p { border-top-style: solid; border-top-width: 1px;}</style>"); //$NON-NLS-1$
 			writer.writeEndElement(); // head
 			writer.writeStartElement("body"); //$NON-NLS-1$
 			writer.writeStartElement("p"); //$NON-NLS-1$
 			writer.writeString("Translation Comparison");
 			writer.writeEndElement();
 			writer.writeStartElement("p"); //$NON-NLS-1$
 			writer.writeString(String.format("Comparing %s (%s) against %s (%s)",
				rawDoc2.getInputURI(), params.getDocument1Label(), inputURI, params.getDocument2Label()));
 			if ( rawDoc3 != null ) {
 				writer.writeString(String.format(" and %s (%s)",
 					rawDoc3.getInputURI(), params.getDocument3Label()));
 			}
 			writer.writeString(".");
 			writer.writeEndElement();
 			writer.writeStartElement("table"); //$NON-NLS-1$
 		}
 	}
 
 	private Event synchronize (IFilter filter,
 		EventType untilType)
 	{
 		boolean found = false;
 		Event event = null;
 		while ( !found && filter.hasNext() ) {
 			event = filter.next();
 			found = (event.getEventType() == untilType);
     	}
    		if ( !found ) {
     		throw new RuntimeException("The document to compare is de-synchronized.");
     	}
    		return event;
 	}
 	
 }
