 package vooga.towerdefense.action.actionlist;
 
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.attributes.Attribute;
 
 /**
  * @author Matthew Roy
  * @author Xu Rui
  *
  */
 public class OnAttributeValue extends Action {
 
 	private Attribute myMonitoredAttribute;
 	private Attribute myConditionAttribute;
 
 	/**
 	 * constructor
 	 * @param attributeWatched
 	 * @param conditionAttribute
 	 */
 	public OnAttributeValue (Attribute attributeWatched, Attribute conditionAttribute) {
 		myMonitoredAttribute = attributeWatched;
 		myConditionAttribute = conditionAttribute;
 	}
 
 	/**
 	 * Update follow up actions when conditions are met
 	 * @param elapsedTime 
 	 */
 	@Override
 	public void executeAction (double elapsedTime) {
 			if (myMonitoredAttribute.getValue()== myConditionAttribute.getValue()){
				updateFollowupActions(elapsedTime);
 				setEnabled(false);
 			}
 	}
 
 	/**
 	 * Overrides from superclasses to prevent follow up action from executing prematurely.
 	 * @param elapsedTime 
 	 */
 	@Override
 	public void update (double elapsedTime) {
 		if (isEnabled()) {
 			executeAction(elapsedTime);
 		}
 
 	}
 
 }
