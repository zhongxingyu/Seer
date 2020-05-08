 /*
  * Collab desktop - Software for shared drawing via internet in real-time
  * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
  *
  * This file is part of Collab desktop.
  *
  * Collab desktop is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Collab desktop is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.mgn.collabdesktop.menu.frames;
 
 import cz.mgn.collabdesktop.menu.MenuFrame;
 import cz.mgn.collabdesktop.network.Client;
 import cz.mgn.collabdesktop.network.ConnectionInterface;
 import cz.mgn.collabdesktop.network.DataInterface;
 import cz.mgn.collabdesktop.network.commands.CommandGenerator;
 import cz.mgn.collabdesktop.network.commands.CommandReader;
 import cz.mgn.collabdesktop.network.commands.InCommands;
 import cz.mgn.collabdesktop.room.model.executor.CommandExecutor;
 import cz.mgn.collabdesktop.room.view.DeskFrame;
 import cz.mgn.collabdesktop.utils.ImageUtil;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
 * @author Martin Indra <aktive@seznam.cz>
  */
 public class ChooseRoom extends MenuFrame implements DataInterface, ActionListener, ConnectionInterface {
 
     protected Client client;
     protected JButton createRoom;
     protected JButton joinRoom;
     protected JButton refresh;
     protected JButton disconnect;
     protected RoomsTable roomsTable;
 
     public ChooseRoom(Client client) {
         super();
         this.client = client;
         client.setConnectionInterface(this);
         client.addDataInterface(this);
         refresh();
     }
 
     public void createRoom(String name, String password, int width, int height) {
         byte[] send = CommandGenerator.generateCreateRoomCommand(width, height, password, name);
         client.send(send);
     }
 
     public void joinRoom(int roomID, String password) {
         byte[] send = CommandGenerator.generateJoinRoomCommand(roomID, password);
         client.send(send);
     }
 
     protected void createRoom() {
         goTo(new CreateRoom(this), true);
     }
 
     protected void refresh() {
         byte[] send = CommandGenerator.generateGetRoomsListCommand();
         client.send(send);
     }
 
     protected void joinRoom() {
         int roomID = roomsTable.getSelectedRoomID();
         if (roomID != -1) {
             if (roomsTable.isSelectedRoomLocked()) {
                 goTo(new JoinRoom(this, roomID), true);
             } else {
                 joinRoom(roomID, "");
             }
         }
     }
 
     protected void disconnect() {
         client.close();
         client.removeDataInterface(this);
         client.setConnectionInterface(null);
     }
 
     @Override
     protected String getSectionName() {
         return "choose room";
     }
 
     @Override
     protected void initComponents() {
         initMenuBar();
 
         Container pane = getContentPane();
         pane.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.CENTER;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(5, 5, 5, 5);
 
         createRoom = new JButton("create");
         createRoom.addActionListener(this);
         c.weightx = 1f / 4f;
         c.gridwidth = 1;
         c.gridx = 0;
         c.gridy = 0;
         pane.add(createRoom, c);
 
         joinRoom = new JButton("join");
         joinRoom.addActionListener(this);
         c.weightx = 1f / 4f;
         c.gridwidth = 1;
         c.gridx = 1;
         c.gridy = 0;
         pane.add(joinRoom, c);
 
         refresh = new JButton("refresh");
         refresh.addActionListener(this);
         c.weightx = 1f / 4f;
         c.gridwidth = 1;
         c.gridx = 2;
         c.gridy = 0;
         pane.add(refresh, c);
 
         disconnect = new JButton("disconnect");
         disconnect.setToolTipText("disconnect from server");
         disconnect.addActionListener(this);
         c.weightx = 1f / 4f;
         c.gridwidth = 1;
         c.gridx = 3;
         c.gridy = 0;
         pane.add(disconnect, c);
 
         roomsTable = new RoomsTable();
         JScrollPane tableScrollPane = new JScrollPane(roomsTable);
         roomsTable.clearRooms();
         c.fill = GridBagConstraints.BOTH;
         c.weighty = 1f;
         c.weightx = 1f;
         c.gridheight = 1;
         c.gridwidth = 4;
         c.gridx = 0;
         c.gridy = 1;
         pane.add(tableScrollPane, c);
 
 
         Insets in = getInsets();
         Dimension size = new Dimension(600, 250);
         size.width += in.left + in.right;
         size.height += in.top + in.bottom;
         setPreferredSize(size);
         setSize(getPreferredSize());
         setMinimumSize(size);
     }
 
     @Override
     public void dataReaded(byte[] data, byte command) {
         switch (command) {
             case InCommands.IN_COMMAND_ROOMS_LIST:
                 roomsListCommand(data);
                 break;
             case InCommands.IN_COMMAND_JOIN_ROOM:
                 joinRoomCommand(data);
                 break;
         }
     }
 
     protected void roomsListCommand(byte[] data) {
         ArrayList<CommandReader.RoomInfo> rooms = CommandReader.readRoomsListCommand(data);
         if (rooms != null) {
             roomsTable.clearRooms();
             for (CommandReader.RoomInfo room : rooms) {
                 roomsTable.addRoom(room.getID(), room.isLocked(), room.getName(), room.getWidth(), room.getHeight(), room.getUsersCount());
             }
         }
     }
 
     protected void joinRoomCommand(byte[] data) {
         Object[] joinRoom = CommandReader.readJoinRoomCommand(data);
         if (joinRoom != null) {
             int yourID = (Integer) joinRoom[0];
             String roomName = (String) joinRoom[1];
 
             Point p = getLocation();
             p.x += getWidth() / 2;
             p.y += getHeight() / 2;
             dispose();
             client.removeDataInterface(this);
             CommandExecutor executor = new CommandExecutor(client, yourID);
             DeskFrame df = new DeskFrame(executor, roomName, p.x, p.y);
             df.setVisible(true);
             client.setConnectionInterface(df);
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == refresh) {
             refresh();
         } else if (e.getSource() == joinRoom) {
             joinRoom();
         } else if (e.getSource() == createRoom) {
             createRoom();
         } else if (e.getSource() == disconnect) {
             disconnect();
         }
     }
 
     @Override
     public void connectionError(Client client) {
        client.setConnectionInterface(null);
        goTo(new ConnectServer(), false);
     }
 
     @Override
     public void connectionSuccessful(Client client) {
     }
 
     @Override
     public void connectionClosed(Client client) {
        client.setConnectionInterface(null);
        goTo(new ConnectServer(), false);
     }
 
     public class RoomsTable extends JTable {
 
         protected Image lockedIcon = null;
         protected Image openedIcon = null;
         protected ArrayList<Integer> ids = new ArrayList<Integer>();
 
         public RoomsTable() {
             loadImages();
             setModel(new javax.swing.table.DefaultTableModel(
                     new Object[][]{},
                     new String[]{
                         "", "Name", "Resolution", "Users"
                     }) {
                 @Override
                 public boolean isCellEditable(int rowIndex, int columnIndex) {
                     return false;
                 }
             });
             setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
             //setShowVerticalLines(false);
             getTableHeader().setReorderingAllowed(false);
             setRowHeight(25);
             getColumnModel().getColumn(0).setPreferredWidth(25);
         }
 
         protected void loadImages() {
             lockedIcon = ImageUtil.loadImageFromResources("/resources/images/lock.png");
             openedIcon = ImageUtil.loadImageFromResources("/resources/images/unlock.png");
         }
 
         @Override
         public Class getColumnClass(int column) {
             if (column == 0) {
                 return ImageIcon.class;
             }
             return super.getColumnClass(column);
         }
 
         public boolean isSelectedRoomLocked() {
             int id = getSelectedRow();
             if (id == -1) {
                 return false;
             }
             id = ids.get(id);
             if (id < 0) {
                 return true;
             }
             return false;
         }
 
         public int getSelectedRoomID() {
             int id = getSelectedRow();
             if (id == -1) {
                 return id;
             }
             id = ids.get(id);
             if (id < 0) {
                 id *= -1;
             }
             return id;
         }
 
         public void clearRooms() {
             DefaultTableModel tModel = (DefaultTableModel) getModel();
             while (tModel.getRowCount() > 0) {
                 tModel.removeRow(0);
             }
             ids.clear();
         }
 
         public void addRoom(int roomID, boolean locked, String name, int width, int height, int usersCount) {
             DefaultTableModel tModel = (DefaultTableModel) getModel();
             ImageIcon lockedIcon = null;
             if (locked) {
                 lockedIcon = new ImageIcon(this.lockedIcon);
             } else {
                 lockedIcon = new ImageIcon(this.openedIcon);
             }
             tModel.addRow(new Object[]{lockedIcon, name, "" + width + " x " + height, "" + usersCount});
             if (locked) {
                 roomID *= -1;
             }
             ids.add(roomID);
         }
     }
 }
