 package edu.uci.ics.algebricks.compiler.algebra.operators.logical;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.uci.ics.algebricks.api.exceptions.AlgebricksException;
 import edu.uci.ics.algebricks.api.expr.IVariableTypeEnvironment;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalExpressionReference;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalOperatorTag;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalVariable;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.InsertDeleteOperator.Kind;
 import edu.uci.ics.algebricks.compiler.algebra.properties.VariablePropagationPolicy;
 import edu.uci.ics.algebricks.compiler.algebra.typing.ITypingContext;
 import edu.uci.ics.algebricks.compiler.algebra.visitors.ILogicalExpressionReferenceTransform;
 import edu.uci.ics.algebricks.compiler.algebra.visitors.ILogicalOperatorVisitor;
 
 public class IndexInsertDeleteOperator extends AbstractLogicalOperator {
 
     private final String datasetName;
     private final String indexName;
     private final List<LogicalExpressionReference> primaryKeyExprs;
     private final List<LogicalExpressionReference> secondaryKeyExprs;
     private final Kind operation;
 
     public IndexInsertDeleteOperator(String datasetName, String indexName,
             List<LogicalExpressionReference> primaryKeyExprs, List<LogicalExpressionReference> secondaryKeyExprs,
             Kind operation) {
         this.datasetName = datasetName;
         this.indexName = indexName;
         this.primaryKeyExprs = primaryKeyExprs;
         this.secondaryKeyExprs = secondaryKeyExprs;
         this.operation = operation;
     }
 
     @Override
     public void recomputeSchema() throws AlgebricksException {
         schema = new ArrayList<LogicalVariable>();
         schema.addAll(inputs.get(0).getOperator().getSchema());
     }
 
     @Override
     public boolean acceptExpressionTransform(ILogicalExpressionReferenceTransform visitor) throws AlgebricksException {
         boolean b = false;
         for (int i = 0; i < primaryKeyExprs.size(); i++) {
             if (visitor.transform(primaryKeyExprs.get(i))) {
                 b = true;
             }
         }
         for (int i = 0; i < secondaryKeyExprs.size(); i++) {
            if (visitor.transform(primaryKeyExprs.get(i))) {
                 b = true;
             }
         }
         return b;
     }
 
     @Override
     public <R, T> R accept(ILogicalOperatorVisitor<R, T> visitor, T arg) throws AlgebricksException {
         return visitor.visitIndexInsertDeleteOperator(this, arg);
     }
 
     @Override
     public boolean isMap() {
         return false;
     }
 
     @Override
     public VariablePropagationPolicy getVariablePropagationPolicy() {
         return VariablePropagationPolicy.ALL;
     }
 
     @Override
     public LogicalOperatorTag getOperatorTag() {
         return LogicalOperatorTag.INDEX_INSERT_DELETE;
     }
 
     @Override
     public IVariableTypeEnvironment computeOutputTypeEnvironment(ITypingContext ctx) throws AlgebricksException {
         return createPropagatingAllInputsTypeEnvironment(ctx);
     }
 
     public List<LogicalExpressionReference> getPrimaryKeyExpressions() {
         return primaryKeyExprs;
     }
 
     public String getDatasetName() {
         return datasetName;
     }
 
     public String getIndexName() {
         return indexName;
     }
 
     public List<LogicalExpressionReference> getSecondaryKeyExpressions() {
         return secondaryKeyExprs;
     }
 
     public Kind getOperation() {
         return operation;
     }
 
 }
