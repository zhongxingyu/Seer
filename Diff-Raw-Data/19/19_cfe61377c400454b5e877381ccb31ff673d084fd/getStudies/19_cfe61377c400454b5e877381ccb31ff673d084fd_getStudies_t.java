 package ctd.services;
 
 import com.skaringa.javaxml.DeserializerException;
 import com.skaringa.javaxml.NoImplementationException;
 import com.skaringa.javaxml.ObjectTransformer;
 import com.skaringa.javaxml.ObjectTransformerFactory;
 import ctd.services.exceptions.Exception400BadRequest;
 import ctd.services.exceptions.Exception401Unauthorized;
 import ctd.services.exceptions.Exception403Forbidden;
 import ctd.services.exceptions.Exception500InternalServerError;
 import ctd.services.internal.GscfService;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Tjeerd van Dijk
  * @author Taco Steemers
  */
 public class getStudies {
     private String strAssayToken;
     private String strSessionToken;
 
     /***
      * TODO: COMMENT
      * 
      * @return
      * @throws Exception400BadRequest
      * @throws Exception403Forbidden
      * @throws Exception500InternalServerError
      */
 
     public String getStudies() throws Exception400BadRequest, Exception403Forbidden, Exception500InternalServerError {
         String strReturn = "";
 
         // Check if the minimal parameters are set
         if(strSessionToken==null){
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getStudies(): strSessionToken==null");
             throw new Exception400BadRequest();
         }
 
         // Check if the provided sessionToken is valid
          GscfService objGSCFService = new GscfService();
         ResourceBundle res = ResourceBundle.getBundle("settings");
         String strConsumerVal = res.getString("ctd.consumerID");
         HashMap<String, String> restParams = new HashMap<String, String>();
         restParams.put("consumer", strConsumerVal);
         String[] strGSCFRespons = objGSCFService.callGSCF(strSessionToken,"isUser",restParams);
         if(!objGSCFService.isUser(strGSCFRespons[1])) {
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getStudies(): strSessionToken invalid: "+strSessionToken);
             throw new Exception403Forbidden();
         }
         //Logger.getLogger(getTicket.class.getName()).log(Level.INFO, "getStudies(): strSessionToken is valid: "+strSessionToken);
 
         strReturn = "<option value='none'>Select a study...</option>";
         objGSCFService = new GscfService();
         res = ResourceBundle.getBundle("settings");
         strGSCFRespons = objGSCFService.callGSCF(strSessionToken,"getStudies",restParams);
         //LinkedList lstStudies = new LinkedList();
         LinkedList lstGSCFResponse = new LinkedList();
 
         ObjectTransformer trans = null;
         try {
             trans = ObjectTransformerFactory.getInstance().getImplementation();
         } catch (NoImplementationException ex) {
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             lstGSCFResponse = (LinkedList) trans.deserializeFromJsonString(strGSCFRespons[1]);
         } catch (DeserializerException ex) {
             Logger.getLogger(getStudies.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         String[] arrOptions = new String[lstGSCFResponse.size()];
         for(int i = 0; i < lstGSCFResponse.size(); i++){
             HashMap<String, String> map = (HashMap<String, String>) lstGSCFResponse.get(i);
            if(map.get("code")==null) {
                map.put("code", "null");
            }
            arrOptions[i] = map.get("code").toLowerCase()+map.get("title").toLowerCase()+"!!SEP!!<option value="+map.get("studyToken")+">"+map.get("code")+" - "+map.get("title")+"</option>";
         }
         Arrays.sort(arrOptions);
         for(int i = 0; i < lstGSCFResponse.size(); i++){
             String[] arrSplit = arrOptions[i].split("!!SEP!!");
             strReturn += arrSplit[1];
         }
 
 
         return strReturn;
     }
     
     /**
      * @return the strSessionToken
      */
     public String getSessionToken() {
         return strSessionToken;
     }
 
     /**
      * @param strSessionToken the strSessionToken to set
      */
     public void setSessionToken(String strSessionToken) {
         this.strSessionToken = strSessionToken;
     }
 
     /**
      * @return the strAssayToken
      */
     public String getAssayToken() {
         return strAssayToken;
     }
 
     /**
      * @param strAssayToken the strAssayToken to set
      */
     public void setAssayToken(String assayToken) {
         this.strAssayToken = assayToken;
     }
 }
