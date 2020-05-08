 package models;
 
import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
import javax.persistence.OneToMany;
 
 @Entity
 public class TripOffer extends Trip {
 
	private static final long serialVersionUID = -386674185196292407L;
	public static Finder<Integer, TripOffer> find = new Finder<Integer, TripOffer>(
			Integer.class, TripOffer.class);

	@OneToMany
	private List<TripMatch> matches = new ArrayList<TripMatch>();

	public TripOffer() {
		super();
	}

	public List<TripMatch> getMatches() {
		return TripMatch.find.where().eq("tripOffer.id", this.getId())
				.findList();
	}
 
 }
