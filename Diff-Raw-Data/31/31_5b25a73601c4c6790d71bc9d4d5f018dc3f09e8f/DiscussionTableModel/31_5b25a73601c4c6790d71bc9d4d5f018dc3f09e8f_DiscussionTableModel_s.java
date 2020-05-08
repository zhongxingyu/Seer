 package org.computer.knauss.reqtDiscussion.ui.uiModel;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 
 public class DiscussionTableModel implements TableModel {
 
 	private static final String[] COLUMNS = new String[] { "ID", "Summary" };
 	private Discussion[] discussions;
 	private List<TableModelListener> listeners = new LinkedList<TableModelListener>();
 	private JTable table;
 
 	@Override
 	public void addTableModelListener(TableModelListener arg0) {
 		this.listeners.add(arg0);
 	}
 
 	@Override
 	public Class<?> getColumnClass(int col) {
 		if (col == 0)
 			return Integer.class;
 		return String.class;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return COLUMNS.length;
 	}
 
 	@Override
 	public String getColumnName(int col) {
 		if (col >= getColumnCount())
 			return null;
 		return COLUMNS[col];
 	}
 
 	@Override
 	public int getRowCount() {
 		if (this.discussions == null)
 			return 0;
 		return this.discussions.length;
 	}
 
 	@Override
 	public Object getValueAt(int row, int col) {
 		if (this.discussions == null || row >= this.getRowCount())
 			return null;
 		Discussion d = this.discussions[row];
 		switch (col) {
 		case 0:
 			return d.getID();
 		case 1:
 			return d.getSummary();
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isCellEditable(int arg0, int arg1) {
 		return false;
 	}
 
 	@Override
 	public void removeTableModelListener(TableModelListener arg0) {
 		this.listeners.remove(arg0);
 	}
 
 	@Override
 	public void setValueAt(Object arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setDiscussions(Discussion[] discussions) {
		this.discussions = discussions;
 		fireTabledataChanged();
 	}
 
 	public void addDiscussions(Discussion[] discussions) {
 		Discussion[] temp = new Discussion[this.discussions.length
 				+ discussions.length];
 		for (int i = 0; i < this.discussions.length; i++) {
 			temp[i] = this.discussions[i];
 		}
 		for (int i = 0; i < discussions.length; i++) {
 			temp[i + this.discussions.length] = discussions[i];
 		}
 		this.discussions = temp;
 	}
 
 	public Discussion[] getDiscussions() {
 		return this.discussions;
 	}
 
 	void fireTabledataChanged() {
 		for (TableModelListener l : this.listeners) {
 			l.tableChanged(new TableModelEvent(this));
 		}
 	}
 
 	public Discussion[] getSelectedDiscussions() {
 		int[] ind = this.table.getSelectedRows();
 		Discussion[] ret = new Discussion[ind.length];
 
 		for (int i = 0; i < ind.length; i++) {
 			if (ind[i] > getRowCount())
 				// invalid selection!
 				return new Discussion[0];
 			ret[i] = this.discussions[this.table.getRowSorter()
 					.convertRowIndexToModel(ind[i])];
 		}
 
 		return ret;
 	}
 
 	public JTable getTable() {
 		return this.table;
 	}
 
 	public void setTable(JTable table) {
 		this.table = table;
 	}
 }
