 package org.faceletslite.imp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
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
 
  
 public class FaceletsCompilerImp implements FaceletsCompiler, CustomTag.Renderer
 {
 	public interface Namespaces 
 	{
 		String UI = "http://java.sun.com/jsf/facelets";
 	    String Core = "http://java.sun.com/jstl/core";
 	    String JspCore = "http://java.sun.com/jsp/jstl/core";
 	    String JsfH = "http://java.sun.com/jsf/html";
 	    String Xhtml = "http://www.w3.org/1999/xhtml"; 
 	    String None = "";
 	}
 	
 	private static final Logger log = Logger.getLogger(FaceletsCompiler.class.getName());
 	
     private final Map<String, ResourceReader> resourceReaderByNsUri = new HashMap<String, ResourceReader>();
     private final Map<String, Namespace> namespaceByUri = new HashMap<String, Namespace>();
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
     				throw new RuntimeException("document builder factory must be set to namespace-aware.");
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
     		String nsUri = namespace.getUri();
     		namespaceByUri.put(nsUri, namespace);
     		ResourceReader resourceReader = namespace.getResourceReader();
     		if (resourceReader!=null) {
     			resourceReaderByNsUri.put(nsUri, resourceReader);
     		}
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
     
 	public String html(Node node)
 	{
 		StringWriter writer = new StringWriter();
 		Transformer documentWriter = documentTransformerPool.get();
 		documentWriter.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 		documentWriter.setOutputProperty(OutputKeys.INDENT, "no");
 		documentWriter.setOutputProperty(OutputKeys.METHOD, "html");
 	    documentWriter.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 		try
 		{
 			documentWriter.transform(new DOMSource(node), new StreamResult(writer));
 			writer.flush();
 			return writer.toString();
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
 
 		RuntimeException error(String message, Exception reason)
 		{
 			message += "\r\n\t(while parsing '"+getResourceName()+"'";
 			if (Is.notEmpty(namespace)) {
 				message += ", namespace "+namespace;
 			}
 			message += ")";
 			throw new RuntimeException(message, reason);
 		}
 		
 		RuntimeException error(String message)
 		{
 			throw error(message, null);
 		}
 		
 		public String render(Object scope) 
 		{
 			Document targetDocument = newDocument();
 			DocumentFragment targetFragment = targetDocument.createDocumentFragment();
 			Dom.appendChildren(
 				targetFragment,
 				process(targetDocument, new MutableContext().scope(scope), null)
 			);
 			return "<!DOCTYPE html>"+"\r\n"+html(targetFragment);
 		}
 		
 		List<Node> process(Document targetDocument, MutableContext context, Map<String, SourceFragment> defines) 
 		{
 			Document workingCopy = sourceDocumentWorkingCopies.get();
 			try
 			{
 				Node sourceRoot = getRootNode(workingCopy);
 				return new Processor(targetDocument, context, defines).compile(sourceRoot);
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
 		
 		class Processor implements CustomTag.Processor
 		{
 			private final Document targetDocument;
 			private final Map<String, SourceFragment> defines;
 			private MutableContext context;
 			
 			public Processor(Document targetDocument, MutableContext context, Map<String, SourceFragment> defines) 
 			{
 				this.targetDocument = targetDocument;
 				this.context = context;
 				this.defines = defines;
 			}
 			
 			public Document getTargetDocument() 
 			{
 				return targetDocument;
 			}
 			
 			Processor with(MutableContext context, Map<String, SourceFragment> defines)
 			{
 				return new Processor(targetDocument, context, defines);
 			}
 			
 			List<Node> compileHtmlTag(Element sourceElement)
 			{
 				Element targetElement = getTargetDocument().createElement(sourceElement.getTagName());
 	    		for (Attr attr: Dom.attrs(sourceElement))
 	    		{
 		    		String name = attr.getName(); 
 	    			if (isHtmlNamespace(Dom.nsUri(attr)) && !name.startsWith("xmlns")) {
 						String newValue = eval(attr);
 						if (Is.notEmpty(newValue)) {
 							targetElement.setAttribute(name, newValue);
 						}
 	    			}
 	    		}
 	    		Dom.appendChildren(targetElement, compileChildren(sourceElement));
 				return nodes(targetElement);	
 			}
 			
 			List<Node> compileJspCoreTag(Element element) 
 	    	{
 				String tagName = element.getLocalName();
 				if ("set".equals(tagName)) {
 					Object value = attr(element, "value", Object.class);
 			        String var = requiredAttr(element, "var", String.class);
 			        context = context.put(var, value);
 			        return nodes();
 				}
 				if ("if".equals(tagName)) {
 					Object test = attr(element, "test", Object.class);
 					String var = attr(element, "var", String.class);
 					if (Is.conditionTrue(test)) {
 						return with(
 							context.put(var, test),
 							defines
 						).compileChildren(element);
 					}
 					return nodes();
 				}
 				if ("forEach".equalsIgnoreCase(tagName)) {
 					List<Node> result = new ArrayList<Node>();
 			        Collection<?> items = attr(element, "items", Collection.class);
 			        if (items==null) {
 			        	items = Collections.emptyList();
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
 			        	new ArrayList<Object>(items)
 			        );
 			        while (status.hasNext()) {
 			        	result.addAll(
 				        	with(
 				        		context.nest() // ??
 				        			.put(var, status.getCurrent())
 				        			.put(varStatus, status),
 				        		defines
 				        	).compileChildren(element)
 				        );
 						status.next();
 			        }
 			        return result;
 	    		}
 				if ("choose".equals(tagName)) {
 					for (Element when: Dom.childrenByTagName(element, Namespaces.Core, "when")) {
 						boolean test = requiredAttr(when, "test", Boolean.class);
 						if (test) {
 							return compileChildren(when);
 						}
 					}
 					for (Element otherwise: Dom.childrenByTagName(element, Namespaces.Core, "otherwise")) {
 						return compileChildren(otherwise);
 					}
					return nodes();
 				}
 				throw error("invalid core tag name '"+tagName+"'");
 	    	}
 	    		
 			List<Node> compileUiTag(Element element) 
 	    	{
 	    		String tagName = element.getLocalName();
 	    		if ("with".equals(tagName)) {
 	    			Object value = attr(element, "value", Object.class);
 	    			MutableContext newContext = value==null ? context : context.nest().scope(value); 
 					return with(newContext, defines).compileChildren(element);
 	    		}
 	    		if ("include".equals(tagName)) {
 	    			String src = attr(element, "src", String.class);
 	    			MutableContext newContext = collectParams(element);
 	    			if (Is.empty(src)) {
 	    				return with(newContext, defines).compileChildren(element);
 	    			}
 	    			else {
 		    			try {
 		    				TemplateImp template = FaceletsCompilerImp.this.compile(normalizeResourceNamePath(src), Namespaces.None);
 		    				return template.process(targetDocument, newContext, defines);
 		    			}
 		    			catch (IOException exc) {
 		    				throw error("cannot include '"+src+"'", exc);
 		    			}
 	    			}
 	    		}
 	    		if ("insert".equals(tagName)) {
 	    			String name = attr(element, "name", String.class);
 	    			if (name==null) { name = ""; }
 	    			SourceFragment fragment = defines==null ? null : defines.get(name);
 	    			if (fragment==null) {
 	    				return compileChildren(element);
 	    			}
 	    			else
 	    			{
 	    				return with(context, fragment.getDefinitions())
 	    					.compileChildren(fragment.getRoot());	    				
 	    			}
 	    		}
 	    		if ("composition".equals(tagName))
 	    		{
 	    			String templateAttr =  attr(element, "template", String.class);
 	    			if (Is.empty(templateAttr)) {
 	    				return compileChildren(element);
 	    			}
 	    			else {
 	    				return applyTemplate(element, templateAttr);
 	    			}
 	    		}
 	    		if ("component".equals(tagName) || "fragment".equals(tagName))
 	    		{
     				return compileChildren(element);
 	    		}
 	    		if ("decorate".equals(tagName)) {
 	    			String template =  requiredAttr(element, "template", String.class);
 	    			return applyTemplate(element, template);
 	    		}
 	    		if ("debug".equals(tagName)) {
 	    			log.log(Level.WARNING, "ignoring ui debug tag");
 	    			return nodes();
 				}
 	    		if ("remove".equals(tagName)) {
 	    			// nothing
 					return nodes();
 				}
 	    		if ("param".equals(tagName)) {
 	    			// ignore (already processed)
 	    			return nodes();
 				}
 	    		if ("define".equals(tagName)) {
 	    			// ignore (already processed)
 	    			return nodes();
 				}
 	    		throw error("invalid ui tag name '"+tagName+"'");
 	    	}
 
 			List<Node> compileJsfHTag(Element element) 
 			{
 				String tagName = element.getLocalName();
 				if ("outputText".equals(tagName)) {
 					String value = attr(element, "value", String.class);
 					if (Is.empty(value)) {
 						return nodes();
 					}
 					else {
 						Boolean escape = attr(element, "escape", Boolean.class);
 						return text(value, escape==null || !escape.equals(Boolean.FALSE));
 					}
 				}
 				throw error("invalid h tag name '"+tagName+"'");
 			}
 	    	
 			List<Node> compileCustomTag(Element element)
 			{
 				String tagName = element.getLocalName();
 				String nsUri = Dom.nsUri(element);
 				Namespace namespace = namespaceByUri.get(nsUri);
 				if (namespace!=null) {
 					CustomTag customTag = namespace.getCustomTag(tagName);
 					if (customTag!=null) {
 						return customTag.process(element, this, (CustomTag.Renderer)FaceletsCompilerImp.this);
 					}
 				}
 				MutableContext newContext = context.nest();
 				for (Attr attr: Dom.attrs(element)) {
 					newContext.put(
 						attr.getName(),
 						eval(attr.getValue(), Object.class)
 					);
 				}
 				try {
 					TemplateImp template = FaceletsCompilerImp.this.compile(tagName, nsUri);
 					return template.process(
 						getTargetDocument(),
 						newContext,
 						collectDefines(element)
 					);
 				} 
 				catch (IOException exc) {
 					throw error("cannot load "+element.getPrefix()+":"+tagName, exc);
 				}
 			}
 			
 			List<Node> compile(Node sourceNode)
 			{
 				if (sourceNode instanceof Document) {
 					return compile(sourceNode.getChildNodes());
 				}
 				else if (sourceNode instanceof Text) {
     				Text text = (Text) sourceNode;
     				String sourceText = text.getData(); 
     				String targetText = eval(sourceText, String.class);
     				if (Is.empty(targetText)) {
     					return nodes();
     				}
     				else {
     					return text(targetText, true);
     				}
     			}
     			else if (sourceNode instanceof Element) {
     				Element sourceElement = (Element)sourceNode;
     				String nsUri = Dom.nsUri(sourceElement);
     				if (isHtmlNamespace(nsUri)) {
     					return compileHtmlTag(sourceElement);
     				}
     				else if (Namespaces.Core.equals(nsUri) || Namespaces.JspCore.equals(nsUri)) {
 		    			return compileJspCoreTag(sourceElement);
 		    		}
     				else if (Namespaces.UI.equals(nsUri)) {
 		    			return compileUiTag(sourceElement);
 		    		}
     				else if (Namespaces.JsfH.equals(nsUri)) {
 		    			return compileJsfHTag(sourceElement);
 		    		}
     				else {
 		    			return compileCustomTag(sourceElement);
 		    		}
     			}
     			else if (sourceNode instanceof Comment) {
     				// TODO optional?
     			}
     			else if (sourceNode instanceof ProcessingInstruction)
     			{
     				ProcessingInstruction instruction = (ProcessingInstruction)sourceNode;
     				if ("facelets".equals(instruction.getTarget())) {
     					String data = instruction.getData().trim();
     					if (data.equals("suspendEvaluation")) {
     						context = context.nest().suspend(true);
     					}
     				}
     			}
 				return nodes();
 			}
 			
 			List<Node> compile(NodeList sourceNodes) 
 			{
 				List<Node> result = new ArrayList<Node>();
 				for (Node node: Dom.iterate(sourceNodes)) {
 					result.addAll(
 						compile(node)
 					);
 	    		}
 				return result;
 			}
 			
 			public List<Node> compileChildren(Node sourceNode) 
 			{
 				return compile(sourceNode.getChildNodes());
 			}
 
 			public List<Node> applyTemplate(Element sourceElement, String templateAttr)
 	    	{
 				try {
 					TemplateImp template = FaceletsCompilerImp.this.compile(normalizeResourceNamePath(templateAttr));
 					return template.process(
 						getTargetDocument(),
 						collectParams(sourceElement), 
 						collectDefines(sourceElement)
 					);
 				}
 				catch (IOException exc) {
 					throw error("cannot read template '"+templateAttr+"'", exc);
 				}
 			}
 			
 			public List<Node> text(String text, boolean escape) 
 			{
 				List<Node> result = new ArrayList<Node>();
 				Document document = getTargetDocument();
 				if (!escape) {
 					result.add(
 						document.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING, "")
 					);
 				}
 				result.add(document.createTextNode(text));
 				if (!escape) {
 					result.add(
 						document.createProcessingInstruction(StreamResult.PI_ENABLE_OUTPUT_ESCAPING, "")
 					);
 				}
 				return result;
 			}
 	    	
 			public List<Node> nodes(Node... nodes)
 			{
 				switch (nodes.length) {
 				case 0: 
 					return Collections.emptyList();
 				case 1:
 					return Collections.singletonList(nodes[0]);
 				}
 				List<Node> result = new ArrayList<Node>();
 				for (Node node: nodes) {
 					result.add(node);
 				}
 				return result;
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
 				}
 				result.put("", new SourceFragment(parent, context, defines));
 				return result; 
 	    	}
 	    	
 	    	MutableContext collectParams(Element parent)
 	    	{
 				MutableContext result = context.nest();
 				for (Element param: Dom.childrenByTagName(parent, Namespaces.UI, "param")) {
 					result.put(
 						requiredAttr(param, "name", String.class),
 						attr(param, "value", Object.class)
 					);
 				}
 				return result;
 	    	}
 	    	
 			boolean isHtmlNamespace(String nsUri)
 			{
 				return Is.empty(nsUri) || Namespaces.Xhtml.equals(nsUri);
 			}
 			
 		    String eval(Attr attr)
 		    {
 		    	return eval(attr.getValue(), String.class); 
 		    }
 		    
 		    public <T> T attr(Element element, String name, Class<T> clazz)
 		    {
 		    	String value = element.getAttribute(name);
 		    	return Is.empty(value) ? null : eval(value, clazz);
 		    }
 		    
 		    public <T> T requiredAttr(Element element, String name, Class<T> clazz)
 		    {
 		    	T result = attr(element, name, clazz);
 		    	if (Is.empty(result)) {
 		    		throw error("missing attribute '"+name+"' in "+element.getTagName());
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
 		    		throw error(message, exc);
 		    	}
 		    }
 		}
 	}
 
 	class SourceFragment
 	{
 		private final Node root;
 		private final MutableContext context;
 		private final Map<String, SourceFragment> defines;
 		
 		public SourceFragment(Node root, MutableContext context, Map<String, SourceFragment> defines) {
 			this.root = root;
 			this.context = context;
 			this.defines = defines;
 		}
 		
 		public Node getRoot()
 		{
 			return root;
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
 			return root.toString();
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
 		
 		MutableContext nest()
 		{
 			return new MutableContext(this);
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
 
 		static void appendChildren(Node parent, List<Node> children)
 		{
 			for (Node child: children) {
 				parent.appendChild(child);
 			}
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
