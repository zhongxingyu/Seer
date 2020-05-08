 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 package net.sf.okapi.filters.openxml.tests;
 
 import java.io.File;
 import java.io.FilenameFilter;
 //import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.logging.Level; // DWH 4-22-09
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.resource.RawDocument;
 //import net.sf.okapi.filters.markupfilter.Parameters;
 //import net.sf.okapi.filters.openxml.OpenXMLContentFilter;
 import net.sf.okapi.filters.openxml.OpenXMLFilter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class OpenXMLZipFullFileTest {
 	private OpenXMLFilter openXMLFilter;
 	private String[] testFileList;
 	private static final String deary="/data/";
 
 	@Before
 	public void setUp() throws Exception {
		openXMLFilter = new OpenXMLFilter();
 		openXMLFilter.setOptions("en", "UTF-8", true);
 
 		// read all files in the test html directory
 		URL url = OpenXMLZipFullFileTest.class.getResource("anchor.txt");
 		File dir = new File(url.toURI()).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
 		File dir2 = new File(dir.getAbsolutePath()+"\\data");
 
 		FilenameFilter filter = new FilenameFilter() {
 			public boolean accept(File dir2, String name) {
 				return ((name.endsWith(".docx") || name.endsWith(".pptx") || name.endsWith(".xlsx")) && !name.startsWith("Output"));
 			}
 		};
 		testFileList = dir2.list(filter);
 	}
 
 	@After
 	public void tearDown() {
 		openXMLFilter.close();
 	}
 
 	@Test
 	public void testAll() throws URISyntaxException {
 		Event event;
 		String base=System.getProperty("user.dir").replace('\\','/').toLowerCase();
 		for (String f : testFileList) {
 			String ff = base+deary+f;
 			String fff = ff.replace(" ","%20");
 			try {
 				URI uriFf = new URI(fff);
 				openXMLFilter.open(new RawDocument(uriFf,"UTF-8","en-US"),true,true,Level.FINEST); // DWH 4-22-09
 				while (openXMLFilter.hasNext()) {
 					event = openXMLFilter.next();
 				}
 			} catch (Exception e) {
 				//System.err.println("Error for file: " + f + ": " + e.toString());
 				throw new RuntimeException("Error for file: " + f + ": " + e.toString());
 			}
 		}
 	}
 
 	@Test
 	public void testNonwellformed() {
 		String filename = "/nonwellformed.specialtest";
 		try
 		{
 			URI uriFf = new URI(filename);
 			openXMLFilter.open(uriFf,true,Level.FINEST); // DWH 4-22-09
 			while (openXMLFilter.hasNext())
 			{
 				Event event = openXMLFilter.next();
 			}
 			throw new RuntimeException("Should have recognized" + filename + " is not an MSOffice 2007 file");
 		}
 		catch(Exception e)
 		{
 			//System.err.println("Error for file: " + f + ": " + e.toString());
 			filename = "All is swell";
 		}
 	}
 }
