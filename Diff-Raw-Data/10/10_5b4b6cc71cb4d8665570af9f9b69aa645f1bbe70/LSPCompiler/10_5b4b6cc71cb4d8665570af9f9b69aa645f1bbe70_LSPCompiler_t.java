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
 
 package nu.staldal.lsp.compiler;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 
 import nu.staldal.xtree.*;
 import nu.staldal.syntax.ParseException;
 
 import nu.staldal.lsp.*;
 import nu.staldal.lsp.compile.*;
 import nu.staldal.lsp.expr.*;
 import nu.staldal.lsp.compiledexpr.*;
 
 
 /**
  * Compiles an LSP page into JVM bytecode.
  *
  * <p>An instance of this class may be reused, but is not thread safe.
  *
  * @see LSPCompilerHelper
  */
 public class LSPCompiler
 {
 	private static final boolean DEBUG = false;
 
     private static final String LSP_CORE_NS = "http://staldal.nu/LSP/core";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
     private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
     
     private TreeBuilder tb;
 	private String pageName;
     private URLResolver resolver;
 	private LSPJVMCompiler jvmCompiler;
 
     private HashMap importedFiles;
     private boolean compileDynamic;
 
 	// (String)namespaceURI -> (String)className
 	private HashMap extLibsInPage; 
 
 	private boolean inPi;
 	private boolean inExtElement;
 	
 	private Element currentSourceElement;
 	private LSPElement currentElement;
 	
 	// (String)namespaceURI -> (String)className
 	private HashMap extDict = new HashMap();
     
     private Properties outputProperties;
 
     private boolean xhtml;
    private boolean acceptNull;
    
 
     /**
 	 * Create a new LSP compiler. The instance may be reused, 
 	 * but may <em>not</em> be used from several threads concurrently.
 	 * Create several instances if multiple threads needs to compile 
 	 * concurrently.
 	 */
 	public LSPCompiler()
     {
         tb = null;
 		pageName = null;
         resolver = null;
 		jvmCompiler = new LSPJVMCompiler();
         xhtml = false;
        acceptNull = false;
     }
     
     
     /**
      * Set to <code>true</code> to use <code>xhtml</code> as default output 
      * type.
      */
     public void setXhtml(boolean xhtml)
     {
         this.xhtml = xhtml;    
     }
 
     
     /**
 	 * Set to <code>true</code> to make the compiled page accept 
      * <code>null</code> values without runtime error.
      */    
     public void setAcceptNull(boolean acceptNull)
     {
         this.acceptNull = acceptNull;    
     }
     
 
     /**
 	 * Start compilation of an LSP page.
 	 *
 	 * @param page       page name
 	 * @param r          {@link URLResolver} to use for resolving <code>&lt;lsp:import&gt;</code>
 	 *
 	 * @return SAX2 ContentHandler to feed the LSP source into
 	 */
 	public ContentHandler startCompile(String page, URLResolver r)
     {
     	importedFiles = new HashMap();
         compileDynamic = false;
 		extLibsInPage = new HashMap();
 
 		pageName = page;
         resolver = r;
         tb = new TreeBuilder();
        
         return tb;
     }
 
 
     /**
 	 * Finish the compilation.
 	 * 
 	 * @param out  where to write the compiled code (as JVM bytecode).
 	 *
 	 * @throws SAXException if any compilation error occurs
 	 * @throws IOException if any I/O error occurs when reading 
 	 *         &lt;lsp:import&gt;ed files, or when writing compiled code
 	 */
 	public void finishCompile(OutputStream out)
     	throws SAXException, IOException
     {
         if (tb == null) throw new IllegalStateException(
             "startCompile() must be invoked before finishCompile()");
 
         Element tree = tb.getTree();
         
         // temporary lower current thread priority
         int currentPriority = Thread.currentThread().getPriority();        
         if (currentPriority > 3)
         {
             Thread.currentThread().setPriority(currentPriority-2);
         }
 
 		long startTime = System.currentTimeMillis();
 		if (DEBUG) System.out.println("LSP Compile...");
 
         processImports(tree);
 		
 		for (int i = 0; i<tree.numberOfNamespaceMappings(); i++)
 		{
 			String namespace = tree.getNamespaceMapping(i)[1];
 			
 			if (DEBUG) System.out.println("Namespace URI: " + namespace); 
 			
 			lookupExtensionHandler(tree, namespace);
 		}
 		
 		inExtElement = false;
         inPi = false;
 		currentElement = null;
 		currentSourceElement = null;
         outputProperties = null;
 
         LSPNode compiledTree = compileNode(tree);
         
         if (outputProperties == null)
             outputProperties = new Properties();
         
         if (!outputProperties.containsKey("method"))
         {
             String method;
             
             if (tree.getLocalName().equalsIgnoreCase("html")
                         && tree.getNamespaceURI().length() == 0)
             {
                 method = "html";
             }
             else if (tree.getLocalName().equals("html")
                         && tree.getNamespaceURI().equals(XHTML_NS))
             {
                 method = xhtml ? "xhtml" : "html";
             }
             else
             {
                 method = "xml";
             }            
             
             outputProperties.setProperty("method", method);
         }
         
 		jvmCompiler.compileToByteCode(pageName, compiledTree, 
 			importedFiles, compileDynamic,
 			extLibsInPage, outputProperties, out, acceptNull);
 
 		outputProperties = null;
         tb = null;
 		pageName = null;
         resolver = null;        
 
 		long timeElapsed = System.currentTimeMillis()-startTime;
 		if (DEBUG) System.out.println("in " + timeElapsed + " ms");
 
         // restore current thread priority
         Thread.currentThread().setPriority(currentPriority);
     }
 	
 	
 	static SAXException fixSourceException(Node node, String msg)
 	{
 		return new SAXParseException(msg, null,
 			node.getSystemId(), node.getLineNumber(), node.getColumnNumber());
 	}
 
 
 	private static SAXException fixParseException(Node node,
 		String expression, ParseException e)
 	{
 		return new SAXParseException(
 			"Illegal LSP expression:\n"
 				+ expression + "\n" + Utils.nChars(e.getColumn()-1,' ')
 				+ "^ "+ e.getMessage(),
 			null,
 			node.getSystemId(),
 			node.getLineNumber(),
 			node.getColumnNumber());
 	}
 
 
     private static SAXException fixIllegalTemplate(Node node, String template)
     {
         String msg = (template.length() > 50)
             ? "Illegal LSP template"
 			: ("Illegal LSP template: " + template);
 
 		return new SAXParseException(msg, null,
 			node.getSystemId(), node.getLineNumber(), node.getColumnNumber());
     }
 
 
 	private String getAttr(String name, Element el, boolean throwOnError)
 		throws SAXException
 	{
 		String value = el.getAttributeValue(el.lookupAttribute("", name));
 
 		if ((value == null) || (value.length() < 1))
 		{
 			if (throwOnError)
 			{
 				throw fixSourceException(el,
 					"lsp:" + el.getLocalName() + " element must have a "
 					+ name + " attribute");
 			}
 			else
 			{
 				return null;
 			}
 		}
 		return value;
 	}
 
 
 	private String getAttrWithEmpty(String name, Element el, boolean throwOnError)
 		throws SAXException
 	{
 		String value = el.getAttributeValue(el.lookupAttribute("", name));
 
 		if (value == null)
 		{
 			if (throwOnError)
 			{
 				throw fixSourceException(el,
 					"lsp:" + el.getLocalName() + " element must have a "
 					+ name + " attribute");
 			}
 			else
 			{
 				return null;
 			}
 		}
 		return value;
 	}
 
 
 	private static ArrayList processTemplate(Node n,
         char left, char right, char quot1, char quot2,
         String template)
         throws SAXException
 	{
 		ArrayList vector = new ArrayList(template.length()/16);
 		StringBuffer text = new StringBuffer();
 		StringBuffer expr = null;
 		char quote = 0;
 		char brace = 0;
 
 		for (int i = 0; i < template.length(); i++)
 		{
 			char c = template.charAt(i);
 			if (expr == null)
 			{
 				if (c == left)
 				{
 					if (brace == 0)
 					{
 						brace = left;
 					}
 					else if (brace == left)
 					{
 						text.append(left);
 						brace = 0;
 					}
 					else if (brace == right)
 					{
 						throw fixIllegalTemplate(n, template);
 					}
 				}
 				else if (c == right)
 				{
 					if (brace == 0)
 					{
 						brace = right;
 					}
 					else if (brace == right)
 					{
 						text.append(right);
 						brace = 0;
 					}
 					else if (brace == left)
 					{
 						throw fixIllegalTemplate(n, template);
 					}
 				}
 				else
 				{
 					if (brace == left)
 					{
 						if (text.length() > 0)
 							vector.add(text.toString());
 						text = null;
 
 						expr = new StringBuffer();
 						expr.append(c);
 						brace = 0;
 					}
 					else if (brace == right)
 					{
 						throw fixIllegalTemplate(n, template);
 					}
 					else
 					{
 						text.append(c);
 					}
 				}
 			}
 			else // expr != null
 			{
 				if (c == quot1 || c == quot2)
 				{
 					expr.append(c);
 					if (quote == 0)
 					{
 						quote = c;
 					}
 					else if (quote == c)
 					{
 						quote = 0;
 					}
 				}
 				else if (c == right)
 				{
 					if (quote == 0)
 					{
                         String exp = expr.toString();
                         LSPExpr res;
                         try {
                             res = LSPExpr.parseFromString(exp);
                         }
                         catch (ParseException e)
                         {
                             throw fixParseException(
 								n, exp, (ParseException)e);
                         }
                         vector.add(res);
 						expr = null;
 						text = new StringBuffer();
 					}
 					else
 					{
 						expr.append(c);
 					}
 				}
 				else
 				{
 					expr.append(c);
 				}
 			}
 		}
 
 		if (brace != 0)
 		{
 		    throw fixIllegalTemplate(n, template);
 		}
 
 		if ((text != null) && (text.length() > 0))
 			vector.add(text.toString());
 		text = null;
 
 		return vector;
 	}
 
 
     private static boolean containsSystemId(Node node, String systemId)
     {
         if (systemId.equals(node.getSystemId())) 
             return true;
         else if (node.getParent() != null)
             return containsSystemId(node.getParent(), systemId);
         else
             return false;
     }
     
     
     private void processImports(Element el)
     	throws SAXException, IOException
     {
 		for (int i = 0; i < el.numberOfChildren(); i++)
 		{
 			if (!(el.getChild(i) instanceof Element)) continue;
 
 			Element child = (Element)el.getChild(i);
 
 			if (child.getNamespaceURI().equals(LSP_CORE_NS)
 					&& child.getLocalName().equals("import"))
 			{
 				String url = getAttr("file", child, true);
                 boolean duplicate = false;
 				if (importedFiles.put(url, url) != null)
                     duplicate = true;
 
 				TreeBuilder tb = new TreeBuilder();
 				resolver.resolve(url, tb);
 				Element importedDoc = tb.getTree();
                 importedFiles.put(url, importedDoc.getSystemId());
                 
                 if (duplicate)
                 {
                     if (containsSystemId(child, importedDoc.getSystemId()))
                         throw fixSourceException(child, "circular import");
                 }
 
 				el.replaceChild(importedDoc, i);
 				processImports(importedDoc);
 			}
 			else
 			{
 				processImports(child);
 			}
 		}
 	}
 
 
     private LSPNode compileNode(Node node) throws SAXException
     {
         if (node instanceof Element)
             return compileNode((Element)node);
         else if (node instanceof Text)
             return compileNode((Text)node);
         else if (node instanceof ProcessingInstruction)
             return compileNode((ProcessingInstruction)node);
         else
         	throw new LSPException("Unrecognized XTree Node: "
         		+ node.getClass().getName());
     }
 
 
     private LSPNode compileNode(Element el) throws SAXException
     {
 		if (el.getNamespaceURI().equals(LSP_CORE_NS))
 		{
 			// Dispatch LSP command
 			if (el.getLocalName().equals("output"))
 			{
 				return process_output(el);
 			}
 			else if (el.getLocalName().equals("root"))
 			{
 				return process_root(el);
 			}
 			else if (el.getLocalName().equals("processing-instruction"))
 			{
 				return process_processing_instruction(el);
 			}
 			else if (el.getLocalName().equals("if"))
 			{
 				return process_if(el);
 			}
 			else if (el.getLocalName().equals("value-of"))
 			{
 				return process_value_of(el);
 			}
 			else if (el.getLocalName().equals("choose"))
 			{
 				return process_choose(el);
 			}
 			else if (el.getLocalName().equals("when"))
 			{
 				throw fixSourceException(el,
 					"<lsp:when> must occur inside <lsp:choose>");
 			}
 			else if (el.getLocalName().equals("otherwise"))
 			{
 				throw fixSourceException(el,
 					"<lsp:otherwise> must occur inside <lsp:choose>");
 			}
 			else if (el.getLocalName().equals("for-each"))
 			{
 				return process_for_each(el);
 			}
 			else if (el.getLocalName().equals("let"))
 			{
 				return process_let(el);
 			}
 			else if (el.getLocalName().equals("element"))
 			{
 				return process_element(el);
 			}
 			else if (el.getLocalName().equals("attribute"))
 			{
 				return process_attribute(el);
 			}
 			else
 			{
 				throw fixSourceException(el,
 					"unrecognized LSP command: " + el.getLocalName());
 			}
 		}
 		else
 		{
 			LSPElement newEl;
 			boolean inExtElementNow = false; 			
 			
 			String extClass = lookupExtensionHandler(el, el.getNamespaceURI());
 			if (!inExtElement && (extClass != null))
 			{
 				inExtElement = true;
 				inExtElementNow = true;
 				newEl = new LSPExtElement(extClass, 
 					el.getNamespaceURI(), el.getLocalName(),
 					el.numberOfAttributes(), el.numberOfChildren(), el); 
 			}
 			else
 			{
 				newEl = new LSPElement(el.getNamespaceURI(), el.getLocalName(),
 					el.numberOfAttributes(), el.numberOfChildren(), el);
 			}
 
 			for (int i = 0; i < el.numberOfNamespaceMappings(); i++)
 			{
 				String[] m = el.getNamespaceMapping(i);
 				if (m[1].equals(LSP_CORE_NS)) continue;
 				
 				if (lookupExtensionHandlerClass(el, m[1]) != null)
 					continue;
 												
 				newEl.addNamespaceMapping(m[0], m[1]);				
 			}
 
 			for (int i = 0; i < el.numberOfAttributes(); i++)
 			{
 				String URI = el.getAttributeNamespaceURI(i);
 				String local = el.getAttributeLocalName(i);
 				String type = el.getAttributeType(i);
 				String value = el.getAttributeValue(i);
 
 				LSPExpr newValue = processTemplateExpr(el, value);
 
 				newEl.addAttribute(new StringLiteral(URI), 
 								   new StringLiteral(local),
 								   type, newValue);
 			}
 
 			currentElement = newEl;
 			currentSourceElement = el;
 			compileChildren(el, newEl);
 			
 			if (inExtElementNow) inExtElement = false;
 
 			return newEl;
 		}
     }
 
 	
 	private String lookupExtensionHandler(Node el, String ns)
 		throws SAXException
 	{
 		if (ns == null || ns.length() == 0) 
 			return null;
 
 		String className = lookupExtensionHandlerClass(el, ns);
 		
 		if (className == null) return null;
 			
 		extLibsInPage.put(ns, className);
 		return className;
 	}
 	
     
 	private String lookupExtensionHandlerClass(Node el, String ns)
 		throws SAXException
 	{	
 	    String className = (String)extDict.get(ns);
 		
 		if (className == null)
         try
         {
 			String fileName = "/nu/staldal/lsp/extlib/" 
 				+ Utils.encodePath(ns);
 			InputStream is = getClass().getResourceAsStream(fileName);
 			if (is == null) return null;
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			className = br.readLine();
 			br.close();
 			if (className == null)
 				throw fixSourceException(el,
 					"Illegal LSP Extension config file: " + fileName);
 
 			Class cls = Class.forName(className);
 			if (!nu.staldal.lsp.LSPExtLib.class.isAssignableFrom(cls))
 			throw fixSourceException(el, 
 				"LSP Extension class " + className 
 				+ " must implement nu.staldal.lsp.LSPExtLib");
 				
 			extDict.put(ns, className);
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw fixSourceException(el, 
 				"extension class " + className + " not found"); 
 		}
         catch (IOException e)
         {
             throw fixSourceException(el,
                 "Unable to read LSP Extension config file: " + e.toString());
         }
 		
 		return className;
 	}	
 	
 
     private LSPNode compileNode(Text text)
         throws SAXException
     {
         return new LSPText(text.getValue(), text);
     }
 
 
     private LSPNode compileNode(ProcessingInstruction pi)
         throws SAXException
     {
         return new LSPProcessingInstruction(
 			new StringLiteral(pi.getTarget()), 
 			new LSPText(pi.getValue(), pi), pi);
     }
 
 
     private LSPExpr processTemplateExpr(Node n, String template)
         throws SAXException
     {
 		ArrayList vec = processTemplate(n, '{', '}', '\'', '\"', template);
 
 		BuiltInFunctionCall expr = new BuiltInFunctionCall("concat", vec.size());
 
 		for (Iterator e = vec.iterator(); e.hasNext(); )
 		{
 			Object o = e.next();
 			if (o instanceof String)
 			{
 				expr.addArgument(new StringLiteral((String)o));
 			}
 			else if (o instanceof LSPExpr)
 			{
 				expr.addArgument(compileExpr(n, (LSPExpr)o));
 			}
 		}
 
 		if (expr.numberOfArgs() == 0)
 			return new StringLiteral("");
 		else if (expr.numberOfArgs() == 1)
 			return expr.getArg(0);
 		else
 			return expr;
     }
 
 
 	private void compileChildren(Element el, LSPContainer container)
 		throws SAXException
 	{
 		for (int i = 0; i < el.numberOfChildren(); i++)
 		{
 			Node child = el.getChild(i);
             
             if (child.isWhitespaceNode()
                     && (!child.getPreserveSpace())
                     && (i+1 < el.numberOfChildren())
                     && el.getChild(i+1) instanceof Element)
             {
                 Element child2 = (Element)el.getChild(i+1);
 
                 if (child2.getNamespaceURI().equals(LSP_CORE_NS))
                 {
                     if (child2.getLocalName().equals("attribute")
                             || child2.getLocalName().equals("output"))
                     {               
                         continue; // strip whitespace                        
                     }
                     
                     if (child2.getLocalName().equals("if")
                             && (child2.numberOfChildren() >= 1) 
                             && (child2.getChild(0) instanceof Element))
                     {
                         Element child3 = (Element)child2.getChild(0);
 
                         if (child3.getNamespaceURI().equals(LSP_CORE_NS)
                                 && child3.getLocalName().equals("attribute"))
                         {
                             continue; // strip whitespace                        
                         }                                                
                     }
                     else if (child2.getLocalName().equals("if")
                             && (child2.numberOfChildren() >= 2) 
                             && (child2.getChild(0).isWhitespaceNode())
                             && (child2.getChild(1) instanceof Element))
                     {
                         Element child3 = (Element)child2.getChild(1);
 
                         if (child3.getNamespaceURI().equals(LSP_CORE_NS)
                                 && child3.getLocalName().equals("attribute"))
                         {
                             continue; // strip whitespace                        
                         }                                                
                     }
                 }
             }
 
             if (child.isWhitespaceNode()
                     && (!child.getPreserveSpace())
                     && (i > 0)
                     && (i == el.numberOfChildren()-1) // last child
                     && el.getChild(i-1) instanceof Element)
             {
                 Element child2 = (Element)el.getChild(i-1);
 
                 if (child2.getNamespaceURI().equals(LSP_CORE_NS))
                 {
                     if (child2.getLocalName().equals("attribute")
                             || child2.getLocalName().equals("output"))
                     {               
                         continue; // strip whitespace                        
                     }
                     
                     if (child2.getLocalName().equals("if")
                             && (child2.numberOfChildren() >= 1) 
                             && (child2.getChild(0) instanceof Element))
                     {
                         Element child3 = (Element)child2.getChild(0);
 
                         if (child3.getNamespaceURI().equals(LSP_CORE_NS)
                                 && child3.getLocalName().equals("attribute"))
                         {
                             continue; // strip whitespace                        
                         }                                                
                     }
                     else if (child2.getLocalName().equals("if")
                             && (child2.numberOfChildren() >= 2) 
                             && (child2.getChild(0).isWhitespaceNode())
                             && (child2.getChild(1) instanceof Element))
                     {
                         Element child3 = (Element)child2.getChild(1);
 
                         if (child3.getNamespaceURI().equals(LSP_CORE_NS)
                                 && child3.getLocalName().equals("attribute"))
                         {
                             continue; // strip whitespace                        
                         }                                                
                     }
                 }
             }            
             
             LSPNode compiledNode = compileNode(child);
             container.addChild(compiledNode);
 		}
 	}
 
 
 	private LSPNode compileChildren(Element el)
 		throws SAXException
 	{
 		if (el.numberOfChildren() == 1)
 			return compileNode(el.getChild(0)); // optimization
 		else
 		{
 			LSPContainer container = new LSPSimpleContainer(el.numberOfChildren(), el);
 			compileChildren(el, container);
 			return container;
 		}
 	}
 
     
     private void removeWhitespace(Element el)
 		throws SAXException
     {
         if (el.numberOfChildren() == 0)
             return;        
         Node firstNode = el.getChild(0);
         if (firstNode.isWhitespaceNode() && !firstNode.getPreserveSpace())
             el.removeChild(0);
         
         if (el.numberOfChildren() == 0)
             return;        
         Node lastNode = el.getChild(el.numberOfChildren()-1);
         if (lastNode.isWhitespaceNode() && !lastNode.getPreserveSpace())
             el.removeChild(el.numberOfChildren()-1);                
     }    
     
 
 	private LSPNode process_output(Element el)
 		throws SAXException
 	{
 		if (outputProperties != null)
         throw fixSourceException(el,
 			"only one <lsp:output> element allowed");
             
         outputProperties = new Properties();
         for (int i = 0; i <el.numberOfAttributes(); i++)
         {
             outputProperties.setProperty(
                 el.getAttributeLocalName(i),
                 el.getAttributeValue(i));
         }        
         
         return new LSPSimpleContainer(0, null); // return empty container        
 	}
 
     
 	private LSPNode process_root(Element el)
 		throws SAXException
 	{
 		return compileChildren(el);
 	}
 
 
 	private LSPNode process_value_of(Element el)
 		throws SAXException
 	{
 		String exp = getAttr("select", el, true);
         
         String _disableOutputEscaping = 
             getAttr("disable-output-escaping", el, false);
         boolean disableOutputEscaping =
             "yes".equals(_disableOutputEscaping)
                 || "disable-output-escaping".equals(_disableOutputEscaping);
                 
         if (inPi && disableOutputEscaping) throw fixSourceException(el,
 			"disable-output-escaping may not be used in processing instruction");
                 
 		try {
 			LSPExpr select = LSPExpr.parseFromString(exp);
 
 			return new LSPTemplate(compileExpr(el, select), el,
                 disableOutputEscaping);
 		}
 		catch (ParseException e)
 		{
 			throw fixParseException(el, exp, e);
 		}
 	}
 
 
 	private LSPNode process_processing_instruction(Element el)
 		throws SAXException
 	{
 		if (inPi) throw fixSourceException(el,
 			"<lsp:processing-instruction> may not be nested");
 
 		LSPExpr name = processTemplateExpr(el, getAttr("name", el, true));
 
 		inPi = true;
 		LSPNode data = compileChildren(el);
 		inPi = false;
 
 		return new LSPProcessingInstruction(name, data, el);
 	}
 
 
 	private LSPNode process_element(Element el)
 		throws SAXException
 	{
 		if (inPi) throw fixSourceException(el,
 			"<lsp:element> may not be nested in <lsp:processing-instruction>");
 
 		LSPExpr name = processTemplateExpr(el, getAttr("name", el, true));
 		String nsAttr = getAttrWithEmpty("namespace", el, false);
 		LSPExpr ns = (nsAttr != null) 
 			? processTemplateExpr(el, nsAttr)
 			: new StringLiteral(el.lookupNamespaceURI(""));
 
 		LSPElement newEl = new LSPElement(ns, name, -1, el.numberOfChildren(), el);
 
 		currentElement = newEl;
 		currentSourceElement = el;
 		compileChildren(el, newEl);
 
 		return newEl;
 	}
 
 	
 	private LSPNode process_attribute(Element el)
 		throws SAXException
 	{
         return process_attribute(el, null);
     }
     
     
     private LSPNode process_attribute(Element el, LSPExpr test)
 		throws SAXException
 	{
 		if (inPi) throw fixSourceException(el,
 			"<lsp:attribute> may not be nested in <lsp:processing-instruction>");
 		if (currentElement == null) throw fixSourceException(el,
 			"<lsp:attribute> must be inside element");
 		if ((test == null) && (el.getParent() != currentSourceElement))
             throw fixSourceException(el,
 			    "<lsp:attribute> must be directly inside element");
 		
 		if (el.numberOfChildren() > 0) throw fixSourceException(el,
 			"<lsp:attribute> must be empty");
 		
 		LSPExpr name = processTemplateExpr(el, getAttr("name", el, true));
         
         if (test != null)
         {
             name = new ConditionalExpr(test, name, new StringLiteral(""));
         }
         
 		String nsAttr = getAttrWithEmpty("namespace", el, false);
 		LSPExpr ns = (nsAttr != null) 
 			? processTemplateExpr(el, nsAttr)
 			: new StringLiteral("");
 		
 		String valueAttr = getAttrWithEmpty("value", el, true);
 		LSPExpr value = processTemplateExpr(el, valueAttr);
 
 		currentElement.addAttribute(ns, name, "CDATA", value);		
 		
 		return new LSPSimpleContainer(0, null); // return empty container
 	}
 
 	
 	private LSPNode process_if(Element el)
 		throws SAXException
 	{
         removeWhitespace(el);
 		
         String exp = getAttr("test", el, true);
 		try {                        
 			LSPExpr test = compileExpr(el, LSPExpr.parseFromString(exp));            
             
             if (el.numberOfChildren() == 1 
                     && el.getChild(0) instanceof Element)
             {
                 Element theChild = (Element)el.getChild(0);
 
                 if (theChild.getNamespaceURI().equals(LSP_CORE_NS)
                         && theChild.getLocalName().equals("attribute"))
                 {
                     return process_attribute(theChild, test);
                 }
             }
 
 			return new LSPIf(test, compileChildren(el), el);
 		}
 		catch (ParseException e)
 		{
 			throw fixParseException(el, exp, e);
 		}
 	}
 
 
 	private LSPNode process_choose(Element el)
 		throws SAXException
 	{
 		LSPChoose choose = new LSPChoose(el.numberOfChildren(), el);
 		for (int i = 0; i < el.numberOfChildren(); i++)
 		{
 			Node _child = el.getChild(i);
 			if (_child instanceof Element)
 			{
 				Element child = (Element)_child;
 
 				if (child.getNamespaceURI().equals(LSP_CORE_NS)
 						&& child.getLocalName().equals("when")
 						&& (choose.getOtherwise() == null))
 				{
                     removeWhitespace(child);
 					String exp = getAttr("test", child, true);
 					try {
 						LSPExpr test = LSPExpr.parseFromString(exp);
 
 						choose.addWhen(compileExpr(el, test), compileChildren(child));
 					}
 					catch (ParseException e)
 					{
 						throw fixParseException(child, exp, e);
 					}
 				}
 				else if (child.getNamespaceURI().equals(LSP_CORE_NS)
 						    && child.getLocalName().equals("otherwise")
 						    && (choose.getOtherwise() == null))
 				{
                     removeWhitespace(child);
 					choose.setOtherwise(compileChildren(child));
 				}
 				else
 				{
 					throw fixSourceException(child,
 						"content of <lsp:choose> must match "
 						+ "(lsp:when+, lsp:otherwise?): "
 						+ child.getLocalName());
 				}
 			}
 			else if (_child instanceof Text)
 			{
 				Text child = (Text)_child;
 				if (!child.isWhitespaceNode())
 					throw fixSourceException(child,
 						"content of <lsp:choose> must match "
 						+ "(lsp:when+, lsp:otherwise?): CharacterData");
 				// ignore whitespace
 			}
 			else if (_child instanceof ProcessingInstruction)
 			{
 				// ignore PI
 			}
 			else
 			{
 	        	throw fixSourceException(_child, "Unrecognized XTree Node: "
 	        		+ _child.getClass().getName());
 			}
 		}
 
         if (choose.getNWhens() < 1)
             throw fixSourceException(el,
                 "content of <lsp:choose> must match "
                 + "(lsp:when+, lsp:otherwise?)");
         
 		return choose;
 	}
 
 	
 	private LSPNode process_for_each(Element el)
 		throws SAXException
 	{
         removeWhitespace(el);
         
 		String exp = getAttr("select", el, true);
 		String var = getAttr("var", el, true); 
 		String status = getAttr("status", el, false); 
 		try {
 			LSPExpr theList = LSPExpr.parseFromString(exp);
 
 			return new LSPForEach(compileExpr(el, theList), var, 
 				status, compileChildren(el), el);
 		}
 		catch (ParseException e)
 		{
 			throw fixParseException(el, exp, e);
 		}
 	}
 
 	
 	private LSPNode process_let(Element el)
 		throws SAXException
 	{
 		if (el.numberOfAttributes() == 0)
 			throw fixSourceException(el, 
 				"<lsp:let> must have at least one attribute");
 		
         removeWhitespace(el);
 
         String[] vars = new String[el.numberOfAttributes()];
 		LSPExpr[] values = new LSPExpr[el.numberOfAttributes()];
 
 		for (int i = 0; i < el.numberOfAttributes(); i++)
 		{
 			vars[i] = el.getAttributeLocalName(i);
 			String exp = el.getAttributeValue(i);
 			try {		
 				values[i] = compileExpr(el, LSPExpr.parseFromString(exp));				
 			}
 			catch (ParseException e)
 			{
 				throw fixParseException(el, exp, e);
 			}
 		}
 
 		return new LSPLet(vars, values, compileChildren(el), el);
 	}
 	
 	
 	private LSPExpr compileExpr(Node el, LSPExpr expr)
 		throws SAXException
 	{
 		if (expr instanceof StringLiteral)
 		{
 			return expr;
 		}
 		else if (expr instanceof NumberLiteral)
 		{
 			return expr;
 		}
 		else if (expr instanceof BinaryExpr)
 		{
 			return new BinaryExpr(
 				compileExpr(el, ((BinaryExpr)expr).getLeft()), 
 							compileExpr(el, ((BinaryExpr)expr).getRight()), 
 							((BinaryExpr)expr).getOp()); 
 		}
 		else if (expr instanceof UnaryExpr)
 		{
 			return new UnaryExpr(compileExpr(el, ((UnaryExpr)expr).getLeft()));
 		}
 		else if (expr instanceof FunctionCall)
 		{
 			FunctionCall fc = (FunctionCall)expr;
 			if (fc.getPrefix() == null || fc.getPrefix() == "")
 			{	// built-in function
 				BuiltInFunctionCall call = 
 					new BuiltInFunctionCall(fc.getName(), fc.numberOfArgs());
 					
 				for (int i = 0; i<fc.numberOfArgs(); i++)
 				{
 					call.addArgument(compileExpr(el, fc.getArg(i)));
 				}
 				return call;
 			}
 			else
 			{	// extension function
 				String ns = el.lookupNamespaceURI(fc.getPrefix());
 				if (ns == null)
 				{
 					throw fixSourceException(el, 
 						"no mapping for namespace prefix " + fc.getPrefix()); 
 				}
 					
 				String extLibClassName = lookupExtensionHandler(el, ns); 
 				if (extLibClassName == null)
 					throw fixSourceException(el, 
 						"no handler found for extension namespace " + ns);			
 
 				Class extLibClass;
 				String methodName = "_"+fc.getName();
 		
 				try {
 					extLibClass = Class.forName(extLibClassName);
 		
 					Class[] argTypes = new Class[fc.numberOfArgs()];
 					for (int i = 0; i<fc.numberOfArgs(); i++)
 					{
 						argTypes[i] = Object.class;
 					}
 					
 					java.lang.reflect.Method m = extLibClass.getMethod(methodName, argTypes);
 					if (!Object.class.equals(m.getReturnType()))
 					{
 						throw fixSourceException(el, "Extension function not found: "
 							+ fc.getName() + "/" + fc.numberOfArgs());
 					}			
 				}
 				catch (ClassNotFoundException e)
 				{
 					throw fixSourceException(el, "Extension library class not found: "
 						+ extLibClassName);
 				}
 				catch (NoSuchMethodException e)
 				{
 					throw fixSourceException(el, "Extension function not found: "
 						+ fc.getName() + "/" + fc.numberOfArgs());
 				}				
 												
 				ExtensionFunctionCall call = 
 					new ExtensionFunctionCall(
 						extLibClassName, fc.getName(), fc.numberOfArgs());
 					
 				for (int i = 0; i<fc.numberOfArgs(); i++)
 				{
 					call.addArgument(compileExpr(el, fc.getArg(i)));
 				}
 				return call;
 				
 			}
 		}
 		else if (expr instanceof VariableReference)
 		{
 			return expr;
 		}
 		else if (expr instanceof TupleExpr)
 		{
 			return new TupleExpr(compileExpr(el, ((TupleExpr)expr).getBase()), 
 								 compileExpr(el, ((TupleExpr)expr).getKey()));
 		}
 		else if (expr instanceof ConditionalExpr)
 		{
 			return new ConditionalExpr(
 				compileExpr(el, ((ConditionalExpr)expr).getTest()), 
 				compileExpr(el, ((ConditionalExpr)expr).getThen()), 
 				compileExpr(el, ((ConditionalExpr)expr).getElse())); 
 		}
         else
         {
 			throw new LSPException("Unrecognized LSPExpr: "
 				+ expr.getClass().getName());
 		}
 	}
 	
 }
