 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.StringReader;
 import java.util.Iterator;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.meandre.components.test.framework.ComponentTesterBase;
 import org.seasr.meandre.components.tools.ModelVocabulary;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 
 /** The base class for performing component testing.
  *
  * @author Xavier Llor&agrave;
  *
  */
 public class JSTORComponentTests {
 
 	/** The component test base supporting class for these tests. */
 	private static ComponentTesterBase ctb = null;
 
 	/** Generates the descriptors for the specified folders */
 	@BeforeClass
 	public static void initializeTestResources () {
 
 		ctb = new ComponentTesterBase();
 		ctb.setBaseTestPort(50000);
		ctb.setFlowsFolder("test" + File.separator + "flows" + File.separator + "jstor");
		ctb.setTempDescriptorFolder("tmp");
		ctb.setTempDescriptorFolder("tmp" + File.separator + "desc" + File.separator + "jstor");
		ctb.setSourceFolders(new String [] { "src-analytics", "src-jstor", "src-nlp", "src-tools", "src-transform", "src-vis" });
 		ctb.initialize();
 	}
 
     /** Generates the descriptors for the specified folders */
 	@AfterClass
 	public static void destroyTestResources () {
 		// Clean the temp folder
 		ctb.printCoverageReport();
 		//ctb.destroy();
 	}
 
 	/** The test of the basic XML reading and writing to text components. */
 	@SuppressWarnings("unchecked")
 	@Test
	public void testXMLReadingWritingTest() {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ByteArrayOutputStream err = new ByteArrayOutputStream();
 
 		ctb.runZigZag(ctb.getZigZag("jstor-page-text-extraction.zz"),out,err);
 
 		Model model = ModelFactory.createDefaultModel();
 		model.read(new StringReader(out.toString()),null,"N-TRIPLE");
 		assertTrue(model.size()==1);
 		Iterator iter = model.listStatements();
 		while ( iter.hasNext() ) {
 			Statement stm = (Statement) iter.next();
 			String sText = stm.getObject().toString();
 			assertTrue(stm.getPredicate().equals(ModelVocabulary.text));
 			assertTrue(sText.startsWith("The disgraced financier Bernard L. Madoff"));
 			assertTrue(sText.contains("June 16. "));
 		}
 	}
 
 	/** Test the tokenizer on an example XML JSTOR document. */
 	@Test
 	public void testTokenizer() {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ByteArrayOutputStream err = new ByteArrayOutputStream();
 
 		ctb.runZigZag(ctb.getZigZag("jstor-page-text-tokenizer.zz"),out,err);
 
 		String [] ta = out.toString().split(ComponentTesterBase.NEW_LINE);
 		assertEquals(323,ta.length);
 		assertEquals("back",ta[147]);
 		assertEquals("jail",ta[13]);
 		assertEquals("anger",ta[245]);
 		assertEquals("The",ta[0]);
 		assertEquals(".",ta[322]);
 	}
 
 
 	/** Test the sentence detection on an example XML JSTOR document. */
 	@Test
 	public void testSentenceDetection() {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ByteArrayOutputStream err = new ByteArrayOutputStream();
 
 		ctb.runZigZag(ctb.getZigZag("jstor-page-text-sentence-detector.zz"),out,err);
 
 		String [] ta = out.toString().split(ComponentTesterBase.NEW_LINE);
 		assertEquals(15,ta.length);
 		assertTrue(ta[0].startsWith("The disgraced financier"));
 		assertTrue(ta[13].startsWith("The 11 counts"));
 		assertTrue(ta[14].startsWith("Sentencing was"));
 	}
 }
