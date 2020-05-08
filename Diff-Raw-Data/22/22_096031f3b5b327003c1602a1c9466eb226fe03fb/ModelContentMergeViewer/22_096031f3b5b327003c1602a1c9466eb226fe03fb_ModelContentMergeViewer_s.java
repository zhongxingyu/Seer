 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
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
 import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
 import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.diff.metamodel.AddModelElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.ModelInputSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.RemoveModelElement;
 import org.eclipse.emf.compare.merge.api.AbstractMerger;
 import org.eclipse.emf.compare.merge.api.MergeFactory;
import org.eclipse.emf.compare.merge.service.MergeService;
 import org.eclipse.emf.compare.ui.AbstractCompareAction;
 import org.eclipse.emf.compare.ui.EMFCompareUIPlugin;
 import org.eclipse.emf.compare.ui.ICompareEditorPartListener;
 import org.eclipse.emf.compare.ui.ModelCompareInput;
 import org.eclipse.emf.compare.ui.TypedElementWrapper;
 import org.eclipse.emf.compare.ui.contentprovider.ModelContentMergeContentProvider;
 import org.eclipse.emf.compare.ui.util.EMFCompareConstants;
 import org.eclipse.emf.compare.ui.util.EMFCompareEObjectUtils;
 import org.eclipse.emf.compare.ui.viewerpart.ModelContentMergeViewerPart;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
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
 
 	private IPropertyChangeListener structureSelectionListener;
 
 	private IPropertyChangeListener preferenceListener;
 
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
 		setContentProvider(new ModelContentMergeContentProvider(config));
 
 		structureSelectionListener = new IPropertyChangeListener() {
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
 		};
 		configuration.addPropertyChangeListener(structureSelectionListener);
 
 		preferenceListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().endsWith("color")) { //$NON-NLS-1$
 					updateColors();
 				}
 			}
 		};
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
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
 	 * Returns the compare configuration of this viewer.
 	 * 
 	 * @return The compare configuration of this viewer.
 	 */
 	public CompareConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	/**
 	 * Returns the center {@link Canvas} appearing between the vewer parts.
 	 * 
 	 * @return The center {@link Canvas}.
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
 						drawLine(gc, getLeftItem(diff), getRightItem(diff), diff);
 					}
 				}
 			};
 		canvas.moveAbove(null);
 		return canvas;
 	}
 
 	/**
 	 * Returns the item (either {@link TableItem} or {@link TreeItem}) representing the left element of the
 	 * given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the left item for.
 	 * @return The item representing the left element of the given {@link DiffElement}.
 	 */
 	public Item getLeftItem(DiffElement diff) {
 		EObject leftElement = EMFCompareEObjectUtils.getLeftElement(diff);
 		EObject rightElement = EMFCompareEObjectUtils.getRightElement(diff);
 		if (selectedTab == PROPERTIES_TAB) {
 			leftElement = diff;
 			rightElement = diff;
 		}
 		Item leftItem = (Item)leftPart.find(leftElement);
 		final Item rightItem = (Item)rightPart.find(rightElement);
 
 		if (selectedTab == TREE_TAB && leftItem != null
 				&& (!leftItem.getData().equals(leftElement) || diff instanceof AddModelElement)) {
 			if (rightItem != null && rightItem.getData().equals(rightElement) 
 					&& rightItem.getData() instanceof EObject && ((EObject)rightItem.getData()).eContainer() != null) {
 				final int rightIndex = ((EObject)rightItem.getData()).eContainer().eContents().indexOf(
 						rightItem.getData());
 				final EList leftList = ((EObject)leftItem.getData()).eContents();
 				final int leftIndex = Math.min(rightIndex - 1, leftList.size() - 1);
 				if (leftIndex > 0)
 					leftItem = (TreeItem)leftPart.find((EObject)leftList.get(leftIndex));
 			}
 		} else if (selectedTab == TREE_TAB && leftItem == null) {
 			leftItem = leftPart.getTreeRoot();
 		}
 
 		return leftItem;
 	}
 
 	/**
 	 * Returns the item (either {@link TableItem} or {@link TreeItem}) representing the right element of the
 	 * given {@link DiffElement}.
 	 * 
 	 * @param diff
 	 *            Diff we need to find the right item for.
 	 * @return The item representing the right element of the given {@link DiffElement}.
 	 */
 	public Item getRightItem(DiffElement diff) {
 		EObject leftElement = EMFCompareEObjectUtils.getLeftElement(diff);
 		EObject rightElement = EMFCompareEObjectUtils.getRightElement(diff);
 		if (selectedTab == PROPERTIES_TAB) {
 			leftElement = diff;
 			rightElement = diff;
 		}
 		final Item leftItem = (Item)leftPart.find(leftElement);
 		Item rightItem = (Item)rightPart.find(rightElement);
 		
 		if (rightItem == null) {
 			rightItem = (Item)rightPart.getTreeRoot();
 			rightElement = (EObject)rightItem.getData();
 		}
 
 		if (selectedTab == TREE_TAB && rightItem != null
 				&& (!rightItem.getData().equals(rightElement) || diff instanceof RemoveModelElement)) {
 			if (leftItem != null && leftItem.getData().equals(leftElement) 
 					&& leftItem.getData() instanceof EObject && ((EObject)leftItem.getData()).eContainer() != null) {
 				final int leftIndex = ((EObject)leftItem.getData()).eContainer().eContents().indexOf(
 						leftItem.getData());
 				// Ensures we cannot trigger ArrayOutOfBounds exeptions
 				final EList rightList = ((EObject)rightItem.getData()).eContents();
 				final int rightIndex = Math.min(leftIndex - 1, rightList.size() - 1);
 				if (rightIndex > 0)
 					rightItem = (TreeItem)rightPart.find((EObject)rightList.get(rightIndex));
 			}
 		} else if (selectedTab == TREE_TAB && rightItem == null) {
 			rightItem = rightPart.getTreeRoot();
 		}
 
 		return rightItem;
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
 	 * Sets the parts' tree selection given the {@link DiffElement} to select and the identifier of the side
 	 * which triggered the selection change.
 	 * 
 	 * @param diff
 	 *            {@link DiffElement} backing the current selection.
 	 */
 	public void setSelection(DiffElement diff) {
 		currentDiff = diff;
 		leftPart.navigateToDiff(currentDiff);
 		rightPart.navigateToDiff(currentDiff);
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
 		if (currentDiff != null)
 			copy(currentDiff, true);
 		currentDiff = null;
 	}
 
 	protected void copyDiffRightToLeft() {
 		if (currentDiff != null)
 			copy(currentDiff, false);
 		currentDiff = null;
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
 		leftPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.RIGHT);
 		rightPart = new ModelContentMergeViewerPart(this, composite, EMFCompareConstants.LEFT);
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
 	 * @see ContentMergeViewer#getContents(boolean)
 	 */
 	@Override
 	protected byte[] getContents(boolean left) {
 		byte[] contents = null;
 
 		EObject root = ((TypedElementWrapper)((IMergeViewerContentProvider)getContentProvider())
 				.getLeftContent(getInput())).getObject();
 		if (!left)
 			root = ((TypedElementWrapper)((IMergeViewerContentProvider)getContentProvider())
 					.getRightContent(getInput())).getObject();
 
 		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			root.eResource().save(stream, null);
 			contents = stream.toByteArray();
 		} catch (IOException e) {
 			EMFComparePlugin.getDefault().log(e, false);
 		}
 
 		return contents;
 	}
 
 	/**
 	 * Performs some additional cleanup at the viewer's disposal.
 	 * 
 	 * @see ContentMergeViewer#handleDispose(DisposeEvent)
 	 */
 	protected void handleDispose(DisposeEvent event) {
 		super.handleDispose(event);
 		configuration.removePropertyChangeListener(structureSelectionListener);
 		structureSelectionListener = null;
 		EMFCompareUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
 		preferenceListener = null;
 		leftPart = null;
 		rightPart = null;
 		ancestorPart = null;
 		canvas = null;
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
 
 			final PaintListener paintListener = new PaintListener() {
 				public void paintControl(PaintEvent event) {
 					doubleBufferedPaint(event.gc);
 				}
 			};
 			addPaintListener(paintListener);
 
 			addDisposeListener(new DisposeListener() {
 				public void widgetDisposed(DisposeEvent e) {
 					if (buffer != null) {
 						buffer.dispose();
 						buffer = null;
 					}
 					removePaintListener(paintListener);
 				}
 			});
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
 
 			int leftY = leftBounds.y + leftRectangleHeight / 2 + treeTabBorder + leftPart.getHeaderHeight();
 			int rightY = rightBounds.y + rightRectangleHeight / 2 + treeTabBorder
 					+ rightPart.getHeaderHeight();
 			if (selectedTab == TREE_TAB
 					&& (!leftItem.getData().equals(EMFCompareEObjectUtils.getLeftElement(diff)) || diff instanceof AddModelElement)) {
 				leftY += leftRectangleHeight / 2;
 			}
 			if (selectedTab == TREE_TAB
 					&& (!rightItem.getData().equals(EMFCompareEObjectUtils.getRightElement(diff)) || diff instanceof RemoveModelElement)) {
 				rightY += rightRectangleHeight / 2;
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
 
 		private void buildBaseCenterCurve(int w) {
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
