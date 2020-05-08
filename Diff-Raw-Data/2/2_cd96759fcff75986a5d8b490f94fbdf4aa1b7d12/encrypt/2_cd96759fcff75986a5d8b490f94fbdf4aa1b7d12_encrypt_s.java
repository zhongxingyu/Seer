 /**
  ########################################################################
  # Copyright (C) 2013 Panagiotis Kritikakos <panoskrt@gmail.com>   	#
  # 																		#
  # This program is free software: you can redistribute it and/or modify #
  # it under the terms of the GNU General Public License as published by #
  # the Free Software Foundation, either version 3 of the License, or 	#
  # (at your option) any later version. 					#
  # 									#
  # This program is distributed in the hope that it will be useful, 	#
  # but WITHOUT ANY WARRANTY; without even the implied warranty of 	#
  # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 	#
  # GNU General Public License for more details. 			#
  # 									#
  # You should have received a copy of the GNU General Public License 	#
  # along with this program. If not, see <http://www.gnu.org/licenses/>. #
  ########################################################################
  **/
 
package come.panoskrt.javaencrypt;
 
 import java.io.FileInputStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class Encrypt {
 
 	private FileInputStream fis;
 
 	public Encrypt() {
 		// Constructor
 	}
 
 	public String encryptString(String input, String salt, String algorithm) {
 		String output = null;
 		String fullInput = null;
 
 		if (!salt.equals(null)) {
 			fullInput = salt + input;
 		} else {
 			fullInput = input;
 		}
 
 		try {
 			MessageDigest digest = MessageDigest.getInstance(algorithm);
 			digest.update(fullInput.getBytes(), 0, fullInput.length());
 			output = new BigInteger(1, digest.digest()).toString(16);
 		} catch (NoSuchAlgorithmException ex) {
 			ex.printStackTrace();
 		}
 		return output;
 	}
 
 	// Based on: http://www.mkyong.com/java/how-to-generate-a-file-checksum-value-in-java/
 	public String calculateFingerprint(String fileName, String algorithm) {
 		StringBuffer sb = new StringBuffer();
 		try {
 			MessageDigest md = MessageDigest.getInstance(algorithm);
 			fis = new FileInputStream(fileName);
 			byte[] dataBytes = new byte[1024];
 
 			int nread = 0;
 			while ((nread = fis.read(dataBytes)) != -1) {
 				md.update(dataBytes, 0, nread);
 			}
 
 			byte[] mdbytes = md.digest();
 			for (int i = 0; i < mdbytes.length; i++) {
 				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return sb.toString();
 	}
 }
