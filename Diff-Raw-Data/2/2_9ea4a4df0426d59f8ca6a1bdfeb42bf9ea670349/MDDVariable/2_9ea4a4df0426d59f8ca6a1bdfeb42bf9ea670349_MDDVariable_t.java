 package org.colomoto.mddlib;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import org.colomoto.mddlib.internal.MDDStore;
 
 /**
  * Definition of a multi-valued variable used in a MDDManager.
  * This class is also used to retrieve (or create) nodes associated to the variable.
  * <p>
  * A variable has a name, an optional associated object and set of possible values.
  * For the sake of simplicity, only the number of possible values is defined,
  * the factory will use the integer range [0..nbval[.
  * <p>
  * A Boolean variable uses 2 values and will be mapped to the [0,1] interval.
  * <br>A ternary variable uses 3 values and will be mapped to the [0,2] interval.
  * <p>
  * If your variable can take another set of values, it is your responsibility to map
  * them to the [0..nbval[ interval.
  * 
  * @author Aurelien Naldi
  */
 public class MDDVariable {
 
 	/**
 	 * Key used to create the variable.
 	 * It is used to show the variable name.
 	 */
 	public final Object key;
 	
 	/**
 	 * Number of possible values for this variable.
 	 * it must be at least 2.
 	 */
 	public final byte nbval;
 	
 	/**
 	 * Rank of this variable in the MDDmanager.
 	 * Ranks should be unique and provided by the MDD store.
 	 */
 	public final int order;
 
 	/**
 	 * MDDStore in which this variable appears.
 	 */
 	private final MDDStore store;
 	
 	
 	public MDDVariable (MDDStore store, int order, Object key, byte nbval) {
 		this.store = store;
 		this.order = order;
 		this.key = key;
 		this.nbval = nbval;
 	}
 	
 	/**
 	 * get a boolean node. It will be created if needed
 	 * The resulting ID should be freed when it stopped being used. 
 	 * 
 	 * @param f left child (false)
 	 * @param t right child (true)
 	 * 
 	 * @return the ID of the node
 	 */
 	public int getNode(int f, int t) {
 		if (nbval != 2) {
 			throw new RuntimeException("MDD: not a Boolean variable (nbval="+nbval+")");
 		}
 		
 		return store.getNode(order, f, t);
 	}
 
 	/**
 	 * Get a multi-valued node. It will be created if needed.
 	 * The resulting ID should be freed when it stopped being used.
 	 *  
 	 * @param children
 	 * 
 	 * @return the ID of the node
 	 */
 	public int getNode(int[] children) {
 		if (nbval != children.length) {
 			throw new RuntimeException("MDD: nbval mismatch ("+nbval+" vs "+children.length+")");
 		}
 		
 		return store.getNode(order, children);
 	}
 	
 	/**
 	 * Get a Boolean node and release the provided children.
 	 * @see <code>getNode(int, int)</code>.
 	 * 
 	 * @param f
 	 * @param t
 	 * 
 	 * @return the ID of the node
 	 */
 	public int getNodeFree(int f, int t) {
 		int ret = getNode(f, t);
 		store.free(f);
 		store.free(t);
 		return ret;
 	}
 
 	/**
 	 * Get a node and release the provided children.
 	 * @see <code>getNode(int[])</code>.
 	 * 
 	 * @param children
 	 * 
 	 * @return the ID of the node
 	 */
 	public int getNodeFree(int[] children) {
 		int ret = getNode(children);
 		for (int n: children) {
 			store.free(n);
 		}
 		return ret;
 	}
 
 	/**
 	 * get a node for the specified variable with two different children:
 	 * a "true" child in the specified range and a "false" one outside.
 	 * 
 	 * @param var the variable
 	 * @param vfalse the "false" child
 	 * @param vtrue the "true" child
 	 * @param start the start of the "true" range
 	 * @param end the end of the "true" range
 	 * 
 	 * @return a MDD ID.
 	 */
 	public int getSimpleNode(int vfalse, int vtrue, int start, int end) {
 		if (start>end || start<0 || end>=nbval) {
 			return -1;
 		}
 		if (nbval == 2) {
 			if (start != end) {
 				return vtrue;
 			}
 			if (start == 0) {
 				return store.getNode(order, vtrue, vfalse);
 			}
 			return store.getNode(order, vfalse, vtrue);
 		}
 		
 		
 		int[] children = new int[nbval];
 		for (int i=0 ; i<start ; i++) {
 			children[i] = vfalse;
 		}
 		for (int i=start ; i<=end ; i++) {
 			children[i] = vtrue;
 		}
 		for (int i=end+1 ; i<nbval ; i++) {
 			children[i] = vfalse;
 		}
 		return store.getNode(order, children);
 	}
 
 	@Override
 	public int hashCode() {
 		return key.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if (other instanceof MDDVariable) {
 			return key.equals(((MDDVariable)other).key);
 		}
 		return key.equals(other);
 	}
 
 	/**
 	 * Test if this variable comes after another variable.
 	 * 
 	 * @param other
 	 * @return
 	 */
 	public boolean after(MDDVariable other) {
 		if (other == null) {
 			return false;
 		}
 
		return other.order < this.order;
 	}
 	
 	public static MDDVariable selectFirstVariable(MDDVariable v1, MDDVariable v2) {
 		if (v1 == null || v1.after(v2)) {
 			return v2;
 		}
 		
 		return v1;
 	}
 }
