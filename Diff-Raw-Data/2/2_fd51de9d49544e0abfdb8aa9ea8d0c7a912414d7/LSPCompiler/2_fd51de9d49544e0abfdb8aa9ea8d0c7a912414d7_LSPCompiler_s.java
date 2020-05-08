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
 
 package nu.staldal.lsp;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 
 import nu.staldal.xtree.*;
 import nu.staldal.syntax.ParseException;
 
 import nu.staldal.lsp.compile.*;
 import nu.staldal.lsp.expr.*;
 import nu.staldal.lsp.compiledexpr.*;
 
 import nu.staldal.lagoon.util.LagoonUtil;
 
 
 public class LSPCompiler
 {
	private static final boolean DEBUG = true;
 
     private static final String LSP_CORE_NS = "http://staldal.nu/LSP/core";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
 
     private TreeBuilder tb;
     private URLResolver resolver;
 
     private Hashtable importedFiles;
     private Vector includedFiles;
     private boolean compileDynamic;
     private boolean executeDynamic;
 
 	private int raw;
 	private boolean inPi;
 	private boolean inExtElement;
 	private Hashtable extDict = new Hashtable();
 
 
 	private static SAXException fixSourceException(Node node, String msg)
 	{
 		return new SAXParseException(msg, null,
 			node.getSystemId(), node.getLineNumber(), node.getColumnNumber());
 	}
 
 
 	private static SAXException fixParseException(Node node,
 		String expression, ParseException e)
 	{
 		return new SAXParseException(
 			"Illegal LSP expression:\n"
 				+ expression + "\n" + LSPUtil.nChars(e.getColumn()-1,' ')
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
 
 
 	private static Vector processTemplate(Node n,
         char left, char right, char quot1, char quot2,
         String template)
         throws SAXException
 	{
 		Vector vector = new Vector(template.length()/16);
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
 							vector.addElement(text.toString());
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
                         vector.addElement(res);
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
 			vector.addElement(text.toString());
 		text = null;
 
 		return vector;
 	}
 
 
     public LSPCompiler()
     {
         tb = null;
         resolver = null;
     }
 
 
     public ContentHandler startCompile(URLResolver r)
     {
     	importedFiles = new Hashtable();
 	    includedFiles = new Vector();
         compileDynamic = false;
         executeDynamic = false;
 		
 		inExtElement = false;
 
         resolver = r;
         tb = new TreeBuilder();
 
         return tb;
     }
 
 
     public LSPPage finishCompile()
     	throws SAXException, IOException
     {
         if (tb == null) throw new IllegalStateException(
             "startCompile() must be invoked before finishCompile()");
 
         Element tree = tb.getTree();
 
 		long startTime = System.currentTimeMillis();
 		if (DEBUG) System.out.println("LSP Compile...");
 
         processImports(tree);
 
         raw = 0;
         inPi = false;
         LSPNode compiledTree = compileNode(tree);
 
         tb = null;
         resolver = null;
 
 		long timeElapsed = System.currentTimeMillis()-startTime;
 		if (DEBUG) System.out.println("in " + timeElapsed + " ms");
 
         return new LSPInterpreter(compiledTree, importedFiles, includedFiles,
             compileDynamic, executeDynamic);
     }
 
 
     private void processImports(Element el)
     	throws SAXException, IOException
     {
 		for (int i = 0; i < el.numberOfChildren(); i++)
 		{
 			if (!(el.getChild(i) instanceof Element)) continue;
 
 			Element child = (Element)el.getChild(i);
 
 			if ((child.getNamespaceURI() != null)
 					&& child.getNamespaceURI().equals(LSP_CORE_NS)
 					&& child.getLocalName().equals("import"))
 			{
 				String url = getAttr("file", child, true);
 				if (importedFiles.put(url, url) != null)
 				{
 					// *** check for circular import
 				}
 
 				TreeBuilder tb = new TreeBuilder();
 				resolver.resolve(url, tb);
 				Element importedDoc = tb.getTree();
 
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
 		if ((el.getNamespaceURI() != null)
 				&& el.getNamespaceURI().equals(LSP_CORE_NS))
 		{
 			// Dispatch LSP command
 			if (el.getLocalName().equals("root"))
 			{
 				return process_root(el);
 			}
 			else if (el.getLocalName().equals("processing-instruction"))
 			{
 				return process_processing_instruction(el);
 			}
 			else if (el.getLocalName().equals("include"))
 			{
 				return process_include(el);
 			}
 			else if (el.getLocalName().equals("if"))
 			{
 				return process_if(el);
 			}
 			else if (el.getLocalName().equals("raw"))
 			{
 				return process_raw(el);
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
 			// *** more to implement
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
 			
 			Class extClass = lookupExtensionHandler(el, el.getNamespaceURI());
 			if (!inExtElement && (extClass != null))
 			{
 				inExtElement = true;
 				inExtElementNow = true;
 				newEl = new LSPExtElement(extClass.getName(), 
 					el.getNamespaceURI(), el.getLocalName(),
 					el.numberOfAttributes(), el.numberOfChildren()); 
 			}
 			else
 			{
 				newEl = new LSPElement(el.getNamespaceURI(), el.getLocalName(),
 					el.numberOfAttributes(), el.numberOfChildren());
 			}
 
 			for (int i = 0; i < el.numberOfNamespaceMappings(); i++)
 			{
 				String[] m = el.getNamespaceMapping(i);
 				if (!m[1].equals(LSP_CORE_NS))
 					newEl.addNamespaceMapping(m[0], m[1]);
 			}
 
 			for (int i = 0; i < el.numberOfAttributes(); i++)
 			{
 				String URI = el.getAttributeNamespaceURI(i);
 				String local = el.getAttributeLocalName(i);
 				String type = el.getAttributeType(i);
 				String value = el.getAttributeValue(i);
 
 				LSPExpr newValue = (raw > 0)
                     ? new StringLiteral(value)
                     : processTemplateExpr(el, value);
 
 				newEl.addAttribute(URI, local, type, newValue);
 			}
 
 			compileChildren(el, newEl);
 			
 			if (inExtElementNow) inExtElement = false;
 
 			return newEl;
 		}
     }
 
 	
 	private Class lookupExtensionHandler(Node el, String ns)
 		throws SAXException
 	{
 		if (ns == null || ns.length() == 0) 
 			return null;
 		
         Class cls = (Class)extDict.get(ns);
 		
 		if (cls != null) return cls;
 		
 		String className = null;
         try
         {
 			String fileName = "/nu/staldal/lsp/extlib/" 
 				+ LagoonUtil.encodePath(ns);
 			InputStream is = getClass().getResourceAsStream(fileName);
 			if (is == null) return null;
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			className = br.readLine();
 			if (className == null)
 				throw fixSourceException(el,
 					"Illegal LSP Extension config file: " + fileName);
 
 			cls = Class.forName(className);
 			if (!nu.staldal.lsp.LSPExtLib.class.isAssignableFrom(cls))
 			throw fixSourceException(el, 
 				"LSP extension class " + className 
 				+ " must implement nu.staldal.lsp.LSPExtLib");
 				
 			extDict.put(ns, cls);
 			return cls;
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw fixSourceException(el, 
 				"extension class " + className + " not found"); 
 		}
         catch (IOException e)
         {
             throw fixSourceException(el,
                 "Unable to read producer config file: " + e.toString());
         }
 	}
 	
 
     private LSPNode compileNode(Text text)
         throws SAXException
     {
 		if (raw > 0)
 		{
 			return new LSPText(text.getValue());
 		}
 		else
 		{
 			Vector vec = processTemplate(text, '{', '}', '\'', '\"',
 				text.getValue());
 
 			LSPContainer container = new LSPContainer(vec.size());
 
 			for (Enumeration e = vec.elements(); e.hasMoreElements(); )
 			{
 				Object o = e.nextElement();
 				if (o instanceof String)
 				{
 					container.addChild(new LSPText((String)o));
 				}
 				else if (o instanceof LSPExpr)
 				{
 					container.addChild(
 						new LSPTemplate(compileExpr(text, (LSPExpr)o)));
 				}
 			}
 
 			if (container.numberOfChildren() == 1)
 				return container.getChild(0);
 			else
 				return container;
 		}
     }
 
 
     private LSPNode compileNode(ProcessingInstruction pi)
         throws SAXException
     {
         return new LSPContainer(0);
     }
 
 
     private LSPExpr processTemplateExpr(Node n, String template)
         throws SAXException
     {
 		Vector vec = processTemplate(n, '{', '}', '\'', '\"', template);
 
 		BuiltInFunctionCall expr = new BuiltInFunctionCall("concat", vec.size());
 
 		for (Enumeration e = vec.elements(); e.hasMoreElements(); )
 		{
 			Object o = e.nextElement();
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
 			container.addChild(compileNode(child));
 		}
 	}
 
 
 	private LSPNode compileChildren(Element el)
 		throws SAXException
 	{
 		if (el.numberOfChildren() == 1)
 			return compileNode(el.getChild(0)); // optimization
 		else
 		{
 			LSPContainer container = new LSPContainer(el.numberOfChildren());
 			compileChildren(el, container);
 			return container;
 		}
 	}
 
 
 	private LSPNode process_root(Element el)
 		throws SAXException
 	{
 		return compileChildren(el);
 	}
 
 
 	private LSPNode process_raw(Element el)
 		throws SAXException
 	{
         raw++;
         LSPNode ret = compileChildren(el);
         raw--;
         return ret;
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
 
 		return new LSPProcessingInstruction(name, data);
 	}
 
 
 	private LSPNode process_include(Element el)
 		throws SAXException
 	{
         LSPExpr file = processTemplateExpr(el, getAttr("file", el, true));
 
 		if (file instanceof StringLiteral)
 		{
 			includedFiles.addElement(((StringLiteral)file).getValue());
 		}
 		else
 		{
 			executeDynamic = true;
 		}
 
 		return new LSPInclude(file);
 	}
 
 
 	private LSPNode process_if(Element el)
 		throws SAXException
 	{
 		String exp = getAttr("test", el, true);
 		try {
 			LSPExpr test = LSPExpr.parseFromString(exp);
 
 			return new LSPIf(compileExpr(el, test), compileChildren(el));
 		}
 		catch (ParseException e)
 		{
 			throw fixParseException(el, exp, e);
 		}
 	}
 
 
 	private LSPNode process_choose(Element el)
 		throws SAXException
 	{
 		LSPChoose choose = new LSPChoose(el.numberOfChildren());
 		for (int i = 0; i < el.numberOfChildren(); i++)
 		{
 			Node _child = el.getChild(i);
 			if (_child instanceof Element)
 			{
 				Element child = (Element)_child;
 
 				if ((child.getNamespaceURI() != null)
 						&& child.getNamespaceURI().equals(LSP_CORE_NS)
 						&& child.getLocalName().equals("when")
 						&& (choose.getOtherwise() == null))
 				{
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
 				else if ((child.getNamespaceURI() != null)
 						&& child.getNamespaceURI().equals(LSP_CORE_NS)
 						&& child.getLocalName().equals("otherwise")
 						&& (choose.getOtherwise() == null))
 				{
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
 				if (child.getValue().trim().length() > 0)
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
 
 		return choose;
 	}
 
 
 	private LSPNode process_for_each(Element el)
 		throws SAXException
 	{
 		String exp = getAttr("select", el, true);
 		String var = getAttr("var", el, true); 
 		try {
 			LSPExpr theList = LSPExpr.parseFromString(exp);
 
 			return new LSPForEach(compileExpr(el, theList), var, compileChildren(el));
 		}
 		catch (ParseException e)
 		{
 			throw fixParseException(el, exp, e);
 		}
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
 					
 				Class extClass = lookupExtensionHandler(el, ns); 
 				if (extClass == null)
 					throw fixSourceException(el, 
 						"no handler found for extension namespace " + ns);			
 				
 				ExtensionFunctionCall call = 
 					new ExtensionFunctionCall(
 						extClass.getName(), fc.getName(), fc.numberOfArgs());
 					
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
 								 ((TupleExpr)expr).getName());
 		}
         else
         {
 			throw new LSPException("Unrecognized LSPExpr: "
 				+ expr.getClass().getName());
 		}
 	}
 	
 }
