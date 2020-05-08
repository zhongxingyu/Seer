 package com.velix.bson.io;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import com.velix.bson.BSON;
 import com.velix.bson.BSONDocument;
 import com.velix.bson.Binary;
 import com.velix.bson.CodeWS;
 import com.velix.bson.ElementType;
 import com.velix.bson.JavascriptCode;
 import com.velix.bson.JavascriptCodeWS;
 import com.velix.bson.ObjectId;
 import com.velix.bson.Symbol;
 import com.velix.bson.Timestamp;
 import com.velix.bson.Binary.SubType;
 import com.velix.jmongo.MongoDocument;
 
 public class BSONDecoder {
 
 	public static <D extends BSONDocument> D decode(BSONInput in, Class<D> clazz)
 			throws IOException {
 		in.readInteger();
 		D document;
 		try {
 			document = clazz.newInstance();
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		byte typeValue;
 		while ((typeValue = (byte) in.read()) != 0) {
 			ElementType elementType = ElementType.valueOf(typeValue);
 			if (null == elementType) {
 				throw new IOException("unkown type value[" + typeValue + "]");
 			}
 			String name = in.readCString();
 			Object element = null;
 			switch (elementType) {
 			case ARRAY:
 				BSONDocument array = decode(in, MongoDocument.class);
 				List<Object> list = new ArrayList<Object>(array.size());
 				for (int i = 0;; i++) {
 					String key = String.valueOf(i);
 					if (array.containsKey(key)) {
 						list.add(array.get(key));
 					} else {
 						break;
 					}
 				}
 				element = list;
 				break;
 			case BINARY:
 				int c = in.readInteger();
 				byte subTypeValue = in.read();
 				Binary.SubType subType = SubType.valueOf(subTypeValue);
				if (null == elementType) {
 					throw new IOException("unkown binay sub type value["
 							+ subTypeValue + "]");
 				}
 				byte[] data = new byte[c];
 				in.read(data);
 				element = new Binary(data, subType);
 				break;
 			case BOOLEAN:
 				element = in.read() > 0;
 				break;
 			case EMBEDDED_DOCUMENT:
 				element = decode(in, BSONDocument.class);
 				break;
 			case FLOATING_POINT:
 				element = Double.longBitsToDouble(in.readLong());
 				break;
 			case INTEGER_32:
 				element = in.readInteger();
 				break;
 			case INTEGER_64:
 				element = in.readLong();
 				break;
 			case JAVASCRIPT_CODE:
 				element = new JavascriptCode(in.readString());
 				break;
 			case JAVASCRIPT_CODE_W_SCOPE:
 				CodeWS codeWS = new CodeWS();
 				in.readInteger();
 				codeWS.setJavascriptCode(in.readString());
 				codeWS.setDocument(decode(in, BSONDocument.class));
 				element = new JavascriptCodeWS(codeWS);
 				break;
 			case MAX_KEY:
 				element = BSON.MAX_KEY;
 				break;
 			case MIN_KEY:
 				element = BSON.MIN_KEY;
 				break;
 			case NULL:
 				element = null;
 				break;
 			case OBJECT_ID:
 				byte[] oidValue = new byte[12];
 				in.read(oidValue);
 				element = new ObjectId(oidValue);
 				break;
 			case REGULAR_EXPRESSION:
 				String pattern = in.readCString();
 				String flags = in.readCString();
 				int f = 0;
 				if (flags.contains("i")) {
 					f |= Pattern.CASE_INSENSITIVE;
 				}
 				if (flags.contains("m")) {
 					f |= Pattern.MULTILINE;
 				}
 				if (flags.contains("s")) {
 					f |= Pattern.DOTALL;
 				}
 				element = Pattern.compile(pattern, f);
 				break;
 			case SYMBOL:
 				element = new Symbol(in.readString());
 				break;
 			case TIMESTAMP:
 				element = new Timestamp(in.readLong());
 				break;
 			case UTC_DATETIME:
 				element = new Date(in.readLong());
 				break;
 			case UTF8_STRING:
 				element = in.readString();
 				break;
 			default:
 				throw new IOException("decode error");
 			}
 			document.put(name, element);
 		}
 		return document;
 	}
 
 }
