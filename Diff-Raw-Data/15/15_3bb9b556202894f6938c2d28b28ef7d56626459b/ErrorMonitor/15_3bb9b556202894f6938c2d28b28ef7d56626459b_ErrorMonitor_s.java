 package de.ckulla.errormonitor.internal;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.TaskBar;
 import org.eclipse.swt.widgets.TaskItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.markers.internal.ProblemMarker;
 
 @SuppressWarnings("restriction")
 public class ErrorMonitor implements IResourceChangeListener, IStartup {
 
 	Set<Long> markerIds = new HashSet<Long>();
 	
 	int previousNumberOfMarkers;
 	
 	static TaskItem getTaskBarItem() {
 		TaskBar bar = PlatformUI.getWorkbench().getDisplay().getSystemTaskBar();
 		if (bar == null)
 			return null;
 		TaskItem item = bar.getItem(PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getShell());
 		if (item == null)
 			item = bar.getItem(null);
 		return item;
 	}
 
 	public void earlyStartup() {
 		MarkerFilter filter = new ProblemFilter("all problems");
 		try {
 			@SuppressWarnings("unchecked")
 			final Collection<ProblemMarker> markers = filter.findMarkers(new NullProgressMonitor(), true);
 			if (markers != null) {
 				for (ProblemMarker marker: markers) {
 					if (marker.getSeverity()==IMarker.SEVERITY_ERROR)
 						markerIds.add (marker.getMarker().getId());
 				}
 			}
 			update();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				this,
 				IResourceChangeEvent.POST_CHANGE);
 //						| IResourceChangeEvent.PRE_BUILD
 //						| IResourceChangeEvent.POST_BUILD);
 	}
 
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 	}
 
 	@Override
 	public void resourceChanged(IResourceChangeEvent event) {
 		IMarkerDelta[] deltas = event.findMarkerDeltas(IMarker.PROBLEM, true);
 		if (deltas != null) {
 			for (IMarkerDelta delta : deltas) {
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 					if (delta.getAttribute(IMarker.SEVERITY, -1)==IMarker.SEVERITY_ERROR)
 						markerIds.add (delta.getId());
 					break;
 				case IResourceDelta.REMOVED:
 					if (delta.getAttribute(IMarker.SEVERITY, -1)==IMarker.SEVERITY_ERROR)
 						markerIds.remove (delta.getId());
 					break;
 				}
 			}
 			update();
 		}
 	}
 
 	private void update() {
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		workbench.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				int numberOfMarkers = 0;
				if (markerIds != null && markerIds.size() > 0) {
 					numberOfMarkers = markerIds.size();
					if (numberOfMarkers != previousNumberOfMarkers) {
						getTaskBarItem().setOverlayText(numberOfMarkers>0?String.valueOf(numberOfMarkers):null);
						previousNumberOfMarkers = numberOfMarkers;
					}
 				}
 			}
 		});
 	}
}
