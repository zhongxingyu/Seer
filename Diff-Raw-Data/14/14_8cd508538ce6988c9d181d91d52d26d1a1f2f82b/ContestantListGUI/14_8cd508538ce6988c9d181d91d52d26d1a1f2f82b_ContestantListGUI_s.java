 /**
  * ContestantListGUI -- Sort table for Contestant list, 
  * sorts table in column choice, is editable, displays the contestant image 
  * Adapted from docs.oracle.com (TableSortDemo.java)
  * 
  * @author Manor Freeman, Hazel Rivera, Martin Grabarczyk, Liam Corrigan, Jeff
  *         Westaway, Delerina Hill
  * V 1.0 March 1, 2012
  */
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 
 public class ContestantListGUI extends JPanel	{
 	//Attributes
 	private static final long serialVersionUID = 1L;
 
 	private boolean DEBUG = false;
 	private static final int WIDTH = 850;
 	private static final int HEIGHT = 400;
 	private int numConts=0;
 	private Contestant[] contestants;
 	private Font textInputFieldFont;
 	private Color textInputFieldColor;
 
 	private JPanel contListPanel;
 
 	private ImageIcon placeHolderImg = new ImageIcon(getClass().getResource("images/uploadPic_small.jpg"));
 	//    private JLabel imgPlaceHolder;    
 
 	/*******************************  CONSTRUCTOR  ***********************************/
 	/**
 	 * Calls the contestant list interface
 	 */
 	public ContestantListGUI(Contestant[] conts, int numConts) {
 		super(new GridLayout(1,0));
 		this.numConts=numConts;
 		this.contestants=conts;
 		add(createContList());
 	}
 	/**
 	 * Creates the contestant list sorted table 
 	 * @return contestant custom table
 	 */
 	public JComponent createContList() {
 		contListPanel = new JPanel();
 		MyTableModel contTable=new MyTableModel(contestants, numConts);
 		JTable table = new JTable(contTable);
 
 		table.setPreferredScrollableViewportSize(new Dimension(WIDTH,HEIGHT));
 		//		table.setFillsViewportHeight(true);
 		//		table.setAutoCreateRowSorter(true);
 		table.setRowHeight(77);
 
 		table.setFont(new Font("Viner Hand ITC",Font.PLAIN,18));
 		table.setForeground(Color.BLUE);
 		table.setSelectionForeground(Color.RED);
 		//		table.setOpaque(false);
 		//		table.setBackground(new Color(0,0,0,64));
 		table.setSelectionBackground(new Color(0,0,0,64)); // When a cell is selected, this entire row is highlighted.
 
 		//Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 		contListPanel.add(scrollPane);	
 
 		return contListPanel;
 	} // End of Constructor
 
 	/** 
 	 * Creates a custom sort table of contestant information
 	 * This class gets called by the main panel
 	 */
 	class MyTableModel extends AbstractTableModel {
		private String[] columnNames =  {"User ID", "First Name", "Last Name", "Tribe", "Picture", "Eliminated"};
 		private Contestant[] contestants;
 		private Object[][] data;
 		public MyTableModel(Contestant[] conts, int numConts) {
 			super();
 			this.contestants = conts;
			data = new Object[numConts][6];
 			for(int i=0;i<numConts;i++){
 				data[i][0]=contestants[i].getID();
 				data[i][1]=contestants[i].getFirst();
 				data[i][2]=contestants[i].getLast();
 				data[i][3]=contestants[i].getTribe();
 				if(contestants[i].getPicture()!=null && !contestants[i].getPicture().equals("null")){	
 					//Scale the Image to fit inside the table
 					ImageIcon contPic = createImageIcon(contestants[i].getPicture());
 					Image img = contPic.getImage() ;  
 					Image newimg = img.getScaledInstance( 100, 80,  java.awt.Image.SCALE_SMOOTH ) ;  
 					contPic = new ImageIcon(newimg);
 					data[i][4]=contPic;
 				}
 				else{
 					data[i][4]=placeHolderImg;
 				}
				data[i][5]=(contestants[i].getElimRound()!=null);
 			}	
 		}
 
 		public ImageIcon createImageIcon(String path) {
 			java.net.URL imgURL = SurvivorPoolAdminGUI.class.getResource(path);
 			if(imgURL != null) {
 				return new ImageIcon(imgURL);
 			} else {
 				//JOptionPane.showMessageDialog(this, "The image must be in the images folder.");
 				return null;
 			}		
 		}
 
 		//		private Object[][] data = {  /*************** TODO Implement getters  here */
 		//				{ "MG", "Martin", "Grabarczyk", "Dental Tribe", placeHolderImg, "Round 1" },
 		//		{ "HR", "Hazel", "Rivera", "Cavity Tribe", placeHolderImg, "Round 2" },
 		//				{ "MF", "Manor", "Freeman", "Dental Tribe",placeHolderImg, "Round 3" },
 		//				{ "DH", "Delerina", "Hill", "Plaque Tribe", placeHolderImg, "Not Eliminated" },
 		//				{ "JW", "Jeff", "Westaway", "Dental Tribe", placeHolderImg, "Not Eliminated" },
 		//				{ "LC", "Liam", "Corrigan", "Plaque Tribe", placeHolderImg, "Not Eliminated" },
 		//		};
 		/* 
 		 * (non-Javadoc)
 		 * @see javax.swing.table.TableModel#getColumnCount()
 		 */
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		public int getRowCount() {
 			return data.length;
 		}
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 		/*
 		 * Default renderer/editor for this cell.
 		 */
 		public Class getColumnClass(int c) {
 			return getValueAt(0,c).getClass();
 		}
 		/*
 		 * This method makes the tables editable
 		 */
 		//		public boolean isCellEditable(int row, int col) {
 		//Note that the cell address is constant
 		//		if(col < 2) {
 		//			return false;
 		//		} else {
 		//			return true;			
 		//		}
 		//	}
 	}
 
 	//		 /*
 	//         * Don't need to implement this method unless your table's
 	//         * data can change.
 	//         */
 	//        public void setValueAt(Object value, int row, int col) {
 	//            if (DEBUG) {
 	//                System.out.println("Setting value at " + row + "," + col
 	//                                   + " to " + value
 	//                                   + " (an instance of "
 	//                                   + value.getClass() + ")");
 	//            }
 	// 
 	//            data[row][col] = value;
 	//            // Normally, one should call fireTableCellUpdated() when
 	//            // a value is changed.  However, doing so in this demo
 	//            // causes a problem with TableSorter.  The tableChanged()
 	//            // call on TableSorter that results from calling
 	//            // fireTableCellUpdated() causes the indices to be regenerated
 	//            // when they shouldn't be.  Ideally, TableSorter should be
 	//            // given a more intelligent tableChanged() implementation,
 	//            // and then the following line can be uncommented.
 	//            // fireTableCellUpdated(row, col);
 	// 
 	//            if (DEBUG) {
 	//                System.out.println("New value of data:");
 	//                printDebugData();
 	//            }
 	//        }
 	// 
 	//        private void printDebugData() {
 	//            int numRows = getRowCount();
 	//            int numCols = getColumnCount();
 	// 
 	//            for (int i=0; i < numRows; i++) {
 	//                System.out.print("    row " + i + ":");
 	//                for (int j=0; j < numCols; j++) {
 	//                    System.out.print("  " + data[i][j]);
 	//                }
 	//                System.out.println();
 	//            }
 	//            System.out.println("--------------------------");
 	//        }
 	/**
 	 * To Format the fields and labels
 	 * @param font
 	 * @param color
 	 * @return
 	 */
 	protected JComponent setGameFont(Font font, Color color) {
 		this.textInputFieldFont = font;
 		this.textInputFieldColor = color;
 
 		JPanel panel = new JPanel();
 
 		//    	id.setFont(font);
 		//    	id.setForeground(color);
 		//    	
 		//    	first.setFont(font);
 		//    	first.setForeground(color);
 		//    	
 		//    	last.setFont(font);
 		//    	last.setForeground(color);
 		//    	
 		return panel;
 	}
 	protected Font getGameFont() {
 		return textInputFieldFont;
 	}
 	protected Color getGameFontColor() {
 		return textInputFieldColor;
 	}
 
 	//    /**
 	//     * Create the GUI and show it.  For thread safety,
 	//     * this method should be invoked from the
 	//     * event-dispatching thread.
 	//     */
 	//    private static void createAndShowGUI() {
 	//        //Create and set up the window.
 	//        JFrame frame = new JFrame("Contestant List Test");
 	//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	// 
 	//        //Create and set up the content pane.
 	//        ContestantListGUI newContentPane = new ContestantListGUI();
 	//        newContentPane.setOpaque(true); //content panes must be opaque
 	//        frame.setContentPane(newContentPane);
 	// 
 	//        //Display the window.
 	//        frame.pack();
 	//        frame.setVisible(true);
 	//    }
 	// 
 	//    public static void main(String[] args) {
 	//        //Schedule a job for the event-dispatching thread:
 	//        //creating and showing this application's GUI.
 	//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
 	//            public void run() {
 	//                createAndShowGUI();
 	//            }
 	//        });
 	//    }
 
 }// End of this Class
