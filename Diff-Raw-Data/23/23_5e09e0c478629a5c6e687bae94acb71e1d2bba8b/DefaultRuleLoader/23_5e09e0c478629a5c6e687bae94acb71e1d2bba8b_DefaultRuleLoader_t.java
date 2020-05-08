 package uk.ac.ed.inf.Metabolic;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 import org.pathwayeditor.contextadapter.publicapi.IDefaultValidationRuleConfigLoader;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleConfig;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition.RuleLevel;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationRuleConfig;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationRuleDefinition;
 
 /**
  * Supplies the default Rules and their definitions.
  * Just mocked rules at the moment but these should be replaced with real rules.
  * 
  * @author Richard Adams
  *
  */
 public class DefaultRuleLoader implements IDefaultValidationRuleConfigLoader {
 	static final int ERROR_ID = 1000;
 	static IValidationRuleDefinition def1, def2, def3, def4,ERROR;
 	
 	static IValidationRuleConfig config1 ,config2, config3, config4, ERROR_CONFIG;
 	static IContext context;
 	static {
 		context = MetabolicContextAdapterServiceProvider.getInstance().getContext();
 		def1 = new ValidationRuleDefinition(context, "Rule 1", "Layout", 1, RuleLevel.MANDATORY);
 		def2 = new ValidationRuleDefinition(context, "Rule 2", "Type", 2, RuleLevel.OPTIONAL);
 		def3 = new ValidationRuleDefinition(context, "Rule 3", "Graph", 3, RuleLevel.GUIDELINE);
 		def4 = new ValidationRuleDefinition(context, "Rule 4 with quite a long name", "Graph", 4, RuleLevel.GUIDELINE);
 		ERROR = new ValidationRuleDefinition(context, "Exception!", "Graph", ERROR_ID, RuleLevel.MANDATORY);
 		
 		config1 = new ValidationRuleConfig(def1, true, true);// must be run, error
 		config2 = new ValidationRuleConfig(def2, true, true); // must be run, error
 		config3 = new ValidationRuleConfig(def3, true, false); // must be run, warning
 		config4 = new ValidationRuleConfig(def4, false, true); // not run, error
		ERROR_CONFIG = new ValidationRuleConfig(ERROR, true, true); // not run, error
 		
 	}
 
 	public Set<IValidationRuleConfig> loadDefaultRuleConfigurations() {
		Set <IValidationRuleConfig> rc =  new HashSet<IValidationRuleConfig>(Arrays.asList(new IValidationRuleConfig[]{config1, config2, config3,config4, ERROR_CONFIG}));
 		return rc;
 	}
 
 	
 
 }
