 package hardware;
 
 import items.Item;
 import items.ItemMagic;
 import items.ItemRanged;
 import items.ItemTool;
 
 import org.lwjgl.input.Mouse;
 
 import render.Render;
 import spells.Spell;
 import utils.ActionbarItem;
 import utils.MathHelper;
 import world.World;
 import blocks.Block;
 import blocks.BlockBackWall;
 import blocks.BlockChest;
 import entities.EntityPlayer;
 
 public class MouseInput 
 {	
 	private static boolean mouseLock = false;
 	
 	public static void mouse(World world, EntityPlayer player)
 	{
 		if(player.isStunned())
 		{
 			return;
 		}		
 		int active = player.selectedSlot;
 		int mouseBX = ((Render.getCameraX() + MathHelper.getCorrectMouseXPosition()) / 6);
 		int mouseBY = ((Render.getCameraY() + MathHelper.getCorrectMouseYPosition()) / 6);
 		
 		if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)){
 			player.setIsMining(false);
 		}
 		if(Mouse.isButtonDown(0) && player.inventory.getMainInventoryStack(active) != null) //Left Mouse Down && Actionbar slot isnt empty
 		{			
 			if(!player.isInventoryOpen)
 			{
 				int selectedItemID = player.inventory.getMainInventoryStack(active).getItemID();
 				if(selectedItemID < ActionbarItem.spellIndex)
 				{
 					Item item = Item.itemsList[selectedItemID];
 					if(!player.isSwingingTool() && item instanceof ItemTool) //If the player isn't swinging a tool, start swinging
 					{
 						ItemTool tool = (ItemTool) item;
 						player.startSwingingTool(player.isFacingRight);
 						if (player.getIsMining()){
 							world.soundEngine.playSoundEffect(tool.hitSound);
 						}				
 					}
 					//Attempt to launch a projectile				
 					if(selectedItemID >= ActionbarItem.itemIndex)
 					{
 						if (item instanceof ItemMagic)
 						{
 							ItemMagic spell = (ItemMagic) item;
 							player.launchProjectileMagic(world, 
 									(double)(Render.getCameraX() + MathHelper.getCorrectMouseXPosition()), 
 									(double)(Render.getCameraY() + MathHelper.getCorrectMouseYPosition()), 
 									spell);
 						}
 						else if (item instanceof ItemRanged)
 						{
 							ItemRanged weapon = (ItemRanged) item;
 							player.launchProjectileWeapon(world, 
 									(double)(Render.getCameraX() + MathHelper.getCorrectMouseXPosition()), 
 									(double)(Render.getCameraY() + MathHelper.getCorrectMouseYPosition()), 
 									weapon);
						}
						//Try to mine a block
						player.breakBlock(world, mouseBX, mouseBY, Item.itemsList[player.inventory.getMainInventoryStack(active).getItemID()]); 
 					}
 				}
 			}
 		}
 		else if (Mouse.isButtonDown(1)) //Right Mouse down
 		{			
 			if(world.getBlock(mouseBX, mouseBY) instanceof BlockChest && player.isInventoryOpen) //If the block is a chest
 			{
 				player.setViewedChest(mouseBX, mouseBY);
 			}
 			else
 			{
 				if((player.viewedChestX != mouseBX || player.viewedChestY != mouseBY) && 
 					world.getBlock(mouseBX, mouseBY) instanceof BlockChest && player.isInventoryOpen)
 				{
 				}
 				else
 				{
 					player.isViewingChest = false;
 				}
 			}
 			
 			if(player.inventory.getMainInventoryStack(active) != null)
 			{
 				int selectedItemID = player.inventory.getMainInventoryStack(active).getItemID();	
 				
 				//If player is holding a block
 				if (!player.isInventoryOpen && selectedItemID < ActionbarItem.itemIndex) 
 				{
 					mouseBX = (int) ((double)(Render.getCameraX() + MathHelper.getCorrectMouseXPosition()) / 6);
 					mouseBY = (int) ((double)(Render.getCameraY() + MathHelper.getCorrectMouseYPosition()) / 6);				
 					
 					double d = Math.sqrt(( 
 						(Math.pow(((MathHelper.getCorrectMouseXPosition() + Render.getCameraX()) - (player.x + ((player.isFacingRight) ? 9 : 3))), 2)) +
 						(Math.pow(((MathHelper.getCorrectMouseYPosition() + Render.getCameraY()) - (player.y + 9)), 2))  
 					)); //Find the distance from the appropriate hand
 					
 					if(d <= player.getMaximumBlockPlaceDistance()) //if the click was close enough to place a block, try to place one
 					{
 						if (Block.blocksList[selectedItemID].clone() instanceof BlockBackWall){
 							world.placeBackWall(player, mouseBX, mouseBY, Block.blocksList[player.inventory.getMainInventoryStack(active).getItemID()].clone());
 						}
 						else {
 							world.placeBlock(player, mouseBX, mouseBY, Block.blocksList[player.inventory.getMainInventoryStack(active).getItemID()].clone());
 						}
 					}
 				}
 				else if(player.inventory.getMainInventoryStack(active) != null && !player.isInventoryOpen && 
 						player.inventory.getMainInventoryStack(active).getItemID() >= ActionbarItem.itemIndex && 
 						player.inventory.getMainInventoryStack(active).getItemID() < ActionbarItem.spellIndex) //otherwise, if it's an item
 				{
 					if(!mouseLock)
 					{
 						Item.itemsList[player.inventory.getMainInventoryStack(active).getItemID()].onRightClick(world, player);
 						mouseLock = true;
 					}
 				}
 				else if(player.inventory.getMainInventoryStack(active) != null && !player.isInventoryOpen) //Spell
 				{
 					if(!mouseLock)
 					{
 						Spell.spellList[player.inventory.getMainInventoryStack(active).getItemID()].onRightClick(world, player);
 						mouseLock = true;
 					}
 				}
				if (selectedItemID < ActionbarItem.spellIndex){
 					Item item = Item.itemsList[selectedItemID];
 					if(!player.isSwingingTool() && item instanceof ItemTool) //If the player isn't swinging a tool, start swinging
 					{
 						ItemTool tool = (ItemTool) item;
 						player.startSwingingTool(player.isFacingRight);
 						if (player.getIsMining()){
 							world.soundEngine.playSoundEffect(tool.hitSound);
 						}				
					}
					//Mine the backwall
					player.breakBackBlock(world, mouseBX, mouseBY, Item.itemsList[player.inventory.getMainInventoryStack(active).getItemID()]);
 				}
 				
 			}
 		}		
 		else
 		{
 			mouseLock = false;
 		}
 		
 		//Check for mouse wheel movement if the inventory isnt open
 		if(!player.isInventoryOpen)
 		{
 			int wheelMovement = Mouse.getDWheel();
 			
 	        if (wheelMovement > 0) //If mouse scrolled up
 	        {
 	        	player.selectedSlot--; //Decrease selected slot (actionbar)
 	        	player.clearSwing();
 	        }
 	        else if(wheelMovement < 0) //If mouse scrolled down
 	        {
 	        	player.selectedSlot++; //Increase selected slot (actionbar)
 	        	player.clearSwing();
 	        }
 	        
 	        //Bounds checking
 	        if(player.selectedSlot < 0)
 	        {
 	        	player.selectedSlot = 11;
 	        }
 	        if(player.selectedSlot > 11)
 	        {
 	        	player.selectedSlot = 0;
 	        }
 		}
 	}
 }
