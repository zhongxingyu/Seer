 package topshelf.gwt.common.client;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.Composite;
 
 /**
  * Simply a subset of {@link ComplexPanel}'s functionality
  * that supports specifically {@link ListItem}s as children.
  * 
  * @author bloo
  *
  */
 public abstract class AbstractBaseList extends Composite {
 	
 	boolean inline = false;
 	
 	private class List extends ComplexPanel {
 		List(Element listElement) {
 			setElement(listElement);
 		}
 		void add(ListItem li) {
 			super.add(li, getElement());
 		}
 		void insert(ListItem li, int beforeIndex) {
 			super.insert(li, getElement(), beforeIndex, false);
 		}
 		boolean remove(ListItem li) {
 			return super.remove(li);
 		}
 		ListItem getLast() {
 			int cnt = super.getWidgetCount();
			return 0 == cnt ? null : getItem(cnt-1);
 		}
 		ListItem getFirst() {
			return 0 == super.getWidgetCount() ? null : getItem(0);
 		}
 		ListItem getItem(int index) {
 			return (ListItem)super.getWidget(index);
 		}
 	}
 	
 	private List backing;
 	
 	protected AbstractBaseList(Element listElement) {
 		backing = new List(listElement);
 		initWidget(backing);
 	}
 	
 	public void setInline(boolean inline) {
 		this.inline = inline;
 	}
 	
 	public void add(ListItem li) {
 		configListItem(li);
 		backing.add(li);
 	}
 	
 	public void insert(ListItem li, int beforeIndex) {
 		configListItem(li);
 		backing.insert(li, beforeIndex);
 	}
 	
 	public boolean remove(ListItem li) {
 		return backing.remove(li);
 	}
 	
 	public boolean remove(int index) {
 		return backing.remove(index);
 	}
 	
 	public void clear() {
 		backing.clear();
 	}
 	
 	public ListItem getFirst() {
 		return backing.getFirst();
 	}
 	
 	public ListItem getLast() {
 		return backing.getLast();
 	}
 	
 	public ListItem getItem(int index) {
 		return backing.getItem(index);
 	}
 	
 	private void configListItem(ListItem li) {
 		if (inline) li.getElement().getStyle().setDisplay(Display.INLINE);
 	}
 }
