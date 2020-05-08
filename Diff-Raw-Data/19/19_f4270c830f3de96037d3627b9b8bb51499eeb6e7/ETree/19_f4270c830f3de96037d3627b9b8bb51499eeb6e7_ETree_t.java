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
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import parsers.FastaSequence;
 import parsers.FastaSequenceOneAtATime;
 import parsers.NewRDPNode;
 import parsers.NewRDPParserFileLine;
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 import utils.ConfigReader;
 import utils.ProcessWrapper;
 
 public class ETree implements Serializable
 {
 	private static final long serialVersionUID = 8463272194826212918L;
 	public static final String ROOT_NAME = "root";
 	
 	public static final double[] LEVELS = {0.0, 0.15,0.14,0.13,0.12,0.11,0.10,0.09,0.08,0.07,0.06, 0.05,0.04,0.03,0.02,0.01};
 	private int node_number =1;
 	public static final int RDP_THRESHOLD = 80;	
 	
 	private ENode topNode=null;
 	
 	public void validateTree() throws Exception
 	{
 		this.topNode.validateNodeAndDaughters();
 	}
 	
 	public ENode getTopNode()
 	{
 		return topNode;
 	}
 	
 	public int getTotalNumberOfSequences()
 	{
 		return this.topNode.getNumOfSequencesAtTips();
 	}
 	
 	public void addSequence(ProbSequence probSeq, String sampleName) throws Exception
 	{	
 		if( topNode == null )
 		{
 			initialize(probSeq, sampleName);
 		}
 		else
 		{
 			ProbSequence newSeq = ProbNW.align(topNode.getProbSequence(), probSeq);
 			newSeq.setMapCount(topNode.getProbSequence(), probSeq);
 			topNode.setProbSequence( newSeq);
 			ENode index = addToOrCreateNode(topNode, probSeq, sampleName);
 			
 			while( index != null)
 				index = addToOrCreateNode(index, probSeq, sampleName);
 		}
 	}
 	
 	private void trimSingletonsAtTop(  ) throws Exception
 	{	
 		while(this.topNode.getDaughters().size() ==1)
 		{
 			ENode daugtherNode = this.topNode.getDaughters().get(0);
 			this.topNode.setDaughters(daugtherNode.getDaughters());
 			for(ENode d : this.topNode.getDaughters())
 				d.setParent(this.topNode);
 			System.out.println("Removed level " + daugtherNode.getLevel());
 		}
 	}
 	
 	private List<ENode> deleteTopBranchesAndGetTipBrancesToRerun() throws Exception
 	{
 		trimSingletonsAtTop();
 		
 		List<ENode> tips = getAllNodesAtTips();
 		
 		HashSet<ENode> toRerun = new HashSet<ENode>();
 		
 		for( int x=0; x < tips.size()-1; x++)
 		{
 			ENode xNode = tips.get(x);
 			if( ! xNode.isMarkedForDeletion())
 			{
 				for( int y=x+1; y < tips.size(); y++)
 				{
 					ENode yNode = tips.get(y);
 					
 					if( ! yNode.isMarkedForDeletion()
 							&& ! xNode.getParent().equals(yNode))
 					{
 						ProbSequence align = ProbNW.align(xNode.getProbSequence(), yNode.getProbSequence());
 						if( align.getAverageDistance() < xNode.getLevel())
 						{
 							ENode aNode = xNode;
 							
 							if( yNode.getMaxNumberOfSeqsInBranch()< 
 									xNode.getMaxNumberOfSeqsInBranch()) 
 								aNode = yNode;
 							
 							toRerun.add(aNode);
 							
 							ENode lastNode = aNode;
 							while( ! aNode.getNodeName().equals(ROOT_NAME))
 							{
 								aNode.setMarkedForDeletion(true);
 								lastNode = aNode;
 								aNode = aNode.getParent();
 							}
 							
 							extendDeletionDown(lastNode, toRerun);
 						}
 					}
 				}
 			}	
 		}
 		
 		for( ENode node : this.topNode.getDaughters())
 			extendDeletionDown(node, toRerun);
 		
 		for( Iterator<ENode> i= this.topNode.getDaughters().iterator(); i.hasNext();  )
 			if( i.next().isMarkedForDeletion() )
 			{
 				i.remove();
 			}
 		
 		List<ENode> list = new ArrayList<ENode>(toRerun);
 				
 		Collections.sort(list);
 		return list;
 	}
 	
 	private void extendDeletionDown(ENode enode, HashSet<ENode> marked)
 	{
 		if( ! enode.isMarkedForDeletion() ) 
 			return;
 		
 		for( ENode d : enode.getDaughters() )
 		{
 			d.setMarkedForDeletion(true);
 		}
 		
 		for( ENode d : enode.getDaughters())
 		{
 			if( d.getDaughters().size() == 0 )
 				marked.add(d);
 		}
 		
 		for( ENode d : enode.getDaughters())
 		{
 			extendDeletionDown(d, marked);
 		}
 	}
 	
 	/*
 	 * wildly unthread safe.
 	 * 
 	 * 
 	 */
 	public void attemptRerunOfErrorsAtTips() throws Exception
 	{
 		System.out.println("pre-tri with " + this.topNode.getDaughters().size() + " nodes ");
 		List<ENode> toRerun = deleteTopBranchesAndGetTipBrancesToRerun();
 		System.out.println("post-tri with " + this.topNode.getDaughters().size() + " nodes ");
 		
 		System.out.println("Attempting to re-add " + toRerun.size() + " nodes ");
 		
 		for(ENode node : toRerun)
 		{
 			System.out.println("Adding " + node.getNodeName());
 			ENode index = addToOrCreateNode(topNode, node.getProbSequence(), "NEW_" +  node.getNodeName() +"_");
 			
 			while( index != null)
 				index = addToOrCreateNode(index, node.getProbSequence(), "NEW_" + node.getNodeName()+ "_");
 		}
 	}
 	
 	private static int getLeastCommonDistance(  ENode aNode, ENode anotherNode)
 		throws Exception
 	{
 		if( aNode.getLevel() != anotherNode.getLevel())
 			throw new Exception("Logic error");
 		
 		int x=0;
 		
 		while( ! aNode.getParent().getNodeName().equals(anotherNode.getParent().getNodeName()))
 		{
 			aNode = aNode.getParent();
 			anotherNode = anotherNode.getParent();
 			x++;
 		}
 		
 		return x;
 	}
 	
 	public void writePairedNodeInformation( String filepath ) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filepath)));
 		
 		writer.write("nodeA\tnodeB\tparentA\tparentB\tnodeLevel\tsameParents\tleastCommonDistnace\talignmentDistance\talignmentDistanceMinusPredictedDistance\tnumSequencesA\tnumSequencesB\tmaxTreeSeqsA\tmaxTreeSeqsB\ttotalSeqs\t");
 		writer.write("numChoicesA\tnumChoicesB\tmaxNumberChoicesA\tmaxNumberChoicesB\n");
 		
 		List<ENode> list = getAllNodes();
 		
 		for( int x=0; x < list.size()-1; x++)
 		{
 			System.out.println("Writing outer node " + x + " Of " + list.size());
 			ENode aNode = list.get(x);
 			
 			if( aNode.getParent() != null && ! aNode.getParent().getNodeName().equals(ROOT_NAME)) 
 				for( int y=x+1; y <list.size(); y++)
 				{
 					ENode bNode = list.get(y);
 					
 					if( bNode.getParent() != null && ! bNode.getParent().getNodeName().equals(ROOT_NAME) &&
 							aNode.getLevel() == bNode.getLevel()) 
 						{
 							writer.write( aNode.getNodeName() + "\t" );
 							writer.write( bNode.getNodeName() + "\t" );
 							writer.write( aNode.getParent().getNodeName() + "\t");
 							writer.write( bNode.getParent().getNodeName() + "\t");
 							writer.write( aNode.getLevel()+ "\t");
 							writer.write(aNode.getParent().getNodeName().equals(bNode.getParent().getNodeName()) + "\t");
 							writer.write( getLeastCommonDistance(aNode, bNode) + "\t");
 							ProbSequence probSeq = ProbNW.align(aNode.getProbSequence(), bNode.getProbSequence());
 							writer.write(probSeq.getAverageDistance() + "\t");
 							writer.write((probSeq.getAverageDistance() - aNode.getLevel()) + "\t" );
 							writer.write( aNode.getProbSequence().getNumRepresentedSequences() + "\t");
 							writer.write( bNode.getProbSequence().getNumRepresentedSequences() + "\t");
 							writer.write( aNode.getMaxNumberOfSeqsInBranch() + "\t");
 							writer.write( bNode.getMaxNumberOfSeqsInBranch()+ "\t");
 							
 							int totalNum = aNode.getProbSequence().getNumRepresentedSequences() + 
 									bNode.getProbSequence().getNumRepresentedSequences();
 							writer.write(totalNum + "\t");
 							writer.write(aNode.getNumChoices() + "\t");
 							writer.write(bNode.getNumChoices() + "\t");
 							writer.write(aNode.getMaxNumberOfChoicesInBranch() + "\t");
 							writer.write(bNode.getMaxNumberOfChoicesInBranch() + "\n");
 						}
 				}
 			writer.flush();
 		}
 		
 		writer.flush();  writer.close();
 	}
 	
 	public List<ENode> getAllNodes()
 	{
 		List<ENode> list = new ArrayList<ENode>();
 		addNodeAndDaughters(this.topNode, list);
 		return list;
 	}
 	
 	public List<ENode> getAllNodesAtTips()
 	{
 		List<ENode> allNodes = getAllNodes();
 		
 		for( Iterator<ENode> i = allNodes.iterator(); i.hasNext();)
 			if( i.next().getDaughters().size() >0)
 				i.remove();
 				
 		return allNodes;
 	}
 	
 	private void addNodeAndDaughters(ENode node, List<ENode> list)
 	{
 		list.add(node);
 		
 		for( ENode d : node.getDaughters())
 			addNodeAndDaughters(d, list);
 	}
 	
 	public static int getIndex(double level) throws Exception
 	{
 		for( int i =0; i < LEVELS.length; i++)
 			if( LEVELS[i] == level)
 				return i;
 		
 		throw new Exception("Could not find " + level);
 	}
 	
 	public void mergeAllDaughters() throws Exception
 	{
 		
 		while( true)
 		{
 			int numMerged = this.topNode.attemptDaughterMerge();
 			
 			System.out.println("Merged " + numMerged + " with total nodes " + getAllNodes().size());
 			
 			if(numMerged == 0 )
 				return;
 		}
 	}
 	
 	public void addOtherTree(ETree otherTree) throws Exception
 	{
 		ProbSequence newSeq = ProbNW.align(topNode.getProbSequence(), otherTree.topNode.getProbSequence());
 		newSeq.setMapCount(topNode.getProbSequence(), otherTree.topNode.getProbSequence());
 		topNode.setProbSequence( newSeq);
 		
 		for( ENode otherNode : otherTree.getTopNode().getDaughters() )
 		{
 			boolean merged = false;
 			
 			for( ENode thisNode : this.getTopNode().getDaughters())
 			{
 				if( !merged)
 					merged = thisNode.attemptToMergeOtherNodeToThisNode(otherNode);
 			}
 			
 			if( ! merged)
 				this.topNode.getDaughters().add(otherNode);
 		}
 	}
 	
 	private ENode getBestMatchToTips(ProbSequence querySequence, List<ENode> targets) throws Exception
 	{
 		if( targets.size() < 2)
 			throw new Exception("Logic error: getBestMatchToTip called on list of size " + targets.size());
 		
 		ENode node = null;
 		Double maxVal = null;
 		
 		for( ENode target : targets )
 		{
 			List<ENode> tips = target.getAllNodesAtTips();
 			
 			for( ENode tip : tips )
 			{
 				ProbSequence alignment = ProbNW.align(querySequence, tip.getProbSequence());
 				
 				double distance = alignment.getAverageDistance();
 				if( node == null ||  distance < maxVal.doubleValue())
 				{
 					node = target;
 					maxVal = distance;
 				}
 			}
 		}
 		
 		return node;
 	}
 
 	private ENode addToOrCreateNode( ENode parent , ProbSequence newSeq, String nodePrefixName) throws Exception
 	{
 		if( parent.getDaughters().size() == 0 )
 			return null;
 		
 		List<ENode> targetList =new ArrayList<ENode>();
 		
 		for( ENode node : parent.getDaughters() )
 		{
 			ProbSequence possibleAlignment= ProbNW.align(node.getProbSequence(), newSeq);
 			//System.out.println( possibleAlignment.getSumDistance()  + "  " + node.getLevel()  );
 			if( possibleAlignment.getAverageDistance() <= node.getLevel())
 			{
 				targetList.add(node);
 			}
 			
 		}
 		
 		if( targetList.size() > 0 )
 		{
 			parent.incrementNumChoices( targetList.size() -1);
 			ENode chosenNode =  ( targetList.size() == 1 ? targetList.get(0) : getBestMatchToTips(newSeq, targetList));
 			ProbSequence chosenSequence= ProbSequence.makeDeepCopy(newSeq);
 			chosenSequence.setMapCount(chosenNode.getProbSequence(), newSeq);
 			chosenNode.setProbSequence(chosenSequence);
 			return chosenNode;
 		}
 		
 		
 		// still here - no matches - add a new node
 		newSeq = ProbSequence.makeDeepCopy(newSeq);
 		ENode newNode = new ENode(newSeq, nodePrefixName +node_number++ + "_",  parent.getDaughters().get(0).getLevel(), parent);
 		parent.getDaughters().add(newNode);
 		int index = getIndex(newNode.getLevel());
 		
 		for( int x=index +1; x < LEVELS.length; x++)
 		{
 			ENode previousNode =newNode;
 			newSeq = ProbSequence.makeDeepCopy(newSeq);
 			newNode = new ENode( newSeq, nodePrefixName + node_number++ + "_", LEVELS[x], previousNode);
 			previousNode.getDaughters().add(newNode);
 		}
 		
 		return null;
 	}
 	
 	public ETree()
 	{
 		
 	}
 	
 	private void initialize(ProbSequence aSeq, String sampleName)
 		throws Exception
 	{
 		this.topNode = new ENode(aSeq, ROOT_NAME, LEVELS[0], null);
 		ENode lastNode = topNode;
 		
 		for( int x=1; x < LEVELS.length; x++)
 		{
 			aSeq = ProbSequence.makeDeepCopy(aSeq);
 			ENode nextNode = new ENode(aSeq, sampleName + node_number++ , LEVELS[x], lastNode);
 			lastNode.getDaughters().add(nextNode);
 			lastNode = nextNode;
 		}
 	}
 	
 	public void writeAsXML(String xmlFilePath) throws Exception
 	{
 		writeAsXML(new File(xmlFilePath));
 	}
 	
 	public void writeAsText(String textFile, boolean detailed) throws Exception
 	{
 		writeAsText(new File(textFile), detailed);
 	}
 	
 	public void writeAsText(File textFile, boolean detailed) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
 		
 		this.topNode.writeNodeAndDaughters(writer,detailed);
 		
 		writer.flush();  writer.close();
 	}
 	
 	public void writeNodesInTabularFormat( String outFile) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 		
 		writer.write("nodeName\tlevel\tnumChoices\tmaxNumChoices\tparentName\tnumberSequences\tmaxNumSequences\talignmentScore\t"  +
 		"alignmentScoreAverage\talignmentLength\tconsensusSequence\n");
 		
 		for( ENode node : getAllNodes() )
 		{
 			writer.write(node.getNodeName() + "\t");
 			writer.write(node.getLevel() + "\t");
 			writer.write(node.getNumChoices() + "\t");
 			writer.write(node.getMaxNumberOfChoicesInBranch() + "\t");
 			writer.write((node.getParent() == null ? ROOT_NAME : node.getParent().getNodeName()) + "\t");
 			writer.write( node.getNumOfSequencesAtTips() +"\t" );
 			writer.write(node.getMaxNumberOfSeqsInBranch() + "\t");
 			writer.write(node.getProbSequence().getAlignmentScore() + "\t");
 			writer.write(node.getProbSequence().getAlignmentScoreAveragedByCol() + "\t");
 			writer.write(node.getProbSequence().getColumns().size() + "\t");
 			writer.write(node.getProbSequence().getConsensus() + "\n");
 			
 		}
 		
 		writer.flush();  writer.close();
 	}
 	
 	public void writeAsXML(File xmlFile) throws Exception
 	{
 
 		HashMap<String, NewRDPParserFileLine> rdpMap =  tryForRDPMap();
 			
 		BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile));
 		
 		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		writer.write("<phyloxml xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.phyloxml.org http://www.phyloxml.org/1.10/phyloxml.xsd\" xmlns=\"http://www.phyloxml.org\">\n");
 		writer.write("<phylogeny rooted=\"true\" rerootable=\"false\">\n");
 		
 		addNodeAndDaughtersToXML(this.topNode,writer,0, rdpMap);
 		
 		writer.write("</phylogeny>\n");
 		writer.write("</phyloxml>\n");
 		
 		writer.flush();  writer.close();
 	}
 	
 	public void writeUngappedConsensusSequences(String filePath) throws Exception
 	{
 		writeUngappedConsensusSequences(new File(filePath));
 	}
 	
 	public void writeUngappedConsensusSequences(File outFile) throws Exception
 	{
 		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 		
 		for( ENode node : topNode.getDaughters() )
 			writeUngappedSequenceAndSubSequences(writer, node);
 			
 		writer.close(); writer.close();
 	}
 	
 	
 	public static ETree getEtreeFromFasta(String fastaFilePath, String sampleName) throws Exception
 	{
 		return getEtreeFromFasta(fastaFilePath, sampleName,-1);
 	}
 	
 	public static ETree getEtreeFromFasta(String fastaFilePath, String sampleName, int maxSamples) throws Exception
 	{
 		ETree eTree = new ETree();
 		//System.out.println(sampleName);
 		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(fastaFilePath);
 		
 		int numDone=0;
 		for(FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence())
 			if( fs.isOnlyACGT())
 			if( maxSamples <0 || numDone < maxSamples)
 			{
 				//System.out.println(fs.getFirstTokenOfHeader());
 				int numDereplicatedSamples = 1;
 				
 				try
 				{
 					numDereplicatedSamples = getNumberOfDereplicatedSequences(fs);
 				}
 				catch(Exception ex)
 				{
 					ex.printStackTrace();
 					throw new Exception("Expectng header in the format of >Name_aNum_321 where last # of time is the # of times dereplicated sample is observed");
 				}
 				
 				ProbSequence probSeq = new ProbSequence(fs.getSequence(), numDereplicatedSamples, sampleName);
 				eTree.addSequence(probSeq, sampleName);
 				
 				numDone++;
 				
				//if( numDone % 20 ==0)
 					System.out.println(numDone);
 				
 				//eTree.writeAsText(ConfigReader.getETreeTestDir() + File.separator + "secondTreeAsText_" + numDone +".txt", false);
 			}
 		
 		fsoat.close();
 		
 		return eTree;
 	}
 	
 	private HashMap<String, NewRDPParserFileLine> tryForRDPMap()
 	{
 		try
 		{
 			File seqFile = new File( ConfigReader.getETreeTestDir() + File.separator + "consensusSequences.txt");
 			
 			if( seqFile.exists())
 				seqFile.delete();
 			
 			if( seqFile.exists())
 				throw new Exception("Could not delete " + seqFile.getAbsolutePath());
 			
 			File rdpFile = new File(ConfigReader.getETreeTestDir() + File.separator + "rdpSeqFile.txt");
 			
 			if( rdpFile.exists())
 				rdpFile.delete();
 			
 			if( rdpFile.exists())
 				throw new Exception("Could not delete " + rdpFile.getAbsolutePath());
 			
 			writeUngappedConsensusSequences(seqFile);
 			
 			String[] args = new String[7];
 			
 			args[0] = "java";
 			args[1] = "-jar";
 			args[2] = ConfigReader.getRDPJarPath();
 			args[3] = "-q";
 			args[4] = seqFile.getAbsolutePath();
 			args[5] = "-o";
 			args[6] = rdpFile.getAbsolutePath();
 			
 			new ProcessWrapper(args);
 			
 			return NewRDPParserFileLine.getAsMapFromSingleThread(rdpFile.getAbsolutePath());
 		}
 		catch(Exception ex)
 		{
 			System.out.println("Could not get RDP map");
 			ex.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private void writeUngappedSequenceAndSubSequences( BufferedWriter writer, ENode node) throws Exception
 	{
 		writer.write(">" + node.getNodeName() + "\n");
 		writer.write(node.getProbSequence().getConsensus().replaceAll("-", "") + "\n");
 		
 		for( ENode subNode : node.getDaughters() )
 			writeUngappedSequenceAndSubSequences(writer, subNode);
 	}
 	
 	private void addNodeAndDaughtersToXML( ENode node, BufferedWriter writer, int level, HashMap<String, NewRDPParserFileLine> rdpMap ) throws Exception
 	{
 		
 		String tabString = "";
 		
 		for( int x=0; x <= level; x++ )
 			tabString += "\t";
 		
 		String taxaName = "" + node.getLevel();
 		String rank = null;
 		String commonName = null;
 		//String phylaName = null;
 		
 		if( rdpMap != null )
 		{
 			NewRDPParserFileLine line = rdpMap.get( node.getNodeName() );
 			
 			if( line != null)
 			{
 				NewRDPNode rdpNode = line.getLowestNodeAtThreshold(RDP_THRESHOLD);
 				taxaName = rdpNode.getTaxaName() + " " + taxaName;
 				rank = line.getLowestRankThreshold(RDP_THRESHOLD);
 				commonName = tabString + "\t<common_name>" + line.getSummaryStringNoScore(50, 2)+"</common_name>\n";
 				//NewRDPNode phylaNode = line.getTaxaMap().get(NewRDPParserFileLine.PHYLUM);
 				
 				//if( phylaNode != null)
 				//	phylaName = tabString + "\t<accession>" + phylaNode.getTaxaName() + "</accession>\n";
 			}
 				
 		}
 		
 		writer.write(tabString + "<clade>\n");
 		
 		//if( phylaName != null)
 		//	writer.write(phylaName);
 		writer.write( tabString + "\t<name>" + node.getNodeName() 
 				+ "(" + node.getNumOfSequencesAtTips() + "seqs) level " + node.getLevel() +"</name>\n");
 		
 		if( level > 1 )
 		{
 			double branchLength = LEVELS[level-1] - LEVELS[level];
 			writer.write(tabString + "\t<branch_length>" + branchLength +  "</branch_length>\n");
 		}
 		else
 		{
 			double branchLength = 0.01;
 			writer.write(tabString + "\t<branch_length>" + branchLength +  "</branch_length>\n");
 		}
 		
 		writer.write(tabString + "\t<taxonomy>");
 		// obviously, just a stub at this point
 		
 		
 		writer.write(tabString + "\t<scientific_name>" + taxaName + "(" + rank + ")" +"</scientific_name>\n");
 		
 		if( commonName != null)
 			writer.write(commonName);
 		
 		if( rank != null)
 			writer.write(tabString + "\t<rank>" + rank + "</rank>\n");
 		
 		writer.write(tabString + "\t</taxonomy>\n");
 		
 		level++;
 		
 		for( ENode daughter: node.getDaughters() )
 			addNodeAndDaughtersToXML(daughter, writer, level, rdpMap);
 		
 		writer.write(tabString + "</clade>\n");
 	}
 	
 	static int getNumberOfDereplicatedSequences(FastaSequence fs) throws Exception
 	{
 		StringTokenizer header = new StringTokenizer(fs.getFirstTokenOfHeader(), "_");
 		header.nextToken();
 		header.nextToken();
 		
 		int returnVal = Integer.parseInt(header.nextToken());
 		
 		if( header.hasMoreTokens())
 			throw new Exception("Parsing error");
 		
 		return returnVal;
 	}
 	
 	public void writeAsSerializedObject(String outFilePath) throws Exception
 	{
 		ObjectOutputStream out =new ObjectOutputStream( new GZIPOutputStream(
 				new FileOutputStream(new File(outFilePath))));
 		
 		out.writeObject(this);
 		
 		out.flush(); out.close();
 	}
 	
 	public static ETree readAsSerializedObject( String inFilePath) throws Exception
 	{
 		ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inFilePath)));
 		ETree etree = (ETree) in.readObject();
 		in.close();
 		return etree;
 	}
 	
 	public static void main(String[] args) throws Exception
 	{
 		if( args.length !=1)
 		{
 			System.out.println("Usage Etree inFastaFile");
 			System.exit(1);
 		}
 		
 	}
 }
