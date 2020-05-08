 package edu.uci.ics.algebricks.compiler.algebra.operators.physical;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uci.ics.algebricks.api.constraints.AlgebricksPartitionConstraint;
 import edu.uci.ics.algebricks.api.exceptions.AlgebricksException;
 import edu.uci.ics.algebricks.compiler.algebra.base.ILogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalVariable;
 import edu.uci.ics.algebricks.compiler.algebra.base.PhysicalOperatorTag;
 import edu.uci.ics.algebricks.compiler.algebra.metadata.IMetadataProvider;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.AbstractLogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.IOperatorSchema;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.InsertDeleteOperator;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.InsertDeleteOperator.Kind;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.OrderOperator.IOrder.OrderKind;
 import edu.uci.ics.algebricks.compiler.algebra.properties.ILocalStructuralProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPartitioningProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPartitioningRequirementsCoordinator;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPhysicalPropertiesVector;
 import edu.uci.ics.algebricks.compiler.algebra.properties.LocalOrderProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.OrderColumn;
 import edu.uci.ics.algebricks.compiler.algebra.properties.PhysicalRequirements;
 import edu.uci.ics.algebricks.compiler.algebra.properties.StructuralPropertiesVector;
 import edu.uci.ics.algebricks.compiler.algebra.properties.UnorderedPartitionedProperty;
 import edu.uci.ics.algebricks.compiler.optimizer.base.IOptimizationContext;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.base.IHyracksJobBuilder;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.impl.JobGenContext;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.impl.JobGenHelper;
 import edu.uci.ics.algebricks.utils.Pair;
 import edu.uci.ics.hyracks.api.dataflow.IOperatorDescriptor;
 import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
 import edu.uci.ics.hyracks.api.job.JobSpecification;
 
 public class InsertDeletePOperator extends AbstractPhysicalOperator {
 
     private LogicalVariable payload;
     private List<LogicalVariable> keys;
 
     public InsertDeletePOperator(LogicalVariable payload, List<LogicalVariable> keys) {
         this.payload = payload;
         this.keys = keys;
     }
 
     @Override
     public PhysicalOperatorTag getOperatorTag() {
         return PhysicalOperatorTag.INSERT_DELETE;
     }
 
     @Override
     public void computeDeliveredProperties(ILogicalOperator op, IOptimizationContext context) {
         AbstractLogicalOperator op2 = (AbstractLogicalOperator) op.getInputs().get(0).getOperator();
         deliveredProperties = (StructuralPropertiesVector) op2.getDeliveredPhysicalProperties().clone();
     }
 
     @Override
     public PhysicalRequirements getRequiredPropertiesForChildren(ILogicalOperator op,
             IPhysicalPropertiesVector reqdByParent) {
         IPartitioningProperty pp = new UnorderedPartitionedProperty(new HashSet<LogicalVariable>(keys), null);
         List<ILocalStructuralProperty> orderProps = new LinkedList<ILocalStructuralProperty>();
         for (LogicalVariable k : keys) {
             orderProps.add(new LocalOrderProperty(new OrderColumn(k, OrderKind.ASC)));
         }
         StructuralPropertiesVector[] r = new StructuralPropertiesVector[] { new StructuralPropertiesVector(pp,
                 orderProps) };
         return new PhysicalRequirements(r, IPartitioningRequirementsCoordinator.NO_COORDINATION);
     }
 
     @Override
     public void contributeRuntimeOperator(IHyracksJobBuilder builder, JobGenContext context, ILogicalOperator op,
             IOperatorSchema propagatedSchema, IOperatorSchema[] inputSchemas, IOperatorSchema outerPlanSchema)
             throws AlgebricksException {
         InsertDeleteOperator insertDeleteOp = (InsertDeleteOperator) op;
         IMetadataProvider<?, ?> mp = context.getMetadataProvider();
 
         JobSpecification spec = builder.getJobSpec();
         RecordDescriptor inputDesc = JobGenHelper.mkRecordDescriptor(op.getInputs().get(0).getOperator(),
                 inputSchemas[0], context);
 
         Pair<IOperatorDescriptor, AlgebricksPartitionConstraint> runtimeAndConstraints = null;
         if (insertDeleteOp.getOperation() == Kind.INSERT)
             runtimeAndConstraints = mp.getInsertRuntime(insertDeleteOp.getDatasetName(), propagatedSchema, keys,
                     payload, inputDesc, context, spec);
         else
             runtimeAndConstraints = mp.getDeleteRuntime(insertDeleteOp.getDatasetName(), propagatedSchema, keys,
                    inputDesc, context, spec);
 
         builder.contributeHyracksOperator(insertDeleteOp, runtimeAndConstraints.first);
         builder.contributeAlgebricksPartitionConstraint(runtimeAndConstraints.first, runtimeAndConstraints.second);
         ILogicalOperator src = insertDeleteOp.getInputs().get(0).getOperator();
         builder.contributeGraphEdge(src, 0, insertDeleteOp, 0);
     }
 
     @Override
     public boolean isMicroOperator() {
         return false;
     }
 
 }
