 package coed.plugin.base;
 
 import java.util.HashMap;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 
 import coed.base.common.ICoedCommunicator;
 import coed.base.common.ICoedVersioner;
 import coed.base.data.CoedFile;
 import coed.base.data.CoedFileLine;
 import coed.base.data.CoedProject;
 import coed.base.data.IFileObserver;
 import coed.base.data.exceptions.InvalidConfigFileException;
import coed.base.data.exceptions.NotConnectedToServerException;
 import coed.base.data.exceptions.UnknownVersionerTypeException;
 import coed.collab.client.CoedCommunicatorFactory;
 import coed.plugin.views.IFileTree;
 import coed.plugin.views.IUserList;
 
 
 public class StandardController implements IPluginController, IPartListener, IFileObserver {
 	private ICoedCommunicator communicator;
 	private HashMap<AbstractDecoratedTextEditor,CoedFile> editors;
 	private AbstractDecoratedTextEditor activeEditor;
 	private IDocument activeDocument;
 	private IFileTree fileTree;
 	private IUserList userList;
 	
 	public StandardController(){
 		//TODO: ask an ICoedCommunicator-factory to give us an instance
 		try {
			this.communicator = new CoedCommunicatorFactory().create("");
 		} catch (UnknownVersionerTypeException e) {
 			// TODO Auto-generated catch block
 			this.communicator=null;
 			e.printStackTrace();
 		} catch (InvalidConfigFileException e) {
 			// TODO Auto-generated catch block
 			this.communicator=null;
 			e.printStackTrace();
 		}
 		
 		this.editors = new HashMap<AbstractDecoratedTextEditor, CoedFile>();
 		this.activeEditor = null;
 	}
 
 	@Override
 	public boolean attachFileTree(IFileTree filet) {
 		//TODO: not maybe it should be a list...
 		this.fileTree=filet;
 		return true;	
 	}
 	
 	@Override
 	public void detachFileTree(IFileTree filet) {
 		//TODO: not maybe it should be a list...
 		if (this.fileTree.equals(filet)) this.fileTree=null;
 		
 	}
 
 	@Override
 	public boolean attachUserList(IUserList userl) {
 		//TODO: not maybe it should be a list...
 		this.userList=userl;
 		return true;
 	}
 
 	@Override
 	public void detachUserList(IUserList userl) {
 		//TODO: not maybe it should be a list...
 		if (this.userList.equals(userl)) this.userList=null;
 	}
 	
 	private CoedFile findCoedFileFor(AbstractDecoratedTextEditor texte){
 		return null;
 	}
 	
 	private void setAsActive(AbstractDecoratedTextEditor texte){
 		activeEditor=texte;
 		activeDocument=activeEditor.getDocumentProvider().getDocument(activeEditor.getEditorInput());
 		communicator.addChangeListener(this);
 		//TODO: should update based on changes
 		//TODO: need to listen for user input 
 	}
 	
 	@Override
 	public void startCollabFor(AbstractDecoratedTextEditor texte) {
 		editors.put(texte, findCoedFileFor(texte));
 		//TODO: set as active ?
 		setAsActive(texte);
 		/*DEBUG*/System.out.println("Going collab for: "+texte);
 		texte.getSite().getPage().addPartListener(this);
 		/*texte.getDocumentProvider().
 			getAnnotationModel(activeEditor.getEditorInput()).
 			addAnnotation(new Annotation("org.eclipse.ui.workbench.texteditor.spelling", true, "xxx"), new Position(100, 200));*/
 		//TODO: listen to it, to see if it is focused, then make it current
 		
 	}
 
 	@Override
 	public void endCollabFor(AbstractDecoratedTextEditor texte) {
 		//TODO: maybe do some stuff before closing
 		//TODO: restore contents if needed
 		
 		/*DEBUG*/System.out.println("Ending collab for: "+texte);
 		
 		if (activeEditor!=null && activeEditor.equals(texte)) {
 			activeDocument=null;
 			activeEditor=null;
 		}
 		editors.remove(texte);
 		//last one closes the door
 		if (editors.size()==0) {
 			texte.getSite().getPage().removePartListener(this);
 		}
 	}
 
 	@Override
 	public String[] getAllUsers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String[] getCollabUsers(CoedFile file) {
		try {
			return communicator.getActiveUsers(file);
		} catch(NotConnectedToServerException e) {
			// TODO: handle this
			e.printStackTrace();
		}
		return null;
 	}
 
 	@Override
 	public CoedFile requestInfo(CoedFile file) {
 		return null;
 	}
 
 	@Override
 	public CoedProject requestInfo(CoedProject project) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean requestVersionAction(String action, CoedFile file) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean requestVersionAction(String action, CoedProject project) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	//----------------------------------------------------
 	//Listeners for editor activation, deactivation.
 	//Won't use all of them, because we are not really interested in every action :)
 	
 	
 	@Override
 	public void partActivated(IWorkbenchPart part) {
 		if (editors.containsKey(part)) {
 			setAsActive((AbstractDecoratedTextEditor) part);
 			/*DEBUG*/System.out.println("Resumed : "+part);
 		}
 	}
 
 	@Override
 	public void partBroughtToTop(IWorkbenchPart part) {
 		//not doing anything because activate is fired after 'to top'.
 		//so listening to that instead.
 	}
 
 	@Override
 	public void partClosed(IWorkbenchPart part) {
 		if (editors.containsKey(part)) {
 			//not try-catching here, because we are pretty sure if it
 			// is in the map, it is ok.
 			endCollabFor((AbstractDecoratedTextEditor)part);
 		}
 	}
 
 	@Override
 	public void partDeactivated(IWorkbenchPart part) {
 		if (part.equals(activeEditor)){
 			activeEditor=null;
 			activeDocument=null;
 			/*DEBUG*/System.out.println("Paused : "+part);
 		}
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPart part) {
 		//not doing anything here. :-"
 	}
 
 	//This is the part where the controller receives info about changes to coed files.
 	//At this moment it will update the active document, and ignore other updates.
 	//ATTENTION: This is just an informing method, it does not contain the actual data
 	
 	public void update(CoedFile file) {
 		//TODO: real equality checking
 		if (activeEditor!=null && editors.get(activeEditor).getPath().equals(file.getPath())) {
 			try {
 				showChanges(file, activeDocument);
 			} catch (BadLocationException e) {
 				//this will need a soft reset
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void showChanges(CoedFile file, IDocument doc) throws BadLocationException {
		CoedFileLine[] lines;
		try {
			lines = communicator.getChanges(file);
		} catch(NotConnectedToServerException e) {
			e.printStackTrace();
			// TODO: handle this
			return;
		}
		
 		if (lines.length > 0) {
 			for (int i = 0; i < lines.length; i++) {
 				String[] text = lines[i].getText();
 				Integer lineNum = lines[i].getLine();
 				for (int j = 0; j < text.length; j++) {
 					activeDocument.replace(activeDocument.getLineOffset(lineNum+j), 0, text[j]);
 				}
 			}
 		}
 
 	}
 
 }
