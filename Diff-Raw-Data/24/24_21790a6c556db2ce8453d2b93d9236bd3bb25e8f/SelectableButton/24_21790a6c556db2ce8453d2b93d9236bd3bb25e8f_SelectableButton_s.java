 package com.arenz.spriteeditor.ui.components;
 
 import javax.swing.JButton;
 
 public class SelectableButton extends JButton {
 	
 	public SelectableButton() {
 		super();
 	}
 	
 	public SelectableButton(String name) {
 		super(name);
 	}
 	
 	public void selectButton(boolean selected) {
		setEnabled(selected);
 	}
 }
