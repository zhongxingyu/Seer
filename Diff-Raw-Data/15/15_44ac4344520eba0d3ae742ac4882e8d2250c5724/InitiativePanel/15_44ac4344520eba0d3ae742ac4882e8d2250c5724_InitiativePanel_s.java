 /* The MIT License
  * 
  * Copyright (c) 2008 Jay Gorrell
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.ui.tokenpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.token.EditTokenDialog;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.InitiativeList;
 import net.rptools.maptool.model.InitiativeListModel;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.InitiativeList.TokenInitiative;
 import net.rptools.maptool.model.Token.Type;
 import net.rptools.maptool.model.Zone.Event;
 
 import com.jeta.forms.components.line.HorizontalLineComponent;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jidesoft.swing.JideSplitButton;
 
 /**
  * This panel shows the initiative order inside of MapTools.
  * 
  * @author Jay
  */
 public class InitiativePanel extends JPanel implements PropertyChangeListener, ActionListener, ModelChangeListener, ListSelectionListener {
   
     /*---------------------------------------------------------------------------------------------
      * Instance Variables
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Model containing all of the tokens in this initiative.
      */
     private InitiativeList list;
     
     /**
      * The model used to display a list in the panel;
      */
     private InitiativeListModel model;
     
     /**
      * Component that displays the round
      */
     private JLabel round;
     
     /**
      * Component that displays the initiative list.
      */
     private JList displayList;
     
     /**
      * Flag indicating that token images are shown in the list.
      */
     private boolean showTokens = true;
     
     /**
      * Flag indicating that token states are shown in the list. Only valid if {@link #showTokens} is <code>true</code>.
      */
     private boolean showTokenStates = true;
     
     /**
      * Flag indicating that initiative state is shown in the list.
      */
     private boolean showInitState = true;
     
     /**
      * The zone data being displayed.
      */
     private Zone zone;
     
     /**
      * The component that contains the initiative menu.
      */
     private JideSplitButton menuButton;
     
     /*---------------------------------------------------------------------------------------------
      * Constructor
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Setup the menu 
      */
     public InitiativePanel() {
         
         // Build the form and add it's component
         setLayout(new BorderLayout());
         JPanel panel = new JPanel(new FormLayout("8dlu pref 8dlu pref 4dlu fill:30px 0px:grow 8dlu", "4dlu fill:pref 7px fill:0px:grow 4dlu"));
         add(panel, SwingConstants.CENTER);
         menuButton = new JideSplitButton(I18N.getText("initPanel.menuButton"));
         menuButton.addActionListener(this);
         panel.add(menuButton, new CellConstraints(2, 2));
         JLabel label = new JLabel(I18N.getText("initPanel.round"));
         panel.add(label, new CellConstraints(4, 2));
         round = new JLabel();
         round.setHorizontalAlignment(SwingConstants.CENTER);
         round.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
         round.setFont(getFont().deriveFont(Font.BOLD));
         panel.add(round, new CellConstraints(6, 2));
         panel.add(new HorizontalLineComponent(), new CellConstraints(2, 3, 6, 1));
 
         // Set up the list with an empty model
         displayList = new JList();
         model = new InitiativeListModel();
         displayList.setModel(model);
         setList(new InitiativeList(null));
         displayList.setCellRenderer(new InitiativeListCellRenderer(this));
         displayList.setDragEnabled(true);
         displayList.setTransferHandler(new InitiativeTransferHandler(this));
         displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         displayList.addListSelectionListener(this);
         displayList.addMouseListener(new DoubleClickHandler());
         panel.add(new JScrollPane(displayList), new CellConstraints(2, 4, 6, 1));
         updateView();
     }
     
     /*---------------------------------------------------------------------------------------------
      * Instance Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Update the view after the connection has been created. This allows the menus to be tailored for
      * GM's and Player's properly
      */
     public void updateView() {
         
         // Set up the menu
         menuButton.removeAll();
         boolean isGM = MapTool.getPlayer() == null || MapTool.getPlayer().isGM();        
         if (isGM) {
             I18N.setAction("initPanel.sort", SORT_LIST_ACTION);        
             menuButton.add(new JMenuItem(SORT_LIST_ACTION));
             menuButton.addSeparator();
             I18N.setAction("initPanel.toggleHold", TOGGLE_HOLD_ACTION);        
             menuButton.add(new JMenuItem(TOGGLE_HOLD_ACTION));
         } // endif
         I18N.setAction("initPanel.setInitState", SET_INIT_STATE_VALUE);        
         menuButton.add(new JMenuItem(SET_INIT_STATE_VALUE));
         I18N.setAction("initPanel.clearInitState", CLEAR_INIT_STATE_VALUE);        
         menuButton.add(new JMenuItem(CLEAR_INIT_STATE_VALUE));
         menuButton.addSeparator();        
         I18N.setAction("initPanel.showTokens", SHOW_TOKENS_ACTION);
         JCheckBoxMenuItem item = new JCheckBoxMenuItem(SHOW_TOKENS_ACTION);
         item.setSelected(true);
         menuButton.add(item);
         I18N.setAction("initPanel.showTokenStates", SHOW_TOKEN_STATES_ACTION);
         item = new JCheckBoxMenuItem(SHOW_TOKEN_STATES_ACTION);
         item.setSelected(true);
         menuButton.add(item);
         I18N.setAction("initPanel.showInitStates", SHOW_INIT_STATE);
         item = new JCheckBoxMenuItem(SHOW_INIT_STATE);
         item.setSelected(true);
         menuButton.add(item);
         if (isGM) {
             menuButton.addSeparator();
             I18N.setAction("initPanel.addPCs", ADD_PCS_ACTION);
             menuButton.add(new JMenuItem(ADD_PCS_ACTION));
             I18N.setAction("initPanel.addAll", ADD_ALL_ACTION);
             menuButton.add(new JMenuItem(ADD_ALL_ACTION));
             menuButton.addSeparator();
             I18N.setAction("initPanel.remove", REMOVE_TOKEN_ACTION);
             menuButton.add(new JMenuItem(REMOVE_TOKEN_ACTION));
             I18N.setAction("initPanel.removeAll", REMOVE_ALL_ACTION);
             menuButton.add(new JMenuItem(REMOVE_ALL_ACTION));
             menuButton.setText(I18N.getText("initPanel.menuButton"));
             displayList.setDragEnabled(true);
         } else {
             menuButton.setText(I18N.getText("initPanel.toggleHold"));
             displayList.setDragEnabled(false);
         } // endif
         valueChanged(null);
     }
     
     /**
      * Remove all of the tokens from the model and clear round and current 
      */
     public void clearTokens() {
         list.clearModel();
     }
     
     /**
      * Make sure that the token references match the zone
      */
     public void update() {
         list.update();
     }
     
     /** @return Getter for list */
     public InitiativeList getList() {
         return list;
     }
 
     /** @param theList Setter for the list to set */
     public void setList(InitiativeList theList) {
         
         // Remove the old list 
         if (list != null) 
             list.removePropertyChangeListener(this);
         
         // Add the new one
         list = theList;
         if (list != null) {
             list.addPropertyChangeListener(this);
             round.setText(list.getRound() >= 0 ? Integer.toString(list.getRound()) : "");
         } // endif
         model.setList(list);
     }
 
     /** @return Getter for showTokens */
     public boolean isShowTokens() {
         return showTokens;
     }
 
     /** @return Getter for showTokenStates */
     public boolean isShowTokenStates() {
         return showTokenStates;
     }
 
     /** @return Getter for showInitState */
     public boolean isShowInitState() {
         return showInitState;
     }
 
     /** @return Getter for model */
     public InitiativeListModel getModel() {
         return model;
     }
 
     /**
      * Set the zone that we are currently working on.
      * 
      * @param aZone The new zone
      */
     public void setZone(Zone aZone) {
         if (aZone == zone) return;
         if (zone != null)
             zone.removeModelChangeListener(this);
         zone = aZone;
         if (zone != null) 
             zone.addModelChangeListener(this);
         setList(zone.getInitiativeList());
         if (!MapTool.getPlayer().isGM()) {
             REMOVE_TOKEN_ACTION.setEnabled(false);
             SORT_LIST_ACTION.setEnabled(false);
        }
     }
     
     /*---------------------------------------------------------------------------------------------
      * ListSelectionListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
      */
     public void valueChanged(ListSelectionEvent e) {
         if (e != null && e.getValueIsAdjusting()) return;
         TokenInitiative ti =  (TokenInitiative)displayList.getSelectedValue();
         if (ti == null) ti = ((InitiativeListModel)displayList.getModel()).getCurrentTokenInitiative();
         boolean enabled = (ti != null && (MapTool.getPlayer() == null || MapTool.getPlayer().isGM() || ti.getToken().isOwner(MapTool.getPlayer().getName()))) ? true : false;
         CLEAR_INIT_STATE_VALUE.setEnabled(enabled);
         REMOVE_TOKEN_ACTION.setEnabled(enabled);
         SET_INIT_STATE_VALUE.setEnabled(enabled);
         TOGGLE_HOLD_ACTION.setEnabled(enabled);
         if (MapTool.getPlayer() != null && !MapTool.getPlayer().isGM()) {
             menuButton.setEnabled(enabled);
         } else {
             menuButton.setEnabled(true);
         } // endif
     }
     
     /*---------------------------------------------------------------------------------------------
      * PropertyChangeListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
      */
     public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals(InitiativeList.ROUND_PROP)) {
             String text = list.getRound() < 0 ? "" : Integer.toString(list.getRound());
             round.setText(text);
         } else if (evt.getPropertyName().equals(InitiativeList.CURRENT_PROP)) {
             if (list.getCurrent() < 0) return;
             Token t = list.getTokenInitiative(list.getCurrent()).getToken();
             String s = String.format(I18N.getText("initPanel.displayMessage"), t.getName());
             if (t.isVisible()) {
                 MapTool.addMessage(TextMessage.say(null, s));
             } else {
                 MapTool.addMessage(TextMessage.me(null, s));
             } // endif
         } // endif
     }
 
     /*---------------------------------------------------------------------------------------------
      * ActionListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
         if (MapTool.getPlayer() == null || MapTool.getPlayer().isGM()) {
             list.nextInitiative();
         } else {
             TOGGLE_HOLD_ACTION.actionPerformed(e);
         }
     }
 
     /*---------------------------------------------------------------------------------------------
      * ModelChangeListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * @see net.rptools.maptool.model.ModelChangeListener#modelChanged(net.rptools.maptool.model.ModelChangeEvent)
      */
     public void modelChanged(ModelChangeEvent event) {
         if (!event.getEvent().equals(Event.INITIATIVE_LIST_CHANGED)) return;
         if ((Zone)event.getModel() == zone)
           setList(((Zone)event.getModel()).getInitiativeList());
     }
     
     /*---------------------------------------------------------------------------------------------
      * Menu Actions
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * This action will remove the selected token from the list.
      */
     public final Action REMOVE_TOKEN_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null) return;
             int index = list.indexOf(ti.getToken());
             list.removeToken(index);
         };
     };
     
     /**
      * This action will turn the selected token's initiative on and off.
      */
     public final Action TOGGLE_HOLD_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null) return;
             ti.setHolding(!ti.isHolding());
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_TOKENS_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showTokens = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.setCellRenderer(new InitiativeListCellRenderer(InitiativePanel.this)); // Regenerates the size of each row.
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_TOKEN_STATES_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showTokenStates = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.repaint();
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_INIT_STATE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showInitState = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.repaint();
         };
     };
     
     /**
      * This action will set the initiative state of the currently selected token.
      */
     public final Action SET_INIT_STATE_VALUE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null) return;
             String sName = MapTool.getPlayer().isGM() && ti.getToken().getGMName() != null ? ti.getToken().getGMName() : ti.getToken().getName();
             String input = JOptionPane.showInputDialog(String.format(I18N.getText("initPanel.enterState"), sName));
             if (input == null) return;
             ti.setState(input.trim());
         };
     };
 
     /**
      * This action toggles the display of token images.
      */
     public final Action SORT_LIST_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.sort();
         };
     };
 
     /**
      * This action will set the initiative state of the currently selected token.
      */
     public final Action CLEAR_INIT_STATE_VALUE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null) return;
             ti.setState(null);
         };
     };
 
     /**
      * This action will remove all tokens from the initiative panel.
      */
     public final Action REMOVE_ALL_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             clearTokens();
         };
     };
 
     /**
      * This action will add all tokens in the zone to this initiative panel.
      */
     public final Action ADD_ALL_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.insertTokens(list.getZone().getTokens());
         };
     };
 
     /**
      * This action will add all PC tokens in the zone to this initiative panel.
      */
     public final Action ADD_PCS_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             List<Token> tokens = new ArrayList<Token>();
             for (Token token : list.getZone().getTokens()) {
                 if (token.getType() == Type.PC) 
                     tokens.add(token);
             } // endfor
             list.insertTokens(tokens);
         };
     };
     
     /*---------------------------------------------------------------------------------------------
      * DoubleClickHandler Inner Class
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Handle a double click on the list of the table.
      * 
      * @author jgorrell
      * @version $Revision$ $Date$ $Author$
      */
     private class DoubleClickHandler extends MouseAdapter {
       
       /**
        * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
        */
       public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
           SwingUtilities.invokeLater(new Runnable() {
             public void run() { 
                 if (displayList.getSelectedValue() != null) {
                     EditTokenDialog tokenPropertiesDialog = MapTool.getFrame().getTokenPropertiesDialog();
                     Token token = ((TokenInitiative)displayList.getSelectedValue()).getToken(); 
                     tokenPropertiesDialog.showDialog(token);
 
                     if (tokenPropertiesDialog.isTokenSaved()) {
                         ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zone);
                         renderer.repaint();
                         renderer.flush(token);
                         MapTool.serverCommand().putToken(zone.getId(), token);
                         renderer.getZone().putToken(token);
                         MapTool.getFrame().updateImpersonatePanel(token);
                     } // endif
                 } // endif
             }
           });
         } // endif
       }
     }
 }
