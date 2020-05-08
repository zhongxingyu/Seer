 package cx.ath.jbzdak.jpaGui.ui.table;
 
 import cx.ath.jbzdak.common.famfamicons.IconManager;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;

import java.awt.Component;
import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class AkcjeRendererEditor<T> extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
 
 	private static final long serialVersionUID = 1L;
 
 	private final Class<T> clazz;
 
 	private final EditableTableModel<T> model;
 
 	private T value;
 
 	private JButton saveButton, deleteButton, refreshButton;
 
 	private final HighlightCellRenderer renderer = new HighlightCellRenderer(){
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		protected void getTableCellRendererEntry(JTable table, Object value,
 				boolean isSelected, boolean hasFocus, int row, int column) {
 			//Nothig to do
 
 		}
 
 		@Override
 		protected boolean isHighlighted(JTable table, Object value,
 				boolean isSelected, boolean hasFocus, int row, int column) {
 			return model.isHighlighted(row);
 		}
 
 	};
 
 	public AkcjeRendererEditor(Class<T> clazz, EditableTableModel<T> model) {
 		super();
 		this.clazz = clazz;
 		this.model = model;
 		renderer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
 		saveButton = prepareButton("cart_add", "Zapisz zmiany");
 		refreshButton = prepareButton("arrow_undo", "Cofnij zmiany");
 		deleteButton = prepareButton("cart_delete", "Usuń");
 		refreshButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("refreshButton.actionPerformed()");
 				AkcjeRendererEditor.this.model.refresh(value);
 				fireEditingStopped();
 			}
 		});
 		deleteButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("deleteButton.actionPerformed()");
 				AkcjeRendererEditor.this.model.remove(value);
 				fireEditingStopped();
 			}
 		});
 		saveButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("saveButton.actionPerformed()");
 				AkcjeRendererEditor.this.model.save(value);
 				fireEditingStopped();
 			}
 		});
 		renderer.add(saveButton);
 		renderer.add(deleteButton);
 		renderer.add(refreshButton);
 	}
 
 	private JButton prepareButton(String iconName, String tooltipText){
 		JButton result = new JButton(IconManager.getIconSafe(iconName));
 		result.setToolTipText(tooltipText);
 		result.setContentAreaFilled(false);
 		result.setBorderPainted(false);
 		result.setBorder(BorderFactory.createEmptyBorder());
 		result.putClientProperty("JComponent.sizeVariant", "tiny");
 		return result;
 	}
 
 	private void setupButtons(int row){
 		refreshButton.setEnabled(model.mayRefresh(row));
 		deleteButton.setEnabled(model.mayDelete(row));
 		saveButton.setEnabled(model.maySave(row));
 	}
 
 
 	@Override
 	public Component getTableCellRendererComponent(JTable table,
 			Object value, boolean isSelected, boolean hasFocus, int row,
 			int column) {
 		setValue(clazz.cast(value));
 		setupButtons(row);
 		return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 	}
 
 	@Override
 	public Component getTableCellEditorComponent(JTable table,
 			Object value, boolean isSelected, int row, int column) {
 		setValue(clazz.cast(value));
 		setupButtons(row);
 		return renderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
 	}
 
	@Override
 	public Object getCellEditorValue() {
 		return value;
 	}
 
 	public T getValue() {
 		return value;
 	}
 
 	public void setValue(T value) {
 		this.value = value;
 	}
 
 
 }
