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
 import gda.device.detector.areadetector.v17.ADBase.ImageMode;
 import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
 import gda.scan.ScanInformation;
 
 public class SingleExposureStandard extends SimpleAcquire {
 
 	
 	public SingleExposureStandard(ADBase adBase, double readoutTime) {
 		super(adBase, readoutTime);
 	}
 
 	@Override
 	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
 		if (numImages != 1) {
			throw new IllegalArgumentException("This single exposure triggering strategy expects to expose only 1 image");
 		}
 		super.prepareForCollection(collectionTime, 1, scanInfo);
 		configureTriggerMode();
 		getAdBase().setImageModeWait(ImageMode.SINGLE);
 		getAdBase().setNumImages(1);
 	}
 
 	protected void configureTriggerMode() throws Exception {
 		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
 	}
 
 }
