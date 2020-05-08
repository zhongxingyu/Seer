 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 public class AboutPanel extends JPanel {
 
     private static final long serialVersionUID = 766490351950584347L;
 
     ListenerHandler           listener;
     GridBagConstraints        c                = new GridBagConstraints();
     Font                      textfont         = new Font("Courier New", 1, 15);
     Color                     yellowColor      = new Color(0, 205, 106);
     Font                      mono             = new Font(Font.MONOSPACED,
                                                        Font.PLAIN, 12);
 
     JLabel                    enlLogo, aboutUs, version, versionnumber,
             weblink1, weblink2, weblink3, weblink4, weblink, contact,
             contact_mail, contact_g_M0P, contact_g_Xeno;
     JTextArea                 aboutUsText;
     Cursor                    hand             = new Cursor(Cursor.HAND_CURSOR);
 
     public AboutPanel(ListenerHandler listenerHandler) {
         super(new GridBagLayout());
         this.listener = listenerHandler;
 
         //
         this.enlLogo = new JLabel(makeImageIcon("/images/enlightened_big.png"));
         this.aboutUs = new JLabel("About us");
         this.version = new JLabel("Version");
        this.versionnumber = new JLabel("0.1.00");
         this.weblink = new JLabel("Weblinks");
         this.weblink1 = new JLabel("Our Website");
         this.weblink2 = new JLabel("G+ Decode Community");
         this.weblink3 = new JLabel("Niantic Investigation");
         this.weblink4 = new JLabel("Intel Map");
         this.contact = new JLabel("Contact us");
         this.contact_mail = new JLabel("Write a mail");
         this.contact_g_M0P = new JLabel("G+ MOP");
         this.contact_g_Xeno = new JLabel("G+ Xenowarius");
 
         this.aboutUsText = new JTextArea(
                 "This tool is developed by Xenowar and MOP.\n"
                         + "We implemented several decoding methods which were frequently used by the Ingress team.");
 
         // apply style to elements
         this.aboutUs.setFont(this.textfont);
         this.aboutUsText.setFont(this.textfont);
         this.version.setFont(this.textfont);
         this.versionnumber.setFont(this.textfont);
         this.weblink.setFont(this.textfont);
         this.weblink1.setFont(this.textfont);
         this.weblink2.setFont(this.textfont);
         this.weblink3.setFont(this.textfont);
         this.weblink4.setFont(this.textfont);
         this.contact.setFont(this.textfont);
         this.contact_mail.setFont(this.textfont);
         this.contact_g_M0P.setFont(this.textfont);
         this.contact_g_Xeno.setFont(this.textfont);
 
         this.aboutUs.setBackground(Color.black);
         this.aboutUsText.setBackground(Color.black);
         this.version.setBackground(Color.black);
         this.versionnumber.setBackground(Color.black);
         this.weblink.setBackground(Color.black);
         this.weblink1.setBackground(Color.black);
         this.weblink2.setBackground(Color.black);
         this.weblink3.setBackground(Color.black);
         this.weblink4.setBackground(Color.black);
         this.contact.setBackground(Color.black);
         this.contact_mail.setBackground(Color.black);
         this.contact_g_M0P.setBackground(Color.black);
         this.contact_g_Xeno.setBackground(Color.black);
 
         this.aboutUs.setForeground(this.yellowColor);
         this.aboutUsText.setForeground(this.yellowColor);
         this.version.setForeground(this.yellowColor);
         this.versionnumber.setForeground(this.yellowColor);
         this.weblink.setForeground(this.yellowColor);
         this.weblink1.setForeground(this.yellowColor);
         this.weblink2.setForeground(this.yellowColor);
         this.weblink3.setForeground(this.yellowColor);
         this.weblink4.setForeground(this.yellowColor);
         this.contact.setForeground(this.yellowColor);
         this.contact_mail.setForeground(this.yellowColor);
         this.contact_g_M0P.setForeground(this.yellowColor);
         this.contact_g_Xeno.setForeground(this.yellowColor);
 
         this.aboutUsText.setEditable(false);
         this.aboutUsText.setBorder(new EmptyBorder(0, 0, 0, 0));
         this.aboutUsText.setLineWrap(true);
         this.aboutUsText.setWrapStyleWord(true);
 
         // setting custom cursor
         this.weblink1.setCursor(this.hand);
         this.weblink2.setCursor(this.hand);
         this.weblink3.setCursor(this.hand);
         this.weblink4.setCursor(this.hand);
         this.contact_mail.setCursor(this.hand);
         this.contact_g_M0P.setCursor(this.hand);
         this.contact_g_Xeno.setCursor(this.hand);
 
         // adding listener for weblinks
         this.weblink1.setName("link1");
         this.weblink1.addMouseListener(this.listener);
         this.weblink2.setName("link2");
         this.weblink2.addMouseListener(this.listener);
         this.weblink3.setName("link3");
         this.weblink3.addMouseListener(this.listener);
         this.weblink4.setName("link4");
         this.weblink4.addMouseListener(this.listener);
         this.contact_mail.setName("mail");
         this.contact_mail.addMouseListener(this.listener);
         this.contact_g_M0P.setName("MOP");
         this.contact_g_M0P.addMouseListener(this.listener);
         this.contact_g_Xeno.setName("Xeno");
         this.contact_g_Xeno.addMouseListener(this.listener);
         this.contact.setName("Ee");
         this.contact.addMouseListener(this.listener);
 
         // adding components to panel
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.1;
         this.c.gridx = 2;
         this.c.gridy = 0;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 0, 0, 0);
         add(this.aboutUs, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 1;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.aboutUsText, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.1;
         this.c.gridx = 2;
         this.c.gridy = 2;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 0, 0, 0);
         add(this.version, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 3;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.versionnumber, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.1;
         this.c.gridx = 2;
         this.c.gridy = 4;
         this.c.gridwidth = 2;
         this.c.insets = new Insets(0, 0, 0, 0);
         add(this.weblink, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 5;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.weblink1, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 6;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.weblink2, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 7;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.weblink3, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 8;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.weblink4, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 9;
         this.c.insets = new Insets(0, 0, 0, 0);
         add(this.contact, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 10;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.contact_mail, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 11;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.contact_g_M0P, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.8;
         this.c.gridx = 3;
         this.c.gridy = 12;
         this.c.insets = new Insets(0, 30, 0, 0);
         add(this.contact_g_Xeno, this.c);
 
         this.c.fill = GridBagConstraints.BOTH;
         this.c.weightx = 0.1;
         this.c.gridx = 0;
         this.c.gridy = 0;
         this.c.anchor = GridBagConstraints.WEST;
         this.c.gridheight = 20;
         this.c.insets = new Insets(0, 0, 0, 0);
         add(this.enlLogo, this.c);
 
         setBackground(Color.black);
     }
 
     public ImageIcon makeImageIcon(String relative_path) {
         URL imgURL = getClass().getResource(relative_path);
         return new ImageIcon(imgURL);
     }
 }
