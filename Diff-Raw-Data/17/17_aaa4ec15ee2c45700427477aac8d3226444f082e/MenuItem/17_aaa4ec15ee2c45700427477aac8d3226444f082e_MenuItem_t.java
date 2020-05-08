 package org.adamk33n3r.karthas.gui;
 
 import java.awt.Point;
 
 import org.adamk33n3r.karthas.Executable;
 import org.adamk33n3r.karthas.Renderable;
 import org.newdawn.slick.Color;
 
 /**
  * A class that defines a menu item that has text and a function associated with it.
  * It is a child of a {@link Menu}
  * @author adamk33n3r
  * 
  */
 public class MenuItem implements Renderable, Executable {
 
 	String text = "default";
 	int position;
 	
 	private Color color, selectedColor;
 	private Color fontColor, selectedFontColor;
 	private Color disabledColor, borderColor;
 	
 	boolean selected;
 	private boolean disabled;
 
 	private MenuItemAction function = null;
 
 	protected Menu parentMenu = null;
 	
 	/**
 	 * If {@code text} is "Go Back", creates a {@code MenuItem} with a function to go back to the previous menu. Otherwise, makes a {@code MenuItem} with no action
 	 * @param text - The text to show in the {@code MenuItem}
 	 */
 	public MenuItem(String text) {
 		this.text = text;
		this.color = GUI.DEFAULT_MENU_COLOR;
		this.selectedColor = GUI.DEFAULT_SELECTED_MENU_COLOR;
		this.disabledColor = GUI.DEFAULT_DISABLED_COLOR;
		this.borderColor = GUI.DEFAULT_BORDER_COLOR;
		this.fontColor = GUI.DEFAULT_FONT_COLOR;
		this.selectedFontColor = GUI.DEFAULT_SELECTED_FONT_COLOR;
 		if (!text.equals("Go Back")) {
 			this.function = new MenuItemAction() {
 				@Override
 				public void execute() {
 				}
 			};
 		} else {
 			this.function = new MenuItemAction() {
 				@Override
 				public void execute() {
 					if(parentMenu != null)
 						GUI.changeTo(parentMenu.getParent());
 				}
 			};
 		}
 	}
 
 	/**
 	 * Creates a new {@code MenuItem}
 	 * @param text - The text to show in the {@code MenuItem}
 	 * @param function - The function that by pressing the return key on the {@code MenuItem} should be executed
 	 */
 	public MenuItem(String text, MenuItemAction function) {
 		this.text = text;
 		this.color = GUI.DEFAULT_MENU_COLOR;
 		this.selectedColor = GUI.DEFAULT_SELECTED_MENU_COLOR;
 		this.disabledColor = GUI.DEFAULT_DISABLED_COLOR;
 		this.borderColor = GUI.DEFAULT_BORDER_COLOR;
 		this.fontColor = GUI.DEFAULT_FONT_COLOR;
 		this.selectedFontColor = GUI.DEFAULT_SELECTED_FONT_COLOR;
 		this.function = function;
 	}
 	
 	/**
 	 * Creates a new {@code MenuItem}
 	 * @param text - The text to show in the {@code MenuItem}
 	 * @param function - The function that by pressing the return key on the {@code MenuItem} should be executed
 	 */
 	public MenuItem(String text, Color color, Color selectedColor, Color disabledColor, Color borderColor, MenuItemAction function) {
 		this.text = text;
 		this.color = color;
 		this.selectedColor = selectedColor;
 		this.disabledColor = disabledColor;
 		this.borderColor = borderColor;
 		this.fontColor = GUI.DEFAULT_FONT_COLOR;
 		this.selectedFontColor = GUI.DEFAULT_SELECTED_FONT_COLOR;
 		this.function = function;
 	}
 	
 	/**
 	 * Creates a new {@code MenuItem}
 	 * @param text - The text to show in the {@code MenuItem}
 	 * @param function - The function that by pressing the return key on the {@code MenuItem} should be executed
 	 */
 	public MenuItem(String text, Color color, Color selectedColor, Color disabledColor, Color borderColor, Color fontColor, Color selectedFontColor, MenuItemAction function) {
 		this.text = text;
 		this.color = color;
 		this.selectedColor = selectedColor;
 		this.disabledColor = disabledColor;
 		this.borderColor = borderColor;
 		this.fontColor = fontColor;
 		this.selectedFontColor = selectedFontColor;
 		this.function = function;
 	}
 	
 	/**
 	 * Sets the owner of this {@code MenuItem}
 	 * @param parent - The parent
 	 */
 	public void setParent(Menu parent) {
 		this.parentMenu = parent;
 	}
 	
 	public void enable() {
 		disabled = false;
 	}
 	
 	public void disable() {
 		disabled = true;
 	}
 	
 	/**
 	 * Renders the {@code MenuItem}
 	 */
 	@Override
 	public void render() {
 		int y = GUI.height - parentMenu.getHeight() - 40 + (position * 25);
 		if(disabled)
 			GUI.drawRect(50, y, 50 + parentMenu.getWidth(), y + 20, this.disabledColor, 5, this.borderColor);
 		else
 			GUI.drawRect(50, y, 50 + parentMenu.getWidth(), y + 20, this.color, 5, this.borderColor);
 		if(selected) {
 			GUI.drawPolygon(this.selectedColor, new Point(50, y), new Point(50 + parentMenu.getWidth() - 20, y), new Point(50 + parentMenu.getWidth(), y + 20), new Point(50 + 20, y + 20));
 			GUI.drawString(50 + 35, y - 6, text, this.selectedFontColor, GUI.font);
 		} else {
 			GUI.drawString(50 + 35, y - 6, text, this.fontColor, GUI.font);
 		}
 	}
 
 	/**
 	 * Executes the {@code MenuItemAction} associated with this {@code MenuItem}
 	 */
 	@Override
 	public void execute() {
 		function.execute();
 	}
 
 	@Override
 	public void update() {
 		
 	}
 
 }
