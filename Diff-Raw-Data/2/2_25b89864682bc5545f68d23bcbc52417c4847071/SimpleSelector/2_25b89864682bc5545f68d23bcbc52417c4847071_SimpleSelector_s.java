 package html2windows.css.level1;
 
 import html2windows.dom.Element;
 import html2windows.dom.Document;
 
 abstract class SimpleSelector extends SelectorAdapter{
 	public boolean match(Element element){
 		if (realMatch(element)) {
			if (prev() != null && prev().match(element)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected abstract boolean realMatch(Element element);
 }
