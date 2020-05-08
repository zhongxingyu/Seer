 package dk.statsbiblioteket.doms.central.connectors.fedora;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 3/15/11
  * Time: 10:25 AM
  * To change this template use File | Settings | File Templates.
  */
 public class FedoraRelation {
     private String subject;
 
     private String object;
    private String predicate;
 
    public FedoraRelation(String subject, String predicate, String object) {
 
         this.subject = subject;
        this.predicate = predicate;
         this.object = object;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public String getObject() {
         return object;
     }
 
    public String getPredicate() {
        return predicate;
     }
 }
