 /**
  * 
  */
 package com.github.progval.openquote.sites;
 
 // Project specific
 import com.github.progval.openquote.SiteItem;
 
 // Parsing HTML
 import org.jsoup.nodes.Element;
 
 /**
  *
  * @author ProgVal
  */
 public class VdmItem extends SiteItem {
 	public VdmItem(Element baseElement) {
         Element content = (Element) baseElement.child(0);
    	this.id = baseElement.id().replaceAll("[^0-9]", "");
     	this.content = content.text();
    	this.content += id;
 	}
 	
 	public String toString() {
 		return this.content;
 	}
 }
