 package org.atlasapi.remotesite.channel4.epg;
 
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.Node;
 
 public class C4MediaContentElement extends Element {
 
     public C4MediaContentElement(Element element) {
         super(element);
     }
 
     public C4MediaContentElement(String name, String uri) {
         super(name, uri);
     }
 
     public C4MediaContentElement(String name) {
         super(name);
     }
 
     public String thumbnail() {
        return getMediaElementValue("thumbnail");
     }
 
    private String getMediaElementValue(String elementName) {
         Elements childElements = this.getChildElements(elementName, "http://search.yahoo.com/mrss/");
        if (childElements.size() == 0) {
             return null;
         }
        return childElements.get(0).getValue();
     }
 
     @Override
     public Node copy() {
         return new C4MediaContentElement(this);
     }
 
 }
