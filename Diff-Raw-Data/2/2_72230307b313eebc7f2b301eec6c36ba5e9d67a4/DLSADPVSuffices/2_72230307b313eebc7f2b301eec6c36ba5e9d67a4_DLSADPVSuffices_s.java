 /*-
  * Copyright © 2014 Diamond Light Source Ltd.
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
 
 package uk.ac.gda.epics.adviewer;
 
 public class DLSADPVSuffices implements ADPVSuffices {
 	private static final String MPG_PROC_PLUGIN_SUFFIX = "PROC:";
	private static final String ROI_PLUGIN_SUFFIX = "ROI";
 	private static final String MPG_PLUGIN_SUFFIX = "MJPG:";
 	private static final String ARRAY_PLUGIN_SUFFIX = "ARR:";
 	private static final String STAT_PLUGIN_SUFFIX = "STAT:";//"STAT1:";
 	private static final String ADBASE_SUFFIX = "CAM:";
 	
 	@Override
 	public String getADBaseSuffix() {
 		return ADBASE_SUFFIX;
 	}
 
 	@Override
 	public String getArrayROISuffix() {
 		return ROI_PLUGIN_SUFFIX;
 	}
 
 	@Override
 	public String getArraySuffix() {
 		return ARRAY_PLUGIN_SUFFIX;
 	}
 
 	@Override
 	public String getStatSuffix() {
 		return STAT_PLUGIN_SUFFIX;
 	}
 
 	@Override
 	public String getMPGProcSuffix() {
 		return MPG_PROC_PLUGIN_SUFFIX;
 	}
 
 	@Override
 	public String getMPGSuffix() {
 		return MPG_PLUGIN_SUFFIX;
 	}
 
 }
