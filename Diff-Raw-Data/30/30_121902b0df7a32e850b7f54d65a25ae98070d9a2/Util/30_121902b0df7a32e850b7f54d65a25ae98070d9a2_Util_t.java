 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ============================================================================*/
 
 package net.sf.okapi.common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.Channel;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.CharsetEncoder;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 
 import org.w3c.dom.Node;
 
 /**
  * Collection of various all-purpose helper functions.
  */
 public class Util {
 
 	public static final String LINEBREAK_DOS = "\r\n";
 	public static final String LINEBREAK_UNIX = "\n";
 	public static final String LINEBREAK_MAC = "\r";
 
 	public static final String RTF_STARTCODE = "{\\cs5\\f1\\cf15\\lang1024 ";
 	public static final String RTF_ENDCODE = "}";
 	public static final String RTF_STARTINLINE = "{\\cs6\\f1\\cf6\\lang1024 ";
 	public static final String RTF_ENDINLINE = "}";
 	public static final String RTF_STARTMARKER = "{\\cs15\\v\\cf12\\sub\\f2 \\{0>}{\\v\\f1 ";
 	public static final String RTF_MIDMARKER1 = "}{\\cs15\\v\\cf12\\sub\\f2 <\\}";
 	public static final String RTF_MIDMARKER2 = "\\{>}";
 	public static final String RTF_ENDMARKER = "{\\cs15\\v\\cf12\\sub\\f2 <0\\}}";
 
 	private static final String NEWLINES_REGEX = "\r(\n)?";
 	private static final Pattern NEWLINES_REGEX_PATTERN = Pattern.compile(NEWLINES_REGEX);
 
 	/**
 	 * Convert all .r\n and \r to linefeed (\n)
 	 * 
 	 * @param text
 	 * @return converted string
 	 */
 	static public String normalizeNewlines(String text) {
 		return NEWLINES_REGEX_PATTERN.matcher(text).replaceAll("\n");
 	}
 
 	/**
 	 * Removes from the start of a string any of the specified characters.
 	 * 
 	 * @param text
 	 *            String to trim.
 	 * @param chars
 	 *            List of the characters to trim.
 	 * @return The trimmed string.
 	 */
 	static public String trimStart(String text, String chars) {
 		if (text == null)
 			return text;
 		int n = 0;
 		while (n < text.length()) {
 			if (chars.indexOf(text.charAt(n)) == -1)
 				break;
 			n++;
 		}
 		if (n >= text.length())
 			return "";
 		if (n > 0)
 			return text.substring(n);
 		return text;
 	}
 
 	/**
 	 * Removes from the end of a string any of the specified characters.
 	 * 
 	 * @param text
 	 *            String to trim.
 	 * @param chars
 	 *            List of the characters to trim.
 	 * @return The trimmed string.
 	 */
 	static public String trimEnd(String text, String chars) {
 		if (text == null)
 			return text;
 		int n = text.length() - 1;
 		while (n >= 0) {
 			if (chars.indexOf(text.charAt(n)) == -1)
 				break;
 			n--;
 		}
 		if (n < 0)
 			return "";
 		if (n > 0)
 			return text.substring(0, n + 1);
 		return text;
 	}
 
 	static public String trim(String text) {
 		
 		if (text == null) return "";	
 						
 		return text.trim();
 	}
 	
 	/**
 	 * Gets the directory name of a full path.
 	 * 
 	 * @param path
 	 *            Full path from where to extract the directory name. The path
 	 *            can be a URL path (e.g. "/C:/test/file.ext").
 	 * @return The directory name (without the final separator), or an empty
 	 *         string if path is a filename.
 	 */
 	static public String getDirectoryName(String path) {
		String tmp = path.replace('\\', '/'); // Normalize separators (some path are mixed)
		int n = tmp.lastIndexOf('/'); // Try generic first
//		if (n == -1) { // Then try Windows
//			n = path.lastIndexOf('\\');
//		}
 		if (n > 0)
 			return path.substring(0, n);
 		else
 			return "";
 	}
 
 	/**
 	 * Creates the directory tree for the give full path (dir+filename)
 	 * 
 	 * @param path
 	 *            Directory and filename. If you want to pass only a directory
 	 *            name make sure it has a trailing separator (e.g.
 	 *            "c:\project\tmp\"). The path can be a URL path (e.g.
 	 *            "/C:/test/file.ext").
 	 */
 	static public void createDirectories(String path) {
		String tmp = path.replace('\\', '/'); // Normalize separators (some path are mixed)
		int n = tmp.lastIndexOf('/'); // Try generic first
//		if (n == -1) { // Then try Windows
//			n = path.lastIndexOf('\\');
//		}
 		if (n == -1)
 			return; // Nothing to do
 		// Else, use the directory part and create the tree
 		String dir = path.substring(0, n);
 		File F = new File(dir);
 		F.mkdirs();
 	}
 
 	/**
 	 * Escapes a string for XML.
 	 * 
 	 * @param text
 	 *            String to escape.
 	 * @param quoteMode
 	 *            0=no quote escaped, 1=apos and quot, 2=#39 and quot, and
 	 *            3=quot only.
 	 * @param escapeGT
 	 *            True to always escape '>' to gt
 	 * @param encoder
 	 *            The character set encoder to use to detect un-supported
 	 *            character, or null to never escape normal characters.
 	 * @return The escaped string.
 	 */
 	static public String escapeToXML(String text, int quoteMode, boolean escapeGT, CharsetEncoder encoder) {
 		if (text == null)
 			return "";
 		StringBuffer sbTmp = new StringBuffer(text.length());
 		char ch;
 		for (int i = 0; i < text.length(); i++) {
 			ch = text.charAt(i);
 			switch (ch) {
 			case '<':
 				sbTmp.append("&lt;");
 				continue;
 			case '>':
 				if (escapeGT)
 					sbTmp.append("&gt;");
 				else {
 					if ((i > 0) && (text.charAt(i - 1) == ']'))
 						sbTmp.append("&gt;");
 					else
 						sbTmp.append('>');
 				}
 				continue;
 			case '&':
 				sbTmp.append("&amp;");
 				continue;
 			case '"':
 				if (quoteMode > 0)
 					sbTmp.append("&quot;");
 				else
 					sbTmp.append('"');
 				continue;
 			case '\'':
 				switch (quoteMode) {
 				case 1:
 					sbTmp.append("&apos;");
 					break;
 				case 2:
 					sbTmp.append("&#39;");
 					break;
 				default:
 					sbTmp.append(text.charAt(i));
 					break;
 				}
 				continue;
 			default:
 				if (text.charAt(i) > 127) { // Extended chars
 					if (Character.isHighSurrogate(ch)) {
 						int cp = text.codePointAt(i++);
 						String tmp = new String(Character.toChars(cp));
 						if ((encoder != null) && !encoder.canEncode(tmp)) {
 							sbTmp.append(String.format("&#x%x;", cp));
 						} else {
 							sbTmp.append(tmp);
 						}
 					} else {
 						if ((encoder != null) && (!encoder.canEncode(text.charAt(i)))) {
 							sbTmp.append(String.format("&#x%04x;", text.codePointAt(i)));
 						} else { // No encoder or char is supported
 							sbTmp.append(text.charAt(i));
 						}
 					}
 				} else { // ASCII chars
 					sbTmp.append(text.charAt(i));
 				}
 				continue;
 			}
 		}
 		return sbTmp.toString();
 	}
 
 	/**
 	 * Escapes a given string into RTF format.
 	 * 
 	 * @param text
 	 *            the string to convert.
 	 * @param convertLineBreaks
 	 *            Indicates if the line-breaks should be converted.
 	 * @param lineBreakStyle
 	 *            Type of line-break conversion. 0=do nothing special, 1=close
 	 *            then re-open as external, 2=close then re-open as internal.
 	 * @param encoder
 	 *            Encoder to use for the extended characters.
 	 * @return The input string escaped to RTF.
 	 */
 	static public String escapeToRTF(String text, boolean convertLineBreaks, int lineBreakStyle, CharsetEncoder encoder) {
 		try {
 			if (text == null)
 				return "";
 			StringBuffer tmp = new StringBuffer(text.length());
 			CharBuffer tmpBuf = CharBuffer.allocate(1);
 			ByteBuffer encBuf;
 
 			for (int i = 0; i < text.length(); i++) {
 				switch (text.charAt(i)) {
 				case '{':
 				case '}':
 				case '\\':
 					tmp.append("\\" + text.charAt(i));
 					break;
 				case '\r': // to skip
 					break;
 				case '\n':
 					if (convertLineBreaks) {
 						switch (lineBreakStyle) {
 						case 1: // Outside external
 							tmp.append(RTF_ENDCODE);
 							tmp.append("\r\n\\par ");
 							tmp.append(RTF_STARTCODE);
 							continue;
 						case 2:
 							tmp.append(RTF_ENDINLINE);
 							tmp.append("\r\n\\par ");
 							tmp.append(RTF_STARTINLINE);
 							continue;
 						case 0: // Just convert
 						default:
 							tmp.append("\r\n\\par ");
 							continue;
 						}
 					} else
 						tmp.append("\n");
 					break;
 				case '\u00a0': // Non-breaking space
 					tmp.append("\\~"); // No extra space (it's a control word)
 					break;
 				case '\t':
 					tmp.append("\\tab ");
 					break;
 				case '\u2022':
 					tmp.append("\\bullet ");
 					break;
 				case '\u2018':
 					tmp.append("\\lquote ");
 					break;
 				case '\u2019':
 					tmp.append("\\rquote ");
 					break;
 				case '\u201c':
 					tmp.append("\\ldblquote ");
 					break;
 				case '\u201d':
 					tmp.append("\\rdblquote ");
 					break;
 				case '\u2013':
 					tmp.append("\\endash ");
 					break;
 				case '\u2014':
 					tmp.append("\\emdash ");
 					break;
 				case '\u200d':
 					tmp.append("\\zwj ");
 					break;
 				case '\u200c':
 					tmp.append("\\zwnj ");
 					break;
 				case '\u200e':
 					tmp.append("\\ltrmark ");
 					break;
 				case '\u200f':
 					tmp.append("\\rtlmark ");
 					break;
 
 				default:
 					if (text.charAt(i) > 127) {
 						if (encoder.canEncode(text.charAt(i))) {
 							tmpBuf.put(0, text.charAt(i));
 							tmpBuf.position(0);
 							encBuf = encoder.encode(tmpBuf);
 							if (encBuf.limit() > 1) {
 								tmp.append(String.format("{\\uc%d", encBuf.limit()));
 								tmp.append(String.format("\\u%d", (int) text.charAt(i)));
 								for (int j = 0; j < encBuf.limit(); j++) {
 									tmp.append(String.format("\\'%x", (encBuf.get(j) < 0 ? (0xFF ^ ~encBuf.get(j))
 											: encBuf.get(j))));
 								}
 								tmp.append("}");
 							} else {
 								tmp.append(String.format("\\u%d", (int) text.charAt(i)));
 								tmp.append(String.format("\\'%x", (encBuf.get(0) < 0 ? (0xFF ^ ~encBuf.get(0)) : encBuf
 										.get(0))));
 							}
 						} else { // Cannot encode in the RTF encoding, so use
 							// just Unicode
 							tmp.append(String.format("\\u%d ?", (int) text.charAt(i)));
 						}
 					} else
 						tmp.append(text.charAt(i));
 					break;
 				}
 			}
 			return tmp.toString();
 		} catch (CharacterCodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Copy one {@link Channel} to another.
 	 * 
 	 * @param inChannel
 	 *            - input Channel
 	 * @param outChannel
 	 *            - output Channel
 	 * @throws OkapiIOException
 	 */
 	public static void copy(ReadableByteChannel inChannel, WritableByteChannel outChannel) {
 		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
 		try {
 			while (inChannel.read(buffer) != -1) {
 				// prepare the buffer to be drained
 				buffer.flip();
 				// write to the channel, may block
 				outChannel.write(buffer);
 				// If partial transfer, shift remainder down
 				// If buffer is empty, same as doing clear()
 				buffer.compact();
 			}
 
 			// EOF will leave buffer in fill state
 			buffer.flip();
 			// make sure the buffer is fully drained.
 			while (buffer.hasRemaining()) {
 				outChannel.write(buffer);
 			}
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	/**
 	 * Copy an {@link InputStream} to a File.
 	 * 
 	 * @param is
 	 *            - input stream
 	 * @param outputFile
 	 *            - output {@link File}
 	 * @throws OkapiIOException
 	 */
 	public static void copy(InputStream is, File outputFile) {
 		copy(is, outputFile.getAbsolutePath());
 	}
 
 	/**
 	 * Copy an {@link InputStream} to a File.
 	 * 
 	 * @param is
 	 *            - input stream
 	 * @param outputPath
 	 *            - output path
 	 * @throws OkapiIOException
 	 */
 	public static void copy(InputStream is, String outputPath) {
 		ReadableByteChannel inChannel = null;
 		WritableByteChannel outChannel = null;
 		try {
 			inChannel = Channels.newChannel(is);
 			outChannel = new FileOutputStream(new File(outputPath)).getChannel();
 			copy(inChannel, outChannel);
 		} catch (FileNotFoundException e) {
 			throw new OkapiIOException(e);
 		} finally {
 			try {
 				if (inChannel != null)
 					inChannel.close();
 				if (outChannel != null)
 					outChannel.close();
 			} catch (IOException e) {
 				throw new OkapiIOException(e);
 			}
 		}
 	}
 
 	/**
 	 * Copies one file to another.
 	 * 
 	 * @param in
 	 *            - input File
 	 * @param out
 	 *            - output File
 	 * @throws OkapiIOException
 	 */
 	public static void copyFile(File in, File out) {
 		FileChannel inChannel = null;
 		FileChannel outChannel = null;
 		try {
 			inChannel = new FileInputStream(in).getChannel();
 			outChannel = new FileOutputStream(out).getChannel();
 			copy(inChannel, outChannel);
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		} finally {
 			try {
 				if (inChannel != null)
 					inChannel.close();
 				if (outChannel != null)
 					outChannel.close();
 			} catch (IOException e) {
 				throw new OkapiIOException(e);
 			}
 		}
 	}
 
 	/**
 	 * Copies a file from one location to another.
 	 * 
 	 * @param fromPath
 	 *            The path of the file to copy.
 	 * @param toPath
 	 *            The path of the copy to make.
 	 * @param move
 	 *            True to move the file, false to copy it.
 	 * @throws OkapiIOException
 	 */
 	public static void copyFile(String fromPath, String toPath, boolean move) {
 		FileChannel ic = null;
 		FileChannel oc = null;
 		try {
 			createDirectories(toPath);
 			ic = new FileInputStream(fromPath).getChannel();
 			oc = new FileOutputStream(toPath).getChannel();
 			copy(ic, oc);
 			if (move) {
 				ic.close();
 				ic = null;
 				File file = new File(fromPath);
 				file.delete();
 			}
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		} finally {
 			try {
 				if (ic != null)
 					ic.close();
 				if (oc != null)
 					oc.close();
 			} catch (IOException e) {
 				throw new OkapiIOException(e);
 			}
 		}
 	}
 
 	/**
 	 * Recursive function to delete the content of a given directory (including
 	 * all its sub-directories. This does not delete the original parent
 	 * directory.
 	 * 
 	 * @param directory
 	 *            Directory of the content to delete.
 	 */
 	private static void deleteDirectory(File directory) {
 		for (File f : directory.listFiles()) {
 			if (f.isDirectory()) {
 				deleteDirectory(f);
 			}
 			f.delete();
 		}
 	}
 
 	/**
 	 * Delete the content of a given directory, and if requested, the directory
 	 * itself. Sub-directories and their content are part of the deleted
 	 * content.
 	 * 
 	 * @param directory
 	 *            The path of the directory to delete
 	 * @param contentOnly
 	 *            Indicates if the directory itself should be removed. If this
 	 *            flag is false, only the content is deleted.
 	 */
 	public static void deleteDirectory(String directory, boolean contentOnly) {
 		File f = new File(directory);
 		// Make sure this is a directory
 		if (!f.isDirectory())
 			return;
 		deleteDirectory(f);
 		if (!contentOnly)
 			f.delete();
 	}
 
 	/**
 	 * Gets the filename of a path.
 	 * 
 	 * @param path
 	 *            The path from where to get the filename. The path can be a URL
 	 *            path (e.g. "/C:/test/file.ext").
 	 * @param keepExtension
 	 *            True to keep the existing extension, false to remove it.
 	 * @return The filename with or without extension.
 	 */
 	static public String getFilename(String path, boolean keepExtension) {
 		// Get the filename
 		int n = path.lastIndexOf('/'); // Try generic first
 		if (n == -1) { // Then try Windows
 			n = path.lastIndexOf('\\');
 		}
 		if (n > -1)
 			path = path.substring(n + 1);
 		// Stop here if we keep the extension
 		if (keepExtension)
 			return path;
 		// Else: remove the extension if there is one
 		n = path.lastIndexOf('.');
 		if (n > -1)
 			return path.substring(0, n);
 		else
 			return path;
 	}
 
 	/**
 	 * Gets the extension of a given path or filename.
 	 * 
 	 * @param path
 	 *            The original path or filename.
 	 * @return The last extension of the filename (including the period), or
 	 *         null if there is no period in the filename. If the filename ends
 	 *         with a period, the return is a period.
 	 */
 	static public String getExtension(String path) {
 		// Get the extension
 		int n = path.lastIndexOf('.');
 		if (n == -1)
 			return null; // None
 		return path.substring(n);
 	}
 
 	/**
 	 * Makes a URI string from a path. If the path itself can be recognized as a
 	 * string URI already, it is passed unchanged. For example "C:\test" and
 	 * "file:///C:/test" will both return "file:///C:/test" encoded as URI.
 	 * 
 	 * @param pathOrUri
 	 *            The path to change to URI string.
 	 * @return The URI string.
 	 * @throws OkapiUnsupportedEncodingException
 	 */
 	static public String makeURIFromPath(String pathOrUri) {
 		// This should catch most of the URI forms
 		pathOrUri = pathOrUri.replace('\\', '/');
 		if (pathOrUri.indexOf("://") != -1)
 			return pathOrUri;
 		// If not that, then assume it's a file
 		try {
 			String tmp = URLEncoder.encode(pathOrUri, "UTF-8");
 			// Use '%20' instead of '+': '+ not working with File(uri) it seems
 			return "file:///" + tmp.replace("+", "%20");
 		} catch (UnsupportedEncodingException e) {
 			throw new OkapiUnsupportedEncodingException(e); // UTF-8 should be
 			// always supported
 			// anyway
 		}
 	}
 
 	/**
 	 * Creates a new URI object from a path or a URI string.
 	 * 
 	 * @param pathOrUri
 	 *            The path or URI string to use.
 	 * @return The new URI object for the given path or URI string.
 	 */
 	static public URI toURI(String pathOrUri) {
 		try {
 			return new URI(makeURIFromPath(pathOrUri));
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Gets the longest common path between an existing current directory and a
 	 * new one.
 	 * 
 	 * @param currentDir
 	 *            The current longest common path.
 	 * @param newDir
 	 *            The new directory to compare with.
 	 * @param ignoreCase
 	 *            True if the method should ignore cases differences.
 	 * @return The longest sub-directory that is common to both directories.
 	 *         This can be a null or empty string.
 	 */
 	static public String longestCommonDir(String currentDir, String newDir, boolean ignoreCase) {
 		if (currentDir == null)
 			return newDir;
 		if (currentDir.length() == 0)
 			return currentDir;
 
 		// Get temporary copies
 		String currentLow = currentDir;
 		String newLow = newDir;
 		if (ignoreCase) {
 			currentLow = currentDir.toLowerCase();
 			newLow = newDir.toLowerCase();
 		}
 
 		// The new path equals, or include the existing root: no change
 		if (newLow.indexOf(currentLow) == 0)
 			return currentDir;
 
 		// Search the common path
 		String tmp = currentLow;
 		int i = 0;
 		while (newLow.indexOf(tmp) != 0) {
 			tmp = Util.getDirectoryName(tmp);
 			i++;
 			if (tmp.length() == 0)
 				return ""; // No common path at all
 		}
 
 		// Do not return currentDir.substring(0, tmp.length());
 		// because the lower-case string maybe of a different length than cased
 		// one
 		// (e.g. German Sz). Instead re-do the splitting as many time as needed.
 		tmp = currentDir;
 		for (int j = 0; j < i; j++) {
 			tmp = Util.getDirectoryName(tmp);
 		}
 		return tmp;
 	}
 
 	/**
 	 * Indicates if the current OS is case-sensitive.
 	 * 
 	 * @return True if the current OS is case-sensitive, false if otherwise.
 	 */
 	static public boolean isOSCaseSensitive() {
 		// May not work on all platforms,
 		// But should on basic Windows, Mac and Linux
 		// (Use Windows file separator-type to guess the OS)
 		return !File.separator.equals("\\");
 	}
 
 	/**
 	 * Writes a Byte-Order-Mark if the encoding indicates it is needed. This
 	 * methods must be the first call after opening the writer.
 	 * 
 	 * @param writer
 	 *            Writer where to output the BOM.
 	 * @param bomOnUTF8
 	 *            Indicates if we should use a BOM on UTF-8 files.
 	 * @param encoding
 	 *            Encoding of the output.
 	 * @throws OkapiIOException
 	 */
 	static public void writeBOMIfNeeded(Writer writer, boolean bomOnUTF8, String encoding) {
 		try {
 			String tmp = encoding.toLowerCase();
 
 			// Check UTF-8 first (most cases)
 			if ((bomOnUTF8) && (tmp.equalsIgnoreCase("utf-8"))) {
 				writer.write("\ufeff");
 				return;
 			}
 
 			/*
 			 * It seems writers do the following: For "UTF-16" they output
 			 * UTF-16BE with a BOM For "UTF-16LE" they output UTF-16LE without
 			 * BOM For "UTF-16BE" they output UTF-16BE without BOM So we force a
 			 * BOM for UTF-16LE and UTF-16BE
 			 */
 			if (tmp.equals("utf-16be") || tmp.equals("utf-16le")) {
 				writer.write("\ufeff");
 				return;
 			}
 			// TODO: Is this an issue? Does *reading* UTF-16LE/BE does not check
 			// for BOM?
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	/**
 	 * Gets the default system temporary directory to use for the current user.
 	 * The directory path returned has never a trailing separator.
 	 * 
 	 * @return The directory path of the temporary directory to use, without
 	 *         trailing separator.
 	 */
 	public static String getTempDirectory() {
 		String tmp = System.getProperty("java.io.tmpdir");
 		// Normalize for all platforms: no trailing separator
 		if (tmp.endsWith(File.separator)) // This separator is always
 			// platform-specific
 			tmp = tmp.substring(0, tmp.length() - 1);
 		return tmp;
 	}
 
 	/**
 	 * Splits a given ISO language tag into its components.
 	 * 
 	 * @param language
 	 *            The language code to process.
 	 * @return An array of two strings: 0=language, 1=region/country (or empty)
 	 */
 	public static String[] splitLanguageCode(String language) {
 		if ((language == null) || (language.length() == 0))
 			return null;
 		String[] parts = new String[2];
 		language = language.replace('_', '-');
 		int n = language.indexOf('-');
 		if (n > -1) {
 			parts[0] = language.substring(0, n);
 			parts[1] = language.substring(n + 1);
 		} else {
 			parts[0] = language;
 			parts[1] = "";
 		}
 		return parts;
 	}
 
 	/**
 	 * Gets the text content of the first TEXT child of an element node. This is
 	 * to use instead of node.getTextContent() which does not work with some
 	 * Macintosh Java VMs. Note this work-around get <b>only the first TEXT
 	 * node</b>.
 	 * 
 	 * @param node
 	 *            the container element.
 	 * @return the text of the first TEXT child node.
 	 */
 	public static String getTextContent(Node node) {
 		Node tmp = node.getFirstChild();
 		while (true) {
 			if (tmp == null)
 				return "";
 			if (tmp.getNodeType() == Node.TEXT_NODE) {
 				return tmp.getNodeValue();
 			}
 			tmp = tmp.getNextSibling();
 		}
 	}
 
 	/**
 	 * Calculates safely a percentage. If the total is 0, the methods return 1.
 	 * 
 	 * @param part
 	 *            The part of the total.
 	 * @param total
 	 *            the total.
 	 * @return The percentage of part in total.
 	 */
 	public static int getPercentage(int part, int total) {
 		return (total == 0 ? 1 : Math.round((float) part / (float) total * 100));
 	}
 
 	/**
 	 * Creates a string ID based on the hash code of the given text.
 	 * 
 	 * @param text
 	 *            The text to make an ID for.
 	 * @return The string ID for the given text.
 	 */
 	public static String makeID(String text) {
 		int n = text.hashCode();
 		return String.format("%s%X", ((n > 0) ? 'P' : 'N'), n);
 	}
 
 	/**
 	 * Indicates if two language codes are 'the same'. The comparison ignores
 	 * case differences, and if the parameter ignoreRegion is true, any part
 	 * after the first '-' is also ignored. Note that the character '_' is
 	 * treated like a character '-'.
 	 * 
 	 * @param lang1
 	 *            first language code to compare.
 	 * @param lang2
 	 *            second language code to compare.
 	 * @param ignoreRegion
 	 *            True to ignore any part after the first separator, false to
 	 *            take it into account.
 	 * @return true if, according the given options, the two language codes are
 	 *         the same. False otherwise.
 	 */
 	static public boolean isSameLanguage(String lang1, String lang2, boolean ignoreRegion) {
 		lang1 = lang1.replace('_', '-');
 		lang2 = lang2.replace('_', '-');
 		if (ignoreRegion) { // Do not take the region part into account
 			int n = lang1.indexOf('-');
 			if (n > -1) {
 				lang1 = lang1.substring(0, n);
 			}
 			n = lang2.indexOf('-');
 			if (n > -1) {
 				lang2 = lang2.substring(0, n);
 			}
 		}
 		return lang1.equalsIgnoreCase(lang2);
 	}
 
 	/**
 	 * Indicates if a given string is null or empty.
 	 * 
 	 * @param string
 	 *            the string to check.
 	 * @return true if the given string is null or empty.
 	 */
 	static public boolean isEmpty(String string) {
 		// return (string == null || string == ""); // !!! Doesn't work
 		return (string == null || string == "" || (string != null && string.length() == 0));
 	}
 	
 	static public boolean isEmpty(String string, boolean ignoreWS) {
 		return (string == null || string == "" ||				
 				(string != null && 
 						(string.length() == 0 || 
 						(ignoreWS && isEmpty(string.trim()))
 				)));
 	}
 	
 	static public boolean isEmpty(StringBuilder sb) {
 		return (sb == null ||(sb != null && sb.length() == 0));
 	}
 
 	static public <E> boolean isEmpty(List <E> e) {
 		return (e == null ||(e != null && e.isEmpty()));
 	}
 	
 // Safe string functions	
 	static public int getLength(String string) {
 		return (isEmpty(string)) ? 0 : string.length();
 	}
 
 	static public char getCharAt(String string, int pos) {
 		if (isEmpty(string))
 			return '\0';
 
 		return (string.length() > pos) ? string.charAt(pos) : 0;
 	}
 
 	static public char getLastChar(String string) {
 		if (isEmpty(string))
 			return '\0';
 
 		return string.charAt(string.length() - 1);
 	}
 
 	static public String deleteLastChar(String string) {
 		if (isEmpty(string))
 			return "";
 
 		return string.substring(0, string.length() - 1);
 	}
 
 	static public char getLastChar(StringBuilder sb) {
 		if ((sb == null) || (sb.length() == 0))
 			return '\0';
 		return sb.charAt(sb.length() - 1);
 	}
 
 	static public void deleteLastChar(StringBuilder sb) {
 		if ((sb == null) || (sb.length() == 0))
 			return;
 		sb.deleteCharAt(sb.length() - 1);
 	}
 
 // List helpers	
 	public static <E> boolean checkIndex(int index, List<E> list) {
 		
 		return ((list != null) && (index >= 0) && (index < list.size()));
 	}
 
 	/**
 	 * Converts an integer value to a string.
 	 * This method simply calls <code>String.valueOf(intValue);</code>.
 	 * @param value the value to convert.
 	 * @return the string representation of the given value.
 	 */
 	public static String intToStr (int value) {
 		return String.valueOf(value);
 	}
 	
 	/**
 	 * Converts a string to an integer. If the conversion fails the method
 	 * returns the given default value.
 	 * @param stringValue the string to convert.
 	 * @param intDefault the default value to use if the conversion fails.
 	 * @return the integer value of the string, or the provided default
 	 * value if the conversion failed.
 	 */
 	public static int strToInt (String value, int intDefault) {
 		if (Util.isEmpty(value))
 			return intDefault;
 		try {
 			return Integer.valueOf(value);
 		}
 		catch (NumberFormatException e) {
 			return intDefault; 
 		}
 	}
 
 	/**
 	 * Gets the element of an array for a given index.
 	 * the method returns null if the index is out of bounds.
 	 * @param array the array where to lookup the element.
 	 * @param index the index.
 	 * @return the element of the array for the given index, or null if the
 	 * index is out of bounds, or if the element is null.
 	 */
 	public static <T>T get(T[] array, int index) {
 		if (index >= 0 && index < array.length)
 			return array[index];
 		else
 			return null;
 	}
 
 	/**
 	 * Indicates whether a byte-flag is set or not in a given value. 
 	 * @param value the value to check.
 	 * @param flag the flag to look for.
 	 * @return true if the flag is set, false if it is not.
 	 */
 	public static boolean checkFlag (int value, int flag) {
 		return (value & flag) == flag;
 	}
 
 }
