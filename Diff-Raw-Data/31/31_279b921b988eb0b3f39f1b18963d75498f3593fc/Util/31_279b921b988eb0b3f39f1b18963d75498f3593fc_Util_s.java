package org.computer.knauss.reqtDiscussion.io;
 
 import java.sql.Date;
 
 import org.computer.knauss.reqtDiscussion.io.util.DateParser;
 
 public class Util {
 
 	public static Date parseDate(String dateString) {
 		return DateParser.getInstance().parseDate(dateString);
 	}
 
 	public static String idsToKey(int[] ids) {
 		StringBuffer sb = new StringBuffer();
 		for (int id : ids) {
 			sb.append(id);
 			sb.append(",");
 		}
 		return sb.substring(0, sb.length() - 1);
 	}
 
 	public static int[] stringToID(String idString) {
 		String[] tokens = idString.substring(1).split(",");
 
 		int[] tmp = new int[tokens.length];
 		for (int i = 0; i < tmp.length; i++) {
 			tmp[i] = Integer.parseInt(tokens[i].trim());
 		}
 		return tmp;
 	}
 }
