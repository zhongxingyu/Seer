 package ctd.services.internal;
 
 import com.skaringa.javaxml.ObjectTransformer;
 import com.skaringa.javaxml.ObjectTransformerFactory;
 import ctd.services.exceptions.Exception307TemporaryRedirect;
 import ctd.services.exceptions.Exception500InternalServerError;
 import ctd.services.getTicket;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 /**
  * This class is used to generate the HTML formatted rows of the overview page
  * @author Tjeerd van Dijk
  * @author Taco Steemers
  */
 public class Overview {
 
     private String strOffset;
 
     String strSessionToken = "";
 
     public String Overview() throws Exception307TemporaryRedirect {
         String strRet = "";
 
         //Check if the user is logged in
         GscfService objGSCFService = new GscfService();
         String[] strGSCFRespons = new String[2];
         ResourceBundle res = ResourceBundle.getBundle("settings");
         try {
             strGSCFRespons = objGSCFService.callGSCF(getSessionToken(),"isUser",null);
         } catch (Exception500InternalServerError e) {
             Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "OVERVIEW ERROR (isUser): "+e.getError());
         }
 
         if(!objGSCFService.isUser(strGSCFRespons[1])) {
             String urlAuthRemote = objGSCFService.urlAuthRemote(getSessionToken(), res.getString("ctd.moduleURL")+"/index.jsp?p=overview");
             throw new Exception307TemporaryRedirect(urlAuthRemote);
         }
 
         // offset is not yet implemented in overview.jsp, but it can be used to
         // split the overview into multiple pages.
         // Now only the first 20 results are given
         if(getOffset()!=null) {
             strOffset = " LIMIT "+strOffset +",20";
         } else {
             strOffset = "";
         }
         strOffset = "";
 
 
         LinkedList lstGetAssays = objGSCFService.callGSCF2(getSessionToken(),"getAssays",null);
         String strStudyQuery = "";
         String strStudyCall = "";
         Map mapAssayNames = new HashMap();
                 
         for(int i=0; i<lstGetAssays.size(); i++) {
             HashMap<String, String> objMap = (HashMap) lstGetAssays.get(i);
             
             if(!strStudyQuery.equals("")) strStudyQuery += ",";
             strStudyQuery += "'"+objMap.get("parentStudyToken")+"'";
 
             if(!strStudyCall.equals("")) strStudyCall += "&studyToken=";
             strStudyCall += objMap.get("parentStudyToken");
 
            mapAssayNames.put(objMap.get("assayToken"), objMap.get("name"));
         }
 
         HashMap<String, String> objParam = new HashMap();
         objParam.put("studyToken",strStudyCall);
         LinkedList lstGetStudies = objGSCFService.callGSCF2(getSessionToken(),"getStudies",objParam);
 
         Map mapStudyNames = new HashMap();
         for(int i=0; i<lstGetStudies.size(); i++) {
             HashMap<String, String> objMap = (HashMap) lstGetStudies.get(i);
             mapStudyNames.put(objMap.get("studyToken"), objMap.get("title"));
         }
 
         if(!strStudyQuery.equals("")) strStudyQuery = " WHERE a.study_token IN(" + strStudyQuery + ") ";
 
         //open hibernate connection
         SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
         Session session = sessionFactory.openSession();
 
         // Select study_tokens, assay_tokens, the number of rows in study_sample_assay
         // with these 2 keys and the number of rows in expression with these keys
         String strQuery = "SELECT a.X_REF, a.study_token, COUNT(a.id),"
                                     +" (SELECT 1 FROM expression b "
                                     +" WHERE b.study_sample_assay_id=a.id "
                                     +" GROUP BY b.study_sample_assay_id) AS totaal "
                             +" FROM study_sample_assay a"
                             +" "+strStudyQuery
                             +" GROUP BY a.X_REF"
                             +" "+strOffset+"";
         //Logger.getLogger(getTicket.class.getName()).log(Level.SEVERE, "OVERVIEW QUERY: "+strQuery);
         SQLQuery sql = session.createSQLQuery(strQuery);
         Iterator it2 = sql.list().iterator();
         int iRownr = 1;
 
         // the gscf url in order to be able to refer to study and assay details
         String strGscfHome = res.getString("gscf.baseURL");
         LinkedList<String> lstRijen = new LinkedList<String>();
         while (it2.hasNext()) {
             Object[] data = (Object[]) it2.next();
 
             // strClass is used to give even and odd rows a different background color
             String strClass = "odd";
             if(iRownr%2==0) strClass = "even";
 
             String strExprCount = "0";
             if(data[3]!=null) {
                 strExprCount = data[3].toString();
             }
 
             iRownr++;
             
             String strLine = (String)mapStudyNames.get((String)data[1])+(String)mapAssayNames.get((String)data[0])+"!!SEP!!<tr class=\""+strClass+"\">\n";
             strLine += "\t<td class=\"tdoverview\"><a href='"+strGscfHome+"/assay/showByToken/"+(String)data[0]+"'>"+mapAssayNames.get((String)data[0])+"</a></td>\n";
             strLine += "\t<td class=\"tdoverview\"><a href='"+strGscfHome+"/study/showByToken/"+(String)data[1]+"'>"+mapStudyNames.get((String)data[1])+"</a></td>\n";
             strLine += "\t<td class=\"tdoverview\">"+data[2].toString()+" ("+strExprCount+")</td>\n";
             strLine += "</tr>\n";
             lstRijen.add(strLine);
         }
 
         for(int i=0; i<lstRijen.size(); i++) {
             String[] arrLine = lstRijen.get(i).split("!!SEP!!");
             strRet += arrLine[1];
         }
         
         session.close();
 
         return strRet;
     }
 
      /**
      * This function calls the constructor Overview() of this class and returns the result
      * @return a String containing the table rows
      */
     public String getContent() throws Exception307TemporaryRedirect {
         return this.Overview();
     }
 
     /**
      * @return the strOffset
      */
     public String getOffset() {
         return strOffset;
     }
 
     /**
      * @param strOffset the strOffset to set
      */
     public void setOffset(String strOffset) {
         this.strOffset = strOffset;
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
 }
