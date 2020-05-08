 package dk.aamj.itu.plugin.view.option3.views;
 
 
 import java.io.File;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.*;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.SWT;
 
 
 public class MainView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "dk.aamj.itu.plugin.view.option3.views.MainView";
 
 	private TableViewer viewer;
 	private Action InsertAction;
 	private Action CopyAction;
 	
 	private Map<String, String> templateList = new HashMap<String, String>();
 	private IntentHandler intentHandler = new IntentHandler();
 
 
 	class ViewContentProvider implements IStructuredContentProvider {
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 		
 		public Object[] getElements(Object parent) {
 			
 			return templateList.values().toArray();
 			
 		}
 		
 	}
 	
 	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
 		public String getColumnText(Object obj, int index) {
 			return getText(obj);
 		}
 		public Image getColumnImage(Object obj, int index) {
 			return getImage(obj);
 		}
 		public Image getImage(Object obj) {
 			return PlatformUI.getWorkbench().
 					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
 		}
 	}
 	class NameSorter extends ViewerSorter {
 	}
 
 	/**
 	 * The constructor.
 	 */
 	public MainView() {
 		
 		super();
 				
 		// Read the file names inside /templates and add options
 		URI uri = null;
 		try {
 			
 			URL url = FileLocator.find(Platform.getBundle("dk.aamj.itu.plugin.view.option3"), new Path("templates"), null);
 			if(url != null)
 				uri = FileLocator.resolve(url).toURI();
 			
 		} catch (Exception e) {
 			
 			showMessage("couldnt locate templates folder");
 			
 		}
 		
 		if(uri != null) {
 			
 			File folder = new File(uri);
 			File[] listOfFiles = folder.listFiles();
 			for (int i = 0; i < listOfFiles.length; i++) {
 				
 				if (listOfFiles[i].isFile()) {
 					
 					String formattedFileName = listOfFiles[i].getName().replace("_", ".").replace(".txt", "");
 					templateList.put(listOfFiles[i].getName(), formattedFileName);
 					
 				}
 				
 			}
 			
 			
 		}
 		
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 		viewer.setInput(getViewSite());
 
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "dk.aamj.itu.plugin.view.option3.viewer");
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				MainView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		
 		if (viewer.getSelection().isEmpty())
 			return;
 		
 		manager.add(CopyAction);
 		
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		
 	}
 
 
 	private void makeActions() {
 
 		CopyAction = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 
 				try {
 
 					intentHandler.CopyIntent(obj.toString().replace(".", "_"));
 
 				} catch (Exception e) {
 
 					e.printStackTrace();
 					showMessage("ERROR: " + e);
 				}
 
 			}
 		};
 		CopyAction.setText("Copy");
 		CopyAction.setToolTipText("Copy selected Intent snippet to clipboard");
 		CopyAction.setImageDescriptor(
 				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK)
 				);
 
 		InsertAction = new Action() {
 			public void run() {
 				
 				ISelection selection = viewer.getSelection();
				if(selection != null)
 					return;
 				
 				Object obj = ((IStructuredSelection)selection).getFirstElement();
 
 				try {
 
 					int result = intentHandler.InsertIntent(obj.toString().replace(".", "_"));
 
 					// Add more error checking here
 					if(result == 0) {
 
 						// Should we create the method for them?
 						showMessage("Please create a method, and position cursor inside method.");
 
 					}
 					// else if (result == 1) { showMessage("You owe me a beer"); } //Success
 
 				} catch (Exception e) {
 
 					e.printStackTrace();
 					showMessage("This is log " + e);
 				}
 
 			}
 		};
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				InsertAction.run();
 			}
 		});
 	}
 	private void showMessage(String message) {
 		MessageDialog.openInformation(
 				viewer.getControl().getShell(),
 				"Main View",
 				message
 				);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 }
