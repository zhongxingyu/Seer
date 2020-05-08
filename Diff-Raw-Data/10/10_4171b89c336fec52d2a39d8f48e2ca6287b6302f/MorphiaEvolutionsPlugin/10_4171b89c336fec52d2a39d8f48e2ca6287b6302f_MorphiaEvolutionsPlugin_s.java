 package play.modules.morphiaevolutions;
 
 import com.mongodb.*;
 import com.mongodb.DB;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.Play;
 import play.PlayPlugin;
 import play.classloading.ApplicationClasses;
 import play.classloading.ApplicationClassloader;
 import play.db.*;
 import play.exceptions.PlayException;
 import play.exceptions.UnexpectedException;
 import play.libs.Codec;
 import play.libs.IO;
 import play.modules.morphia.MorphiaPlugin;
 import play.mvc.Http;
 import play.mvc.results.Redirect;
 import play.vfs.VirtualFile;
 
 import java.io.File;
 import java.lang.Override;
 import java.util.*;
 
 
 public class MorphiaEvolutionsPlugin extends PlayPlugin {
     public static final String EVOLUTIONS_COLLECTION = "evolutions";
     public static final String FIELD_REVISION = "revision";
     public static final String FIELD_HASH = "hash";
     public static final String FIELD_APPLIED_AT = "applied_at";
     public static final String FIELD_APPLY_SCRIPT = "apply_script";
     public static final String FIELD_REVERT_SCRIPT = "revert_script";
     public static final String FIELD_STATE = "state";
     public static final String FIELD_LAST_PROBLEM = "last_problem";
 
     public static final File evolutionsDirectory = Play.getFile("db/evolutions");
 
 
     public static void main(String[] args) {
         Play.id = System.getProperty("play.id");
         Play.applicationPath = new File(System.getProperty("application.path"));
         Play.guessFrameworkPath();
         Play.readConfiguration();
         Play.javaPath = new ArrayList<VirtualFile>();
         Play.classes = new ApplicationClasses();
         Play.classloader = new ApplicationClassloader();
         Logger.init();
         Logger.setUp("ERROR");
         new MorphiaPlugin().onApplicationStart();
 
         /** Sumary **/
         Evolution database = listDatabaseEvolutions().peek();
         Evolution application = listApplicationEvolutions().peek();
 
         if ("resolve".equals(System.getProperty("mode"))) {
             try {
                 checkEvolutionsState();
                 System.out.println("~");
                 System.out.println("~ Nothing to resolve...");
                 System.out.println("~");
                 return;
             } catch (InconsistentDatabase e) {
                 resolve(e.revision);
                 System.out.println("~");
                 System.out.println("~ Revision " + e.revision + " has been resolved;");
                 System.out.println("~");
             } catch (InvalidDatabaseRevision e) {
                 // see later
             }
         }
 
         /** Check inconsistency **/
         try {
             checkEvolutionsState();
         } catch (InconsistentDatabase e) {
             System.out.println("~");
             System.out.println("~ Your database is an inconsistent state!");
             System.out.println("~");
             System.out.println("~ While applying this script part:");
             System.out.println("");
             System.out.println(e.evolutionScript);
             System.out.println("");
             System.out.println("~ The following error occured:");
             System.out.println("");
             System.out.println(e.error);
             System.out.println("");
            System.out.println("~ Please correct it manually, and mark it resolved by running `play evolutions:resolve`");
             System.out.println("~");
             return;
         } catch (InvalidDatabaseRevision e) {
             // see later
         }
 
         System.out.print("~ Application revision is " + application.revision + " [" + application.hash.substring(0, 7) + "]");
         System.out.println(" and Database revision is " + database.revision + " [" + database.hash.substring(0, 7) + "]");
         System.out.println("~");
 
         /** Evolution script **/
         List<Evolution> evolutions = getEvolutionScript();
         if (evolutions.isEmpty()) {
             System.out.println("~ Your database is up to date");
             System.out.println("~");
         } else {
 
             if ("apply".equals(System.getProperty("mode"))) {
 
                 System.out.println("~ Applying evolutions:");
                 System.out.println("");
                 System.out.println("# ------------------------------------------------------------------------------");
                 System.out.println("");
                 System.out.println(toHumanReadableScript(evolutions));
                 System.out.println("");
                 System.out.println("# ------------------------------------------------------------------------------");
                 System.out.println("");
                 if (applyScript(true)) {
                     System.out.println("~");
                     System.out.println("~ Evolutions script successfully applied!");
                     System.out.println("~");
                 } else {
                     System.out.println("~");
                     System.out.println("~ Can't apply evolutions...");
                     System.out.println("~");
                 }
 
 
             } else if ("markApplied".equals(System.getProperty("mode"))) {
 
                 if (applyScript(false)) {
                     System.out.println("~ Evolutions script marked as applied!");
                     System.out.println("~");
                 } else {
                     System.out.println("~ Can't apply evolutions...");
                     System.out.println("~");
                 }
 
             } else {
 
                 System.out.println("~ Your database needs evolutions!");
                 System.out.println("");
                 System.out.println("# ------------------------------------------------------------------------------");
                 System.out.println("");
                 System.out.println(toHumanReadableScript(evolutions));
                 System.out.println("");
                 System.out.println("# ------------------------------------------------------------------------------");
                 System.out.println("");
                System.out.println("~ Run `play evolutions:apply` to automatically apply this script to the database");
                System.out.println("~ or apply it yourself and mark it done using `play evolutions:markApplied`");
                 System.out.println("~");
             }
 
 
 
         }
         return;
     }
 
     @Override
     public boolean rawInvocation(Http.Request request, Http.Response response) throws Exception {
 
         // Mark an evolution as resolved
         if (Play.mode.isDev() && request.method.equals("POST") && request.url.matches("^/@morphiaevolutions/force/[0-9]+$")) {
             int revision = Integer.parseInt(request.url.substring(request.url.lastIndexOf("/") + 1));
             resolve(revision);
             new Redirect("/").apply(request, response);
             return true;
         }
 
         // Apply the current evolution script
         if (Play.mode.isDev() && request.method.equals("POST") && request.url.equals("/@morphiaevolutions/apply")) {
             applyScript(true);
             new Redirect("/").apply(request, response);
             return true;
         }
         return super.rawInvocation(request, response);
     }
 
     @Override
     public void beforeInvocation() {
         if(isDisabled() || Play.mode.isProd()) {
             return;
         }
         try {
             checkEvolutionsState();
         } catch (InvalidDatabaseRevision e) {
             if ("mem".equals(Play.configuration.getProperty("db")) && listDatabaseEvolutions().peek().revision == 0) {
                 Logger.info("Automatically applying evolutions in in-memory database");
                 applyScript(true);
             } else {
                 throw e;
             }
         }
     }
     @Override
     public void onApplicationStart() {
         if (!isDisabled() && Play.mode.isProd()) {
             try {
                 checkEvolutionsState();
             } catch (InvalidDatabaseRevision e) {
                 Logger.warn("");
                 Logger.warn("Your database is not up to date.");
                Logger.warn("Use `play evolutions` command to manage database evolutions.");
                 throw e;
             }
         }
     }
 
     public synchronized static void checkEvolutionsState() {
         List<Evolution> evolutionScript = getEvolutionScript();
 
         DB db = MorphiaPlugin.ds().getDB();
         try {
             if (db.collectionExists(EVOLUTIONS_COLLECTION)) {
                 DBCursor results = db.getCollection(EVOLUTIONS_COLLECTION).find(new BasicDBObject(FIELD_STATE, "/applying_.*/"));
                 for (DBObject result : results) {
                     String script = String.format("# --- Rev:%s,%s - %s\n\n%s",
                             result.get(FIELD_REVISION),
                             result.get(FIELD_STATE).toString().equals("applying_up") ? "Ups" : "Downs",
                             result.get(FIELD_HASH),
                             result.get(FIELD_STATE).toString().equals("applying_up") ? result.get(FIELD_APPLY_SCRIPT) : result.get(FIELD_REVERT_SCRIPT)
                     );
                     String error = result.get(FIELD_LAST_PROBLEM).toString();
                     int revision = Integer.parseInt(result.get(FIELD_REVISION).toString());
                     throw new InconsistentDatabase(script, error, revision);
                 }
             }
         } catch (Exception e) {
             throw new UnexpectedException(e);
         }
 
         if (!evolutionScript.isEmpty()) {
             throw new InvalidDatabaseRevision(toHumanReadableScript(evolutionScript));
         }
 
     }
 
     public static synchronized boolean applyScript(boolean runScript) {
         DB db = MorphiaPlugin.ds().getDB();
         int applying = -1;
         try {
             for (Evolution evolution : getEvolutionScript()) {
                 applying = evolution.revision;
 
                 if (evolution.applyUp) {
                     DBObject dbo = new BasicDBObject();
                     dbo.put(FIELD_REVISION, evolution.revision);
                     dbo.put(FIELD_HASH, evolution.hash);
                     dbo.put(FIELD_APPLIED_AT, new Date(System.currentTimeMillis()));
                     dbo.put(FIELD_APPLY_SCRIPT, evolution.js_up);
                     dbo.put(FIELD_REVERT_SCRIPT, evolution.js_down);
                     dbo.put(FIELD_STATE, "applying_up");
                     db.getCollection(EVOLUTIONS_COLLECTION).insert(dbo);
                 } else {
                     db.getCollection(EVOLUTIONS_COLLECTION).update(
                             new BasicDBObject(FIELD_REVISION, evolution.revision),
                             new BasicDBObject("$set", new BasicDBObject(FIELD_STATE, "applying_down"))
                     );
                 }
                 if (runScript) {
                     db.doEval(evolution.applyUp ? evolution.js_up : evolution.js_down);
                 }
                 if (evolution.applyUp) {
                     db.getCollection(EVOLUTIONS_COLLECTION).update(
                             new BasicDBObject(FIELD_REVISION, evolution.revision),
                             new BasicDBObject("$set", new BasicDBObject(FIELD_STATE, "applied"))
                     );
                 } else {
                     db.getCollection(EVOLUTIONS_COLLECTION).remove(new BasicDBObject(FIELD_REVISION, evolution.revision));
                 }
             }
             return true;
         } catch (Exception e) {
             String message = e.getMessage();
             db.getCollection(EVOLUTIONS_COLLECTION).update(
                     new BasicDBObject(FIELD_REVISION, applying),
                     new BasicDBObject(FIELD_LAST_PROBLEM, message)
             );
             Logger.error(e, "Can't apply evolution");
             return false;
         }
     }
 
     public static synchronized List<Evolution> getEvolutionScript() {
         Stack<Evolution> app = listApplicationEvolutions();
         Stack<Evolution> db = listDatabaseEvolutions();
         List<Evolution> downs = new ArrayList<Evolution>();
         List<Evolution> ups = new ArrayList<Evolution>();
 
         // Apply non conflicting evolutions (ups and downs)
         while (db.peek().revision != app.peek().revision) {
             if (db.peek().revision > app.peek().revision) {
                 downs.add(db.pop());
             } else {
                 ups.add(app.pop());
             }
         }
 
         // Revert conflicting to fork node
         while (db.peek().revision == app.peek().revision && !(db.peek().hash.equals(app.peek().hash))) {
             downs.add(db.pop());
             ups.add(app.pop());
         }
 
         // Ups need to be applied earlier first
         Collections.reverse(ups);
 
         List<Evolution> script = new ArrayList<Evolution>();
         script.addAll(downs);
         script.addAll(ups);
 
         return script;
     }
 
     public synchronized static Stack<Evolution> listDatabaseEvolutions() {
         Stack<Evolution> evolutions = new Stack<Evolution>();
         evolutions.add(new Evolution(0, "", "", false));
         DB db = MorphiaPlugin.ds().getDB();
         if (db.collectionExists(EVOLUTIONS_COLLECTION)) {
             DBCursor results = db.getCollection(EVOLUTIONS_COLLECTION).find();
             for (DBObject result : results) {
                 evolutions.push(new Evolution(Double.valueOf(result.get(FIELD_REVISION).toString()).intValue(), result.get(FIELD_APPLY_SCRIPT).toString(), result.get(FIELD_REVERT_SCRIPT).toString(), false));
             }
         }
         return evolutions;
     }
 
 
     public synchronized static Stack<Evolution> listApplicationEvolutions() {
         Stack<Evolution> evolutions = new Stack<Evolution>();
         evolutions.add(new Evolution(0, "", "", true));
         if (evolutionsDirectory.exists()) {
             for (File evolution : evolutionsDirectory.listFiles()) {
                 if (evolution.getName().matches("^[0-9]+[.]js$")) {
                     if (Logger.isTraceEnabled()) {
                         Logger.trace("Loading evolution %s", evolution);
                     }
 
                     int version = Integer.parseInt(evolution.getName().substring(0, evolution.getName().indexOf(".")));
                     String sql = IO.readContentAsString(evolution);
                     StringBuffer js_up = new StringBuffer();
                     StringBuffer js_down = new StringBuffer();
                     StringBuffer current = new StringBuffer();
                     for (String line : sql.split("\r?\n")) {
                         if (line.trim().matches("^//.*[!]Ups")) {
                             current = js_up;
                         } else if (line.trim().matches("^//.*[!]Downs")) {
                             current = js_down;
                         } else if (line.trim().startsWith("//")) {
                             // skip
                         } else if (!StringUtils.isEmpty(line.trim())) {
                             current.append(line).append("\n");
                         }
                     }
                     evolutions.add(new Evolution(version, js_up.toString(), js_down.toString(), true));
                 }
             }
             Collections.sort(evolutions);
         }
         return evolutions;
     }
 
     public static synchronized void resolve(int revision) {
         try {
             DB db = MorphiaPlugin.ds().getDB();
             BasicDBList queryUpdate = new BasicDBList();
             queryUpdate.add(new BasicDBObject(FIELD_REVISION, revision));
             queryUpdate.add(new BasicDBObject(FIELD_STATE, "applying_up"));
             db.getCollection(EVOLUTIONS_COLLECTION).update(
                     queryUpdate,
                     new BasicDBObject("$set", new BasicDBObject(FIELD_STATE, "applied")));
 
             BasicDBList queryDelete = new BasicDBList();
             queryDelete.add(new BasicDBObject(FIELD_REVISION, revision));
             queryDelete.add(new BasicDBObject(FIELD_STATE, "applying_down"));
             db.getCollection(EVOLUTIONS_COLLECTION).remove(queryDelete);
         } catch (Exception e) {
             throw new UnexpectedException(e);
         }
     }
 
 
 
     public static String toHumanReadableScript(List<Evolution> evolutionScript) {
         // Construct the script
         StringBuilder sql = new StringBuilder();
         boolean containsDown = false;
         for (Evolution evolution : evolutionScript) {
             if (!evolution.applyUp) {
                 containsDown = true;
             }
             sql.append("// --- Rev:").append(evolution.revision).append(",").append(evolution.applyUp ? "Ups" : "Downs").append(" - ").append(evolution.hash.substring(0, 7)).append("\n");
             sql.append("\n");
             sql.append(evolution.applyUp ? evolution.js_up : evolution.js_down);
             sql.append("\n\n");
         }
 
         if (containsDown) {
             sql.insert(0, "// !!! WARNING! This script contains DOWNS evolutions that are likely destructives\n\n");
         }
 
         return sql.toString().trim();
     }
 
     /**
      * Checks if evolutions is disabled in application.conf (property "morphia.evolutions.enabled")
      */
     private boolean isDisabled() {
         return "false".equals(Play.configuration.getProperty("morphia.evolutions.enabled", "true"));
     }
 
 
 
     /**
      * Datastructure representing one evolution.
      * More than broadly inspired by the Play core equivalent.
      * @see play.db.Evolutions.Evolution
      */
     protected static class Evolution implements Comparable<Evolution> {
         int revision;
         String js_up;
         String js_down;
         String hash;
         boolean applyUp;
 
         public Evolution(int revision, String js_up, String js_down, boolean applyUp) {
             this.revision = revision;
             this.js_down = js_down;
             this.js_up = js_up;
             this.hash = Codec.hexSHA1(js_up + js_down);
             this.applyUp = applyUp;
         }
 
         public int compareTo(Evolution o) {
             return this.revision - o.revision;
         }
 
         @Override
         public boolean equals(Object obj) {
             return (obj instanceof Evolution) && ((Evolution) obj).revision == this.revision;
         }
 
         @Override
         public int hashCode() {
             return revision;
         }
 
     }
 
 
     // Exceptions
     public static class InvalidDatabaseRevision extends PlayException {
 
         String evolutionScript;
 
         public InvalidDatabaseRevision(String evolutionScript) {
             this.evolutionScript = evolutionScript;
         }
 
         @Override
         public String getErrorTitle() {
             return "Your database needs evolution!";
         }
 
         @Override
         public String getErrorDescription() {
             return "An SQL script will be run on your database.";
         }
 
         @Override
         public String getMoreHTML() {
             return "<h3>This SQL script must be run:</h3><pre style=\"background:#fff; border:1px solid #ccc; padding: 5px\">" + evolutionScript + "</pre><form action='/@morphiaevolutions/apply' method='POST'><input type='submit' value='Apply evolutions'></form>";
         }
     }
 
     public static class InconsistentDatabase extends PlayException {
 
         String evolutionScript;
         String error;
         int revision;
 
         public InconsistentDatabase(String evolutionScript, String error, int revision) {
             this.evolutionScript = evolutionScript;
             this.error = error;
             this.revision = revision;
         }
 
         @Override
         public String getErrorTitle() {
             return "Your database is an inconsistent state!";
         }
 
         @Override
         public String getErrorDescription() {
             return "An evolution has not been applied properly. Please check the problem and resolve it manually before making it as resolved.";
         }
 
         @Override
         public String getMoreHTML() {
             return "<h3>This SQL script has been run, and there was a problem:</h3><pre style=\"background:#fff; border:1px solid #ccc; padding: 5px\">" + evolutionScript + "</pre><h4>This error has been thrown:</h4><pre style=\"background:#fff; border:1px solid #ccc; color: #c00; padding: 5px\">" + error + "</pre><form action='/@morphiaevolutions/force/" + revision + "' method='POST'><input type='submit' value='Mark it resolved'></form>";
         }
     }
 
 }
