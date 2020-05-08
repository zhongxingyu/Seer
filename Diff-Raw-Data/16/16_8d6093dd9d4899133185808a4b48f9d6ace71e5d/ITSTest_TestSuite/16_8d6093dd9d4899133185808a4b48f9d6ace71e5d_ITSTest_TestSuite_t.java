 /*===========================================================================
   Copyright (C) 2012 by the Okapi Framework contributors
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
 
 package org.w3c.its;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.Util;
 
 import org.junit.Test;
 
 public class ITSTest_TestSuite {
 
 	public static final String XML = "xml";
 	public static final String HTML = "html";
 	
 	private String root = TestUtil.getParentDir(this.getClass(), "/input.xml") + "/its2.0/inputdata";
 	private FileCompare fc = new FileCompare();
 	
 
 	@Test
 	public void process () throws URISyntaxException {
		processBatches(root+"/translate", Main.DC_TRANSLATE);
 //		processBatches(root+"/localizationnote", Main.DC_LOCALIZATIONNOTE);
 //		processBatches(root+"/terminology", Main.DC_TERMINOLOGY);
 //		processBatches(root+"/directionality", Main.DC_DIRECTIONALITY);
 //		processBatches(root+"/languageinformation", Main.DC_LANGUAGEINFORMATION);
 //		processBatches(root+"/elementswithintext", Main.DC_WITHINTEXT);
 //		processBatches(root+"/domain", Main.DC_DOMAIN);
 //		processBatches(root+"/disambiguation", Main.DC_DISAMBIGUATION);
 //		processBatches(root+"/localefilter", Main.DC_LOCALEFILTER);
		processBatches(root+"/externalresource", Main.DC_EXTERNALRESOURCE);
 //		processBatches(root+"/targetpointer", Main.DC_TARGETPOINTER);
		processBatches(root+"/idvalue", Main.DC_IDVALUE);
 		processBatches(root+"/preservespace", Main.DC_PRESERVESPACE);
 //		processBatches(root+"/locqualityissue", Main.DC_LOCQUALITYISSUE);
 //		processBatches(root+"/storagesize", Main.DC_STORAGESIZE);
		processBatches(root+"/mtconfidence", Main.DC_MTCONFIDENCE);
 //		processBatches(root+"/allowedcharacters", Main.DC_ALLOWEDCHARACTERS);
 	}
 	
 	/**
 	 * Shortcut to process both xml and html formats
 	 * @param base
 	 * @param category
 	 * @throws URISyntaxException
 	 */
 	public void processBatches (String base, String category) throws URISyntaxException {
 		processBatch(base+"/html", category);
 		processBatch(base+"/xml", category);
 	}
 	
 	/**
 	 * Process all files in specified folder
 	 * @param base
 	 * @param category
 	 * @throws URISyntaxException
 	 */
 	public void processBatch(String base, String category) throws URISyntaxException {
 		
 		removeOutput(base);
 		
 		File f = new File(base);
 		if (! f.exists())
 			return;
 		
 		String[] files = Util.getFilteredFiles(base, "");
 		
 		for (String file : files) {
 			if (file.contains("rules")) 
 				continue;
 			process(base + "/" + file, category);
 		}
 	}
 
 	private void removeOutput (String baseDir) {
 		String outDir = baseDir.replace("/inputdata/", "/output/");
 		Util.deleteDirectory(outDir, true);
 	}
 
 	private void process (String baseName,
 		String dataCategory)
 	{
 		String input = baseName;
 		String output = input.replace("/inputdata/", "/output/");
 		int n = output.lastIndexOf('.');
 		if ( n > -1 ) output = output.substring(0, n);
 		output += "output";
 		output += ".txt"; //Util.getExtension(input);
 		
 		Main.main(new String[]{input, output, "-dc", dataCategory});
 		assertTrue(new File(output).exists());
 		
 		String gold = output.replace("/output/", "/expected/");
		assertTrue(fc.compareFilesPerLines(output, gold, "UTF-8"));
//		fc.compareFilesPerLines(output, gold, "UTF-8");
 	}
 	
 }
