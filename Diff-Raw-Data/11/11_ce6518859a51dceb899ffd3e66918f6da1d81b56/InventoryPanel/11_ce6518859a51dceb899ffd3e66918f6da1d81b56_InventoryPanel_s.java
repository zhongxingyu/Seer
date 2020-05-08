 package org.sankozi.rogueland.gui;
 
 import com.google.common.base.Throwables;
 import com.google.inject.Inject;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.io.IOException;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTextPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.html.HTMLDocument;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.model.Item;
 import org.sankozi.rogueland.resources.ResourceProvider;
 
 /**
  *
  * @author sankozi
  */
 public class InventoryPanel extends JPanel implements AncestorListener, ListSelectionListener{
     private final static Logger LOG = Logger.getLogger(InventoryPanel.class);
     transient GameSupport gameSupport;
 
     private final JSplitPane contents = new JSplitPane();
 
     private final DefaultListModel itemsDataModel = new DefaultListModel();
     private final JList itemList = new JList(itemsDataModel);
     private final JTextPane itemDescription = new JTextPane();
 
     @Inject 
 	void setGameSupport(GameSupport support){
         this.gameSupport = support;
 	}
 
     {
         itemList.addListSelectionListener(this);
         itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         itemList.setCellRenderer(new ItemListRenderer());
         
         initItemDescription();
         
         setLayout(new BorderLayout());
         contents.setLeftComponent(itemList);
         contents.setRightComponent(itemDescription);
         contents.setDividerLocation(150);
         add(contents, BorderLayout.CENTER);
 
         addAncestorListener(this);
     }
 
     MutableAttributeSet attrs;
     Font font = ResourceProvider.getFont(Constants.STANDARD_FONT_NAME, 14f);
 
     private void initItemDescription() {
         itemDescription.setEditable(false);
         itemDescription.setContentType("text/html");
         itemDescription.setDocument(new CustomFontStyledDocument());
     }
 
     private class CustomFontStyledDocument extends HTMLDocument{
 
         @Override
         public Font getFont(AttributeSet attr) {
             return font;
         }
     }
 
     private class ItemListRenderer implements ListCellRenderer {
 
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             JLabel ret = new JLabel(((Item)value).getName()); 
             ret.setFont(font);
             if(isSelected){
                 ret.setOpaque(true);
                 ret.setBackground(Constants.BACKGROUND_TEXT_COLOR);
                 ret.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, false));
             }
             return ret;
         }
     }
 
     //~onShow
     @Override
     public void ancestorAdded(AncestorEvent event) {
         LOG.info("ancestorListener");
         itemsDataModel.clear();
         for(Item item : gameSupport.getGame().getPlayer().getEquipment().getItems()){
             itemsDataModel.addElement(item);
         }
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
         Item item = (Item) itemList.getSelectedValue();
        itemDescription.setText(item.getDescription().replace("\n", "<br/>"));
     }
 }
