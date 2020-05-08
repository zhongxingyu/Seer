 package assistant;
 
 import com.fasterxml.jackson.databind.AnnotationIntrospector;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
 import com.thetransactioncompany.jsonrpc2.server.MessageContext;
 import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
 import javafx.util.Pair;
 import net.minidev.json.JSONObject;
 import org.hibernate.ejb.HibernatePersistence;
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.spi.PersistenceProvider;
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-25
  * Time: 14:26
  */
 public abstract class SharedHandler implements RequestHandler{
     private EntityManagerFactory entityManagerFactory; //TODO should maybe be created only once. Like in Engine.
     public EntityManager em;
 
     public int userId;
     public String method;
     public JSONObject responseParams;
     public JSONRPC2Response response;
 
     /**
      * Creates the entity factory that produces the entity managers
      */
     public SharedHandler(){
         PersistenceProvider persistenceProvider = new HibernatePersistence();
         entityManagerFactory = persistenceProvider.createEntityManagerFactory(Constants.Settings.PERSISTENCE_NAME, new HashMap());
     }
 
     /**
      *
      * @param serializedObject
      * @return
      * @throws IOException
      * @throws ClassNotFoundException
      */
     //TODO Check if classType is not needed to be used in some way
     protected static <T>  T deSerialize(java.lang.Class<T> classType, String serializedObject) throws IOException,
             ClassNotFoundException {
         BASE64Decoder base64Decoder = new BASE64Decoder();
         byte [] data = base64Decoder.decodeBuffer(serializedObject);
         ObjectInputStream ois = new ObjectInputStream(
                 new ByteArrayInputStream( data ) );
         Object o = ois.readObject();
         ois.close();
 
         return (T)o;
     }
 
     /**
      *
      * @param object
      * @return
      * @throws IOException
      */
     protected static String serialize(Serializable object) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream( baos );
         oos.writeObject( object );
         oos.close();
         BASE64Encoder base64Encoder =  new BASE64Encoder();
         return new String( base64Encoder.encode(baos.toByteArray()) );
     }
 
 
     @Override
     public abstract String[] handledRequests();
 
     @Override
     public final JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) { //TODO find out what MessageContext is exactly and how we can use it
 
         try{
             initializeData(jsonrpc2Request);
             process(jsonrpc2Request.getNamedParams());
             validateResponse();
             return response;
         }catch (JSONRPC2Error e){
             return new JSONRPC2Response(e, userId);
         }catch (Exception e){
             return null; //TODO:
         }
 
     }
 
     private static void validateResponse() {
         //TODO: check that value and userId is present in the response. And that value is in "result"
     }
 
     private void initializeData(JSONRPC2Request jsonrpc2Request) throws Exception {
         em = entityManagerFactory.createEntityManager();
         responseParams = new JSONObject();
         method = jsonrpc2Request.getMethod();
 
         try{
             userId = Integer.parseInt(jsonrpc2Request.getID().toString());
         }catch(NumberFormatException e){
             throwJSONRPC2Error(JSONRPC2Error.INVALID_REQUEST, Constants.Errors.BAD_ID);
         }
         response = new JSONRPC2Response(responseParams, userId);
     }
 
     protected abstract void process(Map<String, Object> jsonrpc2Params) throws Exception;
 
     public void throwJSONRPC2Error(JSONRPC2Error e, String... msgToAppend) throws Exception {
         for(String msg: msgToAppend){
             e.appendMessage( " || " + msg);
         }
         throw e;
     }
 
     /**
      * Makes sure that certain parameters are present in the request
      * @param jsonrpc2Params    The params in the request
      * @param keys              The keys that must be present
      * @return                  c
      * @throws Exception        If the map does not contain a key or the key is null
      */
     public Map<String, Object> getParams(Map<String, Object> jsonrpc2Params, String... keys) throws Exception {
         Pair<String, Boolean>[] np = new Pair[keys.length];
         for(int i = 0; i < keys.length; i ++){
             String key = keys[i];
             np[i] = new Pair<String, Boolean>(key, false);
         }
         return getParams(jsonrpc2Params, np);
     }
 
 
     /**
      * Allows certain params to be null while retrieving them
      * @param jsonrpc2Params    The params in the request
      * @param keypairs          Contains the key and a boolean stating if the key can be null or not
      * @return                  The params in the request
      * @throws Exception        If the map does not contain a key or the key is null. If the key can´t be null that is
      */
     public Map<String, Object> getParams(Map<String, Object> jsonrpc2Params, Pair<String, Boolean>... keypairs) throws Exception {
         Map<String, Object> returnParams= new HashMap<String, Object>();
         Object value;
         for(Pair<String, Boolean> pair: keypairs){
             value = jsonrpc2Params.get(pair.getKey());
 
             if(value == null && !pair.getValue()){
                 throwJSONRPC2Error(JSONRPC2Error.INVALID_PARAMS, Constants.Errors.PARAM_NOT_FOUND, pair.getKey());
             }
             returnParams.put(pair.getKey(), value);
         }
         return returnParams;
     }
 
 
     protected void persistObjects(Object... objects){
         em.getTransaction().begin();
         for(Object o: objects){
             em.persist(o);
         }
         em.getTransaction().commit(); //TODO: Check if it is important to close EntityManager at some point
     }
 }
 
 
