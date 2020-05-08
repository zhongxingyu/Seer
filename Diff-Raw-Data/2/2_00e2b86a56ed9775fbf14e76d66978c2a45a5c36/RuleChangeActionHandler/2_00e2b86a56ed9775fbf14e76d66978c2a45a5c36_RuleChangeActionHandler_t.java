 package actionHandlers;
 
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.drools.compiler.DroolsParserException;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import services.NomicService;
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.environment.ActionHandler;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import actions.ProposeRuleAddition;
 import actions.ProposeRuleChange;
 import actions.ProposeRuleModification;
 
 import com.fasterxml.jackson.annotation.PropertyAccessor;
 import com.google.inject.Inject;
 
 import enums.RuleChangeType;
 
 public class RuleChangeActionHandler implements ActionHandler {
 	
 	final StatefulKnowledgeSession session;
 	private final Logger logger = Logger.getLogger(RuleChangeActionHandler.class);
 	final EnvironmentServiceProvider serviceProvider;
 	
 	NomicService nomicService;
 	
 	@Inject
 	public RuleChangeActionHandler(StatefulKnowledgeSession session,
 			EnvironmentServiceProvider serviceProvider) {
 		super();
 		this.session = session;
 		this.serviceProvider = serviceProvider;
 	}
 	
 	public NomicService getNomicService() {
 		if (nomicService == null) {
 			try {
 				nomicService = serviceProvider.getEnvironmentService(NomicService.class);
 			} catch (UnavailableServiceException e) {
 				logger.warn("Unable to get NomicService.");
 			}
 		}
 		return nomicService;
 	}
 
 	@Override
 	public boolean canHandle(Action action) {
 		return action instanceof ProposeRuleChange;
 	}
 
 	@Override
 	public Input handle(Action action, UUID actor)
 			throws ActionHandlingException {
 		
 		NomicService service = getNomicService();
 		
 		RuleChangeType change = ((ProposeRuleChange)action).getRuleChangeType();
 		
 		if (change == RuleChangeType.MODIFICATION) {
 			ProposeRuleModification ruleMod = (ProposeRuleModification)action;
 			try {
 				service.addRule(ruleMod.getNewRule());
				service.RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
 			} catch (DroolsParserException e) {
 				logger.warn("Unable to parse new version of existing rule.", e);
 			}
 		}
 		else if (change == RuleChangeType.ADDITION) {
 			ProposeRuleAddition ruleMod = (ProposeRuleAddition)action;
 			try {
 				service.addRule(ruleMod.getNewRule());
 			} catch (DroolsParserException e) {
 				logger.warn("Unable to parse new rule.", e);
 			}
 		}
 		
 		session.insert(action);
 		return null;
 	}
 
 }
