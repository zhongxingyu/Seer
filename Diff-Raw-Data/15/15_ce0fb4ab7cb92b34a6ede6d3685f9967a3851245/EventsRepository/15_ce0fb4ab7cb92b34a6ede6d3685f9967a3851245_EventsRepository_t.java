 package se.cygni.expenses.jdbi;
 
 import org.skife.jdbi.v2.sqlobject.BindBean;
 import org.skife.jdbi.v2.sqlobject.SqlQuery;
 import org.skife.jdbi.v2.sqlobject.SqlUpdate;
 import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
 import se.cygni.expenses.api.Event;
 
 import java.util.List;
 
 @RegisterMapper(EventMapper.class)
 public interface EventsRepository {
 
    String TABLE_NAME = "event";
 
    String CREATE_TABLE_STATEMENT =
             "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                 "id SERIAL, " +
                 "name VARCHAR(40), " +
                 "date TIMESTAMP, " +
                 "PRIMARY KEY(id)" +
                 ")";
 
     @SqlQuery("select * from event")
     List<Event> findAll();
 
     @SqlUpdate("insert into event (name, date) values (:name, :date)")
     void add(@BindBean Event event);
 }
