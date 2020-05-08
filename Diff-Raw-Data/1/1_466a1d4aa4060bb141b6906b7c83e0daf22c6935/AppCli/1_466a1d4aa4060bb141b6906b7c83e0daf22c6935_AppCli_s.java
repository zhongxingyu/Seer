 package it.nuccioservizi.as400querier;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 
 import com.google.common.base.CaseFormat;
 
 public class AppCli {
 
   private static String getRecordString(
       final ResultSet record,
       final int columnIndex) throws SQLException {
     String v = record.getString(columnIndex);
     if (v != null) {
       v = v.trim();
     }
     return v == null || v.isEmpty() ? null : v;
   }
 
   private static AppProperties loadProperties() throws IOException {
     try {
       return AppProperties.load();
     } catch (final IllegalArgumentException ex) {
       System.err.println("Mancano alcuni parametri di configurazione:");
       System.err.println(ex.getMessage());
       System.err.println("Creare o modificare il file '"
           + AppProperties.LOCAL_PROPERTIES_FILE_NAME + "'.");
       System.err
           .println("In alternativa si possono definire passando parametri nel formato -Dproprietà=valore al comando.");
       System.exit(1);
       throw new IllegalStateException(ex.getMessage());
     }
   }
 
   /**
    * @param args
    *          Query to submit to the as400 server.
    * @throws IOException
    *           For errors reading configuration file.
    * @throws SQLException
    *           For errors connecting to AS400.
    */
   public static void main(final String[] args) throws IOException, SQLException {
     final ArgsParser argsParser = new ArgsParser(args);
 
     final AppProperties properties = loadProperties();
     final String host = properties.get(Property.AS400_HOST);
     final String user = properties.get(Property.AS400_USERNAME);
     final String pwd = properties.get(Property.AS400_PASSWORD);
     final String dbUrl = "jdbc:as400://" + host;
 
     final JsonFactory jsonFactory = new JsonFactory();
 
     try (final JsonGenerator jg = jsonFactory.createJsonGenerator(System.out)) {
       jg.writeStartObject();
 
       try (final Connection conn = DriverManager
           .getConnection(dbUrl, user, pwd)) {
         try (final Statement statement = conn.createStatement(
             ResultSet.TYPE_FORWARD_ONLY,
             ResultSet.CONCUR_READ_ONLY)) {
           final As400Query query = argsParser.getQuery();
           final String sql = query.toSql(argsParser.getVars());
           if (query.isUpdate()) {
             final int updated = statement.executeUpdate(sql);
             jg.writeNumberField("updated", updated);
           } else {
             try (final ResultSet results = statement.executeQuery(sql)) {
 
               final ResultSetMetaData metadata = results.getMetaData();
               final int lastColumnIndex = metadata.getColumnCount();
 
               jg.writeArrayFieldStart("columnNames");
               for (int columnIndex = 1; columnIndex <= lastColumnIndex; ++columnIndex) {
                 jg.writeString(CaseFormat.UPPER_UNDERSCORE.to(
                     CaseFormat.LOWER_CAMEL,
                     metadata.getColumnLabel(columnIndex)));
               }
               jg.writeEndArray();
 
               jg.writeArrayFieldStart("rows");
               while (results.next()) {
                 jg.writeStartArray();
                 for (int columnIndex = 1; columnIndex <= lastColumnIndex; ++columnIndex) {
                   jg.writeString(getRecordString(results, columnIndex));
                 }
                 jg.writeEndArray();
               }
               jg.writeEndArray();
             }
           }
         }
       } catch (final Exception ex) {
         jg.writeStringField("error", ex.getMessage());
       }
 
       jg.writeEndObject();
     }
   }
 
 }
