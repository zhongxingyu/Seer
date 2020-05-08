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
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
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
 import de.sofd.viskit.controllers.ImageListViewZoomPanApplyToAllController;
 import de.sofd.viskit.controllers.MultiILVSyncSetController;
 import de.sofd.viskit.controllers.MultiImageListViewController;
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
 import de.sofd.viskit.ui.imagelist.ImageListViewCell.CompositingMode;
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
                             model.addElement(new FileBasedDicomImageListViewModelElement(jFileChooser1.getSelectedFile().getPath() + File.separator + children[i]));
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
     
     MultiILVSyncSetController multiSyncSetController = new MultiILVSyncSetController();
     {
         for (Color c : syncColors) {
             multiSyncSetController.addSyncSet(c);
         }
         multiSyncSetController.addSyncControllerType("selection", new MultiILVSyncSetController.SyncControllerFactory() {
             @Override
             public MultiImageListViewController createController() {
                 ImageListViewSelectionSynchronizationController result = new ImageListViewSelectionSynchronizationController();
                 result.setKeepRelativeSelectionIndices(true);
                 result.setEnabled(true);
                 return result;
             }
         });
         multiSyncSetController.addSyncControllerType("windowing", new MultiILVSyncSetController.SyncControllerFactory() {
             @Override
             public MultiImageListViewController createController() {
                 GenericILVCellPropertySyncController result = new GenericILVCellPropertySyncController(new String[]{"windowLocation", "windowWidth"});
                 result.setEnabled(true);
                 return result;
             }
         });
         multiSyncSetController.addSyncControllerType("zoompan", new MultiILVSyncSetController.SyncControllerFactory() {
             @Override
             public MultiImageListViewController createController() {
                 GenericILVCellPropertySyncController result = new GenericILVCellPropertySyncController(new String[]{"scale", "centerOffset"});
                 result.setEnabled(true);
                 return result;
             }
         });
     }
     
     public JFrame newMultiListFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
         final JFrame theFrame = (graphicsConfig == null ? new JFrame(frameTitle) : new JFrame(frameTitle, graphicsConfig));
         JToolBar toolbar = new JToolBar("toolbar");
         toolbar.setFloatable(false);
         
         List<ListModel> listModels = new ArrayList<ListModel>();
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/headvolume")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/series1")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/INCISIX")));
         listModels.add(getViewerListModelForDirectory(new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd810__center4001")));
         //listModels.add(getViewerListModelForDirectory(new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd833__center4001")));
         listModels.add(getViewerListModelForDirectory(new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd865__center4001")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/ARTIFIX")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/BRAINIX")));
         //listModels.add(getViewerListModelForDirectory(new File("/tmp/cd00926__center10101")));
         //listModels.add(getViewerListModelForDirectory(new File("/tmp/cd00927__center10103")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00900__center10102")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00901__center14146")));
         
         //listModels.add(getViewerListModelForDirectory(new File("/home/sofd/disk88888/Images/cd88888010__center100")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00900__center10102")));
         ///*
         //listModels.add(getViewerListModelForDirectory(new File("/Users/fokko/disk312046/Images/cd00903__center10101")));
         //listModels.add(getViewerListModelForDirectory(new File("/Users/fokko/disk312046/Images/cd00904__center10101")));
         //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/disk312046/Images/cd00917__center10102")));
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
                 final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
                 final JCheckBox cb = new JCheckBox();
                 cb.setBackground(c);
                 lvp.getToolbar().add(cb);
                 cb.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         if (cb.isSelected()) {
                             syncSet.addList(lvp.getListView());
                         } else {
                             syncSet.removeList(lvp.getListView());
                         }
                     }
                 });
             }
         }
 
         toolbar.add(new JLabel("Sync: "));
         for (Color c : syncColors) {
             final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
             
             JCheckBox cb = new JCheckBox("selections");
             cb.setModel(syncSet.getIsControllerSyncedModel("selection"));
             cb.setBackground(c);
             toolbar.add(cb);
             
             cb = new JCheckBox("windowing");
             cb.setModel(syncSet.getIsControllerSyncedModel("windowing"));
             cb.setBackground(c);
             toolbar.add(cb);
 
             cb = new JCheckBox("zoom/pan");
             cb.setModel(syncSet.getIsControllerSyncedModel("zoompan"));
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
             //listView = newJGridImageListView(false);
             this.add(listView, BorderLayout.CENTER);
             new ImageListViewInitialWindowingController(listView) {
                 @Override
                 protected void initializeCell(final ImageListViewCell cell) {
                     try {
                         final DicomImageListViewModelElement delt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                         ImageListViewWindowingApplyToAllController.runWithAllControllersInhibited(new Runnable() {
                             @Override
                             public void run() {
                                 double wl = delt.getDicomImageMetaData().getDouble(Tag.WindowCenter);
                                 double ww = delt.getDicomImageMetaData().getDouble(Tag.WindowWidth);
                                 cell.setWindowWidth((int)ww);
                                 cell.setWindowLocation((int)wl);
                             }
                         });
                     } catch (Exception e) {
                         super.initializeCell(cell);
                     }
                 }
             }.setEnabled(true);
             new ImageListViewMouseWindowingController(listView);
             new ImageListViewMouseZoomPanController(listView);
             new ImageListViewRoiInputEventController(listView);
             new ImageListViewImagePaintController(listView).setEnabled(true);
             
             ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(listView);
             sssc.setScrollPositionTracksSelection(true);
             sssc.setSelectionTracksScrollPosition(true);
             sssc.setAllowEmptySelection(false);
             sssc.setEnabled(true);
             
            final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(listView, JImageListView.PAINT_ZORDER_IMAGE-1) {
                 @Override
                 protected String[] getTextToPrint(ImageListViewCell cell) {
                     final DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                     DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                     return new String[] {
                             "" + elt.getImageKey(),
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
             /*
             Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                        listView, BeanProperty.create("scaleMode"),
                                        scaleModeCombo, BeanProperty.create("selectedItem")).bind();
             */
             scaleModeCombo.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     // if exactly one element is selected and visible, make sure it stays so after the scaleMode change
                     ListSelectionModel sm = listView.getSelectionModel();
                     int oldSelIdx = sm.getMinSelectionIndex();
                     boolean mustRetainOldIdx = oldSelIdx != -1 && sm.getMaxSelectionIndex() == oldSelIdx && listView.isVisibleIndex(oldSelIdx);
                     listView.setScaleMode((JImageListView.ScaleMode) scaleModeCombo.getSelectedItem());
                     if (mustRetainOldIdx && ! listView.isVisibleIndex(oldSelIdx)) {
                         listView.getSelectionModel().setLeadSelectionIndex(oldSelIdx);
                     }
                 }
             });
             scaleModeCombo.setSelectedItem(listView.getScaleMode());
             
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
             final JCheckBox alphaBlendCheckbox = new JCheckBox("blend");
             toolbar.add(alphaBlendCheckbox);
             alphaBlendCheckbox.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     ImageListViewCell.CompositingMode cm = alphaBlendCheckbox.isSelected() ? CompositingMode.CM_BLEND : CompositingMode.CM_REPLACE;
                     for (int i = 0; i < listView.getLength(); i++) {
                         listView.getCell(i).setCompositingMode(cm);
                     }
                 }
             });
             */
 
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
 
             ImageListViewZoomPanApplyToAllController zpAllController = new ImageListViewZoomPanApplyToAllController(listView);
             JCheckBox zpAllCheckbox = new JCheckBox("z/p all");
             toolbar.add(zpAllCheckbox);
             Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                     zpAllController, BeanProperty.create("enabled"),
                     zpAllCheckbox, BeanProperty.create("selected")).bind();
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
