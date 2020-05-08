 package net.cyklotron.cms.search.internal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.store.Directory;
 import org.jcontainer.dna.Logger;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.filesystem.FileSystem;
 
 import net.cyklotron.cms.ProtectedResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.IndexableResource;
 import net.cyklotron.cms.search.IndexingFacility;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchUtil;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * Implementation of Indexing
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: IndexingFacilityImpl.java,v 1.14 2007-11-18 21:23:23 rafal Exp $
  */
 public class IndexingFacilityImpl implements IndexingFacility 
 {
     /** logging facility */
     private Logger log;
 
     /** search service - for managing index resources */
     private SearchService searchService;
     
    private Context context;

     // local ---------------------------------------------------------------------------------------
 
     private IndexingFacilityUtil utility;
     private IndexingResourceChangesListener indexingListener;
     
     /** Lucene document construction facility. */
     private DocumentConstructor docConstructor;
     
     private Subject anonymousSubject;
    private CoralSessionFactory coralSessionFactory;
     private FileSystem fileSystem;
 
     /**
      * Creates the facility.
      * @param searchService
      * @param fileSystem
      */
     public IndexingFacilityImpl(Context context, CoralSessionFactory sessionFactory,
         Logger logger, SearchService searchService, FileSystem fileSystem,
         PreferencesService preferencesService, CategoryService categoryService,CategoryQueryService categoryQueryService,
         UserManager userManager, IntegrationService integrationService)
     {
        this.context = context;
         this.searchService = searchService;
         this.log = logger;
         this.fileSystem = fileSystem;
        this.coralSessionFactory = sessionFactory;

         docConstructor = new DocumentConstructor(context, logger, preferencesService, userManager,
             categoryService, integrationService);
         
         utility = new IndexingFacilityUtil(searchService, categoryQueryService, fileSystem,
             searchService.getConfiguration().getChild(BASE_DIRECTORY).getValue(DEFAULT_BASE_DIRECTORY),
             searchService.getConfiguration().getChild("mergeFactor").getValueAsInteger(20),
             searchService.getConfiguration().getChild("minMergeDocs").getValueAsInteger(100),
             searchService.getConfiguration().getChild("maxMergeDocs").getValueAsInteger(5000));
 
         CoralSession anonSession = sessionFactory.getAnonymousSession();
         try
         {
             anonymousSubject = anonSession.getUserSubject();
         }
         finally
         {
             anonSession.close();
         }
         
         // register listeners
         indexingListener = new IndexingResourceChangesListener(
             log, searchService, this, sessionFactory);
    }
 
     // IndexingFacility methods --------------------------------------------------------------------
 
     /**
      * @{inheritDoc}
      */
     public String getIndexFilesPath(SiteResource site, String indexName)
         throws SearchException
     {
         return utility.getIndexFilesPath(site, indexName);
     }
 
     /**
      * @{inheritDoc}
      */
     public void createIndexFiles(IndexResource index) throws SearchException
     {
         utility.createIndexFiles(index);
     }
 
     /**
      * @{inheritDoc}
      */
     public Directory getIndexDirectory(IndexResource index) throws SearchException
     {
         return utility.getIndexDirectory(index);
     }
 
     /**
      * @{inheritDoc}
      */
     public void reindex(CoralSession coralSession, IndexResource index) throws SearchException
     {
         String oldDirectoryPath = index.getFilesLocation();
         String tempDirectoryPath = oldDirectoryPath + "_" + System.nanoTime();
         utility.checkDirectory(tempDirectoryPath);
         Directory tempDir = new LedgeFSDirectory(fileSystem, tempDirectoryPath);
         IndexWriter indexWriter = 
             utility.openIndexWriter(tempDir, index, true, "reindexing the index");
         Set<Resource> querySet = utility.getQueryIndexResourceIds(coralSession, index);
         try
         {
             ReindexStats stats = new ReindexStats();
             // go recursive on all branches
             List resources = searchService.getIndexedBranches(coralSession, index);
              
             for (Iterator i = resources.iterator(); i.hasNext();)
             {
                 Resource branch = (Resource) (i.next());
                 index(coralSession, branch, branch, indexWriter, index, querySet, true, stats);
             }
 
             // go locally on nodes
             resources = searchService.getIndexedNodes(coralSession, index);
             for (Iterator i = resources.iterator(); i.hasNext();)
             {
                 Resource branch = (Resource) (i.next());
                 index(coralSession, branch, branch, indexWriter, index, querySet, false, stats);
             }
             indexWriter.commit(IndexingFacilityUtil.resetChangeCounter());
             if(stats.documentsAdded == 0)
             {
                 StringBuilder buff = new StringBuilder();
                 buff.append(index.toString());
                 buff.append(" is misconfigured: checked ");
                 buff.append(stats.documentsChecked);
                 buff.append(" resources ");
                 if(querySet != null)
                 {
                     buff.append(" against ");
                     buff.append(querySet.size());
                     buff.append(" category query results ");
                 }
                 buff.append("and added 0 resources to index");
                 log.error(buff.toString());
             }
         }
         catch (IOException e)
         {
             throw new SearchException(
                 "IndexingFacility: Could not index branches or nodes for index '"+
                 index.getPath()+"' while reindexing the index", e);
         }
         catch (SearchException e)
         {
             throw new SearchException(
                 "IndexingFacility: Could not index branches or nodes for index '"+
                 index.getPath()+"' while reindexing the index", e);
         }
 
         finally
         {
             utility.closeIndexWriter(indexWriter, index, "reindexing the index");
         }
         
         // clear searcher cache
         searchService.getSearchingFacility().clearSearcher(index);
         
         String temp2DirectoryPath = tempDirectoryPath + "_2";
         try
         {
             synchronized(index)
             {
                 fileSystem.rename(oldDirectoryPath, temp2DirectoryPath);
                 fileSystem.rename(tempDirectoryPath, oldDirectoryPath);
                 searchService.getSearchingFacility().clearSearcher(index);
             }
             fileSystem.deleteRecursive(temp2DirectoryPath);
         }
         catch(IOException e)
         {
             throw new SearchException("failed to reindex resources", e);
         }
     }
 
     public void indexMissing(CoralSession coralSession, IndexResource index) throws SearchException
     {
         Set missingResources = 
             SearchUtil.getIndexableResources(coralSession, log, getMissingResourceIds(coralSession, index));
         IndexableResource[] res = (IndexableResource[]) 
             missingResources.toArray(new IndexableResource[missingResources.size()]);
         addToIndex(coralSession, index, res);
     }
 
     public void deleteDeleted(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         Set deletedResourcesIds = getDeletedResourcesIds(coralSession, index);
         long[] ids = new long[deletedResourcesIds.size()];
         int i = 0;
         for (Iterator iter = deletedResourcesIds.iterator(); iter.hasNext(); i++)
         {
             ids[i] = ((Long)iter.next()).longValue();
         }
         deleteFromIndex(index, ids);
     }
 
     public void reindexDuplicated(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         Set duplicateResourcesIds = getDuplicateResourceIds(index);
         long[] ids = new long[duplicateResourcesIds.size()];
         int i = 0;
         for (Iterator iter = duplicateResourcesIds.iterator(); iter.hasNext(); i++)
         {
             ids[i] = ((Long)iter.next()).longValue();
         }
         deleteFromIndex(index, ids);
         Set resources = SearchUtil.getIndexableResources(coralSession, log, duplicateResourcesIds);
         IndexableResource[] res = 
             (IndexableResource[]) resources.toArray(new IndexableResource[resources.size()]);
         addToIndex(coralSession, index, res); 
     }
     
     public void optimize(IndexResource index) throws SearchException
     {
         synchronized(index)
         {
             Directory dir = getIndexDirectory(index);
             IndexWriter indexWriter = 
                 utility.openIndexWriter(dir, index, false, "optimizing the index");
             try
             {
                 indexWriter.forceMerge(1);
                 indexWriter.commit(IndexingFacilityUtil.resetChangeCounter());
             }
             catch (IOException e)
             {
                 throw new SearchException("IndexingFacility: Problem optimising " +
                         "index '"+index.getPath()+"'", e);
             }
             finally
             {
                 utility.closeIndexWriter(indexWriter, index, "incrementally optimizing the index");
             }
         }
     }
     
     /**
     * Chcecks if a given resource may be put in the given index.
      * @param node resource to be checked
      * @param index index to be checked
      * @param querySet TODO
     * 
      * @return <code>true</code> if given resource may be indexed by a given index
     * @throws SearchException 
      */
     private boolean liableForIndexing(CoralSession coralSession, IndexableResource node,
         IndexResource index, Set<Resource> querySet)
         throws SearchException
     {
         if(querySet == null || querySet.contains(node))
         {
             if(!index.getPublic())
             {
                 return true;
             }
             if(node instanceof ProtectedResource)
             {
                 ProtectedResource protectedNode = (ProtectedResource)node;
                 return protectedNode.canView(coralSession, anonymousSubject, new Date());
             }
             return true;
         }
         return false;
     }
 
     // index info ----------------------------------------------------------------------------------
 
     public Set getIndexedResourceIds(IndexResource index)
         throws SearchException
     {
         return utility.getIndexedResourceIds(index);
     }
 
     public Set getMissingResourceIds(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         return utility.getMissingResourceIds(coralSession, index);
     }
     
     public Set getDeletedResourcesIds(CoralSession coralSession, IndexResource index)
     throws SearchException
     {
         return utility.getDeletedResourcesIds(coralSession, index);
     }
 
     public Set getDuplicateResourceIds(IndexResource index)
         throws SearchException
     {
         return utility.getDuplicateResourceIds(index);
     }
     
     public Set getQueryIndexResourceIds(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         return utility.getQueryIndexResourceIds(coralSession, index);
     }
 
     /**
      * Returns map of resource sets keyed by index resoruces. This shows the mapping between
      * the index and indexed resource. The parameter is the set of resources to be assigned
      * to indexes.
      * 
      * @param resources the set of resources for which indexes are sought
      * @return map of found indexes with corresponding resources. 
      */
     public Map getResourcesByIndex(CoralSession coralSession, Set resources)    
     {
         Map resSetByIndex = new HashMap();
         Resource[] tmpIndexes = null;
         for (Iterator iter = resources.iterator(); iter.hasNext();)
         {
             IndexableResource res = (IndexableResource)iter.next();
             
             // add indexes indexing the resource as a node
             tmpIndexes = searchService.getIndexedNodesRelation(coralSession).getInverted().get(res);
             addResourceForIndexes(resSetByIndex, res, tmpIndexes);
             
             // add indexes indexing the resource as a part of a branch
             Resource resource = res;
             while (resource != null)
             {
                 tmpIndexes = searchService.getIndexedBranchesRelation(coralSession).getInverted().get(resource);
                 addResourceForIndexes(resSetByIndex, res, tmpIndexes);
                 resource = resource.getParent();
             }
         }
         
         return resSetByIndex;
     }
     
     private void addResourceForIndexes(Map resSetByIndex, Resource res, Resource[] tmpIndexes)
     {
         for (int i = 0; i < tmpIndexes.length; i++)
         {
             IndexResource index = (IndexResource) tmpIndexes[i];
             Set resSet = (Set) resSetByIndex.get(index);
             if(resSet == null)
             {
                 resSet = new HashSet();
             }
             resSet.add(res);
             resSetByIndex.put(index, resSet);
         }
     }    
     
     // adding to index -----------------------------------------------------------------------------
     
     public void addToIndex(CoralSession coralSession, IndexResource index, IndexableResource[] res)
         throws SearchException
     {
         final String action = "adding resources to the index";
         synchronized(index)
         {
             // get index data directory
             Directory dir = getIndexDirectory(index);
     
             IndexWriter indexWriter = utility.openIndexWriter(dir, index, false, action);
             Set<Resource> querySet = utility.getQueryIndexResourceIds(coralSession, index);
             
             int updateCount = 0;
             for (int i = 0; i < res.length; i++)
             {
                 IndexableResource resource = res[i];
                 Resource branch = utility.getBranch(coralSession, index, resource);
                 
                 // add to index
                 if(branch != null && liableForIndexing(coralSession, resource, index, querySet))
                 {
                     // cache the document maybe
                     // - need a kind of temporary cache while adding resources to many indexes
                     Document doc = docConstructor.createDocument(coralSession, resource, branch);
                     if(doc == null)
                     {
                         log.error("IndexingFacility: Could not create Document for resource #"+
                             resource.getIdString()+" '"+resource.getPath()+"'");
                     }
                     else
                     {
                         try
                         {
                             indexWriter.addDocument(doc);
                             updateCount++;
                         }
                         catch(IOException e)
                         {
                             log.error(
                             "IndexingFacility: Could not add document to IndexWriter for index '"+
                             index.getPath()+"' for resource #"+resource.getIdString()+" '"+
                             resource.getPath()+"'", e);
                         }
                     }
                 }
             }
 
             Map<String, String> userData = utility
                 .getLastCommitUserData(index, indexWriter, action);
             utility.commitIndexWriter(indexWriter, index,
                 IndexingFacilityUtil.incrementChangeCounter(userData, updateCount), action);
             utility.closeIndexWriter(indexWriter, index, action);
         }
     }
 
     // deleting from index -------------------------------------------------------------------------
     
     public void deleteFromIndex(IndexResource index, IndexableResource[] iRes)
         throws SearchException
     {
         long[] id = new long[iRes.length];
         for (int i = 0; i < iRes.length; i++)
         {
             id[i] = iRes[i].getId();
         }
         deleteFromIndex(index, id);
     }
 
     public void deleteFromIndex(IndexResource index, long[] ids)
         throws SearchException
     {
         String[] id = new String[ids.length];
         for (int i = 0; i < ids.length; i++)
         {
             id[i] = Long.toString(ids[i]);
         }
         
         synchronized(index)
         {
             IndexWriter indexWriter = utility.openIndexWriter(getIndexDirectory(index), index,
                 false, "removing resources # " + id);
 
             Collection<Term> terms = new ArrayList<>();
             for(String identifier : id)
             {
                 terms.add(new Term(SearchConstants.FIELD_ID, identifier));
             }
             try
             {
                 indexWriter.deleteDocuments(terms.toArray(new Term[terms.size()]));
             }
             catch(IOException e)
             {
                 log.error(
                     "IndexingFacility: Could not remove resources #'" + index.getPath() + "'", e);
             }
 
             utility.closeIndexWriter(indexWriter, index, "removing resources # " + id);
         }
     }    
     
     /**
      * remove index lock file.
      * 
      * @param index
      */    
     public void removeStaleWriteLock(IndexResource index)
         throws SearchException
     {
         Directory dir = getIndexDirectory(index);
         synchronized(index)
         {
             try
             {
                 if(dir.fileExists(IndexWriter.WRITE_LOCK_NAME))
                 {
                     dir.deleteFile(IndexWriter.WRITE_LOCK_NAME);
                 }
             }
             catch(IOException e)
             {
                 throw new SearchException(
                     "IndexingLock: Could not remove the index lock file for index: "
                         + index.getPath(), e);
             }
         }
     }
     
     // implementation ------------------------------------------------------------------------------
 
     /**
      * index the resource tree.
      * @param node the resource tree node.
      * @param branch branch under which current node is indexed
      * @param indexWriter the opened index writer.
      * @param index the index resource representing the lucene index.
      * @param querySet TODO
      * @param recursive <code>true</code> if indexing should be recursive.
      * @param stats TODO
      */
     private void index(CoralSession coralSession, Resource node, Resource branch,
         IndexWriter indexWriter, IndexResource index, Set<Resource> querySet, boolean recursive,
         ReindexStats stats)
     throws IOException, SearchException
     {
         stats.documentsChecked++;
         if (node instanceof IndexableResource)
         {
         	IndexableResource res = (IndexableResource)node;
             // add to index
             if(liableForIndexing(coralSession, res, index, querySet))
         	{
 	            Document doc = docConstructor.createDocument(coralSession, res, branch);
                 if(doc == null)
                 {
                     log.error("IndexingFacility: Could not create Document for resource '"+
                         res.getPath()+"'");
                 }
 	            else
 	            {
                     stats.documentsAdded++;
 	                indexWriter.addDocument(doc);
 	            }
 			}
         }
 
         if (recursive)
         {
             Resource[] children = coralSession.getStore().getResource(node);
             for (int i = 0; i < children.length; i++)
             {
                 index(coralSession, children[i], branch, indexWriter, index, querySet, recursive, stats);
             }
         }
     }
 
     public static class ReindexStats
     {
         public int documentsChecked;
 
         public int documentsAdded;
     }
 }
