 /*
  * Copyright 2013 Netherlands eScience Center
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.esciencecenter.esalsa.loadbalancer;
 
 import java.util.ArrayList;
 
 import nl.esciencecenter.esalsa.util.Block;
 import nl.esciencecenter.esalsa.util.Distribution;
 import nl.esciencecenter.esalsa.util.Grid;
 import nl.esciencecenter.esalsa.util.Layer;
 import nl.esciencecenter.esalsa.util.Layers;
 import nl.esciencecenter.esalsa.util.Neighbours;
 import nl.esciencecenter.esalsa.util.Set;
 import nl.esciencecenter.esalsa.util.Topology;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * LoadBalancer is used to generate a block distribution for the Parallel Ocean Program (POP). This block distribution is based
  * on the ocean topology, the desired block size, and the desired number of clusters, nodes per cluster, and cores per node.   
  * 
  * @author Jason Maassen <J.Maassen@esciencecenter.nl>
  * @version 1.0
  * @since 1.0
  * 
  */
 public class LoadBalancer {
 	
 	/** Logger used for debugging */
 	private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);
 	
 	/** The number of clusters to use (default = 1) */ 
 	private final int clusters;
 
 	/** The number of nodes to use. */
 	private final int nodes;
 	
 	/** The number of cores to use. */  
 	private final int cores;
 	
 	/** The topology on which the grid is defined */
 	private final Topology topology; 
 	
 	/** The function object used the determine block neighbours. */  
 	private final Neighbours neighbours;
 	
 	/** The grid containing all blocks. */ 
 	private final Grid grid;
 	
 	/** The store for generated layers. */ 
 	private final Layers layers;
 	
 	/** The layer at which the individual blocks are defined. */  
 	private final Layer blockLayer;
 	
 	/** The layer containing a single set with all blocks. */  
 	private final Layer combinedLayer;
 	
 	/** List of all blocks that have been created. */ 
 	private final ArrayList<Block> allBlocks = new ArrayList<Block>();
 		
 	/** Method to use when splitting */
 	private final String splitMethod; 
 	
 	/**
 	 * Create a LoadBalancer for the provided topology. 
 	 * 
 	 * @param layers the store for the layers
 	 * @param neighbours the function object used to determine the block neighbors. 
 	 * @param grid the grid containing all blocks.
 	 * @param blockWidth the width of a block. 
 	 * @param blockHeight the height of a block. 
 	 * @param clusters the desired number of clusters. 
 	 * @param nodes the desired number of nodes.
 	 * @param cores the desired number of cores.
 	 * @throws Exception if the LoadBalancer could not be initialized. 
 	 */
 	public LoadBalancer(Layers layers, Neighbours neighbours, Topology topology, Grid grid, int blockWidth, int blockHeight, 
 			int clusters, int nodes, int cores, String splitMethod) throws Exception {
 
 		this.layers = layers;
 		this.neighbours = neighbours;
 		this.topology = topology;
 		this.grid = grid;
 		
 		this.clusters = clusters;
 		this.nodes = nodes;
 		this.cores = cores;
 
 		this.splitMethod = splitMethod; 
 		
 		// Create two layers here, one containing each block in a separate set, and one containing all blocks in one set.
 		blockLayer = new Layer("BLOCKS");
 		combinedLayer = new Layer("ALL");
 		
 		for (int y=0;y<grid.height;y++) { 
 			for (int x=0;x<grid.width;x++) { 
 			
 				Block b = grid.get(x, y);
 				
 				if (b != null) {					
 					blockLayer.add(new Set(b));
 					allBlocks.add(b);
 				}
 			}
 		}
 		
 		combinedLayer.add(new Set(allBlocks));
 		
 		// Add both layers to the store.  
 		layers.add(combinedLayer);
 		layers.add(blockLayer);
 	}
 
 	/** 
 	 * Split a set into a specified number (<code>parts</code>) of subsets and store these subsets in the <code>output</code> 
 	 * layer.
 	 * 
 	 * @param set the input to split. 
 	 * @param subsets the number of subsets to create. 
 	 * @param output the output layer in which to store the subsets. 
 	 * @throws Exception if the split failed. 
 	 */
 	private void split(Set set, int subsets, Layer output) throws Exception { 
 
 		System.out.println("Splitting set of " + set.size() + " into " + subsets + " subsets");
 		
 		// Creating a subset of size 1 is easy. 
 		if (subsets == 1) { 
 			output.add(new Set(set));
 			return;
 		}
 		
 		// If the set contains (less than) the amount of part we need, 
 		// it is easy to split it.		
 		final int size = set.size();
 		
 		if (size <= subsets) { 
 			for (int i=0;i<size;i++) { 
 				output.add(new Set(set.get(i)));
 			}	
 			
 			return;
 		}
 
 		ArrayList<Set> next = new ArrayList<Set>();
 		
 		Split split = null;
 		
 		if (splitMethod.equalsIgnoreCase("simple")) { 
 			split = new SimpleSplit(set, subsets);
		} else if (splitMethod.equalsIgnoreCase("roughlyrect")) {
 			split = new RoughlyRectangularSplit(set, subsets);
 		} else if (splitMethod.equalsIgnoreCase("search")) {
 			split = new SearchSplit(set, subsets, neighbours);
 		} 
 		
 		if (split == null) { 
 			throw new Exception("Unknown split method: " + splitMethod);
 		}
 		
 		split.split(next);
 
 		output.addAll(next);
 		set.addSubSets(next);
 		
 		
 /*		
 		
 		// Otherwise, we try to split parts into its prime factors.
 		int [] primes = PrimeFactorization.factor(subsets);
 		
 		if (logger.isDebugEnabled()) { 
 			logger.debug("Got primes " + Arrays.toString(primes));
 		}
 				
 		ArrayList<Set> current = new ArrayList<Set>();
 		current.add(set);
 		
 		ArrayList<Set> next = new ArrayList<Set>();
 		
 		for (int i=0;i<primes.length;i++) { 			
 			int prime = primes[i];
 			
 			for (Set s : current) { 
 
 				if (prime <= 3) { 
 					// Splitting into two or three is easy!  
 					new SimpleSplit(s, prime).split(next);
 				} else { 
 					// Splitting into a prime number of parts is hard!
 					new SmartSplit(s, prime).split(next);
 				}
 			}
 
 			// Clear current list, and swap with next.
 			current.clear();
 			ArrayList<Set> tmp = current;
 			current = next;
 			next = tmp;
 		}
 		
 		output.addAll(current);
 		set.addSubSets(current);
 */		
 	}
 	
 	/** 
 	 * Split an existing layer <code>previousLayer</code> into <code>subsets</code> subsets and store these subsets in a new layer 
 	 * <code>newLayer</code>.
 	 * 
 	 * @param previousLayer the name of the existing layer to split. 
 	 * @param newLayer the name of the new layer to create. 
 	 * @param subsets the number of subsets to create. 
 	 * @throws Exception if the subsets could not be created. 
 	 */
 	private void split(String previousLayer, String newLayer, int subsets) throws Exception {
 		
 		if (logger.isDebugEnabled()) {
 			logger.debug("split " + previousLayer + " " + newLayer + " " + subsets);
 		}
 		
 		Layer current = layers.get(previousLayer);		
 		Layer result = new Layer(newLayer);
 		
 		for (int i=0;i<current.size();i++) { 
 			split(current.get(i), subsets, result);
 		}
 		
 		layers.add(result);
 	}
 	
 	/** 
 	 * Print statistics on work distribution and communication in <code>layer</code> to console. 
 	 *   
 	 * @param layer the layer to print statistics for. 
 	 */
 	private void printStatistics(Layer layer) { 
 		
 		if (layer == null) { 
 			return;
 		}
 
 		System.out.println("Statistics for layer: " + layer.name);		
 		System.out.println("  Sets: " + layer.size());
 	
 		for (int i=0;i<layer.size();i++) { 
 			Set tmp = layer.get(i);
 			System.out.println("   " + i + " (" + tmp.minX + "," + tmp.minY + ") - (" + tmp.maxX + "," + tmp.maxY + ") " + tmp.size() + " " + tmp.getCommunication(neighbours));
 		}
 	}
 
 	/** 
 	 * Print statistics on work distribution and communication in <code>layer</code> to console. 
 	 *   
 	 * @param layer the name of the layer to print statistics for, or <code>ALL</code> to print statistics on all layers.  
 	 */
 	public void printStatistics(String layer) throws Exception {
 		
 		if (layer.equalsIgnoreCase("ALL")) { 
 			printStatistics(layers.get("CLUSTERS"));
 			printStatistics(layers.get("NODES"));
 			printStatistics(layers.get("CORES"));
 		} else { 
 			Layer l = layers.get(layer);
 
 			if (l == null) { 
 				throw new Exception("Layer " + layer + " not found!");
 			}
 			
 			printStatistics(l);
 		}
 	}
 	
 	/** 
 	 * Mark all blocks in a set with a certain value.
 	 * 
 	 * @param set the set containg the blocks to mark.
 	 * @param value the value to mark the blocks with. 
 	 */
 	private void markAll(Set set, int value) { 
 		for (Block b : set) { 
 			b.setMark(value);
 		}
 	}
 	
 	/** 
 	 * Assign work to cores by recursively marking the sets in the various layers. 
 	 * 
 	 * @param set the set containing the blocks to mark. 
 	 * @return the number of new subsets encountered. 
 	 */
 	private int divideWork(Set set) { 
 		
 		if (set.countSubSets() == 0) { 
 			return 1;
 		}
 		
 		int count = 0;
 		
 		for (Set sub : set.getSubSets()) { 
 		
 			for (Block b : sub) { 
 				b.addToMark(count);
 			}
 
 			count += divideWork(sub);
 		}
 
 		return count;
 	} 
 	
 	/** 
 	 * Assign work to cores by recursively marking the sets in the various layers. 
 	 * 
 	 * This algorithm works as follows:
 	 * 
 	 * - Initially all blocks in the combined layer are marked with 0. 
 	 * - Next, we call {@link #divideWork(Set)} to assign all work in the subsets of the combined set. 
 	 *   
 	 * The {@link #divideWork(Set)} then recursively counts the number of subsets of the combined set, adding the number of 
 	 * subsets it has seen so far to the mark of each of the blocks in the subsets. 
 	 * 
 	 * As a result:
 	 * 
 	 * - all blocks in a CORE set will end up with the same mark
 	 * - all blocks in NODE set will have marks ranging from (X .. X+coresPerNode),
 	 * - all blocks in a CLUSTER set will have marks ranging from (Y ... Y+(corePerNode*nodesPerCLuster))          
 	 *   
 	 */
 	private void divideWork() { 
 		markAll(combinedLayer.get(0), 0);
 		
 		if (layers.size() == 2) { 
 			// No additional layers have been defined, so directly assign the blocks.
 		
 			int core = 0;
 			
 			for (int y=0;y<grid.height;y++) { 
 				for (int x=0;x<grid.width;x++) { 
 				
 					Block b = grid.get(x, y);
 					
 					if (b != null) {					
 						b.setMark(core++);
 					}
 				}
 			}
 			
 		} else { 
 			divideWork(combinedLayer.get(0));
 		}
 	}
 	
 	/** 
 	 * Distribute the available blocks over the cores, taking the topology, desired block size, number of cores per node and 
 	 * nodes per cluster into account.
 	 *
 	 * @return the generated distribution.
 	 * @throws Exception the blocks could not be distributed.
 	 */
 	public Distribution split() throws Exception {
 		
 		String prev = "ALL";
 		
 		if (clusters > 1) {
 			split(prev, "CLUSTERS", clusters);
 			prev = "CLUSTERS";
 		}
 		
 		if (nodes > 1) {
 			split(prev, "NODES", nodes);
 			prev = "NODES";
 		}
 		
 		if (cores >= 1) { 
 			split(prev, "CORES", cores);
 		}
 		
 		divideWork();
 		
 		Layer layer = layers.get("CORES");
 				
 		if (layer == null || layer.size() != (cores*nodes*clusters)) { 
 			throw new Exception("INTERNAL ERROR: Failed to retrieve CORE layer with " + (cores*nodes*clusters) + " cores! (" 
 					+ (layer == null ? "NULL" : "" + layer.size()) + ")");
 		}
 		
 		int [] result = new int[grid.width*grid.height];
 		int maxBlocksPerCore = 0;
 		int minBlocksPerCore = Integer.MAX_VALUE;
 		
 		for (Set s : layer) { 
 
 			int blocks = s.size();
 			
 			if (blocks > maxBlocksPerCore) { 
 				maxBlocksPerCore = blocks;
 			}
 
 			if (blocks < minBlocksPerCore) { 
 				minBlocksPerCore = blocks;
 			}
 			
 			for (Block b : s) { 
 				result[b.coordinate.y * grid.width + b.coordinate.x] = b.getMark()+1; 
 			}
 		}
 
 		return new Distribution(topology.width, topology.height, 
 				grid.blockWidth, grid.blockHeight, 
 				clusters, nodes, cores, 
 				minBlocksPerCore, maxBlocksPerCore, 
 				grid.width * grid.height, result);		
 	}
 }
