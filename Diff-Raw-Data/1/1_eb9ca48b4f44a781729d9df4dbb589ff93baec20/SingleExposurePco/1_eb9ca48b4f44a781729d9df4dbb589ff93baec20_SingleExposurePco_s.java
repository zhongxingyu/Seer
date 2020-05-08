 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.device.detector.addetector.triggering;
 
 import gda.device.detector.areadetector.v17.ADBase;
 import gda.device.detector.areadetector.v17.ADDriverPco;
 import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
 import gda.scan.ScanInformation;
 import gda.util.LiveModeUtil;
 
 public class SingleExposurePco extends SingleExposureStandard {
 
 	private final ADDriverPco adDriverPco;
 
 	public SingleExposurePco(ADBase adBase, ADDriverPco adDriverPco, double readoutTime) {
 		super(adBase, readoutTime);
 		this.adDriverPco = adDriverPco;
 	}
 
 	@Override
 	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
 		super.prepareForCollection(collectionTime, numImages, scanInfo);
 		getAdBase().setAcquirePeriod(0.); // for pco always set acq period to 0 to force delay to 0.
 		if (LiveModeUtil.isLiveMode()) {
 			adDriverPco.getArmModePV().putWait(true);
			Thread.sleep(2000); //if we do not wait here the first attempt to acquire an image stalls with the IOC in readout mode
 		}
 	}
 
 	@Override
 	public void completeCollection() throws Exception {
 		super.completeCollection();
 		if (LiveModeUtil.isLiveMode()) {
 			adDriverPco.getArmModePV().putWait(false);
 		}
 	}
 
 	@Override
 	protected void configureTriggerMode() throws Exception {
 		// Reported Epics bug: changing mode while acquiring causes an IOC crash (28oct2011 RobW)
 		getAdBase().stopAcquiring();
 		getAdBase().setTriggerMode(PcoTriggerMode.SOFTWARE.ordinal());
 	}
 
 }
