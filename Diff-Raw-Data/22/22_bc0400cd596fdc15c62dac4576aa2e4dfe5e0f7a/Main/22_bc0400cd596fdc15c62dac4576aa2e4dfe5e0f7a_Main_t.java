 package org.gui;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JLayeredPane;
 import javax.swing.JEditorPane;
 import javax.swing.JButton;
 
 import org.command.Invoker;
 import org.errors.table.NoMoreRoomException;
 import org.info.InformationProvider;
 import org.queue.MainQueue;
 import org.shared.HtmlUtils;
 import org.shared.Utils;
 import org.system.SystemInterface;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JPanel;
 import java.awt.Color;
 import javax.swing.JInternalFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import java.awt.Font;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.ScrollPaneConstants;
 import java.awt.SystemColor;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeSet;
 
 import javax.swing.DropMode;
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JSeparator;
 import javax.swing.border.LineBorder;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.JProgressBar;
 import javax.swing.SwingConstants;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 import javax.swing.DefaultComboBoxModel;
 import org.info.comment.CommentCategoryE;
 import java.awt.FlowLayout;
 import java.awt.BorderLayout;
 import javax.swing.BoxLayout;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.GridLayout;
 import java.awt.Component;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import java.awt.Toolkit;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Main.
  */
 public class Main {
 
 	/** The info obj. */
 	private InformationProvider infoObj=InformationProvider.getSingletonObject(); //NEED TO REMOVE AND USE SystemInterfaceObj
 			
 	private MainQueue Sim=new MainQueue();
 	
 	/** The System interface obj. */
 	private SystemInterface SystemInterfaceObj=new SystemInterface();
 	
 	/** The frm manager. */
 	private JFrame frmManager;
 	
 	/** The editor menuoutput. */
 	private JEditorPane editorMenuoutput;
 	
 	/** The editor tables. */
 	private JEditorPane editorTables;
 	
 	/** The editor pane_recipts_recipt. */
 	private JEditorPane editorPane_recipts_recipt;
 	
 	/** The list_recipts. */
 	private JList<String> list_recipts;
 	
 	/** The slider. */
 	private JSlider slider;
 	
 	private Timer timer_gui_tabbedPaneUpdating;
 	
 	/** The lbl current speed. */
 	private JLabel lblCurrentSpeed;
 	private JButton btnStartSimulation;
 	private JButton btnPauseSimulation;
 	private JTabbedPane tabbedPane;
 
 	private String strPreviousTable = "";
 	private JProgressBar progressBar;
 	private JComboBox comboBox_Comments;
 	private JEditorPane editorPane_Comments;
 	
 	/**
 	 * Launch the application.
 	 *
 	 * @param args the arguments
 	 */
 	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Main window = new Main();
 					window.frmManager.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Main() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmManager = new JFrame();
 		frmManager.setTitle("Manager");
 		frmManager.setBounds(100, 100, 760, 600);
 		frmManager.setMinimumSize(new Dimension(760, 600));
 		frmManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				
 				if(strPreviousTable.equals("Simulation")){
 					timer_gui_tabbedPaneUpdating.cancel();	
 				}
 				
 				
 				
 		        int index = tabbedPane.getSelectedIndex();
 		        String titleAt = tabbedPane.getTitleAt(index);
 				System.out.println("Tab changed to: " + titleAt + " from " + strPreviousTable);
 		        strPreviousTable=titleAt;
 		        
 		        if(titleAt.equals("Simulation")){
 		        	  timer_gui_tabbedPaneUpdating = new Timer();
 				        timer_gui_tabbedPaneUpdating.schedule(new UpdateSimulationTask()
 					        ,0,//initial delay
 					        1000);  //subsequent rate
 		        }
 		      
 			}
 		});
 		frmManager.getContentPane().setLayout(new BoxLayout(frmManager.getContentPane(), BoxLayout.X_AXIS));
 		frmManager.getContentPane().add(tabbedPane);
 		
 		JPanel panel_menu = new JPanel();
 		tabbedPane.addTab("Menu", null, panel_menu, null);
 		tabbedPane.setEnabledAt(0, true);
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton btnGetCompleteMenu = new JButton("Get Complete Menu");
 		panel_2.add(btnGetCompleteMenu);
 		
 		JButton btnGetHeartHealthy = new JButton("Get Heart Healthy Menu");
 		panel_2.add(btnGetHeartHealthy);
 		
 		JButton btnGetPriceMenu = new JButton("Get Price Menu");
 		panel_2.add(btnGetPriceMenu);
 		
 		JButton btnGetIngredientMenu = new JButton("Get Ingredient Menu");
 		panel_2.add(btnGetIngredientMenu);
 		
 		JPanel panel_3 = new JPanel();
 		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
 		
 		JScrollPane scrollPane = new JScrollPane();
 		panel_3.add(scrollPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		editorMenuoutput = new JEditorPane();
 		scrollPane.setViewportView(editorMenuoutput);
 		editorMenuoutput.setEditable(false);
 		editorMenuoutput.setContentType("text/html");
 		GroupLayout gl_panel_menu = new GroupLayout(panel_menu);
 		gl_panel_menu.setHorizontalGroup(
 			gl_panel_menu.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, gl_panel_menu.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_menu.createParallelGroup(Alignment.TRAILING)
 						.addComponent(panel_3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 719, Short.MAX_VALUE)
 						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 719, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap())
 		);
 		gl_panel_menu.setVerticalGroup(
 			gl_panel_menu.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_panel_menu.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
 					.addGap(18)
 					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 		);
 		panel_menu.setLayout(gl_panel_menu);
 		//Action Listeners
 		btnGetIngredientMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {	
 				
 				String[] possibilities = infoObj.getUniqueIngredients();
 				
 				String s = (String)JOptionPane.showInputDialog(
 				                    frmManager,
 				                    "What Ingredient do you want to view??"
 				                    + "",
 				                    "Menu",
 				                    JOptionPane.PLAIN_MESSAGE,null,
 				                    possibilities,
 				                    null);
 
 				//If a string was returned, say so.
 				if ((s != null) && (s.length() > 0)) {
 				    editorMenuoutput.setText(HtmlUtils.convertMenuStringToHtml(SystemInterfaceObj.getIngredientMenu(s).toString()));
 				    return;
 				}else{
 					editorMenuoutput.setText("Canceled");
 					return;
 				}
 				
 				
 			}
 		});
 		//Action Listeners
 		btnGetPriceMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {							
 				Double s=null;
 				try{
 					s = Double.parseDouble((String)JOptionPane.showInputDialog(
 		                    frmManager,
 		                    "Set Max Price you want to view. Input value needs to be a number"
 		                    + "",
 		                    "Menu",
 		                    JOptionPane.PLAIN_MESSAGE,null,
 		                    null,
 		                    null));
 
 				}catch(Exception exin){
 					//editorMenuoutput.setText(exin.toString());
 					//return;
 					s=null;
 				}
 				
 				//If a string was returned, say so.
 				if ((s != null)&&(s>=0.0)) {
 				    editorMenuoutput.setText(HtmlUtils.convertMenuStringToHtml(SystemInterfaceObj.getPriceMenu(s).toString()));
 				    return;
 				}else{
 					editorMenuoutput.setText("Canceled");
 					return;
 				}
 	
 			}
 		});
 		//Action Listeners
 		btnGetHeartHealthy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				editorMenuoutput.setText(HtmlUtils.convertMenuStringToHtml(SystemInterfaceObj.getHeartHealthyMenu().toString()));				
 			}
 		});
 		
 		//Action Listeners
 		//Get Complete menu
 		btnGetCompleteMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				editorMenuoutput.setText(HtmlUtils.convertMenuStringToHtml(SystemInterfaceObj.getGetCompleteMenu())); 
 			}
 		});
 		
 		JPanel panel_tables = new JPanel();
 		tabbedPane.addTab("Tables", null, panel_tables, null);
 		
 		JPanel panel_tables_group1 = new JPanel();
 		
 		JPanel panel_tables_group2 = new JPanel();
 		panel_tables_group2.setAlignmentX(Component.LEFT_ALIGNMENT);
 		panel_tables_group2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton btn_tables_getTables = new JButton("Get All Tables");
 		panel_tables_group2.add(btn_tables_getTables);
 		
 		JButton btn_tables_getAvailableTables = new JButton("Get Available Tables");
 		panel_tables_group2.add(btn_tables_getAvailableTables);
 		GroupLayout gl_panel_tables = new GroupLayout(panel_tables);
 		gl_panel_tables.setHorizontalGroup(
 			gl_panel_tables.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_tables.createSequentialGroup()
 					.addGroup(gl_panel_tables.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_tables.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(panel_tables_group2, GroupLayout.PREFERRED_SIZE, 375, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_panel_tables.createSequentialGroup()
 							.addGap(7)
 							.addComponent(panel_tables_group1, GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)))
 					.addContainerGap())
 		);
 		gl_panel_tables.setVerticalGroup(
 			gl_panel_tables.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, gl_panel_tables.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(panel_tables_group1, GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(panel_tables_group2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(9))
 		);
 		panel_tables_group1.setLayout(new BoxLayout(panel_tables_group1, BoxLayout.X_AXIS));
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		panel_tables_group1.add(scrollPane_1);
 		
 		editorTables = new JEditorPane();
 		editorTables.setContentType("text/html");
 		editorTables.setEditable(false);
 		scrollPane_1.setViewportView(editorTables);
 		panel_tables.setLayout(gl_panel_tables);
 		//Action Listeners
 		btn_tables_getAvailableTables.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		//Action Listeners
 		btn_tables_getTables.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				editorTables.setText(HtmlUtils.convertMenuStringToHtml(SystemInterfaceObj.getTables()));
 			}
 		});
 		
 		JPanel panel_recipt = new JPanel();
 		tabbedPane.addTab("Recipts", null, panel_recipt, null);
 		panel_recipt.setLayout(null);
 		
 		JLabel lblLblreciptslist = new JLabel("Recipt List");
 		lblLblreciptslist.setBounds(10, 11, 111, 14);
 		panel_recipt.add(lblLblreciptslist);
 		
 		JScrollPane scrollPane_recipts_recipt = new JScrollPane();
		scrollPane_recipts_recipt.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane_recipts_recipt.setBounds(188, 36, 444, 471);
 		panel_recipt.add(scrollPane_recipts_recipt);
 		
 		editorPane_recipts_recipt = new JEditorPane();
 		editorPane_recipts_recipt.setEditable(false);
 		editorPane_recipts_recipt.setContentType("text/html");
 		scrollPane_recipts_recipt.setViewportView(editorPane_recipts_recipt);
 		
 		JScrollPane scrollPane_recipts_reciptlist = new JScrollPane();
		scrollPane_recipts_reciptlist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane_recipts_reciptlist.setBounds(10, 36, 168, 437);
 		panel_recipt.add(scrollPane_recipts_reciptlist);
 		
 		list_recipts = new JList();
 		scrollPane_recipts_reciptlist.setViewportView(list_recipts);
 		
 		JButton btn_recipt_getReciptList = new JButton("Get Recipt List");
 		btn_recipt_getReciptList.setBounds(10, 484, 168, 23);
 		panel_recipt.add(btn_recipt_getReciptList);
 		
 		JPanel panel_comments = new JPanel();
 		panel_comments.setLayout(null);
 		tabbedPane.addTab("Comments", null, panel_comments, null);
 		
 		JLabel lblCommentList = new JLabel("Comment List");
 		lblCommentList.setBounds(10, 11, 111, 14);
 		panel_comments.add(lblCommentList);
 		
 		JScrollPane scrollPane_2 = new JScrollPane();
 		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane_2.setBounds(20, 36, 612, 437);
 		panel_comments.add(scrollPane_2);
 		
 		editorPane_Comments = new JEditorPane();
 		editorPane_Comments.setEditable(false);
 		editorPane_Comments.setContentType("text/html");
 		scrollPane_2.setViewportView(editorPane_Comments);
 		
 		JButton btnGetComment = new JButton("Get Comment");
 	
 		btnGetComment.setBounds(368, 484, 168, 23);
 		panel_comments.add(btnGetComment);
 		
 		comboBox_Comments = new JComboBox();
 		comboBox_Comments.setModel(new DefaultComboBoxModel(CommentCategoryE.values()));
 		comboBox_Comments.setBounds(191, 485, 168, 20);
 		panel_comments.add(comboBox_Comments);
 		
 		btnGetComment.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				CommentCategoryE CommentCat=(CommentCategoryE) comboBox_Comments.getItemAt(comboBox_Comments.getSelectedIndex());
 				System.out.println(CommentCat);
 				
 				String CurrentResult=infoObj.getCommentMgr().getCommentsByCategoryStr(CommentCat);
 				
 				if(CurrentResult.trim().length()==0){
 					editorPane_Comments.setText("No Comments for " + CommentCat);
 				}else{
 					editorPane_Comments.setText(HtmlUtils.convertMenuStringToHtml(CurrentResult.trim()));
 				}
 				
 				//TODO:Complete Add Comment
 				
 			}
 		});
 		
 		JPanel panel_orders = new JPanel();
 		panel_orders.setLayout(null);
 		tabbedPane.addTab("Orders", null, panel_orders, null);
 		
 		JLabel lbl_order_orderList = new JLabel("Order List");
 		lbl_order_orderList.setBounds(10, 11, 111, 14);
 		panel_orders.add(lbl_order_orderList);
 		
 		JScrollPane scrollPane_4 = new JScrollPane();
 		scrollPane_4.setBounds(188, 36, 444, 471);
 		panel_orders.add(scrollPane_4);
 		
 		final JEditorPane editorOrderOutput = new JEditorPane();
 		editorOrderOutput.setEditable(false);
 		editorOrderOutput.setContentType("text/html");
 		scrollPane_4.setViewportView(editorOrderOutput);
 		
 		JScrollPane scrollPane_5 = new JScrollPane();
 		scrollPane_5.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 		scrollPane_5.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane_5.setBounds(10, 36, 168, 437);
 		panel_orders.add(scrollPane_5);
 		
 		JList list_1 = new JList();
 		scrollPane_5.setViewportView(list_1);
 
 //Get orders
 		JButton btnGetOrderList = new JButton("Get Order List");
 		
 		//Action Listeners
 		btnGetOrderList.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				String test = HtmlUtils.convertOrdersStringToHtml(SystemInterfaceObj.getSampleOrders().toString());
 				editorOrderOutput.setText(HtmlUtils.convertOrdersStringToHtml(SystemInterfaceObj.getSampleOrders().toString()));
 			}
 		});
 		
 		btnGetOrderList.setBounds(10, 484, 168, 23);
 		panel_orders.add(btnGetOrderList);
 		
 		JPanel panel_simulation = new JPanel();
 		panel_simulation.setLayout(null);
 		tabbedPane.addTab("Simulation", null, panel_simulation, null);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.setBounds(12, 13, 618, 124);
 		panel_simulation.add(panel);
 		panel.setLayout(null);
 		
 		lblCurrentSpeed = new JLabel("Current Speed: 5");
 		lblCurrentSpeed.setBounds(12, 91, 142, 14);
 		panel.add(lblCurrentSpeed);
 		
 		slider = new JSlider();
 		slider.setBounds(12, 38, 200, 52);
 		panel.add(slider);
 		slider.setValue(5);
 		slider.setSnapToTicks(true);
 		slider.setMinimum(1);
 		slider.setMaximum(10);
 		slider.setPaintLabels(true);
 		slider.setPaintTicks(true);
 		slider.setMajorTickSpacing(1);
 		
 		JLabel lblSpeed = new JLabel("Speed");
 		lblSpeed.setBounds(12, 23, 46, 14);
 		panel.add(lblSpeed);
 		
 		btnStartSimulation = new JButton("Start Simulation");
 		btnStartSimulation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Sim.start(Integer.parseInt(slider.getValue()+"00"));
 				
 				btnStartSimulation.setEnabled(false);
 				btnPauseSimulation.setEnabled(true);
 				slider.setEnabled(false);
 			}
 		});
 		btnStartSimulation.setBounds(239, 19, 152, 23);
 		panel.add(btnStartSimulation);
 		
 		btnPauseSimulation = new JButton("Pause Simulation");
 		btnPauseSimulation.setEnabled(false);
 		btnPauseSimulation.setBounds(239, 52, 152, 23);
 		panel.add(btnPauseSimulation);
 		
 		JButton btn_simulation_controls_reset = new JButton("Reset Simulation");
 		btn_simulation_controls_reset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		btn_simulation_controls_reset.setBounds(239, 88, 152, 23);
 		panel.add(btn_simulation_controls_reset);
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setLayout(null);
 		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_1.setBounds(12, 142, 618, 244);
 		panel_simulation.add(panel_1);
 		
 		progressBar = new JProgressBar();
 		progressBar.setValue(100);
 		progressBar.setOrientation(SwingConstants.VERTICAL);
 		progressBar.setBounds(22, 68, 34, 133);
 		panel_1.add(progressBar);
 		
 		JLabel lblTable = new JLabel("Table\r\n Capacity");
 		lblTable.setEnabled(true);
 		lblTable.setBounds(12, 43, 98, 23);
 		panel_1.add(lblTable);
 		
 		JLabel lblCustomersWaiting = new JLabel("Customers Waiting");
 		lblCustomersWaiting.setBounds(12, 27, 157, 14);
 		panel_1.add(lblCustomersWaiting);
 		
 		JButton btnFreeSpace = new JButton("Free Space");
 		btnFreeSpace.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				infoObj.Tables().loadXML("data\\tables\\Table_conf_1.xml");
 			}
 		});
 		btnFreeSpace.setBounds(68, 77, 89, 23);
 		panel_1.add(btnFreeSpace);
 		//Action Listeners
 		btnPauseSimulation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Sim.stop();
 				btnStartSimulation.setEnabled(true);
 				btnPauseSimulation.setEnabled(false);
 				slider.setEnabled(true);
 			}
 		});
 		//Action Listeners
 		slider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				lblCurrentSpeed.setText("Current Speed: "+slider.getValue());				
 			}
 		});
 	}
 	
 	class UpdateSimulationTask extends TimerTask {
         public void run() {
         	try{//Make sure Thread does not stop
                 System.out.println("Simulation Tab Update");
                 //timer.cancel(); //Not necessary because
                                   //we call System.exit
                 String strper=infoObj.Tables().getTotalSeatsAvailablePercent();
                 strper=strper.substring(0,strper.indexOf("."));
                 Integer per=Integer.parseInt(strper);
                 
                 System.out.println(strper+"-"+per);
                 
                 progressBar.setValue(per);
         	}catch(Exception E){
         		//Nothing
         	}                  
             	
         }
     }
 }
