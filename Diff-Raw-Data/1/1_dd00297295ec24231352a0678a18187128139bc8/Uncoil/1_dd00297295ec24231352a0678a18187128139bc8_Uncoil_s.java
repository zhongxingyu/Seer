 package uk.co.plogic.gwt.lib.cluster.uncoil;
 
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import uk.co.plogic.gwt.lib.cluster.domain.Coord;
 
 /**
  * Create a data structure which reveals the relationships between the nodes it is
  * populated with. These Nodes are probably from a {@see NodePage}.
  * 
  * Uncoil is only concerned with points that will be on a map - it doesn't maintain
  * the tree in detail, i.e. it should only have active nodes.
  * 
  * This implementation uses a simple linked list. It might be better as some sort
  * of tree in the future.
  *  
  * @author si
  *
  */
 public class Uncoil implements Iterator<Nest>, Cloneable{
 
 	private Nest linkList; // kept as (roughly) the middle Nest in the link list
 	private Nest linkListStart;
 	private Nest linkListEnd;
 	private Nest iteratorPointer;
 	private int linkListSize;
 	Logger logger = Logger.getLogger("uk.co.plogic.cluster.uncoil.Uncoil");
 
 	public Uncoil() {}
 	
 	/**
 	 * @return number of Nodes in link list
 	 */
 	public int sizeOf() {
 		return linkListSize;
 	}
 	
 	public Uncoil clone() {
 
 //		Uncoil cUncoil;
 //		try {
 //			cUncoil = (Uncoil) super.clone();
 //			
 //		} catch (CloneNotSupportedException e) {
 //			// ??
 //			return null;
 //		}
 
 		Uncoil cUncoil = new Uncoil();
 		
 		// the cloned Uncoil is going to be manipulated so rebuild
 		// all Nests and the link list. The Nodes themselves wont
 		// be modified.
 		// Maybe - might be quicker to copy the existing link list?
 
 		cUncoil.linkList = null; // remove existing link list ; causes reset in addNode
 		
 		Nest citeratorPointer = linkListStart;
 		while( citeratorPointer != null) {
 			Nest nst = new Nest(citeratorPointer.getLeftID(), citeratorPointer.getRightID(),
 								citeratorPointer.getCoord(), citeratorPointer.getWeight());
 			cUncoil.addNest(nst);
 			citeratorPointer = citeratorPointer.getNextNest();
 		}
 
 		return cUncoil;
 
 	}
 
 	public void addNest(Nest nst) {
 
 		// uncoil is only concerned with points that are shown on the map.
 		// It doesn't care about the tree structure. 
 		if( ! nst.getCoord().isInitialised() )
 			return;
 		
 		int lft = nst.getLeftID();
 		
 		if( linkList == null ) {
 			// init on first Node
 			// see clone() before changing this block
 			linkList = nst;
 			linkListStart = nst;
 			linkListEnd = nst;
 			linkListSize = 1;
 			return;
 		}
 		
 		linkListSize++;
 		
 		Nest traverse = linkList;
 		while( traverse != null ) {
 			if( lft < traverse.getLeftID() ) {
 				// left
 				Nest lastNest = traverse.getLastNest();
 				if( lastNest == null ) {
 					traverse.setLastNest(nst);
 					nst.setNextNest(traverse);
 					linkListStart = nst;
 					traverse = null;
 				} else if( lft >= lastNest.getLeftID() ) {
 					// insert it here
 					lastNest.setNextNest(nst);
 					traverse.setLastNest(nst);
 					nst.setLastNest(lastNest);
 					nst.setNextNest(traverse);
 					traverse = null;
 				} else {
 					traverse = lastNest;
 				}
 		
 				
 			} else {
 				// right
 				Nest nextNest = traverse.getNextNest();
 				if( nextNest == null ) {
 					traverse.setNextNest(nst);
 					nst.setLastNest(traverse);
 					linkListEnd = nst;
 					traverse = null;
 				} else if( lft < nextNest.getLeftID() ) {
 					// insert it here
 					nextNest.setLastNest(nst);
 					traverse.setNextNest(nst);
 					nst.setNextNest(nextNest);
 					nst.setLastNest(traverse);
 					traverse = null;
 				} else {
 					traverse = nextNest;
 				}
 			}
 			
 		}
 
 		resetLinkList();
 		resetIterator();
 		
 		// remove effect of the weight of the Nest if it's parents already exist in the link list
 		Nest p = findParent(nst.getLeftID(), nst.getRightID());
 		if( p != null ) {
 			String msg = "Found parent: " + p.toString() + " for:" + nst.toString();
 			logger.info(msg);
 			
 			// remove effect of nst on parent
 			Coord cToRemove = nst.getCoord();
 			double cWeight = nst.getWeight();
 			
 			Coord c = p.getCoord();
 			double nodeWeight = p.getWeight();
 			
 			double xOriginal = c.getX()*nodeWeight;
 			double yOriginal = c.getY()*nodeWeight;
 			
 			double xRemove = cToRemove.getX()*cWeight;
 			double yRemove = cToRemove.getY()*cWeight;
 			
 			double newWeight = nodeWeight - cWeight;
 			
 			// equal to 0 means scraggly tree (intermediateLeafNode) so do nothing
 			if( newWeight < 0 ) {
 				msg = "Found negative weight after removing child:";
 				msg += nst.toString()+" from parent:"+p.toString();
 				logger.severe(msg);
 			} else if( newWeight > 0 ) {
 
 				Coord newC = new Coord(	(xOriginal-xRemove)/newWeight,
 										(yOriginal-yRemove)/newWeight
 									  );
 				logger.info("Coord:"+c.toString()+" Adjusted to:"+newC.toString());
 				p.setCoord(newC);
 				p.setWeight((int) newWeight);
 
 			}
 
 
 			if( newWeight < 1 ) {
 				// it has been replaced by subsequent children
 				logger.info("Dropping:" + p.toString());
 
 				dropNest(p);
 				resetLinkList();
 				resetIterator();
 			}
 		}
 		
 		
 	}
 	
 
 	/**
 	 * for testing
 	 * @return
 	 */
 	protected Nest getLinkListStart() {
 		return linkListStart;
 	}
 	protected Nest getLinkList() {
 		return linkList;
 	}
 
 	
 	/**
 	 * return the first Nest from the LinkList with an ID (leftID) less
 	 * that nestID.
 	 * 
 	 * This method could/should use a better indexing method. Maybe a b-tree.
 	 * I'm not going to for now as I expect link lists to be around 200 items
 	 * in practise.
 	 * 
 	 * @param nestID
 	 * @return null if can't be found
 	 */
 	public Nest getNestBefore(int nestID) {
 
 		Nest traverse;
 		// start at end or middle of link list
 		if( nestID > linkList.getLeftID() ) {
 			traverse = linkListEnd;
 		} else {
 			traverse = linkList;
 		}
 		
 		while( traverse != null ) {
 			if( traverse.getLeftID() < nestID)
 				return traverse;
 			traverse = traverse.getLastNest();
 		}
 		return null;
 	}
 	
 	/**
 	 * opposite to @see getNestBefore()
 	 * @param nestID
 	 * @return
 	 */
 	public Nest getNestAfter(int nestID) {
 
 		Nest traverse;
 		// start at end or middle of link list
 		if( nestID < linkList.getLeftID() ) {
 			traverse = linkListStart;
 		} else {
 			traverse = linkList;
 		}
 		
 		while( traverse != null ) {
 			if( traverse.getLeftID() > nestID)
 				return traverse;
 			traverse = traverse.getNextNest();
 		}
 		return null;
 	}
 
 	/**
 	 * set the main link list pointer (linkList) to the centre of the link list.
 	 */
 	private void resetLinkList() {
 		// don't worry about rounding, it's only rough
 		int midPoint = (linkListEnd.getLeftID() - linkListStart.getLeftID())/2;
 		linkList = getNestAfter(midPoint);
 	}
 
 
 	/**
 	 * Return node with exact left and right if it exists in the link list 
 	 * @param nodeLeft
 	 * @param nodeRight
 	 * @return Node or null
 	 */
 	public Nest findExact(int nodeLeft, int nodeRight) {
 		
 		Nest nst = getNestBefore(nodeLeft);
 		if( nst == null ) return null;
 
 		while( nst != null ) {
 			if( nst.getLeftID() == nodeLeft && nst.getRightID() == nodeRight ) {
 				return nst;
 			}
 			nst = nst.getNextNest();
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Find child using nested set index. i.e. might not be direct child.
 	 * @param nodeLeft
 	 * @param nodeRight
 	 * @return closest child
 	 */
 	public Nest findChild(int nodeLeft, int nodeRight) {
 		
 		// TODO - confirm we need to traverse. As no two nests can share a left ID
 		// I think getNestAfter() could be return the next child.
 		
 		Nest nst = getNestAfter(nodeLeft);
 		if( nst == null ) return null;
 
 		while( nst != null ) {
 			if( nst.getLeftID() > nodeLeft && nst.getRightID() < nodeRight ) {
 				return nst;
 			}
 			nst = nst.getNextNest();
 		}
 		return null;
 	}
 
 	/**
 	 * Find parent using nested set index. i.e. might not be direct parent.
 	 * @param nodeLeft
 	 * @param nodeRight
 	 * @return
 	 */
 	public Nest findParent(int nodeLeft, int nodeRight) {
 		
 		// TODO - as in findChild() - confirm we need to traverse.
 		
 		Nest nst = getNestBefore(nodeLeft);
 		if( nst == null ) return null;
 
 		while( nst != null ) {
 			if( nst.getLeftID() < nodeLeft && nst.getRightID() > nodeRight ) {
 				return nst;
 			}
 			nst = nst.getLastNest();
 		}
 		return null;
 	}
 	
 	/**
 	 * Find the closest relative to the given left right Nest position.
 	 * @param nodeLeft
 	 * @param nodeRight
 	 * @return
 	 */
 	public Nest findRelative(int nodeLeft, int nodeRight) {
 		Nest n;
 		// find self
 		if( (n=findExact(nodeLeft, nodeRight)) != null )
 			return n;
 		if( (n=findChild(nodeLeft, nodeRight)) != null )
 			return n;
 		return findParent(nodeLeft, nodeRight);
 	}
 
 	public void resetIterator() {
 		iteratorPointer = linkListStart;
 	}
 	
 	/**
 	 * loop through each Node in each Nest
 	 */
 	@Override
 	public boolean hasNext() {
 		return (iteratorPointer != null);
 	}
 
 	@Override
 	public Nest next() {
 		Nest n = iteratorPointer;
 		iteratorPointer = iteratorPointer.getNextNest();
 		return n;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * remove NodeLink from linklist chain
 	 * @param doomedNodeLink
 	 */
 	private void dropNest(Nest doomedNest) {
 
 		if( doomedNest == linkListStart ) {
 			linkListStart = doomedNest.getNextNest();
 			doomedNest.getNextNest().setLastNest(null);
 		} else {
 			doomedNest.getLastNest().setNextNest(doomedNest.getNextNest());
 			doomedNest.getNextNest().setLastNest(doomedNest.getLastNest());
 		}
 		linkListSize--;
 		logger.info("Removed nest:"+doomedNest.toString());
 
 	}
 
 	/**
 	 * return coordinate that represents all nodes.
 	 * @return
 	 */
 	public Coord getWeightedCoord() {
 		
 		double totalWeight = 0;
 		double totalX = 0;
 		double totalY = 0;
 		Nest localIteratorPointer = linkListStart;
 
 		while( localIteratorPointer != null ) {
 			Coord x = localIteratorPointer.getCoord();
 			int weight = localIteratorPointer.getWeight();
 			totalWeight += weight;
 			totalX += x.getX() * weight;
 			totalY += x.getY() * weight;
 			localIteratorPointer = localIteratorPointer.getNextNest();
 		}
 
 		if( totalWeight < 1 )
 			return null;
 		else
 			return new Coord(totalX/totalWeight, totalY/totalWeight);
 	}
 }
