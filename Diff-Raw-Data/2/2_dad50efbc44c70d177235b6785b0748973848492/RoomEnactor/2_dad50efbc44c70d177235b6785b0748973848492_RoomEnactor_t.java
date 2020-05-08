 package context.apps.demos.roomlight;
 
 import context.arch.discoverer.ComponentDescription;
 import context.arch.discoverer.component.NonConstantAttributeElement;
 import context.arch.discoverer.query.AbstractQueryItem;
 import context.arch.discoverer.query.ElseQueryItem;
 import context.arch.discoverer.query.ORQueryItem;
 import context.arch.discoverer.query.RuleQueryItem;
 import context.arch.discoverer.query.comparison.AttributeComparison;
 import context.arch.enactor.Enactor;
 import context.arch.enactor.EnactorReference;
 import context.arch.service.helper.ServiceInput;
 import context.arch.storage.AttributeNameValue;
 import context.arch.storage.Attributes;
 import context.arch.widget.Widget;
 import context.arch.widget.Widget.WidgetData;
 
 public class RoomEnactor extends Enactor {
 	
 	public static final short BRIGHTNESS_THRESHOLD = 100;
 
 	@SuppressWarnings("serial")
 	public RoomEnactor(AbstractQueryItem<?,?> inWidgetQuery, AbstractQueryItem<?,?> outWidgetQuery) {
 		super(inWidgetQuery, outWidgetQuery, "light", "");
 		
 		/*
 		 * Set up for enactor references, one for each outcome
 		 */
 		
 		// light off
 		AbstractQueryItem<?, ?> offQI = 
 			new ORQueryItem(
 					RuleQueryItem.instance(
							new NonConstantAttributeElement(AttributeNameValue.instance("presence", 0))), // equal to 0; no one in the room, OR
 					RuleQueryItem.instance(
 							new NonConstantAttributeElement(AttributeNameValue.instance("brightness", BRIGHTNESS_THRESHOLD)), 
 							new AttributeComparison(AttributeComparison.Comparison.GREATER)) // brightness more than BRIGHTNESS_THRESHOLD
 					);
 		EnactorReference er = new RoomEnactorReference( 
 				offQI, 
 				LightWidget.LIGHT_OFF);
 		er.addServiceInput(new ServiceInput("LightService", "lightOff"));
 		addReference(er);
 		
 		// light on, and brightness dependent
 		er = new RoomEnactorReference( 
 				new ElseQueryItem(offQI), 
 				LightWidget.LIGHT_ON);
 		er.addServiceInput(new ServiceInput("LightService", "lightOn", 
 				new Attributes() {{
 					addAttribute("light", Integer.class);
 				}}));
 		addReference(er);
 		
 		start();
 	}
 	
 	private int light;
 	
 	public int getLight() {
 		return light;
 	}
 	
 	private class RoomEnactorReference extends EnactorReference {
 
 		public RoomEnactorReference(AbstractQueryItem<?,?> conditionQuery, String outcomeValue) {
 			super(RoomEnactor.this, conditionQuery, outcomeValue);
 		}
 		
 		private double LIGHT_SCALE = Math.pow(BRIGHTNESS_THRESHOLD, 0.33);
 		
 		@Override
 		protected Attributes conditionSatisfied(ComponentDescription inWidgetState, Attributes outAtts) {
 			long timestamp = outAtts.getAttributeValue(Widget.TIMESTAMP);
 			WidgetData data = new WidgetData("LightWidget", timestamp);
 			
 			if (outcomeValue == LightWidget.LIGHT_ON) {
 				/*
 				 * "Proprietary or complicated" way to calculate light voltage level from sensed brightness
 				 * Actually, using Steven's power law (http://en.wikipedia.org/wiki/Steven%27s_power_law) for light perception
 				 * psi = k*I^0.33
 				 * Scaled to 10
 				 */
 				short brightness = inWidgetState.getAttributeValue(RoomWidget.BRIGHTNESS);
 				light = (int)(10*Math.pow(BRIGHTNESS_THRESHOLD - brightness, 0.33) / LIGHT_SCALE); // brightness will never be lower than BRIGHTNESS_THRESHOLD
 			}
 			else {
 				light = 0;
 			}			
 				        
 	        /*
 	         * Note that outcomeValue would not equal the light level, so user may not be able to ask about specific level
 	         * TODO consider whether and how to support functional relationships
 	         */
 			
 			data.setAttributeValue(LightWidget.LIGHT, light);
 			
 			outAtts.putAll(data.toAttributes());
 	        return outAtts;
 		}
 		
 	}
 
 }
