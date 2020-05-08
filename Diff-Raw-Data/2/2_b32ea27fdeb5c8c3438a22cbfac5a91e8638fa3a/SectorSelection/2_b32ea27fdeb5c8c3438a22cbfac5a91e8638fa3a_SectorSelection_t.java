 package org.dawb.workbench.plotting.system.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
 import org.dawb.workbench.plotting.system.swtxy.translate.TranslationEvent;
 import org.dawb.workbench.plotting.system.swtxy.translate.TranslationListener;
 import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
 import org.dawb.workbench.plotting.system.swtxy.util.Sector;
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
 
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.SectorROIHandler;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class SectorSelection extends AbstractSelectionRegion {
 
 	DecoratedSector sector;
 
 	public SectorSelection(String name, Axis xAxis, Axis yAxis) {
 		super(name, xAxis, yAxis);
 		setRegionColor(ColorConstants.red);
 		setAlpha(80);
 		setLineWidth(2);
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
 		final int xpix = xAxis.getValuePosition(x, false);
 		final int ypix = yAxis.getValuePosition(y, false);
 		return sector.containsPoint(xpix, ypix);
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
 	protected ROIBase createROI(boolean recordResult) {
 		final Point c = sector.getCentre();
 		final double[] r = sector.getRadii();
 		final double[] a = sector.getAnglesDegrees();
 		final double x = xAxis.getPositionValue(c.x(), false);
 		final double y = yAxis.getPositionValue(c.y(), false);
 		
 		final int symmetry = roi!=null ? ((SectorROI)roi).getSymmetry() : 0;
 		final boolean combine = roi!=null ? ((SectorROI)roi).isCombineSymmetry() : false;
 		
 		final SectorROI sroi = new SectorROI(x, y, xAxis.getPositionValue((int) (c.preciseX() + r[0]), false) - x,
 				                             yAxis.getPositionValue((int) (c.preciseY() + r[1]), false) - y,
 				                             Math.toRadians(360 - a[1]), Math.toRadians(360 - a[0]));
 		sroi.setSymmetry(symmetry);
 		sroi.setCombineSymmetry(combine);
 		
 		if (recordResult) {
 			roi = sroi;
 		}
 
 		return sroi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase roi) {
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
 
 	class DecoratedSector extends Sector implements IRegionContainer {
 		private List<IFigure> handles;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private SectorROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 
 		public DecoratedSector(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			this.parent = parent;
 			roiHandler = new SectorROIHandler((SectorROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedSector.this.parent.repaint();
 				}
 			};
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
 				RectangularHandle h = new RectangularHandle(xAxis, yAxis, getRegionColor(), this, SIDE,
 						xAxis.getValuePosition(hpt[0], false), yAxis.getValuePosition(hpt[1], false));
 				parent.add(h);
 				mover = new FigureTranslator(getXyGraph(), h);
 				mover.addTranslationListener(handleListener);
 				h.addFigureListener(moveListener);
 				handles.add(h);
 			}
 
 			addFigureListener(moveListener);
 			mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 			mover.addTranslationListener(createRegionNotifier());
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
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
 						spt = new int[] { (int) xAxis.getPositionValue(start.x(), false), (int) yAxis.getPositionValue(start.y(), false) };
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
 						
 						SectorROI croi = roiHandler.interpretMouseDragging(spt,
 								new int[] { (int) xAxis.getPositionValue(end.x(), false), (int) yAxis.getPositionValue(end.y(), false) });
 
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
 
 						SectorROI croi = roiHandler.interpretMouseDragging(spt,
 								new int[] { (int) xAxis.getPositionValue(end.x(), false), (int) yAxis.getPositionValue(end.y(), false) });
 
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
 
 			final double cx = xAxis.getValuePosition(x, false);
 			final double cy = yAxis.getValuePosition(y, false);
 			setCentre(cx, cy);
 			setRadii(xAxis.getValuePosition(x + r[0], false) - cx, yAxis.getValuePosition(y + r[1], false) - cy);
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
 				roiHandler.setROI(sroi);
 				configureHandles();
 			} else {
 				SectorROIHandler handler = new SectorROIHandler(sroi);
 				for (int i = 0; i < imax; i++) {
 					double[] hpt = handler.getAnchorPoint(i, SIDE);
 					SelectionHandle handle = (SelectionHandle) handles.get(i);
 					if (handle != omit) {
 						handle.setSelectionPoint(new PrecisionPoint(xAxis.getValuePosition(hpt[0], false), yAxis.getValuePosition(hpt[1], false)));
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
 	}
 }
