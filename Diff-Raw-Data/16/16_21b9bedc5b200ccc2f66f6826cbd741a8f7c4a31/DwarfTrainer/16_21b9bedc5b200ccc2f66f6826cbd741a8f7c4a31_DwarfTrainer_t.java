 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Entity;
 import org.bukkit.Location;
 import com.smartaleq.bukkit.dwarfcraft.ui.Out;
 
 import redecouverte.npcspawner.*;
 
 public class DwarfTrainer {
 	// public static BasicHumanNpcList HumanNpcList;
 	private BasicHumanNpc basicHumanNpc;
 	private Integer skillId;
 
 	// constructor only for *trainers*
 	public DwarfTrainer (Player player, String uniqueId, String name, Integer skillId) {
 		this.skillId = skillId; 
 
 		basicHumanNpc = NpcSpawner.SpawnBasicHumanNpc(
 				uniqueId, 
 				name, 
 				player.getWorld(), 
 				player.getLocation().getX(), 
 				player.getLocation().getY(), 
 				player.getLocation().getZ(), 
 				player.getLocation().getYaw(), 
 				player.getLocation().getPitch());
 		
 		Material material = Dwarf.find(player).getSkill(skillId).getTrainerHeldMaterial();
 		assert (material != null);
 		
		ItemStack itemStack = new ItemStack(material);
		itemStack.setAmount(1);
		itemStack.setDurability((short)0);
		basicHumanNpc.getBukkitEntity().setItemInHand(itemStack);
 	}
 	
 	public boolean equals(Object that) {
 		if ( this == that ) {
 			return true;
 		}
 		else if ( that instanceof HumanEntity ) {
 			if ( basicHumanNpc.getBukkitEntity().getEntityId() == ((HumanEntity)that).getEntityId()) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public String getUniqueId() { return basicHumanNpc.getUniqueId(); }
 	public String getName() { return basicHumanNpc.getName(); }
 	public BasicHumanNpc getBasicHumanNpc() { return basicHumanNpc; }
 	public Integer getSkillTrained() { return skillId; }
 	
 	public void lookAt(Entity target) {
 		assert(target != null);
 		Location l;
 		l = target.getLocation().clone();
 		if ( target instanceof Player ) {
 			l.setY(l.getY()+((Player) target).getEyeHeight());
 		}
 		this.lookAt(l);
 		return;
 	}
 	
 	public void lookAt(Location l) {
 		assert(l != null);
 		return;
 	}
 	
 	public void printSkillInfo(Player player){
 		Out.printSkillInfo(player, Dwarf.find(player).getSkill(this.skillId));
 	}
 	
 	public void trainSkill(Player player){
 		boolean soFarSoGood = true;
 		Dwarf dwarf = (Dwarf.find(player));
 		assert(dwarf != null);
 		Skill skill = dwarf.getSkill(this.skillId);
 		assert(skill != null);
 		
 		List <ItemStack> trainingCosts = dwarf.calculateTrainingCost(skill); 
 		
 		//Must be a dwarf, not an elf
 		if (dwarf.isElf) {
 			Out.sendMessage(dwarf, "&cYou are an &fElf &cnot a &9Dwarf&6!", "&6[Train &b"+skill.id+"&6] ");
 			soFarSoGood = false;
 		}
 		else Out.sendMessage(dwarf, "&aYou are a &9Dwarf &aand can train skills.", "&6[Train &b"+skill.id+"&6] ");
 		
 		//Must have skill level between 0 and 29
 		if ( skill.level >= 30) {
 			Out.sendMessage(dwarf, "&cYour skill is max level (30)!", "&6[Train &b"+skill.id+"&6] ");
 			soFarSoGood = false;
 		}
 		
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
 			return;
 		}
 		else{
 			return; //something else goes here
 		}
 	}
 }
