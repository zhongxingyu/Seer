 package yuuki.entity;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import yuuki.action.Action;
 import yuuki.content.Mergeable;
 import yuuki.ui.Interactable;
 import yuuki.util.InvalidIndexException;
 
 /**
  * Generates entities based on their names.
  */
 public class EntityFactory implements
 Mergeable<Map<String, Character.Definition>> {
 	
 	/**
 	 * The name of the EntityDefinition for a player character.
 	 */
 	private static final String PLAYER_CHARACTER_NAME = "__PLAYER";
 	
 	/**
 	 * All defined entities as read from the entity definitions file. When a
 	 * definition is used to create an instance, it must be cloned, or else
 	 * multiple instances of characters will share the same references to
 	 * stats.
 	 */
 	private Map<String, Deque<Character.Definition>> definitions;
 	
 	/**
 	 * Allocates a new EntityFactory. The definition files are read and the
 	 * list of base actions is populated.
 	 */
 	public EntityFactory() {
 		definitions = new HashMap<String, Deque<Character.Definition>>();
 	}
 	
 	/**
 	 * Adds a definition to this EntityFactory.
 	 * 
 	 * @param name The character's name.
 	 * @param hp The base hit points.
 	 * @param hpg The hit points gained per level.
 	 * @param mp The base mana points.
 	 * @param mpg The mana points gained per level.
 	 * @param str The base strength.
 	 * @param strg The strength gained per level.
 	 * @param def The base defense.
 	 * @param defg The defense gained per level.
 	 * @param agl The base agility.
 	 * @param aglg The agility gained per level.
 	 * @param acc The base accuracy.
 	 * @param accg The accuracy gained per level.
 	 * @param mag The base magic ability.
 	 * @param magg The magic ability gained per level.
 	 * @param luk The base luck.
 	 * @param lukg The luck gained per level.
 	 * @param moves The actions that this character may perform.
 	 * @param overworldArt The path to the overworld art.
 	 * @param xp The experienced gained when this character is defeated.
 	 */
 	public void addDefinition(String name, int hp, int hpg, int mp, int mpg,
 			int str, int strg, int def, int defg, int agl, int aglg, int acc,
 			int accg, int mag, int magg, int luk, int lukg, Action[] moves,
 			String overworldArt, int xp) {
 		Character.Definition cd = new Character.Definition();
 		cd.name = name;
 		cd.hp = new VariableStat("health", hp, hpg);
 		cd.mp = new VariableStat("mana", mp, mpg);
 		cd.str = new Stat("strength", str, strg);
 		cd.def = new Stat("defense", def, defg);
 		cd.agl = new Stat("agility", agl, aglg);
 		cd.acc = new Stat("accuracy", acc, accg);
 		cd.mag = new Stat("magic", mag, magg);
 		cd.luk = new Stat("luck", luk, lukg);
 		cd.overworldArt = overworldArt;
 		cd.moves = moves;
 		cd.xp = xp;
 		addDefinition(cd);
 	}
 	
 	/**
 	 * Adds a definition to this EntityFactory.
 	 * 
 	 * @param def The definition to add.
 	 */
 	public void addDefinition(Character.Definition def) {
 		String index = def.name.toLowerCase();
 		Deque<Character.Definition> d = definitions.get(index);
 		if (d == null) {
 			d = new ArrayDeque<Character.Definition>();
 			definitions.put(index, d);
 		}
 		d.push(def);
 	}
 	
 	@Override
 	public void merge(Map<String, Character.Definition> content) {
 		for (Character.Definition def : content.values()) {
 			addDefinition(def);
 		}
 	}
 	
 	@Override
 	public void subtract(Map<String, Character.Definition> content) {
 		for (Character.Definition def : content.values()) {
 			String index = def.name.toLowerCase();
 			Deque<Character.Definition> d = definitions.get(index);
 			if (d != null) {
 				d.remove(def);
 				if (d.isEmpty()) {
					definitions.remove(d);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates a NonPlayerCharacter of a specified level.
 	 * 
 	 * @param name The name of the NPC; must match one in the definitions file.
 	 * @param level The level of the NPC. This must be at least 1.
 	 * 
 	 * @return An NPC with the given name and level.
 	 * 
 	 * @throws InvalidIndexException If the given name does not refer to an
 	 * existing NPC.
 	 */
 	public NonPlayerCharacter createNpc(String name, int level) throws
 	InvalidIndexException {
 		Character.Definition d = getDefinition(name);
 		NonPlayerCharacter m;
 		m = new NonPlayerCharacter(d.name, level, d.moves, d.hp, d.mp, d.str,
 				d.def, d.agl, d.acc, d.mag, d.luk, d.overworldArt, d.xp);
 		return m;
 	}
 	
 	/**
 	 * Creates the player character.
 	 * 
 	 * @param name The name to give the player character.
 	 * @param level The starting level of the player character.
 	 * @param ui A reference to the game's user interface.
 	 * 
 	 * @return An instance of PlayerCharacter with the given name and level.
 	 */
 	public PlayerCharacter createPlayer(String name, int level,
 			Interactable ui) {
 		Character.Definition d = null;
 		try {
 			d = getDefinition(PLAYER_CHARACTER_NAME);
 		} catch (InvalidIndexException e) {
 			// should never happen
 			throw new Error("PLAYER_CHARACTER_NAME is invalid", e);
 		}
 		PlayerCharacter m;
 		m = new PlayerCharacter(name, level, d.moves, d.hp, d.mp, d.str, d.def,
 				d.agl, d.acc, d.mag, d.luk, d.overworldArt, ui);
 		return m;
 	}
 	
 	/**
 	 * Creates a random NPC from a set of names.
 	 * 
 	 * @param levelMin The minimum (inclusive) level of the NPC.
 	 * @param levelMax The maximum (inclusive) level of the NPC.
 	 * @param names The names to select from. One is chosen randomly from this
 	 * array. Each element must match one name from the definitions file. Set
 	 * this argument to null for any random entity.
 	 * 
 	 * @return A random NPC with one of the names given and with a level in the
 	 * given range.
 	 * 
 	 * @throws InvalidIndexException If one of the given names is invalid.
 	 */
 	public NonPlayerCharacter createRandomNpc(int levelMin, int levelMax,
 			String... names) throws InvalidIndexException {
 		Set<String> validNames = definitions.keySet();
 		String[] possibleNames =  validNames.toArray(new String[0]);
 		int nameInd = (int) Math.floor(Math.random() * possibleNames.length);
 		int diff = levelMax - levelMin;
 		int level = levelMin + (int) Math.floor(Math.random() * diff);
 		String name = possibleNames[nameInd];
 		NonPlayerCharacter entity = createNpc(name, level);
 		return entity;
 	}
 	
 	/**
 	 * Creates a number of random NPCs from a set of names.
 	 * 
 	 * @param size The number of NPCs to create.
 	 * @param levelMin The minimum (inclusive) level of the NPCs.
 	 * @param levelMax The maximum (inclusive) level of the NPCs.
 	 * @param names The names to select from. The NPC types are chosen randomly
 	 * from this array. Each element must match one name from the definitions
 	 * file. Set this argument to null for any random entities.
 	 * 
 	 * @return An array containing the specified number of random NPCs of the
 	 * given types and with levels in the given range.
 	 * 
 	 * @throws InvalidIndexException If one of the given names is invalid.
 	 */
 	public NonPlayerCharacter[] createRandomNpcTeam(int size, int levelMin,
 			int levelMax, String... names) throws InvalidIndexException {
 		NonPlayerCharacter[] entities = new NonPlayerCharacter[size];
 		for (int i = 0; i < size; i++) {
 			entities[i] = createRandomNpc(levelMin, levelMax, names);
 		}
 		return entities;
 	}
 	
 	/**
 	 * Returns an array containing the names of all defined entities.
 	 * 
 	 * @return The entities.
 	 */
 	public String[] getEntityNames() {
 		String[] names = definitions.keySet().toArray(new String[0]);
 		return names;
 	}
 	
 	/**
 	 * Gets an entity definition. The name is normalized and the definition is
 	 * deep-cloned to ensure that its contents are never shared between two
 	 * instances.
 	 * 
 	 * @param name The name of the entity.
 	 * @return The definition for the named entity.
 	 * @throws InvalidIndexException If the given name doesn't exist.
 	 */
 	private Character.Definition getDefinition(String name) throws
 	InvalidIndexException {
 		Deque<Character.Definition> dq = definitions.get(name.toLowerCase());
 		if (dq == null) {
 			throw new InvalidIndexException(name);
 		}
 		Character.Definition def = dq.peek();
 		return def.clone();
 	}
 	
 }
