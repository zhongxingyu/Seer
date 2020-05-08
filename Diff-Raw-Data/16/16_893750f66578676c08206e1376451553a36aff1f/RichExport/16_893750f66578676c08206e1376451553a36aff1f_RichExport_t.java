 /*******************************************************************************
  * Copyright (c) 2011 Árpád Magosányi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the License
  * which accompanies this distribution in the file LICENSE.txt
  *******************************************************************************/
 package org.rulez.magwas.styledhtml;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import java.io.OutputStream;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.nio.charset.Charset;
 import java.io.OutputStreamWriter;
 
 
 
 import org.w3c.dom.DOMConfiguration;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.Document;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 
 
 
 
 
 import uk.ac.bolton.archimate.editor.model.IModelExporter;
 import uk.ac.bolton.archimate.model.IArchimateModel;
 import uk.ac.bolton.archimate.model.util.ArchimateResource;
 import uk.ac.bolton.archimate.model.util.ArchimateResourceFactory;
 import uk.ac.bolton.archimate.editor.utils.HTMLUtils;
 
 
 /**
  * Rich Exporter of Archimate model
  * <p>
  * Input is the style directory, containing:<ul>
  * 		<li>a file called style.xslt: the xml stylesheet to be applied to the "rich model file"</li>
  * 		<li>any other files needed for the presentation of resulting html</li></ul>
  * 
  * <p>
  * In the future (If I still think it is a good idea), the style directory may contain a file containing the name of scripts to be run on archirich.xml and index.html.
  * <p>
  * Output is the report directory, containing:<ul>
  * 		<li>copy of contents of the style directory</li>
  * 		<li>archirich.xml : it is the model file, but each document tags are enriched with HTMLReportExporter's paqrseCharsAndLinks.</li>
  * 		<li>diagrams in png: all diagrams are saved to ID.png, where ID is the id of the diagram</li>
  * 		<li>index.html: the result of applying style.xslt to archirich.xml</li>
  * 
  * @author Árpád Magosányi
  */
 public class RichExport implements IModelExporter {
 	
 
     public RichExport() {
     	
     }
 
     @Override
     public void export(IArchimateModel model){
         	File target = Widgets.askSaveFile(IPreferenceConstants.LAST_RICH_PATH, new String[] { "*.xml" } );
         	if(null == target) {
         		return;
         	}
         	export(model,target);
     }
     public static void export(IArchimateModel model, File target) {
             try {
           	ArchimateResource resource = (ArchimateResource) ArchimateResourceFactory.createResource(target);
         	resource.getContents().add(model);
         	// we get it in xml
         	Document xml = resource.save(null,resource.getDefaultSaveOptions(),null);
         	resource.getContents().remove(model);
         	//enrich the xml
         	NodeList nl = xml.getElementsByTagName("documentation");
         	for(int i=0;i<nl.getLength();i++) {
         		Element n = (Element) nl.item(i);
         		parseCharsAndLinks(n);
         	}
         	NodeList pl = xml.getElementsByTagName("purpose");
         	for(int j=0;j<pl.getLength();j++) {
         		Element k = (Element) pl.item(j);
         		parseCharsAndLinks(k);
         	}
         	enrichXML(xml,xml);
         	
         	//save the xml
         	DOMConfiguration docConfig = xml.getDomConfig();
         	docConfig.setParameter("well-formed", true);
         	xml.normalizeDocument();
             DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
             DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
             LSSerializer writer = impl.createLSSerializer();
             String str = writer.writeToString(xml);
             String enc = xml.getXmlEncoding();
             if (enc == null) {
             	enc="utf-16";
             }
             Charset cs = Charset.forName(enc);
             OutputStream os = new FileOutputStream(target);
             OutputStreamWriter fw = new OutputStreamWriter(os,cs);
             fw.write(str);
             fw.close();
         } catch(Exception e) {
         	Widgets.tellProblem("Problem Exporting Model", e.toString());
         	e.printStackTrace();
         }
     }
 
     private static void enrichXML(Document doc, Node n) {
     	NodeList nl = n.getChildNodes();
     	int ll=nl.getLength();
     	for(int i=0;i<ll;i++) {
     		Node m = nl.item(i);
            	if (Node.ELEMENT_NODE == m.getNodeType()) {
            		Element e = (Element)m;
            	    enrichXML(doc, e);
            	    System.out.println("node="+e+e.getAttribute("name"));
         		enrichElement(doc,(Element) e);    		
            	}
     	}
     }
     
     private static void enrichElement(Document xml,Element m){
     	// copies element node to an identical node which have nodename of the xsi:type attribute
     	String typename = m.getAttribute("xsi:type");
     	if("" != typename ) {
     		xml.renameNode(m, namespaceForType(typename), typename);
     		m.removeAttribute("xsi:type");
     	}
     	
     	List<Element> props = getChildElementsByTagName(m,"property");
     	System.out.println("property children="+ props);
     	int l = props.size();
     	for(int i=0;i<l;i++) {
     		Element p = (Element) props.get(i);
     		if ( m != p.getParentNode()) {
     			continue;
     		}
     		String key = p.getAttribute("key");
     		String value = p.getAttribute("value");
     		System.out.println("property("+key+")="+value);
    		if(key.equals("objectClass")) {
    			System.out.println("creating "+value);
    			Element e = getOrCreateElement(xml, m,value);
    			e.setAttribute("parentid", m.getAttribute("id"));
    			
     		}
     		if(key.contains(":")) {
     			String[] k = key.split(":",2);
     			createSubElement(xml, m,k[0],k[1], value);
     		}
     	}
     }
 
     private static List<Element> getChildElementsByTagName(Element e, String name) {
     	NodeList nl = e.getChildNodes();
     	int l = nl.getLength();
     	List<Element> out = new ArrayList<Element>();
     	for(int i=0;i<l;i++) {
     		Node c = nl.item(i);
            	if (Node.ELEMENT_NODE == c.getNodeType()) {
            		System.out.println("childnode="+c);
            		if(c.getNodeName().equals(name)) {
            			out.add((Element) c);
            		}
            	}    		
     	}
     	return out;
     }
     private static void createSubElement(Document doc, Element m, String el, String propname, String value) {
     	Element obj = getOrCreateElement(doc,m,el);
    	obj.setAttribute("parentid", m.getAttribute("id"));
     	Element prop = getOrCreateElement(doc,obj,propname);
     	prop.setTextContent(value);
     }
     private static Element getOrCreateElement(Document doc, Element m, String value) {
     	List<Element> nl = getChildElementsByTagName(m,value);
    	System.out.println("found "+nl.size()+" "+value+" children");
     	if(0 == nl.size()) {
        	System.out.println("creating it");
     		Element e = doc.createElement(value);
     		m.appendChild(e);
     		return e;
     	}
     	if(1 == nl.size()) {
        	System.out.println("getting it");
     		return nl.get(0);
         }
     	Widgets.tellProblem("property problem", "objectClass name '"+value+"' is reserved");
     	return null;
     }
     
     private static String namespaceForType(String tname) {
     	String xmlns = tname.split(":")[0];
     	if(xmlns.equals("archimate")) {
     		return "http://www.bolton.ac.uk/archimate";
     	}
     	return "http://namespaces.local/"+xmlns;
     }
     private static void parseCharsAndLinks(Element n) {
         // Escape chars
     	Document d = n.getOwnerDocument();
     	String s = n.getTextContent();
     	n.setTextContent("");
         
         String[] ss = s.split("(\r\n|\r|\n)");
         
         for(String sss : ss) {
         	parseLinks(sss,n);
         	n.appendChild(d.createElement("br"));
         }
         
     }
     
     private static void parseLinks(String s, Node parent) {
        	Matcher matcher = HTMLUtils.HTML_LINK_PATTERN.matcher(s);
     	Document d = parent.getOwnerDocument();
     	
         int lastend=0;
         while(matcher.find(lastend)) {
             String group = matcher.group();
             String text = s.substring(lastend,matcher.start());
 
             Node txt = d.createTextNode(text);
             lastend=matcher.end();
             parent.appendChild(txt);
             Element a = d.createElement("a");
             a.setAttribute("href", group);
             Node sub = d.createTextNode(group);
             a.appendChild(sub);
             parent.appendChild(a);
         }
         if(lastend < s.length()){
         	Node txt = d.createTextNode(s.substring(lastend));
         	parent.appendChild(txt);
         }
     }
 }
