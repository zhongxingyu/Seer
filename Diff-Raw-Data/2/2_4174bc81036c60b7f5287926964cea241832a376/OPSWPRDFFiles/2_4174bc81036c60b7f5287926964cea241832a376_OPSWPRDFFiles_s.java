 /* Copyright (C) 2013  Egon Willighagen <egon.willighagen@gmail.com>
  *
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *   - Neither the name of the <organization> nor the
  *     names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.unimaas.bigcat.wikipathways.curator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.hp.hpl.jena.n3.turtle.TurtleParseException;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 public class OPSWPRDFFiles {
 
	public final static String WS_OPS_WP2RDF_TTLS = "/home/egonw/var/Projects/GitHub/WikiPathwaysCurator/data/OPSWPRDF/";
 
 	private static Model loadedData = null;
 	private static boolean locked = false;
 	private static String parseErrors = "";
 	
 	public static Model loadData() throws InterruptedException {
 		if (loadedData != null) return loadedData;
 
 		while (locked) Thread.sleep(1000);
 
 		if (loadedData != null) return loadedData;
 
 		locked = true;
 
 		File dir = new File(WS_OPS_WP2RDF_TTLS);
 		FilenameFilter filter = new FilenameFilter() {
 		    public boolean accept(File dir, String name) {
 		        return name.toLowerCase().endsWith(".ttl");
 		    }
 		};
 	
 		File[] files = dir.listFiles(filter);
 		StringBuffer parseFailReport = new StringBuffer();
 		String directory = "target/UnitTest" ;
 		File tbdFolder = new File(directory);
 		tbdFolder.mkdir();
 		loadedData = ModelFactory.createDefaultModel();
 		for (File file : files) {
 			try {
 				loadedData.read(new FileReader(file), "", "TURTLE");
 			} catch (FileNotFoundException exception) {
 				parseFailReport.append(file.getName())
 			    .append(": not found\n");
 			} catch (TurtleParseException exception) {
 				parseFailReport.append(file.getName())
 				    .append(": ").append(exception.getMessage())
 				    .append('\n');
 			}
 		}
 		locked = false;
 		parseErrors = parseFailReport.toString();
 		return loadedData;
 	}
 	
 	@Test
 	public void testLoadingRDF() throws InterruptedException {
 		loadData();
 		if (parseErrors.length() > 0) Assert.fail(parseErrors.toString());
 	}
 
 }
