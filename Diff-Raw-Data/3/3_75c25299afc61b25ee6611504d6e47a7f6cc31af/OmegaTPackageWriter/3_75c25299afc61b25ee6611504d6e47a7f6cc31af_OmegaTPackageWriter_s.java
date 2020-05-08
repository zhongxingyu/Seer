 /*===========================================================================
   Copyright (C) 2010-2013 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.omegat;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.steps.rainbowkit.xliff.XLIFFPackageWriter;
 
 public class OmegaTPackageWriter extends XLIFFPackageWriter {
 
 	public static final String OKAPI_HOME = "OKAPI_HOME";
 	
 	Options options;
 	
 	public OmegaTPackageWriter () {
 		super();
 		options = new Options();
 		extractionType = Manifest.EXTRACTIONTYPE_OMEGAT;
 	}
 	
 	@Override
 	protected void processStartBatch () {
 		setForOmegat(true);
 		manifest.setSubDirectories("original", "source", "target", "done", "tm", true);
 		setTMXInfo(true, manifest.getPackageRoot()+"omegat"+File.separator+"project_save.tmx", true, true);
 		super.processStartBatch();
 	}
 	
 	@Override
 	protected void processEndBatch () {
 		// Force creation of needed sub-directories even if empty
 		Util.createDirectories(manifest.getPackageRoot()+"omegat/");
 		Util.createDirectories(manifest.getPackageRoot()+"glossary/");
 		Util.createDirectories(manifest.getTempTargetDirectory());
 		Util.createDirectories(manifest.getTempTmDirectory());
 
 		// Write the OmegaT project file
 		createOmegaTProject();
 		
 		// Call base class method
 		super.processEndBatch();
 	}
 	
 	@Override
 	protected void processStartDocument (Event event) {
 		// Set the writer's options
 		// Get the options from the parameters
 		if ( !Util.isEmpty(params.getWriterOptions()) ) {
 			options.fromString(params.getWriterOptions());
 		}
 		super.processStartDocument(event);
 	}
 	
 	private void createOmegaTProject () {
 		XMLWriter XR = null;
 		try {
 			XR = new XMLWriter(manifest.getPackageRoot() + "omegat.project");
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
 			
 			XR.writeStartElement("dictionary_dir");
 			XR.writeRawXML("__DEFAULT__");
 			XR.writeEndElementLineBreak(); // dictionary_dir
 			
 			XR.writeStartElement("source_lang");
 			XR.writeRawXML(manifest.getSourceLocale().toString());
 			XR.writeEndElementLineBreak(); // source_lang
 
 			XR.writeStartElement("target_lang");
 			XR.writeRawXML(manifest.getTargetLocale().toString());
 			XR.writeEndElementLineBreak(); // target_lang
 
 			XR.writeStartElement("sentence_seg");
 			// If the data are pre-segmented set the project with no segmentation
 			// Otherwise use the user's choice
 			XR.writeRawXML(getPreSegmented() ? "false" : (options.getAllowSegmentation() ? "true" : "false"));
 			XR.writeEndElementLineBreak(); // sentence_seg
 
 			// Include post-processing hook to trigger the Translation Kit Post-Processing pipeline
 			// IMPORTANT: Part of this code cannot be tested in debug mode
 			// (i.e. is not complied in a jar file)
 			if ( options.getIncludePostProcessingHook() ) {
 				
 				String jarPath = null;
 				
 				// First check if there's a valid OKAPI_HOME envar
 				String home = System.getenv().get(OKAPI_HOME);
 				if ( home != null ) {
 					String jarRelPath = "lib" + File.separator + "rainbow.jar";
 					File jar = new File(home, jarRelPath);
 					try {
 						if ( jar.exists() && jar.isFile() ) {
 							jarPath = String.format("${%s}%s%s",
 								OKAPI_HOME,
 								home.endsWith(File.separator) ? "" : File.separator,
 								jarRelPath);
 						}
 					} catch (SecurityException e) {
 						// Nothing
 					}
 				}
 				
 				// Next try the ClassLoader
 				if ( jarPath == null ) {
 					try {
 						// Note for debugging: this will return null if the code is not in a jar
 						File jar = new File(ClassLoader.getSystemResource("rainbow.jar").toURI());
 						jarPath = jar.getAbsolutePath();
 					} catch (NullPointerException e) {
 						// The above just doesn't work in some environments, e.g. Jython:
 						// getSystemResource() returns null.
 					} catch (URISyntaxException e) {
 						// Nothing
 					}
 				}
 				
 				// Finally, write the element only if we have a path
 				if ( jarPath != null ) {
					String externalCmd = String.format("java -jar \"%s\" -x TranslationKitPostProcessing -np \"${projectRoot}manifest.rkm\" -fc okf_rainbowkit-noprompt",
 						jarPath);
 					XR.writeStartElement("external_command");
 					XR.writeString(externalCmd);
 					XR.writeEndElementLineBreak(); // external_command
 				}
 			}
 
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
