 package com.lrns123.srgutility.lua.meta;
 
 import java.util.zip.ZipEntry;
 import org.luaj.vm2.LuaTable;
 import org.luaj.vm2.LuaValue;
 import org.luaj.vm2.Varargs;
 import org.luaj.vm2.lib.LibFunction;
 import org.luaj.vm2.lib.VarArgFunction;
 
 public class ZipEntryMeta extends LibFunction
 {
 	private static LuaTable metatable;
 
 	private static final int OP_GETCOMMENT = 0;
 	private static final int OP_GETCOMPRESSEDSIZE = 1;
 	private static final int OP_GETCRC = 2;
 	private static final int OP_GETEXTRA = 3;
 	private static final int OP_GETMETHOD = 4;
 	private static final int OP_GETNAME = 5;
 	private static final int OP_GETSIZE = 6;
 	private static final int OP_GETTIME = 7;
 	private static final int OP_ISDIRECTORY = 8;
 	
 	public static LuaTable getMetaTable()
 	{
 		if (metatable == null)
 			new ZipEntryMeta();
 		return metatable;
 	}
 
 	private ZipEntryMeta()
 	{
 		metatable = new LuaTable();
 
 		bind(metatable, ZipEntryMetaV.class, new String[] { "getComment", "getCompressedSize", "getCRC", "getExtra", "getMethod", "getName", "getSize", "getTime", "isDirectory" });
 		metatable.set(INDEX, metatable);
 	}
 
 	public static final class ZipEntryMetaV extends VarArgFunction
 	{
 		@Override
 		public Varargs invoke(Varargs args)
 		{
 			ZipEntry entry = (ZipEntry)args.arg1().checkuserdata(ZipEntry.class);
 			
 			switch (opcode)
 			{
 				case OP_GETCOMMENT:
 					return LuaValue.valueOf(entry.getComment());
 				case OP_GETCOMPRESSEDSIZE:
 					return LuaValue.valueOf(entry.getCompressedSize());
 				case OP_GETCRC:
 					return LuaValue.valueOf(entry.getCrc());
 				case OP_GETEXTRA:
 					return LuaValue.valueOf(entry.getExtra());
 				case OP_GETMETHOD:
 					return LuaValue.valueOf(entry.getMethod());
 				case OP_GETNAME:
 					return LuaValue.valueOf(entry.getName());
 				case OP_GETSIZE:
 					return LuaValue.valueOf(entry.getSize());
 				case OP_GETTIME:
 					return LuaValue.valueOf(entry.getTime());
 				case OP_ISDIRECTORY:
 					return LuaValue.valueOf(entry.isDirectory());
 			}
 			return LuaValue.NIL;
 		}
 	}
 }
