 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.actions;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 import com.google.common.collect.ImmutableList;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.NotSerializableException;
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIMessages;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.configuration.IEMFCompareConfiguration;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.statushandlers.StatusAdapter;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * Action that manages the save of the comparison model.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class SaveComparisonModelAction extends Action {
 
 	/** The dialog buttons. */
 	private static final ImmutableList<String> DIALOG_BUTTON_LABELS = ImmutableList.of("Replace", "Cancel");
 
 	/** The compare configuration object used to get the compare model. */
 	private IEMFCompareConfiguration configuration;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param configuration
 	 *            The compare configuration object.
 	 */
 	public SaveComparisonModelAction(IEMFCompareConfiguration configuration) {
 		this.configuration = configuration;
 		setToolTipText(EMFCompareIDEUIMessages.getString("save.model.tooltip")); //$NON-NLS-1$
 		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(EMFCompareIDEUIPlugin.PLUGIN_ID,
 				"icons/full/toolb16/saveas_edit.gif")); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.Action#run()
 	 */
 	@Override
 	public void run() {
 		Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 
 		FileDialog fileDialog = new FileDialog(parent, SWT.SAVE);
 		String filePath = fileDialog.open();
 		if (filePath != null) {
 			File file = new File(filePath);
 			if (file.exists()) {
 				MessageDialog messageDialog = new MessageDialog(parent, "File already exists", null,
 						"File \"" + file.toString()
 								+ "\" already exists. Do you want to replace the existing one?",
 						MessageDialog.WARNING, DIALOG_BUTTON_LABELS.toArray(new String[0]), 1);
 				int open = messageDialog.open();
 				if (open == DIALOG_BUTTON_LABELS.indexOf("Replace")) {
 					saveComparison(file);
					refreshLocation(filePath);
 				} // else do nothing
 			} else {
 				saveComparison(file);
				refreshLocation(filePath);
 			}
 		}
 
 		super.run();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.action.Action#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		return configuration.isLeftEditable() && configuration.isRightEditable();
 	}
 
 	/**
 	 * Save the compare model in the given result {@link File}.
 	 * 
 	 * @param file
 	 *            the result {@link File}.
 	 */
 	private void saveComparison(File file) {
 		Comparison comparison = configuration.getComparison();
 		Resource resource = new XMIResourceImpl(URI.createFileURI(file.getAbsolutePath()));
 		Copier copier = new Copier(false);
 		EObject comparisonCopy = copier.copy(comparison);
 		copier.copyReferences();
 
 		resource.getContents().add(comparisonCopy);
 		try {
 			resource.save(newHashMap());
 		} catch (RuntimeException e) {
 			if (e.getCause() instanceof NotSerializableException) {
 				final Status status = new Status(IStatus.ERROR, EMFCompareIDEUIPlugin.PLUGIN_ID,
 						EMFCompareIDEUIMessages.getString("resource.not.serializable"), e); //$NON-NLS-1$
 				StatusManager.getManager().handle(new StatusAdapter(status), StatusManager.SHOW);
 			} else {
 				EMFCompareIDEUIPlugin.getDefault().log(e);
 			}
 		} catch (IOException e) {
 			EMFCompareIDEUIPlugin.getDefault().log(e);
 		}
 	}

	/**
	 * Refresh the folder containing the given path.
	 * 
	 * @param path
	 *            the given path.
	 */
	private void refreshLocation(String path) {
		try {
			IFile fileForLocation = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
					new Path(path));
			if (fileForLocation != null) {
				fileForLocation.getParent().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			EMFCompareIDEUIPlugin.getDefault().log(e);
		}
	}
 }
