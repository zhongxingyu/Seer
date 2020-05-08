 package pl.edu.agh.two.mud.common.world.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
import com.sun.jndi.url.corbaname.corbanameURLContextFactory;

 import pl.edu.agh.two.mud.common.ICreature;
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.world.exception.NoCreatureWithSuchNameException;
 
 public class Field implements Serializable {
 
 	@Autowired
 	private Board board;
 
 	private String name;
 	private String description;
 
 	private int x;
 	private int y;
 
 	private List<IPlayer> players = new ArrayList<IPlayer>();
 
 	private List<ICreature> creatures = new ArrayList<ICreature>();
 
 	public Field(int y, int x, String name, String description) {
 		this.x = x;
 		this.y = y;
 		this.name = name;
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public boolean addPlayer(IPlayer player) {
 		addCreature(player);
 		return players.add(player);
 	}
 
 	public boolean removePlayer(IPlayer player) {
 		removeCreature(player);
 		return players.remove(player);
 	}
 
 	public List<IPlayer> getPlayers() {
 		return players;
 	}
 
 	public boolean addCreature(ICreature creature) {
 		return creatures.add(creature);
 	}
 
 	public boolean removeCreature(ICreature creature) {
 		return creatures.remove(creature);
 	}
 
 	public List<ICreature> getCreatures() {
 		return creatures;
 	}
 
 	public IPlayer getPlayerByName(String playerName) throws NoCreatureWithSuchNameException {
 		for (IPlayer player : players) {
 			if (player.getName().equals(playerName)) {
 				return player;
 			}
 		}
 		throw new NoCreatureWithSuchNameException(playerName);
 	}
 	
 	public ICreature getCreatureByName(String creatureName) throws NoCreatureWithSuchNameException {
 		for (ICreature creature : creatures) {
 			if (creature.getName().equals(creatureName)) {
 				return creature;
 			}
 		}
 		throw new NoCreatureWithSuchNameException(creatureName);
 	}
 
 	public String getFormattedFieldSummary() {
 		String result = "Lokacja: " + getName() + "\n" + getDescription() + "\nPostacie na polu: ";
 		for (IPlayer player : getPlayers()) {
 			result += player.getName() + ", ";
 		}
 		if (getPlayers().size() > 0) {
 			result = result.substring(0, result.length() - 2);
 		}
 
 		if (getCreatures().size() > 0) {
 			result += "\nPotwory na polu: ";
 			for (ICreature creature : getCreatures()) {
 				if (creature instanceof IPlayer) {
 					continue;
 				}
 				result += creature.getName() + ", ";
 			}
 
 			result = result.substring(0, result.length() - 2);
 		}
 
 		result += "\nWidzisz droge w kierunku: ";
 		for (Direction direction : board.getPossibleDirections(this)) {
 			result += "\t" + direction.toString();
 		}
 
 		return result;
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 }
