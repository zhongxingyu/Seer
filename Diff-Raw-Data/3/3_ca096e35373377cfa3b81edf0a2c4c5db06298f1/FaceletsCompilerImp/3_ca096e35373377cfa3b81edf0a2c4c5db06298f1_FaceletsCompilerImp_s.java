 package org.faceletslite.imp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.el.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.faceletslite.*;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
  
 public class FaceletsCompilerImp implements FaceletsCompiler 
 {
 	public interface Namespaces 
 	{
 		String UI = "http://java.sun.com/jsf/facelets";
 	    String Core = "http://java.sun.com/jsp/jstl/core";
 	    String JsfH = "http://java.sun.com/jsf/html";
 	    String Xhtml = "http://www.w3.org/1999/xhtml"; 
 	    String None = "";
 	}
 	
 	private static final Logger log = Logger.getLogger(FaceletsCompiler.class.getName());
 	
     private final Map<String, ResourceReader> resourceReaderByNsUri = new HashMap<String, ResourceReader>();
 	private final ExpressionFactory expressionFactory;
 	private final FunctionMapper functionMapper;
 	private final ELResolver resolver;
 	private final Map<String, Facelet> templateCache;
 	private final Pool<DocumentBuilder> documentBuilderPool;
 	private final Pool<Transformer> documentTransformerPool;
     
 	public FaceletsCompilerImp()
 	{
 		this(new DefaultConfiguration());
 	}
 	
     public FaceletsCompilerImp(final Configuration configuration) 
 	{
     	this.expressionFactory = configuration.getExpressionFactory();
     	this.templateCache = configuration.getCache();
     	this.resolver = configuration.getELResolver();
     	this.functionMapper = configuration.getFunctionMapper();
     	this.documentBuilderPool = new Pool<DocumentBuilder>() {
     		@Override public DocumentBuilder create() {
     			DocumentBuilder result = configuration.createDocumentBuilder();
     			if (!result.isNamespaceAware()) {
     				throw new RuntimeException("document builder factory must be set to namespace aware.");
     			}
     			result.reset();
     			return result;
     		}
     	};
     	this.documentTransformerPool = new Pool<Transformer>() {
     		@Override public Transformer create() {
     			Transformer result = configuration.createDocumentTransformer();
     			result.reset();
     			return result;
     		}
     	};
     	ResourceReader standardResourceReader = configuration.getResourceReader();
     	if (standardResourceReader!=null) {
     		resourceReaderByNsUri.put(Namespaces.None, standardResourceReader);
     	}
     	for (Namespace namespace: configuration.getCustomNamespaces())
     	{
     		resourceReaderByNsUri.put(namespace.getUri(), namespace.getResourceReader());
     	}
 	}
     
     @Override public TemplateImp compile(InputStream in) throws IOException
     {
 		return new TemplateImp("", Namespaces.None, parse(in, "input"));
     }
     
     @Override public TemplateImp compile(String resourceName) throws IOException
     {
     	return compile(resourceName, null);
     }
     
     @Override public TemplateImp compile(String resourceName, String nsUri) throws IOException
     {
     	String key = resourceName;
     	if (nsUri==null) {
     		nsUri = Namespaces.None;
     	}  
     	if (!Namespaces.None.equals(nsUri)) {
     		key = nsUri+"/"+key;
     	}
 	    TemplateImp result = templateCache==null ? null : (TemplateImp)templateCache.get(key);
 	    if (result==null) {
 	    	result = new TemplateImp(resourceName, nsUri, parse(resourceName, nsUri));
 	    	if (templateCache!=null) {
 	    		templateCache.put(key, result);
 	    	}
 	    }
 	    return result;
     }
     
     private Document parse(String resourceName, String nsUri) throws IOException
     {
     	String resourceInfo ="resource '"+resourceName+"'";
     	if (!Namespaces.None.equals(nsUri)) {
 			resourceInfo += ", namespace "+nsUri;
 		}
     	ResourceReader resourceReader = resourceReaderByNsUri.get(nsUri);
     	if (resourceReader==null) {
     		throw new IOException("no resource reader to read "+resourceInfo);
     	}
     	InputStream in = resourceReader.read(resourceName);
 		return parse(in, resourceInfo);
     }
     
     private Document parse(InputStream in, String resourceInfo) throws IOException
     { 
     	DocumentBuilder builder = documentBuilderPool.get();
     	try {
     		Document document = builder.parse(in);
     		document.normalizeDocument();
     		return document;
     	}
     	catch (SAXException exc)
     	{
     		if (exc instanceof SAXParseException) {
     			SAXParseException parseExc = (SAXParseException)exc;
     			int line = parseExc.getLineNumber();
     			int col = parseExc.getColumnNumber();
     			if (line>=0) {
     				resourceInfo += ", line "+line;
     				if (col>0) {
     					resourceInfo += ", column "+col;
     				}
     			}
     		}
     		throw new RuntimeException("cannot parse "+resourceInfo+":\r\n\t"+exc.getMessage(), exc); 
     	}
     	finally {
     		in.close();
     		documentBuilderPool.release(builder);
     	}
     }
     
     private Document newDocument()
     { 
     	DocumentBuilder builder = documentBuilderPool.get();
     	try {
     		return builder.newDocument();
     	}
     	finally {
     		documentBuilderPool.release(builder);
     	}
     }
     
 	class TemplateImp implements Facelet
 	{
 		private final Pool<Document> sourceDocumentWorkingCopies;
 		private final String resourceName;
 		private final String namespace;
 		
 		TemplateImp(String resourceName, String namespace, final Document sourceDocument) 
 		{
 			this.resourceName = resourceName;
 			this.namespace = namespace;
 			// even read access to a document is not thread-safe, so we pool!
 			this.sourceDocumentWorkingCopies = new Pool<Document>() {
 				protected Document create() {
 					Transformer transformer = documentTransformerPool.get();
 					try {
 						synchronized (sourceDocument) {
 							DOMSource source = new DOMSource(sourceDocument);
 							DOMResult result = new DOMResult();
 							transformer.transform(source,result);
 							return (Document)result.getNode();
 						}
 					} 
 					catch (TransformerException exc) {
 						throw new RuntimeException("cannot clone source document", exc);
 					}
 					finally {
 						documentTransformerPool.release(transformer);
 					}
 				};
 			};
 		}
 		
 		public String getResourceName() 
 		{
 			return resourceName;
 		}
 		
 		public String getNamespace() 
 		{
 			return namespace;
 		}
 
 		RuntimeException throwException(String message, Exception reason)
 		{
 			message += "\r\n\t(while parsing '"+getResourceName()+"'";
 			if (Is.notEmpty(namespace)) {
 				message += ", namespace "+namespace;
 			}
 			message += ")";
 			throw new RuntimeException(message, reason);
 		}
 		
 		RuntimeException throwException(String message)
 		{
 			throw throwException(message, null);
 		}
 		
 		String getResourcePath()
 		{
 			String result = "";
 			int lastIndexOfSlash = resourceName.lastIndexOf("/");
 			if (lastIndexOfSlash>=0) {
 				result = resourceName.substring(0, lastIndexOfSlash+1);
 			}
 			return result;
 		}
 		
 		String normalizeResourceNamePath(String resourceName) 
 		{
 			boolean absolute = resourceName.startsWith("/");
 			if (!absolute) {
 				resourceName = getResourcePath() + resourceName;
 			}
 			return resourceName;
 		}
 		
 		public void write(Object context, Writer writer)  
 		{
 			DocumentFragment targetDocument = newDocument().createDocumentFragment();
 			process(targetDocument, new MutableContext().scope(context), null);
 			Transformer documentWriter = documentTransformerPool.get();
 			documentWriter.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			documentWriter.setOutputProperty(OutputKeys.INDENT, "yes");
 			documentWriter.setOutputProperty(OutputKeys.METHOD, "html");
 		    documentWriter.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 			try
 			{
 				String docType = "<!DOCTYPE html>";
 				writer.append(docType+"\r\n"); // avoid messy DOM doctype handling!
 				documentWriter.transform(new DOMSource(targetDocument), new StreamResult(writer));
 				writer.flush();
 			}
 			catch (IOException exc)
 			{
 				throw new RuntimeException("cannot write", exc);
 			}
 			catch (TransformerException exc)
 			{
 				throw new RuntimeException("cannot write", exc);
 			}
 			finally
 			{
 				documentTransformerPool.release(documentWriter);
 			}
 		}
 		
 		public String render(Object context) 
 		{
 			StringWriter writer = new StringWriter();
 			write(context, writer);
 			return writer.toString();
 		}
 		
 		void process(Node targetParent, MutableContext context, Map<String, SourceFragment> defines) 
 		{
 			Document workingCopy = sourceDocumentWorkingCopies.get();
 			try
 			{
 				Node sourceParent = getRootNode(workingCopy);
 		    	process(targetParent, sourceParent.getChildNodes(), context, defines);
 			}
 			finally {
 				sourceDocumentWorkingCopies.release(workingCopy);
 			}
 		}
 		
 		Node getRootNode(Document sourceDocument)
 		{
     		for (Element composition: Dom.elementsByTagName(sourceDocument, Namespaces.UI, "composition")) {
     			return composition;
     		}
     		for (Element component: Dom.elementsByTagName(sourceDocument, Namespaces.UI, "component")) {
     			return component;
     		}
 	    	return sourceDocument;
 		}
 		
 		void process(Node targetParent, NodeList nodes, MutableContext context, Map<String, SourceFragment> defines)
 		{
 			new Handler(targetParent, context, defines).handle(nodes);
 		}
 		
 		class Handler
 		{
 			private final Node targetParent;
 			private final Map<String, SourceFragment> defines;
 			private MutableContext context;
 			
 			public Handler(Node targetParent, MutableContext context, Map<String, SourceFragment> defines) 
 			{
 				this.targetParent = targetParent;
 				this.context = context;
 				this.defines = defines;
 			}
 			
 			void handleHtmlTag(Element element)
 			{
 				Element newElement = document().createElement(element.getTagName());
 	    		for (Attr attr: Dom.attrs(element))
 	    		{
 	    			if (Is.empty(Dom.nsUri(attr))) {
 			    		String name = attr.getName(); 
 						String newValue = eval(attr);
 						if (Is.notEmpty(newValue)) {
 							newElement.setAttribute(name, newValue);
 						}
 	    			}
 	    		}
 				process(newElement, element.getChildNodes(), context, defines);
 				targetParent.appendChild(newElement);	
 			}
 			
 			void handleJspCoreTag(Element element) 
 	    	{
 				String tagName = element.getLocalName();
 				if ("set".equals(tagName)) {
 					Object value = attr(element, "value", Object.class);
 			        String var = requiredAttr(element, "var", String.class);
 			        context = new MutableContext(context).put(var, value);
 			        return;
 				}
 				if ("if".equals(tagName)) {
 					Object test = attr(element, "test", Object.class);
 					String var = attr(element, "var", String.class);
 					if (Is.conditionTrue(test)) {
 						process(
 							targetParent,
 							element.getChildNodes(),
 							new MutableContext(context).put(var, test),
 							defines
 						);
 					}
 					return;
 				}
 				if ("forEach".equalsIgnoreCase(tagName)) {
 			        List<?> items = attr(element, "items", List.class);
 			        if (items==null) {
 			        	items = new ArrayList<Object>();
 			        }
 			        String var = attr(element, "var", String.class);
 			        String varStatus = attr(element, "varStatus", String.class);
 			        String begin = attr(element, "begin", String.class);
 			        String end = attr(element, "end", String.class);
 			        String step = attr(element, "step", String.class);
 			        LoopTagStatusImp status = new LoopTagStatusImp(
 			        	Safe.toInt(begin, 0),
 			        	Safe.toInt(end, items.size()-1),
 			        	Safe.toInt(step, 1),
 				        	items
 			        );
 			        while (status.hasNext()) {
 			        	process(
 			        		targetParent,
 			        		element.getChildNodes(),
 			        		new MutableContext(context)
 			        			.put(var, status.getCurrent())
 			        			.put(varStatus, status),
 			        		defines
 			        	);
 						status.next();
 			        }
 			        return;
 	    		}
 				if ("choose".equals(tagName)) {
 					for (Element when: Dom.childrenByTagName(element, Namespaces.Core, "when")) {
 						boolean test = requiredAttr(when, "test", Boolean.class);
 						if (test) {
 							process(
 								targetParent,
 								when.getChildNodes(),
 								context,
 								defines
 							);
 							return;
 						}
 					}
 					for (Element otherwise: Dom.childrenByTagName(element, Namespaces.Core, "otherwise")) {
 						process(targetParent, otherwise.getChildNodes(), context, defines);
 					}
 					return;
 				}
 				if ("catch".equals(tagName)) {
 					try {
 						process(
 			        		targetParent,
 			        		element.getChildNodes(),
 			        		context,
 			        		defines
 			        	);
 					}
 					catch (Exception exc) {
 				        String var = requiredAttr(element, "var", String.class);
 				        context.put(var, exc);
 					}
 					return;
 				}
 				throw throwException("invalid core tag name '"+tagName+"'");
 	    	}
 	    		
 			void handleUiTag(Element element) 
 	    	{
 	    		String tagName = element.getLocalName();
 	    		if ("with".equals(tagName)) {
 	    			Object value = attr(element, "value", Object.class);
 	    			MutableContext newContext = value==null ? context : new MutableContext(context).scope(value); 
 					process(
 						targetParent,
 						element.getChildNodes(),
 						newContext,
 						defines
 					);
 	    			return;
 	    		}
 	    		if ("include".equals(tagName)) {
 	    			String src = attr(element, "src", String.class);
 	    			MutableContext newContext = collectParams(element);
 	    			if (Is.empty(src)) {
 	    				process(targetParent, element.getChildNodes(), newContext, defines);
 	    			}
 	    			else {
 		    			try {
 		    				compile(normalizeResourceNamePath(src), getNamespace())
 		    					.process(targetParent, newContext, defines);
 		    			}
 		    			catch (IOException exc) {
 		    				throw throwException("cannot include '"+src+"'", exc);
 		    			}
 	    			}
 	    			return;
 	    		}
 	    		if ("insert".equals(tagName)) {
 	    			String name = attr(element, "name", String.class);
 	    			if (name==null) { name = ""; }
 	    			SourceFragment fragment = defines==null ? null : defines.get(name);
 	    			if (fragment!=null) {
 						process(
 							targetParent,
 							fragment.getNodes(),
							fragment.getContext(),
 							fragment.getDefinitions()
 						);
 	    			}
 	    			else
 	    			{
 	    				process(targetParent, element.getChildNodes(), context, defines);
 	    			}
 	    			return;
 	    		}
 	    		if ("composition".equals(tagName))
 	    		{
 	    			String templateAttr =  attr(element, "template", String.class);
 	    			if (Is.notEmpty(templateAttr)) {
 	    				applyTemplate(element, templateAttr);
 	    			}
 	    			else {
 	    				process(targetParent, element.getChildNodes(), context, defines);
 	    			}
 	    			return;
 	    		}
 	    		if ("component".equals(tagName) || "fragment".equals(tagName))
 	    		{
     				process(targetParent, element.getChildNodes(), context, defines);
 	    			return;
 	    		}
 	    		if ("decorate".equals(tagName)) {
 	    			String template =  requiredAttr(element, "template", String.class);
 	    			applyTemplate(element, template);
 	    			return;
 	    		}
 	    		if ("debug".equals(tagName)) {
 	    			log.log(Level.WARNING, "ignoring ui debug tag");
 					return;
 				}
 	    		if ("remove".equals(tagName)) {
 	    			// nothing
 					return;
 				}
 	    		if ("param".equals(tagName)) {
 	    			throw throwException(element.getNodeName()
 	    				+" only allowed as child of :include, :composition or :decorate tag");
 				}
 	    		if ("define".equals(tagName)) {
 	    			throw throwException(element.getNodeName()
 	    				+" only allowed as child of :composition, :component, :decorate, :frament or custom tag");
 				}
 	    		throw throwException("invalid ui tag name '"+tagName+"'");
 	    	}
 
 			void handleJsfHTag(Element element) 
 			{
 				String tagName = element.getLocalName();
 				if ("outputText".equals(tagName)) {
 					String value = attr(element, "value", String.class);
 					if (Is.notEmpty(value)) {
 						Document document = Dom.document(targetParent);
 						Boolean escape = attr(element, "escape", Boolean.class);
 						boolean suspendEscaping = escape!=null && escape.equals(Boolean.FALSE);
 						if (suspendEscaping) {
 							targetParent.appendChild(
 								document.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING, "")
 							);
 						}
 						targetParent.appendChild(document.createTextNode(value));
 						if (suspendEscaping) {
 							targetParent.appendChild(
 								document.createProcessingInstruction(StreamResult.PI_ENABLE_OUTPUT_ESCAPING, "")
 							);
 						}
 					}
 					return;
 				}
 				throw throwException("invalid h tag name '"+tagName+"'");
 			}
 	    	
 			void handleCustomTag(Element element)
 			{
 				String tagName = element.getLocalName();
 				MutableContext newContext = new MutableContext(context);
 				for (Attr attr: Dom.attrs(element)) {
 					newContext.put(
 						attr.getName(),
 						eval(attr.getValue(), Object.class)
 					);
 				}
 				try {
 					compile(tagName, Dom.nsUri(element)).process(
 						targetParent,
 						newContext,
 						collectDefines(element)
 					);
 				} 
 				catch (IOException exc) {
 					throw throwException("cannot load "+element.getPrefix()+":"+tagName, exc);
 				}
 			}
 			
 			void handle(Node node)
 			{
     			if (node instanceof Text) {
     				Text text = (Text) node;
     				String oldText = text.getData(); 
     				String newText = eval(oldText, String.class);
     				if (Is.notEmpty(newText)) {
     					if (targetParent instanceof Document) {
     						throw throwException("missing <html/> root tag");
     					}
     					targetParent.appendChild(document().createTextNode(newText));
     				}
     			}
     			else if (node instanceof Element) {
     				Element element = (Element)node;
     				String nsUri = Dom.nsUri(element);
     				if (Is.empty(nsUri) || Namespaces.Xhtml.equals(nsUri)) {
     					handleHtmlTag(element);
     				}
     				else if (Namespaces.Core.equals(nsUri)) {
 		    			handleJspCoreTag(element);
 		    		}
     				else if (Namespaces.UI.equals(nsUri)) {
 		    			handleUiTag(element);
 		    		}
     				else if (Namespaces.JsfH.equals(nsUri)) {
 		    			handleJsfHTag(element);
 		    		}
     				else {
 		    			handleCustomTag(element);
 		    		}
     			}
     			else if (node instanceof Comment) {
     				// TODO optional?
     			}
     			else if (node instanceof ProcessingInstruction)
     			{
     				ProcessingInstruction instruction = (ProcessingInstruction)node;
     				if ("facelets".equals(instruction.getTarget())) {
     					String data = instruction.getData().trim();
     					if (data.equals("suspendEvaluation")) {
     						context = new MutableContext(context).suspend(true);
     					}
     				}
     			}
 			}
 			
 			void handle(NodeList nodes) 
 			{
 				for (Node node: Dom.iterate(nodes)) {
 					handle(node);
 	    		}
 			}
 
 			void applyTemplate(Element element, String templateAttr)
 	    	{
 				try {
 					compile(templateAttr).process(
 						targetParent,
 						collectParams(element), 
 						collectDefines(element));
 				}
 				catch (IOException exc) {
 					throw throwException("cannot read template '"+templateAttr+"'", exc);
 				}
 	    	}
 	    	
 	    	Map<String, SourceFragment> collectDefines(Element parent)
 	    	{
 	    		Map<String, SourceFragment> result = new HashMap<String, SourceFragment>();
 	    		if (defines!=null) {
 	    			result.putAll(defines);
 	    		}
 				for (Element define: Dom.childrenByTagName(parent, Namespaces.UI, "define")) {
 					String name = requiredAttr(define, "name", String.class);
 					result.put(name, new SourceFragment(define, context, defines));
 					parent.removeChild(define);
 				}
 				result.put("", new SourceFragment(parent, context, defines));
 				return result; 
 	    	}
 	    	
 	    	MutableContext collectParams(Element parent)
 	    	{
 				MutableContext result = new MutableContext(context);
 				for (Element param: Dom.childrenByTagName(parent, Namespaces.UI, "param")) {
 					result.put(
 						requiredAttr(param, "name", String.class),
 						requiredAttr(param, "value", Object.class)
 					);
 					parent.removeChild(param);
 				}
 				return result;
 	    	}
 	    	
 	    	Document document() 
 	    	{
 	    		return Dom.document(targetParent);
 	    	}
 	    	
 		    String eval(Attr attr)
 		    {
 		    	return eval(attr.getValue(), String.class); 
 		    }
 		    
 		    <T> T attr(Element element, String name, Class<T> clazz)
 		    {
 		    	String value = element.getAttribute(name);
 		    	return Is.empty(value) ? null : eval(value, clazz);
 		    }
 		    
 		    <T> T requiredAttr(Element element, String name, Class<T> clazz)
 		    {
 		    	T result = attr(element, name, clazz);
 		    	if (Is.empty(result)) {
 		    		throw throwException("missing attribute '"+name+"' in "+element.getTagName());
 		    	}
 		    	return result;
 		    }
 		    
 		    @SuppressWarnings("unchecked")
 			<T> T eval(String text, Class<T> clazz) 
 		    {
 		    	try {
 			    	return context.eval(text, clazz);
 		    	} 
 		    	catch (RuntimeException exc) {
 		    		String message = text+" expression evaluation failed:\r\n\t"+exc.getMessage();
 		    		throw throwException(message, exc);
 		    	}
 		    }
 		}
 	}
 
 	class SourceFragment
 	{
 		private final NodeList nodes;
 		private final MutableContext context;
 		private final Map<String, SourceFragment> defines;
 		
 		public SourceFragment(Node sourceParent, MutableContext context, Map<String, SourceFragment> defines) {
 			this.nodes = sourceParent.getChildNodes();
 			this.context = context;
 			this.defines = defines;
 		}
 		
 		public NodeList getNodes()
 		{
 			return nodes;
 		}
 		
 		public MutableContext getContext() 
 		{
 			return context;
 		}
 		
 		public Map<String, SourceFragment> getDefinitions() 
 		{
 			return defines;
 		}
 		
 		public String toString()
 		{
 			return nodes.getLength()==0 ? "[]" : nodes.item(0).getParentNode().toString();
 		}
 	}
 	
 	class MutableContext extends ELContext 
 	{
 		private final ELContext fallback;
 		private Object scope;
 		private boolean suspended;
     	private final Map<String, ValueExpression> variables = new HashMap<String, ValueExpression>();
 		private final VariableMapper variableMapper = new VariableMapper() 
 		{
 			@Override public ValueExpression setVariable(String name, ValueExpression expr) 
 			{
 				variables.put(name, expr);
 				return expr;
 			} 
 			@Override public ValueExpression resolveVariable(String name) 
 			{
 				Object value = scope==null ? null : resolver.getValue(MutableContext.this, scope, name);
 				if (value!=null) {
 					return wrap(value);
 				}
 				ValueExpression expr = variables.get(name);
 				if (expr!=null) {
 					return expr;
 				}
 				return fallback==null ? wrap(null) : fallback.getVariableMapper().resolveVariable(name);
 			}
 		};
 		
 		MutableContext() 
 		{
 			this(null);
 		}
 		
 		MutableContext(ELContext fallback) 
 		{
 			this.fallback = fallback;
 			this.suspended = (fallback instanceof MutableContext) ? ((MutableContext)fallback).suspended  : false;
 		}
 		
 		MutableContext scope(Object scope)
 		{
 			this.scope = scope;
 			return this;
 		}
 		
 		MutableContext suspend(boolean suspend)
 		{
 			this.suspended = suspend;
 			return this;
 		}
 
 		MutableContext put(String name, Object value) 
 		{
 			if (name!=null) {
 				variables.put(name, wrap(value));
 			}
 			return this;
 		}
 		
 		private ValueExpression wrap(Object value) 
 		{
 			return expressionFactory.createValueExpression(value, Object.class);
 		}
 		
 		@Override public ELResolver getELResolver() 
 		{
 			return resolver;
 		}
 
 		@Override public FunctionMapper getFunctionMapper() 
 		{
 			return functionMapper;
 		}
 
 		@Override public VariableMapper getVariableMapper() 
 		{
 			return variableMapper;
 		}
 		
 		@SuppressWarnings("unchecked")
 		<T> T eval(String text, Class<?> clazz) {
 			if (suspended) {
 				if (clazz==String.class || clazz==Object.class) {
 					return (T)text;
 				}
 			}
 			return (T)expressionFactory
 	    			.createValueExpression(this, text, clazz)
 	    			.getValue(this)
 	    	;
 		}
 	}
 
     public static class LoopTagStatusImp
     {
     	private int begin;
     	private int end;
     	private int step;
     	private int index;
     	private int count = 1;
     	private final List<?> items;
     	public LoopTagStatusImp(int begin, int end, int step, List<?> items) {
     		this.begin = begin;
     		this.end = end;
     		this.step = step;
     		this.index = begin;
     		this.items = items;
     	}
 		public Object getCurrent() { return Safe.get(items, index); }
 		public int getIndex() { return index; }
 		public int getCount() { return count; }
 		public boolean isFirst() { return index==begin; }
 		public boolean isLast() { return index==end; }
 		public boolean isEven() { return index % 2 == 0; }
 		public boolean isOdd() { return index % 2 == 1; }
 		public Integer getBegin() { return begin; }
 		public Integer getEnd() { return end; }
 		public Integer getStep() { return step; }
 		public boolean hasNext() { return index<=end; }
 		public void next() { index += step; count += 1; }
     }
     
     static class Dom 
     {
 		static Iterable<Node> iterate(NodeList nodeList)
 		{
 			List<Node> nodes = new ArrayList<Node>();
 			if (nodeList!=null) {
 				for (int i=0; i<nodeList.getLength(); ++i) {
 					nodes.add(nodeList.item(i));
 				}
 			}
 			return nodes;
 		}
 
 		static Iterable<Element> elementsByTagName(Node parent, String nsUri, String tagName)
 		{
 			if (parent instanceof Document) {
 				return elements(((Document)parent).getElementsByTagNameNS(nsUri, tagName));
 			}
 			if (parent instanceof Element) {
 				return elements(((Element)parent).getElementsByTagNameNS(nsUri, tagName));
 			}
 			return Collections.emptyList();
 		}
 
 		static Iterable<Element> childrenByTagName(Node parent, String nsUri, String tagName)
 		{
 			List<Element> elements = new ArrayList<Element>();
 			NodeList nodeList = parent.getChildNodes();
 			for (int i=0; i<nodeList.getLength(); ++i) {
 				Node node = nodeList.item(i);
 				if (node instanceof Element) {
 					Element element = (Element)node;
 					if (element.getLocalName().equals(tagName) && Safe.equals(nsUri(element), nsUri))
 					{
 						elements.add((Element)node);
 					}
 				}
 			}
 			return elements;
 		}
 
 		static Iterable<Element> elements(NodeList nodeList)
 		{
 			List<Element> elements = new ArrayList<Element>();
 			for (int i=0; i<nodeList.getLength(); ++i) {
 				Node node = nodeList.item(i);
 				if (node instanceof Element) {
 					elements.add((Element)node);
 				}
 			}
 			return elements;
 		}
 
 		static Iterable<Attr> attrs(Element element)
 		{
 			return attrs(element.getAttributes());
 		}
 
 		static Iterable<Attr> attrs(NamedNodeMap nodeMap)
 		{
 			List<Attr> attrs = new ArrayList<Attr>();
 			for (int i=0; i<nodeMap.getLength(); ++i) {
 				Node node = nodeMap.item(i);
 				if (node instanceof Attr) {
 					attrs.add((Attr)node);
 				}
 			}
 			return attrs;
 		}
 
 		static Document document(Node parent) 
 		{
 			return (parent instanceof Document) ? (Document)parent : parent.getOwnerDocument();
 		}
 
 		static String nsUri(Node node) 
     	{
     		//return node.getNamespaceURI(); <-- buggy in old XML implementations, such as the JDK's default one
 			return node.lookupNamespaceURI(node.getPrefix());
     	}
     }
     
     static abstract class Pool<T> 
     {
     	private final ConcurrentLinkedQueue<T> container = new ConcurrentLinkedQueue<T>();
     	
     	public T get()
     	{
     		T result = container.poll();
     		if (result==null) {
     			result = create();
     		}
     		return result;
     	}
     	
     	public void release(T object)
     	{
     		container.offer(object);
     	}
     	
     	protected abstract T create();
     }
 
     static class Safe
     {
     	@SuppressWarnings("unchecked")
     	static boolean equals(Object obj1, Object obj2)
     	{
     		if (obj1==obj2) {
     			return true;
     		}
     		if (obj1==null || obj2==null) {
     			return false;
     		}
     		Class<?> class1 = obj1.getClass();
     		Class<?> class2 = obj2.getClass();
     		if (class1.isArray() && class2.isArray()) {
     			Object[] array1 = (Object[])obj1;
     			Object[] array2 = (Object[])obj2;
     			return Arrays.deepEquals(array1, array2);
     		}
     		return obj1.equals(obj2);
     	}
     	
     	static <T> T get(List<T> list, int index)
     	{
     		return get(list, index, null);
     	}
     	
     	static <T> T get(List<T> list, int index, T _default)
     	{
     		return list!=null && 0<=index && index<list.size() ? list.get(index) : _default;
     	}
 
     	static int toInt(Object object, int _default)
     	{
     	    if (object!=null) {
     	        if (object instanceof Number) {
     	            return ((Number)object).intValue();
     	        }
     	        if (object instanceof CharSequence) {
     	            try {
     	                return Integer.parseInt(object.toString());
     	            }
     	            catch (NumberFormatException exc) {
     	            }
     	        }
     	    }
     	    return _default;
     	}
     }
     
     static class Is
     {
     	static boolean conditionTrue(Object object)
     	{
     		if (object==null) {
     			return false;
     		}
     		if (object instanceof Boolean) {
     			return (Boolean)object;
     		}
     		if (object instanceof String) {
     			return !((String)object).trim().equalsIgnoreCase("false");
     		}
     		return true;
     	}
     	
     	static boolean empty(Object object)
     	{
     		if (object==null) {
     			return true;
     		}
     		if (object instanceof String) {
     			return ((String)object).length()==0;
     		}
     		if (object instanceof List<?>) {
     			return ((List<?>)object).size()==0;
     		}
     		return false;
     	}
 
     	static boolean notEmpty(Object object)
     	{
     		return !empty(object);
     	}
     }
 }
