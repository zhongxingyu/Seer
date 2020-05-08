 package org.eclipse.wst.xml.vex.ui.internal.editor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.ui.internal.Logger;
 import org.eclipse.wst.xml.ui.internal.tabletree.XMLEditorMessages;
 import org.eclipse.wst.xml.ui.internal.tabletree.XMLTableTreeViewer;
 import org.eclipse.wst.xml.vex.ui.internal.swt.VexWidget;
 
 public class VEXMultiPageEditorPart extends MultiPageEditorPart {
 
 	private StructuredTextEditor textEditor = null;
 	private VexEditorMultiPage vexEditor = null;
 	private int sourcePageIndex;
 	private int visualPageIndex;
 	private IDOMDocument modelDocument;
 	
 	protected void createPages() {
 		try {
 			createSourcePage();
 			createVisualEditorPage();
 			addVisualEditorPage();
 			addSourcePage();
 			
 			VexWidget vexWidget = vexEditor.getVexWidget();
 			modelDocument = (IDOMDocument) vexWidget.getDocument().getDOMDocument();
 			textEditor.getTextViewer().setDocument(modelDocument.getStructuredDocument());
 			setActivePage(visualPageIndex);
 
 		} catch (PartInitException e) {
 			Logger.logException(e);
 			throw new RuntimeException(e);
 		}
 
 	}
 	
 	private void createSourcePage() {
 		textEditor = new StructuredTextEditor();
 		textEditor.setEditorPart(this);
 	}
 	
 	private void createVisualEditorPage() {
 		vexEditor = new VexEditorMultiPage();
 	}
 	
 	/**
 	 * Adds the source page of the multi-page editor.
 	 */
 	private void addSourcePage() throws PartInitException {
 		sourcePageIndex = addPage(textEditor, getEditorInput());
 		setPageText(sourcePageIndex, XMLEditorMessages.XMLMultiPageEditorPart_0);
 		vexEditor.setDomDoc(modelDocument);
 		firePropertyChange(PROP_TITLE);
 
 		// Changes to the Text Viewer's document instance should also
 		// force an input refresh
 		textEditor.getTextViewer().addTextInputListener(new TextInputListener());
 	}
 	
 	private void addVisualEditorPage() throws PartInitException {
 		visualPageIndex = addPage(vexEditor, getEditorInput());
 		setPageText(visualPageIndex, "Author");
 		firePropertyChange(PROP_TITLE);
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 	  vexEditor.doSave(monitor);
 	}
 
 	public void doSaveAs() {
 		vexEditor.doSaveAs();
 	}
 
 	public boolean isSaveAsAllowed() {
 		return vexEditor.isSaveAsAllowed();
 	}
 
 	private IDocument getDocument() {
 		IDocument document = null;
 		if (textEditor != null) {
 			document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
 		}
 		return document;
 	}
 	
 	private void setDOMDocument(IDocument document) {
 	    IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForRead(getDocument());
 	    IDOMDocument modelDocument = null;
 	    try {
 	      if (model instanceof IDOMModel) {
 	        modelDocument = ((IDOMModel)model).getDocument();
 	      }
 	    } finally {
 	      if (model != null)
 	        model.releaseFromRead();
 	    }
 	}

	public VexEditorMultiPage getVexEditor() {
		return vexEditor;
	}
 }
