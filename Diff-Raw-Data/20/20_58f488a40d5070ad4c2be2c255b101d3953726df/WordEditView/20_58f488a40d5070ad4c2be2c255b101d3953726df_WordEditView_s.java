 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Iterator;
 import java.util.Observable;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.controller.WordEditController;
 import edu.bu.cs673.AwesomeAlphabet.model.WordPictureSound;
 
 public class WordEditView extends PageView {
 	
 	private WordEditController m_controller = null;
 	private JTextField m_wordField = new JTextField();
 	private JComboBox m_themeChoice = new JComboBox(new DefaultComboBoxModel());
 	private JTextField m_imageFileField = new JTextField();
 	private JTextField m_soundFileField = new JTextField();
 	private static final JFileChooser chooser = new JFileChooser();
 	private String[] letters = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
 			"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
 	};
	private JComboBox m_letterChoice = new JComboBox(letters);	
 	
 	public WordEditView(String pageName) {
 		super(pageName);
 		
 		m_imageFileField.setEditable(false);
 		m_soundFileField.setEditable(false);
 		
 		JButton selectImage = new JButton("Select");
 		selectImage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				int result = chooser.showOpenDialog(WordEditView.this.m_panel);
 				if (result == JFileChooser.APPROVE_OPTION) {
 					File f = chooser.getSelectedFile();
 					m_imageFileField.setText(f.getAbsolutePath());
 				}
 			}
 		});
 
 		JButton selectSound = new JButton("Select");
 		selectSound.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				int result = chooser.showOpenDialog(WordEditView.this.m_panel);
 				if (result == JFileChooser.APPROVE_OPTION) {
 					File f = chooser.getSelectedFile();
 					m_soundFileField.setText(f.getAbsolutePath());
 				}
 			}
 		});
 
 		m_themeChoice.addItem("--none--");
 		
 		m_panel.setLayout(new BorderLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		JPanel centerPanel = new JPanel(new GridBagLayout());
 		centerPanel.setBackground(backgroundColor);
 
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 1;
 		c.gridwidth = 1;
 		centerPanel.add(new JLabel("Word:"), c);
 		
 		c.gridx = 1;
 		c.gridy = 0;
 		centerPanel.add(m_wordField, c);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		centerPanel.add(new JLabel("Theme:"), c);
 		
 		c.gridx = 1;
 		c.gridy = 1;
 		centerPanel.add(m_themeChoice, c);
 		
 		c.gridx = 0;
 		c.gridy = 2;
 		centerPanel.add(new JLabel("Associated Letter:"), c);
 		
 		c.gridx = 1;
 		c.gridy = 2;
 		centerPanel.add(m_letterChoice, c);
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		centerPanel.add(new JLabel("Image file:"), c);
 		
 		c.gridx = 1;
 		c.gridy = 3;
 		centerPanel.add(m_imageFileField, c);
 		
 		c.gridx = 2;
 		c.gridy = 3;
 		centerPanel.add(selectImage, c);
 		
 		c.gridx = 0;
 		c.gridy = 4;
 		centerPanel.add(new JLabel("Sound file:"), c);
 		
 		c.gridx = 1;
 		c.gridy = 4;
 		centerPanel.add(m_soundFileField, c);
 		
 		c.gridx = 2;
 		c.gridy = 4;
 		centerPanel.add(selectSound, c);
 		
 		m_panel.add(centerPanel, BorderLayout.CENTER);
 		
 		
 		JPanel p = new JPanel(new FlowLayout());
 		p.setBackground(backgroundColor);
 
 		JButton b = new JButton("Cancel");
 		b.addActionListener(new ButtonHandler(this, "OnCancelClicked"));
 		p.add(b);
 		
 		b = new JButton("Save");
 		b.addActionListener(new ButtonHandler(this, "OnSaveClicked"));
 		p.add(b);
 		
 		m_panel.add(p, BorderLayout.SOUTH);
 		
 		
 	}
 	
 	public void OnCancelClicked() {
 		if (m_controller != null)
 			m_controller.CancelEditWord();
 	}
 	
 	public void OnSaveClicked() {
 		if (m_controller != null) {
 			WordPictureSound wps = m_controller.getCurrentWordEditing();
 			String character = (String) m_letterChoice.getSelectedItem();
 			char letter_c = character.charAt(0);
 			if (wps == null) {
 				/* It is new word being added */
 				m_controller.SaveNewWord(m_wordField.getText(), letter_c, m_imageFileField.getText(),
 					m_soundFileField.getText(), m_themeChoice.getSelectedItem().toString());
 			} else {
 				m_controller.SaveEditWord(m_wordField.getText(), m_imageFileField.getText(),
 						m_soundFileField.getText(), m_themeChoice.getSelectedItem().toString());
 			}
 		}
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void activated() {
 		if (m_controller != null) {
 			Iterator<String> themes = m_controller.getThemeNamesIterator();
 			WordPictureSound wps = m_controller.getCurrentWordEditing();
 			String absSoundFilePath, absImageFilePath;
 			
 			m_themeChoice.removeAllItems();
 			m_themeChoice.addItem("--none--");
 			while (themes.hasNext()) {
 				String s = themes.next();
 				m_themeChoice.addItem(s);
 			}
 			
 			if (wps != null) {
 				m_wordField.setText(wps.GetWordString());
				m_themeChoice.setName(wps.getTheme().getThemeName());
 				m_letterChoice.setSelectedIndex(m_controller.getLetterIndex(wps.getWordLetter()));
 				absImageFilePath = m_controller.getAbsImageFilePath(wps.GetWordString());
 				absSoundFilePath = m_controller.getAbsSoundFilePath(wps.GetWordString());
 				m_imageFileField.setText(absImageFilePath);
 				m_soundFileField.setText(absSoundFilePath);
 			}
 			
 		}
 	}
 
 	
 	/**
 	 * Sets the controller associated with this view.
 	 * 
 	 * @param controller   The controller.
 	 */
 	public void SetController(WordEditController controller)
 	{
 		m_controller = controller;
 	}
 }
