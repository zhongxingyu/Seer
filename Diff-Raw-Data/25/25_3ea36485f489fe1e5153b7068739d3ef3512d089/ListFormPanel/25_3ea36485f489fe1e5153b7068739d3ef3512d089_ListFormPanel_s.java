 package org.gwtapp.ccalc.client.ui.form;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gwtapp.ccalc.client.ElementCallbackAdapter;
 import org.gwtapp.core.rpc.data.ModelData;
 import org.gwtapp.template.client.ui.TemplateFormPanel;
 import org.gwtapp.template.client.ui.TemplatePanel;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ListFormPanel<T extends ModelData> extends
 		TemplateFormPanel<List<T>> {
 
 	public static interface RowCallback<T> {
 		Widget createHeaderFormPanel();
 
 		TemplateFormPanel<? extends T> createRowFormPanel(int row);
 	}
 
 	private List<TemplateFormPanel<? extends T>> rows = new ArrayList<TemplateFormPanel<? extends T>>();
 	private final RowCallback<T> rowCallback;
 
 	public ListFormPanel(RowCallback<T> rowCallback) {
 		this(new ElementCallbackAdapter(DOM.createDiv()), rowCallback);
 	}
 
 	public ListFormPanel(TemplatePanel.ElementCallback element,
 			RowCallback<T> rowCallback) {
 		super(element);
 		this.rowCallback = rowCallback;
 	}
 
 	public TemplateFormPanel<? extends T> getRow(int index) {
 		return rows.get(index);
 	}
 
 	@Override
 	public List<T> getValue() {
 		List<T> value = new ArrayList<T>();
 		for (TemplateFormPanel<? extends T> row : rows) {
 			value.add(row.getValue());
 		}
 		return value;
 	}
 
 	private final ValueChangeHandler<T> handler = new ValueChangeHandler<T>() {
 		@Override
 		public void onValueChange(ValueChangeEvent<T> event) {
 			ValueChangeEvent.fire(ListFormPanel.this, getValue());
 		}
 	};
 
 	@Override
 	public void setValue(final List<T> value, boolean fireEvents) {
 		clear();
 		rows.clear();
 		add(rowCallback.createHeaderFormPanel());
 		for (int row = 0; row < value.size(); row++) {
 			insertRow(row, value.get(row));
 		}
 		super.setValue(value, fireEvents);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void insertRow(int row, T item) {
 		TemplateFormPanel<? extends T> rowPanel = rowCallback
 				.createRowFormPanel(row);
 		((TemplateFormPanel<T>) rowPanel).addValueChangeHandler(handler);
 		((TemplateFormPanel<T>) rowPanel).setValue(item);
 		insert(rowPanel, row + 1);
 		rows.add(row, rowPanel);
 	}
 
 	public void insert(Widget w, int beforeIndex) {
 		if (beforeIndex < getWidgetCount()) {
 			insert(w, getElement(), beforeIndex, true);
 		} else {
 			add(w);
 		}
 	}
 
 	public void removeRow(int row) {
 		remove(row + 1);
 		rows.remove(row);
 	}
 
 	public int getRowIndex(Widget rowPanel) {
 		return Math.max(-1, getWidgetIndex(rowPanel) - 1);
 	}
 
 	public List<TemplateFormPanel<? extends T>> getRows() {
 		List<TemplateFormPanel<? extends T>> rows = new ArrayList<TemplateFormPanel<? extends T>>();
 		for (int i = 0; i < this.rows.size(); i++) {
 			rows.add(this.rows.get(i));
 		}
 		return rows;
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(
 			ValueChangeHandler<List<T>> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 }
