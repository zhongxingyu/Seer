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
 package org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.impl;
 
 import static com.google.common.base.Predicates.alwaysTrue;
 
 import com.google.common.collect.ImmutableList;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.BasicDifferenceGroupImpl;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroup;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 
 /**
  * This implementation of a {@link IDifferenceGroupProvider} will be used as the default group provider.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class DefaultGroupProvider extends AdapterImpl implements IDifferenceGroupProvider {
 
 	/** The unique group provided by this provider. */
 	private IDifferenceGroup group;
 
	/** The comparison object. */
	private Comparison comp;

 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#getGroups(org.eclipse.emf.compare.Comparison)
 	 */
 	public Collection<? extends IDifferenceGroup> getGroups(Comparison comparison) {
		if (group == null || !comparison.equals(comp)) {
			this.comp = comparison;
 			group = new BasicDifferenceGroupImpl(comparison, alwaysTrue());
 		}
 		return ImmutableList.of(group);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
 	 */
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type == IDifferenceGroupProvider.class;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#getLabel()
 	 */
 	public String getLabel() {
 		return "Default";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#setLabel(java.lang.String)
 	 */
 	public void setLabel(String label) {
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#defaultSelected()
 	 */
 	public boolean defaultSelected() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#setDefaultSelected(boolean)
 	 */
 	public void setDefaultSelected(boolean defaultSelected) {
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider#isEnabled(org
 	 *      .eclipse.emf.compare.scope.IComparisonScope, org.eclipse.emf.compare.Comparison)
 	 */
 	public boolean isEnabled(IComparisonScope scope, Comparison comparison) {
 		return true;
 	}
 }
