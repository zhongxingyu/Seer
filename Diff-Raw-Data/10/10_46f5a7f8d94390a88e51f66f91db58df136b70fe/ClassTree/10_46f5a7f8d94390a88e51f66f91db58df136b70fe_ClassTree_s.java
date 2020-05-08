 package shellderp.bcexplorer.ui;
 
 import org.apache.bcel.Repository;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.classfile.FieldOrMethod;
 import org.apache.bcel.generic.*;
 import shellderp.bcexplorer.*;
 import shellderp.bcexplorer.Node;
 import shellderp.bcexplorer.reference.FieldReferenceFilter;
 import shellderp.bcexplorer.reference.MethodReferenceFilter;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created by: Mike
  * Date: 4/9/12
  * Time: 4:11 PM
  */
 public class ClassTree extends JTree {
     public ClassGen classGen;
     public Node constants, fields, methods;
 
     ClassTabPane classTabPane;
     ClassHierarchy classHierarchy;
 
     public ClassTree(ClassGen classGen, ClassTabPane classTabPane, ClassHierarchy classHierarchy) {
         super();
 
         this.classGen = classGen;
         this.classTabPane = classTabPane;
         this.classHierarchy = classHierarchy;
 
         ConstantPoolGen cpgen = classGen.getConstantPool();
         Node root = new Node<>(classGen);
         root.setDisplayText(Utility.accessToString(classGen.getAccessFlags(), true) + " " + Utility.classOrInterface(classGen.getAccessFlags()) + " " + classGen.getClassName());
 
         Constant superNameConstant = cpgen.getConstant(classGen.getSuperclassNameIndex());
         if (superNameConstant != null)
             root.addChild(superNameConstant).setDisplayText("extends " + classGen.getSuperclassName());
 
         Node interfaces = root.addChild("Interfaces");
         for (int constantIndex : classGen.getInterfaces()) {
             Constant constant = cpgen.getConstant(constantIndex);
             interfaces.addChild(constant).setDisplayText(cpgen.getConstantPool().constantToString(constant));
         }
 
         constants = root.addChild("Constant Pool (" + cpgen.getSize() + " entries)");
         for (int i = 0; i < cpgen.getSize(); i++) {
             Constant constant = cpgen.getConstant(i);
             if (constant == null)
                 continue;
 
             String type = constant.getClass().getSimpleName();
             Node typeNode = constants.findChild(type);
             if (typeNode == null)
                 typeNode = constants.addChild(type);
 
             typeNode.addChild(constant).setDisplayText(i + ": " + cpgen.getConstantPool().constantToString(constant));
         }
 
         fields = root.addChild("Fields");
         for (Field field : classGen.getFields()) {
             fields.addChild(field);
         }
 
         methods = root.addChild("Methods");
         for (Method method : classGen.getMethods()) {
             Node m = methods.addChild(method);
 
             InstructionList list = new MethodGen(method, classGen.getClassName(), cpgen).getInstructionList();
             if (list == null)
                 continue;
 
             int posMaxDigits = String.valueOf(list.getEnd().getPosition()).length();
             for (InstructionHandle ih : list.getInstructionHandles()) {
                 String label = String.format("%" + posMaxDigits + "d: %s", ih.getPosition(), ih.getInstruction().toString(cpgen.getConstantPool()));
                 m.addChild(new InstructionWrapper(ih, method)).setDisplayText(label);
             }
         }
 
         setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 
         addMouseListener(new TreeContextMenuListener(contextMenuProvider, this));
 
         super.setModel(new DefaultTreeModel(root));
 
         expandPath(constants.getPath());
         expandPath(interfaces.getPath());
         expandPath(fields.getPath());
         expandPath(methods.getPath());
     }
 
     private DefaultTreeContextMenuProvider contextMenuProvider = new DefaultTreeContextMenuProvider() {
         @Override
         public JPopupMenu createContextMenu(final JTree tree, final TreePath path, final Node node) {
             JPopupMenu menu = super.createContextMenu(tree, path, node);
 
             if (node.get() instanceof Field) {
                 addFieldMenuItems(menu, classGen, (Field) node.get());
             } else if (node.get() instanceof Method) {
                 addMethodMenuItems(menu, classGen, (Method) node.get());
             } else if (node.get() instanceof Constant) {
                 addConstantMenuItems(menu, (Constant) node.get());
             } else if (node.get() instanceof InstructionWrapper) {
                 InstructionHandle iHandle = ((InstructionWrapper) node.get()).instruction;
 
                 if (iHandle.getInstruction() instanceof CPInstruction) {
                     CPInstruction cpInstruction = (CPInstruction) iHandle.getInstruction();
                     addConstantMenuItems(menu, classGen.getConstantPool().getConstant(cpInstruction.getIndex()));
                 }
             } else if (node.get() instanceof ClassGen) {
                 addClassMenuItems(classHierarchy, classTabPane, menu, ((ClassGen) node.get()).getClassName());
             }
 
             return menu;
         }
 
         @Override
         public JPopupMenu createContextMenu(final JTree tree, final TreePath[] paths, final Node[] nodes) {
             JPopupMenu menu = super.createContextMenu(tree, paths, nodes);
 
             return menu;
         }
     };
 
     public void addConstantMenuItems(JPopupMenu menu, Constant constant) {
         ConstantPoolGen cpgen = classGen.getConstantPool();
         ConstantPool cp = cpgen.getConstantPool();
 
         if (constant instanceof ConstantCP) {
             ConstantCP constantCP = (ConstantCP) constant;
 
             ConstantClass constantClass = (ConstantClass) cpgen.getConstant(constantCP.getClassIndex());
             ConstantNameAndType nameAndType = (ConstantNameAndType) cpgen.getConstant(constantCP.getNameAndTypeIndex());
             final String refClassName = Utility.compactClassName((String) constantClass.getConstantValue(cp), false);
             if (addClassMenuItems(classHierarchy, classTabPane, menu, refClassName)) {
                 final FieldOrMethodReference reference = classHierarchy.findFieldOrMethod(refClassName, nameAndType.getName(cp), nameAndType.getSignature(cp));
                 if (reference == null)
                     return;
 
                 FieldOrMethod fieldOrMethod = reference.getFieldOrMethod();
                 String menuText = (fieldOrMethod instanceof Field ? "Field" : "Method") + " '" + fieldOrMethod.getName() + "'";
                 JMenu submenu = new JMenu(menuText);
                 submenu.add(new AbstractAction("Go to declaration") {
                     @Override public void actionPerformed(ActionEvent e) {
                         ClassTree classTree = classTabPane.openClassTab(reference.getClassGen());
                         SwingUtils.goToNode(classTree, reference.getReferencedClassNode(classTree).getPath());
                     }
                 });
                 if (fieldOrMethod instanceof Field) {
                     addFieldMenuItems(submenu.getPopupMenu(), reference.getClassGen(), (Field) fieldOrMethod);
                 } else {
                     addMethodMenuItems(submenu.getPopupMenu(), reference.getClassGen(), (Method) fieldOrMethod);
                 }
                 menu.add(submenu);
             }
         } else if (constant instanceof ConstantClass) {
             ConstantClass constantClass = (ConstantClass) constant;
             String className = Utility.compactClassName((String) constantClass.getConstantValue(cp), false);
             addClassMenuItems(classHierarchy, classTabPane, menu, className);
         }
     }
 
     public static boolean addClassMenuItems(final ClassHierarchy classHierarchy, final ClassTabPane classTabPane, JPopupMenu menu, final String className) {
         menu.addSeparator();
 
         if (!classHierarchy.classes.containsKey(className)) {
             JMenu submenu = new JMenu("Class '" + NameUtil.getSimpleName(className) + "' not loaded");
             submenu.add(new AbstractAction("Attempt load from classpath") {
                 @Override public void actionPerformed(ActionEvent e) {
                     try {
                         ClassGen cg = new ClassGen(Repository.lookupClass(className));
                         classHierarchy.loadClasses(Collections.singletonList(cg));
                     } catch (ClassNotFoundException ex) {
                         System.err.println("class not found in repository: " + className);
                     }
                 }
             });
             menu.add(submenu);
 
             return false;
         }
 
         final Node<ClassGen> classNode = classHierarchy.classes.get(className);
         final ClassGen cg = classNode.get();
         JMenu submenu = new JMenu((cg.isInterface() ? "Interface" : "Class") + " '" + NameUtil.getSimpleName(className) + "'");
         submenu.add(new AbstractAction("Open") {
             @Override public void actionPerformed(ActionEvent e) {
                 classTabPane.openClassTab(classHierarchy.classes.get(className).get());
             }
         });
         submenu.add(new AbstractAction("Show in tree") {
             @Override public void actionPerformed(ActionEvent e) {
                 SwingUtils.goToNode(classHierarchy.getJTree(classTabPane), classNode.getPath());
             }
         });
         submenu.add(new AbstractAction("Find references") {
             @Override public void actionPerformed(ActionEvent e) {
                 // TODO
             }
         });
         submenu.add(new AbstractAction("Unload") {
             @Override public void actionPerformed(ActionEvent e) {
                 classTabPane.closeClassTab(className);
                 classHierarchy.unloadClass(className);
             }
         });
         if (cg.isInterface()) {
             submenu.addSeparator();
             submenu.add(new AbstractAction("Find implementing classes") {
                 @Override public void actionPerformed(ActionEvent e) {
                     List<Reference> implementing = classHierarchy.findImplementingClasses(cg.getClassName());
                     ReferenceTree rt = new ReferenceTree(classTabPane, implementing);
                     String title = "Implementations of " + NameUtil.getSimpleName(cg);
                     classTabPane.addResultTab(title, rt);
                 }
             });
         }
         menu.add(submenu);
 
         return true;
     }
 
     public void addMethodMenuItems(JPopupMenu menu, final ClassGen methodClassGen, final Method method) {
         menu.addSeparator();
 
         menu.add(new AbstractAction("Find Local References") {
             @Override public void actionPerformed(ActionEvent e) {
                 addReferenceTab(methodClassGen, method, ClassHierarchy.findReferences(classGen, new MethodReferenceFilter(methodClassGen, method)));
             }
         });
 
         menu.add(new AbstractAction("Find Global References") {
             @Override public void actionPerformed(ActionEvent e) {
                 addReferenceTab(methodClassGen, method, classHierarchy.findReferences(new MethodReferenceFilter(methodClassGen, method)));
             }
         });
 
         menu.addSeparator();
 
         final Node<ClassGen> superDecl = classHierarchy.findSuperDeclaration(methodClassGen, method);
         if (superDecl != null) {
             menu.add(new AbstractAction("Go to super declaration") {
                 @Override public void actionPerformed(ActionEvent e) {
                     ClassTree classTree = classTabPane.openClassTab(superDecl.get());
                     SwingUtils.goToNode(classTree, classTree.methods.findChild(method).getPath());
                 }
             });
         }
 
         menu.add(new AbstractAction("Find overrides") {
             @Override public void actionPerformed(ActionEvent e) {
                 List<FieldOrMethodReference> overrides = classHierarchy.findOverrides(methodClassGen.getClassName(), method);
                 ReferenceTree rt = new ReferenceTree(classTabPane, overrides);
                 String title = "Overrides of " + NameUtil.getSimpleName(methodClassGen) + "." + method.getName() + NameUtil.getSimpleArgumentString(method.getSignature());
                 classTabPane.addResultTab(title, rt);
             }
         });
 
         Type returnType = method.getReturnType();
         if (returnType instanceof ObjectType) {
             addClassMenuItems(classHierarchy, classTabPane, menu, returnType.toString());
         }
 
         for (Type argType : method.getArgumentTypes()) {
             if (argType instanceof ObjectType) {
                addClassMenuItems(classHierarchy, classTabPane, menu, argType.toString());
             }
         }
     }
 
     public void addFieldMenuItems(JPopupMenu menu, final ClassGen fieldClassGen, final Field field) {
         menu.addSeparator();
 
         menu.add(new AbstractAction("Find Local References") {
             @Override public void actionPerformed(ActionEvent e) {
                 addReferenceTab(fieldClassGen, field, ClassHierarchy.findReferences(classGen, new FieldReferenceFilter(fieldClassGen, field)));
             }
         });
 
         menu.add(new AbstractAction("Find Global References") {
             @Override public void actionPerformed(ActionEvent e) {
                 addReferenceTab(fieldClassGen, field, classHierarchy.findReferences(new FieldReferenceFilter(fieldClassGen, field)));
             }
         });
 
         Type type = field.getType();
         if (type instanceof ObjectType) {
             addClassMenuItems(classHierarchy, classTabPane, menu, type.toString());
         }
     }
 
     public void addReferenceTab(ClassGen targetClassGen, FieldOrMethod target, List<Reference> refs) {
         ReferenceTree rt = new ReferenceTree(classTabPane, refs);
         String title = "References to " + NameUtil.getSimpleName(targetClassGen) + "." + target.getName();
         if (target instanceof Method) {
             title += NameUtil.getSimpleArgumentString(target.getSignature());
         }
         classTabPane.addResultTab(title, rt);
     }
 }
