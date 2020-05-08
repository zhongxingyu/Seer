 /*
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
 
 package uk.ac.diamond.scisoft.analysis.roi.handler;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Wrapper class for a RectangularROI that adds handles
  */
 public class RectangularROIHandler extends ROIHandler {
 	/**
 	 * Number of handle areas
 	 */
 	private final static int NHANDLE = 9;
 	
 	/**
 	 * Handler for RectangularROI
 	 * @param roi
 	 */
 	public RectangularROIHandler(RectangularROI roi) {
 		super();
 		for (int h = 0; h < NHANDLE; h++) {
 			add(-1);
 		}
 		this.roi = roi;
 	}
 
 	/**
 	 * @return Returns the roi.
 	 */
 	@Override
 	public RectangularROI getROI() {
 		return (RectangularROI) roi;
 	}
 
 	@Override
 	public double[] getHandlePoint(int handle, int size) {
 		final RectangularROI oroi = (RectangularROI) roi;
 		double[] pt = null;
 
 		switch (handle) {
 		case 0:
 			pt = oroi.getPoint();
 			break;
 		case 1:
 			pt = oroi.getPoint(0.5, 0);
 			pt[0] -= size/2;
 			break;
 		case 2:
 			pt = oroi.getPoint(1.0, 0);
 			pt[0] -= size;
 			break;
 		case 3:
 			pt = oroi.getPoint(0.0, 0.5);
 			pt[1] -= size/2;
 			break;
 		case 4:
 			pt = oroi.getPoint(0.5, 0.5);
 			pt[0] -= size/2;
 			pt[1] -= size/2;
 			break;
 		case 5:
 			pt = oroi.getPoint(1.0, 0.5);
 			pt[0] -= size;
 			pt[1] -= size/2;
 			break;
 		case 6:
 			pt = oroi.getPoint(0.0, 1.0);
 			pt[1] -= size;
 			break;
 		case 7:
 			pt = oroi.getPoint(0.5, 1.0);
 			pt[0] -= size/2;
 			pt[1] -= size;
 			break;
 		case 8:
 			pt = oroi.getPoint(1.0, 1.0);
 			pt[0] -= size;
 			pt[1] -= size;
 			break;
 		}
 		return pt;
 	}
 
 	@Override
 	public double[] getAnchorPoint(int handle, int size) {
 		final RectangularROI oroi = (RectangularROI) roi;
 		double[] pt = null;
 
 		switch (handle) {
 		case 0:
 			pt = oroi.getPoint();
 			break;
 		case 1:
 			pt = oroi.getPoint(0.5, 0);
 			break;
 		case 2:
 			pt = oroi.getPoint(1.0, 0);
 			break;
 		case 3:
 			pt = oroi.getPoint(0.0, 0.5);
 			break;
 		case 4:
 			pt = oroi.getPoint(0.5, 0.5);
 			break;
 		case 5:
 			pt = oroi.getPoint(1.0, 0.5);
 			break;
 		case 6:
 			pt = oroi.getPoint(0.0, 1.0);
 			break;
 		case 7:
 			pt = oroi.getPoint(0.5, 1.0);
 			break;
 		case 8:
 			pt = oroi.getPoint(1.0, 1.0);
 			break;
 		}
 		return pt;
 	}
 
 	/**
 	 * @param spt starting point 
 	 * @param pt 
 	 * @return resized ROI
 	 */
 	public RectangularROI resize(double[] spt, double[] pt) {
 		RectangularROI rroi = null;
 		double[] ept;
 
 		if (handle == 4)
 			return rroi;
 
 		rroi = (RectangularROI) roi.copy();
 		ept = rroi.getEndPoint();
 
 		switch (handle) {
 		case -1: // new definition
 			rroi.setPoint(spt);
 			rroi.setEndPoint(pt);
 			break;
 		case 0:
 			pt[0] -= spt[0];
 			pt[1] -= spt[1];
 			rroi.setPointKeepEndPoint(pt, true, true);
 			break;
 		case 1:
 			pt[0] -= spt[0];
 			pt[1] -= spt[1];
 			rroi.setPointKeepEndPoint(pt, false, true);
 			break;
 		case 2:
 			rroi.adjustKeepDiagonalPoint(spt, ept, pt, true);
 			break;
 		case 3:
 			pt[0] -= spt[0];
 			pt[1] -= spt[1];
 			rroi.setPointKeepEndPoint(pt, true, false);
 			break;
 		case 5:
 			pt[0] += ept[0] - spt[0];
 			pt[1] += ept[1] - spt[1];
 			rroi.setEndPoint(pt, true, false);
 			break;
 		case 6:
 			rroi.adjustKeepDiagonalPoint(spt, ept, pt, false);
 			break;
 		case 7:
 			pt[0] += ept[0] - spt[0];
 			pt[1] += ept[1] - spt[1];
 			rroi.setEndPoint(pt, false, true);
 			break;
 		case 8:
 			pt[0] += ept[0] - spt[0];
 			pt[1] += ept[1] - spt[1];
 			rroi.setEndPoint(pt, true, true);
 			break;
 		default:
 			break;
 		}
 
 		return rroi;
 	}
 
 	/**
 	 * @param pt
 	 * @return reoriented ROI
 	 */
 	public RectangularROI reorient(double[] pt) {
 		RectangularROI rroi = null;
 
		if (handle == 4 || (handle%2) == 1)
 			return rroi;
 
 		final RectangularROI oroi = (RectangularROI) roi;
 
 		rroi = oroi.copy();
 		double nang, oang;
 
 		switch (handle) {
 		case 0: // keep end point
 			oang = oroi.getAngleRelativeToPoint(1.0, 1.0, oroi.getPoint());
 			nang = oroi.getAngleRelativeToPoint(1.0, 1.0, pt);
 			rroi.addAngle(nang-oang);
 			rroi.setEndPointKeepLengths(oroi.getEndPoint());
 			break;
 		case 2:
 			oang = oroi.getAngleRelativeToPoint(0.0, 1.0, oroi.getPoint(1.0, 0.0));
 			nang = oroi.getAngleRelativeToPoint(0.0, 1.0, pt);
 			rroi.translate(0.0, 1.0);
 			rroi.addAngle(nang-oang);
 			rroi.translate(0.0, -1.0);
 			break;
 		case 6:
 			oang = oroi.getAngleRelativeToPoint(1.0, 0.0, oroi.getPoint(0.0, 1.0));
 			nang = oroi.getAngleRelativeToPoint(1.0, 0.0, pt);
 			rroi.translate(1.0, 0.0);
 			rroi.addAngle(nang-oang);
 			rroi.translate(-1.0, 0.0);
 			break;
 		case 8: // keep start point
 			oang = oroi.getAngleRelativeToPoint(0, 0, oroi.getPoint(1.0, 1.0));
 			nang = oroi.getAngleRelativeToPoint(0, 0, pt);
 			rroi.addAngle(nang-oang);
 			break;
 		}
 		return rroi;
 	}
 
 	@Override
 	public IROI interpretMouseDragging(double[] spt, double[] ept) {
 		final RectangularROI rroi = (RectangularROI) roi;
 		RectangularROI croi = null; // return null if not a valid event
 
 		switch (status) {
 		case RMOVE:
 			croi = rroi.copy();
 			ept[0] -= spt[0];
 			ept[1] -= spt[1];
 			croi.addPoint(ept);
 			break;
 		case NONE:
 			croi = rroi.copy();
 			croi.setEndPoint(ept);
 			break;
 		case REORIENT:
 			croi = reorient(ept);
 			break;
 		case RESIZE:
 			croi = resize(spt, ept);
 			break;
 		case ROTATE:
 			croi = rroi.copy();
 			double ang = croi.getAngleRelativeToMidPoint(ept);
 			double[] mpt = croi.getMidPoint();
 			croi.setAngle(ang);
 			croi.setMidPoint(mpt);
 			break;
 		case CMOVE:
 			break;
 		case CRMOVE:
 			break;
 		}
 
 		return croi;
 	}
 }
