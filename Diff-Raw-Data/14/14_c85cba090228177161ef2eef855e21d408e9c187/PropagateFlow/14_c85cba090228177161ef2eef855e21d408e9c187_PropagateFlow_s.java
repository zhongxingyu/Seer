 package com.sun.tools.javac.comp;
 
 import com.sun.tools.javac.code.Kinds;
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Types;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCNewClass;
 import com.sun.tools.javac.tree.TreeInfo;
 import com.sun.tools.javac.tree.TreeScanner;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.ListBuffer;
 import com.sun.tools.javac.util.Log;
 
 import com.sun.tools.javac.util.Name;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PropagateFlow extends TreeScanner {
     //every tree has this thing...
     protected static final Context.Key<PropagateFlow> pflowKey =
         new Context.Key<PropagateFlow>();
 
     public static PropagateFlow instance(Context context) {
         PropagateFlow instance = context.get(pflowKey);
         if (instance == null)
             instance = new PropagateFlow(context);
         return instance;
     }
 
     //private final Names names;
     private final Log log;
     private Types types;
     //private final Symtab syms;
     //private final Types types;
     private final Check chk;
     //private       TreeMaker make;
     //private final Resolve rs;
     //private Env<AttrContext> attrEnv;
     //private       Lint lint;
     //private final boolean allowImprovedRethrowAnalysis;
     //private final boolean allowImprovedCatchAnalysis;
     public PropagateFlow(Context context) {
         //...and this thing.
         //context.put(pflowKey, this);
         this.types = Types.instance(context);
         log = Log.instance(context);
         chk = Check.instance(context);
     }
 
     private ArrayList<Env<AttrContext>> envs;
     public void analysePropagate(ArrayList<Env<AttrContext>> envs) {
         this.envs = envs;
 //        this.env = env;
 //        for (JCTree p: env.toplevel.props) {
 //            buildGraph((JCTree.JCPropagate)p);
 //        }
         //scan(env.tree);
 
                 /* -find out how to add "throws E" to the tree.
                  *   * JCTree$JCMethodDecl.thrown attribute
                  *   we should construct that object
                  * - Figure what structure to store the constructing
                  *   paths. When the path reaches the LHS,
                  *   we should set "thrown" attribute for each node.
                  */
         for(Env<AttrContext> e : envs) {
             if (e.tree.getTag() == JCTree.PROPAGATE) {
                 try {
                     JCTree.JCPropagate p = (JCTree.JCPropagate)e.tree;
                     log.useSource(e.toplevel.sourcefile);
                     process(p);
                 } catch(Exception ex) {
                     ex.printStackTrace();
                     //report unable to build paths
                 }
             }
         }
     }
 
     List<JCTree.JCMethodDecl> lookupMethods(JCTree.JCPropagateMethodSimple m) {
         JCTree.JCClassDecl clazz = getClassForType(m.selector.selected.type);
 
         if (clazz == null) return null;
 
         JCTree.JCMethodDecl method = getOwnMethod(clazz, m.sym);
         List<JCTree.JCMethodDecl> lst = new ArrayList<JCTree.JCMethodDecl>();
         if (method == null) {
             method = getSuperMethod(clazz, m.sym);
         }
         lst.add(method);
         return lst;
     }
 
     List<JCTree.JCMethodDecl> lookupMethods(JCTree.JCPropagateMethodPolym m) {
         List<JCTree.JCMethodDecl> ret = new ArrayList<JCTree.JCMethodDecl>();
         for(JCTree.JCExpression s : m.selectors) {
             JCTree.JCFieldAccess f = (JCTree.JCFieldAccess) s;
             JCTree.JCClassDecl clazz = getClassForType(f.selected.type);
 
             if (clazz == null) continue; //ignore ast-unreachable classes
 
             JCTree.JCMethodDecl method = getOwnMethod(clazz, f.sym);
             if (method == null) {
                 log.error(m.pos(),
                         "propagate.no.method.found",
                         f.name.toString(), clazz.name.toString());
             } else {
                 ret.add(method);
             }
         }
         return ret.isEmpty() ? null : ret;
     }
 
     JCTree.JCMethodDecl lookupMethod(JCNewClass nclass) {
         JCTree.JCClassDecl clazz = getClassForType(nclass.clazz.type);
         if (clazz == null) return null;
         return getOwnMethod(clazz, nclass.constructor);
     }
 
     JCTree.JCMethodDecl lookupMethod(JCTree.JCMethodInvocation m) {
         JCTree.JCClassDecl clazz = null;
         Symbol s = null;
         switch (m.meth.getTag()) {
             case JCTree.IDENT:
                 JCTree.JCIdent i = (JCTree.JCIdent) m.meth;
                 clazz = getClassForType(i.sym.owner.type);
                 s = i.sym;
                 break;
             case JCTree.SELECT:
                 JCTree.JCFieldAccess f = (JCTree.JCFieldAccess) m.meth;
                 s = f.sym;
                 clazz = getClassForType(f.selected.type);
                 break;
             default:
                 System.err.println("+++ BUG");
         }
 
         if (clazz == null) return null;
 
         JCTree.JCMethodDecl method = getOwnMethod(clazz, s);
         if (method == null) {
             method = getSuperMethod(clazz, s);
         }
         return method;
     }
 
     JCTree.JCClassDecl getClassForType(Type t) {
         JCTree tree = null;
         for(Env<AttrContext> e : envs) {
             tree = TreeInfo.declarationFor(t.tsym, e.tree);
             if (tree != null)
                 break;
         }
         return (JCTree.JCClassDecl)tree;
     }
 
     JCTree.JCMethodDecl getSuperMethod(JCTree.JCClassDecl subclazz, Symbol sym) {
         JCTree.JCClassDecl clazz = getClassForType(subclazz.extending.type);
 
         if (clazz == null) return null;
 
         JCTree.JCMethodDecl method = getOwnMethod(clazz, sym);
         if (method == null) {
             method = getSuperMethod(clazz, sym);
         }
         return method;
     }
 
     JCTree.JCMethodDecl getOwnMethod(JCTree.JCClassDecl clazz, Symbol sym) {
         for (JCTree def : clazz.defs) {
             if (def.getTag() == JCTree.METHODDEF) {
                 JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) def;
                 if (method.sym == sym) {
                     return method;
                 }
             }
         }
         return null;
     }
 
     private List<List<JCTree.JCMethodDecl>> targetsLeft;
     private List<JCTree.JCMethodDecl> nextTargets;
     private PathTree currentTree;
 
     public void process(JCTree.JCPropagate p) {
         this.targetsLeft = new ArrayList<List<JCTree.JCMethodDecl>>();
 
         for(JCTree m : p.nodes) {
             List<JCTree.JCMethodDecl> methods = null;
             if (m.getTag() == JCTree.PROPAGATE_METHOD_SIMPLE) {
                 methods = lookupMethods((JCTree.JCPropagateMethodSimple)m);
             } else if (m.getTag() == JCTree.PROPAGATE_METHOD_POLYM) {
 
                 //important: the first item in this list
                 //is the base type "C" in the decl "A,B<:C".
                 //we will use this specifically later on
                 methods = lookupMethods((JCTree.JCPropagateMethodPolym)m);
 
             } else {
                 System.out.println("UOPS! this is a bug");
                 //error!
             }
             if (methods == null || methods.isEmpty()) {
                 //some propagate nodes wheren't found
                 //maybe due to previous errors or because
                 //there is no AST in the envs for the specified method
                 //...so bail
                 return;
             }
             this.targetsLeft.add(methods);
         }
 //        //popping...
 
 
         List<JCTree.JCMethodDecl> initials = this.targetsLeft.remove(this.targetsLeft.size()-1);
 
         if (this.targetsLeft.size() == 0) { //single node
             for (JCTree.JCMethodDecl m : initials) {
                 currentTree = new PathTree(m, p.thrown);
                 currentTree.setupThrowPath();
                 //m.thrown = m.thrown.append(p.thrown);
                 //m.type.asMethodType().thrown =
                 //        m.type.asMethodType().thrown.append(p.thrown.type);
             }
         } else {
             this.nextTargets = this.targetsLeft.remove(this.targetsLeft.size()-1);
 
             if (initials.size() == 1) { //last node is simple
                 this.currentTree = new PathTree(initials.get(0), p.thrown);
                 buildpath(this.currentTree.node);
             } else { //last node is polym
                 for (JCTree.JCMethodDecl m : initials) {
                     this.currentTree = new PathTree(m, initials.get(0), p.thrown);
                     buildpath(this.currentTree.node);
                 }
             }
 
             if (!this.currentTree.atLeastOnePathFound) {
                 log.error(p.pos(),
                             "propagate.no.callgraph");
             }
         }
     }
 
     void buildpath(PathNode node) {
         scan(node.self.body);
     }
 
     public boolean checkRecursion(JCTree.JCMethodDecl m, PathNode node) {
         if (node == null) {
             return false;
         } else if (node.self.type == m.type) {
             return true;
         } else {
             return checkRecursion(m, node.parent);
         }
     }
 
     boolean matchTargets(JCTree.JCMethodDecl m) {
         //we will match m on the nextTarget's head
         // -if its a simple node, the head is the actual node
         // -if its polym, the head is the base class
         JCTree.JCMethodDecl base = this.nextTargets.get(0);
         return (base.sym == m.sym);
     }
 
     public void visitNewClass(JCNewClass tree) {
         processMethod(lookupMethod(tree));
     }
 
    public void visitApply(JCTree.JCMethodInvocation m) {
        processMethod(lookupMethod(m));
     }
 
     public void processMethod(JCTree.JCMethodDecl found) {
         if (found == null) {
             //the 'm' method is not in any ast
             //Doing nothing == finding a dead end in the graph path.
             //lets do nothing.
         } else if (checkRecursion(found, currentTree.node)) {
             //we already went that way...
             //lets do nothing
         } else if (matchTargets(found)) { //found is propagate node
             if (this.targetsLeft.isEmpty()) { //we found the last propagate node: full match
                 PathNode bk = currentTree.node;
                 currentTree.setRoot(found);
                 currentTree.setupThrowPath();
                 //a polym node might be the first (origin) node. exhaust it:
                 if (this.nextTargets.size() > 1) {
                     for (int i = 1; i < this.nextTargets.size(); i++) {
                         currentTree.node = bk;
                         currentTree.setRoot(this.nextTargets.get(i), this.nextTargets.get(0));
                         currentTree.setupThrowPath();
                     }
                 }
             } else {
                 //-load the next nextTargets
                 //-proceed exploration of each method in each body of each method matched
                 List<JCTree.JCMethodDecl> tgs = this.nextTargets;
                 this.nextTargets = targetsLeft.remove(targetsLeft.size()-1);
                 if (tgs.size() == 1) { //simple propagate
                     PathNode bk = currentTree.node;
                     currentTree.setRoot(tgs.get(0));
                     buildpath(currentTree.node);
                     currentTree.node = bk;
 
                 } else { //polym propagate
                     //for all subtypes+supertype X, navigate on X:m()'s body
                     for (JCTree.JCMethodDecl t : tgs) {
                        PathNode bk = currentTree.node;
                        currentTree.setRoot(t, tgs.get(0));
                        buildpath(currentTree.node);
                        currentTree.node = bk;
                     }
                 }
                 targetsLeft.add(this.nextTargets);
                 this.nextTargets = tgs;
             }
         } else if (found.body == null) {
             //found is abstract. dead end
         } else { //not a propagate node. keep searching
             PathNode bk = currentTree.node;
             currentTree.setRoot(found);
             buildpath(currentTree.node);
             currentTree.node = bk;
         }
     }
 
 
 
     class PathTree {
         public PathNode node = null;
         public JCTree.JCExpression thrown;
         public boolean atLeastOnePathFound;
         PathTree(JCTree.JCMethodDecl m, JCTree.JCExpression thr) {
             setRoot(m);
             this.thrown = thr;
             this.atLeastOnePathFound = false;
         }
 
         PathTree(JCTree.JCMethodDecl m, JCTree.JCMethodDecl base, JCTree.JCExpression thr) {
             setRoot(m,base);
             this.thrown = thr;
             this.atLeastOnePathFound = false;
         }
 
         void setRoot(JCTree.JCMethodDecl m) {
             this.node = new PathNode(m, this.node);
         }
 
         void setRoot(JCTree.JCMethodDecl m, JCTree.JCMethodDecl base) {
             this.node = new PathNode(m, base, this.node);
         }
 
         void setupThrowPath() {
             this.atLeastOnePathFound = true;
             System.out.println("Found path: " + this.pathAsString(this.node));
 
             this.node.setupThrows(thrown);
         }
 
         String pathAsString(PathNode n) {
             if (n.parent != null) {
                 return pathAsString(n.parent) + " -> "
                         + n.self.sym.owner.name + "::" + n.self.name
                         + "(" + n.self.params.toString(",") + ")";
             }
             return n.self.sym.owner.name + "::" + n.self.name
                     + "(" + n.self.params.toString(",") + ")";
         }
     }
 
     class PathNode {
         public PathNode parent;
         public JCTree.JCMethodDecl self;
         public JCTree.JCMethodDecl base; //polym base method z in {x,y <: _z_}
 
         public PathNode(JCTree.JCMethodDecl m, PathNode parent) {
             this.parent = parent;
             this.self = m;
             this.base = null;
         }
 
         public PathNode(JCTree.JCMethodDecl m, JCTree.JCMethodDecl base, PathNode parent) {
             this.parent = parent;
             this.self = m;
             this.base = base;
         }
 
         public void setupThrows(JCTree.JCExpression t) {
             if (!alreadyThrows(t)) {
                 //check overriding
                 //if its a polym node, this.base is the base B in {... <: B}
                 //we then check for overriden methods on B's superclasses.
                 //otherwise, we check for overriden in this.self superclasses
                 JCTree.JCMethodDecl uppermost =
                         this.base == null ? this.self : this.base;
                 JCTree.JCClassDecl clazz = getClassForType(uppermost.sym.owner.type);
                 if (!canOverrideMethodThrow(clazz, this.self, t)) {
                     Symbol overriden = getOverridenMethod(clazz, this.self.sym, this.self.name);
                     ScriptPropagate.logPropagateError(this.self.sym,(Symbol.MethodSymbol)overriden, (Type.ClassType)t.type);
                     log.error(this.self.pos(),
                     "propagate.incompatible.throws", this.self.sym,
                     t.type);
                 } else {
                     //add exception type to thrown list
                     this.self.thrown = this.self.thrown.append(t);
                     this.self.type.asMethodType().thrown
                             = this.self.type.asMethodType().thrown.append(t.type);
                     //add exception type to overriden thrown list
                     //from A to C in {A <: C}
                     //if A extends B extends C, we must add thrown to B as well.
                     if (this.base != null) {
                         Name name = this.self.name;
                         Symbol current = this.self.sym;
                         while (current != this.base.sym) {
                             current = getOverridenMethod(
                                             getClassForType(current.owner.type),
                                                current, name);
                             if (current != null) {
                                 current.type.asMethodType().thrown =
                                         current.type.asMethodType().thrown.append(t.type);
 
                             }
                         }
                     }
                 }
             }
             if (this.parent != null) {
                 this.parent.setupThrows(t);
             }
         }
 
         //check if the method self already specifies it
         //throws t (or a superclass of t)
         public boolean alreadyThrows(JCTree.JCExpression t) {
             ListBuffer<Type> thrown = new ListBuffer<Type>();
             thrown.add(t.type);
             return chk.unhandled(thrown.toList(),
                                  self.type.asMethodType().thrown).isEmpty();
         }
     }
 
     public Symbol getOverridenMethod(JCTree.JCClassDecl clazz,
                                      Symbol ms, Name mname) {
 
         if (clazz.extending == null) return null;
 
         JCTree.JCClassDecl superclazz = getClassForType(clazz.extending.type);
 
         if (superclazz == null) return null;
         Scope.Entry e = superclazz.sym.members().lookup(mname);
 
         while (e.scope != null) {
             if (ms.overrides(e.sym,clazz.sym.type.tsym, types, false)) {
                 return e.sym;
             }
             e = e.next();
         }
         return getOverridenMethod(superclazz, ms, mname);
     }
 
     public boolean canOverrideMethodThrow(JCTree.JCClassDecl clazz,
                                           JCTree.JCMethodDecl m,
                                           JCTree.JCExpression t) {
         //check for parent overrided methods. If they can throw
         //E, we can.
         Symbol overriden = getOverridenMethod(clazz, m.sym, m.name);
         if (overriden == null) {
             return true;
         } else {
             return chk.isHandled(t.type,overriden.type.asMethodType().thrown);
         }
     }
 }
