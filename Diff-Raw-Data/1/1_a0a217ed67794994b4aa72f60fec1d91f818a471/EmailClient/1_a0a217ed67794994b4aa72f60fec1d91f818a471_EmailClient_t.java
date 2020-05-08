 package com.tw.server;
 
 import java.awt.*;
 import java.util.*;
 import java.util.Timer;
 import java.awt.event.*;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.security.Security;
 import javax.mail.*;
 import javax.swing.*;
 import javax.mail.internet.*;
 import javax.activation.FileDataSource;
 import javax.swing.JMenuItem;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 public class EmailClient extends JFrame 
 {
 	//ProfileDialog pd = new ProfileDialog();
 	protected JMenuBar jJMenuBar = null;
 	protected JMenu jMenuFile = null;
 	protected JMenu jMenuHelp = null;
 	protected JMenuItem jMenuItemOption = null;
 	protected JMenuItem jMenuItemSetTime = null;
 	protected JMenuItem jMenuItemExit = null;
 	protected JTabbedPane jTabbedPane = null;
 	protected JPanel jPanel = null;
 	protected JPanel jPanel4 = null;
 	protected JPanel jPanel2 = null;
 	protected JLabel jLabelReceiver = null;
 	protected JLabel jLabelSubject = null;
 	protected static JTextField jTextFieldReceiver = null;
 	protected static JTextField jTextFieldSubject = null;
 	protected JPanel jPanel3 = null;
 	protected JPanel jPanel1 = null;
 	protected JScrollPane jScrollPane = null;
 	protected static JTextArea jTextArea = null;
 	protected JPanel jPanel5 = null;
 	protected JPanel jPanel6 = null;
 	protected JToolBar jJToolBarBar = null;
 	protected JPanel jPanel7 = null;
 	protected JButton jButtonSend = null;
 	protected JMenuItem jMenuItemAbout = null;
 	protected JButton jButtonClear = null;
 	private JLabel jLabelSender = null;
 	protected static JTextField jTextFieldSender = null;
 	private JButton jButtonSubex = null;
 	private JPanel jPanel8 = null;
 	private JPanel jPanel21 = null;
 	private JLabel jLabelDate = null;
 	private JLabel jLabelSubject2 = null;
 	static JTextField jTextFieldReceiveDate = null;
 	static JTextField jTextFieldReceiveSubject = null;
 	private JLabel jLabelSender2 = null;
 	protected static JTextField jTextFieldReceiveSender = null;
 	private JPanel jPanel31 = null;
 	private JPanel jPanel11 = null;
 	private JScrollPane jScrollPane1 = null;
 	protected static JTextArea jTextArea1 = null;
 	private JButton jButtonReceive = null;
 	private JButton jButtonPlayFlash = null;
 	private JMenu jMenuTools = null;
 	private JMenuItem jMenuItemoOption = null;
 	private JDialog jDialogOption = null;
 	private JPanel jContentPane = null;
 	private JTabbedPane jTabbedPane1 = null;
 	private JPanel jPanel9 = null;
 	private JTextField jTextField = null;
 	private JTextField jTextField1 = null;
 	private JTextField jTextField2 = null;
 	private JTextField jTextField3 = null;
 	private JTextField jTextField4 = null;
 	private JTextField jTextField5 = null;
 	private JTextField jTextField6 = null;
 	private JDialog jDialogAbout = null;
 	private JPanel jContentPane1 = null;
 	private JLabel jLabelName1 = null;
 	private JLabel jLabelName2 = null;
 	private JLabel jLabelAddress1 = null;
 	private JLabel jLabelAddress2 = null;
 	protected static JButton jButtonArrowLeft = null;
 	protected static JButton jButtonArrowRight = null;
 	protected static JLabel jLabelMailTotal = null;
 	protected static JLabel jLabelMailStatus = null;
 	private JButton jButtonAddFile = null;
 	protected static JLabel jLabelStatus = null;
 	private JButton jButtonDelete = null;
 	private JMenuItem jMenuItemCalculator = null;
 	private JMenuItem jMenuCalendar = null;
 
 	private Timer timer;
 	private int active;
 	
 	public EmailClient() 
 	{
 		super();
 		initialize();
 	}
 
 	public void initialize() 
 	{
 		this.setSize(508, 558);
 		this.setContentPane(getJPanel5());
 		this.setJMenuBar(getJJMenuBar());
 		this.setTitle("E-mail Client v1.2");
 		this.setResizable(false);
 		
 		timer = new Timer();
 		active = 0;
 		try
 		{
 		ClassLoader cl = this.getClass().getClassLoader();
 		ImageIcon image1 = new ImageIcon(cl.getResource("images/Email.png"));
 		setIconImage(image1.getImage());
 		}
 		catch(Exception x)
 		{
 			
 		}
 		
 		setAutoRecievemail();
 	}
 	
 	//setTimer
 	public void setAutoRecievemail()
 	{
         String default_time = null;
        
 		try {
 			Properties p = new Properties();
 			FileInputStream in;
 			in = new FileInputStream("profile.properties");
 			p.load(in);
 			
 			default_time = p.getProperty("recievetimer");
 			in.close();
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		int idefault_time = Integer.parseInt(default_time) ;
 		
 		closeAutoRecievemail();
 		if (idefault_time != 0)
 		{
 	        //Receive.ReceiveMail()
 	        timer.schedule(new timeouthandler(), idefault_time*60*1000, idefault_time*60*1000);
 	        active = 1;
 		}
 	}
 	
 	//closeTimer
 	public void closeAutoRecievemail()
 	{
 		if (active == 1)
 			timer.cancel();
			timer = new Timer();
 			active = 0;
 	}
 	
 	public JMenuBar getJJMenuBar() {
 		if (jJMenuBar == null) {
 			jJMenuBar = new JMenuBar();
 			jJMenuBar.add(getJMenuFile());
 			jJMenuBar.add(getJMenuTools());
 			jJMenuBar.add(getJMenuHelp());
 		}
 		return jJMenuBar;
 	}
 
 	public JMenu getJMenuFile() {
 		if (jMenuFile == null) {
 			jMenuFile = new JMenu();
 			jMenuFile.add(getJMenuItemOption());
 			jMenuFile.add(getJMenuItemSetTime());
 			jMenuFile.add(getJMenuItemExit());
 			jMenuFile.setText("ɮ(F)");
 			jMenuFile.setMnemonic(java.awt.event.KeyEvent.VK_F);
 		}
 		return jMenuFile;
 	}
 
 	public JMenu getJMenuHelp() {
 		if (jMenuHelp == null) {
 			jMenuHelp = new JMenu();
 			jMenuHelp.add(getJMenuItemAbout());
 			jMenuHelp.setText("(H)");
 			jMenuHelp.setMnemonic(java.awt.event.KeyEvent.VK_H);
 		}
 		return jMenuHelp;
 	}
 
 	public JMenuItem getJMenuItemOption() {
 		if (jMenuItemOption == null) {
 			jMenuItemOption = new JMenuItem();
 			jMenuItemOption.setText("tγ]w");
 			jMenuItemOption
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							ProfileDialog pd = new ProfileDialog();
 							Dimension dlgSize = pd.getPreferredSize();
 							Dimension frmSize = getSize();
 							Point loc = getLocation();
 							pd.setLocation((frmSize.width - dlgSize.width) / 2
 									+ loc.x, (frmSize.height - dlgSize.height)
 									/ 2 + loc.y);
 							pd.show();
 						}
 					});
 		}
 		return jMenuItemOption;
 	}
 
 	public JMenuItem getJMenuItemSetTime() {
 		if (jMenuItemSetTime == null) {
 			jMenuItemSetTime = new JMenuItem();
 			jMenuItemSetTime.setText("۰ʦH");
 			jMenuItemSetTime
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							SetTimeDialog pd = new SetTimeDialog();
 							Dimension dlgSize = pd.getPreferredSize();
 							Dimension frmSize = getSize();
 							Point loc = getLocation();
 							pd.setLocation((frmSize.width - dlgSize.width) / 2
 									+ loc.x, (frmSize.height - dlgSize.height)
 									/ 2 + loc.y);
 							pd.show();
 						}
 					});
 		}
 		return jMenuItemSetTime;
 	}	
 	
 	public JMenuItem getJMenuItemExit() {
 		if (jMenuItemExit == null) {
 			jMenuItemExit = new JMenuItem();
 			jMenuItemExit.setText("");
 			jMenuItemExit.setMnemonic(java.awt.event.KeyEvent.VK_X);
 			jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 					java.awt.event.KeyEvent.VK_X, java.awt.Event.ALT_MASK,
 					false));
 			jMenuItemExit
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							System.exit(0);
 						}
 					});
 		}
 		return jMenuItemExit;
 	}
 
 	public JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.addTab("gsH", getJPanel());
 			jTabbedPane.addTab("X", getJPanel8());
 			jTabbedPane.setBounds(new Rectangle(1, 45, 501, 442));
 		}
 		return jTabbedPane;
 	}
 
 	public JPanel getJPanel() {
 		if (jPanel == null) {
 			jPanel = new JPanel();
 			jPanel.setLayout(null);
 			jPanel.add(getJPanel4(), null);
 		}
 		return jPanel;
 	}
 
 	public JPanel getJPanel4() {
 		if (jPanel4 == null) {
 			jPanel4 = new JPanel();
 			jPanel4.setLayout(null);
 			jPanel4.setBounds(new Rectangle(14, 2, 467, 405));
 			jPanel4.add(getJPanel2(), null);
 			jPanel4.add(getJPanel3(), null);
 		}
 		return jPanel4;
 	}
 
 	public JPanel getJPanel2() {
 		if (jPanel2 == null) {
 			jLabelSender = new JLabel();
 			jLabelSender.setText("H:");
 			jLabelSender.setBounds(new Rectangle(0, 4, 45, 27));
 			jLabelSubject = new JLabel();
 			jLabelSubject.setText("D:");
 			jLabelSubject.setBounds(new Rectangle(0, 58, 45, 28));
 			jLabelReceiver = new JLabel();
 			jLabelReceiver.setText(":");
 			jLabelReceiver.setBounds(new Rectangle(0, 31, 45, 27));
 			jPanel2 = new JPanel();
 			jPanel2.setLayout(null);
 			jPanel2.setBounds(new Rectangle(0, 0, 474, 91));
 			jPanel2.setName("jPanel2");
 			jPanel2.add(jLabelReceiver, null);
 			jPanel2.add(jLabelSubject, null);
 			jPanel2.add(getJTextFieldReceiver(), null);
 			jPanel2.add(getJTextFieldSubject(), null);
 			jPanel2.add(jLabelSender, null);
 			jPanel2.add(getJTextFieldSender(), null);
 			jPanel2.add(getJButtonSubex(), null);
 			jPanel2.add(getJButtonAddFile(), null);
 			jPanel2.add(getJButtonDelete(), null);
 		}
 		return jPanel2;
 	}
 
 	public JTextField getJTextFieldReceiver() {
 		if (jTextFieldReceiver == null) {
 			jTextFieldReceiver = new JTextField();
 			jTextFieldReceiver.setBounds(new Rectangle(50, 31, 416, 22));
 		}
 		return jTextFieldReceiver;
 	}
 
 	public JTextField getJTextFieldSubject() {
 		if (jTextFieldSubject == null) {
 			jTextFieldSubject = new JTextField();
 			jTextFieldSubject.setBounds(new Rectangle(50, 61, 295, 22));
 		}
 		return jTextFieldSubject;
 	}
 
 	public JPanel getJPanel3() {
 		if (jPanel3 == null) {
 			jPanel3 = new JPanel();
 			jPanel3.setLayout(new BoxLayout(getJPanel3(), BoxLayout.X_AXIS));
 			jPanel3.setBounds(new Rectangle(0, 91, 467, 313));
 			jPanel3.setName("jPanel3");
 			jPanel3.add(getJPanel1(), null);
 		}
 		return jPanel3;
 	}
 
 	public JPanel getJPanel1() {
 		if (jPanel1 == null) {
 			GridBagConstraints gridBagConstraints = new GridBagConstraints();
 			gridBagConstraints.fill = GridBagConstraints.BOTH;
 			gridBagConstraints.gridx = 0;
 			gridBagConstraints.gridy = 0;
 			gridBagConstraints.weightx = 1.0;
 			gridBagConstraints.weighty = 1.0;
 			gridBagConstraints.gridwidth = 2;
 			jPanel1 = new JPanel();
 			jPanel1.setLayout(new GridBagLayout());
 			jPanel1.add(getJScrollPane(), gridBagConstraints);
 		}
 		return jPanel1;
 	}
 
 	public JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setViewportView(getJTextArea());
 		}
 		return jScrollPane;
 	}
 
 	public JTextArea getJTextArea() {
 		if (jTextArea == null) {
 			jTextArea = new JTextArea(); 
 		}
 		return jTextArea;
 	}
 
 	public JPanel getJPanel5() {
 		if (jPanel5 == null) {
 			jLabelStatus = new JLabel();
 			jLabelStatus.setBounds(new Rectangle(1, 487, 501, 16));
 			jLabelStatus.setText("");
 			jPanel5 = new JPanel();
 			jPanel5.setLayout(null);
 			jPanel5.add(getJPanel6(), null);
 			jPanel5.add(getJTabbedPane(), null);
 			jPanel5.add(jLabelStatus, null);
 		}
 		return jPanel5;
 	}
 
 	public JPanel getJPanel6() {
 		if (jPanel6 == null) {
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.setRows(1);
 			gridLayout.setVgap(0);
 			gridLayout.setHgap(0);
 			jPanel6 = new JPanel();
 			jPanel6.setBounds(new Rectangle(0, 0, 501, 37));
 			jPanel6.setLayout(gridLayout);
 			jPanel6.add(getJJToolBarBar(), null);
 		}
 		return jPanel6;
 	}
 
 	public JToolBar getJJToolBarBar() {
 		if (jJToolBarBar == null) {
 			jJToolBarBar = new JToolBar();
 			jJToolBarBar.add(getJPanel7());
 		}
 		return jJToolBarBar;
 	}
 
 	public JPanel getJPanel7() {
 		if (jPanel7 == null) {
 			jLabelMailStatus = new JLabel();
 			jLabelMailStatus.setBounds(new Rectangle(350, 19, 120, 15));
 			jLabelMailTotal = new JLabel();
 			jLabelMailTotal.setBounds(new Rectangle(350, 1, 120, 15));
 			jPanel7 = new JPanel();
 			jPanel7.setLayout(null);
 			//jPanel7.add(getJButtonPlayFlash(), null);
 			jPanel7.add(getJButtonReceive(), null);
 			jPanel7.add(getJButtonSend(), null);
 			jPanel7.add(getJButtonClear(), null);
 			jPanel7.add(getJButtonArrowLeft(), null);
 			jPanel7.add(getJButtonArrowRight(), null);
 			jPanel7.add(jLabelMailTotal, null);
 			jPanel7.add(jLabelMailStatus, null);
 		}
 		return jPanel7;
 	}
 
 	public JButton getJButtonSend() {
 		if (jButtonSend == null) {
 			jButtonSend = new JButton();
 			jButtonSend.setIcon(new ImageIcon("images/Send.png"));
 			jButtonSend.setToolTipText("ǰeH(S)");
 			jButtonSend.setBounds(new Rectangle(1, 0, 58, 33));
 			jButtonSend.setMnemonic(java.awt.event.KeyEvent.VK_S);
 			jButtonSend.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent ae) {
 					try {
 						if ((jTextFieldSender.getText()).equals("")) {
 							JOptionPane.showMessageDialog((Component) null,
 									"пJH̹qll}I", "ĵi",
 									JOptionPane.WARNING_MESSAGE);
 						} else if ((jTextFieldReceiver.getText()).equals("")) {
 							JOptionPane.showMessageDialog((Component) null,
 									"пJ̹qll}I", "ĵi",
 									JOptionPane.WARNING_MESSAGE);
 						} else if (checkMail(jTextFieldSender.getText())==false|
 							checkMail(jTextFieldReceiver.getText())==false) {
 							JOptionPane.showMessageDialog((Component) null,
 									"E-mail榡TAЭsJI", "ĵi",
 									JOptionPane.WARNING_MESSAGE);
 						} else {
 							Send.sendmail();
 						}
 					} catch (AddressException ade) {
 						JOptionPane.showMessageDialog((Component) null,
 							"lLkǰeI", "ĵi", JOptionPane.WARNING_MESSAGE);
 						ade.printStackTrace();
 					} catch (MessagingException me) {
 						me.printStackTrace();
 					}					
 				}
 			});
 		}
 		return jButtonSend;
 	}
 	
 	protected boolean checkMail(String mail) {
 		if(mail.matches("[a-zA-Z0-9_.]+@+[a-zA-Z0-9]+.+[a-zA-Z0-9]?+.+[a-zA-Z0-9]")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public JMenuItem getJMenuItemAbout() {
 		if (jMenuItemAbout == null) {
 			jMenuItemAbout = new JMenuItem();
 			jMenuItemAbout.setText("");
 			jMenuItemAbout.setMnemonic(java.awt.event.KeyEvent.VK_A);
 			jMenuItemAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 					java.awt.event.KeyEvent.VK_A, java.awt.Event.ALT_MASK,
 					false));
 			jMenuItemAbout
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							getJDialogAbout();
 							Dimension dlgSize = getJDialogAbout()
 									.getPreferredSize();
 							Dimension frmSize = getSize();
 							Point loc = getLocation();
 							getJDialogAbout()
 									.setLocation(
 											(frmSize.width - dlgSize.width) / 2
 													+ loc.x,
 											(frmSize.height - dlgSize.height)
 													/ 2 + loc.y);
 							jDialogAbout.show();
 						}
 					});
 		}
 		return jMenuItemAbout;
 	}
 
 	public JButton getJButtonClear() {
 		if (jButtonClear == null) {
 			jButtonClear = new JButton();
 			jButtonClear.setIcon(new ImageIcon("images/Broom.png"));
 			jButtonClear.setToolTipText("Mg");
 			jButtonClear.setBounds(new Rectangle(140, 0, 58, 33));
 			jButtonClear.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					Send.Clear();
 				}
 			});
 		}
 		return jButtonClear;
 	}
 
 	private JTextField getJTextFieldSender() {
 		if (jTextFieldSender == null) {
 			ProfileDialog pd = new ProfileDialog();
 			jTextFieldSender = new JTextField();
 			jTextFieldSender.setEnabled(false);		
 			jTextFieldSender.setText(pd.jTextFieldUser.getText()+"@gmail.com");
 			jTextFieldSender.setBounds(new Rectangle(50, 4, 416, 22));
 		}
 		return jTextFieldSender;
 	}
 
 	private JButton getJButtonSubex() {
 		if (jButtonSubex == null) {
 			jButtonSubex = new JButton();
 			jButtonSubex.setIcon(new ImageIcon("images/Favorites.png"));
 			jButtonSubex.setToolTipText("`ΥD");
 			jButtonSubex.setBounds(new Rectangle(430, 54, 35, 35));
 			jButtonSubex.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					SubjectExample se = new SubjectExample();
 					Dimension dlgSize = se.getPreferredSize();
 					Dimension frmSize = getSize();
 					Point loc = getLocation();
 					se.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
 							(frmSize.height - dlgSize.height) / 2 + loc.y);
 					se.show();
 				}
 			});
 		}
 		return jButtonSubex;
 	}
 
 	private JPanel getJPanel8() {
 		if (jPanel8 == null) {
 			jPanel8 = new JPanel();
 			jPanel8.setLayout(null);
 			jPanel8.add(getJPanel21(), null);
 			jPanel8.add(getJPanel31(), null);
 		}
 		return jPanel8;
 	}
 
 	private JPanel getJPanel21() {
 		if (jPanel21 == null) {
 			jLabelSender2 = new JLabel();
 			jLabelSender2.setBounds(new Rectangle(0, 4, 45, 27));
 			jLabelSender2.setText("\u5bc4\u4ef6\u8005:");
 			jLabelSubject2 = new JLabel();
 			jLabelSubject2.setBounds(new Rectangle(0, 58, 45, 28));
 			jLabelSubject2.setText("D:");
 			jLabelDate = new JLabel();
 			jLabelDate.setBounds(new Rectangle(0, 31, 45, 27));
 			jLabelDate.setText(":");
 			jPanel21 = new JPanel();
 			jPanel21.setLayout(null);
 			jPanel21.setName("jPanel2");
 			jPanel21.setBounds(new Rectangle(14, 2, 467, 91));
 			jPanel21.add(jLabelDate, null);
 			jPanel21.add(jLabelSubject2, null);
 			jPanel21.add(getJTextFieldReceiveDate(), null);
 			jPanel21.add(getJTextFieldReceiveSubject(), null);
 			jPanel21.add(jLabelSender2, null);
 			jPanel21.add(getJTextFieldReceiveSender(), null);
 		}
 		return jPanel21;
 	}
 
 	private JTextField getJTextFieldReceiveDate() {
 		if (jTextFieldReceiveDate == null) {
 			jTextFieldReceiveDate = new JTextField();
 			jTextFieldReceiveDate.setBounds(new Rectangle(50, 31, 416, 22));
 		}
 		return jTextFieldReceiveDate;
 	}
 
 	private JTextField getJTextFieldReceiveSubject() {
 		if (jTextFieldReceiveSubject == null) {
 			jTextFieldReceiveSubject = new JTextField();
 			jTextFieldReceiveSubject.setBounds(new Rectangle(50, 61, 416, 22));
 		}
 		return jTextFieldReceiveSubject;
 	}
 
 	private JTextField getJTextFieldReceiveSender() {
 		if (jTextFieldReceiveSender == null) {
 			jTextFieldReceiveSender = new JTextField();
 			jTextFieldReceiveSender.setBounds(new Rectangle(50, 4, 416, 22));
 		}
 		return jTextFieldReceiveSender;
 	}
 
 	private JPanel getJPanel31() {
 		if (jPanel31 == null) {
 			jPanel31 = new JPanel();
 			jPanel31.setLayout(new BoxLayout(getJPanel31(), BoxLayout.X_AXIS));
 			jPanel31.setBounds(new Rectangle(14, 93, 467, 313));
 			jPanel31.setName("jPanel3");
 			jPanel31.add(getJPanel11(), null);
 		}
 		return jPanel31;
 	}
 
 	private JPanel getJPanel11() {
 		if (jPanel11 == null) {
 			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 			gridBagConstraints1.fill = GridBagConstraints.BOTH;
 			gridBagConstraints1.gridx = 0;
 			gridBagConstraints1.gridy = 0;
 			gridBagConstraints1.weightx = 1.0;
 			gridBagConstraints1.weighty = 1.0;
 			gridBagConstraints1.gridwidth = 2;
 			jPanel11 = new JPanel();
 			jPanel11.setLayout(new GridBagLayout());
 			jPanel11.add(getJScrollPane1(), gridBagConstraints1);
 		}
 		return jPanel11;
 	}
 
 	private JScrollPane getJScrollPane1() {
 		if (jScrollPane1 == null) {
 			jScrollPane1 = new JScrollPane();
 			jScrollPane1.setViewportView(getJTextArea1());
 		}
 		return jScrollPane1;
 	}
 
 	private JTextArea getJTextArea1() {
 		if (jTextArea1 == null) {
 			jTextArea1 = new JTextArea();
 		}
 		return jTextArea1;
 	}
 	
 	private JButton getJButtonPlayFlash()
 	{
 		if (jButtonPlayFlash == null) {
 			jButtonPlayFlash = new JButton();
 			jButtonPlayFlash.setBounds(new Rectangle(350, 0, 58, 33));
 			jButtonPlayFlash.setMnemonic(KeyEvent.VK_R);
 			jButtonPlayFlash.setIcon(new ImageIcon("images/Receive.png"));
 			jButtonPlayFlash.setToolTipText("PlayFlash(R)");
 			jButtonPlayFlash
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							try {
 							      Display display = new Display();
 							      Shell shell = new PlayFlash().open (display);
 							      int i=0;
 							      while (!shell.isDisposed()) 
 							      {
 							    	  if (i>400) break;
 							         if (!display.readAndDispatch()) display.sleep ();
 							         i++;
 							      }
 							      display.dispose();
 								
 							} catch (Exception ec) {
 							}
 						}
 					});
 			jButtonPlayFlash.setMnemonic(java.awt.event.KeyEvent.VK_R);
 		}
 		return jButtonPlayFlash;
 		
 	}
 
 	private JButton getJButtonReceive() {
 		if (jButtonReceive == null) {
 			jButtonReceive = new JButton();
 			jButtonReceive.setBounds(new Rectangle(70, 0, 58, 33));
 			jButtonReceive.setMnemonic(KeyEvent.VK_R);
 			jButtonReceive.setIcon(new ImageIcon("images/Receive.png"));
 			jButtonReceive.setToolTipText("ˬdsH(R)");
 			jButtonReceive
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							try {
 								//EmailClient.jLabelStatus.setText("l󱵦...еy");
 								Receive.ReceiveMail();
 							} catch (Exception ec) {
 							}
 						}
 					});
 			jButtonReceive.setMnemonic(java.awt.event.KeyEvent.VK_R);
 		}
 		return jButtonReceive;
 	}
 
 	private JMenu getJMenuTools() {
 		if (jMenuTools == null) {
 			jMenuTools = new JMenu();
 			jMenuTools.setText("u(T)");
 			jMenuTools.setMnemonic(java.awt.event.KeyEvent.VK_T);
 			//jMenuTools.add(getJMenuItemoOption());
 			jMenuTools.add(getJMenuItemCalculator());
 			jMenuTools.add(getJMenuCalendar());
 		}
 		return jMenuTools;
 	}
 
 	/*private JMenuItem getJMenuItemoOption() {
 		if (jMenuItemoOption == null) {
 			jMenuItemoOption = new JMenuItem();
 			jMenuItemoOption.setText("ﶵ(O)");
 			jMenuItemoOption.setMnemonic(java.awt.event.KeyEvent.VK_O);
 			jMenuItemoOption.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 					java.awt.event.KeyEvent.VK_O, java.awt.Event.ALT_MASK,
 					false));
 			jMenuItemoOption.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							getJDialogOption();
 							Dimension dlgSize = getJDialogOption()
 									.getPreferredSize();
 							Dimension frmSize = getSize();
 							Point loc = getLocation();
 							getJDialogOption()
 									.setLocation(
 											(frmSize.width - dlgSize.width) / 2
 													+ loc.x,
 											(frmSize.height - dlgSize.height)
 													/ 2 + loc.y);
 							jDialogOption.show();
 						}
 			});
 		}
 		return jMenuItemoOption;
 	}*/
 
 	private JDialog getJDialogOption() {
 		if (jDialogOption == null) {
 			jDialogOption = new JDialog(this);
 			jDialogOption.setSize(new Dimension(327, 227));
 			jDialogOption.setTitle("ﶵ");
 			jDialogOption.setContentPane(getJContentPane());
 		}
 		return jDialogOption;
 	}
 
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 			gridBagConstraints2.fill = GridBagConstraints.BOTH;
 			gridBagConstraints2.gridy = 0;
 			gridBagConstraints2.weightx = 1.0;
 			gridBagConstraints2.weighty = 1.0;
 			gridBagConstraints2.gridx = 0;
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new GridBagLayout());
 			jContentPane.add(getJTabbedPane1(), gridBagConstraints2);
 		}
 		return jContentPane;
 	}
 
 	private JTabbedPane getJTabbedPane1() {
 		if (jTabbedPane1 == null) {
 			jTabbedPane1 = new JTabbedPane();
 			jTabbedPane1.addTab("r", getJPanel9());
 		}
 		return jTabbedPane1;
 	}
 
 	private JPanel getJPanel9() {
 		if (jPanel9 == null) {
 			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
 			gridBagConstraints9.fill = GridBagConstraints.BOTH;
 			gridBagConstraints9.gridy = 6;
 			gridBagConstraints9.weightx = 1.0;
 			gridBagConstraints9.gridx = 0;
 			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
 			gridBagConstraints8.fill = GridBagConstraints.BOTH;
 			gridBagConstraints8.gridy = 5;
 			gridBagConstraints8.weightx = 1.0;
 			gridBagConstraints8.gridx = 0;
 			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
 			gridBagConstraints7.fill = GridBagConstraints.BOTH;
 			gridBagConstraints7.gridy = 4;
 			gridBagConstraints7.weightx = 1.0;
 			gridBagConstraints7.gridx = 0;
 			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
 			gridBagConstraints6.fill = GridBagConstraints.BOTH;
 			gridBagConstraints6.gridy = 3;
 			gridBagConstraints6.weightx = 1.0;
 			gridBagConstraints6.gridx = 0;
 			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
 			gridBagConstraints5.fill = GridBagConstraints.BOTH;
 			gridBagConstraints5.gridy = 2;
 			gridBagConstraints5.weightx = 1.0;
 			gridBagConstraints5.gridx = 0;
 			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
 			gridBagConstraints4.fill = GridBagConstraints.BOTH;
 			gridBagConstraints4.gridy = 1;
 			gridBagConstraints4.weightx = 1.0;
 			gridBagConstraints4.gridx = 0;
 			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 			gridBagConstraints3.fill = GridBagConstraints.BOTH;
 			gridBagConstraints3.gridy = 0;
 			gridBagConstraints3.weightx = 1.0;
 			gridBagConstraints3.gridx = 0;
 			jPanel9 = new JPanel();
 			jPanel9.setLayout(new GridBagLayout());
 			jPanel9.add(getJTextField(), gridBagConstraints3);
 			jPanel9.add(getJTextField1(), gridBagConstraints4);
 			jPanel9.add(getJTextField2(), gridBagConstraints5);
 			jPanel9.add(getJTextField3(), gridBagConstraints6);
 			jPanel9.add(getJTextField4(), gridBagConstraints7);
 			jPanel9.add(getJTextField5(), gridBagConstraints8);
 			jPanel9.add(getJTextField6(), gridBagConstraints9);
 		}
 		return jPanel9;
 	}
 
 	private JTextField getJTextField() {
 		if (jTextField == null) {
 			jTextField = new JTextField();
 		}
 		return jTextField;
 	}
 
 	private JTextField getJTextField1() {
 		if (jTextField1 == null) {
 			jTextField1 = new JTextField();
 		}
 		return jTextField1;
 	}
 
 	private JTextField getJTextField2() {
 		if (jTextField2 == null) {
 			jTextField2 = new JTextField();
 		}
 		return jTextField2;
 	}
 
 	private JTextField getJTextField3() {
 		if (jTextField3 == null) {
 			jTextField3 = new JTextField();
 		}
 		return jTextField3;
 	}
 
 	private JTextField getJTextField4() {
 		if (jTextField4 == null) {
 			jTextField4 = new JTextField();
 		}
 		return jTextField4;
 	}
 
 	private JTextField getJTextField5() {
 		if (jTextField5 == null) {
 			jTextField5 = new JTextField();
 		}
 		return jTextField5;
 	}
 
 	private JTextField getJTextField6() {
 		if (jTextField6 == null) {
 			jTextField6 = new JTextField();
 		}
 		return jTextField6;
 	}
 
 	private JDialog getJDialogAbout() {
 		if (jDialogAbout == null) {
 			jDialogAbout = new JDialog(this);
 			jDialogAbout.setTitle(" EmailCLient");
 			jDialogAbout.setSize(new Dimension(236, 141));
 			jDialogAbout.setContentPane(getJContentPane1());
 		}
 		return jDialogAbout;
 	}
 
 	private JPanel getJContentPane1() {
 		if (jContentPane1 == null) {
 			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
 			gridBagConstraints13.gridx = 1;
 			gridBagConstraints13.anchor = GridBagConstraints.WEST;
 			gridBagConstraints13.insets = new Insets(0, 5, 0, 0);
 			gridBagConstraints13.gridy = 1;
 			jLabelAddress2 = new JLabel();
 			jLabelAddress2.setText("oo8528182@gmail.com");
 			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
 			gridBagConstraints12.gridx = 1;
 			gridBagConstraints12.insets = new Insets(0, 5, 0, 0);
 			gridBagConstraints12.gridy = 0;
 			jLabelAddress1 = new JLabel();
 			jLabelAddress1.setText("bruce00486@gmail.com");
 			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
 			gridBagConstraints11.gridx = 0;
 			gridBagConstraints11.gridy = 1;
 			jLabelName2 = new JLabel();
 			jLabelName2.setText("Lfv");
 			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
 			gridBagConstraints10.gridx = 0;
 			gridBagConstraints10.gridy = 0;
 			jLabelName1 = new JLabel();
 			jLabelName1.setText("ݾ");
 			jContentPane1 = new JPanel();
 			jContentPane1.setLayout(new GridBagLayout());
 			jContentPane1.add(jLabelName1, gridBagConstraints10);
 			jContentPane1.add(jLabelName2, gridBagConstraints11);
 			jContentPane1.add(jLabelAddress1, gridBagConstraints12);
 			jContentPane1.add(jLabelAddress2, gridBagConstraints13);
 		}
 		return jContentPane1;
 	}
 
 	private JButton getJButtonArrowLeft() {
 		if (jButtonArrowLeft == null) {
 			jButtonArrowLeft = new JButton();
 			jButtonArrowLeft.setIcon(new ImageIcon("images/Arrow_Left.png"));
 			jButtonArrowLeft.setToolTipText("W@(P)");
 			jButtonArrowLeft.setMnemonic(java.awt.event.KeyEvent.VK_P);
 			jButtonArrowLeft.setEnabled(false);
 			jButtonArrowLeft.setBounds(new Rectangle(210, 0, 58, 33));
 			jButtonArrowLeft.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 						Receive.mailPreview();
 				}
 			});
 		}
 		return jButtonArrowLeft;
 	}
 
 	private JButton getJButtonArrowRight() {
 		if (jButtonArrowRight == null) {
 			jButtonArrowRight = new JButton();
 			jButtonArrowRight.setIcon(new ImageIcon("images/Arrow_Right.png"));
 			jButtonArrowRight.setToolTipText("U@(N)");
 			jButtonArrowRight.setMnemonic(java.awt.event.KeyEvent.VK_N);
 			jButtonArrowRight.setEnabled(false);
 			jButtonArrowRight.setBounds(new Rectangle(280, 0, 58, 33));
 			jButtonArrowRight.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					Receive.mailNext();
 				}
 			});
 		}
 		return jButtonArrowRight;
 	}
 
 	private JButton getJButtonAddFile() {
 		if (jButtonAddFile == null) {
 			jButtonAddFile = new JButton();
 			jButtonAddFile.setIcon(new ImageIcon("images/PaperClip.png"));
 			jButtonAddFile.setToolTipText("[ɮ");
 			jButtonAddFile.setBounds(new Rectangle(350, 54, 35, 35));
 			jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					JFileChooser file = new JFileChooser();
 					int result = file.showOpenDialog(new JPanel());
 					if (result == file.APPROVE_OPTION) {
 						String fileName = "";
 						String dir = "";
 						fileName = file.getSelectedFile().getName();
 						dir = file.getCurrentDirectory().toString();
 						int confirmAcceptReject;
 						
 						confirmAcceptReject = JOptionPane.showConfirmDialog(null, dir + fileName, 
 						"ܪ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 						if(confirmAcceptReject == JOptionPane.YES_OPTION){
 							Send.fds = new FileDataSource(dir + "\\" + fileName);
 							jLabelStatus.setText("[ɮ׬G" + dir + fileName);
 							jButtonDelete.setEnabled(true);
 						} else {
 							fileName = "";
 							dir = "";
 							jLabelStatus.setText("");
 						}
 					}
 				}
 			});
 		}
 		return jButtonAddFile;
 	}
 
 	private JButton getJButtonDelete() {
 		if (jButtonDelete == null) {
 			jButtonDelete = new JButton();
 			jButtonDelete.setEnabled(false);
 			jButtonDelete.setIcon(new ImageIcon("images/Delete.png"));
 			jButtonDelete.setBounds(new Rectangle(390, 54, 35, 35));
 			jButtonDelete.setToolTipText("");
 			jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if(Send.fds != null) {
 						Send.fds = null;
 						jLabelStatus.setText("w[ɮסI");
 						jButtonDelete.setEnabled(false);
 					}
 				}
 			});
 		}
 		return jButtonDelete;
 	}
 
 	/**
 	 * This method initializes jMenuItemCalculator	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getJMenuItemCalculator() {
 		if (jMenuItemCalculator == null) {
 			jMenuItemCalculator = new JMenuItem();
 			jMenuItemCalculator.setText("p(C)");
 			jMenuItemCalculator.setMnemonic(java.awt.event.KeyEvent.VK_C);
 			jMenuItemCalculator.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 					java.awt.event.KeyEvent.VK_C, java.awt.Event.ALT_MASK,
 					false));
 			jMenuItemCalculator.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 						Calculator cal = new Calculator();
 						Dimension dlgSize = cal.getPreferredSize();
 						Dimension frmSize = getSize();
 						Point loc = getLocation();
 						cal.setLocation((frmSize.width - dlgSize.width) / 2
 								+ loc.x, (frmSize.height - dlgSize.height)
 								/ 2 + loc.y);
 						cal.show();
 				}
 			});
 		}
 		return jMenuItemCalculator;
 	}
 
 	/**
 	 * This method initializes jMenuCalendar	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getJMenuCalendar() {
 		if (jMenuCalendar == null) {
 			jMenuCalendar = new JMenuItem();
 			jMenuCalendar.setText("ƾ");
 			jMenuCalendar.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 						MyCalendar ca = new MyCalendar();
 						Dimension dlgSize = ca.getPreferredSize();
 						Dimension frmSize = getSize();
 						Point loc = getLocation();
 						ca.setLocation((frmSize.width - dlgSize.width) / 2
 								+ loc.x, (frmSize.height - dlgSize.height)
 								/ 2 + loc.y);
 						ca.show();
 				}
 			});
 		}
 		return jMenuCalendar;
 	}
 	
 	public class timeouthandler extends TimerTask
 	{
 		//run routine
 		public void run() 
 		{
 			try {
 				EmailClient.jLabelStatus.setText("");
 				Receive.ReceiveMail();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }	
 	}
 
 	
 	
 	public static void main(String[] args) 
 	{
 		EmailClient app = new EmailClient(); // إSwingε{
 		
 		// ƥ, {
 		app.addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowClosing(java.awt.event.WindowEvent evt) {
 				System.exit(0);
 			}
 		});
 		
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension frameSize = app.getSize();
 		if (frameSize.height > screenSize.height) {
 			frameSize.height = screenSize.height;
 		}
 		if (frameSize.width > screenSize.width) {
 			frameSize.width = screenSize.width;
 		}
 		app.setLocation((screenSize.width - frameSize.width) / 2,
 				(screenSize.height - frameSize.height) / 2);
 		app.setVisible(true); // ܵ	
 	}
 	
 }
 
