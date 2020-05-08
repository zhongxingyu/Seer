 package com.elmakers.mine.bukkit.plugins.wand;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.elmakers.mine.bukkit.plugins.spells.SpellVariant;
 import com.elmakers.mine.bukkit.plugins.spells.Spells;
 
 class WandPlayerListener extends PlayerListener 
 {
 	private Wands wands;
 	
 	public void setWands(Wands wands)
 	{
 		this.wands = wands;
 	}
 	
 	 /**
      * Called when a player plays an animation, such as an arm swing
      * 
      * @param event Relevant event details
      */
 	@Override
     public void onPlayerAnimation(PlayerAnimationEvent event) 
 	{
 		Player player = event.getPlayer();
 		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
 		{
 			if (event.getPlayer().getInventory().getItemInHand().getTypeId() == wands.getWandTypeId())
 			{
 				WandPermissions permissions = wands.getPermissions(player.getName());	
 				if (!permissions.canUse())
 				{
 					return;
 				}
 				
 				Inventory inventory = player.getInventory();
 				ItemStack[] contents = inventory.getContents();
 				
 				SpellVariant spell = null;
 				for (int i = 0; i < 9; i++)
 				{
					if (contents[i] == null || contents[i].getType() == Material.AIR || contents[i].getTypeId() == wands.getWandTypeId())
 					{
 						continue;
 					}
 					spell = wands.getSpells().getSpell(contents[i].getType(), player);
 					if (spell != null)
 					{
 						break;
 					}
 				}
 				
 				if (spell != null)
 				{
 					wands.getSpells().castSpell(spell, player);
 				}			
 			}
 		}
     }
 	
 	public boolean cycleMaterials(Player player)
 	{
 		Spells spells = wands.getSpells();
 		List<Material> buildingMaterials = spells.getBuildingMaterials();
 		PlayerInventory inventory = player.getInventory();
 		ItemStack[] contents = inventory.getContents();
 		int firstMaterialSlot = 8;
 		boolean foundAir = false;
 		
 		for (int i = 8; i >= 0; i--)
 		{
 			Material mat = contents[i] == null ? Material.AIR : contents[i].getType();
 			if (mat == Material.AIR)
 			{
 				if (foundAir)
 				{
 					break;
 				}
 				else
 				{
 					foundAir = true;
 					firstMaterialSlot = i;
 					continue;
 				}
 			}
 			else
 			{
 				if (buildingMaterials.contains(mat))
 				{
 					firstMaterialSlot = i;
 					continue;
 				}
 				else
 				{
 					break;
 				}
 			}
 		}
 		
 		if (firstMaterialSlot == 8) return false;
 		
 		ItemStack lastSlot = contents[8];
 		SetInventoryTask setInventory = new SetInventoryTask(player);
 		for (int i = 7; i >= firstMaterialSlot; i--)
 		{
 		    setInventory.addItem(i + 1, contents[i]);
 		}
 		setInventory.addItem(firstMaterialSlot, lastSlot);
 		
 		BukkitScheduler bs = wands.getServer().getScheduler();
         bs.scheduleSyncDelayedTask(wands.getPlugin(), setInventory, 1);
 		
 		return true;
 	}
 	
 	public void cycleSpells(Player player)
 	{
 		Spells spells = wands.getSpells();
 		Inventory inventory = player.getInventory();
 		ItemStack[] contents = inventory.getContents();
 		ItemStack[] active = new ItemStack[9];
 		
 		List<Material> newSpellOrder = new ArrayList<Material>();
 		
 		for (int i = 0; i < 9; i++) { active[i] = contents[i]; }
 		
 		int firstSpellSlot = -1;
 		for (int i = 0; i < 9; i++)
 		{
 			boolean isEmpty = active[i] == null;
 			Material activeType = isEmpty ? Material.AIR : active[i].getType();
 			boolean isWand = activeType.getId() == wands.getWandTypeId();
 			boolean isSpell = false;
 			if (activeType != Material.AIR)
 			{
 				SpellVariant spell = spells.getSpell(activeType, player);
 				isSpell = spell != null;
 			}
 			
 			if (isSpell)
 			{
 				if (firstSpellSlot < 0) firstSpellSlot = i;
 				newSpellOrder.add(activeType);
 				inventory.remove(active[i]);
 			}
 			else
 			{
 				if (!isWand && firstSpellSlot >= 0)
 				{
 					break;
 				}
 			}
 			
 		}
 		
 		if (newSpellOrder.size() > 0)
 		{
 		    newSpellOrder.add(newSpellOrder.remove(0));
 		    List<ItemStack> items = new ArrayList<ItemStack>();
 		    for (Material spellMat : newSpellOrder)
 		    {
 		        ItemStack itemStack = new ItemStack(spellMat, 1);
 		        items.add(itemStack);	        
 		    }
 		    
 		    BukkitScheduler bs = wands.getServer().getScheduler();
 		    bs.scheduleSyncDelayedTask(wands.getPlugin(), new AddToInventoryTask(player, items), 1);
 		}
 	}
 	
 	public void spellHelp(Player player)
 	{
 		if (!wands.showItemHelp()) return;
 		
 		// Check for magic item
 		Inventory inventory = player.getInventory();
 		ItemStack[] contents = inventory.getContents();
 		Spells spells = wands.getSpells();
 		
 		boolean inInventory = false;
 		boolean foundInventory = false;
 		SpellVariant spell = null;
 		boolean hasWand = false;
 		
 		for (int i = 0; i < 9; i++)
 		{
 			if (contents[i].getTypeId() == wands.getWandTypeId())
 			{
 				hasWand = true;
 				continue;
 			}
 			
 			if (contents[i].getType() != Material.AIR)
 			{
 				SpellVariant ispell = spells.getSpell(contents[i].getType(), player);
 
 				if (!foundInventory)
 				{
 					if (!inInventory)
 					{
 						if (ispell != null)
 						{
 							inInventory = true;
 						}
 					}
 					else
 					{
 						if (ispell == null)
 						{
 							inInventory = false;
 							foundInventory = true;
 						}
 					}
 				}
 				
 				if (inInventory && i == player.getInventory().getHeldItemSlot())
 				{
 					spell = ispell;
 				}
 			}
 		}
 
 		if (hasWand && spell != null)
 		{
 			player.sendMessage(spell.getName() + " : " + spell.getDescription());
 		}
 	}
   
     /**
      * Called when a player uses an item
      * 
      * @param event Relevant event details
      */
 	@Override
     public void onPlayerInteract(PlayerInteractEvent event) 
 	{
 		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
     	{
 			int materialId = event.getPlayer().getInventory().getItemInHand().getTypeId();
 			Player player = event.getPlayer();
 			WandPermissions permissions = wands.getPermissions(player.getName());
 	
 			if (!permissions.canUse())
 			{
 				return;
 			}
 			
 			boolean cycleSpells = false;
 	
 			cycleSpells = player.isSneaking();
 			if (materialId == wands.getWandTypeId())
 			{	
 				if (cycleSpells)
 				{
 					if (!cycleMaterials(event.getPlayer()))
 					{
 						cycleSpells(event.getPlayer());
 					}
 				}
 				else
 				{
 					cycleSpells(event.getPlayer());
 				}
 			}
 			/*
 			else
 			{
 				spellHelp(event.getPlayer());
 			}
 			*/
     	}
     }
 
 }
