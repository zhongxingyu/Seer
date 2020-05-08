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
 
 package net.sf.okapi.steps.rainbowkit.creation;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.util.regex.Pattern;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.TestUtil;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.pipelinedriver.BatchItemContext;
 import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
 import net.sf.okapi.common.pipelinedriver.PipelineDriver;
 import net.sf.okapi.filters.openoffice.OpenOfficeFilter;
 import net.sf.okapi.filters.properties.PropertiesFilter;
 import net.sf.okapi.filters.xliff.XLIFFFilter;
 import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
 import net.sf.okapi.steps.common.createtarget.CreateTargetStep;
 import net.sf.okapi.steps.rainbowkit.ontram.OntramPackageWriter;
 
 import net.sf.okapi.common.TestUtil;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ExtractionStepTest {
 	
 	private String root;
 	private LocaleId locEN = LocaleId.fromString("en");
 	private LocaleId locFR = LocaleId.fromString("fr");
 	private LocaleId locENUS = LocaleId.fromString("en-us");
 	private LocaleId locRURU = LocaleId.fromString("ru-ru");
 	
 	@Before
 	public void setUp() {
 		root = TestUtil.getParentDir(this.getClass(), "/test01.properties");
		// Java returns paths with a leading slash even on Windows; this
		// does not represent what will really be passed to the step in practice.
		// Strip the leading slash if we're on Windows with a drive-letter path.
		if (System.getProperty("os.name").startsWith("Windows") && Pattern.matches("^/[A-Z]:.*$", root))
			root = root.substring(1);
 	}
 
 	@Test
 	public void stub () {
 		assertTrue(true);
 	}
 	
 	@Test
 	public void testSimpleStep ()
 		throws URISyntaxException
 	{
 		// Ensure output is deleted
 		assertTrue(deleteOutputDir("pack1", true));
 		
 		IPipelineDriver pdriver = new PipelineDriver();
 		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
 		fcMapper.addConfigurations(PropertiesFilter.class.getName());
 		fcMapper.addConfigurations(OpenOfficeFilter.class.getName());
 		pdriver.setFilterConfigurationMapper(fcMapper);
 		pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root)); // Don't include final separator
 		pdriver.addStep(new RawDocumentToFilterEventsStep());
 		pdriver.addStep(new ExtractionStep());
 		
 		String inputPath = root+"/test01.properties";
 		String outputPath = inputPath.replace("test01.", "test01.out.");
 		URI inputURI = new File(inputPath).toURI();
 		URI outputURI = new File(outputPath).toURI();
 		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_properties", outputURI, "UTF-8", locEN, locFR));
 		
 		inputPath = root+"/sub Dir/test01.odt";
 		outputPath = inputPath.replace("test01.", "test01.out.");
 		inputURI = new File(inputPath).toURI();
 		outputURI = new File(outputPath).toURI();
 		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_openoffice", outputURI, "UTF-8", locEN, locFR));
 		
 		pdriver.processBatch();
 
 		File file = new File(root+"pack1/work/test01.properties.xlf");
 		assertTrue(file.exists());
 		file = new File(root+"pack1/work/sub Dir/test01.odt.xlf");
 		assertTrue(file.exists());
 		
 	}
 	
 	@Test
 	public void testXINICreation ()	throws URISyntaxException, IOException
 	{
 		// Ensure output is deleted
 		assertTrue(deleteOutputDir("pack2", true));
 		
 		IPipelineDriver pdriver = new PipelineDriver();
 		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
 		fcMapper.addConfigurations(XLIFFFilter.class.getName());
 		pdriver.setFilterConfigurationMapper(fcMapper);
 		pdriver.setRootDirectories(Util.deleteLastChar(root), 
 				Util.deleteLastChar(root)); // Don't include final separator
 		pdriver.addStep(new RawDocumentToFilterEventsStep());
 		pdriver.addStep(new CreateTargetStep());
 		
 		ExtractionStep es = new ExtractionStep();
 		pdriver.addStep(es);
 		Parameters params = (Parameters) es.getParameters();
 		params.setWriterClass(OntramPackageWriter.class.getName());
 		params.setPackageName("pack2");
 		
 		String inputPath = root+"xiniPack/original/test1.xlf";
 		String outputPath = root+"pack2/original/test1.out.xlf";
 		URI inputURI = new File(inputPath).toURI();
 		URI outputURI = new File(outputPath).toURI();
 		pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, 
 				"UTF-8", locENUS, locRURU));
 		
 		pdriver.processBatch();
 
 		File file = new File(root+"pack2/xini/contents.xini");
 		assertTrue(file.exists());
 		
 		// Compare with the gold file
 		TestUtil.assertEquivalentXml(
 				TestUtil.getFileAsString(new File(root+"xiniPack/xini/contents.xini")), 
 				TestUtil.getFileAsString(new File(root+"pack2/xini/contents.xini"))
 					.replaceFirst("xiniPack/original", ""));
 		assertTrue(deleteOutputDir("pack2", true));
 	}
 	
     public boolean deleteOutputDir (String dirname, boolean relative) {
     	File d;
     	if ( relative ) d = new File(root + File.separator + dirname);
     	else d = new File(dirname);
     	if ( d.isDirectory() ) {
     		String[] children = d.list();
     		for ( int i=0; i<children.length; i++ ) {
     			boolean success = deleteOutputDir(d.getAbsolutePath() + File.separator + children[i], false);
     			if ( !success ) {
     				return false;
     			}
     		}
     	}
     	if ( d.exists() ) return d.delete();
     	else return true;
     }
     
 
 }
