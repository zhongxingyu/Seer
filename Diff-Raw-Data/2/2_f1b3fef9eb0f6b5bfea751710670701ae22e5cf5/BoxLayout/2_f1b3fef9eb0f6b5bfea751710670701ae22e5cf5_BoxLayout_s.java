 package org.muis.base.layout;
 
 import static org.muis.core.layout.LayoutAttributes.*;
 
 import java.awt.Rectangle;
 
 import org.muis.core.MuisElement;
 import org.muis.core.MuisLayout;
 import org.muis.core.layout.*;
 import org.muis.core.style.LayoutStyles;
 import org.muis.core.style.Size;
 import org.muis.util.CompoundListener;
 
 /**
  * Lays out children one-by-one along a given {@link LayoutAttributes#direction direction} ({@link Direction#DOWN DOWN} by default), with a
  * given {@link LayoutAttributes#alignment alignment} along the opposite axis. {@link LayoutAttributes#width},
  * {@link LayoutAttributes#height}, {@link LayoutAttributes#minWidth}, and {@link LayoutAttributes#minHeight} may be used to help determine
  * the sizes of children.
  */
 public class BoxLayout implements MuisLayout {
 	private final CompoundListener.MultiElementCompoundListener theListener;
 
 	/** Creates a box layout */
 	public BoxLayout() {
 		theListener = CompoundListener.create(this);
 		theListener.acceptAll(direction, alignment, crossAlignment).onChange(CompoundListener.layout);
 		theListener.child().acceptAll(width, minWidth, maxWidth, height, minHeight, maxHeight).onChange(CompoundListener.layout);
 	}
 
 	@Override
 	public void initChildren(MuisElement parent, MuisElement [] children) {
 		theListener.listenerFor(parent);
 	}
 
 	@Override
 	public void childAdded(MuisElement parent, MuisElement child) {
 	}
 
 	@Override
 	public void childRemoved(MuisElement parent, MuisElement child) {
 	}
 
 	@Override
 	public void remove(MuisElement parent) {
 		theListener.dropFor(parent);
 	}
 
 	@Override
 	public SizeGuide getWSizer(MuisElement parent, MuisElement [] children) {
 		Direction dir = parent.atts().get(direction, Direction.RIGHT);
 		Size margin = parent.getStyle().getSelf().get(LayoutStyles.margin);
 		Size padding = parent.getStyle().getSelf().get(LayoutStyles.padding);
 		switch (dir) {
 		case UP:
 		case DOWN:
 			return getCrossSizer(children, dir.getOrientation(), margin);
 		case LEFT:
 		case RIGHT:
 			return getMainSizer(children, dir.getOrientation(), margin, padding);
 		}
 		throw new IllegalStateException("Unrecognized layout direction: " + dir);
 	}
 
 	@Override
 	public SizeGuide getHSizer(MuisElement parent, MuisElement [] children) {
 		Direction dir = parent.atts().get(direction, Direction.RIGHT);
 		Size margin = parent.getStyle().getSelf().get(LayoutStyles.margin);
 		Size padding = parent.getStyle().getSelf().get(LayoutStyles.padding);
 		switch (dir) {
 		case UP:
 		case DOWN:
 			return getMainSizer(children, dir.getOrientation(), margin, padding);
 		case LEFT:
 		case RIGHT:
 			return getCrossSizer(children, dir.getOrientation(), margin);
 		}
 		throw new IllegalStateException("Unrecognized layout direction: " + dir);
 	}
 
 	/**
 	 * Gets the size policy in the main direction of the container
 	 *
 	 * @param children The children to get the sizer for
 	 * @param orient The orientation for the layout
 	 * @param margin The margin size for the parent
 	 * @param padding The padding size for the parent
 	 * @return The size policy for the children
 	 */
 	protected SizeGuide getMainSizer(final MuisElement [] children, final Orientation orient, final Size margin, final Size padding) {
 		return new SizeGuide() {
 			@Override
 			public int getMin(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.min, crossSize, csMax);
 			}
 
 			@Override
 			public int getMinPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.minPref, crossSize, csMax);
 			}
 
 			@Override
 			public int getPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.pref, crossSize, csMax);
 			}
 
 			@Override
 			public int getMaxPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.maxPref, crossSize, csMax);
 			}
 
 			@Override
 			public int getMax(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.max, crossSize, csMax);
 			}
 
 			@Override
 			public int get(LayoutGuideType type, int crossSize, boolean csMax) {
 				return BaseLayoutUtils.getBoxLayoutSize(children, orient, type, crossSize, csMax, margin, margin, padding, padding);
 			}
 
 			@Override
 			public int getBaseline(int size) {
 				return 0;
 			}
 		};
 	}
 
 	/**
 	 * Gets the size policy in the non-main direction of the container
 	 *
 	 * @param children The children to get the sizer for
 	 * @param orient The orientation for the layout (not the cross direction of the layout)
 	 * @param margin The margin size for the parent
 	 * @return The size policy for the children
 	 */
 	protected SizeGuide getCrossSizer(final MuisElement [] children, final Orientation orient, final Size margin) {
 		return new SizeGuide() {
 			@Override
 			public int getMin(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.min, crossSize, csMax);
 			}
 
 			@Override
 			public int getMinPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.minPref, crossSize, csMax);
 			}
 
 			@Override
 			public int getPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.pref, crossSize, csMax);
 			}
 
 			@Override
 			public int getMaxPreferred(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.maxPref, crossSize, csMax);
 			}
 
 			@Override
 			public int getMax(int crossSize, boolean csMax) {
 				return get(LayoutGuideType.max, crossSize, csMax);
 			}
 
 			@Override
 			public int get(LayoutGuideType type, int crossSize, boolean csMax) {
 				LayoutSize ret = new LayoutSize();
 				BaseLayoutUtils.getBoxLayoutCrossSize(children, orient, type, crossSize, csMax, ret);
 				ret.add(margin);
 				ret.add(margin);
 				return ret.getTotal();
 			}
 
 			@Override
 			public int getBaseline(int size) {
 				return 0;
 			}
 		};
 	}
 
 	@Override
 	public void layout(MuisElement parent, final MuisElement [] children) {
 		final Direction dir = parent.atts().get(direction, Direction.RIGHT);
 		Alignment align = parent.atts().get(alignment, Alignment.begin);
 		Alignment crossAlign = parent.atts().get(crossAlignment, Alignment.begin);
 		final Size margin = parent.getStyle().getSelf().get(LayoutStyles.margin);
 		final Size padding = parent.getStyle().getSelf().get(LayoutStyles.padding);
 
 		final int parallelSize = parent.bounds().get(dir.getOrientation()).getSize();
 		final int crossSize = parent.bounds().get(dir.getOrientation().opposite()).getSize();
 		final int crossSizeWOMargin = crossSize - margin.evaluate(crossSize) * 2;
 		LayoutUtils.LayoutInterpolation<LayoutSize []> result = LayoutUtils.interpolate(new LayoutUtils.LayoutChecker<LayoutSize []>() {
 			@Override
 			public LayoutSize [] getLayoutValue(LayoutGuideType type) {
 				LayoutSize [] ret = new LayoutSize[children.length];
 				for(int i = 0; i < ret.length; i++) {
 					ret[i] = new LayoutSize();
 					LayoutUtils.getSize(children[i], dir.getOrientation(), type, parallelSize, crossSizeWOMargin, true, ret[i]);
 				}
 				return ret;
 			}
 
 			@Override
 			public int getSize(LayoutSize [] layoutValue) {
 				LayoutSize ret = new LayoutSize();
 				ret.add(margin);
 				ret.add(margin);
 				for(int i = 0; i < layoutValue.length; i++) {
 					if(i > 0)
 						ret.add(padding);
 					ret.add(layoutValue[i]);
 				}
 				return ret.getTotal(parallelSize);
 			}
 		}, parallelSize, true, align == Alignment.justify);
 
 		Rectangle [] bounds = new Rectangle[children.length];
 		for(int c = 0; c < children.length; c++) {
 			bounds[c] = new Rectangle();
 			int mainSize = result.lowerValue[c].getTotal(parallelSize);
 			if(result.proportion > 0)
 				mainSize += Math.round(result.proportion
 					* (result.upperValue[c].getTotal(parallelSize) - result.lowerValue[c].getTotal(parallelSize)));
 			LayoutUtils.setSize(bounds[c], dir.getOrientation(), mainSize);
 
 			int prefCrossSize = LayoutUtils.getSize(children[c], dir.getOrientation().opposite(), LayoutGuideType.pref, crossSize,
 				mainSize, false, null);
 			int oppSize;
 			if(crossSize < prefCrossSize) {
 				int minCrossSize = LayoutUtils.getSize(children[c], dir.getOrientation().opposite(), LayoutGuideType.min, crossSize,
 					mainSize, false, null);
 				if(crossSize < minCrossSize)
 					oppSize = minCrossSize;
 				else
 					oppSize = crossSize;
 			} else if(align == Alignment.justify) {
 				int maxCrossSize = LayoutUtils.getSize(children[c], dir.getOrientation().opposite(), LayoutGuideType.max, crossSize,
 					mainSize, false, null);
 				if(crossSize < maxCrossSize)
 					oppSize = crossSize;
 				else
 					oppSize = maxCrossSize;
 			} else
 				oppSize = prefCrossSize;
 			LayoutUtils.setSize(bounds[c], dir.getOrientation().opposite(), oppSize);
 		}
 
 		// Main alignment
 		switch (align) {
 		case begin:
 		case end:
 			int pos = 0;
 			for(int c = 0; c < children.length; c++) {
 				int childPos = pos;
 				if(c == 0)
 					childPos += margin.evaluate(parallelSize);
 				else
 					childPos += padding.evaluate(parallelSize);
 				LayoutUtils.setPos(bounds[c], dir.getOrientation(), align == Alignment.begin ? childPos : parallelSize - childPos
 					- LayoutUtils.getSize(bounds[c], dir.getOrientation()));
 				pos = childPos + LayoutUtils.getSize(bounds[c], dir.getOrientation());
 			}
 			break;
 		case center:
 		case justify:
 			int freeSpace = parallelSize;
 			freeSpace -= margin.evaluate(parallelSize);
 			for(int c = 0; c < bounds.length; c++) {
 				if(c > 0)
 					freeSpace -= padding.evaluate(parallelSize);
 				freeSpace -= LayoutUtils.getSize(bounds[c], dir.getOrientation());
 			}
 
 			pos = 0;
 			int usedSpace = 0;
 			for(int c = 0; c < children.length; c++) {
 				int extraSpace = (freeSpace - usedSpace) / (children.length + 1 - c);
 				int childPos = pos + extraSpace;
 				if(c == 0)
 					childPos += margin.evaluate(parallelSize);
 				else
 					childPos += padding.evaluate(parallelSize);
 				LayoutUtils.setPos(bounds[c], dir.getOrientation(), align == Alignment.begin ? childPos : parallelSize - childPos
 					- LayoutUtils.getSize(bounds[c], dir.getOrientation()));
 				pos = childPos + LayoutUtils.getSize(bounds[c], dir.getOrientation());
 				usedSpace += extraSpace;
 			}
 		}
 
 		// Cross alignment
 		switch (crossAlign) {
 		case begin:
 			for(Rectangle bound : bounds)
 				LayoutUtils.setPos(bound, dir.getOrientation().opposite(), margin.evaluate(crossSize));
 			break;
 		case end:
 			for(Rectangle bound : bounds)
 				LayoutUtils.setPos(bound, dir.getOrientation().opposite(),
 					crossSize - LayoutUtils.getSize(bound, dir.getOrientation().opposite()) - margin.evaluate(crossSize));
 			break;
 		case center:
 		case justify:
 			for(Rectangle bound : bounds)
 				LayoutUtils.setPos(bound, dir.getOrientation().opposite(),
					margin.evaluate(crossSize) + (crossSize - LayoutUtils.getSize(bound, dir.getOrientation().opposite())) / 2);
 		}
 
 		for(int c = 0; c < children.length; c++)
 			children[c].bounds().setBounds(bounds[c].x, bounds[c].y, bounds[c].width, bounds[c].height);
 	}
 }
