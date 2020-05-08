 package chameleon.core.tag;
 
 import chameleon.core.element.Element;
 
 public abstract class TagImpl implements Tag {
 
 	private Element _element;
 	
   public final Element getElement() {
   	return _element;
   }
   
   public void setElement(Element element, String name) {
   	if(element != _element) {
  		// Set new pointer, backup old for removal.
  		Element old = _element;
  		_element = element;
  		// Remove from current element.
   		if((_element != null) && (_element.tag(name) == this)){
   			_element.removeTag(name);
   		}
  		// Add to new element.
   		if((_element != null) && (_element.tag(name) != this)) {
   		  _element.setTag(this, name);
   		}
   	}
   }
 }
