 package de.minestar.cok.hook;
 
import java.util.ArrayList;

 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.item.ItemTossEvent;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.game.Team;
 import de.minestar.cok.helper.ChatSendHelper;
 
 public class PlayerListener {
 
 	/**
 	 * Called upon Player deaths, removes given items and changes the captain.
 	 * @param event
 	 */
 	@ForgeSubscribe
 	public void onPlayerDeath(PlayerDropsEvent event){
 		if(!CoKGame.gameRunning){
 			event.setCanceled(true);
 			return;
 		}
 		String name = event.entityPlayer.username;
 		if(name == null){
 			return;
 		}
 		Team team = CoKGame.getTeamOfPlayer(name);
 		if(team == null){
 			event.setCanceled(true);
 			return;
 		}
 		//Remove given items from droplist
		ArrayList<EntityItem> droppedItems = new ArrayList<EntityItem>();
 		for(EntityItem itemEntity : event.drops){
			if(!(CoKGame.playerProfessions.get(name) != null
					&& CoKGame.playerProfessions.get(name).givenItems.contains(itemEntity.getEntityItem().getItem()))){
				droppedItems.add(itemEntity);
 			}
 		}
		event.drops.clear();
		event.drops.addAll(droppedItems);
		
 		//Set a new captain
 		if(team.getCaptain().equals(name)){
			//drop head :D
			ItemStack headStack = new ItemStack(Item.skull);
			headStack.setItemDamage(3); //Skulltype head
			headStack.setTagCompound(new NBTTagCompound("tag"));
			NBTTagCompound headTag = headStack.getTagCompound();
			headTag.setString("SkullOwner", name);
			event.drops.add(new EntityItem(event.entityPlayer.worldObj, event.entityPlayer.posX,
					event.entityPlayer.posY, event.entityPlayer.posZ, headStack));
			//change captain
 			team.setRandomCaptain();
 			ChatSendHelper.broadCastError("THE RULER OF THE KINGDOM " + team.getName() + " " + name + " HAS DIED!");
 			if(team.getCaptain().equals("")){
 				ChatSendHelper.broadCastError("THERE IS NO NEW RULER TO ANOUNCE... THIS IS BAD!");
 			} else{
 				ChatSendHelper.broadCastError("LONG LIFE KING " + team.getCaptain() + "!");
 			}
 		}
 	}
 	
 	/**
 	 * Players shouldn't throw items away that were given by the game.
 	 * @param event
 	 */
 	@ForgeSubscribe
 	public void onPlayerTossItem(ItemTossEvent event){
 		if(!event.player.capabilities.isCreativeMode){
 			ItemStack stack = event.entityItem.getEntityItem();
 			if(CoKGame.playerProfessions.get(event.player.username) != null
 					&& CoKGame.playerProfessions.get(event.player.username).givenItems.contains(stack.getItem())){
 				
 				event.setCanceled(true);
 				event.player.inventory.addItemStackToInventory(stack);
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onPlayerHit(LivingAttackEvent event){
 		if(event.entity instanceof EntityPlayer){
 			EntityPlayer attacker = (EntityPlayer) event.entity;
 			if(CoKGame.spectators.contains(attacker.username)){
 				event.setCanceled(true);
 			}
 		}
 	}
 	
 }
