 package dna.tests;
 
 import static org.junit.Assert.*;
 import static org.junit.Assume.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import dna.datastructures.*;
 import dna.graph.Element;
 import dna.graph.IElement;
 import dna.graph.edges.*;
 import dna.graph.nodes.*;
 import dna.util.Rand;
 import static org.mockito.Mockito.*;
 
 @RunWith(Parameterized.class)
 @SuppressWarnings("rawtypes")
 public class DatastructureTester {
 
 	private DataStructure dataStructure;
 	private Class<? extends Element> elementClass;
 	private static final Class[] elementClasses = {DirectedNode.class, DirectedDoubleWeightedNode.class,
 		UndirectedNode.class, UndirectedDoubleWeightedNode.class,
 		UndirectedEdge.class, UndirectedDoubleWeightedEdge.class,
 		DirectedEdge.class, DirectedDoubleWeightedEdge.class
 	};
 
 	public DatastructureTester(Class<?> d, Class<? extends Element> e) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		this.dataStructure = (DataStructure) d.getConstructor(Class.class).newInstance(e);
 		this.elementClass = e;
 	}
 
     @Parameterized.Parameters(name="{0} {1}")
 	public static Collection testPairs() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
     	Class[] dataStructures = {DArrayList.class, DArray.class, DHashSet.class, DHashMap.class, DLinkedList.class};
 
     	ArrayList<Object> result = new ArrayList<>();
     	for ( Class sD: dataStructures ) {
     		for ( Class sE: elementClasses) {
     			// Check whether we can store an object of sE in sD
     			if (( Node.class.isAssignableFrom(sE) && !INodeListDatastructure.class.isAssignableFrom(sD) ) ||
     				( Edge.class.isAssignableFrom(sE) && !IEdgeListDatastructure.class.isAssignableFrom(sD) ))
     				continue;
     			result.add(new Object[]{sD, sE});
     		}
     	}
     	
 		return result;
 	}
     
    @Test
     public void checkOnlyOneDatatype() {
     	IElement dummy;
     	int exceptionCounter = 0;
     	
     	/*
     	 * Checking whether add() throws the exception we want it to throw
     	 * does not cover all cases here - we want to ensure that really only
     	 * instances of one single element class can be stored, and *all* other
     	 * instance types are rejected
     	 * 
     	 * The exception counter will be increased for each *allowed* class and
     	 * each *rejected* class. There should be no gap in between...
     	 */ 
     	
     	for ( Class otherElementClass: elementClasses ) {
     		if (elementClass.isAssignableFrom(otherElementClass)) {
     			assertTrue("Datastructure " + dataStructure.getClass() + "[" + this.elementClass + "] can't store " + otherElementClass + " even if it should", dataStructure.canStore(otherElementClass));
     			exceptionCounter++;
     			continue;
     		}
     		dummy = mock(otherElementClass);
     		
     		boolean exceptionThrown = false;
 			assertFalse("Datastructure " + dataStructure.getClass() + "[" + this.elementClass + "/" + dataStructure.getDataType() + "] can store " + otherElementClass + " even if it shouldn't", dataStructure.canStore(otherElementClass));
     		
     		try {
     			dataStructure.add(dummy);
     		} catch ( RuntimeException e ) {
     			exceptionCounter++;
     			exceptionThrown = true;
     		}
     		
     		assertTrue("Whoopsie - whats up with " + otherElementClass, exceptionThrown);
     			/*
     			 * So, this is thrown when the datastructure is not allowed to be added to
     			 * that list, but no exception is thrown. Common error: add() has not called canAdd()
     			 */
     	}
     	  	
     	assertEquals(elementClasses.length, exceptionCounter);
     }
 
 	@Test(timeout=1500)
 	public void checkAddAndRemove() {
 		IElement dummy = mock(elementClass);
 		assertFalse(dataStructure.contains(dummy));
 		assertEquals(0, dataStructure.size());
 		dataStructure.add(dummy);
 		assertTrue(dataStructure.contains(dummy));
 		assertEquals(1, dataStructure.size());
 		dataStructure.remove(dummy);
 		assertFalse(dataStructure.contains(dummy));
 		assertEquals(0, dataStructure.size());
 	}
 	
 	@Test
 	public void checkMaxNodeIndexOnAddAndRemove() {
 		assumeTrue(dataStructure instanceof INodeListDatastructure);
 		assumeTrue(Node.class.isAssignableFrom(elementClass));
 		INodeListDatastructure tempDS = (INodeListDatastructure) dataStructure;
 		
 		Node[] dummies = new Node[10];
 		for (int i = 0; i < dummies.length; i++) {
 			dummies[i] = (Node) mock(this.elementClass);
 			when(dummies[i].getIndex()).thenReturn(i);
 			tempDS.add(dummies[i]);
 			assertEquals(i, tempDS.getMaxNodeIndex());
 		}
 		
 		/*
 		 * Since we don't know if the data structure really
 		 * used the maxNodeIndex property and did not return
 		 * the count property, we will run some more
 		 * sophisticated tests now
 		 */
 		
 		Node[] secondDummies = new Node[10];
 		int[] prevIndex = new int[10];
 		int lastIndex = tempDS.getMaxNodeIndex();
 		
 		for (int i = 0; i < secondDummies.length; i++) {
 			secondDummies[i] = (Node) mock(this.elementClass);
 			prevIndex[i] = lastIndex;
 			lastIndex = lastIndex + Rand.rand.nextInt(5000) + 3;
 			when(secondDummies[i].getIndex()).thenReturn(lastIndex);
 			tempDS.add(secondDummies[i]);
 			assertEquals(lastIndex, tempDS.getMaxNodeIndex());
 		}
 		
 		for (int i = secondDummies.length - 1; i >= 0; i--) {
 			tempDS.remove(secondDummies[i]);
 			assertEquals(prevIndex[i], tempDS.getMaxNodeIndex());
 		}
 		
 		assertEquals(dummies[dummies.length-1].getIndex(), tempDS.getMaxNodeIndex());
 	}
 	
 	@Test
 	public void checkCorrectSize() {
 		IElement[] dummies = new IElement[10];
 		for (int i = 0; i < dummies.length; i++) {
 			dummies[i] = mock(this.elementClass);
 			if (Node.class.isAssignableFrom(this.elementClass))
 				when(((Node) dummies[i]).getIndex()).thenReturn(i);
 			dataStructure.add(dummies[i]);
 		}
 		assertEquals(dummies.length, dataStructure.size());		
 	}
 	
 	@Test
 	public void checkGetNode() {
 		assumeTrue(dataStructure instanceof INodeListDatastructureReadable);
 		assumeTrue(Node.class.isAssignableFrom(this.elementClass));
 		INodeListDatastructureReadable tempDS = (INodeListDatastructureReadable) dataStructure;
 		
 		Node dummy = (Node) mock(this.elementClass);
 		when(dummy.getIndex()).thenReturn(42);
 		tempDS.add(dummy);
 		
 		assertEquals(null, tempDS.get(43));
 		assertEquals(42, dummy.getIndex());
 		assertEquals(dummy, tempDS.get(42));
 	}
 	
 	@Test
 	public void checkGetElements() {
 		assumeTrue(dataStructure instanceof INodeListDatastructureReadable);
 		IReadable tempDS = (IReadable) dataStructure;		
 		
 		int size = 20;
 		IElement singleDummy;
 		
 		ArrayList<IElement> dummies = new ArrayList<>();
 		for (int i = 0; i < size; i++) {
 			singleDummy = mock(this.elementClass);
 			
 			/*
 			 * Nodes are stored with an index, so set it please!
 			 */
 			if ( singleDummy instanceof Node ) {
 				when(((Node) singleDummy).getIndex()).thenReturn(i);
 			}
 			
 			tempDS.add(singleDummy);
 			dummies.add(singleDummy);
 		}
 		
 		Collection<IElement> elements = tempDS.getElements();
 		
 		/*
 		 * Check for the proper size and content
 		 */
 		assertEquals(dummies.size(), elements.size());
 		assertTrue(dummies.containsAll(elements));
 		assertTrue(elements.containsAll(dummies));
 	}
 	
 	@Test
 	public void checkResizement() {
 		int initialSize = dataStructure.getDefaultSize();
 		int goalSize = 10 * initialSize;
 		
 		assertEquals(0, dataStructure.size());
 		
 		IElement singleDummy;
 		
 		for (int i = 0; i < goalSize; i++) {
 			singleDummy = mock(this.elementClass);
 			
 			/*
 			 * Nodes are stored with an index, so set it please!
 			 */
 			if ( singleDummy instanceof Node ) {
 				when(((Node) singleDummy).getIndex()).thenReturn(i);
 			}
 			
 			dataStructure.add(singleDummy);
 		}
 		assertEquals(goalSize, dataStructure.size());
 	}
 	
 	@Test
 	public void checkIterator() {
 		int size = 20;
 		IElement singleDummy;
 		
 		ArrayList<IElement> dummies = new ArrayList<>();
 		for (int i = 0; i < size; i++) {
 			singleDummy = mock(this.elementClass);
 			
 			/*
 			 * Nodes are stored with an index, so set it please!
 			 */
 			if ( singleDummy instanceof Node ) {
 				when(((Node) singleDummy).getIndex()).thenReturn(i);
 			}
 			
 			dataStructure.add(singleDummy);
 			dummies.add(singleDummy);
 		}
 		
 		Iterator<IElement> elemIterator = dataStructure.iterator();
 		while (elemIterator.hasNext()) {
 			singleDummy = elemIterator.next();
 			assertTrue(dummies.contains(singleDummy));
 			dummies.remove(singleDummy);
 		}
 		assertTrue(dummies.isEmpty());
 	}
 
 	@Test
 	public void checkGetNodeWithGaps() {
 			/*
 			 * Don't run this test for non-node datastructures and
 			 * non-nodes
 			 */
 		assumeTrue(dataStructure instanceof INodeListDatastructureReadable);
 		assumeTrue(Node.class.isAssignableFrom(this.elementClass));
 		
 		INodeListDatastructureReadable tempDS = (INodeListDatastructureReadable) dataStructure;
 		
 		Node dummy1 = (Node) mock(this.elementClass);
 		when(dummy1.getIndex()).thenReturn(0);
 		assertTrue(tempDS.add(dummy1));
 			
 		assertEquals(null, tempDS.get(1));
 		assertEquals(0, dummy1.getIndex());
 		assertEquals(dummy1, tempDS.get(0));
 		
 		Node dummy2 = (Node) mock(this.elementClass);
 		when(dummy2.getIndex()).thenReturn(1);
 		assertTrue(tempDS.add(dummy2));
 		
 		assertEquals(1, dummy2.getIndex());
 		assertEquals(dummy2, tempDS.get(1));		
 		
 		// Remove first node and get second one
 		assertTrue(tempDS.remove(dummy1));
 		assertEquals(1, tempDS.size());
 		assertEquals(dummy2, tempDS.get(1));
 	}
 	
 	@Test
 	public void checkGetRandom() {
 		assumeTrue(dataStructure instanceof INodeListDatastructureReadable);
 		IReadable tempDS = (IReadable) dataStructure;
 		
 		IElement[] dummies = new IElement[10];
 		for (int i = 0; i < dummies.length; i++) {
 			dummies[i] = mock(elementClass);
 			tempDS.add(dummies[i]);
 		}
 		
 		IElement random;
 		for ( int i = 0; i < 2 * dummies.length; i++) {
 			random = tempDS.getRandom();
 			assertTrue(tempDS.contains(random));
 		}
 	}
 	
 }
