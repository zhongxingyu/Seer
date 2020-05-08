 /*
   File: CyAttributesReader.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.readers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.writers.CyAttributesWriter;
 import cytoscape.logger.CyLogger;
 import java.net.URLDecoder;
 import java.text.MessageFormat;
 
 
 // I hate writing parsing code.  Grumble grumble grumble.
 /**
  *
  */
 public class CyAttributesReader {
     public static final String DECODE_PROPERTY = "cytoscape.decode.attributes";
     private static final String badDecodeMessage =
             "Trouble when decoding attribute value, first occurence line no. {0}" +
             "\nIgnore if attributes file was created before 2.6.3 or wasn't creatad by Cytoscape." +
             "\nUse -Dcytoscape.decode.attributes=false when starting Cytoscape to turn off decoding.";
 
     private boolean badDecode;
 
     private int lineNum;
     private boolean doDecoding;
 
     public CyAttributesReader() {
 		lineNum = 0;
         doDecoding = Boolean.valueOf(System.getProperty(DECODE_PROPERTY, "true"));
     }
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param cyAttrs DOCUMENT ME!
 	 * @param fileIn DOCUMENT ME!
 	 *
 	 * @throws IOException DOCUMENT ME!
 	 */
 	public static void loadAttributes(CyAttributes cyAttrs, Reader fileIn) throws IOException {
 		CyAttributesReader ar = new CyAttributesReader();
 		ar.loadAttributesInternal(cyAttrs, fileIn);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param cyAttrs DOCUMENT ME!
 	 * @param fileIn DOCUMENT ME!
 	 *
 	 * @throws IOException DOCUMENT ME!
 	 */
 	public void loadAttributesInternal(CyAttributes cyAttrs, Reader fileIn)
 		throws IOException {
 
 		badDecode = false;
		boolean guessedAttrType = false; // We later set this to true if we have to guess the attribute type.
 
 		try {
 			final BufferedReader reader;
 
 			if (fileIn instanceof BufferedReader) {
 				reader = (BufferedReader) fileIn;
 			} else {
 				reader = new BufferedReader(fileIn);
 			}
 
 			String attributeName;
 			byte type = -1;
 
 			{
 				final String firstLine = reader.readLine();
 				lineNum++;
 
 				if (firstLine == null) {
 					return;
 				}
 
 				final String searchStr = "class=";
 				final int inx = firstLine.indexOf(searchStr);
 
 				if (inx < 0) {
 					attributeName = firstLine.trim();
 				} else {
 					attributeName = firstLine.substring(0, inx - 1).trim();
 
 					String foo = firstLine.substring(inx);
 					final StringTokenizer tokens = new StringTokenizer(foo);
 					foo = tokens.nextToken();
 
 					String className = foo.substring(searchStr.length()).trim();
 
 					if (className.endsWith(")")) {
 						className = className.substring(0, className.length() - 1);
 					}
 
 					if (className.equalsIgnoreCase("java.lang.String")
 					    || className.equalsIgnoreCase("String")) {
 						type = MultiHashMapDefinition.TYPE_STRING;
 					} else if (className.equalsIgnoreCase("java.lang.Boolean")
 					           || className.equalsIgnoreCase("Boolean")) {
 						type = MultiHashMapDefinition.TYPE_BOOLEAN;
 					} else if (className.equalsIgnoreCase("java.lang.Integer")
 					           || className.equalsIgnoreCase("Integer")) {
 						type = MultiHashMapDefinition.TYPE_INTEGER;
 					} else if (className.equalsIgnoreCase("java.lang.Double")
 					           || className.equalsIgnoreCase("Double")
 					           || className.equalsIgnoreCase("java.lang.Float")
 					           || className.equalsIgnoreCase("Float")) {
 						type = MultiHashMapDefinition.TYPE_FLOATING_POINT;
 					}
 				}
 			}
 
 			if (attributeName.indexOf("(") >= 0) {
 				attributeName = attributeName.substring(0, attributeName.indexOf("(")).trim();
 			}
 
 			boolean firstLine = true;
 			boolean list = false;
 
 			while (true) {
 				final String line = reader.readLine();
 				lineNum++;
 
 				if (line == null) {
 					break;
 				}
 
 				if ("".equals(line.trim())) {
 					continue;
 				}
 
 				int inx = line.indexOf('=');
 				String key = line.substring(0, inx).trim();
 				String val = line.substring(inx + 1).trim();
 
 				key = decodeString(key);
 
 				if (firstLine && val.startsWith("(")) {
 					list = true;
 				}
 
 				if (list) {
 					// Chop away leading '(' and trailing ')'.
 					val = val.substring(1).trim();
 					val = val.substring(0, val.length() - 1).trim();
 
 					String[] elms = val.split("::");
 					final ArrayList elmsBuff = new ArrayList();
 
 					for (String vs : elms) {
 						vs = decodeString(vs);
 						vs = decodeSlashEscapes(vs);
 						elmsBuff.add(vs);
 					}
 
 					if (firstLine) {
 						if (type < 0) {
							guessedAttrType = true;
 							while (true) {
 								try {
 									new Integer((String) elmsBuff.get(0));
 									type = MultiHashMapDefinition.TYPE_INTEGER;
 
 									break;
 								} catch (Exception e) {
 								}
 
 								try {
 									new Double((String) elmsBuff.get(0));
 									type = MultiHashMapDefinition.TYPE_FLOATING_POINT;
 
 									break;
 								} catch (Exception e) {
 								}
 
 								//               try {
 								//                 new Boolean((String) elmsBuff.get(0));
 								//                 type = MultiHashMapDefinition.TYPE_BOOLEAN;
 								//                 break; }
 								//               catch (Exception e) {}
 								type = MultiHashMapDefinition.TYPE_STRING;
 
 								break;
 							}
 						}
 
 						firstLine = false;
 					}
 
 					for (int i = 0; i < elmsBuff.size(); i++) {
 						if (type == MultiHashMapDefinition.TYPE_INTEGER) {
 							elmsBuff.set(i, new Integer((String) elmsBuff.get(i)));
 						} else if (type == MultiHashMapDefinition.TYPE_BOOLEAN) {
 							elmsBuff.set(i, new Boolean((String) elmsBuff.get(i)));
 						} else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 							elmsBuff.set(i, new Double((String) elmsBuff.get(i)));
 						} else {
 							// A string; do nothing.
 						}
 					}
 
 					cyAttrs.setListAttribute(key, attributeName, elmsBuff);
 				} else { // Not a list.
 					val = decodeString(val);
 					val = decodeSlashEscapes(val);
 
 					if (firstLine) {
 						if (type < 0) {
							guessedAttrType = true;
 							while (true) {
 								try {
 									new Integer(val);
 									type = MultiHashMapDefinition.TYPE_INTEGER;
 
 									break;
 								} catch (Exception e) {
 								}
 
 								try {
 									new Double(val);
 									type = MultiHashMapDefinition.TYPE_FLOATING_POINT;
 
 									break;
 								} catch (Exception e) {
 								}
 
 								//               try {
 								//                 new Boolean(val);
 								//                 type = MultiHashMapDefinition.TYPE_BOOLEAN;
 								//                 break; }
 								//               catch (Exception e) {}
 								type = MultiHashMapDefinition.TYPE_STRING;
 
 								break;
 							}
 						}
 
 						firstLine = false;
 					}
 
 					if (type == MultiHashMapDefinition.TYPE_INTEGER) {
 						cyAttrs.setAttribute(key, attributeName, new Integer(val));
 					} else if (type == MultiHashMapDefinition.TYPE_BOOLEAN) {
 						cyAttrs.setAttribute(key, attributeName, new Boolean(val));
 					} else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
 						cyAttrs.setAttribute(key, attributeName, new Double(val));
 					} else {
 						cyAttrs.setAttribute(key, attributeName, val);
 					}
 				}
 			}
 		} catch (Exception e) {
			String message;
			if (guessedAttrType) {
				message = "failed parsing attributes file at line: " + lineNum
					+ " with exception: " + e.getMessage()
					+ " This is most likely due to a missing attribute type on the first line.\n"
					+ "Attribute type should be one of the following: "
					+ "(class=String), (class=Boolean), (class=Integer), or (class=Double). "
					+ "(\"Double\" stands for a floating point a.k.a. \"decimal\" number.)"
					+ " This should be added to end of the first line.";
			}
			else
				message = "failed parsing attributes file at line: " + lineNum
			                  + " with exception: " + e.getMessage();
 			CyLogger.getLogger(CyAttributesReader.class).warn(message, e);
 			throw new IOException(message);
 		}
 	}
 
 	private String decodeString(String in) throws IOException {
 		if (doDecoding) {
 			try {
 				in = URLDecoder.decode(in, CyAttributesWriter.ENCODING_SCHEME);
 			}
 			catch (IllegalArgumentException iae) {
 				if (!badDecode) {
 					CyLogger.getLogger(CyAttributesReader.class).info(MessageFormat.format(badDecodeMessage, lineNum), iae);
 					badDecode = true;
 				}
 			}
 		}
 
 		return in;
 	}
 
 	private static String decodeSlashEscapes(String in) {
         final StringBuilder elmBuff = new StringBuilder();
         int inx2;
 
         for (inx2 = 0; inx2 < in.length(); inx2++) {
             char ch = in.charAt(inx2);
 
             if (ch == '\\') {
                 if ((inx2 + 1) < in.length()) {
                     inx2++;
 
                     char ch2 = in.charAt(inx2);
 
                     if (ch2 == 'n') {
                         elmBuff.append('\n');
                     } else if (ch2 == 't') {
                         elmBuff.append('\t');
                     } else if (ch2 == 'b') {
                         elmBuff.append('\b');
                     } else if (ch2 == 'r') {
                         elmBuff.append('\r');
                     } else if (ch2 == 'f') {
                         elmBuff.append('\f');
                     } else {
                         elmBuff.append(ch2);
                     }
                 } else {
                     /* val ends in '\' - just ignore it. */ }
             } else {
                 elmBuff.append(ch);
             }
         }
 
         return elmBuff.toString();
     }
 
     public boolean isDoDecoding() {
         return doDecoding;
     }
 
     public void setDoDecoding(boolean doDec) {
         doDecoding = doDec;
     }
 }
