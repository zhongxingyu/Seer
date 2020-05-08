 package net.todd.biblestudy.rcp.presenters;
 
 import java.util.List;
 
 import net.todd.biblestudy.db.NoteStyle;
 import net.todd.biblestudy.rcp.models.INoteModel;
 import net.todd.biblestudy.rcp.views.CreateLinkDialog;
 import net.todd.biblestudy.rcp.views.ICreateLinkDialog;
 import net.todd.biblestudy.rcp.views.INoteView;
 
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.PlatformUI;
 
 public class NotePresenter implements INoteListener, ICreateLinkListener
 {
 	private INoteView noteView;
 	private INoteModel noteModel;
 	private ICreateLinkDialog createLinkDialog;
 
 	public NotePresenter(INoteView noteView, INoteModel noteModel)
 	{
 		this.noteView = noteView;
 		this.noteModel = noteModel;
 		
		noteView.addNoteViewListener(this);
		
 		handleOpenNote();
 	}
 
 	@Override
 	public void handleEvent(ViewEvent event)
 	{
 		String source = (String)event.getSource();
 		
 		if (source.equals(ViewEvent.NOTE_CONTENT_CHANGED))
 		{
 			handleContentChanged();
 		}
 		else if (source.equals(ViewEvent.NOTE_SHOW_RIGHT_CLICK_MENU))
 		{
 			handleShowRightClickMenu();
 		}
 		else if (source.equals(ViewEvent.NOTE_CREATE_LINK_EVENT))
 		{
 			createLink();
 		}
 		else if (source.equals(ViewEvent.NOTE_CLOSE))
 		{
 			handleCloseNote();
 		}
 		else if (source.equals(ViewEvent.NOTE_SAVE))
 		{
 			handleSaveNote();
 		}
 		else if (source.equals(ViewEvent.NOTE_DELETE))
 		{
 			handleNoteDeleted();
 		}
 	}
 
 	private void handleNoteDeleted()
 	{
 		String secondaryId = noteModel.getNote().getName();
 		noteModel.deleteNoteAndLinks();
 		noteView.closeView(secondaryId);
 	}
 
 	private void handleSaveNote()
 	{
 		noteModel.saveNoteAndLinks();
 		updateDocumentTitle();
 	}
 
 	private void handleCloseNote()
 	{
 		noteView.removeNoteViewListener(this);
 		
 		noteModel = null;
 		noteView = null;
 	}
 
 	private void handleShowRightClickMenu()
 	{
 		Point lastClickedCoordinates = noteView.getLastClickedCoordinates();
 		
 		noteView.showRightClickPopup(lastClickedCoordinates.x, lastClickedCoordinates.y);
 	}
 
 	private void handleContentChanged()
 	{
 		noteModel.updateContent(noteView.getContentText());
 		
 		updateDocumentTitle();
 		
 		updateStylesOnView();
 	}
 
 	private void updateStylesOnView()
 	{
 		List<NoteStyle> updatedStyles = getUpdatedStyles();
 		
 		if (updatedStyles != null)
 		{
 			noteView.replaceNoteStyles(updatedStyles);
 		}
 	}
 
 	private List<NoteStyle> getUpdatedStyles()
 	{
 		List<NoteStyle> stylesForRange = null;
 		
 		String text = noteModel.getNote().getText();
 		
 		if (text != null)
 		{
 			stylesForRange = getStylesForRange(0, text.length());
 		}
 		
 		return stylesForRange;
 	}
 
 	private void updateDocumentTitle()
 	{
 		if (noteModel.isDocumentDirty())
 		{
 			noteView.setViewTitle(noteModel.getNote().getName() + "*");
 		}
 		else
 		{
 			noteView.setViewTitle(noteModel.getNote().getName());
 		}
 	}
 
 	private void handleOpenNote()
 	{
 		noteView.setViewTitle(noteModel.getNote().getName());
 		noteView.setContentText(noteModel.getNote().getText());
 		
 		updateStylesOnView();
 	}
 
 	private void createLink()
 	{
 		createLinkDialog = new CreateLinkDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
 		
 		createLinkDialog.addCreateLinkListener(this);
 		
 		createLinkDialog.openDialog();
 	}
 
 	private List<NoteStyle> getStylesForRange(int startPoint, int stopPoint)
 	{
 		List<NoteStyle> noteStylesForRange = noteModel.getNoteStylesForRange(startPoint, stopPoint);
 		
 		return noteStylesForRange;
 	}
 
 	@Override
 	public void handleCreateLinkEvent(ViewEvent viewEvent)
 	{
 		String source = (String)viewEvent.getSource();
 		
 		if (ViewEvent.CREATE_LINK_DIALOG_OPENED.equals(source)) 
 		{
 			handleCreateLinkDialogOpened();
 		}
 		else if (ViewEvent.CREATE_LINK_DIALOG_CLOSED.equals(source))
 		{
 			handleCreateLinkDialogClosed();
 		}
 		else if (ViewEvent.CREATE_LINK_DO_CREATE_LINK.equals(source))
 		{
 			handleDoCreateLink();
 		}
 	}
 
 	private void handleDoCreateLink()
 	{
 		String linkText = createLinkDialog.getLinkText();
 		Point selection = noteView.getSelectionPoint();
 		
 		int start = selection.x;
 		int stop = selection.y;
 		
 		noteModel.addLink(linkText, start, stop);
 		
 		updateStylesOnView();
 		updateDocumentTitle();
 		
 		createLinkDialog.closeDialog();
 	}
 
 	private void handleCreateLinkDialogClosed()
 	{
 		createLinkDialog.removeCreateLinkListener(this);
 	}
 
 	private void handleCreateLinkDialogOpened()
 	{
 		String selectionText = noteView.getSelectedText();
 		
 		createLinkDialog.setSelectedLinkText(selectionText);
 	}
 }
