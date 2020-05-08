 package chameleon.core.tag;
 
 import chameleon.core.element.Element;
 
 public abstract class TagImpl implements Tag {
 
 	private Element _element;
 	
   public final Element getElement() {
   	return _element;
   }
   
   public void setElement(Element element, String name) {
   	if(element != _element) {
   		if((_element != null) && (_element.tag(name) == this)){
   			_element.removeTag(name);
   		}
  		_element = element;
   		if((_element != null) && (_element.tag(name) != this)) {
   		  _element.setTag(this, name);
   		}
   	}
   }
 }
