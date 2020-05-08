 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.TargetDataLine;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class VoipClient implements ActionListener{
 
 	//gui
 	private static JFrame frame;
 	private static JTextField textField;
 	private static JTextArea textArea;
 	private static JScrollPane scrollPane;
 	private static JList userlist;
 	
 	private InetAddress ip; //the ip address the client whishes to call
 	private InetAddress server_ip; //the ip of the server
 	private int receive_port; //the port to which the client wishes to send
 	private int send_port; //the clients own port, to listen on for traffic
 	private int server_port; //the port with which to connect to the server
 	private DatagramSocket receive_socket; //the socket to receive on
 	private DatagramSocket send_socket; //the socket to send on
 	private Socket socket; //TCP socket to server
 	
 	private static OutputStream out = null; //XXX made these static
 	private static BufferedReader in = null; //XXX made these static
 	
 	private static Thread listenLoop; //XXX my addition
 
   private static boolean connected = false;
 	//private static ArrayList<item> users; //XXX my addition
 	
 
 	//boolean stopCapture = false;
 	AudioFormat audioFormat;
 	TargetDataLine targetDataLine;
 	AudioInputStream audioInputStream;
 	SourceDataLine sourceDataLine;
 
 	public VoipClient(String ip, int receive_port, int send_port){
 		
 		//build the gui
 		frame = new JFrame("VOIP");
 		
 		JPanel pane = new JPanel(new GridBagLayout());
 		GridBagConstraints c1 = new GridBagConstraints();
 		GridBagConstraints c2 = new GridBagConstraints();
 		GridBagConstraints c3 = new GridBagConstraints();
 
 		textArea = new JTextArea("Commands: \n \\call <ip> \n \\dc \n \\exit \n");
 		textArea.setEditable(false);
 		textArea.setLineWrap(true);
 		scrollPane = new JScrollPane(textArea);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		
 		textField = new JTextField();
 		textField.addActionListener(this);
 		textField.setText("\\call 127.0.0.1");
 		userlist = new JList();
 			
 		c1.fill = GridBagConstraints.BOTH;
 		c1.insets = new Insets(10, 10, 5, 5); //top, left, bottom, right
 		c1.gridwidth = 1;
 		c2.gridheight = 1;
 		c1.ipadx = 350;
 		c1.ipady = 400; 
 		c1.weightx = 1;
 		c1.weighty = 1;
 		c1.gridx = 0;
 		c1.gridy = 0;
 		pane.add(scrollPane, c1);
 
 		c2.fill = GridBagConstraints.BOTH;
 		c2.insets = new Insets(5, 10, 10, 10);
 		c2.gridwidth = 1;
 		c2.gridheight = 1;
 		c2.ipadx = 350;
 		c2.ipady = 100;
 		c2.weightx = 1;
 		c2.weighty = 1;
 		c2.gridx = 0;
 		c2.gridy = 1;
 		pane.add(textField, c2);
 		
 		c3.fill = GridBagConstraints.BOTH;
 		c3.insets = new Insets(10, 5, 10, 10);
 		c3.gridwidth = 1;
 		c3.gridheight = 1;
 		c3.ipadx = 200;
 		c3.ipady = 400;
 		c3.weightx = 0.2;
 		c3.weighty = 0.2;
 		c3.gridx = 1;
 		c3.gridy = 0;
 		pane.add(userlist, c3);
 		
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    frame.getContentPane().add(pane);
 	    frame.pack();
 	    /*
 	     * requestFocusInWindow must be called after component realization,
 	     * frame.pack() and before the frame is displayed.
 	     */
 		textField.requestFocusInWindow();
 		textField.setSelectionStart(6);
 		textField.setSelectionEnd(15);
 	    frame.setSize(500, 500);
 	    frame.setVisible(true); 
 		
 		this.receive_port = receive_port;
 		this.send_port = send_port;
 		
 		try	{
 			this.ip = InetAddress.getByName(ip);
 		} catch (Exception e)	{
 			System.out.println(e.getMessage());
 		}
 
 		try {
 			this.receive_socket = new DatagramSocket(this.receive_port);
 			this.send_socket = new DatagramSocket(this.send_port);
 		} catch (Exception e)	{
 			System.out.println(e.getMessage());
 		}
 		/*
 		Thread CaptureAudio = new Thread(new CaptureAudio(this.send_socket, this.ip, this.receive_port));
 		CaptureAudio.start();
 
 		Thread PlayAudio = new Thread(new PlayAudio(this.receive_socket));
 		PlayAudio.start();
 		*/
 	}
 
 	//This method creates and returns an
 	// AudioFormat object for a given set
 	// of format parameters.  If these
 	// parameters don't work well for
 	// you, try some of the other
 	// allowable parameter values, which
 	// are shown in comments following
 	// the declarations.
 	private AudioFormat getAudioFormat(){
 		float sampleRate = 8000.0F;
 		//8000,11025,16000,22050,44100
 		int sampleSizeInBits = 16;
 		//8,16
 		int channels = 1;
 		//1,2
 		boolean signed = true;
 		//true,false
 		boolean bigEndian = false;
 		//true,false
 		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
 	}//end getAudioFormat
 
 	//This method captures audio input
 	// from a microphone and saves it in
 	// a ByteArrayOutputStream object.
 	class CaptureAudio implements Runnable	{
 
 		private byte[] tempBuffer;
 		private DatagramSocket socket;
 		private InetAddress ip;
 		private int port;
 
 		public CaptureAudio(DatagramSocket socket, InetAddress ip, int port)	{
 			this.socket = socket;
 			this.ip = ip;
 			this.port = port;
 			this.tempBuffer = new byte[10000];
 		}
 
 		public void run()	{  
 			try {
 				//Get everything set up for capture
 				audioFormat = getAudioFormat();
 				DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
 				targetDataLine = (TargetDataLine)
 				AudioSystem.getLine(dataLineInfo);
 				targetDataLine.open(audioFormat);
 				targetDataLine.start();
 
 				//Create a thread to capture the
 				// microphone data and start it
 				// running.  It will run until
 				// the Stop button is clicked.
 
 				//Thread captureThread = new Thread(new CaptureThread(send_socket, ip, dest_port));
 				//captureThread.start();
 
 				//capture the audio now
 				//byteArrayOutputStream = new ByteArrayOutputStream();
 				//stopCapture = false;
 				try{//Loop until stopCapture is set
 					// by another thread that
 					// services the Stop button.
 					while(/*!stopCapture*/true){
 						//Read data from the internal
 						// buffer of the data line.
 						int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
 						if(cnt > 0){
 							//Save data in output stream
 							// object.
 							//byteArrayOutputStream.write(tempBuffer, 0, cnt); //XXX save to memory, put socket here?
 							DatagramPacket outPacket = new DatagramPacket(tempBuffer, tempBuffer.length, this.ip, this.port);
 							this.socket.send(outPacket);
 						}//end if
 					}//end while
 					//byteArrayOutputStream.close();
 				}catch (Exception e) {
 					System.out.println(e);
 					System.exit(0);
 				}//end catch
 
 			} catch (Exception e) {
 				System.out.println(e);
 				System.exit(0);
 			}//end catch
 		}
 	}//end captureAudio method
 
 	class PlayAudio implements Runnable {
 
 		private DatagramSocket socket;
 		private byte[] tempBuffer;
 
 		public PlayAudio(DatagramSocket socket)	{
 			this.socket = socket;
 			this.tempBuffer = new byte[10000];
 		}
 
 		public void run()	{
 			try{
 				//Get everything set up for
 				// playback.
 				//Get the previously-saved data
 				// into a byte array object.
 
 				//byte audioData[] = byteArrayOutputStream.toByteArray(); //XXX saved stuff, put socket here
 
 				DatagramPacket inPacket;
 
 				while (true)	{
 
 					inPacket = new DatagramPacket(tempBuffer, tempBuffer.length);
 					this.socket.receive(inPacket);
 
 					byte[] audioData = inPacket.getData();
 
 					//Get an input stream on the
 					// byte array containing the data
 					InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
 					AudioFormat audioFormat = getAudioFormat();
 					audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioData.length/audioFormat.getFrameSize());
 					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
 					sourceDataLine = (SourceDataLine)
 					AudioSystem.getLine(dataLineInfo);
 					sourceDataLine.open(audioFormat);
 					sourceDataLine.start();
 
 					//Create a thread to play back
 					// the data and start it
 					// running.  It will run until
 					// all the data has been played
 					// back.
 
 					//Thread playThread = new Thread(new PlayThread());
 					//playThread.start();
 					try { 
 						int cnt;
 						//Keep looping until the input
 						// read method returns -1 for
 						// empty stream.
 						while((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1){
 							if(cnt > 0){
 								//Write data to the internal
 								// buffer of the data line
 								// where it will be delivered
 								// to the speaker.
 								sourceDataLine.write(tempBuffer, 0, cnt);
 							}//end if
 						}//end while
 						//Block and wait for internal
 						// buffer of the data line to
 						// empty.
 						sourceDataLine.drain();
 						sourceDataLine.close();
 					}catch (Exception e) {
 						System.out.println(e);
 						System.exit(0);
 					}//end catch
 				} //while
 			} catch (Exception e) {
 				System.out.println(e);
 				System.exit(0);
 			}//end catch
 		}
 
 	}//end playAudio
 	
 	private void client_connect(String ip)	{
 		Thread CaptureAudio = new Thread(new CaptureAudio(this.send_socket, this.ip, this.receive_port));
 		CaptureAudio.start();
 
 		Thread PlayAudio = new Thread(new PlayAudio(this.receive_socket));
 		PlayAudio.start();
 	}
 	
 	private void server_connect(String ip, int port)	{
 		System.out.println("IP of server: " + ip);
 		try {
 			this.server_ip = InetAddress.getByName(ip);
 			this.server_port = port;
 			this.socket = new Socket(this.server_ip, this.server_port);
 			this.out = socket.getOutputStream();
 			this.in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			
 			textArea.append("Connected to server at: " + ip + "\n");
       connected = true;
 			
 
 			
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	private void server_send(String message)	{
 		try	{
 			out.write(message.getBytes());
 		} catch	(Exception e){
 			System.out.println("Error sending message: " + e.getMessage());
 		}
 	}
 	
 	public void actionPerformed(ActionEvent a)	{
 		if (a.getSource() == textField)	{
 			String text = textField.getText();
 			  try	{
 					server_send(text); //XXX TODO do not prepend stuff
 			  } catch(Exception e)	{
 				  textArea.append("Error processing text.\n");
         }
 		  textField.setText("");
 		}
 	}
 	
 	private static class ConnectionListener implements Runnable { //XXX my addition
 
 		public void run() { //listens to incoming messages from the server
 			try {
 				while(true) {
 					String data = in.readLine();
           if (data != null) {
 					  textArea.append(data+"\n");
           }
 				}
 			} catch (Exception e) {
 				System.err.println(e.getMessage());
 			}
 		}
 		
 	}
 
 	public static void main(String args[]){
 		String ip = "127.0.0.1"; //destination ip
 		String receive_port = "3001";
 		String send_port = "3002";
 		
 		VoipClient c = new VoipClient(ip, Integer.parseInt(receive_port), Integer.parseInt(send_port)); 
 
    c.server_connect(args[0], Integer.parseInt(args[1])); //TODO use command line arg
 		
 	  listenLoop = new Thread(new ConnectionListener()); //XXX my addition
 	  listenLoop.start(); //XXX my addition
 
     while(connected);
 	}
 }
 
