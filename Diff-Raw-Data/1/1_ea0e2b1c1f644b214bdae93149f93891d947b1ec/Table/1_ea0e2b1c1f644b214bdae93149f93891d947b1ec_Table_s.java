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
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.sql.SQLException;
 
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 
 import de.jandavid.asxcel.view.View;
 
 /**
  * The RouteTable contains all routes and displays them to the user
  * in a way that enables him to make strategic decisions based on
  * the information provided.
  * 
  * @author jdno
  */
 public class Table extends JTable implements KeyListener {
 
 	/**
 	 * For future use.
 	 */
 	private static final long serialVersionUID = 8830972621903323239L;
 	
 	/**
 	 * The table containing the routes gets initialized in this class,
 	 * but the data is contributed by RoutesTableModel.
 	 * @param view The view with access to the model.
 	 */
 	public Table(View view) {
 		this.setModel(new RoutesModel(view.getModel()));
 		this.setAutoCreateRowSorter(true);
 		this.addKeyListener(this);
 		
 		StandardCellRenderer standardCellRenderer = new StandardCellRenderer(view.getModel());
 		this.setDefaultRenderer(String.class, standardCellRenderer);
 		this.setDefaultRenderer(Integer.class, standardCellRenderer);
 		this.setDefaultRenderer(Boolean.class, new BooleanCellRenderer(view.getModel()));
 
 		JTableHeader header = this.getTableHeader();
 		header.setDefaultRenderer(new HeaderRenderer(this));
 		header.setResizingAllowed(false);
 		header.setReorderingAllowed(false);
 		
 		setColumnSizes();
 		setShowVerticalLines(true);
 		setGridColor(new Color(164,164,164));
 		setFocusable(true);
 	}
 	
 	/**
 	 * This auxiliary method sets the width of the columns to custom
 	 * values. This is done because many columns only hold one to
 	 * three letters.
 	 */
 	private void setColumnSizes() {
 		getColumnModel().getColumn(0).setPreferredWidth(140); // origin name
 		getColumnModel().getColumn(1).setPreferredWidth(55); // origin iata
 		getColumnModel().getColumn(2).setPreferredWidth(55); // origin pax
 		getColumnModel().getColumn(3).setPreferredWidth(55); // origin cargo
 		getColumnModel().getColumn(4).setPreferredWidth(140); // destination name
 		getColumnModel().getColumn(5).setPreferredWidth(55); // destination iata
 		getColumnModel().getColumn(6).setPreferredWidth(55); // destination pax
 		getColumnModel().getColumn(7).setPreferredWidth(55); // destination cargo
 		getColumnModel().getColumn(8).setPreferredWidth(70); // transfer possible
 		getColumnModel().getColumn(9).setPreferredWidth(140); // distance
 		getColumnModel().getColumn(10).setPreferredWidth(68); // loads to
 		getColumnModel().getColumn(11).setPreferredWidth(68); // loads from
 		getColumnModel().getColumn(12).setPreferredWidth(68); // scheduled
 	}
 	
 	/**
 	 * This private class is needed to center the column names. It's taken from
 	 * http://stackoverflow.com/questions/7493369/jtable-right-align-header/7494597
 	 * with all respect to the author.
 	 * 
 	 * @author http://stackoverflow.com/users/230513/trashgod
 	 */
 	private class HeaderRenderer implements TableCellRenderer {
 
 	    DefaultTableCellRenderer renderer;
 
 	    public HeaderRenderer(JTable table) {
 	        renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
 	        renderer.setHorizontalAlignment(JLabel.CENTER);
 	    }
 
 	    @Override
 	    public Component getTableCellRendererComponent(
 	        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
 	        return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
 	    }
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// Do nothing.
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// Do nothing.
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		try {
 			if(e.getKeyCode() == KeyEvent.VK_DELETE) {
 				RoutesModel tm = (RoutesModel) this.getModel();
 				tm.removeRow(convertRowIndexToModel(getSelectedRow()));
 			}
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();			
 		}
 	}
 
 }
