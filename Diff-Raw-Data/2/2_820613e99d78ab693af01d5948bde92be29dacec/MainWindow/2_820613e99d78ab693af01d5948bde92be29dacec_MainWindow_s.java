 package org.coronastreet.gpxconverter;
 
 import java.awt.EventQueue;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextArea;
 import java.io.File;
 
 import javax.swing.JScrollPane;
 
 import java.awt.Font;
 import javax.swing.JRadioButton;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 
 public class MainWindow {
 
 	private JFrame frmStravaGpxConverter;
 	private JTextField txtSourceFile;
 	private JFileChooser fc;
 	private JTextArea statusTextArea;
 	protected String newline = "\n";
 	private JTextField loginVal;
 	private JPasswordField passwordVal;
 	private JTextField txtActivityName;
 	private String activityType;
 	private String altimeterEval;
 	private String authToken;
 
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.frmStravaGpxConverter.setVisible(true);
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
 	}
 
 	protected void statusLog(String actionDescription) {
         statusTextArea.append(actionDescription + newline);
         statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength()-1);
     }
 	
 	protected boolean doLogin() {
 		boolean ret = false;
 		
 		HttpClient httpClient = new DefaultHttpClient();
 		statusLog("Authenticating athlete...");
 	    try {
 	        HttpPost request = new HttpPost("http://www.strava.com/api/v2/authentication/login");
 	        String jsonString = "{\"email\":\"" + loginVal.getText() + "\",\"password\":\"" + new String(passwordVal.getPassword()) + "\"} ";
 	        StringEntity params = new StringEntity(jsonString);
 	        //statusLog("Sending Entity: " + jsonString);
 	        request.addHeader("content-type", "application/json");
 	        request.setEntity(params);
 	        HttpResponse response = httpClient.execute(request);
 
 	        if (response.getStatusLine().getStatusCode() != 200) {
 	        	statusLog("Failed to Login.");
 	        	HttpEntity entity = response.getEntity();
 	        	if (entity != null) {
 	        		String output = EntityUtils.toString(entity);
 	        		statusLog(output);
 	        	}
 			}
 	 
 	        HttpEntity entity = response.getEntity();
 	 
 			if (entity != null) {
 				String output = EntityUtils.toString(entity);
 				//statusLog(output);
 				JSONObject userInfo = new JSONObject(output);
 				JSONObject athleteInfo = userInfo.getJSONObject("athlete");
 				statusLog("Logged in as " + athleteInfo.get("name"));
 				authToken = (String)userInfo.get("token");
 				if (authToken.length() > 0) {
 					ret = true;
 				}
 		    } else {
 		    	statusLog("What happened?!");
 		    }
 			
 			
 	    }catch (Exception ex) {
 	        // handle exception here
 	    	ex.printStackTrace();
 	    } finally {
 	        httpClient.getConnectionManager().shutdown();
 	    }
 		
 		return ret;
 	}
 	
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmStravaGpxConverter = new JFrame();
 		frmStravaGpxConverter.getContentPane().setFont(new Font("Tahoma", Font.BOLD, 11));
 		frmStravaGpxConverter.setTitle("Garmin GPX Importer for Strava");
 		frmStravaGpxConverter.setBounds(100, 100, 441, 426);
 		frmStravaGpxConverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmStravaGpxConverter.getContentPane().setLayout(null);
 		
 		fc = new JFileChooser();
 		
 		JLabel lblThisToolConverts = new JLabel("Upload Ride Data from a Garmin GPX file with HR and Cadence data.");
 		lblThisToolConverts.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblThisToolConverts.setHorizontalAlignment(SwingConstants.CENTER);
 		lblThisToolConverts.setBounds(10, 11, 405, 14);
 		frmStravaGpxConverter.getContentPane().add(lblThisToolConverts);
 		
 		txtSourceFile = new JTextField();
 		txtSourceFile.setBounds(24, 36, 286, 20);
 		frmStravaGpxConverter.getContentPane().add(txtSourceFile);
 		txtSourceFile.setColumns(10);
 		
 		statusTextArea = new JTextArea();
 		statusTextArea.setEditable(false);
 		statusTextArea.setColumns(100);
 		statusTextArea.setRows(100);
 		statusTextArea.setLineWrap(true);
 		JScrollPane statusScroller = new JScrollPane(statusTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		statusScroller.setBounds(10, 254, 405, 112);
 		frmStravaGpxConverter.getContentPane().add(statusScroller);
 		
 		JLabel lblSourceGpxFile = new JLabel("Source GPX File");
 		lblSourceGpxFile.setBounds(24, 57, 111, 14);
 		frmStravaGpxConverter.getContentPane().add(lblSourceGpxFile);
 		
 		JButton btnNewButton = new JButton("Find Src");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				fc.setFileFilter(new GPXFilter());
 				int returnVal = fc.showDialog(frmStravaGpxConverter, "Choose Source");
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					txtSourceFile.setText(fc.getSelectedFile().getPath());
 					File f = new File("");
 					fc.setSelectedFile(f);
 				}
 			}
 		});
 		
		btnNewButton.setBounds(320, 34, 78, 23);
 		frmStravaGpxConverter.getContentPane().add(btnNewButton);
 		
 		loginVal = new JTextField();
 		loginVal.setBounds(100, 80, 210, 20);
 		frmStravaGpxConverter.getContentPane().add(loginVal);
 		loginVal.setColumns(10);
 		
 		passwordVal = new JPasswordField();
 		passwordVal.setBounds(100, 110, 210, 20);
 		frmStravaGpxConverter.getContentPane().add(passwordVal);
 		passwordVal.setColumns(10);
 		
 		JLabel lblLogin = new JLabel("Login:");
 		lblLogin.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblLogin.setBounds(45, 82, 51, 14);
 		frmStravaGpxConverter.getContentPane().add(lblLogin);
 		
 		JLabel lblNewLabel = new JLabel("Password:");
 		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel.setBounds(24, 111, 72, 14);
 		frmStravaGpxConverter.getContentPane().add(lblNewLabel);
 		
 		JRadioButton typeIsRide = new JRadioButton("Ride");
 		typeIsRide.setBounds(144, 175, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsRide);
 		
 		JRadioButton typeIsRun = new JRadioButton("Run");
 		typeIsRun.setBounds(211, 175, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsRun);
 		
 		JRadioButton typeIsHike = new JRadioButton("Hike");
 		typeIsHike.setBounds(270, 175, 109, 23);
 		frmStravaGpxConverter.getContentPane().add(typeIsHike);
 		
 		ButtonGroup eventType = new ButtonGroup();
 		eventType.add(typeIsHike);
 		eventType.add(typeIsRun);
 		eventType.add(typeIsRide);
 		
 		typeIsHike.addActionListener(new TypeAction());
 		typeIsRun.addActionListener(new TypeAction());
 		typeIsRide.addActionListener(new TypeAction());
 		
 		JLabel lblAltimeter = new JLabel("Altimeter:");
 		lblAltimeter.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblAltimeter.setBounds(10, 204, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblAltimeter);
 		
 		JRadioButton altYes = new JRadioButton("Yes");
 		altYes.setBounds(144, 196, 51, 23);
 		frmStravaGpxConverter.getContentPane().add(altYes);
 		
 		JRadioButton altNo = new JRadioButton("No");
 		altNo.setBounds(211, 196, 57, 23);
 		frmStravaGpxConverter.getContentPane().add(altNo);
 		
 		ButtonGroup altimeterAvail = new ButtonGroup();
 		altimeterAvail.add(altYes);
 		altimeterAvail.add(altNo);
 		
 		altYes.addActionListener(new DeviceAction());
 		altNo.addActionListener(new DeviceAction());
 		
 		JLabel lblActivityType = new JLabel("Activity Type:");
 		lblActivityType.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblActivityType.setBounds(10, 179, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblActivityType);
 		
 		JLabel lblActivityName = new JLabel("Activity Name:");
 		lblActivityName.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblActivityName.setBounds(10, 148, 86, 14);
 		frmStravaGpxConverter.getContentPane().add(lblActivityName);
 		
 		txtActivityName = new JTextField();
 		txtActivityName.setBounds(100, 147, 315, 20);
 		frmStravaGpxConverter.getContentPane().add(txtActivityName);
 		txtActivityName.setColumns(10);
 		
 		JButton btnConvertIt = new JButton("Upload Data");
 		btnConvertIt.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (doLogin()) {
 					// Check stuff
 					//if (txtSourceFile.getText().equals(txtDestFile.getText())) {
 					//	txtStatusArea.append("Can't write to same file you read from!\n");
 					//	return;
 					//}
 					Converter c = new Converter();
 					c.setInFile(txtSourceFile.getText());
 					c.setAuthToken(authToken);
 					c.setActivityName(txtActivityName.getText());
 					c.setActivityType(activityType);
 					if (altimeterEval.startsWith("Yes")) {
 						c.setDeviceType("Garmin Edge 800");
 					} else {
 						c.setDeviceType("Garmin Edge 200");
 					}
 					//c.setOutFile(txtDestFile.getText());
 					statusLog("Starting Conversion and Upload...");
 					c.convert(statusTextArea);		
 					statusLog("Finished!");
 				} else {
 					statusLog("Problems Authenticating.");
 				}
 			}
 		});
 		btnConvertIt.setBounds(144, 226, 131, 23);
 		frmStravaGpxConverter.getContentPane().add(btnConvertIt);
 		
 
 	}
 	
 	public class TypeAction implements ActionListener { 
 		public void actionPerformed(ActionEvent e) {
 			activityType = e.getActionCommand();
 		}
 	}
 	
 	public class DeviceAction implements ActionListener { 
 		public void actionPerformed(ActionEvent e) {
 			altimeterEval = e.getActionCommand();
 		}
 	}
 }
