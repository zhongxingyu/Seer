 /********************************************************************
 Program Name:	AdministrateSystemOptionManagement.java
 Description:	A AdministrateSystemOptionManagement class that is the 
 				startup UI for GR user(s) to Administrate the service Options.
 Done by:		Lee Kai Quan
 Admin No:		114173S
 Module Group:	IT2297-08
 Last Update:	6-11-2012
 ======================================================================
 Completition:	1) left retrive meal, package, ballroom, facility(DONE)
 				2) formate the download/load/temp file
 				3) formate the method names
 				4) progress bar?
 ***********************************************************************/
 package View.SOM;
 
 import javax.swing.*;
 import java.awt.Color;
 import java.awt.BorderLayout;
 import javax.swing.JSplitPane;
 import javax.swing.JButton;
 import javax.swing.ImageIcon;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.Dimension;
 import java.awt.SystemColor;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.border.BevelBorder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.JTabbedPane;
 import javax.swing.JProgressBar;
 import java.awt.ComponentOrientation;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Stack;
 
 import javax.swing.JPopupMenu;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.SwingConstants;
 import java.awt.Toolkit;
 import java.io.IOException;
 
 import Controller.SOM.AdministrateBallroomControl;
 import Controller.SOM.AdministrateEntertainmentControl;
 import Controller.SOM.AdministrateFacilityControl;
 import Controller.SOM.AdministrateMealControl;
 import Controller.SOM.AdministratePackageControl;
 import Controller.SOM.CSVController;
 
 public class AdministrateServiceOptionManagement {
 	
 	/********************************************************
 	 *					The Attributes
 	 *******************************************************/
 	private JFrame jFrame = null;  
 	private JPanel jContentPane = null;
 	private JMenuBar jJMenuBar = null;
 	private JMenu jMenu_File = null;
 	private JPanel jPanel_toolbar = null;
 	private JSplitPane jSplitPane_Divider = null;
 	private static JPanel jPanel_tabs = null;
 	private JPanel jPanel_Search_Wrapper = null;
 	private JButton jButton_NewTab = null;
 	private JButton jButton_open = null;
 	private JButton jButton_save = null;
 	private JButton jButton_undo = null;
 	private JButton jButton_redo = null;
 	private JToolBar jJToolBarBar = null;
 	private JToggleButton jToggleButton_search = null;
 	private JPanel jPanel_search_header = null;
 	private JLabel jLabel = null;
 	private JScrollPane jScrollPane_table_Wrapper = null;
 	private JTable jTable = null;
 	private static JPanel jPanel_tab_header = null;
 	private static JTabbedPane jTabbedPane = null;
 	private JScrollPane jScrollPane = null;
 	private static JProgressBar jProgressBar = null;
 	private JButton jButton_logout = null;
 	private JButton jButton_search = null;
 	@SuppressWarnings("unchecked")
 	private JComboBox jComboBox_search = null;
 	private JTextField jTextField_search = null;
 	final JFileChooser fc = new JFileChooser();  //  @jve:decl-index=0:visual-constraint="1063,298"
 	protected DefaultTableModel model= new DefaultTableModel();//for the jTable
 	private JPopupMenu jPopupMenu = null;  //  @jve:decl-index=0:visual-constraint="209,861"
 	private JMenuItem jMenuItem_retrive = null;
 	private Thread main=null;
 	private Thread progress=null;
 	
 	@SuppressWarnings("unchecked")
 	protected static LinkedList tabs=null;
 	@SuppressWarnings("unchecked")
 	protected Stack undoManager  =null;
 	private AdministratePackageForm packages=null;
 	private AdministrateEntertainmentForm entertainment=null;
 	private AdministrateBallroomForm ballroom=null;
 	private AdministrateFacilityForm facility=null;
 	private AdministrateMealForm meal= null;  //  @jve:decl-index=0:
 	/********************************************************
 	 *					Start of UI
 	 *******************************************************/
 	public JFrame getJFrame() {
 		if (jFrame == null) {
 			jFrame = new JFrame();
 			jFrame.setSize(new Dimension(1033, 625));//625 on expend
 			jFrame.setLocation(206, 49);	// set the starting window location(set it to center)
 			jFrame.setResizable(false);	//disallow user to resize the frame
 			jFrame.setTitle("Welcome...");	//set the title to the user name of current user after logging in
 			jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Images/SOM/purchase.png")));
 			jFrame.setJMenuBar(getJJMenuBar());
 			jFrame.setContentPane(getJContentPane());
 		}
 		return jFrame;
 	}
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new BorderLayout());
 			jContentPane.add(getJPanel_toolbar(), BorderLayout.NORTH);
 			jContentPane.add(getJSplitPane_Divider(), BorderLayout.CENTER);
 		}
 		return jContentPane;
 	}
 	
 	/********************************************************
 	 *				Start of Menubar
 	 *******************************************************/
 	private JMenuBar getJJMenuBar() {
 		if (jJMenuBar == null) {
 			jJMenuBar = new JMenuBar();
 			jJMenuBar.setBackground(SystemColor.controlHighlight);
 			jJMenuBar.add(getJMenu_File());
 		}
 		return jJMenuBar;
 	}
 	private JMenu getJMenu_File() {
 		if (jMenu_File == null) {
 			jMenu_File = new JMenu();
 			jMenu_File.setText("File");
 		}
 		return jMenu_File;
 	}
 
 	
 	
 	
 	/********************************************************
 	 *				Start of Toolbar
 	 *******************************************************/
 	private JPanel getJPanel_toolbar() {
 		if (jPanel_toolbar == null) {
 			jPanel_toolbar = new JPanel();
 			jPanel_toolbar.setLayout(new BorderLayout());
 			jPanel_toolbar.setPreferredSize(new Dimension(0,40));
 			jPanel_toolbar.setBackground(Color.white);
 			jPanel_toolbar.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
 			jPanel_toolbar.add(getJJToolBarBar(), BorderLayout.CENTER);
 		}
 		return jPanel_toolbar;
 	}
 	private JToolBar getJJToolBarBar() {
 		if (jJToolBarBar == null) {
 			jJToolBarBar = new JToolBar();
 			jJToolBarBar.setFloatable(false);
 			jJToolBarBar.setBackground(SystemColor.control);
 			jJToolBarBar.add(getJButton_NewTab());
 			jJToolBarBar.add(getJButton_open());
 			jJToolBarBar.add(getJButton_save());
 			jJToolBarBar.add(getJToggleButton_search());
 			jJToolBarBar.add(getJButton_undo());
 			jJToolBarBar.add(getJButton_redo());
 			jJToolBarBar.add(getJButton_logout());
 			jJToolBarBar.add(getJButton_logout());
 		}
 		return jJToolBarBar;
 	}
 	private JButton getJButton_NewTab() {
 		if (jButton_NewTab == null) {
 			jButton_NewTab = new JButton();
 			jButton_NewTab.setToolTipText("Opens a new Tab Options");
 			jButton_NewTab.setFocusPainted(false);;
 			jButton_NewTab.setPreferredSize(new Dimension(30,35));
 			jButton_NewTab.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/new.png")));
 			jButton_NewTab.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					main = new Thread () {
 						  public void run () {
 							  newTab();
 						  }
 					  };
 					progress= new Thread(){
 						  @SuppressWarnings("deprecation")
 						public void run(){
 							  double increment=1;
 							  int sleep=300;
 								 for (int i =  0; i <= 100; i+=increment) {
 								      final int percent = i;
 								      try {
 								        SwingUtilities.invokeLater(new Runnable() {
 								         public void run() {
 								        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 								          }
 								        });
 								        Thread.sleep(sleep);
 								        if(!main.isAlive()){
 								        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								        	break;
 										 }
 								        sleep+=100;
 								        
 								       
 								      } catch (InterruptedException e) {
 								    	  
 								      }
 								    } 
 								 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								 this.stop();
 								 this.interrupt();
 						  }
 					  };
 					  progress.start();
 					  main.start(); 
 				}
 			});
 		}
 		return jButton_NewTab;
 	}
 	private JButton getJButton_open() {
 		if (jButton_open == null) {
 			jButton_open = new JButton();
 			jButton_open.setToolTipText("Loads information from a .R file");
 			jButton_open.setFocusPainted(false);
 			jButton_open.setPreferredSize(new Dimension(30,35));
 			jButton_open.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/open.png")));
 			jButton_open.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					fc.showOpenDialog(fc);
 					final String path=fc.getSelectedFile().toString();
 					main = new Thread () {
 						  public void run () {
 							  try {int i=loadCSV(path);
 							  if(i==11)
 								  JOptionPane.showMessageDialog(null, "This file is empty!\nPlease load a correct file", "Error", JOptionPane.WARNING_MESSAGE);
 							  if(i==0)
 								  JOptionPane.showMessageDialog(null, "This file is of a different format\nPlease load a correct file", "Warnning", JOptionPane.WARNING_MESSAGE);
 							  
 							  }
 								catch (IOException e1) {}
 						  }
 					  };
 					progress= new Thread(){
 						  @SuppressWarnings("deprecation")
 						public void run(){
 							  double increment=1;
 							  int sleep=300;
 								 for (int i =  0; i <= 100; i+=increment) {
 								      final int percent = i;
 								      try {
 								        SwingUtilities.invokeLater(new Runnable() {
 								         public void run() {
 								        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 								          }
 								        });
 								        Thread.sleep(sleep);
 								        if(!main.isAlive()){
 								        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								        	break;
 										 }
 								        sleep+=100;
 								        
 								       
 								      } catch (InterruptedException e) {
 								    	
 								      }
 								    } 
 								 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								 this.stop();
 								 this.interrupt();
 						  }
 					  };
 					  progress.start();
 					  main.start(); 
 				}
 			});
 		}
 		return jButton_open;
 	}
 	
 	private JButton getJButton_save() {
 		if (jButton_save == null) {
 			jButton_save = new JButton();
 			jButton_save.setFocusPainted(false);
 			jButton_save.setToolTipText("Does Nothing...");
 			jButton_save.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/save.png")));
 			jButton_save.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					
 				}
 			});
 		}
 		return jButton_save;
 	}
 	private JToggleButton getJToggleButton_search() {
 		if (jToggleButton_search == null) {
 			jToggleButton_search = new JToggleButton();
 			jToggleButton_search.setToolTipText("Toggles the search pannel");
 			jToggleButton_search.setFocusPainted(false);
 			jToggleButton_search.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/search.png")));
 			jToggleButton_search.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if(getJSplitPane_Divider().getDividerLocation()==0){
 						getJSplitPane_Divider().setDividerLocation(170);
 						jToggleButton_search.setSelected(true);
 						getJFrame().setSize(new Dimension(1033, 625));
 						return;
 					}
 					if(getJSplitPane_Divider().getDividerLocation()!=0){
 						jToggleButton_search.setSelected(false);
 						getJSplitPane_Divider().setDividerLocation(0);
 						return;
 					}
 				}
 			});
 		}
 		return jToggleButton_search;
 	}
 	private JButton getJButton_undo() {
 		if (jButton_undo == null) {
 			jButton_undo = new JButton();
 			jButton_undo.setToolTipText("Does Nothing...");
 			jButton_undo.setFocusPainted(false);
 			jButton_undo.setMnemonic(KeyEvent.VK_UNDEFINED);
 			jButton_undo.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/undo.png")));
 		}
 		return jButton_undo;
 	}
 	
 	private JButton getJButton_redo() {
 		if (jButton_redo == null) {
 			jButton_redo = new JButton();
 			jButton_redo.setToolTipText("Does Nothing...");
 			jButton_redo.setFocusPainted(false);
 			jButton_redo.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/redo.png")));
 		}
 		return jButton_redo;
 	}
 	private JButton getJButton_logout() {
 		if (jButton_logout == null) {
 			jButton_logout = new JButton();
 			jButton_logout.setToolTipText("Logs out of the system");
 			jButton_logout.setFocusPainted(false);
 			jButton_logout.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/logout.png")));
 			jButton_logout.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getJFrame().dispose();
 				}
 			});
 		}
 		return jButton_logout;
 	}
 	/********************************************************
 	 *				End of Toolbar
 	 *******************************************************/
 
 	private JSplitPane getJSplitPane_Divider() {
 		if (jSplitPane_Divider == null) {
 			jSplitPane_Divider = new JSplitPane();
 			jSplitPane_Divider.setOneTouchExpandable(false);
 			jSplitPane_Divider.setDividerLocation(0);// open is 170 30 to see the textbox
 			jSplitPane_Divider.setBottomComponent(getJPanel_tabs());
 			jSplitPane_Divider.setTopComponent(getJPanel_Search_Wrapper());
 			jSplitPane_Divider.setOrientation(JSplitPane.VERTICAL_SPLIT);
 			jSplitPane_Divider.addComponentListener(new java.awt.event.ComponentAdapter() {
 				public void componentResized(java.awt.event.ComponentEvent e) {
 					
 				}
 			});
 		}
 		return jSplitPane_Divider;
 	}
 	
 	
 	/********************************************************
 	 *			Start of search panel(s)
 	 *******************************************************/
 	private JPanel getJPanel_Search_Wrapper() {
 		if (jPanel_Search_Wrapper == null) {
 			jPanel_Search_Wrapper = new JPanel();
 			jPanel_Search_Wrapper.setLayout(new BorderLayout());
 			jPanel_Search_Wrapper.setBackground(SystemColor.control);
 			jPanel_Search_Wrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.darkGray));
 			jPanel_Search_Wrapper.add(getJPanel_search_header(), BorderLayout.NORTH);
 			jPanel_Search_Wrapper.add(getJScrollPane_table_Wrapper(), BorderLayout.CENTER);
 			
 		}
 		return jPanel_Search_Wrapper;
 	}
 	private JPanel getJPanel_search_header() {
 		if (jPanel_search_header == null) {
 			jLabel = new JLabel();
 			jLabel.setText("");
 			jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
 			jLabel.setBounds(new Rectangle(92, 0, 177, 27));
 			jPanel_search_header = new JPanel();
 			jPanel_search_header.setLayout(null);
 			jPanel_search_header.setPreferredSize(new Dimension(0,30));
 			jPanel_search_header.setBorder(BorderFactory.createLineBorder(SystemColor.activeCaptionBorder, 1));
 			jPanel_search_header.setBackground(SystemColor.controlHighlight);
 			jPanel_search_header.add(jLabel, null);
 			jPanel_search_header.add(getJButton_search(), null);
 			jPanel_search_header.add(getJComboBox_search(), null);
 			jPanel_search_header.add(getJTextField_search(), null);
 		}
 		return jPanel_search_header;
 	}
 
 	private JScrollPane getJScrollPane_table_Wrapper() {
 		if (jScrollPane_table_Wrapper == null) {
 			jScrollPane_table_Wrapper = new JScrollPane();
 			jScrollPane_table_Wrapper.setPreferredSize(new Dimension(0,140));
 			jScrollPane_table_Wrapper.setMinimumSize(new Dimension(0,140));
 			jScrollPane_table_Wrapper.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			jScrollPane_table_Wrapper.setViewportView(getJTable());
 			jScrollPane_table_Wrapper.setBackground(SystemColor.control);
 		}
 		return jScrollPane_table_Wrapper;
 	}
 	private JTable getJTable() {
 		if (jTable == null) {
 			jTable = new JTable(model);
 			jTable.setOpaque(false);
 			jTable.setEnabled(false);
 			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 			jTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable.setIntercellSpacing(new Dimension(5, 5));
 			jTable.setRowSelectionAllowed(true);
 			jTable.setShowGrid(false);
 			jTable.setFocusable(false);
 			jTable.setColumnSelectionAllowed(false);
 			jTable.addMouseListener(new MouseAdapter() {   
 				public void mouseClicked(java.awt.event.MouseEvent e) {    
 					if (e.getClickCount() == 2){
 						Thread main = new Thread () {
 							  public void run () {
 								  String ID=getJTable().getValueAt(getJTable().getSelectedRow(), 0).toString();
 									if(getJTable().getName().equals("entertainment")){
 										retrieveEntertainmentByID(ID);
 									}
 									else if(getJTable().getName().equals("meal")){
 										retrieveMealByID(ID);
 									}
 									else if(getJTable().getName().equals("facility")){
 										retrieveFacilityByID(ID);
 									}
 									else if(getJTable().getName().equals("ballroom")){
 										retrieveBallroomByID(ID);
 									}
 									else if(getJTable().getName().equals("package")){
 										retrievePackageByID(ID);
 									}
 							  }
 						  };
 						  final Thread a=main;
 						Thread progress= new Thread(){
 							  @SuppressWarnings("deprecation")
 							public void run(){
 								  double increment=1;
 								  int sleep=300;
 									 for (int i =  0; i <= 100; i+=increment) {
 									      final int percent = i;
 									      try {
 									        SwingUtilities.invokeLater(new Runnable() {
 									         public void run() {
 									        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 									          }
 									        });
 									        Thread.sleep(sleep);
 									        if(!a.isAlive()){
 									        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 									        	break;
 											 }
 									        sleep+=100;
 									      } catch (InterruptedException e) {
 									      }
 									    } 
 									 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 									 this.stop();
 									 this.interrupt();
 							  }
 						  };
 						  progress.start();
 						  main.start(); 
 				    }
 					
 				}
 		        @Override
 		        public void mouseReleased(MouseEvent e) {
 		            int r = jTable.rowAtPoint(e.getPoint());
 		            if (r >= 0 && r < jTable.getRowCount()) {
 		            	jTable.setRowSelectionInterval(r, r);
 		            } else {
 		            	jTable.clearSelection();
 		            }
 
 		            int rowindex = jTable.getSelectedRow();
 		            if (rowindex < 0)
 		                return;
 		            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 		                JPopupMenu popup = getJPopupMenu();
 		                popup.show(e.getComponent(), e.getX(), e.getY());
 		            }
 		        }});
 		}
 		return jTable;
 	}
 	private JPopupMenu getJPopupMenu() {
 		if (jPopupMenu == null) {
 			jPopupMenu = new JPopupMenu();
 			jPopupMenu.setOpaque(false);
 			jPopupMenu.setSize(new Dimension(99, 168));
 			jPopupMenu.add(getJMenuItem_retrive());
 		}
 		return jPopupMenu;
 	}
 	private JMenuItem getJMenuItem_retrive() {
 		if (jMenuItem_retrive == null) {
 			jMenuItem_retrive = new JMenuItem();
 			jMenuItem_retrive.setText("Show Details");
 			jMenuItem_retrive.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					Thread main = new Thread () {
 						  public void run () {
 							  String ID=getJTable().getValueAt(getJTable().getSelectedRow(), 0).toString();
 								if(getJTable().getName().equals("entertainment")){
 									retrieveEntertainmentByID(ID);
 								}
 								else if(getJTable().getName().equals("meal")){
 									retrieveMealByID(ID);
 								}
 								else if(getJTable().getName().equals("facility")){
 									retrieveFacilityByID(ID);
 								}
 								else if(getJTable().getName().equals("ballroom")){
 									retrieveBallroomByID(ID);
 								}
 								else if(getJTable().getName().equals("package")){
 									retrievePackageByID(ID);
 								}
 						  }
 					  };
 					  final Thread a=main;
 					Thread progress= new Thread(){
 						  @SuppressWarnings("deprecation")
 						public void run(){
 							  double increment=1;
 								 for (int i =  0; i <= 100; i+=increment) {
 								      final int percent = i;
 								      int sleep=300;
 								      try {
 								        SwingUtilities.invokeLater(new Runnable() {
 								         public void run() {
 								        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 								          }
 								        });
 								        Thread.sleep(sleep);
 								        if(!a.isAlive()){
 								        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								        	break;
 										 }
 								        sleep+=100;
 								       
 								      } catch (InterruptedException e) {
 								    	  
 								      }
 								    } 
 								 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								 this.stop();
 								 this.interrupt();
 						  }
 					  };
 					  progress.start();
 					  main.start(); 
 				}
 			});
 		}
 		return jMenuItem_retrive;
 	}
 	/********************************************************
 	 *			Start of tab panel(s)
 	 *******************************************************/
 	private static JPanel getJPanel_tabs() {
 		if (jPanel_tabs == null) {
 			jPanel_tabs = new JPanel();
 			jPanel_tabs.setLayout(new BorderLayout());
 			jPanel_tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.black));
 			jPanel_tabs.setBackground(SystemColor.control);
 			jPanel_tabs.add(getJPanel_tab_header(), BorderLayout.NORTH);
 			jPanel_tabs.add(getJTabbedPane(), BorderLayout.CENTER);
 		}
 		return jPanel_tabs;
 	}
 	protected static JPanel getJPanel_tab_header() {
 		if (jPanel_tab_header == null) {
 			jPanel_tab_header = new JPanel();
 			jPanel_tab_header.setLayout(new BorderLayout());
 			jPanel_tab_header.setPreferredSize(new Dimension(0,20));
 			jPanel_tab_header.setBackground(SystemColor.controlHighlight);
 			jPanel_tab_header.setBorder(BorderFactory.createLineBorder(SystemColor.activeCaptionBorder, 1));
 			jPanel_tab_header.add(getJProgressBar(), BorderLayout.CENTER);
 		}
 		return jPanel_tab_header;
 	}
 	public static JProgressBar getJProgressBar() {
 		if (jProgressBar == null) {
 			jProgressBar = new JProgressBar();
 			jProgressBar.setOpaque(true);
 			jProgressBar.setValue(0);
 			jProgressBar.setMaximum(100);
 			jProgressBar.setMinimum(0);
 		}
 		return jProgressBar;
 	}
 	protected static JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.setBackground(null);
 			jTabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTabbedPane.setForeground(Color.black);
 			jTabbedPane.setOpaque(true);
 			jTabbedPane.setFocusable(false);
 		}
 		return jTabbedPane;
 	}
 	/********************************************************
 	 *			End of tab panel(s)
 	 *******************************************************/
 	
 
 	private JButton getJButton_search() {
 		if (jButton_search == null) {
 			jButton_search = new JButton();
 			jButton_search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jButton_search.setBounds(new Rectangle(848, 3, 173, 25));
 			jButton_search.setFocusable(false);
 			jButton_search.setFocusPainted(false);
 			jButton_search.setText("Search");
 			jButton_search.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					 Thread main = new Thread () {
 						  public void run () {
 							//the search retreive function here
 								if(getJComboBox_search().getSelectedIndex()==0){
 									JOptionPane.showMessageDialog(null, "Please Select An Search Parameter", "Warnning", JOptionPane.WARNING_MESSAGE);
 									getJComboBox_search().requestFocus();
 								}
 								else if(getJComboBox_search().getSelectedIndex()==1){
 									if(getJTextField_search().getText().equals("")||getJTextField_search().getText().equals("   Enter Search Parameter")){
 										retrievePackage();
 									}
 									else{
 										retrievePackage(getJTextField_search().getText());
 									}
 									
 								}
 								else if(getJComboBox_search().getSelectedIndex()==2){
 									if(getJTextField_search().getText().equals("")||getJTextField_search().getText().equals("   Enter Search Parameter")){
 										retrieveFaciity();
 									}
 									else{
 										retrieveFacility(getJTextField_search().getText());
 									}
 								}
 								else if(getJComboBox_search().getSelectedIndex()==3){
 									if(getJTextField_search().getText().equals("")||getJTextField_search().getText().equals("   Enter Search Parameter")){
 										retrieveBallroom();
 									}
 									else{
 										retrieveBallroom(getJTextField_search().getText());
 									}
 								}
 								else if(getJComboBox_search().getSelectedIndex()==4){
 									if(getJTextField_search().getText().equals("")||getJTextField_search().getText().equals("   Enter Search Parameter")){
 										retrieveEntertainment();
 									}
 									else{
 										retrieveEntertainment(getJTextField_search().getText());
 									}
 								}
 								else if(getJComboBox_search().getSelectedIndex()==5){
 									if(getJTextField_search().getText().equals("")||getJTextField_search().getText().equals("   Enter Search Parameter")){
 										retrieveMeal();
 									}
 									else{
 										retrieveMeal(getJTextField_search().getText());
 									}
 								}
 						  }
 					  };
 					  final Thread a=main;
 					 Thread progress= new Thread(){
 						  @SuppressWarnings("deprecation")
 						public void run(){
 							  double increment=1;
 								 for (int i =  0; i <= 100; i+=increment) {
 								      final int percent = i;
 								     int sleep=500;
 								      try {
 								        SwingUtilities.invokeLater(new Runnable() {
 								         public void run() {
 								        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 								          }
 								        });
 								        Thread.sleep(sleep);
 								        if(!a.isAlive()){
 								        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								        	break;
 										 }
 								        sleep+=100;
 								       
 								      } catch (InterruptedException e) {
 								    	  
 								      }
 								    } 
 								 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 								 this.stop();
 								 this.interrupt();
 						  }
 					  };
 					  progress.start();
 					  main.start(); 
 				}
 			});
 		}
 		return jButton_search;
 	}
 	@SuppressWarnings("unchecked")
 	private JComboBox getJComboBox_search() {
 		if (jComboBox_search == null) {
 			jComboBox_search = new JComboBox();
 			jComboBox_search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jComboBox_search.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jComboBox_search.setFocusable(false);
 			jComboBox_search.addItem("Select A Search Parameter");
 			jComboBox_search.addItem("Packages");
 			jComboBox_search.addItem("Facility");
 			jComboBox_search.addItem("Ballroom");
 			jComboBox_search.addItem("Entertainment");
 			jComboBox_search.addItem("Meal");
 			jComboBox_search.setBounds(new Rectangle(405, 3, 224, 25));
 		}
 		return jComboBox_search;
 	}
 	private JTextField getJTextField_search() {
 		if (jTextField_search == null) {
 			jTextField_search = new JTextField();
 			jTextField_search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_search.setForeground(SystemColor.scrollbar);
 			jTextField_search.setText("   Enter Search Parameter");
 			jTextField_search.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_search.setBounds(new Rectangle(631, 3, 215, 25));
 			jTextField_search.addFocusListener(new java.awt.event.FocusAdapter() {   
 				public void focusLost(java.awt.event.FocusEvent e) {    
 					if(jTextField_search.getText().equals("")){
 						jTextField_search.setForeground(SystemColor.scrollbar);
 						jTextField_search.setText("   Enter Search Parameter");	
 					}
 				}
 				public void focusGained(java.awt.event.FocusEvent e) {
 					if(jTextField_search.getText().equals("   Enter Search Parameter")){
 						jTextField_search.setForeground(SystemColor.black);
 						jTextField_search.setText("");
 					}
 				}
 			});
 		}
 		return jTextField_search;
 	}
 	/********************************************************
 	 *					End of UI
 	 *******************************************************/
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/********************************************************
 	 *					CUSTOM METHODS
 	 *******************************************************/
 	
 	
 	/********************************************************
 	 * Method Name : newTab
 	 * Input Parameter : nil 
 	 * Purpose : To Create a new Tab.. Sub into various steps
 	 * Return :nil
 	 *******************************************************/
 	private void newTab(){
 		createTabContent();	//This method creats and sets the NewTab Content..
 		createTabHeader(jTabbedPane.getTabCount()-1);	//This method creats and sets the NewTab header..
 	}
 	
 	/********************************************************
 	 * Method Name : createTabHeader
 	 * Input Parameter : int
 	 * Purpose : To create and set the custom Tab Header.
 	 * Return : void
 	 *******************************************************/
 	private void createTabHeader(int index){
 		CustomTabHeader tab=new CustomTabHeader(jTabbedPane);
 		jTabbedPane.setTabComponentAt( index,tab );
 	}
 	
 	/********************************************************
 	 * Method Name : createTabContent
 	 * Input Parameter : nil 
 	 * Purpose : To create and set the new Tab content.
 	 * Return :nil
 	 *******************************************************/
 	private void createTabContent(){
 		
 		
 		JButton jButton_Package = new JButton();
 		jButton_Package.setFont(new Font("Segoe UI", Font.PLAIN, 14));
 		jButton_Package.setText("New Package Record");
 		jButton_Package.setOpaque(false);
 		jButton_Package.setFocusPainted(false);
 		jButton_Package.setFocusCycleRoot(false);
 		jButton_Package.setFocusable(false);
 		jButton_Package.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/package.png")));
 		jButton_Package.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				 Thread main = new Thread () {
 					  public void run () {
 						  newPackageTab();	//Creats a new Package Tab
 						  System.out.println("ended main");
 					  }
 				  };
 				  final Thread a=main;
 				  Thread progress= new Thread(){
 					  @SuppressWarnings("deprecation")
 					public void run(){
 						  double increment=1;
 							 for (int i =  0; i <= 100; i+=increment) {
 							      final int percent = i;
 							     int sleep=500;
 							      try {
 							        SwingUtilities.invokeLater(new Runnable() {
 							         public void run() {
 							        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 							          }
 							        });
 							        Thread.sleep(sleep);
 							        if(!a.isAlive()){
 							        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							        	break;
 									 }
 							        sleep+=100;
 							       
 							      } catch (InterruptedException e) {
 							    	  AdministrateServiceOptionManagement.getJProgressBar().setIndeterminate(true);
 							      }
 							    } 
 							 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							 this.stop();
 							 this.interrupt();
 					  }
 				  };
 				  progress.start();
 				  main.start(); 
 			}
 		});
 		
 		JToolBar jToolBar_Package = new JToolBar();
 		jToolBar_Package.setFloatable(false);
 		jToolBar_Package.setOpaque(false);
 		jToolBar_Package.setBounds(new Rectangle(648, 120, 219, 56));
 		jToolBar_Package.add(jButton_Package);
 		
 		
 		
 		JButton jButton_Entertainment = new JButton();
 		jButton_Entertainment.setFont(new Font("Segoe UI", Font.PLAIN, 14));
 		jButton_Entertainment.setFocusable(false);
 		jButton_Entertainment.setOpaque(false);
 		jButton_Entertainment.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/entertainment.png")));
 		jButton_Entertainment.setText("New Entertainment Record");
 		jButton_Entertainment.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				 Thread main = new Thread () {
 					  public void run () {
 						  newEntertainmentTab();// creates a new entertainment tab
 						  System.out.println("ended main");
 					  }
 				  };
 				  final Thread a=main;
 				  Thread progress= new Thread(){
 					  @SuppressWarnings("deprecation")
 					public void run(){
 						  double increment=1;
 						 int sleep=500;
 							 for (int i =  0; i <= 100; i+=increment) {
 							      final int percent = i;
 							      try {
 							        SwingUtilities.invokeLater(new Runnable() {
 							         public void run() {
 							        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 							          }
 							        });
 							        Thread.sleep(sleep);
 							        if(!a.isAlive()){
 							        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							        	break;
 									 }
 							        sleep+=100;
 							      } catch (InterruptedException e) {
 							    	  AdministrateServiceOptionManagement.getJProgressBar().setIndeterminate(true);
 							      }
 							    } 
 							 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							 this.stop();
 							 this.interrupt();
 					  }
 				  };
 				  progress.start();
 				  main.start(); 
 			}
 		});
 		
 		JToolBar jToolBar_Entertainment = new JToolBar();
 		jToolBar_Entertainment.setFloatable(false);
 		jToolBar_Entertainment.setOpaque(false);
 		jToolBar_Entertainment.setBounds(new Rectangle(380, 120, 257, 56));
 		jToolBar_Entertainment.add(jButton_Entertainment);
 		
 		JButton jButton_Meal = new JButton();
 		jButton_Meal.setOpaque(false);
 		jButton_Meal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
 		jButton_Meal.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/meal.png")));
 		jButton_Meal.setText("New Meal Record");
 		jButton_Meal.setFocusable(false);
 		jButton_Meal.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				 Thread main = new Thread () {
 					  public void run () {
 						  newMealTab(); // Creats a new Meal Tab
 						  System.out.println("ended main");
 					  }
 				  };
 				  final Thread a=main;
 				  Thread progress= new Thread(){
 					  @SuppressWarnings("deprecation")
 					public void run(){
 						  double increment=1;
 						 int sleep=500;
 							 for (int i =  0; i <= 100; i+=increment) {
 							      final int percent = i;
 							      try {
 							        SwingUtilities.invokeLater(new Runnable() {
 							         public void run() {
 							        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 							          }
 							        });
 							        Thread.sleep(sleep);
 							        if(!a.isAlive()){
 							        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							        	break;
 									 }
 							        sleep+=100;
 							      } catch (InterruptedException e) {
 							    	  AdministrateServiceOptionManagement.getJProgressBar().setIndeterminate(true);
 							      }
 							    } 
 							 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							 this.stop();
 							 this.interrupt();
 					  }
 				  };
 				  progress.start();
 				  main.start(); 
 			}
 		});
 		
 		JToolBar jToolBar_Meal = new JToolBar();
 		jToolBar_Meal.setFloatable(false);
 		jToolBar_Meal.setOpaque(false);
 		jToolBar_Meal.setBounds(new Rectangle(181, 120, 190, 56));
 		jToolBar_Meal.add(jButton_Meal);
 		
 		JButton jButton_Facility = new JButton();
 		jButton_Facility.setFont(new Font("Segoe UI", Font.PLAIN, 14));
 		jButton_Facility.setText("Register Facility");
 		jButton_Facility.setFocusable(false);
 		jButton_Facility.setOpaque(false);
 		jButton_Facility.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/facility.png")));
 		jButton_Facility.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				 Thread main = new Thread () {
 					  public void run () {
 						  newFacilityTab();	//Creats a new facility Tab
 						  System.out.println("ended main");
 					  }
 				  };
 				  final Thread a=main;
 				  Thread progress= new Thread(){
 					  @SuppressWarnings("deprecation")
 					public void run(){
 						  double increment=1;
 						 int sleep=500;
 							 for (int i =  0; i <= 100; i+=increment) {
 							      final int percent = i;
 							      try {
 							        SwingUtilities.invokeLater(new Runnable() {
 							         public void run() {
 							        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 							          }
 							        });
 							        Thread.sleep(sleep);
 							        if(!a.isAlive()){
 							        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							        	break;
 									 }
 							        sleep+=100;
 							      } catch (InterruptedException e) {
 							    	  AdministrateServiceOptionManagement.getJProgressBar().setIndeterminate(true);
 							      }
 							    } 
 							 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							 this.stop();
 							 this.interrupt();
 					  }
 				  };
 				  progress.start();
 				  main.start(); 
 			}
 		});
 		
 		JToolBar jToolBar_Facility = new JToolBar();
 		jToolBar_Facility.setFloatable(false);
 		jToolBar_Facility.setOpaque(false);
 		jToolBar_Facility.setBounds(new Rectangle(289, 190, 189, 56));
 		jToolBar_Facility.add(jButton_Facility);
 		
 		JButton jButton_ballroom = new JButton();
 		jButton_ballroom.setFont(new Font("Segoe UI",Font.PLAIN, 14));
 		jButton_ballroom.setText("New Ballroom Record");
 		jButton_ballroom.setFocusable(false);
 		jButton_ballroom.setOpaque(false);
 		jButton_ballroom.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/ballroom.png")));
 		jButton_ballroom.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				 Thread main = new Thread () {
 					  public void run () {
 						  newBallroomTab();	//Creats a new ballroom Tab
 						  System.out.println("ended main");
 					  }
 				  };
 				  final Thread a=main;
 				  Thread progress= new Thread(){
 					  @SuppressWarnings("deprecation")
 					public void run(){
 						  double increment=1;
 						 int sleep=500;
 							 for (int i =  0; i <= 100; i+=increment) {
 							      final int percent = i;
 							      try {
 							        SwingUtilities.invokeLater(new Runnable() {
 							         public void run() {
 							        	 AdministrateServiceOptionManagement.getJProgressBar().setValue(percent);
 							          }
 							        });
 							        Thread.sleep(sleep);
 							        if(!a.isAlive()){
 							        	AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							        	break;
 									 }
 							        sleep+=150;
 							      } catch (InterruptedException e) {
 							    	  AdministrateServiceOptionManagement.getJProgressBar().setIndeterminate(true);
 							      }
 							    } 
 							 AdministrateServiceOptionManagement.getJProgressBar().setValue(100);
 							 this.stop();
 							 this.interrupt();
 					  }
 				  };
 				  progress.start();
 				  main.start(); 
 			}
 		});
 		
 		JToolBar jToolBar_Ballroom = new JToolBar();
 		jToolBar_Ballroom.setFloatable(false);
 		jToolBar_Ballroom.setOpaque(false);
 		jToolBar_Ballroom.setBounds(new Rectangle(539, 190, 218, 56));
 		jToolBar_Ballroom.add(jButton_ballroom);
 		
 		JPanel jPanel = new JPanel();
 		jPanel = new JPanel();
 		jPanel.setLayout(null);
 		jPanel.setSize(new Dimension(1000,290));
 		jPanel.setPreferredSize(new Dimension(1000,290));
 		jPanel.add(jToolBar_Meal, null);
 		jPanel.add(jToolBar_Entertainment, null);
 		jPanel.add(jToolBar_Facility, null);
 		jPanel.add(jToolBar_Ballroom, null);
 		jPanel.add(jToolBar_Package, null);
 		
 		jScrollPane = new JScrollPane(jPanel);
 		jScrollPane.setSize(new Dimension(1021, 303));
 		jScrollPane.setViewportView(jPanel);
 		jScrollPane.setPreferredSize(new Dimension(1021, 303));
 
 		jTabbedPane.add("New",jScrollPane);
 	}
 	/********************************************************
 	 * Method Name : newEntertainmentTab
 	 * Input Parameter : nil 
 	 * Purpose : To create and set a new Entertinment Tab content.
 	 * Return : nil
 	 *******************************************************/
 	private void newEntertainmentTab(){
 		entertainment= new AdministrateEntertainmentForm();
 		if(jTabbedPane.getTabCount()==1){
 			jTabbedPane.insertTab("Create Entertainment Form",null , entertainment.getJScrollPane(),null , 1); // sets the content
 			createTabHeader(1);	//sets the custom tab header
 			jTabbedPane.remove(0);
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Create Entertainment Form",null , entertainment.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			jTabbedPane.remove(jTabbedPane.getSelectedIndex());
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount()-1)){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()-1);
 				}
 			}
 	}
 	
 	/********************************************************
 	 * Method Name : newMealTab
 	 * Input Parameter : nil 
 	 * Purpose : To create and set a new Meal Tab content.
 	 * Return : nil
 	 *******************************************************/
 	public void newMealTab(){
 		meal=new AdministrateMealForm();
 		if(jTabbedPane.getTabCount()==1){
 			jTabbedPane.insertTab("Create Meal Form",null , meal.getJScrollPane(),null , 1); // sets the content
 			createTabHeader(1);	//sets the custom tab header
 			jTabbedPane.remove(0);
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Create Meal Form",null , meal.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			jTabbedPane.remove(jTabbedPane.getSelectedIndex());
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount()-1)){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()-1);
 				}
 		}
 	}
 	
 	/********************************************************
 	 * Method Name : newFacilityTab
 	 * Input Parameter : nil 
 	 * Purpose : To create and set a new Facility Tab content.
 	 * Return : nil
 	 *******************************************************/
 	public void newFacilityTab(){	
 		facility=new AdministrateFacilityForm();
 		if(jTabbedPane.getTabCount()==1){
 			jTabbedPane.insertTab("Create Facility Form",null , facility.getJScrollPane(),null , 1); // sets the content
 			createTabHeader(1);	//sets the custom tab header
 			jTabbedPane.remove(0);
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Create Facility Form",null , facility.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			jTabbedPane.remove(jTabbedPane.getSelectedIndex());
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount()-1)){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()-1);
 				}
 			}
 	}
 	
 	/********************************************************
 	 * Method Name : newBallroomTab
 	 * Input Parameter : nil 
 	 * Purpose : To create and set a new Ballroom Tab content.
 	 * Return : nil
 	 *******************************************************/
 	@SuppressWarnings("unchecked")
 	public void newBallroomTab(){
 		ballroom=new AdministrateBallroomForm();
 		AdministrateFacilityControl control= new AdministrateFacilityControl();
 		ballroom.getJComboBox_facilityName().setModel(control.processRetrieveFacilityNames());
 		if(jTabbedPane.getTabCount()==1){
 			jTabbedPane.insertTab("Create Ballroom Form",null , ballroom.getJScrollPane(),null , 1); // sets the content
 			createTabHeader(1);	//sets the custom tab header
 			jTabbedPane.remove(0);
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Create Ballroom Form",null , ballroom.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			jTabbedPane.remove(jTabbedPane.getSelectedIndex());
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount()-1)){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()-1);
 				}
 			}
 	}
 	
 	/********************************************************
 	 * Method Name : newPackageTab
 	 * Input Parameter : nil 
 	 * Purpose : To create and set a new Package Tab content.
 	 * Return : nil
 	 *******************************************************/
 	public void newPackageTab(){	
 		packages=new AdministratePackageForm();
 		if(jTabbedPane.getTabCount()==1){
 			jTabbedPane.insertTab("Create Package Form",null , packages.getJScrollPane(),null , 1); // sets the content
 			createTabHeader(1);	//sets the custom tab header
 			jTabbedPane.remove(0);
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Create Package Form",null , packages.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			jTabbedPane.remove(jTabbedPane.getSelectedIndex());
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount()-1)){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()-1);
 				}
 			}
 	}
 	
 	 /********************************************************
 	  * Method Name : retrieveEntertainment
 	  * Input Parameter : NIL
 	  * Purpose : To retrieve all Entertainment record where isRecord =0
 	  * Return :Void
 	  *******************************************************/
 	@SuppressWarnings("static-access")
 	public void retrieveEntertainment(){
 		AdministrateEntertainmentControl control= new AdministrateEntertainmentControl();
 		model=control.processRetrieveEntertainment();
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().setAutoResizeMode(getJTable().AUTO_RESIZE_OFF);
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(30);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(910);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(80);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(1020);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("entertainment");
 	}
 	
 	/********************************************************
 	  * Method Name : retrieveEntertainment
 	  * Input Parameter : String paramater
 	  * Purpose : To retrieve all Entertainment record where isRecord =0 and like parameter
 	  * Return :void
 	  *******************************************************/
 	@SuppressWarnings("static-access")
 	public void retrieveEntertainment(String parameter){
 		AdministrateEntertainmentControl control= new AdministrateEntertainmentControl();
 		model=control.processRetrieveEntertainment(parameter);
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().setAutoResizeMode(getJTable().AUTO_RESIZE_OFF);
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(30);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(700);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(80);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(700);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("entertainment");
 	}
 
 	/********************************************************
 	  * Method Name : retrieveEntertainmenByID
 	  * Input Parameter : String ID
 	  * Purpose : To retrieve and display a Entertainment record where id=ID
 	  * Return :void
 	  *******************************************************/
 	public void retrieveEntertainmentByID(String ID){
 		AdministrateEntertainmentControl control= new AdministrateEntertainmentControl();
 		control.processRetrieveEntertainmentByID(ID);
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		
 		//sets the entertainment tab
 		entertainment= new AdministrateEntertainmentForm();
 		entertainment.getJTextField_entertaimentID().setText(control.getEntertainment().getEntertainmentID());
 		entertainment.getJCheckBox_entertainmentAvailability().setSelected(control.getEntertainment().isEntertainmentAvailability());
 		entertainment.getJTextField_entertainmentTitle().setText(control.getEntertainment().getEntertainmentTitle());
 		entertainment.getJTextField_entertainmentTitle().setForeground(SystemColor.black);
 		entertainment.getJTextArea_entertainmentDescription().setText(control.getEntertainment().getEntertainmentDescription());
 		entertainment.getJTextArea_entertainmentDescription().setForeground(SystemColor.black);
 		
 		entertainment.getJTextField_entertainmentDiscount().setText(fmt.format(control.getEntertainment().getEntertainmentDiscount())+"%");
 		entertainment.getJSlider_entertainmentDiscount().setValue((int) control.getEntertainment().getEntertainmentDiscount());
 		entertainment.getJButton_createEntertainment().setEnabled(false);
 		entertainment.getJButton_Update().setEnabled(true);
 		entertainment.getJButton_delete().setEnabled(true);
 		entertainment.getJButton_download().setEnabled(true);
 
 		DefaultTableModel model= new DefaultTableModel();
 		model=control.getModel();
 		
 		entertainment.model=model;
 		entertainment.displaySummary();
 		entertainment.getJTextField_entertainmentTotalPrice().setText(fmt.format(entertainment.calculateFinalPrice()));
 		
 		entertainment.getJTable_entertainmentMenu().setModel(entertainment.model);
 		entertainment.getJTable_entertainmentMenu().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		if(jTabbedPane.getTabCount()==0){
 			jTabbedPane.insertTab("Entertainment "+control.getEntertainment().getEntertainmentID(),null , entertainment.getJScrollPane(),null , 0); // sets the content
 			createTabHeader(0);	//sets the custom tab header
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Entertainment "+control.getEntertainment().getEntertainmentID(),null , entertainment.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 				}
 			}
 	}
 	
 	/********************************************************
 	  * Method Name : retrieveMeal
 	  * Input Parameter : NIL
 	  * Purpose : To retrieve all Meal record where isRecord =0
 	  * Return :Void
 	  *******************************************************/
 	public void retrieveMeal(){
 		AdministrateMealControl control= new AdministrateMealControl();
 		model=control.processRetrieveMeal();
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(750);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(110);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(110);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(800);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("meal");
 	}
 	
 	/********************************************************
 	  * Method Name : retrieveMeal
 	  * Input Parameter : String paramater
 	  * Purpose : To retrieve all Meal record where isRecord =0 and like parameter
 	  * Return :void
 	  *******************************************************/
 	public void retrieveMeal(String parameter){
 		AdministrateMealControl control= new AdministrateMealControl();
 		model=control.processRetrieveMeal(parameter);
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(750);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(110);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(110);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(800);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("meal");
 	}
 
 	/********************************************************
 	  * Method Name : retrieveMealByID
 	  * Input Parameter : String ID
 	  * Purpose : To retrieve and display a Meal record where id=ID
 	  * Return :void
 	  *******************************************************/
 	public void retrieveMealByID(String ID){
 		AdministrateMealControl control = new AdministrateMealControl();
 		control.procesRetrieveMealByID(ID);
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		
 		//sets the Meal tab
 		meal= new AdministrateMealForm();
 		meal.getJTextField_mealID().setText(control.getMeal().getMealID());
 		meal.getJCheckBox_mealAvailability().setSelected(control.getMeal().isMealAvailability());
 		meal.getJTextField_mealTitle().setText(control.getMeal().getMealTitle());
 		meal.getJTextField_mealTitle().setForeground(SystemColor.black);
 		meal.getJTextArea_mealDescription().setText(control.getMeal().getMealDescription());
 		meal.getJTextArea_mealDescription().setForeground(SystemColor.black);
 		meal.getJComboBox_mealType().setSelectedItem(control.getMeal().getMealType());
 		meal.getJTextField_mealDiscount().setText(fmt.format(control.getMeal().getMealDiscount())+"%");
 		meal.getJSlider_mealDiscount().setValue((int)control.getMeal().getMealDiscount());
 		meal.getJButton_delete().setEnabled(true);
 		meal.getJButton_update().setEnabled(true);
 		meal.getJButton_download().setEnabled(true);
 		meal.getJButton_upload().setEnabled(false);
 		
 		DefaultTableModel model= new DefaultTableModel();
 		model=control.getModel();
 		
 		meal.model=model;
 		meal.displaySummary();
 		meal.getJTextField_mealTotalPricePerHead().setText(fmt.format(meal.calculateFinalPrice()));
 		
 		meal.getJTable_mealMenu().setModel(model);
 		meal.getJTable_mealMenu().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		meal.getJTable_mealMenu().getColumnModel().getColumn(0).setPreferredWidth(565);
 		meal.getJTable_mealMenu().getColumnModel().getColumn(1).setPreferredWidth(135);
 		meal.getJTable_mealMenu().getColumnModel().getColumn(2).setPreferredWidth(100);
 		meal.getJTable_mealMenu().getColumnModel().getColumn(3).setPreferredWidth(100);
 		meal.getJTable_mealMenu().getColumnModel().getColumn(4).setPreferredWidth(500);
 		if(jTabbedPane.getTabCount()==0){
 			jTabbedPane.insertTab("Meal "+control.getMeal().getMealID(),null , meal.getJScrollPane(),null , 0); // sets the content
 			createTabHeader(0);	//sets the custom tab header
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Meal "+control.getMeal().getMealID(),null , meal.getJScrollPane(),null , jTabbedPane.getSelectedIndex()); // sets the content
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	//sets the custom tab header
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 				}
 			}
 	}
 	
 	/********************************************************
 	 * Method Name		: retrieveBallroom
 	 * Input Parameter 	: void
 	 * Return 			: void
 	 * Purpose 			: To retrieve all Ballroom record
 	 * 					  where is not purchase record
 	 *******************************************************/
 	public void retrieveBallroom(){
 		AdministrateBallroomControl control= new AdministrateBallroomControl();
 		model=control.processRetrieveBallroom();
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(710);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(170);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(90);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(760);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("ballroom");
 	}
 	
 	/********************************************************
 	 * Method Name		: retrieveBallroom
 	 * Input Parameter 	: String
 	 * Return 			: void
 	 * Purpose 			: To retrieve all Ballroom record
 	 * 					  where is not purchase record
 	 * 					  & by input paramater
 	 *******************************************************/
 	public void retrieveBallroom(String parameter){
 		AdministrateBallroomControl control= new AdministrateBallroomControl();
 		model=control.processRetrieveBallroom(parameter);
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(710);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(170);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(90);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(760);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("ballroom");
 	}
 	
 	/********************************************************
 	 * Method Name		: retrieveBallroomByID
 	 * Input Parameter 	: String
 	 * Return 			: void
 	 * Purpose 			: To retrieve a Ballroom record
 	 * 					  by ballroomID
 	 *******************************************************/
 	@SuppressWarnings("unchecked")
 	public void retrieveBallroomByID(String ID){
 		AdministrateBallroomControl control= new AdministrateBallroomControl();
 		control.processRetrieveBallroomByID(ID);
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		
 		//set the ballroom tab
 		ballroom= new AdministrateBallroomForm();
 		AdministrateFacilityControl fControl= new AdministrateFacilityControl();
 		ballroom.getJComboBox_facilityName().setModel(fControl.processRetrieveFacilityNames());
 		
 		fControl.processRetrieveFacilityByID(control.getBallroom().getFacilityID());
 		ballroom.getJComboBox_facilityName().setName(fControl.getFacility().getFacilityID());
 		ballroom.getJComboBox_facilityName().setSelectedItem(fControl.getFacility().getFacilityName());
 		ballroom.getJTextArea_facilityAddress().setText(fControl.getFacility().getFacilityAddress());
 		ballroom.getJTextField_facilityContact().setText(fControl.getFacility().getFacilityContact());
 		
 		ballroom.getJTextField_ballroomID().setText(control.getBallroom().getBallroomID());
 		ballroom.getJCheckBox_ballroomAvailability().setSelected(control.getBallroom().isBallroomAvailability());
 		ballroom.getJTextField_ballroomTitle().setText(control.getBallroom().getBallroomName());
 		ballroom.getJTextField_ballroomTitle().setForeground(SystemColor.black);
 		ballroom.getJComboBox_ballroomSize().setSelectedItem(control.getBallroom().getBallroomSize());
 		ballroom.getJTextField_ballroomPrice().setText(fmt.format(control.getBallroom().getBallroomPrice()));
 		ballroom.getJTextField_ballroomPrice().setForeground(SystemColor.black);
 		ballroom.getJTextArea_ballroomDescription().setText(control.getBallroom().getBallroomDescription());
 		ballroom.getJTextArea_ballroomDescription().setForeground(SystemColor.black);
 		ballroom.getJTextField_ballroomFinalPrice().setText(fmt.format(control.getBallroom().getBallroomFinalPrice()));
 		ballroom.getJSlider_ballroomDiscount().setValue((int) control.getBallroom().getBallroomDiscount());
 		ballroom.getJTextField_ballroomDiscount().setText(fmt.format(control.getBallroom().getBallroomDiscount()));
 		
 		ballroom.getJButton_upload().setEnabled(false);
 		ballroom.getJButton_delete().setEnabled(true);
 		ballroom.getJButton_update().setEnabled(true);
 		ballroom.getJButton_download().setEnabled(true);
 		ballroom.displaySummary();
 		
 		if(jTabbedPane.getTabCount()==0){
 			jTabbedPane.insertTab("Ballroom "+control.getBallroom().getBallroomID(),null , ballroom.getJScrollPane(),null , 0); 
 			createTabHeader(0);	
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Ballroom "+control.getBallroom().getBallroomID(),null , ballroom.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 				}
 			}
 		
 	}
 	
 	 /********************************************************
 	  * Method Name : retrieveFacility
 	  * Input Parameter : NIL
 	  * Purpose : To retrieve all facility record
 	  * Return :Void
 	  *******************************************************/
 	public void retrieveFaciity(){
 		AdministrateFacilityControl control= new AdministrateFacilityControl();
 		model=control.processRetrieveFacility();
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(975);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(600);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(300);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(120);
 		getJTable().getColumnModel().getColumn(5).setPreferredWidth(975);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("facility");
 	}
 	
 	 /********************************************************
 	  * Method Name : retrieveFacility
 	  * Input Parameter : String
 	  * Purpose : To retrieve all facility record where like string
 	  * Return :Void
 	  *******************************************************/
 	public void retrieveFacility(String parameter){
 		AdministrateFacilityControl control= new AdministrateFacilityControl();
 		model=control.processRetrieveFacility(parameter);
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(975);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(600);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(300);
 		getJTable().getColumnModel().getColumn(4).setPreferredWidth(120);
 		getJTable().getColumnModel().getColumn(5).setPreferredWidth(975);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("facility");
 	}
 	
 	/********************************************************
 	  * Method Name 	: retrieveFacilityByID()
 	  * Input Parameter : String 
 	  * Return 			: void
 	  * Purpose 		: To retrieve a facility record by ID
 	  *******************************************************/
 	public void retrieveFacilityByID(String ID){
 		AdministrateFacilityControl control= new AdministrateFacilityControl();
 		control.processRetrieveFacilityByID(ID);
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		
 		//set the facility tab
 		facility= new AdministrateFacilityForm();
 		
 		facility.getJTextField_facilityID().setText(control.getFacility().getFacilityID().toString());
 		facility.getJTextField_facilityContact().setText(control.getFacility().getFacilityContact().toString());
 		facility.getJTextField_facilityContact().setForeground(SystemColor.black);
 		facility.getJTextField_facilityAddress().setText(control.getFacility().getFacilityAddress().toString());
 		facility.getJTextField_facilityAddress().setForeground(SystemColor.black);
 		facility.getJTextArea_facilityDescription().setText(control.getFacility().getFacilityDescription().toString());
 		facility.getJTextArea_facilityDescription().setForeground(SystemColor.black);
 		facility.getJCheckBox_facilityParking().setSelected(control.getFacility().isFacilityParking());
 		facility.getJTextField_facilityWeekendCost().setText(fmt.format(control.getFacility().getFacilityWeekendExtraCost()));
 		facility.getJTextField_facilityWeekendCost().setForeground(SystemColor.black);
 		facility.getJTextField_facilityName().setText(control.getFacility().getFacilityName().toString());
 		facility.getJTextField_facilityName().setForeground(SystemColor.black);
 		
 		facility.getJButton_delete().setEnabled(true);
 		facility.getJButton_update().setEnabled(true);
 		facility.getJButton_upload().setEnabled(false);
 		facility.getJButton_download().setEnabled(true);
 		
 		facility.model=control.getModel();
 		facility.getJTable_ballroomList().setModel(facility.model);
 		facility.getJTable_ballroomList().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		facility.getJTable_ballroomList().getColumnModel().getColumn(0).setPreferredWidth(440);
 		facility.getJTable_ballroomList().getColumnModel().getColumn(1).setPreferredWidth(135);
 		facility.getJTable_ballroomList().getColumnModel().getColumn(2).setPreferredWidth(130);
 		facility.getJTable_ballroomList().getColumnModel().getColumn(3).setPreferredWidth(135);
 		facility.getJTable_ballroomList().getColumnModel().getColumn(4).setPreferredWidth(563);
 		
 		facility.displaySummary();
 		
 		if(jTabbedPane.getTabCount()==0){
 			jTabbedPane.insertTab("Facility "+control.getFacility().getFacilityID(),null , facility.getJScrollPane(),null , 0); 
 			createTabHeader(0);	
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Facility "+control.getFacility().getFacilityID(),null , facility.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 				}
 			}
 	}
 	
 	
 	/********************************************************
 	 * Method Name		: retrievePackage
 	 * Input Parameter 	: void
 	 * Return 			: void
 	 * Purpose 			: To retrieve all Package record
 	 * 					  where is not purchase record
 	 *******************************************************/
 	public void retrievePackage(){
 		AdministratePackageControl control= new AdministratePackageControl();
 		model=control.processRetrievePackage();
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(975);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(140);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(880);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("package");
 	}
 	
 	/********************************************************
 	 * Method Name		: retrievePackage
 	 * Input Parameter 	: String
 	 * Return 			: void
 	 * Purpose 			: To retrieve all Package record
 	 * 					  where is not purchase record
 	 * 					  & by parameter
 	 *******************************************************/
 	public void retrievePackage(String parameter){
 		AdministratePackageControl control= new AdministratePackageControl();
 		model=control.processRetrievePackage(parameter);
 		getJTable().setModel(model);
 		getJTable().setFont(new Font("Century Gothic", Font.PLAIN, 14));
 		getJTable().getColumnModel().getColumn(0).setPreferredWidth(50);
 		getJTable().getColumnModel().getColumn(1).setPreferredWidth(975);
 		getJTable().getColumnModel().getColumn(2).setPreferredWidth(140);
 		getJTable().getColumnModel().getColumn(3).setPreferredWidth(880);
 		jLabel.setText(model.getRowCount()+" Records Found!");
 		getJTable().setName("package");
 	}
 
 	/********************************************************
 	  * Method Name 	: retrievePackageByID()
 	  * Input Parameter : String 
 	  * Return 			: void
 	  * Purpose 		: To retrieve a Package record by ID
 	  *******************************************************/
 	public void retrievePackageByID(String ID){
 		AdministratePackageControl control= new AdministratePackageControl();
 		control.processRetrievePackageByID(ID);
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		//setting the fields
 		packages= new AdministratePackageForm();
 		packages.getJTextField_packageID().setText(control.getPack().getPackageID());
 		packages.getJTextField_packageTitle().setText(control.getPack().getPackageTitle());
 		packages.getJTextField_packageTitle().setForeground(SystemColor.black);
 		packages.getJTextArea_packageDescription().setForeground(SystemColor.black);
 		packages.getJTextArea_packageDescription().setText(control.getPack().getPackageDescription());
 		packages.getJCheckBox_packageAvailability().setSelected(control.getPack().isPackageAvailability());
 		packages.getJTextField_Ballroom().setText(control.getBallroom().getBallroomName());
 		packages.getJTextField_Ballroom().setName(control.getBallroom().getBallroomID());
 		packages.ballroomID=control.getBallroom().getBallroomID();
 		if(control.getEntertainment().getEntertainmentID()!=null){
 			packages.getJTextField_entertainment().setText(control.getEntertainment().getEntertainmentTitle());
 			packages.getJTextField_entertainment().setName(control.getEntertainment().getEntertainmentID());
 			packages.entertainmentID=control.getEntertainment().getEntertainmentID();
 			packages.getJButton_entertainment().setEnabled(true);
 			packages.getJCheckBox_entertainment().setSelected(true);
 		}
 		int numOfMeal=control.getMeals().size();
 		
 		if(numOfMeal>0){
 			packages.getJTextField_mealOption1().setText(control.getMeals().get(0).getMealTitle());
 			packages.getJTextField_mealOption1().setName(control.getMeals().get(0).getMealID());
 			packages.mealOption1ID=control.getMeals().get(0).getMealID();
 			packages.getJButton_mealOption1().setEnabled(true);
 			packages.getJCheckBox_mealOption1().setSelected(true);
 		}
 		if(numOfMeal>1){
 			packages.getJTextField_mealOption2().setText(control.getMeals().get(1).getMealTitle());
 			packages.getJTextField_mealOption2().setName(control.getMeals().get(1).getMealID());
 			packages.mealOption2ID=control.getMeals().get(0).getMealID();
 			packages.getJButton_mealOption2().setEnabled(true);
 			packages.getJCheckBox_mealOption2().setSelected(true);
 		}
 		if(numOfMeal>2){
 			packages.getJTextField_mealOption3().setText(control.getMeals().get(2).getMealTitle());
 			packages.getJTextField_mealOption3().setName(control.getMeals().get(2).getMealID());
 			packages.mealOption3ID=control.getMeals().get(0).getMealID();
 			packages.getJButton_mealOption3().setEnabled(true);
 			packages.getJCheckBox_mealOption3().setSelected(true);
 		}
 		packages.getJTextField_discount().setText(fmt.format(control.getPack().getPackageDiscount()));
 		packages.getJSlider_discount().setValue((int) control.getPack().getPackageDiscount());
 		packages.displaySummary();
 		//set the buttons
 		packages.getJButton_upload().setEnabled(false);
 		packages.getJButton_delete().setEnabled(true);
 		packages.getJButton_update().setEnabled(true);
 		packages.getJButton_download().setEnabled(true);
 		
 		
 		//adds the form into the tab
 		if(jTabbedPane.getTabCount()==0){
 			jTabbedPane.insertTab("Package "+control.getPack().getPackageID(),null , packages.getJScrollPane(),null , 0); 
 			createTabHeader(0);	
 			jTabbedPane.setSelectedIndex(0);
 		}
 		else{
 			jTabbedPane.insertTab("Package "+control.getPack().getPackageID(),null , packages.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 			createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 			if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 				jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 				}
 			}
 	}
 	/********************************************************
 	  * Method Name 	: loadCSV()
 	  * Input Parameter : String 
 	  * Return 			: void
 	  * Purpose 		: To load a csv file data into the form
 	 * @throws IOException 
 	  *******************************************************/
 	@SuppressWarnings("unchecked")
 	public int loadCSV(String path) throws IOException{
 		int result=1;
 		String tabTitle="";
 		//-1 means empty file
 		//0 means wrong format file
 		//1 means all good close the thread
 		CSVController reader= new CSVController();
 		ArrayList<String[]> data= reader.readFile(path);
 		//CHECK IF THE FILE IS EMPTY
 		for(int i=0; i<data.size();i++){
 			for(int x=0;x<data.get(i).length;x++){
 				if(data.get(i)[x]==null)
 					return -1;
 			}
 		}
 		
 		//CHECK THE DATA TYPE IS WAT (PACKAGE, BALLROOM, FACILITY, MEAL, OR ENTERTAINMENT)
 		if(data.get(0)[0].equals("ENTERTAINMENT_ID")){
 			//CHECK FOR THE CORRECT HEADER FORMAT
 			if(!(data.get(0)[0].equals("ENTERTAINMENT_ID")))return 0;
 			if(!(data.get(0)[1].equals("ENTERTAINMENT_TITLE"))) return 0;
 			if(!(data.get(0)[2].equals("ENTERTAINMENT_DESCRIPTION"))) return 0;
 			if(!(data.get(0)[3].equals("ENTERTAINMENT_AVAILABILITY"))) return 0;
 			if(!(data.get(0)[4].equals("ENTERTAINMENT_DISCOUNT"))) return 0;
 			if(!(data.get(0)[5].equals("ENTERTAINMENT_FINAL_PRICE"))) return 0;
 			if(!(data.get(2)[0].equals("ENTERTAINMENT_MENU_NAME"))) return 0;
 			if(!(data.get(2)[1].equals("ENTERTAINMENT_MENU_PRICE"))) return 0;
 			if(!(data.get(2)[2].equals("ENTERTAINMENT_MENU_DESCRIPTION"))) return 0;
 			else{
 				tabTitle="Create Entertainment Form";
 				//LOADS THE DATA
 				//CALLS THE ENTERTAINMENT FORM
 				AdministrateEntertainmentForm form= new AdministrateEntertainmentForm();
 				//CHECKS THE DATABASE FOR AN ID PROJECTED ON THE CSV FILE
 				AdministrateEntertainmentControl control= new AdministrateEntertainmentControl();
 				control.processRetrieveEntertainmentByID(data.get(1)[0]);
 				System.out.println(control.getEntertainment().getEntertainmentDescription());
 				if(control.getEntertainment().getEntertainmentID()!=null){
 					if(!control.getEntertainment().getEntertainmentID().equals(data.get(1)[0])||control.getEntertainment().getEntertainmentID().equals(null)){
 					//INFORMS THE USER THAT THE LOADED DATA DOES NOT REFLECT IN THE CURENT DATABASE
 					JOptionPane.showMessageDialog(null, "This Data does ot exist anyware in the database.", "System Message", JOptionPane.INFORMATION_MESSAGE);
 					
 					}
 					else{
 						//SET THE FORM CONTROLLS AS ACCODINGLY
 						form.getJTextField_entertaimentID().setText(data.get(1)[0]);
 						form.getJButton_createEntertainment().setEnabled(false);
 						form.getJButton_delete().setEnabled(true);
 						form.getJButton_download().setEnabled(true);
 						form.getJButton_Update().setEnabled(true);
 						tabTitle="Entertainment "+data.get(1)[0];
 					}
 				}
 				//SETTING THE FORM FIELDS ACCORDINGLY
 				form.getJTextField_entertainmentTitle().setText(data.get(1)[1]);
 				form.getJTextField_entertainmentTitle().setForeground(SystemColor.black);
 				form.getJTextArea_entertainmentDescription().setText(data.get(1)[2]);
 				form.getJTextArea_entertainmentDescription().setForeground(SystemColor.black);
 				if(data.get(1)[3].equals("YES"))
 					form.getJCheckBox_entertainmentAvailability().setSelected(true);
 				if(data.get(1)[3].equals("NO"))
 						form.getJCheckBox_entertainmentAvailability().setSelected(false);
 				form.getJSlider_entertainmentDiscount().setValue(Integer.parseInt(data.get(1)[4]));
 				form.getJTextField_entertainmentTotalPrice().setText(data.get(1)[5]);
 				
 				//SETTING THE JTABLE IN THE FORM (ENTERTAINMENT MENU TABLE)
 				DefaultTableModel model= new DefaultTableModel();
 				model.setColumnIdentifiers(new String[]{"Entertainment Name","Price/hr","Description"});
 				for(int i=3;i<data.size();i++){
 					model.addRow(data.get(i));
 				}
 				form.getJTable_entertainmentMenu().setModel(model);
 				form.model=model;
 				form.getJTable_entertainmentMenu().getColumnModel().getColumn(0).setPreferredWidth(565);
 				form.getJTable_entertainmentMenu().getColumnModel().getColumn(1).setPreferredWidth(135);
 				form.getJTable_entertainmentMenu().getColumnModel().getColumn(2).setPreferredWidth(698);
 				form.displaySummary();
 				
 				//ADDING THE FORM INTO THE TAB
 				if(jTabbedPane.getTabCount()==0){
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , 0); 
 					createTabHeader(0);	
 					jTabbedPane.setSelectedIndex(0);
 				}
 				else{
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 					createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 					if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 						jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 						}
 					}
 			}
 			result=1;
 		}
 		
 		
 		
 		else if(data.get(0)[0].equals("FACILITY_ID")){
 			tabTitle="";
 			//CHECK FOR THE CORRECT HEADER FORMAT
 			if(!data.get(0)[0].equals("FACILITY_ID"))return 0;
 			if(!data.get(0)[1].equals("FACILITY_NAME"))return 0;
 			if(!data.get(0)[2].equals("FACILITY_CONTACT"))return 0;
 			if(!data.get(0)[3].equals("FACILITY_ADDRESS"))return 0;
 			if(!data.get(0)[4].equals("FACILITY_DESCRIPTION"))return 0;
 			if(!data.get(0)[5].equals("FACILITY_PARKING"))return 0;
 			if(!data.get(0)[6].equals("FACILITY_WEEKEND_SURCHARGE"))return 0;
 			if(!data.get(2)[0].equals("BALLROOM_NAME"))return 0;
 			if(!data.get(2)[1].equals("BALLROOM_PRICE"))return 0;
 			if(!data.get(2)[2].equals("BALLROOM_SIZE"))return 0;
 			if(!data.get(2)[3].equals("BALLROOM_DESCRIPTION"))return 0;
 			
 			else{
 				tabTitle="Create Facility Form";
 				//LOADS THE DATA
 				//CHECKS THE DATABASE FOR AN ID PROJECTED ON THE CSV FILE
 				AdministrateFacilityControl control= new AdministrateFacilityControl();
 				control.processRetrieveFacilityByID(data.get(1)[0]);
 				AdministrateFacilityForm form= new AdministrateFacilityForm();
 				if(control.getFacility().getFacilityID()!=null){
 					if(!control.getFacility().getFacilityID().equals(data.get(1)[0])||control.getFacility().getFacilityID().equals(null)){
 					//INFORMS THE USER THAT THE LOADED DATA DOES NOT REFLECT IN THE CURENT DATABASE
 					JOptionPane.showMessageDialog(null, "This Data does ot exist anyware in the database.", "System Message", JOptionPane.INFORMATION_MESSAGE);
 					
 					}
 					else{
 						//SETS THE FORM CONTROLL ACCORDINGLY
 						form.getJTextField_facilityID().setText(data.get(1)[0]);
 						form.getJButton_upload().setEnabled(false);
 						form.getJButton_delete().setEnabled(true);
 						form.getJButton_download().setEnabled(true);
 						form.getJButton_update().setEnabled(true);
 						tabTitle="Facility "+data.get(1)[0];
 					}
 				}
 				//SETTING THE FORM FIELDS ACCORDLIGLY 
 				form.getJTextField_facilityName().setText(data.get(1)[1]);
 				form.getJTextField_facilityName().setForeground(SystemColor.black);
 				form.getJTextField_facilityAddress().setText(data.get(1)[3]);
 				form.getJTextField_facilityAddress().setForeground(SystemColor.black);
 				form.getJTextField_facilityContact().setText(data.get(1)[2]);
 				form.getJTextField_facilityContact().setForeground(SystemColor.black);
 				form.getJTextArea_facilityDescription().setText(data.get(1)[4]);
 				form.getJTextArea_facilityDescription().setForeground(SystemColor.black);
 				if(data.get(1)[5].equals("YES"))
 					form.getJCheckBox_facilityParking().setSelected(true);
 				if(data.get(1)[5].equals("NO"))
 					form.getJCheckBox_facilityParking().setSelected(false);
 				form.getJTextField_facilityWeekendCost().setText(data.get(1)[6]);
 				
 				//SETTING THE JTABLE IN THE FORM (BALLROOM LIST)
 				DefaultTableModel model= new DefaultTableModel();
 				model.setColumnIdentifiers(new String[]{"Ballroom Name","Price","Entitled Discount","Size","Description"});
 				for(int i=3;i<data.size();i++){
 					model.addRow(data.get(i));
 				}
 				form.getJTable_ballroomList().setModel(model);
 				form.model=model;
 				form.getJTable_ballroomList().getColumnModel().getColumn(0).setPreferredWidth(440);
 				form.getJTable_ballroomList().getColumnModel().getColumn(1).setPreferredWidth(135);
 				form.getJTable_ballroomList().getColumnModel().getColumn(2).setPreferredWidth(135);
 				form.getJTable_ballroomList().getColumnModel().getColumn(3).setPreferredWidth(135);
 				form.getJTable_ballroomList().getColumnModel().getColumn(4).setPreferredWidth(563);
 				form.displaySummary();
 				
 				//ADDING THE FORM INTO THE TAB
 				if(jTabbedPane.getTabCount()==0){
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , 0); 
 					createTabHeader(0);	
 					jTabbedPane.setSelectedIndex(0);
 				}
 				else{
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 					createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 					if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 						jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 						}
 					}
 			}
 			result=1;
 				
 			
 		}
 		else if(data.get(0)[0].equals("BALLROOM_ID")){
 			//CHECK THE HEADER FORMAT
 			if(!data.get(0)[0].equals("BALLROOM_ID"))return 0;
 			if(!data.get(0)[1].equals("FACILITY_NAME"))return 0;
 			if(!data.get(0)[2].equals("FACILITY_CONTACT"))return 0;
 			if(!data.get(0)[3].equals("FACILITY_ADDRESS"))return 0;
 			if(!data.get(0)[4].equals("BALLROOM_AVAILABILITY"))return 0;
 			if(!data.get(0)[5].equals("BALLROOM_NAME"))return 0;
 			if(!data.get(0)[6].equals("BALLROOM_SIZE"))return 0;
 			if(!data.get(0)[7].equals("BALLROOM_PRICE"))return 0;
 			if(!data.get(0)[8].equals("BALLROOM_DESCRIPTION"))return 0;
 			if(!data.get(0)[9].equals("BALLROOM_DISCOUNT"))return 0;
 			if(!data.get(0)[10].equals("BALLROOM_FINAL_PRICE"))return 0;
 			
 			else{
 				tabTitle="Create Ballroom Form";
 				//LOADS THE DATA
 				//CHECKS THE DATABASE FOR AN ID PROJECTED ON THE CSV FILE
 				AdministrateBallroomControl control= new AdministrateBallroomControl();
 				control.processRetrieveBallroomByID(data.get(1)[0]);
 				//CALLS THE BALLROOM FORM
 				AdministrateBallroomForm form= new AdministrateBallroomForm();
 				if(control.getBallroom().getBallroomID()!=null){
 					if(!control.getBallroom().getBallroomID().equals(data.get(1)[0])||control.getBallroom().getBallroomID().equals(null)){
 						//INFORMS THE USER THAT THE LOADED DATA DOES NOT REFLECT IN THE CURENT DATABASE
 						JOptionPane.showMessageDialog(null, "This Data does ot exist anyware in the database.", "System Message", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else{
 						//SETS THE FORM CONTROLL ACCORDINGLY
 						form.getJTextField_ballroomID().setText(data.get(1)[0]);
 						form.getJButton_upload().setEnabled(false);
 						form.getJButton_delete().setEnabled(true);
 						form.getJButton_download().setEnabled(true);
 						form.getJButton_update().setEnabled(true);
 						tabTitle="Ballroom "+data.get(1)[0];
 					}
 				}
 					//set the rest of the fields
 					AdministrateFacilityControl Fcontrol= new AdministrateFacilityControl();
 					form.getJComboBox_facilityName().setModel(Fcontrol.processRetrieveFacilityNames());
 					form.getJComboBox_facilityName().setSelectedItem(data.get(1)[1]);
 					form.getJTextField_facilityContact().setText(data.get(1)[2]);
 					form.getJTextArea_facilityAddress().setText(data.get(1)[3]);
 					if(data.get(1)[4].equals("YES"))
 						form.getJCheckBox_ballroomAvailability().setSelected(true);
 					if(data.get(1)[4].equals("NO"))
 						form.getJCheckBox_ballroomAvailability().setSelected(false);
 					form.getJTextField_ballroomTitle().setText(data.get(1)[5]);
 					form.getJTextField_ballroomTitle().setForeground(SystemColor.black);
 					form.getJComboBox_ballroomSize().setSelectedItem(data.get(1)[6]);
 					form.getJTextField_ballroomPrice().setText(data.get(1)[7]);
 					form.getJTextField_ballroomPrice().setForeground(SystemColor.black);
 					form.getJTextArea_ballroomDescription().setText(data.get(1)[8]);
 					form.getJTextArea_ballroomDescription().setForeground(SystemColor.black);
 					form.getJSlider_ballroomDiscount().setValue(Integer.parseInt(data.get(1)[9]));
 					form.getJTextField_ballroomFinalPrice().setText(data.get(1)[10]);
 					form.displaySummary();
 					//ADDING THE FORM INTO THE TAB
 					if(jTabbedPane.getTabCount()==0){
 						jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , 0); 
 						createTabHeader(0);	
 						jTabbedPane.setSelectedIndex(0);
 					}
 					else{
 						jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 						createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 						if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 							jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 							}
 						}
 			}
 			result=1;
 		}
 		else if(data.get(0)[0].equals("MEAL_ID")){
 			//CHECK THE HEADER FORMAT
 			if(!data.get(0)[0].equals("MEAL_ID"))return 0;
 			if(!data.get(0)[1].equals("MEAL_TITLE"))return 0;
 			if(!data.get(0)[2].equals("MEAL_AVAILABILITY"))return 0;
 			if(!data.get(0)[3].equals("MEAL_DESCRIPTION"))return 0;
 			if(!data.get(0)[4].equals("MEAL_TYPE"))return 0;
 			if(!data.get(0)[5].equals("MEAL_DISCOUNT"))return 0;
 			if(!data.get(0)[6].equals("MEAL_FINAL_PRICE"))return 0;
 			if(!data.get(2)[0].equals("MEAL_MENU_NAME")) return 0;
 			if(!data.get(2)[1].equals("MEAL_MENU_PRICE")) return 0;
 			if(!data.get(2)[2].equals("MEAL_MENU_IS_HALA")) return 0;
 			if(!data.get(2)[3].equals("MEAL_MENU_IS_VEGETARIAN")) return 0;
 			if(!data.get(2)[4].equals("MEAL_MENU_DESCRIPTION")) return 0;
 
 			else{
 				tabTitle="Create Meal Form";
 				//LOADS THE DATA
 				//CHECKS THE DATABASE FOR AN ID PROJECTED ON THE CSV FILE
 				AdministrateMealControl control= new AdministrateMealControl();
 				control.procesRetrieveMealByID(data.get(1)[0]);
 				//CALLS THE FORM
 				AdministrateMealForm form= new AdministrateMealForm();
 				if(control.getMeal().getMealID()!=null){
 					if(!control.getMeal().getMealID().equals(data.get(1)[0])||control.getMeal().getMealID().equals(null)){
 						//INFORMS THE USER THAT THE LOADED DATA DOES NOT REFLECT IN THE CURENT DATABASE
 						JOptionPane.showMessageDialog(null, "This Data does ot exist anyware in the database.", "System Message", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else{
 						//SETS THE FORM CONTROLL ACCORDINGLY
 						form.getJTextField_mealID().setText(data.get(1)[0]);
 						form.getJButton_upload().setEnabled(false);
 						form.getJButton_delete().setEnabled(true);
 						form.getJButton_download().setEnabled(true);
 						form.getJButton_update().setEnabled(true);
 						tabTitle="Meal "+data.get(1)[0];
 					}
 				}
 				//set the fields
 				form.getJTextField_mealTitle().setText(data.get(1)[1]);
 				form.getJTextField_mealTitle().setForeground(SystemColor.black);
 				if(data.get(1)[2].equals("YES"))
 					form.getJCheckBox_mealAvailability().setSelected(true);
 				if(data.get(1)[2].equals("NO"))
 					form.getJCheckBox_mealAvailability().setSelected(false);
 				form.getJTextArea_mealDescription().setText(data.get(1)[3]);
 				form.getJTextArea_mealDescription().setForeground(SystemColor.black);
 				form.getJComboBox_mealType().setSelectedItem(data.get(1)[4]);
 				form.getJSlider_mealDiscount().setValue(Integer.parseInt(data.get(1)[5]));
 				
 				//SETTING THE JTABLE IN THE FORM (MEAL LIST)
 				DefaultTableModel model= new DefaultTableModel();
 				model.setColumnIdentifiers(new String[]{"Meal Name","Price/hr","Halal","Vegetarian","Description"});
 				for(int i=3;i<data.size();i++){
 					model.addRow(data.get(i));
 				}
 				form.getJTable_mealMenu().setModel(model);
 				form.model=model;
 				form.getJTable_mealMenu().getColumnModel().getColumn(0).setPreferredWidth(565);
 				form.getJTable_mealMenu().getColumnModel().getColumn(1).setPreferredWidth(135);
 				form.getJTable_mealMenu().getColumnModel().getColumn(2).setPreferredWidth(100);
 				form.getJTable_mealMenu().getColumnModel().getColumn(3).setPreferredWidth(100);
 				form.getJTable_mealMenu().getColumnModel().getColumn(4).setPreferredWidth(500);
 				form.displaySummary();
 				
 				//ADDING THE FORM INTO THE TAB
 				if(jTabbedPane.getTabCount()==0){
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , 0); 
 					createTabHeader(0);	
 					jTabbedPane.setSelectedIndex(0);
 				}
 				else{
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 					createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 					if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 						jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 						}
 					}
 		}
 		result=1;
 		}
 		else if(data.get(0)[0].equals("PACKAGE_ID")){
 			//CHECK THE HEADER FORMAT
 			if(!data.get(0)[0].equals("PACKAGE_ID"))return 0;
 			if(!data.get(0)[1].equals("PACKAGE_TITLE"))return 0;
 			if(!data.get(0)[2].equals("PACKAGE_AVAILABILITY"))return 0;
 			if(!data.get(0)[3].equals("PACKAGE_DESCRIPTION"))return 0;
 			if(!data.get(0)[4].equals("BALLROOM_ID"))return 0;
 			if(!data.get(0)[5].equals("BALLROOM_NAME"))return 0;
 			if(!data.get(0)[6].equals("ENTERTAINMENT_ID"))return 0;
 			if(!data.get(0)[7].equals("ENTERTAINMENT_NAME"))return 0;
 			if(!data.get(0)[8].equals("MEAL_OPTION1_ID"))return 0;
 			if(!data.get(0)[9].equals("MEAL_OPTION1_NAME"))return 0;
 			if(!data.get(0)[10].equals("MEAL_OPTION2_ID"))return 0;
 			if(!data.get(0)[11].equals("MEAL_OPTION2_NAME"))return 0;
 			if(!data.get(0)[12].equals("MEAL_OPTION3_ID"))return 0;
 			if(!data.get(0)[13].equals("MEAL_OPTION3_NAME"))return 0;
 			if(!data.get(0)[14].equals("PACKAGE_DISCOUNT"))return 0;
 	
 			else{
 				tabTitle="Create Package Form";
 				//LOADS THE DATA
 				//CHECKS THE DATABASE FOR AN ID PROJECTED ON THE CSV FILE
 				AdministratePackageControl control= new AdministratePackageControl();
 				control.processRetrievePackageByID(data.get(1)[0]);
 				//CALLS THE PACKAGE FORM
 				AdministratePackageForm form= new AdministratePackageForm();
 				if(control.getPack().getPackageID()!=null){
 					if(!control.getPack().getPackageID().equals(data.get(1)[0])||control.getPack().getPackageID().equals(null)){
 						//INFORMS THE USER THAT THE LOADED DATA DOES NOT REFLECT IN THE CURENT DATABASE
 						JOptionPane.showMessageDialog(null, "This Data does ot exist anyware in the database.", "System Message", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else{
 						//SETS THE FORM CONTROLL ACCORDINGLY
 						form.getJTextField_packageID().setText(data.get(1)[0]);
 						form.getJButton_upload().setEnabled(false);
 						form.getJButton_delete().setEnabled(true);
 						form.getJButton_download().setEnabled(true);
 						form.getJButton_update().setEnabled(true);
 						tabTitle="Package "+data.get(1)[0];
 					}
 				}
 				//set the fieelds
 				form.getJTextField_packageTitle().setText(data.get(1)[1]);
 				form.getJTextField_packageTitle().setForeground(SystemColor.black);
 				if(data.get(1)[2].equals("YES"))
 					form.getJCheckBox_packageAvailability().setSelected(true);
 				if(data.get(1)[2].equals("NO"))
 					form.getJCheckBox_packageAvailability().setSelected(false);
 				form.getJTextArea_packageDescription().setText(data.get(1)[3]);
 				form.getJTextArea_packageDescription().setForeground(SystemColor.black);
 				form.getJTextField_Ballroom().setName(data.get(1)[4]);
 				form.getJTextField_Ballroom().setText(data.get(1)[5]);
 				form.ballroomID=data.get(1)[4];
 				
				if(!(data.get(1)[6].equals(null))||!(data.get(1)[6].equals(""))||(data.get(1)[6]!=null)){
 					form.getJTextField_entertainment().setName(data.get(1)[6]);
 					form.getJTextField_entertainment().setText(data.get(1)[7]);
 					form.entertainmentID=data.get(1)[6];
 					form.getJCheckBox_entertainment().setSelected(true);
 				}
				if(!(data.get(1)[8].equals(null))||!(data.get(1)[8].equals(""))||(data.get(1)[8]!=null)){
 					form.getJTextField_mealOption1().setName(data.get(1)[8]);
 					form.getJTextField_mealOption1().setText(data.get(1)[9]);
 					form.mealOption1ID=data.get(1)[8];
 					form.getJCheckBox_mealOption1().setSelected(true);
 				}
				if(!(data.get(1)[10].equals(null))||!(data.get(1)[8].equals(""))||(data.get(1)[10]!=null)){
 					form.getJTextField_mealOption2().setName(data.get(1)[10]);
 					form.getJTextField_mealOption2().setText(data.get(1)[11]);
 					form.mealOption2ID=data.get(1)[10];
 					form.getJCheckBox_mealOption2().setSelected(true);
 				}
				if(!(data.get(1)[12].equals(null))||!(data.get(1)[8].equals(""))||(data.get(1)[12]!=null)){
 					form.getJTextField_mealOption3().setName(data.get(1)[12]);
 					form.getJTextField_mealOption3().setText(data.get(1)[13]);
 					form.mealOption3ID=data.get(1)[12];
 					form.getJCheckBox_mealOption3().setSelected(true);
 				}
 				form.getJSlider_discount().setValue(Integer.parseInt(data.get(1)[14]));
 				form.displaySummary();
 				//ADDING THE FORM INTO THE TAB
 				if(jTabbedPane.getTabCount()==0){
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , 0); 
 					createTabHeader(0);	
 					jTabbedPane.setSelectedIndex(0);
 				}
 				else{
 					jTabbedPane.insertTab(tabTitle,null , form.getJScrollPane(),null , jTabbedPane.getSelectedIndex());
 					createTabHeader(jTabbedPane.getSelectedIndex()-1);	
 					if(!(jTabbedPane.getSelectedIndex()==jTabbedPane.getTabCount())){
 						jTabbedPane.setSelectedIndex(jTabbedPane.getSelectedIndex()+1);
 						}
 					}
 			}
 		}
 		return result;
 	}
 }
 
 
 
