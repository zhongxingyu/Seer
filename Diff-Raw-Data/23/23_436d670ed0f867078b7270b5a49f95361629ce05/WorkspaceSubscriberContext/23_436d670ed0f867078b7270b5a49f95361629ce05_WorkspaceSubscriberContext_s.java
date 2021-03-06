 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.ccvs.ui.mappings;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.team.core.diff.IDiffNode;
 import org.eclipse.team.core.diff.IThreeWayDiff;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.core.history.IFileState;
 import org.eclipse.team.core.mapping.*;
 import org.eclipse.team.core.subscribers.Subscriber;
 import org.eclipse.team.core.synchronize.SyncInfo;
 import org.eclipse.team.core.synchronize.SyncInfoFilter;
 import org.eclipse.team.core.synchronize.SyncInfoFilter.ContentComparisonSyncInfoFilter;
 import org.eclipse.team.core.variants.IResourceVariant;
 import org.eclipse.team.internal.ccvs.core.*;
 import org.eclipse.team.internal.ccvs.core.client.PruneFolderVisitor;
 import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
 import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
 import org.eclipse.team.internal.ccvs.ui.Policy;
 
 public class WorkspaceSubscriberContext extends CVSSubscriberMergeContext {
 
 	public static IMergeContext createContext(IResourceMappingScope scope, IProgressMonitor monitor) throws CoreException {
 		Subscriber subscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
 		WorkspaceSubscriberContext mergeContext = new WorkspaceSubscriberContext(subscriber, scope);
 		mergeContext.initialize(monitor, true);
 		return mergeContext;
 	}
 	
 	protected WorkspaceSubscriberContext(Subscriber subscriber, IResourceMappingScope scope) {
 		super(subscriber, scope);
 	}
 
 	public void markAsMerged(final IDiffNode node, final boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
 		run(new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				// Get the latest sync info for the file (i.e. not what is in the set).
 				// We do this because the client may have modified the file since the
 				// set was populated.
 				IResource resource = getDiffTree().getResource(node);
 				if (resource.getType() != IResource.FILE)
 					return;
 				SyncInfo info = getSyncInfo(resource);
 				ensureRemotesMatch(resource, node, info);
 				if (info instanceof CVSSyncInfo) {
 					CVSSyncInfo cvsInfo = (CVSSyncInfo) info;		
 					cvsInfo.makeOutgoing(monitor);
 					if (inSyncHint) {
 						// Compare the contents of the file with the remote
 						// and make the file in-sync if they match
 						ContentComparisonSyncInfoFilter comparator = new SyncInfoFilter.ContentComparisonSyncInfoFilter(false);
 						if (resource.getType() == IResource.FILE && info.getRemote() != null) {
 							if (comparator.compareContents((IFile)resource, info.getRemote(), Policy.subMonitorFor(monitor, 100))) {
 								ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
 								cvsFile.checkedIn(null, false /* not a commit */);
 							}
 						}
 					}
 				}
 			}
 		}, getMergeRule(node), IResource.NONE, monitor);
 	}
 
 	protected void ensureRemotesMatch(IResource resource, IDiffNode node, SyncInfo info) throws CVSException {
 		IResourceVariant variant = info.getRemote();
 		IFileState remote = getRemote(node);
 		if (variant != null && remote != null && remote instanceof IFileRevision) {
 			String ci1 = variant.getContentIdentifier();
 			String ci2 = ((IFileRevision)remote).getContentIdentifier();
 			if (!ci1.equals(ci2)) {
 				throw new CVSException(NLS.bind(CVSUIMessages.WorkspaceSubscriberContext_0, resource.getFullPath().toString()));
 			}
 		}
 	}
 
 	private IFileState getRemote(IDiffNode node) {
 		if (node == null) return null;
 		if (node instanceof IThreeWayDiff) {
 			IThreeWayDiff twd = (IThreeWayDiff) node;
 			return getRemote(twd.getRemoteChange());
 		}
 		if (node instanceof IResourceDiff) {
 			IResourceDiff rd = (IResourceDiff) node;
 			return rd.getAfterState();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.core.mapping.MergeContext#merge(org.eclipse.team.core.diff.IDiffNode, boolean, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus merge(IDiffNode delta, boolean force, IProgressMonitor monitor) throws CoreException {
 		// First, verify that the provided delta matches the current state
 		// i.e. it is possible that a concurrent change has occurred
 		SyncInfo info = getSyncInfo(getDiffTree().getResource(delta));
		if (info == null || info.getKind() == SyncInfo.IN_SYNC || SyncInfo.getDirection(info.getKind()) == SyncInfo.OUTGOING) {
 			// Seems like this one was already merged so return OK
 			return Status.OK_STATUS;
 		}
 //		IDiffNode currentState = SyncInfoToDiffConverter.getDeltaFor(info);
 //		if (!equals(currentState, delta)) {
 //			throw new CVSException(NLS.bind(CVSUIMessages.CVSMergeContext_1, delta.getPath()));
 //		}
 		IStatus status = super.merge(delta, force, monitor);
 		if (status.isOK() && delta.getKind() == IDiffNode.REMOVE) {
 			IResource resource = getDiffTree().getResource(delta);
 			if (resource.getType() == IResource.FILE && !resource.exists()) {
 				// TODO: This behavior is specific to an update from the same branch
 				ICVSResource localResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
 				localResource.unmanage(monitor);
 			}
 			pruneEmptyParents(new IDiffNode[] { delta });
 		}
 		return status;
 	}
 
 	private void pruneEmptyParents(IDiffNode[] deltas) throws CVSException {
 		// TODO: A more explicit tie in to the pruning mechanism would be preferable.
 		// i.e. I don't like referencing the option and visitor directly
 		if (!CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) return;
 		ICVSResource[] cvsResources = new ICVSResource[deltas.length];
 		for (int i = 0; i < cvsResources.length; i++) {
 			cvsResources[i] = CVSWorkspaceRoot.getCVSResourceFor(getDiffTree().getResource(deltas[i]));
 		}
 		new PruneFolderVisitor().visit(
 			CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot()),
 			cvsResources);
 	}
 
 }
