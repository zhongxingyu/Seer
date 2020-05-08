 package com.rsx.impl;
 
 import com.rsx.Element;
 import com.rsx.Value;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ElementImpl implements Element {
 
     private org.w3c.dom.Element element;
 
     public ElementImpl(org.w3c.dom.Element element) {
     this.element = element;
     }
 
     @Override
     public Element element(String elementName) {
         NodeList elements = this.element.getElementsByTagName(elementName);
         org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(0);
 
         if (element == null) {
             return null;
         }
 
         return new ElementImpl(element);
     }
 
     @Override
     public List<Element> elements(String elementName) {
         List<Element> elements = new ArrayList<Element>();
 
         NodeList nodes = element.getElementsByTagName(elementName);
         for (int index = 0; index < nodes.getLength(); index++) {
            elements.add(new ElementImpl((org.w3c.dom.Element) nodes.item(index)));
         }
 
         return elements;
     }
 
     @Override
     public List<Element> elements() {
         List<Element> elements = new ArrayList<Element>();
 
         NodeList nodes = element.getChildNodes();
         for (int index = 0; index < nodes.getLength(); index++) {
             Node node = nodes.item(index);
             if (node instanceof org.w3c.dom.Element) {
                 elements.add(new ElementImpl((org.w3c.dom.Element) node));
             }
         }
 
         return elements;
     }
 
     @Override
     public String name() {
         return element.getNodeName();
     }
 
     @Override
     public Value value() {
         return new ValueImpl(element.getTextContent());
     }
 
     @Override
     public Value attribute(String attributeName) {
         String attribute = element.getAttribute(attributeName);
 
         if (!element.hasAttribute(attributeName)) {
             return null;
         }
 
         return new ValueImpl(attribute);
     }
 }
