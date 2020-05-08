 package edu.ncsu.csc216.solitaire.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.smartcardio.CardPermission;
 import javax.swing.*;
 
 import edu.ncsu.csc216.solitaire.model.Deck;
 import edu.ncsu.csc216.solitaire.model.DeckLinkedList;
 import edu.ncsu.csc216.solitaire.model.Message;
 
 public class CardIcons extends JFrame implements ActionListener {
 	
 	/**
 	 * The icons of the deck
 	 */
 	private static ImageIcon[] icons;
 	private static CardIcons iconsObject;
 	private JPanel[] cardPanels;
 	private JPanel cardPanel;
 	private JRadioButton stepByStepRadio;
 	private JRadioButton letterByLetterRadio;
 	private JRadioButton wholeMessageRadio;
 	private JLabel[] messageLabels;
 	private JLabel answerLabel;
 	private int currentLetterIndex;
 	private JButton runButton;
 	private static final int FULL_COLOR = 255;
 	private static final int HALF_COLOR = 128;
 	private static final int NUM_PANELS = 4;
 	
 	public static CardIcons init() {
 		if (iconsObject == null) {
 			iconsObject = new CardIcons();
 		}		
 		return iconsObject;			
 	}
 	
 	public CardIcons() {
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
 		cardPanels = new JPanel[NUM_PANELS];
 		
 		for (int i = 0; i < cardPanels.length; i++) {
 			cardPanels[i] = new JPanel();
 			cardPanels[i].setLayout(new FlowLayout());
 			cardPanel.add(cardPanels[i]);
 		}
 		
 		initIcons();
 		ImageIcon icons[] = icons();
 		
 		for (int i = 0; i < cardPanels.length; i++) {
 			for (int j = i * icons.length / NUM_PANELS; j < (i + 1) * icons.length / NUM_PANELS; j++) {
 				cardPanels[i].add(new JLabel(icons[j]));
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
 		answerLabel = new JLabel();
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
 		if (index == -1) {
 			for (int i = 0; i < messageLabels.length; i++){
 				messageLabels[i].setBackground(new Color(0, 0, 0, 0));
 			}
 		} else if (index > 0) {
 			messageLabels[index - 1].setBackground(null);
 			messageLabels[index].setBackground(new Color(0, HALF_COLOR / 2, FULL_COLOR, HALF_COLOR / 2));
 		} else {
 			messageLabels[index].setBackground(new Color(0, HALF_COLOR / 2, FULL_COLOR, HALF_COLOR / 2));
 		}
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getActionCommand().equals("Process Another Message")) {
 			UI.setMessageFrame(new MessageFrame());
 			UI.getMessageFrame().setVisible(true);
 			dispose();
 		} else if (ae.getActionCommand().equals("Letter By Letter")) {
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
 			for (int i = 0; i < messageString.length(); i++) {
 				messageArray[i] = new Message(String.valueOf(messageString.charAt(i)));
 			}
 
 			Deck d = UI.getDeck();
 			
 			if (stepByStepRadio.isSelected()) {
 				wholeMessageRadio.setEnabled(false);
 				letterByLetterRadio.setEnabled(false);
 				if (encrypt) {
 					highlightChar(currentLetterIndex);
 					d.nextStep();
 				} else {
 					
 				}
 				if (d.getCurrentStep() == 5) {
 					currentLetterIndex++;
 					d.resetCurrentStep();
 				}
 			} else if (letterByLetterRadio.isSelected()) {
 				wholeMessageRadio.setEnabled(false);
 				stepByStepRadio.setEnabled(false);
 				if (encrypt) {
 					for (int i = 0; i < Deck.NUM_STEPS; i++) {
 						highlightChar(currentLetterIndex);
 						d.nextStep();
 						if (d.getCurrentStep() == 5) {
 							currentLetterIndex++;
 							d.resetCurrentStep();
 						}
 					}
 				} else {
 					
 				}
 			} else {
 				stepByStepRadio.setEnabled(false);
 				letterByLetterRadio.setEnabled(false);
 				if (encrypt) {
 					m.encrypt(d);
 				} else {
 					m.decrypt(d);
 				}
 				currentLetterIndex = messageArray.length;
 			}
 
 			if (currentLetterIndex == messageArray.length) {
 				runButton.setText("Process Another Message");
 			}
 		}
 	}
 
 	
 	/**
 	 * Parses the images and populates the
 	 * CardIcons variable
 	 */
 	public void initIcons() {
 		Deck d = UI.getDeck();
 		icons = new ImageIcon[d.deck().size()];
 		displayDeck(d);
 	}
 	
 	public ImageIcon[] icons() {
 		return icons;
 	}
 	
 	/**
 	 * Displays the deck of CardIcons
 	 * @param d The deck to display
 	 */
 	public void displayDeck(Deck d) {
 		DeckLinkedList cards = d.deck();
 		for (int i = 0; i < icons.length; i++) {
 			icons[i] = new ImageIcon("cards/" + cards.get(i) + ".gif");
 		}
 		repaint();
 	}
 	
 	public static void reset() {
 		iconsObject = new CardIcons();
 	}
 }
 
 
