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
 
 import java.io.BufferedWriter;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 
 public class ENode implements Serializable, Comparable<ENode>
 {
 	private static final long serialVersionUID = -3274141668372679156L;
 	
 	private ProbSequence probSequence;
 	private ENode parent;
 	private float level;
 	private String nodeName;
 	private List<ENode> daughters =new ArrayList<ENode>();
 	
 	public void setParent(ENode parent)
 	{
 		this.parent = parent;
 	}
 
 	public void setDaughters(List<ENode> daughters)
 	{
 		this.daughters = daughters;
 	}
 	
 	@Override
 	public int compareTo(ENode o)
 	{
 		return o.getProbSequence().getNumRepresentedSequences() - this.getProbSequence().getNumRepresentedSequences();
 	}
 		
 	public String getNodeName()
 	{
 		return nodeName;
 	}
 	
 	public ProbSequence getProbSequence()
 	{
 		return probSequence;
 	}
 	
 	
 	public void validateNodeAndDaughters() throws Exception
 	{
 		//System.out.println("Validating " + this.nodeName);
 		this.probSequence.validateProbSequence();
 		
		if( ! nodeName.equals(ETree.ROOT_NAME))
		{
			if(  this.parent == null)
				throw new Exception("Parent not defined");
			
			boolean foundSelf =false;
			
			for( ENode enode : this.parent.daughters )
				if( enode == this)
					foundSelf = true;
			
			if( ! foundSelf)
				throw new Exception("Self not found amoung the children of this's parent!");
		}
		else
		{
			if( this.parent != null)
				throw new Exception("Root parent should be null");
		}
		
 		
 		for( ENode d : daughters )
 			d.validateNodeAndDaughters();
 		
		
 		if( daughters.size() > 0)
 		{
 			double sum =0;
 			
 			double aLevel = daughters.get(0).getLevel();
 			
 			for( ENode d : daughters)
 			{
 				if( aLevel != d.level)
 					throw new Exception("Unexpected level");
 				
 				sum += d.getProbSequence().getNumRepresentedSequences();
 			}
 			
 			//todo:  This should never happen
 			if(  Math.abs(sum - this.getProbSequence().getNumRepresentedSequences()) > 0.0000001 )
 				System.out.println( this.nodeName +  " Unexpected # sequences " + sum + " " + this.getProbSequence().getNumRepresentedSequences());
 		}
 		
 	}
 	
 
 	public int getMaxNumberOfSeqsInBranch()
 	{
 		ENode enode=  this;
 		int max = getProbSequence().getNumRepresentedSequences();
 		
 		while( ! enode.getNodeName().equals(ETree.ROOT_NAME))
 		{
 			max = Math.max(enode.getProbSequence().getNumRepresentedSequences(), max);
 			enode = enode.getParent();
 		}
 		
 		return max;
 	}
 	
 	public int getNumOfAllDaughters()
 	{
 		int sum =daughters.size();
 		
 		for( ENode subNode : daughters)
 			sum += subNode.getNumOfAllDaughters();
 		
 		return sum;
 	}
 	
 	/*
 	 * Attempts to merge the other node to thisNode.
 	 * If succesful, daughter nodes are added recursively and true is returned
 	 * If unsuccesful, this node is not altered and false is returned
 	 */
 	public boolean attemptToMergeOtherNodeToThisNode( ENode otherNode ) throws Exception
 	{
 		if( this.level != otherNode.level )
 			throw new Exception("Two nodes must be at same level " + this.level + " " + otherNode.level);
 		
 		ProbSequence possibleSeq = ProbNW.align(this.probSequence, otherNode.probSequence);
 		
 		if( possibleSeq.getAverageDistance() <= this.level)
 		{
 			possibleSeq.setMapCount(this.getProbSequence(), otherNode.getProbSequence());
 			this.setProbSequence(possibleSeq);
 			for( ENode otherDaughter : otherNode.daughters )
 			{
 				boolean merged = false;
 				
 				for( ENode thisDaughter : this.daughters )
 				{
 					if( ! merged)
 					{
 						merged = thisDaughter.attemptToMergeOtherNodeToThisNode(otherDaughter);
 					}
 				}
 				
 				if( ! merged)
 					this.daughters.add(otherDaughter);
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void writeNodeAndDaughters(BufferedWriter writer, boolean detailed) throws Exception
 	{
 		int level = ETree.getIndex(this.level);
 		String tabString ="";
 		
 		for (int x=1; x <= level; x++)
 			tabString += " ";
 		writer.write(tabString +  this.nodeName + " (" + (this.parent == null ? "root" : this.parent.nodeName) + ") level=" + this.level + " " + this.probSequence.getNumRepresentedSequences() + " seqs with " +  this.daughters.size() + " children and score= " + this.probSequence.getAlignmentScore());
 		
 		this.probSequence.writeThisSequenceToText(writer, "\t" + tabString, detailed);
 		
 		for( ENode enode : daughters)
 			enode.writeNodeAndDaughters(writer, detailed);
 		
 	}
 	
 	/*
 	 * Including this node if this node is a tip node
 	 */
 	public List<ENode> getAllNodesAtTips()
 	{
 		List<ENode> list = new ArrayList<ENode>();
 		
 		if( this.daughters.size() == 0 )
 			list.add(this);
 		else
 			this.addTipsOfAllDaughters(list);
 		
 		return list;
 	}
 	
 	private void addTipsOfAllDaughters(List<ENode> list)
 	{
 		for( ENode d :this.daughters )
 		{
 			if( d.daughters.size() == 0  )
 				list.add(d);
 			
 			d.addTipsOfAllDaughters(list);
 		}
 	}
 	
 	public int getNumOfSequencesAtTips()
 	{
 		int sum =0;
 		
 		if( daughters.size() == 0 )
 			sum = this.probSequence.getNumRepresentedSequences();
 		
 		for( ENode subNode : daughters)
 			sum += subNode.getNumOfSequencesAtTips();
 		
 		return sum;
 	}
 
 	public ENode getParent()
 	{
 		return parent;
 	}
 
 	public float getLevel()
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
 	
 	public ENode( ProbSequence probSequence, String name, float level, ENode parent )
 	{
 		this.probSequence = probSequence;
 		this.nodeName = name;
 		this.parent = parent;
 		this.level = level;
 	}
 }
