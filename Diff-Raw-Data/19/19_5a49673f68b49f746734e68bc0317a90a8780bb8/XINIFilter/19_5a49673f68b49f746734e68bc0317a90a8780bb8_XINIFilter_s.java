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
 
 package net.sf.okapi.filters.xini;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 @UsingParameters(Parameters.class)
 public class XINIFilter implements IFilter {
 	private Parameters params;
 	private EncoderManager encoderManager;
 	private XINIReader reader;
 	private LinkedList<Event> queue;
 	private String relDocName;
 	
 	public XINIFilter () {
 		params = new Parameters();
 		queue = new LinkedList<Event>();
 	}
 
 	public XINIFilter(String relDocName) {
 		this();
 		this.relDocName = relDocName;
 	}
 
 	@Override
 	public void cancel () {
 		//TODO
 	}
 
 	@Override
 	public void close () {
		reader.close();
 	}
 
 	@Override
 	public String getName () {
 		return "okf_xini";
 	}
 
 	@Override
 	public String getDisplayName () {
 		return "XINI Filter";
 	}
 
 	@Override
 	public String getMimeType () {
 		return MimeTypeMapper.XINI_MIME_TYPE;
 	}
 
 	@Override
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.XINI_MIME_TYPE,
 			getClass().getName(),
 			"XINI",
 			"Configuration for XINI documents from ONTRAM.",
 			null,
 			".xini;"));
 		return list;
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MimeTypeMapper.XINI_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
 		}
 		return encoderManager;
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public boolean hasNext () {
 		return !queue.isEmpty();
 	}
 
 	@Override
 	public Event next () {
 		return queue.poll();
 	}
 
 	@Override
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 
 	@Override
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close();
 		
 		// get events
 		reader = new XINIReader();
 		reader.open(input);
 		queue.addAll(reader.getFilterEvents(relDocName));
 	}
 
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		//TODO
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new XINIWriter();
 	}
 
 }
