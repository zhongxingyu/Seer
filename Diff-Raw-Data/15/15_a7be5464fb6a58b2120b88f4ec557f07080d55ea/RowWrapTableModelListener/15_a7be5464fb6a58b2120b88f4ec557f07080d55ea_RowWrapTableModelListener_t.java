 package app.utils;
 
 import app.screens.WorkLogScr;
 
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 /**
  * Created by Barak on 13/01/14.
  */
 public class RowWrapTableModelListener implements TableModelListener
 {
 	public static final int DEFAULT_ROW_HEIGHT = 25;
 
 	private int colToFollow;
 	private JTable table;
 
 	public RowWrapTableModelListener(int colToFollow, JTable table)
 	{
 		this.colToFollow = colToFollow;
 		this.table = table;
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent e)
 	{
 		int colLen = table.getColumnModel().getColumn(colToFollow).getWidth();
 
 		if (e.getType() == TableModelEvent.INSERT)
 		{
 			setRowHeightByColLength(colLen, e.getFirstRow());
 		}
 		else if (e.getType() == TableModelEvent.UPDATE)
 		{
 			// meaning we've update a single row
 			if (e.getFirstRow() == e.getLastRow())
 			{
 				setRowHeightByColLength(colLen, e.getFirstRow());
 			}
 			// The all model has changed
 			else
 			{
 				for (int i = 0; i < table.getRowCount(); i++)
 				{
 					setRowHeightByColLength(colLen, i);
 				}
 			}
 		}
 	}
 
 	private void setRowHeightByColLength(int colLength, int rowIndex)
 	{
 		int linesInRow = 0;
 		String cellText = table.getValueAt(rowIndex, WorkTableModel.JOBS_DESCR_COL).toString();
 		String[] lines = cellText.split("\n");
 		for (String line : lines)
 		{
 			linesInRow++;
 			int textLen = table.getFontMetrics(WorkLogScr.DEFAULT_TEXT_FONT).stringWidth(line);
 			if (textLen > colLength)
 			{
				linesInRow += textLen/ colLength;
 			}
 		}
 
 		table.setRowHeight(rowIndex, linesInRow * DEFAULT_ROW_HEIGHT);
 	}
 }
