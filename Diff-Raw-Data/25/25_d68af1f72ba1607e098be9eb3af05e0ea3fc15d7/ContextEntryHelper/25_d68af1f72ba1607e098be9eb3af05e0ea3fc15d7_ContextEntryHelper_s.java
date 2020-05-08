 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.tb;
 
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.DeleteContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.internal.Messages;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 
 /**
  * The Class ContextEntryHelper comprises methods for creating and modifying
  * context button entries.
  */
 public class ContextEntryHelper {
 
 	/**
 	 * Creates an default update context button entry.
 	 * 
 	 * @param featureProvider
 	 *            the feature provider
 	 * @param pe
 	 *            the pictogram element
 	 * @return the created default update context button entry
 	 */
 	public static IContextButtonEntry createDefaultUpdateContextButton(IFeatureProvider featureProvider,
 			PictogramElement pe) {
 		final IUpdateContext updateContext = new UpdateContext(pe);
 		final IUpdateFeature updateFeature = featureProvider.getUpdateFeature(updateContext);
 		IContextButtonEntry ret = null;
 		if (updateFeature != null) {
 			ret = new ContextButtonEntry(updateFeature, updateContext) {
 				@Override
 				public boolean canExecute() {
 					return super.canExecute() && updateFeature.updateNeeded(updateContext).toBoolean();
 				}
 			};
 			markAsUpdateContextEntry(ret);
 		}
 		return ret;
 	}
 
 	/**
 	 * Creates an default remove context button entry.
 	 * 
 	 * @param featureProvider
 	 *            the feature provider
 	 * @param pe
 	 *            the pictogram element
 	 * @return the created default remove context button entry
 	 */
 	public static IContextButtonEntry createDefaultRemoveContextButton(IFeatureProvider featureProvider,
 			PictogramElement pe) {
 		IRemoveContext removeContext = new RemoveContext(pe);
 		IRemoveFeature removeFeature = featureProvider.getRemoveFeature(removeContext);
 		IContextButtonEntry ret = null;
 		if (removeFeature != null) {
 			ret = new ContextButtonEntry(removeFeature, removeContext);
 			markAsRemoveContextEntry(ret);
 		}
 		return ret;
 	}
 
 	/**
 	 * Creates an default delete context button entry.
 	 * 
 	 * @param featureProvider
 	 *            the feature provider
 	 * @param pe
 	 *            the pictogram element
 	 * @return the created default delete context button entry
 	 */
 	public static IContextButtonEntry createDefaultDeleteContextButton(IFeatureProvider featureProvider,
 			PictogramElement pe) {
 		IDeleteContext deleteContext = new DeleteContext(pe);
 		IDeleteFeature deleteFeature = featureProvider.getDeleteFeature(deleteContext);
 		IContextButtonEntry ret = null;
 		if (deleteFeature != null) {
 			ret = new ContextButtonEntry(deleteFeature, deleteContext);
 			markAsDeleteContextEntry(ret);
 		}
 		return ret;
 	}
 
 	/**
 	 * Creates collapse button entry or expand button entry as indicated by
 	 * parameter collapse.
 	 * 
 	 * @param collapse
 	 *            creates a collapse button entry when true, otherwise a expand
 	 *            button entry
 	 * @param customFeature
 	 *            the custom feature
 	 * @param customContext
 	 *            the custom context
 	 * @return the created collapse button
 	 */
 	public static IContextButtonEntry createCollapseContextButton(boolean collapse, IFeature customFeature,
 			IContext customContext) {
 		IContextButtonEntry ret = null;
 		ret = new ContextButtonEntry(customFeature, customContext);
 		if (collapse) {
 			ret.setText(Messages.ContextEntryHelper_10_xfld);
 			ret.setDescription(Messages.ContextEntryHelper_11_xfld);
 			ret.setIconId(IPlatformImageConstants.IMG_EDIT_COLLAPSE);
 		} else {
 			ret.setText(Messages.ContextEntryHelper_12_xfld);
 			ret.setDescription(Messages.ContextEntryHelper_13_xfld);
 			ret.setIconId(IPlatformImageConstants.IMG_EDIT_EXPAND);
 		}
 		return ret;
 	}
 
 	/**
 	 * Marks the given context entry with update figure.
 	 * 
 	 * @param entry
 	 *            the context entry
 	 */
 	public static void markAsUpdateContextEntry(IContextEntry entry) {
 		if (entry != null) {
 			setTextAndDescription(entry, Messages.ContextEntryHelper_0_xfld, Messages.ContextEntryHelper_1_xfld);
 			entry.setIconId(IPlatformImageConstants.IMG_EDIT_REFRESH);
 		}
 	}
 
 	/**
 	 * Marks the given context entry with remove figure from diagram.
 	 * 
 	 * @param entry
 	 *            the context entry
 	 */
 	public static void markAsRemoveContextEntry(IContextEntry entry) {
 		if (entry != null) {
 			setTextAndDescription(entry, Messages.ContextEntryHelper_2_xfld, Messages.ContextEntryHelper_3_xfld);
 			entry.setIconId(IPlatformImageConstants.IMG_EDIT_REMOVE);
 		}
 	}
 
 	/**
 	 * Marks the given context entry with delete from model.
 	 * 
 	 * @param entry
 	 *            the context entry
 	 */
 	public static void markAsDeleteContextEntry(IContextEntry entry) {
 		if (entry != null) {
 			setTextAndDescription(entry, Messages.ContextEntryHelper_4_xfld, Messages.ContextEntryHelper_5_xfld);
 			entry.setIconId(IPlatformImageConstants.IMG_EDIT_DELETE);
 		}
 	}
 
 	public static final String NAME = Messages.ContextEntryHelper_5_xfld;
 
 	/**
 	 * Marks the given context entry with collapse or expand figure.
 	 * 
 	 * @param entry
 	 *            the context entry
 	 * @param collapse
 	 *            mark with collapse when true, otherwise with expand
 	 */
 	public static void markAsCollapseContextEntry(IContextEntry entry, boolean collapse) {
 		if (entry != null) {
 			if (collapse) {
 				entry.setText(Messages.ContextEntryHelper_6_xfld);
 				entry.setDescription(Messages.ContextEntryHelper_7_xfld);
 				entry.setIconId(IPlatformImageConstants.IMG_EDIT_COLLAPSE);
 			} else {
 				entry.setText(Messages.ContextEntryHelper_8_xfld);
 				entry.setDescription(Messages.ContextEntryHelper_9_xfld);
 				entry.setIconId(IPlatformImageConstants.IMG_EDIT_EXPAND);
 			}
 		}
 	}
 
 	private static void setTextAndDescription(IContextEntry entry, String defaultText, String defaultDescription) {
 		String entryText = defaultText;
 		String entryDescription = defaultDescription;
 		IFeature f = entry.getFeature();
 		if (f != null) {
 			if (f.getName() != null) {
 				entryText = f.getName();
 			}
 			if (f.getDescription() != null) {
 				entryDescription = f.getDescription();
 			}
 		}
 		entry.setText(entryText);
 		entry.setDescription(entryDescription);
 	}
 }
