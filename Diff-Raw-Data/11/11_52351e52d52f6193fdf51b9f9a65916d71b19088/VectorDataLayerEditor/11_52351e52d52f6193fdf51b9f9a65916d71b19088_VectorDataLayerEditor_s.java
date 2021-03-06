 package org.esa.beam.visat.toolviews.layermanager.editors;
 
 import com.bc.ceres.binding.PropertyDescriptor;
 import com.bc.ceres.binding.PropertySet;
 import com.bc.ceres.glayer.Layer;
 import com.bc.ceres.swing.binding.BindingContext;
 import com.bc.ceres.swing.figure.AbstractShapeFigure;
 import com.bc.ceres.swing.figure.Figure;
 import com.bc.ceres.swing.figure.FigureCollection;
 import com.bc.ceres.swing.figure.FigureEditor;
 import com.bc.ceres.swing.figure.FigureStyle;
 import com.bc.ceres.swing.figure.support.DefaultFigureStyle;
 import com.bc.ceres.swing.selection.AbstractSelectionChangeListener;
 import com.bc.ceres.swing.selection.SelectionChangeEvent;
 import com.bc.ceres.swing.selection.SelectionChangeListener;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.ui.AppContext;
 import org.esa.beam.framework.ui.layer.AbstractLayerConfigurationEditor;
 import org.esa.beam.framework.ui.product.ProductSceneView;
 import org.esa.beam.framework.ui.product.SimpleFeatureFigure;
 import org.esa.beam.framework.ui.product.VectorDataLayer;
 import org.esa.beam.util.ObjectUtils;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.HierarchyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 
 public class VectorDataLayerEditor extends AbstractLayerConfigurationEditor {
 
     private static final String FILL_COLOR_NAME = DefaultFigureStyle.FILL_COLOR.getName();
     private static final String FILL_OPACITY_NAME = DefaultFigureStyle.FILL_OPACITY.getName();
     private static final String STROKE_COLOR_NAME = DefaultFigureStyle.STROKE_COLOR.getName();
     private static final String STROKE_WIDTH_NAME = DefaultFigureStyle.STROKE_WIDTH.getName();
     private static final String STROKE_OPACITY_NAME = DefaultFigureStyle.STROKE_OPACITY.getName();
 
     private final SelectionChangeHandler selectionChangeHandler;
 
     private AppContext appContext;
     private AtomicBoolean isAdjusting;
     private static UpdateStylePropertyChangeListener updateStyleListener;
 
     public VectorDataLayerEditor() {
         selectionChangeHandler = new SelectionChangeHandler();
         updateStyleListener = new UpdateStylePropertyChangeListener();
         isAdjusting = new AtomicBoolean(false);
     }
 
     @Override
     public JComponent createControl(final AppContext appContext, Layer layer) {
         JComponent control = super.createControl(appContext, layer);
         this.appContext = appContext;
         if (layer instanceof VectorDataLayer) {
             VectorDataLayer dataLayer = (VectorDataLayer) layer;
             if (!isGeometryType(dataLayer)) {
                 control = new JPanel();
             }
         }
         control.addHierarchyListener(new RemoveListenerListener(appContext));
         return control;
     }
 
     private boolean isGeometryType(VectorDataLayer dataLayer) {
         return Product.GEOMETRY_FEATURE_TYPE_NAME.equals(dataLayer.getVectorDataNode().getFeatureType().getTypeName());
     }
 
     private boolean containsListener(FigureEditor figureEditor, SelectionChangeListener listener) {
         final List<SelectionChangeListener> listeners = Arrays.asList(figureEditor.getSelectionChangeListeners());
         return listeners.contains(listener);
     }
 
     @Override
     protected void initializeBinding(AppContext appContext, BindingContext bindingContext) {
         final AbstractShapeFigure[] shapeFigures = getFigures();
         final PropertyDescriptor fillColor = new PropertyDescriptor(DefaultFigureStyle.FILL_COLOR);
         fillColor.setDefaultValue(getStyleProperty(FILL_COLOR_NAME, shapeFigures));
         final PropertyDescriptor fillOpacity = new PropertyDescriptor(DefaultFigureStyle.FILL_OPACITY);
         fillOpacity.setDefaultValue(getStyleProperty(FILL_OPACITY_NAME, shapeFigures));
 
         final PropertyDescriptor strokeColor = new PropertyDescriptor(DefaultFigureStyle.STROKE_COLOR);
         strokeColor.setDefaultValue(getStyleProperty(STROKE_COLOR_NAME, shapeFigures));
         final PropertyDescriptor strokeWidth = new PropertyDescriptor(DefaultFigureStyle.STROKE_WIDTH);
         strokeWidth.setDefaultValue(getStyleProperty(STROKE_WIDTH_NAME, shapeFigures));
         final PropertyDescriptor strokeOpacity = new PropertyDescriptor(DefaultFigureStyle.STROKE_OPACITY);
         strokeOpacity.setDefaultValue(getStyleProperty(STROKE_OPACITY_NAME, shapeFigures));
 
         addPropertyDescriptor(fillColor);
         addPropertyDescriptor(fillOpacity);
         addPropertyDescriptor(strokeColor);
         addPropertyDescriptor(strokeWidth);
         addPropertyDescriptor(strokeOpacity);
     }
 
     @Override
     public void updateControl() {
         final BindingContext bindingContext = getBindingContext();
 
         if (isAdjusting.compareAndSet(false, true)) {
             try {
                 final AbstractShapeFigure[] selectedFigures = getFigures();
                 updateBinding(selectedFigures, bindingContext);
             } finally {
                 isAdjusting.set(false);
             }
         }
 
         final FigureEditor figureEditor = appContext.getSelectedProductSceneView().getFigureEditor();
         if (!containsListener(figureEditor, selectionChangeHandler)) {
             figureEditor.addSelectionChangeListener(selectionChangeHandler);
             getBindingContext().addPropertyChangeListener(updateStyleListener);
         }
         super.updateControl();
     }
 
     private void updateBinding(AbstractShapeFigure[] selectedFigures, BindingContext bindingContext) {
         final PropertySet propertySet = bindingContext.getPropertySet();
         setPropertyValue(FILL_COLOR_NAME, propertySet, getStyleProperty(FILL_COLOR_NAME, selectedFigures));
         setPropertyValue(FILL_OPACITY_NAME, propertySet, getStyleProperty(FILL_OPACITY_NAME, selectedFigures));
         setPropertyValue(STROKE_COLOR_NAME, propertySet, getStyleProperty(STROKE_COLOR_NAME, selectedFigures));
         setPropertyValue(STROKE_WIDTH_NAME, propertySet, getStyleProperty(STROKE_WIDTH_NAME, selectedFigures));
         setPropertyValue(STROKE_OPACITY_NAME, propertySet, getStyleProperty(STROKE_OPACITY_NAME, selectedFigures));
     }
 
     private void setPropertyValue(String propertyName, PropertySet propertySet, Object value) {
         final Object oldValue = propertySet.getValue(propertyName);
         if (!ObjectUtils.equalObjects(oldValue, value)) {
             propertySet.setValue(propertyName, value);
         }
     }
 
     private Object getStyleProperty(String propertyName, AbstractShapeFigure[] figures) {
         Object lastProperty = null;
         for (AbstractShapeFigure figure : figures) {
             final Object currentProperty = figure.getNormalStyle().getValue(propertyName);
             if (lastProperty == null) {
                 lastProperty = currentProperty;
             } else {
                 if (!lastProperty.equals(currentProperty)) {
                     return null;
                 }
             }
         }
         return lastProperty;
     }
 
     private AbstractShapeFigure[] getFigures() {
         AbstractShapeFigure[] figures = new AbstractShapeFigure[0];
         if (appContext != null) {
             final ProductSceneView sceneView = appContext.getSelectedProductSceneView();
             SimpleFeatureFigure[] featureFigures = sceneView.getSelectedFeatureFigures();
             if (featureFigures.length == 0) {
                 featureFigures = getAllFigures((VectorDataLayer) getLayer());
             }
             List<AbstractShapeFigure> selFigureList = new ArrayList<AbstractShapeFigure>(7);
             for (SimpleFeatureFigure featureFigure : featureFigures) {
                 if (featureFigure instanceof AbstractShapeFigure) {
                     selFigureList.add((AbstractShapeFigure) featureFigure);
                 }
             }
             figures = selFigureList.toArray(new AbstractShapeFigure[selFigureList.size()]);
         }
         return figures;
     }
 
     private SimpleFeatureFigure[] getAllFigures(VectorDataLayer vectorDataLayer) {
         final FigureCollection figureCollection = vectorDataLayer.getFigureCollection();
         ArrayList<SimpleFeatureFigure> selectedFigures = new ArrayList<SimpleFeatureFigure>(
                 figureCollection.getFigureCount());
         for (Figure figure : figureCollection.getFigures()) {
             if (figure instanceof SimpleFeatureFigure) {
                 selectedFigures.add((SimpleFeatureFigure) figure);
             }
         }
         return selectedFigures.toArray(new SimpleFeatureFigure[selectedFigures.size()]);
     }
 
 
     private void transferPropertyValue(String propertyName, PropertySet propertySet, FigureStyle style) {
         final Object value = propertySet.getValue(propertyName);
         if (value != null) {
             style.setValue(propertyName, value);
         }
     }
 
     private class SelectionChangeHandler extends AbstractSelectionChangeListener {
 
         @Override
         public void selectionChanged(SelectionChangeEvent event) {
             updateControl();
         }
     }
 
     private class UpdateStylePropertyChangeListener implements PropertyChangeListener {
 
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getNewValue() == null) {
                 return;
             }
             final AbstractShapeFigure[] selectedFigures = getFigures();
             final BindingContext bindContext = getBindingContext();
             if (isAdjusting.compareAndSet(false, true)) {
                 try {
                     for (AbstractShapeFigure selectedFigure : selectedFigures) {
                         final Object oldFigureValue = selectedFigure.getNormalStyle().getValue(evt.getPropertyName());
                         final Object newValue = evt.getNewValue();
                         if (!newValue.equals(oldFigureValue)) {
                             final FigureStyle origStyle = selectedFigure.getNormalStyle();
                             final DefaultFigureStyle style = new DefaultFigureStyle();
                             style.fromCssString(origStyle.toCssString());
                             transferPropertyValue(evt.getPropertyName(), bindContext.getPropertySet(), style);
                             selectedFigure.setNormalStyle(style);
                         }
                     }
                 } finally {
                     isAdjusting.set(false);
                 }
             }
         }
     }
 
     private class RemoveListenerListener implements HierarchyListener {
 
         private final AppContext applicationContext;
 
         private RemoveListenerListener(AppContext applicationContext) {
             this.applicationContext = applicationContext;
         }
 
         @Override
         public void hierarchyChanged(HierarchyEvent e) {
             if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == HierarchyEvent.PARENT_CHANGED) {
                 if (e.getChanged().getParent() == null) {
                    final FigureEditor figureEditor = applicationContext.getSelectedProductSceneView().getFigureEditor();
                    if (!containsListener(figureEditor, selectionChangeHandler)) {
                        figureEditor.removeSelectionChangeListener(selectionChangeHandler);
                        getBindingContext().removePropertyChangeListener(updateStyleListener);
                     }
                 }
             }
         }
     }
 }
