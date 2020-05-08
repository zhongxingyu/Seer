 // Bukkit Plugin "NBTLib" by Siguza
 // Do whatever you want with this code.
 
 package net.drgnome.nbtlib;
 
 import java.io.*;
 import java.util.*;
 import javax.xml.bind.DatatypeConverter;
 import java.lang.reflect.InvocationTargetException;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * <p>A bunch of NBT tools.</p>
  * <p><i><b>Note:</b> For all the exceptions, see the "invoke" methods of {@link NBTLib}.</i></p>
  */
 public enum NBT
 {
     BOOL(-1),
     END(0),
     BYTE(1),
     SHORT(2),
     INT(3),
     LONG(4),
     FLOAT(5),
     DOUBLE(6),
     BYTE_ARRAY(7),
     STRING(8),
     LIST(9),
     COMPOUND(10),
     INT_ARRAY(11);
     
     private final int _id;
     
     private NBT(int id)
     {
         _id = id;
     }
     
     /**
      * <p>Returns the ID of the NBT Tag represented by this enum constant.</p>
      *
      * @return The ID.
      */
     public int getId()
     {
         return _id;
     }
     
     /**
      * <p>Converts a Bukkit {@link ItemStack} into a {@link Map}.</p>
      *
      * @param item The ItemStack.
      *
      * @return A Map.
      */
     public static Map<String, Tag> itemStackToMap(ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
     {
         return NBTToMap(saveItem(bukkitToMc(item)));
     }
     
     /**
      * <p>Converts a {@link Map} into a Bukkit {@link ItemStack}.</p>
      *
      * @param map The Map.
      *
      * @return A Bukkit ItemStack.
      */
     public static ItemStack mapToItemStack(Map<String, ?> map)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
     {
         return mcToBukkit(loadItem(mapToNBT("", map)));
     }
     
     /**
      * <p>Converts a {@link Map} into an NBTTagCompound.</p>
      *
      * @param name  The name of the tag.
      * @param map   The map.
      *
      * @return An NBTTagCompound.
      */
     public static Object mapToNBT(String name, Map<String, ?> map)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
     {
         return tagToNBT(name, Tag.newCompound(map));
     }
     
     private static Object tagToNBT(String name, Tag tag)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         switch(tag.getType())
         {
             case BOOL:
                 return NBTLib.instantiateMinecraft("NBTTagByte", new Object[]{String.class, byte.class}, new Object[]{name, (byte)(((Boolean)tag.get()).booleanValue() ? 1 : 0)});
             case BYTE:
                 return NBTLib.instantiateMinecraft("NBTTagByte", new Object[]{String.class, byte.class}, new Object[]{name, ((Byte)tag.get()).byteValue()});
             case SHORT:
                 return NBTLib.instantiateMinecraft("NBTTagShort", new Object[]{String.class, short.class}, new Object[]{name, ((Short)tag.get()).shortValue()});
             case INT:
                 return NBTLib.instantiateMinecraft("NBTTagInt", new Object[]{String.class, int.class}, new Object[]{name, ((Integer)tag.get()).intValue()});
             case LONG:
                 return NBTLib.instantiateMinecraft("NBTTagLong", new Object[]{String.class, long.class}, new Object[]{name, ((Long)tag.get()).longValue()});
             case FLOAT:
                 return NBTLib.instantiateMinecraft("NBTTagFloat", new Object[]{String.class, float.class}, new Object[]{name, ((Float)tag.get()).floatValue()});
             case DOUBLE:
                 return NBTLib.instantiateMinecraft("NBTTagDouble", new Object[]{String.class, double.class}, new Object[]{name, ((Double)tag.get()).doubleValue()});
             case BYTE_ARRAY:
                 return NBTLib.instantiateMinecraft("NBTTagByteArray", new Object[]{String.class, byte[].class}, new Object[]{name, (byte[])tag.get()});
             case STRING:
                 return NBTLib.instantiateMinecraft("NBTTagString", new Object[]{String.class, String.class}, new Object[]{name, (String)tag.get()});
             case LIST:
                 Object list = NBTLib.instantiateMinecraft("NBTTagList", new Object[]{String.class}, new Object[]{name});
                 for(Tag t : ((List<Tag>)tag.get()).toArray(new Tag[0]))
                 {
                     NBTLib.invokeMinecraft("NBTTagList", list, "add", new Object[]{NBTLib.getMinecraftPackage() + "NBTBase"}, new Object[]{tagToNBT("", t)});
                 }
                 return list;
             case COMPOUND:
                 Object map = NBTLib.instantiateMinecraft("NBTTagCompound", new Object[]{String.class}, new Object[]{name});
                 for(Map.Entry<String, Tag> entry : ((Map<String, Tag>)tag.get()).entrySet())
                 {
                     String key = entry.getKey();
                     NBTLib.invokeMinecraft("NBTTagCompound", map, "set", new Object[]{String.class, NBTLib.getMinecraftPackage() + "NBTBase"}, new Object[]{key, tagToNBT(key, entry.getValue())});
                 }
                 return map;
             case INT_ARRAY:
                 return NBTLib.instantiateMinecraft("NBTTagIntArray", new Object[]{String.class, int[].class}, new Object[]{name, (int[])tag.get()});
         }
         return null;
     }
     
     /**
      * <p>Converts an NBTTagCompound into a {@link Map}.</p>
      *
      * @param o The NBTTagCompound.
      *
      * @return A map.
      */
     public static Map<String, Tag> NBTToMap(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
     {
         if(getEnum(o) == COMPOUND)
         {
             return (Map<String, Tag>)(NBTToTag(o).get());
         }
         else
         {
             HashMap<String, Tag> map = new HashMap<String, Tag>();
             map.put("", NBTToTag(o));
             return map;
         }
     }
     
     static Tag NBTToTag(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
     {
         switch(getEnum(o))
         {
             case BYTE:
                 return Tag.newByte((Byte)NBTLib.fetchMinecraftField("NBTTagByte", o, "data"));
             case SHORT:
                 return Tag.newShort((Short)NBTLib.fetchMinecraftField("NBTTagShort", o, "data"));
             case INT:
                 return Tag.newInt((Integer)NBTLib.fetchMinecraftField("NBTTagInt", o, "data"));
             case LONG:
                 return Tag.newLong((Long)NBTLib.fetchMinecraftField("NBTTagLong", o, "data"));
             case FLOAT:
                 return Tag.newFloat((Float)NBTLib.fetchMinecraftField("NBTTagFloat", o, "data"));
             case DOUBLE:
                 return Tag.newDouble((Double)NBTLib.fetchMinecraftField("NBTTagDouble", o, "data"));
             case BYTE_ARRAY:
                 return Tag.newByteArray((byte[])NBTLib.fetchMinecraftField("NBTTagByteArray", o, "data"));
             case STRING:
                 return Tag.newString((String)NBTLib.fetchMinecraftField("NBTTagString", o, "data"));
             case LIST:
                 return Tag.newList((List)NBTLib.fetchMinecraftField("NBTTagList", o, "list"));
             case COMPOUND:
                 return Tag.newCompound((Map<String, ?>)NBTLib.fetchMinecraftField("NBTTagCompound", o, "map"));
             case INT_ARRAY:
                 return Tag.newIntArray((int[])NBTLib.fetchMinecraftField("NBTTagIntArray", o, "data"));
         }
         throw new UnknownTagException();
     }
     
     /**
      * <p>Loads an NBTTagCompound from a base64 encoded string.</p>
      *
      * @param string A base64 encoded string.
      *
      * @return An NBTTagCompound.
      */
     public static Object loadNBT64(String string)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return loadNBT(DatatypeConverter.parseBase64Binary(string));
     }
     
     /**
      * <p>Loads an NBTTagCompound from an array of bytes.</p>
      *
      * @param array An array of bytes.
      *
      * @return An NBTTagCompound.
      */
     public static Object loadNBT(byte[] array)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, NBTLib.getMinecraftPackage() + "NBTTagCompound", new Object[]{byte[].class}, new Object[]{array});
     }
     
     /**
      * <p>Loads an NBTTagCompound from an {@link InputStream}.</p>
      *
      * @param stream An {@link InputStream}.
      *
      * @return An NBTTagCompound.
      */
     public static Object loadNBT(InputStream stream)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, NBTLib.getMinecraftPackage() + "NBTTagCompound", new Object[]{InputStream.class}, new Object[]{stream});
     }
     
     /**
      * <p>Loads an NBTTagCompound from a {@link DataInput} object.</p>
      *
      * @param input A {@link DataInput} object.
      *
      * @return An NBTTagCompound.
      */
     public static Object loadNBT(DataInput input)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, NBTLib.getMinecraftPackage() + "NBTTagCompound", new Object[]{DataInput.class}, new Object[]{input});
     }
     
     /**
      * <p>Saves an NBTTagCompound to a base64 encoded string.</p>
      *
      * @param o An NBTTagCompound.
      *
      * @return A base64 encoded string.
      */
     public static String saveNBT64(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return DatatypeConverter.printBase64Binary(saveNBT(o));
     }
     
     /**
      * <p>Loads an NBTTagCompound from an array of bytes.</p>
      *
      * @param o An NBTTagCompound.
      *
      * @return An array of bytes.
      */
     public static byte[] saveNBT(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
        return (byte[])NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, byte[].class, new Object[]{NBTLib.getMinecraftPackage() + "NBTTagCompound"}, new Object[]{o});
     }
     
     /**
      * <p>Saves an NBTTagCompound to an {@link InputStream}.</p>
      *
      * @param stream An {@link InputStream}.
      * @param o An NBTTagCompound.
      */
     public static void saveNBT(OutputStream stream, Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
        NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, void.class, new Object[]{NBTLib.getMinecraftPackage() + "NBTTagCompound", OutputStream.class}, new Object[]{o, stream});
     }
     
     /**
      * <p>Saves an NBTTagCompound to a {@link DataInput} object.</p>
      *
      * @param output A {@link DataInput} object.
      * @param o An NBTTagCompound.
      */
     public static void saveNBT(DataOutput output, Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
        NBTLib.invokeMinecraftDynamic("NBTCompressedStreamTools", null, void.class, new Object[]{NBTLib.getMinecraftPackage() + "NBTTagCompound", DataOutput.class}, new Object[]{o, output});
     }
     
     /**
      * <p>Loads a Bukkit ItemStack from a base64 encoded string (using NBT).</p>
      *
      * @param string A base64 encoded string.
      *
      * @return A bukkit {@link ItemStack}.
      */
     public static ItemStack loadItemStack64(String string)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return mcToBukkit(loadItem(loadNBT64(string)));
     }
     
     /**
      * <p>Loads a Bukkit ItemStack from an array of bytes (using NBT).</p>
      *
      * @param array An array of bytes.
      *
      * @return A bukkit {@link ItemStack}.
      */
     public static ItemStack loadItemStack(byte[] array)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return mcToBukkit(loadItem(loadNBT(array)));
     }
     
     /**
      * <p>Loads a Bukkit ItemStack from an {@link InputStream} (using NBT).</p>
      *
      * @param stream An {@link InputStream}.
      *
      * @return A bukkit {@link ItemStack}.
      */
     public static ItemStack loadItemStack(InputStream stream)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return mcToBukkit(loadItem(loadNBT(stream)));
     }
     
     /**
      * <p>Loads a Bukkit ItemStack from a {@link DataInput} object (using NBT).</p>
      *
      * @param input A {@link DataInput} object.
      *
      * @return A bukkit {@link ItemStack}.
      */
     public static ItemStack loadItemStack(DataInput input)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return mcToBukkit(loadItem(loadNBT(input)));
     }
     
     // NBTTagCompound => ItemStack
     private static Object loadItem(Object nbt)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeMinecraftDynamic("ItemStack", null, NBTLib.getMinecraftPackage() + "ItemStack", new Object[]{NBTLib.getMinecraftPackage() + "NBTTagCompound"}, new Object[]{nbt});
     }
     
     /**
      * <p>Saves a Bukkit ItemStack to a base64 encoded string (using NBT).</p>
      *
      * @param item A bukkit {@link ItemStack}.
      *
      * @return A base64 encoded string.
      */
     public static String saveItemStack64(ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return saveNBT64(saveItem(bukkitToMc(item)));
     }
     
     /**
      * <p>Saves a Bukkit ItemStack to an array of bytes (using NBT).</p>
      *
      * @param item A bukkit {@link ItemStack}.
      *
      * @return An array of bytes.
      */
     public static byte[] saveItemStack(ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return saveNBT(saveItem(bukkitToMc(item)));
     }
     
     /**
      * <p>Saves a Bukkit ItemStack to an {@link OutputStream} (using NBT).</p>
      *
      * @param stream An {@link OutputStream}.
      * @param item A bukkit {@link ItemStack}.
      */
     public static void saveItemStack(OutputStream stream, ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         saveNBT(stream, saveItem(bukkitToMc(item)));
     }
     
     /**
      * <p>Saves a Bukkit ItemStack to a {@link DataOutput} object (using NBT).</p>
      *
      * @param output A {@link DataOutput} object.
      * @param item A bukkit {@link ItemStack}.
      */
     public static void saveItemStack(DataOutput output, ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         saveNBT(output, saveItem(bukkitToMc(item)));
     }
     
     // ItemStack => NBTTagCompound
     private static Object saveItem(Object item)
     throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeMinecraftDynamic("ItemStack", item, NBTLib.getMinecraftPackage() + "NBTTagCompound", new Object[]{NBTLib.getMinecraftPackage() + "NBTTagCompound"}, new Object[]{NBTLib.instantiateMinecraft("NBTTagCompound", new Object[0], new Object[0])});
     }
     
     /**
      * <p><i><b>Note:</b> You will most likely not need this.</i></p>
      * <p>Converts a Bukkit ItemStack into a Minecraft ItemStack.</p>
      *
      * @param item The Bukkit {@link ItemStack}.
      *
      * @return The Minecraft ItemStack.
      */
     public static Object bukkitToMc(ItemStack item)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return NBTLib.invokeCraftbukkit("inventory.CraftItemStack", null, "asNMSCopy", new Object[]{ItemStack.class}, new Object[]{item});
     }
     
     /**
      * <p><i><b>Note:</b> You will most likely not need this.</i></p>
      * <p>Converts a Minecraft ItemStack into a Bukkit ItemStack.</p>
      *
      * @param o The Minecraft ItemStack.
      *
      * @return The Bukkit {@link ItemStack}.
      */
     public static ItemStack mcToBukkit(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         return (ItemStack)NBTLib.invokeCraftbukkit("inventory.CraftItemStack", null, "asBukkitCopy", new Object[]{NBTLib.getMinecraftPackage() + "ItemStack"}, new Object[]{o});
     }
     
     /**
      * <p><i><b>Note:</b> You will most likely not need this.</i></p>
      * <p>Returns an {@link NBT} representing the type of a Minecraft NBTBase object.</p>
      *
      * @param o The Minecraft ItemStack.
      *
      * @return The NBT representation of the type of that NBTBase object.
      */
     public static NBT getEnum(Object o)
     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NBTLibDisabledException
     {
         int i = ((Byte)NBTLib.invokeMinecraft("NBTBase", o, "getTypeId", new Object[0], new Object[0])).intValue();
         for(NBT nbt : values())
         {
             if(nbt.getId() == i)
             {
                 return nbt;
             }
         }
         return null;
     }
 }
