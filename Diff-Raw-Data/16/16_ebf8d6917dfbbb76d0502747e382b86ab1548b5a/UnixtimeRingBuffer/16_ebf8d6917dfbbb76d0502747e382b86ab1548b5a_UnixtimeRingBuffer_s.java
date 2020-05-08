 package org.veiset.coffew8.coffeepi;
 
 
 public class UnixtimeRingBuffer {
 	public final CoffeeState INITIAL_STATE = new CoffeeState(-1); 
 	
 	private final CoffeeState[] buffer;
 	private int position;
 
 	/**
 	 * 
 	 * @param bufferSize total elements in ring buffer
 	 */
 	public UnixtimeRingBuffer(int bufferSize) {
 		assert bufferSize > 0 : "precondition: size="+bufferSize;
 		
 		buffer = new CoffeeState[bufferSize];
 		for (int i = 0; i < bufferSize; i++) {
 			buffer[i] = INITIAL_STATE;
 		}
 		position = 0;
 
 		assert dataInvariant() : "postcondition: invariant";
 		assert buffer.length == bufferSize : "postcondition: stack.length=" +buffer.length + ", size="+bufferSize;
 	}
 
 	public boolean dataInvariant(){
 		return position >= 0 && position < size() && buffer.length > 0;
 	}
 
 	/**
 	 * 
 	 * @return number of elements in the ring buffer
 	 */
 	public int size() {
 		assert dataInvariant() : "pre-/postcondition: invariant";
 		return buffer.length;
 	}
 
 	/**
 	 * 
 	 * @return current position to newest added element
 	 */
 	public int getPosition() {
 		assert dataInvariant() : "pre-/postcondition: invariant";
 		return position;
 	}
 
 	/**
 	 * 
 	 * @param weight
 	 *            new state to take place of the oldest state in the ring buffer
 	 */
 	public synchronized void add(Integer weight) {
 		assert dataInvariant() : "precondition: invariant";
 		
 		increasePosition();
 		buffer[position] = new CoffeeState(weight);
 
 		assert dataInvariant() : "postcondition: invariant";
 	}
 
 	private synchronized void increasePosition() {
 		assert dataInvariant() : "precondition: invariant";
 		position += 1;
 		if (position == size()) {
 			position = 0;
 		}
 		assert dataInvariant() : "postcondition: invariant";
 	}
 
 	/**
 	 * 
 	 * @return most recent state
 	 */
 	public CoffeeState current() {
 		assert dataInvariant() : "pre-/postcondition: invariant";
 		return get(position);
 	}
 
 	/**
 	 * 
 	 * @param id
 	 *            element in the list, makes little sense to use this alone
 	 * @return state of given id
 	 */
 	private CoffeeState get(int id) {
 		assert dataInvariant() : "precondition: invariant";
 		assert id >= 0 && id < size() : "precondition: id="+id;
 		
 		try {
 			return buffer[id];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param coffeState
 	 * 
 	 * @return all data newer than given unixtime
 	 */
 	public CoffeeState[] getElementsAfter(CoffeeState coffeState) {
 		assert dataInvariant() : "precondition: invariant";
 		
 		int number = getNumberOfElementsNewerThan(coffeState);
		
 		assert number >= 0 && number < size() : "number="+number;
 		assert dataInvariant() : "postcondition: invariant";
 		return getLast(number);
 	}
 
 	/**
 	 * 
 	 * @param coffeeState
 	 *            Last time checked for data
 	 * @return id of last relevant data (or -1 if all?)
 	 */
 	private int getNumberOfElementsNewerThan(CoffeeState coffeeState) {
 		assert dataInvariant() : "precondition: invariant";
 		
 		int id;
 		int count = 0;
 		for (int i = 0; i < size(); i++) {
 			if (position - i >= 0) {
 				id = position - i;
 			} else {
 				id = (size() - i) + position;
 			}
 			
 			assert id >= 0 && id < size() : "id="+id;
 			
			if (get(id).newerThan(coffeeState)) {
 				assert count >= 0 && count < size() : "postcondition: count="+count;
 				assert dataInvariant() : "postcondition: invariant";
 				return count;
 			}
 			count++;
 		}
 		assert dataInvariant() : "postcondition: invariant"; 
 		return  size()-1;
 	}
 
 	/**
 	 * 
 	 * @param number
 	 * @return
 	 */
 	public CoffeeState[] getLast(int number) {
 		assert dataInvariant() : "precondition: invariant";
 		
 		if (number <= 0) {
 			return null;
 		}
 		if (number > size())
 			number = size();
 
 		assert number > 0 && number <= size() : "number="+number;
 		
 		CoffeeState[] range = new CoffeeState[number];
 
 		int id;
 		for (int i = 0; i < number; i++) {
 			if (position - i >= 0) {
 				id = position - i;
 			} else {
 				id = (size() - i) + position;
 			}
 			
 			assert id >= 0 && id < size() : "id="+id;
 			assert i >= 0 && i < range.length : "i="+i;
 			
 			range[i] = get(id);
 		}
 		return range;
 	}
 
 }
