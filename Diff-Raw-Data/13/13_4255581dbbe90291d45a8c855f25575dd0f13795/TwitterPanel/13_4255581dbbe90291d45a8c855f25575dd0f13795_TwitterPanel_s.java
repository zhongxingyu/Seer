 package com.sessionfire.twitter.core.ui;
 
 import java.awt.BorderLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import com.sessionfire.twitter.core.Activator;
 import com.sessionfive.app.SessionfivePanel;
 
 public class TwitterPanel extends JPanel implements SessionfivePanel {
 	// private static String TWITTER_USER_DEFAULT = "<Twitter User>";
 	// private static String TWITTER_PASSWORD_DEFAULT = "<Twitter Password>";
 	private static String TWITTER_KEYWORD_DEFAULT = "<Twitter Search>";
 
 	public TwitterPanel() {
 		final Activator activator = Activator.getActivator();
 		setBorder(new EmptyBorder(15, 15, 15, 15));
 		setLayout(new BorderLayout());
 		setOpaque(false);
 		// final JTextField twitterUser = new JTextField(TWITTER_USER_DEFAULT);
 		// JTextField twitterPass = new JTextField(TWITTER_PASSWORD_DEFAULT);
 		final JTextField twitterkeyword = new JTextField(TWITTER_KEYWORD_DEFAULT);
 		final JCheckBox twitterEnabled = new JCheckBox("");
 		twitterEnabled.setEnabled(false);
 
 		twitterEnabled.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if (twitterEnabled.isSelected()) {
 					activator.startNewTwitterPoll(twitterkeyword.getText());
 					twitterkeyword.setEditable(false);
 				} else {
 					activator.stopTwitterPoll();
 					twitterkeyword.setEditable(true);
 				}
 				
 			}
 		});
 		
 		twitterkeyword.addFocusListener(new FocusListener() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (twitterkeyword.getText().isEmpty()) {
 					twitterkeyword.setText(TWITTER_KEYWORD_DEFAULT);
 				}
 			}
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				twitterkeyword.selectAll();
 			}
 		});
 
 		twitterkeyword.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
 					twitterEnabled.setSelected(true);
 				}
 			}
 		});
 		twitterkeyword.getDocument().addDocumentListener(new DocumentListener() {
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				boolean editable = !twitterkeyword.getText().isEmpty()
 						&& !twitterkeyword.getText().equals(TWITTER_KEYWORD_DEFAULT);
 				twitterEnabled.setEnabled(editable);
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				boolean editable = !twitterkeyword.getText().isEmpty()
 						&& !twitterkeyword.getText().equals(TWITTER_KEYWORD_DEFAULT);
 				twitterEnabled.setEnabled(editable);
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				boolean editable = !twitterkeyword.getText().isEmpty()
 						&& !twitterkeyword.getText().equals(TWITTER_KEYWORD_DEFAULT);
 				twitterEnabled.setEnabled(editable);
 			}
 		});
 		
 		final JSlider sizeSlider = new JSlider(SwingConstants.HORIZONTAL, 100, 200, 100);
 		sizeSlider.addChangeListener( new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				activator.getTexShape2().setSize(sizeSlider.getValue());
 			}
 		});
 
 
 		// add(twitterUser);
 		// add(twitterPass);
 		add(twitterkeyword, BorderLayout.CENTER);
 		add(twitterEnabled, BorderLayout.EAST);
 		add(sizeSlider, BorderLayout.SOUTH);
 	}
 
 	private static final long serialVersionUID = 1L;
 }
