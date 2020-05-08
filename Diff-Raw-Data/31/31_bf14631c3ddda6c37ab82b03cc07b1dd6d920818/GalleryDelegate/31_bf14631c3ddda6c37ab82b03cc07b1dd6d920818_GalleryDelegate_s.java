 package org.dawb.common.ui.views;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import org.dawb.common.services.IPlotImageService;
 import org.dawb.common.services.PlotImageData;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.preferences.ViewConstants;
 import org.dawb.common.util.object.ObjectUtils;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
 import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
 import org.eclipse.nebula.widgets.gallery.Gallery;
 import org.eclipse.nebula.widgets.gallery.GalleryItem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 /**
  * May be used to create a lazy gallery of images.
  * 
  * @author fcp94556
  *
  */
 public class GalleryDelegate {
 	
 	private static final Logger logger = LoggerFactory.getLogger(GalleryDelegate.class);
 
 	private Composite                parent;
 	private Thread                   imageThread;
 	private Gallery                  gallery;
 	private GalleryItem              galleryGroup;
 	private BlockingDeque<ImageItem> queue;
 	private GalleryDelegateInfo      info;
 	
 	private ImageServiceBean lockedHistogram = null;
 
 	public ImageServiceBean getLockedHistogram() {
 		return lockedHistogram;
 	}
 
 	public void setLockedHistogram(ImageServiceBean lockedHistogram) {
 		this.lockedHistogram = lockedHistogram;
 		if (lockedHistogram!=null) refreshAll();
 	}
 
 	private String groupLabel;
 
 	
 	public GalleryDelegate() {
 		this.queue = new LinkedBlockingDeque<ImageItem>(Integer.MAX_VALUE);
 		createImageThread();
 	}
 	
 	public Control getControl() {
 		return parent;
 	}
 	
 	public void createContent(String groupLabel, String toolTip, Composite parent) {
 		
 		this.parent     = parent;
 		this.groupLabel = groupLabel;
 		this.gallery    = new Gallery(parent, SWT.V_SCROLL | SWT.VIRTUAL | SWT.MULTI);
 		gallery.setToolTipText(toolTip);
 		
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
 				if (gr.getGallery().isDisposed()) return;
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
 				
 				item.setText(info.getItemName(index, true));
 				
 				final ImageItem ii = new ImageItem();
 				ii.setIndex(index);
 				ii.setItem(item);
 							 	
 			 	// Add to render queue
 			 	queue.offerFirst(ii);	
 			}
 
 		});
 
 		this.galleryGroup = new GalleryItem(gallery, SWT.VIRTUAL);
 		galleryGroup.setText(groupLabel);
 	}
 	
 	public void setLayoutData(Object layoutData) {
 		gallery.setLayoutData(layoutData);
 	}
 	
 	public void setData(GalleryDelegateInfo info) {
 		this.info = info;
 		refreshAll();
 	}
 	
 	public GalleryDelegateInfo getData() {
 		return info;
 	}
 	
 	public void clear() {
 		queue.clear();
 		galleryGroup.clearAll();
 		galleryGroup.setItemCount(0);
 		galleryGroup.setExpanded(true);
 		galleryGroup.setText(groupLabel);
 		
 		gallery.update();
 	}
 	
 	private String selectionDataLabel = "Slice";
 	
 
 	public String getSelectionDataLabel() {
 		return selectionDataLabel;
 	}
 
 	public void setSelectionDataLabel(String selectionDataLabel) {
 		this.selectionDataLabel = selectionDataLabel;
 	}
 
 	public List<IDataset> getSelectionData(GalleryItem[] items) {
 		
 		final List<IDataset> ys = new ArrayList<IDataset>(11);
 		for (GalleryItem item : items) {
 			final ImageItem ii = new ImageItem();
 			ii.setIndex(item.getItemCount());
 			ii.setItem(item);
             try {
             	IDataset slice = info.getData(true, ii);
             	slice.setName(selectionDataLabel+" "+info.getItemName(item.getItemCount(), false));
 				ys.add((AbstractDataset)slice);
 			} catch (Exception e) {
 				logger.error("Cannot slice ", e);
 				continue;
 			}
 		}
         return ys;
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
 				
 				
 		
 			    Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						galleryGroup.clearAll();
 						galleryGroup.setItemCount(info.getSize());
 						galleryGroup.setExpanded(true);
 						galleryGroup.setText(info.getName());
 						
 						gallery.update();
 						gallery.getParent().layout(new Control[]{gallery});
 
 						GalleryItem item = galleryGroup.getItem(galleryGroup.getItemCount()-1);
 						gallery.setSelection(new GalleryItem[]{item});
 					    
 					}
 			    });
 
 				return Status.OK_STATUS;
 			}
 		};
 		refresh.setPriority(Job.INTERACTIVE);
 		refresh.schedule();
 		
 	}
 
 
 	private void createImageThread() {
 
 		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
 
 		this.imageThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(gallery==null || !gallery.isDisposed()) { // This thread is going all the time.
 					try {
 						if (queue==null) break;
 						final ImageItem ii = queue.take();
 						if (ii.getItem()==null) return;
 						if (ii.getIndex()<0)    return;
 						if (ii.getItem().isDisposed()) continue;
 
 						final IDataset set = info.getData(false, ii);
 						if (set==null && info.getDirectThumbnailPath()==null) continue;
 
 						// Generate thumbnail
 						int size  = store.getInt(ViewConstants.IMAGE_SIZE);
 						if (size<1) size = 96;
 
 						final Image image;
 						if (info.getDirectThumbnailPath()!=null) {
 							final String path = info.getPath(ii.getIndex());
 							image = new Image(Display.getDefault(), new BufferedInputStream(new FileInputStream(new File(path))));
 						} else {
 							final IPlotImageService service = (IPlotImageService)ServiceManager.getService(IPlotImageService.class);	            		
 							final PlotImageData id = new PlotImageData(set, size, size);
 							if (getLockedHistogram()!=null) id.setImageServiceBean(lockedHistogram);
 							image = service.getImage(id);
 						}
 
 						Display.getDefault().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								if (ii.getItem().isDisposed()) return;
 								if (ii.getItem().getParent().isDisposed()) return;
 								if (image!=null) {
 									if (image.isDisposed()) return;
 									ii.getItem().setImage(image);
 									ii.getItem().setText(info.getItemName(ii.getIndex(), true));
 								}
 							}
 						});
 					} catch (Throwable ne) {
 						logger.error("Cannot process images", ne);
 						try {
 							Thread.sleep(1000);// Wait a second before carrying on.
 						} catch (InterruptedException e) {
 							logger.error("Cannot sleep in Gallery Thread!", e);
 						} 
 					}
 				}
 			}
 
 		}, "Image View Processing Daemon");
 
 		imageThread.setDaemon(true);
 		imageThread.start();
 	}
 
 	public void setMenu(MenuManager menuManager) {
 		gallery.setMenu(menuManager.createContextMenu(gallery));
 	}
 
 	public void setFocus() {
 		if (gallery!=null&&!gallery.isDisposed()) {
 			gallery.setFocus();
 		}
 	}
 
 	public GalleryItem[] getSelection() {
 		return gallery.getSelection();
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
 			gallery.dispose();
 		}
 		
 		// Nullify variables
 		gallery=null;
 		galleryGroup=null;
 		queue=null;
 		imageThread=null;
 	}
 
 	public void addMouseListener(MouseListener listener) {
		gallery.addMouseListener(listener);
 	}
 	public void addSelectionListener(SelectionListener listener) {
		gallery.addSelectionListener(listener);
 	}
 	public void removeMouseListener(MouseListener listener) {
		gallery.removeMouseListener(listener);
 	}
 	public void removeSelectionListener(SelectionListener listener) {
		gallery.removeSelectionListener(listener);
 	}
 
 	public String getPath(int itemCount) {
 		return info.getPath(itemCount);
 	}
 
 }
