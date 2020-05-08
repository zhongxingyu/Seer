 package fowler.energybilling.sites;
 
 import java.util.Date;
 
 import fowler.energybilling.Dollars;
 import fowler.energybilling.Zone;
 
 public class ResidentialSite extends TimedSite {
 
 	private static final double FUEL = 0.0175;
 	private static final double TAX_RATE = 0.05;
 	public ResidentialSite(Zone zone) {
 		super(zone);
 	}
 
 	protected Dollars charge(int usage, Date start, Date end) {
 		Dollars result = calculateSummerWinterRate(usage, start, end);
		
		// @todo This is weird, figure out what this actually does.
		result = result.plus(result.times(TAX_RATE)); 
 
		return result
			.plus(new Dollars(usage * FUEL).times(1 + TAX_RATE))
			.round(2);
 	}
 }
