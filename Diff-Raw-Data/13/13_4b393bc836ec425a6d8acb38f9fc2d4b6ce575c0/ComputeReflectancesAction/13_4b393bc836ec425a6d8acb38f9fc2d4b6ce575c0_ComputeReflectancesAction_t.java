 package org.esa.beam.chris.ui;
 
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.gpf.ui.DefaultSingleTargetProductDialog;
 import org.esa.beam.framework.gpf.ui.SingleTargetProductDialog;
 import org.esa.beam.framework.ui.command.CommandEvent;
 import org.esa.beam.visat.VisatApp;
 import org.esa.beam.visat.actions.AbstractVisatAction;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Action for computing reflectances.
  *
  * @author Ralf Quast
  * @version $Revision$ $Date$
  */
 public class ComputeReflectancesAction extends AbstractVisatAction {
 
     private static List<String> CHRIS_TYPES;
 
     static {
         CHRIS_TYPES = new ArrayList<String>();
         Collections.addAll(CHRIS_TYPES,
                            "CHRIS_M1_NR",
                            "CHRIS_M2_NR",
                            "CHRIS_M3_NR",
                            "CHRIS_M30_NR",
                            "CHRIS_M3A_NR",
                            "CHRIS_M4_NR",
                            "CHRIS_M5_NR");
     }
 
     @Override
     public void actionPerformed(CommandEvent commandEvent) {
         final SingleTargetProductDialog dialog =
                 new DefaultSingleTargetProductDialog("chris.ComputeReflectances",
                                                      getAppContext(),
                                                      "CHRIS/PROBA Reflectance Computation",
                                                      "chrisReflectanceComputationTool");
        final Product sourceProduct = VisatApp.getApp().getSelectedProduct();
        if (sourceProduct != null && CHRIS_TYPES.contains(sourceProduct.getProductType())) {
            final String sourceProductName = sourceProduct.getName();
            dialog.getTargetProductSelector().getModel().setProductName(sourceProductName.replace("_NR", "_REFL"));
        }
         dialog.show();
     }
 
     @Override
     public void updateState() {
         final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
         final boolean enabled = selectedProduct == null
                 || CHRIS_TYPES.contains(selectedProduct.getProductType());
 
         setEnabled(enabled);
     }
 }
