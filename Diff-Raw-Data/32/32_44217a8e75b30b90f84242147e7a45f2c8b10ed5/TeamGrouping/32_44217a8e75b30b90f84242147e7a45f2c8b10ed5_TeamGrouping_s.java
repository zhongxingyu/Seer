 package roundrobin;
 
 import java.util.List;
 
 import plugin1.MyCategory;
 
 public class TeamGrouping extends MyCategory<Team> {
 
 	private Tournament _t;
 	
 	public TeamGrouping(Tournament t, String name) {
 		super(name);
 		_t = t;
 	}
 	public TeamGrouping(Tournament t, String name, List<Team> teams) {
 		super(name);
 		_t = t;
 		for(Team te : teams){
 			this.addMember(te);
 		}
 	}
 
 	@Override
 	public Team getBlank() {
 		return new Team(_t, "");
 	}
<<<<<<< HEAD
=======

>>>>>>> 55df286c4cdb3d6cc03cc13bd49f3ca598666edf
 }
