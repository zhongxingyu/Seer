 package cz.cuni.mff.odcleanstore.webfrontend.bo.dn;
 
 public class DNFilterTemplateInstanceCompiler 
 {
 	public static CompiledDNRule compile(DNFilterTemplateInstance instance)
 	{
 		// 1. Create rule.
 		//
 		String description = String.format
 		(
 			"Raw form of a filter rule template instance. " +
 			"Property: %s; Pattern: %s; Keep: %s;", 
 			instance.getPropertyName(), 
 			instance.getPattern(),
 			instance.getKeep().toString()
 		);
 		
 		CompiledDNRule rule = new CompiledDNRule(instance.getGroupId(), description);
 
 		// 2. Create components.
 		//
 		String modification = String.format
 		(
			"{?s ?p ?o} WHERE {GRAPH $$graph$$ {?s ?p ?o} FILTER (?p = %s AND %sfn:match(str(?o), '%s'))}", 
 			instance.getPropertyName(),
 			instance.getKeep() ? "!" : "",
 			instance.getPattern()
 		);
 
 		String compDescription = String.format
 		(
 			"Filter out %smatching values of the property.", 
 			instance.getKeep() ? "non-" : ""
 		);
 		
 		CompiledDNRuleComponent component = new CompiledDNRuleComponent
 		(
 			CompiledDNRuleComponent.TypeLabel.DELETE,
 			modification,
 			compDescription
 		);
 
 		// 3. Register components with rule.
 		//
 		rule.addComponent(component);
 		
 		return rule;
 	}
 }
