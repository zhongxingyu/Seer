 package fi.cosky.sdk;
 import java.util.List;
 
 /*
  * This file is subject to the terms and conditions defined in
  * file 'LICENSE.txt', which is part of this source code package.
  */
 
 public class BaseData {
     private List<Link> Meta;
     private Link self;
    
     public Link getLink(String key) {
         if (self == null) {
             findSelf();
         }
 
         if (key.equals("self")) return self;
 
         for (Link l : Meta) {
             if (l.getRel().equals(key)) {
                 return concatLink(self, l);
             }
         }
         return null;
     }
 
     private Link concatLink(Link self, Link rel) {
     	String newSelf = self.getUri();
     	String newRel = rel.getUri();
     	
     	if (newRel.contains("../")) {
       		while (newRel.contains("../")) {
         		newSelf = newSelf.substring(0, newSelf.lastIndexOf('/'));
         		newRel = newRel.substring(newRel.indexOf("/")+1);
         	}
      		if (newRel.length() < 1) {
        		return new Link(rel.getRel(), newSelf, rel.getMethod(), rel.isEnabled());
        	} else {
        		return new Link(rel.getRel(), newSelf+"/"+newRel , rel.getMethod(), rel.isEnabled());
        	}
     	}
    	return new Link(rel.getRel(), self.getUri() + rel.getUri(), rel.getMethod(), rel.isEnabled());
        
     }
         
     @Override
     public String toString() {
          return API.gson.toJson(this);
     }
 
     public void findSelf() {
         for (Link l : Meta) {
             if (l.getRel().equals("self")) {
                 self = l;
                 break;
             }
         }
     }
 
     public List<Link> getLinks() {
         return this.Meta;
     }
 
     public void setLinks(List<Link> links) {
     	this.Meta = links;
     }
 
 }
