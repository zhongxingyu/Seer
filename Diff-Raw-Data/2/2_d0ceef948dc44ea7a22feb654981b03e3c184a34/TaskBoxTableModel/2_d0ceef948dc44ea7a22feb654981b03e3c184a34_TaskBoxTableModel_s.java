 /*OsmUi is a user interface for Osmosis
 Copyright (C) 2011  Verena Käfer, Peter Vollmer, Niklas Schnelle
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or 
 any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package de.osmui.ui.models;
 
 
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import de.osmui.i18n.I18N;
 import de.osmui.model.osm.TTask;
 
 /**
  * @author Niklas Schnelle, Peter Vollmer, Verena Käfer
  *
  *no tests, only getter and setter
  */
 public class TaskBoxTableModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = -2453623808903546286L;
 
 	List<TTask> data;
 	
 	public void setTasks(List<TTask> tasks){
 		data = tasks;
 		fireTableStructureChanged();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 1;
 	}
 
 	@Override
 	public int getRowCount() {
 		return (data == null) ? 0 : data.size() - 1;
 	}
 
 	@Override
 	public Object getValueAt(int row, int column) {
		TTask value = data.get(row + 1);
 		return value;
 	}
 
 	@Override
 	public boolean isCellEditable(int row, int col) {
 		return false;
 	}
 
 	@Override
 	public Class<? extends Object> getColumnClass(int c) {
 		return getValueAt(0, c).getClass();
 	}
 	
 	@Override
 	public String getColumnName(int col){
 		return I18N.getString("TaskBoxTableModel.name");
 	}
 
 }
