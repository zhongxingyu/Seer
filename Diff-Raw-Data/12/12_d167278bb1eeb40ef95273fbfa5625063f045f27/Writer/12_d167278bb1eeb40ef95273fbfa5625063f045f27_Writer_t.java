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
 
 package net.sf.okapi.applications.rainbow.packages.omegat;
 
 import java.io.File;
 import java.util.List;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.common.annotation.ScoresAnnotation;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 
 public class Writer extends net.sf.okapi.applications.rainbow.packages.xliff.Writer {
 
 	@Override
 	public String getPackageType () {
 		return "omegat";
 	}
 	
 	@Override
 	public String getReaderClass () {
 		//TODO: Use dynamic name
 		return "net.sf.okapi.applications.rainbow.packages.omegat.Reader";
 	}
 	
 	@Override
 	public void writeStartPackage () {
 		// Set any non-default folders before calling the base class method.
 		manifest.setSourceLocation("source");
 		manifest.setTargetLocation("target");
 		tmxPathApproved = manifest.getRoot() + File.separator + "omegat" + File.separator
 			+ "project_save.tmx";
 		tmxPathUnApproved = manifest.getRoot() + File.separator + "tm" + File.separator
 			+ "unapproved.tmx";
 		tmxPathAlternate = manifest.getRoot() + File.separator + "tm" + File.separator
 			+ "alternate.tmx";
 		tmxPathLeverage = manifest.getRoot() + File.separator + "tm" + File.separator
 			+ "leverage.tmx";
 
 		// Force the creation tool setting (needed for TMX with OmegaT workarounds)
 		creationTool = "OmegaT";
 
 		// Call the base class method
 		super.writeStartPackage();
 		
 		tmxWriterApproved.setOmegaTWorkarounds(true);
 		tmxWriterUnApproved.setOmegaTWorkarounds(true);
 		tmxWriterAlternate.setOmegaTWorkarounds(true);
 		tmxWriterLeverage.setOmegaTWorkarounds(true);
 
 		// Force OmegaT-specific settings
 		options.gMode = true;
 		options.message = "THIS FILE IS INTENDED TO BE USED WITH OMEGAT ONLY. "
 			+ "It uses specific XLIFF constructs in ways that may not work with other XLIFF tools.";
 		
 		// Create the OmegaT-specific directories
 		Util.createDirectories(manifest.getRoot() + File.separator + "glossary" + File.separator);
 		Util.createDirectories(manifest.getRoot() + File.separator + "omegat" + File.separator);
 		Util.createDirectories(manifest.getRoot() + File.separator + "tm" + File.separator);
 	}
 
 	@Override
 	public void writeEndPackage (boolean p_bCreateZip) {
 		// Write the OmegaT project file
 		createOmegaTProject();
 		// And then, normal ending
 		super.writeEndPackage(p_bCreateZip);
 	}
 
 	@Override
 	public void writeScoredItem (TextUnit item,
 		ScoresAnnotation scores)
 	{
 		// In OmegaT we put both the approved and exact match of the project_save.tmx (the 'approved' one). 
 		String tuid = item.getName();
 		TextContainer srcTC = item.getSource();
 		TextContainer trgTC = item.getTarget(trgLoc);
 
 		if ( !srcTC.isSegmented() ) { // Source is not segmented
 			if ( scores.getScore(0) == 100 ) {
 				tmxWriterApproved.writeTU(srcTC, trgTC, tuid, null);
 			}
 			else if ( scores.getScore(0) > 0 ) {
 				tmxWriterLeverage.writeTU(srcTC, trgTC, tuid, null);
 			}
 			// Else: skip score of 0
 		}
 		else if ( trgTC.isSegmented() ) { // Source AND target are segmented
 			// Write the segments
 			List<Segment> srcList = srcTC.getSegments();
 			List<Segment> trgList = trgTC.getSegments();
 			for ( int i=0; i<srcList.size(); i++ ) {
 				if ( scores.getScore(i) >= 100 ) {
 					tmxWriterApproved.writeTU(srcList.get(i).text,
 						(i>trgList.size()-1) ? null : trgList.get(i).text,
						((tuid==null) ? null : String.format("%s_s%02d", tuid, i+1)),
 						null);
 				}
 				else if ( scores.getScore(i) > 0 ) {
 					TextFragment tf = srcList.get(i).text.clone();
 					tf.setCodedText(Util.ORIGIN_MT+" "+tf.getCodedText());
 					tmxWriterLeverage.writeTU(tf,
 						(i>trgList.size()-1) ? null : trgList.get(i).text,
						((tuid==null) ? null : String.format("%s_s%02d", tuid, i+1)),
 						null);
 				}
 			}
 			// Else: skip score of 0
 		}
 		// Else no TMX output needed for source segmented but not target
 	}
 	
 	private void createOmegaTProject () {
 		XMLWriter XR = null;
 		try {
 			XR = new XMLWriter(manifest.getRoot() + File.separator + "omegat.project");
 			XR.writeStartDocument();
 			XR.writeStartElement("omegat");
 			XR.writeStartElement("project");
 			XR.writeAttributeString("version", "1.0");
 
 			XR.writeStartElement("source_dir");
 			XR.writeRawXML("__DEFAULT__");
 			XR.writeEndElementLineBreak(); // source_dir
 			
 			XR.writeStartElement("target_dir");
 			XR.writeRawXML("__DEFAULT__");
 			XR.writeEndElementLineBreak(); // target_dir
 			
 			XR.writeStartElement("tm_dir");
 			XR.writeRawXML("__DEFAULT__");
 			XR.writeEndElementLineBreak(); // tm_dir
 			
 			XR.writeStartElement("glossary_dir");
 			XR.writeRawXML("__DEFAULT__");
 			XR.writeEndElementLineBreak(); // glossary_dir
 			
 			XR.writeStartElement("source_lang");
 			XR.writeRawXML(manifest.getSourceLanguage().toBCP47());
 			XR.writeEndElementLineBreak(); // source_lang
 
 			XR.writeStartElement("target_lang");
 			XR.writeRawXML(manifest.getTargetLanguage().toBCP47());
 			XR.writeEndElementLineBreak(); // target_lang
 
 			XR.writeStartElement("sentence_seg");
 			XR.writeRawXML(preSegmented ? "false" : "true");
 			XR.writeEndElementLineBreak(); // sentence_seg
 
 			XR.writeEndElementLineBreak(); // project
 			XR.writeEndElement(); // omegat
 		}
 		finally {
 			if ( XR != null ) {
 				XR.writeEndDocument();
 				XR.close();
 			}
 		}
 	}
 }
