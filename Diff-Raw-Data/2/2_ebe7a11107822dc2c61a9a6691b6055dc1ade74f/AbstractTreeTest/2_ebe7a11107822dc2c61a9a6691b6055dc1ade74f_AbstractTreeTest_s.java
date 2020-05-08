 package eu.eyan.duplicate.lister.helper;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.ExpectedException;
 
 public abstract class AbstractTreeTest {
 
 	public static final int FILE_CONTENT_SEED = 123;
 	public static final int FILE_SIZE = 123;
 	
 	protected Fil sameFil;
 	protected String root;
 	protected TreeBuilder tree;
 	
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 	  
 	@Before
 	public void setUp(){
 		sameFil = new Fil("same.binary").withRandomBinaryContent(FILE_SIZE, FILE_CONTENT_SEED);
		root = "C:\\temp\\test";
 		tree = createTestTree(sameFil, root);
 	}
 	
 	private static TreeBuilder createTestTree(Fil sameFil, String root) {
 			TreeBuilder tree = new TreeBuilder(root)
 				.withDir(new Dir("1")
 					.withDir(new Dir("11")
 						.withFil(new Fil("11f1"))
 						.withFil(new Fil("11f2"))
 						.withFil(sameFil)
 					)
 					.withDir(new Dir("12"))
 					.withFil(new Fil("1f1"))
 					.withFil(new Fil("1f2"))
 					.withFil(sameFil)
 				)
 				.withDir(new Dir("2")
 					.withDir(new Dir("21"))
 					.withDir(new Dir("22"))
 					.withDir(new Dir("23"))
 				)
 				.withFil(sameFil);
 			return tree;
 		}
 }
 
