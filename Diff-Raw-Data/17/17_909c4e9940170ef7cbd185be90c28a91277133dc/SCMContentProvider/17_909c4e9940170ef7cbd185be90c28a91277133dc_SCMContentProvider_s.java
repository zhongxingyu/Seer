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
 package org.eclipse.emf.emfstore.internal.client.ui.views.scm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementIdToEObjectMapping;
import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESChangePackageImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.CompositeOperation;
import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 
 /**
  * Content provider for the scm views.
  * 
  * @author emueller
  */
 public class SCMContentProvider extends AdapterFactoryContentProvider implements
 	ITreeContentProvider {
 
 	private boolean showRootNodes = true;
 	private boolean reverseNodes = true;
 	private Map<ChangePackage, VirtualNode<AbstractOperation>> changePackageToFilteredMapping;
 	private Map<ChangePackage, List<Object>> changePackageToNonFilteredMapping;
 	private ModelElementIdToEObjectMapping idToEObjectMapping;
 
 	/**
 	 * Default constructor.
 	 * 
 	 */
 	public SCMContentProvider() {
 		super(new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 		this.changePackageToFilteredMapping = new LinkedHashMap<ChangePackage, VirtualNode<AbstractOperation>>();
 		this.changePackageToNonFilteredMapping = new LinkedHashMap<ChangePackage, List<Object>>();
 	}
 
 	/**
 	 * @param idToEObjectMapping
 	 *            a mapping from IDs to EObjects that is necessary to resolve
 	 *            deleted EObjects
 	 */
 	public SCMContentProvider(
 		ModelElementIdToEObjectMapping idToEObjectMapping) {
 		this();
 		this.idToEObjectMapping = idToEObjectMapping;
 	}
 
 	/**
 	 * Sets the flag to reverse the order of the nodes. Default value is true -
 	 * i.e. the more recent operations are on top.
 	 * 
 	 * @param reverseNodes
 	 *            the new value
 	 */
 	public void setReverseNodes(boolean reverseNodes) {
 		this.reverseNodes = reverseNodes;
 	}
 
 	/**
 	 * Returns if the nodes should be reversed.
 	 * 
 	 * @return true if the nodes should be reversed in order
 	 */
 	public boolean isReverseNodes() {
 		return reverseNodes;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object[] getElements(Object object) {
 
 		if (object instanceof List<?> && showRootNodes) {
 
 			List<?> list = (List<?>) object;
 			List<Object> result = new ArrayList<Object>(list.size());
 			result.addAll(list);
 			return ((List<?>) result).toArray();
 
 		} else if (object instanceof List<?>) {
 			// valid inputs are a list of HistoryInfos,
 			// a list of operations as well as a list
 			// of ChangePackages
 			List<?> list = (List<?>) object;
 
 			if (list.size() == 0) {
 				return list.toArray();
 			}
 
 			List<Object> result = new ArrayList<Object>(list.size());
 
 			if (isListOf(list, HistoryInfo.class)) {
 				for (HistoryInfo info : (List<HistoryInfo>) list) {
 					if (info.getChangePackage() != null) {
 						result.addAll(getReversedOperations(info
 							.getChangePackage()));
 					}
 				}
 			} else if (isListOf(list, AbstractOperation.class)) {
 				FilteredOperationsResult filteredOpsResult = new FilterOperations(
 					idToEObjectMapping).filter(list.toArray());
 
 				result.addAll(filteredOpsResult.getNonFiltered());
 
 				if (filteredOpsResult.getFilteredOperations().size() > 0) {
 					VirtualNode<AbstractOperation> node = new VirtualNode<AbstractOperation>(
 						filteredOpsResult.getFilteredOperations());
 					result.add(node);
 				}
 			} else {
 				for (ChangePackage changePackage : (List<ChangePackage>) list) {
 					result.addAll(getReversedOperations(changePackage));
 				}
 			}
 
 			return result.toArray();
 
 		} else if (object instanceof EObject) {
 			return new Object[] { object };
 		}
 
 		return super.getElements(object);
 	}
 
 	private List<AbstractOperation> getReversedOperations(
 		ChangePackage changePackage) {
 		List<AbstractOperation> ops = new ArrayList<AbstractOperation>(
 			changePackage.getOperations());
 		Collections.reverse(ops);
 		return ops;
 	}
 
 	private boolean isListOf(List<?> list, Class<? extends EObject> clazz) {
 		Object firstElement = list.get(0);
 
 		return clazz.isInstance(firstElement);
 	}
 
 	private void filter(ChangePackage changePackage, Object[] input,
 		Class<? extends EObject> clazz) {
 
 		// check whether we already filtered this change package
 		if (changePackageHasBeenFiltered(changePackage)) {
 			return;
 		}
 
 		FilteredOperationsResult result = new FilterOperations(
 			idToEObjectMapping, clazz).filter(input);
 		VirtualNode<AbstractOperation> node = new VirtualNode<AbstractOperation>(
 			result.getFilteredOperations());
 		changePackageToNonFilteredMapping.put(changePackage,
												result.getNonFiltered());
 		changePackageToFilteredMapping.put(changePackage, node);
 	}
 
 	private boolean changePackageHasBeenFiltered(ChangePackage changePackage) {
 		return changePackageToNonFilteredMapping.containsKey(changePackage);
 	}
 
 	@Override
 	public boolean hasChildren(Object object) {
 		return getChildren(object).length > 0;
 	}
 
 	@Override
 	public Object[] getChildren(Object object) {
 
 		if (object instanceof HistoryInfo) {
 			HistoryInfo historyInfo = (HistoryInfo) object;
 			return getChildren(historyInfo.getChangePackage());
		} else if (object instanceof ESChangePackage) {
 
			ChangePackage changePackage = ((ESChangePackageImpl) object)
				.toInternalAPI();
 
 			filter(changePackage, super.getChildren(object), LogMessage.class);

			List<Object> result = new ArrayList<Object>();
 			result.addAll(changePackageToNonFilteredMapping.get(changePackage));
 			VirtualNode<AbstractOperation> node = changePackageToFilteredMapping
 				.get(changePackage);
 
 			if (node.getContent().size() > 0) {
 				result.add(node);
 			}
 
 			return result.toArray();
 
 		} else if (object instanceof VirtualNode<?>) {
 			return ((VirtualNode<?>) object).getContent().toArray();
 		} else if (object instanceof CompositeOperation) {
 			return ((CompositeOperation) object).getSubOperations().toArray();
 		}
 
 		return super.getChildren(object);
 	}
 
 	/**
 	 * Whether to show root nodes.
 	 * 
 	 * @return true, if root nodes are shown, false otherwise
 	 */
 	public boolean isShowRootNodes() {
 		return showRootNodes;
 	}
 
 	/**
 	 * Determines whether root nodes are shown.
 	 * 
 	 * @param showRootNodes
 	 *            if true, root nodes will be shown
 	 */
 	public void setShowRootNodes(boolean showRootNodes) {
 		this.showRootNodes = showRootNodes;
 	}
 
 }
