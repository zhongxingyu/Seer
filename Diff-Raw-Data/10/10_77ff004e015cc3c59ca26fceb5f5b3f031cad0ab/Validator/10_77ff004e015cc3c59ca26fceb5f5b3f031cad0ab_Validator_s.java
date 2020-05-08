 /******************************************************************************* 
 * Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Holger Staudacher - initial API and implementation
 *******************************************************************************/ 
 package org.eclipse.libra.warproducts.core.validation;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.libra.warproducts.core.*;
 import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
 import org.eclipse.pde.internal.core.product.ProductPlugin;
 
 public class Validator {
 
   public static final String SERVLET_BRIDGE_ID 
     = "org.eclipse.equinox.servletbridge"; //$NON-NLS-1$
 
   public static final String[] BANNED_BUNDLES = new String[] { 
     "javax.servlet", //$NON-NLS-1$
     "org.eclipse.update.configurator",  //$NON-NLS-1$
     "org.eclipse.equinox.http.jetty",  //$NON-NLS-1$
     "org.mortbay.jetty.server",  //$NON-NLS-1$
     "org.mortbay.jetty.util"  //$NON-NLS-1$
   };
 
   public static final String[] REQUIRED_BUNDLES = new String[] { 
    "org.eclipse.equinox.servletbridge.extensionbundle", //$NON-NLS-1$
     "org.eclipse.equinox.http.registry", //$NON-NLS-1$
     "org.eclipse.equinox.registry", //$NON-NLS-1$
     "org.eclipse.equinox.http.servlet", //$NON-NLS-1$
     "org.eclipse.equinox.http.servletbridge" //$NON-NLS-1$
   };
   
   private IWARProduct product;
   private Validation validation;
 
   public Validator( final IWARProduct product ) {
     this.product = product;
   }
 
   public Validation validate() {
     Validation result = null;
     if( product != null ) {
       validation = new Validation();
       result = validation;
       doValidation();
     }
     return result;
   }
 
   private void doValidation() {
     validateLibraries();
     validateBannedBundles();
     validateRequiredBundles();
   }
 
   private void validateLibraries( ) {
     IPath[] libraries = product.getLibraries();
     checkForServletBridge( libraries );
     checkLibrariesExist( libraries );
   }
 
   private void checkForServletBridge( final IPath[] libraries ) {
     if( libraries.length == 0 
         || !librariesContainsServletBridge( libraries ) ) 
     {
       int type = ValidationError.LIBRARY_MISSING;
       String message = Messages.validatorMissingLibrary;
       ValidationError error = new ValidationError( type, message, null );
       validation.addError( error );
     }
   }
 
   private boolean librariesContainsServletBridge( final IPath[] libraries ) {
     boolean result = false;
     for( int i = 0; i < libraries.length && !result; i++ ) {
       IPath path = libraries[ i ];
       String pathString = path.toOSString();
       if( pathString.indexOf( SERVLET_BRIDGE_ID ) != -1 ) {
         result = true;
       }
     }
     return result;
   }
   
   private void checkLibrariesExist( final IPath[] libraries ) {
     for( int i = 0; i < libraries.length; i++ ) {
       IPath path 
         = WARProductUtil.getAbsolutLibraryPath( libraries[ i ], product );
       if( path != null ) {
         String pathString = path.toOSString();
         File lib = new File( pathString );
         if( !lib.exists() ) {
           createLibraryMissingError( libraries[ i ] );
         }
       } else{
         createLibraryMissingError( libraries[ i ] );
       }
     }
   }
 
   private void createLibraryMissingError( final IPath path )
   {
     int type = ValidationError.LIBRARY_DOESNT_EXIST;
     String message 
       = Messages.validatorLibraryNotExist + path + ".";//$NON-NLS-1$
     ValidationError error = new ValidationError( type, message, path );
     validation.addError( error );
   }
   
   private void validateBannedBundles() {
     IProductPlugin[] plugins = product.getPlugins();
     for( int i = 0; i < plugins.length; i++ ) {
       IProductPlugin plugin = plugins[ i ];
       if( isBundleContained( plugin.getId(), BANNED_BUNDLES ) ) {
         int type = ValidationError.BUNDLE_BANNED;
         String message = Messages.validatorForbiddenBundle + plugin.getId();
         ValidationError error = new ValidationError( type, message, plugin );
         validation.addError( error );
       }
     }
   }
   
   private void validateRequiredBundles() {
     IProductPlugin[] bundles = product.getPlugins();
     String[] containedBundles = getBundleIdArray( bundles );
     WARProductModel fakeModel = new WARProductModel();
     for( int i = 0; i < REQUIRED_BUNDLES.length; i++ ) {
       String bundleId = REQUIRED_BUNDLES[ i ];
       if( !isBundleContained( bundleId, containedBundles ) ) {
         createMissingBundleError( fakeModel, bundleId );
       }
     }
   }
 
   private String[] getBundleIdArray( final IProductPlugin[] plugins ) {
     String[] result = new String[ plugins.length ];
     for( int i = 0; i < plugins.length; i++ ) {
       result[ i ] = plugins[ i ].getId();
     }
     return result;
   }
   
   private void createMissingBundleError( final WARProductModel fakeModel,
                                          final String bundleId )
   {
     int type = ValidationError.BUNDLE_MISSING;
     String message = Messages.validatorMissingBundle + bundleId;
     IProductPlugin missingBundle = new ProductPlugin( fakeModel );
     missingBundle.setId( bundleId );
     missingBundle.setVersion( "0.0.0" ); //$NON-NLS-1$
     ValidationError error 
       = new ValidationError( type, message, missingBundle );
     validation.addError( error );
   }
 
   private boolean isBundleContained( final String id, 
                                      final String[] bundleList ) 
   {
     boolean result = false;
     for( int i = 0; i < bundleList.length && !result; i++ ) {
       String bannedId = bundleList[ i ];
       if( bannedId.equals( id ) ) {
         result = true;
       }
     }
     return result;
   }
 }
