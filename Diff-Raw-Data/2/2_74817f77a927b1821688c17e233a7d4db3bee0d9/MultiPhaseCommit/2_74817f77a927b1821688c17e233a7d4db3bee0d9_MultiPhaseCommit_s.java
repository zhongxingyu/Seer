 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.sokan.index.indexer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.reuseware.sokan.FacetedRequest;
 import org.reuseware.sokan.ID;
 import org.reuseware.sokan.IndexMetaData;
 import org.reuseware.sokan.IndexRow;
 import org.reuseware.sokan.IndexTransaction;
 import org.reuseware.sokan.SokanFactory;
 import org.reuseware.sokan.index.CommitCache;
 import org.reuseware.sokan.index.IndexCache;
 import org.reuseware.sokan.index.SokanIndexPlugin;
 import org.reuseware.sokan.index.persister.PersistencyManager;
 import org.reuseware.sokan.index.util.CoreUtil;
 import org.reuseware.sokan.index.util.FacetUtil;
 import org.reuseware.sokan.index.util.IndexUtil;
 import org.reuseware.sokan.index.util.ResourceUtil;
 
 /**
  * The multi-phase commit controls the index call cycle and commits
  * index updates after each phase of index calls.
  */
 public class MultiPhaseCommit {
 	
 	private static final int MAX_GEN_COUNT = 10;
 	
 	private DependencyManager dependencyManager;
 	private MetaDataManager metaDataManager;
 	private PersistencyManager persistencyManager;
 	private IndexerManager indexerManager;
 	private String commitTimeStamp = IndexCache.INIT_TIME;
 
 	/**
 	 * Construct a new multi-phase commit that uses the given managers.
 	 * 
 	 * @param dependencyManager  the dependencyManager
 	 * @param metaDataManager    the metaDataManager
 	 * @param persistencyManager the persistencyManager
 	 * @param indexerManager     the indexerManager
 	 */
 	public MultiPhaseCommit(DependencyManager dependencyManager,
 			MetaDataManager metaDataManager, 
 			PersistencyManager persistencyManager, 
 			IndexerManager indexerManager) {
 
 		this.dependencyManager = dependencyManager;
 		this.metaDataManager = metaDataManager;
 		this.persistencyManager = persistencyManager;
 		this.indexerManager = indexerManager;
 	}
 
 	/**
 	 * @return time stamp of last commit
 	 */
 	public String getCommitTimeStamp() {
 		return commitTimeStamp;
 	}
 
 	/**
 	 * Starts a multi-phase commit with the given commit cache.
 	 * 
 	 * @param cache the   commit cache holding information about modified artifacts
 	 * @param resourceSet the resource set to pass to the indexers for resource 
 	 *                    loading and creation
 	 * @param callCount   counter for recursive calls
 	 * @param monitor     a monitor to record progress of this long running operation
 	 * @return IDs of all modified index rows
 	 */
 	public Set<ID> start(CommitCache cache,
 			ResourceSet resourceSet,
 			int callCount, IProgressMonitor monitor) {
 		
 		if (callCount == MAX_GEN_COUNT) {
 			//break if generated resources repeatedly register new resources
 			return Collections.emptySet();
 		}
 		
 		if (cache.isEmpty()) {
 			return Collections.emptySet();
 		}
 		
 		Set<ID> delta = new LinkedHashSet<ID>();
 		Set<Resource> toSave = new LinkedHashSet<Resource>();
 		
 		List<Indexer> allNormalIndexers = 
 			indexerManager.getIndexers(null, false);
 		List<List<Indexer>> allIndexersByPhase = 
 			indexerManager.sortByPhases(indexerManager.getIndexers(null, true));
 		
 		expandCache(cache, allNormalIndexers);
 		
 		Set<URI> toDelete = new LinkedHashSet<URI>(cache.getDeletedResources());
 		Set<URI> userDeleted = new LinkedHashSet<URI>(cache.getDeletedResources());
 		
 		int restart = 0;
 		while (!cache.isEmpty() && restart < MAX_GEN_COUNT) {
 			for (List<Indexer> currentPhaseIndexers : allIndexersByPhase) {
 				// getDependent - before
 				caculateDependencies(currentPhaseIndexers, cache);
 	
 				IndexTransaction trans = invokeIndexers(cache, currentPhaseIndexers, userDeleted, resourceSet, monitor);
 				if (!commit(trans)) {
 					return null;
 				}
 				
 				// getDependent - after
 				caculateDependencies(currentPhaseIndexers, cache);
 				
 				updateDelta(delta, cache);
 				
 				// remove processed indexers
 				cache.getNewResources().clear();
 				cache.getUpdatedResources().clear();
 				cache.getDeletedResources().clear();
 				for (List<Indexer> anArtifactsIndexerList : cache.getUpdateMap().values()) {
 					Iterator<Indexer> i = anArtifactsIndexerList.iterator();
 					while (i.hasNext()) {
 						Indexer next = i.next();
 						if (currentPhaseIndexers.contains(next)) {
 							i.remove();
 						}
 					}	
 				}
 				
 				//mark generated resources as updated
 				List<Resource> updatedResources = updateCacheWithGenerated(resourceSet, cache);
 				if (!updatedResources.isEmpty()) {
 					toSave.addAll(updatedResources);
 					toDelete.addAll(cache.getDeletedResources());
 					toDelete.removeAll(cache.getNewResources());
 					updateDelta(delta, cache);
 					expandCache(cache, allNormalIndexers);
 					//restart with new
 					restart++;
 					break;
 				}
 				
 				List<Indexer> remainingIndexer = new ArrayList<Indexer>();
 				for (List<Indexer> phaseIndexerList : allIndexersByPhase.subList(
 						allIndexersByPhase.indexOf(currentPhaseIndexers) + 1, allIndexersByPhase.size())) {
 					remainingIndexer.addAll(phaseIndexerList);
 				}
 				if (requireIndexersFromEarlierPhase(cache, remainingIndexer)) {
 					restart++;
 					break;
 				}
 			}
 		}
 		
 		if (restart ==  MAX_GEN_COUNT) {
 			List<ID> involved = new ArrayList<ID>();
 			for (ID id : cache.getUpdateMap().keySet()) {
 				if (!cache.getUpdateMap().get(id).isEmpty()) {
 					involved.add(id);
 				}
 			}			
			SokanIndexPlugin.logError("Possible cyclic dependency (involved artivatcts: " + involved + ")", null);
 		}
 		
 		// remove deleted
 		if (!toDelete.isEmpty()) {
 			// handle deleted: calculate dependencies and remove
 			IndexTransaction trans = buildRemoveTransaction(toDelete);
 			// commit: remove deleted
 			if (!commit(trans)) {
 				return null;
 			}
 		}
 
 		boolean wasEmpty = cache.isEmpty();
 		saveGenerated(toSave, resourceSet);
 		if (wasEmpty && !cache.isEmpty()) {
 			//are generated resources allowed to register new resources in index?
 			//modelquery currently does so....
 			Set<ID> extraDelta = start(cache, resourceSet, callCount++, monitor);
 			delta.addAll(extraDelta);
 		}
 		
 		return delta;
 	}
 
 	private void expandCache(CommitCache cache,
 			List<Indexer> allIndexerList) {
 		for (ID idForCompleteUpdate : cache.extractDeletedIDs()) {
 			List<Indexer> indexerList = cache.getUpdateMap().get(idForCompleteUpdate);
 			if (indexerList == null) {
 				indexerList = new ArrayList<Indexer>();
 				cache.getUpdateMap().put(idForCompleteUpdate, indexerList);
 			}
 			for (Indexer indexer : allIndexerList) {
 				if (!indexerList.contains(indexer)) {
 					indexerList.add(indexer);
 				}
 			}
 		}
 		for (ID idForCompleteUpdate : cache.extractUpdAndNewIDs()) {
 			List<Indexer> indexerList = cache.getUpdateMap().get(idForCompleteUpdate);
 			if (indexerList == null) {
 				indexerList = new ArrayList<Indexer>();
 				cache.getUpdateMap().put(idForCompleteUpdate, indexerList);
 			}
 			for (Indexer indexer : allIndexerList) {
 				if (!indexerList.contains(indexer)) {
 					indexerList.add(indexer);
 				}
 			}
 		}
 	}
 	
 	private boolean requireIndexersFromEarlierPhase(CommitCache cache,
 			List<Indexer> remainingIndexer) {
 		for (List<Indexer> neededIndexers : cache.getUpdateMap().values()) {
 			for (Indexer neededIndexer : neededIndexers) {
 				if (!remainingIndexer.contains(neededIndexer)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private void updateDelta(Set<ID> delta, CommitCache cache) {
 		Set<ID> iDs = null;
 		
 		iDs = cache.extractDeletedIDs();
 		delta.addAll(iDs);
 		
 		iDs = cache.extractUpdatedIDs();
 		delta.addAll(iDs);
 
 		for (URI uri : cache.getNewResources()) {
 			ID id = ResourceUtil.idFrom(uri);
 			if (id != null) {
 				delta.add(id);
 			} 
 		}
 	}
 	
 	private List<Resource> updateCacheWithGenerated(ResourceSet rSet, CommitCache cache) {
 		List<Resource> updatedResource = new ArrayList<Resource>();
 		for (Resource res : new ArrayList<Resource>(rSet.getResources())) {
 			if (res.isModified()) {
 				URI uri = res.getURI();
 				res.setModified(false);
 				if (!res.getContents().isEmpty()) {			
 					cache.getNewResources().add(uri);
 					cache.getDeletedResources().remove(uri);
 					// remember that this resource was generated by an indexer
 					cache.getGeneratedResources().add(uri);
 				} else {
 					cache.getDeletedResources().add(uri);
 				}
 				updatedResource.add(res);
 			}
 		}
 		return updatedResource;
 	}
 	
 	private void saveGenerated(Set<Resource> toSave, ResourceSet rSet) {
 		for (Resource res : toSave) {
 			try {
 				res.save(rSet.getLoadOptions());
 				if (res.getContents().isEmpty()) {
 					res.delete(rSet.getLoadOptions());
 				}
 			} catch (Exception e) {
 				if (e.getMessage().equals("The resource tree is locked for modifications.")) {
 					//ignore: happens if the project is already closed
 				} else {
 					SokanIndexPlugin.logError("Error saving generated resource " + res.getURI(), e);
 				}
 			}
 			if (res instanceof Validatable) {
 				((Validatable) res).validate();
 			}
 		}
 	}
 
 	private boolean commit(IndexTransaction trans) {
 		if (persistencyManager.commit(trans)) {
 			commitTimeStamp = CoreUtil.now();
 			return true;
 		}
 
 		SokanIndexPlugin.logError("ERROR! Index commit failed!", null);
 		commitTimeStamp = null;
 		return false;
 	}
 
 	private void caculateDependencies(List<Indexer> indexers, CommitCache cache) {
 		dependencyManager.calculateDependenciesOfUpdatedArtifacts(
 				indexers, cache);
 	}
 
 	private IndexTransaction invokeIndexers(CommitCache cache,
 			List<Indexer> indexerList, Set<URI> deleted, ResourceSet rSet, IProgressMonitor monitor) {
 
 		List<IndexRow> updatedRows = new ArrayList<IndexRow>();
 		
 		for (ID artifactID : cache.getUpdateMap().keySet()) {
 			IndexRow oldRow = IndexUtil.INSTANCE.getIndex(artifactID);
 			URI updatedURI = null;
 			if (oldRow == null) {
 				// artifact is not indexed -> must be new
 				for (URI addedURI : cache.getNewResources()) {
 					ID newID = ResourceUtil.idFrom(addedURI);
 					if (newID.equals(artifactID)) {
 						updatedURI = addedURI;
 						break;
 					}
 				}
 			} else {
 				updatedURI = ResourceUtil.uriFrom(oldRow.getPhyURI());
 			}
 			if (updatedURI == null) {
 				continue;
 			}
 
 			IndexMetaData oldData = oldRow == null ? null : oldRow.getMetaData();
 			List<Indexer> indexersToCall = new ArrayList<Indexer>();
 			Iterator<Indexer> i = cache.getUpdateMap().get(artifactID).iterator();
 			while (i.hasNext()) {
 				Indexer next = i.next();
 				if (indexerList.contains(next)) {
 					if (!deleted.contains(updatedURI) || next instanceof DependencyIndexer) {
 						indexersToCall.add(next);
 					}
 				}
 			}
 			boolean generated = cache.getGeneratedResources().contains(updatedURI);
 			IndexRow newRow = metaDataManager.createIndexRow(updatedURI, generated, indexersToCall, oldData, rSet, monitor);
 			if (newRow == null) {
 				SokanIndexPlugin.logError(
 						"Was not able to create IndexRow for: " + artifactID.getSegments(),
 						null);
 				continue;			
 			}
 			if (newRow != null) {
 				updatedRows.add(newRow);
 			}
 		}
 
 		IndexTransaction trans = SokanFactory.eINSTANCE.createIndexTransaction();
 
 		trans.getUpdateArtifacts().addAll(updatedRows);
 
 		return trans;
 	}
 
 
 	private IndexTransaction buildRemoveTransaction(Set<URI> remResources) {
 		if (remResources == null || remResources.isEmpty()) {
 			return null;
 		}
 
 		IndexTransaction trans = SokanFactory.eINSTANCE.createIndexTransaction();
 		for (URI uri : remResources) {
 			FacetedRequest request = FacetUtil.buildFacetedRequest(
 					"phyURI", new String[] {uri.toString()});
 			for (IndexRow row : IndexUtil.INSTANCE.getIndex(request)) {
 				trans.getRemArtifacts().add(row.getArtifactID());
 			}
 		}
 
 		return trans;
 	}
 
 
 }
