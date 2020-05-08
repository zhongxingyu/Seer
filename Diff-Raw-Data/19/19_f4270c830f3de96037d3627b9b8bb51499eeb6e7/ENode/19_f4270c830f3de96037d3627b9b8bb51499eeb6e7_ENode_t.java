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
 import java.util.Iterator;
 import java.util.List;
 
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 
 public class ENode implements Serializable, Comparable<ENode>
 {
 	private static final long serialVersionUID = -3274141668372679156L;
 	
 	private ProbSequence probSequence;
 	private ENode parent;
 	private double level;
 	private String nodeName;
 	private List<ENode> daughters =new ArrayList<ENode>();
 	private boolean markedForDeletion = false;
 	private int numChoices =0;
 	
 	public int getNumChoices()
 	{
 		return numChoices;
 	}
 	
 	public void incrementNumChoices()
 	{
 		numChoices++;
 	}
 	
 	public void incrementNumChoices(int numberToAdd)
 	{
 		this.numChoices += numberToAdd;
 	}
 	
 	public void setMarkedForDeletion(boolean markedForDeletion)
 	{
 		this.markedForDeletion = markedForDeletion;
 	}
 	
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
 	
 	public boolean isMarkedForDeletion()
 	{
 		return markedForDeletion;
 	}
 	
 	public String getNodeName()
 	{
 		return nodeName;
 	}
 	
 	public ProbSequence getProbSequence()
 	{
 		return probSequence;
 	}
 	
 	/*
 	 * Wildly un-thread safe!
 	 */
 	public int attemptDaughterMerge() throws Exception
 	{
 		int numMerged=0;
 		
 		for(int x=0; x < this.daughters.size()-1; x++)
 		{
 			ENode xNode = this.daughters.get(x);
 			//System.out.println("Attempting daughter merge for " + xNode.level + " " + xNode.nodeName);
 			
 			if( ! xNode.markedForDeletion)  for( int y=x+1; y < this.daughters.size(); y++)
 			{
 				ENode yNode = this.daughters.get(y);
 				
 				if( ! yNode.markedForDeletion)
 				{
 					ProbSequence possibleAlignment = ProbNW.align(xNode.getProbSequence(), yNode.getProbSequence());
 					
 					if( possibleAlignment.getAverageDistance() <= xNode.getLevel() )
 					{
 						numMerged++;
 						yNode.markedForDeletion = true;
 						xNode.daughters.addAll(yNode.daughters);
 						xNode.incrementNumChoices(yNode.getNumChoices()); 
 						possibleAlignment.setMapCount(xNode.getProbSequence(), yNode.getProbSequence());
 						xNode.setProbSequence(possibleAlignment);
 					}
 				}
 			}
 		}
 		
 		for( ENode d: this.daughters)
 			numMerged += d.attemptDaughterMerge();
 		
 		for( Iterator<ENode> i = this.daughters.iterator(); i.hasNext(); )
 		{
 			if( i.next().markedForDeletion)
 				i.remove();
 		}
 		
 		return numMerged;
 	}
 
 	public void validateNodeAndDaughters() throws Exception
 	{
 		System.out.println("Validating " + this.nodeName);
 		this.probSequence.validateProbSequence();
 		
 		if( markedForDeletion)
 			throw new Exception("Node should be deleted " + this.nodeName);
 		
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
 			
 
 			if(  Math.abs(sum - this.getProbSequence().getNumRepresentedSequences()) > 0.0000001 )
 				throw new Exception( this.nodeName +  " Unexpected # sequences " + sum + " " + this.getProbSequence().getNumRepresentedSequences());
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
 	
 
 	public int getMaxNumberOfChoicesInBranch()
 	{
 		ENode enode=  this;
 		int max = enode.getNumChoices();
 		
 		while( ! enode.getNodeName().equals(ETree.ROOT_NAME))
 		{
 			max = Math.max(enode.getNumChoices(), max);
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
 		this.addTipsOfAllDaughters(list);
		
		if( this.daughters.size() == 0 )
			list.add(this);
		
 		return list;
 	}
 	
 	private void addTipsOfAllDaughters(List<ENode> list)
 	{
 		for( ENode d :this.daughters )
 		{
 			if( d.daughters.size() == 0)
 				list.add(d);
 			else for ( ENode d2 : this.daughters)
 				d2.addTipsOfAllDaughters(list);
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
