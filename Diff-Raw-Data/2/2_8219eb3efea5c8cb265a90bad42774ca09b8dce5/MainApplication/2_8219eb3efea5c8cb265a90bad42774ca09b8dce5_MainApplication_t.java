 package pn2sc;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.incquery.runtime.api.AdvancedIncQueryEngine;
 import org.eclipse.incquery.runtime.api.IncQueryEngine;
 import org.eclipse.incquery.runtime.evm.api.EventDrivenVM;
 import org.eclipse.incquery.runtime.evm.api.ExecutionSchema;
 import org.eclipse.incquery.runtime.evm.api.Job;
 import org.eclipse.incquery.runtime.evm.api.RuleSpecification;
 import org.eclipse.incquery.runtime.evm.api.Scheduler.ISchedulerFactory;
 import org.eclipse.incquery.runtime.evm.specific.Schedulers;
 import org.eclipse.incquery.runtime.evm.specific.event.IncQueryEventRealm;
 import org.eclipse.incquery.runtime.evm.specific.job.EnableJob;
 import org.eclipse.incquery.runtime.exception.IncQueryException;
 
 import pn2sc.jobs.Pn2ScJobs;
 import pn2sc.queries.EquivContainsMatcher;
 import pn2sc.queries.EquivMatcher;
 import pn2sc.queries.TraceElementMatcher;
 import pn2sctrace.PN2SCTracemodel;
 import pn2sctrace.Pn2sctraceFactory;
 import PetriNet.PetriNetPackage;
 
 import com.google.common.base.Stopwatch;
 
 public class MainApplication implements IApplication {
 
 	private Resource stateChartResource;
 	private Resource traceResource;
 
 	private Config config;
 	private Resource petriNetResource;
 	private PN2SCTracemodel traceModel;
 	private ResourceSetImpl resourceSet;
 	private Stopwatch stopwatch;
 	private AdvancedIncQueryEngine engine;
 	private ISchedulerFactory schedulerFactory;
 	private Set<RuleSpecification<?>> rules;
 	private ExecutionSchema executionSchema;
 	private Pn2ScJobs pn2ScJobs;
 
 	@Override
 	public Object start(IApplicationContext appContext) throws Exception {
 
 		String[] args = (String[])appContext.getArguments().get("application.args");
 		// process program arguments
 		config = new Config();
 		config.processParameters(args);
 
 		// load phase
 		startWatch();
 		loadModel();
 		stopWatch("read");
 
 
 		// transformation phase
 		startWatch();
 
 		// create empty rule set and helper job holder
 		rules = new HashSet<RuleSpecification<?>>();
 		pn2ScJobs = new Pn2ScJobs(engine, petriNetResource, stateChartResource, traceResource, config.getBasePath(), config.getDebugTransform());
 		pn2ScJobs.setMatchers(
 				EquivMatcher.on(engine), 
 				EquivContainsMatcher.on(engine), 
 				TraceElementMatcher.on(engine));
 
 		// perform initialisation and then transformation
 		initialisation();
 		transformPn2Sc();
 		stopWatch("transformation");
 
 		// save the models
 		startWatch();
 		traceResource.save(null);
 		stateChartResource.save(null);
 		petriNetResource.save(null);
 		stopWatch("save");
 		
 
 		// Change driven demo
 		if (config.getChangeDriven() == 1) {
 			changePropagation();
 		}
 		return IApplication.EXIT_OK;
 	}
 
 	private PN2SCTracemodel initTraceModel() {
 		Pn2sctraceFactory factory = Pn2sctraceFactory.eINSTANCE;
 		PN2SCTracemodel pn2scTracemodel = factory.createPN2SCTracemodel();
 		return pn2scTracemodel;
 	}
 
 	/* 
 	 * Load source PetriNet and create StateChart and the trace model.
 	 */
 	public void loadModel() {
 		// create resource set for resources: petrinet, statechart, tracemodel
 		resourceSet = new ResourceSetImpl();
 		// read petrinet
 		URI sourceURI = URI.createFileURI(config.getBasePath() + "/" + config.getSourceFile()+".petrinet");
 		petriNetResource = resourceSet.getResource(sourceURI, true);
 		
 		// create statechart
 		URI targetURI = URI.createFileURI(config.getBasePath() + "/" + config.getSourceFile()+".statecharts");
 		stateChartResource = resourceSet.createResource(targetURI);
 		// create trace model
 		URI traceURI = URI.createFileURI(config.getBasePath() + "/" + config.getSourceFile()+".pn2sctrace");
 		traceResource = resourceSet.createResource(traceURI);
 		traceModel = initTraceModel();
 		traceResource.getContents().add(traceModel);
 
 		// start engine for the resources
 		try {
 			engine = AdvancedIncQueryEngine.from(IncQueryEngine.on(resourceSet));
 			//new GroupOfFilePn2sc().prepare(engine);
 		} catch (IncQueryException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		//schedulerFactory = UpdateCompleteBasedScheduler.getIQBaseSchedulerFactory(engine);
 		schedulerFactory = Schedulers.getIQEngineSchedulerFactory(engine); 
 		
 	}
 
 	/*
 	 * Run the initialisation phase of the transformation.
 	 */
 	public void initialisation() {
 		// place->OR mapping
 		rules = pn2ScJobs.getInitialisationRules();
 
 		// execute rule engine
 		executionSchema = EventDrivenVM.createExecutionSchema(IncQueryEventRealm.create(engine), schedulerFactory, rules);
 		executionSchema.dispose();
 		rules.clear();
 	}
 
 	public void transformPn2Sc() {
 		// execute AND and OR rules
 		rules = pn2ScJobs.getAndOrRules();
 
 		executionSchema = EventDrivenVM.createExecutionSchema(IncQueryEventRealm.create(engine), schedulerFactory, rules);
 		executionSchema.dispose();
 		rules.clear();
 		
 		// clean orphaned root ORs; and create StateChart root
 		rules = pn2ScJobs.getFinalisationRules();
 
 		executionSchema = EventDrivenVM.createExecutionSchema(IncQueryEventRealm.create(engine), schedulerFactory, rules);
 		executionSchema.dispose();
 		rules.clear();
 	}
 
 	public void changePropagation() {
 		try {
 
 			/* setup change propagation rules */
 			rules = pn2ScJobs.getCPRules();
 
 			/* setup rule engine */
 			// add name feature to watch updates
 			HashSet<EStructuralFeature> features = new HashSet<EStructuralFeature>();
 			features.add(PetriNetPackage.Literals.NAMED_ELEMENT__NAME);
 			engine.getBaseIndex().registerEStructuralFeatures(features);
 			//enginePN.getLogger().setLevel(Level.DEBUG);
 			
 			// create execution schema, set enablejobs to false before it, and true after
 			for(RuleSpecification<?> ruleSpec : rules) {
 				for(Job<?> job : ruleSpec.getJobs().values()) {
 					if (job instanceof EnableJob) {
 						((EnableJob<?>) job).setEnabled(false);
 					}
 				}
 			}
 			executionSchema = EventDrivenVM.createExecutionSchema(IncQueryEventRealm.create(engine), schedulerFactory, rules);
 			for(RuleSpecification<?> ruleSpec : rules) {
 				for(Job<?> job : ruleSpec.getJobs().values()) {
 					if (job instanceof EnableJob) {
 						((EnableJob<?>) job).setEnabled(true);
 					}
 				}
 			}
 			//Context context = executionSchema.getContext();
 			//context.put(EnableableJob.EXECUTE_JOB, false);
 
 			/* modify instance model */
 			pn2ScJobs.manipulate();
 			
 			// dispose schema
 			executionSchema.dispose();
 			rules.clear();
 		} catch (IncQueryException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	/*
 	 * Start measuring time
 	 */
 	public void startWatch() {
 		stopwatch = new Stopwatch().start();
 	}
 
 	/* 
 	 * Stop measuring time, and print elapsed time in millisecs
 	 */
 	public void stopWatch(String id) {
 		stopwatch.stop();
 		long readTime = stopwatch.elapsedTime(TimeUnit.MILLISECONDS);
 		//System.out.println("Model,Phase,Type,Value,Unit");
 		System.out.println(config.getSourceFile() + "," + id + ",Time," + readTime + ",ms");
		System.out.println(config.getSourceFile() + "," + id + ",Memory," + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + ",bytes");
 	}
 
 	@Override
 	public void stop() {
 	}
 
 }
