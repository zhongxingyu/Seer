 /* 
  * Copyright (C) 2002-2008 by Brockmann Consult
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.chris.ui;
 
 import com.bc.ceres.core.ProgressMonitor;
 import com.bc.ceres.swing.SwingHelper;
 import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
 import com.jidesoft.grid.ColorCellEditor;
 import com.jidesoft.grid.ColorCellRenderer;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.RasterDataNode;
 import org.esa.beam.framework.ui.PixelPositionListener;
 import org.esa.beam.framework.ui.UIUtils;
 import org.esa.beam.framework.ui.product.ProductSceneView;
 import org.esa.beam.visat.VisatApp;
 
 import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.RenderedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.concurrent.ExecutionException;
 
 /**
  * Cluster labeling tool view.
  *
  * @author Marco Peters
  * @author Ralf Quast
  * @author Marco Zhlke
  * @version $Revision$ $Date$
  */
 public class ClusterLabelingWindow extends JDialog {
 
     private final CloudLabeler cloudLabeler;
     private final VisatApp visatApp;
 
     private LabelTableModel tableModel;
     private JCheckBox abundancesCheckBox;
     private JTable table;
     private PixelPositionListener pixelPositionListener;
     private VetoablePsvCloseListener clusterMapCloseListener;
     private VetoablePsvCloseListener rgbFrameCloseListener;
     private JInternalFrame clusterMapFrame;
     private JInternalFrame rgbFrame;
 
     public ClusterLabelingWindow(CloudLabeler cloudLabeler) {
         super(VisatApp.getApp().getMainFrame(),
               MessageFormat.format("CHRIS/PROBA Cloud Labeling Tool - {0}", cloudLabeler.getRadianceProduct().getName()),
               false);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         this.cloudLabeler = cloudLabeler;
         visatApp = VisatApp.getApp();
         getContentPane().add(createControl());
         pixelPositionListener = new ClusterClassSelector(cloudLabeler);
     }
 
     @Override
     public void dispose() {
         if (clusterMapFrame != null && !clusterMapFrame.isClosed()) {
             clusterMapFrame.removeVetoableChangeListener(clusterMapCloseListener);
             closeFrame(clusterMapFrame);
         }
         clusterMapFrame = null;
 
         if (rgbFrame != null && !rgbFrame.isClosed()) {
             rgbFrame.removeVetoableChangeListener(rgbFrameCloseListener);
             closeFrame(rgbFrame);
         }
         rgbFrame = null;
         super.dispose();
     }
 
     private void closeFrame(JInternalFrame internalFrame) {
         if (internalFrame != null) {
             final ProductSceneView sceneView = getProductSceneView(internalFrame);
             if (sceneView != null && sceneView.getImageDisplay() != null) {
                 sceneView.getImageDisplay().removePixelPositionListener(pixelPositionListener);
                 visatApp.getDesktopPane().closeFrame(internalFrame);
             }
         }
     }
 
     @Override
     public void setVisible(boolean b) {
         if (b) {
             if (rgbFrame == null) {
                 final ProductSceneView rgbView = cloudLabeler.getRgbSceneView();
                 final String title = MessageFormat.format("{0} - RGB", cloudLabeler.getRadianceProduct().getName());
                 final Icon icon = UIUtils.loadImageIcon("icons/RsBandAsSwath16.gif");
                 rgbView.getScene().setName(title);
                 rgbView.setCommandUIFactory(visatApp.getCommandUIFactory());
                 rgbView.setNoDataOverlayEnabled(false);
                 rgbView.setROIOverlayEnabled(false);
                 rgbView.setGraticuleOverlayEnabled(false);
                 rgbView.setPinOverlayEnabled(false);
                 rgbView.setLayerProperties(visatApp.getPreferences());
                 rgbView.getImageDisplay().addPixelPositionListener(pixelPositionListener);
 
                 rgbFrame = visatApp.createInternalFrame(title, icon, rgbView, "");
                 rgbFrameCloseListener = new VetoablePsvCloseListener();
                 rgbFrame.addVetoableChangeListener(rgbFrameCloseListener);
             }
 
             final Band clusterMapBand = cloudLabeler.getClusterMapBand();
             clusterMapFrame = visatApp.findInternalFrame(clusterMapBand);
             if (clusterMapFrame == null) {
                 InternalFrameListener ifl = new ClusterMapFrameHandler(
                         clusterMapBand, cloudLabeler.getRadianceProduct().getDisplayName());
                 visatApp.addInternalFrameListener(ifl);
                 visatApp.openProductSceneView(clusterMapBand, "");
             }
             pack();
             SwingHelper.centerComponent(this, VisatApp.getApp().getMainFrame());
         }
         super.setVisible(b);
     }
 
     private static ProductSceneView getProductSceneView(JInternalFrame internalFrame) {
         final Container contentPane = internalFrame.getContentPane();
         if (contentPane instanceof ProductSceneView) {
             return (ProductSceneView) contentPane;
         }
         return null;
     }
 
     private class ClusterMapFrameHandler extends InternalFrameAdapter {
 
         private final Band clusterMapBand;
         private final String displayName;
 
         private ClusterMapFrameHandler(Band clusterMapBand, String displayName) {
             this.clusterMapBand = clusterMapBand;
             this.displayName = displayName;
         }
 
         @Override
         public void internalFrameOpened(InternalFrameEvent e) {
             final JInternalFrame internalFrame = e.getInternalFrame();
             final ProductSceneView productSceneView = getProductSceneView(internalFrame);
             if (productSceneView.getRaster() == clusterMapBand) {
                 clusterMapFrame = internalFrame;
                 clusterMapCloseListener = new VetoablePsvCloseListener();
                 clusterMapFrame.addVetoableChangeListener(clusterMapCloseListener);
                 visatApp.removeInternalFrameListener(this);
                 internalFrame.setTitle(displayName + " - Cluster Map");
                 productSceneView.getImageDisplay().addPixelPositionListener(pixelPositionListener);
             }
         }
 
     }
 
     private class VetoablePsvCloseListener implements VetoableChangeListener {
 
         public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
             if (JInternalFrame.IS_CLOSED_PROPERTY.equals(evt.getPropertyName())) {
                 if ((Boolean) evt.getNewValue()) {
                     if (visatApp.showQuestionDialog("Do you want to exit the cloud labeling tool?",
                                                     null) == JOptionPane.NO_OPTION) {
                         throw new PropertyVetoException("Do not close.", evt);
                     } else {
                         dispose();
                         cloudLabeler.disposeSourceProducts();
                     }
                 }
             }
         }
     }
 
     private JComponent createControl() {
 
         final JButton createButton = new JButton("Create Cloud Mask");
         createButton.setMnemonic('M');
         createButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 new CloudProductSwingWorker().execute();
                 dispose();
             }
         });
 
         final JButton closeButton = new JButton("Close");
         closeButton.setMnemonic('C');
         closeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 cloudLabeler.disposeSourceProducts();
                 dispose();
             }
         });
 
         final JButton helpButton = new JButton("Help");
         helpButton.setMnemonic('H');
         helpButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                // todo
             }
         });
 
         final JPanel mainPanel = new JPanel(new BorderLayout(4, 4));
         mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 
         final JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 
         buttonRow.add(createButton);
         buttonRow.add(closeButton);
         buttonRow.add(helpButton);
 
         tableModel = new LabelTableModel(cloudLabeler.getClusterMapBand().getImageInfo());
         tableModel.setCloudClusterIndexes(new int[0]);
         tableModel.setRejectedIndexes(new int[0]);
         tableModel.setClusterProperties(cloudLabeler.getClusterProperties());
         table = new JTable(tableModel);
         table.getColumnModel().getColumn(4).setCellRenderer(new BrightnessRenderer());
         table.setDefaultRenderer(Double.class, new PercentageRenderer());
         final ColorCellRenderer colorCellRenderer = new ColorCellRenderer();
         colorCellRenderer.setColorValueVisible(false);
         table.setDefaultRenderer(Color.class, colorCellRenderer);
         table.setDefaultEditor(Color.class, new ColorCellEditor());
         table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         final JScrollPane tableScrollPane = new JScrollPane(table);
         tableScrollPane.getViewport().setPreferredSize(table.getPreferredSize());
 
         final JPanel tablePanel = new JPanel(new BorderLayout(2, 2));
         tablePanel.add(tableScrollPane, BorderLayout.CENTER);
         abundancesCheckBox = new JCheckBox("Perform spectral unmixing on cloud pixel spectra", false);
         abundancesCheckBox.setToolTipText(
                 "Select to weight final cloud probability with cloud pixel spectrum abundances");
         abundancesCheckBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (abundancesCheckBox.isSelected()) {
                     final String message = "Spectral unmixing is extremely time consuming!";
                     visatApp.showInfoDialog("CHRIS Cloud Screening", message, "chrisbox.postLabling.showWarning");
                 }
             }
         });
         tablePanel.add(abundancesCheckBox, BorderLayout.SOUTH);
 
         mainPanel.add(tablePanel, BorderLayout.CENTER);
         mainPanel.add(buttonRow, BorderLayout.SOUTH);
 
         tableModel.addTableModelListener(new LableTableModelListener(mainPanel));
 
         updateAbundancesCheckBox();
         return mainPanel;
     }
 
     private void updateAbundancesCheckBox() {
         if (tableModel.getCloudIndexes().length > 0) {
             abundancesCheckBox.setEnabled(true);
         } else {
             abundancesCheckBox.setEnabled(false);
             abundancesCheckBox.setSelected(false);
         }
     }
 
 
     public void selectClass(int clusterIndex) {
         table.getSelectionModel().setSelectionInterval(clusterIndex, clusterIndex);
     }
 
 
     private class ClusterClassSelector implements PixelPositionListener {
 
         private final CloudLabeler cloudLabeler;
 
         public ClusterClassSelector(CloudLabeler cloudLabeler) {
             this.cloudLabeler = cloudLabeler;
         }
 
         public void pixelPosChanged(RenderedImage sourceImage, int pixelX, int pixelY, boolean pixelPosValid,
                                     MouseEvent e) {
             if (pixelPosValid) {
                 final int[] clusterIndex = new int[1];
                 try {
                     cloudLabeler.getClusterMapBand().readPixels(pixelX, pixelY, 1, 1, clusterIndex);
                 } catch (IOException e1) {
                     //ignore
                 }
                 selectClass(clusterIndex[0]);
             }
         }
 
         public void pixelPosNotAvailable(RenderedImage sourceImage) {
 
         }
     }
 
     private class CloudProductSwingWorker extends ProgressMonitorSwingWorker<Object, Object> {
 
         private CloudProductSwingWorker() {
             super(visatApp.getMainFrame(), "Computing cloud product...");
         }
 
         @Override
         protected Object doInBackground(ProgressMonitor pm) throws Exception {
             final int[] cloudClusterIndexes = tableModel.getCloudIndexes();
             final int[] surfaceClusterIndexes = tableModel.getSurfaceIndexes();
             cloudLabeler.performCloudProductComputation(cloudClusterIndexes,
                                                         surfaceClusterIndexes,
                                                         abundancesCheckBox.isSelected(), pm);
             return null;
         }
 
         @Override
         protected void done() {
             try {
                 get();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 e.printStackTrace();
                 visatApp.showErrorDialog(e.getCause().getMessage());
             }
             updateImage(cloudLabeler.getCloudProductBand());
             cloudLabeler.disposeSourceProducts();
         }
 
         private void updateImage(RasterDataNode dataNode) {
             final JInternalFrame internalFrame = visatApp.findInternalFrame(dataNode);
             if (internalFrame != null) {
                 final ProductSceneView sceneView = (ProductSceneView) internalFrame.getContentPane();
                 visatApp.updateImage(sceneView);
             }
         }
 
     }
 
     private class LableTableModelListener implements TableModelListener {
 
         private final JPanel mainPanel;
 
         public LableTableModelListener(JPanel mainPanel) {
             this.mainPanel = mainPanel;
         }
 
         public void tableChanged(TableModelEvent e) {
             if (e.getColumn() == 1) { // color
                 applyLabeling(true);
             } else if (e.getColumn() == 3) { // rejected
                 applyLabeling(false);
             } else if (e.getColumn() == 2) { // cloud
                 updateAbundancesCheckBox();
             }
         }
 
         private void applyLabeling(boolean updateColors) {
             final Cursor oldCursor = mainPanel.getCursor();
             mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             try {
                 if (!updateColors) {
                     cloudLabeler.performLabelingStep(tableModel.getRejectedIndexes());
                     tableModel.setClusterProperties(cloudLabeler.getClusterProperties());
                 }
                 Band membershipBand = cloudLabeler.getClusterMapBand();
                 membershipBand.setImageInfo(tableModel.getImageInfo().createDeepCopy());
                 updateImage(membershipBand);
             } finally {
                 mainPanel.setCursor(oldCursor);
             }
         }
 
         private void updateImage(RasterDataNode dataNode) {
             final JInternalFrame internalFrame = visatApp.findInternalFrame(dataNode);
             if (internalFrame != null) {
                 final ProductSceneView sceneView = (ProductSceneView) internalFrame.getContentPane();
                 visatApp.updateImage(sceneView);
             }
         }
 
 
     }
 }
