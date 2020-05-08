 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.Iterator;
 import java.util.Observable;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.controller.WPSController;
 
 public class WPSView extends PageView {
 	
 	private JList m_wordList = new JList();
 	private DefaultListModel m_wordModel = new DefaultListModel();
 	private JTextField m_textField = new JTextField();
 
 	private WPSController m_controller;
 	static Logger log = Logger.getLogger(WPSView.class);
 
 	public WPSView(String pageName) {
 		super(pageName);
 		
 		m_panel.setLayout(new BorderLayout());
 		
 		JPanel centerPanel = new JPanel();
 		centerPanel.setBackground(backgroundColor);
 		centerPanel.setLayout(new GridBagLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 1;
 		centerPanel.add(new JLabel("Search:"));
 		
 		c.gridx = 1;
 		c.gridy = 0;
		c.gridwidth = 2;
 		centerPanel.add(m_textField, c);
 		
 		m_wordList.setModel(m_wordModel);
 		m_wordList.setVisibleRowCount(10);
 		JScrollPane scroll = new JScrollPane();
 		scroll.setSize(200, 300);
 		scroll.setViewportView(m_wordList);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		centerPanel.add(scroll, c);
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBackground(backgroundColor);
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridheight = 2;
 		c.gridwidth = 1;
 		centerPanel.add(buttonPanel, c);
 		
 		JButton b = new JButton("Add");
 		b.addActionListener(new ButtonHandler(this, "OnWordAddClicked"));
 		buttonPanel.add(b);
 		
 		b = new JButton("Delete");
 		b.addActionListener(new ButtonHandler(this, "OnWordDeleteClicked"));
 		buttonPanel.add(b);
 		
 		b = new JButton("Edit");
 		b.addActionListener(new ButtonHandler(this, "OnWordEditClicked"));
 		buttonPanel.add(b);
 		
 		m_panel.add(centerPanel, BorderLayout.CENTER);
 		
 		b = getButtonImage(AA_NAV_BUTTON_RETURN_HOME, "Return to Main Menu");
 		b.addActionListener(new ButtonHandler(this, "OnReturnHomeClicked"));
 		m_panel.add(b, BorderLayout.SOUTH);
 				
 		log.info("Initialized the " + pageName);
 	}
 	
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void activated() {
 		Iterator<String> words = m_controller.getWords();
 		
 		if (words != null) {
 			m_wordModel.removeAllElements();
 			while (words.hasNext()) {
 				m_wordModel.addElement(words.next());
 			}
 		}
 	}
 	
 	public void OnWordAddClicked() {
 		if (m_controller != null) {
 			m_controller.CreateNewWord();
 		}
 	}
 	
 	public void OnWordEditClicked() {
 		String word = (String) m_wordList.getSelectedValue();
 		if (m_controller != null && word != null) {
 			m_controller.EditWord(word);
 		}
 	}
 	
 	public void OnWordDeleteClicked() {
 		String word = (String) m_wordList.getSelectedValue();
 		if (m_controller != null && word != null) {
 			m_controller.DeleteWord(word);
 		}
 	}
 	
 	public void OnReturnHomeClicked() {
 		if (m_controller != null)
 			m_controller.GoToMainMenu();
 	}
 	
 	/**
 	 * Sets the controller associated with this view.
 	 * 
 	 * @param controller   The controller.
 	 */
 	public void SetController(WPSController controller)
 	{
 		m_controller = controller;
 	}
 
 }
