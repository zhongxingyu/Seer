 package fr.imag.adele.apam.application.security;
 
 import appsgate.lig.colorLight.actuator.spec.CoreColorLightSpec;
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.PropertyManager;
 
 public class SecurityStateManager implements PropertyManager {
 
 
 	/**
 	 * The identified state transition events triggered by application change
 	 */
 	public enum Event {
 		
 		START_LOCK,
 		END_LOCK,
 		
 		START_FIRE,
 		END_FIRE;
 
 	}
 
 
 	/**
 	 * The identified emergency states in the domain, with the associated automata to
 	 * arbitrate conflicts
 	 */
 	public enum State {
 		
 		NORMAL("normal") {
 			public State next(Event event) {
 				switch (event) {
 					case START_LOCK : return LOCKED;
 					case START_FIRE : return FIRE;
					default 		: return NORMAL;
 				}
 			}
 		},
 		
 		LOCKED("locked"){
 			public State next(Event event) {
 				switch (event) {
 					case END_LOCK 	: return NORMAL;
 					case START_FIRE : return FIRE;
 					default 		: throw new IllegalArgumentException("Illegal transition");
 				}
 			}
 		},
 		
 		FIRE("fire") {
 			public State next(Event event) {
 				switch (event) {
 					case END_FIRE 	: return NORMAL;
 					case START_LOCK : return FIRE;
 					case END_LOCK 	: return FIRE;
 					default 		: throw new IllegalArgumentException("Illegal transition");
 				}
 			}
 		};
 
 		private final String label;
 		
 		private State(String label) {
 			this.label = label;
 		}	
 		
 		public String getLabel() {
 			return label;
 		}
 		
 		public abstract State next(Event event) ;
 	}
 	/**
 	 * The current state 
 	 */
 	private State currentState;
 	
 	/**
 	 * This is the APAM internal property used to notify state changes
 	 */
 	@SuppressWarnings("unused")
 	private String stateProperty;
 
 	private CoreColorLightSpec light;
 	
 
 	public SecurityStateManager() {
 	}
 	
 	@SuppressWarnings("unused")
 	private void start(Instance instance) {
 		ApamManagers.addPropertyManager(this);
 
 		currentState 	= State.NORMAL;
 		stateProperty 	= currentState.getLabel();
 	}
 
 	
 	private void transition(Event event) {
 		currentState 	= currentState.next(event);
 		stateProperty 	= currentState.getLabel();
 		
 		if (light != null) {
 			switch (currentState) {
 			case NORMAL:
				light.setBlue();
 				break;
 			case LOCKED:
 				light.setGreen();
 				break;
 			case FIRE:
 				light.setRed();
 				break;
 			}
 
 		}
 	}
 
 	@Override
 	public void attributeChanged(Component component, String attr, String newValue, String oldValue) {
 		
 		Event event = null;
 		
 		if (component.getName().equals("fireManagementDomain") && attr.equals("emergency")) {
 			event =  newValue.equals("emergency") ? Event.START_FIRE : Event.END_FIRE;
 		} 
 		
 		if (component.getName().equals("buildingAccessDomain") && attr.equals("locked")) {
 			event =  newValue.equals("true") ? Event.START_LOCK : Event.END_LOCK;
 		} 
 
 		if (event == null)
 			return;
 		
 		transition(event);
 	}
 
 	@Override
 	public void attributeAdded(Component component, String attr, String newValue) {
 		attributeChanged(component, attr, newValue, null);
 	}
 
 	@Override
 	public void attributeRemoved(Component component, String attr,	String oldValue) {
 		attributeChanged(component, attr, null, oldValue);
 	}
 
 
 	@Override
 	public String getName() {
 		return "SecurityStateManager";
 	}
 
 	@Override
 	public int getPriority() {
 		return 0;
 	}
 
 
 	@Override
 	public void newComposite(ManagerModel model, CompositeType composite) {
 	}
 
 
 	
 	
 	
 }
