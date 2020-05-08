 package roundrobin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import backbone.Attribute;
 import backbone.Grouping;
 import backbone.StringAttribute;
 import backbone.Unit;
 import backbone.UnitAttribute;
 
 public class Player implements Unit {
 
 	private UnitAttribute<Team> _team;
 	private StringAttribute _name;
 	private Tournament _t;
 	
 	public Player(Tournament t, String name){
 		_t = t;
 		_name = new StringAttribute("Name", name);
 		_team = new UnitAttribute<Team>("Team", null, t.getTeams());
 		
 	}
 	
 	public void setTeam(Team t){
 		_team.att = t;
 	}
 	@Override
 	public boolean deleteFromGrouping() {
 		return _t.getPlayers().deleteMember(this);
 	}
 
 	@Override
 	public List<Attribute> getAttributes() {
 		ArrayList<Attribute> atts = new ArrayList<Attribute>();
 		atts.add(_name);
 		atts.add(_team);
 		return atts;
 	}
 
 	@Override
 	public Grouping getMemberOf() {
 		return _t.getPlayers();
 	}
 
 	@Override
 	public String getName() {
		return _name.getAttribute();
 	}
 
 	/**
 	 * Sets the attributes.
 	 */
 	@Override
 	public void setAttribute(Attribute attribute) {
 		if(attribute.getType() == Attribute.Type.STRING)
 			_name = (StringAttribute)attribute;
 		else if(attribute.getType() == Attribute.Type.UNIT){
 			Team t = (Team)((UnitAttribute<Team>)attribute).att;
 			if(t == null){
 				if(_team.att != null){
 					_team.att.removePlayer(this);
 				}
 				_team = (UnitAttribute<Team>)attribute;
 			}
 			else{
 				if(_team.att != null){
 					_team.att.removePlayer(this);
 				}
 				t.addPlayer(this);
 				_team = (UnitAttribute<Team>)attribute;
 			}
 		}
 
 	}
 
 	@Override
 	public void setMemberOf(Grouping<Unit> g) {
 	}
 
 	@Override
 	public void setName(String name) {
 		this._name = new StringAttribute("Name", name);
 		
 	}
 
 }
