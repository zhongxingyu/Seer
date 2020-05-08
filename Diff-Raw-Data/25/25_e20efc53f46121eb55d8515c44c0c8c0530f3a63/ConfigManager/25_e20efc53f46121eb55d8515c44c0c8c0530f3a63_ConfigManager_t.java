 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Material;
 
 class ConfigManager {
 
 	private DwarfCraft plugin;
 
 	private String configDirectory;
 
 	private String configMainFileName;
 	private String configSkillsFileName;
 	private int configSkillsVersion;
 	private String configEffectsFileName;
 	private int configEffectsVersion;
 	private String configMessagesFileName;
 	private String configGreeterMessagesFileName;
 	private String dbpath;
 	private List<Skill> skillsArray = new ArrayList<Skill>();
 	protected ConfigManager(DwarfCraft plugin, String directory,
 			String paramsFileName) {
 		this.plugin = plugin;
 		configDirectory = directory;
 		configMainFileName = paramsFileName;
 
 		if (!readConfigFile() || !readSkillsFile() || !readEffectsFile()
				|| !readMessagesFile() ) {
 			System.out
 					.println("[SEVERE] Failed to Enable DwarfCraft Skills and Effects)");
 			plugin.getServer().getPluginManager().disablePlugin(plugin);
 			// TODO failed to init skills
 		}
 
 	}
 
 	protected int countSkills() {
 		int count = 0;
 		for (Skill s : skillsArray) {
 			if (s != null)
 				count++;
 		}
 		return count;
 	}
 
 	protected List<Skill> getAllSkills() {
 		List<Skill> newSkillsArray = new ArrayList<Skill>();
 		for (Skill s : skillsArray) {
 			newSkillsArray.add(s.clone());
 		}
 		return newSkillsArray;
 	}
 
 	protected int getConfigSkillsVersion() {
 		return configSkillsVersion;
 	}
 
 	protected String getDbPath() {
 		return dbpath;
 	}
 
 	private void getDefaultValues() {
 		if (configSkillsVersion == 0)
 			configSkillsVersion = 100;
 		if (configEffectsVersion == 0)
 			configEffectsVersion = 100;
 		if (configSkillsFileName == null)
 			configSkillsFileName = "skills.config";
 		if (configEffectsFileName == null)
 			configEffectsFileName = "effects.config";
 		;
 		if (configMessagesFileName == null)
 			configMessagesFileName = "messages.config";
 		if (configGreeterMessagesFileName == null)
 			configGreeterMessagesFileName = "greeters.config";
 		if (dbpath == null)
 			dbpath = "./DwarfCraft/dwarfcraft.db";
 		if (Messages.PRIMARYRACECONFIRM == null)
 			Messages.PRIMARYRACECONFIRM = Messages.Fixed.PRIMARYRACECONFIRM
 					.getMessage();
 		if (Messages.PRIMARYRACESUCCESS == null)
 			Messages.PRIMARYRACESUCCESS = Messages.Fixed.PRIMARYRACESUCCESS
 					.getMessage();
 		if (Messages.SECONDARYRACEALREADY == null)
 			Messages.SECONDARYRACEALREADY = Messages.Fixed.SECONDARYRACEALREADY
 					.getMessage();
 		if (Messages.SECONDARYRACECONFIRM == null)
 			Messages.SECONDARYRACECONFIRM = Messages.Fixed.SECONDARYRACECONFIRM
 					.getMessage();
 		if (Messages.SECONDARYRACESUCCESS == null)
 			Messages.SECONDARYRACESUCCESS = Messages.Fixed.SECONDARYRACESUCCESS
 					.getMessage();
 	}
 
 	private boolean readConfigFile() {
 		try {
 			System.out.println("DC Init: Reading Config File: "
 					+ configDirectory + configMainFileName);
 			getDefaultValues();
 			FileReader fr = new FileReader(configDirectory + configMainFileName);
 			BufferedReader br = new BufferedReader(fr);
 			String line = br.readLine();
 			while (line != null) {
 				if (line.length() == 0) {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '#') {
 					line = br.readLine();
 					continue;
 				}
 				String[] theline = line.split(":");
 				if (theline.length > 2) {
 					line = br.readLine();
 					continue;
 				}
 				if (theline[0].equalsIgnoreCase("Skills File Name"))
 					configSkillsFileName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Effects File Name"))
 					configEffectsFileName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Messages File Name"))
 					configMessagesFileName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Greeter Messages File Name"))
 					configGreeterMessagesFileName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Database File Name"))
 					dbpath = configDirectory + theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Debug Level"))
 					DwarfCraft.debugMessagesThreshold = Integer
 							.parseInt(theline[1].trim());
 				if (theline[0].equalsIgnoreCase("Primary Race Name"))
 					Messages.primaryRaceName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Secondary Race Name"))
 					Messages.secondaryRaceName = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Primary Race Plural"))
 					Messages.primaryRacePlural = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Secondary Race Plural"))
 					Messages.secondaryRacePlural = theline[1].trim();
 				line = br.readLine();
 			}
 
 		} catch (FileNotFoundException fN) {
 			fN.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	private boolean readEffectsFile() {
 		System.out.println("DC Init: Reading effects file: " + configDirectory
 				+ configEffectsFileName);
 		String line = "";
 		try {
 			FileReader fr = new FileReader(configDirectory
 					+ configEffectsFileName);
 			BufferedReader br = new BufferedReader(fr);
 			line = br.readLine();
 			while (line != null) {
 				if (line.length() == 0) {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '#') {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '^') {
 					configEffectsVersion = Integer.parseInt(line.substring(2));
 					line = br.readLine();
 					continue;
 				}
 				String[] theline = line.split(",");
 				if (theline.length < 20) {
 					line = br.readLine();
 					continue;
 				}
 
 				int effectId = Integer.parseInt(theline[0]);
 				double baseValue = Double.parseDouble(theline[1]);
 				double levelUpMultiplier = Double.parseDouble(theline[2]);
 				double noviceLevelUpMultiplier = Double.parseDouble(theline[3]);
 				double minValue = Double.parseDouble(theline[4]);
 				double maxValue = Double.parseDouble(theline[5]);
 				boolean floorResult = (theline[6].equalsIgnoreCase("TRUE"));
 				boolean hasException = (theline[7].equalsIgnoreCase("TRUE"));
 				int exceptionLow = Integer.parseInt(theline[8]);
 				int exceptionHigh = Integer.parseInt(theline[9]);
 				double exceptionValue = Double.parseDouble(theline[10]);
 				int elfLevel = Integer.parseInt(theline[11]);
 				EffectType effectType = EffectType.getEffectType(theline[12]);
 				int initiator = Integer.parseInt(theline[13]);
 				int output = Integer.parseInt(theline[14]);
 				boolean toolRequired = (theline[15].equalsIgnoreCase("TRUE"));
 
 				int[] tooltable = { Integer.parseInt(theline[16]),
 						Integer.parseInt(theline[17]),
 						Integer.parseInt(theline[18]),
 						Integer.parseInt(theline[19]),
 						Integer.parseInt(theline[20]) };
 
 				for (Skill skill : skillsArray) {
 					if (effectId / 10 == skill.getId()) {
 						skill.getEffects().add(
 								new Effect(effectId, baseValue,
 										levelUpMultiplier,
 										noviceLevelUpMultiplier, minValue,
 										maxValue, floorResult, hasException,
 										exceptionLow, exceptionHigh,
 										exceptionValue, elfLevel, effectType,
 										initiator, output, toolRequired,
 										tooltable));
 						break;
 					}
 				}
 				line = br.readLine();
 			}
 			return true;
 		} catch (FileNotFoundException fN) {
 			fN.printStackTrace();
 			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
	protected boolean readGreeterMessagesfile() {
 		System.out.println("DC Init: Reading greeter messages file: "
 				+ configDirectory + configGreeterMessagesFileName);
 		try {
 			getDefaultValues();
 			FileReader fr = new FileReader(configDirectory
 					+ configGreeterMessagesFileName);
 			BufferedReader br = new BufferedReader(fr);
 			String messageId = br.readLine();
 			while (messageId != null) {
 				messageId = messageId.trim();
 				String leftClick, rightClick;
 				if (messageId.length() == 0) {
 					messageId = br.readLine();
 					continue;
 				}
 				if (messageId.charAt(0) == '#') {
 					messageId = br.readLine();
 					continue;
 				}
 				leftClick = br.readLine().trim();
 				rightClick = br.readLine().trim();
 
 				plugin.getDataManager().insertGreeterMessage(messageId,
 						new GreeterMessage(leftClick, rightClick));
 				messageId = br.readLine();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// Default to enum values if not found
 			if (Messages.GeneralInfo == null)
 				Messages.GeneralInfo = Messages.Fixed.GENERALHELPMESSAGE
 						.getMessage();
 			if (Messages.ServerRules == null)
 				Messages.ServerRules = Messages.Fixed.SERVERRULESMESSAGE
 						.getMessage();
 		}
 		return true;
 
 	}
 
 	private boolean readMessagesFile() {
 		System.out.println("DC Init: Reading general messages file: "
 				+ configDirectory + configMessagesFileName);
 		try {
 			getDefaultValues();
 			FileReader fr = new FileReader(configDirectory
 					+ configMessagesFileName);
 			BufferedReader br = new BufferedReader(fr);
 			String line = br.readLine();
 			while (line != null) {
 				if (line.length() == 0) {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '#') {
 					line = br.readLine();
 					continue;
 				}
 				String[] theline = line.split(":");
 				if (theline.length > 2) {
 					line = br.readLine();
 					continue;
 				}
 
 				if (theline[0].equalsIgnoreCase("General Info"))
 					Messages.GeneralInfo = theline[1].trim();
 				if (theline[0].equalsIgnoreCase("Server Rules"))
 					Messages.ServerRules = theline[1].trim();
 
 				line = br.readLine();
 			}
 
 		} catch (FileNotFoundException fN) {
 			fN.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// Default to enum values if not found
 			if (Messages.GeneralInfo == null)
 				Messages.GeneralInfo = Messages.Fixed.GENERALHELPMESSAGE
 						.getMessage();
 			if (Messages.ServerRules == null)
 				Messages.ServerRules = Messages.Fixed.SERVERRULESMESSAGE
 						.getMessage();
 		}
 		return true;
 	}
 
 	private boolean readSkillsFile() {
 		String line = "";
 		System.out.println("DC Init: Reading skills file: " + configDirectory
 				+ configSkillsFileName);
 		try {
 			FileReader fr = new FileReader(configDirectory
 					+ configSkillsFileName);
 			BufferedReader br = new BufferedReader(fr);
 			line = br.readLine();
 			while (line != null) {
 				if (line.length() == 0) {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '#') {
 					line = br.readLine();
 					continue;
 				}
 				if (line.charAt(0) == '^') {
 					configSkillsVersion = Integer.parseInt(line.substring(2));
 					line = br.readLine();
 					continue;
 				}
 				String[] theline = line.split(",");
 				if (theline.length < 12) {
 					continue;
 				}
 				// Creating a new Skill
 				Material trainerHeldMaterial = Material.AIR;
 
 				int id = Integer.parseInt(theline[0]);
 				String displayName = theline[1];
 				// New skill initialized with level 0
 				int level = 0;
 
 				// Training cost stack array created, including "empty"
 				// itemstacks of type 0 qty 0
 				Material TrainingItem1Mat = Material.getMaterial(Integer
 						.parseInt(theline[2]));
 				double TrainingItem1BaseCost = Double.parseDouble(theline[3]);
 				int TrainingItem1MaxAmount = Integer.parseInt(theline[4]);
 				Material TrainingItem2Mat = Material.getMaterial(Integer
 						.parseInt(theline[5]));
 				double TrainingItem2BaseCost = Double.parseDouble(theline[6]);
 				int TrainingItem2MaxAmount = Integer.parseInt(theline[7]);
 				Material TrainingItem3Mat = Material.getMaterial(Integer
 						.parseInt(theline[8]));
 				double TrainingItem3BaseCost = Double.parseDouble(theline[9]);
 				int TrainingItem3MaxAmount = Integer.parseInt(theline[10]);
 				trainerHeldMaterial = Material.getMaterial(Integer
 						.parseInt(theline[11]));
 				// Effects generated from effects file
 				List<Effect> effects = new ArrayList<Effect>();
 				// create the new skill in the skillsarray
 
 				skillsArray.add(new Skill(id, displayName, level, effects,
 						TrainingItem1Mat, TrainingItem1BaseCost,
 						TrainingItem1MaxAmount, TrainingItem2Mat,
 						TrainingItem2BaseCost, TrainingItem2MaxAmount,
 						TrainingItem3Mat, TrainingItem3BaseCost,
 						TrainingItem3MaxAmount, trainerHeldMaterial));
 
 				line = br.readLine();
 			}
 			return true;
 		} catch (FileNotFoundException fN) {
 			fN.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
