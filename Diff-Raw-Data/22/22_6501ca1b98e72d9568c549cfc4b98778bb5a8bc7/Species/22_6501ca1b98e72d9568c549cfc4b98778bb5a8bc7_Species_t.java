 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class Species {
 	private String name;
 	private ExpCurve curve;
 	private Type type1;
 	private Type type2;
 	private int baseHP;
 	private int baseAtk;
 	private int baseDef;
 	private int baseSpcAtk;
 	private int baseSpcDef;
 	private int baseSpe;
 	private int[] killExps;
 	private int pokedexNum;
 	private String[] abilities1;
 	private String[] abilities2;
 	private String dwAbility;
 	private int[] numAbilities;
 	private int evHP;
 	private int evAtk;
 	private int evDef;
 	private int evSpcAtk;
 	private int evSpcDef;
 	private int evSpe;
 	private int nameValue;
 
 	private static Species[] allSpecies;
 	private static final HashMap<String, Species> nameMap;
 
 	public Species(String s_name, ExpCurve s_curve, Type s_type1, Type s_type2,
 			int s_baseHP, int s_baseAtk, int s_baseDef, int s_baseSpcAtk,
 			int s_baseSpcDef, int s_baseSpe, int[] s_killExps, int s_pokedexNum,
 			String[] s_abilities1, String[] s_abilities2, String s_dwAbility, int[] s_numAbilities,
 			int s_evHP, int s_evAtk, int s_evDef, int s_evSpcAtk,
 			int s_evSpcDef, int s_evSpe, int s_nameValue) {
 		name = s_name;
 		curve = s_curve;
 		type1 = s_type1;
 		type2 = s_type2;
 		baseHP = s_baseHP;
 		baseAtk = s_baseAtk;
 		baseDef = s_baseDef;
 		baseSpcAtk = s_baseSpcAtk;
 		baseSpcDef = s_baseSpcDef;
 		baseSpe = s_baseSpe;
 		killExps = s_killExps;
 		pokedexNum = s_pokedexNum;
 		abilities1 = s_abilities1;
 		abilities2 = s_abilities2;
 		dwAbility = s_dwAbility;
 		numAbilities = s_numAbilities;
 		evHP = s_evHP;
 		evAtk = s_evAtk;
 		evDef = s_evDef;
 		evSpcAtk = s_evSpcAtk;
 		evSpcDef = s_evSpcDef;
 		evSpe = s_evSpe;
 		nameValue = s_nameValue;
 	}
 
 	// initialize allSpecies to be a list of all species
 	static {
 		allSpecies = new Species[Constants.numPokes + 1];
 		nameMap = new HashMap<String, Species>();
 		Species s;
 		String[] ts;
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					System.class.getResource("/resources/pokemon.txt")
 							.openStream()));
			for (int i = 1; i < allSpecies.length; i++) {
 				// s = new Species();
 				String s_name = in.readLine();
 				ExpCurve s_curve = ExpCurve.valueOf(in.readLine());
				ts = in.readLine().split("\\|");
 				Type s_type1, s_type2;
 				if (ts.length == 0) {
 					s_type1 = Type.NONE;
 					s_type2 = Type.NONE;
 				} else if (ts.length == 1) {
 					s_type1 = Type.valueOf(ts[0]);
 					s_type2 = Type.NONE;
 				} else {
 					s_type1 = Type.valueOf(ts[0]);
 					s_type2 = Type.valueOf(ts[1]);
 				}
 				int[] basestats = toIntArray(in.readLine());
 				int s_baseHP = basestats[0];
 				int s_baseAtk = basestats[1];
 				int s_baseDef = basestats[2];
 				int s_baseSpcAtk = basestats[3];
 				int s_baseSpcDef = basestats[4];
 				int s_baseSpd = basestats[5];
 				int[] s_killExps = new int[3];
 				s_killExps[0] = Integer.parseInt(in.readLine());
 				s_killExps[1] = Integer.parseInt(in.readLine());
 				s_killExps[2] = Integer.parseInt(in.readLine());
 				int s_pokedexNum = i;
 				String[] s_abilities1 = new String[3];
 				String[] s_abilities2 = new String[3];
 				int[] s_numAbilities = new int[3];
 				
				String[] abilsg3 = in.readLine().split("\\|", 2);
 				s_abilities1[0] = abilsg3[0];
 				s_abilities2[0] = abilsg3.length > 1 ? abilsg3[1] : "";
 				s_numAbilities[0] = abilsg3.length;
 				
				String[] abilsg4 = in.readLine().split("\\|",2);
 				s_abilities1[1] = abilsg4[0];
				s_abilities2[1] = abilsg4.length > 1 ? abilsg4[1] : "";
				s_numAbilities[1] = (abilsg4.length == 1 || abilsg4[1].isEmpty()) ? 1 : 2;
 				
				String[] abilsg5 = in.readLine().split("\\|", 3);
 				s_abilities1[2] = abilsg5[0];
 				s_abilities2[2] = abilsg5[1];
 				String s_dwAbility = abilsg5[2];
 				s_numAbilities[2] = 1 + (abilsg5[1].isEmpty() ? 0 : 1) + (abilsg5[2].isEmpty() ? 0 : 1);
 				
 				int[] evs = toIntArray(in.readLine());
 				int s_evHP = evs[0];
 				int s_evAtk = evs[1];
 				int s_evDef = evs[2];
 				int s_evSpd = evs[3];
 				int s_evSpcAtk = evs[4];
 				int s_evSpcDef = evs[5];
 				int s_nameValue = Integer.parseInt(in.readLine());
 
 				s = new Species(s_name, s_curve, s_type1, s_type2, s_baseHP,
 						s_baseAtk, s_baseDef, s_baseSpcAtk, s_baseSpcDef,
 						s_baseSpd, s_killExps, s_pokedexNum, s_abilities1,
 						s_abilities2, s_dwAbility, s_numAbilities, s_evHP, s_evAtk, s_evDef,
 						s_evSpcAtk, s_evSpcDef, s_evSpd, s_nameValue);
 				allSpecies[i] = s;
 				nameMap.put(Constants.hashName(s.getName()), s);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// returns the species object corresponding to the pokemon with pokedex
 	// number i
 	public static Species getSpecies(int i) {
 		if (i < 0 || i >= allSpecies.length)
 			return null;
 		return allSpecies[i];
 	}
 
 	private static int[] toIntArray(String line) {
		String[] tokens = line.split("\\|");
 		int[] iarr = new int[tokens.length];
 		for (int i = 0; i < tokens.length; i++) {
 			iarr[i] = Integer.parseInt(tokens[i]);
 		}
 		return iarr;
 	}
 
 	// returns the species with this name, or null if it does not exist
 	public static Species getSpeciesFromName(String name) {
 		name = Constants.hashName(name);
 		if (!nameMap.containsKey(name))
 			return null;
 		return nameMap.get(name);
 	}
 
 	public String toString() {
 		return String
 				.format("%d %s %s %s%s Stats: %d %d %d %d %d %d Exp: %s EVs: %d %d %d %d %d %d Abilities: %s %s %s",
 						pokedexNum, name, curve, type1,
 						((type2 == Type.NONE) ? "" : " " + type2), baseHP,
 						baseAtk, baseDef, baseSpe, baseSpcAtk, baseSpcDef,
 						Arrays.toString(killExps), evHP, evAtk, evDef, evSpe, evSpcAtk, evSpcDef,
 						Arrays.toString(abilities1), Arrays.toString(abilities2), dwAbility);
 		// return name + " " + curve + " " + type1 + ((type2 == Type.NONE) ? ""
 		// : " " + type2) + " ";
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public ExpCurve getCurve() {
 		return curve;
 	}
 
 	public Type getType1() {
 		return type1;
 	}
 
 	public Type getType2() {
 		return type2;
 	}
 
 	public int getBaseHP() {
 		return baseHP;
 	}
 
 	public int getBaseAtk() {
 		return baseAtk;
 	}
 
 	public int getBaseDef() {
 		return baseDef;
 	}
 
 	public int getBaseSpcAtk() {
 		return baseSpcAtk;
 	}
 
 	public int getBaseSpcDef() {
 		return baseSpcDef;
 	}
 
 	public int getBaseSpe() {
 		return baseSpe;
 	}
 
 	public int getKillExp() {
 		if(Settings.game == Game.BLACK2 || Settings.game == Game.WHITE2) {
 			return killExps[2];
 		}
 		else if(Settings.game == Game.BLACK || Settings.game == Game.WHITE) {
 			return killExps[1];
 		}
 		else {
 			return killExps[0];
 		}
 	}
 
 	public int getPokedexNum() {
 		return pokedexNum;
 	}
 
 	public String getAbility1() {
 		return abilities1[Settings.game.generationIndex()];
 	}
 
 	public String getAbility2() {
 		return abilities2[Settings.game.generationIndex()];
 	}
 
 	public int getNumAbilities() {
 		return numAbilities[Settings.game.generationIndex()];
 	}
 
 	public int getEvHP() {
 		return evHP;
 	}
 
 	public int getEvAtk() {
 		return evAtk;
 	}
 
 	public int getEvDef() {
 		return evDef;
 	}
 
 	public int getEvSpcAtk() {
 		return evSpcAtk;
 	}
 
 	public int getEvSpcDef() {
 		return evSpcDef;
 	}
 
 	public int getEvSpe() {
 		return evSpe;
 	}
 
 	public int getNameValue() {
 		return nameValue;
 	}
 }
