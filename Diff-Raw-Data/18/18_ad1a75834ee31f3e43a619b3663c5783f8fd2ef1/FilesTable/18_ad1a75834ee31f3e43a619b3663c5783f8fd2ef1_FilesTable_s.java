 //==============================================================================
 // Date Created:		1 December 2009
// Last Updated:		29 August 2012
 //
 // File name:			FilesTable.java
 // File author:			Matthew Hydock
 // File Description:	Creates a Swing table to display necessary file data.
 //==============================================================================
 
 import java.awt.*;
 import javax.swing.JProgressBar;
 import javax.swing.JTable;
 import javax.swing.table.*;
 import java.util.Vector;
 
 public class FilesTable extends JTable
 {
	private KleptoChanCore core = KleptoChanCore.getInstance();
 	private FilesTableModel tableModel = new FilesTableModel(core.getFileList());
 	
 	public FilesTable()
 	{
 		super();
 		setModel(tableModel);
 		setDefaultRenderer(Object.class, new FilesProgressRenderer());
 	}
 
 //==============================================================================
 // Private class to deal with how internal data is stored.
 //==============================================================================
 	class FilesTableModel extends AbstractTableModel
 	{
 		private Vector list;
 		
 		public FilesTableModel (Vector c)
 		{
 			list = c;
 		}
 		
 		public String getColumnName(int col)
 		{
 			switch (col)
 			{
 				case 0: return "File Name";
 				case 1: return "File Progress";
 				case 2: return "Percent Complete";
 				case 3: return "Current Size";
 				case 4: return "Status";
 			}
 			
 			return null;
 		}
 		
 		public synchronized int getRowCount()
 		{
 			return list.size();
 		}
 
 		public int getColumnCount()
 		{
 			return 5;
 		}
 				
 		public Object getValueAt(int row, int col)
 		{
 				FileConnection fc;
 				synchronized (list)
 				{
 	        		fc = (FileConnection)(list.get(row));
 				}
 				
         		switch (col)
         		{
         			case 0: return fc.getName();
         			case 1:
         			case 2: return ((fc.getFinalSize() != -1)?"    " + (int)(100.0*((float)fc.getCurrentSize()/(float)fc.getFinalSize())) + "%":"    --%");
         			case 3: return fc.getPrettySize();
         			case 4: return fc.getStatus();
         		}
         	
         		return null;
 		}
 
 		public boolean isCellEditable(int row, int col)
 		{
 			return false;
 		}
 	
 		public void setValueAt(Object value, int row, int col) {}
 
 		public synchronized FileConnection getFileConnection(int i)
 		{
 			return (FileConnection)(list.get(i));
 		}
 	}
 //==============================================================================
 
 
 //==============================================================================
 // Custom cell renderer. If it's not the progress bar, use the default.
 //==============================================================================
 	class FilesProgressRenderer extends JProgressBar implements TableCellRenderer
 	{
 		DefaultTableCellRenderer defRender = new DefaultTableCellRenderer();
 		
 		public FilesProgressRenderer()
 		{
 			super(0,0);
 			setStringPainted(false);
 		}
 		
 		public Component getTableCellRendererComponent(JTable table, Object o, boolean isSelected, boolean hasFocus, int row, int column)
 		{
 			if (column != 1)
 				return defRender.getTableCellRendererComponent(table,o,isSelected,hasFocus,row,column);
 			
 			FileConnection fc = ((FilesTableModel)table.getModel()).getFileConnection(row);
 			
 			if (fc.getFinalSize() == 1)
 			{
 				setMaximum(1);
 				setValue(0);
 			}
 			else
 			{
 				setMaximum((int)fc.getFinalSize());
 				setValue((int)fc.getCurrentSize());
 			}
 			
 			if (fc.error != null)
 				setForeground(Color.yellow);
 			else if (fc.getCurrentSize() >= fc.getFinalSize())
 				setForeground(new Color(0,200,0));
 			else
 				setForeground(new Color(150,255,150));
         	
         	return this;
 		}
 		
 		public boolean isDisplayable()
 		{
 			return true;
 		}
 	}
 //==============================================================================
 }
 
