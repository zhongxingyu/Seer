 package me.scarfacehd2.DarianLikesDick;
 
import java.util.logging.Logger;

 public class DLDUtil {
 
	static Logger log = Logger.getLogger("Minecraft");
 	
 	/*
 	 *  consoleMSG : Send (String)msg to the console with level (String)level.
 	 */
 	public static void consoleMSG(String level, String msg)
 	{
		if(level == "warning") log.warning("[DarianLikesDick] " + msg);
		if(level == "severe") log.severe("[DarianLikesDick] " + msg);
		else log.info("[DarianLikesDick] " + msg);
 	}
 	
 }
