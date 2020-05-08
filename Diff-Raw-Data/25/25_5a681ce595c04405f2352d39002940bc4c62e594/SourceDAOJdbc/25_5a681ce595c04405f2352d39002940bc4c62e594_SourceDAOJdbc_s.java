 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.kurochenko.ispub.source.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import javax.sql.DataSource;
 import net.kurochenko.ispub.author.form.Author;
 import net.kurochenko.ispub.source.form.Source;
 import net.kurochenko.ispub.utils.DBUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author kurochenko
  */
 @Repository
 public class SourceDAOJdbc implements SourceDAO {
     
     private static Logger logger = Logger.getLogger(SourceDAOJdbc.class);
 
     @Autowired
     private DataSource ds;
     
     @Override
     public void insert(Source source) {
         if (source == null) {
             throw new IllegalArgumentException("Source is null");
         }
         
         Connection conn = null;
 
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("INSERT INTO source "
                     + "(source) VALUES "
                     + "(?)",
                     Statement.RETURN_GENERATED_KEYS);
 
             st.setString(1, source.getName());
            
 
             int count = st.executeUpdate();
            source.setID(DBUtils.getId(st.getGeneratedKeys()));
             assert count == 1;
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Failed to add source", ex);
             throw new RuntimeException("Failed to add source", ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public void remove(Long id) {
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             
             PreparedStatement stSA = conn.prepareStatement("DELETE * FROM author_source WHERE source=?");
             PreparedStatement stS = conn.prepareStatement("DELETE * FROM source WHERE idsource=?");
             
             stSA.setLong(1, id);
             stS.setLong(1, id);
             
             stSA.executeUpdate();
             stS.executeUpdate();
             
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when removing source",ex);
             throw new RuntimeException("Error when removing source",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public Source getByID(Long sourceID) {
         if (sourceID == null) {
             throw new IllegalArgumentException("Source ID is null");
         }
         
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM source WHERE idsource=?");
             st.setLong(1, sourceID);
 
             ResultSet rs = st.executeQuery();
             if (rs.next()) {
                 Source source = rowToSource(rs);
                 assert !rs.next();
                 return source;
             } else {
                 return null;
             }
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting source by ID from DB",ex);
             throw new RuntimeException("Error when getting source by ID from DB");
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public Source getByName(String name) {
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM source WHERE source LIKE ?");
             st.setString(1, name);
 
             ResultSet rs = st.executeQuery();
             if (rs.next()) {
                 Source source = rowToSource(rs);
                 assert !rs.next();
                 return source;
             } else {
                 return null;
             }
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting source by name from DB",ex);
             throw new RuntimeException("Error when getting source by name from DB");
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public List<Source> list() {
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM source");
             ResultSet rs = st.executeQuery();
             List<Source> found = new ArrayList<Source>();
             while(rs.next()) {
                 found.add(rowToSource(rs));
             }
             return found;
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting all sources from DB",ex);
             throw new RuntimeException("Error when getting all sources from DB",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public List<Source> getByAuthor(Author author) {
         if (author == null) {
             throw new IllegalArgumentException("Author is null");
         }
         if (author.getId() == null) {
             throw new IllegalArgumentException("Author ID is null");
         }
         
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM source "
                     + "WHERE idsource "
                     + "IN (SELECT source FROM author_source WHERE author=?)");
             st.setLong(1, author.getId());
             ResultSet rs = st.executeQuery();
             List<Source> found = new ArrayList<Source>();
             while(rs.next()) {
                 found.add(rowToSource(rs));
             }
             return found;
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting sources for author from DB",ex);
             throw new RuntimeException("Error when getting sources for author from DB",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
     
     private Source rowToSource(ResultSet rs) throws SQLException {
         if (rs == null) {
             throw new IllegalArgumentException("ResultSet is null");
         }
         Source result = new Source();
        result.setID(rs.getLong("idsource"));
         result.setName(rs.getString("source"));
         return result;
     }    
 }
