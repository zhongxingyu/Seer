 package client;
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import java.awt.BorderLayout;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.SwingConstants;
 import javax.swing.JList;
 import javax.swing.Box;
 import javax.swing.JTextField;
 import javax.swing.JPanel;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeSelectionModel;
 
 import java.awt.Component;
 import java.awt.CardLayout;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 public class MainApp {
 
     private JFrame frame;
     private JTextField roomText;
     private JTextField type;
     private JTextField SigninText;
     private ClientConnection conn;
     public JTable roomTable;
     public JList chatList;
     public JLabel roomLabel;
     public JList userList;
     public JTree tree;
     /**
      * Launch the application.
      * @wbp.parser.entryPoint
      */
     public void init() {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     initialize();
                     frame.setVisible(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     public MainApp(ClientConnection conn) {
         this.conn = conn;
         
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         frame = new JFrame();
         frame.setBounds(100, 100, 450, 300);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().setLayout(new CardLayout(0, 0));
 
         /**
          * Go to the main app when the button is clicked
          */
         
         JPanel main_panel = new JPanel();
         frame.getContentPane().add(main_panel, "name_1367989148345403000");
         main_panel.setLayout(new BorderLayout(0, 0));
         
         JLabel title_label = new JLabel("");
         main_panel.add(title_label, BorderLayout.NORTH);
         title_label.setHorizontalAlignment(SwingConstants.CENTER);
         
         Box verticalBox = Box.createVerticalBox();
         main_panel.add(verticalBox, BorderLayout.WEST);
         
         JLabel lblNewLabel = new JLabel("Rooms");
         verticalBox.add(lblNewLabel);
         
         Box verticalBox_1 = Box.createVerticalBox();
         main_panel.add(verticalBox_1, BorderLayout.EAST);
         
         
         
         tree = new JTree();
         tree.setModel(new DefaultTreeModel(
                 new DefaultMutableTreeNode("All Users") {
                 }
             ));
         tree.getSelectionModel().setSelectionMode
             (TreeSelectionModel.SINGLE_TREE_SELECTION);
         tree.setAlignmentX(Component.LEFT_ALIGNMENT);
         tree.setVisibleRowCount(40);
         tree.setShowsRootHandles(true);
         tree.setModel(new DefaultTreeModel(
             new DefaultMutableTreeNode("All Users") {}
         ));
         
         
         JScrollPane UserScrollPane = new JScrollPane(tree);
         verticalBox_1.add(tree);
         
         
         JLabel lblPeople = new JLabel("Users in this room");
         verticalBox_1.add(lblPeople);
         
        DefaultListModel userListModel = new DefaultListModel();
        userList = new JList(userListModel);
         verticalBox_1.add(userList);
         
         JPanel chatPanel = new JPanel();
         main_panel.add(chatPanel, BorderLayout.CENTER);
         chatPanel.setLayout(new BorderLayout(0, 0));
         
         DefaultListModel chatListModel = new DefaultListModel();
         chatList = new JList(chatListModel);
 
         JScrollPane scrollPane = new JScrollPane(chatList);
         chatPanel.add(scrollPane, BorderLayout.CENTER);
         
         Box horizontalBox = Box.createHorizontalBox();
         chatPanel.add(horizontalBox, BorderLayout.SOUTH);
         
         JList list_3 = new JList();
         horizontalBox.add(list_3);
         
         JLabel lblNewLabel_1 = new JLabel("Type:");
         horizontalBox.add(lblNewLabel_1);
         
         type = new JTextField();
         type.setColumns(10);
 
         roomLabel = new JLabel("Room Name");
         chatPanel.add(roomLabel, BorderLayout.NORTH);
         
         roomText = new JTextField();
         
         roomText.setColumns(10);
   
         roomText.setMaximumSize(roomText.getPreferredSize() );
         
         
         DefaultTableModel roomModel = new DefaultTableModel();
         roomModel.setDataVector(new Object[][] {}, new Object[] { "leave", "join", "name", ""});
         roomTable = new JTable(roomModel);
         roomTable.getColumnModel().getColumn(0).setPreferredWidth(27);
         roomTable.getColumnModel().getColumn(1).setPreferredWidth(27);
         roomTable.getColumnModel().getColumn(3).setPreferredWidth(27);
 
         roomTable.getColumn("leave").setCellRenderer(new ButtonRenderer());
         roomTable.getColumn("leave").setCellEditor(
             new ButtonEditor(new JCheckBox(), roomLabel));
         roomTable.getColumn("join").setCellRenderer(new ButtonRenderer());
         roomTable.getColumn("join").setCellEditor(
             new JoinButtonEditor(new JCheckBox(), roomLabel, conn, chatList));
         //JScrollPane roomPane = new JScrollPane();
         roomText.addKeyListener(new RoomTextListener(roomTable, roomText, roomLabel, conn));
         verticalBox.add(roomText);
         verticalBox.add(roomTable);
         //verticalBox.add(roomPane);
         verticalBox.add(roomTable);
         
         
         TypeListener typeListener = new TypeListener(chatList, type, roomTable, roomLabel, conn);
         type.addKeyListener(typeListener);
         horizontalBox.add(type);
         
         
        
 
 
         
     }
 
 }
