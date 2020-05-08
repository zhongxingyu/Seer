 package se.mickelus.modjam.creation;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 
 import se.mickelus.modjam.Constants;
 
 import net.minecraft.command.ICommand;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.server.MinecraftServer;
 
 public class LoadCommand implements ICommand {
 
 	@Override
 	public int compareTo(Object arg0) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getCommandName() {
 		return Constants.LOAD_CMD;
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender icommandsender) {
 		return Constants.LOAD_CMD + " <name> <x> <y> <z>";
 	}
 
 	@Override
 	public List getCommandAliases() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void processCommand(ICommandSender icommandsender, String[] astring) {
		EntityPlayer player = (EntityPlayer) icommandsender;
		
		if(!(icommandsender instanceof EntityPlayer)) {
			return;
		}
		
 		if(astring.length != 4){
			player.addChatMessage(getCommandUsage(icommandsender));
 			return;
 		}
 		try {
 			File file = MinecraftServer.getServer().getFile(Constants.SAVE_PATH);
 			
 			if(!file.exists()){
 				return;
 			}
 			
 			FileInputStream filein = new FileInputStream(file);
 			NBTTagCompound nbt = CompressedStreamTools.readCompressed(filein);
 			if(nbt.hasKey(astring[0])){
 				NBTTagCompound nbtSegment = nbt.getCompoundTag(astring[0]);
 				if(nbtSegment.hasKey("blocks")){
 					try {
 						int x = Integer.parseInt(astring[1]);
 						int y = Integer.parseInt(astring[2]);
 						int z = Integer.parseInt(astring[3]);
 						
 						int[] shape = nbtSegment.getIntArray("blocks");
 						
 						for(int sx = 0; sx < 16; sx++) {
 							for(int sy = 0; sy < 16; sy++) {
 								for(int sz = 0; sz < 16; sz++) {
									player.worldObj.setBlock(x+sx, y+sy, z+sz, shape[(sx*256+sy*16+sz)]);
 								}
 							}
 						}
 					}
 					catch(NumberFormatException e) {
 						
 					}
 				}
 			}
 			
 			filein.close();
 		}
 		catch(Exception e) {
 			
 		}
 	}
 
 	@Override
 	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public List addTabCompletionOptions(ICommandSender icommandsender,
 			String[] astring) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isUsernameIndex(String[] astring, int i) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
