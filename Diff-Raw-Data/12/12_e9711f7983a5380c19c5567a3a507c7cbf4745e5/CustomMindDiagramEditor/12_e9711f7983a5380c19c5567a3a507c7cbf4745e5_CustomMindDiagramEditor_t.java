 package org.ow2.mindEd.adl.editor.graphic.ui.custom.part;
 
 import java.util.List;
 
 import org.eclipse.draw2d.DelegatingLayout;
 import org.eclipse.draw2d.FreeformLayer;
 import org.eclipse.draw2d.Layer;
 import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.graph.Path;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts.CustomConnectionLayerEx;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.providers.MindCustomDocumentProvider;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.tools.MindCustomPaletteFactory;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditor;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindPaletteFactory;
 import org.ow2.mindEd.ide.core.ModelToProjectUtil;
 
 public class CustomMindDiagramEditor extends MindDiagramEditor {
 
 	@Override
 	protected PaletteRoot createPaletteRoot(PaletteRoot existingPaletteRoot) {
 		return super.createPaletteRoot(existingPaletteRoot);
 	}
 
 	@Override
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 		// Get a custom connection layer to install a custom connection router
 		setupCustomConnectionLayerEx();
 	}
 
 	/**
      * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
      */
 	protected void initializeGraphicalViewerContents() {
         super.initializeGraphicalViewerContents();
         // Now that things are initialized, we should refresh
         refreshAll(getDiagramEditPart());
     }
 	
 	/**
 	 * Refresh all edit parts
 	 * @param editPart
 	 */
 	public void refreshAll(EditPart editPart) {
 		 editPart.refresh();
 		 for (Object child : editPart.getChildren()) {
 			 if (child instanceof EditPart)
 				 refreshAll((EditPart)child);
 		 }
 	}
 	
 
 	protected IDocumentProvider provider;
 	
 	protected IDocumentProvider getDocumentProvider(IEditorInput input) {
 		if (provider == null) setDocumentProvider(input);
 		return provider;
 	}
 	
 	protected void setDocumentProvider(IEditorInput input) {
 		if (input instanceof IFileEditorInput
 				|| input instanceof URIEditorInput) {
 			provider = new MindCustomDocumentProvider();
 			setDocumentProvider(provider);
 		} else {
 			super.setDocumentProvider(input);
 		}
 	}
 	
 	@Override
 	public void setInput(IEditorInput input) {
		if (input instanceof FileEditorInput) {
			FileEditorInput fei = (FileEditorInput) input;
			if (fei.getFile().getFileExtension().equals("adl")) {
				input = new FileEditorInput(fei.getFile().getParent().getFile(new org.eclipse.core.runtime.Path(fei.getName()+"_diagram")));
			}
		}
 		ModelToProjectUtil.INSTANCE.setEditorInput(input);
 		super.setInput(input);
 	}
 	
 	@Override
 	public void dispose() {
 		try {
 			super.dispose();
 		} catch (Exception e) {
 			// Workaround for a bug in DiagramDocumentEditor
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	public Object getAdapter(Class type) {
 		if (type == IContentOutlinePage.class) {
 			return null;
 		}
 		return super.getAdapter(type);
 	}
 	
 	
 	
 	public static final String EXTERNAL_NODE_LABELS_LAYER = "External Node Labels";
 	
 	/**
 	 * @author Yann Davin
 	 */
 	public void setupCustomConnectionLayerEx() {
 		DiagramRootEditPart root = (DiagramRootEditPart)getDiagramGraphicalViewer().getRootEditPart();
 		LayeredPane printableLayers = (LayeredPane) root
 				.getLayer(LayerConstants.PRINTABLE_LAYERS);
 		Layer connlayer = printableLayers
 				.getLayer(LayerConstants.CONNECTION_LAYER);
 		if (connlayer == null || connlayer instanceof CustomConnectionLayerEx) {
 			return;
 		}
 		
 		printableLayers.removeLayer(LayerConstants.CONNECTION_LAYER);
 		Layer decorationLayer = printableLayers
 				.getLayer(DiagramRootEditPart.DECORATION_PRINTABLE_LAYER);
 		printableLayers
 				.removeLayer(DiagramRootEditPart.DECORATION_PRINTABLE_LAYER);
 		printableLayers.addLayerBefore(new CustomConnectionLayerEx(),
 				LayerConstants.CONNECTION_LAYER, printableLayers
 						.getLayer(LayerConstants.PRIMARY_LAYER));
 		printableLayers.addLayerBefore(decorationLayer,
 				DiagramRootEditPart.DECORATION_PRINTABLE_LAYER, printableLayers
 						.getLayer(LayerConstants.CONNECTION_LAYER));
 
 		FreeformLayer extLabelsLayer = new FreeformLayer();
 		extLabelsLayer.setLayoutManager(new DelegatingLayout());
 		printableLayers.addLayerAfter(extLabelsLayer,
 				EXTERNAL_NODE_LABELS_LAYER,
 				LayerConstants.PRIMARY_LAYER);
 		LayeredPane scalableLayers = (LayeredPane) root
 				.getLayer(LayerConstants.SCALABLE_LAYERS);
 		FreeformLayer scaledFeedbackLayer = new FreeformLayer();
 		scaledFeedbackLayer.setEnabled(false);
 		scalableLayers.addLayerAfter(scaledFeedbackLayer,
 				LayerConstants.SCALED_FEEDBACK_LAYER,
 				DiagramRootEditPart.DECORATION_UNPRINTABLE_LAYER);
 	}
 	
 	
 }
