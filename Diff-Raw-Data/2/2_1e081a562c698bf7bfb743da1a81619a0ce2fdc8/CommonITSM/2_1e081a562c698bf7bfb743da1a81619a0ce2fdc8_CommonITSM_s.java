 package scripts.itsm;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.trackstudio.app.TriggerManager;
 import com.trackstudio.app.adapter.AdapterManager;
 import com.trackstudio.app.session.SessionContext;
 import com.trackstudio.constants.CategoryConstants;
 import com.trackstudio.exception.GranException;
 import com.trackstudio.kernel.manager.KernelManager;
 import com.trackstudio.secured.SecuredPrstatusBean;
 import com.trackstudio.secured.SecuredTaskBean;
 import com.trackstudio.secured.SecuredUDFValueBean;
 
 /**
  * Common class for ITSM configuration
  */
 public class CommonITSM {
    private static Properties properties = null;
 
     {
         try {
             properties = new Properties();
             properties.load(new FileReader("itsm.properties"));
         } catch (IOException e) {
             properties = null;
 
         }
     }
    
     public static final String EMAIL_PATTERN = "электронная почта:\\s*\\\"?(\\S+\\s*\\S+[^\\\"])?\\\"?\\s+(<|&lt;)?(([-A-Za-z0-9!#$%&'*+/=?^_`{|}~]+(\\.[-A-Za-z0-9!#$%&'*+/=?^_`{|}~]+)*)@([A-Za-z0-9.]+))(&gt;|>)?\\r?\\n";
 	public static final String PHONE_PATTERN = "телефон:\\s*([0-9\\+\\s\\-\\(\\)]+)+\\r?\\n";
 	public static final String COMPANY_PATTERN = "компания:\\s*(\\S+[\\s\\S]*?)\\r?\\n";
 	
     protected String INCIDENT_PRODUCT_UDFID = "4028818212b7e87b0112be28559c0606";
     protected String PROBLEM_PRODUCT_UDFID = "ff80808112bf740e0112bf896aff00e0";
     protected String INCIDENT_CLIENT_UDFID2 = "ff808181341d98dd01341dea63bf0003"; // 11 права тут //ff8081812e6bb868012e6c5fe88205ae 10 права тут
     protected String INCIDENT_CLIENT_UDF = "Клиент";
     protected String INCIDENT_CLIENTLINK_UDF = "Ссылка на клиента";
     protected String INCIDENT_CLIENTLINK_UDFID = "ff8081812e6bb868012e6c5fe88205ae"; 
     
     protected String INCIDENT_FEEDBACK_OPERATION = "ff8081812ed77864012ed7984c6e00fb";
     
     protected String INCIDENT_CLIENTDATA_UDFID = "ff8081812e6bb868012e6bc433ce0006";
     protected String INCIDENT_EMAIL_UDF = "Электронная почта клиента";
     protected String INCIDENT_COMPANY_UDF = "Компания клиента";
     protected String INCIDENT_PHONE_UDF = "Контактный телефон";
     protected String WORKAROUND_PRODUCT_UDFID = "ff8081812ec813e8012ec823e2940045";
     protected String PRODUCT_CATEGORY_UDFID = "ff8081812e6bb868012e6c45861804a3";
     protected String CLIENT_ROLE_ID = "402881821204446701124cffdf95036d";
     protected String CLIENT_ROOT_ID = "40288182125234610112523776590001";
     protected String INCIDENT_IMPACT_UDFID = "297eef002e045fd5012e046392f0004a";
     protected String INCIDENT_URGENCY_UDFID = "297eef002e045fd5012e0462c9370004";
     protected String INCIDENT_WORKFLOW = "402881821204446701122906b74502df";
     protected String FIRST_LINE_ROLE_ID = "ff8081812e5c7497012e5c8adb520096";
     protected String SECOND_LINE_ROLE_ID = "ff8081812e5c7497012e5c8b36cb011b";
     protected String THIRD_LINE_ROLE_ID = "ff8081812e5c7497012e5c8b7a9901a0";
     protected String FIRST_LINE_MANAGER_ROLE_ID = "ff8081812e5c7497012e5c8bdd430225";
     protected String SECOND_LINE_MANAGER_ROLE_ID = "ff8081812e5c7497012e5c8c0e7f02aa";
     protected String THIRD_LINE_MANAGER_ROLE_ID = "ff8081812e5c7497012e5c8c4165032f";
     protected String ESCALATE_OPERATION = "ff8081812e9fac85012e9fdea972009e";
     protected String ESCALATOR_BOT_ROLE = "ff8081812e9fac85012e9fb372780002";
     protected String WORKAROUND_CATEGORY_ID = "ff8081812e6bb868012e6bca1de1007f";
     protected String WORKAROUND_ROOT_ID = "ff8081812e6bb868012e6bc8d4b3007e";
     protected String PROBLEM_ROOT_ID = "40288182125234610112570ef3eb0038";
     protected String PROBLEM_CATEGORY_ID = "4028818212991c6e01129a46597c0095";
     protected String INCIDENT_WORKAROND_UDFID = "ff8081812e6bb868012e6bcc372a010d";
     protected String INCIDENT_TYPE_UDFID = "ff8081812e6bb868012e6c52ef830536";
     protected String PROBLEM_WORKAROND_UDFID = "ff8081812f8bd341012f8c9ec0f4027a";
     protected String WORKAROUND_FAQ_UDFID = "ff8081812ec813e8012ec823030a0004";
     protected String WORKAROUND_FAQ_YES = "ff8081812ec813e8012ec82309bc0043";
     protected String INCIDENT_RETURN_2_LINE_OPERATION = "ff8081812eb4404c012eb45f97d100d0";
     protected String INCIDENT_RETURN_3_LINE_OPERATION = "ff8081812ed77864012ed7922b4c0008";
     protected String PROBLEM_CONFIRM_OPERATION = "ff8081812fbfe1d8012fbffd65ef0006";
     protected String INCIDENT_CONFIRM_OPERATION = "ff8081812e80827a012e80ecbe8c0002";
     protected String PROBLEM_DECLINE_OPERATION = "ff8081812fbfe1d8012fc01e914f0052";
     protected String INCIDENT_DECLINE_OPERATION = "ff8081812ea037d4012ea053997c00d6";
     
 
     protected String WORKAROUND_IN_PROBLEM_OPERATION = "ff8081812f8bd341012f8c988f5a0155";
     protected String WORKAROUND_CLOSE_OPERATION = "000000002f96c5ae012f96ed9a0e0009";
     protected String PROBLEM_CLOSE_OPERATION = "ff80808112bf740e0112bf896a7d00bc";
     protected String WORKAROUND_IN_INCIDENT_OPERATION = "ff8081812ea037d4012ea04ebabb001b";
     protected String INCIDENT_RELATED_PROBLEM_UDFID = "ff8081812f06d861012f06ff59560036";
     protected String PROBLEM_DUPLICATE_UDFID = "ff8081812f90bce8012f90fa53ce0108";
     protected String PROBLEM_RFC_UDFID = "ff8081812f8bd341012f8c9e5b74022f";
     protected String RFC_ROOT_ID = "40288182125234610112571090a4003a";
     protected String PRODUCT_ROOT_ID = "4028818212044467011232a18cc202ec";
     protected String PRODUCT_CATEGORY_ID = "4028818212b7e87b0112be20285904ef";
     protected String RFC_CATEGORY_ID = "4028818212b7e87b0112b8e6af570111";
     protected String RFC_PRODUCT_UDFID = "000000002f9b2134012f9b48808700b0";
     protected String PRODUCT_DEPRECATE_OPERATION = "ff808181301ba7e801301c51bab10107";
     protected String PRODUCT_REPLACEMENT_UDFID = "ff808181301ba7e801301c477acf00be";
     protected String PRODUCT_STATE_IN_USE = "4028818212b7e87b0112be1d1f4f04ed";
     protected String INCIDENT_DEADLINE_UDFID = "ff8081812e762c70012e774f955a000b";
 
     public CommonITSM() {
         if (properties != null) {
         	
             INCIDENT_PRODUCT_UDFID = properties.getProperty("itsm.incident.udf.product");
             INCIDENT_TYPE_UDFID = properties.getProperty("itsm.incident.udf.type");
             WORKAROUND_PRODUCT_UDFID = properties.getProperty("itsm.workaround.udf.product");
             PRODUCT_CATEGORY_UDFID = properties.getProperty("itsm.product.udf.categories");
             CLIENT_ROLE_ID = properties.getProperty("itsm.client.role");
             CLIENT_ROOT_ID = properties.getProperty("itsm.client.root");
             
             INCIDENT_CLIENT_UDF = properties.getProperty("itsm.incident.udf.client");
             INCIDENT_EMAIL_UDF = properties.getProperty("itsm.incident.udf.email");
             INCIDENT_COMPANY_UDF = properties.getProperty("itsm.incident.udf.company");
             INCIDENT_PHONE_UDF = properties.getProperty("itsm.incident.udf.phone");
             INCIDENT_CLIENTLINK_UDF = properties.getProperty("itsm.incident.udf.clientlink");
             
             INCIDENT_CLIENTDATA_UDFID = properties.getProperty("itsm.incident.udf.clientdata");
             INCIDENT_IMPACT_UDFID = properties.getProperty("itsm.incident.udf.impact");
             INCIDENT_URGENCY_UDFID = properties.getProperty("itsm.incident.udf.urgency");
             INCIDENT_WORKFLOW = properties.getProperty("itsm.incident.workflow");
             FIRST_LINE_ROLE_ID = properties.getProperty("itsm.firstline.role");
             FIRST_LINE_MANAGER_ROLE_ID = properties.getProperty("itsm.firstline.manager.role");
             SECOND_LINE_MANAGER_ROLE_ID = properties.getProperty("itsm.secondline.manager.role");
             THIRD_LINE_MANAGER_ROLE_ID = properties.getProperty("itsm.thirdline.manager.role");
             ESCALATE_OPERATION = properties.getProperty("itsm.escalate.operation");
             SECOND_LINE_ROLE_ID = properties.getProperty("itsm.second.role");
             THIRD_LINE_ROLE_ID = properties.getProperty("itsm.third.role");
             ESCALATOR_BOT_ROLE = properties.getProperty("itsm.escalator.role");
             WORKAROUND_CATEGORY_ID = properties.getProperty("itsm.workaround.category");
             INCIDENT_WORKAROND_UDFID = properties.getProperty("itsm.incident.udf.workaround");
             PROBLEM_WORKAROND_UDFID = properties.getProperty("itsm.problem.udf.workaround");
             WORKAROUND_ROOT_ID = properties.getProperty("itsm.workaround.root");
             WORKAROUND_FAQ_UDFID = properties.getProperty("itsm.workarond.udf.faq");
             WORKAROUND_FAQ_YES = properties.getProperty("itsm.workaround.faq.value");
             INCIDENT_RETURN_2_LINE_OPERATION = properties.getProperty("itsm.incident.return2line.operation");
             INCIDENT_RETURN_3_LINE_OPERATION = properties.getProperty("itsm.incident.return3line.operation");
             WORKAROUND_IN_PROBLEM_OPERATION = properties.getProperty("itsm.problem.workaround.operation");
             WORKAROUND_IN_INCIDENT_OPERATION = properties.getProperty("itsm.incident.workaround.operation");
             INCIDENT_RELATED_PROBLEM_UDFID = properties.getProperty("itsm.incident.udf.related.problem");
             PROBLEM_DUPLICATE_UDFID = properties.getProperty("itsm.problem.udf.duplicate");
             RFC_ROOT_ID = properties.getProperty("itsm.rfc.root");
             RFC_CATEGORY_ID = properties.getProperty("itsm.rfc.category");
             RFC_PRODUCT_UDFID = properties.getProperty("itsm.rfc.udf.product");
             PROBLEM_RFC_UDFID = properties.getProperty("itsm.problem.udf.rfc");
             PRODUCT_ROOT_ID = properties.getProperty("itsm.product.root");
             PRODUCT_CATEGORY_ID = properties.getProperty("itsm.product.category");
             PRODUCT_DEPRECATE_OPERATION = properties.getProperty("itsm.product.deprecate.operation");
             PRODUCT_REPLACEMENT_UDFID = properties.getProperty("itsm.product.udf.replacement");
             PRODUCT_STATE_IN_USE = properties.getProperty("itsm.product.state.in_use");
             WORKAROUND_CLOSE_OPERATION = properties.getProperty("itsm.workaround.close.operation");
             PROBLEM_CLOSE_OPERATION = properties.getProperty("itsm.problem.close.operation");
             INCIDENT_DEADLINE_UDFID =  properties.getProperty("itsm.incident.udf.deadline");
         }
     }
     
     protected Object getUDFValueByCaption(SecuredTaskBean t, String caption) throws GranException{
     	ArrayList<SecuredUDFValueBean> map = t.getUDFValuesList();
         for (SecuredUDFValueBean u: map){
         	if (u.getCaption().equals(caption)) return u.getValue();
         }
         return null;
     }
     public String executeOperation(String mstatusId, SecuredTaskBean task, String text, Map<String, String> udfMap) throws GranException {
         if (AdapterManager.getInstance().getSecuredStepAdapterManager().getNextStatus(task.getSecure(), task.getId(), mstatusId)!=null)
        return  TriggerManager.getInstance().createMessage(task.getSecure(), task.getId(), mstatusId, text, null, task.getHandlerUserId(), task.getHandlerGroupId(), null, task.getPriorityId(), task.getDeadline(), task.getBudget(), udfMap!=null ? (HashMap)udfMap: null, true, null );
         else return null;
     }
     protected void setPermissionUDF(SessionContext sc, String newUDFId, String clientUdfId)
 			throws GranException {
 		// скопировать права с поля Клиент
 		String incidentsRoot = KernelManager.getTask().findByNumber("50");
 		Set<SecuredPrstatusBean> prstatusSet = new TreeSet<SecuredPrstatusBean>(AdapterManager.getInstance().getSecuredPrstatusAdapterManager().getAvailablePrstatusList(sc, sc.getUserId()));
 		for (SecuredPrstatusBean spb : prstatusSet) {
             if (spb.isAllowedByACL() || spb.getUser().getSecure().allowedByACL(incidentsRoot)) {
                 List<String> types = AdapterManager.getInstance().getSecuredUDFAdapterManager().getUDFRuleList(sc, spb.getId(), clientUdfId);
                 KernelManager.getUdf().resetUDFRule(newUDFId, spb.getId());
         		for (String type: types)
         		KernelManager.getUdf().setUDFRule(newUDFId, spb.getId(), type);
             }
             }
 
 		
 		// для mstatus нужно отдельно доставать
 		List<String> editableIds = KernelManager.getUdf().getOperationsWhereUDFIsEditable(clientUdfId);
         List<String> viewableIds = KernelManager.getUdf().getOperationsWhereUDFIsViewable(clientUdfId);
         
 		for (String mstatusId: editableIds)
 			KernelManager.getUdf().setMstatusUDFRule(newUDFId, mstatusId, CategoryConstants.EDIT_ALL);	
 		
 		for (String mstatusId: viewableIds)
 			KernelManager.getUdf().setMstatusUDFRule(newUDFId, mstatusId, CategoryConstants.VIEW_ALL);
 	}
 }
