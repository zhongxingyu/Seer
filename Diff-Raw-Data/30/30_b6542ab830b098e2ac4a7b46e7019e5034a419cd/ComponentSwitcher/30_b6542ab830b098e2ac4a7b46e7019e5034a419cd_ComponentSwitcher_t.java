 package org.nju.artemis.aejb.evolution.behaviors;
 
 import org.jboss.logging.Logger;
 import org.nju.artemis.aejb.component.AEjbUtilities;
 import org.nju.artemis.aejb.evolution.OperationContext;
 import org.nju.artemis.aejb.evolution.OperationFailedException;
 import org.nju.artemis.aejb.evolution.handlers.DependencyChangeHandler;
 import org.nju.artemis.aejb.evolution.handlers.DependencyComputeHandler;
import org.nju.artemis.aejb.evolution.handlers.ComponentInstanceDirectHandler;
import org.nju.artemis.aejb.evolution.handlers.InterfaceCompareHandler;
import org.nju.artemis.aejb.evolution.handlers.OperationStepHandler;
 
 /**
  * This evolution behavior used to switch component to a target one.<br>
  * It composed by four handlers:
  * 
  * {@link InterfaceIdentifyHandler};
  * {@link DependencyComputeHandler};
 * {@link ComponentInstanceDirectHandler};
  * {@link DependencyChangeHandler}.
  * 
  * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
  */
 public class ComponentSwitcher extends EvolutionBehavior{
 	Logger log = Logger.getLogger(ComponentSwitcher.class);
 	
 	private static final String HANDLER_NAME = "ComponentSwitcher";
 	private final String fromName;
 	private final String toName;
 	private final String protocol;
 	private AEjbUtilities utilities;
 	
 	public ComponentSwitcher(String fromName, String toName, String protocol) {
 		this.fromName = fromName;
 		this.toName = toName;
 		this.protocol = protocol;
 	}
 	
 	@Override
 	public OperationResult execute(OperationContext context) throws OperationFailedException {
 		utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
 		perform();
 		return OperationResult.Expected;
 	}
 
 	@Override
 	protected void initializeStepHandlers() {
		handlers.add(new InterfaceCompareHandler());
 		handlers.add(new DependencyComputeHandler());
		handlers.add(new ComponentInstanceDirectHandler());
 		handlers.add(new DependencyChangeHandler());
 		// Not need lock component
 //		handlers.add(new OperationAlterHandler(ComponentLocker.LOCK));
 //		handlers.add(new ComponentLocker());
 //		handlers.add(new OperationAlterHandler(ComponentLocker.UNLOCK));
 //		handlers.add(new ComponentLocker());
 	}
 
 	@Override
 	protected OperationContext generateOperationContext() {
 		OperationContext context = new OperationContext(null, fromName);
 		context.getContextData().put(AEjbUtilities.class, utilities);
 		context.getContextData().put("toName", toName);
 		context.getContextData().put("protocol", protocol);
 		return context;
 	}
 
 	@Override
 	public String getHandlerName() {
 		return HANDLER_NAME;
 	}
 
	@Override
	void rollBackWhenUnExpectedResult(OperationStepHandler handler) throws OperationFailedException {
		throw new OperationFailedException(handler.getHandlerName() + " gets unexpected result, evolution interrupted.");
	}

 }
