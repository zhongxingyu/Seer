 package org.strategoxt.imp.debug.core.str.launching;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchListener;
 import org.eclipse.debug.core.ILaunchManager;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.strategoxt.HybridInterpreter;
 import org.strategoxt.IncompatibleJarException;
 import org.strategoxt.NoInteropRegistererJarException;
 import org.strategoxt.lang.MissingStrategyException;
 import org.strategoxt.lang.StrategoErrorExit;
 import org.strategoxt.lang.StrategoException;
 import org.strategoxt.lang.StrategoExit;
 
 /**
  * 
  * This DebuggableHybridInterpreter extends the HybridInterpreter so that it can provide debugging support.
  * It will execute the stratego code in a separate JVM.
  * 
  * DebuggableHybridInterpreter does not extend HybridInterpreterDebugRuntime, because we have full control over the HybridInterpreter here.
  * 
  * Use the HybridInterpreterDebugRuntime when it should be controllable from another JVM.
  * @author rlindeman
  *
  */
 public class DebuggableHybridInterpreter extends HybridInterpreter implements ILaunchListener {
 
 	/**
 	 * If true calling invoke will launch a debug session.
 	 * If false calling invoke will just return the result (no debug JVM is started)
 	 */
 	private boolean isDebugLaunchEnabled = false;
 	
 	private String projectpath = null;
 	
 	/**
 	 * Keep track of the number of simultaneous launches.
 	 */
 	public static int counter = 0;
 	
 	
 	/**
 	 * Creates a new interpreter
 	 * @param termFactory
 	 */
 	public DebuggableHybridInterpreter(ITermFactory termFactory) {
 		super(termFactory);
 		initLaunchListener();
 	}
 
 	
 	/**
 	 * Creates an interpreter that bases its definition scope on an existing instance.
 	 * 
 	 * TODO: For now just ignore this because the HybridInterpreter will be instantiated in another VM.
 	 * 
 	 * 
 	 * @param interpreter		The interpreter to base this instance on.
 	 * 
 	 * @param reuseRegistries	The names of operator registries that should not be re-created,
 	 *                       	but can be reused from the old instance.
 	 */
 	public DebuggableHybridInterpreter(HybridInterpreter interpreter, String... reuseRegistries) 
 	{
 		super(interpreter, reuseRegistries);
 		initLaunchListener();
 	}
 	
 	private void initLaunchListener()
 	{
 		// connect to the eclipse launch manager and listen to launches.
 		// so we can keep track of the number of simultaneous HybridInterpreter launches.
 		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
 	}
 	
 	private List<URL> loadJars = new ArrayList<URL>();
 	
 	@Override
 	public void loadJars(ClassLoader parentClassLoader, URL... jars)
 	throws SecurityException, NoInteropRegistererJarException, IncompatibleJarException, IOException {
 
 		// save the locations of the jar, so we can load the in the other VM
 		for(URL jarURL : jars)
 		{
 			loadJars.add(jarURL);
 		}
 
 		super.loadJars(parentClassLoader, jars);
 	}
 	
 	/**
 	 * Returns a list of strings with paths to jars that are dynamically loaded by the HybridInterpreter.
 	 * @return
 	 */
 	public List<String> getLoadJarsAsStringList()
 	{
 		List<String> list = new ArrayList<String>();
 		for(URL jarURL : this.loadJars)
 		{
 			list.add(jarURL.getPath());
 		}
 		return list;
 	}
 	
 	public List<IPath> getLoadJarsAsIPathList()
 	{
 		List<IPath> list = new ArrayList<IPath>();
 		for(URL jarURL : this.loadJars)
 		{
 			list.add(new Path(jarURL.getFile()));
 		}
 		return list;
 	}
 	
 	/**
 	 * Invokes a compiled or interpreted strategy bound to this instance.
 	 * 
 	 * Wraps any StrategoException into checked InterpreterException exceptions.
 	 */
 	@Override
 	public boolean invoke(String name)
 			throws InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
 		
 		try {
 			if (this.isDebugLaunchEnabled())
 			{
 				// TODO: limit the number of simultaneous launches...
 				tryLaunch(name);
 			}
 			
 			// TODO: wait for launch to finish
 			boolean result = super.invoke(name);
 			return result;
 		} catch (StrategoErrorExit e) {
 			throw new InterpreterErrorExit(e.getMessage(), e.getTerm(), e);
         } catch (StrategoExit e) {
             throw new InterpreterExit(e.getValue(), e);
         } catch (MissingStrategyException e) {
         	throw new UndefinedStrategyException(e);
         } catch (StrategoException e) {
             throw new InterpreterException(e);
         }
 		//return false;
 	}
 	
 	/**
 	 * Tries to launch a debug HybridInterpreter session that will invoke the given strategy name.
 	 * @param name
 	 */
 	private void tryLaunch(String name)
 	{
 		// launch a JVM
 		// http://www.eclipse.org/articles/Article-Java-launch/launching-java.html
 		
 		// find HybridInterpreter launch config
 		ILaunchConfigurationWorkingCopy configWC = LaunchUtils.createHybridInterpreterLaunchConfigurationWorkingCopy();
 		if (configWC == null)
 		{
 			System.err.println("No config working copy!");
 		}
 		// set the required attributes
 		
 		// strategy name
 		// IStrategoConstants.ATTR_STRATEGO_STRATEGY_NAME
 		configWC.setAttribute(IStrategoConstants.ATTR_STRATEGO_STRATEGY_NAME, name);
 		
 		// required jars
 		// IStrategoConstants.ATTR_STRATEGO_REQUIRED_JARS
 		configWC.setAttribute(IStrategoConstants.ATTR_STRATEGO_REQUIRED_JARS, getLoadJarsAsStringList());
 		
 		// also set the path to the project, breakpoints 
 		// IStrategoConstants.ATTR_STRATEGO_PROGRAM
 		configWC.setAttribute(IStrategoConstants.ATTR_STRATEGO_PROGRAM, (String) null);
 		//org.example.lang1
 		// classpath
 		// IStrategoConstants.ATTR_STRATEGO_CLASSPATH
 		
 		// set the project path, so we can filter breakpoints limited to this project
 		configWC.setAttribute(IStrategoConstants.ATTR_PROJECT_DIRECTORY, this.getProjectpath());
 
 		// set metadata directory
 		configWC.setAttribute(IStrategoConstants.ATTR_METADATA_DIRECTORY, (String) null);
 
 		// set current term
 		IStrategoTerm term = this.current();
 		if (term != null)
 		{
 			configWC.setAttribute(IStrategoConstants.ATTR_CURRENT_TERM, term.toString());
 		}
 		
 		// and launch
 		try {
 			ILaunchConfiguration config = configWC.doSave();
 			// TODO: only launch if we have breakpoints
 			// Use the Descriptor
 			// We also need EditorIOAgent
 			//config.launch(ILaunchManager.DEBUG_MODE, null);
 			IProgressMonitor monitor = null;
 			boolean build = false;
 			boolean register = true;
 			config.launch(ILaunchManager.DEBUG_MODE, monitor, build, register);
 			//boolean build,
             //boolean register
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Points to the spoofax project this HybridInterpreter is loaded for.
 	 * @return
 	 */
 	public String getProjectpath() {
 		return projectpath;
 	}
 
 
 	public void setProjectpath(String projectpath) {
 		this.projectpath = projectpath;
 	}
 	
 	public boolean isDebugLaunchEnabled() {
 		return isDebugLaunchEnabled;
 	}
 	
 	public void setDebugLaunchEnabled(boolean isDebugLaunchEnabled) {
 		this.isDebugLaunchEnabled = isDebugLaunchEnabled;
 	}
 
 	// ILaunchListener interface
 	public void launchRemoved(ILaunch launch) {
 		// TODO Auto-generated method stub
 		// System.out.println("Launch removed");
 	}
 	
 	public void launchAdded(ILaunch launch) {
 		// TODO Auto-generated method stub
 		// System.out.println("Launch added");
 	}
 	
 	public void launchChanged(ILaunch launch) {
 		// TODO Auto-generated method stub
 		// System.out.println("Launch changed");
 	}
 
 	@Override
 	public void uninit() {
 		super.uninit();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
 	}
 }
