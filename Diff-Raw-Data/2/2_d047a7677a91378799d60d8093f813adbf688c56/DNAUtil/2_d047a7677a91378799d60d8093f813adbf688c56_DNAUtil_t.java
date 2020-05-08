 package utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Gene;
 import model.Isoform;
 import model.Options;
 
 public class DNAUtil {
 
 	
 	public static String calculateResults(Options o) throws Exception {
 	
 		String gffFilepath = o.gffPath;
 		String fastaFilepath = o.fastaPath;
 		
 		int DEFAULT_MAX_SEQUENCE_SIZE = 25;
 		
 		List<Gene> genes = null;
 		String dnaSequence = null;
 		
 		if(gffFilepath != null && gffFilepath.length() > 0) {
 			genes = DNAFileReader.readGFF(gffFilepath);
 		}
 		
 		if(fastaFilepath != null && fastaFilepath.length() > 0) {
 			long start = System.nanoTime();
 			dnaSequence = DNAFileReader.readFASTA(fastaFilepath);
 			long stop = System.nanoTime();
 			
 			System.out.println("Time: " + (stop - start));
 		}
 		
 		String results = "";
 		
 		if(dnaSequence == null)
 		{
 			results += "DNA Sequence was never initialized. Please make sure a FASTA file has been included.";
 			return results;
 		}
 		
 		//ALL OPTION TYPES
 		//Options will be sanitized by this point
 		//So perform all calculations on the specified values
 		if(o.byUpstream) { //Search in front of each mRNA taking into account + and - (+ search in front / - search after upstream size)
 			int sizeRange = o.upstreamSize;
 			if(o.bySize) { //Search for repeats of specified size
 				int minSize = o.minSize;
 				int maxSize = o.maxSize < 1 ? DEFAULT_MAX_SEQUENCE_SIZE : o.maxSize; //Max size the length of the dna sequence... sure				
 				
 				//TODO Iterate over each mRNA stored in genes and search the
 				//upstream area for repeated sequences greater than minSize and
 				//less than maxSize
 				
 				for(Gene gene : genes)
 				{
 					for(Isoform iso : gene.getIsoforms())
 					{
 						if(iso.getFeature().equals("mRNA"))
 						{
 							StringBuffer dnaSubSequence;
 							if(iso.isForwardStrand())
 							{
 								dnaSubSequence = new StringBuffer(dnaSequence.substring(iso.getStart()-1-sizeRange, iso.getStart()-1));
 							}
 							else
 							{
 								dnaSubSequence = new StringBuffer(dnaSequence.substring(iso.getStart()+1, iso.getStart()+1+sizeRange)).reverse();
 							}
 							SuffixTree tree = new SuffixTree(dnaSubSequence.toString());
 							System.out.println("after suffix tree");
 							ArrayList<SuffixTreeNode> repeats = SuffixTree.findAllRepeatsOfLength(tree, minSize, maxSize);							
 							
 							ArrayList<SuffixTreeNode> leaves = new ArrayList<SuffixTreeNode>();
 							for(SuffixTreeNode node : repeats)
 							{
 								String repeat = SuffixTree.getPathLabel(node);
 								SuffixTree.getLeafNodesFromNode(node, leaves);
 								for(SuffixTreeNode leaf : leaves)
 								{
 									String leafString = SuffixTree.getPathLabel(leaf);
 									int startingPos = dnaSubSequence.length() - leafString.length() + 2;
 									results += "Repeat: " + repeat + ", Size: " + repeat.length() + ", Number of occurrences: " + leaves.size() + ", Starting Position in Original Sequence: " + startingPos + ", Ending Position in Original Sequence: " + (startingPos+repeat.length()-1) + "\n";
 								}
 								leaves.clear();
 							}
 						}
 					}
 				}
 				
 				
 				
 			} else { //Search for repeats by specific string
 				String searchPattern = o.sequence;
 				
 				//TODO Iterate over each mRNA stored in genes and search the
 				//upstream area for repeats that match the searchPattern string
 				
 				for(Gene gene : genes)
 				{
 					for(Isoform iso : gene.getIsoforms())
 					{
 						if(iso.getFeature().equals("mRNA"))
 						{
 							StringBuffer dnaSubSequence;
 							if(iso.isForwardStrand())
 							{
 								dnaSubSequence = new StringBuffer(dnaSequence.substring(iso.getStart()-1-sizeRange, iso.getStart()-1));
 							}
 							else
 							{
 								dnaSubSequence = new StringBuffer(dnaSequence.substring(iso.getStart()+1, iso.getStart()+1+sizeRange)).reverse();
 							}
 							SuffixTree tree = new SuffixTree(dnaSubSequence.toString());
 							System.out.println("after suffix tree");
 							SuffixTreeNode repeatNode = SuffixTree.findRepeats(tree, searchPattern);
 							
 							ArrayList<SuffixTreeNode> leaves = new ArrayList<SuffixTreeNode>();
 							String label = SuffixTree.getPathLabel(repeatNode);
 							SuffixTree.getLeafNodesFromNode(repeatNode, leaves);
 							for(SuffixTreeNode leaf : leaves)
 							{
 								String leafString = SuffixTree.getPathLabel(leaf);
 								int startingPos = dnaSubSequence.length() - leafString.length() + 2;
 								results += "Repeat: " + label + ", Size: " + label.length() + ", Number of occurrences: " + leaves.size() + ", Starting Position in Original Sequence: " + startingPos + ", Ending Position in Original Sequence: " + (startingPos+label.length()-1) + "\n";
 							}
 							leaves.clear();
 						}
 					}
 				}
 				
 				
 			}
 		} else { //Search in the specified range
 			int startPosition = o.start;
 			int stopPosition = o.stop < 1 ? dnaSequence.length() : o.stop;
 			
 			if(o.bySize) { //Search for repeats of specified size
 				int minSize = o.minSize;
 				int maxSize = o.maxSize < 1 ? DEFAULT_MAX_SEQUENCE_SIZE : o.maxSize; //Max size the length of the dna sequence... sure
 				
 				//TODO use a suffix tree and find all repeated sequences
 				//in the specified range that are greater than min size 
 				//and less than maxSize. Range is specified by startPosition and stopPosition
 				String dnaSubSequence = dnaSequence.substring(startPosition-1, stopPosition);
 				SuffixTree tree = new SuffixTree(dnaSubSequence);
 				System.out.println("after suffix tree");
 				System.out.println("Min: " + minSize + ", Max: " + maxSize);
 				ArrayList<SuffixTreeNode> repeats = SuffixTree.findAllRepeatsOfLength(tree, minSize, maxSize);
 		     	
 				ArrayList<SuffixTreeNode> leaves = new ArrayList<SuffixTreeNode>();
 				//int c = 1;
 				for(SuffixTreeNode node : repeats)
 				{
 					String repeat = SuffixTree.getPathLabel(node);
 					SuffixTree.getLeafNodesFromNode(node, leaves);
 					//int count = 1;
 					for(SuffixTreeNode leaf : leaves)
 					{
 						//System.out.println("Repeats: " + repeats.size() + ", Count: " + c + ", Leaves: " + leaves.size() + ", Count: " + count++);
 						String leafString = SuffixTree.getPathLabel(leaf);
 						int startingPos = dnaSubSequence.length() - leafString.length() + 2;
 						results += "Repeat: " + repeat + ", Size: " + repeat.length() + ", Number of occurrences: " + leaves.size() + ", Starting Position in Original Sequence: " + startingPos + ", Ending Position in Original Sequence: " + (startingPos+repeat.length()-1) + "\n";
 					}
 					//c++;
 					leaves.clear();
 				}
 				
 			} else { //Search for repeats by specific string
 				String searchPattern = o.sequence;
 				System.out.println("Start: " + startPosition + ", End: " + stopPosition);
 				//use a suffix tree and find all repeated sequences
 				//in the specified range that are the same as the searchPattern string
 				String dnaSubSequence = dnaSequence.substring(startPosition-1, stopPosition);
 				SuffixTree tree = new SuffixTree(dnaSubSequence);
 				System.out.println("after suffix tree");
 				SuffixTreeNode repeatNode = SuffixTree.findRepeats(tree, searchPattern);
 
 				ArrayList<SuffixTreeNode> leaves = new ArrayList<SuffixTreeNode>();
 				String label = SuffixTree.getPathLabel(repeatNode);
				//System.out.println("Label: " + label.length());
 				SuffixTree.getLeafNodesFromNode(repeatNode, leaves);
 				for(SuffixTreeNode leaf : leaves)
 				{
 					String leafString = SuffixTree.getPathLabel(leaf);
 					int startingPos = dnaSubSequence.length() - leafString.length() + 2;
 					results += "Repeat: " + label + ", Size: " + label.length() + ", Number of occurrences: " + leaves.size() + ", Starting Position in Original Sequence: " + startingPos + ", Ending Position in Original Sequence: " + (startingPos+label.length()-1) + "\n";
 				}
 				leaves.clear();
 			}
 		}
 		
 		return results;
 	}
 	
 }
