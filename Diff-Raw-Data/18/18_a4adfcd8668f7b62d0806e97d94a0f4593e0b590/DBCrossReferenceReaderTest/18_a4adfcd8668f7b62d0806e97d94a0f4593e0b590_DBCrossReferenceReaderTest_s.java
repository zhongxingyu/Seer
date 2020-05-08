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
 
 import cytoscape.data.ontology.DBReference;
 import cytoscape.data.ontology.readers.DBCrossReferenceReader;
 
 import junit.framework.TestCase;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import java.util.Map;
 
 
 /**
  * This class actually tests the following:
  *
  * DBReference DBCrossReferences DBCrossReferenceReader
  *
  * @author kono
  *
  */
 public class DBCrossReferenceReaderTest extends TestCase {
 	DBCrossReferenceReader rd;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		rd = new DBCrossReferenceReader();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		rd = null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 */
 	public void testGetXrefMap() {
 		try {
 			rd.readResourceFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Map<String, DBReference> sampleMap = rd.getXrefMap();
 
 		assertTrue(sampleMap.size() > 0);
 		assertTrue(sampleMap.containsKey("KEGG_PATHWAY"));
 		assertTrue(sampleMap.containsKey("AgBase"));
 		assertTrue(sampleMap.containsKey("ZFIN"));
 
 		DBReference ref = sampleMap.get("SGD");
 		assertEquals("Saccharomyces Genome Database", ref.getFullName());
 
 		try {
 			URL url = ref.getGenericURL();
 			assertEquals("http://www.yeastgenome.org/", url.toString());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		assertEquals("Identifier for SGD Loci", ref.getObject());
 
 		/*
 		 * The following lines of codes requirs network connection.
 		 */
 
 		//		try {
 		//			String result = connectToURL(ref.getQueryURL("S000006169"));
 		//			assertNotNull(result);
 		//
 		//			/*
 		//			 * Check this is a correct web page or not.
 		//			 */
 		//			assertTrue(result.contains("YPL248C"));
 		//			assertTrue(result.contains("GAL4"));
 		//			assertTrue(result.contains("DNA-binding transcription"));
 		//
 		//		} catch (SocketTimeoutException e) {
 		//			System.out
 		//					.print("======= Connection to SGD timeout.  Check connection or try again later ======");
 		//		} catch (MalformedURLException e) {
 		//			// TODO Auto-generated catch block
 		//			e.printStackTrace();
 		//		} catch (IOException e) {
 		//			// TODO Auto-generated catch block
 		//
 		//		}
 	}
 
 	private String connectToURL(URL url) throws IOException {
		URLConnection uc = url.openConnection();
 
 		/*
 		 * Set timeout.
 		 */
 		uc.setReadTimeout(5000);
 
 		InputStream is = uc.getInputStream();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		String s;
 		StringBuffer sb = new StringBuffer();
 
 		while ((s = reader.readLine()) != null) {
 			sb.append(s);
 		}
 
 		reader.close();
 
 		return sb.toString();
 	}
 }
