 package org.qrone.r7.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.qrone.coder.QClass;
 import org.qrone.coder.QFunc;
 import org.qrone.coder.QState;
 import org.qrone.coder.render.QLangJQuery;
 import org.qrone.r7.QrONEUtils;
 import org.qrone.r7.parser.HTML5Deck.HTML5Set;
 import org.qrone.r7.resolver.URIResolver;
 import org.qrone.r7.tag.HTML5TagHandler;
 import org.qrone.r7.tag.HTML5TagResult;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 import org.w3c.dom.css.CSSMediaRule;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.css.CSSStyleRule;
 import org.w3c.dom.css.CSSValue;
 import org.w3c.dom.stylesheets.MediaList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import se.fishtank.css.selectors.NodeSelectorException;
 import se.fishtank.css.selectors.dom.DOMNodeSelector;
 
 public class HTML5OM {
 	private HTML5Deck deck;
 	
 	private Document document;
 	private Element body;
 	private Map<Node, List<CSS3Rule>> map = new Hashtable<Node, List<CSS3Rule>>();
 	
 	private List<CSS3OM> stylesheets = new LinkedList<CSS3OM>();
 	private List<String> javascripts = new LinkedList<String>();
 
 	private List<String> requires = new LinkedList<String>();
 	private List<Element> jslibs = new LinkedList<Element>();
 	private List<Element> csslibs = new LinkedList<Element>();
 
 	private Map<String, String> metamap = new Hashtable<String, String>();
 	
 	private DOMNodeSelector nodeselector;
 
 	private Map<Element, List<HTML5TagResult>> extmap = new Hashtable<Element, List<HTML5TagResult>>();
 	private Map<String, Set<Node>> selectcache = new Hashtable<String, Set<Node>>();
 	
 	private URI uri;
 	
 	public HTML5OM(HTML5Deck deck, URI uri){
 		this.uri = uri;
 		this.deck = deck;
 	}
 	
 	public HTML5Deck getDeck(){
 		return deck;
 	}
 	
 	public HTML5Element getBody(){
 		return new HTML5Element(this, body);
 	}
 	
 	public Set<Node> select(String selector){
 		try{
 			Set<Node> nodes = selectcache.get(selector);
 			if(nodes == null){
 				nodes = nodeselector.querySelectorAll(selector);
 			}
 			return nodes;
 		}catch(NodeSelectorException e){}
 		return null;
 	}
 	
 	public void parseStyleSheet(URI path, String css) throws IOException{
 		final CSS3OM s = new CSS3OM();
 		s.parse(path, css);
 		CSS3Visitor v = new CSS3Visitor(){
 
 			@Override
 			public void visit(CSSMediaRule rule){
 				MediaList list = rule.getMedia();
 				if(list.getMediaText().indexOf("all") >= 0 || list.getMediaText().indexOf("screen") >= 0){
 					accept(rule);
 				}
 			}
 			
 			@Override
 			public void visit(CSSStyleRule style) {
 				Set<Node> set = select(style.getSelectorText());
 				if(set != null){
 					for (Iterator<Node> i = set.iterator(); i
 							.hasNext();) {
 						Node node = i.next();
 						List<CSS3Rule> list = map.get(node);
 						if(list == null){
 							list = new ArrayList<CSS3Rule>();
 							map.put(node, list);
 						}
 						list.add(new CSS3Rule(s, style));
 					}
 				}
 			}
 		};
 		v.visit(s.getStyleSheet());
 		stylesheets.add(s);
 	}
 	
 	public void parse(final URIResolver resolver) throws SAXException, IOException{
 		InputStream in = resolver.getInputStream(uri);
 		document = HTML5Parser.parse(new InputSource(in));
 		in.close();
 		
 		nodeselector = new DOMNodeSelector(document);
 		HTML5Visitor visitor = new HTML5Visitor() {
 			
 			private boolean inBody = false;
 			private boolean inScript = false;
 			private boolean inStyle = false;
 			
 			@Override
 			public void visit(Element e) {
 				for (Iterator<HTML5TagHandler> i = deck.getTagHandlers().iterator(); i
 						.hasNext();) {
 					HTML5TagHandler h = i.next();
 					HTML5TagResult r = h.process(new HTML5Element(HTML5OM.this, e));
 					if(r != null){
 						List<HTML5TagResult> l = extmap.get(e);
 						if(l == null){
 							l = new ArrayList<HTML5TagResult>();
 							extmap.put(e, l);
 						}
 						l.add(r);
 					}
 				}
 				
 				NamedNodeMap map = e.getAttributes();
 				for (int i = 0; i < map.getLength(); i++) {
 					Node n = map.item(i);
 					if(n.getNodeName().startsWith("on")){
 						e.setAttribute(n.getNodeName(), JSParser.compress(n.getNodeValue(),true)
 								.replace("__QRONE_PREFIX_NAME__", 
 								"qrone('" + getURI().toString() + "','__QRONE_ID__')"));
 					}else if(n.getNodeName().equals("href") && n.getNodeValue().startsWith("javascript:")){
 						String js = n.getNodeValue();
 						if(js.startsWith("javascript:")){
 							js = js.substring("javascript:".length());
 							e.setAttribute(n.getNodeName(), "javascript:" + JSParser.compress(js,true)
 									.replace("__QRONE_PREFIX_NAME__", 
 									  "qrone('" + getURI().toString() + "','__QRONE_ID__')"));
 						}
 					}
 				}
 				
 				if(e.getNodeName().equals("head")){
 					accept(e);
 				}else if(e.getNodeName().equals("body")){
 					HTML5OM.this.body = e;
 					inBody = true;
 					accept(e);
 					inBody = false;
 				}else if(e.getNodeName().equals("script")){
 					if(!inBody){
 						inScript = true;
 						accept(e);
 						inScript = false;
 						if(e.hasAttribute("src"))
 							jslibs.add(e);
 					}
 				}else if(e.getNodeName().equals("style")){
 					inStyle = true;
 					accept(e);
 					inStyle = false;
 				}else if(e.getNodeName().equals("link")){
 					if(e.hasAttribute("href")){
 						if(e.hasAttribute("include")){
 							try {
 								URI cssuri = uri.resolve(new URI(e.getAttribute("href")));
 								InputStream cssin = resolver.getInputStream(cssuri);
 								if(cssin != null){
 									parseStyleSheet(cssuri, QrONEUtils.convertStreamToString(cssin));
 								}
 							} catch (URISyntaxException e1) {
 								csslibs.add(e);
 							} catch (IOException e1) {
 								csslibs.add(e);
 							}
 						}else{
 							csslibs.add(e);
 						}
 					}
 				}else if(e.getNodeName().equals("meta")){
 					metamap.put(e.getAttribute("name"), e.getAttribute("content"));
 					accept(e);
 				}else{
 					accept(e);
 				}
 			}
 			
 			@Override
 			public void visit(Text n) {
 				if(inScript){
 					findRequires(n.getNodeValue());
 					javascripts.add(JSParser.clean(n.getNodeValue()));
 				}else if(inStyle){
 					try {
						parseStyleSheet(null, CSS3Parser.clean(n.getNodeValue()));
 					} catch (DOMException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		};
 		visitor.visit(document);
 
 		HTML5Visitor v = new HTML5Visitor() {
 			@Override
 			public void visit(Text n) {
 				
 			}
 			
 			@Override
 			public void visit(Element e) {
 			}
 		};
 		v.visit(document);
 	}
 	
 	public URI getURI(){
 		return uri;
 	}
 
 	private void findRequires(String js){
 		Pattern pattern = Pattern.compile("require\\s*\\(\\s*[\"'](.*?)[\"']\\s*\\)\\s*;?");
 		Matcher matcher = pattern.matcher(js);
 		while (matcher.find()) {
 			requires.add(matcher.group(1));
 		}
 	}
 
 	/*
 	public String getScripts() {
 		StringBuffer b = new StringBuffer();
 		for (Iterator<String> j = javascripts.iterator(); j.hasNext();) {
 			b.append(j.next());
 		}
 		return b.toString();
 	}
 	*/
 	
 	public List<CSS3OM> getStyles() {
 		return stylesheets;
 	}
 	
 	public List<String> getRequires() {
 		return requires;
 	}
 
 	public List<Element> getJSLibraries() {
 		return jslibs;
 	}
 
 	public List<Element> getCSSLibraries() {
 		return csslibs;
 	}
 	
 	public Map<String,String> getMETAMap() {
 		return metamap;
 	}
 	
 	public void removeProperty(Element e, String prop) {
 		List<CSS3Rule> l = map.get(e);
 		if(l != null){
 			for (Iterator<CSS3Rule> iter = l.iterator(); iter.hasNext();) {
 				iter.next().removeProperty(prop);
 			}
 		}
 		
 		String attr = e.getAttribute("style");
 		if(attr != null){
 			try {
 				CSSStyleDeclaration style = CSS3Parser.parsestyle(attr);
 				CSSValue v = style.getPropertyCSSValue(prop);
 				if(v != null){
 					style.removeProperty(prop);
 					e.setAttribute("style", style.getCssText());
 				}
 			} catch (IOException e1) {
 			}
 		}
 	}
 	
 	public String getProperty(Element e, String prop) {
 		CSS3Values v = getPropertyValue(e, prop);
 		return v != null ? v.getValue() : null;
 	}
 	
 	public CSS3Values getPropertyValue(Element e, String prop) {
 		List<CSS3Rule> l = map.get(e);
 		CSS3Values value = null;
 		CSS3Values imporantValue = null;
 		if(l != null){
 			for (Iterator<CSS3Rule> iter = l.iterator(); iter.hasNext();) {
 				CSS3Rule rule = iter.next();
 				CSS3Values v = rule.getProperty(prop);
 				if(v != null){
 					if(v.isImportant())
 						imporantValue = v;
 					else
 						value = v;
 				}
 			}
 		}
 		String attr = e.getAttribute("style");
 		if(attr != null){
 			try {
 				CSSStyleDeclaration style = CSS3Parser.parsestyle(attr);
 				CSSValue v = style.getPropertyCSSValue(prop);
 				if(v != null){
					CSS3Values v3 = new CSS3Values(null, style, v, prop);
 					if(v3.isImportant())
 						imporantValue = v3;
 					else
 						value = v3;
 				}
 			} catch (IOException e1) {
 			}
 		}
 		return imporantValue != null ? imporantValue : value;
 	}
 
 	public void setProperty(Element e, String prop, String value) {
 		String attr = e.getAttribute("style");
 		if(attr != null){
 			try {
 				CSSStyleDeclaration style = CSS3Parser.parsestyle(attr);
 				style.setProperty(prop, value, null);
 				e.setAttribute("style", style.getCssText());
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}else{
 			e.setAttribute("style", prop + ":" + value + ";");
 		}
 	}
 
 	public void renameProperty(Element e, String prop, String newprop) {
 		List<CSS3Rule> l = map.get(e);
 		if(l != null){
 			for (Iterator<CSS3Rule> iter = l.iterator(); iter.hasNext();) {
 				CSS3Rule rule = iter.next();
 				CSS3Values v = rule.getProperty(prop);
 				if(v != null){
 					rule.setProperty(newprop, v.getValue(), v.isImportant());
 					rule.removeProperty(prop);
 				}
 			}
 		}
 		
 		String attr = e.getAttribute("style");
 		if(attr != null){
 			try {
 				CSSStyleDeclaration style = CSS3Parser.parsestyle(attr);
 				CSSValue v = style.getPropertyCSSValue(prop);
 				if(v != null){
 					style.setProperty(newprop, v.getCssText(), style.getPropertyPriority(prop));
 					style.removeProperty(prop);
 					e.setAttribute("style", style.getCssText());
 				}
 			} catch (IOException e1) {
 			}
 		}
 	}
 	
 	public List<HTML5TagResult> getTagResult(Element e){
 		return extmap.get(e);
 	}
 	
 	//---
 	/*
 	private void process(HTML5Writer t){
 		process(t, false, null);
 	}
 
 	private void process(HTML5Writer t, boolean bodyOnly, String id){
 		process(t, bodyOnly, null, null, null);
 	}
 
 	private void process(final HTML5Writer t, final boolean bodyOnly, 
 			final Stack<HTML5OM> xomlist, final String target, String id){
 		process(t, bodyOnly ? body : document.getDocumentElement(),
 				xomlist, target, id);
 	}
 	*/
 	//private void process(final HTML5Writer t, final Element element, 
 	//		final Stack<HTML5OM> xomlist, final String target, String id){
 	
 
 	public void process(final HTML5Template t, final Set<HTML5OM> xomlist){
 		
 		final HTML5Template bodyt = new HTML5Template(this, t.getURI());
 		//final Set<HTML5OM> xomlist = new HashSet<HTML5OM>();
 		process(bodyt, t, body, null, xomlist);
 		
 
 		final HTML5Set set = deck.getRecurseHeader(getURI(), xomlist);
 		HTML5Selializer s = new HTML5Selializer() {
 			@Override
 			public void visit(Document e) {
 				out("<!DOCTYPE html>");
 				super.visit(e);
 			}
 			
 			@Override
 			public void visit(Element e) {
 				if(e.getNodeName().equals("head")){
 					start(e);
 					accept(e);
 					deck.outputStyles(b, set, uri);
 					end(e);
 				}else if(e.getNodeName().equals("body")){
 					start(e);
 					t.append(bodyt);
 					deck.outputScripts(b, set, uri);
 					end(e);
 				}else if(e.getNodeName().equals("script")){
 				}else if(e.getNodeName().equals("style")){
 				}else if(e.getNodeName().equals("link")){
 				}else if(e.getNodeName().equals("meta")){
 					if(e.getAttribute("name").equals("extends")){
 					}else{
 						start(e);
 						accept(e);
 						end(e);
 					}
 				}else{
 					out(e);
 				}
 			}
 
 			@Override
 			public void visit(Text n) {
 				out(n.getNodeValue());
 			}
 		};
 		
 		s.visit(this, document, null, t);
 	}
 	
 	public void process(final HTML5Writer t, final NodeProcessor p,
 			Node node, String id, final Set<HTML5OM> xomlist){
 		if(node == null)
 			node = body;
 		if(xomlist != null && !xomlist.contains(this)){
 			xomlist.add(this);
 
 			String path = getMETAMap().get("include-in");
 			if(path != null){
 				final String[] paths = path.split("#", 2);
 				if(paths.length == 2){
 					HTML5OM xom = deck.compile(getURI().resolve(paths[0]));
 					if(xom != null){
 						xomlist.add(this);
 						xom.process(t, new NodeProcessor() {
 							@Override
 							public void processTarget(HTML5Writer w, HTML5OM om, Element node) {
 								HTML5OM.this.process(t, p, null, null, xomlist);
 							}
 							
 							@Override
 							public boolean isTarget(Element node) {
 								String id = node.getAttribute("id");
 								return id != null && id.equals(paths[1]);
 							}
 						}, null, null, xomlist);
 						return;
 					}
 				}
 			}
 		}
 		
 		HTML5Selializer s = new HTML5Selializer() {
 			int formatting = 0;
 			boolean inScript;
 			
 			@Override
 			public void visit(Element e) {
 				if(e.getNodeName().equals("body")){
 					accept(e);
 				}else if(e.getNodeName().equals("script")){
 					start(e);
 					inScript = true;
 					accept(e);
 					inScript = false;
 					end(e);
 				}else if(e.getNodeName().equals("style")){
 				}else if(e.getNodeName().equals("link")){
 				}else if(e.getNodeName().equals("pre") || e.getNodeName().equals("code")){
 					formatting++;
 					out(e);
 					formatting--;
 				}else if(e.getNodeName().equals("meta")){
 					if(e.getAttribute("name").equals("extends")){
 					}else{
 						start(e);
 						accept(e);
 						end(e);
 					}
 				}else{
 					out(e);
 				}
 			}
 
 			@Override
 			public void visit(Text n) {
 				if(inScript){
 					out(jsmin(n.getNodeValue(), "qrone[\"" + getURI().toString() + "\"]"));
 				}else if(formatting>0){
 					writeraw(n.getNodeValue());
 				}else{
 					write(n.getNodeValue());
 				}
 			}
 			
 			@Override
 			protected void out(final Element e) {
 				final CSS3Values include = getPropertyValue(e, "include");
 				final String uniqueid = QrONEUtils.uniqueid();
 				if(include != null){
 					final String path = include.getURL();
 					if(path != null && path.trim().length() > 0){
 						super.out(e,new Delegate() {
 							@Override
 							public void accept() {
 								HTML5OM xom = deck.compile(getURI().resolve(path));
 								xom.process(t, p, null, uniqueid, xomlist);
 							}
 						});
 					}else{
 						try{
 							throw new IOException();
 						}catch(IOException e1){
 							e1.printStackTrace();
 						}
 					}
 				}else if(p != null && p.isTarget(e)){
 					super.out(e,new Delegate() {
 						@Override
 						public void accept() {
 							p.processTarget(t, HTML5OM.this, e);
 						}
 					});
 				}else{
 					super.out(e);
 				}
 			}
 		};
 		s.visit(this, node, id, t);
 	}
 
 	public String getHTML(){
 		HTML5Template t = new HTML5Template(this);
 		String o = t.out();
 		try {
 			deck.getSpriter().create();
 		} catch (IOException e) {}
 		return o;
 	}
 
 	private String scriptCache = null;
 	private String scriptCacheNoHTML = null;
 	public String getScripts(boolean html){
 		if(html){
 			if(scriptCache != null){
 				return scriptCache;
 			}
 		}else{
 			if(scriptCacheNoHTML != null){
 				return scriptCacheNoHTML;
 			}
 		}
 
 		final QClass jqueryclass = new QClass(getURI().toString());
 		final QFunc method = jqueryclass.constructor();
 		method.arg("String", "id");
 		final QState jqueryhtml = method.state().returns();
 		
 		process(new HTML5Writer() {
 			@Override
 			public void append(String key, String value) {
 				jqueryhtml.var("String", key);
 			}
 			
 			@Override
 			public void append(String str) {
 				jqueryhtml.str(str);
 			}
 			
 			@Override
 			public void append(char c) {
 				jqueryhtml.str(String.valueOf(c));
 			}
 
 			@Override
 			public void append(HTML5Template t) {
 			}
 		}, null, body, null, null);
 		
 		StringBuilder b = new StringBuilder();
 		if(!html){
 			b.append("qrone[\"" + getURI().toString() + "\"]=function(){};");
 		}else{
 			QLangJQuery q = new QLangJQuery();
 			q.accept(jqueryclass);
 			b.append(q.build());
 		}
 		
 		for (Iterator<String> i = javascripts.iterator(); i
 				.hasNext();) {
 			String userjs = i.next();
 			b.append(JSParser.compress(userjs.toString(), true)
 				.replace("__QRONE_PREFIX_NAME__","qrone[\"" + getURI().toString() + "\"]"));
 		}
 		
 
 		if(html){
 			scriptCache = b.toString();
 		}else{
 			scriptCacheNoHTML = b.toString();
 		}
 		return b.toString();
 	}
 	
 	public String getScripts() {
 		return getScripts(true);
 	}
 	
 	public String serialize(){
 		return serialize(null);
 	}
 	
 	public String serialize(String lang){
 		if(lang != null && lang.equals("js")){
 			return getScripts(true);
 		}else{
 			return getHTML();
 		}
 	}
 	
 }
