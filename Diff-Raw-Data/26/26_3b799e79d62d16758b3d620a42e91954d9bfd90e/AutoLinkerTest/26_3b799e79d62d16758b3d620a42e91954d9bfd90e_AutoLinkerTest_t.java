 /**
  * Created by Michael Simons, michael-simons.eu
  * and released under The BSD License
  * http://www.opensource.org/licenses/bsd-license.php
  *
  * Copyright (c) 2010, Michael Simons
  * All rights reserved.
  *
  * Redistribution  and  use  in  source   and  binary  forms,  with  or   without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source   code must retain   the above copyright   notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary  form must reproduce  the above copyright  notice,
  *   this list of conditions  and the following  disclaimer in the  documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the name  of  michael-simons.eu   nor the names  of its contributors
  *   may be used  to endorse   or promote  products derived  from  this  software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
  * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
  * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
  * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
  * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.simons.tests.autolinker;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Entities.EscapeMode;
 import org.junit.Assert;
 import org.junit.Test;
 
 import ac.simons.autolinker.AutoLinker;
 import ac.simons.autolinker.LinkableEmailaddresses;
 import ac.simons.autolinker.LinkableTwitterUsers;
 import ac.simons.autolinker.LinkableUrls;
 
 /**
  * @author Michael J. Simons
  */
 public class AutoLinkerTest {
 	@Test
 	public void test() throws IOException {
 		Properties texts = new Properties();
 		texts.load(new InputStreamReader(AutoLinkerTest.class.getResourceAsStream("/testdata.properties"), Charset.forName("UTF-8")));
 	
 		final Set<String> keys = new HashSet<String>();
 		
 		for(Object _prop : texts.keySet()) {
 			String property = (String) _prop; 
 			keys.add(property.substring(0, property.indexOf(".")));
 		}
 		
 		
 		final AutoLinker autoLinker = new AutoLinker();
 		autoLinker.getLinkables().put(1, new LinkableEmailaddresses());
 		autoLinker.getLinkables().put(2, new LinkableTwitterUsers());
 		autoLinker.getLinkables().put(3, new LinkableUrls());
 		
		int cnt = 0;
 		for(String key : keys) {
 			final String in = String.format("%s.in", key.trim());
 			final String out = String.format("%s.out", key.trim());
 			Document document = autoLinker.autoLink(texts.getProperty(in));
 			document.outputSettings().prettyPrint(false).escapeMode(EscapeMode.xhtml);				
			Assert.assertEquals("Test " + key + " fails", texts.getProperty(out), document.body().html());			
			++cnt;
 		}
		Assert.assertTrue(cnt > 0);
 	}
 }
