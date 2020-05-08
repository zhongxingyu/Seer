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
 
 package net.sf.okapi.steps.rainbowkit.ontram;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.filters.xini.XINIWriter;
 import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;
 
 public class OntramPackageWriter extends BasePackageWriter {
 
	private XINIWriter writer;
 
 	public OntramPackageWriter() {
 		super(Manifest.EXTRACTIONTYPE_ONTRAM);
		writer = new XINIWriter();
 	}
 
 	@Override
 	protected void processStartBatch() {
 		manifest.setSubDirectories("original", "xini", "xini", "translated", null, false);
 		setTMXInfo(false, null, false, false);
 
		writer = new XINIWriter();
 		writer.setOutputPath(manifest.getTempSourceDirectory() + "contents.xini");
 		
 		writer.init();
 		
 		super.processStartBatch();
 	}
 
 	@Override
 	protected void processStartDocument(Event event) {
 		super.processStartDocument(event);
 		
 		MergingInfo info = manifest.getItem(docId);
 		String inputPath = info.getRelativeInputPath();
 		writer.setNextPageName(inputPath);
 		
 		writer.handleEvent(event);
 	}
 	
 	@Override
 	protected void processEndBatch() {
 		super.processEndBatch();
 
 		writer.writeXINI();
 		close();
 	}
 
 	@Override
 	protected void processTextUnit (Event event) {
 		// Skip non-translatable
 		ITextUnit tu = event.getTextUnit();
 		if ( !tu.isTranslatable() ) return;
 		
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
 	public String getName() {
 		return getClass().getName();
 	}
 }
