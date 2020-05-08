 package com.orangeleap.tangerine.service.rule;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.springframework.context.ApplicationContext;
 
 import com.orangeleap.tangerine.service.customization.RulesConfService;
 import com.orangeleap.tangerine.type.RuleObjectType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.RulesStack;
 
 public class OrangeLeapRuleSession {
 
 		protected final Log logger = OLLogger.getLog(getClass());
 
 		private OrangeLeapRuleBase orangeLeapRuleBase;
 
 		private Map<String, Object> map = new TreeMap<String, Object>();
 
 		public OrangeLeapRuleSession(OrangeLeapRuleBase orangeLeapRuleBase) {
 			this.orangeLeapRuleBase = orangeLeapRuleBase;
 		}
 
 		public void put(RuleObjectType type, Object object) {
 			if (object != null) map.put(type.getType(), object);
 		}
 
 		public void executeRules() {
 
 			// Re-entrancy check will not execute the same event's rules again within that event's rule processing
 			String operation = "OrangeLeapRuleSession.executeRules() " + orangeLeapRuleBase.getRuleEventNameType();
 	        boolean reentrant = RulesStack.push(operation);
 	        try {
 		        if (!reentrant) {
 					try {
 
 						logger.debug("Executing dynamic ruleset for "+orangeLeapRuleBase.getRuleEventNameType());
 
 						map.put(RULE_EXECUTION_SUMMARY, new ArrayList<String>());
 						addServicesToMap();
 
 						RulesConfService rulesConfService = (RulesConfService)orangeLeapRuleBase.getApplicationContext().getBean("rulesConfService");
 						rulesConfService.fireRulesEvent(orangeLeapRuleBase.getRuleEventNameType(), orangeLeapRuleBase.isTestMode(), map);
 
 					} catch (OrangeLeapConsequenceRuntimeException e) {
 						throw e;
 					} catch (Exception e) {
 						logger.error(e.getMessage());
 						throw new RuntimeException(e);
 					}
 		        }
 	        } finally {
 	        	printRulesExecutionSummary();
 	        	RulesStack.pop(operation);
 	        }
 
 		}
 
 		@SuppressWarnings("unchecked")
 		private void printRulesExecutionSummary() {
			
 			try {
 
 				StringBuilder sb = new StringBuilder();
 				sb.append("Rules Execution Summary for "+orangeLeapRuleBase.getRuleEventNameType()+":\n");
 				List<String> summary = (List<String>)map.get(RULE_EXECUTION_SUMMARY);
 				if (summary != null) for (String s: summary) sb.append(s).append("\n");
	
 				logger.info(sb.toString()); // TODO change to debug
			
 			} catch (Exception e) {
 				logger.error(e);
 			}
 
 		}
 
 		public static final String RULE_EXECUTION_SUMMARY = "ruleExecutionSummary";
 
 		// Add any new services used by rules to this list:
 		private void addServicesToMap() {
 			ApplicationContext applicationContext = orangeLeapRuleBase.getApplicationContext();
 			map.put("logger", logger);
 			map.put("applicationContext", applicationContext);
 			map.put("tangerineUserHelper", applicationContext.getBean("tangerineUserHelper"));
 			map.put("userHelper", applicationContext.getBean("tangerineUserHelper"));
 			map.put("siteService", applicationContext.getBean("siteService"));
 			map.put("constituentService", applicationContext.getBean("constituentService"));
 			map.put("giftService", applicationContext.getBean("giftService"));
 			map.put("communicationHistoryService", applicationContext.getBean("communicationHistoryService"));
 			map.put("picklistItemService", applicationContext.getBean("picklistItemService"));
 			map.put("emailService", applicationContext.getBean("emailService"));
 			map.put("mailService", applicationContext.getBean("mailService"));
 			map.put("errorLogService", applicationContext.getBean("errorLogService"));
 			map.put("ruleHelperService", applicationContext.getBean("ruleHelperService"));
 			map.put("paymentGateway", applicationContext.getBean("paymentGateway"));
 		}
 
 }
