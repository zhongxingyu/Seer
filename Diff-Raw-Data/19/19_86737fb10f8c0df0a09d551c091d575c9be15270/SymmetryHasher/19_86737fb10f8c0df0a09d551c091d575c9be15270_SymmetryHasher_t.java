 package edu.berkeley.gamesman.hasher.symmetry;
 
 import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
 import edu.berkeley.gamesman.hasher.genhasher.GenState;
 import edu.berkeley.gamesman.hasher.genhasher.SymmetryWrapper;
 
 public class SymmetryHasher<S extends GenState> {
 	private final SymNode<S> root;
 	private final SymmetryWrapper<S> myWrapper;
 
 	public SymmetryHasher(GenHasher<S> hasher, int suffixStartsAt) {
 		myWrapper = new SymmetryWrapper<S>(hasher);
		root = new SymNode<S>(myWrapper, hasher.numElements - suffixStartsAt,
 				new int[hasher.numElements - suffixStartsAt], 0L);
 	}
 }
