 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package us.exultant.ahs.codec.ebon;
 
 import us.exultant.ahs.codec.eon.*;
 import us.exultant.ahs.util.*;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This is a very fast binary protocol implementing {@link EonObject}. Fields are either
  * fixed-lenth (i.e. booleans, doubles, integers, and longs), or length prefixes the field
  * as a binary integer (i.e. strings and byte arrays); {@link EbonObject} and
  * {@link EbonArray} can be nested. Key lengths for every field are stored as a short (2
  * bytes), so keys may not exceed 65536 bytes in length. No type coersion is allowed by
  * any of the methods that return stored values; when serialized, type is stored as one
  * byte for each field.  Strings are stored in the UTF-8 charset.
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public class EbonObject implements EonObject {
 	public EbonObject() {
 		$map = new HashMap<String,Object>();
 	}
 	
 	private Map<String,Object> $map;
 	
 	
 	public String getKlass() {
 		return optString(Eon.MAGICWORD_CLASS,null);
 	}
 	
 	public void assertKlass(Object $x) throws EbonException {
 		assertKlass(Eon.getKlass($x));
 	}
 	public void assertKlass(Class<?> $x) throws EbonException {
 		assertKlass(Eon.getKlass($x));
 	}
 	public void assertKlass(String $x) throws EbonException {
 		String $klass = getKlass();
 		if ($klass == null) throw new EbonException("Class of EbonObject is not declared.");
 		if (!$x.equals($klass)) throw new EbonException("EbonObject class \""+$klass+"\" does not match desired class \""+$x+"\".");
 	}
 	
 	public void putKlass(Object $x) {
 		put(Eon.MAGICWORD_CLASS,Eon.getKlass($x));	
 	}
 	public void putKlass(Class<?> $x) {
 		put(Eon.MAGICWORD_CLASS,Eon.getKlass($x));
 	}
 	public void putKlass(String $x) {
 		put(Eon.MAGICWORD_CLASS,$x);
 	}
 	
 	protected Object get(String $key) throws EbonException {
 		Object $o = opt($key);
 		if ($o == null) throw new EbonException("EbonObject[" + $key + "] not found.");
 		return $o;
 	}
 	
 	public void putName(String $x) {
 		put(Eon.MAGICWORD_NAME, $x);
 	}
 	
 	public String getName() throws EbonException {
 		return getString(Eon.MAGICWORD_NAME);
 	}
 	
 	public void putData(EonObject $x) {
 		if ($x.getClass() != EbonObject.class) throw new IllegalArgumentException("EonObject isn't willing to deal with nested EonObject other than EbonObject.");
 		put(Eon.MAGICWORD_DATA, $x);
 	}
 	
 	public void putData(EonArray $x) {
 		if ($x.getClass() != EbonArray.class) throw new IllegalArgumentException("EonObject isn't willing to deal with nested EonArray other than EonArray.");
 		put(Eon.MAGICWORD_DATA, $x);
 	}
 	
 	public void putData(String $x) {
 		put(Eon.MAGICWORD_DATA, $x);
 	}
 	
 	public void putData(byte[] $x) {
 		put(Eon.MAGICWORD_DATA, $x);
 	}
 	
 	public EbonObject getData() throws EbonException {
 		return getObj(Eon.MAGICWORD_DATA);
 	}
 	
 	public EbonArray getArrayData() throws EbonException {
 		return getArr(Eon.MAGICWORD_DATA);
 	}
 	
 	public String getStringData() throws EbonException {
 		return getString(Eon.MAGICWORD_DATA);
 	}
 	
 	public byte[] getByteData() throws EbonException {
 		return getBytes(Eon.MAGICWORD_DATA);
 	}
 	
 	public void put(String $key, boolean $val) {
 		$map.put($key, $val);
 	}
 	
 	public void put(String $key, String $val) {
 		$map.put($key, $val);
 	}
 	
 	public void put(String $key, EonObject $val) {
 		if ($val.getClass() != EbonObject.class) throw new IllegalArgumentException("EonObject isn't willing to deal with nested EonObject other than EbonObject.");
 		$map.put($key, $val);
 	}
 	
 	public void put(String $key, EonArray $val) {
 		if ($val.getClass() != EbonArray.class) throw new IllegalArgumentException("EonObject isn't willing to deal with nested EonArray other than EonArray.");
 		$map.put($key, $val);
 	}
 	
 	protected Object opt(String $key) {
 		return $map.get($key);
 	}
 	
 	public boolean has(String $key) {
 		return (opt($key) != null);
 	}
 	
 	public int size() {
 		return $map.size();
 	}
 	
 	public void put(String $key, byte[] $val) {
 		$map.put($key, $val);
 	}
 	
 	public byte[] getBytes(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof byte[]) {
 			return (byte[]) $x;
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not a byte[].");
 		}
 	}
 	
 	public byte[] optBytes(String $key) {
 		return optBytes($key, null);
 	}
 	
 	public byte[] optBytes(String $key, byte[] $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof byte[])
 			return (byte[]) $x;
 		else
 			return $default;
 	}
 	
 	public boolean getBoolean(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof Boolean) {
 			return ($x.equals(Boolean.TRUE));
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not a Boolean.");
 		}
 	}
 	
 	public boolean optBoolean(String $key, boolean $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Boolean)
 			return ($x.equals(Boolean.TRUE));
 		else
 			return $default;
 	}
 	
 	public void put(String $key, double $val) {
 		$map.put($key, $val);
 	}
 	
 	public double getDouble(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof Double) {
 			return ((Double) $x).doubleValue();
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not a Double.");
 		}
 	}
 	
 	public double optDouble(String $key, double $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Double)
 			return ((Double) $x).doubleValue();
 		else
 			return $default;
 	}
 	
 	public void put(String $key, int $val) {
 		$map.put($key, Integer.valueOf($val));
 	}
 	
 	public int getInt(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof Integer) {
 			return ((Integer) $x).intValue();
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not an Integer.");
 		}
 	}
 	
 	public int optInt(String $key, int $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Integer)
 			return ((Integer) $x).intValue();
 		else
 			return $default;
 	}
 	
 	public void put(String $key, long $val) {
 		$map.put($key, Long.valueOf($val));
 	}
 	
 	public long getLong(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof Long) {
 			return ((Long) $x).longValue();
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not a Long.");
 		}
 	}
 	
 	public long optLong(String $key, long $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Long)
 			return ((Long) $x).longValue();
 		else
 			return $default;
 	}
 	
 	public String getString(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof String) {
 			return (String) $x;
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not a String.");
 		}
 	}
 	
 	public String optString(String $key) {
 		return optString($key, null);
 	}
 	
 	public String optString(String $key, String $default) {
 		Object $x = opt($key);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof String)
 			return (String) $x;
 		else
 			return $default;
 	}
 	
 	public EbonObject getObj(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof EbonObject) {
 			return (EbonObject) $x;
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not an EbonObject.");
 		}
 	}
 	
 	public EbonObject optObj(String $key) {
 		Object $x = opt($key);
 		if ($x == null)
 			return null;
 		else if ($x instanceof EbonObject)
 			return (EbonObject) $x;
 		else
 			return null;
 	}
 	
 	public EbonArray getArr(String $key) throws EbonException {
 		Object $x = get($key);
 		if ($x instanceof EbonArray) {
 			return (EbonArray) $x;
 		} else {
 			throw new EbonException("EbonObject[" + $key + "] is not an EbonArray.");
 		}
 	}
 	
 	public EbonArray optArr(String $key) {
 		Object $x = opt($key);
 		if ($x == null)
 			return null;
 		else if ($x instanceof EbonArray)
 			return (EbonArray) $x;
 		else
 			return null;
 	}
 	
 	
 	
 	public byte[] serialize() throws EbonException {
 		Bah $bah = new Bah(128);
 		DataOutputStream $dou = new DataOutputStream($bah);
 		serialize($dou);
		return $bah.getByteArray();
 	}
 	
 	/**
 	 * Package-visible so EbonArray and EbonObject can play tag.
 	 * @param $dou
 	 * @throws EbonException
 	 */
 	void serialize(DataOutputStream $dou) throws EbonException {
 		//SOMEDAY:AHS: wants me an ordered map that performs more like a linked list than that TreeMap thang -- i only ever want fifo traversal and a comparator for that is not my favorite idea.
 		
 		try {
 			$dou.writeByte((byte)'o');
 			$dou.writeInt($map.size());
 			for (Map.Entry<String,Object> $ent : $map.entrySet()) {
 				byte[] $k = $ent.getKey().getBytes(Strings.UTF_8);
 				$dou.writeShort($k.length);
 				$dou.write($k);
 				
 				Object $x = $ent.getValue();
 				if ($x instanceof byte[]) {
 					byte[] $y = (byte[]) $x;
 					$dou.writeByte((byte)'[');
 					$dou.writeInt($y.length);
 					$dou.write($y);
 				} else if ($x instanceof Boolean) {
 					$dou.writeByte((byte)'b');
 					$dou.writeBoolean($x.equals(Boolean.TRUE));
 				} else if ($x instanceof Double) {
 					$dou.writeByte((byte)'d');
 					$dou.writeDouble((Double) $x);
 				} else if ($x instanceof Integer) {
 					$dou.writeByte((byte)'i');
 					$dou.writeInt((Integer) $x);
 				} else if ($x instanceof Long) {
 					$dou.writeByte((byte)'l');
 					$dou.writeLong((Long) $x);
 				} else if ($x instanceof String) {
 					// it might seem a little strange here to just blast on past the methods DataOutputStream provides us for strings.
 					// however, we want the header to contain the length of the string _in_bytes_, so we can't use writeChars and have to do this hop-skip instead.
 					// (writeUTF is also kinda gross because it uses a modified UTF-8 instead of the real deal (and also has a 32k limit).)
 					byte[] $y = ((String) $x).getBytes(Strings.UTF_8);
 					$dou.writeByte((byte)'s');
 					$dou.writeInt($y.length);
 					$dou.write($y);
 				} else if ($x instanceof EbonObject) {
 					((EbonObject) $x).serialize($dou);
 				} else if ($x instanceof EbonArray) {
 					((EbonArray) $x).serialize($dou);
 				}
 			}
 		} catch (IOException $e) {
 			// ought not happen.  we can't really get io exceptions from writing to an internal buffer we just declared...
 			throw new MajorBug("Unimaginably strange error while writing to an internal buffer.", $e);
 		}
 	}
 	
 	public void deserialize(byte[] $bats) throws EbonException {
 		DataInputStream $din = new DataInputStream(new ByteArrayInputStream($bats));
 		byte $bat;
 		try {
 			$bat = $din.readByte();
 		} catch (IOException $e) {
 			throw new EbonException($e);
 		}
 		if ('o' != $bat) throw new EbonException("An EbonObject serial must begin with 'o'.");
 		deserialize($din);
 	}
 	
 	void deserialize(DataInputStream $din) throws EbonException {
 		try {
 			// i can see one wanting to skip over entire entries.  perhaps the format should accomodate this by including a binary length field (even though it is not necessary because a byte array of such size is never allocated).  it would also help with basic validity/sanity checking.
 			// but there would be costs in that: you'd have to write recursively into many buffers, then go back and merge them all in order to be able to get the length field correct and at the beginning of each block.  that results in a lot of relatively sizable copy operations that we just don't need.
 			// deferred-decoding implementations would also end up doing tons of unnecessary copies for every field they defer the decoding of, to the point that i suspect the overhead would be worse than just doing all the damn decoding at once in nearly every situation.
 			// anyway, any implementation that needs to defend itself from protocol-level attacks like a packet that claims its about to have a 2GB field needs to implement something where it throws exceptions as soon as the total number of reserved bytes is about to go past a limit, because attacks based on small but almost infinitely nested fields can be just as dangerous.
 			// ...incidentally, did you realize that you can make an infinitely long eon object by fragmenting binary chunks when they get over 2GB in size and the make sort of a linked list out of it?  true fact.
 			final int $mapl = $din.readInt();
 			int $len;	// temp bucket
 			byte[] $bats;	// temp bucket
 			byte $switch;	// temp bucket
 			String $key;	// self explanitory
 			Object $win;	// self explanitory
 			$bats = new byte[1024];
 			for (int $i = 0; $i < $mapl; $i++) {
 				$len = $din.readShort();
 				if ($len > $din.available()) throw new EOFException("Invalid format; Length header specified a key to be longer than remaining data.");
 				if ($bats.length < $len) $bats = new byte[$len];
 				$din.read($bats, 0, $len);
 				$key = new String($bats, 0, $len, Strings.UTF_8);
 				if (has($key)) throw new EbonException("Duplicate key \"" + $key + "\"");
 				
 				$switch = $din.readByte();
 				switch ($switch) {
 					case '[':
 						$len = $din.readInt();
 						if ($len > $din.available()) throw new EOFException("Invalid format; Length header specified a field to be longer than remaining data.");
 						byte[] $newbats = new byte[$len];	/* This is kind of awkward, but I'm betting that the inlining is easier this way than allocating $win as a byte[] directly and then having to cast for the read call. */
 						$din.read($newbats);
 						$win = $newbats;
 						break;
 					case 'b':
 						$win = $din.readBoolean();
 						break;
 					case 'd':
 						$win = $din.readDouble();
 						break;
 					case 'i':
 						$win = $din.readInt();
 						break;
 					case 'l':
 						$win = $din.readLong();
 						break;
 					case 's':
 						$len = $din.readInt();
 						if ($len > $din.available()) throw new EOFException("Invalid format; Length header specified a field to be longer than remaining data.");
 						if ($bats.length < $len) $bats = new byte[$len];
 						$din.read($bats, 0, $len);	/* it would be nice if there was a factory for strings that would let me read from DataInputStream directly without this intermediate byte array copy, but this is as close as we can get.  reusing the $bats array whenever possible does save us a lot of the garbage, anyway. */
 						$win = new String($bats, 0, $len, Strings.UTF_8);
 						break;
 					case 'o':
 						$win = new EbonObject();
 						((EbonObject)$win).deserialize($din);
 						break;
 					case 'a':
 						$win = new EbonArray();
 						((EbonArray)$win).deserialize($din);
 						break;
 					default:
 						throw new EbonException("EbonObject does not recognize field type '"+$switch+"'.");
 				}
 				$map.put($key, $win);
 			}
 		} catch (EOFException $e) {
 			throw new EbonException("Unexpected end of EbonObject.", $e);
 		} catch (IOException $e) {
 			if ($e instanceof EbonException) throw (EbonException)$e;	// i hate this line so
 			// we can't really get io exceptions from reading from an internal buffer we just declared...
 			throw new EbonException($e);
 		}
 	}
 	
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((this.$map == null) ? 0 : this.$map.hashCode());
 		return result;
 	}
 	
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (getClass() != obj.getClass()) return false;
 		EbonObject other = (EbonObject) obj;
 		if (this.$map == null) {
 			if (other.$map != null) return false;
 		} else if (!this.$map.equals(other.$map)) return false;
 		return true;
 	}
 
 	public String toString() {
 		return "EbonObject [$map=" + this.$map + "]";
 	}
 	
 	public String toArrStr() {
 		try {
 			return Arr.toString(this.serialize());
 		} catch (EbonException $e) {
 			return X.toString($e);
 		}
 	}
 	
 	public Set<Map.Entry<String,Object>> entrySet() {
 		return $map.entrySet();
 	}
 }
