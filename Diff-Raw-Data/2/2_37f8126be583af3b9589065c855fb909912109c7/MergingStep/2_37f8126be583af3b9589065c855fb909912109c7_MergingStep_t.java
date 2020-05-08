 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.postprocess;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 
 @UsingParameters(Parameters.class)
 public class MergingStep extends BasePipelineStep {
 
 	public static final String NAME = "Rainbow Translation Kit Merging";
 	
 	private Parameters params;
 	private MergingInfo info;
 	private Merger merger;
 	private IFilterConfigurationMapper fcMapper;
 	private LocaleId targetLocale;
 
 	public MergingStep () {
 		super();
 		params = new Parameters();
 	}
 
 	@Override
 	public String getDescription () {
 		return "Post-process a Rainbow translation kit."
 			+ " Expects: filter events. Sends back: filter events or raw documents.";
 	}
 
 	@Override
 	public String getName () {
 		return NAME;
 	}
 
 	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 		this.fcMapper = fcMapper;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
 	public void setTargetLocale (LocaleId targetLocale) {
 		this.targetLocale = targetLocale; 
 	}
 	
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			return handleStartDocument(event);
 		default:
 			if ( merger != null ) {
 				return merger.handleEvent(event);
 			}
 		}
 		return event;
 	}
 
 	@Override
 	protected Event handleStartDocument (Event event) {
 		// Initial document is expected to be a manifest
 		StartDocument sd = event.getStartDocument();
 		info = sd.getAnnotation(MergingInfo.class);
 		if ( info == null ) {
 			throw new OkapiBadFilterInputException("Start document is missing the merging info annotation.");
 		}
 		Manifest manifest = sd.getAnnotation(Manifest.class);
 		if ( manifest == null ) {
 			throw new OkapiBadFilterInputException("Start document is missing the manifest annotation.");
 		}
 		
 		// Create the merger (for each new manifest)
		boolean alwaysForceTargetLocale = Manifest.EXTRACTIONTYPE_ONTRAM.equals(info.getExtractionType());
 		LocaleId targetLocaleToUse;
 		if ( params.getForceTargetLocale() || alwaysForceTargetLocale) {
 			targetLocaleToUse = targetLocale;
 		}
 		else {
 			targetLocaleToUse = null;
 		}
 		merger = new Merger(manifest, fcMapper, params.getPreserveSegmentation(),
 				targetLocaleToUse, params.getReturnRawDocument(), params.getOverrideOutputPath());
 		
 		// And trigger the merging
 		return merger.startMerging(info, event);
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 	
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 }
