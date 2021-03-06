 package org.strategoxt.imp.runtime.services;
 
import static org.spoofax.interpreter.core.Tools.*;
import static org.spoofax.interpreter.terms.IStrategoTerm.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import lpg.runtime.IAst;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.imp.parser.IModelListener;
 import org.eclipse.imp.parser.IParseController;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.StackTracer;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.library.LoggingIOAgent;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.strategoxt.HybridInterpreter;
 import org.strategoxt.IncompatibleJarException;
 import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
 import org.strategoxt.imp.generator.sdf2imp;
 import org.strategoxt.imp.runtime.Debug;
 import org.strategoxt.imp.runtime.EditorState;
 import org.strategoxt.imp.runtime.Environment;
 import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
 import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
 import org.strategoxt.imp.runtime.dynamicloading.IDynamicLanguageService;
 import org.strategoxt.imp.runtime.parser.SGLRParseController;
 import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
 import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
 import org.strategoxt.imp.runtime.stratego.StrategoConsole;
 import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
 import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
 import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNode;
 import org.strategoxt.lang.Context;
 import org.strategoxt.lang.StrategoException;
 import org.strategoxt.stratego_lib.set_config_0_0;
 
 /**
  * Basic Stratego feedback (i.e., errors and warnings) provider.
  * This service may also be used as a basis for other semantic services
  * such as reference resolving.
  * 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class StrategoObserver implements IDynamicLanguageService, IModelListener {
 	
 	// TODO: separate delay for error markers?
 	public static final int OBSERVER_DELAY = 600;
 	
 	private static Map<Descriptor, HybridInterpreter> cachedRuntimes =
 		Collections.synchronizedMap(new WeakHashMap<Descriptor, HybridInterpreter>());
 	
 	private final String feedbackFunction;
 	
 	private final AstMessageHandler messages = new AstMessageHandler(AstMessageHandler.ANALYSIS_MARKER_TYPE);
 	
 	private Lock observerSchedulerLock = new ReentrantLock();
 	
 	private Job asyncObserverScheduler;
 	
 	private HybridInterpreter runtime;
 	
 	private volatile Descriptor descriptor;
 	
 	private volatile boolean isUpdateStarted;
 	
 	private volatile boolean rushNextUpdate;
 	
 	public StrategoObserver(Descriptor descriptor, String feedbackFunction) {
 		this.descriptor = descriptor;
 		this.feedbackFunction = feedbackFunction;
 	}
 
 	public final AnalysisRequired getAnalysisRequired() {
 		return AnalysisRequired.TYPE_ANALYSIS;
 	}
 	
 	/**
 	 * Returns a value indicating whether or not an analysis has
 	 * been scheduled or completed at this point.
 	 * 
 	 * @return true if update() or asyncUpdate() have been called.
 	 */
 	public boolean isUpdateScheduled() {
 		return isUpdateStarted;
 	}
 	
 	public void setRushNextUpdate(boolean rushNextUpdate) {
 		this.rushNextUpdate = rushNextUpdate;
 	}
 	
 	public AstMessageHandler getMessages() {
 		return messages;
 	}
 	
 	public Object getSyncRoot() {
 		 // TODO: *maybe* use descriptor as syncroot? deadlocky?
 		return Environment.getSyncRoot();
 	}
 
 	public String getLog() {
 		assert Thread.holdsLock(getSyncRoot());
 		return ((EditorIOAgent) runtime.getIOAgent()).getLog().trim();
 	}
 	
 	private void init(IProgressMonitor monitor) {
 		assert Thread.holdsLock(getSyncRoot());
 		
 		HybridInterpreter prototype = cachedRuntimes.get(descriptor);
 		if (prototype != null) {
 			runtime = new HybridInterpreter(prototype);
 			return;
 		}
 		
 		monitor.subTask("Loading analysis runtime");
 		
 		Debug.startTimer();
 		List<String> jars = new ArrayList<String>();
 		
 		for (File file : descriptor.getAttachedFiles()) {
 			String filename = file.toString();
 			if (filename.endsWith(".ctree")) {
 				initRuntime(monitor);
 				loadCTree(filename);
 			} else if (filename.endsWith(".jar")) {
 				initRuntime(monitor);
 				jars.add(filename);
 			} else if (filename.endsWith(".str")) {
 				Environment.asynOpenErrorDialog("Loading analysis components", "Cannot use .str files as a provider: please specify a .ctree or .jar file instead (usually built in /include/)", null);
 			}
 		}
 		
 		if (!jars.isEmpty()) loadJars(jars);
 		Debug.stopTimer("Loaded analysis components");
 		
 		monitor.subTask(null);
 		cachedRuntimes.put(descriptor, runtime);
 	}
 
 	private void initRuntime(IProgressMonitor monitor) {
 		assert Thread.holdsLock(getSyncRoot());
 		
 		if (runtime == null) {
 			Debug.startTimer();
 			runtime = Environment.createInterpreter(getSyncRoot() != Environment.getSyncRoot());
 			runtime.init();
 			Debug.stopTimer("Created new Stratego runtime instance");
 			try {
 				ITermFactory factory = runtime.getFactory();
 				IStrategoTuple programName = factory.makeTuple(
 						factory.makeString("program"),
 						factory.makeString(descriptor.getLanguage().getName()));
 				set_config_0_0.instance.invoke(runtime.getCompiledContext(), programName);
 			} catch (BadDescriptorException e) {
 				// Ignore
 			}
 			monitor.subTask("Loading analysis runtime components");
 		}
 	}
 
 	private void loadCTree(String filename) {
 		try {
 			Debug.startTimer("Loading Stratego module ", filename);
 			assert getSyncRoot() == Environment.getSyncRoot() || !Thread.holdsLock(Environment.getSyncRoot());
 			synchronized (Environment.getSyncRoot()) {
 				runtime.load(descriptor.openAttachment(filename));
 			}
 			Debug.stopTimer("Successfully loaded " +  filename);
 		} catch (InterpreterException e) {
 			Environment.logException(new BadDescriptorException("Error loading compiler service provider " + filename, e));
 			if (descriptor.isDynamicallyLoaded())
 				Environment.asynOpenErrorDialog("Dynamic descriptor loading", "Error loading compiler service provider " + filename, e);
 		} catch (IOException e) {
 			Environment.logException(new BadDescriptorException("Could not load compiler service provider " + filename, e));
 			if (descriptor.isDynamicallyLoaded())
 				Environment.asynOpenErrorDialog("Dynamic descriptor loading", "Error loading compiler service provider " + filename, e);
 		}
 	}
 	
 	private void loadJars(List<String> jars) {
 		try {
 			URL[] classpath = new URL[jars.size()];
 			for (int i = 0; i < classpath.length; i++) {
				classpath[i] = descriptor.getBasePath().append(jars.get(i)).toFile().toURL();
 			}
 			runtime.loadJars(classpath);
 		} catch (SecurityException e) {
 			Environment.logException("Error loading compiler service providers " + jars, e);
 			if (descriptor.isDynamicallyLoaded())
 				Environment.asynOpenErrorDialog("Dynamic descriptor loading", "Error loading compiler service providers " + jars, e);
 		} catch (IncompatibleJarException e) {
 			Environment.logException("Error loading compiler service providers " + jars, e);
 			if (descriptor.isDynamicallyLoaded())
 				Environment.asynOpenErrorDialog("Dynamic descriptor loading", "Error loading compiler service providers " + jars, e);
 		} catch (IOException e) {
 			Environment.logException("Error loading compiler service providers " + jars, e);
 			if (descriptor.isDynamicallyLoaded())
 				Environment.asynOpenErrorDialog("Dynamic descriptor loading", "Error loading compiler service providers " + jars, e);
 		}
 	}
 
 	/**
 	 * Starts a new update() operation, asynchronously.
 	 */
 	public void scheduleUpdate(final IParseController parseController) {
 		isUpdateStarted = true;
 		
 		observerSchedulerLock.lock();
 		try {
 			if (asyncObserverScheduler != null)
 				asyncObserverScheduler.cancel();
 				
 			// TODO: reuse observer schedulers; just rename them and set a new parsecontroller
 			asyncObserverScheduler = new Job("Analyzing updates to " + parseController.getPath().lastSegment()) {
 				@Override
 				public IStatus run(IProgressMonitor monitor) {
 					monitor.beginTask("", IProgressMonitor.UNKNOWN);
 					update(parseController, monitor);
 					return Status.OK_STATUS;
 				}
 			};
 			
 			// UNDONE: observer job is no longer a WorkspaceJob
 			//         thus avoiding analysis delays and progress view spamming 
 			// asyncObserverScheduler.setRule(parseController.getProject().getResource());
 			asyncObserverScheduler.setSystem(true);
 			if (rushNextUpdate) {
 				rushNextUpdate = false;
 				asyncObserverScheduler.schedule(0);
 			} else {
 				asyncObserverScheduler.schedule(OBSERVER_DELAY);
 			}
 		} finally {
 			observerSchedulerLock.unlock();
 		}
 	}
 
 	public void update(IParseController parseController, IProgressMonitor monitor) {
 		isUpdateStarted = true;
 		
 		IStrategoAstNode ast = (IStrategoAstNode) parseController.getCurrentAst();
 		if (ast == null || ast.getConstructor() == null)
 			return;
 		
 		if (feedbackFunction == null) {
 			messages.clearMarkers(ast.getResource());
 			messages.commitDeletions();
 			return;
 		}
 			
 		if (monitor.isCanceled())
 			return;
 		
 		IStrategoTerm feedback = null;
 		
 		try {
 			synchronized (getSyncRoot()) {
 				feedback = invokeSilent(feedbackFunction, ast.getResource(), makeInputTerm(ast, false));
 	
 				if (feedback == null) {
 					reportRewritingFailed();
 					String log = getLog();
 					Environment.logException(log.length() == 0 ? "Analysis failed" : "Analysis failed:\n" + log);
 					messages.clearMarkers(ast.getResource());
 					messages.addMarkerFirstLine(ast.getResource(), "Analysis failed (see error log)", IMarker.SEVERITY_ERROR);
 					messages.commitAllChanges();
 				} else if (!monitor.isCanceled()) {
 					// TODO: figure out how this was supposed to be synchronized
 					presentToUser(ast.getResource(), feedback);
 				}
 			}
 		} finally {
 			// System.out.println("OBSERVED " + System.currentTimeMillis()); // DEBUG
 			// processEditorRecolorEvents(parseController);
 		}
 	}
 
 	@Deprecated
 	@SuppressWarnings("unused")
 	private void processEditorRecolorEvents(IParseController parseController) {
 		if (parseController instanceof SGLRParseController) {
 			EditorState editor = ((SGLRParseController) parseController).getEditor();
 			if (editor != null)
 				AstMessageHandler.processEditorRecolorEvents(editor.getEditor());
 		}
 		AstMessageHandler.processAllEditorRecolorEvents();
 	}
 
 	public void reportRewritingFailed() {
 		assert Thread.holdsLock(getSyncRoot());
 		StackTracer trace = runtime.getContext().getStackTracer();
 		runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println(
 				trace.getTraceDepth() != 0 ? "rewriting failed, trace:" : "rewriting failed");
 		trace.printStackTrace();
 		if (descriptor.isDynamicallyLoaded())
 			StrategoConsole.activateConsole();
 	}
 	
 	/* UNDONE: asynchronous feedback presentation
 	private void asyncPresentToUser(final IParseController parseController, final IStrategoTerm feedback, final String log) {
 		Job job = new WorkspaceJob("Showing feedback") {
 			{ setSystem(true); } // don't show to user
 			@Override
 			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
 				presentToUser(parseController, feedback, log);
 				return Status.OK_STATUS;
 			}
 		};
 		
 		job.setRule(parseController.getProject().getResource());
 		job.setSystem(true);
 		job.schedule();
 	}
 	*/
 
 	private void presentToUser(IResource resource, IStrategoTerm feedback) {
 		assert Thread.holdsLock(getSyncRoot());
 		assert feedback != null;
 
 		// TODO: use FileTrackingIOAgent to find out what to clear
 		// UNDONE: messages.clearAllMarkers();
 		messages.clearMarkers(resource);
 
 		try {
 			if (feedback.getTermType() == TUPLE
 					&& termAt(feedback, 0).getTermType() == LIST
 					&& termAt(feedback, 1).getTermType() == LIST
 					&& termAt(feedback, 2).getTermType() == LIST) {
 				
 			    IStrategoList errors = termAt(feedback, 0);
 			    IStrategoList warnings = termAt(feedback, 1);
 			    IStrategoList notes = termAt(feedback, 2);
 			    feedbackToMarkers(resource, errors, IMarker.SEVERITY_ERROR);
 			    feedbackToMarkers(resource, warnings, IMarker.SEVERITY_WARNING);
 			    feedbackToMarkers(resource, notes, IMarker.SEVERITY_INFO);
 			} else {
 				// Throw an exception to trigger an Eclipse pop-up  
 				throw new StrategoException("Illegal output from " + feedbackFunction + " (should be (errors,warnings,notes) tuple: " + feedback);
 			}
 		} finally {
 			messages.commitAllChanges();
 		}
 	}
 	
 	private final void feedbackToMarkers(IResource resource, IStrategoList feedbacks, int severity) {
 		assert Thread.holdsLock(getSyncRoot());
 		
 		Context context = runtime.getCompiledContext();
 		sdf2imp.init(context);
 		feedbacks = postProcessFeedback(feedbacks, context);
 		
 	    for (IStrategoTerm feedback : feedbacks.getAllSubterms()) {
 	        IStrategoTerm term = termAt(feedback, 0);
 			IStrategoString messageTerm = termAt(feedback, 1);
 			String message = messageTerm.stringValue();
 			
 			messages.addMarker(resource, term, message, severity);
 	    }
 	}
 
 	private IStrategoList postProcessFeedback(IStrategoList feedbacks, Context context) {
 		IStrategoList result =
 				(IStrategoList) postprocess_feedback_results_0_0.instance.invoke(context, feedbacks);
 		if (result == null) {
 			// Throw an exception to trigger an Eclipse pop-up  
 			throw new StrategoException("Illegal output from " + feedbackFunction + ": " + feedbacks);
 		}
 		return result;
 	}	
 	
 	/**
 	 * Invoke a Stratego function with a specific AST node as its input.
 	 * 
 	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
 	 */
 	public IStrategoTerm invoke(String function, IStrategoAstNode node)
 			throws UndefinedStrategyException, InterpreterErrorExit, InterpreterExit, InterpreterException {
 
 		IStrategoTerm input = makeInputTerm(node, true);
 		return invoke(function, input, node.getResource());
 	}
 
 	private static IStrategoTerm makeInputTerm(IStrategoAstNode node, boolean includeSubNode) {
 		ITermFactory factory = Environment.getTermFactory();
 		String path = node.getResource().getProjectRelativePath().toPortableString();
 		String absolutePath = node.getResource().getProject().getLocation().toOSString();
 		
 		if (includeSubNode) {
 			IStrategoTerm[] inputParts = {
 					node.getTerm(),
 					StrategoTermPath.createPath(node),
 					getRoot(node).getTerm(),
 					factory.makeString(path),
 					factory.makeString(absolutePath)
 				};
 			return factory.makeTuple(inputParts);
 		} else {
 			IStrategoTerm[] inputParts = {
 					node.getTerm(),
 					factory.makeString(path),
 					factory.makeString(absolutePath)
 				};
 			return factory.makeTuple(inputParts);
 		}
 	}
 	
 	/**
 	 * Invoke a Stratego function with a specific term its input,
 	 * given a particular working directory.
 	 */
 	public IStrategoTerm invoke(String function, IStrategoTerm term, IResource resource)
 			throws UndefinedStrategyException, InterpreterErrorExit, InterpreterExit, InterpreterException {
 		
 		synchronized (getSyncRoot()) {
 			if (runtime == null) init(new NullProgressMonitor());
 			if (runtime == null) return null;
 			
 		    Debug.startTimer();
 			// TODO: Make Context support monitor.isCanceled()?
 			//       (e.g., overriding Context.lookupPrimitive to throw an OperationCanceledException) 
 			
 			runtime.setCurrent(term);
 			IPath path = resource.getLocation();
 			initRuntimePath(path.removeLastSegments(1));
 
 			((LoggingIOAgent) runtime.getIOAgent()).clearLog();
 			boolean success = runtime.invoke(function);
 			
 			Debug.stopTimer("Evaluated strategy " + function + (success ? "" : " (failed)"));
 			return success ? runtime.current() : null;
 		}
 	}
 
 	/**
 	 * Invoke a Stratego function with a specific AST node as its input,
 	 * logging and swallowing all exceptions.
 	 * 
 	 * @see #getAstNode(IStrategoTerm)  To retrieve the AST node associated with the resulting term.
 	 */
 	public IStrategoTerm invokeSilent(String function, IStrategoAstNode node) {
 		return invokeSilent(function, node.getResource(), makeInputTerm(node, true));
 	}
 	
 	/**
 	 * Invoke a Stratego function with a specific term its input,
 	 * given a particular working directory.
 	 * Logs and swallows all exceptions.
 	 */
 	public IStrategoTerm invokeSilent(String function, IResource resource, IStrategoTerm input) {
 		assert Thread.holdsLock(getSyncRoot());
 		IStrategoTerm result = null;
 		
 		try {
 			result = invoke(function, input, resource);
 		} catch (InterpreterExit e) {
 			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
 			messages.clearMarkers(resource);
 			messages.addMarkerFirstLine(resource, "Analysis failed (see error log)", IMarker.SEVERITY_ERROR);
 			messages.commitAllChanges();
 			Environment.logException("Runtime exited when evaluating strategy " + function, e);
 		} catch (UndefinedStrategyException e) {
 			// Note that this condition may also be reached when the semantic service hasn't been loaded yet
 			runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println("Internal error: strategy does not exist: " + function);
 			Environment.logException("Strategy does not exist: " + function, e);
 		} catch (InterpreterException e) {
 			runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println("Internal error evaluating " + function + " (see error log)");
 			Environment.logException("Internal error evaluating strategy " + function, e);
 			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
 		} catch (RuntimeException e) {
 			runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println("Internal error evaluating " + function + " (see error log)");
 			Environment.logException("Internal error evaluating strategy " + function, e);
 			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
 		} catch (Error e) { // e.g. NoClassDefFoundError due to bad/missing stratego jar
 			runtime.getIOAgent().getOutputStream(IOAgent.CONST_STDERR).println("Internal error evaluating " + function + " (see error log)");
 			Environment.logException("Internal error evaluating strategy " + function, e);
 			if (descriptor.isDynamicallyLoaded()) StrategoConsole.activateConsole();
 		}
 		
 		return result;
 	}
 
 	public IAst getAstNode(IStrategoTerm term) {
 		if (term == null) return null;
 			
 		if (term instanceof WrappedAstNode) {
 			return ((WrappedAstNode) term).getNode();
 		} else {
 			Environment.logException("Resolved reference is not associated with an AST node " + runtime.current());
 			return null;
 		}
 	}
 	
 	private void initRuntimePath(IPath workingDir) {
 		assert Thread.holdsLock(getSyncRoot());
 		
 		try {
 			runtime.getIOAgent().setWorkingDir(workingDir.toOSString());
 			((EditorIOAgent) runtime.getIOAgent()).setDescriptor(descriptor);
 		} catch (IOException e) {
 			Environment.logException("Could not set Stratego working directory", e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private static IStrategoAstNode getRoot(IStrategoAstNode node) {
 		while (node.getParent() != null)
 			node = node.getParent();
 		return node;
 	}
 	
 	public HybridInterpreter getRuntime() {
 		assert Thread.holdsLock(getSyncRoot());
 		
 		return runtime;
 	}
 
 	public void prepareForReinitialize() {
 		// Do nothing
 	}
 
 	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
 		synchronized (getSyncRoot()) {
 			cachedRuntimes.remove(descriptor);
 			runtime = null;
 			descriptor = newDescriptor;
 		}
 	}
 
 }
