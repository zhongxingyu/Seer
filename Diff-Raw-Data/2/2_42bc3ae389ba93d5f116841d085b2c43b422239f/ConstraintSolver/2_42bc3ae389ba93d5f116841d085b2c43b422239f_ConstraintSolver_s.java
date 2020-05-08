 package cd.semantic.ti.constraintSolving;
 
 import java.util.Set;
 
 import cd.semantic.ti.constraintSolving.constraints.LowerConstBoundConstraint;
 import cd.semantic.ti.constraintSolving.constraints.TypeConstraint;
 import cd.semantic.ti.constraintSolving.constraints.TypeConstraintVisitor;
 import cd.semantic.ti.constraintSolving.constraints.UpperConstBoundConstraint;
 import cd.semantic.ti.constraintSolving.constraints.VariableInequalityConstraint;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class ConstraintSolver {
 	private final ConstraintSystem constraintSystem;
 	private boolean hasSolution;
 
 	public ConstraintSolver(ConstraintSystem constraints) {
 		this.constraintSystem = checkNotNull(constraints);
 	}
 
 	public boolean hasSolution() {
 		return hasSolution;
 	}
 
 	public void solve() {
 		hasSolution = true;
 		boolean changed;
 		UnsatisfiedConstraintUpdater variableUpdater = new UnsatisfiedConstraintUpdater();
 		Set<TypeConstraint> constraints = constraintSystem.getTypeConstraints();
 		do {
 			changed = false;
 			for (TypeConstraint constraint : constraints) {
 				if (!constraint.isSatisfied()) {
 					constraint.accept(variableUpdater, null);
 					changed = true;
 				}
 			}
 		} while (changed && hasSolution);
 	}
 
 	private class UnsatisfiedConstraintUpdater implements
 			TypeConstraintVisitor<Void, Void> {
 
 		@Override
 		public Void visitLowerConstBoundConstraint(
 				LowerConstBoundConstraint constraint, Void arg) {
 			constraint.getTypeVariable().extend(constraint.getLowerBound());
 			return null;
 		}
 
 		@Override
 		public Void visitUpperConstBoundConstraint(
 				UpperConstBoundConstraint constraint, Void arg) {
 			hasSolution = false;
 			return null;
 		}
 
 		@Override
 		public Void visitVariableInequalityConstraint(
 				VariableInequalityConstraint constraint, Void arg) {
			constraint.getLeft().extend(constraint.getRight());
 			return null;
 		}
 	}
 
 }
