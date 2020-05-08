 package net.minecraft.src;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.minecraft.server.MinecraftServer;
 
 public class mod_TreeBreaker extends BaseModMp {
 	@MLProp
 	public static boolean breakwood = true;
 
 	@MLProp
 	public static boolean breakleaves = true;
 
 	@MLProp(info = "Additional target block IDs. Separate by ','")
 	public static String additionalTargets = "";
 
 	public static Set<Integer> targetIDs = new LinkedHashSet();
 	public static Set<Integer> additionalTargetIDs = new LinkedHashSet();
 
 	@MLProp(info = "maximum number of block break (0 = unlimited)")
 	public static int breaklimit = 0;
 
 	public static final int cmd_break = 0;
 	public static final int cmd_mode = 1;
 	public static final int cmd_target = 2;
 	public static final int cmd_limit = 3;
 	public static final int cmd_itembreak = 4;
 
 	public MinecraftServer minecraftserver = null;
 
 	class BreakResister {
 		public EntityPlayerMP player;
 		int i;
 		int j;
 		int k;
 		int blockId;
 		int metadata;
 		int stacksize;
 		int itemdamage;
 		World worldObj;
 
 		public BreakResister(EntityPlayerMP entityplayermp, int i, int j, int k, int blockId, int metadata) {
 			this.player = entityplayermp;
 			this.i = i;
 			this.j = j;
 			this.k = k;
 			this.blockId = blockId;
 			this.metadata = metadata;
 			this.worldObj = entityplayermp.worldObj;
 		}
 	}
 
 	@Override
 	public String getVersion() {
		return "[1.2.4] TreeBreaker 0.0.2";
 	}
 
 	@Override
 	public void load() {
 		String str = additionalTargets;
 		String[] tokens = str.split(",");
 		try {
 			for(String token : tokens) {
 				additionalTargetIDs.add(Integer.parseInt(token.trim()));
 				targetIDs.add(Integer.parseInt(token.trim()));
 			}
 		} catch(NumberFormatException e) {
 
 		}
 
 		String strMode = "TreeBreaker target =";
 		if(breakwood) strMode += " Wood";
 		if(breakleaves) strMode += " Leaves";
 		strMode += " " + additionalTargetIDs;
 		System.out.println(strMode);
 
 		ModLoader.setInGameHook(this, true, true);
 	}
 
 	@Override
 	public void handlePacket(Packet230ModLoader packet230modloader,
 			EntityPlayerMP entityplayermp) {
 		if(minecraftserver == null) {
 			return;
 		}
 
 		if(packet230modloader.dataInt[0] == cmd_break) {
 
 			System.out.printf("[%d] recv %d, %d, %d, %d, %d, %d\n",
 					packet230modloader.modId, packet230modloader.dataInt[0],
 					packet230modloader.dataInt[1], packet230modloader.dataInt[2], packet230modloader.dataInt[3],
 					packet230modloader.dataInt[4], packet230modloader.dataInt[5]);
 
 			BreakResister breakResister =  new BreakResister(entityplayermp, packet230modloader.dataInt[1], packet230modloader.dataInt[2],
 					packet230modloader.dataInt[3], packet230modloader.dataInt[4], packet230modloader.dataInt[5]);
 			breakBlock(breakResister);
 		}
 		else if(packet230modloader.dataInt[0] == cmd_itembreak) {
 			breakItem(entityplayermp);
 		}
 	}
 
 	public void breakItem(EntityPlayerMP entityplayermp) {
         ItemStack itemstack = entityplayermp.getCurrentEquippedItem();
 
         if(itemstack != null) {
 	        itemstack.onItemDestroyedByUse(entityplayermp);
 	        entityplayermp.destroyCurrentEquippedItem();
         }
 	}
 
 	@Override
 	public void onTickInGame(MinecraftServer minecraftserver) {
 		if(this.minecraftserver == null) {
 			this.minecraftserver = minecraftserver;
 		}
 	}
 
 	public void breakBlock(BreakResister breakResister) {
 		System.out.println("breakBlock");
 
 		int blockId = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
 		if(Block.blocksList[blockId] == null) {
 			return;
 		}
 
 		Block b = Block.blocksList[blockId];
 		if(breakwood == false && b instanceof BlockLog) return;
 		if(breakleaves == false && b instanceof BlockLeaves) return;
 		if(b instanceof BlockLog || b instanceof BlockLeaves) {
 
 		}
 		else if(targetIDs.contains(blockId) == false) {
 			return;
 		}
 
 		Material material = breakResister.worldObj.getBlockMaterial(breakResister.i, breakResister.j, breakResister.k);
 		if(material.isSolid() == false) {
 			return;
 		}
 
 		System.out.printf("breakBlock %d, %d, %d\n", breakResister.i, breakResister.j, breakResister.k);
 
         if (breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k) != 0)
         {
     		//breakResister.player.itemInWorldManager.blockHarvessted(breakResister.i, breakResister.j, breakResister.k);
 
 
         	// copy from blockHarvessted
             int i = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
             int j = breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k);
             breakResister.worldObj.playAuxSFXAtEntity(breakResister.player, 2001, breakResister.i, breakResister.j, breakResister.k, i + (breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k) << 12));
             boolean flag = breakResister.player.itemInWorldManager.removeBlock(breakResister.i, breakResister.j, breakResister.k);
 
             if (breakResister.player.itemInWorldManager.isCreative())
             {
                 ((EntityPlayerMP)breakResister.player).playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
             }
             else
             {
                 ItemStack itemstack = breakResister.player.getCurrentEquippedItem();
                 boolean flag1 = breakResister.player.canHarvestBlock(Block.blocksList[i]);
 
                 if (itemstack != null)
                 {
                     itemstack.onDestroyBlock(i, breakResister.i, breakResister.j, breakResister.k, breakResister.player);
 
                     if (itemstack.stackSize == 0)
                     {
                         itemstack.onItemDestroyedByUse(breakResister.player);
                         breakResister.player.destroyCurrentEquippedItem();
                     }
                 }
 
                 if (flag && flag1)
                 {
                     Block.blocksList[i].harvestBlock(breakResister.worldObj, breakResister.player, (int)breakResister.player.posX, (int)breakResister.player.posY, (int)breakResister.player.posZ, j);
                 }
             }
 
 
 
         	breakResister.player.playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
         }
 	}
 
 	@Override
 	public void handleLogin(EntityPlayerMP entityplayermp) {
 		sendBreakMode(entityplayermp);
 		sendTargetIds(entityplayermp);
 		sendBreakLimit(entityplayermp);
 	}
 
 	public void sendBreakLimit(EntityPlayerMP entityplayermp) {
 		Packet230ModLoader packet = new Packet230ModLoader();
 		packet.modId = getId();
 		packet.dataInt = new int[2];
 		packet.dataInt[0] = cmd_limit;
 		packet.dataInt[1] = breaklimit;
 		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
 	}
 
 	public void sendBreakMode(EntityPlayerMP entityplayermp) {
 		Packet230ModLoader packet = new Packet230ModLoader();
 		packet.modId = getId();
 		packet.dataInt = new int[2];
 		packet.dataInt[0] = cmd_mode;
 
 		int nMode = 0;
 		if(breakwood) nMode |= 1;
 		if(breakleaves) nMode |= 2;
 		packet.dataInt[1] = nMode;
 		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
 	}
 
 	public void sendTargetIds(EntityPlayerMP entityplayermp) {
 		Packet230ModLoader packet = new Packet230ModLoader();
 		packet.modId = getId();
 		packet.dataInt = new int[1];
 		packet.dataInt[0] = cmd_target;
 		packet.dataString = new String[1];
 		packet.dataString[0] = additionalTargets;
 		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
 	}
 
 }
