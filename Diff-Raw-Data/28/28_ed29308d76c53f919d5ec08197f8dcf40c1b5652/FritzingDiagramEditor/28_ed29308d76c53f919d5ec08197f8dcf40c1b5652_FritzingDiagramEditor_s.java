 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
 import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
 import org.eclipse.gmf.runtime.diagram.ui.internal.properties.WorkspaceViewerProperties;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocumentProvider;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.IEditorInput;
 import org.fritzing.fritzing.diagram.edit.parts.SketchGridLayer;
 
 /**
  * @generated
  */
 public class FritzingDiagramEditor extends DiagramDocumentEditor {
 
 	/**
 	 * @generated
 	 */
 	public static final String ID = "org.fritzing.fritzing.diagram.part.FritzingDiagramEditorID"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final String CONTEXT_ID = "org.fritzing.fritzing.diagram.ui.diagramContext"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public FritzingDiagramEditor() {
 		super(true);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected String getContextID() {
 		return CONTEXT_ID;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected PaletteRoot createPaletteRoot(PaletteRoot existingPaletteRoot) {
 		PaletteRoot root = super.createPaletteRoot(existingPaletteRoot);
 		new FritzingPaletteFactory().fillPalette(root);
 		return root;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected PreferencesHint getPreferencesHint() {
 		return FritzingDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT;
 	}
 
 	/**
 	 * @generated
 	 */
 	public String getContributorId() {
 		return FritzingDiagramEditorPlugin.ID;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected IDocumentProvider getDocumentProvider(IEditorInput input) {
 		if (input instanceof URIEditorInput) {
 			return FritzingDiagramEditorPlugin.getInstance()
 					.getDocumentProvider();
 		}
 		return super.getDocumentProvider(input);
 	}
 
 	/**
 	 * @generated
 	 */
 	public TransactionalEditingDomain getEditingDomain() {
 		IDocument document = getEditorInput() != null ? getDocumentProvider()
 				.getDocument(getEditorInput()) : null;
 		if (document instanceof IDiagramDocument) {
 			return ((IDiagramDocument) document).getEditingDomain();
 		}
 		return super.getEditingDomain();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setDocumentProvider(IEditorInput input) {
 		if (input instanceof URIEditorInput) {
 			setDocumentProvider(FritzingDiagramEditorPlugin.getInstance()
 					.getDocumentProvider());
 		} else {
 			super.setDocumentProvider(input);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor#configureGraphicalViewer()
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 		if (getDiagramGraphicalViewer() instanceof DiagramGraphicalViewer) {
 			((DiagramGraphicalViewer) getDiagramGraphicalViewer())
 					.hookWorkspacePreferenceStore(getWorkspaceViewerPreferenceStore());
			// XXX: turns the grid color RED on Mac..
			getWorkspaceViewerPreferenceStore().setValue(
 					WorkspaceViewerProperties.GRIDLINECOLOR,
					SketchGridLayer.THIS_FORE.hashCode());
			getWorkspaceViewerPreferenceStore().setValue(
 					WorkspaceViewerProperties.GRIDLINESTYLE, SWT.LINE_SOLID);
			getWorkspaceViewerPreferenceStore().setValue(
 					WorkspaceViewerProperties.GRIDORDER, false);
			getWorkspaceViewerPreferenceStore().setValue(
 					WorkspaceViewerProperties.ZOOM, 1.0);
 		}
 		
 		configurePaletteViewer(); // see note there
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditorWithFlyOutPalette#configurePaletteViewer()
 	 */
 	@Override
 	protected void configurePaletteViewer() {
 //		super.configurePaletteViewer();
 		// XXX: this doesn't get called anywhere, maybe bug in GMF -- 
 		// so we are calling this from createGraphicalViewer()
 		PaletteViewer viewer = getEditDomain().getPaletteViewer();
 		if (viewer != null) {
 			viewer.getPaletteViewerPreferences().
 				setLayoutSetting(PaletteViewerPreferences.LAYOUT_ICONS);
 		}
 	}
 
 }
