 /** 
  * Author:  anthony.fodor@gmail.com
  * 
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package reduceOTU;
 
 import java.util.Collections;
 import java.util.List;
 
 import dynamicProgramming.PairedAlignment;
 
 public class ReducedTools
 {
 	/*
 	 * Static factory methods only
 	 */
 	private ReducedTools()
 	{}
 	
 	/*
 	 * Recreate the paired alignment from an input string and edits.
 	 * Note the score in the paired alignment will be undefined
 	 */
 	public static PairedAlignment getAlignment(String s1, List<IndividualEdit> edits)
 		throws Exception
 	{
 		final StringBuffer top = new StringBuffer();
 		final StringBuffer bottom = new StringBuffer();
 		
 		int index=0;
 		
 		for( IndividualEdit ie : edits)
 		{
			if( ie.getPostion() - 1 > index)
			{
				String s = s1.substring(index, ie.getPostion());
				top.append(s);
				bottom.append(s);
			}
 			
 			if(ie.getEditType().equals(IndividualEdit.EDIT_TYPE.SUBSTITUITION))
 			{
 				top.append(s1.charAt(ie.getPostion()));
 				bottom.append(ie.getBase());
 				index = ie.getPostion()+1;
 			}
 			else if( ie.getEditType().equals(IndividualEdit.EDIT_TYPE.DELETION))
 			{
 				top.append(s1.charAt(ie.getPostion()));
 				bottom.append("-");
 				index = ie.getPostion()+1;
 			}
 			else if ( ie.getEditType().equals(IndividualEdit.EDIT_TYPE.INSERTION))
 			{
 				bottom.append( ie.getBase());
 				top.append("-");
				index = ie.getPostion();
 			}
 			else throw new Exception("Illegal type " + ie.getEditType());
 			
 			
 		}
 		
 		if( index < s1.length() -1)
 		{
 			String s= s1.substring(index, s1.length());
 			top.append(s);
 			bottom.append(s);
 		}
 			
 		return new PairedAlignment()
 		{
 			
 			@Override
 			public String getSecondSequence()
 			{
 				return bottom.toString();
 			}
 			
 			@Override
 			public String getFirstSequence()
 			{
 				return top.toString();
 			}
 			
 			@Override
 			public float getAlignmentScore()
 			{
 				return Float.NaN;
 			}
 		};
 	}
 }
