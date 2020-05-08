 package br.uff.midiacom.xml;
 
 import br.uff.midiacom.xml.datatype.string.StringType;
 
 
 /**
  * This class implements methods of the NCLElement interface.
  */
 public class XMLElementImpl<T extends XMLIdentifiableElement, P extends XMLElement> {
 
     protected StringType id;
     protected P parent;
 
 
     public void setId(String id) throws XMLException {
         if(!validate(id))
            throw new XMLException("Invalid identifier");
 
         this.id = new StringType(id);
     }
 
 
     public String getId() {
         if(id != null)
             return id.getValue();
         else
             return null;
     }
 
 
     public boolean setParent(P parent) {
         if(this.parent != null && parent != null)
             return false;
 
         this.parent = parent;
         return true;
     }
 
 
     public P getParent() {
         return parent;
     }
 
 
     public boolean compare(T other) {
         return id.getValue().equals(other.getId());
     }
 
 
     protected boolean validate(String id) {
         return true;
     }
 }
