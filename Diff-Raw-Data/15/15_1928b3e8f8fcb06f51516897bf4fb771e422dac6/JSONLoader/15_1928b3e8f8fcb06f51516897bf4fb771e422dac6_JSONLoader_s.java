 package com.gamegear.firstwing.levels;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.gamegear.firstwing.FirstWing;
 import com.gamegear.firstwing.actors.json.ActorFile;
 
 public class JSONLoader  {
 	
 	public com.gamegear.firstwing.levels.json.LevelProperties getLevel(FileHandle mapHandler)
 	{
 		com.gamegear.firstwing.levels.json.LevelProperties level = null;
 		String jsonFile = "";
 		try {
 			jsonFile = mapHandler.readString();
 		} catch (Exception e) {
 			System.out.println("File didn't load");
 		}
 		mapHandler = null;
 	 
 		try {
 			//convert JSON string to Map
 			level = FirstWing.mapper.readValue(jsonFile, new TypeReference<com.gamegear.firstwing.levels.json.LevelProperties>(){});
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return level;
 	}
 	
 	public ActorFile getActorFile(FileHandle mapHandler)
 	{
 		ActorFile level = null;
 		String jsonFile = "";
 		try {
 			jsonFile = mapHandler.readString();
 		} catch (Exception e) {
 			System.out.println("File didn't load");
 		}
 		
 		mapHandler = null;
 	 
 		try {
 			//convert JSON string to Map
 			level = FirstWing.mapper.readValue(jsonFile, new TypeReference<ActorFile>(){});
	 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return level;
 	}
 }
