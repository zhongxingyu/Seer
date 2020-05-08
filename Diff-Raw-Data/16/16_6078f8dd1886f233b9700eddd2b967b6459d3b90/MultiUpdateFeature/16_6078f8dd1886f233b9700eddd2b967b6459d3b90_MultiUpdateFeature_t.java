 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Ivar Meikas
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.core.features;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 
 public class MultiUpdateFeature extends AbstractUpdateFeature {
 
	public final static String FORCE_UPDATE_ALL = "force.update.all";
	
 	protected List<IUpdateFeature> features = new ArrayList<IUpdateFeature>();
 
 	public MultiUpdateFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public boolean canUpdate(IUpdateContext context) {
 		for (IUpdateFeature p : features) {
 			if (p.canUpdate(context)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public IReason updateNeeded(IUpdateContext context) {
 		String text = null;
 		for (IUpdateFeature p : features) {
 			IReason reason = p.updateNeeded(context);
 			if (reason.toBoolean()) {
 				if (text==null) {
 					text = reason.getText();
 				}
 				else
 					text += "\n" + reason.getText(); //$NON-NLS-1$
 			}
 		}
 		if (text!=null)
 			return Reason.createTrueReason(text);
 		return Reason.createFalseReason();
 	}
 
 	@Override
 	public boolean update(IUpdateContext context) {
 		boolean updated = false;
		boolean forceUpdate =  Boolean.TRUE.equals(context.getProperty(FORCE_UPDATE_ALL));
			
 		for (IUpdateFeature p : features) {
			if ((p.updateNeeded(context).toBoolean() || forceUpdate) && p.update(context)) {
 				updated = true;
 			}
 		}
 
 		return updated;
 	}
 
 	public void addUpdateFeature(IUpdateFeature feature) {
 		if (feature != null) {
 			features.add(feature);
 		}
 	}
 }
