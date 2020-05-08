 package org.muis.base.layout;
 
 import org.muis.core.MuisAttribute;
 import org.muis.core.MuisElement;
 import org.muis.core.MuisLayout;
 import org.muis.core.event.MuisEvent;
 import org.muis.core.layout.SimpleSizePolicy;
 import org.muis.core.layout.SizePolicy;
 
 /**
  * A very simple layout that positions and sizes children independently using positional ({@link LayoutConstants#left},
  * {@link LayoutConstants#right}, {@link LayoutConstants#top}, {@link LayoutConstants#bottom}), size ({@link LayoutConstants#width} and
  * {@link LayoutConstants#height}) and minimum size ({@link LayoutConstants#minWidth} and {@link LayoutConstants#minHeight}) attributes or
  * sizers.
  */
 public class SimpleLayout implements MuisLayout
 {
 	private static class RelayoutListener implements org.muis.core.event.MuisEventListener<MuisAttribute<?>>
 	{
 		private final MuisElement theParent;
 
 		RelayoutListener(MuisElement parent)
 		{
 			theParent = parent;
 		}
 
 		@Override
 		public void eventOccurred(MuisEvent<MuisAttribute<?>> event, MuisElement element)
 		{
 			MuisAttribute<?> attr = event.getValue();
 			if(attr == LayoutConstants.left || attr == LayoutConstants.right || attr == LayoutConstants.top
 				|| attr == LayoutConstants.bottom || attr == LayoutConstants.width || attr == LayoutConstants.height
 				|| attr == LayoutConstants.minWidth || attr == LayoutConstants.minHeight)
 				theParent.relayout(false);
 		}
 
 		@Override
 		public boolean isLocal()
 		{
 			return true;
 		}
 	}
 
 	@Override
 	public void initChildren(MuisElement parent, MuisElement [] children)
 	{
 		RelayoutListener listener = new RelayoutListener(parent);
 		for(MuisElement child : children)
 		{
 			child.acceptAttribute(LayoutConstants.left);
 			child.acceptAttribute(LayoutConstants.right);
 			child.acceptAttribute(LayoutConstants.top);
 			child.acceptAttribute(LayoutConstants.bottom);
 			child.acceptAttribute(LayoutConstants.width);
 			child.acceptAttribute(LayoutConstants.height);
 			child.acceptAttribute(LayoutConstants.minWidth);
 			child.acceptAttribute(LayoutConstants.minHeight);
 			child.addListener(MuisElement.ATTRIBUTE_SET, listener);
 		}
 	}
 
 	@Override
 	public SizePolicy getWSizer(MuisElement parent, MuisElement [] children, int parentHeight)
 	{
 		return getSizer(children, LayoutConstants.left, LayoutConstants.right, LayoutConstants.width, LayoutConstants.minWidth,
 			parentHeight, false);
 	}
 
 	@Override
 	public SizePolicy getHSizer(MuisElement parent, MuisElement [] children, int parentWidth)
 	{
 		return getSizer(children, LayoutConstants.top, LayoutConstants.bottom, LayoutConstants.height, LayoutConstants.minHeight,
 			parentWidth, true);
 	}
 
 	/**
 	 * Gets a sizer for a container in one dimension
 	 * 
 	 * @param children The children to lay out
 	 * @param minPosAtt The attribute to control the minimum position of a child (left or top)
 	 * @param maxPosAtt The attribute to control the maximum position of a child (right or bottom)
 	 * @param sizeAtt The attribute to control the size of a child (width or height)
 	 * @param minSizeAtt The attribute to control the minimum size of a child (minWidth or minHeight)
 	 * @param breadth The size of the opposite dimension of the space to lay out the children in
 	 * @param vertical Whether the children are being sized in the vertical dimension or the horizontal
 	 * @return The size policy for the container of the given children in the given dimension
 	 */
 	protected SizePolicy getSizer(MuisElement [] children, MuisAttribute<Length> minPosAtt, MuisAttribute<Length> maxPosAtt,
 		MuisAttribute<Length> sizeAtt, MuisAttribute<Length> minSizeAtt, int breadth, boolean vertical)
 	{
 		SimpleSizePolicy ret = new SimpleSizePolicy();
 		for(MuisElement child : children)
 		{
 			Length minPosL = child.getAttribute(minPosAtt);
 			Length maxPosL = child.getAttribute(maxPosAtt);
 			Length sizeL = child.getAttribute(sizeAtt);
 			Length minSizeL = child.getAttribute(minSizeAtt);
 			if(maxPosL != null && !maxPosL.getUnit().isRelative())
 			{
 				int r = maxPosL.evaluate(0);
 				if(ret.getMin() < r)
 					ret.setMin(r);
 				if(ret.getPreferred() < r)
 					ret.setPreferred(r);
 			}
 			else if(sizeL != null && !sizeL.getUnit().isRelative())
 			{
 				int w = sizeL.evaluate(0);
 				int x;
 				if(minPosL != null && !minPosL.getUnit().isRelative())
 					x = minPosL.evaluate(0);
 				else
 					x = 0;
 
 				if(ret.getMin() < x + w)
 					ret.setMin(x + w);
 				if(ret.getPreferred() < x + w)
 					ret.setPreferred(x + w);
 			}
 			else if(minSizeL != null && !minSizeL.getUnit().isRelative())
 			{
 				SizePolicy childSizer = vertical ? child.getHSizer(breadth) : child.getWSizer(breadth);
 				int minW = minSizeL.evaluate(0);
 				int prefW = childSizer.getPreferred();
 				if(prefW < minW)
 					prefW = minW;
 				int x;
 				if(minPosL != null && !minPosL.getUnit().isRelative())
 					x = minPosL.evaluate(0);
 				else
 					x = 0;
 
 				if(ret.getMin() < x + minW)
 					ret.setMin(x + minW);
 				if(ret.getPreferred() < x + prefW)
 					ret.setPreferred(x + prefW);
 			}
 			else if(minPosL != null && !minPosL.getUnit().isRelative())
 			{
 				SizePolicy childSizer = vertical ? child.getHSizer(breadth) : child.getWSizer(breadth);
 				int x = minPosL.evaluate(0);
 				if(ret.getMin() < x + childSizer.getMin())
 					ret.setMin(x + childSizer.getMin());
 				if(ret.getPreferred() < x + childSizer.getPreferred())
 					ret.setPreferred(x + childSizer.getPreferred());
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public void layout(MuisElement parent, MuisElement [] children)
 	{
 		java.awt.Rectangle bounds = new java.awt.Rectangle();
 		int [] dim = new int[2];
 		for(MuisElement child : children)
 		{
 			Length left = child.getAttribute(LayoutConstants.left);
 			Length right = child.getAttribute(LayoutConstants.right);
 			Length top = child.getAttribute(LayoutConstants.top);
 			Length bottom = child.getAttribute(LayoutConstants.bottom);
 			Length w = child.getAttribute(LayoutConstants.width);
 			Length h = child.getAttribute(LayoutConstants.height);
 			Length minW = child.getAttribute(LayoutConstants.minWidth);
 			Length minH = child.getAttribute(LayoutConstants.minHeight);
 
 			layout(child, false, parent.getHeight(), left, right, w, minW, parent.getWidth(), dim);
 			bounds.x = dim[0];
 			bounds.width = dim[1];
 			layout(child, true, parent.getWidth(), top, bottom, h, minH, parent.getHeight(), dim);
 			bounds.y = dim[0];
 			bounds.height = dim[1];
 			child.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
 		}
 	}
 
 	/**
 	 * Lays out a single child on one dimension within its parent based on its attributes and its size policy
 	 * 
 	 * @param child The child to position
 	 * @param vertical Whether the layout dimension is vertical (to get the child's sizer if needed)
 	 * @param breadth The size of the non-layout dimension of the parent
 	 * @param minPosAtt The value of the attribute controlling the child's minimum position (left or top)
 	 * @param maxPosAtt The value of the attribute controlling the child's maximum position (right or bottom)
 	 * @param sizeAtt The value of the attribute controlling the child's size (width or height)
 	 * @param minSizeAtt The value of the attribute controlling the child's minimum size(minWidth or minHeight)
 	 * @param length The length of the parent container along the dimension
 	 * @param dim The array to put the result (position (x or y) and size (width or height)) into
 	 */
 	protected void layout(MuisElement child, boolean vertical, int breadth, Length minPosAtt, Length maxPosAtt, Length sizeAtt,
 		Length minSizeAtt, int length, int [] dim)
 	{
 		if(maxPosAtt != null)
 		{
 			int max = maxPosAtt.evaluate(length);
 			if(minPosAtt != null)
 			{
 				dim[0] = minPosAtt.evaluate(length);
 				dim[1] = max - dim[0];
 			}
 			else if(sizeAtt != null)
 			{
 				dim[1] = sizeAtt.evaluate(length);
 				dim[0] = max - dim[1];
 			}
 			else
 			{
 				SizePolicy sizer = vertical ? child.getHSizer(breadth) : child.getWSizer(breadth);
 				dim[1] = sizer.getPreferred();
 				if(minSizeAtt != null && dim[1] < minSizeAtt.evaluate(length))
 					dim[1] = minSizeAtt.evaluate(length);
 				dim[0] = max - dim[1];
 			}
 		}
 		else if(sizeAtt != null)
 		{
 			dim[1] = sizeAtt.evaluate(length);
 			if(minSizeAtt != null && dim[1] < minSizeAtt.evaluate(length))
 				dim[1] = minSizeAtt.evaluate(length);
 			if(minPosAtt != null)
 				dim[0] = minPosAtt.evaluate(length);
 			else
 				dim[0] = 0;
 		}
 		else if(minPosAtt != null)
 		{
 			SizePolicy sizer = vertical ? child.getHSizer(breadth) : child.getWSizer(breadth);
 			dim[0] = minPosAtt.evaluate(length);
 			dim[1] = sizer.getPreferred();
 			if(minSizeAtt != null && dim[1] < minSizeAtt.evaluate(length))
 				dim[1] = minSizeAtt.evaluate(length);
 		}
 		else
 		{
 			SizePolicy sizer = vertical ? child.getHSizer(breadth) : child.getWSizer(breadth);
 			dim[0] = 0;
 			dim[1] = sizer.getPreferred();
 			if(minSizeAtt != null && dim[1] < minSizeAtt.evaluate(length))
 				dim[1] = minSizeAtt.evaluate(length);
 		}
 	}
 
 	@Override
 	public void remove(MuisElement parent)
 	{
 		for(MuisElement child : parent.getChildren())
 		{
 			child.removeListener(MuisElement.ATTRIBUTE_SET, RelayoutListener.class);
 			child.rejectAttribute(LayoutConstants.left);
 			child.rejectAttribute(LayoutConstants.right);
 			child.rejectAttribute(LayoutConstants.top);
 			child.rejectAttribute(LayoutConstants.bottom);
 			child.rejectAttribute(LayoutConstants.width);
 			child.rejectAttribute(LayoutConstants.height);
 			child.rejectAttribute(LayoutConstants.minWidth);
 			child.rejectAttribute(LayoutConstants.minHeight);
			child.rejectAttribute(LayoutConstants.maxWidth);
			child.rejectAttribute(LayoutConstants.maxHeight);
 		}
 	}
 }
