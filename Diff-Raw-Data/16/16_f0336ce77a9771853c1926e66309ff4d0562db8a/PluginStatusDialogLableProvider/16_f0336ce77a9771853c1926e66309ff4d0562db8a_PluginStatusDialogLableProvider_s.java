 /******************************************************************************* 
 * Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Holger Staudacher - initial API and implementation
 *******************************************************************************/ 
 package org.eclipse.libra.warproducts.ui.validation;
 
 import org.eclipse.debug.internal.ui.DefaultLabelProvider;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.libra.warproducts.core.validation.ValidationError;
 import org.eclipse.libra.warproducts.ui.WARProductConstants;
 import org.eclipse.pde.internal.ui.*;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class PluginStatusDialogLableProvider extends DefaultLabelProvider {
 
   private static final String VALIDATION_ERROR_IMAGE 
    = "icons/problem_server.gif";
 
   public Image getImage( final Object element ) {
     PDELabelProvider pdeLabelProvider 
       = PDEPlugin.getDefault().getLabelProvider();
     Image result = null;
     if( element instanceof String ) {
       String pluginId = WARProductConstants.PLUGIN_ID;
       String imagePath = VALIDATION_ERROR_IMAGE; //$NON-NLS-1$
       ImageDescriptor imageDescriptor 
         = AbstractUIPlugin.imageDescriptorFromPlugin( pluginId, imagePath );
       result = pdeLabelProvider.get( imageDescriptor );
     } else if( element instanceof ValidationError ) {
       result = getValidationErrorImage( element );
     } else {
       result = pdeLabelProvider.getImage( element );
     }
     return result;
   }
 
   private Image getValidationErrorImage( final Object element ) {
     PDELabelProvider pdeLabelProvider 
       = PDEPlugin.getDefault().getLabelProvider();
     Image result;
     ValidationError error = ( ValidationError )element;
     int type = error.getType();
     if(    type == ValidationError.LIBRARY_MISSING 
         || type == ValidationError.LIBRARY_DOESNT_EXIST ) 
     {
       result = pdeLabelProvider.get( PDEPluginImages.DESC_JAR_LIB_OBJ );
     } else {
       result = pdeLabelProvider.get( PDEPluginImages.DESC_PLUGIN_OBJ );
     }
     return result;
   }
   
   public String getText( final Object element ) {
     return PDEPlugin.getDefault().getLabelProvider().getText( element );
   }
 }
