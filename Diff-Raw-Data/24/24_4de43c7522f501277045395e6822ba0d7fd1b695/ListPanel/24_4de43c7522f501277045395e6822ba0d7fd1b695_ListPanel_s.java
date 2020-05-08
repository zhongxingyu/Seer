 package manager.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.border.Border;
 
 import Utils.Constants;
 import factory.FactoryData;
 
 public abstract class ListPanel<T extends FactoryData> extends OverlayPanel {
 	
 	protected ArrayList<T> itemList = new ArrayList<T>();
 	protected HashMap<T, ClickablePanel> panels = new HashMap<T, ClickablePanel>();
 	
 	protected Border generalPadding = Constants.MEDIUM_PADDING;
 	protected Border itemPadding = Constants.FIELD_PADDING;
 	protected int itemMargin = 5;
 	
 	protected int itemWidth = 300;
 	protected int itemHeight = 30;
 	
 	public void setGeneralPadding(Border generalPadding) {
 		this.generalPadding = generalPadding;
 	}
 	
 	public void setItemPadding(Border itemPadding) {
 		this.itemPadding = itemPadding;
 	}
 
 	public void setItemMargin(int itemMargin) {
 		this.itemMargin = itemMargin;
 	}
 	
 	public void setItemWidth(int itemWidth) {
 		this.itemWidth = itemWidth;
 	}
 
 	public void setItemHeight(int itemHeight) {
 		this.itemHeight = itemHeight;
 	}
 
 	public HashMap<T, ClickablePanel> getPanels() {
 		return panels;
 	}
 	
 	public abstract void parseList();
 	
 	public void updateList(ArrayList<T> newList) {
		itemList = newList;
		parseList();
		restoreColors();
 	}
 	
 	public void restoreColors() {
 		for(ClickablePanel panel : panels.values()) {
 			panel.restoreColor();
 		}
 	}
 }
