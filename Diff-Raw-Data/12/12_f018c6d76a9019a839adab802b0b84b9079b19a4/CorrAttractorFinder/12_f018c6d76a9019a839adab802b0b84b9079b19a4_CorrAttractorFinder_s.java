 package caf;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Properties;
 
 import org.apache.commons.math.distribution.NormalDistributionImpl;
 
 import obj.Annotations;
 import obj.Chromosome;
 import obj.DataFile;
 import obj.GeneSet;
 import util.StatOps;
 import worker.Converger;
 import worker.GeneSetMerger;
 import worker.Scheduler;
 
 public class CorrAttractorFinder {
 	private static Properties config;
 	private static String configFile;
 	private static String command;
 	
 	private static int segment = 0;
 	private static int numSegments = 1;
 	private static long jobID;
 	private static int minSize = 10;
 	private static boolean debugging = false;
 	private static boolean rankBased = false;
 	private static String breakPoint = null;
 	private static int maxIter = 100;
 	private static boolean rowNorm = false;
 	private static float zThreshold = -1;
 	private static String convergeMethod = "FIXEDSIZE";
 	
 	private static int bins = 7;
 	private static int splineOrder = 3;
 	
 	private static DataFile ma;
 	private static Annotations annot;
 	private static ArrayList<Chromosome> chrs;
 	
 	static ArrayList<Chromosome> parseChromGenes(String file, ArrayList<String> genes, HashMap<String, Integer> geneMap) throws IOException{
 		ArrayList<Chromosome> chrs = new ArrayList<Chromosome>();
 		Chromosome.setGeneMap(geneMap);
 		Chromosome.setGeneNames(genes);
 		BufferedReader br = new BufferedReader(new FileReader(file));
 		br.readLine(); // first line header
 		
 		String line = br.readLine();
 		while(line != null){
 			//System.out.println(line);
 			String[] tokens = line.split("\t");
 			int nt = tokens.length;
 			Chromosome chr = new Chromosome(tokens[nt-1]);
 			float coord = Float.parseFloat(tokens[5]);
 			boolean strand = tokens[2].equals("(+)");
 			if(chrs.contains(chr)){
 				chrs.get(chrs.indexOf(chr)).addGene(tokens[0], coord, strand);
 			}else{
 				chr.addGene(tokens[0], coord, strand);
 				chrs.add(chr);
 			}
 			line = br.readLine();
 		}
 		return chrs;
 	}
 	
 	private static void SystemConfiguration() throws Exception{
 		String numSegmentsProperty = System.getProperty("SGE_TASK_LAST");
 		if (numSegmentsProperty != null && numSegmentsProperty.length() > 0) {
             try {
                 numSegments = Integer.parseInt(numSegmentsProperty);
             } catch (NumberFormatException nfe) {
                 System.out.println("WARNING: Couldn't parse number of segments property SGE_TASK_LAST=" + numSegmentsProperty + ", use 1");
             }
         }
 		String jobIDProperty = System.getProperty("JOB_ID");
         jobID = System.currentTimeMillis();
         if (jobIDProperty != null && jobIDProperty.length() > 0) {
             try {
                 jobID = Integer.parseInt(jobIDProperty);
             } catch (NumberFormatException nfe) {
                 System.out.println("WARNING: Couldn't parse job id JOB_ID=" + jobIDProperty + ", use current time");
                 jobID = System.currentTimeMillis();
             }
         }else if(numSegments != 1){
         	throw new RuntimeException("No job ID is assigned!!");
         }
         System.out.printf("%-25s%s\n", "Job ID: ",jobID);
 		String segmentProperty = System.getProperty("SGE_TASK_ID");
         if (segmentProperty != null && segmentProperty.length() > 0) {
             try {
                 segment = Integer.parseInt(segmentProperty) - 1;
             } catch (NumberFormatException nfe) {
                 System.out.println("WARNING: Couldn't parse segment property SGE_TASK_ID=" + segmentProperty + ", use 0");
             }
         }
         System.out.println("Total Segments: " + numSegments + "\tThis Segment: " + segment);
         
 		
 	}
 	private static void fileConfiguration()throws Exception{
 		//========Gene Expression==========
 			String confLine = config.getProperty("ge");
 			ma = null;
 			if(confLine == null){
 				throw new RuntimeException("No data file specified!");
 			}
 			//String[] files = confLine.split(",|;");
 			
 			System.out.printf("%-25s%s\n", "Expression Data:", confLine);
 			try {
 				ma = DataFile.parse(confLine);
 				ma.sortProbes();
 		    } catch (Exception e) {
 		    	throw new RuntimeException("ERROR:Problem parsing data file.\n" + e);
 		    }
 		//=======Annotations for dataset 1====================
 		    annot = null;
 		    confLine = config.getProperty("annot");
 		    if(confLine != null){
 		    	System.out.printf("%-25s%s\n", "GeneExp Annotations:", confLine);
 		    	try {
 	                annot = Annotations.parseAnnotations(confLine);
 	            } catch (Exception e) {
 	                throw new RuntimeException("ERROR: Couldn't parse annotations file '" + confLine+ "\n" + e);
 	            }
 		    }
 			
 		//========gene location file========================
 		    chrs = null;
 		    confLine = config.getProperty("gene_location");
 		    if(confLine != null){
 		    	System.out.printf("%-25s%s\n", "Gene Location:", confLine);
 		    	try {
 	                chrs = parseChromGenes(confLine, ma.getProbes(), ma.getRows());
 	            } catch (Exception e) {
 	                throw new RuntimeException("ERROR: Couldn't parse gene location file '" + confLine+ "\n" + e);
 	            }
 		    }
 		    
 		    confLine = config.getProperty("converge_method");
 	    	if (confLine != null && confLine.length() > 0) {
 	    		confLine.toUpperCase();
 	    		if(!confLine.equals("ZSCORE") && !confLine.equals("FIXEDSIZE")){
 	    			System.out.println("WARNING: Couldn't recognize converge method: " + confLine + ", using default = " + convergeMethod);
 	    		}
 	            convergeMethod = confLine;
 	        }
 	    	System.out.printf("%-25s%s\n", "Converge Method:", convergeMethod);
 		    
 		    confLine = config.getProperty("rank_based");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               rankBased = Boolean.parseBoolean(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse whether using rank-based MI: " + confLine + ", defaultly using it.");
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Rank-based correlation:", rankBased);
 	    	
 	    	/*confLine = config.getProperty("fdr");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               fdrThreshold = Double.parseDouble(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse FDR threshold: " + confLine + ", using default = 0.05.");
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "FDR threshold:", fdrThreshold);*/
 	    	
 	    	confLine = config.getProperty("max_iter");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               maxIter = Integer.parseInt(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse Max Iteration: " + confLine + ", using default = 100");
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Max Iteration:", maxIter);
 	    	
 	    	confLine = config.getProperty("row_normalization");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               rowNorm = Boolean.parseBoolean(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse Row Normalization: " + confLine + ", using default = true");
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Row Normalization:", rowNorm);
 	    	
 	    	/*confLine = config.getProperty("corr_threshold");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               corrThreshold = Float.parseFloat(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse Correlation Threshold: " + confLine + ", using default = " + corrThreshold);
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Correlation Threshold:", corrThreshold);
 	    	*/
 	    	confLine = config.getProperty("z_threshold");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               zThreshold = Float.parseFloat(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse Z score Threshold: " + confLine + ", using variable threshold.");
 	            }
 	        }
 	    	if(zThreshold >= 0){
 	    		System.out.printf("%-25s%s\n", "Z Threshold:", zThreshold);
 	    	}
 	    	
 	    	confLine = config.getProperty("min_size");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               minSize = Integer.parseInt(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse minimum attractor size : " + confLine + ", using default = " + minSize);
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Min Size:", minSize);
 	    	
 	    	/*confLine = config.getProperty("attractor_size");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               attractorSize = Integer.parseInt(confLine);
 	            } catch (NumberFormatException nfe) {
 	            	System.out.println("WARNING: Couldn't parse fixed attractor size: " + confLine + ", using default = " + attractorSize);
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Fixed Attractor Size:", attractorSize);
 	    	*/
 	    	
 	    	confLine = config.getProperty("bins");
 	    	if (confLine != null && confLine.length() > 0) {
 	           try {
 	               bins = Integer.parseInt(confLine);
 	           } catch (NumberFormatException nfe) {
 	               System.out.println("WARNING: Couldn't parse number of bins property: " + confLine + ", use 7");
 	           }
 	        }
 	    	
 	    	confLine = config.getProperty("spline_order");
 	    	if (confLine != null && confLine.length() > 0) {
 	            try {
 	               splineOrder = Integer.parseInt(confLine);
 	            } catch (NumberFormatException nfe) {
 	               System.out.println("WARNING: Couldn't parse spline order property: " + confLine + ", use 3");
 	            }
 	        }
 	    	System.out.printf("%-25s%s\n", "Bins:", bins);
 	    	System.out.printf("%-25s%s\n", "Spline Order:", splineOrder);
 	}
 		
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		long tOrigin = System.currentTimeMillis();
 		
 		System.out.println("==Correlation Attractor Finder============Wei-Yi Cheng==wc2302@columbia.edu===========\n");
 			SystemConfiguration();
 		System.out.println("\n===================================================================================\n");
 			
 		if(args.length < 1)	throw new RuntimeException("No configuration file specified!!!");
 		if(args.length < 2) throw new RuntimeException("No command specified!!");
 		
 		if(args.length >= 2){
 			command = args[1];
 			if(command.equalsIgnoreCase("DEBUG")){
 				System.out.println("**** Debugging mode");
 				debugging = true;
 				jobID = Long.parseLong(args[2]);
 				breakPoint = args[3];
 				
 				System.out.printf("%-25s%s\n", "ID:", jobID);
 				System.out.printf("%-25s%s\n", "BreakPoint:", breakPoint);
 			}else if(command.equalsIgnoreCase("CAF")){
 				System.out.println("-- Attractor finding");
 			}else if(command.equalsIgnoreCase("CNV")){
 				System.out.println("-- CNV Finding");
 			}else{
 				throw new RuntimeException("ERROR: Cannot underatand command: " + command);
 			}
 		}
 		
 			configFile = args[0];
 			config = new Properties();
 			config.load(new FileInputStream(configFile));	
 			fileConfiguration();
 		System.out.println("\n===================================================================================\n");
 		
 		GeneSet.setProbeNames(ma.getProbes());
 		GeneSet.setAnnotations(annot);
 		Scheduler scdr = new Scheduler(segment, numSegments, jobID);
 		Converger cvg = new Converger(segment, numSegments, jobID, convergeMethod, maxIter, rankBased);
 		cvg.setAttractorSize(minSize);
 		cvg.setMIParameter(bins, splineOrder);
 		int fold = (int) Math.round(Math.sqrt(numSegments));
 		if(!debugging)
 		{
 			if(rowNorm){
 				ma.normalizeRows();
 			}
 			
 			if(command.equalsIgnoreCase("CAF")){
 				float[][] data = ma.getData();
 				
 				int m = ma.getNumRows();
 				int n = ma.getNumCols();
 				
 				// transform the first data matrix into ranks
 				float[][] val = new float[m][n];
 				if(rankBased){
 					for(int i = 0; i < m; i++){
 						System.arraycopy(StatOps.rank(data[i]), 0, val[i], 0, n);
 					}
 				}else{
 					val = data;
 				}
 				if(zThreshold < 0){
 					cvg.setZThreshold(m);
 					System.out.printf("%-25s%s\n", "Z Threshold:", cvg.getZThreshold());
 				}else{
 					cvg.setZThreshold(zThreshold);
 				}
 				cvg.findAttractor(val, data);
 			}else if(command.equalsIgnoreCase("CNV")){
 				cvg.findCNV(ma, chrs, minSize);
 			}
 			
 			// fold the number of workers to the squre root of the total number of workers
 			scdr.waitTillFinished(0, fold);
 			
 		}
 		
 		ma = null;
 		
 		if(!debugging || breakPoint.equalsIgnoreCase("merge"))
 		{
 			if(segment < fold){
 				GeneSetMerger mg = new GeneSetMerger(segment, fold, jobID);
 				mg.setMinSize(minSize);
 				mg.mergeGeneSets("tmp/" + jobID + "/geneset/", numSegments, false);
 			}else{
 				System.out.println("Job finished. Exit.");
 				System.exit(0);
 			}
 			
 		}
 		if(!debugging  || breakPoint.equalsIgnoreCase("output") || breakPoint.equalsIgnoreCase("merge"))
 		{
 			if(annot != null)GeneSet.setAnnotations(annot);
 			if(scdr.allFinished(fold)|| (breakPoint.equalsIgnoreCase("output") && segment==0)){
 				GeneSetMerger mg = new GeneSetMerger(segment, 1, jobID);
 				mg.setMinSize(minSize);
 				mg.mergeGeneSets("tmp/" + jobID + "/merge" + (GeneSetMerger.mergeCount-1), fold, true);
 			}
 		}
 		
 		
 		System.out.println("Done in " + (System.currentTimeMillis() - tOrigin) + " msecs.");
 		System.out.println("\n====Thank you!!==================================@ Columbia University 2011=======\n");
 	}
 
 }
