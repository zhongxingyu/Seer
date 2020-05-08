 package net.beadsproject.beads.play;
 
 import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
 import net.beadsproject.beads.core.BeadArray;
 import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.RTInput;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;
 
 public class DefaultEnvironmentFactory extends EnvironmentFactory {
 
 	public Environment createEnvironment(AudioContext ac) {
 		Environment e = new Environment();
 		e.ac = ac;
 		//set up clock as a pathway
 		Clock c = new Clock(e.ac, 500f);
 		c.setName("master clock");
 		e.ac.out.addDependent(c);
 		BeadArray clockListener = new BeadArray();
 		c.addMessageListener(clockListener);
 		e.pathways.put(c.getName(), clockListener);
 		//and also as an object
 		e.elements.put(c.getName(), c);
 		//set up audio input as object in environment
		RTInput in = new RTInput(e.ac, e.ac.getAudioFormat());
 //		Gain in = new Gain(e.ac, 2);
 		e.elements.put("in", in);
 		//set up in and main out as object in channels
 
 		//try with delay
 //		TapIn tin = new TapIn(e.ac, 10000);
 //		TapOut tout = new TapOut(e.ac, tin, 50f);
 //		e.ac.out.addInput(tout);
 //		e.channels.put("out", tin);
 
 		e.channels.put("out", e.ac.out);
 		return e;
 	}
 }
