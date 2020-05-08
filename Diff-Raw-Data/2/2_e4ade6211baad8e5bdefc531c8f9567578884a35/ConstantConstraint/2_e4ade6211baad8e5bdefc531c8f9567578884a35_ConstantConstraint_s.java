 package cd.semantic.ti.constraintSolving.constraints;
 
 import cd.semantic.ti.constraintSolving.ConstantTypeSet;
 
 import com.google.common.collect.ImmutableList;
 
 public class ConstantConstraint extends TypeConstraint {
 
 	private final boolean isSatisfiedIfActive;
 
 	public ConstantConstraint(ConstantTypeSet subTypeSet,
 			ConstantTypeSet superTypeSet,
 			ImmutableList<ConstraintCondition> conditions) {
 		super(conditions);
 		this.isSatisfiedIfActive = subTypeSet.isSubsetOf(superTypeSet);
 	}
 
 	@Override
 	public boolean isSatisfied() {
		return isActive() || isSatisfiedIfActive;
 	}
 
 	@Override
 	public <R, A> R accept(TypeConstraintVisitor<R, A> visitor, A arg) {
 		return visitor.visit(this, arg);
 	}
 	
 	@Override
 	public String toString () {
 		return buildString(Boolean.toString(isSatisfiedIfActive));
 	}
 
 }
