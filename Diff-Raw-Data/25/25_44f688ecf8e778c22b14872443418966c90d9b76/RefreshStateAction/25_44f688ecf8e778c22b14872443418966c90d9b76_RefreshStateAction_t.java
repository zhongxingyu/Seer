 package net.sourceforge.eclipseccase.ui.actions;
 
 import java.util.*;
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.team.core.Team;
 
 public class RefreshStateAction extends ClearcaseWorkspaceAction {
 	/*
 	 * Method declared on IActionDelegate.
 	 */
 	public void execute(IAction action) {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			public void run(final IProgressMonitor monitor) throws CoreException {
 				try {
 					IResource[] resources = getSelectedResources();
 					beginTask(monitor, "Refreshing state...", resources.length);
 
 					if (ClearcasePlugin.isUseQuickRefresh()) {
 						Map map = new TreeMap();
						List fileResources = new ArrayList();
						
 						for (int i = 0; i < resources.length; i++) {
 							IResource resource = resources[i];
 							monitor.subTask("Collecting members for: " + resource.getName());
 							checkCanceled(monitor);
 							if (resource.getType() == IResource.FILE) {
								fileResources.add(resource);
								continue;
 							}
 							if (ClearcaseProvider.getClearcaseProvider(resource) == null) {
 								// Skip resource if it has no ClearCase provider
 								continue;
 							}
 
 							String location = resource.getLocation().toOSString();
 							if (!map.containsKey(location)) {
 								map.put(location, resource);
 							}
 						}
 
 						List result = new ArrayList();
 
 						String path = null;
 						Iterator iterator = map.keySet().iterator();
 						while (iterator.hasNext()) {
 							String current = (String) iterator.next();
 							if (path == null || !current.startsWith(path)) {
 								path = current;
 								result.add(map.get(current));
 							} else {
 								IResource curr = (IResource) map.get(current);
 								IResource old = (IResource) map.get(path);
 								if (curr.getProject() != old.getProject()) {
 									result.add(map.get(current));
 									path = current;
 								}
 							}
 						}
 
						// Refresh directories using quick refresh
 						iterator = result.iterator();
						// Don't iterate, just pick the ClearCase provider from the first element
 						if (iterator.hasNext()) {
 							ClearcaseProvider clearcaseProvider = ClearcaseProvider.getClearcaseProvider((IResource) iterator.next());
 							clearcaseProvider.refreshRecursive((IResource[]) result.toArray(new IResource[0]), subMonitor(monitor));
 							checkCanceled(monitor);
 						}
						
						// Refresh files using normal refresh
						iterator = fileResources.iterator();
						while (iterator.hasNext()) {
							IResource resource = (IResource) iterator.next();
							checkCanceled(monitor);
							ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
							provider.refresh(resource, false);
						}
 
 					} else {
 						for (int i = 0; i < resources.length; i++) {
 							IResource resource = resources[i];
 							checkCanceled(monitor);
 							ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
							provider.refresh(resource, false);
 						}
 					}
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 
 		executeInBackground(runnable, "Refreshing state");
 	}
 
 	public boolean isEnabled() {
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
 		if (resources.length == 1) {
 
 			// always ignore derived resources
 			if (Team.isIgnoredHint(resources[0]))
 				return false;
 
 			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resources[0]);
 			if (provider == null)
 				return false;
 		}
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
 			if (provider == null)
 				return false;
 		}
 		return true;
 	}
 
 }
