 package se.cygni.expenses.jdbi;
 
 import org.h2.jdbcx.JdbcConnectionPool;
 import org.junit.Test;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import se.cygni.expenses.api.Event;
 
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class EventsRepositoryTest {
 
     @Test
     public void shouldListEvents() {
         //given
         JdbcConnectionPool ds = JdbcConnectionPool.create("jdbc:h2:mem:test2", "sa", "");
 
         DBI dbi = new DBI(ds);
 
         createTables(dbi);
 
         insertEvent(dbi, new Event(1, "Cancun", new Date(0)));
 
         EventsRepository target = dbi.open(EventsRepository.class);
 
         //when
         List<Event> result = target.findAll();
         //then
 
         assertThat("result contains two elements", result.size(), is(2));
 
         ds.dispose();
 
 
     }
 
     private void insertEvent(DBI dbi, Event event) {
         Handle handle = dbi.open();
 
         handle.execute("insert into event " +
                 "(id, name, date) " +
                 "values " +
                 "(" + event.getId() + "," +
                "(" + event.getName() + "," +
                "(" + event.getDate() + ")");
 
     }
 
     private void createTables(DBI dbi) {
 
         Handle handle = dbi.open();
 
         handle.execute("CREATE TABLE IF NOT EXISTS event (" +
                 "id BIGINT, " +
                "description VARCHAR(40), " +
                 "PRIMARY KEY(id)" +
                 ")");
 
         handle.execute("CREATE TABLE IF NOT EXISTS expense (" +
                 "id BIGINT, " +
                 "description VARCHAR(40), " +
                 "person VARCHAR(55), " +
                 "date TIMESTAMP, " +
                 "amount INT, " +
                 "eventId BIGINT, " +
                 "PRIMARY KEY(id), " +
                 "FOREIGN KEY(eventId) REFERENCES event(id)" +
                 ")");
 
         handle.close();
     }
 
 }
