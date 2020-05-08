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
 package org.eclipse.emf.compare.diagram.internal.extensions.provider.spec;
 
 import com.google.common.base.Preconditions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.diagram.internal.extensions.DiagramDiff;
 import org.eclipse.emf.compare.diagram.internal.extensions.provider.DiagramDiffItemProvider;
 import org.eclipse.emf.compare.provider.AdapterFactoryUtil;
 import org.eclipse.emf.compare.provider.ForwardingItemProvider;
 import org.eclipse.emf.compare.provider.IItemDescriptionProvider;
 import org.eclipse.emf.compare.provider.IItemStyledLabelProvider;
 import org.eclipse.emf.compare.provider.spec.OverlayImageProvider;
 import org.eclipse.emf.compare.provider.utils.ComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.IComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.Style;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.gmf.runtime.notation.View;
 
 /**
  * Unique item provider for every diagram differences.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  */
 public class ForwardingDiagramDiffItemProvider extends ForwardingItemProvider implements IItemStyledLabelProvider, IItemDescriptionProvider {
 
 	/** Used to describe the change. */
 	protected static final String HAS_BEEN = " has been "; //$NON-NLS-1$
 
 	/** Used to describe an illegal state exception. */
 	protected static final String UNSUPPORTED = "Unsupported "; //$NON-NLS-1$
 
 	/** Used to describe an illegal state exception. */
 	protected static final String VALUE = " value: "; //$NON-NLS-1$
 
 	/** The elide length. */
 	private static final int ELIDE_LENGTH = 50;
 
 	/** The image provider used with this item provider. */
 	private OverlayImageProvider overlayProvider;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param delegate
 	 *            Default item provider adapter.
 	 */
 	public ForwardingDiagramDiffItemProvider(ItemProviderAdapter delegate) {
 		super(delegate);
 		if (delegate instanceof DiagramDiffItemProvider) {
 			overlayProvider = new OverlayImageProvider(((DiagramDiffItemProvider)delegate())
 					.getResourceLocator());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ForwardingItemProvider#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Collection<?> getChildren(Object object) {
 		final Collection<Object> ret = new ArrayList<Object>();
 		Match match = null;
 		if (isCandidateToAddChildren(object)) {
 			DiagramDiff diagramDiff = (DiagramDiff)object;
 			EObject view = diagramDiff.getView();
 			Comparison comparison = diagramDiff.getMatch().getComparison();
 			match = comparison.getMatch(view);
			ret.addAll(match.getSubmatches());
			ITreeItemContentProvider contentProvider = (ITreeItemContentProvider)getRootAdapterFactory()
					.adapt(match, ITreeItemContentProvider.class);
			if (contentProvider != null) {
				ret.addAll(contentProvider.getChildren(match));
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * It checks that the given object can hold children.
 	 * 
 	 * @param object
 	 *            The object.
 	 * @return True if it can hold children.
 	 */
 	protected boolean isCandidateToAddChildren(Object object) {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ForwardingItemProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
 	public boolean hasChildren(Object object) {
 		return !getChildren(object).isEmpty();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ForwardingItemProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object object) {
 		return getStyledText(object).getString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ForwardingItemProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Object getImage(Object object) {
 		final DiagramDiff diagramDiff = (DiagramDiff)object;
 		Object image = null;
 		if (diagramDiff.getView() instanceof View) {
 			image = getImage(getRootAdapterFactory(), ((View)diagramDiff.getView()).getElement());
 		} else {
 			image = super.getImage(object);
 		}
 		if (overlayProvider != null && image != null) {
 			Object diffImage = overlayProvider.getComposedImage(diagramDiff, image);
 			return ((DiagramDiffItemProvider)delegate()).getOverlayImage(object, diffImage);
 		} else {
 			return image;
 		}
 	}
 
 	/**
 	 * Returns the image of the given <code>object</code> by adapting it to {@link IItemLabelProvider} and
 	 * asking for its {@link IItemLabelProvider#getImage(Object) text}. Returns null if <code>object</code> is
 	 * null.
 	 * 
 	 * @param adapterFactory
 	 *            the adapter factory to adapt from
 	 * @param object
 	 *            the object from which we want a image
 	 * @return the image, or null if object is null.
 	 * @throws NullPointerException
 	 *             if <code>adapterFactory</code> is null.
 	 */
 	private static Object getImage(AdapterFactory adapterFactory, Object object) {
 		Preconditions.checkNotNull(adapterFactory);
 		if (object != null) {
 			Object adapter = adapterFactory.adapt(object, IItemLabelProvider.class);
 			if (adapter instanceof IItemLabelProvider) {
 				return ((IItemLabelProvider)adapter).getImage(object);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * It builds the label of the item related to the given difference.
 	 * 
 	 * @param diagramDiff
 	 *            the difference.
 	 * @return the label.
 	 */
 	protected String getValueText(final DiagramDiff diagramDiff) {
 		return getValueText(diagramDiff.getView());
 	}
 
 	/**
 	 * It builds the label of the item related to the given object.
 	 * 
 	 * @param view
 	 *            the object.
 	 * @return the label.
 	 */
 	protected String getValueText(EObject view) {
 		EObject reference = null;
 		if (view instanceof View) {
 			reference = ((View)view).getElement();
 		}
 		String value = AdapterFactoryUtil.getText(getRootAdapterFactory(), reference);
 		if (value == null) {
 			value = "<null>"; //$NON-NLS-1$
 		} else {
 			value = org.eclipse.emf.compare.provider.spec.Strings.elide(value, ELIDE_LENGTH, "..."); //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * Returns the name of the main reference or the kind of attribute change linked to the given
 	 * {@link DiagramDiff}.
 	 * 
 	 * @param diagramDiff
 	 *            the given {@link DiagramDiff}.
 	 * @return the name of the main reference or the kind of attribute change linked to the given
 	 *         {@link DiagramDiff}.
 	 */
 	protected String getReferenceText(final DiagramDiff diagramDiff) {
 		return " "; //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getForeground(java.lang.Object)
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		DiagramDiff referenceChange = (DiagramDiff)object;
 		switch (referenceChange.getState()) {
 			case MERGED:
 			case DISCARDED:
 				return URI.createURI("color://rgb/156/156/156"); //$NON-NLS-1$
 			default:
 				return super.getForeground(object);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemDescriptionProvider#getDescription(java.lang.Object)
 	 */
 	public String getDescription(Object object) {
 		final DiagramDiff diagramDiff = (DiagramDiff)object;
 
 		final String valueText = getValueText(diagramDiff);
 		final String referenceText = getReferenceText(diagramDiff);
 
 		String remotely = ""; //$NON-NLS-1$
 		if (diagramDiff.getSource() == DifferenceSource.RIGHT) {
 			remotely = "remotely "; //$NON-NLS-1$
 		}
 
 		String ret = ""; //$NON-NLS-1$
 		final String hasBeen = " has been "; //$NON-NLS-1$
 
 		switch (diagramDiff.getKind()) {
 			case ADD:
 				ret = valueText + hasBeen + remotely + "added to " + referenceText; //$NON-NLS-1$ 
 				break;
 			case DELETE:
 				ret = valueText + hasBeen + remotely + "deleted from " + referenceText; //$NON-NLS-1$ 
 				break;
 			case CHANGE:
 				ret = referenceText + " " + valueText + hasBeen + remotely + "change"; //$NON-NLS-1$ //$NON-NLS-2$ 
 				break;
 			case MOVE:
 				ret = valueText + hasBeen + remotely + "moved in " + referenceText; //$NON-NLS-1$ 
 				break;
 			default:
 				throw new IllegalStateException(UNSUPPORTED + DifferenceKind.class.getSimpleName() + VALUE
 						+ diagramDiff.getKind());
 		}
 
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemStyledLabelProvider#getStyledText(java.lang.Object)
 	 */
 	public IComposedStyledString getStyledText(Object object) {
 		final DiagramDiff diagramDiff = (DiagramDiff)object;
 
 		final String valueText = getValueText(diagramDiff);
 
 		final String referenceText = getReferenceText(diagramDiff);
 
 		ComposedStyledString ret = new ComposedStyledString(valueText);
 		ret.append(" [" + referenceText, Style.DECORATIONS_STYLER); //$NON-NLS-1$
 
 		buildStyledText(diagramDiff.getKind(), ret);
 		ret.append("]", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 
 		return ret;
 	}
 
 	/**
 	 * It builds the given {@link ComposedStyledString} adding the kind of change in description.
 	 * 
 	 * @param kind
 	 *            The kind of change.
 	 * @param ret
 	 *            The {@link ComposedStyledString} to build.
 	 */
 	protected void buildStyledText(final DifferenceKind kind, ComposedStyledString ret) {
 		switch (kind) {
 			case ADD:
 				ret.append(" add", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 				break;
 			case DELETE:
 				ret.append(" delete", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 				break;
 			case CHANGE:
 				ret.append(" change", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 				break;
 			case MOVE:
 				ret.append(" move", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 				break;
 			default:
 				throw new IllegalStateException(UNSUPPORTED + DifferenceKind.class.getSimpleName() + VALUE
 						+ kind);
 		}
 	}
 }
