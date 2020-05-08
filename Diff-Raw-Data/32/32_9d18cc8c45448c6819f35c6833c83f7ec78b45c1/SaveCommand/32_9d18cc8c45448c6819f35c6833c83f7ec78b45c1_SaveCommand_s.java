 package se.mickelus.modjam.creation;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.text.html.parser.Entity;
 
 import se.mickelus.modjam.Constants;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.command.ICommand;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTBase;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.server.MinecraftServer;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.MinecraftForge;
 
 public class SaveCommand implements ICommand{
 
 	public SaveCommand() {
 	}
 	
 	@Override
 	public int compareTo(Object arg0) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getCommandName() {
 		return Constants.SAVE_CMD;
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender icommandsender) {
 		return Constants.SAVE_CMD + " <name>";
 		//return Constants.SAVE_CMD + " <name> <type> <x+> <x-> <y+> <y-> <z+> <z->";
 	}
 
 	@Override
 	public List getCommandAliases() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void processCommand(ICommandSender icommandsender, String[] astring) {
 		if(astring.length != 1){
 			if(icommandsender instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) icommandsender;
 				player.addChatMessage(getCommandUsage(icommandsender));
 			}
 			return;
 		}
 		if(icommandsender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) icommandsender;
 			File file = MinecraftServer.getServer().getFile(Constants.SAVE_PATH);
 			
 			if(!file.exists()){
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return;
 				}
 			}
 			
 			try {
 				FileOutputStream fileos = new FileOutputStream(file);
 			
 				int[] shape = new int[4096];
 				int x = player.chunkCoordX * 16;
 				int y = player.chunkCoordY * 16;
 				int z = player.chunkCoordZ * 16;
 				
 				System.out.println("CX: " + player.chunkCoordX);
 				System.out.println("X: " + x);
 				System.out.println("CY: " + player.chunkCoordY);
 				System.out.println("Y: " + y);
 				System.out.println("CZ: " + player.chunkCoordZ);
 				System.out.println("Z: " + z);
 				
 				for(int sx = 0; sx < 16; sx++) {
 					for(int sy = 0; sy < 16; sy++) {
 						for(int sz = 0; sz < 16; sz++) {
							shape[(sx*256+sy*16+sz)] = MinecraftServer.getServer().getConfigurationManager().
							getPlayerForUsername(icommandsender.getCommandSenderName()).
							worldObj.getBlockId(x+sx, y+sy, z+sz);
 						}
 					}
 				}
 				
 				NBTTagCompound nbt = new NBTTagCompound();
 				NBTTagCompound nbtSegment = new NBTTagCompound();
 				nbtSegment.setIntArray("blocks", shape);
 				nbt.setCompoundTag(astring[0], nbtSegment);
 			
 				CompressedStreamTools.writeCompressed(nbt, fileos);
 				fileos.close();
 			}
 			catch(Exception e){
 				System.out.println("IOEXCEPTION");
 			}
 		}
 	}
 
 	@Override
 	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
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
