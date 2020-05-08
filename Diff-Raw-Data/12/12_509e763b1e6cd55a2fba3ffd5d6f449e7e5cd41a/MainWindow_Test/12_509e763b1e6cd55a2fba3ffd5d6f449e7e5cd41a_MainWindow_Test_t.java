 package uk.ac.cam.db538.dexter.gui;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import lombok.val;
 
 import org.jf.dexlib.Util.AccessFlags;
 import org.junit.Test;
 
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.DexField;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
 
 public class MainWindow_Test {
 
   private static void execAddClassesToTree(DefaultMutableTreeNode root, List<DexClass> classes) {
     try {
       Method m = FileTab.class.getDeclaredMethod("addClassesToTree", DefaultMutableTreeNode.class, List.class);
       m.setAccessible(true);
       m.invoke(null, root, classes);
     } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
       e.printStackTrace(System.err);
       fail("Couldn't execute method: " + e.getClass().getSimpleName());
     }
   }
 
   @Test
   public void testAddClassesToTree_EmptyList() {
     val root = new DefaultMutableTreeNode("root");
     val classes = new LinkedList<DexClass>();
     execAddClassesToTree(root, classes);
     assertEquals(0, root.getChildCount());
   }
 
   @Test
   public void testAddClassesToTree_TwoPackages() {
     val root = new DefaultMutableTreeNode("root");
     val classes = new LinkedList<DexClass>();
     val cache = new DexTypeCache();
     val dexFile = new Dex();
 
    val cls11 = new DexClass(dexFile, DexClassType.parse("Lcom.example1.a;", cache), null, null, null, null, null, null);
    val cls12 = new DexClass(dexFile, DexClassType.parse("Lcom.example1.b;", cache), null, null, null, null, null, null);
    val cls21 = new DexClass(dexFile, DexClassType.parse("Lcom.example2.a;", cache), null, null, null, null, null, null);
 
     classes.add(cls11);
     classes.add(cls12);
     classes.add(cls21);
     execAddClassesToTree(root, classes);
 
     assertEquals(2, root.getChildCount());
 
     val child1 = (DefaultMutableTreeNode) root.getChildAt(0);
     assertEquals("com.example1", (String) child1.getUserObject());
     assertEquals(false, child1.isLeaf());
     assertEquals(2, child1.getChildCount());
     {
       val child11 = (DefaultMutableTreeNode) child1.getChildAt(0);
       assertEquals(cls11, child11.getUserObject());
       assertEquals(true, child11.isLeaf());
 
       val child12 = (DefaultMutableTreeNode) child1.getChildAt(1);
       assertEquals(cls12, child12.getUserObject());
       assertEquals(true, child12.isLeaf());
     }
 
     val child2 = (DefaultMutableTreeNode) root.getChildAt(1);
     assertEquals("com.example2", (String) child2.getUserObject());
     assertEquals(false, child2.isLeaf());
     assertEquals(1, child2.getChildCount());
     {
       val child21 = (DefaultMutableTreeNode) child2.getChildAt(0);
       assertEquals(cls21, child21.getUserObject());
       assertEquals(true, child21.isLeaf());
     }
   }
 
   @Test
   public void testAddClassesToTree_DefaultPackage() {
     val root = new DefaultMutableTreeNode("root");
     val classes = new LinkedList<DexClass>();
     val cache = new DexTypeCache();
     val dexFile = new Dex();
 
    val cls = new DexClass(dexFile, DexClassType.parse("LTestClass;", cache), null, null, null, null, null, null);
     classes.add(cls);
 
     execAddClassesToTree(root, classes);
 
     assertEquals(1, root.getChildCount());
 
     val pkgNode = (DefaultMutableTreeNode) root.getChildAt(0);
     assertEquals("(default package)", (String) pkgNode.getUserObject());
     assertEquals(1, pkgNode.getChildCount());
 
     val clsNode = (DefaultMutableTreeNode) pkgNode.getChildAt(0);
     assertEquals(0, clsNode.getChildCount());
     assertEquals(cls, clsNode.getUserObject());
   }
 
   @Test
   public void testAddClassesToTree_StaticFields() throws UnknownTypeException {
     val root = new DefaultMutableTreeNode("root");
     val classes = new LinkedList<DexClass>();
 
     DexRegisterType typeInt = DexRegisterType.parse("I", null);
 
     val cache = new DexTypeCache();
     val dexFile = new Dex();
 
    val cls = new DexClass(dexFile, DexClassType.parse("LTestClass;", cache), null, null, null, null, null, null);
 
     val staticField1 = new DexField(null, "a", typeInt, EnumSet.of(AccessFlags.STATIC), null);
     val staticField2 = new DexField(null, "c", typeInt, EnumSet.of(AccessFlags.STATIC), null);
     val instanceField1 = new DexField(null, "d", typeInt, null, null);
     val instanceField2 = new DexField(null, "b", typeInt, null, null);
 
     val method1 = new DexDirectMethod(cls, "a", null, new DexPrototype(typeInt, Arrays.asList(new DexRegisterType[] { typeInt, typeInt })), new DexCode(), null, null);
 
     cls.addField(staticField1);
     cls.addField(staticField2);
     cls.addField(instanceField1);
     cls.addField(instanceField2);
     cls.addMethod(method1);
     classes.add(cls);
 
     execAddClassesToTree(root, classes);
 
     val pkgNode = (DefaultMutableTreeNode) root.getChildAt(0);
     val clsNode = (DefaultMutableTreeNode) pkgNode.getChildAt(0);
 
     val fieldNode = (DefaultMutableTreeNode) clsNode.getChildAt(0);
     assertEquals(4, fieldNode.getChildCount());
 
     val fieldNode1 = (DefaultMutableTreeNode) fieldNode.getChildAt(0);
     val fieldNode2 = (DefaultMutableTreeNode) fieldNode.getChildAt(1);
     val fieldNode3 = (DefaultMutableTreeNode) fieldNode.getChildAt(2);
     val fieldNode4 = (DefaultMutableTreeNode) fieldNode.getChildAt(3);
 
     assertEquals(staticField1, fieldNode1.getUserObject());
     assertEquals(instanceField2, fieldNode2.getUserObject());
     assertEquals(staticField2, fieldNode3.getUserObject());
     assertEquals(instanceField1, fieldNode4.getUserObject());
 
     val methodNode = (DefaultMutableTreeNode) clsNode.getChildAt(1);
     assertEquals(1, methodNode.getChildCount());
 
     val methodNode1 = (DefaultMutableTreeNode) methodNode.getChildAt(0);
     assertEquals(method1, methodNode1.getUserObject());
   }
 
   private static void execInsertNodeAlphabetically(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
     try {
       Method m = FileTab.class.getDeclaredMethod("insertNodeAlphabetically", DefaultMutableTreeNode.class, DefaultMutableTreeNode.class);
       m.setAccessible(true);
       m.invoke(null, parent, newChild);
     } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
       fail("Couldn't execute method: " + e.getClass().getSimpleName());
     }
   }
 
   @Test
   public void testInsertNodeAlphabetically_Empty() {
     val parent = new DefaultMutableTreeNode();
     val newChild = new DefaultMutableTreeNode("a");
 
     execInsertNodeAlphabetically(parent, newChild);
 
     assertEquals(1, parent.getChildCount());
     assertEquals(newChild, parent.getChildAt(0));
   }
 
   @Test
   public void testInsertNodeAlphabetically_CorrectOrder() {
     val parent = new DefaultMutableTreeNode();
     val newChild1 = new DefaultMutableTreeNode("a");
     val newChild2 = new DefaultMutableTreeNode("b");
 
     execInsertNodeAlphabetically(parent, newChild1);
     execInsertNodeAlphabetically(parent, newChild2);
 
     assertEquals(2, parent.getChildCount());
     assertEquals(newChild1, parent.getChildAt(0));
     assertEquals(newChild2, parent.getChildAt(1));
   }
 
   @Test
   public void testInsertNodeAlphabetically_WrongOrder() {
     val parent = new DefaultMutableTreeNode();
     val newChild1 = new DefaultMutableTreeNode("a");
     val newChild2 = new DefaultMutableTreeNode("b");
 
     execInsertNodeAlphabetically(parent, newChild2);
     execInsertNodeAlphabetically(parent, newChild1);
 
     assertEquals(2, parent.getChildCount());
     assertEquals(newChild1, parent.getChildAt(0));
     assertEquals(newChild2, parent.getChildAt(1));
   }
 
   @Test
   public void testInsertNodeAlphabetically_CaseIgnored() {
     val parent = new DefaultMutableTreeNode();
     val newChild1 = new DefaultMutableTreeNode("a");
     val newChild2 = new DefaultMutableTreeNode("B");
 
     execInsertNodeAlphabetically(parent, newChild1);
     execInsertNodeAlphabetically(parent, newChild2);
 
     assertEquals(2, parent.getChildCount());
     assertEquals(newChild1, parent.getChildAt(0));
     assertEquals(newChild2, parent.getChildAt(1));
   }
 }
 
