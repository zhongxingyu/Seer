 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.views.scm;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.ui.Activator;
 import org.eclipse.emf.emfstore.client.ui.views.changes.ChangePackageVisualizationHelper;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationId;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Label provider for the scm views.
  * 
  * @author Shterev
  */
 public class SCMLabelProvider extends ColumnLabelProvider {
 
 	private Project project;
 	private List<OperationId> highlighted;
 	private static final String ELEMENT_NOT_FOUND = "There is no sufficient information to display this element";
 	private static final String LOCAL_REVISION = "Local revision";
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param project
 	 *            the project.
 	 */
 	public SCMLabelProvider(Project project) {
 		super();
 		this.project = project;
 		this.highlighted = new ArrayList<OperationId>();
 
 		baseRevision = Activator.getImageDescriptor("icons/HistoryInfo_base.png").createImage();
 		currentRevision = Activator.getImageDescriptor("icons/HistoryInfo_current.png").createImage();
 		headRevision = Activator.getImageDescriptor("icons/HistoryInfo_head.png").createImage();
 	}
 
 	private ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
 		ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 	private AdapterFactoryLabelProvider adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 	private ChangePackageVisualizationHelper changePackageVisualizationHelper;
 	private Image baseRevision;
 	private Image currentRevision;
 	private Image headRevision;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getText(Object element) {
 
 		String ret = null;
 		if (element instanceof HistoryInfo) {
 			HistoryInfo historyInfo = (HistoryInfo) element;
 			return getText(historyInfo);
 		} else if (element instanceof AbstractOperation && changePackageVisualizationHelper != null) {
 			ret = changePackageVisualizationHelper.getDescription((AbstractOperation) element);
 		} else if (element instanceof ModelElementId && changePackageVisualizationHelper != null) {
 			EObject modelElement = changePackageVisualizationHelper.getModelElement((ModelElementId) element);
 			if (modelElement != null) {
				ret = adapterFactoryLabelProvider.getText(modelElement);
 			} else {
 				return ELEMENT_NOT_FOUND;
 			}
 		} else if (element instanceof ChangePackage) {
 			ChangePackage changePackage = (ChangePackage) element;
 			return getText(changePackage);
 		} else if (element instanceof EObject) {
 			ret = adapterFactoryLabelProvider.getText(element);
 		} else {
 			ret = super.getText(element);
 		}
 
 		return ret;
 	}
 
 	private String getText(ChangePackage changePackage) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Change Package");
 		if (changePackage.getLogMessage() != null) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
 			LogMessage logMessage = changePackage.getLogMessage();
 			builder.append(" [");
 			builder.append(logMessage.getAuthor());
 			Date clientDate = logMessage.getClientDate();
 			if (clientDate != null) {
 				builder.append(" @ ");
 				builder.append(dateFormat.format(clientDate));
 			}
 			builder.append("] ");
 			builder.append(logMessage.getMessage());
 		}
 		return builder.toString();
 	}
 
 	private String getText(HistoryInfo historyInfo) {
 		if (historyInfo.getPrimerySpec() != null && historyInfo.getPrimerySpec().getIdentifier() == -1) {
 			return LOCAL_REVISION;
 		}
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
 		String baseVersion = "";
 		if (historyInfo.getPrimerySpec().getIdentifier() == WorkspaceManager.getProjectSpace(project).getBaseVersion()
 			.getIdentifier()) {
 			baseVersion = "*";
 		}
 		StringBuilder builder = new StringBuilder();
 
 		if (!historyInfo.getTagSpecs().isEmpty()) {
 			builder.append("[");
 			for (TagVersionSpec versionSpec : historyInfo.getTagSpecs()) {
 				builder.append(versionSpec.getName());
 				builder.append(",");
 			}
 			builder.replace(builder.length() - 1, builder.length(), "] ");
 		}
 
 		builder.append(baseVersion);
 		builder.append("Version ");
 		builder.append(historyInfo.getPrimerySpec().getIdentifier());
 		LogMessage logMessage = null;
 
 		if (historyInfo.getLogMessage() != null) {
 			logMessage = historyInfo.getLogMessage();
 		} else if (historyInfo.getChangePackage() != null && historyInfo.getChangePackage().getLogMessage() != null) {
 			logMessage = historyInfo.getChangePackage().getLogMessage();
 		}
 		if (logMessage != null) {
 			builder.append(" [");
 			builder.append(logMessage.getAuthor());
 			Date clientDate = logMessage.getClientDate();
 			if (clientDate != null) {
 				builder.append(" @ ");
 				builder.append(dateFormat.format(clientDate));
 			}
 			builder.append("] ");
 			builder.append(logMessage.getMessage());
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Color getForeground(Object element) {
 
 		if (element instanceof AbstractOperation) {
 			AbstractOperation operation = (AbstractOperation) element;
 			if (highlighted.contains(operation.getOperationId())) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
 			}
 		}
 
 		return super.getForeground(element);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Font getFont(Object element) {
 
 		Font italic = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
 		Font bold = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
 
 		// if (getText(findTopParent((TreeNode)
 		// element)).equals(LOCAL_REVISION)) {
 		// return italic;
 		// }
 		String text = getText(element);
 		if (text == null) {
 			text = "";
 		}
 		if (element instanceof HistoryInfo) {
 			if (text.equals(LOCAL_REVISION)) {
 				return italic;
 			}
 			HistoryInfo historyInfo = (HistoryInfo) element;
 			if (historyInfo.getPrimerySpec().getIdentifier() == WorkspaceManager.getProjectSpace(project)
 				.getBaseVersion().getIdentifier()) {
 				return bold;
 			}
 		} else if (element instanceof ModelElementId) {
 			if (text.equals(ELEMENT_NOT_FOUND)) {
 				return italic;
 			}
 		}
 		if (element instanceof EObject && ((EObject) element).eContainer() instanceof AbstractOperation) {
 			AbstractOperation op = (AbstractOperation) ((EObject) element).eContainer();
 			if (element instanceof ModelElementId && element.equals(op.getModelElementId())) {
 				return bold;
 			}
 
 			EObject modelElement = (EObject) element;
 			Project project = ModelUtil.getProject(modelElement);
 			if (project != null && project.getModelElementId(modelElement).equals(op.getModelElementId())) {
 				return bold;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Image getImage(Object element) {
 
 		if (element instanceof ModelElementId) {
 			return adapterFactoryLabelProvider.getImage(changePackageVisualizationHelper
 				.getModelElement((ModelElementId) element));
 		} else if (element instanceof HistoryInfo) {
 			String text = getText(element);
 			if (text.equals(LOCAL_REVISION)) {
 				return currentRevision;
 			}
 			if (text.matches("\\[.*BASE.*\\].*")) {
 				return baseRevision;
 			}
 			if (text.matches("\\[.*HEAD.*\\].*")) {
 				return headRevision;
 			}
 		}
 		if (element instanceof CompositeOperation && ((CompositeOperation) element).getMainOperation() != null) {
 			return changePackageVisualizationHelper.getImage(adapterFactoryLabelProvider,
 				((CompositeOperation) element).getMainOperation());
 		}
 
 		if (element instanceof AbstractOperation) {
 			return changePackageVisualizationHelper.getImage(adapterFactoryLabelProvider, (AbstractOperation) element);
 		}
 		return adapterFactoryLabelProvider.getImage(element);
 	}
 
 	/**
 	 * @param changePackageVisualizationHelper
 	 *            the changePackageVisualizationHelper to set
 	 */
 	public void setChangePackageVisualizationHelper(ChangePackageVisualizationHelper changePackageVisualizationHelper) {
 		this.changePackageVisualizationHelper = changePackageVisualizationHelper;
 	}
 
 	/**
 	 * @return the changePackageVisualizationHelper
 	 */
 	public ChangePackageVisualizationHelper getChangePackageVisualizationHelper() {
 		return changePackageVisualizationHelper;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getToolTipText(Object element) {
 		HistoryInfo historyInfo = (HistoryInfo) ModelUtil.getParent(HistoryInfo.class, (EObject) element);
 		return getText(historyInfo);
 	}
 
 	/**
 	 * @return the highlighted elements list.
 	 */
 	public List<OperationId> getHighlighted() {
 		return highlighted;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		headRevision.dispose();
 		currentRevision.dispose();
 		baseRevision.dispose();
 		if (adapterFactory != null) {
 			adapterFactory.dispose();
 		}
 		if (changePackageVisualizationHelper != null) {
 			changePackageVisualizationHelper.dispose();
 		}
 	}
 }
