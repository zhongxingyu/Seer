 package emasher.sockets.modules;
 
 import java.util.List;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.common.ForgeDirection;
 import emasher.api.RSPulseModule;
 import emasher.api.SideConfig;
 import emasher.api.SocketTileAccess;
 import emasher.sockets.PacketHandler;
 import emasher.sockets.SocketsMod;
 
 public class ModBUD extends RSPulseModule
 {
 
 	public ModBUD(int id)
 	{
 		super(id, "sockets:BUD_0", "sockets:BUD_1", "sockets:DBUD_0", "sockets:DBUD_1");
 	}
 
 	@Override
 	public String getLocalizedName()
 	{
 		return "Block Update Sensor";
 	}
 	
 	@Override
 	public void getToolTip(List l)
 	{
 		l.add("Creats an internal redstone pulse");
 		l.add("upon an adjacent block update");
 	}
 	
 	@Override
 	public void getIndicatorKey(List l)
 	{
 		l.add(SocketsMod.PREF_RED + "RS Circuit to pulse");
 		l.add(SocketsMod.PREF_WHITE + "Toggle directional mode");
 	}
 	
 	@Override
 	public int getCurrentTexture(SideConfig config)
 	{
 		if(config.meta == 0)
 		{
 			if(config.tank == 0) return 0;
 			else return 2;
 		}
 		if(config.tank == 0) return 1;
 		else return 3;
 	}
 	
 	@Override
 	public void addRecipe()
 	{
 		GameRegistry.addShapedRecipe(new ItemStack(SocketsMod.module, 1, moduleID), "i", "b", Character.valueOf('i'), Block.pressurePlateGold, Character.valueOf('b'), SocketsMod.blankSide);
 	}
 	
 	@Override
 	public void init(SocketTileAccess ts, SideConfig config, ForgeDirection side)
 	{
 		config.tank = 0;
 		ts.sendClientSideState(side.ordinal());
 	}
 	
 	@Override
 	public void onGenericRemoteSignal(SocketTileAccess ts, SideConfig config, ForgeDirection side)
 	{
 		if(config.tank == 0) config.tank = 1;
 		else config.tank = 0;
 		ts.sendClientSideState(side.ordinal());
 	}
 	
 	@Override
 	public void onAdjChange(SocketTileAccess ts, SideConfig config, ForgeDirection side)
 	{
 		if(config.tank == 0)
 		{
 			config.meta = 1;
 			
 			for(int i = 0; i < 3; i++)
 			{
 				if(config.rsControl[i])
 				{
 					ts.modifyRS(i, true);
 				}
 			}
 			
 			ts.updateAdj(side);
 			ts.sendClientSideState(side.ordinal());
 		}
 	}
 	
 	@Override
 	public void onAdjChangeSide(SocketTileAccess ts, SideConfig config, ForgeDirection side)
 	{
 		if(config.tank == 1)
 		{
 			config.meta = 1;
 			
 			for(int i = 0; i < 3; i++)
 			{
 				if(config.rsControl[i])
 				{
 					ts.modifyRS(i, true);
 				}
 			}
 			
 			ts.updateAdj(side);
 			ts.sendClientSideState(side.ordinal());
 		}
 	}
 
     @Override
     public boolean isOutputingRedstone(SideConfig config, SocketTileAccess ts)
     {
         return false;
     }
 
 }
