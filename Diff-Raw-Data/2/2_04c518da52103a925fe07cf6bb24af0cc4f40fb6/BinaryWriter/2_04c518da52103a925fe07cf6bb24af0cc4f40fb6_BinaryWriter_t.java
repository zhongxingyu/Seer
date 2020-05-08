 /*
 Property List Binary Writer - LGPL 3.0 licensed
 Copyright (C) 2012  Yørn de Jong
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
 File is part of the Property List project.
 Project page on http://plist.sf.net/
 */
 package net.sf.plist.io.bin;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import net.sf.plist.*;
 import net.sf.plist.io.PropertyListException;
 import net.sf.plist.io.PropertyListWriter;
 
 /**
  * Serializes a tree consisting of {@link NSObject}s to a binary property list
  */
 public class BinaryWriter extends PropertyListWriter implements BinaryFields {
 
 	/** Numbered list containing all objects */
 	protected final List<NSObject> objectIndex = new ArrayList<NSObject>();
 	
 	/** Bytes expected at the start of the file */
 	protected final static byte[] STARTMAGIC = "bplist00".getBytes();
 	
 	/** Pattern used to determine whether a string contains only ASCII characters */
 	public final static Pattern ASCIIPATTERN = Pattern.compile("[\\p{ASCII}]*");
 	
 	/** Size of offset entries in bytes */
 	protected byte offsetEntrySize;
 	/** Size of object references in bytes */
 	protected byte objRefSize;
 	/** Number of objects in stream */
 	protected int numObjects;
 	/** The offset table */
 	protected int[] offsetTable;
 	
 	/** The amount of bytes written */
 	protected int pointer = 8;
 	/** The outputstream */
 	protected OutputStream stream;
 	
 	/**
 	 * Get amount of bytes required to represent a long
 	 * @param l the long
 	 * @return the amount of bytes required
 	 */
 	protected static byte getLongLength(long l) {
 		if (l == 0)
 			return 1;
 		if (l < 0)
 			return 8;
 		byte result = 0;
 		while(l != 0 && result >= 0) {
 			l >>= 8;
 			result++;
 		}
 		return result;
 	}
 	
 	/**
 	 * Split a long into bytes, little endian
 	 * @param l the long
 	 * @param size the amount of bytes to output
 	 * @return the bytes that make up the long
 	 */
 	protected static byte[] longToByteArray(long l, byte size) {
 		byte[] result = new byte[size];
 		for(byte i=size;i>0;i--) {
 			result[i-1] = (byte)l;
 			l >>= 8;
 		}
 		return result;
 	}
 	
 	/**
 	 * Calculate the 2log of a long, rounded up to the nearest integer or minimal result
 	 * @param l the long
 	 * @return the 2log
 	 */
 	protected static byte log2ceil(long l) {
 		for(byte i=0;i<63;i++)
 			if (l <= 1<<i)
 				return i;
 		return 64;
 	}
 	
 	/** @see PropertyListWriter#PropertyListWriter(NSObject) */
 	public BinaryWriter(NSObject root) { super(root); }
 	
 	/** {@inheritDoc} */
 	@Override
 	public synchronized void write(OutputStream stream) throws PropertyListException,
 			IOException {
 		this.stream = stream;
 		stream.write("bplist00".getBytes());
 		if (objectIndex.size() == 0)
 			buildObjectIndex(root);
 		numObjects = objectIndex.size();
 		objRefSize = getLongLength(numObjects);
 		offsetTable = new int[numObjects];
 		for(int i=0;i<numObjects;i++) {
 			offsetTable[i] = pointer;
 			pointer += writeObject(objectIndex.get(i));
 		}
 		offsetEntrySize = getLongLength(pointer);
 		
 		// write offset table
 		for(long offset : offsetTable)
 			stream.write(longToByteArray(offset, offsetEntrySize));
 		
 		// write metadata
 		stream.write(new byte[]{0,0,0,0,0,0,offsetEntrySize,objRefSize,0,0,0,0}); // padding and sizes
 		stream.write(longToByteArray(numObjects, (byte)4)); // number of objects
 		stream.write(new byte[]{0,0,0,0,0,0,0,0}); // rootobject, always at the beginning in this implementation
 		stream.write(longToByteArray(pointer, (byte)8)); // pointer to offsetTable
 		stream.close();
 	}
 	
 	/**
 	 * Write an object to the stream
 	 * @param obj the object to write
 	 * @return the amount of bytes written
 	 * @throws PropertyListException if the object could not be converted to binary data
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeObject(NSObject obj) throws PropertyListException, IOException {
 		if (obj instanceof NSArray)
 			return writeArray((NSArray) obj);
 		else if (obj instanceof NSBoolean)
 			return writeBoolean((NSBoolean) obj);
 		else if (obj instanceof NSData)
 			return writeData((NSData) obj);
 		else if (obj instanceof NSDate)
 			return writeDate((NSDate) obj);
 		else if (obj instanceof NSDictionary)
 			return writeDictionary((NSDictionary) obj);
 		else if (obj instanceof NSInteger)
 			return writeInteger((NSInteger) obj);
 		else if (obj instanceof NSReal)
 			return writeReal((NSReal) obj);
 		else if (obj instanceof NSString)
 			return writeString((NSString) obj);
 		else if (obj instanceof NSUID)
 			return writeUID((NSUID) obj);
 		else throw new PropertyListException("Unknown NSObjecttype; "+obj.getClass().getSimpleName());
 	}
 	
 	/**
 	 * Write the object header consisting of the type and length to the stream
 	 * @param length the length of the object (units differ between objects)
 	 * @param type the 4 bits determining the type of the object
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeObjectHeader(long length, byte type) throws IOException {
 		if (length < 0xF) {
 			stream.write(new byte[]{(byte) (length | (type << 4))});
 			return 1;
 		}
 		byte longLen = getLongLength(length);
 		stream.write(new byte[]{
 				(byte) (0xF | (type << 4)),
 				(byte) (log2ceil(longLen) | (INT << 4))
 			});
 		stream.write(longToByteArray(length, longLen));
 		return 2+longLen;
 	}
 	
 	/**
 	 * Write an array to the stream
 	 * @param obj the array
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeArray(NSArray obj) throws IOException {
 		long len = writeObjectHeader(obj.toList().size(), ARRAY);
 		byte[] buffer = new byte[obj.toList().size()*objRefSize];
 		int i=0;
 		for(NSObject o : obj.toList()) {
 			System.arraycopy(longToByteArray(objectIndex.indexOf(o), objRefSize), 0, buffer, i*objRefSize, objRefSize);
 			i++;
 		}
 		stream.write(buffer);
 		return len+obj.toList().size()*objRefSize;
 	}
 	
 	/**
 	 * Write a boolean to the stream
 	 * @param obj the boolean
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeBoolean(NSBoolean obj) throws IOException {
		stream.write(NSBoolean.TRUE.equals(obj) ? BOOLTRUE : BOOLFALSE);
 		return 1;
 	}
 	
 	/**
 	 * Write a data object to the stream
 	 * @param obj the data object
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeData(NSData obj) throws IOException {
 		long len = writeObjectHeader(obj.toBytes().length, DATA);
 		len += obj.toBytes().length;
 		stream.write(obj.toBytes());
 		return len;
 	}
 	
 	/**
 	 * Write date to the stream
 	 * @param obj the date
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeDate(NSDate obj) throws IOException {
 		long l = Double.doubleToLongBits(obj.toDouble());
 		stream.write(new byte[]{
 				(byte) (3 | (DATE << 4)) // 3 is 2log(8)
 			});
 		stream.write(longToByteArray(l, (byte) 8));
 		return 9; // 1 byte header, 8 bytes float
 	}
 	
 	/**
 	 * Write a dictionary to the stream
 	 * @param obj the dictionary
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeDictionary(NSDictionary obj) throws IOException {
 		long len = writeObjectHeader(obj.toMap().size(), DICT);
 		byte[] keyBuffer = new byte[obj.toMap().size()*objRefSize];
 		byte[] objBuffer = new byte[obj.toMap().size()*objRefSize];
 		int i=0;
 		for(Entry<String,NSObject> e : obj.entrySet()) {
 			int keyIndex = objectIndex.indexOf(new NSString(e.getKey()));
 			int valueIndex = objectIndex.indexOf(e.getValue());
 			System.arraycopy(longToByteArray(keyIndex, objRefSize), 0, keyBuffer, i*objRefSize, objRefSize);
 			System.arraycopy(longToByteArray(valueIndex, objRefSize), 0, objBuffer, i*objRefSize, objRefSize);
 			i++;
 		}
 		stream.write(keyBuffer);
 		stream.write(objBuffer);
 		return len+2*obj.toMap().size()*objRefSize;
 	}
 	
 	/**
 	 * Write an integer to the stream
 	 * @param obj the integer
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeInteger(NSInteger obj) throws IOException {
 		byte lengthByte = log2ceil(getLongLength(obj.toLong()));
 		byte longLen = (byte) (1<<lengthByte);
 		stream.write(new byte[]{
 				(byte) (lengthByte | (INT << 4))
 			});
 		stream.write(longToByteArray(obj.toLong(), longLen));
 		return longLen+1;
 	}
 	
 	/**
 	 * Write a real to the stream
 	 * @param obj the real
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeReal(NSReal obj) throws IOException {
 		long l = Double.doubleToLongBits(obj.toDouble());
 		byte lengthByte = log2ceil(getLongLength(l));
 		byte longLen = (byte) (1<<lengthByte);
 		stream.write(new byte[]{
 				(byte) (lengthByte | (REAL << 4))
 			});
 		stream.write(longToByteArray(l, longLen));
 		return longLen+1;
 	}
 	
 	/**
 	 * Write a string to the stream
 	 * @param obj the string
 	 * @return the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeString(NSString obj) throws IOException {
 		String str = obj.toString();
 		boolean isAscii = "".equals(ASCIIPATTERN.matcher(str).replaceFirst(""));
 		byte[] bytes = str.getBytes(isAscii ? ASCIICHARSET.toString() : UNICODECHARSET.toString());
 		long len = writeObjectHeader(str.length(), isAscii ? ASCIISTRING : UNICODESTRING);
 		stream.write(bytes);
 		return len+bytes.length;
 	}
 	
 	/**
 	 * Write a CF$UID to the stream
 	 * @param obj	the NSUID containing the CF$UID
 	 * @return	the amount of bytes written
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected long writeUID(NSUID obj) throws IOException {
 		writeObjectHeader(0, UID);
 		stream.write(obj.getCfUid());
 		return 2; // one byte header, one byte UID
 	}
 	
 	/**
 	 * Add all objects to the objectIndex, and search all objects of type {@link NSDictionary} or {@link NSArray} for more.
 	 * @param objs the objects
 	 */
 	protected void buildObjectIndex(NSObject... objs) {
 		for(NSObject obj : objs) {
 			if (!objectIndex.contains(obj)) {
 				objectIndex.add(obj);
 				if (obj instanceof NSDictionary) {
 					buildObjectIndex(((NSDictionary) obj).toMap().keySet().toArray(new String[0]));
 					buildObjectIndex(((NSDictionary) obj).toMap().values().toArray(new NSObject[0]));
 				} else if (obj instanceof NSArray) {
 					buildObjectIndex(((NSArray) obj).toList().toArray(new NSObject[0]));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Add all strings to the objectIndex as {@link NSString}s
 	 * @param strings the strings
 	 */
 	protected void buildObjectIndex(String... strings) {
 		for(String string : strings) {
 			NSString obj = new NSString(string);
 			if (!objectIndex.contains(obj))
 				objectIndex.add(obj);
 		}
 	}
 }
