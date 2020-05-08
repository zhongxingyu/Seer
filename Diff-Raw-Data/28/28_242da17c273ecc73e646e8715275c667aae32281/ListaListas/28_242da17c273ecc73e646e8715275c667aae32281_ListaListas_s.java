 package gui;
 
 import java.util.Vector;
 
 import net.rim.device.api.collection.ReadableList;
 import net.rim.device.api.collection.util.UnsortedReadableList;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.component.KeywordFilterField;
 import net.rim.device.api.ui.component.KeywordProvider;
 import net.rim.device.api.ui.component.ListField;
 import net.rim.device.api.ui.component.ListFieldCallback;
 
 abstract class ListaListas extends KeywordFilterField {
 	private Unsorted _u;
 
 	public ListaListas() {
 		super();
 		setCallback(new Callback());
 		_u = new Unsorted();
 		setLabel("Buscar: ");
 	}
 	
 	public void setSelectedIndex(int index) {
 		super.setSelectedIndex(index);
 	}
 	
 	public void loadFrom(Vector vector) {
 		_u.loadFrom(vector.elements());
 	}
 
 	public void setSourceList(KeywordProvider helper) {
 		setSourceList(_u, helper);
 	}
 
 	public void insert(int index, Object element) {
 		_u.insert(index, element);
 		updateList();
 	}
 
 	public void insert(Object element) {
 		_u.insert(element);
 		updateList();
 	}
 
 	public void update(Object old, Object nw) {
		setText("");
		_u.update(old, nw);
		updateList();
 	}
 
 	public void setText(String text) {
 		getKeywordField().setText(text);
 		invalidate();
 	}
 
 	public int getIndex(Object element) {
 		return _u.getIndex(element);
 	}
 
 	public void remove(int index) {
		_u.delete(index);
 	}
 
 	public void remove(Object element) {
		_u.delete(element);
 	}
 }
 
 class Unsorted extends UnsortedReadableList {
 	public void insert(int index, Object element) {
 		insertAt(index, element);
 	}
 
 	public void insert(Object element) {
 		doAdd(element);
 	}
 
 	public void delete(int index) {
 		doRemove(getAt(index));
 	}
 
 	public void delete(Object element) {
 		doRemove(element);
 	}
 
 	public void update(Object old, Object nw) {
 		doUpdate(old, nw);
 	}
 }
 
 class Callback implements ListFieldCallback {
 
 	public void drawListRow(ListField listField, Graphics graphics, int index,
 			int y, int width) {
 		ReadableList r = ((ListaListas)listField).getResultList();
 		if(String.class.isInstance(r.getAt(index))) {
 			graphics.setFont(graphics.getFont().derive(Font.BOLD));
 			int color = graphics.getColor();
 			graphics.setColor(Color.LIGHTGRAY);
 			graphics.drawLine(5, listField.getRowHeight() + y - 1, listField.getWidth() - 5, listField.getRowHeight() + y - 1);
 			graphics.setColor(color);
 		}
 		graphics.drawText(r.getAt(index).toString(), 0, y);
 	}
 
 	public Object get(ListField listField, int index) {
 		return null;
 	}
 
 	public int getPreferredWidth(ListField listField) {
 		return 0;
 	}
 
 	public int indexOfList(ListField listField, String prefix, int start) {
 		return 0;
 	}
 	
 }
