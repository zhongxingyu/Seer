 /*
  *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
  * 
  *  MONGKIE is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  MONGKE is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  * 
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.ui.datatable.graph;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.util.*;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.CellEditorListener;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import kobic.prefuse.display.DataViewSupport;
 import org.mongkie.datatable.DataChildFactory;
 import org.mongkie.datatable.DataNode;
 import org.mongkie.datatable.DataTableControllerUI;
 import org.mongkie.datatable.spi.GraphDataTable;
 import org.mongkie.util.AccumulativeEventsProcessor;
 import org.mongkie.util.lang.StringUtilities;
 import org.mongkie.visualization.MongkieDisplay;
 import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
 import org.netbeans.swing.outline.Outline;
 import org.openide.awt.HtmlRenderer;
 import org.openide.explorer.ExplorerManager;
 import org.openide.explorer.view.OutlineView;
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import prefuse.Visualization;
 import prefuse.data.Graph;
 import prefuse.data.Schema;
 import prefuse.data.Table;
 import prefuse.data.Tuple;
 import prefuse.data.event.TupleSetListener;
 import prefuse.data.tuple.TupleSet;
 import prefuse.visual.VisualItem;
 import prefuse.visual.expression.InGroupPredicate;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 public abstract class AbstractDataTable extends OutlineView implements GraphDataTable, ExplorerManager.Provider {
 
     private AbstractModel model;
 
     protected AbstractDataTable() {
         super("Name");
         setBorder(BorderFactory.createEmptyBorder());
 
         final Outline outline = getOutline();
         outline.setRootVisible(false);
         outline.getTableHeader().setPreferredSize(new Dimension(0, 24));
         outline.setSelectVisibleColumnsLabel("Show/hide columns");
         final TableCellRenderer defaultRenderer = outline.getDefaultRenderer(Node.Property.class);
         outline.setDefaultRenderer(Node.Property.class, new DefaultOutlineCellRenderer() {
             /**
              * Gray color for the even lines in the view.
              */
             private final Color VERY_LIGHT_GRAY = new Color(236, 236, 236);
 
             private Color getSelectionBackground() {
                 Color c = UIManager.getColor("List.selectionBackground");
                 return c != null ? c : Color.BLUE;
             }
 
             private Color getSelectionForeground() {
                 Color c = UIManager.getColor("List.selectionForeground");
                 return c != null ? c : Color.WHITE;
             }
 
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null) {
                     DataNode.Property property = (DataNode.Property) value;
                    Component cell;
                     if (property.getValueType() == Boolean.TYPE) {
                         cell = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                     } else {
                         String string = StringUtilities.escapeHTML(
                                 model.getDisplay().getDataViewSupport(getDataGroup()).getStringAt(property.getTuple(), property.getName()));
                         String html = "<html>" + string + "</html>";
                         cell = table.getDefaultRenderer(String.class).getTableCellRendererComponent(table, html, isSelected, false, row, column);
                         ((HtmlRenderer.Renderer) cell).setHtml(true);
                         ((HtmlRenderer.Renderer) cell).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
                         ((JLabel) cell).setToolTipText("".equals(string) ? null : StringUtilities.createHtmlTooltip(property.getDisplayName(), string, 4));
                     }
                    cell.setBackground(isSelected ? getSelectionBackground() : row % 2 == 1 ? VERY_LIGHT_GRAY : table.getBackground());
                    cell.setForeground(isSelected ? getSelectionForeground() : table.getForeground());
                    return cell;
                 }
                return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
             }
         });
         final TableCellEditor defaultEditor = outline.getDefaultEditor(Node.Property.class);
         outline.setDefaultEditor(Node.Property.class, new TableCellEditor() {
             @Override
             public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                 return defaultEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
             }
 
             @Override
             public Object getCellEditorValue() {
                 return defaultEditor.getCellEditorValue();
             }
 
             @Override
             public boolean isCellEditable(EventObject e) {
                 boolean editable = false;
                 if (e instanceof MouseEvent) {
                     MouseEvent me = (MouseEvent) e;
                     editable = (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() >= 2)
                             && model.getDisplay().getDataEditSupport(getDataGroup()).isEditable(outline.getColumnName(outline.columnAtPoint(me.getPoint())));
                 }
                 return editable;
             }
 
             @Override
             public boolean shouldSelectCell(EventObject anEvent) {
                 return false;
             }
 
             @Override
             public boolean stopCellEditing() {
                 return defaultEditor.stopCellEditing();
             }
 
             @Override
             public void cancelCellEditing() {
                 defaultEditor.cancelCellEditing();
             }
 
             @Override
             public void addCellEditorListener(CellEditorListener l) {
                 defaultEditor.addCellEditorListener(l);
             }
 
             @Override
             public void removeCellEditorListener(CellEditorListener l) {
                 defaultEditor.removeCellEditorListener(l);
             }
         });
     }
 
     @Override
     public JComponent getView() {
         MongkieDisplay d = model.getDisplay();
         return (d != null && d.isFired()) ? this : null;
     }
 
     @Override
     public Tool[] getTools() {
         if (tools == null) {
             tools = new Tool[]{new FilterToolsPanel(this)};
         }
         return tools;
     }
     private Tool[] tools;
 
     protected final Table getTable() {
         assert model != null && model.getDisplay() != null;
         return (Table) model.getDisplay().getVisualization().getSourceData(getDataGroup());
     }
 
     @Override
     public void clear() {
         if (model != null) {
             model.setSelctionSyncEnabled(false);
         }
         if (childFactory != null) {
             childFactory.setTable(null, null);
         }
     }
 
     @Override
     public void refreshModel(MongkieDisplay d) {
         AbstractModel old = model;
         if (old != null) {
             old.unset();
         }
         if (d != null) {
             model = lookupModel(d);
             if (model == null) {
                 model = createModel(d);
                 d.add(model);
             }
             Table table = getTable();
             String labelColumn = getLabelColumn(d);
             setPropertyColumns();
             DataViewSupport viewSupport = (DataViewSupport) table.getClientProperty(DataViewSupport.PROP_KEY);
             Schema outline = viewSupport.getOutlineSchema();
             for (int i = 0, j = 0; i < outline.getColumnCount(); i++) {
                 String col = outline.getColumnName(i);
                 addPropertyColumn(col, viewSupport.getColumnTitle(col), outline.getColumnType(i).getSimpleName());
             }
             getOutline().getColumnModel().removeColumn(getOutline().getColumnModel().getColumn(0));
             model.reset(table, showing); // Must reset the model *BEFORE* refreshing child nodes
             if (tools != null) {
                 // Also filter tool must be refreshed before filter actions called in the child factory performed
                 ((FilterToolsPanel) tools[0]).refresh(false);
             }
             if (childFactory == null) {
                 em.setRootContext(new AbstractNode(
                         Children.create(childFactory = new DataChildFactory(table, labelColumn), false)) {
                     @Override
                     public Action[] getActions(boolean context) {
                         return new Action[]{};
                     }
                 });
             } else {
                 childFactory.setTable(table, labelColumn);
             }
         } else {
             clear();
             model = null;
             setPropertyColumns();
             getOutline().getColumnModel().removeColumn(getOutline().getColumnModel().getColumn(0));
         }
     }
 
     public DataChildFactory getDataChildFactory() {
         return childFactory;
     }
     private DataChildFactory childFactory;
 
     @Override
     public void deselected() {
         showing = false;
         if (model != null) {
             model.setSelctionSyncEnabled(false);
         }
     }
 
     @Override
     public void selected() {
         showing = true;
         if (model != null) {
             model.setSelectedNodesOf(model.getDisplay().getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS));
             model.setSelctionSyncEnabled(true);
         }
     }
 
     boolean isSelected() {
         return showing;
     }
     private boolean showing = false;
 
     private AbstractModel lookupModel(MongkieDisplay d) {
         for (AbstractModel m : d.getLookup().lookupAll(AbstractModel.class)) {
             if (this == m.getDataTable()) {
                 return m;
             }
         }
         return null;
     }
 
     protected AbstractModel createModel(MongkieDisplay d) {
         return new AbstractModel(d, this);
     }
 
     protected abstract String getLabelColumn(MongkieDisplay d);
 
     @Override
     public ExplorerManager getExplorerManager() {
         return em;
     }
     private final ExplorerManager em = new ExplorerManager();
 
     @Override
     public AbstractModel getModel() {
         return model;
     }
 
     @Override
     public void setSelectedNodes(final Node[] nodes) {
         if (showing && model != null) {
             final Node[] olds = em.getSelectedNodes();
             model.setSelectedNodesInternal(nodes);
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     model.propertyChange(new PropertyChangeEvent(em, ExplorerManager.PROP_SELECTED_NODES, olds, nodes));
                 }
             });
         }
     }
 
     public static class AbstractModel<T extends AbstractDataTable> implements Model<T>, TupleSetListener, PropertyChangeListener {
 
         private final T dataTable;
         private final MongkieDisplay display;
         private Set<Node> selectedNodes = new HashSet<Node>();
         private Table table;
 
         protected AbstractModel(MongkieDisplay display, T dataTable) {
             this.display = display;
             this.dataTable = dataTable;
         }
 
         @Override
         public MongkieDisplay getDisplay() {
             return display;
         }
 
         public Graph getGraph() {
             assert display != null;
             return display.getGraph();
         }
 
         @Override
         public Table getTable() {
             return table;
         }
 
         protected void reset(Table table, boolean selected) {
             setSelctionSyncEnabled(selected);
             this.table = table;
         }
 
         protected void unset() {
             setSelctionSyncEnabled(false);
             this.table = null;
         }
 
         protected void setSelctionSyncEnabled(boolean enabled) {
             if (enabled) {
                 dataTable.getExplorerManager().removePropertyChangeListener(this);
                 dataTable.getExplorerManager().addPropertyChangeListener(this);
                 TupleSet focusedTupleSet = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
                 focusedTupleSet.addTupleSetListener(this);
                 setSelectedNodesOf(focusedTupleSet);
             } else {
                 dataTable.getExplorerManager().removePropertyChangeListener(this);
                 display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).removeTupleSetListener(this);
                 clearSelection();
             }
         }
 
         /**
          * Synchronize selections in the data table with selections in the
          * display. It will pan the display to place a last selected *ONE* item in the
          * center of the display.
          *
          * @param evt event object of selection of the data table
          */
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             if (!internalTableSelection && evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                 final TupleSet focusedTupleSet = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
                 centerItem = null;
                 display.getVisualization().process(new Runnable() {
                     @Override
                     public void run() {
                         List<Node> oldNodes = Arrays.asList((Node[]) evt.getOldValue());
                         selectedNodes.clear();
                         selectedNodes.addAll(Arrays.asList((Node[]) evt.getNewValue()));
                         internalDisplaySelection = true;
                         // Firstly, remove unselected items
                         if (oldNodes.isEmpty()) { // Ensure focus items is empty
                             focusedTupleSet.clear();
                         } else {
                             for (Node n : oldNodes) {
                                 VisualItem item = display.getVisualization().getVisualItem(dataTable.getDataGroup(), ((DataNode) n).getTuple());
                                 if (focusedTupleSet.containsTuple(item) && !selectedNodes.contains(n)) {
                                     focusedTupleSet.removeTuple(item);
                                 }
                             }
                         }
                         // Then, add newly selected items
                         for (Node n : selectedNodes) {
                             VisualItem item = display.getVisualization().getVisualItem(dataTable.getDataGroup(), ((DataNode) n).getTuple());
                             if (!focusedTupleSet.containsTuple(item)) {
                                 if (selectedNodes.size() == 1) {
                                     focusedTupleSet.setTuple(item);
                                 } else {
                                     focusedTupleSet.addTuple(item);
                                 }
                             }
                             if (selectedNodes.size() == 1) {
                                 centerItem = item;
                             }
                         }
                         internalDisplaySelection = false;
                     }
                 }, Visualization.DRAW);
                 if (centerItem != null && !dataTable.getDataChildFactory().isRefreshing()
                         && !Lookup.getDefault().lookup(DataTableControllerUI.class).isRefreshing(dataTable)) {
                     panDisplayCenterTo(centerItem);
                 }
             }
         }
         private VisualItem centerItem;
         private boolean internalDisplaySelection = false;
 
         private void panDisplayCenterTo(VisualItem item) {
             double displayX = display.getDisplayX();
             double displayY = display.getDisplayY();
             double scale = display.getScale();
             double itemX = item.getBounds().getCenterX() * scale;
             double itemY = item.getBounds().getCenterY() * scale;
             double screenWidth = display.getWidth();
             double screenHeight = display.getHeight();
             double moveX = (itemX * -1) + ((screenWidth / 2) + displayX);
             double moveY = (itemY * -1) + ((screenHeight / 2) + displayY);
             display.animatePan(moveX, moveY, 1000);
         }
 
         /**
          * Synchronize selections in the display with selections in the data
          * table.
          *
          * @param tupleSet total set of selected items in the display
          * @param added    just selected items
          * @param removed  just deselected items
          */
         @Override
         public void tupleSetChanged(final TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
             if (!internalDisplaySelection) {
                 if (added.length > 1 || removed.length > 1) {
                     setSelectedNodesOf(tupleSet);
                 } else {
                     if (tableSelectionQ != null && tableSelectionQ.isAccumulating()) {
                         tableSelectionQ.eventAttended();
                     } else {
                         tableSelectionQ = new AccumulativeEventsProcessor(new Runnable() {
                             @Override
                             public void run() {
                                 setSelectedNodesOf(tupleSet);
                             }
                         });
                         tableSelectionQ.start();
                     }
                 }
             }
         }
         private AccumulativeEventsProcessor tableSelectionQ;
 
         protected void setSelectedNodesOf(TupleSet selectedItems) {
             if (!dataTable.isSelected()) {
                 return;
             }
             selectedNodes.clear();
             for (Iterator<VisualItem> items = selectedItems.tuples(new InGroupPredicate(dataTable.getDataGroup())); items.hasNext();) {
                 Tuple tuple = items.next().getSourceTuple();
                 DataNode n = dataTable.getDataChildFactory().getNodeOf(tuple);
 //                if (n == null) {
 //                    // filtered rows by DataViewSupport.getFilter()?
 //                    continue;
 //                }
                 assert n != null;
                 selectedNodes.add(n);
             }
             setSelectedNodesInternal(selectedNodes.isEmpty() ? new Node[]{} : selectedNodes.toArray(new Node[]{}));
         }
 
         protected void clearSelection() {
             selectedNodes.clear();
             setSelectedNodesInternal(new Node[]{});
         }
 
         private void setSelectedNodesInternal(final Node[] nodes) {
             try {
                 internalTableSelection = true;
                 dataTable.getExplorerManager().setSelectedNodes(nodes);
             } catch (PropertyVetoException ex) {
                 Exceptions.printStackTrace(ex);
             } finally {
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         internalTableSelection = false;
                     }
                 });
             }
         }
         private boolean internalTableSelection = false;
 
         @Override
         public T getDataTable() {
             return dataTable;
         }
     }
 }
