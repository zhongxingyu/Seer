 /*===========================================================================
   Copyright (C) 2012 by the Okapi Framework contributors
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
 ===========================================================================*/
 
 package net.sf.okapi.lib.verification;
 
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 
 class SpaceChecker {
 
 	/**
 	 * Checks and fixes white spaces for a given text fragment.
 	 * The target fragment passed as a parameter is modified
 	 * @param srcFrag original fragment
 	 * @param trgFrag the fragment to fix
 	 */
 	public void checkSpaces(TextFragment srcFrag, TextFragment trgFrag) {
 
 		if ((!trgFrag.isEmpty()) && (trgFrag.hasCode())) {
 			if (trgFrag.compareTo(srcFrag, true) != 0) {
 				StringBuilder trgText = new StringBuilder(trgFrag.getCodedText());
 				StringBuilder srcText = new StringBuilder(srcFrag.getCodedText());
 				int tCur = 0;
 
 				// Iterate over trgText
 				while (tCur < trgText.length()) {
 					if (TextFragment.isMarker(trgText.charAt(tCur))) {
 						int tIndexBefore = 0;
 						int tIndexAfter = 0;
 
 						if (tCur == 0) {
 							tIndexBefore = tCur;
 						} else {
 							tIndexBefore = tCur - 1;
 						}
 
 						if (tCur >= trgText.length() - 2) {
 							tIndexAfter = trgText.length() - 1;
 						} else {
 							tIndexAfter = tCur + 2;
 						}
 
 						Code tCode = trgFrag.getCode(trgText.charAt(tCur + 1));
 
 						// Search source for matching code
 						int sCur = 0;
 						while (sCur < srcText.length()) {
 							if (TextFragment.isMarker(srcText.charAt(sCur))) {
 								Code sCode = srcFrag.getCode(srcText.charAt(sCur + 1));
 								if ((sCode.getId() == tCode.getId())
 										&& (sCode.getTagType() == tCode.getTagType())) {
 									int sIndexBefore = 0;
 									int sIndexAfter = 0;
 
 									if (sCur == 0) {
 										sIndexBefore = sCur;
 									} else {
 										sIndexBefore = sCur - 1;
 									}
 
 									if (sCur >= srcText.length() - 2) {
 										sIndexAfter = srcText.length() - 1;
 									} else {
 										sIndexAfter = sCur + 2;
 									}
 
 									// fix spaces before tag
 									while (sIndexBefore >= 0) {
 										if (Character.isWhitespace(srcText.charAt(sIndexBefore))) {
											if ((tIndexBefore >= 0)
 													&& (!Character.isWhitespace(trgText.charAt(tIndexBefore)))) {
 												trgText.insert(tIndexBefore + 1, srcText.charAt(sIndexBefore));
 												tCur += 1;
 												tIndexAfter += 1;
 											} else if (tIndexBefore >= 0) {
 												if (tIndexBefore > 0)
 													tIndexBefore -= 1;
 												else
 													break;
 											}
 											sIndexBefore -= 1;
 										} else {
 											// check target
 											while (tIndexBefore >= 0) {
 												if (Character.isWhitespace(trgText.charAt(tIndexBefore))) {
 													trgText.deleteCharAt(tIndexBefore);
 													tCur -= 1;
 													tIndexAfter -= 1;
 												} else {
 													break;
 												}
 												tIndexBefore -= 1;
 											}
 											break;
 										}
 									}
 
 									// fix spaces after tag
 									while (sIndexAfter < srcText.length()) {
 										if (Character.isWhitespace(srcText.charAt(sIndexAfter))) {
 											if ((tIndexAfter < trgText.length())
 													&& (!Character.isWhitespace(trgText.charAt(tIndexAfter)))) {
 												trgText.insert(tIndexAfter, srcText.charAt(sIndexAfter));
 												tIndexAfter += 1;
 											} else if (tIndexAfter < trgText.length()) {
 												if (tIndexAfter < trgText.length())
 													tIndexAfter += 1;
 												else
 													break;
 											}
 											sIndexAfter += 1;
 										} else {
 											// check target
 											while (tIndexAfter <= trgText.length()) {
 												if (Character.isWhitespace(trgText.charAt(tIndexAfter))) {
 													trgText.deleteCharAt(tIndexAfter);
 												} else {
 													tIndexAfter += 1;
 													break;
 												}
 											}
 											break;
 										}
 									}
 									// continue to next target tag
 									break;
 								}
 								// skip index character
 								sCur += 1;
 							}
 							// iterate
 							sCur += 1;
 						}
 						// skip index character
 						tCur += 1;
 					}
 					// iterate
 					tCur += 1;
 				}

 				// write fixed string into target
 				trgFrag.setCodedText(trgText.toString(), false);
 
 			} else {
 				// Add code here for handling an exact match
 			}
 		}
 	}
 	
 }
