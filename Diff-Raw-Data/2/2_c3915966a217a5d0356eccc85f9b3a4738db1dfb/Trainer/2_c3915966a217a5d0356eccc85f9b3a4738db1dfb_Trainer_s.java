 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 //a trainer has a class name and some pokemon, corresponding to some location in memory
 public class Trainer implements Battleable, Iterable<Pokemon>{
     private String name;
     private ArrayList<Pokemon> pokes;
     private int offset;
     
     @Override
     public void battle(Pokemon p) {
         for(Pokemon tp : pokes) {
             tp.battle(p);
         }
     }
 
     @Override
     public Iterator<Pokemon> iterator() {
         return pokes.iterator();
     }
     
     public String toString() {
         return String.format("%s (0x%X: %s)", name, offset, allPokes());
     }
     
     public String allPokes() {
         StringBuilder sb = new StringBuilder();
         for(Pokemon p : pokes) {
             sb.append(p.levelName() + ", ");
         }
         return sb.toString();
     } 
     
     private static final HashMap<Integer,Trainer> allTrainers;
     
     public static Trainer getTrainer(int offset) {
         if(!allTrainers.containsKey(offset))
             return null;
         else
             return allTrainers.get(offset);
     }
     
     static {
         allTrainers = new HashMap<Integer,Trainer>();
         
         List<Trainer> trainerList = null;
         if (Settings.isRB)
             trainerList = getData("trainer_data_blue.txt");
         else
             trainerList = getData("trainer_data_y.txt");
         
         for(Trainer t : trainerList) {
             allTrainers.put(new Integer(t.offset), t);
         }
         
         fixSpecialTrainers();
     }
     //reads trainer_data_(blue|yellow).txt to get trainer data
     private static List<Trainer> getData(String filename) {
         ArrayList<Trainer> trainers = new ArrayList<Trainer>();
         BufferedReader in;
         try {
             in = new BufferedReader(new InputStreamReader(
                     System.class.getResource("/resources/" + filename).openStream()));
 
             String currentName = "";
             Trainer t;
             while(in.ready()) {
                 String text = in.readLine();
                 //names are formatted as [NAME]
                 if(text.startsWith("[")){
                     //TODO: error checking is for noobs
                     currentName = text.substring(1,text.length()-1); 
                     continue;
                 } else if (text.startsWith("0x")) { //line is a 0x(pointer): list of pokes
                     String[] parts = text.split(":"); //this should be length 2
                     int offset = Integer.parseInt(parts[0].substring(2),16);
                     
                     t = new Trainer();
                     t.name = currentName;
                     t.offset = offset;
                     t.pokes = new ArrayList<Pokemon>();
                     
                     //read off pokemon
                     String[] pokeStrs = parts[1].split(",");
                     for(String pokeStr : pokeStrs) {
                         pokeStr = pokeStr.trim();
                         if (pokeStr.isEmpty())
                             continue;
                         //the string should be "L# POKENAME"
                         //Pokemon p = new Pokemon();
                         String[] levelName = pokeStr.split(" ");
                         int level = Integer.parseInt(levelName[0].substring(1));
                         Species s = PokemonNames.getSpeciesFromName(levelName[1]);
                         t.pokes.add(new Pokemon(s, level, false));
                     }
                     trainers.add(t);
                 }
             }
             in.close();
             return trainers;
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
             
         return null;
     }
     
     //manually fixes the movesets of special trainers
     private static void fixSpecialTrainers() {
         Pokemon p;
         Moveset m;
         //BROCK
         if(Settings.isRB) {
             Trainer brock = getTrainer(0x3A3B5);
             p = brock.pokes.get(1); //ONIX
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("SCREECH");
             m.addMove("BIDE");
             m.addMove("BIND");
             p.setMoveset(m);
         } else {
             Trainer brock = getTrainer(0x3A454);
             p = brock.pokes.get(1); //ONIX
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("SCREECH");
             m.addMove("BIDE");
             m.addMove("BIND");
             p.setMoveset(m);
         }
         
         //MISTY
         if(Settings.isRB) {
             Trainer misty = getTrainer(0x3A3BB);
             p = misty.pokes.get(1); //STARMIE
             p.getMoveset().addMove("BUBBLEBEAM");
         } else {
             Trainer misty = getTrainer(0x3A45A);
             p = misty.pokes.get(1); //STARMIE
             p.getMoveset().addMove("BUBBLEBEAM");
         }
         
         //SURGE
         if(Settings.isRB) {
             Trainer surge = getTrainer(0x3A3C1);
             p = surge.pokes.get(2); //RAICHU
             p.getMoveset().addMove("THUNDERBOLT");
         } else {
             Trainer surge = getTrainer(0x3A460);
             p = surge.pokes.get(0); //RAICHU
             m = new Moveset();
             m.addMove("THUNDERBOLT");
             m.addMove("GROWL");
             m.addMove("MEGA PUNCH");
             m.addMove("MEGA KICK");
             p.setMoveset(m);
         }
         
         //ERIKA
         if(Settings.isRB) {
             Trainer erika = getTrainer(0x3A3C9);
             p = erika.pokes.get(0); //VICTREEBEL
             m = new Moveset();
             m.addMove("WRAP");
             m.addMove("POISONPOWDER");
             m.addMove("SLEEP POWDER");
             m.addMove("RAZOR LEAF");
             p.setMoveset(m);
             
             p = erika.pokes.get(2); //VILEPLUME
             m = new Moveset();
             m.addMove("POISONPOWDER");
             m.addMove("MEGA DRAIN");
             m.addMove("SLEEP POWDER");
             m.addMove("PETAL DANCE");
             p.setMoveset(m);
             
         } else {
             Trainer erika = getTrainer(0x3A464);
             p = erika.pokes.get(0); //TANGELA
             m = new Moveset();
             m.addMove("BIND");
             m.addMove("MEGA DRAIN");
             m.addMove("VINE WHIP");
             m.addMove("CONSTRICT");
             p.setMoveset(m);
             
             p = erika.pokes.get(2); //GLOOM
             m = new Moveset();
             m.addMove("ACID");
             m.addMove("PETAL DANCE");
             m.addMove("STUN SPORE");
             m.addMove("SLEEP POWDER");
             p.setMoveset(m);
         }
         
         //KOGA
         if(Settings.isRB) {
             Trainer koga = getTrainer(0x3A3D1);
             p = koga.pokes.get(3); //WEEZING
             m = new Moveset();
             m.addMove("SMOG");
             m.addMove("SLUDGE");
             m.addMove("TOXIC");
             m.addMove("SELFDESTRUCT");
             p.setMoveset(m);
         } else {
             Trainer koga = getTrainer(0x3A46C);
             p = koga.pokes.get(0); //VENONAT
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("TOXIC");
             m.addMove("SLEEP POWDER");
             m.addMove("PSYCHIC");
             p.setMoveset(m);
             
             p = koga.pokes.get(1); //VENONAT
             m = new Moveset();
             m.addMove("TOXIC");
             m.addMove("PSYBEAM");
             m.addMove("SUPERSONIC");
             m.addMove("PSYCHIC");
             p.setMoveset(m);
             
             p = koga.pokes.get(2); //VENONAT
             m = new Moveset();
             m.addMove("TOXIC");
             m.addMove("PSYCHIC");
             m.addMove("SLEEP POWDER");
             m.addMove("DOUBLE-EDGE");
             p.setMoveset(m);
             
             p = koga.pokes.get(3); //VENOMOTH
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("TOXIC");
             m.addMove("SLEEP POWDER");
             m.addMove("PSYCHIC");
             p.setMoveset(m);
         }
         
         //SABRINA
         if(Settings.isRB) {
             Trainer sabrina = getTrainer(0x3A3E5);
             p = sabrina.pokes.get(3); //ALAKAZAM
             m = new Moveset();
             m.addMove("PSYBEAM");
             m.addMove("RECOVER");
             m.addMove("PSYWAVE");
             m.addMove("REFLECT");
             p.setMoveset(m);
         } else {
             Trainer sabrina = getTrainer(0x3A47E);
             p = sabrina.pokes.get(0); //ABRA
             m = new Moveset();
             m.addMove("TELEPORT");
             m.addMove("FLASH");
             p.setMoveset(m);
             
             p = sabrina.pokes.get(1); //KADABRA
             m = new Moveset();
             m.addMove("PSYCHIC");
             m.addMove("RECOVER");
             m.addMove("KINESIS");
             m.addMove("PSYWAVE");
             p.setMoveset(m);
             
             p = sabrina.pokes.get(2); //ALAKAZAM
             m = new Moveset();
             m.addMove("PSYCHIC");
             m.addMove("PSYWAVE");
             m.addMove("REFLECT");
             m.addMove("RECOVER");
             p.setMoveset(m);
         }
         
         //BLAINE
         if(Settings.isRB) {
             Trainer blaine = getTrainer(0x3A3DB);
             p = blaine.pokes.get(3); //ARCANINE
             m = new Moveset();
             m.addMove("ROAR");
             m.addMove("EMBER");
             m.addMove("TAKE DOWN");
             m.addMove("FIRE BLAST");
             p.setMoveset(m);
         } else {
             Trainer blaine = getTrainer(0x3A476);
             p = blaine.pokes.get(0); //NINETALES
             m = new Moveset();
             m.addMove("CONFUSE RAY");
             m.addMove("QUICK ATTACK");
             m.addMove("TAIL WHIP");
             m.addMove("FLAMETHROWER");
             p.setMoveset(m);
             
             p = blaine.pokes.get(1); //RAPIDASH
             m = new Moveset();
             m.addMove("TAKE DOWN");
             m.addMove("STOMP");
             m.addMove("GROWL");
             m.addMove("FIRE SPIN");
             p.setMoveset(m);
             
             p = blaine.pokes.get(2); //ARCANINE
             m = new Moveset();
             m.addMove("REFLECT");
             m.addMove("TAKE DOWN");
             m.addMove("FIRE BLAST");
             m.addMove("FLAMETHROWER");
             p.setMoveset(m);
         }
         
         //GIOVANNI (all)
         //rocket hideout fight
         if(Settings.isRB){
         } else {
             Trainer giovanni1 = getTrainer(0x3A2FD); //hideout, not in main route
             p = giovanni1.pokes.get(2); //PERSIAN
             m = new Moveset();
             m.addMove("PAY DAY");
             m.addMove("SCRATCH");
             m.addMove("BITE");
             m.addMove("GROWL");
             p.setMoveset(m);
         }
         //silph co fight
         if(Settings.isRB){
         } else {
             Trainer giovanni2 = getTrainer(0x3A305); //silph co
             //TODO check this offset
            p = giovanni2.pokes.get(2); //PERSIAN
             m = new Moveset();
             m.addMove("PAY DAY");
             m.addMove("SCRATCH");
             m.addMove("BITE");
             m.addMove("GROWL");
             p.setMoveset(m);
             
             p = giovanni2.pokes.get(3); //NIDOQUEEN
             m = new Moveset();
             m.addMove("DOUBLE KICK");
             m.addMove("TAIL WHIP");
             m.addMove("POISON STING");
             m.addMove("BODY SLAM");
             p.setMoveset(m);
         }
         //gym fight
         if(Settings.isRB){
             Trainer giovanni3 = getTrainer(0x3A290); //gym
             p = giovanni3.pokes.get(0); //RHYHORN
             m = new Moveset();
             m.addMove("STOMP");
             m.addMove("TAIL WHIP");
             m.addMove("FURY ATTACK");
             m.addMove("HORN ATTACK");
             p.setMoveset(m);
             
             p = giovanni3.pokes.get(4); //RHYDON
             m = new Moveset();
             m.addMove("STOMP");
             m.addMove("TAIL WHIP");
             m.addMove("FISSURE");
             m.addMove("HORN DRILL");
             p.setMoveset(m);
         } else {
             Trainer giovanni3 = getTrainer(0x3A30F); //gym
             p = giovanni3.pokes.get(0); //RHYHORN
             m = new Moveset();
             m.addMove("SAND-ATTACK");
             m.addMove("DIG");
             m.addMove("FISSURE");
             m.addMove("EARTHQUAKE");
             p.setMoveset(m);
             
             p = giovanni3.pokes.get(1); //PERSIAN
             m = new Moveset();
             m.addMove("SCREECH");
             m.addMove("SLASH");
             m.addMove("FURY SWIPES");
             m.addMove("DOUBLE TEAM");
             p.setMoveset(m);
             
             p = giovanni3.pokes.get(2); //NIDOQUEEN
             m = new Moveset();
             m.addMove("TAIL WHIP");
             m.addMove("EARTHQUAKE");
             m.addMove("DOUBLE KICK");
             m.addMove("THUNDER");
             p.setMoveset(m);
             
             p = giovanni3.pokes.get(3); //NIDOKING
             m = new Moveset();
             m.addMove("THUNDER");
             m.addMove("LEER");
             m.addMove("EARTHQUAKE");
             m.addMove("THRASH");
             p.setMoveset(m);
             
             p = giovanni3.pokes.get(4); //RHYDON
             m = new Moveset();
             m.addMove("ROCK SLIDE");
             m.addMove("FURY ATTACK");
             m.addMove("EARTHQUAKE");
             m.addMove("HORN DRILL");
             p.setMoveset(m);
         }
         //E4
         //LORELEI
         if(Settings.isRB) {
             Trainer lorelei = getTrainer(0x3A4BB);
             p = lorelei.pokes.get(4); //LAPRAS
             m = new Moveset();
             m.addMove("BODY SLAM");
             m.addMove("CONFUSE RAY");
             m.addMove("HYDRO PUMP");
             m.addMove("BLIZZARD");
             p.setMoveset(m);
         } else {
             Trainer lorelei = getTrainer(0x3A53F);
             p = lorelei.pokes.get(0); //DEWGONG
             m = new Moveset();
             m.addMove("BUBBLEBEAM");
             m.addMove("AURORA BEAM");
             m.addMove("REST");
             m.addMove("TAKE DOWN");
             p.setMoveset(m);
             
             p = lorelei.pokes.get(1); //CLOYSTER
             m = new Moveset();
             m.addMove("BODY SLAM");
             m.addMove("CONFUSE RAY");
             m.addMove("HYDRO PUMP");
             m.addMove("BLIZZARD");
             p.setMoveset(m);
             
             p = lorelei.pokes.get(2); //SLOWBRO
             m = new Moveset();
             m.addMove("SURF");
             m.addMove("PSYCHIC");
             m.addMove("WITHDRAW");
             m.addMove("AMNESIA");
             p.setMoveset(m);
             
             p = lorelei.pokes.get(3); //JYNX
             m = new Moveset();
             m.addMove("DOUBLESLAP");
             m.addMove("ICE PUNCH");
             m.addMove("LOVELY KISS");
             m.addMove("THRASH");
             p.setMoveset(m);
             
             p = lorelei.pokes.get(4); //LAPRAS
             m = new Moveset();
             m.addMove("BODY SLAM");
             m.addMove("CONFUSE RAY");
             m.addMove("HYDRO PUMP");
             m.addMove("BLIZZARD");
             p.setMoveset(m);
         }
         
         //BRUNO
         if(Settings.isRB) {
             Trainer bruno = getTrainer(0x3A3A9);
             p = bruno.pokes.get(4); //MACHAMP
             m = new Moveset();
             m.addMove("LEER");
             m.addMove("FOCUS ENERGY");
             m.addMove("FISSURE");
             m.addMove("SUBMISSION");
             p.setMoveset(m);
         } else {
             Trainer bruno = getTrainer(0x3A448);
             p = bruno.pokes.get(0); //ONIX
             m = new Moveset();
             m.addMove("ROCK SLIDE");
             m.addMove("DIG");
             m.addMove("SCREECH");
             m.addMove("SLAM");
             p.setMoveset(m);
             
             p = bruno.pokes.get(1); //HITMONCHAN
             m = new Moveset();
             m.addMove("ICE PUNCH");
             m.addMove("FIRE PUNCH");
             m.addMove("THUNDERPUNCH");
             m.addMove("DOUBLE TEAM");
             p.setMoveset(m);
             
             p = bruno.pokes.get(2); //HITMONLEE
             m = new Moveset();
             m.addMove("DOUBLE KICK");
             m.addMove("DOUBLE TEAM");
             m.addMove("HI JUMP KICK");
             m.addMove("MEGA KICK");
             p.setMoveset(m);
             
             p = bruno.pokes.get(3); //ONIX
             m = new Moveset();
             m.addMove("ROCK SLIDE");
             m.addMove("SCREECH");
             m.addMove("EARTHQUAKE");
             m.addMove("SLAM");
             p.setMoveset(m);
             
             p = bruno.pokes.get(4); //MACHAMP
             m = new Moveset();
             m.addMove("LEER");
             m.addMove("STRENGTH");
             m.addMove("KARATE CHOP");
             m.addMove("SUBMISSION");
             p.setMoveset(m);
         }
         
         //AGATHA
         if(Settings.isRB) {
             Trainer agatha = getTrainer(0x3A516);
             p = agatha.pokes.get(1); //GOLBAT
             m = new Moveset();
             m.addMove("SUPERSONIC");
             m.addMove("CONFUSE RAY");
             m.addMove("WING ATTACK");
             m.addMove("HAZE");
             p.setMoveset(m);
             
             p = agatha.pokes.get(4); //GENGAR
             m = new Moveset();
             m.addMove("CONFUSE RAY");
             m.addMove("NIGHT SHADE");
             m.addMove("TOXIC");
             m.addMove("DREAM EATER");
             p.setMoveset(m);
         } else {
             Trainer agatha = getTrainer(0x3A59A);
             p = agatha.pokes.get(0); //GENGAR
             m = new Moveset();
             m.addMove("CONFUSE RAY");
             m.addMove("LICK");
             m.addMove("SUBSTITUTE");
             m.addMove("MEGA DRAIN");
             p.setMoveset(m);
             
             p = agatha.pokes.get(1); //GOLBAT
             m = new Moveset();
             m.addMove("SUPERSONIC");
             m.addMove("LEECH LIFE");
             m.addMove("WING ATTACK");
             m.addMove("TOXIC");
             p.setMoveset(m);
             
             p = agatha.pokes.get(2); //HAUNTER
             m = new Moveset();
             m.addMove("CONFUSE RAY");
             m.addMove("LICK");
             m.addMove("HYPNOSIS");
             m.addMove("DREAM EATER");
             p.setMoveset(m);
             
             p = agatha.pokes.get(3); //ARBOK
             m = new Moveset();
             m.addMove("WRAP");
             m.addMove("GLARE");
             m.addMove("SCREECH");
             m.addMove("ACID");
             p.setMoveset(m);
             
             p = agatha.pokes.get(4); //GENGAR
             m = new Moveset();
             m.addMove("CONFUSE RAY");
             m.addMove("PSYCHIC");
             m.addMove("HYPNOSIS");
             m.addMove("DREAM EATER");
             p.setMoveset(m);
         }
         
         //LANCE
         if(Settings.isRB){
             Trainer lance = getTrainer(0x3A522);
             p = lance.pokes.get(4); //DRAGONITE
             m = new Moveset();
             m.addMove("AGILITY");
             m.addMove("SLAM");
             m.addMove("BARRIER");
             m.addMove("HYPER BEAM");
             p.setMoveset(m);
         } else {       
             Trainer lance = getTrainer(0x3A5A6);
             p = lance.pokes.get(1); //DRAGONAIR
             m = new Moveset();
             m.addMove("THUNDERBOLT");
             m.addMove("SLAM");
             m.addMove("THUNDER WAVE");
             m.addMove("HYPER BEAM");
             p.setMoveset(m);
             
             p = lance.pokes.get(2); //DRAGONAIR
             m = new Moveset();
             m.addMove("WRAP");
             m.addMove("BUBBLEBEAM");
             m.addMove("ICE BEAM");
             m.addMove("HYPER BEAM");
             p.setMoveset(m);
             
             p = lance.pokes.get(3); //AERODACTYL
             m = new Moveset();
             m.addMove("WING ATTACK");
             m.addMove("FLY");
             m.addMove("SWIFT");
             m.addMove("HYPER BEAM");
             p.setMoveset(m);
             
             p = lance.pokes.get(4); //DRAGONITE
             m = new Moveset();
             m.addMove("BLIZZARD");
             m.addMove("FIRE BLAST");
             m.addMove("THUNDER");
             m.addMove("HYPER BEAM");
             p.setMoveset(m);
         }
         
         //GARY MOTHERFUCKING OAK
         if(Settings.isRB) {
             //c,s,b means your starter was
             //charmander, squirtle, bublasaur
             Trainer rival_anne_C = getTrainer(0x3A401);
             Trainer rival_anne_S = getTrainer(0x3A40B);
             Trainer rival_anne_B = getTrainer(0x3A415);
             
             m = new Moveset();
             m.addMove("TELEPORT");
             m.addMove("CONFUSION");
             p = rival_anne_C.pokes.get(2); //KADABRA;
             p.setMoveset(m);
             p = rival_anne_S.pokes.get(2); //KADABRA;
             p.setMoveset(m);
             p = rival_anne_B.pokes.get(2); //KADABRA;
             p.setMoveset(m);
             
             Trainer rival_tower_C = getTrainer(0x3A41F);
             Trainer rival_tower_S = getTrainer(0x3A42B);
             Trainer rival_tower_B = getTrainer(0x3A437);
             m = new Moveset();
             m.addMove("BITE");
             m.addMove("ROAR");
             m.addMove("EMBER");
             p = rival_tower_C.pokes.get(1); //GROWLITHE
             p.setMoveset(m);
             p = rival_tower_S.pokes.get(2); //GROWLITHE
             p.setMoveset(m);
             
             Trainer rival_silph_C = getTrainer(0x3A443);
             Trainer rival_silph_S = getTrainer(0x3A44F);
             Trainer rival_silph_B = getTrainer(0x3A45B);
             m = new Moveset();
             m.addMove("HYPNOSIS");
             m.addMove("REFLECT");
             m.addMove("LEECH SEED");
             m.addMove("STUN SPORE");
             p = rival_silph_C.pokes.get(2); //EXEGGCUTE
             p.setMoveset(m);
             p = rival_silph_B.pokes.get(1); //EXEGGCUTE
             p.setMoveset(m);
             
             Trainer rival_viridian_C = getTrainer(0x3A467);
             Trainer rival_viridian_S = getTrainer(0x3A475);
             Trainer rival_viridian_B = getTrainer(0x3A483);
             m = new Moveset();
             m.addMove("FURY ATTACK");
             m.addMove("STOMP");
             m.addMove("HORN ATTACK");
             m.addMove("TAIL WHIP");
             p = rival_viridian_C.pokes.get(1); //RHYHORN
             p.setMoveset(m);
             p = rival_viridian_S.pokes.get(1); //RHYHORN
             p.setMoveset(m);
             p = rival_viridian_B.pokes.get(1); //RHYHORN
             p.setMoveset(m);
 
             m = new Moveset();
             m.addMove("LEECH SEED");
             m.addMove("POISONPOWDER");
             m.addMove("SOLARBEAM");
             m.addMove("GROWTH");
             p = rival_viridian_B.pokes.get(2); //EXEGGCUTE
             p.setMoveset(m);
             p = rival_viridian_C.pokes.get(3); //EXEGGCUTE
             p.setMoveset(m);
             
             Trainer rival_e4_C = getTrainer(0x3A491);
             Trainer rival_e4_S = getTrainer(0x3A49F);
             Trainer rival_e4_B = getTrainer(0x3A4AD);
             m = new Moveset();
             m.addMove("WING ATTACK");
             m.addMove("MIRROR MOVE");
             m.addMove("SKY ATTACK");
             m.addMove("WHIRLWIND");
             p = rival_e4_C.pokes.get(0); //PIDGEOT
             p.setMoveset(m);
             p = rival_e4_S.pokes.get(0); //PIDGEOT
             p.setMoveset(m);
             p = rival_e4_B.pokes.get(0); //PIDGEOT
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("LEER");
             m.addMove("TAIL WHIP");
             m.addMove("FURY ATTACK");
             m.addMove("HORN DRILL");
             p = rival_e4_C.pokes.get(2); //RHYDON
             p.setMoveset(m);
             p = rival_e4_S.pokes.get(2); //RHYDON
             p.setMoveset(m);
             p = rival_e4_B.pokes.get(2); //RHYDON
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("HYDRO PUMP");
             m.addMove("BLIZZARD");
             m.addMove("BITE");
             m.addMove("WITHDRAW");
             p = rival_e4_C.pokes.get(5); //BLASTOISE
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("GROWTH");
             m.addMove("MEGA DRAIN");
             m.addMove("RAZOR LEAF");
             m.addMove("SOLARBEAM");
             p = rival_e4_S.pokes.get(5); //VENUSAUR
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("FIRE BLAST");
             m.addMove("RAGE");
             m.addMove("SLASH");
             m.addMove("FIRE SPIN");
             p = rival_e4_B.pokes.get(5); //CHARIZARD
             p.setMoveset(m);
         } else {
             //TODO: all of gary motherfucking oak
             //eevee in cerulean battle
             Trainer rival_cerulean = getTrainer(0x3A292);
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("TAIL WHIP");
             m.addMove("SAND-ATTACK");
             p = rival_cerulean.pokes.get(3); //EEVEE
             p.setMoveset(m);
             
             //v,f,j
             //vaporean flareon jolteon
             //0,1,2 wins
 
             //tower fight, shellder
             Trainer rival_tower_J = getTrainer(0x3A4A3);
             Trainer rival_tower_F = getTrainer(0x3A4AF);
             Trainer rival_tower_V = getTrainer(0x3A4BB);
             m = new Moveset();
             m.addMove("TACKLE");
             m.addMove("WITHDRAW");
             m.addMove("SUPERSONIC");
             m.addMove("CLAMP");
             p = rival_tower_J.pokes.get(1); //SHELLDER
             p.setMoveset(m);
             p = rival_tower_F.pokes.get(2); //SHELLDER
             p.setMoveset(m);
             
             //silph fight, sandslash
             Trainer rival_silph_J = getTrainer(0x3A4C7);
             Trainer rival_silph_F = getTrainer(0x3A4D3);
             Trainer rival_silph_V = getTrainer(0x3A4DF);
             m = new Moveset();
             m.addMove("SLASH");
             m.addMove("SAND-ATTACK");
             m.addMove("POISON STING");
             m.addMove("SWIFT");
             p = rival_silph_J.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             p = rival_silph_F.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             p = rival_silph_V.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             
             //viridian fight, magneton, vaporeon,
             Trainer rival_viridian_J = getTrainer(0x3A4EB);
             Trainer rival_viridian_F = getTrainer(0x3A4F9);
             Trainer rival_viridian_V = getTrainer(0x3A507);
             m = new Moveset();
             m.addMove("THUNDERSHOCK");
             m.addMove("THUNDER WAVE");
             m.addMove("SUPERSONIC");
             m.addMove("SWIFT");
             p = rival_viridian_F.pokes.get(3); //MAGNETON
             p.setMoveset(m);
             p = rival_viridian_V.pokes.get(2); //MAGNETON
             p.setMoveset(m);
             m = new Moveset();
             m.addMove("HYDRO PUMP");
             m.addMove("AURORA BEAM");
             m.addMove("HAZE");
             m.addMove("ACID ARMOR");
             p = rival_viridian_V.pokes.get(5); //VAPOREON
             p.setMoveset(m);
             
             //e4, every fucking single one
             Trainer rival_e4_J = getTrainer(0x3A515);
             Trainer rival_e4_F = getTrainer(0x3A523);
             Trainer rival_e4_V = getTrainer(0x3A531);
             m = new Moveset();
             m.addMove("THUNDERSHOCK");
             m.addMove("THUNDER WAVE");
             m.addMove("SCREECH");
             m.addMove("SWIFT");
             p = rival_e4_F.pokes.get(3); //MAGNETON
             p.setMoveset(m);
             p = rival_e4_V.pokes.get(4); //MAGNETON
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("ICE BEAM");
             m.addMove("SPIKE CANNON");
             m.addMove("AURORA BEAM");
             m.addMove("CLAMP");
             p = rival_e4_J.pokes.get(3); //CLOYSTER
             p.setMoveset(m);
             p = rival_e4_F.pokes.get(4); //CLOYSTER
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("FIRE SPIN");
             m.addMove("TAIL WHIP");
             m.addMove("QUICK ATTACK");
             m.addMove("CONFUSE RAY");
             p = rival_e4_J.pokes.get(4); //NINETALES
             p.setMoveset(m);
             p = rival_e4_V.pokes.get(3); //NINETALES
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("EARTHQUAKE");
             m.addMove("SLASH");
             m.addMove("POISON STING");
             m.addMove("FURY SWIPES");
             p = rival_e4_J.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             p = rival_e4_F.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             p = rival_e4_V.pokes.get(0); //SANDSLASH
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("PSYBEAM");
             m.addMove("RECOVER");
             m.addMove("PSYCHIC");
             m.addMove("KINESIS");
             p = rival_e4_J.pokes.get(1); //ALAKAZAM
             p.setMoveset(m);
             p = rival_e4_F.pokes.get(1); //ALAKAZAM
             p.setMoveset(m);
             p = rival_e4_V.pokes.get(1); //ALAKAZAM
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("BARRAGE");
             m.addMove("HYPNOSIS");
             m.addMove("STOMP");
             m.addMove("LEECH SEED");
             p = rival_e4_J.pokes.get(2); //EXEGGUTOR
             p.setMoveset(m);
             p = rival_e4_F.pokes.get(2); //EXEGGUTOR
             p.setMoveset(m);
             p = rival_e4_V.pokes.get(2); //EXEGGUTOR
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("THUNDER");
             m.addMove("THUNDERBOLT");
             m.addMove("QUICK ATTACK");
             m.addMove("PIN MISSILE");
             p = rival_e4_J.pokes.get(5); //JOLTEON
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("FLAMETHROWER");
             m.addMove("QUICK ATTACK");
             m.addMove("SMOG");
             m.addMove("FIRE SPIN");
             p = rival_e4_F.pokes.get(5); //FLAREON
             p.setMoveset(m);
             
             m = new Moveset();
             m.addMove("HYDRO PUMP");
             m.addMove("AURORA BEAM");
             m.addMove("QUICK ATTACK");
             m.addMove("MIST");
             p = rival_e4_V.pokes.get(5); //VAPOREON
             p.setMoveset(m);
         }
     }
 }
