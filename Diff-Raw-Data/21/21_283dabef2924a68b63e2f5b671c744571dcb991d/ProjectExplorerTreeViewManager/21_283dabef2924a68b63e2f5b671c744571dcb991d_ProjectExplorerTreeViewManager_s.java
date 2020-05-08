 package org.codefaces.ui.views;
 
 import java.util.Map;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.core.models.RepoFolder;
 import org.codefaces.core.models.RepoFolderRoot;
 import org.codefaces.core.models.RepoResource;
 import org.codefaces.ui.CodeFacesUIActivator;
 import org.codefaces.ui.Images;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 public class ProjectExplorerTreeViewManager {
 	private final TreeViewer viewer;
 
 	private Display display;
 
 	private Map<Object, Object> loadedResources = new ConcurrentHashMap<Object, Object>();
 
 	private BlockingDeque<RepoResource> waitingQueue = new LinkedBlockingDeque<RepoResource>();
 
 	private class RepoResourceLoadingJob extends Job {
 		public RepoResourceLoadingJob() {
 			super("");
 			setSystem(true);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			RepoResource resource = null;
 			try {
 				while ((resource = waitingQueue.takeFirst()) != null) {
 					loadResource(resource);
 				}
 			} catch (InterruptedException e) {
 				@SuppressWarnings("null")
 				IStatus status = new Status(Status.ERROR,
 						CodeFacesUIActivator.PLUGIN_ID,
 						"Errors occurs when loading resource "
 								+ resource.getFullPath().toString(), e);
 				CodeFacesUIActivator.getDefault().getLog().log(status);
 			}
 
 			return Status.OK_STATUS;
 		}
 
 		private void loadResource(final RepoResource resource) {
 			display.syncExec(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						resource.getChildren();
 						loadedResources.put(resource, resource);
 						viewer.refresh(resource, true);
 					} catch (Exception e) {
 						IStatus status = new Status(Status.ERROR,
 								CodeFacesUIActivator.PLUGIN_ID,
 								"Errors occurs when getting children "
 										+ resource.getFullPath().toString(), e);
 						CodeFacesUIActivator.getDefault().getLog().log(status);
 					}
 				}
 			});
 		}
 	}
 
 	private static class LoadingItem {
 		public String getText() {
 			return "...";
 		}
 
 		public Image getImage() {
 			return null;
 		}
 	}
 
 	public ProjectExplorerTreeViewManager(TreeViewer treeView) {
 		this.viewer = treeView;
 		display = treeView.getControl().getDisplay();
 		new RepoResourceLoadingJob().schedule();
 	}
 
 	public Object[] getElement(Object parent) {
 		if (!(parent instanceof RepoResource)) {
 			return new Object[0];
 		}
 
 		RepoResource resource = (RepoResource) parent;
 		if (!loadedResources.containsKey(resource)) {
 			try {
 				waitingQueue.putLast(resource);
 			} catch (InterruptedException e) {
 				IStatus status = new Status(Status.ERROR,
 						CodeFacesUIActivator.PLUGIN_ID,
 						"Errors occurs when loading resource "
 								+ resource.getFullPath().toString(), e);
 				CodeFacesUIActivator.getDefault().getLog().log(status);
 			}
 
 			return new Object[] { new LoadingItem() };
 		}
 
 		return resource.getChildren().toArray();
 	}
 
 	public String getText(Object obj) {
 		if (obj instanceof LoadingItem) {
 			return ((LoadingItem) obj).getText();
 		}
 
 		if (obj instanceof RepoFolderRoot) {
 			RepoFolderRoot root = (RepoFolderRoot) obj;
 			return root.getBranch().getName() + "@"
 					+ root.getBranch().getRepo().getUrl();
 		}
 
 		if (obj instanceof RepoResource) {
 			return ((RepoResource) obj).getName();
 		}
 
 		return obj.toString();
 	}
 
 	public Image getImage(Object obj) {
 		if (obj instanceof LoadingItem) {
 			return ((LoadingItem) obj).getImage();
 		}
 
		if (obj instanceof RepoFile) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_FILE);
 		}

 		if (obj instanceof RepoFolder) {
 			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_FOLDER);
 		}

		if (obj instanceof RepoFolderRoot) {
			return Images.getImageDescriptorFromRegistry(
					Images.IMG_REPO_FOLDER_ROOT).createImage();
 		}
 
 		return null;
 	}
 }
