 package presentation.model;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.util.Observable;
 
import util.TextUtils;

 /**
  * Tracks the content of the search mask and updates the its view when needed.
  */
 public class TabSearchModel extends Observable {
 
 	private static final String DEFAULT_SEARCH_STRING = "Benutzernamen, Buchtitel oder Autor eingeben";
 	private String searchFieldText = DEFAULT_SEARCH_STRING;
 	private ModelController controller;
 
 	public TabSearchModel(ModelController controller) {
 		this.controller = controller;
 	}
 
 	/**
 	 * Sets the text to be displayed on the search field to the default
 	 * "please-type-here" value.
 	 */
 	public void resetSearchText() {
 		searchFieldText = DEFAULT_SEARCH_STRING;
 		setChanged();
 		notifyObservers(searchFieldText);
 	}
 
 	/**
 	 * Returns the text which should be displayd on the search field.
 	 * 
 	 * @return current text for the display
 	 */
 	public String getSearchText() {
 		return searchFieldText;
 	}
 
 	public void setSearchText(String newText, boolean shouldUpdate) {
 		searchFieldText = newText;
 		if (shouldUpdate) {
 			setChanged();
 			notifyObservers(searchFieldText);
 		}
 	}
 
 	public boolean isDefaultSearchText() {
 		return DEFAULT_SEARCH_STRING.equals(searchFieldText);
 	}
 
 	public Color getSearchFieldColor() {
 		if (isDefaultSearchText())
 			return Color.GRAY;
 		return Color.BLACK;
 	}
 	
 	public void setRequestFocus(){
 		setChanged();
 		//TODO:That is the worst hack on earth
 		notifyObservers("requestFocus");
 	}
 
	public void ForwardKeyEvent(KeyEvent e) {
		System.out.println("key will be forwarded");
		if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			controller.resultlist_model.delchar();
		else if (e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == KeyEvent.VK_ENTER)
			; //TODO: Uh, ugly old C code, bäääh
		else 
 			controller.resultlist_model.addchar(e.getKeyChar());
 	}
 }
