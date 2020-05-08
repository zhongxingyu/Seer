 package com.wijlens.vektis.editors;
 
 
 import java.util.List;
 
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.MultiPageEditorPart;
 
 import com.wijlens.vektis.domain.Bericht;
 import com.wijlens.vektis.domain.GegevensElement;
 import com.wijlens.vektis.domain.GegevensElementListener;
 import com.wijlens.vektis.domain.Record;
 
 /**
  * An example showing how to create a multi-page editor.
  * This example has 3 pages:
  * <ul>
  * <li>page 0 contains a nested text editor.
  * <li>page 1 allows you to change the font used in page 2
  * <li>page 2 shows the words in page 0 in sorted order
  * </ul>
  */
 public class VektisEditor extends MultiPageEditorPart implements IResourceChangeListener{
 
 	/** The text editor used in page 0. */
 	private TextEditor editor;
 
 	private TreeViewer treeViewer;
 
 	private VektisEditorContentProvider treeContentProvider;
 
 	private Tree tree;
 	
 	private boolean isPageModified;
 
 	private Bericht bericht;
 	/**
 	 * Creates a multi-page editor example.
 	 */
 	public VektisEditor() {
 		super();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 	/**
 	 * Creates page 0 of the multi-page editor,
 	 * which contains a text editor.
 	 */
 	void createPage0() {
 		try {
 			editor = new TextEditor();
 			int index = addPage(editor, getEditorInput());
 			setPageText(index, editor.getTitle());
 		} catch (PartInitException e) {
 			ErrorDialog.openError(
 				getSite().getShell(),
 				"Error creating nested text editor",
 				null,
 				e.getStatus());
 		}
 	}
 	/**
 	 * Creates page 1 of the multi-page editor,
 	 * which allows you to change the font used in page 2.
 	 */
 	void createPage1() {
 
 		treeViewer = new TreeViewer(getContainer(),SWT.MULTI);
 		
 		tree = treeViewer.getTree();
 		
 		tree.setHeaderVisible(true);
 		
 		tree.addListener(SWT.MouseHover, new Listener() {
 		      public void handleEvent(Event event) {
 		        Point point = new Point(event.x, event.y);
 		        ViewerCell cell = treeViewer.getCell(point);
 		        if (cell != null) {
 		        	Object element = cell.getElement();
 		    		String waarde = "";
 		    		if (element instanceof Record) {
 		    			List<GegevensElement> elementen = ((Record) element).elementen();
 		    			if (cell.getColumnIndex() < elementen.size()) {
 		    				waarde = elementen.get(cell.getColumnIndex()).label();
 		    				IStatusLineManager statusLine = getEditorSite().getActionBars().getStatusLineManager();
 				            statusLine.setMessage(waarde);
 		    			}
 		    		}
 		        	
 		        }
 		      }
 		    });
 
 		int index = addPage(tree);
 		setPageText(index, "Document");
 	}
 	/**
 	 * Creates the pages of the multi-page editor.
 	 */
 	protected void createPages() {
 		createPage0();
 		createPage1();
 		
 		initTreeContent();
 	}
 	private void initTreeContent() {
 		treeContentProvider = new VektisEditorContentProvider();
 		treeViewer.setContentProvider(treeContentProvider);
 		treeViewer.setCellModifier(new VektisEditorCellModifier(this,treeViewer));
 		ColumnViewerToolTipSupport.enableFor(treeViewer); 
 		treeViewer.setLabelProvider(new VektisEditorCellLabelProvider(editor,treeViewer));
 		
 		
 		//treeViewer.setInput(new Bericht());
 		treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				updateTreeFromTextEditor();
 			}
 
 			
 		});
 		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
 	}
 	
 	private void updateTreeFromTextEditor() {
 		bericht = new Bericht(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());
 		
 		bericht.addListener(new GegevensElementListener() {
 			
 			@Override
 			public void wijziging(GegevensElement gegevensElement, String oldValue,
 					String newValue) {
 				treeViewer.update(gegevensElement.record(), new String[]{gegevensElement.record().elementen().indexOf(gegevensElement) + ""});
 				treeModified();
 			}
 		});
 		
 		int maxLineSize = bericht.getMaxLineSize();
 		String[] columnProperties = new String[maxLineSize];
 		TextCellEditor[] cellEditors = new TextCellEditor[maxLineSize];
 		for (int i = 0; i < maxLineSize; i++) {
 			TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);
 			treeColumn.setText(i + 1 + "");
 			treeColumn.setWidth(100);
 			
 			columnProperties[i] = i + "";
 			
 			cellEditors[i] = new TextCellEditor(tree);
 		}
 		treeViewer.setCellEditors(cellEditors);
 		treeViewer.setColumnProperties(columnProperties);
 		treeViewer.setInput(bericht);
 		
 	}
 	
 	protected void treeModified() {
 		boolean wasDirty = isDirty();
 		
 		isPageModified = true;
 		
 		if(!wasDirty){
 			firePropertyChange(IEditorPart.PROP_DIRTY);
 		}
 	}
 	
	
	
 	@Override
 	protected void handlePropertyChange(int propertyId) {
 		if(propertyId == IEditorPart.PROP_DIRTY){
 			isPageModified = isDirty();
 		}
 		super.handlePropertyChange(propertyId);
 	}
 	
	
	
 	@Override
 	public boolean isDirty() {
 		return isPageModified || super.isDirty();
 	}
 	
 	protected void pageChange(int newPageIndex){
 		switch(newPageIndex){
 		case 0 :
			if(isDirty()){
 				updateTextEditorFromTree();
 			}
 			break;
 		case 1 :
			if(isPageModified){
				updateTreeFromTextEditor();
				
 			}
 			break;
 		}
 		isPageModified = false;
 		super.pageChange(newPageIndex);
 	}
 	
 	private void updateTextEditorFromTree() {
 		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(bericht.parse());
 		
 	}
 	/**
 	 * The <code>MultiPageEditorPart</code> implementation of this 
 	 * <code>IWorkbenchPart</code> method disposes all nested editors.
 	 * Subclasses may extend.
 	 */
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 	/**
 	 * Saves the multi-page editor's document.
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		updateTextWhenTreeIsModified();
 		getEditor(0).doSave(monitor);
 	}
 	private void updateTextWhenTreeIsModified() {
 		if(getActivePage()== 1 && isPageModified){
 			updateTextEditorFromTree();
 		}
		isPageModified =false;
 	}
 	/**
 	 * Saves the multi-page editor's document as another file.
 	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
 	 * to correspond to the nested editor's.
 	 */
 	public void doSaveAs() {
 		updateTextWhenTreeIsModified();
 		IEditorPart editor = getEditor(0);
 		editor.doSaveAs();
 		setPageText(0, editor.getTitle());
 		setInput(editor.getEditorInput());
 	}
 	
 	
 	/* (non-Javadoc)
 	 * Method declared on IEditorPart.
 	 */
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 	
 	
 	/**
 	 * Closes all project files on project close.
 	 */
 	public void resourceChanged(final IResourceChangeEvent event){
 		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
 			Display.getDefault().asyncExec(new Runnable(){
 				public void run(){
 					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
 					for (int i = 0; i<pages.length; i++){
 						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
 							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
 							pages[i].closeEditor(editorPart,true);
 						}
 					}
 				}            
 			});
 		}
 	}
 }
