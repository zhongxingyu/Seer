 package net.sparktank.morrigan.views;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sparktank.morrigan.Activator;
 import net.sparktank.morrigan.actions.NewPlaylistAction;
 import net.sparktank.morrigan.dialogs.MorriganMsgDlg;
 import net.sparktank.morrigan.handler.CallMediaListEditor;
 import net.sparktank.morrigan.helpers.PlaylistHelper;
 import net.sparktank.morrigan.model.ui.MediaExplorerItem;
 
 import org.eclipse.core.commands.common.CommandException;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.ViewPart;
 
 public class ViewMediaExplorer extends ViewPart {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public static final String ID = "net.sparktank.morrigan.views.ViewMediaExplorer";
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private TableViewer viewer;
 	
 	ArrayList<MediaExplorerItem> items = new ArrayList<MediaExplorerItem>();
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	/**
 	 * This is a callback that will allow us to create the viewer and initialise it.
 	 */
 	public void createPartControl(Composite parent) {
 		makeContent();
 		
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		viewer.setContentProvider(contentProvider);
 		viewer.setLabelProvider(labelProvider);
 		viewer.setInput(getViewSite()); // use content provider.
 		getSite().setSelectionProvider(viewer);
 		viewer.addDoubleClickListener(doubleClickListener);
 		
 		addToolbar();
 		addMenu();
 	}
 	
 	@Override
 	public void dispose() {
 		clearImageCache();
 		super.dispose();
 	}
 	
 	private IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
 		
 		@Override
 		public Object[] getElements(Object inputElement) {
 			return items.toArray();
 		}
 		
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 		
 		@Override
 		public void dispose() {}
 		
 	};
 	
 	private ILabelProvider labelProvider = new ILabelProvider() {
 		
 		@Override
 		public String getText(Object element) {
 			if (element instanceof MediaExplorerItem) {
 				MediaExplorerItem item = (MediaExplorerItem) element;
 				return item.toString();
 			}
 			return null;
 		}
 		
 		@Override
 		public Image getImage(Object element) {
 			if (element instanceof MediaExplorerItem) {
 				MediaExplorerItem item = (MediaExplorerItem) element;
 				switch (item.type) {
 					
 					case PLAYLIST:
 						return readFromImageCache(Activator.getImageDescriptor("icons/playlist.gif"));
 						
 					case LIBRARY:
 						return readFromImageCache(Activator.getImageDescriptor("icons/library.gif"));
 						
 					case DISPLAY:
 						return readFromImageCache(Activator.getImageDescriptor("icons/display.gif"));
 					
 				}
 			}
 			return null;
 		}
 		
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 		
 		@Override
 		public void removeListener(ILabelProviderListener listener) {}
 		@Override
 		public void dispose() {}
 		@Override
 		public void addListener(ILabelProviderListener listener) {}
 	};
 	
 	private IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
 		public void doubleClick(DoubleClickEvent event) {
 			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
 			try {
 				handlerService.executeCommand(CallMediaListEditor.ID, null);
 			} catch (CommandException e) {
 				new MorriganMsgDlg(e).open();
 			}
 		}
 	};
 	
 	private void addToolbar () {
 		getViewSite().getActionBars().getToolBarManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
 	}
 	
 	private void addMenu () {
 		getViewSite().getActionBars().getMenuManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
 		getViewSite().getActionBars().getMenuManager().add(new Separator());
 		getViewSite().getActionBars().getMenuManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
 	}
 	
 	private void makeContent () {
		items.clear();
 		items.add(new MediaExplorerItem("dis", "Display", MediaExplorerItem.ItemType.DISPLAY));
 		items.add(new MediaExplorerItem("lib", "Library", MediaExplorerItem.ItemType.LIBRARY));
 		items.addAll(PlaylistHelper.instance.getAllPlaylists());
 	}
 	
 	private Map<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>();
 	
 	private void clearImageCache () {
 		for (ImageDescriptor i : imageCache.keySet()) {
 			imageCache.get(i).dispose();
 		}
 	}
 	
 	private Image readFromImageCache (ImageDescriptor id) {
 		if (!imageCache.containsKey(id)) {
 			imageCache.put(id, id.createImage());
 		}
 		return imageCache.get(id);
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 	
 	public void refresh () {
		makeContent();
 		viewer.refresh();
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
