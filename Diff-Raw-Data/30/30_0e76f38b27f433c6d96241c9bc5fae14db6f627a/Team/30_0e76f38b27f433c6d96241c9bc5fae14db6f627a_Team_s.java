 package roundrobin;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import backbone.Attribute;
 import backbone.CompetitiveUnit;
 import backbone.Grouping;
 import backbone.GroupingAttribute;
 import backbone.IntAttribute;
 import backbone.StringAttribute;
 import backbone.Unit;
 import backbone.UnitAttribute;
 
 public class Team implements CompetitiveUnit {
 
 	private Tournament _t;
 	private StringAttribute _name;
 	private IntAttribute _gamesWon;
 	private IntAttribute _gamesLost;
 	private GroupingAttribute<Player> _players;
 	private HashMap<Team, Integer> _teamsFaced;
 	
 	public Team(Tournament t, String name){
 		_t = t;
 		_name = new StringAttribute("Name", name);
 		_gamesWon = new IntAttribute("Wins", 0);
 		_gamesLost = new IntAttribute("Losses", 0);
		_players = new GroupingAttribute<Player>("Players", _t.getPlayers());
 		_teamsFaced = new HashMap<Team, Integer>();
 	}
 	
 	public void addFacedTeam(Team t, int i){
 		_teamsFaced.put(t, i);
 	}
 	
 	public void removeFacedTeam(Team t){
 		_teamsFaced.remove(t);
 	}
 	
 	public boolean hasfacedBefore(Team t, int i){
 		Integer whenFaced = Integer.MAX_VALUE;
 		if(_teamsFaced.containsKey(t)){
 			whenFaced = _teamsFaced.get(t);
 		}
 		return i > whenFaced;
 	}
 	
 	@Override
 	public boolean deleteFromGrouping() {
 		return _t.getTeams().deleteMember(this);
 	}
 
 	@Override
 	public List<Attribute> getAttributes() {
 		ArrayList<Attribute> atts = new ArrayList<Attribute>();
 		atts.add(_name);
 		atts.add(_gamesWon);
 		atts.add(_gamesLost);
 		atts.add(_players);
 		return atts;
 	}
 
 	@Override
 	public Grouping getMemberOf() {
 		return _t.getTeams();
 	}
 
 	@Override
 	public String getName() {
 		return _name.value;
 	}
 
 	public void setPlayers(){
 		for(Player p : _players.getMembers()){
 			addPlayer(p);
 		}
 	}
 	@Override
 	public void setAttribute(Attribute attribute) {
 		if(attribute.getType() == Attribute.Type.STRING)
 			_name = (StringAttribute)attribute;
 		else if(attribute.getType() == Attribute.Type.GROUPING){
 			_players = (GroupingAttribute<Player>)attribute;
 			setPlayers();
 		}
 		else if(attribute.getType() == Attribute.Type.INT){
 			IntAttribute att = (IntAttribute)attribute;
 			if(att.getTitle().equals("Wins")){
 				_gamesWon = att;
 			}else if(att.getTitle().equals("Losses")) _gamesLost = att;
 		}
 
 	}
 
 	@Override
 	public void setMemberOf(Grouping<Unit> g) {
 		// DO NOTHING
 
 	}
 
 	public void addPlayer(Player player) {
 		if(!hasPlayer(player)){
 			this._players.getMembers().add(player);
 			player.setTeam(this);
 		}
 	}
 	
 	public void removePlayer(Player player){
 		if(hasPlayer(player)) this._players.getMembers().remove(player);
 	}
 
 	public boolean hasPlayer(Player player) {
 		return this._players.getMembers().contains(player);
 	}
 
 	@Override
 	public void setName(String name) {
 		this._name = new StringAttribute("Name", name);
 		
 	}
 
 }
