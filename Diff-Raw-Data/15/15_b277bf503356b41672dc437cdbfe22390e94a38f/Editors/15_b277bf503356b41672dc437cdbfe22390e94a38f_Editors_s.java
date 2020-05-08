 package sk.stuba.fiit.perconik.eclipse.ui;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 
 public final class Editors
 {
 	private Editors()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final IEditorPart getActiveEditor()
 	{
 		return getActiveEditor(Workbenches.getActivePage());
 	}
 	
 	public static final IEditorPart getActiveEditor(final IWorkbenchPage page)
 	{
 		if (page == null)
 		{
 			return null;
 		}
 		
 		return page.getActiveEditor();
 	}
 	
 	public static final ISourceViewer getSourceViewer(final IEditorPart editor)
 	{
 		if (editor == null)
 		{
 			return null;
 		}
 
 		return (ISourceViewer) editor.getAdapter(ITextOperationTarget.class);
 	}
 	
 	public static final StyledText getStyledText(final IEditorPart editor)
 	{
 		ISourceViewer viewer = getSourceViewer(editor);
 		
 		if (viewer == null)
 		{
 			return null;
 		}
 
 		return viewer.getTextWidget();
 	}
 
 	public static final IDocument getDocument(final IEditorPart editor)
 	{
 		ISourceViewer viewer = getSourceViewer(editor);
 		
 		if (viewer == null)
 		{
 			return null;
 		}
 
 		return viewer.getDocument();
 	}
 	
 	public static final IEditorPart waitForActiveEditor()
 	{
		return waitForActiveEditor(Workbenches.getActivePage());
 	}
 
 	public static final IEditorPart waitForActiveEditor(final IWorkbenchPage page)
 	{
 		IEditorPart editor;
 		
 		while ((editor = getActiveEditor(page)) == null) {}
 		
 		return editor;
 	}
 }
