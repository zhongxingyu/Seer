 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others,
  * Copyright (c) 2008 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    IBM Corporation - initial API and implementation of 2D version
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package org.eclipse.draw3d;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.draw3d.geometry.BoundingBoxImpl;
 import org.eclipse.draw3d.geometry.IBoundingBox;
 import org.eclipse.draw3d.geometry.IVector3f;
 import org.eclipse.draw3d.geometry.Math3D;
 import org.eclipse.draw3d.geometry.Vector3f;
 import org.eclipse.draw3d.geometry.Vector3fImpl;
 import org.eclipse.draw3d.util.CoordinateConverter;
 
 /**
  * 3D version of {@link org.eclipse.draw2d.ChopboxAnchor}.
  * 
  * @author IBM Corporation (original 2D version)
  * @author Jens von Pilgrim
  * @version $Revision$
  * @since 31.10.2007
  * @see org.eclipse.draw2d.ChopboxAnchor
  */
 public class ChopboxAnchor3D extends AbstractConnectionAnchor3D {
 
 	private static final Point TMP_P = new Point();
 
 	private static final Vector3fImpl TMP_V3_1 = new Vector3fImpl();
 
 	/**
 	 * In order to speed up the calculations and reduce memory footprint, all
 	 * anchor instances use this vector for their calculations.
 	 */
 	private static final Vector3fImpl TMP_V3_2 = new Vector3fImpl();
 
 	/**
 	 * Constructs a new ChopboxAnchor.
 	 */
 	protected ChopboxAnchor3D() {
 		// nothing to initialize
 	}
 
 	/**
 	 * Constructs a ChopboxAnchor with the given <i>owner</i> figure.
 	 * 
 	 * @param owner the owner figure
 	 * @since 2.0
 	 */
 	public ChopboxAnchor3D(IFigure owner) {
 		super(owner);
 	}
 
 	/**
 	 * Returns the 3D bounding box of this ChopboxAnchor's owner. Subclasses can
 	 * override this method to adjust the box the anchor can be placed on.
 	 * 
 	 * @return the bounds of this ChopboxAnchor's owner
 	 * @throws IllegalStateException if no 3D bounding box can be determined
 	 */
 	protected IBoundingBox getBounds3D() {
 
 		IFigure owner2D = getOwner();
 		if (owner2D instanceof IFigure3D) {
 			IFigure3D owner3D = (IFigure3D) owner2D;
 			return owner3D.getBounds3D();
 		} else {
 			IFigure3D ancestor3D = Figure3DHelper.getAncestor3D(owner2D);
 			if (ancestor3D == null)
 				throw new IllegalStateException("no 3D ancestor found");
 
 			Rectangle bounds = owner2D.getBounds();
 
 			Point origin = bounds.getBottomLeft();
 			IVector3f location = ancestor3D.getLocation3D(origin);
 
 			Point dimensions = new Point(bounds.width, bounds.height);
 			IVector3f size = ancestor3D.getLocation3D(dimensions);
 
 			return new BoundingBoxImpl(location, size);
 		}
 	}
 
 	/**
 	 * Returns the 2D bounds of this ChopboxAnchor's owner. Subclasses can
 	 * override this method to adjust the box the anchor can be placed on. For
 	 * instance, the owner figure may have a drop shadow that should not be
	 * included in the box.
	 * <p>
	 * Copied from original {@link org.eclipse.draw2d.ChopboxAnchor}.
	 * </p>
 	 * 
 	 * @return the 2D bounds of this ChopboxAnchor's owner
 	 */
 	protected Rectangle getBox() {
 		return getOwner().getBounds();
 	}
 
 	/**
 	 * Gets a Rectangle from {@link #getBox()} and returns the Point where a
 	 * line from the center of the Rectangle to the Point <i>reference</i>
 	 * intersects the Rectangle.
 	 * <p>
 	 * Copied from original
 	 * {@link org.eclipse.draw2d.ChopboxAnchor#getLocation(Point)}.
 	 * </p>
 	 * 
 	 * @param reference The reference point
 	 * @return The anchor location
 	 */
 	@Override
 	public Point getLocation(Point reference) {
 		return doGetLocation(reference, true);
 	}
 
 	/**
 	 * @param reference
 	 * @param absoluteMode used in 2D mode to translate owner to absolute. In 3D
 	 *            mode this is done when converting the location to 3D. (have I
 	 *            heard someone whisper "this is a hack"?)
 	 * @return
 	 */
 	private Point doGetLocation(Point reference, boolean absoluteMode) {
 		Rectangle r = Rectangle.SINGLETON;
 		r.setBounds(getBox());
 		r.translate(-1, -1);
 		r.resize(1, 1);
 
 		if (absoluteMode)
 			getOwner().translateToAbsolute(r);
 		float centerX = r.x + 0.5f * r.width;
 		float centerY = r.y + 0.5f * r.height;
 
 		if (r.isEmpty()
 				|| reference == null
 				|| (reference.x == (int) centerX && reference.y == (int) centerY)) {
 			return new Point((int) centerX, (int) centerY); // This avoids
 			// divide-by-zero
 		}
 
 		float dx = reference.x - centerX;
 		float dy = reference.y - centerY;
 
 		// r.width, r.height, dx, and dy are guaranteed to be non-zero.
 		float scale = 0.5f / Math.max(Math.abs(dx) / r.width, Math.abs(dy)
 				/ r.height);
 
 		dx *= scale;
 		dy *= scale;
 		centerX += dx;
 		centerY += dy;
 
 		return new Point(Math.round(centerX), Math.round(centerY));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.AbstractConnectionAnchor3D#getLocation3D(IVector3f,
 	 *      Vector3f)
 	 */
 	@Override
 	public IVector3f getLocation3D(IVector3f i_reference, Vector3f io_result) {
 
		Vector3f result = io_result;
		if (result == null)
			result = new Vector3fImpl();
 
 		// maybe the owner is a 2D figure!
 		if (getOwner() instanceof IFigure3D) {
 			IFigure3D owner3D = (IFigure3D) getOwner();
 
 			// bounding box in relative coordinates
 			IBoundingBox boundingBox = getBounds3D();
 			Vector3f size = boundingBox.getSize(null);
 			boundingBox.getCenter(TMP_V3_1);
 
 			// bounding box is a dot
 			if (size.lengthSquared() == 0 || i_reference == null) {
				result.set(TMP_V3_1);
				return result;
 			}
 
 			// We can avoid doing intersections by simply scaling the vector
 			// relativeRef - center3D appropriately. The correct scaling factor
 			// depends on which face of the bounding box the vector intersects
 			// with
 			// and can be determined by comparing the sizes of the factors
 			// lambdai =
 			// 2*delta.i / size.i where i in {x, y, z}. The smallest of those
 			// factors is the correct scaling factor. There are no special cases
 			// like intersecting with edges or corners with this method.
 			TMP_V3_2.set(i_reference);
 			owner3D.transformToRelative(TMP_V3_2);
 			Math3D.sub(TMP_V3_2, TMP_V3_1, TMP_V3_2);
 			float lambdaX = Math.abs(size.getX() / (2 * TMP_V3_2.x));
 			float lambdaY = Math.abs(size.getY() / (2 * TMP_V3_2.y));
 			float lambdaZ = Math.abs(size.getZ() / (2 * TMP_V3_2.z));
 
 			float lambda = Math.min(lambdaX, lambdaY);
 			lambda = Math.min(lambda, lambdaZ);
 
 			TMP_V3_2.scale(lambda);
 			Math3D.add(TMP_V3_1, TMP_V3_2, TMP_V3_2);
 
 			// delta is relative to the owner of this anchor, so make it
 			// absolute
 			owner3D.transformToAbsolute(TMP_V3_2);
 
			result.set(TMP_V3_2);
			return result;
 		} else { // 2D owner
 			IFigure3D ancestor3D = Figure3DHelper.getAncestor3D(getOwner());
 			Point result2D = null;
 			if (i_reference == null) {
 				result2D = doGetLocation(null, false);
 			} else {
 				float x = i_reference.getX();
 				float y = i_reference.getY();
 				float z = i_reference.getZ();
 
 				CoordinateConverter.worldToSurface(x, y, z, ancestor3D, TMP_P);
 				result2D = doGetLocation(TMP_P, false);
 			}
 
 			// TODO fix this hack when we get a surfaceLocationMatrix
 			float depth = -ancestor3D.getSize3D().getZ();
 			return CoordinateConverter.surfaceToWorld(result2D.x, result2D.y,
					depth, ancestor3D, result);
 		}
 	}
 
 	/**
 	 * Returns the anchor's reference point. In the case of the ChopboxAnchor,
 	 * this is the center of the anchor's owner. Copied from original
 	 * {@link org.eclipse.draw2d.ChopboxAnchor}.
 	 * 
 	 * @return The reference point
 	 */
 	@Override
 	public Point getReferencePoint() {
 		Point ref = getBox().getCenter();
 		getOwner().translateToAbsolute(ref);
 		return ref;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.ConnectionAnchor3D#getReferencePoint3D(Vector3f)
 	 */
 	public Vector3f getReferencePoint3D(Vector3f io_result) {
 
 		IFigure owner = getOwner();
 		if (owner == null)
 			return null;
 
 		Vector3f result = io_result;
 		if (result == null)
			result = new Vector3fImpl();
 
 		if (owner instanceof IFigure3D) {
 			getBounds3D().getCenter(TMP_V3_2);
 
 			IFigure3D owner3D = (IFigure3D) owner;
 			owner3D.transformToAbsolute(TMP_V3_2);
 
 			result.set(TMP_V3_2);
 			return result;
 		} else {
 			// Point ref = getReferencePoint();
 			// return CoordinateConverter.surfaceToWorld(ref.x, ref.y,
 			// Figure3DHelper.getAncestor3D(owner));
 			return null;
 		}
 	}
 
 }
