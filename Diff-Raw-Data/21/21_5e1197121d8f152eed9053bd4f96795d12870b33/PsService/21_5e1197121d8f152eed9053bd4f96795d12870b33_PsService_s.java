 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard.db.data_objects;
 import java.util.TreeMap;
 import java.util.Date;
 import javax.persistence.*;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 /**
  * An object describing perfSonar service
  * @author tomw
  */
 @Cacheable
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 public class PsService {
     
     // parameter names in JSON
     public static final String ID="id";
     public static final String TYPE="type";
     public static final String NAME="name";
     public static final String DESCRIPTION="description";
     public static final String PARAMETERS="parameters";
     public static final String CHECK_INTERVAL="checkInterval";
     public static final String RUNNING="running";
     public static final String PREV_CHECK_TIME="prevCheckTime";
     public static final String NEXT_CHECK_TIME="nextCheckTime";
     public static final String RUNNING_SINCE="runningSince";
     public static final String TIMEOUT="timeout";
     public static final String RESULT="result";
     
     @Id
     @GeneratedValue
     private int    id;
     private String type;
     private String name;
     private String description;
     @Lob
     private TreeMap<String,Object> parameters = new TreeMap<String,Object> ();
     private int checkInterval;
     private boolean running;
     
     @Temporal(TemporalType.TIMESTAMP)
     private Date prevCheckTime;
     @Temporal(TemporalType.TIMESTAMP)
     private Date nextCheckTime;
     @Temporal(TemporalType.TIMESTAMP)
     private Date runningSince;
     
     private int timeout;  
     @OneToOne(cascade=CascadeType.ALL)
     private PsServiceResult serviceResult;
     
     
 
     /**
      * create new service, assign unique id to it
      */
     public PsService(){
         
     }
 
     public int getCheckInterval() {
         return checkInterval;
     }
 
     public void setCheckInterval(int checkInterval) {
         this.checkInterval = checkInterval;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getId() {
         return id;
     }
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Date getNextCheckTime() {
         return nextCheckTime;
     }
 
     public void setNextCheckTime(Date nextCheckTime) {
         this.nextCheckTime = nextCheckTime;
     }
     
     /**
      * if the next scheduled check time happens before NOW() return true
      * else return false;
      * @return 
      */
     public boolean isDue(){
         if(getNextCheckTime().before(new Date())){
             return true;
         }else{
             return false;
         }
     }
 
     public TreeMap<String, Object> getParameters() {
         return parameters;
     }
 
     public void setParameters(TreeMap<String, Object> parameters) {
         this.parameters = parameters;
     }
     /**
      * set a value of parameter of name parameterName
      * @param parameterName
      * @param parameter 
      */
     public void setParameter(String parameterName, Object parameter){
         this.parameters.put(parameterName, parameter);
     }
     /**
      * return value of parameter parameterName
      * @param parameterName
      * @return 
      */
     public Object getParameter(String parameterName){
         return this.parameters.get(parameterName);
     }
     
     public Date getPrevCheckTime() {
         return prevCheckTime;
     }
 
     public void setPrevCheckTime(Date prevCheckTime) {
         this.prevCheckTime = prevCheckTime;
     }
 
     public PsServiceResult getResult() {
         return serviceResult;
     }
 
     public void setResult(PsServiceResult result) {
         this.serviceResult = result;
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void setRunning(boolean running) {
         this.running = running;
    }
 
     public Date getRunningSince() {
         return runningSince;
     }
 
     public void setRunningSince(Date runningSince) {
         this.runningSince = runningSince;
     }
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     @Override
     public String toString() {
         return "PsService{" + "id=" + id + ", type=" + type + ", name=" + name + '}';
     }
     
     
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final PsService other = (PsService) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
    
 
     
     
     
 }
