 /*
  * Copyright (c) 2013 Aritzh (Aritz Lopez)
  *
  * This game is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This game is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this
  * game. If not, see http://www.gnu.org/licenses/.
  */
 
 package aritzh.waywia.bds;
 
 import aritzh.waywia.core.GameLogger;
 import aritzh.waywia.util.Util;
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Files;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * Special BDS that can store different BDSs inside of it
  *
  * @author Aritz Lopez
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class BDSCompound extends BDS {
 
 	private List<BDS> items = new ArrayList<>();
 	private String name;
 
 	/**
 	 * Constructs an empty BDSCompound
 	 *
 	 * @param name The name of this BDS
 	 */
 	public BDSCompound(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Constructs a BDSCompound with the BDSs inside of the list
 	 *
 	 * @param items The list of BDSs to construct this with
 	 * @param name  The name of this BDS
 	 */
 	public BDSCompound(List<BDS> items, String name) {
 		this.items.addAll(items);
 		this.name = name;
 	}
 
 	/**
 	 * Constructs a BDSCompound with the BDSs inside of the array
 	 *
 	 * @param items The array of BDSs to construct this with
 	 */
 	public BDSCompound(BDS[] items, String name) {
 		this(Arrays.asList(items), name);
 	}
 
 	public BDSCompound(File file) throws IOException {
 		this(Files.toByteArray(file));
 	}
 
 	/**
 	 * Constructs a BDS with the single specified BDS inside
 	 *
 	 * @param item The BDS this BDSCompound will contain
 	 * @param name The name of this BDS
 	 */
 	public BDSCompound(BDS item, String name) {
 		this(new BDS[]{item}, name);
 	}
 
 	/**
 	 * Parses a BDSCompound from a byte array
 	 *
 	 * @param data The byte array to parse this BDSCompound from
 	 */
 	public BDSCompound(byte[] data) {
 		this(data, true);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return this == obj || (this.hashCode() == obj.hashCode() && this.getClass().equals(obj.getClass()));
 	}
 
 	/**
 	 * Returns a hashcode made from the byte array of this BDSCompound, which should be unique,
 	 * unless the objects are the same or equal.
 	 */
 	@Override
 	public int hashCode() {
 		return Arrays.toString(this.getBytes()).hashCode();
 	}
 
 	/**
 	 * Parses a BDSCompound from a byte array
 	 *
 	 * @param compressed If false, compression will not be used
 	 * @param data       The byte array to parse this BDSCompound from
 	 */
 	public BDSCompound(byte[] data, boolean compressed) {
 		data = (compressed ? BDSCompound.decompress(data) : data);
 		this.parse(ByteStreams.newDataInput(data), true);
 	}
 
 	public BDSCompound(ByteArrayDataInput input, boolean withType) {
 		this.parse(input, withType);
 	}
 
 	private void parse(ByteArrayDataInput input, boolean withType) {
 		if (input == null) {
 			this.name = "";
 		}
 		try {
 			if (withType) input.readByte();
 			this.name = input.readUTF();
 
 			for (byte typeB = input.readByte(); typeB < BDSType.values().length && typeB >= 0; typeB = input.readByte()) {
 				BDSType type = BDSType.values()[typeB];
 				switch (type) {
 					case BDS_COMPEND:
 						return;
 					case BDS_BYTE:
 						this.items.add(new BDSByte(input));
 						break;
 					case BDS_COMPOUND:
 						this.items.add(new BDSCompound(input, false));
 						break;
 					case BDS_INT:
 						this.items.add(new BDSInt(input));
 						break;
 					case BDS_SHORT:
 						this.items.add(new BDSShort(input));
 						break;
 					case BDS_STRING:
 						this.items.add(new BDSString(input));
 						break;
 					default:
 						throw new IllegalArgumentException("Could not parse BDSCompound");
 				}
 
 			}
 		} catch (NullPointerException e) {
 			throw new IllegalArgumentException("Could not parse BDSCompound", e);
 		}
 	}
 
 	/**
 	 * Add an element to this BDS
 	 *
 	 * @param bds The element to be added
 	 * @return {@code this}. Eases builder pattern
 	 */
 	public BDSCompound add(BDS bds) {
 		if (bds == this) throw new IllegalArgumentException("Cannot add itself as a sub-element!");
 		this.items.add(bds);
 		return this;
 	}
 
 	/**
 	 * Add an element to the list, in the specified position
 	 *
 	 * @param idx The position to add the element to
 	 * @param bds The element to be added
 	 * @return {@code this}. Eases builder pattern
 	 */
 	public BDSCompound add(int idx, BDS bds) {
 		this.items.add(idx, bds);
 		return this;
 	}
 
 	public BDSCompound addAll(Collection<? extends BDS> coll) {
 		this.items.addAll(coll);
 		return this;
 	}
 
 	public List<BDS> getAllByName(String name) {
 		List<BDS> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds.getName().equals(name)) ret.add(bds);
 		}
 		return ret;
 	}
 
 	public BDS getByName(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds.getName().equals(name)) {
 				if (offset <= 0) return bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the element in the specified position in the list
 	 *
 	 * @param idx The index of the element
 	 * @return The element in the {@code idx} position
 	 */
 	public BDS get(int idx) {
 		return this.items.get(idx);
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSString from the list
 	 *
 	 * @param offset The number of BDSStrings to skip
 	 * @return The {@code offset}'th BDSString from the list
 	 */
 	public BDSString getString(int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSString) {
 				if (offset == 0) return (BDSString) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	public List<BDSString> getAllStrings() {
 		List<BDSString> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSString) {
 				ret.add((BDSString) bds);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSByte from the list
 	 *
 	 * @param offset The number of BDSBytes to skip
 	 * @return The {@code offset}'th BDSByte from the list
 	 */
 	public BDSByte getByte(int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSByte) {
 				if (offset == 0) return (BDSByte) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	public List<BDSByte> getAllBytes() {
 		List<BDSByte> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSByte) {
 				ret.add((BDSByte) bds);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSShort from the list
 	 *
 	 * @param offset The number of BDSShorts to skip
 	 * @return The {@code offset}'th BDSShort from the list
 	 */
 	public BDSShort getShort(int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSShort) {
 				if (offset == 0) return (BDSShort) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	public List<BDSShort> getAllShorts() {
 		List<BDSShort> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSShort) {
 				ret.add((BDSShort) bds);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSInt from the list
 	 *
 	 * @param offset The number of BDSInts to skip
 	 * @return The {@code offset}'th BDSInt from the list
 	 */
 	public BDSInt getInt(int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSInt) {
 				if (offset == 0) return (BDSInt) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	public List<BDSInt> getAllInts() {
 		List<BDSInt> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSInt) {
 				ret.add((BDSInt) bds);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the {@code offset}th BDSCompound from the list
 	 *
 	 * @param offset The number of BDSCompounds to skip
 	 * @return The {@code offset}th BDSCompound from the list
 	 */
 	public BDSCompound getComp(int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSCompound) {
 				if (offset == 0) return (BDSCompound) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	public List<BDSCompound> getAllCompounds() {
 		List<BDSCompound> ret = new ArrayList<>();
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSCompound) {
 				ret.add((BDSCompound) bds);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSString from the list
 	 *
 	 * @param offset The number of BDSStrings to skip
 	 * @return The {@code offset}'th BDSString from the list
 	 */
 	public BDSString getString(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSString && bds.getName().equals(name)) {
 				if (offset == 0) return (BDSString) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSByte from the list
 	 *
 	 * @param offset The number of BDSBytes to skip
 	 * @return The {@code offset}'th BDSByte from the list
 	 */
 	public BDSByte getByte(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSByte && bds.getName().equals(name)) {
 				if (offset == 0) return (BDSByte) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSShort from the list
 	 *
 	 * @param offset The number of BDSShorts to skip
 	 * @return The {@code offset}'th BDSShort from the list
 	 */
 	public BDSShort getShort(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSShort && bds.getName().equals(name)) {
 				if (offset == 0) return (BDSShort) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@code offset}'th BDSInt from the list
 	 *
 	 * @param offset The number of BDSInts to skip
 	 * @return The {@code offset}'th BDSInt from the list
 	 */
 	public BDSInt getInt(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSInt && bds.getName().equals(name)) {
 				if (offset == 0) return (BDSInt) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the {@code offset}th BDSCompound from the list
 	 *
 	 * @param offset The number of BDSCompounds to skip
 	 * @return The {@code offset}th BDSCompound from the list
 	 */
 	public BDSCompound getComp(String name, int offset) {
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSCompound && bds.getName().equals(name)) {
 				if (offset == 0) return (BDSCompound) bds;
 				else offset--;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Remove the element from the list
 	 *
 	 * @param bds The element to be removed
 	 * @return {@code true} if this BDSCompound contained the element
 	 */
 	public boolean remove(BDS bds) {
 		return this.items.remove(bds);
 	}
 
 	/**
 	 * Stores the data from this BDSCompound into a byte array, so that
 	 * it can be easy and efficiently saved. This method doesn't
 	 * use gzip compression.
 	 *
 	 * @return The byte array identifying this BDS
 	 * @see #getBytes() Compressed counterpart
 	 */
 	private byte[] getUncompressedBytes() {
 
 		ByteArrayDataOutput output = ByteStreams.newDataOutput();
 		output.writeByte(this.getType().toByte());
 		output.writeUTF(this.name);
 		for (BDS bds : this.items) {
 			if (bds instanceof BDSCompound) {
 				output.write(((BDSCompound) bds).getUncompressedBytes());
 			} else output.write(bds.getBytes());
 		}
 		output.write(new BDSCompEnd().getBytes());
 		return output.toByteArray();
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public BDSType getType() {
 		return BDSType.BDS_COMPOUND;
 	}
 
 	@Override
 	public Object getData() {
 		return new ArrayList<>(this.items);
 	}
 
 	/**
 	 * Stores the data from this BDSCompound into a byte array, so that
 	 * it can be easy and efficiently saved. Gzip is used to compress the data
 	 *
 	 * @return The byte array identifying this BDS
 	 * @see #getUncompressedBytes() Uncompressed counterpart
 	 */
 	public byte[] getBytes() {
 		return compress(this.getUncompressedBytes());
 	}
 
 	public static byte[] decompress(byte[] data) {
 		try {
 			GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(data));
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 			int nRead;
 			byte[] buffer = new byte[16384];
 			while ((nRead = stream.read(buffer, 0, buffer.length)) != -1) {
 				byteStream.write(buffer, 0, nRead);
 			}
 			byteStream.flush();
 			return byteStream.toByteArray();
 		} catch (IOException e) {
 			throw new IllegalArgumentException("Could not decompress");
 		}
 	}
 
 	public static byte[] compress(byte[] data) {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			GZIPOutputStream gzos = new GZIPOutputStream(baos);
 			gzos.write(data);
 			gzos.close();
 			return baos.toByteArray();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void writeToFile(File f) {
 		try (FileOutputStream fos = new FileOutputStream(f)) {
 			fos.write(this.getBytes());
 			fos.flush();
 			fos.close();
 		} catch (IOException e) {
 			GameLogger.logAndThrowAsRuntime("Could not write BDSCompound to file!", e);
 		}
 	}
 
 	public BDSCompound copyOf(BDSCompound comp) {
 		return new BDSCompound(comp.getBytes());
 	}
 
 	@Override
 	public String toString(int level) {
 		StringBuilder builder = new StringBuilder();
 
 		builder.append(Util.repeatString("    ", level) + this.getType().toString() + ":" + this.getName());
 		boolean some = false;
 		for (BDS b : this.items) {
 			some = true;
 			if (b instanceof BDSCompEnd) continue;
 			builder.append("\n" + b.toString(level + 1));
 		}
 		if (!some) builder.append("\n" + Util.repeatString("    ", level + 1) + "EMPTY COMPOUND");
 
 		return builder.toString();
 	}
 
 	private class BDSCompEnd extends BDS {
 
 		public BDSCompEnd() {
 		}
 
 		@Override
 		public byte[] getBytes() {
 			return new byte[]{this.getType().toByte()};
 		}
 
 		@Override
 		public String getName() {
 			return "";
 		}
 
 		@Override
 		public BDSType getType() {
 			return BDSType.BDS_COMPEND;
 		}
 
 		@Override
 		public Object getData() {
 			return null;
 		}
 
 		@Override
 		public String toString(int level) {
 			return "";
 		}
 	}
 }
