 package jpaoletti.jpm.hibernate.core.monitor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import jpaoletti.jpm.core.monitor.MonitorLine;
 import jpaoletti.jpm.core.monitor.MonitorSource;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 
 /**
  * Todo
  */
 public class SQLMonitorSource extends MonitorSource {
 
     private String query;
     private String lastLineQuery;
     private Integer idColumn;
 
     @Override
     public void init() {
         setQuery(getConfig("query"));
         setLastLineQuery(getConfig("last-line-query"));
         setIdColumn(Integer.parseInt(getConfig("id-column", "0")));
     }
 
     @Override
     public List<MonitorLine> getLinesFrom(Object actual) throws Exception {
         final List<MonitorLine> result = new ArrayList<MonitorLine>();
         String sql;
         if (actual == null) {
             sql = getQuery().trim();
         } else {
             sql = getLastLineQuery().trim();
         }
         sql = sql.replaceAll("\\$actual", (actual == null) ? "" : actual.toString());
         getPersistenceManager().init(getPersistenceManager().newConnection());
         try {
             final SQLQuery c = ((Session) getPersistenceManager().getConnection()).createSQLQuery(sql);
             final List<?> l = c.list();
             for (Iterator<?> iterator = l.iterator(); iterator.hasNext();) {
                 final Object item = iterator.next();
                 if (item instanceof Object[]) {
                     final Object[] objects = (Object[]) item;
                    result.add(new MonitorLine(objects[getIdColumn()], objects));
                 } else {
                     final Object[] objects = {item};
                    result.add(new MonitorLine(item, objects));
                 }
             }
         } finally {
             getPersistenceManager().finish(null);
         }
         return result;
     }
 
     @Override
    public List<MonitorLine> getLastLine(Integer count) throws Exception {
        final List<MonitorLine> result = new ArrayList<MonitorLine>();
         getPersistenceManager().init(getPersistenceManager().newConnection());
         try {
             final SQLQuery c = ((Session) getPersistenceManager().getConnection()).createSQLQuery(getLastLineQuery().trim());
             c.setMaxResults(1);
             final Object item = c.uniqueResult();
             if (item instanceof Object[]) {
                 final Object[] objects = (Object[]) item;
                result.add(new MonitorLine(objects[getIdColumn()], objects));
             } else {
                 final Object[] objects = {item};
                result.add(new MonitorLine(item, objects));
             }
         } finally {
             getPersistenceManager().finish(null);
         }
         return result;
     }
 
     /**
      * @param query the query to set
      */
     public void setQuery(String query) {
         this.query = query;
     }
 
     /**
      * @return the query
      */
     public String getQuery() {
         return query;
     }
 
     /**
      * @param lastLineQuery the lastLineQuery to set
      */
     public void setLastLineQuery(String lastLineQuery) {
         this.lastLineQuery = lastLineQuery;
     }
 
     /**
      * @return the lastLineQuery
      */
     public String getLastLineQuery() {
         return lastLineQuery;
     }
 
     /**
      * @param idColumn the idColumn to set
      */
     public void setIdColumn(Integer idColumn) {
         this.idColumn = idColumn;
     }
 
     /**
      * @return the idColumn
      */
     public Integer getIdColumn() {
         return idColumn;
     }
 }
