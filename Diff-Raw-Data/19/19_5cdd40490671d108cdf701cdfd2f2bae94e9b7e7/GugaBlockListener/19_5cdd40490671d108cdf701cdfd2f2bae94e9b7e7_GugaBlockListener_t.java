 package me.Guga.Guga_SERVER_MOD;
 
 import java.util.ArrayList;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GugaBlockListener extends BlockListener
 {
 	GugaBlockListener(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	public void onBlockBreak(BlockBreakEvent e)
 	{
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: playerName="+e.getPlayer().getName()+",typeID="+e.getBlock().getTypeId());
 		}
		plugin.logger.LogBlockBreak(e, e.getBlock().getTypeId());
 		long timeStart = System.nanoTime();
 		Player p = e.getPlayer();
 		if (plugin.arena.IsArena(p.getLocation()))
 		{
 			if (p.isOp())
 			{
 				return;
 			}
 			p.sendMessage("You cannot destroy blocks in arena!");
 			e.setCancelled(true);
 			return;
 		}
 		if (!plugin.acc.UserIsLogged(p) && plugin.config.accountsModule)
 		{
 			e.setCancelled(true);
 			p.sendMessage("******************************");
 			if (plugin.acc.UserIsRegistered(p))
 			{
 				p.sendMessage("YOU ARE NOT LOGGED IN! Please login by typing /login password");
 			}
 			else
 			{
 				p.sendMessage("YOU ARE NOT REGISTERED! Please register by typing /register password");
 			}
 			p.sendMessage("******************************");
 			return;
 		}
 		GugaProfession prof = plugin.professions.get(p.getName());
 		boolean canBreak = false;
 		//*************************GRIEFING PROTECTION*************************
 		Block targetBlock;
 		targetBlock = e.getBlock();
 		if (prof == null)
 		{
 			int i = 0;
 			while (i < allowedBlocksTier1.length)
 			{
 				if (e.getBlock().getTypeId() == allowedBlocksTier1[i])
 				{
 					canBreak = true;
 					break;
 				}
 				i++;
 			}
 			if (canBreak)
 			{
 				Block blockAbove = targetBlock.getRelative(BlockFace.UP);
 				if (blockAbove.getTypeId() == 55)
 				{
 					canBreak = false;
 				}
 			}
 			if (p.isOp())
 			{
 				if(plugin.acc.UserIsLogged(p))
 				{
 					canBreak = true;
 				}
 			}
 			if (!canBreak)
 			{
 				p.sendMessage("You need to be atleast level 5 to destroy this block!");
 				e.setCancelled(true);
 				return;
 			}
 		}
 		else
 		{
 			int level = prof.GetLevel();
 			if (level < 5)
 			{
 				int i = 0;
 				while (i < allowedBlocksTier1.length)
 				{
 					if (e.getBlock().getTypeId() == allowedBlocksTier1[i])
 					{
 						canBreak = true;
 						break;
 					}
 					i++;
 				}
 				if (canBreak)
 				{
 					Block blockAbove = targetBlock.getRelative(BlockFace.UP);
 					if (blockAbove.getTypeId() == 55)
 					{
 						canBreak = false;
 					}
 				}
 				if (p.isOp())
 				{
 					if(plugin.acc.UserIsLogged(p))
 					{
 						canBreak = true;
 					}
 				}
 				if (!canBreak)
 				{
 					p.sendMessage("You need to be atleast level 5 to destroy this block!");
 					e.setCancelled(true);
 					return;
 				}
 			}
 		}
 		//*************************************************************************
 		String chestOwner;
 		int blockType = targetBlock.getTypeId();
 		if (blockType == 54)
 		{
 			chestOwner = plugin.chests.GetChestOwner(targetBlock);
 			if(chestOwner.matches("notFound"))
 			{
 				
 			}
 			else
 			{
 				e.setCancelled(true);
 				if (chestOwner.matches(p.getName()))
 				{
 					p.sendMessage("You cannot destroy locked chest! Unlock this chest first.");
 				}
 				else
 				{
 					p.sendMessage("You cannot destroy locked chest! " + chestOwner + " is owner of this chest.");
 				}
 			}
 		}
 		if (prof instanceof GugaMiner)
 		{
 			int typeId = targetBlock.getTypeId();
 			if (!IsInstaBreakBlock(typeId))
 			{
 				prof.GainExperience(3);
 			}
 			if (typeId == 1)
 			{
 				GugaBonusDrop bonus = ((GugaMiner)prof).CobbleStoneDrop();
 				if (bonus == GugaBonusDrop.DIAMOND)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(264,1));
 					prof.GainExperience(25);
 					p.sendMessage("You found a Diamond!");
 				}
 				else if (bonus == GugaBonusDrop.GOLD)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(14,1));
 					prof.GainExperience(25);
 					p.sendMessage("You found a Gold!");
 				}
 				else if (bonus == GugaBonusDrop.IRON)
 				{
 					p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(15,1));
 					prof.GainExperience(25);
 					p.sendMessage("You found an Iron!");
 				}
 			}
 			else if ((typeId == 15) || (typeId == 14) || (typeId == 56))
 			{
 				int drops;
 				if (dropsCache.contains(targetBlock))
 				{
 					dropsCache.remove(dropsCache.indexOf(targetBlock));
 					return;
 				}
 				boolean bonusExp = false;
 				drops = ((GugaMiner)prof).BonusDrop(targetBlock);
 				int i = 0;
 				while (i< drops)
 				{
 					if (typeId ==56)
 					{
 						p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(264,1));
 					}
 					else
 					{
 						p.getWorld().dropItem(targetBlock.getLocation(), new ItemStack(targetBlock.getTypeId(),1));
 					}
 					bonusExp = true;
 					if (i == 1)
 					{
 						i++;
 					}//**************ON BLOCK PLACE**************
 					i++;
 				}
 				if (bonusExp)
 				{
 					prof.GainExperience(25);
 				}
 			}
 		}
 		else if (prof instanceof GugaHunter)
 		{
 			int typeId = targetBlock.getTypeId();
 			if (!IsInstaBreakBlock(typeId))
 			{
 				prof.GainExperience(1);
 			}
 		}
 		if (plugin.debug == true)
 		{
 			plugin.log.info("BLOCK_BREAK_EVENT: Time=" + ((System.nanoTime() - timeStart)/1000));
 		}
 	}
 	public void onBlockPlace(BlockPlaceEvent e)
 	{
 		if (plugin.debug)
 		{
 			plugin.log.info("BLOCK_PLACE_EVENT: typeID="+e.getBlock().getTypeId()+",PlayerName="+e.getPlayer().getName());
 		}
 		plugin.logger.LogBlockPlace(e);
 		if (plugin.arena.IsArena(e.getBlock().getLocation()))
 		{
 			if (!e.getPlayer().isOp())
 			{
 				e.setCancelled(true);
 				return;
 			}
 		}
 		int typeId = e.getBlock().getTypeId();
 		if ((typeId == 14) || (typeId == 15) || (typeId == 56))
 		{
 			dropsCache.add(e.getBlock());
 		}
 	}
 	public boolean IsInstaBreakBlock(int blockId)
 	{
 		int i = 0;
 		while (i < instaBreakBlocks.length)
 		{
 			if (instaBreakBlocks[i] == blockId)
 			{
 				return true;
 			}
 			i++;
 		}
 		return false;
 	}
 	public void onBlockIgnite(BlockIgniteEvent e)
 	{
 		if(e.getCause() == IgniteCause.FLINT_AND_STEEL)
 		{
 			plugin.logger.LogBlockIgnite(e);
 		}
 	}
 	public void onBlockBurn(BlockBurnEvent e)
 	{
 		Block block = e.getBlock();
 		if (block.getTypeId() == 5)
 		{
 			Block relBlock;
 			BlockFace faceVals[] = BlockFace.values();
 			int i = 0;
 			while (i < faceVals.length)
 			{
 				relBlock = block.getRelative(faceVals[i]);
 				if (relBlock.getTypeId() == 51)
 				{
 					relBlock.setTypeId(0);
 				}
 				i++;
 			}
 			
 			e.setCancelled(true);
 			return;
 		}
 	}
 	public void onBlockRedstoneChange(BlockRedstoneEvent e)
 	{
 		Block block = e.getBlock();
 		int blockID = block.getTypeId();
 		if (plugin.debug)
 		{
 			plugin.log.info("REDSTONE_CHANGE_EVENT: typeID=" + blockID + ",blockData=" + block.getData() + ",x=" + block.getX() + ",y=" + block.getY() + ",z=" + block.getZ());
 		}
 	}
 	public int[] allowedBlocksTier1 = {1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 24, 31, 32, 37, 38, 39, 40, 56, 78, 79, 81, 82};
 	public int[] instaBreakBlocks = {6, 18, 30, 31, 32, 37, 38, 39, 40, 50, 55, 59, 75, 76, 83};
 	public ArrayList<Block> dropsCache = new ArrayList<Block>();
 	public static Guga_SERVER_MOD plugin;
 }
