 /*
  * PostgreSQLDataBackend.java
  * 
  * Copyright (C) 2009 Nicola Roberto Viganò
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gestionecassa.server.datamanager.backends;
 
 import gestionecassa.Admin;
 import gestionecassa.Article;
 import gestionecassa.ArticleWithOptions;
 import gestionecassa.Cassiere;
 import gestionecassa.EventDate;
 import gestionecassa.Log;
 import gestionecassa.OrganizedEvent;
 import gestionecassa.order.EntrySingleArticle;
 import gestionecassa.order.EntrySingleArticleWithOption;
 import gestionecassa.order.EntrySingleOption;
 import gestionecassa.order.Order;
 import gestionecassa.server.datamanager.BackendAPI_2;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author ben
  */
 public class PostgreSQLDataBackend implements BackendAPI_2 {
 
     /**
      * reference to the logger for the DB
      */
     Logger logger;
 
     /**
      * Connection to the DB
      */
     Connection db;
 
     /**
      * List of tables and information on how to create them if missing
      */
     Map<String,String> tables;
 
     /**
      * Default constructor
      */
     public PostgreSQLDataBackend() {
         logger = Log.GESTIONECASSA_SERVER_DATAMANAGER_DB;
 
         tables = new ConcurrentSkipListMap<String, String>();
         tables.put("01_cassieres",
                 "id_cassiere serial PRIMARY KEY, " +
                 "username text UNIQUE, " +
                 "password text, " +
                 "enabled boolean " +
 //                "trusted boolean " +
                 "");
         tables.put("02_viewers",
                 "id_viewer serial PRIMARY KEY, " +
                 "username text UNIQUE, " +
                 "password text, " +
                 "enabled boolean ");
         tables.put("03_admins",
                 "id_admin serial PRIMARY KEY, " +
                 "username text UNIQUE, " +
                 "password text, " +
                 "enabled boolean ");
         tables.put("04_events",
                 "id_event serial PRIMARY KEY, " +
                 "name text UNIQUE ");
         tables.put("05_dates_event",
                 "id_date_event serial PRIMARY KEY, " +
                 "id_event integer REFERENCES events ON DELETE CASCADE, " +
                 "title text, " +
                 "start_date timestamp UNIQUE, " +
                 "end_date timestamp UNIQUE ");
         tables.put("06_orders",
                 "id_order serial PRIMARY KEY, " +
                 "time_order timestamp, " +
                 "id_date_event integer DEFAULT '1' REFERENCES dates_event ON DELETE SET DEFAULT, " +
                 "hostname text, " +
                 "id_cassiere integer REFERENCES cassieres ON DELETE RESTRICT, " +
                 "price_tot numeric ");
         tables.put("07_articles",
                 "id_article serial PRIMARY KEY, " +
                 "name text UNIQUE, " +
                 "enabled boolean, " +
                 "has_options boolean, " +
                 "price numeric, " +
                 "num_pos integer NOT NULL ");
         tables.put("08_options",
                 "id_option serial PRIMARY KEY, " +
                 "id_article integer REFERENCES articles ON DELETE CASCADE, " +
                 "name text ");
         tables.put("09_articles_in_order",
                 "id_art_in_ord serial PRIMARY KEY, " +
                 "id_order integer REFERENCES orders ON DELETE CASCADE, " +
                 "id_article integer REFERENCES articles ON DELETE RESTRICT, " +
                 "num_tot integer ");
         tables.put("10_opts_of_article_in_order",
                 "id_art_in_ord integer REFERENCES articles_in_order ON DELETE CASCADE, " +
                 "id_option integer REFERENCES options ON DELETE RESTRICT, " +
                 "num_parz integer ");
     }
 
     /**
      * Initialization method: if it fails the DB is useless.
      *
      * @param url Url of the DB to use
      * 
      * @throws IOException
      */
     public void init(String url) throws IOException {
         try {
             Class.forName("org.postgresql.Driver");
             String username = "gestionecassa";
             String password = "GestioneCassa";
             db = DriverManager.getConnection(url, username, password);
         } catch (ClassNotFoundException ex) {
             logger.error("classe non trovata", ex);
             throw new IOException(ex);
         } catch (SQLException ex) {
             logger.error("errore connessione db", ex);
             throw new IOException(ex);
         }
         
         String query =  "SELECT table_name" +
                         "   FROM information_schema.tables" +
                         "   WHERE table_schema='public'" +
                         "       AND table_type='BASE TABLE';";
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 Set<String> tabelleDB = new ConcurrentSkipListSet<String>();
                 while (rs.next()) {
                     tabelleDB.add(rs.getString("table_name"));
                 }
                 rs.close();
 
                 for (String table_ref : tables.keySet()) {
                     String table_name = table_ref.substring(3);
                     if (!tabelleDB.contains(table_name)) {
                         logger.warn(table_name + " is not in the table list. Creating " +
                                 " blank one");
                         genericCommit("CREATE TABLE " + table_name + " ( " +
                                         tables.get(table_ref) + " );");
                     }
                 }
 
                String queryEvent = "SELECT * FROM events WHERE id_event = '1';";
                 rs = st.executeQuery(queryEvent);
                 if (!rs.next()) {
                     queryEvent = "INSERT INTO events (name) VALUES ('other');";
                     genericCommit(queryEvent);
                     Timestamp date = new Timestamp(new Date().getTime());
                     queryEvent = "INSERT INTO dates_event " +
                             "   (title, id_event, start_date, end_date ) " +
                             "VALUES ('other', '1', '" + date.toString() +
                             "', '" + date.toString() + "' )";
                     genericCommit(queryEvent);
                 } else if (!rs.getString("name").equals("other")) {
                     st.close();
                     throw new IOException("Strange format of events");
                 }
 
             } catch (SQLException ex) {
                 logger.error("Errore nella query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nella comunicazione col DB", ex);
             throw new IOException(ex);
         }
     }
 
     //-----------------//
 
     /**
      *
      * @param article
      * @param position
      * @throws IOException
      */
     public void addArticleToListAt(Article article, int position) throws IOException {
         
         addArticleToList(article);
 
         moveArticleAt(article, position);
     }
 
     /**
      * 
      * @param article
      * @param position
      * @throws IOException
      */
     public void moveArticleAt(Article article, int position) throws IOException {
         
         String orderQuery = "SELECT id_article, name, num_pos" +
                             "   FROM articles;";
         try {
             Statement stIns = db.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                                  ResultSet.CONCUR_UPDATABLE);
             try {
                 ResultSet rs = stIns.executeQuery(orderQuery);
                 while (rs.next()) {
                     if (rs.getString("name").equals(article.getName())) {
                         rs.updateInt("num_pos", position);
                         rs.updateRow();
                     } else {
                         int currPos = rs.getInt("num_pos");
                         if (currPos >= position) {
                             rs.updateInt("num_pos", currPos+1);
                             rs.updateRow();
                         }
                     }
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + orderQuery, ex);
                 throw new IOException(ex);
             } finally {
                 stIns.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nella counicazione col DB", ex);
             throw new IOException(ex);
         }
     }
 
     /**
      *
      * @param article
      * @throws IOException
      */
     public void addArticleToList(Article article) throws IOException {
         // Start by inserting the article in the proper table.
         String subQueryPos = "SELECT currval('articles_id_article_seq') -1";
         String insQuery =
                 "INSERT INTO articles (name, price, enabled, has_options, num_pos)" +
                 "VALUES ('" + article.getName() + "', '" +
                     article.getPrice() + "', " + article.isEnabled() + ", '" +
                     article.hasOptions() + "', (" + subQueryPos + ") );";
         genericCommit(insQuery);
 
         //then just if it has options
         if (article.hasOptions()) {
             String currValQuery = "SELECT currval('articles_id_article_seq');";
             try {
                 Statement stIns = db.createStatement();
                 try {
                     ResultSet keys = stIns.executeQuery(currValQuery);
                     keys.next();
                     int idArticle = keys.getInt("currval");
                     List<String> opts = ((ArticleWithOptions)article).getOptions();
                     String insOptsQuery =
                             "INSERT INTO options (id_article, name) VALUES ";
                     for (Iterator<String> it = opts.iterator(); it.hasNext();) {
                         String option = it.next();
                         insOptsQuery += "('" + idArticle +
                                 "', '" + option + "')" +
                                 (it.hasNext() ? "," : ";");
                     }
 
                     genericCommit(insOptsQuery);
                     
                 } catch (SQLException ex) {
                     logger.error("Errore con la query: " + currValQuery, ex);
                     throw new IOException(ex);
                 } finally {
                     stIns.close();
                 }
             } catch (SQLException ex) {
                 logger.error("Errore nella counicazione col DB", ex);
                 throw new IOException(ex);
             }
         }
     }
 
     /**
      * 
      * @param article
      * @param enable
      * 
      * @throws IOException
      */
     public void enableArticleFromList(Article article, boolean enable) throws IOException {
         String query =  "SELECT id_article, enabled" +
                         "   FROM articles" +
                         "   WHERE name = '" + article.getName() + "';";
         genericEnabler(query, enable);
     }
 
     /**
      * 
      * @return
      * @throws IOException
      */
     public List<Article> loadArticlesList() throws IOException {
 
         List<Article> outout = new Vector<Article>();
         String query =  "SELECT *" +
                         "   FROM articles" +
                         "   ORDER BY num_pos;";
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 while (rs.next()) {
                     int idArticle = rs.getInt("id_article");
                     
                     outout.add(rs.getBoolean("has_options")
                                     ? new ArticleWithOptions(idArticle,
                                             rs.getString("name"),
                                             rs.getDouble("price"),
                                             getOptionsByArticleID(idArticle),
                                             rs.getBoolean("enabled"))
                                     : new Article(idArticle,
                                             rs.getString("name"),
                                             rs.getDouble("price"),
                                             rs.getBoolean("enabled"))
                                );
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
 
         } catch (SQLException ex) {
             logger.error("Errore nella counicazione col DB", ex);
             throw new IOException(ex);
         }
         return outout;
     }
 
     //-----------------//
 
     /**
      * 
      * @param admin
      * @throws IOException
      */
     public void addAdmin(Admin admin) throws IOException {
         String insQuery =
                 "INSERT INTO admins (username, password, enabled)" +
                 "VALUES ('" + admin.getUsername() + "', '" +
                     admin.getPassword() + "', " +
                     admin.isEnabled() + " )";
         genericCommit(insQuery);
     }
 
     /**
      *
      * @return
      * @throws IOException
      */
     public List<Admin> loadAdminsList() throws IOException {
 
         String query =  "SELECT *" +
                         "   FROM admins;";
         List<Admin> outout = new LinkedList<Admin>();
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 while (rs.next()) {
                     outout.add(new Admin(rs.getInt("id_admin"),
                                          rs.getString("username"),
                                          rs.getString("password"),
                                          rs.getBoolean("enabled")));
                 }
                 rs.close();
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
 
         } catch (SQLException ex) {
             logger.error("Errore nella comunicazione col DB", ex);
             throw new IOException(ex);
         }
         return outout;
     }
 
     /**
      *
      * @param admin
      * @param enable
      * @throws IOException
      */
     public void enableAdmin(Admin admin, boolean enable) throws IOException {
         String query =  "SELECT id_admin, enabled" +
                         "   FROM admins" +
                         "   WHERE username = '" + admin.getUsername() + "';";
         genericEnabler(query, enable);
     }
 
     /**
      * 
      * @param cassiere
      * @throws IOException
      */
     public void addCassiere(Cassiere cassiere) throws IOException {
         String insQuery =
                 "INSERT INTO cassieres (username, password, enabled)" +
                 "VALUES ('" + cassiere.getUsername() + "', '" +
                     cassiere.getPassword() + "', " +
                     cassiere.isEnabled() + " )";
         genericCommit(insQuery);
     }
 
     /**
      * 
      * @return
      * @throws IOException
      */
     public List<Cassiere> loadCassiereList() throws IOException {
 
         String query =  "SELECT *" +
                         "   FROM cassieres;";
         List<Cassiere> outout = new LinkedList<Cassiere>();
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 while (rs.next()) {
                     outout.add(new Cassiere(rs.getInt("id_cassiere"),
                                             rs.getString("username"),
                                             rs.getString("password"),
                                             rs.getBoolean("enabled")));
                 }
                 rs.close();
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore di comunicazione col DB", ex);
             throw new IOException(ex);
         }
         return outout;
     }
 
     /**
      *
      * @param cassiere
      * @param enable
      * @throws IOException
      */
     public void enableCassiere(Cassiere cassiere, boolean enable) throws IOException {
         String query =  "SELECT id_cassiere, enabled" +
                         "   FROM cassieres" +
                         "   WHERE username = '" + cassiere.getUsername() + "';";
         genericEnabler(query, enable);
     }
     
     //-----------------//
 
     /**
      *
      * @param ev
      * @throws IOException
      */
     public void addOrganizedEvent(OrganizedEvent ev) throws IOException {
         String query = "INSERT INTO events (name) VALUES ('" + ev.name + "');";
         genericCommit(query);
 
         List<EventDate> dates = ev.datesList;
 
         query = "INSERT INTO dates_event (id_event, start_date, end_date, " +
                 "title) VALUES ";
         String idEventQuery = "(SELECT id_event FROM events WHERE name = '" +
                 ev.name + "')";
         for (Iterator<EventDate> it = dates.iterator(); it.hasNext();) {
             EventDate eventDate = it.next();
             query += "( " + idEventQuery + " , '" +
                     new Timestamp(eventDate.startDate.getTime()).toString() +
                     "', '" +
                     new Timestamp(eventDate.endDate.getTime()).toString() +
                     "', '" + eventDate.titleDate + "')" +
                     (it.hasNext() ? "," : ";");
         }
         genericCommit(query);
     }
 
     /**
      * 
      * @param evd
      * @param title
      * @throws IOException
      */
     public void addDateToOrgEvent(EventDate evd, String title) throws IOException {
 
         String idEventQuery = "(SELECT id_event FROM events WHERE name = '" +
                 title + "')";
         String query = "INSERT INTO dates_event (id_event, start_date, " +
                 "end_date, title) " +
                 "VALUES ( " + idEventQuery + ",  '" +
                     new Timestamp(evd.startDate.getTime()).toString() +
                     "', '" +
                     new Timestamp(evd.endDate.getTime()).toString() +
                     "', '" + evd.titleDate + "');";
         genericCommit(query);
     }
 
     /**
      *
      * @param name
      * @return
      * @throws IOException
      */
     public List<EventDate> getDatesOfOrgEvent(String name) throws IOException {
         List<EventDate> output = new LinkedList<EventDate>();
 
         String query = "SELECT d.title AS title, d.start_date AS start, " +
                             "d.end_date AS end" +
                 "   FROM events AS e, dates_event AS d" +
                 "   WHERE e.name = '" + name + "'" +
                 "       AND e.id_event = d.id_event";
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
                 while (rs.next()) {
                     output.add(new EventDate(rs.getString("title"),
                             rs.getTimestamp("start").getTime(),
                             rs.getTimestamp("end").getTime()));
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
         return output;
     }
 
     /**
      *
      * @return
      * @throws IOException
      */
     public List<OrganizedEvent> getOrganizedEvents() throws IOException {
         List<OrganizedEvent> output = new LinkedList<OrganizedEvent>();
 
         String query = "SELECT e.name AS name, d.title AS title, " +
                             "d.start_date AS start, d.end_date AS end" +
                 "   FROM events AS e, dates_event AS d" +
                 "   WHERE e.name <> 'other'" +
                 "       AND e.id_event = d.id_event";
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
                 if (rs.next()) {
                     OrganizedEvent ev = new OrganizedEvent(rs.getString("name"));
                     ev.datesList.add(new EventDate(rs.getString("title"),
                             rs.getTimestamp("start").getTime(),
                             rs.getTimestamp("end").getTime()));
                     while (rs.next()) {
                         if (ev.name.equals(rs.getString("name"))) {
                             ev.datesList.add(new EventDate(rs.getString("title"),
                                     rs.getTimestamp("start").getTime(),
                                     rs.getTimestamp("end").getTime()));
                         } else {
                             output.add(ev);
                             ev = new OrganizedEvent(rs.getString("name"));
                             ev.datesList.add(new EventDate(rs.getString("title"),
                                     rs.getTimestamp("start").getTime(),
                                     rs.getTimestamp("end").getTime()));
                         }
                     }
                     output.add(ev);
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
         return output;
     }
     
     //-----------------//
 
     /**
      * 
      * @param order
      * @throws IOException
      */
     public void addNewOrder( Order order) throws IOException {
 
         int idCassiere = getIdCassiereByUsername(order.getUsername());
         int idOrder = addOrderToOrdersTable(order, idCassiere);
 
         for (EntrySingleArticle entry : order.getListaBeni()) {
 
             int idArticle; // if trusted is > 0, otherwise < 0
 //            if (idCassiere > 0) {
                 idArticle = entry.article.getId();
 //            } else {
 //                idArticle = getIdArticleByName(entry.article.getName());
 //                idCassiere = - idCassiere;
 //            }
             try {
                 Statement st = db.createStatement();
                 try {
 
                     String addEntry =
                         "INSERT INTO articles_in_order (id_article, id_order, "+
                             "num_tot )" +
                         "VALUES ('" + idArticle + "', '" +
                             idOrder + "', '" +
                             entry.numTot + "' )";
                     st.execute(addEntry);
 
                     if (entry.article.hasOptions()) {
                         ResultSet key = st.executeQuery("SELECT " +
                                 "currval('articles_in_order_id_art_in_ord_seq');");
                         key.next();
                         int idArtInOrd = key.getInt("currval");
 
                         EntrySingleArticleWithOption entryOpts =
                                 (EntrySingleArticleWithOption)entry;
                         List<EntrySingleOption> opts = entryOpts.numPartial;
 
                         String addOpt =
                             "INSERT INTO opts_of_article_in_order " +
                                 "(id_art_in_ord, id_option, num_parz )" +
                             "VALUES ";
                         Statement optionSt = db.createStatement();
 
                         for (Iterator<EntrySingleOption> iter = opts.iterator();
                                 iter.hasNext();)
                         {
                             EntrySingleOption option = iter.next();
                             ResultSet optRs = optionSt.executeQuery(
                                     "SELECT id_option" +
                                     "   FROM options" +
                                     "   WHERE name = '"+option.optionName+"'" +
                                     "       AND id_article = '" + idArticle +
                                     "';");
                             optRs.next();
 
                             addOpt += "('" + idArtInOrd +
                                     "', '" + optRs.getInt("id_option") +
                                     "', '" + option.numPartial + "')" +
                                     (iter.hasNext() ? "," : ";");
                             optRs.close();
                         }
 
                         optionSt.executeUpdate(addOpt);
                         optionSt.close();
                     }
                 } catch (SQLException ex) {
                     logger.error("Errore con le commit", ex);
                     throw new IOException(ex);
                 } finally {
                     st.close();
                 }
             } catch (SQLException ex) {
                 logger.error("Errore nella comunciazione col DB", ex);
                 throw new IOException(ex);
             }
         }
     }
 
     /**
      * 
      * @param order 
      *
      * @throws IOException
      */
     public void delLastOrder(Order order) throws IOException {
         final String timestamp = new Timestamp(order.getData().getTime()).toString();
 
         String cassiereOrderQuery =
                 "SELECT t2.id_order" +
                 "   FROM cassieres AS t1, orders AS t2" +
                 "   WHERE t1.id_cassiere = t2.id_cassiere" +
                 "       AND t1.username = '" + order.getUsername() + "'" +
                 "       AND t2.time_order = '" + timestamp + "'" +
                 "   ORDER BY t2.time_order DESC";
         String deleteQuery = "DELETE FROM orders" +
                 "   WHERE id_order = (" + cassiereOrderQuery + ");";
         
         genericCommit(deleteQuery);
     }
     
     //-------------------//
     // Utility functions
     //-------------------//
 
     /**
      * This method just commits the passed query to the DB and then changes the
      * status of the "enabled" field, but if something goes wrong it fails
      * gracefully reporting what happened precisely and safely.
      * (closing what is needed closed)
      * 
      * @param query The query to commit
      * @param enable new value of the enabled field.
      *
      * @throws IOException
      */
     private void genericEnabler(String query, boolean enable) throws IOException {
         try {
             Statement st = db.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                               ResultSet.CONCUR_UPDATABLE);
             try {
                 ResultSet rs = st.executeQuery(query);
                 rs.next();
                 rs.updateBoolean("enabled", enable);
                 rs.updateRow();
                 rs.close();
             } catch (SQLException ex) {
                 logger.error("Errore con query: " + query + " o con update", ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
             st.close();
         } catch (SQLException ex) {
             logger.error("Errore", ex);
             throw new IOException(ex);
         }
     }
 
     /**
      * This method just commits the passed query to the DB, but if something
      * goes wrong it fails gracefully reporting what happened precisely and
      * safely. (closing what is needed closed)
      * 
      * @param query The query to commit.
      *
      * @throws IOException
      */
     private void genericCommit(String query) throws IOException {
         try {
             Statement stIns = db.createStatement();
             try {
                 stIns.executeUpdate(query);
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 stIns.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nella comunicazione col DB", ex);
             throw new IOException(ex);
         }
     }
 
     /**
      * This method safely extracts the Options for a precise Article identified
      * by the id. If it fails, it does it gracefully.
      *
      * @param art_id Id of the Article
      *
      * @return the list of the Options for the Article selected
      *
      * @throws IOException
      */
     private List<String> getOptionsByArticleID(int art_id) throws IOException {
         List<String> options = new LinkedList<String>();
         String queryOpts =  "SELECT name" +
                             "   FROM options" +
                             "   WHERE id_article = '" + art_id + "';";
         try {
             Statement stOpts = db.createStatement();
 
             try {
                 ResultSet rsOpts = stOpts.executeQuery(queryOpts);
                 while (rsOpts.next()) {
                     options.add(rsOpts.getString("name"));
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + queryOpts, ex);
                 throw new IOException(ex);
             } finally {
                 stOpts.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nella counicazione col DB", ex);
             throw new IOException(ex);
         }
         return options;
     }
 
     /**
      *
      * @param username
      * @return
      * @throws IOException
      */
     private int getIdCassiereByUsername(String username) throws IOException {
 
         String query =  "SELECT id_cassiere, enabled" +
                             "   FROM cassieres" +
                             "   WHERE username = '" + username + "'";
         int idCassiere;
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 if (rs.next() && rs.getBoolean("enabled")) {
                     idCassiere = rs.getInt("id_cassiere");
 //                    if (!rs.getBoolean("trusted")) {
 //                        idCassiere = - idCassiere;
 //                    }
                 } else {
                     rs.close();
                     st.close();
                     throw new IOException("The cassiere is not on the list, " +
                             "or is disabled");
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
         return idCassiere;
     }
 
     /**
      * 
      * @param name
      * @return
      * @throws IOException
      */
     private int getIdArticleByName(String name) throws IOException {
 
         String query =  "SELECT id_article, enabled" +
                             "   FROM articles" +
                             "   WHERE name = '" + name + "'";
         int idArticle;
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
 
                 if (rs.next() && rs.getBoolean("enabled")) {
                     idArticle = rs.getInt("id_article");
                 } else {
                     rs.close();
                     st.close();
                     throw new IOException("The article is not on the list, " +
                             "or is disabled");
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
         return idArticle;
     }
 
     /**
      *
      * @param order 
      * @return
      * @throws IOException
      */
     private int addOrderToOrdersTable(Order order, int idCassiere) throws IOException {
         
         final String timestamp = new Timestamp(order.getData().getTime()).toString();
         int idDateEvent = 1;
 
         // Add it to the tabe of events
         String query = "SELECT id_date_event " +
                 "   FROM dates_event" +
                 "   WHERE start_date < '" + timestamp + "'" +
                 "       AND end_date > '" + timestamp + "'";
         try {
             Statement st = db.createStatement();
             try {
                 ResultSet rs = st.executeQuery(query);
                 if (rs.next()) {
                     idDateEvent = rs.getInt("id_date_event");
                 }
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
 
         // and the order to the table of orders
         query = "INSERT INTO orders (time_order, id_cassiere, " +
                     "price_tot, hostname, id_date_event)" +
                 "VALUES ('" + timestamp + "', '" +
                     idCassiere + "', '" +
                     order.getTotalPrice() + "', '" +
                     order.getHostname() + "', '" + idDateEvent + "' )";
         int idOrder;
         try {
             Statement st = db.createStatement();
             try {
                 st.execute(query);
                 ResultSet key = st.executeQuery(
                         "SELECT currval( 'orders_id_order_seq' );");
                 key.next();
                 idOrder = key.getInt("currval");
             } catch (SQLException ex) {
                 logger.error("Errore con la query: " + query, ex);
                 throw new IOException(ex);
             } finally {
                 st.close();
             }
         } catch (SQLException ex) {
             logger.error("Errore nel connettermi al DB", ex);
             throw new IOException(ex);
         }
         return idOrder;
     }
 }
