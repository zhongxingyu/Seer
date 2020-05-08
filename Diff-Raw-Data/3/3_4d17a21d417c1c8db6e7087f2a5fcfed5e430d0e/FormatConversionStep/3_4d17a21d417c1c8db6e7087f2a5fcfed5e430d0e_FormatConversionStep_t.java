 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.formatconversion;
 
 import java.io.File;
 import java.net.URI;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.filterwriter.TMXFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.pensieve.PensieveFilterWriter;
 import net.sf.okapi.filters.po.POFilterWriter;
 
 @UsingParameters(Parameters.class)
 public class FormatConversionStep extends BasePipelineStep {
 
 	private static final int PO_OUTPUT = 0;
 	private static final int TMX_OUTPUT = 1;
 	private static final int TABLE_OUTPUT = 2;
 	private static final int PENSIEVE_OUTPUT = 3;
 	
 	private Parameters params;
 	private IFilterWriter writer;
 	private boolean firstOutputCreated;
 	private int outputType;
 	private URI outputURI;
 	private LocaleId targetLocale;
 
 	public FormatConversionStep () {
 		params = new Parameters();
 	}
 
 	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
 	public void setOutputURI (URI outputURI) {
 		this.outputURI = outputURI;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
 	public void setTargetLocale (LocaleId targetLocale) {
 		this.targetLocale = targetLocale;
 	}
 	
 	public String getDescription () {
 		return "Converts the output of a filter into a specified file format."
 			+ " Expects: filter events. Sends back: filter events.";
 	}
 
 	public String getName () {
 		return "Format Conversion";
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters) params;
 	}
 
 	@Override
 	public Parameters getParameters () {
 		return params;
 	}
 	
 	public Event handleEvent (Event event) {
 		switch (event.getEventType()) {
 		case START_BATCH:
 			firstOutputCreated = false;
 			if ( params.getOutputFormat().equals(Parameters.FORMAT_PO) ) {
 				outputType = PO_OUTPUT;
 				createPOWriter();
 			}
 			else if ( params.getOutputFormat().equals(Parameters.FORMAT_TMX) ) {
 				outputType = TMX_OUTPUT;
 				createTMXWriter();
 			}
 			else if ( params.getOutputFormat().equals(Parameters.FORMAT_PENSIEVE) ) {
 				outputType = PENSIEVE_OUTPUT;
 				createPensieveWriter();
 			}
 			else if ( params.getOutputFormat().equals(Parameters.FORMAT_TABLE) ) {
 				outputType = TABLE_OUTPUT;
 				createTableWriter();
 			}
 			// Start sending event to the writer
 			writer.handleEvent(event);
 			break;
 			
 		case END_BATCH:
 			if ( params.isSingleOutput() ) {
 				Ending ending = new Ending("end");
 				writer.handleEvent(new Event(EventType.END_DOCUMENT, ending));
 				writer.close();
 			}
 			break;
 			
 		case START_DOCUMENT:
 			if ( !firstOutputCreated || !params.isSingleOutput() ) {
 				switch ( outputType ) {
 				case PO_OUTPUT:
 					startPOOutput();
 					break;
 				case TMX_OUTPUT:
 					startTMXOutput();
 					break;
 				case TABLE_OUTPUT:
 					startTableOutput();
 					break;
 				case PENSIEVE_OUTPUT:
 					startPensieveOutput();
 					break;
 				}
				writer.handleEvent(event);
 			}
 			break;
 			
 		case END_DOCUMENT:
 			if ( !params.isSingleOutput() ) {
 				writer.handleEvent(event);
 				writer.close();
 			}
 			// Else: Do nothing
 			break;
 			
 		case START_SUBDOCUMENT:
 		case END_SUBDOCUMENT:
 		case START_GROUP:
 		case END_GROUP:
 			writer.handleEvent(event);
 			break;
 
 		case TEXT_UNIT:
 			processTextUnit(event);
 			break;
 			
 		case START_BATCH_ITEM:
 		case END_BATCH_ITEM:
 		case RAW_DOCUMENT:
 		case DOCUMENT_PART:
 		case CUSTOM:
 			// Do nothing
 			break;
 		}
 		return event;
 	}
 
 	protected void processTextUnit (Event event) {
 		TextUnit tu = (TextUnit)event.getResource();
 		
 		// Skip empty or code-only entries
 		if ( params.getSkipEntriesWithoutText() ) {
 			if ( !tu.getSource().hasText(true, false) ) return;
 		}
 		
 		// If requested, overwrite the target
 		switch ( params.getTargetStyle() ) {
 		case Parameters.TRG_FORCEEMPTY:
 			tu.createTarget(targetLocale, true, IResource.CREATE_EMPTY);
 			break;
 		case Parameters.TRG_FORCESOURCE:
 			tu.createTarget(targetLocale, true, IResource.COPY_ALL);
 			break;
 		}
 		writer.handleEvent(event);
 	}
 
 	private void createPOWriter () {
 		writer = new POFilterWriter();
 		net.sf.okapi.filters.po.Parameters outParams = (net.sf.okapi.filters.po.Parameters)writer.getParameters();
 		outParams.outputGeneric = params.getUseGenericCodes();
 	}
 	
 	private void startPOOutput () {
 		File outFile;
 		if ( params.isSingleOutput() ) {
 			outFile = new File(params.getOutputPath());
 		}
 		else {
 			outFile = new File(outputURI);
 		}
 		// Not needed, writer does this: Util.createDirectories(outFile.getAbsolutePath());
 		writer.setOutput(outFile.getPath());
 		writer.setOptions(targetLocale, "UTF-8");
 		firstOutputCreated = true;
 	}
 
 	private void createTMXWriter () {
 		writer = new TMXFilterWriter();
 //		net.sf.okapi.filters.po.Parameters outParams = (net.sf.okapi.filters.po.Parameters)writer.getParameters();
 //		outParams.outputGeneric = params.getUseGenericCodes();
 	}
 	
 	private void startTMXOutput () {
 		File outFile;
 		if ( params.isSingleOutput() ) {
 			outFile = new File(params.getOutputPath());
 		}
 		else {
 			outFile = new File(outputURI);
 		}
 		writer.setOutput(outFile.getPath());
 		writer.setOptions(targetLocale, "UTF-8");
 		firstOutputCreated = true;
 	}
 
 	private void createTableWriter () {
 		writer = new TableFilterWriter();
 		TableFilterWriterParameters options = (TableFilterWriterParameters)writer.getParameters();
 		options.fromString(params.getFormatOptions());
 	}
 	
 	private void startTableOutput () {
 		File outFile;
 		if ( params.isSingleOutput() ) {
 			outFile = new File(params.getOutputPath());
 		}
 		else {
 			outFile = new File(outputURI);
 		}
 		// Not needed, writer does this: Util.createDirectories(outFile.getAbsolutePath());
 		writer.setOutput(outFile.getPath());
 		writer.setOptions(targetLocale, "UTF-8");
 		firstOutputCreated = true;
 	}
 
 	private void createPensieveWriter () {
 		writer = new PensieveFilterWriter();
 	}
 
 	private void startPensieveOutput () {
 		writer.setOutput(params.getOutputPath());
 		writer.setOptions(targetLocale, "UTF-8");
 	}
 
 }
