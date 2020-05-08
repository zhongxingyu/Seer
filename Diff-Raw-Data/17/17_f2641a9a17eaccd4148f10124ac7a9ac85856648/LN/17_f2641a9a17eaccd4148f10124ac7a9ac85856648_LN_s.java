 package ml;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 
 public class LN {
 
 	ANode data;
 	LN next;
 	LN back;
 	double backLen;
     String backLabel;
     double backSupport;
 
 	public LN( ANode data ) {
 		this.data = data;
 	}
 
 	public static LN create() {
 		ANode data = new ANode();
 
 		LN n = new LN(data);
 		n.next = new LN(data);
 		n.next.next = new LN(data);
 		n.next.next.next = n;
 		return n;
 	}
 
     public static LN[] getAsList(LN n) {
 		return getAsList(n, true);
 	}
 
 	public static LN[] getAsList(LN n, boolean back) {
 		int nNodes = countNodes(n, back);
 		LN[] list = new LN[nNodes];
 
 
 
 		int xpos = insertDFS(n, list, 0, back );
 
 		if (xpos != nNodes) {
 			throw new RuntimeException("xpos != nNodes");
 		}
 
 		return list;
 	}
 
     private static int countNodes(LN n) {
         return countNodes(n, true);
     }
     private static int countNodes(LN n, boolean back) {
 //		if (n.data.isTip) {
 //			return 1;
 //		} else {
 //			return 1 + countNodes(n.next.back) + countNodes(n.next.next.back);
 //		}
 
         if( n == null ) {
             return 0;
         } else {
             if( back ) {
                 return 1 + countNodes(n.back, false ) + countNodes(n.next.back, false) + countNodes(n.next.next.back, false);
             } else {
                 return 1 + countNodes(n.next.back, false) + countNodes(n.next.next.back, false);
             }
         }
 
 	}
 
 	private static int insertDFS(LN n, LN[] list, int pos) {
 //		if (n.data.isTip) {
 //			list[pos] = n;
 //			return pos + 1;
 //		} else {
 //
 //			pos = insertDFS(n.next.back, list, pos);
 //			pos = insertDFS(n.next.next.back, list, pos);
 //			list[pos] = n;
 //			return pos + 1;
 //		}
 
         return insertDFS(n, list, pos, true );
 	}
 
     private static int insertDFS(LN n, LN[] list, int pos, boolean back ) {
         if( n != null ) {
             if( back ) {
                 pos = insertDFS( n.back, list, pos, false );
             }
             pos = insertDFS(n.next.back, list, pos, false );
 			pos = insertDFS(n.next.next.back, list, pos, false );
 			list[pos] = n;
 			return pos + 1;
         } else {
             return pos;
         }
 
     }
 
     public static LN getTowardsTree( LN ln ) {
         if( !ln.data.isTip ) {
             throw new RuntimeException( "this method must only be called for tip LNs" );
         }
 
         for( int i = 0; i < 3; i++, ln = ln.next ) {
             if( ln.back != null ) {
                 return ln;
             }
         }
 
         throw new RuntimeException( "this seems to be a completely unlinked LN (all LNs in ring have no back pointer" );
     }
 
 	static LN getTowardsNonTip(LN n) {
 		LN start = n;
 		LN cur = n.next;
 
 		while (cur != start) {
 // FIXME: is this right
 			cur = cur.next;
 
 			if (!cur.back.data.isTip) {
 				break;
 			}
 		}
 
 		return cur;
 	}
 
     static LN getTowardsTip(LN n) {
 		LN start = n;
 		LN cur = n.next;
 
 		while (cur != start) {
             if (cur.back.data.isTip) {
 				break;
 			}
 			cur = cur.next;
 
 
 		}
 
 		return cur;
 	}
 
 
 	static Set<String> getTipSet(LN[] list) {
 		HashSet<String> set = new HashSet<String>();
 
 		for( LN n : list ) {
 			if( n.data.isTip) {
 				set.add(n.data.getTipName());
 			}
 		}
 
 		return set;
 	}
 
 	static LN[] findBranchBySplitTrivial( LN n, String tipName ) {
 		LN[] list = getAsList(n);
 
 		
 		for( LN ln : list ) {
 			if( ln.data.isTip && ln.data.getTipName().equals(tipName)) {
 				LN[] ret = {ln, ln.back};
 				return ret;
 			}
 		}
 
 		throw new RuntimeException( "could not find node for tip '" + tipName + "'" );
 
 	}
 	
 	static LN[] findBranchBySplit( LN n, String[] split ) {
 		if( split.length == 1 ) {
 			// trivial case, won't work with the algorithm below
 
 			return findBranchBySplitTrivial( n, split[0] );
 		}
 
 
 		LN[] list = getAsList(n);
 
 		Map<String,LN> tipIndex = new HashMap<String,LN>();
 		Map<Integer,LN> nodeIndex = new HashMap<Integer,LN>();
 		
 		// build indices: tipname => LN and node-serialnumber => LN
 		for( LN ln : list ) {
 			if( ln.data.isTip ) {
 				tipIndex.put(ln.data.getTipName(), ln);
 			}
 
 			if( !nodeIndex.containsKey(n.data.serial)) {
 				nodeIndex.put(ln.data.serial, ln);
 			}
 		}
 
 		//
 		// at-hoc algorithm for finding the separating branch, given one split-set: 
 		// flood the tree from the tips of the split-set:
 		//
 		// nodes get 'marked' if they have two marked
 		// neighbors (ancetors). At the beginning only the leafs from one split-set are marked.
 		// The recursion stops at the branch that separates the two split-sets (which can never have two
         // marked ancestors (at least this is my theory...))
 		// (this description is rubbish)
 		//
 		Set<Integer>markedNodes = new HashSet<Integer>();
 		Set<Integer>openNodes = new HashSet<Integer>(markedNodes);
 
 		for( String name : split ) {
 			LN tip = tipIndex.get(name);
 
 			if( tip == null ) {
 				throw new RuntimeException("tip '" + name + "' not found in tip index" );
 			}
 
 			markedNodes.add( tip.data.serial );
 
 			tip = LN.getTowardsTree(tip);
 
 			openNodes.add( tip.back.data.serial );
 		}
 
 		while( openNodes.size() > 1 ) {
 
 			int lastNode = -1;
 			LN lastLN = null;
 
 			// if this is too slow, we could keep track of the mark-count
 			// and use a priority queue:
 			
 			for( int next : openNodes ) {
 				LN oln = nodeIndex.get(next);
 
 				int markedCount = 0;
 				for( int i = 0; i < 3; i++, oln = oln.next ) {
 					if( markedNodes.contains(oln.back.data.serial)) {
 						markedCount++;
 					}
 				}
 
 
 				if( markedCount == 2 ) {
 					lastNode = next;
 					lastLN = oln;
 					break;
 				}
 			}
 
 			if( lastNode < 0 ) {
 				throw new RuntimeException("no open node with two marked neighbors found. looks like a bad split" );
 			}
 
 			openNodes.remove(lastNode);
 			markedNodes.add(lastNode);
 
 			for( int i = 0; i < 3; i++, lastLN = lastLN.next ) {
 				if( !markedNodes.contains(lastLN.back.data.serial)) {
 					openNodes.add(lastLN.back.data.serial);
 				}
 			}
 
 		}
 
 		if( openNodes.size() != 1 ) {
 			throw new RuntimeException( "split finder finished but openNodes.size != 1 !?" );
 		}
 
 		
 
 		LN[] ret = new LN[2];
 
 		// the last node on the open list is on one side of the separating
 		LN n1 = nodeIndex.get(openNodes.iterator().next());
 
 		// the neighbor that is not in the 'marked' set is the other half of the
 		// separating branch
 		
 		for( int i = 0; i < 3; i++, n1 = n1.next ) {
 			if( !markedNodes.contains(n1.back.data.serial) ) {
 				ret[0] = n1;
 				ret[1] = n1.back;
 				return ret;
 			}
 		}
 		
 		throw new RuntimeException("the node next to the split does not have an unmarked neighbor" );
 
 
 	}
 
     public static LN[] removeTaxon( LN n, String taxon ) {
         if( n.data.isTip(taxon)) {
             // keep it simple
             throw new RuntimeException( "ooops! the tip to be removed is used as pseudo root. bailing out" );
         }
 
         LN[] nodelist = getAsList(n);
 
         for( LN node : nodelist ) {
 //            if( node.data.isTip ) {
 //                System.out.printf( "tip: %s\n", node.data.getTipName() );
 //            }
             if( node.data.isTip(taxon) ) {
                 node = getTowardsTree(node);

                 node.back.next.back.back = node.back.next.next.back;
                 node.back.next.next.back.back = node.back.next.back;
 
                 LN[] ret = {node.back.next.back, node.back.next.next.back};
                 return ret;
             }
         }
 
         throw new RuntimeException( "could not find tip with name '" + taxon + "'" );
     }
 
 
 	public static double longestPath( LN n ) {
 		if( !n.data.isTip ) {
 			throw new RuntimeException("this method is only for tips");
 		}
 
 
 		n = LN.getTowardsTree(n);
 
 		return n.backLen + longestPathRec( n.back );
 	}
 
 
 	public static double longestPathRec( LN n ) {
 		if( n.data.isTip ) {
 			return 0.0;
 		} else {
 			double len1 = longestPathRec(n.next.back) + n.next.backLen;
 			double len2 = longestPathRec(n.next.next.back) + n.next.next.backLen;
 
 			return Math.max(len1, len2);
 
 		}
 	}
 
     public static LN deepCloneDirected( LN n, LN prev ) {
         assert( n.data == n.next.data && n.data == n.next.next.data );
 
         ANode data = new ANode(n.data);
 
         LN nn = new LN(data);
         nn.next = new LN(data);
         nn.next.next = new LN(data);
         nn.next.next.next = nn;
 
         // if we are at the pseudo root (prev == null), also clone n.back
         // in the other case, let it point to prev, which is the previously cloned portion of the tree.
         if( prev == null ) {
             nn.back = (n.back != null) ? deepCloneDirected(n.back, nn) : null;
         } else {
             nn.back = prev;
         }
 
         nn.next.back = (n.next.back != null) ? deepCloneDirected(n.next.back, nn.next) : null;
         nn.next.next.back = (n.next.next.back != null) ? deepCloneDirected(n.next.next.back, nn.next.next) : null;
 
         nn.backLabel = n.backLabel;
         nn.backLen = n.backLen;
         nn.backSupport = n.backSupport;
 
         nn.next.backLabel = n.next.backLabel;
         nn.next.backLen = n.next.backLen;
         nn.next.backSupport = n.next.backSupport;
 
         nn.next.next.backLabel = n.next.next.backLabel;
         nn.next.next.backLen = n.next.next.backLen;
         nn.next.next.backSupport = n.next.next.backSupport;
 
 
         return nn;
     }
 
 
     public static LN deepClone( LN n ) {
         return deepCloneDirected(n, null);
     }
 
     public static boolean checkLinkSymmetry( LN n ) {
         LN[] list = getAsList(n);
 
         boolean consistent = true;
 
         for( int i = 0; i < list.length && consistent; i++ ) {
             consistent = consistent &&
                     (list[i].back == null || (list[i].back.back != null && list[i] == list[i].back.back));
         }
 
 
         return consistent;
     }
 
     static boolean cmpLNList( LN[] l1, LN[] l2 ) {
 
 
         if( l1.length != l2.length ) {
             return false;
         } else {
             boolean equal = true;
 
             for( int i = 0; i < l1.length && equal; i++ ) {
                 equal = equal &&
                         l1[i].backLabel.equals(l2[i].backLabel) &&
                         l1[i].backLen == l2[i].backLen &&
                         l1[i].backSupport == l2[i].backSupport &&
                         l1[i].data.contentEquals(l2[i].data);
             }
 
             return equal;
         }
     }
 
     static boolean cmpLNListObjectIdentity( LN[] l1, LN[] l2 ) {
         if( l1.length != l2.length ) {
             return false;
         } else {
             boolean equal = true;
 
             for( int i = 0; i < l1.length && equal; i++ ) {
                 equal = equal &&
                         l1[i] == l2[i];
 
             }
 
             return equal;
         }
     }
 
 }
