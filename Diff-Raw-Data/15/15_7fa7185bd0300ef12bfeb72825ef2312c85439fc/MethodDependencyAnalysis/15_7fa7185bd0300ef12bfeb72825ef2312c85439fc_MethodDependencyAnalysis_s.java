 package depend;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import com.ibm.wala.classLoader.IBytecodeMethod;
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
 import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
 import com.ibm.wala.ipa.callgraph.AnalysisCache;
 import com.ibm.wala.ipa.callgraph.AnalysisOptions;
 import com.ibm.wala.ipa.callgraph.AnalysisScope;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.impl.Everywhere;
 import com.ibm.wala.ipa.cha.ClassHierarchy;
 import com.ibm.wala.ipa.cha.ClassHierarchyException;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.ssa.IR;
 import com.ibm.wala.ssa.SSAArrayLoadInstruction;
 import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
 import com.ibm.wala.ssa.SSAArrayStoreInstruction;
 import com.ibm.wala.ssa.SSAFieldAccessInstruction;
 import com.ibm.wala.ssa.SSAGetInstruction;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.ssa.SSAPutInstruction;
 import com.ibm.wala.types.FieldReference;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.collections.ReverseIterator;
 import com.ibm.wala.util.config.AnalysisScopeReader;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.graph.traverse.Topological;
 import com.ibm.wala.util.io.FileProvider;
 import com.ibm.wala.util.warnings.Warnings;
 import com.ibm.wala.viz.DotUtil;
 
 import depend.RWSet.AccessInfo;
 import depend.util.CallGraphGenerator;
 import depend.util.SimpleGraph;
 import depend.util.Timer;
 import depend.util.Util;
 
 /**
  * Method dependency analysis based on:
  * 
  * - Read-Write (RW) sets to object fields - propagation of accesses through
  * private method calls
  * 
  * @author damorim
  * 
  */
 public class MethodDependencyAnalysis {
 
   /**
    * constants
    */
   // this is expensive: it demands the creation of the
   // application call graph
   private static final boolean PROPAGATE_CALLS = true;
 
   /**
    * instance fields
    */
   AnalysisScope scope;
   ClassHierarchy cha;
   private AnalysisOptions options;
   // several caches
   private static AnalysisCache cache;
   // application(instance)-specific caches
   private Map<IMethod, RWSet> rwSets = new HashMap<IMethod, RWSet>();
 
   /**
    * parse input and call init
    * 
    * @param args
    * @throws IOException
    * @throws IllegalArgumentException
    * @throws WalaException
    * @throws CancelException
    */
   public MethodDependencyAnalysis(Properties p) throws IOException,
       IllegalArgumentException, WalaException, CancelException {
     init(p);
     if (Util.getBooleanProperty("printWalaWarnings")) {
       System.out.println(Warnings.asString());
     }
   }
 
   /**
    * clear all internal data
    * 
    * @throws Exception
    */
   public void reset() throws Exception {
     scope = null;
     cha = null;
     options = null;
     cache = null;
     rwSets = new HashMap<IMethod, RWSet>();
   }
 
   /**
    * all the work is done here!
    * 
    * in several steps
    * 
    * @param appJar
    *          path to the .jar file with all .class application class files
    * @param exclusionFile
    *          a WALA specification indicating what class files to ignore
    * 
    * @throws IOException
    * @throws ClassHierarchyException
    * @throws WalaException
    * @throws CancelException
    */
   private void init(Properties p) throws IOException, ClassHierarchyException,
       WalaException, CancelException {
 
     String appJar = (String) p.get("appJar");
     if (appJar == null) {
       throw new UnsupportedOperationException(
           "expected command-line to include -appJar");
     }
     String exclusionFile = p.getProperty("exclusionFile",
         CallGraphTestUtil.REGRESSION_EXCLUSIONS);
     Warnings.clear();
     File tmp = (exclusionFile != null) ? (new FileProvider())
         .getFile(exclusionFile) : new File(
         CallGraphTestUtil.REGRESSION_EXCLUSIONS);
 
     Timer timer = new Timer();
     boolean debugTime = Boolean.parseBoolean(p.getProperty("measureTime"));
 
     if (debugTime) {
       timer.start();
     }
     // very important: define scope of analysis!
     // + relevant: what is in appJar and Java libraries
     // - not relevant: what is in exclusionFile
     scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, tmp);
     if (debugTime) {
       timer.stop();
       System.out.println("<< timing");
       timer.show("done building scope");
     }
 
     // Class Hierarchy Analysis (CHA)
     cha = ClassHierarchy.make(scope);
     options = new AnalysisOptions(scope, null);
     cache = new AnalysisCache();
     if (debugTime) {
       timer.stop();
       timer.show("done building CHA");
     }
 
     // Extracting direct read-write sets for all methods
     // and classes within the analysis scope
     directRWSets();
     if (debugTime) {
       timer.stop();
       timer.show("building direct RW Sets");
     }
 
     if (PROPAGATE_CALLS) {
 
       // building the call graph
       CallGraphGenerator cgg = new CallGraphGenerator(scope, cha);
       Graph<CGNode> graph = cgg.getCallGraph();
       if (debugTime) {
         timer.stop();
         timer.show("done building call graph");
       }
 
       if (Util.getBooleanProperty("genCallGraph")) {
         System.out.println("=== (dot)");
         DotUtil.dotify(graph, null, PDFTypeHierarchy.DOT_FILE, "/tmp/cg.pdf",
             Util.getStringProperty("dotPath"));
         System.out.println("===");
       }
 
       // propagate RWSet from callees (only private methods) to callers
       propagateRWSets(graph);
       if (debugTime) {
         timer.stop();
         timer.show("done propagating RW sets");
       }
 
     }
 
     if (debugTime) {
       System.out.println(">> timing");
     }
 
   }
 
   /**
    * This method navigates through each and every method in the scope of
    * analysis (see scope) and builds a RWSet object for that method. It uses CHA
    * to perform the navigation.
    */
   private void directRWSets() {
     // finds rw-sets for all application methods
     List<IClass> toVisit = new ArrayList<IClass>();
     IClass root = cha.getRootClass();
     toVisit.add(root);
     Set<IMethod> visited = new HashSet<IMethod>();
     while (!toVisit.isEmpty()) {
       IClass cur = toVisit.remove(0);
       // want to visit all classes regardless it is application
       // as not application classes can be parent of application
       // classes.
       toVisit.addAll(cha.getImmediateSubclasses(cur));
       if (Util.isAppClass(cur)) {
         for (IMethod imethod : cur.getAllMethods()) {
           if (imethod.isNative() || imethod.isAbstract()
               || visited.contains(imethod)) {
             continue;
           }
           visited.add(imethod);
           updateRWSet(imethod);
         }
       }
     }
   }
 
   /***
    * This method builds a RWSet for a given method. It does **not** consider
    * indirect field reads and writes through other (say, private) methods
    **/
   private void updateRWSet(IMethod imethod) {
     // cache results for this call
     RWSet result = rwSets.get(imethod);
     if (result != null) {
       return;
     }
 
     IR ir = cache.getIRFactory().makeIR(imethod, Everywhere.EVERYWHERE,
         options.getSSAOptions());
 
     Set<RWSet.AccessInfo> readSet = new HashSet<RWSet.AccessInfo>();
     Set<RWSet.AccessInfo> writeSet = new HashSet<RWSet.AccessInfo>();
     Map<Integer, FieldReference> ssaVar = new HashMap<Integer, FieldReference>();
 
     SSAInstruction[] instructions = ir.getInstructions();
     for (int i = 0; i < instructions.length; i++) {
       SSAInstruction ins = instructions[i];
       if (ins == null)
         continue;
       int kind = -1;
       if (ins instanceof SSAGetInstruction) {
         kind = 0;
       } else if (ins instanceof SSAPutInstruction) {
         kind = 1;
       } else if (ins instanceof SSAArrayLoadInstruction) {
         kind = 2;
       } else if (ins instanceof SSAArrayStoreInstruction) {
         kind = 3;
       }
       switch (kind) {
       case -1:
         break;
       case 0:
       case 1:
         SSAFieldAccessInstruction fai = (SSAFieldAccessInstruction) ins;
         // A FieldReference object denotes an access (read or write) to a field
         FieldReference fr = fai.getDeclaredField();
         Set<RWSet.AccessInfo> set = (kind == 0) ? readSet : writeSet;
         IBytecodeMethod method = (IBytecodeMethod) ir.getMethod();
         try {
           int sourceLineNum = method.getLineNumber(method.getBytecodeIndex(i));
           AccessInfo accessInfo = RWSet.makeAccessInfo(
               method.getDeclaringClass(), method, sourceLineNum,
               fr.getDeclaringClass(), fr);
           set.add(accessInfo);
         } catch (InvalidClassFileException e) {
           e.printStackTrace();
         }
         // remembering ssa-definition if one exists
         int def = fai.getDef();
         if (def != -1) {
           ssaVar.put(def, fr);
         }
         break;
       case 2:
       case 3:
         SSAArrayReferenceInstruction ari = (SSAArrayReferenceInstruction) ins;
         int arRef = ari.getArrayRef();
         fr = ssaVar.get(arRef);
         if (fr != null) {
           set = (kind == 2) ? readSet : writeSet;
           IBytecodeMethod method1 = (IBytecodeMethod) ir.getMethod();
           try {
             int sourceLineNum = method1.getLineNumber(method1
                 .getBytecodeIndex(i));
             AccessInfo accessInfo = RWSet.makeAccessInfo(
                 method1.getDeclaringClass(), method1, sourceLineNum,
                 fr.getDeclaringClass(), fr);
             set.add(accessInfo);
           } catch (InvalidClassFileException e) {
             e.printStackTrace();
           }
         }
         break;
       default:
         throw new RuntimeException("UNEXPECTED");
       }
     }
 
     result = new RWSet(readSet, writeSet);
     rwSets.put(imethod, result);
   }
 
   /***
    * This method propagates information of RWSet from callees to callers. It
    * needs call-graph information to identify the callees of a method.
    * 
    * This propagation is important since otherwise the impact of the call of the
    * private method may not be observable.
    * 
    * @param appJar
    * @param exclusionFile
    * @throws IllegalArgumentException
    * @throws WalaException
    * @throws CancelException
    * @throws IOException
    **/
   private void propagateRWSets(Graph<CGNode> callGraph)
       throws IllegalArgumentException, WalaException, CancelException,
       IOException {
     /**
      * Propagate information across the edges of the control-flow graph. For
      * that, we use the reverse topological order so to reduce the number of
      * iterations we need to process for computing fix point.
      */
     Iterator<CGNode> it = ReverseIterator.reverse(Topological
         .makeTopologicalIter(callGraph));
     while (it.hasNext()) {
       CGNode node = it.next();
       IMethod cMethod = node.getMethod();
       if (!Util.isRelevantMethod(cMethod)) {
         continue;
       }
       RWSet rwSetC = null;
       Iterator<CGNode> it2 = callGraph.getPredNodes(node);
       while (it2.hasNext()) {
         CGNode p = it2.next();
         IMethod pMethod = p.getMethod();
         if (!Util.isRelevantMethod(pMethod)) {
           continue;
         }
         RWSet rwSetP = rwSets.get(pMethod);
         if (rwSetP == null) {
           Util.logWarning("no RW-set info for method " + pMethod.toString());
           continue;
         }
         if (rwSetC == null) {
           rwSetC = rwSets.get(cMethod);
           if (rwSetC == null) {
             Util.logWarning("no RW-set info for method " + cMethod);
             continue;
           }
         }
         // propagate!
         rwSetP.writeSet.addAll(rwSetC.writeSet);
         rwSetP.readSet.addAll(rwSetC.readSet);
       }
     }
   }
 
   public SimpleGraph getDependencies(IMethod method, boolean onlyPublicClasses,
       boolean onlyPublicMethods, int strLine) {
     if (method == null) {
       throw new RuntimeException("Could not find informed method!");
     }
 
     SimpleGraph result = new SimpleGraph();
     /********* find transitive method writers *********/
     Set<AccessInfo> reads = rwSets.get(method).readSet;
 
     if (strLine == -1) {
       findDependency(method, result, reads);
     } else {
       findDependency(method, result, reads, strLine);
     }
     return result;
   }
 
   private void findDependency(IMethod method, SimpleGraph result,
       Set<AccessInfo> reads, int strLine) {
     boolean onlyPublicClasses = false;
     boolean onlyPublicMethods = false;
     for (AccessInfo access : reads) {
       int line = access.accessLineNumber;
       if (line != strLine) {
         System.out.println("linha:" + line);
         continue;
       }
      fillGraph(method, result, onlyPublicClasses, onlyPublicMethods, access,
          line);
     }
   }
 
   private void findDependency(IMethod method, SimpleGraph result,
       Set<AccessInfo> reads) {
     boolean onlyPublicClasses = false;
     boolean onlyPublicMethods = false;
     for (AccessInfo access : reads) {
      int line = access.accessLineNumber;
      fillGraph(method, result, onlyPublicClasses, onlyPublicMethods, access,
          line);
     }
   }
 
   private void fillGraph(IMethod method, SimpleGraph result,
      boolean onlyPublicClasses, boolean onlyPublicMethods, AccessInfo access,
      int line) {
     FieldReference fr = access.fieldDefinition;
     for (Map.Entry<IMethod, RWSet> entry : rwSets.entrySet()) {
       IMethod writer = entry.getKey();
       if (onlyPublicClasses && !writer.getDeclaringClass().isPublic()) {
         continue;
       }
       if (onlyPublicMethods && !writer.isPublic()) {
         continue;
       }
       Set<AccessInfo> writeSet = entry.getValue().writeSet;
       for (AccessInfo writeAccessInfo : writeSet) {
         if (writeAccessInfo.fieldDefinition.equals(fr)) {
          result.getNode(writer).add(new SimpleGraph.Edge(method, fr, line));
         }
       }
     }
   }
 }
