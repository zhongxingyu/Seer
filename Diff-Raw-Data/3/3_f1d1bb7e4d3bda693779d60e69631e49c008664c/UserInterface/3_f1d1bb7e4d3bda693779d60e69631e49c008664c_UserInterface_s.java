 package nl.cwi.sen.metastudio;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 
 import metastudio.graph.MetaGraphFactory;
 import nl.cwi.sen.metastudio.adt.editordata.EditorDataFactory;
 import nl.cwi.sen.metastudio.adt.texteditor.ActionList;
 import nl.cwi.sen.metastudio.adt.texteditor.TextEditorFactory;
 import nl.cwi.sen.metastudio.bridge.UserEnvironmentBridge;
 import nl.cwi.sen.metastudio.bridge.UserEnvironmentTif;
 import nl.cwi.sen.metastudio.editor.MetaEditor;
 import nl.cwi.sen.metastudio.moduleview.ModuleExplorerPart;
 import nl.cwi.sen.metastudio.moduleview.ModuleInfoPart;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermList;
 import aterm.pure.PureFactory;
 
 public class UserInterface implements UserEnvironmentTif, Runnable {
 	private static IStatusLineManager statusLineMgr;
 
 	private PureFactory factory;
 	private EditorDataFactory editorDataFactory;
 	private TextEditorFactory textEditorFactory;
 	private MetaGraphFactory metaGraphFactory;
 	private static UserEnvironmentBridge bridge;
 	private static Thread t;
 	private static PopupMenu popupMenu;
 	private static EditorRegistry editorRegistry;
 
 	private ATerm ACTION_MENUBAR;
 	private ATerm ACTION_TOOLBAR;
 	private ATerm ACTION_MODULE_POPUP;
 	private ATerm ACTION_NEW_MODULE_POPUP;
 
 	public UserInterface() {
 	}
 
 	public UserInterface(IStatusLineManager statusLineManager) {
 		statusLineMgr = statusLineManager;
 	}
 
 	public void run() {
 		factory = new PureFactory();
 		editorDataFactory = new EditorDataFactory(factory);
 		textEditorFactory = new TextEditorFactory(factory);
 
//		ATerm trm = factory.parse("[menu([\"Actions\",\"Parse\"]),menu([\"Actions\",\"ViewTree\"])]");
//		ActionList aL = textEditorFactory.ActionListFromTerm(trm);

 		metaGraphFactory = new MetaGraphFactory();
 		bridge = new UserEnvironmentBridge(factory, this);
 
 		MetastudioConnection f =
 			new MetastudioConnection(
 				bridge,
 				factory,
 				editorDataFactory,
 				textEditorFactory,
 				metaGraphFactory);
 
 		String[] args = new String[6];
 		args[0] = "-TB_HOST_NAME";
 		args[1] = "localhost";
 		args[2] = "-TB_PORT";
 		args[3] = "9000";
 		args[4] = "-TB_TOOL_NAME";
 		args[5] = "user-environment";
 
 		try {
 			bridge.init(args);
 		} catch (UnknownHostException e) {
 		}
 		try {
 			bridge.connect();
 		} catch (IOException e) {
 		}
 
 		t = new Thread(bridge);
 		t.start();
 
 		initialize();
 	}
 
 	private void initialize() {
 		popupMenu = new PopupMenu();
 		editorRegistry = new EditorRegistry();
 
 		initializeATermPatterns();
 	}
 
 	private void initializeATermPatterns() {
 		ACTION_MENUBAR = factory.parse("studio-menubar");
 		ACTION_TOOLBAR = factory.parse("studio-toolbar");
 		ACTION_MODULE_POPUP = factory.parse("module-popup");
 		ACTION_NEW_MODULE_POPUP = factory.parse("new-module-popup");
 	}
 
 	public void initializeUi(String s0) {
 		//		final String str0 = s0;
 		//		Display.getDefault().asyncExec(new Runnable() {
 		//			public void run() {
 		//				ModuleExplorerPart.addModule(str0);
 		//			}
 		//		});
 	}
 
 	public void buttonsFound(
 		ATerm actionType,
 		String moduleName,
 		ATerm actions) {
 		if (actionType.equals(ACTION_MENUBAR)) {
 			//addMenu(buttonType, moduleName, (ATermList) buttons);
 		} else if (actionType.equals(ACTION_TOOLBAR)) {
 			//addToolBarActions((ATermList) buttons);
 		} else {
 			popupMenu.setMenu(actionType, moduleName, (ATermList) actions);
 		}
 	}
 
 	public void addStatus(ATerm t0, String s1) {
 		System.out.println(
 			"UserInterface.java: Add status not implemented yet!");
 	}
 
 	public void addStatusf(ATerm t0, String s1, ATerm t2) {
 		final String message = formatString(s1, (ATermList) t2);
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				statusLineMgr.setMessage(message);
 			}
 		});
 	}
 
 	public void endStatus(ATerm t0) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				statusLineMgr.setMessage("");
 			}
 		});
 	}
 
 	public void error(String s0) {
 		System.out.println("UserInterface.java: Error not implemented yet!");
 	}
 
 	public void errorf(String s0, ATerm t1) {
 		System.out.println("UserInterface.java: Errorf not implemented yet!");
 	}
 
 	public void message(String s0) {
 		System.out.println("UserInterface.java: Message not implemented yet!");
 	}
 
 	public void messagef(String s0, ATerm t1) {
 		System.out.println("UserInterface.java: Messagef not implemented yet!");
 	}
 
 	public void displayMessage(ATerm t0, String s1) {
 		System.out.println(
 			"UserInterface.java: Display message not implemented yet!");
 	}
 
 	public void warning(String s0) {
 		System.out.println("UserInterface.java: Warning not implemented yet!");
 	}
 
 	public void warningf(String s0, ATerm t1) {
 		System.out.println("UserInterface.java: Warningf not implemented yet!");
 	}
 
 	public void clearHistory() {
 		System.out.println(
 			"UserInterface.java: Clear history not implemented yet!");
 	}
 
 	private void setModules(ATermList importList) {
 		// TODO moduleManager.clearModules();
 		final ATermList _importList = importList;
 
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				ATermList importList = _importList;
 				while (!importList.isEmpty()) {
 					ATermList importPair = (ATermList) importList.getFirst();
 					importList = importList.getNext();
 					ATermAppl moduleTerm = (ATermAppl) importPair.getFirst();
 					String name = moduleTerm.getName();
 					ModuleExplorerPart.addModule(name);
 				}
 			}
 		});
 	}
 
 	public void deleteModules(ATerm mods) {
 		final ATermList _modules = (ATermList) mods;
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				ATermList modules = _modules;
 				for (; !modules.isEmpty(); modules = modules.getNext()) {
 					String moduleName =
 						((ATermAppl) modules.getFirst()).getAFun().getName();
 					ModuleExplorerPart.removeModule(moduleName);
 				}
 			}
 		});
 	}
 
 	public void moduleInfo(final String module, ATerm info) {
 		ATermList pairs = (ATermList) info;
 		List entries = new LinkedList();
 
 		while (!pairs.isEmpty()) {
 			ATermList pair = (ATermList) pairs.getFirst();
 
 			String name = pair.getFirst().toString();
 
 			String value;
 			ATerm valueTerm = pair.getNext().getFirst();
 			if (valueTerm.getType() == ATerm.APPL) {
 				value = ((ATermAppl) valueTerm).getName();
 			} else {
 				value = valueTerm.toString();
 			}
 			String[] entry = { name, value };
 			entries.add(entry);
 
 			pairs = pairs.getNext();
 		}
 
 		final List finalEntries = entries;
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				ModuleInfoPart.setModuleInfo(module, finalEntries);
 			}
 		});
 	}
 
 	public void updateList(String s0, String s1) {
 		System.out.println(
 			"UserInterface.java: Update list not implemented yet!");
 	}
 
 	public void newGraph(ATerm t0) {
 		setModules((ATermList) t0);
 	}
 
 	public void displayGraph(String s0, ATerm t1) {
 		System.out.println(
 			"UserInterface.java: Display graph not implemented yet!");
 	}
 
 	public void graphLayouted(String s0, ATerm t1) {
 		System.out.println(
 			"UserInterface.java: Graph layouted not implemented yet!");
 	}
 
 	public ATerm showQuestionDialog(final String question) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				// Hack to get a shell for the messagebox
 				Shell shell = ModuleExplorerPart.getShell();
 
 				ATerm answer = factory.make("snd-value(answer(cancel))");
 				if (shell != null) {
 					MessageBox messageBox =
 						new MessageBox(
 							PlatformUI
 								.getWorkbench()
 								.getActiveWorkbenchWindow()
 								.getShell(),
 							SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
 					messageBox.setMessage(question);
 
 					int choice = messageBox.open();
 
 					if (choice == SWT.YES) {
 						answer = factory.make("snd-value(answer(yes))");
 					} else if (choice == SWT.NO) {
 						answer = factory.make("snd-value(answer(no))");
 					}
 				}
 				try {
 					bridge.sendTerm(answer);
 				} catch (IOException e) {
 				}
 			}
 		});
 
 		// keep compiler happy...
 		return null;
 	}
 
 	// Not used in Eclipse GUI
 	public ATerm showFileDialog(String s0, String s1, String s2) {
 		return null;
 	}
 
 	public void editFile(
 		final ATerm editorId,
 		String editor,
 		final String fileName) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				IWorkbenchPage page =
 					PlatformUI
 						.getWorkbench()
 						.getActiveWorkbenchWindow()
 						.getActivePage();
 
 				IPath path = new Path(fileName);
 				IFile file =
 					MetastudioPlugin
 						.getWorkspace()
 						.getRoot()
 						.getFileForLocation(
 						path);
 
 				IEditorPart part = null;
 				try {
 					part = page.openEditor(file);
 				} catch (Exception e) {
 				}
 
 				if (editorRegistry.getEditorPartByeditorId(editorId) == null
 					&& part != null) {
 					editorRegistry.addEditor(editorId, fileName, part);
 				}
 			}
 		});
 	}
 
 	public void editorDisconnected(IEditorPart part) {
 		ATerm editorId = editorRegistry.geteditorIdByEditorPart(part);
 		MetastudioConnection connection = new MetastudioConnection();
 		connection.getBridge().postEvent(
 			connection.getPureFactory().make(
 				"editor-disconnected(<term>)",
 				editorId));
 
 		editorRegistry.removeEditor((IEditorPart) part);
 	}
 
 	public void killEditor(ATerm editorId) {
 		System.out.println(
 			"UserInterface.java: Kill editor not implemented yet!");
 	}
 
 	public void setActions(final ATerm editorId, final ATerm actionList) {
 		System.out.println("SetActions");
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				MetaEditor part =
 					(MetaEditor) editorRegistry.getEditorPartByeditorId(
 						editorId);
 				ActionList _actionList =
 					textEditorFactory.ActionListFromTerm(actionList);
 				part.setActions(editorId, _actionList);
 			}
 		});
 	}
 
 	public void editorToFront(final ATerm editorId) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				IWorkbenchPage page =
 					PlatformUI
 						.getWorkbench()
 						.getActiveWorkbenchWindow()
 						.getActivePage();
 
 				String fileName =
 					editorRegistry.getFileNameByeditorId(editorId);
 				IPath path = new Path(fileName);
 				IFile file =
 					MetastudioPlugin
 						.getWorkspace()
 						.getRoot()
 						.getFileForLocation(
 						path);
 
 				try {
 					page.openEditor(file);
 				} catch (Exception e) {
 				}
 			}
 		});
 	}
 
 	public void getContents(final ATerm editorId, final ATerm focus) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				MetaEditor part =
 					(MetaEditor) editorRegistry.getEditorPartByeditorId(
 						editorId);
 				part.getContents(
 					editorId,
 					editorDataFactory.FocusFromTerm(focus));
 			}
 		});
 	}
 
 	public void rereadContents(ATerm t0) {
 		System.out.println(
 			"UserInterface.java: Reread contents not implemented yet!");
 	}
 
 	public void setCursorAtLocation(final ATerm editorId, final int location) {
 		System.out.println(
 			"UserInterface.java: Set cursor @ location not implemented yet!");
 	}
 
 	public void setCursorAtFocus(final ATerm editorId, final ATerm focus) {
 		System.out.println(
 			"UserInterface.java: Set cursor @ focus not implemented yet!");
 	}
 
 	public void setFocus(final ATerm editorId, final ATerm focus) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				MetaEditor part =
 					(MetaEditor) editorRegistry.getEditorPartByeditorId(
 						editorId);
 				part.setFocus(editorDataFactory.FocusFromTerm(focus));
 			}
 		});
 	}
 
 	public void clearFocus(final ATerm editorId) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				MetaEditor part =
 					(MetaEditor) editorRegistry.getEditorPartByeditorId(
 						editorId);
 				part.clearFocus();
 			}
 		});
 	}
 
 	String formatString(String format, ATermList args) {
 		int index;
 		String prefix = "";
 		String postfix = format;
 		while ((index = postfix.indexOf("%")) != -1) {
 			prefix += postfix.substring(0, index);
 			switch (postfix.charAt(index + 1)) {
 				case 't' :
 				case 'd' :
 					prefix += args.getFirst().toString();
 					break;
 				case 's' :
 					prefix += ((ATermAppl) args.getFirst()).getName();
 					break;
 				default :
 					prefix += "%" + postfix.charAt(index + 1);
 			}
 			postfix = postfix.substring(index + 2);
 			args = args.getNext();
 		}
 		return prefix + postfix;
 	}
 
 	public void recAckEvent(ATerm t0) {
 	}
 
 	public void recTerminate(ATerm t0) {
 	}
 }
