 package player;
 
 import com.player.bll.*;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 public class VampPlayerGUI extends javax.swing.JFrame {
 
     UserAccount user;
     Library library;
     Queue queue;
     VampPlayer player;
     Playlist selectedPlaylist;
 
     public VampPlayerGUI() {
         user = new UserAccount();
         initComponents();
         library = new Library();
         queue = new Queue();
         player = new VampPlayer(queue);
 
         new Thread() {
 
             public void run() {
                 while (true) {
                     updatePlayerData();
                     try {
                         this.sleep(250);
                     } catch (Exception e) {
                     }
                 }
             }
         }.start();
     }
 
     public void updatePlayerData() {
         if (player.getCurrentSong() != null) {
             titleOfCurrentTrackPlaying.setText(player.getCurrentSong().getArtist() + " - " + player.getCurrentSong().getTitle());
             songTimeLabel.setText( player.getFormatedTime() );
             songPositionSlider.setValue((int) (player.getProgressPercent() * 1000));
         }
     }
 
     public void setUserAccount(UserAccount user) {
         this.user = user;
     }
 
     public void newUser(UserAccount user) {
         setUserAccount(user);
         welcomeUserLabel.setText("Welcome, " + user.getUsername() + "!");
         library = new Library(user);
 
         setVisible(true);
         updateLibraryUI();
         updatePlaylistsUI();
     }
 
     public void updateLibraryUI() {
         DefaultTableModel model = (DefaultTableModel) libraryTable.getModel();
 
         model.setDataVector(library.getLibraryVector(), new String[]{
                     "Title", "Artist", "Track Length", "Album", "Track Number"
                 });
     }
 
     public void updateQueueUI() {
         DefaultTableModel model = (DefaultTableModel) playerQueueTable.getModel();
 
         model.setDataVector(queue.getDataVector(), new String[]{
                     "Title", "Artist", "Track Length", "Album", "Track Number"
                 });
     }
     
     public void updatePlaylistsUI() {
         DefaultTableModel model = (DefaultTableModel) playlistNamesTable.getModel();
         model.setDataVector( user.getPlaylistsVector( user.getPlaylists() ), new String[]{
           "Playlists"
         });
     }
     
     public void updatePlaylistSongsUI( Playlist p ) {
         DefaultTableModel model = (DefaultTableModel) playlistSongsTable.getModel();
         model.setDataVector( p.getDataVector(), new String[]{
           "Title", "Artist", "Track Length", "Album", "Track Number"
         });
     }
 
     public void addSongToQueueUI(Song song) {
         queue.add(song);
         updateQueueUI();
     }
 
     public void addSongToLibrary() {
         JFileChooser fc = new JFileChooser();
         fc.setFileFilter(new MediaFileFilter());
         int ret = fc.showOpenDialog(this);
         if (ret == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fc.getSelectedFile();
             Song newSong = new Song(selectedFile);
             newSong.setUser(user);
             library.add(newSong);
             updateLibraryUI();
         }
     }
 
     public void removeSongFromLibrary(Song s) {
         library.remove(s);
         updateLibraryUI();
     }
     
     public void createPlaylist() {
         final JFrame frame = new JFrame("Create Playlist");
         JLabel label = new JLabel("Enter Playlist Name");
         final JTextField jText = new JTextField();
         final JButton ok = new JButton("Enter");
         final JButton cancel = new JButton("Cancel");
 
         frame.setSize(400, 350);
         frame.setLocation(200, 200);
         Container c = frame.getContentPane();
         c.setLayout(new GridLayout(2, 2));
         c.add(label);
         c.add(jText);
         c.add(ok);
         c.add(cancel);
             
         frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         
         ok.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 user.createPlaylist( jText.getText() );
                 frame.setVisible(false);
                 updatePlaylistsUI();
             }
         });
         cancel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 frame.dispose();
             }
         });
     }
     
      public void renamePlaylist( final Playlist p ) {
         final JFrame frame = new JFrame("Create Playlist");
         JLabel label = new JLabel("Enter Playlist Name");
         final JTextField jText = new JTextField();
         final JButton ok = new JButton("Enter");
         final JButton cancel = new JButton("Cancel");
 
         frame.setSize(400, 350);
         frame.setLocation(200, 200);
         Container c = frame.getContentPane();
         c.setLayout(new GridLayout(2, 2));
         c.add(label);
         c.add(jText);
         c.add(ok);
         c.add(cancel);
             
         frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
         
         ok.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 p.rename( jText.getText() );
                 frame.setVisible(false);
                 updatePlaylistsUI();
             }
         });
         cancel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 frame.dispose();
             }
         });
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         playlistSongsRightClickPopupMenu = new javax.swing.JPopupMenu();
         deleteSongFromPlaylistRightClickMenuItem = new javax.swing.JMenuItem();
         playlistNamesRightClickPopupMenu = new javax.swing.JPopupMenu();
         newPlaylistRightClickMenuItem = new javax.swing.JMenuItem();
         deletePlaylistRightClickMenuItem = new javax.swing.JMenuItem();
         jSeparator3 = new javax.swing.JPopupMenu.Separator();
         addPlaylistToQueueRightClickMenuItem = new javax.swing.JMenuItem();
         jSeparator4 = new javax.swing.JPopupMenu.Separator();
         renamePlaylistRightClickMenuItem = new javax.swing.JMenuItem();
         libraryRightClickPopupMenu = new javax.swing.JPopupMenu();
         addSongToPlaylistRightClickMenuItem = new javax.swing.JMenuItem();
         jSeparator5 = new javax.swing.JPopupMenu.Separator();
         deleteLibrarySongRightClickMenuItem = new javax.swing.JMenuItem();
         jSeparator6 = new javax.swing.JPopupMenu.Separator();
         addLibrarySongToQueueRightClickMenuItem = new javax.swing.JMenuItem();
         playerQueueRightClickPopupMenu = new javax.swing.JPopupMenu();
         deleteFromQueueRightClickMenuItem = new javax.swing.JMenuItem();
         jTabbedPane = new javax.swing.JTabbedPane();
         playerQueuePanel = new javax.swing.JPanel();
         playerQueueScrollPane = new javax.swing.JScrollPane();
         playerQueueTable = new javax.swing.JTable();
         playlistPanel = new javax.swing.JPanel();
         playlistSongsScrollPanel = new javax.swing.JScrollPane();
         playlistSongsTable = new javax.swing.JTable();
         playlistNamesScrollPanel = new javax.swing.JScrollPane();
         playlistNamesTable = new javax.swing.JTable();
         libraryPanel = new javax.swing.JPanel();
         libraryScrollPanel = new javax.swing.JScrollPane();
         libraryTable = new javax.swing.JTable();
         mediaStreamPanel = new javax.swing.JPanel();
         currentTrackLabel = new javax.swing.JLabel();
         rewindButton = new javax.swing.JButton();
         stopButton = new javax.swing.JButton();
         playButton = new javax.swing.JButton();
         pauseButton = new javax.swing.JButton();
         fastForwardButton = new javax.swing.JButton();
         welcomeUserLabel = new javax.swing.JLabel();
         songTimeLabel = new javax.swing.JLabel();
         titleOfCurrentTrackPlaying = new javax.swing.JLabel();
         songPositionSlider = new javax.swing.JSlider();
         jLabel1 = new javax.swing.JLabel();
         applicationMenuBar = new javax.swing.JMenuBar();
         fileMenuItem = new javax.swing.JMenu();
         createNewAccountMenuItem = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         exitApplicationMenuItem = new javax.swing.JMenuItem();
         libraryMenuItem = new javax.swing.JMenu();
         addSongToLibraryMenuItem = new javax.swing.JMenuItem();
 
         deleteSongFromPlaylistRightClickMenuItem.setText("Delete From Playlist");
         deleteSongFromPlaylistRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteSongFromPlaylistRightClickMenuItemActionPerformed(evt);
             }
         });
         playlistSongsRightClickPopupMenu.add(deleteSongFromPlaylistRightClickMenuItem);
 
         newPlaylistRightClickMenuItem.setText("New Playlist");
         newPlaylistRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newPlaylistRightClickMenuItemActionPerformed(evt);
             }
         });
         playlistNamesRightClickPopupMenu.add(newPlaylistRightClickMenuItem);
 
         deletePlaylistRightClickMenuItem.setText("Delete Playlist");
         deletePlaylistRightClickMenuItem.setToolTipText("");
         deletePlaylistRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deletePlaylistRightClickMenuItemActionPerformed(evt);
             }
         });
         playlistNamesRightClickPopupMenu.add(deletePlaylistRightClickMenuItem);
         playlistNamesRightClickPopupMenu.add(jSeparator3);
 
         addPlaylistToQueueRightClickMenuItem.setText("Queue Playlist");
         addPlaylistToQueueRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addPlaylistToQueueRightClickMenuItemActionPerformed(evt);
             }
         });
         playlistNamesRightClickPopupMenu.add(addPlaylistToQueueRightClickMenuItem);
         playlistNamesRightClickPopupMenu.add(jSeparator4);
 
         renamePlaylistRightClickMenuItem.setText("Rename Playlist");
         renamePlaylistRightClickMenuItem.setToolTipText("");
         renamePlaylistRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 renamePlaylistRightClickMenuItemActionPerformed(evt);
             }
         });
         playlistNamesRightClickPopupMenu.add(renamePlaylistRightClickMenuItem);
 
         addSongToPlaylistRightClickMenuItem.setText("Add to Playlist");
         addSongToPlaylistRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addSongToPlaylistRightClickMenuItemActionPerformed(evt);
             }
         });
         libraryRightClickPopupMenu.add(addSongToPlaylistRightClickMenuItem);
         libraryRightClickPopupMenu.add(jSeparator5);
 
         deleteLibrarySongRightClickMenuItem.setText("Delete From Library");
         deleteLibrarySongRightClickMenuItem.setActionCommand("Remove Song");
         deleteLibrarySongRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteLibrarySongRightClickMenuItemActionPerformed(evt);
             }
         });
         libraryRightClickPopupMenu.add(deleteLibrarySongRightClickMenuItem);
         libraryRightClickPopupMenu.add(jSeparator6);
 
         addLibrarySongToQueueRightClickMenuItem.setText("Add to Queue");
         addLibrarySongToQueueRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addLibrarySongToQueueRightClickMenuItemActionPerformed(evt);
             }
         });
         libraryRightClickPopupMenu.add(addLibrarySongToQueueRightClickMenuItem);
 
         deleteFromQueueRightClickMenuItem.setText("Delete From Queue");
         deleteFromQueueRightClickMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteFromQueueRightClickMenuItemActionPerformed(evt);
             }
         });
         playerQueueRightClickPopupMenu.add(deleteFromQueueRightClickMenuItem);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jTabbedPane.setBackground(new java.awt.Color(102, 102, 102));
         jTabbedPane.setToolTipText("");
 
         playerQueuePanel.setBackground(new java.awt.Color(51, 51, 51));
 
         playerQueueTable.setAutoCreateRowSorter(true);
         playerQueueTable.setBackground(new java.awt.Color(153, 153, 153));
         playerQueueTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         playerQueueTable.setForeground(new java.awt.Color(255, 255, 255));
         playerQueueTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "Title", "Artist", "Track Length", "Album", "Track Number"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         playerQueueTable.setSelectionBackground(new java.awt.Color(204, 204, 204));
         playerQueueTable.setSelectionForeground(new java.awt.Color(51, 51, 51));
         playerQueueTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 playerQueueTableMouseClicked(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 playerQueueTableMouseReleased(evt);
             }
         });
         playerQueueScrollPane.setViewportView(playerQueueTable);
 
         javax.swing.GroupLayout playerQueuePanelLayout = new javax.swing.GroupLayout(playerQueuePanel);
         playerQueuePanel.setLayout(playerQueuePanelLayout);
         playerQueuePanelLayout.setHorizontalGroup(
             playerQueuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, playerQueuePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(playerQueueScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
                 .addContainerGap())
         );
         playerQueuePanelLayout.setVerticalGroup(
             playerQueuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(playerQueuePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(playerQueueScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane.addTab("Player Queue", playerQueuePanel);
 
         playlistPanel.setBackground(new java.awt.Color(51, 51, 51));
 
         playlistSongsTable.setAutoCreateRowSorter(true);
         playlistSongsTable.setBackground(new java.awt.Color(153, 153, 153));
         playlistSongsTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         playlistSongsTable.setForeground(new java.awt.Color(255, 255, 255));
         playlistSongsTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "Title", "Artist", "Track Length", "Album", "Track Number"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         playlistSongsTable.setSelectionBackground(new java.awt.Color(204, 204, 204));
         playlistSongsTable.setSelectionForeground(new java.awt.Color(51, 51, 51));
         playlistSongsTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 playlistSongsTableMouseReleased(evt);
             }
         });
         playlistSongsScrollPanel.setViewportView(playlistSongsTable);
 
         playlistNamesTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         playlistNamesTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null},
                 {null}
             },
             new String [] {
                 "Playlists"
             }
         ));
         playlistNamesTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 playlistNamesTableMouseClicked(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 playlistNamesTableMouseReleased(evt);
             }
         });
         playlistNamesScrollPanel.setViewportView(playlistNamesTable);
 
         javax.swing.GroupLayout playlistPanelLayout = new javax.swing.GroupLayout(playlistPanel);
         playlistPanel.setLayout(playlistPanelLayout);
         playlistPanelLayout.setHorizontalGroup(
             playlistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, playlistPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(playlistNamesScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(playlistSongsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                 .addContainerGap())
         );
         playlistPanelLayout.setVerticalGroup(
             playlistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(playlistPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(playlistPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(playlistSongsScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                     .addComponent(playlistNamesScrollPanel))
                 .addContainerGap())
         );
 
         jTabbedPane.addTab("Playlist", playlistPanel);
 
         libraryPanel.setBackground(new java.awt.Color(51, 51, 51));
 
         libraryTable.setAutoCreateRowSorter(true);
         libraryTable.setBackground(new java.awt.Color(153, 153, 153));
         libraryTable.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         libraryTable.setForeground(new java.awt.Color(255, 255, 255));
         libraryTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null},
                 {null, null, null, null, null}
             },
             new String [] {
                 "Title", "Artist", "Track Length", "Album", "Track Number"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         libraryTable.setSelectionBackground(new java.awt.Color(204, 204, 204));
         libraryTable.setSelectionForeground(new java.awt.Color(51, 51, 51));
         libraryTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 libraryTableMouseClicked(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 libraryTableMouseReleased(evt);
             }
         });
         libraryScrollPanel.setViewportView(libraryTable);
 
         javax.swing.GroupLayout libraryPanelLayout = new javax.swing.GroupLayout(libraryPanel);
         libraryPanel.setLayout(libraryPanelLayout);
         libraryPanelLayout.setHorizontalGroup(
             libraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libraryPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(libraryScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
                 .addContainerGap())
         );
         libraryPanelLayout.setVerticalGroup(
             libraryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(libraryPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(libraryScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPane.addTab("Library", libraryPanel);
 
         mediaStreamPanel.setBackground(new java.awt.Color(102, 102, 102));
         mediaStreamPanel.setForeground(new java.awt.Color(204, 204, 204));
 
         currentTrackLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         currentTrackLabel.setForeground(new java.awt.Color(204, 204, 204));
         currentTrackLabel.setText("Current Track: ");
 
         rewindButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/back.png"))); // NOI18N
         rewindButton.setContentAreaFilled(false);
         rewindButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rewindButtonActionPerformed(evt);
             }
         });
 
         stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/stop.png"))); // NOI18N
         stopButton.setToolTipText("");
         stopButton.setContentAreaFilled(false);
         stopButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 stopButtonActionPerformed(evt);
             }
         });
 
         playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/play.png"))); // NOI18N
         playButton.setContentAreaFilled(false);
         playButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 playButtonActionPerformed(evt);
             }
         });
 
         pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/pause.png"))); // NOI18N
         pauseButton.setContentAreaFilled(false);
         pauseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pauseButtonActionPerformed(evt);
             }
         });
 
         fastForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/forward.png"))); // NOI18N
         fastForwardButton.setContentAreaFilled(false);
         fastForwardButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fastForwardButtonActionPerformed(evt);
             }
         });
 
         welcomeUserLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         welcomeUserLabel.setForeground(new java.awt.Color(204, 204, 204));
         welcomeUserLabel.setText("Welcome, " + user.getUsername() + "!");
         welcomeUserLabel.setToolTipText("");
 
         songTimeLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         songTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         songTimeLabel.setText("00:00");
         songTimeLabel.setToolTipText("");
         songTimeLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
         songTimeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 
         songPositionSlider.setMaximum(1000);
         songPositionSlider.setValue(0);
         songPositionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 songPositionSliderStateChanged(evt);
             }
         });
         songPositionSlider.addInputMethodListener(new java.awt.event.InputMethodListener() {
             public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                 songPositionSliderCaretPositionChanged(evt);
             }
             public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
             }
         });
 
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/Vamp2.png"))); // NOI18N
         jLabel1.setRequestFocusEnabled(false);
 
         javax.swing.GroupLayout mediaStreamPanelLayout = new javax.swing.GroupLayout(mediaStreamPanel);
         mediaStreamPanel.setLayout(mediaStreamPanelLayout);
         mediaStreamPanelLayout.setHorizontalGroup(
             mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mediaStreamPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(songPositionSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mediaStreamPanelLayout.createSequentialGroup()
                         .addComponent(currentTrackLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(titleOfCurrentTrackPlaying, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(songTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(24, 24, 24))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mediaStreamPanelLayout.createSequentialGroup()
                         .addGap(105, 105, 105)
                         .addComponent(rewindButton)
                         .addGap(18, 18, 18)
                         .addComponent(stopButton)
                         .addGap(18, 18, 18)
                         .addComponent(playButton)
                         .addGap(18, 18, 18)
                         .addComponent(pauseButton)
                         .addGap(18, 18, 18)
                         .addComponent(fastForwardButton)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mediaStreamPanelLayout.createSequentialGroup()
                         .addGap(8, 8, 8)
                         .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(welcomeUserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         mediaStreamPanelLayout.setVerticalGroup(
             mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mediaStreamPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(welcomeUserLabel)
                     .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(currentTrackLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(titleOfCurrentTrackPlaying, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(songTimeLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(songPositionSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(mediaStreamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rewindButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(stopButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(playButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(pauseButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(fastForwardButton, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addContainerGap())
         );
 
         mediaStreamPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {currentTrackLabel, songTimeLabel, titleOfCurrentTrackPlaying});
 
         fileMenuItem.setText("File");
 
         createNewAccountMenuItem.setText("Create New Account ");
         createNewAccountMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createNewAccountMenuItemActionPerformed(evt);
             }
         });
         fileMenuItem.add(createNewAccountMenuItem);
         fileMenuItem.add(jSeparator1);
 
         exitApplicationMenuItem.setText("Exit VAMP");
         exitApplicationMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitApplicationMenuItemActionPerformed(evt);
             }
         });
         fileMenuItem.add(exitApplicationMenuItem);
 
         applicationMenuBar.add(fileMenuItem);
 
         libraryMenuItem.setText("Library");
 
         addSongToLibraryMenuItem.setText("Add song to library");
         addSongToLibraryMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addSongToLibraryMenuItemActionPerformed(evt);
             }
         });
         libraryMenuItem.add(addSongToLibraryMenuItem);
 
         applicationMenuBar.add(libraryMenuItem);
 
         setJMenuBar(applicationMenuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jTabbedPane)
             .addComponent(mediaStreamPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(mediaStreamPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane.getAccessibleContext().setAccessibleName("");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void exitApplicationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitApplicationMenuItemActionPerformed
         this.setVisible(false);
         dispose();
     }//GEN-LAST:event_exitApplicationMenuItemActionPerformed
 
     private void rewindButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rewindButtonActionPerformed
         player.skipBackward();
     }//GEN-LAST:event_rewindButtonActionPerformed
 
     private void createNewAccountMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewAccountMenuItemActionPerformed
         CreateUserAccountGUI create = new CreateUserAccountGUI();
         create.setVisible(true);
         this.setVisible(false);
     }//GEN-LAST:event_createNewAccountMenuItemActionPerformed
 
     private void libraryTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_libraryTableMouseReleased
         TableMouseListenerInterface rightClick = new TableRightClick();
         rightClick.libraryMouseReleased(evt);
     }//GEN-LAST:event_libraryTableMouseReleased
 
     private void playlistSongsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistSongsTableMouseReleased
         TableMouseListenerInterface rightClick = new TableRightClick();
         rightClick.playlistSongsMouseReleased(evt);
     }//GEN-LAST:event_playlistSongsTableMouseReleased
 
     private void playlistNamesTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistNamesTableMouseReleased
         TableMouseListenerInterface rightClick = new TableRightClick();
         rightClick.playlistNamesMouseReleased(evt);
     }//GEN-LAST:event_playlistNamesTableMouseReleased
 
   private void addSongToLibraryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSongToLibraryMenuItemActionPerformed
       addSongToLibrary();
   }//GEN-LAST:event_addSongToLibraryMenuItemActionPerformed
 
   private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
       player.play();
   }//GEN-LAST:event_playButtonActionPerformed
 
   private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
       player.stop();
   }//GEN-LAST:event_stopButtonActionPerformed
 
   private void fastForwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastForwardButtonActionPerformed
       player.skipForward();
   }//GEN-LAST:event_fastForwardButtonActionPerformed
 
   private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
       player.pause();
   }//GEN-LAST:event_pauseButtonActionPerformed
 
     private void deleteLibrarySongRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrarySongRightClickMenuItemActionPerformed
         library.remove(libraryTable.getSelectedRow());
         updateLibraryUI();
     }//GEN-LAST:event_deleteLibrarySongRightClickMenuItemActionPerformed
 
   private void songPositionSliderCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_songPositionSliderCaretPositionChanged
   }//GEN-LAST:event_songPositionSliderCaretPositionChanged
 
   private void addLibrarySongToQueueRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLibrarySongToQueueRightClickMenuItemActionPerformed
       addSongToQueueUI(library.get(libraryTable.getSelectedRow()));
   }//GEN-LAST:event_addLibrarySongToQueueRightClickMenuItemActionPerformed
 
   private void songPositionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_songPositionSliderStateChanged
       final int buffersize = 10;
       if ((int) (player.getProgressPercent() * 1000) > songPositionSlider.getValue() + buffersize || (int) (player.getProgressPercent() * 1000) < songPositionSlider.getValue() - buffersize) {
           System.out.println("Change song position to: " + songPositionSlider.getValue());
           try {
               player.seekToPosition((double) songPositionSlider.getValue() / 1000);
           } catch (Exception e) {
           }
       }
   }//GEN-LAST:event_songPositionSliderStateChanged
 
   private void libraryTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_libraryTableMouseClicked
       if (evt.getClickCount() == 2) {
           addSongToQueueUI(library.get(libraryTable.getSelectedRow()));
           player.skipToEnd();
           player.play();
       }
   }//GEN-LAST:event_libraryTableMouseClicked
 
   private void playerQueueTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playerQueueTableMouseClicked
       if (evt.getClickCount() == 2) {
           player.stop();
           player.setPosition(playerQueueTable.getSelectedRow());
           player.play();
       }
   }//GEN-LAST:event_playerQueueTableMouseClicked
 
     private void deleteSongFromPlaylistRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSongFromPlaylistRightClickMenuItemActionPerformed
       if ( selectedPlaylist != null ) {
         selectedPlaylist.remove( playlistSongsTable.getSelectedRow() );
         updatePlaylistSongsUI( selectedPlaylist );
       }
     }//GEN-LAST:event_deleteSongFromPlaylistRightClickMenuItemActionPerformed
 
     private void playerQueueTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playerQueueTableMouseReleased
         TableMouseListenerInterface rightClick = new TableRightClick();
         rightClick.queueMouseReleased(evt);
     }//GEN-LAST:event_playerQueueTableMouseReleased
 
     private void deleteFromQueueRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFromQueueRightClickMenuItemActionPerformed
         // TODO add your handling code here:
         queue.remove(queue.get(playerQueueTable.getSelectedRow()));
         updateQueueUI(); 
     }//GEN-LAST:event_deleteFromQueueRightClickMenuItemActionPerformed
 
   private void playlistNamesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistNamesTableMouseClicked
    if ( playlistNamesTable.getSelectedRow() >= 0 && playlistNamesTable.getSelectedRow() < user.getPlaylists().size() ) {
       selectedPlaylist =  user.getPlaylists().get( playlistNamesTable.getSelectedRow() );
       updatePlaylistSongsUI( selectedPlaylist );
     }
   }//GEN-LAST:event_playlistNamesTableMouseClicked
 
   private void addPlaylistToQueueRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlaylistToQueueRightClickMenuItemActionPerformed
     queue.clone(  user.getPlaylists().get( playlistNamesTable.getSelectedRow() ) );
     this.updateQueueUI();
   }//GEN-LAST:event_addPlaylistToQueueRightClickMenuItemActionPerformed
 
   private void newPlaylistRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPlaylistRightClickMenuItemActionPerformed
     createPlaylist();
   }//GEN-LAST:event_newPlaylistRightClickMenuItemActionPerformed
 
   private void addSongToPlaylistRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSongToPlaylistRightClickMenuItemActionPerformed
     if ( selectedPlaylist != null ) {
       System.out.println( "Addingt song to playlist" );
       selectedPlaylist.add( library.get(libraryTable.getSelectedRow()) );
       updatePlaylistsUI();
       updatePlaylistSongsUI(selectedPlaylist);
     } else {
       JOptionPane.showMessageDialog(this, "Please Select a Playlist First.", "No Playlist Selected", JOptionPane.ERROR_MESSAGE);
     }
   }//GEN-LAST:event_addSongToPlaylistRightClickMenuItemActionPerformed
 
   private void deletePlaylistRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePlaylistRightClickMenuItemActionPerformed
     selectedPlaylist = user.getPlaylists().get( playlistNamesTable.getSelectedRow() );
     if ( selectedPlaylist != null ) {
       user.deletePlaylist( selectedPlaylist );
       selectedPlaylist = null;
       updatePlaylistsUI();
     }
   }//GEN-LAST:event_deletePlaylistRightClickMenuItemActionPerformed
 
   private void renamePlaylistRightClickMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renamePlaylistRightClickMenuItemActionPerformed
     if ( selectedPlaylist != null ) {
       renamePlaylist( selectedPlaylist );
     } else {
       JOptionPane.showMessageDialog(this, "Please Select a Playlist First.", "No Playlist Selected", JOptionPane.ERROR_MESSAGE);
     }
   }//GEN-LAST:event_renamePlaylistRightClickMenuItemActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(VampPlayerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(VampPlayerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(VampPlayerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(VampPlayerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new VampPlayerGUI().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem addLibrarySongToQueueRightClickMenuItem;
     private javax.swing.JMenuItem addPlaylistToQueueRightClickMenuItem;
     private javax.swing.JMenuItem addSongToLibraryMenuItem;
     private javax.swing.JMenuItem addSongToPlaylistRightClickMenuItem;
     private javax.swing.JMenuBar applicationMenuBar;
     private javax.swing.JMenuItem createNewAccountMenuItem;
     private javax.swing.JLabel currentTrackLabel;
     private javax.swing.JMenuItem deleteFromQueueRightClickMenuItem;
     private javax.swing.JMenuItem deleteLibrarySongRightClickMenuItem;
     private javax.swing.JMenuItem deletePlaylistRightClickMenuItem;
     private javax.swing.JMenuItem deleteSongFromPlaylistRightClickMenuItem;
     private javax.swing.JMenuItem exitApplicationMenuItem;
     private javax.swing.JButton fastForwardButton;
     private javax.swing.JMenu fileMenuItem;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     private javax.swing.JPopupMenu.Separator jSeparator3;
     private javax.swing.JPopupMenu.Separator jSeparator4;
     private javax.swing.JPopupMenu.Separator jSeparator5;
     private javax.swing.JPopupMenu.Separator jSeparator6;
     private javax.swing.JTabbedPane jTabbedPane;
     private javax.swing.JMenu libraryMenuItem;
     private javax.swing.JPanel libraryPanel;
     private javax.swing.JPopupMenu libraryRightClickPopupMenu;
     private javax.swing.JScrollPane libraryScrollPanel;
     private javax.swing.JTable libraryTable;
     private javax.swing.JPanel mediaStreamPanel;
     private javax.swing.JMenuItem newPlaylistRightClickMenuItem;
     private javax.swing.JButton pauseButton;
     private javax.swing.JButton playButton;
     private javax.swing.JPanel playerQueuePanel;
     private javax.swing.JPopupMenu playerQueueRightClickPopupMenu;
     private javax.swing.JScrollPane playerQueueScrollPane;
     private javax.swing.JTable playerQueueTable;
     private javax.swing.JPopupMenu playlistNamesRightClickPopupMenu;
     private javax.swing.JScrollPane playlistNamesScrollPanel;
     private javax.swing.JTable playlistNamesTable;
     private javax.swing.JPanel playlistPanel;
     private javax.swing.JPopupMenu playlistSongsRightClickPopupMenu;
     private javax.swing.JScrollPane playlistSongsScrollPanel;
     private javax.swing.JTable playlistSongsTable;
     private javax.swing.JMenuItem renamePlaylistRightClickMenuItem;
     private javax.swing.JButton rewindButton;
     private javax.swing.JSlider songPositionSlider;
     private javax.swing.JLabel songTimeLabel;
     private javax.swing.JButton stopButton;
     private javax.swing.JLabel titleOfCurrentTrackPlaying;
     private javax.swing.JLabel welcomeUserLabel;
     // End of variables declaration//GEN-END:variables
 
     interface TableMouseListenerInterface {
 
         public void playlistSongsMouseReleased(MouseEvent e);
         public void playlistNamesMouseReleased(MouseEvent e);
         public void libraryMouseReleased(MouseEvent e);
         public void queueMouseReleased(MouseEvent e);
     }
 
     class TableRightClick implements TableMouseListenerInterface {
 
         @Override
         public void playlistSongsMouseReleased(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 JTable source = (JTable) e.getSource();
                 int row = source.rowAtPoint(e.getPoint());
                 int column = source.columnAtPoint(e.getPoint());
 
                 if (!source.isRowSelected(row)) {
                     source.changeSelection(row, column, false, false);
                 }
 
                 playlistSongsRightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         @Override
         public void playlistNamesMouseReleased(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 JTable source = (JTable) e.getSource();
                 int row = source.rowAtPoint(e.getPoint());
                 int column = source.columnAtPoint(e.getPoint());
 
                 if (!source.isRowSelected(row)) {
                     source.changeSelection(row, column, false, false);
                 }
 
                 playlistNamesRightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         @Override
         public void libraryMouseReleased(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 JTable source = (JTable) e.getSource();
                 int row = source.rowAtPoint(e.getPoint());
                 int column = source.columnAtPoint(e.getPoint());
 
                 if (!source.isRowSelected(row)) {
                     source.changeSelection(row, column, false, false);
                 }
 
                 libraryRightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         @Override
         public void queueMouseReleased(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 JTable source = (JTable) e.getSource();
                 int row = source.rowAtPoint(e.getPoint());
                 int column = source.columnAtPoint(e.getPoint());
 
                 if (!source.isRowSelected(row)) {
                     source.changeSelection(row, column, false, false);
                 }
 
                 playerQueueRightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
 
     }
 }
