/**
 * Copyright (c) 2013, Lourens "Lrns123" Elzinga
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
