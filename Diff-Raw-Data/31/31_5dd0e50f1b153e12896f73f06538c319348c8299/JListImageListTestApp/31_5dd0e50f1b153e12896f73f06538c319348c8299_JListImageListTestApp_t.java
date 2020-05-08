 package de.sofd.viskit.ui.imagelist.jlistimpl.test;
 
 import de.sofd.draw2d.Drawing;
 import de.sofd.draw2d.DrawingObject;
 import de.sofd.draw2d.viewer.tools.EllipseTool;
 import de.sofd.draw2d.viewer.tools.SelectorTool;
import de.sofd.util.FloatRange;
 import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
 import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
 import de.sofd.viskit.controllers.ImageListViewWindowingApplyToAllController;
 import de.sofd.viskit.image.Dcm;
 import de.sofd.viskit.image.DcmImageListViewModelElement;
 import de.sofd.viskit.image.DicomInputOutput;
 import de.sofd.viskit.model.DicomImageListViewModelElement;
 import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
 import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
 import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.beans.BeanInfo;
 import java.beans.Introspector;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JToolBar;
 import javax.swing.ListModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.dcm4che2.data.BasicDicomObject;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 
 /**
  *
  * @author olaf
  */
 public class JListImageListTestApp {
 
     public JListImageListTestApp() throws Exception {
         final DefaultListModel model = new DefaultListModel();
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/resources/DICOM-Testbilder/1578"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd822__center4001"));
         //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd836__center4001"));
         for (int i = 10; i < 90; i++) {
             model.addElement(new TestImageModelElement(i));
             //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/1578/f0003563_006" + i + ".dcm"));
             //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/24-bit Uncompressed Color.dcm"));
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
         final JImageListView viewer = new JGridImageListView(); viewer.setScaleMode(JGridImageListView.MyScaleMode.newCellGridMode(2, 2));
        // set up initial windowing for all cell to optimal values.
        // happens on cell creation, i.e. right at the start, thus increasing initialization time very much.
        // Might use a controller that does this in the first cellPaint event for the cell instead
        // (once we have cellPaint events)
        viewer.addImageListViewListener(new ImageListViewListener() {
            @Override
            public void onImageListViewEvent(ImageListViewEvent e) {
                if (e instanceof ImageListViewCellAddEvent) {
                    ImageListViewCellAddEvent cae = (ImageListViewCellAddEvent) e;
                    ImageListViewCell addedCell = cae.getCell();
                    FloatRange usedRange = addedCell.getDisplayedModelElement().getUsedPixelValuesRange();
                    addedCell.setWindowWidth((int) usedRange.getDelta());
                    addedCell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                }
            }
        });
         ((JGridImageListView)viewer).setRendererType(JGridImageListView.RendererType.JAVA2D);
         viewer.addImageListViewListener(new ImageListViewListener() {
             @Override
             public void onImageListViewEvent(ImageListViewEvent e) {
                 System.out.println("ImageListViewEvent: " + e);
             }
         });
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
 
         JFrame f = new JFrame("JListImageListView test");
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
                 try {
                     ImageListViewModelElement elt = viewer.getSelectedValue();
                     if (null != elt) {
                         ImageListViewCell cell = viewer.getCellForElement(elt);
                         setWindowingToOptimal(cell);
                     }
                 } catch (IndexOutOfBoundsException ex) {
                     System.out.println("list has no 4th element or 4th element contains no ROIs...");
                 }
             }
         });
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
         scaleModeCombo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JImageListView.ScaleMode sm = (JImageListView.ScaleMode) scaleModeCombo.getModel().getSelectedItem();
                 viewer.setScaleMode(sm);
             }
         });
 
         new ImageListViewMouseWindowingController(viewer);
         new ImageListViewMouseZoomPanController(viewer);
         new ImageListViewRoiInputEventController(viewer);
         //new ImageListViewWindowingApplyToAllController(viewer).setEnabled(true);
 
         f.getContentPane().add(viewer, BorderLayout.CENTER);
         f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setSize(700, 700);
         f.setVisible(true);
     }
 
     private static void setWindowingToDcm(ImageListViewCell cell) {
         if (!(cell.getDisplayedModelElement() instanceof DicomImageListViewModelElement)) { return; }
         DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
         DicomObject dobj = elt.getDicomImageMetaData();
         if (dobj.contains(Tag.WindowCenter) && dobj.contains(Tag.WindowWidth)) {
             cell.setWindowLocation((int) dobj.getFloat(Tag.WindowCenter));
             cell.setWindowWidth((int) dobj.getFloat(Tag.WindowWidth));
         }
     }
 
     private static void setWindowingToOptimal(ImageListViewCell cell) {
         ImageListViewModelElement elt = cell.getDisplayedModelElement();
         BufferedImage rawImg = elt.getImage();
         int numBands = rawImg.getRaster().getNumBands();
         int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
         for (int x = 0; x < rawImg.getWidth(); x++) {
             for (int y = 0; y < rawImg.getHeight(); y++) {
                 for (int band = 0; band < numBands; band++) {
                     int value = rawImg.getRaster().getSample(x, y, band);
                     if (value < min) { min = value; }
                     if (value > max) { max = value; }
                 }
             }
         }
         cell.setWindowLocation((min + max) / 2);
         cell.setWindowWidth(max - min);
     }
 
     protected static DefaultListModel getViewerListModelForDirectory(File dir) {
         DefaultListModel result = new DefaultListModel();
         File[] files = dir.listFiles();
         Arrays.sort(files);
         for (File f: files) {
             if (!f.getName().toLowerCase().endsWith(".dcm")) { continue; }
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
