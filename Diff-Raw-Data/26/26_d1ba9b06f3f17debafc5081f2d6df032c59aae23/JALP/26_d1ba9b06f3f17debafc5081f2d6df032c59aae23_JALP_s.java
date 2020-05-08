 package uk.co.mtford.jalp;
 
 import org.apache.commons.io.FileUtils;
 import uk.co.mtford.jalp.abduction.Definition;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.*;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.InEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.*;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 02/06/2012
  * Time: 11:36
  * To change this template use File | Settings | File Templates.
  */
 public class JALP {
 
     public static void getVisualizer(String folderName, RuleNode root) throws IOException {
         File visualizer = new File("visualizer2");
         File destination = new File(folderName);
         FileUtils.copyDirectory(visualizer, destination);
         String js = "var data=\""+root.toJSON()+"\"";
         FileUtils.writeStringToFile(new File(folderName+"/output.js"),js);
     }
 
     public static void reduceResult(Result result) {
         List<PredicateInstance> substAbducibles = new LinkedList<PredicateInstance>();
         List<DenialInstance> substDenials = new LinkedList<DenialInstance>();
         List<IEqualityInstance> substEqualities = new LinkedList<IEqualityInstance>();
         List<IConstraintInstance> substConstraints = new LinkedList<IConstraintInstance>();
         List<VariableInstance> queryVariables = new LinkedList<VariableInstance>();
 
         for (IInferableInstance inferable:result.getQuery()) queryVariables.addAll(inferable.getVariables());
 
         Set<VariableInstance> relevantVariables = new HashSet<VariableInstance>(queryVariables);
         HashMap<VariableInstance,IUnifiableAtomInstance> relevantAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
 
 
         for (PredicateInstance a:result.getStore().abducibles) {
            substAbducibles.add((PredicateInstance)a.performSubstitutions(result.getAssignments()));
         }
 
         for (DenialInstance d:result.getStore().denials) {
            substDenials.add((DenialInstance) d.performSubstitutions(result.getAssignments()));
         }
 
         for (IEqualityInstance e:result.getStore().equalities) {
            substEqualities.add((IEqualityInstance) e.performSubstitutions(result.getAssignments()));
         }
 
         for (IConstraintInstance c:result.getStore().constraints) {
            substConstraints.add((IConstraintInstance)c.performSubstitutions(result.getAssignments()));
         }
 
         Set<IUnifiableAtomInstance> keySet = new HashSet<IUnifiableAtomInstance>(result.getAssignments().keySet());
 
         for (IUnifiableAtomInstance key:keySet) {
 
 
             if (queryVariables.contains(key)) {
                 IUnifiableAtomInstance value = result.getAssignments().get(key);
 
                 while (keySet.contains(value)) value = result.getAssignments().get(value);
                 relevantAssignments.put((VariableInstance) key,value);
             }
         }
 
         result.setAssignments(relevantAssignments);
         result.getStore().abducibles=substAbducibles;
         result.getStore().denials = substDenials;
         result.getStore().equalities=substEqualities;
         result.getStore().constraints=substConstraints;
 
     }
 
     public static TrueInstance makeTrueInstance() {
         return new TrueInstance();
     }
     public static FalseInstance makeFalseInstance() {
         return new FalseInstance();
     }
 
     public static CharConstantInstance makeCharConstantInstance(String string) {
         return new CharConstantInstance(string);
     }
     public static IntegerConstantInstance makeIntegerConstantInstance(int n) {
         return new IntegerConstantInstance(n);
     }
 
     public static VariableInstance makeVariableInstance(String name) {
         return new VariableInstance(name);
     }
 
     public static PredicateInstance makePredicateInstance(String name, List<IUnifiableAtomInstance> parameters) {
         return new PredicateInstance(name,parameters);
     }
     public static PredicateInstance makePredicateInstance(String name, IUnifiableAtomInstance ... parameters) {
         return new PredicateInstance(name,parameters);
     }
 
     public static EqualityInstance makeEqualityInstance(IUnifiableAtomInstance left, IUnifiableAtomInstance right) {
         return new EqualityInstance(left,right);
     }
 
     public static InEqualityInstance makeInEqualityInstance(IUnifiableAtomInstance left, IUnifiableAtomInstance right) {
         return new InEqualityInstance(left,right);
     }
     public static InEqualityInstance makeInEqualityInstance(EqualityInstance equalityInstance) {
         return new InEqualityInstance(equalityInstance);
     }
 
     public static IntegerConstantListInstance makeIntegerConstantListInstance(Collection<IntegerConstantInstance> integers) {
         return new IntegerConstantListInstance(integers);
     }
     public static IntegerConstantListInstance makeIntegerConstantListInstance(IntegerConstantInstance[] integers) {
         return new IntegerConstantListInstance(integers);
     }
     public static IntegerConstantListInstance makeIntegerConstantListInstance(int ... integers) {
         return new IntegerConstantListInstance(integers);
     }
     public static IntegerConstantListInstance makeIntegerConstantListInstance() {
         return new IntegerConstantListInstance();
     }
 
     public static CharConstantListInstance makeCharConstantListInstance(Collection<CharConstantInstance> constants) {
         return new CharConstantListInstance(constants);
     }
     public static CharConstantListInstance makeCharConstantListInstance(CharConstantInstance[] constants) {
         return new CharConstantListInstance(constants);
     }
     public static CharConstantListInstance makeCharConstantListInstance(String ... strings) {
         return new CharConstantListInstance(strings);
     }
     public static CharConstantListInstance makeCharConstantListInstance() {
         return new CharConstantListInstance();
     }
 
     public static NegationInstance makeNegationInstance(IInferableInstance subformula) {
         return new NegationInstance(subformula);
     }
 
     public static InIntegerListConstraintInstance makeInIntegerListConstraint(ITermInstance left, IntegerConstantListInstance right) {
         return new InIntegerListConstraintInstance(left,right);
     }
 
     public static InConstantListConstraintInstance makeInConstantListConstraint(ITermInstance left, CharConstantListInstance right) {
         return new InConstantListConstraintInstance(left,right);
     }
 
     public static GreaterThanConstraintInstance makeGreaterThanConstraintInstance(ITermInstance left, ITermInstance right) {
         return new GreaterThanConstraintInstance(left,right);
     }
 
     public static GreaterThanEqConstraintInstance makeGreaterThanEqConstraintInstance(ITermInstance left, ITermInstance right) {
         return new GreaterThanEqConstraintInstance(left,right);
     }
 
     public static LessThanConstraintInstance makeLessThanConstraintInstance(ITermInstance left, ITermInstance right) {
         return new LessThanConstraintInstance(left,right);
     }
 
     public static LessThanEqConstraintInstance makeLessThanEqConstraintInstance(ITermInstance left, ITermInstance right) {
         return new LessThanEqConstraintInstance(left,right);
     }
 
     public static NegativeConstraintInstance makeNegativeConstraintInstance(ConstraintInstance constraintInstance) {
         return new NegativeConstraintInstance(constraintInstance);
     }
 
     public static DenialInstance makeDenialInstance(List<VariableInstance> universalVariables,List<IInferableInstance> body) {
         return new DenialInstance(body,universalVariables);
     }
     public static DenialInstance makeDenialInstance(List<VariableInstance> universalVariables,IInferableInstance ... body) {
         return new DenialInstance(universalVariables, body);
     }
     public static DenialInstance makeDenialInstance(List<IInferableInstance> body) {
         return new DenialInstance(body);
     }
     public static DenialInstance makeDenialInstance(IInferableInstance ... body) {
         return new DenialInstance(body);
     }
     public static DenialInstance makeDenialInstance() {
         return new DenialInstance();
     }
 
     public static Definition makeFact(String headPredicateName, ConstantInstance[] headParameters) {
         return new Definition(new PredicateInstance(headPredicateName,headParameters), (List<IInferableInstance>) null,null);
     }
     public static Definition makeFact(String headPredicateName, List<ConstantInstance> headParameters) {
         return new Definition(new PredicateInstance(headPredicateName,new LinkedList<IUnifiableAtomInstance>(headParameters)), (List<IInferableInstance>) null,null);
     }
 
     public static Definition makeRule(String headPredicateName, VariableInstance[] headParameters, IInferableInstance[] body) {
         return new Definition(new PredicateInstance(headPredicateName,headParameters),body,null);
     }
     public static Definition makeRule(String headPredicateName, List<ConstantInstance> headParameters, IInferableInstance[] body) {
         return new Definition(new PredicateInstance(headPredicateName,new LinkedList<IUnifiableAtomInstance>(headParameters)),body,null);
     }
     public static Definition makeRule(String headPredicateName, VariableInstance[] headParameters, List<IInferableInstance> body) {
         return new Definition(new PredicateInstance(headPredicateName,headParameters),body,null);
     }
     public static Definition makeRule(String headPredicateName, List<ConstantInstance> headParameters, List<IInferableInstance> body) {
         return new Definition(new PredicateInstance(headPredicateName,new LinkedList<IUnifiableAtomInstance>(headParameters)),body,null);
     }
 
 }
 
