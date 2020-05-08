 package com.cse454.nel;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DocumentConnect extends MySQLConnect {
 
 	public DocumentConnect() throws SQLException {
 		super(defaultUrl, "sentences");
 	}
 
 	public void EntityUpdate(int sentenceId, String entityString) {
 		String sql = "UPDATE sentences SET entities = ? where sentenceID = ?";
 		PreparedStatement st = null;
 		try {
 			st = connection.prepareStatement(sql);
 			st.setString(1, entityString);
 			st.setInt(2, sentenceId);
 			st.executeUpdate();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (st != null) {
 				try {
 					st.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public String getArticleId(int docID) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 
 		try {
 			st = connection.prepareStatement("SELECT docName FROM sentences WHERE docID = ? LIMIT 1");
 			st.setInt(1, docID);
 			rs = st.executeQuery();
 
 			rs.first();
 			String articleID = rs.getString("docName");
 
 			return articleID;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 	}
 
 	public List<Sentence> getDocumentById(int docID) throws SQLException {
 		PreparedStatement st = null;
 		try {
 			st = connection.prepareStatement("SELECT sentenceID, tokens, ner FROM sentences WHERE docID = ? ORDER BY sentenceID");
 			st.setInt(1,docID);
 		} catch (Exception e) {
 			throw e;
 		}
 		return getDocument(st);
 	}
 
 	public List<Sentence> getDocumentByName(String name) throws SQLException {
 		PreparedStatement st = null;
 		try {
			st = connection.prepareStatement("SELECT sentenceID, tokens, ner FROM sentences WHERE docID = ? ORDER BY sentenceID");
 			st.setString(1, name);
 		} catch (Exception e) {
 			throw e;
 		}
 		return getDocument(st);
 	}
 
 	public List<Sentence> getDocument(PreparedStatement st) throws SQLException {
 		ResultSet rs = null;
 
 		try {
 			rs = st.executeQuery();
 
 			List<Sentence> sentences = new ArrayList<Sentence>();
 			while (rs.next()) {
 				Sentence sentence = new Sentence(rs.getInt(1), rs.getString(2), rs.getString(3));
 				sentences.add(sentence);
 			}
 
 			return sentences;
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
