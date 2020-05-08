 package de.loggi.service.impl;
 
 import de.loggi.exceptions.ProcessingException;
 import de.loggi.model.Parameter;
 import de.loggi.processors.ColumnProcessor;
 import de.loggi.service.ConfigurationService;
 import de.loggi.service.WriteService;
 import org.apache.commons.cli.CommandLine;
 import org.h2.jdbcx.JdbcConnectionPool;
 import org.h2.tools.Server;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author CptSpaetzle
  */
 public class H2WriteServiceImpl implements WriteService {
 
     Logger logger = LoggerFactory.getLogger(this.getClass());
 
     public static final String TABLE_NAME = "records";
     public static final String H2_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String H2_SERVER_URI = "jdbc:h2:mem:loggi";
     public static final String DEBUG_TRACE_OPTION=";TRACE_LEVEL_SYSTEM_OUT=3";
 
     public static final Pattern REGEX_PRECISION = Pattern.compile("\\((\\d+?)\\)");
 
     private ConfigurationService configuration;
     private JdbcConnectionPool cp;
     private Server server;
 
     private String username = "user";
     private String password = "password";
     private String port = "8082";
 
     @Override
     public void processRecord(String record) throws ProcessingException {
 
         String[] fields = new String[configuration.getProcessors().size()];
         String[] values = new String[configuration.getProcessors().size()];
         for (int i = 0; i < configuration.getProcessors().size(); i++) {
             ColumnProcessor processor = configuration.getProcessors().get(i);
             fields[i] = processor.getColumn().getName();
             String rawValue = processor.getColumnValue(record);
             String formattedValue = null;
             try {
                 formattedValue = getFormattedValue(rawValue, processor.getColumn().getDataType(), processor.getColumn().getDataFormat());
             } catch (Exception e) {
                 throw new ProcessingException("Exception formatting field value. Column:" + fields[i] + " raw value:[" + rawValue + "], format:[" + processor.getColumn().getDataFormat() + "]. Record:["+record+"]", e);
 
             }
 
             values[i] = formattedValue == null ? "'null'" : formattedValue;
         }
 
         try {
             Connection conn = cp.getConnection();
             StringBuilder sqlStatement = new StringBuilder();
             sqlStatement
                     .append("insert into ").append(TABLE_NAME).append("(")
                     .append(StringUtils.arrayToCommaDelimitedString(fields))
                     .append(") values (")
                     .append(StringUtils.arrayToCommaDelimitedString(values))
                     .append(")");
             logger.debug("H2> {}", sqlStatement.toString());
             conn.prepareStatement(sqlStatement.toString()).executeUpdate();
             conn.close();
         } catch (SQLException e) {
             logger.error("Exception processing record", e);
         }
     }
 
     private String getFormattedValue(String columnValue, String dataType, String dataFormat) throws Exception {
         // trim value down to varchar limit
         // TODO move precision fetch to init state so we should make it only once
         if (dataType.toLowerCase().startsWith("varchar")) {
             Matcher matcher = REGEX_PRECISION.matcher(dataType);
             if (matcher.find()) {
                 int precision = Integer.valueOf(matcher.group(1));
                 int maxSize = precision > columnValue.length() ? columnValue.length() : precision;
                 String trimmedValue = columnValue.substring(0, maxSize);
                 //using two single quotes to create a quote
                 return "'" + trimmedValue.replace("'", "''") + "'";
             }
         }
 
         // "support" for int values
         if (dataType.toLowerCase().startsWith("int")) {
             return columnValue;
         }
         // adjust date to H2 internal format
         if (dataType.toLowerCase().startsWith("datetime") && !dataFormat.isEmpty()) {
             SimpleDateFormat dt = new SimpleDateFormat(dataFormat);
             Date date = dt.parse(columnValue);
             SimpleDateFormat h2dt = new SimpleDateFormat(H2_DATETIME);
             return "'" + h2dt.format(date) + "'";
         }
         return "'" + columnValue + "'";
     }
 
     @Override
     public void initialize() throws Exception {
         CommandLine commandLine = configuration.getCommandLine();
         if (commandLine.hasOption(Parameter.H2_SERVER_PORT.getShortName())) {
             port = commandLine.getOptionValue(Parameter.H2_SERVER_PORT.getShortName());
         }
         if (commandLine.hasOption(Parameter.H2_USERNAME.getShortName())) {
             username = commandLine.getOptionValue(Parameter.H2_USERNAME.getShortName());
         }
         if (commandLine.hasOption(Parameter.H2_PASSWORD.getShortName())) {
             password = commandLine.getOptionValue(Parameter.H2_PASSWORD.getShortName());
         }
 
         Class.forName("org.h2.Driver").newInstance();
         String serverUri = H2_SERVER_URI;
         if(configuration.getCommandLine().hasOption(Parameter.DEBUG.getShortName())){
             serverUri = serverUri + DEBUG_TRACE_OPTION;
         }
         cp = JdbcConnectionPool.create(serverUri, username, password);
         // create & start embedded H2 Server
         server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", port).start();
         Connection conn = cp.getConnection();
 
         // create table
         StringBuilder sqlCreateTable = new StringBuilder();
         sqlCreateTable.append("create table ").append(TABLE_NAME).append("(");
         for (int i = 0; i < configuration.getProcessors().size(); i++) {
             ColumnProcessor processor = configuration.getProcessors().get(i);
             if (i > 0) {
                 sqlCreateTable.append(", ");
             }
             // it's not safe to assign datatype like this :(
             sqlCreateTable.append(processor.getColumn().getName())
                     .append(" ").append(processor.getColumn().getDataType());
         }
         sqlCreateTable.append(")");
         logger.debug("H2> {}", sqlCreateTable.toString());
         conn.prepareStatement(sqlCreateTable.toString()).executeUpdate();
         conn.close();
     }
 
     @Override
     public void finalizeAndShutdown() {
         cp.dispose();
         server.shutdown();
     }
 
     public void setConfiguration(ConfigurationService configuration) {
         this.configuration = configuration;
     }
 
     public String getPort() {
         return port;
     }
 }
