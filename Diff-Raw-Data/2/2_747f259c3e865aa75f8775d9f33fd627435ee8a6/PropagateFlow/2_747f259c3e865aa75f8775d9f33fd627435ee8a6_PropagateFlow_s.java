 package com.sun.tools.javac.comp;
 
 import com.sun.tools.javac.code.Scope;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Types;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCNewClass;
 import com.sun.tools.javac.tree.TreeInfo;
 import com.sun.tools.javac.tree.TreeScanner;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
 import com.sun.tools.javac.util.ListBuffer;
 import com.sun.tools.javac.util.Log;
 
 import com.sun.tools.javac.util.Name;
 import com.sun.tools.javac.util.Pair;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 class UnrelatedClassException extends Exception {
     private static final long serialVersionUID = 1;
     public Symbol first;
     public Symbol second;
     UnrelatedClassException(Symbol f, Symbol s) {
         this.first = f;
         this.second = s;
     }
 
 }
 
 class OverridingTriple {
     public JCTree.JCClassDecl clazz;
     public JCTree.JCMethodDecl method;
     public JCTree.JCExpression thrown;
     public DiagnosticPosition pos;
 
     public OverridingTriple(JCTree.JCClassDecl c, JCTree.JCMethodDecl m,
             JCTree.JCExpression t, DiagnosticPosition pos) {
         this.clazz = c;
         this.method = m;
         this.thrown = t;
         this.pos = pos;
     }
 }
 
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
 
         overridingCheckList = new ArrayList<OverridingTriple>();
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
                 } catch(UnrelatedClassException ex) {
                     log.error(currentPropagate.pos(),
                             "propagate.unrelated.class",
                             ex.first, ex.second);
                 } catch(Exception ex) {
                     ex.printStackTrace();
                     return;
                     //report unable to build paths
                 }
             }
         }
         checkOverriding();
         ScriptPropagate.logPaths();
     }
 
     void checkOverriding() {
         //check overriding
         //if its a polym node, this.base is the base B in {... <: B}
         //we then check for overriden methods on B's superclasses.
         //otherwise, we check for overriden in this.self superclasses
         for (OverridingTriple triple : overridingCheckList) {
             if (!canOverrideMethodThrow(triple.clazz, triple.method, triple.thrown)) {
                 Symbol overriden = getOverridenMethod(triple.clazz, triple.method.sym, triple.method.name);
                 ScriptPropagate.logPropagateError(triple.method.sym,(Symbol.MethodSymbol)overriden, (Type.ClassType)triple.thrown.type);
                 log.error(triple.pos,
                 "propagate.incompatible.throws",
                 triple.clazz.sym, triple.method.sym, triple.thrown.type);
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
 
     public int hierarchyDistance(JCTree.JCMethodDecl sub, JCTree.JCMethodDecl base)
             throws UnrelatedClassException{
         int i = 0;
         Type subtype = ((Type.ClassType)sub.sym.owner.type).supertype_field;
         while (true) {
             if (subtype == Type.noType) throw new UnrelatedClassException(sub.sym.owner, base.sym.owner);
             if (subtype.tsym == base.sym.owner) {
                 return i;
             }
             i++;
             subtype = ((Type.ClassType)subtype).supertype_field;
         }
     }
 
     public List<JCTree.JCMethodDecl> sortHierarchy(List<JCTree.JCMethodDecl> lst)
     throws UnrelatedClassException {
         //0 is base
         //sort rest
         SortedMap<Integer, List<JCTree.JCMethodDecl>> map
                 = new TreeMap<Integer, List<JCTree.JCMethodDecl>>();
 
         List<JCTree.JCMethodDecl> ret = new ArrayList<JCTree.JCMethodDecl>();
         for (int i = 1; i < lst.size(); i++) {
             JCTree.JCMethodDecl m = lst.get(i);
             int distance = hierarchyDistance(m, lst.get(0));
             List<JCTree.JCMethodDecl> l = map.get(distance);
             if (l == null) {
                 l = new ArrayList<JCTree.JCMethodDecl>();
             }
             if (l.contains(m)) {
                   log.error(this.currentPropagate.pos(),
                         "propagate.duplicated.class",
                         m.sym.owner);
             } else {
                 l.add(m);
             }
             map.put(distance, l);
         }
         for (SortedMap.Entry<Integer, List<JCTree.JCMethodDecl>> entry : map.entrySet()) {
             for (JCTree.JCMethodDecl m : entry.getValue()) {
                 ret.add(m);
             }
         }
 
         ret.add(0, lst.get(0));
         return ret;
     }
 
     List<JCTree.JCMethodDecl> lookupMethods(JCTree.JCPropagateMethodPolym m)
         throws UnrelatedClassException {
 
         List<JCTree.JCMethodDecl> ret = new ArrayList<JCTree.JCMethodDecl>();
         for(JCTree.JCExpression s : m.selectors) {
             JCTree.JCFieldAccess f = (JCTree.JCFieldAccess) s;
             JCTree.JCClassDecl clazz = getClassForType(f.selected.type);
 
             if (clazz == null) continue; //ignore ast-unreachable classes
 
             JCTree.JCMethodDecl method = getOwnMethod(clazz, f.sym);
             if (method == null) {
                 log.error(this.currentPropagate.pos(),
                         "propagate.no.method.found",
                         f.name.toString(), clazz.name.toString());
             } else {
                 ret.add(method);
             }
         }
         return ret.isEmpty() ? null : sortHierarchy(ret);
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
         if (subclazz.extending == null) return null; //playing safe
 
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
 
     private List<Target> targetsLeft;
     private Target nextTargets;
     private PathTree currentTree;
     private JCTree.JCPropagate currentPropagate;
 
     protected List<OverridingTriple> overridingCheckList;
 
     class Target {
         private List<JCTree.JCMethodDecl> _methods;
         private List<List<JCTree.JCMethodDecl>> _oredMethods;
         public boolean direct;
         public boolean ored;
         public List<JCTree.JCMethodDecl> matched;
 
         public Target(List<JCTree.JCMethodDecl> methods, boolean direct) {
             this._methods = methods;
             this.direct = direct;
             this.ored = false;
         }
 
         public Target(List<List<JCTree.JCMethodDecl>> methods, boolean direct, boolean _) {
             this._oredMethods = methods;
             this.direct = direct;
             this.ored = true;
         }
 
         public boolean match(JCTree.JCMethodDecl m) {
             if (this.ored) {
                 for (List<JCTree.JCMethodDecl> lst : _oredMethods) {
                     if (m.sym == lst.get(0).sym) {
                         this.matched = lst;
                         return true;
                     }
                 }
                 return false;
             } else {
                if (m.sym == this._methods.get(0).sym) {
                    this.matched = this._methods;
                    return true;
                } else {
                    return false;
                }
             }
         }
 
         boolean isPolym() {
             return (this.matched != null && this.matched.size() > 1) ||
                    (!this.ored && this._methods.size() > 1);
         }
 
         List<JCTree.JCMethodDecl> getMethods() {
             return this._methods;
         }
 
         List<JCTree.JCMethodDecl> getHeadlessMethods() {
             return this._methods.subList(1, this._methods.size());
         }
         List<JCTree.JCMethodDecl> getHeadlessMatches() {
         return this.matched.subList(1, this.matched.size());
         }
 
         JCTree.JCMethodDecl getHead() {
             return this._methods.get(0);
         }
         boolean isOred() {
             return this.ored;
         }
 
         List<JCTree.JCMethodDecl> getOreds() {
             List<JCTree.JCMethodDecl> ret = new ArrayList<JCTree.JCMethodDecl>();
             for (List<JCTree.JCMethodDecl> lst : _oredMethods) {
                 ret.addAll(lst);
             }
             return ret;
         }
     }
 
     public void loadMethods(JCTree m) throws UnrelatedClassException {
         boolean direct = false;
         List<JCTree.JCMethodDecl> methods = null;
         if (m.getTag() == JCTree.PROPAGATE_METHOD_SIMPLE) {
             JCTree.JCPropagateMethodSimple ps = (JCTree.JCPropagateMethodSimple)m;
             methods = lookupMethods(ps);
             direct = ps.direct;
 
             //some propagate nodes wheren't found
             //maybe due to previous errors or because
             //there is no AST in the envs for the specified method
             //...so add unless...
             if (methods != null && !methods.isEmpty()) {
                 this.targetsLeft.add(new Target(methods, direct));
             }
         } else if (m.getTag() == JCTree.PROPAGATE_METHOD_POLYM) {
             JCTree.JCPropagateMethodPolym pm = (JCTree.JCPropagateMethodPolym)m;
             //important: the first item in this list
             //is the base type "C" in the decl "A,B<:C".
             //we will use this specifically later on
             methods = lookupMethods(pm);
             direct = pm.direct;
 
             //some propagate nodes wheren't found
             //maybe due to previous errors or because
             //there is no AST in the envs for the specified method
             //...so add unless...
             if (methods != null && !methods.isEmpty()) {
                 this.targetsLeft.add(new Target(methods, direct));
             }
         } else if (m.getTag() == JCTree.PROPAGATE_METHOD_OR) {
             JCTree.JCPropagateMethodOr or  = (JCTree.JCPropagateMethodOr)m;
             direct = or.direct;
             List<List<JCTree.JCMethodDecl>> oredMethods = new ArrayList<List<JCTree.JCMethodDecl>>();
             for (JCTree t : or.ored) {
                 if (t.getTag() == JCTree.PROPAGATE_METHOD_SIMPLE) {
                     JCTree.JCPropagateMethodSimple tx = (JCTree.JCPropagateMethodSimple)t;
                     methods = lookupMethods(tx);
                } else if (m.getTag() == JCTree.PROPAGATE_METHOD_POLYM) {
                     JCTree.JCPropagateMethodPolym tm = (JCTree.JCPropagateMethodPolym)t;
                     methods = lookupMethods(tm);
                 } else {
                     throw new RuntimeException("UOPS!!! this is a bug");
                 }
                 oredMethods.add(methods);
             }
             this.targetsLeft.add(new Target(oredMethods, direct, true));
         } else {
             throw new RuntimeException("UOPS! this is a bug");
         }
     }
 
     public void process(JCTree.JCPropagate p)
     throws UnrelatedClassException {
         this.currentPropagate = p;
 
         this.targetsLeft = new ArrayList<Target>();
 
         for(JCTree m : p.nodes) {
             loadMethods(m);
         }
 
         //popping...
         Target initials = this.targetsLeft.remove(this.targetsLeft.size()-1);
 
         if (this.targetsLeft.isEmpty()) { //single node
             if (initials.isPolym()) {
                 for (JCTree.JCMethodDecl m : initials.getHeadlessMethods()) {
                     currentTree = new PathTree(this.currentPropagate, m, initials.getHead(),p.thrown);
                     currentTree.setupThrowPath();
 
                 }
             } else if (initials.isOred()) {
                 for (JCTree.JCMethodDecl m : initials.getOreds()) {
                     currentTree = new PathTree(this.currentPropagate, m, p.thrown);
                     currentTree.setupThrowPath();
                 }
             } else {
                 currentTree = new PathTree(this.currentPropagate, initials.getHead(), p.thrown);
                 currentTree.setupThrowPath();
             }
         } else {
             this.nextTargets = this.targetsLeft.remove(this.targetsLeft.size()-1);
 
             if (initials.isOred()) { //handling site is ored
                 boolean lastFound = false;
                 for (JCTree.JCMethodDecl m : initials.getOreds()) {
                     this.currentTree = new PathTree(this.currentPropagate, m, p.thrown);
                     buildpath(this.currentTree.node);
                     lastFound = lastFound || this.currentTree.atLeastOnePathFound;
                     //this.currentTree.atLeastOnePathFound = this.currentTree.atLeastOnePathFound || false;
                 }
                 this.currentTree.atLeastOnePathFound = lastFound;
             } else if (initials.isPolym()) { //handling site is polym
                 for (JCTree.JCMethodDecl m : initials.getHeadlessMethods()) {
                     this.currentTree = new PathTree(this.currentPropagate, m, initials.getHead(), p.thrown);
                     buildpath(this.currentTree.node);
                 }
             } else { //handling site is simple
                 this.currentTree = new PathTree(this.currentPropagate, initials.getHead(), p.thrown);
                 buildpath(this.currentTree.node);
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
         return this.nextTargets.match(m);
         //return m.sym == this.nextTargets.methods.get(0).sym;
     }
 
     boolean shouldBeDirect() {
         return this.nextTargets.direct;
     }
 
     public void visitNewClass(JCNewClass tree) {
         processMethod(lookupMethod(tree));
         scan(tree.encl);
         scan(tree.clazz);
         //scan(tree.typeargs);
         scan(tree.args);
         scan(tree.def);
 
     }
 
     public void visitApply(JCTree.JCMethodInvocation tree) {
         processMethod(lookupMethod(tree));
         //scan(tree.typeargs);
         scan(tree.meth);
         scan(tree.args);
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
                 currentTree.setRoot(found, this.nextTargets.matched.get(0));
                 currentTree.setupThrowPath();
                 currentTree.node = bk;
                 //a polym node might be the first (raising-site) node. exhaust it:
                 if (this.nextTargets.isPolym()) {
                     for (JCTree.JCMethodDecl met : this.nextTargets.getHeadlessMatches()) {
                         currentTree.node = bk;
                         currentTree.setRoot(
                                 met,
                                 this.nextTargets.matched.get(0));
                         currentTree.setupThrowPath();
                     }
                 }
             } else { //intermediary match
                 //-load the next nextTargets
                 //-proceed exploration of each method in each body of each method matched
                 Target tgs = this.nextTargets;
                 this.nextTargets = targetsLeft.remove(targetsLeft.size()-1);
                 if (tgs.isPolym()) {
                     //for all subtypes+supertype X, navigate on X:m()'s body
                     for (JCTree.JCMethodDecl t : tgs.getMethods()) {
                        PathNode bk = currentTree.node;
                        currentTree.setRoot(t, tgs.getHead());
                        buildpath(currentTree.node);
                        currentTree.node = bk;
                     }
                 } else {
                     PathNode bk = currentTree.node;
                     currentTree.setRoot(tgs.matched.get(0));
                     buildpath(currentTree.node);
                     currentTree.node = bk;
                 }
                 targetsLeft.add(this.nextTargets);
                 this.nextTargets = tgs;
             }
         } else /*matchTargets*/ if (found.body == null) {
             //found is abstract. dead end
         } else if (shouldBeDirect()) {
             //we didn't match the current, and we are looking for a direct call
             //don't proceed.
         } else { //not a propagate node and we are working with indirect call.
                  //keep searching
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
         public JCTree.JCPropagate currentPropagate;
 
         PathTree(JCTree.JCPropagate p, JCTree.JCMethodDecl m, JCTree.JCExpression thr) {
             this.currentPropagate = p;
             this.thrown = thr;
             this.atLeastOnePathFound = false;
             setRoot(m);
         }
 
         PathTree(JCTree.JCPropagate p, JCTree.JCMethodDecl m, JCTree.JCMethodDecl base, JCTree.JCExpression thr) {
             this.currentPropagate = p;
             this.thrown = thr;
             this.atLeastOnePathFound = false;
             setRoot(m,base);
         }
 
         void setRoot(JCTree.JCMethodDecl m) {
             this.node = new PathNode(this.currentPropagate, m, this.node);
         }
 
         void setRoot(JCTree.JCMethodDecl m, JCTree.JCMethodDecl base) {
             this.node = new PathNode(this.currentPropagate, m, base, this.node);
         }
 
         void setupThrowPath() {
             this.atLeastOnePathFound = true;
             ScriptPropagate.addPath(this.currentPropagate.thrown.toString(), this.pathAsString(this.node));
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
         public JCTree.JCPropagate currentPropagate;
 
         public PathNode(JCTree.JCPropagate p, JCTree.JCMethodDecl m, PathNode parent) {
             this.currentPropagate = p;
             this.parent = parent;
             this.self = m;
             this.base = null;
         }
 
         public PathNode(JCTree.JCPropagate p, JCTree.JCMethodDecl m, JCTree.JCMethodDecl base, PathNode parent) {
             this.currentPropagate = p;
             this.parent = parent;
             this.self = m;
             this.base = base;
         }
 
         public void setupThrows(JCTree.JCExpression t) {
             if (!alreadyThrows(t)) {
                 JCTree.JCMethodDecl uppermost =
                         this.base == null ? this.self : this.base;
                 JCTree.JCClassDecl clazz = getClassForType(uppermost.sym.owner.type);
 
                 //postponing overriding check
                 overridingCheckList.add(
                         new OverridingTriple(clazz, uppermost, t, this.currentPropagate.pos()));
 
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
