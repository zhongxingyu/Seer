 package com.Cayviel.HardCoreWorlds;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.World;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Creature;
 
 public class MiscFunctions {
 
 	public static CreatureType creatureTypeFromEntity(Entity entity) {
 	    if ( ! (entity instanceof Creature)) {
 	        return null;
 	    }
 	    String name = entity.getClass().getSimpleName();
 	    name = name.substring(5); // Remove "Craft"
 	    name.toUpperCase();
 
 	    return CreatureType.fromName(name);
 	}
 
 	public static String[] mergequotes(String[] args){
 		int wordcount = args.length;
 		if (wordcount == 1){
 			args[0].replace("\"","");
			args[0].replace("\'","");
 			return args;
 		}
 		String argstostring=args[0];
 		for (int i=1; i<wordcount; i++){
 			argstostring = argstostring+" "+args[i];
 		}
 		List<String> matchList = new ArrayList<String>();
 		Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
 		Matcher regexMatcher = regex.matcher(argstostring);
 		while (regexMatcher.find()) {
 		    matchList.add(regexMatcher.group());
 		}
 
 		String[] newargs = new String[matchList.size()];
 		for (int i = 0; i<matchList.size(); i++){
 			newargs[i]=matchList.get(i);
 			newargs[i]=newargs[i].replace("\"","");
 			newargs[i]=newargs[i].replace("\'","");
 		}
 		return newargs;
 	}
 	
 	public static void WorldListUpdate(World world){
 		if (world == null) return;
 		Boolean wEx;
 		Object owEx = Config.config.get("Worlds."+world.getName()+".Hardcore");
 		if (owEx instanceof Boolean){
 			wEx = (Boolean)owEx;
 		}else{
 			if(owEx!=null){
 				HardCoreWorlds.log.info("[HardCoreWorlds]: Unrecognized value in field Hardcore for world: "+world.getName()); return;
 			}else{
 				wEx = null;
 			}
 		}
 		
 		String dEx = Config.config.getString("Worlds."+world.getName()+".Mob Difficulty");
 		Integer bEx = Config.config.getInt("Worlds."+world.getName()+".Ban Duration");
 
 		if (wEx == null){
 			Config.config.set("Worlds."+world.getName()+".Hardcore", false);
 			Config.config.set("Worlds."+world.getName()+".Mob Difficulty","Hard");
 			Config.config.set("Worlds."+world.getName()+".Ban Duration",-1);
 			FileSetup.saveconfig(Config.config, Config.configfile);
 		}else{
 			if(wEx != null &&((dEx == null)||(bEx==null))){
 				if (bEx == null){
 					Config.config.set("Worlds."+world.getName()+".Ban Duration",-1);
 					FileSetup.saveconfig(Config.config, Config.configfile);
 				}
 				if (dEx == null){
 					Config.config.set("Worlds."+world.getName()+".Mob Difficulty","Hard");
 					FileSetup.saveconfig(Config.config, Config.configfile);
 				}
 			}
 		}
 
 	}
 	
 	public static boolean sContainsInt(String s){
 		try{Integer.parseInt(s); return true;}catch(NumberFormatException  e){
 			return false;
 		}
 	}
 }
 
 
