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
  * @author Taco Steemers
  * @author Tjeerd van Dijk
  */
 public class getAssays {
     private String strStudyToken;
     private String strSessionToken;
 
     /***
      * This function calls GSCF in order to get a list of assayTokens that are
      * available for a certain study.
      * 
      * @return the list of assayTokens, HTML formatted as OPTION's
      *
      * @throws Exception400BadRequest thrown if the sessionToken or studyToken not is set
      * @throws Exception403Forbidden thrown if the sessionToken isn't valide
      * @throws Exception500InternalServerError thrown if something goes wrong
      */
 
     public String getAssays() throws Exception400BadRequest, Exception403Forbidden, Exception500InternalServerError {
         String strReturn = "";
 
         // Check if the minimal parameters are set
         if(strSessionToken==null){
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getAssays(): strSessionToken==null");
             throw new Exception400BadRequest();
         }
         if(strStudyToken==null){
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getAssays(): strStudyToken==null");
             throw new Exception400BadRequest();
         }
 
         // Check if the provided sessionToken is valid
         GscfService objGSCFService = new GscfService();
         String[] strGSCFRespons = objGSCFService.callGSCF(strSessionToken,"isUser",null);
         if(!objGSCFService.isUser(strGSCFRespons[1])) {
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "getAssays(): strSessionToken invalid: "+strSessionToken);
             throw new Exception403Forbidden();
         }
 
         strReturn = "<option value='none'>Select an assay...</option>";
         objGSCFService = new GscfService();
         HashMap<String, String> restParams = new HashMap<String, String>();
        restParams.put("studyToken", getStudyToken());
         strGSCFRespons = objGSCFService.callGSCF(strSessionToken,"getAssays",restParams);
 
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
             arrOptions[i] = map.get("name").toLowerCase()+"!!SEP!!<option value="+map.get("assayToken")+">"+map.get("name")+"</option>";
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
     public String getStudyToken() {
         return strStudyToken;
     }
 
     /**
      * @param studyToken the strStudyToken to set
      */
     public void setStudyToken(String studyToken) {
         this.strStudyToken = studyToken;
     }
 }
