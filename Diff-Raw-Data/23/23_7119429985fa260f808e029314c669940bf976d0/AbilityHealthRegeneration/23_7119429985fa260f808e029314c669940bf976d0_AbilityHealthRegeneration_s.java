 package org.monk.MineQuest.Ability;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.monk.MineQuest.MineQuest;
 import org.monk.MineQuest.Ability.Ability;
 import org.monk.MineQuest.Ability.PassiveAbility;
 import org.monk.MineQuest.Event.AbilityEvent;
 import org.monk.MineQuest.Event.Relative.AuraEvent;
 import org.monk.MineQuest.Quester.Quester;
 
 public class AbilityHealthRegeneration extends Ability implements PassiveAbility {
 	public AbilityHealthRegeneration() {
 		super();
 		config = new int[] {5000, 300000, 1};
 	}
 	
 	@Override
 	public void enable(Quester quester) {
 		super.enable(quester);
		MineQuest.getEventParser().addEvent(new AbilityEvent(config[1], this));
		MineQuest.getEventParser().addEvent(new AuraEvent(quester, config[0], config[1], config[2], true, 0));
 	}
 	
 	@Override
 	public void eventActivate() {
 		super.eventActivate();
 		disable();
 		myclass.getQuester().sendMessage("Health Regeneration Complete!");
 	}
 
 	@Override
 	public void castAbility(Quester quester, Location location,
 			LivingEntity entity) {
 		// Passive
 	}
 
 	@Override
 	public List<ItemStack> getManaCost() {
 		List<ItemStack> list = new ArrayList<ItemStack>();
 
 		list.add(new ItemStack(Material.BREAD, 1));
 		list.add(new ItemStack(Material.BREAD, 1));
 		
 		return list;
 	}
 
 	@Override
 	public String getName() {
 		return "Health Regeneration";
 	}
 
 	@Override
 	public int getReqLevel() {
 		return 20;
 	}
 
 	@Override
 	public String getSkillClass() {
 		return "Warrior";
 	}
 
 }
