 package edu.bu.cs673.AwesomeAlphabet.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.Iterator;
 import java.util.Observable;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.controller.ButtonHandler;
 import edu.bu.cs673.AwesomeAlphabet.controller.ThemeController;
 import edu.bu.cs673.AwesomeAlphabet.value.ThemeViewData;
 
 public class ThemePageView extends PageView implements IThemeControllerView {
 
 	static Logger log = Logger.getLogger(ThemePageView.class);
 	private ThemeController m_controller;
 	
 	private JTable m_themeTable = new JTable();
 	private ThemeTableModel m_themeModel = new ThemeTableModel();
 	private JTextField m_textField = new JTextField();
 	
 	private JButton m_modifyButton = new JButton("Modify");
 	private JButton m_deleteButton = new JButton("Delete");
 	
 	private JLabel m_currentTheme = new JLabel("**");
 	
 	public ThemePageView(String pageName) {
 		super(pageName);
 		
 		m_panel.setLayout(new BorderLayout());
 		
 		JPanel parentPanel = new JPanel();
 		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));
 		parentPanel.add(Box.createVerticalGlue());
 
 		JPanel centerPanel = new JPanel();
 		centerPanel.setBackground(backgroundColor);
 		centerPanel.setLayout(new GridBagLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		
 		JPanel p = new JPanel();
 		p.setBackground(headingBackground);
 		JLabel l = new JLabel("Theme Settings");
 		l.setFont(headingFont);
 		l.setHorizontalAlignment(SwingConstants.CENTER);
 		p.add(l);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 1;
 		c.gridwidth = 2;
 		centerPanel.add(p, c);
 
 		c.gridy = 1;
 		centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL), c);
 		
 		c.gridy = 2;
 		JPanel currentThemePanel = new JPanel(new FlowLayout());
 		currentThemePanel.setBackground(backgroundColor);
 		l = new JLabel("Current Theme:  ");
 		l.setFont(infoFont);
 		m_currentTheme.setFont(infoFont);
 		currentThemePanel.add(l);
 		currentThemePanel.add(m_currentTheme);
 		centerPanel.add(currentThemePanel, c);
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.PAGE_END;
 		centerPanel.add(m_textField, c);
 
 		JButton b = new JButton("Add");
 		b.addActionListener(new ButtonHandler(this, "OnThemeAddClicked"));
 		c.gridx = 1;
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.CENTER;
 		centerPanel.add(b, c);
 		
 		m_themeTable.setModel(m_themeModel);
 		m_themeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		ListSelectionModel lsm = m_themeTable.getSelectionModel();
 		lsm.addListSelectionListener(new ListSelectionListener() {
 
 			@Override
 			public void valueChanged(ListSelectionEvent lse) {
 				if (lse.getValueIsAdjusting())
 					return;
 				boolean editable = m_themeModel.rowIsEditable(m_themeTable.getSelectedRow());
 				m_modifyButton.setEnabled(editable);
 				m_deleteButton.setEnabled(editable);
 			}
 		});
 		
 		JScrollPane sp = new JScrollPane(m_themeTable);
 		sp.setBackground(backgroundColor);
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 4;
 		c.weightx = 0.5;
 		c.weighty = 0.5;
 		c.anchor = GridBagConstraints.PAGE_START;
 		centerPanel.add(sp, c);
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBackground(backgroundColor);
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 4;
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 		centerPanel.add(buttonPanel, c);
 		
 		m_modifyButton.addActionListener(new ButtonHandler(this, "OnThemeModifyClicked"));
 		buttonPanel.add(m_modifyButton);
 		
 		m_deleteButton.addActionListener(new ButtonHandler(this, "OnThemeDeleteClicked"));
 		buttonPanel.add(m_deleteButton);
 		
 		b = new JButton("Set As Current Theme");
 		b.addActionListener(new ButtonHandler(this, "OnSetThemeClicked"));
 		buttonPanel.add(b);
 		
 		b = new JButton("Use No Theme");
 		b.addActionListener(new ButtonHandler(this, "OnClearCurrentTheme"));
 		buttonPanel.add(b);
 		
 		parentPanel.add(centerPanel);
 		parentPanel.add(Box.createVerticalGlue());
 		m_panel.add(parentPanel, BorderLayout.CENTER);
 		
 		b = getButtonImage(AA_NAV_BUTTON_RETURN_HOME, "Return to Options Menu");
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
 		Iterator<ThemeViewData> i = m_controller.getThemeNamesIterator();
 		
 		m_currentTheme.setText(m_controller.getCurrentTheme());
 		m_themeModel.removeAllElements();
 		while (i.hasNext()) {
 			m_themeModel.addElement(i.next());
 		}
 		m_themeModel.sort();
 		m_themeModel.fireTableDataChanged();
 	}
 	
 	public void OnReturnHomeClick() {
 		if (m_controller != null)
 			m_controller.GoToOptionsPage();
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
 		String oldThemeName = m_themeModel.getThemeName(m_themeTable.getSelectedRow());
 		if (m_controller != null && newThemeName.length() > 0) {
 			m_controller.changeThemeName(oldThemeName, newThemeName);
 		}
 	}
 	
 	public void OnThemeDeleteClicked() {
 		String themeName = m_themeModel.getThemeName(m_themeTable.getSelectedRow());
 		if (m_controller != null) {
 			m_controller.deleteTheme(themeName);
 		}
 	}
 	
 	public void OnSetThemeClicked() {
 		String themeName = m_themeModel.getThemeName(m_themeTable.getSelectedRow());
		if (themeName == null)
			JOptionPane.showMessageDialog(m_panel, "Please select a theme.");
		else if (m_controller != null) {
 			m_controller.setCurrentTheme(themeName);
 		}
 	}
 
 	public void OnClearCurrentTheme() {
 		if (m_controller != null)
 			m_controller.setCurrentTheme(null);
 	}
 	
 	public void SetController(ThemeController themeController) {
 		m_controller = themeController;
 	}
 
 }
