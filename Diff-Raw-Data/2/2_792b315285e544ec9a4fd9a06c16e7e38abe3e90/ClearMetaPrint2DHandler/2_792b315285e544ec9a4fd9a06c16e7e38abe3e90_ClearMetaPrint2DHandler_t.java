 /* *****************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  ******************************************************************************/
 package net.bioclipse.metaprint2d.ui.actions;
 
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
 import net.bioclipse.cdk.jchempaint.view.JChemPaintView;
 import net.bioclipse.cdk.ui.sdfeditor.editor.MoleculesEditor;
 import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
 import net.bioclipse.metaprint2d.ui.Activator;
 import net.bioclipse.metaprint2d.ui.MetaPrintGenerator;
 import net.bioclipse.metaprint2d.ui.Metaprint2DConstants;
 import net.bioclipse.metaprint2d.ui.business.IMetaPrint2DManager;
 import net.bioclipse.metaprint2d.ui.views.MetaPrint2DReportView;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.contexts.IContextService;
 
 
 public class ClearMetaPrint2DHandler extends AbstractHandler implements IHandler {
 
     Logger logger = Logger.getLogger( ClearMetaPrint2DHandler.class );
 
     public Object execute( ExecutionEvent event ) throws ExecutionException {
 
         //Make sure we are called from a supported editor
         IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
         .getActivePage().getActiveEditor();
 
         if (part instanceof net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart) {
             executeInMoleculesEditor(part);
         }
         else if ( part instanceof JChemPaintEditor ) {
             executeInJCP(part);
         }
         
         MetaPrintGenerator.setVisible(false);
 
         updateLinkedViews();
         
         return null;
     }
 
     public void executeInJCP(IEditorPart part){
         JChemPaintEditor jcpeditor = (JChemPaintEditor) part;
 
         //Turn of generators
 //        jcpeditor.getWidget().setUseExtensionGenerators( false );
 
         IMetaPrint2DManager m2d = Activator.getDefault().getMetaPrint2DManager();
         m2d.clear( jcpeditor.getCDKMolecule());
        jcpeditor.setMoleculeProperty(Metaprint2DConstants.METAPRINT_RESULT_PROPERTY, null);
         //manually update jcpeditor
         jcpeditor.update();
 
 
     }
     
     private void updateLinkedViews() {
 
         //Manually refresh m2d report view
         IViewPart reportView = PlatformUI.getWorkbench().
                                getActiveWorkbenchWindow().getActivePage().
                                findView(MetaPrint2DReportView.VIEW_ID);
         if (reportView!=null)
             ((MetaPrint2DReportView)reportView).refresh();
                 
         //Manually refresh 2D view as well
         IViewPart jcpview = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                 .getActivePage().findView( JChemPaintView.VIEW_ID );
         if (jcpview!=null)
             ((JChemPaintView)jcpview).refresh();
     }
 
     
     public void executeInMoleculesEditor(IEditorPart part) {
 
         MultiPageMoleculesEditorPart molPart=(MultiPageMoleculesEditorPart)part;
         
         IContextService contextService = (IContextService) PlatformUI.getWorkbench().
         getService(IContextService.class);
 
         for (Object cs : contextService.getActiveContextIds()){
             if (MultiPageMoleculesEditorPart.JCP_CONTEXT.equals( cs )){
                 //JCP is active
                 Object obj = molPart.getAdapter(JChemPaintEditor.class);
                 if (obj!= null){
                     JChemPaintEditor jcp=(JChemPaintEditor)obj;
                     executeInJCP( jcp );
                     return;
                 }
             }
 
         }
         
         MoleculesEditor molEditor=molPart.getMoleculesPage();
 
         molEditor.setRenderer2DConfigurator( null );
 
         molEditor.getMolTableViewer().refresh();
         logger.debug("Removed renderer2dconfigurator in MoleculesTable");
       }
 
 
 
 }
