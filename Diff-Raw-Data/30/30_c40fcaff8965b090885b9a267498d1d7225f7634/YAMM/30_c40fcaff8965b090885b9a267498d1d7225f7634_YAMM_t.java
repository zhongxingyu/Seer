 /*  YAMM.java - main class
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.yamm;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 import javax.swing.*;
 import javax.swing.tree.*;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 
 
 import org.gjt.fredde.yamm.mail.*;
 import org.gjt.fredde.yamm.gui.*;
 import org.gjt.fredde.util.gui.MsgDialog;
 import org.gjt.fredde.util.gui.SplashScreen;
 import org.gjt.fredde.yamm.gui.main.*;
 import org.gjt.fredde.yamm.encode.*;
 
 /**
  * The big Main-class of YAMM
  */
 public class YAMM extends JFrame implements HyperlinkListener
 {
   /** To get the Language strings for buttons, menus, etc... */
   static protected ResourceBundle         res;
 
   /** The box the user has selected. */
   public String                 selectedbox  = System.getProperty("user.home") + "/.yamm/boxes/inbox";
 
   /** The version of YAMM */
   public static    String                 yammVersion  = "0.7.2";
 
   /** The compileDate of YAMM */
   public static    String                 compDate     = "19990409";
 
   /** the file that contains the current mail */
   public String		  mailPageString   = "file:///" + System.getProperty("user.home") + "/.yamm/tmp/";
   public URL	          mailPage; //     = new URL("file:/" + System.getProperty("user.home") + "/.yamm/tmp/mail.html");
   public boolean	  mailName = false;
 
   /** The encryption key */
   public static    char[]                 encKey       = "myKey".toCharArray();
 
   /** The vector containing the attaced files. */
   static protected Vector                 attach;
 
   /** The theme to use */
 //  static protected String                 theme        = "default";
 
   /** To check if the user has sun.misc.Base64Decoder */
   static protected boolean                base64       = false;
 
   /** The vector containing the mails of a box */
   public Vector                 listOfMails  = new Vector();
 
   /** The properties to get configuration stuff from */
   static protected Properties             props        = new Properties();
 
   /** What's in the tree */
   static protected DefaultMutableTreeNode top;
 
   /** If it should sort 1 to 10 or 10 to 1*/
   static protected boolean                firstSort    = true;
 
   /** Which column that was sorted */
   static protected int                    sortedCol    = 0;
 
   /** The Table that lists the mails in listOfMails */
   public JTable       mailList;
 
   /** The JEditorPane for this frame */
   public JEditorPane  mail;
 
   JButton      b;
   BorderLayout blayout;
   JLabel       myLabel;
   JTextField   myTextField;
   JPopupMenu   myPopup;
   JPopupMenu   treepop;
   JSplitPane   SPane, SPane2;
   JTree        tree;
   mainToolBar  tbar;
   JProgressBar pBar;
   JTabbedPane  JTPane;
   JList        myList;
 
   int          mainx, mainy, mainw, mainh, hsplit, vsplit;
 
   /**
    * Encrypts the String provided with the key specified in encKey.
    * @param encs The String to Encrypt
    */
   public static String encrypt(String encs) {
     int i, j=0, c;
     char enc[] = encs.toCharArray();
 
     for(i=0;i < enc.length;i++) {
       c = (enc[i]-32)+(encKey[j]-32);
 
       if(c >= 0 && c <= 94) enc[i] = (char)(c+32);
       else enc[i] = (char)((c%95)+32);
 
       if((j +1) == encKey.length) j = 0;
       else j++;
     }
     return new String(enc);
   }
 
   /**
    * Decrypts the String provided with the key specified in encKey.
    * @param encs The String to decrypt
    */
   public static String decrypt(String encs) {
     int i, j=0, c;
     char enc[] = encs.toCharArray();
 
     for(i=0;i < enc.length;i++) {
       c = (enc[i] - encKey[j]);
 
       if(c < 0) enc[i] = (char)(c+95+32);
       else enc[i] = (char)(c+32);
 
       if(encKey.length == (j+1)) j = 0;
       else j++;
     }
     return new String(enc);
   }
 
   /**
    * Creates the main-window and adds all the components in it.
    */
   public YAMM() {
     Mailbox.getMail(selectedbox, 0, attach, mailName);
     try { mailPage = new URL(mailPageString + mailName + ".html"); }
     catch (MalformedURLException mue) { System.err.println(mue); }
 
     System.out.println("Locale: " + Locale.getDefault());
 
     boolean result = false;
     System.out.println("Checking for sun classes...");
 
     // check for sun classes
     if(ClassLoader.getSystemResource("sun/misc/BASE64Decoder.class") == null) { result = false; }
     else { result = true; base64 = true; }
     System.out.println("sun.misc.BASE64Decoder: " + ((result) ? "yes" : "no"));
 
 
     // load the config
     try {
       InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
       props.load(in);
       in.close();
     } catch (IOException propsioe) { System.err.println(propsioe); }
 //    Locale.setDefault(new Locale(props.getProperty("", (Locale.getDefault()).toString())));
 //    theme = props.getProperty("theme", "default");
 
     // get the main window's settings and default them if an exception is caught 
     mainx = Integer.parseInt(props.getProperty("mainx", "0"));
     mainy = Integer.parseInt(props.getProperty("mainy", "0"));
     mainw = Integer.parseInt(props.getProperty("mainw", "550"));
     mainh = Integer.parseInt(props.getProperty("mainh", "400"));
     hsplit = Integer.parseInt(props.getProperty("hsplit", "150"));
     vsplit = Integer.parseInt(props.getProperty("vsplit", "125"));
 
     setBounds(mainx, mainy, mainw, mainh);
     getContentPane().setLayout(new BorderLayout());
 
 /*
     // if you want to redirekt the err stream to a file.
     // good for debugging
     try {
       FileOutputStream err = new FileOutputStream("stderr.log");
       PrintStream errPrintStream = new PrintStream(err);
       System.setErr(errPrintStream);
     }
     catch (IOException ioe) {
       System.out.println(ioe);
     }
 */
 
     // get the resources to use
     try {
       res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
     }
     catch (MissingResourceException mre) {
       mre.printStackTrace();
       System.exit(1);
     }
 
     // the menubar
     setJMenuBar(new mainMenu(this));
 
     // the toolbar
     tbar = new mainToolBar(this);
     getContentPane().add("North", tbar);
 
     // create a list of mails in the selected box
     Mailbox.createList(selectedbox, listOfMails);
 
     // handles some stuff for the table
     TableModel dataModel = new AbstractTableModel() {
       final String headername[] = { "#",res.getString("SUBJECT"), res.getString("FROM"), res.getString("DATE") };
 
       public int getColumnCount() { return 4; }
       public int getRowCount() {  return  listOfMails.size(); }
       public Object getValueAt(int row, int col) { return  ((Vector)listOfMails.elementAt(row)).elementAt(col); }
 
       public boolean isCellEditable(int col) { return false; }
       public String getColumnName(int column) {  return headername[column]; }
     };
 
     mailList = new JTable(dataModel);
     TableColumn column = null;
     column = mailList.getColumnModel().getColumn(3);
     column.setPreferredWidth(100);
     column.setMaxWidth(100);
     column.setMinWidth(10);
     column = mailList.getColumnModel().getColumn(0);
     column.setPreferredWidth(20);
     column.setMinWidth(10);
     column.setMaxWidth(30);
 
     mailList.setFont(new Font("Serif", 0, 12));
     mailList.setRowHeight(14);
     mailList.setSelectionMode(2);
     mailList.setColumnSelectionAllowed(false);
     mailList.setShowHorizontalLines(false);
     mailList.setShowVerticalLines(false);
 
     TMListener(mailList);
     mailList.addMouseListener(mouseListener);
 
     myPopup = new JPopupMenu("Test");
     myPopup.setInvoker(mailList);
 
     // adds items to the myPopup JPopupMenu 
     createMLPopup(myPopup);
 
     mail = new JEditorPane();
     
 //    mail = new yammJEditorPane();
     mail.setContentType("text/html");
     attach = new Vector();
     Mailbox.getMail(selectedbox, 0, attach, mailName);
     try { mail.setPage(mailPage); }
     catch (IOException ioe) { System.err.println(ioe); }
 //    mail.setText(mbox.getMail(selectedbox, 0, attach)); // sets the text to mail 0 in current selected box
     mail.setEditable(false);
     mail.addHyperlinkListener(this);
 //    mail.setLineWrap(true);
 //    mail.setWrapStyleWord(true);
 
     JTPane = new JTabbedPane(JTabbedPane.BOTTOM);
     JTPane.addTab(res.getString("MAIL"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/mail.gif"), new JScrollPane(mail, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
 
     class MyCellRenderer extends JLabel implements ListCellRenderer {
       protected boolean selected;
 
       public Component getListCellRendererComponent(
         JList   list,
         Object  value,
         int     index,
         boolean selected,
         boolean cellHasFocus) {
 
         String s = value.toString();
         setText(s);
 
         String end = s;
 
         if(end.indexOf(".") != -1) end = end.substring(end.lastIndexOf(".") + 1, end.length());
 
         if(end.equalsIgnoreCase("gif") || end.equalsIgnoreCase("jpg") || end.equalsIgnoreCase("bmp") || end.equalsIgnoreCase("xpm") || end.equalsIgnoreCase("jpeg") || end.equalsIgnoreCase("xcf") || end.equalsIgnoreCase("png"))
           setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/image.gif"));
 
         else if(end.equalsIgnoreCase("htm") || end.equalsIgnoreCase("html") || end.equalsIgnoreCase("txt") || end.equalsIgnoreCase("doc") || end.equalsIgnoreCase("c") || end.equalsIgnoreCase("cc") || end.equalsIgnoreCase("cpp") || end.equalsIgnoreCase("h") || end.equalsIgnoreCase("java"))
           setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/text.gif"));
 
         else if(end.equalsIgnoreCase("wav") || end.equalsIgnoreCase("au") || end.equalsIgnoreCase("mp3") || end.equalsIgnoreCase("mod") || end.equalsIgnoreCase("xm") || end.equalsIgnoreCase("midi") || end.equalsIgnoreCase("mid"))
           setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/sound.gif"));
 
         else if(end.equalsIgnoreCase("zip") || end.equalsIgnoreCase("jar") || end.equalsIgnoreCase("rpm") || end.equalsIgnoreCase("deb") || end.equalsIgnoreCase("arj") || end.equalsIgnoreCase("gz") || end.equalsIgnoreCase("tar") || end.equalsIgnoreCase("tgz") || end.equalsIgnoreCase("z"))
           setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/packed.gif"));
 
         else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/types/unknown.gif"));
 
         this.selected = selected;
         return this;
       }
     /**
       * paint is subclassed to draw the background correctly.  JLabel
       * currently does not allow backgrounds other than white, and it
       * will also fill behind the icon.  Something that isn't desirable.
       */
     public void paint(Graphics g) {
 	Color            bColor;
 	Icon             currentI = getIcon();
 
 	if(selected)
             bColor = new Color(204, 204, 255);
 	else if(getParent() != null)
             // Pick background color up from parent (which will come from the JTree we're contained in).
 	    bColor = getParent().getBackground();
 	else bColor = getBackground();
 	g.setColor(bColor);
 
 	if(currentI != null && getText() != null) {
 	    int          offset = (currentI.getIconWidth() + getIconTextGap());
 	    g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
 	}
 	else g.fillRect(0, 0, getWidth()-1, getHeight()-1);
 	super.paint(g);
       }
     }
 
     JPanel myPanel = new JPanel(new BorderLayout());
 
     Box hori1 = Box.createHorizontalBox();
 
     b = new JButton(res.getString("VEXT"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/search.gif"));
     b.setToolTipText(res.getString("VEXT"));
     b.addActionListener(BListener);
     hori1.add(b);
 
     attach = new Vector();
 
     ListModel attachModel = new AbstractListModel() {
       public Object getElementAt(int index) { return ((Vector)attach.elementAt(index)).elementAt(1); }
       public int    getSize() { return attach.size(); }
     };
 
     myList = new JList(attachModel);
     myList.setCellRenderer(new MyCellRenderer());
     myPanel.add("Center", myList);
     myPanel.add("South", hori1);
 
     Border ram = BorderFactory.createEtchedBorder();
     myList.setBorder(ram);
 
     JTPane.addTab(res.getString("ATTACH"), new ImageIcon("org/gjt/fredde/yamm/images/buttons/attach.gif"), myPanel);
 
     SPane = new JSplitPane(0, new JScrollPane(mailList), JTPane);
 
     SPane.setDividerLocation(hsplit);
     SPane.setBackground(Color.white);
     SPane.setMinimumSize(new Dimension(0, 0));
     SPane.setPreferredSize(new Dimension(300, 50));
 //    SPane.setOpaque(true);  // makes it look nicer/uglier
 
 
     top = new DefaultMutableTreeNode("Mail Boxes");
     tree = new mainJTree(this, top, tbar);
 
     SPane2 = new JSplitPane(1, tree, SPane);
     SPane2.setDividerLocation(vsplit);
     getContentPane().add("Center", SPane2);
 
     addWindowListener(new FLyssnare());
     show();
   }
 
   void SortFirst(int row) {
     for(int i = 0; i<listOfMails.size();i++) {
       Object temp = null;
       for (int j=0; j<listOfMails.size(); j++) {
         if (((Vector)listOfMails.elementAt(i)).elementAt(row).toString().compareTo(((Vector)listOfMails.elementAt(j)).elementAt(row)) > 0 ) {
            temp = listOfMails.elementAt(j);
            listOfMails.setElementAt(listOfMails.elementAt(i), j);
            listOfMails.setElementAt(temp, i);
         }
       }
     }
     mailList.updateUI();
   }
 
   void SortLast(int row) {
     for(int i = 0; i<listOfMails.size();i++) {
       Object temp = null;
       for (int j=0; j<listOfMails.size(); j++) {
         if (((Vector)listOfMails.elementAt(i)).elementAt(row).toString().compareTo(((Vector)listOfMails.elementAt(j)).elementAt(row)) < 0 ) {
            temp = listOfMails.elementAt(i);
            listOfMails.setElementAt(listOfMails.elementAt(j), i);
            listOfMails.setElementAt(temp, j);
         }
       }
     }
     mailList.updateUI();
   }
 
   void createMLPopup(JPopupMenu jpmenu) {
     Vector list = new Vector();
 
     fileList(list, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
     StringTokenizer tok;
 
     JMenu copy = new JMenu(res.getString("COPY")), move = new JMenu(res.getString("MOVE"));
     JMenuItem row, delete = new JMenuItem(res.getString("DELETE")), reply = new JMenuItem(res.getString("REPLY"));
 
     delete.addActionListener(OtherMListener);
     reply.addActionListener(OtherMListener);
 
     for(int i = 0;i<list.size();i++) {
       if(!(list.get(i).toString()).equals(selectedbox) && !(list.get(i).toString()).endsWith(".config")) {
         String temp = list.get(i).toString();
 
         tok = new StringTokenizer(temp, System.getProperty("file.separator"));
         String name = tok.nextToken();
 
         row = new JMenuItem(list.get(i).toString());
         row.addActionListener(KMListener);
         copy.add(row);
 
         row = new JMenuItem(list.get(i).toString());
         row.addActionListener(FMListener);
         move.add(row);
       }
     }
     jpmenu.add(reply);
     jpmenu.addSeparator();
     jpmenu.add(copy);
     jpmenu.add(move);
     jpmenu.add(delete);
   }
 
   /**
    * Checks if the link is a mailto:-link.
    * If it is, it starts a write-window with the specified mailto:-address,
    * if not, it starts the program that handles this filetype.
    */
   public void hyperlinkUpdate(HyperlinkEvent e) {
     if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 
       if((e.getURL().toString()).startsWith("mailto:")) {
         String link = e.getURL().toString();
         new YAMMWrite(link.substring(link.indexOf("mailto:") + 7, link.length()));
       }
 
       else {
         try{ Runtime.getRuntime().exec("start " + e.getURL()); }
         catch(IOException ioe) { new MsgDialog(this, "Error!", ioe.toString()); }
       }        
     }
     else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
       mail.setCursor(new Cursor(Cursor.HAND_CURSOR));
     }
     else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
       mail.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
     }
   }
  
   private void fileList(Vector vect, File f) {
     if((f.toString()).equals(System.getProperty("user.home") + "/.yamm/boxes")) {
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         fileList(vect, new File(f, list[i]));
     }
       
     else if(f.isDirectory()) {
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         fileList(vect, new File(f, list[i]));
     }
     else {
       vect.add(f.toString());
     }
   }
 
   class newBoxDialog extends JDialog {
     JButton    b;
     JComboBox  inFolder;
     JTextField jtfield;
 
     public newBoxDialog(JFrame frame) {
       super(frame, true);
       setBounds(0, 0, 300, 100);
       setResizable(false);
 
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
       getContentPane().setLayout(new GridLayout(3, 2, 2, 2));
 
 
       File[] boxes = new File(System.getProperty("user.home") + "/.yamm/boxes/").listFiles();
       Vector bvector = new Vector();
       bvector.add("boxes");
 
       for(int i=0;i<boxes.length;i++) {
         String tmp = boxes[i].toString();
         String box = tmp.substring(tmp.indexOf("boxes") -1, tmp.length());
 
         if(boxes[i].isDirectory()) bvector.add(box);
       }
 
       getContentPane().add(new JLabel(res.getString("NAME")));
 
       jtfield = new JTextField();
       getContentPane().add(jtfield);
 
       getContentPane().add(new JLabel(res.getString("CREATEIN")));
 
       inFolder = new JComboBox(bvector);
       getContentPane().add(inFolder);
 
 
       b = new JButton(res.getString("OK"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
 
       b = new JButton(res.getString("CANCEL"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
 
       show();
     }
 
     ActionListener BListener2 = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         String arg = ((JButton)e.getSource()).getText();
 
         File box = new File(System.getProperty("user.home") + "/.yamm/" + inFolder.getSelectedItem() + "/" + jtfield.getText());
         if(arg.equals(res.getString("OK"))) {
           if(box.exists()) System.out.println(box.toString() + " exists!");
 
           else {
             try {
               box.createNewFile();
             } catch (IOException ioe) { System.out.println(ioe); }
 /*
             top.removeAllChildren();
             createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash")));
             tree.updateUI();
 */
             dispose();
           }
         }
 
         else if(arg.equals(res.getString("CANCEL"))) {
           dispose();
         }
       }
     };
   }
 
   ActionListener KMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
 
       int i = 0;
 
       while(i<4) {
         if(mailList.getColumnName(i).equals("#")) { break; }
         i++;
       }
 
 
       int[] mlist = mailList.getSelectedRows();
       int[] copyList = new int[mlist.length];
 
       for(int j = 0;j < mlist.length;j++) {
         copyList[j] = Integer.parseInt(mailList.getValueAt(mlist[j], i).toString());
       }
 
       for (int a=0; a<copyList.length; a++ ) {
         Mailbox.copyMail(selectedbox, kommando, copyList[a]);
       }
           
       tree.updateUI();
     }
   };
 
   ActionListener FMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
 
       int i = 0;
 
       while(i<4) {
         if(mailList.getColumnName(i).equals("#")) { break; }
         i++;
       }
 
 
       int[] mlist = mailList.getSelectedRows();
       int[] moveList = new int[mlist.length];
 
       for(int j = 0;j < mlist.length;j++) {
         moveList[j] = Integer.parseInt(mailList.getValueAt(mlist[j], i).toString());
       }
 
       for (int a=moveList.length -1; a>=0; a-- ) {
         Mailbox.moveMail(selectedbox, kommando, moveList[a]);
       }
           
       tree.updateUI();
 
       Mailbox.createList(selectedbox, listOfMails);
 
       mailList.updateUI();
       attach = new Vector();
 
       if(mailName) mailName = false;
       else mailName = true;
 
       Mailbox.getMail(selectedbox, mailList.getSelectedRow(), attach, mailName);
 
       try { mailPage = new URL(mailPageString + mailName + ".html"); }
       catch (MalformedURLException mue) { System.err.println(mue); }
 
       try { mail.setPage(mailPage); }
       catch (IOException ioe) { System.err.println(ioe); }    }
   };
 
   ActionListener OtherMListener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
 
       int i = 0;
 
       while(i<4) {
         if(mailList.getColumnName(i).equals("#")) { break; }
         i++;
       }
 
       if(kommando.equals(res.getString("DELETE"))) {
 
         int[] mlist = mailList.getSelectedRows();
         int[] deleteList = new int[mlist.length];
 
         for(int j = 0;j < mlist.length;j++) {
           deleteList[j] = Integer.parseInt(mailList.getValueAt(mlist[j], i).toString());
         }
 
         Arrays.sort(deleteList);
 
         for (int a=deleteList.length -1; a>=0; a-- ) {
           Mailbox.deleteMail(selectedbox, deleteList[a]);
         }
           
         tree.updateUI();
 
         Mailbox.createList(selectedbox, listOfMails);
 
         mailList.updateUI();
         attach = new Vector();
 
         if(mailName) mailName = false;
         else mailName = true;
 
         Mailbox.getMail(selectedbox, mailList.getSelectedRow(), attach, mailName);
 
         try { mailPage = new URL(mailPageString + mailName + ".html"); }
         catch (MalformedURLException mue) { System.err.println(mue); }
 
         try { mail.setPage(mailPage); }
         catch (IOException ioe) { System.err.println(ioe); }
       }
       else if(kommando.equals(res.getString("REPLY"))) {
                         
         String[] mail = Mailbox.getMailForReplyHeaders(selectedbox, Integer.parseInt(mailList.getValueAt(mailList.getSelectedRow(), i).toString()));
 
         YAMMWrite yam = new YAMMWrite(mail[0], mail[1], mail[0] + " " + res.getString("WROTE") + "\n");
         Mailbox.getMailForReply(selectedbox, Integer.parseInt(mailList.getValueAt(mailList.getSelectedRow(), i).toString()), yam.myTextArea);	
       }
     }
   };
 
   ActionListener BListener = new ActionListener() {
     public void actionPerformed(ActionEvent e) {
       String arg = ((JButton)e.getSource()).getToolTipText();
      
       if(arg.equals(res.getString("VEXT"))) {
 
         int whichAtt = myList.getSelectedIndex();
 
         if(whichAtt != -1) {
           String filename = ((Vector)attach.elementAt(whichAtt)).elementAt(1).toString();
           String encode = ((Vector)attach.elementAt(whichAtt)).elementAt(0).toString();
           String home = System.getProperty("user.home") + "/.yamm";
 
           System.out.println("encode: " + encode + " filename: " + filename);
           if(filename.endsWith(".jpg") || filename.endsWith(".gif") || filename.endsWith(".JPG") || filename.endsWith(".GIF")) {
             new File("tmp").mkdir();
 
             if(encode.equalsIgnoreCase("base64")) {
               if(base64) {
                 Mailbox.export_attach(selectedbox, mailList.getSelectedRow(), filename);
    
                 new Base64Decode("B64Decode " + filename, new File(home + "/tmp/" + filename)).start();
               }
               else new MsgDialog("No Support for base64 encoded files..."); // System.out.println("No support for base64 encoded files...");
             }
 
             if(encode.equalsIgnoreCase("x-uuencode")) {
               Mailbox.export_attach(selectedbox, mailList.getSelectedRow(), filename);
             
               new UUDecode(null, "UUDecode " + filename, home + "/tmp/" + filename, true).start();
             }
           }
 
           else if(encode.equalsIgnoreCase("base64")) {
             if(base64) {
               Mailbox.export_attach(selectedbox, mailList.getSelectedRow(), filename);
               JFileChooser jfs = new JFileChooser();
               jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
               jfs.setMultiSelectionEnabled(false);
               jfs.setSelectedFile(new File(filename));
               int ret = jfs.showSaveDialog(null);
     
               if(ret == JFileChooser.APPROVE_OPTION) {
                 if(jfs.getSelectedFile() != null) {
                   new Base64Decode("B64Decode " + filename, jfs.getSelectedFile()).start();
                 }
               }
             }
             else System.out.println("No support for base64 encoded files...");
           }
           else if(encode.equalsIgnoreCase("x-uuencode")) {
             Mailbox.export_attach(selectedbox, mailList.getSelectedRow(), filename);
             JFileChooser jfs = new JFileChooser();
             jfs.setFileSelectionMode(JFileChooser.FILES_ONLY);
             jfs.setMultiSelectionEnabled(false);
             jfs.setSelectedFile(new File(filename));
             int ret = jfs.showSaveDialog(null);
             
             if(ret == JFileChooser.APPROVE_OPTION) {
               if(jfs.getSelectedFile() != null) {
                 new UUDecode(null, "UUDecode " + filename, jfs.getSelectedFile().toString(), false).start();
               }
             }
           }  
         }
       }
     }
   };
 
 
   MouseListener mouseListener = new MouseAdapter() {
     public void mouseReleased(MouseEvent me) {
       if(me.isPopupTrigger()) myPopup.show(mailList, me.getX(), me.getY());
       else get_mail();
       if(mailList.getSelectedRow() != -1 && !(mailList.getSelectedRow() >= listOfMails.size())) { ((JButton)tbar.reply).setEnabled(true); ((JButton)tbar.print).setEnabled(true); ((JButton)tbar.forward).setEnabled(true);}
       else { ((JButton)tbar.reply).setEnabled(false); ((JButton)tbar.forward).setEnabled(false); ((JButton)tbar.print).setEnabled(false); }
     }
     public void mousePressed(MouseEvent me) {
       if(me.isPopupTrigger()) myPopup.show(mailList, me.getX(), me.getY());
     }
     
     void get_mail() {
       int i = 0;
 
       while(i<4) {
         if(mailList.getColumnName(i).equals("#")) { break; }
         i++;
       }
       int whatMail = Integer.parseInt(mailList.getValueAt(mailList.getSelectedRow(), i).toString());
 
       attach = new Vector();
         if(mailName) mailName = false;
         else mailName = true;
 
         Mailbox.getMail(selectedbox,whatMail, attach, mailName);
         try { mailPage = new URL(mailPageString + mailName + ".html"); }
         catch (MalformedURLException mue) { System.err.println(mue); }
 
         try { mail.setPage(mailPage); }
         catch (IOException ioe) { System.err.println(ioe); }
 //        if(mailName) mailName = false;
 //        else mailName = true;
 //      mail.setText(mbox.getMail(selectedbox, whatMail, attach));
       myList.updateUI();
     }
   };
 
   void TMListener(JTable table) {
     final JTable tableView = table;
     tableView.setColumnSelectionAllowed(false);
 
     MouseAdapter lmListener = new MouseAdapter() {
       public void mouseClicked(MouseEvent e) {
           TableColumnModel columnModel = tableView.getColumnModel();
           int viewColumn = columnModel.getColumnIndexAtX(e.getX());
           int column = tableView.convertColumnIndexToModel(viewColumn);
 
           if(column != -1) {
             if(column == sortedCol) {
               if(firstSort) {
                 SortFirst(column);
                 firstSort = false;
               }
               else {
                 SortLast(column);
                 firstSort = true;
               }
             }
             else {
               SortFirst(column);
               firstSort = false;
               sortedCol = column;
             }
           }
       }
     };
     JTableHeader th = tableView.getTableHeader();
     th.addMouseListener(lmListener);
   }
 
   class FLyssnare extends WindowAdapter {
     public void windowClosing(WindowEvent event) {
         Rectangle rv = new Rectangle();
         getBounds(rv);
 
         try {
           InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
           props.load(in);
           in.close();
         } catch (IOException propsioe) { System.err.println(propsioe); }
 
         props.setProperty("mainx", new Integer(rv.x).toString());
         props.setProperty("mainy", new Integer(rv.y).toString());
         props.setProperty("mainw", new Integer(rv.width).toString());
         props.setProperty("mainh", new Integer(rv.height).toString());
         props.setProperty("vsplit", new Integer(SPane2.getDividerLocation()).toString());
         props.setProperty("hsplit", new Integer(SPane.getDividerLocation()).toString());
 
 
         try {
           OutputStream out = new FileOutputStream(System.getProperty("user.home") + "/.yamm/.config");
           props.store(out, "YAMM configuration file");
           out.close();
         } catch(IOException propsioe) { System.err.println(propsioe); }
 
         dispose();
         System.exit(0);
     }
   }
 
   /**
    * Shows a SplashScreen while starting the program.
    * It checks if the user has config-files, if not it'll
    * create such and write a little welcome message in
    * ~home/.yamm/boxes/local/inbox.
    */
   public static void main(String argv[])
   {
     SplashScreen splash = new SplashScreen("YAMM " + yammVersion + " Copyright (c) 1999 Fredrik Ehnbom", "org/gjt/fredde/yamm/images/logo.gif");
     YAMM nFrame = null;
 
     if(!(new File(System.getProperty("user.home") + "/.yamm/boxes")).exists()) {
       try {
         String home = System.getProperty("user.home");
        String user = System.getProperty("user.name");
         SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
         InetAddress myInetaddr = InetAddress.getLocalHost();
         String host = myInetaddr.getHostName();
 
        if(host == null) host = "localhost";
 
        (new File(home + "/.yamm/servers")).mkdirs();
        (new File(home + "/.yamm/boxes")).mkdirs();
         PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(home + "/.yamm/boxes/inbox")));
 
         out.println("Date: " + dateFormat.format(new Date()));
        out.println("From: Fredrik Ehnbom <fredde@gjt.org>");
        out.println("To: " + user + "@" + host);
         out.println("Subject: Welcome to YAMM " + yammVersion);
        out.println("\n<html>\nWelcome " + user + "!");
        out.println("\nFeel free to <a href=\"mailto:fredde@gjt.org\">mail me</a> your questions, comments, suggestions or bug-reports.");
         out.println("For the bug-report, try to send me as much info as possible. (regarding the");
         out.println("bug, not what you had for breakfast that day! ;) )");
        out.println("\nBest regards,\n\nFredrik Ehnbom\n</html>\n.\n");
         out.close();
 
         (new File(home + "/.yamm/boxes/outbox")).createNewFile();
         (new File(home + "/.yamm/boxes/trash")).createNewFile();
         (new File(home + "/.yamm/.config")).createNewFile();
         (new File(home + "/.yamm/.filters")).createNewFile();
         (new File(home + "/.yamm/tmp")).mkdirs();
       } catch(IOException ioe) { new MsgDialog(nFrame, "Error!", ioe.toString()); }
     }
     String result;
 
     nFrame = new YAMM();
     nFrame.setTitle("Yet Another Mail Manager " + yammVersion);
     splash.dispose();
   }
 }
 
