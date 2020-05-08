 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.data.buffers.SineBuffer;
 
 public class Glide extends UGen {
 
 	private float currentValue;
 	private float targetValue;
 	private int glideTime; //in samples
 	private int countSinceGlide;
 	private boolean gliding;
 	private boolean nothingChanged;
 	
 	public Glide(AudioContext context, float currentValue) {
 		super(context, 1);
 		this.currentValue = currentValue;
		targetValue = 0f;
 		glideTime = (int)context.msToSamples(100f);
 		countSinceGlide = 0;
 		gliding = false;
 		nothingChanged = false;
 	}
 	
 	public Glide(AudioContext context) {
 		this(context, 0f);
 	}
	
 	public void setValue(float targetValue) {
 		this.targetValue = targetValue;
 		gliding = true;
 		nothingChanged = false;
 		countSinceGlide = 0;
		currentValue = getValue();
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
 					if(countSinceGlide >= glideTime) {
 						gliding = false;
						bufOut[0][i] = currentValue = targetValue;
 					} else {
 						float offset = ((float)countSinceGlide / glideTime);
						bufOut[0][i] = offset * targetValue + (1f - offset) * currentValue;
 						nothingChanged = false;
 					}
 					countSinceGlide++;
 				} else {
 					bufOut[0][i] = currentValue;
 				}
 			}
 		}
 	}
 
 	public static void main(String args[]) {
 		AudioContext ac = new AudioContext();
 		final Glide g = new Glide(ac, 500) {
 			public void calculateBuffer() {
 				super.calculateBuffer();
 				System.out.println(getValue());
 			}
 		};
 		WavePlayer wp = new WavePlayer(ac, g, new SineBuffer().getDefault());
 		ac.out.addInput(wp);
 		ac.start();
 		Thread t = new Thread() {
 			public void run() {
 				while(true) {
 					g.setValue((float)Math.random() * 10000 + 100);
 					try {
						sleep(500);
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		t.start();
 	}
 }
