 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegionContainer;
 import org.dawnsci.plotting.api.region.ILockableRegion;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.dawnsci.plotting.draw2d.swtxy.util.Sector;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 import uk.ac.diamond.scisoft.analysis.roi.handler.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.roi.handler.SectorROIHandler;
 
 /**
  * You should not call this concrete class outside of the draw2d 
  * extensions unless absolutely required.
  * 
  * @author Peter Chang
  *
  */
 class SectorSelection extends AbstractSelectionRegion implements ILockableRegion{
 
 	DecoratedSector sector;
 
 	SectorSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.red);
 		setAlpha(80);
 		setLineWidth(2);
 	}
	
	public void setRegionColor(Color regionColor) {

		super.setRegionColor(regionColor);
		if (sector!=null) {
			sector.setForegroundColor(regionColor);
			sector.setBackgroundColor(regionColor);
		}
	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		getBean().setVisible(visible);
 		if (sector != null)
 			sector.setVisible(visible);
 	}
 
 	@Override
 	public void setMobile(boolean mobile) {
 		getBean().setMobile(mobile);
 		if (sector != null)
 			sector.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		sector = new DecoratedSector(parent);
 		sector.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(sector);
 		sync(getBean());
 		sector.setForegroundColor(getRegionColor());
 		sector.setAlpha(getAlpha());
 		sector.setLineWidth(getLineWidth());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int[] pix = coords.getValuePosition(x,y);
 		return sector.containsPoint(pix[0], pix[1]);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.SECTOR;
 	}
 
 	@Override
 	protected void updateConnectionBounds() { // called after a handle translation
 		if (sector != null) {
 			sector.updateFromHandles();
 			Rectangle b = sector.getBounds();
 			if (b != null)
 				sector.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		if (clicks.size() < 2)
 			return;
 
 		g.setLineStyle(Graphics.LINE_DOT);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 
 		final Point cen = clicks.getFirstPoint();
 		Point inn = clicks.getPoint(1);
 		Dimension rd = inn.getDifference(cen);
 		final double ri = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
 		if (clicks.size() == 2) {
 			g.setLineWidth(getLineWidth());
 			g.drawOval((int) (cen.preciseX() - ri), (int) (cen.preciseY() - ri), (int) (2*ri), (int) (2*ri));
 		} else {
 			double as = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
 			Point out = clicks.getPoint(2);
 			rd = out.getDifference(cen);
 			final double ro = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
 			double ae = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
 			double[] a = calcAngles(as, ae);
 			Sector s = new Sector(cen.preciseX(), cen.preciseY(), ri,  ro, a[0], a[1]);
 		    s.setLineStyle(Graphics.LINE_DOT);
 			s.setLineWidth(getLineWidth());
			s.setForegroundColor(getRegionColor());
			s.setBackgroundColor(getRegionColor());
 			s.paintFigure(g);
 		}
 	}
 
 	private Boolean clockwise = null;
 	private double[] calcAngles(double anglea, double angleb) {
 		if (anglea < 0)
 			anglea += 360;
 		if (angleb < 0)
 			angleb += 360;
 		if (clockwise == null) {
 			if (anglea == 0) {
 				clockwise = angleb > 180;
 			} else {
 				clockwise = anglea > angleb;
 			}
 		}
 
 		double l;
 		if (clockwise) {
 			if (anglea < 180) {
 				if (angleb < 180) {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= 360;
 				} else
 					l = angleb - 360 - anglea;
 			} else {
 				if (angleb < 180) {
 					l = angleb - anglea;
 				} else {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= 360;
 				}
 			}
 		} else {
 			if (anglea < 180) {
 				if (angleb < 180) {
 					l = angleb - anglea;
 					if (l < 0)
 						l += 360;
 				} else
 					l = angleb - anglea;
 			} else {
 				if (angleb < 180)
 					l = angleb - anglea + 360;
 				else {
 					l = angleb - anglea;
 					if (l < 0)
 						l += 360;
 				}
 			}
 		}
 
 		return l < 0 ? new double[] {anglea + l, anglea} : new double[] {anglea, anglea + l};
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (sector != null) {
 			sector.setup(clicks);
 			setRegionColor(getRegionColor());
 			setOpaque(false);
 			setAlpha(getAlpha());
 			updateConnectionBounds();
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-sector.png";
 	}
 
 	@Override
 	protected IROI createROI(boolean recordResult) {
 		final Point c = sector.getCentre();
 		final double[] r = sector.getRadii();
 		final double[] a = sector.getAnglesDegrees();
 		final double[] p1 = coords.getPositionValue(c.x(),c.y());
 		final double[] p2 = coords.getPositionValue((int) (c.preciseX() + r[0]), (int) (c.preciseY() + r[1]));
 		
 		final int symmetry = roi!=null ? ((SectorROI)roi).getSymmetry() : 0;
 		final boolean combine = roi!=null ? ((SectorROI)roi).isCombineSymmetry() : false;
 		
 		final SectorROI sroi = new SectorROI(p1[0], p1[1], p2[0]-p1[0],
 				                             p2[1]-p1[1],
 				                             Math.toRadians(360 - a[1]), Math.toRadians(360 - a[0]));
 		sroi.setName(getName());
 		sroi.setSymmetry(symmetry);
 		sroi.setCombineSymmetry(combine);
 		if (roi!=null) sroi.setPlot(roi.isPlot());
 		// set the Region isActive flag
 		this.setActive(this.isActive());
 		
 		if (recordResult) {
 			roi = sroi;
 		}
 
 		return sroi;
 	}
 
 	@Override
 	protected void updateROI(IROI roi) {
 		if (roi instanceof SectorROI) {
 			if (sector == null)
 				return;
 
 			sector.updateFromROI((SectorROI) roi);
 			updateConnectionBounds();
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 3;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (sector != null) {
 			sector.dispose();
 		}
 	}
 	
 	private boolean isCenterMovable=true;
 
 	class DecoratedSector extends Sector implements IRegionContainer {
 		private List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private SectorROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 
 		public DecoratedSector(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			this.parent = parent;
 			roiHandler = new SectorROIHandler((SectorROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedSector.this.parent.repaint();
 				}
 			};
			setBackgroundColor(getRegionColor());
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
 
 		/**
 		 * Set up sector according to clicks
 		 * @param points
 		 */
 		public void setup(PointList points) {
 			final Point cen = points.getFirstPoint();
 			Point inn = points.getPoint(1);
 			Dimension rd = inn.getDifference(cen);
 			final double ri = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
 
 			double as = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
 			Point out = points.getPoint(2);
 			rd = out.getDifference(cen);
 			final double ro = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
 			double ae = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
 			double[] a = calcAngles(as, ae);
 			setCentre(cen.preciseX(), cen.preciseY());
 			if (ri < ro)
 				setRadii(ri,  ro);
 			else
 				setRadii(ro,  ri);
 			setAnglesDegrees(a[0], a[1]);
 
 			roiHandler.setROI(createROI(true));
 			configureHandles();
 		}
 
 		private void configureHandles() {
 			// handles
 			FigureTranslator mover;
 			final int imax = roiHandler.size();
 			for (int i = 0; i < imax; i++) {
 				double[] hpt = roiHandler.getAnchorPoint(i, SIDE);
 				roiHandler.set(i, i);
 				
 				int[] p = coords.getValuePosition(hpt);
 				RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, p[0], p[1]);
 				h.setVisible(isVisible() && isMobile());
 				parent.add(h);
 				mover = new FigureTranslator(getXyGraph(), h);
 				mover.setActive(isMobile());
 				mover.addTranslationListener(handleListener);
 				fTranslators.add(mover);
 				h.addFigureListener(moveListener);
 				handles.add(h);
 			}
 
 			addFigureListener(moveListener);
 			mover = new FigureTranslator(getXyGraph(), parent, this, handles) {
 				public void mouseDragged(MouseEvent event) {
 					if (!isCenterMovable) return;
 					super.mouseDragged(event);
 				}
 			};
 			mover.setActive(isMobile());
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			for (IFigure h : handles) {
 				h.setVisible(visible);
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
 
 		public TranslationListener createRegionNotifier() {
 			return new TranslationListener() {
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					updateConnectionBounds();
 					fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					fireROIChanged(createROI(true));
 					roiHandler.setROI(roi);
 					fireROISelection();
 				}
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 				}
 			};
 		}
 
 		private TranslationListener createHandleNotifier() {
 			return new TranslationListener() {
 				private int[] spt;
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point start = translator.getStartLocation();
 						double[] c = coords.getPositionValue(start.x(), start.y());
 						spt = new int[]{(int)c[0], (int)c[1]};
 						final IFigure handle = translator.getRedrawFigure();
 						final int h = handles.indexOf(handle);
 						HandleStatus status = HandleStatus.RESIZE;
 						if (h == (roiHandler.size()-1)) {
 							status = HandleStatus.CMOVE;
 						} else if (h == 4) {
 							status = HandleStatus.RMOVE;
 						}
 						roiHandler.configureDragging(h, status);
 					}
 				}
 
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point end = translator.getEndLocation();
 						
 						double[] c = coords.getPositionValue(end.x(), end.y());
 						int[] r = new int[]{(int)c[0], (int)c[1]};
 
 						SectorROI croi = roiHandler.interpretMouseDragging(spt,r);
 
 						final SelectionHandle handle = (SelectionHandle) translator.getRedrawFigure();
 						updateFromROI(croi, handle);
 						fireROIDragged(croi, ROIEvent.DRAG_TYPE.RESIZE);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point end = translator.getEndLocation();
 
 						double[] c = coords.getPositionValue(end.x(), end.y());
 						int[] r = new int[]{(int)c[0], (int)c[1]};
 
 						SectorROI croi = roiHandler.interpretMouseDragging(spt,r);
 
 						updateFromROI(croi);
 						roiHandler.unconfigureDragging();
 						roi = croi;
 
 						fireROIChanged(croi);
 						fireROISelection();
 					}
 				}
 			};
 		}
 
 		/**
 		 * Update selection according to centre handle
 		 */
 		private void updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(roiHandler.size() - 1);
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
 		public void updateFromROI(SectorROI sroi) {
 			roiHandler.setROI(sroi);
 			updateFromROI(sroi, null);
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 * @param omit selection handle to not move
 		 */
 		private void updateFromROI(SectorROI sroi, SelectionHandle omit) {
 			final double x = sroi.getPointX();
 			final double y = sroi.getPointY();
 			final double[] r = sroi.getRadii();
 			final double[] a = sroi.getAnglesDegrees();
 
 			final int[] c  = coords.getValuePosition(x, y);
 			final int[] rd = coords.getValuePosition(x + r[0], y + r[1]);
 			setCentre(c[0], c[1]);
 			setRadii(rd[0] - c[0], rd[1] - c[1]);
 			setAnglesDegrees(360-a[1], 360-a[0]);
 			
 			if (sroi.getSymmetry() == SectorROI.NONE) {
 				setDrawSymmetry(false);
 			} else {
 				setDrawSymmetry(true);
 				double[] nang = sroi.getSymmetryAngles();
 				setSymmetryAnglesDegrees(-Math.toDegrees(nang[1]), -Math.toDegrees(nang[0]));
 			}
 
 			int imax = handles.size();
 			if (imax != roiHandler.size()) {
 				configureHandles();
 			} else {
 				SectorROIHandler handler = new SectorROIHandler(sroi);
 				for (int i = 0; i < imax; i++) {
 					double[] hpt = handler.getAnchorPoint(i, SIDE);
 					SelectionHandle handle = (SelectionHandle) handles.get(i);
 					if (handle != omit) {
 						int[] pnt  = coords.getValuePosition(hpt);
 						handle.setSelectionPoint(new PrecisionPoint(pnt[0], pnt[1]));
 					}
 				}
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return SectorSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 
 		public List<FigureTranslator> getHandleTranslators() {
 			return fTranslators;
 		}
 		public List<IFigure> getHandles() {
 			return handles;
 		}
 	}
 
 	public boolean isCenterMovable() {
 		return isCenterMovable;
 	}
 
 	public void setCenterMovable(boolean isCenterMovable) {
 		this.isCenterMovable = isCenterMovable;
 		if (isCenterMovable) {
 			sector.setCursor(Draw2DUtils.getRoiMoveCursor());
 			sector.getHandleTranslators().get(sector.getHandleTranslators().size()-1).setActive(true);
 			sector.getHandles().get(sector.getHandles().size()-1).setVisible(true);
 		} else {
 			sector.setCursor(null);			
 			sector.getHandleTranslators().get(sector.getHandleTranslators().size()-1).setActive(false);
 			sector.getHandles().get(sector.getHandles().size()-1).setVisible(false);
 		}
 	}
 }
