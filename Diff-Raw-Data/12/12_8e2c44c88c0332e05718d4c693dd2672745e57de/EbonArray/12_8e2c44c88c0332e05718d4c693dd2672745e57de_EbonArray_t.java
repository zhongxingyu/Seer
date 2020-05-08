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
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.codec.eon.*;
 
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
 public class EbonArray implements EonArray {
 	public EbonArray() {
 		$arr = new ArrayList<Object>();
 	}
 	
 	public EbonArray(int $capacity) {
 		$arr = new ArrayList<Object>($capacity);
 	}
 	
 	private List<Object> $arr;
 	
 	public int size() {
 		return $arr.size();
 	}
 	
 	protected Object opt(int $index) {
 		return $arr.get($index);
 	}
 	
 	protected Object get(int $index) throws EbonException {
 		Object $o = opt($index);
 		if ($o == null) throw new EbonException("EbonArray[" + $index + "] not found.");
 		return $o;
 	}
 	
 	public void put(int $index, byte[] $val) {
 		$arr.add($index, $val);
 	}
 	
 	public byte[] getBytes(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof byte[]) {
 			return (byte[]) $x;
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not a byte[].");
 		}
 	}
 	
 	public byte[] optBytes(int $index) {
 		return optBytes($index, null);
 	}
 	
 	public byte[] optBytes(int $index, byte[] $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof byte[])
 			return (byte[]) $x;
 		else
 			return $default;
 	}
 	
 	public void put(int $index, boolean $val) {
 		$arr.add($index, $val);
 	}
 	
 	public boolean getBoolean(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof Boolean) {
 			return ($x.equals(Boolean.TRUE));
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not a Boolean.");
 		}
 	}
 	
 	public boolean optBoolean(int $index, boolean $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Boolean)
 			return ($x.equals(Boolean.TRUE));
 		else
 			return $default;
 	}
 
 	public void put(int $index, double $val) {
 		$arr.add($index, $val);
 	}
 	
 	public double getDouble(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof Double) {
 			return ((Double) $x).doubleValue();
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not a Double.");
 		}
 	}
 
 	public double optDouble(int $index, double $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Double)
 			return ((Double) $x).doubleValue();
 		else
 			return $default;
 	}
 
 	public void put(int $index, int $val) {
 		$arr.add($index, $val);
 	}
 
 	public int getInt(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof Integer) {
 			return ((Integer) $x).intValue();
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not an Integer.");
 		}
 	}
 
 	public int optInt(int $index, int $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Integer)
 			return ((Integer) $x).intValue();
 		else
 			return $default;
 	}
 
 	public void put(int $index, long $val)  {
 		$arr.add($index, $val);
 	}
 
 	public long getLong(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof Long) {
 			return ((Long) $x).longValue();
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not a Long.");
 		}
 	}
 
 	public long optLong(int $index, long $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof Long)
 			return ((Long) $x).longValue();
 		else
 			return $default;
 	}
 
 	public void put(int $index, String $val) {
 		$arr.add($index, $val);
 	}
 
 	public String getString(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof String) {
 			return (String) $x;
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not a String.");
 		}
 	}
 
 	public String optString(int $index) {
 		return optString($index, null);
 	}
 
 	public String optString(int $index, String $default) {
 		Object $x = opt($index);
 		if ($x == null)
 			return $default;
 		else if ($x instanceof String)
 			return (String) $x;
 		else
 			return $default;
 	}
 
 	public void put(int $index, EonObject $val) {
 		$arr.add($index, $val);
 	}
 
 	public EbonObject getObj(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof EbonObject) {
 			return (EbonObject) $x;
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not an EbonObject.");
 		}
 	}
 
 	public EbonObject optObj(int $index) {
 		Object $x = opt($index);
 		if ($x == null)
 			return null;
 		else if ($x instanceof EbonObject)
 			return (EbonObject) $x;
 		else
 			return null;
 	}
 
 	public void put(int $index, EonArray $val) {
 		$arr.add($index, $val);
 	}
 
 	public EbonArray getArr(int $index) throws EbonException {
 		Object $x = get($index);
 		if ($x instanceof EbonArray) {
 			return (EbonArray) $x;
 		} else {
 			throw new EbonException("EbonArray[" + $index + "] is not an EbonArray.");
 		}
 	}
 
 	public EbonArray optArr(int $index) {
 		Object $x = opt($index);
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
		return ($bah.size() == $bah.getByteArray().length) ? $bah.getByteArray() : $bah.toByteArray();
 	}
 	
 	/**
 	 * Package-visible so EbonArray and EbonObject can play tag.
 	 * @param $dou
 	 * @throws EbonException
 	 */
 	void serialize(DataOutputStream $dou) throws EbonException {
 		//SOMEDAY:AHS: wants me an ordered map that performs more like a linked list than that TreeMap thang -- i only ever want fifo traversal and a comparator for that is not my favorite idea.
 		
 		try {
 			$dou.writeByte((byte)'a');
 			final int $arrl = $arr.size();
 			$dou.writeInt($arrl);
 			for (int $i = 0; $i < $arrl; $i++) {
 				Object $x = $arr.get($i);
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
 			throw new EbonException($e);
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
 		if ('a' != $bat) throw new EbonException("An EbonArray serial must begin with 'a'.");
 		deserialize($din);
 	}
 	
 	void deserialize(DataInputStream $din) throws EbonException {
 		try {
 			final int $arrl = $din.readInt();
 			int $len;		// temp bucket
 			byte[] $bats = null;	// temp bucket
 			byte $switch;		// temp bucket
 			Object $win;		// self explanitory
 			if ($arrl < 0) throw new EbonException("Invalid format; arrays cannot have negative length.");
 			for (int $i = 0; $i < $arrl; $i++) {
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
 						if ($bats == null || $bats.length < $len) $bats = new byte[$len];
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
 						throw new EbonException("EbonArray does not recognize field type '"+$switch+"'.");
 				}
 				$arr.add($win);
 			}
 		} catch (EOFException $e) {
 			throw new EbonException("Unexpected end of EbonArray.", $e);
 		} catch (IOException $e) {
 			if ($e instanceof EbonException) throw (EbonException)$e;	// i hate this line so
 			// we can't really get io exceptions from reading from an internal buffer we just declared...
 			throw new EbonException($e);
 		}
 	}
 }
