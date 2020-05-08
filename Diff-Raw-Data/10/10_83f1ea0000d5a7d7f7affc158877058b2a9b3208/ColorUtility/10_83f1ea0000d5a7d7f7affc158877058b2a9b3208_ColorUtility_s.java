 package leocarbon.cu;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.util.Locale;
 import java.util.Random;
 import java.util.ResourceBundle;
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JColorChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.colorchooser.AbstractColorChooserPanel;
 import leocarbon.cu.modules.AverageColor;
 import leocarbon.cu.modules.DigitalEyedropper;
 import leocarbon.cu.modules.InvertColor;
 import leocarbon.cu.modules.RandomColor;
 import leocarbon.cu.modules.ScrollColor;
 import leocarbon.cu.modules.ToneColor;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.simplericity.macify.eawt.Application;
 import org.simplericity.macify.eawt.ApplicationEvent;
 import org.simplericity.macify.eawt.ApplicationListener;
 import org.simplericity.macify.eawt.DefaultApplication;
 
 public class ColorUtility extends JFrame {
     public static ColorUtility CU;
     private static Application MacOSCU;
     
     public static Dimension EvD, CUD;
     public static Point EvP;
     
     public static boolean OptionsLAFChange = false;
     
     public static About A;
     public static Options O;
     public static boolean dologging = false;
     
     public static final ResourceBundle RB = ResourceBundle.getBundle("leocarbon.cu.language.locale", Locale.getDefault());
     
     public static InvertColor IC;
     public static RandomColor RC;
     public static ScrollColor SC;
     public static DigitalEyedropper DEyed;
     public static AverageColor AC;
     public static ToneColor TC;
     
     //<editor-fold defaultstate="collapsed" desc="Menu bar">
     //Menu bar
     public static JMenuBar menubar;
     public static JMenu menu;
     public static String[] menuNames = {
         RB.getString("file"), RB.getString("edit"), RB.getString("modules"), RB.getString("window"), RB.getString("help")
     };
     public static JMenuItem menuitem;
     public static String[] FilemenuitemNames = {
         RB.getString("file.Options")
     }; int[] FilemenuitemAccelerators = {
         KeyEvent.VK_O
     };
     public static String[] EditmenuitemNames = {
         RB.getString("edit.cHex"), RB.getString("edit.cAHex"), RB.getString("edit.cRGB"), RB.getString("edit.cRGBA"), "/",
         RB.getString("edit.pHex"), RB.getString("edit.pAHex"), RB.getString("edit.pRGB"), RB.getString("edit.pRGBA"), "/",
         RB.getString("edit.undo"), RB.getString("edit.redo"), "/",
         RB.getString("IC.invert"), RB.getString("IC.rinvert"), RB.getString("IC.ginvert"), RB.getString("IC.binvert"), "/",
         RB.getString("edit.b"), RB.getString("edit.d"),
     }; int[] EditmenuitemAccelerators = {
         KeyEvent.VK_C, -1, -1, -1, -1, KeyEvent.VK_V, -1, -1, -1, -1, KeyEvent.VK_Z, KeyEvent.VK_Y, -1,
         KeyEvent.VK_I, -1, -1, -1, -1,
         KeyEvent.VK_B, KeyEvent.VK_D
     };
     
     public static final String[] ModulesmenuitemNames = {
         RB.getString("AC"), RB.getString("DEyed"), RB.getString("RC"), RB.getString("SC")
     }; int[] ModulesmenuitemAccelerators = {
         KeyEvent.VK_A, KeyEvent.VK_E,KeyEvent.VK_R, KeyEvent.VK_S
     };
     public static final String[] HelpmenuitemNames = {
         RB.getString("help.a")
     }; int[] HelpmenuitemAccelerators = {
         KeyEvent.VK_0
     };
     public static JRadioButtonMenuItem rbmenuitem;
     public static JCheckBoxMenuItem cbmenuitem;
 //</editor-fold>
     
     public static Font Monaco18 = new Font("Monaco", Font.PLAIN, 18);
         
     public static JColorChooser cc;
     public static JCheckBox cmtoggle;
     
     public static JColorChooser ccc;
     public static JCheckBox ccmtoggle;
     
     public static Easyview Ev;
     public static JCheckBox evtoggle;
         
     public static String OSname;
     public static void getOSname(){
         OSname = System.getProperty("os.name").toLowerCase();
         if(OSname.contains("win")) OSname = "win";
         else if(OSname.contains("mac")) OSname = "mac";
     }
     
     public static void main(String[] args) {
         getOSname();
         if("mac".equals(OSname)){
             System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Color Utility");
             System.setProperty("apple.laf.useScreenMenuBar", "true");
         }
         CU = new ColorUtility();
         EvD = new Dimension(Ev.getSize());
         EvP = new Point(Ev.getLocationOnScreen());
         CUD = new Dimension(CU.getSize());
         
     } public ColorUtility() {
         //Configure the logger (Apache log4j)
        PropertyConfigurator.configure(getClass().getResource("/leocarbon/cu/logging/log4j.properties"));
         if(dologging){
             Logger.getLogger(ColorUtility.class.getName()).trace("rootlogger configured");
 
             Logger.getLogger(ColorUtility.class.getName()).trace("Operating System: " + System.getProperty("os.name"));
             Logger.getLogger(ColorUtility.class.getName()).trace("ColorUtility.OSname: " + OSname);
             Logger.getLogger(ColorUtility.class.getName()).trace("Java vendor: " + System.getProperty("java.vendor"));
             Logger.getLogger(ColorUtility.class.getName()).trace("Java vendor URL: ["+System.getProperty("java.vendor.url")+"]");
             Logger.getLogger(ColorUtility.class.getName()).trace("Java version: " + System.getProperty("java.version"));
             Logger.getLogger(ColorUtility.class.getName()).trace("Look and Feel: " + UIManager.getSystemLookAndFeelClassName());
         }
         
         //Set Look and Feel to the operating system's native Look and Feel
         if(!OptionsLAFChange){
             try {
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 if(UIManager.getLookAndFeel().getClass().getName().equals(UIManager.getSystemLookAndFeelClassName()) && dologging) Logger.getLogger(ColorUtility.class.getName()).trace("Set Look and Feel to "+UIManager.getSystemLookAndFeelClassName());
             } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException E){
                 if(dologging){
                     Logger.getLogger(ColorUtility.class.getName()).error(E);
                     Logger.getLogger(ColorUtility.class.getName()).trace("Could not set Look and Feel to "+UIManager.getSystemLookAndFeelClassName());
                 }
             }
         } else OptionsLAFChange = false;
         
         //Extra implementations for Mac OS
         if("mac".equals(OSname)){
             MacOSCU = new DefaultApplication();
             MacOSCU.addApplicationListener(new MacOSXHandler());
             MacOSCU.addPreferencesMenuItem();
             MacOSCU.setEnabledPreferencesMenu(true);
             if(dologging) Logger.getLogger(ColorUtility.class.getName()).trace("Optimized GUI for Mac OS");
         }
         
         //Initialize GridBagConstraints
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         
         //Initialize the window
         setTitle(RB.getString("CU.Title"));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLayout(new GridBagLayout());
         
         //Initialize the menubar and its menus
         menubar = new JMenuBar();
         menu = new JMenu(RB.getString("file")); menubar.add(createmenu(RB.getString("file"), FilemenuitemNames, FilemenuitemAccelerators));
         menu = new JMenu(RB.getString("edit")); menubar.add(createmenu(RB.getString("edit"), EditmenuitemNames, EditmenuitemAccelerators));
         menu = new JMenu(RB.getString("modules"));
             AverageColor a = new AverageColor(); DigitalEyedropper d = new DigitalEyedropper(); RandomColor rc = new RandomColor(); ScrollColor s = new ScrollColor();
             menubar.add(createmenu(RB.getString("modules"), ModulesmenuitemNames, ModulesmenuitemAccelerators));
         menu = new JMenu(RB.getString("help")); menubar.add(createmenu(RB.getString("help"), HelpmenuitemNames, HelpmenuitemAccelerators));
         
         //Make the Swatches bigger
         int SwatchSize = 15;
         UIManager.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(SwatchSize, SwatchSize));
         UIManager.put("ColorChooser.swatchesSwatchSize", new Dimension(SwatchSize, SwatchSize));
         //ColorChooser (JColorChooser)
         cmtoggle = new JCheckBox(RB.getString("cm"),true);
         Border cmborder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), RB.getString("cm"));
         final JPanel cm = new JPanel(new BorderLayout());
         cm.setBorder(cmborder);
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 1;
         c.insets = new Insets(2,4,2,4);
         add(cm,c);
 
         //CustomColorChooser (JColorChooser)
         ccmtoggle = new JCheckBox(RB.getString("ccm"),true);
         Border ccmborder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), RB.getString("ccm"));
         final JPanel ccm = new JPanel(new BorderLayout());
         ccm.setBorder(ccmborder);
         c.gridy = 2;
         add(ccm,c);
         
         //Easyview
         evtoggle = new JCheckBox(RB.getString("ev"),true);
         //Border evborder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Easy View");
         Ev = new Easyview();
         c.gridy = 0;
         c.insets = new Insets(-1,-1,2,-1);
         //evpanel.setBorder(evborder);
         add(Ev,c);
         
         int r, g, b;
         Random R = new Random();
         do {
             cc = new JColorChooser(new Color((R.nextFloat()),R.nextFloat(),R.nextFloat()));
             r = cc.getColor().getRed();
             g = cc.getColor().getGreen();
             b = cc.getColor().getBlue();
         } while(r >= 92 && g >= 92 && b >= 92);
         
         IC = new InvertColor();
         RC = new RandomColor();
         SC = new ScrollColor();
         DEyed = new DigitalEyedropper();
         AC = new AverageColor();
         TC = new ToneColor();
     
         ccc = new JColorChooser(new Color((R.nextFloat()),R.nextFloat(),R.nextFloat()));
         AbstractColorChooserPanel[] newPanels = {
             IC, RC, SC, DEyed, AC, TC
         };
         ccc.setChooserPanels(newPanels);
 
         Ev.update(cc.getColor());
         
         cc.setPreviewPanel(new JPanel());
         ccc.setPreviewPanel(new JPanel());
         
         cm.add(cc,BorderLayout.CENTER);
         ccm.add(ccc,BorderLayout.CENTER);
         
         ActionHandler.createColorMixerChangeListener();
         ActionHandler.createController(ccm,ccmtoggle);
         ActionHandler.createController(Ev,evtoggle);
         ActionHandler.createController(cm,cmtoggle);
 
         setJMenuBar(menubar);
 
         pack();
         setLocationRelativeTo(null);
         setVisible(true);
         setResizable(false);
         setMaximumSize(this.getSize());
         
         if(dologging) Logger.getLogger(ColorUtility.class.getName()).trace("Finished creating GUI");
     }
     
     public static JMenuItem[] Visibilitymi = new JMenuItem[4];
     private static int Visibilitymiindex = 0;
     public static JMenu createmenu(String description, String[] menuitemNames, int[] accelerator) {
         for(int a = 0; a < menuitemNames.length; ++a){
             if("/".equals(menuitemNames[a])) menu.addSeparator();
             else {
                 menuitem = new JMenuItem(menuitemNames[a]);
                 if(accelerator[a] == -1){
                     menuitem.setAccelerator(null);
                 } else {
                     menuitem.setAccelerator(KeyStroke.getKeyStroke(accelerator[a], Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                 }
                 menuitem.addActionListener(ActionHandler.ActionListener);
                 menuitem.setActionCommand(description+".menu."+menuitemNames[a]);
                 if(menuitemNames[a].equals(RB.getString("edit.cRGB")) || menuitemNames[a].equals(RB.getString("edit.pRGB")) || menuitemNames[a].equals(RB.getString("edit.undo")) || menuitemNames[a].equals(RB.getString("edit.redo"))){
                     menuitem.setEnabled(false);
                 }
                 menu.add(menuitem);
             }
         }
         menu.getAccessibleContext().setAccessibleDescription(description);
         
         return menu;
     }
     
     
     public class MacOSXHandler implements ApplicationListener {
         @Override
         public void handleAbout(ApplicationEvent AE) {
             A = new About();
             AE.setHandled(true);
         }
         @Override
         public void handlePreferences(ApplicationEvent AE) {
             O = new Options();
             AE.setHandled(true);
         }
         @Override
         public void handleQuit(ApplicationEvent AE) {
             CU.setVisible(false);
             CU.dispose();
             CU = null;
             System.gc();
             AE.setHandled(true);
         } 
         @Override
         public void handleOpenApplication(ApplicationEvent AE) {} @Override
         public void handleOpenFile(ApplicationEvent AE) {} @Override
         public void handlePrintFile(ApplicationEvent AE) {} @Override
         public void handleReOpenApplication(ApplicationEvent AE) {}
     }
 }
