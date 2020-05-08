 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package gda.device.detector.xmap;
 
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.device.detector.xmap.edxd.EDXDElement;
 import gda.device.detector.xmap.edxd.EDXDMappingController;
 import gda.factory.FactoryException;
 import gda.factory.Finder;
 import gda.observable.IObserver;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EpicsXmapController3ROI extends DeviceBase implements XmapController ,IObserver{
 	private EDXDMappingController edxdController;
 	private int numberOfMca;
 	private String edxdControllerName;
 	private double[][][] controlRois;
 	private int actualNumberOfROIs;
 	private static final Logger logger = LoggerFactory.getLogger(EpicsXmapController3ROI.class);
 	
 	public String getEdxdControllerName() {
 		return edxdControllerName;
 	}
 
 	public void setEdxdControllerName(String edxdControllerName) {
 		this.edxdControllerName = edxdControllerName;
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		if((edxdController = (EDXDMappingController)Finder.getInstance().find(edxdControllerName) )!= null){
 			edxdController.addIObserver(this);
 			configureNumberOfMca();
 		}
 	}
 	
 	public void configureNumberOfMca(){
 		numberOfMca = edxdController.getNumberOfElements();
 		controlRois = new double[numberOfMca][][];
 	}
    
 	@Override
 	public void clearAndStart() throws DeviceException {
 		//logger.info("Setting the mode for the xmap");
 		edxdController.setResume(false);
 		//logger.info("staarting the xmap");
 		edxdController.start();
 	}
 
 	@Override
 	public void deleteROIs(int mcaIndex) throws DeviceException {
 		controlRois[mcaIndex] = null;
 		
 	}
 
 	@Override
 	public double getAcquisitionTime() throws DeviceException {
 		return edxdController.getAcquisitionTime();
 	}
 
 	@Override
 	public int[] getData(int mcaNumber) throws DeviceException {
 		int numberOfBins =  getNumberOfBins();
 		int[] returnArray = new int[numberOfBins];
 		int[] replyArray = edxdController.getSubDetector(mcaNumber).readoutInts();
 		System.arraycopy(replyArray, 0, returnArray, 0, numberOfBins);
 		return returnArray;
 	}
 
 	@Override
 	public int[][] getData() throws DeviceException {
 		//should write data to a file
 		//bespoke scan scripts write data at the moment
 		int numberOfBins =  getNumberOfBins();
 		int[][] data = new int[numberOfMca][numberOfBins];
 		for (int i = 0; i < numberOfMca; i++) {
 			data[i] = getData(i);
 		}
 		return data;
 	}
 
 	@Override
 	public int getNumberOfBins() throws DeviceException {
 		return edxdController.getBins();
 	}
 
 	@Override
 	public void setNumberOfBins(int numberOfBins) throws DeviceException {
 		edxdController.setBins(numberOfBins);
 		
 	}
 
 	/**
 	 * Returns the roi count if they have been set, otherwise reads the total possible count
 	 * from EPICS
 	 */
 	@Override
 	public int getNumberOfROIs() {
 		if (actualNumberOfROIs>0) return actualNumberOfROIs;
 		try {
 			return edxdController.getMaxAllowedROIs();
 		} catch (DeviceException e) {
 			logger.error("Unable to read the max allowed ROIs from the detector", e);
 		}
 		return 0;
 	}
 
 	/**
 	 * Returns a count for each mca for a given roi number
 	 * For instance if roi=0 the first roi 
 	 */
 	@Override
 	public double[] getROICounts(int roiIndex) throws DeviceException {
 		double[] roiCounts = new double[numberOfMca];
 		for (int j = 0; j < numberOfMca; j++) {
 			double individualMCARois[] = this.getROIs(j);
 			roiCounts[j] = individualMCARois[roiIndex];
 		}
 		return roiCounts;
 	}
 	/**
 	 * @param mcaNumber
 	 * @return double array of regions of interest
 	 * @throws DeviceException
 	 */
	@Override
 	public double[] getROIs(int mcaNumber) throws DeviceException {
 		int[] mcaData = getData(mcaNumber);
 		double[] roiSums = new double[controlRois[mcaNumber].length];
 
 		for (int i = 0; i < controlRois[mcaNumber].length; i++) {
 			roiSums[i] = calcROICounts((int)controlRois[mcaNumber][i][0], (int)controlRois[mcaNumber][i][1], mcaData);
 		}
 		return roiSums;
 	}
 	
 	@Override
 	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
 		int[] mcaData = data[mcaNumber];
 		double[] roiSums = new double[controlRois[mcaNumber].length];
 
 		
 		
 		for (int i = 0; i < controlRois[mcaNumber].length; i++) {
 			int min = (int)controlRois[mcaNumber][i][0];
 			int max = (int)controlRois[mcaNumber][i][1];
 			roiSums[i] = calcROICounts(min, max, mcaData);
 		}
 		return roiSums;
 	}
 	
 	
 	private double calcROICounts(int min, int max, int[] data) {
 		double sum = 0;
 		for (int i = min; i <= max; i++) {
 			sum += data[i];
 		}
 		return sum;
 	}
 
 	/**
 	 * Returns a sum of all rois for each mca channel
 	 */
 	@Override
 	public double[] getROIsSum() throws DeviceException {
 		double[] roiSum = new double[actualNumberOfROIs];
 		for (int j = 0; j < numberOfMca; j++) {
 			double individualMCARois[] = this.getROIs(j);
 			for (int i = 0; i < actualNumberOfROIs; i++) {
 				roiSum[i] = roiSum[i] + individualMCARois[i];
 			}
 		}
 		return roiSum;
 	}
 
 	@Override
 	public double getReadRate() throws DeviceException {
 		// Not implemented in the new Interface
 		return 0;
 	}
 	
 	
 
 
 	@Override
 	public double getRealTime() throws DeviceException {
 		return getRealTime(0);
 	}
 	
 	/**
 	 * Get the real time for the mca element
 	 * @param mcaNumber
 	 * @return real time
 	 * @throws DeviceException
 	 */
 	public double getRealTime(int mcaNumber) throws DeviceException {
 		return edxdController.getSubDetector(mcaNumber).getRealTime();
 	}
 
 	@Override
 	public int getStatus() throws DeviceException {
 		return edxdController.getStatus();
 	}
 
 	@Override
 	public double getStatusRate() throws DeviceException {
 		// Not implemented in the new Interface
 		return 0;
 	}
 
 	@Override
 	public void setAcquisitionTime(double collectionTime) throws DeviceException {
 		edxdController.setAquisitionTime(collectionTime);
 	}
 
 	@Override
 	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
 		if (rois.length != numberOfMca) {
 			logger.error("ROIs length does not match the Number of MCA");
 			return;
 		}
 		for (int mcaIndex = 0; mcaIndex < numberOfMca; mcaIndex++) {
 			this.setNthROI(rois[mcaIndex], roiIndex, mcaIndex);
 		}
 		actualNumberOfROIs = roiIndex + 1;
 	}
 	
 	/**
 	 * @param roi
 	 * @param roiIndex
 	 * @param mcaIndex
 	 * @throws DeviceException
 	 */
 	private void setNthROI(double[] roi, int roiIndex, int mcaIndex) throws DeviceException {
 		if(roiIndex >= edxdController.getMaxAllowedROIs())
 		{
 			logger.error("Not a valid roi index");
 		return;
 		}
 		EDXDElement element = edxdController.getSubDetector(mcaIndex);
 		double roiLow[] = element.getLowROIs();
 		double roiHigh[] = element.getHighROIs();
 		if(roi[0] <= roi[1])
 			{
 				roiLow[roiIndex] = roi[0];
 				roiHigh[roiIndex] = roi[1];
 			}
 		else{
 			roiLow[roiIndex] = roi[1];
 			roiHigh[roiIndex] = roi[0];
 		}
 
 		element.setLowROIs(roiLow);
 		element.setHighROIs(roiHigh);
 		if(controlRois[mcaIndex] == null)
 			controlRois[mcaIndex] = new double[edxdController.getMaxAllowedROIs()][];
 		controlRois[mcaIndex][roiIndex] = new double []{roiLow[roiIndex], roiHigh[roiIndex]};
 		edxdController.activateROI();
 		
 		
 	}
 
 	@Override
 	public void setNumberOfElements(int numberOfMca) {
 		edxdController.setNumberOfElements(numberOfMca);
 		this.numberOfMca=numberOfMca;
 	}
 
 	@Override
 	public void setNumberOfROIs(int numberOfROIs) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Set rois the array can be of size [maximum number rois][2] if it is lower for instance
 	 * [actual number of rois][2] then the other possible rois will be set to zero.
 	 * 
 	 * The actual number of rois is also taken from the length of the first dimension of this array
 	 * so it should always be passed in with size of the actual number of rois.
 	 */
 	@Override
 	public void setROI(final double[][] actualRois, int mcaIndex) throws DeviceException {
 		
 		// The ROIS might not be scaled to the max ROI size, so we ensure that this has been done
 		final double[][] rois = new double[edxdController.getMaxAllowedROIs()][2];
 		for (int i = 0; i < actualRois.length; i++) {
 			rois[i][0] = actualRois[i][0];
 			rois[i][1] = actualRois[i][1];
 		}
 		controlRois[mcaIndex] = rois;
 		edxdController.getSubDetector(mcaIndex).setROIs(rois);
 		edxdController.activateROI();
 		actualNumberOfROIs = actualRois.length;
 	}
 
 	@Override
 	public void setROIs(double[][] rois) throws DeviceException {
 		for(int i =0; i< numberOfMca; i++)
 			setROI(rois, i);
 	}
 
 	@Override
 	public void setReadRate(double readRate) throws DeviceException {
 		// Not implemented in new xmap epics interface
 		
 	}
 
 	@Override
 	public void setReadRate(String readRate) throws DeviceException {
 		// Not implemented in new xmap epics interface
 	}
 
 	@Override
 	public void setStatusRate(double statusRate) throws DeviceException {
 		// Not implemented in new xmap epics interface
 	}
 
 	@Override
 	public void setStatusRate(String statusRate) throws DeviceException {
 		// Not implemented in new xmap epics interface
 		
 	}
 
 	@Override
 	public void start() throws DeviceException {
 		//edxdController.setResume(true);
 	    edxdController.start();
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		edxdController.stop();
 		
 	}
 
 	/**
 	 * Returns the total events recorded
 	 * @param mcaNumber
 	 * @throws DeviceException
 	 */
 	@Override
 	public int getEvents(final int mcaNumber) throws DeviceException {
 		return edxdController.getEvents(mcaNumber);
 	}
 
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		//TODO status update needs to be made
 	}
 
 
 	@Override
 	public double getICR(int mcaNumber) throws DeviceException {
 		return edxdController.getICR(mcaNumber);
 	}
 
 
 	@Override
 	public double getOCR(int mcaNumber) throws DeviceException {
 		return edxdController.getOCR(mcaNumber);
 	}
 
 	@Override
 	public int getNumberOfElements() throws DeviceException {
 		return edxdController.getNumberOfElements();
 	}
 
 }
