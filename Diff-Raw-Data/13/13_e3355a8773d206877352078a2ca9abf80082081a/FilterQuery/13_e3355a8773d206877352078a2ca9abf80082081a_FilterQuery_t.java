 package util;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.persistence.Query;
 
import org.apache.commons.lang.StringUtils;
 import play.db.jpa.JPA;
 import play.libs.F;

 
 /**
  * Filter query builder. This was quickly hacked together, and has a limited functionality.
  * The Hibernate Criteria API would have been nice to use but it does not support working with collections.
  * <p/>
  * TODO screen this for SQL injection!
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class FilterQuery {
 
 
     private final Class<?> type;
     private Map<F.Tuple<String, String>, Object> filters = new HashMap<F.Tuple<String, String>, Object>();
     private Map<F.Tuple<String, String>, Object> additionalWhere = new HashMap<F.Tuple<String, String>, Object>();
     private Map<String, String> joins = new HashMap<String, String>();
     private boolean distinct;
 
     public FilterQuery(Class<?> type) {
         this.type = type;
     }
 
     public void addWhere(String condition, String paramName, Object value) {
         additionalWhere.put(new F.Tuple<String, String>(condition, paramName), value);
     }
 
     public void setDistinct(boolean distinct) {
         this.distinct = distinct;
     }
 
     public void addJoin(String field, String on, String label) {
         joins.put(label, on + "." + field);
     }
 
    private String afterWhere = "";
 
     public void addAfterWhere(String s) {
        if(StringUtils.isNotEmpty(afterWhere)) {
             afterWhere += " ";
         }
         afterWhere += s;
     }
 
     public void addFilter(String condition, String operation, Object value) {
         F.Tuple<String, String> t = new F.Tuple<String, String>(condition, operation);
         filters.put(t, value);
     }
 
     public Query build() {
         String q = "select " + (distinct ? "distinct" : "") + " o from " + type.getSimpleName() + " o";
         if (joins.size() > 0) {
             for (String label : joins.keySet()) {
                 q += " join " + joins.get(label) + " " + label;
             }
         }
         q += " where";
 
         for (Iterator<F.Tuple<String, String>> it = filters.keySet().iterator(); it.hasNext();) {
             F.Tuple<String, String> next = (F.Tuple<String, String>) it.next();
 
             q += " o." + next._1 + " " + next._2 + " " + " (:" + validParamName(next._1) + ")";
             if (it.hasNext() || additionalWhere.size() > 0) {
                 q += " and";
             }
         }
         for (Iterator<F.Tuple<String, String>> aw = additionalWhere.keySet().iterator(); aw.hasNext();) {
             F.Tuple<String, String> next = (F.Tuple<String, String>) aw.next();
             q += " " + next._1;
             if (aw.hasNext()) {
                 q += " and";
             }
         }
        if (StringUtils.isNotEmpty(afterWhere)) {
             q += " " + afterWhere;
         }
 
         Query query = JPA.em().createQuery(q);
         for (F.Tuple<String, String> t : filters.keySet()) {
             query.setParameter(validParamName(t._1), filters.get(t));
         }
         for (F.Tuple<String, String> t : additionalWhere.keySet()) {
             query.setParameter(t._2, additionalWhere.get(t));
         }
 
         return query;
     }
 
     private String validParamName(String c) {
         return c.replaceAll("\\.", "_").replaceAll(" ", "_");
     }
 
 }
