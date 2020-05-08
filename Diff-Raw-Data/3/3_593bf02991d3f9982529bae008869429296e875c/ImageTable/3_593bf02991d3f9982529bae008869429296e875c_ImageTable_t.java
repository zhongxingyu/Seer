 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.swing.*;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.*;
 
 public class ImageTable extends JTable implements TableModelListener {
 
 	/**
 	 * @author Andreas J
 	 */
 	private static final long serialVersionUID = 1L;
 	// private static DefaultTableModel model = new DefaultTableModel();
 	private ImageTableModel model;
 	private ImageTableListener listener;
 	private Dimension dim;
 
 	// private static List<SettingsPicture> selected;
 	/*
 	 * Constructor for the ImageTableSet number of rows, columns, renderer
 	 */
 	public ImageTable(ImageTableModel model, Dimension dim) {
 		// call super class and initialize objects
 		super(model);
 		this.dim = dim;
 		this.model = model;
 		listener = new ImageTableListener(this, model);
 		this.setPreferredSize(new Dimension(600, 300));
 		// remove table header
 		setTableHeader(null);
 		// set table sizing properties
 		// setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		this.setAutoCreateColumnsFromModel(true);
          this.setFocusable(true);
          this.setRequestFocusEnabled(true);
 		setColumnSize();
 		setRowSize();
 		// set grid and spacing properties
 		setIntercellSpacing(new Dimension(0, 0));
 
 		setBorder(null);
 		setDragEnabled(false);
 		setShowGrid(false);
 		this.setBackground(Color.WHITE);
 		this.setForeground(Color.WHITE);
 		this.setSelectionBackground(this.getBackground());
 		this.setSelectionForeground(this.getBackground());
 		// Set listener models and add listener
 		this.setCellSelectionEnabled(true);
 		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		getColumnModel().getSelectionModel().addListSelectionListener(listener);
 		getSelectionModel().addListSelectionListener(listener);
 		// set color for column selection
 		setOpaque(false);
 		ImageTableRenderer imageTabelRenderer = new ImageTableRenderer(
 				getDefaultRenderer(Object.class), Color.yellow);
 		setDefaultRenderer(ImageIcon.class, imageTabelRenderer);
 
 	}
 
 	// sets Size of rows based on tabledimensions
 
 	public void setRowSize() {
 		for (int i = 0; i < model.getRowCount(); i++) {
          
 			this.setRowHeight(i, 60);
 			this.setRowMargin(5);
 		}
 		System.out.println("Row count" + getRowCount());
 
 	}
 
 	// sets Size of columns based on tabledimensions
 	public void setColumnSize() {
 		TableColumn column = null;
 		for (int i = 0; i < model.getColumnCount(); i++) {
 			column = getColumnModel().getColumn(i);
 			column.sizeWidthToFit();
 		}
 	}
 
 	public void setSelected(Object[] sel) {
 
 	}
 
 	public void removeFlagged() {
 
 		for (int i = 0; i < getRowCount(); i++) {
 			for (int j = 0; j < getColumnCount(); j++) {
 
 
 				i = convertRowIndexToModel(i);
 				j = convertColumnIndexToModel(j);
 				if (model.cellIsFlagged(i, j)){
 					model.setflagOnPicture(i, j, true);
 				}
 					
 					
 			}
 		}
 	}
}
 
