 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.obeonetwork.dsl.dbermapping.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.obeonetwork.dsl.dbermapping.ColumnMapping;
 import org.obeonetwork.dsl.dbermapping.DBERMappingFactory;
 import org.obeonetwork.dsl.dbermapping.DBERMappingPackage;
 import org.obeonetwork.dsl.dbermapping.DatabaseMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyElementMapping;
 import org.obeonetwork.dsl.dbermapping.ForeignKeyMapping;
 import org.obeonetwork.dsl.dbermapping.PrimaryKeyMapping;
 import org.obeonetwork.dsl.dbermapping.TableMapping;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class DBERMappingFactoryImpl extends EFactoryImpl implements DBERMappingFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static DBERMappingFactory init() {
 		try {
 			DBERMappingFactory theDBERMappingFactory = (DBERMappingFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.obeonetwork.org/dsl/dbermapping/1.0"); 
 			if (theDBERMappingFactory != null) {
 				return theDBERMappingFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new DBERMappingFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DBERMappingFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case DBERMappingPackage.DATABASE_MAPPING: return createDatabaseMapping();
 			case DBERMappingPackage.TABLE_MAPPING: return createTableMapping();
 			case DBERMappingPackage.COLUMN_MAPPING: return createColumnMapping();
 			case DBERMappingPackage.PRIMARY_KEY_MAPPING: return createPrimaryKeyMapping();
 			case DBERMappingPackage.FOREIGN_KEY_MAPPING: return createForeignKeyMapping();
 			case DBERMappingPackage.FOREIGN_KEY_ELEMENT_MAPPING: return createForeignKeyElementMapping();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DatabaseMapping createDatabaseMapping() {
 		DatabaseMappingImpl databaseMapping = new DatabaseMappingImpl();
 		return databaseMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TableMapping createTableMapping() {
 		TableMappingImpl tableMapping = new TableMappingImpl();
 		return tableMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ColumnMapping createColumnMapping() {
 		ColumnMappingImpl columnMapping = new ColumnMappingImpl();
 		return columnMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PrimaryKeyMapping createPrimaryKeyMapping() {
 		PrimaryKeyMappingImpl primaryKeyMapping = new PrimaryKeyMappingImpl();
 		return primaryKeyMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ForeignKeyMapping createForeignKeyMapping() {
 		ForeignKeyMappingImpl foreignKeyMapping = new ForeignKeyMappingImpl();
 		return foreignKeyMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ForeignKeyElementMapping createForeignKeyElementMapping() {
 		ForeignKeyElementMappingImpl foreignKeyElementMapping = new ForeignKeyElementMappingImpl();
 		return foreignKeyElementMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DBERMappingPackage getDBERMappingPackage() {
 		return (DBERMappingPackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static DBERMappingPackage getPackage() {
 		return DBERMappingPackage.eINSTANCE;
 	}
 
 } //DBERMappingFactoryImpl
