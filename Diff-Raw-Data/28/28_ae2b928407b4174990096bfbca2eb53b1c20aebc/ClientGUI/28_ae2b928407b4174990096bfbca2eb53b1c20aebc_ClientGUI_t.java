 import java.io.*;
 import java.net.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.text.*;
 
 /** 
     Client for CSE 667 Project 2
     Team 4:
     Peter McLarhan
     Benjamin Rogers
     Jun Wang
  **/
 class ClientGUI extends JFrame implements ActionListener{
 
     static int SEARCH_FLAG = 0;
     static int FILE_FLAG = 1;
     static int ERROR_FLAG = -1;
     static String CONFIG_FILE = "client.config";
     static int BLOCK_SIZE = 32;
  
     String host;
     int sock;
     String directory;
 
     JFrame frame = new JFrame();
     JPanel pane = new JPanel();
     JLabel hostLabel = new JLabel("Hostname:");
     JLabel sockLabel = new JLabel("Port Number:");
     JTextField hostText = new JTextField(20);
     JTextField sockText = new JTextField(20);
     JTextField searchText = new JTextField(20);
     JButton searchButton = new JButton("Search");
     JTextField fileText = new JTextField(20);
     JButton fileButton = new JButton("Upload File");
     JTextField directoryText = new JTextField(20);
     JButton directoryButton = new JButton("Set Directory");
 
     // sets up a very basic GUI
     ClientGUI() {
 	super("Remote Storage Client");
 	setBounds(100,100,400,200);
 	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	pane.setLayout(new GridLayout(5, 2, 5, 10));
 	Container con = this.getContentPane();
 	con.add(pane);
 
 	// Read in the config file
 	try {
 	    FileInputStream fstream = new FileInputStream(CONFIG_FILE);
 	    DataInputStream in = new DataInputStream(fstream);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	    String strLine;
 	    host = br.readLine().substring(7);
 	    sock = Integer.parseInt(br.readLine().substring(9));
 	    directory = br.readLine().substring(12);
 	    
 	} catch (Exception e) {
 	    System.err.println("Execption: " + e.getMessage());
 	    JOptionPane.showMessageDialog(null, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
 	    host = "localhost";
 	    sock = 6789;
 	    directory = System.getProperty("user.dir");
 	}
 
 	hostText.setText(host);
 	sockText.setText(sock + "");
 	directoryText.setText(directory);
 
 	hostText.addActionListener(this);
 	sockText.addActionListener(this);
 	searchText.addActionListener(this);
 	searchButton.addActionListener(this);
 	fileText.addActionListener(this);
 	fileButton.addActionListener(this);
 	directoryText.addActionListener(this);
 	directoryButton.addActionListener(this);
 	pane.add(hostLabel);
 	pane.add(sockLabel);
 	pane.add(hostText);
 	pane.add(sockText);
 	pane.add(searchText);
 	pane.add(searchButton);
 	pane.add(fileText);
 	pane.add(fileButton);
 	pane.add(directoryText);
 	pane.add(directoryButton);
 	setVisible(true);
     }
 
     // When buttons are pressed
     public void actionPerformed(ActionEvent event) {
 
 	ArrayList<String> message = new ArrayList<String>();
 	message.clear();
 	
 	Object source = event.getSource();
 
 	// if a search is requested
 	// TODO
 	if (source == searchButton) {
 
 	    message.add(searchText.getText());
 	    
 	    // Send the search text
 	    try {
 		connect(host, sock, message, SEARCH_FLAG);
 	    } catch (Exception e) {
 		System.err.println("Execption: " + e.getMessage());
 		JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
 	    }
 	     setVisible(true);
 	}
 
 	// if a file upload is requested
 	// TODO
 	if (source == fileButton) {
 
 	    // Read in the file
 	    try {
 		ReadFile(fileText.getText(), message);
 	    } catch (Exception e) {
 		System.err.println("Execption: " + e.getMessage());
 		JOptionPane.showMessageDialog(null, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
 		message.add("ERROR");
 	    }
 	    
 	    // Send the file text
 	    try {
 		connect(host, sock, message, FILE_FLAG);
 	    } catch (Exception e) {
 		System.err.println("Execption: " + e.getMessage());
 		JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
 		//e.printStackTrace();
 	    }
 	     setVisible(true);
 	}
     }
 
     // reads in a file, the lines are added the the message arraylist
     public void ReadFile(String filename, ArrayList<String> message) throws Exception{
 
 	message.add(filename);
 
 	FileInputStream fstream = new FileInputStream(directory + filename);
 	DataInputStream in = new DataInputStream(fstream);
 	BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	String strLine;
 	while ((strLine = br.readLine()) != null) {
 	    message.add(strLine);
 	}
     }
 
     // converts Bytes to string
     public String bytesToString(byte[][] wordArray) {
 	String str = "";
 	for (int i = 0; i < wordArray.length; i++) {
 	    str += new String(wordArray[i]);
 	    System.out.println(new String(wordArray[i]));
 	}
 	return str;
     }
 
    // delete leading zeros
     public static String dezero(String str) {
 	String newString = str.substring(1, str.length());
 	while (newString.indexOf('0') == 0 && newString.length() > 1) {
 	    newString = newString.substring(2);
 	}
 	return newString;
     }
 
     // converts Int to padded int string
     public String blockInt(int value) {
 	DecimalFormat myFormatter = new DecimalFormat("00000000000000000000000000000000");
 	return myFormatter.format(value);
     }
 
     // reduces spaces of length > 1 to length 1
     public String reduceSpaces(String str) {
 	String newStr = str;
 	for (int i = 0; i < newStr.length() - 1; i++) {
 	    if (newStr.charAt(i) == ' ' && newStr.charAt(i+1) == ' ') {
 		newStr = newStr.substring(0, i) + newStr.substring(i + 1);
 		i--;
 	    }
 	}
 	return newStr;
     }
 
     // counts the number of given chars in a string
     public static int count(String str, char c) {
 	int count = 0;
 
 	for (int i = 0; i < str.length(); i++) {
 	    if (str.charAt(i) == c) {
 		count++;
 	    }
 	}
 	
 	return count;
     }
 
     // byte array to string of ints seperated by commas
     public static String bytesToIntStr(byte[] bytes) {
 	String str = "";
 	for (int i = 0; i < bytes.length; i++) {
 	    str += Byte.toString(bytes[i]) + ",";
 	}
 	str = str.substring(0, str.length()-1);
 	return str;
     }
 
         // decodes a block
    public static String decodeBlock(String str) {
 	String newStr = "";
 	Integer temp;
 	String[] strs = str.split(",");
 	if (strs.length != BLOCK_SIZE) {
 	    return "";
 	}
	byte[] b = new byte[strs.length];
 	for (int i = 0; i < strs.length; i++) {
 	    if (!strs[i].equals("")) {
 		temp = Integer.parseInt(strs[i]);
		b[i] = temp.byteValue();
 	    }
 	}
	newStr = new String(b);
 	return newStr;
     }
 
     // connects to the server and sends the message
     public void connect(String host, int sock, ArrayList<String> message, int flag) throws Exception {
 
 	Enc enc = new Enc();
 	String sentence;
 	String modifiedSentence;
 	int size;
 	BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
 	Socket clientSocket = new Socket(host, sock);
 	DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
 	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 
 	sentence = ""; //flag + " \n " + message.size() + " \n ";
 	for (int i = 0; i < message.size(); i++) {
 	    sentence += message.get(i) + "\n";
 	}
 
 	byte[][] preWordArray = enc.toBlocks(sentence);
 	byte[][] wordArray = new byte[preWordArray.length][BLOCK_SIZE];
 
 	byte[] key1 = "this can be any size we want".getBytes();
 	byte[] key2 = "16 chars exactly".getBytes();
 	byte[] streamKey = "This one is twenty-four!".getBytes();
 
 	StreamChunker stream = new StreamChunker(streamKey);
 
 	for (int i = 0; i < preWordArray.length; i++) {
 	    byte [] Xi = enc.preEnc(preWordArray, i, key2);
 	    if (flag != SEARCH_FLAG) {
 		wordArray[i] = enc.xor(Xi, enc.getT(stream.getChunk(), enc.getPubkey(Xi, key1)));
 	    } else {
 		wordArray[i] = Xi;
 	    }
 	    //System.out.print(new String(wordArray[i]));
 	}
 
 	if (flag == FILE_FLAG) {
 	    for (int i = 0; i < wordArray[0].length; i++) {
 		wordArray[0][i] = preWordArray[0][i];
 	    }
 	}
 
 	outToServer.writeChars(blockInt(flag));
 	outToServer.writeChars(blockInt(wordArray.length));
 	for (int i = 0; i < wordArray.length; i++) {
 	    outToServer.writeBytes(bytesToIntStr(wordArray[i]) + "\n");
 	}
 	if (flag == SEARCH_FLAG) {
 		outToServer.writeBytes(bytesToIntStr(Enc.getPubkey(wordArray[0],key1)) + "\n");// sending key1
 	}
 	modifiedSentence = inFromServer.readLine();
 
 	// if File message
 	if (FILE_FLAG == Integer.parseInt(modifiedSentence)) {
 	    modifiedSentence = inFromServer.readLine();
 	    // if Error message
 	    if (ERROR_FLAG == Integer.parseInt(modifiedSentence)) {
 		JOptionPane.showMessageDialog(null, "Error writing file", "Server Error", JOptionPane.ERROR_MESSAGE);
 	    } else {
 		JOptionPane.showMessageDialog(null, "Successful file write", "Server Message", JOptionPane.PLAIN_MESSAGE);
 	    }
 	}
 	// if Search message
 	if (SEARCH_FLAG == Integer.parseInt(modifiedSentence)) {
 	    modifiedSentence = inFromServer.readLine();
 	    // if Error message
 	    if (ERROR_FLAG == Integer.parseInt(modifiedSentence)) {
 		JOptionPane.showMessageDialog(null, "Error searching for word", "Server Error", JOptionPane.ERROR_MESSAGE);
 	    } else {
 		size = Integer.parseInt(inFromServer.readLine());
 		message.clear();
 		sentence = "";
 
 		for (int i = 0; i < size; i++) {
 		    message.add(inFromServer.readLine());
 		    sentence += "\n" + message.get(i);
 		}
 
 		// Choose to weather or not to download found files
 		Object[] options = {"Download", "Cancel"};
 		int n = JOptionPane.showOptionDialog(frame, size + " files with matches:" + sentence + "\nDownload files?", "Server Message", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 		if (n == JOptionPane.YES_OPTION) {
 		    outToServer.writeBytes(FILE_FLAG + "\n" + message.size() + sentence + "\n");
 		    try {
 			int fSize;
 			// download files
 			for (int i = 0; i < message.size(); i++) {
 			    fSize = Integer.parseInt(inFromServer.readLine());
 			    if (fSize == ERROR_FLAG) {
 				throw new Exception("Server Error");
 			    } else {
 				FileWriter fstream = new FileWriter(directory + message.get(i));
 				BufferedWriter out = new BufferedWriter(fstream);
 				String outString;
                                byte[][] blocks = new byte[fSize][];
 				for (int j = 0; j < fSize; j++) {
 				    outString = inFromServer.readLine();
 				    // DECODING HAPPENS HERE!!!!!!!!!!
				    outString = decodeBlock(outString);
                                    blocks[j] = outString.getBytes();
 				    // DECODING HAPPENS HERE!!!!!!!!!!
 				}
                                byte[][] decrypted = Enc.decrypt(blocks, streamKey, key1, key2);
                                for (int j = 0; j < decrypted.length; j++) {
				    out.write((new String(decrypted[i])) + " ");			
                                }
 				out.close();
 			    }
 			}
 		    } catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Error download files", "Server Error", JOptionPane.ERROR_MESSAGE);
 		    }
 		} else {
 		    outToServer.writeBytes(ERROR_FLAG + "\n");
 		}
 	    }
 	}
 
 	clientSocket.close();
 
     }
 
     // initializes the GUI
     public static void main(String argv[]){
 
 	new ClientGUI();
 
     }
 }
