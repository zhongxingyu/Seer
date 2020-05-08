 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.services;
 
 /**
  * This class is used to manipulate strings.
  * 
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  * @since 1.1
  */
 public class StringUtilService {
 
 	public String toUnicode(String input) {
 		StringBuffer buf = new StringBuffer(input.length());
 
 		for (int i = 0; i < input.length(); i++) {
 			char ch = input.charAt(i);
 			// Does the char need to be converted to unicode?
 			if ((ch >= 0x0020) && (ch <= 0x007e)) {
 				// No.
 				buf.append(ch);
 			} else {
 				// Yes.
 				buf.append("\\u");
 				// Get hex value of the char.
 				String hex = Integer.toHexString(input.charAt(i) & 0xFFFF);
 				for (int j = 0; j < 4 - hex.length(); j++)
 					// Prepend zeros because unicode requires 4 digits
 					buf.append("0");
 				buf.append(hex.toLowerCase()); // standard unicode format.
 			}
 		}
 
 		return buf.toString();
 	}
 
 //	public String toASCII(String input) {
 //		return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll(
 //				"\\p{InCombiningDiacriticalMarks}+", "");
 //	}
 }
