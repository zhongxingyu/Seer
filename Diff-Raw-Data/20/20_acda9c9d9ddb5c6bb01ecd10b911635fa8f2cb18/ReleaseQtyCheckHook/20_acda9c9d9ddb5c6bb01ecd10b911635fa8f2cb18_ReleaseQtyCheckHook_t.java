 /**
  * 
  */
 package com.varian.hook;
 
 import java.util.List;
 
 import com.sap.me.appconfig.FindSystemRuleSettingRequest;
 import com.sap.me.appconfig.SystemRuleServiceInterface;
 import com.sap.me.appconfig.SystemRuleSetting;
 import com.sap.me.demand.ShopOrderReleaseHookDTO;
 import com.sap.me.demand.ShopOrderReleaseHookDTO.AdditionalInfoList;
 import com.sap.me.extension.Services;
 import com.varian.hook.exception.OrderReleaseQtyException;
 import com.visiprise.frame.service.ext.ActivityInterface;
 
 /**
  * @author vmurthy
  *
  */
 public class ReleaseQtyCheckHook implements
 		ActivityInterface<ShopOrderReleaseHookDTO> {
 
 
 	private static final String COM_SAP_ME_APPCONFIG = "com.sap.me.appconfig";
 	private static final String SYSTEM_RULE_SERVICE = "SystemRuleService";
 	private static final long serialVersionUID = 1L;
 	private SystemRuleServiceInterface systemRuleService;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.visiprise.frame.service.ext.ActivityInterface#execute(java.lang.Object)
 	 */
 	public void execute(ShopOrderReleaseHookDTO dto) throws Exception {
 		initServices();
 		boolean sysruleval1 = false;
 		int sysruleval2 = 0;
 		String ruleName1 = "Z_SFC_REL_QTY_VALIDATION";
 		String ruleName2 = "Z_SFC_RELEASE_QTY_LIMIT";
 		FindSystemRuleSettingRequest findsysrulereq1 = new FindSystemRuleSettingRequest();
 		findsysrulereq1.setRuleName(ruleName1);
 		SystemRuleSetting sysrulesetting1 = systemRuleService.findSystemRuleSetting(findsysrulereq1);
 		sysruleval1 =  Boolean.valueOf(sysrulesetting1.getSetting().toString());		
 		if(sysruleval1){
 			FindSystemRuleSettingRequest findsysrulereq2 = new FindSystemRuleSettingRequest();
 			findsysrulereq2.setRuleName(ruleName2);
 			SystemRuleSetting sysrulesetting2 = systemRuleService.findSystemRuleSetting(findsysrulereq2);
			try {				
 				sysruleval2 = Integer.parseInt(sysrulesetting2.getSetting().toString());
				}
			catch (Exception e){
				throw new OrderReleaseQtyException (20103,Integer.toString(sysruleval2));
					}
 				if (sysruleval2 > 0)
 				{
 					List<AdditionalInfoList> sfcList = dto.getAdditionalInfoList();
 					if (sfcList.size() > sysruleval2) {	    	
 						throw new OrderReleaseQtyException(20102,Integer.toString(sysruleval2));
 					}	
 				} 
		
 				else 
 				{
 					throw new OrderReleaseQtyException (20103,Integer.toString(sysruleval2));	
 				}
 			
 			}
		
 		}
	
 
 
 	/*
 	 *	The method should be called before the services are used.
 	 */
 	/**
 	 *	Initialization of services that are represented as fields.
 	 */
 	private void initServices(){
 		systemRuleService = Services.getService(COM_SAP_ME_APPCONFIG,SYSTEM_RULE_SERVICE);
 	}
 
 }
