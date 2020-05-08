 package gui;
 
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.util.Calendar;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import synclogic.ObjectRequest;
 
 import model.SaveableClass;
 import model.XmlSerializerX;
 
 public class GUI extends JFrame implements WindowListener{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	//protected static final String IMAGE_PATH = "gui/";
 	protected static final Color COLOR_BACKGROUND = Color.WHITE;
 	protected static final Color COLOR_APOINTMENT = new Color(247, 199, 173);
 	protected static final Color COLOR_NO_APOINTMENT = new Color(198, 243, 214);
 	//protected static final Color COLOR_IN_DELIVERY = new Color(206, 219, 239);
 	protected static final Font FONT_HEADER = new Font("", Font.PLAIN, 20);
 	protected static final Font FONT_FIELD = new Font("", Font.PLAIN, 18);
 	protected GridBagConstraints c;
 	JPanel panel = new JPanel();
 	
 	
 	private JButton logOutButton;
 	private JButton createButton;
 	private JLabel helpLabel;
 	private JLabel overskrift;
	private GUILoggInInfo notificationShower;
 	
 	private String [] options = {"Meeting","Appointment"};
 	
 	private JDialog progressWindow;
 	private JProgressBar progressBar;
 	private Thread progressThread;
 	
 	public GUI(){
 		
 //		this.setPreferredSize(new Dimension(800,600));
 		SmallCalendar smallCal = new SmallCalendar();
 		
 		CalendarPanecolumnHeaderPanel calPanel = new CalendarPanecolumnHeaderPanel();
 		
 //		getContentPane().setBackground(COLOR_BACKGROUND);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		c = new GridBagConstraints();
 		
 		setLayout(new GridBagLayout());
 		
 		c.gridx=0;
 		c.gridy=0;
 		c.anchor=GridBagConstraints.NORTHWEST;
		this.notificationShower = new GUILoggInInfo();
		add(this.notificationShower,c);
 		
 		overskrift = new JLabel("CAL - X");
 		c.gridx=7;
 		c.gridy=0;
 		add(overskrift,c);
 		
 		c.gridx=0;
 		c.gridy=5;
 		add(smallCal,c);
 		
 		c.fill=GridBagConstraints.HORIZONTAL;
 		c.gridx=9;
 		c.gridy=3;
 		add(calPanel,c);
 		
 		c.gridx=9;
 		c.gridy=5;
 		add(new ScrollPane(smallCal),c); //adder kalenderen i mainen
 		
 		helpLabel = new JLabel("");
 		c.gridx=9;
 		c.gridy=6;
 		add(helpLabel, c);
 		
 		createButton = new JButton("Create");
 		c.gridx=0;
 		c.gridy=7;
 		add(createButton,c);
 		
 		logOutButton = new JButton("Log Out");
 		c.gridx=30;
 		c.gridy=0;
 		add(logOutButton,c);
 		
 		JFrame nullFrame = null;
 		progressWindow = new JDialog(nullFrame, "Logging out...", true);
 		progressBar = new JProgressBar();
 		progressBar.setIndeterminate(true);
 		progressWindow.add(progressBar);
 		progressWindow.setSize(300, 75);
 		progressWindow.setLocationRelativeTo(null);
 //		progressWindow.setBounds(getWidth()/2-300/2, getHeight()/2-75/2, 300, 75);
 		progressWindow.setResizable(false);
 //		progressWindow.setVisible(true);
 		
 		this.addWindowListener(this);
 		addActionListeners();
 //		this.setResizable(false);
 		this.setMinimumSize(new Dimension(1421, 505));
 		pack();
 		setLocationRelativeTo(null);
 		
 //		XCal.getCSU().requestObject(new ObjectRequest(SaveableClass.Appointment, "5"));
 		
 	}
 	private void addActionListeners() {
 		createButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int response=JOptionPane.showOptionDialog(null, "Meeting or Appointment?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, "none of your business");
 				if(response==0){
 					JDialog mGUI = new MeetingGui();
 					mGUI.setVisible(true);
 				}
 				if(response==1){
 					Calendar now = Calendar.getInstance();
 					now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY)+1);
 					JDialog aGUI = new AppointmentGui(now);
 					aGUI.setVisible(true);
 					
 				}
 			}
 		});
 		logOutButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				progressThread = new Thread(new ProgressBar());
 				progressThread.start();
 				try {
 					XCal.getCSU().disconnect();
 				} catch (IOException e1) {
 				}
 				LogIn logIn = new LogIn();
 //				logIn.pack();
 				logIn.setVisible(true);
 				progressWindow.setVisible(false);
 				setVisible(false);
 			}
 		});
 	}
 	
 	class ProgressBar implements Runnable{
 
 		@Override
 		public void run() {
 			progressWindow.setVisible(true);
 			
 		}
 		
 	}
 	
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowClosing(WindowEvent e) {
 		// TODO Auto-generated method stub
 //		System.out.println("WindowClosing");
 //		XCal.getCSU().disconnect();
 		
 	}
 	@Override
 	public void windowClosed(WindowEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("WindowClosed");
 		try {
 			XCal.getCSU().disconnect();
 		} catch (IOException e1) {
 		}
 		System.exit(0);
 	}
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
