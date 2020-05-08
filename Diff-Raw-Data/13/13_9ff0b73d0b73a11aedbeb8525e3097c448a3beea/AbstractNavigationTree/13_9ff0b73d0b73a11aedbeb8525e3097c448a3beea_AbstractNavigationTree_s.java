 package com.argility.centralpages.ui.nav;
 
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Tree;
 
 @SuppressWarnings("serial")
 public abstract class AbstractNavigationTree extends Tree implements ItemClickListener{
 	
 	public AbstractNavigationTree() {
 		
 		addListener((ItemClickListener)this);
 	}
 	
 	public Panel asPanel(String caption) {
 		Panel p = new Panel(caption);
 		p.addComponent(this);
 		return p;
 	}
 }
