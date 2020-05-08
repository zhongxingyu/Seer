 package jp.ac.osaka_u.ist.sel.metricstool.pdg;
 
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableUsageInfo;
 
 
 /**
  * PDGŐm[h\NX
  * 
  * @author t-miyake
  *
  */
 public class PDGControlNode extends ControllableNode<ConditionalBlockInfo> {
 
     public PDGControlNode(final ConditionalBlockInfo conditionalBlock) {
         super(conditionalBlock);
 
         this.text = "ControlNode : " + conditionalBlock.getConditionalClause().getText() + "<"
                 + conditionalBlock.getConditionalClause().getFromLine() + ">";
     }
 
     @Override
     protected Set<VariableInfo<? extends UnitInfo>> extractDefinedVariables(
             ConditionalBlockInfo core) {
         Set<VariableInfo<? extends UnitInfo>> definedVariables = new HashSet<VariableInfo<? extends UnitInfo>>();
 
         for (VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>> usage : core
                 .getConditionalClause().getCondition().getVariableUsages()) {
             if (usage.isAssignment()) {
                 definedVariables.add(usage.getUsedVariable());
             }
         }
         return definedVariables;
     }
 
     @Override
     protected SortedSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> extractVariableReference(
             ConditionalBlockInfo core) {
         final SortedSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> references = new TreeSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>>();
 
         for (VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>> usage : core
                 .getConditionalClause().getCondition().getVariableUsages()) {
             if (usage.isReference()) {
                 references.add(usage);
             }
         }
 
         return references;
     }
 
     /**
      * ̐m[hɐ䂳m[hǉ
      * @param controlledNode 䂳m[h
      */
     public void addControlDependingNode(final PDGNode<?> controlledNode) {
         if (null == controlledNode) {
             throw new IllegalArgumentException();
         }
 
        final ControlDepedneceEdge controlFlow = new ControlDepedneceEdge(this, controlledNode);
         this.addFowardEdge(controlFlow);
         controlledNode.addBackwardEdge(controlFlow);
     }
 
 }
