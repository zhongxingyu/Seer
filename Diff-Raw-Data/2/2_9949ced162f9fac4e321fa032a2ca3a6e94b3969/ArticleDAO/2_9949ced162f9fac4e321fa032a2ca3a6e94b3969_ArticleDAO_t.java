 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *
  *  Contributors:
  *     Wim Jongman - initial API and implementation
  *
  *
  *******************************************************************************/
 package org.eclipse.ecf.protocol.nntp.store.derby.internal;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import org.eclipse.ecf.protocol.nntp.core.ArticleFactory;
 import org.eclipse.ecf.protocol.nntp.core.Debug;
 import org.eclipse.ecf.protocol.nntp.core.StringUtils;
 import org.eclipse.ecf.protocol.nntp.model.IArticle;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 
 public class ArticleDAO {
 
 	private final Connection connection;
 
 	private PreparedStatement insertArticle;
 	private PreparedStatement updateArticle;
 	private PreparedStatement deleteArticle;
 
 	private PreparedStatement getArticleRange;
 
 	private PreparedStatement getArticleHeader;
 
 	private PreparedStatement deleteArticleHeader;
 
 	private PreparedStatement getArticleBody;
 
 	private PreparedStatement deleteArticleBody;
 
 	private PreparedStatement insertArticleHeader;
 
 	private PreparedStatement insertArticleBody;
 
 	private PreparedStatement updateArticleBody;
 
 	private PreparedStatement getOldestArticle;
 
 	private PreparedStatement getNewestArticle;
 
 	private PreparedStatement deleteArticleReply;
 
 	private PreparedStatement insertArticleReply;
 
 	private PreparedStatement getArticleByUri;
 
 	private PreparedStatement getArticleByID;
 
 	private PreparedStatement getArticleReplies;
 
 	private PreparedStatement getArticleIdsFromUser;
 
 	private PreparedStatement getMarkedArticles;
 
 	public ArticleDAO(Connection connection) throws StoreException {
 		this.connection = connection;
 		prepareStatements();
 	}
 
 	private void prepareStatements() throws StoreException {
 
 		try {
 			getArticleHeader = connection
 					.prepareStatement("select * from articleheader where articleid = ?");
 
 			getArticleBody = connection
 					.prepareStatement("select * from articlebody where articleid = ?");
 
 			getArticleRange = connection
 					.prepareStatement("select * from article where (articleNumber between ? and ?) and newsgroupId = ?");
 
 			getArticleByUri = connection
 					.prepareStatement("select * from article where uri = ?");
 
 			getArticleByID = connection
 					.prepareStatement("select * from article where ID = ? and newsgroupId = ?");
 
 			getArticleReplies = connection
 					.prepareStatement("Select article.ID, "
 							+ "article.messageID, " + "article.uri, "
 							+ "article.newsgroupID, "
 							+ "article.articleNumber, " + "article.isMarked, "
 							+ "article.isRead " + "from articleReply "
 							+ "left outer join "
 							+ "article on article.ID = articleReply.articleID "
 							+ "where messageIDRepliedto = ?");
 
 			getOldestArticle = connection
 					.prepareStatement("select * from article where newsgroupId = ? order by articleNumber asc");
 			getOldestArticle.setMaxRows(1);
 
 			getNewestArticle = connection
 					.prepareStatement("select * from article where newsgroupId = ? order by articleNumber desc");
 			getNewestArticle.setMaxRows(1);
 
 			insertArticle = connection.prepareStatement(
 					"insert into Article ( messageID, uri, newsgroupID, articleNumber, "
 							+ "isMarked," + "isRead) values(?, ?, ?, ?, ?, ?)",
 					Statement.RETURN_GENERATED_KEYS);
 
 			insertArticleHeader = connection
 					.prepareStatement("insert into ArticleHeader (articleid, attribute, value) values(?, ?, ?)");
 
 			insertArticleReply = connection
 					.prepareStatement("insert into ArticleReply (articleid, messageIDRepliedTo) values(?, ?)");
 
 			insertArticleBody = connection
 					.prepareStatement("insert into ArticleBody (articleid, body) values(?, ?)");
 
 			updateArticle = connection
 					.prepareStatement("update Article set messageID = ?, uri = ?, "
 							+ "newsgroupid = ?, articleNumber = ?,"
 							+ "isMarked = ?,"
 							+ "isRead = ? where messageID = ?");
 
 			updateArticleBody = connection
 					.prepareStatement("update Articlebody set articleID = ?, body = ? where articleID = ?");
 
 			deleteArticle = connection
 					.prepareStatement("delete from article where uri like ? ");
 
 			deleteArticleHeader = connection
 					.prepareStatement("delete from articleheader where articleid = ?");
 
 			deleteArticleReply = connection
 					.prepareStatement("delete from articleReply where articleid = ?");
 
 			deleteArticleBody = connection
 					.prepareStatement("delete from articlebody where articleid = ?");
 
 			getArticleIdsFromUser = connection
 					.prepareStatement("select articleid from articleheader where attribute = 'From:' and value = ?");
 
 			getMarkedArticles = connection
					.prepareStatement("select * from article where isMarked = '1' and newsgroupId = ?");
 
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	public IArticle[] getArticles(INewsgroup newsgroup, int from, int to)
 			throws StoreException {
 		try {
 			getArticleRange.setInt(1, from);
 			getArticleRange.setInt(2, to);
 			getArticleRange.setInt(3,
 					Integer.parseInt(newsgroup.getProperty("DB_ID")));
 			getArticleRange.execute();
 			ResultSet r = getArticleRange.getResultSet();
 			if (r == null)
 				return new IArticle[0];
 
 			ArrayList result = new ArrayList();
 
 			while (r.next()) {
 				IArticle article = ArticleFactory.createArticle(
 						getArticleNumber(r), newsgroup);
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 				result.add(article);
 			}
 			r.close();
 			return (IArticle[]) result.toArray(new IArticle[0]);
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	public IArticle getOldestArticle(INewsgroup newsgroup)
 			throws StoreException {
 		return processOneArticle(getOldestArticle, newsgroup);
 	}
 
 	public IArticle getNewestArticle(INewsgroup newsgroup)
 			throws StoreException {
 		return processOneArticle(getNewestArticle, newsgroup);
 	}
 
 	private IArticle processOneArticle(PreparedStatement stat,
 			INewsgroup newsgroup) throws StoreException {
 		try {
 			stat.setInt(1, Integer.parseInt(newsgroup.getProperty("DB_ID")));
 			stat.execute();
 			ResultSet r = stat.getResultSet();
 			if (r == null)
 				return null;
 
 			while (r.next()) {
 				IArticle article = ArticleFactory.createArticle(
 						getArticleNumber(r), newsgroup);
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 				return article;
 			}
 			r.close();
 			return null;
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 
 	}
 
 	private boolean isRead(ResultSet r) throws SQLException {
 		return r.getString(7).equals("1");
 	}
 
 	private boolean isMarked(ResultSet r) throws SQLException {
 		return r.getString(6).equals("1");
 	}
 
 	private int getArticleNumber(ResultSet r) throws SQLException {
 		return r.getInt(5);
 	}
 
 	private int getArticleID(ResultSet r) throws SQLException {
 		return r.getInt(1);
 	}
 
 	public void insertArticle(IArticle article) throws StoreException {
 		article.setProperty("DB_ID", processArticle(insertArticle, article)
 				+ "");
 		insertArticleHeader(article);
 		insertArticleReplies(article);
 	}
 
 	public void insertArticle(IArticle article, String[] body)
 			throws StoreException {
 		insertArticle(article);
 		insertArticleBody(article, body);
 	}
 
 	public void updateArticle(IArticle article, String[] body)
 			throws StoreException {
 		updateArticle(article);
 		updateArticleBody(article, body);
 	}
 
 	public void updateArticle(IArticle article) throws StoreException {
 		deleteArticleHeader(article);
 		processArticle(updateArticle, article);
 		insertArticleHeader(article);
 	}
 
 	public void insertArticleReplies(IArticle article) throws StoreException {
 		processArticleReplies(insertArticleReply, article);
 	}
 
 	private void processArticleReplies(PreparedStatement statement,
 			IArticle article) throws StoreException {
 		if (article.getLastReference() != null) {
 			try {
 				statement.setInt(1,
 						Integer.parseInt(article.getProperty("DB_ID")));
 				statement.setString(2, article.getLastReference());
 				statement.execute();
 			} catch (SQLException e) {
 				throw new StoreException(e.getMessage(), e);
 			}
 		}
 	}
 
 	public void deleteArticleReplies(IArticle article) throws StoreException {
 		try {
 			int id = Integer.parseInt(article.getProperty("DB_ID"));
 			deleteArticleReply.setInt(1, id);
 			deleteArticleReply.execute();
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	public void deleteArticle(IArticle article) throws StoreException {
 		processDeleteArticle(article.getURL());
 	}
 
 	private void processDeleteArticle(String url) throws StoreException {
 		try {
 			deleteArticle.setString(1, url);
 			deleteArticle.execute();
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	private int processArticle(PreparedStatement statement, IArticle article)
 			throws StoreException {
 		try {
 			statement.setString(1, article.getMessageId());
 			statement.setString(2, article.getURL());
 			statement.setInt(3, Integer.parseInt(article.getNewsgroup()
 					.getProperty("DB_ID")));
 			statement.setInt(4, article.getArticleNumber());
 			statement.setString(5, article.isMarked() ? "1" : "0");
 			statement.setString(6, article.isRead() ? "1" : "0");
 			if (statement == updateArticle) {
 				statement.setString(7, article.getMessageId());
 			}
 			statement.execute();
 			ResultSet r = statement.getGeneratedKeys();
 			if (r != null && r.next()) {
 				int result = r.getInt(1);
 				r.close();
 				return result;
 			}
 			return 0;
 
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	private void loadArticleHeaders(IArticle article, int articleID)
 			throws StoreException {
 		try {
 			getArticleHeader.setInt(1, articleID);
 			getArticleHeader.execute();
 			ResultSet r = getArticleHeader.getResultSet();
 			if (r == null)
 				return;
 			while (r.next()) {
 				article.setHeaderAttributeValue(r.getString(2), r.getString(3));
 			}
 			r.close();
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	public void insertArticleBody(IArticle article, String[] body)
 			throws StoreException {
 		processArticleBody(insertArticleBody, article, body);
 	}
 
 	public String[] getArticleBody(IArticle article) throws StoreException {
 		try {
 
 			getArticleBody.setInt(1,
 					Integer.parseInt(article.getProperty("DB_ID")));
 			getArticleBody.execute();
 			ResultSet r = getArticleBody.getResultSet();
 			while (r.next()) {
 				Clob clob = r.getClob(2);
 				Reader is = clob.getCharacterStream();
 				StringBuffer body = new StringBuffer(1024);
 				int c = is.read();
 				while (c > 0) {
 					body.append((char) c);
 					c = is.read();
 				}
 				r.close();
 				return StringUtils.split(body.toString(), SALVO.CRLF);
 			}
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 		return new String[0];
 	}
 
 	public void deleteArticleBody(IArticle article) throws StoreException {
 		try {
 			int id = Integer.parseInt(article.getProperty("DB_ID"));
 			deleteArticleBody.setInt(1, id);
 			deleteArticleBody.execute();
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	public void updateArticleBody(IArticle article, String[] body)
 			throws StoreException {
 		processArticleBody(updateArticleBody, article, body);
 
 	}
 
 	private void processArticleBody(PreparedStatement statement,
 			IArticle article, String[] body) throws StoreException {
 		try {
 			statement.setInt(1, Integer.parseInt(article.getProperty("DB_ID")));
 
 			StringBuffer buffer = new StringBuffer(body.length * 80);
 			for (int i = 0; i < body.length; i++) {
 				String string = body[i];
 				buffer.append(string + SALVO.CRLF);
 			}
 			statement.setClob(2, new StringReader(buffer.toString()));
 			if (statement == updateArticleBody)
 				statement.setInt(3,
 						Integer.parseInt(article.getProperty("DB_ID")));
 			statement.execute();
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 
 	}
 
 	public void insertArticleHeader(IArticle article) throws StoreException {
 		processArticleHeader(insertArticleHeader, article);
 	}
 
 	public void deleteArticleHeader(IArticle article) throws StoreException {
 		try {
 			int id = Integer.parseInt(article.getProperty("DB_ID"));
 			deleteArticleHeader.setInt(1, id);
 			deleteArticleHeader.execute();
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	private void processArticleHeader(PreparedStatement statement,
 			IArticle article) throws StoreException {
 		try {
 			int r = Debug.timerStart(getClass());
 			String[] headerAttributes = article.getHeaderAttributes();
 			String[] headerAttrValues = article.getHeaderAttributeValues();
 			int articleID = Integer.parseInt(article.getProperty("DB_ID"));
 			Debug.timerStop(getClass(), r);
 			for (int i = 0; i < headerAttributes.length; i++) {
 				String attr = headerAttributes[i];
 				statement.setInt(1, articleID);
 				statement.setString(2, attr);
 				statement.setString(3, headerAttrValues[i]);
 				statement.execute();
 			}
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 
 	}
 
 	public void deleteArticle(INewsgroup group) throws StoreException {
 		processDeleteArticle(group.getURL() + "%");
 	}
 
 	public IArticle getArticle(INewsgroup group, String URL)
 			throws StoreException {
 
 		return internalGetArticleByString(getArticleByUri, group, URL);
 	}
 
 	public boolean hasArticle(IArticle article) throws StoreException {
 		return getArticles(article.getNewsgroup(), article.getArticleNumber(),
 				article.getArticleNumber()).length != 0;
 	}
 
 	private IArticle internalGetArticleByString(PreparedStatement s,
 			INewsgroup group, String URL) throws StoreException {
 		try {
 			s.setString(1, URL);
 			s.execute();
 			ResultSet r = s.getResultSet();
 			if (r == null)
 				throw new StoreException("Article with uri/messageID " + URL
 						+ " not in the database");
 
 			if (r.next()) {
 				new NewsgroupDAO(connection)
 						.getNewsgroup(null, getNewsgroup(r));
 				IArticle article = ArticleFactory.createArticle(
 						getArticleNumber(r), group);
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 				r.close();
 				return article;
 			}
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 		return null;
 
 	}
 
 	private int getNewsgroup(ResultSet r) throws SQLException {
 		return r.getInt(4);
 	}
 
 	public IArticle[] getFollowUps(IArticle articleIn) throws StoreException {
 		try {
 			getArticleReplies.setString(1, articleIn.getMessageId());
 			getArticleReplies.execute();
 			ResultSet r = getArticleReplies.getResultSet();
 			if (r == null)
 				return new IArticle[0];
 
 			ArrayList result = new ArrayList();
 
 			while (r.next()) {
 				IArticle article = ArticleFactory.createArticle(
 						getArticleNumber(r), articleIn.getNewsgroup());
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 				result.add(article);
 			}
 			r.close();
 			return (IArticle[]) result.toArray(new IArticle[0]);
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Get articlesIds of a particular user
 	 * 
 	 * @param userId
 	 *            Full user name
 	 * @return articlesId s of a particular user
 	 * @throws StoreException
 	 */
 	public Integer[] getArticleIdsFromUser(String userId) throws StoreException {
 		try {
 			getArticleIdsFromUser.setString(1, userId);
 			getArticleIdsFromUser.execute();
 
 			ResultSet r = getArticleIdsFromUser.getResultSet();
 
 			if (r == null) {
 				return null;
 			}
 
 			ArrayList<Integer> result = new ArrayList<Integer>();
 			while (r.next()) {
 				result.add(r.getInt(1));
 			}
 			r.close();
 
 			Integer output[] = new Integer[result.size()];
 			output = result.toArray(output);
 
 			return output;
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Get a article from the articleId
 	 * 
 	 * @param newsgroup
 	 *            Newsgroup
 	 * @param id
 	 *            articleId
 	 * @return article corresponds to given articleId
 	 * @throws StoreException
 	 */
 	public IArticle getArticleById(INewsgroup newsgroup, int id)
 			throws StoreException {
 		try {
 			getArticleByID.setInt(1, id);
 			getArticleByID.setInt(2,
 					Integer.parseInt(newsgroup.getProperty("DB_ID")));
 			getArticleByID.execute();
 
 			ResultSet r = getArticleByID.getResultSet();
 
 			if (r == null)
 				return null;
 
 			IArticle article = null;
 			while (r.next()) {
 				article = ArticleFactory.createArticle(getArticleNumber(r),
 						newsgroup);
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 			}
 			r.close();
 
 			return article;
 
 		} catch (SQLException e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 * Get marked articles for a particular newsgroup
 	 * 
 	 * @param newsgroup
 	 *            Newsgroup
 	 * @return marked articles for a particular newsgroup
 	 * @throws StoreException
 	 */
 	public IArticle[] getMarkedArticles(INewsgroup newsgroup)
 			throws StoreException {
 
 		try {
 			getMarkedArticles.setInt(1,
 					Integer.parseInt(newsgroup.getProperty("DB_ID")));
 			getMarkedArticles.execute();
 			ResultSet r = getMarkedArticles.getResultSet();
 
 			if (r == null)
 				return new IArticle[0];
 
 			ArrayList result = new ArrayList();
 
 			while (r.next()) {
 				IArticle article = ArticleFactory.createArticle(
 						getArticleNumber(r), newsgroup);
 				article.setMarked(isMarked(r));
 				article.setRead(isRead(r));
 				article.setProperty("DB_ID", getArticleID(r) + "");
 				loadArticleHeaders(article, getArticleID(r));
 				result.add(article);
 			}
 			r.close();
 			return (IArticle[]) result.toArray(new IArticle[0]);
 
 		} catch (Exception e) {
 			throw new StoreException(e.getMessage(), e);
 		}
 
 	}
 
 }
