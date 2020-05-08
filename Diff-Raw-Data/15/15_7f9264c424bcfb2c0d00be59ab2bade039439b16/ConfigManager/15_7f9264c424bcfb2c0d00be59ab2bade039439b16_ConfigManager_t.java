 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.inventory.ItemStack;
 
 public class ConfigManager {
 
 	static String configDirectory = "./DwarfCraft/";
 	static String configSkillsFileName = "skills.config";
 	static String configEffectsFileName = "effects.config";
 		
 	
 	static int maximumSkillCount = 100;
 	static int maximumEffectCount = 1000;	
 	
 	static List<Skill> skillsArray = new ArrayList<Skill>();
 	
 	public static boolean setUpSkillsArray(){
 		String line = "";
 		
 		try {
 			FileReader fr = new FileReader(configDirectory + configSkillsFileName);
 			BufferedReader br = new BufferedReader(fr);
 			for(int row = 0; row < maximumSkillCount; row++) {
 				line = br.readLine();
 				if(line == null) continue;
 				if(line.charAt(0) == '#') {
 					row--;
 					continue;
 				}
 				String[] theline = line.split(",");
 				if (theline.length < 11){ 
 					continue;
 					}
 				//Creating a new Skill - with ID, Name, School read from file
 				int id = Integer.parseInt(theline[0]);
 				String displayName = theline[1];
 				School school = School.getSchool(theline[2]);
 				//New skill initialized with level 0
 				int level = 0;
 				//Training cost stack array created, including "empty" itemstacks of type 0 qty 0
 				ItemStack[] trainingCost = {
 						new ItemStack(Integer.parseInt(theline[3]), Integer.parseInt(theline[4])),
 						new ItemStack(Integer.parseInt(theline[5]), Integer.parseInt(theline[6])),
 						new ItemStack(Integer.parseInt(theline[7]), Integer.parseInt(theline[8]))};
 				//training multipliers taken from file
 				double noviceIncrement = Double.parseDouble(theline[9]);
 				double masterMultiplier = Double.parseDouble(theline[10]);
 				//Effects generated from effects file
 				Effect[] effects;
 				effects = new Effect[10];
 				//create the new skill in the skillsarray
				skillsArray.add(new Skill(id, displayName, school, level, effects, trainingCost, noviceIncrement, masterMultiplier));
 			}
 			return true;
 		}
 		catch(FileNotFoundException fN) {
 			fN.printStackTrace();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static boolean setUpEffects(){
 		String line = "";
 		try {
 			FileReader fr = new FileReader(configDirectory + configEffectsFileName);
 			BufferedReader br = new BufferedReader(fr);
 			
 			effectplacingloop: 
 			for(int row = 0; row < maximumEffectCount; row++) {
 				line = br.readLine();
 				if(line == null) continue;
 				if(line.charAt(0) == '#') {
 					row--;
 					continue;
 				}
 				String[] theline = line.split(",");
 				if (theline.length < 20){ 
 					continue;
 					}
 				
 				int effectId = Integer.parseInt(theline[0]); 
 				double baseValue = Double.parseDouble(theline[1]);
 				double levelUpMultiplier = Double.parseDouble(theline[2]);
 				double noviceLevelUpMultiplier = Double.parseDouble(theline[3]);
 				double minValue = Double.parseDouble(theline[4]);
 				double maxValue = Double.parseDouble(theline[5]);
 				boolean floorResult = (theline[6] == "1"); 
 				boolean hasException = (theline[7] == "1"); 
 				int exceptionLow = Integer.parseInt(theline[8]); 
 				int exceptionHigh = Integer.parseInt(theline[9]); 
 				double exceptionValue = Double.parseDouble(theline[10]);
 				int elfLevel = Integer.parseInt(theline[11]);
 				EffectType effectType = EffectType.getEffectType(theline[12]);
 				int initiator = Integer.parseInt(theline[13]); 
 				int output = Integer.parseInt(theline[14]); 
 				boolean allowFist = (theline[15] == "TRUE");
 
 				int[] tooltable = 
 						{Integer.parseInt(theline[16]),
 						Integer.parseInt(theline[17]),
 						Integer.parseInt(theline[18]),
 						Integer.parseInt(theline[19]),
 						Integer.parseInt(theline[20])};
 
 				for(Skill skill:skillsArray){
 					if(effectId / 10 == skill.id){
 						for(int j=0;j<10;j++){
 							if(skill.effects[j] == null){
 								skill.effects[j] = new Effect(effectId, baseValue, levelUpMultiplier, noviceLevelUpMultiplier, minValue, maxValue, floorResult, hasException, exceptionLow, exceptionHigh, exceptionValue, elfLevel, effectType, initiator, output, allowFist, tooltable);
 								continue effectplacingloop;
 							}
 						}
 					}
 				}
 				
 			}
 			return true;
 		}
 		catch(Exception e){
 			System.out.println(e);
 			return false;
 		}
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	public static List<Skill> getAllSkills() {
 		List<Skill> newSkillsArray = (List<Skill>) ((ArrayList<Skill>) skillsArray).clone();
 		return newSkillsArray;
 	}
 
 	public static int countSkills() {
 		int count = 0;
 		for (Skill s: skillsArray){
 			if (s != null) count++;
 		}
 		return count;
 	}
 
 }
