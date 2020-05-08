 package edu.ncsu.csc216.solitaire.ui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.Scanner;
 import java.util.zip.DataFormatException;
 
 import javax.swing.*;
 
 import edu.ncsu.csc216.solitaire.model.*;
 
 /**
  * The userInterface
  * @author Andrew Kofink, William Blazer
  */
 public class UI {
 	
 	private static Deck deck;
 	
 	private static MessageFrame mf;
 	
 	private static DeckFrame df;
 	
 	private static Message message;
 	
 	/**
 	 * @param args The input parameters to the program
 	 */
 	public static void main(String[] args) {
 		userInterface();
 
 		mf = new MessageFrame();
 		mf.setVisible(true);
 	}
 	
 	/**
 	 * Interfaces with the user and accepts the input file
 	 */
 	public static void userInterface() {
 		
 		//Get the deck file from the user
 		JFileChooser chooser = new JFileChooser(new File("."));
 		int returnValue = chooser.showOpenDialog(null);
 		
 		try {
 			if (returnValue == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().canRead()) {
 				File f = chooser.getSelectedFile();
 				Scanner in = new Scanner(f);
 				
 				//Look for proper file format
 				String deckString = "";
 				while (in.hasNextLine()) {
 					deckString += in.nextLine();
 				}
 				
 				//Match this regular expression, or the file format is wrong
 				if (!deckString.matches("\\A(\\d+[ ]){27}\\d+\\z")) {
 					throw new DataFormatException();
 				}
 
 				//Gather file contents into an array of integers
 				in = new Scanner(f);
 				int[] deckInts = new int[28];
 				for (int i = 0; i < deckInts.length; i++) {
 					deckInts[i] = in.nextInt();
 				}
 				
 				deck = new Deck(deckInts);
 			} else {
 				throw new FileNotFoundException();
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.");
 			userInterface();
 		} catch (DataFormatException dfe) {
 			System.out.println("Invalid file.");
 			userInterface();
 		}
 	}
 	
 	public static Deck getDeck() {
 		return deck;
 	}
 	
 	public static MessageFrame getMessageFrame() {
 		return mf;
 	}
 	
 	public static DeckFrame getDeckFrame() {
 		return df;
 	}
 	
 	public static void setAnswerLabel(String newText) {
 		df.setAnswerLabel(newText);
 	}
 	
 	public static String getAnswerLabel() {
 		return df.getAnswerLabel();
 	}
 	
 	public static void setDeckFrame(DeckFrame df) {
 		UI.df = df;
 	}
 	
 	public static void setMessage(Message m) {
 		message = m;
 	}
 	
 	public static Message getMessage() {
 		return message;
 	}
 }
 
 class MessageFrame extends JFrame implements ActionListener {
 	
 	private JTextArea messageTextArea;
 	
 	private char encType;
 	
 	public MessageFrame() {
 		super();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new FlowLayout());
 		
 		JPanel messagePanel = new JPanel();
 		messagePanel.setLayout(new BorderLayout());
 		add(messagePanel);
 		
 		JLabel messageTextLabel = new JLabel("Please enter a message.");
 		messagePanel.add(messageTextLabel, BorderLayout.NORTH);
 		
 		messageTextArea = new JTextArea(5, 20);
 		JScrollPane scrollPane = new JScrollPane(messageTextArea);
 		messagePanel.add(scrollPane, BorderLayout.CENTER);
 		
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new FlowLayout());
 		messagePanel.add(buttons, BorderLayout.SOUTH);
 		
 		JButton encrypt = new JButton("Encrypt");
 		encrypt.addActionListener(this);
 		buttons.add(encrypt);
 		
 		JButton decrypt = new JButton("Decrypt");
 		decrypt.addActionListener(this);
 		buttons.add(decrypt);
 		
 		pack();
 	}
 	
 	public void actionPerformed(ActionEvent ae) {
 		Message m = new Message(messageTextArea.getText());
 		UI.setMessage(m);
 		
 		setVisible(false);
 		UI.setDeckFrame(new DeckFrame());
 		UI.getDeckFrame().setVisible(true);
 		
 		if (ae.getActionCommand().toLowerCase().startsWith("e")) {
 			encType = 'e';
 		} else {
 			encType = 'd';
 		}
 	}
 	
 	public String getMessageText() {
 		return messageTextArea.getText();
 	}
 	
 	public void clearMessageText() {
 		messageTextArea.setText("");
 	}
 	
 	public char getEncType() {
 		return encType;
 	}
 }
 
 class DeckFrame extends JFrame implements ActionListener {
 	
 	private JPanel cardPanel;
 	private JRadioButton stepByStepRadio;
 	private JRadioButton letterByLetterRadio;
 	private JRadioButton wholeMessageRadio;
 	private JLabel[] messageLabels;
 	private JLabel answerLabel;
 	private int currentLetterIndex = 0;
 	private JButton runButton;
 	private static final int FULL_COLOR = 255;
 	private static final int HALF_COLOR = 128;
 	private static final int NUM_PANELS = 4;
 	
 	public DeckFrame() {
 		super();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//Create panels
 		//Main Panel
 		JPanel mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
 		add(mainPanel);
 		
 		//Card Panel
 		cardPanel = new JPanel();
 		cardPanel.setLayout(new GridLayout(NUM_PANELS, 1));
 		mainPanel.add(cardPanel, BorderLayout.CENTER);
 		
 		//Control Panel
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(new GridLayout(NUM_PANELS, 1));
 		mainPanel.add(controlPanel, BorderLayout.EAST);
 		
 		//Message Panel
 		JPanel messagePanel = new JPanel();
 		messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		mainPanel.add(messagePanel, BorderLayout.NORTH);
 		
 		//Answer Panel
 		JPanel answerPanel = new JPanel();
 		answerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		mainPanel.add(answerPanel, BorderLayout.SOUTH);
 		
 		//Add components to panels
 		//Card Panel
 		JPanel panels[] = new JPanel[NUM_PANELS];
 		
 		for (int i = 0; i < panels.length; i++) {
 			panels[i] = new JPanel();
 			panels[i].setLayout(new FlowLayout());
 			cardPanel.add(panels[i]);
 		}
 		
 		CardIcons.initIcons();
 		ImageIcon icons[] = CardIcons.icons();
 		
 		for (int i = 0; i < panels.length; i++) {
 			for (int j = i * icons.length / NUM_PANELS; j < (i + 1) * icons.length / NUM_PANELS; j++) {
 				panels[i].add(new JLabel(icons[j]));
 			}
 		}
 		
 		//Control Panel
 		ButtonGroup radioMenu = new ButtonGroup();
 		
 		stepByStepRadio = new JRadioButton("Step By Step");
 		letterByLetterRadio = new JRadioButton("Letter By Letter");
 		wholeMessageRadio = new JRadioButton("Whole Message");
 		
 		stepByStepRadio.addActionListener(this);
 		letterByLetterRadio.addActionListener(this);
 		wholeMessageRadio.addActionListener(this);
 		
 		wholeMessageRadio.setSelected(true);
 		
 		radioMenu.add(stepByStepRadio);
 		radioMenu.add(letterByLetterRadio);
 		radioMenu.add(wholeMessageRadio);
 
 		JPanel runButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		runButton = new JButton("Run to Completion");
 		runButton.addActionListener(this);
 		runButtonPanel.add(runButton);
 		
 		controlPanel.add(stepByStepRadio);
 		controlPanel.add(letterByLetterRadio);
 		controlPanel.add(wholeMessageRadio);
 		controlPanel.add(runButtonPanel);
 		
 		
 		//Message Panel
 		String message = UI.getMessageFrame().getMessageText().toUpperCase();
 		char[] messageChars = message.toCharArray();
 		messageLabels = new JLabel[messageChars.length];
 		
 		for (int i = 0; i < messageLabels.length; i++) {
 			messageLabels[i] = new JLabel("" + messageChars[i]);
 			messageLabels[i].setOpaque(true);
 			messagePanel.add(messageLabels[i]);
 		}
 		
 		//Answer Panel
 		answerLabel = new JLabel("This is a test");
 		answerPanel.add(answerLabel);
 		
 		pack();
 	}
 	
 	public void setAnswerLabel(String newText) {
 		answerLabel.setText(newText);
 	}
 	
 	public String getAnswerLabel() {
 		return answerLabel.getText();
 	}
 	
 	public void highlightChar(int index) {
 		if (index > 0) {
 			messageLabels[index - 1].setBackground(null);
 			messageLabels[index].setBackground(new Color(0, HALF_COLOR / 2, FULL_COLOR, HALF_COLOR / 2));
 		} else {
 			messageLabels[index].setBackground(new Color(0, HALF_COLOR / 2, FULL_COLOR, HALF_COLOR / 2));
 		}
 	}
 
 	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("Letter By Letter")) {
 			runButton.setText("Next Letter");
 		} else if (ae.getActionCommand().equals("Whole Message")) {
 			runButton.setText("Run To Completion");
 		} else if (ae.getActionCommand().equals("Step By Step")) {
 			runButton.setText("Next Step");
 		} else {
 			boolean encrypt = UI.getMessageFrame().getEncType() == 'e';
 			Message m = UI.getMessage();
 			String messageString = m.getMessage();
 
 			Message[] messageArray = new Message[messageString.length()];
 			for (int i = 1; i < messageString.length(); i++) {
 				messageArray[i] = new Message(messageString.substring(i - 1, i));
 			}
 
 			Deck d = UI.getDeck();
 			
 			if (stepByStepRadio.isSelected()) {
 				if (encrypt) {
 					for (int i = 0; i < messageArray.length; i++) {
 						char newChar = '/';
 						while (newChar == '/') {
 							newChar = messageArray[i].nextStep(d);
 						}
 					}
 				} else {
 					
 				}
 			} else if (letterByLetterRadio.isSelected()) {
 				if (encrypt) {
 					messageArray[currentLetterIndex].nextStep(d);
 				} else {
 					
 				}
 				highlightChar(currentLetterIndex);
 				currentLetterIndex++;
 			} else {
 				if (encrypt) {
 					m.encrypt(d);
 				} else {
 					m.decrypt(d);
 				}
 				currentLetterIndex = messageArray.length;
 			}
 
 			if (currentLetterIndex == messageArray.length) {
				setVisible(false);
				MessageFrame mf = UI.getMessageFrame();
				mf.clearMessageText();
				mf.setVisible(true);
 			}
 		}
 	}
 }
