 /**
  * Copyright (C) 2013 Schneider-Electric
  *
  * This file is part of "Mind Compiler" is free software: you can redistribute 
  * it and/or modify it under the terms of the GNU Lesser General Public License 
  * as published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact: mind@ow2.org
  *
  * Authors: Stephane Seyvoz
  * Contributors: 
  */
 
 package org.ow2.mind.unit;
 
 import static org.ow2.mind.adl.membrane.ControllerInterfaceDecorationHelper.setReferencedInterface;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.CompilerError;
 import org.objectweb.fractal.adl.Definition;
 import org.objectweb.fractal.adl.Loader;
 import org.objectweb.fractal.adl.Node;
 import org.objectweb.fractal.adl.NodeFactory;
 import org.objectweb.fractal.adl.error.Error;
 import org.objectweb.fractal.adl.error.GenericErrors;
 import org.objectweb.fractal.adl.interfaces.Interface;
 import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
 import org.objectweb.fractal.adl.merger.MergeException;
 import org.objectweb.fractal.adl.merger.NodeMerger;
 import org.objectweb.fractal.adl.types.TypeInterface;
 import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
 import org.objectweb.fractal.adl.util.FractalADLLogManager;
 import org.ow2.mind.ADLCompiler;
 import org.ow2.mind.ADLCompiler.CompilationStage;
 import org.ow2.mind.ForceRegenContextHelper;
 import org.ow2.mind.adl.annotation.predefined.Singleton;
 import org.ow2.mind.adl.ast.ASTHelper;
 import org.ow2.mind.adl.ast.Binding;
 import org.ow2.mind.adl.ast.BindingContainer;
 import org.ow2.mind.adl.ast.Component;
 import org.ow2.mind.adl.ast.ComponentContainer;
 import org.ow2.mind.adl.ast.DefinitionReference;
 import org.ow2.mind.adl.ast.ImplementationContainer;
 import org.ow2.mind.adl.ast.MindInterface;
 import org.ow2.mind.adl.ast.Source;
 import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
 import org.ow2.mind.adl.membrane.ControllerInterfaceDecorationHelper;
 import org.ow2.mind.adl.membrane.ast.Controller;
 import org.ow2.mind.adl.membrane.ast.ControllerContainer;
 import org.ow2.mind.adl.membrane.ast.ControllerInterface;
 import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;
 import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;
 import org.ow2.mind.annotation.AnnotationHelper;
 import org.ow2.mind.cli.CmdFlag;
 import org.ow2.mind.cli.CmdOption;
 import org.ow2.mind.cli.CmdOptionBooleanEvaluator;
 import org.ow2.mind.cli.CommandLine;
 import org.ow2.mind.cli.CommandLineOptionExtensionHelper;
 import org.ow2.mind.cli.CommandOptionHandler;
 import org.ow2.mind.cli.InvalidCommandLineException;
 import org.ow2.mind.cli.Options;
 import org.ow2.mind.cli.OutPathOptionHandler;
 import org.ow2.mind.cli.PrintStackTraceOptionHandler;
 import org.ow2.mind.cli.SrcPathOptionHandler;
 import org.ow2.mind.error.ErrorManager;
 import org.ow2.mind.idl.IDLLoader;
 import org.ow2.mind.idl.ast.IDL;
 import org.ow2.mind.idl.ast.InterfaceDefinition;
 import org.ow2.mind.idl.ast.Method;
 import org.ow2.mind.idl.ast.Parameter;
 import org.ow2.mind.idl.ast.PrimitiveType;
 import org.ow2.mind.idl.ast.Type;
 import org.ow2.mind.inject.GuiceModuleExtensionHelper;
 import org.ow2.mind.io.OutputFileLocator;
 import org.ow2.mind.plugin.PluginLoaderModule;
 import org.ow2.mind.plugin.PluginManager;
 import org.ow2.mind.unit.annotations.Cleanup;
 import org.ow2.mind.unit.annotations.Init;
 import org.ow2.mind.unit.annotations.Test;
 import org.ow2.mind.unit.annotations.TestSuite;
 import org.ow2.mind.unit.cli.CUnitModeOptionHandler;
 import org.ow2.mind.unit.model.Suite;
 import org.ow2.mind.unit.model.TestCase;
 import org.ow2.mind.unit.model.TestInfo;
 import org.ow2.mind.unit.st.BasicSuiteSourceGenerator;
 import org.ow2.mind.unit.st.SuiteSourceGenerator;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 //import org.ow2.mind.adl.annotations.DumpASTAnnotationProcessor;
 
 public class Launcher {
 
 	protected static final String PROGRAM_NAME_PROPERTY_NAME = "mindunit.launcher.name";
 	protected static final String ID_PREFIX                  = "org.ow2.mind.unit.test.";
 
 	protected final CmdFlag 		helpOpt					= new CmdFlag(
 			ID_PREFIX
 			+ "Help",
 			"h", "help",
 			"Print this help and exit");
 
 	protected final CmdFlag 		versionOpt				= new CmdFlag(
 			ID_PREFIX
 			+ "Version",
 			"v", "version",
 			"Print version number and exit");
 
 	protected final CmdFlag 		extensionPointsListOpt	= new CmdFlag(
 			ID_PREFIX
 			+ "PrintExtensionPoints",
 			null,
 			"extension-points",
 			"Print the list of available extension points and exit.");
 
 	protected final Options 		options					= new Options();
 
 	protected static Logger			logger					= FractalADLLogManager.getLogger("mindunit");
 
 	// Used for ADL files listing through directory exploration
 	protected List<File>			validTestFoldersList 	= new ArrayList<File>();
 	// Used by source-path configuration, as it uses a URLClassLoader
 	protected List<URL>				urlList 				= new ArrayList<URL>();
 
 	protected List<String>			testFolderADLList 		= new ArrayList<String>();
 
 	protected List<Definition>		validTestSuitesDefsList	= new ArrayList<Definition>();
 	protected List<Suite>			testSuites 				= new ArrayList<Suite>();
 
 	protected Map<Object, Object> 	compilerContext			= new HashMap<Object, Object>();
 
 	protected final String 			adlName 				= "org.ow2.mind.unit.MindUnitApplication";
 
 	// compiler components :
 	protected Injector 				injector;
 
 	protected ErrorManager 			errorManager;
 	protected ADLCompiler 			adlCompiler;
 
 	protected NodeFactory			nodeFactoryItf;
 	protected SuiteSourceGenerator 	suiteCSrcGenerator;
 	protected Loader 				loaderItf;
 	protected IDLLoader 			idlLoaderItf;
 	protected OutputFileLocator 	outputFileLocatorItf;
 	protected NodeMerger 			nodeMergerItf;
 
 	// default mode
 	private CUnitMode 				cunit_mode 				= CUnitMode.AUTOMATED;
 
 	// build configuration
 	String exeName 					= null;
 	String rootAdlName 				= null;
 
 	protected void init(final String... args) throws InvalidCommandLineException {
 
 		List<String> 	testFoldersList;
 
 		if (logger.isLoggable(Level.CONFIG)) {
 			for (final String arg : args) {
 				logger.config("[arg] " + arg);
 			}
 		}
 
 		/****** Initialization of the PluginManager Component *******/
 
 		final Injector bootStrapPluginManagerInjector = getBootstrapInjector();
 		final PluginManager pluginManager = bootStrapPluginManagerInjector
 				.getInstance(PluginManager.class);
 
 		addOptions(pluginManager);
 
 		// parse arguments to a CommandLine.
 		final CommandLine cmdLine = CommandLine.parseArgs(options, false, args);
 		checkExclusiveGroups(pluginManager, cmdLine);
 		compilerContext
 		.put(CmdOptionBooleanEvaluator.CMD_LINE_CONTEXT_KEY, cmdLine);
 		invokeOptionHandlers(pluginManager, cmdLine, compilerContext);
 
 		// If help is asked, print it and exit.
 		if (helpOpt.isPresent(cmdLine)) {
 			printHelp(System.out);
 			System.exit(0);
 		}
 
 		// If version is asked, print it and exit.
 		if (versionOpt.isPresent(cmdLine)) {
 			printVersion(System.out);
 			System.exit(0);
 		}
 
 		// If the extension points list is asked, print it and exit.
 		if (extensionPointsListOpt.isPresent(cmdLine)) {
 			printExtensionPoints(pluginManager, System.out);
 			System.exit(0);
 		}
 
 		// get the test folders list
 		testFoldersList = cmdLine.getArguments();
 
 		if (!testFoldersList.isEmpty()) {
 			checkAndStoreValidTargetFolders(testFoldersList);
 		} else {
 			logger.severe("You must specify a target directory.");
 			printHelp(System.out);
 			System.exit(1);
 		}
 
 		// also add the URL of the generated files folder (for the mindUnitSuite.c file to be accessible)
 		createAndAddGeneratedFilesFolderToPathURLs();
 
 		// add the computed test folders to the src-path
 		addTestFoldersToPath();
 
 		// initialize compiler
 		initInjector(pluginManager, compilerContext);
 
 		initCompiler();
 
 		/*
 		 *  We need to force regeneration (which leads to lose incremental compilation advantages... :( )
 		 *  In order for:
 		 *  - The Container not to be reloaded with already existing @TestSuite-s, leading to duplicates
 		 *  (we don't check for consistency yet)
 		 *  - The @TestSuites not to be reloaded with already added exported test interfaces (with their internal
 		 *  dual interface, membrane code with stubs when needed, and internal binding...)
 		 *  (as previously we want to avoid duplicates since we only ADD elements (no diff/remove), and our check
 		 *  for no outer interface on @TestSuite-s would fail)
 		 *  - The MindUnitSuiteDefinition has to be regenerated according to @TestSuite-s and @Test-s,
 		 *  as the C implementation and client interfaces may change according to what we find
 		 *  - The Container also has already been completed with according bindings at the previous compile time
 		 *  and we again do not check for consistency
 		 *  
 		 *  TODO: consider incremental compilation solutions ? What we only want to reload every time are 
 		 *  the components containing the user content and not the user files (only when there is change there...).
 		 *  How to discriminate such behavior ?
 		 */
 		ForceRegenContextHelper.setForceRegen(compilerContext, true);
 
 	}
 
 	private void constructTestApplication() {
 
 		// Build list of ADL files
 		listTestFolderADLs();
 
 		/*
 		 * Then parse/load them (but stay at CHECK_ADL stage).
 		 * We obtain a list of Definition-s
 		 */
 		filterValidTestSuites();
 
 		if (validTestSuitesDefsList.isEmpty()) {
 			logger.info("No @TestSuite found: exit");
 			System.exit(0);
 		}
 
 		/*
 		 * Get the test container 
 		 */
 		String testContainerName = "org.ow2.mind.unit.MindUnitContainer";
 
 		Definition containerDef = getContainerFromName(testContainerName);
 
 		/*
 		 * Add all test cases to the test container as sub-component instances
 		 */
 		logger.info("Adding TestSuites to the MindUnit container");
 
 		// create a component instance for each @TestSuite definition and add it to the container
 		addTestSuitesToContainer(containerDef);
 
 		/*
 		 * Load the application
 		 */
 		logger.info("Preparing the host application");
 
 		configureApplicationTemplateFromCUnitMode();
 
 		/*
 		 * Write the C implementation of the "Suite" component with the good
 		 * struct referencing all the TestEntries ("run" method) previously found.
 		 * Needs to write an according StringTemplate. If we made all TestCases as
 		 * @Singleton, we could reference CALLs (no "this") and create according bindings ?
 		 * A bit heavy but more architecture-friendly. And may simplify function names calculations.
 		 */
 		logger.info("@TestSuites introspection - Find @Tests - Export test interfaces - Create internal bindings in the @TestSuites");
 
 		// prepare to store information needed to create client interfaces on our Suite component
 		Map<String, String> suiteClientItfsNameSignature = new LinkedHashMap<String, String>();
 
 		// prepare to store bindings to be created from suite to @TestSuite-s interfaces in the container
 		// note: only targets will be initialized and the source component name will be configured at addBinding time
 		List<Binding> containerBindings = new ArrayList<Binding>();
 
 		// build the Suite list
 		for (Definition currDef : validTestSuitesDefsList)
 			// note: at each iteration the lists are growing
 			// note2: the suite definition will be modified to export interfaces containing @Test-s
 			introspectAndPrepareTestSuite(currDef, suiteClientItfsNameSignature, containerBindings);
 
 
 		logger.info("Generating Suite source implementation");
 
 		try {
 			// injected (see Module and initInjector)
 			suiteCSrcGenerator.visit(testSuites, compilerContext);
 		} catch (ADLException e1) {
 			logger.severe("Error while generating the C source implementation of the Suite ! Cause:");
 			e1.printStackTrace();
 		}
 
 		logger.info("Creating the Suite component");
 
 		/*
 		 * Create a primitive with the source file as implementation
 		 */
 		Definition mindUnitSuiteDefinition = createSuiteDefinition();
 
 		logger.info("Adding unit-gen/mindUnitSuite.c as source");
 
 		addCSourceToDefinition(mindUnitSuiteDefinition, BasicSuiteSourceGenerator.getSuiteFileName());
 
 		/*
 		 * Create the good client interfaces matching the test ones (need to store the latter !)
 		 * And create bindings from the suite client interfaces to the @TestSuite-s
 		 */
 
 		logger.info("Creating client interfaces on the Suite component");
 
 		instantiateNeededClientInterfaces(mindUnitSuiteDefinition, suiteClientItfsNameSignature);
 
 		logger.info("Adding the Suite to the test container");
 
 		/*
 		 * Then create a "Suite" component instance and add it to the test container
 		 */
 
 		String mindUnitSuiteInstanceName = addSuiteToContainer(containerDef, mindUnitSuiteDefinition);
 
 		logger.info("Creating bindings in the container, from generated Suite component interfaces to @TestSuite-s exported test interfaces");
 
 		configureAndAddBindingsFromSuiteToTestSuitesInContainer(containerDef, containerBindings, mindUnitSuiteInstanceName);
 
 
 		// Debug
 		//DumpASTAnnotationProcessor.showDefinitionContent(containerDef);
 
 		/*
 		 * Then configure target exe name
 		 */
 		String simpleOutput = "mindUnitOutput";
 		switch (cunit_mode) {
 		case CONSOLE:
 			exeName = "console_" + simpleOutput;
 			break;
 		case GCOV:
 			exeName = "gcov_" + simpleOutput;
 			break;
 		default:
 		case AUTOMATED:
 			exeName = "automated_" + simpleOutput;
 			break;
 		}
 
 		// The "compile" method is naturally following in the main when we return...
 	}
 
 	private void configureAndAddBindingsFromSuiteToTestSuitesInContainer(
 			Definition containerDef, List<Binding> containerBindings,
 			String mindUnitSuiteInstanceName) {
 
 		assert containerDef instanceof BindingContainer;
 
 		BindingContainer containerDefAsBdgCtr = (BindingContainer) containerDef;
 		for (Binding currBinding : containerBindings) {
 			currBinding.setFromComponent(mindUnitSuiteInstanceName);
 			containerDefAsBdgCtr.addBinding(currBinding);
 		}
 
 	}
 
 	private String addSuiteToContainer(Definition containerDef,
 			Definition mindUnitSuiteDefinition) {
 
 		DefinitionReference mindUnitSuiteDefRef = ASTHelper.newDefinitionReference(nodeFactoryItf, mindUnitSuiteDefinition.getName());
 		ASTHelper.setResolvedDefinition(mindUnitSuiteDefRef, mindUnitSuiteDefinition);
 		String mindUnitSuiteInstanceName = mindUnitSuiteDefRef.getName().replace(".", "_") + "Instance";
 		Component mindUnitSuiteComp = ASTHelper.newComponent(nodeFactoryItf, mindUnitSuiteInstanceName, mindUnitSuiteDefRef);
 		mindUnitSuiteComp.setDefinitionReference(mindUnitSuiteDefRef);
 		ASTHelper.setResolvedComponentDefinition(mindUnitSuiteComp, mindUnitSuiteDefinition);
 		((ComponentContainer) containerDef).addComponent(mindUnitSuiteComp);
 
 		return mindUnitSuiteInstanceName;
 	}
 
 	/**
 	 * For each pair of "interface name - signature" create a client interface instance on the Suite.
 	 * @param mindUnitSuiteDefinition
 	 * @param suiteClientItfsNameSignature
 	 */
 	private void instantiateNeededClientInterfaces(
 			Definition mindUnitSuiteDefinition,
 			Map<String, String> suiteClientItfsNameSignature) {
 
 		// the newPrimitiveDefinitionNode enforces ImplementationContainer.class compatibility
 		InterfaceContainer mindUnitSuiteDefAsItfCtr = (InterfaceContainer) mindUnitSuiteDefinition;
 		for (String currItfInstanceName : suiteClientItfsNameSignature.keySet()) {
 			String currItfSignature = suiteClientItfsNameSignature.get(currItfInstanceName);
 
 			// we get the InterfaceDefinition from the compiler's cache (instead of creating a new map...)
 			IDL currIDL = null;
 			try {
 				currIDL = idlLoaderItf.load(currItfSignature, compilerContext);
 			} catch (ADLException e) {
 				logger.severe("Could not load " + currItfSignature + " interface ! - Exit to prevent C suite file inconsistency !");
 				System.exit(1);
 			}
 
 			MindInterface newSuiteCltItf = ASTHelper.newClientInterfaceNode(nodeFactoryItf, currItfInstanceName, currItfSignature);
 
 			assert currIDL instanceof InterfaceDefinition;
 			InterfaceDefinitionDecorationHelper.setResolvedInterfaceDefinition(newSuiteCltItf, (InterfaceDefinition) currIDL);
 
 			mindUnitSuiteDefAsItfCtr.addInterface(newSuiteCltItf);
 		}
 
 	}
 
 	/**
 	 * Create a Source node with path pointing to the generated C file and add it to the Suite definition.
 	 * @param mindUnitSuiteDefinition The Suite definition.
 	 * @param cSuiteFileName The name of the C file implementing the Suite.
 	 */
 	private void addCSourceToDefinition(Definition mindUnitSuiteDefinition,
 			String cSuiteFileName) {
 
 		ImplementationContainer mindUnitSuiteDefAsImplCtr = (ImplementationContainer) mindUnitSuiteDefinition;
 		Source mindUnitSuiteSource = ASTHelper.newSource(nodeFactoryItf);
 		mindUnitSuiteSource.setPath(cSuiteFileName);
 		mindUnitSuiteDefAsImplCtr.addSource(mindUnitSuiteSource);
 	}
 
 	/**
 	 * Create a @Singleton primitive definition: "MindUnitSuiteDefinition" 
 	 * @return The new (empty) definition
 	 */
 	private Definition createSuiteDefinition() {
 
 		Definition mindUnitSuiteDefinition = ASTHelper.newPrimitiveDefinitionNode(nodeFactoryItf, "MindUnitSuiteDefinition", (DefinitionReference[]) null);
 		// it has to be a @Singleton for our test "void func(void) { CALL(itf, meth)(); }" functions to be able to enter the Mind world (no 'mind_this')
 		// both following methods are needed since we won't trigger the Singleton Annotation Processor, and the struct definitions rely on both
 		// ASTHelper.isSingleton doesn't check the "singleton decoration" by the way but the Annotation and is used to name singleton instances.
 		ASTHelper.setSingletonDecoration(mindUnitSuiteDefinition);
 		try {
 			AnnotationHelper.addAnnotation(mindUnitSuiteDefinition, new Singleton());
 		} catch (ADLException e1) {
 			// will never happen since the exception is raised only when you try to put an annotation two times on a definition... which is not our case
 		}
 		// the newPrimitiveDefinitionNode enforces ImplementationContainer.class compatibility
 
 		return mindUnitSuiteDefinition;
 	}
 
 	/**
 	 * This method introspects the @TestSuite definition to find @Tests and edit the definition to export them.
 	 * As an optimization we also prepare content for later processing.
 	 * 
 	 * @param suiteDef The @TestSuite definition containing @Test-s. The definition will be modified to export interfaces containing @Tests and internal bindings to these interfaces will be created.
 	 * @param suiteClientItfsNameSignature A list to keep track of the exported interfaces so as to create matching client interfaces on the @Suite component.
 	 * @param containerBindings A return parameter in which are added pre-filled bindings (as exported interfaces are found here) to be completed and added to the container later.
 	 */
 	private void introspectAndPrepareTestSuite(Definition suiteDef, Map<String, String> suiteClientItfsNameSignature, List<Binding> containerBindings) {
 
 		String description = (AnnotationHelper.getAnnotation(suiteDef, TestSuite.class)).value;
 		if (description == null)
 			description = suiteDef.getName();
 
 		if (testSuiteHasExternalInterface(suiteDef))
 			return;
 
 		if (suiteDef instanceof ComponentContainer) {
 			// handle all sub-components (no recursion)
 			ComponentContainer currCompContainer = (ComponentContainer) suiteDef;
 			for (Component currComp : currCompContainer.getComponents()) {
 				Definition currCompDef = null;
 				try {
 					currCompDef = ASTHelper.getResolvedComponentDefinition(currComp, loaderItf, compilerContext);
 				} catch (ADLException e) {
 					logger.severe("Could not resolve definition of " + suiteDef.getName() + "." + currComp.getName() + " ! - skip");
 					continue;
 				}
 				if (currCompDef instanceof InterfaceContainer) {
 					// handle sub-component server interfaces, find the list of @Test-annotated methods
 					InterfaceContainer currItfContainer = (InterfaceContainer) currCompDef;
 
 					for (Interface currItf : currItfContainer.getInterfaces()) {
 						// according to test interfaces we will not only prepare the current TestSuite where needed
 						// with export interfaces, internal bindings, but also prepare the list of parent container bindings
 						// and matching client interfaces info for them to be created on the client Suite
 						Suite currSuite = introspectInterfaceAndPrepareTestSuite(suiteDef, description, currComp, currItf, suiteClientItfsNameSignature, containerBindings);
 						if (currSuite != null)
 							testSuites.add(currSuite);
 					}
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * Check if the @TestSuite component has external interfaces. Must have none.
 	 * @param currDef The TestSuite to be checked.
 	 * @return true on error
 	 */
 	private boolean testSuiteHasExternalInterface(Definition currDef) {
 
 		assert currDef instanceof InterfaceContainer;
 		InterfaceContainer currDefAsItfCtr = (InterfaceContainer) currDef;
 
 		if (currDefAsItfCtr.getInterfaces().length > 0) {
 			logger.warning("While handling @TestSuite + " + currDef.getName() + ": A test suite must not have any external interface - skipping");
 			return true;
 		} 
 
 		return false;
 	}
 
 	private Suite introspectInterfaceAndPrepareTestSuite(Definition suiteDef, String suiteDescription, Component currComp, Interface currItf,
 			Map<String, String> suiteClientItfsNameSignature,
 			List<Binding> containerBindings) {
 
 		assert suiteDef instanceof BindingContainer; // should be as a default anyway
 		BindingContainer currDefAsBdgCtr = (BindingContainer) suiteDef;
 		assert suiteDef instanceof InterfaceContainer; // should be as a default anyway
 		InterfaceContainer currDefAsItfCtr = (InterfaceContainer) suiteDef;
 
 		// as we will want to export interfaces and create internal bindings, we need
 		// to create the dual internal interface and have the definition as an InternalInterfaceContainer
 		// inspired from the CompositeInternalInterfaceLoader
 		turnToInternalInterfaceContainer(suiteDef);
 		InternalInterfaceContainer currDefAsInternalItfCtr = (InternalInterfaceContainer) suiteDef;
 
 		turnToControllerContainer(suiteDef);
 		ControllerContainer currDefAsCtrlCtr = (ControllerContainer) suiteDef;
 
 		// return
 		Suite suite = null;
 
 		// useful vars
 		String currItfInitFuncName = null;
 		String currItfInitMethName = null;
 		String currItfCleanupFuncName = null;
 		String currItfCleanupMethName = null;
 		String itfSignature = null;
 		String itfExportName = null;
 		InterfaceDefinition currItfDef = null;
 
 		String itfName = currItf.getName();
 
 		// prepare test cases list - each TestCase is the same as a CU_TestInfo row
 		List<TestCase> currItfValidTestCases = new ArrayList<TestCase>();
 
 		// should be everywhere isn't it ?
 		assert currItf instanceof TypeInterface;
 		TypeInterface currTypeItf = (TypeInterface) currItf;
 
 		// we only are concerned by server interfaces
 		if (!currTypeItf.getRole().equals(TypeInterface.SERVER_ROLE))
 			return suite;
 
 		boolean hasTests = false;
 		itfSignature = currTypeItf.getSignature();
 
 		// we need interface details
 		IDL currIDL;
 		try {
 			currIDL = idlLoaderItf.load(itfSignature, compilerContext);
 		} catch (ADLException e) {
 			logger.warning("Could not load interface definition " + itfSignature + " - skipping");
 			return suite;
 		}
 
 		assert currIDL instanceof InterfaceDefinition;
 		currItfDef = (InterfaceDefinition) currIDL;
 
 		// handle methods in the interface to find existing @Test, @Init, @Cleanup
 		for (Method currMethod : currItfDef.getMethods()) {
 
 			boolean isTest 		= AnnotationHelper.getAnnotation(currMethod, Test.class) != null;
 			boolean isInit 		= AnnotationHelper.getAnnotation(currMethod, Init.class) != null;
 			boolean isCleanup 	= AnnotationHelper.getAnnotation(currMethod, Cleanup.class) != null;
 
 			// Maybe replace the algorithm for a switch-case ?
 
 			// ^ = XOR
 			if (isTest ^ isInit ^ isCleanup) {
 
 				if (isTest) {
 
 					Test testCase = AnnotationHelper.getAnnotation(currMethod, Test.class);
 
 					String testDescription = null;
 					if (testCase.value == null)
 						testDescription = currMethod.getName();
 					else
 						testDescription = testCase.value;
 
 					// return type should be void
 					Type methodType = currMethod.getType();
 					if (!(methodType instanceof PrimitiveType
 							&& ((PrimitiveType) methodType).getName().equals("void"))) {
 						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method return type should be \"void\" - Adding to test list anyway");
 					}
 
 					// argument must be void ( = no argument)
 					Parameter[] methodParams = currMethod.getParameters();
 					if (methodParams.length > 0) {
 						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method arguments must be \"(void)\" - Skipping method");
 						continue;
 					}
 
 					// compute the relay function name we'll provide to CUnit that will CALL the test
 					// we compute complex names to avoid clashes (as a tester is not needed to be @Singleton)
 					String cRelayFuncName = "__cunit_relay_"		// prefix
 							+ suiteDef.getName().substring(suiteDef.getName().lastIndexOf(".") + 1).replace(".", "_")	// the @TestSuite simple definition name
 							+ "_" + currComp.getName()				// sub-component instance
 							+ "_" + currTypeItf.getName()			// interface instance
 							+ "_" + currMethod.getName();			// method instance
 
 					// remember which interfaces should be instantiated as clients on the Suite component
 					itfExportName = suiteDef.getName().replace(".", "_") + "_" + currComp.getName() + "_" + currTypeItf.getName();
 					suiteClientItfsNameSignature.put(
 							itfExportName,
 							itfSignature);
 
 					// create the test case
 					currItfValidTestCases.add(new TestCase(testDescription, cRelayFuncName, currMethod.getName()));
 
 					// need to know if we have to export the interface to the surrounding @TestSuite composite
 					hasTests = true;
 
 				} else if (isInit) {
 					if (currItfInitFuncName != null) {
 						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": An @Init method was already defined - Skipping");
 						continue;
 					}
 
 					// compute the relay function name we'll provide to CUnit that will CALL the test
 					// we compute complex names to avoid clashes (as a tester is not needed to be @Singleton)
 					currItfInitFuncName = "__cunit_relay_"			// prefix
 							+ suiteDef.getName().substring(suiteDef.getName().lastIndexOf(".") + 1).replace(".", "_") 	// the @TestSuite definition name
 							+ "_" + currComp.getName()				// sub-component instance
 							+ "_" + currTypeItf.getName()			// interface instance
 							+ "_" + currMethod.getName();			// method instance
 
 					// simple name for the CALL
 					currItfInitMethName = currMethod.getName();
 
					// return type should be int (according to CUnit)
 					Type methodType = currMethod.getType();
 					if (!(methodType instanceof PrimitiveType
 							&& ((PrimitiveType) methodType).getName().equals("int"))) {
						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Init method return type should be \"int\" - Adding to test list anyway");
 					}
 
 					// argument must be void ( = no argument)
 					Parameter[] methodParams = currMethod.getParameters();
 					if (methodParams.length > 0) {
						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Init method arguments must be \"(void)\" - Skipping method");
 						continue;
 					}
 
 				} else if (isCleanup) {
 					if (currItfCleanupFuncName != null) {
						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": An @Cleanup method was already defined - Skipping");
 						continue;
 					}
 					// compute the relay function name we'll provide to CUnit that will CALL the test
 					// we compute complex names to avoid clashes (as a tester is not needed to be @Singleton)
 					currItfCleanupFuncName = "__cunit_relay_"			// prefix
 							+ suiteDef.getName().substring(suiteDef.getName().lastIndexOf(".") + 1).replace(".", "_") 	// the @TestSuite definition name
 							+ "_" + currComp.getName()				// sub-component instance
 							+ "_" + currTypeItf.getName()			// interface instance
 							+ "_" + currMethod.getName();			// method instance
 
 					// simple name for the CALL
 					currItfCleanupMethName = currMethod.getName();
 
					// return type should be int (according to CUnit)
 					Type methodType = currMethod.getType();
 					if (!(methodType instanceof PrimitiveType
 							&& ((PrimitiveType) methodType).getName().equals("int"))) {
						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Cleanup method return type should be \"int\" - Adding to test list anyway");
 					}
 
 					// argument must be void ( = no argument)
 					Parameter[] methodParams = currMethod.getParameters();
 					if (methodParams.length > 0) {
						logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Cleanup method arguments must be \"(void)\" - Skipping method");
 						continue;
 					}
 				}
 			} else if (isTest || isInit || isCleanup) {
 				// if the XOR failed and there was at least one annotation it means we had 2 or more... 
 				// and we didn't want to raise an error when there was no annotation at all
 				logger.warning("@Init, @Test and @Cleanup are mutually exclusive - Please clarify " + currItfDef.getName() + "#" + currMethod.getName() + " role - Skipping method");
 				continue;
 			}
 		} // end for all methods
 
 		/*
 		 * Export interface containing @Test to the surrounding @TestSuite
 		 * And create the matching internal binding
 		 * And prepare the outer binding (in the container) from the generated Suite component to the current @TestSuite
 		 */
 		if (hasTests) {
 			MindInterface newSuiteServerItf = ASTHelper.newServerInterfaceNode(nodeFactoryItf, itfExportName, itfSignature);
 			InterfaceDefinitionDecorationHelper.setResolvedInterfaceDefinition(newSuiteServerItf, currItfDef);
 
 			//logger.info("Creating " + newSuiteServerItf.getName() + " interface instance for definition " + currDef.getName() + " with interface definition " + currItfDef.getName());
 			currDefAsItfCtr.addInterface(newSuiteServerItf);
 
 			// also create the INTERNAL interface
 			// inspired by the CompositeInternalInterfaceLoader
 			TypeInterface newSuiteServerInternalClientItf = getInternalInterface(newSuiteServerItf);
 			InterfaceDefinitionDecorationHelper.setResolvedInterfaceDefinition((TypeInterface) newSuiteServerInternalClientItf, currItfDef);
 			currDefAsInternalItfCtr.addInternalInterface(newSuiteServerInternalClientItf);
 
 
 			//-- the following is for the @TestSuite membrane (_ctrl_impl.c) to have
 			// it's "interface delegator" generated
 			ControllerInterfaceDecorationHelper.setDelegatedInterface(newSuiteServerInternalClientItf,
 					newSuiteServerItf);
 			ControllerInterfaceDecorationHelper.setDelegatedInterface(newSuiteServerItf,
 					newSuiteServerInternalClientItf);
 
 			// add controller
 			Controller ctrl = newControllerNode();
 			ControllerInterface externalCtrlItf = newControllerInterfaceNode(newSuiteServerItf.getName(), false);
 			ControllerInterface internalCtrlItf = newControllerInterfaceNode(newSuiteServerItf.getName(), true);
 			ctrl.addControllerInterface(externalCtrlItf);
 			ctrl.addControllerInterface(internalCtrlItf);
 			ctrl.addSource(newSourceNode("InterfaceDelegator"));
 			currDefAsCtrlCtr.addController(ctrl);
 
 			setReferencedInterface(externalCtrlItf, newSuiteServerItf);
 
 			// -- create internal binding
 			Binding newInternalBinding = newInternalDelegationBinding(itfExportName, currComp, itfName);
 
 			// add it to the @TestSuite composite definition
 			currDefAsBdgCtr.addBinding(newInternalBinding);
 			//logger.info("Created binding from the surrounding @TestSuite to the sub-component interface");
 
 			// -- create outer binding
 			Binding newOuterBinding = newOuterBinding(itfExportName, suiteDef);
 
 			containerBindings.add(newOuterBinding);
 		}
 
 		// build the test suite
 		if (!currItfValidTestCases.isEmpty()) {
 			String structName = "_cu_ti_"
 					+ suiteDef.getName().substring(suiteDef.getName().lastIndexOf(".") + 1).replace(".", "_") 	// the @TestSuite definition name
 					+ "_" + currComp.getName()				// sub-component instance
 					+ "_" + currTypeItf.getName();
 
 			TestInfo currTestInfo = new TestInfo(structName, currItfValidTestCases);
 
 			// we want to be able to discriminate Mind Suites where multiple tester component instances provide the same interface
 			String fullSuiteDescription = suiteDescription 	// @TestSuite description
 					+ " - " + currComp.getName()		// sub-comp
 					+ " - " + currTypeItf.getName();	// itf
 
 			suite = new Suite(fullSuiteDescription,
 					currItfInitFuncName, currItfInitMethName,
 					currItfCleanupFuncName, currItfCleanupMethName,
 					currTestInfo,
 					itfExportName);
 		}
 		
 		return suite;
 
 	}
 
 	/**
 	 * Create and initialize an internal delegation binding for our test interface, like:
 	 * "binds this.exportItf to tester.itf" where "this" is the current TestSuite. 
 	 * @param itfExportName The exported interface instance name - A detailed name to keep debug easy.
 	 * @param currComp The target tester sub-component.
 	 * @param itfName The target test interface.
 	 * @return A new internal delegation binding.
 	 */
 	private Binding newInternalDelegationBinding(String itfExportName,
 			Component currComp, String itfName) {
 
 		Binding newInternalBinding = ASTHelper.newBinding(nodeFactoryItf);
 		newInternalBinding.setFromComponent(Binding.THIS_COMPONENT);
 		newInternalBinding.setFromInterface(itfExportName);
 		// TODO: Support collections ? // setFromInterfaceNumber
 		newInternalBinding.setToComponent(currComp.getName());
 		newInternalBinding.setToInterface(itfName);
 		// TODO: Support collections ?
 
 		return newInternalBinding;
 	}
 
 	/**
 	 * Create and initialize a binding for which we know the destination (the currently
 	 * exported interface) and its source (that will use the same name) but wish
 	 * to configure the source later.
 	 * @param itfExportName The source and destination interface instance name.
 	 * @param currDef The destination TestSuite definition (we'll compute it's instance name using name convention).
 	 * @return A new binding instance to be exported to the test container.
 	 */
 	private Binding newOuterBinding(String itfExportName, Definition currDef) {
 
 		Binding newOuterBinding = ASTHelper.newBinding(nodeFactoryItf);
 
 		// no "setFromComponent" since it will be completed later with the Suite instance name
 		// itf name won't change though
 		newOuterBinding.setFromInterface(itfExportName);
 		// TODO: Support collections ? // setFromInterfaceNumber
 		// target instance name is convention-based (see addComponents to the container way before in this code)
 		newOuterBinding.setToComponent(currDef.getName().replace(".", "_") + "Instance");
 		// same in client and server
 		newOuterBinding.setToInterface(itfExportName);
 		// TODO: Support collections ? // setFromInterfaceNumber
 
 		return newOuterBinding;
 	}
 
 	/**
 	 * According to command-line configured --cunit-mode, load the
 	 * application templated with Console or Automated sub-component.
 	 * @return The whole application definition to be compiled in the end.
 	 */
 	private void configureApplicationTemplateFromCUnitMode() {
 		//List<Object> loadedDefs = null;
 		rootAdlName = adlName + "<";
 
 		String cunitModeUserInput = (String) compilerContext.get(CUnitModeOptionHandler.CUNITMODE_CONTEXT_KEY);
 		if (cunitModeUserInput.equals("console")) {
 			cunit_mode = CUnitMode.CONSOLE;
 			logger.info("Loading container in Console mode");
 			rootAdlName += "org.ow2.mind.unit.MindUnitConsole";
 		} else {
 			if (cunitModeUserInput.equals("gcov")) {
 				cunit_mode = CUnitMode.GCOV;
 				logger.info("Loading container in GCov Automated mode");
 			}
 			else {
 				cunit_mode = CUnitMode.AUTOMATED;
 				logger.info("Loading container in basic Automated mode");
 			}
 
 			rootAdlName += "org.ow2.mind.unit.MindUnitAutomated";
 		}
 
 		rootAdlName += ">";
 
 		/*
 		 * If we wanted to check the application container...
 		try {
 			loadedDefs = adlCompiler.compile(rootAdlName, "", CompilationStage.CHECK_ADL, compilerContext);
 		} catch (ADLException e) {
 			if (!errorManager.getErrors().contains(e.getError())) {
 				// the error has not been logged in the error manager, print it.
 				try {
 					errorManager.logError(e.getError());
 				} catch (final ADLException e2) {
 					System.exit(1);
 				}
 			}
 		} catch (InterruptedException e) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, "Interrupted while executing compilation tasks");
 		}
 		 */
 
 	}
 
 	/**
 	 * Instantiate each @TestSuite as a component instance and add the instance to the container.
 	 * @param containerDef The container definition to be filled.
 	 */
 	private void addTestSuitesToContainer(Definition containerDef) {
 		for (Definition currTestDef : validTestSuitesDefsList) {
 			DefinitionReference currTestDefRef = ASTHelper.newDefinitionReference(nodeFactoryItf, currTestDef.getName());
 			ASTHelper.setResolvedDefinition(currTestDefRef, currTestDef);
 
 			// Instantiate a component of @TestSuite type
 			Component currComp = ASTHelper.newComponent(nodeFactoryItf, currTestDef.getName().replace(".", "_") + "Instance", currTestDefRef);
 			currComp.setDefinitionReference(currTestDefRef);
 			ASTHelper.setResolvedComponentDefinition(currComp, currTestDef);
 
 			// Add the component to the container
 			((ComponentContainer) containerDef).addComponent(currComp);
 		}
 	}
 
 	private Definition getContainerFromName(String testContainerName) {
 		Definition containerDef = null;
 
 		try {
 			List<Object> containerDefs = adlCompiler.compile(testContainerName, "", CompilationStage.CHECK_ADL, compilerContext);
 			if (containerDefs == null) {
 				logger.severe("Test container could not be loaded - Check availability of the MindUnit components in your runtime folder/source-path !");
 				System.exit(1);
 			}
 
 			// should be size 1 and of course a definition... why would it be different ?
 			if (containerDefs.size() > 1) 
 				logger.warning("Container loading returned multiple definitions - Using only the first one");
 
 			Object containerObj = containerDefs.get(0);
 			if (!(containerObj instanceof Definition)) {
 				logger.severe("Container type wasn't a definition or could not be loaded");
 				System.exit(1);
 			}
 
 			containerDef = (Definition) containerDefs.get(0);
 
 			if (!ASTHelper.isComposite(containerDef)) {
 				logger.severe("Container wasn't a composite ! Please be serious.");
 				System.exit(1);
 			}
 
 		} catch (ADLException e) {
 			if (!errorManager.getErrors().contains(e.getError())) {
 				// the error has not been logged in the error manager, print it.
 				try {
 					errorManager.logError(e.getError());
 				} catch (final ADLException e2) {
 					System.exit(1);
 				}
 			}
 		} catch (InterruptedException e) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, "Interrupted while executing compilation tasks");
 		}
 
 		return containerDef;
 	}
 
 	/**
 	 * Filters the testFolderADLList of ADLs to keep only the @TestSuite-annotated ones.
 	 * Those TestSuite-s Definitions are added to the validTestSuitesDefsList list.
 	 */
 	private void filterValidTestSuites() {
 		for (String currentADL : testFolderADLList) {
 			List<Object> l;
 			try {
 				// Here the components may be reloaded from the incremental compilation cache 
 				// if no modification happened
 				l = adlCompiler.compile(currentADL, "", CompilationStage.CHECK_ADL, compilerContext);
 				if (l != null && !l.isEmpty()) {
 					for (Object currObj : l) {
 						if (!(currObj instanceof Definition))
 							// error case that should never happen
 							logger.warning("Encountered object \"" + currObj.toString() + "\" while handling " + currentADL + " isn't a definition !");
 						else {
 							// we've got a definition
 							Definition currDef = (Definition) currObj;
 							// Then keep only if annotated with @TestSuite
 							if (AnnotationHelper.getAnnotation(currDef, TestSuite.class) != null) {
 								validTestSuitesDefsList.add(currDef);
 								logger.info("@TestSuite found: " + currDef.getName());
 							}
 						}
 					}
 				} else logger.info(currentADL + " definition load failed, invalid ADL");
 			} catch (ADLException e) {
 				logger.info(currentADL + " definition load failed, invalid ADL");
 			} catch (InterruptedException e) {
 				logger.info(currentADL + " definition load failed, thread was interrupted ! detailed error below: ");
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Create "unit-gen" folder in the user-defined output folder,
 	 * then add it to the URL list that will be added to the source-path,
 	 * for the generated files to be accessible to loading.
 	 */
 	private void createAndAddGeneratedFilesFolderToPathURLs() {
 		File outDir = OutPathOptionHandler.getOutPath(compilerContext);
 		File genFilesDir = new File(outDir.getAbsolutePath(), "unit-gen");
 		while (!genFilesDir.exists())
 			genFilesDir.mkdirs();
 		try {
 			urlList.add(genFilesDir.toURI().toURL());
 		} catch (MalformedURLException e2) {
 			logger.severe("Could not access to " + outDir.getPath() + "/" + "unit-gen" + " file generation path !");
 		}
 	}
 
 	/**
 	 * A method to validate the folders the user provided as command-line arguments.
 	 * The valid test folders are then stored in 2 fields:
 	 * - List<File> validTestFoldersList: Used for ADL files listing through directory exploration
 	 * - List<URL> urlList: Used by source-path configuration, as it uses a URLClassLoader
 	 * @param testFoldersList The user-defined target tests folders list.
 	 */
 	private void checkAndStoreValidTargetFolders(List<String> testFoldersList) {
 		for (String testFolder : testFoldersList) {
 			final File testDirectory = new File(testFolder);
 			if (!testDirectory.isDirectory() || !testDirectory.canRead()) {
 				logger.severe(String.format("Cannot read source path '%s' - skipped.",
 						testDirectory.getPath()));
 			} else {
 				validTestFoldersList.add(testDirectory);
 				try {
 					URL testDirURL = testDirectory.toURI().toURL();
 					urlList.add(testDirURL);
 				} catch (final MalformedURLException e) {
 					// will never happen since we already checked File
 				}
 			}
 		}
 	}
 
 	/**
 	 * Inspired from org.ow2.mind.cli.SrcPathOptionHandler.
 	 * We extend the original ClassLoader by using it as a parent to a new ClassLoader.
 	 * Valid test folders are taken from this class urlList attribute.
 	 * We also extend the ClassLoader to the current jar in order to use local (hidden)
 	 * resources.
 	 */
 	protected void addTestFoldersToPath() {
 		// get the --src-path elements
 		URLClassLoader srcPathClassLoader = (URLClassLoader) SrcPathOptionHandler.getSourceClassLoader(compilerContext);
 
 		// URL array of test path, replace the original source class-loader with our enriched one
 		// and use the original source class-loader as parent so as to keep everything intact
 		ClassLoader srcAndTestPathClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), srcPathClassLoader);
 
 		// replace the original source classloader with the new one in the context
 		compilerContext.remove("classloader");
 		compilerContext.put("classloader", srcAndTestPathClassLoader);
 	}
 
 	/**
 	 * Inspired by Mindoc's DocumentationIndexGenerator.
 	 * @throws IOException
 	 */
 	protected void listTestFolderADLs() {
 		for (final File directory : validTestFoldersList) {
 			try {
 				exploreDirectory(directory.getCanonicalFile(), null);
 			} catch (IOException e) {
 				logger.severe(String.format("Cannot find directory '%s' - skipped.",
 						directory.getPath()));
 			}
 		}
 	}
 
 	/**
 	 * Recursively find ADL files from the root directory.
 	 * Inspired by Mindoc's DocumentationIndexGenerator.
 	 * FileFilterUtils comes from Apache commons-io.
 	 * @throws IOException
 	 */
 	private void exploreDirectory(final File directory, String currentPackage) {
 		if(directory.isHidden()) return;
 
 		String subPackage = "";
 
 		for (final File file : directory.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".adl"))) {
 			String compName = file.getName();
 			// remove ".adl" extension
 			compName = compName.substring(0, compName.length() - 4);
 			// add package
 			compName = currentPackage + "." + compName;
 
 			// save component info
 			testFolderADLList.add(compName);
 		}
 
 		for (final File subDirectory: directory.listFiles(
 				new FileFilter() {
 					public boolean accept(final File pathname) {
 						return pathname.isDirectory();
 					}
 				})) {
 
 			// base folder
 			if (currentPackage == null)
 				subPackage = subDirectory.getName();
 			else // already in sub-folder
 				subPackage = currentPackage + "." + subDirectory.getName();
 
 			// recursion
 			exploreDirectory(subDirectory, subPackage);
 		}
 	}
 
 	protected Injector getBootstrapInjector() {
 		return Guice.createInjector(new PluginLoaderModule());
 	}
 
 	/**
 	 * Here we use the standard compiler initialization + A number of internals usually coming later.
 	 */
 	protected void initCompiler() {
 		errorManager = injector.getInstance(ErrorManager.class);
 		adlCompiler = injector.getInstance(ADLCompiler.class);
 
 		// Our additions
 		nodeFactoryItf = injector.getInstance(NodeFactory.class);
 		suiteCSrcGenerator = injector.getInstance(SuiteSourceGenerator.class);
 		loaderItf = injector.getInstance(Loader.class);
 		idlLoaderItf = injector.getInstance(IDLLoader.class);
 		outputFileLocatorItf = injector.getInstance(OutputFileLocator.class);
 		nodeMergerItf = injector.getInstance(NodeMerger.class);
 	}
 
 	protected void initInjector(final PluginManager pluginManager,
 			final Map<Object, Object> compilerContext) {
 		injector = Guice.createInjector(GuiceModuleExtensionHelper.getModules(
 				pluginManager, compilerContext));
 	}
 
 	public List<Object> compile(final List<Error> errors,
 			final List<Error> warnings) throws InvalidCommandLineException {
 
 		logger.info("Launching executable compilation (executable name: " + exeName + ")");
 
 		final List<Object> result = new ArrayList<Object>();
 		try {
 			/*final HashMap<Object, Object> contextMap = new HashMap<Object, Object>(
 					compilerContext);*/
 
 			// Force compilation stage to be CompilationStage.COMPILE_EXE
 			final List<Object> l = adlCompiler.compile(rootAdlName, exeName,
 					CompilationStage.COMPILE_EXE, compilerContext);
 
 			if (l != null) result.addAll(l);
 
 		} catch (final InterruptedException e1) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, "Interrupted while executing compilation tasks");
 		} catch (final ADLException e1) {
 			if (!errorManager.getErrors().contains(e1.getError())) {
 				// the error has not been logged in the error manager, print it.
 				try {
 					errorManager.logError(e1.getError());
 				} catch (final ADLException e2) {
 					// ignore
 				}
 			}
 		}
 		if (errors != null) errors.addAll(errorManager.getErrors());
 		if (warnings != null) warnings.addAll(errorManager.getWarnings());
 		return result;
 	}
 
 	protected void addOptions(final PluginManager pluginManagerItf) {
 		options.addOptions(helpOpt, versionOpt, extensionPointsListOpt);
 
 		options.addOptions(CommandLineOptionExtensionHelper
 				.getCommandOptions(pluginManagerItf));
 	}
 
 	protected void checkExclusiveGroups(final PluginManager pluginManagerItf,
 			final CommandLine cmdLine) throws InvalidCommandLineException {
 		final Collection<Set<String>> exclusiveGroups = CommandLineOptionExtensionHelper
 				.getExclusiveGroups(pluginManagerItf);
 		for (final Set<String> exclusiveGroup : exclusiveGroups) {
 			CmdOption opt = null;
 			for (final String id : exclusiveGroup) {
 				final CmdOption opt1 = cmdLine.getOptions().getById(id);
 				if (opt1.isPresent(cmdLine)) {
 					if (opt != null) {
 						throw new InvalidCommandLineException("Options '"
 								+ opt.getPrototype() + "' and '" + opt1.getPrototype()
 								+ "' cannot be specified simultaneously on the command line.",
 								1);
 					}
 					opt = opt1;
 				}
 			}
 		}
 	}
 
 	protected void invokeOptionHandlers(final PluginManager pluginManagerItf,
 			final CommandLine cmdLine, final Map<Object, Object> context)
 					throws InvalidCommandLineException {
 		final List<CmdOption> toBeExecuted = new LinkedList<CmdOption>(cmdLine
 				.getOptions().getOptions());
 		final Set<String> executedId = new HashSet<String>(toBeExecuted.size());
 		while (!toBeExecuted.isEmpty()) {
 			final int toBeExecutedSize = toBeExecuted.size();
 			final Iterator<CmdOption> iter = toBeExecuted.iterator();
 			while (iter.hasNext()) {
 				final CmdOption option = iter.next();
 				final List<String> precedenceIds = CommandLineOptionExtensionHelper
 						.getPrecedenceIds(option, pluginManagerItf);
 				if (executedId.containsAll(precedenceIds)) {
 					// task ready to be executed
 					for (final CommandOptionHandler handler : CommandLineOptionExtensionHelper
 							.getHandler(option, pluginManagerItf)) {
 						handler.processCommandOption(option, cmdLine, context);
 					}
 					executedId.add(option.getId());
 					iter.remove();
 				}
 			}
 			if (toBeExecutedSize == toBeExecuted.size()) {
 				// nothing has been executed. there is a circular dependency
 				throw new CompilerError(GenericErrors.GENERIC_ERROR,
 						"Circular dependency in command line option handlers: "
 								+ toBeExecuted);
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 	// Utility methods
 	// ---------------------------------------------------------------------------
 
 	//-- new utility methods imported from CompositeInterfaceLoader & AbstractMembraneLoader since there is no helper for this
 	protected TypeInterface getInternalInterface(final Interface itf) {
 		if (!(itf instanceof TypeInterface)) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, itf,
 					"Interface is not a TypeInterface");
 		}
 
 		// clone external interface to create its dual internal interface.
 		final TypeInterface internalItf;
 		try {
 			internalItf = (TypeInterface) nodeMergerItf.merge(
 					nodeFactoryItf.newNode("internalInterface",
 							TypeInterface.class.getName()), itf, null);
 			internalItf.astSetSource(itf.astGetSource());
 		} catch (final ClassNotFoundException e) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
 					"Node factory error");
 		} catch (final MergeException e) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
 					"Node merge error");
 		}
 		if (TypeInterfaceUtil.isClient(itf))
 			internalItf.setRole(TypeInterface.SERVER_ROLE);
 		else
 			internalItf.setRole(TypeInterface.CLIENT_ROLE);
 
 		return internalItf;
 	}
 
 	protected Controller newControllerNode() {
 		return MembraneASTHelper.newControllerNode(nodeFactoryItf);
 	}
 
 	protected ControllerInterface newControllerInterfaceNode(
 			final String itfName, final boolean isInternal) {
 		return MembraneASTHelper.newControllerInterfaceNode(nodeFactoryItf,
 				itfName, isInternal);
 	}
 
 	protected Source newSourceNode(final String path) {
 		return MembraneASTHelper.newSourceNode(nodeFactoryItf, path);
 	}
 
 	protected ControllerContainer turnToControllerContainer(final Node node) {
 		return MembraneASTHelper.turnToControllerContainer(node, nodeFactoryItf,
 				nodeMergerItf);
 	}
 
 	protected InternalInterfaceContainer turnToInternalInterfaceContainer(
 			final Node node) {
 		return MembraneASTHelper.turnToInternalInterfaceContainer(node,
 				nodeFactoryItf, nodeMergerItf);
 	}
 
 	public enum CUnitMode {
 		AUTOMATED, CONSOLE, GCOV
 	}
 
 	//-- original utility methods
 
 	private void printExtensionPoints(final PluginManager pluginManager,
 			final PrintStream out) {
 		final Iterable<String> extensionPoints = pluginManager
 				.getExtensionPointNames();
 		System.out.println("Supported extension points are : ");
 		for (final String extensionPoint : extensionPoints) {
 			System.out.println("\t'" + extensionPoint + "'");
 		}
 	}
 
 	protected static void checkDir(final File d)
 			throws InvalidCommandLineException {
 		if (d.exists() && !d.isDirectory())
 			throw new InvalidCommandLineException("Invalid build directory '"
 					+ d.getAbsolutePath() + "' not a directory", 6);
 	}
 
 	protected String getVersion() {
 		final String pkgVersion = this.getClass().getPackage()
 				.getImplementationVersion();
 		return (pkgVersion == null) ? "unknown" : pkgVersion;
 	}
 
 	protected String getProgramName() {
 		return System.getProperty(PROGRAM_NAME_PROPERTY_NAME, getClass().getName());
 	}
 
 	protected void printVersion(final PrintStream ps) {
 		ps.println(getProgramName() + " version " + getVersion());
 	}
 
 	protected void printHelp(final PrintStream ps) {
 		printUsage(ps);
 		ps.println();
 		ps.println("Available options are :");
 		int maxCol = 0;
 
 		for (final CmdOption opt : options.getOptions()) {
 			final int col = 2 + opt.getPrototype().length();
 			if (col > maxCol) maxCol = col;
 		}
 		for (final CmdOption opt : options.getOptions()) {
 			final StringBuffer sb = new StringBuffer("  ");
 			sb.append(opt.getPrototype());
 			while (sb.length() < maxCol)
 				sb.append(' ');
 			sb.append("  ").append(opt.getDescription());
 			ps.println(sb);
 		}
 	}
 
 	protected void printUsage(final PrintStream ps) {
 		ps.println("Usage: " + getProgramName()
 				+ " [OPTIONS] (<test_path>)+");
 		ps.println("  where <test_path> is a path where to find test cases to be ran.");
 	}
 
 	protected void handleException(final InvalidCommandLineException e) {
 		logger.log(Level.FINER, "Caught an InvalidCommandLineException", e);
 		if (PrintStackTraceOptionHandler.getPrintStackTrace(compilerContext)) {
 			e.printStackTrace();
 		} else {
 			System.err.println(e.getMessage());
 			printHelp(System.err);
 			System.exit(e.getExitValue());
 		}
 	}
 
 	/**
 	 * Entry point.
 	 * 
 	 * @param args
 	 */
 	public static void main(final String... args) {
 		final Launcher l = new Launcher();
 		try {
 			l.init(args);
 			l.constructTestApplication();
 			l.compile(null, null);
 		} catch (final InvalidCommandLineException e) {
 			l.handleException(e);
 		}
 		if (!l.errorManager.getErrors().isEmpty()) System.exit(1);
 	}
 
 	public static void nonExitMain(final String... args)
 			throws InvalidCommandLineException, ADLException {
 		nonExitMain(null, null, args);
 	}
 
 	public static void nonExitMain(final List<Error> errors,
 			final List<Error> warnings, final String... args)
 					throws InvalidCommandLineException, ADLException {
 		final Launcher l = new Launcher();
 		l.init(args);
 		l.constructTestApplication();
 		l.compile(errors, warnings);
 	}
 
 }
