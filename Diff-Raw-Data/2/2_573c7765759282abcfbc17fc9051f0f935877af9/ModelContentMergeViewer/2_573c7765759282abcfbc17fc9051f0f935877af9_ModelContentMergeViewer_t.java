 /*******************************************************************************
  * Copyright (c) 2006, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.viewer.content;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.merge.IMerger;
 import org.eclipse.emf.compare.diff.merge.service.MergeFactory;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSetSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.DiffResourceSet;
 import org.eclipse.emf.compare.diff.metamodel.util.DiffAdapterFactory;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.MatchResourceSet;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.EMFCompareUIMessages;
 import org.eclipse.emf.compare.ui.EMFCompareUIPlugin;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ICompareInputDetailsProvider;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.TypedElementWrapper;
 import org.eclipse.emf.compare.ui.internal.ModelComparator;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.viewer.content.part.AbstractCenterPart;
 import org.eclipse.emf.compare.ui.viewer.content.part.IModelContentMergeViewerTab;
 import org.eclipse.emf.compare.ui.viewer.content.part.ModelContentMergeTabFolder;
 import org.eclipse.emf.compare.ui.viewer.content.part.ModelContentMergeTabItem;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 
 /**
  * Compare and merge viewer with two side-by-side content areas and an optional content area for the ancestor.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ModelContentMergeViewer extends ContentMergeViewer {
 	/** Name of the bundle resources property file. */
 	public static final String BUNDLE_NAME = "org.eclipse.emf.compare.ui.viewer.content.ModelMergeViewerResources"; //$NON-NLS-1$
 
 	/** Width to affect to the center area. */
 	public static final int CENTER_WIDTH = 34;
 
 	/** Keeps references to the colors to use when drawing differences markers. */
 	/* package */static Map<String, RGB> colors = new EMFCompareMap<String, RGB>();
 
 	/**
 	 * Indicates that the diff markers should be drawn. This allows defining a threshold to avoid too long
 	 * drawing times.
 	 */
 	private static boolean drawDiffMarkers;
 
 	/** Keeps track of the currently selected tab for this viewer part. */
 	protected int activeTabIndex;
 
 	/** Ancestor part of the three possible parts of this content viewer. */
 	protected ModelContentMergeTabFolder ancestorPart;
 
 	/** Keeps track of the current diff Selection. */
 	protected final List<DiffElement> currentSelection = new ArrayList<DiffElement>();
 
 	/** Left of the three possible parts of this content viewer. */
 	protected ModelContentMergeTabFolder leftPart;
 
 	/** Right of the three possible parts of this content viewer. */
 	protected ModelContentMergeTabFolder rightPart;
 
 	/**
 	 * {@link CompareConfiguration} controls various aspect of the GUI elements. This will keep track of the
 	 * one used to created this compare editor.
 	 * 
 	 * @since 1.1
 	 */
 	protected final CompareConfiguration configuration;
 
 	/**
 	 * Indicates that this is a three-way comparison.
 	 * 
 	 * @since 1.1
 	 */
 	protected boolean isThreeWay;
 
 	/**
 	 * this is the "center" part of the content merge viewer where we handle all the drawing operations.
 	 */
 	private AbstractCenterPart canvas;
 
 	/**
 	 * This is the action we instantiate to handle the {@link DiffElement}s merge from the left model to the
 	 * right model.
 	 */
 	private Action copyDiffLeftToRight;
 
 	/**
 	 * This is the action we instantiate to handle the {@link DiffElement}s merge from the right model to the
 	 * left model.
 	 */
 	private Action copyDiffRightToLeft;
 
 	/**
 	 * Indicates that the left model has been modified since opening. Will allow us to prompt the user to save
 	 * this model.
 	 */
 	private boolean leftDirty;
 
 	/** This listener will be registered for notifications against all tab folders. */
 	private EditorPartListener partListener;
 
 	/**
 	 * This will listen for changes made on this plug-in's
 	 * {@link org.eclipse.jface.preference.PreferenceStore} to update the GUI colors as needed.
 	 */
 	private final IPropertyChangeListener preferenceListener;
 
 	/**
 	 * Indicates that the right model has been modified since opening. Will allow us to prompt the user to
 	 * save this model.
 	 */
 	private boolean rightDirty;
 
 	/**
 	 * This will listen for changes of the {@link CompareConfiguration} concerning the structure's input and
 	 * selection.
 	 */
 	private final IPropertyChangeListener structureSelectionListener;
 
 	/**
 	 * Creates a new model content merge viewer and initializes it.
 	 * 
 	 * @param parent
 	 *            Parent composite for this viewer.
 	 * @param config
 	 *            The configuration object.
 	 */
 	public ModelContentMergeViewer(Composite parent, CompareConfiguration config) {
 		super(SWT.NONE, ResourceBundle.getBundle(BUNDLE_NAME), config);
 		configuration = config;
 		buildControl(parent);
 		updatePreferences();
 
 		final IMergeViewerContentProvider contentProvider = createMergeViewerContentProvider();
 		if (contentProvider != null) {
 			setContentProvider(contentProvider);
 		} else {
 			// Fall back to default
 			setContentProvider(new ModelContentMergeContentProvider(configuration));
 		}
 
 		// disables diff copy from either side
 		switchCopyState(false);
 
 		structureSelectionListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EMFCompareConstants.PROPERTY_STRUCTURE_SELECTION)) {
 					final List<?> elements = (List<?>)event.getNewValue();
 					// We'll remove all diffgroups without subDiffs from the selection
 					final List<DiffElement> selectedDiffs = new ArrayList<DiffElement>();
 					for (int i = 0; i < elements.size(); i++) {
 						if (elements.get(i) instanceof DiffElement
 								&& !(elements.get(i) instanceof DiffGroup && ((DiffGroup)elements.get(i))
 										.getSubDiffElements().size() == 0)) {
 							selectedDiffs.add((DiffElement)elements.get(i));
 						}
 					}
 					setSelection(selectedDiffs);
 				}
 			}
 		};
 		configuration.addPropertyChangeListener(structureSelectionListener);
 
 		preferenceListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().matches(".*(color|differences)")) { //$NON-NLS-1$
 					updatePreferences();
 				}
 			}
 		};
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
 	}
 
 	/**
 	 * Returns the color identified by the given key in {@link #colors}.
 	 * 
 	 * @param key
 	 *            Key of the color to return.
 	 * @return The color identified by the given key in the map.
 	 */
 	public static final RGB getColor(String key) {
 		return colors.get(key);
 	}
 
 	/**
 	 * Returns <code>True</code> if the trees and center have to draw markers over the differences.
 	 * 
 	 * @return <code>True</code> if the trees and center have to draw markers over the differences,
 	 *         <code>False</code> otherwise.
 	 */
 	public static boolean shouldDrawDiffMarkers() {
 		return drawDiffMarkers;
 	}
 
 	/**
 	 * Returns the center {@link Canvas} appearing between the viewer parts.
 	 * 
 	 * @return The center {@link Canvas}.
 	 */
 	public Canvas getCenterPart() {
 		if (canvas == null && !getControl().isDisposed()) {
 			canvas = getCenterCanvas();
 		}
 		if (canvas != null) {
 			canvas.moveAbove(null);
 		}
 		return canvas;
 	}
 
 	/**
 	 * Builds the {@link CenterCanvas}. This method is useful to be overridden.
 	 * 
 	 * @return The {@link CenterCanvas}.
 	 * @since 1.2
 	 */
 	protected CenterCanvas getCenterCanvas() {
 		return new CenterCanvas((Composite)getControl());
 	}
 
 	/**
 	 * Returns the compare configuration of this viewer.
 	 * 
 	 * @return The compare configuration of this viewer.
 	 */
 	public CompareConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setInput(Object input) {
 		// This would be a lot faster if the comparison wasn't replayed each time, yet we cannot rely on the
 		// HistoryItem interface ; replace with => history does not make use of this interface.
 		if (input instanceof ICompareInput && ((ICompareInput)input).getAncestor() != null) {
 			isThreeWay = true;
 		}
 		final ModelComparator comparator;
 		if (input instanceof ICompareInput) {
 			comparator = ModelComparator.getComparator(configuration, (ICompareInput)input);
 		} else {
 			comparator = ModelComparator.getComparator(configuration);
 		}
 		if (input instanceof ComparisonResourceSnapshot) {
 			final ComparisonResourceSnapshot snapshot = (ComparisonResourceSnapshot)input;
 			super.setInput(createModelCompareInput(comparator, snapshot));
 		} else if (input instanceof ComparisonResourceSetSnapshot) {
 			final ComparisonResourceSetSnapshot snapshot = (ComparisonResourceSetSnapshot)input;
 			super.setInput(createModelCompareInput(comparator, snapshot));
 		} else if (input instanceof ModelCompareInput) {
 			// if there is already a ModelCompareInput provided, no reloading of resources should be done.
 			super.setInput(input);
 		} else if (input instanceof ICompareInput) {
 			comparator.loadResources((ICompareInput)input);
 			final ComparisonSnapshot snapshot = comparator.compare(configuration);
 			super.setInput(createModelCompareInput(comparator, snapshot));
 			configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snapshot);
 		} else {
 			super.setInput(input);
 		}
 	}
 
 	/**
 	 * Creates the {@link ModelCompareInput} for this particular viewer.
 	 * 
 	 * @param provider
 	 *            The input provider instance that is in charge of this comparison.
 	 * @param snapshot
 	 *            Snapshot describing the current comparison.
 	 * @return The prepared {@link ModelCompareInput} for this particular viewer.
 	 * @since 1.1
 	 */
 	protected ModelCompareInput createModelCompareInput(ICompareInputDetailsProvider provider,
 			ComparisonSnapshot snapshot) {
 		if (snapshot instanceof ComparisonResourceSetSnapshot) {
 			return new ModelCompareInput(((ComparisonResourceSetSnapshot)snapshot).getMatchResourceSet(),
 					((ComparisonResourceSetSnapshot)snapshot).getDiffResourceSet(), provider);
 		}
 		return new ModelCompareInput(((ComparisonResourceSnapshot)snapshot).getMatch(),
 				((ComparisonResourceSnapshot)snapshot).getDiff(), provider);
 	}
 
 	/**
 	 * Sets the parts' tree selection given the {@link DiffElement} to select.
 	 * 
 	 * @param diff
 	 *            {@link DiffElement} backing the current selection.
 	 */
 	public void setSelection(DiffElement diff) {
 		final List<DiffElement> diffs = new ArrayList<DiffElement>();
 		diffs.add(diff);
 		setSelection(diffs);
 	}
 
 	/**
 	 * Sets the parts' tree selection given the list of {@link DiffElement}s to select.
 	 * 
 	 * @param diffs
 	 *            {@link DiffElement} backing the current selection.
 	 */
 	public void setSelection(List<DiffElement> diffs) {
 		currentSelection.clear();
 		if (diffs.size() > 0) {
 			currentSelection.addAll(diffs);
 			if (leftPart != null) {
 				leftPart.navigateToDiff(diffs);
 			}
 			if (rightPart != null) {
 				rightPart.navigateToDiff(diffs);
 			}
 			if (isThreeWay) {
 				ancestorPart.navigateToDiff(diffs.get(0));
 			}
 			switchCopyState(true);
 		}
 	}
 
 	/**
 	 * Redraws this viewer.
 	 */
 	public void update() {
 		if (isThreeWay) {
 			ancestorPart.layout();
 		}
 		rightPart.layout();
 		leftPart.layout();
 		updateCenter();
 		updateToolItems();
 	}
 
 	/**
 	 * Redraws the center Control.
 	 */
 	public void updateCenter() {
 		if (getCenterPart() != null) {
 			getCenterPart().redraw();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#copy(boolean)
 	 */
 	@Override
 	protected void copy(boolean leftToRight) {
 		if (((ModelCompareInput)getInput()).getDiffAsList().size() > 0) {
 			// Avoids warnings "resource has changed ..."
 			setRightDirty(false);
 			setLeftDirty(false);
 
 			((ModelCompareInput)getInput()).copy(leftToRight);
 			if (((ModelCompareInput)getInput()).getDiff() instanceof DiffModel) {
 				final ComparisonResourceSnapshot snap = DiffFactory.eINSTANCE
 						.createComparisonResourceSnapshot();
 				snap.setDiff((DiffModel)((ModelCompareInput)getInput()).getDiff());
 				snap.setMatch((MatchModel)((ModelCompareInput)getInput()).getMatch());
 				configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 			} else {
 				final ComparisonResourceSetSnapshot snap = DiffFactory.eINSTANCE
 						.createComparisonResourceSetSnapshot();
 				snap.setDiffResourceSet((DiffResourceSet)((ModelCompareInput)getInput()).getDiff());
 				snap.setMatchResourceSet((MatchResourceSet)((ModelCompareInput)getInput()).getMatch());
 				configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 			}
 			leftDirty |= !leftToRight;
 			rightDirty |= leftToRight;
 			setLeftDirty(leftDirty);
 			setRightDirty(rightDirty);
 			update();
 		}
 	}
 
 	/**
 	 * Copies a list of {@link DiffElement}s or {@link DiffGroup}s in the given direction, then updates the
 	 * toolbar items states as well as the dirty state of both the left and the right models.
 	 * 
 	 * @param diffs
 	 *            {@link DiffElement Element}s to copy.
 	 * @param leftToRight
 	 *            Direction of the copy.
 	 * @see ModelCompareInput#copy(List, boolean)
 	 */
 	protected void copy(List<DiffElement> diffs, boolean leftToRight) {
 		if (diffs.size() > 0) {
 			// Avoids warnings "resource has changed ..."
 			setRightDirty(false);
 			setLeftDirty(false);
 
 			((ModelCompareInput)getInput()).copy(diffs, leftToRight);
 			if (((ModelCompareInput)getInput()).getDiff() instanceof DiffModel) {
 				final ComparisonResourceSnapshot snap = DiffFactory.eINSTANCE
 						.createComparisonResourceSnapshot();
 				snap.setDiff((DiffModel)((ModelCompareInput)getInput()).getDiff());
 				snap.setMatch((MatchModel)((ModelCompareInput)getInput()).getMatch());
 				configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 			} else {
 				final ComparisonResourceSetSnapshot snap = DiffFactory.eINSTANCE
 						.createComparisonResourceSetSnapshot();
 				snap.setDiffResourceSet((DiffResourceSet)((ModelCompareInput)getInput()).getDiff());
 				snap.setMatchResourceSet((MatchResourceSet)((ModelCompareInput)getInput()).getMatch());
 				configuration.setProperty(EMFCompareConstants.PROPERTY_CONTENT_INPUT_CHANGED, snap);
 			}
 			leftDirty |= !leftToRight && configuration.isLeftEditable();
 			rightDirty |= leftToRight && configuration.isRightEditable();
 			setLeftDirty(leftDirty);
 			setRightDirty(rightDirty);
 			update();
 		}
 	}
 
 	/**
 	 * Undoes the changes implied by the currently selected {@link DiffElement diff}.
 	 */
 	protected void copyDiffLeftToRight() {
 		if (currentSelection != null) {
 			copy(currentSelection, true);
 		}
 		currentSelection.clear();
 		switchCopyState(false);
 	}
 
 	/**
 	 * Applies the changes implied by the currently selected {@link DiffElement diff}.
 	 */
 	protected void copyDiffRightToLeft() {
 		if (currentSelection != null) {
 			copy(currentSelection, false);
 		}
 		currentSelection.clear();
 		switchCopyState(false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#createControls(Composite)
 	 */
 	@Override
 	protected void createControls(Composite composite) {
 		leftPart = createModelContentMergeTabFolder(composite, EMFCompareConstants.LEFT);
 		rightPart = createModelContentMergeTabFolder(composite, EMFCompareConstants.RIGHT);
 		ancestorPart = createModelContentMergeTabFolder(composite, EMFCompareConstants.ANCESTOR);
 
 		partListener = new EditorPartListener(leftPart, rightPart, ancestorPart);
 		leftPart.addCompareEditorPartListener(partListener);
 		rightPart.addCompareEditorPartListener(partListener);
 		ancestorPart.addCompareEditorPartListener(partListener);
 
 		createPropertiesSyncHandlers(leftPart, rightPart, ancestorPart);
 		createTreeSyncHandlers(leftPart, rightPart, ancestorPart);
 	}
 
 	/**
 	 * Creates and return our content provider.
 	 * 
 	 * @return The {@link IMergeViewerContentProvider content provider} for this merge viewer.
 	 * @since 1.1
 	 */
 	protected IMergeViewerContentProvider createMergeViewerContentProvider() {
 		return new ModelContentMergeContentProvider(configuration);
 	}
 
 	/**
 	 * Creates a new {@link ModelContentMergeTabFolder tab folder} for the specified <code>side</code> (left,
 	 * right or ancestor). Clients may override this method in order to create their custom part.
 	 * 
 	 * @param composite
 	 *            The parent {@link Composite} of the part to create.
 	 * @param side
 	 *            The side where the created part is going to be displayed.
 	 * @return The created tab folder.
 	 * @since 1.1
 	 */
 	protected ModelContentMergeTabFolder createModelContentMergeTabFolder(Composite composite, int side) {
 		return new ModelContentMergeTabFolder(this, composite, side);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#createToolItems(ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager tbm) {
 		// COPY DIFF LEFT TO RIGHT
 		if (getCompareConfiguration().isRightEditable()) {
 			copyDiffLeftToRight = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 					"action.CopyDiffLeftToRight.") { //$NON-NLS-1$
 				@Override
 				public void run() {
 					copyDiffLeftToRight();
 				}
 			};
 			final ActionContributionItem copyLeftToRightContribution = new ActionContributionItem(
 					copyDiffLeftToRight);
 			copyLeftToRightContribution.setVisible(true);
 			tbm.appendToGroup("merge", copyLeftToRightContribution); //$NON-NLS-1$
 		}
 		// COPY DIFF RIGHT TO LEFT
 		if (getCompareConfiguration().isLeftEditable()) {
 			copyDiffRightToLeft = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 					"action.CopyDiffRightToLeft.") { //$NON-NLS-1$
 				@Override
 				public void run() {
 					copyDiffRightToLeft();
 				}
 			};
 			final ActionContributionItem copyRightToLeftContribution = new ActionContributionItem(
 					copyDiffRightToLeft);
 			copyRightToLeftContribution.setVisible(true);
 			tbm.appendToGroup("merge", copyRightToLeftContribution); //$NON-NLS-1$
 		}
 		// NEXT DIFF
 		final Action nextDiff = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 				"action.NextDiff.") { //$NON-NLS-1$
 			@Override
 			public void run() {
 				navigate(true);
 			}
 		};
 		final ActionContributionItem nextDiffContribution = new ActionContributionItem(nextDiff);
 		nextDiffContribution.setVisible(true);
 		tbm.appendToGroup("navigation", nextDiffContribution); //$NON-NLS-1$
 		// PREVIOUS DIFF
 		final Action previousDiff = new AbstractCompareAction(ResourceBundle.getBundle(BUNDLE_NAME),
 				"action.PrevDiff.") { //$NON-NLS-1$
 			@Override
 			public void run() {
 				navigate(false);
 			}
 		};
 		final ActionContributionItem previousDiffContribution = new ActionContributionItem(previousDiff);
 		previousDiffContribution.setVisible(true);
 		tbm.appendToGroup("navigation", previousDiffContribution); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#fireSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	@Override
 	protected void fireSelectionChanged(final SelectionChangedEvent event) {
 		super.fireSelectionChanged(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#getContents(boolean)
 	 */
 	@Override
 	protected byte[] getContents(boolean left) {
 		byte[] contents = null;
 
 		final Object input;
 		if (left) {
 			input = ((IMergeViewerContentProvider)getContentProvider()).getLeftContent(getInput());
 		} else {
 			input = ((IMergeViewerContentProvider)getContentProvider()).getRightContent(getInput());
 		}
 
 		final Resource resource;
 		if (input instanceof TypedElementWrapper) {
 			resource = ((TypedElementWrapper)input).getObject().eResource();
		} else if (input instanceof List<?>) {
			resource = (Resource)((List<?>)input).get(0);
 		} else {
 			resource = (Resource)input;
 		}
 
 		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			resource.save(stream, null);
 			contents = stream.toByteArray();
 		} catch (final IOException e) {
 			EMFComparePlugin.log(e, false);
 		}
 
 		return contents;
 	}
 
 	/**
 	 * This will minimize the list of differences to the visible differences. Differences are considered
 	 * "visible" if {@link DiffAdapterFactory#shouldBeHidden(org.eclipse.emf.ecore.EObject)} returns false on
 	 * it.
 	 * 
 	 * @return {@link List} of the visible differences for this comparison.
 	 */
 	protected List<DiffElement> getVisibleDiffs() {
 		final List<DiffElement> diffs = ((ModelCompareInput)getInput()).getDiffAsList();
 		final List<DiffElement> visibleDiffs = new ArrayList<DiffElement>(diffs.size());
 
 		for (int i = 0; i < diffs.size(); i++) {
 			if (!DiffAdapterFactory.shouldBeHidden(diffs.get(i))) {
 				visibleDiffs.add(diffs.get(i));
 			}
 		}
 
 		return visibleDiffs;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		super.handleDispose(event);
 		configuration.removePropertyChangeListener(structureSelectionListener);
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
 		leftPart.removeCompareEditorPartListener(partListener);
 		leftPart.dispose();
 		leftPart = null;
 		rightPart.removeCompareEditorPartListener(partListener);
 		rightPart.dispose();
 		rightPart = null;
 		ancestorPart.removeCompareEditorPartListener(partListener);
 		ancestorPart.dispose();
 		ancestorPart = null;
 		canvas.dispose();
 		canvas = null;
 		currentSelection.clear();
 		ModelComparator.removeComparator(configuration);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#handleResizeAncestor(int, int, int, int)
 	 */
 	@Override
 	protected void handleResizeAncestor(int x, int y, int width, int height) {
 		ancestorPart.setBounds(x, y, width, height);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#handleResizeLeftRight(int, int, int, int)
 	 */
 	@Override
 	protected void handleResizeLeftRight(int x, int y, int leftWidth, int centerWidth, int rightWidth,
 			int height) {
 		if (getCenterPart() != null) {
 			getCenterPart().setBounds(leftWidth - CENTER_WIDTH / 2, y, CENTER_WIDTH, height);
 		}
 		leftPart.setBounds(x, y, leftWidth - CENTER_WIDTH / 2, height);
 		rightPart.setBounds(x + leftWidth + CENTER_WIDTH / 2, y, rightWidth - CENTER_WIDTH / 2, height);
 		update();
 	}
 
 	/**
 	 * Selects the next or previous {@link DiffElement} as compared to the currently selected one.
 	 * 
 	 * @param down
 	 *            <code>True</code> if we seek the next {@link DiffElement}, <code>False</code> for the
 	 *            previous.
 	 */
 	protected void navigate(boolean down) {
 		final List<DiffElement> diffs = getVisibleDiffs();
 		if (diffs.size() != 0) {
 			final DiffElement theDiff;
 			if (currentSelection.size() > 0 && !(currentSelection.get(0) instanceof DiffGroup)) {
 				theDiff = currentSelection.get(0);
 			} else if (diffs.size() == 1) {
 				theDiff = diffs.get(0);
 			} else if (down) {
 				theDiff = diffs.get(diffs.size() - 1);
 			} else {
 				theDiff = diffs.get(1);
 			}
 			for (int i = 0; i < diffs.size(); i++) {
 				if (diffs.get(i).equals(theDiff) && down) {
 					DiffElement next = diffs.get(0);
 					if (diffs.size() > i + 1) {
 						next = diffs.get(i + 1);
 					}
 					if (next != null && !DiffAdapterFactory.shouldBeHidden(next)) {
 						setSelection(next);
 						break;
 					}
 				} else if (diffs.get(i).equals(theDiff) && !down) {
 					DiffElement previous = diffs.get(diffs.size() - 1);
 					if (i > 0) {
 						previous = diffs.get(i - 1);
 					}
 					if (previous != null && !DiffAdapterFactory.shouldBeHidden(previous)) {
 						setSelection(previous);
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will enable or disable the toolbar's copy actions according to the given <code>boolean</code>. The
 	 * "copy diff left to right" action will be enabled if <code>enable</code> is <code>True</code>, but the
 	 * "copy diff right to left" action will only be activated if <code>enable</code> is <code>True</code> AND
 	 * the left model isn't a remote model.
 	 * 
 	 * @param enabled
 	 *            <code>True</code> if we seek to enable the actions, <code>False</code> otherwise.
 	 */
 	protected void switchCopyState(boolean enabled) {
 		final ModelComparator comparator = ModelComparator.getComparator(configuration);
 
 		boolean leftEditable = configuration.isLeftEditable();
 		if (comparator != null)
 			leftEditable = leftEditable && !comparator.isLeftRemote();
 		boolean rightEditable = configuration.isRightEditable();
 		if (comparator != null)
 			rightEditable = rightEditable && !comparator.isRightRemote();
 
 		boolean canCopyLeftToRight = true;
 		boolean canCopyRightToLeft = true;
 		if (currentSelection.size() == 1) {
 			final IMerger merger = MergeFactory.createMerger(currentSelection.get(0));
 			canCopyLeftToRight = merger.canUndoInTarget();
 			canCopyRightToLeft = merger.canApplyInOrigin();
 		}
 
 		if (copyDiffLeftToRight != null) {
 			copyDiffLeftToRight.setEnabled(rightEditable && enabled && canCopyLeftToRight);
 		}
 		if (copyDiffRightToLeft != null) {
 			copyDiffRightToLeft.setEnabled(leftEditable && enabled && canCopyRightToLeft);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#updateContent(Object, Object, Object)
 	 */
 	@Override
 	protected void updateContent(Object ancestor, Object left, Object right) {
 		Object ancestorObject = ancestor;
 		Object leftObject = left;
 		Object rightObject = right;
 		if (ancestorObject instanceof TypedElementWrapper) {
 			if (((TypedElementWrapper)ancestorObject).getObject() == null) {
 				ancestorObject = null;
 			} else {
 				ancestorObject = getInputObject((TypedElementWrapper)ancestorObject);
 			}
 		}
 		if (leftObject instanceof TypedElementWrapper) {
 			leftObject = getInputObject((TypedElementWrapper)leftObject);
 		}
 		if (rightObject instanceof TypedElementWrapper) {
 			rightObject = getInputObject((TypedElementWrapper)rightObject);
 		}
 
 		if (ancestorObject != null) {
 			ancestorPart.setInput(ancestorObject);
 		}
 		if (leftObject != null) {
 			leftPart.setInput(leftObject);
 		}
 		if (rightObject != null) {
 			rightPart.setInput(rightObject);
 		}
 		update();
 	}
 
 	/**
 	 * Updates the values of all the variables using preferences values.
 	 */
 	protected void updatePreferences() {
 		final IPreferenceStore comparePreferences = EMFCompareUIPlugin.getDefault().getPreferenceStore();
 		updateColors(comparePreferences);
 		updateDrawDiffMarkers(comparePreferences);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#updateToolItems()
 	 */
 	@Override
 	protected void updateToolItems() {
 		super.updateToolItems();
 		CompareViewerPane.getToolBarManager(getControl().getParent()).update(true);
 	}
 
 	/**
 	 * Takes care of the creation of the synchronization handlers for the properties tab of our viewer parts.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void createPropertiesSyncHandlers(ModelContentMergeTabFolder... parts) {
 		if (parts.length < 2) {
 			throw new IllegalArgumentException(
 					EMFCompareUIMessages.getString("ModelContentMergeViewer.illegalSync")); //$NON-NLS-1$
 		}
 
 		// horizontal synchronization
 		handleHSync(leftPart.getPropertyPart(), rightPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		handleHSync(ancestorPart.getPropertyPart(), rightPart.getPropertyPart(), leftPart.getPropertyPart());
 		handleHSync(rightPart.getPropertyPart(), leftPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		// Vertical synchronization
 		handleVSync(leftPart.getPropertyPart(), rightPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		handleVSync(rightPart.getPropertyPart(), leftPart.getPropertyPart(), ancestorPart.getPropertyPart());
 		handleVSync(ancestorPart.getPropertyPart(), rightPart.getPropertyPart(), leftPart.getPropertyPart());
 	}
 
 	/**
 	 * Takes care of the creation of the synchronization handlers for the tree tab of our viewer parts.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void createTreeSyncHandlers(ModelContentMergeTabFolder... parts) {
 		if (parts.length < 2) {
 			throw new IllegalArgumentException(
 					EMFCompareUIMessages.getString("ModelContentMergeViewer.illegalSync")); //$NON-NLS-1$
 		}
 
 		handleHSync(leftPart.getTreePart(), rightPart.getTreePart(), ancestorPart.getTreePart());
 		handleHSync(rightPart.getTreePart(), leftPart.getTreePart(), ancestorPart.getTreePart());
 		handleHSync(ancestorPart.getTreePart(), rightPart.getTreePart(), leftPart.getTreePart());
 	}
 
 	/**
 	 * This will return the actual Object that is to be used as input of the different tabs.
 	 * 
 	 * @param elementWrapper
 	 *            Element that we've been fed.
 	 * @return Actual Object that is to be used as input of the different tabs.
 	 */
 	private Object getInputObject(TypedElementWrapper elementWrapper) {
 		final Resource resource = elementWrapper.getObject().eResource();
 		if (resource != null) {
 			return resource.getResourceSet();
 		}
 		return elementWrapper;
 	}
 
 	/**
 	 * Allows synchronization of the properties viewports horizontal scrolling.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void handleHSync(IModelContentMergeViewerTab... parts) {
 		// inspired from TreeMergeViewer#hsynchViewport
 		final Scrollable scroll1 = (Scrollable)parts[0].getControl();
 		final Scrollable scroll2 = (Scrollable)parts[1].getControl();
 		final Scrollable scroll3;
 		if (parts.length > 2) {
 			scroll3 = (Scrollable)parts[2].getControl();
 		} else {
 			scroll3 = null;
 		}
 		final ScrollBar scrollBar1 = scroll1.getHorizontalBar();
 
 		scrollBar1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final int max = scrollBar1.getMaximum() - scrollBar1.getThumb();
 				double v = 0.0;
 				if (max > 0) {
 					v = (double)scrollBar1.getSelection() / (double)max;
 				}
 				if (scroll2.isVisible()) {
 					final ScrollBar scrollBar2 = scroll2.getHorizontalBar();
 					scrollBar2.setSelection((int)((scrollBar2.getMaximum() - scrollBar2.getThumb()) * v));
 				}
 				if (scroll3 != null && scroll3.isVisible()) {
 					final ScrollBar scrollBar3 = scroll3.getHorizontalBar();
 					scrollBar3.setSelection((int)((scrollBar3.getMaximum() - scrollBar3.getThumb()) * v));
 				}
 				if ("carbon".equals(SWT.getPlatform()) && getControl() != null //$NON-NLS-1$
 						&& !getControl().isDisposed()) {
 					getControl().getDisplay().update();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Allows synchronization of the viewports vertical scrolling.
 	 * 
 	 * @param parts
 	 *            The other parts to synchronize with.
 	 */
 	private void handleVSync(IModelContentMergeViewerTab... parts) {
 		// inspired from TreeMergeViewer#hsynchViewport
 		final Scrollable table1 = (Scrollable)parts[0].getControl();
 		final Scrollable table2 = (Scrollable)parts[1].getControl();
 		final Scrollable table3;
 		if (parts.length > 2) {
 			table3 = (Scrollable)parts[2].getControl();
 		} else {
 			table3 = null;
 		}
 		final ScrollBar scrollBar1 = table1.getVerticalBar();
 
 		scrollBar1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final int max = scrollBar1.getMaximum() - scrollBar1.getThumb();
 				double v = 0.0;
 				if (max > 0) {
 					v = (double)scrollBar1.getSelection() / (double)max;
 				}
 				if (table2.isVisible()) {
 					final ScrollBar scrollBar2 = table2.getVerticalBar();
 					scrollBar2.setSelection((int)((scrollBar2.getMaximum() - scrollBar2.getThumb()) * v));
 				}
 				if (table3 != null && table3.isVisible()) {
 					final ScrollBar scrollBar3 = table3.getVerticalBar();
 					scrollBar3.setSelection((int)((scrollBar3.getMaximum() - scrollBar3.getThumb()) * v));
 				}
 				if ("carbon".equals(SWT.getPlatform()) && getControl() != null //$NON-NLS-1$
 						&& !getControl().isDisposed()) {
 					getControl().getDisplay().update();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Updates the value of the colors as they are changed on the preference page.
 	 * 
 	 * @param comparePreferences
 	 *            Preference store where to retrieve our values.
 	 */
 	private void updateColors(IPreferenceStore comparePreferences) {
 		final RGB highlightColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR);
 		final RGB changedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR);
 		final RGB conflictingColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_CONFLICTING_COLOR);
 		final RGB addedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_ADDED_COLOR);
 		final RGB removedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_REMOVED_COLOR);
 		colors.put(EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR, highlightColor);
 		colors.put(EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR, changedColor);
 		colors.put(EMFCompareConstants.PREFERENCES_KEY_CONFLICTING_COLOR, conflictingColor);
 		colors.put(EMFCompareConstants.PREFERENCES_KEY_ADDED_COLOR, addedColor);
 		colors.put(EMFCompareConstants.PREFERENCES_KEY_REMOVED_COLOR, removedColor);
 	}
 
 	/**
 	 * Updates the value of the boolean indicating that we should ignore diff markers as it is changed on the
 	 * preference page.
 	 * 
 	 * @param comparePreferences
 	 *            Preference store where to retrieve our values.
 	 */
 	private void updateDrawDiffMarkers(IPreferenceStore comparePreferences) {
 		drawDiffMarkers = comparePreferences.getBoolean(EMFCompareConstants.PREFERENCES_KEY_DRAW_DIFFERENCES);
 	}
 
 	/**
 	 * Basic implementation of an {@link ICompareEditorPartListener}.
 	 */
 	private class EditorPartListener implements ICompareEditorPartListener {
 		/** Viewer parts this listener is registered for. */
 		private final ModelContentMergeTabFolder[] viewerParts;
 
 		/**
 		 * Instantiate this {@link EditorPartListener} given the left, right and ancestor viewer parts.
 		 * 
 		 * @param parts
 		 *            The viewer parts.
 		 */
 		public EditorPartListener(ModelContentMergeTabFolder... parts) {
 			viewerParts = parts;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#selectedTabChanged()
 		 */
 		public void selectedTabChanged(int newIndex) {
 			for (int i = 0; i < viewerParts.length; i++) {
 				viewerParts[i].setSelectedTab(newIndex);
 			}
 			updateCenter();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#selectionChanged()
 		 */
 		public void selectionChanged(SelectionChangedEvent event) {
 			fireSelectionChanged(event);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#updateCenter()
 		 */
 		public void updateCenter() {
 			ModelContentMergeViewer.this.updateCenter();
 		}
 	}
 
 	/**
 	 * An AbstractCenterPart which computes the graphical lines that it has to draw in relation to the visible
 	 * difference elements.
 	 * 
 	 * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
 	 * @since 1.2
 	 */
 	public class CenterCanvas extends AbstractCenterPart {
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param parent
 		 *            The parent composite.
 		 */
 		public CenterCanvas(Composite parent) {
 			super(parent);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.ui.viewer.content.part.AbstractCenterPart#doPaint(org.eclipse.swt.graphics.GC)
 		 */
 		@Override
 		public void doPaint(GC gc) {
 			if (!ModelContentMergeViewer.shouldDrawDiffMarkers() || getInput() == null)
 				return;
 			final List<ModelContentMergeTabItem> leftVisible = leftPart.getVisibleElements();
 			final List<ModelContentMergeTabItem> rightVisible = rightPart.getVisibleElements();
 			final List<DiffElement> visibleDiffs = retainVisibleDiffs(leftVisible, rightVisible);
 			// we don't clear selection when the last diff is merged so this could happen
 			if (currentSelection.size() > 0 && currentSelection.get(0).eContainer() != null) {
 				visibleDiffs.addAll(currentSelection);
 			}
 			for (final DiffElement diff : visibleDiffs) {
 				if (hasLineBeDrawn(diff)) {
 					final ModelContentMergeTabItem leftUIItem = leftPart.getUIItem(diff);
 					final ModelContentMergeTabItem rightUIItem = rightPart.getUIItem(diff);
 					drawLine(gc, leftUIItem, rightUIItem);
 				}
 			}
 		}
 
 		/**
 		 * Check if the line has to be drawn in relation to the difference element.
 		 * 
 		 * @param diff
 		 *            the difference element.
 		 * @return true if it has to be drawn, false otherwise.
 		 */
 		protected boolean hasLineBeDrawn(final DiffElement diff) {
 			if (!(diff instanceof DiffGroup)) {
 				final ModelContentMergeTabItem leftUIItem = leftPart.getUIItem(diff);
 				final ModelContentMergeTabItem rightUIItem = rightPart.getUIItem(diff);
 				return leftUIItem != null && rightUIItem != null;
 			}
 			return false;
 		}
 
 	}
 }
