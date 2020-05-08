 package org.monitoring.queryapisql.preaggregation;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import org.monitoring.queryapi.Event;
 
 /**
  *
  * @author Michal
  */
 public class PostgreSQLDatabase {
 
     Connection conn = null;
     List<Event> events = new LinkedList<Event>();
     PostgreSQLDatabaseMapper mapper = new PostgreSQLDatabaseMapper();
 
     public PostgreSQLDatabase() {
         try {
             Class.forName("org.postgresql.Driver");
             conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/preaggregate", "postgres", "root");
             mapper = new PostgreSQLDatabaseMapper();
         } catch (SQLException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }
     }
 
     public List<Event> getAllEvents() {
         try {
             String query = "SELECT * "
                     + "FROM MeterEvent event "
                     + "ORDER BY event.time";
             PreparedStatement st = conn.prepareStatement(query);
             ResultSet result = st.executeQuery();
             return mapper.getResult(result);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public void save(Event event) {
         try {
             String query = "INSERT INTO event (source,date,value) VALUES (?,?,?)";
             PreparedStatement st = conn.prepareStatement(query);
             mapper.set(st, event);
             int result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public List<Event> getAggregate() {
         try {
             String query = "SELECT esource , sum(evalue) as sum "
                     + "FROM MeterEvent event"
                     + "GROUP BY event.edate";
             PreparedStatement st = conn.prepareStatement(query);
             ResultSet result = st.executeQuery();
             while (result.next()) {
                 //TODO
                 result.getDate("date");
                 result.getDouble("sum");
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public List<Event> updateAggregate(int timeActual, int timeNext, Date date, String field, Event event) {
         try {
             String query = "WITH upsert AS"
                    + "(UPDATE aggregate" + timeActual + " SET sum" + field + " = sum" + field + " + ? "
                     + "WHERE date = ? returning id) "
                    + "INSERT INTO aggregate" + timeActual + " (sum" + field + ", date, source) "
                    + "SELECT ?,?,? WHERE NOT EXISTS (SELECT 1 FROM upsert) ; ";
             PreparedStatement st = conn.prepareStatement(query);
             st.setDouble(1, event.getValue());
             st.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
             st.setDouble(3, event.getValue());
            st.setTimestamp(4, new java.sql.Timestamp(date.getTime()));
            st.setString(5, event.getSource());
             String q = st.toString();
             int result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public void execute(String query) {
         try {
             PreparedStatement st = conn.prepareStatement(query);
             int result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void dropTable() {
         try {
             String query = "DROP TABLE aggregate60;";
             PreparedStatement st = conn.prepareStatement(query);
             int result = st.executeUpdate();
             query = "DROP TABLE event";
             st = conn.prepareStatement(query);
             result = st.executeUpdate();
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void createTable(String table,  int timeActual, int timeNext, String[] fields) {
         StringBuilder builder = new StringBuilder();
         builder.append("CREATE TABLE ");
         builder.append(table);
         builder.append("(");
         builder.append("id SERIAL, ");
         builder.append("date TIMESTAMP, ");
         builder.append("source VARCHAR(100), ");
         for (Integer j = 0; j < timeNext / timeActual; j++) {
             for (String field : fields) {
                 builder.append(field).append(j).append(" DOUBLE precision DEFAULT 0,");
             }
         }
         builder.append(" CONSTRAINT pk_agg60_id PRIMARY KEY (id) ");
         builder.append(")");
         execute(builder.toString());
         
         execute("CREATE TABLE event(id serial NOT NULL, source character(100),date timestamp without time zone,"
                 + "value integer, CONSTRAINT pk_id PRIMARY KEY (id))");
         execute("CREATE INDEX date_agg ON aggregate60 (date);");
         execute("CREATE INDEX date_event ON event (date);");
     }
 }
