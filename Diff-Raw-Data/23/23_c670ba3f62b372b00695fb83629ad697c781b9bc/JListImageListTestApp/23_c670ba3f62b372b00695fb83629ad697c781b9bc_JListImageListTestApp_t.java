 package de.sofd.viskit.ui.imagelist.jlistimpl.test;
 
 import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.dcm4che2.data.BasicDicomObject;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 
 import de.sofd.draw2d.Drawing;
 import de.sofd.draw2d.DrawingObject;
 import de.sofd.draw2d.viewer.tools.EllipseTool;
 import de.sofd.draw2d.viewer.tools.SelectorTool;
 import de.sofd.util.FloatRange;
 import de.sofd.viskit.controllers.ImageListViewInitialWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
 import de.sofd.viskit.controllers.ImageListViewPrintTextToCellsController;
 import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
 import de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController;
 import de.sofd.viskit.controllers.ImageListViewWindowingApplyToAllController;
 import de.sofd.viskit.image.Dcm;
 import de.sofd.viskit.image.DcmImageListViewModelElement;
 import de.sofd.viskit.image.DicomInputOutput;
 import de.sofd.viskit.model.DicomImageListViewModelElement;
 import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.ui.RoiToolPanel;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
 import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
 
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
        JFrame f1 = newFrame("Viskit ImageList test app window 1", null);
        JFrame f2 = newFrame("Viskit ImageList test app window 2", null);
    }
    
    public JFrame newFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
         //final DefaultListModel model = new DefaultListModel();
         final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/resources/DICOM-Testbilder/1578"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd822__center4001"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd836__center4001"));
         for (int i = 10; i < 90; i++) {
             //model.addElement(new TestImageModelElement(i));
             //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/1578/f0003563_006" + i + ".dcm"));
             //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/24-bit Uncompressed Color.dcm"));
             //model.addElement(new FileBasedDicomImageListViewModelElement("/shares/projects/DICOM-Testbilder/1578/f0003563_006"+i+".dcm"));
         }
 
         URL url = this.getClass().getResource("67010.dcm");
         //url = new URL("file:///I:/DICOM/dcm4che-2.0.18-bin/dcm4che-2.0.18/bin/67010");
         BasicDicomObject basicDicomObject = DicomInputOutput.read(url);
         Dcm dcm = new Dcm(url, basicDicomObject);
         DcmImageListViewModelElement dcmImageListViewModelElement = new DcmImageListViewModelElement(dcm);
         //model.addElement(dcmImageListViewModelElement);
 
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/cd846__center4001__39.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series1/cd014__center001__0.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series2/cd014__center001__25.dcm")));
         //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/24-bit Uncompressed Color.dcm"));
 
         //final JImageListView viewer = new JListImageListView();
         final JImageListView viewer = new JGridImageListView();
         viewer.setScaleMode(JGridImageListView.MyScaleMode.newCellGridMode(2, 2));
         new ImageListViewInitialWindowingController(viewer).setEnabled(true);
         ((JGridImageListView) viewer).setRendererType(JGridImageListView.RendererType.OPENGL);
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
 
         toolbar.add(new AbstractAction("tgglTxt") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ptc.setEnabled(!ptc.isEnabled());
                 //viewer.refreshCells();
             }
         });
         
         f.getContentPane().add(viewer, BorderLayout.CENTER);
         f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setSize(900, 900);
         f.setVisible(true);
        
        return f;
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
 
     public static void main(String[] args) throws Exception {
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
