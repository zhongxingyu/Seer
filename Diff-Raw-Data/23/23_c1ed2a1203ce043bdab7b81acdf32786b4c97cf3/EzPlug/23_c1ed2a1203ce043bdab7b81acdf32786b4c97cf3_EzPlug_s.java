 package plugins.adufour.ezplug;
 
 import icy.main.Icy;
 import icy.plugin.abstract_.Plugin;
 import icy.plugin.interface_.PluginImageAnalysis;
 import icy.plugin.interface_.PluginLibrary;
 import icy.type.value.IntegerValue;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.StringWriter;
 import java.util.HashMap;
 
 import plugins.adufour.ezplug.EzMessage.MessageType;
 import plugins.adufour.ezplug.EzMessage.OutputType;
 
 /**
  * Main component of the EzPlug framework. EzPlug sits as an additional layer of the
  * {@link icy.plugin.abstract_.Plugin} class hierarchy, providing additional features to simplify
  * the development of plug-ins for ICY by greatly simplifying two redundant and tedious processes:
  * a) designing intuitive and homogeneous graphical interfaces; b) saving/loading parameters to/from
  * disk in a standardized way (e.g. XML). The EzPlug class implements
  * {@link icy.plugin.interface_.PluginImageAnalysis} in order to launch via the {@link #compute()}
  * method as any other plug-in using a graphical interface. <br>
  * <br>
  * The plug-in developer may add user parameters very simply via the
  * {@link #addEzComponent(EzComponent) addEzComponent} method. A great variety of graphical
  * components are already available and tailored to all major types of data to handle. For instance,
  * a boolean flag parameter is bound to a check box, a numerical parameter is bound to a spinner
  * component with adjustable min-max-step values and automatically handles mouse-wheel events to
  * scroll through the values. See the {@link plugins.adufour.ezplug.EzVar} class hierarchy for a
  * larger overview.<br>
  * Other graphical components such as labels, buttons and groups of components are also available,
  * allowing to build sleek and homogeneous interfaces very fast with zero knowledge in GUI
  * programming.<br>
  * Finally, each EzPlug comes with a top logo panel with the plug-in name and an action panel in the
  * bottom, from where the user may start or stop the execution process, but also load or save the
  * user parameters on disk via XML files. Some of these features are optional and simply require to
  * implement additional interfaces to benefit from them.<br>
  * <br>
  * EzPlugs must implement the three following abstract methods:<br>
  * <ul>
  * <li>The {@link #initialize()} method is used to create the user interface (parameters, buttons,
  * groups etc..)
  * <li>The {@link #execute()} method holds the main execution code of the plug-in, and will be
  * called when the "Run" button is clicked.
  * <li>The {@link #clean()} method should be used to clean "sensitive" structures of the plug-in
  * (e.g. sequence painters, I/O streams, etc.), in order to free memory properly and/or avoid
  * polluting the display (especially when the plug-in window is closed by the user).
  * </ul>
  * See below for a sample code and an illustration of what EzPlug can do.
  * 
  * <table>
  * <tr>
  * <td>
  * Here is a sample code on the left, and the resulting interface on the right:</td>
  * </tr>
  * <tr>
  * <td>
  * 
  * <pre>
  * // Sample EzPlug that replicates a sequence a number of times
  * public class ReplicateSequence extends {@link plugins.adufour.ezplug.EzPlug}
  * {
  *    {@link plugins.adufour.ezplug.EzVarSequence} input;
  *    {@link plugins.adufour.ezplug.EzVarInteger} nbCopies;
  *  
  *    // initialize plug-in and declare variables
  *    public void {@link #initialize()}
  *    {
  *       // create a variable of type integer, bounded between 1 and 10 (step 2)
  *       nbCopies = new {@link plugins.adufour.ezplug.EzVarInteger}(&quot;Number of replicates&quot;, 1, 10, 2);
  *       
  *       // create a variable of type {@link icy.sequence.Sequence}
  *       input = new {@link plugins.adufour.ezplug.EzVarSequence}(&quot;Sequence to copy&quot;);
  *       
  *       // add the variables to the interface
  *       // NOTE: variables are added to the interface in an ordered manner...
  *       {@link #addEzComponent(EzComponent) addEzComponent}(input);
  *       {@link #addEzComponent(EzComponent) addEzComponent}(nbCopies);
  *       // ...and this order may differ from the order of creation
  *    }
  * 
  *    // main plug-in code
  *    public void {@link #execute()}
  *    {
  *       {@link icy.sequence.Sequence} sequence = input.{@link plugins.adufour.ezplug.EzVar#getValue() getValue()};
  *       
  *       for(int i=1; i&lt;=nbCopies.{@link plugins.adufour.ezplug.EzVar#getValue() getValue()}; i++)
  *       {
  *          {@link icy.sequence.Sequence} copy = sequence.getCopy();
  *          copy.setName(copy.getName() + &quot;#&quot; + i);
  *          super.addSequence(copy);
  *       }
  *    }
  * 
  *    // structure cleaning
  *    public void {@link #clean()}
  *    {
  *       // nothing special to clean here
  *    }
  * }
  * </pre>
  * 
  * </td>
  * <td>
  * <img src="doc-files/SamplePlugin.png"/></td>
  * </tr>
  * 
  * 
  * @see plugins.adufour.ezplug.EzGUI
  * @author Alexandre Dufour
  * @version 2011-01-17: added collapse support for EzGroup components<br>
  *          2011-01-14: graphical is now thread-safe and compatible for non-GUI use<br>
  *          2010-12-20: renamed run() to execute() / enabled threading & off-line mode<br>
  *          2010-11-19: Golden master revision before release (major code refactoring)<br>
  *          2010-11-18: Removed showResult() / added clean() method<br>
  *          2010-08-11: Parameter IO support (via XML files)<br>
  *          2010-07-22: major revision. Many more variables, hide/show support, nicer GUI<br>
  *          2010-04-22: code runs in a separate thread to de-freeze the interface<br>
  *          2010-01-20: EzPlug meets ICY<br>
  *          2009-01-01: major revision. New Variable implementation<br>
  *          2008-08-04: simplified and improved variable declaration<br>
  *          2008-07-20: adjusted graphical interface layout<br>
  *          2008-05-12: full input/output parameter support (box programming)<br>
  *          2008-04-20: new Variable implementation<br>
  *          2008-04-06: first implementation
  */
 public abstract class EzPlug extends Plugin implements PluginLibrary, PluginImageAnalysis, Runnable
 {
 	public static final String				EZPLUG_MAINTAINERS	= "Alexandre Dufour (adufour@pasteur.fr)";
 	
 	/**
 	 * Number of active instances of this plugin
 	 */
 	private static IntegerValue				nbInstances			= new IntegerValue(0);
 	
 	private EzGUI							ezgui;
 	
 	private final HashMap<String, EzVar<?>>	ezVars;
 	
 	private boolean							timeTrial			= false;
 	
 	private long							startTime;
 	
 	protected EzPlug()
 	{
 		ezVars = new HashMap<String, EzVar<?>>();
 		
 		synchronized (nbInstances)
 		{
 			nbInstances.setValue(nbInstances.getValue() + 1);
 		}
 	}
 	
 	/**
 	 * Gets the name of this EzPlug (defaults to the class name).
 	 * 
 	 * @return the name of this EzPlug
 	 */
 	public String getName()
 	{
 		return getDescriptor().getName();
 	}
 	
 	/**
 	 * Gets the graphical interface attached to this EzPlug. This interface gives access to specific
 	 * methods related to user interaction (e.g. progress bars, highlighting, etc.).
 	 * 
 	 * @return the graphical interface of the EzPlug, or null if the EzPlug has been created via
 	 *         code without generating the interface
 	 */
 	public EzGUI getUI()
 	{
 		return ezgui;
 	}
 	
 	/**
 	 * This method lets the developer initialize the user interface of this EzPlug by adding
 	 * variables and other EzComponent objects via the {@link #addEzComponent(EzComponent)} method
 	 * 
 	 * @see plugins.adufour.ezplug.EzVar
 	 * @see plugins.adufour.ezplug.EzComponent
 	 */
 	protected abstract void initialize();
 	
 	protected void addComponent(Component component)
 	{
 		if (component == null)
 		{
 			// the component was not initialized inside the plug-in code
 			throw new EzException("Error in plugin \"" + getName() + "\": null graphical component", false);
 		}
 		
 		EzGUI g = getUI();
 		if (g != null)
 			g.addComponent(component);
 	}
 	
 	/**
 	 * Adds a graphical component to the interface. Components are graphically ordered on the
 	 * interface panel in top-down fashion in the same order as they are added.
 	 * 
 	 * @param component
 	 *            the component to add
 	 * @see plugins.adufour.ezplug.EzVar
 	 * @see plugins.adufour.ezplug.EzButton
 	 * @see plugins.adufour.ezplug.EzGroup
 	 */
 	protected void addEzComponent(EzComponent component)
 	{
 		if (component == null)
 		{
 			// the component was not initialized inside the plug-in code
 			throw new EzException("Error in plugin \"" + getName() + "\": a plugin variable was not initialized", false);
 		}
 		
 		EzGUI g = getUI();
 		if (g != null)
 			g.addEzComponent(component, true);
 	}
 	
 	void registerVariable(EzVar<?> var)
 	{
 		String varID = var.getID();
 		if (ezVars.containsKey(varID))
 			throw new IllegalArgumentException("Variable " + varID + " already exists");
 		
 		ezVars.put(varID, var);
 	}
 	
 	/**
 	 * Saves the EzPlug user parameters into the specified XML file
 	 * 
 	 * @param file
 	 * @see plugins.adufour.ezplug.EzParameterIO
 	 */
 	public void saveParameters(File file)
 	{
 		EzParameterIO.save(ezVars, file);
 	}
 	
 	/**
 	 * Saves the EzPlug user parameters into the specified XML file
 	 * 
 	 * @param file
 	 * @see plugins.adufour.ezplug.EzParameterIO
 	 */
 	public void loadParameters(File file)
 	{
		EzParameterIO.load(file, ezVars);
 	}
 	
 	@Override
 	public synchronized void run()
 	{
 		try
 		{
 			if (ezgui != null)
 				ezgui.setRunningState(true);
 			
 			startTime = System.nanoTime();
 			
 			execute();
 			
 			if (timeTrial)
 				System.out.println(getName() + " executed in " + (System.nanoTime() - startTime) / 1000000 + " ms");
 		}
 		catch (EzException e)
 		{
 			EzMessage.message(e.getMessage(), MessageType.WARNING, OutputType.DIALOG);
 			if (!e.catchException)
 				throw e;
 		}
 		finally
 		{
 			if (ezgui != null)
 				ezgui.setRunningState(false);
 		}
 	}
 	
 	/**
 	 * Sets whether the execution time of this EzPlug should be displayed on the console
 	 * 
 	 * @param displayRunningTime
 	 */
 	public void setTimeDisplay(boolean displayRunningTime)
 	{
 		this.timeTrial = displayRunningTime;
 	}
 	
 	/**
 	 * Gets the starting execution time of this EzPlug (in nanoseconds). This method can be used in
 	 * conjunction with the {@link System#nanoTime()} method to measure elapsed time during the
 	 * execution process
 	 * 
 	 * @return the starting execution time in nanoseconds (obtained from the
 	 *         {@link System#nanoTime()} method)
 	 */
 	public long getStartTime()
 	{
 		return startTime;
 	}
 	
 	/**
 	 * @return the number of active (not-destroyed) instances of this plugin
 	 */
 	public static int getNbInstances()
 	{
 		return nbInstances.getValue();
 	}
 	
 	/**
 	 * Main method containing the core execution code of this EzPlug. This method is launched in a
 	 * separate thread only if it was called from the user interface (via the run button)
 	 */
 	protected abstract void execute();
 	
 	/**
 	 * Clean method called whenever the user interface (or ICY) is closed, in order to clean
 	 * internal and user-defined structures properly.
 	 */
 	void cleanFromUI()
 	{
 		// clean user-specific items
 		try
 		{
 			clean();
 		}
 		catch (EzException eze)
 		{
 			// do not display anything as the interface is being closed
 			if (!eze.catchException)
 				throw eze;
 		}
 		
 		// clear all variables
 		
 		for (EzVar<?> var : ezVars.values())
 		{
 			// remove any listeners we know about
 			var.removeAllVarChangeListeners();
 			
 			var.setUI(null);
 		}
 		
 		ezVars.clear();
 		
 		ezgui = null;
 		
 		synchronized (nbInstances)
 		{
 			nbInstances.setValue(nbInstances.getValue() - 1);
 		}
 	}
 	
 	/**
 	 * Cleans user-defined structures before the window is closed.<br>
 	 * This method should be used if this EzPlug has references to some structures which should be
 	 * cleaned properly before closing the plug window (e.g. painters on a sequence, I/O streams,
 	 * etc.)
 	 */
 	public abstract void clean();
 	
 	/**
 	 * Generates an EzPlug code fragment that is ready to use and compile
 	 * 
 	 * @param className
 	 *            the name of the new class
 	 */
 	public static String generateEzPlugCodeFragment(String className)
 	{
 		StringWriter sw = new StringWriter();
 		
 		sw.write("import " + EzPlug.class.getPackage().getName() + ".*;\n\n");
 		sw.write("public class " + className + " extends " + EzPlug.class.getName() + "\n{\n");
 		
 		for (java.lang.reflect.Method m : EzPlug.class.getDeclaredMethods())
 		{
 			if (java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
 			{
 				Class<?> returnType = m.getReturnType();
 				sw.write("  public " + (returnType == null ? "void" : returnType.getName()) + " ");
 				
 				sw.write(m.getName() + "(");
 				
 				Class<?>[] params = m.getParameterTypes();
 				if (params.length > 0)
 				{
 					int cpt = 1;
 					
 					sw.write(params[0].getName() + " arg" + cpt++);
 					
 					for (int i = 1; i < params.length; i++)
 						sw.write(", " + params[i].getName() + " arg" + cpt++);
 				}
 				
 				sw.write(")\n  {\n");
 				sw.write("  // TODO: write your code here\n");
 				sw.write("  }\n\n");
 			}
 		}
 		
 		sw.write('}');
 		
 		return sw.toString();
 	}
 	
 	/**
 	 * Entry point of this EzPlug, which creates the user interface and displays it on the main
 	 * desktop pane. This method can be called either via ICY's main menu or directly via code.
 	 */
 	@Override
 	public final void compute()
 	{
 		try
 		{
 			// generate the user interface
 			createUI();
 			
 			// show the interface to the user
 			showUI();
 		}
 		catch (EzException e)
 		{
 			if (e.catchException)
 				EzMessage.message(e.getMessage(), MessageType.ERROR, OutputType.DIALOG);
 			else throw e;
 		}
 	}
 	
 	/**
 	 * Generates the user interface of this EzPlug. Note that the window is not shown on screen
 	 * (this can be done by calling the {@link #showUI()} method.
 	 */
 	public void createUI()
 	{
 		// generate the main interface
 		ezgui = new EzGUI(this);
 		
 		// incorporate user-defined parameters
 		initialize();
 		
 		// pack the frame
 		ezgui.repack(true);
 		
 		// addIcyFrame(ezgui);
 		Icy.getMainInterface().addToDesktopPane(ezgui);
 		
 		// Pack the window a second time after adding to the desktop pane,
 		// in order to inherit correct UI properties (borders, layouts, etc.)
 		// note: parameters need not be updated here
 		ezgui.repack(false);
 		
 		// this line is necessary since addIcyFrame forces visibility to true...
 		ezgui.setVisible(false);
 	}
 	
 	/**
 	 * Displays the user interface on screen. If it didn't exist, the interface is first created
 	 */
 	public void showUI()
 	{
 		if (ezgui == null)
 			createUI();
 		
 		ezgui.setVisible(true);
 	}
 	
 	/**
 	 * Hides the user interface (without destroying it)
 	 */
 	public void hideUI()
 	{
 		if (ezgui == null)
 			return;
 		
 		ezgui.setVisible(false);
 	}
 }
