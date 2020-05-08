 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.data.buffers.SineBuffer;
 
 /**
  * 
  * Simple UGen that ramps between given values over a given duration (e.g., for portamento).
  * 
  * @author ben
  * @beads.category control
  */
 public class Glide extends UGen {
 
 	private float currentValue;
 	private float previousValue;
 	private float targetValue;
 	private int glideTime; //in samples
 	private int countSinceGlide;
 	private boolean gliding;
 	private boolean nothingChanged;
 	
 	public Glide(AudioContext context, float currentValue, float glideTimeMS) {
 		this(context, currentValue);
 		setGlideTime(glideTimeMS);
 	}
 	
 	public Glide(AudioContext context, float currentValue) {
 		super(context, 1);
 		this.currentValue = currentValue;
 		glideTime = (int)context.msToSamples(100f);
 		countSinceGlide = 0;
 		gliding = false;
 		nothingChanged = false;
 		outputInitializationRegime = OutputInitializationRegime.RETAIN;
 		outputPauseRegime = OutputPauseRegime.RETAIN;
 		bufOut[0] = new float[bufferSize];
 	}
 	
 	public Glide(AudioContext context) {
 		this(context, 0f);
 	}
 
 	public void setValue(float targetValue) {
 		this.targetValue = targetValue;
 		gliding = true;
 		nothingChanged = false;
 		countSinceGlide = 0;
 		previousValue = currentValue;
 	}
 	
 	public void setValueImmediately(float targetValue) {
 		currentValue = targetValue;
 		gliding = false;
 		nothingChanged = false;
 		countSinceGlide = 0;
 	}
 	
 	public void setGlideTime(float msTime) {
 		glideTime = (int)context.msToSamples(msTime);
 	}
 	
 	public float getGlideTime() {
 		return (float)context.samplesToMs(glideTime);
 	}
 
 	@Override
 	public void calculateBuffer() {
 		if(!nothingChanged) {
 			nothingChanged = true;
 			for(int i = 0; i < bufferSize; i++) {
 				if(gliding) {
					if(glideTime <= 0f) {
						gliding = false;
						bufOut[0][i] = previousValue = targetValue;
						nothingChanged = false;
					} else if(countSinceGlide >= glideTime) {
 						gliding = false;
 						bufOut[0][i] = previousValue = targetValue;
 					} else {
 						float offset = ((float)countSinceGlide / glideTime);
 						bufOut[0][i] = currentValue = offset * targetValue + (1f - offset) * previousValue;
 						nothingChanged = false;
 					}
 					countSinceGlide++;
 				} else {
 					bufOut[0][i] = currentValue;
 				}
 			}
 		}
 	}
 
 }
