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
 
 package gda.device.detector.areadetector.v18.impl;
 
 import gda.device.detector.areadetector.v17.NDPluginBasePVs;
 import gda.device.detector.areadetector.v17.impl.ADDriverPilatusImpl;
 import gda.device.detector.areadetector.v17.impl.NDPluginBasePVsImpl;
 import gda.device.detector.areadetector.v18.NDStatsPVs;
 import gda.epics.LazyPVFactory;
 import gda.epics.PV;
 import gda.epics.PVWithSeparateReadback;
 import gda.epics.ReadOnlyPV;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 public class NDStatsPVsImpl implements InitializingBean, NDStatsPVs {
 
 	static final Logger logger = LoggerFactory.getLogger(ADDriverPilatusImpl.class);
 
 	public static NDStatsPVsImpl createFromBasePVName(String basePVName) {
 		NDPluginBasePVsImpl pluginBasePVs =  NDPluginBasePVsImpl.createFromBasePVName(basePVName);
 		
 		NDStatsPVsImpl statsPVsImpl = new NDStatsPVsImpl();
 		statsPVsImpl.setBasePVName(basePVName);
 		statsPVsImpl.setPluginBasePVs(pluginBasePVs);
 		try {
 			statsPVsImpl.afterPropertiesSet();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		
 		return statsPVsImpl;
 	}
 	
 	/**
 	 * Map to PV names
 	 */
 	private enum PVNames {
 		ComputeCentroid, ComputeCentroid_RBV, ComputeStatistics, ComputeStatistics_RBV, TSControl, TSNumPoints, TSCurrentPoint // also TSRead.SCAN
 	}
 
 	private String basePVName;
 
 	//
 
 	private PV<Boolean> computeStatisticsPVPair;
 
 	private PV<Boolean> computeCentroidPVPair;
 
 	private NDPluginBasePVs pluginBasePVs;
 
 	private PV<TSControlCommands> tsControlPV;
 
 	private PV<Integer> tsNumPointsPV;
 
 	private ReadOnlyPV<Integer> tsCurrentPointPV;
 
 	private Map<Stat, ReadOnlyPV<Double[]>> tsArrayPVMap;
 
 	private PV<Integer> tsReadScanPV;
 
 	public void setBasePVName(String basePVName) {
 		this.basePVName = basePVName;
 	}
 
 	public String getBasePVName() {
 		return basePVName;
 	}
 
 	public void setPluginBasePVs(NDPluginBasePVs pluginBasePVs) {
 		this.pluginBasePVs = pluginBasePVs;
 	}
 
 	@Override
 	public NDPluginBasePVs getPluginBasePVs() {
 		return pluginBasePVs;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (basePVName == null) {
 			throw new IllegalArgumentException("'basePVName' needs to be declared");
 		}
 		createLazyPvs();
 	}
 
 	private void createLazyPvs() {
 
 		computeStatisticsPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.ComputeStatistics.name())),
 				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.ComputeStatistics_RBV.name())));
 
 		computeCentroidPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.ComputeCentroid.name())),
 				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.ComputeCentroid_RBV.name())));
 
 		tsControlPV = LazyPVFactory.newEnumPV(fullname(PVNames.TSControl.name()), TSControlCommands.class);
 
 		tsNumPointsPV = LazyPVFactory.newIntegerPV(fullname(PVNames.TSNumPoints.name()));
 
 		tsCurrentPointPV = LazyPVFactory.newReadOnlyIntegerPV(fullname(PVNames.TSCurrentPoint.name()));
 		
 		tsReadScanPV = LazyPVFactory.newIntegerPV(fullname("TSRead.SCAN"));
 
 		tsArrayPVMap = new HashMap<NDStatsPVs.Stat, ReadOnlyPV<Double[]>>();
 
 		for (Stat stat : Arrays.asList(BasicStat.values())) {
 			tsArrayPVMap.put(stat, LazyPVFactory.newReadOnlyDoubleArrayPV(fullname("TS" + ((Enum<?>) stat).name())));
 		}
 
 		for (Stat stat : Arrays.asList(CentroidStat.values())) {
 			tsArrayPVMap.put(stat, LazyPVFactory.newReadOnlyDoubleArrayPV(fullname("TS" + ((Enum<?>) stat).name())));
 		}
 
 	}
 
 	private String fullname(String pvName) {
 		return basePVName + pvName;
 	}
 
 	@Override
 	public PV<Boolean> getComputeStatistsicsPVPair() {
 		return computeStatisticsPVPair;
 	}
 
 	@Override
 	public PV<Boolean> getComputeCentroidPVPair() {
 		return computeCentroidPVPair;
 	}
 
 	@Override
 	public PV<TSControlCommands> getTSControlPV() {
 		return tsControlPV;
 	}
 
 	@Override
 	public PV<Integer> getTSNumPointsPV() {
 		return tsNumPointsPV;
 	}
 
 	@Override
 	public ReadOnlyPV<Integer> getTSCurrentPointPV() {
 		return tsCurrentPointPV;
 	}
 
 	@Override
 	public ReadOnlyPV<Double[]> getTSArrayPV(Stat stat) {
 		return tsArrayPVMap.get(stat);
 	}
 
 	@Override
 	public PV<Integer> getTSReadScanPV() {
 		return tsReadScanPV;
 	}
 
 }
