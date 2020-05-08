 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: 
  * 	Martin Taal - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
 import org.eclipse.emf.cdo.common.commit.CDOCommitInfoHandler;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.common.model.CDOClassInfo;
 import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
 import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
 import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
 import org.eclipse.emf.cdo.session.CDOSession;
 import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.osgi.framework.BundleActivator;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.common.properties.IPropertiesProvider;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.generics.ActionType;
 import com.netxforge.netxstudio.generics.CommitLogEntry;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.server.internal.ServerActivator;
 
 /**
  * Implements the CDO {@link CDOCommitInfoHandler} which stores commit info in
  * the database using {@link CommitLogEntry}.
  * 
  * @author Martin Taal
  * @author Christophe Bouhier
  */
 public class CommitInfoHandler implements CDOCommitInfoHandler {
 
 	@Inject
 	ModelUtils modelUtils;
 
 	/*
 	 * The maximum number of entries in the commit log.
 	 */
 	private int maxCommitEntries = -1;
 
 	public static final int NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY_DEFAULT = 100; // how
 																			// many
 																			// days
 																			// to
 																			// keep
 																			// commit
 																			// info
 																			// for
 																			// a
 																			// user.
 	public static final String NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY = "netxstudio.max.commit.info.quantity"; // how
 																									// many
 	public synchronized void handleCommitInfo(CDOCommitInfo commitInfo) {
 		// don't do this when the server is initializing
 		if (ServerUtils.getInstance().isInitializing()) {
 			return;
 		}
 
 		// don't save commit info
 		// check if we are saving the commit info resource
 		// if so bail
 		for (final CDOIDAndVersion cdoIdAndVersion : commitInfo.getNewObjects()) {
 			if (cdoIdAndVersion instanceof InternalCDORevision) {
 				if (((InternalCDORevision) cdoIdAndVersion).getEClass() == GenericsPackage.eINSTANCE
 						.getCommitLogEntry()) {
 					return;
 				}
 			}
 		}
 
 		if (commitInfo.getComment() == null) {
 			if (ServerActivator.DEBUG) {
 				ServerActivator.TRACE.trace(
 						ServerActivator.TRACE_SERVER_COMMIT_INFO_CDO_OPTION,
 						"transaction source unknown (comment not set) for transaction for commit="
 								+ commitInfo.toString());
 			}
 			return;
 		}
 
 		// skip server side committing.
 		if (commitInfo.getComment() != null
 				&& commitInfo.getComment().equals(
 						IDataProvider.SERVER_COMMIT_COMMENT)) {
 			// do not log server side handling.
 			return;
 		}
 
 		final XMLGregorianCalendar commitTimeStamp = modelUtils
 				.toXMLDate(new Date(commitInfo.getTimeStamp()));
 
 		// Do not use our dataprovider.
 		final CDOSession session = ServerUtils.getInstance().openJVMSession();
 		final CDOTransaction transaction = session.openTransaction();
 
 		final Resource resource = transaction
 				.getOrCreateResource("/CDOCommitInfo_" + commitInfo.getUserID());
 
 		// DELETE
 		for (final CDOIDAndVersion cdoIdAndVersion : commitInfo
 				.getDetachedObjects()) {
 			final CommitLogEntry logEntry = GenericsFactory.eINSTANCE
 					.createCommitLogEntry();
 			logEntry.setUser(commitInfo.getUserID());
 			logEntry.setTimeStamp(commitTimeStamp);
 			logEntry.setAction(ActionType.DELETE);
 			logEntry.setObjectId(cdoIdAndVersion.toString());
 			addAndTruncate(resource.getContents(), logEntry);
 		}
 
 		// ADD
 		for (final CDOIDAndVersion cdoIdAndVersion : commitInfo.getNewObjects()) {
 			final CommitLogEntry logEntry = GenericsFactory.eINSTANCE
 					.createCommitLogEntry();
 			logEntry.setUser(commitInfo.getUserID());
 			logEntry.setTimeStamp(commitTimeStamp);
 			logEntry.setAction(ActionType.ADD);
 
 			if (cdoIdAndVersion instanceof InternalCDORevision) {
 				InternalCDORevision icdoRev = (InternalCDORevision) cdoIdAndVersion;
 				CDOClassInfo classInfo = icdoRev.getClassInfo();
 				logEntry.setObjectId(classInfo.getEClass().getName() + " "
 						+ icdoRev.getID() + "" + icdoRev.getVersion());
 				logEntry.setChange(modelUtils.cdoDumpNewObject(icdoRev));
 			}
 			addAndTruncate(resource.getContents(), logEntry);
 		}
 
 		// UPDATE
 		for (final CDORevisionKey key : commitInfo.getChangedObjects()) {
 			final CDORevisionDelta delta = (CDORevisionDelta) key;
 
 			final CommitLogEntry logEntry = GenericsFactory.eINSTANCE
 					.createCommitLogEntry();
 			logEntry.setUser(commitInfo.getUserID());
 			logEntry.setTimeStamp(commitTimeStamp);
 			logEntry.setAction(ActionType.UPDATE);
 
 			CDOID id = delta.getID();
 
 			logEntry.setObjectId(modelUtils.truncate(id.toString()));
 
 			final StringBuilder sb = new StringBuilder();
 			modelUtils.cdoDumpFeatureDeltas(sb, delta.getFeatureDeltas());
 
 			logEntry.setChange(modelUtils.truncate(sb.toString()));
 			addAndTruncate(resource.getContents(), logEntry);
 		}
 		try {
 			transaction
 					.setCommitComment(IDataProvider.COMMITINFO_COMMIT_COMMENT);
 			transaction.commit();
 			transaction.close();
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		} finally {
 			session.close();
 		}
 	}
 
 	/**
 	 * @param resource
 	 * @param logEntry
 	 */
 	public void addAndTruncate(final EList<EObject> contents,
 			CommitLogEntry commitInfo) {
 
 		// Lazy init maxStats var.
 		if (maxCommitEntries == -1) {
 			boolean storeMaxCommits = false;
 			BundleActivator a = ServerActivator.getInstance();
 			if (a instanceof IPropertiesProvider) {
 				String property = ((IPropertiesProvider) a).getProperties()
 						.getProperty(NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY);
 				
 				if (property == null) {
 					maxCommitEntries = new Integer(
 							NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY_DEFAULT);
 					storeMaxCommits = true;
 				} else {
 					if (ServerActivator.DEBUG) {
 						ServerActivator.TRACE.trace(
 								ServerActivator.TRACE_SERVER_COMMIT_INFO_CDO_OPTION,
 								"found property: " + NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY);
 					}
 					try {
 						maxCommitEntries = new Integer(property);
 					} catch (NumberFormatException nfe) {
 
 						if (ServerActivator.DEBUG) {
 							ServerActivator.TRACE.trace(
 									ServerActivator.TRACE_SERVER_COMMIT_INFO_CDO_OPTION,
 									"Error reading property", nfe);
 						}
 
 						maxCommitEntries = new Integer(
 								NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY_DEFAULT);
 						storeMaxCommits = true;
 					}
 				}
 			}
 
 			if (storeMaxCommits) {
 				// Should be saved when the Activator stops!
 				((IPropertiesProvider) a).getProperties().setProperty(
 						NETXSTUDIO_MAX_COMMIT_INFO_QUANTITY,
 						new Integer(maxCommitEntries).toString());
 			}
 		}
 
 		contents.add(0, commitInfo);
 
 		// truncate the list, if exceeding max. size.
 		if (contents.size() > maxCommitEntries) {
 
 			List<EObject> subList = Lists.newArrayList(contents
 					.subList(0, maxCommitEntries));
 			boolean retainAll = contents.retainAll(subList);
 
 			if (retainAll) {
 				if (ServerActivator.DEBUG) {
 					ServerActivator.TRACE.trace(
 							ServerActivator.TRACE_SERVER_COMMIT_INFO_CDO_OPTION,
							"truncing commit info to max "
 									+ maxCommitEntries);
 				}
 			}
 		}
 	}
 
 }
