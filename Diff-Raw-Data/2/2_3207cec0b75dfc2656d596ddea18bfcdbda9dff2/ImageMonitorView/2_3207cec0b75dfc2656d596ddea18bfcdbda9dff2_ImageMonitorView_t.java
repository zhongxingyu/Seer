 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.ui.views;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import org.dawb.common.services.IPlotImageService;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.preferences.ViewConstants;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.util.image.ImageFileUtils;
 import org.dawb.common.util.io.FileUtils;
 import org.dawb.common.util.io.SortingUtils;
 import org.dawb.common.util.object.ObjectUtils;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
 import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
 import org.eclipse.nebula.widgets.gallery.Gallery;
 import org.eclipse.nebula.widgets.gallery.GalleryItem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceAdapter;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 
 public class ImageMonitorView extends ViewPart implements MouseListener, SelectionListener {
 
 	public static final String ID = "org.dawb.workbench.views.imageMonitorView"; //$NON-NLS-1$
     
 	private static Logger  logger = LoggerFactory.getLogger(ImageMonitorView.class);
 	
 	private Gallery                  gallery;
 	private GalleryItem              galleryGroup;
 	private BlockingDeque<ImageItem> queue;
 	private Thread                   imageThread;
 	private String                   lastDirectoryPath;
 	private String                   directoryPath;
 	private File[]                   fileList;
 	private IReusableEditor          editor;
 	private Comparator<File>         currentComparitor=SortingUtils.DATE_SORT_BACKWARDS;
 
 	private GallerySelectionProvider selectionProvider;
 	private CLabel                   locationLabel;
 	
 	public ImageMonitorView() throws Exception {
 		this.queue = new LinkedBlockingDeque<ImageItem>(Integer.MAX_VALUE);
 	}
 
 	/**
 	 * Create contents of the view part.
 	 * @param parent
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 
 		parent.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(parent);
 		
 		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		
 		this.locationLabel = new CLabel(parent, SWT.NONE);
 		locationLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		locationLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		if (directoryPath!=null) locationLabel.setText(directoryPath);
 
 		this.gallery = new Gallery(parent, SWT.V_SCROLL | SWT.VIRTUAL | SWT.SINGLE);
 		gallery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		gallery.setToolTipText("Double click to open a file, afterwards the same editor will be used where possible. Right click to start a new editor.");
 		
 		// Renderers
 		final DefaultGalleryGroupRenderer gr = new DefaultGalleryGroupRenderer();
 		gr.setMinMargin(2);
 		
 		// Size image - parameterize this so that the user can change it.
 		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
 		final int    size      = store.getInt(ViewConstants.IMAGE_SIZE);
 		store.addPropertyChangeListener(new IPropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if (!event.getProperty().equals(ViewConstants.IMAGE_SIZE)) return;
 				int side = ObjectUtils.getInteger(event.getNewValue());
 				gr.setItemHeight(side);
 				gr.setItemWidth(side);
 				refreshAll();
 			}
 		});
 		gr.setItemHeight(size);
 		gr.setItemWidth(size);
 		gr.setAutoMargin(true);
 		gallery.setGroupRenderer(gr);
 
 		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
 		gallery.setItemRenderer(ir);
 		
 		
 		// Virtual
 		gallery.setVirtualGroups(true);
 		gallery.addListener(SWT.SetData, new Listener() {
 			public void handleEvent(Event event) {
 				
 				GalleryItem item = (GalleryItem) event.item;
 				int index = gallery.indexOf(item);
 				item.setItemCount(index);
 				
 				final File f = getFile(index);
 				item.setText(f.getName());
 				
 				final ImageItem ii = new ImageItem();
 				ii.setFile(f);
 				ii.setItem(item);
 							 	
 			 	// Add to render queue
 			 	queue.offerFirst(ii);	
 			}
 
 		});
 		
 		DropTarget dt = new DropTarget(gallery, DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
 		dt.setTransfer(new Transfer[] { TextTransfer.getInstance (), FileTransfer.getInstance(), ResourceTransfer.getInstance()});
 		dt.addDropListener(new DropTargetAdapter() {
 			@Override
 			public void drop(DropTargetEvent event) {
 				if (((DropTarget)event.getSource()).getControl()==gallery) return;
                 final Object data = event.data;
                 if (data instanceof String[]) {
                 	setDirectoryPath(((String[])data)[0]);
                 	
                 } else if (data instanceof IResource[]) {
                 	final IResource[] res = (IResource[])data;
                 	setDirectoryPath(res[0].getLocation().toOSString());
                 	
                 } else if (data instanceof File[]) {
                 	setDirectoryPath(((File[])data)[0].getAbsolutePath());
                 }
 			}			
 		});
 		
 		final DragSource dragSource = new DragSource(gallery, DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
 		dragSource.setTransfer(new Transfer[] { FileTransfer.getInstance () });
 		dragSource.addDragListener(new DragSourceAdapter() {
 			public void dragSetData(DragSourceEvent event){
 				if (getSelectedPaths()==null) return;
 				event.data = getSelectedPaths();
 			}
 		});
 
 
 		this.galleryGroup = new GalleryItem(gallery, SWT.VIRTUAL);
 		galleryGroup.setText("Please choose a directory to monitor...");
 		if (directoryPath!=null) refreshAll();
 		
 		createActions();
 		initializeToolBar();
 		initializeMenu();
 		
 		this.selectionProvider = new GallerySelectionProvider();
 		getSite().setSelectionProvider(selectionProvider);
 		
 		gallery.addMouseListener(this);
 		gallery.addSelectionListener(this);
 		
 		try {
 			createImageThread();
 		} catch (Exception e) {
 			logger.error("Cannot start thumbnail thread!", e);
 		}
 
 	}
 	
 	private File getFile(int index) {
 		if (fileList==null) {
 			final List<File> fl = SortingUtils.getSortedFileList(new File(directoryPath), getFileFilter(), currentComparitor);
 			fileList = fl.toArray(new File[fl.size()]);
 		}
 		return fileList[index];
 	}
 	
 	public void refreshAll() {
 		refreshAll(false);
 	}
 	private void refreshAll(final boolean updateSelection) {
 		
 		queue.clear();
 		
 		// We use a job for this as the file list can be large
 		final Job refresh = new Job("Refresh Image Monitor") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				
 				if (directoryPath==null) return Status.CANCEL_STATUS;
 				
 				// We make file list in this thread for speed reasons
 			    final List<File> fl = SortingUtils.getSortedFileList(new File(directoryPath), getFileFilter(), currentComparitor);
 			    if (fl==null) {
 				    setItemCount(0, true, false);
 			    	return Status.CANCEL_STATUS;
 			    }
 			    
 			    fileList = fl.toArray(new File[fl.size()]);
 		        setItemCount(fileList.length, true, updateSelection);
 
 				return Status.OK_STATUS;
 			}
 		};
 		refresh.setPriority(Job.BUILD);
 		refresh.schedule();
 		
 	}
 	private boolean monitoringDirectory = false;
 	
 	public void setMonitoring(final boolean monitoring) {
 		monitoringDirectory = monitoring;
 		updateMonitoring();
 	}
 	
 	public void toggleMonitor() {
 		monitoringDirectory = !monitoringDirectory;
 		updateMonitoring();
 	}
 
 	private void updateMonitoring() {
 		if (monitoringDirectory) {
 
 			// Java 7 has a WatchService precisely for this using nio
 			// WatchService watcher = FileSystems.getDefault().newWatchService();
 			final Thread thread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 
 					while (monitoringDirectory) {
 
 						try {
 							final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
 							final int    rate      = store.getInt("org.dawb.workbench.views.image.monitor.poll.rate");
 							final long   time      = rate*1000;
 							Thread.sleep(time);
 						} catch (InterruptedException e) {
 							logger.error("Cannot monitor "+getDirectoryPath(), e);
 							return;
 						}
 						if (ImageMonitorView.this.fileList==null) continue;
 
 						final File   root  = new File(directoryPath);	
 						if (!root.exists()) {
 							setItemCount(0, true, false);
 							continue;
 						}
 						
 						
 						if (lastDirectoryPath!=null&&lastDirectoryPath.equals(directoryPath)) {
 							
 						    final List<File>   fl = SortingUtils.getSortedFileList(new File(directoryPath), getFileFilter(), currentComparitor);
 							if (fl==null) {
 								setItemCount(0, true, false);
 								continue;
 							}
 						    final int    interval = fl.size()-fileList.length;
 							if (interval!=0) {
 								ImageMonitorView.this.fileList = fl.toArray(new File[fl.size()]);
 							    setItemCount(fileList.length, false, false); // Do not loose other items.
 							} else {
 								// Do nothing - is this right? Maybe same number, different files.
 							}
 						} else { // New directory, refresh the lot
 							fileList = null;
 							refreshAll(true);
 						}
 						ImageMonitorView.this.lastDirectoryPath = directoryPath;
 
 					}
 				}
 			}, "Image Monitor monitor thread");
 			thread.setDaemon(true);
 			thread.start();
 		}	
 	}
 	
 
 	private void setItemCount(final int count, final boolean clear, final boolean updateSelection) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				
 				try {
 					if (queue==null) return;
 					
 					queue.clear();
 					if (clear) galleryGroup.clearAll();
 					galleryGroup.setItemCount(count);
 					galleryGroup.setExpanded(true);
 					gallery.update();
 					
 					final int galCount = galleryGroup.getItemCount();
 					if (galCount>0) {
 						GalleryItem item = galleryGroup.getItem(galCount-1);
 						gallery.setSelection(new GalleryItem[]{item});
 					    
 						if (updateSelection) updateSelection();
 					}
 					
 					gallery.getParent().layout(new Control[]{gallery});
 					
 				} catch (Throwable ne) {
 					logger.error("Error updating gallery content!", ne);
 				}
 
 			}
 	    });
 	}
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		
 		super.init(site);
 
 		try {
 			if (memento==null || memento.getString("DIR")==null) return;
 			this.directoryPath     = memento.getString("DIR");
 		} catch (Exception ne) {
 			throw new PartInitException(ne.getMessage());
 		}
 	}
 
 	@Override
 	public void saveState(IMemento memento) {
 		try {
 			memento.putString("DIR", directoryPath);
 		} catch (Exception e) {
 			logger.error("Cannot save plot bean", e);
 		}
 	}
 
 
 	public String getDirectoryPath() {
 		return directoryPath;
 	}
 
 	public void setDirectoryPath(String directoryPath) {
 		this.directoryPath = directoryPath;
 		locationLabel.setText(directoryPath);
 		this.fileList      = null;
 		refreshAll();
 	}
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		final MenuManager menuManager = new MenuManager();
 		gallery.setMenu(menuManager.createContextMenu(gallery));
 		getSite().registerContextMenu(menuManager, null);
 		
 		// Add toggle buttons which are hard to do in xml config
 		final IToolBarManager      man = getViewSite().getActionBars().getToolBarManager();
 		final CheckableActionGroup grp = new CheckableActionGroup();
		final Action byDate = new Action("Sort by date, newest at the top", IAction.AS_CHECK_BOX) {
 			public void run() {
 				currentComparitor = SortingUtils.DATE_SORT_BACKWARDS;
 				refreshAll();
 			}
 		};
 		byDate.setImageDescriptor(Activator.getImageDescriptor("icons/sortByDate.gif"));
 		grp.add(byDate);
 		man.add(byDate);
 		byDate.setChecked(true);
 		
 		final Action byName = new Action("Sort by name", IAction.AS_CHECK_BOX) {
 			public void run() {
 				currentComparitor = SortingUtils.NATURAL_SORT_CASE_INSENSITIVE;
 				refreshAll();
 			}
 		};
 		byName.setImageDescriptor(Activator.getImageDescriptor("icons/sortByName.gif"));
 		grp.add(byName);
 		man.add(byName);
 		
 		man.add(new Separator());
 		
 		Action prefs = new Action("Preferences...", Activator.getImageDescriptor("icons/data.gif")) {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), ViewConstants.PAGE_ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		man.add(prefs);
 		
 		getViewSite().getActionBars().getMenuManager().add(prefs);
 	}
 
 	/**
 	 * Initialize the toolbar.
 	 */
 	private void initializeToolBar() {
 		getViewSite().getActionBars()
 				.getToolBarManager();
 		
 	}
 
 	/**
 	 * Initialize the menu.
 	 */
 	private void initializeMenu() {
 		getViewSite().getActionBars()
 				.getMenuManager();
 	}
 
 	@Override
 	public void setFocus() {
 		if (gallery!=null&&!gallery.isDisposed()) {
 			gallery.setFocus();
 		}
 	}
 	
 
 	/**
 	 * This will be configured from options but 
 	 * for now it is any image accepted.
 	 * @return
 	 */
 	protected FileFilter getFileFilter() {
 		return new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				if (pathname.isDirectory()) return false;
 				if (pathname.isHidden())    return false;
 				final String name = pathname.getName();
 				if (name==null||"".equals(name)) return false;
 				if (name.startsWith("."))        return false;
 				if (name.endsWith(".edf"))       return true;
 				if (ImageFileUtils.isImage(name))      return true;
 				
 				final String ext = FileUtils.getFileExtension(pathname);
 				if (LoaderFactory.getSupportedExtensions().contains(ext)) return true;
 				return false;
 
 			}
 		};
 	}
 
 	@Override
 	public void mouseDoubleClick(MouseEvent e) {
 		openSelectedLinked();
 	}
 	
 	@Override
 	public void mouseDown(MouseEvent e) {
         //updateSelection();
 	}
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		updateSelection();
 	}
 
 	private void updateSelection() {
 		
 		final File imageFile   = getSelectedFile();
 		if (imageFile==null) return;
 		
 		IActionBars bars = getViewSite().getActionBars();
 		bars.getStatusLineManager().setMessage(imageFile.getAbsolutePath());
 
 		if (editor!=null) {
 			try { // Figure out if disposed
 				editor.getEditorInput();
 				editor.getEditorSite();
 				editor.setFocus();
 			} catch (Throwable ne) {
 				editor = null;
 			}
 		}
 		
 		// If editor not the same, we nullify it and open another one
 		if (editor!=null) {
 			try {
 			    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(imageFile.getAbsolutePath());
 			    if (desc.getId()!=editor.getEditorSite().getId()) {
 					final IEditorPart p = EclipseUtils.openExternalEditor(imageFile.getAbsolutePath());
 					if (p instanceof IReusableEditor) this.editor = (IReusableEditor)p;
 					return;
 			    }
 			} catch (PartInitException e1) {
 				logger.error("Cannot open editor for file: "+imageFile, e1);
 			}
 		}
 		
 		
 		if (editor!=null) {
 			final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(imageFile);
 			final IEditorInput store      = new FileStoreEditorInput(externalFile);
 			editor.setInput(store);
 		}
 		
 		selectionProvider.setSelection(new StructuredSelection(imageFile));
 	}
 
 	public void openSelectedLinked() {
 		
 		final File imageFile   = getSelectedFile();
 		try {
 			final IEditorPart p = EclipseUtils.openExternalEditor(imageFile.getAbsolutePath());
 			if (p instanceof IReusableEditor) this.editor = (IReusableEditor)p;
 		} catch (PartInitException e1) {
 			logger.error("Cannot open editor for file: "+imageFile, e1);
 		}
 	}
 	
 	public void openSelected() {
 		
 		final File imageFile   = getSelectedFile();
 		if (imageFile==null) return;
 		try {
 			EclipseUtils.openExternalEditor(imageFile.getAbsolutePath());
 		} catch (PartInitException e) {
 			logger.error("Cannot open file "+imageFile, e);
 		}
 	}
 
 	private File getSelectedFile() {
 		
 		final GalleryItem[] items = gallery.getSelection();
 		if (items==null || items.length<1) return null;
 		
 		final GalleryItem item = items[0];
 		final int  index       = item.getItemCount();
 		final File imageFile   = getFile(index);
 		return imageFile;
 	}
 	
 	private String[] getSelectedPaths() {
 		
 		final GalleryItem[] items = gallery.getSelection();
 		if (items==null || items.length<1) return null;
 		
 		final String[] fa = new String[items.length];
 		for (int i = 0; i < items.length; i++) {
 			fa[i] = getFile(items[i].getItemCount()).getAbsolutePath();
  		}
 
 		return fa;
 	}
 
 
 	public void dispose() {
 		
 		queue.clear();
 		queue.add(new ImageItem()); // stops queue.
 	
 		if (gallery!=null&&!gallery.isDisposed()) {
 			// Dispose images, may be a lot!
 			for (int i = 0; i<gallery.getItemCount() ; ++i) {
 				if (gallery.getItem(i).getImage()!=null) {
 					gallery.getItem(i).getImage().dispose();
 				}
 			}
 			gallery.removeSelectionListener(this);
 			gallery.removeMouseListener(this);
 			gallery.dispose();
 		}
 		
 		// Nullify variables
 		gallery=null;
 		galleryGroup=null;
 		queue=null;
 		imageThread=null;
 		lastDirectoryPath=null;
 		directoryPath=null;
 		fileList=null;
 		this.editor=null;
 		
 		super.dispose();
 	}
 	
 	private void createImageThread() throws Exception {
 
 		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
 		final IPlotImageService service = (IPlotImageService)ServiceManager.getService(IPlotImageService.class);
 
 		this.imageThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				
 				while(gallery!=null && !gallery.isDisposed()) { // This thread is going all the time.
 					
 					if (queue==null) break; // stops the thread on dispose.
 					
 					ImageItem ii = null;
 
 					try { // We tolerate almost all faults so that the thumbnail service thread keeps working.
  				        ii = queue.take();
 						if (ii==null||ii.getItem()==null||ii.getFile()==null) {
 							Thread.sleep(100);
 							continue;
 						}
 
 						final int    size = store.getInt(ViewConstants.IMAGE_SIZE);
 
 						final Image     image     = service.createImage(ii.getFile(), size, size);
 						// This image must be disposed later!
 						
                         final ImageItem imageItem = ii;
 						Display.getDefault().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								if (imageItem.getItem().isDisposed()) return;
 								try {
 									if (image!=null) {
 										imageItem.getItem().setImage(image);
 									} else {
 										imageItem.getItem().setImage(service.getIconForFile(imageItem.getFile()));
 									}
 								} catch (Throwable ne) {//Intentional, thrown if thread running during shutdown.
 									return;
 								}
 							}
 						});
 					} catch (Throwable ne) {
 						logger.error("Cannot process thumbnail image "+ii.getFile());
 						if (gallery.isDisposed()) return;
 						continue;
 					}
 
 				}
 				
 				logger.debug("Stopped image thumbail generation thread.");
 			}
 		}, "Image View Processing Daemon");
 		
 		imageThread.setDaemon(true);
 		imageThread.start();
 	}
 
 	@Override
 	public void mouseUp(MouseEvent e) {
 		//System.out.println(e);
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// Auto-generated method stub
 		
 	}
 
 }
