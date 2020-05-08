 package org.nolat.rsircbot.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 
 import org.nolat.rsircbot.tools.Calculate;
 
 public class HiscoreData {
     private static final String baseUrl = "http://services.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
     private String username;
     private String[] skillNames = { "overall", "attack", "defence", "strength", "hitpoints",
             "ranged", "prayer", "magic", "cooking", "woodcutting", "fletching",
             "fishing", "firemaking", "crafting", "smithing", "mining", "herblore",
             "agility", "thieving", "slayer", "farming", "runecraft", "hunter", "construction" };
 
     private HashMap<String, RankLevelXp> hiscores = new HashMap<String, RankLevelXp>();
 
     public HiscoreData(String username) throws IOException {
         this.username = username;
         username = username.replaceAll(" ", "_");
         System.out.println("Fetching hiscore data for " + username);
 
         URL hiscoreUrl = new URL(baseUrl + username);
         URLConnection yc = hiscoreUrl.openConnection();
         BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
         String inputLine;
         int c = 0;
         while ((inputLine = in.readLine()) != null) {
             String[] tokens = inputLine.split(",");
             if (tokens.length == 3) {
                 RankLevelXp rlx = new RankLevelXp(tokens[0], tokens[1], tokens[2]);
                 if (c <= skillNames.length) {
                     hiscores.put(skillNames[c], rlx);
                 }
                 c++;
             }
         }
        in.close();
     }
 
     public RankLevelXp getDataForSkill(String skill) {
         return hiscores.get(skill.toLowerCase());
     }
 
     public int getCombatLevel() {
         int atk = hiscores.get("attack").getLevel();
         int str = hiscores.get("strength").getLevel();
         int def = hiscores.get("defence").getLevel();
         int prayer = hiscores.get("prayer").getLevel();
         int range = hiscores.get("ranged").getLevel();
         int magic = hiscores.get("magic").getLevel();
         int hp = hiscores.get("hitpoints").getLevel();
         return Calculate.combatLevel(atk, str, def, prayer, range, magic, hp);
     }
 
     public RankLevelXp getOverall() {
         return getDataForSkill("overall");
     }
 
     public RankLevelXp getAttack() {
         return getDataForSkill("attack");
     }
 
     public RankLevelXp getDefence() {
         return getDataForSkill("defence");
     }
 
     public RankLevelXp getStrength() {
         return getDataForSkill("strength");
     }
 
     public RankLevelXp getHitpoints() {
         return getDataForSkill("hitpoints");
     }
 
     public RankLevelXp getRanged() {
         return getDataForSkill("ranged");
     }
 
     public RankLevelXp getPrayer() {
         return getDataForSkill("prayer");
     }
 
     public RankLevelXp getMagic() {
         return getDataForSkill("magic");
     }
 
     public RankLevelXp getCooking() {
         return getDataForSkill("cooking");
     }
 
     public RankLevelXp getWoodcutting() {
         return getDataForSkill("woodcutting");
     }
 
     public RankLevelXp getFletching() {
         return getDataForSkill("fletching");
     }
 
     public RankLevelXp getFishing() {
         return getDataForSkill("fishing");
     }
 
     public RankLevelXp getFiremaking() {
         return getDataForSkill("firemaking");
     }
 
     public RankLevelXp getCrafting() {
         return getDataForSkill("crafting");
     }
 
     public RankLevelXp getSmithing() {
         return getDataForSkill("smithing");
     }
 
     public RankLevelXp getMining() {
         return getDataForSkill("mining");
     }
 
     public RankLevelXp getHerblore() {
         return getDataForSkill("herblore");
     }
 
     public RankLevelXp getAgility() {
         return getDataForSkill("agility");
     }
 
     public RankLevelXp getThieving() {
         return getDataForSkill("thieving");
     }
 
     public RankLevelXp getSlayer() {
         return getDataForSkill("slayer");
     }
 
     public RankLevelXp getFarming() {
         return getDataForSkill("farming");
     }
 
     public RankLevelXp getRunecraft() {
         return getDataForSkill("runecraft");
     }
 
     public RankLevelXp getHunter() {
         return getDataForSkill("hunter");
     }
 
     public RankLevelXp getConstruction() {
         return getDataForSkill("construction");
     }
 }
