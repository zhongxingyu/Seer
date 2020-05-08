 package html2windows.css.level1;
 
 import java.util.ArrayList;
 
 import html2windows.dom.Element;
 import html2windows.dom.Document;
 
public class CSS1SelectorMatcher implements SelectorMatcher{
 	CSS1SelectorCompiler compiler = new CSS1SelectorCompiler();
 	
 	public ArrayList<Element> getElementBySelector(String selectorGroupText, Document document){
 		ArrayList<Element> result = new ArrayList<Element>();
 		
 		// Split group by comma symbol and spaces
 		String[] selectorTexts = selectorGroupText.split("\\s*,\\s*");
 		for(String selectorText : selectorTexts){
			Selector[] selector = compiler.compile(selectorText);
			Element[] elements = selector.select();
			result.addAll(Array.toList(elements));
 		}
 		
 		return result;
 	}
 	
 	private ArrayList<Element> select(Selector selector, Document document){
 		ArrayList<Element> result = new ArrayList<Element>();
 		
		selectTraverse(selector, document.documentElement, result);
 		
 		return result;
 	}
 	
 	private void selectTraverse(Selector selector, Element parent, ArrayList<Element> result){
 		if (selector.match(parent))
 			result.add(parent);
 		
 		for(Node child : parent.childNodes()){
 			if (child instanceof Element){
 				selectTraverse(selector, (Element)child, result);
 			}
 		}
 	}
 }
