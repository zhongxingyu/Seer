 /*
  *   This file is part of the computer assignment for the Information Retrieval
  *   course at KTH.
  *
  *   First version:  Johan Boye, 2012
  *   Students:       Bernard Hernéndez Pérez, Fernando José Iglesias García, 2012
  *
  *   Date : 17/04/2012.
  */  
 
 import java.util.*;
 import java.io.*;
 
 public class PageRank{
 
     /**************************************************************************************
      *                          PARAMETERS TO CHANGE MANUALLY                             *
      **************************************************************************************/
 
     /**
      * Variable to choose if use the original powerIteration method or the modified by us.
      */
     boolean original = false;
     
     /**
      * Choose the number of first users we want to show.
      */
     Integer nPositionsShow = 10;
     
     /**  
      *   Maximal number of documents. We're assuming here that we don't have more docs than
      *   we can keep in main memory.
      */
     final static int MAX_NUMBER_OF_DOCS = 2000000;
 
     /**
      *   The probability that the surfer  will be  bored,  stop following links, and take a 
      *   random jump somewhere.
      */
     final static double BORED = 0.15;
 
     /**
      *   Convergence criterion: Transition  probabilities do not  change more  that EPSILON 
      *   from one iteration to another.
      */
     final static double EPSILON = 0.00000;
 
     /**
      *   Never do more than this number of iterations  regardless of whether the transistion
      *   probabilities converge or not only for power iteration method.
      */
     final static int MAX_NUMBER_OF_ITERATIONS = 40;
     
     /*************************************************************************************/
     
     /**
      *   The number of documents with no outlinks.
      */
     int numberOfSinks = 0;
     
     /**
      *   Variable to debug.
      */   
     static boolean DEBUG = false;
     
     /**
     *   Variable only followers.
     */
    static boolean onlyFollowers = false;
    
    /**
      * The pageRanks vector used when we created the page rank.
      */
     double[] pi = null;
      
     /**
      *   The number of outlinks from each node.
      */
     int[] out = new int[MAX_NUMBER_OF_DOCS];
     
     /**
      *   Mapping from document names to document numbers.
      */
     Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();
 
     /**
      *   Mapping from document numbers to document names
      */
     String[] docName = new String[MAX_NUMBER_OF_DOCS];
 
     /**  
      *   A memory-efficient representation of the transition  matrix. The  outlinks are 
      *   represented as a Hashtable, whose keys are the numbers of the documents linked 
      *   from.<p>
      *
      *   The value corresponding to key i is a Hashtable whose keys are all the numbers 
      *   of documents j that i links to.<p>
      *
      *   If there are no outlinks from i, then the value corresponding key i is null.
      */
     Hashtable<Integer,Hashtable<Integer,Integer>> link = 
     		                        new Hashtable<Integer,Hashtable<Integer,Integer>>();
 
     
     /**
      *  ArrayList that contains the names of all the followers to later choose if
      *  do the pagerank only taking in account followers, or also another twitter
      *  users.
      */
     static ArrayList<String> followers = new ArrayList<String>();
     
 
     /**
      * Constructor.
      * @param fileGraph : Path to the file which contains the graph.
      * @param filePR : Path to the file where we will save the pageRank.
      * @param method : 0 (PowerIteration) | 1,2,3,4,5 (MonteCarlo i).
      */
     PageRank( String fileGraph, String filePR, int method) {
         int noOfDocs = readDocs(fileGraph);
 	    computePagerank( noOfDocs, method );
         savePageRanks(noOfDocs, filePR);
     }
 
     /**
      *   Reads the documents and creates the docs table. When this method finishes 
      *   executing  then the  @code{out}  vector of  outlinks  is  initialised for 
      *   each doc, and the @code{p} matrix is filled  with  zeroes  (that indicate 
      *   direct links) and NO_LINK (if there  is no direct link. <p>
      *
      *   @return the number of documents read.
      */
     int readDocs( String filename ) {
 	int fileIndex = 0;
 	
 	int numberUsersMoreThanOneRecall = 0;
 	int numberUsersOneRecall = 0;
 	
 	try {
 
         // Reading the file (graph).
 	    System.err.print( "Reading file... " );
 	    BufferedReader in = new BufferedReader( new FileReader( filename ));
 	    String line;
 	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
     		int index = line.indexOf( ";" );           // Position of ;.
     		String title = line.substring( 0, index ); // String from 0 to ;.
     		Integer fromdoc = docNumber.get( title );  // Number for a doc name.
     		
             // If is a previously unseen doc, add it to the table.
    		if (onlyFollowers && !followers.contains(title))
    			continue;
    		else
     			if (fromdoc == null)  {	
     				fromdoc = fileIndex++;
     				docNumber.put( title, fromdoc );
     				docName[fromdoc] = title;
     			}
     		
     		// Check all outlinks.
     		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
     		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
     			String token = tok.nextToken();     
     			index = token.indexOf("(");       
     		    String otherTitle = token.substring(0,index);
     		    Integer nCalls = Integer.parseInt(token.substring(index+1,token.length()-1)); 
     		    Integer otherDoc = docNumber.get( otherTitle );
 
     		    // Avoid users that mentioned themselves.
     		    if (fromdoc == otherDoc) continue;
     		    
     		    // Counting.
     		    if (nCalls>1) numberUsersMoreThanOneRecall++;
     		    else numberUsersOneRecall++;
    		     
                 // If is a previously unseen doc, add it to the table.
    		    if (onlyFollowers && !followers.contains(otherTitle))
    		    	continue;
    		    else
     		    	if (otherDoc == null) {
     		    		otherDoc = fileIndex++;
     		    		docNumber.put( otherTitle, otherDoc );
     		    		docName[otherDoc] = otherTitle;
     		    	}
    		   
     		    // Set probability to 0, means that there is an outlink.
     		    if ( link.get(fromdoc) == null ) 
     			    link.put(fromdoc, new Hashtable<Integer,Integer>());
     		
     		    if ( link.get(fromdoc).get(otherDoc) == null ) {
         			link.get(fromdoc).put( otherDoc, nCalls );
         			out[fromdoc] += nCalls;
     		    }
     		}
 	    }
 
 	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) 
 		    System.err.print( "stopped reading since documents table is full. " );
 	    else 
 		    System.err.print( "done. " );
 	    
 	    // Compute the number of sinks.
 	    for ( int i=0; i<fileIndex; i++ ) {
 		if ( out[i] == 0 )
 		    numberOfSinks++;
 	    }
 	}
 	catch ( FileNotFoundException e ) {
 	    System.err.println( "File " + filename + " not found!" );
 	}
 	catch ( IOException e ) {
 	    System.err.println( "Error reading file " + filename );
 	}
 	System.err.println( "Read " + fileIndex + " number of documents" );
 	System.err.println( "Users " + fileIndex + " -> " + numberUsersOneRecall + "/" 
 	                         + numberUsersMoreThanOneRecall + " (1)/(i).");
 	return fileIndex;
     }
 
     /**
      *   Depending of the value of the variable method we will execute one
      *   of the method learnt in class. When the Algorithm for calculating
      *   the pagerank  finishes we  will show also the first nPositionShow
      *   users ranked in our list.
      *   
      */
     void computePagerank( int N , int method) {
 
         // Call the chosen algorithm.
         switch (method){
         case 0  : pi = powerIteration(N); break;
         case 1  : pi = monteCarlo1(N,N);  break;
         case 4  : pi = monteCarlo4(N, 100); break;
         case 5  : pi = monteCarlo5(N,N);  break;
         default :
 		System.out.println("The Chosen algorithm (" + method + ") doesnt exists!.");
 		System.exit(1);
         }
 
         // Show the best ranked and the sum check.
         showRank(N,pi,method);
     }
 
     /********************************************************************
      *                         POWER ITERATION
      * ******************************************************************
      * Method that calculates the page rank using the powerIteration 
      * algorithm. Is very slow and impossible to use if we have too 
      * much data.
      * 
      * @return the PageRank vector
      */
 	private double[] powerIteration( int N ) {
     
     	// PageRank vector
         double[] pi = new double[N];
     	
         // Check for convergence criterion
     	boolean stop = false;
     	int iterations = 0;
 
     	// Initial probabilities, assume the surfer is in a particular page
     	pi[0] = 1.0;
     	for ( int i = 1 ; i < N ; ++i )
     	    pi[i] = 0.0;
 
     	double[] piNext = new double[N];
 
     	while ( iterations < MAX_NUMBER_OF_ITERATIONS && !stop ) {
 
     	    for ( int i = 0 ; i < N ; ++i ) {
 
         		// Initialize to the sum of the random jump for all the documents
         		piNext[i] = BORED / N;
         		for ( int j = 0 ; j < N ; ++j ) {
         		    Hashtable<Integer, Integer> outlinks = link.get(j);
         		    if ( outlinks == null )
                         piNext[i] += pi[j] * (1-BORED) / N;
                     else  {
                     	if ( outlinks.get(i) != null && outlinks.get(i)>=0 ) {                        
                     		if (original)
                     			piNext[i] += pi[j] * (1-BORED) / outlinks.size();
                     		else {	
                     			piNext[i] += (outlinks.get(i)) * (pi[j] * (1-BORED) / out[j]);	
                     		} // else
                         } // if
                     } // else
     		    } // for
     	    } // for
 
     	    // Finish to iterate?
     	    stop = true;
     	    for ( int i = 0 ; i < N ; ++i )
     		  if ( Math.abs( piNext[i] - pi[i] ) > EPSILON ) {
     		    stop = false;
     		    break;
     		  }
 
     	    // Update the PageRank vector
     	    for ( int i = 0 ; i < N ; ++i ) pi[i] = piNext[i];
 
     	    // -- DEBUG
     	    if (DEBUG) {
     	    	System.out.println("it:" + iterations);
     	    	showRank(N,pi,0);
     	    }
     	    
     	    ++iterations;   	    
     	    
     	} // end while.
 
         // --- DEBUG ---
         if ( DEBUG )
     	    System.err.println(">>>> Power iteration finished after " + 
                                iterations + " iterations");
 
         return pi;
     }
 	/********************************************************************/
 
     /********************************************************************
      *                          MONTECARLO 1
      * ******************************************************************
      * Algorithm 1 described in Avrachenkov's et al. paper.
      * Simulate N runs of a random walk initiated at a randomly chose page. 
      * Evaluate the PageRank for each page as the fraction of random walks
      * which end at that page.
      *
      * @return the PageRank vector
      */
     private double[] monteCarlo1( int N, int nRandomWalks ) {
     
         // PageRank vector
         double[] pi = new double[N];
 
         // # random walks which end at every page
         double[] end = new double[N];
         for ( int i = 0 ; i < N ; ++i ) end[i] = 0;
         
         Random randomGen = new Random();
 
         // Simulate the random walks
         for ( int i = 0 ; i < nRandomWalks ; ++i ) {
         	
         	if ((DEBUG) && ((i%1000)==0)) showRank(N,end,1);
         	
             int initPage = randomGen.nextInt(N); // Choose randomly initial page,
             ++end[ randomWalk(N, initPage) ];    // Increment where the rW ends.  
         }
 
         // Normalize by the number of random walks.
         for (int i = 0 ; i < N ; ++i ) 
             pi[i] = end[i] / (double)nRandomWalks;
   
         // --- DEBUG ---
         if ( DEBUG )
             System.err.println(">>>> Monte Carlo Algorithm 1 finished after "
                     + nRandomWalks + " random walks");
 
         return pi;
     }
     /********************************************************************/
 
     /********************************************************************
      *                          MONTECARLO 4
      * ******************************************************************
      * Algorithm 4 described in Avrachenkov's et al. paper, complete path
      * stopping at dangling nodes.
      * Simulate random walks starting exactly m times from each page. Evaluate 
      * the PageRank for each page as the fraction of visits to that page divided
      * by the total number of visited pages.
      *
      * @return the PageRank vector
      */
     private double[] monteCarlo4( int N, int m ) {
     
         // PageRank vector
         double[] pi = new double[N];
         // # random visits to every page        
         int[] visits = new int[N];
         for ( int i = 0 ; i < N ; ++i ) visits[i] = 0;
         // Total number of visits
         int totalVisits = 0;
         
         // Simulate the random walks
         for ( int i = 0 ; i < m ; ++i )
             for ( int j = 0 ; j < N ; ++j )
                 totalVisits += randomWalk(N, j, visits);
 
         for (int i = 0 ; i < N ; ++i )
             pi[i] = visits[i] / (double)totalVisits;
 
         // --- DEBUG ---
         if ( DEBUG )
             System.err.println(">>>> Monte Carlo Algorithm 4 finished after "
                     + m + " random walks from each page");
 
         return pi;
 
     }
     /********************************************************************/
     
     /********************************************************************
      *                          MONTECARLO 5
      * ******************************************************************
      * Algorithm 5 described in Avrachenkov's et al. paper.
      * Simulate N samples of the random walk started at a random page. For 
      * any page j, evaluate pij as the total  number of  visits to page  i
      * divided by the total number of visited pages.
      *
      * @return the PageRank vector
      */
     private double[] monteCarlo5( int N, int nRandomWalks ) {
     
         // PageRank vector
         double[] pi = new double[N];
 
         // # random walks which end at every page
         double[] visited = new double[N];
         for ( int i = 0 ; i < N ; ++i ) visited[i] = 0;
         
         Random randomGen = new Random();
 
         // Simulate the random walks
         for ( int i = 0 ; i < nRandomWalks ; ++i ) {
         	
         	if ((DEBUG) && ((i%1000)==0)) showRank(N,visited,5);
         	
             int initPage = randomGen.nextInt(N);  // Choose randomly initial page.
             ++visited[ randomWalk(N, initPage) ]; // Increment the page visited.
         }
 
         // Find the total number visits done.
         int totalVisits = 0;
         for (int i = 0 ; i < N ; ++i) 
             totalVisits += visited[i];
 
         // Normalize by the number of total visits done.
         for (int i = 0 ; i < N ; ++i ) 
             pi[i] = visited[i] / (double)totalVisits;
 
         // --- DEBUG ---
         if ( DEBUG )
             System.err.println(">>>> Monte Carlo Algorithm 5 finished after "
                     + nRandomWalks + " random walks");
 
         return pi;
 
     }
     /********************************************************************/
     
     /**
      * Simulation of random walk. 
      *
      * @return the page where the walk ends
      */
     private int randomWalk( int N, int actualPage ) {
     
         // A random walk terminates with probability 1-C
         final double C = 1-BORED;  // same as the one used in powerIteration?
 
         double random;
         boolean terminate = false;
         while ( ! terminate ) {
             
             // Generate a random number to test termination of the random walk
             random = Math.random();
             if ( random < (1 - C) ) {
                 terminate = true;
             } else {
                 // Generate a random number to make the transition to another page
                 random = 1 - Math.random();  // random is in (0, 1]
                 // Cumulative probability of the transitions already explored
                 double cumsum = 0.0;
                 for ( int i = 0 ; i < N ; ++i ) {                	
                 	
                     // Read the probability of going to page i from actualPage
                     double pij = 0.0;
                     Hashtable<Integer, Integer> outlinks = link.get(actualPage);
                     
                     // Set the probabilities.
                     if ( outlinks == null )
                         pij = 1 / (double) N;
                     else 
                     	if ( outlinks.get(i) != null && outlinks.get(i)>=0 )  {                    		
                     		if (original)
                     			pij = 1 / (double) outlinks.size();
                     		else
                     			pij = (double) outlinks.get(i) / (double) out[actualPage];
                     	}
 
                     // The jump to i is allow if the transistion prob. is non-zero
                     if ( pij != 0.0 )
                         if ( random > cumsum && random <= cumsum + pij ) {
                             actualPage = i;
                             break;
                         } // if
                     
                     cumsum += pij;
                 } // for
             } // else
         } // while
 
         return actualPage;
 
     }
 
     /**
      * Simulation of random walk 
      *
      * @return number of visited pages
      */
     private int randomWalk( int N, int actualPage, int[] visits ) {
 
     	final double C = 1-BORED;
         int totalVisits = 0;
         double random;
         boolean terminate = false;
         while ( ! terminate ) {
             
             // Update statistics
             ++totalVisits;
             ++visits[ actualPage ];
 
             // Generate a random number to test termination of the random walk
             random = Math.random();
             if ( random < (1 - C) ) {
                 terminate = true;
             } else {
                 // Generate a random number to do the transition to another page
                 random = 1 - Math.random();  // random is in (0, 1]
                 // Cumulative probability of the transitions already explored
                 double cumsum = 0.0;
                 for ( int i = 0 ; i < N ; ++i ) {
 
                     // Read the probability of going to page i from actualPage
                     double pij = 0.0;
                     Hashtable<Integer, Integer> outlinks = link.get(actualPage);
                     if ( outlinks == null ) { 
                         // Dangling node (wihtout no outlinks)
                         terminate = true;
                         break;
                     } else if ( outlinks.get(i) != null && outlinks.get(i) >=0 )
                         pij = 1 / (double)outlinks.size();
 
                     // Jump to i allowed if the transistion prob. is non-zero
                     if ( pij != 0.0 )
                         if ( random > cumsum && random <= cumsum + pij ) {
                             actualPage = i;
                             break;
                         }
                     
                     cumsum += pij;
 
                 }
             }
 
         }
 
         return totalVisits;
 
     }
 
    /**
     *  Method to show a list of the nPositionShow first best ranked.
     */
    public void showRank(int N, double[] pi, int method){
 	   // Sort the pages by rank
 		Integer[] idxs = new Integer[N];
 		final Double[] vs = new Double[N]; 
 		for ( int i = 0 ; i < N ; ++i ) idxs[i] = i;
 		for ( int i = 0 ; i < N ; ++i ) vs[i] = pi[i];
 		Arrays.sort(idxs, new Comparator<Integer>() {
 			@Override public int compare(final Integer o1, final Integer o2) {
 				return -1 * Double.compare( vs[o1], vs[o2] );
 			}
 		});
 
 		// Show the first ranked in the page rank.
 		for ( int i = 0 ; i < nPositionsShow; ++i )
 			System.out.println(i+1 + ". " + docName[ idxs[i] ] + " " + pi[ idxs[i] ] ); 
 		
 		// Check the sum is equal to 1.
         double sum = 0.0;
         for ( int i = 0 ; i < N ; ++i )
             sum += pi[i];
         
         switch (method) {
         case 0 : System.out.println(">>>> Sum(pi) = " + sum); break;
         case 1 : System.out.println(">>>> End(pi) = " + sum); break;
         case 5 : System.out.println(">>>> Visited(pi) = " + sum); break;
         }
         System.out.println("---------------------------------------");
    }
    
    /**
     * Method that save the pageRanks in a file.
     */
    public void savePageRanks(int N, String filename){
 
         FileWriter file = null;
         PrintWriter pw = null;
 
         try {
             file = new FileWriter(filename);
             pw = new PrintWriter(file);
             
             // Sort the pages by rank
     		Integer[] idxs = new Integer[N];
     		final Double[] vs = new Double[N]; 
     		for ( int i = 0 ; i < N ; ++i ) idxs[i] = i;
     		for ( int i = 0 ; i < N ; ++i ) vs[i] = pi[i];
     		Arrays.sort(idxs, new Comparator<Integer>() {
     			@Override public int compare(final Integer o1, final Integer o2) {
     				return -1 * Double.compare( vs[o1], vs[o2] );
     			}
     		});
 
     		// Save the pageRank in order.
     		for ( int i = 0 ; i < N; ++i )
     			pw.println(docName[idxs[i]] + ";" + pi[idxs[i]]);
         
         } catch (Exception e1) {
             e1.printStackTrace();
             System.out.println("The file doesnt exist! ");
         } finally {
             try {
                 if (file != null)
                     file.close();
             } catch (Exception e2){
                     System.out.println("Problem closing the file.");
             }
         }    
     }
    
    /**
     * Method that fill the arrayList with the followers.
     */
    public static void fillFollowersList(String followersFilename){
 	   
 	   try {
 		   	// Reading the file (followers).
 		   	System.err.print( "Reading followers file... " );
 		   	BufferedReader in = new BufferedReader( new FileReader( followersFilename ));
 		   	String line;
 		   	while ((line = in.readLine()) != null) {
 			     int index = line.indexOf( ";" );                      // Position of ;.
 			     String name = line.substring(index+1,line.length());  // String from 0 to ;.
 			     followers.add(name);
 		    }
 		 
 	   } catch ( FileNotFoundException e ) {
 		    System.err.println( "File " + followersFilename + " not found!" );
 	   } catch ( IOException e ) {
 		    System.err.println( "Error reading file " + followersFilename );
 	   }
 	   System.err.println( followers.size() + " followers have been read.");
    }
     
     /************************************************************************
      *                                MAIN
      ************************************************************************
      * The Main method .....
      *
      * Execute : PageRank <PathGraphFile> <PathPageRankFile> <Mode>
      *           Mode : 0 (powerIteration)
      *                  1-5 (MonteCarlo(i))
      */
     public static void main( String[] args ) {
     	if ( args.length < 3 ) 
     	    System.err.println( "Execute : PageRank <PathGraphFile> <PathPageRankFile> <Mode(0,1,2,3,4,5)> (debug)*" );
         else  
             if ( args.length == 3)                                          // If three arguments
             	new PageRank(args[0],args[1],Integer.parseInt(args[2]));         // Execute noDEBUG.
             else {                                                           // If more than three
             	if (args[3].equals("debug") ) {                                  // If debug in args[3]
                     DEBUG = true;                                                // Activate DEBUG.
                     System.err.println(">>>> DEBUG mode activated");
             	} else {
            		onlyFollowers = true;
             		fillFollowersList(args[3]);
             	}
             	new PageRank(args[0],args[1],Integer.parseInt(args[2]));
             }
     }
 }
