 package racefix;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import sabazios.A;
 import sabazios.domains.PointerForValue;
 import sabazios.util.CodeLocation;
 import sabazios.util.U;
 
 import com.ibm.wala.classLoader.CallSiteReference;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
 import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
 import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
 import com.ibm.wala.shrikeBT.MethodData;
 import com.ibm.wala.ssa.DefUse;
 import com.ibm.wala.ssa.ISSABasicBlock;
 import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
 import com.ibm.wala.ssa.SSAGetInstruction;
 import com.ibm.wala.ssa.SSAInstruction;
 import com.ibm.wala.ssa.SSAInvokeInstruction;
 import com.ibm.wala.ssa.SSANewInstruction;
 import com.ibm.wala.ssa.SSAPhiInstruction;
 import com.ibm.wala.ssa.SSAReturnInstruction;
 import com.ibm.wala.types.FieldReference;
 import com.ibm.wala.types.MethodReference;
 import com.sun.org.apache.bcel.internal.generic.SALOAD;
 
 /**
  * 
  * @author caius
  * 
  */
 public class AccessTrace {
   private final CGNode n;
   private final int v;
   private final A a;
   private LinkedHashSet<PointerKey> pointers = new LinkedHashSet<PointerKey>();
   private LinkedHashSet<InstanceKey> instances = new LinkedHashSet<InstanceKey>();
   private PointerForValue pv;
 
   public AccessTrace(A a, CGNode n, int v) {
     this.a = a;
     this.n = n;
     this.v = v;
     pv = a.pointerForValue;
   }
 
   public void compute() {
     CGNode n2 = n;
     int v2 = v;
     solveNV(n2, v2);
   }
 
   private void solveNV(CGNode node, int value) {
     LocalPointerKey lpk = pv.get(node, value);
     Iterator<Object> succNodes = a.heapGraph.getSuccNodes(lpk);
     while (succNodes.hasNext()) {
       InstanceKey o = (InstanceKey) succNodes.next();
       instances.add(o);
 
       DefUse du = node.getDU();
       SSAInstruction def = du.getDef(value);
 
       if (node.getMethod().getNumberOfParameters() >= value) {
         Iterator<CGNode> predNodes = a.callGraph.getPredNodes(node);
         while (predNodes.hasNext()) {
           CGNode cgNode = (CGNode) predNodes.next();
 
           Iterator<CallSiteReference> possibleSites = a.callGraph.getPossibleSites(cgNode, node);
           while (possibleSites.hasNext()) {
             CallSiteReference callSiteReference = (CallSiteReference) possibleSites.next();
             SSAAbstractInvokeInstruction[] calls = cgNode.getIR().getCalls(callSiteReference);
 
             // now we must find the calls that have as a possible parameter
             // our node
             for (SSAAbstractInvokeInstruction ssaAbstractInvokeInstruction : calls) {
               int use = ssaAbstractInvokeInstruction.getUse(value - 1);
               solveNV(cgNode, use);
             }
           }
         }
       }
 
       // method invokation
 
       // find the def
 
       // SSAInvokeInstruction ssaII;
       // ssaII.getCallSite();
       // a.callGraph.getNumberOfTargets(node, site);
 
       // SSAReturnInstruction ssaRI;
       // ssaRI.getUse(j);
 
       if (def instanceof SSAInvokeInstruction) {
         SSAInvokeInstruction invoke = (SSAInvokeInstruction) def;
         CallSiteReference callSite = invoke.getCallSite();
        Set<CGNode> possibleTargets = a.callGraph.getPossibleTargets(node, callSite);
        for (CGNode cgNode : possibleTargets) {
           Iterator<SSAInstruction> instructionsIterator = cgNode.getIR().iterateAllInstructions();
           while (instructionsIterator.hasNext()) {
             SSAInstruction ssaInstruction = (SSAInstruction) instructionsIterator.next();
             if (ssaInstruction instanceof SSAReturnInstruction) {
               SSAReturnInstruction returnInstr = (SSAReturnInstruction) ssaInstruction;
               int use = returnInstr.getUse(0);
               solveNV(cgNode, use);
             }
           }
         }
       }
 
       if (def instanceof SSAGetInstruction) {
         SSAGetInstruction get = (SSAGetInstruction) def;
         int v1 = get.getRef();
         LocalPointerKey lpc = pv.get(node, v1);
         Iterator<Object> predNodes = a.heapGraph.getSuccNodes(lpc);
         Set<InstanceKey> objs = new HashSet<InstanceKey>();
         while (predNodes.hasNext()) {
           Object object = (Object) predNodes.next();
           objs.add((InstanceKey) object);
         }
 
         FieldReference declaredField = get.getDeclaredField();
         Iterator<Object> pred = a.heapGraph.getPredNodes(o);
         while (pred.hasNext()) {
           Object prev = (Object) pred.next();
           if (prev instanceof InstanceFieldKey) {
             InstanceFieldKey field = (InstanceFieldKey) prev;
 
             if (field.getField().getReference().equals(declaredField)) {
               Iterator<Object> predNodes2 = a.heapGraph.getPredNodes(field);
               while (predNodes2.hasNext()) {
                 Object object = (Object) predNodes2.next();
                 if (objs.contains(object)) {
                   pointers.add(field);
                   break;
                 }
               }
             }
           }
         }
         solveNV(node, get.getRef());
       }
 
       if (def instanceof SSAPhiInstruction) {
         SSAPhiInstruction phi = (SSAPhiInstruction) def;
         int numberOfUses = phi.getNumberOfUses();
         for (int i = 0; i < numberOfUses; i++) {
           int use = phi.getUse(i);
           solveNV(node, use);
         }
       }
 
     }
   }
 
   public String getTestString() {
     String s = "";
 
     for (PointerKey o : pointers) {
       if (o instanceof LocalPointerKey) {
         LocalPointerKey p = (LocalPointerKey) o;
         s += "LPK:";
         s += p.getNode().getMethod().getDeclaringClass().getName().getClassName().toString();
         s += ".";
         s += p.getNode().getMethod().getName().toString();
         s += "-v";
         s += p.getValueNumber();
         String variableName = CodeLocation.variableName(n, p.getValueNumber());
         if (variableName != null)
           s += "-" + variableName;
         s += "\n";
       }
 
       if (o instanceof InstanceFieldKey) {
         InstanceFieldKey p = (InstanceFieldKey) o;
         s += "IFK:";
         s += p.getField().getDeclaringClass().getName().getClassName().toString();
         s += ".";
         s += p.getField().getName().toString();
         s += "\n";
       }
     }
 
     for (InstanceKey o : instances) {
       if (o instanceof AllocationSiteInNode) {
         AllocationSiteInNode as = (AllocationSiteInNode) o;
         s += "O:";
         s += as.getNode().getMethod().getDeclaringClass().getName().getClassName().toString();
         s += ".";
         s += as.getNode().getMethod().getName().toString();
         s += "-new ";
         s += as.getSite().getDeclaredType().getName().getClassName().toString();
         s += "\n";
       }
     }
     return s;
   }
 }
