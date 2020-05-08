 package gt.general.logic;
 
 import static com.google.common.collect.Sets.newHashSet;
 import gt.general.logic.persistance.YamlSerializable;
 import gt.general.logic.response.Response;
 import gt.general.logic.trigger.Trigger;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 
 import com.google.common.collect.Iterables;
 
 public class TriggerContext {
 
 	public enum InputFunction {
 		AND, OR
 	}
 	
 	private final Set<Trigger> triggers = newHashSet();
 	private final Set<Response> responses = newHashSet();
 	
 	private InputFunction inputFunction = InputFunction.OR;
 
 	private String label;
 	
 	private final Set<Trigger> activeTriggers = newHashSet();
 	
 	/**
 	 * Generates a new TriggerContext
 	 */
 	public TriggerContext() {		
 		setLabel("context_" + hashCode());
 	}
 
 	/**
 	 * @return the inputFunction
 	 */
 	public InputFunction getInputFunction() {
 		return inputFunction;
 	}
 
 	/**
 	 * @param inputFunction the inputFunction to set
 	 */
 	public void setInputFunction(final InputFunction inputFunction) {
 		this.inputFunction = inputFunction;
 	}
 	
 	/**
 	 * toggle (AND / OR) input function
 	 */
 	public void toggleInputFunction() {
 		if(inputFunction == InputFunction.OR) {
 			inputFunction = InputFunction.AND;
 		} else {
 			inputFunction = InputFunction.OR;
 		}
 	}
 	
 	/**
 	 * @param trigger trigger which changes its state
 	 * @param state new state of the trigger
 	 */
 	public void updateTriggerState(final Trigger trigger, final boolean state, final Player player) {		
 		boolean oldState = evalInputFuntion();
 		
 		if(state) {
 			activeTriggers.add(trigger);
 		} else {
 			activeTriggers.remove(trigger);
 		}
 		boolean newState = evalInputFuntion();
 		
 		if(oldState != newState) {
 			
 			TriggerEvent e = new TriggerEvent(newState, player);
 			
 			for(Response response : responses) {
 				response.triggered(e);
 			}
 		}
 	}
 	
 	/**
 	 * @return true if the context should be considered triggered
 	 */
 	private boolean evalInputFuntion() {
 		switch (inputFunction) {
 		case OR:
 			return activeTriggers.size() != 0;
 		case AND:
 			return activeTriggers.size() == triggers.size();
 		default:
 			break;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return true if the TriggerContext has a trigger and a response
 	 */
 	public boolean isComplete() {
 		return !triggers.isEmpty() && !responses.isEmpty();
 	}
 
 	/**
 	 * @return the triggers of this context
 	 */
 	public Collection<Trigger> getTriggers() {
 		return triggers;
 	}
 
 	/**
 	 * @return the responses of this context
 	 */
 	public Collection<Response> getResponses() {
 		return responses;
 	}
 	
 	/**
 	 * @param serializable the serializable to be removed
 	 */
 	public void removeSerializable(final YamlSerializable serializable) {
 		triggers.remove(serializable);
 		responses.remove(serializable);
 		activeTriggers.remove(serializable);
 		serializable.dispose();
 	}
 	
 	/**
 	 * @return the label of this context
 	 */
 	public String getLabel() {
 		return label;
 	}
 	
 	/**
 	 * @param label the label of this context
 	 */
 	public void setLabel(final String label) {
 		this.label = label;
 	}
 	
 	/**
 	 * @param trigger the trigger to add
 	 */
 	public void addTrigger(final Trigger trigger) {
 		triggers.add(trigger);
 		trigger.setContext(this);
 	}
 	
 	/**
 	 * @param response the response to add
 	 */
 	public void addResponse(final Response response) {
 		responses.add(response);
 	}
 
 	/**
 	 * disposes all triggers and responses
 	 */
 	public void dispose() {
 		for(Trigger trigger : triggers) {
 			trigger.dispose();
 		}
		
 		for(Response response : responses) {
 			response.dispose();
 		}
 	}
 	
 	public Iterable<YamlSerializable> getAllItems() {
 		return Iterables.concat(triggers, responses);
 	}
 
 	public void setupInverted(Trigger trigger) {
 		activeTriggers.add(trigger);		
 	}
 	
 }
