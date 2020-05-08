 /**
  * 
  * CabConverter: A Cabrillo generation tool for MacLoggerDX
  * Original concept and author: B. Scott Andersen (NE1RD)
  */
 package com.bsandersen.CabConverter;
 
 import java.util.Formatter;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 /**
  * This class provides a means of viewing the contents of an imported ADIF file.
  * @author B. Scott Andersen
  *
  * CabConverter by B. Scott Andersen (NE1RD) is licensed under a 
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  */
 public class LogViewer extends JScrollPane {
 	
 	/*
 	 * Fulfill the needs of our parent
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/*
 	 * Make this a Singleton
 	 */
 	private static LogViewer me;
 	
 	/**
 	 * The LogViewer object will be a singleton. This gets the reference to its instance.
 	 * @return reference to LogViewer
 	 */
 	public static LogViewer getInstance() {
 		return me;
 	}
 	
 	/*
 	 * Reference to the JTable that holds our log
 	 */
 	private JTable table;
 	private LogTable logTable;
 	
 	/**
 	 * Constructor
 	 */
 	public LogViewer() {
 		me = this;
 		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		logTable = new LogTable();
 		table = new JTable(logTable);
 		table.getTableHeader().setReorderingAllowed(false);
 		table.setRowSelectionAllowed(false);
 		table.setColumnSelectionAllowed(false);
 		table.setCellSelectionEnabled(false);
 		setViewportView(table);
 	}
 	
 	/**
 	 * This method removes all previously inserted QSO records from the table.
 	 */
 	public void removeAllRows() {
 		logTable.removeAllRows();
 	}
 	
 	/**
 	 * This method adds the QSO data to the table
 	 */
 	public void addQSO(ADIFrecord rec) {
 		logTable.addQSO(rec);
 	}
 	
 	/**
 	 * This method triggers the UI to refresh after records are added or removed.
 	 */
 	public void refresh() {
 		logTable.refresh();
 	}
 	
 	/**
 	 * LogTable is a table model used to manage the log display area.
 	 * @author B. Scott Andersen
 	 */
 	private class LogTable extends AbstractTableModel {
 		/**
 		 * Fulfill the needs of our parent
 		 */
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * Titles for our table
 		 */
 		private final String titles[] = new String[] {
 				"Date", "Time", 
 				"Frequency", "Mode", 
 				"Call", 
 				"RSTr", "RSTs", 
 				"Serial Recv", "Serial Sent", 
 				"Received", "Sent"
 		};
 		
 		/*
 		 * rows - number of active rows
 		 * MAXROWS - total number of QSOs that can be processed by the program
 		 * data - the QSO data to be displayed by the table
 		 */
 		private int rows = 0;
 		private final int MAXROWS = 2000;
 		private String data[][] = new String[MAXROWS][titles.length];
 		
 		/*
 		 * Data column aliases
 		 */
 		private static final int COL_DATE = 0;
 		private static final int COL_TIME = 1;
 		private static final int COL_FREQUENCY = 2;
 		private static final int COL_MODE = 3;
 		private static final int COL_CALL = 4;
 		private static final int COL_RSTR = 5;
 		private static final int COL_RSTS = 6;
 		private static final int COL_SERR = 7;
 		private static final int COL_SERS = 8;
 		private static final int COL_EXCR = 9;
 		private static final int COL_EXCS = 10;
 		
 		/**
 		 * Constructor
 		 */
 		LogTable() {
 		}
 
 		public void refresh() {
 			fireTableDataChanged();
 		}
 
 		@Override
 		public Class<?> getColumnClass(int arg0) {
 			return String.class;
 		}
 
 		@Override
 		public int getColumnCount() {
 			return titles.length;
 		}
 
 		@Override
 		public String getColumnName(int arg0) {
 			return titles[arg0];
 		}
 
 		@Override
 		public int getRowCount() {
 			return rows;
 		}
 
 		@Override
 		public Object getValueAt(int arg0, int arg1) {
 			return data[arg0][arg1];
 		}
 
 		@Override
 		public boolean isCellEditable(int arg0, int arg1) {
 			return false;
 		}
 
 		@Override
 		public void setValueAt(Object arg0, int arg1, int arg2) {
 			data[arg1][arg2] = (String)arg0;
 		}
 		
 		/**
 		 * This method removes all previously inserted QSO records from the table.
 		 */
 		public void removeAllRows() {
 			rows = 0;
 		}
 		
 		/**
 		 * This method takes a QSO held in an ADIFrecord and adds it to the table.
 		 * If we run out of table data space (we shouldn't, but if we do), we 
 		 * throw a runtime exception and tell the user to get the program updated
 		 * with a bigger table.
 		 * @param rec - the ADIFrecord read from the file to be inserted in the table
 		 */
 		public void addQSO(ADIFrecord rec) {
 			Formatter f = new Formatter();
 			
 			if (rows < MAXROWS-2) {
 				data[rows][COL_DATE] = new String(rec.date);
 				data[rows][COL_TIME] = new String(rec.time.substring(0, 4));
 				
 				f.format("%.3f", rec.frequency);
 				data[rows][COL_FREQUENCY] = new String(f.toString());
 				data[rows][COL_MODE] = new String(rec.mode);
 				data[rows][COL_CALL] = new String(rec.call);
 				data[rows][COL_RSTR] = new String(rec.rstReceived);
 				data[rows][COL_RSTS] = new String(rec.rstSent);
 				if (rec.serialNumberSent >= 0) {
					data[rows][COL_SERR] = new String(new Integer(rec.serialNumberSent).toString());
 				} else {
					data[rows][COL_SERR] = new String("");
 				}
 				if (rec.serialNumberReceived >= 0) {
					data[rows][COL_SERS] = new String(new Integer(rec.serialNumberReceived).toString());
 				} else {
					data[rows][COL_SERS] = new String("");
 				}
 				data[rows][COL_EXCR] = new String(rec.exchangeReceived);
 				data[rows][COL_EXCS] = new String(rec.exchangeSent);
 				rows++;
 			} else {
 				f.close();
 				throw new RuntimeException("Too many QSOs. Increase QSO table size in program.");
 			}
 			f.close();
 		} // addQSO
 	} // LogTable
 } // LogViewer
