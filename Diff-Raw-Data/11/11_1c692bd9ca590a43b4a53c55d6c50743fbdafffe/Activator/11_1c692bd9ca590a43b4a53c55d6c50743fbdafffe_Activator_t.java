 package org.eclipse.stem.ui;
 
 /*******************************************************************************
  * Copyright (c) 2006 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.stem.ui.adapters.color.ColorProviderAdapterFactory;
 import org.eclipse.stem.ui.adapters.featuremodifiereditcomposite.ExperimentFeatureModifierEditCompositeFactory;
 import org.eclipse.stem.ui.adapters.newmodifierpage.CommonNewModifierPageAdapterFactory;
 import org.eclipse.stem.ui.adapters.newmodifierpage.GraphNewModifierPageAdapterFactory;
 import org.eclipse.stem.ui.adapters.newmodifierpage.LabelsNewModifierPageAdapterFactory;
 import org.eclipse.stem.ui.adapters.newmodifierpage.SequencerNewModifierPageAdapterFactory;
 import org.eclipse.stem.ui.adapters.propertystrings.LabelsPropertyStringProviderAdapterFactory;
 import org.eclipse.stem.ui.perspectives.Simulation;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class Activator extends AbstractUIPlugin {
 
 	/**
 	 * The symbolic identifier for this plug-in
 	 */
 	public static final String PLUGIN_ID = "org.eclipse.stem.ui"; //$NON-NLS-1$
 
 	// The shared instance.
 	private static Activator plugin;
 
 	// The SWT color registry
 	private static ColorRegistry colorRegistry = null;
 
 	// The SWT font registry
 	private static FontRegistry fontRegistry = null;
 
 	/**
 	 * The constructor.
 	 */
 	public Activator() {
 		super();
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 * 
 	 * @param context
 	 * @throws Exception
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		new SequencerNewModifierPageAdapterFactory();
 		new GraphNewModifierPageAdapterFactory();
 		new LabelsNewModifierPageAdapterFactory();
 		new ExperimentFeatureModifierEditCompositeFactory();
 		new LabelsPropertyStringProviderAdapterFactory();
 		new CommonNewModifierPageAdapterFactory();
 		
 		//Get the extension color providers and add them to the composed factory
 		IConfigurationElement[] elements = 
 			Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.stem.ui.colorproviders");
 		for (IConfigurationElement element : elements) {
 			if (!element.isValid()) {
 				Activator.logError("Invalid color provider extension", null);
 				continue;
 			}
 			//Get the bundle that extended the extension point
 			Bundle bundle = Platform.getBundle(element.getContributor().getName());
 			String name = element.getAttribute("name");
 			String factory = element.getAttribute("factory");
 			String provider = element.getAttribute("provider");
 			//Use the bundle's class loader to load the classes
 			Class factoryClass = bundle.loadClass(factory);
 			Object factoryInstance = factoryClass.newInstance();
 			Class providerClass = bundle.loadClass(provider);
 			//Register the specific factory in the general factory
 			ColorProviderAdapterFactory.INSTANCE.addAdapterFactory(
 					(AdapterFactory)factoryInstance, providerClass, name);
 		}
 	} // start
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 * 
 	 * @param context
 	 * @throws Exception
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		plugin = null;
 	}
 
 	/**
 	 * Returns the shared instance.
 	 * 
 	 * @return the shared instance.
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the color registry
 	 * 
 	 * @return a reference to the color registry
 	 */
 	public ColorRegistry getColorRegistry() {
 		if (colorRegistry == null) {
 			colorRegistry = new ColorRegistry();
 			initializeColorRegistry(colorRegistry);
 		}
 		return colorRegistry;
 	} // getColorRegistry
 
 	/**
 	 * Returns the font registry
 	 * 
 	 * @return a reference to the font registry
 	 */
 	public FontRegistry getFontRegistry() {
 		if (fontRegistry == null) {
 			fontRegistry = new FontRegistry();
 			initializeFontRegistry(fontRegistry);
 		}
 		return fontRegistry;
 	} // getColorRegistry
 
 	/**
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	@Override
 	protected void initializeImageRegistry(ImageRegistry registry) {
 //		final String ICON_PATH_PREFIX = "icons/full/customobj16/"; //$NON-NLS-1$
 		final String ICON_ACTION_PATH_PREFIX = "icons/full/actions16/"; //$NON-NLS-1$
 
 		final String CUSTOM_ICON_PATH_PREFIX2 = "icons/full/customobj16/"; //$NON-NLS-1$C
 		
 		registry.put(ISharedImages.AUTOMATIC_EXPERIMENT_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "AutomaticExperiment.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DECORATOR_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Decorator.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DECORATOR_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "DecoratorModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DYNAMIC_LABEL_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "DynamicLabel.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.EDGE_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Edge.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.EXPERIMENT_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Experiment.gif")); //$NON-NLS-1$
 		
 
 		registry.put(ISharedImages.EXPERIMENT_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "ExperimentModelFile.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.GRAPH_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Graph.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.GRAPH_FOLDER_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "GraphModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.MODEL_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Model.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.MODEL_FOLDER_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "ModelModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.MODIFIER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Modifier.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.MODIFIER_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "ModifierModelFile.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.NODE_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Node.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.PREDICATE_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Predicate.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.PREDICATE_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "PredicateModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.RECORDED_SIMULATION_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "RecordedSimulations.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.RECORDED_SIMULATION_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "RecordedSimulationsModelFile.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.SCENARIO_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Scenario.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.SCENARIO_FOLDER_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "ScenarioModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.SEQUENCER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Sequencer.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.SEQUENCER_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "SequencerModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STATIC_LABEL_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "StaticLabel.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.TRIGGER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "Trigger.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.TRIGGER_FOLDER_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "TriggerModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_PROJECT_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "stem.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_EXPLORER_PROJECT_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "StemProject.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_EXPLORER_VIEW_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "stemExplorer.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.SIMULATION_CONTROL_VIEW_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "simulationControlView.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.SIMULATION_CONTROL_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "simulationControl.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DELETE_FILE_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "delete.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_GRAPH_FILE_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "genericFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_FOLDER_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "ScenarioModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEM_SCENARIO_FILE_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2
 						+ "ScenarioModelFile.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.RUN_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "run.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.PAUSE_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "pause.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.RESET_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "reset.gif")); //$NON-NLS-1$
 		
 		registry.put(ISharedImages.RESTART_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "restart.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STEP_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "step.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.STOP_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, ICON_ACTION_PATH_PREFIX + "stop.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DISABLED_RUN_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, ICON_ACTION_PATH_PREFIX
 						+ "disabledRun.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DISABLED_PAUSE_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, ICON_ACTION_PATH_PREFIX
 						+ "disabledPause.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DISABLED_RESET_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, ICON_ACTION_PATH_PREFIX
 						+ "disabledReset.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DISABLED_STEP_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, ICON_ACTION_PATH_PREFIX
 						+ "disabledStep.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.DISABLED_STOP_ICON,
 				imageDescriptorFromPlugin(PLUGIN_ID, ICON_ACTION_PATH_PREFIX
 						+ "disabledStop.gif")); //$NON-NLS-1$
 
 		registry.put(ISharedImages.MAP_ICON, imageDescriptorFromPlugin(
 				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "map.gif")); //$NON-NLS-1$
 
 //		registry.put(ISharedImages.RECORDED_SIMULATION_ICON, imageDescriptorFromPlugin(
 //				PLUGIN_ID, CUSTOM_ICON_PATH_PREFIX2 + "RecordedSimulations.gif")); //$NON-NLS-1$
 	} // initializeImageRegistry
 
 	/**
 	 * Add SWT colors to color registry
 	 */
 	protected void initializeColorRegistry(ColorRegistry colorRegistry) {
 		// Register colors in registry
 		colorRegistry.put(ISharedColors.GREEN, new RGB(55, 255, 50));
 		colorRegistry.put(ISharedColors.YELLOW, new RGB(255, 255, 0));
 		colorRegistry.put(ISharedColors.ORANGE, new RGB(255, 170, 30));
 		colorRegistry.put(ISharedColors.GRAY, new RGB(217, 217, 217));
 		// Add more colors here
 	} // initializeColorRegistry
 
 	/**
 	 * Add SWT fonts to font registry
 	 */
 	protected void initializeFontRegistry(FontRegistry fontRegistry) {
 		int fontHeight = JFaceResources.getDefaultFont().getFontData()[0]
 				.getHeight();
 
 		FontData fontData = new FontData("Courier New", fontHeight, //$NON-NLS-1$
 				SWT.NORMAL);
 		// Register colors in registry
 		fontRegistry.put(ISharedFonts.DEFAULT, new FontData[] { fontData });
 		// Add more colors here
 	} // initializeColorRegistry
 
 	/**
 	 * Log an error to the ILog for this plugin
 	 * 
 	 * @param message
 	 *            the localized error message text
 	 * @param exception
 	 *            the associated exception, or null
 	 */
 	public static void logError(String message, Throwable exception) {
 		plugin.getLog().log(
 				new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(),
 						0, message, exception));
 	} // logError
 
 	/**
 	 * Log information to the ILog for this plugin
 	 * 
 	 * @param message
 	 *            the localized information message text
 	 * @param exception
 	 *            the associated exception, or null
 	 */
 	public static void logInformation(String message, Throwable exception) {
 		plugin.getLog().log(
 				new Status(IStatus.INFO, plugin.getBundle().getSymbolicName(),
 						0, message, exception));
 	} // logInformation
 
 	/**
 	 * @param perspectiveId
 	 *            the id of the perspective to switch to
 	 */
 	public static void switchToPerspective(String perspectiveId) {
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		try {
 			workbench.showPerspective(
					perspectiveId, workbench
 							.getActiveWorkbenchWindow());
 		} catch (WorkbenchException e) {
 			Activator.logError("Problem switching to Perspective with ID=\""
 					+ perspectiveId + "\"", e);
 		}
 	} // switchToPerspective
 } // Activator
