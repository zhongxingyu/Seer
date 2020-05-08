 package org.dyndns.schuschu.xmms2client.view.element;
 
 import java.util.Vector;
 
import javax.swing.text.AbstractDocument.Content;

 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceBackend;
 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceViewElement;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class FooTable implements FooInterfaceViewElement {
 
 	private Table table;
 	private FooInterfaceBackend backend;
 	private int[] highlight = new int[0];
 
 	public FooTable(Composite parent, int style) {
 		table = new Table(parent, style);
 	}
 
 	@Override
 	public void addKeyListener(KeyListener key) {
 		table.addKeyListener(key);
 	}
 
 	@Override
 	public void addMouseListener(MouseListener mouse) {
 		table.addMouseListener(mouse);
 	}
 
 	@Override
 	public FooInterfaceBackend getBackend() {
 		return backend;
 	}
 
 	@Override
 	public int[] getIndices() {
 		return table.getSelectionIndices();
 	}
 
 	@Override
 	public Control getReal() {
 		return table;
 	}
 
 	@Override
 	public void removeKeyListener(KeyListener key) {
 		table.removeKeyListener(key);
 
 	}
 
 	@Override
 	public void removeMouseListener(MouseListener mouse) {
 		table.removeMouseListener(mouse);
 
 	}
 
 	@Override
 	public void setBackend(FooInterfaceBackend backend) {
 		this.backend = backend;
 
 	}
 
 	@Override
 	public void setContent(Vector<String> content) {
 
 		table.removeAll();
 		// TODO: adapt for multiple columns via constructor etc
 		TableColumn column;
 		if (table.getColumnCount() == 0) {
 			new TableColumn(table, SWT.NONE);
 		}
 		column = table.getColumn(0);
 
 		// column.setWidth(table.getSize().x);
 
 		for (String s : content) {
 			TableItem item = new TableItem(table, SWT.NONE);
 			item.setText(s);
 		}
 		column.pack();
 	}
 
 	@Override
 	public void setSelection(int[] indices) {
 		table.setSelection(indices);
 
 	}
 
 	@Override
 	public void setSingleSelectionMode() {
 		// TODO find out if swt can do this
 
 	}
 
 	@Override
 	public void setLayoutData(Object layoutData) {
 		table.setLayoutData(layoutData);
 	}
 
 	@Override
 	public void highlight(int[] indicies) {
 
 		final Color hlcolor = table.getDisplay().getSystemColor(SWT.COLOR_RED);
 		final Color defcolor = table.getDisplay().getSystemColor(
 				SWT.COLOR_LIST_BACKGROUND);
 
 		if (table.getItemCount() != 0) {
 
 			if (indicies != null) {
 				for (int id : highlight) {
 					if (id >= 0 && id < table.getItemCount()) {
 						table.getItem(id).setBackground(defcolor);
 					}
 				}
 				highlight = indicies;
 			}
 
 			for (int id : highlight) {
 				if (id >= 0 && id < table.getItemCount()) {
 					table.getItem(id).setBackground(hlcolor);
 				}
 			}
 		}
 	}
 }
