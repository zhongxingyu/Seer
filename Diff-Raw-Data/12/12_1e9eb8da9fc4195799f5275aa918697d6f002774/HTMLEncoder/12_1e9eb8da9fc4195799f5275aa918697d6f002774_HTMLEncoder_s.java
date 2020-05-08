 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 /*
  * Created on Feb 2, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.vfny.geoserver.action;
 
 /**
  * HTMLEncoder purpose.
  * 
  * <p>
  * This is a class taken from the java.sun.com Developer forums that  can be
  * used to encode/decode a String to/from HTML text.
  * </p>
  * 
  * <p>
  * Capabilities:
  * </p>
  * 
  * <ul>
  * <li>
  * encode: Encodes a String into HTML text
  * </li>
  * <li>
  * decode: Takes an HTML text and decodes it to a normal String
  * </li>
  * </ul>
  * 
  * <p>
  * Example Use:
  * </p>
  * <pre><code>
  * HTMLEncoder.encode("Knig"); //will return "K&ouml;nig" (For JavaDocs: "K&amp;ouml;nig")
  * HTMLEncoder.decode("K&ouml;nig"); //will return "Knig"
  * </code></pre>
  *
  * @author rgould, Refractions Research, Inc.
 * @author $Author: emperorkefka $ (last modification)
 * @version $Id: HTMLEncoder.java,v 1.1 2004/02/02 23:27:27 emperorkefka Exp $
  */
 public class HTMLEncoder {
     private static java.util.Hashtable chars = null;
 
     /**
      * Translate a String from HTML-text to a regular human-readable String
      * @param val the String to be decoded
      * @return
      */
     public static String decode(String val) {
         if (val != null) {
             StringBuffer buf = new StringBuffer(val.length() + 8);
             char c;
             char d;
             String res;
             int indice;
 
             for (int i = 0; i < val.length(); i++) {
                 c = val.charAt(i);
 
                 switch (c) {
                 case '&':
 
                     if ((i + 1) <= val.length()) {
                         res = val.substring(i);
                         indice = val.indexOf(';'); // Hasta el ;
 
                         if (indice != -1) {
                             d = val.charAt(1);
 
                             if (d == '#') {
                                 res = res.substring(2, indice);
 
                                 try {
                                     buf.append((char) Integer.parseInt(res));
                                     i = i + indice;
 
                                     break;
                                 } catch (Throwable t) {
                                 }
 
                                 // es un numero se coge hasta el ;
                             } else {
                                 // No es un numero
                                 res = res.substring(1, indice);
 
                                 try {
                                     buf.append(getChar(res));
                                     i = i + indice;
 
                                     break;
                                 } catch (Throwable t) {
                                 }
                             }
                         }
                     }
 
                     buf.append(c);
 
                     break;
 
                 default:
                     buf.append(c);
 
                     break;
                 }
             }
 
             return buf.toString();
         } else {
             return new String("");
         }
     }
 
     /**
      * Translate a String to an HTML-encoded version of itself
      * @param val The String to be encoded
      * @return
      */
     public static String encode(String val) {
         if (val != null) {
             StringBuffer buf = new StringBuffer(val.length() + 8);
             char c;
 
             for (int i = 0; i < val.length(); i++) {
                 c = val.charAt(i);
 
                 switch (c) {
                 case '<':
                     buf.append("&lt;");
 
                     break;
 
                 case '>':
                     buf.append("&gt;");
 
                     break;
 
                 case '&':
                     buf.append("&amp;");
 
                     break;
 
                 case '\"':
                     buf.append("&#034;");
 
                     break;
 
                 case '\'':
                     buf.append("&#039;");
 
                     break;
 
                case '':
                 case '':
                 case '':
                 case '':
                 case '':
                 case '':
                 case '':
                 case '':
                 case '':
                 case '':
                     buf.append(c + "&acute;");
 
                    break;
 
                 case 160:
                 case 161:
                 case 162:
                 case 163:
                 case 164:
                 case 165:
                 case 166:
                 case 167:
                 case 168:
                 case 169:
                 case 170:
                 case 171:
                 case 172:
                 case 173:
                 case 174:
                 case 175:
                 case 176:
                 case 177:
                 case 178:
                 case 179:
                 case 180:
                 case 181:
                 case 182:
                 case 183:
                 case 184:
                 case 185:
                 case 186:
                 case 187:
                 case 188:
                 case 189:
                 case 190:
                 case 191:
                 case 192:
                 case 194:
                 case 195:
                 case 196:
                 case 197:
                 case 198:
                 case 199:
                 case 200:
                 case 202:
                 case 203:
                 case 204:
                 case 206:
                 case 207:
                 case 208:
                 case 209:
                 case 210:
                 case 212:
                 case 213:
                 case 214:
                 case 215:
                 case 216:
                 case 217:
                 case 219:
                 case 220:
                 case 221:
                 case 222:
                 case 223:
                 case 224:
                 case 226:
                 case 227:
                 case 228:
                 case 229:
                 case 230:
                 case 231:
                 case 232:
                 case 234:
                 case 235:
                 case 236:
                 case 238:
                 case 239:
                 case 240:
                 case 241:
                 case 242:
                 case 244:
                 case 245:
                 case 246:
                 case 247:
                 case 248:
                 case 249:
                 case 251:
                 case 252:
                 case 253:
                 case 254:
                 case 255:
                     buf.append("&#" + ((int) c) + ";");
 
                     break;
 
                 default:
                     buf.append(c);
 
                     break;
                 }
             }
 
             return buf.toString();
         } else {
             return new String("");
         }
     }
 
     private static char getChar(String res) throws IllegalArgumentException {
         loadChar();
 
         Character o = (Character) chars.get(res);
 
         if (o == null) {
             throw new IllegalArgumentException("Inexistant char: " + res);
         } else {
             return o.charValue();
         }
     }
 
     private static void loadChar() {
         if (chars == null) {
             chars = new java.util.Hashtable();
             chars.put("amp", new Character((char) 38));
             chars.put("lt", new Character((char) 60));
             chars.put("gt", new Character((char) 62));
             chars.put("nbsp", new Character((char) 160));
             chars.put("iexcl", new Character((char) 161));
             chars.put("cent", new Character((char) 162));
             chars.put("pound", new Character((char) 163));
             chars.put("curren", new Character((char) 164));
             chars.put("yen", new Character((char) 165));
             chars.put("brvbar", new Character((char) 166));
             chars.put("sect", new Character((char) 167));
             chars.put("uml", new Character((char) 168));
             chars.put("copy", new Character((char) 169));
             chars.put("ordf", new Character((char) 170));
             chars.put("laquo", new Character((char) 171));
             chars.put("not", new Character((char) 172));
             chars.put("shy", new Character((char) 173));
             chars.put("reg", new Character((char) 174));
             chars.put("macr", new Character((char) 175));
             chars.put("deg", new Character((char) 176));
             chars.put("plusmn", new Character((char) 177));
             chars.put("sup2", new Character((char) 178));
             chars.put("sup3", new Character((char) 179));
             chars.put("acute", new Character((char) 180));
             chars.put("micro", new Character((char) 181));
             chars.put("para", new Character((char) 182));
             chars.put("middot", new Character((char) 183));
             chars.put("cedil", new Character((char) 184));
             chars.put("sup1", new Character((char) 185));
             chars.put("ordm", new Character((char) 186));
             chars.put("raquo", new Character((char) 187));
             chars.put("frac14", new Character((char) 188));
             chars.put("frac12", new Character((char) 189));
             chars.put("frac34", new Character((char) 190));
             chars.put("iquest", new Character((char) 191));
             chars.put("Agrave", new Character((char) 192));
             chars.put("Aacute", new Character((char) 193));
             chars.put("Acirc", new Character((char) 194));
             chars.put("Atilde", new Character((char) 195));
             chars.put("Auml", new Character((char) 196));
             chars.put("Aring", new Character((char) 197));
             chars.put("AElig", new Character((char) 198));
             chars.put("Ccedil", new Character((char) 199));
             chars.put("Egrave", new Character((char) 200));
             chars.put("Eacute", new Character((char) 201));
             chars.put("Ecirc", new Character((char) 202));
             chars.put("Euml", new Character((char) 203));
             chars.put("Igrave", new Character((char) 204));
             chars.put("Iacute", new Character((char) 205));
             chars.put("Icirc", new Character((char) 206));
             chars.put("Iuml", new Character((char) 207));
             chars.put("ETH", new Character((char) 208));
             chars.put("Ntilde", new Character((char) 209));
             chars.put("Ograve", new Character((char) 210));
             chars.put("Oacute", new Character((char) 211));
             chars.put("Ocirc", new Character((char) 212));
             chars.put("Otilde", new Character((char) 213));
             chars.put("Ouml", new Character((char) 214));
             chars.put("times", new Character((char) 215));
             chars.put("Oslash", new Character((char) 216));
             chars.put("Ugrave", new Character((char) 217));
             chars.put("Uacute", new Character((char) 218));
             chars.put("Ucirc", new Character((char) 219));
             chars.put("Uuml", new Character((char) 220));
             chars.put("Yacute", new Character((char) 221));
             chars.put("THORN", new Character((char) 222));
             chars.put("szlig", new Character((char) 223));
             chars.put("agrave", new Character((char) 224));
             chars.put("aacute", new Character((char) 225));
             chars.put("acirc", new Character((char) 226));
             chars.put("atilde", new Character((char) 227));
             chars.put("auml", new Character((char) 228));
             chars.put("aring", new Character((char) 229));
             chars.put("aelig", new Character((char) 230));
             chars.put("ccedil", new Character((char) 231));
             chars.put("egrave", new Character((char) 232));
             chars.put("eacute", new Character((char) 233));
             chars.put("ecirc", new Character((char) 234));
             chars.put("euml", new Character((char) 235));
             chars.put("igrave", new Character((char) 236));
             chars.put("iacute", new Character((char) 237));
             chars.put("icirc", new Character((char) 238));
             chars.put("iuml", new Character((char) 239));
             chars.put("eth", new Character((char) 240));
             chars.put("ntilde", new Character((char) 241));
             chars.put("ograve", new Character((char) 242));
             chars.put("oacute", new Character((char) 243));
             chars.put("ocirc", new Character((char) 244));
             chars.put("otilde", new Character((char) 245));
             chars.put("ouml", new Character((char) 246));
             chars.put("divide", new Character((char) 247));
             chars.put("oslash", new Character((char) 248));
             chars.put("ugrave", new Character((char) 249));
             chars.put("uacute", new Character((char) 250));
             chars.put("ucirc", new Character((char) 251));
             chars.put("uuml", new Character((char) 252));
             chars.put("yacute", new Character((char) 253));
             chars.put("thorn", new Character((char) 254));
             chars.put("yuml", new Character((char) 255));
         }
     }
 }
