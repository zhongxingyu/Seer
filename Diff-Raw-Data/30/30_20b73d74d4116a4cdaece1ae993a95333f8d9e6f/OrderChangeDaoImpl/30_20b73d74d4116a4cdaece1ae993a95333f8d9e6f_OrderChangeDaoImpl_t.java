 package com.exadel.borsch.dao.impl;
 
 import com.exadel.borsch.dao.BorschJdbcDaoSupport;
 import com.exadel.borsch.dao.OrderChangeDao;
 import com.exadel.borsch.entity.ChangeAction;
 import com.exadel.borsch.entity.OrderChange;
 import com.exadel.borsch.util.DateTimeUtils;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Andrew Zhilka
  */
 public class OrderChangeDaoImpl extends BorschJdbcDaoSupport implements OrderChangeDao {
     private static final String QUERY_TRUNCATE_TABLE = "TRUNCATE TABLE OrderChanges";
 
     private static final String QUERY_SELECT_CHANGE = "SELECT * FROM OrderChanges ";
 
     private static final String QUERY_SELECT_CHANGE_BY_ID = QUERY_SELECT_CHANGE + "WHERE changeId=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_DISH_ID = QUERY_SELECT_CHANGE + "WHERE changedDishId=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_MENU_ITEM_ID = QUERY_SELECT_CHANGE + "WHERE menuItemId=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_DATE = QUERY_SELECT_CHANGE + "WHERE dateOfChange=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_USER_AND_MENU_ITEM_ID = QUERY_SELECT_CHANGE
             + "WHERE actedUserId=?,menuItemId=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_WEEK_START = QUERY_SELECT_CHANGE + "WHERE startOfWeek=?";
 
     private static final String QUERY_SELECT_CHANGE_BY_USER_ID = QUERY_SELECT_CHANGE + "WHERE actedUserID=?";
 
     private static final String QUERY_DELETE_CHANGE = "DELETE FROM OrderChanges chageId=?";
 
     private static final String QUERY_DELETE_BY_USER_ID = "DELETE FROM OrderChanges WHERE actedUserId=?";
 
     private static final String QUERY_DELETE_BY_MENU_ITEM_ID = "DELETE FROM OrderChanges WHERE menuItemId=?";
 
     private static final String QUERY_DELETE_BY_DISH_ID = "DELETE FROM OrderChanges WHERE changedDishId=?";
 
     private static final String QUERY_UPDATE_CHANGE = "UPDATE OrderChanges SET "
             + "changedDishId=?,actedUserId=?,menuItemId=?,dateOfChange=?,committedAction=? WHERE changeId=?";
 
     private static final RowMapper<OrderChange> CHANGE_ROW_MAPPER = new RowMapper<OrderChange>() {
         @Override
         public OrderChange mapRow(ResultSet rs, int rowNum) throws SQLException {
             return new OrderChange(
                     rs.getLong("changeId"),
                     rs.getLong("changedDishId"),
                     rs.getLong("actedUserId"),
                     rs.getLong("menuItemId"),
                     new DateTime(rs.getDate("dateOfChange")),
                     ChangeAction.valueOf(rs.getString("committedAction"))
             );
         }
     };
 
     private static final Logger LOGGER = LoggerFactory.getLogger(OrderChangeDaoImpl.class);
 
     public OrderChangeDaoImpl(JdbcTemplate jdbcTemplate) {
         super(jdbcTemplate);
         setJdbcInsert(getJdbcInsert()
                 .withTableName("OrderChanges")
                 .usingGeneratedKeyColumns("changeId"));
     }
 
     @Override
     public OrderChange getById(Long id) {
         try {
             return getJdbcTemplate().queryForObject(
                     QUERY_SELECT_CHANGE_BY_ID,
                     new Object[]{id},
                     CHANGE_ROW_MAPPER
             );
         } catch (EmptyResultDataAccessException e) {
             LOGGER.trace("Error why trying to get change by ID", e);
             return null;
         }
     }
 
     @Override
     public void save(OrderChange change) {
         Map<String, Object> params = new HashMap<>();
         params.put("changedDishId", change.getChangedDishId());
         params.put("actedUserId", change.getActedUserId());
         params.put("menuItemId", change.getMenuItemId());
         params.put("dateOfChange", change.getDateOfChange().toDate());
         params.put("committedAction", change.getCommittedAction().name());
         Date startOfWeek = DateTimeUtils.getStartOfWeek(change.getDateOfChange()).toDate();
         params.put("startOfWeek", startOfWeek);
 
         change.setId((Long) getJdbcInsert()
                 .executeAndReturnKey(params));
     }
 
     @Override
     public void delete(Long id) {
         getJdbcTemplate().update(
                 QUERY_DELETE_CHANGE,
                 id
         );
     }
 
     @Override
     public void update(OrderChange change) {
         getJdbcTemplate().update(
                 QUERY_UPDATE_CHANGE,
                 change.getChangedDishId(),
                 change.getActedUserId(),
                 change.getMenuItemId(),
                 change.getDateOfChange().toDate(),
                 change.getCommittedAction().toString(),
                 change.getId()
         );
     }
 
     @Override
     public List<OrderChange> getByUserAndMenuItemId(OrderChange change, Long userId, Long menuItemId) {
         return getJdbcTemplate().query(
                 QUERY_SELECT_CHANGE_BY_USER_AND_MENU_ITEM_ID,
                 new Object[]{
                         change.getActedUserId(),
                         change.getMenuItemId()},
                 CHANGE_ROW_MAPPER
         );
     }
 
     @Override
     public List<OrderChange> getAllByDate(DateTime date) {
         return getJdbcTemplate().query(
                 QUERY_SELECT_CHANGE_BY_DATE,
                 new Object[]{date.toDate()},
                 CHANGE_ROW_MAPPER
         );
     }
 
     @Override
     public List<OrderChange> getAllByWeekStart(DateTime date) {
         return getJdbcTemplate().query(
                 QUERY_SELECT_CHANGE_BY_WEEK_START,
                 new Object[]{date.toDate()},
                 CHANGE_ROW_MAPPER
         );
     }
 
     @Override
     public List<OrderChange> getAllByDishId(Long dishId) {
         return getJdbcTemplate().query(
                 QUERY_SELECT_CHANGE_BY_DISH_ID,
                 new Object[]{dishId},
                 CHANGE_ROW_MAPPER
         );
     }
 
 
     @Override
     public List<OrderChange> getAllChangesByUser(Long userId) {
         return getJdbcTemplate().query(
                 QUERY_SELECT_CHANGE_BY_USER_ID,
                 new Object[]{userId},
                 CHANGE_ROW_MAPPER
         );
     }
 
     @Override
     public void truncateTable() {
         getJdbcTemplate().update(QUERY_TRUNCATE_TABLE);
     }
 
     @Override
     public void deleteAllByMenuItemId(Long menuItemId) {
         getJdbcTemplate().update(
                 QUERY_DELETE_BY_MENU_ITEM_ID,
                 menuItemId
         );
     }
 
     @Override
     public void deleteAllByDishId(Long dishId) {
         getJdbcTemplate().update(
                 QUERY_DELETE_BY_DISH_ID,
                 dishId
         );
     }
 
     @Override
     public void deleteAllbyUserId(Long userID) {
         getJdbcTemplate().update(
                 QUERY_DELETE_BY_USER_ID,
                 userID
         );
     }
 
 
 }
