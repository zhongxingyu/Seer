 package org.eclipse.uml2.diagram.clazz.edit.commands;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
 import org.eclipse.uml2.diagram.clazz.edit.helpers.DependencyEditHelper;
 import org.eclipse.uml2.diagram.clazz.edit.policies.UMLBaseItemSemanticEditPolicy;
 import org.eclipse.uml2.uml.Dependency;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.UMLFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 public class DependencyLinkCreateCommand extends CreateElementCommand {
 
 	/**
 	 * @generated
 	 */
 	private final EObject source;
 
 	/**
 	 * @generated
 	 */
 	private final EObject target;
 
 	/**
 	 * @generated
 	 */
 	private Package container;
 
 	/**
 	 * @generated
 	 */
 	public DependencyLinkCreateCommand(CreateRelationshipRequest request, EObject source, EObject target) {
 		super(request);
 		this.source = source;
 		this.target = target;
 		if (request.getContainmentFeature() == null) {
 			setContainmentFeature(UMLPackage.eINSTANCE.getPackage_PackagedElement());
 		}
 
 		// Find container element for the new link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null; element = element.eContainer()) {
 			if (element instanceof Package) {
 				container = (Package) element;
 				super.setElementToEdit(container);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	public boolean canExecute() {
 		if (source == null && target == null) {
 			return false;
 		}
 		if (source != null && !(source instanceof NamedElement)) {
 			return false;
 		}
 		if (target != null && !(target instanceof NamedElement)) {
 			return false;
 		}
 		if (getSource() == null) {
 			return true; // link creation is in progress; source is not defined yet
 		}
 		// target may be null here but it's possible to check constraint
 		if (getContainer() == null) {
 			return false;
 		}
 		return UMLBaseItemSemanticEditPolicy.LinkConstraints.canCreateDependency_4002(getContainer(), getSource(), getTarget());
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EObject doDefaultElementCreationGen() {
 		// org.eclipse.uml2.uml.Dependency newElement = (org.eclipse.uml2.uml.Dependency) super.doDefaultElementCreation();
 		Dependency newElement = UMLFactory.eINSTANCE.createDependency();
 		getContainer().getPackagedElements().add(newElement);
 		newElement.getClients().add(getSource());
 		newElement.getSuppliers().add(getTarget());
 		return newElement;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected EObject doDefaultElementCreation() {
 		Dependency newElement;
 		EClass eClass = (EClass) getCreateRequest().getParameter(DependencyEditHelper.PARAMETER_DEPENDENCY_TYPE);
 		if (eClass == null) {
			newElement = (Dependency) doDefaultElementCreation();
 		} else {
 			EReference containment = getContainmentFeature();
 			EObject element = getElementToEdit();
 			if (containment == null || element == null) {
 				return null;
 			}
 			newElement = (Dependency) EMFCoreUtil.create(element, containment, eClass);
 			if (newElement != null) {
 				newElement.getClients().add(getSource());
 				newElement.getSuppliers().add(getTarget());
 			}
 		}
 		return newElement;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EClass getEClassToEdit() {
 		return UMLPackage.eINSTANCE.getPackage();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		if (!canExecute()) {
 			throw new ExecutionException("Invalid arguments in create link command"); //$NON-NLS-1$
 		}
 		return super.doExecuteWithResult(monitor, info);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected ConfigureRequest createConfigureRequest() {
 		ConfigureRequest request = super.createConfigureRequest();
 		request.setParameter(CreateRelationshipRequest.SOURCE, getSource());
 		request.setParameter(CreateRelationshipRequest.TARGET, getTarget());
 		return request;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void setElementToEdit(EObject element) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NamedElement getSource() {
 		return (NamedElement) source;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected NamedElement getTarget() {
 		return (NamedElement) target;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Package getContainer() {
 		return container;
 	}
 }
