 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.filterwriter.XLIFFWriter;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;
 
 public class XLIFFPackageWriter extends BasePackageWriter {
 
 	private XLIFFWriter writer;
 
 	public XLIFFPackageWriter () {
 		super(Manifest.EXTRACTIONTYPE_XLIFF);
 	}
 	
 	@Override
 	protected void processStartBatch () {
 		manifest.setSubDirectories("original", "work", "work", "done");
 		super.processStartBatch();
 	}
 	
 	@Override
 	protected void processStartDocument (Event event) {
 		super.processStartDocument(event);
 		
 		writer = new XLIFFWriter();
 		writer.setOptions(manifest.getTargetLocale(), "UTF-8");
 		MergingInfo item = manifest.getItem(docId);
 		String path = manifest.getSourceDirectory() + item.getRelativeInputPath() + ".xlf";
 		writer.setOutput(path); // Not really used, but doesn't hurt just in case
 
 		//TODO: get it from params
 		writer.setPlaceholderMode(true);
 		
 		StartDocument sd = event.getStartDocument();
 		writer.create(path, null, manifest.getSourceLocale(), manifest.getTargetLocale(),
 			sd.getMimeType(), item.getRelativeInputPath(), null);
 
 	}
 	
 	@Override
 	protected void processEndDocument (Event event) {
 		writer.handleEvent(event);
 		if ( writer != null ) {
 			writer.close();
 			writer = null;
 		}
 
 		super.processEndDocument(event);
 	}
 
 	@Override
 	protected void processStartSubDocument (Event event) {
 		writer.handleEvent(event);
 	}
 	
 	@Override
 	protected void processEndSubDocument (Event event) {
 		writer.handleEvent(event);
 	}
 	
 	@Override
	protected void processStartGroup (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
	protected void processEndGroup (Event event) {
		writer.handleEvent(event);
	}
	
	@Override
 	protected void processTextUnit (Event event) {
 		writer.handleEvent(event);
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
 }
