 package org.basex.core.proc;
 
 import static org.basex.Text.*;
 import static org.basex.core.Commands.*;
 import org.basex.core.Process;
 import org.basex.data.Data;
 import org.basex.io.IO;
 
 /**
  * Evaluates the 'drop database' command.
  *
  * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
  * @author Christian Gruen
  */
 public final class DropDB extends Process {
   /**
    * Constructor.
    * @param n name of database
    */
   public DropDB(final String n) {
     super(STANDARD, n);
   }
   
   @Override
   protected boolean exec() {
     final String db = args[0];
     final Data data = context.data();
     if(data != null && data.meta.dbname.equals(db)) exec(new Close());
 
     return !IO.dbpath(db).exists() ? error(DBNOTFOUND, db) :
       drop(db) ? info(DBDROPPED) : error(DBNOTDROPPED);
   }
 
   /**
    * Delete a database.
    * @param db database name
    * @return success of operation
    */
   public static boolean drop(final String db) {
    return IO.dbdelete(IO.get(db).dbname(), null);
   }
   
   @Override
   public String toString() {
     return COMMANDS.DROP.name() + " " + DROP.DB + args();
   }
 }
