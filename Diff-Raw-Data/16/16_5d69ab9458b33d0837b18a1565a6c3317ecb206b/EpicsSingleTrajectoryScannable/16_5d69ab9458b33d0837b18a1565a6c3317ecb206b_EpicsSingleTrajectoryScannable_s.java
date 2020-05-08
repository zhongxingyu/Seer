 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
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
 
 package gda.device.scannable;
 
 import gda.device.ContinuousParameters;
 import gda.device.DeviceException;
 import gda.factory.FactoryException;
import gda.factory.Finder;
 import gda.observable.IObserver;
 import gda.scan.Trajectory;
 import gda.scan.TrajectoryScanController;
 import gda.scan.TrajectoryScanController.BuildStatus;
 import gda.scan.TrajectoryScanController.ExecuteStatus;
 import gda.scan.TrajectoryScanController.ReadStatus;
 import gda.scan.TrajectoryScanController.TrajectoryScanProperty;
 import gda.util.OutOfRangeException;
 import gov.aps.jca.TimeoutException;
 
 //TODO Explain the meaning of this class
 public class EpicsSingleTrajectoryScannable extends ScannableMotionUnitsBase implements ContinuouslyScannable, IObserver {
 
 //	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScannable2.class);
 	@SuppressWarnings("unused")
 	private static final int ONEDMODE = 1;
 	private static final int TWODMODE = 2;
 	protected TrajectoryScanController tracController;
 	private double readTimeout = 60.0;
 	public double getReadTimeout() {
 		return readTimeout;
 	}
 	public void setReadTimeout(double readTimeout) {
 		this.readTimeout = readTimeout;
 	}
 	private ContinuousParameters continuousParameters;
 	@SuppressWarnings("unused")
 	private int actualPulses;
 	@SuppressWarnings("unused")
 	private double[] xpositions;
 	protected boolean trajMoveComplete;
 	private double[] scannablePositions;
 	private int mode;
 	private boolean trajectoryBuildDone;
 	protected int trajectoryIndex =0;
 	
 	public TrajectoryScanController getTracController() {
 		return tracController;
 	}
 	public void setTracController(TrajectoryScanController tracController) {
 		this.tracController = tracController;
 	}
 	
 	public int getMode() {
 		return mode;
 	}
 	public void setMode(int mode) {
 		this.mode = mode;
 	}
 	@Override
 	public void configure() throws FactoryException {	
 		//TODO Too implicit!! epicsTrajectoryScanController as a default. How are you supposed to know
 		//Also on I18 we want two different trajectory stages with differing stade indices. hard coded epicsTrajectoryScanController makes this impossible.
 		//if(tracController == null)
 		//	tracController = (TrajectoryScanController)Finder.getInstance().find("epicsTrajectoryScanController");
 		tracController.addIObserver(this);
 	}
 	@Override
 	public void continuousMoveComplete() throws DeviceException {
 		try {
 			if (tracController.getExecuteStatus() != ExecuteStatus.ABORT) {
 				tracController.read();
 				double waitedSoFar = 0.0;
 				while (tracController.isReading() && waitedSoFar < (readTimeout * 1000)) {
 					Thread.sleep(1000);
 					waitedSoFar += 1000.0;
 				}
 				// check the read status from the controller
 				if (tracController.getReadStatus() != ReadStatus.SUCCESS) {
 					trajectoryBuildDone = false;
 					throw new DeviceException("Unable to get the read status from the Trajectory Controller");
 				}
 				actualPulses = tracController.getActualPulses();
 				// get the actual xpositions
 				xpositions = tracController.getMActual(trajectoryIndex);
 			}
 		} catch (TimeoutException e) {
 			trajectoryBuildDone = false;
 			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
 		} catch (InterruptedException e) {
 			trajectoryBuildDone = false;
 			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
 		}
 	}
 
 	@Override
 	public ContinuousParameters getContinuousParameters() {
 		return continuousParameters;
 	}
 
 	@Override
 	public void performContinuousMove() throws DeviceException {
 		trajMoveComplete = false;
 		try {
 			tracController.execute();
 		} catch (Exception e) {
 			throw new DeviceException(getName() + " exception in performContinuousMove",e);
 		}
 	}
 
 	@Override
 	public boolean isBusy() throws DeviceException {
 		try {
 			return tracController.isBusy();
 		} catch (Exception e) {
 			throw new DeviceException(getName() + " exception in isBusy",e);
 		}
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		try {
 			tracController.stop();
 		} catch (Exception e) {
 			throw new DeviceException(getName() + " exception in stop",e);
 		}
 		super.stop();
 	}
 
 	
 	@Override
 	public int prepareForContinuousMove() throws DeviceException {
 		// build the trajectory
 		if(mode == TWODMODE && trajectoryBuildDone)
 			return continuousParameters.getNumberDataPoints();
 		
 		try {
 			for(int i =1 ; i <= TrajectoryScanController.MAX_TRAJECTORY; i++)
 			{	
 				tracController.setMMove(i, false);				
 			}
 			tracController.setMMove(trajectoryIndex, true);
 			
 			Trajectory trajectory = new Trajectory();
 			trajectory.setTotalElementNumber(continuousParameters.getNumberDataPoints() +1 );
 			trajectory.setTotalPulseNumber(continuousParameters.getNumberDataPoints()+ 1 );
 			trajectory.setController(tracController);
 			double[] path = trajectory.defineCVPath(continuousParameters.getStartPosition(), 
 					continuousParameters.getEndPosition(), continuousParameters.getTotalTime());
 			
 			tracController.setMTraj(trajectoryIndex, path);
 			
 			tracController.setNumberOfElements((int)trajectory.getElementNumbers());
 			tracController.setNumberOfPulses((int) trajectory.getPulseNumbers());
 			tracController.setStartPulseElement((int) trajectory.getStartPulseElement());
 			tracController.setStopPulseElement((int) trajectory.getStopPulseElement());
 
 			if (tracController.getStopPulseElement() != (int) (trajectory.getStopPulseElement())){
 				tracController.setStopPulseElement((int) (trajectory.getStopPulseElement()));	
 			}
 			tracController.setTime((trajectory.getTotalTime()));
    
 			
 			tracController.build();
 			while(tracController.isBuilding()){
 				//wait for the build to finsih
 				Thread.sleep(30);
 			}
 			//check the build status from epics          
 			if(tracController.getBuildStatus() != BuildStatus.SUCCESS){
 				throw new DeviceException("Unable to build the trajectory with the given start and stop positions and the time");
 			}
 			trajectoryBuildDone = true;
 			return continuousParameters.getNumberDataPoints();
 		} catch (TimeoutException e) {
 			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
 		} catch (InterruptedException e) {
 			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
 		} catch (OutOfRangeException e) {
 			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
 		}
 	}
 
 	@Override
 	public void setContinuousParameters(ContinuousParameters parameters) {
 		this.continuousParameters = parameters;
 
 	}
 	@Override
 	//TO DO discuss with FY
 	public void update(Object source, Object arg) {
 		if(source instanceof TrajectoryScanProperty)
 		{
 			if(((TrajectoryScanProperty)source) == TrajectoryScanProperty.EXECUTE )
 			{
 				if(arg instanceof ExecuteStatus)
 				{
 					if(((ExecuteStatus)arg) == ExecuteStatus.SUCCESS)
 						trajMoveComplete = true;
 					else
 						trajMoveComplete = false;
 							
 				}
 			}
 		}
 		
 	}
 	@Override
 	public double calculateEnergy(int frameIndex) {
 		double stepSize = (continuousParameters.getEndPosition() -continuousParameters.getStartPosition())/continuousParameters.getNumberDataPoints();
 		return (continuousParameters.getStartPosition() + (frameIndex * stepSize));
 
 	}
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		return tracController.getName();
 
 	}
 	@Override
 	public void atScanLineEnd() throws DeviceException {
 		try {
 			scannablePositions = tracController.getMActual(trajectoryIndex);
 		} catch (TimeoutException e) {
 			throw new DeviceException(getName() + " exception in atScanLineEnd",e);
 		} catch (InterruptedException e) {
 			throw new DeviceException(getName() + " exception in atScanLineEnd",e);
 		}
 
 	}
 	
 	@Override
 	public void atScanEnd() throws DeviceException {
 		trajectoryBuildDone = false;
 
 	}
 	@Override
 	public void atCommandFailure() 
 	{
 		trajectoryBuildDone = false;
 	}
 	public double[] getScannablePositions() {
 		return scannablePositions;
 	}
 	public void setTrajectoryIndex(int trajectoryIndex) {
 		this.trajectoryIndex = trajectoryIndex;
 	}
 	public int getTrajectoryIndex() {
 		return trajectoryIndex;
 	}
 }
