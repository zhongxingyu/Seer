 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ml;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author sim
  */
 public class ClassifierLTree {
     public static void main( String[] args ) {
 
         File dir = new File( args[0] );
 
         File rnFile = new File( args[1] );
 
         Map<String,String> rnm = parseRealNeighbors( rnFile );
 		Map<String,String[]> splitmap = parseSplits( rnFile );
 
         File oltFile = new File( dir, "RAxML_originalLabelledTree." + args[2] );
         File classFile = new File( dir, "RAxML_classification." + args[2] );
         File reftreeFile = new File( args[3] );
 
         // parse 'original labelled tree'
         LN n;
         {
             TreeParser tp = new TreeParser(oltFile);
             n = tp.parse();
         }
         LN[] lnl = LN.getAsList(n);
 
         double oltDiameter = treeDiameter(n);
 
         // parse reference tree used for weighted branch difference stuff
         LN reftree;
         {
             TreeParser tpreftree = new TreeParser(reftreeFile);
             reftree = tpreftree.parse();
         }
 
 // highest path weight in reference tree (=path with the highest sum of edge weights, no necessarily the longest path)
         double reftreeDiameter = treeDiameter(reftree);
 
         // some 'anal' tests for the deep-clone stuff
         {
             LN[] list1 = LN.getAsList(n);
             LN nn = LN.deepClone(n);
 
             LN[] list2 = LN.getAsList(n);
             LN[] list3 = LN.getAsList(nn);
 
             System.out.printf( "cmp: %s %s %s %s %s %s\n", LN.cmpLNList( list1, list2 ), LN.cmpLNList( list1, list3 ), LN.cmpLNList( list2, list3 ), LN.cmpLNListObjectIdentity( list1, list2 ), LN.cmpLNListObjectIdentity( list1, list3 ), LN.cmpLNListObjectIdentity( list2, list3 ));
             System.out.printf( "sym: %s %s\n", LN.checkLinkSymmetry(n), LN.checkLinkSymmetry(nn) );
         }
         
         try {
 
             BufferedReader r = new BufferedReader(new FileReader(classFile));
 
 
             String line;
 
             while( ( line = r.readLine() ) != null ) {
                 
 		
 
                 try {
                     // parse line from raxml classification output
                     StringTokenizer ts = new StringTokenizer(line);
 
                     // the name of the classified taxon
                     String seq = ts.nextToken();
 
                     // name of the branch, the classifier has put the taxon in (= current insertion position)
                     String branch = ts.nextToken();
 
                     // boostrap support of this classification
                     String supports = ts.nextToken();
                     int support = Integer.parseInt(supports);
 
 					System.out.printf( "seq: '%s'\n", seq );
 
 
                     // get the split that identifies the original insertion position
                     String[] split = splitmap.get(seq);
                     LN[] realBranch = LN.findBranchBySplit(n, split);
 
 					// find the split that identifies the current insertion position
 					String[] insertSplit;
 					{
 						LN[] insertBranch = findBranchByName( n, branch );
 
 						LN[] ll = LN.getAsList(insertBranch[0], false);
 						LN[] lr = LN.getAsList(insertBranch[1], false);
 
 						Set<String> sl = LN.getTipSet(ll);
 						Set<String> sr = LN.getTipSet(lr);
 
 						Set<String> smallset = (sl.size() <= sr.size()) ? sl : sr;
 
 						insertSplit = new String[smallset.size()];
 						insertSplit = smallset.toArray(insertSplit);
 						Arrays.sort(insertSplit);
 					}
 
                     // get the weighted path length between current and original insertion position
                     // in the reference tree
                     double lenOT;
                     {
 
 						//System.out.printf( "diameter: %f\n", reftreeDiameter );
 
                         // original position branch
                         LN reftreePruned = LN.deepClone(reftree);
 
                         // this call has two important effects:
                         // - remove the current taxon from the reference tree (copy), so that its topology
                         //   resembles the pruned tree from the current classification
                         // - return the branch from the reference tree that corresponds to the original taxon position
                         LN[] opb = LN.removeTaxon(reftreePruned, seq);
 
 
                         // identify the current insertion position from the pruned tree in the
                         // reference tree. The position is identified by the split set (or how ever this thin is called)
 
                         // the LN referenced by reftreePruned can (!?) never be removed by removeTaxon so it's ok to use it as pseudo root
                         LN[] ipb = LN.findBranchBySplit(reftreePruned, insertSplit);
 
 
                         // calculate weighted path length beween original and current insertion branches
                         {
 							double lenOT1 = getPathLenBranchToBranch(opb, ipb);
 							
 							LN[] opbflip = {opb[1], opb[0]};
 							double lenOT2 = getPathLenBranchToBranch(opbflip, ipb);
 
 
 							System.out.printf( "opb: %f %f\n", lenOT1, lenOT2 );
 							lenOT = Math.min( lenOT1, lenOT2 );
 						}
 
                         lenOT += opb[0].backLen;
 
                     }
 
                     // for comparison: get the path length in the (possibly unweighted) pruned tree
                     // get path len between real position and current insertion position
                     double len = getPathLenToNamedBranch(realBranch[0], branch, false);
                     if( len < 0 ) {
                         len = getPathLenToNamedBranch(realBranch[1], branch, false);
                     }
                     len += realBranch[0].backLen;


                     int lenUW = getUnweightedPathLenToNamedBranch(realBranch[0], branch, false);
                     if( lenUW == Integer.MAX_VALUE ) {
                         lenUW = getUnweightedPathLenToNamedBranch(realBranch[1], branch, false);
                     }
 
 
                     System.out.printf( "%s %s %s %d %d %f %f %f (%f %f)\n", seq, branch, realBranch[0].backLabel, support, lenUW, len, lenOT, lenOT / reftreeDiameter, reftreeDiameter, oltDiameter );
                     //System.out.printf( "branch: %s '%s' '%s'\n", b[0].backLabel, b[0].data.isTip ? b[0].data.getTipName() : "*NOTIP*", b[1].data.isTip ? b[1].data.getTipName() : "*NOTIP*");
 
 //					}
 
                 } catch (NoSuchElementException x) {
                     System.out.printf( "bad line in raxml classifier output: " + line );
                     x.printStackTrace();
 
                     throw new RuntimeException( "bailing out" );
 
                 }
                 
             }
 
         } catch (IOException ex) {
             Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);
             throw new RuntimeException("bailing out");
         }
 
 
 
     }
 
     private static LN[] expandToAllNodes(LN[] lnl) {
         LN[] lnlx = new LN[lnl.length * 3];
 
         int pos = 0;
         for( LN ln : lnl ) {
             lnlx[pos++] = ln;
             lnlx[pos++] = ln.next;
             lnlx[pos++] = ln.next.next;
         }
 
         return lnlx;
     }
 
 	private static LN[] findBranchByName(LN n, String branch) {
 		LN[] list = LN.getAsList(n);
 
 
 		for( LN node : list ) {
 			if( node.backLabel.equals(branch)) {
 				assert( node.back.backLabel.equals(branch));
 				LN[] ret = {node, node.back};
 
 				return ret;
 			}
 		}
 
 		throw new RuntimeException( "could not find named branch '" + branch + "'" );
 	}
 
     private static LN findTipWithNamedBranch(LN[] lnl, String branch) {
         System.out.printf( "find: %s %d\n", branch, lnl.length );
 
         LN[] lnlx = expandToAllNodes( lnl );
 
         for( LN ln : lnlx ) {
             if( ln.back != null ) {
                 System.out.printf( "(%s %s)\n", ln.backLabel, ln.data.isTip );
             }
 
             if( ln.back!= null && ln.data.isTip && ln.backLabel.equals(branch)) {
                 return ln;
             }
         }
         System.out.printf( "\n" );
         return null;
     }
 
     static boolean branchEquals( LN[] b1, LN[] b2 ) {
         return (b1[0].data.serial == b2[0].data.serial && b1[1].data.serial == b2[1].data.serial) ||
                (b1[0].data.serial == b2[1].data.serial && b1[1].data.serial == b2[0].data.serial);
     }
 
     private static double getPathLenToBranch(LN n, LN[] b) {
 		if( n == null ) {
 			return Double.POSITIVE_INFINITY;
 		}
 
 //		System.out.printf( "bbd: %d (%d %d)\n", n.data.serial, b[0].data.serial, b[1].data.serial );
 
         if( n.data.serial == b[0].data.serial || n.data.serial == b[1].data.serial ) {
             return 0.0;
         } else {
             double len = getPathLenToBranch(n.next.back, b);
             if( len < Double.POSITIVE_INFINITY ) {
                 return len + n.next.backLen;
             } else {
                 len = getPathLenToBranch(n.next.next.back, b);
 
                 if( len < Double.POSITIVE_INFINITY ) {
                     return len + n.next.next.backLen;
                 }
             }
 
             return Double.POSITIVE_INFINITY;
 
         }
     }
 
 	private static double getPathLenBranchToBranch(LN n[], LN[] b) {
 		
 
 		//System.out.printf( "bbd: %d (%d %d) --- ", n[0].data.serial, n[1].data.serial, b[0].data.serial, b[1].data.serial );
 
         if( branchEquals(n, b) ) {
             return 0.0;
         } else {
             //double len = Double.POSITIVE_INFINITY;
 
 			if( !n[0].data.isTip ) {
 				{
 					LN[] next = {n[0].next.back, n[0].next};
 
 					double len = getPathLenBranchToBranch( next, b);
 
 					if( len < Double.POSITIVE_INFINITY ) {
 						return len + n[0].backLen;
 					}
 				}
 
 				{
 					LN[] next = {n[0].next.next.back, n[0].next.next};
 
 					double len = getPathLenBranchToBranch( next, b);
 
 					if( len < Double.POSITIVE_INFINITY ) {
 						return len + n[0].backLen;
 					}
 				}
 
 			}
 
 //            if( !n[1].data.isTip ) {
 //				{
 //					LN[] next = {n[1].next.back, n[1].next};
 //
 //					double len = getBranchBranchDistancex( next, b);
 //
 //					if( len < Double.POSITIVE_INFINITY ) {
 //						return len + 1;//n[1].backLen;
 //					}
 //				}
 //
 //				{
 //					LN[] next = {n[1].next.next.back, n[1].next.next};
 //
 //					double len = getBranchBranchDistancex( next, b);
 //
 //					if( len < Double.POSITIVE_INFINITY ) {
 //						return len + 1;//n[1].backLen;
 //					}
 //				}
 //
 //			}
 
 
             return Double.POSITIVE_INFINITY;
 
         }
     }
 
     private static Map<String, String> parseRealNeighbors(File rnFile) {
         try {
             BufferedReader r = new BufferedReader(new FileReader(rnFile));
 
             Map<String,String> map = new HashMap<String, String>();
 
 
             String line;
 
             while( (line = r.readLine()) != null ) {
 
 
                 try {
                     StringTokenizer st = new StringTokenizer(line);
 					String seq = st.nextToken();
                     String k = st.nextToken();
                     String v = st.nextToken();
 
                     map.put(k,v);
                 } catch( NoSuchElementException x ) {
 
                     System.out.printf( "bad line in tsv file: " + line );
                     x.printStackTrace();
                     throw new RuntimeException("bailing out");
                 }
             }
 
             r.close();
 
 //			for( Map.Entry<String,String> e : map.entrySet() ) {
 //				System.out.printf( "rnm: '%s' => '%s'\n", e.getKey(), e.getValue() );
 //
 //			}
 
             return map;
 
         } catch (IOException ex) {
             Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);
 
             throw new RuntimeException( "bailing out");
         }
     }
 
 	private static Map<String, String[]> parseSplits(File rnFile) {
         try {
             BufferedReader r = new BufferedReader(new FileReader(rnFile));
 
             Map<String,String[]> map = new HashMap<String, String[]>();
 
 
             String line;
 
             while( (line = r.readLine()) != null ) {
 
 
                 try {
                     StringTokenizer st = new StringTokenizer(line);
 					String seq = st.nextToken();
                     String k = st.nextToken();
 					st.nextToken(); // skip the 'real neighbor'
                     String v = st.nextToken();
 
 
 					//
 					// parse the 'split list' (comma separated list of tips names)
 					//
 
 					StringTokenizer st2 = new StringTokenizer(v, ",");
 					String tip;
 					
 					int num = st2.countTokens();
 					if( num < 1 ) {
 						throw new RuntimeException("could not parse split list from real neighbor file");
 					}
 
 					String[] split = new String[num];
 
 
 //					System.out.printf( "split: %s\n", k );
 					for( int i = 0; i < num; i++ ) {
 						split[i] = st2.nextToken();
 //						System.out.printf( " '%s'", split[i] );
 					}
 					//split[num] = k;
 //					System.out.println();
 					
                     map.put(k,split);
                 } catch( NoSuchElementException x ) {
 
                     System.out.printf( "bad line in tsv file: " + line );
                     x.printStackTrace();
                     throw new RuntimeException("bailing out");
                 }
             }
 
             r.close();
 
 //			for( Map.Entry<String,String> e : map.entrySet() ) {
 //				System.out.printf( "rnm: '%s' => '%s'\n", e.getKey(), e.getValue() );
 //
 //			}
 
             return map;
 
         } catch (IOException ex) {
             Logger.getLogger(ClassifierLTree.class.getName()).log(Level.SEVERE, null, ex);
 
             throw new RuntimeException( "bailing out");
         }
     }
 
 //    public static LN findTip( LN start, String name ) {
 //        if( start.data.isTip ) {
 //
 //            if(start.data.getTipName().equals(name)) {
 //                return getTowardsTree(start);
 //            } else {
 //                return null;
 //            }
 //
 //
 //        } else {
 //            LN r = findTip(start.next.back, name);
 //            if( r != null ) {
 //                return r;
 //            }
 //
 //            return findTip(start.next.next.back, name);
 //        }
 //    }
 
 
     public static LN findTip( LN[] list, String name ) {
         //System.out.printf( "list size: %d\n", list.length );
         for( LN ln : list ) {
 //            if( ln.data.isTip) {
 //                System.out.printf( "tip: %s\n", ln.data.getTipName() );
 //            }
 
             if( ln.data.isTip && ln.data.getTipName().equals(name)) {
                 return LN.getTowardsTree(ln);
             }
         }
 
         return null;
     }
 
     public static double getPathLenToNamedBranch( LN node, String name) {
         return getPathLenToNamedBranch(node, name, true);
     }
 //    public static double getPathLenToNamedBranch( LN node, String name, boolean back ) {
 //
 //        if( node.backLabel.equals(name)) {
 //            return 0.0;
 //        }
 //        if( back && node.back != null ) {
 //            double len = getPathLenToNamedBranch(node.back, name, false);
 //
 //            if( len >= 0 ) {
 //                return len + node.backLen;
 //            }
 //        }
 //        if( node.next.back != null ) {
 //            double len = getPathLenToNamedBranch(node.next.back, name, false);
 //
 //            if( len >= 0 ) {
 //                return len + node.next.backLen;
 //            }
 //        }
 //        if( node.next.next.back != null ) {
 //            double len = getPathLenToNamedBranch(node.next.next.back, name, false);
 //
 //            if( len >= 0 ) {
 //                return len + node.next.next.backLen;
 //            }
 //        }
 //
 //        return -1;
 //
 //    }
 
     public static double getPathLenToNamedBranch( LN node, String name, boolean back ) {
 
         if( back ) {
             throw new RuntimeException( "the 'back' flag is not supported for this opperation.bailing out." );
         }
 
 
         if( node.backLabel.equals(name)) {
             return 0.0;
         }
         
         if( node.next.back != null ) {
             double len = getPathLenToNamedBranch(node.next.back, name, false);
 
             if( len >= 0 ) {
                 return len + node.backLen;
             }
         }
         if( node.next.next.back != null ) {
             double len = getPathLenToNamedBranch(node.next.next.back, name, false);
 
             if( len >= 0 ) {
                 return len + node.backLen;
             }
         }
 
         return -1;
 
     }
 
     public static int getUnweightedPathLenToNamedBranch( LN node, String name, boolean back ) {
 
         if( back ) {
             throw new RuntimeException( "the 'back' flag is not supported for this opperation.bailing out." );
         }
 
 
         if( node.backLabel.equals(name)) {
             return 0;
         }
 
         if( node.next.back != null ) {
             int len = getUnweightedPathLenToNamedBranch(node.next.back, name, false);
 
             if( len < Integer.MAX_VALUE ) {
                 return len + 1;
             }
         }
         if( node.next.next.back != null ) {
             int len = getUnweightedPathLenToNamedBranch(node.next.next.back, name, false);
 
             if( len < Integer.MAX_VALUE ) {
                 return len + 1;
             }
         }
 
         return Integer.MAX_VALUE;
 
     }
 
 	public static double treeDiameter( LN n ) {
 		LN[] list = LN.getAsList(n);
 
 		int cnt = 0;
 		double longestPath = 0.0;
 
 
 		for( LN node : list ) {
 			if( node.data.isTip && node.back != null ) {
 				cnt++;
 
 				longestPath = Math.max( longestPath, LN.longestPath(node));
 			}
 		}
 
 
 		System.out.printf( "cnt: %d\n", cnt );
 
 		return longestPath;
 	}
 
     
 
 }
