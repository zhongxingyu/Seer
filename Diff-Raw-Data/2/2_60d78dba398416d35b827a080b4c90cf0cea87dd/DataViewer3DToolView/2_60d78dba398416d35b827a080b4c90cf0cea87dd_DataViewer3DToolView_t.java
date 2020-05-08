 /**
  * 
  */
 package org.esa.beam.dataViewer3D.beamIntegration;
 
 import static org.esa.beam.dataViewer3D.utils.NumberTypeUtils.castToType;
 
 import java.awt.BorderLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import org.esa.beam.dataViewer3D.data.coordinates.CoordinatesSystem;
 import org.esa.beam.dataViewer3D.data.dataset.AbstractDataSet;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet3D;
 import org.esa.beam.dataViewer3D.data.dataset.DataSet4D;
 import org.esa.beam.dataViewer3D.data.source.BandDataSource;
 import org.esa.beam.dataViewer3D.gui.DataViewer;
 import org.esa.beam.dataViewer3D.gui.JOGLDataViewer;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductNodeList;
 import org.esa.beam.framework.ui.application.support.AbstractToolView;
 import org.esa.beam.framework.ui.product.ProductTreeListenerAdapter;
 import org.esa.beam.visat.VisatApp;
 
 import com.bc.ceres.swing.TableLayout;
 
 /**
  * A tool view that displays the 3D data viewer.
  * 
  * @author Martin Pecka
  */
 public class DataViewer3DToolView extends AbstractToolView
 {
 
     /** The container for the DataViewer or its placeholder button */
     protected final JPanel     viewerPane     = new JPanel();
     /** The bands this data viewer uses to generate the source data. */
     protected final List<Band> involvedBands  = new LinkedList<Band>();
     /** The expression denoting the mask for valid pixels to be read. */
     protected String           maskExpression = "";
     /** The maximum number of displayed points. */
     protected Integer          maxPoints      = null;
     /** The data viewer used to display the data. */
     protected final DataViewer dataViewer     = createDataViewer();
 
     @Override
     protected JComponent createControl()
     {
         // create the top toolbar with control buttons
         final TableLayout layout = new TableLayout(2);
         layout.setTableAnchor(TableLayout.Anchor.WEST);
         layout.setTableFill(TableLayout.Fill.HORIZONTAL);
         layout.setTablePadding(new Insets(2, 2, 2, 2));
         layout.setTableWeightX(1.0);
         final JPanel buttons = new JPanel(layout);
 
         // AbstractButton selectBandsButton = ToolButtonFactory.createButton(new SelectBandsAction(), false);
         final AbstractButton selectBandsButton = new JButton(new SelectBandsAction());
         selectBandsButton.setText("Select bands"); /* I18N */// TODO change to icon
         selectBandsButton.setToolTipText("Select bands to create the interactive view from");
         selectBandsButton.setName(getContext().getPane().getControl().getName() + ".selectBands.button");
         selectBandsButton.setEnabled(false);
 
         // AbstractButton updateViewButton = ToolButtonFactory.createButton(new UpdateViewAction(), false);
         final AbstractButton updateViewButton = new JButton(new UpdateViewAction());
         updateViewButton.setText("Update view"); /* I18N */// TODO change to icon
         updateViewButton.setToolTipText("Update the view to reflect changes in the source bands");
         updateViewButton.setName(getContext().getPane().getControl().getName() + ".updateView.button");
         updateViewButton.setEnabled(false);
 
         buttons.add(selectBandsButton);
         buttons.add(updateViewButton);
 
         // create the viewer pane and fill it with the placeholder button at the startup time
         final AbstractButton noBandsPlaceholderButton = new JButton(new SelectBandsAction());
         noBandsPlaceholderButton.setText("Click here to select bands."); /* I18N */
         noBandsPlaceholderButton.setToolTipText("Click here to select bands.");/* I18N */
         noBandsPlaceholderButton.setEnabled(false);
 
         viewerPane.setLayout(new BorderLayout());
         viewerPane.add(noBandsPlaceholderButton, BorderLayout.CENTER);
 
         final JPanel mainPanel = new JPanel(new BorderLayout());
         mainPanel.add(buttons, BorderLayout.NORTH);
         mainPanel.add(viewerPane, BorderLayout.CENTER);
 
         // update the enabled state of the buttons whenever a product is opened/closed
         VisatApp.getApp().addProductTreeListener(new ProductTreeListenerAdapter() {
 
             @Override
             public void productAdded(Product product)
             {
                 updateState();
             }
 
             @Override
             public void productRemoved(Product product)
             {
                 updateState();
             }
 
             /**
              * Update the state of the control buttons to reflect if there is at least one product open.
              */
             private void updateState()
             {
                 boolean enabled = VisatApp.getApp().getProductTree().getModel().getRoot() != null;
                 selectBandsButton.setEnabled(enabled);
                 noBandsPlaceholderButton.setEnabled(enabled);
             }
         });
 
         return mainPanel;
     }
 
     /**
      * Set the given bands as the source for this viewer.
      * <p>
      * This method doesn't update the view.
      * <p>
      * Call
      * {@link #createCoordinatesSystemForCurrentDataSet(Double, Double, Double, Double, Double, Double, Double, Double)}
      * if needed.
      * 
      * @param bandX The band used for the x axis.
      * @param bandY The band used for the y axis.
      * @param bandZ The band used for the z axis.
      */
     public void setBands(Band bandX, Band bandY, Band bandZ)
     {
         setBands(bandX, bandY, bandZ, null);
     }
 
     /**
      * Set the given bands as the source for this viewer.
      * <p>
      * This method doesn't update the view.
      * <p>
      * Call
      * {@link #createCoordinatesSystemForCurrentDataSet(Double, Double, Double, Double, Double, Double, Double, Double)}
      * if needed.
      * 
      * @param bandX The band used for the x axis.
      * @param bandY The band used for the y axis.
      * @param bandZ The band used for the z axis.
      * @param bandW The band used for the w axis.
      */
     public void setBands(Band bandX, Band bandY, Band bandZ, Band bandW)
     {
         involvedBands.clear();
         involvedBands.add(bandX);
         involvedBands.add(bandY);
         involvedBands.add(bandZ);
         if (bandW != null)
             involvedBands.add(bandW);
 
         DataSet dataSet;
         if (bandW == null) {
             // TODO allow adjusting the precision
             dataSet = AbstractDataSet.createFromDataSources(maxPoints, BandDataSource.createForBand(bandX, 10),
                     BandDataSource.createForBand(bandY, 10), BandDataSource.createForBand(bandZ, 10));
         } else {
             // TODO allow adjusting the precision
             dataSet = AbstractDataSet.createFromDataSources(maxPoints, BandDataSource.createForBand(bandX, 10),
                     BandDataSource.createForBand(bandY, 10), BandDataSource.createForBand(bandZ, 10),
                     BandDataSource.createForBand(bandW, 10));
         }
         dataViewer.setDataSet(dataSet);
     }
 
     /**
      * Create a default coordinates system for the current data set, where the bounds are computed from the data set's
      * minima and maxima.
      */
     public void createDefaultCoordinatesSystemForCurrentDataSet()
     {
         dataViewer.setCoordinatesSystem(CoordinatesSystem.createDefaultCoordinatesSystem(dataViewer.getDataSet()));
         dataViewer.getCoordinatesSystem().setShowGrid(true);
     }
 
     /**
      * Create a coordinates system for the current data set. Non-null parameters are used as bounds, <code>null</code>
      * parameters are autocomputed from the data set's minima and maxima.
      * 
      * @param xMin Minimum value for x axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param xMax Maximum value for x axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param yMin Minimum value for y axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param yMax Maximum value for y axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param zMin Minimum value for z axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param zMax Maximum value for z axis. <code>null</code> means to autocompute the value based on the dataset data.
      * @param wMin Minimum value for w axis. <code>null</code> means to autocompute the value based on the dataset data.
      *            Pass <code>null</code> if the data set is only 3-dimensional.
      * @param wMax Maximum value for w axis. <code>null</code> means to autocompute the value based on the dataset data.
      *            Pass <code>null</code> if the data set is only 3-dimensional.
      */
     @SuppressWarnings("unchecked")
     public <X extends Number, Y extends Number, Z extends Number, W extends Number> void createCoordinatesSystemForCurrentDataSet(
             Double xMin, Double xMax, Double yMin, Double yMax, Double zMin, Double zMax, Double wMin, Double wMax)
     {
         if (dataViewer.getDataSet() instanceof DataSet3D<?, ?, ?>) {
             DataSet3D<X, Y, Z> dataSet = (DataSet3D<X, Y, Z>) dataViewer.getDataSet();
             dataViewer.setCoordinatesSystem(CoordinatesSystem.createCoordinatesSystem(
                     castToType(dataSet.getMinX(), xMin), castToType(dataSet.getMinX(), xMax),
                     castToType(dataSet.getMinY(), yMin), castToType(dataSet.getMaxY(), yMax),
                     castToType(dataSet.getMinZ(), zMin), castToType(dataSet.getMaxZ(), zMax), dataSet));
         } else {
             DataSet4D<X, Y, Z, W> dataSet = (DataSet4D<X, Y, Z, W>) dataViewer.getDataSet();
             dataViewer.setCoordinatesSystem(CoordinatesSystem.createCoordinatesSystem(
                     castToType(dataSet.getMinX(), xMin), castToType(dataSet.getMinX(), xMax),
                     castToType(dataSet.getMinY(), yMin), castToType(dataSet.getMaxY(), yMax),
                     castToType(dataSet.getMinZ(), zMin), castToType(dataSet.getMaxZ(), zMax),
                     castToType(dataSet.getMinW(), wMin), castToType(dataSet.getMaxW(), wMax), dataSet));
         }
         dataViewer.getCoordinatesSystem().setShowGrid(true);
     }
 
     /**
      * Update the view (reread the source bands and display the new data).
      */
     public void updateView()
     {
         if (dataViewer.getDataSet() != null) {
             if (!(viewerPane.getComponent(0) instanceof DataViewer)) {
                 viewerPane.removeAll();
                 viewerPane.add((JComponent) dataViewer); // dataViewer is surely a JComponent (see createDataViewer() )
             }
             dataViewer.update();
         }
     }
 
     /**
      * Creates the data viewer instance to be used within this class.
      * 
      * @return The data viewer.
      */
     private final DataViewer createDataViewer()
     {
         DataViewer viewer = createDataViewerImpl();
         if (!(viewer instanceof JComponent))
             throw new IllegalStateException(getClass()
                     + ": The viewer returned by createDataViewerImpl() must extend JComponent.");
         return viewer;
     }
 
     /**
      * Create an instance of the data viewer to be used within this class.
      * <p>
      * The returned object must extend {@link JComponent}!
      * 
      * @return The viewer instance.
      */
     protected DataViewer createDataViewerImpl()
     {
         return new JOGLDataViewer();
     }
 
     /**
      * The action for selecting the source bands.
      * 
      * @author Martin Pecka
      */
     private class SelectBandsAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -1042044060810157729L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             final Product[] prods = VisatApp.getApp().getProductManager().getProducts();
             final ProductNodeList<Product> products = new ProductNodeList<Product>();
             for (Product prod : prods) {
                 products.add(prod);
             }
 
             BandSelectionDialog bandSelectionDialog = new BandSelectionDialog(VisatApp.getApp(), VisatApp.getApp()
                     .getSelectedProduct(), products, involvedBands, maskExpression, "");
             bandSelectionDialog.show();
 
            maxPoints = bandSelectionDialog.getMaxPoints();

             List<Band> bands = bandSelectionDialog.getBands();
             if (bands != null && bands.size() > 0) {
                 if (bands.size() == 3) {
                     setBands(bands.get(0), bands.get(1), bands.get(2));
                 } else if (bands.size() == 4) {
                     setBands(bands.get(0), bands.get(1), bands.get(2), bands.get(3));
                 }
                 maskExpression = bandSelectionDialog.getMaskExpression();
                 createCoordinatesSystemForCurrentDataSet(bandSelectionDialog.getxMin(), bandSelectionDialog.getxMax(),
                         bandSelectionDialog.getyMin(), bandSelectionDialog.getyMax(), bandSelectionDialog.getzMin(),
                         bandSelectionDialog.getzMax(), bandSelectionDialog.getwMin(), bandSelectionDialog.getwMax());
                 updateView();
             }
         }
     }
 
     /**
      * The action that updates the view.
      * 
      * @author Martin Pecka
      */
     private class UpdateViewAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = 6339280477468610781L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             updateView();
         }
 
     }
 }
