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
 import java.io.FilenameFilter;
 
 import org.junit.Test;
 
 public class JenkinsPath {
 
	public final static String WS_OPS_WP2RDF_TTLS = "/var/lib/jenkins/jobs/WikiPathways RDF/ws/OPSWPRDF/";
 
 	@Test
 	public void testLoadingRDF() {
 		File dir = new File(WS_OPS_WP2RDF_TTLS);
 		FilenameFilter filter = new FilenameFilter() {
 		    public boolean accept(File dir, String name) {
 		        return name.toLowerCase().endsWith(".ttl");
 		    }
 		};
 	
 		File[] files = dir.listFiles(filter);
 		for (File file : files) {
 			System.out.println(file);
 		}
 	}
 
 }
