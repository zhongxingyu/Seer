 /*
  * utils - MapWriter.java - Copyright © 2008-2010 David Roden
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package net.pterodactylus.util.collection;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.pterodactylus.util.io.Closer;
 import net.pterodactylus.util.logging.Logging;
 import net.pterodactylus.util.number.Hex;
 
 /**
  * Helper class that emulates the function of
  * {@link Properties#store(java.io.OutputStream, String)} and
  * {@link Properties#load(java.io.InputStream)} but does not suffer from the
  * drawbacks of {@link Properties} (namely the fact that a
  * <code>Properties</code> can not contain <code>null</code> values).
  *
 * @author David Roden &lt;droden@gmail.com&gt;
  */
 public class MapWriter {
 
 	/** The logger. */
 	private static final Logger logger = Logging.getLogger(MapWriter.class.getName());
 
 	/**
 	 * Writes the given map to the given writer.
 	 *
 	 * @param writer
 	 *            The writer to write the map’s content to
 	 * @param map
 	 *            The map to write
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static void write(Writer writer, Map<String, String> map) throws IOException {
 		for (Entry<String, String> entry : map.entrySet()) {
 			if (entry.getValue() != null) {
 				writer.write(encode(entry.getKey()));
 				writer.write('=');
 				writer.write(encode(entry.getValue()));
 				writer.write('\n');
 			}
 		}
 	}
 
 	/**
 	 * Reads a map from the given reader. Lines are read from the given reader
 	 * until a line is encountered that does not contain a colon (“:”) or equals
 	 * sign (“=”).
 	 *
 	 * @param reader
 	 *            The reader to read from
 	 * @return The map that was read
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static Map<String, String> read(Reader reader) throws IOException {
 		logger.log(Level.FINE, "MapWriter.read(reader=" + reader + ")");
 		Map<String, String> map = new HashMap<String, String>();
 		BufferedReader bufferedReader = new BufferedReader(reader);
 		try {
 			String line;
 			while ((line = bufferedReader.readLine()) != null) {
 				logger.log(Level.FINEST, "Read line: “" + line + "”");
 				if (line.startsWith("#") || (line.length() == 0)) {
 					continue;
 				}
 				if (line.indexOf('=') == -1) {
 					break;
 				}
 				int split = line.indexOf('=');
 				String key = decode(line.substring(0, split));
 				String value = decode(line.substring(split + 1));
 				map.put(key, value);
 			}
 		} finally {
 			Closer.close(bufferedReader);
 		}
 		return map;
 	}
 
 	//
 	// PRIVATE METHODS
 	//
 
 	/**
 	 * Encodes the given String by replacing certain “unsafe” characters. CR
 	 * (0x0d) is replaced by “\r”, LF (0x0a) is replaced by “\n”, the backslash
 	 * (‘\’) will be replaced by “\\”, other characters that are either smaller
 	 * than 0x20 or larger than 0x7f or that are ‘:’ or ‘=’ will be replaced by
 	 * their unicode notation (“\u0000” for NUL, 0x00). All other values are
 	 * copied verbatim.
 	 *
 	 * @param value
 	 *            The value to encode
 	 * @return The encoded value
 	 */
 	static String encode(String value) {
 		StringBuilder encodedString = new StringBuilder();
 		for (char character : value.toCharArray()) {
 			if (character == 0x0d) {
 				encodedString.append("\\r");
 			} else if (character == 0x0a) {
 				encodedString.append("\\n");
 			} else if (character == '\\') {
 				encodedString.append("\\\\");
 			} else if ((character < 0x20) || (character == '=') || (character > 0x7f)) {
 				encodedString.append("\\u").append(Hex.toHex(character, 4));
 			} else {
 				encodedString.append(character);
 			}
 		}
 		return encodedString.toString();
 	}
 
 	/**
 	 * Decodes the given value by reversing the changes made by
 	 * {@link #encode(String)}.
 	 *
 	 * @param value
 	 *            The value to decode
 	 * @return The decoded value
 	 */
 	static String decode(String value) {
 		StringBuilder decodedString = new StringBuilder();
 		boolean backslash = false;
 		int hexDigit = 0;
 		char[] hexDigits = new char[4];
 		for (char character : value.toCharArray()) {
 			if (hexDigit > 0) {
 				hexDigits[hexDigit - 1] = character;
 				hexDigit++;
 				if (hexDigit > 4) {
 					decodedString.append((char) Integer.parseInt(new String(hexDigits), 16));
 					hexDigit = 0;
 				}
 			} else if (backslash) {
 				if (character == '\\') {
 					decodedString.append('\\');
 				} else if (character == 'r') {
 					decodedString.append('\r');
 				} else if (character == 'n') {
 					decodedString.append('\n');
 				} else if (character == 'u') {
 					hexDigit = 1;
 				}
 				backslash = false;
 			} else if (character == '\\') {
 				backslash = true;
 				continue;
 			} else {
 				decodedString.append(character);
 			}
 		}
 		return decodedString.toString();
 	}
 
 }
