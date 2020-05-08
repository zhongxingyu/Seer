 package PostProcessors;
 
 import java.awt.Color;
 
 import Common.ColorOutput;
 import Common.FeatureList;
 import LightDJGUI.GenericKnob;
 import Visualizors.RGBGradientLinear;
 
 /**
  * This post processor takes care of white bursts and the "emergency lighting" commands.
  * @author steve
  *
  */
 public class Blackout extends PostProcessor {
 
 	protected GenericKnob fallKnob;
 	protected GenericKnob riseKnob;
 	protected boolean active;
 	protected float brightness;
 	
 	protected static float BRIGHTNESS_HIGH_CUTOFF = 0.98f;
 	protected static float BRIGHTNESS_LOW_CUTOFF = 0.02f;
 	
 	protected static float K_GAIN = 30.0f;
 	
 	
 	public Blackout(double updatesPerSecond) {
 		super(updatesPerSecond);
 		brightness = 1.0f;
 	}
 
 	@Override
 	public String getName() {
 		return "Blackout";
 	}
 
 	@Override
 	public void init() {
 		
 		active = false;
 		
 		// Make a user control
		fallKnob = new GenericKnob(0.4f, scale(40), "Fall");
		riseKnob = new GenericKnob(0.4f, scale(40), "Rise");
 		requestUserControl(fallKnob);
 		requestUserControl(riseKnob);
 		
 	}
 
 	@Override
 	public void postProcess(ColorOutput colorOutput, FeatureList featureList) {
 		
 		float delta;
 		float gain;
 		float input;
 		
 		// Currently not active. Should it became active?
 		if (((Double) featureList.getFeature("KEY_BACKSPACE")) == 1.0) {
 			// ACTIVATE!
 			input = -1.0f;
 			gain = fallKnob.getValue();
 		} else {
 			input = 1.0f;
 			gain = riseKnob.getValue();
 		}
 		
 		// Compute the next brightness!
 		delta = (float) (K_GAIN * gain / UPDATES_PER_SECOND) * input;
 		brightness += delta;
 		// Make sure we don't go out of [0.0, 1.0]
 		if (brightness > 1.0f) {
 			brightness = 1.0f;
 		} else if (brightness < 0.0f) {
 			brightness = 0.0f;
 		}
 		
 		
 		if (brightness <= BRIGHTNESS_HIGH_CUTOFF) {
 			active = true;
 		} else {
 			active = false;
 		}
 		
 		// Continue a burst that is currently running
 		if (active) {
 			if (brightness >= BRIGHTNESS_LOW_CUTOFF) {
 				for(int light = 0; light < ColorOutput.NUM_FRONT_RGB_PANELS*ColorOutput.NUM_LEDS_PER_RGB_BOARD; light++) {
 					colorOutput.rgbLightsFront[light] = RGBGradientLinear.linearGradient(Color.BLACK, colorOutput.rgbLightsFront[light], brightness);
 				}
 				for(int light = 0; light < ColorOutput.NUM_REAR_RGB_PANELS*ColorOutput.NUM_LEDS_PER_RGB_BOARD; light++) {
 					colorOutput.rgbLightsRear[light] = RGBGradientLinear.linearGradient(Color.BLACK, colorOutput.rgbLightsRear[light], brightness);
 				}
 				for(int light = 0; light < ColorOutput.NUM_UV_LIGHTS; light++) {
 					colorOutput.uvLights[light] *= brightness;
 					colorOutput.whiteLights[light] *= brightness;
 				}
 				
 				
 			} else {
 				// If we're basically dark, just turn everything off!
 				colorOutput.allOff();
 			}
 		}
 		
 	}
 
 	@Override
 	public boolean isActive() {
 		return active;
 	}
 
 
 }
