 package com.bizosys.hsearch.federate;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public final class BitSetOrSet {
 	
 	private static boolean DEBUG_MODE = FederatedSearchLog.l.isDebugEnabled();
 	private static boolean INFO_MODE = FederatedSearchLog.l.isInfoEnabled();
 
 	private BitSet bitsets = null;
 	@SuppressWarnings("rawtypes")
 	private Set sets = null;
 	
 	public final int size() {
 		
 		if ( null != sets) return sets.size();
 
 		if ( null != bitsets) {
 			int size = 0;
 			for (int i = bitsets.nextSetBit(0); i >= 0; i = bitsets.nextSetBit(i+1)) size++;
 			return size;
 		}
 		
 		return 0;
 	}
 	
 	public final boolean isEmpty() {
 		if ( null != bitsets) return bitsets.isEmpty();
 		if ( null != sets) return ( sets.size() == 0 );
 		return true;
 	}	
 	
 	public final void setDocumentSequences(BitSet bitSets) {
 		this.bitsets = bitSets;
 		this.sets = null;
 	}
 	
 	public final void setDocumentIds(Set sets) {
 		this.bitsets = null;
 		this.sets = sets;
 	}
 	
 	public final BitSet getDocumentSequences() {
 		return this.bitsets ;
 	}
 	
 	public final Set getDocumentIds() {
 		return this.sets;
 	}
 
 	public final void clear() {
 		if ( null != bitsets ) bitsets.clear();
 		if ( null != sets ) sets.clear();
 	}
 	
 	public final void reset() {
 		if ( null != bitsets ) bitsets.clear();
 		if ( null != sets ) sets.clear();
 	}
 
 	public final void and (final BitSetOrSet source) throws FederatedSearchException {
 		if ( null == source) {
 			if ( DEBUG_MODE ) {
 				FederatedSearchLog.l.debug("\n\n Source is null. Clearning \n\n");
 			}			
 			this.clear();
 			return;
 		}
 		
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.debug("\n\n" + source.bitsets + "\t" + source.sets + "\n\n");
 		}
 		if ( null != source.sets) {
 			if ( null == this.sets) this.sets = new HashSet<Object>(); 
 			and (this.sets,source.sets);
 		} else if ( null != source.bitsets) {
 			if ( null == this.bitsets) this.bitsets = new BitSet(); 
 			and (this.bitsets,source.bitsets);
 		} else {
 			this.clear();
 		}
 	}
 	
 	public final void or (final BitSetOrSet source) throws FederatedSearchException {
 		if ( null == source) return;
 		
 		if ( null != source.bitsets) {
 			if ( null == this.bitsets) this.bitsets = new BitSet(); 
 			or (this.bitsets,source.bitsets);
 		} else if ( null != source.sets) {
 			if ( null == this.sets) this.sets = new HashSet<Object>(); 
 			or (this.sets,source.sets);
 		}
 	}
 
 	public final void not (final BitSetOrSet source) throws FederatedSearchException {
 		if ( null == source) return;
 		if ( null != source.bitsets) {
 			if ( null == this.bitsets) this.bitsets = new BitSet(); 
 			not (this.bitsets,source.bitsets);
 		}
		else {
 			if ( null == this.sets) this.sets = new HashSet<Object>(); 
 			not (this.sets,source.sets);
 		}
 	}
 	
 	@Override
 	public final String toString() {
 		int bitSetsT = ( null == bitsets) ? 0 : bitsets.length();
 		int setsT = ( null == sets) ? 0 : sets.size();
 		return "BitSetOrSet - " + bitSetsT + "-" + setsT;
 	}
 	
 	/************************************************************
 	 * SET CALCULATION START
 	 *************************************************************/
 	
 	private static final void and (final Set<Object> destination, final Set<Object> source) throws FederatedSearchException {
 		
 		if ( null == destination) {
 			source.clear();
 			return;
 		}
 		
 		if ( null == source) {
 			destination.clear();
 			return;
 		}
 
 		int sourceT = ( null == source) ? 0 : source.size();
 		int destinationT = ( null == destination) ? 0 : destination.size();
 
 		if ( 0 == sourceT) {
 			if ( INFO_MODE ) {
 				FederatedSearchLog.l.info("Set<?>: and cleared bits as source is null");
 			}
 			destination.clear();
 			return;
 		}
 		
 		List<Object> intersected = new ArrayList<Object>();
 		if ( sourceT > destinationT) {
 			for (Object key : destination) {
 				if ( source.contains(key)) intersected.add(key);
 			}
 		} else {
 			for (Object key : source) {
 				if ( destination.contains(key)) intersected.add(key);
 			}
 		}
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.info("BitSetOrSet: " + source.size() + " AND " + destination.size() + " = " + intersected.size() );
 		}
 		
 		destination.clear();
 		destination.addAll(intersected);
 		
 		
 	}
 	
 	private static final void or (final Set<Object> destination, final Set<Object> source) throws FederatedSearchException {
 		if ( null == destination) {
			throw new FederatedSearchException("Destination set is null");
 		}
 		
 		int sourceT = ( null == source) ? 0 : source.size();
 		if ( 0 == sourceT) return;
 		
 		destination.addAll(source);
 		
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.info("BitSetOrSet: " + source.size() + " OR XX = " + destination.size() );
 		}
 	}
 
 	private static final void not (final Set<Object> destination, final Set<Object> source) throws FederatedSearchException {
 		if ( null == destination) {
			throw new FederatedSearchException("Destination set is null");
 		}
 		destination.removeAll(source);
 	}
 		
 	
 	/************************************************************
 	 * BITSET CALCULATION STARTS
 	 *************************************************************/
 	
 	private static final void and (final BitSet destination, final BitSet source) throws FederatedSearchException {
 		
 		if ( null == destination) {
 			source.clear();
 		}
 
 		if ( null == source) {
 			if ( INFO_MODE ) {
 				FederatedSearchLog.l.info("BitSetOrSet: and cleared bits as source is null");
 			}
 			destination.clear();
 			return;
 		}
 		
 		destination.and(source);
 		
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.info("BitSetOrSet: and result " + destination.toString());
 		}
 		
 	}
 	
 	private static final void or (BitSet destination, BitSet source) throws FederatedSearchException {
 		
 		if ( null == destination) {
 			throw new FederatedSearchException("Destination bitset is null");
 		}
 		if ( null == source) return;
 
 		destination.or(source);
 		
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.info("BitSetOrSet: or result " + destination.toString());
 		}
 		
 	}
 
 	private static final void not (BitSet destination, BitSet source) throws FederatedSearchException {
 		
 		if ( null == destination) {
 			throw new FederatedSearchException("Destination bitset is null");
 		}
 
 		if ( null == source) return;
 		
 		destination.andNot(source);
 		
 		if ( DEBUG_MODE ) {
 			FederatedSearchLog.l.info("BitSetOrSet: andNot result " + destination.toString());
 		}
 		
 	}	
 
 	/************************************************************
 	 * BITSET CALCULATION ENDS
 	 *************************************************************/
 	
 }
