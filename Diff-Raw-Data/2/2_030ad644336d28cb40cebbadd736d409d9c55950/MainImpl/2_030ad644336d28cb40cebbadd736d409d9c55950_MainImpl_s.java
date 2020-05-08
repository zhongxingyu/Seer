 /**
  */
 package lightast.impl;
 
 import java.util.Collection;
 
 import lightast.BIMServerCredentials;
 import lightast.LightFixture;
import lightast.Lamp;
 import lightast.LightastPackage;
 import lightast.Main;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Main</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link lightast.impl.MainImpl#getBIMServer <em>BIM Server</em>}</li>
  *   <li>{@link lightast.impl.MainImpl#getFixtures <em>Fixtures</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class MainImpl extends EObjectImpl implements Main {
 	/**
 	 * The cached value of the '{@link #getBIMServer() <em>BIM Server</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBIMServer()
 	 * @generated
 	 * @ordered
 	 */
 	protected BIMServerCredentials bimServer;
 
 	/**
 	 * The cached value of the '{@link #getFixtures() <em>Fixtures</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFixtures()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<LightFixture> fixtures;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected MainImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return LightastPackage.Literals.MAIN;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BIMServerCredentials getBIMServer() {
 		return bimServer;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetBIMServer(BIMServerCredentials newBIMServer, NotificationChain msgs) {
 		BIMServerCredentials oldBIMServer = bimServer;
 		bimServer = newBIMServer;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, LightastPackage.MAIN__BIM_SERVER, oldBIMServer, newBIMServer);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBIMServer(BIMServerCredentials newBIMServer) {
 		if (newBIMServer != bimServer) {
 			NotificationChain msgs = null;
 			if (bimServer != null)
 				msgs = ((InternalEObject)bimServer).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - LightastPackage.MAIN__BIM_SERVER, null, msgs);
 			if (newBIMServer != null)
 				msgs = ((InternalEObject)newBIMServer).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - LightastPackage.MAIN__BIM_SERVER, null, msgs);
 			msgs = basicSetBIMServer(newBIMServer, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, LightastPackage.MAIN__BIM_SERVER, newBIMServer, newBIMServer));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<LightFixture> getFixtures() {
 		if (fixtures == null) {
 			fixtures = new EObjectContainmentEList<LightFixture>(LightFixture.class, this, LightastPackage.MAIN__FIXTURES);
 		}
 		return fixtures;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case LightastPackage.MAIN__BIM_SERVER:
 				return basicSetBIMServer(null, msgs);
 			case LightastPackage.MAIN__FIXTURES:
 				return ((InternalEList<?>)getFixtures()).basicRemove(otherEnd, msgs);
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
 			case LightastPackage.MAIN__BIM_SERVER:
 				return getBIMServer();
 			case LightastPackage.MAIN__FIXTURES:
 				return getFixtures();
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
 			case LightastPackage.MAIN__BIM_SERVER:
 				setBIMServer((BIMServerCredentials)newValue);
 				return;
 			case LightastPackage.MAIN__FIXTURES:
 				getFixtures().clear();
 				getFixtures().addAll((Collection<? extends LightFixture>)newValue);
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
 			case LightastPackage.MAIN__BIM_SERVER:
 				setBIMServer((BIMServerCredentials)null);
 				return;
 			case LightastPackage.MAIN__FIXTURES:
 				getFixtures().clear();
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
 			case LightastPackage.MAIN__BIM_SERVER:
 				return bimServer != null;
 			case LightastPackage.MAIN__FIXTURES:
 				return fixtures != null && !fixtures.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 } //MainImpl
