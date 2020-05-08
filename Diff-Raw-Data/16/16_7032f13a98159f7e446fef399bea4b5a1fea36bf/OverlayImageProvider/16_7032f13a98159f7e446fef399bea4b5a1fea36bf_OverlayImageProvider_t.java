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
 package org.eclipse.emf.compare.provider.spec;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Conflict;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.internal.merge.IDiffMergeData;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.provider.ComposedImage;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class OverlayImageProvider {
 
 	private final ResourceLocator fResourceLocator;
 
 	/**
 	 * 
 	 */
 	public OverlayImageProvider(ResourceLocator resourceLocator) {
 		this.fResourceLocator = resourceLocator;
 	}
 
 	public Object getComposedImage(Diff diff, Object imageToCompose) {
 		String overlay = getImageOverlay(diff);
 		return getComposedImage(imageToCompose, overlay);
 	}
 
 	public Object getComposedImage(Match match, Object imageToCompose) {
 		String overlay = getImageOverlay(match);
 		return getComposedImage(imageToCompose, overlay);
 	}
 
 	private Object getComposedImage(Object imageToCompose, String overlay) {
 		Collection<Object> images = newArrayList();
 		images.add(imageToCompose);
 		if (overlay != null) {
 			Object image = fResourceLocator.getImage(overlay);
 			images.add(image);
 		}
 		return new ComposedImageExtension(images);
 	}
 
 	// Nothing here has to be externalized
 	@SuppressWarnings("nls")
 	private String getImageOverlay(Diff diff) {
 		final DifferenceSource source = diff.getSource();
 		final Match match = diff.getMatch();
 		final Conflict conflict = diff.getConflict();
 		final DifferenceKind diffKind = diff.getKind();
 		final Comparison comparison = match.getComparison();
 		String path = "full/ovr16/";
 
 		if (diff.getState() == DifferenceState.MERGED) {
 			Adapter adapter = EcoreUtil.getExistingAdapter(diff, IDiffMergeData.class);
 			if (adapter != null) {
 				IDiffMergeData iMergeData = (IDiffMergeData)adapter;
				if (!iMergeData.isLeftEditable() || !iMergeData.isRightEditable()) {
 					if (iMergeData.hasBeenMergedToLeft()) {
						if (diff.getSource() == DifferenceSource.LEFT) {
							path += "removed_ov";
						} else {
							path += "merged_ov";
						}
 					} else {
						if (diff.getSource() == DifferenceSource.LEFT) {
							path += "merged_ov";
						} else {
							path += "removed_ov";
						}
 					}
 				} else if (iMergeData.isLeftEditable() && iMergeData.isRightEditable()) {
 					if (iMergeData.hasBeenMergedToLeft()) {
 						path += "merged_left_ov";
 					} else {
 						path += "merged_right_ov";
 					}
 				}
 			} else {
 				path += "merged_ov";
 			}
 		} else if (diff.getState() == DifferenceState.DISCARDED) {
 			path += "removed_ov";
 		} else if (comparison.isThreeWay()) {
 			// "png" needs explicit declaration, "gif" does not
 			String extension = "";
 			if (conflict != null) {
 				extension = ".png";
 				if (conflict.getKind() == ConflictKind.PSEUDO) {
 					path += "p";
 				}
 				path += "conf";
 				if (source == DifferenceSource.RIGHT) {
 					path += "r_";
 				}
 			} else {
 				switch (source) {
 					case LEFT:
 						path += "r_out";
 						break;
 					case RIGHT:
 						path += "r_in";
 						break;
 					default:
 						// Cannot happen ... for now
 						break;
 				}
 			}
 
 			switch (diffKind) {
 				case ADD:
 					path += "add_ov";
 					break;
 				case DELETE:
 					path += "del_ov";
 					break;
 				case CHANGE:
 					// fallthrough
 				case MOVE:
 					path += "chg_ov";
 					break;
 				default:
 					// Cannot happen ... for now
 					break;
 			}
 			path += extension;
 		} else {
 			switch (diffKind) {
 				case ADD:
 					path += "add_ov";
 					break;
 				case DELETE:
 					path += "del_ov";
 					break;
 				case CHANGE:
 					// fallthrough
 				case MOVE:
 					path += "chg_ov";
 					break;
 				default:
 					break;
 			}
 		}
 		return path;
 	}
 
 	// Nothing here has to be externalized
 	@SuppressWarnings("nls")
 	private String getImageOverlay(Match match) {
 		return "full/ovr16/match_ov.png";
 	}
 
 	private final class ComposedImageExtension extends ComposedImage {
 
 		/**
 		 * 
 		 */
 		private static final int X_OFFSET = 10;
 
 		/**
 		 * @param images
 		 */
 		ComposedImageExtension(Collection<?> images) {
 			super(images);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.edit.provider.ComposedImage#getDrawPoints(org.eclipse.emf.edit.provider.ComposedImage.Size)
 		 */
 		@Override
 		public List<Point> getDrawPoints(Size size) {
 			List<ComposedImage.Point> result = super.getDrawPoints(size);
 			if (result.size() > 1) {
 				result.get(1).x = X_OFFSET;
 				result.get(1).y = 2;
 			}
 			return result;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.edit.provider.ComposedImage#getSize(java.util.Collection)
 		 */
 		@Override
 		public Size getSize(Collection<? extends Size> sizes) {
 			this.imageSizes = newArrayList(sizes);
 			List<Point> drawPoints = getDrawPoints(null);
 
 			Size result = new Size();
 			for (int i = 0; i < sizes.size(); i++) {
 				Size size = this.imageSizes.get(i);
 				Point point = drawPoints.get(i);
 
 				result.width = Math.max(result.width, size.width + Math.abs(point.x));
 				result.height = Math.max(result.height, size.height + Math.abs(point.y));
 			}
 			return result;
 		}
 	}
 }
