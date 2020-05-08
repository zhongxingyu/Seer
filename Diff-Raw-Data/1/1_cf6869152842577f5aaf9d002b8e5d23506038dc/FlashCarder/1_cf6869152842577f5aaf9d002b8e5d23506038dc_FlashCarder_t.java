 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import javax.swing.*;
 
 /**
  * @author Atlee
  * @version 2013.09.22
  */
 public class FlashCarder {
 	private static final String TITLE = "FlashCarder";
 	private static final String VERSION = "Version 2013.09.22";
 
 // Look & Feel constants
 	private static final String LOOK_AND_FEEL = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
 	private static final int MARGIN = 4;
 
 // GUI fields
 	private JFrame frame;
 	private JLabel fileNameLabel;
 	private FlashCardPanel flashCardPanel;
 	private JLabel statusLabel; // todo: implement this
 
 // file fields
 	private static JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
 	private String fileName;
 
 // data fields
 	private Card card;
 	private CardSet cardSetWorking; // pull cards from here
 	private CardSet cardSetToReview; // put difficult cards here when done
 	private CardSet cardSetRetired; // put easy cards here when done
 	private boolean modified; // whether the file ought to be saved or not
 	private boolean showSideBFirst;
 
 // public methods
 	public FlashCarder() {
 		setLookAndFeel();
 		frame = makeFrame();
 		clearCards();
 		frame.setVisible(true);
 	}
 
 // private methods
 	private void clearCards() {
 		showSideBFirst = false;
 		setCard(null);
 		cardSetWorking = null;
 		cardSetToReview = null;
 		cardSetRetired = null;
 		modified = false;
 	}
 
 	private boolean getNextCard() {
 		if(cardSetWorking != null) {
 			setCard(cardSetWorking.removeNextCard());
 			return card != null;
 		}
 		return false;
 	}
 
 	/**
 	 * Puts the current Card back into the working set.
 	 * Does not mark the card as seen.
 	 * Does call Card.randomize().
 	 */
 	private void putCardInWorkingSet() {
 		if(cardSetWorking != null && card != null) {
 			Card.randomize();
 			cardSetWorking.addCard(card);
 			card = null;
 		}
 	}
 
 	/**
 	 * Puts the current Card into the To Review set.
 	 * Marks the card as seen and not easy.
 	 * Doesn't call Card.randomize().
 	 */
 	private void putCardInToReviewSet() {
 		if(cardSetToReview != null && card != null) {
 			card.setSeen(false);
 			cardSetToReview.addCard(card);
 			card = null;
 			modified = true;
 		}
 	}
 
 	/**
 	 * Puts the current Card into the Retired set.
 	 * Marks the card as seen and easy.
 	 * Doesn't call Card.randomize().
 	 */
 	private void putCardInRetiredSet() {
 		if(cardSetRetired != null && card != null) {
 			card.setSeen(true);
 			cardSetRetired.addCard(card);
 			card = null;
 			modified = true;
 		}
 	}
 
 	private void setCard(Card card) {
 		this.card = card;
 		if(card != null) {
 			flashCardPanel.set(card.getSideA(), card.getSideB(), showSideBFirst);
 		} else {
 			flashCardPanel.set("", "", showSideBFirst);
 		}
 	}
 
 	/**
 	 * If a file is open and it's changed (card stats and whatnot),
 	 * then this method will ask the user whether to save it,
 	 * and then save it.
 	 */
 	private void saveProgress() {
 		if(modified && fileName != null) {
 			if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame
 				,new JLabel("Save progress with " + fileName + " ?")
 				,"Unsaved Progress"
 				,JOptionPane.YES_NO_OPTION
 			)) {
 				saveToFile(fileName);
 			}
 		}
 	}
 
 	private boolean saveToFile(String fileName) {
 		if(cardSetWorking == null) return true; // nothing saved
 
 		CardSet setToWrite = new CardSet(cardSetWorking);
 		setToWrite.addCardSet(cardSetToReview);
 		setToWrite.addCardSet(cardSetRetired);
		if(card != null) setToWrite.addCard(card);
 
 		if(!setToWrite.writeToFile(fileName)) {
 			JOptionPane.showMessageDialog(frame
 				,new JLabel("There was a problem saving " + fileName + ", sorry.")
 				,"File / Save As problem"
 				,JOptionPane.ERROR_MESSAGE
 			);
 			return false;
 		}
 
 		modified = false;
 		setFileName(fileName);
 
 		return true;
 	}
 
 // private GUI methods
 	private JPanel makeCardPanel() {
 		return new JPanel(new BorderLayout(MARGIN, MARGIN));
 	}
 
 	private JPanel makeBottomButtonPanel() {
 		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, MARGIN, MARGIN));
 		buttonPanel.setPreferredSize(new Dimension(1, 50));
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, MARGIN, MARGIN, MARGIN));
 
 		JButton btn;
 
 		btn = new JButton("Known");
 		btn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleRetireButton(); }});
 		buttonPanel.add(btn);
 
 		btn = new JButton("Fresh");
 		btn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleWorkingButton(); }});
 		buttonPanel.add(btn);
 
 		btn = new JButton("To Review");
 		btn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleToReviewButton(); }});
 		buttonPanel.add(btn);
 
 		return buttonPanel;
 	}
 
 	private JFrame makeFrame() {
 		JFrame frame = new JFrame(TITLE);
 		frame.addWindowListener(new WindowAdapter() { public void  windowClosing(WindowEvent we) { handleQuitRequest(); }});
 
 		frame.setJMenuBar(makeMenuBar());
 
 		Container contentPane = frame.getContentPane();
 		contentPane.setLayout(new BorderLayout(MARGIN, MARGIN));
 
 		fileNameLabel = new JLabel();
 		fileNameLabel.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
 		setFileName(null);
 		contentPane.add(fileNameLabel, BorderLayout.NORTH);
 
 		flashCardPanel = new FlashCardPanel();
 		contentPane.add(flashCardPanel, BorderLayout.CENTER);
 
 		contentPane.add(makeBottomButtonPanel(), BorderLayout.SOUTH);
 
 		frame.pack();
 
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.setLocation((d.width - frame.getWidth()) / 2, (d.height - frame.getHeight()) / 2);
 
 		return frame;
 	}
 
 	private JMenuBar makeMenuBar() {
 		final int SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
 
 		JMenuBar menuBar = new JMenuBar();
 
 		JMenu menu;
 		JMenuItem item;
 
 		// File
 		menu = new JMenu("File");
 		menuBar.add(menu);
 		// File / Open
 		item = new JMenuItem("Open...");
 		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_MASK));
 		item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleFileOpen(); }});
 		menu.add(item);
 		// File / Save
 		item = new JMenuItem("Save");
 		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_MASK));
 		item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleFileSave(); }});
 		menu.add(item);
 		// File / Save As
 		item = new JMenuItem("Save As...");
 		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, SHORTCUT_MASK));
 		item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleFileSaveAs(); }});
 		menu.add(item);
 		// File / Exit
 		item = new JMenuItem("Exit");
 		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, SHORTCUT_MASK));
 		item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleQuitRequest(); }});
 		menu.add(item);
 
 		// Help
 		menu = new JMenu("Help");
 		menuBar.add(menu);
 		// Help / About
 		item = new JMenuItem("About");
 		item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { handleHelpAbout(); }});
 		menu.add(item);
 
 		return menuBar;
 	}
 
 	private void setLookAndFeel() {
 		try { UIManager.setLookAndFeel(LOOK_AND_FEEL); }
 		catch(Exception e) { /* no big deal, just a look and feel thing */ }
 		System.setProperty("awt.useSystemAAFontSettings", "on");
 		System.setProperty("swing.aatext", "true");
 	}
 
 // private GUI helper methods
 	private void setFileName(String newFileName) {
 		fileName = newFileName;
 		String prefix = modified ? "*" : "";
 		String suffix = fileName == null ? "(no file opened)" : fileName;
 		fileNameLabel.setText(prefix + suffix);
 	}
 
 // private GUI handler methods
 	private void handleQuitRequest() {
 		saveProgress();
 
 		System.exit(0);
 	}
 
 	private void handleFileOpen() {
 		saveProgress();
 
 		setFileName(null);
 
 		String chosenFileName;
 
 		if(JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(frame)) {
 			File file = fileChooser.getSelectedFile();
 			chosenFileName = file.getAbsolutePath();
 		} else return;
 
 		clearCards();
 		cardSetWorking = CardSet.createFromFile(chosenFileName);
 		if(cardSetWorking == null) {
 			JOptionPane.showMessageDialog(frame
 				,new JLabel("There was a problem opening " + chosenFileName + ", sorry.")
 				,"File / Open problem"
 				,JOptionPane.ERROR_MESSAGE
 			);
 		} else {
 			cardSetToReview = new CardSet();
 			cardSetRetired = new CardSet();
 			modified = false;
 			setFileName(chosenFileName);
 			getNextCard();
 		}
 	}
 
 	private void handleFileSave() {
 		if(fileName == null) {
 			JOptionPane.showMessageDialog(frame, "There isn't anything to save.", "File / Save", JOptionPane.INFORMATION_MESSAGE);
 		} else {
 			saveToFile(fileName);
 		}
 	}
 
 	private void handleFileSaveAs() {
 		String chosenFileName = null;
 		while(JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(frame)) {
 			File file = fileChooser.getSelectedFile();
 			chosenFileName = file.getAbsolutePath();
 			if(file.exists()) {
 				int decision = JOptionPane.showConfirmDialog(frame
 					,new JLabel("Overwrite " + chosenFileName + " ?")
 					,"Confirm File Overwrite"
 					,JOptionPane.YES_NO_CANCEL_OPTION
 				);
 				if(decision == JOptionPane.YES_OPTION) break;
 				if(decision == JOptionPane.CANCEL_OPTION) return;
 			} else break;
 		}
 
 		if(chosenFileName == null) return;
 
 		saveToFile(chosenFileName);
 	}
 
 	private void handleHelpAbout() {
 		JOptionPane.showMessageDialog(frame, TITLE + "\n" + VERSION, "About", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	private void handleRetireButton() {
 		putCardInRetiredSet();
 		getNextCard();
 	}
 
 	private void handleWorkingButton() {
 		putCardInWorkingSet();
 		getNextCard();
 	}
 
 	private void handleToReviewButton() {
 		putCardInToReviewSet();
 		getNextCard();
 	}
 }
