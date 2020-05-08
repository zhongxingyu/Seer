 package org.colomoto.mddlib.internal;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.colomoto.mddlib.MDDManager;
 import org.colomoto.mddlib.MDDManagerFactory;
 import org.colomoto.mddlib.MDDVariable;
 import org.colomoto.mddlib.MDDVariableFactory;
 import org.colomoto.mddlib.NodeRelation;
 import org.colomoto.mddlib.VariableEffect;
 
 /**
  * MDD factory implementation: create, store, retrieve a collection of MDDs.
  * <p>
  * The number of leaves is defined upon creation and can not be changed.
  * <p>
  * MDDs are stored in a large integer array, divided into blocs.
  * Each bloc denotes a MDD node, providing its level and list of children,
  * as well as the reference counter.
  * To avoid duplication, a hashmap allows to find existing nodes quickly.
  * New nodes are added by extending the storage array, but blocs can also
  * be freed when unused. A chained list allows to reuse these free blocs.
  * <p>
  * @see MDDManager for further details.
  * 
  * @author Aurelien Naldi
  */
 public class MDDStoreImpl implements MDDStore {
 
 	private static final int DEFAULT_CAPACITY  = 100;
 	private static final int DEFAULT_HASHITEMS = 20;
 
 	private static final int FILL_LIMIT = 80;
 	
 	private static final int INC_COUNT = 1;
 	private static final int INC_VALUES = 2;
 	
 	private static final int[] NOTFLIP = {1,0};
 	
 	/* Temporary switches to enable/disable freeing nodes */
 	private static final boolean CANFREE=true;
 	private static final boolean CANFREEHASH=CANFREE;
 	
 	
 	protected MDDVariable[] variables;
 
 	private int blocsize;
 
 	private int[] hashcodes;
 	private int[] hashitems;
 	private int[] blocs;
 	
 	// starting point for free blocs/item chained lists
 	private int freeBloc = -1;
 	private int freeItem = -1;
 
 	// first free position at the end of data/hash-items arrays
 	private int lastitem = 0;
 	private int lastbloc = 0;
 
 	private int nbnodes = 0;
 	private final int nbleaves;
 
 	/**
 	 * Create a new MDDStore using the default capacity.
 	 * Note that this constructor should be called through {@link MDDManagerFactory}, not directly.
 	 * 
 	 * @param keys		the list of variables that can be used.
 	 * @param nbleaves	the number of values that can be reached.
 	 */
 	public MDDStoreImpl(Collection<?> keys, int nbleaves) {
 		this(DEFAULT_CAPACITY, keys, nbleaves);
 	}
 
 	/**
 	 * Create a new MDDStore.
 	 * 
 	 * @param capacity		number of nodes that can be stored in the initially reserved space.
 	 * @param variables		the list of variables that can be used.
 	 * @param nbleaves		the number of values that can be reached.
 	 */
 	private MDDStoreImpl(int capacity, Collection<?> keys, int nbleaves) {
 		if (keys instanceof MDDVariableFactory) {
 			this.variables = getVariables((MDDVariableFactory)keys);
 		} else {
 			this.variables = getBooleanVariables(keys);
 		}
 		
 		this.nbleaves = nbleaves;
 		blocsize = 2;
 		for (MDDVariable var: variables) {
 			if (var.nbval > blocsize ) {
 				blocsize  = var.nbval;
 			}
 		}
 		blocsize += INC_VALUES;  // add INC_VALUES cells in the bloc for metadata (type, usage count)
 		
 		hashcodes = new int[capacity*2];
 		hashitems = new int[DEFAULT_HASHITEMS];
 		reset_hash();
 
 		lastbloc = nbleaves;
 		blocs = new int[nbleaves + capacity*blocsize];
 		for (int i=0 ; i<nbleaves ; i++) {
 			blocs[i] = i;
 		}
 	}
 
 	@Override
 	public MDDManager getManager(List<?> order) {
 		return MDDManagerProxy.getProxy(this, order);
 	}
 
 
 	/* ********************* VARIABLES ****************************** */
 	
 	private MDDVariable[] getBooleanVariables(Collection<?> keys) {
 		
 		MDDVariable[] variables = new MDDVariable[keys.size()];
 		int i=0;
 		byte v = 2;
 		for (Object key: keys) {
 			variables[i] = new MDDVariable(this, i, key, v);
 			i++;
 		}
 		
 		return variables;
 	}
 	
 	private MDDVariable[] getVariables(MDDVariableFactory keys) {
 		
 		MDDVariable[] variables = new MDDVariable[keys.size()];
 		int i=0;
 		for (Object key: keys) {
 			byte v = keys.getNbValue(key);
 			variables[i] = new MDDVariable(this, i, key, v);
 			i++;
 		}
 		
 		return variables;
 	}
 	
 	@Override
 	public MDDVariable getNodeVariable(int n) {
 		if (isleaf(n)) {
 			return null;
 		}
 		
 		int l = getLevel(n);
 		if (l < 0) {
 			throw new RuntimeException("Invalid level found for "+n+": free/use bug?");
 		}
 		return variables[l];
 	}
 
 	@Override
 	public int getVariableIndex(MDDVariable var) {
 		return var.order;
 	}
 
 	@Override
 	public MDDVariable getVariableForKey(Object key) {
 		// TODO: make getVariableID faster if needed
 		//MDDVariable var = m_key2variable.get(o);
 		for (MDDVariable var: variables) {
 			if ( key.equals(var.key) ) {
 				return var;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public MDDVariable[] getAllVariables() {
 		// should we return a clone?
 		return variables;
 	}
 
 	@Override
 	public int getNode(int var, int lchild, int rchild) {
 		if (lchild == rchild) {
 			return use(lchild);
 		}
 		if ((!isleaf(lchild) && blocs[lchild] <= var) || (!isleaf(rchild) && blocs[rchild] <= var)) {
 			System.err.println("Invalid request");
 			return -1;
 		}
 		int hash = compute_bhash(var, lchild, rchild);
 		boolean hashexists = hashcodes[hash] != -1;
 		if (hashexists) {
 			int pos = hashcodes[hash];
 			if (is_equal(pos, var, lchild, rchild)) {
 				return use(pos);
 			}
 			int item = hashcodes[hash+1];
 			while (item != -1) {
 				pos = hashitems[item];
 				if (is_equal(pos, var, lchild, rchild)) {
 					return use(pos);
 				}
 				item = hashitems[item+1];
 			}
 		}
 		
 		// node not found, create it
 		int pos = get_free_bloc();
 		blocs[pos] = var;
 		blocs[pos+INC_COUNT] = 0; 	   // reset usage count
 		blocs[pos+INC_VALUES] = lchild;
 		blocs[pos+INC_VALUES+1] = rchild;
 		
 		// increase usage of the children
 		use(lchild);
 		use(rchild);
 		nbnodes++;
 
 		if ( (100*nbnodes)/hashcodes.length > FILL_LIMIT) {
 			extend_hash();
 		} else {
 			place_hash(pos, hash);
 		}
 		return use(pos);
 	}
 
 	private int getNodeFree(int var, int f, int t) {
 		int ret = getNode(var, f, t);
 		free(f);
 		free(t);
 		return ret;
 	}
 	
 	private int getNodeFree(int var, int[] children) {
 		int ret = getNode(var, children);
 		for (int c: children) {
 			free(c);
 		}
 		return ret;
 		
 	}
 
 	@Override
 	public int getNode(int var, int[] children) {
 		// check that the children are not all equal
 		int child = children[0];
 		for (int c:children) {
 			if (c != child) {
 				child = -1;
 				break;
 			}
 			
 			if (!isleaf(c) && blocs[c] <= var) {
 				System.err.println("Invalid node request!");
 				return -1;
 			}
 		}
 		if (child > -1) {
 			return use(child);
 		}
 		
 		int hash = compute_mhash(var, children);
 		boolean hashexists = hashcodes[hash] != -1;
 		if (hashexists) {
 			int pos = hashcodes[hash];
 			if (is_equal(pos, var, children)) {
 				return use(pos);
 			}
 			int item = hashcodes[hash+1];
 			while (item != -1) {
 				pos = hashitems[item];
 				if (is_equal(pos, var, children)) {
 					return use(pos);
 				}
 				item = hashitems[item+1];
 			}
 		}
 		
 		// node not found, create it
 		int pos = get_free_bloc();
 		blocs[pos] = var;
 		blocs[pos+INC_COUNT] = 0; 	   // reset usage count
 		System.arraycopy(children, 0, blocs, pos+INC_VALUES, children.length);
 		
 		for (int c: children) {
 			use(c);
 		}
 		
 		nbnodes++;
 		
 		if ( (100*nbnodes)/hashcodes.length > FILL_LIMIT) {
 			extend_hash();
 		} else {
 			place_hash(pos, hash);
 		}
 		return use(pos);
 	}
 
 	/* ******************** USAGE COUNT ***************************** */
 	
 	@Override
 	public int use(int node) {
 		if (!isleaf(node)) {
 			blocs[node+INC_COUNT]++;
 		}
 		return node;
 	}
 
 	@Override
 	public void free(int pos) {
 		if (!CANFREE) {
 			return;
 		}
 		if (isleaf(pos)) {
 			return;
 		}
 		
 		if (blocs[pos+INC_COUNT] > 1) {
 			blocs[pos+INC_COUNT]--;
 			return;
 		}
 
 		if (blocs[pos] < 0) {
 			System.err.println("re-free bloc: "+pos);
 			return;
 		}
 
 		int var = blocs[pos];
 		int nbval = variables[var].nbval;
 		
 		// remove it from the hash
 		freeHash(pos, var, nbval);
 
 		// clear the data and set the bloc as free
 		blocs[pos] = -1;
 		if (lastbloc == pos+blocsize) {
 			lastbloc = pos;
 		} else {
 			blocs[pos+1] = freeBloc;
 			freeBloc = pos;
 		}
 		// free the children
 		for (int i=0 ; i<nbval ; i++) {
 			free(blocs[pos+INC_VALUES+i]);
 			blocs[pos+INC_VALUES+i] = 0;
 		}
 		nbnodes--;
 	}
 	
 	/**
 	 * Internal method to remove an entry from the hashtable.
 	 * This aims to be called by <code>free(int)</code> when needed.
 	 * 
 	 * @param pos
 	 * @param var
 	 * @param nbval
 	 */
 	private void freeHash(int pos, int var, int nbval) {
 		if (!CANFREEHASH) {
 			return;
 		}
 		
 		// compute the hash
 		int hash;
 		if (nbval == 2) {
 			hash = compute_bhash(var, blocs[pos+INC_VALUES], blocs[pos+INC_VALUES+1]);
 		} else {
 			int[] children = new int[variables[var].nbval];
 			System.arraycopy(blocs, pos+INC_VALUES, children, 0, children.length);
 			hash = compute_mhash(var, children);
 		}
 
 		int hpos = hashcodes[hash];
 		int itemPos = hashcodes[hash+1];
 		if (hpos == pos) { 		// the item is in the main hashtable
 			if (itemPos == -1) {
 				hashcodes[hash] = -1;
 			} else {
 				// re-chain back this hashcode
 				hashcodes[hash] = hashitems[itemPos];
 				hashcodes[hash+1] = hashitems[itemPos+1];
 				free_hashitem(itemPos);
 			}
 			return;
 		}
 		
 		if (true) {
 			//return;
 		}
 
 		// the item is not in the main hashtable: look it up in the hashitem linktable
 		int nextItem, prevItem = -1;
 		while (itemPos != -1) {
 			hpos = hashitems[itemPos];
 			nextItem = hashitems[itemPos+1];
 			if (hpos == pos) {
 				if (prevItem == -1) {
 					// first item, link in the main hashcodes array
 					hashcodes[hash+1] = nextItem;
 				} else {
 					// update chain in the hashitems array
 					hashitems[prevItem+1] = nextItem;
 				}
 				free_hashitem(itemPos);
 				return;
 			}
 			prevItem = itemPos;
 			itemPos = nextItem;
 		}
 		System.err.println("item not found !!!!");
 	}
 	
 	/**
 	 * Internal method to destroy a hash item.
 	 * This aims to be called by <code>freeHash</code> when needed.
 	 * 
 	 * @param item
 	 */
 	private void free_hashitem(int item) {
 		if (item < 0) {
 			throw new RuntimeException("Trying to free a negative hashitem");
 		}
 		// free the hash item
 		if (item == lastitem-2) {
 			lastitem = item;
 		} else {
 			hashitems[item]   = -1;
 			hashitems[item+1] = freeItem;
 			freeItem = item;
 		}
 	}
 
 	/**
 	 * Internal method to insert a new hash.
 	 * It works like <code>get_free_bloc</code>
 	 */
 	private void place_hash(int blocPos, int hash) {
 		if (hashcodes[hash] == -1) {
 			hashcodes[hash] = blocPos;
 			hashcodes[hash+1] = -1;
 			return;
 		}
 		int pos;
 		if (freeItem >= 0) {
 			pos = freeItem;
 			freeItem = hashitems[pos+1];
 		} else {
 			pos = lastitem;
 			lastitem += 2;
 			if (lastitem > hashitems.length) {
 				hashitems = extend_array(hashitems);
 			}
 		}
 		
 		hashitems[pos] = blocPos;
 		hashitems[pos+1] = hashcodes[hash+1];
 		if (hashcodes[hash+1] == pos) {
 			System.err.println("BIG BUG with hash link list!");
 		}
 		hashcodes[hash+1] = pos;
 	}
 
 	/**
 	 * Flip the values of leaves reachable in this MDD.
 	 * 
 	 * @param node
 	 * @param newValues
 	 * 
 	 * @return the ID of a node rooting a MDD with the same structure but different leaves
 	 */
 	private int leafFlip(int node, int[] newValues) {
 		if (isleaf(node)) {
 			if (node >= newValues.length) {
 				return node;
 			}
 			return newValues[node];
 		}
 		int level = blocs[node];
 		int nbval = variables[level].nbval;
 		if (nbval == 2) {
 			int l = leafFlip(blocs[node+INC_VALUES], newValues);
 			int r = leafFlip(blocs[node+INC_VALUES+1], newValues);
 			return getNodeFree(level, l, r);
 		}
 		
 		int[] children = new int[nbval];
 		for (int i=0 ; i<children.length ; i++) {
 			children[i] = leafFlip(blocs[node+INC_VALUES+i], newValues);
 		}
 		return getNodeFree(level, children);
 	}
 
 
 	@Override
 	public int not(int node) {
 		return leafFlip(node, NOTFLIP);
 	}
 
 
 	@Override
 	public NodeRelation getRelation(int first, int other) {
 		if (first == other) {
 			if (isleaf(first)) {
 				return NodeRelation.LL;
 			}
 			return NodeRelation.NN;
 		}
 		
 		if (isleaf(first)) {
 			if (isleaf(other)) {
 				return NodeRelation.LL;
 			}
 			return NodeRelation.LN;
 		}
 		
 		if (isleaf(other)) {
 			return NodeRelation.NL;
 		}
 		
 		int l1 = blocs[first];
 		int l2 = blocs[other];
 		if (l1 == l2) {
 			return NodeRelation.NN;
 		} else if (l1 < l2) {
 			return NodeRelation.NNn;
 		} else {
 			return NodeRelation.NNf;
 		}
 	}
 	
 	/**
 	 * helper to compute hashcodes. Shamelessly stolen from JavaBDD.
 	 * @param a
 	 * @param b
 	 * @return
 	 */
     private int PAIR(int a, int b) {
         return ((a + b) * (a + b + 1) / 2 + a);
     }
     /**
 	 * Compute hashcodes for a Boolean node. Shamelessly stolen from JavaBDD.
      * @param var
      * @param lchild
      * @param rchild
      * @return
      */
 	private int compute_bhash(int var, int lchild, int rchild) {
 		int hash = PAIR(rchild, PAIR(lchild, var));
 		return (Math.abs(hash) % (hashcodes.length / 2))*2;
 	}
 
 	/**
 	 * Helper to compute hashcodes for multi-valued nodes, adapted from JavaBDD.
 	 * 
 	 * @param var
 	 * @param children
 	 * @return
 	 */
 	private int compute_mhash(int var, int[] children) {
 		int hash = var;
 		for (int i=0 ; i<children.length ; i++) {
 			hash = PAIR(children[i], hash);
 		}
 		return (Math.abs(hash) % (hashcodes.length / 2))*2;
 	}
 
 	/**
 	 * Test if an existing node is the same as a requested one (Boolean version).
 	 * 
 	 * @param position existing node ID
 	 * @param var level of the requested node
 	 * @param lchild
 	 * @param rchild
 	 * 
 	 * @return
 	 */
 	private boolean is_equal(int position, int var, int lchild, int rchild) {
 		return blocs[position] == var && blocs[position+INC_VALUES] == lchild && blocs[position+INC_VALUES+1] == rchild;
 	}
 
 	/**
 	 * Test if an existing node is the same as a requested one (multi-valued version).
 	 * 
 	 * @param position
 	 * @param var
 	 * @param children
 	 * @return
 	 */
 	private boolean is_equal(int position, int var, int[] children) {
 		if (blocs[position] != var) {
 			return false;
 		}
 		for (int i=0 ; i<children.length ; i++) {
 			if (blocs[position+INC_VALUES+i] != children[i]) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * Get the next free data bloc.
 	 * Look-up among free blocs or allocate a new one.
 	 * 
 	 * @return
 	 */
 	private int get_free_bloc() {
 		int pos = freeBloc;
 		if (pos >= 0) {
 			freeBloc = blocs[pos+1];
 			return pos;
 		}
 		pos = lastbloc;
 		lastbloc += blocsize;
 		if (lastbloc > blocs.length) {
 			blocs = extend_array(blocs);
 		}
 		return pos;
 	}
 
 	/**
 	 * extend an array: allocate a bigger array and copy existing data.
 	 */
 	private int[] extend_array(int[] data) {
 		int[] new_array = new int[data.length*2];
 		System.arraycopy(data, 0, new_array, 0, data.length);
 		return new_array;
 	}
 	
 	/**
 	 * extend the hashing array: allocate a bigger array and recompute all hash
 	 * into the new array.
 	 */
 	private void extend_hash() {
 		hashcodes = new int[hashcodes.length*2];
 		reset_hash();
 		
 		for (int i=nbleaves ; i<lastbloc ; i+= blocsize) {
 			int idvar = blocs[i];
 			if (idvar < 0) {
 				// empty bloc, skip it
 				continue;
 			}
 			int nbval = variables[idvar].nbval;
 			int hash;
 			if (nbval == 2) {
 				hash = compute_bhash(idvar, blocs[i+INC_VALUES], blocs[i+INC_VALUES+1]);
 			} else {
 				int[] children = new int[nbval];
 				System.arraycopy(blocs, i+INC_VALUES, children, 0, nbval);
 				hash = compute_mhash(idvar, children);
 			}
 			
 			// link it
 			place_hash(i, hash);
 		}
 	}
 
 	/**
 	 * Reset all values in a hash array
 	 */
 	private void reset_hash() {
 		// clear all positions
 		for (int i=0 ; i<hashcodes.length ; i++) {
 			hashcodes[i] = -1;
 		}
 		// clear hashitems
 		for (int i=0 ; i<hashitems.length ; i++) {
 			hashitems[i] = -1;
 		}
 		lastitem = 0;
 		freeItem = -1;
 	}
 
 	@Override
 	public int getNodeCount() {
 		return nbnodes;
 	}
 
 	@Override
 	public int getLeafCount() {
 		return nbleaves;
 	}
 
 	@Override
 	public boolean isleaf(int id) {
 		return id < nbleaves;
 	}
 	
 	/**
 	 * Get the level of a node (i.e. index of the associated variable.
 	 * 
 	 * @param id
 	 * @return the node level
 	 */
 	private int getLevel(int id) {
 		if (isleaf(id)) {
 			return -1;
 		}
 		return blocs[id];
 	}
 
 	@Override
 	public int getChild(int id, int value) {
 		if (isleaf(id)) {
 			return -1;
 		}
 		return blocs[id+INC_VALUES+value];
 	}
 
 	@Override
 	public int[] getChildren(int node) {
 		if (isleaf(node)) {
 			return null;
 		}
 		
 		int nbchildren = getNodeVariable(node).nbval;
 		int[] next = new int[nbchildren];
 		System.arraycopy(blocs, node+INC_VALUES, next, 0, nbchildren);
 		return next;
 	}
 
 
 	@Override
 	public byte reach(int node, byte[] values) {
 		while (!isleaf(node)) {
 			int level = getLevel(node);
 			node = getChild(node, values[level]);
 		}
 		return (byte)node;
 	}
 
 	@Override
 	public byte reach(int node, byte[] values, int[] orderMap) {
 		if (orderMap == null) {
 			return reach(node, values);
 		}
 		
 		while (!isleaf(node)) {
 			int level = getLevel(node);
 			node = getChild(node, values[orderMap[level]]);
 		}
 		return (byte)node;
 	}
 
 	
 	@Override
 	public int getSign(int node, MDDVariable pivot) {
 		return getSign(node, pivot, 0);
 	}
 	
 	private int getSign(int node, MDDVariable pivot, int curSign) {
 		if (isleaf(node)) {
 			return curSign;
 		}
 
 		MDDVariable var = getNodeVariable(node);
 		if (var.order < pivot.order) {
 			// recursive call
 			for (int i=0 ; i<var.nbval ; i++) {
 				curSign = getSign(getChild(node, i), pivot, curSign);
 			}
 		} else if (var == pivot) {
 			for (int i=1 ; i<var.nbval ; i++) {
 				curSign = getSign_sub(getChild(node, i-1), getChild(node, i), curSign);
 			}
 		}
 		return curSign;
 	}
 	
 	private int getSign_sub(int n1, int n2, int curSign) {
 		if (n1 == n2) {
 			return curSign;
 		}
 		int nbval;
 		switch (getRelation(n1, n2)) {
 		case LL:
 			// make the choice!
 			if (n1 > n2) {
 				switch (curSign) {
 				case 0:
 					curSign = -1;
 					break;
 				case 1:
 					curSign = 2;
 					break;
 				}
 			} else if (n1 < n2) {
 				switch (curSign) {
 				case 0:
 					curSign = 1;
 					break;
 				case -1:
 					curSign = 2;
 					break;
 				}
 			}
 			break;
 		case LN:
 		case NNf:
 			nbval = getNodeVariable(n2).nbval;
 			for (int i=0 ; i<nbval ; i++) {
 				curSign = getSign_sub(n1, getChild(n2, i), curSign);
 			}
 			break;
 		case NL:
 		case NNn:
 			nbval = getNodeVariable(n1).nbval;
 			for (int i=0 ; i<nbval ; i++) {
 				curSign = getSign_sub(getChild(n1, i), n2, curSign);
 			}
 			break;
 		case NN:
 			nbval = getNodeVariable(n1).nbval;
 			for (int i=0 ; i<nbval ; i++) {
 				curSign = getSign_sub(getChild(n1, i), getChild(n2, i), curSign);
 			}
 			break;
 		}
 		return curSign;
 	}
 	
 	@Override
 	public boolean[] collectDecisionVariables(int node) {
 		boolean[] vars = new boolean[variables.length];
 		
 		collectDecisionVariables(vars, node);
 		
 		return vars;
 	}
 	
 	/**
 	 * Recursive backend for collectDecisionVariables(int).
 	 * 
 	 * @param flags
 	 * @param node
 	 */
 	private void collectDecisionVariables(boolean[] flags, int node) {
 		MDDVariable var = getNodeVariable(node);
 		if (var == null) {
 			return;
 		}
 		
 		flags[getLevel(node)] = true;
 		for (int i=0 ; i<var.nbval ; i++) {
 			collectDecisionVariables(flags, getChild(node, i));
 		}
 	}
 
 	@Override
 	public VariableEffect getVariableEffect(MDDVariable var, int node) {
 		
 		// no effect if we can not encounter the wanted variable
 		MDDVariable curVar = getNodeVariable(node);
 		if (curVar == null || curVar.after(var)) {
 			return VariableEffect.NONE;
 		}
 
 		// if we found the variable, we will find an effect downstream
 		if (curVar.equals(var)) {
 			VariableEffect effect = VariableEffect.NONE;
 			int curChild = getChild(node, 0);
 			for (int value=1 ; value < var.nbval ; value++) {
 				int nextChild = getChild(node, value);
 				if (nextChild != curChild) {
 					effect = effect.combine( lookupEffect(curChild, nextChild) );
 					curChild = nextChild;
 				}
 			}
 			return effect;
 		}
 
 
 		// otherwise, just browse deeper
 		int curChild = getChild(node, 0);
 		VariableEffect effect = getVariableEffect(var, curChild);
 		for (int value=1 ; value < curVar.nbval ; value++) {
 			int nextChild = getChild(node, value);
 			if (nextChild != curChild) {
 				curChild = nextChild;
 				effect = effect.combine( getVariableEffect(var, nextChild) );
 				if (effect == VariableEffect.DUAL) {
 					return effect;
 				}
 			}
 		}
 		
 		return effect;
 	}
 
 	@Override
 	public VariableEffect[] getMultivaluedVariableEffect(MDDVariable var, int node) {
 		if (var.nbval == 2) {
 			return new VariableEffect[] { getVariableEffect(var, node) };
 		}
 		
 		// real multivalued lookup
 		VariableEffect[] effects = new VariableEffect[var.nbval-1];
 		for (int i=1 ; i<var.nbval ; i++) {
 			effects[i-1] = VariableEffect.NONE;
 		}
 		inspectVariableEffect(var, node, effects);
 		return effects;
 	}
 	
 	private void inspectVariableEffect(MDDVariable var, int node, VariableEffect[] effects) {
 		// no effect if we can not encounter the wanted variable
 		MDDVariable curVar = getNodeVariable(node);
 		if (curVar == null || curVar.after(var)) {
 			return;
 		}
 
 		// if we found the variable, we will find an effect downstream
 		if (curVar.equals(var)) {
 			int curChild = getChild(node, 0);
 			for (int value=1 ; value < var.nbval ; value++) {
 				int nextChild = getChild(node, value);
 				if (nextChild != curChild) {
 					effects[value-1] = effects[value-1].combine( lookupEffect(curChild, nextChild) );
 					curChild = nextChild;
 				}
 			}
 			return;
 		}
 
 
 		// otherwise, just browse deeper
 		int curChild = getChild(node, 0);
 		inspectVariableEffect(var, curChild, effects);
		for (int value=1 ; value < var.nbval ; value++) {
 			int nextChild = getChild(node, value);
 			if (nextChild != curChild) {
 				curChild = nextChild;
 				inspectVariableEffect(var, nextChild, effects);
 			}
 		}
 		
 		return;
 	}
 
 	private VariableEffect lookupEffect(int low, int high) {
 		NodeRelation rel = getRelation(low, high);
 		switch (rel) {
 
 		case LL:
 			if (low < high) {
 				return VariableEffect.POSITIVE;
 			}
 			if (low > high) {
 				return VariableEffect.NEGATIVE;
 			}
 			return VariableEffect.NONE;
 
 
 		case LN:
 		case NNf:
 			MDDVariable var = getNodeVariable(high);
 			int curChild = getChild(high, 0);
 			VariableEffect effect = lookupEffect(low, curChild);
 			for (int value=1 ; value < var.nbval ; value++) {
 				int nextChild = getChild(high, value);
 				if (nextChild != curChild) {
 					curChild = nextChild;
 					effect = effect.combine( lookupEffect(low, nextChild) );
 					if (effect == VariableEffect.DUAL) {
 						return effect;
 					}
 				}
 			}
 			return effect;
 
 
 		case NL:
 		case NNn:
 			var = getNodeVariable(low);
 			curChild = getChild(low, 0);
 			effect = lookupEffect(curChild, high);
 			for (int value=1 ; value < var.nbval ; value++) {
 				int nextChild = getChild(low, value);
 				if (nextChild != curChild) {
 					curChild = nextChild;
 					effect = effect.combine( lookupEffect(nextChild, high) );
 					if (effect == VariableEffect.DUAL) {
 						return effect;
 					}
 				}
 			}
 			return effect;
 
 
 		case NN:
 			var = getNodeVariable(high);
 			curChild = getChild(high, 0);
 			int curChildLow = getChild(low, 0);
 			effect = lookupEffect(curChildLow, curChild);
 			for (int value=1 ; value < var.nbval ; value++) {
 				int nextChild = getChild(high, value);
 				int nextChildLow = getChild(low, value);
 				if (nextChild != curChild || nextChildLow != curChildLow) {
 					curChild = nextChild;
 					curChildLow = nextChildLow;
 					effect = effect.combine( lookupEffect(nextChildLow, nextChild) );
 					if (effect == VariableEffect.DUAL) {
 						return effect;
 					}
 				}
 			}
 			return effect;
 
 
 		default:
 			throw new RuntimeException("Invalid node relation");
 		}
 		
 	}
 
 	/* ***************** DEBUG ********************** */
 	/**
 	 * Debug helper: print a MDD on standard output.
 	 * @param node  the node index
 	 */
 	public void printNode(int node) {
 		print(node, "");
 	}
 	
 	private void print(int node, String prefix) {
 		if (isleaf(node)) {
 			System.out.println(prefix+node);
 			return;
 		}
 		MDDVariable var = variables[blocs[node]];
 		System.out.println(prefix+var.key);
 		prefix += "   ";
 		for (int i=0 ; i<var.nbval ; i++) {
 			print(blocs[node+INC_VALUES+i], prefix);
 		}
 	}
 	
 
 	/**
 	 * print raw data structure (hashcodes, hashitems and data blocs).
 	 * A must if you enjoy reading boring series of numbers. 
 	 */
 	public void debug() {
 		System.out.println("------------------------------------------------------------");
 		System.out.println("Raw factory info: ");
 		System.out.println("    "+nbleaves  + " leaves -- " + blocsize + " cell per bloc"); 
 		System.out.println("    "+nbnodes + " nodes" );
 		System.out.print("Hashes: ");
 		prettyPrintArray(hashcodes,0,2,-1);
 		System.out.print("HList:  ");
 		prettyPrintArray(hashitems, 0, 2, lastitem);
 		System.out.print("Data:   ");
 		prettyPrintArray(blocs, nbleaves, blocsize, lastbloc);
 		System.out.println("------------------------------------------------------------");
 	}
 
 	/**
 	 * Debug helper: Pretty printer for the Array used as storage.
 	 *  
 	 * @param a		the array
 	 * @param skip	number of elements to skip at the beginning
 	 * @param bs	block size
 	 * @param last	last element to print
 	 */
 	private void prettyPrintArray(int[] a, int skip, int bs, int last) {
 		if (last == -1) {
 			last = a.length;
 		}
 		for (int i=0 ; i<last ; i++) {
 			int b = i-skip;
 			if (b>=0 && b%bs == 0) {
 				System.out.print("| ");
 			}
 			System.out.print(a[i]+" ");
 		}
 		System.out.println();
 	}
 }
