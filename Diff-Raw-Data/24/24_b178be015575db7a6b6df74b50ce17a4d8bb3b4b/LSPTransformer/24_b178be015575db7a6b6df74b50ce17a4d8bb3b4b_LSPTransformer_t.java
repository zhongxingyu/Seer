 /*
  * Copyright (c) 2001, Mikael Stldal
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
 
 package nu.staldal.lagoon.producer;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 
 import nu.staldal.lsp.*;
 import nu.staldal.lagoon.core.*;
 import nu.staldal.lagoon.util.*;
 
 
 public class LSPTransformer extends Transform
 {
 	private static final boolean DEBUG = false;
 
     private LSPCompiler compiler;
     private LSPPage theCompiledPage;
     private Hashtable params;
 
     public void init()
         throws LagoonException, IOException
     {
         if (Wildcard.isWildcard(getSourceMan().getTargetPath()))
             throw new LagoonException("Cannot use with wildcard pattern: "
                 + getSourceMan().getTargetPath());
 
         compiler = new LSPCompiler();
 
         theCompiledPage = (LSPPage)getObjectFromRepository("page");
         if (DEBUG)
             if (theCompiledPage == null)
                 System.out.println("No compiled page found");
 
         params = new Hashtable();
        	for (Enumeration e = getParamNames(); e.hasMoreElements(); )
        	{
 			String paramName = (String)e.nextElement();
 		    params.put(paramName, getParam(paramName));
 		}
     }
 
 	private boolean sourceUpdated(long when)
         throws LagoonException, IOException
 	{
         if (theCompiledPage.isCompileDynamic()) return true;
 
         if (getNext().hasBeenUpdated(when))
         {
         	return true;
 		}
 
 		for (Enumeration e = theCompiledPage.getCompileDependentFiles();
              e.hasMoreElements(); )
 		{
 			if (getSourceMan().fileHasBeenUpdated((String)e.nextElement(),
                                                   when))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
     private void readSource(XMLStreamProducer next, Target target)
         throws SAXException, IOException
     {
 		if (DEBUG) System.out.println("LSP compile");
 
         ContentHandler ch = compiler.startCompile(
             new URLResolver() {
                 public InputSource resolve(String url) throws IOException
                 {
                     if (LagoonUtil.absoluteURL(url))
                     {
                         return new InputSource(url);
                     }
                     else
                     {
                        return new InputSource(
							getSourceMan().getFileURL(url).toExternalForm());
                     }
                 }
             });
         next.start(ch, target);
         theCompiledPage = compiler.finishCompile();
         putObjectIntoRepository("page", theCompiledPage);
     }
 
 
     public void start(ContentHandler ch, Target target)
         throws IOException, SAXException
     {
         if ((theCompiledPage == null)
                 || sourceUpdated(theCompiledPage.getTimeCompiled()))
         {
             readSource(getNext(), target);
 		}
 
         ch.startDocument();
 
         theCompiledPage.execute(ch, new URLResolver() {
             public InputSource resolve(String url) throws IOException
             {
                 if (url.length() == 0)
                 	throw new IOException("empty URL");
                 if (LagoonUtil.absoluteURL(url))
                 {
                     return new InputSource(url);
                 }
                 else
                 {
                     // *** search Sitemap
                     InputStream fis = getSourceMan().openFile(url);
                     return new InputSource(fis);
                 }
             }
         }, params);
 
         ch.endDocument();
     }
 
 
     public boolean hasBeenUpdated(long when)
         throws LagoonException, IOException
     {
         if ((theCompiledPage == null)
                 || sourceUpdated(theCompiledPage.getTimeCompiled()))
         {
             return true;
         }
 
 		if (theCompiledPage.isExecuteDynamic()) return true;
 
 		for (Enumeration e = theCompiledPage.getExecuteDependentFiles();
              e.hasMoreElements(); )
 		{
 			if (getSourceMan().fileHasBeenUpdated((String)e.nextElement(),
 				when))
 			{
 				return true;
 			}
 		}
 
         return false;
     }
 
 }
