 package org.eclipse.uml2.diagram.clazz.edit.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
 import org.eclipse.uml2.diagram.clazz.edit.policies.UMLBaseItemSemanticEditPolicy;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.GeneralizationSet;
 
 /**
  * @generated
  */
 public class GeneralizationGeneralReorientCommand extends EditElementCommand {
 
 	/**
 	 * @generated
 	 */
 	private final int reorientDirection;
 
 	/**
 	 * @generated
 	 */
 	private final EObject referenceOwner;
 
 	/**
 	 * @generated
 	 */
 	private final EObject oldEnd;
 
 	/**
 	 * @generated
 	 */
 	private final EObject newEnd;
 
 	/**
 	 * @generated
 	 */
 	public GeneralizationGeneralReorientCommand(ReorientReferenceRelationshipRequest request) {
 		super(request.getLabel(), null, request);
 		reorientDirection = request.getDirection();
 		referenceOwner = request.getReferenceOwner();
 		oldEnd = request.getOldRelationshipEnd();
 		newEnd = request.getNewRelationshipEnd();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public boolean canExecute() {
		if (false == referenceOwner instanceof GeneralizationSet && ((GeneralizationSet)referenceOwner).getGeneralization(getOldTarget()) != null) {
 			return false;
 		}
 		if (reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
 			return canReorientSource();
 		}
 		if (reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
 			return canReorientTarget();
 		}
 		return false;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected boolean canReorientSource() {
 		return false;
//		if (!(oldEnd instanceof Classifier && newEnd instanceof Generalization)) {
//			return false;
//		}
//		return UMLBaseItemSemanticEditPolicy.LinkConstraints.canExistGeneralizationGeneral_4012(getNewSource(), getOldTarget());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean canReorientTarget() {
 		if (!(oldEnd instanceof Classifier && newEnd instanceof Classifier)) {
 			return false;
 		}
 		return UMLBaseItemSemanticEditPolicy.LinkConstraints.canExistGeneralizationGeneral_4012(getOldSource(), getNewTarget());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		if (!canExecute()) {
 			throw new ExecutionException("Invalid arguments in reorient link command"); //$NON-NLS-1$
 		}
 		if (reorientDirection == ReorientRelationshipRequest.REORIENT_SOURCE) {
 			return reorientSource();
 		}
 		if (reorientDirection == ReorientRelationshipRequest.REORIENT_TARGET) {
 			return reorientTarget();
 		}
 		throw new IllegalStateException();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected CommandResult reorientSource() throws ExecutionException {
 		getOldSource().setGeneral(null);
 		getNewSource().setGeneral(getOldTarget());
 		return CommandResult.newOKCommandResult(referenceOwner);
 	}
 
 	/**
	 * @generated
 	 */
 	protected CommandResult reorientTarget() throws ExecutionException {
 		for (Generalization g: getAllGeneralizations()) {
 			g.setGeneral(getNewTarget());
 		}
 		return CommandResult.newOKCommandResult(referenceOwner);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected Generalization getOldSource() {
 		return ((GeneralizationSet) referenceOwner).getGeneralization(getOldTarget());
 	}
 
 	/**
 	 * @NOT generated
 	 * Returns list of Generalizations of GeneralizationSet, that had been connected to classifier.  
 	 * General property of all of them will be changed after reroute 
 	 */
 	protected List<Generalization> getAllGeneralizations() {
 		List<Generalization> result = new ArrayList<Generalization>();
		for (Generalization g: ((GeneralizationSet) referenceOwner).getGeneralizations()) {
 			if (getOldTarget().equals(g.getGeneral())) {
 				result.add(g);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Generalization getNewSource() {
 		return (Generalization) newEnd;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Classifier getOldTarget() {
 		return (Classifier) oldEnd;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Classifier getNewTarget() {
 		return (Classifier) newEnd;
 	}
 }
