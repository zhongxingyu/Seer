 package assistant;
 
 import assistant.pair.NullableExtendedParam;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
 import com.thetransactioncompany.jsonrpc2.server.MessageContext;
 import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
 import core.Engine;
 import models.Reply;
 import net.minidev.json.JSONObject;
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.io.*;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-25
  * Time: 14:26
  */
 
 /**
  * Shared handler is responsible for containing variables and methods shared between all different handlers.
  * The handlers should use the public fields and NEVER create them on their own.
  */
 public abstract class SharedHandler implements RequestHandler{
 
     // The manager that handles SQL input and output. Always use this manager.
     public EntityManager em;
     // This id is always retrieved from the request from the client.
     public int accountId;
     // The method that the request wants to reach.
     public String method;
     // To be filled with information that the response will be filled with.
     public JSONObject responseParams;
 
 
     // Will automatically be filled with the responseParams before being sent to the client.
     private JSONRPC2Response response;
 
 
 
     /**
      * Must be overridden by handlers. Contains a array of accepted methods from the request.
      * @return
      */
     @Override
     public abstract String[] handledRequests();
 
     /**
      * Can not be overridden by handlers! Must be final! Handles the request and sets the public fields.
      * @param jsonrpc2Request   The request from the client.
      * @param messageContext    Not sure what to do with this.... TODO Find out...
      * @return                  The response to be sent back to the client. Can contain a error if something went wrong.
      */
     @Override
     public final JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) { //TODO find out what MessageContext is exactly and how we can use it
 
         try{
             initializeData(jsonrpc2Request);
             //This is where the handler gets to
             process(jsonrpc2Request.getNamedParams());
             validateResponse();
             return response;
         }catch (JSONRPC2Error e){
             return new JSONRPC2Response(e, accountId);
         }catch (Exception e){
             return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR.appendMessage(" || Exception: " + e.toString()), accountId);
         }
 
     }
 
     private static void validateResponse() {
         //TODO: check that value and accountId is present in the response. And that value is in "result"
     }
 
     private void initializeData(JSONRPC2Request jsonrpc2Request) throws Exception {
         em = Engine.entityManagerFactory.createEntityManager();
         responseParams = new JSONObject();
         method = jsonrpc2Request.getMethod();
 
         try{
             accountId = Integer.parseInt(jsonrpc2Request.getID().toString());
         }catch(NumberFormatException e){
             throwJSONRPC2Error(JSONRPC2Error.INVALID_REQUEST, Constants.Errors.BAD_ID);
         }
         response = new JSONRPC2Response(responseParams, accountId);
     }
 
     /**
      * Must be overridden by handlers. This is where the handlers extending this handlers takes over.
      * Any exception will be caught and do not need to be handled by the handlers.
      * @param jsonrpc2Params    The parameters from the client.
      * @throws Exception
      */
     protected abstract void process(Map<String, Object> jsonrpc2Params) throws Exception;
 
     /**
      * Throws and JSONRPC2Error with messages to append
      * @param e             The error to throw
      * @param msgToAppend   The messages to append to the error
      * @throws Exception
      */
     public void throwJSONRPC2Error(JSONRPC2Error e, String... msgToAppend) throws Exception {
         for(String msg: msgToAppend){
             e.appendMessage( " || " + msg);
         }
         throw e;
     }
 
     /**
      * Makes sure that given parameters are present in the request, no other parameters are allowed!
      * @param jsonrpc2Params    The parameters in the request.
      * @param params            The parameters that must be present in the request.
      * @return                  If all checks out, all the parameters are returned.
      * @throws Exception        If the map does not contain a key or the key is null. Also if there are more parameters then the allowed.
      */
     public Map<String, Object> getParams(Map<String, Object> jsonrpc2Params, String... params) throws Exception {
         NullableExtendedParam[] np = new NullableExtendedParam[params.length];
         for(int i = 0; i < params.length; i ++){
             String param = params[i];
             np[i] = new NullableExtendedParam(param, false);
         }
         return getParams(jsonrpc2Params, np);
     }
 
 
     /**
      * Same as getParams(Map<>, String...) but allows certain params to be null or not present.
      * @param jsonrpc2Params    The params in the request
      * @param params            Contains the param and a boolean stating if the parameter can be null or not
      * @return                  The params in the request that are present. The ones that could be null will be null.
      * @throws Exception        If the map does not contain a key or the key is null, If the key can´t be null that is. Also if there are more params then the allowed.
      */
     //TODO: Send what class each object should so we do not need to cast the objects retrieved
     public Map<String, Object> getParams(Map<String, Object> jsonrpc2Params, NullableExtendedParam... params) throws Exception {
         Map<String, Object> returnParams= new HashMap<String, Object>();
         Object value;
         String param;
         boolean allowedToBeNull;
         for(NullableExtendedParam extendedParam: params){
             param = extendedParam.getParam();
             value = jsonrpc2Params.get(param);
             allowedToBeNull = extendedParam.isNullable();
             if(value == null && !allowedToBeNull){
                 throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_NOT_FOUND, param);
             }
             if(Long.class.isInstance(value)){
                 value = (int)(long)(Long)value; //Needed to avoid using Long
             }
             returnParams.put(param, value);
             jsonrpc2Params.remove(param);
         }
         if(!jsonrpc2Params.isEmpty()){
             String debugInfo = "Bad params >> ";
             for(Map.Entry entry: jsonrpc2Params.entrySet()){
                 debugInfo += "Key: " + entry.getKey() + "  Value: " + entry.getValue() + " | ";
             }
             throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_NOT_ALLOWED, debugInfo);
         }
         return returnParams;
     }
 
 
     /**
      * Persists objects to the database and commits them. Can throw SQL and persisting errors that will be caught.
      * In simple terms: Takes the objects, converts them, and tries to send them into the database.
      * @param objects   The objects to be persisted
      */
     protected void persistObjects(Object... objects){
         em.getTransaction().begin();
         for(Object o: objects){
             em.persist(o);
         }
         em.getTransaction().commit(); //TODO: Check if it is important to close EntityManager at some point
     }
 
     /**
      * Removes objects from the database and commits them.
      * In simple terms: Takes the objects, removes them, and tries to remove them into the database.
      * @param objects   The objects to be persisted
      */
     protected void removeObjects(Object... objects){
         em.getTransaction().begin();
         for(Object o: objects){
             em.remove(o);
         }
         em.getTransaction().commit(); //TODO: Check if it is important to close EntityManager at some point
     }
 
     /**
      * Kind of the opposite to persisting. Will update the given objects according to latest updates in the database.
      * @param objects   The objects that will be updated.
      */
     protected void refreshObjects(Object... objects) {
         em.getTransaction().begin();
         for(Object o: objects){
             em.refresh(o);
         }
 
         em.getTransaction().commit(); //TODO: Check if it is important to close EntityManager at some point
     }
 
     /**
      * Handles sorting of replies.
      */
     protected enum ReplyComparator implements Comparator<Reply> {
 
         TIME_SORT {
             public int compare(Reply r1, Reply r2) {
                 return r1.getTime().compareTo(r2.getTime());
             }},
 
         VOTE_SORT {
             public int compare(Reply r1, Reply r2) {
                 int nrThumbsUp1 = r1.getThumbsUp().size();
                 int nrThumbsUp2 = r2.getThumbsUp().size();
                 if (nrThumbsUp1 < nrThumbsUp2){
                     return 1;
                 } else if (nrThumbsUp1 > nrThumbsUp2){
                     return -1;
                 } else{
                     return 0;
                 }
             }};
 
         /**
          * Get the replies in reversed order.
          * Example use: ReplyComparator.descending(ReplyComparator.getComparator(ReplyComparator.VOTE_SORT, ReplyComparator.TIME_SORT));
          * @param comparator
          * @return  The comparator to be used for sorting lists/arrays containing Reply objects.
          */
         public static Comparator<Reply> descending(final Comparator<Reply> comparator) {
             return new Comparator<Reply>() {
                 public int compare(Reply r1, Reply r2) {
                     return -1 * comparator.compare(r1, r2);
                 }
             };
         }
 
         /**
          * Get the replies in reversed order.
          * Example use: ReplyComparator.getComparator(ReplyComparator.VOTE_SORT, ReplyComparator.TIME_SORT);
          * @param multipleOptions   Sorts after the first option, then the second and so on.
          * @return                  The comparator to be used for sorting lists/arrays containing Reply objects.
          */
         public static Comparator<Reply> getComparator(final ReplyComparator... multipleOptions) {
             return new Comparator<Reply>() {
                 public int compare(Reply r1, Reply r2) {
                     for (ReplyComparator option : multipleOptions) {
                         int result = option.compare(r1, r2);
                         if (result != 0) {
                             return result;
                         }
                     }
                     return 0;
                 }
             };
         }
     }
 
     /**
      * Gets all the columns by the given entity class.
      * @param classType The class (entity) to load the columns from.
      * @return  A list of objects by the given class.
      */
     public  <T>  List<T>  getAllColumns(java.lang.Class<T> classType ) {
         CriteriaBuilder builder = em.getCriteriaBuilder();
         CriteriaQuery<T> query = builder.createQuery(classType);
         Root<T> variableRoot = query.from(classType);
         query.select(variableRoot);
 
        return em.createQuery(query).getResultList();
     }
 
     protected Timestamp getCurrentTime(){
         return new Timestamp(new Date().getTime());
     }
 }
 
 
