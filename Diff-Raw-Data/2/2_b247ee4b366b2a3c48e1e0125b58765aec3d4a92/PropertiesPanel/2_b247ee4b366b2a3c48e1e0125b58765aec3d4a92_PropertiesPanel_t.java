 /*
  * Copyright 2012 Eike Kettner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.eknet.neoswing.view;
 
 import com.tinkerpop.blueprints.Edge;
 import com.tinkerpop.blueprints.Element;
 import com.tinkerpop.blueprints.Vertex;
 import org.eknet.neoswing.ComponentFactory;
 import org.eknet.neoswing.DbAction;
 import org.eknet.neoswing.ElementId;
 import org.eknet.neoswing.GraphModel;
 import org.eknet.neoswing.actions.DeletePropertyAction;
 import org.eknet.neoswing.actions.EditPropertyAction;
 import org.eknet.neoswing.actions.SetDefaultLabelAction;
 import org.eknet.neoswing.utils.NeoSwingUtil;
 import org.eknet.neoswing.utils.PopupTrigger;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.AbstractTableModel;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Window;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Shows the properties of a {@link Element}.
  * <p/>
  * Do not use the same instance for {@link Element}s coming from
  * different databases (or change the class to cleanup the {@code TransactionEventHandler}
  * properly).
  * 
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 11.01.12 19:19
  */
 public class PropertiesPanel extends JPanel {
 
   private ElementId<?> element;
 
   private final ComponentFactory factory;
   private final PropertiesTableModel tableModel = new PropertiesTableModel();
   private final GraphModel model;
   private final EditPropertyAction addPropertyAction;
 
   private JLabel infoLabel;
   private JTable table;
 
   private final PopupTrigger popupTrigger = new PopupTrigger(true) {
     @Override
     protected JPopupMenu getPopupMenu() {
       return createPopup();
     }
   };
   
   public PropertiesPanel(GraphModel model, ComponentFactory factory, final ElementId<?> element) {
     super(new BorderLayout(), true);
     this.element = element;
     this.factory = factory;
     this.model = model;
     this.addPropertyAction = new EditPropertyAction(model);
 
 
     table = factory.createTable();
     table.setModel(tableModel);
     table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     table.addMouseListener(popupTrigger);
     add(new JScrollPane(table), BorderLayout.CENTER);
     add(createHeadPanel(), BorderLayout.NORTH);
     updateComponents();
   }
 
 
   private JPanel createHeadPanel() {
     JPanel main = factory.createPanel();
     main.setLayout(new BorderLayout());
     JToolBar bar = factory.createToolbar();
     main.add(bar, BorderLayout.NORTH);
 
     JButton button = factory.createToolbarButton();
     button.setAction(addPropertyAction);
     bar.add(button);
 
     JPanel panel = factory.createPanel();
     panel.setLayout(new FlowLayout(FlowLayout.LEADING));
     main.add(panel, BorderLayout.CENTER);
     infoLabel = factory.createLabel();
     infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD));
     panel.add(infoLabel);
 
     return main;
   }
 
   private void updateComponents() {
     tableModel.reload();
     addPropertyAction.setElement(element);
     
     if (element != null) {
       model.execute(new DbAction<String, Object>() {
         @Override
         protected String doInTx(GraphModel model) {
           Element el = model.getDatabase().lookup(element);
           StringBuilder text = new StringBuilder();
           text.append("Properties of ");
           if (el instanceof Vertex) {
             text.append("node ").append(el.getId());
           }
           if (el instanceof Edge) {
             Edge relationship = (Edge) el;
             text.append("relationship ")
                 .append(relationship.getId())
                 .append(" / ")
                 .append(relationship.getLabel());
           }
           text.append(" [").append(tableModel.getRowCount()).append("]");
           return text.toString();
         }
 
         @Override
         protected void done() {
           infoLabel.setText(safeGet());
         }
       });
     }
   }
 
   public void setElement(ElementId<?> element) {
     if (NeoSwingUtil.equals(this.element, element)) {
       return;
     }
     this.element = element;
     updateComponents();
   }
 
   private JPopupMenu createPopup() {
     JPopupMenu menu = factory.createPopupMenu();
     Window owner = NeoSwingUtil.findOwner(this);
     if (element != null) {
       int index = table.getSelectedRow();
       if (index >= 0) {
         Entry entry = tableModel.getEntry(index);
         EditPropertyAction editAction = new EditPropertyAction(element, entry.key, model);
         editAction.setWindow(owner);
         menu.add(new JMenuItem(editAction));
 
         DeletePropertyAction deleteAction = new DeletePropertyAction(model, element, entry.key);
         deleteAction.setWindow(owner);
         menu.add(new JMenuItem(deleteAction));
 
         menu.addSeparator();
         menu.add(new JMenuItem(new SetDefaultLabelAction(model, element, entry.key)));
       }
     }
     menu.addSeparator();
     addPropertyAction.setWindow(owner);
     menu.add(new JMenuItem(addPropertyAction));
     return menu;
   }
   
   private final class PropertiesTableModel extends AbstractTableModel {
 
     private List<Entry> data = new ArrayList<Entry>();
     private final String[] cols = new String[]{"Key", "Value"};
     
     public void reload() {
       this.data = new ArrayList<Entry>();
       load();
     }
     
     private void load() {
       if (data.isEmpty() && element != null) {
         model.execute(new DbAction<Object, Object>() {
           @Override
           protected Object doInTx(GraphModel model) {
             Element el = model.getDatabase().lookup(element);
             if (el != null) {
               for (String key : el.getPropertyKeys()) {
                 data.add(new Entry(key, el.getProperty(key)));
               }
             }
             return null;
           }
 
           @Override
           protected void done() {
             fireTableDataChanged();
           }
         });
       }
     }
 
     @Override
     public String getColumnName(int column) {
       return cols[column];
     }
 
     @Override
     public int getRowCount() {
       return data.size();
     }
 
     @Override
     public int getColumnCount() {
       return cols.length;
     }
 
     public Entry getEntry(int index) {
       return data.get(index);
     }
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
       Entry entry = data.get(rowIndex);
       if (columnIndex == 0) {
         return entry.key;
       }
       if (columnIndex == 1) {
        return entry.value + "";
       }
       return null;
     }
   }
   
   private static class Entry {
     private String key;
     private Object value;
 
     private Entry(String key, Object value) {
       this.key = key;
       this.value = value;
     }
   }
 }
