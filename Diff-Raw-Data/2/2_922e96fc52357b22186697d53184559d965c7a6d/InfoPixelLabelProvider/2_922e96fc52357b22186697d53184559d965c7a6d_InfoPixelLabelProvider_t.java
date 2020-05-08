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
 
 package org.dawb.workbench.plotting.tools;
 
 import java.util.Collection;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.rcp.pixelinfoutils.Vector3dutil;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 
 public class InfoPixelLabelProvider extends ColumnLabelProvider {
 
 
 	private final int column;
 	private final InfoPixelTool tool;
 	private final IPlottingSystem plotSystem;
 
 	private static final Logger logger = LoggerFactory.getLogger(InfoPixelLabelProvider.class);
 
 	public InfoPixelLabelProvider(InfoPixelTool tool, int i) {
 
 		this.column = i;
 		this.tool   = tool;
 		this.plotSystem = tool.getPlottingSystem();
 	}
 
 
 	@Override
 	public String getText(Object element) {
 
 		double x = 0.0;
 		double y = 0.0;
 		
 		try {
 			if (element instanceof IRegion){
 				
 				final IRegion region = (IRegion)element;
 				if (region.getRegionType()==RegionType.POINT) {
 					PointROI pr = (PointROI)tool.getBounds(region);
 					x = pr.getPointX();
 					y = pr.getPointY();
 				} else {
 					x = tool.xValues[0];
 					y = tool.yValues[0];
 				}
 	
 			}else {
 				return null;
 			}
 	
 			IDiffractionMetadata dmeta = null;
 			AbstractDataset set = null;
 			final Collection<ITrace> traces = plotSystem.getTraces(IImageTrace.class);
 			final IImageTrace trace = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 			if (trace!=null) {
 				set = trace.getData();
 				final IMetaData      meta = set.getMetadata();
 				if (meta instanceof IDiffractionMetadata) {
 	
 					dmeta = (IDiffractionMetadata)meta;
 				}
 			}
 	
 			QSpace qSpace  = null;
 			Vector3dutil vectorUtil= null;
 			if (dmeta != null) {
 	
 				try {
 					DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
 					DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
 					
 					if (!(detector2dProperties == null)){
 						qSpace = new QSpace(detector2dProperties,
 								diffractionCrystalEnvironment);
 										
 						vectorUtil = new Vector3dutil(qSpace, x, y);
 					}
 				} catch (Exception e) {
 					logger.error("Could not create a detector properties object from metadata", e);
 				}
 			}
 	
 			switch(column) {
 			case 0: // "Point Id"
 				return ( ( (IRegion)element).getRegionType() == RegionType.POINT) ? ((IRegion)element).getName(): "";
 			case 1: // "X position"
 				return String.format("% 4.4f", x);
 			case 2: // "Y position"
 				return String.format("% 4.4f", y);
 			case 3: // "Data value"
 				if (set == null || vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
 				return String.format("% 4.4f", set.getDouble((int)y, (int) x));
 			case 4: // q X
 				if (vectorUtil==null || vectorUtil.getQMask(qSpace, x, y) == null) return "-";
 				return String.format("% 4.4f", vectorUtil.getQx());
 			case 5: // q Y
 				if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
 				return String.format("% 4.4f", vectorUtil.getQy());
 			case 6: // q Z
 				if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
 				return String.format("% 4.4f", vectorUtil.getQz());
 			case 7: // 20
 				if (qSpace == null) return "-";
				return String.format("% 3.3f", Math.toDegrees(vectorUtil.getQScatteringAngle(qSpace)));
 			case 8: // resolution
 				if (vectorUtil==null ||vectorUtil.getQMask(qSpace, x, y) == null) return "-";
 				return String.format("% 4.4f", (2*Math.PI)/vectorUtil.getQlength());
 			case 9: // Dataset name
 				if (set == null) return "-";
 				return set.getName();
 	
 			default:
 				return "Not found";
 			}
 		} catch (Throwable ne) { 
 			// Must not throw anything from this method - user sees millions of messages!
 			logger.error("Cannot get label!", ne);
 			return "";
 		}
 		
 	}
 
 	@Override
 	public String getToolTipText(Object element) {
 		return "Any selection region can be used in information box tool.";
 	}
 
 }
