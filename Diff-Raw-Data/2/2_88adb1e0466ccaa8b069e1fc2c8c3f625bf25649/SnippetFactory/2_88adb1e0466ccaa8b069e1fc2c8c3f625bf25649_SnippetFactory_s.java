 /**
  * File: SnippetFactory.java
  * Date: 11.05.2012
  */
 package org.smartsnip.persistence.hibernate;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.search.FullTextQuery;
 import org.hibernate.search.FullTextSession;
 import org.hibernate.search.Search;
 import org.hibernate.search.SearchException;
 import org.hibernate.search.query.dsl.QueryBuilder;
 import org.smartsnip.core.Category;
 import org.smartsnip.core.Snippet;
 import org.smartsnip.core.Tag;
 import org.smartsnip.core.User;
 import org.smartsnip.persistence.IPersistence;
 import org.smartsnip.shared.Pair;
 
import com.sun.xml.internal.stream.Entity;

 /**
  * @author Gerhard Aigner
  * 
  */
 public class SnippetFactory {
 
 	private SnippetFactory() {
 		// no instances
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#writeSnippet(Snippet, int)}
 	 * 
 	 * @param snippet
 	 * @param flags
 	 * @return the Id
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#writeSnippet(org.smartsnip.core.Snippet,
 	 *      int)
 	 */
 	static Long writeSnippet(Snippet snippet, int flags) throws IOException {
 		Session session = DBSessionFactory.open();
 		Long result;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			DBQuery licQuery;
 			DBLicense dblicense;
 
 			DBSnippet entity = new DBSnippet();
 			entity.setSnippetId(snippet.getHashId()); // snippetId is read-only
 			entity.setHeadline(snippet.getName());
 			entity.setDescription(snippet.getDescription());
 			entity.setViewcount(snippet.getViewcount());
 			// lastEdited is read-only
 			entity.setOwner(snippet.getOwnerUsername());
 			entity.setCategoryId(CategoryFactory
 					.fetchCategory(session, snippet).getCategoryId());
 
 			dblicense = new DBLicense();
 			licQuery = new DBQuery(session);
 			dblicense.setShortDescr(snippet.getLicense());
 			dblicense = licQuery.fromSingle(dblicense, DBQuery.QUERY_NULLABLE);
 			if (dblicense != null) {
 				entity.setLicenseId(dblicense.getLicenseId());
 			}
 			result = (Long) query.write(entity, flags);
 
 			// allow new tags even on present flag IPersistence.DB_UPDATE_ONLY
 			// skip existing tags even on present flag IPersistence.DB_NEW_ONLY
 			TagFactory
 					.pushTags(
 							session,
 							snippet,
 							flags
 									& ~(IPersistence.DB_UPDATE_ONLY | IPersistence.DB_NEW_ONLY));
 
 			// allow new tag-snippet relationships even if
 			// IPersistence.DB_UPDATE_ONLY flag is present
 			TagFactory.updateRelTagSnippet(session, result, snippet.getTags(),
 					flags & ~(IPersistence.DB_UPDATE_ONLY));
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#writeSnippet(List, int)}
 	 * 
 	 * @param snippets
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#writeSnippet(java.util.List,
 	 *      int)
 	 */
 	static void writeSnippet(List<Snippet> snippets, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query;
 			DBQuery licQuery;
 			DBLicense dblicense;
 			DBSnippet entity;
 			Long snippetId;
 
 			for (Snippet snippet : snippets) {
 				query = new DBQuery(session);
 				entity = new DBSnippet();
 				// snippetId is read-only
 				entity.setHeadline(snippet.getName());
 				entity.setDescription(snippet.getDescription());
 				entity.setViewcount(snippet.getViewcount());
 				// lastEdited is read-only
 				entity.setOwner(snippet.getOwnerUsername());
 				entity.setCategoryId(new Long(snippet.getCategory().hashCode()));
 
 				dblicense = new DBLicense();
 				licQuery = new DBQuery(session);
 				dblicense.setShortDescr(snippet.getLicense());
 				dblicense = licQuery.fromSingle(dblicense,
 						DBQuery.QUERY_NULLABLE);
 				entity.setLicenseId(dblicense.getLicenseId());
 
 				snippetId = (Long) query.write(entity, flags);
 
 				// allow new tags even if IPersistence.DB_UPDATE_ONLY flag is
 				// present
 				// skip existing tags even if IPersistence.DB_NEW_ONLY flag is
 				// present
 				TagFactory
 						.pushTags(
 								session,
 								snippet,
 								flags
 										& ~(IPersistence.DB_UPDATE_ONLY | IPersistence.DB_NEW_ONLY));
 
 				// allow new tag-snippet relationships even if
 				// IPersistence.DB_UPDATE_ONLY flag is present
 				TagFactory.updateRelTagSnippet(session, snippetId,
 						snippet.getTags(), flags
 								& ~(IPersistence.DB_UPDATE_ONLY));
 			}
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#writeLicense(java.lang.String, java.lang.String, int)}
 	 * 
 	 * @param shortDescription
 	 * @param fullText
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#writeLicense(java.lang.String,
 	 *      java.lang.String, int)
 	 */
 	static void writeLicense(String shortDescription, String fullText, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			DBLicense entity = new DBLicense();
 			entity.setShortDescr(shortDescription);
 			entity.setLicenseText(fullText);
 
 			query.write(entity, flags);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#writeRating(Integer, Snippet, User, int)}
 	 * 
 	 * @param rating
 	 * @param snippet
 	 * @param user
 	 * @param flags
 	 * @return the updated average rating
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#writeRating(java.lang.Integer,
 	 *      org.smartsnip.core.Snippet, org.smartsnip.core.User, int)
 	 */
 	static Float writeRating(Integer rating, Snippet snippet, User user,
 			int flags) throws IOException {
 		Session session = DBSessionFactory.open();
 		Float oldRating, ratingCount;
 		Float overwritten = 0F;
 		Float oneIfOverwritten = 0F;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			// get the old rating
 			DBSnippet snip = new DBSnippet();
 			snip.setSnippetId(snippet.getHashId());
 			oldRating = (Float) query.selectSingle(snip, "ratingAverage",
 					DBQuery.QUERY_NOT_NULL | DBQuery.QUERY_UNIQUE_RESULT);
 
 			// get the number of ratings
 			query.reset();
 			DBRating entity = new DBRating();
 			entity.setRatingId(snippet.getHashId(), null);
 			query.addWhereParameter("value >", "value", "", 0);
 			List<DBRating> ratings = query
 					.from(entity, IPersistence.DB_DEFAULT);
 			ratingCount = new Integer(ratings.size()).floatValue();
 
 			// get the rating which will be overwritten (if present)
 			query.reset();
 			entity.setRatingId(snippet.getHashId(), user.getUsername());
 			DBRating oldEntity = query.fromSingle(entity,
 					DBQuery.QUERY_NULLABLE | DBQuery.QUERY_UNIQUE_RESULT);
 			if (oldEntity != null && oldEntity.getValue() > 0F) {
 				overwritten = oldEntity.getValue().floatValue();
 				oneIfOverwritten = 1F;
 			}
 
 			// write the new rating
 			query.reset();
 			entity.setValue(rating);
 
 			query.write(entity, flags);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return ((ratingCount * oldRating) + rating.floatValue() - overwritten)
 				/ (ratingCount + 1F - oneIfOverwritten);
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#unRate(User, Snippet, int)}
 	 * 
 	 * @param user
 	 * @param snippet
 	 * @param flags
 	 * @return the updated average rating
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#unRate(org.smartsnip.core.User,
 	 *      org.smartsnip.core.Snippet, int)
 	 */
 	static Float unRate(User user, Snippet snippet, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 		Float oldRating, ratingCount;
 		Float overwritten = 0F;
 		Float oneIfOverwritten = 0F;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			// get the old rating
 			DBSnippet snip = new DBSnippet();
 			snip.setSnippetId(snippet.getHashId());
 			oldRating = (Float) query.selectSingle(snip, "ratingAverage",
 					DBQuery.QUERY_NOT_NULL | DBQuery.QUERY_UNIQUE_RESULT);
 
 			// get the number of ratings
 			DBRating entity = new DBRating();
 			entity.setRatingId(snippet.getHashId(), null);
 			query.reset();
 			query.addWhereParameter("value >", "value", "", 0);
 			List<DBRating> ratings = query
 					.from(entity, IPersistence.DB_DEFAULT);
 			ratingCount = new Integer(ratings.size()).floatValue();
 
 			// get the rating which will be overwritten (if present)
 			query.reset();
 			entity.setRatingId(snippet.getHashId(), user.getUsername());
 			DBRating oldEntity = query.fromSingle(entity,
 					DBQuery.QUERY_NULLABLE | DBQuery.QUERY_UNIQUE_RESULT);
 			if (oldEntity != null && oldEntity.getValue() > 0F) {
 				overwritten = oldEntity.getValue().floatValue();
 				oneIfOverwritten = 1F;
 			}
 
 			// remove the rating
 			query.reset();
 			if (oldEntity != null) {
 				query.remove(entity, flags);
 			}
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return ((ratingCount * oldRating) - overwritten)
 				/ (ratingCount - oneIfOverwritten);
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#addFavourite(Snippet, User, int)}
 	 * 
 	 * @param snippet
 	 * @param user
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#addFavourite(org.smartsnip.core.Snippet,
 	 *      org.smartsnip.core.User, int)
 	 */
 	static void addFavourite(Snippet snippet, User user, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			DBFavourite entity = new DBFavourite();
 			entity.setFavouriteId(user.getUsername(), snippet.getHashId());
 			entity.setFavourite(true);
 
 			query.write(entity, flags);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#removeFavourite(Snippet, User, int)}
 	 * 
 	 * @param snippet
 	 * @param user
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#removeFavourite(org.smartsnip.core.Snippet,
 	 *      org.smartsnip.core.User, int)
 	 */
 	static void removeFavourite(Snippet snippet, User user, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			DBFavourite entity = new DBFavourite();
 			entity.setFavouriteId(user.getUsername(), snippet.getHashId());
 
 			query.remove(entity, flags);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#removeSnippet(Snippet, int)}
 	 * 
 	 * @param snippet
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#removeSnippet(org.smartsnip.core.Snippet,
 	 *      int)
 	 */
 	static void removeSnippet(Snippet snippet, int flags) throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			DBSnippet entity = new DBSnippet();
 			entity.setSnippetId(snippet.getHashId());
 
 			// delete unused tags on flag DB_FORCE_DELETE
 			// step 1: fetch possible candidates
 			List<String> tagNames = null;
 			if (DBQuery.hasFlag(flags, IPersistence.DB_FORCE_DELETE)) {
 				tagNames = new ArrayList<String>();
 				DBRelTagSnippet tagRef = new DBRelTagSnippet();
 				tagRef.setTagSnippetId(snippet.getHashId(), null);
 				for (Iterator<DBRelTagSnippet> itr = query.iterate(tagRef,
 						IPersistence.DB_DEFAULT); itr.hasNext();) {
 					tagNames.add(itr.next().getTagSnippetId().getTagName());
 				}
 				query.reset();
 			}
 
 			// delete the snippet. Code, ratings, comments, votes and
 			// relationships to the tags
 			// are removed through DB constraints automatically
 			query.remove(entity, flags);
 
 			// delete unused tags on flag DB_FORCE_DELETE
 			// step 2: remove orphaned tags from DB
 			if (DBQuery.hasFlag(flags, IPersistence.DB_FORCE_DELETE)) {
 				DBTag tag = new DBTag();
 				for (String name : tagNames) {
 					tag.setName(name);
 					query.reset();
 					query.remove(tag, flags);
 				}
 			}
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#removeLicense(String, int)}
 	 * 
 	 * @param shortDescription
 	 * @param flags
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#removeLicense(String,
 	 *      int)
 	 */
 	static void removeLicense(String shortDescription, int flags)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			DBLicense entity = new DBLicense();
 			entity.setShortDescr(shortDescription);
 
 			query.remove(entity, flags);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getUserSnippets(User)}
 	 * 
 	 * @param owner
 	 * @return a list of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getUserSnippets(org.smartsnip.core.User)
 	 */
 	static List<Snippet> getUserSnippets(User owner) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		List<Snippet> result = new ArrayList<Snippet>();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			DBSnippet entity = new DBSnippet();
 			Snippet snippet;
 			entity.setOwner(owner.getUsername());
 
 			for (Iterator<DBSnippet> iterator = query.iterate(entity,
 					DBQuery.QUERY_CACHEABLE); iterator.hasNext();) {
 				entity = iterator.next();
 
 				snippet = helper.createSnippet(entity.getSnippetId(), owner
 						.getUsername(), entity.getHeadline(), entity
 						.getDescription(),
 						CategoryFactory.fetchCategory(session, entity)
 								.getName(), TagFactory.fetchTags(helper,
 								session, entity.getSnippetId()),
 						CommentFactory.fetchCommentIds(session,
 								entity.getSnippetId()),
 						fetchLicense(helper, session, entity).getShortDescr(),
 						entity.getViewcount(), entity.getRatingAverage());
 				helper.setCodeOfSnippet(snippet,
 						CodeFactory.fetchNewestCode(helper, session, snippet));
 				result.add(snippet);
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getFavorited(User)}
 	 * 
 	 * @param user
 	 * @return a list of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getFavorited(org.smartsnip.core.User)
 	 */
 	static List<Snippet> getFavorited(User user) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		List<Snippet> result = new ArrayList<Snippet>();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			DBFavourite entity = new DBFavourite();
 			entity.setFavouriteId(user.getUsername(), null);
 			entity.setFavourite(true);
 
 			DBSnippet snip;
 			DBQuery snipQuery;
 			for (Iterator<DBFavourite> iterator = query.iterate(entity,
 					IPersistence.DB_DEFAULT); iterator.hasNext();) {
 				entity = iterator.next();
 				snip = new DBSnippet();
 				Snippet snippet;
 				snip.setSnippetId(entity.getFavouriteId().getSnippetId());
 				snipQuery = new DBQuery(session);
 				snip = snipQuery.fromSingle(snip, DBQuery.QUERY_NOT_NULL);
 
 				snippet = helper.createSnippet(
 						snip.getSnippetId(),
 						snip.getOwner(),
 						snip.getHeadline(),
 						snip.getDescription(),
 						CategoryFactory.fetchCategory(session, snip).getName(),
 						TagFactory.fetchTags(helper, session,
 								snip.getSnippetId()),
 						CommentFactory.fetchCommentIds(session,
 								snip.getSnippetId()),
 						fetchLicense(helper, session, snip).getShortDescr(),
 						snip.getViewcount(), snip.getRatingAverage());
 				helper.setCodeOfSnippet(snippet,
 						CodeFactory.fetchNewestCode(helper, session, snippet));
 				result.add(snippet);
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getSnippets(List)}
 	 * 
 	 * @param matchingTags
 	 * @return a list of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getSnippets(java.util.List)
 	 */
 	static List<Snippet> getSnippets(List<Tag> matchingTags) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		List<Snippet> result = new ArrayList<Snippet>();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query;
 			DBRelTagSnippet entity;
 			DBSnippet snip;
 			Snippet snippet;
 			Set<Tag> tags = new TreeSet<Tag>(matchingTags);
 			Set<Long> snippetIds = new TreeSet<Long>();
 
 			// fetch all relationships of tags
 			for (Tag tag : tags) {
 				query = new DBQuery(session);
 				entity = new DBRelTagSnippet();
 				entity.setTagSnippetId(null, tag.toString());
 				for (Iterator<DBRelTagSnippet> itr = query.iterate(entity,
 						DBQuery.QUERY_CACHEABLE); itr.hasNext();) {
 					snippetIds.add(itr.next().getTagSnippetId().getSnippetId());
 				}
 			}
 
 			for (Long id : snippetIds) {
 				snip = new DBSnippet();
 				snip.setSnippetId(id);
 				query = new DBQuery(session);
 				snip = query.fromSingle(snip, DBQuery.QUERY_NOT_NULL
 						| DBQuery.QUERY_CACHEABLE);
 
 				snippet = helper.createSnippet(snip.getSnippetId(),
 						snip.getOwner(), snip.getHeadline(),
 						snip.getDescription(),
 						CategoryFactory.fetchCategory(session, snip).getName(),
 						TagFactory.fetchTags(helper, session, id),
 						CommentFactory.fetchCommentIds(session, id),
 						fetchLicense(helper, session, snip).getShortDescr(),
 						snip.getViewcount(), snip.getRatingAverage());
 				helper.setCodeOfSnippet(snippet,
 						CodeFactory.fetchNewestCode(helper, session, snippet));
 				result.add(snippet);
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#getSnippets(Category, Integer, Integer)}
 	 * 
 	 * @param category
 	 * @param start
 	 * @param count
 	 * @return a list of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getSnippets(org.smartsnip.core.Category,
 	 *      java.lang.Integer, java.lang.Integer)
 	 */
 	static List<Snippet> getSnippets(Category category, Integer start,
 			Integer count) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		int initialSize = 10;
 		if (count != null && count > 0) {
 			initialSize = count;
 		}
 		List<Snippet> result = new ArrayList<Snippet>(initialSize);
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 
 			DBQuery query = new DBQuery(session);
 			DBCategory cat = new DBCategory();
 			cat.setName(category.getName());
 			cat = query.fromSingle(cat, DBQuery.QUERY_NOT_NULL
 					| DBQuery.QUERY_CACHEABLE);
 
 			query = new DBQuery(session);
 			DBSnippet entity = new DBSnippet();
 			Snippet snippet;
 			entity.setCategoryId(cat.getCategoryId());
 
 			for (Iterator<DBSnippet> iterator = query.iterate(entity,
 					DBQuery.QUERY_CACHEABLE); iterator.hasNext();) {
 				entity = iterator.next();
 
 				snippet = helper.createSnippet(entity.getSnippetId(), entity
 						.getOwner(), entity.getHeadline(), entity
 						.getDescription(),
 						CategoryFactory.fetchCategory(session, entity)
 								.getName(), TagFactory.fetchTags(helper,
 								session, entity.getSnippetId()),
 						CommentFactory.fetchCommentIds(session,
 								entity.getSnippetId()),
 						fetchLicense(helper, session, entity).getShortDescr(),
 						entity.getViewcount(), entity.getRatingAverage());
 				helper.setCodeOfSnippet(snippet,
 						CodeFactory.fetchNewestCode(helper, session, snippet));
 				result.add(snippet);
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getSnippet(Long)}
 	 * 
 	 * @param id
 	 * @return a snippet
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getSnippet(java.lang.Long)
 	 */
 	static Snippet getSnippet(Long id) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		Snippet result;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			result = fetchSnippet(helper, session, id);
 			helper.setCodeOfSnippet(result,
 					CodeFactory.fetchNewestCode(helper, session, result));
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#getAllSnippets(Integer, Integer, int)}
 	 * 
 	 * @param start
 	 * @param count
 	 * @param sortingOrder
 	 * @return all snippets of the given range
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getAllSnippets(Integer,
 	 *      Integer, int)
 	 */
 	static List<Snippet> getAllSnippets(Integer start, Integer count,
 			int sortingOrder) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		int initialSize = 10;
 		if (count != null && count > 0) {
 			initialSize = count;
 		}
 		List<Snippet> result = new ArrayList<Snippet>(initialSize);
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 
 			DBQuery query = new DBQuery(session);
 
 			// set the sorting order, reverse order chosen in all cases
 			switch (sortingOrder) {
 			case IPersistence.SORT_LATEST:
 				query.addOrder("lastEdited", DBQuery.ORDER_DESCENDING);
 				break;
 			case IPersistence.SORT_MOSTVIEWED:
 				query.addOrder("viewcount", DBQuery.ORDER_DESCENDING);
 				break;
 			case IPersistence.SORT_BEST_RATED:
 				query.addOrder("ratingAverage", DBQuery.ORDER_DESCENDING);
 				break;
 			default:
 				// case IPersistence.SORT_UNSORTED
 				break;
 			}
 
 			DBSnippet dbSnip = new DBSnippet();
 			Snippet snippet;
 			List<DBSnippet> snips = query.from(dbSnip, start, count, DBQuery.QUERY_CACHEABLE);
 
 			for (DBSnippet entity: snips) {
 
 				snippet = helper.createSnippet(entity.getSnippetId(), entity
 						.getOwner(), entity.getHeadline(), entity
 						.getDescription(),
 						CategoryFactory.fetchCategory(session, entity)
 								.getName(), TagFactory.fetchTags(helper,
 								session, entity.getSnippetId()),
 						CommentFactory.fetchCommentIds(session,
 								entity.getSnippetId()),
 						fetchLicense(helper, session, entity).getShortDescr(),
 						entity.getViewcount(), entity.getRatingAverage());
 				helper.setCodeOfSnippet(snippet,
 						CodeFactory.fetchNewestCode(helper, session, snippet));
 				result.add(snippet);
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getRandomSnippet(double)}
 	 * 
 	 * @param random
 	 *            a normalized random number (0 <= random <= 1)
 	 * @return a snippet
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getRandomSnippet(double)
 	 */
 	static Snippet getRandomSnippet(double random) throws IOException {
 		if (random < 0 || random > 1) {
 			throw new IOException(
 					"Random Query failed: the random number must be of the interval [0, 1], but is: "
 							+ random);
 		}
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		Snippet result = null;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			DBSnippet entity = new DBSnippet();
 			double offset = (query.count(entity, IPersistence.DB_DEFAULT)
 					.doubleValue() - 0.5) * random;
 			query.reset();
 			List<DBSnippet> snips = query.from(entity,
 					new Double(offset).intValue(), 1, IPersistence.DB_DEFAULT);
 			if (snips != null && !snips.isEmpty()) {
 				entity = snips.get(0);
 				result = helper.createSnippet(entity.getSnippetId(), entity
 						.getOwner(), entity.getHeadline(), entity
 						.getDescription(),
 						CategoryFactory.fetchCategory(session, entity)
 								.getName(), TagFactory.fetchTags(helper,
 								session, entity.getSnippetId()),
 						CommentFactory.fetchCommentIds(session,
 								entity.getSnippetId()),
 						fetchLicense(helper, session, entity).getShortDescr(),
 						entity.getViewcount(), entity.getRatingAverage());
 				helper.setCodeOfSnippet(result,
 						CodeFactory.fetchNewestCode(helper, session, result));
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getLicense(String)}
 	 * 
 	 * @param shortDescription
 	 * @return the license file as string
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getLicense(String)
 	 */
 	static String getLicense(String shortDescription) throws IOException {
 		Session session = DBSessionFactory.open();
 		DBLicense entity;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			entity = new DBLicense();
 			entity.setShortDescr(shortDescription);
 			entity = query.fromSingle(entity, DBQuery.QUERY_NULLABLE
 					| DBQuery.QUERY_CACHEABLE);
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return entity.getLicenseText();
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getRatings(Snippet)}
 	 * 
 	 * @param snippet
 	 * @return a list of ratings
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getRatings(org.smartsnip.core.Snippet)
 	 */
 	static List<Pair<User, Integer>> getRatings(Snippet snippet)
 			throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		List<Pair<User, Integer>> result = new ArrayList<Pair<User, Integer>>();
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			DBRating entity = new DBRating();
 			entity.setRatingId(snippet.getHashId(), null);
 
 			DBUser dbUser;
 			for (Iterator<DBRating> iterator = query.iterate(entity,
 					IPersistence.DB_DEFAULT); iterator.hasNext();) {
 				entity = iterator.next();
 
 				query.reset();
 				dbUser = new DBUser();
 				dbUser.setUserName(entity.getRatingId().getUserName());
 				dbUser = query.fromSingle(dbUser, DBQuery.QUERY_NOT_NULL);
 
 				result.add(new Pair<User, Integer>(helper.createUser(
 						dbUser.getUserName(), dbUser.getFullName(),
 						dbUser.getEmail(), dbUser.getUserState(),
 						dbUser.getLastLogin()), entity.getValue()));
 			}
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getAverageRating(Snippet)}
 	 * 
 	 * @param snippet
 	 * @return the average rating value
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getAverageRating(org.smartsnip.core.Snippet)
 	 */
 	static Float getAverageRating(Snippet snippet) throws IOException {
 		Session session = DBSessionFactory.open();
 		DBSnippet entity;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 			entity = new DBSnippet();
 			entity.setSnippetId(snippet.getHashId());
 			entity = query.fromSingle(entity, DBQuery.QUERY_NOT_NULL);
 
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return entity.getRatingAverage();
 	}
 
 	/**
 	 * Implementation of
 	 * {@link IPersistence#search(String, Integer, Integer, int)}
 	 * 
 	 * @param searchString
 	 * @param start
 	 * @param count
 	 * @param sortingOrder
 	 *            the sorting order: one of the constants
 	 *            {@link IPersistence#SORT_UNSORTED} ,
 	 *            {@link IPersistence#SORT_LATEST} ,
 	 *            {@link IPersistence#SORT_MOSTVIEWED} and
 	 *            {@link IPersistence#SORT_BEST_RATED}.
 	 * @return a list of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#search(java.lang.String,
 	 *      java.lang.Integer, java.lang.Integer, int)
 	 */
 	static List<Snippet> search(String searchString, Integer start,
 			Integer count, int sortingOrder) throws IOException {
 		Session session = DBSessionFactory.open();
 		SqlPersistenceHelper helper = new SqlPersistenceHelper();
 		List<Snippet> result = new ArrayList<Snippet>();
 
 		FullTextSession fullTextSession = Search.getFullTextSession(session);
 
 		Transaction tx = null;
 		try {
 			tx = fullTextSession.beginTransaction(); // TODO include tags,
 														// categories
 
 			// lucene full text query
 			QueryBuilder builder = fullTextSession.getSearchFactory()
 					.buildQueryBuilder().forEntity(DBSnippet.class).get();
 			org.apache.lucene.search.Query luceneQuery = null;
 			try {
 				luceneQuery = builder.keyword()
 						.onFields("headline", "description")
 						.matching(searchString).createQuery();
 
 			} catch (SearchException se) {
 				Logger log = Logger.getLogger(SnippetFactory.class);
 				log.trace("Search with no results: " + searchString, se);
 			}
 
 			if (luceneQuery != null) {
 				// wrap it in a hibernate.search query and add sorting criteria
 				FullTextQuery query = fullTextSession.createFullTextQuery(
 						luceneQuery);
 
 				// set the sorting order, reverse order chosen in all cases
 				switch (sortingOrder) {
 				case IPersistence.SORT_LATEST:
 					query.setSort(new Sort(new SortField("lastEdited",
 							SortField.STRING, true)));
 					break;
 				case IPersistence.SORT_MOSTVIEWED:
 					query.setSort(new Sort(new SortField("viewcount",
 							SortField.STRING, true)));
 					break;
 				case IPersistence.SORT_BEST_RATED:
 					query.setSort(new Sort(new SortField("ratingAverage",
 							SortField.STRING, true)));
 					break;
 				default:
 					// case IPersistence.SORT_UNSORTED
 					break;
 				}
 
 				// narrow the result range
 				if (start != null && start > 0) {
 					query.setFirstResult(start);
 				}
 				if (count != null && count > 0) {
 					query.setFetchSize(count);
 				}
 				query.setCacheable(true);
 
 				// execute search and build Snippets
 				Snippet snippet;
 				@SuppressWarnings("unchecked")
 				List<DBSnippet> entities = query.list();
 				for (DBSnippet entity : entities) {
 					snippet = helper.createSnippet(entity.getSnippetId(),
 							entity.getOwner(), entity.getHeadline(), entity
 									.getDescription(), CategoryFactory
 									.fetchCategory(session, entity).getName(),
 							TagFactory.fetchTags(helper, session,
 									entity.getSnippetId()), CommentFactory
 									.fetchCommentIds(session,
 											entity.getSnippetId()),
 							fetchLicense(helper, session, entity)
 									.getShortDescr(), entity.getViewcount(),
 							entity.getRatingAverage());
 					helper.setCodeOfSnippet(snippet, CodeFactory
 							.fetchNewestCode(helper, session, snippet));
 					result.add(snippet);
 				}
 			}
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Implementation of {@link IPersistence#getSnippetsCount()}
 	 * 
 	 * @return the number of snippets
 	 * @throws IOException
 	 * @see org.smartsnip.persistence.hibernate.SqlPersistenceImpl#getSnippetsCount()
 	 */
 	static int getSnippetsCount() throws IOException {
 		Session session = DBSessionFactory.open();
 		DBSnippet entity;
 		int result = 0;
 
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			DBQuery query = new DBQuery(session);
 
 			entity = new DBSnippet();
 
 			result = query.count(entity, DBQuery.QUERY_CACHEABLE).intValue();
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null)
 				tx.rollback();
 			throw new IOException(e);
 		} finally {
 			DBSessionFactory.close(session);
 		}
 		return result;
 	}
 
 	/**
 	 * Helper method to fetch a snippet.
 	 * 
 	 * @param helper
 	 *            the PersisteceHelper object to create the tags
 	 * @param session
 	 *            the session in which the query is to execute
 	 * @param id
 	 *            the snippet id
 	 * @return the Snippet with no code inserted
 	 */
 	static Snippet fetchSnippet(SqlPersistenceHelper helper, Session session,
 			Long id) {
 		Snippet result;
 		DBQuery query = new DBQuery(session);
 		DBSnippet entity = new DBSnippet();
 		entity.setSnippetId(id);
 		entity = query.fromSingle(entity, DBQuery.QUERY_NOT_NULL
 				| DBQuery.QUERY_CACHEABLE);
 
 		result = helper.createSnippet(entity.getSnippetId(), entity.getOwner(),
 				entity.getHeadline(), entity.getDescription(), CategoryFactory
 						.fetchCategory(session, entity).getName(), TagFactory
 						.fetchTags(helper, session, entity.getSnippetId()),
 				CommentFactory.fetchCommentIds(session, entity.getSnippetId()),
 				fetchLicense(helper, session, entity).getShortDescr(), entity
 						.getViewcount(), entity.getRatingAverage());
 		return result;
 	}
 
 	/**
 	 * Helper method to fetch a license from a snippet.
 	 * 
 	 * @param helper
 	 *            the PersisteceHelper object to create the tags
 	 * @param session
 	 *            the session in which the query is to execute
 	 * @param snippet
 	 *            the snippet as source of the license
 	 * @return the DBLicense
 	 */
 	static DBLicense fetchLicense(SqlPersistenceHelper helper, Session session,
 			DBSnippet snippet) {
 		DBQuery query = new DBQuery(session);
 		DBLicense entity = new DBLicense();
 		entity.setLicenseId(snippet.getLicenseId());
 		entity = query.fromSingle(entity, DBQuery.QUERY_NOT_NULL
 				| DBQuery.QUERY_CACHEABLE);
 		return entity;
 	}
 }
