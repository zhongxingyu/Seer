 /**
  * This file is part of ASxcel.
  *
  * ASxcel is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ASxcel is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ASxcel.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.jandavid.asxcel.view.Routes;
 
 import java.sql.SQLException;
 
 import javax.swing.table.AbstractTableModel;
 
 import de.jandavid.asxcel.model.Model;
 import de.jandavid.asxcel.model.Route;
 
 /**
  * This is the model for the table, which contributes the actual
  * data to the table. It gets the table directly from the model,
  * and it writes changed directly back to the model, where they
  * get saved to the database.
  * 
  * @author jdno
  */
 public class RoutesModel extends AbstractTableModel {
 
 	/**
 	 * For future use.
 	 */
 	private static final long serialVersionUID = 2048022952298622817L;
 	
 	/**
 	 * This array contains the column names.
 	 */
 	private String[] columnNames = {"Origin", "IATA", "PAX", "Cargo",
 			"Destination", "IATA", "PAX", "Cargo", "Transfer", "Distance", "Loads to", "Loads from", "Scheduled"};
 	
 	private Class<?>[] columnClasses = {String.class, String.class, Integer.class, Integer.class,
 			String.class, String.class, Integer.class, Integer.class, Boolean.class, Integer.class, Integer.class, 
 			Integer.class, Boolean.class};
 	
 	/**
 	 * This is needed to load and save the data.
 	 */
 	private Model model;
 	
 	/**
 	 * The RoutesTableModel gets initialized with the model, because
 	 * it fetches the data to display and writes changes back to it.
 	 * @param model
 	 */
 	public RoutesModel(Model model) {
 		this.model = model;
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
 	 */
 	@Override
 	public Class<?> getColumnClass(int columnIndex) {
 		return columnClasses[columnIndex];
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getColumnCount()
 	 */
 	@Override
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 	
 	@Override
 	public String getColumnName(int column) {
 		return columnNames[column];
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getRowCount()
 	 */
 	@Override
 	public int getRowCount() {
 		return model.getEnterprise().getRoutes().size();
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.TableModel#getValueAt(int, int)
 	 */
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		Route r = model.getEnterprise().getRoutes().get(rowIndex);
 		
 		switch(columnIndex) {
 		case 0: // origin airport
 			return r.getOrigin().getName();
 		case 1: // origin iata
 			return r.getOrigin().getIataCode();
 		case 2: // origin PAX
 			return r.getOrigin().getPassengers();
 		case 3: // origin cargo
 			return r.getOrigin().getCargo();
 		case 4: // destination airport
 			return r.getDestination().getName();
 		case 5: // destination iata
 			return r.getDestination().getIataCode();
 		case 6: // destination PAX
 			return r.getDestination().getPassengers();
 		case 7: // destination cargo
 			return r.getDestination().getCargo();
 		case 8: // transfer possible
 			return r.getDestination().isTransferPossible();
 		case 9: // distance
 			return r.getDistance();
 		case 10: // load to
 			return r.getLoadTo();
 		case 11: // load from
 			return r.getLoadFrom();
 		case 12: // scheduled
 			return r.isScheduled();
 		default:
 			return "";
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
 	 */
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		if(columnIndex != 0 && columnIndex != 4) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * This method removes a row by first removing it from the data model and
 	 * then from this table model.
 	 * @param row The row to delete.
 	 * @throws SQLException If a SQL error occurs this gets thrown.
 	 */
 	public void removeRow(int row) throws SQLException {
 		model.getEnterprise().deleteRoute(row);
 		
 		fireTableDataChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
 	 */
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		Route r = model.getEnterprise().getRoutes().get(rowIndex);
 
 		try {
 			switch(columnIndex) {
 			case 0: // origin airport
 				break;
 			case 1: // origin iata
 				r.getOrigin().setIataCode((String) aValue);
 				break;
 			case 2: // origin PAX
 				r.getOrigin().setPassengers((Integer) aValue);
 				break;
 			case 3: // origin cargo
 				r.getOrigin().setCargo((Integer) aValue);
 				break;
 			case 4: // destination airport
 				break;
 			case 5: // destination iata
 				r.getDestination().setIataCode((String) aValue);
 				break;
 			case 6: // destination PAX
 				r.getDestination().setPassengers((Integer) aValue);
 				break;
 			case 7: // destination cargo
 				r.getDestination().setCargo((Integer) aValue);
 				break;
 			case 8: // transfer possible
 				r.getDestination().setTransferPossible(!r.getDestination().isTransferPossible());
 				break;
 			case 9: // distance
 				r.setDistance((Integer) aValue);
 				break;
 			case 10: // load to
 				r.setLoadTo((Integer) aValue);
 				break;
 			case 11: // load from
 				r.setLoadFrom((Integer) aValue);
 				break;
 			case 12: // scheduled
 				r.setScheduled(!r.isScheduled());
 				break;
 			default:
 				break;
 			}
 			
 			fireTableDataChanged();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
