 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package diva.impl;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import diva.Configuration;
 import diva.Context;
 import diva.DivaFactory;
 import diva.DivaPackage;
 import diva.Priority;
 import diva.PriorityRule;
 import diva.Property;
 import diva.PropertyPriority;
 import diva.Rule;
 import diva.SimulationModel;
 import diva.VariableValue;
 import diva.VariantExpression;
 import diva.Verdict;
 import diva.visitors.Visitor;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Context</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link diva.impl.ContextImpl#getVariable <em>Variable</em>}</li>
  *   <li>{@link diva.impl.ContextImpl#getConfiguration <em>Configuration</em>}</li>
  *   <li>{@link diva.impl.ContextImpl#getOracle <em>Oracle</em>}</li>
  *   <li>{@link diva.impl.ContextImpl#getVerdict <em>Verdict</em>}</li>
  *   <li>{@link diva.impl.ContextImpl#getPriority <em>Priority</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ContextImpl extends NamedElementImpl implements Context {
 	/**
 	 * The cached value of the '{@link #getVariable() <em>Variable</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVariable()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<VariableValue> variable;
 
 	/**
 	 * The cached value of the '{@link #getConfiguration() <em>Configuration</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getConfiguration()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Configuration> configuration;
 
 	/**
 	 * The cached value of the '{@link #getOracle() <em>Oracle</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOracle()
 	 * @generated
 	 * @ordered
 	 */
 	protected VariantExpression oracle;
 
 	/**
 	 * The default value of the '{@link #getVerdict() <em>Verdict</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVerdict()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Verdict VERDICT_EDEFAULT = Verdict.NONE;
 
 	/**
 	 * The cached value of the '{@link #getVerdict() <em>Verdict</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVerdict()
 	 * @generated
 	 * @ordered
 	 */
 	protected Verdict verdict = VERDICT_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getPriority() <em>Priority</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Priority> priority;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ContextImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DivaPackage.Literals.CONTEXT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<VariableValue> getVariable() {
 		if (variable == null) {
 			variable = new EObjectContainmentEList<VariableValue>(VariableValue.class, this, DivaPackage.CONTEXT__VARIABLE);
 		}
 		return variable;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Configuration> getConfiguration() {
 		if (configuration == null) {
 			configuration = new EObjectContainmentEList<Configuration>(Configuration.class, this, DivaPackage.CONTEXT__CONFIGURATION);
 		}
 		return configuration;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public VariantExpression getOracle() {
 		return oracle;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOracle(VariantExpression newOracle, NotificationChain msgs) {
 		VariantExpression oldOracle = oracle;
 		oracle = newOracle;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DivaPackage.CONTEXT__ORACLE, oldOracle, newOracle);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOracle(VariantExpression newOracle) {
 		if (newOracle != oracle) {
 			NotificationChain msgs = null;
 			if (oracle != null)
 				msgs = ((InternalEObject)oracle).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DivaPackage.CONTEXT__ORACLE, null, msgs);
 			if (newOracle != null)
 				msgs = ((InternalEObject)newOracle).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DivaPackage.CONTEXT__ORACLE, null, msgs);
 			msgs = basicSetOracle(newOracle, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DivaPackage.CONTEXT__ORACLE, newOracle, newOracle));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Verdict getVerdict() {
 		return verdict;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVerdict(Verdict newVerdict) {
 		Verdict oldVerdict = verdict;
 		verdict = newVerdict == null ? VERDICT_EDEFAULT : newVerdict;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DivaPackage.CONTEXT__VERDICT, oldVerdict, verdict));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Priority> getPriority() {
 		if (priority == null) {
 			priority = new EObjectContainmentEList<Priority>(Priority.class, this, DivaPackage.CONTEXT__PRIORITY);
 		}
 		return priority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public <C, R> R accept(Visitor<C, R> visitor, C context) {
 		return visitor.visitContext(this, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case DivaPackage.CONTEXT__VARIABLE:
 				return ((InternalEList<?>)getVariable()).basicRemove(otherEnd, msgs);
 			case DivaPackage.CONTEXT__CONFIGURATION:
 				return ((InternalEList<?>)getConfiguration()).basicRemove(otherEnd, msgs);
 			case DivaPackage.CONTEXT__ORACLE:
 				return basicSetOracle(null, msgs);
 			case DivaPackage.CONTEXT__PRIORITY:
 				return ((InternalEList<?>)getPriority()).basicRemove(otherEnd, msgs);
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
 			case DivaPackage.CONTEXT__VARIABLE:
 				return getVariable();
 			case DivaPackage.CONTEXT__CONFIGURATION:
 				return getConfiguration();
 			case DivaPackage.CONTEXT__ORACLE:
 				return getOracle();
 			case DivaPackage.CONTEXT__VERDICT:
 				return getVerdict();
 			case DivaPackage.CONTEXT__PRIORITY:
 				return getPriority();
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
 			case DivaPackage.CONTEXT__VARIABLE:
 				getVariable().clear();
 				getVariable().addAll((Collection<? extends VariableValue>)newValue);
 				return;
 			case DivaPackage.CONTEXT__CONFIGURATION:
 				getConfiguration().clear();
 				getConfiguration().addAll((Collection<? extends Configuration>)newValue);
 				return;
 			case DivaPackage.CONTEXT__ORACLE:
 				setOracle((VariantExpression)newValue);
 				return;
 			case DivaPackage.CONTEXT__VERDICT:
 				setVerdict((Verdict)newValue);
 				return;
 			case DivaPackage.CONTEXT__PRIORITY:
 				getPriority().clear();
 				getPriority().addAll((Collection<? extends Priority>)newValue);
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
 			case DivaPackage.CONTEXT__VARIABLE:
 				getVariable().clear();
 				return;
 			case DivaPackage.CONTEXT__CONFIGURATION:
 				getConfiguration().clear();
 				return;
 			case DivaPackage.CONTEXT__ORACLE:
 				setOracle((VariantExpression)null);
 				return;
 			case DivaPackage.CONTEXT__VERDICT:
 				setVerdict(VERDICT_EDEFAULT);
 				return;
 			case DivaPackage.CONTEXT__PRIORITY:
 				getPriority().clear();
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
 			case DivaPackage.CONTEXT__VARIABLE:
 				return variable != null && !variable.isEmpty();
 			case DivaPackage.CONTEXT__CONFIGURATION:
 				return configuration != null && !configuration.isEmpty();
 			case DivaPackage.CONTEXT__ORACLE:
 				return oracle != null;
 			case DivaPackage.CONTEXT__VERDICT:
 				return verdict != VERDICT_EDEFAULT;
 			case DivaPackage.CONTEXT__PRIORITY:
 				return priority != null && !priority.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (verdict: ");
 		result.append(verdict);
 		result.append(')');
 		return result.toString();
 	}
 	
 	/**
 	 * @generated NOT
 	 * @param variabilityModelImpl
 	 */
 	public void computePriorities(SimulationModel model) {
 		Map<Property, Priority> table = new HashMap<Property, Priority>();
 		for(Property prop : model.getModel().getProperty()) {
 			Priority p = DivaFactory.eINSTANCE.createPriority();
 			p.setProperty(prop);
 			p.setPriority(0);// this is the minimum
 			getPriority().add(p);
 			table.put(prop, p);
 		}
 		
 		for(Rule r : model.getModel().getRule()) {
 			if (r instanceof PriorityRule) {
 				PriorityRule pr = (PriorityRule) r;
 				if (pr.getContext().eval(this, null)) {
 					for(PropertyPriority pp : pr.getPriority()) {
 						Priority p = table.get(pp.getProperty());
 						if (p.getPriority() < pp.getPriority()) {
 							p.setPriority(pp.getPriority());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void computeScores(SimulationModel simuModel) {
 		for(Configuration cfg : getConfiguration()) {
 			cfg.computeScore(this);
 		}
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void computeVerdicts() {
 		for(Configuration c : getConfiguration()) {
 			c.computeVerdicts(getOracle());
 		}
 		Configuration cfg = bestConfiguration();
 		if (cfg == null)
 			setVerdict(Verdict.FAIL);
 		else
 			setVerdict(cfg.getVerdict());		
 	}
 
 	/**
 	 * @generated NOT
 	 * @return
 	 */
 	public Configuration bestConfiguration() {
 		if (getConfiguration().size() == 0)
 			return null;
 		else {
 			Iterator<Configuration> iter = getConfiguration().iterator();
 			Configuration max = iter.next();
 			while(iter.hasNext()) {
 				Configuration current = iter.next();
 				if (current.getTotalScore() > max.getTotalScore())
 					max = current;
 			}
 			return max;
 		}
 	}
 
 	@Override
 	/**
 	 * @generated NOT
 	 */
 	public Set<VariableValue> changes_from(Context other) {
 		Set<VariableValue> result = new HashSet<VariableValue>();
 		for(VariableValue vv : getVariable()) {
 			for(VariableValue ovv : other.getVariable()) {
 				if (ovv.getVariable().equals(vv.getVariable())) {
 					if(! ovv.hasSameValue(vv)) {
						result.add(ovv);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 } //ContextImpl
