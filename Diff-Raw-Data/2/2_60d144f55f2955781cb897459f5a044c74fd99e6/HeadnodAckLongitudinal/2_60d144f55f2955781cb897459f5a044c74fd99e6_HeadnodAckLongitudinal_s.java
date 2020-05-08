 package rhythm;
 
 import static rhythm.Features.*;
 import static rhythm.MathUtil.invLogit;
 import static rhythm.MathUtil.logit;
 
 public class HeadnodAckLongitudinal implements Processor {
 	private double sessionB = DefaultSessionB;
 	private double lastB = DefaultLastB;
 	private double allianceB = DefaultAllianceB;
 	private double sessionAllianceB = DefaultSessionAllianceB;
 	
 	public static final double DefaultSessionB = 0.06;
	public static final double DefaultLastB = -0.04;
 	public static final double DefaultAllianceB = -0.28;
 	public static final double DefaultSessionAllianceB = 0.06;
 	
 	public HeadnodAckLongitudinal sessionB(double b) {
 		sessionB = b;
 		return this;
 	}
 	
 	public HeadnodAckLongitudinal lastB(double b) {
 		lastB = b;
 		return this;
 	}
 	
 	public HeadnodAckLongitudinal allianceB(double b) {
 		allianceB = b;
 		return this;
 	}
 	
 	public HeadnodAckLongitudinal sessionAllianceB(double b) {
 		sessionAllianceB = b;
 		return this;
 	}
 	
 	public void process(Context c, Sentence s) {
 		for (Behavior b : s.behaviors())
 			if ("headnod".equals(b) && b.at(0) && s.is(TURN_START))
 				adjust(c, b);
 	}
 	
 	protected void adjust(Context c, Behavior b) {
 		double p = b.probability();
 		if ((p > 0) && (p < 1)) {
 			int sessions = c.get(SESSION_INDEX, 0);
 			int last = c.is(LAST_SESSION) ? 1 : 0;
 			double alliance = c.get(ALLIANCE, 0.0);
 			double a = c.get(LONGITUDINAL_EFFECT_MULTIPLIER, 1.0);
 			b.probability(invLogit(logit(p)
 				+ sessionB*sessions*a 
 				+ lastB*last*a
 				+ allianceB*alliance*a
 				+ sessionAllianceB*sessions*alliance*a));
 		}
 	}
 }
