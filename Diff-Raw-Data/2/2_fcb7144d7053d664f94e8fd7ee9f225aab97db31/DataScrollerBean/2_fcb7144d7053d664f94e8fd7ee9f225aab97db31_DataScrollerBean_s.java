 package org.richfaces.demo.dataTableScroller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.model.SelectItem;
 
 public class DataScrollerBean {
 
 	private int rows = 10;
 	private int scrollerPage=1;
 	private int size;
 	
 	public int getRows() {
 		return rows;
 	}
 
 	public void setRows(int rows) {
 		this.rows = rows;
 	}
 	
 	public List<SelectItem> getPagesToScroll() {
 		List<SelectItem> list = new ArrayList<SelectItem>();
		for (int i = 1; i <= size / getRows()+1; i++) {
 			if (Math.abs(i - scrollerPage) < 5) {
 				SelectItem item = new SelectItem(i);
 				list.add(item);
 			}
 		}
 		return list;
 	}
 
 	public int getScrollerPage() {
 		return scrollerPage;
 	}
 
 	public void setScrollerPage(int scrollerPage) {
 		this.scrollerPage = scrollerPage;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 	
 }
