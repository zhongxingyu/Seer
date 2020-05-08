 /******************************************************************************* 
 * Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Holger Staudacher - initial API and implementation
 *******************************************************************************/
 package org.eclipse.libra.warproducts.ui.editor;
 
 import java.io.File;
 import java.util.Map;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.libra.warproducts.core.IWARProduct;
 import org.eclipse.libra.warproducts.ui.Messages;
 import org.eclipse.libra.warproducts.ui.WARProductConstants;
 import org.eclipse.libra.warproducts.ui.validation.*;
 import org.eclipse.pde.internal.core.iproduct.IProduct;
 import org.eclipse.pde.internal.core.iproduct.IProductModel;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.editor.*;
 import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
 import org.eclipse.pde.internal.ui.editor.product.ProductEditor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.*;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class WARProductEditor extends ProductEditor 
   implements IValidationListener 
 {
 
  private static final String PRODUCT_EDITOR_IMAGE = "icons/exp_product.gif"; //$NON-NLS-1$
   private static final String WARPRODUCT_FILE_EXTENSION = ".warproduct"; //$NON-NLS-1$
   private WARProductExportAction exportAction;
 
   protected void addEditorPages() {
     try {
       addPage( new OverviewPage( this ) );
       addPage( new ConfigurationPage( this, useFeatures() ) );
     } catch( final PartInitException e ) {
       PDEPlugin.logException( e );
     }    
     addSourcePage( WebXMLInputContext.CONTEXT_ID );
   }
   
   protected PDESourcePage createSourcePage( final PDEFormEditor editor,
                                             final String title,
                                             final String name,
                                             final String contextId )
   {
     PDESourcePage result = null;
     if( contextId.equals( WebXMLInputContext.CONTEXT_ID ) ) {
       result = new WebXMLSourcePage( editor, title, name );
     } else {
       result = super.createSourcePage( editor, title, name, contextId );
     }
     return result;
   }
   
   public String getContextIDForSaveAs() {
     return WARProductInputContext.CONTEXT_ID;
   }
   
   protected String getEditorID() {
     return WARProductConstants.EDITOR_ID;
   }
 
   public void contributeToToolbar( final IToolBarManager manager ) {
     IProductModel model = ( IProductModel )getAggregateModel();
     IProduct product = model.getProduct();
     WARProductValidateAction validationAction 
       = new WARProductValidateAction( ( IWARProduct )product );
     validationAction.addValidationListener( this );
     manager.add( validationAction );
     manager.add( getExportAction() );
   }
 
   private WARProductExportAction getExportAction() {
     if( exportAction == null ) {
       exportAction = new WARProductExportAction( this );
       exportAction.setToolTipText( Messages.editorExport );
       String pluginId = WARProductConstants.PLUGIN_ID;
       String imagePath = PRODUCT_EDITOR_IMAGE; //$NON-NLS-1$
       ImageDescriptor descExportProductTool 
         = AbstractUIPlugin.imageDescriptorFromPlugin( pluginId, imagePath );
       exportAction.setImageDescriptor( descExportProductTool );
     }
     return exportAction;
   }
 
   protected InputContextManager createInputContextManager() {
     return new WARProductInputContextManager( this );
   }
 
   protected void createSystemFileContexts( final InputContextManager manager,
                                            final FileStoreEditorInput input )
   {
     File file = new File( input.getURI() );
     String name = file.getName();
     if( name.endsWith( WARPRODUCT_FILE_EXTENSION ) ) { //$NON-NLS-1$
       IFileStore store;
       try {
         store = EFS.getStore( file.toURI() );
         IEditorInput in = new FileStoreEditorInput( store );
         manager.putContext( in, 
                             new WARProductInputContext( this, in, true ) );
       } catch( final CoreException e ) {
         PDEPlugin.logException( e );
       }
     }
   }
 
   protected void createStorageContexts( final InputContextManager manager,
                                         final IStorageEditorInput input )
   {
     if( input.getName().endsWith( WARPRODUCT_FILE_EXTENSION ) ) { //$NON-NLS-1$
       WARProductInputContext context 
         = new WARProductInputContext( this, input, true );
       manager.putContext( input, context );
     }
   }
 
   protected void createResourceContexts( final InputContextManager manager,
                                          final IFileEditorInput input )
   {
     WARProductInputContext context 
       = new WARProductInputContext( this, input, true );
     manager.putContext( input, context );
     manager.monitorFile( input.getFile() );
     IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
     IProductModel model = ( IProductModel )getAggregateModel();
     IWARProduct product = ( IWARProduct )model.getProduct();
     IFile webXml = root.getFile( product.getWebXml() );
     IEditorInput webXmlInput = new FileEditorInput( webXml );
     WebXMLInputContext webXmlContext 
       = new WebXMLInputContext( this, webXmlInput, false );
     manager.putContext( webXmlInput, webXmlContext );
     manager.monitorFile( webXml );
   }
 
   public boolean useFeatures() {
     return false;
   }
   
   protected ISortableContentOutlinePage createContentOutline() {
     return new WARProductOutlinePage( this );
   }
 
   public void validationFinished( final Map errors ) {
     Shell shell = PDEPlugin.getActiveWorkbenchShell();
     if( !errors.isEmpty() ) {
       PluginStatusDialog dialog = new PluginStatusDialog( shell );
       dialog.setInput( errors );
       dialog.open();
     } else {
       String pluginValidationMessage 
         = Messages.Validation;
       String noProblemsMessage 
         = Messages.noProblems;
       MessageDialog.openInformation( shell,
                                      pluginValidationMessage,
                                      noProblemsMessage );
     }
   }
 
   protected String computeInitialPageId() {
     return OverviewPage.PAGE_ID;
   }
   
 }
