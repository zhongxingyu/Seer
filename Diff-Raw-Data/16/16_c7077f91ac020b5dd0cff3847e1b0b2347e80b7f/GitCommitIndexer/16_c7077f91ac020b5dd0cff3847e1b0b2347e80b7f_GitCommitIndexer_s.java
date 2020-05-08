 package jiracommitviewer.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import jiracommitviewer.RepositoryManager;
 import jiracommitviewer.domain.AbstractRepository;
 import jiracommitviewer.domain.GitCommitKey;
 import jiracommitviewer.domain.GitRepository;
 import jiracommitviewer.domain.LogEntry;
 import jiracommitviewer.index.exception.IndexException;
 import jiracommitviewer.repository.exception.RepositoryException;
 import jiracommitviewer.repository.service.GitRepositoryService;
 import jiracommitviewer.repository.service.LogEntryEnumerator;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.Validate;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.document.DateTools.Resolution;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.config.util.IndexPathManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueManager;
 import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.project.version.VersionManager;
 import com.atlassian.jira.security.PermissionManager;
 import com.atlassian.jira.security.Permissions;
 import com.atlassian.jira.util.JiraKeyUtils;
 import com.opensymphony.util.TextUtils;
 
 /**
  * Indexer for Git repositories.
  * 
  * @author mark
  */
 public class GitCommitIndexer implements CommitIndexer<GitRepository, GitCommitKey> {
 
 	private final static Logger logger = LoggerFactory.getLogger(GitCommitIndexer.class);
 	
     @Autowired
     private RepositoryManager repositoryManager;
     @Autowired
     private GitRepositoryService gitRepositoryService;
     @Autowired
     private VersionManager versionManager;
     @Autowired
     private IssueManager issueManager;
     @Autowired
     private PermissionManager permissionManager;
     @Autowired
     private ChangeHistoryManager changeHistoryManager;
     @Autowired
     private IndexPathManager indexPathManager;
     
     private LuceneIndexAccessor indexAccessor;
 
     public GitCommitIndexer() {
     	indexAccessor = new DefaultLuceneIndexAccessor();
     }
 
     public GitCommitIndexer(final LuceneIndexAccessor accessor) {
         this.indexAccessor = accessor;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void index(final GitRepository repository) throws IndexException, RepositoryException {
     	Validate.notNull(repository, "repository must not be null");
     	
     	gitRepositoryService.cloneRepository(repository);
     	gitRepositoryService.fetch(repository);
     	createIndexIfNeeded();
     	updateIndex(repository, false);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public File getIndexPath() {
     	return new File(gitRepositoryService.getIndexPath() + File.separator + "indexes");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<LogEntry<GitRepository, GitCommitKey>> getAllLogEntriesByIssue(final Issue issue, final int pageNumber, 
     		final int pageSize, final boolean ascending) throws IndexException {
     	Validate.notNull(issue, "issue must not be null");
     	Validate.isTrue(pageNumber >= 0, "pageNumber must be >= 0");
     	Validate.isTrue(pageSize >= 0, "pageSize must be >= 0");
     	
         if (logger.isDebugEnabled()) {
             logger.debug("Retrieving revisions for: " + issue.getKey());
         }
 
         // Create indexes if necessary to prevent getting an error
         createIndexIfNeeded();
         
         try {
 	        final IndexReader reader = indexAccessor.getIndexReader(getIndexPath().getPath());
 	        final IndexSearcher searcher = new IndexSearcher(reader);
 	
 	        try {
 	            final TopDocs hits = searcher.search(createQueryByIssueKey(issue), MAX_COMMITS, 
 	            		new Sort(new SortField(FIELD_DATE, SortField.LONG, !ascending)));
 	            final List<LogEntry<GitRepository, GitCommitKey>> logEntries = 
 	            		new ArrayList<LogEntry<GitRepository, GitCommitKey>>(hits.totalHits);
 	
 	            for (int i = 0; i < Math.min(hits.totalHits, MAX_COMMITS); i++) {
 	            	// Skip results until we arrive at our page
 	            	if (i < pageNumber * pageSize) {
 	            		continue;
 	            	}
 	            	// Page now filled so break out
 	            	if (i >= pageNumber * pageSize + pageSize) {
 	            		break;
 	            	}
 	            	
 	                final Document doc = searcher.doc(hits.scoreDocs[i].doc);
 	                final String repositoryId = doc.get(FIELD_REPOSITORY);
 	                final AbstractRepository repository = repositoryManager.getRepository(
 	                		repositoryManager.parseRepositoryId(repositoryId));
 	                
 	                // Verify that the repository is of the correct type.
 	                if (repository instanceof GitRepository) {
 	                	final GitCommitKey commitKey = GitCommitKey.unmarshal(doc.get(FIELD_COMMITKEY));
 	                	try {
 	                		final LogEntry<GitRepository, GitCommitKey> logEntry = gitRepositoryService
 	                				.getLogEntry((GitRepository)repository, commitKey);
 	                		if (logEntry.getBranches() == null) {
 	                			logEntry.setBranches(Arrays.asList(doc.getValues(FIELD_BRANCH)));
 	                		}
 	                		logEntries.add(logEntry);
 	                	} catch (final RepositoryException re) {
 	                		// Assume that this is because the commit could not be found due to a change of history. If that's
 	                		// the case then expect this to be cleared up by the main indexing activity.
 	                		logger.warn("Could not find log message for commit: " + commitKey.marshal(), re);
 	            			continue;
 	                	}
 	                }
 	            }
 	            return logEntries;
 	        } finally {
 	            searcher.close();
 	            reader.close();
 	        }
         } catch (final IOException ioe) {
         	throw new IndexException("Index IO access error", ioe);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public List<LogEntry<GitRepository, GitCommitKey>> getAllLogEntriesByProject(final String projectKey, final User user, 
     		final int pageNumber, final int pageSize, final boolean ascending) throws IndexException {
     	Validate.notNull(projectKey, "projectKey must not be null");
     	Validate.notNull(user, "user must not be null");
     	Validate.isTrue(pageNumber >= 0, "pageNumber must be >= 0");
     	Validate.isTrue(pageSize >= 0, "pageSize must be >= 0");
     	
     	// Create indexes if necessary to prevent getting an error
         createIndexIfNeeded();
         
         // Set up and perform a search for all documents having the supplied projectKey,
         // sorted in descending date order
         final TermQuery query = new TermQuery(new Term(FIELD_PROJECTKEY, projectKey));
 
         try {
 	        final IndexReader reader = indexAccessor.getIndexReader(getIndexPath().getPath());
 	        final IndexSearcher searcher = new IndexSearcher(reader);
 	
 	        try {
 	            final TopDocs hits = searcher.search(query, new ProjectRevisionFilter(issueManager, permissionManager, user, projectKey), 
 	            		MAX_COMMITS, new Sort(new SortField(FIELD_DATE, SortField.LONG, !ascending)));
 	            final List<LogEntry<GitRepository, GitCommitKey>> logEntries = new ArrayList<LogEntry<GitRepository, GitCommitKey>>();
 	            
 	            for (int i = 0, j = Math.min(hits.totalHits, MAX_COMMITS); i < j; ++i) {
 	            	// Skip results until we arrive at our page
 	            	if (i < pageNumber * pageSize) {
 	            		continue;
 	            	}
 	            	// Page now filled so break out
 	            	if (i >= pageNumber * pageSize + pageSize) {
 	            		break;
 	            	}
 	
 	                final Document doc = searcher.doc(hits.scoreDocs[i].doc);
 	
 	                final String repositoryId = doc.get(FIELD_REPOSITORY);
 	                final AbstractRepository repository = repositoryManager.getRepository(
 	                		repositoryManager.parseRepositoryId(repositoryId));
 	                
 	                // Verify that the repository is of the correct type.
 	                if (repository instanceof GitRepository) {
 	                	final GitCommitKey commitKey = GitCommitKey.unmarshal(doc.get(FIELD_COMMITKEY));
 	                	try {
 	                		final LogEntry<GitRepository, GitCommitKey> logEntry = gitRepositoryService
 	                				.getLogEntry((GitRepository)repository, commitKey);
 	                		if (logEntry.getBranches() == null) {
 	                			logEntry.setBranches(Arrays.asList(doc.getValues(FIELD_BRANCH)));
 	                		}
 	                		logEntries.add(logEntry);
 	                	} catch (final RepositoryException re) {
 	                		// Assume that this is because the commit could not be found due to a change of history. If that's
 	                		// the case then expect this to be cleared up by the main indexing activity.
 	                		logger.warn("Could not find log message for commit: " + commitKey.marshal(), re);
 	            			continue;
 	                	}
 	                }
 	            }
 	            return logEntries;
 	        } finally {
 	            searcher.close();
 	            reader.close();
 	        }
         } catch (final IOException ioe) {
         	throw new IndexException("Index IO access error", ioe);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("deprecation")
 	@Override
     public List<LogEntry<GitRepository, GitCommitKey>> getAllLogEntriesByVersion(final Version version, final User user, 
     		final int pageNumber, final int pageSize, final boolean ascending) throws IndexException {
     	Validate.notNull(version, "version must not be null");
     	Validate.notNull(user, "user must not be null");
     	Validate.isTrue(pageNumber >= 0, "pageNumber must be >= 0");
     	Validate.isTrue(pageSize >= 0, "pageSize must be >= 0");
     	
     	// Create indexes if necessary to prevent getting an error
         createIndexIfNeeded();
 
         // Find all isuses affected by and fixed by any of the versions:
         final Collection<Issue> issues = new HashSet<Issue>();
 
         issues.addAll(versionManager.getIssuesWithFixVersion(version));
         issues.addAll(versionManager.getIssuesWithAffectsVersion(version));
 
         // Construct a query with all the issue keys. Make sure to increase the maximum number of clauses if needed.
         final int maxClauses = BooleanQuery.getMaxClauseCount();
         if (issues.size() > maxClauses) {
             BooleanQuery.setMaxClauseCount(issues.size());
         }
 
         final BooleanQuery query = new BooleanQuery();
         final Set<String> permittedIssueKeys = new HashSet<String>();
 
         for (final Issue issue : issues) {
             final Issue theIssue = issueManager.getIssueObject(issue.getKey());
 
             if (permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, theIssue, user)) {
                 final TermQuery termQuery = new TermQuery(new Term(FIELD_ISSUEKEY, issue.getKey()));
                 query.add(termQuery, BooleanClause.Occur.SHOULD);
                 permittedIssueKeys.add(issue.getKey());
             }
         }
 
         try {
 	        final IndexReader reader = indexAccessor.getIndexReader(getIndexPath().getPath());
 	        final IndexSearcher searcher = new IndexSearcher(reader);
 	
 	        try {
 	            // Run the query and sort by date in descending order
 	            final TopDocs hits = searcher.search(query, new PermittedIssuesRevisionFilter(issueManager, permissionManager, user, 
 	            		permittedIssueKeys), MAX_COMMITS, new Sort(new SortField(FIELD_DATE, SortField.LONG, !ascending)));
 	            final List<LogEntry<GitRepository, GitCommitKey>> logEntries = new ArrayList<LogEntry<GitRepository, GitCommitKey>>();
 	
 	            for (int i = 0, j = Math.min(hits.totalHits, MAX_COMMITS); i < j; ++i) {
 	            	// Skip results until we arrive at our page
 	            	if (i < pageNumber * pageSize) {
 	            		continue;
 	            	}
 	            	// Page now filled so break out
 	            	if (i >= pageNumber * pageSize + pageSize) {
 	            		break;
 	            	}
 	
 	                final Document doc = searcher.doc(hits.scoreDocs[i].doc);
 	
 	                final String repositoryId = doc.get(FIELD_REPOSITORY);
 	                final AbstractRepository repository = repositoryManager.getRepository(
 	                		repositoryManager.parseRepositoryId(repositoryId));
 	                
 	                // Verify that the repository is of the correct type.
 	                if (repository instanceof GitRepository) {
 	                	final GitCommitKey commitKey = GitCommitKey.unmarshal(doc.get(FIELD_COMMITKEY));
 	                	try {
 	                		final LogEntry<GitRepository, GitCommitKey> logEntry = gitRepositoryService
 	                				.getLogEntry((GitRepository)repository, commitKey);
 	                		if (logEntry.getBranches() == null) {
 	                			logEntry.setBranches(Arrays.asList(doc.getValues(FIELD_BRANCH)));
 	                		}
 	                		logEntries.add(logEntry);
 	                	} catch (final RepositoryException re) {
 	                		// Assume that this is because the commit could not be found due to a change of history. If that's
 	                		// the case then expect this to be cleared up by the main indexing activity.
 	                		logger.warn("Could not find log message for commit: " + commitKey.marshal(), re);
 	            			continue;
 	                	}
 	                }
 	            }
 	            return logEntries;
 	        } finally {
 	            searcher.close();
 	            reader.close();
 	            BooleanQuery.setMaxClauseCount(maxClauses);
 	        }
         } catch (final IOException ioe) {
         	throw new IndexException("Index IO access error", ioe);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeEntries(final GitRepository repository) throws IndexException {
         logger.debug("Deleteing log entries for: " + repository.getId());
         
         // Create indexes if necessary to prevent getting an error
         createIndexIfNeeded();
 
         try {
         	final IndexWriter writer = indexAccessor.getIndexWriter(getIndexPath().getPath(), false, ANALYZER);
         	try {
 	            writer.deleteDocuments(new Term(FIELD_REPOSITORY, String.valueOf(repository.getId())));
 	        } finally {
 	        	writer.close();
 	        }
         } catch (final IOException ioe) {
             throw new IndexException("Index IO access error", ioe);
         }
     }
     
     /**
      * Looks for the revision index directory and creates it if it does not already exists.
      *
      * @return {@code true} if the index directory was created; {@code false} if the index already exists
      * @throws IndexException if the index cannot be created
      */
     private boolean createIndexIfNeeded() throws IndexException {
         boolean indexExists = getIndexPath().exists();
         if (!indexExists) {
             try {
                 indexAccessor.getIndexWriter(getIndexPath().getPath(), true, ANALYZER).close();
                 return true;
             } catch (final IOException ioe) {
                 throw new IndexException("Cannot create the repository index", ioe);
             }
         }
         return false;
     }
     
     /**
      * This method updates the index, assuming it already exists.
      *
      * @param repository the repository to index. It must already be active and ready to index. Must not be {@code null}
      * @param fullIndex true if this should be a full index rather than a partial index
      * @throws IndexException if there is some problem in the indexing subsystem meaning indexes cannot be updated
      * @throws RepositoryException 
      */
 	private void updateIndex(final GitRepository repository, final boolean fullIndex) throws IndexException, RepositoryException {
     	assert repository != null : "repository must not be null";
     	
         logger.debug("Updating commit index for repository: " + repository.getId() + ", full index = " + fullIndex);
     	
         try {
             // Examine differences between the heads as they are now and as they were when last indexed. Any differences
             // mean we'll have to examine every ancestor of these tips.
             // This should cover scenarios such as:
             // - Creating a new branch (derived from another tip, or not)
             // - Deleting an existing branch
             // - Merging an existing branch (with commit and fast-forward)
             // - Resetting a branch to an older commit (we must index from both old and new nodes)
             final Map<String, GitCommitKey> repositoryBranches = gitRepositoryService.getBranchHeads(repository);
             final Map<String, GitCommitKey> indexedBranches = getBranchHeadsIndexed(repository);
             final Map<GitCommitKey, List<String>> commitKeys = new HashMap<GitCommitKey, List<String>>();
             if (fullIndex) {
             	for (final Map.Entry<String, GitCommitKey> branchHead : repositoryBranches.entrySet()) {
             		commitKeys.put(branchHead.getValue(), Arrays.asList(branchHead.getKey()));
             	}
             } else {
 	            for (final Map.Entry<String, GitCommitKey> branchHead : repositoryBranches.entrySet()) {
 	            	final GitCommitKey indexedCommitKey = indexedBranches.remove(branchHead.getKey());
 	            	// Detect branch added
 	            	if (indexedCommitKey == null) {
 	            		commitKeys.put(branchHead.getValue(), Arrays.asList(branchHead.getKey()));
 	            		continue;
 	            	}
 	            	if (!indexedCommitKey.getCommitHash().equals(branchHead.getValue().getCommitHash())) {
 	            		commitKeys.put(indexedCommitKey, Arrays.asList(branchHead.getKey()));
 	            		commitKeys.put(branchHead.getValue(), Arrays.asList(branchHead.getKey()));
 	            	}
 	            }
 	            // Detect branches removed. We'll want to remove the branch from all documents where it is
 	            // kept and if those documents are empty of branches, the document should be removed entirely.
 	            // It is therefore easier to just reindex fully from here.
 	            if (indexedBranches.size() > 0) {
 	            	logger.debug("One or more branch deletions detected; performing full index");
             		updateIndex(repository, true);
             		return;
             	}
             }
             
             // Get the filtered entries
             final LogEntryEnumerator<GitRepository, GitCommitKey> logEntryEnumerator = 
            		gitRepositoryService.getLogEntries(repository, commitKeys.size() == 0 ? null : commitKeys);
 
             // 0 - Success
             // 1 - Failure
             int status = 1;
             final IndexWriter writer = indexAccessor.getIndexWriter(getIndexPath().getPath(), false, ANALYZER);
             try {
             	writer.prepareCommit();
                 
                 // Delete all documents to start with when full indexing
                 if (fullIndex) {
                 	writer.deleteDocuments(new Term(FIELD_REPOSITORY, String.valueOf(repository.getId())));
                 }
                 
                 final IndexReader reader = indexAccessor.getIndexReader(getIndexPath().getPath());
                 try {
                 	while (logEntryEnumerator.hasNext()) {
                 		final LogEntry<GitRepository, GitCommitKey> logEntry = logEntryEnumerator.next();
                 		
                 		if (TextUtils.stringSet(logEntry.getMessage()) && isKeyInString(logEntry)) {
                 			if (fullIndex || getDocument(repository, logEntry, reader, true) == null) {
                 				final Document previousDocument = getDocument(repository, logEntry, reader, false);
                 				
                 				// Delete any previous document (existing document, but with different branch sets)
                 				final TermQuery repoQuery = new TermQuery(new Term(FIELD_REPOSITORY, String.valueOf(repository.getId())));
                 	            final TermQuery revQuery = new TermQuery(new Term(FIELD_COMMITKEY, logEntry.getCommitKey().marshal()));
                 	            final BooleanQuery repoAndRevQuery = new BooleanQuery();
                 	            repoAndRevQuery.add(repoQuery, BooleanClause.Occur.MUST);
                 	            repoAndRevQuery.add(revQuery, BooleanClause.Occur.MUST);
                 				writer.deleteDocuments(repoAndRevQuery);
 
                 				// We may not be scanning all branches, so we must check that on previous document,
                 				// if a branch existed on it before that doesn't appear on it now and the branch
                 				// still exists, add it.
                 				if (previousDocument != null) {
                 					final List<String> previousBranches = new ArrayList<String>();
                 					for (final Fieldable fieldable : previousDocument.getFieldables(FIELD_BRANCH)) {
                 						previousBranches.add(fieldable.stringValue());
                 					}
                 					previousBranches.removeAll(logEntry.getBranches());
                 					for (final String previousBranch : previousBranches) {
                 						if (repositoryBranches.keySet().contains(previousBranch)) {
                 							Collection<List<String>> scanningBranches = commitKeys.values();
                 							// Only if we're not scanning it
                 							if (!scanningBranches.contains(Arrays.asList(previousBranch))) {
                 								logEntry.addBranch(previousBranch);
                 							}
                 						}
                 					}
                 				}
                 				
                 				// Create the new document and add it to the index
                 				final Document doc = createDocument(repository, logEntry);
                 				logger.debug("Indexing repository: " + repository.getId() + ", commit: " + 
                 						logEntry.getCommitKey().marshal());
                 				writer.addDocument(doc);
                 			}
                 		}
                 	}
                 } finally {
                     reader.close();
                 }
                 status = 0;
             } finally {
             	try {
             		if (status == 0) {
             			writer.commit();
             			updateBranchesIndexed(repository, repositoryBranches, writer);
             		} else if (status == 1) {
             			logger.debug("Error, rolling back indexing just performed");
             			writer.rollback();
             		}
             	} finally {
             		writer.close();
             	}
             }
         } catch (final IOException e) {
             logger.warn("Unable to index repository '" + repository.getDisplayName() + "'", e);
         }
         logger.debug("Indexing for repository complete: " + repository.getId());
     }
     
     /**
      * Works out whether a given commit for the specified repository is already in the index or not and returns its document
      * if it is.
      * <p>
      * This searches by the repository and commit key matching the specified {@code logEntry}.
      * 
      * @param gitRepository the repository. Must not be {@code null}
      * @param logEntry the commit. Must not be {@code null}
      * @param reader the index reader. Must not be {@code null}
      * @param withBranches if true, branch names from the specified {@code logEntry} must match those in any document as well
      * @return the document if found, or {@code null} if not
      * @throws IndexException if a problem occurs reading the index
      */
     private Document getDocument(final GitRepository gitRepository, final LogEntry<GitRepository, GitCommitKey> logEntry, 
     		final IndexReader reader, final boolean withBranches) throws IndexException {
     	assert gitRepository != null : "gitRepository must not be null";
     	assert logEntry != null : "logEntry must not be null";
     	assert reader != null : "reader must not be null";
     	
         final IndexSearcher searcher = new IndexSearcher(reader);
         try {
 	        try {
 	            final TermQuery repoQuery = new TermQuery(new Term(FIELD_REPOSITORY, String.valueOf(gitRepository.getId())));
 	            final TermQuery revQuery = new TermQuery(new Term(FIELD_COMMITKEY, logEntry.getCommitKey().marshal()));
 	            final BooleanQuery repoAndRevQuery = new BooleanQuery();
 	
 	            repoAndRevQuery.add(repoQuery, BooleanClause.Occur.MUST);
 	            repoAndRevQuery.add(revQuery, BooleanClause.Occur.MUST);
 	
 	            final TopDocs hits = searcher.search(repoAndRevQuery, MAX_COMMITS);
 	
 	            if (hits.totalHits == 1) {
 	                final Document foundDocument = searcher.doc(hits.scoreDocs[0].doc);
 	                if (!withBranches) {
 	                	return foundDocument;
 	                }
 	                final Fieldable[] fieldables = foundDocument.getFieldables(FIELD_BRANCH);
 	                final String[] documentBranches = new String[fieldables.length];
 	                for (int i = 0; i < fieldables.length; i++) {
 	                	documentBranches[i] = fieldables[i].stringValue();
 	                }
 	                return new HashSet<String>(logEntry.getBranches()).equals(new HashSet<String>(Arrays.asList(documentBranches)))
 	                		? foundDocument
 	                		: null;
 	            } else if (hits.totalHits == 0) {
 	                return null;
 	            } else {
 	                throw new IndexException("Found MORE than one document for commit key: " + logEntry.getCommitKey().marshal() + 
 	                	", repository=" + gitRepository.getId());
 	            }
 	        } finally {
 	            searcher.close();
 	        }
         } catch (final IOException ioe) {
         	throw new IndexException("Error accessing index", ioe);
         }
     }
 
     /**
      * Creates the index query that finds all log entries for the specified {@code issue}.
      * 
      * @param issue the issue to get log entries for. Must not be {@code null}
      * @return the query. Never {@code null}
      */
     private Query createQueryByIssueKey(final Issue issue) {
     	Validate.notNull(issue, "issue must not be null");
     	
         final BooleanQuery query = new BooleanQuery();
 
         // Add current key
         query.add(new TermQuery(new Term(FIELD_ISSUEKEY, issue.getKey())), BooleanClause.Occur.SHOULD);
 
         // Add all previous keys
         final Collection<String> previousIssueKeys = changeHistoryManager.getPreviousIssueKeys(issue.getId());
         for (final String previousIssueKey : previousIssueKeys) {
             TermQuery termQuery = new TermQuery(new Term(FIELD_ISSUEKEY, previousIssueKey));
             query.add(termQuery, BooleanClause.Occur.SHOULD);
         }
 
         return query;
     }
     
     /**
      * Creates a new Lucene document for the supplied log entry. This method is used when indexing
      * commits, not during retrieval.
      *
      * @param gitRepository the repository to which {@code logEntry} belongs. Must not be {@code null}
      * @param logEntry the log entry that is about to be indexed. Must not be {@code null}
      * @return a Lucene document object that is ready to be added to an index. Never {@code null}
      */
     private Document createDocument(final GitRepository gitRepository, final LogEntry<GitRepository, GitCommitKey> logEntry) {
     	assert gitRepository != null : "gitRepository must not be null";
     	assert logEntry != null : "logEntry must not be null";
     	
         final Document doc = new Document();
 
         doc.add(new Field(FIELD_MESSAGE, logEntry.getMessage(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 
         if (logEntry.getAuthorName() != null) {
             doc.add(new Field(FIELD_AUTHOR, logEntry.getAuthorName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         }
 
         doc.add(new Field(FIELD_REPOSITORY, String.valueOf(gitRepository.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field(FIELD_COMMITKEY, logEntry.getCommitKey().marshal(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         for (final String branch : logEntry.getBranches()) {
         	doc.add(new Field(FIELD_BRANCH, branch, Field.Store.YES, Field.Index.NOT_ANALYZED));
         }
 
         if (logEntry.getDate() != null) {
             doc.add(new Field(FIELD_DATE, DateTools.dateToString(logEntry.getDate(), Resolution.SECOND), Field.Store.YES, Field.Index.NOT_ANALYZED));
         }
 
         // relevant issue keys
         final List<String> keys = getIssueKeysFromString(logEntry);
 
         // Relevant project keys. Used to avoid adding duplicate projects.
         final Map<String, String> projects = new HashMap<String, String>();
 
         for (final String issueKey : keys) {
             doc.add(new Field(FIELD_ISSUEKEY, issueKey, Field.Store.YES, Field.Index.NOT_ANALYZED));
             String projectKey = getProjectKeyFromIssueKey(issueKey);
             if (!projects.containsKey(projectKey)) {
                 projects.put(projectKey, projectKey);
                 doc.add(new Field(FIELD_PROJECTKEY, projectKey, Field.Store.YES, Field.Index.NOT_ANALYZED));
             }
         }
 
         return doc;
     }
     
     /**
      * Gets a map of branches that have been indexed for this {@code repository}.
      * 
      * @param repository the repository to get indexed branches for. Must not be {@code null}
      * @return the indexed branches or an empty map if no indexed branches could be found
      * @throws IndexException
      */
     private Map<String, GitCommitKey> getBranchHeadsIndexed(final GitRepository repository) throws IndexException {
     	assert repository != null : "repository must not be null";
     	
     	final Document doc = getBranchesIndexedDocument(repository);
     	if (doc == null) {
     		return Collections.emptyMap();
     	}
     	final Map<String, GitCommitKey> branches = new HashMap<String, GitCommitKey>();
 		for (final Fieldable branch : doc.getFieldables(FIELD_BRANCH)) {
 			final String[] parts = StringUtils.split(branch.stringValue(), "-", 3);
 			branches.put(parts[2], GitCommitKey.unmarshal(parts[0] + "-" + parts[1]));
 		}
 		return branches;
     }
     
     /**
      * Updates the map of indexed {@code branches} for the specified {@code repository}.
      * 
      * @param repository the repository whose branches to update. Must not be {@code null}
      * @param branches the map of branches to write to the index. Must not be {@code null}
      * @param writer the opened index writer. Must not be {@code null}
      * @throws IndexException
      */
     private void updateBranchesIndexed(final GitRepository repository, final Map<String, GitCommitKey> branches,
     		final IndexWriter writer) throws IndexException {
     	assert repository != null : "repository must not be null";
     	assert branches != null : "branches must not be null";
     	assert writer != null : "writer must not be null";
     	
     	// Get and update or create a new document containing the given branches
     	Document doc = getBranchesIndexedDocument(repository);
     	if (doc == null) {
     		doc = new Document();
     		doc.add(new Field(FIELD_REPOSITORY, String.valueOf(repository.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
     		doc.add(new Field(FIELD_BRANCHMAP, String.valueOf(Boolean.TRUE), Field.Store.YES, Field.Index.NOT_ANALYZED));
     	} else {
     		doc.removeFields(FIELD_BRANCH);
     	}
     	for (final Map.Entry<String, GitCommitKey> branch : branches.entrySet()) {
 			doc.add(new Field(FIELD_BRANCH, branch.getValue().marshal() + "-" + branch.getKey(), Field.Store.YES, 
 				Field.Index.NOT_ANALYZED));
 		}
     	
     	// Write the document, replacing any that existed before
     	try {
     		final TermQuery repoQuery = new TermQuery(new Term(FIELD_REPOSITORY, String.valueOf(repository.getId())));
 			final TermQuery branchMapQuery = new TermQuery(new Term(FIELD_BRANCHMAP, String.valueOf(Boolean.TRUE)));
          
 			final BooleanQuery repoAndBranchQuery = new BooleanQuery();
 			repoAndBranchQuery.add(repoQuery, BooleanClause.Occur.MUST);
 			repoAndBranchQuery.add(branchMapQuery, BooleanClause.Occur.MUST);
     		writer.deleteDocuments(repoAndBranchQuery);
     		
     		writer.addDocument(doc);
     	} catch (final IOException ioe) {
     		throw new IndexException("Unable to write to index", ioe);
     	}
     }
     
     /**
      * Gets the document which stores branches currently indexed against the specified {@code repository}.
      * 
      * @param repository the repository to get indexed branches for. Must not be {@code null}
      * @return the document found or {@code null} if no such document exists
      * @throws IndexException
      */
     private Document getBranchesIndexedDocument(final GitRepository repository) throws IndexException {
     	assert repository != null : "repository must not be null";
     	
     	try {
     		final IndexReader reader = indexAccessor.getIndexReader(getIndexPath().getPath());
     		final IndexSearcher searcher = new IndexSearcher(reader);
     		try {
     			final TermQuery repoQuery = new TermQuery(new Term(FIELD_REPOSITORY, String.valueOf(repository.getId())));
     			final TermQuery branchMapQuery = new TermQuery(new Term(FIELD_BRANCHMAP, String.valueOf(Boolean.TRUE)));
 	         
     			final BooleanQuery repoAndBranchQuery = new BooleanQuery();
     			repoAndBranchQuery.add(repoQuery, BooleanClause.Occur.MUST);
     			repoAndBranchQuery.add(branchMapQuery, BooleanClause.Occur.MUST);
 	         
 				final TopDocs topDocs = searcher.search(repoAndBranchQuery, 1);
 				if (topDocs.totalHits == 0) {
 					return null;
 				}
 				return searcher.doc(topDocs.scoreDocs[0].doc);
     		} finally {
 	    		searcher.close();
 	    		reader.close();
 	    	}
     	} catch (final IOException e) {
     		throw new IndexException("Unable to search for branches", e);
     	}
     }
     
     /**
      * Gets the project key from the supplied {@code issueKey}.
      * 
      * @param issueKey the issue key. Must not be {@code null}
      * @return the project key. Never {@code null}
      */
     private String getProjectKeyFromIssueKey(final String issueKey) {
     	assert issueKey != null : "issueKey must not be null";
     	
         final String issueKeyUpperCase = StringUtils.upperCase(issueKey);
         return JiraKeyUtils.getFastProjectKeyFromIssueKey(issueKeyUpperCase);
     }
     
     /**
      * Gets a list of actual issue keys from the supplied {@code logEntry}'s log message.
      * 
      * @param logEntry the log entry. Must not be {@code null}
      * @return the list of issue keys. Never {@code null}
      */
     private List<String> getIssueKeysFromString(final LogEntry<GitRepository, GitCommitKey> logEntry) {
     	assert logEntry != null : "logEntry must not be null";
     	
         final String logMessageUpperCase = StringUtils.upperCase(logEntry.getMessage());
         return JiraKeyUtils.getIssueKeysFromString(logMessageUpperCase);
     }
     
     /**
      * Gets whether there is an issue key within the specified {@code logEntry}.
      * 
      * @param logEntry the log entry to check. Must not be {@code null}
      * @return true if there is an issue key in the entry's message; false otherwise
      */
     private boolean isKeyInString(final LogEntry<GitRepository, GitCommitKey> logEntry) {
     	assert logEntry != null : "logEntry must not be null";
     	
         final String logMessageUpperCase = StringUtils.upperCase(logEntry.getMessage());
         return JiraKeyUtils.isKeyInString(logMessageUpperCase);
     }
 }
