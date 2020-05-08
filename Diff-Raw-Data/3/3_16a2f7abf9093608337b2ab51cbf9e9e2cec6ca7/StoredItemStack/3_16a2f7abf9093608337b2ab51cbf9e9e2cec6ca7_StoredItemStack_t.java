 package au.com.mineauz.PlayerSpy;
 
 import java.io.*;
 import java.util.HashMap;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 import au.com.mineauz.PlayerSpy.Records.RecordFormatException;
 
 public class StoredItemStack 
 {
 	public StoredItemStack(ItemStack stack)
 	{
 		if(stack != null)
 			mItem = stack.clone();
 		else
 			mItem = new ItemStack(0);
 	}
 	
 	public ItemStack getItem()
 	{
 		return mItem;
 	}
 	
 	public void writeItemStack(DataOutputStream stream) throws IOException
 	{
 		// item id
 		stream.writeInt(mItem.getTypeId());
 		// item data
 		stream.writeByte(mItem.getData().getData());
 		// item durability
 		stream.writeShort(mItem.getDurability());
 		// item count
 		stream.writeByte(mItem.getAmount());
 		// has enchants
 		stream.writeByte(mItem.getEnchantments().size());
 		
 		for(Enchantment ench : mItem.getEnchantments().keySet())
 		{
 			stream.writeShort(ench.getId());
 			stream.writeShort(mItem.getEnchantments().get(ench));
 		}
 
 	}
 	
 	public static StoredItemStack readItemStack(DataInputStream stream) throws IOException, RecordFormatException
 	{
 		//TODO: Redo this so that all the new stuff is supported
 		int itemId, amount;
 		short durability;
 		//byte data;
 		
 		itemId = stream.readInt();
 		/*data = */stream.readByte();
 		durability = stream.readShort();
 		amount = stream.readByte();
 		int enchantCount = stream.readByte();
 		
 		if(itemId < 0 || itemId > Short.MAX_VALUE)
 			throw new RecordFormatException("Bad item id " + itemId);
 		
 		if(enchantCount < 0)
 			throw new RecordFormatException("Bad enchantment count " + enchantCount);
 		
 		HashMap<Enchantment,Integer> map = new HashMap<Enchantment,Integer>();
 		
 		for(int i = 0; i < enchantCount; i++)
 		{
 			Enchantment ench = Enchantment.getById(stream.readShort());
 			if(ench == null)
 				throw new RecordFormatException("Bad enchantment type");
 			map.put(ench,(int)stream.readShort());
 		}
 		ItemStack item = new ItemStack(itemId,amount,durability/*,data*/);
 		item.addUnsafeEnchantments(map);
 		
 		return new StoredItemStack(item);
 	}
 	
 	public int getSize()
 	{
 		return 9 + 4 * mItem.getEnchantments().size();
 	}
 	private ItemStack mItem;
 }
