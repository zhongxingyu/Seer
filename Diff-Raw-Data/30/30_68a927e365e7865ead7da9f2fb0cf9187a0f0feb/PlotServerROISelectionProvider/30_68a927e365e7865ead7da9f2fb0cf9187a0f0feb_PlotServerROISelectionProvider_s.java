 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.nxdetector.roi;
 
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import org.python.core.PyString;
 
 import uk.ac.diamond.scisoft.analysis.SDAPlotter;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.roi.PerimeterBoxROIList;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROIList;
 
 public class PlotServerROISelectionProvider implements IndexedRectangularROIProvider<Integer>{
 	
 	private int maximumActiveRois;
 	private String viewName;
 	private static final String ROIViewNameSuffix = " Array";
 
 	public static String getGuiName(String detectorName){
 		return detectorName + ROIViewNameSuffix;
 	}
 
 	public PlotServerROISelectionProvider() {
 	}
 	
 	public PlotServerROISelectionProvider(String detectorName, int maximumActiveRois) {
 		viewName = getGuiName(detectorName);
 		this.maximumActiveRois = maximumActiveRois;
 	}
 
 	/**
 	 * Returns a list of active Rois. This will have no more than maximumActiveRois elements.
 	 * @return a list of active rois.
 	 * @throws Exception 
 	 */
 	public List<RectangularROI<Integer>> getActiveRoiList() throws Exception {
 		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList = getScisoftRoiListFromSDAPlotter();
 		ArrayList<RectangularROI<Integer>> roiList = new ArrayList<RectangularROI<Integer>>();
 		for (uk.ac.diamond.scisoft.analysis.roi.RectangularROI scisoftRoi : scisoftRoiList) {
 			roiList.add(ImutableRectangularIntegerROI.valueOf(scisoftRoi));
 		}
 		return roiList;
 	}
 
 	public List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> getScisoftRoiListFromSDAPlotter() throws Exception {
		GuiBean guiBean;
		try {
			guiBean = SDAPlotter.getGuiBean(viewName);
		} catch (Exception eOriginal) {
			String[] guiNames;
			try {
				guiNames = SDAPlotter.getGuiNames();
			} catch (Exception e1) {
				throw new Exception("Problem communicating with SDAPlotter", e1);
			}
 			throw new Exception(MessageFormat.format(
					"Problem getting gui bean for view named ''{0}''. Available: {1}",
					viewName, Arrays.toString(guiNames)), eOriginal);
		}
		if (guiBean == null) {
			throw new Exception(MessageFormat.format(
					"No gui bean for view named ''{0}''. Available: {1}",
 					viewName, Arrays.toString(SDAPlotter.getGuiNames())));
 		}
 		Serializable serializable = guiBean.get(GuiParameters.ROIDATALIST);
 		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList;
 		if (serializable instanceof PerimeterBoxROIList) {
 			scisoftRoiList = new Vector<uk.ac.diamond.scisoft.analysis.roi.RectangularROI>((PerimeterBoxROIList) guiBean.get(GuiParameters.ROIDATALIST));
 		} else {
 			scisoftRoiList = (RectangularROIList) guiBean.get(GuiParameters.ROIDATALIST);
 		}
 		return scisoftRoiList;
 	}
 
 	@Override
 	public RectangularROI<Integer> getRoi(int index) throws IndexOutOfBoundsException, Exception {
 		if (index >= maximumActiveRois) {
 			throw new IndexOutOfBoundsException("Maximum index is: " + maximumActiveRois);
 		}
 		List<uk.ac.diamond.scisoft.analysis.roi.RectangularROI> scisoftRoiList = getScisoftRoiListFromSDAPlotter();
 		if (index >= scisoftRoiList.size()) {
 			return null;
 		}
 		return ImutableRectangularIntegerROI.valueOf(scisoftRoiList.get(index));
 	}
 	
 	public PyString __str__() {
 		String str = "Regions from: '" + viewName +"'";
 		str += "\n";
 		List<RectangularROI<Integer>> roiList;
 		try {
 			roiList = getActiveRoiList();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		for (int i = 0; i < maximumActiveRois; i++) {
 			String roiRepr;
 			if ((i >= roiList.size()) || (roiList.get(i) == null)) {
 				roiRepr = "---";
 			} else {
 				roiRepr = roiList.get(i).toString();
 			}
 			str += (i + 1) + ". " + roiRepr + "\n";
 		}
 		return new PyString(str);
 	}
 
 	public int getMaximumActiveRois() {
 		return maximumActiveRois;
 	}
 
 	public void setMaximumActiveRois(int maximumActiveRois) {
 		this.maximumActiveRois = maximumActiveRois;
 	}
 
 	public String getViewName() {
 		return viewName;
 	}
 
 	public void setViewName(String viewName) {
 		this.viewName = viewName;
 	}
 }
