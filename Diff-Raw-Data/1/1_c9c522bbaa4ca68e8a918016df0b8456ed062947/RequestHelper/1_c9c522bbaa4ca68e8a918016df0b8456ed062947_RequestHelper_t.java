 /*
  * Sewing: a Simple framework for Embedded-OSGi Web Development
  * Copyright (C) 2009 Bug Labs
  * Email: bballantine@buglabs.net
  * Site: http://www.buglabs.net
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA  02111-1307, USA.
  */
 
 package com.buglabs.osgi.sewing.pub.util;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.osgi.service.log.LogService;
 
 import com.buglabs.osgi.sewing.LogManager;
 
 /**
  * Static methods for helping us parse a string list of paramaters typically
  * pulled off the request object in a get request or form post
  * 
  * @author brian
  * 
  */
 public class RequestHelper {
 
 	private static final String CONTENT_TYPE = "Content-Type";
 	private static final String MULTIPART_TYPE = "multipart/form-data";
 	private static final String BOUNDARY_KEY = "boundary";
 	private static final String ENCODING = "ISO-8859-1";
 
 	public static RequestParameters parseParams(HttpServletRequest req) {
 		RequestParameters params = new RequestParameters();
 
 		// do the querystring
 		String querystring = req.getQueryString();
 		if (querystring != null)
 			params = parseParamString(querystring);
 
 		String requestBody = "";
 		try {
 			requestBody = RequestReader.read(req);
 		} catch (IOException e) {
 			LogManager.log(LogService.LOG_DEBUG, "Failed to read body.", e);
 		}
 
 		params = parseParamString(requestBody, params);
 
 		// since we've pulled the data off the input stream
 		// it makes sense to make the raw body available
 		params.put(RequestParameters.REQUEST_BODY_PARAM_KEY, requestBody);
 
 		return params;
 	}
 
 	/**
 	 * parse a param string into a RequestParameters object
 	 * 
 	 * @param param_string
 	 * @return
 	 */
 	static public RequestParameters parseParamString(String param_string) {
 		return parseParamString(param_string, new RequestParameters());
 	}
 
 	/**
 	 * Parse a param string, adding it to an existing parameters object
 	 * 
 	 * @param param_string
 	 * @param param_map
 	 * @return
 	 */
 	static public RequestParameters parseParamString(String param_string, RequestParameters param_map) {
 		param_string = param_string.trim();
 
 		String k = "", v = "", tmp_key = "";
 		char mode = 'k';
 		int len = param_string.length();
 		for (int n = 0; n != len; ++n) {
 			char c = param_string.charAt(n);
 			if (c == '&') {
 				tmp_key = unescape(k);
 				if (param_map.get(tmp_key) == null)
 					param_map.put(tmp_key, unescape(v));
 				else
 					param_map.put(tmp_key, param_map.get(tmp_key) + "," + unescape(v));
 				k = v = "";
 				mode = 'k';
 			} else if (c == '=') {
 				mode = 'v';
 			} else {
 				if (mode == 'k') {
 					k += c;
 				} else if (mode == 'v') {
 					v += c;
 				}
 			}
 			if (n == len - 1 && k.length() > 0 && v.length() > 0) {
 				tmp_key = unescape(k);
 				if (param_map.get(tmp_key) == null)
 					param_map.put(tmp_key, unescape(v));
 				else
 					param_map.put(tmp_key, param_map.get(tmp_key) + "," + unescape(v));
 			}
 
 		}
 		return param_map;
 	}
 
 	static private String unescape(String s) {
 		StringBuffer sbuf = new StringBuffer();
 		int l = s.length();
 		int ch = -1;
 		int b, sumb = 0;
 		for (int i = 0, more = -1; i < l; i++) {
 			/* Get next byte b from URL segment s */
 			switch (ch = s.charAt(i)) {
 			case '%':
 				ch = s.charAt(++i);
 				int hb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
 				ch = s.charAt(++i);
 				int lb = (Character.isDigit((char) ch) ? ch - '0' : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
 				b = (hb << 4) | lb;
 				break;
 			case '+':
 				b = ' ';
 				break;
 			default:
 				b = ch;
 			}
 			/* Decode byte b as UTF-8, sumb collects incomplete chars */
 			if ((b & 0xc0) == 0x80) { // 10xxxxxx (continuation byte)
 				sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
 				if (--more == 0)
 					sbuf.append((char) sumb); // Add char to sbuf
 			} else if ((b & 0x80) == 0x00) { // 0xxxxxxx (yields 7 bits)
 				sbuf.append((char) b); // Store in sbuf
 			} else if ((b & 0xe0) == 0xc0) { // 110xxxxx (yields 5 bits)
 				sumb = b & 0x1f;
 				more = 1; // Expect 1 more byte
 			} else if ((b & 0xf0) == 0xe0) { // 1110xxxx (yields 4 bits)
 				sumb = b & 0x0f;
 				more = 2; // Expect 2 more bytes
 			} else if ((b & 0xf8) == 0xf0) { // 11110xxx (yields 3 bits)
 				sumb = b & 0x07;
 				more = 3; // Expect 3 more bytes
 			} else if ((b & 0xfc) == 0xf8) { // 111110xx (yields 2 bits)
 				sumb = b & 0x03;
 				more = 4; // Expect 4 more bytes
 			} else /*if ((b & 0xfe) == 0xfc)*/{ // 1111110x (yields 1 bit)
 				sumb = b & 0x01;
 				more = 5; // Expect 5 more bytes
 			}
 			/* No need to test if the UTF-8 encoding is well-formed */
 		}
 		return sbuf.toString();
 	}
 
 	/**
 	 * Does the request come from a multipart post?
 	 * 
 	 * @param req
 	 * @return
 	 */
 	public static boolean isMultipart(HttpServletRequest req) {
 		String[] contentType = StringUtil.split(req.getHeader(CONTENT_TYPE), ";");
 		return (contentType.length > 0 && contentType[0].trim().equals(MULTIPART_TYPE));
 	}
 
 	/**
 	 * Parse a multipart form
 	 * 
 	 * @param req
 	 * @return
 	 */
 	public static RequestParameters parseMultipart(HttpServletRequest req) {
 		RequestParameters params = new RequestParameters();
 		String boundary = getMultipartBoundary(req);
 		try {
 			params = readMultipart(boundary, req);
 		} catch (IOException e) {
 			LogManager.log(LogService.LOG_ERROR, "Failed to read multi-part body.", e);
 		}
 		return params;
 	}
 
 	/**
 	 * Get's the item boundary for a multipart post from the header
 	 * 
 	 * @param req
 	 * @return
 	 */
 	public static String getMultipartBoundary(HttpServletRequest req) {
 		// if multipart, Content-Type will look something like:
 		// multipart/form-data; boundary=-----------------1372285795046453821504463
 		String[] contentType = StringUtil.split(req.getHeader(CONTENT_TYPE), ";");
 
 		// Extra check while we're here
 		if (contentType.length < 1 || !contentType[0].trim().equals(MULTIPART_TYPE)) {
 			return null;
 		}
 
 		// Scan to the "boundary" item
 		int x = 1;
 		while (x < contentType.length && !contentType[x].trim().startsWith(BOUNDARY_KEY))
 			x++;
 		if (x == contentType.length)
 			return null;
 
 		// second string in item is boundaryItem
 		// return it if it's found
 		String[] boundaryItem = StringUtil.split(contentType[x].trim(), "=");
 
 		if (boundaryItem.length == 2 && boundaryItem[0].trim().equals(BOUNDARY_KEY))
 			return "--" + boundaryItem[1].trim();
 
 		// default, boundary not found
 		return null;
 	}
 
 	/**
 	 * Read from a BufferedReader representing a multipart form post into a
 	 * RequestParameters object
 	 * 
 	 * @param boundary
 	 *            separator between multipart chunks
 	 * @param reader
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	private static RequestParameters readMultipart(String boundary, HttpServletRequest req) throws IOException {
 		RequestParameters params = new RequestParameters();
 
 		String requestBody = RequestReader.read(req);
 
 		String[] chunks = StringUtil.split(requestBody, boundary);
 		for (int i = 0; i < chunks.length; i++) {
 			// last boundary ends in --, after split, will be last item
 			if (chunks[i].trim().equals("--"))
 				continue;
 			params.add(readMultipartChunk(chunks[i]));
 		}
 
 		return params;
 	}
 
 	/**
 	 * Reads a multipart chunk (which is a string) and returns a
 	 * RequestParameters object with one parameter representing the chunk
 	 * 
 	 * @param chunk
 	 *            a string from the multipart post representing one item
 	 * @return
 	 */
 	private static RequestParameters readMultipartChunk(String chunk) {
 		String[] lines = StringUtil.split(chunk, "\n");
 
 		// Go through the chunk and pull out data
 		String name = null, val = null, filename = null, contentType = null;
 		boolean dataRead = false;
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i].trim();
 
 			// content-disposition is usually the first line of the chunk
 			// it has useful stuff in it like field name and filename
 			if (line.toLowerCase().startsWith("content-disposition")) {
 				name = getName(line);
 				filename = getFilename(line);
 				dataRead = true;
 			}
 
 			// content-type only appears with files
 			if (line.toLowerCase().startsWith("content-type")) {
 				contentType = getContentType(line);
 			}
 
 			// a blank line separates the header stuff found above
 			// from the data
 			// dataRead just means we've already found the header stuff
 			if (dataRead && line.length() == 0) {
 				val = getVal(lines, i + 1);
 				break; // data will be last thing
 			}
 		}
 
 		return createParamsFromData(name, val, filename, contentType);
 	}
 
 	/**
 	 * Take in all the stuff that might make up a Request Parameter and create a
 	 * new RequestParameters object with only one param
 	 * 
 	 * @param name
 	 *            param name
 	 * @param val
 	 *            the value as string (could be the contents of a file)
 	 * @param filename
 	 *            if it's a file, this will be set
 	 * @param contentType
 	 *            if it's a file, this will be the content type
 	 * @return a new RequestParameters object w/ one item
 	 */
 	private static RequestParameters createParamsFromData(String name, String val, String filename, String contentType) {
 		RequestParameters params = new RequestParameters();
 		// Something we can save
 		if (name != null && val != null) {
 
 			// it's not a file if fields are null
 			// if filename length is 0, it's a file who's form
 			// 	field wasn't filled out, so we store the empty value
 			if (filename == null || contentType == null || filename.length() == 0) {
 				params.put(name, val.trim());
 			} else { // it's a file
 				try {
 					params.setFile(new FormFile(filename, contentType, val.getBytes(ENCODING)));
 				} catch (UnsupportedEncodingException e) {
 					LogManager.log(LogService.LOG_ERROR, "Failed to set file.", e);
 				}
 			}
 
 		}
 		return params;
 	}
 
 	/**
 	 * puts the lines from the mulitpart chunk together into a piece of data
 	 * 
 	 * @param lines
 	 *            an array of lines from the mulitpart chunk
 	 * @param i
 	 *            which line to start writing to data
 	 * @return the final composed data as a string
 	 */
 	private static String getVal(String[] lines, int i) {
 		StringBuffer sbuf = new StringBuffer();
 		// append all but the last line
 		while (i < lines.length - 1) {
 			sbuf.append(lines[i]);
 			sbuf.append('\n');
 			i++;
 		}
 		sbuf.append(lines[i].trim()); // last line w/o line break
 		return sbuf.toString();
 	}
 
 	/**
 	 * Extract param name from a line
 	 * 
 	 * @param line
 	 *            will look something like this: Content-Disposition: form-data;
 	 *            name="myfile"; filename="test.jpg"
 	 * @return
 	 */
 	private static String getName(String line) {
 		String val = getValFrom(line, "name=", ";");
 		// remove quotes from front and back
 		return (val == null) ? null : val.substring(1, val.length() - 1);
 	}
 
 	/**
 	 * Extract content type from a line
 	 * 
 	 * @param line
 	 *            will look something like this: Content-Type: image/jpeg
 	 * @return
 	 */
 	private static String getContentType(String line) {
 		return getValFrom(line.toLowerCase().trim(), "Content-Type: ".toLowerCase(), ";");
 	}
 
 	/**
 	 * Extract filename from line
 	 * 
 	 * @param line
 	 *            will look something like this: Content-Disposition: form-data;
 	 *            name="myfile"; filename="test.jpg"
 	 * 
 	 * @return
 	 */
 	private static String getFilename(String line) {
 		String val = getValFrom(line, "filename=", ";");
 		// remove quotes from front and back
 		return (val == null) ? null : val.substring(1, val.length() - 1);
 	}
 
 	/**
 	 * Get a string from line between prefix & suffix
 	 * 
 	 * @param line
 	 * @param prefix
 	 * @param suffix
 	 * @return
 	 */
 	private static String getValFrom(String line, String prefix, String suffix) {
 
 		int start = line.indexOf(prefix);
 		if (start == -1)
 			return null;
 
 		start = start + prefix.length();
 		if (start >= line.length())
 			return null;
 
 		int end = line.indexOf(suffix, start);
 
 		String out;
 		if (end < 0)
 			out = line.substring(start);
 		else
 			out = line.substring(start, end);
 		return out;
 	}
 
 	/**
 	 * This is a copy of kgilmer's split function from
 	 * com.buglabs.util.StringUtil This function was copied over for the purpose
 	 * of trying to remove Buglabs framework code dependencies so it could
 	 * potentially run in any osgi env.
 	 * 
 	 * @author bballantine
 	 * 
 	 */
 	private static class StringUtil {
 
 		/**
 		 * custom string splitting function as CDC/Foundation does not include
 		 * String.split();
 		 * 
 		 * @param s
 		 *            Input String
 		 * @param seperator
 		 * @return
 		 */
 		public static String[] split(String s, String seperator) {
 			if (s == null || seperator == null || s.length() == 0 || seperator.length() == 0) {
 				return (new String[0]);
 			}
 
 			List tokens = new ArrayList();
 			String token;
 			int index_a = 0;
 			int index_b = 0;
 
 			while (true) {
 				index_b = s.indexOf(seperator, index_a);
 				if (index_b == -1) {
 					token = s.substring(index_a);
 
 					if (token.length() > 0) {
 						tokens.add(token);
 					}
 
 					break;
 				}
 				token = s.substring(index_a, index_b);
 				if (token.length() >= 0) {
 					tokens.add(token);
 				}
 				index_a = index_b + seperator.length();
 			}
 			String[] str_array = new String[tokens.size()];
 			for (int i = 0; i < str_array.length; i++) {
 				str_array[i] = (String) (tokens.get(i));
 			}
 			return str_array;
 		}
 	}
 
 }
