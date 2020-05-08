 package com.visionarysoftwaresolutions.menu;
 
 import com.visionarysoftwaresolutions.menu.composite.MenuComponent;
 
 
 public abstract class MenuComponentDecorator extends MenuComponent {
 	private MenuComponent component;
 	
 	public MenuComponentDecorator(MenuComponent toDecorate){
 		super(toDecorate.getText());
 		component = toDecorate;
 	}
 	
 	@Override
 	public void add(MenuComponent toAdd){
 		component.add(toAdd);
 	}
 	
 	@Override
 	public void remove(MenuComponent toRemove){
 		component.remove(toRemove);
 	}
 	
 	@Override
 	public boolean hasChildren(){
 		return component.hasChildren();
 	}
 	
 	@Override
 	public MenuComponent getChild(String text){
 		return component.getChild(text);
 	}
 	
 	@Override
 	public MenuIterator createIterator(){
 		return component.createIterator();
 	}
	
	@Override
	public String toString(){
		return component.toString();
	}
 }
