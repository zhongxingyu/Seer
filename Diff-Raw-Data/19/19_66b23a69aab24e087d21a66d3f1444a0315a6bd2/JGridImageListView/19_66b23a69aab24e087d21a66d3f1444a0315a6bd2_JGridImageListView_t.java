 package de.sofd.viskit.ui.imagelist.gridlistimpl;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.DefaultListModel;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.log4j.Logger;
 
 import de.sofd.swing.AbstractFramedSelectionGridListComponentFactory;
 import de.sofd.swing.JGridList;
 import de.sofd.util.DynScope;
 import de.sofd.util.IntRange;
 import de.sofd.util.Misc;
 import de.sofd.viskit.image.ViskitImage;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.model.NotInitializedException;
 import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 import de.sofd.viskit.ui.imagelist.cellviewers.java2d.ImageListViewCellViewer;
 
 /**
  * JImageListView implementation that uses an aggreagated {@link JGridList}.
  *
  * @author Sofd GmbH
  */
 public class JGridImageListView extends JImageListView {
 
     static final Logger logger = Logger.getLogger(JGridImageListView.class);
 
     /**
      * The central {@link JGridList} that we embed to display the cells.
      */
     protected final JGridList wrappedGridList;
 
     /**
      * use for the wrapped grid list a separate model that always tracks
      * (via shallow copying) our #getModel(). This is so we can
      * better control who sees changes when, e.g. we can more easily
      * ensure that if the model changes and the wrappedGridList calls
      * back into us (e.g. into the component factory), the corresponding
      * changes in this object (e.g. cell creation for new model elements)
      * will already have been done.
      */
     protected DefaultListModel wrappedGridListModel;
 
     private boolean inExternalSetFirstVisibleIdx = false;
 
     public JGridImageListView() {
         setLayout(new GridLayout(1, 1));
         wrappedGridList = new JGridList() {
             @Override
             protected void setupUiInteractions() {
                 //super.setupUiInteractions();
             }
             @Override
             public void setFirstDisplayedIdx(int newValue) {
                 if (!inExternalSetFirstVisibleIdx) {
                     JGridImageListView.this.setFirstVisibleIndex(newValue);
                     return;
                 }
                 super.setFirstDisplayedIdx(newValue);
             }
         };
         setupInternalUiInteractions();
        //wrappedGridList.setDragEnabled(true);
         wrappedGridList.setBackground(Color.BLACK);
         wrappedGridList.setVisible(true);
         this.add(wrappedGridList);
         setScaleMode(new MyScaleMode(2, 2));
         setModel(new DefaultListModel());
         wrappedGridList.setComponentFactory(new WrappedGridListComponentFactory());
         setSelectionModel(wrappedGridList.getSelectionModel());
         wrappedGridList.addMouseListener(wholeGridTestMouseHandler);
         wrappedGridList.addMouseMotionListener(wholeGridTestMouseHandler);
         wrappedGridList.addMouseWheelListener(wholeGridTestMouseHandler);
     }
     
     @Override
     protected void copyUiStateToSubComponent(Component c) {
         // do nothing so we keep the wrappedGridList's bg color
     }
 
     @Override
     public void setModel(ListModel model) {
         wrappedGridList.setModel(null);
         super.setModel(model);
         wrappedGridList.setModel(wrappedGridListModel = copyModel(model));
         updateElementPriorities();
     }
 
     private static DefaultListModel copyModel(ListModel m) {
         DefaultListModel result = new DefaultListModel();
         int size = m.getSize();
         for (int i = 0; i < size; i++) {
             result.addElement(m.getElementAt(i));
         }
         return result;
     }
 
     @Override
     protected void modelIntervalAdded(ListDataEvent e) {
         super.modelIntervalAdded(e);
         for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
             wrappedGridListModel.add(i, getModel().getElementAt(i));
         }
         updateElementPriorities();
         // TODO: set initial scale
     }
 
     @Override
     protected void modelIntervalRemoved(ListDataEvent e) {
         super.modelIntervalRemoved(e);
         wrappedGridListModel.removeRange(e.getIndex0(), e.getIndex1());
         updateElementPriorities();
     }
 
     @Override
     protected void modelContentsChanged(ListDataEvent e) {
         super.modelContentsChanged(e);
         wrappedGridList.setModel(wrappedGridListModel = copyModel(getModel()));
         updateElementPriorities();
     }
 
     // delegate our selectionModel property to wrappedGridList's selectionModel
     
     @Override
     public ListSelectionModel getSelectionModel() {
         return wrappedGridList.getSelectionModel();
     }
     
     @Override
     public void setSelectionModel(ListSelectionModel selectionModel) {
         ListSelectionModel oldSelectionModel = getSelectionModel();
         if (oldSelectionModel != null) {
             oldSelectionModel.removeListSelectionListener(listSelectionListener);
         }
         wrappedGridList.setSelectionModel(selectionModel);
         if (wrappedGridList.getSelectionModel() != null) {
             wrappedGridList.getSelectionModel().addListSelectionListener(listSelectionListener);
         }
         firePropertyChange(PROP_SELECTIONMODEL, oldSelectionModel, selectionModel);
     }
     
     private ListSelectionListener listSelectionListener = new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent e) {
             selectionChanged(e);
             fireListSelectionEvent(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
         }
     };
 
     @Override
     public void setFirstVisibleIndex(int newValue) {
         super.setFirstVisibleIndex(newValue);
         updateElementPriorities();
         inExternalSetFirstVisibleIdx = true;
         try {
             wrappedGridList.setFirstDisplayedIdx(getFirstVisibleIndex());
         } finally {
             inExternalSetFirstVisibleIdx = false;
         }
     }
     
     @Override
     public int getLastVisibleIndex() {
         return getFirstVisibleIndex() + getScaleMode().getCellColumnCount() * getScaleMode().getCellRowCount() - 1;
     }
     
     //TODO: setFirstVisibleIndex() prop change event
     
     public void ensureIndexIsVisible(int idx) {
         wrappedGridList.ensureIndexIsVisible(idx);
     }
     
     protected IntRange previouslyVisibleRange = null;
 
     /**
      * Determine newly visible and newly invisible model elements (compared to
      * last call of this method), change their priorities accordingly (newly
      * invisible ones to 0 (the default), newly visible ones to 10).
      */
     protected void updateElementPriorities() {
         int firstVisIdx = getFirstVisibleIndex();
         int lastVisIdx = getLastVisibleIndex();
         if (getModel() != null) {
             IntRange newlyVisibleRange = null;
             int lastVisModelIdx = Math.min(lastVisIdx, getModel().getSize() - 1);
             if (lastVisModelIdx >= firstVisIdx) {
                 newlyVisibleRange = new IntRange(firstVisIdx, lastVisModelIdx);
                 IntRange[] newlyInvisibleRanges = IntRange.subtract(previouslyVisibleRange, newlyVisibleRange);
                 IntRange[] newlyVisibleRanges =   IntRange.subtract(newlyVisibleRange, previouslyVisibleRange);
                 for (IntRange r : newlyInvisibleRanges) {
                     for (int i = r.getMin(); i <= r.getMax(); i++) {
                         logger.debug("setting to prio  0: index " + i);
                         getElementAt(i).setPriority(this, 0);
                     }
                 }
                 for (IntRange r : newlyVisibleRanges) {
                     for (int i = r.getMin(); i <= r.getMax(); i++) {
                         logger.debug("setting to prio 10: index " + i);
                         getElementAt(i).setPriority(this, 10);
                     }
                 }
             }
             previouslyVisibleRange = newlyVisibleRange;
         } else {
             previouslyVisibleRange = null;
         }
     }
     
     /**
      * Class for the ScaleModes that JGridImageListView instances support. Any
      * rectangular grid of n x m cells is supported.
      */
     public static class MyScaleMode implements ScaleMode {
         private final int cellRowCount, cellColumnCount;
 
         public MyScaleMode(int cellRowCount, int cellColumnCount) {
             this.cellRowCount = cellRowCount;
             this.cellColumnCount = cellColumnCount;
         }
 
         public static MyScaleMode newCellGridMode(int rowCount, int columnCount) {
             return new MyScaleMode(rowCount, columnCount);
         }
 
         public int getCellColumnCount() {
             return cellColumnCount;
         }
 
         public int getCellRowCount() {
             return cellRowCount;
         }
 
         @Override
         public String getDisplayName() {
             return "" + cellColumnCount + "x" + cellRowCount;
         }
 
         @Override
         public String toString() {
             //return "[JListImageListView.MyScaleMode: " + getDisplayName() + "]";
             return getDisplayName();
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final MyScaleMode other = (MyScaleMode) obj;
             if (this.cellRowCount != other.cellRowCount) {
                 return false;
             }
             if (this.cellColumnCount != other.cellColumnCount) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 29 * hash + this.cellRowCount;
             hash = 29 * hash + this.cellColumnCount;
             return hash;
         }
 
     }
 
     @Override
     public MyScaleMode getScaleMode() {
         return (MyScaleMode) super.getScaleMode();
     }
 
 
     protected static Collection<ScaleMode> supportedScaleModes;
     static {
         supportedScaleModes = new ArrayList<ScaleMode>();
         supportedScaleModes.add(MyScaleMode.newCellGridMode(1, 1));
         supportedScaleModes.add(MyScaleMode.newCellGridMode(2, 2));
         supportedScaleModes.add(MyScaleMode.newCellGridMode(3, 3));
         supportedScaleModes.add(MyScaleMode.newCellGridMode(4, 4));
         supportedScaleModes.add(MyScaleMode.newCellGridMode(5, 5));
     }
 
     @Override
     public Collection<ScaleMode> getSupportedScaleModes() {
         return supportedScaleModes;
     }
 
     @Override
     protected void doSetScaleMode(ScaleMode oldScaleMode, ScaleMode newScaleMode) {
         MyScaleMode sm = (MyScaleMode) newScaleMode;
         updateElementPriorities();
         wrappedGridList.setGridSizes(sm.getCellRowCount(), sm.getCellColumnCount());
     }
 
     @Override
     public int getCellBorderWidth() {
         return WrappedGridListComponentFactory.BORDER_WIDTH;
     }
     
     @Override
     public Dimension getCurrentCellSize(ImageListViewCell cell) {
         return new Dimension(wrappedGridList.getSize().width / getScaleMode().getCellColumnCount(),
                              wrappedGridList.getSize().height / getScaleMode().getCellRowCount());
     }
     
     @Override
     public Dimension getUnscaledPreferredCellDisplayAreaSize(ImageListViewCell cell) {
         ViskitImage img = cell.getDisplayedModelElement().getImage();
         return new Dimension(img.getWidth(), img.getHeight());
     }
 
     @Override
     public void refreshCellForIndex(int idx) {
         JComponent comp = wrappedGridList.getComponentFor(idx);
         if (null != comp) {
             comp.repaint();
         }
     }
 
     @Override
     public void refreshCells() {
         wrappedGridList.repaintCells();
     }
 
     class WrappedGridListComponentFactory extends AbstractFramedSelectionGridListComponentFactory {
 
         public static final int BORDER_WIDTH = 2;
 
         public WrappedGridListComponentFactory() {
             super(new EmptyBorder(BORDER_WIDTH,BORDER_WIDTH,BORDER_WIDTH,BORDER_WIDTH), new LineBorder(Color.RED, BORDER_WIDTH));
         }
 
         @Override
         public boolean canReuseComponents() {
             return false;
         }
 
         @Override
         public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
             ImageListViewModelElement elt = (ImageListViewModelElement) modelItem;
             ImageListViewCell cell = getCellForElement(elt);
             assert (parent.getComponentCount() == 0);
             JComponent resultComponent = new ImageListViewCellViewer(cell);
             resultComponent.setBackground(Color.BLACK);
             resultComponent.setVisible(true);
             parent.add(resultComponent);
             //resultComponent.addMouseListener(gridComponentMouseHandler);
             //resultComponent.addMouseMotionListener(gridComponentMouseHandler);
             //resultComponent.addMouseWheelListener(gridComponentMouseHandler);
 
             return resultComponent;
         }
 
         @Override
         public void deleteComponent(JGridList source, JPanel parent, Object modelItem, JComponent component) {
             //component.removeMouseListener(gridComponentMouseHandler);
             //component.removeMouseMotionListener(gridComponentMouseHandler);
             //component.removeMouseWheelListener(gridComponentMouseHandler);
             super.deleteComponent(source, parent, modelItem, component);
         }
 
     }
 
     /*
     // TODO: the gridComponentMouseHandler eats the events so they're no longer received by
     //       the wrappedGridList, so e.g. selecting using the mouse no longer works.
     //       Figure out how to avoid this. Maybe use just one listener (per type) on the
     //       whole wrappedGridList? (as we do in JGridList's own mouse event handling)
     //
     //       Thus we've disabled the gridComponentMouseHandler for now and enabled the
     //       wholeGridTestMouseHandler (single listener on the whole list) instead. Figure
     //       out what's best in the long run.
 
     private MouseAdapter gridComponentMouseHandler = new MouseAdapter() {
 
         @Override
         public void mouseClicked(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mousePressed(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseReleased(final MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseEntered(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseExited(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseMoved(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseDragged(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent evt) {
             dispatchEventToCell(evt);
         }
 
     };
     */
 
     private MouseAdapter wholeGridTestMouseHandler = new MouseAdapter() {
 
         @Override
         public void mouseClicked(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mousePressed(MouseEvent evt) {
             dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseReleased(final MouseEvent evt) {
              dispatchEventToCell(evt);
         }
 
         // TODO: generate correct enter/exit events for the cells?
         //       this is something that would be much easier with per-cell
         //       mouse listeners of course... (see TODO in commted-out block above)
 
         @Override
         public void mouseEntered(MouseEvent evt) {
              //dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseExited(MouseEvent evt) {
             //dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseMoved(MouseEvent evt) {
              dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseDragged(MouseEvent evt) {
              dispatchEventToCell(evt);
         }
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent evt) {
              dispatchEventToCell(evt);
         }
 
     };
 
     protected void dispatchEventToCell(InputEvent evt) {
         dispatchEventToCell(evt, true);
     }
 
     protected void dispatchEventToCell(InputEvent evt, boolean refreshCell) {
         if (evt instanceof MouseEvent) {
             dispatchEventToCell((MouseEvent)evt);
         }
     }
 
     /**
      * (hack) {@link DynScope} key that other parties may use to pass in the
      * originating source cell of a mouse event. The value MUST be an object
      * array containing the cell and the cell viewer component.
      */
     public static final String DSK_ORIGINAL_EVENT_SOURCE_CELL = JGridImageListView.class.getName() + ".dsk_originalEventSourceCell";
     
     protected void dispatchEventToCell(MouseEvent evt) {
         ImageListViewCell sourceCell = null;
         try {
             JComponent sourceComponent = null;
             if (DynScope.contains(DSK_ORIGINAL_EVENT_SOURCE_CELL)) {
                 Object[] cellAndComponent = (Object[]) DynScope.get(DSK_ORIGINAL_EVENT_SOURCE_CELL);
                 sourceCell = (ImageListViewCell) cellAndComponent[0];
                 sourceComponent = (JComponent) cellAndComponent[1];
             } else {
                 int clickedModelIndex = wrappedGridList.findModelIndexAt(evt.getPoint());
                 if (clickedModelIndex != -1) {
                     sourceCell = getCell(clickedModelIndex);
                     sourceComponent = wrappedGridList.getComponentFor(clickedModelIndex);
                 }
             }
             if (sourceCell != null) {
                 Point mousePosInCell = SwingUtilities.convertPoint(wrappedGridList, evt.getPoint(), sourceComponent);
                 MouseEvent ce = Misc.deepCopy(evt);
                 ce.setSource(sourceCell);
                 ce.translatePoint(mousePosInCell.x - ce.getX(), mousePosInCell.y - ce.getY());
                 if (ce instanceof MouseWheelEvent) {
                     fireCellMouseWheelEvent((MouseWheelEvent) ce);
                 } else {
                     fireCellMouseEvent(ce);
                 }
                if (ce.isConsumed()) {
                    evt.consume();
                }
             }
         } catch (NotInitializedException e) {
             logger.debug("NotInitializedException during firing of MouseEvent " + evt + ". Reinitializing.");
             sourceCell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
         } catch (Exception e) {
             logger.error("Exception during firing of MouseEvent " + evt + ". Setting the model elt to permanent ERROR state.", e);
             //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
             sourceCell.getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
             sourceCell.getDisplayedModelElement().setErrorInfo(e);
         }
     }
 
     protected void setupInternalUiInteractions() {
         wrappedGridList.setFocusable(true);
         wrappedGridList.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (null == getSelectionModel() || null == getModel()) {
                     return;
                 }
                 requestFocus();
                 if (e.getButton() != MouseEvent.BUTTON1) { return; }
                 int clickedModelIndex = wrappedGridList.findModelIndexAt(e.getPoint());
                 if (clickedModelIndex != -1) {
                     if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
                         getSelectionModel().setSelectionInterval(clickedModelIndex, clickedModelIndex);
                     } else {
                         getSelectionModel().addSelectionInterval(clickedModelIndex, clickedModelIndex);
                     }
                 }
             }
         });
 
         InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         ActionMap actionMap = this.getActionMap();
         if (inputMap != null && actionMap != null) {
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
             actionMap.put("up", new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     shiftSelectionBy(-getScaleMode().getCellColumnCount());
                 }
             });
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
             actionMap.put("down", new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     shiftSelectionBy(getScaleMode().getCellColumnCount());
                 }
             });
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
             actionMap.put("left", new SelectionShiftAction(-1));
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
             actionMap.put("right", new SelectionShiftAction(1));
         }
     }
 
     protected Action upAction = new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
         }
     };
     
     protected void shiftSelectionBy(int shift) {
         if (null == getSelectionModel() || null == getModel()) {
             return;
         }
         int idx = getSelectionModel().getLeadSelectionIndex();
         if (idx != -1) {
             idx += shift;
             if (idx >= 0 && idx < getModel().getSize()) {
                 getSelectionModel().setSelectionInterval(idx, idx);
             }
         }
     }
     
     protected class SelectionShiftAction extends AbstractAction {
         private int shift;
         public SelectionShiftAction(int shift) {
             this.shift = shift;
         }
         @Override
         public void actionPerformed(ActionEvent e) {
             shiftSelectionBy(shift);
         }
     }
 
     @Override
     protected void fireCellMouseWheelEvent(MouseWheelEvent e) {
         try {
             super.fireCellMouseWheelEvent(e);
         } finally {
             // internal mouse wheel handling: scroll the list (unless the event was consumed by an external listener)
             if (!e.isConsumed()) {
                 int fvi = getFirstVisibleIndex();
                 if (e.getWheelRotation() < 0) {
                     if (fvi > 0) {
                         setFirstVisibleIndex(fvi - 1);
                     }
                 } else {
                     if (getLastVisibleIndex() < getLength() - 1) {
                         setFirstVisibleIndex(fvi + 1);
                     }
                 }
                 e.consume();
             }
         }
     }
 
 }
