 package org.sankozi.rogueland.gui;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.gui.render.GuiRenderer;
 import org.sankozi.rogueland.gui.render.TextAreaItemRenderer;
 import org.sankozi.rogueland.gui.render.TextAreaPlayerRenderer;
 import org.sankozi.rogueland.model.EquippedItems;
 import org.sankozi.rogueland.model.Item;
 import org.sankozi.rogueland.model.Player;
 import org.sankozi.rogueland.resources.ResourceProvider;
 
 import javax.swing.*;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  *
  * @author sankozi
  */
 public class InventoryPanel extends JPanel implements AncestorListener, ListSelectionListener, KeyListener{
     private final static Logger LOG = Logger.getLogger(InventoryPanel.class);
     transient GameSupport gameSupport;
     transient private Action returnToGame;
 
     private final JSplitPane contents = new JSplitPane();
 
     private final DefaultListModel<ItemDto> itemsDataModel = new DefaultListModel<>();
     private final JList<ItemDto> itemList = new JList<>(itemsDataModel);
 
     /** panel with stats and item description */
     private final JPanel statsPanel = new JPanel();
     private final JPanel itemDescription = new JPanel();
     private final JPanel playerDescription = new JPanel();
     private final GuiRenderer<Player> playerRenderer = new TextAreaPlayerRenderer();
     private final GuiRenderer<Item> itemRenderer = new TextAreaItemRenderer();
 
     @Inject 
 	void setGameSupport(GameSupport support){
         this.gameSupport = support;
 	}
 
     @Inject
     public void setReturnToGame(@Named("return-to-game") Action returnToGame) {
         this.returnToGame = returnToGame;
     }
 
     {
         itemList.addListSelectionListener(this);
         itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         itemList.setCellRenderer(new ItemListRenderer());
         
         initStatsPanel();
         initItemDescription();
         
         setLayout(new BorderLayout());
         contents.setLeftComponent(itemList);
         contents.setRightComponent(statsPanel);
         contents.setDividerLocation(150);
         add(contents, BorderLayout.CENTER);
 
         addAncestorListener(this);
         itemList.addKeyListener(this);
         itemList.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 JList list = (JList)evt.getSource();
                 if (evt.getClickCount() == 2) {
                     int index = list.locationToIndex(evt.getPoint());
                     ItemDto item = itemsDataModel.get(index);
                     Player player = gameSupport.getGame().getPlayer();
                     if(item.equipped){
                         if(player.getEquippedItems().remove(item.item)){
                             item.equipped = false;
                             playerRenderer.render(player, playerDescription);
                         }
                     } else {
                         if(player.getEquippedItems().equip(item.item)){
                             item.equipped = true;
                             playerRenderer.render(player, playerDescription);
                         }
                     }
                     list.repaint();
                 } 
             }
         });
         itemDescription.addKeyListener(this);
         addKeyListener(this);
     }
 
     Font font = ResourceProvider.getFont(Constants.STANDARD_FONT_NAME, 14f);
 
     private void initItemDescription() {
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
             returnToGame.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
         }
     }
 
     private void initStatsPanel() {
         statsPanel.setLayout(new MigLayout("fill, wrap 1", "[left, fill]","[50%, top][50%, top]"));
         statsPanel.add(itemDescription, "grow");
         statsPanel.add(playerDescription, "grow");
         playerDescription.setLayout(new BorderLayout());
     }
 
     private class ItemListRenderer implements ListCellRenderer<ItemDto> {
         @Override
         public Component getListCellRendererComponent(JList list, ItemDto item, int index, boolean isSelected, boolean cellHasFocus) {
             JLabel ret = new JLabel(item.item.getName());
             ret.setFont(font);
             if(isSelected){
                 ret.setOpaque(true);
                 if(item.equipped){
//                    ret.setBackground(Constants.BACKGROUND_EMPH_TEXT_COLOR);
                 } else {
//                    ret.setBackground(Constants.BACKGROUND_TEXT_COLOR);
                 }
                 ret.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, false));
             } else if(item.equipped){
                 ret.setOpaque(true);
 //                ret.setBackground(Constants.BACKGROUND_EMPH_TEXT_COLOR);
             }
             return ret;
         }
     }
 
     private final static class ItemDto {
         private final Item item;
         private boolean equipped;
 
         ItemDto(Item item, boolean wasEquipped) {
             this.item = item;
             this.equipped = wasEquipped;
         }
     }
 
     //~onShow
     @Override
     public void ancestorAdded(AncestorEvent event) {
         LOG.info("ancestorListener");
         itemsDataModel.clear();
         Player player = gameSupport.getGame().getPlayer();
         EquippedItems equippedItems = player.getEquippedItems();
         for(Item item : equippedItems.getEquippedItems()){
             itemsDataModel.addElement(new ItemDto(item, true));
         }
         for(Item item : equippedItems.getUnequippedItems()){
             itemsDataModel.addElement(new ItemDto(item, false));
         }
         playerRenderer.render(player, playerDescription);
         repaint();
     }
 
     @Override
     public void ancestorRemoved(AncestorEvent event) {
     }
 
     @Override
     public void ancestorMoved(AncestorEvent event) {
     }
 
     @Override
     public void valueChanged(ListSelectionEvent e) {
         ItemDto item = itemList.getSelectedValue();
         itemRenderer.render(item == null ? null : item.item, itemDescription);
     }
 }
