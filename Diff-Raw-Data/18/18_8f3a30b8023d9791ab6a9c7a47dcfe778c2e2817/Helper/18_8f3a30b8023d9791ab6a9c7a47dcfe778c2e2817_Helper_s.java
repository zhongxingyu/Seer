 /**
  * 
  */
 package org.fubme.persistency;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.fubme.factories.PostFactory;
 import org.fubme.models.Comment;
 import org.fubme.models.Post;
 import org.fubme.models.Tag;
 import org.fubme.models.User;
 
 /**
  * @author riccardo
  * 
  */
 
 public abstract class Helper {
 
 	/**
 	 * Retrieves all comments given a post filtering out all comments from those
 	 * users blocked by the user passed as parameter
 	 * 
 	 * @param post
 	 * @param user
 	 * @return a list of comments
 	 */
 	public static final List<Comment> getComments(Post post, User user) {
 		List<Comment> comments = new ArrayList<Comment>();
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 		ResultSet resultset = null;
 		String sql = "SELECT lcp.id,lcp.body,lcp.luser_id,lcp.time,lcp.post_id from luser_comments_post as lcp join post as p on p.id = lcp.post_id where p.id = "
 				+ post.getId();
 		try {
 			stmt = connection.createStatement();
 			resultset = stmt.executeQuery(sql);
 			while (resultset.next()) {
 				Comment comment = new Comment(""
 						+ resultset.getInt(Comment.POST_ID),
 						resultset.getString(Comment.LUSER_ID),
 						resultset.getTimestamp(Comment.TIME),
 						resultset.getString(Comment.BODY));
 				comments.add(comment);
 			}
 			stmt.close();
 			resultset.close();
 		} catch (SQLException ex) {
 			comments = null;
 			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 			if (resultset != null)
 				resultset = null;
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 		return comments;
 	}
 
 	public static final List<Tag> getTags(Post post) {
 		List<Tag> tags = new ArrayList<Tag>();
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 		ResultSet resultset = null;
 		String sql = "SELECT tag_name from post_tagged_as where post_id = "
 				+ post.getId();
 		try {
 			stmt = connection.createStatement();
 			resultset = stmt.executeQuery(sql);
 			while (resultset.next()) {
 				tags.add(new Tag(resultset.getString(Tag.TAG_NAME)));
 			}
 			stmt.close();
 			resultset.close();
 		} catch (SQLException ex) {
 			tags = null;
 			Logger.getLogger(Helper.class.getName())
 					.log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 			if (resultset != null)
 				resultset = null;
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 		return tags;
 	}
 
 	public static final List<Post> getPostsFromUser(User user, int limit) {
 		List<Post> posts = new ArrayList<Post>();
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 		ResultSet resultset = null;
 		String sql = "SELECT * FROM post WHERE " + Post.LUSER_ID + " = '"
 				+ user.getId() + "' ORDER BY ptime limit " + limit;
 		try {
 			stmt = connection.createStatement();
 			resultset = stmt.executeQuery(sql);
 			while (resultset.next()) {
 				Post post = PostFactory.getPost(
 						resultset.getTimestamp(Post.PTIME),
 						resultset.getInt(Post.ID),
 						resultset.getString(Post.LUSER_ID),
 						resultset.getString(Post.BODY),
 						resultset.getString(Post.LINK),
 						resultset.getString(Post.MIME));
 				post.setComments(Helper.getComments(post, user));
 				post.setTags(Helper.getTags(post));
 				posts.add(post);
 			}
 			stmt.close();
 			resultset.close();
 		} catch (SQLException ex) {
 			posts = null;
 			Logger.getLogger(Helper.class.getName())
 					.log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 			if (resultset != null)
 				resultset = null;
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 		return posts;
 	}
 
	public static final Post getPost(String id, User user) {
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 		String sql = "select * from post where id = " + id;
 		Post post = null;
 		try {
 			stmt = connection.createStatement();
 			ResultSet result = stmt.executeQuery(sql);
 			while (result.next()) {
 				post = PostFactory.getPost(result.getTimestamp("ptime"),
 						result.getInt("id"), result.getString("luser_id"),
 						result.getString("body"), result.getString("link"),
 						result.getString("mime"));
 				post.setComments(Helper.getComments(post, user));
 				post.setTags(Helper.getTags(post));
 			}
 			System.out.println("Helper.getPost() \t" + post.getId() + "\t"
 					+ post.getBody() + "\t" + post.getUser_id() + "\t");
 			return post;
 		} catch (SQLException ex) {
 			post = null;
 			Logger.getLogger(Helper.class.getName())
 					.log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 
 		return null;
 	}
 
 	public static final List<User> getLikes(Post post) {
 		List<User> likes = new ArrayList<User>();
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 
 		String sql = "SELECT luser_id from luser_likes_post where post_id = "
 				+ post.getId();
 		try {
 			stmt = connection.createStatement();
 			ResultSet result = stmt.executeQuery(sql);
 			while (result.next()) {
 				likes.add(new User(result.getString("luser_id"), null));
 			}
 		} catch (SQLException ex) {
 			post = null;
 			Logger.getLogger(Helper.class.getName())
 					.log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 
 		return likes;
 	}
 
 	public static final boolean doesUserLikesPost(User user, Post post) {
 		Connection connection = DBConnection.getConnection();
 		Statement stmt = null;
 		String sql = "select luser_id from luser_likes_post where post_id = "
 				+ post.getId() + "and luser_id = '" + user.getId() + "'";
 		try {
 			stmt = connection.createStatement();
 			ResultSet result = stmt.executeQuery(sql);
 			if (result.next()) {
 
 				Logger.getLogger(Helper.class.getName()).log(Level.SEVERE,
 						user.getId() + " likes post " + post.getId());
 				return true;
 
 			}
 		} catch (SQLException ex) {
 			post = null;
 			Logger.getLogger(Helper.class.getName())
 					.log(Level.SEVERE, null, ex);
 		} finally {
 			if (stmt != null)
 				stmt = null;
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 
 				}
 				connection = null;
 			}
 		}
 		Logger.getLogger(Helper.class.getName()).log(Level.SEVERE,
 				user.getId() + " doesn't like post " + post.getId());
 		return false;
 	}
 }
