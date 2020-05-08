 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.export;
 
 import java.util.HashSet;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSetSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.internal.wizard.SaveDeltaWizard;
 import org.eclipse.emf.compare.ui.viewer.content.ModelContentMergeViewer;
 import org.eclipse.emf.compare.ui.viewer.structure.ModelStructureMergeViewer;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IMenuCreator;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This implementation of {@link AbstractCompareAction} is used to create the structure viewer's "export as"
  * action.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ExportMenu extends AbstractCompareAction implements IMenuCreator {
 	/** Wild card for all file extensions. */
 	private static final String ALL_EXTENSIONS = "*"; //$NON-NLS-1$
 
 	/** Bundle where the messages and icons can be found. */
 	private static final ResourceBundle BUNDLE = ResourceBundle
 			.getBundle(ModelContentMergeViewer.BUNDLE_NAME);
 
 	/** Keeps track of the {@link ActionContributionItem actions} declared for the extension point. */
 	private static final Set<ExportActionDescriptor> CACHED_ACTIONS = new HashSet<ExportActionDescriptor>();
 
 	/** Name of the extension point to parse for actions. */
 	private static final String EXPORT_ACTIONS_EXTENSION_POINT = "org.eclipse.emf.compare.ui.export"; //$NON-NLS-1$
 
 	/** Control under which the menu must be created. */
 	protected final Control parentControl;
 
 	/** Viewer currently displaying this menu. */
 	protected final ModelStructureMergeViewer parentViewer;
 
 	/** Menu manager for this action. */
 	private final MenuManager menuManager = new MenuManager();
 
 	/** Default action of the displayed menu. */
 	private final Action saveAction;
 
 	static {
 		parseExtensionMetaData();
 	}
 
 	/**
 	 * This default constructor will instantiate an action given the {@link #BUNDLE bundle} resources prefixed
 	 * by &quot;action.save&quot;.
 	 * 
 	 * @param parent
 	 *            {@link Control} under which this {@link Action}'s menu must be created.
 	 * @param owner
 	 *            The viewer currently displaying this menu.
 	 */
 	public ExportMenu(Control parent, ModelStructureMergeViewer owner) {
 		super(BUNDLE, "action.export.menu."); //$NON-NLS-1$
 		setMenuCreator(this);
 		parentControl = parent;
 		parentViewer = owner;
 		final ResourceBundle bundle = ResourceBundle.getBundle(ModelContentMergeViewer.BUNDLE_NAME);
 		saveAction = new AbstractCompareAction(bundle, "action.export.emfdiff.") { //$NON-NLS-1$
 			@Override
 			public void run() {
 				final SaveDeltaWizard wizard = new SaveDeltaWizard(bundle
 						.getString("UI_SaveDeltaWizard_FileExtension")); //$NON-NLS-1$
 				wizard.init(PlatformUI.getWorkbench(), (ComparisonSnapshot)parentViewer.getInput());
 				final WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow().getShell(), wizard);
 				dialog.open();
 			}
 		};
 	}
 
 	/**
 	 * This will parse {@link #EXPORT_ACTIONS_EXTENSION_POINT} for actions to display.
 	 */
 	private static void parseExtensionMetaData() {
 		final IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(
 				EXPORT_ACTIONS_EXTENSION_POINT).getExtensions();
 		for (final IExtension extension : extensions) {
 			for (final IConfigurationElement configElement : extension.getConfigurationElements()) {
 				final ExportActionDescriptor descriptor = new ExportActionDescriptor(configElement);
 				CACHED_ACTIONS.add(descriptor);
 			}
 		}
 	}
 
 	/**
 	 * This will add the given action to this action's menu.
 	 * 
 	 * @param action
 	 *            {@link Action} to add to this action's menu.
 	 */
 	public void addActionToMenu(Action action) {
 		final ActionContributionItem contribution = new ActionContributionItem(action);
 		menuManager.add(contribution);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
 	 */
 	public void dispose() {
 		if (menuManager.getMenu() != null) {
 			menuManager.getMenu().dispose();
 		}
 		menuManager.dispose();
 	}
 
 	/**
 	 * This will switch the enable state of {@link #saveAction}.
 	 * 
 	 * @param enabled
 	 *            <code>True</code> if the action must be enabled, <code>False</code> otherwise
 	 */
 	public void enableSave(boolean enabled) {
 		saveAction.setEnabled(enabled);
 	}
 
 	/**
 	 * This will return all actions from the {@link #CACHED_ACTIONS cached actions} that apply to the given
 	 * <code>fileExtension</code>.
 	 * 
 	 * @param fileExtension
 	 *            File extension which we seel actions for.
 	 * @return All actions from the {@link #CACHED_ACTIONS cached actions} that apply to the given
 	 *         <code>fileExtension</code>.
 	 * @see #ALL_EXTENSIONS
 	 * @see #getComparedModelsExtension()
 	 */
 	public Set<ExportActionDescriptor> getActions(String fileExtension) {
 		final Set<ExportActionDescriptor> result = new HashSet<ExportActionDescriptor>(CACHED_ACTIONS.size());
 		for (final ExportActionDescriptor actionDescriptor : CACHED_ACTIONS) {
 			final String extension;
 			if (actionDescriptor.getFileExtension() != null) {
 				extension = actionDescriptor.getFileExtension();
 			} else {
 				extension = ALL_EXTENSIONS;
 			}
 			if (extension.equals(ALL_EXTENSIONS) || extension.equals(fileExtension)) {
 				result.add(actionDescriptor);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the file extension of the compared models. If the extensions aren't the same, returns
 	 * {@value #ALL_EXTENSIONS}.
 	 * 
 	 * @return The file extension of the compared models.
 	 */
 	public String getComparedModelsExtension() {
 		String extension = ALL_EXTENSIONS;
 		if (parentViewer.getInput() instanceof ComparisonResourceSnapshot) {
 			final DiffModel diffModel = ((ComparisonResourceSnapshot)parentViewer.getInput()).getDiff();
 			String leftExtension = ALL_EXTENSIONS;
 			String rightExtension = ALL_EXTENSIONS;
 			String ancestorExtension = null;
 			if (diffModel.getLeftRoots().get(0).eResource() != null) {
 				leftExtension = diffModel.getLeftRoots().get(0).eResource().getURI().fileExtension();
 			}
 			if (diffModel.getRightRoots().get(0).eResource() != null) {
 				rightExtension = diffModel.getRightRoots().get(0).eResource().getURI().fileExtension();
 			}
 			if (!diffModel.getAncestorRoots().isEmpty()
 					&& diffModel.getAncestorRoots().get(0).eResource() != null) {
 				ancestorExtension = diffModel.getAncestorRoots().get(0).eResource().getURI().fileExtension();
 			}
 
 			if (leftExtension.equals(rightExtension)
 					&& (ancestorExtension == null || leftExtension.equals(ancestorExtension))) {
 				extension = leftExtension;
 			}
 		} else {
 			String lastExtension = null;
 			for (final DiffModel diff : ((ComparisonResourceSetSnapshot)parentViewer.getInput())
 					.getDiffResourceSet().getDiffModels()) {
 				String leftExtension = ALL_EXTENSIONS;
 				String rightExtension = ALL_EXTENSIONS;
 				String ancestorExtension = null;
 				if (diff.getLeftRoots().get(0).eResource() != null) {
 					leftExtension = diff.getLeftRoots().get(0).eResource().getURI().fileExtension();
 				}
 				if (diff.getRightRoots().get(0).eResource() != null) {
 					rightExtension = diff.getRightRoots().get(0).eResource().getURI().fileExtension();
 				}
 				if (!diff.getAncestorRoots().isEmpty() && diff.getAncestorRoots().get(0).eResource() != null) {
 					ancestorExtension = diff.getAncestorRoots().get(0).eResource().getURI().fileExtension();
 				}
 
 				if (leftExtension.equals(rightExtension)
 						&& (ancestorExtension == null || leftExtension.equals(ancestorExtension))) {
 					if (lastExtension != null && !leftExtension.equals(lastExtension)) {
 						lastExtension = ALL_EXTENSIONS;
 						break;
 					} else if (lastExtension == null) {
 						lastExtension = leftExtension;
 					}
 				}
 			}
 			extension = lastExtension;
 		}
 		return extension;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
 	 */
 	public Menu getMenu(Control parent) {
 		// Creates the menu if needed, or removes all elements except for the save action
 		if (menuManager.getMenu() == null) {
 			menuManager.createContextMenu(parent);
 		} else {
 			menuManager.removeAll();
 		}
 		menuManager.add(saveAction);
 		for (final ExportActionDescriptor descriptor : getActions(getComparedModelsExtension())) {
 			final IExportAction actionDescriptor = descriptor.getActionDescriptorInstance();
 			final Action action = new AbstractCompareAction(actionDescriptor) {
 				@Override
 				public void run() {
					actionDescriptor.exportSnapshot((ComparisonResourceSnapshot)parentViewer.getInput());
 				}
 			};
 			addActionToMenu(action);
 		}
 		return menuManager.getMenu();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
 	 */
 	public Menu getMenu(Menu parent) {
 		if (menuManager.getMenu() != null)
 			return menuManager.getMenu();
 		return null;
 	}
 
 	/**
 	 * If this action is triggered, it'll run the default save action.
 	 * 
 	 * @see org.eclipse.emf.compare.ui.AbstractCompareAction#run()
 	 */
 	@Override
 	public void run() {
 		if (saveAction.isEnabled()) {
 			saveAction.run();
 		}
 	}
 }
 
 /**
  * Describes an action registered from a plug-in's extension point for the "export diff as..." menu.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 final class ExportActionDescriptor {
 	/** Name of the extension point attribute corresponding to the action's class. */
 	private static final String ATTRIBUTE_EXPORT_ACTION_CLASS = "class"; //$NON-NLS-1$
 
 	/** Name of the extension point attribute corresponding to the file extensions considered. */
 	private static final String ATTRIBUTE_FILE_EXTENSION = "fileExtension"; //$NON-NLS-1$
 
 	/** This descriptor's wrapped {@link IExportAction action}. */
 	private IExportAction action;
 
 	/** Keeps a reference to the configuration element that describes the {@link Action}. */
 	private final IConfigurationElement element;
 
 	/** Considered file extensions. */
 	private final String fileExtension;
 
 	/**
 	 * Constructs a new descriptor from an IConfigurationElement.
 	 * 
 	 * @param configuration
 	 *            Configuration of the {@link Action}.
 	 */
 	public ExportActionDescriptor(IConfigurationElement configuration) {
 		element = configuration;
 		fileExtension = element.getAttribute(ATTRIBUTE_FILE_EXTENSION);
 	}
 
 	/**
 	 * Returns an instance of the {@link IExportAction action's decriptor}.
 	 * 
 	 * @return Instance of the {@link IExportAction action's decriptor}.
 	 */
 	public IExportAction getActionDescriptorInstance() {
 		if (action == null) {
 			try {
 				action = (IExportAction)element.createExecutableExtension(ATTRIBUTE_EXPORT_ACTION_CLASS);
 			} catch (final CoreException e) {
 				EMFComparePlugin.log(e, true);
 			}
 		}
 		return action;
 	}
 
 	/**
 	 * Returns the wrapped action's considered file extensions.
 	 * 
 	 * @return The wrapped action's considered file extensions.
 	 */
 	public String getFileExtension() {
 		return fileExtension;
 	}
 }
