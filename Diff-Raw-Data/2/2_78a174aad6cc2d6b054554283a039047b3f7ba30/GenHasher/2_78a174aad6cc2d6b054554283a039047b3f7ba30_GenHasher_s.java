 package edu.berkeley.gamesman.hasher.genhasher;
 
 import edu.berkeley.gamesman.util.qll.Factory;
 import edu.berkeley.gamesman.util.qll.Pool;
 import edu.berkeley.gamesman.hasher.cachehasher.CacheMove;
 
 /**
  * A very generalized hasher for sequences where it's possible to count the
  * number of ways of completing a given sequence subject to constraints
  * 
  * @author dnspies
  * @param <S>
  *            The state type to be hashed
  */
 public abstract class GenHasher<S extends GenState> {
 
 	private static boolean superAsserts = false;
 	/**
 	 * The length of the sequences
 	 */
 	public final int numElements;
 	/**
 	 * The number of possible digits for each element of the sequence
 	 */
 	public final int digitBase;
 
 	private long allPositions = -1L;
 
 	private final Pool<S> statePool = new Pool<S>(new Factory<S>() {
 		@Override
 		public S newObject() {
 			return newState();
 		}
 
 		@Override
 		public void reset(S t) {
 			assert validTest(t);
 		}
 	});
 	private final Pool<S> interStatePool = new Pool<S>(new Factory<S>() {
 		@Override
 		public S newObject() {
 			return newState();
 		}
 
 		@Override
 		public void reset(S t) {
 		}
 	});
 
 	/**
 	 * @param numElements
 	 *            The length of the sequences
 	 * @param digitBase
 	 *            The number of possible digits for each element of the sequence
 	 */
 	public GenHasher(int numElements, int digitBase) {
 		this.numElements = numElements;
 		this.digitBase = digitBase;
 	}
 
 	/**
 	 * Hashes a given state
 	 * 
 	 * @param state
 	 *            The state to be hashed
 	 * @return The hash of the state
 	 */
 	public final long hash(S state) {
 		return hash(state, numElements);
 	}
 
 	/**
 	 * Returns the hash of the pieces up to suffixStart
 	 * 
 	 * @param state
 	 *            The state to hash
 	 * @param suffixStart
 	 *            The place to stop hashing at (exclusive)
 	 * @return The hash of the pieces up to the start of the suffix
 	 */
 	final long hash(S state, int suffixStart) {
 		assert validTest(state);
 		long hash = innerHash(state, suffixStart);
 		assert validTest(state);
 		return hash;
 	}
 
 	/**
 	 * @param state
 	 *            If debugging is turned on, tests that the state is valid
 	 * @return Whether the state is valid
 	 */
 	protected final boolean validTest(S state) {
 		return !superAsserts || totalValid(state);
 	}
 
 	/**
 	 * Tests validity of the entire state (without making any assumtions).
 	 * Override this if valid() doesn't sufficiently check any state. It will
 	 * greatly simplify debugging
 	 * 
 	 * @param state
 	 *            The state to test
 	 * @return Whether the state is valid
 	 */
 	protected boolean totalValid(S state) {
 		return valid(state);
 	}
 
 	private final boolean validPrefTest(S state) {
 		return !superAsserts || totalValidPref(state);
 	}
 
 	/**
 	 * Tests validity of state prefix without making assumptions. Override this
 	 * if validPref() doesn't sufficiently check any prefix. It will greatly
 	 * simplify debugging.
 	 * 
 	 * @param state
 	 *            The prefix to test
 	 * @return Whether the prefix is valid
 	 */
 	protected boolean totalValidPref(S state) {
 		return validPref(state);
 	}
 
 	/**
 	 * Compute the hash of the state. This method may be overridden for
 	 * efficiency purposes. It will be called by the hash method.
 	 * 
 	 * @param state
 	 *            The state to hash
 	 * @param suffixStart
 	 *            The place to stop hashing at
 	 * @return The hash of the state
 	 */
 	protected long innerHash(S state, int suffixStart) {
 		S tempState = getPoolPref();
 		tempState.set(state);
 		long total = 0L;
		while (tempState.leastSig() < suffixStart) {
 			total += sigValue(tempState);
 			tempState.trunc();
 		}
 		releasePref(tempState);
 		return total;
 	}
 
 	/**
 	 * @param state
 	 *            Computes the hash contribution for the lowest digit in this
 	 *            prefix
 	 * @return The amount which this digit contributes to the hash
 	 */
 	protected long sigValue(S state) {
 		int val = state.leastSig();
 		long result = 0L;
 		state.resetLS(false);
 		while (state.leastSig() < val) {
 			result += countCompletions(state);
 			boolean incred = state.incr(1);
 			assert incred;
 		}
 		assert state.leastSig() == val;
 		return result;
 	}
 
 	/**
 	 * Unhashes a state and stores it in fillState
 	 * 
 	 * @param hash
 	 *            The hash of the state
 	 * @param fillState
 	 *            A state object to be filled
 	 */
 	public final void unhash(long hash, S fillState) {
 		innerUnhash(hash, fillState);
 		assert validTest(fillState);
 	}
 
 	/**
 	 * Unhashes a state and stores it in fillState. This method may be
 	 * overridden for efficiency purposes. It will be called by unhash.
 	 * 
 	 * @param hash
 	 *            The hash of the state
 	 * @param fillState
 	 *            A state object to be filled
 	 */
 	protected void innerUnhash(long hash, S fillState) {
 		fillState.clear();
 		while (!fillState.isComplete()) {
 			fillState.addOn(false);
 			hash -= raiseLS(fillState, hash);
 			assert validPrefTest(fillState);
 		}
 		assert hash == 0;
 	}
 
 	/**
 	 * Determines the next digit for a given prefix and remaining hash to be
 	 * used
 	 * 
 	 * @param state
 	 *            The state to add the next digit to
 	 * @param hash
 	 *            The remaining amount of hash to be used
 	 * @return The amount of hash which has been used
 	 */
 	protected long raiseLS(S state, long hash) {
 		long result = 0L;
 		long countPositions = countCompletions(state);
 		while (hash >= result + countPositions) {
 			result += countPositions;
 			boolean incred = state.incr(1);
 			assert incred;
 			countPositions = countCompletions(state);
 		}
 		return result;
 	}
 
 	/**
 	 * Takes a state with hash h and modifies it so it hashes to h+1
 	 * 
 	 * @param state
 	 *            The state to modify
 	 * @return The index n of the smallest-index piece such that for all m>=n
 	 *         piece m was not changed.
 	 */
 	public final int step(S state) {
 		return step(state, 1);
 	}
 
 	/**
 	 * Takes a state with hash h and modifies it so it hashes to h+dir where dir
 	 * = +/- 1
 	 * 
 	 * @param state
 	 *            The state to modify
 	 * @param dir
 	 *            The direction to step
 	 * @return The index n of the smallest-index piece such that for all m>=n
 	 *         piece m was not changed.
 	 */
 	public final int step(S state, int dir) {
 		assert validTest(state);
 		int result = innerStep(state, dir);
 		assert validTest(state);
 		return result;
 	}
 
 	/**
 	 * Takes a state with hash h and modifies it so it hashes to h+dir where dir
 	 * = +/- 1. You may override this method for efficiency purposes. It will be
 	 * called by step.
 	 * 
 	 * @param state
 	 *            The state to modify
 	 * @param dir
 	 *            The direction to step
 	 * @return The index n of the smallest-index piece such that for all m>=n
 	 *         piece m was not changed.
 	 */
 	protected int innerStep(S state, int dir) {
 		return basicStep(state, dir);
 	}
 
 	// Handles the case where state is invalid
 	private int basicStep(S state, int dir) {
 		assert dir == 1 || dir == -1;
 		if (state.isEmpty())
 			return -1;
 		boolean incred = state.incr(dir);
 		int result;
 		if (incred) {
 			while (!validPref(state)) {
 				incred = state.incr(dir);
 				if (!incred)
 					break;
 			}
 		}
 		if (incred)
 			result = state.getStart() + 1;
 		else {
 			state.trunc();
 			result = basicStep(state, dir);
 			addValid(state, dir == -1);
 		}
 		assert validPrefTest(state);
 		return result;
 	}
 
 	/**
 	 * Returns whether the given state prefix is a prefix for any valid state
 	 * 
 	 * @param state
 	 *            The prefix
 	 * @return Whether there exists a state for which this prefix is valid
 	 */
 	protected boolean validPref(S state) {
 		return countCompletions(state) > 0;
 	}
 
 	/**
 	 * Returns whether the given state is valid. This method should only be
 	 * called for complete states
 	 * 
 	 * @param state
 	 *            The state
 	 * @return Whether it's valid
 	 */
 	protected boolean valid(S state) {
 		assert state.isComplete();
 		return validPref(state);
 	}
 
 	/**
 	 * Adds the next valid element onto the end of the state. This should only
 	 * be called if validPref(state) would return true
 	 * 
 	 * @param state
 	 *            The state to append to
 	 * @param startHigh
 	 *            whether to start counting down from the top or (if false)
 	 *            counting up from the bottom
 	 */
 	protected final void addValid(S state, boolean startHigh) {
 		state.addOn(startHigh);
 		boolean inced = incToValid(state, startHigh ? -1 : 1);
 		assert inced;
 		assert validPrefTest(state);
 	}
 
 	/**
 	 * Resets the first element to the first valid digit. This should only be
 	 * called if trunc(state) followed by validPref(state) would return true
 	 * 
 	 * @param state
 	 *            The state to reset
 	 * @param startHigh
 	 *            Whether to start counting down from the top or (if false)
 	 *            counting up from the bottom
 	 */
 	protected final void resetValid(S state, boolean startHigh) {
 		state.resetLS(startHigh);
 		boolean inced = incToValid(state, startHigh ? -1 : 1);
 		assert inced;
 		assert validPrefTest(state);
 	}
 
 	/**
 	 * Increments the current digit until it reaches a position for which this
 	 * prefix is valid.
 	 * 
 	 * @param state
 	 *            The state to increment
 	 * @param dir
 	 *            The direction (-1 or 1)
 	 * @return true if it reaches a valid prefix. false if it exhausts all the
 	 *         remaining digits
 	 */
 	protected boolean incToValid(S state, int dir) {
 		while (!validPref(state)) {
 			boolean incred = state.incr(dir);
 			if (!incred)
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Adds the lowest valid remaining digits on to make a valid complete
 	 * sequence
 	 * 
 	 * @param state
 	 *            The state to modify
 	 * @param startHigh
 	 *            Whether to add the highest possible digits or the lowest
 	 *            possible digits
 	 */
 	protected void validComplete(S state, boolean startHigh) {
 		while (!state.isComplete()) {
 			addValid(state, startHigh);
 		}
 		assert validTest(state);
 	}
 
 	/**
 	 * Counts the number of possible positions which have the given state prefix
 	 * (state.startAt indicates where the prefix starts).
 	 * 
 	 * @param state
 	 *            The prefix to count.
 	 * @return The number of ways of completing this prefix
 	 */
 	protected final long countCompletions(S state) {
 		if (state.validLS())
 			return innerCountCompletions(state);
 		else
 			return 0;
 	}
 
 	/**
 	 * Counts the number of ways of completing a given state-prefix. This method
 	 * is crucial to being able to hash. Note that if state is complete, then
 	 * this method should return 1 (for valid) or 0 (for invalid).
 	 * 
 	 * @param state
 	 *            A (possibly incomplete) state
 	 * @return The number of positions possible with the given state prefix
 	 */
 	protected abstract long innerCountCompletions(S state);
 
 	/**
 	 * @return The total number of positions hashed by this hasher
 	 */
 	public final long totalPositions() {
 		if (allPositions == -1) {
 			S state = getPoolPref();
 			state.clear();
 			allPositions = countCompletions(state);
 			releasePref(state);
 		}
 		return allPositions;
 	}
 
 	/**
 	 * @return A new complete valid instance of type S
 	 */
 	public final S newState() {
 		S res = innerNewState();
 		assert validTest(res);
 		return res;
 	}
 
 	/**
 	 * Must return a complete state
 	 * 
 	 * @return Returns a new state
 	 */
 	protected abstract S innerNewState();
 
 	public static void enableToughAsserts() {
 		superAsserts = true;
 	}
 
 	/**
 	 * @return A state from the inner state pool. Use release to return it to
 	 *         the pool.
 	 */
 	public final S getPoolState() {
 		S state = statePool.get();
 		assert validTest(state);
 		return state;
 	}
 
 	/**
 	 * @return A state from the prefix pool. This one is not validated so it may
 	 *         be bad and doesn't need to be reset before being returned.
 	 */
 	protected final S getPoolPref() {
 		return interStatePool.get();
 	}
 
 	/**
 	 * Releases a state back to the pool. It should be valid and complete
 	 * 
 	 * @param poolState
 	 *            The state to release
 	 */
 	public final void release(S poolState) {
 		assert validTest(poolState);
 		assert poolState.hasHasher(this);
 		statePool.release(poolState);
 	}
 
 	/**
 	 * Releases a state back to the prefix pool. It does not need to be valid or
 	 * complete.
 	 * 
 	 * @param poolPref
 	 *            The state to release.
 	 */
 	protected final void releasePref(S poolPref) {
 		interStatePool.release(poolPref);
 	}
 
 	/**
 	 * @param state
 	 *            The state
 	 * @return The place where the prefix starts for this state
 	 */
 	protected final int getStart(GenState state) {
 		return state.getStart();
 	}
 
 	/**
 	 * This is equivalent to getStart(state)==0
 	 * 
 	 * @param state
 	 *            The state
 	 * @return Whether the state is complete.
 	 */
 	protected final boolean isComplete(S state) {
 		return state.isComplete();
 	}
 
 	/**
 	 * This is equivalent to get(getStart(state))
 	 * 
 	 * @param state
 	 *            The state
 	 * @return The value of the element at the start of this prefix
 	 */
 	protected final int leastSig(S state) {
 		return state.leastSig();
 	}
 
 	/**
 	 * Adds another element onto the prefix. (ie state.start--)
 	 * 
 	 * @param state
 	 *            The state to add to
 	 * @param startHigh
 	 *            Whether to make that element as high as possible (true) or as
 	 *            low as possible (false)
 	 */
 	protected final void addOn(S state, boolean startHigh) {
 		state.addOn(startHigh);
 	}
 
 	/**
 	 * Increments leastSig(state) by dir (dir==1 or -1) (This may be overridden
 	 * to increment to the next valid position)
 	 * 
 	 * @param state
 	 *            The state to increment
 	 * @param dir
 	 *            The direction to go (up or down)
 	 * @return Whether this digit is still in [0,digBase).
 	 */
 	protected final boolean incr(S state, int dir) {
 		return state.incr(dir);
 	}
 
 	/**
 	 * Removes an element from the prefix. (ie state.start++)
 	 * 
 	 * @param state
 	 *            The state
 	 */
 	protected final void trunc(S state) {
 		state.trunc();
 	}
 
 	/**
 	 * Removes all elements up to place from the prefix. <br />
 	 * <code>
 	 * while (getStart(state) &lt place) {<br />
 	 * &nbsp trunc(state);<br />
 	 * }<br />
 	 * </code>
 	 * 
 	 * @param state
 	 *            The state
 	 * @param place
 	 *            The place
 	 */
 	protected final void trunc(S state, int place) {
 		state.trunc(place);
 	}
 
 	/**
 	 * Adds ls onto the prefix (ie state[state.start--]=ls)
 	 * 
 	 * @param state
 	 *            The prefix to add to
 	 * @param ls
 	 *            The element to add
 	 */
 	protected final void addLS(S state, int ls) {
 		state.addLS(ls);
 	}
 
 	/**
 	 * Resets the prefix element at start to 0 or digBase-1 (may be overridden
 	 * to find first valid position)
 	 * 
 	 * @param state
 	 *            The state
 	 * @param startHigh
 	 *            Whether to go to digBase-1 (high=true) or 0 (low=false);
 	 */
 	protected final void resetLS(S state, boolean startHigh) {
 		state.resetLS(startHigh);
 	}
 
 	/**
 	 * Determines if this prefix is empty (ie getStart(state)==numElements)
 	 * 
 	 * @param state
 	 *            The state
 	 * @return If it's empty
 	 */
 	protected final boolean isEmpty(S state) {
 		return state.isEmpty();
 	}
 
 	public static boolean useToughAsserts() {
 		return superAsserts;
 	}
 
 	/**
 	 * Takes a separate state (possibly from another hasher) and makes a move on
 	 * that state which it stores in childState
 	 * 
 	 * @param firstState
 	 *            The parent
 	 * @param move
 	 *            The move
 	 * @param childState
 	 *            The state in which to store the result
 	 */
 	public final void makeMove(GenState firstState, CacheMove move, S childState) {
 		childState.setOther(firstState);
 		// Theoretically someone could override matchSeq(s) in order to modify
 		// firstState. This would not be appropriately checked. There doesn't
 		// seem to be any way around that.
 		makeMove(childState, move);
 		assert validTest(childState);
 	}
 
 	/**
 	 * Steps steppingState until it reaches parentState+move. Returns the amount
 	 * by which the hash changes as a result.
 	 * 
 	 * @param steppingState
 	 *            The state to be modified
 	 * @param parentHasher
 	 *            The hasher for the parent state
 	 * @param parentState
 	 *            The parent state
 	 * @param move
 	 *            The move to search for
 	 * @param same
 	 *            The point at which all elements are known to be the same
 	 *            passed here (so nothing needs to be incremented)
 	 * @param dir
 	 *            The direction in which to step
 	 * @return The amount by which the hash changed<br />
 	 *         TODO Check this is correct when dir==-1
 	 */
 	public final <T extends GenState> long stepTo(S steppingState,
 			GenHasher<T> parentHasher, T parentState, CacheMove move, int same,
 			int dir) {
 		assert !useToughAsserts() || steppingState.matches(parentState, same);
 		long diff = 0;
 		for (int i = 0; i < same - 1; i++) {
 			diff -= sigValue(steppingState);
 			steppingState.trunc();
 		}
 		parentHasher.makeMove(parentState, move);
 		while (true) {
 			while (steppingState.leastSig() != parentState.get(steppingState
 					.getStart())) {
 				diff += countCompletions(steppingState);
 				boolean incred = steppingState.incr(dir);
 				assert incred;
 			}
 			if (steppingState.isComplete())
 				break;
 			steppingState.addOn(dir == -1);
 		}
 		parentHasher.unmakeMove(parentState, move);
 		assert parentHasher.validTest(parentState);
 		assert validTest(steppingState);
 		return diff;
 	}
 
 	/**
 	 * Puts a lower bound on the hash of the next child from move given a
 	 * starting parent state (or upper bound on hash of previous child if
 	 * dir==-1). In the general case the problem of finding it exactly appears
 	 * to be NP-complete (TODO Prove this) which is why this method only finds a
 	 * bound rather than finding it exactly although most specific instances
 	 * appear to have a polynomial-time shortcut. This method guarantees that if
 	 * you call it with dir == 1 for the start of a range and dir==-1 for the
 	 * end of a range, the produced range of children will be on the order of
 	 * the initial range.<br />
 	 * In particular if the start and end share a prefix which invalidates the
 	 * move, the returned range will have negative length
 	 * 
 	 * @param parentHasher
 	 *            The hasher for the parent state
 	 * @param parentState
 	 *            The parent state
 	 * @param move
 	 *            The move to search for
 	 * @param dir
 	 *            The direction in which to search
 	 * @return The lower/upper bound for the child
 	 */
 	public final <T extends GenState> long getChildBound(
 			GenHasher<T> parentHasher, T parentState, CacheMove move, int dir) {
 		assert dir == 1 || dir == -1;
 		assert parentHasher.validTest(parentState);
 		int pMove = move.numChanges - 1;
 		S childState = getPoolPref();
 		childState.clear();
 		boolean clobbered = false;
 		while (validPref(childState) && !childState.isComplete()) {
 			int nextStart = childState.getStart() - 1;
 			int nextLS = parentState.get(nextStart);
 			if (pMove >= 0 && move.getChangePlace(pMove) == nextStart) {
 				int changeFrom = move.getChangeFrom(pMove);
 				int changeTo = move.getChangeTo(pMove);
 				if (nextLS == changeFrom) {
 					childState.addLS(changeTo);
 				} else {
 					if (dir == -1 && changeFrom > nextLS || dir == 1
 							&& changeFrom < nextLS) {
 						clobbered = innerStep(childState, dir) == -1;
 					}
 					if (!clobbered)
 						childState.addLS(changeTo);
 					break;
 				}
 				pMove--;
 			} else
 				childState.addLS(nextLS);
 		}
 		final long result;
 		if (clobbered)
 			result = -1;
 		else if (isComplete(childState) && valid(childState)) {
 			result = hash(childState);
 		} else {
 			final boolean isValid = validPref(childState)
 					|| basicStep(childState, dir) != -1;
 			if (isValid) {
 				assert validPrefTest(childState);
 				validComplete(childState, dir == -1);
 				assert validTest(childState);
 				result = hash(childState);
 			} else
 				result = -1;
 		}
 		releasePref(childState);
 		return result;
 	}
 
 	private final void makeMove(GenState state, CacheMove move) {
 		for (int i = 0; i < move.numChanges; i++) {
 			if (state.get(move.getChangePlace(i)) == move.getChangeFrom(i))
 				state.set(move.getChangePlace(i), move.getChangeTo(i));
 			else
 				throw new Error("Cannot make this move");
 		}
 	}
 
 	private final void unmakeMove(GenState state, CacheMove move) {
 		for (int i = 0; i < move.numChanges; i++) {
 			if (state.get(move.getChangePlace(i)) == move.getChangeTo(i))
 				state.set(move.getChangePlace(i), move.getChangeFrom(i));
 			else
 				throw new Error("Cannot unmake this move");
 		}
 	}
 
 	/**
 	 * Sets the state's internal ints to match seq
 	 * 
 	 * @param state
 	 *            The state to set
 	 * @param seq
 	 *            The sequence to match
 	 */
 	public final void set(S state, int[] seq) {
 		state.set(seq, 0);
 		assert validTest(state);
 	}
 
 	/**
 	 * Returns whether get(state,getStart(state)) is in [0,digBase)
 	 * 
 	 * @param state
 	 *            The state
 	 * @return Whether the LS is a valid digit
 	 */
 	protected final boolean validLS(S state) {
 		return state.validLS();
 	}
 }
