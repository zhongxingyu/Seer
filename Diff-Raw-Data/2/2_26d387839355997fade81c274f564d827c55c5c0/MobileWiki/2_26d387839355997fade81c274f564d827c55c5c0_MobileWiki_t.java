 package com.mobilewiki.webservice;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.mobilewiki.db.DatabaseController;
 
 public class MobileWiki {
 	private DatabaseController db_controller;
 	private String query = "";
 	private ResultSet rs = null;
 
 	public MobileWiki() {
 		db_controller = DatabaseController.getInstance();
 	}
 	
 	public String respondMessage(String message) {
 		return "Received message: " + message;
 	}
 
 	public List<Integer> getArticleIds() {
 		List<Integer> article_ids = new ArrayList<Integer>();
 
 		query = "Select Article_Id From mobilewikia.wiki_article";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null) {
 				while (rs.next()) {
 					article_ids.add(rs.getInt("Article_Id"));
 				}
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out.println("Error closing ResultSet: getArticleIds()");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return article_ids;
 	}
 
 	public String getTitleForArticleId(int article_id) {
 		String title = "";
 
 		query = "Select Title From mobilewikia.wiki_article Where Article_id = '" + article_id + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				title = rs.getString("Title");
 			}
 			
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getTitleForArticleId("
 								+ article_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return title;
 	}
 
 	public int getArticleIdForTitle(String title) {
 		int article_id = 0;
 
 		query = "Select Article_id from mobilewikia.wiki_article Where Title = '"
 				+ title + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				article_id = rs.getInt("Article_id");
 			}
 			
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getArticleIdForTitle("
 								+ title + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return article_id;
 	}	
 	
 	public List<Integer> getContentIdsforArticleId(int article_id) {
 		List<Integer> content_ids = new ArrayList<Integer>();
 		
 		query = "Select Content_Id From mobilewikia.wiki_content Where Article_id = '"
				+ article_id + "' order by date_change desc limit 1";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null) {
 				while (rs.next()) {
 					content_ids.add(rs.getInt("Content_Id"));
 				}
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getContentIdsforArticleId("
 								+ article_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return content_ids;
 	}
 
 	public String getDateChangeForContentId(int content_id) {
 		String date_change = "";
 
 		query = "Select Date_Change From mobilewikia.wiki_content Where Content_id = '"
 				+ content_id + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				date_change = rs.getString("Date_Change");
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getDateChangeForContentId("
 								+ content_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return date_change;
 	}
 
 	public int getArticleIdForContentId(int content_id) {
 		int article_id = -1;
 
 		query = "Select Article_Id From mobilewikia.wiki_content Where Content_id = '"
 				+ content_id + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				article_id = rs.getInt("Article_Id");
 
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getArticleIdForContentId("
 								+ content_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return article_id;
 	}
 
 	public String getContentForContentId(int content_id) {
 		String content = "";
 
 		query = "Select Content From mobilewikia.wiki_content Where Content_id = '"
 				+ content_id + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				content = rs.getString("Content");
 
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getContentForContentId("
 								+ content_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return content;
 	}
 
 	public String getTagForContentId(int content_id) {
 		String tag = "";
 
 		query = "Select Tag From mobilewikia.wiki_content Where Content_Id = '"
 				+ content_id + "'";
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				tag = rs.getString("Tag");
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getTagForContentId("
 								+ content_id + ")");
 			}
 			//db_controller.closeConnectionStatement();
 		}
 
 		return tag;
 	}
 
 	public int createMain(String wiki_name, String logo_link) {
 		int main_id = -1;
 
 		query = "Insert Into mobilewikia.wiki_main (Wiki_Name, Logo_Link) Values ('" + wiki_name + "', '" + logo_link + "')";
 		try {
 			main_id = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Insert: " + query);
 		}
 
 		return main_id;
 	}
 	
 	public int createArticle(String title) {
 		int article_id = -1;
 
 		query = "Insert Into mobilewikia.wiki_article (Title) Values ('" + title + "')";
 		try {
 			article_id = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Insert: " + query);
 		}
 
 		return article_id;
 	}
 	
 	public int createContent(int article_id, String content, String tag) {
 		int content_id = -1;
 
 		query = "Insert Into mobilewikia.wiki_content (Article_Id, Content, Tag) Values ('" + article_id + "','" + content + "', '" + tag + "')";
 		try {
 			content_id = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Insert: " + query);
 		}
 
 		return content_id;
 	}
 
 	public int editMain(int main_id, String wiki_name, String logo_link) {
 		int update_state = -1;
 
 		query = "Update mobilewikia.wiki_main Set Wiki_Name = '" + wiki_name + "', Logo_Link = '" + logo_link + "' Where Main_Id = '" + main_id + "'";
 		try {
 			update_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Update: " + query);
 		}
 
 		return update_state;
 	}
 	
 	public int editArticle(int article_id, String title) {
 		int update_state = -1;
 
 		query = "Update mobilewikia.wiki_article Set Title = '" + title + "' Where Article_Id = '" + article_id + "'";
 		try {
 			update_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Update: " + query);
 		}
 
 		return update_state;
 	}
 	
 	public int editContent(int content_id, int article_id, String content, String tag) {
 		int update_state = -1;
 
 		query = "Update mobilewikia.wiki_content Set Article_Id = '" + article_id + "', Content = '" + content + "', Tag = '" + tag + "' Where Content_Id = '" + content_id + "'";
 		try {
 			update_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Update: " + query);
 		}
 
 		return update_state;
 	}
 	
 	public int deleteMain(int main_id) {
 		int delete_state = -1;
 
 		query = "Delete mobilewikia.wiki_main Where Main_Id = '" + main_id + "'";
 		try {
 			delete_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Delete: " + query);
 		}
 
 		return delete_state;
 	}
 	
 	public int deleteArticle(int article_id) {
 		int delete_state = -1;
 
 		query = "Delete mobilewikia.wiki_article Where Article_Id = '" + article_id + "'";
 		try {
 			delete_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Delete: " + query);
 		}
 
 		return delete_state;
 	}
 	
 	public int deleteContent(int content_id) {
 		int delete_state = -1;
 
 		query = "Delete mobilewikia.wiki_content Where Content_Id = '" + content_id + "'";
 		try {
 			delete_state = db_controller.executeUpdate(query);
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Delete: " + query);
 		}
 
 		return delete_state;
 	}
 
 	public HashMap<String, String> getContentTitleTagForArticleId(int article_id) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		query = "SELECT a.article_id, c.content_id, a.title, c.content, c.tag " +
 				"FROM mobilewikia.wiki_article a" +
 				"LEFT JOIN mobilewikia.wiki_content c ON ( a.article_id = c.article_id ) " +
 				"WHERE a.article_id = '" + article_id + "'";
 		
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				result.put("article_id", rs.getString("article_id"));
 				result.put("content_id", rs.getString("content_id"));
 				result.put("title", rs.getString("title"));
 				result.put("content", rs.getString("content"));
 				result.put("tag", rs.getString("tag"));
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getContentTitleTagForArticleId("
 								+ article_id + ")");
 			}
 		}
 		
 		return result;
 	}
 
 	public HashMap<String, String> getContentTitleTagForContentId(int content_id) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		query = "SELECT a.article_id, c.content_id, a.title, c.content, c.tag " +
 				"FROM mobilewikia.wiki_article a" +
 				"LEFT JOIN mobilewikia.wiki_content c ON ( a.article_id = c.article_id ) " +
 				"WHERE c.content_id = '" + content_id + "'";
 		
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				result.put("article_id", rs.getString("article_id"));
 				result.put("content_id", rs.getString("content_id"));
 				result.put("title", rs.getString("title"));
 				result.put("content", rs.getString("content"));
 				result.put("tag", rs.getString("tag"));
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getContentTitleTagForContentId("
 								+ content_id + ")");
 			}
 		}
 		
 		return result;
 	}
 	
 	public HashMap<String, String> getContentTitleTagForTitle(String title) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		query = "SELECT a.article_id, c.content_id, a.title, c.content, c.tag " +
 				"FROM mobilewikia.wiki_article a" +
 				"LEFT JOIN mobilewikia.wiki_content c ON ( a.article_id = c.article_id ) " +
 				"WHERE c.title = '" + title + "'";
 		
 		try {
 			rs = db_controller.getResultSet(query);
 
 			if (rs != null && rs.next()) {
 				result.put("article_id", rs.getString("article_id"));
 				result.put("content_id", rs.getString("content_id"));
 				result.put("title", rs.getString("title"));
 				result.put("content", rs.getString("content"));
 				result.put("tag", rs.getString("tag"));
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e.toString());
 			System.out.println("Error executing the Query: " + query);
 
 		} finally {
 			try {
 				if (rs != null) {
 					rs.close();
 				}
 
 			} catch (SQLException e) {
 				System.err.println(e.toString());
 				System.out
 						.println("Error closing ResultSet: getContentTitleTagForTitle("
 								+ title + ")");
 			}
 		}
 		
 		return result;
 	}
 	
     public String getAllTitlesWithTags() {
         List<String> result = new ArrayList<>();
 
         query = "SELECT a.article_id, a.title, c.tag, c.date_change " +
                 "FROM mobilewikia.wiki_article a, mobilewikia.wiki_content c " +
                 "WHERE c.article_id = a.article_id";
 
         try {
             rs = db_controller.getResultSet(query);
             if(null == rs)
                 return null;
 
             while (rs.next()) {
                 result.add(rs.getString("a.title"));
                 result.add(rs.getString("c.tag"));
             }
 
         } catch (SQLException e) {
             return null;
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
 
             } catch (SQLException e) {
                 System.err.println(e.toString());
             }
         }
 
         StringBuilder sb = new StringBuilder();
         for(String item : result) {
             sb.append(item);
             sb.append('\n');
         }
 
         return sb.toString();
     }
 }
