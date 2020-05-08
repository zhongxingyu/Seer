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
 
 import org.esa.beam.chris.operators.ComputeSurfaceReflectancesOp;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.ui.DefaultSingleTargetProductDialog;
 import org.esa.beam.framework.gpf.ui.SingleTargetProductDialog;
 import org.esa.beam.framework.ui.command.CommandEvent;
 import org.esa.beam.visat.VisatApp;
 import org.esa.beam.visat.actions.AbstractVisatAction;
 
 /**
  * todo - API doc
  *
  * @author Ralf Quast
  * @version $Revision$ $Date$
  */
 public class PerformAtmosphericCorrectionAction extends AbstractVisatAction {
     @Override
     public void actionPerformed(CommandEvent commandEvent) {
        final String sourceProductName = VisatApp.getApp().getSelectedProduct().getName();
         final SingleTargetProductDialog dialog =
                 new DefaultSingleTargetProductDialog(
                         OperatorSpi.getOperatorAlias(ComputeSurfaceReflectancesOp.class),
                         getAppContext(),
                         "CHRIS/PROBA Atmospheric Correction",
                         "chrisAtmosphericCorrectionTool");
//        dialog.getTargetProductSelector().getModel().setProductName(sourceProductName.replace("_REFL", "_ATM"));
         dialog.show();
     }
 
     @Override
     public void updateState() {
         final Product selectedProduct = VisatApp.getApp().getSelectedProduct();
         final boolean enabled = selectedProduct == null ||
                selectedProduct.getProductType().startsWith("CHRIS_M") &&
                selectedProduct.containsBand("cloud_product");
 
         setEnabled(enabled);
     }
 }
