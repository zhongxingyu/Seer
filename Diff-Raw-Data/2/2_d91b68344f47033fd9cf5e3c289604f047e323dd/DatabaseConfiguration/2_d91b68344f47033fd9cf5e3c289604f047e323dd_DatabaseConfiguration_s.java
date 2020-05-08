 /*
  * Copyright (c) 2012 Sparsity Technologies www.sparsity-technologies.com
  * 
  * This file is part of 'dexjava-etl'.
  * 
  * Licensed under the GNU Lesser General Public License (LGPL) v3, (the
  * "License"). You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.gnu.org/licenses/lgpl-3.0.txt
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.sparsity.dex.etl.config.bean;
 
 import java.io.File;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sparsity.dex.etl.DexUtilsException;
 import com.sparsity.dex.gdb.Attribute;
 import com.sparsity.dex.gdb.AttributeList;
 import com.sparsity.dex.gdb.AttributeListIterator;
 import com.sparsity.dex.gdb.Database;
 import com.sparsity.dex.gdb.Dex;
 import com.sparsity.dex.gdb.DexConfig;
 import com.sparsity.dex.gdb.DexProperties;
 import com.sparsity.dex.gdb.Graph;
 import com.sparsity.dex.gdb.Session;
 import com.sparsity.dex.gdb.Type;
 import com.sparsity.dex.gdb.TypeList;
 import com.sparsity.dex.gdb.TypeListIterator;
 
 /**
  * Allows for opening and closing a Dex database.
  * <p>
  * It also automatically manages a per-thread "working" {@link Session}. The
  * user just gets a Session ({@link #getSession()}) and this automatically
  * creates one if required or returns its {@link Session}. Moreover, this
  * provides an exclusive per-thread {@link Session}.
  * 
  * @author Sparsity Technologies
  * 
  */
 public class DatabaseConfiguration {
 
     /**
      * {@link Log} instance.
      */
     private static Log log = LogFactory.getLog(DatabaseConfiguration.class);
 
     /**
      * Unique name for the {@link DatabaseConfiguration}.
      */
     private String name = null;
 
     /**
      * Alias database.
      */
     private String alias = null;
 
     /**
      * Path database.
      */
     private String path = null;
 
     /**
      * Dex configuration file.
      */
     private String dexConf = null;
 
     /**
      * Manages a Dex {@link Session} as well as its {@link Graph} instance.
      * 
      * @author Sparsity Technologies
      * 
      */
     private class SessionManager {
 
         /**
          * {@link Graph} instance.
          */
         private Graph graph = null;
 
         /**
          * {@link Session} instance.
          */
         private Session session = null;
 
         /**
          * Thread identifier.
          */
         long thId = Thread.currentThread().getId();
 
         /**
          * Creates a new instance.
          */
         public SessionManager() {
         }
 
         /**
          * Closes the {@link Session}.
          */
         public void closeSession() {
             if (session != null && !session.isClosed()) {
                 session.close();
                 session = null;
                 graph = null;
                 log.debug("Dex Session was closed for thread " + thId);
             }
         }
 
         /**
          * Gets if the {@link Session} is closed.
          * 
          * @return
          */
         public boolean isClosed() {
             return session == null || session.isClosed();
         }
 
         /**
          * Gets the {@link Session}.
          * <p>
          * It creates a new one {@link Session} if there was no {@link Session}
          * or if it was already closed.
          * 
          * @return The {@link Session}.
          */
         public Session getSession() {
             if (isClosed()) {
                 session = DatabaseConfiguration.this.db.newSession();
                 graph = session.getGraph();
                 log.debug("Dex Session was created for thread " + thId);
             }
             return session;
         }
 
         /**
          * Gets the {@link Graph}.
          * <p>
          * It creates a new one {@link Session} if there was no {@link Session}
          * or if it was already closed.
          * 
          * @return The {@link Graph} of the {@link Session}.
          */
         public Graph getGraph() {
             if (isClosed()) {
                 getSession();
             }
             return graph;
         }
     }
 
     /**
      * Manages a per-thread local {@link SessionManager} instance.
      * <p>
      * Therefore, each thread will just access its {@link SessionManager}
      * instance.
      */
     private ThreadLocal<SessionManager> sessMngr = new ThreadLocal<SessionManager>() {
         @Override
         protected SessionManager initialValue() {
             return new SessionManager();
         }
     };
 
     /**
      * {@link DexConfig} instance.
      */
     private DexConfig dexCfg = null;
 
     /**
      * {@link Dex} instance.
      */
     private Dex dex = null;
 
     /**
      * {@link Database} instance.
      */
     private Database db = null;
 
     /**
      * Parent {@link Configuration} instance.
      */
     private Configuration configuration;
 
     /**
      * Gets the unique name.
      * 
      * @return The unique name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the unique name.
      * 
      * @param n
      *            The unique name. It cannot be <code>null</code> or a blank
      *            string.
      */
     public void setName(String n) {
         if (n == null || n.matches("[\\s]++")) {
             log.error("Name cannot be null or a blank string.");
             throw new IllegalArgumentException(
                     "Name cannot be null or a blank string.");
         }
         name = n;
     }
 
     /**
      * Gets the alias.
      * 
      * @return The alias.
      */
     public String getAlias() {
         return alias;
     }
 
     /**
      * Sets the alias.
      * 
      * @param a
      *            The alias. It cannot be <code>null</code> or a blank string.
      */
     public void setAlias(String a) {
         if (a == null || a.matches("[\\s]++")) {
             log.error("Alias cannot be null or a blank string.");
             throw new IllegalArgumentException(
                     "Alias cannot be null or a blank string.");
         }
         alias = a;
     }
 
     /**
      * Gets the path.
      * 
      * @return The path.
      */
     public String getPath() {
         return path;
     }
 
     /**
      * Sets the path.
      * 
      * @param p
      *            The path. It cannot be <code>null</code> or a blank string.
      */
     public void setPath(String p) {
         if (p == null || p.matches("[\\s]++")) {
             String msg = new String("Path cannot be null or a blank string.");
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
         path = p;
     }
 
     /**
      * Gets the Dex runtime configuration file path.
      * 
      * @return The Dex runtime configuration file path.
      */
     public String getDexConfiguration() {
         return dexConf;
     }
 
     /**
      * Sets the Dex runtime configuration file path.
      * 
      * @param c
      *            The Dex runtime configuration file path.
      */
     public void setDexConfiguration(String c) {
         if (c == null || c.matches("[\\s]++")) {
             String msg = new String(
                     "Dex runtime configuration file path cannot be null or a blank string.");
             log.error(msg);
             throw new IllegalArgumentException(msg);
         }
         dexConf = c;
     }
 
     /**
      * Gets the parent {@link Configuration} instance.
      * 
      * @return The parent {@link Configuration} instance.
      */
     public Configuration getConfiguration() {
         return configuration;
     }
 
     /**
      * Sets the parent {@link Configuration} instance.
      * 
      * @param c
      *            The parent {@link Configuration} instance.
      */
     public void setConfiguration(Configuration c) {
         configuration = c;
     }
 
     @Override
     public boolean equals(Object o) {
         boolean result = false;
         if (o instanceof DatabaseConfiguration) {
             DatabaseConfiguration aux = (DatabaseConfiguration) o;
             String n = getName();
             if (n == null) {
                 result = (aux.getName() == null);
             } else {
                 result = n.equals(aux.getName());
             }
 
             if (result) {
                 String a = getAlias();
                 if (a == null) {
                     result = (aux.getAlias() == null);
                 } else {
                     result = a.equals(aux.getAlias());
                 }
             }
 
             if (result) {
                 String p = getPath();
                 if (p == null) {
                     result = (aux.getPath() == null);
                 } else {
                     result = p.equals(aux.getPath());
                 }
             }
         }
         return result;
     }
 
     /**
      * Gets if the Database has been already closed or not.
      * 
      * @return <code>true</code> if closed, <code>false</code> otherwise.
      */
     public boolean isClosed() {
         return (dex == null && db == null);
     }
 
     /**
      * Opens the Database and sets the working {@link Session} for the calling
      * thread.
      * <p>
      * The Database is open once, so this opens the Database just if it has not
      * been opened before.
      * <p>
      * In any case, it starts the working {@link Session} for the calling thread
      * if necessary.
      */
     public void openDatabase() {
         if (isClosed()) {
             File f = new File(getPath());
             if (dexConf != null) {
                 DexProperties.load(dexConf);
             }
             dexCfg = new DexConfig();
             dex = new Dex(dexCfg);
             db = null;
             try {
                 if (f.exists()) {
                    db = dex.open(f.getAbsolutePath(), true);
                     if (db.getAlias().compareTo(getAlias()) != 0) {
                         throw new DexUtilsException(
                                 "Database with an unexpected name/alias");
                     }
                 } else {
                     db = dex.create(f.getAbsolutePath(), alias);
                 }
             } catch (Exception e) {
                 String msg = new String("DexUtils cannot open/create the "
                         + getAlias() + " database " + "located at "
                         + f.getAbsolutePath());
                 log.error(msg, e);
                 throw new DexUtilsException(msg, e);
             }
             log.info("Database " + getAlias() + "[" + f.getAbsolutePath()
                     + "] was opened");
         }
 
         this.sessMngr.get().getSession();
     }
 
     /**
      * Closes and opens the Database.
      * <p>
      * Closing the database closes the working {@link Session}.
      */
     public void restartDatabase() {
         closeDatabase();
         openDatabase();
     }
 
     /**
      * Closes the Database.
      * <p>
      * Therefore it also closes the working {@link Session}.
      */
     public void closeDatabase() {
         //
         // FIXME This just closes the Session of the calling thread, others may
         // remain
         // open!
         //
         if (!sessMngr.get().isClosed()) {
             sessMngr.get().closeSession();
         }
         if (!isClosed()) {
             db.close();
             db = null;
             dex.close();
             dex = null;
             log.info("Database " + getAlias() + " was closed");
         }
     }
 
     /**
      * Gets the working {@link Session} for the calling thread.
      * <p>
      * If necessary, it creates a new {@link Session}.
      * 
      * @return The working {@link Session} for the calling thread.
      */
     public Session getSession() {
         return sessMngr.get().getSession();
     }
 
     /**
      * Gets the {@link Graph} of the working {@link Session} for the calling
      * thread.
      * <p>
      * If necessary, it creates a new {@link Session}.
      * 
      * @return The working {@link Graph} for the calling thread.
      */
     public Graph getGraph() {
         return sessMngr.get().getGraph();
     }
 
     /**
      * Drops all attributes and node and edge types.
      */
     public void dropSchema() {
         if (isClosed()) {
             openDatabase();
         }
         Graph graph = sessMngr.get().getGraph();
 
         TypeList types = graph.findEdgeTypes();
         TypeListIterator typeIt = types.iterator();
         while (typeIt.hasNext()) {
             Integer type = typeIt.next();
             AttributeList alist = graph.findAttributes(type);
             AttributeListIterator attrIt = alist.iterator();
             while (attrIt.hasNext()) {
                 graph.removeAttribute(attrIt.next());
             }
             graph.removeType(type);
         }
         types = graph.findNodeTypes();
         typeIt = types.iterator();
         while (typeIt.hasNext()) {
             Integer type = typeIt.next();
             AttributeList alist = graph.findAttributes(type);
             AttributeListIterator attrIt = alist.iterator();
             while (attrIt.hasNext()) {
                 graph.removeAttribute(attrIt.next());
             }
             graph.removeType(type);
         }
         log.info("Schema for the Database " + getAlias() + " was droped");
     }
 
     /**
      * Gets the Dex attribute identifier for the given attribute.
      * 
      * @param typename
      *            Node or edge type name.
      * @param name
      *            Attribute name.
      * @return The Dex attribute identifier or
      *         {@link Attribute#InvalidAttribute} if if does not exist.
      */
     public Integer getAttributeIdentifier(String typename, String name) {
         if (isClosed()) {
             openDatabase();
         }
         int type = getTypeIdentifier(typename);
 
         if (type != Type.InvalidType) {
             return sessMngr.get().getGraph().findAttribute(type, name);
         } else {
             return Attribute.InvalidAttribute;
         }
     }
 
     /**
      * Gets the Dex type identifier for the given type.
      * 
      * @param name
      *            Node or edge type name.
      * @return The Dex node or edge type identifier or {@link Type#InvalidType}
      *         if it does not exist.
      */
     public Integer getTypeIdentifier(String name) {
         if (isClosed()) {
             openDatabase();
         }
         return sessMngr.get().getGraph().findType(name);
     }
 }
