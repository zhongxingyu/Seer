 package net.sf.anathema.campaign.note;
 
 import net.sf.anathema.basics.repository.input.NewItemEditorInput;
 import net.sf.anathema.basics.repository.input.ProxyItemEditorInput;
 import net.sf.anathema.basics.repository.itemtype.IItemType;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 
 public class NewNoteActionDelegate implements IObjectActionDelegate {
 
   private static final String NOTES_EDITOR_ID = "net.sf.anathema.campaign.note.noteeditor"; //$NON-NLS-1$
   private IWorkbenchPart targetPart;
 
   @Override
   public void setActivePart(IAction action, IWorkbenchPart targetPart) {
     this.targetPart = targetPart;
   }
 
   @Override
   public void run(IAction action) {
     IWorkbenchPage page = targetPart.getSite().getPage();
     IItemType itemType = NotesRepositoryUtilities.getNotesItemType();
     IEditorInput input = new ProxyItemEditorInput(itemType.getUntitledName(), new NewItemEditorInput(itemType));
     try {
       page.openEditor(input, NOTES_EDITOR_ID);
     }
     catch (PartInitException e) {
      // TODO Fehlerhandling
      e.printStackTrace();
     }
   }
 
   @Override
   public void selectionChanged(IAction action, ISelection selection) {
     // nothing to do
   }
 }
