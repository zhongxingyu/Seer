 import java.util.List;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.ini4j.InvalidFileFormatException;
 import org.ini4j.Wini;
 
 //import org.ini4j.*;
 
 public class Main {
     private static StringBuilder output = new StringBuilder();
     
     public static void append(String s) {
         output.append(s);
     }
     public static void appendln(String s) {
         output.append(s + Constants.endl);
     }
     
     public static void main(String[] args) throws InvalidFileFormatException, IOException { 
        String fileName = (args.length > 0) ? args[0] : "config.ini";
        Wini ini = new Wini(new File(fileName));
         //set pokemon
         String species = ini.get("poke", "species");
         int level = ini.get("poke", "level", int.class);
         int atkIV = ini.get("poke", "atkIV", int.class);
         int defIV = ini.get("poke", "defIV", int.class);
         int spdIV = ini.get("poke", "spdIV", int.class);
         int spcIV = ini.get("poke", "spcIV", int.class);
         //set game
         String gameName = ini.get("game", "game");
         if(gameName.equalsIgnoreCase("yellow"))
             Settings.isRB = false;
         else
             Settings.isRB = true;
         
         Initialization.init();
         
         IVs ivs = new IVs(atkIV,defIV,spdIV,spcIV);
         Pokemon p = new Pokemon(PokemonNames.getSpeciesFromName(species),level,ivs,false);
         List<GameAction> actions = RouteParser.parseFile(ini.get("files","routeFile"));
         
         int[] XItems = {0,0,0,0,0}; //atk,def,spd,spc,acc
         int numBattles = 0;
         int rareCandies = 0;
         int HPUp = 0;
         int iron = 0;
         int protein = 0;
         int carbos = 0;
         int calcium = 0;
         for(GameAction a : actions) {        
             a.performAction(p);
             if (a instanceof Battle) {
                 StatModifier sm = ((Battle) a).getMod1();
                 XItems[0] += Math.max(0, sm.getAtkStage());
                 XItems[1] += Math.max(0, sm.getDefStage());
                 XItems[2] += Math.max(0, sm.getSpdStage());
                 XItems[3] += Math.max(0, sm.getSpcStage());
                 XItems[4] += sm.getUsedXAcc() ? 1 : 0;
                 numBattles++;
             } else if (a == GameAction.eatRareCandy) {
                 rareCandies++;
             } else if (a == GameAction.eatHPUp){
                 HPUp++;
             } else if (a == GameAction.eatIron){
                 iron++;
             } else if (a == GameAction.eatProtein){
                 protein++;
             } else if (a == GameAction.eatCarbos){
                 carbos++;
             } else if (a == GameAction.eatCalcium){
                 calcium++;
             }
         }        
         
         if(ini.get("util", "printxitems", boolean.class)) {
             if(XItems[0] != 0)
                 appendln("X ATTACKS: " + XItems[0]);
             if(XItems[1] != 0)
                 appendln("X DEFENDS: " + XItems[1]);
             if(XItems[2] != 0)
                 appendln("X SPEEDS: " + XItems[2]);
             if(XItems[3] != 0)
                 appendln("X SPECIALS: " + XItems[3]);
             if(XItems[4] != 0)
                 appendln("X ACCURACYS: " + XItems[4]);
             int cost = XItems[0] * 500 + XItems[1] * 550 + XItems[2] * 350 + XItems[3] * 350 + XItems[4] * 950;
             if(cost != 0)
                 appendln("X item cost: " + cost);
         }
         
         if(ini.get("util", "printrarecandies", boolean.class)) {
             if(rareCandies != 0)
                 appendln("Total Rare Candies: " + rareCandies);
         }
         if(ini.get("util", "printstatboosters", boolean.class)) {
             if(HPUp != 0) {
                 appendln("HP UP: " + HPUp);
             }
             if(iron != 0) {
                 appendln("IRON: " + iron);
             }
             if(protein != 0) {
                 appendln("PROTEIN: " + protein);
             }
             if(carbos != 0) {
                 appendln("CARBOS: " + carbos);
             }
             if(calcium != 0) {
                 appendln("CALCIUM: " + calcium);
             }
         }
         //System.out.println("Total Battles: " + numBattles);
         
         
         FileWriter fw = new FileWriter(ini.get("files", "outputFile"));
         BufferedWriter bw = new BufferedWriter(fw);
         bw.write(output.toString());
         bw.close();
         
         
         
     }
 }
