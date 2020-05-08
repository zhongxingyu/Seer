 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 package net.sf.okapi.filters.openxml.tests;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.filters.openxml.AbstractTranslator;
 
 /**
  * Implements ITranslator and modifies text to be translated to
  * show the tags the translator will see while translating.  This
  * is used in OpenXMLRoundTripTest, so the tags are shown in the
  * orignal file format.
  */
 
public class CodePeekTranslator extends AbstractTranslator {
 	  // extends GenericSkeletonWriter because expandCodeContent is protected
 	public final static int MSWORD=1;
 	public final static int MSEXCEL=2;
 	public final static int MSPOWERPOINT=3;
 	public final static int MSWORDCHART=4; // DWH 4-16-09
 	static final String CONS="BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz";
 	static final String NUM="0123456789";
 	static final String PUNC=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?";
 	static final String PUNCNL=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?";
 	static final String PUNCDNL="- 	`~!#$%^&*()_+[{]}\\;:\",<.>";
 	static final String UPR="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	static final String LWR="abcdefghijklmnopqrstuvwxyz";
 	static final String wordon="<w:r><w:t>";
 	static final String wordoff="</w:t></w:r>";
 	static final String ppon="<a:r><a:t>";
 	static final String ppoff="</a:t></a:r>";
 	static final String lbrac="{";
 	static final String rbrac="}";
 
 	public String translate(TextFragment tf, Logger LOGGER, int nFileType)
 	{
 		String s = tf.getCodedText();
 		String rslt=s,ss="",slow,sss;
 		int i,j,k,len,codenum;
 		char carrot;
 		int nSurroundingCodes=0; // DWH 4-8-09
 		Code code;
 		char ch;
 		try
 		{
 			len = s.length();
 			List<Code> codes = tf.getCodes();
 			if (len>1)
 			{
 				for(i=0;i<len;i++)
 				{
 					ch = s.charAt(i);
 					code = null;
 					switch ( ch )
 					{
 						case TextFragment.MARKER_OPENING:
 							sss = s.substring(i,i+2);
 							codenum = TextFragment.toIndex(s.charAt(++i));
 							code = codes.get(codenum);
 							ss += sss + lbrac + "g" + codenum + ":" + eggspand(code) + rbrac;
 							nSurroundingCodes++;
 							break;
 						case TextFragment.MARKER_CLOSING:
 							sss = s.substring(i,i+2);
 							codenum = TextFragment.toIndex(s.charAt(++i));
 							code = codes.get(codenum);
 							ss += lbrac + "/g" + codenum + ":" + eggspand(code) + rbrac + sss;
 							nSurroundingCodes--;
 							break;
 						case TextFragment.MARKER_ISOLATED:
 							sss = s.substring(i,i+2);
 							codenum = TextFragment.toIndex(s.charAt(++i));
 							code = codes.get(codenum);
 							if (code.getTagType()==TextFragment.TagType.OPENING)
 								nSurroundingCodes++;
 							else if (code.getTagType()==TextFragment.TagType.CLOSING)
 								nSurroundingCodes--;
 							if (nSurroundingCodes>0)
 							{
 								if (nFileType==MSWORD)
 								{
 									ss += sss + "lbrac + x" + codenum + ":" + eggspand(code) + rbrac;
 								}
 								else if (nFileType==MSPOWERPOINT)
 									ss += sss;
 								else
 									ss += sss;								
 							}
 							else
 							{
 								if (nFileType==MSWORD)
 								{
 									if (code.getTagType()==TextFragment.TagType.OPENING)
 										ss += wordon + lbrac + "x" + codenum + ":" + eggspand(code) + rbrac + wordoff + sss;
 									else if (code.getTagType()==TextFragment.TagType.OPENING)
 										ss += sss + wordon + lbrac + "x" + codenum + ":" + eggspand(code) + rbrac + wordoff;
 									else
 										ss += sss + lbrac + "x" + codenum  + ":" + eggspand(code)+ rbrac;
 								}
 								else if (nFileType==MSPOWERPOINT)
 									ss += sss /* + ppon + lbrac + codenum + rbrac + ppoff*/;
 								else
 									ss += sss;
 							}
 							break;
 						case TextFragment.MARKER_SEGMENT:
 							sss = s.substring(i,i+2);
 							codenum = TextFragment.toIndex(s.charAt(++i));
 							code = codes.get(codenum);
 							if (code.getTagType()==TextFragment.TagType.OPENING)
 								nSurroundingCodes++;
 							else if (code.getTagType()==TextFragment.TagType.CLOSING)
 								nSurroundingCodes--;
 							ss += sss /* + lbrac + "y" + codenum + rbrac */;
 							break;
 					}
 					if (code!=null)
 						continue;
 					if (i+2<len && s.substring(i,i+3).equals("---"))
 					{
 						ss += "---";
 						i += 2;
 					}
 					else if (i+1<len && s.substring(i,i+2).equals("--"))
 					{
 						ss += "--";
 						i += 1;				
 					}
 					else
 					{
 						j = hominyOf(s.substring(i),NUM);
 						if (j>0)
 						{
 							ss += s.substring(i,i+j);
 							i += j-1;
 							continue;
 						}
 						j = hominyOf(s.substring(i),CONS);
 						if (j>0)
 						{
 							k = hominyLetters(s.substring(i+j));
 							slow = s.substring(i,i+j).toLowerCase();
 							if (k > -1)
 							{
 								ss += s.substring(i+j,i+j+k);
 								i += k;
 							}
 							ss += slow+"ay";
 							i += j-1;
 							continue;
 						}
 						else
 						{
 							k = hominyLetters(s.substring(i));
 							if (k>0)
 							{
 								ss += s.substring(i,i+k)+"hay";
 								i += k-1;
 							}
 							else
 							{
 								carrot = s.charAt(i);
 								if (carrot=='&') // DWH 4-21-09 handle entities
 								{
 									k = s.indexOf(';', i);
 									if (k>=0 && (k-i<=5 || (k-i<=7 && s.charAt(i+1)=='#')))
 										// entity: leave it alone
 									{
 										ss += s.substring(i,k+1);
 										i += k-i;
 									}
 									else
 										ss += carrot;
 								}
 								else if (TextFragment.isMarker(carrot))
 								{
 									ss += s.substring(i,i+2);
 									i++;
 								}
 								else
 									ss += carrot;
 							}
 						}
 					}			
 				}
 				rslt = ss;
 			}
 		}
 		catch(Throwable e)
 		{
 			LOGGER.log(Level.WARNING,"Tag Translator failed on "+s);
 		}
 		return rslt;
 	}
 	private int hominyOf(String s, String of)
 	{
 		int i=0,len=s.length();
 		char carrot;
 		for(i=0;i<len;i++)
 		{
 			carrot = s.charAt(i);
 			if (of.indexOf(carrot) < 0)
 				break;
 		}
 		return i;
 	}
 	private int hominyLetters(String s)
 	{
 		int i=0,len=s.length();
 		char carrot;
 		for(i=0;i<len;i++)
 		{
 			carrot = s.charAt(i);
 			if (!Character.isLetter(carrot) && carrot!='\'')
 				break;
 		}
 		return i;		
 	}
 	private String eggspand(Code code)
 	{
 		String s,ss="";
 		int len;
 		char carrot;
 		s = expandCodeContent(code, "en-US", 1);
 		len = s.length();
 		for(int i=0; i<len; i++)
 		{
 			carrot = s.charAt(i);
 //			if (carrot=='<')
 //				ss += "&lt;";
 //			else
 				ss += carrot;
 		}
 		return ss;
 	}
 }
