 package net.kurochenko.ispub.author.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import javax.sql.DataSource;
 import net.kurochenko.ispub.author.form.Author;
 import net.kurochenko.ispub.department.dao.DepartmentDAO;
 import net.kurochenko.ispub.source.dao.SourceDAO;
 import net.kurochenko.ispub.utils.DBUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author Andrej Kuročenko <kurochenko@mail.muni.cz>
  */
 @Repository
 public class AuthorDAOJdbc implements AuthorDAO {
  
     private static Logger logger = Logger.getLogger(AuthorDAOJdbc.class);
     
     @Autowired
     private DataSource ds;
     
     @Autowired
     private SourceDAO sourceDAO;
     
     @Autowired
     private DepartmentDAO departmentDAO;
 
     @Override
     public void insert(Author author) {
         if (author == null) {
             throw new IllegalArgumentException("Author is null");
         }
         if (author.getId() != null) {
             throw new IllegalArgumentException("Authors ID is set. You may want to update.");
         }
         
         Connection conn = null;
 
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("INSERT INTO author "
                     + "(me_id, name, surname, note) VALUES "
                     + "(?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS);
 
             st.setString(1, author.getMeId());
             st.setString(2, author.getName());
             st.setString(3, author.getSurname());
             st.setString(4, author.getNote());
            
 
             int count = st.executeUpdate();
             author.setId(DBUtils.getId(st.getGeneratedKeys()));
             assert count == 1;
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Failed to add Author", ex);
             throw new RuntimeException("Failed to add Author", ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public void update(Author author) {
         if (author == null) {
             throw new IllegalArgumentException("Author is null");
         }
         if (author.getId() == null) {
             throw new IllegalArgumentException("Authors ID is null. You may want to insert.");
         }
         
         Connection conn = null;
 
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("UPDATE author SET "
                     + "name=? "
                     + "surname=? "
                     + "me_id=? "
                     + "note=? "
                     + "WHERE idauthor=?");
             
             st.setString(1, author.getName());
             st.setString(2, author.getSurname());
             st.setString(3, author.getMeId());
             st.setString(4, author.getNote());
             st.setLong(2, author.getId());
 
             int count = st.executeUpdate();
             if (count == 0) {
                 throw new IllegalArgumentException("Contact with given ID not found");
             }
             assert count == 1;
             
             updateSources(author);
             updateDepartments(author);
             
             
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Update of contact failed", ex);
             throw new RuntimeException("Error while updating contact", ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
     
     private void updateSources(Author author) {
         if (author == null) {
             throw new IllegalArgumentException("Author is null");
         }
         
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             
             PreparedStatement st = conn.prepareStatement("DELETE * FROM author_source WHERE author=?");
                         
             st.setLong(1, author.getId());
             st.executeUpdate();
             
             for (int i = 0; i < author.getSources().size(); i++) {
                if (author.getSources().get(i).getId() == null) {
                     sourceDAO.insert(author.getSources().get(i));            
                 }
                 
                 st = conn.prepareStatement("INSERT INTO author_source (author, source) VALUES (?,?)");
                 st.setLong(1, author.getId());
                st.setLong(2, author.getSources().get(i).getId());
                 
                 st.executeUpdate();
             }
             
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when updating author sources",ex);
             throw new RuntimeException("Error when updating author sources",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
     
     private void updateDepartments(Author author) {
         if (author == null) {
             throw new IllegalArgumentException("Author is null");
         }
         
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             
             PreparedStatement st = conn.prepareStatement("DELETE * FROM author_department WHERE author=?");
                         
             st.setLong(1, author.getId());
             st.executeUpdate();
             
             for (int i = 0; i < author.getDepartments().size(); i++) {
                 if (author.getDepartments().get(i).getID() == null) {
                     departmentDAO.save(author.getDepartments().get(i));            
                 }
                 
                 st = conn.prepareStatement("INSERT INTO author_department (author, department) VALUES (?,?)");
                 st.setLong(1, author.getId());
                 st.setLong(2, author.getDepartments().get(i).getID());
                 
                 st.executeUpdate();
             }
             
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when updating author departments",ex);
             throw new RuntimeException("Error when updating author departments",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public void remove(Integer idAuthor) {
         if (idAuthor == null) {
             throw new IllegalArgumentException("Author ID is null");
         }
         
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             
             PreparedStatement stAS = conn.prepareStatement("DELETE * FROM author_source WHERE author=?");
             PreparedStatement stAD = conn.prepareStatement("DELETE * FROM author_department WHERE author=?");
             PreparedStatement stA = conn.prepareStatement("DELETE * FROM author WHERE idauthor=?");
             
             stAS.setInt(1, idAuthor);
             stAD.setInt(1, idAuthor);
             stA.setInt(1, idAuthor);
             
             stAS.executeUpdate();
             stAD.executeUpdate();
             stA.executeUpdate();
             
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when removing author",ex);
             throw new RuntimeException("Error when removing author",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public Author getByID(Integer idAuthor) {
         if (idAuthor == null) {
             throw new IllegalArgumentException("Author ID is null");
         }
         
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "SELECT * FROM author WHERE idauthor=?");
             st.setLong(1, idAuthor);
 
             ResultSet rs = st.executeQuery();
             if (rs.next()) {
                 Author author = rowToAuthor(rs);
                 author.setSources(sourceDAO.getByAuthor(author));
                 author.setDepartments(departmentDAO.getByAuthor(author));
                 assert !rs.next();
                 return author;
             } else {
                 return null;
             }
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting author by ID from DB",ex);
             throw new RuntimeException("Error when getting author by ID from DB");
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
 
     @Override
     public List<Author> list() {
         Connection conn = null;
         
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM author");
             ResultSet rs = st.executeQuery();
             List<Author> found = new ArrayList<Author>();
             while(rs.next()) {
                 Author author = rowToAuthor(rs);
                 author.setSources(sourceDAO.getByAuthor(author));
                 author.setDepartments(departmentDAO.getByAuthor(author));
                 found.add(author);
             }
             return found;
         } catch (SQLException ex) {
             logger.log(Level.ERROR, "Error when getting all contacts from DB",ex);
             throw new RuntimeException("Error when getting all contacts from DB",ex);
         } finally {
             DBUtils.closeConnection(conn);
         }
     }
     
     private Author rowToAuthor(ResultSet rs) throws SQLException {
         if (rs == null) {
             throw new IllegalArgumentException("ResultSet is null");
         }
         Author result = new Author();
         result.setName(rs.getString("name"));
         result.setSurname(rs.getString("surname"));
         result.setMeId(rs.getString("me_id"));
         result.setNote(rs.getString("note"));
         result.setId(rs.getLong("idauthor"));
         return result;
     }
     
 }
