 /**
  * 
  */
 package org.esa.beam.dataViewer3D.beamIntegration;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractButton;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.html.HTMLDocument;
 
 import org.esa.beam.dataViewer3D.beamIntegration.BandSelectionDialog.UniversalListener.IgnoredEvent;
 import org.esa.beam.framework.barithm.PossiblyInvalidExpression;
 import org.esa.beam.framework.barithm.TypeUtils;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.framework.datamodel.ProductNodeList;
 import org.esa.beam.framework.datamodel.VirtualBand;
 import org.esa.beam.framework.dataop.barithm.BandArithmetic;
 import org.esa.beam.framework.ui.GridBagUtils;
 import org.esa.beam.framework.ui.ModalDialog;
 import org.esa.beam.framework.ui.product.ProductExpressionPane;
 import org.esa.beam.util.Debug;
 import org.esa.beam.util.Guardian;
 import org.esa.beam.util.PropertyMap;
 import org.esa.beam.visat.VisatApp;
 
 import com.bc.ceres.binding.ConversionException;
 import com.bc.ceres.binding.Converter;
 import com.bc.ceres.binding.PropertyContainer;
 import com.bc.ceres.binding.PropertyDescriptor;
 import com.bc.ceres.binding.PropertySet;
 import com.bc.ceres.binding.ValueRange;
 import com.bc.ceres.swing.binding.BindingContext;
 import com.bc.ceres.swing.binding.PropertyEditor;
 import com.bc.ceres.swing.binding.PropertyEditorRegistry;
 import com.bc.ceres.swing.binding.internal.CheckBoxEditor;
 import com.bc.ceres.swing.binding.internal.NumericEditor;
 import com.bc.ceres.swing.binding.internal.TextFieldEditor;
 import com.bc.jexp.Namespace;
 import com.bc.jexp.ParseException;
 import com.jidesoft.tooltip.BalloonTip;
 import com.jidesoft.tooltip.shapes.RoundedRectangularBalloonShape;
 
 /**
  * The dialog for setting up the 3D data viewer's data sources.
  * <p>
  * The dialog introduces a mechanism called band expression casting for its internal purposes. An expression with a cast
  * is of the form <code>(cast_to_type) expression</code> or just <code>expression</code>, where <code>expression</code>
  * is a valid band maths expression and <code>cast_to_type</code> is one of {@link ProductData}.TYPESTRING_*. An
  * expression cannot be 'down-cast' - you cannot cast it to a datatype which cannot hold all the values from the bands
  * the expression consists of.
  * 
  * @author Martin Pecka
  */
 public class BandSelectionDialog extends ModalDialog
 {
     private static final String                PROPERTY_NAME_PRODUCT         = "productName";
     private static final String                PROPERTY_NAME_X_EXPRESSION    = "xExpression";
     private static final String                PROPERTY_NAME_Y_EXPRESSION    = "yExpression";
     private static final String                PROPERTY_NAME_Z_EXPRESSION    = "zExpression";
     private static final String                PROPERTY_NAME_W_EXPRESSION    = "wExpression";
     private static final String                PROPERTY_NAME_MASK_EXPRESSION = "maskExpression";
     private static final String                PROPERTY_NAME_MAX_POINTS      = "maxPoints";
 
     private static final String                PROPERTY_NAME_X_AUTODETECT    = "xAutodetect";
     private static final String                PROPERTY_NAME_Y_AUTODETECT    = "yAutodetect";
     private static final String                PROPERTY_NAME_Z_AUTODETECT    = "zAutodetect";
     private static final String                PROPERTY_NAME_W_AUTODETECT    = "wAutodetect";
 
     private static final String                PROPERTY_NAME_X_MIN           = "xMin";
     private static final String                PROPERTY_NAME_Y_MIN           = "yMin";
     private static final String                PROPERTY_NAME_Z_MIN           = "zMin";
     private static final String                PROPERTY_NAME_W_MIN           = "wMin";
     private static final String                PROPERTY_NAME_X_MAX           = "xMax";
     private static final String                PROPERTY_NAME_Y_MAX           = "yMax";
     private static final String                PROPERTY_NAME_Z_MAX           = "zMax";
     private static final String                PROPERTY_NAME_W_MAX           = "wMax";
 
     private static final String                PROPERTY_KEY_MAX_POINTS       = "data.viewer.3D.max.points";
     private static final String                PROPERTY_KEY_X_MIN            = "data.viewer.3D.x.min";
     private static final String                PROPERTY_KEY_Y_MIN            = "data.viewer.3D.y.min";
     private static final String                PROPERTY_KEY_Z_MIN            = "data.viewer.3D.z.min";
     private static final String                PROPERTY_KEY_W_MIN            = "data.viewer.3D.w.min";
     private static final String                PROPERTY_KEY_X_MAX            = "data.viewer.3D.x.max";
     private static final String                PROPERTY_KEY_Y_MAX            = "data.viewer.3D.y.max";
     private static final String                PROPERTY_KEY_Z_MAX            = "data.viewer.3D.z.max";
     private static final String                PROPERTY_KEY_W_MAX            = "data.viewer.3D.w.max";
     private static final String                PROPERTY_KEY_X_AUTODETECT     = "data.viewer.3D.x.autodetect";
     private static final String                PROPERTY_KEY_Y_AUTODETECT     = "data.viewer.3D.y.autodetect";
     private static final String                PROPERTY_KEY_Z_AUTODETECT     = "data.viewer.3D.z.autodetect";
     private static final String                PROPERTY_KEY_W_AUTODETECT     = "data.viewer.3D.w.autodetect";
 
     /** Name of the target product. */
     protected String                           productName;
     /** The application this dialog shows in. */
     protected final VisatApp                   visatApp;
     /** List of available products. */
     protected final ProductNodeList<Product>   productsList;
     /** Bindings between components and properties. */
     protected final BindingContext             bindingContext;
     /** The target product. */
     protected Product                          targetProduct;
     /** Expression defining an axis' band. */
     protected PossiblyInvalidExpression        xExpression, yExpression, zExpression, wExpression;
     /** The maximum number of displayed points. */
     protected Integer                          maxPoints;
     /** Bounds of the aces. */
     protected Double                           xMin, xMax, yMin, yMax, zMin, zMax, wMin, wMax;
     /** Flags to determine whether to autodetect axis bounds or set them manually. */
     protected boolean                          xAutodetect                   = true, yAutodetect = true,
             zAutodetect = true, wAutodetect = true;
     /** The list of bands to load initial expressions from and where the dialog result is stored. */
     protected final List<Band>                 bands;
     /** Expression for the mask. */
     protected PossiblyInvalidExpression        maskExpression;
 
     /** A warning icon. */
     private final Icon                         warningIcon                   = loadWarningIcon();
 
     /** Will become true after this dialog is shown and then confirmed and closed. */
     protected boolean                          hasFinishedNormally;
 
     /** The namespace of band arithmetic expressions. */
     protected final Namespace                  namespace;
 
     /** A mapping from property names to their corresponding keys in the application preferences map. */
     protected static final Map<String, String> nameToKeyProperties           = new HashMap<String, String>(17);
 
     static {
         nameToKeyProperties.put(PROPERTY_NAME_X_AUTODETECT, PROPERTY_KEY_X_AUTODETECT);
         nameToKeyProperties.put(PROPERTY_NAME_Y_AUTODETECT, PROPERTY_KEY_Y_AUTODETECT);
         nameToKeyProperties.put(PROPERTY_NAME_Z_AUTODETECT, PROPERTY_KEY_Z_AUTODETECT);
         nameToKeyProperties.put(PROPERTY_NAME_W_AUTODETECT, PROPERTY_KEY_W_AUTODETECT);
         nameToKeyProperties.put(PROPERTY_NAME_X_MIN, PROPERTY_KEY_X_MIN);
         nameToKeyProperties.put(PROPERTY_NAME_Y_MIN, PROPERTY_KEY_Y_MIN);
         nameToKeyProperties.put(PROPERTY_NAME_Z_MIN, PROPERTY_KEY_Z_MIN);
         nameToKeyProperties.put(PROPERTY_NAME_W_MIN, PROPERTY_KEY_W_MIN);
         nameToKeyProperties.put(PROPERTY_NAME_X_MAX, PROPERTY_KEY_X_MAX);
         nameToKeyProperties.put(PROPERTY_NAME_Y_MAX, PROPERTY_KEY_Y_MAX);
         nameToKeyProperties.put(PROPERTY_NAME_Z_MAX, PROPERTY_KEY_Z_MAX);
         nameToKeyProperties.put(PROPERTY_NAME_W_MAX, PROPERTY_KEY_W_MAX);
         nameToKeyProperties.put(PROPERTY_NAME_MAX_POINTS, PROPERTY_KEY_MAX_POINTS);
     }
 
     /**
      * Create a new setup dialog for 3D data viewer.
      * 
      * @param visatApp The application the dialog runs in.
      * @param currentProduct The product open and selected in the time this dialog was created.
      * @param productsList All products open in the time this dialog was created.
      * @param bands The list of (possibly virtual) bands to set as defaults for the aces' sources in this dialog. May be
      *            <code>null</code> or empty.
      * @param maskExpression The expression that defines the pixels the user is interested in.
      * @param helpId Id to JavaHelp.
      */
     public BandSelectionDialog(final VisatApp visatApp, Product currentProduct, ProductNodeList<Product> productsList,
             List<Band> bands, String maskExpression, String helpId)
     {
         super(visatApp.getMainFrame(), "3D Data Viewer Setup", ID_OK_CANCEL_HELP, helpId); /* I18N */
 
         Guardian.assertNotNull("currentProduct", currentProduct);
         Guardian.assertNotNull("productsList", productsList);
         Guardian.assertGreaterThan("productsList must be not empty", productsList.size(), 0);
         Guardian.assertTrue("bands have incorrect size",
                 bands == null || bands.isEmpty() || bands.size() == 3 || bands.size() == 4);
 
         hasFinishedNormally = false;
 
         this.visatApp = visatApp;
         this.targetProduct = currentProduct;
         this.productsList = productsList;
 
         if (bands == null || bands.isEmpty()) {
             this.bands = new ArrayList<Band>(4);
         } else {
             this.bands = new ArrayList<Band>(bands);
         }
 
         final Product[] products = getCompatibleProducts();
         final int defaultIndex = Arrays.asList(products).indexOf(targetProduct);
         namespace = BandArithmetic.createDefaultNamespace(products, defaultIndex == -1 ? 0 : defaultIndex);
 
         xExpression = new PossiblyInvalidExpression(namespace);
         yExpression = new PossiblyInvalidExpression(namespace);
         zExpression = new PossiblyInvalidExpression(namespace);
         wExpression = new PossiblyInvalidExpression(namespace);
         this.maskExpression = new PossiblyInvalidExpression(maskExpression != null ? maskExpression : "", namespace);
 
         this.bindingContext = createBindingContext();
 
         makeUI();
     }
 
     /**
      * Return the bands to be used as sources for the aces.
      * 
      * @return The bands to be used as sources for the aces.
      * 
      * @throws IllegalStateException When called between {@link #show()} and the confirmation of the dialog.
      */
     public List<Band> getBands()
     {
         if (!hasFinishedNormally)
             throw new IllegalStateException(getClass()
                     + ": Cannot read the dialog's result until it is completed and hidden.");
         return bands;
     }
 
     /**
      * Return the expression used as a mask for the pixels the user is interested in.
      * 
      * @return The expression used as a mask for the pixels the user is interested in.
      * 
      * @throws IllegalStateException When called between {@link #show()} and the confirmation of the dialog.
      */
     public String getMaskExpression()
     {
         if (!hasFinishedNormally)
             throw new IllegalStateException(getClass()
                     + ": Cannot read the dialog's result until it is completed and hidden.");
         return maskExpression.getExpression();
     }
 
     /**
      * Return the maximum number of points to display. <code>null</code> if the user doesn't want to set a limit.
      * 
      * @return The maximum number of points to display. <code>null</code> if the user doesn't want to set a limit.
      * 
      * @throws IllegalStateException When called between {@link #show()} and the confirmation of the dialog.
      */
     public Integer getMaxPoints()
     {
         if (!hasFinishedNormally)
             throw new IllegalStateException(getClass()
                     + ": Cannot read the dialog's result until it is completed and hidden.");
         return maxPoints;
     }
 
     /**
      * @return The xMin.
      */
     public Double getxMin()
     {
         return xAutodetect ? null : xMin;
     }
 
     /**
      * @return The xMax.
      */
     public Double getxMax()
     {
         return xAutodetect ? null : xMax;
     }
 
     /**
      * @return The yMin.
      */
     public Double getyMin()
     {
         return yAutodetect ? null : yMin;
     }
 
     /**
      * @return The yMax.
      */
     public Double getyMax()
     {
         return yAutodetect ? null : yMax;
     }
 
     /**
      * @return The zMin.
      */
     public Double getzMin()
     {
         return zAutodetect ? null : zMin;
     }
 
     /**
      * @return The zMax.
      */
     public Double getzMax()
     {
         return zAutodetect ? null : zMax;
     }
 
     /**
      * @return The wMin.
      */
     public Double getwMin()
     {
         return wAutodetect ? null : wMin;
     }
 
     /**
      * @return The wMax.
      */
     public Double getwMax()
     {
         return wAutodetect ? null : wMax;
     }
 
     @Override
     public int show()
     {
         if (hasFinishedNormally)
             throw new IllegalStateException("Cannot reuse " + getClass());
 
         hasFinishedNormally = false;
         return super.show();
     }
 
     @Override
     protected void onOK()
     {
         final Product[] products = getCompatibleProducts();
         final int defaultProductIndex = Arrays.asList(products).indexOf(targetProduct);
 
         String validMaskExpression;
         int width, height;
         PossiblyInvalidExpression expression;
 
         bands.clear();
 
         for (String propertyName : new String[] { PROPERTY_NAME_X_EXPRESSION, PROPERTY_NAME_Y_EXPRESSION,
                 PROPERTY_NAME_Z_EXPRESSION, PROPERTY_NAME_W_EXPRESSION }) {
             expression = (PossiblyInvalidExpression) bindingContext.getBinding(propertyName).getPropertyValue();
 
             // w axis may be undefined
             if (expression.isEmpty())
                 continue;
 
             try {
                 validMaskExpression = BandArithmetic.getValidMaskExpression(expression.getExpression(), products,
                         defaultProductIndex, null);
             } catch (ParseException e) {
                 String errorMessage = "The band could not be created.\nAn parse error occurred in "
                         + bindingContext.getPropertySet().getDescriptor(propertyName).getDescription() + ":\n"
                         + e.getMessage(); /* I18N */
                 visatApp.showErrorDialog(errorMessage);
                 hide();
                 return;
             }
 
             width = targetProduct.getSceneRasterWidth();
             height = targetProduct.getSceneRasterHeight();
 
            VirtualBand band = new VirtualBand(propertyName, expression.getType(), width, height,
                     expression.getExpression());
             setBandProperties(band, validMaskExpression);
 
             bands.add(band);
         }
 
         storePreferences();
 
         hasFinishedNormally = true;
         hide();
     }
 
     /**
      * Perform all properties changes needed to be performed when a new band gets constructed.
      * 
      * @param band The newly constructed band.
      * @param validMaskExpression The mask for valid pixels.
      */
     protected void setBandProperties(VirtualBand band, String validMaskExpression)
     {
         if (!maskExpression.isEmpty())
             band.setValidPixelExpression("(" + validMaskExpression + ") & (" + maskExpression.getExpression() + ")");
         else
             band.setValidPixelExpression(validMaskExpression);
        band.setModified(true);
        targetProduct.addBand(band);
     }
 
     @Override
     protected boolean verifyUserInput()
     {
         // check band expressions
         for (String propertyName : new String[] { PROPERTY_NAME_X_EXPRESSION, PROPERTY_NAME_Y_EXPRESSION,
                 PROPERTY_NAME_Z_EXPRESSION, PROPERTY_NAME_W_EXPRESSION, PROPERTY_NAME_MASK_EXPRESSION }) {
 
             if (!verifyExpressionValue(propertyName)) {
                 showErrorDialog("Please check the band maths expression you have entered in \""
                         + bindingContext.getPropertySet().getDescriptor(propertyName).getDescription()
                         + "\".\nIt is not valid."); /* I18N */
                 // set focus to the editor with invalid data
                 bindingContext.getBinding(propertyName).getComponents()[0].requestFocusInWindow();
                 return false;
             }
 
         }
 
         // check that min <= max for axis bounds
         for (Double[] bounds : new Double[][] { new Double[] { xMin, xMax }, new Double[] { yMin, yMax },
                 new Double[] { zMin, zMax }, new Double[] { wMin, wMax } }) {
             if (bounds[0] != null && bounds[1] != null) {
                 if (bounds[0] > bounds[1]) {
                     showErrorDialog("You have set a minimum larger than the corresponding maximum."); /* I18N */
                     return false;
                 }
             }
         }
 
         return super.verifyUserInput();
     }
 
     /**
      * Verify if the expression corresponding to the given property is valid. That means the expression contains no
      * errors and isn't empty if it cannot be.
      * 
      * @param propertyName The property containing the expression (one of <code>PROPERTY_NAME_?_EXPRESSION</code>).
      * @return True if the expression passed the verification.
      */
     protected boolean verifyExpressionValue(String propertyName)
     {
         return verifyExpressionValue(propertyName, null);
     }
 
     /**
      * Verify if the expression corresponding to the given property is valid. That means the expression contains no
      * errors and isn't empty if it cannot be.
      * 
      * @param propertyName The property containing the expression (one of <code>PROPERTY_NAME_?_EXPRESSION</code>).
      * @param value The value to use instead of the property's value (useful when checking the property in advance).
      * @return True if the expression passed the verification.
      */
     protected boolean verifyExpressionValue(String propertyName, String value)
     {
         PossiblyInvalidExpression expr;
         if (value != null) {
             expr = new PossiblyInvalidExpression(value, namespace);
         } else {
             expr = (PossiblyInvalidExpression) bindingContext.getBinding(propertyName).getPropertyValue();
         }
 
         return expr.isValid()
                 && (!bindingContext.getPropertySet().getDescriptor(propertyName).isNotEmpty() || !expr.isEmpty());
     }
 
     /**
      * Store the preferences of this dialog into the application's preferences. This method doesn't handle persisting of
      * the preferences! If overriding, be sure to call the super implementation.
      */
     protected void storePreferences()
     {
         final PropertyMap preferences = visatApp.getPreferences();
 
         preferences.setPropertyInt(PROPERTY_KEY_MAX_POINTS, maxPoints == null || maxPoints == 0 ? null : maxPoints);
         preferences.setPropertyBool(PROPERTY_KEY_X_AUTODETECT, xAutodetect);
         preferences.setPropertyBool(PROPERTY_KEY_Y_AUTODETECT, yAutodetect);
         preferences.setPropertyBool(PROPERTY_KEY_Z_AUTODETECT, zAutodetect);
         preferences.setPropertyBool(PROPERTY_KEY_W_AUTODETECT, wAutodetect);
         if (xMin != null)
             preferences.setPropertyDouble(PROPERTY_KEY_X_MIN, xMin);
         if (yMin != null)
             preferences.setPropertyDouble(PROPERTY_KEY_Y_MIN, yMin);
         if (zMin != null)
             preferences.setPropertyDouble(PROPERTY_KEY_Z_MIN, zMin);
         if (wMin != null)
             preferences.setPropertyDouble(PROPERTY_KEY_W_MIN, wMin);
         if (xMax != null)
             preferences.setPropertyDouble(PROPERTY_KEY_X_MAX, xMax);
         if (yMax != null)
             preferences.setPropertyDouble(PROPERTY_KEY_Y_MAX, yMax);
         if (zMax != null)
             preferences.setPropertyDouble(PROPERTY_KEY_Z_MAX, zMax);
         if (wMax != null)
             preferences.setPropertyDouble(PROPERTY_KEY_W_MAX, wMax);
     }
 
     /**
      * Create and setup the binding context which specifies the binding between UI components and properties.
      * 
      * @return The binding.
      */
     protected BindingContext createBindingContext()
     {
         final PropertyContainer container = PropertyContainer.createObjectBacked(this);
         final BindingContext context = new BindingContext(container);
 
         container.addPropertyChangeListener(PROPERTY_NAME_PRODUCT, new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 targetProduct = productsList.getByDisplayName(productName);
             }
         });
 
         final PropertyMap preferences = visatApp.getPreferences();
 
         PropertyDescriptor descriptor;
 
         /* EXPRESSION FIELDS */
 
         // X axis
         setupExpressionAndBoundsBindings(context, PROPERTY_NAME_X_EXPRESSION, PROPERTY_NAME_X_AUTODETECT,
                 PROPERTY_NAME_X_MIN, PROPERTY_NAME_X_MAX, true, "Expression for x axis' source band",
                 "Autodetect x axis bounds"); /* I18N */
         if (!bands.isEmpty())
             setDefaultValueFromBand(context.getPropertySet().getDescriptor(PROPERTY_NAME_X_EXPRESSION), bands.get(0));
 
         // Y axis
         setupExpressionAndBoundsBindings(context, PROPERTY_NAME_Y_EXPRESSION, PROPERTY_NAME_Y_AUTODETECT,
                 PROPERTY_NAME_Y_MIN, PROPERTY_NAME_Y_MAX, true, "Expression for y axis' source band",
                 "Autodetect y axis bounds");/* I18N */
         if (!bands.isEmpty())
             setDefaultValueFromBand(context.getPropertySet().getDescriptor(PROPERTY_NAME_Y_EXPRESSION), bands.get(1));
 
         // Z axis
         setupExpressionAndBoundsBindings(context, PROPERTY_NAME_Z_EXPRESSION, PROPERTY_NAME_Z_AUTODETECT,
                 PROPERTY_NAME_Z_MIN, PROPERTY_NAME_Z_MAX, true, "Expression for z axis' source band",
                 "Autodetect z axis bounds");/* I18N */
         if (!bands.isEmpty())
             setDefaultValueFromBand(context.getPropertySet().getDescriptor(PROPERTY_NAME_Z_EXPRESSION), bands.get(2));
 
         // W axis
         setupExpressionAndBoundsBindings(context, PROPERTY_NAME_W_EXPRESSION, PROPERTY_NAME_W_AUTODETECT,
                 PROPERTY_NAME_W_MIN, PROPERTY_NAME_W_MAX, false, "Expression for w axis' source band",
                 "Autodetect w axis bounds");/* I18N */
         if (bands.size() == 4)
             setDefaultValueFromBand(context.getPropertySet().getDescriptor(PROPERTY_NAME_W_EXPRESSION), bands.get(3));
 
         // Mask
         setupExpressionBindings(context, PROPERTY_NAME_MASK_EXPRESSION, false,
                 "Expression for the valid pixels mask (optional)");/* I18N */
         if (!maskExpression.getExpressionWithCast().trim().isEmpty())
             context.getPropertySet().getDescriptor(PROPERTY_NAME_MASK_EXPRESSION).setDefaultValue(maskExpression);
 
         /* OTHER FIELDS */
 
         descriptor = container.getDescriptor(PROPERTY_NAME_MAX_POINTS);
         descriptor.setDisplayName("Maximum number of displayed points");/* I18N */
         descriptor.setDescription(descriptor.getDisplayName() + " (leave empty to display just all points)");/* I18N */
         descriptor.setNotEmpty(false);
         descriptor.setDefaultValue(preferences.getPropertyInt(PROPERTY_KEY_MAX_POINTS, null));
         descriptor.setValueRange(ValueRange.parseValueRange("(0,*)"));
 
         // use the defaults as current values
         container.setDefaultValues();
 
         return context;
     }
 
     /**
      * Add bindings for an expression property and its corresponding autodetect fields.
      * 
      * @param context The context to add the binding to.
      * @param propertyExpression Name of the property containing the expression.
      * @param propertyAutodetect Name of the property defining if bounds autodetection should be used.
      * @param propertyMin Name of the property containing the minimum bound.
      * @param propertyMax Name of the property containing the maximum bound.
      * @param required If the expression has to be nonempty.
      * @param expressionLabel Label for the expression field.
      * @param autodetectLabel Label for the autodetection field.
      */
     protected void setupExpressionAndBoundsBindings(final BindingContext context, final String propertyExpression,
             final String propertyAutodetect, final String propertyMin, final String propertyMax,
             final boolean required, final String expressionLabel, final String autodetectLabel)
     {
         setupExpressionBindings(context, propertyExpression, required, expressionLabel);
 
         final PropertyMap preferences = visatApp.getPreferences();
         final PropertySet container = context.getPropertySet();
 
         PropertyDescriptor descriptor = container.getDescriptor(propertyAutodetect);
         descriptor.setDisplayName(autodetectLabel);
         descriptor.setDescription(descriptor.getDisplayName());
         descriptor.setDefaultValue(preferences.getPropertyBool(nameToKeyProperties.get(propertyAutodetect), true));
         descriptor.setNotNull(true);
 
         descriptor = container.getDescriptor(propertyMin);
         descriptor.setDisplayName("Minimum");/* I18N */
         descriptor.setDescription(descriptor.getDisplayName());
         descriptor.setNotEmpty(false);
         descriptor.setDefaultValue(preferences.getPropertyDouble(nameToKeyProperties.get(propertyMin), null));
 
         descriptor = container.getDescriptor(propertyMax);
         descriptor.setDisplayName("Maximum");/* I18N */
         descriptor.setDescription(descriptor.getDisplayName());
         descriptor.setNotEmpty(false);
         descriptor.setDefaultValue(preferences.getPropertyDouble(nameToKeyProperties.get(propertyMax), null));
 
         context.bindEnabledState(propertyMin, false, propertyAutodetect, Boolean.TRUE);
         context.bindEnabledState(propertyMax, false, propertyAutodetect, Boolean.TRUE);
     }
 
     /**
      * Add binding for the expression defined by the given property.
      * 
      * @param context The context to add the binding to.
      * @param propertyExpression Name of the property containing the expression.
      * @param required If the expression has to be nonempty.
      * @param expressionLabel Label for the expression field.
      */
     protected void setupExpressionBindings(final BindingContext context, final String propertyExpression,
             final boolean required, final String expressionLabel)
     {
         final PropertyDescriptor descriptor = context.getPropertySet().getDescriptor(propertyExpression);
         descriptor.setDisplayName(expressionLabel);
         descriptor.setDescription(descriptor.getDisplayName());
         descriptor.setNotEmpty(required);
         descriptor.setConverter(new ExpressionConverter());
     }
 
     /**
      * Parse an expression field value from the given band and use it as the default value of the given property.
      * 
      * @param descriptor Descriptor of the property to be set.
      * @param band The band to read defaults from.
      */
     protected void setDefaultValueFromBand(final PropertyDescriptor descriptor, final Band band)
     {
         String text;
         if (band instanceof VirtualBand) {
             text = ((VirtualBand) band).getExpression();
         } else {
             text = band.getName();
         }
         final PossiblyInvalidExpression expression = new PossiblyInvalidExpression(text, namespace, band.getDataType());
 
         descriptor.setDefaultValue(expression);
     }
 
     /**
      * Create and setup the GUI.
      */
     protected void makeUI()
     {
         final JPanel panel = GridBagUtils.createPanel();
         final GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridy++;
 
         // x expression and axis bounds
         addExpressionField(PROPERTY_NAME_X_EXPRESSION, gbc, panel);
         addBoundsDetectionComponents(PROPERTY_NAME_X_EXPRESSION, PROPERTY_NAME_X_AUTODETECT, PROPERTY_NAME_X_MIN,
                 PROPERTY_NAME_X_MAX, gbc, panel);
 
         // y expression and axis bounds
         addExpressionField(PROPERTY_NAME_Y_EXPRESSION, gbc, panel);
         addBoundsDetectionComponents(PROPERTY_NAME_Y_EXPRESSION, PROPERTY_NAME_Y_AUTODETECT, PROPERTY_NAME_Y_MIN,
                 PROPERTY_NAME_Y_MAX, gbc, panel);
 
         // z expression and axis bounds
         addExpressionField(PROPERTY_NAME_Z_EXPRESSION, gbc, panel);
         addBoundsDetectionComponents(PROPERTY_NAME_Z_EXPRESSION, PROPERTY_NAME_Z_AUTODETECT, PROPERTY_NAME_Z_MIN,
                 PROPERTY_NAME_Z_MAX, gbc, panel);
 
         // w expression and axis bounds
         addExpressionField(PROPERTY_NAME_W_EXPRESSION, gbc, panel);
         addBoundsDetectionComponents(PROPERTY_NAME_W_EXPRESSION, PROPERTY_NAME_W_AUTODETECT, PROPERTY_NAME_W_MIN,
                 PROPERTY_NAME_W_MAX, gbc, panel);
 
         // mask expression
         addExpressionField(PROPERTY_NAME_MASK_EXPRESSION, gbc, panel);
 
         // add vertical spacing
         GridBagUtils.addToPanel(panel, new JLabel(), gbc,
                 "weightx=1, insets.bottom=4, gridwidth=3, fill=HORIZONTAL, anchor=CENTER");
         gbc.gridy++;
 
         // max points input
         JComponent[] components = createPropertyComponents(PROPERTY_NAME_MAX_POINTS, NumericEditor.class);
         GridBagUtils.addToPanel(panel, components[1], gbc,
                 "weightx=0, insets.top=3, gridwidth=1, fill=HORIZONTAL, anchor=WEST");
         GridBagUtils.addToPanel(panel, components[0], gbc,
                 "weightx=1, insets.top=3, gridwidth=2, fill=HORIZONTAL, anchor=WEST");
 
         // this panel just makes sure the dialog will be wide enough
         final JPanel minWidthPanel = new FixedSizePanel(550, 5);
         gbc.gridy++;
         GridBagUtils.addToPanel(panel, minWidthPanel, gbc, "weightx=1, gridwidth=3, fill=HORIZONTAL, anchor=WEST");
 
         // add the listener that allows submitting the dialog by Enter and cancelling by Esc
         final KeyListener dialogSubmissionListener = new DialogSubmissionKeyListener();
         for (Component component : panel.getComponents()) {
             if (component instanceof JTextComponent) {
                 component.addKeyListener(dialogSubmissionListener);
             }
         }
 
         setContent(panel);
     }
 
     /**
      * Create and return the components defined by the given property. Usually it will be an editor (at position 0) and
      * its label (at position 1). Checkboxes only have the editor, because the label is an integral part of the editor.
      * 
      * @param propertyName Name of the property to create components for.
      * @param editorClass Class of the editor to create.
      * @return The array containing at least the editor component (at index 0) and possibly more needed components.
      */
     protected JComponent[] createPropertyComponents(String propertyName, Class<? extends PropertyEditor> editorClass)
     {
         PropertyDescriptor descriptor = bindingContext.getPropertySet().getDescriptor(propertyName);
         PropertyEditor editor = PropertyEditorRegistry.getInstance().getPropertyEditor(editorClass.getName());
         return editor.createComponents(descriptor, bindingContext);
     }
 
     /**
      * Add the expression editor components to the given panel using the given grid bag constraints.
      * 
      * @param propertyName Name of the property defining the expression.
      * @param gbc The constraints to use when adding to the panel.
      * @param panel The panel to add the components to.
      */
     protected void addExpressionField(final String propertyName, final GridBagConstraints gbc, final JPanel panel)
     {
         final JComponent[] components = createPropertyComponents(propertyName, TextFieldEditor.class);
 
         // validity checking listeners
         Guardian.assertTrue("Expression editor must be a JTextComponent", components[0] instanceof JTextComponent); /* I18N */
         final JTextComponent component = (JTextComponent) components[0];
         final UniversalListener listener = new UniversalListener(IgnoredEvent.FOCUS_GAINED) {
             @Override
             public void perform()
             {
                 checkAndShowErrorsInExpressionEditor(propertyName, component);
             }
         };
         component.getDocument().addDocumentListener(listener);
         component.addFocusListener(listener);
 
         // edit expression buttons
         AbstractButton editExpressionButton = new JButton("Edit Expression..."); /* I18N */
         editExpressionButton.addActionListener(createEditExpressionButtonListener(propertyName));
 
         // add to layout
         GridBagUtils.addToPanel(panel, components[1], gbc,
                 "weightx=0, insets.top=3, insets.bottom=8, gridwidth=1, fill=NONE, anchor=WEST");
         GridBagUtils.addToPanel(panel, components[0], gbc,
                 "weightx=1, weighty=0, insets.top=3, insets.bottom=0, insets.left=4, insets.right=4, gridwidth=1, "
                         + "fill=HORIZONTAL, anchor=WEST");
         GridBagUtils.addToPanel(panel, editExpressionButton, gbc,
                 "weightx=0, insets.top=3, gridwidth=1, fill=NONE, anchor=EAST");
 
         gbc.gridy++;
     }
 
     /**
      * Check the expression given by the property for errors, and adjust the editor's appearance according to the
      * validity of the expression.
      * 
      * @param propertyName Property for the expression.
      * @param component The editor component.
      */
     protected void checkAndShowErrorsInExpressionEditor(final String propertyName, final JTextComponent component)
     {
         if (!verifyExpressionValue(propertyName, component.getText())) {
             component.setBackground(Color.pink);
         } else {
             component.setBackground(Color.white);
         }
     }
 
     /**
      * Create an {@link ActionListener} for the edit expression button, which shows up a band maths arithmetic dialog
      * for the expression.
      * 
      * @param propertyName The property defining the expression.
      * @return The listener.
      */
     protected ActionListener createEditExpressionButtonListener(final String propertyName)
     {
         return new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 final Product[] compatibleProducts = getCompatibleProducts();
                 final ProductExpressionPane pep = ProductExpressionPane.createGeneralExpressionPane(compatibleProducts,
                         targetProduct, visatApp.getPreferences());
                 final PossiblyInvalidExpression expression = (PossiblyInvalidExpression) bindingContext.getBinding(
                         propertyName).getPropertyValue();
                 pep.setCode(expression.getExpression());
                 int status = pep.showModalDialog(getJDialog(), "Band Maths Expression Editor"); /* I18N */
                 if (status == ModalDialog.ID_OK) {
                     String newExpr = pep.getCode();
                     expression.setExpression(newExpr);
                     // workaround to call property change listeners
                     bindingContext.getBinding(propertyName).setPropertyValue(new PossiblyInvalidExpression(expression));
                 }
                 pep.dispose();
             }
         };
     }
 
     /**
      * Add the components for defining axis' bounds autodetection or manual setting of the bounds.
      * 
      * @param expressionPropertyName Property for the expression.
      * @param autodetectPropertyName Property for bounds autodetection.
      * @param minPropertyName Property for the lower bound.
      * @param maxPropertyName Property for the upper bound.
      * @param gbc The constraints to use when adding components to the layout.
      * @param panel The panel to add the components to.
      */
     protected void addBoundsDetectionComponents(final String expressionPropertyName,
             final String autodetectPropertyName, final String minPropertyName, final String maxPropertyName,
             final GridBagConstraints gbc, final JPanel panel)
     {
         // the panel for bounds controls
         final JPanel boundsPanel = new JPanel(new GridBagLayout());
 
         // add autodetection enabling checkbox
         JComponent[] components = createPropertyComponents(autodetectPropertyName, CheckBoxEditor.class);
         GridBagUtils
                 .addToPanel(panel, components[0], gbc,
                         "weighty=0, insets.top=0, insets.bottom=0, insets.left=4, insets.right=4, gridwidth=3, fill=HORIZONTAL, anchor=WEST");
         // hide bounds control when autodetection is selected
         bindingContext.addPropertyChangeListener(autodetectPropertyName, new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 boundsPanel.setVisible(!(Boolean) evt.getNewValue());
                 getJDialog().pack();
             }
         });
         // update the bounds controls visible state
         if ((Boolean) bindingContext.getPropertySet().getValue(autodetectPropertyName))
             boundsPanel.setVisible(false);
         gbc.gridy++;
 
         // add boundsPanel to the main panel
         GridBagUtils
                 .addToPanel(panel, boundsPanel, gbc,
                         "weighty=1, insets.top=0, insets.bottom=8, insets.left=4, insets.right=4, gridwidth=3, fill=HORIZONTAL, anchor=WEST");
 
         gbc.gridy++;
 
         final WarningBalloonTip balloonTip = new WarningBalloonTip(expressionPropertyName);
 
         final JComponent[] minComponents = createPropertyComponents(minPropertyName, NumericEditor.class);
         final JComponent[] maxComponents = createPropertyComponents(maxPropertyName, NumericEditor.class);
 
         JPanel labelPanel = new JPanel(new GridLayout());
 
         // maxComponent[1] is right here - this specifies the tip position of the balloon's shape
         final JComponent minWarning = createWarningButton(maxComponents[1], balloonTip);
         labelPanel.add(minWarning);
         labelPanel.add(minComponents[1]);
 
         GridBagConstraints boundsGbc = new GridBagConstraints();
         GridBagUtils.addToPanel(boundsPanel, labelPanel, boundsGbc,
                 "weighty=0, weightx=1, fill=HORIZONTAL, anchor=WEST");
         GridBagUtils.addToPanel(boundsPanel, minComponents[0], boundsGbc,
                 "weighty=0, weightx=1, fill=HORIZONTAL, anchor=WEST, insets.right=8");
 
         labelPanel = new JPanel(new GridLayout());
         final JComponent maxWarning = createWarningButton(maxComponents[1], balloonTip);
         labelPanel.add(maxWarning);
         labelPanel.add(maxComponents[1]);
 
         GridBagUtils.addToPanel(boundsPanel, labelPanel, boundsGbc,
                 "weighty=0, weightx=1, fill=HORIZONTAL, anchor=EAST");
         GridBagUtils.addToPanel(boundsPanel, maxComponents[0], boundsGbc,
                 "weighty=0, weightx=1, fill=HORIZONTAL, anchor=EAST");
 
         // this listener shows/hides the warning buttons when bounds overflow
         final UniversalListener listener = new UniversalListener(IgnoredEvent.FOCUS_GAINED) {
             @Override
             public void perform()
             {
                 if (!bindingContext.getBinding(minPropertyName).getComponents()[0].isEnabled())
                     return;
 
                 final PossiblyInvalidExpression expr = (PossiblyInvalidExpression) bindingContext.getBinding(
                         expressionPropertyName).getPropertyValue();
                 final String expression = ((JTextComponent) bindingContext.getBinding(expressionPropertyName)
                         .getComponents()[0]).getText();
                 expr.parseExpression(expression);
 
                 // hide the warning for invalid expressions, we don't know much about them
                 if (expr.isEmpty() || !expr.isValid()) {
                     minWarning.setVisible(false);
                     maxWarning.setVisible(false);
                     return;
                 }
 
                 final String dataType = expr.getTypeString();
 
                 Double min;
                 try {
                     min = Double.parseDouble(((JTextComponent) bindingContext.getBinding(minPropertyName)
                             .getComponents()[0]).getText());
                 } catch (NumberFormatException ex) {
                     min = null;
                 }
 
                 Double max;
                 try {
                     max = Double.parseDouble(((JTextComponent) bindingContext.getBinding(maxPropertyName)
                             .getComponents()[0]).getText());
                 } catch (NumberFormatException ex) {
                     max = null;
                 }
 
                 final int idealDataTypeCode = TypeUtils.getIdealDataTypeForBounds(expr.getType(), min, max);
                 final String idealDataType = ProductData.getTypeString(idealDataTypeCode);
                 if (idealDataTypeCode != expr.getType()) {
                     balloonTip.setWarning(dataType, idealDataType);
                     minWarning.setVisible(true);
                     maxWarning.setVisible(true);
                 } else {
                     minWarning.setVisible(false);
                     maxWarning.setVisible(false);
                 }
             }
         };
 
         // add event listeners for displaying or hiding the bounds overflow warning
         bindingContext.getBinding(expressionPropertyName).getComponents()[0].addFocusListener(listener);
         bindingContext.getBinding(minPropertyName).getComponents()[0].addFocusListener(listener);
         bindingContext.getBinding(maxPropertyName).getComponents()[0].addFocusListener(listener);
         ((JTextComponent) bindingContext.getBinding(expressionPropertyName).getComponents()[0]).getDocument()
                 .addDocumentListener(listener);
         ((JTextComponent) bindingContext.getBinding(minPropertyName).getComponents()[0]).getDocument()
                 .addDocumentListener(listener);
         ((JTextComponent) bindingContext.getBinding(maxPropertyName).getComponents()[0]).getDocument()
                 .addDocumentListener(listener);
         bindingContext.addPropertyChangeListener(autodetectPropertyName, listener);
 
         // update the state immediately
         listener.perform();
     }
 
     /**
      * Create the warning button which pops up the given balloon tip when clicked.
      * 
      * @param balloonOwner The owner component of the balloon tooltip.
      * @param balloonTip The balloon tooltip to show when clicked.
      * @return The warning button.
      */
     protected JComponent createWarningButton(final JComponent balloonOwner, final BalloonTip balloonTip)
     {
         final JButton button = new JButton(warningIcon);
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 balloonTip.show(balloonOwner, 0, 0);
             }
         });
         return button;
     }
 
     /**
      * Return the array of all open products compatible with the target product.
      * 
      * @return The array of all open products compatible with the target product.
      */
     protected Product[] getCompatibleProducts()
     {
         List<Product> compatibleProducts = new ArrayList<Product>(productsList.size());
         compatibleProducts.add(targetProduct);
         final float geolocationEps = getGeolocationEps();
         Debug.trace("BandMathsDialog.geolocationEps = " + geolocationEps);
         Debug.trace("BandMathsDialog.getCompatibleProducts:");
         Debug.trace("  comparing: " + targetProduct.getName());
         for (int i = 0; i < productsList.size(); i++) {
             final Product product = productsList.getAt(i);
             if (targetProduct != product) {
                 Debug.trace("  with:      " + product.getDisplayName());
                 final boolean isCompatibleProduct = targetProduct.isCompatibleProduct(product, geolocationEps);
                 Debug.trace("  result:    " + isCompatibleProduct);
                 if (isCompatibleProduct) {
                     compatibleProducts.add(product);
                 }
             }
         }
         return compatibleProducts.toArray(new Product[compatibleProducts.size()]);
     }
 
     /**
      * The value of preference VisatApp.PROPERTY_KEY_GEOLOCATION_EPS.
      * 
      * @return The value of preference VisatApp.PROPERTY_KEY_GEOLOCATION_EPS.
      */
     protected float getGeolocationEps()
     {
         return (float) visatApp.getPreferences().getPropertyDouble(VisatApp.PROPERTY_KEY_GEOLOCATION_EPS,
                 VisatApp.PROPERTY_DEFAULT_GEOLOCATION_EPS);
     }
 
     /**
      * Return the icon describing a warning label.
      * 
      * @return The icon describing a warning label.
      */
     protected Icon loadWarningIcon()
     {
         return new ImageIcon(getClass().getResource("/images/icon_warning.gif"));
     }
 
     /**
      * Converter for {@link PossiblyInvalidExpression} used by the property descriptors.
      * 
      * @author Martin Pecka
      */
     protected class ExpressionConverter implements Converter<PossiblyInvalidExpression>
     {
 
         @Override
         public Class<? extends PossiblyInvalidExpression> getValueType()
         {
             return PossiblyInvalidExpression.class;
         }
 
         @Override
         public PossiblyInvalidExpression parse(String text) throws ConversionException
         {
             return new PossiblyInvalidExpression(text, namespace);
         }
 
         @Override
         public String format(PossiblyInvalidExpression value)
         {
             if (value == null)
                 return "";
             return value.toString();
         }
 
     }
 
     /**
      * A key listener that allows submitting the dialog by pressing Enter or cancelling it by pressing Esc.
      * 
      * @author Martin Pecka
      */
     protected class DialogSubmissionKeyListener extends KeyAdapter
     {
         @Override
         public void keyPressed(KeyEvent e)
         {
             if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                 getButton(ID_OK).requestFocusInWindow();
                 getButton(ID_OK).doClick();
             } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                 getButton(ID_CANCEL).requestFocusInWindow();
                 getButton(ID_CANCEL).doClick();
             }
         }
     }
 
     /**
      * A non-resizable {@link JPanel} with a fixed size given at the creation time.
      * 
      * @author Martin Pecka
      */
     protected class FixedSizePanel extends JPanel
     {
         /** */
         private static final long serialVersionUID = -5814819136198679171L;
         private final Dimension   size;
 
         public FixedSizePanel(int width, int height)
         {
             this.size = new Dimension(width, height);
         }
 
         @Override
         public Dimension getSize(Dimension rv)
         {
             rv.setSize(size);
             return rv;
         }
 
         @Override
         public Dimension getSize()
         {
             return size;
         }
 
         @Override
         public Dimension getPreferredSize()
         {
             return size;
         }
     };
 
     /**
      * A listener of different types allowing to perform a single action on multiple listener events.
      * 
      * @author Martin Pecka
      */
     protected abstract static class UniversalListener implements DocumentListener, FocusListener,
             PropertyChangeListener
     {
         public static enum IgnoredEvent
         {
             FOCUS_LOST, FOCUS_GAINED;
         }
 
         private final Set<IgnoredEvent> ignoredEvents;
 
         /**
          * @param ignored List of events that should be ignored.
          */
         public UniversalListener(IgnoredEvent... ignored)
         {
             ignoredEvents = new HashSet<IgnoredEvent>(Arrays.asList(ignored));
         }
 
         /**
          * Do the work of the listener.
          */
         public abstract void perform();
 
         @Override
         public void propertyChange(PropertyChangeEvent evt)
         {
             perform();
         }
 
         @Override
         public void focusGained(FocusEvent e)
         {
             if (!ignoredEvents.contains(IgnoredEvent.FOCUS_GAINED))
                 perform();
         }
 
         @Override
         public void focusLost(FocusEvent e)
         {
             if (!ignoredEvents.contains(IgnoredEvent.FOCUS_LOST))
                 perform();
         }
 
         @Override
         public void insertUpdate(DocumentEvent e)
         {
             perform();
         }
 
         @Override
         public void removeUpdate(DocumentEvent e)
         {
             perform();
         }
 
         @Override
         public void changedUpdate(DocumentEvent e)
         {
             perform();
         }
     }
 
     /**
      * A balloon tooltip showing a warning about overflowing type bounds.
      * 
      * @author Martin Pecka
      */
     protected class WarningBalloonTip extends BalloonTip
     {
         /** */
         private static final long serialVersionUID = 1090390447609073831L;
 
         /** The property defining the expression this tooltip is bound to. */
         protected final String    expressionProperty;
 
         public WarningBalloonTip(final String expressionProperty)
         {
             super();
 
             this.expressionProperty = expressionProperty;
 
             // the component to copy visual style from
             final JLabel styleSource = new JLabel();
 
             // set the content pane's visual appearance
             final JEditorPane pane = new JEditorPane("text/html", "");
             pane.setEditable(false);
             pane.setOpaque(false);
             pane.setBackground(new Color(0, 0, 0, 0));
             pane.setSelectionColor(new Color(0, 0, 0, 0));
             pane.setForeground(styleSource.getForeground());
             pane.setSelectedTextColor(styleSource.getForeground());
 
             final Font font = styleSource.getFont().deriveFont(8);
             final String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize()
                     + "pt; }";
             ((HTMLDocument) pane.getDocument()).getStyleSheet().addRule(bodyRule);
 
             // the balloon's shape
             final RoundedRectangularBalloonShape balloonShape = new RoundedRectangularBalloonShape();
             balloonShape.setPosition(SwingConstants.BOTTOM);
             balloonShape.setVertexPosition(0.5);
 
             setContent(pane);
             setBalloonShape(balloonShape);
 
             // add the hyperlink listener handling the link to cast the expression to a sugested type
             pane.addHyperlinkListener(new HyperlinkListener() {
                 @Override
                 public void hyperlinkUpdate(HyperlinkEvent e)
                 {
                     if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                         if (e.getURL().toString().startsWith("http://castTo?type=")) {
                             final String type = e.getURL().toString().replace("http://castTo?type=", "");
                             final PossiblyInvalidExpression expression = (PossiblyInvalidExpression) bindingContext
                                     .getBinding(expressionProperty).getPropertyValue();
                             expression.setTypeString(type);
                             // workaround for firing property change listeners
                             bindingContext.getBinding(expressionProperty).setPropertyValue(
                                     new PossiblyInvalidExpression(expression));
                             hide();
                         }
                     }
                 }
             });
         }
 
         /**
          * Set the new warning with the given types used.
          * 
          * @param dataType The data type of the current expression.
          * @param idealDataType The data type to cast the expression to.
          */
         public void setWarning(String dataType, String idealDataType)
         {
             final String content = "<html><body><p>The bounds you've entered don't fit into the data type of the band "
                     + "(which is " + dataType + ").<br/>Either <a href=\"http://castTo?type=" + idealDataType
                     + "\">cast the band to the " + idealDataType
                     + " data type</a> (which is sufficient) or adjust the bounds.<br/>Leaving "
                     + "the bounds in this state will result in cropping them to the bounds of the band's "
                     + "data type.</p></body></html>"; /* I18N */
             ((JEditorPane) getContent()).setText(content);
         }
     }
 }
