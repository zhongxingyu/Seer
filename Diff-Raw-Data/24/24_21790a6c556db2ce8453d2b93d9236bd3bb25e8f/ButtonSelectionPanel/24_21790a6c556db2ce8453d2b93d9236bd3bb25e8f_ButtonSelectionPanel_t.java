 package com.arenz.spriteeditor.ui.components;
 
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 public class ButtonSelectionPanel extends JPanel {
 	private ArrayList<SelectableButton> buttons = new ArrayList<SelectableButton>();
 	private SelectableButton selectedButton;
 	
 	public void selectButton(SelectableButton button) {
 		deselectSelectedButton();
 		selectedButton = button;
 		selectedButton.selectButton(true);
		revalidate();
 	}
 
 	private void deselectSelectedButton() {
 		if (selectedButton != null) {
 			selectedButton.selectButton(false);
			selectedButton = null;
 		}
 	}
 	
 	public void addButton(SelectableButton button) {
 		buttons.add(button);
 		this.add(button);
 	}
 
 	public void removeButton(SelectableButton button) {
 		buttons.remove(button);
 		this.remove(button);
 	}
 
 	public void removeAllButtons() {
 		buttons.clear();
 		this.removeAll();
 	}
 }
