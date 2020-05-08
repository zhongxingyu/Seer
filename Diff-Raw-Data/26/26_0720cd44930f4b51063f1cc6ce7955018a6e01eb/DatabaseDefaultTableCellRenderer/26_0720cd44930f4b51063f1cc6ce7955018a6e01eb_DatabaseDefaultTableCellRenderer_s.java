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
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 
 import eu.lp0.cursus.db.data.Entity;
 
 public abstract class DatabaseDefaultTableCellRenderer<T extends Entity, V> extends DefaultTableCellRenderer {
 	private final DatabaseTableCellRenderer.Column<T, V> column;
 
 	public DatabaseDefaultTableCellRenderer(DatabaseTableCellRenderer.Column<T, V> column) {
 		this.column = column;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public final Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int vRow, int vCol) {
 		if (value instanceof Entity) {
 			value = column.loadValue((T)value, false);
 		} else {
 			// DefaultCellEditor may try to be smart and obtain the renderer by passing us a converted Boolean value
			value = false;
 		}
 		return getTableCellRendererComponent_(table, (V)value, isSelected, hasFocus, vRow, vCol);
 	}
 
 	protected Component getTableCellRendererComponent_(JTable table, V value, boolean isSelected, boolean hasFocus, int vRow, int vCol) {
 		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, vRow, vCol);
 	}
 }
