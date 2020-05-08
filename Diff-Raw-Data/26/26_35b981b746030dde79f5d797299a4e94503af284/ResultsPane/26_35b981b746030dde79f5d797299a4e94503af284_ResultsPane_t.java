 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 
 package org.wwscc.protimer;
 
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 
 public class ResultsPane extends JPanel
 {
 	JTable tbl;
 	ResultsModel mdl;
 
 	public ResultsPane(ResultsModel inModel)
 	{
 		setLayout(new BorderLayout());
 
 		mdl = inModel;
 
 		ColorTimeRenderer large  = new ColorTimeRenderer(3);
 		ColorTimeRenderer medium = new ColorTimeRenderer(2);
 		//ColorTimeRenderer small  = new ColorTimeRenderer(1);
 		DifferenceRenderer diff  = new DifferenceRenderer();
 
 		tbl = new JTable(inModel) { public void tableChanged(TableModelEvent e) { super.tableChanged(e); scrollTable(); } };
 		tbl.setDefaultRenderer(ColorTime.class, new ColorTimeRenderer(1));
 		tbl.setRowHeight(140);
 		tbl.setRowSelectionAllowed(false);
 		//tbl.setIntercellSpacing(new Dimension(8, 8));
 
 		TableColumnModel tcm = tbl.getColumnModel();
		columnStyle(tcm, 0, 50,  70,  200, medium);
		columnStyle(tcm, 1, 50,  70,  200, medium);
		columnStyle(tcm, 2, 80,  110, 280, large);
 
		columnStyle(tcm, 3, 180, 230, 700, diff);
 
		columnStyle(tcm, 4, 50,  70,  200, medium);
		columnStyle(tcm, 5, 50,  70,  200, medium);
		columnStyle(tcm, 6, 80,  110, 280, large);
 
 		JScrollPane scroll = new JScrollPane(tbl, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		add(scroll);
 	}
 
 
 	public void scrollTable()
 	{
 		Runnable scrollit = new Runnable() {
 			public void run() { 
 				if ((tbl == null) || (mdl == null)) return;
 				tbl.scrollRectToVisible(tbl.getCellRect(mdl.getRowCount(), 1, true));
 			}
 		};
 		SwingUtilities.invokeLater(scrollit);
 	}
 
 
 	protected void columnStyle(TableColumnModel tcm, int column, int min, int mid, int max, TableCellRenderer rend)
 	{
 		TableColumn tc = tcm.getColumn(column);
 		if (tc == null) return;
 		tc.setMinWidth(min);
 		tc.setPreferredWidth(mid);
 		tc.setMaxWidth(max);
 		tc.setCellRenderer(rend);
 	}
 
 }
 
