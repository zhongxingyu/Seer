 package org.freecode.irc.votebot.dao;
 
 import org.freecode.irc.votebot.entity.Poll;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 import org.springframework.jdbc.support.GeneratedKeyHolder;
 import org.springframework.jdbc.support.KeyHolder;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.Future;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deprecated
  * Date: 11/21/13
  * Time: 7:33 PM
  */
 public class PollDAO extends JdbcDaoSupport {
     private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS polls (id INTEGER PRIMARY KEY AUTOINCREMENT, question string NOT NULL, options string NOT NULL DEFAULT 'yes,no,abstain', closed BOOLEAN DEFAULT 0, expiry INTEGER DEFAULT 0, creator STRING DEFAULT 'null')";
     private static final String GET_OPEN_POLL_BY_ID = "SELECT * FROM polls WHERE id = ? AND closed = 0 LIMIT 1";
    private static final String GET_POLL_BY_STRING = "SELECT * FROM polls WHERE question LIKE %?% ORDER BY id ASC";
     private static final String GET_POLL_BY_ID = "SELECT * FROM polls WHERE id = ? LIMIT 1";
     private static final String GET_OPEN_POLLS_THAT_EXPIRED = "SELECT * FROM polls WHERE closed = 0 AND expiry > ?";
     private static final String SET_POLL_STATUS_BY_ID = "UPDATE polls SET closed = ? WHERE id = ? AND closed = ?";
     private static final String ADD_NEW_POLL = "INSERT INTO polls(question, expiry, creator) VALUES (?,?,?)";
     public HashMap<Integer, Future> futures = new HashMap<>();
 
     public void createTable() throws SQLException {
         getJdbcTemplate().execute(CREATE_TABLE);
     }
 
     public Poll getOpenPoll(int id) throws SQLException {
         try {
             return getJdbcTemplate().queryForObject(GET_OPEN_POLL_BY_ID,
                     new Object[]{id},
                     new BeanPropertyRowMapper<>(Poll.class));
         } catch (EmptyResultDataAccessException empty) {
             return null;
         }
     }
 
     public int addNewPoll(final String question, final long expiry, final String creator) throws SQLException {
         PreparedStatementCreator psc = new PreparedStatementCreator() {
             public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                 PreparedStatement ps = con.prepareStatement(ADD_NEW_POLL);
                 ps.setString(1, question);
                 ps.setLong(2, expiry);
                 ps.setString(3, creator);
                 return ps;
             }
         };
 
         KeyHolder keyHolder = new GeneratedKeyHolder();
         getJdbcTemplate().update(psc, keyHolder);
         return keyHolder.getKey().intValue();
     }
 
     public Poll getPoll(int id) throws SQLException {
         try {
             return getJdbcTemplate().queryForObject(GET_POLL_BY_ID,
                     new Object[]{id},
                     new BeanPropertyRowMapper<>(Poll.class));
         } catch (EmptyResultDataAccessException empty) {
             return null;
         }
     }
 
     public Poll[] getOpenPolls() throws SQLException {
         try {
             List<Poll> polls = getJdbcTemplate().query(GET_OPEN_POLLS_THAT_EXPIRED,
                     new Object[]{System.currentTimeMillis()},
                     new BeanPropertyRowMapper<>(Poll.class));
             return polls.toArray(new Poll[polls.size()]);
         } catch (EmptyResultDataAccessException empty) {
             return new Poll[]{};
         }
     }
 
     public Poll[] getPollsContaining(String phrase) throws SQLException {
         try {
             List<Poll> polls = getJdbcTemplate().query(GET_POLL_BY_STRING,
                     new Object[]{phrase},
                     new BeanPropertyRowMapper<>(Poll.class));
             return polls.toArray(new Poll[polls.size()]);
         } catch (EmptyResultDataAccessException empty) {
             return new Poll[]{};
         }
     }
 
     public int setStatusOfPoll(final int id, final boolean status) throws SQLException {
         return getJdbcTemplate().update(SET_POLL_STATUS_BY_ID, status, id, !status);
     }
 
 }
