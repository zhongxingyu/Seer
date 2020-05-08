 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.net.URL;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.plaf.ComboBoxUI;
 
 public class GUI2 extends JFrame {
 
     private static final long serialVersionUID = -849850209835435686L;
 
     public static JLabel makeTextLabel(String text) {
         JLabel label = new JLabel(text);
         label.setFont(new Font("Courier New", 1, 15)); //$NON-NLS-1$
         label.setForeground(new Color(0, 205, 106));
         return label;
     }
 
     ListenerHandler              listener;
     JPanel                       mainPanel, innerPane1, innerPane2;
     JTabbedPane                  tabbedPane;
     JCheckBox                    alwaysontop;
     JCheckBox                    livedecode;
 
     JTextField                   tf_code;
     JComboBox<String>            cb_code;
     DefaultComboBoxModel<String> model;
     ColoredMenuBar               menubar;
     JButton                      btn_generate;
     JButton                      close, minimize, small, medium, large;
     GridBagConstraints           c            = new GridBagConstraints();
 
     Font                         textfont     = new Font("Courier New", 1, 16); //$NON-NLS-1$
 
     Color                        yellowColor  = new Color(0, 205, 106);
 
     Font                         mono         = new Font(Font.MONOSPACED,
                                                       Font.PLAIN, 14);
     boolean                      islivedecode = false;
 
     Vector<String>               codes        = new Vector<>();
 
     static String                currentlang  = "en";                          //$NON-NLS-1$
 
     CipherPanel                  cipher1;
     CipherPanel2                 cipher2;
     CipherPanel3                 cipher3;
     ConvertPanel                 convert;
     AboutPanel                   about;
 
     public GUI2(String lang) {
         currentlang = lang;
         this.listener = new ListenerHandler(this);
         this.cipher1 = new CipherPanel(this.listener);
         this.cipher2 = new CipherPanel2(this.listener);
         this.cipher3 = new CipherPanel3(this.listener);
         this.convert = new ConvertPanel(this.listener);
         this.about = new AboutPanel(this.listener);
     }
 
     public void initGUI() {
 
         // init main panel components
         this.mainPanel = new JPanel(new GridBagLayout());
         this.tabbedPane = new JTabbedPane();
 
         this.mainPanel.setBackground(Color.black);
 
         this.tabbedPane.setBackground(Color.black);
         this.tabbedPane.setForeground(this.yellowColor);
         this.tabbedPane.setFont(this.textfont);
         this.tabbedPane.setUI(new CustomTabbedPaneUI());
         this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 
         // preparing combobox model
         this.model = new DefaultComboBoxModel<>(this.codes);
 
         // menu bar items
         this.close = new JButton(makeImageIcon("/images/close.png")); //$NON-NLS-1$
         this.close.setRolloverIcon(makeImageIcon("/images/close_hover.png")); //$NON-NLS-1$
         this.close.setPressedIcon(makeImageIcon("/images/close_pressed.png")); //$NON-NLS-1$
         this.close.setBorder(BorderFactory.createEmptyBorder());
         this.close.setContentAreaFilled(false);
         this.close.setName("close"); //$NON-NLS-1$
         this.close.addMouseListener(this.listener);
         this.minimize = new JButton(makeImageIcon("/images/minimize.png")); //$NON-NLS-1$
         this.minimize
                 .setRolloverIcon(makeImageIcon("/images/minimize_hover.png")); //$NON-NLS-1$
         this.minimize
                 .setPressedIcon(makeImageIcon("/images/minimize_pressed.png")); //$NON-NLS-1$
         this.minimize.setBorder(BorderFactory.createEmptyBorder());
         this.minimize.setContentAreaFilled(false);
         this.minimize.setName("minimize"); //$NON-NLS-1$
         this.minimize.addMouseListener(this.listener);
         this.large = new JButton(makeImageIcon("/images/large.png")); //$NON-NLS-1$
         this.large.setRolloverIcon(makeImageIcon("/images/large_hover.png")); //$NON-NLS-1$
         this.large.setPressedIcon(makeImageIcon("/images/large_pressed.png")); //$NON-NLS-1$
         this.large.setBorder(BorderFactory.createEmptyBorder());
         this.large.setContentAreaFilled(false);
         this.large.setName("large"); //$NON-NLS-1$
         this.large.addMouseListener(this.listener);
         this.medium = new JButton(makeImageIcon("/images/medium.png")); //$NON-NLS-1$
         this.medium.setRolloverIcon(makeImageIcon("/images/medium_hover.png")); //$NON-NLS-1$
         this.medium.setPressedIcon(makeImageIcon("/images/medium_pressed.png")); //$NON-NLS-1$
         this.medium.setBorder(BorderFactory.createEmptyBorder());
         this.medium.setContentAreaFilled(false);
         this.medium.setName("medium"); //$NON-NLS-1$
         this.medium.addMouseListener(this.listener);
         this.small = new JButton(makeImageIcon("/images/small.png")); //$NON-NLS-1$
         this.small.setRolloverIcon(makeImageIcon("/images/small_hover.png")); //$NON-NLS-1$
         this.small.setPressedIcon(makeImageIcon("/images/small_pressed.png")); //$NON-NLS-1$
         this.small.setBorder(BorderFactory.createEmptyBorder());
         this.small.setContentAreaFilled(false);
         this.small.setName("small"); //$NON-NLS-1$
         this.small.addMouseListener(this.listener);
 
         JLabel ingressIcon = new JLabel(
                 makeImageIcon("/images/Ingress_Logo_Middle.png")); //$NON-NLS-1$
         JLabel flagGerman = new JLabel(makeImageIcon("/images/flag_german.png")); //$NON-NLS-1$
         // JLabel flagChinese = new JLabel(
         //                makeImageIcon("/images/flag_chinese.png")); //$NON-NLS-1$
         JLabel flagEnglish = new JLabel(
                 makeImageIcon("/images/flag_english.png")); //$NON-NLS-1$
 
         JLabel title = new JLabel(Messages.getString("GUI2.Label_Title")); //$NON-NLS-1$
         title.setFont(new Font("Courier New", 1, 25)); //$NON-NLS-1$
         title.setForeground(new Color(0, 205, 106));
 
         flagGerman.setName("flag_de"); //$NON-NLS-1$
         flagEnglish.setName("flag_en"); //$NON-NLS-1$
         //        flagChinese.setName("flag_cn"); //$NON-NLS-1$
 
         flagGerman.addMouseListener(this.listener);
         flagEnglish.addMouseListener(this.listener);
         // flagChinese.addMouseListener(this.listener);
 
         // init menubar
         this.menubar = new ColoredMenuBar();
         this.menubar.addMouseMotionListener(this.listener);
         this.menubar.addMouseListener(this.listener);
         this.menubar.setBorder(new EmptyBorder(0, 0, 0, 0));
 
         this.menubar.add(ingressIcon);
         this.menubar.add(Box.createHorizontalStrut(10));
         this.menubar.add(title);
         this.menubar.add(Box.createHorizontalGlue());
         if (GUI2.currentlang.equals("en")) { //$NON-NLS-1$
             this.menubar.add(flagGerman);
             this.menubar.add(Box.createHorizontalStrut(5));
             // this.menubar.add(flagChinese);
         } else if (GUI2.currentlang.equals("de")) { //$NON-NLS-1$
             this.menubar.add(flagEnglish);
             this.menubar.add(Box.createHorizontalStrut(5));
             // this.menubar.add(flagChinese);
             //        } else if (GUI2.currentlang.equals("cn")) { //$NON-NLS-1$
             // this.menubar.add(flagEnglish);
             // this.menubar.add(Box.createHorizontalStrut(5));
             // this.menubar.add(flagGerman);
         }
         this.menubar.add(Box.createHorizontalStrut(10));
         this.menubar.add(this.small);
         this.menubar.add(this.medium);
         this.menubar.add(this.large);
         this.menubar.add(this.minimize);
         this.menubar.add(this.close);
 
         // adding pannels to tabbed pain
         this.tabbedPane
                 .addTab(Messages.getString("GUI2.Label_Tab1"), makeImageIcon("/images/Ingress_Logo.png"), //$NON-NLS-1$ //$NON-NLS-2$
                         this.cipher1,
                         Messages.getString("GUI2.Label_Tab1_Desc")); //$NON-NLS-1$
         this.tabbedPane.addTab(Messages.getString("GUI2.Label_Tab2"), //$NON-NLS-1$
                 makeImageIcon("/images/Ingress_Logo.png"), this.cipher2, //$NON-NLS-1$
                 Messages.getString("GUI2.Label_Tab2_Desc")); //$NON-NLS-1$
         this.tabbedPane
                 .addTab(Messages.getString("GUI2.Label_Tab3"), makeImageIcon("/images/Ingress_Logo.png"), //$NON-NLS-1$ //$NON-NLS-2$
                         this.cipher3,
                         Messages.getString("GUI2.Label_Tab3_Desc")); //$NON-NLS-1$
         this.tabbedPane.addTab(Messages.getString("GUI2.Label_Tab4"), //$NON-NLS-1$
                 makeImageIcon("/images/Ingress_Logo.png"), this.convert, //$NON-NLS-1$
                 Messages.getString("GUI2.Label_Tab4_Desc")); //$NON-NLS-1$
         this.tabbedPane.addTab(Messages.getString("GUI2.Label_Tab5"), //$NON-NLS-1$
                 makeImageIcon("/images/enlightened.png"), this.about, //$NON-NLS-1$
                 Messages.getString("GUI2.Label_Tab5_Desc")); //$NON-NLS-1$
         this.tabbedPane.setMnemonicAt(0, KeyEvent.VK_0);
         this.tabbedPane.setMnemonicAt(1, KeyEvent.VK_1);
 
         // adding components to mainpanel
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 0;
         this.c.insets = new Insets(0, 5, 0, 5);
         this.mainPanel.add(
                 makeTextLabel(Messages.getString("GUI2.Label_Code")), this.c); //$NON-NLS-1$
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.01;
         this.c.gridx = 1;
         this.c.gridy = 0;
         this.c.insets = new Insets(0, 5, 0, 5);
         this.alwaysontop = new JCheckBox(
                 Messages.getString("GUI2.Label_AlwaysOnTop")); //$NON-NLS-1$
         this.alwaysontop.setFont(this.textfont);
         this.alwaysontop.setForeground(this.yellowColor);
         this.alwaysontop.setActionCommand("onTop"); //$NON-NLS-1$
         this.alwaysontop.addActionListener(this.listener);
         this.mainPanel.add(this.alwaysontop, this.c);
 
         this.c.fill = GridBagConstraints.HORIZONTAL;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 1;
         // this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 5, 5, 5);
         this.tf_code = new JTextField();
         this.tf_code.setBackground(Color.black);
         this.tf_code.setFont(this.mono);
         this.tf_code.setForeground(this.yellowColor);
         this.tf_code.setName("code"); //$NON-NLS-1$
         this.tf_code.addCaretListener(this.listener);
 
         // combox testing
         this.cb_code = new JComboBox<>(this.model);
         this.cb_code.setBackground(Color.black);
         this.cb_code.setEditable(true);
         this.cb_code.setForeground(this.yellowColor);
         this.cb_code.setBorder(new LineBorder(this.yellowColor, 1));
         this.cb_code.setUI((ComboBoxUI) MyComboBoxUI.createUI(this.cb_code));
         this.cb_code.setFont(this.mono);
         this.cb_code.setName("code"); //$NON-NLS-1$
         this.cb_code.getEditor().getEditorComponent()
                 .addKeyListener(this.listener);
         this.cb_code.getEditor().getEditorComponent().setName("code"); //$NON-NLS-1$
         // testing end
 
         this.mainPanel.add(this.cb_code, this.c);
 
         this.c.fill = GridBagConstraints.HORIZONTAL;
         this.c.weightx = 0.01;
         this.c.gridx = 1;
         this.c.gridy = 1;
         // this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 5, 5, 5);
         this.livedecode = new JCheckBox(
                 Messages.getString("GUI2.Label_LiveDecode")); //$NON-NLS-1$
         this.livedecode.setFont(this.textfont);
         this.livedecode.setForeground(this.yellowColor);
         this.livedecode.setActionCommand("liveDecode"); //$NON-NLS-1$
         this.livedecode.addActionListener(this.listener);
         this.mainPanel.add(this.livedecode, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 2;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 0, 0, 0);
         this.mainPanel.add(this.tabbedPane, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 1.0;
         this.c.gridx = 0;
         this.c.gridy = 3;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(5, 5, 5, 5);
         this.btn_generate = new JButton(
                 Messages.getString("GUI2.Label_Generate")); //$NON-NLS-1$
         this.btn_generate.setBackground(Color.black);
         this.btn_generate.setFont(this.textfont);
         this.btn_generate.setForeground(this.yellowColor);
         this.btn_generate.setActionCommand("generate"); //$NON-NLS-1$
         this.btn_generate.addActionListener(this.listener);
         this.mainPanel.add(this.btn_generate, this.c);
 
         // adding components to frame
         add(this.menubar, BorderLayout.NORTH);
         add(this.mainPanel, BorderLayout.CENTER);
 
         // general frame settings
         setSize(new Dimension(900, 500));
         setIconImage(makeImageIcon("/images/Ingress_Logo_Middle.png") //$NON-NLS-1$
                 .getImage());
        setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 450,
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 250);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setTitle(Messages.getString("GUI2.Label_Title")); //$NON-NLS-1$
         setUndecorated(true);
         setVisible(true);
     }
 
     public ImageIcon makeImageIcon(String relative_path) {
         URL imgURL = getClass().getResource(relative_path);
         return new ImageIcon(imgURL);
     }
 }
