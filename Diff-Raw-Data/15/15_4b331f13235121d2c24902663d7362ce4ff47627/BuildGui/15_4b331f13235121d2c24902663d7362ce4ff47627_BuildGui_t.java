 /*
  * McZLauncher (ZeTRiX's Minecraft Launcher)
  * Copyright (C) 2012 Evgen Yanov <http://www.zetlog.ru>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program (In the "_License" folder). If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ru.zetrix.engine;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import javax.swing.*;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.EmptyBorder;
 import ru.zetrix.gui.ServerStats;
 import ru.zetrix.settings.*;
 
 /**
  * @author ZeTRiX
  */
 public class BuildGui extends JFrame {
     public static boolean WrongClient = false;
     public static BuildGui buildgui;
     public static MCStart MineStart;
     private static Updater Update;
     public JButton loginb = new JButton("Login", (new ImageIcon(ru.zetrix.settings.Util.getRes("login.png"))));
     public JButton openNews = new JButton("News", (new ImageIcon(ru.zetrix.settings.Util.getRes("news.png"))));
     public JButton Options = new JButton("More Options", (new ImageIcon(ru.zetrix.settings.Util.getRes("opt.png"))));
     public static JLabel UserText = new JLabel("User:");
     public static JTextField UserName = new JTextField(20);
     public static JLabel PassText = new JLabel("Password:");
     public static JPasswordField Password = new JPasswordField(20);
     public JCheckBox SaveData = new JCheckBox("Remember me", false);
     
     private static JTabbedPane tabbedPane;
     private static Box MainBox;
     private static Box OptBox;
     private static Box RegBox;
     private static Box SupBox;
     private static Box MonBox;
     private static Box AboutBox;
     
     public static JLabel User = new JLabel("Login:");
     public static JTextField Login = new JTextField(20);
     public static JLabel Pass = new JLabel("Password:");
     public static JPasswordField Passwd = new JPasswordField(20);
     public static JLabel Mail = new JLabel("Email:");
     public static JTextField MailF = new JTextField(20);
     public JButton regb = new JButton("Register", (new ImageIcon(ru.zetrix.settings.Util.getRes("reg.png"))));
 
     String[] modelem = new String[] {"Online", "Offline"};
     private JComboBox mode = new JComboBox(modelem);
 
     public static JLabel MemText = new JLabel("Memory:");
     String[] memoryset = new String[] {"1024", "2048", "4096", "8192", "16384"};
     private JComboBox memory = new JComboBox(memoryset);
     public static JLabel MemType = new JLabel(" MB");
     public JCheckBox FullScreen = new JCheckBox("Enable Fullscreen", Util.getPropertyBoolean("fullscreen"));
     public JButton updb = new JButton("Update now", (new ImageIcon(ru.zetrix.settings.Util.getRes("upd.png"))));
     
     public static JLabel MonText = new JLabel("Server to Join:");
     public JComboBox MonitorCB = new JComboBox(MonitorUtils.CreateServerList());
     public static ServerStats MonitGui = new ServerStats();
     
     public JButton Send = new JButton("Send", (new ImageIcon(Util.getRes("send.png"))));
     public static JLabel ClC = new JLabel("Your text:");
     public static JLabel ClT = new JLabel("Recieved text:");
     public static JTextField ClNum = new JTextField(20);
     public static JTextPane Text;
     public static DataOutputStream out;
     
     public static JLabel About = new JLabel("McZLauncher (ZeTRiX's Minecraft Launcher)");
     public static JLabel About1 = new JLabel("This program is free software: you can redistribute it and/or modify");
     public static JLabel About2 = new JLabel("it under the terms of the GNU General Public License as published by");
     public static JLabel About3 = new JLabel("the Free Software Foundation, either version 3 of the License.");
     public static JLabel About4 = new JLabel("Licensed under GNU GPL ver.3");
     public static JLabel About5 = new JLabel("Copyright (C) 2012 ZeTRiX (Evgen Yanov)");
     
     public static Font BaseFont;
     public static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
     public BuildGui() {
         setTitle(MZLOptions.LauncherTitle);
         setIconImage(ru.zetrix.settings.Util.getRes("ficon.png"));
         setBackground(Color.BLACK);
         this.setBounds(200, 200, 460, 200);
         setResizable(false);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         try {
             InputStream is = this.getClass().getResourceAsStream("/ru/zetrix/res/Bevan.ttf");
             Font NewFont = Font.createFont(Font.TRUETYPE_FONT, is);
             BaseFont = NewFont.deriveFont(Font.PLAIN, 14);
         } catch (FontFormatException | IOException ex) {
             print(ex.toString());
         }
         
         final ImageIcon icon = new ImageIcon(BuildGui.class.getResource("/ru/zetrix/res/bg.png"));
         tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT) {
             @Override
             protected void paintComponent(Graphics g) {
                 g.drawImage(icon.getImage(), 0,0, null);
                 super.paintComponent(g);
             }
         };
         Box box1 = Box.createHorizontalBox();
         box1.add(UserText);
         box1.add(Box.createHorizontalStrut(6));
         box1.add(UserName);
         if (Util.getPropertyString("login") != null)
             UserName.setText(Util.getPropertyString("login"));
         Box box2 = Box.createHorizontalBox();
         box2.add(PassText);
         box2.add(Box.createHorizontalStrut(6));
         box2.add(Password);
         if (Util.getPropertyString("password") != null)
             Password.setText(new String(Base64.decode(Util.getPropertyString("password"))));
         Box box3 = Box.createHorizontalBox();
         box3.add(Box.createHorizontalGlue());
         //box3.add(update);
         box3.add(Box.createHorizontalStrut(12));
         box3.add(SaveData);
         box3.add(Box.createHorizontalStrut(10));
         mode.setSelectedIndex(0);
         box3.add(mode);
         box3.add(Box.createHorizontalStrut(10));
         box3.add(loginb);
         UserText.setPreferredSize(PassText.getPreferredSize());
         MainBox = Box.createVerticalBox();
         MainBox.setBorder(new EmptyBorder(12,12,12,12));
         MainBox.add(box1);
         MainBox.add(Box.createVerticalStrut(12));
         MainBox.add(box2);
         MainBox.add(Box.createVerticalStrut(17));
         MainBox.add(box3);
         tabbedPane.add("Login", MainBox);
 
         Box opt1 = Box.createHorizontalBox();
         opt1.add(MemText);
         opt1.add(Box.createHorizontalStrut(16));
         memory.setSelectedItem(Util.getPropertyInt("memory", 1024));
         opt1.add(memory);
         opt1.add(Box.createHorizontalStrut(16));
         opt1.add(MemType);
         Box opt2_1 = Box.createHorizontalBox();
         opt2_1.add(FullScreen);
         Box opt2 = Box.createHorizontalBox();
         opt2.add(Options);
         opt2.add(Box.createHorizontalStrut(6));
         opt2.add(openNews);
         opt2.add(Box.createHorizontalStrut(6));
         opt2.add(updb);
         Options.setPreferredSize(openNews.getPreferredSize());
         OptBox = Box.createVerticalBox();
         OptBox.setBorder(new EmptyBorder(12,12,12,12));
         OptBox.add(opt1);
         OptBox.add(Box.createVerticalStrut(12));
         OptBox.add(opt2_1);
         OptBox.add(Box.createVerticalStrut(12));
         OptBox.add(opt2);
         tabbedPane.add("Options & Update", OptBox);
         
         Box usbox = Box.createHorizontalBox();
         usbox.add(User);
         usbox.add(Box.createHorizontalStrut(5));
         usbox.add(Login);
         Box pasbox = Box.createHorizontalBox();
         pasbox.add(Pass);
         pasbox.add(Box.createHorizontalStrut(5));
         pasbox.add(Passwd);
         Box embox = Box.createHorizontalBox();
         embox.add(Mail);
         embox.add(Box.createHorizontalStrut(5));
         embox.add(MailF);
         Box acpbox = Box.createHorizontalBox();
         acpbox.add(Box.createHorizontalGlue());
         acpbox.add(Box.createHorizontalStrut(8));
         acpbox.add(regb);
         User.setPreferredSize(Pass.getPreferredSize());
         Mail.setPreferredSize(Pass.getPreferredSize());
         RegBox = Box.createVerticalBox();
         RegBox.setBorder(new EmptyBorder(10,10,10,10));
         RegBox.add(usbox);
         RegBox.add(Box.createVerticalStrut(10));
         RegBox.add(pasbox);
         RegBox.add(Box.createVerticalStrut(10));
         RegBox.add(embox);
         RegBox.add(Box.createVerticalStrut(15));
         RegBox.add(acpbox);
         tabbedPane.add("Register", RegBox);
         
         
         Box sup1 = Box.createHorizontalBox();
         sup1.add(ClC);
         box1.add(Box.createHorizontalStrut(6));
         sup1.add(ClNum);
         Box sup2 = Box.createHorizontalBox();
         sup2.add(ClT);
         sup2.add(Box.createHorizontalStrut(6));
         Text = new JTextPane() {
             private static final long serialVersionUID = 1L;
         };
         Text.setText("Server message will appear here!");
         Text.setEditable(false);
         sup2.add(Text);
         Box sup3 = Box.createHorizontalBox();
         sup3.add(Send);
         ClC.setPreferredSize(ClT.getPreferredSize());
         SupBox = Box.createVerticalBox();
         SupBox.setBorder(new EmptyBorder(12,12,12,12));
         SupBox.add(sup1);
         SupBox.add(Box.createVerticalStrut(8));
         SupBox.add(sup2);
         SupBox.add(Box.createVerticalStrut(6));
         SupBox.add(sup3);
         tabbedPane.add("Support", SupBox);
         
         Box mon1 = Box.createHorizontalBox();
         mon1.add(MonText);
         mon1.add(Box.createHorizontalStrut(8));
         MonitorCB.setSelectedIndex(Util.getPropertyInt("server", 0));
         mon1.add(MonitorCB);
         Box mon2 = Box.createHorizontalBox();
         mon2.add(new JLabel());
         mon2.add(Box.createHorizontalStrut(8));
         mon2.add(MonitGui);
         Box mon3 = Box.createHorizontalBox();
         mon3.add(new JLabel());
         mon3.add(Box.createHorizontalStrut(8));
         mon3.add(new JLabel());
         MonBox = Box.createVerticalBox();
         MonBox.setBorder(new EmptyBorder(12,12,12,12));
         MonBox.add(mon1);
         MonBox.add(Box.createVerticalStrut(12));
         MonBox.add(mon2);
         MonBox.add(Box.createVerticalStrut(12));
         MonBox.add(mon3);
         tabbedPane.add("Servers", MonBox);
         
         Box ab0 = Box.createHorizontalBox();
         Box ab1 = Box.createVerticalBox();
         About.setFont(BaseFont);
         ab1.add(About);
         ab1.add(About1);
         ab1.add(About2);
         ab1.add(About3);
         ab0.add(ab1);
         Box ab2 = Box.createHorizontalBox();
         ab2.add(About4);
         ab2.add(Box.createHorizontalStrut(6));
         Box ab3 = Box.createHorizontalBox();
         ab3.add(About5);
         AboutBox = Box.createVerticalBox();
         AboutBox.setBorder(new EmptyBorder(12,12,12,12));
         AboutBox.add(ab0);
         AboutBox.add(Box.createVerticalStrut(8));
         AboutBox.add(ab2);
         AboutBox.add(Box.createVerticalStrut(6));
         AboutBox.add(ab3);
         tabbedPane.add("About", AboutBox);
         
         this.getContentPane().add(tabbedPane);
         
         loginb.addActionListener(new LoginListner());
         regb.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                Auther.Register(Login.getText(), new String(Passwd.getPassword()), MailF.getText());
             }
         });
         updb.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 updb.setEnabled(false);
                 Update = new Updater();
                 Update.show();
             }
         });
         openNews.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 new ru.zetrix.gui.News().setVisible(true);
             }
         });
         Options.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 new ru.zetrix.gui.Options().setVisible(true);
             }
         });
         memory.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Util.setProperty("memory", memory.getSelectedItem());
             }
         });
         SaveData.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (SaveData.isSelected()) {
                     Util.setProperty("login", UserName.getText());
                     Util.setProperty("password", Base64.encode(new String (BuildGui.Password.getPassword())));
                 }
             }
         });
         FullScreen.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (FullScreen.isSelected()) {
                     Util.setProperty("fullscreen", true);
                 } else {
                     Util.setProperty("fullscreen", false);
                 }
             }
         });
         Send.addActionListener(new SupSend());
         MonitorCB.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Util.setProperty("server", Integer.valueOf(MonitorCB.getSelectedIndex()));
                 MonitorUtils.pollSelectedServer();
             }
         });
     }
     
     class LoginListner implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             if (mode.getSelectedItem().equals("Online") && (UserName.getText().length() > 1) && (Password.getPassword().length > 1)) {
                 
                 if (Auther.Authorize(UserName.getText(), new String(Password.getPassword())) == true) {
                 setVisible(false);
                 } else if (WrongClient == true) {
                     JOptionPane.showMessageDialog((Component)
                             null,
                             "Version of your launcher is not valid. \n Please, visit the server site to get a new one! \n",
                             "Warning",
                             JOptionPane.WARNING_MESSAGE);
                 }
         
             } else if(UserName.getText().length() < 2) {
                 JOptionPane.showMessageDialog((Component)
                         null,
                         "You forget to enter a username or it's not valid \n Please, next time, do not forget to enter a valid one. \n",
                         "Warning",
                         JOptionPane.WARNING_MESSAGE);
             } else {
                 print("Switching to offline mode...");
                 
                 if (Auther.OfflineCheck() == true) {
                     MineStart = new MCStart(UserName.getText(), "12345");
                     setVisible(false);
                 } else {
                     JOptionPane.showMessageDialog((Component)
                             null,
                             "One or more game files not found! \n Can't do anything, case of offline mode. \n You must be in Online mode to download client files.",
                             "Offline mode Error",
                             JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         }
     }
     
         class SupSend implements ActionListener {
             public void actionPerformed(ActionEvent e) {
                 if (ClNum.getText().toString().trim() == null) {
                     JOptionPane.showMessageDialog((Component)
                             null,
                             "No command was typed. \n Please, type a command and try again! \n",
                             "Warning",
                             JOptionPane.WARNING_MESSAGE);
                 } else {
                     try {
                         out.writeUTF(ClNum.getText().toString());
                         dlprint(dateFormat.format(Calendar.getInstance().getTime()) + "> Client> " + ClNum.getText().toString());
                         out.flush();
                     } catch (IOException ex) {
                         System.out.println(ex.toString());
                     }
                 }
             }
         }
             
     public static void McZMSConnect(final String ip) {
         new Thread() {
             @Override
             public void run() {
                 int sPort = 59092;
                 print("Chat server found: " + ip.trim() + ":" + sPort);
                 try {
                     Socket socket = new Socket(InetAddress.getByName(ip.trim()), sPort);
                     
                     InputStream sin = socket.getInputStream();
                     OutputStream sout = socket.getOutputStream();
                     
                     DataInputStream in = new DataInputStream(sin);
                     out = new DataOutputStream(sout);
                     while (true) {
                         String tl = in.readUTF();
                         Text.setText(tl);
                         dlprint(dateFormat.format(Calendar.getInstance().getTime()) + "> Server> " + tl);
                     }
                 } catch (Exception x) {
                     print(x.toString());
                 }
             }
         }
                 .start();
     }
     
     public static void main(String[] args) {
         try {
 //            UIManager.setLookAndFeel(MZLaf.class.getCanonicalName());
             for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (Exception e) {
             print("Error: " + e);
         }
         buildgui = new BuildGui();
         buildgui.show();
         SupStart();
     }
     
     public static void SupStart() {
         String SupRes = ru.zetrix.settings.NetUtil.ConnectNet(MZLOptions.AuthScrpt, "a=addr" + "&opt=" + ru.zetrix.settings.ShieldUtil.GetMAC());
         if (SupRes == null) {
             print("No internet connection!");
         } else {
             McZMSConnect(SupRes);
         }
     }
     
     private static void print(String str) {
         Debug.Logger(str);
     }
     
     private static void dlprint(String str) {
         Debug.DiagLog(str);
     }
 }
