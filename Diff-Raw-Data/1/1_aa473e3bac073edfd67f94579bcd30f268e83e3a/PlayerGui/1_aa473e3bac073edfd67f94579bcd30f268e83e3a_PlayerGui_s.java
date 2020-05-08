 package gamesincommon;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 
 public class PlayerGui {
 
   private JFrame frame;
   
   private JPanel playerPanel;
   private CardLayout playerLayout;
   
   private JPanel userDisplayPanel;
   private DefaultListModel<SteamId> playerNameModel;
   private JList<SteamId> playerNameList;
   private JButton changeUserButton;
   
   private JPanel userEnterPanel;
   private JTextField userEnterTextField;
   private ActionListener selectUserAction;
   private JButton selectUserButton = new JButton("Select user");
   
   private JPanel playerFriendsPanel;
   private DefaultListModel<SteamId> playerFriendsModel;
   private JList<SteamId> playerFriendsList;
   private JScrollPane playerFriendsScroll;
   
   private GamesInCommon gamesInCommon = new GamesInCommon();
 
   /**
    * Launch the application.
    */
   public static void main(String[] args) {
     
     try {
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     } catch (UnsupportedLookAndFeelException | IllegalAccessException
         | InstantiationException | ClassNotFoundException e) {
         e.printStackTrace();
     }
     
     EventQueue.invokeLater(new Runnable() {
       public void run() {
         try {
           PlayerGui window = new PlayerGui();
           window.frame.setVisible(true);
         } catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
     
   }
 
   /**
    * Create the application.
    */
   public PlayerGui() {
     initialize();
   }
 
   /**
    * Initialize the contents of the frame.
    */
   private void initialize() {
     
     frame = new JFrame();
     frame.setBounds(100, 100, 450, 400);
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));
     
     playerPanel = new JPanel();
     frame.getContentPane().add(playerPanel, "north");
     playerLayout = new CardLayout(0, 0);
     playerPanel.setLayout(playerLayout);
     
     userEnterPanel = new JPanel();
     playerPanel.add(userEnterPanel);
     userEnterPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));
     
     userEnterTextField = new JTextField();
     userEnterTextField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     userEnterPanel.add(userEnterTextField, "flowx,cell 0 0,grow");
     
     selectUserAction = new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         addUser();
       }
     };
     
     userEnterTextField.addKeyListener(new KeyListener() {
       @Override
       public void keyPressed(KeyEvent e) {
       }
       @Override
       public void keyReleased(KeyEvent e) {
       }
       @Override
       public void keyTyped(KeyEvent e) {
         
         char c = e.getKeyChar();
 
         if (c == KeyEvent.VK_ENTER) {
           addUser();
         }
       }
     });
     
     selectUserButton = new JButton("Select user");
     selectUserButton.addActionListener(selectUserAction);
     userEnterPanel.add(selectUserButton, "cell 0 0, growy");
     
     userDisplayPanel = new JPanel();
     playerPanel.add(userDisplayPanel);
     userDisplayPanel.setLayout(new MigLayout("","[grow]","[grow]"));
     
     playerNameModel = new DefaultListModel<SteamId>();
     playerNameList = new JList<SteamId>(playerNameModel);
     playerNameList.setCellRenderer(new PlayerListRenderer());
     playerNameList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     userDisplayPanel.add(playerNameList, "flowx,cell 0 0,grow");
     
     changeUserButton = new JButton("Change user");
     changeUserButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         playerLayout.next(playerPanel);
       }
     });
     userDisplayPanel.add(changeUserButton, "cell 0 0,growy");
     
     playerFriendsPanel = new JPanel();
     frame.getContentPane().add(playerFriendsPanel, "cell 0 0,grow");
     playerFriendsPanel.setLayout(new GridLayout(1, 0, 0, 0));
     
     playerFriendsModel = new DefaultListModel<SteamId>();
     playerFriendsList = new JList<SteamId>(playerFriendsModel);
     playerFriendsList.setCellRenderer(new PlayerCheckRenderer());
     
     playerFriendsScroll = new JScrollPane(playerFriendsList);
     playerFriendsPanel.add(playerFriendsScroll);
     
   }
   
   private void addUser() {
     String name = userEnterTextField.getText();
     
     if (!name.isEmpty()) {
       try {
         final SteamId id = gamesInCommon.checkSteamId(name);
         SteamId blank = null;
         playerNameModel.addElement(blank);
         
         if (!id.equals(playerNameModel.firstElement())) {
           playerNameModel.clear();
           playerNameModel.addElement(id);
           userEnterTextField.setText("");
           displayFriends(id.getFriends());
         }
       } catch (SteamCondenserException e) {
         e.printStackTrace();
         userEnterTextField.setText(e.getMessage());
         userEnterTextField.selectAll();
       }
     }
 
     playerLayout.next(playerPanel);
   }
   
   private void displayFriends(SteamId[] friends) {
     
     final ExecutorService taskExecutor = Executors.newCachedThreadPool();
     
     for (final SteamId id : friends) {
       taskExecutor.execute(new Runnable() {
         @Override
         public void run() {
           try {
             id.fetchData();
             System.out.println("Data fetched for id: " + id.getNickname());
             SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                 addSortedFriend(id);
               }
             });
             
             System.out.println("Added element: " + id);
           } catch (SteamCondenserException e) {
             e.printStackTrace();
           }
         }
       });
     }
   }
   
   private void addSortedFriend(final SteamId id) {
     
     if (playerFriendsModel.size() == 0) {
       playerFriendsModel.addElement(id);
       return;
     }
     
     for (int i = 0; i < playerFriendsModel.size(); i++) {
       SteamId friendId = playerFriendsModel.get(i);
       if (id.getNickname().compareToIgnoreCase(friendId.getNickname()) < 0) {
         playerFriendsModel.add(i, id);
         return;
       }
     }
     
     playerFriendsModel.addElement(id);
     
   }
   
   class PlayerCheckRenderer extends PlayerListRenderer {
 
     private static final long serialVersionUID = 1L;
 
   }
   
   class PlayerListRenderer extends DefaultListCellRenderer {
     
     private static final long serialVersionUID = 1L;
 
     @Override
     public Component getListCellRendererComponent(
         JList<?> list, Object value, int index,
         boolean isSelected, boolean cellHasFocus) {
       
       super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
       
       if (value instanceof SteamId) {
         SteamId id = (SteamId) value;
         setText(id.getNickname());
         try {
           ImageIcon icon = new ImageIcon(new URL(id.getAvatarIconUrl()));
           setIcon(icon);
         } catch (MalformedURLException e) {
           e.printStackTrace();
         }
       }
        
       return this;
       
     }
 
   }
 }
