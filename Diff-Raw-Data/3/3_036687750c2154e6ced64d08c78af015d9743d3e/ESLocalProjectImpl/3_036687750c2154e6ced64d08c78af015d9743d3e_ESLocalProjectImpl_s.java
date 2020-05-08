 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl.api;
 
 import static org.eclipse.emf.emfstore.internal.common.APIUtil.copy;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.exceptions.ESProjectNotSharedException;
 import org.eclipse.emf.emfstore.client.util.ESVoidCallable;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.common.model.ESModelElementId;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.client.model.impl.WorkspaceBase;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.api.AbstractAPIImpl;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ESModelElementIdImpl;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESLocalProjectIdImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESBranchVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESPrimaryVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESTagVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.exceptions.ESUpdateRequiredException;
 import org.eclipse.emf.emfstore.server.model.ESBranchInfo;
 import org.eclipse.emf.emfstore.server.model.ESGlobalProjectId;
 import org.eclipse.emf.emfstore.server.model.ESHistoryInfo;
 import org.eclipse.emf.emfstore.server.model.ESLocalProjectId;
 import org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESBranchVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 
 /**
  * <p>
  * Mapping between {@link ESLocalProject} and {@link ProjectSpace}.
  * </p>
  * <p>
  * All methods except {@code getModelElements()} are wrapped in commands by default.
  * </p>
  * 
  * @author emueller
  * 
  */
 public class ESLocalProjectImpl extends AbstractAPIImpl<ESLocalProjectImpl, ProjectSpace>
 	implements ESLocalProject {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param projectSpace
 	 *            the delegate
 	 */
 	public ESLocalProjectImpl(ProjectSpace projectSpace) {
 		super(projectSpace);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getGlobalProjectId()
 	 */
 	public ESGlobalProjectId getGlobalProjectId() {
 		checkIsShared();
 		return RunESCommand.runWithResult(new Callable<ESGlobalProjectId>() {
 			public ESGlobalProjectId call() throws Exception {
 				return toInternalAPI().getProjectId().toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getProjectName()
 	 */
 	public String getProjectName() {
 		return RunESCommand.runWithResult(new Callable<String>() {
 			public String call() throws Exception {
 				return toInternalAPI().getProjectName();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#delete(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(final IProgressMonitor monitor) throws IOException {
 		RunESCommand.WithException.run(IOException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().delete(monitor);
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#resolveVersionSpec(org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec resolveVersionSpec(ESVersionSpec versionSpec, final IProgressMonitor monitor)
 		throws ESException {
 
 		checkIsShared();
 
 		final ESVersionSpecImpl<?, ? extends VersionSpec> versionSpecImpl = (ESVersionSpecImpl<?, ?>) versionSpec;
 
 		final PrimaryVersionSpec resolvedVersionSpec =
 			RunESCommand.WithException.runWithResult(ESException.class, new Callable<PrimaryVersionSpec>() {
 				public PrimaryVersionSpec call() throws Exception {
 					return new ServerCall<PrimaryVersionSpec>(toInternalAPI(), monitor) {
 						@Override
 						protected PrimaryVersionSpec run() throws ESException {
 							return getConnectionManager().resolveVersionSpec(
 								getSessionId(),
 								toInternalAPI().getProjectId(),
 								versionSpecImpl.toInternalAPI());
 						}
 					}.execute();
 				}
 			});
 
 		return resolvedVersionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getBranches(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESBranchInfo> getBranches(final IProgressMonitor monitor) throws ESException {
 		checkIsShared();
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<List<ESBranchInfo>>() {
 			public List<ESBranchInfo> call() throws Exception {
 				final List<BranchInfo> branchInfos = new ServerCall<List<BranchInfo>>(toInternalAPI(), monitor) {
 					@Override
 					protected List<BranchInfo> run() throws ESException {
 						return getConnectionManager().getBranches(
 							getSessionId(),
 							toInternalAPI().getProjectId());
 					}
 				}.execute();
 
 				return APIUtil.mapToAPI(ESBranchInfo.class, branchInfos);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#addTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void addTag(ESPrimaryVersionSpec versionSpec, ESTagVersionSpec tag, final IProgressMonitor monitor)
 		throws ESException {
 
 		checkIsShared();
 
 		final PrimaryVersionSpec primaryVersionSpec = ((ESPrimaryVersionSpecImpl) versionSpec).toInternalAPI();
 		final TagVersionSpec tagVersionSpec = ((ESTagVersionSpecImpl) tag).toInternalAPI();
 
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				new ServerCall<Void>(toInternalAPI(), monitor) {
 					@Override
 					protected Void run() throws ESException {
 						getConnectionManager().addTag(
 							getUsersession().getSessionId(),
 							toInternalAPI().getProjectId(),
 							primaryVersionSpec,
 							tagVersionSpec);
 						return null;
 					}
 				}.execute();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#removeTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void removeTag(ESPrimaryVersionSpec versionSpec, ESTagVersionSpec tag, final IProgressMonitor monitor)
 		throws ESException {
 
 		checkIsShared();
 
 		final PrimaryVersionSpec primaryVersionSpec = ((ESPrimaryVersionSpecImpl) versionSpec).toInternalAPI();
 		final TagVersionSpec tagVersionSpec = ((ESTagVersionSpecImpl) tag).toInternalAPI();
 
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				new ServerCall<Void>(toInternalAPI(), monitor) {
 					@Override
 					protected Void run() throws ESException {
 						getConnectionManager().removeTag(
 							getSessionId(),
 							toInternalAPI().getProjectId(),
 							primaryVersionSpec,
 							tagVersionSpec);
 						return null;
 					}
 				}.execute();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getModelElement(java.lang.Object)
 	 */
 	public EObject getModelElement(ESModelElementId modelElementId) {
 		final ModelElementId internalId = ((ESModelElementIdImpl) modelElementId).toInternalAPI();
 		return RunESCommand.runWithResult(new Callable<EObject>() {
 			public EObject call() throws Exception {
 				return toInternalAPI().getProject().get(internalId);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getModelElementId(org.eclipse.emf.ecore.EObject)
 	 */
 	public ESModelElementIdImpl getModelElementId(EObject modelElement) {
 		final ModelElementId modelElementId = toInternalAPI().getProject().getModelElementId(modelElement);
 
 		if (modelElementId == null) {
 			return null;
 		}
 
 		return modelElementId.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getModelElements()
 	 */
 	public EList<EObject> getModelElements() {
 		return RunESCommand.runWithResult(new Callable<EList<EObject>>() {
 			public EList<EObject> call() throws Exception {
 				return toInternalAPI().getProject().getModelElements();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getAllModelElements()
 	 */
 	public Set<EObject> getAllModelElements() {
 		return RunESCommand.runWithResult(new Callable<Set<EObject>>() {
 			public Set<EObject> call() throws Exception {
 				return toInternalAPI().getProject().getAllModelElements();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getAllModelElementsByClass(java.lang.Class,
 	 *      java.lang.Boolean)
 	 */
 	public <T extends EObject> Set<T> getAllModelElementsByClass(final Class<T> modelElementClass,
 		final Boolean includeSubclasses) {
 		return RunESCommand.runWithResult(new Callable<Set<T>>() {
 			public Set<T> call() throws Exception {
 				return toInternalAPI().getProject().getAllModelElementsByClass(modelElementClass, includeSubclasses);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#getAllModelElementsByClass(java.lang.Class)
 	 */
 	public <T extends EObject> Set<T> getAllModelElementsByClass(final Class<T> modelElementClass) {
 		return RunESCommand.runWithResult(new Callable<Set<T>>() {
 			public Set<T> call() throws Exception {
 				return toInternalAPI().getProject().getAllModelElementsByClass(modelElementClass);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#contains(java.lang.Object)
 	 */
 	public boolean contains(ESModelElementId modelElementId) {
 		final ModelElementId id = ((ESModelElementIdImpl) modelElementId).toInternalAPI();
 		return toInternalAPI().getProject().contains(id);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESObjectContainer#contains(org.eclipse.emf.ecore.EObject)
 	 */
 	public boolean contains(EObject modelElement) {
 		return toInternalAPI().getProject().contains(modelElement);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#commit(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec commit(final IProgressMonitor monitor) throws ESException {
 		final PrimaryVersionSpec versionSpec = RunESCommand.WithException.runWithResult(ESException.class,
 			new Callable<PrimaryVersionSpec>() {
 				public PrimaryVersionSpec call()
 					throws Exception {
 					return toInternalAPI()
 						.commit(monitor);
 				}
 			});
 		return versionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#commit(java.lang.String,
 	 *      org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec commit(final String logMessage, final ESCommitCallback callback,
 		final IProgressMonitor monitor) throws ESUpdateRequiredException, ESException {
 
 		checkIsShared();
 
 		final PrimaryVersionSpec versionSpec = RunESCommand.WithException.runWithResult(ESException.class,
 			new Callable<PrimaryVersionSpec>() {
 
 				public PrimaryVersionSpec call()
 					throws Exception {
 					return toInternalAPI()
 						.commit(
 							logMessage,
 							callback,
 							monitor);
 				}
 			});
 
 		return versionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#commitToBranch(org.eclipse.emf.emfstore.server.model.versionspec.ESBranchVersionSpec,
 	 *      java.lang.String, org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec commitToBranch(final ESBranchVersionSpec branch, final String logMessage,
 		final ESCommitCallback callback, final IProgressMonitor monitor) throws InvalidVersionSpecException,
 		ESUpdateRequiredException, ESException {
 
 		final PrimaryVersionSpec versionSpec = RunESCommand.WithException.runWithResult(ESException.class,
 			new Callable<PrimaryVersionSpec>() {
 
 				public PrimaryVersionSpec call()
 					throws Exception {
 					BranchVersionSpec versionSpec = null;
 					if (branch != null) {
 						versionSpec = ((ESBranchVersionSpecImpl) branch)
 							.toInternalAPI();
 					}
 
 					return toInternalAPI()
 						.commitToBranch(
 							versionSpec,
 							logMessage,
 							callback,
 							monitor);
 				}
 			});
 
 		return versionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#update(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec update(final IProgressMonitor monitor) throws ChangeConflictException, ESException {
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESPrimaryVersionSpec>() {
 			public ESPrimaryVersionSpec call() throws Exception {
 				final PrimaryVersionSpec versionSpec = toInternalAPI().update(monitor);
 				return versionSpec.toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#update(org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec,
 	 *      org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec update(ESVersionSpec versionSpec, final ESUpdateCallback callback,
 		final IProgressMonitor monitor)
 		throws ChangeConflictException, ESException {
 
 		final VersionSpec version;
 
 		if (versionSpec == null) {
 			version = null;
 		} else {
 			final ESVersionSpecImpl<?, ? extends VersionSpec> versionSpecImpl = (ESVersionSpecImpl<?, ?>) versionSpec;
 			version = versionSpecImpl.toInternalAPI();
 		}
 
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESPrimaryVersionSpec>() {
 
 			public ESPrimaryVersionSpec call() throws Exception {
 				final PrimaryVersionSpec primaryVersionSpec = toInternalAPI().update(
 					version,
 					callback,
 					monitor);
 				return primaryVersionSpec.toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#merge(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.client.ESChangeConflict,
 	 *      org.eclipse.emf.emfstore.client.changetracking.merging.ConflictResolver,
 	 *      org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	// public boolean merge(ESPrimaryVersionSpec target, final ESChangeConflict changeConflict,
 	// final ESConflictResolver conflictResolver, final ESUpdateCallback callback, final IProgressMonitor monitor)
 	// throws ESException {
 	//
 	// final ESPrimaryVersionSpecImpl primaryVersionSpecImpl = (ESPrimaryVersionSpecImpl) target;
 	//
 	// return RunESCommand.WithException.runWithResult(ESException.class, new Callable<Boolean>() {
 	// public Boolean call() throws Exception {
 	// return toInternalAPI().merge(
 	// primaryVersionSpecImpl.toInternalAPI(),
 	// changeConflict,
 	// conflictResolver,
 	// callback,
 	// monitor);
 	// }
 	// });
 	// }
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#mergeBranch(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.client.changetracking.merging.ConflictResolver,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	// public void mergeBranch(ESPrimaryVersionSpec branchSpec, final ESConflictResolver conflictResolver,
 	// final IProgressMonitor monitor) throws ESException {
 	//
 	// final ESPrimaryVersionSpecImpl primaryVersionSpecImpl = (ESPrimaryVersionSpecImpl) branchSpec;
 	//
 	// RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 	// public Void call() throws Exception {
 	// toInternalAPI().mergeBranch(
 	// primaryVersionSpecImpl.toInternalAPI(),
 	// conflictResolver,
 	// monitor);
 	// return null;
 	// }
 	// });
 	// }
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#addToWorkspace(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void addToWorkspace(final IProgressMonitor progressMonitor) {
 		RunESCommand.run(new Callable<Void>() {
 
 			public Void call() throws Exception {
 				final ProjectSpaceBase projectSpace = (ProjectSpaceBase) ESLocalProjectImpl.this.toInternalAPI();
 
 				final WorkspaceBase workspace = (WorkspaceBase) ESWorkspaceProviderImpl.getInstance()
 					.getInternalWorkspace();
 				projectSpace.initResources(workspace.getResourceSet());
 				workspace.addProjectSpace(projectSpace);
 				workspace.save();
 
 				ESWorkspaceProviderImpl.getObserverBus().register(projectSpace);
 
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#shareProject(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void shareProject(final IProgressMonitor monitor) throws ESException {
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().shareProject(monitor);
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#shareProject(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESRemoteProject shareProject(final ESUsersession session, final IProgressMonitor monitor) throws ESException {
 
 		final ESUsersessionImpl usersessionImpl = (ESUsersessionImpl) session;
 
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESRemoteProject>() {
 			public ESRemoteProject call() throws Exception {
 				final ProjectInfo projectInfo = toInternalAPI().shareProject(
 					usersessionImpl != null ? usersessionImpl.toInternalAPI() : null,
 					monitor);
 				return new ESRemoteProjectImpl(getUsersession().toInternalAPI().getServerInfo(), projectInfo);
 
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#isShared()
 	 */
 	public boolean isShared() {
 		return toInternalAPI().isShared();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getUsersession()
 	 */
 	public ESUsersessionImpl getUsersession() {
 		return RunESCommand.runWithResult(new Callable<ESUsersessionImpl>() {
 			public ESUsersessionImpl call() throws Exception {
 				if (toInternalAPI().getUsersession() == null) {
 					return null;
 				}
 				return toInternalAPI().getUsersession().toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getBaseVersion()
 	 */
 	public ESPrimaryVersionSpec getBaseVersion() {
 		checkIsShared();
 		return RunESCommand.runWithResult(new Callable<ESPrimaryVersionSpec>() {
 			public ESPrimaryVersionSpec call() throws Exception {
				return toInternalAPI().getBaseVersion().toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getLastUpdated()
 	 */
 	public Date getLastUpdated() {
 		checkIsShared();
 		return RunESCommand.runWithResult(new Callable<Date>() {
 			public Date call() throws Exception {
 				return toInternalAPI().getLastUpdated();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getRecentLogMessages()
 	 */
 	public List<String> getRecentLogMessages() {
 		checkIsShared();
 		return RunESCommand.runWithResult(new Callable<List<String>>() {
 			public List<String> call() throws Exception {
 				return toInternalAPI().getOldLogMessages();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#undoLastOperation()
 	 */
 	public void undoLastOperation() {
 		RunESCommand.run(new ESVoidCallable() {
 			@Override
 			public void run() {
 				toInternalAPI().undoLastOperation();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#undoLastOperations(int)
 	 */
 	public void undoLastOperations(final int nrOperations) {
 		RunESCommand.run(new ESVoidCallable() {
 			@Override
 			public void run() {
 				toInternalAPI().undoLastOperations(nrOperations);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#isUpdated()
 	 */
 	public boolean isUpdated() throws ESException {
 		checkIsShared();
 		return toInternalAPI().isUpdated();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#revert()
 	 */
 	public void revert() {
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().revert();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#save()
 	 */
 	public void save() {
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().save();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#hasUnsavedChanges()
 	 */
 	public boolean hasUnsavedChanges() {
 		return toInternalAPI().hasUnsavedChanges();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#hasUncommitedChanges()
 	 */
 	public boolean hasUncommitedChanges() {
 		return toInternalAPI().isDirty();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getRemoteProject()
 	 */
 	public ESRemoteProjectImpl getRemoteProject() throws ESException {
 		checkIsShared();
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESRemoteProjectImpl>() {
 			public ESRemoteProjectImpl call() throws Exception {
 				// TODO OTS only return if server is available
 				if (getUsersession() == null || getUsersession().getServer() == null) {
 					throw new ESException(Messages.ESLocalProjectImpl_No_Usersession_Found);
 				}
 
 				final ProjectInfo projectInfo = org.eclipse.emf.emfstore.internal.server.model.ModelFactory.eINSTANCE
 					.createProjectInfo();
 				projectInfo.setProjectId(ModelUtil.clone(toInternalAPI().getProjectId()));
 				projectInfo.setName(getProjectName());
 				projectInfo.setVersion(ModelUtil.clone(toInternalAPI().getBaseVersion()));
 
 				return new ESRemoteProjectImpl(getUsersession().toInternalAPI().getServerInfo(), projectInfo);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESLocalProject#getLocalProjectId()
 	 */
 	public ESLocalProjectId getLocalProjectId() {
 		return RunESCommand.runWithResult(new Callable<ESLocalProjectId>() {
 			public ESLocalProjectId call() throws Exception {
 				return new ESLocalProjectIdImpl(toInternalAPI().getIdentifier());
 			}
 		});
 	}
 
 	private void checkIsShared() {
 		if (!isShared()) {
 			throw new ESProjectNotSharedException();
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getHistoryInfos(org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESHistoryInfo> getHistoryInfos(final ESHistoryQuery<? extends ESHistoryQuery<?>> query,
 		final IProgressMonitor monitor) throws ESException {
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<List<ESHistoryInfo>>() {
 			public List<ESHistoryInfo> call() throws Exception {
 				return copy(getRemoteProject().getHistoryInfos(getUsersession(), query, monitor));
 			}
 		});
 	}
 
 }
