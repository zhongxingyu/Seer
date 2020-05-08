 package com.gratex.perconik.activity.listeners;
 
 import static com.gratex.perconik.activity.DataTransferObjects.setApplicationData;
 import static com.gratex.perconik.activity.DataTransferObjects.setEventData;
 import static com.gratex.perconik.activity.DataTransferObjects.setProjectData;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import sk.stuba.fiit.perconik.core.listeners.TextSelectionListener;
 import sk.stuba.fiit.perconik.eclipse.ui.Editors;
 import com.google.common.base.Throwables;
 import com.gratex.perconik.activity.ActivityServices;
 import com.gratex.perconik.activity.ActivityServices.WatcherServiceOperation;
 import com.gratex.perconik.activity.DataTransferObjects;
 import com.gratex.perconik.services.activity.IVsActivityWatcherService;
 import com.gratex.perconik.services.activity.IdeCodeOperationDto;
 import com.gratex.perconik.services.activity.IdeCodeOperationTypeEnum;
 
 /**
  * A listener of {@code IdeCodeOperation} events. This listener creates
  * {@link IdeCodeOperationDto} data transfer objects and passes them to
  * the <i>Activity Watcher Service</i> to be transferred into the
  * <i>User Activity Client Application</i> for further processing.
  * 
  * <p> TODO document how DTOs are build and what data they contain
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class IdeCodeListener extends IdeListener implements TextSelectionListener
 {
 	// TODO code operation type PasteFromWeb is not supported
 	
 	public IdeCodeListener()
 	{
 	}
 
 	static final class Selection
 	{
 		String text;
 		
 		final Position start = new Position();
 		
 		final Position end = new Position();
 		
 		static final class Position
 		{
 			int line, offset;
 		}
 	}
 	
 	static final void process(final IFile file, final Selection selection)
 	{
 		final IdeCodeOperationDto data = new IdeCodeOperationDto();
 
 		data.setDocument(DataTransferObjects.newDocumentData(file));
 		data.setCode(selection.text);
 		
 		data.setStartColumnIndex(selection.start.offset);
 		data.setStartRowIndex(selection.start.line);
 		
 		data.setEndColumnIndex(selection.end.offset);
 		data.setEndRowIndex(selection.end.line);
 		
 		data.setOperationType(IdeCodeOperationTypeEnum.SELECTION_CHANGED);
 		data.setWebUrl(null);
 		
 		System.out.println("TEXT: "+selection.text+" FROM "+selection.start.line+":"+selection.start.offset+" TO "+selection.end.line+":"+selection.end.offset);
 		
 		setProjectData(data, file.getProject());
 		setApplicationData(data);
 		setEventData(data);
 		
 		ActivityServices.performWatcherServiceOperation(new WatcherServiceOperation()
 		{
 			public final void perform(final IVsActivityWatcherService service)
 			{
 				service.notifyIdeCodeOperationAsync(data);
 			}
 		});
 	}
 
 	public final void selectionChanged(final IWorkbenchPart part, final ITextSelection selection)
 	{
		IEditorPart editor = (IEditorPart) part;
 		
		IFile file = Editors.getFile(editor);
 		
		IDocument document = Editors.getDocument(editor);
 		
 		Selection data = new Selection();
 		
 		data.text = selection.getText();
 		
 		data.start.line = selection.getStartLine();
 		data.end.line   = selection.getEndLine();
 
 		try
 		{
 			int offset = selection.getOffset();
 			
 			data.start.offset = offset - document.getLineOffset(data.start.line);
 			data.end.offset   = offset + selection.getLength() - document.getLineOffset(data.end.line);
 			
 			String delimeter = document.getLineDelimiter(data.end.line);
 			
 			if (data.text.endsWith(delimeter))
 			{
 				data.end.line ++;
 				data.end.offset = 0;
 			}
 		}
 		catch (BadLocationException e)
 		{
 			throw Throwables.propagate(e);
 		}
 		
 		process(file, data);
 	}
 }
