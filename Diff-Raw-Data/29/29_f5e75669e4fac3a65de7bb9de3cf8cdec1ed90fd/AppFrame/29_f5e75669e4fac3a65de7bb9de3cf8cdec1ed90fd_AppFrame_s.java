 package com.ns.app;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 
 import org.apache.commons.lang3.Validate;
 
 /**
  * Manages the main application frame and controls the lifetime of the
  * application.
  */
 public class AppFrame extends JFrame {
 	private static final long serialVersionUID = -5547457548599580479L;
 	private Unpacker unpacker;
 	private ColourStrategy colourStrategy;
 	private JTable table;
 
 	/**
 	 * Responds to window close events. This is required to ensure that
 	 * resources are correctly cleaned up.
 	 */
 	private WindowAdapter windowAdapter = new WindowAdapter() {

 		@Override
 		public void windowClosing(WindowEvent e) {
 			super.windowClosing(e);
 			AppFrame.this.dispose();
 		}
 
 		@Override
 		public void windowClosed(WindowEvent e) {
 			super.windowClosed(e);
 			exit();
 		}
 	};
 
 	/**
 	 * Renders cells in the table.
 	 */
 	private TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
 		private static final long serialVersionUID = -6657347584881810678L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus,
 				int rowIndex, int vColIndex) {
 
 			Component comp = super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, rowIndex, vColIndex);
 
 			comp.setBackground(colourStrategy.getColour(
 					unpacker.getFile(rowIndex),
 					AppFrame.this.isShaded(rowIndex)));
 
 			return comp;
 		}
 	};
 
 	/**
 	 * Returns a flag indicating whether the specified row should be shaded on
 	 * the the displayed table. Files in every second directory are shaded so
	 * that the directory boundaries stand out.
 	 * 
 	 * @param rowIndex
 	 *            the index of the row
 	 * @return true if the specified row index should be shaded
 	 */
 	private boolean isShaded(int rowIndex) {
 		Validate.isTrue(rowIndex >= 0,
 				"Row index must be greater than or equal to zero.");
 		boolean bShaded = false;
 		String prevFolderPath = unpacker.getFolderPath(0);
 		for (int i = 0; i <= rowIndex; i++) {
 			String folderPath = unpacker.getFolderPath(i);
 			if (prevFolderPath.compareToIgnoreCase(folderPath) != 0) {
 				prevFolderPath = folderPath;
 				bShaded = !bShaded;
 			}
 		}
 		return bShaded;
 	}
 
 	/**
 	 * Constructor. Prepares resources and initializes the layout and contents
 	 * of the main frame.
 	 */
 	public AppFrame(File cvsPackFile, ColourStrategy colourStrategy) {
 		super("Cvs pack viewer");
 
 		this.colourStrategy = colourStrategy;
 
 		// Unpack and prepare the cvs pack file.
 		Unpacker unpacker;
 		try {
 			unpacker = new Unpacker(cvsPackFile);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		this.unpacker = unpacker;
 
 		// Prepare event listeners.
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		addWindowListener(this.windowAdapter);
 
 		// Initialize layout and contents.
 		setBounds(100, 100, 450, 300);
 		TableModel tableModel = new AppTableModel(unpacker);
 		table = new JTable(tableModel);
 		for (int i = 0; i < tableModel.getColumnCount(); i++) {
 			table.getColumnModel().getColumn(i)
 					.setCellRenderer(tableCellRenderer);
 		}
 		getContentPane().add(table, BorderLayout.CENTER);
 	}
 
 	/**
 	 * Cleans up resources and exits the application.
 	 */
 	private void exit() {
 		try {
 			unpacker.close();
 		} catch (IOException e) {
 			e.printStackTrace(); // Ignore errors
 		}
 
 		System.exit(0);
 	}
 }
