 package edu.wustl.cab2b.client.ui.mainframe;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 
 import org.jdesktop.swingx.HorizontalLayout;
 import org.jdesktop.swingx.JXFrame;
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bStandardFonts;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author hrishikesh_rajpathak
  *
  */
 class MyGlassPane extends JComponent implements ActionListener {
 	Cab2bLabel loggedInUserLabel;
 
 	String[] tabsImagesUnPressed = { "resources/images/home_tab.gif",
 			"resources/images/searchdata_tab.gif", "resources/images/experiment_tab.gif" };
 
 	String[] tabsImagesPressed = { "resources/images/home_MO_tab.gif",
 			"resources/images/searchdata_MO_tab.gif", "resources/images/experiment_MO_tab.gif" };
 
 	String[] tabs = { "Home", "Search Data", "Experiment" };
 
 	JButton[] tabButtons = new Cab2bButton[tabs.length];
 
 	public Color navigationButtonBgColorSelected = Color.WHITE;
 
 	JXPanel tabsPanel;
 
 	public Color navigationButtonBgColorUnSelected = new Color(200, 200, 220);
 
 	MainFrame mainFrame;
 
 	JXFrame frame;
 
 	private static final long serialVersionUID = 1L;
 
 	public MyGlassPane(JLabel leftLabel, JLabel middleLabel, JLabel rightLabel,
 			MainFrame mainFrame, JXFrame frame) {
 		this.frame = mainFrame;
 		this.mainFrame = mainFrame;
 		middleLabel.setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		Cab2bPanel topMiddlePanel = new Cab2bPanel();
 		topMiddlePanel.setPreferredSize(new Dimension(300, 40));
 		topMiddlePanel.setMinimumSize(new Dimension(300, 40));
 		topMiddlePanel.setMaximumSize(new Dimension(300, 40));
 		topMiddlePanel.setOpaque(false);
 
 		tabsPanel = new Cab2bPanel();
 		tabsPanel.setPreferredSize(new Dimension(300, 30));
 		tabsPanel.setMinimumSize(new Dimension(300, 30));
 		tabsPanel.setMaximumSize(new Dimension(300, 30));
 		tabsPanel.setOpaque(false);
 		tabsPanel.setLayout(new HorizontalLayout(10));
 
 		for (int i = 0; i < tabs.length; i++) {
 			String tabText = tabs[i];
 			ImageIcon icon = new ImageIcon(tabsImagesUnPressed[i]);
 			tabButtons[i] = new Cab2bButton(tabText, true, Cab2bStandardFonts.ARIAL_BOLD_12);
 			tabButtons[i].setIcon(icon);
 			tabButtons[i].setBorder(null);
 			tabButtons[i].addActionListener(this);
 			tabsPanel.add(tabButtons[i]);
 
 		}
 		ImageIcon icon=new ImageIcon(tabsImagesPressed[0]);
 		tabButtons[0].setIcon(icon);
 		gbc.gridx = 3;
 		gbc.gridy = 0;
 		gbc.gridwidth = 3;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.VERTICAL;
 		middleLabel.add(topMiddlePanel, gbc);
 
 		gbc.gridx = 3;
 		gbc.gridy = 1;
 		gbc.gridwidth = 3;
 		gbc.gridheight = 1;
 		gbc.fill = GridBagConstraints.VERTICAL;
 		middleLabel.add(tabsPanel, gbc);
 
 		rightLabel.setLayout(new GridBagLayout());
 
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridwidth = 2;
 		gbc.gridheight = 2;
 		gbc.weightx = 1.5;
 		gbc.fill = GridBagConstraints.VERTICAL;
 		rightLabel.add(new JLabel(""), gbc);
 
 		Date date = new Date();
 		loggedInUserLabel = new Cab2bLabel("<html><b>Robert Lloyd</b> <br>"
 				+ DateFormat.getDateInstance(DateFormat.LONG).format(date).toString()
 				+ "<br><br></html>");
 		Cab2bHyperlink logOutHyperLink = new Cab2bHyperlink();
 		logOutHyperLink.setClickedHyperlinkColor(Color.GRAY);
 		logOutHyperLink.setUnclickedHyperlinkColor(Color.WHITE);
 		logOutHyperLink.setText("Logout");
 		logOutHyperLink.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked on logOut Link");
 			}
 		});
 
 		Cab2bHyperlink mySettingHyperlInk = new Cab2bHyperlink();
 		mySettingHyperlInk.setClickedHyperlinkColor(Color.GRAY);
 		mySettingHyperlInk.setUnclickedHyperlinkColor(Color.WHITE);
 		mySettingHyperlInk.setText("MySettings");
 		mySettingHyperlInk.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked on My-Settings Link");
 			}
 		});
 
 		loggedInUserLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
 		loggedInUserLabel.setForeground(Color.WHITE);
 
 		Cab2bPanel linkPanel = new Cab2bPanel();
 		linkPanel.add(logOutHyperLink);
 		linkPanel.add(mySettingHyperlInk);
 		JLabel label = new JLabel("");
 		linkPanel.add("br ", label);
 		linkPanel.add("tab ", loggedInUserLabel);
 		linkPanel.setOpaque(false);
 
 		gbc.gridx = 4;
 		gbc.gridy = 0;
 		gbc.gridwidth = 2;
 		gbc.gridheight = 2;
 		gbc.weightx = 0.5;
 		gbc.fill = GridBagConstraints.VERTICAL;
 		rightLabel.add(linkPanel, gbc);
 
 		this.repaint();
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		Logger.out.info("Global Nagigation Panel Button");
 		JButton button = (JButton) e.getSource();
 
 		for (int i = 0; i < tabButtons.length; i++) {
 			tabButtons[i].setIcon(new ImageIcon(tabsImagesPressed[i]));
 			if (tabButtons[i].getActionCommand().equals(button.getActionCommand())) {
 				tabButtons[i].setBackground(navigationButtonBgColorSelected);
 				if (tabButtons[i].getActionCommand().equals("Home")) {
 					if (this.frame instanceof MainFrame) {
 						MainFrame mainframePanel = (MainFrame) this.frame;
 						mainframePanel.setHomeWelcomePanel();
 						Logger.out.info("Global Nagigation Panel Home Button");
 						tabButtons[i].setBackground(Color.BLUE);
 					}
 				} else if (tabButtons[i].getActionCommand().equals("Search Data")) {
 					tabButtons[i].setBackground(Color.BLUE);
 					GlobalNavigationPanel.mainSearchPanel = new MainSearchPanel();
 					Dimension relDimension = CommonUtils.getRelativeDimension(
 							MainFrame.mainframeScreenDimesion, 0.90f, 0.85f);
 					GlobalNavigationPanel.mainSearchPanel.setPreferredSize(relDimension);
 					GlobalNavigationPanel.mainSearchPanel.setSize(relDimension);
 
					
 
 					// Update the variable for latest screen dimension from the
 					// toolkit, this is to handle the situations were
 					// Application is started and then screen resolution is
 					// changed, but the variable stiil holds old resolution
 					// size.
 					MainFrame.mainframeScreenDimesion = Toolkit.getDefaultToolkit().getScreenSize();
 					Dimension dimension = MainFrame.mainframeScreenDimesion;
 					WindowUtilities.showInDialog(mainFrame, GlobalNavigationPanel.mainSearchPanel,
 							"Search Data", new Dimension((int) (dimension.width * 0.90),
 									(int) (dimension.height * 0.85)), true, true);
 
 					MainSearchPanel.getDataList().clear();
 					GlobalNavigationPanel.mainSearchPanel = null;
 
 				} else if (tabButtons[i].getActionCommand().equals("Experiment")) {
 
 					MainFrame mainframePanel = (MainFrame) this.frame;
 					mainframePanel.setOpenExperimentWelcomePanel();
 					tabButtons[i].setBackground(Color.BLUE);
 					return;
 				}
 			} else {
 				// tabButtons[i].setBackground(navigationButtonBgColorUnSelected);
 				tabButtons[i].setIcon(new ImageIcon(tabsImagesUnPressed[i]));
 			}
 		}
 		this.updateUI();
 		this.repaint();
 	}
 
 }
