 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen;
 
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Batch Validation</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationProviderClassName <em>Validation Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationProviderPriority <em>Validation Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMarkerNavigationProviderClassName <em>Marker Navigation Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMarkerNavigationProviderPriority <em>Marker Navigation Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isValidationEnabled <em>Validation Enabled</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMetricProviderClassName <em>Metric Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMetricProviderPriority <em>Metric Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationDecoratorProviderClassName <em>Validation Decorator Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isValidationDecorators <em>Validation Decorators</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationDecoratorProviderPriority <em>Validation Decorator Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isLiveValidationUIFeedback <em>Live Validation UI Feedback</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation()
  * @model interface="true" abstract="true"
  * @generated
  */
 public interface BatchValidation extends EObject {
 	/**
 	 * Returns the value of the '<em><b>Validation Provider Class Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Validation Provider Class Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Validation Provider Class Name</em>' attribute.
 	 * @see #setValidationProviderClassName(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationProviderClassName()
 	 * @model
 	 * @generated
 	 */
 	String getValidationProviderClassName();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationProviderClassName <em>Validation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Provider Class Name</em>' attribute.
 	 * @see #getValidationProviderClassName()
 	 * @generated
 	 */
 	void setValidationProviderClassName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Validation Provider Priority</b></em>' attribute.
 	 * The literals are from the enumeration {@link org.eclipse.gmf.codegen.gmfgen.ProviderPriority}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Validation Provider Priority</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Validation Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #setValidationProviderPriority(ProviderPriority)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationProviderPriority()
 	 * @model
 	 * @generated
 	 */
 	ProviderPriority getValidationProviderPriority();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationProviderPriority <em>Validation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #getValidationProviderPriority()
 	 * @generated
 	 */
 	void setValidationProviderPriority(ProviderPriority value);
 
 	/**
 	 * Returns the value of the '<em><b>Marker Navigation Provider Class Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Marker Navigation Provider Class Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Marker Navigation Provider Class Name</em>' attribute.
 	 * @see #setMarkerNavigationProviderClassName(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_MarkerNavigationProviderClassName()
 	 * @model
 	 * @generated
 	 */
 	String getMarkerNavigationProviderClassName();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMarkerNavigationProviderClassName <em>Marker Navigation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Marker Navigation Provider Class Name</em>' attribute.
 	 * @see #getMarkerNavigationProviderClassName()
 	 * @generated
 	 */
 	void setMarkerNavigationProviderClassName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Marker Navigation Provider Priority</b></em>' attribute.
 	 * The literals are from the enumeration {@link org.eclipse.gmf.codegen.gmfgen.ProviderPriority}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Marker Navigation Provider Priority</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Marker Navigation Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #setMarkerNavigationProviderPriority(ProviderPriority)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_MarkerNavigationProviderPriority()
 	 * @model
 	 * @generated
 	 */
 	ProviderPriority getMarkerNavigationProviderPriority();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMarkerNavigationProviderPriority <em>Marker Navigation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Marker Navigation Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #getMarkerNavigationProviderPriority()
 	 * @generated
 	 */
 	void setMarkerNavigationProviderPriority(ProviderPriority value);
 
 	/**
 	 * Returns the value of the '<em><b>Validation Enabled</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * <!-- begin-model-doc -->
 	 * If enabled, ValidationAction is contributed by the editor plugin
 	 * <!-- end-model-doc -->
 	 * @return the value of the '<em>Validation Enabled</em>' attribute.
 	 * @see #setValidationEnabled(boolean)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationEnabled()
 	 * @model
 	 * @generated
 	 */
 	boolean isValidationEnabled();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isValidationEnabled <em>Validation Enabled</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Enabled</em>' attribute.
 	 * @see #isValidationEnabled()
 	 * @generated
 	 */
 	void setValidationEnabled(boolean value);
 
 	/**
 	 * Returns the value of the '<em><b>Metric Provider Class Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Metric Provider Class Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Metric Provider Class Name</em>' attribute.
 	 * @see #setMetricProviderClassName(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_MetricProviderClassName()
 	 * @model
 	 * @generated
 	 */
 	String getMetricProviderClassName();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMetricProviderClassName <em>Metric Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Metric Provider Class Name</em>' attribute.
 	 * @see #getMetricProviderClassName()
 	 * @generated
 	 */
 	void setMetricProviderClassName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Metric Provider Priority</b></em>' attribute.
 	 * The literals are from the enumeration {@link org.eclipse.gmf.codegen.gmfgen.ProviderPriority}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Metric Provider Priority</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Metric Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #setMetricProviderPriority(ProviderPriority)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_MetricProviderPriority()
 	 * @model
 	 * @generated
 	 */
 	ProviderPriority getMetricProviderPriority();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getMetricProviderPriority <em>Metric Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Metric Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #getMetricProviderPriority()
 	 * @generated
 	 */
 	void setMetricProviderPriority(ProviderPriority value);
 
 	/**
 	 * Returns the value of the '<em><b>Validation Decorator Provider Class Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Validation Decorator Provider Class Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Validation Decorator Provider Class Name</em>' attribute.
 	 * @see #setValidationDecoratorProviderClassName(String)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationDecoratorProviderClassName()
 	 * @model
 	 * @generated
 	 */
 	String getValidationDecoratorProviderClassName();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationDecoratorProviderClassName <em>Validation Decorator Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Decorator Provider Class Name</em>' attribute.
 	 * @see #getValidationDecoratorProviderClassName()
 	 * @generated
 	 */
 	void setValidationDecoratorProviderClassName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Validation Decorators</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Validation Decorators</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Validation Decorators</em>' attribute.
 	 * @see #setValidationDecorators(boolean)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationDecorators()
 	 * @model
 	 * @generated
 	 */
 	boolean isValidationDecorators();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isValidationDecorators <em>Validation Decorators</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Decorators</em>' attribute.
 	 * @see #isValidationDecorators()
 	 * @generated
 	 */
 	void setValidationDecorators(boolean value);
 
 	/**
 	 * Returns the value of the '<em><b>Validation Decorator Provider Priority</b></em>' attribute.
 	 * The literals are from the enumeration {@link org.eclipse.gmf.codegen.gmfgen.ProviderPriority}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Validation Decorator Provider Priority</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Validation Decorator Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #setValidationDecoratorProviderPriority(ProviderPriority)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_ValidationDecoratorProviderPriority()
 	 * @model
 	 * @generated
 	 */
 	ProviderPriority getValidationDecoratorProviderPriority();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#getValidationDecoratorProviderPriority <em>Validation Decorator Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Validation Decorator Provider Priority</em>' attribute.
 	 * @see org.eclipse.gmf.codegen.gmfgen.ProviderPriority
 	 * @see #getValidationDecoratorProviderPriority()
 	 * @generated
 	 */
 	void setValidationDecoratorProviderPriority(ProviderPriority value);
 
 	/**
 	 * Returns the value of the '<em><b>Live Validation UI Feedback</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Live Validation UI Feedback</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Live Validation UI Feedback</em>' attribute.
 	 * @see #setLiveValidationUIFeedback(boolean)
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#getBatchValidation_LiveValidationUIFeedback()
 	 * @model
 	 * @generated
 	 */
 	boolean isLiveValidationUIFeedback();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.gmf.codegen.gmfgen.BatchValidation#isLiveValidationUIFeedback <em>Live Validation UI Feedback</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Live Validation UI Feedback</em>' attribute.
 	 * @see #isLiveValidationUIFeedback()
 	 * @generated
 	 */
 	void setLiveValidationUIFeedback(boolean value);
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
 	String getValidationProviderQualifiedClassName();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * <!-- begin-model-doc -->
 	 * Gets diagnostic marker type ID local to the editor plugin. The editor pluginID should be used to fully-qualify this ID.
 	 * <!-- end-model-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
 	String getValidationDiagnosticMarkerType();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
 	String getMarkerNavigationProviderQualifiedClassName();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
 	String getMetricProviderQualifiedClassName();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
 	String getMetricViewID();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @model kind="operation"
 	 * @generated
 	 */
	String getValidationDecoratorProviderQualifedClassName();
 
 } // BatchValidation
