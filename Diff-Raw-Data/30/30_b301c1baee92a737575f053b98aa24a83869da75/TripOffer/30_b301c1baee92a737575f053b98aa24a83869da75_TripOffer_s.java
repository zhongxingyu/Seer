 package models;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 
 @Entity
 public class TripOffer extends Trip {
 
    private static final long serialVersionUID = -386674185196292407L;
    public static Finder<Integer, TripOffer> find = new Finder<Integer, TripOffer>(Integer.class, TripOffer.class);

    public TripOffer() {
        super();
    }
    
    public List<TripMatch> getMatches()
    {
	return TripMatch.find.where().eq("tripOffer.id", this.getId()).findList();
    }
 
 }
