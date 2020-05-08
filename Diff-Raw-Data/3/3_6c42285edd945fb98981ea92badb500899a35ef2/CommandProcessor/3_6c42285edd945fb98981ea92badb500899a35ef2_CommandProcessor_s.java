 package net.argius.stew;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import javax.script.*;
 import net.argius.stew.io.*;
 import net.argius.stew.ui.*;
 
 /**
  * Command Processor.
  */
 final class CommandProcessor {
 
     private static Logger log = Logger.getLogger(CommandProcessor.class);
     private static ResourceManager res = ResourceManager.getInstance(Command.class);
     private static final String HYPHEN_E = "-e";
 
     private final Environment env;
     private final OutputProcessor op;
 
     CommandProcessor(Environment env) {
         this.env = env;
         this.op = env.getOutputProcessor();
     }
 
     /**
      * Invokes this command.
      * @param parameterString
      * @return whether this application continues or not
      * @throws CommandException
      */
     boolean invoke(String parameterString) throws CommandException {
         Parameter p = new Parameter(parameterString);
         if (parameterString.replaceFirst("^\\s+", "").startsWith(HYPHEN_E)) {
             final int offset = parameterString.indexOf(HYPHEN_E) + 2;
             for (String s : parameterString.substring(offset).split(HYPHEN_E)) {
                 op.output(" >> " + s);
                 if (!invoke(s)) {
                     outputMessage("w.exit-not-available-in-sequencial-command");
                 }
             }
             return true;
         }
         final String commandName = p.at(0);
         try {
             return invoke(commandName, new Parameter(parameterString));
         } catch (UsageException ex) {
             outputMessage("e.usage", commandName, ex.getMessage());
         } catch (DynamicLoadingException ex) {
             log.error(ex);
             outputMessage("e.not-found", commandName);
         } catch (CommandException ex) {
             log.error(ex);
             Throwable cause = ex.getCause();
             String message = (cause == null) ? ex.getMessage() : cause.getMessage();
             outputMessage("e.command", message);
         } catch (IOException ex) {
             log.error(ex);
             outputMessage("e.command", ex.getMessage());
         } catch (SQLException ex) {
             log.error(ex);
             SQLException parent = ex;
             while (true) {
                 SQLException sqle = parent.getNextException();
                 if (sqle == null || sqle == parent) {
                     break;
                 }
                 log.error(sqle, "------ SQLException.getNextException ------");
                 parent = sqle;
             }
             outputMessage("e.database", ex.getMessage());
         } catch (UnsupportedOperationException ex) {
             log.warn(ex);
             outputMessage("e.unsupported", ex.getMessage());
         } catch (RuntimeException ex) {
             log.error(ex);
             outputMessage("e.runtime", ex.getMessage());
         } catch (Throwable th) {
             log.fatal(th);
             outputMessage("e.fatal", th.getMessage());
         }
         try {
             Connection conn = env.getCurrentConnection();
             if (conn != null) {
                 boolean isClosed = conn.isClosed();
                 if (isClosed) {
                     log.info("connection is already closed");
                     disconnect();
                 }
             }
         } catch (SQLException ex) {
             log.warn(ex);
         }
         return true;
     }
 
     private boolean invoke(String commandName, Parameter p) throws IOException, SQLException {
         assert commandName != null;
         // do nothing if blank
         if (commandName.length() == 0) {
             return true;
         }
         // exit
         if (commandName.equalsIgnoreCase("exit")) {
             disconnect();
             outputMessage("i.exit");
             return false;
         }
         // connect
         if (commandName.equalsIgnoreCase("connect") || commandName.equalsIgnoreCase("-c")) {
             connect(p);
             return true;
         }
         // from file
         if (commandName.equals("-f")) {
             final File file = Path.resolve(env.getCurrentDirectory(), p.at(1));
             final String abspath = file.getAbsolutePath();
             if (log.isDebugEnabled()) {
                 log.debug("absolute path = [%s]", abspath);
             }
             if (!file.isFile()) {
                 outputMessage("e.file-not-exists", abspath);
                 throw new UsageException(res.get("usage.-f"));
             }
             log.debug("-f %s", file.getAbsolutePath());
             invoke(String.format("%s%s", Command.readFileAsString(file), p.after(2)));
             return true;
         }
         // script
         if (commandName.equals("-s")) {
             if (!p.has(1)) {
                 throw new UsageException(res.get("usage.-s"));
             }
             final String p1 = p.at(1);
             if (p1.equals(".")) {
                 env.initializeScriptContext();
                 outputMessage("i.script-context-initialized");
                 return true;
             }
             final File file;
             if (p1.contains(".")) { // by extension
                 file = Path.resolve(env.getCurrentDirectory(), p1);
                 if (!file.exists() || !file.isFile()) {
                     outputMessage("e.file-not-exists", p1);
                     return true;
                 }
                 log.debug("script file: %s", file.getAbsolutePath());
             } else { // by name
                 file = null;
                 log.debug("script name: %s", p1);
             }
             ScriptEngine engine = (file == null)
                 ? new ScriptEngineManager().getEngineByName(p1)
                 : new ScriptEngineManager().getEngineByExtension(Path.getExtension(file));
             if (engine == null) {
                 outputMessage("e.unsupported", p1);
                 return true;
             }
             engine.setContext(env.getScriptContext());
             engine.put("connection", env.getCurrentConnection());
             engine.put("conn", env.getCurrentConnection());
             engine.put("patameter", p);
             engine.put("p", p);
             engine.put("outputProcessor", op);
             engine.put("op", op);
             try {
                 if (file == null) {
                     engine.put(ScriptEngine.FILENAME, null);
                     engine.eval(p.after(2));
                 } else {
                     engine.put(ScriptEngine.FILENAME, file.getAbsolutePath());
                     Reader r = new FileReader(file);
                     try {
                         engine.eval(r);
                     } finally {
                         r.close();
                     }
                 }
             } catch (Exception ex) {
                 throw new CommandException(ex);
             }
             return true;
         }
         // alias
         AliasMap aliasMap = env.getAliasMap();
         if (commandName.equalsIgnoreCase("alias") || commandName.equalsIgnoreCase("unalias")) {
             aliasMap.reload();
             if (commandName.equalsIgnoreCase("alias")) {
                 if (p.has(2)) {
                     final String keyword = p.at(1);
                     if (isUsableKeywordForAlias(keyword)) {
                         outputMessage("w.unusable-keyword-for-alias", keyword);
                         return true;
                     }
                     aliasMap.setValue(keyword, p.after(2));
                     aliasMap.save();
                 } else if (p.has(1)) {
                     final String keyword = p.at(1);
                     if (isUsableKeywordForAlias(keyword)) {
                         outputMessage("w.unusable-keyword-for-alias", keyword);
                         return true;
                     }
                     if (aliasMap.containsKey(keyword)) {
                         outputMessage("i.dump-alias", keyword, aliasMap.getValue(keyword));
                     }
                 } else {
                     if (aliasMap.isEmpty()) {
                         outputMessage("i.noalias");
                     } else {
                         for (final String key : new TreeSet<String>(aliasMap.keys())) {
                             outputMessage("i.dump-alias", key, aliasMap.getValue(key));
                         }
                     }
                 }
             } else if (commandName.equalsIgnoreCase("unalias")) {
                 if (p.has(1)) {
                     aliasMap.remove(p.at(1));
                     aliasMap.save();
                 } else {
                     throw new UsageException(res.get("usage.unalias"));
                 }
             }
             return true;
         } else if (aliasMap.containsKey(commandName)) {
             final String command = aliasMap.expand(commandName, p);
             op.output(" >> " + command);
             invoke(command);
             return true;
         }
         // cd
         if (commandName.equalsIgnoreCase("cd")) {
             if (!p.has(1)) {
                 throw new UsageException(res.get("usage.cd"));
             }
             File olddir = env.getCurrentDirectory();
             final String path = p.at(1);
             final File dir = new File(path);
             final File newdir = ((dir.isAbsolute()) ? dir : new File(olddir, path)).getCanonicalFile();
             if (!newdir.isDirectory()) {
                 outputMessage("e.dir-not-exists", newdir);
                 return true;
             }
             env.setCurrentDirectory(newdir);
             outputMessage("i.directory-changed", olddir.getAbsolutePath(), newdir.getAbsolutePath());
             return true;
         }
         // at
         if (commandName.equals("@")) {
             final String currentDirectory = env.getCurrentDirectory().getAbsolutePath();
             final String systemDirectory = Bootstrap.getSystemDirectory().getAbsolutePath();
             op.output(String.format("current dir : %s", currentDirectory));
             op.output(String.format("system  dir : %s", systemDirectory));
             return true;
         }
         // report -
         if (commandName.equals("-")) {
             return invoke("report -");
         }
         // runtime informations
         if (commandName.equals("?")) {
             if (p.has(1)) {
                 for (final String k : p.asArray()) {
                     if (k.equals("?")) {
                         continue;
                     }
                     final String s = System.getProperties().containsKey(k)
                             ? String.format("[%s]", System.getProperty(k))
                             : "undefined";
                     op.output(String.format("%s=%s", k, s));
                 }
             } else {
                 op.output(String.format("JRE : %s %s",
                                         System.getProperty("java.runtime.name"),
                                         System.getProperty("java.runtime.version")));
                 op.output(String.format("OS : %s (osver=%s)",
                                         System.getProperty("os.name"),
                                         System.getProperty("os.version")));
                 op.output(String.format("Locale : %s", Locale.getDefault()));
             }
             return true;
         }
         // connection
         Connection conn = env.getCurrentConnection();
         if (conn == null) {
             outputMessage("e.not-connect");
         } else if (commandName.equalsIgnoreCase("disconnect") || commandName.equalsIgnoreCase("-d")) {
             disconnect();
             outputMessage("i.disconnected");
         } else if (commandName.equalsIgnoreCase("commit")) {
             conn.commit();
             outputMessage("i.committed");
         } else if (commandName.equalsIgnoreCase("rollback")) {
             conn.rollback();
             outputMessage("i.rollbacked");
         } else {
             executeDynamicCommand(commandName, conn, p);
         }
         return true;
     }
 
     private static boolean isUsableKeywordForAlias(String keyword) {
         return keyword != null && keyword.matches("(?i)-.*|exit|alias|unalias");
     }
 
     private void connect(Parameter p) throws SQLException {
         log.info("connect start");
         disconnect();
         final String id = p.at(1);
         Connector connector;
         if (!p.has(1)) {
             connector = AnonymousConnector.getConnector(id, p.at(2), p.at(3));
         } else if (id.indexOf('@') >= 0) {
             connector = AnonymousConnector.getConnector(id);
         } else {
             connector = env.getConnectorMap().getConnector(id);
         }
         if (connector != null) {
             env.establishConnection(connector);
         } else {
             outputMessage("e.no-connector", id);
         }
         log.info("connect end");
     }
 
     private void disconnect() {
         log.debug("disconnect start");
         try {
             env.releaseConnection();
         } catch (SQLException ex) {
             outputMessage("w.connection-closed-abnormally");
         }
         log.debug("disconnect end");
     }
 
     private void executeDynamicCommand(String commandName, Connection conn, Parameter p) {
         assert commandName != null && !commandName.contains(" ");
         final String fqcn;
         if (commandName.indexOf('.') > 0) {
             fqcn = commandName;
         } else {
             fqcn = "net.argius.stew.command."
                    + commandName.substring(0, 1).toUpperCase()
                    + commandName.substring(1).toLowerCase();
         }
         Class<? extends Command> c;
         try {
             c = DynamicLoader.loadClass(fqcn);
         } catch (DynamicLoadingException ex) {
             c = Command.isSelect(p.asString()) ? Select.class : UpdateAndOthers.class;
         }
         Command command = DynamicLoader.newInstance(c);
         try {
             Connector connector = env.getCurrentConnector();
             if (connector.isReadOnly() && !command.isReadOnly()) {
                 outputMessage("e.readonly");
                 return;
             }
             command.setEnvironment(env);
             log.info("command: %s start", command);
             log.debug(p);
             command.initialize();
             command.execute(conn, p);
         } finally {
             command.close();
         }
         log.info("command: %s end", command);
     }
 
     /**
      * Outputs message.
      * @param id message-id (resource)
      * @param args
      * @throws CommandException
      */
     void outputMessage(String id, Object... args) throws CommandException {
         op.output(res.get(id, args));
     }
 
     /**
      * SQL statement command.
      */
     abstract static class RawSQL extends Command {
 
         @Override
         public final void execute(Connection conn, Parameter parameter) throws CommandException {
             final String rawString = parameter.asString();
             try {
                 Statement stmt = prepareStatement(conn, rawString);
                 try {
                     execute(stmt, rawString);
                 } finally {
                     stmt.close();
                 }
             } catch (SQLException ex) {
                 throw new CommandException(ex);
             }
         }
 
         protected abstract void execute(Statement stmt, String sql) throws SQLException;
 
     }
 
     /**
      * Select statement command.
      */
     static final class Select extends RawSQL {
 
         public Select() {
             // empty
         }
 
         @Override
         public boolean isReadOnly() {
             return true;
         }
 
         @Override
         public void execute(Statement stmt, String rawString) throws SQLException {
             final long startTime = System.currentTimeMillis();
             ResultSet rs = executeQuery(stmt, rawString);
             try {
                 outputMessage("i.response-time", (System.currentTimeMillis() - startTime) / 1000f);
                 ResultSetReference ref = new ResultSetReference(rs, rawString);
                 output(ref);
                 outputMessage("i.selected", ref.getRecordCount());
             } finally {
                 rs.close();
             }
         }
 
     }
 
     /**
      * Update statement (contains all SQL excepted a Select SQL) command.
      */
     static final class UpdateAndOthers extends RawSQL {
 
         public UpdateAndOthers() {
             // empty
         }
 
         @Override
         public boolean isReadOnly() {
             return false;
         }
 
         @Override
         protected void execute(Statement stmt, String sql) throws SQLException {
             final int updatedCount = executeUpdate(stmt, sql);
             final String msgId;
             if (sql.matches("(?i)\\s*UPDATE.*")) {
                 msgId = "i.updated";
             } else if (sql.matches("(?i)\\\\s*INSERT.*")) {
                 msgId = "i.inserted";
             } else if (sql.matches("(?i)\\\\s*DELETE.*")) {
                 msgId = "i.deleted";
             } else {
                 msgId = "i.proceeded";
             }
             outputMessage(msgId, updatedCount);
         }
 
     }
 
 }
