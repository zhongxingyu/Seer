 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.Iterator;
 import java.util.Observable;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.controller.ThemeController;
 
 public class ThemePageView extends PageView implements IThemeControllerView {
 
 	static Logger log = Logger.getLogger(ThemePageView.class);
 	private ThemeController m_controller;
 	
 	private JList m_themeList = new JList();
 	private DefaultListModel m_themeModel = new DefaultListModel();
 	private JTextField m_textField = new JTextField();
 	
 	public ThemePageView(String pageName) {
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
 		centerPanel.add(m_textField, c);
 		
 		m_themeList.setModel(m_themeModel);
 		m_themeList.setVisibleRowCount(10);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		centerPanel.add(m_themeList, c);
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBackground(backgroundColor);
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridheight = 2;
 		centerPanel.add(buttonPanel, c);
 		
 		JButton b = new JButton("Add");
 		b.addActionListener(new ButtonHandler(this, "OnThemeAddClicked"));
 		buttonPanel.add(b);
 		
 		b = new JButton("Modfiy");
 		b.addActionListener(new ButtonHandler(this, "OnThemeModifyClicked"));
 		buttonPanel.add(b);
 		
 		b = new JButton("Delete");
 		b.addActionListener(new ButtonHandler(this, "OnThemeDeleteClicked"));
 		buttonPanel.add(b);
 		
 		m_panel.add(centerPanel, BorderLayout.CENTER);
 		
 		b = getButtonImage(AA_NAV_BUTTON_RETURN_HOME, "Return to Main Menu");
 		b.addActionListener(new ButtonHandler(this, "OnReturnHomeClick"));
 		m_panel.add(b, BorderLayout.SOUTH);
 				
 		log.info("Initialized the " + pageName);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o != null) {
			repopulateThemes();
 		}
 	}
 
 	@Override
 	public void activated() {
 		log.info("Activated " + super.getPageName());
		repopulateThemes();
	}

	private void repopulateThemes() {
		Iterator<String> i = m_controller.getThemeNamesIterator();
		
		m_themeModel.removeAllElements();
		while (i.hasNext()) {
			m_themeModel.addElement(i.next());
		}
 	}
 	
 	public void OnReturnHomeClick() {
 		if (m_controller != null)
 			m_controller.GoToTitlePage();
 	}
 	
 	public void OnThemeAddClicked() {
 		String newTheme = m_textField.getText();
 		if (m_controller != null && newTheme.length() > 0) {
 			if (!m_controller.addTheme(newTheme)) {
 				JOptionPane.showMessageDialog(m_panel, "Theme already exists.");
 			}
 			m_textField.setText("");
 		}
 	}
 
 	public void OnThemeModifyClicked() {
 		String newThemeName = m_textField.getText();
 		String oldThemeName = (String) m_themeList.getSelectedValue();
 		if (m_controller != null && newThemeName.length() > 0) {
 			m_controller.changeThemeName(oldThemeName, newThemeName);
 		}
 	}
 	
 	public void OnThemeDeleteClicked() {
 		String themeName = (String) m_themeList.getSelectedValue();
 		if (m_controller != null) {
 			m_controller.deleteTheme(themeName);
 		}
 	}
 	
 	public void SetController(ThemeController themeController) {
 		m_controller = themeController;
 	}
 
 }
