 package PostProcessors;
 
 import java.awt.Color;
 
 import Common.ColorOutput;
 import Common.FeatureList;
 import LightDJGUI.ColorKnob;
 import LightDJGUI.GenericKnob;
 import Visualizors.RGBGradientLinear;
 
 /**
  * This post processor takes care of white bursts and the "emergency lighting" commands.
  * @author steve
  *
  */
 public class WhiteBurst extends PostProcessor {
 
 	protected GenericKnob lengthKnob;
 	protected GenericKnob heatKnob;
 	protected ColorKnob colorKnob;
 	protected boolean active;
 	protected boolean highPower;
 	protected long startTime;
 	
 	
 	// White burst
 	protected long whiteBurstTime;	// milliseconds
 	
 	public WhiteBurst(double updatesPerSecond) {
 		super(updatesPerSecond);
 		
 	}
 
 	@Override
 	public String getName() {
 		return "Burst";
 	}
 
 	@Override
 	public void init() {
 		
 		active = false;
 		highPower = false;
 		whiteBurstTime = 500;	
 		
 		// Make a user control
		lengthKnob = new GenericKnob(0.25f, scale(40), "Time");
		heatKnob = new GenericKnob(1.0f, scale(40), "Heat");
		colorKnob = new ColorKnob(0.0f, scale(40), "Color");
 		requestUserControl(lengthKnob);
 		requestUserControl(heatKnob);
 		requestUserControl(colorKnob);
 		
 	}
 
 	@Override
 	public void postProcess(ColorOutput colorOutput, FeatureList featureList) {
 		
 		
 		// Currently not active. Should it became active?
 		if (((Double) featureList.getFeature("KEY_ENTER")) == 1.0 || ((Double) featureList.getFeature("KEY_F5")) == 1.0 || ((Double) featureList.getFeature("KEY_F8")) == 1.0) {
 			// ACTIVATE!
 			active = true;
 			if (((Double) featureList.getFeature("KEY_F5")) == 1.0) {
 				highPower = true;
 			}
 			startTime = System.currentTimeMillis();
 		}
 		
 		
 		// Continue a burst that is currently running
 		if (active) {
 			
 			// Get the timing parameters
 			long now = System.currentTimeMillis();
 			long delta = (now - startTime);
 			
 			whiteBurstTime = (long) Math.round(lengthKnob.getValue() * 2000);
 			
 			// See if we're done
 			if (delta > whiteBurstTime) {
 				active = false;
 				highPower = false;
 				return;
 			}
 			
 			// Not done - still fading! Compute how much.
 			double alpha = (double) delta / whiteBurstTime;
 			Color c = Color.getHSBColor(colorKnob.getValue(), 1.0f - heatKnob.getValue(), 1.0f);
 			
 			for(int light = 0; light < ColorOutput.NUM_FRONT_RGB_PANELS*ColorOutput.NUM_LEDS_PER_RGB_BOARD; light++) {
 				colorOutput.rgbLightsFront[light] = RGBGradientLinear.linearGradient(c, colorOutput.rgbLightsFront[light], alpha);
 			}
 
 			if (highPower) {
 				for(int light = 0; light < ColorOutput.NUM_UVWHITE_PANELS; light++) {
 					colorOutput.uvLights[light] = (1 - alpha) * 1.0 + alpha * colorOutput.uvLights[light];
 					colorOutput.whiteLights[light] = (1 - alpha) * 1.0 + alpha * colorOutput.whiteLights[light];
 				}	
 			}
 		}
 		
 	}
 
 	@Override
 	public boolean isActive() {
 		return active;
 	}
 
 
 }
