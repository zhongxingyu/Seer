 package taigore.inventorysaver;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.minecraft.crash.CrashReport;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.util.ReportedException;
 import taigore.inventorysaver.client.InvSaverClientPacketHandler;
 
 public class Packet250BagSync extends Packet250CustomPayload
 {
 	public int entityID;
 	public List<ItemStack> inventory;
 	
	public Packet250BagSync(EntityBag toSync)
	{
		super(InvSaverClientPacketHandler.chanBagInventorySync, encodeBagToByteArray(toSync));
	}
 	public Packet250BagSync() {}
 
 	private static byte[] encodeBagToByteArray(EntityBag toEncode)
 	{
 		if(toEncode != null)
 		{
 			byte[] outputData = null;
 			ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
 			DataOutputStream dataConverter = new DataOutputStream(byteWriter);
 			
 			try
 			{
 				try
 				{
 					//Specifications:
 					//First int - entityID
 					//Second int - amount of stacks
 					//Subsequent data - item stacks in NBT format
 					dataConverter.writeInt(toEncode.entityId);
 					dataConverter.writeInt(toEncode.bagContents.size());
 					
 					for(ItemStack toSend : toEncode.bagContents)
 					{
 						Packet.writeNBTTagCompound(toSend.writeToNBT(new NBTTagCompound()), dataConverter);
 					}
 					
 					dataConverter.flush();
 					outputData = byteWriter.toByteArray();
 				}
 				finally
 				{
 					if(dataConverter != null) dataConverter.close();
 					if(byteWriter != null) byteWriter.close();
 				}
 			}
 			catch(Exception exc)
 			{
 				CrashReport rep = CrashReport.makeCrashReport(exc, "Taigore InventorySaver: failed an I/O operation while creating a custom packet");
 				throw new ReportedException(rep);
 			}
 			
 			return outputData;
 		}
 		else throw new IllegalArgumentException(String.format("Taigore InventorySaver: null bag entity supplied to packet constructor"));
 	}
 	
 	public static Packet250BagSync decodePacket(Packet250CustomPayload toDecode)
 	{
 		if(toDecode.channel.equals(InvSaverClientPacketHandler.chanBagInventorySync))
 		{
 			Packet250BagSync outputPacket = new Packet250BagSync();
 			ByteArrayInputStream byteReader = new ByteArrayInputStream(toDecode.data);
 			DataInputStream dataConverter = new DataInputStream(byteReader);
 			
 			try
 			{
 				try
 				{
 					//Specifications:
 					//First int - entityID
 					//Second int - amount of stacks
 					//Subsequent data - item stacks in NBT format
 					outputPacket.entityID = dataConverter.readInt();
 					int stacksAmount = dataConverter.readInt();
 					outputPacket.inventory = new LinkedList();
 					
 					for(int i = 0; i < stacksAmount; i++)
 					{
 						NBTTagCompound itemNBT = Packet.readNBTTagCompound(dataConverter);
 						
 						outputPacket.inventory.add(ItemStack.loadItemStackFromNBT(itemNBT));
 					}
 				}
 				finally
 				{
 					if(dataConverter != null) dataConverter.close();
 					if(byteReader != null) byteReader.close();
 				}
 			}
 			catch(Exception exc)
 			{
 				CrashReport rep = CrashReport.makeCrashReport(exc, "Taigore InventorySaver: failed an I/O operation while reading a custom packet");
 				throw new ReportedException(rep);
 			}
 			
 			return outputPacket;
 		}
 		else throw new IllegalArgumentException(String.format("Taigore InventorySaver: unknown packet of channel \"%s\" given for processing", toDecode.channel));
 	}
 }
