 package com.cse454.nel.mysql;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.cse454.nel.CrossWikiData;
 import com.cse454.nel.PullFromWikipedia;
 
 public class WikiConnect extends MySQLConnect {
 	private static String defaultDB = "wikidb";
 	
 	private PullFromWikipedia wikipedia;
 
 	private Map<String, String> page_textCache; // page_latest -> text
 
 	public WikiConnect() throws SQLException {
         super(defaultUrl, defaultDB);
         
         wikipedia = new PullFromWikipedia(); 
 
         page_textCache = new HashMap<String, String>();
 	}
 
 	/**
 	 * Retrieves entities from Google Cross Wiki Data based on the given entity mention.
 	 * @param entityMention the string to lookup
 	 * @param removeDisambiguation true to remove obvious disambiguation pages from results
 	 */
 	public List<CrossWikiData> GetCrossWikiDocs(String entityMention, boolean removeDisambiguation) throws Exception {
 
 		String query = "Select * from crosswiki where mention = ? order by likelihood desc limit 10";
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		List<CrossWikiData> crossWikiData = new ArrayList<CrossWikiData>();
 		try {
 			st = connection.prepareStatement(query);
 			st.setString(1, entityMention);
 			rs = st.executeQuery();
 			while (rs.next()) {
 				if (removeDisambiguation && rs.getString(3).contains("_(disambiguation)")) {
 					continue;
 				}
 				crossWikiData.add(new CrossWikiData(rs.getString(1), rs.getDouble(2), rs.getString(3)));
 			}
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return crossWikiData;
 	}
 
 	/**
 	 *
 	 * @param query
 	 * @param pages <page id, page title>
 	 * @param redirects <page id, page title>
 	 * @throws Exception
 	 */
 	public void GetPages(String query, final Map<String, String> pages, final Map<String, String> redirects) throws SQLException {
 		ExecuteQuery(
 				"SELECT page_id, page_title, page_is_redirect FROM page WHERE page_title LIKE '"+query.replaceAll("'", "''")+"' AND page_namespace = 0;",
 				new QueryResponder<Void>() {
 					public Void Result(ResultSet result) throws SQLException {
 						while (result.next()) {
 							// If this is a redirect
 							String id = result.getString(1);
 							String title = result.getString(2);
 							if (!result.getBoolean(3)) {
 								pages.put(id, title);
 							} else if (redirects != null) {
 								redirects.put(id, title);
 							}
 						}
 						return null;
 					}
 				}
 		);
 	}
 
 	/**
 	 * Does not sanitize pageID
 	 * @param pages a set of page id's
 	 * @param pageID
 	 * @throws Exception
 	 */
 	public void GetPageLinks(final Set<String> pages, String pageID) throws SQLException {
 		ExecuteQuery(
 				"SELECT pl_title FROM pagelinks WHERE pl_from = "+pageID+" AND pl_namespace = 0;",
 				new QueryResponder<Void>() {
 					public Void Result(ResultSet result) throws SQLException {
 						while (result.next()) {
 							pages.add(result.getString(1));
 						}
 						return null;
 					}
 				}
 		);
 	}
 
 	public String GetArticleName(String pageID) throws SQLException {
 		return ExecuteQuery(
 					"SELECT page_title FROM page WHERE page_id = " + pageID,
 					new QueryResponder<String>() {
 						public String Result(ResultSet result) throws SQLException {
 							if (result.next())
 								return result.getString(1);
 							else
 								return null;
 						}
 					});
 	}
 
 	/**
 	 * Returns the number of in-links to an article with the given title
 	 * @param title the article title (exact)
 	 */
 	public int GetInlinks(String title) throws SQLException {
 		return ExecuteQuery(
 				"SELECT COUNT(pl_from) FROM pagelinks WHERE pl_title = '" + title.replaceAll("'", "''") + "'",
 				new QueryResponder<Integer>() {
 					public Integer Result(ResultSet result) throws SQLException {
 						if (result.next()) {
 							return result.getInt(1);
 						} else {
 							throw new SQLException("No count returned.");
 						}
 					}
 				}
 		);
 	}
 
 
 	private String ReplaceWhileEffective(String str, String rgx, String replace) {
 		String oldStr;
 		do {
 			oldStr = str;
 			str = str.replaceAll(rgx, replace);
 		} while (str != oldStr);
 		return str;
 	}
 
 	/**
 	 *
 	 * @param str
 	 * @param startStr
 	 * @param endStr must be same length as startStr (or exceptions will ensue)
 	 * @return
 	 */
 	private String RemoveRecursiveStruct(String str, String startStr, String endStr) {
 		int tokLen = startStr.length();
 		String ret = "";
 
 		while (true) {
 			int start = str.indexOf(startStr);
 			if (start >= 0) {
 				ret += str.substring(0, start);
 				str = str.substring(start);
 
 				// Now find the end of this struct
 				int depth = 1;
 				int i = startStr.length();
 				int len = str.length() - tokLen;
 				while (i <= len) {
 					String substr = str.substring(i, i+tokLen);
 					if (substr.equals(startStr)) {
 						++depth;
 					} else if (substr.equals(endStr)) {
 						--depth;
 						if (depth == 0) {
 							i += tokLen;
 							break;
 						}
 					}
 					++i;
 				}
 
 				// We have found the whole struct, remove it.
 				str = str.substring(i);
 
 			} else {
 				ret += str;
 				break;
 			}
 		}
 
 
 		return ret;
 	}
 
 	public String GetCleanedWikiText(String pageID) throws Exception {
 		String text = GetWikiText(pageID);
 		if (text == null) {
 			return "";
 		}
 		text = text.replaceAll("#REDIRECT", "");			// Redirects
 		text = text.replaceAll("(?s:\\{\\|.*?\\|\\})", ""); // Tables {| ... |}
 		text = RemoveRecursiveStruct(text, "{{", "}}");
 
 		String noDoubleBracketRgx = "(?:(?:[^\\[\\|])|(?:\\[(?!\\[)))+?";
 		String innerDoubleBracketRgx =
 				"(?s:\\[\\[" +				// opening brackets
 					"(?:" + noDoubleBracketRgx + "\\|)*?" + // stuff before visible text
 					"(" + noDoubleBracketRgx + ")\\|?" +	// visible text
 				"\\]\\])";
 		text = ReplaceWhileEffective(text, innerDoubleBracketRgx, "$1"); // [[ ... ]] links
 		text = text.replaceAll("<[^>]+>", ""); // html
 
 		return text;
 	}
 
 	public boolean doesWikiPageExist(final String pageTitle) throws Exception {
 		String query = "SELECT page_title from page where page_title = ?";
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try {
 			st = connection.prepareStatement(query);
 			st.setString(1, pageTitle);
 			rs = st.executeQuery();
 			if (rs.next()) {
 				return true;
 			}
 			return false;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 	}
 	/**
 	 * Returns the wiki article text for the given id. Does not sanitize pageID
 	 * @param pageID
 	 * @return
 	 * @throws Exception
 	 */
 	public String GetWikiText(final String pageTitle) throws Exception {
 		if (page_textCache.containsKey(pageTitle)) {
 			return page_textCache.get(pageTitle);
 		}
 
 		String query = "SELECT text.old_text " +
 					   "FROM page " +
 					   "	LEFT JOIN revision " +
 					   "		ON page.page_latest = revision.rev_id " +
 					   "	LEFT JOIN text " +
 					   "		ON text.old_id = revision.rev_text_id " +
 					   "WHERE page.page_namespace = 0 and page.page_title = ?";
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement(query);
 			st.setString(1, pageTitle);
 			rs = st.executeQuery();
 			if (rs.next()) {
 				String text = rs.getString(1);
 				page_textCache.put(pageTitle, text);
 				return text;
 			}
 			
 			System.out.println("Importing Wikipedia text for: "+pageTitle);
 			String text = wikipedia.GetWikipediaText(pageTitle);
 			if (text == null) {
 				System.err.println("No wiki text returned or found for: " + pageTitle);
 			}
 			else
 			{
 				AddPage(pageTitle, text);
				return GetWikiText(pageTitle);
 			}
 			
 			return null;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 	}
 	
 	public void AddPage(String name, String text) throws Exception {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			// Insert text
 			st = connection.prepareStatement("INSERT INTO text (old_text, old_flags) VALUES (?, ' ')", Statement.RETURN_GENERATED_KEYS);
 			st.setString(1, text);
 			st.executeUpdate();
 			
 			int old_text_id = -1;
 			rs = st.getGeneratedKeys();
 			if (rs.next()) {
 				old_text_id = rs.getInt(1);
 			} else {
 				throw new Exception("Failed to generate text key");
 			}
 			
 			rs.close();
 			st.close();
 			
 			// Insert revision
 			st = connection.prepareStatement("INSERT INTO revision (rev_text_id, rev_page, rev_comment, rev_user, rev_user_text, rev_timestamp, rev_minor_edit, rev_deleted, rev_len, rev_parent_id, rev_sha1) VALUES (?, 0, ' ', 0, ' ', ' ', 0, 0, 0, 0, ' ')", Statement.RETURN_GENERATED_KEYS);
 			st.setInt(1, old_text_id);
 			st.executeUpdate();
 			
 			int rev_id = -1;
 			rs = st.getGeneratedKeys();
 			if (rs.next()) {
 				rev_id = rs.getInt(1);
 			} else {
 				throw new Exception("Failed to generate revision key");
 			}
 			
 			rs.close();
 			st.close();
 			
 			// Insert page
 			st = connection.prepareStatement("INSERT INTO page (page_namespace, page_title, page_restrictions, page_counter, page_is_redirect, page_is_new, page_random, page_touched, page_latest, page_len) " +
 											 " VALUES          (0,              ?,          ' ',               0,            0,                0,           0,           ' ',          ?,           0)");
 			st.setString(1, name);
 			st.setInt(2, rev_id);
 			st.executeUpdate();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 	}
 
 }
