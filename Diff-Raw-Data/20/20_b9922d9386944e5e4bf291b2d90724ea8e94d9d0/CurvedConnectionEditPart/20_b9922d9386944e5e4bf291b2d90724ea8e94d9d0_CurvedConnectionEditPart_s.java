 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2011, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    mwenz Bug 352119 - initial API, implementation and documentation contributed by Benjamin Schmeling
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.parts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.draw2d.AbstractRouter;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.graphiti.mm.algorithms.styles.PrecisionPoint;
 import org.eclipse.graphiti.mm.pictograms.CurvedConnection;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 
 public class CurvedConnectionEditPart extends ConnectionEditPart {
 
 	public CurvedConnectionEditPart(IConfigurationProvider configurationProvider, CurvedConnection connection) {
 		super(configurationProvider, connection);
 	}
 
 	private CurvedConnection getCurvedConnection() {
 		return (CurvedConnection) super.getConnection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
 	 */
 	@Override
 	protected IFigure createFigure() {
 		IFigure ret = super.createFigure();
 		if (ret instanceof org.eclipse.draw2d.Connection) {
 			org.eclipse.draw2d.Connection draw2dConnection = (org.eclipse.draw2d.Connection) ret;
 			List<PrecisionPoint> controllPoints = new ArrayList<PrecisionPoint>();
 			controllPoints.addAll(getCurvedConnection().getControlPoints());
 			draw2dConnection.setConnectionRouter(new BezierRouter(controllPoints));
 		}
 		return ret;
 	}
 
 	@Override
 	protected void refreshVisuals() {
 		super.refreshVisuals();
 		refreshControlPoints();
 	}
 
 	private void refreshControlPoints() {
 		IFigure figure = getFigure();
 		if (figure instanceof org.eclipse.draw2d.Connection) {
 			org.eclipse.draw2d.Connection draw2dConnection = (org.eclipse.draw2d.Connection) figure;
			List<PrecisionPoint> controllPoints = new ArrayList<PrecisionPoint>();
			controllPoints.addAll(getCurvedConnection().getControlPoints());
			draw2dConnection.setConnectionRouter(new BezierRouter(controllPoints));
 		}
 	}
 
	private static class BezierRouter extends AbstractRouter {
 
 		private List<PrecisionPoint> bezierPoints;
 		private Map<org.eclipse.draw2d.Connection, Object> constraints = new HashMap<org.eclipse.draw2d.Connection, Object>(
 				11);
 
 		public BezierRouter(List<PrecisionPoint> bezierPoints) {
 			this.bezierPoints = bezierPoints;
 		}
 
 		public void route(org.eclipse.draw2d.Connection connection) {
 			List<org.eclipse.draw2d.geometry.PrecisionPoint> controllPoints = new ArrayList<org.eclipse.draw2d.geometry.PrecisionPoint>();
 			PointList points = connection.getPoints();
 			points.removeAllPoints();
 			Point ref1 = connection.getTargetAnchor().getReferencePoint();
 			Point ref2 = connection.getSourceAnchor().getReferencePoint();
 			org.eclipse.draw2d.geometry.PrecisionPoint start = new org.eclipse.draw2d.geometry.PrecisionPoint();
 			org.eclipse.draw2d.geometry.PrecisionPoint end = new org.eclipse.draw2d.geometry.PrecisionPoint();
 			start.setLocation(connection.getSourceAnchor().getLocation(ref1));
 			connection.translateToRelative(start);
 			end.setLocation(connection.getTargetAnchor().getLocation(ref2));
 			connection.translateToRelative(end);
 			controllPoints.add(start);
 			double gradient = (end.preciseY() - start.preciseY()) / (-end.preciseX() + start.preciseX());
 			double ortho_gradient = -Math.pow(gradient, -1);
 			double orthovector_x = 1;
 			double orthovector_y = ortho_gradient;
 			double factor_to_length = 1 / Math.sqrt((Math.pow(orthovector_y, 2) + Math.pow(orthovector_x, 2)));
 			for (PrecisionPoint precisionPoint : this.bezierPoints) {
 
 				double orthovector_x_cp = factor_to_length * orthovector_x * precisionPoint.getY();
 				double orthovector_y_cp = factor_to_length * orthovector_y * precisionPoint.getY();
 				if (Double.isNaN(orthovector_x_cp)) {
 					orthovector_x_cp = 0;
 				}
 				if (Double.isNaN(orthovector_y_cp)) {
 					orthovector_y_cp = 1 * precisionPoint.getY();
 				}
 				org.eclipse.draw2d.geometry.PrecisionPoint anchor = new org.eclipse.draw2d.geometry.PrecisionPoint(
 						(start.x + (end.x - start.x) * precisionPoint.getX() - orthovector_x_cp),
 						(start.y - (start.y - end.y) * precisionPoint.getX()) + orthovector_y_cp);
 
 				controllPoints.add(anchor);
 			}
 			controllPoints.add(end);
 			int precision = 10;
 			double factor = 1.0d / precision;
 			points.addPoint(start);
 			for (int i = 1; i < precision; i++) {
 				int j = 0;
 				double sum_x = 0;
 				double sum_y = 0;
 				for (Point point : controllPoints) {
 					sum_x += (bezier(j, controllPoints.size() - 1, i * factor) * point.preciseX());
 					sum_y += (bezier(j, controllPoints.size() - 1, i * factor) * point.preciseY());
 					j++;
 				}
 				org.eclipse.draw2d.geometry.PrecisionPoint bezierPoint = new org.eclipse.draw2d.geometry.PrecisionPoint(
 						sum_x, sum_y);
 				points.addPoint(bezierPoint);
 			}
 			points.addPoint(end);
 			connection.setPoints(points);
 		}
 
 		private double bezier(int i, int n, double t) {
 			return binomialCoefficients(n, i) * Math.pow(t, i) * Math.pow((1 - t), (n - i));
 		}
 
		static long binomialCoefficients(int n, int k) {
 			long coeff = 1;
 			for (int i = n - k + 1; i <= n; i++) {
 				coeff *= i;
 			}
 			for (int i = 1; i <= k; i++) {
 				coeff /= i;
 			}
 			return coeff;
 		}
 
 		@Override
 		public void setConstraint(org.eclipse.draw2d.Connection connection, Object constraint) {
 			constraints.put(connection, constraint);
 		}
 
 		@Override
 		public void remove(org.eclipse.draw2d.Connection connection) {
 			constraints.remove(connection);
 		}
 
 		@Override
 		public Object getConstraint(org.eclipse.draw2d.Connection connection) {
 			return constraints.get(connection);
 		}
 	}
 }
