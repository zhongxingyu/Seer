 /*
  * Copyright (c) 2001-2002, Mikael Stldal
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
 
 import javax.xml.transform.*;
 import javax.xml.transform.Source;
 import javax.xml.transform.sax.*;
 import javax.xml.transform.stream.StreamSource;
 import org.xml.sax.*;
 
 import nu.staldal.lagoon.core.*;
 import nu.staldal.xmlutil.*;
 
 
 public class XSLTransformer extends Transform
 {
 	private static final boolean DEBUG = false;
 
     private String xslFile;
     private SAXTransformerFactory tfactory;
     private boolean always;
    private long stylesheetRead = 0;
 
     private StylesheetContainer container;
 
     public void init() throws LagoonException, IOException
     {
         xslFile = getParam("stylesheet");
     	if (xslFile == null)
     	{
 			throw new LagoonException("stylesheet parameter not specified");
 		}
 
         String a = getParam("always");
         always = (a != null) && (a.length() > 0);
 
         try
         {
 			TransformerFactory tf = TransformerFactory.newInstance();
             if (!(tf.getFeature(SAXTransformerFactory.FEATURE)
                     && tf.getFeature(SAXSource.FEATURE)
                     && tf.getFeature(SAXResult.FEATURE)
                     && tf.getFeature(StreamSource.FEATURE)))
             {
                 throw new LagoonException("The transformer factory "
                     + tf.getClass().getName() + " doesn't support SAX");
             }
             tfactory = (SAXTransformerFactory)tf;
 
             container = (StylesheetContainer)getObjectFromRepository(
                 "stylesheet");
 		}
         catch (LagoonException e)
 		{
 			throw e;
 		}
 		catch (SAXException e)
         {
             throw new LagoonException(e.getMessage());
         }
     }
     
    public void afterBuild()
        throws IOException
    {
        if (stylesheetRead > 0)
        {
            container.stylesheetRead = System.currentTimeMillis();
            putObjectIntoRepository("stylesheet", container);
        }
    }

     private void readStylesheet(final Target target)
     	throws IOException, SAXException
     {
         container = new StylesheetContainer(always);
 
         final String xslPath = getSourceMan().getFileURL(xslFile);
 			
 		if (!getContext().canCheckFileHasBeenUpdated(xslPath))
 			container.compileDynamic = true;
 
         if (DEBUG) System.out.println("Read stylesheet: " + xslPath);
 
         tfactory.setURIResolver(new URIResolver() {
             public Source resolve(String href, String base)
             {
 				String thisFile = 
 					getContext().getFileURLRelativeTo(href, xslPath);
 
 				try {
 	                if (!getContext().canCheckFileHasBeenUpdated(thisFile))
 	                {
 	                    container.compileDynamic = true;
 	                }
 					else
 					{
 	                    container.importedFiles.put(thisFile, "");
 					}
 				
 					return getSourceMan().getFileAsJAXPSource(thisFile, target);
 				}
 				catch (FileNotFoundException e)
 				{
 					return null; // let XSLT processor discover error
 				}
             }
         });
 
 		Source ss = getSourceMan().getFileAsJAXPSource(xslPath, target);
 		
         try {
             container.stylesheet = tfactory.newTemplates(ss);
            stylesheetRead = System.currentTimeMillis();
             putObjectIntoRepository("stylesheet", container);
         }
         catch (TransformerConfigurationException e)
         {
             throw new SAXException(e);
         }
 
         if (DEBUG)
         {
             System.out.println("---depends on files:");
             for (Enumeration e = container.importedFiles.keys();
                 e.hasMoreElements(); )
             {
                 System.out.println("\t" + e.nextElement());
             }
             System.out.println("---");
         }
     }
 
 	private boolean stylesheetUpdated()
         throws LagoonException, IOException
 	{
         if (container == null) return true;
     
         if (container.compileDynamic) return true;
 
         if (getSourceMan().fileHasBeenUpdated(xslFile,
                                               container.stylesheetRead))
         {
         	return true;
 		}
 
 		for (Enumeration e = container.importedFiles.keys();
              e.hasMoreElements(); )
 		{
 			if (getSourceMan().fileHasBeenUpdated((String)e.nextElement(),
                                                   container.stylesheetRead))
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
     public void start(org.xml.sax.ContentHandler sax, final Target target)
     	throws IOException, SAXException
     {
         if (stylesheetUpdated())
         {
             readStylesheet(target);
         }
 
         if (DEBUG) System.out.println("Transforming");
 
         TransformerHandler th;
         try {
             th = tfactory.newTransformerHandler(container.stylesheet);
         }
         catch (TransformerConfigurationException e)
         {
             throw new SAXException(e);
         }
         th.setResult(new SAXResult(sax));
 
        	for (Enumeration e = getParamNames(); e.hasMoreElements(); )
        	{
 			String paramName = (String)e.nextElement();
 			if (!paramName.equals("stylesheet")
                     && !paramName.equals("always"))
 			{
 				th.getTransformer().setParameter(paramName,
                                                  getParam(paramName));
 			}
 		}
 
         th.getTransformer().setURIResolver(new URIResolver() {
             public Source resolve(String href, String base)
             {
 				try {
 					if (!getContext().canCheckFileHasBeenUpdated(href))
 					{
 						container.executeDynamic = true;
 					}
 					else
 					{
 						container.readFiles.put(
 							getSourceMan().getFileURL(href), "");
 					}
 					
 					return getSourceMan().getFileAsJAXPSource(href, target);
 					}
 				catch (FileNotFoundException e)
 				{
 					return null; // let XSLT processor discover error
 				}
             } 
         });
 
         getNext().start(new ContentHandlerFixer(th), target);
 
         putObjectIntoRepository("stylesheet", container);
 
         if (DEBUG)
         {
             System.out.println("---execute depends on files:");
             for (Enumeration e = container.readFiles.keys();
                  e.hasMoreElements(); )
             {
                 System.out.println("\t" + e.nextElement());
             }
             System.out.println("---");
         }
     }
 
     public boolean hasBeenUpdated(long when)
         throws LagoonException, IOException
     {
         if (stylesheetUpdated())
         {
             return true;
         }
 
 		if (container.executeDynamic) return true;
 
 		for (Enumeration e = container.readFiles.keys();
              e.hasMoreElements(); )
 		{
 			String f = (String)e.nextElement();
 			if (getSourceMan().fileHasBeenUpdated(f, when))
 			{
 				return true;
 			}
 		}
         
         return getNext().hasBeenUpdated(when);
     }
 }
 
 
 class StylesheetContainer implements Serializable
 {
     Templates stylesheet;
     long stylesheetRead;
     boolean executeDynamic;
     boolean compileDynamic;
     Hashtable importedFiles;
     Hashtable readFiles;
 
     StylesheetContainer(boolean always)
     {
         executeDynamic = always;
         compileDynamic = false;
 
         importedFiles = new Hashtable();
         readFiles = new Hashtable();
     }
 }
