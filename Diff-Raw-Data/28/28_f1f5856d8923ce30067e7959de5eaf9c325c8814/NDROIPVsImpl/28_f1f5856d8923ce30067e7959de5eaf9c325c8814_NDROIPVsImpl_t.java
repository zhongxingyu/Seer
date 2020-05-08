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
 
 package gda.device.detector.areadetector.v17.impl;
 
 import gda.device.detector.areadetector.v17.NDPluginBasePVs;
 import gda.device.detector.areadetector.v17.NDROIPVs;
 import gda.device.detector.areadetector.v17.NDROIPVs.ROIDimensionPVs;
 import gda.epics.LazyPVFactory;
 import gda.epics.PV;
 import gda.epics.PVWithSeparateReadback;
 
 import org.springframework.beans.factory.InitializingBean;
 
 public class NDROIPVsImpl implements NDROIPVs, InitializingBean {
 
 	public static NDROIPVs createFromBasePVName(String basePVName) {
 		NDPluginBasePVsImpl pluginBasePVs =  NDPluginBasePVsImpl.createFromBasePVName(basePVName);
 		
 		NDROIPVsImpl ndroiPVsImpl = new NDROIPVsImpl();
 		ndroiPVsImpl.setBasePVName(basePVName);
 		ndroiPVsImpl.setPluginBasePVs(pluginBasePVs);
 		try {
 			ndroiPVsImpl.afterPropertiesSet();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return ndroiPVsImpl;
 	}
 	
 	private NDPluginBasePVs pluginBasePvs;
 	
 	private String basePVName;
 	
 	//
 	
 	private PV<String> namePV;
 
 	private PV<NDDataType> dataTypeOutPV;
 
 	private PV<Boolean> enableScalePVPair;
 
 	private PV<Integer> scalePVPair;
 
 	private ROIDimensionPVs xDimensionPVs;
 
 	private ROIDimensionPVs yDimensionPVs;
 
 	private ROIDimensionPVs zDimensionPVs;
 
 		
 	public void setPluginBasePVs(NDPluginBasePVs pluginBasePvs) {
 		this.pluginBasePvs = pluginBasePvs;
 	}
 	
 	@Override
 	public NDPluginBasePVs getPluginBasePVs() {
 		return pluginBasePvs;
 	}
 	
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (getBasePVName() == null) {
 			throw new IllegalArgumentException("'basePVName' needs to be declared");
 		}
 		createLazyPvs();
 	}
 
 	private void createLazyPvs() {
 		namePV = LazyPVFactory.newStringPV(basePVName +"Name");
 		
 		dataTypeOutPV = LazyPVFactory.newEnumPV(basePVName +"DataTypeOut", NDDataType.class);
 
 		enableScalePVPair = new PVWithSeparateReadback<Boolean>(
 				LazyPVFactory.newBooleanFromIntegerPV(basePVName +"EnableScale"),
 				LazyPVFactory.newReadOnlyBooleanFromEnumPV(basePVName +"EnableScale_RBV"));
 
 		scalePVPair = new PVWithSeparateReadback<Integer>(
 				LazyPVFactory.newIntegerPV(basePVName +"Scale"),
 				LazyPVFactory.newReadOnlyIntegerPV(basePVName +"Scale_RBV"));
 		
 		xDimensionPVs = new ROIDimensionPVsImpl(basePVName, "X");
 
 		yDimensionPVs = new ROIDimensionPVsImpl(basePVName, "Y");
 		
 		zDimensionPVs = new ROIDimensionPVsImpl(basePVName, "Z");
 
 	}
 	
 	@Override
 	public PV<String> getNamePV() {
 		return namePV;
 	}
 
 	@Override
 	public PV<NDDataType> getDataTypeOutPV() {
 		return dataTypeOutPV;
 	}
 
 	@Override
 	public PV<Boolean> getEnableScalePVPair() {
 		return enableScalePVPair;
 	}
 
 	@Override
 	public PV<Integer> getScalePVPair() {
 		return scalePVPair;
 	}
 
 	@Override
 	public ROIDimensionPVs getXDimension() {
 		return xDimensionPVs;
 	}
 
 	@Override
 	public ROIDimensionPVs getYDimension() {
 		return yDimensionPVs;
 	}
 
 	@Override
 	public ROIDimensionPVs getZDimension() {
 		return zDimensionPVs;
 	}
 
 }
 
 class ROIDimensionPVsImpl implements ROIDimensionPVs {
 	
 	public PV<Boolean> enablePVPair;
 
 	public PV<Integer> minPVPair;
 
 	public PV<Integer> sizePVPair;
 	
 	public ROIDimensionPVsImpl(String basePVName, String dim) {
 		
 		enablePVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromEnumPV(basePVName + "Enable" + dim),
 				LazyPVFactory.newReadOnlyBooleanFromEnumPV(basePVName + "Enable" + dim + "_RBV"));
 
 		minPVPair = new PVWithSeparateReadback<Integer>(
 				LazyPVFactory.newIntegerPV(basePVName + "Min" + dim),
 				LazyPVFactory.newReadOnlyIntegerPV(basePVName + "Min" + dim + "_RBV"));
 
 		sizePVPair = new PVWithSeparateReadback<Integer>(
 				LazyPVFactory.newIntegerPV(basePVName + "Size" + dim),
 				LazyPVFactory.newReadOnlyIntegerPV(basePVName + "Size" + dim + "_RBV"));
 	}
 
 	@Override
 	public PV<Boolean> getEnablePVPair() {
 		return enablePVPair;
 	}
 
 	@Override
 	public PV<Integer> getMinPVPair() {
 		return minPVPair;
 	}
 
 	@Override
 	public PV<Integer> getSizePVPair() {
 		return sizePVPair;
 	}
 
 }
