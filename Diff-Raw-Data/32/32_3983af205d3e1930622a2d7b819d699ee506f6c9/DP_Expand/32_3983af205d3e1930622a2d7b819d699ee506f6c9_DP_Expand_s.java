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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import dynamicProgramming.DNASubstitutionMatrix;
 import dynamicProgramming.NeedlemanWunsch;
 import dynamicProgramming.PairedAlignment;
 import dynamicProgramming.SubstitutionMatrix;
 
 public class DP_Expand
 {
 	public static final int BANDWITH = 5;
 	public static final float GAP_PENALTY = -0.5f;
 	public static final SubstitutionMatrix MATRIX = new DNASubstitutionMatrix();
 	
 	private final String s1;
 	private final String s2;
 	
 	private final int numAllowedEdits;
 	
 	private boolean paddingLeft = false;
 	
 	private int leftIndex_S1;
 	private int leftIndex_S2;
 	//private int rightIndex_S1;
 	//private int rightIndex_S2;
 	
 	private final boolean wasSuccesful;
 	
 	//gaps and insertions at the beginning or end of a sequence don't count to this total
 	private int numErrors=0;
 	
 	public int getNumErrors()
 	{
 		return numErrors;
 	}
 	
 	public boolean alignmentWasSuccesful()
 	{
 		return wasSuccesful;
 	}
 	
 	private List<IndividualEdit> editList = new ArrayList<IndividualEdit>();
 	
 	// to make this thread safe should return Collections.unmodifiable(editList)
 	// not done here for performance.
 	public List<IndividualEdit> getEditList()
 	{
 		return editList;
 	}
 	
 	public DP_Expand( String s1, String s2, int s1WordIndex, int s2WordIndex, 
 						int wordSize, int numAllowedEdits) throws Exception
 	{
 		this.s1 = s1;
 		this.s2 = s2;
 		this.numAllowedEdits = numAllowedEdits;
 		
 		this.leftIndex_S1 = s1WordIndex;
 		this.leftIndex_S2 = s2WordIndex;
 		//this.rightIndex_S1 = s1WordIndex + wordSize -1;
 		//this.rightIndex_S2 = s2WordIndex + wordSize -1;
 		this.wasSuccesful = expand();
 		
 	}
 	
 	private boolean expand() throws Exception
 	{
 		if( ! expandLeft())
 			return false;
 		
 		Collections.reverse(this.editList);
 		
 		return true;
 	}
 	
 	private boolean expandLeft() throws Exception
 	{
 		this.leftIndex_S1--;
 		this.leftIndex_S2--;
 		
 		if( this.leftIndex_S1 <0 && this.leftIndex_S2 <0)
 			return true;
 		
 		if( leftIndex_S1 >=0 && leftIndex_S2 >=0 && this.s1.charAt(leftIndex_S1) == this.s2.charAt(leftIndex_S2) )
 			return expandLeft();
 		
 		// second sequence is longer - 1 error to open but we can extend this without penalty
 		if( leftIndex_S1 < 0 )
 		{
			editList.add( new IndividualEdit(IndividualEdit.EDIT_TYPE.DELETION, leftIndex_S2, 
 											s2.charAt(leftIndex_S2)));
 			
 			if( ! paddingLeft)
 			{
 				paddingLeft = true;
 				numErrors++;
 			}
 				
 			if( numErrors > numAllowedEdits )
 				return false;
 			else
 				return expandLeft();
 		}
 		
 		// first sequence is longer - 1 error to open but we can extend this without penalty
 		if( leftIndex_S2 < 0 )
 		{
			editList.add( new IndividualEdit(IndividualEdit.EDIT_TYPE.INSERTION, leftIndex_S1, 
					s1.charAt(leftIndex_S1)));
 			
 			if( ! paddingLeft)
 			{
 				paddingLeft = true;
 				numErrors++;
 			}
 
 			if( numErrors > numAllowedEdits )
 				return false;
 			else
 				return expandLeft();
 		}
 		
 		// still here - do NW alignment
 		int leftBoundS1 = Math.max(leftIndex_S1-BANDWITH+1, 0);
 		int leftBoundS2 = Math.max(leftIndex_S2-BANDWITH+1,0);
 		
 		String fragS1 = s1.substring(leftBoundS1, leftIndex_S1+1);
 		String fragS2 = s2.substring(leftBoundS2,leftIndex_S2+1);
 		
 		//System.out.println("In " + fragS1 + " " + leftBoundS1 + " "+ leftIndex_S1);
 		//System.out.println("In " + fragS2 + " " + leftBoundS2 + " " + leftIndex_S2);
 		
 		PairedAlignment pa = NeedlemanWunsch.globalAlignTwoSequences(
 				fragS1, fragS2, 
 				MATRIX, GAP_PENALTY, 100, false);
 		
 		//System.out.println(pa.toString());
 		
 		char c1 = pa.getFirstSequence().charAt(pa.getFirstSequence().length()-1);
 		char c2 = pa.getSecondSequence().charAt(pa.getSecondSequence().length()-1);
 		//System.out.println( c1 + " "+ c2);
 		
 		if(c1 == '-' && c2 == '-')
 			throw new Exception("Alignment error");
 		
 		if( c1 == c2)
 		{
 			return expandLeft();
 		}
 		else if( c1 == '-' )
 		{
 			this.leftIndex_S1++;
 			
 			editList.add(new IndividualEdit(IndividualEdit.EDIT_TYPE.INSERTION,
 					this.leftIndex_S1, this.s2.charAt(leftIndex_S2)));
 			
 			numErrors++;
 		}
 		else if( c2 == '-')
 		{
 			editList.add(new IndividualEdit(IndividualEdit.EDIT_TYPE.DELETION,
 					this.leftIndex_S1, '-'));
 			this.leftIndex_S2++;
 			
 			numErrors++;
 		}
 		else
 		{
 			editList.add(new IndividualEdit(IndividualEdit.EDIT_TYPE.SUBSTITUITION,
 					this.leftIndex_S1, c2));
 			
 			numErrors++;
 		}
 		
 		if( numErrors > numAllowedEdits )
 			return false;
 		else
 			return expandLeft();
 	}
 	
 }
