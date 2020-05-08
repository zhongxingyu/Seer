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
 
 package eTree;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 
 public class ENode implements Serializable
 {
 	
 	
 	private static final long serialVersionUID = -3274141668372679156L;
 	
 	private ProbSequence probSequence;
 	private ENode parent;
 	private double level;
 	private String nodeName;
 	private List<ENode> daughters =new ArrayList<ENode>();
 	
 	public String getNodeName()
 	{
 		return nodeName;
 	}
 	
 	public ProbSequence getProbSequence()
 	{
 		return probSequence;
 	}
 	
 	public int getNumOfAllDaughters()
 	{
 		int sum =daughters.size();
 		
 		for( ENode subNode : daughters)
 			sum += subNode.getNumOfAllDaughters();
 		
 		return sum;
 	}
 	
 	/*
	 * Attempts to merge this node to otherNode.
 	 * If succesful, daughter nodes are added recursively and true is returned
 	 * If unsuccesful, this node is not altered and false is returned
 	 */
	public boolean attemptMergeToOtherNode( ENode otherNode ) throws Exception
 	{
 		if( this.level != otherNode.level )
 			throw new Exception("Two nodes must be at same level " + this.level + " " + otherNode.level);
 		
 		ProbSequence possibleSeq = ProbNW.align(this.probSequence, otherNode.probSequence);
 		
 		if( possibleSeq.getAverageDistance() <= this.level)
 		{
 			this.setProbSequence(possibleSeq);
 			for( ENode otherDaughter : otherNode.daughters )
 			{
 				for( ENode thisDaughter : this.daughters )
 				{
					boolean merged = false;
 					if( ! merged)
 					{
						merged = thisDaughter.attemptMergeToOtherNode(otherDaughter);
 					}
					
					if( ! merged)
						this.daughters.add(otherDaughter);
 				}
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public int getNumOfSequencesAtTip()
 	{
 		int sum =0;
 		
 		if( daughters.size() == 0 )
 			sum = this.probSequence.getNumRepresentedSequences();
 		
 		for( ENode subNode : daughters)
 			sum += subNode.getNumOfSequencesAtTip();
 		
 		return sum;
 	}
 
 	public ENode getParent()
 	{
 		return parent;
 	}
 
 	public double getLevel()
 	{
 		return level;
 	}
 	
 	public void setProbSequence(ProbSequence probSequence)
 	{
 		this.probSequence = probSequence;
 	}
 
 	public List<ENode> getDaughters()
 	{
 		return daughters;
 	}
 	
 	public ENode( ProbSequence probSequence, String name, double level, ENode parent )
 	{
 		this.probSequence = probSequence;
 		this.nodeName = name;
 		this.parent = parent;
 		this.level = level;
 	}
 }
