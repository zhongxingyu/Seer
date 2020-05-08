 package at.ac.tuwien.sepm.dao.hsqldb;
 
 import at.ac.tuwien.sepm.dao.TodoDao;
 import at.ac.tuwien.sepm.entity.Todo;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Repository;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Markus MUTH
  */
 @Repository
 public class DBTodoDao extends DBBaseDao implements TodoDao {
     private static final int MAX_LENGTH_NAME=200;
     private static final int MAX_LENGTH_DESCRIPTION=500;
 
     @Autowired
     DBLvaDao lvaDao;
 
     @Autowired
     DBMetaLvaDao metaLvaDao;
 
     @Override
     public boolean create(Todo toCreate) throws IOException, DataAccessException {
         if(toCreate == null) {
             return false;
         } if(toCreate.getName()!=null && toCreate.getName().length()>MAX_LENGTH_NAME) {
             throw new IOException(ExceptionMessages.tooLongName(MAX_LENGTH_NAME));
         } if(toCreate.getDescription()!=null && toCreate.getDescription().length()>MAX_LENGTH_DESCRIPTION) {
             throw new IOException(ExceptionMessages.tooLongDescription(MAX_LENGTH_DESCRIPTION));
         }
 
         Integer lva=null;
         if(toCreate.getLva()!=null) {
             lva=toCreate.getLva().getId();
         }
 
         String stmt = "INSERT INTO todo (id,lva,name,description,done) VALUES (null,?,?,?,?);";
         jdbcTemplate.update(stmt, lva, toCreate.getName(), toCreate.getDescription(), toCreate.getDone());
 
         return true;
     }
 
     @Override
     public Todo readById(int id) throws DataAccessException {
         if(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM todo WHERE id=?", RowMappers.getIntegerRowMapper(), id) == 0){
             return null;
         }
 
         String stmt="SELECT * FROM todo WHERE id=?;";
         Todo result = jdbcTemplate.queryForObject(stmt, RowMappers.getTodoRowMapper(), id);
 
         result.setLva(lvaDao.readByIdWithoutLvaDates(result.getLva().getId()));
 
         return result;
     }
 
     @Override
     public List<Todo> readByDone(boolean done) throws DataAccessException {
         if(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM todo WHERE done=?", RowMappers.getIntegerRowMapper(), done) == 0){
             return new ArrayList<Todo>();
         }
 
         String stmt="SELECT * FROM todo WHERE done=?;";
         List<Todo> result = jdbcTemplate.query(stmt, RowMappers.getTodoRowMapper(), done);
 
         for(int i=0; i<result.size(); i++) {
             result.set(i, readById(result.get(i).getId()));
         }
 
         return result;
     }
 
     @Override
     public boolean update(Todo toUpdate) throws IOException, DataAccessException {
         if(toUpdate == null) {
             return false;
         } if(toUpdate.getName()!=null && toUpdate.getName().length()>MAX_LENGTH_NAME) {
             throw new IOException(ExceptionMessages.tooLongName(MAX_LENGTH_NAME));
         } if(toUpdate.getDescription()!=null && toUpdate.getDescription().length()>MAX_LENGTH_DESCRIPTION) {
             throw new IOException(ExceptionMessages.tooLongDescription(MAX_LENGTH_DESCRIPTION));
         }
 
         String stmt = "SELECT COUNT(*) FROM todo WHERE id=?";
         if(jdbcTemplate.queryForObject(stmt, RowMappers.getIntegerRowMapper(), toUpdate.getId()) == 0) {
             return false;
         }
 
         String stmtUpdateLva = "UPDATE todo SET lva=? WHERE id=?";
         String stmtUpdateName = "UPDATE todo SET name=? WHERE id=?";
         String stmtUpdateDescription = "UPDATE todo SET description=? WHERE id=?";
         String stmtUpdateDone = "UPDATE todo SET done=? WHERE id=?";
 
         if(toUpdate.getLva() != null) {
             jdbcTemplate.update(stmtUpdateLva, toUpdate.getLva().getId(), toUpdate.getId());
         }
         if(toUpdate.getName() != null) {
             jdbcTemplate.update(stmtUpdateName, toUpdate.getName(), toUpdate.getId());
         }
         if(toUpdate.getDescription() != null) {
             jdbcTemplate.update(stmtUpdateDescription, toUpdate.getDescription(), toUpdate.getId());
         }
         if(toUpdate.getDone() != null) {
             jdbcTemplate.update(stmtUpdateDone, toUpdate.getDone(), toUpdate.getId());
         }
         return true;
     }
 
     @Override
     public boolean delete(int id) throws DataAccessException {
         if(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM todo WHERE id=?", RowMappers.getIntegerRowMapper(), id) == 0){
             return false;
         }
 
         String stmt="DELETE FROM todo WHERE id=?";
         jdbcTemplate.update(stmt, id);
 
         return true;
     }
 
     @Override
     public List<Todo> getAllTodos() throws DataAccessException {
         String query = "SELECT * FROM todo";
         List<Todo> result = jdbcTemplate.query(query, RowMappers.getTodoRowMapper());
         for(int i=0; i<result.size(); i++) {
             result.set(i, readById(result.get(i).getId()));
            //if(result.get(i).getLva() != null)    //todo privater termin = null aber es kommt immer lva = lva mit id 0 anstatt null
                 result.get(i).getLva().setMetaLVA(metaLvaDao.readById(result.get(i).getLva().getMetaLVA().getId()));
             //else
            //     result.get(i).setLva(null);
         }
         return result;
     }
 }
