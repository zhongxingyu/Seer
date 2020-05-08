 package edu.calpoly.stat312.StructuresDuel;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class TestCase {
 	private static long currentID = 0;
 	private static RandomData rand = new RandomData();
 
 	/**
 	 * An identifier for this test run
 	 */
 	private long id;
 
 	// Information to run the test
 	/**
 	 * The type of data structure to use
 	 */
 	private Type type;
 	/**
 	 * The operation to do on the data structure
 	 */
 	private Operation op;
 	/**
 	 * The type of random data to use for the keys
 	 */
 	private DataType keyType;
 	/**
 	 * The type of random data to use for the values
 	 */
 	private DataType valueType;
 	/**
 	 * How much data to generate
 	 */
 	private int dataCount = -1;
 
 	/**
 	 * Alternative data to use to run the test (prevents generation of data)
 	 */
 	private Map<String, String> data;
 
 	/**
 	 * How long the test took
 	 */
 	private long nanoTime = -1;
 
 	/**
 	 * Create a new TestCase with default values. Many things will need to be
 	 * set via setters
 	 */
 	public TestCase() {
 		id = currentID;
 		currentID++;
 	}
 
 	public TestCase(Type type) {
 		this.type = type;
 	}
 
 	public TestCase(Operation op) {
 		this.op = op;
 	}
 
 	public TestCase(DataType keyType, DataType valueType, int dataCount) {
 		this.keyType = keyType;
 		this.valueType = valueType;
 		this.dataCount = dataCount;
 	}
 
 	public TestCase(Map<String, String> data) {
 		this.data = data;
 	}
 
 	/**
 	 * Run this test, and return the result value as a long. Tests can only be
 	 * run once, to preserve the one-write characteristics of the time value.
 	 * Use duplicate() to get a fresh instance.
 	 * 
 	 * @return The time taken for this test in nanoseconds. The qualifications
 	 *         for Timer's elapsedNanoTime method apply.
 	 */
 	public long runTest() {
 		if (nanoTime >= 0) {
 			throw new IllegalOperationError("Cannot run test twice.");
 		}
 
		Map<String, String> tempData;

 		if (data == null) {
 			// Generate random data
			tempData = rand.getDataBlock(keyType, valueType, dataCount);
		} else {
			tempData = data;
 		}
 
 		// Run the test and get the result
		System.out.println(this);
		nanoTime = Tester.runTest(type, tempData, op).elapsedNanoTime();
		System.out.println(this);
 
 		return nanoTime;
 	}
 
 	public TestCase duplicate() {
 		TestCase t = new TestCase();
 
 		// Copy the value of each field
 		t.type = type;
 		t.op = op;
 
 		t.keyType = keyType;
 		t.valueType = valueType;
 		t.dataCount = dataCount;
 
 		t.data = data;
 
 		return t;
 	}
 
 	public static enum Part {
 		TYPE, OPERATION, GENSETTINGS, DATA;
 	}
 
 	public static List<TestCase> cartesianProd(List<TestCase> row,
 			List<TestCase> col) {
 		List<TestCase> prod = new ArrayList<>();
 
 		for (TestCase a : row) {
 			for (TestCase b : col) {
 				prod.add(combine(a, b));
 			}
 		}
 
 		return prod;
 	}
 
 	/**
 	 * Use each of the items in the lists as an instance of the specified part
 	 * and make a cartesian product
 	 * 
 	 * @param row
 	 * @param rowPart
 	 * @param col
 	 * @param colPart
 	 * @return
 	 */
 	public static List<TestCase> cartesianProd(List<TestCase> row,
 			Part rowPart, List<TestCase> col, Part colPart) {
 		List<TestCase> prod = new ArrayList<>();
 
 		for (TestCase r : row) {
 			for (TestCase c : col) {
 				prod.add(combine(r, rowPart, c, colPart));
 			}
 		}
 
 		return prod;
 	}
 
 	/**
 	 * 
 	 * This variant uses the items from main and crosses them with the items in
 	 * by, with respect to byPart.
 	 * 
 	 * @param main
 	 * @param by
 	 * @param byPart
 	 * @return
 	 */
 	public static List<TestCase> cartesianProd(List<TestCase> main,
 			List<TestCase> by, Part byPart) {
 		List<TestCase> prod = new ArrayList<>();
 
 		for (TestCase aItem : main) {
 			for (TestCase bItem : by) {
 				// combine always returns a new item
 				prod.add(combine(aItem, bItem, byPart));
 			}
 		}
 
 		return prod;
 	}
 
 	private static TestCase combine(TestCase a, TestCase b) {
 		TestCase combined = new TestCase();
 
 		// Copy the set attributes from a and b onto combined. If an attribute
 		// is set in both a and b, a's version will remain.
 		copyAttrib(b, combined);
 		copyAttrib(a, combined);
 
 		return combined;
 	}
 
 	private static TestCase combine(TestCase a, Part aPart, TestCase b,
 			Part bPart) {
 		TestCase combined = new TestCase();
 
 		// Copy the specified attributes
 		copyAttrib(b, combined, bPart);
 		copyAttrib(a, combined, aPart);
 
 		return combined;
 	}
 
 	/**
 	 * Create a clone of main and set bPart to the value found in b
 	 * 
 	 * @param main
 	 * @param b
 	 * @param bPart
 	 * @return
 	 */
 	private static TestCase combine(TestCase main, TestCase b, Part bPart) {
 		TestCase combined = main.duplicate();
 		copyAttrib(b, combined, bPart);
 		return combined;
 	}
 
 	/**
 	 * Copy only set attributes from <b>from</b> to <b>to</b>.
 	 * 
 	 * @param from
 	 *            The source TestCase
 	 * @param to
 	 *            The destination TestCase
 	 */
 	private static void copyAttrib(TestCase from, TestCase to) {
 		if (from.type != null) {
 			to.type = from.type;
 		}
 
 		if (from.op != null) {
 			to.op = from.op;
 		}
 
 		if (from.data != null) {
 			to.data = from.data;
 		}
 
 		if (from.keyType != null && from.valueType != null
 				&& from.dataCount >= 0) {
 			to.keyType = from.keyType;
 			to.valueType = from.valueType;
 			to.dataCount = from.dataCount;
 		}
 
 		return;
 	}
 
 	/**
 	 * Copy the specified part from <b>from</b> to <b>to</b>.
 	 * 
 	 * @param from
 	 *            The source TestCase
 	 * @param to
 	 *            The destination TestCase
 	 * @param part
 	 *            The part to copy
 	 */
 	private static void copyAttrib(TestCase from, TestCase to, Part part) {
 		switch (part) {
 		case TYPE:
 			to.type = from.type;
 			break;
 
 		case OPERATION:
 			to.op = from.op;
 			break;
 
 		case DATA:
 			to.data = from.data;
 			break;
 
 		case GENSETTINGS:
 			to.keyType = from.keyType;
 			to.valueType = from.valueType;
 			to.dataCount = from.dataCount;
 			break;
 		}
 
 		return;
 	}
 
 	/**
 	 * Returns a List containing each item in the provided list duplicated the
 	 * given number of times. All the items in the returned list are clones of
 	 * the ones in the provided list.
 	 * 
 	 * @param list
 	 *            The list of items to replicate
 	 * @param times
 	 *            The number of times to replicate each item
 	 * @return The resulting list
 	 */
 	public static List<TestCase> replicate(List<TestCase> list, int times) {
 		List<TestCase> replicated = new ArrayList<>();
 
 		for (TestCase item : list) {
 			// Add the number of duplicates requested
 			for (int i = 0; i < times; i++) {
 				replicated.add(item.duplicate());
 			}
 		}
 
 		return replicated;
 	}
 
 	/**
 	 * Shuffle the given list using the RandomData that is attached to this
 	 * TestCase.
 	 * 
 	 * @param list
 	 *            The list to shuffle
 	 */
 	public static <T> void shuffle(List<T> list) {
 		rand.shuffle(list);
 	}
 
 	/**
 	 * Run all the tests in the provided List in the order they are returned by
 	 * its iterator. Return all the results in a new list in the same order.
 	 * 
 	 * @param list
 	 *            The List to run tests from
 	 * @return A list containing all the result from the tests
 	 */
 	public static List<Long> runList(List<? extends TestCase> list) {
 		List<Long> res = new ArrayList<>();
 
 		for (TestCase t : list) {
 			res.add(t.runTest());
 		}
 
 		return res;
 	}
 
 	public static RandomData getRand() {
 		return rand;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		if (this.type != null) {
 			throw new IllegalOperationError("Type already set.");
 		}
 		this.type = type;
 	}
 
 	public Operation getOp() {
 		return op;
 	}
 
 	public void setOp(Operation op) {
 		if (this.op != null) {
 			throw new IllegalOperationError("Op already set.");
 		}
 		this.op = op;
 	}
 
 	public DataType getKeyType() {
 		return keyType;
 	}
 
 	public void setKeyType(DataType keyType) {
 		if (this.keyType != null) {
 			throw new IllegalOperationError("keyType already set.");
 		}
 		this.keyType = keyType;
 	}
 
 	public DataType getValueType() {
 		return valueType;
 	}
 
 	public void setValueType(DataType valueType) {
 		if (this.valueType != null) {
 			throw new IllegalOperationError("valueType already set.");
 		}
 		this.valueType = valueType;
 	}
 
 	public int getDataCount() {
 		return dataCount;
 	}
 
 	public void setDataCount(int dataCount) {
 		if (this.dataCount >= 0) {
 			throw new IllegalOperationError("dataCount already set.");
 		}
 		this.dataCount = dataCount;
 	}
 
 	public Map<String, String> getData() {
 		return data;
 	}
 
 	public void setData(Map<String, String> data) {
 		if (this.data != null) {
 			throw new IllegalOperationError("data already set.");
 		}
 		this.data = data;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public long getTime() {
 		if (nanoTime < 0) {
 			throw new UnrunTimerException(
 					"Test has not been run. There is no time to return.");
 		}
 
 		return nanoTime;
 	}
 
 	@Override
 	public String toString() {
 		// Format:
 		// 24: INSERT 24 RANDOM keys with RANDOM values into TRIE in 12.37s
 		StringBuilder sb = new StringBuilder();
 		sb.append(id);
 		sb.append(": ");
 
 		sb.append(op.toString());
 		sb.append(' ');
 
 		if (data == null) {
 			sb.append(dataCount);
 			sb.append(' ');
 			sb.append(keyType);
 			sb.append(" keys with ");
 			sb.append(valueType);
 			sb.append(" values ");
 		} else {
 			sb.append(data.size());
 			sb.append(" data points ");
 		}
 		
 		switch(op){
 		case INSERT:
 			// INSERT into
 			sb.append("into ");
 			break;
 		case RETRIEVE:
 		case DELETE:
 			// RETRIEVE from
 			// DELETE from
 			sb.append("from ");
 			break;
 		}
 
 		sb.append(type);
 
 		if (hasRun()) {
 			sb.append(" in ");
 			sb.append(nanoTime * 1e-9);
 			sb.append('s');
 		}
 
 		return sb.toString();
 	}
 
 	public boolean hasRun() {
 		return nanoTime >= 0;
 	}
 }
