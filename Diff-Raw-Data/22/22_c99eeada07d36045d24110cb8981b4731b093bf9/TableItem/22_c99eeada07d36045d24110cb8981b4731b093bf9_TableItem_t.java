 //
 // $Id: TableItem.java 3098 2004-08-27 02:12:55Z mdb $
 //
 // ToyBox library - framework for matchmaking networked games
 // Copyright (C) 2004 Three Rings Design, Inc., All Rights Reserved
 // http://www.gamegardens.com/code/
 //
 // This library is free software; you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published
 // by the Free Software Foundation; either version 2.1 of the License, or
 // (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package com.threerings.toybox.lobby.table;
 
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.threerings.util.MessageBundle;
 import com.threerings.util.Name;
 
 import com.threerings.parlor.client.SeatednessObserver;
 import com.threerings.parlor.client.TableDirector;
 import com.threerings.parlor.data.Table;
 import com.threerings.parlor.game.data.PartyGameConfig;
 
 import com.threerings.toybox.data.GameDefinition;
 import com.threerings.toybox.data.ToyBoxGameConfig;
 import com.threerings.toybox.lobby.data.LobbyCodes;
 import com.threerings.toybox.util.ToyBoxContext;
 
 import static com.threerings.toybox.lobby.Log.log;
 
 /**
  * A table item displays the user interface for a single table (whether it
  * be in-play or still being matchmade).
  */
 public class TableItem
     extends JPanel
     implements ActionListener, SeatednessObserver
 {
     /** A reference to the table we are displaying. */
     public Table table;
 
     /**
      * Creates a new table item to display and interact with the supplied
      * table.
      */
     public TableItem (ToyBoxContext ctx, TableDirector tdtr, Table table)
     {
         // keep track of these
         _tdtr = tdtr;
         _ctx = ctx;
 
         // add ourselves as a seatedness observer
         _tdtr.addSeatednessObserver(this);
 
         // figure out who we are
        _self = ctx.getUsername();
 
         // grab the table config reference
         _tconfig = (ToyBoxGameConfig)table.config;
 
         // now create our user interface
     	setBorder(BorderFactory.createLineBorder(Color.black));
         setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
         // create a label for the table
         JLabel tlabel = new JLabel("Table " + table.tableId);
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         gbc.insets = new Insets(2, 0, 0, 0);
         add(tlabel, gbc);
 
         // we have one button for every "seat" at the table
         int bcount = _tconfig.getDesiredPlayers();
         if (bcount == -1) {
             bcount = _tconfig.getMaximumPlayers();
         }
 
         // show the game configuration if this is a party game
         StringBuffer confdesc = new StringBuffer("<html>");
         if (_tconfig.isPartyGame()) {
             MessageBundle msgs = ctx.getMessageManager().getBundle(
                 _tconfig.getBundleName());
             GameDefinition gdef = _tconfig.getGameDefinition();
             for (int ii = 0; ii < gdef.params.length; ii++) {
                 confdesc.append(msgs.xlate(gdef.params[ii].getLabel()));
                 confdesc.append(": ");
                 confdesc.append(_tconfig.params.get(gdef.params[ii].ident));
                 confdesc.append("<br>\n");
             }
         }
 
         // create blank buttons for now and then we'll update everything
         // with the current state when we're done
         gbc.weightx = 1.0;
         gbc.insets = new Insets(2, 0, 2, 0);
         _seats = new JButton[bcount];
         for (int i = 0; i < bcount; i++) {
             // create the button
             _seats[i] = new JButton(JOIN_LABEL);
             _seats[i].addActionListener(this);
 
             // if we're on the left
             if (i % 2 == 0) {
                 // if we're the last seat, then we've got an odd number
                 // and need to center this final seat
                 if (i == bcount-1) {
                     gbc.gridwidth = GridBagConstraints.REMAINDER;
                 } else {
                     gbc.gridwidth = 1;
                 }
 
             } else {
                 gbc.gridwidth = GridBagConstraints.REMAINDER;
             }
 
             // adjust the insets of our last element
             if (i == bcount-1) {
                 gbc.insets = new Insets(2, 0, 4, 0);
             }
 
             // and add the button with the configured constraints
             if (_tconfig.isPartyGame()) {
                 add(new JLabel(confdesc.toString()), gbc);
             } else {
                 add(_seats[i], gbc);
             }
 
             // if we just added the first button, add the "go" button
             // right after it
             if (i == 0) {
                 String msg = _tconfig.isPartyGame() ? "m.join" : "m.watch";
                 _goButton = new JButton(_ctx.xlate(LobbyCodes.LOBBY_MSGS, msg));
                 _goButton.setActionCommand("go");
                 _goButton.addActionListener(this);
                 add(_goButton, gbc);
             }
         }
 
         // and update ourselves based on the contents of the occupants
         // list
         tableUpdated(table);
     }
 
     /**
      * Called when our table has been updated and we need to update the UI
      * to reflect the new information.
      */
     public void tableUpdated (Table table)
     {
         // grab this new table reference
         this.table = table;
 
         // first look to see if we're already sitting at a table
         boolean isSeated = _tdtr.isSeated();
 
         // now enable and label the buttons accordingly
         int slength = _seats.length;
         for (int i = 0; i < slength; i++) {
             if (table.occupants[i] == null) {
                 _seats[i].setText(JOIN_LABEL);
                 _seats[i].setEnabled(!isSeated);
                 _seats[i].setActionCommand("join");
 
             } else if (table.occupants[i].equals(_self) &&
                        !table.inPlay()) {
                 _seats[i].setText(LEAVE_LABEL);
                 _seats[i].setEnabled(true);
                 _seats[i].setActionCommand("leave");
 
             } else {
                 _seats[i].setText(table.occupants[i].toString());
                 _seats[i].setEnabled(false);
             }
         }
 
         // show or hide our "go" button appropriately
         _goButton.setVisible(table.gameOid != -1);
     }
 
     /**
      * Called by the table list view prior to removing us. Here we clean
      * up.
      */
     public void tableRemoved ()
     {
         // no more observy
         _tdtr.removeSeatednessObserver(this);
     }
 
     // documentation inherited
     public void actionPerformed (ActionEvent event)
     {
         String cmd = event.getActionCommand();
         if (cmd.equals("join")) {
             // figure out what position this button is in
             int position = -1;
             for (int i = 0; i < _seats.length; i++) {
                 if (_seats[i] == event.getSource()) {
                     position = i;
                     break;
                 }
             }
 
             // sanity check
             if (position == -1) {
                 log.warning("Unable to figure out what position a <join> " +
                             "click came from [event=" + event + "].");
             } else {
                 // otherwise, request to join the table at this position
                 _tdtr.joinTable(table.tableId, position);
             }
 
         } else if (cmd.equals("leave")) {
             // if we're not joining, we're leaving
             _tdtr.leaveTable(table.tableId);
 
         } else if (cmd.equals("go")) {
             // they want to see the game... so go there
             _ctx.getLocationDirector().moveTo(table.gameOid);
 
         } else {
             log.warning("Received unknown action [event=" + event + "].");
         }
     }
 
     // documentation inherited
     public void seatednessDidChange (boolean isSeated)
     {
         // just update ourselves
         tableUpdated(table);
 
         // enable or disable the go button based on our seatedness
         if (_goButton.isVisible()) {
             _goButton.setEnabled(!isSeated);
         }
     }
 
     /** A reference to our context. */
     protected ToyBoxContext _ctx;
 
     /** Our username. */
     protected Name _self;
 
     /** A reference to our table director. */
     protected TableDirector _tdtr;
 
     /** A casted reference to our table config object. */
     protected ToyBoxGameConfig _tconfig;
 
     /** We have a button for each "seat" at the table. */
     protected JButton[] _seats;
 
     /** We have a button for going to games that are already in
      * progress. */
     protected JButton _goButton;
 
     /** The text shown for seats at which the user can join. */
     protected static final String JOIN_LABEL = "<join>";
 
     /** The text shown for the seat in which this user occupies and which
      * lets her/him know that they can leave that seat by clicking. */
     protected static final String LEAVE_LABEL = "<leave>";
 }
