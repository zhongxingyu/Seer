 package org.gsoft.openserv.rulesengine;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.annotation.Resource;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatelessKnowledgeSession;
 import org.gsoft.openserv.buslogic.system.SystemSettingsLogic;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Component;
 import org.springframework.util.ResourceUtils;
 
 @Component
 public class DroolsRulesEngine implements RulesEngine{
 	private boolean isOpen = false;
 	private KnowledgeBase base = null;
 	private ArrayList<Object> knowledge = new ArrayList<Object>();
 	@Resource
 	private ApplicationContext springContext;
 	@Resource
 	private SystemSettingsLogic systemSettingsLogic;
 	
 	private KnowledgeBase getKnowledgeBase(){
 		if(base == null){
 			KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
 			File ruleDir;
 			try {
 				ruleDir = ResourceUtils.getFile("classpath:rules");
 			} catch (FileNotFoundException e) {
 				throw new RulesEngineException("Unable to load DRL files.", e);
 			}
 			for(File ruleFile:ruleDir.listFiles()){
 				if(ruleFile.getName().endsWith(".drl"))
 					builder.add(ResourceFactory.newFileResource(ruleFile), ResourceType.DRL);
 			}
 			if(builder.hasErrors()){
 				StringBuilder errorMessage = new StringBuilder("Errors parsing DRLs.  KnowledgeBuilder errors: ");
 				String comma = "";
 				for(KnowledgeBuilderError error : builder.getErrors()){
 					errorMessage.append(comma);
 					errorMessage.append(error.getMessage());
 					comma = ",";
 				}
 				throw new RulesEngineException(errorMessage.toString());
 			}
 			base = KnowledgeBaseFactory.newKnowledgeBase();
 			base.addKnowledgePackages(builder.getKnowledgePackages());
 		}
 		return base;
 	}
 	
 	public boolean isOpen() {
 		return this.isOpen;
 	}
 
 	public void close() {
 		isOpen = false;
 		knowledge.clear();
 	}
 
 	public void open() {
 		isOpen = true;
 	}
 
 	public void evaluateRules() {
 		if(!this.isOpen)
 			throw new RulesEngineException("RulesEngine must be open before it can be run.");
 		HashMap<String,Date> sysDate = new HashMap<String, Date>();
 		sysDate.put("systemDate", systemSettingsLogic.getCurrentSystemDate());
 		knowledge.add(sysDate);
 		StatelessKnowledgeSession session = this.getKnowledgeBase().newStatelessKnowledgeSession();
 		session.setGlobal("springContext", springContext);
 		session.execute(knowledge);
 	}
 
 	public void addContext(final Object... addedContext) {
 		if(!this.isOpen)
 			throw new RulesEngineException("RulesEngine must be open before objects can be added to the context.");
 		knowledge.addAll(Arrays.asList(addedContext));
 	}
 
 }
