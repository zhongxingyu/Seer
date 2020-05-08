 /*
  * Copyright 2009-2010 by The Regents of the University of California
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * you may obtain a copy of the License from
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.uci.ics.algebricks.compiler.algebra.operators.physical;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uci.ics.algebricks.api.data.IBinaryComparatorFactoryProvider;
 import edu.uci.ics.algebricks.api.data.IBinaryHashFunctionFactoryProvider;
 import edu.uci.ics.algebricks.api.exceptions.AlgebricksException;
 import edu.uci.ics.algebricks.compiler.algebra.base.ILogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalVariable;
 import edu.uci.ics.algebricks.compiler.algebra.base.PhysicalOperatorTag;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.AbstractLogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.IOperatorSchema;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.OrderOperator.IOrder.OrderKind;
 import edu.uci.ics.algebricks.compiler.algebra.properties.ILocalStructuralProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.INodeDomain;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPartitioningProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPartitioningRequirementsCoordinator;
 import edu.uci.ics.algebricks.compiler.algebra.properties.IPhysicalPropertiesVector;
 import edu.uci.ics.algebricks.compiler.algebra.properties.LocalOrderProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.OrderColumn;
 import edu.uci.ics.algebricks.compiler.algebra.properties.PhysicalRequirements;
 import edu.uci.ics.algebricks.compiler.algebra.properties.StructuralPropertiesVector;
 import edu.uci.ics.algebricks.compiler.algebra.properties.UnorderedPartitionedProperty;
 import edu.uci.ics.algebricks.compiler.algebra.properties.ILocalStructuralProperty.PropertyType;
 import edu.uci.ics.algebricks.compiler.optimizer.base.IOptimizationContext;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.base.IHyracksJobBuilder.TargetConstraint;
 import edu.uci.ics.algebricks.runtime.hyracks.jobgen.impl.JobGenContext;
 import edu.uci.ics.algebricks.utils.Pair;
 import edu.uci.ics.hyracks.api.dataflow.IConnectorDescriptor;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.IBinaryHashFunctionFactory;
 import edu.uci.ics.hyracks.api.dataflow.value.ITuplePartitionComputerFactory;
 import edu.uci.ics.hyracks.api.job.JobSpecification;
 import edu.uci.ics.hyracks.dataflow.common.data.partition.FieldHashPartitionComputerFactory;
 import edu.uci.ics.hyracks.dataflow.std.connectors.MToNHashPartitioningMergingConnectorDescriptor;
 
 public class HashPartitionMergeExchangePOperator extends AbstractExchangePOperator {
 
     private List<OrderColumn> orderColumns;
     private List<LogicalVariable> partitionFields;
     private INodeDomain domain;
 
     public HashPartitionMergeExchangePOperator(List<OrderColumn> orderColumns, List<LogicalVariable> partitionFields,
             INodeDomain domain) {
         this.orderColumns = orderColumns;
         this.partitionFields = partitionFields;
         this.domain = domain;
     }
 
     @Override
     public PhysicalOperatorTag getOperatorTag() {
         return PhysicalOperatorTag.HASH_PARTITION_MERGE_EXCHANGE;
     }
 
     public List<OrderColumn> getOrderExpressions() {
         return orderColumns;
     }
 
     @Override
     public void computeDeliveredProperties(ILogicalOperator op, IOptimizationContext context) {
         IPartitioningProperty p = new UnorderedPartitionedProperty(new HashSet<LogicalVariable>(partitionFields),
                 domain);
         AbstractLogicalOperator op2 = (AbstractLogicalOperator) op.getInputs().get(0).getOperator();
         List<ILocalStructuralProperty> op2Locals = op2.getDeliveredPhysicalProperties().getLocalProperties();
         List<ILocalStructuralProperty> locals = new ArrayList<ILocalStructuralProperty>();
         for (ILocalStructuralProperty prop : op2Locals) {
             if (prop.getPropertyType() == PropertyType.LOCAL_ORDER_PROPERTY) {
                 locals.add(prop);
             } else {
                 break;
             }
         }
 
         this.deliveredProperties = new StructuralPropertiesVector(p, locals);
     }
 
     @Override
     public PhysicalRequirements getRequiredPropertiesForChildren(ILogicalOperator op,
             IPhysicalPropertiesVector reqdByParent) {
         List<ILocalStructuralProperty> orderProps = new LinkedList<ILocalStructuralProperty>();
         for (OrderColumn oc : orderColumns) {
             LogicalVariable var = oc.getColumn();
             switch (oc.getOrder()) {
                 case ASC: {
                     orderProps.add(new LocalOrderProperty(new OrderColumn(var, OrderKind.ASC)));
                     break;
                 }
                 case DESC: {
                     orderProps.add(new LocalOrderProperty(new OrderColumn(var, OrderKind.DESC)));
                     break;
                 }
                 default: {
                     throw new IllegalStateException();
                 }
             }
         }
         StructuralPropertiesVector[] r = new StructuralPropertiesVector[] { new StructuralPropertiesVector(null,
                 orderProps) };
         return new PhysicalRequirements(r, IPartitioningRequirementsCoordinator.NO_COORDINATION);
     }
 
     @Override
     public String toString() {
         return getOperatorTag().toString() + " MERGE:" + orderColumns + " HASH:" + partitionFields;
     }
 
     @Override
     public Pair<IConnectorDescriptor, TargetConstraint> createConnectorDescriptor(JobSpecification spec,
             IOperatorSchema opSchema, JobGenContext context) throws AlgebricksException {
        int[] keys = new int[orderColumns.size()];
         IBinaryHashFunctionFactory[] hashFunctionFactories = new IBinaryHashFunctionFactory[partitionFields.size()];
         {
             int i = 0;
             IBinaryHashFunctionFactoryProvider hashFunProvider = context.getBinaryHashFunctionFactoryProvider();
             for (LogicalVariable v : partitionFields) {
                 keys[i] = opSchema.findVariable(v);
                 hashFunctionFactories[i] = hashFunProvider.getBinaryHashFunctionFactory(context.getVarType(v));
                 ++i;
             }
         }
         ITuplePartitionComputerFactory tpcf = new FieldHashPartitionComputerFactory(keys, hashFunctionFactories);
 
         int n = orderColumns.size();
         int[] sortFields = new int[n];
         IBinaryComparatorFactory[] comparatorFactories = new IBinaryComparatorFactory[n];
         {
             int j = 0;
             for (OrderColumn oc : orderColumns) {
                 LogicalVariable var = oc.getColumn();
                 sortFields[j] = opSchema.findVariable(var);
                 Object type = context.getVarType(var);
                 IBinaryComparatorFactoryProvider bcfp = context.getBinaryComparatorFactoryProvider();
                 comparatorFactories[j] = bcfp.getBinaryComparatorFactory(type, oc.getOrder());
                 j++;
             }
         }
 
         IConnectorDescriptor conn = new MToNHashPartitioningMergingConnectorDescriptor(spec, tpcf, sortFields,
                 comparatorFactories);
         return new Pair<IConnectorDescriptor, TargetConstraint>(conn, null);
     }
 
 }
