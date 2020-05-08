 package se.mickelus.modjam.creation;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.File;
 import java.io.FileInputStream;
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
 		return Constants.SAVE_CMD + " <name> <type> <west> <east> <top> <bottom> <south> <north>";
 	}
 
 	@Override
 	public List getCommandAliases() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void processCommand(ICommandSender icommandsender, String[] astring) {
 		EntityPlayer player = (EntityPlayer) icommandsender;
 		if(astring.length != 8){
 			if(icommandsender instanceof EntityPlayer) {
 				player.addChatMessage(getCommandUsage(icommandsender));
 			}
 			return;
 		}
 		if(icommandsender instanceof EntityPlayer) {
 			File dir = new File(Constants.SAVE_PATH);
 			System.out.println(dir);
 			if(!dir.exists()){
 				dir.mkdirs();
 			}
 			
 			File file = MinecraftServer.getServer().getFile(Constants.SAVE_PATH + Constants.SAVE_NAME);
 			System.out.println(file);
 			
 			NBTTagCompound nbt = null;
 			if(!file.exists()){
 				try {
 					file.createNewFile();
 					nbt = new NBTTagCompound();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return;
 				}
 			}
 			else {
 				try {
 					FileInputStream filein = new FileInputStream(file);
 					nbt = CompressedStreamTools.readCompressed(filein);
 					filein.close();
 				}
 				catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			try {
 				
 			
 				int[] shape = new int[4096];
 				int x = player.chunkCoordX * 16;
 				int y = player.chunkCoordY * 16;
 				int z = player.chunkCoordZ * 16;
 				
				int type = Integer.parseInt(astring[1]);
 				int west = Integer.parseInt(astring[2]);
 				int east = Integer.parseInt(astring[3]);
 				int top = Integer.parseInt(astring[4]);
 				int bottom = Integer.parseInt(astring[5]);
 				int south = Integer.parseInt(astring[6]);
 				int north = Integer.parseInt(astring[7]);
 				
 				for(int sx = 0; sx < 16; sx++) {
 					for(int sy = 0; sy < 16; sy++) {
 						for(int sz = 0; sz < 16; sz++) {
 							shape[(sx*256+sy*16+sz)] = player.worldObj.getBlockId(x+sx, y+sy, z+sz);
 						}
 					}
 				}
 				
 				NBTTagCompound nbtSegment = new NBTTagCompound();
 				nbtSegment.setIntArray("blocks", shape);
 				nbtSegment.setInteger("west", west);
 				nbtSegment.setInteger("east", east);
 				nbtSegment.setInteger("top", top);
 				nbtSegment.setInteger("bottom", bottom);
 				nbtSegment.setInteger("south", south);
 				nbtSegment.setInteger("north", north);
 				nbtSegment.setInteger("type", type);
 				nbt.setCompoundTag(astring[0], nbtSegment);
 			
 				FileOutputStream fileos = new FileOutputStream(file);
 				CompressedStreamTools.writeCompressed(nbt, fileos);
 				fileos.close();
 			}
 			catch(Exception e){
 				e.printStackTrace();
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
