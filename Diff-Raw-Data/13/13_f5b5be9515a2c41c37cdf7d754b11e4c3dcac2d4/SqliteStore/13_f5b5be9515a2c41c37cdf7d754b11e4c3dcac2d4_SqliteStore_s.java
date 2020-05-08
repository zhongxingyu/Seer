 package org.reefy.storesqlite;
 
 import com.google.common.base.Preconditions;
 import com.google.common.util.concurrent.AbstractIdleService;
 import org.reefy.transportrest.api.Key;
 import org.reefy.transportrest.api.RawKey;
 import org.reefy.transportrest.api.RawValue;
 import org.reefy.transportrest.api.Value;
 import org.reefy.transportrest.api.store.Store;
 import org.reefy.transportrest.api.store.StoreException;
 
 import java.io.File;
 import java.nio.charset.StandardCharsets;
 import java.sql.*;
 
 public class SqliteStore extends AbstractIdleService implements Store {
 
     private final String id;
     private volatile Connection connection = null;
 
     public SqliteStore(String id) {
         this.id = id;
     }
 
     @Override
     protected void startUp() throws Exception {
         // create a database connection
         Preconditions.checkState(connection == null);
         Class.forName("org.sqlite.JDBC");
 
         new File("db").mkdirs();
 
         connection = DriverManager.getConnection("jdbc:sqlite:db/" + id + ".db");
     }
 
     @Override
     protected void shutDown() throws Exception{
         connection.close();
     }
 
     // TODO: Synchronizing everything might be bad for perf
     @Override
     public synchronized void clear() throws StoreException {
         Preconditions.checkState(isRunning(), "Sqlite store service is not running.");
 
         try
         {
             final Statement statement = connection.createStatement();
 
             statement.setQueryTimeout(30);  // set timeout to 30 sec.
 
             statement.executeUpdate("drop table if exists keys");
             statement.executeUpdate("create table keys (key char(20), value blob)");
             statement.close();
         } catch (SQLException e) {
             throw new StoreException(e);
         }
     }
 
     @Override
     public synchronized <V> void put(Key key, Value<V> value, PutCallback<V> callback) {
         Preconditions.checkState(isRunning(), "Sqlite store service is not running.");
 
         try {
             final PreparedStatement putStatement = connection.prepareStatement("insert into keys values(?, ?)");
             putStatement.setBytes(1, key.getBytes());
             putStatement.setBytes(2, value.getBytes());
             putStatement.execute();
             putStatement.close();
         } catch (SQLException e) {
             callback.fail(new StoreException(e));
         }
         callback.succeed();
     }
 
     @Override
     public synchronized <V> void get(Key key, GetCallback<V> callback) {
         Preconditions.checkState(isRunning(), "Sqlite store service is not running.");
 
         try {
             final PreparedStatement statement = connection.prepareStatement("select key, value from keys where key = ?");
             statement.setBytes(1, key.getBytes());
             final ResultSet rs = statement.executeQuery();
 
             if (!rs.next()) {
                 statement.close();
                 callback.notFound();
                 return;
             }
 
             // read the result set
             final RawKey retrieved = new RawKey(rs.getBytes("key"));
             System.out.println("key = " + retrieved);
             final byte[] valueBytes = rs.getBytes("value");
             System.out.println("value = " + new String(valueBytes, StandardCharsets.ISO_8859_1));
 
             statement.close();
 
             callback.succeed(new RawValue(valueBytes));
         } catch (SQLException e) {
             //TODO: statement.close();
             callback.fail(new StoreException(e));
         }
     }
 }
