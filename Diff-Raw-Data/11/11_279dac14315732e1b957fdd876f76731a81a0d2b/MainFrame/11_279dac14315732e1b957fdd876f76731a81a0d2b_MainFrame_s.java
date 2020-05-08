 package com.edsdev.jconvert.presentation;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.plaf.FontUIResource;
 
 import com.edsdev.jconvert.domain.ConversionType;
 import com.edsdev.jconvert.persistence.DataLoader;
 import com.edsdev.jconvert.util.JConvertSettingsProperties;
 import com.edsdev.jconvert.util.Logger;
 import com.edsdev.jconvert.util.Messages;
 import com.edsdev.jconvert.util.ResourceManager;
 
 public class MainFrame extends JFrame implements ConversionsChangedListener {
 
     private static final long serialVersionUID = 1L;
 
     private AboutDialog aboutDlg = null;
 
     private JTabbedPane tabbedPane = null;
 
     private List data;
 
     private static final Logger log = Logger.getInstance(MainFrame.class);
 
     public static void main(String[] args) {
         initializeLocale();
         MainFrame frame = new MainFrame();
         frame.setIconImage(ResourceManager.getImage("icon.jpg"));
         frame.setVisible(true);
     }
 
     private static void initializeLocale() {
         //        Locale[] locales = Locale.getAvailableLocales();
         //        for (int i = 0;i < locales.length;i++) {
         //            log.debug(locales[i].getLanguage() + "_" + locales[i].getCountry() + " " + locales[i].getDisplayName());
         //        }
         String language = JConvertSettingsProperties.getLocaleLanguage();
         if (language != null) {
             String country = JConvertSettingsProperties.getLocaleCountry();
             String variant = JConvertSettingsProperties.getLocaleVariant();
             if (country == null) {
                 country = "";
             }
             if (variant == null) {
                 variant = "";
             }
 
             Locale.setDefault(new Locale(language, country, variant));
 
         }
     }
 
     public MainFrame() {
         super();
         try {
             // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             // do nothing
         }
         initFonts();
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         init();
     }
 
     private void init() {
 
         this.setTitle("JConvert");
         this.setJMenuBar(getMenu());
 
         setContent();
         this.addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent e) {
                 saveSettings();
             }
         });
         loadSettings();
     }
 
     private void setPos(int x, int y) {
         //verify that x and y are not off the screen
         this.setLocation(x, y);
     }
 
     private void loadSettings() {
         this.setSize(600, 400);
         try {
             String temp = JConvertSettingsProperties.getAppWidth();
             //assume that if the width is in the properties file, then everything else is. This file is written by the
             // application and thus we should be able to assume this.
             if (temp != null) {
                 this.setSize(new Integer(temp).intValue(),
                     new Integer(JConvertSettingsProperties.getAppHeight()).intValue());
                 setPos(new Integer(JConvertSettingsProperties.getAppX()).intValue(), new Integer(
                     JConvertSettingsProperties.getAppY()).intValue());
                 restorTabInfo();
             }
         } catch (Exception e) {
             //do nothing
         }
 
     }
 
     public void restorTabInfo() {
         String lastTab = JConvertSettingsProperties.getLastTab();
         for (int i = 0; i < tabbedPane.getTabCount(); i++) {
             if (tabbedPane.getTitleAt(i).equals(lastTab)) {
                 tabbedPane.setSelectedIndex(i);
                 ConversionPanel panel = (ConversionPanel) tabbedPane.getComponent(i);
                 panel.setFromUnit(JConvertSettingsProperties.getLastFrom());
                 panel.setToUnit(JConvertSettingsProperties.getLastTo());
                 panel.setFromValue(JConvertSettingsProperties.getLastValue());
             }
         }
     }
 
     public void recordCurrentTabInfo() {
         JConvertSettingsProperties.setLastTab(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
         ConversionPanel panel = (ConversionPanel) tabbedPane.getSelectedComponent();
         JConvertSettingsProperties.setLastValue(panel.getFromValue());
         JConvertSettingsProperties.setLastFrom(panel.getFromUnit());
         JConvertSettingsProperties.setLastTo(panel.getToUnit());
     }
 
     private void saveSettings() {
         JConvertSettingsProperties.setAppWidth(this.getWidth() + "");
         JConvertSettingsProperties.setAppHeight(this.getHeight() + "");
         JConvertSettingsProperties.setAppX(this.getX() + "");
         JConvertSettingsProperties.setAppY(this.getY() + "");
         JConvertSettingsProperties.setHiddenTabs("");
         recordCurrentTabInfo();
 
         JConvertSettingsProperties.setLocaleLanguage(Locale.getDefault().getLanguage());
         JConvertSettingsProperties.setLocaleCountry(Locale.getDefault().getCountry());
         JConvertSettingsProperties.setLocaleVariant(Locale.getDefault().getVariant());
         JConvertSettingsProperties.persist();
 
     }
 
     private void setContent() {
         this.getContentPane().removeAll();
 
         tabbedPane = new JTabbedPane();
         // tabbedPane.addChangeListener(new ChangeListener() {
         // public void stateChanged(ChangeEvent e) {
         // log.debug(e.getSource());
         // }
         // });
         this.getContentPane().add(tabbedPane);
         data = getData();
         Collections.sort(data);
         Iterator iter = data.iterator();
         List hiddenTabs = getHiddenTabs();
         while (iter.hasNext()) {
             ConversionTypeData ctd = (ConversionTypeData) iter.next();
             if (!hiddenTabs.contains(ctd.getTypeName())) {
                 JPanel panel = getNewPanel(ctd);
                 tabbedPane.addTab(Messages.getUnitTranslation(ctd.getTypeName()), panel);
             }
         }
     }
 
     private List getHiddenTabs() {
         ArrayList rv = new ArrayList();
         String hiddenTabs = JConvertSettingsProperties.getHiddenTabs();
         if (hiddenTabs != null) {
             StringTokenizer tokenizer = new StringTokenizer(hiddenTabs, ",");
             while (tokenizer.hasMoreTokens()) {
                 rv.add(tokenizer.nextToken());
             }
         }
         return rv;
     }
 
     private JPanel getNewPanel(ConversionTypeData ctd) {
         return new ConversionPanel(ctd);
     }
 
     private JMenuBar getMenu() {
         JMenuBar bar = new JMenuBar();
 
         JMenu fileMenu = new JMenu(Messages.getResource("fileMenu"));
         fileMenu.setMnemonic(getChar("fileMenuMnemonic"));
 
         JMenuItem fileMenuCustom = new JMenuItem(Messages.getResource("fileMenuCustom"));
         fileMenuCustom.setMnemonic(getChar("fileMenuCustomMnemonic"));
         fileMenuCustom.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 AddCustomConversionDlg dlg = new AddCustomConversionDlg(MainFrame.this);
                 dlg.addConversionsChangedListener(MainFrame.this);
                 dlg.show();
             }
         });
 
         JMenuItem fileMenuExit = new JMenuItem(Messages.getResource("fileMenuExit"));
         fileMenuExit.setMnemonic(getChar("fileMenuExitMnemonic"));
         fileMenuExit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
 
         fileMenu.add(fileMenuCustom);
         fileMenu.addSeparator();
         fileMenu.add(fileMenuExit);
 
         JMenu helpMenu = new JMenu(Messages.getResource("helpMenu"));
         helpMenu.setMnemonic(getChar("helpMenuMnemonic"));
 
         JMenuItem helpMenuAbout = new JMenuItem(Messages.getResource("helpAbout"));
         helpMenuAbout.setMnemonic(getChar("helpAboutMnemonic"));
         helpMenuAbout.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 displayAboutDialog();
             }
         });
         helpMenu.add(helpMenuAbout);
 
         bar.add(fileMenu);
         bar.add(helpMenu);
 
         return bar;
     }
 
     private char getChar(String key) {
         String theChar = Messages.getResource(key);
         return theChar.charAt(0);
     }
 
     private void displayAboutDialog() {
         if (aboutDlg == null) {
             aboutDlg = new AboutDialog(MainFrame.this);
         }
         aboutDlg.setVisible(true);
     }
 
     private List getData() {
         List rv = new ArrayList();
 
         List domainData = new DataLoader().loadData();
         Iterator iter = domainData.iterator();
         while (iter.hasNext()) {
             ConversionType type = (ConversionType) iter.next();
             ConversionTypeData ctd = new ConversionTypeData();
             // type.printDetails();
             ctd.setType(type);
             rv.add(ctd);
         }
 
         return rv;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.edsdev.jconvert.presentation.ConversionsChangedListener#conversionsUpdated()
      */
     public void conversionsUpdated() {
         saveSettings();
         setContent();
         loadSettings();
     }
 
     private void initFonts() {
        String test = Messages.getResource("Acceleration");
 
         JLabel lbl = new JLabel();
         if (lbl.getFont().canDisplayUpTo(test) == -1) {
             log.debug("Using font:" + lbl.getFont().getName());
             return;
         }
 
         // Determine which fonts support this text
         Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
         for (int j = 0; j < allfonts.length; j++) {
             if (allfonts[j].canDisplayUpTo(test) == -1) {
                 log.debug("Using font:" + allfonts[j].getFontName());
                 setUIFont(new FontUIResource(allfonts[j].getFontName(), Font.PLAIN, 10));
                 return;
             }
         }
     }
 
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
         java.util.Enumeration keys = UIManager.getDefaults().keys();
         while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             Object value = UIManager.get(key);
             if (value instanceof javax.swing.plaf.FontUIResource)
                 UIManager.put(key, f);
         }
     }
 }
