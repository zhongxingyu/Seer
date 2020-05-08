 /*******************************************************************************
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ui.contentmergeviewer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareViewerPane;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.AddModelElement;
 import org.eclipse.emf.compare.diff.DiffElement;
 import org.eclipse.emf.compare.diff.DiffModel;
 import org.eclipse.emf.compare.diff.ModelInputSnapshot;
 import org.eclipse.emf.compare.diff.RemoveModelElement;
 import org.eclipse.emf.compare.diff.generic.DiffMaker;
 import org.eclipse.emf.compare.match.Match2Elements;
 import org.eclipse.emf.compare.match.MatchModel;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.compare.merge.api.AbstractMerger;
 import org.eclipse.emf.compare.merge.api.MergeFactory;
 import org.eclipse.emf.compare.merge.service.MergeService;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.EMFCompareUIPlugin;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.TypedElementWrapper;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.compare.ui.viewerpart.ModelContentMergeViewerPart;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Compare and merge viewer with two side-by-side content areas and an optional content area for the ancestor.
  * getKind
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class ModelContentMergeViewer extends ContentMergeViewer {
 	/** Name of the bundle resources property file. */
 	public static final String BUNDLE_NAME = "org.eclipse.emf.compare.ui.contentmergeviewer.ModelMergeViewerResources"; //$NON-NLS-1$
 
 	/** ID of the Tree tab of the {@link ModelContentMergeViewerPart}. */
 	public static final int TREE_TAB = 0;
 
 	/** ID of the Properties tab of the {@link ModelContentMergeViewerPart}. */
 	public static final int PROPERTIES_TAB = 1;
 
 	/** Width to affect to the center area. */
 	public static final int CENTER_WIDTH = 34;
 
 	private CompareConfiguration configuration;
 
 	private RGB highlightColor;
 
 	private RGB changedColor;
 
 	private RGB addedColor;
 
 	private RGB removedColor;
 
 	private ModelContentMergeViewerPart leftPart;
 
 	private ModelContentMergeViewerPart rightPart;
 
 	private ModelContentMergeViewerPart ancestorPart;
 
 	private AbstractBufferedCanvas canvas;
 
 	private DiffElement currentDiff;
 
 	private int selectedTab = TREE_TAB;
 
 	/**
 	 * Creates a new model content merge viewer and intializes it.
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
 		updateColors();
 
 		configuration.addPropertyChangeListener(new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EMFCompareConstants.PROPERTY_STRUCTURE_SELECTION)) {
 					Object selected = null;
 					if (event.getNewValue() instanceof IStructuredSelection) {
 						selected = ((IStructuredSelection)event.getNewValue()).getFirstElement();
 					}
 					if (selected instanceof DiffElement) {
 						currentDiff = (DiffElement)selected;
 						leftPart.navigateToDiff(currentDiff);
 						rightPart.navigateToDiff(currentDiff);
 					}
 				}
 			}
 		});
 
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
 				new IPropertyChangeListener() {
 					public void propertyChange(PropertyChangeEvent event) {
 						if (event.getProperty().endsWith("color")) { //$NON-NLS-1$
 							updateColors();
 						}
 					}
 				});
 	}
 
 	/**
 	 * Return <code>true</code> if this viewer should be showing the given match.
 	 * 
 	 * @param match
 	 *            Match element to be checked.
 	 * @return <code>True</code> if this viewer should show this match, <code>False</code> otherwise.
 	 */
 	public boolean shouldShow(Match2Elements match) {
 		final DiffElement diff = ((ModelCompareInput)getInput()).findDiffFromMatch(match);
 		return diff != null;
 	}
 
 	/**
 	 * Redraws this viewer.
 	 */
 	public void update() {
 		ancestorPart.layout();
 		rightPart.layout();
 		leftPart.layout();
 		updateCenter();
 	}
 
 	/**
 	 * Redraws the center Control.
 	 */
 	public void updateCenter() {
 		getCenterPart().redraw();
 	}
 
 	/**
 	 * Returns the center {@link Sash} appearing between the vewer parts.
 	 * 
 	 * @return The center {@link Sash}.
 	 */
 	public Canvas getCenterPart() {
 		if (canvas == null)
 			canvas = new AbstractBufferedCanvas((Composite)getControl()) {
 				public void doPaint(GC gc) {
 					// Draw lines on the left and right edges
 					gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
 					gc.drawLine(0, 0, 0, getBounds().height);
 					gc.drawLine(getBounds().width - 1, 0, getBounds().width - 1, getBounds().height);
 
 					for (final DiffElement diff : ((ModelCompareInput)getInput()).getDiffAsList()) {
 						EObject leftElement = EMFCompareEObjectUtils.getLeftElement(diff);
 						EObject rightElement = EMFCompareEObjectUtils.getRightElement(diff);
 						if (selectedTab == PROPERTIES_TAB) {
 							leftElement = diff;
 							rightElement = diff;
 						}
 						final Item leftItem = (Item)leftPart.find(leftElement);
 						final Item rightItem = (Item)rightPart.find(rightElement);
 						drawLine(gc, leftItem, rightItem, diff);
 					}
 				}
 			};
 		canvas.moveAbove(null);
 		return canvas;
 	}
 
 	/**
 	 * Returns the highlighting color.
 	 * 
 	 * @return The highlighting color.
 	 */
 	public RGB getHighlightColor() {
 		return highlightColor;
 	}
 
 	/**
 	 * Returns the changed element color.
 	 * 
 	 * @return The changed element color.
 	 */
 	public RGB getChangedColor() {
 		return changedColor;
 	}
 
 	/**
 	 * Returns the added element color.
 	 * 
 	 * @return The added element color.
 	 */
 	public RGB getAddedColor() {
 		return addedColor;
 	}
 
 	/**
 	 * Returns the removed element color.
 	 * 
 	 * @return The removed element color.
 	 */
 	public RGB getRemovedColor() {
 		return removedColor;
 	}
 
 	/**
 	 * Sets the value of the currently selected tab.
 	 * 
 	 * @param newIndex
 	 *            Newly selected tab.
 	 */
 	public void setSelectedTab(int newIndex) {
 		selectedTab = newIndex;
 	}
 
 	/**
 	 * Gets the value of the currently selected tab.
 	 * 
 	 * @return Index of the currently selected tab.
 	 */
 	public int getSelectedTab() {
 		return selectedTab;
 	}
 
 	/**
 	 * Sets the parts' tree selection given the {@link DiffElement} to select and the identifier of the side
 	 * which triggered the selection change.
 	 * 
 	 * @param diff
 	 *            {@link DiffElement} backing the current selection.
 	 * @param sourceSide
 	 *            {@link EMFCompareConstants#LEFT} if the selection change has been triggered by the left
 	 *            side, {@link EMFCompareConstants#RIGHT} if triggered by the right side, <code>-1</code> if
 	 *            triggered by a structure selection changed.
 	 */
 	public void setTreeSelection(DiffElement diff, int sourceSide) {
 		if (selectedTab == TREE_TAB) {
 			// Finds the TreeItems to select
 			final TreeItem leftItem = (TreeItem)leftPart.find(EMFCompareEObjectUtils.getLeftElement(diff));
 			final TreeItem rightItem = (TreeItem)rightPart.find(EMFCompareEObjectUtils.getRightElement(diff));
 
 			// Sets the TreeItems background to the highlighted color
 			leftItem.setBackground(new Color(leftItem.getDisplay(), getHighlightColor()));
 			rightItem.setBackground(new Color(rightItem.getDisplay(), getHighlightColor()));
 
 			// We expand the tree item in the cases of Add or Remove elements.
 			if (diff instanceof AddModelElement
 					&& (sourceSide == -1 || sourceSide == EMFCompareConstants.RIGHT)) {
 				leftItem.setExpanded(true);
 			} else if (diff instanceof RemoveModelElement
 					&& (sourceSide == -1 || sourceSide == EMFCompareConstants.LEFT)) {
 				rightItem.setExpanded(true);
 			}
 
 			// Sets the trees selection and updates the viewer
 			leftPart.setSelection(new StructuredSelection(leftItem.getData()));
 			rightPart.setSelection(new StructuredSelection(rightItem.getData()));
 			updateCenter();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setInput(Object input) {
 		if (configuration.getProperty(EMFCompareConstants.PROPERTY_INPUT_CHANGED) != null) {
 			final ModelInputSnapshot snapshot = (ModelInputSnapshot)configuration
 					.getProperty(EMFCompareConstants.PROPERTY_INPUT_CHANGED);
 			super.setInput(new ModelCompareInput(snapshot.getMatch(), snapshot.getDiff()));
 		} else if (input instanceof ModelInputSnapshot) {
 			final ModelInputSnapshot snapshot = (ModelInputSnapshot)input;
 			super.setInput(new ModelCompareInput(snapshot.getMatch(), snapshot.getDiff()));
 		} else {
 			Object typedInput = input;
 
 			if (typedInput instanceof ICompareInput) {
 				final ITypedElement left = ((ICompareInput)typedInput).getLeft();
 				final ITypedElement right = ((ICompareInput)typedInput).getRight();
 
 				if (left instanceof IStreamContentAccessor && right instanceof IStreamContentAccessor) {
 					try {
 						/*
 						 * Models order is arbitrary, but we will invert the right and the left for CVS/SVN
 						 * comparison.
 						 */
 						final EObject rightModel = ModelUtils.load(((IStreamContentAccessor)left)
 								.getContents(), left.getName());
 						final EObject leftModel = ModelUtils.load(((IStreamContentAccessor)right)
 								.getContents(), right.getName());
 
 						final MatchModel match = new MatchService().doMatch(leftModel, rightModel,
 								new NullProgressMonitor());
 						final DiffModel diff = new DiffMaker().doDiff(match);
 
 						typedInput = new ModelCompareInput(match, diff);
 					} catch (InterruptedException e) {
 						EMFComparePlugin.getDefault().log(e.getMessage(), true);
 					} catch (IOException e) {
 						EMFComparePlugin.getDefault().log(e.getMessage(), true);
 					} catch (CoreException e) {
 						EMFComparePlugin.getDefault().log(e.getMessage(), true);
 					}
 				}
 			}
 			super.setInput(typedInput);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#copy(boolean)
 	 */
 	@Override
 	protected void copy(boolean leftToRight) {
 		((ModelCompareInput)getInput()).copy(leftToRight);
 		setRightDirty(leftToRight);
 		setLeftDirty(!leftToRight);
 	}
 
 	protected void copyDiffLeftToRight() {
		DiffElement diff = null;
 		if (leftPart.getSelectedElements() != null && leftPart.getSelectedElements().size() > 0) {
 			for (final DiffElement aDiff : ((ModelCompareInput)getInput()).getDiffAsList()) {
 				if (leftPart.getSelectedElements().get(0).getData().equals(
 						EMFCompareEObjectUtils.getLeftElement(aDiff))) {
 					diff = aDiff;
 					break;
 				}
 			}
 			if (diff != null)
 				copy(diff, true);
 		}
 	}
 
 	protected void copyDiffRightToLeft() {
 		DiffElement diff = null;
 		for (final DiffElement aDiff : ((ModelCompareInput)getInput()).getDiffAsList()) {
 			if (rightPart.getSelectedElements().get(0).getData().equals(
 					EMFCompareEObjectUtils.getRightElement(aDiff))) {
 				diff = aDiff;
 				break;
 			}
 		}
 		if (diff != null)
 			copy(diff, false);
 	}
 
 	protected void copy(DiffElement diff, boolean leftToRight) {
 		final MergeFactory mergeFactory = new MergeService().getBestFactory();
 		if (diff != null) {
 			final AbstractMerger merger = mergeFactory.createMerger(diff);
 			if (leftToRight && merger.canUndoInTarget()) {
 				merger.undoInTarget();
 				setRightDirty(true);
 			} else if (!leftToRight && merger.canApplyInOrigin()) {
 				merger.applyInOrigin();
 				setLeftDirty(true);
 			}
 			update();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#createControls(Composite)
 	 */
 	@Override
 	protected void createControls(Composite composite) {
 		leftPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.LEFT);
 		rightPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.RIGHT);
 		ancestorPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.ANCESTOR);
 
 		final EditorPartListener partListener = new EditorPartListener(leftPart, rightPart, ancestorPart);
 		leftPart.addCompareEditorPartListener(partListener);
 		rightPart.addCompareEditorPartListener(partListener);
 		ancestorPart.addCompareEditorPartListener(partListener);
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
 			final Action copyDiffLeftToRight = new AbstractCompareAction(ResourceBundle
 					.getBundle(BUNDLE_NAME), "action.CopyDiffLeftToRight.") { //$NON-NLS-1$
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
 			final Action copyDiffRightToLeft = new AbstractCompareAction(ResourceBundle
 					.getBundle(BUNDLE_NAME), "action.CopyDiffRightToLeft.") { //$NON-NLS-1$
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
 			public void run() {
 				navigate(false);
 			}
 		};
 		final ActionContributionItem previousDiffContribution = new ActionContributionItem(previousDiff);
 		previousDiffContribution.setVisible(true);
 		tbm.appendToGroup("navigation", previousDiffContribution); //$NON-NLS-1$
 	}
 
 	protected void navigate(boolean down) {
 		final List<DiffElement> diffs = ((ModelCompareInput)getInput()).getDiffAsList();
 		if (diffs.size() != 0) {
 			DiffElement theDiff = diffs.get(0);
 			if (currentDiff != null) {
 				theDiff = currentDiff;
 			}
 			for (int i = 0; i < diffs.size(); i++) {
 				if (diffs.get(i).equals(theDiff) && down) {
 					DiffElement next = diffs.get(0);
 					if (diffs.size() > i + 1) {
 						next = diffs.get(i + 1);
 					}
 					currentDiff = next;
 					leftPart.navigateToDiff(currentDiff);
 					rightPart.navigateToDiff(currentDiff);
 					break;
 				} else if (diffs.get(i).equals(theDiff) && !down) {
 					DiffElement previous = diffs.get(diffs.size() - 1);
 					if (i > 0) {
 						previous = diffs.get(i - 1);
 					}
 					currentDiff = previous;
 					leftPart.navigateToDiff(currentDiff);
 					rightPart.navigateToDiff(currentDiff);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Updates the value of the colors as they are changed on the preference page.
 	 */
 	protected void updateColors() {
 		final IPreferenceStore comparePreferences = EMFCompareUIPlugin.getDefault().getPreferenceStore();
 		highlightColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_HIGHLIGHT_COLOR);
 		changedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_CHANGED_COLOR);
 		addedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_ADDED_COLOR);
 		removedColor = PreferenceConverter.getColor(comparePreferences,
 				EMFCompareConstants.PREFERENCES_KEY_REMOVED_COLOR);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#updateToolItems()
 	 */
 	@Override
 	protected void updateToolItems() {
 		/*
 		 * DIRTY We can override "updateToolItems", yet we have no access to the superclass's
 		 * "fCopyLeftToRightAction" and "fCopyRightToLeftAction" nor do we have accessors for it. We could
 		 * alter MergeViewerContentProvider "isRightEditable" to enable these actions yet it is an internal
 		 * class and reimplementing an IMergeViewerContentProvider seems useless for such a trivial task. We
 		 * will then iterate through the ToolBarManager's contributor and enable the two which labels are
 		 * defined by the keys "action.CopyLeftToRight.label" and "action.CopyRightToLeft.label".
 		 */
 		final ToolBarManager tbm = CompareViewerPane.getToolBarManager(getControl().getParent());
 		for (IContributionItem toolBarItem : tbm.getItems()) {
 			if (toolBarItem instanceof ActionContributionItem) {
 				final IAction action = ((ActionContributionItem)toolBarItem).getAction();
 				if (action.getText().equals(
 						ResourceBundle.getBundle(BUNDLE_NAME).getString("action.CopyLeftToRight.label"))) { //$NON-NLS-1$
 					action.setEnabled(getCompareConfiguration().isRightEditable());
 				} else if (action.getText().equals(
 						ResourceBundle.getBundle(BUNDLE_NAME).getString("action.CopyRightToLeft.label"))) { //$NON-NLS-1$
 					action.setEnabled(getCompareConfiguration().isLeftEditable());
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#getContents(boolean)
 	 */
 	@Override
 	protected byte[] getContents(boolean left) {
 		byte[] contents = null;
 
		EObject root = (EObject)((IMergeViewerContentProvider)getContentProvider())
				.getLeftContent(getInput());
 		if (!left)
			root = (EObject)((IMergeViewerContentProvider)getContentProvider()).getRightContent(getInput());
 
 		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			root.eResource().save(stream, null);
 			contents = stream.toByteArray();
 		} catch (final IOException e) {
 			EMFComparePlugin.getDefault().log(e, false);
 		}
 		return contents;
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
 		getCenterPart().setBounds(leftWidth - (CENTER_WIDTH / 2), y, CENTER_WIDTH, height);
 		leftPart.setBounds(x, y, leftWidth - (CENTER_WIDTH / 2), height);
 		rightPart.setBounds(x + leftWidth + (CENTER_WIDTH / 2), y, rightWidth - (CENTER_WIDTH / 2), height);
 		update();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see ContentMergeViewer#updateContent(Object, Object, Object)
 	 */
 	@Override
 	protected void updateContent(Object ancestor, Object left, Object right) {
 		if (getInput() != null) {
 			Object leftObject = left;
 			Object rightObject = right;
 			if (leftObject instanceof TypedElementWrapper)
 				leftObject = ((TypedElementWrapper)leftObject).getObject();
 			if (rightObject instanceof TypedElementWrapper)
 				rightObject = ((TypedElementWrapper)rightObject).getObject();
 
 			if (left != null) {
 				leftPart.setInput(left);
 			}
 			if (right != null) {
 				rightPart.setInput(right);
 			}
 		}
 		update();
 	}
 
 	/**
 	 * Basic implementation of an {@link ICompareEditorPartListener}.
 	 */
 	private class EditorPartListener implements ICompareEditorPartListener {
 		private ModelContentMergeViewerPart[] viewerParts;
 
 		/**
 		 * Instantiate this {@link EditorPartListener} given the left, right and ancestor viewer parts.
 		 * 
 		 * @param parts
 		 *            The viewer parts.
 		 */
 		public EditorPartListener(ModelContentMergeViewerPart... parts) {
 			viewerParts = parts;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see ICompareEditorPartListener#selectedTabChanged()
 		 */
 		public void selectedTabChanged(int newIndex) {
 			selectedTab = newIndex;
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
 	 * We want to avoid flickering as much as possible for our draw operations on the center part, yet we
 	 * can't use double buffering to draw on it. We will then draw on a {@link Canvas} moved above that center
 	 * part.
 	 */
 	private abstract class AbstractBufferedCanvas extends Canvas {
 		private Image buffer;
 
 		private double[] baseCenterCurve;
 
 		/**
 		 * Default constructor, instantiates the canvas given its parent.
 		 * 
 		 * @param parent
 		 *            Parent of the canvas.
 		 */
 		public AbstractBufferedCanvas(Composite parent) {
 			super(parent, SWT.NO_BACKGROUND | SWT.NO_MERGE_PAINTS | SWT.NO_REDRAW_RESIZE);
 
 			addPaintListener(new PaintListener() {
 				public void paintControl(PaintEvent event) {
 					doubleBufferedPaint(event.gc);
 				}
 			});
 
 			addDisposeListener(new DisposeListener() {
 				public void widgetDisposed(DisposeEvent e) {
 					if (buffer != null) {
 						buffer.dispose();
 						buffer = null;
 					}
 				}
 			});
 		}
 
 		/**
 		 * Repaints the canvas.
 		 */
 		public void repaint() {
 			if (!isDisposed()) {
 				final GC gc = new GC(this);
 				doubleBufferedPaint(gc);
 				gc.dispose();
 			}
 		}
 
 		private void doubleBufferedPaint(GC dest) {
 			final Point size = getSize();
 
 			if (size.x <= 0 || size.y <= 0)
 				return;
 
 			if (buffer != null) {
 				final Rectangle bufferBounds = buffer.getBounds();
 				if (bufferBounds.width != size.x || bufferBounds.height != size.y) {
 					buffer.dispose();
 					buffer = null;
 				}
 			}
 
 			if (buffer == null)
 				buffer = new Image(getDisplay(), size.x, size.y);
 
 			final GC gc = new GC(buffer);
 			try {
 				gc.setBackground(getBackground());
 				gc.fillRectangle(0, 0, size.x, size.y);
 				doPaint(gc);
 			} finally {
 				gc.dispose();
 			}
 
 			dest.drawImage(buffer, 0, 0);
 		}
 
 		protected void drawLine(GC gc, Item leftItem, Item rightItem, DiffElement diff) {
 			if (leftItem == null || rightItem == null)
 				return;
 			final Rectangle centerbounds = getCenterPart().getBounds();
 			Rectangle leftBounds = null;
 			Rectangle rightBounds = null;
 			if (selectedTab == TREE_TAB) {
 				leftBounds = ((TreeItem)leftItem).getBounds();
 				rightBounds = ((TreeItem)rightItem).getBounds();
 			} else if (selectedTab == PROPERTIES_TAB) {
 				leftBounds = ((TableItem)leftItem).getBounds();
 				rightBounds = ((TableItem)rightItem).getBounds();
 			} else {
 				throw new IllegalStateException("Invalid value for tab selection"); //$NON-NLS-1$
 			}
 
 			// Defines the circling Color
 			RGB color = changedColor;
 			if (diff instanceof AddModelElement) {
 				color = addedColor;
 			} else if (diff instanceof RemoveModelElement) {
 				color = removedColor;
 			}
 
 			// Defines all variables needed for drawing the line.
 			final int treeTabBorder = 5;
 			final int leftX = 0;
 			final int rightX = centerbounds.width;
 			final int leftRectangleHeight = leftBounds.height - 1;
 			final int rightRectangleHeight = rightBounds.height - 1;
 
 			int leftY = leftBounds.y + leftRectangleHeight / 2 + treeTabBorder;
 			int rightY = rightBounds.y + rightRectangleHeight / 2 + treeTabBorder;
 			if (selectedTab == TREE_TAB
 					&& (!leftItem.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff)) || diff instanceof AddModelElement)) {
 				leftY = leftBounds.y + leftRectangleHeight + treeTabBorder;
 			}
 			if (selectedTab == TREE_TAB
 					&& (!rightItem.getData().equals(EMFCompareEObjectUtils.getRightElement(diff)) || diff instanceof RemoveModelElement)) {
 				rightY = rightBounds.y + rightRectangleHeight + treeTabBorder;
 			}
 
 			int lineWidth = 1;
 			if (selectedTab == PROPERTIES_TAB || leftPart.getSelectedElements().contains(leftItem)
 					|| rightPart.getSelectedElements().contains(rightItem)) {
 				lineWidth = 2;
 			}
 
 			// Performs the actual drawing
 			gc.setForeground(new Color(getCenterPart().getDisplay(), color));
 			gc.setLineWidth(lineWidth);
 			gc.setLineStyle(SWT.LINE_SOLID);
 			final int[] points = getCenterCurvePoints(leftX, leftY, rightX, rightY);
 			for (int i = 1; i < points.length; i++) {
 				gc.drawLine(leftX + i - 1, points[i - 1], leftX + i, points[i]);
 			}
 		}
 
 		private int[] getCenterCurvePoints(int startx, int starty, int endx, int endy) {
 			if (baseCenterCurve == null) {
 				buildBaseCenterCurve(endx - startx);
 			}
 			double height = endy - starty;
 			height = height / 2;
 			final int width = endx - startx;
 			final int[] points = new int[width];
 			for (int i = 0; i < width; i++) {
 				points[i] = (int)(-height * baseCenterCurve[i] + height + starty);
 			}
 			return points;
 		}
 
 		private void buildBaseCenterCurve(final int w) {
 			final double width = w;
 			baseCenterCurve = new double[CENTER_WIDTH];
 			for (int i = 0; i < CENTER_WIDTH; i++) {
 				final double r = i / width;
 				baseCenterCurve[i] = Math.cos(Math.PI * r);
 			}
 		}
 
 		/**
 		 * Reimplement this method for the actual drawing.
 		 * 
 		 * @param gc
 		 *            {@link GC} used for the painting.
 		 */
 		public abstract void doPaint(GC gc);
 	}
 }
