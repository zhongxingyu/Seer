 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.p2.impl;
 
 import java.util.Collection;
 import org.eclipse.b3.aggregator.p2.InstallableUnitFragment;
 import org.eclipse.b3.aggregator.p2.P2Package;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Installable Unit Fragment</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * </p>
  * 
  * @generated
  */
 public class InstallableUnitFragmentImpl extends InstallableUnitImpl implements InstallableUnitFragment {
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected InstallableUnitFragmentImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
	 * @generated
 	 */
 	public Collection<IRequirement> getHost() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isFragment() {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return P2Package.Literals.INSTALLABLE_UNIT_FRAGMENT;
 	}
 
 } // InstallableUnitFragmentImpl
