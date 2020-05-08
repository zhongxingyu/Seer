 package de.sofd.swing.test.dnd.jgridlist;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.TooManyListenersException;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.sofd.swing.DefaultGridListComponentFactory;
 import de.sofd.swing.GridListComponentFactory;
 import de.sofd.swing.JGridList;
 
 
 public class JGridListDndTestApp {
     
     private class ListFrame extends JFrame {
         private JGridList gridList;
         private DefaultListModel listModel;
         private JToolBar toolbar;
         
         public ListFrame(String title) {
             super(title);
             setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             setSize(800, 700);
             toolbar = new JToolBar("toolbar");
             toolbar.setFloatable(false);
             getContentPane().add(toolbar, BorderLayout.PAGE_START);
             
             listModel = new DefaultListModel();
             for (int i=0; i<50; ++i) {
                 listModel.addElement("element "+i);
             }
 
             //gridList = new JGridList(listModel, nonComponentReusingComponentFactory);
             
             // try to trigger former ArrayIndexOutOfBoundsException bug in scrollbar handling with zero-size models:
             // (see comment on private internalScrollbarValueIsAdjusting member variable in JGridList)
             gridList = new JGridList(new DefaultListModel(), nonComponentReusingComponentFactory);
             gridList.setModel(listModel);
             gridList.setDragEnabled(true);
             ListTH th = new ListTH();
             gridList.setTransferHandler(th);
             try {
                 gridList.getDropTarget().addDropTargetListener(th);
             } catch (TooManyListenersException e) {
                 throw new RuntimeException("SHOULD NEVER HAPPEN", e);
             }
             
             getContentPane().add(gridList, BorderLayout.CENTER);
             // TODO: DefaultGridListComponentFactory selection visualization doesn't work
             //   (background colors are always reset??)
             /*
             gridList.setComponentFactory(new AbstractFramedSelectionGridListComponentFactory() {
                 @Override
                 public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
                     JLabel l = new JLabel(""+modelItem);
                     parent.add(l);
                     return l;
                 }
             });
             */
             gridList.setFirstDisplayedIdx(0);
             gridList.setGridSizes(4, 4);
             gridList.setVisible(true);
             
             gridList.getSelectionModel().setSelectionInterval(7, 7);
             gridList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
             gridList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             
             initToolBar();
         }
         
 
         private DataFlavor gridListCellFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                 "; class=" + GridListCellContents.class.getCanonicalName(),
                 "JGridList cell contents");
         
         private class ListTH extends TransferHandler implements DropTargetListener {
            //TODO: setRenderedDropLocation(null) also necessary when the drag leaves the component...
            // (may require using something other than canImport() for this)
             
             private int[] draggedIndices;
             private int addIndex = -1;
             private int addCount = 0;
             
             private boolean couldImport;
             private JGridList.DropLocation lastDropLocation;
 
             @Override
             public boolean canImport(TransferSupport ts) {
                 if (!ts.isDataFlavorSupported(gridListCellFlavor)) {
                     couldImport = false;
                 } else {
                     JGridList list = (JGridList) ts.getComponent();
                     //remember the dropLocation and the method result for the following call of dragOver().
                     lastDropLocation = list.getDropLocationFor(ts.getDropLocation().getDropPoint());
                     couldImport = (lastDropLocation != null);
                 }
                 return couldImport;
             }
 
             @Override
             public boolean importData(TransferSupport ts) {
                 if (!canImport(ts)) {
                     return false;
                 }
                 JGridList list = (JGridList) ts.getComponent();
                 DefaultListModel model = (DefaultListModel)list.getModel();
                 try {
                     JGridList.DropLocation dl = list.getDropLocationFor(ts.getDropLocation().getDropPoint());
                     list.setRenderedDropLocation(null);
                     Transferable t = ts.getTransferable();
                     GridListCellContents cellContents = (GridListCellContents) t.getTransferData(gridListCellFlavor);
                     System.out.println("importing: " + cellContents);
                     String[] strings = cellContents.getStrings();
                     boolean first = true;
                     for (int i = strings.length - 1; i >= 0; i--) {
                         String s = strings[i];
                         if (first) {
                             if (dl.isInsert()) {
                                 model.insertElementAt(s, dl.getIndex());
                             } else {
                                 model.setElementAt(s, dl.getIndex());
                             }
                             first = false;
                         } else {
                             model.insertElementAt(s, dl.getIndex());
                         }
                         addIndex = dl.getIndex();
                         addCount = strings.length;
                         if (!dl.isInsert()) {
                             addCount -= 1;
                         }
                     }
                     return true;
                 } catch (UnsupportedFlavorException e) {
                     e.printStackTrace();
                     return false;
                 } catch (IOException e) {
                     e.printStackTrace();
                     return false;
                 }
             }
 
             @Override
             public int getSourceActions(JComponent c) {
                 return COPY|MOVE;
             }
             
             @Override
             protected Transferable createTransferable(JComponent c) {
                 JGridList list = (JGridList) c;
                 final StringBuffer txt = new StringBuffer(30);
                 boolean start = true;
                 draggedIndices = list.getSelectedIndices();
                 final Object[] values = list.getSelectedValues();
                 for (Object elt : values) {
                     if (!start) {
                         txt.append("\n");
                     }
                     txt.append(elt.toString());
                     start = false;
                 }
                 return new Transferable() {
                     @Override
                     public boolean isDataFlavorSupported(DataFlavor flavor) {
                         return flavor.equals(DataFlavor.stringFlavor) || flavor.equals(gridListCellFlavor);
                     }
                     @Override
                     public DataFlavor[] getTransferDataFlavors() {
                         return new DataFlavor[]{gridListCellFlavor, DataFlavor.stringFlavor};
                     }
                     @Override
                     public Object getTransferData(DataFlavor flavor)
                             throws UnsupportedFlavorException, IOException {
                         if (flavor.equals(gridListCellFlavor)) {
                             return new GridListCellContents(values);
                         } else if (flavor.equals(DataFlavor.stringFlavor)) {
                             return txt.toString();
                         } else {
                             throw new UnsupportedFlavorException(flavor);
                         }
                     }
                 };
             }
             
             @Override
             protected void exportDone(JComponent source, Transferable data, int action) {
                 JGridList list = (JGridList) source;
                 DefaultListModel model = (DefaultListModel)list.getModel();
                 if (action == TransferHandler.MOVE) {
                     if (draggedIndices != null) {
                         for (int i = 0; i < draggedIndices.length; i++) {
                             if (draggedIndices[i] >= addIndex) {
                                 draggedIndices[i] += addCount;
                             }
                         }
                         for (int i = draggedIndices.length - 1; i >= 0; i--) {
                             model.remove(draggedIndices[i]);
                         }
                     }
                 }
                 draggedIndices = null;
                 addCount = 0;
                 addIndex = -1;
                 list.setRenderedDropLocation(null);
             }
 
 
             //DropTargetListener methods. Called by the list's DropTarget immediately
             //AFTER the corresponding TransferHandler methods. E.g. dragOver() is
             //called after a corresponding call of canImport().
             
             @Override
             public void dropActionChanged(DropTargetDragEvent dtde) {
             }
             @Override
             public void drop(DropTargetDropEvent dtde) {
                 JGridList list = (JGridList) dtde.getDropTargetContext().getComponent();
                 list.setRenderedDropLocation(null);
             }
             @Override
             public void dragOver(DropTargetDragEvent dtde) {
                 JGridList list = (JGridList) dtde.getDropTargetContext().getComponent();
                 list.setRenderedDropLocation(couldImport ? lastDropLocation : null);
             }
             @Override
             public void dragExit(DropTargetEvent dte) {
                 JGridList list = (JGridList) dte.getDropTargetContext().getComponent();
                 list.setRenderedDropLocation(null);
             }
             @Override
             public void dragEnter(DropTargetDragEvent dtde) {
             }
         }
 
         private void initToolBar() {
             final JTextField dispIdxDisplay = new JTextField() {
                 @Override
                 public Dimension getMaximumSize() {
                     return new Dimension(50, super.getMaximumSize().height);
                 }
             };
             toolbar.add(new AbstractAction("<") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gridList.setFirstDisplayedIdx(gridList.getFirstDisplayedIdx()-1);
                     dispIdxDisplay.setText(""+gridList.getFirstDisplayedIdx());
                 }
             });
             toolbar.add(new AbstractAction(">") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gridList.setFirstDisplayedIdx(gridList.getFirstDisplayedIdx()+1);
                     dispIdxDisplay.setText(""+gridList.getFirstDisplayedIdx());
                 }
             });
             toolbar.add(new JLabel("startIdx:"));
             toolbar.add(dispIdxDisplay);
             toolbar.add(new AbstractAction("set") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         gridList.setFirstDisplayedIdx(Integer.parseInt(dispIdxDisplay.getText()));
                     } catch (NumberFormatException ex) {
                         //ignore
                     }
                 }
             });
             toolbar.add(new JLabel("gridSize:"));
             final JSpinner gridSizeSpinner = new JSpinner(new SpinnerNumberModel(4,1,9,1)) {
                 @Override
                 public Dimension getMaximumSize() {
                     return new Dimension(50, super.getMaximumSize().height);
                 }
             };
             gridSizeSpinner.addChangeListener(new ChangeListener() {
                 @Override
                 public void stateChanged(ChangeEvent e) {
                     int value = (Integer)gridSizeSpinner.getValue();
                     gridList.setGridSizes(value, value);
                 }
             });
             toolbar.add(gridSizeSpinner);
             
             final JTextField intStartEntry = new JTextField("10") {
                 @Override
                 public Dimension getMaximumSize() {
                     return new Dimension(50, super.getMaximumSize().height);
                 }
             };
             final JTextField intLengthEntry = new JTextField("5") {
                 @Override
                 public Dimension getMaximumSize() {
                     return new Dimension(50, super.getMaximumSize().height);
                 }
             };
             toolbar.add(new JLabel("intStart:"));
             toolbar.add(intStartEntry);
             toolbar.add(new JLabel("intLength:"));
             toolbar.add(intLengthEntry);
             toolbar.add(new AbstractAction("add") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         int intStart = Integer.parseInt(intStartEntry.getText());
                         int intLength = Integer.parseInt(intLengthEntry.getText());
                         for (int i=0; i<intLength; i++) {
                             listModel.add(intStart, "Add"+i);
                         }
                     } catch (NumberFormatException ex) {
                         //ignore
                     }
                 }
             });
             toolbar.add(new AbstractAction("rm") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         int intStart = Integer.parseInt(intStartEntry.getText());
                         int intLength = Integer.parseInt(intLengthEntry.getText());
                         for (int i=0; i<intLength; i++) {
                             listModel.remove(intStart);
                         }
                     } catch (NumberFormatException ex) {
                         //ignore
                     }
                 }
             });
             final JCheckBox reuseCompsCb = new JCheckBox("reuseCellComps");
             toolbar.add(reuseCompsCb);
             reuseCompsCb.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (reuseCompsCb.isSelected()) {
                         gridList.setComponentFactory(componentReusingComponentFactory);
                     } else {
                         gridList.setComponentFactory(nonComponentReusingComponentFactory);
                     }
                 }
             });
             final JCheckBox showScrollbarCb = new JCheckBox("ScrBar");
             toolbar.add(showScrollbarCb);
             showScrollbarCb.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     gridList.setShowScrollbar(showScrollbarCb.isSelected());
                 }
             });
             showScrollbarCb.setSelected(gridList.isShowScrollbar());
         }
     }
     
     public JGridListDndTestApp() {
         ListFrame f1 = new ListFrame("frame1");
         f1.setVisible(true);
         ListFrame f2 = new ListFrame("frame2");
         f2.setVisible(true);
     }
     
 
     private GridListComponentFactory nonComponentReusingComponentFactory = new DefaultGridListComponentFactory();
 
     private GridListComponentFactory componentReusingComponentFactory = new DefaultGridListComponentFactory() {
         @Override
         public boolean canReuseComponents() {
             return true;
         }
         @Override
         public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
             if (parent.getComponentCount() == 0) {
                 System.out.println("creating new cell component for model element: " + modelItem);
                 JLabel l = new JLabel(""+modelItem);
                 parent.add(l);
                 return l;
             } else {
                 System.out.println("reusing existing cell component for model element: " + modelItem);
                 JLabel l = (JLabel) parent.getComponent(0);
                 l.setText("" + modelItem);
                 return l;
             }
         }
     };
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new JGridListDndTestApp();
             }
         });
     }
 
 }
