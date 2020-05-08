 package au.com.mineauz.PlayerSpy.storage;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.minecraft.server.v1_4_R1.NBTBase;
 import net.minecraft.server.v1_4_R1.NBTTagByte;
 import net.minecraft.server.v1_4_R1.NBTTagCompound;
 import net.minecraft.server.v1_4_R1.NBTTagDouble;
 import net.minecraft.server.v1_4_R1.NBTTagFloat;
 import net.minecraft.server.v1_4_R1.NBTTagInt;
 import net.minecraft.server.v1_4_R1.NBTTagList;
 import net.minecraft.server.v1_4_R1.NBTTagLong;
 import net.minecraft.server.v1_4_R1.NBTTagShort;
 import net.minecraft.server.v1_4_R1.NBTTagString;
 
 import org.bukkit.inventory.meta.*;
 
 import au.com.mineauz.PlayerSpy.Records.RecordFormatException;
 
 public class StoredItemMeta
 {
 	private ItemMeta mMeta;
 	
 	public StoredItemMeta(ItemMeta meta)
 	{
 		mMeta = meta;
 	}
 	public StoredItemMeta()
 	{
 		
 	}
 	
 	public ItemMeta getMeta()
 	{
 		return mMeta;
 	}
 	
 	@SuppressWarnings( "unchecked" )
 	private Object makeObjectFor(NBTBase tag)
 	{
 		Object data = null;
 		
 		if(tag instanceof NBTTagByte)
 			data = ((NBTTagByte)tag).data;
 		else if(tag instanceof NBTTagShort)
 			data = ((NBTTagShort)tag).data;
 		else if(tag instanceof NBTTagInt)
 			data = ((NBTTagInt)tag).data;
 		else if(tag instanceof NBTTagLong)
 			data = ((NBTTagLong)tag).data;
 		
 		else if(tag instanceof NBTTagString)
 			data = ((NBTTagString)tag).data;
 		
 		else if(tag instanceof NBTTagFloat)
 			data = ((NBTTagFloat)tag).data;
 		else if(tag instanceof NBTTagDouble)
 			data = ((NBTTagDouble)tag).data;
 		
 		else if(tag instanceof NBTTagList)
 		{
 			List<Object> list = new ArrayList<Object>();
 			for(int i = 0; i < ((NBTTagList)tag).size(); ++i)
 			{
 				NBTBase subTag = ((NBTTagList)tag).get(i);
 				
 				list.add(makeObjectFor(subTag));
 			}
 			
 			data = list;
 		}
 		
 		else if(tag instanceof NBTTagCompound)
 		{
 			Map<String, Object> map = new HashMap<String, Object>();
 			
 			Collection<NBTBase> tags = (Collection<NBTBase>)((NBTTagCompound)tag).c();
 			
 			for(NBTBase subTag : tags)
 			{
 				map.put(subTag.getName(), makeObjectFor(subTag));
 			}
 			
 			data = map;
 		}
 		
 		else
 			throw new RuntimeException("Unsupported tag type: " + tag.getClass().getName());
 		
 		return data;
 	}
 	@SuppressWarnings( "unchecked" )
 	private NBTBase makeTagFor(Object data)
 	{
 		NBTBase tag = null;
 		if(data instanceof String)
 		{
 			tag = new NBTTagString("", (String)data);
 		}
 		else if(data instanceof Integer)
 		{
 			tag = new NBTTagInt("", (Integer)data);
 		}
 		else if(data instanceof Double)
 		{
 			tag = new NBTTagDouble("", (Double)data);
 		}
 		else if(data instanceof Byte)
 		{
 			tag = new NBTTagByte("", (Byte)data);
 		}
 		else if(data instanceof Float)
 		{
 			tag = new NBTTagFloat("", (Float)data);
 		}
 		else if(data instanceof Long)
 		{
 			tag = new NBTTagLong("", (Long)data);
 		}
 		else if(data instanceof Short)
 		{
 			tag = new NBTTagShort("", (Short)data);
 		}
 		else if(data instanceof List)
 		{
 			tag = new NBTTagList("");
 			
 			Class<?> lastType = null;
 			for(Object obj : ((List<?>)data))
 			{
 				if(lastType != null && !lastType.isInstance(obj))
 					throw new RuntimeException("Can not have multiple types withing a tag list");
 				lastType = obj.getClass();
 				
 				NBTBase subTag = makeTagFor(obj);
 				
 				((NBTTagList)tag).add(subTag);
 			}
 		}
 		else if(data instanceof Map)
 		{
 			tag = new NBTTagCompound("");
 			
 			for(Entry<String, Object> entry : ((Map<String, Object>)data).entrySet())
 			{
 				NBTBase subTag = makeTagFor(entry.getValue());
 				if(subTag != null)
 					((NBTTagCompound)tag).set(entry.getKey(), subTag);
 			}
 		}
 		else
 			throw new RuntimeException("Invalid type " + data.getClass().getName() + " for making NBT tags");
 		
 		return tag;
 	}
 	
 	public void write(DataOutput output) throws IOException
 	{
 		if(mMeta == null) // It was an invalid item like air
 		{
 			output.writeByte(-1);
 			return;
 		}
 		
 		output.writeByte(1);
 		
 		// Make it into a nbt tag
 		Map<String,Object> data = mMeta.serialize();
 		NBTTagCompound root = (NBTTagCompound)makeTagFor(data);
 		root.setName("meta");
 		
 		if(root.getTypeId() != 10)
 			throw new RuntimeException("How?");
 		NBTBase.a(root, output);
 	}
 	
 	public void read(DataInput input) throws IOException, RecordFormatException
 	{
 		try
 		{
 			byte typeid = input.readByte(); 
 			
 			if(typeid == -1)
 				return; // No metadata
 			
 			NBTTagCompound root = (NBTTagCompound)NBTBase.b(input);
 			@SuppressWarnings( "unchecked" )
 			Map<String,Object> data = (Map<String,Object>)makeObjectFor(root);
 			
 			Class<?> itemMetaDeserializerClass = Class.forName("org.bukkit.craftbukkit.v1_4_R1.inventory.CraftMetaItem$SerializableMeta");
 			
 			Method deserialize = itemMetaDeserializerClass.getDeclaredMethod("deserialize", Map.class);
 			
 			mMeta = (ItemMeta)deserialize.invoke(null,  data);
 			
 		}
		catch(IOException e)
 		{
			throw new RecordFormatException("Error reading Item Meta. Malformed data.");
 		}
 		catch ( Exception e )
 		{
 			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	public int getSize()
 	{
 		// It is quite complicated and this is just easier
 		try
 		{
 			ByteArrayOutputStream stream = new ByteArrayOutputStream();
 			DataOutputStream output = new DataOutputStream(stream);
 			
 			write(output);
 			
 			return stream.size();
 		}
 		catch(IOException e)
 		{
 			return 0;
 		}
 	}
 	
 	
 	
 }
