 package org.eclipse.stem.core.scenario.impl;
 
 /******************************************************************************* 
  * Copyright (c) 2006, 2008 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  *     IBM Corporation - initial API and implementation 
  *******************************************************************************/
  
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.stem.core.Constants;
 import org.eclipse.stem.core.CorePlugin;
 import org.eclipse.stem.core.IdentifiableFilter;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.common.impl.IdentifiableImpl;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.UnresolvedIdentifiable;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.IntegrationDecorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.model.ModelPackage;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.scenario.ScenarioPackage;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.core.solver.Solver;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Scenario</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getModel <em>Model</em>}</li>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getSequencer <em>Sequencer</em>}</li>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getScenarioDecorators <em>Scenario Decorators</em>}</li>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getCanonicalGraph <em>Canonical Graph</em>}</li>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getProgress <em>Progress</em>}</li>
  *   <li>{@link org.eclipse.stem.core.scenario.impl.ScenarioImpl#getSolver <em>Solver</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ScenarioImpl extends IdentifiableImpl implements Scenario {
 
 	/**
 	 * This is used to generate unique {@link URI}'s for each {@link Scenario} generated
 	 */
 	private int scenarioCounter = 0;
 
 	/**
 	 * This is the {@link URI} used to identify the canonical {@link Graph} used during the
 	 * simulation
 	 * 
 	 * TODO is this the right URI to use for canonical graphs?
 	 */
 	protected static final URI CANONICAL_GRAPH_URI = URI
 			.createURI("stem:canonicalgraph");
 	
 	/**
 	 * This flag controls the reporting of each and every unresolved 
 	 * {@link Identifiable} reference encountered when creating the 
 	 * canonical {@link Graph}.  Default: <code>false</code>
 	 */
 	public static boolean reportEachUnresolvedIdentifiable = true;
 
 	/**
 	 * This flag controls the reporting of dangling air transport edges 
 	 */
 	public static boolean reportDanglingAirTransportEdges = false; 
 
 	/**
 	 * This flag controls the reporting of a single summary message of the 
 	 * number of unresolved {@link Identifiable} references encountered 
 	 * when creating the canonical {@link Graph}. Default: <code>false</code>
 	 */
 	public static boolean reportNumberofUnresolvedIdentifiables = false;
 	
 	/**
 	 * The cached value of the '{@link #getModel() <em>Model</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getModel()
 	 * @generated
 	 * @ordered
 	 */
 	protected Model model;
 
 	/**
 	 * The cached value of the '{@link #getSequencer() <em>Sequencer</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getSequencer()
 	 * @generated
 	 * @ordered
 	 */
 	protected Sequencer sequencer;
 
 	/**
 	 * The cached value of the '{@link #getScenarioDecorators() <em>Scenario Decorators</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getScenarioDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Decorator> scenarioDecorators;
 
 	/**
 	 * The cached value of the '{@link #getCanonicalGraph() <em>Canonical Graph</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getCanonicalGraph()
 	 * @generated
 	 * @ordered
 	 */
 	protected Graph canonicalGraph;
 	
 	/**
 	 * The default value of the '{@link #getProgress() <em>Progress</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProgress()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double PROGRESS_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getProgress() <em>Progress</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProgress()
 	 * @generated
 	 * @ordered
 	 */
 	protected double progress = PROGRESS_EDEFAULT;
 
 	
 	/**
 	 * The cached value of the '{@link #getSolver() <em>Solver</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSolver()
 	 * @generated
 	 * @ordered
 	 */
 	protected Solver solver;
 
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected ScenarioImpl() {
 		super();
 		setURI(STEMURI.createURI(URI_TYPE_SCENARIO_SEGMENT + "/"
 				+ Integer.toString(scenarioCounter++)));
 		setTypeURI(STEMURI.SCENARIO_TYPE_URI);
 	} // ScenarioImpl
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ScenarioPackage.Literals.SCENARIO;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Model getModel() {
 		if (model != null && model.eIsProxy()) {
 			InternalEObject oldModel = (InternalEObject)model;
 			model = (Model)eResolveProxy(oldModel);
 			if (model != oldModel) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ScenarioPackage.SCENARIO__MODEL, oldModel, model));
 			}
 		}
 		return model;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Model basicGetModel() {
 		return model;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModel(Model newModel) {
 		Model oldModel = model;
 		model = newModel;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ScenarioPackage.SCENARIO__MODEL, oldModel, model));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Sequencer getSequencer() {
 		if (sequencer != null && sequencer.eIsProxy()) {
 			InternalEObject oldSequencer = (InternalEObject)sequencer;
 			sequencer = (Sequencer)eResolveProxy(oldSequencer);
 			if (sequencer != oldSequencer) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ScenarioPackage.SCENARIO__SEQUENCER, oldSequencer, sequencer));
 			}
 		}
 		return sequencer;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Sequencer basicGetSequencer() {
 		return sequencer;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSequencer(Sequencer newSequencer) {
 		Sequencer oldSequencer = sequencer;
 		sequencer = newSequencer;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ScenarioPackage.SCENARIO__SEQUENCER, oldSequencer, sequencer));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * The value is set in {@link #initialize()} <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Graph getCanonicalGraph() {
 		return canonicalGraph;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getProgress() {
 		return progress;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProgress(double newProgress) {
 		double oldProgress = progress;
 		progress = newProgress;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ScenarioPackage.SCENARIO__PROGRESS, oldProgress, progress));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Solver getSolver() {
 		return solver;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSolver(Solver newSolver, NotificationChain msgs) {
 		Solver oldSolver = solver;
 		solver = newSolver;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ScenarioPackage.SCENARIO__SOLVER, oldSolver, newSolver);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSolver(Solver newSolver) {
 		if (newSolver != solver) {
 			NotificationChain msgs = null;
 			if (solver != null)
 				msgs = ((InternalEObject)solver).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ScenarioPackage.SCENARIO__SOLVER, null, msgs);
 			if (newSolver != null)
 				msgs = ((InternalEObject)newSolver).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ScenarioPackage.SCENARIO__SOLVER, null, msgs);
 			msgs = basicSetSolver(newSolver, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ScenarioPackage.SCENARIO__SOLVER, newSolver, newSolver));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return <code>true</code> if it's ok to call step again.
 	 * 
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean step() {
 		// Is the sequencer finished?
 		if (!getSequencer().isTimeToStop()) {
 			// No
 			// Is there a canonical graph?
 			if (canonicalGraph == null) {
 				// No
 				// Create one then...
 				initialize();
 			} // if no canonical graph
 
 			// Everything should be sane
 			assert sane();
 
 			final STEMTime currentTime = getSequencer().getNextTime();
 			final long timeDelta = getSequencer().getTimeDelta();
 			
 			// First reset the progress of each decorator to 0. Necessary for the iteration progress
 			// bar
 			for (final Iterator<Decorator> decoratorIter = canonicalGraph
 					.getDecorators().iterator(); decoratorIter.hasNext();) {
 				final Decorator decorator = decoratorIter.next();
 				decorator.setProgress(0.0);
 			}
 			
 			// This is the solver we will be using to advance the solution
 			// one step
 			
 			Solver solver = this.getSolver();
 			
 			// Needed to support old model where the solver was part of the decorator
 			
 			if(solver == null) {
 				Solver [] solvers = this.getSolvers();
 				// Use the default finite difference when not available
 				for(Solver s:solvers)
 					if(s.getClass().getName().equals("org.eclipse.stem.solvers.fd.impl.FiniteDifferenceImpl"))
 					{solver = s;this.setSolver(s);break;}
 			}
 			
 			// Make sure the decorators are set on the solver
 			if(solver.getDecorators() == null) solver.setDecorators(canonicalGraph.getDecorators());
 			
 			// Do the one step using the current solver
 			solver.step(currentTime, timeDelta, getSequencer().getCycle());
 						
 			// Everything should still be sane
 			assert sane();
 	
 			// Once everyone is done, we tell the graph to switch all the
 			// dynamic labels to their next value and then we're ready to do
 			// it all over again.
 			canonicalGraph.switchToNextValue(currentTime);
 		} // if sequencer finished
 
 		return !getSequencer().isTimeToStop();
 	} // step
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void reset() {
 		// Set the sequencer back to its original state. The sequencer determines
 		// the "current" time of the simulation. This is a value used to compute
 		// the values of dynamic labels for each step of the simulation.
 		getSequencer().reset();
 		
 		//  Now give each decorator a chance to reset its dynamic
 		// labels in the canonical graph
 		
 		// SED fix. We need to reset the disease models first since the infectors
 		// inoculators depend up those being reset beforehand.
 		
 		ArrayList<Decorator>intDecorators = new ArrayList<Decorator>();
 		ArrayList<Decorator>otherDecorators = new ArrayList<Decorator>();
 		
 		for (final Iterator<Decorator> graphDecoratorIter = canonicalGraph.getDecorators()
 				.iterator(); graphDecoratorIter.hasNext();) {
 			final Decorator decorator = graphDecoratorIter
 					.next();
 			if(decorator instanceof IntegrationDecorator)intDecorators.add(decorator);
 			else otherDecorators.add(decorator);
 		}
 		for(Decorator decorator:intDecorators){
 			// Is the Decorator enabled?
 			if (decorator.isEnabled()) {
 				// Yes
 				decorator.resetLabels();
 			} // if 
 			
 			// Reset the solver
 		} // for each decorator
 		for(Decorator decorator:otherDecorators){
 			// Is the Decorator enabled?
 			if (decorator.isEnabled()) {
 				// Yes
 				decorator.resetLabels();
 			} // if 
 			
 			// Reset the solver
 		} // for each decorator
 	
 		solver.reset();
 	}// reset
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void initialize() {
 		// Set the sequencer back to its original state. The sequencer determines
 		// the "current" time of the simulation. This is a value used to compute
 		// the values of dynamic labels for each step of the simulation.
 		getSequencer().reset();
 
 		// Get the canonical graph that we'll use for the simulation. It
 		// maintains all state information during the simulation.
 		canonicalGraph = getModel().getCanonicalGraph(CANONICAL_GRAPH_URI, new IdentifiableFilter(getModel().getDublinCore().getCoverage()));
 
 		canonicalGraph.setTime((STEMTime) EcoreUtil.copy(getSequencer()
 				.getCurrentTime()));
 
 		//
 		// Confirm, re-validate the set of unresolved identifiables.
 		// Because we add graphs to graphs and models to models some of the 
 		// unresolved identifiables do not resolve until the very end
 		// this method does that check on the FINAL canonicalGraph.
 		//
 		Iterator<UnresolvedIdentifiable> unresolved = canonicalGraph.getUnresolvedIdentifiables().iterator();
 		  while (unresolved.hasNext()) {
 		   UnresolvedIdentifiable temp = unresolved.next();
 		   
 		   if (canonicalGraph.getNodes().get(temp.getUnresolvedURI()) != null 
 		     || canonicalGraph.getEdges().get(temp.getUnresolvedURI()) != null) {
 		    unresolved.remove();
 		   }
 		}
 		// Just checking...
 		assert canonicalGraph.sane();
 
 		// Add the adaptors to keep track of the progress for each graph decorator
 		final double numDecorators = this.getCanonicalGraph().getDecorators().size();
 		final Scenario self = this;
 		for (final Iterator<Decorator> scenarioDecorators = this.getCanonicalGraph().getDecorators()
 				.iterator(); scenarioDecorators.hasNext();) {
 			final Decorator decorator = scenarioDecorators.next();
 			decorator.eAdapters().add(
 					new AdapterImpl() {
 						/**
 						   * @override
 						   */
 						  public void notifyChanged(Notification msg)
 						  {
 							  Decorator decorator = (Decorator)msg.getNotifier();
 							  switch(msg.getFeatureID(Decorator.class)) {
 							  	case ModelPackage.DECORATOR__PROGRESS:
 							  		double delta = (msg.getNewDoubleValue() - msg.getOldDoubleValue());
 							  		delta /= numDecorators;
 							  		self.setProgress(self.getProgress()+delta);
 							  }
 						  }
 					}
 			);
 		}
 		
 		// The scenario decorators are regular decorators that are allowed to
 		// modified the state of the canonical graph to customize it for a
 		// particular scenario. At this point the model decorators have done
 		// their job. They were invoked above in the call to
 		// getCanonicalGraph(). Now it is the turn of the scenario decorators to
 		// add their final touch to the graph. The scenario decorators need to
 		// be copied however so that any state information they may generate and
 		// retain does not become part of the scenario itself. 
 
		ArrayList<Decorator>intDecorators = new ArrayList<Decorator>();
		ArrayList<Decorator>otherDecorators = new ArrayList<Decorator>();
 		// Let the scenario decorators decorate the graph
 		for (final Iterator<Decorator> scenarioDecorators = getScenarioDecorators()
 				.iterator(); scenarioDecorators.hasNext();) {
 			final Decorator decorator = (Decorator) EcoreUtil.copy(scenarioDecorators.next());
			if(decorator instanceof IntegrationDecorator)intDecorators.add(decorator);
			else otherDecorators.add(decorator);
		}
		for(Decorator decorator:intDecorators) {
			canonicalGraph.getDecorators().add(decorator);
			decorator.decorateGraph();
			decorator.setGraphDecorated(true);
		} 
		for(Decorator decorator:otherDecorators) {
 			canonicalGraph.getDecorators().add(decorator);
 			decorator.decorateGraph();
 			decorator.setGraphDecorated(true);
		} 
 
 		
 		// Are there any unresolved identifiables in the canonical graph and 
 		// should we report them?
 		if ( reportEachUnresolvedIdentifiable && canonicalGraph.getUnresolvedIdentifiables().size() > 0) {
 			// Yes
 			logUnresolvedIdentifiables();
 		}
 		
 		// Are there any unresolved identifiables in the canonical graph and 
 		// should we report their number?
 		if ( reportNumberofUnresolvedIdentifiables && canonicalGraph.getUnresolvedIdentifiables().size() > 0) {
 			// Yes
 			logNumberOfUnresolvedIdentifiables();
 		}
 	} // initialize
 
 	private org.eclipse.stem.core.solver.Solver [] getSolvers() {
 		Solver [] solvers;
 		final IExtensionRegistry registry = Platform.getExtensionRegistry();
 		final IConfigurationElement[] solverConfigElements = registry
 				.getConfigurationElementsFor(org.eclipse.stem.core.Constants.ID_SOLVER_EXTENSION_POINT);
 
 		final List<Solver> temp = new ArrayList<Solver>();
 
 		solvers = new Solver[solverConfigElements.length];
 
 		for (int i = 0; i < solverConfigElements.length; i++) {
 			final IConfigurationElement element = solverConfigElements[i];
 			// Does the element specify the class of the disease model?
 				if (element.getName().equals(Constants.SOLVER_ELEMENT)) {
 					// Yes
 					try {
 						temp.add((Solver) element
 								.createExecutableExtension("class")); //$NON-NLS-1$
 					} catch (final Exception e) {
 						CorePlugin.logError(
 								"Can't create solver", e); //$NON-NLS-1$
 					}
 				} // if
 			} // for each configuration element
 
 			solvers = temp.toArray(new Solver[] {});
 
 		return solvers;
 	}
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public String produceTitle() {
 		String retValue = "";
 		if (dublinCore != null) {
 			final String title = dublinCore.getTitle();
 			retValue = title == null ? "" : title;
 		}
 		return retValue;
 	} // produceTitle
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ScenarioPackage.SCENARIO__SOLVER:
 				return basicSetSolver(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Decorator> getScenarioDecorators() {
 		if (scenarioDecorators == null) {
 			scenarioDecorators = new EObjectResolvingEList<Decorator>(Decorator.class, this, ScenarioPackage.SCENARIO__SCENARIO_DECORATORS);
 		}
 		return scenarioDecorators;
 	}
 
 	/**
 	 * Report the unresolved Identifiables in the canonical graph
 	 */
 	private void logUnresolvedIdentifiables() {
 		for (Iterator<UnresolvedIdentifiable> unresolvedIter = canonicalGraph
 				.getUnresolvedIdentifiables().iterator(); unresolvedIter
 				.hasNext();) {
 			final UnresolvedIdentifiable unresolvedID = unresolvedIter
 					.next();
 				
 			CorePlugin.logInformation("In scenario "
 					+ produceTitle()
 					+ "\", model \""
 					+ unresolvedID.getModel().getDublinCore()
 							.getTitle()
 					+ "\",  the URI \""
 					+ unresolvedID.getUnresolvedURI()
 					+ "\" was unresolved."
 	
 			, null);
 		} // for each unresolved identifiable
 	} // reportUnresolvedIdentifiables
 
 	/**
 	 * Report the number of unresolved Identifiables in the canonical graph.
 	 */
 	private void logNumberOfUnresolvedIdentifiables() {
 		CorePlugin.logInformation("In scenario \""
 				+ produceTitle()
 				+ "\", there were "
 				+ canonicalGraph.getUnresolvedIdentifiables().size()
 				+ " unresolved references."
 		, null);
 		
 	} // logNumberOfUnresolvedIdentifiables
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ScenarioPackage.SCENARIO__MODEL:
 				if (resolve) return getModel();
 				return basicGetModel();
 			case ScenarioPackage.SCENARIO__SEQUENCER:
 				if (resolve) return getSequencer();
 				return basicGetSequencer();
 			case ScenarioPackage.SCENARIO__SCENARIO_DECORATORS:
 				return getScenarioDecorators();
 			case ScenarioPackage.SCENARIO__CANONICAL_GRAPH:
 				return getCanonicalGraph();
 			case ScenarioPackage.SCENARIO__PROGRESS:
 				return getProgress();
 			case ScenarioPackage.SCENARIO__SOLVER:
 				return getSolver();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 		@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ScenarioPackage.SCENARIO__MODEL:
 				setModel((Model)newValue);
 				return;
 			case ScenarioPackage.SCENARIO__SEQUENCER:
 				setSequencer((Sequencer)newValue);
 				return;
 			case ScenarioPackage.SCENARIO__SCENARIO_DECORATORS:
 				getScenarioDecorators().clear();
 				getScenarioDecorators().addAll((Collection<? extends Decorator>)newValue);
 				return;
 			case ScenarioPackage.SCENARIO__PROGRESS:
 				setProgress((Double)newValue);
 				return;
 			case ScenarioPackage.SCENARIO__SOLVER:
 				setSolver((Solver)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ScenarioPackage.SCENARIO__MODEL:
 				setModel((Model)null);
 				return;
 			case ScenarioPackage.SCENARIO__SEQUENCER:
 				setSequencer((Sequencer)null);
 				return;
 			case ScenarioPackage.SCENARIO__SCENARIO_DECORATORS:
 				getScenarioDecorators().clear();
 				return;
 			case ScenarioPackage.SCENARIO__PROGRESS:
 				setProgress(PROGRESS_EDEFAULT);
 				return;
 			case ScenarioPackage.SCENARIO__SOLVER:
 				setSolver((Solver)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ScenarioPackage.SCENARIO__MODEL:
 				return model != null;
 			case ScenarioPackage.SCENARIO__SEQUENCER:
 				return sequencer != null;
 			case ScenarioPackage.SCENARIO__SCENARIO_DECORATORS:
 				return scenarioDecorators != null && !scenarioDecorators.isEmpty();
 			case ScenarioPackage.SCENARIO__CANONICAL_GRAPH:
 				return canonicalGraph != null;
 			case ScenarioPackage.SCENARIO__PROGRESS:
 				return progress != PROGRESS_EDEFAULT;
 			case ScenarioPackage.SCENARIO__SOLVER:
 				return solver != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#toString()
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		// If there is a title then we use that otherwise we let the super class
 		// handle it
 		final String title = getDublinCore().getTitle();
 		return (title != null ? title : super.toString());
 	} // toString
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#sane()
 	 * @generated NOT
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		if (retValue && model != null) {
 			retValue = retValue && model.sane();
 			assert retValue;
 		}
 
 		if (retValue && canonicalGraph != null) {
 			retValue = retValue && canonicalGraph.sane();
 			assert retValue;
 		}
 
 		if (retValue && sequencer != null) {
 			retValue = retValue && sequencer.sane();
 			assert retValue;
 		}
 
 		return retValue;
 	} // sane
 
 } // ScenarioImpl
