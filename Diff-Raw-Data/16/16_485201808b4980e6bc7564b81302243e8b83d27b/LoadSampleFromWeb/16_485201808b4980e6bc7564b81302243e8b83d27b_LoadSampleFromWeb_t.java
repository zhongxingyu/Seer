 package net.beadsproject.beads.data;
 
 import java.util.Random;
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.KillTrigger;
 import net.beadsproject.beads.ugens.Clock;
 import net.beadsproject.beads.ugens.Gain;
 import net.beadsproject.beads.ugens.SamplePlayer;
 
 
 public class LoadSampleFromWeb {
 
 	public static void main(String[] args) throws Exception {
 		
//		String prefix = "http://www.olliebown.com/files/music/Roland909";
		String prefix = "/Users/ollie/Music/Audio/crash test audio/chopped live sounds/Drum machines/Roland 909";
		
		Sample kik = SampleManager.sample(prefix + "/BT0A0A7.WAV");
		Sample hh = SampleManager.sample(prefix + "/HHCD8.WAV");
		Sample sn = SampleManager.sample(prefix + "/STAT3S7.WAV");
		
 		final Sample[] kit = new Sample[] {kik, hh, sn};
 		final Random rng = new Random();
 		
 		final AudioContext ac = new AudioContext();
 		final Clock c = new Clock(ac, 200f);
 		ac.out.addDependent(c);
 		c.addMessageListener(new Bead() {
 			public void messageReceived(Bead message) {
 				if(c.isBeat(4)) {
 					SamplePlayer sp = new SamplePlayer(ac, kit[0]);
 					ac.out.addInput(sp);
 				} else if(c.isBeat(2)) {
 					SamplePlayer sp = new SamplePlayer(ac, kit[rng.nextInt(3)]);
 					if(rng.nextFloat() < 0.1f) {
 						sp.getRateEnvelope().setValue(-1f);
 						sp.setPosition(sp.getBuffer().getLength());
 					}
 					ac.out.addInput(sp);
 				} else if(c.isBeat()) {
 					SamplePlayer sp = new SamplePlayer(ac, kit[rng.nextInt(2)]);
 					Gain g = new Gain(ac, 1, rng.nextFloat());
 					g.addInput(sp);
					sp.setKillListener(new KillTrigger(g));
 					ac.out.addInput(g);
 				}
 				if(c.getCount() % 8 == 0 && rng.nextFloat() < 0.1f) {
 					SamplePlayer sp = new SamplePlayer(ac, kit[1]);
 					sp.getRateEnvelope().setValue(2f);
 					Gain g = new Gain(ac, 1, rng.nextFloat());
 					g.addInput(sp);
					sp.setKillListener(new KillTrigger(g));
 					ac.out.addInput(g);
 				}
 			}
 		});
 		
 		ac.start();
 		
 	}
 }
