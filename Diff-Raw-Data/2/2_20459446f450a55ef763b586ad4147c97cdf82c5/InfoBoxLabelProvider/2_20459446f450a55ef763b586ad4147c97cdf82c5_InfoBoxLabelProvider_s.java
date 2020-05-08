 /*
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
 
 package org.dawb.workbench.plotting.tools;
 
 import java.util.Collection;
 
 import javax.vecmath.Vector3d;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
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
 
 public class InfoBoxLabelProvider extends ColumnLabelProvider {
 
 
 	private final int column;
 	private final InfoBox tool;
 	private final IPlottingSystem plotSystem;
 
 	private static final Logger logger = LoggerFactory.getLogger(InfoBoxLabelProvider.class);
 
 	public InfoBoxLabelProvider(InfoBox tool, int i) {
 
 		this.column = i;
 		this.tool   = tool;
 		this.plotSystem = tool.getPlottingSystem();
 	}
 
 
 	@Override
 	public String getText(Object element) {
 
 		double x = 0.0;
 		double y = 0.0;
 		
 		if (element instanceof IRegion){
 			
 			x = tool.xValues[0];
 			y = tool.yValues[0];
 
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
 		Vector3d q = null;
 		if (dmeta != null) {
 
 			try {
 				DetectorProperties detector2dProperties = dmeta.getDetector2DProperties();
 				DiffractionCrystalEnvironment diffractionCrystalEnvironment = dmeta.getDiffractionCrystalEnvironment();
 				
 				if (!(detector2dProperties == null)){
 					qSpace = new QSpace(detector2dProperties,
 							diffractionCrystalEnvironment);
 				
 					q = qSpace.qFromPixelPosition(x, y);
 				}
 			} catch (Exception e) {
 				logger.error("Could not create a detector properties object from metadata", e);
 			}
 		}
 
 		switch(column) {
 		case 0: // "X position"
 			return String.format("% 4.4f", x);
 		case 1: // "Y position"
 			return String.format("% 4.4f", y);
 		case 2: // "Data value"
 			if (set == null || q == null) return "-";
			return String.format("% 4.4f", set.getDouble((int)q.x, (int)q.y));
 		case 3: // q X
 			if (q == null) return "-";
 			return String.format("% 4.4f", q.x);
 		case 4: // q Y
 			if (q == null) return "-";
 			return String.format("% 4.4f", q.y);
 		case 5: // q Z
 			if (q == null) return "-";
 			return String.format("% 4.4f", q.z);
 		case 6: // 20
 			if (qSpace == null) return "-";
 			return String.format("% 3.3f", Math.toDegrees(qSpace.scatteringAngle(q)));
 		case 7: // resolution
 			if (q == null) return "-";
 			return String.format("% 4.4f", (2*Math.PI)/q.length());
 		case 8: // Dataset name
 			if (set == null) return "-";
 			return set.getName();
 
 		default:
 			return "Not found";
 		}
 		
 	}
 
 	@Override
 	public String getToolTipText(Object element) {
 		return "Any selection region can be used in information box tool.";
 	}
 
 }
