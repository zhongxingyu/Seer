 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.core.util;
 
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 public final class Validator {
 
 	private final static String IP_RE = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d|[\\*])){3}$";
 	private final static String IP_RE_MASK = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d|[\\*])){2}(\\.([\\d\\w\\*]{1,3}))$";
 	private final static String CLIENT_ID_RE = "^@([0-9]+)$";
 	private final static String MAIL_RE = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 	
 	public static boolean isValidIp(String value) {
 		return Pattern.matches(IP_RE, value);
 	}
 
 	public static boolean isValidSearchIp(String value) {
 		return Pattern.matches(IP_RE_MASK, value);
 	}
 	
 	public static boolean isValidClientId(String value) {
 		return Pattern.matches(CLIENT_ID_RE, value);
 	}
 
 	public static boolean isValidEmail(String value) {
 		return Pattern.matches(MAIL_RE, value);
 	}
 
 	public static boolean isValidPlayerName(String value) {
		if (StringUtils.isEmpty(value) || value.contains(" ")) return false;
		
//		for (int i = 0; i < value.length(); i++)
//			if (!validPlayerNameChar(value.charAt(i)))
//				return false;
 		return true;
 	}
 
 	private static boolean validPlayerNameChar(char c) {
 		// Continuously improve this.
 		return c < 256 && c != ' ';
 	}	
 }
