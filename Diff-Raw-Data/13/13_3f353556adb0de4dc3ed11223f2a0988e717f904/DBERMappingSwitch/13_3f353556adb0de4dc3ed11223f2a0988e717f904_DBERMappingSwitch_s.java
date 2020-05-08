 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.obeonetwork.dsl.dbermapping.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.obeonetwork.dsl.dbermapping.ColumnMapping;
 import org.obeonetwork.dsl.dbermapping.DBERMappingPackage;
 import org.obeonetwork.dsl.dbermapping.DatabaseMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyElementMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyMapping;
 import org.obeonetwork.dsl.dbermapping.PrimaryKeyMapping;
 import org.obeonetwork.dsl.dbermapping.TableMapping;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Switch</b> for the model's inheritance hierarchy.
  * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
  * to invoke the <code>caseXXX</code> method for each class of the model,
  * starting with the actual class of the object
  * and proceeding up the inheritance hierarchy
  * until a non-null result is returned,
  * which is the result of the switch.
  * <!-- end-user-doc -->
  * @see org.obeonetwork.dsl.dbermapping.DBERMappingPackage
  * @generated
  */
 public class DBERMappingSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static DBERMappingPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DBERMappingSwitch() {
 		if (modelPackage == null) {
 			modelPackage = DBERMappingPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	public T doSwitch(EObject theEObject) {
 		return doSwitch(theEObject.eClass(), theEObject);
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(EClass theEClass, EObject theEObject) {
 		if (theEClass.eContainer() == modelPackage) {
 			return doSwitch(theEClass.getClassifierID(), theEObject);
 		}
 		else {
 			List<EClass> eSuperTypes = theEClass.getESuperTypes();
 			return
 				eSuperTypes.isEmpty() ?
 					defaultCase(theEObject) :
 					doSwitch(eSuperTypes.get(0), theEObject);
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(int classifierID, EObject theEObject) {
 		switch (classifierID) {
 			case DBERMappingPackage.DATABASE_MAPPING: {
 				DatabaseMapping databaseMapping = (DatabaseMapping)theEObject;
 				T result = caseDatabaseMapping(databaseMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case DBERMappingPackage.TABLE_MAPPING: {
 				TableMapping tableMapping = (TableMapping)theEObject;
 				T result = caseTableMapping(tableMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case DBERMappingPackage.COLUMN_MAPPING: {
 				ColumnMapping columnMapping = (ColumnMapping)theEObject;
 				T result = caseColumnMapping(columnMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case DBERMappingPackage.PRIMARY_KEY_MAPPING: {
 				PrimaryKeyMapping primaryKeyMapping = (PrimaryKeyMapping)theEObject;
 				T result = casePrimaryKeyMapping(primaryKeyMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case DBERMappingPackage.FOREIGN_KEY_MAPPING: {
 				ForeignKeyMapping foreignKeyMapping = (ForeignKeyMapping)theEObject;
 				T result = caseForeignKeyMapping(foreignKeyMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case DBERMappingPackage.FOREIGN_KEY_ELEMENT_MAPPING: {
 				ForeignKeyElementMapping foreignKeyElementMapping = (ForeignKeyElementMapping)theEObject;
 				T result = caseForeignKeyElementMapping(foreignKeyElementMapping);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			default: return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Database Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Database Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDatabaseMapping(DatabaseMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Table Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Table Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseTableMapping(TableMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Column Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Column Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseColumnMapping(ColumnMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Primary Key Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Primary Key Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T casePrimaryKeyMapping(PrimaryKeyMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Foreign Key Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Foreign Key Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseForeignKeyMapping(ForeignKeyMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Foreign Key Element Mapping</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Foreign Key Element Mapping</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseForeignKeyElementMapping(ForeignKeyElementMapping object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch, but this is the last case anyway.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
 	 * @generated
 	 */
 	public T defaultCase(EObject object) {
 		return null;
 	}
 
 } //DBERMappingSwitch
