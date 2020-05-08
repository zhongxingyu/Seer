 package org.makumba.parade.view.beans;
 
 import java.util.Iterator;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.model.Row;
 
 public class ParadeBean {
 
     protected Row row;
 
     public ParadeBean() {
         super();
     }
 
     public void setContext(String context) {
     
         Session s = null;
         try {
             s = InitServlet.getSessionFactory().openSession();
             Transaction tx = s.beginTransaction();
             row = (Row) s.createQuery("from Row r where r.rowname = :context").setString("context", context)
                     .uniqueResult();
             if (row == null) {
                 throw new RuntimeException("Could not find row " + context);
             }
             tx.commit();
     
         } finally {
             if (s != null)
                 s.close();
         }
     }
     
     protected String allowedOperations = new String();
 
     public String getAllowedAntOperations() {
         
         if(allowedOperations.length() == 0) {
             
             for (Iterator<String> iterator = ParadeProperties.getElements("ant.displayedOps").iterator(); iterator.hasNext();) {
                 String allowed = iterator.next();
                if(allowed != null && allowed != "null" && allowed.length() > 0) {
                    if(allowed.startsWith("#")) {
                        allowed = allowed.substring(1);
                    }
                     allowedOperations += "'"+allowed+"'";
                    
                    if(iterator.hasNext()) allowedOperations +=",";
                }
             }
         }
         return allowedOperations;
 
     
     }
 
 }
