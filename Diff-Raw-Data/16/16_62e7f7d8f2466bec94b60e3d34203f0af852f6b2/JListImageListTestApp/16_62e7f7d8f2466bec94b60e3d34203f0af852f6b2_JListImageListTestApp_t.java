 package de.sofd.viskit.ui.imagelist.jlistimpl.test;
 
import de.sofd.draw2d.Drawing;
import de.sofd.draw2d.DrawingObject;
 import de.sofd.draw2d.viewer.tools.EllipseTool;
 import de.sofd.draw2d.viewer.tools.SelectorTool;
 import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
 import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
 import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
 import de.sofd.viskit.image.Dcm;
 import de.sofd.viskit.image.DcmImageListViewModelElement;
 import de.sofd.viskit.image.DicomInputOutput;
 import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
 import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
 import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
 import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.dcm4che2.data.BasicDicomObject;
 
 /**
  *
  * @author olaf
  */
 public class JListImageListTestApp {
 
     public JListImageListTestApp() {
         final DefaultListModel model = new DefaultListModel();
         for (int i = 0; i < 10; i++) {
             model.addElement(new TestImageModelElement(i));
         }
 
         URL url = this.getClass().getResource("67010.dcm");
         //url = new URL("file:///I:/DICOM/dcm4che-2.0.18-bin/dcm4che-2.0.18/bin/67010");
         BasicDicomObject basicDicomObject = DicomInputOutput.read(url);
         Dcm dcm = new Dcm(url, basicDicomObject);
         DcmImageListViewModelElement dcmImageListViewModelElement = new DcmImageListViewModelElement(dcm);
         model.addElement(dcmImageListViewModelElement);
 
         final JImageListView viewer = new JListImageListView();
         //final JImageListView viewer = new JGridImageListView();
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
                 System.out.println("cell propChanged " + evt.getPropertyName() + " => " + evt.getNewValue() + " in cell " + evt.getSource());
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
         }
 
         JFrame f = new JFrame("JListImageListView test");
         JToolBar toolbar = new JToolBar("toolbar");
         toolbar.setFloatable(false);
         toolbar.add(new AbstractAction("+Img") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ImageListViewModelElement newElt = new TestImageModelElement(99);
                 model.addElement(newElt);
                 viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
             }
         });
         toolbar.add(new AbstractAction("-Sel") {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ImageListViewModelElement elt = viewer.getSelectedValue();
                 if (elt != null) {
                     model.removeElement(elt);
                 }
             }
         });
        toolbar.add(new AbstractAction("1stRoiMv") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ImageListViewModelElement elt = viewer.getElementAt(0);
                    Drawing roiDrawing = elt.getRoiDrawing();
                    DrawingObject roi = roiDrawing.get(0);
                    roi.moveBy(10, 5);
                } catch (IndexOutOfBoundsException ex) {
                    System.out.println("list empty or first element contains no ROIs...");
                }
            }
        });
         toolbar.add(new JLabel("ScaleMode:"));
         final JComboBox scaleModeCombo = new JComboBox();
         for (JImageListView.ScaleMode sm : viewer.getSupportedScaleModes()) {
             scaleModeCombo.addItem(sm);
         }
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
 
         f.getContentPane().add(viewer, BorderLayout.CENTER);
         f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setSize(700, 700);
         f.setVisible(true);
     }
 
 
     public static void main(String[] args) throws Exception {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new JListImageListTestApp();
             }
         });
     }
 
 }
