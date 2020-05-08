 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.obeonetwork.dsl.dbermapping.util;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
 import org.eclipse.emf.ecore.EObject;
import org.obeonetwork.dsl.dbermapping.*;
 import org.obeonetwork.dsl.dbermapping.ColumnMapping;
 import org.obeonetwork.dsl.dbermapping.DBERMappingPackage;
 import org.obeonetwork.dsl.dbermapping.DatabaseMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyElementMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyMapping;
 import org.obeonetwork.dsl.dbermapping.PrimaryKeyMapping;
 import org.obeonetwork.dsl.dbermapping.TableMapping;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Adapter Factory</b> for the model.
  * It provides an adapter <code>createXXX</code> method for each class of the model.
  * <!-- end-user-doc -->
  * @see org.obeonetwork.dsl.dbermapping.DBERMappingPackage
  * @generated
  */
 public class DBERMappingAdapterFactory extends AdapterFactoryImpl {
 	/**
 	 * The cached model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static DBERMappingPackage modelPackage;
 
 	/**
 	 * Creates an instance of the adapter factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DBERMappingAdapterFactory() {
 		if (modelPackage == null) {
 			modelPackage = DBERMappingPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Returns whether this factory is applicable for the type of the object.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
 	 * <!-- end-user-doc -->
 	 * @return whether this factory is applicable for the type of the object.
 	 * @generated
 	 */
 	@Override
 	public boolean isFactoryForType(Object object) {
 		if (object == modelPackage) {
 			return true;
 		}
 		if (object instanceof EObject) {
 			return ((EObject)object).eClass().getEPackage() == modelPackage;
 		}
 		return false;
 	}
 
 	/**
 	 * The switch that delegates to the <code>createXXX</code> methods.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DBERMappingSwitch<Adapter> modelSwitch =
 		new DBERMappingSwitch<Adapter>() {
 			@Override
 			public Adapter caseDatabaseMapping(DatabaseMapping object) {
 				return createDatabaseMappingAdapter();
 			}
 			@Override
 			public Adapter caseTableMapping(TableMapping object) {
 				return createTableMappingAdapter();
 			}
 			@Override
 			public Adapter caseColumnMapping(ColumnMapping object) {
 				return createColumnMappingAdapter();
 			}
 			@Override
 			public Adapter casePrimaryKeyMapping(PrimaryKeyMapping object) {
 				return createPrimaryKeyMappingAdapter();
 			}
 			@Override
 			public Adapter caseForeignKeyMapping(ForeignKeyMapping object) {
 				return createForeignKeyMappingAdapter();
 			}
 			@Override
 			public Adapter caseForeignKeyElementMapping(ForeignKeyElementMapping object) {
 				return createForeignKeyElementMappingAdapter();
 			}
 			@Override
 			public Adapter defaultCase(EObject object) {
 				return createEObjectAdapter();
 			}
 		};
 
 	/**
 	 * Creates an adapter for the <code>target</code>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param target the object to adapt.
 	 * @return the adapter for the <code>target</code>.
 	 * @generated
 	 */
 	@Override
 	public Adapter createAdapter(Notifier target) {
 		return modelSwitch.doSwitch((EObject)target);
 	}
 
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.DatabaseMapping <em>Database Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.DatabaseMapping
 	 * @generated
 	 */
 	public Adapter createDatabaseMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.TableMapping <em>Table Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.TableMapping
 	 * @generated
 	 */
 	public Adapter createTableMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.ColumnMapping <em>Column Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.ColumnMapping
 	 * @generated
 	 */
 	public Adapter createColumnMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.PrimaryKeyMapping <em>Primary Key Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.PrimaryKeyMapping
 	 * @generated
 	 */
 	public Adapter createPrimaryKeyMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.ForeignKeyMapping <em>Foreign Key Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.ForeignKeyMapping
 	 * @generated
 	 */
 	public Adapter createForeignKeyMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.obeonetwork.dsl.dbermapping.ForeignKeyElementMapping <em>Foreign Key Element Mapping</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.obeonetwork.dsl.dbermapping.ForeignKeyElementMapping
 	 * @generated
 	 */
 	public Adapter createForeignKeyElementMappingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for the default case.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @generated
 	 */
 	public Adapter createEObjectAdapter() {
 		return null;
 	}
 
 } //DBERMappingAdapterFactory
