 package frontend;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rtextarea.SearchContext;
 import org.fife.ui.rtextarea.SearchEngine;
 
 /**
  * A JToolBar for finding and replacing text in an RSyntaxTextAreas.
  * 
  * @author samuelainsworth
  * 
  */
 public class FindAndReplaceToolBar extends JToolBar implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1764075378185358272L;
 
 	private final RSyntaxTextArea textArea;
 	private JTextField searchField;
 	private JCheckBox regexCheckBox;
 	private JCheckBox matchCaseCheckBox;
 
 	public FindAndReplaceToolBar(final RSyntaxTextArea textArea) {
 		this.textArea = textArea;
 
 		setFloatable(false);
 		searchField = new JTextField(30);
 		searchField.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				updateMarkAll();
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
 			}
 		});
 		add(searchField);
 		final JButton nextButton = new JButton("Find Next");
 		nextButton.setActionCommand("FindNext");
 		nextButton.addActionListener(this);
 		add(nextButton);
 		searchField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				nextButton.doClick(0);
 			}
 		});
 		searchField.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					setVisible(false);
 				}
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
 			}
 		});
 
 		JButton prevButton = new JButton("Find Previous");
 		prevButton.setActionCommand("FindPrev");
 		prevButton.addActionListener(this);
 		add(prevButton);
 
 		regexCheckBox = new JCheckBox("Regex");
 		regexCheckBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				updateMarkAll();
 			}
 		});
 		add(regexCheckBox);
 
 		matchCaseCheckBox = new JCheckBox("Match Case");
 		matchCaseCheckBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				updateMarkAll();
 			}
 		});
 		add(matchCaseCheckBox);
 	}
 
 	private void updateMarkAll() {
 		// Weird RSyntaxTextArea bug requires we unmark everything first
 		textArea.markAll("", false, false, false);
 		textArea.markAll(searchField.getText(), matchCaseCheckBox.isSelected(),
 				false, regexCheckBox.isSelected());
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// "FindNext" => search forward, "FindPrev" => search backward
 		String command = e.getActionCommand();
 		boolean forward = "FindNext".equals(command);
 
 		// Create an object defining our search parameters.
 		SearchContext context = new SearchContext();
 		String text = searchField.getText();
 		if (text.length() == 0) {
 			return;
 		}
 		context.setSearchFor(text);
 		context.setMatchCase(matchCaseCheckBox.isSelected());
 		context.setRegularExpression(regexCheckBox.isSelected());
 		context.setSearchForward(forward);
 		context.setWholeWord(false);
 
 		boolean found = SearchEngine.find(textArea, context);
 
 		if (!found) {
 			// null centers on screen
 			JOptionPane.showMessageDialog(null, "Text not found");
 		}
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
			searchField.requestFocus();
 		}
 	}
 
 }
