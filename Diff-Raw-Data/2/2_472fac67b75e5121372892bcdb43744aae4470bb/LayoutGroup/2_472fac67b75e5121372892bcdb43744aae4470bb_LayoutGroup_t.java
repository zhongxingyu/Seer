 package org.glom.web.shared.libglom.layout;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class LayoutGroup extends LayoutItem {
 
 	private static final long serialVersionUID = 2795852472980010553L;
 	private int columnCount = 0;
 
 	/**
 	 * @param columnCount the columnCount to set
 	 */
 	public void setColumnCount(int columnCount) {
 		this.columnCount = columnCount;
 	}
 
	static protected class LayoutItemList extends ArrayList<LayoutItem> {
 		private static final long serialVersionUID = 8610424318876440333L;
 	};
 
 	private LayoutItemList items = new LayoutItemList();
 
 	/**
 	 * @return
 	 */
 	public List<LayoutItem> getItems() {
 		return items;
 	}
 
 	/**
 	 * @param layoutItemField
 	 */
 	public void addItem(final LayoutItem layoutItem) {
 		items.add(layoutItem);
 	}
 
 	/**
 	 * @return
 	 */
 	public int getColumnCount() {
 		return columnCount;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getExpectedResultSize() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getPrimaryKeyIndex() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
 	 * @param expectedResultSize
 	 */
 	public void setExpectedResultSize(final int expectedResultSize) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @param b
 	 */
 	public void setHiddenPrimaryKey(final boolean b) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @param i
 	 */
 	public void setPrimaryKeyIndex(final int i) {
 		// TODO Auto-generated method stub
 
 	}
 }
