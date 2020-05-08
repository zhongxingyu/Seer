 package ab5_adts;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import ab1_adts.statmodule.AverageVariance;
 import ab5_adts.ListRec.IList;
 import ab5_adts.ListRec.MLinkedList;
 
 public class ListRecIter {
 
 	IList<Integer> itr;
 	IList<Integer> rec;
 
 	AverageVariance itrStats;
 	AverageVariance recStats;
 
 	private static int NoOfElements = 500;
 	private static int NoOfRepeats = 100;
 
 	@Test
 	public void test_experimente() { // Aufgabe 5.1.3:
 		
 		
 		// Frage an Prof. Khler:
 		// Haben wir hier auch die Experimentergebnisse
 		// durch Diagramme und Auswertungen zu diskutieren
 		// oder ist random Testing und ImplementationsTesting
 		// der Zweck dieser Aufgabe?
 		
 		
 		initStats();
 		for (int t = NoOfRepeats; t > 0; t--) {
 
 			// reset both lists
 			itr = new MLinkedList<>();
 			rec = new MLinkedList<>();
 			
 			cons(0);// initialize with one element
 			// because insert at index 0 is disallowed
 			
 			for (int i = 0; i < NoOfElements; i++) {
 				int pos = (int) Math.floor(Math.random() * i);
 				insert(i, pos);
 			}
 			addAccessCount();
 			
 			shouldBeSame();
 		}
 		
 		outStats();
 
 	}
 
 	private void outStats() {
 		System.out.println("NoOfElements: " + NoOfElements);
 		System.out.println("NoOfRepeats: " + NoOfRepeats);
		System.out.println("ItrAvg," + itrStats.getAverage());
		System.out.println("RecAvg," + recStats.getAverage());
 		System.out.println("Factor: "+recStats.getAverage()/itrStats.getAverage());
 	}
 
 	private void addAccessCount() {
 		itrStats.addValue(itr.accessCount());
 		recStats.addValue(rec.accessCount());
 	}
 
 	private void initStats() {
 		itrStats = new AverageVariance();
 		recStats = new AverageVariance();
 	}
 
 	// test whether new functions are working
 	@Test
 	public void test_both() {
 		itr = new MLinkedList<>();
 		rec = new MLinkedList<>();
 
 		// both lists are empty
 		shouldBeSame();
 
 		// cons 1. are both still same?
 		cons(1);
 		shouldBeSame();
 
 		// repeat again
 		cons(2);
 		shouldBeSame();
 
 		// does the tail work?
 		tail();
 		shouldBeSame();
 
 		// insert 3 at index 0 and check
 		insert(3, 0);
 		assertEquals((Integer) 3, itr.get(0));
 		assertEquals((Integer) 3, rec.get(0));
 		shouldBeSame();
 
 		// repeat with 4
 		insert(4, 0);
 		assertEquals((Integer) 4, itr.get(0));
 		assertEquals((Integer) 4, rec.get(0));
 		shouldBeSame();
 
 		// insert in-between
 		insert(5, 2);
 		shouldBeSame();
 
 		// insert in-between
 		insert(6, 2);
 		shouldBeSame();
 
 		// (btw, inserting at the end or in a empty list is not allowed)
 	}
 
 	// test tail negative
 	@Test(expected = java.lang.IndexOutOfBoundsException.class)
 	public void test_no_tail_rec() {
 		itr = new MLinkedList<>();
 		rec = new MLinkedList<>();
 		tail();
 	}
 
 	// test tail negative
 	@Test(expected = java.lang.IndexOutOfBoundsException.class)
 	public void test_no_tail_itr() {
 		itr = new MLinkedList<>();
 		rec = new MLinkedList<>();
 		tail();
 	}
 
 	private void shouldBeSame() {
 		assertEquals(itr.length(), rec.length());
 		assertEquals(itr, rec);
 	}
 
 	private void tail() {
 		itr.tail();
 		rec.tail();
 	}
 
 	private void cons(int i) {
 		itr.cons(i);
 		rec.cons(i);
 	}
 
 	private void insert(int i, int index) {
 		itr.insertIter(i, index);
 		rec.insert(i, index);
 	}
 
 	private void out() {
 		System.out.println("Iter: " + itr);
 		System.out.println("Rec: " + rec);
 	}
 
 }
