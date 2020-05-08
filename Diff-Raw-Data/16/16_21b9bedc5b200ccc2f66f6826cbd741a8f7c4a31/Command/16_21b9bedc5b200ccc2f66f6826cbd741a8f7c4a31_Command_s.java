 package com.smartaleq.bukkit.dwarfcraft.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import com.smartaleq.bukkit.dwarfcraft.*;
 import com.smartaleq.bukkit.dwarfcraft.ui.Out;
 
 public class Command {
 	
 	Player player;
 	String[] playerInput;
 	private final DwarfCraft plugin;
 	
 	public Command(DwarfCraft instance, Player player, String[] playerInput){
 		this.plugin = instance;
 		this.player = player;
 		this.playerInput = playerInput;
 	}
 	
 	public Player getPlayer(String playerName){
 		Player[] players = plugin.getServer().getOnlinePlayers();
         for (Player player : players) {
             if (player.getName().equalsIgnoreCase(playerName)) return player;
         }
         return null;
 	}	
 	
 
 	
 	public boolean execute(){
 		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started execute");
 
 		if (playerInput[0].equalsIgnoreCase("debug")) return debug();
 
 		if (playerInput[0].equalsIgnoreCase("help")) return help();
 		if (playerInput[0].equalsIgnoreCase("?")) return help();
 		if (playerInput[0].equalsIgnoreCase("info")) return info();
 		if (playerInput[0].equalsIgnoreCase("rules")) return rules();
 		if (playerInput[0].equalsIgnoreCase("commands")) return commands(1);
 		if (playerInput[0].equalsIgnoreCase("commands2")) return commands(2);
 		if (playerInput[0].equalsIgnoreCase("tutorial")) return tutorial(1);
 		if (playerInput[0].equalsIgnoreCase("tutorial2")) return tutorial(2);
 		if (playerInput[0].equalsIgnoreCase("tutorial3")) return tutorial(3);
 		if (playerInput[0].equalsIgnoreCase("tutorial4")) return tutorial(4);
 		if (playerInput[0].equalsIgnoreCase("tutorial5")) return tutorial(5);
 		if (playerInput[0].equalsIgnoreCase("skillsheet")) 			return skillSheet();
 		if (playerInput[0].equalsIgnoreCase("skillinfo")) 			return skillInfo();
 		if (playerInput[0].equalsIgnoreCase("effectinfo")) 			return effectInfo();
 		
 		if (playerInput[0].equalsIgnoreCase("train")) 				return train();
 		if (playerInput[0].equalsIgnoreCase("setskill")) 			return (player.isOp() ? setSkill(): notAnOpError());
 		if (playerInput[0].equalsIgnoreCase("setskill")) 			return (player.isOp() ? setAll(): notAnOpError());
 		
 		if (playerInput[0].equalsIgnoreCase("MAKE"+Messages.primaryRaceName)) 		return makeMeDwarf(false);
 		if (playerInput[0].equalsIgnoreCase("REALLYMAKE"+Messages.primaryRaceName))	return makeMeDwarf(true);
 		if (playerInput[0].equalsIgnoreCase("MAKE"+Messages.secondaryRaceName)) 	return makeMeElf(false);
 		if (playerInput[0].equalsIgnoreCase("REALLYMAKE"+Messages.secondaryRaceName))return makeMeElf(true);
 		
 		if (playerInput[0].equalsIgnoreCase("schools")) 			return schoolList();
 		if (playerInput[0].equalsIgnoreCase("schoollist")) 			return schoolList();
 		if (playerInput[0].equalsIgnoreCase("school")) 				return schoolInfo();
 		if (playerInput[0].equalsIgnoreCase("schoolinfo")) 			return schoolInfo();
 		if (playerInput[0].equalsIgnoreCase("HERE")) 				return here();
 
 		if (playerInput[0].equalsIgnoreCase("createschool")) 		return (player.isOp() ? createSchool(): notAnOpError());
 		if (playerInput[0].equalsIgnoreCase("removeschool")) 		return (player.isOp() ? removeSchool(): notAnOpError());			
 		if (playerInput[0].equalsIgnoreCase("listschools")) 		return (player.isOp() ? listAllSchools(): notAnOpError());
 		
		if (playerInput[0].equalsIgnoreCase("createmaster"))		return (player.isOp() ? createMaster() : notAnOpError());		
 		if (playerInput[0].equalsIgnoreCase("createtrainer"))		return (player.isOp() ? createTrainer() : notAnOpError());
 		if (playerInput[0].equalsIgnoreCase("removetrainer"))		return (player.isOp() ? removeTrainer() : notAnOpError());
 		if (playerInput[0].equalsIgnoreCase("listtrainers"))		return (player.isOp() ? listTrainers() : notAnOpError());
 		return false;
 	}
 	
 
 
 	private boolean rules() {
 		return Out.rules(player);
 	}
 
 	private boolean listAllSchools() {
 		if(!Out.listSchools(player)){
 			Out.sendMessage(player, Messages.Fixed.ERRORNOZONES.message); 
 			return true;
 		}
 		return true;
 	}
 	
 	private boolean createMaster() {
		DwarfTrainer d = new DwarfTrainer(player, playerInput[1], playerInput[2], 3);
 		DataManager.insertTrainer(d);
 		return true;
 	}
 	private boolean createTrainer() {
 		if ( playerInput[1] == null || playerInput[2] == null || playerInput[3] == null || playerInput[4] != null ) {
 			Out.sendMessage(player, "&cSyntax: createtrainer <ID> <name> <held item>");
 			return true;
 		}
 		if ( DataManager.getTrainer(playerInput[1]) != null ) {
 			Out.sendMessage(player, "&cThis NPC ID is already in use.");
 			return true;
 		}
 		
 		Skill s = Dwarf.find(player).getSkill(playerInput[3]);
 		
 		if ( s == null ) {
 			Out.sendMessage(player, "&cNo such skill.");
 			return true;
 		}
 		
 		DwarfTrainer d = new DwarfTrainer(player, playerInput[1], playerInput[2], s.getId());
 		DataManager.insertTrainer(d);
 		return true;
 	}
 	
 	private boolean removeTrainer() {
 		if ( playerInput[1] == null || playerInput[2] != null ) {
 			Out.sendMessage(player, "&cSyntax: removetrainer <ID>");
 			return true;
 		}
 		
 		if ( DataManager.removeTrainer(playerInput[1]) ) {
 			Out.sendMessage(player, "Trainer removed.");
 		} else {
 			Out.sendMessage(player, "Could not find trainer.");
 		}
 		return true;
 	}
 	
 	private boolean listTrainers() {
 		// print all the trainers here so admins can get UniqueIDs to delete them if needed
 		for ( DwarfTrainer d : DataManager.getTrainerList()) {
 			Out.sendMessage(player, "ID: " + d.getUniqueId() + " Name: " + d.getName() + " Trains: (" + d.getSkillTrained() + ") " + Dwarf.find(player).getSkill(d.getSkillTrained()).displayName);
 		}
 		return true;
 	}
 
 	private boolean removeSchool() {
 		if (playerInput[1]== null) {
 			Out.sendMessage(player, "&cYou need to enter the exact school name.");
 		}
 		if(!DataManager.removeSchoolZone(playerInput[1])){
 			Out.sendMessage(player, "&cYou need to enter the exact school name.");
 		}
 		else return true;
 		return false;
 	}
 
 	/**
 	 * Changes the level of debug reporting in console
 	 */
 	private boolean debug() {
 		try{if (playerInput[1] != null ) {
 			DwarfCraft.debugMessagesThreshold=Integer.parseInt(playerInput[1]);
 			if (DwarfCraft.debugMessagesThreshold < 9) System.out.println("*** DEBUG LEVEL CHANGED TO "+playerInput[1]+" ***");
 			Out.sendBroadcast(plugin.getServer(), "Debug messaging level set to "+DwarfCraft.debugMessagesThreshold);
 			return true;
 		}}
 		catch(NumberFormatException e){
 			Out.sendMessage(player, Messages.Fixed.ERRORBADINPUT.message);
 			return true;
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Sends detailed help text from command help listing or general help text with no argument
 	 */
 	public boolean help() {
 		if (playerInput.length > 3) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		if (playerInput[1] == null)	return Out.generalInfo(player);
 		for (CommandInfo c: CommandInfo.values()){
 			if (playerInput[1].equalsIgnoreCase(c.toString())){	
 				return Out.commandHelp(player, c);
 			}
 		}
 		Out.sendMessage(player, Messages.Fixed.ERRORCOMMANDNOTFOUND.message);
 		return false;
 	}
 	
 	
 	private boolean info() {
 		return Out.info(player);
 	}
 
 	private boolean commands(int i) {
 		return Out.commandList(player, i);
 	}
 	private boolean tutorial(int i) {
 		return Out.tutorial(player, i);
 	}
 
 	/**
 	 * Player command to print current skillsheet
 	 * Syntax: /dc skillsheet [target]
 	 * Does own sanitization and error checking.
 	 * Target is optional, will print caller's skillsheet on null
 	 */
 	private boolean skillSheet() {
 		if (playerInput.length > 4) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf target = parseDwarfNameInput(1);
 		if (target == null) return true;
 		if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: skillsheet target =" + playerInput[1]);
 		return Out.printSkillSheet(target, player, playerInput[1]);
 	}
 
 	/**
 	 * Player command to print skill information
 	 * Syntax: /dc skillinfo <skill>
 	 * <skill> is skill ID or skill name
 	 * Does own error checking.
 	 */
 	private boolean skillInfo() {
 		if (playerInput.length > 3) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = Dwarf.find(player);
 		assert (dwarf != null);
 		Skill skill = dwarf.getSkill(playerInput[1]);
 		if (skill == null) return true;
 		return Out.printSkillInfo(player, skill);
 	}
 
 	/**
 	 * Player command to print effect information
 	 * Syntax: /dc effectinfo <effect>
 	 * <effect> is effect ID
 	 * Does own error checking.
 	 */
 	private boolean effectInfo() {
 		if (playerInput.length > 3) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = Dwarf.find(player);
 		assert(dwarf != null);
 		Effect effect = parseEffectInput(1, dwarf);
 		if(effect == null)return true;
 		return Out.effectInfo(player, effect);
 	}
 	
 	/**
 	 * Player command for training skills
 	 * Syntax: /dc train <skill>
 	 * If successful increases skill, removes items
 	 * Does own error checking
 	 */
 	private boolean train() {
 		if (playerInput.length > 3) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		boolean soFarSoGood = true;
 		Dwarf dwarf = (Dwarf.find(player));
 		assert(dwarf != null);
 		Skill skill = parseSkillInput(1, dwarf);
 		List <ItemStack> trainingCosts = dwarf.calculateTrainingCost(skill); 
 		//Must be a dwarf, not an elf
 		if (dwarf.isElf) {
 			Out.sendMessage(dwarf, "&cYou are one of the &f&t&cnot a &9&p&6!", "&6[Train &b"+skill.id+"&6] ");
 			soFarSoGood = false;
 		}
 		else Out.sendMessage(dwarf, "&aYou are a &9&p &aand can train skills.", "&6[Train &b"+skill.id+"&6] ");
 		
 		//Must have skill level between 0 and 29
 		if ( skill.level >= 30 || skill.level < 0) {
 			Out.sendMessage(dwarf, "&cYour skill is max level (30)!", "&6[Train &b"+skill.id+"&6] ");
 			soFarSoGood = false;
 		}
 			
 		//Must be in training Zone, may change this to near a trainer if/when NPCs are implemented
 		if (!dwarf.isInSchoolZone(skill.school)) {
 			Out.sendMessage(dwarf, "&cYou are not in a &1"+skill.school+" &ctraining zone", "&6[Train &b"+skill.id+"&6] ");
 			soFarSoGood = false;
 		}
 		else Out.sendMessage(dwarf, "&aYou are in a &1"+skill.school+" &atraining zone", "&6[Train &b"+skill.id+"&6] ");
 		
 		//Must have enough materials to train
 		for (ItemStack itemStack: trainingCosts) {
 			if(itemStack == null) continue;
 			if(itemStack.getAmount() == 0) continue;
 			if(dwarf.countItem(itemStack.getTypeId()) < itemStack.getAmount()) {
 				Out.sendMessage(dwarf, "&cYou do not have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &crequired", "&6[Train &b"+skill.id+"&6] ");
 				soFarSoGood = false;
 			}
 			else Out.sendMessage(dwarf, "&aYou have the &2"+itemStack.getAmount() + " " + itemStack.getType()+ " &arequired", "&6[Train &b"+skill.id+"&6] ");
 
 		}
 		
 		//If passed all the 'musts' successfully
 		if(soFarSoGood){
 			skill.level++;
 			for (ItemStack itemStack: trainingCosts)
 				dwarf.removeInventoryItems(itemStack.getTypeId(), itemStack.getAmount());
 			Out.sendMessage(dwarf,"&6Training Successful!","&6[&b"+skill.id+"&6] ");
 			DataManager.saveDwarfData(dwarf);
 			return true;
 		}
 		else{
 			return true; //something else goes here
 		}
 	}
 	
 	/**
 	 * Admin Command to change all of a player's skills.
 	 * Syntax: /dc setall <player> <level>
 	 * <player> is target, <level> is desired level in range 0-30
 	 */
 	private boolean setAll() {
 		if (playerInput.length > 4) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = parseDwarfNameInput(1);
 		if (dwarf == null) return true;
 		if (dwarf.isElf()) {
 			Out.sendMessage(player, "&cError: &ePlayer &9" + playerInput[1] + " &eis an &f&s.");
 			return true;
 		}
 		Integer level = parseSkillLevelInput(2);
 		if (level == -2)return true;			
 		for(Skill s:dwarf.skills) s.level = level;
 		Out.sendMessage(player, "&aAdmin: &eset all skills for player &9" + dwarf.player.getDisplayName() + "&e to &3" + level);
 		DataManager.saveDwarfData(dwarf);
 		return true;
 
 	}
 	
 	/**
 	 * Admin Command to change a player's skill.
 	 * Syntax: /dc setskill <player> <skill> <level>
 	 * <player> is target, <skill> is skill ID or alpha
 	 * <level> is desired level in range 0-30
 	 */
 	private boolean setSkill() {
 		if (playerInput.length > 5) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = parseDwarfNameInput(1);
 		if (dwarf == null) return true;
 		if (dwarf.isElf()) {
 			Out.sendMessage(player, "&cError: &ePlayer &9" + playerInput[1] + " &eis an &f&s.");
 			return true;
 		}
 		Skill skill = parseSkillInput(2, dwarf);
 		if (skill == null) return true;
 		Integer level = parseSkillLevelInput(3);
 		if (level == -2)return true;			
 		skill.level = level;
 		Out.sendMessage(player, "&aAdmin: &eset skill &b" + skill.displayName + "&e for player &9" + dwarf.player.getDisplayName() + "&e to &3" + level);
 		DataManager.saveDwarfData(dwarf);
 		return true;
 	}
 
 	private boolean makeMeDwarf(boolean confirmed) {
 		if (playerInput.length > 2) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = Dwarf.find(player);
 		if (dwarf.isElf) return dwarf.makeElfIntoDwarf(); 
 		else if (confirmed) {
 			Out.becameDwarf(player);
 			return dwarf.makeElfIntoDwarf();
 		}
 		else {
 			Out.confirmBecomingDwarf(player);
 			return true;
 		}
 	}
 	
 	private boolean makeMeElf(boolean confirmed) {
 		if (playerInput.length > 2) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		Dwarf dwarf = Dwarf.find(player);		
 		if (dwarf.isElf) {
 			Out.alreadyElf(player);
 			return true;
 		}
 		else if (confirmed) {
 			Out.becameElf(player);
 			return dwarf.makeElf();
 		}
 		else {
 			Out.confirmBecomingElf(player);
 			return true;
 		}
 	}
 	
 	private boolean schoolList() {
 		if (playerInput.length > 2) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		return Out.schoolList(player);
 	}
 	
 	private boolean schoolInfo() {
 		if (playerInput.length > 3) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		List<Skill> skills = new ArrayList<Skill>();
 		School school = parseSchoolInput(1);
 		for (Skill s: (Dwarf.find(player)).skills){
 			if (s==null) continue;
 			if(s.school == school) {
 				skills.add(s);
 			}
 		}
 		Out.schoolInfo(player, school, skills);
 		return false;
 	}
 	
 	private boolean here() {
 		if (playerInput.length > 2) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		return Out.here(player,(Dwarf.find(player)).listAllZones());
 	}
 	
 	private boolean createSchool() {
 		if (playerInput.length > 10) {
 			Out.sendMessage(player, Messages.Fixed.ERRORTOOMANYINPUTS.message);
 			return true;
 		}
 		try {
 			World world = player.getWorld();
 			School school = parseSchoolInput(1);
 			double x1 = Double.parseDouble(playerInput[2]); 
 			double x2 = Double.parseDouble(playerInput[3]); 
 			double y1 = Double.parseDouble(playerInput[4]); 
 			double y2 = Double.parseDouble(playerInput[5]);
 			double z1 = Double.parseDouble(playerInput[6]); 
 			double z2 = Double.parseDouble(playerInput[7]);
 			String name = playerInput[8];
 			return DataManager.addSchoolZone(new Vector(x1,y1,z1), new Vector(x2,y2,z2), world, school, name);
 		} 
 		catch (NumberFormatException e) {
 			e.printStackTrace();
 			return false;
 		} 		
 	}
 
 	private boolean notAnOpError(){
 		Out.sendMessage(player, Messages.Fixed.ERRORNOTOP.message);
 	return true;
 	}
 
 	private Dwarf parseDwarfNameInput(int argNumber){
 		String dwarfName;
 		Dwarf target = null;
 		try{
 			dwarfName = playerInput[argNumber];
 		}
 		catch (NullPointerException npe){
 			Out.sendMessage(player, Messages.Fixed.ERRORMISSINGINPUT.message);
 			return null;
 		}	
 		if (dwarfName == "") dwarfName = player.getName();
 		Player playerTarget = getPlayer(dwarfName);
 		if (playerTarget != null && playerTarget.isOnline())
 			target = Dwarf.find(playerTarget);
 		else
 			target = Dwarf.findOffline(dwarfName);
 		if (target == null) {
 			Out.sendMessage(player, Messages.Fixed.ERRORINVALIDDWARFNAME.message);
 		}
 		return target;
 	}
 	
 	private int parseSkillLevelInput(int argNumber){
 		int level = -2;
 		try{
 			level = Integer.parseInt(playerInput[argNumber]);
 		}
 		catch (NullPointerException npe){
 			Out.sendMessage(player, Messages.Fixed.ERRORMISSINGINPUT.message);
 			return level;
 		}	
 		catch(NumberFormatException nfe) {
 			Out.sendMessage(player, Messages.Fixed.ERRORNOTANUMBER.message);
 			return level;
 		}
 		if (level >30 || level <-1){
 			Out.sendMessage(player, Messages.Fixed.ERRORINVALIDSKILLLEVEL.message);
 			return -2;
 		}
 		return level;
 	}
 	
 	private Skill parseSkillInput(int argNumber, Dwarf dwarf){
 		Skill skill = null;
 		int skillID = -1;
 		try{
 			skillID = Integer.parseInt(playerInput[argNumber]);
 			skill = dwarf.getSkill(skillID);
 		}
 		catch (NullPointerException npe){
 			Out.sendMessage(player, Messages.Fixed.ERRORMISSINGINPUT.message);
 			return null;
 		}
 		catch(NumberFormatException nfe){}
 		if (skill == null) skill = dwarf.getSkill(playerInput[argNumber]);
 		if (skill == null) Out.sendMessage(player, Messages.Fixed.ERRORNOTVALIDSKILLINPUT.message);
 		return skill;
 		
 	}
 	
 	private Effect parseEffectInput(int argNumber, Dwarf dwarf){
 		Effect effect;
 		int effectId = -1;
 		try{
 			effectId = Integer.parseInt(playerInput[argNumber]);
 			if (effectId < 0 || effectId>=1000){
 				Out.sendMessage(player, Messages.Fixed.ERRORNOTVALIDEFFECTINPUT.message);
 			}
 			effect = dwarf.getEffect(effectId);
 			return effect;
 		}
 		catch (NullPointerException npe){
 			Out.sendMessage(player, Messages.Fixed.ERRORMISSINGINPUT.message);
 			return null;
 		}
 		catch(NumberFormatException nfe){
 			Out.sendMessage(player, Messages.Fixed.ERRORNOTVALIDEFFECTINPUT.message);
 			return null;
 		}		
 	}
 	
 	private School parseSchoolInput(int argNumber){
 		School school = null;
 		int schoolId = -1;
 		try{
 			schoolId = Integer.parseInt(playerInput[argNumber]);
 			school = School.getSchool(schoolId);
 		}
 		catch (NullPointerException npe){
 			Out.sendMessage(player, Messages.Fixed.ERRORMISSINGINPUT.message);
 			return null;
 		}
 		catch(NumberFormatException nfe){
 		}		
 		if (school == null) school = School.getSchool(playerInput[argNumber]);
 		if (school == null) Out.sendMessage(player, Messages.Fixed.ERRORNOTVALIDSKILLINPUT.message);
 		return school;
 	}
 
 }
