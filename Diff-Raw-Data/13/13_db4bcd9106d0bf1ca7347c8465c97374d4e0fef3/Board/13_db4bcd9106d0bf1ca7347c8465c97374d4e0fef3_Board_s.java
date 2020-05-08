 package pl.edu.agh.two.mud.common.world.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.world.exception.NoPlayerWithSuchNameException;
 
public class Board {
 
 	private String name;
 	private String description;
 
 	private Field[][] fields;
 
 	private Map<IPlayer, Field> playersOnFields = new HashMap<IPlayer, Field>();
 	private Field startingField;
 	private int xAxisSize;
 	private int yAxisSize;
 
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
 
 	public Field[][] getFields() {
 		return fields;
 	}
 
 	public void setFields(Field[][] fields) {
 		this.fields = fields.clone();
 	}
 
 	public Field getStartingField() {
 		return startingField;
 	}
 
 	public void setStartingField(Field startingField) {
 		this.startingField = startingField;
 	}
 
 	public void setxAxisSize(int xAxisSize) {
 		this.xAxisSize = xAxisSize;
 	}
 
 	public void setyAxisSize(int yAxisSize) {
 		this.yAxisSize = yAxisSize;
 	}
 
 	public List<Direction> getPossibleDirections(Field field) {
 		List<Direction> result = new ArrayList<Direction>();
 		int x = field.getX();
 		int y = field.getY();
 		if (y > 0 && fields[y - 1][x] != null) {
 			result.add(Direction.N);
 		}
 		if (y < yAxisSize - 1 && fields[y + 1][x] != null) {
 			result.add(Direction.S);
 		}
 		if (x > 0 && fields[y][x - 1] != null) {
 			result.add(Direction.W);
 		}
 		if (x < xAxisSize - 1 && fields[y][x + 1] != null) {
 			result.add(Direction.E);
 		}
 
 		return result;
 	}
 
 	public boolean addPlayer(IPlayer player) {
 		playersOnFields.put(player, startingField);
 		return playersOnFields.containsKey(player);
 	}
 
 	public boolean removePlayer(IPlayer player) {
 		playersOnFields.put(player, null);
 		return playersOnFields.containsKey(player);
 	}
 
 	public Set<IPlayer> getPlayers() {
 		return playersOnFields.keySet();
 	}
 
 	public IPlayer getPlayerByName(String playerName) throws NoPlayerWithSuchNameException {
 		for (IPlayer player : playersOnFields.keySet()) {
 			if (player.getName().equals(playerName)) {
 				return player;
 			}
 		}
 		throw new NoPlayerWithSuchNameException(playerName);
 	}
 
 	public Field getPlayersPosition(IPlayer player) {
 		return playersOnFields.get(player);
 	}
 
 	public void setPlayersPosition(IPlayer player, Field field) {
 		playersOnFields.put(player, field);
 	}
 }
