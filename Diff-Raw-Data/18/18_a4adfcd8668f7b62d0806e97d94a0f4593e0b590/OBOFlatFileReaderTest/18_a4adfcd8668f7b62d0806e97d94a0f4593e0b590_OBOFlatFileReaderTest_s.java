 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.ontology.readers;
 
 import cytoscape.CyNetwork;
 
 import cytoscape.data.CyAttributes;
 
 import cytoscape.data.ontology.readers.OBOFlatFileReader;
 import cytoscape.data.ontology.readers.OBOTags;
 
 import junit.framework.TestCase;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.URL;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  *
  */
 public class OBOFlatFileReaderTest extends TestCase {
 	private static final String REMOTE_OBO = "http://www.geneontology.org/ontology/gene_ontology.obo";
 	private static final String LOCAL_OBO = "testData/annotation/goslim_generic.obo";
 	private static final int DAG_SIZE = 10738;
 	private InputStream is;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		File sampleOBO = new File(LOCAL_OBO);
 
 		URL oboUrl = sampleOBO.toURL();
		is = oboUrl.openStream();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		try {
 			if (is != null) {
 				is.close();
 			}
 		} catch (IOException ioe) {
 		} finally {
 			is = null;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 */
 	public void testReadOBO() {
 		long start = System.currentTimeMillis();
 		OBOFlatFileReader obor = new OBOFlatFileReader(is, null);
 
 		try {
 			obor.readOntology();
 			assertNotNull(obor.getHeader());
 
 			Map header = obor.getHeader();
 			Set<String> keys = obor.getHeader().keySet();
 
 			for (String key : keys) {
 				System.out.println("Key = " + key + ", Value = " + header.get(key));
 			}
 
 			assertTrue(keys.contains("format-version"));
 			assertTrue(keys.contains("date"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("## OBO Loading time = " + (System.currentTimeMillis() - start)
 		                   + " msec.");
 
 		start = System.currentTimeMillis();
 
 		CyAttributes goTermAttr = obor.getTermsAttributes();
 		String[] names = goTermAttr.getAttributeNames();
 
 		for (int i = 0; i < names.length; i++) {
 			System.out.println("Name = " + names[i]);
 		}
 
 		String curString = goTermAttr.getStringAttribute("GO:0000004", "ontology.name");
 		System.out.println("GO:0000004 = " + curString);
 		assertEquals(curString, "biological process unknown");
 
 		curString = goTermAttr.getStringAttribute("GO:0016049", "ontology.name");
 		System.out.println("GO:0016049 = " + curString);
 		assertEquals(curString, "cell growth");
 
 		Map synoMap = goTermAttr.getMapAttribute("GO:0016049",
 		                                         "ontology." + OBOTags.SYNONYM.toString());
 		assertNotNull(synoMap);
 		assertEquals(4, synoMap.size());
 
 		for (Object val : synoMap.keySet()) {
 			System.out.print("GO:0016049 Synonyms = " + val.toString());
 			System.out.println(" ( " + synoMap.get(val) + " )");
 		}
 
 		CyNetwork dag = obor.getDag();
 		assertNotNull(dag);
 
 		System.out.println("## OBO Test time = " + (System.currentTimeMillis() - start) + " msec.");
 	}
 }
