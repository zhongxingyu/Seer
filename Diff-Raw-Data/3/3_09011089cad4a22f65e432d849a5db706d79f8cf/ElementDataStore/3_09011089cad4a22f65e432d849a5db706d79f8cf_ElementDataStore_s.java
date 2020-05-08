 package content.datastore.element;
 
 import content.handlers.ResultHandler;
 import content.handlers.ResultHandlerEx;
 import content.handlers.element.*;
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
 import org.springframework.jdbc.core.PreparedStatementSetter;
 import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 
 public class ElementDataStore {
 
   public void create(final String id, final InputStream stream, final ElementCreateHandler handler) {
     read(id, new ResultHandler<ElementRead>() {
       @Override
       public void success(ElementRead element) {
         handler.alreadyExists();
       }
 
       @Override
       public void notFound() {
         int rows = jdbcTemplate.update(INSERT, new PreparedStatementSetter() {
           @Override
           public void setValues(PreparedStatement ps) throws SQLException {
             ps.setBinaryStream(1, stream);
             ps.setString(2, id);
           }
         });
         if(rows <= 0) {
           handler.unableToUpdate();
         } else {
           handler.success();
         }
       }
     });
   }
 
   public void create(final String id, final ElementCreate element, final ElementCreateHandler handler) {
     read(id, new ResultHandler<ElementRead>() {
       @Override
       public void success(ElementRead element) {
         handler.alreadyExists();
       }
 
       @Override
       public void notFound() {
         int rows = jdbcTemplate.update(create.newPreparedStatementCreator(new Object[]{element.getValue(), id}));
         if (rows <= 0) {
           handler.unableToUpdate();
         } else {
           handler.success();
         }
       }
     });
   }
 
   public void read(String id, final ResultHandler<ElementRead> handler) {
     jdbcTemplate.query(select.newPreparedStatementCreator(new Object[]{id}), new ResultSetExtractor() {
       @Override
       public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
         if (rs.next()) {
           ElementRead element = new ElementRead();
           element.setValue(rs.getString(1));
           handler.success(element);
         } else {
           handler.notFound();
         }
         return null;
       }
     });
   }
 
   public void read(final String id, final ResultHandlerEx<BufferedInputStream> handler) {
     jdbcTemplate.query(select.newPreparedStatementCreator(new Object[]{id}), new ResultSetExtractor() {
       @Override
       public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
         if (rs.next()) {
           BufferedInputStream stream = null;
           try {
             stream = new BufferedInputStream(rs.getBinaryStream(1));
             handler.success(stream);
           } finally {
             try {
               if(stream != null) {
                 stream.close();
               }
             } catch (IOException e) {
               handler.exception(e);
             }
           }
         } else {
           handler.notFound();
         }
         return null;
       }
     });
   }
 
   public void update(final String id, final ElementModify element, final ElementModifyHandler handler) {
     read(id, new ResultHandler<ElementRead>() {
       @Override
       public void success(ElementRead element1) {
         int rows = jdbcTemplate.update(update.newPreparedStatementCreator(new Object[]{element.getValue(), id}));
         if (rows <= 0) {
           handler.unableToUpdate();
         } else {
           handler.success();
         }
       }
 
       @Override
       public void notFound() {
         handler.notFound();
       }
     });
   }
 
   public void update(final String id, final InputStream stream, final ElementModifyHandler handler) {
     read(id, new ResultHandler<ElementRead>() {
       @Override
       public void success(ElementRead element1) {
         int rows = jdbcTemplate.update(UPDATE, new PreparedStatementSetter() {
           @Override
           public void setValues(PreparedStatement ps) throws SQLException {
             ps.setBinaryStream(1, stream);
             ps.setString(2, id);
           }
         });
         if (rows <= 0) {
           handler.unableToUpdate();
         } else {
           handler.success();
         }
       }
 
       @Override
       public void notFound() {
         handler.notFound();
       }
     });
   }
 
   public void delete(final String id, final ElementDeleteHandler handler) {
     read(id, new ResultHandler<ElementRead>() {
       @Override
       public void success(ElementRead element) {
         int rows = jdbcTemplate.update(delete.newPreparedStatementCreator(new Object[]{id}));
         if (rows <= 0) {
           handler.unableToUpdate();
         } else {
           handler.success();
         }
       }
 
       @Override
       public void notFound() {
         handler.notFound();
       }
     });
   }
 
   public ElementDataStore(JdbcTemplate jdbcTemplate) {
     this.jdbcTemplate = jdbcTemplate;
    lobHandler = new DefaultLobHandler();
   }
 
   public static final String INSERT = "INSERT INTO `ELEMENT` (`VALUE`, `KEY`) VALUES (?, ?)";
 
   public static final String SELECT = "SELECT `VALUE` FROM `ELEMENT` WHERE `KEY`=?";
 
   public static final String UPDATE = "UPDATE `ELEMENT` SET `VALUE`=? WHERE `KEY`=?";
 
   public static final String DELETE = "DELETE FROM `ELEMENT` WHERE `KEY`=?";
 
   private final PreparedStatementCreatorFactory create = new PreparedStatementCreatorFactory(
     INSERT, new int[]{Types.VARCHAR, Types.VARCHAR, });
 
   private final PreparedStatementCreatorFactory select = new PreparedStatementCreatorFactory(
     SELECT, new int[]{Types.VARCHAR, });
 
   private final PreparedStatementCreatorFactory update = new PreparedStatementCreatorFactory(
     UPDATE, new int[]{Types.VARCHAR, Types.VARCHAR, });
 
   private final PreparedStatementCreatorFactory delete = new PreparedStatementCreatorFactory(
     DELETE, new int[]{Types.VARCHAR, });
 
   private final JdbcTemplate jdbcTemplate;
 
   private static final Logger log = Logger.getLogger(ElementDataStore.class);
 
 }
