 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatableEllipse;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class EllipseSelection extends AbstractSelectionRegion {
 
 	DecoratedEllipse ellipse;
 
 	public EllipseSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.lightGreen);
 		setAlpha(80);
 		setLineWidth(2);
 		labelColour = ColorConstants.black;
 		labelFont = new Font(Display.getCurrent(), "Dialog", 10, SWT.BOLD);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		getBean().setVisible(visible);
 		if (ellipse != null)
 			ellipse.setVisible(visible);
 	}
 
 	@Override
 	public void setMobile(final boolean mobile) {
 		getBean().setMobile(mobile);
 		if (ellipse != null)
 			ellipse.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		ellipse = new DecoratedEllipse(parent);
 		ellipse.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(ellipse);
 		sync(getBean());
 		ellipse.setForegroundColor(getRegionColor());
 		ellipse.setAlpha(getAlpha());
 		ellipse.setLineWidth(getLineWidth());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int[] pix = coords.getValuePosition(x,y);
 		return ellipse.containsPoint(pix[0], pix[1]);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.ELLIPSE;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (ellipse != null) {
 			ellipse.updateFromHandles();
 			Rectangle b = ellipse.getBounds();
 			if (b != null)
 				ellipse.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		if (clicks.size() <= 1)
 			return;
 
 		g.setLineStyle(SWT.LINE_DOT);
 		g.setLineWidth(2);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.drawOval(new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint()));
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (ellipse != null) {
 			ellipse.setup(clicks);
 			setRegionColor(getRegionColor());
 			setOpaque(false);
 			setAlpha(getAlpha());
 			updateConnectionBounds();
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-ellipse.png";
 	}
 
 	@Override
 	protected ROIBase createROI(boolean recordResult) {
 		final EllipticalROI eroi = new EllipticalROI();
 		Point p = ellipse.getCentre();
 		eroi.setPoint(coords.getPositionValue(p.x(), p.y()));
 		eroi.setAngleDegrees(ellipse.getAngleDegrees());
 		double[] axes = ellipse.getAxes();
 		double[] v = coords.getPositionValue((int) (p.preciseX() + axes[0]), (int) (p.preciseY() + axes[1]));
 		v[0] -= eroi.getPointX(); v[0] *= 0.5;
 		v[1] -= eroi.getPointY(); v[1] *= 0.5;
 		eroi.setSemiAxes(v);
 
 		if (roi!=null) eroi.setPlot(roi.isPlot());
 //		System.err.println("To roi, " + eroi.toString());
 		if (recordResult) {
 			roi = eroi;
 		}
 		return eroi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase roi) {
 		if (ellipse == null)
 			return;
 
 		if (roi instanceof EllipticalROI) {
 			ellipse.updateFromROI((EllipticalROI) roi);
 			updateConnectionBounds();
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 2;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (ellipse != null) {
 			ellipse.dispose();
 		}
 	}
 
 	class DecoratedEllipse extends RotatableEllipse implements IRegionContainer {
 		List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private static final int SIDE = 8;
 		public DecoratedEllipse(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			this.parent = parent;
 			setFill(false);
 			handleListener = createHandleNotifier();
 			showMajorAxis(true);
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedEllipse.this.parent.repaint();
 				}
 			};
 		}
 
 		public void dispose() {
 			for (IFigure f : handles) {
 				((SelectionHandle) f).removeMouseListeners();
 			}
 			for (FigureTranslator t : fTranslators) {
 				t.removeTranslationListeners();
 			}
 			removeFigureListener(moveListener);
 		}
 
 		public void setup(PointList corners) {
 			Rectangle r = new Rectangle(corners.getFirstPoint(), corners.getLastPoint());
 			if (r.preciseWidth() < r.preciseHeight()) {
 				setAxes(r.preciseHeight(), r.preciseWidth());
 				setAngle(90);
 			} else {
 				setAxes(r.preciseWidth(), r.preciseHeight());
 			}
 			Point c = r.getCenter();
 			setCentre(c.preciseX(), c.preciseY());
 
 			createHandles(true);
 		}
 
 		private void createHandles(boolean createROI) {
 			boolean mobile = isMobile();
 			boolean visible = isVisible() && mobile;
 			// handles
 			for (int i = 0; i < 4; i++) {
 				addHandle(getPoint(i*90), mobile, visible);
 			}
 			addCentreHandle(mobile, visible);
 
 			// figure move
 			addFigureListener(moveListener);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 			mover.setActive(isMobile());
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 
 			if (createROI)
 				createROI(true);
 
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		@Override
 		protected void outlineShape(Graphics graphics) {
 			super.outlineShape(graphics);
 			if (label != null && isShowLabel()) {
				graphics.setAlpha(192);
 				graphics.setForegroundColor(labelColour);
				graphics.setBackgroundColor(ColorConstants.white);
 				graphics.setFont(labelFont);
				graphics.fillString(label, getPoint(45));
 			}
 		}
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			boolean hVisible = visible && isMobile();
 			for (IFigure h : handles) {
 				h.setVisible(hVisible);
 			}
 		}
 
 		public void setMobile(boolean mobile) {
 			for (IFigure h : handles) {
 				h.setVisible(mobile);
 			}
 			for (FigureTranslator f : fTranslators) {
 				f.setActive(mobile);
 			}
 		}
 
 		private void addHandle(Point p, boolean mobile, boolean visible) {
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE,
 					p.preciseX(), p.preciseY());
 			h.setVisible(visible);
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
 			mover.setActive(mobile);
 			mover.addTranslationListener(handleListener);
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		private void addCentreHandle(boolean mobile, boolean visible) {
 			Point c = getCentre();
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, c.preciseX(), c.preciseY());
 			h.setVisible(visible);
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h, h, handles);
 			mover.setActive(mobile);
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		@Override
 		public String toString() {
 			return "EllSel: cen=" + getCentre() + ", axes=" + Arrays.toString(getAxes()) + ", ang=" + getAngleDegrees();
 		}
 
 		private TranslationListener createHandleNotifier() {
 			return new TranslationListener() {
 				SelectionHandle ha;
 				private boolean major;
 				private Point centre;
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						ha = (SelectionHandle) ((FigureTranslator) src).getRedrawFigure();
 						centre = getCentre();
 						int current = handles.indexOf(ha);
 						switch (current) {
 						case 0: case 2:
 							major = true;
 							break;
 						case 1: case 3:
 							major = false;
 							break;
 						default:
 							ha = null;
 							centre = null;
 							break;
 						}
 					}
 				}
 
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						if (ha != null) {
 							double[] axes = getAxes();
 							Point pa = getInverseTransformedPoint(new PrecisionPoint(ha.getSelectionPoint()));
 							Point pb = getInverseTransformedPoint(centre);
 							Dimension d = pa.getDifference(pb);
 							if (major) {
 								axes[0] *= 2. * Math.abs(d.preciseWidth());
 							} else {
 								axes[1] *= 2. * Math.abs(d.preciseHeight());
 							}
 							setAxes(axes[0], axes[1]);
 							updateHandlePositions();
 						}
 						fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						fireROIChanged(createROI(true));
 						fireROISelection();
 					}
 					ha = null;
 					centre = null;
 				}
 			};
 		}
 
 		/**
 		 * @return list of handle points (can be null)
 		 */
 		public PointList getPoints() {
 			int imax = handles.size() - 1;
 			if (imax < 0)
 				return null;
 
 			PointList pts = new PointList(imax);
 			for (int i = 0; i < imax; i++) {
 				IFigure f = handles.get(i);
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					pts.addPoint(h.getSelectionPoint());
 				}
 			}
 			return pts;
 		}
 
 		/**
 		 * Update selection according to centre handle
 		 */
 		private void updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(handles.size() - 1);
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					Point p = h.getSelectionPoint();
 					setCentre(p.preciseX(), p.preciseY());
 				}
 			}
 		}
 
 		@Override
 		public Rectangle getBounds() {
 			Rectangle b = super.getBounds();
 			if (handles != null)
 				for (IFigure f : handles) {
 					if (f instanceof SelectionHandle) {
 						SelectionHandle h = (SelectionHandle) f;
 						b.union(h.getBounds());
 					}
 				}
 			return b;
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 */
 		public void updateFromROI(EllipticalROI eroi) {
 			final double[] xy = eroi.getPointRef();
 			int[] p1 = coords.getValuePosition(xy[0], xy[1]);
 			int[] p2 = coords.getValuePosition(2*eroi.getSemiAxis(0) + xy[0], 2*eroi.getSemiAxis(1) + xy[1]);
 
 			setAxes(p2[0] - p1[0], p2[1] - p1[1]);
 
 			setCentre(p1[0], p1[1]);
 			setAngle(eroi.getAngleDegrees());
 
 			updateHandlePositions();
 		}
 
 		private void updateHandlePositions() {
 			final int imax = handles.size() - 1;
 			if (imax > 0) {
 				for (int i = 0; i < imax; i++) {
 					Point np = getPoint(90 * i);
 					SelectionHandle h = (SelectionHandle) handles.get(i);
 					h.setSelectionPoint(np);
 				}
 				SelectionHandle h = (SelectionHandle) handles.get(imax);
 				h.setSelectionPoint(getCentre());
 			} else {
 				createHandles(false);
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return EllipseSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 	}
 }
