 /*
  * Copyright (c) 2001-2005, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp;
 
 import java.util.Map;
 import java.util.Properties;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 
 /**
  * A compiled LSP Page.
  *
  * <p>An LSP page instance may be reused and is thread safe.
  */
 public interface LSPPage
 {
     /**
      * Current version of LSP.
      */
     public static final String LSP_VERSION_NAME = "1.9";
     
     /**
      * Version number to check compatibility between runtime and compiled pages.
     * Will only be bumped with incompatibility is introduced (not nessecary 
      * with each release).
      */
     public static final int LSP_VERSION_NUM = 170;
     
 
     /**
 	 * Get a list of files which was imported with this page was compiled.
      * 
      * @return list of files which was imported with this page was compiled
 	 */
 	public String[] getCompileDependentFiles();
 
 	
     /**
 	 * Currently not used.
      * 
      * @return <code>true</code> if the page is compile dynamic 
 	 */
     public boolean isCompileDynamic();
 
     
 	/**
 	 * When this page was compiled.
      * 
 	 * @return when the page was compiled, in the same format as {@link java.lang.System#currentTimeMillis()} 
 	 */
 	public long getTimeCompiled();
 
 
     /**
 	 * Get the name of this page.
      * 
      * @return the name of this page. 
 	 */
 	public String getPageName();
 	
 
     /**
 	 * Output properties to use then this page is serialized.
      *
      * From &lt;lsp:output&gt;.
      * 
      * @return output properties to use then this page is serialized
 	 */
 	public Properties getOutputProperties();
     
 
     /**
      * Execute this LSP page and sends the output as SAX2 events to the
      * supplied {@link org.xml.sax.ContentHandler}. 
 	 * Does <em>not</em> output <code>startDocument()</code>/<code>endDocument()</code>
 	 * events.
 	 *
 	 * @param ch		  SAX2 {@link org.xml.sax.ContentHandler} to send output to
 	 * @param params      Parameters to the LSP page
 	 * @param extContext  external context which will be passed to ExtLibs
 	 *
 	 * @throws SAXException  may throw {@link org.xml.sax.SAXException}
      */
     public void execute(ContentHandler ch, Map<String,Object> params, Object extContext)
         throws SAXException;
 }
 
