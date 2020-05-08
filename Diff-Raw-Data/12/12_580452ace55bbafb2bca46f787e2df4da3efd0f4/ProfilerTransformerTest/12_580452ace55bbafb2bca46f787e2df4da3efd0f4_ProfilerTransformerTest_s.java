 package fi.lolcatz.profiler;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import org.junit.*;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.AbstractInsnNode;
 import org.objectweb.asm.tree.InsnNode;
 
 public class ProfilerTransformerTest implements Opcodes {
 
     public static LinkedList<AbstractInsnNode> oneInsnList;
     public static LinkedList<AbstractInsnNode> fiveInsnList;
     public static LinkedList<AbstractInsnNode> emptyInsnList;
 
     public ProfilerTransformer transformer;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
        RootLogger.initLogger();
         RootLogger.setLoggingLevel(Level.ALL);
         oneInsnList = new LinkedList<AbstractInsnNode>();
         oneInsnList.add(new InsnNode(ICONST_0));
         fiveInsnList = new LinkedList<AbstractInsnNode>();
         fiveInsnList.add(new InsnNode(ICONST_1));
         fiveInsnList.add(new InsnNode(IINC));
         fiveInsnList.add(new InsnNode(NOP));
         fiveInsnList.add(new InsnNode(LCONST_1));
         fiveInsnList.add(new InsnNode(ACONST_NULL));
         emptyInsnList = new LinkedList<AbstractInsnNode>();
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
 
     @Before
     public void setUp() throws Exception {
         transformer = new ProfilerTransformer();
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testCalculateCostEmptyList() {
         long cost = transformer.calculateCost(emptyInsnList);
         assertTrue("Cost of empty instruction list was not 0", cost == 0);
     }
 
     @Test
     public void testCalculateCostOneInsn() {
         long cost = transformer.calculateCost(oneInsnList);
         assertTrue("Cost of one instruction was not one.", cost == 1);
     }
 
     @Test
     public void testCalculateCostFiveInsn() {
         long cost = transformer.calculateCost(fiveInsnList);
         assertTrue("Cost of five instruction was not five.", cost == 5);
     }
 
     // @Test
     // public void testFindBasicBlockBeginnings() {
     // InsnList insns = new InsnList();
     // insns.add(new LabelNode());
     // insns.add(oneInsnList.getFirst());
     // insns.add(new LabelNode());
     // addListToInsnList(insns, fiveInsnList);
     // insns.add(new LabelNode());
     // addListToInsnList(insns, fiveInsnList);
     // insns.add(new LabelNode());
     //
     // insns.resetLabels();
     //
     // assertEquals(3, transformer.findBasicBlockBeginnings(insns).size());
     // }
     //
     // private static void addListToInsnList(InsnList insnList, List<AbstractInsnNode> list) {
     // for (AbstractInsnNode abstractInsnNode : list) {
     // insnList.add(abstractInsnNode);
     // }
     // }
 
 }
