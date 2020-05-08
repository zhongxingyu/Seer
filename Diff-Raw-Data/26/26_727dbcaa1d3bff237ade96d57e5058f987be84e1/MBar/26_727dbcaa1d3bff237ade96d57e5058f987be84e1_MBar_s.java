 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.HashMap;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import cmd.CmdAutoWrap;
 import cmd.CmdIndent;
 import cmd.CmdInsert;
 import cmd.CmdNew;
 import cmd.CmdOpen;
 import cmd.CmdRedo;
 import cmd.CmdSave;
 import cmd.CmdSaveAs;
 import cmd.CmdUndo;
 import cmd.CmdWellFormed;
 import cmd.Command;
 import cmd.Context;
 import construct.Ahref;
 import construct.Bold;
 import construct.Header;
 import construct.Img;
 import construct.Italic;
 import construct.List;
 import construct.Strikethrough;
 import construct.Table;
 import construct.Underline;
 
 public class MBar extends JMenuBar
 {
     private static final long serialVersionUID = 8343344753778048592L;
 
     private HashMap<Class<?>, Command> commands;
 
     private Component parent;
     
     public MBar(Component parent)
     {
         this.parent = parent;
         
         // File Menu
         JMenu muFile = new JMenu("File");
 
         // File : Open
         JMenuItem miOpen = new JMenuItem("Open");
         
         miOpen.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_O,
                 InputEvent.CTRL_DOWN_MASK));
         
         miOpen.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 JFileChooser c = new JFileChooser("~/");
                 
                 c.setMultiSelectionEnabled(false);
                 
                 
                 int rv = c.showOpenDialog(MBar.this.parent);
                 
                 if (rv == JFileChooser.APPROVE_OPTION)
                 {
                     File f = c.getSelectedFile();
                     Context con = new Context();
                     con.setFile(f);
                     
                     commands.get(CmdOpen.class).execute(con);
                 }
             }
         });
         muFile.add(miOpen);
         
         // File : New
         JMenuItem miNew = new JMenuItem("New");
         miNew.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_N,
                 InputEvent.CTRL_DOWN_MASK));
         
         miNew.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 commands.get(CmdNew.class).execute(new Context());
             }
         });
         muFile.add(miNew);
         
         // File : Save
         JMenuItem miSave = new JMenuItem("Save");
         
         miSave.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_S,
                 InputEvent.CTRL_DOWN_MASK));
         
         miSave.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 commands.get(CmdSave.class).execute(c);
             }
         });
         muFile.add(miSave);
         
         // File : Save As
         JMenuItem miSaveAs = new JMenuItem("Save As");
         
         miSaveAs.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_S,
                 InputEvent.SHIFT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK));
         
         miSaveAs.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 JFileChooser c = new JFileChooser("~/");
                 
                 c.setMultiSelectionEnabled(false);
                 
                 int rv = c.showSaveDialog(MBar.this.parent);
                 
                 if (rv == JFileChooser.APPROVE_OPTION)
                 {
                     File f = c.getSelectedFile();
                     Context con = new Context();
                     con.setFile(f);
                     
                     commands.get(CmdSaveAs.class).execute(con);
                 }
             }
         });
         muFile.add(miSaveAs);
 
         // add file menu to the bar
         MBar.this.add(muFile);
 
         // Format Menu
         JMenu muFormat = new JMenu("Format");
         
         // Format : Indent
         JMenuItem miIndent = new JMenuItem("Indent");
         
         miIndent.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_I,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
         
         miIndent.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setboolean(false);
                 commands.get(CmdIndent.class).execute(c);
             }
         });
         muFormat.add(miIndent);
         
         // Format : Indent All
         JMenuItem miIndentAll = new JMenuItem("Indent All");
         
         miIndentAll.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_I,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
         
         miIndentAll.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setboolean(true);
                 commands.get(CmdIndent.class).execute(c);
             }
         });
         muFormat.add(miIndentAll);
 
         // Format : IsWellFormed
         JMenuItem miWellFormed = new JMenuItem("Check Well Formed");
         
         miWellFormed.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_W,
                 InputEvent.CTRL_DOWN_MASK));
         
         miWellFormed.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 commands.get(CmdWellFormed.class).execute(new Context());
             }
         });
         muFormat.add(miWellFormed);
         
         //Format : Undo
         JMenuItem miUndo = new JMenuItem("Undo");
         
         miUndo.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_Z,
                 InputEvent.CTRL_DOWN_MASK));
         
         miUndo.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 commands.get(CmdUndo.class).execute(new Context());
             }
         });
         muFormat.add(miUndo);
         
         //Format : Redo
         JMenuItem miRedo = new JMenuItem("Redo");
         
         miRedo.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_Y,
                 InputEvent.CTRL_DOWN_MASK));
         
         miRedo.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 commands.get(CmdRedo.class).execute(new Context());
             }
         });
         muFormat.add(miRedo);
         
         // Format : Word Wrap
         JMenuItem miWordWrap = new JMenuItem("Toggle Word Wrap");
         
         miWordWrap.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_W,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
         
         miWordWrap.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 commands.get(CmdAutoWrap.class).execute(new Context());
             }
         });
         muFormat.add(miWordWrap);
 
         // add format menu to the bar
         MBar.this.add(muFormat);
 
         // Insert Menu
         JMenu muInsert = new JMenu("Insert");
         
         // Insert : A/Link
         JMenuItem miAhref = new JMenuItem("a href");
         miAhref.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_A,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
         
         miAhref.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 getUrlForTag();
             }
         });
         muInsert.add(miAhref);
        
         // Insert : Image
         JMenuItem miImg = new JMenuItem("Image");
         
         miImg.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_I,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
         
         miImg.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 getImageResource();
             }
         });
         muInsert.add(miImg);
         
         // Insert : Header
         JMenuItem miHeader = new JMenuItem("Header");
         
         miHeader.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_H,
                 InputEvent.CTRL_DOWN_MASK));
         
         miHeader.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 getHeaderLevel();
             }
         });
         muInsert.add(miHeader);
 
         // Insert : List
         JMenuItem miList = new JMenuItem("List");
         
         miList.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_L,
                 InputEvent.CTRL_DOWN_MASK));
         
         miList.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 getListProperties();
             }
         });
         
         muInsert.add(miList);
        
         // Insert : Table
         JMenuItem miTable = new JMenuItem("Table");
         
         miTable.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_T,
                 InputEvent.CTRL_DOWN_MASK));
         
         miTable.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 getTableSize();
             }
         });
         muInsert.add(miTable);
         
         // Text SubMenu
         JMenu muText = new JMenu("Text");
        
         // Sub Text : Bold 
         JMenuItem miBold = new JMenuItem("Bold");
         
         miBold.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_B,
                 InputEvent.CTRL_DOWN_MASK));
         
         miBold.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Bold());
                 commands.get(CmdInsert.class).execute(c);
             }
         });
         muText.add(miBold);
         
         // Sub Text : Italic
         JMenuItem miItalic = new JMenuItem("Italic");
         
         miItalic.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_I,
                 InputEvent.CTRL_DOWN_MASK));
         
         miItalic.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Italic());
                 commands.get(CmdInsert.class).execute(c);
             }
         });
         muText.add(miItalic);
         
         // Sub Text : Strikethrough
         JMenuItem miStrike = new JMenuItem("Strikethrough");
         
         miStrike.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_S,
                 InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
         
         miStrike.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Strikethrough());
                 commands.get(CmdInsert.class).execute(c);
             }
         });
         muText.add(miStrike);
         
         // Sub text : Underline
         JMenuItem miUnderline = new JMenuItem("Underline");
         
         miUnderline.setAccelerator(KeyStroke.getKeyStroke(
                 KeyEvent.VK_U,
                 InputEvent.CTRL_DOWN_MASK));
         
         miUnderline.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Underline());
                 commands.get(CmdInsert.class).execute(c);
             }
         });
         muText.add(miUnderline);
         
         // add text submenu to the insert menu
         muInsert.add(muText);
 
         // add the insert menu to the bar
         MBar.this.add(muInsert);
     }
     
     /**
      * Stores commands generated by Client
      * 
      * @param commands
      *        a HashMap relating a command's class object to an instantiated
      *        version of the real Command object
      */
     public void storeCommands(HashMap<Class<?>, Command> commands)
     {
         this.commands = commands;
     }
 
     public void getUrlForTag()
     {
         final JFrame frame = new JFrame();
         frame.setLocationRelativeTo(this);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.getContentPane().setLayout(new GridLayout(2, 1));
         JPanel input = new JPanel(new GridLayout(1, 2));
         input.add(new JLabel("Enter URL: "));
         final JTextField field = new JTextField("http://");
         input.add(field);
         frame.getContentPane().add(input);
         JPanel choices = new JPanel(new GridLayout(1, 2));
         JButton cancel = new JButton("Cancel");
         JButton confirm = new JButton("Confirm");
         confirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Ahref(field.getText()));
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         cancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
         choices.add(cancel);
         choices.add(confirm);
 
         frame.getContentPane().add(choices);
         frame.pack();
         frame.setVisible(true);
 
     }
 
     public void getImageResource()
     {
         final JFrame frame = new JFrame();
         frame.setLocationRelativeTo(this);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         JTabbedPane types = new JTabbedPane();
 
         JButton cancel = new JButton("Cancel");
         JButton localcancel = new JButton("Cancel");
         JButton localConfirm = new JButton("Confirm");
         JButton urlConfirm = new JButton("Confirm");
 
         cancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
         localcancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
 
         // URL choice
         JPanel URL = new JPanel(new GridLayout(2, 1));
         JPanel input = new JPanel(new GridLayout(1, 2));
         input.add(new JLabel("Enter URL:"));
         final JTextField field = new JTextField("http://");
         urlConfirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Img(field.getText()));
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         input.add(field);
         URL.add(input);
         JPanel URLbuttons = new JPanel(new GridLayout(1, 2));
         URLbuttons.add(cancel);
         URLbuttons.add(urlConfirm);
         URL.add(URLbuttons);
         types.add(URL, "From URL");
 
         // Local choice
         JPanel local = new JPanel(new GridLayout(2, 1));
         final JButton choose = new JButton("Choose File");
         local.add(choose);
         final JFileChooser files = new JFileChooser("~/");
         files.setMultiSelectionEnabled(false);
         choose.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
 
                 files.showOpenDialog(frame);
                 if (files.getSelectedFile() != null)
                 {
                     choose.setText(files.getSelectedFile().getName());
                 }
             }
         });
         localConfirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
 
                 if (files.getSelectedFile() != null)
                 {
                     c.setHtmlconst(new Img(files.getSelectedFile().getPath()));
                 }
                 else
                 {
                     c.setHtmlconst(new Img(null));
                 }
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         JPanel localButtons = new JPanel(new GridLayout(1, 2));
         localButtons.add(localcancel);
         localButtons.add(localConfirm);
         local.add(localButtons);
         types.add(local, "From Computer");
 
         frame.getContentPane().add(types);
         frame.pack();
         frame.setVisible(true);
 
     }
 
     /**
      * Opens a GUI popup to get the header level and executes the insert command
      */
     public void getHeaderLevel()
     {
         final JFrame frame = new JFrame();
         frame.setLocationRelativeTo(this);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.getContentPane().setLayout(new GridLayout(2, 1));
         frame.setTitle("Header Level Selection");
         final JComboBox list = new JComboBox();
         for (int i = 1; i <= 7; i++)
         {
             list.addItem(i);
         }
         frame.getContentPane().add(list);
         JPanel choices = new JPanel(new GridLayout(1, 2));
         JButton cancel = new JButton("Cancel");
         JButton confirm = new JButton("Confirm");
         confirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new Header(list.getSelectedIndex() + 1));
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         cancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
         choices.add(cancel);
         choices.add(confirm);
 
         frame.getContentPane().add(choices);
         frame.pack();
         frame.setVisible(true);
 
     }
 
     /**
      * Opens a GUI pop-up to retrieve list properties, then executes CmdInsert
      * with a new list construct
      */
     public void getListProperties()
     {
         // Create a JFrame to display the option components
         final JFrame frame = new JFrame();
 
         frame.setLocationRelativeTo(this);
         frame.setTitle("List Properties");
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.getContentPane().setLayout(new GridLayout(3, 1));
 
         // Create a panel to hold the option components
         JPanel buttons = new JPanel(new GridLayout(1, 3));
 
         // This group ensures only one radio button is selected at a time
         ButtonGroup btnGroup = new ButtonGroup();
 
         // the Unordered radio button
         JRadioButton u = new JRadioButton("Unordered", true);
         u.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 listType = "u";
             }
         });
         buttons.add(u);
         btnGroup.add(u);
 
         // the Ordered radio button
         JRadioButton o = new JRadioButton("Ordered");
         o.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 listType = "o";
             }
         });
         buttons.add(o);
         btnGroup.add(o);
 
         // the Definition radio button
         JRadioButton d = new JRadioButton("Definition");
         d.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 listType = "d";
             }
         });
         buttons.add(d);
         btnGroup.add(d);
 
         // add the buttons panel to the frame
         frame.getContentPane().add(buttons);
 
         // create a panel to hold the length option
         JPanel len = new JPanel(new GridLayout(1, 2));
         len.add(new JLabel("List Length"));
 
         final JTextField length = new JTextField();
         len.add(length);
         frame.getContentPane().add(len);
 
         // create a panel to hold the choices buttons
         JPanel choices = new JPanel(new GridLayout(1, 2));
 
         JButton confirm = new JButton("Confirm");
         confirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 Context c = new Context();
                 c.setHtmlconst(new List(
                         Integer.parseInt(length.getText()),
                         listType));
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         choices.add(confirm);
 
         JButton cancel = new JButton("Cancel");
         cancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
         choices.add(cancel);
 
         frame.getContentPane().add(choices);
         frame.pack();
 
         // frame.setLocationRelativeTo(MainView.this);
         frame.setVisible(true);
     }
 
     /**
      * A string representing the type of list the user wishes to generate
      */
    private String listType = null;
 
     /**
      * Opens a GUI popup to get the dimensions for the table and executes the
      * insert command
      */
     public void getTableSize()
     {
         final int[] dimensions = new int[2];
         final JFrame frame = new JFrame();
         frame.setLocationRelativeTo(this);
         frame.getContentPane().setLayout(new BorderLayout());
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         JLabel label = new JLabel("Table Dimensions");
         frame.getContentPane().add(label, BorderLayout.NORTH);
         JPanel pane = new JPanel();
         pane.setLayout(new GridLayout(2, 2));
         JLabel len = new JLabel("Rows");
         pane.add(len);
         final JTextField rows = new JTextField();
         pane.add(rows);
         JLabel wid = new JLabel("Columns");
         pane.add(wid);
         final JTextField cols = new JTextField();
         pane.add(cols);
         frame.getContentPane().add(pane, BorderLayout.CENTER);
         JPanel buttons = new JPanel();
         buttons.setLayout(new GridLayout(1, 2));
         JButton cancel = new JButton("Cancel");
         JButton confirm = new JButton("Confirm");
         confirm.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 dimensions[0] = Integer.parseInt(rows.getText());
                 dimensions[1] = Integer.parseInt(cols.getText());
                 Context c = new Context();
                 c.setHtmlconst(new Table(dimensions[0], dimensions[1]));
                 commands.get(CmdInsert.class).execute(c);
                 frame.dispose();
             }
         });
         cancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispose();
             }
         });
 
         buttons.add(cancel);
         buttons.add(confirm);
         frame.getContentPane().add(buttons, BorderLayout.SOUTH);
         frame.setTitle("Table Size");
         frame.pack();
         frame.setVisible(true);
 
     }
 
 }
