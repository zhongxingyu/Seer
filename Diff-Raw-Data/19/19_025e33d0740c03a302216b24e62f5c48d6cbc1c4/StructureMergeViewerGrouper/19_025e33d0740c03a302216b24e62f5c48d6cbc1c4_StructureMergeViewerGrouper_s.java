 /*******************************************************************************
 * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.eventbus.EventBus;
 
 import java.util.List;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 
 /**
  * This class will be used by the EMF Compare UI to group differences together in the structural differences
  * tree viewer.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  * @since 3.0
  */
 public final class StructureMergeViewerGrouper {
 	/**
 	 * The currently selected group provider. This is what will compute and give us the groups we need to
 	 * display.
 	 */
 	private IDifferenceGroupProvider provider;
 
 	/** List of all TreeViewers on which this grouper is applied. */
 	private List<TreeViewer> viewers = Lists.newArrayList();
 
 	/** The {@link EventBus} associated with this grouper. */
 	private EventBus eventBus;
 
 	/**
 	 * Constructs the difference grouper.
 	 * 
 	 * @param eventBus
 	 *            The {@link EventBus} which will be associated with this difference grouper.
 	 */
 	public StructureMergeViewerGrouper(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 
 	/**
 	 * This will be called internally by the ComparisonNode in order to know how it should group its
 	 * differences.
 	 * 
 	 * @param comparison
 	 *            The comparison which differences need to be grouped.
 	 * @return The collection of groups we could retrieve from the currently selected group provider. Empty
 	 *         {@link Iterable} if we have no group provider set.
 	 */
 	public Iterable<? extends IDifferenceGroup> getGroups(final Comparison comparison) {
 		if (provider == null) {
 			return ImmutableList.of();
 		}
 
		final Iterable<? extends IDifferenceGroup> groups = provider.getGroups(comparison);
		final Iterable<? extends IDifferenceGroup> filteredGroups = Iterables.filter(groups,
				new NonEmptyGroup());
 		return filteredGroups;
 	}
 
 	/**
 	 * Sets the instance that will provide the groups to be displayed in the structural differences view.
 	 * 
 	 * @param provider
 	 *            The provider that will be use to compute the groups that are to be displayed in the UI.
 	 */
 	public void setProvider(IDifferenceGroupProvider provider) {
 		if (this.provider != provider) {
 			this.provider = provider;
 			eventBus.post(provider);
 			refreshViewers();
 		}
 	}
 
 	/**
 	 * Refreshes the viewers registered with this grouper. Will try and conserve the expanded tree paths when
 	 * possible.
 	 */
 	private void refreshViewers() {
 		for (TreeViewer viewer : viewers) {
 			final TreePath[] paths = viewer.getExpandedTreePaths();
 			viewer.refresh();
 			viewer.setExpandedTreePaths(paths);
 		}
 	}
 
 	/**
 	 * Install this grouper on the given viewer.
 	 * <p>
 	 * Note that this will also install a dispose listener on that viewer in order to remove the grouper
 	 * whenever the viewer is disposed.
 	 * </p>
 	 * 
 	 * @param viewer
 	 *            The viewer on which the grouper will be installed.
 	 */
 	public void install(final TreeViewer viewer) {
 		viewer.getTree().addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				uninstall(viewer);
 			}
 		});
 		viewers.add(viewer);
 	}
 
 	/**
 	 * Uninstall this grouper from the given viewer.
 	 * 
 	 * @param viewer
 	 *            The viewer from which the grouper should be removed.
 	 */
 	public void uninstall(TreeViewer viewer) {
 		viewers.remove(viewer);
 	}
 
 	/**
 	 * This predicate will be used to filter the empty groups out of the displayed list.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static class NonEmptyGroup implements Predicate<IDifferenceGroup> {
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see com.google.common.base.Predicate#apply(java.lang.Object)
 		 */
 		public boolean apply(IDifferenceGroup input) {
 			return input != null && !Iterables.isEmpty(input.getDifferences());
 		}
 	}
 }
