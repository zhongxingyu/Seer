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
 package org.eclipse.emf.compare.provider.spec;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import com.google.common.collect.ImmutableCollection;
 import com.google.common.collect.ImmutableList;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.provider.AdapterFactoryUtil;
 import org.eclipse.emf.compare.provider.IItemDescriptionProvider;
 import org.eclipse.emf.compare.provider.IItemStyledLabelProvider;
 import org.eclipse.emf.compare.provider.ResourceAttachmentChangeItemProvider;
 import org.eclipse.emf.compare.provider.utils.ComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.IComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.Style;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * Specialized {@link ResourceAttachmentChangeItemProvider} returning nice output for {@link #getText(Object)}
  * and {@link #getImage(Object)}.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class ResourceAttachmentChangeItemProviderSpec extends ResourceAttachmentChangeItemProvider implements IItemStyledLabelProvider, IItemDescriptionProvider {
 
 	/** The image provider used with this item provider. */
 	private final OverlayImageProvider overlayProvider;
 
 	/**
 	 * Constructor calling super {@link #ResourceAttachmentChangeItemProvider(AdapterFactory)}.
 	 * 
 	 * @param adapterFactory
 	 *            The adapter factory.
 	 */
 	public ResourceAttachmentChangeItemProviderSpec(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 		overlayProvider = new OverlayImageProvider(getResourceLocator());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ResourceAttachmentChangeItemProvider#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Collection<?> getChildren(Object object) {
 		Collection<?> superChildren = super.getChildren(object);
 		List<? super Object> ret = newArrayList(superChildren);
 
 		ResourceAttachmentChange resourceAttachmentChange = (ResourceAttachmentChange)object;
 		final EObject value;
		switch (resourceAttachmentChange.getSource()) {
 			case LEFT:
				value = resourceAttachmentChange.getMatch().getLeft();
 				break;
 			case RIGHT:
				value = resourceAttachmentChange.getMatch().getRight();
 				break;
 			default:
 				value = null;
 				throw new IllegalStateException();
 		}
 
 		Match matchOfValue = resourceAttachmentChange.getMatch().getComparison().getMatch(value);
 		if (matchOfValue != null) {
 			Collection<?> children = getChildren(matchOfValue);
 			children.remove(resourceAttachmentChange);
 			ret.addAll(children);
 		}
 
 		return ImmutableList.copyOf(ret);
 	}
 
 	/**
 	 * Returns the children of the given {@link Match}.
 	 * 
 	 * @param matchOfValue
 	 *            the given {@link Match}.
 	 * @return the children of the given {@link Match}.
 	 */
 	private Collection<?> getChildren(Match matchOfValue) {
 		final Collection<?> children;
 		ITreeItemContentProvider matchItemContentProvider = (ITreeItemContentProvider)adapterFactory.adapt(
 				matchOfValue, ITreeItemContentProvider.class);
 		if (matchItemContentProvider != null) {
 			Collection<?> itemProviderChildren = matchItemContentProvider.getChildren(matchOfValue);
 			if (itemProviderChildren instanceof ImmutableCollection<?>) {
 				children = newArrayList(itemProviderChildren);
 			} else {
 				children = itemProviderChildren;
 			}
 
 			Iterator<?> childrenIterator = children.iterator();
 			while (childrenIterator.hasNext()) {
 				Object child = childrenIterator.next();
 				if (child instanceof Match) {
 					if (!matchItemContentProvider.hasChildren(child)) {
 						childrenIterator.remove();
 					}
 				}
 
 			}
 		} else {
 			children = ImmutableList.of();
 		}
 		return children;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.MatchItemProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Object getImage(Object object) {
 		final Match match = ((ResourceAttachmentChange)object).getMatch();
 		Object ret = AdapterFactoryUtil.getImage(getRootAdapterFactory(), match.getLeft());
 		if (ret == null) {
 			ret = AdapterFactoryUtil.getImage(getRootAdapterFactory(), match.getRight());
 		}
 		if (ret == null) {
 			ret = AdapterFactoryUtil.getImage(getRootAdapterFactory(), match.getOrigin());
 		}
 		if (ret == null) {
 			ret = super.getImage(object);
 		}
 
 		Object matchImage = overlayProvider.getComposedImage((ResourceAttachmentChange)object, ret);
 		ret = overlayImage(object, matchImage);
 
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ResourceAttachmentChangeItemProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object object) {
 		return getStyledText(object).getString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemStyledLabelProvider#getStyledText(java.lang.Object)
 	 */
 	public IComposedStyledString getStyledText(Object object) {
 		final Match match = ((ResourceAttachmentChange)object).getMatch();
 		String value = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getLeft());
 		if (value == null) {
 			value = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getRight());
 		}
 		if (value == null) {
 			value = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getOrigin());
 		}
 		if (value == null) {
 			value = super.getText(object);
 		}
 
 		ComposedStyledString ret = new ComposedStyledString(value);
 
 		DifferenceKind labelValue = ((ResourceAttachmentChange)object).getKind();
 		String label = labelValue == null ? "" : labelValue.toString().toLowerCase(); //$NON-NLS-1$
 
 		return ret.append(" [resource contents " + label + "]", Style.DECORATIONS_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemDescriptionProvider#getDescription(java.lang.Object)
 	 */
 	public String getDescription(Object object) {
 		final ResourceAttachmentChange rac = (ResourceAttachmentChange)object;
 		final Match match = rac.getMatch();
 		String ret = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getLeft());
 		if (ret == null) {
 			ret = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getRight());
 		}
 		if (ret == null) {
 			ret = AdapterFactoryUtil.getText(getRootAdapterFactory(), match.getOrigin());
 		}
 		if (ret == null) {
 			ret = super.getText(object);
 		}
 
 		String remotely = "";
 		if (rac.getSource() == DifferenceSource.RIGHT) {
 			remotely = "remotely ";
 		}
 
 		DifferenceKind labelValue = rac.getKind();
 		final String hasBeen = " has been ";
 
 		switch (labelValue) {
 			case ADD:
 				ret += hasBeen + remotely + "added to resource contents";
 				break;
 			case DELETE:
 				ret += hasBeen + remotely + "deleted from resource contents";
 				break;
 			case MOVE:
 				ret += hasBeen + remotely + "moved in resource contents";
 				break;
 			default:
 				throw new IllegalStateException("Unsupported " + DifferenceKind.class.getSimpleName()
 						+ " value: " + rac.getKind());
 		}
 		return ret;
 	}
 }
