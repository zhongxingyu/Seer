 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index.sql;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkitExtension;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.index.sql.Container;
 import org.eclipse.dltk.core.index.sql.DbFactory;
 import org.eclipse.dltk.core.index.sql.File;
 import org.eclipse.dltk.core.index.sql.SqlIndex;
 import org.eclipse.dltk.core.index2.AbstractIndexer;
 import org.eclipse.dltk.core.index2.search.ISearchEngine;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Implementation of SQL-based indexer.
  * 
  * @author michael
  * 
  */
 public class SqlIndexer extends AbstractIndexer {
 
 	private Connection connection;
 	private File file;
 	private String natureId;
 
 	public void addDeclaration(DeclarationInfo info) {
 
 		try {
 			DbFactory.getInstance().getElementDao().insert(connection,
 					info.elementType, info.flags, info.offset, info.length,
 					info.nameOffset, info.nameLength, info.elementName,
 					info.metadata, info.qualifier, info.parent, file.getId(),
 					natureId, false);
 
 		} catch (SQLException e) {
 			SqlIndex
 					.error(
 							"An exception was thrown while inserting model element declaration",
 							e);
 		}
 	}
 
 	public void addReference(ReferenceInfo info) {
 
 		try {
 			DbFactory.getInstance().getElementDao().insert(connection,
 					info.elementType, 0, info.offset, info.length, 0, 0,
 					info.elementName, info.metadata, info.qualifier, null,
 					file.getId(), natureId, true);
 
 		} catch (SQLException e) {
 			SqlIndex
 					.error(
 							"An exception was thrown while inserting model element reference",
 							e);
 		}
 	}
 
 	public void indexDocument(ISourceModule sourceModule) {
 
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 
 		try {
 			DbFactory dbFactory = DbFactory.getInstance();
 			connection = dbFactory.createConnection();
 			try {
 				connection.setAutoCommit(false);
 
 				IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 						.getLanguageToolkit(sourceModule);
 				if (toolkit == null) {
 					return;
 				}
 
 				natureId = toolkit.getNatureId();
 
				IPath containerPath;
				if (sourceModule instanceof SourceModule) {
					containerPath = sourceModule.getScriptProject().getPath();
				} else {
					containerPath = sourceModule.getAncestor(
							IModelElement.PROJECT_FRAGMENT).getPath();
				}
 				Container container = dbFactory.getContainerDao().insert(
 						connection, containerPath.toString());
 
 				String relativePath;
 				if (toolkit instanceof IDLTKLanguageToolkitExtension
 						&& ((IDLTKLanguageToolkitExtension) toolkit)
 								.isArchiveFileName(sourceModule.getPath()
 										.toString())) {
 					relativePath = ((ExternalSourceModule) sourceModule)
 							.getFullPath().toString();
 				} else {
 					relativePath = Util.relativePath(sourceModule.getPath(),
 							containerPath.segmentCount());
 				}
 				IFileHandle handle = EnvironmentPathUtils.getFile(sourceModule);
 
 				File existing = dbFactory.getFileDao().select(connection,
 						relativePath, container.getId());
 				if (existing != null) {
 					if (existing.getTimestamp() == handle.lastModified()) {
 						// File is not updated - nothing to do
 						return;
 					}
 					// Re-index:
 					dbFactory.getFileDao().deleteById(connection,
 							existing.getId());
 				}
 				long lastModifyed;
 				if (handle != null) {
 					lastModifyed = handle.lastModified();
 				} else {
 					lastModifyed = 0;
 				}
 				file = dbFactory.getFileDao().insert(connection, relativePath,
 						lastModifyed, container.getId());
 
 				super.indexDocument(sourceModule);
 
 			} finally {
 				connection.commit();
 				connection.close();
 
 				p.done(natureId, "SQL Index Document", sourceModule
 						.getSourceRange().getLength());
 			}
 		} catch (Exception e) {
 			SqlIndex
 					.error("An exception was thrown while indexing document", e);
 		}
 	}
 
 	public Map<String, Long> getDocuments(IPath containerPath) {
 		try {
 			DbFactory dbFactory = DbFactory.getInstance();
 			Connection connection = dbFactory.createConnection();
 			try {
 				Container containerDao = dbFactory.getContainerDao()
 						.selectByPath(connection, containerPath.toString());
 				if (containerDao != null) {
 
 					File[] files = dbFactory.getFileDao().selectByContainerId(
 							connection, containerDao.getId());
 					Map<String, Long> paths = new HashMap<String, Long>();
 					for (File fileDao : files) {
 						paths.put(fileDao.getPath(), fileDao.getTimestamp());
 					}
 					return paths;
 				}
 			} finally {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			SqlIndex
 					.error(
 							"An exception thrown while analyzing source module changes",
 							e);
 		}
 		return null;
 	}
 
 	public void removeContainer(IPath containerPath) {
 		try {
 			DbFactory dbFactory = DbFactory.getInstance();
 			Connection connection = dbFactory.createConnection();
 			try {
 				dbFactory.getContainerDao().deleteByPath(connection,
 						containerPath.toString());
 			} finally {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			SqlIndex
 					.error(
 							NLS
 									.bind(
 											"An exception thrown while removing container ''{0}'' from index",
 											containerPath.toString()), e);
 		}
 	}
 
 	public void removeDocument(IPath containerPath, String relativePath) {
 		try {
 			DbFactory dbFactory = DbFactory.getInstance();
 			Connection connection = dbFactory.createConnection();
 			try {
 				Container containerDao = dbFactory.getContainerDao()
 						.selectByPath(connection, containerPath.toString());
 				if (containerDao != null) {
 					dbFactory.getFileDao().delete(connection, relativePath,
 							containerDao.getId());
 				}
 			} finally {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			SqlIndex
 					.error(
 							NLS
 									.bind(
 											"An exception thrown while removing file ''{0}'' from index",
 											containerPath.append(relativePath)
 													.toString()), e);
 		}
 	}
 
 	public ISearchEngine createSearchEngine() {
 		return new SqlSearchEngine();
 	}
 }
