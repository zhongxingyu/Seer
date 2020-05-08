 package de.sofd.viskit.ui.imagelist.jlistimpl.test;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.ButtonModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.ListModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 
 import de.sofd.draw2d.Drawing;
 import de.sofd.draw2d.DrawingObject;
 import de.sofd.draw2d.viewer.tools.EllipseTool;
 import de.sofd.draw2d.viewer.tools.SelectorTool;
 import de.sofd.swing.DefaultBoundedListSelectionModel;
 import de.sofd.util.FloatRange;
 import de.sofd.util.IdentityHashSet;
 import de.sofd.viskit.controllers.GenericILVCellPropertySyncController;
 import de.sofd.viskit.controllers.ImageListViewInitialWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseMeasurementController;
 import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
 import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
 import de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController;
 import de.sofd.viskit.controllers.ImageListViewSelectionScrollSyncController;
 import de.sofd.viskit.controllers.ImageListViewSelectionSynchronizationController;
 import de.sofd.viskit.controllers.ImageListViewWindowingApplyToAllController;
 import de.sofd.viskit.controllers.cellpaint.ImageListViewImagePaintController;
 import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintTextToCellsController;
 import de.sofd.viskit.model.DicomImageListViewModelElement;
 import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.model.LookupTable;
 import de.sofd.viskit.model.LookupTables;
 import de.sofd.viskit.ui.LookupTableCellRenderer;
 import de.sofd.viskit.ui.RoiToolPanel;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
 import de.sofd.viskit.ui.imagelist.glimpl.JGLImageListView;
 import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
 import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
import de.sofd.viskit.util.DicomUtil;
 
 /**
  *
  * @author olaf
  */
 public class JListImageListTestApp {
 
     public JListImageListTestApp() throws Exception {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice[] gs = ge.getScreenDevices();
         
         //// creating the frames as follows reproduces OpenGL event handling bugs under Linux/nVidia
         //JFrame f1 = newFrame("Viskit ImageList test app window 1", gs[0].getDefaultConfiguration());
         //JFrame f2 = newFrame("Viskit ImageList test app window 2", (gs.length > 1 ? gs[1].getDefaultConfiguration() : null));
 
         //// creating them like this apparently works better
         //JFrame f1 = newSingleListFrame("Viskit ImageList test app window 1", null);
         //JFrame f2 = newSingleListFrame("Viskit ImageList test app window 2", null);
         JFrame f2 = newMultiListFrame("Multi-List frame", null);
     }
     
     public JFrame newSingleListFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
         //final DefaultListModel model = getTestImageViewerListModel();
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/resources/DICOM-Testbilder/1578"));
         final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00900__center10102"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/pet-studie/cd855__center4001"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd822__center4001"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd836__center4001"));
 
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/cd846__center4001__39.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series1/cd014__center001__0.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series2/cd014__center001__25.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/24-bit Uncompressed Color.dcm"));
 
         final JImageListView viewer;
         //viewer = newJListImageListView();
         //viewer = newJGridImageListView(true);
         //viewer = newJGridImageListView(false);
         viewer = newJGLImageListView();
         
         new ImageListViewInitialWindowingController(viewer).setEnabled(true);
         viewer.setModel(model);
         viewer.addCellPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 //System.out.println("cell propChanged " + evt.getPropertyName() + " => " + evt.getNewValue() + " in cell " + evt.getSource());
             }
         });
         viewer.addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent evt) {
                 System.out.println("SelectionChanged => {" + evt.getFirstIndex() + "," + evt.getLastIndex() + "} in " + evt.getSource());
             }
         });
         for (int i = 0; i < model.size(); i++) {
             viewer.getCell(i).getRoiDrawingViewer().activateTool(new SelectorTool());
             //setWindowingToDcm(viewer.getCell(i));
             //setWindowingToOptimal(viewer.getCell(i));
         }
         viewer.getSelectionModel().setSelectionInterval(0, 1);
         viewer.addImageListViewListener(new ImageListViewListener() {
 
             @Override
             public void onImageListViewEvent(ImageListViewEvent e) {
                 if (e instanceof ImageListViewCellAddEvent) {
                     //setWindowingToDcm(((ImageListViewCellAddEvent)e).getCell());
                     //setWindowingToOptimal(((ImageListViewCellAddEvent)e).getCell());
                 }
             }
         });
 
         final JFrame f = (graphicsConfig == null ? new JFrame(frameTitle) : new JFrame(frameTitle, graphicsConfig));
         JToolBar toolbar = new JToolBar("toolbar");
         toolbar.setFloatable(false);
         toolbar.add(new AbstractAction("+Img") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 ImageListViewModelElement newElt = new TestImageModelElement(5);
                 model.addElement(newElt);
                 viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
             }
         });
         toolbar.add(new AbstractAction("InsImg") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 int idx = viewer.getLeadSelectionIndex();
                 if (idx >= 0) {
                     ImageListViewModelElement newElt = new TestImageModelElement(7);
                     model.add(idx, newElt);
                     viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
                 }
             }
         });
         toolbar.add(new AbstractAction("DelImg") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 ImageListViewModelElement elt = viewer.getSelectedValue();
                 if (elt != null) {
                     model.removeElement(elt);
                 }
             }
         });
         toolbar.add(new AbstractAction("RoiMv") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     ImageListViewModelElement elt = viewer.getElementAt(3);
                     Drawing roiDrawing = elt.getRoiDrawing();
                     DrawingObject roi = roiDrawing.get(0);
                     roi.moveBy(10, 5);
                 } catch (IndexOutOfBoundsException ex) {
                     System.out.println("list has no 4th element or 4th element contains no ROIs...");
                 }
             }
         });
         toolbar.add(new AbstractAction("WndOptim") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 ImageListViewModelElement elt = viewer.getSelectedValue();
                 if (null != elt) {
                     ImageListViewCell cell = viewer.getCellForElement(elt);
                     FloatRange usedRange = cell.getDisplayedModelElement().getUsedPixelValuesRange();
                     cell.setWindowWidth((int) usedRange.getDelta());
                     cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                 }
             }
         });
         JCheckBox wndAllCheckbox = new JCheckBox("WndAll");
         toolbar.add(wndAllCheckbox);
         
         toolbar.add(new AbstractAction("Load From Dir") {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser jFileChooser1 = new javax.swing.JFileChooser();
                 jFileChooser1.setAcceptAllFileFilterUsed(false);
                 jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                 jFileChooser1.setName("jFileChooser1");
                 int returnVal = jFileChooser1.showOpenDialog(f);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     final DefaultListModel model = new DefaultListModel();
                     File file = new File(jFileChooser1.getSelectedFile().getPath());
                     String[] children = file.list();
                     Arrays.sort(children);
                     for (int i = 0; i < children.length; i++) {
                         if (children[i].endsWith(".dcm")) {
                             System.out.println(children[i]);
                             try {
                                 model.addElement(new FileBasedDicomImageListViewModelElement(jFileChooser1.getSelectedFile().getPath() + File.separator + children[i]));
                             } catch (MalformedURLException ex) {
                                 ex.printStackTrace();
                             }
                         }
                     }
                     viewer.setModel(model);
                 }
 
 
             }
         });
         
         toolbar.add(new JLabel("lut:"));
         final JComboBox lutCombo = new JComboBox();
         for (LookupTable lut : LookupTables.getAllKnownLuts()) {
             lutCombo.addItem(lut);
         }
         lutCombo.setRenderer(new LookupTableCellRenderer());
         lutCombo.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     LookupTable lut = (LookupTable) lutCombo.getSelectedItem();
                     System.out.println("activating lut: " + lut);
                     for (int i = 0; i < viewer.getLength(); i++) {
                         viewer.getCell(i).setLookupTable(lut);
                     }
                     // TODO: apply to newly added cells. Have a controller to generalize this for arbitrary cell properties
                 }
             }
         });
         toolbar.add(lutCombo);
         
         RoiToolPanel roiToolPanel = new RoiToolPanel();
         toolbar.add(roiToolPanel);
 
         toolbar.add(new JLabel("ScaleMode:"));
         final JComboBox scaleModeCombo = new JComboBox();
         for (JImageListView.ScaleMode sm : viewer.getSupportedScaleModes()) {
             scaleModeCombo.addItem(sm);
         }
         //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(6, 6));
         //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(7, 7));
         //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(8, 8));
         toolbar.add(scaleModeCombo);
         scaleModeCombo.setEditable(false);
         Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                    viewer, BeanProperty.create("scaleMode"),
                                    scaleModeCombo, BeanProperty.create("selectedItem")).bind();
         
         new ImageListViewMouseWindowingController(viewer);
         new ImageListViewMouseZoomPanController(viewer);
         new ImageListViewRoiInputEventController(viewer);
         new ImageListViewRoiToolApplicationController(viewer).setRoiToolPanel(roiToolPanel);
         final ImageListViewWindowingApplyToAllController wndAllController = new ImageListViewWindowingApplyToAllController(viewer);
         Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                    wndAllController, BeanProperty.create("enabled"),
                                    wndAllCheckbox, BeanProperty.create("selected")).bind();
 
         new ImageListViewImagePaintController(viewer).setEnabled(true);
         
         ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(viewer);
         sssc.setScrollPositionTracksSelection(true);
         sssc.setSelectionTracksScrollPosition(true);
         sssc.setAllowEmptySelection(false);
         sssc.setEnabled(true);
         
         final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(viewer) {
             @Override
             protected String[] getTextToPrint(ImageListViewCell cell) {
                 DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                 DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                 return new String[] {
                         "PN: " + dicomImageMetaData.getString(Tag.PatientName),
                         "SL: " + dicomImageMetaData.getString(Tag.SliceLocation),
                         "wl/ww: " + cell.getWindowLocation() + "/" + cell.getWindowWidth(),
                         "Zoom: " + cell.getScale()
                 };
             }
         };
         ptc.setEnabled(true);
         
         new ImageListViewMouseMeasurementController(viewer).setEnabled(true);
 
         toolbar.add(new AbstractAction("tgglTxt") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ptc.setEnabled(!ptc.isEnabled());
             }
         });
         
         f.getContentPane().add(viewer, BorderLayout.CENTER);
         f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setSize(1100, 900);
         f.setVisible(true);
         
         return f;
     }
 
     private Color[] syncColors = new Color[]{Color.red, Color.green, Color.cyan};
 
     private Map<Color, SyncColorState> syncColorStates = new HashMap<Color, SyncColorState>();
     
     private static class SyncColorState {
         Set<JImageListView> syncSet;
         // TODO: generalize to support any kind of *SyncController without explicit coding (=> ListSyncController base interface with #setLists etc.)
         ImageListViewSelectionSynchronizationController selSyncController;
         GenericILVCellPropertySyncController windowingSyncController;
         GenericILVCellPropertySyncController zoomPanSyncController;
         ButtonModel selSyncCheckboxModel, windowingSyncCheckboxModel, zoomPanSyncCheckboxModel;
     }
     
     {
         ActionListener controllerUpdater = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateSyncControllers();
             }
         };
         for (Color c : syncColors) {
             SyncColorState state = new SyncColorState();
             syncColorStates.put(c, state);
 
             state.syncSet = new IdentityHashSet<JImageListView>();
 
             state.selSyncController = new ImageListViewSelectionSynchronizationController();
             state.selSyncController.setKeepRelativeSelectionIndices(true);
             state.selSyncController.setEnabled(true);
             state.selSyncCheckboxModel = new JToggleButton.ToggleButtonModel();
             state.selSyncCheckboxModel.addActionListener(controllerUpdater);
             
             state.windowingSyncController = new GenericILVCellPropertySyncController(new String[]{"windowLocation", "windowWidth"});
             state.windowingSyncController.setEnabled(true);
             state.windowingSyncCheckboxModel = new JToggleButton.ToggleButtonModel();
             state.windowingSyncCheckboxModel.addActionListener(controllerUpdater);
             
             state.zoomPanSyncController = new GenericILVCellPropertySyncController(new String[]{"scale", "centerOffset"});
             state.zoomPanSyncController.setEnabled(true);
             state.zoomPanSyncCheckboxModel = new JToggleButton.ToggleButtonModel();
             state.zoomPanSyncCheckboxModel.addActionListener(controllerUpdater);
         }
     }
     
     private void updateSyncControllers() {
         for (Color c : syncColors) {
             SyncColorState state = syncColorStates.get(c);
 
             if (state.windowingSyncCheckboxModel.isSelected()) {
                 state.windowingSyncController.setLists(state.syncSet);
             } else {
                 state.windowingSyncController.setLists(new JImageListView[0]);
             }
 
             if (state.selSyncCheckboxModel.isSelected()) {
                 // TODO: constraint: a list shouldn't be in more than one selSyncController at the same time
                 state.selSyncController.setLists(state.syncSet);
             } else {
                 state.selSyncController.setLists(new JImageListView[0]);
             }
 
             if (state.zoomPanSyncCheckboxModel.isSelected()) {
                 state.zoomPanSyncController.setLists(state.syncSet);
             } else {
                 state.zoomPanSyncController.setLists(new JImageListView[0]);
             }
         }
     }
     
     public JFrame newMultiListFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
         final JFrame theFrame = (graphicsConfig == null ? new JFrame(frameTitle) : new JFrame(frameTitle, graphicsConfig));
         JToolBar toolbar = new JToolBar("toolbar");
         toolbar.setFloatable(false);
         
         List<ListModel> listModels = new ArrayList<ListModel>();
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00900__center10102")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00901__center14146")));
 
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00900__center10102")));
         ///*
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00901__center14146")));
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00902__center10101")));
         /*
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00903__center10101")));
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00904__center10101")));
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00905__center10101")));
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00906__center10102")));
         listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00907__center10102")));
         //*/
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00908__center10101")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00909__center10101")));
         //*/
         
         List<JImageListView> lists = new ArrayList<JImageListView>();
         
         JPanel listsPanel = new JPanel();
         listsPanel.setLayout(new GridLayout((listModels.size() - 1) / 3 + 1, Math.min(listModels.size(), 3), 10, 10));
         for (ListModel lm : listModels) {
             final ListViewPanel lvp = new ListViewPanel();
             lvp.getListView().setModel(lm);
             listsPanel.add(lvp);
             lists.add(lvp.getListView());
             for (final Color c : syncColors) {
                 final JCheckBox cb = new JCheckBox();
                 cb.setBackground(c);
                 lvp.getToolbar().add(cb);
                 cb.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         if (cb.isSelected()) {
                             syncColorStates.get(c).syncSet.add(lvp.getListView());
                         } else {
                             syncColorStates.get(c).syncSet.remove(lvp.getListView());
                         }
                         updateSyncControllers();
                     }
                 });
             }
         }
 
         ImageListViewSelectionSynchronizationController selSyncController = new ImageListViewSelectionSynchronizationController();
         selSyncController.setLists(lists.toArray(new JImageListView[lists.size()]));
         selSyncController.setKeepRelativeSelectionIndices(true);
 
         toolbar.add(new JLabel("Sync: "));
         for (Color c : syncColors) {
             SyncColorState state = syncColorStates.get(c);
             
             JCheckBox cb = new JCheckBox("selections");
             cb.setModel(state.selSyncCheckboxModel);
             cb.setBackground(c);
             toolbar.add(cb);
             
             cb = new JCheckBox("windowing");
             cb.setModel(state.windowingSyncCheckboxModel);
             cb.setBackground(c);
             toolbar.add(cb);
 
             cb = new JCheckBox("zoom/pan");
             cb.setModel(state.zoomPanSyncCheckboxModel);
             cb.setBackground(c);
             toolbar.add(cb);
         }
         
         theFrame.getContentPane().add(listsPanel, BorderLayout.CENTER);
         theFrame.getContentPane().add(toolbar, BorderLayout.PAGE_START);
         theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         theFrame.setSize(1250, 800);
         theFrame.setVisible(true);
         
         return theFrame;
     }
 
     private class ListViewPanel extends JPanel {
         private JToolBar toolbar;
         private JImageListView listView;
         
         public ListViewPanel() {
             this.setLayout(new BorderLayout());
             listView = newJGLImageListView();
             this.add(listView, BorderLayout.CENTER);
             new ImageListViewInitialWindowingController(listView).setEnabled(true);
             new ImageListViewMouseWindowingController(listView);
             new ImageListViewMouseZoomPanController(listView);
             new ImageListViewRoiInputEventController(listView);
             new ImageListViewImagePaintController(listView).setEnabled(true);
             
             ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(listView);
             sssc.setScrollPositionTracksSelection(true);
             sssc.setSelectionTracksScrollPosition(true);
             sssc.setAllowEmptySelection(false);
             sssc.setEnabled(true);
             
             final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(listView) {
                 @Override
                 protected String[] getTextToPrint(ImageListViewCell cell) {
                     DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                     DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                     return new String[] {
                             "PN: " + dicomImageMetaData.getString(Tag.PatientName),
                             "SL: " + dicomImageMetaData.getString(Tag.SliceLocation),
                             "wl/ww: " + cell.getWindowLocation() + "/" + cell.getWindowWidth(),
                             "Zoom: " + cell.getScale(),
                             "Slice orientation: " + DicomUtil.getSliceOrientation(dicomImageMetaData)
                     };
                 }
             };
             ptc.setEnabled(true);
             
             new ImageListViewMouseMeasurementController(listView).setEnabled(true);
 
             toolbar = new JToolBar();
             toolbar.setFloatable(false);
             this.add(toolbar, BorderLayout.PAGE_START);
 
             toolbar.add(new JLabel("ScaleMode:"));
             final JComboBox scaleModeCombo = new JComboBox();
             for (JImageListView.ScaleMode sm : listView.getSupportedScaleModes()) {
                 scaleModeCombo.addItem(sm);
             }
             //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(6, 6));
             //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(7, 7));
             //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(8, 8));
             toolbar.add(scaleModeCombo);
             scaleModeCombo.setEditable(false);
             Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                        listView, BeanProperty.create("scaleMode"),
                                        scaleModeCombo, BeanProperty.create("selectedItem")).bind();
 
             toolbar.add(new JLabel("lut:"));
             final JComboBox lutCombo = new JComboBox();
             for (LookupTable lut : LookupTables.getAllKnownLuts()) {
                 lutCombo.addItem(lut);
             }
             lutCombo.setRenderer(new LookupTableCellRenderer(70));
             lutCombo.addItemListener(new ItemListener() {
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     if (e.getStateChange() == ItemEvent.SELECTED) {
                         LookupTable lut = (LookupTable) lutCombo.getSelectedItem();
                         System.out.println("activating lut: " + lut);
                         for (int i = 0; i < listView.getLength(); i++) {
                             listView.getCell(i).setLookupTable(lut);
                         }
                         // TODO: apply to newly added cells. Have a controller to generalize this for arbitrary cell properties
                     }
                 }
             });
             toolbar.add(lutCombo);
             /*
             toolbar.add(new AbstractAction("setEmptyModel") {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     listView.setModel(new DefaultListModel());
                 }
             });
             */
             
             ImageListViewWindowingApplyToAllController wndAllController = new ImageListViewWindowingApplyToAllController(listView);
             JCheckBox wndAllCheckbox = new JCheckBox("wnd.all");
             toolbar.add(wndAllCheckbox);
             Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                     wndAllController, BeanProperty.create("enabled"),
                     wndAllCheckbox, BeanProperty.create("selected")).bind();
         }
 
         public JImageListView getListView() {
             return listView;
         }
         
         public JToolBar getToolbar() {
             return toolbar;
         }
 
     }
     
     
     
     
     protected JListImageListView newJListImageListView() {
         JListImageListView viewer = new JListImageListView();
         viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
         return viewer;
     }
     
     protected JGridImageListView newJGridImageListView(boolean useOpenglRenderer) {
         final JGridImageListView viewer = new JGridImageListView();
         viewer.setScaleMode(JGridImageListView.MyScaleMode.newCellGridMode(2, 2));
         if (useOpenglRenderer) {
             ((JGridImageListView) viewer).setRendererType(JGridImageListView.RendererType.OPENGL);
         }
         viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
         return viewer;
     }
 
     protected JGLImageListView newJGLImageListView() {
         final JGLImageListView viewer = new JGLImageListView();
         viewer.setScaleMode(JGLImageListView.MyScaleMode.newCellGridMode(5, 5));
         viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
         viewer.setScaleMode(JGLImageListView.MyScaleMode.newCellGridMode(2, 2));
         viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
         return viewer;
     }
 
     protected static DefaultListModel getViewerListModelForDirectory(File dir) {
         DefaultListModel result = new DefaultListModel();
         File[] files = dir.listFiles();
         Arrays.sort(files);
         for (File f : files) {
             if (!f.getName().toLowerCase().endsWith(".dcm")) {
                 continue;
             }
             result.addElement(new FileBasedDicomImageListViewModelElement(f));
         }
         System.err.println("" + result.size() + " images found in " + dir);
         return result;
     }
     
     protected static DefaultListModel getTestImageViewerListModel() {
         final DefaultListModel result = new DefaultListModel();
         for (int i = 10; i < 90; i++) {
             result.addElement(new TestImageModelElement(i));
         }
         return result;
     }
     
 
     public static void main(String[] args) throws Exception {
         //System.out.println("press enter..."); System.in.read();   // use when profiling startup performance
         System.out.println("go");
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     new JListImageListTestApp();
                 } catch (Exception e) {
                     System.err.println("Exception during UI initialization (before event loop start). Exiting.");
                     e.printStackTrace();
                     System.exit(-1);
                 }
             }
         });
     }
 }
