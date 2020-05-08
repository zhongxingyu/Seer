 package net.gmx.nosefish.fishysigns_cb.cbics.logic;
 
 import java.util.regex.Pattern;
 
 import net.gmx.nosefish.fishysigns.annotation.FishySignIdentifier;
 import net.gmx.nosefish.fishysigns.iobox.IOSignal;
 import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
 import net.gmx.nosefish.fishysigns_cb.cbics.CBBase3ISO;
 
 public class MC3036 extends CBBase3ISO {
 	@FishySignIdentifier
 	public static final Pattern[] regEx = {
 		null,
		Pattern.compile("\\[MC3036\\].*", Pattern.CASE_INSENSITIVE),
 		null,
 		null
 	};
 
 	
 	public MC3036(UnloadedSign sign) {
 		super(sign);
 	}
 
 	@Override
 	public String getCode() {
 		return "[MC3036]";
 	}
 
 	@Override
 	public String getName() {
 		return "D LEVL FLIPFLOP";
 	}
 
 	@Override
 	public String getHelpText() {
 		return "Logic gate: level-triggered D-FlipFlop. Input 1: clock. Input 2: D. Input 3: reset.";
 	}
 
 	@Override
 	protected void initializeIC() {
 		refresh();
 	}
 
 	@Override
 	public void handleDirectInputChange(IOSignal oldS, IOSignal newS) {
 		boolean clk = newS.getState(0);
 		boolean D = newS.getState(1);
 		boolean reset = newS.getState(2);
 		if (reset) {
 			updateOutput(IOSignal.L);
 		} else if (clk) {
            updateOutput(IOSignal.factory(D));
         }
 	}
 
 }
