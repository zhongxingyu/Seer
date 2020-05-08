 package View.SOM;
 
 import java.awt.Color;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.SystemColor;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableModel;
 
 import View.RIM.InvitationWelcomeView;
 
 import Controller.SOM.AdministrateEntertainmentControl;
 import Controller.SOM.AdministrateMealControl;
 import Controller.SOM.AdministratePackageControl;
 import Controller.SOM.InitiateEventControl;
 import javax.swing.WindowConstants;
 
 
 
 public class InitiateEventForm {
 	/********************************************************
 	 *					The Attributes
 	 *******************************************************/
 	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="22,29"
 	private JPanel jContentPane = null;
 	private JPanel jPanel = null;
 	private static JTextField jTextField_seectedPackageTitle = null;
 	private JButton jButton_selectStandardPackage = null;
 	private static JButton jButton_selectCustomPackage = null;
 	private JTextField jTextField_selectedDate = null;
 	private JButton jButton_selectDate = null;
 	@SuppressWarnings("unchecked")
 	private JComboBox jComboBox_selectTime = null;
 	private JButton jButton_createEvent = null;
 	private JLabel jLabel_step1 = null;
 	private JLabel jLabel_step2 = null;
 	private JFrame jFrame_StandardPackage = null;  //  @jve:decl-index=0:visual-constraint="518,46"
 	private JScrollPane jScrollPane_StandardPackage = null;
 	private JPanel jPanel_standardPackage = null;
 	private JPanel jPanel_seachPanel = null;
 	private JScrollPane jScrollPane_search = null;
 	private JTable jTable_search = null;
 	private JToolBar jToolBar_previous = null;
 	private JButton jButton_previous = null;
 	private JToolBar jToolBar_next = null;
 	private JButton jButton_next = null;
 	private JPanel jPanel_body = null;
 	private JPanel jPanel_package = null;
 	private JTextField jTextField_packageTitle = null;
 	private JScrollPane jScrollPane_packageDescription = null;
 	private JTextArea jTextArea_packageDescription = null;
 	private JLabel jLabel_packageDescription = null;
 	private JToggleButton jToggleButton_viewPackage = null;
 	private JPanel jPanel_ballroom = null;
 	private JPopupMenu jPopupMenu_package = null;  //  @jve:decl-index=0:visual-constraint="495,10"
 	private JMenuItem jMenuItem_retrive_package = null;
 	private JTextField jTextField_ballroomTitle = null;
 	private JToggleButton jToggleButton_viewBallroom = null;
 	private JLabel jLabel_ballroomSize = null;
 	private JLabel jLabel_ballroomFinalPrice = null;
 	private JLabel jLabel_ballroomParking = null;
 	private JLabel jLabel_weekendSurcharge = null;
 	private JLabel jLabel_facilityName = null;
 	private JLabel jLabel_ballroomAddress = null;
 	private JTextField jTextField_ballroomSize = null;
 	private JTextField jTextField_ballroomFinalPrice = null;
 	private JTextField jTextField_ballroomParking = null;
 	private JTextField jTextField_ballroomWeekendSurcharge = null;
 	private JTextField jTextField_facilityName = null;
 	private JScrollPane jScrollPane_facilityAddress = null;
 	private JTextArea jTextArea_facilityAddress = null;
 	private JPanel jPanel_meal = null;
 	private JLabel jLabel_mealOption1 = null;
 	private JLabel jLabel_mealOption2 = null;
 	private JLabel jLabel_mealOption3 = null;
 	private JTextField jTextField_mealOption1 = null;
 	private JScrollPane jScrollPane_mealOption1 = null;
 	private JTable jTable_mealOption1 = null;
 	private JToggleButton jToggleButton_viewMealOption1 = null;
 	private JTextField jTextField_mealOption2 = null;
 	private JToggleButton jToggleButton_viewMealOption2 = null;
 	private JScrollPane jScrollPane_mealOption2 = null;
 	private JTable jTable_mealOption2 = null;
 	private JTextField jTextField_mealOption3 = null;
 	private JToggleButton jToggleButton_viewMealOption3 = null;
 	private JScrollPane jScrollPane_mealOption3 = null;
 	private JTable jTable_mealOption3 = null;
 	private JToolBar jJToolBarBar_StandardPackage = null;
 	private JButton jButton_standardPackage = null;
 	private static JLabel jLabel_step3 = null;
 	private static JTextField jTextField_eventName = null;
 	
 	protected static boolean isStandard=false;
 	protected static boolean isCustom=false;
 	protected static String cPackageTitle=null;
 	protected static String cPackageDescription=null;
 	protected static boolean cPackageAvailability=false;
 	protected static double cDiscount=0.00;
 	protected static String cEntertainmentID=null;
 	protected static String cBallroomID=null;
 	protected static String cMealOption1ID=null;
 	protected static String cMealOption2ID=null;
 	protected static String cMealOption3ID=null;
 	private JScrollPane jScrollPane_eventDescription = null;
 	private JTextArea jTextArea_eventDescription = null;
 	private JPanel jPanel_entertainment = null;
 	private JTextField jTextField_entertainmentTitle = null;
 	private JToggleButton jToggleButton_viewEntertainment = null;
 	private JLabel jLabel_entertainmentMenu = null;
 	private JScrollPane jScrollPane_entertainmentMenu = null;
 	private JTable jTable_enterainementMenu = null;
 	private JLabel jLabel_entertainmentFInalPrice = null;
 	private JTextField jTextField_entertainmentFinalPrice = null;
 	private JPanel jPanel_mealOption1 = null;
 	private JPanel jPanel_mealOption2 = null;
 	private JPanel jPanel_mealOption3 = null;
 	/********************************************************
 	 *					Start of UI
 	 *******************************************************/
 	public JFrame getJFrame() {
 		if (jFrame == null) {
 			jFrame = new JFrame();
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 			jFrame.setSize(new Dimension(485, 546));
 			jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			jFrame.setLocation(screenSize.width/2 - (jFrame.getWidth()/2),screenSize.height/2 - (jFrame.getHeight()/2));
 			jFrame.setTitle("Create Event Form");
 			jFrame.setContentPane(getJContentPane());
 			jFrame.setResizable(false);
 		}
 		return jFrame;
 	}
 	private JPanel getJContentPane() {			
 
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(null);
 			jContentPane.setBackground(SystemColor.control);
 			jContentPane.add(getJPanel(), null);
 		}
 		return jContentPane;
 	}
 	private JPanel getJPanel() {
 		if (jPanel == null) {
 			jLabel_step3 = new JLabel();
 			jLabel_step3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_step3.setBounds(new Rectangle(15, 260, 365, 30));
 			jLabel_step3.setText("Step 3 - Give your event a name and description");
 			jLabel_step2 = new JLabel();
 			jLabel_step2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_step2.setBounds(new Rectangle(20, 140, 365, 30));
 			jLabel_step2.setText("Step 2- Choose your prefered date and time");
 			jLabel_step1 = new JLabel();
 			jLabel_step1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_step1.setBounds(new Rectangle(20, 20, 412, 30));
 			jLabel_step1.setText("Step 1-  Select an available package or customise you very own.");
 			jPanel = new JPanel();
 			jPanel.setLayout(null);
 			jPanel.setBounds(new Rectangle(15, 15, 450, 477));
 			jPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 			jPanel.add(getJTextField_seectedPackageTitle(), null);
 			jPanel.add(getJButton_selectStandardPackage(), null);
 			jPanel.add(getJButton_selectCustomPackage(), null);
 			jPanel.add(getJTextField_selectedDate(), null);
 			jPanel.add(getJButton_selectDate(), null);
 			jPanel.add(getJComboBox_selectTime(), null);
 			jPanel.add(getJButton_createEvent(), null);
 			jPanel.add(jLabel_step1, null);
 			jPanel.add(jLabel_step2, null);
 			jPanel.add(jLabel_step3, null);
 			jPanel.add(getJTextField_eventName(), null);
 			jPanel.add(getJScrollPane_eventDescription(), null);
 		}
 		return jPanel;
 	}
 	protected static JTextField getJTextField_seectedPackageTitle() {
 		if (jTextField_seectedPackageTitle == null) {
 			jTextField_seectedPackageTitle = new JTextField();
 			jTextField_seectedPackageTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_seectedPackageTitle.setEnabled(false);
 			jTextField_seectedPackageTitle.setBounds(new Rectangle(15, 60, 420, 30));
 		}
 		return jTextField_seectedPackageTitle;
 	}
 	private JButton getJButton_selectStandardPackage() {
 		if (jButton_selectStandardPackage == null) {
 			jButton_selectStandardPackage = new JButton();
 			jButton_selectStandardPackage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jButton_selectStandardPackage.setFocusable(false);
 			jButton_selectStandardPackage.setFocusPainted(false);
 			jButton_selectStandardPackage.setBounds(new Rectangle(15, 100, 205, 30));
 			jButton_selectStandardPackage.setText("Select Available Packages");
 			jButton_selectStandardPackage
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							
 							AdministratePackageControl control= new AdministratePackageControl();
 							DefaultTableModel model=control.processRetrievePackage();
 							getJTable_search().setModel(model);
 							jTable_search.getColumnModel().getColumn(0).setPreferredWidth(50);
 							jTable_search.getColumnModel().getColumn(1).setPreferredWidth(450);
 							jTable_search.getColumnModel().getColumn(2).setPreferredWidth(130);
 							jTable_search.getColumnModel().getColumn(3).setPreferredWidth(625);
 							
 							getJToolBar_previous().setVisible(false);
 							getJToolBar_next().setVisible(false);
 							//set the frame location and size settings
 							getJFrame_StandardPackage().setVisible(true);
 							getJFrame_StandardPackage().setSize(new Dimension(675, 237));
 							getJPanel_standardPackage().setPreferredSize(new Dimension(640, 50));
 							
 							getJPanel_package().setPreferredSize(new Dimension(618, 80));
 							getJPanel_package().setMaximumSize(new Dimension(618, 80));
 							getJPanel_package().setMinimumSize(new Dimension(618, 80));
 							
 							getJPanel_ballroom().setMaximumSize(new Dimension(618, 80));
 							getJPanel_ballroom().setPreferredSize(new Dimension(618,80));
 							
 							getJPanel_entertainment().setMaximumSize(new Dimension(616,75));
 							getJPanel_entertainment().setPreferredSize(new Dimension(616,75));
 							
 							getJPanel_meal().setPreferredSize(new Dimension(616,240));
 							getJPanel_meal().setMaximumSize(new Dimension(616,240));
 							getJPanel_mealOption1().setPreferredSize(new Dimension(609,70));
 							getJPanel_mealOption1().setMaximumSize(new Dimension(609,70));
 							getJPanel_mealOption1().setMinimumSize(new Dimension(609,70));
 							getJPanel_mealOption2().setPreferredSize(new Dimension(609,70));
 							getJPanel_mealOption2().setMaximumSize(new Dimension(609,70));
 							getJPanel_mealOption2().setMinimumSize(new Dimension(609,70));
 							getJPanel_mealOption3().setPreferredSize(new Dimension(609,70));
 							getJPanel_mealOption3().setMaximumSize(new Dimension(609,70));
 							getJPanel_mealOption3().setMinimumSize(new Dimension(609,70));
 							
 							getJToggleButton_viewPackage().setSelected(false);
 							getJToggleButton_viewBallroom().setSelected(false);
 							getJToggleButton_viewEntertainment().setSelected(false);
 							getJToggleButton_viewMealOption1().setSelected(false);
 							getJToggleButton_viewMealOption2().setSelected(false);
 							getJToggleButton_viewMealOption3().setSelected(false);
 							Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 							getJFrame_StandardPackage().setLocation(screenSize.width/2 - (getJFrame_StandardPackage().getWidth()/2),screenSize.height/2 - (getJFrame_StandardPackage().getHeight()/2));
 						}
 					});
 		}
 		return jButton_selectStandardPackage;
 	}
 	private static JButton getJButton_selectCustomPackage() {
 		if (jButton_selectCustomPackage == null) {
 			jButton_selectCustomPackage = new JButton();
 			jButton_selectCustomPackage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jButton_selectCustomPackage.setFocusable(false);
 			jButton_selectCustomPackage.setFocusPainted(false);
 			jButton_selectCustomPackage.setBounds(new Rectangle(230, 100, 205, 30));
 			jButton_selectCustomPackage.setText("Customise My Own Package");
 			jButton_selectCustomPackage
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							CreateCustomPackageForm form= new CreateCustomPackageForm();
 							form.getJFrame().setVisible(true);
 						}
 					});
 		}
 		return jButton_selectCustomPackage;
 	}
 	private JTextField getJTextField_selectedDate() {
 		if (jTextField_selectedDate == null) {
 			jTextField_selectedDate = new JTextField();
 			jTextField_selectedDate.setEnabled(false);
 			jTextField_selectedDate.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_selectedDate.setBounds(new Rectangle(15, 180, 379, 30));
 		}
 		return jTextField_selectedDate;
 	}
 	protected JButton getJButton_selectDate() {
 		if (jButton_selectDate == null) {
 			jButton_selectDate = new JButton();
 			jButton_selectDate.setFocusable(false);
 			jButton_selectDate.setFocusPainted(false);
 			jButton_selectDate.setEnabled(true);
 			jButton_selectDate.setBounds(new Rectangle(395, 180, 40, 30));
 			jButton_selectDate.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/30_calendar_icon.png")));
 			jButton_selectDate.addActionListener(new java.awt.event.ActionListener() {
 				@SuppressWarnings("unchecked")
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if(cPackageTitle!=null){
 						getJTextField_selectedDate().setEnabled(true);
 						getJTextField_selectedDate().setText(new CalendarForm(getJFrame()).getPickedDate());
 						if(!getJTextField_selectedDate().getText().equals("")){
 							String Date=getJTextField_selectedDate().getText();
 							if(isCustom){
 								filterTiming(cBallroomID, Date);
 								if(getJComboBox_selectTime().getItemCount()==2){
 									getJComboBox_selectTime().removeAll();
 									getJComboBox_selectTime().addItem("No Available Timings");
 									JOptionPane.showMessageDialog(null, "Sorry, we are fully booked at this date.\nPlease select another date", "Sorry", JOptionPane.WARNING_MESSAGE);
 								}
 							}
 							else{
 								String ballroomID=getJTextField_selectedDate().getName();
 								filterTiming(ballroomID, Date);
 								if(getJComboBox_selectTime().getItemCount()==2){
 									getJComboBox_selectTime().removeAll();
 									getJComboBox_selectTime().addItem("No Available Timings");
 									JOptionPane.showMessageDialog(null, "Sorry, we are fully booked at this date.\nPlease select another date", "Sorry", JOptionPane.WARNING_MESSAGE);
 								}
 							}
 							
 							
 						}
 						getJTextField_selectedDate().setEnabled(false);
 					}
 				}
 			});
 		}
 		return jButton_selectDate;
 	}
 	@SuppressWarnings("unchecked")
 	private JComboBox getJComboBox_selectTime() {
 		if (jComboBox_selectTime == null) {
 			jComboBox_selectTime = new JComboBox();
 			jComboBox_selectTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jComboBox_selectTime.setFocusable(false);
 			jComboBox_selectTime.setEnabled(false);
 			jComboBox_selectTime.setBounds(new Rectangle(15, 220, 420, 30));
 		}
 		return jComboBox_selectTime;
 	}
 	private JTextField getJTextField_eventName() {
 		if (jTextField_eventName == null) {
 			jTextField_eventName = new JTextField();
 			jTextField_eventName.setText("Give Your Event A Name");
 			jTextField_eventName.setForeground(SystemColor.scrollbar);
 			jTextField_eventName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_eventName.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_eventName.setBounds(new Rectangle(15, 300, 420, 30));
 			jTextField_eventName.addFocusListener(new java.awt.event.FocusAdapter() {   
 				public void focusLost(java.awt.event.FocusEvent e) {    
 					if(jTextField_eventName.getText().equals("")){
 						jTextField_eventName.setText("Give Your Event A Name");
 						jTextField_eventName.setForeground(SystemColor.scrollbar);
 					}
 				}
 				public void focusGained(java.awt.event.FocusEvent e) {
 					if(jTextField_eventName.getText().equals("Give Your Event A Name")){
 						jTextField_eventName.setText("");
 						jTextField_eventName.setForeground(SystemColor.BLACK);
 					}
 				}
 			});
 		}
 		return jTextField_eventName;
 	}
 	private JScrollPane getJScrollPane_eventDescription() {
 		if (jScrollPane_eventDescription == null) {
 			jScrollPane_eventDescription = new JScrollPane();
 			jScrollPane_eventDescription.setBounds(new Rectangle(15, 337, 420, 90));
 			jScrollPane_eventDescription.setViewportView(getJTextArea_eventDescription());
 		}
 		return jScrollPane_eventDescription;
 	}
 	private JTextArea getJTextArea_eventDescription() {
 		if (jTextArea_eventDescription == null) {
 			jTextArea_eventDescription = new JTextArea();
 			jTextArea_eventDescription.setForeground(SystemColor.scrollbar);
 			jTextArea_eventDescription.setText("\n\n                                            Give Your Event A Description");
 			jTextArea_eventDescription.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextArea_eventDescription.addFocusListener(new java.awt.event.FocusAdapter() {   
 				public void focusLost(java.awt.event.FocusEvent e) {    
 					if(jTextArea_eventDescription.getText().equals("")){
 						jTextArea_eventDescription.setForeground(SystemColor.scrollbar);
 						jTextArea_eventDescription.setText("\n\n                                            Give Your Event A Description");
 					}
 				}
 				public void focusGained(java.awt.event.FocusEvent e) {
 					if(jTextArea_eventDescription.getText().equals("\n\n                                            Give Your Event A Description")){
 						jTextArea_eventDescription.setText("");
 						jTextArea_eventDescription.setForeground(SystemColor.BLACK);
 						jTextArea_eventDescription.setWrapStyleWord(true);
 						jTextArea_eventDescription.setLineWrap(true);
 					}
 				}
 			});
 		}
 		return jTextArea_eventDescription;
 	}
 	private JButton getJButton_createEvent() {
 		if (jButton_createEvent == null) {
 			jButton_createEvent = new JButton();
 			jButton_createEvent.setFont(new Font("Century Gothic", Font.PLAIN, 12));
 			jButton_createEvent.setFocusable(false);
 			jButton_createEvent.setFocusPainted(false);
 			jButton_createEvent.setBounds(new Rectangle(135, 435, 177, 30));
 			jButton_createEvent.setText("Create My Event");
 			jButton_createEvent.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if(validate())
 					createEvent();
 				}
 			});
 		}
 		return jButton_createEvent;
 	}
 	/********************************************************
 	 *		Start Select Standard Package UI
 	 *******************************************************/
 	private JFrame getJFrame_StandardPackage() {
 		if (jFrame_StandardPackage == null) {
 			jFrame_StandardPackage = new JFrame();
 			jFrame_StandardPackage.setSize(new Dimension(675, 878));
 			jFrame_StandardPackage.setTitle("Select Standard Package");
 			jFrame_StandardPackage.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			jFrame_StandardPackage.setContentPane(getJScrollPane_StandardPackage());
 			jFrame_StandardPackage.setResizable(false);
 		}
 		return jFrame_StandardPackage;
 	}
 	private JScrollPane getJScrollPane_StandardPackage() {
 		if (jScrollPane_StandardPackage == null) {
 			jScrollPane_StandardPackage = new JScrollPane();
 			jScrollPane_StandardPackage.setViewportView(getJPanel_standardPackage());
 		}
 		return jScrollPane_StandardPackage;
 	}
 	private JPanel getJPanel_standardPackage() {
 		if (jPanel_standardPackage == null) {
 			jPanel_standardPackage = new JPanel();
 			jPanel_standardPackage.setLayout(null);
 			jPanel_standardPackage.setBackground(SystemColor.control);
 			jPanel_standardPackage.setPreferredSize(new Dimension(640, 775));
 			jPanel_standardPackage.add(getJPanel_seachPanel(), null);
 			jPanel_standardPackage.add(getJPanel_body(), null);
 		}
 		return jPanel_standardPackage;
 	}
 	private JPanel getJPanel_seachPanel() {
 		if (jPanel_seachPanel == null) {
 			jPanel_seachPanel = new JPanel();
 			jPanel_seachPanel.setLayout(null);
 			jPanel_seachPanel.setOpaque(false);
 			jPanel_seachPanel.setBounds(new Rectangle(15, 23, 624, 200));
 			jPanel_seachPanel.add(getJScrollPane_search(), null);
 			jPanel_seachPanel.add(getJToolBar_previous(), null);
 			jPanel_seachPanel.add(getJToolBar_next(), null);
 		}
 		return jPanel_seachPanel;
 	}
 	private JScrollPane getJScrollPane_search() {
 		if (jScrollPane_search == null) {
 			jScrollPane_search = new JScrollPane();
 			jScrollPane_search.setBounds(new Rectangle(0, 0, 624, 155));
 			jScrollPane_search.setViewportView(getJTable_search());
 		}
 		return jScrollPane_search;
 	}
 	@SuppressWarnings("static-access")
 	private JTable getJTable_search() {
 		if (jTable_search == null) {
 			DefaultTableModel model= new DefaultTableModel();
 			model.setColumnIdentifiers(new Object[]{"ID","Title","Type","Description"});
 			jTable_search = new JTable(model);
 			jTable_search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTable_search.setOpaque(false);
 			jTable_search.setEnabled(false);
 			jTable_search.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			jTable_search.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable_search.setIntercellSpacing(new Dimension(5, 5));
 			jTable_search.setRowSelectionAllowed(true);
 			jTable_search.setShowGrid(false);
 			jTable_search.setFocusable(false);
 			jTable_search.setColumnSelectionAllowed(false);
 			jTable_search.setAutoResizeMode(jTable_search.AUTO_RESIZE_OFF);
 			jTable_search.getColumnModel().getColumn(0).setPreferredWidth(50);
 			jTable_search.getColumnModel().getColumn(1).setPreferredWidth(450);
 			jTable_search.getColumnModel().getColumn(2).setPreferredWidth(130);
 			jTable_search.getColumnModel().getColumn(3).setPreferredWidth(625);
 			jTable_search.addMouseListener(new MouseAdapter() {   
 				public void mouseClicked(java.awt.event.MouseEvent e) {    
 					if (e.getClickCount() == 2){
 						String ID=getJTable_search().getValueAt(getJTable_search().getSelectedRow(), 0).toString();
 						retrievePackage(ID);
 						getJToolBar_next().setVisible(true);
 						getJToolBar_previous().setVisible(true);
 						getJPanel_standardPackage().setPreferredSize(new Dimension(640, 775));
 						getJFrame_StandardPackage().setSize(new Dimension(690,622));
 						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 						getJFrame_StandardPackage().setLocation(screenSize.width/2 - (getJFrame_StandardPackage().getWidth()/2),screenSize.height/2 - (getJFrame_StandardPackage().getHeight()/2));
 					}
 				}
 				 @Override
 			        public void mouseReleased(MouseEvent e) {
 			            int r = jTable_search.rowAtPoint(e.getPoint());
 			            if (r >= 0 && r < jTable_search.getRowCount()) {
 			            	jTable_search.setRowSelectionInterval(r, r);
 			            } else {
 			            	jTable_search.clearSelection();
 			            }
 
 			            int rowindex = jTable_search.getSelectedRow();
 			            if (rowindex < 0)
 			                return;
 			            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 			                JPopupMenu popup = getJPopupMenu_package();
 			                popup.show(e.getComponent(), e.getX(), e.getY());
 			            }
 			        }
 			});
 		}
 		return jTable_search;
 	}
 	private JPopupMenu getJPopupMenu_package() {
 		if (jPopupMenu_package == null) {
 			jPopupMenu_package = new JPopupMenu();
 			jPopupMenu_package.setOpaque(false);
 			jPopupMenu_package.setSize(new Dimension(99, 168));
 			jPopupMenu_package.add(getJMenuItem_retrive_package());
 		}
 		return jPopupMenu_package;
 	}
 	private JMenuItem getJMenuItem_retrive_package() {
 		if (jMenuItem_retrive_package == null) {
 			jMenuItem_retrive_package = new JMenuItem();
 			jMenuItem_retrive_package.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jMenuItem_retrive_package.setText("Show Details");
 			jMenuItem_retrive_package.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					String ID=getJTable_search().getValueAt(getJTable_search().getSelectedRow(), 0).toString();
 					retrievePackage(ID);
 					getJToolBar_next().setVisible(true);
 					getJToolBar_previous().setVisible(true);
 					getJPanel_standardPackage().setPreferredSize(new Dimension(695, 775));
 					getJFrame_StandardPackage().setSize(new Dimension(690,622));
 					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 					getJFrame_StandardPackage().setLocation(screenSize.width/2 - (getJFrame_StandardPackage().getWidth()/2),screenSize.height/2 - (getJFrame_StandardPackage().getHeight()/2));
 				}
 			});
 		}
 		return jMenuItem_retrive_package;
 	}
 	private JToolBar getJToolBar_previous() {
 		if (jToolBar_previous == null) {
 			jToolBar_previous = new JToolBar();
 			jToolBar_previous.setOpaque(false);
 			jToolBar_previous.setVisible(false);
 			jToolBar_previous.setFloatable(false);
 			jToolBar_previous.setFocusable(false);
 			jToolBar_previous.setBounds(new Rectangle(0, 160, 50, 40));
 			jToolBar_previous.add(getJButton_previous());
 		}
 		return jToolBar_previous;
 	}
 	private JButton getJButton_previous() {
 		if (jButton_previous == null) {
 			jButton_previous = new JButton();
 			jButton_previous.setFocusable(false);
 			jButton_previous.setFocusPainted(false);
 			jButton_previous.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/previous_button.png")));
 			jButton_previous.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					int row = getJTable_search().getSelectedRow();
 					if(row!=0){
 						row--;
 						getJTable_search().setSelectionMode(0);
 						getJTable_search().changeSelection(row, 0, false, false);
 						String ID=getJTable_search().getValueAt(getJTable_search().getSelectedRow(), 0).toString();
 						retrievePackage(ID);
 						getJPanel_standardPackage().setPreferredSize(new Dimension(640, 775));
 						getJFrame_StandardPackage().setSize(new Dimension(690,622));
 						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 						getJFrame_StandardPackage().setLocation(screenSize.width/2 - (getJFrame_StandardPackage().getWidth()/2),screenSize.height/2 - (getJFrame_StandardPackage().getHeight()/2));
 					}
 				}
 			});
 		}
 		return jButton_previous;
 	}
 	private JToolBar getJToolBar_next() {
 		if (jToolBar_next == null) {
 			jToolBar_next = new JToolBar();
 			jToolBar_next.setOpaque(false);
 			jToolBar_next.setVisible(false);
 			jToolBar_next.setFloatable(false);
 			jToolBar_next.setFocusable(false);
 			jToolBar_next.setBounds(new Rectangle(590, 160, 50, 40));
 			jToolBar_next.add(getJButton_next());
 		}
 		return jToolBar_next;
 	}
 	private JButton getJButton_next() {
 		if (jButton_next == null) {
 			jButton_next = new JButton();
 			jButton_next.setFocusable(false);
 			jButton_next.setFocusPainted(false);
 			jButton_next.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/next_button.png")));
 			jButton_next.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					int row = getJTable_search().getSelectedRow();
 					if(row!=(getJTable_search().getRowCount()-1)){
 						row++;
 						getJTable_search().setSelectionMode(0);
 						getJTable_search().changeSelection(row, 0, false, false);
 						String ID=getJTable_search().getValueAt(getJTable_search().getSelectedRow(), 0).toString();
 						retrievePackage(ID);
 						getJPanel_standardPackage().setPreferredSize(new Dimension(640, 775));
 						getJFrame_StandardPackage().setSize(new Dimension(690,622));
 						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 						getJFrame_StandardPackage().setLocation(screenSize.width/2 - (getJFrame_StandardPackage().getWidth()/2),screenSize.height/2 - (getJFrame_StandardPackage().getHeight()/2));
 					}
 				}
 			});
 		}
 		return jButton_next;
 	}
 	private JPanel getJPanel_body() {
 		if (jPanel_body == null) {
 			jPanel_body = new JPanel();
 			jPanel_body.setLayout(new BoxLayout(getJPanel_body(), BoxLayout.Y_AXIS));
 			jPanel_body.setSize(new Dimension(624,535));
 			jPanel_body.setPreferredSize(new Dimension(624,535));
 			jPanel_body.setBounds(new Rectangle(15, 240, 624, 535));
 			jPanel_body.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 			jPanel_body.add(getJPanel_package(), null);
 			jPanel_body.add(getJPanel_ballroom(), null);
 			jPanel_body.add(getJPanel_entertainment(), null);
 			jPanel_body.add(getJPanel_meal(), null);
 			jPanel_body.add(getJJToolBarBar_StandardPackage(), null);
 			
 		}
 		return jPanel_body;
 	}
 	private JPanel getJPanel_package() {
 		if (jPanel_package == null) {
 			jLabel_packageDescription = new JLabel();
 			jLabel_packageDescription.setBounds(new Rectangle(15, 70, 170, 30));
 			jLabel_packageDescription.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_packageDescription.setText("Description :");
 			jPanel_package = new JPanel();
 			jPanel_package.setLayout(null);
 			jPanel_package.setMaximumSize(new Dimension(618, 80));
 			jPanel_package.setMinimumSize(new Dimension(618, 80));
 			jPanel_package.setPreferredSize(new Dimension(618, 80));
 			jPanel_package.setBorder(BorderFactory.createTitledBorder(null,"Package", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Century Gothic", Font.PLAIN, 14), Color.black));
 			jPanel_package.add(getJTextField_packageTitle(), null);
 			jPanel_package.add(getJScrollPane_packageDescription(), null);
 			jPanel_package.add(jLabel_packageDescription, null);
 			jPanel_package.add(getJToggleButton_viewPackage(), null);
 		}
 		return jPanel_package;
 	}
 	private JTextField getJTextField_packageTitle() {
 		if (jTextField_packageTitle == null) {
 			jTextField_packageTitle = new JTextField();
 			jTextField_packageTitle.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_packageTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_packageTitle.setEditable(false);
 			jTextField_packageTitle.setBounds(new Rectangle(15, 30, 447, 30));
 		}
 		return jTextField_packageTitle;
 	}
 	private JScrollPane getJScrollPane_packageDescription() {
 		if (jScrollPane_packageDescription == null) {
 			jScrollPane_packageDescription = new JScrollPane();
 			jScrollPane_packageDescription.setBounds(new Rectangle(15, 110, 590, 100));
 			jScrollPane_packageDescription.setViewportView(getJTextArea_packageDescription());
 		}
 		return jScrollPane_packageDescription;
 	}
 	private JTextArea getJTextArea_packageDescription() {
 		if (jTextArea_packageDescription == null) {
 			jTextArea_packageDescription = new JTextArea();
 			jTextArea_packageDescription.setFont(new Font("Century Gothic", Font.PLAIN, 12));
 			jTextArea_packageDescription.setEditable(false);
 			jTextArea_packageDescription.setLineWrap(true);
 			jTextArea_packageDescription.setWrapStyleWord(true);
 		}
 		return jTextArea_packageDescription;
 	}
 	private JToggleButton getJToggleButton_viewPackage() {
 		if (jToggleButton_viewPackage == null) {
 			jToggleButton_viewPackage = new JToggleButton();
 			jToggleButton_viewPackage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewPackage.setFocusable(false);
 			jToggleButton_viewPackage.setFocusPainted(false);
 			jToggleButton_viewPackage.setBounds(new Rectangle(471, 30, 136, 30));
 			jToggleButton_viewPackage.setText("View Description");
 			jToggleButton_viewPackage.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					if(jToggleButton_viewPackage.isSelected()){
 						getJPanel_package().setMaximumSize(new Dimension(618,220));
 						getJPanel_package().setPreferredSize(new Dimension(618,220));
 						getJPanel_package().setSize(new Dimension(618,220));
 						getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+140));	
 						getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+140));
 						getJFrame_StandardPackage().repaint();
 					}
 					else{
 						getJPanel_package().setMaximumSize(new Dimension(618, 80));
 						getJPanel_package().setPreferredSize(new Dimension(618, 80));
 						getJPanel_package().setSize(new Dimension(618, 80));
 						getJPanel_package().setMinimumSize(new Dimension(618, 80));
 						getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-140));	
 						getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-140));
 						getJFrame_StandardPackage().repaint();
 					}
 				}
 			});
 		}
 		return jToggleButton_viewPackage;
 	}
 	private JPanel getJPanel_ballroom() {
 		if (jPanel_ballroom == null) {
 			jLabel_ballroomAddress = new JLabel();
 			jLabel_ballroomAddress.setBounds(new Rectangle(15, 260, 100, 30));
 			jLabel_ballroomAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_ballroomAddress.setText("Address :");
 			jLabel_facilityName = new JLabel();
 			jLabel_facilityName.setBounds(new Rectangle(15, 170, 100, 30));
 			jLabel_facilityName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_facilityName.setText("Faciity Name :");
 			jLabel_weekendSurcharge = new JLabel();
 			jLabel_weekendSurcharge.setBounds(new Rectangle(315, 120, 127, 30));
 			jLabel_weekendSurcharge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_weekendSurcharge.setText("Weekend Surcharge :");
 			jLabel_ballroomParking = new JLabel();
 			jLabel_ballroomParking.setBounds(new Rectangle(15, 120, 70, 30));
 			jLabel_ballroomParking.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_ballroomParking.setText("Parking :");
 			jLabel_ballroomFinalPrice = new JLabel();
 			jLabel_ballroomFinalPrice.setBounds(new Rectangle(315, 80, 70, 30));
 			jLabel_ballroomFinalPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_ballroomFinalPrice.setText("Rate :");
 			jLabel_ballroomSize = new JLabel();
 			jLabel_ballroomSize.setBounds(new Rectangle(15, 80, 70, 30));
 			jLabel_ballroomSize.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_ballroomSize.setText("Size :");
 			jPanel_ballroom = new JPanel();
 			jPanel_ballroom.setMaximumSize(new Dimension(618, 80));
 			jPanel_ballroom.setPreferredSize(new Dimension(618,80));
 			jPanel_ballroom.setLayout(null);
 			jPanel_ballroom.setBorder(BorderFactory.createTitledBorder(null,"Ballroom", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.PLAIN, 14), Color.black));
 			jPanel_ballroom.add(getJTextField_ballroomTitle(), null);
 			jPanel_ballroom.add(getJToggleButton_viewBallroom(), null);
 			jPanel_ballroom.add(jLabel_ballroomSize, null);
 			jPanel_ballroom.add(jLabel_ballroomFinalPrice, null);
 			jPanel_ballroom.add(jLabel_ballroomParking, null);
 			jPanel_ballroom.add(jLabel_weekendSurcharge, null);
 			jPanel_ballroom.add(jLabel_facilityName, null);
 			jPanel_ballroom.add(jLabel_ballroomAddress, null);
 			jPanel_ballroom.add(getJTextField_ballroomSize(), null);
 			jPanel_ballroom.add(getJTextField_ballroomFinalPrice(), null);
 			jPanel_ballroom.add(getJTextField_ballroomParking(), null);
 			jPanel_ballroom.add(getJTextField_ballroomWeekendSurcharge(), null);
 			jPanel_ballroom.add(getJTextField_facilityName(), null);
 			jPanel_ballroom.add(getJScrollPane_facilityAddress(), null);
 		}
 		return jPanel_ballroom;
 	}
 	private JTextField getJTextField_ballroomTitle() {
 		if (jTextField_ballroomTitle == null) {
 			jTextField_ballroomTitle = new JTextField();
 			jTextField_ballroomTitle.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_ballroomTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_ballroomTitle.setEditable(false);
 			jTextField_ballroomTitle.setBounds(new Rectangle(15, 30, 447, 30));
 		}
 		return jTextField_ballroomTitle;
 	}
 	private JToggleButton getJToggleButton_viewBallroom() {
 		if (jToggleButton_viewBallroom == null) {
 			jToggleButton_viewBallroom = new JToggleButton();
 			jToggleButton_viewBallroom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewBallroom.setFocusable(false);
 			jToggleButton_viewBallroom.setFocusPainted(false);
 			jToggleButton_viewBallroom.setBounds(new Rectangle(471, 30, 136, 30));
 			jToggleButton_viewBallroom.setText("View Description");
 			jToggleButton_viewBallroom.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					if(jToggleButton_viewBallroom.isSelected()){
 						getJPanel_ballroom().setMaximumSize(new Dimension(616,450));
 						getJPanel_ballroom().setPreferredSize(new Dimension(616,450));
 						getJPanel_ballroom().setSize(new Dimension(616,450));
 						getJPanel_ballroom().setMinimumSize(new Dimension(616,450));
 						getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+370));	
 						getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+370));
 						getJFrame_StandardPackage().repaint();
 					}
 					else{
 						getJPanel_ballroom().setMaximumSize(new Dimension(618, 80));
 						getJPanel_ballroom().setPreferredSize(new Dimension(618, 80));
 						getJPanel_ballroom().setSize(new Dimension(618, 80));
 						getJPanel_ballroom().setMinimumSize(new Dimension(618, 80));
 						getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-370));	
 						getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-370));
 					}
 				}
 			});
 		}
 		return jToggleButton_viewBallroom;
 	}
 	private JTextField getJTextField_ballroomSize() {
 		if (jTextField_ballroomSize == null) {
 			jTextField_ballroomSize = new JTextField();
 			jTextField_ballroomSize.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_ballroomSize.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_ballroomSize.setEditable(false);
 			jTextField_ballroomSize.setBounds(new Rectangle(85, 80, 200, 30));
 		}
 		return jTextField_ballroomSize;
 	}
 	private JTextField getJTextField_ballroomFinalPrice() {
 		if (jTextField_ballroomFinalPrice == null) {
 			jTextField_ballroomFinalPrice = new JTextField();
 			jTextField_ballroomFinalPrice.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_ballroomFinalPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_ballroomFinalPrice.setEditable(false);
 			jTextField_ballroomFinalPrice.setBounds(new Rectangle(440, 80, 160, 30));
 		}
 		return jTextField_ballroomFinalPrice;
 	}
 	private JTextField getJTextField_ballroomParking() {
 		if (jTextField_ballroomParking == null) {
 			jTextField_ballroomParking = new JTextField();
 			jTextField_ballroomParking.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_ballroomParking.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_ballroomParking.setEditable(false);
 			jTextField_ballroomParking.setBounds(new Rectangle(85, 120, 200, 30));
 		}
 		return jTextField_ballroomParking;
 	}
 	private JTextField getJTextField_ballroomWeekendSurcharge() {
 		if (jTextField_ballroomWeekendSurcharge == null) {
 			jTextField_ballroomWeekendSurcharge = new JTextField();
 			jTextField_ballroomWeekendSurcharge.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_ballroomWeekendSurcharge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_ballroomWeekendSurcharge.setEditable(false);
 			jTextField_ballroomWeekendSurcharge.setBounds(new Rectangle(440, 120, 160, 30));
 		}
 		return jTextField_ballroomWeekendSurcharge;
 	}
 	private JTextField getJTextField_facilityName() {
 		if (jTextField_facilityName == null) {
 			jTextField_facilityName = new JTextField();
 			jTextField_facilityName.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_facilityName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_facilityName.setEditable(false);
 			jTextField_facilityName.setBounds(new Rectangle(15, 210, 584, 30));
 		}
 		return jTextField_facilityName;
 	}
 	private JScrollPane getJScrollPane_facilityAddress() {
 		if (jScrollPane_facilityAddress == null) {
 			jScrollPane_facilityAddress = new JScrollPane();
 			jScrollPane_facilityAddress.setBounds(new Rectangle(15, 300, 584, 140));
 			jScrollPane_facilityAddress.setViewportView(getJTextArea_facilityAddress());
 		}
 		return jScrollPane_facilityAddress;
 	}
 	private JTextArea getJTextArea_facilityAddress() {
 		if (jTextArea_facilityAddress == null) {
 			jTextArea_facilityAddress = new JTextArea();
 			jTextArea_facilityAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextArea_facilityAddress.setEditable(false);
 			jTextArea_facilityAddress.setWrapStyleWord(true);
 			jTextArea_facilityAddress.setLineWrap(true);
 		}
 		return jTextArea_facilityAddress;
 	}
 	private JPanel getJPanel_entertainment() {
 		if (jPanel_entertainment == null) {
 			jLabel_entertainmentFInalPrice = new JLabel();
 			jLabel_entertainmentFInalPrice.setBounds(new Rectangle(270, 75, 121, 31));
 			jLabel_entertainmentFInalPrice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_entertainmentFInalPrice.setText("Total Price :");
 			jLabel_entertainmentMenu = new JLabel();
 			jLabel_entertainmentMenu.setBounds(new Rectangle(15, 75, 241, 31));
 			jLabel_entertainmentMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_entertainmentMenu.setText("Activity :");
 			jPanel_entertainment = new JPanel();
 			jPanel_entertainment.setMaximumSize(new Dimension(616,75));
 			jPanel_entertainment.setPreferredSize(new Dimension(616,75));
 			jPanel_entertainment.setBorder(BorderFactory.createTitledBorder(null,"Entertainment", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.PLAIN, 14), Color.black));
 			jPanel_entertainment.setLayout(null);
 			jPanel_entertainment.add(getJTextField_entertainmentTitle(), null);
 			jPanel_entertainment.add(getJToggleButton_viewEntertainment(), null);
 			jPanel_entertainment.add(jLabel_entertainmentMenu, null);
 			jPanel_entertainment.add(getJScrollPane_entertainmentMenu(), null);
 			jPanel_entertainment.add(jLabel_entertainmentFInalPrice, null);
 			jPanel_entertainment.add(getJTextField_entertainmentFinalPrice(), null);
 		}
 		return jPanel_entertainment;
 	}
 	private JTextField getJTextField_entertainmentTitle() {
 		if (jTextField_entertainmentTitle == null) {
 			jTextField_entertainmentTitle = new JTextField();
 			jTextField_entertainmentTitle.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_entertainmentTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_entertainmentTitle.setEditable(false);
 			jTextField_entertainmentTitle.setBounds(new Rectangle(15, 30, 447, 30));
 		}
 		return jTextField_entertainmentTitle;
 	}
 	private JToggleButton getJToggleButton_viewEntertainment() {
 		if (jToggleButton_viewEntertainment == null) {
 			jToggleButton_viewEntertainment = new JToggleButton();
 			jToggleButton_viewEntertainment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewEntertainment.setFocusable(false);
 			jToggleButton_viewEntertainment.setFocusPainted(false);
 			jToggleButton_viewEntertainment.setBounds(new Rectangle(471, 30, 136, 30));
 			jToggleButton_viewEntertainment.setText("View Description");
 			jToggleButton_viewEntertainment
 					.addItemListener(new java.awt.event.ItemListener() {
 						public void itemStateChanged(java.awt.event.ItemEvent e) {
 							if(jToggleButton_viewEntertainment.isSelected()){
 								getJPanel_entertainment().setMaximumSize(new Dimension(616,250));
 								getJPanel_entertainment().setPreferredSize(new Dimension(616,250));
 								getJPanel_entertainment().setSize(new Dimension(616,250));
 								getJPanel_entertainment().setMinimumSize(new Dimension(616,250));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+165));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+165));
 								getJFrame_StandardPackage().repaint();
 							}
 							else{
 								getJPanel_entertainment().setMaximumSize(new Dimension(616,80));
 								getJPanel_entertainment().setPreferredSize(new Dimension(616,80));
 								getJPanel_entertainment().setSize(new Dimension(616,80));
 								getJPanel_entertainment().setMinimumSize(new Dimension(616,80));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-165));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-165));
 								getJFrame_StandardPackage().repaint();
 							}
 						}
 					});
 		}
 		return jToggleButton_viewEntertainment;
 	}
 	private JScrollPane getJScrollPane_entertainmentMenu() {
 		if (jScrollPane_entertainmentMenu == null) {
 			jScrollPane_entertainmentMenu = new JScrollPane();
 			jScrollPane_entertainmentMenu.setBounds(new Rectangle(15, 120, 590, 100));
 			jScrollPane_entertainmentMenu.setViewportView(getJTable_enterainementMenu());
 		}
 		return jScrollPane_entertainmentMenu;
 	}
 	@SuppressWarnings("static-access")
 	private JTable getJTable_enterainementMenu() {
 		if (jTable_enterainementMenu == null) {
 			DefaultTableModel model= new DefaultTableModel();
 			model.setColumnIdentifiers(new Object[]{"Entertainment Name","Price","Description"});
 			jTable_enterainementMenu = new JTable(model);
 			jTable_enterainementMenu.setOpaque(false);
 			jTable_enterainementMenu.setEnabled(false);
 			jTable_enterainementMenu.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			jTable_enterainementMenu.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable_enterainementMenu.setIntercellSpacing(new Dimension(5, 5));
 			jTable_enterainementMenu.setRowSelectionAllowed(true);
 			jTable_enterainementMenu.setShowGrid(false);
 			jTable_enterainementMenu.setFocusable(false);
 			jTable_enterainementMenu.setColumnSelectionAllowed(false);
 			jTable_enterainementMenu.setAutoResizeMode(getJTable_enterainementMenu().AUTO_RESIZE_OFF);
 			jTable_enterainementMenu.getColumnModel().getColumn(0).setPreferredWidth(50);
 			jTable_enterainementMenu.getColumnModel().getColumn(1).setPreferredWidth(150);
 			jTable_enterainementMenu.getColumnModel().getColumn(2).setPreferredWidth(500);
 		}
 		return jTable_enterainementMenu;
 	}
 	private JTextField getJTextField_entertainmentFinalPrice() {
 		if (jTextField_entertainmentFinalPrice == null) {
 			jTextField_entertainmentFinalPrice = new JTextField();
 			jTextField_entertainmentFinalPrice.setEditable(false);
 			jTextField_entertainmentFinalPrice.setBounds(new Rectangle(390, 80, 212, 30));
 		}
 		return jTextField_entertainmentFinalPrice;
 	}
 	private JPanel getJPanel_meal() {
 		if (jPanel_meal == null) {
 			jLabel_mealOption3 = new JLabel();
 			jLabel_mealOption3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_mealOption3.setText("Option 3 :");
 			jLabel_mealOption3.setBounds(new Rectangle(15, 22, 80, 30));
 			jLabel_mealOption2 = new JLabel();
 			jLabel_mealOption2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_mealOption2.setText("Option 2 :");
 			jLabel_mealOption2.setBounds(new Rectangle(15, 22, 80, 30));
 			jLabel_mealOption1 = new JLabel();
 			jLabel_mealOption1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jLabel_mealOption1.setText("Option 1 :");
 			jLabel_mealOption1.setBounds(new Rectangle(15, 22, 80, 30));
 			jPanel_meal = new JPanel();
 			jPanel_meal.setPreferredSize(new Dimension(616,240));
 			jPanel_meal.setMaximumSize(new Dimension(616,240));
 			jPanel_meal.setBorder(BorderFactory.createTitledBorder(null,"Meal Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.PLAIN, 14), Color.black));
 			jPanel_meal.setLayout(new BoxLayout(getJPanel_meal(), BoxLayout.Y_AXIS));
 			jPanel_meal.add(getJPanel_mealOption1(), null);
 			jPanel_meal.add(getJPanel_mealOption2(), null);
 			jPanel_meal.add(getJPanel_mealOption3(), null);
 		}
 		return jPanel_meal;
 	}
 	private JPanel getJPanel_mealOption1() {
 		if (jPanel_mealOption1 == null) {
 			jPanel_mealOption1 = new JPanel();
 			jPanel_mealOption1.setLayout(null);
 			jPanel_mealOption1.setPreferredSize(new Dimension(609,70));
 			jPanel_mealOption1.setMaximumSize(new Dimension(609,70));
 			jPanel_mealOption1.setMinimumSize(new Dimension(609,70));
 			jPanel_mealOption1.setOpaque(false);
 			jPanel_mealOption1.add(jLabel_mealOption1, null);
 			jPanel_mealOption1.add(getJTextField_mealOption1(), null);
 			jPanel_mealOption1.add(getJToggleButton_viewMealOption1(), null);
 			jPanel_mealOption1.add(getJScrollPane_mealOption1(), null);
 		}
 		return jPanel_mealOption1;
 	}
 	private JTextField getJTextField_mealOption1() {
 		if (jTextField_mealOption1 == null) {
 			jTextField_mealOption1 = new JTextField();
 			jTextField_mealOption1.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_mealOption1.setEditable(false);
 			jTextField_mealOption1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_mealOption1.setBounds(new Rectangle(96, 22, 360, 30));
 		}
 		return jTextField_mealOption1;
 	}
 	private JScrollPane getJScrollPane_mealOption1() {
 		if (jScrollPane_mealOption1 == null) {
 			jScrollPane_mealOption1 = new JScrollPane();
 			jScrollPane_mealOption1.setBounds(new Rectangle(15, 72, 585, 100));
 			jScrollPane_mealOption1.setViewportView(getJTable_mealOption1());
 		}
 		return jScrollPane_mealOption1;
 	}
 	@SuppressWarnings({ "static-access" })
 	private JTable getJTable_mealOption1() {
 		if (jTable_mealOption1 == null) {
 			DefaultTableModel model= new DefaultTableModel();
 			model.setColumnIdentifiers(new Object[]{"Meal Name","Price/Hr","Halal","Vegetarian","Description"});
 			jTable_mealOption1 = new JTable(model);
 			jTable_mealOption1.setOpaque(false);
 			jTable_mealOption1.setEnabled(false);
 			jTable_mealOption1.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			jTable_mealOption1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable_mealOption1.setIntercellSpacing(new Dimension(5, 5));
 			jTable_mealOption1.setRowSelectionAllowed(true);
 			jTable_mealOption1.setShowGrid(false);
 			jTable_mealOption1.setFocusable(false);
 			jTable_mealOption1.setColumnSelectionAllowed(false);
 			jTable_mealOption1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTable_mealOption1.setAutoResizeMode(getJTable_mealOption1().AUTO_RESIZE_OFF);
 			jTable_mealOption1.getColumnModel().getColumn(0).setPreferredWidth(450);
 			jTable_mealOption1.getColumnModel().getColumn(1).setPreferredWidth(115);
 			jTable_mealOption1.getColumnModel().getColumn(2).setPreferredWidth(50);
 			jTable_mealOption1.getColumnModel().getColumn(3).setPreferredWidth(80);
 			jTable_mealOption1.getColumnModel().getColumn(4).setPreferredWidth(435);
 		}
 		return jTable_mealOption1;
 	}
 	private JToggleButton getJToggleButton_viewMealOption1() {
 		if (jToggleButton_viewMealOption1 == null) {
 			jToggleButton_viewMealOption1 = new JToggleButton();
 			jToggleButton_viewMealOption1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewMealOption1.setEnabled(false);
 			jToggleButton_viewMealOption1.setText("View Meal Details");
 			jToggleButton_viewMealOption1.setBounds(new Rectangle(465, 22, 136, 30));
 			jToggleButton_viewMealOption1.setFocusable(false);
 			jToggleButton_viewMealOption1.setFocusPainted(false);
 			jToggleButton_viewMealOption1
 					.addItemListener(new java.awt.event.ItemListener() {
 						@SuppressWarnings({ "static-access" })
 						public void itemStateChanged(java.awt.event.ItemEvent e) {
 							if(jTable_mealOption1.getModel().getRowCount()==0){
 								AdministrateMealControl control= new AdministrateMealControl();
 								getJTable_mealOption1().setModel(control.processRetrieveMealMenuByID(getJTextField_mealOption1().getName()));
 								jTable_mealOption1.setAutoResizeMode(getJTable_mealOption1().AUTO_RESIZE_OFF);
 								jTable_mealOption1.getColumnModel().getColumn(0).setPreferredWidth(450);
 								jTable_mealOption1.getColumnModel().getColumn(1).setPreferredWidth(115);
 								jTable_mealOption1.getColumnModel().getColumn(2).setPreferredWidth(50);
 								jTable_mealOption1.getColumnModel().getColumn(3).setPreferredWidth(80);
 								jTable_mealOption1.getColumnModel().getColumn(4).setPreferredWidth(435);
 							}
 							if(jToggleButton_viewMealOption1.isSelected()){
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height+102));
 								jPanel_mealOption1.setPreferredSize(new Dimension(609,172));
 								jPanel_mealOption1.setMaximumSize(new Dimension(609,172));
 								jPanel_mealOption1.setMinimumSize(new Dimension(609,172));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+102));
 								getJFrame_StandardPackage().repaint();
 							}
 							else{
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height-102));
 								jPanel_mealOption1.setPreferredSize(new Dimension(609,70));
 								jPanel_mealOption1.setMaximumSize(new Dimension(609,70));
 								jPanel_mealOption1.setMinimumSize(new Dimension(609,70));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-102));
 								getJFrame_StandardPackage().repaint();
 							}
 							
 					}});
 		}
 		return jToggleButton_viewMealOption1;
 	}
 	private JPanel getJPanel_mealOption2() {
 		if (jPanel_mealOption2 == null) {
 			jPanel_mealOption2 = new JPanel();
 			jPanel_mealOption2.setLayout(null);
 			jPanel_mealOption2.setPreferredSize(new Dimension(609,70));
 			jPanel_mealOption2.setMaximumSize(new Dimension(609,70));
 			jPanel_mealOption2.setMinimumSize(new Dimension(609,70));
 			jPanel_mealOption2.add(jLabel_mealOption2, null);
 			jPanel_mealOption2.add(getJTextField_mealOption2(), null);
 			jPanel_mealOption2.add(getJToggleButton_viewMealOption2(), null);
 			jPanel_mealOption2.add(getJScrollPane_mealOption2(), null);
 		}
 		return jPanel_mealOption2;
 	}
 	private JTextField getJTextField_mealOption2() {
 		if (jTextField_mealOption2 == null) {
 			jTextField_mealOption2 = new JTextField();
 			jTextField_mealOption2.setEditable(false);
 			jTextField_mealOption2.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_mealOption2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_mealOption2.setBounds(new Rectangle(96, 22, 360, 30));
 		}
 		return jTextField_mealOption2;
 	}
 	private JToggleButton getJToggleButton_viewMealOption2() {
 		if (jToggleButton_viewMealOption2 == null) {
 			jToggleButton_viewMealOption2 = new JToggleButton();
 			jToggleButton_viewMealOption2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewMealOption2.setFocusable(false);
 			jToggleButton_viewMealOption2.setEnabled(false);
 			jToggleButton_viewMealOption2.setFocusPainted(false);
 			jToggleButton_viewMealOption2.setBounds(new Rectangle(465, 22, 136, 30));
 			jToggleButton_viewMealOption2.setText("View MealDetails");
 			jToggleButton_viewMealOption2
 					.addItemListener(new java.awt.event.ItemListener() {
 						@SuppressWarnings("static-access")
 						public void itemStateChanged(java.awt.event.ItemEvent e) {
 							if(jTable_mealOption2.getModel().getRowCount()==0){
 								AdministrateMealControl control= new AdministrateMealControl();
 								getJTable_mealOption2().setModel(control.processRetrieveMealMenuByID(getJTextField_mealOption2().getName()));
 								jTable_mealOption2.setAutoResizeMode(getJTable_mealOption2().AUTO_RESIZE_OFF);
 								jTable_mealOption2.getColumnModel().getColumn(0).setPreferredWidth(450);
 								jTable_mealOption2.getColumnModel().getColumn(1).setPreferredWidth(115);
 								jTable_mealOption2.getColumnModel().getColumn(2).setPreferredWidth(50);
 								jTable_mealOption2.getColumnModel().getColumn(3).setPreferredWidth(80);
 								jTable_mealOption2.getColumnModel().getColumn(4).setPreferredWidth(435);
 							}
 							if(jToggleButton_viewMealOption2.isSelected()){
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height+102));
 								jPanel_mealOption2.setPreferredSize(new Dimension(609,172));
 								jPanel_mealOption2.setMaximumSize(new Dimension(609,172));
 								jPanel_mealOption2.setMinimumSize(new Dimension(609,172));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+102));
 								getJFrame_StandardPackage().repaint();
 							}
 							else{
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height-102));
 								jPanel_mealOption2.setPreferredSize(new Dimension(609,70));
 								jPanel_mealOption2.setMaximumSize(new Dimension(609,70));
 								jPanel_mealOption2.setMinimumSize(new Dimension(609,70));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-102));
 								getJFrame_StandardPackage().repaint();
 							}
 						}
 					});
 		}
 		return jToggleButton_viewMealOption2;
 	}
 	private JScrollPane getJScrollPane_mealOption2() {
 		if (jScrollPane_mealOption2 == null) {
 			jScrollPane_mealOption2 = new JScrollPane();
 			jScrollPane_mealOption2.setBounds(new Rectangle(15, 72, 590, 100));
 			jScrollPane_mealOption2.setViewportView(getJTable_mealOption2());
 		}
 		return jScrollPane_mealOption2;
 	}
 	@SuppressWarnings("static-access")
 	private JTable getJTable_mealOption2() {
 		if (jTable_mealOption2 == null) {
 			DefaultTableModel model= new DefaultTableModel();
 			model.setColumnIdentifiers(new Object[]{"Meal Name","Price/Hr","Halal","Vegetarian","Description"});
 			jTable_mealOption2 = new JTable(model);
 			jTable_mealOption2.setOpaque(false);
 			jTable_mealOption2.setEnabled(false);
 			jTable_mealOption2.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			jTable_mealOption2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable_mealOption2.setIntercellSpacing(new Dimension(5, 5));
 			jTable_mealOption2.setRowSelectionAllowed(true);
 			jTable_mealOption2.setShowGrid(false);
 			jTable_mealOption2.setFocusable(false);
 			jTable_mealOption2.setColumnSelectionAllowed(false);
 			jTable_mealOption2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTable_mealOption2.setAutoResizeMode(getJTable_mealOption2().AUTO_RESIZE_OFF);
 			jTable_mealOption2.getColumnModel().getColumn(0).setPreferredWidth(450);
 			jTable_mealOption2.getColumnModel().getColumn(1).setPreferredWidth(115);
 			jTable_mealOption2.getColumnModel().getColumn(2).setPreferredWidth(50);
 			jTable_mealOption2.getColumnModel().getColumn(3).setPreferredWidth(80);
 			jTable_mealOption2.getColumnModel().getColumn(4).setPreferredWidth(435);
 		}
 		return jTable_mealOption2;
 	}
 	private JPanel getJPanel_mealOption3() {
 		if (jPanel_mealOption3 == null) {
 			jPanel_mealOption3 = new JPanel();
 			jPanel_mealOption3.setLayout(null);
 			jPanel_mealOption3.setPreferredSize(new Dimension(609,70));
 			jPanel_mealOption3.setMaximumSize(new Dimension(609,70));
 			jPanel_mealOption3.setMinimumSize(new Dimension(609,70));
 			jPanel_mealOption3.add(jLabel_mealOption3, null);
 			jPanel_mealOption3.add(getJTextField_mealOption3(), null);
 			jPanel_mealOption3.add(getJToggleButton_viewMealOption3(), null);
 			jPanel_mealOption3.add(getJScrollPane_mealOption3(), null);
 		}
 		return jPanel_mealOption3;
 	}
 	private JTextField getJTextField_mealOption3() {
 		if (jTextField_mealOption3 == null) {
 			jTextField_mealOption3 = new JTextField();
 			jTextField_mealOption3.setEditable(false);
 			jTextField_mealOption3.setHorizontalAlignment(JTextField.CENTER);
 			jTextField_mealOption3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTextField_mealOption3.setBounds(new Rectangle(96, 22, 360, 30));
 		}
 		return jTextField_mealOption3;
 	}
 	private JToggleButton getJToggleButton_viewMealOption3() {
 		if (jToggleButton_viewMealOption3 == null) {
 			jToggleButton_viewMealOption3 = new JToggleButton();
 			jToggleButton_viewMealOption3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jToggleButton_viewMealOption3.setFocusable(false);
 			jToggleButton_viewMealOption3.setFocusPainted(false);
 			jToggleButton_viewMealOption3.setEnabled(false);
 			jToggleButton_viewMealOption3.setBounds(new Rectangle(465, 22, 136, 30));
 			jToggleButton_viewMealOption3.setText("View Meal Details");
 			jToggleButton_viewMealOption3
 					.addItemListener(new java.awt.event.ItemListener() {
 						@SuppressWarnings("static-access")
 						public void itemStateChanged(java.awt.event.ItemEvent e) {
 							if(jTable_mealOption3.getModel().getRowCount()==0){
 								AdministrateMealControl control= new AdministrateMealControl();
 								getJTable_mealOption3().setModel(control.processRetrieveMealMenuByID(getJTextField_mealOption3().getName()));
 								jTable_mealOption3.setAutoResizeMode(getJTable_mealOption3().AUTO_RESIZE_OFF);
 								jTable_mealOption3.getColumnModel().getColumn(0).setPreferredWidth(450);
 								jTable_mealOption3.getColumnModel().getColumn(1).setPreferredWidth(115);
 								jTable_mealOption3.getColumnModel().getColumn(2).setPreferredWidth(50);
 								jTable_mealOption3.getColumnModel().getColumn(3).setPreferredWidth(80);
 								jTable_mealOption3.getColumnModel().getColumn(4).setPreferredWidth(435);
 							}
 							if(jToggleButton_viewMealOption3.isSelected()){
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height+102));
 								jPanel_mealOption3.setPreferredSize(new Dimension(609,172));
 								jPanel_mealOption3.setMaximumSize(new Dimension(609,172));
 								jPanel_mealOption3.setMinimumSize(new Dimension(609,172));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height+102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height+102));
 								getJFrame_StandardPackage().repaint();
 							}
 							else{
 								jPanel_meal.setMaximumSize(new Dimension(jPanel_meal.getMaximumSize().width,jPanel_meal.getMaximumSize().height-102));
 								jPanel_mealOption3.setPreferredSize(new Dimension(609,70));
 								jPanel_mealOption3.setMaximumSize(new Dimension(609,70));
 								jPanel_mealOption3.setMinimumSize(new Dimension(609,70));
 								getJPanel_standardPackage().setPreferredSize(new Dimension(getJPanel_standardPackage().getPreferredSize().width,getJPanel_standardPackage().getPreferredSize().height-102));	
 								getJPanel_body().setSize(new Dimension(getJPanel_body().getSize().width,getJPanel_body().getSize().height-102));
 								getJFrame_StandardPackage().repaint();
 							}
 						}
 					});
 		}
 		return jToggleButton_viewMealOption3;
 	}
 	private JScrollPane getJScrollPane_mealOption3() {
 		if (jScrollPane_mealOption3 == null) {
 			jScrollPane_mealOption3 = new JScrollPane();
 			jScrollPane_mealOption3.setBounds(new Rectangle(15, 72, 590, 100));
 			jScrollPane_mealOption3.setViewportView(getJTable_mealOption3());
 		}
 		return jScrollPane_mealOption3;
 	}
 	@SuppressWarnings({ "static-access" })
 	private JTable getJTable_mealOption3() {
 		if (jTable_mealOption3 == null) {
 			DefaultTableModel model= new DefaultTableModel();
 			model.setColumnIdentifiers(new Object[]{"Meal Name","Price/Hr","Halal","Vegetarian","Description"});
 			jTable_mealOption3 = new JTable(model);
 			jTable_mealOption3.setOpaque(false);
 			jTable_mealOption3.setEnabled(false);
 			jTable_mealOption3.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 			jTable_mealOption3.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			jTable_mealOption3.setIntercellSpacing(new Dimension(5, 5));
 			jTable_mealOption3.setRowSelectionAllowed(true);
 			jTable_mealOption3.setShowGrid(false);
 			jTable_mealOption3.setFocusable(false);
 			jTable_mealOption3.setColumnSelectionAllowed(false);
 			jTable_mealOption3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jTable_mealOption3.setAutoResizeMode(getJTable_mealOption3().AUTO_RESIZE_OFF);
 			jTable_mealOption3.getColumnModel().getColumn(0).setPreferredWidth(450);
 			jTable_mealOption3.getColumnModel().getColumn(1).setPreferredWidth(115);
 			jTable_mealOption3.getColumnModel().getColumn(2).setPreferredWidth(50);
 			jTable_mealOption3.getColumnModel().getColumn(3).setPreferredWidth(80);
 			jTable_mealOption3.getColumnModel().getColumn(4).setPreferredWidth(435);
 		}
 		return jTable_mealOption3;
 	}
 	private JToolBar getJJToolBarBar_StandardPackage() {
 		if (jJToolBarBar_StandardPackage == null) {
 			jJToolBarBar_StandardPackage = new JToolBar();
 			jJToolBarBar_StandardPackage.setFloatable(false);
 			jJToolBarBar_StandardPackage.setFocusable(false);
 			jJToolBarBar_StandardPackage.add(getJButton_standardPackage());
 		}
 		return jJToolBarBar_StandardPackage;
 	}
 	private JButton getJButton_standardPackage() {
 		if (jButton_standardPackage == null) {
 			jButton_standardPackage = new JButton();
 			jButton_standardPackage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
 			jButton_standardPackage.setText("Select This Package");
 			jButton_standardPackage.setIcon(new ImageIcon(getClass().getResource("/Images/SOM/Select.png")));
 			jButton_standardPackage.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getJTextField_seectedPackageTitle().setText(getJTextField_packageTitle().getText());
 					getJTextField_seectedPackageTitle().setName(getJTextField_packageTitle().getName());
 					getJButton_selectDate().setEnabled(true);
 					cPackageTitle=getJTextField_packageTitle().getText();
 					getJTextField_selectedDate().setName(getJTextField_ballroomTitle().getName());
 					getJFrame_StandardPackage().setVisible(false);
 					isStandard=true;
 					isCustom=false;
 				}
 			});
 		}
 		return jButton_standardPackage;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/********************************************************
 	 *				Start of Custom Methods
 	 *******************************************************/
 	/********************************************************
 	  * Method Name : validate
 	  * Input Parameter : void
 	  * Purpose : To rvalidate
 	  * Return :boolean
 	  *******************************************************/
 	public boolean validate(){
 		boolean validate=true;
 		if(getJTextField_seectedPackageTitle().getText().equals("")){
 			validate=false;
 			JOptionPane.showMessageDialog(null, "Please select a package", "Missing Requirement", JOptionPane.WARNING_MESSAGE);
 			getJButton_selectStandardPackage().requestFocus();
 		}
 		else if(getJTextField_selectedDate().getText().equals("")){
 			validate=false;
 			JOptionPane.showMessageDialog(null, "Please select a prefered date", "Missing Requirement", JOptionPane.WARNING_MESSAGE);
 			getJButton_selectDate().requestFocus();
 		}
 		else if(getJComboBox_selectTime().getSelectedIndex()==0||getJComboBox_selectTime().getSelectedIndex()==getJComboBox_selectTime().getItemCount()){
 			validate=false;
 			JOptionPane.showMessageDialog(null, "Please select a prefered time", "Missing Requirement", JOptionPane.WARNING_MESSAGE);
 			getJComboBox_selectTime().requestFocus();
 		}
 		else if(getJTextField_eventName().getText().equals("")||getJTextField_eventName().getText().equals("Give Your Event A Name")){
 			validate=false;
 			JOptionPane.showMessageDialog(null, "Please enter a event name", "Missing Requirement", JOptionPane.WARNING_MESSAGE);
 			getJTextField_eventName().requestFocus();
 		}
 		else if(getJTextArea_eventDescription().getText().equals("")||getJTextArea_eventDescription().getText().equals("\n\n                                            Give Your Event A Description")){
 			validate=false;
 			JOptionPane.showMessageDialog(null, "Please enter a event description", "Missing Requirement", JOptionPane.WARNING_MESSAGE);
 			getJTextArea_eventDescription().requestFocus();
 		}
 		return validate;
 	}
 	/********************************************************
 	  * Method Name : retrievePackage
 	  * Input Parameter : void
 	  * Purpose : To retrieve all Package record
 	  * Return :void
 	  *******************************************************/
 	public void retrievePackage(String ID){
 		//retrieve the package details
 		AdministratePackageControl control= new AdministratePackageControl();
 		control.processRetrievePackageByID(ID);
 		//setting the package details in the respective fields
 		getJTextField_packageTitle().setText(control.getPack().getPackageTitle());
 		getJTextField_packageTitle().setName(control.getPack().getPackageID());
 		getJTextArea_packageDescription().setText(control.getPack().getPackageDescription());
 		//setting the ballroom details in the respective fields
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		getJTextField_ballroomTitle().setText(control.getBallroom().getBallroomName());
 		getJTextField_ballroomTitle().setName(control.getBallroom().getBallroomID());
 		getJTextField_ballroomSize().setText(control.getBallroom().getBallroomSize());
 		getJTextField_ballroomFinalPrice().setText(fmt.format(control.getBallroom().getBallroomFinalPrice()));
 		if(control.getFacility().isFacilityParking()){
 			getJTextField_ballroomParking().setText("Free Parking");
 		}
 		if(!control.getFacility().isFacilityParking()){
 			getJTextField_ballroomParking().setText("narmal Parking Rate");
 		}
 		getJTextField_ballroomWeekendSurcharge().setText(fmt.format(control.getFacility().getFacilityWeekendExtraCost()));
 		getJTextField_facilityName().setText(control.getFacility().getFacilityName());
 		getJTextArea_facilityAddress().setText(control.getFacility().getFacilityDescription());
 		//setting the meal details in the respective fields
 		if(control.getMealIDs().size()==1){
 			getJTextField_mealOption1().setText(control.getMeals().get(0).getMealTitle());
 			getJTextField_mealOption1().setName(control.getMeals().get(0).getMealID());
 			getJToggleButton_viewMealOption1().setEnabled(true);
 			//set the meal meu here
 		}
 		else if(control.getMealIDs().size()==2){
 			getJTextField_mealOption1().setText(control.getMeals().get(0).getMealTitle());
 			getJTextField_mealOption1().setName(control.getMeals().get(0).getMealID());
 			getJToggleButton_viewMealOption1().setEnabled(true);
 			getJTextField_mealOption2().setText(control.getMeals().get(1).getMealTitle());
 			getJTextField_mealOption2().setName(control.getMeals().get(1).getMealID());
 			getJToggleButton_viewMealOption2().setEnabled(true);
 		}
 		else if(control.getMealIDs().size()==3){
 			getJTextField_mealOption1().setText(control.getMeals().get(0).getMealTitle());
 			getJTextField_mealOption1().setName(control.getMeals().get(0).getMealID());
 			getJToggleButton_viewMealOption1().setEnabled(true);
 			getJTextField_mealOption2().setText(control.getMeals().get(1).getMealTitle());
 			getJTextField_mealOption2().setName(control.getMeals().get(1).getMealID());
 			getJToggleButton_viewMealOption2().setEnabled(true);
 			getJTextField_mealOption3().setText(control.getMeals().get(2).getMealTitle());
 			getJTextField_mealOption3().setName(control.getMeals().get(2).getMealID());
 			getJToggleButton_viewMealOption3().setEnabled(true);
 		}
 		
 	}
 	
 	/********************************************************
 	  * Method Name : retrieveAvailableTimings
 	  * Input Parameter : void
 	  * Purpose : To retrieve all Package record
 	  * Return :void
 	  *******************************************************/
 	@SuppressWarnings("unchecked")
 	public void filterTiming(String ballroomID, String Date){
 		InitiateEventControl control= new InitiateEventControl();
 		ArrayList<String> timings= new ArrayList<String>();
 		timings=control.processRetrieveTimings(ballroomID, Date);
 		//get the ballroom id and date and package id, check the event table for the package than the date
 		//select * from event where packageid=id and event date=date
 		//returns a arraylist of date max is 2
 		//for loop to filter the timings
 		getJComboBox_selectTime().removeAllItems();
 		if(timings.size()==0){
 			getJComboBox_selectTime().addItem("==================");
 			getJComboBox_selectTime().addItem("12.00 pm");
 			getJComboBox_selectTime().addItem("3.00 pm");
 			getJComboBox_selectTime().addItem("6.00 pm");
 			getJComboBox_selectTime().addItem("7.00 pm");
 			getJComboBox_selectTime().addItem("==================");
 			getJComboBox_selectTime().setEnabled(true);
 		}
 		else{
 			getJComboBox_selectTime().addItem("==================");
 			getJComboBox_selectTime().addItem("12.00 pm");
 			getJComboBox_selectTime().addItem("3.00 pm");
 			getJComboBox_selectTime().addItem("6.00 pm");
 			getJComboBox_selectTime().addItem("7.00 pm");
 			getJComboBox_selectTime().addItem("==================");
 			getJComboBox_selectTime().setEnabled(true);
 			String[]delete= new String[6];
 			//fill the array with""
 			for(int i=0; i<delete.length;i++){
 				delete[i]="";
 			}
 			//get the dates to delete
 			for(int i=0; i<timings.size();i++){
 				for(int x=0;x<getJComboBox_selectTime().getItemCount();x++){
 					if(getJComboBox_selectTime().getItemAt(x).toString().equals(timings.get(i))){
 						delete[x]=timings.get(i);
 					}
 					else{
 						if(delete[x].equals(""))
 							delete[x]="";
 					}
 				}
 			}
 			//remove the dates
 			for(int i=0;i<delete.length;i++){
 				if(!delete[i].equals("")){
 					getJComboBox_selectTime().removeItem(delete[i]);
 				}
 			}
 		}		
 			
 		}
 	
 	/********************************************************
 	  * Method Name : createEvent
 	  * Input Parameter : void
 	  * Purpose : To retrieve all Package record
 	  * Return :void
 	  *******************************************************/
 	public void createEvent(){
 		
 		String newPackageID=duplicate();
 		
 		//getting the username
 		
 		//finally create the event
 		InitiateEventControl control= new InitiateEventControl(newPackageID, "kaiquan88@gmail.com","Pending",getJTextField_selectedDate().getText(),getJComboBox_selectTime().getSelectedItem().toString(),getJTextField_eventName().getText(),getJTextArea_eventDescription().getText());
 		String eventID=control.processInitiateEvent();
 		if(eventID!=null){
 			this.getJFrame().dispose();
 			new InvitationWelcomeView(Integer.parseInt(eventID));
 		}
 		else{
 			System.out.println("smth wrong");
 		}
 		
 	}
 	/********************************************************
 	  * Method Name : depulicate
 	  * Input Parameter : void
 	  * Purpose : To create a duplicate ercord for the event created
 	  * Return :void
 	  *******************************************************/
 	public String duplicate(){
 		String[]newMeals= new String[3];
 		newMeals[0]="null";
 		newMeals[1]="null";
 		newMeals[2]="null";
 		String newEntertainmentID="";
 		String newPackageID="";
 		
 		if(isStandard){
 			//recreate the entire package(); 
 			//retrieve the selected package (standard package)
 			AdministratePackageControl pkControl= new AdministratePackageControl();
 			pkControl.processRetrievePackageByID(getJTextField_seectedPackageTitle().getName());
 			System.out.println(pkControl.getPack().getEntertainmentID());
 			if(pkControl.getPack().getEntertainmentID()!=null){
 				//retrieveing the entertainmtne and entertainment menu
 				AdministrateEntertainmentControl entControl= new AdministrateEntertainmentControl();
 				entControl.processRetrieveEntertainmentByID(pkControl.getPack().getEntertainmentID());
 				DefaultTableModel entertainmentMenu=entControl.getModel();
 				//recreate the entertainment
 				AdministrateEntertainmentControl newControl= new AdministrateEntertainmentControl(entControl.getEntertainment().getEntertainmentTitle(), entControl.getEntertainment().getEntertainmentDescription(), true, entControl.getEntertainment().getEntertainmentDiscount(), entControl.getEntertainment().getEntertainmentPrice(),entControl.getEntertainment().getEntertainmentFinalPrice(),entControl.getEntertainment().getEntertainmentHits(), true);
 				newControl.setModel(entertainmentMenu);
 				//create and store the record
 				newEntertainmentID=entControl.processCreateEntertainment();
 			}
 			//retrieve the meals
 			pkControl.processRetrieveMealOptions(pkControl.getPack().getPackageID());
 			int mealNumber=pkControl.getMealIDs().size();
 			
 			for(int i=0; i<mealNumber;i++){
 				//recreating the meal record inviually
 				AdministrateMealControl mealMenu= new AdministrateMealControl();
 				mealMenu.procesRetrieveMealByID(pkControl.getMealIDs().get(i));
 				AdministrateMealControl newMeal= new AdministrateMealControl(mealMenu.getMeal().getMealTitle(),mealMenu.getMeal().getMealDescription(), mealMenu.getMeal().getMealType(), mealMenu.getMeal().getMealPricePerHead(),mealMenu.getMeal().getMealDiscount(),mealMenu.getMeal().getMealFinalPrice(),true,mealMenu.getMeal().getMealHits() ,true);
 				newMeal.setModel(mealMenu.processRetrieveMealMenuByID(pkControl.getMealIDs().get(i)));
 				//create and store the id
 				newMeals[i]=(newMeal.processCreateMeal());
 			}
 			
 			//recreating the package
 			AdministratePackageControl newPackage= new AdministratePackageControl("", newEntertainmentID, pkControl.getBallroom().getBallroomID(),"Standard", pkControl.getPack().getPackageTitle(),pkControl.getPack().getPackageDescription(),true, pkControl.getPack().getPackageHits(),pkControl.getPack().getPackageDiscount(),true);
 			newPackageID=newPackage.processCreatePackage(newMeals[0], newMeals[1], newMeals[2]);
 			
 			
 			
 		}
 		else if(isCustom){
 			//create the ballroom
 			int i=0;
			if(cEntertainmentID!=null){
				//retrieve the entertainment
				AdministrateEntertainmentControl eControl= new AdministrateEntertainmentControl();
				eControl.processRetrieveEntertainmentByID(cEntertainmentID);
				DefaultTableModel entertainmentMenu=eControl.getModel();
				//re-create the entertainment
				AdministrateEntertainmentControl newEControl= new AdministrateEntertainmentControl(eControl.getEntertainment().getEntertainmentTitle(), eControl.getEntertainment().getEntertainmentDescription(), true, eControl.getEntertainment().getEntertainmentDiscount(), eControl.getEntertainment().getEntertainmentPrice(),eControl.getEntertainment().getEntertainmentFinalPrice(),eControl.getEntertainment().getEntertainmentHits(), true);
				newEControl.setModel(entertainmentMenu);
				newEntertainmentID=newEControl.processCreateEntertainment();
			}
 			for(i=0; i<2; i++){
 				//create the entertainment
				
 				newMeals[0]="null";
 				newMeals[1]="null";
 				newMeals[2]="null";
 				//create the meal
 				if(cMealOption1ID!=null){
 					AdministrateMealControl mControl=new AdministrateMealControl();
 					mControl.procesRetrieveMealByID(cMealOption1ID);
 					AdministrateMealControl newMControl= new AdministrateMealControl(mControl.getMeal().getMealTitle(),mControl.getMeal().getMealDescription(), mControl.getMeal().getMealType(), mControl.getMeal().getMealPricePerHead(),mControl.getMeal().getMealDiscount(),mControl.getMeal().getMealFinalPrice(),true,mControl.getMeal().getMealHits() ,true);
 					newMControl.setModel(mControl.processRetrieveMealMenuByID(cMealOption1ID));
 					newMeals[0]=(newMControl.processCreateMeal());
 				}
 				if(cMealOption2ID!=null){
 					AdministrateMealControl mControl=new AdministrateMealControl();
 					mControl.procesRetrieveMealByID(cMealOption2ID);
 					AdministrateMealControl newMControl= new AdministrateMealControl(mControl.getMeal().getMealTitle(),mControl.getMeal().getMealDescription(), mControl.getMeal().getMealType(), mControl.getMeal().getMealPricePerHead(),mControl.getMeal().getMealDiscount(),mControl.getMeal().getMealFinalPrice(),true,mControl.getMeal().getMealHits() ,true);
 					newMControl.setModel(mControl.processRetrieveMealMenuByID(cMealOption2ID));
 					newMeals[1]=(newMControl.processCreateMeal());
 				}
 				if(cMealOption3ID!=null){
 					AdministrateMealControl mControl=new AdministrateMealControl();
 					mControl.procesRetrieveMealByID(cMealOption3ID);
 					AdministrateMealControl newMControl= new AdministrateMealControl(mControl.getMeal().getMealTitle(),mControl.getMeal().getMealDescription(), mControl.getMeal().getMealType(), mControl.getMeal().getMealPricePerHead(),mControl.getMeal().getMealDiscount(),mControl.getMeal().getMealFinalPrice(),true,mControl.getMeal().getMealHits() ,true);
 					newMControl.setModel(mControl.processRetrieveMealMenuByID(cMealOption3ID));
 					newMeals[2]=(newMControl.processCreateMeal());
 				}
 				
 			
 				//create the package
 				if(i==0){
 					AdministratePackageControl newPackage= new AdministratePackageControl("", newEntertainmentID, cBallroomID,"Custom",cPackageTitle,cPackageDescription,true, 0,0,true);
 					newPackageID=newPackage.processCreatePackage(newMeals[0], newMeals[1], newMeals[2]);
 				//get the new package id and return
 				}
 				else{
 					AdministratePackageControl newPackage= new AdministratePackageControl("", newEntertainmentID, cBallroomID,"Custom",cPackageTitle,cPackageDescription,true, 0,0,false);
 					newPackageID=newPackage.processCreatePackage(newMeals[0], newMeals[1], newMeals[2]);
 				}
 			}
 		}
 		return newPackageID;
 	}
 
 	
 
 	
 
 	
 
 	
 	
 	
 	
 	
 	
 
 	
 
 }
