 package net.sf.anathema.campaign.note;
 
 import net.sf.anathema.basics.repository.input.NewItemEditorInput;
 import net.sf.anathema.basics.repository.input.ProxyItemEditorInput;
 import net.sf.anathema.basics.repository.itemtype.IItemType;
 
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 
 public class NewNoteActionDelegate implements IObjectActionDelegate {
 
  private static final String PLUGIN_ID = "net.sf.anathema.campaign.note"; //$NON-NLS-1$
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
      Platform.getLog(Platform.getBundle(PLUGIN_ID)).log(
          new Status(IStatus.ERROR, PLUGIN_ID, "Couldn't create editor for new note.", e));
     }
   }
 
   @Override
   public void selectionChanged(IAction action, ISelection selection) {
     // nothing to do
   }
 }
