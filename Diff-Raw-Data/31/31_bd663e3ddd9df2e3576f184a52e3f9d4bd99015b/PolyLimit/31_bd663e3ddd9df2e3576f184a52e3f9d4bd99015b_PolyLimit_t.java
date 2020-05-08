 package net.beadsproject.beads.ugens;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.UGen;
 
 public class PolyLimit extends UGen {
 
 	private int maxInputs;
 	private Queue<UGen> existingInputs;
 	
 	public PolyLimit(AudioContext context, int inouts, int maxInputs) {
 		super(context, inouts, inouts);
 		setMaxInputs(maxInputs);
 		existingInputs = new LinkedList<UGen>();
 	}
 	
 	public void addInput(UGen sourceUGen) {
		if(existingInputs.contains(sourceUGen)) {
			existingInputs.remove(sourceUGen);
			existingInputs.add(sourceUGen);
		} else {
			if(existingInputs.size() > maxInputs) {
				UGen deadUGen = existingInputs.poll();
				removeAllConnections(deadUGen);
				existingInputs.add(sourceUGen);
			}
			super.addInput(sourceUGen);
 		}
 	}
 	
 	public void removeAllConnections(UGen sourceUGen) {
 		super.removeAllConnections(sourceUGen);
 		existingInputs.remove(sourceUGen);
 	}
 
 	public int getMaxInputs() {
 		return maxInputs;
 	}
 
 	public void setMaxInputs(int maxInputs) {
 		this.maxInputs = maxInputs;
 	}
 
 	@Override
 	public void calculateBuffer() {
 		for(int i = 0; i < ins; i++) {
 			bufOut[i] = bufIn[i];	//TESTME
 		}
 	}
 
 }
