 package Client;
 /**
  * SOEN 490
  * Capstone 2011
  * Team members: 	
  * 			Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.http.client.ClientProtocolException;
 
 
 public class DemoGui extends JFrame {
 	
 	private static final int WIDTH = 300;
 	private static final int HEIGHT = 465;
 	private static final String PLAYER_PATH = "C:\\Program Files (x86)\\QuickTime\\QuickTimePlayer.exe"; //change this depending on your computer
 	private static final String DIRECTORY_PATH = ".\\src\\Retrieved Files\\";
 	private JButton uploadFileButton;
 	private JFileChooser fileChooser;
 	private JScrollPane listScrollPane;
 	private Vector<String> audioFileNames; 
 	private Map<String, Message> messages;
 	private JList listOfFiles;
     private String filePath;
 	private Process process;
 	private FileTransfer fileTransfer;
 	private JTextField statusField;
 	private JTextField[] inputFields; // latitude, longitude, speed, email
 	private JTextArea fileInfo;
 	private JScrollPane fileInfoScrollPane;
 	
 	public DemoGui( ) {
 		fileTransfer = new FileTransfer();
 		messages = new HashMap<String, Message>();
 	}
 	
 	public void createAndShowGui() {
 		fileChooser = new JFileChooser(".\\src\\");
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		fileChooser.setFileFilter(new AudioFilter());
 		listOfFiles = new JList();		
 		listScrollPane = new JScrollPane(listOfFiles);	
 		audioFileNames = new Vector<String>();
 		statusField = new JTextField(23);
 		statusField.setEditable(false);
 		fileInfo = new JTextArea(8, 23);
 		fileInfo.setEditable(false);
 		fileInfoScrollPane = new JScrollPane(fileInfo);
 
 		uploadFileButton = new JButton("Upload File");
 		uploadFileButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int fileChooserValue = fileChooser.showDialog(uploadFileButton, "Select");
 				
 				 if (fileChooserValue == JFileChooser.APPROVE_OPTION) {
 					 try {
 						 //send file
 						int response = fileTransfer.uploadFile(fileChooser.getSelectedFile(), inputFields[0].getText(), inputFields[1].getText(), inputFields[2].getText(), inputFields[3].getText());
 						statusField.setText("Status Code: " + response);
 					} catch (ClientProtocolException e1) {
 						e1.printStackTrace();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				 }
 			}
 		});
 				
 		JButton retrieveFileButton = new JButton("Retrieve Files");
 		retrieveFileButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				File directory = new File(DIRECTORY_PATH);		
 				if(!directory.exists()) {
 					directory.mkdir(); // create folder to place the files if it doesn't exist already
 				}				
 				
 				//detele previous audio files in the folder				
 				deleteFiles(directory);			
 				//Retrieve audio files	
 				try {
 					messages = fileTransfer.downloadFiles(inputFields[0].getText(), inputFields[1].getText(), inputFields[2].getText(), "bin", DIRECTORY_PATH, "date");
 				} catch (ClientProtocolException e1) {
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				audioFileNames.clear();
 				for(String name: messages.keySet()) {
 					audioFileNames.add(name);
 				}
 						
 				listOfFiles.setListData(audioFileNames);
 				listScrollPane.validate();
 				listOfFiles.setSelectedIndex(0); // ensures that a file is selected
 				fileInfo.setText(messages.get((String)listOfFiles.getSelectedValue()).toString()); // show information about the audio file in the text area
 				findFilePath();
 			}
 		});
 		
 	    listOfFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listOfFiles.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if(e.getValueIsAdjusting()) {
 					findFilePath();
 				}				
 			}
 		});
 
 		JButton playButton = new JButton("Open Audio File");
 		playButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ProcessBuilder processBuilder = new ProcessBuilder(PLAYER_PATH, filePath);			 
 				try {			
 					if(process != null) {
 						process.destroy(); //If a file is currently playing, close the application playing it. 
 					}
 		
 					process = processBuilder.start();
 					
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}		
 			}		
 		});
 				
 		JButton stopButton = new JButton("Close Audio File");
 		stopButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(process != null) {
 					process.destroy(); // closes the external application
 				}
 			}		
 		});
 		
 		String[] labels = {"Latitude", "Longitude", "Speed", "Email"};
 		
 		JPanel labelPanel = new JPanel(new GridLayout(labels.length / 2, 2));
 		JPanel fieldPanel = new JPanel(new GridLayout(labels.length / 2, 2));
 		
 		JPanel inputPanel = new JPanel(new BorderLayout());
 		inputPanel.add(labelPanel);
 		inputPanel.add(fieldPanel);
 		inputFields = new JTextField[labels.length];
 		
 		for(int i = 0; i < labels.length; i++) {
 			inputFields[i] = new JTextField();
 			inputFields[i].setToolTipText(labels[i]);
 			inputFields[i].setColumns(10);
 			JLabel label = new JLabel(labels[i], JLabel.RIGHT);
 			label.setLabelFor(inputFields[i]);
 			labelPanel.add(label);
 			JPanel panel = new JPanel(new FlowLayout());
 			panel.add(inputFields[i]);
 			fieldPanel.add(panel);
 		}
 		
 		//default values
 		inputFields[0].setText("-73.679810");
 		inputFields[1].setText("45.546050");
		inputFields[2].setText("50");
		inputFields[3].setText("testGui@test.com");
 		
 		JPanel fileChooserPanel = new JPanel();
 		fileChooserPanel.add(uploadFileButton);
 		fileChooserPanel.add(retrieveFileButton);
 		fileChooserPanel.add(statusField);
 		fileChooserPanel.add(listScrollPane);
 		fileChooserPanel.add(fileInfoScrollPane);
 			
 		JPanel mediaButtonPanel = new JPanel();
 		mediaButtonPanel.add(playButton);
 		mediaButtonPanel.add(stopButton);
 		
 		add(fieldPanel, BorderLayout.NORTH);
 		add(fileChooserPanel, BorderLayout.CENTER);
 		add(mediaButtonPanel, BorderLayout.SOUTH);
 
 		setSize(WIDTH, HEIGHT);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setVisible(true);
 	}
 	
 	private void findFilePath() {
 		String selectedFile = "";				
 		selectedFile = (String)listOfFiles.getSelectedValue();				
 		Message message = messages.get(selectedFile);
 		fileInfo.setText(message.toString());
 
 		try {
 			filePath = message.getMessage().getCanonicalPath();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}	
 	}
 	
 	private void deleteFiles(File directory) {	
 		String[] fileNames = directory.list();		
 		for(int i = 0; i < fileNames.length; i++) {
 			File file = new File(directory, fileNames[i]);
 			file.delete();
 		}
 		
 	}
 }
 
