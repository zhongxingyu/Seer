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
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 
 import org.jdesktop.swingx.HorizontalLayout;
 import org.jdesktop.swingx.JXFrame;
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bStandardFonts;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This is a top level navigation panel, which is placed at the
  * top of the <code>MainFrame</code>.
  * 
  * @author chetan_bh
  */
 public class GlobalNavigationPanel extends Cab2bPanel implements ActionListener {
     /**
      * Background color of the panel. 
      */
     Color bgColor = new Color(58, 95, 205); //new Color(120,120,220);
 
     //58;95;205
     /**
      * Foreground color of the panel.
      */
     Color fgColor = new Color(255, 255, 255); // Color.WHITE
 
     /**
      * Label for the title of the application.
      */
     Cab2bLabel titleLabel;
 
     /** Panel holding the title label. */
     JXPanel titlePanel;
 
     JXPanel middlePanel;
 
     JXPanel topMiddlePanel;
 
     JXPanel tabsPanel;
 
     MainFrame mainFrame;
 
     /**
      * Label to show details of the logged in user to the application. 
      */
     Cab2bLabel loggedInUserLabel;
 
     /**
      * Button text to show in the navigation panel.
      */
     String[] tabs = { "Home", "Search Data", "Experiment" };
 
     /**
      * Button text to show in the navigation panel.
      */
     String[] tabsImages = { "resources/images/home_tab.gif", "resources/images/searchdata_tab.gif", "resources/images/experiment_tab.gif" };
 
     /**
      * Buttons to show in the navigation panel.
      */
     JButton[] tabButtons = new Cab2bButton[tabs.length];
 
     public Color navigationButtonBgColorSelected = new Color(221, 221, 221);
 
     public Color navigationButtonBgColorUnSelected = new Color(168, 168, 168);
 
     /**
      * Top level panel to hold all sub panels.
      */
     JXPanel parentPanel;
 
     /**
      * <code>MainFrame</code> reference.
      */
     JXFrame frame;
 
     public static MainSearchPanel mainSearchPanel = null;
 
     public GlobalNavigationPanel(JXFrame frame, MainFrame mainFrame) {
         this.mainFrame = mainFrame;
         this.frame = frame;
         this.setBackground(bgColor);
         this.setLayout(new RiverLayout(0, 0));
         this.setPreferredSize(new Dimension(1024, 75));
         this.setMaximumSize(new Dimension(1024, 75));
         this.setMinimumSize(new Dimension(1024, 75));
 
         initGUIWithGB();
 
     }
 
     private void initGUIWithGB() {
         /* to this panel add top panel and bottom panel */
 
         Cab2bPanel topPanel = new Cab2bPanel();
         topPanel.setBackground(bgColor);
         topPanel.setPreferredSize(new Dimension(1024, 70));
         topPanel.setMinimumSize(new Dimension(1024, 70));
         topPanel.setMaximumSize(new Dimension(1024, 70));
 
         GridBagConstraints gbc = new GridBagConstraints();
         topPanel.setLayout(new GridBagLayout());
 
         // 1 component
         Icon logoIcon = new ImageIcon("resources/images/b2b_logo.gif");
         titleLabel = new Cab2bLabel(logoIcon);
 
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 2;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(titleLabel, gbc);
         //this.add(titleLabel);
 
         // 2 component
         JXPanel extraSpaceFillerPanel1 = new Cab2bPanel();
         extraSpaceFillerPanel1.setBackground(bgColor);
         extraSpaceFillerPanel1.setPreferredSize(new Dimension(60, 70));
         extraSpaceFillerPanel1.setMinimumSize(new Dimension(60, 70));
         extraSpaceFillerPanel1.setMaximumSize(new Dimension(60, 70));
 
         gbc.gridx = 2;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 2;
         gbc.weightx = 0.50;
         gbc.fill = GridBagConstraints.BOTH;
         topPanel.add(extraSpaceFillerPanel1, gbc);
         //this.add("hfill vfill", extraSpaceFillerPanel1);
 
         // 3rd component
         topMiddlePanel = new Cab2bPanel();
         topMiddlePanel.setPreferredSize(new Dimension(300, 37));
         topMiddlePanel.setMinimumSize(new Dimension(300, 37));
         topMiddlePanel.setMaximumSize(new Dimension(300, 37));
         topMiddlePanel.setBackground(bgColor);
 
         // 4th component
         tabsPanel = new Cab2bPanel();
         tabsPanel.setPreferredSize(new Dimension(300, 33));
         tabsPanel.setMinimumSize(new Dimension(300, 33));
         tabsPanel.setMaximumSize(new Dimension(300, 33));
         tabsPanel.setBackground(bgColor);
         tabsPanel.setLayout(new HorizontalLayout(10));
 
         for (int i = 0; i < tabs.length; i++) {
             String tabText = tabs[i];
             String tabImageFileLocation = tabsImages[i];
             ImageIcon imageIcon = new ImageIcon(tabImageFileLocation);
             //tabButtons[i] = new Cab2bButton(imageIcon);
             //tabButtons[i].setActionCommand(tabText);
             tabButtons[i] = new Cab2bButton(tabText, true, Cab2bStandardFonts.ARIAL_BOLD_12);
             tabButtons[i].setBorder(null);
             if (i != 0) {
                 tabButtons[i].setBackground(navigationButtonBgColorUnSelected); //new Color(168, 168, 168)
             } else {
                 tabButtons[i].setBackground(navigationButtonBgColorSelected); //new Color(220,220,240)
             }
             tabButtons[i].addActionListener(this);
             tabsPanel.add(tabButtons[i]);
         }
 
         gbc.gridx = 3;
         gbc.gridy = 0;
         gbc.gridwidth = 3;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(topMiddlePanel, gbc);
 
         gbc.gridx = 3;
         gbc.gridy = 1;
         gbc.gridwidth = 3;
         gbc.gridheight = 1;
         gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(tabsPanel, gbc);
 
         // 5th component
         JXPanel extraSpaceFillerPanel2 = new Cab2bPanel();
         extraSpaceFillerPanel2.setBackground(bgColor);
         gbc.gridx = 7;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 0.50;
         topPanel.add(extraSpaceFillerPanel2, gbc);
         //this.add("hfill vfill", extraSpaceFillerPanel2);
 
         // 6th component, which is a cab2b banner image.
         Icon bannerIcon = new ImageIcon("resources/images/b2b_banner.gif");
         Cab2bLabel bannerLabel = new Cab2bLabel(bannerIcon);
         gbc.gridx = 8;
         gbc.gridy = 0;
         gbc.gridwidth = 2;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(bannerLabel, gbc);
 
         Date date = new Date();
        loggedInUserLabel = new Cab2bLabel("<html>Robert Lloyd <br>"
                 + DateFormat.getDateInstance(DateFormat.LONG).format(date).toString() + "<br><br></html>");
         Cab2bHyperlink logOutHyperLink = new Cab2bHyperlink(Color.GRAY, Color.WHITE);
         logOutHyperLink.setText("Logout");
         logOutHyperLink.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Logger.out.info("Clicked on logOut Link");
             }
         });
 
         Cab2bHyperlink mySettingHyperlInk = new Cab2bHyperlink(Color.GRAY, Color.WHITE);
         mySettingHyperlInk.setText("MySettings");
         mySettingHyperlInk.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Logger.out.info("Clicked on My-Settings Link");
             }
         });
 
         loggedInUserLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
         loggedInUserLabel.setForeground(fgColor);
         loggedInUserLabel.setBackground(bgColor);
 
         Cab2bPanel linkPanel = new Cab2bPanel();
         linkPanel.add(logOutHyperLink);
         linkPanel.add(mySettingHyperlInk);
         linkPanel.setOpaque(false);
 
         Dimension prefSize = loggedInUserLabel.getPreferredSize();
         loggedInUserLabel.setMaximumSize(prefSize);
         loggedInUserLabel.setMinimumSize(prefSize);
 
         gbc.gridx = 10;
         gbc.gridy = 0;
         gbc.gridwidth = 2;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(loggedInUserLabel, gbc);
 
         //this.add("hfill", loggedInUserLabel);		
         gbc.gridx = 10;
         gbc.gridy = 1;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
 
         //gbc.fill = GridBagConstraints.VERTICAL;
         topPanel.add(linkPanel, gbc);
 
         /*	gbc.gridx = 11;
          gbc.gridy = 1;
          gbc.gridwidth = 1;
          gbc.gridheight = 1;		
          gbc.fill = GridBagConstraints.VERTICAL;
          topPanel.add(mySettingHyperlInk,gbc);*/
 
         JXPanel bottomBorderPanel = new Cab2bPanel();
         bottomBorderPanel.setPreferredSize(new Dimension(1024, 5));
         bottomBorderPanel.setMaximumSize(new Dimension(1024, 5));
         bottomBorderPanel.setMinimumSize(new Dimension(1024, 5));
         bottomBorderPanel.setBackground(navigationButtonBgColorSelected);
 
         this.add("hfill", topPanel);
 
         this.add("br hfill", bottomBorderPanel);
 
     }
 
     /**
      * Global navigation button's action listener.
      */
     public void actionPerformed(ActionEvent e) {
         Logger.out.info("Global Nagigation Panel Button");
         JButton button = (JButton) e.getSource();
         for (int i = 0; i < tabButtons.length; i++) {
             if (tabButtons[i].getActionCommand().equals(button.getActionCommand())) {
                 tabButtons[i].setBackground(navigationButtonBgColorSelected);
                 if (tabButtons[i].getActionCommand().equals("Home")) {
                     if (this.frame instanceof MainFrame) {
                         MainFrame mainframePanel = (MainFrame) this.frame;
                         mainframePanel.setHomeWelcomePanel();
                         Logger.out.info("Global Nagigation Panel Home Button");
                     }
                 } else if (tabButtons[i].getActionCommand().equals("Search Data")) {
                     GlobalNavigationPanel.mainSearchPanel = new MainSearchPanel();
                     Dimension relDimension = CommonUtils.getRelativeDimension(MainFrame.mainframeScreenDimesion,
                                                                               0.90f, 0.85f);
                     GlobalNavigationPanel.mainSearchPanel.setPreferredSize(relDimension);
                     GlobalNavigationPanel.mainSearchPanel.setSize(relDimension);
 
                     edu.wustl.cab2b.client.ui.util.CommonUtils.FrameReference = mainFrame;
 
                     // Update the variable for latest screen dimension from the toolkit, this is to handle the situations were
                     // Application is started and then screen resolution is changed, but the variable stiil holds old resolution size.
                     MainFrame.mainframeScreenDimesion = Toolkit.getDefaultToolkit().getScreenSize();
                     Dimension dimension = MainFrame.mainframeScreenDimesion;
                     WindowUtilities.showInDialog(mainFrame, GlobalNavigationPanel.mainSearchPanel, "Search Data", new Dimension((int)(dimension.width * 0.90), (int)(dimension.height * 0.85)), true, true);
 
                     GlobalNavigationPanel.mainSearchPanel.getDataList().clear();
                     GlobalNavigationPanel.mainSearchPanel = null;
                 } else if (tabButtons[i].getActionCommand().equals("Experiment")) {
 
                     MainFrame mainframePanel = (MainFrame) this.frame;
                     mainframePanel.setOpenExperimentWelcomePanel();
                     return;
                 }
             } else {
                 tabButtons[i].setBackground(navigationButtonBgColorUnSelected);
             }
         }
         this.updateUI();
         this.repaint();
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Logger.configure("");
         GlobalNavigationPanel globalNavigationPanel = new GlobalNavigationPanel(new JXFrame(), new MainFrame());
 
         //JFrame frame = WindowUtilities.openInJFrame(globalNavigationPanel, 1024, 70, "Global Navigation Panel");
 
         JXFrame frame = WindowUtilities.showInFrame(globalNavigationPanel, "Global Navigation Panel");
         //frame.getRootPaneExt().removeAll();
         //frame.setResizable(false);
         //frame.setBackground(new Color(120,120,220));
     }
 
 }
