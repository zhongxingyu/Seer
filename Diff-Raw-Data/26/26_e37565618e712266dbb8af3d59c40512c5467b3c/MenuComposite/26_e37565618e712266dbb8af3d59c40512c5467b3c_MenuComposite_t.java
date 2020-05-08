 package com.visionarysoftwaresolutions.menu.composite;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import com.visionarysoftwaresolutions.menu.MenuIterator;
 import com.visionarysoftwaresolutions.util.NullChecker;
 
 public class MenuComposite extends MenuComponent {
 	private Map<String, MenuComponent> components;
 	
 	public MenuComposite(String name) {
 		super(name);
 		components = new LinkedHashMap<String, MenuComponent>();
 	}
 	
 	@Override
 	public void add(MenuComponent child) {
 		NullChecker.checkNull(child);
 		components.put(child.getText(), child);
 	}
 
 	@Override
 	public void remove(MenuComponent child) {
 		NullChecker.checkNull(child);
 		components.remove(child.getText());
 	}
 	
 	@Override
 	public boolean hasChildren() {
 		return !components.isEmpty();
 	}
 
 	@Override
 	public MenuComponent getChild(String text) {
 		return components.get(text);
 	}
 
 	@Override
 	public MenuIterator createIterator() {
 		return new MenuIterator(components.values());
 	}
 	
 	@Override
 	public String toString(){
 		StringBuilder result = new StringBuilder();
		result.append(getText());
 		result.append(" children - ");
 		MenuIterator it = createIterator();
 		while(it.hasNext()){
 			result.append(" ");
 			result.append(it.next().toString());
 			result.append(" ");
 		}
 		return result.toString();
 	}
 }
