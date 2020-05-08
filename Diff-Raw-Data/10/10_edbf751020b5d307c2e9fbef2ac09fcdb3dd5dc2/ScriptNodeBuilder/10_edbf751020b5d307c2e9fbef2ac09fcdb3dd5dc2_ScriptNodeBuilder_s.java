 package org.wiredwidgets.cow.server.transform.graph.bpmn20;
 
 import javax.xml.bind.JAXBElement;
 
 import org.omg.spec.bpmn._20100524.model.Property;
 import org.omg.spec.bpmn._20100524.model.TScriptTask;
 import org.springframework.stereotype.Component;
 import org.wiredwidgets.cow.server.api.model.v2.Script;
 
 @Component
 public class ScriptNodeBuilder extends AbstractFlowNodeBuilder<Script, TScriptTask> {
 
 	@Override
 	public Class<Script> getType() {
 		return Script.class;
 	}
 
 	@Override
 	public TScriptTask newNode() {
 		return new TScriptTask();
 	}
 
 	@Override
 	public JAXBElement<TScriptTask> createElement(TScriptTask node) {
 		return factory.createScriptTask(node);
 	}
 
 	@Override
 	protected void buildInternal(TScriptTask node, Script script,
 			Bpmn20ProcessContext context) {
 		
         node.setName(script.getName());
         
         // add imports at the process level;
         for (String className : script.getImports()) {
        	 context.addImport(className);
         }
         
         // create a process level property to indicate whether the script has been
         // run or not. 
         String completedPropertyName = node.getId() + "_completed";
         Property property = context.addProcessVariable(completedPropertyName, "Boolean");
         
         org.omg.spec.bpmn._20100524.model.Script bpmn20Script = new org.omg.spec.bpmn._20100524.model.Script();           
         bpmn20Script.getContent().add(script.getContent());
         
         // XXX: kind of a hack
         // Tried to do this somehow via output variable assignment but was unable
         // to get it working.  Unclear whether it is possible to have an output variable
         // that is simply an expression value.  In any case this gets the job done.
         
         String scriptLine = "kcontext.setVariable(\"" + completedPropertyName + "\"" + ", true);";
         
         bpmn20Script.getContent().add(scriptLine);
         
         node.setScriptFormat(script.getScriptFormat());
         node.setScript(bpmn20Script);	
 		
 	}
 	
 }
