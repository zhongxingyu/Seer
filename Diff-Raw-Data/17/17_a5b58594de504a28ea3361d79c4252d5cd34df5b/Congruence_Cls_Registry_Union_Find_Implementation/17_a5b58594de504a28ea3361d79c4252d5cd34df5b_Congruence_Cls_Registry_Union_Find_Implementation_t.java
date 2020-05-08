 package edu.clemson.resolve.congruence_class_demo;
 
import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
 import com.google.common.collect.TreeMultimap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 
 public class Congruence_Cls_Registry_Union_Find_Implementation<Op_Name extends Op_Name_Type> implements Congruence_Cls_Registry_Interface<Op_Name> {
 
     static Map<RegistryElement, RBES> CongruenceMap;// similar to a parent array
     static Multimap<RBES, RegistryElement> CongruMapInverse;
     Map<String, String> ClassTagMap;// Not implemented yet
     static final String CLS = "CC_";// String to use that precedes an RBES id
 
     public Congruence_Cls_Registry_Union_Find_Implementation() {
         CongruenceMap = new TreeMap<RegistryElement, RBES>();
         CongruMapInverse = TreeMultimap.<RBES, RegistryElement>create();
     }
 
     // Appends A_Lst to Op_Name's id and adds to registry
     // Requires: equivalent joined expression not already in registry
     // A_Lst only contains RBES's (not actual params)
     @Override
     public void Add_CC_for(CSC_Lst A_Lst, Op_Name Op) {
         RegistryElement newExpr = new RegistryElement(Op, A_Lst);
         RBES newClassId = new RBES();
         newClassId.numMembers++;
         // add new RBES as root of tree
         CongruenceMap.put(newClassId.toRegistryElement(), newClassId);
         CongruMapInverse.put(newClassId, newClassId.toRegistryElement());
         // add new expr as child of new RBES
         CongruenceMap.put(newExpr, newClassId);
         CongruMapInverse.put(newClassId, newExpr);
     }
 
     // True if no equivalent expression is in registry
     // an equivalent expression is one where operands in the same postition
     // share the same root and have the same operator
     @Override
     public boolean Would_be_Ext(CSC_Lst A_Lst, Op_Name Op) {
         // Ensure arguments are roots
         if (A_Lst != null) {
             A_Lst = GetRootedArgList(A_Lst);
         }
         if (CongruenceMap.containsKey(new RegistryElement(Op, A_Lst))) {
             return false;
         }
         return true;
     }
 
     @Override
     public void Append_CC_of(RBES RBSet, CSC_Lst L) {
         RBES rootOfRBSet = CompressingFind(RBSet.toRegistryElement());
         L.members.add(rootOfRBSet);
     }
 
     @Override
     public boolean Is_Eqv_SC(String S) {
         return (S != null);
     }
 
     // Req: RE is a key in CongruenceMap
     // Returns the root a Registry element.  Changes all nodes in path
     // to root to point to the root directly.
     // This can be done using recursion, but will probably use more memory
     private RBES CompressingFind(RegistryElement RE) {
         RBES parent = CongruenceMap.get(RE);
         RegistryElement child = RE;
         // Early exit possible
         if (RE.RootOp != null && RE.RootOp.equals(parent)) {
             return parent;
         }
         Stack<RegistryElement> toUpdate = new Stack<RegistryElement>();
         // child.RootOp is null unless that RE represents an RBES on the key side
         while (child.RootOp == null || !parent.equals(child.RootOp)) {
             toUpdate.push(child);
             child = parent.toRegistryElement();
             parent = CongruenceMap.get(new RegistryElement(parent));
             assert (parent != null);
         }
         while (!toUpdate.isEmpty()) {
             RBES toRemove = CongruenceMap.put(toUpdate.peek(), parent);
             toRemove.numMembers--;
             parent.numMembers++;
             CongruMapInverse.remove(toRemove, toUpdate.peek());
             CongruMapInverse.put(parent, toUpdate.pop());
         }
         return parent;
     }
 
     // Req: every element of A_Lst must be in the CongruenceMap
     // This does compressing find on each element of A_Lst and updates A_Lst
     private CSC_Lst GetRootedArgList(CSC_Lst A_Lst) {
         // Ensure arguments are roots
         if (A_Lst != null) {
             for (RBES p : A_Lst.members) {
                 p = CompressingFind(p.toRegistryElement());
             }
         }
         return A_Lst;
     }
     // Requires the equivalent expr to already be in reg
     // i.e. op cant be null
 
     @Override
     public RBES Get_RBEC_for(CSC_Lst A_Lst, Op_Name Op) {
         // Ensure arguments are roots
         if (A_Lst != null) {
             A_Lst = GetRootedArgList(A_Lst);
             return CompressingFind(new RegistryElement(Op, A_Lst));
         } else {
             return CompressingFind(new RegistryElement(Op));
         }
     }
 
     // Does a union based on rank
     @Override
     public void Make_Congruent(RBES S, RBES T) {
         if (S.numMembers < T.numMembers) {
             RBES temp = T;
             T = S;
             S = temp;
         }
         S.numMembers++;
         T.numMembers--;
         RBES overwritten = CongruenceMap.put(T.toRegistryElement(), S);
         CongruMapInverse.remove(overwritten, T.toRegistryElement());
         CongruMapInverse.put(S, T.toRegistryElement());
         // for all RegistryElements that contain overwritten in their CSC_Lst
         //  replace overwritten with S
         for(RegistryElement reg : CongruenceMap.keySet()){
             if(reg.A_Lst!=null && reg.A_Lst.members.contains(overwritten)){
                RBES whatRegMapsTo = CongruenceMap.get(reg);
                CongruenceMap.remove(reg);
                CongruMapInverse.remove(whatRegMapsTo, reg);
                 int index = reg.A_Lst.members.indexOf(overwritten);
                 reg.A_Lst.members.set(index,S);
                // Overwrite old map entry (otherwise it won't sort correctly)
                CongruenceMap.put(reg, whatRegMapsTo);
                CongruMapInverse.put(whatRegMapsTo,reg);
             }
         }
 
     }
 
     // Inverts the Congruence map and shows the translated contents by values.
     @Override
     public String showRegistry(String title) {
         String rStr = title + "\nClass\tMembers\n------\t----";
 
        SetMultimap<RegistryElement, RBES> multimap = Multimaps.forMap(CongruenceMap);
        Multimap<RBES, RegistryElement> inverse = Multimaps.invertFrom(multimap, HashMultimap.<RBES, RegistryElement>create());
         for (RBES classId : inverse.keySet()) {
             rStr += "\n" + CLS + classId.id + " <-\n";
             Iterator<RegistryElement> exp = inverse.get(classId).iterator();
             while (exp.hasNext()) {
                 RegistryElement exprN = exp.next();
                 rStr += "\t";
                 if (exprN.RootOp != null) {
                     rStr += CLS + exprN.RootOp.id + "\n";
                     continue;
                 } else {
                     rStr += exprN.Op.getNameOf();
                 }
                 // get params if there are any
                 if (exprN.A_Lst != null) {
                     String params = "(";
                     for (RBES p : exprN.A_Lst.members) {
                         params += CLS + p.id + ",";
                     }
                     if (params.length() > 1) {
                         params = params.substring(0, params.length() - 1);
                     }
                     rStr += params + ")";
                 }
                 rStr += "\n";
             }
 
         }
         return rStr;
     }
 }
