 package se.cygni.expenses.jdbi;
 
 import org.skife.jdbi.v2.sqlobject.Bind;
 import org.skife.jdbi.v2.sqlobject.BindBean;
 import org.skife.jdbi.v2.sqlobject.SqlQuery;
 import org.skife.jdbi.v2.sqlobject.SqlUpdate;
 import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
 import se.cygni.expenses.api.Expense;
 
 import java.util.List;
 
 @RegisterMapper(ExpenseMapper.class)
 public interface ExpensesRepository {
 
    static final String TABLE_NAME = "expense";
 
    static final String CREATE_TABLE_STATEMENT =
             "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                     "id SERIAL, " +
                     "description VARCHAR(40), " +
                     "person VARCHAR(55), " +
                     "date TIMESTAMP, " +
                     "amount INT, " +
                     "eventId BIGINT, " +
                     "PRIMARY KEY(id), " +
                     "FOREIGN KEY(eventId) REFERENCES event(id)" +
                     ")";
 
     @SqlQuery("select * from expense")
     List<Expense> findAll();
 
     @SqlQuery("select * from expense where eventId = :eventId")
     List<Expense> findExpensesForEventId(@Bind("eventId") long eventId);
 
     @SqlUpdate("insert into expense (description, person, date, amount, eventId) " +
             "values (:description, :person, :date, :amount, :eventId)")
     void add(@BindBean Expense expense);
 }
