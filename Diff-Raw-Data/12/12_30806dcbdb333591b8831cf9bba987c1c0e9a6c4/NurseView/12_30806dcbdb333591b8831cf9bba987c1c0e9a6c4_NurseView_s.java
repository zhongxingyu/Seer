 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 
 import java.awt.GridLayout;
 import javax.swing.JRadioButton;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import javax.swing.border.LineBorder;
 
 
 public class NurseView extends MonitorView{
 
 	private JFrame frmNurseMonitor;
 	private JFrame frmPatientTrend;
 	private PatientTrend winPatientTrend;
 
 	private NurseStation nurseStation;
 	private NurseView nurseView;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		
 		try {
             // Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} 
 		catch (UnsupportedLookAndFeelException e) {
 			// handle exception
 		}
 		catch (ClassNotFoundException e) {
 			// handle exception
 		}
 		catch (InstantiationException e) {
 			// handle exception
 		}
 		catch (IllegalAccessException e) {
 			// handle exception
 		}
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
					NurseView window = new NurseView();
 					window.frmNurseMonitor.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public NurseView() {
 		nurseView = this;
 		initialize();
 	}
 
 	public NurseView(NurseStation ns) {
 		nurseView = this;
 		nurseStation = ns;
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmNurseMonitor = new JFrame();
 		frmNurseMonitor.setTitle("Nurse Monitor\r\n");
 		frmNurseMonitor.setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\AnDev\\git\\SE442-Heartbeat\\Heartbeat\\heartbeat.jpg"));	
 		frmNurseMonitor.setBounds(100, 100, 450, 300);
 		frmNurseMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frmNurseMonitor.setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmNewPatient = new JMenuItem("New Patient");
 		mntmNewPatient.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NewPatientDialog newPatient = new NewPatientDialog(nurseView);
 				newPatient.setVisible(true);
 			}
 		});
 		mnFile.add(mntmNewPatient);
 		
 		JMenuItem mntmOpenPatient = new JMenuItem("Open Patient");
 		mnFile.add(mntmOpenPatient);
 		
 		JMenuItem mntmClose = new JMenuItem("Close");
 		mnFile.add(mntmClose);
 		
 		JSeparator separator = new JSeparator();
 		mnFile.add(separator);
 		
 		JMenuItem mntmSave = new JMenuItem("Save");
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mnFile.add(mntmExit);
 		
 		JMenu mnEdit = new JMenu("Edit");
 		menuBar.add(mnEdit);
 		
 		JMenuItem mntmCopy = new JMenuItem("Copy");
 		mnEdit.add(mntmCopy);
 		
 		JMenuItem mntmPaste = new JMenuItem("Paste");
 		mnEdit.add(mntmPaste);
 		
 		JMenu mnTools = new JMenu("Tools");
 		menuBar.add(mnTools);
 		
 		JMenuItem mntmPatientType = new JMenuItem("Patient Type");
 		mnTools.add(mntmPatientType);
 		
 		JMenuItem mntmDeletePatient = new JMenuItem("Delete Patient");
 		mnTools.add(mntmDeletePatient);
 		
 		JSeparator separator_2 = new JSeparator();
 		mnTools.add(separator_2);
 		
 		JMenuItem mntmDisplayHistory = new JMenuItem("Display History");
 		mntmDisplayHistory.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Clicked");
 				createTrendWindow();
 			}
 		});
 		mnTools.add(mntmDisplayHistory);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frmNurseMonitor.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 		
 		JPanel panel = new JPanel();
 		tabbedPane.addTab("New tab", null, panel, null);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_1 = new JPanel();
 		panel.add(panel_1, BorderLayout.NORTH);
 		panel_1.setLayout(new GridLayout(0, 2, 0, 0));
 		
 		JTextPane txtpnPatientName = new JTextPane();
 		txtpnPatientName.setEditable(false);
 		txtpnPatientName.setText("Patient Name");
 		txtpnPatientName.setBorder(BorderFactory.createLineBorder(Color.black));
 		panel_1.add(txtpnPatientName);
 		
 		JTextPane txtpnPatientId = new JTextPane();
 		txtpnPatientId.setEditable(false);
 		txtpnPatientId.setText("Patient ID");
 		txtpnPatientId.setBorder(BorderFactory.createLineBorder(Color.black));
 		panel_1.add(txtpnPatientId);
 		
 		JTextPane txtpnAdmitDate = new JTextPane();
 		txtpnAdmitDate.setEditable(false);
 		txtpnAdmitDate.setBorder(new LineBorder(new Color(0, 0, 0)));
 		txtpnAdmitDate.setText("Admit Date:");
 		panel_1.add(txtpnAdmitDate);
 		
 		JTextPane textPane_4 = new JTextPane();
 		textPane_4.setEditable(false);
 		textPane_4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		textPane_4.setText("11/30/2012");
 		panel_1.add(textPane_4);
 		
 		JPanel panel_2 = new JPanel();
 		panel.add(panel_2, BorderLayout.CENTER);
 		panel_2.setLayout(new GridLayout(0, 3, 0, 0));
 		
 		JTextPane txtpnNibp = new JTextPane();
 		txtpnNibp.setEditable(false);
 		txtpnNibp.setText("NIBP:");
 		panel_2.add(txtpnNibp);
 		
 		JTextPane textPane = new JTextPane();
 		textPane.setEditable(false);
 		textPane.setText("120/80");
 		panel_2.add(textPane);
 		
 		JRadioButton radioButton = new JRadioButton("");
 		radioButton.setEnabled(false);
 		radioButton.setSelected(false);
 		panel_2.add(radioButton);
 		
 		JTextPane txtpnPulse = new JTextPane();
 		txtpnPulse.setEditable(false);
 		txtpnPulse.setText("Pulse:");
 		panel_2.add(txtpnPulse);
 		
 		JTextPane textPane_1 = new JTextPane();
 		textPane_1.setEditable(false);
 		textPane_1.setText("60");
 		panel_2.add(textPane_1);
 		
 		JRadioButton radioButton_1 = new JRadioButton("");
 		radioButton_1.setEnabled(false);
 		radioButton_1.setSelected(false);
 		panel_2.add(radioButton_1);
 		
 		JTextPane txtpnTemp = new JTextPane();
 		txtpnTemp.setEditable(false);
 		txtpnTemp.setText("Temp:");
 		panel_2.add(txtpnTemp);
 		
 		JTextPane textPane_2 = new JTextPane();
 		textPane_2.setEditable(false);
 		textPane_2.setText("98.6");
 		panel_2.add(textPane_2);
 		
 		JRadioButton radioButton_2 = new JRadioButton("");
 		radioButton_2.setEnabled(false);
 		radioButton_2.setSelected(false);
 		panel_2.add(radioButton_2);
 		
 		JTextPane txtpnRespiratoryRate = new JTextPane();
 		txtpnRespiratoryRate.setEditable(false);
 		txtpnRespiratoryRate.setText("Respiratory rate:");
 		panel_2.add(txtpnRespiratoryRate);
 		
 		JTextPane textPane_3 = new JTextPane();
 		textPane_3.setEditable(false);
 		textPane_3.setText("30");
 		panel_2.add(textPane_3);
 		
 		JRadioButton radioButton_3 = new JRadioButton("");
 		radioButton_3.setEnabled(false);
 		radioButton_3.setSelected(false);
 		panel_2.add(radioButton_3);
 		
 		JTextPane txtpnAlarm = new JTextPane();
 		txtpnAlarm.setEditable(false);
 		txtpnAlarm.setText("Alarm:");
 		panel_2.add(txtpnAlarm);
 		
 		JPanel panel_AlarmCode = new JPanel();
 		panel_AlarmCode.setBackground(Color.GREEN);
 		panel_2.add(panel_AlarmCode);
 		
 		JButton btnResetAlarm = new JButton("Reset Alarm");
 		panel_2.add(btnResetAlarm);
 	}
 
 	public void createTrendWindow(){
 		System.out.println("Creating Trend Window");
 		winPatientTrend = new PatientTrend();
 	}
 	
 	public void addPatient(String name, String type){
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 		Date today = Calendar.getInstance().getTime();
 		String reportDate = df.format(today);
 		
 		Random rand = new Random();
 		Integer idInt = rand.nextInt(9999) + 1;
 		System.out.println(name + ", " + type  + ", " + idInt.toString() +", " + reportDate);
 		nurseStation.admitPatient(idInt.toString(), name, type);
 		
 	}
 	
 }
