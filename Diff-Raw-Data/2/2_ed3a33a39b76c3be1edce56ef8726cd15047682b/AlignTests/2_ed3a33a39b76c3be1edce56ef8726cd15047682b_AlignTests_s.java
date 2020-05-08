 package name.kazennikov.annotations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 
 /**
  * Alignement tests
  * @author Anton Kazennikov
  *
  */
 public class AlignTests extends TestCase {
 	private static String FOO = "foo";
 	private static String BAR = "bar";
 	
 	public Document prepareDoc1() {
 		Document doc = new Document("doc", "foobarbar foobar foobaz");
 
 		doc.addAnnotation(FOO, 0, 3);
 		doc.addAnnotation(FOO, 3, 4);
 		doc.addAnnotation(BAR, 0, 4);
 
 		doc.addAnnotation(FOO, 4, 6);
 		doc.addAnnotation(BAR, 4, 6);
 		
 		doc.addAnnotation(FOO, 6, 7);
 		doc.addAnnotation(BAR, 6, 7);
 		
 		// phantom node before actual node
 		doc.addAnnotation(FOO, 7, 7);
 		doc.addAnnotation(FOO, 7, 10);
 		doc.addAnnotation(BAR, 7, 7);
 		
 		// phantom node after actual node
 		doc.addAnnotation(FOO, 10, 11);
 		doc.addAnnotation(FOO, 11, 11);
 		doc.addAnnotation(BAR, 10, 11);
 
 
 		
 		doc.sortAnnotations();
 		return doc;
 	}
 	
 	@Test
 	public void testAlignmentGeneric() {
 		Document doc = prepareDoc1();
 		List<List<Annotation>> left = new ArrayList<List<Annotation>>();
 		List<List<Annotation>> right = new ArrayList<List<Annotation>>();
 		Align.getAligned(doc.get(FOO), doc.get(BAR), left, right);
 
 		assertEquals(true, left.size() == right.size());
 		assertEquals(2, left.get(0).size());
 		assertEquals(1, right.get(0).size());
 	}
 	
 	@Test
 	public void testAlignmentSimple() {
 		Document doc = prepareDoc1();
 		List<List<Annotation>> left = new ArrayList<List<Annotation>>();
 		List<List<Annotation>> right = new ArrayList<List<Annotation>>();
 		Align.getAligned(doc.get(FOO), doc.get(BAR), left, right);
 
 		assertEquals(true, left.size() == right.size());
 		assertEquals(1, left.get(1).size());
 		assertEquals(1, right.get(1).size());
 	}
 	
 	@Test
	public void testAlignmentSimple() {
 		Document doc = prepareDoc1();
 		List<List<Annotation>> left = new ArrayList<List<Annotation>>();
 		List<List<Annotation>> right = new ArrayList<List<Annotation>>();
 		Align.getAligned(doc.get(FOO), doc.get(BAR), left, right);
 
 		assertEquals(true, left.size() == right.size());
 		assertEquals(1, left.get(1).size());
 		assertEquals(1, right.get(1).size());
 	}
 
 
 
 }
