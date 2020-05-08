 
 package menu.panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListDataListener;
 
 import linewars.gamestate.MapConfiguration;
 import linewars.gamestate.NodeConfiguration;
 import linewars.gamestate.Race;
 import menu.ContentProvider;
 import menu.ContentProvider.MenuImage;
 import menu.WindowManager;
 import menu.components.ComboBoxRenderer;
 import menu.components.CustomScrollBar;
 import menu.components.MenuButton;
 import menu.components.MenuComboBox;
 import menu.components.MenuScrollPane;
 import menu.components.MenuTextArea;
 import menu.components.MenuTextField;
 import menu.networking.Client;
 import menu.networking.MessageType;
 import menu.networking.PlayerBean;
 import menu.networking.Server;
 
 public class CreateGamePanel extends javax.swing.JPanel {
 	private static final long serialVersionUID = 1L;
 	private static final int PORT = 9001;
 	private static final int ENTER_KEY = 10;
 	
 	private WindowManager wm;
 	private SelectionComboBoxModel comboBoxModel;
 	private List<PlayerPanel> players;
 	private Client client;
 	private boolean isServer;
 	private OptionsPane options;
     
     public CreateGamePanel(WindowManager wm, OptionsPane options) {
     	this.wm = wm;
     	this.options = options;
     	init();
     }
     
     @Override
     public void paintComponent(Graphics g)
     {
     	Image img = ContentProvider.getImageResource(MenuImage.lobby_back);
     	g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
     }
     
     public void startServer() throws IOException {
     	Server s = new Server(PORT, replayToggleButton.isSelected(), selectionComboBox.getSelectedItem(), this);
     	s.start();
     	isServer = true;
     	startClient("127.0.0.1");
     }
     
     public void startClient(String serverIp) throws SocketException {
     	client = new Client(PORT, serverIp, this, options.getPlayerName());
     	client.start();
     	
     	if (!isServer) {
     		replayToggleButton.setEnabled(false);
     		selectionComboBox.setEnabled(false);
     		startButton.setEnabled(false);
     	}
     }
 
     public void setReplay(final boolean isReplay) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		replayToggleButton.setSelected(isReplay);
     		replayCheckBoxActionPerformed(null);
     	}});
     }
     
     public void setSelection(final Object selection) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		selectionComboBox.setSelectedItem(selection);
     	}});
     }
     
     public void setPlayerName(final int playerId, final String name) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		players.get(playerId).name.setText(name);
     	}});
     }
     
     public void setPlayerSlot(final int playerId, final int slot) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		players.get(playerId).slot.setSelectedItem(new Integer(slot));
     	}});
     }
     
     public void setPlayerRace(final int playerId, final Object raceIndex) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		if (raceIndex != null)
     			players.get(playerId).race.setSelectedIndex((Integer) raceIndex);
     	}});
     }
     
     public void setPlayerColor(final int playerId, final Color color) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		players.get(playerId).color.setSelectedItem(color);
     		System.out.println(players.get(playerId).race.getSize());
     	}});
     }
     
     public void updateChat(final String message) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		chatArea.append(message);
     	}});
     }
     
     public void updatePlayerPanel(final int playerId, final PlayerBean info) {
     	setPlayerName(playerId, info.getName());
     	setPlayerSlot(playerId, info.getSlot());
     	setPlayerRace(playerId, info.getRaceIndex());
     	setPlayerColor(playerId, info.getColor());
     }
     
     public void addPlayer(final boolean enabled) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		PlayerPanel pp = new PlayerPanel(enabled);
     		pp.setEnabled(enabled);
     		players.add(pp);
     		lobbyPanel.add(pp);
     	}});
     }
     
     public void removePlayer(final int playerId) {
     	SwingUtilities.invokeLater(new Runnable() { public void run() {
     		PlayerPanel pp = players.get(playerId);
     		players.remove(playerId);
     		lobbyPanel.remove(pp);
     		lobbyPanel.validate();
     		lobbyPanel.repaint();
     	}});
     }
     
     private void init() {
     	players = new ArrayList<PlayerPanel>();
     	isServer = false;
     	client = null;
         initComponents();
         replayToggleButton.setEnabled(false);
         selectionComboBox.setPreferredSize(new Dimension(100, 28));
     }
     
     private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	client.sendMessage(MessageType.startGame);
     }                                           
 
     private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	String playerName = players.get(client.getPlayerIndex()).name.getText();
     	String message = chatField.getText();
     	String toAppend = playerName + ": " + message + "\n";
     	
     	// sends the chat message over the network
     	client.sendMessage(MessageType.chat, toAppend);
         
         // sets the field to blank
         chatField.setText("");
     }
     
     private void sendEnterKeyPressed(KeyEvent e) {
     	if (e.getKeyCode() == ENTER_KEY)
     		sendButtonActionPerformed(null);
     }
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	if (isServer)
     		client.sendMessage(MessageType.serverCancelGame);
     	else
     		client.sendMessage(MessageType.clientCancelGame);
     	
     	goBackToTitleMenu();
     }
     
     public void goBackToTitleMenu() {
     	wm.gotoTitleMenu();
     }
 
     private void selectionBoxActionPerformed(java.awt.event.ActionEvent evt) {
     	if (replayToggleButton.isSelected() == false)
     	{
     		previewPanel.setMap((MapConfiguration) selectionComboBox.getSelectedItem());
     	}
     	client.sendMessage(MessageType.selection, selectionComboBox.getSelectedItem());
     }                                            
 
     private void replayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
     	boolean isSelected = replayToggleButton.isSelected();
         selectionLabel.setText((isSelected ? "Replay" : "Map") + " Selection");
         comboBoxModel.setReplay(isSelected);
         if (client != null && players.size() > client.getPlayerIndex()) {
         	players.get(client.getPlayerIndex()).setEnabled(!isSelected);
         	client.sendMessage(MessageType.isReplay, isSelected);
         }
     }                                              
 
     private MapConfiguration[] getAvailableMaps() {
         return ContentProvider.getAvailableMaps();
     }
 
     private Object[] getAvailableReplays() {
         return ContentProvider.getAvailableReplays();
     }
 
     public Race[] getAvailableRaces() {
         return ContentProvider.getAvailableRaces();
     }
     
     private Color[] getAvailableColors() {
     	// FIXME
     	return ContentProvider.getAvailableColors();
     }
     
     private Integer[] getAvailableSlots(boolean filter) {
     	MapConfiguration map = (MapConfiguration) selectionComboBox.getSelectedItem();
     	
     	// get count of nodes
     	int count = 0;
     	List<Integer> slots = new ArrayList<Integer>();
     	for(NodeConfiguration node : map.getNodes())
     	{
     		if(node.isStartNode())
     		{
     			count++;
     			slots.add(count);
     		}
     	}
     	
     	// remove used slots
     	if (filter == true)
     	{
 	    	for (int i = 0; i < players.size(); ++i)
 	    	{
 	    		if (client.getPlayerIndex() != i)
 	    			slots.remove(players.get(i).slot.getSelectedItem());
 	    	}
     	}
     	
     	return slots.toArray(new Integer[0]);
     }
     
     private boolean allIsWell(PlayerPanel p) {
     	return (client != null && players.size() > client.getPlayerIndex() &&  p == players.get(client.getPlayerIndex()));
     }
 
     private class PlayerPanel extends javax.swing.JPanel {
 		private static final long serialVersionUID = 1L;
 		public PlayerPanel(boolean filter) {
             initComponents(filter);
         }        
 
         private void raceChangeActionPerformed(java.awt.event.ActionEvent evt) {         
         	if (allIsWell(this))
         		client.sendMessage(MessageType.race, race.getSelectedIndex());
         }              
 
         private void slotChangeActionPerformed(java.awt.event.ActionEvent evt) {
         	if (allIsWell(this))
         		client.sendMessage(MessageType.slot, (Integer) slot.getSelectedItem());
         }                                          
 
         private void colorChangeActionPerformed(java.awt.event.ActionEvent evt) {
         	if (allIsWell(this))
         		client.sendMessage(MessageType.color, color.getSelectedItem());
         }
         
         @Override
         public void setEnabled(boolean enabled) {
         	color.setEnabled(enabled);
         	name.setEnabled(enabled);
         	race.setEnabled(enabled);
         	slot.setEnabled(enabled);
         }
      
         private void initComponents(boolean filter) {
         	
         	removeAll();
 
         	setOpaque(false);
         	
             slot = new MenuComboBox(false);
             race = new MenuComboBox(false);
             name = new javax.swing.JLabel();
             color = new MenuComboBox(true);
 
             setMaximumSize(new java.awt.Dimension(712, 28));
             setMinimumSize(new java.awt.Dimension(712, 28));
             setPreferredSize(new java.awt.Dimension(712, 28));
 
             slot.setBorder(null);
             slot.setModel(new DefaultComboBoxModel(getAvailableSlots(false)));
             slot.setEditor(null);
             slot.setFocusable(false);
             slot.setKeySelectionManager(null);
             slot.setPreferredSize(new java.awt.Dimension(120, 28));
             slot.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     slotChangeActionPerformed(evt);
                 }
             });
 
             // FIXME make it so it filters again
             race.setModel(new javax.swing.DefaultComboBoxModel(getAvailableRaces()));
             race.setBorder(null);
             race.setFocusable(false);
             race.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     raceChangeActionPerformed(evt);
                 }
             });
 
             name.setText(options.getPlayerName());
             name.setFocusable(false);
             name.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
             name.setMaximumSize(new java.awt.Dimension(53, 28));
             name.setMinimumSize(new java.awt.Dimension(53, 28));
             name.setPreferredSize(new java.awt.Dimension(53, 28));
             name.setFont(ContentProvider.FONT.deriveFont(16.0f));
             name.setForeground(Color.black);
 
             color.setModel(new javax.swing.DefaultComboBoxModel(getAvailableColors()));
             color.setBorder(null);
             color.setEditor(null);
             color.setFocusable(false);
             color.setKeySelectionManager(null);
             color.setPreferredSize(new java.awt.Dimension(120, 28));
             color.setRenderer(new ComboBoxRenderer());
             color.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     colorChangeActionPerformed(evt);
                 }
             });
 
             javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
             this.setLayout(layout);
             layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                     .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(color, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(slot, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(race, 0, 359, Short.MAX_VALUE))
             );
             layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(slot, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(color, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addComponent(race, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addContainerGap())
             );
         }
         
         private javax.swing.JComboBox color;
         private javax.swing.JLabel name;
         private javax.swing.JComboBox race;
         private javax.swing.JComboBox slot;
     }
     
     private class SelectionComboBoxModel implements ComboBoxModel {
         private int map;
         private int replay;
         
         private boolean isReplay;
         private Object[] choices;
         
         public SelectionComboBoxModel()
         {
         	map = 0;
         	replay = 0;
         	isReplay = false;
         	choices = getAvailableMaps();
         }
 
         public void setSelectedItem(Object anItem) {
             Object[] str =  getItems();
             for (int i = 0; i < str.length; ++i) {
                 if (str[i].equals(anItem)) {
                     if (isReplay) replay = i; else map = i;
                 }
             }
         }
 
         public Object getSelectedItem() {
             return getElementAt(isReplay ? replay : map);
         }
 
         public int getSize() {
             return getItems().length;
         }
 
         public Object getElementAt(int index) {
             return getItems()[index];
         }
         
         public void setReplay(boolean isReplay) {
         	this.isReplay = isReplay;
         	choices = isReplay ? getAvailableReplays() : getAvailableMaps();
         }
 
         public void addListDataListener(ListDataListener l) {}
         public void removeListDataListener(ListDataListener l) {}
 
         private Object[] getItems() {
             return choices;
         }
     }
     
     private class SlotComboBoxModel implements ComboBoxModel {
         private Object selection;
         private boolean filter;
         
         public SlotComboBoxModel(boolean filter)
         {
         	this.filter = filter;
         	selection = getItems()[0];
         }
 
         public void setSelectedItem(Object anItem) {
 //            for (Object o : getItems())
 //            {
 //            	if (anItem == o)
 //            	{
 //            		selection = anItem;
 //            		return;
 //            	}
 //            }
             selection = anItem;
         }
 
         public Object getSelectedItem() {
             return selection;
         }
 
         public int getSize() {
             return getItems().length;
         }
 
         public Object getElementAt(int index) {
             return getItems()[index];
         }
 
         public void addListDataListener(ListDataListener l) {}
         public void removeListDataListener(ListDataListener l) {}
 
         private Object[] getItems() {
             return getAvailableSlots(filter);
         }
     }
     
     private void initComponents() {
 
     	removeAll();
     	setOpaque(false);
     	
         replayLabel = new javax.swing.JLabel();
         selectionLabel = new javax.swing.JLabel();
         replayToggleButton = new javax.swing.JToggleButton();
         selectionComboBox = new MenuComboBox(false);
         previewPanel = new MapPanel();
         chatWindow = new javax.swing.JPanel();
         chatArea = new MenuTextArea();
         chatField = new MenuTextField();
         sendButton = new MenuButton(MenuImage.lobby_button_default, MenuImage.lobby_button_rollover, 12);
         buttonPanel = new javax.swing.JPanel();
         startButton = new MenuButton(MenuImage.lobby_button_default, MenuImage.lobby_button_rollover, 12);
         cancelButton = new MenuButton(MenuImage.lobby_button_default, MenuImage.lobby_button_rollover, 12);
         lobbyScrollPane = new MenuScrollPane();
         lobbyPanel = new javax.swing.JPanel();
         chatScrollPane1 = new MenuScrollPane();
 
         setMaximumSize(new java.awt.Dimension(1024, 640));
         setMinimumSize(new java.awt.Dimension(1024, 640));
 
         replayLabel.setFont(ContentProvider.FONT.deriveFont(18.0f));
         replayLabel.setText("Replay");
         replayLabel.setFocusable(false);
         replayLabel.setForeground(Color.black);
 
         selectionLabel.setFont(ContentProvider.FONT.deriveFont(18.0f));
         selectionLabel.setText("Map Selection");
         selectionLabel.setFocusable(false);
         selectionLabel.setForeground(Color.black);
 
         replayToggleButton.setFocusPainted(false);
         replayToggleButton.setFocusable(false);
         replayToggleButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 replayCheckBoxActionPerformed(evt);
             }
         });
 
         comboBoxModel = new SelectionComboBoxModel();
         selectionComboBox.setModel(comboBoxModel);
         selectionComboBox.setFocusable(false);
         selectionComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectionBoxActionPerformed(evt);
             }
         });
 
         previewPanel.setPreferredSize(new java.awt.Dimension(250, 250));
 
         javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
         previewPanel.setLayout(previewPanelLayout);
         previewPanelLayout.setHorizontalGroup(
             previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 250, Short.MAX_VALUE)
         );
         previewPanelLayout.setVerticalGroup(
             previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 250, Short.MAX_VALUE)
         );
 
         chatWindow.setMaximumSize(new java.awt.Dimension(250, 250));
         chatWindow.setMinimumSize(new java.awt.Dimension(250, 250));
         chatWindow.setPreferredSize(new java.awt.Dimension(250, 250));
         chatWindow.setOpaque(false);
 
         chatScrollPane1.setOpaque(false);
         chatScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         chatScrollPane1.setVerticalScrollBar(new CustomScrollBar());
         chatScrollPane1.setBorder(null);
 
         chatField.addKeyListener(new KeyListener() {
 			public void keyTyped(KeyEvent e) {}
 			public void keyPressed(KeyEvent e) {
 				sendEnterKeyPressed(e);
 			}
 			public void keyReleased(KeyEvent e) {}
         });
         
         chatArea.setColumns(20);
         chatArea.setEditable(false);
         chatArea.setLineWrap(true);
         chatArea.setRows(5);
         chatArea.setBorder(null);
         chatArea.setFocusable(false);
         chatScrollPane1.setViewportView(chatArea);
 
         sendButton.setText("Send");
         sendButton.setSize(new Dimension(50, 18));
         sendButton.setFocusable(false);
         sendButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 sendButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout chatWindowLayout = new javax.swing.GroupLayout(chatWindow);
         chatWindow.setLayout(chatWindowLayout);
         chatWindowLayout.setHorizontalGroup(
             chatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chatWindowLayout.createSequentialGroup()
                 .addComponent(chatField, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(sendButton))
             .addComponent(chatScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
         );
         chatWindowLayout.setVerticalGroup(
             chatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chatWindowLayout.createSequentialGroup()
                 .addComponent(chatScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(chatWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(chatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(sendButton)))
         );
 
         buttonPanel.setOpaque(false);
         buttonPanel.setMaximumSize(new java.awt.Dimension(100, 25));
         buttonPanel.setMinimumSize(new java.awt.Dimension(100, 25));
         buttonPanel.setPreferredSize(new java.awt.Dimension(100, 25));
         buttonPanel.setLayout(new java.awt.GridLayout(1, 2, 10, 0));
 
         startButton.setText("Start");
         startButton.setFocusable(false);
         startButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 startButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(startButton);
 
         cancelButton.setText("Cancel");
         cancelButton.setFocusable(false);
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(cancelButton);
 
         lobbyScrollPane.setOpaque(false);
         lobbyScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         lobbyScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        lobbyScrollPane.setBorder(null);
        lobbyScrollPane.setVerticalScrollBar(new CustomScrollBar());
 
         lobbyPanel.setOpaque(false);
         lobbyPanel.setBorder(null);
         lobbyPanel.setLayout(new javax.swing.BoxLayout(lobbyPanel, javax.swing.BoxLayout.Y_AXIS));
 
         lobbyScrollPane.setViewportView(lobbyPanel);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(replayLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(replayToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(35, 35, 35)
                         .addComponent(selectionLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(selectionComboBox, 0, 461, Short.MAX_VALUE))
                     .addComponent(lobbyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(buttonPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                     .addComponent(previewPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chatWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(previewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(chatWindow, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(replayToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addComponent(replayLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(selectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(selectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(lobbyScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)))
                 .addContainerGap())
         );
     }
 
     private javax.swing.JPanel buttonPanel;
     private javax.swing.JButton cancelButton;
     private javax.swing.JTextArea chatArea;
     private javax.swing.JTextField chatField;
     private javax.swing.JPanel chatWindow;
     private javax.swing.JScrollPane chatScrollPane1;
     private javax.swing.JPanel lobbyPanel;
     private javax.swing.JScrollPane lobbyScrollPane;
     private MapPanel previewPanel;
     private javax.swing.JLabel replayLabel;
     private javax.swing.JToggleButton replayToggleButton;
     private javax.swing.JComboBox selectionComboBox;
     private javax.swing.JLabel selectionLabel;
     private javax.swing.JButton sendButton;
     private javax.swing.JButton startButton;   
 }
