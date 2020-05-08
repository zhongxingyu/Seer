 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.restconfig;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.restlet.data.MediaType;
 import org.restlet.resource.OutputRepresentation;
 import org.restlet.resource.Representation;
 
 public class AutoXMLFormat implements DataFormat {
     String myRootName;
 
     public AutoXMLFormat(){
         this("root");
     }
 
     public AutoXMLFormat(String s){
         myRootName = s;
     }
 
     public Representation makeRepresentation(Map map) {
         map.remove("page");
         Element root = new Element(myRootName);
         final Document doc = new Document(root);
         insert(root, map);
         return new OutputRepresentation(MediaType.APPLICATION_XML){
             public void write(OutputStream outputStream){
                 try{
                     XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                     outputter.output(doc, outputStream);
                 } catch(IOException ioe){
                     ioe.printStackTrace();
                 }
             }
         };
     }
 
     public void insert(Element elem, Object o){
         if (o instanceof Map){
             Iterator it = ((Map)o).entrySet().iterator();
             while (it.hasNext()){
                 Map.Entry entry = (Map.Entry)it.next();
                 Element newElem = new Element(entry.getKey().toString());
                 insert(newElem, entry.getValue());
                 elem.addContent(newElem);
             }
         } else if (o instanceof Collection) {
             Iterator it = ((Collection)o).iterator();
             while (it.hasNext()){
                 Element newElem = new Element("entry");
                 Object entry = it.next();
                 insert(newElem, entry);
                 elem.addContent(newElem); 
             }
         } else {
             elem.addContent(o == null ? "" : o.toString());
         }        
     }
 
     public Map readRepresentation(Representation rep) {
         Map m;
         try {
             SAXBuilder builder = new SAXBuilder();
             Document doc = builder.build(rep.getStream());
             Element elem = doc.getRootElement();
             m = (Map)convert(elem);
         } catch (Exception e){
             m = new HashMap();
         }
         return m;
     }
 
     private Object convert(Element elem){
         List children = elem.getChildren();
         if (children.size() == 0){
            if (elem.getContent().size() == 0){
                 return null;
             } else {
                 return elem.getText();
             }
         } else if (children.get(0) instanceof Element){
             Element child = (Element)children.get(0);
             if (child.getName().equals("entry")){
                 List l = new ArrayList();
                 Iterator it = elem.getChildren("entry").iterator();
                 while(it.hasNext()){
                     Element curr = (Element)it.next();
                     l.add(convert(curr));
                 }
                 return l;
             } else {
                 Map m = new HashMap();
                 Iterator it = children.iterator();
                 while (it.hasNext()){
                     Element curr = (Element)it.next();
                     m.put(curr.getName(), convert(curr));
                 }
                 return m;
             }
         }
         throw new RuntimeException("Hm, there was a problem parsing XML");
     }
 }
