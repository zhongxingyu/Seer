 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui.component;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.persistence.PersistenceException;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.dao.AbstractDAO;
 import eu.lp0.cursus.db.data.AbstractEntity;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.DatabaseError;
 import eu.lp0.cursus.util.Messages;
 
 public class DatabaseTableModel<T extends AbstractEntity, O extends Frame & DatabaseWindow> extends AbstractTableModel {
 	private final Logger log = LoggerFactory.getLogger(getClass());
 	private O win;
 	private AbstractDAO<T> dao;
 	private List<Method> columnGetters = new ArrayList<Method>();
 	private List<Method> columnSetters = new ArrayList<Method>();
 	private ArrayList<T> rows = new ArrayList<T>();
 
 	public DatabaseTableModel(Class<T> clazz, O win, AbstractDAO<T> dao) {
 		this.win = win;
 		this.dao = dao;
 
 		SortedMap<Integer, Method> getters = new TreeMap<Integer, Method>();
 		SortedMap<Integer, Method> setters = new TreeMap<Integer, Method>();
 		for (Method m : clazz.getMethods()) {
 			TableModelColumn a = m.getAnnotation(TableModelColumn.class);
 			if (a != null) {
 				assert (m.getReturnType() != Void.class);
 				assert (m.getParameterTypes().length == 0);
 				getters.put(a.index(), m);
 
 				try {
 					Method setter = clazz.getMethod(m.getName().replaceFirst("^(is|get)", "set"), m.getReturnType()); //$NON-NLS-1$ //$NON-NLS-2$
 					setters.put(a.index(), setter);
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		columnGetters.addAll(getters.values());
 		columnSetters.addAll(setters.values());
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		return Messages.getString(columnGetters.get(column).getAnnotation(TableModelColumn.class).name());
 	}
 
 	@Override
 	public Class<?> getColumnClass(int columnIndex) {
 		return columnGetters.get(columnIndex).getReturnType();
 	}
 
 	@Override
 	public int getRowCount() {
 		return rows.size();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return columnGetters.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		try {
 			return columnGetters.get(columnIndex).invoke(rows.get(rowIndex));
 		} catch (Exception e) {
 			log.error(String.format("get row=%d, column=%d", rowIndex, columnIndex), e); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return true;
 	}
 
 	public boolean setEditedValueAt(int rowIndex, int columnIndex, Object newValue) {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		if (!isCellEditable(rowIndex, columnIndex)) {
 			return false;
 		}
 
 		Object oldValue = getValueAt(rowIndex, columnIndex);
 		if (oldValue == newValue || oldValue != null && oldValue.equals(newValue)) {
 			return true;
 		}
 
 		try {
 			columnSetters.get(columnIndex).invoke(rows.get(rowIndex), newValue);
 		} catch (Exception e) {
 			log.error(String.format("Unable to set row=%d, column=%d, oldValue=%s, newValue=%s", rowIndex, columnIndex, oldValue, newValue), e); //$NON-NLS-1$
 			return false;
 		}
 
 		boolean revert = false;
 		win.getDatabase().startSession();
 		try {
 			DatabaseSession.begin();
 
 			T item = dao.get(rows.get(rowIndex));
 			try {
 				columnSetters.get(columnIndex).invoke(item, newValue);
 			} catch (Exception e) {
 				revert = true;
 				log.error(String.format("Unable to update row=%d, column=%d, oldValue=%s, newValue=%s", rowIndex, columnIndex, oldValue, newValue), e); //$NON-NLS-1$
 				return false;
 			}
 			dao.persist(item);
 
 			DatabaseSession.commit();
 		} catch (PersistenceException e) {
 			revert = true;
 			log.error(String.format("Unable to save changes: row=%d, column=%d, oldValue=%s, newValue=%s", rowIndex, columnIndex, oldValue, newValue), e); //$NON-NLS-1$
 			DatabaseError.errorSaving(win, Constants.APP_NAME, e);
 			return false;
 		} finally {
 			if (revert) {
 				try {
 					columnSetters.get(columnIndex).invoke(rows.get(rowIndex), oldValue);
 					fireTableCellUpdated(rowIndex, columnIndex);
 				} catch (Exception e) {
 					log.error(String.format("Unable to revert row=%d, column=%d, oldValue=%s, newValue=%s", rowIndex, columnIndex, oldValue, newValue), e); //$NON-NLS-1$
 				}
 			}
 
 			win.getDatabase().endSession();
 		}
 
 		fireTableCellUpdated(rowIndex, columnIndex);
 		return true;
 	}
 
 	public void updateModel(List<T> newRows) {
 		for (int idxOld = 0; idxOld < rows.size(); idxOld++) {
 			int idxNew = newRows.indexOf(rows.get(idxOld));
 			if (idxNew != -1) {
 				rows.set(idxOld, newRows.get(idxNew));
 				fireTableRowsUpdated(idxOld, idxOld);
 				newRows.remove(idxNew);
 			} else {
 				rows.remove(idxOld);
 				fireTableRowsDeleted(idxOld, idxOld);
 				idxOld--;
 			}
 		}
 
 		for (T newRow : newRows) {
 			int idxNew = rows.size();
 			rows.add(newRow);
 			fireTableRowsInserted(idxNew, idxNew);
 		}
 
 		rows.trimToSize();
 	}
 
 	public void setupEditableModel(JTable table) {
 		table.setModel(this);
 
 		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(this);
 		sorter.setSortsOnUpdates(true);
 		table.setRowSorter(sorter);
 
 		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
 		while (cols.hasMoreElements()) {
 			TableColumn col = cols.nextElement();
 			col.setCellEditor(getCellEditor(col.getModelIndex()));
 		}
 	}
 
 	public TableCellEditor getCellEditor(int modelIndex) {
 		Class<?> type = getColumnClass(modelIndex);
 		if (type.isEnum()) {
			return new TableCellEditor(new JComboBox(type.getEnumConstants()));
 		} else if (type == String.class) {
			return new TableCellEditor(new JTextField());
 		} else if (type == boolean.class) {
			return new TableCellEditor(new JCheckBox());
 		} else {
 			return null;
 		}
 	}
 
	public class TableCellEditor extends DefaultCellEditor {
 		private int rowIndex = -1;
 		private int columnIndex = -1;
 
		public TableCellEditor(JCheckBox checkBox) {
 			super(checkBox);
 			checkBox.setBorder(null);
 		}
 
		public TableCellEditor(JComboBox comboBox) {
 			super(comboBox);
 			comboBox.setBorder(null);
 		}
 
		public TableCellEditor(JTextField textField) {
 			super(textField);
 			textField.setBorder(null);
 		}
 
 		@Override
 		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
 			rowIndex = table.convertRowIndexToModel(row);
 			columnIndex = table.convertColumnIndexToModel(column);
 			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
 		}
 
 		@Override
 		public boolean stopCellEditing() {
 			boolean ok = DatabaseTableModel.this.setEditedValueAt(rowIndex, columnIndex, getCellEditorValue());
 			if (ok) {
 				cancelCellEditing();
 			}
 			return ok;
 		}
 	}
 }
