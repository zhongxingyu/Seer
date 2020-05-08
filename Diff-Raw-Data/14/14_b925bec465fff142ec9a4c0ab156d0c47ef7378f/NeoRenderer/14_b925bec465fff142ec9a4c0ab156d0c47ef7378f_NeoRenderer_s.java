 package org.amanzi.awe.render.network;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 
 import org.amanzi.awe.catalog.neo.actions.Feature;
 import org.amanzi.awe.catalog.neo.actions.NeoReader;
 import org.amanzi.awe.catalog.neo.beans.Sector;
 import org.amanzi.awe.neo.views.network.views.NeoNetworkView;
 import org.amanzi.awe.views.network.utils.ITreeSelectionChanged;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Point;
 
 public class NeoRenderer extends RendererImpl implements ITreeSelectionChanged 
 {
 	private static final Color SELECTION_COLOR = Color.RED;
 	private AffineTransform base_transform = null; // save original graphics
 	// transform for repeated
 	// re-use
 	private Color drawColor = Color.DARK_GRAY;
 	private Color fillColor = new Color(120, 255, 170);
 	private MathTransform transform_d2w;
 	private MathTransform transform_w2d;
 	private List<String> selectedTreeItems = new ArrayList<String>();
 
 	public NeoRenderer() {
 		Display display = PlatformUI.getWorkbench().getDisplay();
 		display.syncExec(new Runnable() {
 
 			public void run() {
 				final IWorkbenchWindow window = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow();
 				try {
 					final NeoNetworkView viewPart = (NeoNetworkView) window
 							.getActivePage().showView(
 									NeoNetworkView.NETWORK_VIEW_ID);
					viewPart.addChangeListeners(NeoRenderer.this);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	@Override
 	public final void render(final Graphics2D g, final IProgressMonitor monitor)
 			throws RenderException {
 		ILayer layer = getContext().getLayer();
 		IGeoResource resource = layer.findGeoResource(NeoReader.class);
 		
 			renderNeo(g, resource, monitor);
 		}
 
 
 	private void renderSector(final Graphics2D g, final java.awt.Point p,
 			final double azimuth, final double beamwidth) {
 		renderSector(g, p, azimuth, beamwidth, false);
 	}
 
 	private void renderSector(final Graphics2D g, final java.awt.Point p,
 			final double azimuth, double beamwidth, final boolean selected) {
 		if (base_transform == null) {
 			base_transform = g.getTransform();
 		}
 		if (beamwidth < 10) {
 			beamwidth = 10;
 		}
 
 		g.setTransform(base_transform);
 		g.translate(p.x, p.y);
 		g.rotate(Math.toRadians(-90 + azimuth - beamwidth / 2.0));
 		g.setColor(fillColor);
 		if (selected) {
 			g.setColor(SELECTION_COLOR);
 		}
 
 		g.fillArc(-20, -20, 40, 40, 0, -(int) beamwidth);
 		g.setColor(drawColor);
 
 		g.drawArc(-20, -20, 40, 40, 0, -(int) beamwidth);
 		g.drawLine(0, 0, 20, 0);
 		g.rotate(Math.toRadians(beamwidth));
 		g.drawLine(0, 0, 20, 0);
 
 		g.setColor(drawColor);
 	}
 
 	/**
 	 * This one is very simple, just draw a circle at the site location.
 	 * 
 	 * @param g
 	 * @param p
 	 */
 	private void renderSite(final Graphics2D g, final java.awt.Point p) {
 		g.fillOval(p.x - 5, p.y - 5, 10, 10);
 	}
 
 	/**
 	 * Draws a red circle round the point that represents selection of site.
 	 * 
 	 * @param g
 	 *            {@link Graphics2D} object
 	 * @param p
 	 *            {@link java.awt.Point} object
 	 * @param labelString
 	 */
 	private void renderSelector(final Graphics2D g, final java.awt.Point p,
 			final boolean selected) {
 		if (selected) {
 			g.setColor(SELECTION_COLOR);
 			g.drawOval(p.x - 10, p.y - 10, 20, 20);
 			g.setColor(drawColor);
 		}
 	}
 
 	@Override
 	public final void render(final IProgressMonitor monitor)
 			throws RenderException {
 
 		final Graphics2D g = getContext().getImage().createGraphics();
 		render(g, monitor);
 	}
 
 	public void update(List<String> selectedTreeItems) {
 		this.selectedTreeItems = selectedTreeItems;
 		setState(RENDER_REQUEST);
 	}
 
 	private void renderNeo(final Graphics2D g,
 			final IGeoResource neoGeoResource, IProgressMonitor monitor)
 			throws RenderException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		monitor.beginTask("render network sites and sectors",
 				IProgressMonitor.UNKNOWN);
 		// TODO: Get size from info
 
 		try {
 
 			monitor.subTask("connecting");
 
 			NeoReader neoReader = neoGeoResource.resolve(NeoReader.class,
 					new SubProgressMonitor(monitor, 10));
 
 			setCrsTransforms(neoGeoResource.getInfo(null).getCRS());
 			Envelope bounds_transformed = getTransformedBounds();
 
 			g.setColor(drawColor);
 			int count = 0;
 			monitor.subTask("drawing");
 			Coordinate world_location = new Coordinate(); // single object for
 															// re-use in
 															// transform
 			// below (minimize object creation)
 
 			for (Feature feature : neoReader.getFeatures()) {
 				Point[] points = feature.getPoints();
 				Point point = points[0]; // TODO: Support multi-point geometries
 				Coordinate location = point.getCoordinate();
 
 				if (bounds_transformed != null
 						&& !bounds_transformed.contains(location)) {
 					continue; // Don't draw points outside viewport
 				}
 				try {
 					JTS.transform(location, world_location, transform_d2w);
 				} catch (Exception e) {
 					// JTS.transform(location, world_location,
 					// transform_w2d.inverse());
 				}
 
 				java.awt.Point p = getContext().worldToPixel(world_location);
 				renderSite(g, p);
 
 				double[] label_position_angles = new double[] { 0, 90 };
 				Map<String, Object> properties = feature.getProperties();
 
 				String bsc = (String) properties.get("bsc");
 				boolean selected = false;
 				if (!selectedTreeItems.isEmpty()) {
 					if (selectedTreeItems.contains(bsc)
 							|| selectedTreeItems.contains(feature.toString())) {
 						selected = true;
 					}
 				}
 
 				if (properties != null) {
 					try {
 						if (properties.containsKey("sectors")) {
 							Object sectorsObj = properties.get("sectors");
 							System.out.println("Found sectors: " + sectorsObj);
 							if (sectorsObj instanceof Object[]) {
 								Sector[] sectors = (Sector[]) sectorsObj;
 								for (int s = 0; s < sectors.length; s++) {
 									Sector sector = sectors[s];
 									if (sector != null) {
 
 										double azimuth = sector.getAzimuth();
 										double beamwidth = sector
 												.getBeamwidth();
 
 										boolean selectedSector = false;
 
 										if (sector.getName() != null) {
 											if (selected) {
 												selectedSector = true;
 											} else {
 												final String sectorName = sector
 														.getName();
 												if (selectedTreeItems
 														.contains(sectorName)) {
 													selectedSector = true;
 												}
 											}
 											renderSector(g, p, azimuth,
 													beamwidth, selectedSector);
 										} else {
 											renderSector(g, p, azimuth,
 													beamwidth);
 										}
 
 										if (s < label_position_angles.length) {
 											label_position_angles[s] = azimuth;
 										}
 										// g.setColor(drawColor);
 										// g.rotate(-Math.toRadians(beamwidth/2));
 										// g.drawString(sector.getString("name"),20,0);
 									}
 								}
 							} else {
 								System.err.println(" " + sectorsObj);
 							}
 						}
 					} finally {
 						if (base_transform != null) {
 							// recover the normal transform
 							g.setTransform(base_transform);
 							g.setColor(drawColor);
 						}
 					}
 				}
 				renderSelector(g, p, selected);
 
 				double label_position_angle = Math.toRadians(-90
 						+ (label_position_angles[0] + label_position_angles[1])
 						/ 2.0);
 				int label_x = 5 + (int) (10 * Math.cos(label_position_angle));
 				int label_y = (int) (10 * Math.sin(label_position_angle));
 				g.drawString(feature.toString(), p.x + label_x, p.y + label_y);
 				g.setTransform(base_transform);
 				monitor.worked(1);
 				count++;
 				if (monitor.isCanceled()) {
 					break;
 				}
 			}
 			// updateNetworkTreeView(jsonReader);
 		} catch (TransformException e) {
 			throw new RenderException(e);
 		} catch (FactoryException e) {
 			throw new RenderException(e);
 		} catch (IOException e) {
 			throw new RenderException(e); // rethrow any exceptions encountered
 		} finally {
 			// if (jsonReader != null)
 			// jsonReader.close();
 			monitor.done();
 		}
 	}
 
 	private void setCrsTransforms(final CoordinateReferenceSystem dataCrs)
 			throws FactoryException {
 		boolean lenient = true; // needs to be lenient to work on uDIG 1.1
 		// (otherwise we get error:
 		// bursa wolf parameters required
 		CoordinateReferenceSystem worldCrs = context.getCRS();
 		this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
 		this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient); // could
 		// use
 		// transform_d2w.inverse()
 		// also
 	}
 
 	private Envelope getTransformedBounds() throws TransformException {
 		ReferencedEnvelope bounds = getRenderBounds();
 		Envelope bounds_transformed = null;
 		if (bounds != null && transform_w2d != null) {
 			bounds_transformed = JTS.transform(bounds, transform_w2d);
 		}
 		return bounds_transformed;
 	}
 }
