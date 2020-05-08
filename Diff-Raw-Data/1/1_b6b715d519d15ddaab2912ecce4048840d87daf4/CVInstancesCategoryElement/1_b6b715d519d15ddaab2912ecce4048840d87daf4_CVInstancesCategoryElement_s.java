 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.ui.views;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Display;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
import org.jboss.tools.deltacloud.core.DeltaCloudImage;
 import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
 import org.jboss.tools.deltacloud.core.IInstanceFilter;
 import org.jboss.tools.deltacloud.core.IInstanceListListener;
 
 public class CVInstancesCategoryElement extends CVCategoryElement implements IInstanceListListener {
 
 	private Viewer viewer;
 	private CVInstancesCategoryElement category;
 	
 	public CVInstancesCategoryElement(Object element, String name, Viewer viewer) {
 		super(element, name, CVCategoryElement.INSTANCES);
 		this.viewer = viewer;
 		DeltaCloud cloud = (DeltaCloud)getElement();
 		cloud.addInstanceListListener(this);
 		this.category = this;
 	}
 
 	protected void finalize() throws Throwable {
 		DeltaCloud cloud = (DeltaCloud)getElement();
 		cloud.removeInstanceListListener(this);
 		super.finalize();
 	}
 	
 	@Override
 	public Object[] getChildren() {
 		if (!initialized) {
 			DeltaCloud cloud = (DeltaCloud)getElement();
 			cloud.removeInstanceListListener(this);
 			DeltaCloudInstance[] instances = filter(cloud.getCurrInstances());
 			for (int i = 0; i < instances.length; ++i) {
 				DeltaCloudInstance d = instances[i];
 				CVInstanceElement element = new CVInstanceElement(d, d.getName());
 				addChild(element);
 			}
 			initialized = true;
 			cloud.addInstanceListListener(this);
 		}
 		return super.getChildren();
 	}
 
 	@Override
 	public void listChanged(DeltaCloud cloud, DeltaCloudInstance[] newInstances) {
 		clearChildren();
 		DeltaCloudInstance[] instances = filter(newInstances);
 		for (int i = 0; i < instances.length; ++i) {
 			DeltaCloudInstance d = instances[i];
 			CVInstanceElement element = new CVInstanceElement(d, d.getName());
 			addChild(element);
 		}
 		initialized = true;
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				((TreeViewer)viewer).refresh(category, false);
 			}
 		});
 	}
 
 	private DeltaCloudInstance[] filter(DeltaCloudInstance[] input) {
 		ArrayList<DeltaCloudInstance> array = new ArrayList<DeltaCloudInstance>();
 		DeltaCloud cloud = (DeltaCloud)getElement();
 		IInstanceFilter f = cloud.getInstanceFilter();
 		for (int i = 0; i < input.length; ++i) {
 			DeltaCloudInstance instance = input[i];
 			if (f.isVisible(instance))
 				array.add(instance);
 		}
 		return array.toArray(new DeltaCloudInstance[array.size()]);
 	}
 
 }
