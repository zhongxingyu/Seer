 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.ini4j.InvalidFileFormatException;
 import org.ini4j.Wini;
 
 public class Main {
 	private static StringBuilder output = new StringBuilder();
 
 	public static void append(String s) {
 		output.append(s);
 	}
 
 	public static void appendln(String s) {
 		output.append(s + Constants.endl);
 	}
 
 	public static void main(String[] args) throws InvalidFileFormatException,
 			IOException {
 		String fileName = (args.length > 0) ? args[0] : "config.ini";
 		Wini ini = new Wini(new File(fileName));
 
 		// set game
 
 		String gameName = ini.get("game", "game");
 		if (gameName.equalsIgnoreCase("firered"))
 			Settings.game = Game.FIRERED;
 		else if (gameName.equalsIgnoreCase("leafgreen"))
 			Settings.game = Game.LEAFGREEN;
 		else if (gameName.equalsIgnoreCase("ruby"))
 			Settings.game = Game.RUBY;
 		else if (gameName.equalsIgnoreCase("SAPPHIRE"))
 			Settings.game = Game.SAPPHIRE;
 		else if (gameName.equalsIgnoreCase("diamond"))
 			Settings.game = Game.DIAMOND;
 		else if (gameName.equalsIgnoreCase("pearl"))
 			Settings.game = Game.PEARL;
 		else if (gameName.equalsIgnoreCase("platinum"))
 			Settings.game = Game.PLATINUM;
 		else if (gameName.equalsIgnoreCase("heartgold"))
 			Settings.game = Game.HEARTGOLD;
 		else if (gameName.equalsIgnoreCase("soulsilver"))
 			Settings.game = Game.SOULSILVER;
 		else
 			Settings.game = Game.EMERALD;
 
 		Trainer.initTrainers();
 
 		// set pokemon
 		boolean atkBadge = Settings.game.hasBadgeBoosts() ? ini.get("extras",
 				"atkBadge", boolean.class) : false;
 		boolean defBadge = Settings.game.hasBadgeBoosts() ? ini.get("extras",
 				"defBadge", boolean.class) : false;
 		boolean speBadge = Settings.game.hasBadgeBoosts() ? ini.get("extras",
 				"speBadge", boolean.class) : false;
 		boolean spcBadge = Settings.game.hasBadgeBoosts() ? ini.get("extras",
 				"spcBadge", boolean.class) : false;
 		int pkmnCount = ini.get("party", "pokemon", int.class);
 		Party p = new Party();
 		for (int poke = 1; poke <= pkmnCount; poke++) {
 			String iniSection = "poke" + poke;
 
 			String species = ini.get(iniSection, "species");
 			int level = ini.get(iniSection, "level", int.class);
 			int hpIV = ini.get(iniSection, "hpIV", int.class);
 			int atkIV = ini.get(iniSection, "atkIV", int.class);
 			int defIV = ini.get(iniSection, "defIV", int.class);
 			int spaIV = ini.get(iniSection, "spaIV", int.class);
 			int spdIV = ini.get(iniSection, "spdIV", int.class);
 			int speIV = ini.get(iniSection, "speIV", int.class);
 			Nature nature = Nature.getNature(ini.get(iniSection, "nature"));
 			IVs ivs = new IVs(hpIV, atkIV, defIV, spaIV, spdIV, speIV);
 			int hpEV = ini.get(iniSection, "hpEV", int.class);
 			int atkEV = ini.get(iniSection, "atkEV", int.class);
 			int defEV = ini.get(iniSection, "defEV", int.class);
 			int spaEV = ini.get(iniSection, "spaEV", int.class);
 			int spdEV = ini.get(iniSection, "spdEV", int.class);
 			int speEV = ini.get(iniSection, "speEV", int.class);
 
 			Pokemon pkmn = null;
 			try {
 				pkmn = new Pokemon(Species.getSpeciesFromName(species), level,
 						ivs, nature);
 				pkmn.setAtkBadge(atkBadge);
 				pkmn.setDefBadge(defBadge);
 				pkmn.setSpeBadge(speBadge);
 				pkmn.setSpcBadge(spcBadge);
 				EVs evs = new EVs(hpEV, atkEV, defEV, spaEV, spdEV, speEV);
 				pkmn.setEVs(evs);
 				p.addPartyPokemon(pkmn);
 			} catch (NullPointerException e) {
 				e.printStackTrace();
 				appendln("Error in your config file. Perhaps you have an incorrect pokemon species name?");
 				FileWriter fw = new FileWriter(ini.get("files", "outputFile"));
 				BufferedWriter bw = new BufferedWriter(fw);
 				bw.write(output.toString());
 				bw.close();
 				return;
 			}
 		}
 		List<GameAction> actions = RouteParser.parseFile(ini.get("files",
 				"routeFile"));
 
 		int[] XItems = { 0, 0, 0, 0, 0 }; // atk,def,spa,spd,spe
 		int numBattles = 0;
 		int rareCandies = 0;
 		int HPUp = 0;
 		int iron = 0;
 		int protein = 0;
 		int calcium = 0;
 		int zinc = 0;
 		int carbos = 0;
 		for (GameAction a : actions) {
 			a.performAction(p);
 			if (a instanceof Battle) {
 				// this is broken because I mess with the BattleOptions stat mod
 				// dynamically in the Complex battle flags code now
 				StatModifier sm = ((Battle) a).getMod1();
 				XItems[0] += Math.max(0, sm.getAtkStage());
 				XItems[1] += Math.max(0, sm.getDefStage());
 				XItems[2] += Math.max(0, sm.getSpcAtkStage());
 				XItems[3] += Math.max(0, sm.getSpcDefStage());
 				XItems[4] += Math.max(0, sm.getSpeStage());
 				numBattles++;
 			} else if (a == GameAction.eatRareCandy) {
 				rareCandies++;
 			} else if (a == GameAction.eatHPUp) {
 				HPUp++;
 			} else if (a == GameAction.eatIron) {
 				iron++;
 			} else if (a == GameAction.eatProtein) {
 				protein++;
 			} else if (a == GameAction.eatCarbos) {
 				carbos++;
 			} else if (a == GameAction.eatCalcium) {
 				calcium++;
 			} else if (a == GameAction.eatZinc) {
 				zinc++;
 			}
 		}
 
 		if (ini.get("util", "printxitems", boolean.class)) {
 			if (XItems[0] != 0)
 				appendln("X ATTACKS: " + XItems[0]);
 			if (XItems[1] != 0)
 				appendln("X DEFENDS: " + XItems[1]);
 			if (XItems[2] != 0)
 				appendln("X SPECIALS: " + XItems[2]);
 			if (XItems[3] != 0)
 				appendln("X SPECIAL DEFS: " + XItems[3]);
 			if (XItems[4] != 0)
 				appendln("X SPEEDS: " + XItems[4]);
 			int cost = XItems[0] * 500 + XItems[1] * 550 + XItems[2] * 350
 					+ XItems[3] * 350 + XItems[4] * 350;
 			if (cost != 0)
 				appendln("X item cost: " + cost);
 		}
 
 		if (ini.get("util", "printrarecandies", boolean.class)) {
 			if (rareCandies != 0)
 				appendln("Total Rare Candies: " + rareCandies);
 		}
 		if (ini.get("util", "printstatboosters", boolean.class)) {
 			if (HPUp != 0) {
 				appendln("HP UP: " + HPUp);
 			}
 			if (iron != 0) {
 				appendln("IRON: " + iron);
 			}
 			if (protein != 0) {
 				appendln("PROTEIN: " + protein);
 			}
 			if (calcium != 0) {
 				appendln("CALCIUM: " + calcium);
 			}
 			if (zinc != 0) {
 				appendln("ZINC: " + zinc);
 			}
 			if (carbos != 0) {
 				appendln("CARBOS: " + carbos);
 			}
 		}
 		// System.out.println("Total Battles: " + numBattles);
 
 		FileWriter fw = new FileWriter(ini.get("files", "outputFile"));
 		BufferedWriter bw = new BufferedWriter(fw);
 		bw.write(output.toString());
 		bw.close();
 
 	}
 }
