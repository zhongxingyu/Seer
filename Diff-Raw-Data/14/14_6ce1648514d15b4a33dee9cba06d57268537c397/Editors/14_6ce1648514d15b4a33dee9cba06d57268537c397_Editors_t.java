 package sk.stuba.fiit.perconik.eclipse.ui;
 
 import javax.annotation.Nullable;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import com.google.common.base.Preconditions;
 
 /**
  * Static utility methods pertaining to Eclipse editors.
  *
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class Editors
 {
 	private Editors()
 	{
 		throw new AssertionError();
 	}
 	
 	/**
 	 * Gets the active editor.
 	 * @return the active editor or {@code null} if there is no active editor
 	 */
 	public static final IEditorPart getActiveEditor()
 	{
 		return getActiveEditor(Workbenches.getActivePage());
 	}
 	
 	/**
 	 * Gets the currently active editor.
 	 * @param page the page, may be {@code null}
 	 * @return the active editor or {@code null} if the page
 	 *         is {@code null} or there is no active editor
 	 */
 	public static final IEditorPart getActiveEditor(@Nullable final IWorkbenchPage page)
 	{
 		if (page == null)
 		{
 			return null;
 		}
 		
 		return page.getActiveEditor();
 	}
 	
	public static final IResource getResource(@Nullable final IEditorPart editor)
 	{
		return editor != null ? (IResource) editor.getEditorInput().getAdapter(IResource.class) : null;
 	}
 
	public static final IFile getFile(@Nullable final IEditorPart editor)
 	{
		if (editor == null)
		{
			return null;
		}
		
 		IEditorInput input = editor.getEditorInput();
 		
 		if (input instanceof IFileEditorInput)
 		{
 			return ((IFileEditorInput) input).getFile();
 		}
 		
 		IResource resource = getResource(editor);
 		
 		return resource instanceof IFile ? (IFile) resource : null;
 	}
 
 	/**
 	 * Gets the source viewer from given editor.
 	 * @param editor the editor, may be {@code null}
 	 * @return the source viewer or {@code null} if the editor
 	 *         is {@code null} or there is no source viewer
 	 */
 	public static final ISourceViewer getSourceViewer(@Nullable final IEditorPart editor)
 	{
 		if (editor == null)
 		{
 			return null;
 		}
 
 		Object viewer = editor.getAdapter(ITextOperationTarget.class);
 		
 		return viewer instanceof ISourceViewer ? (ISourceViewer) viewer : null;
 	}
 	
 	/**
 	 * Gets the text widget from given editor.
 	 * @param editor the editor, may be {@code null}
 	 * @return the text widget or {@code null} if the editor
 	 *         is {@code null} or there is no text widget
 	 */
 	public static final StyledText getStyledText(@Nullable final IEditorPart editor)
 	{
 		ISourceViewer viewer = getSourceViewer(editor);
 		
 		if (viewer == null)
 		{
 			return null;
 		}
 
 		return viewer.getTextWidget();
 	}
 
 	/**
 	 * Gets the input document from given editor.
 	 * @param editor the editor, may be {@code null}
 	 * @return the document or {@code null} if the editor
 	 *         is {@code null} or there is no document 
 	 */
 	public static final IDocument getDocument(@Nullable final IEditorPart editor)
 	{
 		ISourceViewer viewer = getSourceViewer(editor);
 		
 		if (viewer == null)
 		{
 			return null;
 		}
 
 		return viewer.getDocument();
 	}
 	
 	/**
 	 * Waits for the currently active editor.
 	 * This method blocks until there is an active editor.
 	 * @see #getActiveEditor()
 	 */
 	public static final IEditorPart waitForActiveEditor()
 	{
 		return waitForActiveEditor(Workbenches.waitForActivePage());
 	}
 
 	/**
 	 * Waits for the currently active editor.
 	 * This method blocks until there is an active editor.
 	 * @param page the page, can not be {@code null}
 	 * @see #getActiveEditor(IWorkbenchPage)
 	 */
 	public static final IEditorPart waitForActiveEditor(final IWorkbenchPage page)
 	{
 		Preconditions.checkNotNull(page);
 		
 		IEditorPart editor;
 		
 		while ((editor = getActiveEditor(page)) == null) {}
 		
 		return editor;
 	}
 }
