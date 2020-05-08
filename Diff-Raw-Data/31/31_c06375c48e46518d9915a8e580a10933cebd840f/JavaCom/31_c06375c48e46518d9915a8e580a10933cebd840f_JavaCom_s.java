 package javacom;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.TextArea;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JLabel;
 
 import java.awt.Label;
 import java.awt.Choice;
 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JRadioButton;
 import javax.swing.JToggleButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 
 public class JavaCom extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField urlTextField;
 	private JTextField querystringTextField;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	private Boolean isGet = false;
 	private long messageInterval = 5000;
 	private Timer messagingTimer = new Timer();
 	TextArea serverResponseTextArea;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					JavaCom frame = new JavaCom();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public JavaCom() {
 		setTitle("JavaCom");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 550, 500);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		serverResponseTextArea = new TextArea();
 		serverResponseTextArea.setBounds(10, 192, 514, 259);
 		contentPane.add(serverResponseTextArea);
 		
 		JLabel lblServerResponse = new JLabel("Server Response:");
 		lblServerResponse.setBounds(10, 172, 118, 14);
 		contentPane.add(lblServerResponse);
 		
 		Label label = new Label("URL:");
 		label.setBounds(10, 10, 50, 22);
 		contentPane.add(label);
 		
 		Label label_1 = new Label("Query String:");
 		label_1.setBounds(10, 38, 86, 22);
 		contentPane.add(label_1);
 		
 		Label label_2 = new Label("Send Message Interval:\r\n");
 		label_2.setBounds(10, 66, 139, 22);
 		contentPane.add(label_2);
 		
 		Label label_3 = new Label("Protocol:");
 		label_3.setBounds(10, 144, 62, 22);
 		contentPane.add(label_3);
 		
 		urlTextField = new JTextField();
 		urlTextField.setBounds(75, 12, 385, 20);
 		contentPane.add(urlTextField);
 		urlTextField.setColumns(10);
 		
 		querystringTextField = new JTextField();
 		querystringTextField.setColumns(10);
 		querystringTextField.setBounds(99, 40, 395, 20);
 		contentPane.add(querystringTextField);
 		
 		final List messageIntervalList = new List();
 		messageIntervalList.setBounds(173, 66, 190, 73);
 		messageIntervalList.add("5 seconds");
 		messageIntervalList.add("15 seconds");
 		messageIntervalList.add("30 seconds");
 		messageIntervalList.add("60 seconds");
 		messageIntervalList.addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
             	String selected = messageIntervalList.getSelectedItem();
             	if (selected.equals("5 seconds")) messageInterval = 5000;
             	else if (selected.equals("15 seconds")) messageInterval = 15000;
             	else if (selected.equals("30 seconds")) messageInterval = 30000;
             	else if (selected.equals("60 seconds")) messageInterval = 60000;
             }
         });  
 		contentPane.add(messageIntervalList);
 		
 		JRadioButton rdbtnGet = new JRadioButton("GET");
 		rdbtnGet.addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
             	isGet = true;
             }
         });  
 		buttonGroup.add(rdbtnGet);
 		rdbtnGet.setBounds(78, 144, 50, 23);
 		contentPane.add(rdbtnGet);
 		
 		JRadioButton rdbtnPost = new JRadioButton("POST");
 		rdbtnPost.addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
             	isGet = false;
             }
         });  
 		buttonGroup.add(rdbtnPost);
 		rdbtnPost.setBounds(133, 143, 62, 23);
 		contentPane.add(rdbtnPost);
 		
 		final JButton btnStartSending = new JButton("Start Sending");
 		btnStartSending.setBounds(385, 144, 139, 23);
 		btnStartSending.addActionListener(new ActionListener() {
  
             public void actionPerformed(ActionEvent e)
             {
             	//error checking
             	if (!isGet)
             	{
             		JOptionPane.showMessageDialog(null, "When protocol is POST, you must specify a query string such as: ?message=Test Message", "ERROR", JOptionPane.INFORMATION_MESSAGE);
             	}
             	if (btnStartSending.getText() == "Start Sending")
             	{
             		btnStartSending.setText("Stop Sending");
            		messagingTimer.cancel();
             	}
             	else
             	{
             		btnStartSending.setText("Start Sending");
            		startSending();
             	}
             }
         });  
 		contentPane.add(btnStartSending);
 		
 	}
 	
 	private void startSending()
 	{
 		messagingTimer.scheduleAtFixedRate(new TimerTask() {
 			  @Override
 			  public void run() {
 				  if(isGet)
 				  {
 					  try
 					  {
						  serverResponseTextArea.append(sendGet());
 					  } catch (Exception e) 
 					  {
 							// TODO Auto-generated catch block
						  serverResponseTextArea.append(e.getMessage());
 					  }
 				  } else {
 					  try
 					  {
						  serverResponseTextArea.append(sendPost());
 					  } catch (Exception e) 
 					  {
 							// TODO Auto-generated catch block
						  serverResponseTextArea.append(e.getMessage());
 					  }
 				  }
 			  }
 			}, messageInterval, messageInterval);
 	}
 	
 	
 	// HTTP GET request
 	//based on: http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 	private String sendGet() throws Exception {
  
 		String url = urlTextField.getText() + querystringTextField.getText();
  
 		URL obj = new URL(url);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
  
 		// optional default is GET
 		con.setRequestMethod("GET");
  
 		//add request header
 		con.setRequestProperty("User-Agent", "Mozilla/5.0");
  
 		int responseCode = con.getResponseCode();
 		BufferedReader in = new BufferedReader(
 		        new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		StringBuffer response = new StringBuffer();
 		response.append("\nSending 'GET' request to URL : " + url);
		response.append("\nResponse Code : " + responseCode);
 		while ((inputLine = in.readLine()) != null) {
 			response.append(inputLine);
 		}
 		in.close();
  
 		//return result
 		return response.toString();
 	}
 	
 	// HTTP POST request
 	private String sendPost() throws Exception {
  
 		String url = urlTextField.getText() ;
 		URL obj = new URL(url);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
  
 		//add request header
 		con.setRequestMethod("POST");
 		con.setRequestProperty("User-Agent", "Mozilla/5.0");
 		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
  
 		String urlParameters = querystringTextField.getText().replace("?", "");
  
 		// Send post request
 		con.setDoOutput(true);
 		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
 		wr.writeBytes(urlParameters);
 		wr.flush();
 		wr.close();
  
 		int responseCode = con.getResponseCode();
 		 
 		BufferedReader in = new BufferedReader(
 		        new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		StringBuffer response = new StringBuffer();
 		
 		response.append("\nSending 'POST' request to URL : " + url);
 		response.append("\nPost parameters : " + urlParameters);
		response.append("\nResponse Code : " + responseCode);
  
 		while ((inputLine = in.readLine()) != null) {
 			response.append(inputLine);
 		}
 		in.close();
  
 		//return result
 		return response.toString();
  
 	}
 }
