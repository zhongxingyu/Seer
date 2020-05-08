 /**
  * Copyright 2011-2012 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 /**
  * 
  */
 package fr.dutra.confluence2wordpress.core.toc;
 
 import static com.atlassian.confluence.content.render.xhtml.XhtmlConstants.*;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import javax.xml.stream.XMLStreamException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.atlassian.confluence.content.render.xhtml.HtmlElementIdCreator;
 import com.atlassian.confluence.renderer.PageContext;
 
 import fr.dutra.confluence2wordpress.core.author.Author;
 import fr.dutra.confluence2wordpress.util.StaxUtils;
 import fr.dutra.confluence2wordpress.util.XPathUtils;
 
 
 /**
  * @author Alexandre Dutra
  *
  */
 public class TOCBuilder {
 
     private static final String AUTHOR = "author";
 
 	private static final String H1 = "h1";
 	private static final String H2 = "h2";
 	private static final String H3 = "h3";
 	private static final String H4 = "h4";
 	private static final String H5 = "h5";
 	private static final String H6 = "h6";
 
 	private static final String NAME = "name";
 
 	private static final String LOCAL_NAME_PARAMETER_XPATH = "*[local-name()='parameter']";
 
 	private static final String MAIN_XPATH = "//*[name()='h1' or name()='h2' or name()='h3' or name()='h4' or name()='h5' or name()='h6' or name()='ac:macro']";
 
 	private Heading current;
     
     private List<Heading> headings = new ArrayList<Heading>();
 
 	private Heading root;
 
 	private PageContext pageContext;
 
     public List<Heading> getHeadings() {
         return headings;
     }
     
 	public Heading buildTOC(String storage, PageContext pageContext) throws TOCException {
 		this.pageContext = pageContext;
 		this.root = new Heading(0);
 		this.root.setLabel(pageContext.getPageTitle());
 		this.current = this.root;
 		try {
 			NodeList nodes = XPathUtils.evaluateXPathAsNodeList(StaxUtils.getReader(storage), MAIN_XPATH);
 			for(int i = 0; i < nodes.getLength(); i++) {
 				Node node = nodes.item(i);
 				String name = node.getNodeName();
 				if(name.equals(H1)){
 					gotHeading(1, node);
 				} else if(name.equals(H2)){
 					gotHeading(2, node);
 				} else if(name.equals(H3)){
 					gotHeading(3, node);
 				} else if(name.equals(H4)){
 					gotHeading(4, node);
 				} else if(name.equals(H5)){
 					gotHeading(5, node);
 				} else if(name.equals(H6)){
 					gotHeading(6, node);
 				} else {
 					String macroName = node.getAttributes().getNamedItemNS(CONFLUENCE_XHTML_NAMESPACE_URI, NAME).getTextContent();
 					if(AUTHOR.equals(macroName)){
 						gotAuthorMacro(node);
 					}
 				}
 			}
		} catch (XPathExpressionException e) {
			throw new TOCException(e);
		} catch (XMLStreamException e) {
			throw new TOCException(e);
		} catch (MalformedURLException e) {
			throw new TOCException(e);
		} catch (URISyntaxException e) {
			throw new TOCException(e);
 		}
 		return this.root;
 	}
 
 	private void gotHeading(int level, Node node) {
 		Heading heading = new Heading(level);
 		String body = node.getTextContent();
 		heading.setLabel(body);
 		try {
 			//AbstractPageLink.generateAnchor generates the final anchor
 			//but escaped for XML storage. We need URLencode instead,
 			//so we are basically doing exactly what generateAnchor does
 			//less the XML escaping.
 			//AbstractPageLink.generateAnchor(pageContext, body);
 			String anchor = URLEncoder.encode(HtmlElementIdCreator.convertToIdHtml5(pageContext.getPageTitle() + "-" + body), "UTF-8");
 			heading.setAnchor(anchor);
 		} catch (UnsupportedEncodingException e) {
 		}
 		Heading parent = findParentHeading(level);
 		parent.addChild(heading);
 		heading.setParent(parent);
 		current = heading;
 	}
 
 	private Heading findParentHeading(int level) {
 		Heading cur = current;
 		while(cur != root && cur.getLevel() >= level) {
 			cur = cur.getParent();
 		}
 		// this was abandoned since we mainly use H3 as uppermost level headings
 //		while(level - cur.getLevel()  > 1) {
 //			Heading h = new Heading(cur.getLevel() + 1);
 //			h.setParent(cur);
 //			cur.addChild(h);
 //			cur = h;
 //		}
 		return cur;
 	}
 
 	private void gotAuthorMacro(Node authorNode) throws XPathExpressionException, MalformedURLException, URISyntaxException {
 		Map<String, String> parameters = getAuthorMacroParameters(authorNode);
 		Author author = Author.fromMacroParameters(parameters);
 		current.setAuthor(author);
 	}
 
 	private Map<String, String> getAuthorMacroParameters(Node authorNode) throws XPathExpressionException {
 		Map<String,String> parameters = new HashMap<String, String>();
 		NodeList parameterNodes = XPathUtils.evaluateXPathAsNodeList(authorNode, LOCAL_NAME_PARAMETER_XPATH);
 		for(int i = 0; i < parameterNodes.getLength(); i++) {
 			Node parameterNode = parameterNodes.item(i);
 			String key = parameterNode.getAttributes().getNamedItemNS(CONFLUENCE_XHTML_NAMESPACE_URI, NAME).getTextContent();
 			String value = parameterNode.getTextContent();
 			parameters.put(key, value);
 		}
 		return parameters;
 	}
 	
 }
