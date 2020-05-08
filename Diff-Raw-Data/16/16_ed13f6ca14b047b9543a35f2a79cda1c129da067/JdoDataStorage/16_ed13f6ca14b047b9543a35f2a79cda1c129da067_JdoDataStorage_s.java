 package com.bouncingdata.plfdemo.datastore;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import org.springframework.dao.DataRetrievalFailureException;
 import org.springframework.orm.jdo.support.JdoDaoSupport;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.SearchResult;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Activity;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Analysis;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisDataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisVote;
 import com.bouncingdata.plfdemo.datastore.pojo.model.BcDataScript;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Comment;
 import com.bouncingdata.plfdemo.datastore.pojo.model.CommentVote;
 import com.bouncingdata.plfdemo.datastore.pojo.model.DataCollection;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Dataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.ExecutionLog;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Following;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Group;
 import com.bouncingdata.plfdemo.datastore.pojo.model.GroupAuthority;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Scraper;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Tag;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Visualization;
 
 @SuppressWarnings({ "unchecked", "deprecation" })
 public class JdoDataStorage extends JdoDaoSupport implements DataStorage {
 
 	private UserActionLog createUserActionLogger(int actionType) {
 		/* Start log */
 		UserActionLog actionLog = new UserActionLog(actionType);
 		actionLog.startLog();
 		return actionLog;
 
 	}
 
 	private void FinallizeLogData(UserActionLog actionLog, User user) {
 		actionLog.setUser(user);
 		actionLog.stopLog();
 		user.setUserActionLog(actionLog);
 	}
 
 	@Override
 	public List<Dataset> getDatasetList(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 
 		Query q = pm.newQuery(Dataset.class);
 		q.setFilter("user.id==" + userId + " && isActive == true");
 		List<Dataset> results = null;
 		try {
 			results = (List<Dataset>) q.execute();
 			results = (List<Dataset>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			tx.begin();
 			tx.commit();
 			q.closeAll();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public List<Analysis> getAnalysisList(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(Analysis.class);
 		q.setFilter("user.id==" + userId);
 		List<Analysis> results = null;
 		try {
 			results = (List<Analysis>) q.execute();
 			results = (List<Analysis>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			tx.begin();
 			tx.commit();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Analysis> getPrivateAnalyses(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(Analysis.class);
 		q.setFilter("user.id==" + userId + " && isPublised==false");
 		List<Analysis> results = null;
 		try {
 			results = (List<Analysis>) q.execute();
 			results = (List<Analysis>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			tx.begin();
 			User user = pm.getObjectById(User.class, userId);
 			tx.commit();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Analysis> getPublicAnalyses(int userId) {
 		UserActionLog actionLog = createUserActionLogger(UserActionLog.ActionType.GETPRIVATEANALYSES);
 
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(Analysis.class);
 		q.setFilter("user.id==" + userId + " && isPublised==true");
 		List<Analysis> results = null;
 		try {
 			results = (List<Analysis>) q.execute();
 			results = (List<Analysis>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			tx.begin();
 			User user = pm.getObjectById(User.class, userId);
 			FinallizeLogData(actionLog, user);
 			tx.commit();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public User findUserByUsername(String username) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(User.class);
 		User user = null;
 		q.setFilter("username == '" + username + "'");
 		try {
 			List<User> results = (List<User>) q.execute();
 			if (results.size() > 0)
 				user = ((List<User>) pm.detachCopyAll(results)).get(0);
 			return user;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public User findUserByEmail(String email) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(User.class);
 		User user = null;
 		q.setFilter("email == '" + email + "'");
 		try {
 			List<User> results = (List<User>) q.execute();
 			if (results.size() > 0)
 				user = ((List<User>) pm.detachCopyAll(results)).get(0);
 			return user;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public ExecutionLog getExecutionLog(String executionId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(ExecutionLog.class);
 		q.setFilter("executionId==\"" + executionId + "\"");
 		List<ExecutionLog> results = null;
 		try {
 			results = (List<ExecutionLog>) q.execute();
 			results = (List<ExecutionLog>) pm.detachCopyAll(results);
 			return (results.size() > 0 ? results.get(0) : null);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<AnalysisDataset> getAnalysisDatasets(int anlsId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(AnalysisDataset.class);
 		q.setFilter("analysis.id == " + anlsId + " && isActive == true");
 		List<AnalysisDataset> results = null;
 		try {
 			results = (List<AnalysisDataset>) q.execute();
 			results = (List<AnalysisDataset>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<AnalysisDataset> getRelatedAnalysis(int datasetId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(AnalysisDataset.class);
 		q.setFilter("dataset.id == " + datasetId + " && isActive == true");
 		List<AnalysisDataset> results = null;
 		try {
 			results = (List<AnalysisDataset>) q.execute();
 			results = (List<AnalysisDataset>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Visualization> getAnalysisVisualizations(int anlsId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Visualization.class);
 		q.setFilter("analysis.id == " + anlsId + " && isActive==true");
 		List<Visualization> results = null;
 		try {
 			results = (List<Visualization>) q.execute();
 			results = (List<Visualization>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public SearchResult search(String query) {
 		PersistenceManager pm = getPersistenceManager();
 		SearchResult sr = new SearchResult();
 		Query q = pm.newQuery(Analysis.class);
 		try {
 			q.setFilter("this.name.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\")");
 			List<Analysis> apps = (List<Analysis>) pm
 					.detachCopyAll((List<Analysis>) q.execute());
 			sr.setAnalyses(apps);
 
 			q = pm.newQuery(Dataset.class);
 			q.setFilter("this.isActive == true && (this.name.toLowerCase().matches(\".*"
 					+ query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\"))");
 			List<Dataset> datasets = (List<Dataset>) pm
 					.detachCopyAll((List<Dataset>) q.execute());
 			sr.setDatasets(datasets);
 
 			q = pm.newQuery(Scraper.class);
 			q.setFilter("this.name.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\")");
 			List<Scraper> scrapers = (List<Scraper>) pm
 					.detachCopyAll((List<Scraper>) q.execute());
 			sr.setScrapers(scrapers);
 
 			q = pm.newQuery(User.class);
 			q.setFilter("this.username.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.firstName.toLowerCase().matches(\".*"
 					+ query + ".*\") || "
 					+ "this.lastName.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.email.toLowerCase().matches(\".*" + query
 					+ ".*\")");
 			List<User> users = (List<User>) pm.detachCopyAll((List<User>) q
 					.execute());
 			sr.setUsers(users);
 			return sr;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public SearchResult search(String query, int ownerId) {
 		PersistenceManager pm = getPersistenceManager();
 		SearchResult sr = new SearchResult();
 		Query q = pm.newQuery(Analysis.class);
 		try {
 			q.setFilter("this.user.id == " + ownerId
 					+ " && (this.name.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\"))");
 			List<Analysis> apps = (List<Analysis>) pm
 					.detachCopyAll((List<Analysis>) q.execute());
 			sr.setAnalyses(apps);
 
 			q = pm.newQuery(Dataset.class);
 			q.setFilter("this.user.id == "
 					+ ownerId
 					+ " && this.isActive == true && (this.name.toLowerCase().matches(\".*"
 					+ query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\"))");
 			List<Dataset> datasets = (List<Dataset>) pm
 					.detachCopyAll((List<Dataset>) q.execute());
 			sr.setDatasets(datasets);
 
 			q = pm.newQuery(Scraper.class);
 			q.setFilter("this.user.id == " + ownerId
 					+ " && (this.name.toLowerCase().matches(\".*" + query
 					+ ".*\") || this.description.toLowerCase().matches(\".*"
 					+ query + ".*\"))");
 			List<Scraper> scrapers = (List<Scraper>) pm
 					.detachCopyAll((List<Scraper>) q.execute());
 			sr.setScrapers(scrapers);
 
 			return sr;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createUser(User user) {
 		List<User> users = new ArrayList<User>();
 		users.add(user);
 		persistData(users);
 	}
 
 	public void deleteUser(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		User user = pm.getObjectById(User.class, userId);
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			pm.deletePersistent(user);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createGroup(Group group) {
 		List<Group> groups = new ArrayList<Group>();
 		groups.add(group);
 		persistData(groups);
 	}
 
 	@Override
 	public void createAnalysis(Analysis analysis) {
 		/* Start log */
 		PersistenceManager pm = getPersistenceManager();
 		User user = pm.getObjectById(User.class, analysis.getUser().getId());
 		Transaction tx = pm.currentTransaction();
 		analysis.setUser(user);
 		Set<Tag> tags = analysis.getTags();
 		if (tags != null) {
 			Set<Tag> tagSet = new HashSet<Tag>();
 			for (Tag t : tags) {
 				if (t.getId() > 0) {
 					Tag tag = pm.getObjectById(Tag.class, t.getId());
 					tagSet.add(tag);
 				} else {
 					tagSet.add(t);
 				}
 			}
 			analysis.setTags(tagSet);
 		}
 
 		try {
 			tx.begin();
 			pm.makePersistent(analysis);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void deleteAnalysis(int analysisId) {
 		PersistenceManager pm = getPersistenceManager();
 		Analysis anls = pm.getObjectById(Analysis.class, analysisId);
 
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			pm.deletePersistent(anls);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	private <T> void persistData(Collection<T> collection) {
 		if (collection != null && collection.size() > 0) {
 			PersistenceManager pm = getPersistenceManager();
 			
 			Transaction tx = pm.currentTransaction();
 			try {
 				tx.begin();
 				pm.makePersistentAll(collection);
 				tx.commit();
 			} finally {
 				if (tx.isActive())
 					tx.rollback();
 				pm.close();
 			}
 		}
 	}
 
 	@Override
 	public Collection<String> getUserAuthorities(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		/*
 		 * Query q = pm.newQuery(Membership.class); q.execute(); q =
 		 * pm.newQuery(GroupAuthority.class); q.execute(); q =
 		 * pm.newQuery(Group.class); q.execute();
 		 */
 		/**
 		 * "select ga.authority from users u, memberships m, group_authorities
 		 * ga where u.user_id = m.user_id and m.group_id = ga.group_id and
 		 * u.user_id = " + userId
 		 */
 		// temporarily use JPQL
 		Query q = pm
 				.newQuery(
 						"javax.jdo.query.JPQL",
 						"SELECT ga FROM com.bouncingdata.plfdemo.datastore.pojo.model.User u, "
 								+ "com.bouncingdata.plfdemo.datastore.pojo.model.Membership m, "
 								+ "com.bouncingdata.plfdemo.datastore.pojo.model.GroupAuthority ga "
 								+ "WHERE u.id = m.userId AND m.groupId = ga.groupId AND u.id = "
 								+ userId);
 		try {
 			List<GroupAuthority> gas = (List<GroupAuthority>) q.execute();
 			if (gas != null) {
 				List<String> authorities = new ArrayList<String>();
 				for (GroupAuthority ga : gas) {
 					authorities.add(ga.getAuthority());
 				}
 				return authorities;
 			}
 			return null;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public Analysis getAnalysisByGuid(String guid) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Analysis.class);
 		q.setFilter("guid == \"" + guid + "\"");
 		Analysis anls = null;
 		try {
 			List<Analysis> anlses = (List<Analysis>) q.execute();
 			anls = anlses.size() > 0 ? anlses.get(0) : null;
 			if (anls != null) {
 				anls.setCommentCount(anls.getComments() != null ? anls
 						.getComments().size() : 0);
 				anls = pm.detachCopy(anls);
 			}
 			return anls;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void updateAnalysis(Analysis analysis) {
 		if (analysis.getUser() == null)
 			return;
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		// User user = pm.getObjectById(User.class, analysis.getUser().getId());
 		try {
 			tx.begin();
 			Analysis anls = pm.getObjectById(Analysis.class, analysis.getId());
 			anls.setName(analysis.getName());
 			anls.setDescription(analysis.getDescription());
 			anls.setLanguage(analysis.getLanguage());
 			// anls.setUser(user);
 			anls.setLastUpdate(new Date());
 			anls.setPublished(analysis.isPublished());
 			anls.setTags(analysis.getTags());
 			anls.setLineCount(analysis.getLineCount());
 			anls.setThumbnail(analysis.getThumbnail());
 			//
 			tx.commit();
 		} finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createVisualization(Visualization visualization) {
 		PersistenceManager pm = getPersistenceManager();
 		User user = pm.getObjectById(User.class, visualization.getUser()
 				.getId());
 		visualization.setUser(user);
 		Analysis anls = pm.getObjectById(Analysis.class, visualization
 				.getAnalysis().getId());
 		visualization.setAnalysis(anls);
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			pm.makePersistent(visualization);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public Dataset getDatasetByGuid(String guid) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Dataset.class);
 		q.setFilter("guid == \"" + guid + "\" && isActive == true");
 		Dataset ds = null;
 		try {
 			List<Dataset> results = (List<Dataset>) q.execute();
 			results = (List<Dataset>) pm.detachCopyAll(results);
 			ds = results.size() > 0 ? results.get(0) : null;
 			return ds;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void updateDashboard(String guid, String status) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(Analysis.class);
 		q.setFilter("guid == '" + guid + "'");
 		try {
 			tx.begin();
 			List<Analysis> db = (List<Analysis>) q.execute();
 			if (db != null && db.size() > 0) {
 				Analysis analysis = db.get(0);
 				analysis.setStatus(status);
 			}
 			tx.commit();
 		} finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void invalidateViz(Analysis anls) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Visualization.class);
 		q.setFilter("analysis.id == " + anls.getId() + " && isActive == true");
 		List<Visualization> vis = (List<Visualization>) q.execute();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			for (Visualization v : vis) {
 				v.setActive(false);
 			}
 			tx.commit();
 		} finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Comment> getComments(int analysisId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Comment.class);
 		q.setFilter("analysis.id == " + analysisId);
 		q.setOrdering("createAt ascending");
 		try {
 			List<Comment> results = (List<Comment>) q.execute();
 			results = (List<Comment>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addComment(int userId, int analysisId, Comment comment) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			User user = pm.getObjectById(User.class, userId);
 			Analysis analysis = pm.getObjectById(Analysis.class, analysisId);
 			comment.setUser(user);
 			comment.setAnalysis(analysis);
 			analysis.getComments().add(comment);
 			pm.makePersistent(comment);
 			pm.makePersistent(analysis);
 			tx.commit();
 		} finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 			pm.close();
 		}
 	}
 
 	@Override
 	public void removeComment(int commentId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Comment comment = pm.getObjectById(Comment.class, commentId);
 		try {
 			tx.begin();
 			// Analysis analysis = pm.getObjectById(Analysis.class,
 			// comment.getAnalysis().getId());
 			pm.deletePersistent(comment);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void updateComment(Comment comment) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Comment getComment(int commentId) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			return pm.getObjectById(Comment.class, commentId);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public Analysis getAnalysis(int analysisId) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			Analysis anls = pm.getObjectById(Analysis.class, analysisId);
 			anls.setCommentCount(anls.getComments() != null ? anls
 					.getComments().size() : 0);
 			return anls;
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public User getUser(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			return pm.getObjectById(User.class, userId);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public CommentVote getCommentVote(int userId, int commentId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(CommentVote.class);
 		q.setFilter("user.id == " + userId + " && comment.id == " + commentId
 				+ " && isActive == true");
 		try {
 			List<CommentVote> results = (List<CommentVote>) q.execute();
 			results = (List<CommentVote>) pm.detachCopyAll(results);
 			if (results.size() > 0) {
 				CommentVote vote = results.get(0);
 				return vote;
 			} else
 				return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addCommentVote(int userId, int commentId,
 			CommentVote commentVote) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		User user = pm.getObjectById(User.class, userId);
 		Comment comment = pm.getObjectById(Comment.class, commentId);
 		if (user == null || comment == null) {
 			throw new DataRetrievalFailureException(
 					"User or Comment object not found, userId " + userId
 							+ ", commentId " + commentId);
 		}
 		try {
 			tx.begin();
 			commentVote.setUser(user);
 			commentVote.setComment(comment);
 			int vote = commentVote.getVote();
 			commentVote.setVote(vote >= 0 ? 1 : -1);
 			pm.makePersistent(commentVote);
 
 			if (vote >= 0) {
 				comment.setUpVote(comment.getUpVote() + 1);
 			} else {
 				comment.setDownVote(comment.getDownVote() + 1);
 			}
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void removeCommentVote(int userId, int commentId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(CommentVote.class);
 		q.setFilter("user.id == " + userId + " && comment.id == " + commentId
 				+ " && isActive == true");
 		List<CommentVote> results = (List<CommentVote>) q.execute();
 		try {
 			if (results.size() > 0) {
 				tx.begin();
 				CommentVote cv = results.get(0);
 				cv.setActive(false);
 				int vote = cv.getVote();
 				Comment c = cv.getComment();
 				// concurrent concern here?
 				if (vote >= 0) {
 					c.setUpVote(c.getUpVote() - 1);
 				} else {
 					c.setDownVote(c.getDownVote() - 1);
 				}
 				tx.commit();
 			}
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public AnalysisVote getAnalysisVote(int userId, int analysisId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(AnalysisVote.class);
 		q.setFilter("user.id == " + userId + " && analysis.id == " + analysisId
 				+ " && isActive == true");
 		try {
 			List<AnalysisVote> results = (List<AnalysisVote>) q.execute();
 			results = (List<AnalysisVote>) pm.detachCopyAll(results);
 			if (results.size() > 0) {
 				AnalysisVote vote = results.get(0);
 				return vote;
 			} else
 				return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addAnalysisVote(int userId, int analysisId,
 			AnalysisVote analysisVote) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		User user = pm.getObjectById(User.class, userId);
 		Analysis anls = pm.getObjectById(Analysis.class, analysisId);
 		try {
 			tx.begin();
 			analysisVote.setUser(user);
 			analysisVote.setAnalysis(anls);
 			int vote = analysisVote.getVote();
 			analysisVote.setVote(vote >= 0 ? 1 : -1);
 			pm.makePersistent(analysisVote);
 
 			if (vote >= 0) {
 				anls.setScore(anls.getScore() + 1);
 			} else {
 				anls.setScore(anls.getScore() - 1);
 			}
 
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void removeAnalysisVote(int userId, int analysisId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		Query q = pm.newQuery(AnalysisVote.class);
 		q.setFilter("user.id == " + userId + " && analysis.id == " + analysisId
 				+ " && isActive == true");
 		List<AnalysisVote> results = (List<AnalysisVote>) q.execute();
 		try {
 			if (results.size() > 0) {
 				tx.begin();
 				AnalysisVote av = results.get(0);
 				av.setActive(false);
 				int vote = av.getVote();
 				Analysis anls = av.getAnalysis();
 				// concurrent concern here?
 				if (vote >= 0) {
 					anls.setScore(anls.getScore() - 1);
 				} else {
 					anls.setScore(anls.getScore() + 1);
 				}
 				tx.commit();
 			}
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public Activity getActivity(int activityId) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			return pm.getObjectById(Activity.class, activityId);
 		} finally {
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void createActivity(Activity activity) {
 		if (activity.getUser() == null) {
 			return;
 		}
 		PersistenceManager pm = getPersistenceManager();
 		User user = pm.getObjectById(User.class, activity.getUser().getId());
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			activity.setUser(user);
 			pm.makePersistent(activity);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void removeActivity(int activityId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Activity activity = pm.getObjectById(Activity.class, activityId);
 			pm.deletePersistent(activity);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void updateActivity(Activity activity) {
 		PersistenceManager pm = getPersistenceManager();
 		Activity pstObj = pm.getObjectById(Activity.class, activity.getId());
 		if (pstObj == null) {
 			// throw an custom exception?
 			return;
 		}
 
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			pstObj.setObjectId(activity.getObjectId());
 			pstObj.setAction(activity.getAction());
 			pstObj.setTime(activity.getTime());
 			pstObj.setPublic(activity.isPublic());
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public List<Activity> getUserActitity(int userId, Date cutPoint) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Activity.class);
 		q.setFilter("user.id == " + userId + " && time >= cut_point");
 		q.declareImports("java.util.Date");
 		q.declareParameters("Date cut_point");
 		q.setOrdering("time DESC");
 		try {
 			List<Activity> activities = (List<Activity>) q.execute(cutPoint);
 			activities = (List<Activity>) pm.detachCopyAll(activities);
 			return activities;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<User> getFollowers(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("user.id == " + userId);
 		try {
 			List<Following> followings = (List<Following>) q.execute();
 			followings = (List<Following>) pm.detachCopyAll(followings);
 			List<User> results = new ArrayList<User>();
 			for (Following f : followings) {
 				results.add(f.getFollower());
 			}
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<User> getFollowingUsers(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("follower.id == " + userId);
 		try {
 			List<Following> followings = (List<Following>) q.execute();
 			followings = (List<Following>) pm.detachCopyAll(followings);
 			List<User> results = new ArrayList<User>();
 			for (Following f : followings) {
 				results.add(f.getUser());
 			}
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	// public List<Activity> getFeed(int userId, Date cutPoint, int maxNumber) {
 	public List<Activity> getFeed(int userId, int maxNumber) {
 		PersistenceManager pm = getPersistenceManager();
 		// get list of feed (now, is list of following users)
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("follower.id == " + userId);
 		try {
 			List<Following> followings = (List<Following>) q.execute();
 
 			// query from the 'activities' table, with condition: actor is in
 			// the above list
 			q = pm.newQuery(Activity.class);
 			StringBuilder filter = new StringBuilder();
 			filter.append("(");
 			for (Following f : followings) {
 				filter.append("user.id == " + f.getUser().getId() + " ||");
 			}
 
 			filter.append(" user.id == " + userId + ")");
 			List<Activity> activities = null;
 			/*
 			 * if (cutPoint != null) { q.setFilter(filter.toString() +
 			 * " && (time >= cut_point) && isPublic == true");
 			 * q.declareImports("import java.util.Date");
 			 * q.declareParameters("Date cut_point");
 			 * q.setOrdering("time DESC"); if (maxNumber > 0) { q.setRange(0,
 			 * maxNumber); } else q.setRange(0, 20); activities =
 			 * (List<Activity>) q.execute(cutPoint); activities =
 			 * (List<Activity>) pm.detachCopyAll(activities); } else {
 			 * q.setFilter("isPublic == true"); q.setOrdering("time DESC");
 			 * q.setRange(0, maxNumber); activities = (List<Activity>)
 			 * q.execute(cutPoint); activities = (List<Activity>)
 			 * pm.detachCopyAll(activities); }
 			 */
 
 			filter.append(" && isPublic == true");
 			q.setFilter(filter.toString());
 			q.setOrdering("time DESC");
 			q.setRange(0, maxNumber);
 			activities = (List<Activity>) q.execute();
 			activities = (List<Activity>) pm.detachCopyAll(activities);
 
 			// set the target object
 			for (Activity ac : activities) {
 				try {
 					Analysis anls = pm.getObjectById(Analysis.class,
 							ac.getObjectId());
 					// List<Comment> comments = getComments(ac.getObjectId());
 					List<Comment> comments = anls.getComments();
 					anls.setCommentCount(comments != null ? comments.size() : 0);
 					ac.setObject(anls);
 				} catch (Exception e) {
 					logger.debug("", e);
 					continue;
 				}
 			}
 			return activities;
 
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	public List<Activity> getMoreFeed(int userId, List<Following> followings,
 			int lastId, int maxNumber) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Activity.class);
 		StringBuilder filter = new StringBuilder();
 		filter.append("(");
 		for (Following f : followings) {
 			filter.append("user.id == " + f.getUser().getId() + " ||");
 		}
 
 		filter.append(" user.id == " + userId + ")");
 		q.setFilter("id < " + lastId + " && isPublic == true");
 		q.setOrdering("time DESC");
 		q.setRange(0, maxNumber);
 		try {
 			List<Activity> activities = (List<Activity>) q.execute();
 			activities = (List<Activity>) pm.detachCopyAll(activities);
 
 			// set the target object
 			Iterator<Activity> iter = activities.iterator();
 			while (iter.hasNext()) {
 				Activity ac = iter.next();
 				try {
 					Analysis anls = pm.getObjectById(Analysis.class,
 							ac.getObjectId());
 					List<Comment> comments = getComments(ac.getObjectId());
 					anls.setCommentCount(comments != null ? comments.size() : 0);
 					ac.setObject(anls);
 				} catch (Exception e) {
 					iter.remove();
 				}
 			}
 			return activities;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Following> getFollowingList(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("user.id == " + userId);
 		try {
 			List<Following> followings = (List<Following>) q.execute();
 			followings = (List<Following>) pm.detachCopyAll(followings);
 			return followings;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<User> findFriends(User finder, String query) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(User.class);
 		q.setFilter("(this.username !=\"" + finder.getUsername()
 				+ "\") && (this.username.matches(\".*" + query
 				+ ".*\") || this.firstName.matches(\".*" + query + ".*\")"
 				+ " || this.lastName.matches(\".*" + query
 				+ ".*\") || this.email.matches(\".*" + query + ".*\"))");
 		try {
 			List<User> results = (List<User>) q.execute();
 			results = (List<User>) pm.detachCopyAll(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createFollowing(int follower, int target) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		User followerUser = pm.getObjectById(User.class, follower);
 		User targetUser = pm.getObjectById(User.class, target);
 		if (followerUser == null || targetUser == null)
 			return;
 		Following f = new Following(targetUser, followerUser, new Date());
 		try {
 			tx.begin();
 			pm.makePersistent(f);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void removeFollowing(int follower, int target) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		User followerUser = pm.getObjectById(User.class, follower);
 		User targetUser = pm.getObjectById(User.class, target);
 		if (followerUser == null || targetUser == null)
 			return;
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("user.id==" + target + " && follower.id==" + follower);
 		try {
 			tx.begin();
 			q.deletePersistentAll();
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public boolean isFollowing(int follower, int target) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Following.class);
 		q.setFilter("user.id==" + target + " && follower.id==" + follower);
 		try {
 			List<Following> results = (List<Following>) q.execute();
 			return (results != null && results.size() > 0);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createDataset(Dataset dataset) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		User user = pm.getObjectById(User.class, dataset.getUser().getId());
 		dataset.setUser(user);
 		Scraper scraper = null;
 		if (dataset.getScraper() != null) {
 			scraper = pm.getObjectById(Scraper.class, dataset.getScraper()
 					.getId());
 			dataset.setScraper(scraper);
 			scraper.getDatasets().add(dataset);
 		} else {
 			dataset.setScraper(null);
 		}
 		try {
 			tx.begin();
 			pm.makePersistent(dataset);
 			if (scraper != null)
 				pm.makePersistent(scraper);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createDatasets(List<Dataset> datasets) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		List<Scraper> scrapers = new ArrayList<Scraper>();
 		for (Dataset ds : datasets) {
 			User user = pm.getObjectById(User.class, ds.getUser().getId());
 			ds.setUser(user);
 			if (ds.getScraper() != null) {
 				Scraper scraper = pm.getObjectById(Scraper.class, ds
 						.getScraper().getId());
 				ds.setScraper(scraper);
 				scraper.getDatasets().add(ds);
 				scrapers.add(scraper);
 			} else
 				ds.setScraper(null);
 		}
 		try {
 			tx.begin();
 			pm.makePersistentAll(datasets);
 			pm.makePersistentAll(scrapers);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void invalidateDataset(Scraper scraper) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Dataset.class);
 		q.setFilter("scraper.id == " + scraper.getId() + " && isActive == true");
 		List<Dataset> datasets = (List<Dataset>) q.execute();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			/*
 			 * for (Dataset d : datasets) { d.setActive(false); }
 			 */
 			pm.deletePersistentAll(datasets);
 			tx.commit();
 		} finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createBcDataScript(BcDataScript script) {
 		PersistenceManager pm = getPersistenceManager();
 		User user = pm.getObjectById(User.class, script.getUser().getId());
 		Transaction tx = pm.currentTransaction();
 		script.setUser(user);
 		Set<Tag> tags = script.getTags();
 		if (tags != null) {
 			Set<Tag> tagSet = new HashSet<Tag>();
 			for (Tag t : tags) {
 				if (t.getId() > 0) {
 					Tag tag = pm.getObjectById(Tag.class, t.getId());
 					tagSet.add(tag);
 				} else {
 					tagSet.add(t);
 				}
 			}
 			script.setTags(tagSet);
 		}
 
 		try {
 			tx.begin();
 			pm.makePersistent(script);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void updateBcDataScript(BcDataScript script) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		BcDataScript scr = null;
 		if (script instanceof Analysis) {
 			scr = pm.getObjectById(Analysis.class, script.getId());
 		} else if (script instanceof Scraper) {
 			scr = pm.getObjectById(Scraper.class, script.getId());
 		} else
 			return;
 
 		try {
 			tx.begin();
 			scr.setName(script.getName());
 			scr.setDescription(script.getDescription());
 			scr.setLanguage(script.getLanguage());
 			scr.setLineCount(script.getLineCount());
 			scr.setPublished(script.isPublished());
 			scr.setGuid(script.getGuid());
 			scr.setLastUpdate(new Date());
 			// scr.setTags(script.getTags());
 			scr.setExecuted(script.isExecuted());
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void deleteBcDataScript(int scriptId, String type) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		BcDataScript scr;
 		if ("analysis".equals(type)) {
 			scr = pm.getObjectById(Analysis.class, scriptId);
 		} else if ("scraper".equals(type)) {
 			scr = pm.getObjectById(Scraper.class, scriptId);
 		} else
 			return;
 		try {
 			tx.begin();
 			pm.deletePersistent(scr);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public Scraper getScraperByGuid(String guid) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Scraper.class);
 		q.setFilter("guid == \"" + guid + "\"");
 		Scraper scraper = null;
 		try {
 			List<Scraper> results = (List<Scraper>) q.execute();
 			if (results.size() > 0) {
 				scraper = results.get(0);
 				scraper = pm.detachCopy(scraper);
 			}
 			return scraper;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Dataset> getScraperDatasets(int scraperId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Dataset.class);
 		q.setFilter("scraper.id == " + scraperId + " && isActive == true");
 		try {
 			List<Dataset> datasets = (List<Dataset>) q.execute();
 			datasets = (List<Dataset>) pm.detachCopyAll(datasets);
 			return datasets;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Scraper> getScraperList(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Scraper.class);
 		q.setFilter("user.id == " + userId);
 		try {
 			List<Scraper> scrapers = (List<Scraper>) q.execute();
 			return (List<Scraper>) pm.detachCopyAll(scrapers);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Scraper> getPublicScrapers(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Scraper.class);
 		q.setFilter("user.id == " + userId + " && isPublished == true");
 		try {
 			List<Scraper> scrapers = (List<Scraper>) q.execute();
 			return (List<Scraper>) pm.detachCopyAll(scrapers);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public Dataset getDatasetByName(String identifier) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Dataset.class);
 		q.setFilter("name == \"" + identifier + "\" && isActive == true");
 		Dataset dataset = null;
 		try {
 			List<Dataset> datasets = (List<Dataset>) q.execute();
 			if (datasets.size() > 0) {
 				dataset = datasets.get(0);
 				dataset = pm.detachCopy(dataset);
 			}
 			return dataset;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void invalidateDatasets(Analysis analysis) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(AnalysisDataset.class);
 		q.setFilter("analysis.id == " + analysis.getId()
 				+ " && isActive == true");
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			List<AnalysisDataset> anlsDts = (List<AnalysisDataset>) q.execute();
 			for (AnalysisDataset item : anlsDts) {
 				item.setActive(false);
 			}
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createAnalysisDatasets(List<AnalysisDataset> anlsDts) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			for (AnalysisDataset item : anlsDts) {
 				Analysis anls = pm.getObjectById(Analysis.class, item
 						.getAnalysis().getId());
 				Dataset dts = pm.getObjectById(Dataset.class, item.getDataset()
 						.getId());
 				item.setAnalysis(anls);
 				item.setDataset(dts);
 			}
 			pm.makePersistentAll(anlsDts);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void createDataCollection(DataCollection collection) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 
 		try {
 			tx.begin();
 			DataCollection dataCollection = new DataCollection();
 			dataCollection.setName(collection.getName());
 			dataCollection.setDescription(collection.getDescription());
 			List<Dataset> datasets = new ArrayList<Dataset>();
 			dataCollection.setDatasets(datasets);
 			if (collection.getDatasets() != null) {
 				for (Dataset ds : collection.getDatasets()) {
 					Dataset dataset = pm.getObjectById(Dataset.class,
 							ds.getId());
 					if (dataset != null)
 						datasets.add(dataset);
 				}
 			}
 			pm.makePersistent(collection);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void deleteDataCollection(int collectionId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 
 		try {
 			tx.begin();
 			DataCollection collection = pm.getObjectById(DataCollection.class,
 					collectionId);
 			if (collection != null)
 				pm.deletePersistent(collection);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void updateDataCollection(DataCollection collection) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 
 		try {
 			tx.begin();
 			DataCollection dataCol = pm.getObjectById(DataCollection.class,
 					collection.getId());
 			if (dataCol != null) {
 				List<Dataset> datasets = new ArrayList<Dataset>();
 				dataCol.setName(collection.getName());
 				dataCol.setDescription(collection.getDescription());
 				dataCol.setDatasets(datasets);
 				if (collection.getDatasets() != null) {
 					for (Dataset ds : collection.getDatasets()) {
 						Dataset dataset = pm.getObjectById(Dataset.class,
 								ds.getId());
 						if (dataset != null)
 							datasets.add(dataset);
 					}
 				}
 			}
 
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public DataCollection getDataCollection(int collectionId) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			return pm.getObjectById(DataCollection.class, collectionId);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<DataCollection> getUserCollections(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(DataCollection.class);
 		q.setFilter("this.user.id == " + userId);
 		try {
 			List<DataCollection> dataCol = (List<DataCollection>) q.execute();
 			dataCol = (List<DataCollection>) pm.detachCopyAll(dataCol);
 			return dataCol;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void deleteUserCollections(int userId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(DataCollection.class);
 		q.setFilter("this.user.id == " + userId);
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			List<DataCollection> dataCol = (List<DataCollection>) q.execute();
 			if (dataCol != null)
 				pm.deletePersistentAll(dataCol);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addDatasetToCollection(int datasetId, int collectionId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			DataCollection collection = pm.getObjectById(DataCollection.class,
 					collectionId);
 			Dataset dataset = pm.getObjectById(Dataset.class, datasetId);
 			if (collection == null || dataset == null)
 				return;
 			if (collection.getDatasets() == null)
 				collection.setDatasets(new ArrayList<Dataset>());
 			collection.getDatasets().add(dataset);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Analysis> getMostPopularAnalyses() {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Analysis.class);
 		q.setOrdering("score DESC");
 		try {
			List<Analysis> scrapers = (List<Analysis>) q.execute();
			scrapers = scrapers.subList(0, 10);
			return (List<Analysis>) pm.detachCopyAll(scrapers);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public List<Dataset> getMostPopularDatasets() {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Dataset.class);
 		q.setOrdering("rowCount DESC");
 		try {
 			List<Dataset> datasets = (List<Dataset>) q.execute();
			datasets = datasets.subList(0, 10);
			return (List<Dataset>) pm.detachCopyAll(datasets);
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
   
   @Override
   public void addAnalysisTags(int anlsId, List<Tag> tags) {
     PersistenceManager pm = getPersistenceManager();
     Transaction tx = pm.currentTransaction();
     try {
       tx.begin();
       Analysis anls = pm.getObjectById(Analysis.class, anlsId);
       Set<Tag> tagset = anls.getTags();
       if (tagset == null) {
         tagset = new HashSet<Tag>();
         anls.setTags(tagset);
       }
       for (Tag tag : tags) {
         Tag tagObj = pm.getObjectById(Tag.class, tag.getId());
         tagset.add(tagObj);
       }
       pm.makePersistent(anls);
       tx.commit();
     } finally {
       if (tx.isActive()) tx.rollback();
       pm.close();
     }
   }
   
   @Override
   public boolean hasTag(int anlsId, String tag) {
     PersistenceManager pm = getPersistenceManager();
     Analysis anls = pm.getObjectById(Analysis.class, anlsId);
     Set<Tag> tagset = anls.getTags();
     if (tagset == null) return false;
     for (Tag t : tagset) {
       if (t.getTag().equals(tag)) return true;
     }
     return false;
     
   }
 
 	@Override
 	public void createTag(Tag tag) {
 		List<Tag> tags = new ArrayList<Tag>();
 		tags.add(tag);
 		persistData(tags);
 	}
 
 	public Tag getTag(String tagStr) {
 		PersistenceManager pm = getPersistenceManager();
 		Query q = pm.newQuery(Tag.class);
 		q.setFilter("tag == \"" + tagStr + "\"");
 		Tag tag = null;
 		try {
 			List<Tag> tags = (List<Tag>) q.execute();
 			tag = tags.size() > 0 ? tags.get(0) : null;
 			return tag;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addAnalysisTag(int anlsId, int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Analysis anls = pm.getObjectById(Analysis.class, anlsId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			anls.getTags().add(tag);
 			pm.makePersistent(anls);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void addScraperTag(int scId, int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Scraper sc = pm.getObjectById(Scraper.class, scId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			sc.getTags().add(tag);
 			pm.makePersistent(sc);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public void deleteAnalysisTag(int anlsId, int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Analysis anls = pm.getObjectById(Analysis.class, anlsId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			anls.getTags().remove(tag);
 			pm.makePersistent(anls);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public List<Analysis> getAnalysisByTag(int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query query = pm
 				.newQuery(
 						"javax.jdo.query.SQL",
 						"SELECT t1 . * FROM analyses t1 INNER JOIN Analysis_tags t2 ON t1.id = t2.id_OID WHERE t2.id_EID = :param");
 		Map params = new HashMap();
 		params.put("param", tagId);
 		List<Analysis> results = (List<Analysis>) query.executeWithMap(params);
 		System.out.println("Size : " + results.size());
 		return results;
 
 	}
 
 	@Override
 	public Set<Tag> getTagByAnalysis(int anlsId) {
 		Set<Tag> results = null;
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Analysis anls = pm.getObjectById(Analysis.class, anlsId);
 			results = anls.getTags();
 			System.out.println("Size : " + results.size());
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 		return results;
 	}
 
 	@Override
 	public void deleteScraperTag(int scId, int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Scraper scp = pm.getObjectById(Scraper.class, scId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			scp.getTags().remove(tag);
 			pm.makePersistent(scp);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public Set<Tag> getTagByScraper(int scId) {
 		Set<Tag> results = null;
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Scraper scp = pm.getObjectById(Scraper.class, scId);
 			results = scp.getTags();
 			System.out.println("Size : " + results.size());
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 		return results;
 	}
 
 	@Override
 	public List<Scraper> getScraperByTag(int tagId) {
 		PersistenceManager pm = getPersistenceManager();
 		Query query = pm
 				.newQuery(
 						"javax.jdo.query.SQL",
 						"SELECT t1 . * FROM scrapers t1 INNER JOIN Scraper_tags t2 ON t1.id = t2.id_OID WHERE t2.id_EID = :param");
 		Map params = new HashMap();
 		params.put("param", tagId);
 		List<Scraper> results = (List<Scraper>) query.executeWithMap(params);
 		System.out.println("Size : " + results.size());
 		return results;
 	}
 
 	@Override
 	public void addDataSetTag(int dsId, int tagId) {
 
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Dataset ds = pm.getObjectById(Dataset.class, dsId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			ds.getTags().add(tag);
 
 			pm.makePersistent(ds);
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 	}
 
 	@Override
 	public void deleteDataSetTag(int dsId, int tagId) {
 
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Dataset dts = pm.getObjectById(Dataset.class, dsId);
 			Tag tag = pm.getObjectById(Tag.class, tagId);
 			dts.getTags().remove(tag);
 			pm.makePersistent(dts);
 
 			User user = dts.getUser();
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 
 	}
 
 	@Override
 	public Set<Tag> getTagByDataSet(int dsId) {
 
 		Set<Tag> results = null;
 		PersistenceManager pm = getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			Dataset dts = pm.getObjectById(Dataset.class, dsId);
 			results = dts.getTags();
 			System.out.println("Size : " + results.size());
 
 			User user = dts.getUser();
 
 			tx.commit();
 		} finally {
 			if (tx.isActive())
 				tx.rollback();
 			pm.close();
 		}
 		return results;
 	}
 
 	@Override
 	public List<Dataset> getDataSetByTag(int tagId) {
 
 		PersistenceManager pm = getPersistenceManager();
 		Query query = pm
 				.newQuery(
 						"javax.jdo.query.SQL",
 						"SELECT t1 . * FROM datasets t1 INNER JOIN Dataset_tags t2 ON t1.id = t2.id_OID WHERE t2.id_EID = :param");
 		Map params = new HashMap();
 		params.put("param", tagId);
 		List<Dataset> results = (List<Dataset>) query.executeWithMap(params);
 		System.out.println("Size : " + results.size());
 
 		return results;
 	}
 
   @Override
   public void createTags(List<Tag> tags) {
     persistData(tags);
   }
 
 }
