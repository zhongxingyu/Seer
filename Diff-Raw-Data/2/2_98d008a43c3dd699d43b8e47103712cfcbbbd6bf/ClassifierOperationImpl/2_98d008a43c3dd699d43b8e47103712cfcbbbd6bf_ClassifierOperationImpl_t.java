 /**
  */
 package com.github.kanafghan.welipse.webdsl.impl;
 
 import com.github.kanafghan.welipse.webdsl.ClassifierOperation;
 import com.github.kanafghan.welipse.webdsl.Expression;
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.VariableDeclaration;
 import com.github.kanafghan.welipse.webdsl.VariableExp;
 import com.github.kanafghan.welipse.webdsl.WebDSLPackage;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Classifier Operation</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link com.github.kanafghan.welipse.webdsl.impl.ClassifierOperationImpl#getOperation <em>Operation</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.webdsl.impl.ClassifierOperationImpl#getArguments <em>Arguments</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ClassifierOperationImpl extends PropertyOperationImpl implements ClassifierOperation {
 	/**
 	 * The cached value of the '{@link #getOperation() <em>Operation</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOperation()
 	 * @generated
 	 * @ordered
 	 */
 	protected EOperation operation;
 
 	/**
 	 * The cached value of the '{@link #getArguments() <em>Arguments</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getArguments()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Expression> arguments;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ClassifierOperationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return WebDSLPackage.Literals.CLASSIFIER_OPERATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EOperation getOperation() {
 		if (operation != null && operation.eIsProxy()) {
 			InternalEObject oldOperation = (InternalEObject)operation;
 			operation = (EOperation)eResolveProxy(oldOperation);
 			if (operation != oldOperation) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, WebDSLPackage.CLASSIFIER_OPERATION__OPERATION, oldOperation, operation));
 			}
 		}
 		return operation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EOperation basicGetOperation() {
 		return operation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOperation(EOperation newOperation) {
 		EOperation oldOperation = operation;
 		operation = newOperation;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, WebDSLPackage.CLASSIFIER_OPERATION__OPERATION, oldOperation, operation));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Expression> getArguments() {
 		if (arguments == null) {
 			arguments = new EObjectContainmentEList<Expression>(Expression.class, this, WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS);
 		}
 		return arguments;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS:
 				return ((InternalEList<?>)getArguments()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case WebDSLPackage.CLASSIFIER_OPERATION__OPERATION:
 				if (resolve) return getOperation();
 				return basicGetOperation();
 			case WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS:
 				return getArguments();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case WebDSLPackage.CLASSIFIER_OPERATION__OPERATION:
 				setOperation((EOperation)newValue);
 				return;
 			case WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS:
 				getArguments().clear();
 				getArguments().addAll((Collection<? extends Expression>)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case WebDSLPackage.CLASSIFIER_OPERATION__OPERATION:
 				setOperation((EOperation)null);
 				return;
 			case WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS:
 				getArguments().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case WebDSLPackage.CLASSIFIER_OPERATION__OPERATION:
 				return operation != null;
 			case WebDSLPackage.CLASSIFIER_OPERATION__ARGUMENTS:
 				return arguments != null && !arguments.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public EClassifier type() {
 		if (getOperation() != null) {			
 			return getOperation().getEType();
 		} else {
 			throw new Error("In classifier operation '"+ getIdentifier() +"()' the Operation is not set. "
 					+ "(You must call the initialize() method of the expression first.)");
 		}
 	}
 
 	public static final String[] UTILITY_OPERATIONS = {"getAll"};
 	
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public void initialize(Page page) {
 		// let the source be initialized first
 		getSource().initialize(page);
 		
 		// initialize the arguments
 		for (Expression argument : getArguments()) {
 			argument.initialize(page);
 		}
 		
 		if (getSource() instanceof VariableExp) {
 			VariableExp var = (VariableExp) getSource();
 			VariableDeclaration declaration = var.getDeclaration();
 			if (declaration != null) {				
 				EClassifier type = declaration.getType();
 				if (type != null) {
 					if (type instanceof EClass) {
 						EClass cls = (EClass) type;
 						for (EOperation operation : cls.getEOperations()) {
 							if (operation.getName().equals(getIdentifier())) {
 								EList<EParameter> eParameters = operation.getEParameters();
 								if (eParameters.size() == getArguments().size()) {
 									boolean isMatch = true;
 									for (int i=0; i<eParameters.size(); i++) {
 										EClassifier type1 = eParameters.get(i).getEType();
 										EClassifier type2 = getArguments().get(i).type();
 										if (!type1.getName().equals(type2.getName())) {
 											isMatch = false;
 											break;
 										}
 									}
 									if (isMatch) {										
 										setOperation(operation);
 										break;
 									}
 								}
 							}
 						}
 						
						if (getOperation() == null) {
 							throw new Error("The type '"+ cls.getName() +"' has no operation '"
 									+ getIdentifier() +"()' with "+ getArguments().size() +" arguments.");
 						}
 					} else {
 						throw new Error("The type of variable '"+ var.getVar() +"' is not EClass. "
 								+ "It was expected to be EClass in order to find operation '"
 								+ getIdentifier() +"()'");
 					}
 				} else {
 					throw new Error("The type of variable '"+ var.getVar() +"' is not set.");
 				}
 			} else {
 				throw new Error("The declaration of variable '"+ var.getVar() 
 						+"' is not set. It was not possible to determine its type.");
 			}
 		} else {
 			throw new Error("The Source expression of classifier operation must be variable expression, "
 					+ "but got "+ getSource());
 		}
 	}
 	
 
 } //ClassifierOperationImpl
