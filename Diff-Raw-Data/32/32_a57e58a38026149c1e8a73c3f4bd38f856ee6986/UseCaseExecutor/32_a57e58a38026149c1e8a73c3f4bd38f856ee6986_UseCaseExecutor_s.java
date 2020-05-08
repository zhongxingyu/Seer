 package spectacular.spec.execution;
 
 
 import groovy.lang.Closure;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import spectacular.data.model.*;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class UseCaseExecutor implements Executor<UseCase> {
 
     private static final String LOGGER_SEPARATOR = "-------------------------------";
 
     private static Log LOGGER = LogFactory.getLog(UseCaseExecutor.class);
 
 
     public void execute(Executable useCaseExecutable, Map<String, StepActionChain> stepActionChains, FixtureInventory fixtures, ExecutionResult result) throws SpectacularException {
 
         if (!useCaseExecutable.getExecutableType().equals(ExecutableType.USE_CASE)) {
             throw (new SpectacularException("Support for " + useCaseExecutable.getExecutableType() + " is not supported at this time."));
         }
 
 
         UseCase useCase = (UseCase) useCaseExecutable;
         if (LOGGER.isInfoEnabled()) LOGGER.info("Executing use case:  " + useCase.getUseCaseTitle());
         SpectacularExecutionContext context = new SpectacularExecutionContext();
 
         if (LOGGER.isInfoEnabled()) LOGGER.info("\tExecuting Preconditions\n" + LOGGER_SEPARATOR);
         List<String> preconditionList = useCase.getPreconditions();
         for (String precondition : preconditionList) {
             if (LOGGER.isInfoEnabled()) LOGGER.info("\t\tPrecondition:  " + precondition);
             // TODO:  Implement preconditions
         }
 
         if (LOGGER.isInfoEnabled()) LOGGER.info("\tExecuting Primary Flow\n" + LOGGER_SEPARATOR);
         Flow flow = useCase.getPrimaryFlow();
         FlowResult flowResult = null;
         try {
             flowResult = executeFlow(flow, stepActionChains, fixtures, context);
         } catch (Exception e) {
             flowResult.setStatus(ExecutionResultStatus.FAIL);
             flowResult.setStatusCommentary("Error during execution:  " + e);
             throw (new SpectacularException(e));
         }
 
 
     }
 
     public FlowResult executeFlow(Flow flow, Map<String, StepActionChain> stepActionChains, FixtureInventory fixtureInventory, SpectacularExecutionContext context) throws Exception {
 
        if (LOGGER.isInfoEnabled()) LOGGER.info("\t\tFlow:  " + flow.getFlowTitle());
         FlowResult flowResult = new FlowResult(flow);
         flowResult.setStatus(ExecutionResultStatus.NOT_EXECUTED);
 
         List<Step> stepList = flow.getSteps();
         for (Step step : stepList) {
 
            if (LOGGER.isInfoEnabled()) LOGGER.info("\t\t\tStep (" + step.getStepType() + "):  " + step.getStepTitle());
             StepResult stepResult = new StepResult(step);
 
 
             // look for step actions within chains
             StepActionChain chain = stepActionChains.get(step.getStepTitle());
             if (chain == null) {
 
                if (LOGGER.isInfoEnabled()) LOGGER.info("\t\t\tPENDING:  NO ACTION CHAINS FOR THIS STEP");
                 stepResult.setStatus(ExecutionResultStatus.PENDING);
                 stepResult.setStatusCommentary("Unable to find Actions matching this step.");
 
                 flowResult.setStatus(ExecutionResultStatus.PENDING);
 
             }
             List<Action> actionList = stepActionChains.get(step.getStepTitle()).getActions();
             if (actionList == null) {
 
                if (LOGGER.isInfoEnabled()) LOGGER.info("\t\t\tPENDING:  NO ACTIONS FOR THIS CHAIN");
                 stepResult.setStatus(ExecutionResultStatus.PENDING);
                 stepResult.setStatusCommentary("Unable to find Action List matching this step.");
 
                 flowResult.setStatus(ExecutionResultStatus.PENDING);
 
             }
 
            if (LOGGER.isInfoEnabled()) LOGGER.info("\t\t\tExecuting actions...");
             for (Action action : actionList) {
 
                if (LOGGER.isInfoEnabled()) LOGGER.info("\t\t\t\t**" + action.getActionText() + "**");
                 ActionResult actionResult = executeFixtureForAction(action, fixtureInventory, context);
                 if(!actionResult.getStatus().equals(ExecutionResultStatus.PASS)) {
                     stepResult.addActionResult(actionResult);
                     stepResult.setStatus(ExecutionResultStatus.FAIL);
                     break;
                 }
 
             }
 
             flowResult.addStepResult(stepResult);
 
         }
 
 
         return (flowResult);
 
     }
 
     public ActionResult executeFixtureForAction(Action action, FixtureInventory fixtureInventory, SpectacularExecutionContext context) {
 
         ActionResult result = new ActionResult(action);
         result.setStatus(ExecutionResultStatus.NOT_EXECUTED);
 
         // find fixture
         String actionText = action.getActionText();
         ExecutionInstance executionInstance = fixtureInventory.findExecutionInstanceForText(actionText);
 
         if (executionInstance == null) {
             result.setStatus(ExecutionResultStatus.PENDING);
             return (result);
         }
 
         try {
 
             // Object[] regexMatches = executionInstance.getRegexGroupMatches().toArray();
             int i = 1;
             Object[] regexMatches = new Object[executionInstance.getRegexGroupMatches().size() + 1];
             regexMatches[0] = context;
             for (String match : executionInstance.getRegexGroupMatches()) {
                 regexMatches[i] = match;
                 i++;
             }
 
 
             executionInstance.getClosure().call(regexMatches);
 
 
         } catch (Exception e) {
             if (LOGGER.isInfoEnabled()) LOGGER.info("Error while executing fixture.", e);
             result.setStatus(ExecutionResultStatus.FAIL);
             result.setStatusCommentary(e.toString());
             return (result);
         }
 
 
         result.setStatus(ExecutionResultStatus.PASS);
         return result;
 
     }
 
 
 }
