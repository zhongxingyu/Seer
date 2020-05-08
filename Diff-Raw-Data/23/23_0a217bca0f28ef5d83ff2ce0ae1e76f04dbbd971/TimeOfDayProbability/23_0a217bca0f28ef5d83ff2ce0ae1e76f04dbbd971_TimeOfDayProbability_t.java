 package probability;
 
import collections.Hits;
import dtos.HitDto;
import dtos.VenueDto;
 
public class TimeOfDayProbability extends Probability {

	public TimeOfDayProbability(VenueDto venue, int startHour, int stopHour) {
		super(venue);
	}

	@Override
	public float getProbability(Hits hits) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean matches(HitDto hit) {
		// TODO Auto-generated method stub
		return false;
	}
	
 }
