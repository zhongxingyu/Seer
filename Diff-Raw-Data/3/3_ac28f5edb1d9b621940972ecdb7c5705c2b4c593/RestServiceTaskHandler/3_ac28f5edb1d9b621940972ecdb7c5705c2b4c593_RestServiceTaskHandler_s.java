 package org.wiredwidgets.cow.server.manager;
 
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.process.WorkItem;
 import org.drools.runtime.process.WorkItemHandler;
 import org.drools.runtime.process.WorkItemManager;
 import org.drools.runtime.process.WorkflowProcessInstance;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.common.TemplateParserContext;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.expression.spel.support.StandardEvaluationContext;
 import org.springframework.http.HttpMethod;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 import org.wiredwidgets.cow.server.service.ProcessInstanceService;
 
 
 public class RestServiceTaskHandler implements WorkItemHandler {
 	
 	private static Logger log = Logger.getLogger(RestServiceTaskHandler.class);
         @Autowired
         RestTemplate restTemplate; 
         @Autowired
         StatefulKnowledgeSession kSession;        
         
                 
         private String url = null;
         private String method = null;
         private String content = null;
         private String var = null;
         
 	@Override
 	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {	
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
 		log.info("Work item: " + item.getName());
 		
 		for (Entry<String, Object> entry : item.getParameters().entrySet()) {
 			log.info(entry.getKey() + ":" + entry.getValue());
 		}
 		
                 content = (String)item.getParameter("content");
                 var = (String)item.getParameter("var");
                 url = (String)item.getParameter("url");
                 method = (String)item.getParameter("method"); 
                 
                 //RestTemplate restTemplate = new RestTemplate();
                 String result = null;
                 if (method.equalsIgnoreCase(HttpMethod.GET.name())) {                     
                     result = restTemplate.getForObject(evaluateExpression(url,item), String.class); 
                     System.out.println("GET result: " + result);
                 } else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {            
                     try {
                         // this method expects XML content in the response.  if none if found an exception is thrown
                         result = restTemplate.postForObject(evaluateExpression(url,item), evaluateExpression(content,item), String.class);
                         System.out.println("POST result: " + result);
                     }
                     catch (RestClientException e) {
                         // in this case, just log the error and move on.  The result will be null.
                         log.error(e);
                     }
                 }            
                 
                  
                 // update the result variable, if specified
                if (var != null && result != null) {                      
                     WorkflowProcessInstance pi = (WorkflowProcessInstance)kSession.getProcessInstance(item.getProcessInstanceId());                    
                     pi.setVariable(var, result); 
                     //executionService.setVariable(execution.getId(), var, result);
                     log.info(var + ": " + pi.getVariable(var));
                 }
 
                 // signal the execution to exit this state                
                 //executionService.signalExecutionById(execution.getId());
                 kSession.signalEvent(item.getName(), null);
                 
 		manager.completeWorkItem(item.getId(), null);		
 		
 	}
         /*
         public void setContent(String content) {
         this.content = content;
         }
 
         public void setMethod(String method) {
             this.method = method;
         }
 
         public void setUrl(String url) {
             this.url = url;
         }
 
         public void setVar(String var) {
             this.var = var;
         } */   
             
         private String evaluateExpression(String expression, WorkItem wi) {
         //private String evaluateExpression(String expression) {
         // change ${xxx} to #{#xxx} 
         expression = expression.replaceAll("\\$\\{(.*)\\}", "#\\{#$1\\}");
 
         StandardEvaluationContext context = new StandardEvaluationContext();
         ExpressionParser parser = new SpelExpressionParser();    
              
         //context.setVariables((executionService.getVariables(execution.getId(), executionService.getVariableNames(execution.getId()))));
         context.setVariables(wi.getParameters());
         System.out.println(parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class));
         return parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class);
     }
 
 }
