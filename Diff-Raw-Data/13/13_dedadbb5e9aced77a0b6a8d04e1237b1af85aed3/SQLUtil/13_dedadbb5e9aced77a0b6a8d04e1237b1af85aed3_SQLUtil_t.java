 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.util;
 
 import com.sun.grid.logging.SGELog;
 import com.sun.grid.util.sqlutil.Command;
 import com.sun.grid.util.sqlutil.UpdateDbModelCommand;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.StreamHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 /**
  * Database independed commandline sql util.
  *
  * It's used to test the database connection parameters during the
  * installation of dbwriter and the arco reporting web application.
  */
 public class SQLUtil {
    /** connection to the database. */
    private Connection connection;
    /** secondary connection to the database. */
    private Connection connection2;
    /** finished flag. */
    private boolean    done = false;
    /** logger, handles all log messages. */
    private Logger     logger;
    /** handler for the logger. */
    private Handler    handler;
    /** map with all registered commands (key is the
     *  command name, values is the command). */
    private Map        cmdMap = new HashMap();
 
    /** If this flag is set true, the sqlutil exists
     *  if a command fails */
    private boolean    exitOnError = true;
    /**
     *  Map with the variables.
     */
    private Map        variables = new HashMap();
 
    /** Creates a new instance of SQLUtil. */
    public SQLUtil() {
       logger = Logger.getLogger(getClass().getName());
 
       logger.setUseParentHandlers(false);
       handler = new MyHandler(new MyFormatter());
 
       logger.setLevel(Level.INFO);
       handler.setLevel(Level.INFO);
       logger.setUseParentHandlers(false);
       
       logger.addHandler(handler);
       SGELog.init(logger);
       initCmds();
    }
    
    /**
     * register a new command.
     * @param cmd  the command
     */
    private void reg(final Command cmd) {
       cmdMap.put(cmd.getName().toLowerCase(), cmd);
    }
 
    /**
     * get the connection to the database.
     * @return  the connection
     */
    public final Connection getConnection() {
       return this.connection;
    }
 
    /**
     * get the secondary connection to the database.
     * can be used for creating/dropping synonyms as a different user from 
     * the user who is creating tables/views.
     * @return  the connection
     */
    public final Connection getConnection2() {
       return this.connection2;
    }
    
    /**
     *  get the value of a variable.
     *  @param variable name of the variable
     *  @return the value of the variable or <code>null</code>
     *          if the variable was not found
     */
    public final String getValue(final String variable) {
       return (String) variables.get(variable.toUpperCase());
    }
 
    /**
     *  set the value of a variable.
     *  @param  variable  name of the variable
     *  @param  value     value of the variable
     */
    public final void setValue(final String variable, final String value) {
       SGELog.fine("set variable ''{0}'' = ''{1}''", variable, value);
       variables.put(variable.toUpperCase(), value);
    }
 
    /**
     *   get the names of all variables.
     *   @return  array with all variable names
     */
    public final String[] getVariables() {
       String [] ret = new String[variables.size()];
       variables.keySet().toArray(ret);
       return ret;
    }
    /**
     * get the appropriate connection to the database.
     * SQLUtil can hold two connections, for manipulating
     * the synonyms we use secondary connection, otherwise
     * the primary one.
     * @return  the connection
     */
    public final Connection getDBConnection() {
       Connection conn;
       String syn = getValue("SYNONYMS");
       if (syn != null && syn.equals("1")) {
          conn = connection2;
       } else {
          conn = connection;
       }
       return conn;
    }
    
    /**
     * set the appropriate connection to the database.
     * @return  the connection
     */
    public final void setDBConnection(Connection conn) {
       String syn = getValue("SYNONYMS");
       if (syn != null && syn.equals("1")) {
          connection2 = conn;
       } else {
          connection = conn;
       }
    }
    
    /**
     * get a command.
     * @param name  name of the command
     * @return the command or <code>null</code> if the command
     *         was not found
     */
    public Command getCommand(final String name) {
       return (Command) cmdMap.get(name.toLowerCase());
    }
 
    /**
     *  initializes and registers all commands.
     */
    private void initCmds() {
       reg(new ConnectCommand(this));
       reg(new SQLCommand(this, "select"));
       reg(new SQLCommand(this, "insert"));
       reg(new SQLCommand(this, "update"));
       reg(new SQLCommand(this, "delete"));
       reg(new SQLCommand(this, "create"));
       reg(new SQLCommand(this, "alter"));
       reg(new SQLCommand(this, "drop"));
       reg(new SQLCommand(this, "grant"));
       reg(new SQLCommand(this, "revoke"));
       reg(new SQLCommand(this, "commit"));
       reg(new SQLCommand(this, "flush"));
       reg(new HelpCommand(this));
       reg(new ExitCommand(this));
       reg(new UpdateDbModelCommand(this, "install"));
       reg(new SQLUtil.DebugLevelCommand(this, "debug"));
       reg(new SQLUtil.SetCommand(this, "set"));
       reg(new SQLUtil.EnvCommand(this, "env"));
       reg(new ExitOnErrorCommand(this));
    }
 
    /**
     *  run the SQLUtil.
     *  Reading commands from stdin
     *  Executing the commands
     */
    public final void run() {
       try {
          Reader reader = new InputStreamReader(System.in);
          BufferedReader in = new BufferedReader(reader);
 
          StringBuffer buf = new StringBuffer();
 
          String line = null;
 
          while (!done) {
             buf.setLength(0);
             if (logger.isLoggable(Level.INFO)) {
                System.err.print("prompt: ");
             }
             while (!done && (line = in.readLine()) != null) {
                if (line.endsWith("\\")) {
                    buf.append(line.substring(0, line.length() - 1));
                } else {
                   buf.append(line);
                   break;
                }
             }
             if (buf.length() == 0) {
                continue;
             }
             line = buf.toString();
 
             int index = line.indexOf(' ');
             String cmdName = null;
             String args = null;
             if (index > 0) {
               cmdName = line.substring(0, index);
               args = line.substring(index + 1);
             } else {
                cmdName = line;
                args = null;
             }
 
             Command cmd = getCommand(cmdName);
 
             if (cmd == null) {
                SGELog.warning("Command ''{0}'' unknown", cmdName);
                cmd = getCommand("help");
                cmd.run(null);
                if( exitOnError ) {
                   System.exit(1);
                }
             } else {
                int ret = cmd.run(args);
                if (!exitOnError) {
                   SGELog.info("__exit(" + ret + ")");
                }
                if (ret != 0) {
                   // Command not successfully executed
                   if( exitOnError ) {
                      System.exit(ret);
                   } 
                }
             }
          }
       } catch (Exception e) {
           SGELog.severe(e, "Unknown error: {0}", e.getMessage());
           e.printStackTrace();
           System.exit(-1);
       }
 
    }
 
    /**
     * the main method.
     * @param args the SQLUtil has no command line args
     */
    public static void main(final String[] args) {
 
       SQLUtil util = new SQLUtil();
 
       util.run();
    }
 
    /**
     * replace all variables in a string.
     * Variables has the form <code>{variable name}</code>
     * @param   str  the string where the variables should be replaced
     * @return  the string with the replaced variables
     */
    public final String replaceVariables(final String str) {
 
       StringBuffer buf = new StringBuffer(str);
 
       int open  = -1;
       int close = -1;
       String var = null;
       String value = null;
       int index = 0;
       while (index < buf.length()) {
          open = buf.indexOf("{", index);
          if (open >= 0) {
             close = buf.indexOf("}", open + 1);
             if (close > open) {
               var = buf.substring(open + 1, close);
               value = getValue(var);
               if (value == null) {
                  value = "";
               }
               buf.replace(open, close + 1, value);
               index = open + value.length();
             } else {
                break;
             }
          } else {
             break;
          }
       }
       return buf.toString();
    }
 
 
    /**
     *   This Command opens a JDBC connection to the database.
     */
    class ConnectCommand extends Command {
       /**
        *  Create a new ConnectionCommand.
        * @param aSqlUtil the sql util
        */
       public ConnectCommand(final SQLUtil aSqlUtil) {
          super(aSqlUtil, "connect");
       }
 
       /**
        *  opens a JDBC connection to the database.
        *  @param  args  String in the form
                         &lt;driver&gt; &lt;jdbc-url&gt; &lt;user&gt;
                         [&lt;password&gt;]
        *  @return 0   Connection established
        *          1   invalid arguments
        *          2   jdbc driver class not found
        *          3   connect error
        */
       public final int run(final String args) {
          if (args == null) {
             SGELog.severe(usage());
             return 1;
         }
          StringTokenizer st = new StringTokenizer(replaceVariables(args), " ");
          String driver = null;
          String url = null;
          String user = null;
          String pw = null;

          switch(st.countTokens()) {
             case 3:
                driver = st.nextToken();
                url = st.nextToken();
                user = st.nextToken();
                break;
             case 4:
                driver = st.nextToken();
                url = st.nextToken();
                user = st.nextToken();
                pw = st.nextToken();
                break;
             default:
              SGELog.severe("invalid arguments for command " + getName());
              SGELog.severe(usage());
              return 1;
          }
 
          SGELog.fine("driver is ''{0}''", driver);
 
          try {
             Class.forName(driver);
          } catch (ClassNotFoundException cnfe) {
            SGELog.severe("driver class " + driver + " not found");
             return 2;
          }

          Connection conn = getDBConnection();
          if(conn != null) {
             try {
                conn.close();
             } catch (SQLException sqle) {
                SGELog.warning("Failure while closing existing connection: " + sqle.getMessage());
             } finally {
                conn = null;
             }
          }
          try {
             SGELog.info("connect to {0} as user {1}", url, user);
             conn = DriverManager.getConnection(url, user, pw);
             setDBConnection(conn);
             SGELog.info("connected");
             return 0;
          } catch (SQLException sqle) {
             SGELog.severe("Can not get a connection to {0}", url);
             SGELog.severe(sqle.getMessage());
             return 3;
          }
       }
       
       /**
        *  give a usage message for this command.
        *  @return the usage message
        */
       public final String usage() {
           return getName() + " <driver> <url> <user> [<password>]";
       }
    }
 
    /**
     *   This command exits the SQLUtil.
     */
    class ExitCommand extends Command {
 
       /**
        *  create a new ExitCommand.
        * @param aSqlUtil  the sql util
        */
       public ExitCommand(final SQLUtil aSqlUtil) {
          super(aSqlUtil, "exit");
       }
 
       /**
        *   Run the exit command.
        * @param args  arguments for this command
        * @return  0
        */
       public final int run(final String args) {
          System.exit(0);
          return 0;
       }
       /**
        * get the usage message of this command.
        * @return the usage message
        */
       public final String usage() {
          return getName();
       }
 
    }
 
    class ExitOnErrorCommand extends Command {
       
       public ExitOnErrorCommand(final SQLUtil aSqlUtil) {
          super(aSqlUtil, "exitOnError" );
       }
 
       public int run(String args) {
          if ( args == null || args.length() == 0 ) {
             SGELog.info("exitOnError switched " + (exitOnError? "on" : "off") );
             return 0;
          } else if(args.startsWith("on")) {
             exitOnError = true;
             SGELog.info("exitOnError switched on" );
             return 0;
          } else if(args.startsWith("off")) {
             exitOnError = false;
             SGELog.info( "exitOnError switched off" );
             return 0;
          } else {
             SGELog.severe("invalid args");
             return -1;
          }
       }
 
       public String usage() {
          return "exitOnError [on|off]";
       }
       
    }
    /**
     *  This class prints the available commands
     *  or gives the usage for a command.
     */
    class HelpCommand extends Command {
       /**
        * Create a new help command.
        * @param aSqlUtil  the sql util
        */
       public HelpCommand(final SQLUtil aSqlUtil) {
          super(aSqlUtil, "help");
       }
 
       /**
        * run the help command.
        * @param args  name of a command or <code>null</code>
        * @return  0
        */
       public final int run(final String args) {
 
          if (args != null && args.length() > 0) {
             Command cmd = getCommand(args);
             if (cmd == null) {
                SGELog.warning("Command ''{0}'' unknown", args);
             } else {
                SGELog.info("usage: {0}", cmd.usage());
             }
          } else {
             StringBuffer cmds = new StringBuffer();
 
             Iterator iter = cmdMap.keySet().iterator();
             while (iter.hasNext()) {
                cmds.append(iter.next());
                cmds.append(' ');
             }
             SGELog.info("available commands: {0}", cmds);
          }
          return 0;
       }
       /**
        * get the usage message of this command.
        * @return the usage message
        */
       public final String usage() {
          return "help [command]";
       }
    }
 
    /**
     * This command executes a SQL command.
     * If the SQL command has a ResultSet the content
     * of this ResultSet is writen as INFO message
     */
    class SQLCommand extends Command {
 
       /**
        *  Create  a SQLCommand.
        *  @param  aSqlUtil  the sql util
        *  @param  name  Name of the SQLCommand
                         (e.g. "select", "update", "create", ...)
        */
       public SQLCommand(final SQLUtil aSqlUtil, final String name) {
          super(aSqlUtil, name);
       }
 
       /**
        *  run the SQLCommand.
        *  @param   args  argument line for the SQLCommand
        *  @return  0     if the sql command has been successfully
        *                 executed
        *           else  error
        */
       public final int run(final String args) {
          Connection conn = getDBConnection();
          if (conn == null) {
             SGELog.severe("Can not execute this command, not connected");
             return 1;
          }
 
          try {
              Statement stmt = conn.createStatement();
              try {
                 String sql = getName();
                 if (args != null) {
                    sql += " " + replaceVariables(args);
                 }
                  if (stmt.execute(sql)) {
                     // stmt produces a result set
                    ResultSet rs = stmt.getResultSet();
 
                    try {
                       
                       ResultSetMetaData metadata = rs.getMetaData();
                       int colCount = metadata.getColumnCount();
                       StringBuffer buf = new StringBuffer();
                       
                       String print_header = this.getSQLUtil().getValue("print_header");
                       String column_separator = getSQLUtil().getValue("column_separator");
                       String line_prefix = getSQLUtil().getValue("line_prefix");
                       if(line_prefix == null ) {
                          line_prefix = "";
                       }
                       String line_suffix = getSQLUtil().getValue("line_suffix");
                       if(line_suffix == null ) {
                          line_suffix = "";
                       }
                       
                       if(column_separator == null) {
                          column_separator = "\t";
                       }
                       if( print_header != null && print_header.equals("true") ) {
                          // print the headers
                          buf.append(line_prefix);
                          for(int i = 1; i <= colCount; i++ ) {
                             if( i > 1 ) {
                                buf.append(column_separator);
                             }
                             buf.append(metadata.getColumnName(i));
                          }
                          buf.append(line_suffix);
                          SGELog.info(buf.toString());
                       }
                       while (rs.next()) {
                          buf.setLength(0);
                          buf.append(line_prefix);
                          for (int i = 1; i <= colCount; i++) {
                              if (i > 1) {
                                 buf.append(column_separator);
                              }
                              buf.append(rs.getString(i));
                          }
                          buf.append(line_suffix);
                          SGELog.info(buf.toString());
                       }
                    } finally {
                       rs.close();
                    }
                  } else if (stmt.getUpdateCount() > 0) {
                     SGELog.info("{0} rows updated",
                                 new Integer(stmt.getUpdateCount()));
                  }
              } finally {
                 stmt.close();
              }
              return 0;
          } catch (SQLException sqle) {
             SGELog.severe("sql: {0}", sqle.getMessage());
             return 1;
          }
       }
       /**
        * get the usage message of this command.
        * @return the usage message
        */
       public final String usage() {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          
          pw.print(getName());
          pw.println(" (sql command, depends on the database)");
          pw.println();
          pw.println("Variables: ");
          pw.println("   print_header     if set to true the column names of a query result will be printed");
          pw.println("   column_separator separator between two column (default is \\t)");
          pw.flush();
          
          return sw.getBuffer().toString();
       }
 
    }
 
    /**
     *  Formatter for the logger.
     */
    class MyFormatter extends Formatter {
       /** buffers the formated messages. */
       private StringBuffer buf = new StringBuffer();
 
       /**
        *  @see java.util.logging.Formatter#format
        */
       public final String format(final LogRecord record) {
          if (!Level.INFO.equals(record.getLevel())) {
             buf.append(record.getLevel().getName());
             buf.append(": ");
          }
          String msg = null;
          if (record.getParameters() != null) {
             msg = MessageFormat.format(record.getMessage(),
                                        record.getParameters());
          } else {
             msg = record.getMessage();
          }
 
          buf.append(msg);
          buf.append("\n");
          String ret = buf.toString();
          buf.setLength(0);
          return ret;
       }
    }
 
    /**
     *  This handler writer the formated messages
     *  to stdout and flushes the stream.
     */
    class MyHandler extends StreamHandler {
 
       /**
        * Create a new instance of MyHandler.
        * @param formatter the formatter
        */
       public MyHandler(final Formatter formatter) {
          super(System.out, formatter);
 
       }
 
       /**
        * publish a log record.
        * @param record the log record
        */
       public final void publish(final LogRecord record) {
          super.publish(record);
          flush();
       }
    }
 
 
    /** valid debug levels for the debug level command. */
    public static final Level [] DEBUG_LEVELS = {
        Level.SEVERE,
        Level.WARNING,
        Level.INFO,
        Level.CONFIG,
        Level.FINE,
        Level.ALL
    };
 
    /**
     *  This command prints all enviromnent variables.
     */
    class EnvCommand extends Command {
 
       /**
        *  Create a new instance of the EnvCommand.
        * @param aSQLUtil  the sql util which instatiates this command
        * @param aName     name of the command
        */
       public EnvCommand(final SQLUtil aSQLUtil, final String aName) {
          super(aSQLUtil, aName);
       }
 
       /**
        * Run this commands.
        * Writes all variables into SGELog
        * @param args  the arguments for this command
        *              (Currently not used)
        * @return This function always returns 0
        */
       public final int run(final String args) {
          String [] vars = SQLUtil.this.getVariables();
          for (int i = 0; i < vars.length; i++) {
             SGELog.info("{0}={1}", vars[i], getValue(vars[i]));
          }
          return 0;
       }
 
       /**
        * get the usage message of this command.
        * @return the usage message (equals to the name)
        */
       public String usage() {
          return getName();
       }
 
    }
 
    /**
     *  This command set the value of a environment variable.
     */
    class SetCommand extends Command {
 
       /**
        *  Create a new instance of the SetCommand.
        * @param aSQLUtil  the sql util which instatiates this command
        * @param aName     name of the command
        */
       public SetCommand(final SQLUtil aSQLUtil, final String aName) {
          super(aSQLUtil, aName);
       }
 
       /**
        * Run this command.
        * @param args  This must must have the form
        *              <code>&lt;variable&gt; &lt;value&gt;</code>
        * @return This method always returns 0
        */
       public final int run(final String args) {
          int index = args.indexOf(' ');
          if (index <= 0) {
             SGELog.severe(usage());
             return 1;
          }
          String variable = args.substring(0, index).trim();
          String value = args.substring(index + 1).trim();
 
          SQLUtil.this.setValue(variable, value);
          return 0;
       }
 
       /**
        * Get the usage message of this command.
        * @return the usage message
        */
       public final String usage() {
          return getName() + " <variable> <value>";
       }
 
    }
    /**
     *   This command print the current debug level and sets new debug level.
     */
    class DebugLevelCommand extends Command {
 
       /**
        * Create a new DebugLevel Command.
        * @param sSQLUtil  the sqlutil which instantiates this command
        * @param name      name if the command in the sqlutil
        */
       public DebugLevelCommand(final SQLUtil sSQLUtil, final String name) {
          super(sSQLUtil, name);
       }
 
       /**
        * execute this command. If <code>args</code> is <code>null</code>
        * the current debug level is printed,
        * else args must be a valid debug level. This debug level set in
        * the logger.
        * @see   java.util.logging.Level#parse
        * @param args  the debug level
        * @return 0  command successfully executed
        *         1  args contains no vaild debug level
        */
       public int run(final String args) {
 
          if (args != null) {
             try {
                Level level = null;
 //               if( args.equalsIgnoreCase("ALL")) {
 //                  level = Level.ALL;
 //               } else if ( args.equalsIgnoreCase("OFF")) {
 //                  level = Level.OFF;
 //               } else {
                   level = Level.parse(args.toUpperCase());
 //               }
                logger.setLevel(level);
                handler.setLevel(level);
                SGELog.fine("set debug level to {0}", level);
                return 0;
             } catch (IllegalArgumentException iae) {
                SGELog.severe("{0} is not a valid debug level", args);
                SGELog.info(usage());
                return 1;
             }
          } else {
             SGELog.info("current debug level is {0}", logger.getLevel());
             return 0;
          }
       }
 
       /**
        * get the usage message for this command.
        * @return the usage message
        */
       public final String usage() {
          StringBuffer ret = new StringBuffer();
          ret.append(getName());
          ret.append(" [(");
          for (int i = 0; i < DEBUG_LEVELS.length; i++) {
             if (i > 0) {
                ret.append("|");
             }
             ret.append(DEBUG_LEVELS[i]);
          }
          ret.append(")]");
          return ret.toString();
       }
 
    }
 }
