 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.tcf.te.launch.ui.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
 import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
 import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 
 /**
  * LaunchNode
  */
 public class LaunchNode extends ContainerModelNode {
 
 	public static final String TYPE_ROOT = "root"; //$NON-NLS-1$
 	public static final String TYPE_LAUNCH_CONFIG_TYPE = "launchConfigType"; //$NON-NLS-1$
 	public static final String TYPE_LAUNCH_CONFIG = "launchConfig"; //$NON-NLS-1$
 
 	protected static final String PROPERTY_LAUNCH_CONFIG_TYPE = TYPE_LAUNCH_CONFIG_TYPE;
 	protected static final String PROPERTY_LAUNCH_CONFIG = TYPE_LAUNCH_CONFIG;
 	protected static final String PROPERTY_MODEL = "model"; //$NON-NLS-1$
 
 	private LaunchNode(String type) {
 		super();
 		setProperty(IModelNode.PROPERTY_TYPE, type);
 	}
 
 	public LaunchNode(LaunchModel model) {
 		this(TYPE_ROOT);
 		setProperty(PROPERTY_MODEL, model);
 	}
 
 	public LaunchNode(ILaunchConfiguration config) {
 		this(TYPE_LAUNCH_CONFIG);
 		setProperty(PROPERTY_LAUNCH_CONFIG, config);
 	}
 
 	public LaunchNode(ILaunchConfigurationType configType) {
 		this(TYPE_LAUNCH_CONFIG_TYPE);
 		setProperty(PROPERTY_LAUNCH_CONFIG_TYPE, configType);
 	}
 
 	public boolean isType(String type) {
 		return type.equals(getStringProperty(IModelNode.PROPERTY_TYPE));
 	}
 
 	/**
 	 * Return the model for this node. Must not be <code>null</code>.
 	 */
 	public LaunchModel getModel() {
 		LaunchModel model = (LaunchModel)getProperty(PROPERTY_MODEL);
 		IModelNode parent = getParent();
 
 		while (model == null && parent != null) {
 			model = (LaunchModel)parent.getProperty(PROPERTY_MODEL);
 			parent = parent.getParent();
 		}
 
 		Assert.isNotNull(model);
 		return model;
 	}
 
 	/**
 	 * Return the launch confuration for this node or <code>null</code>.
 	 */
 	public ILaunchConfiguration getLaunchConfiguration() {
 		return (ILaunchConfiguration)getProperty(PROPERTY_LAUNCH_CONFIG);
 	}
 
 	/**
 	 * Return the launch configuration type for this launch node or <code>null</code>.
 	 */
 	public ILaunchConfigurationType getLaunchConfigurationType() {
 		if (getLaunchConfiguration() != null) {
 			try {
 				return getLaunchConfiguration().getType();
 			}
 			catch (Exception e) {
 			}
 		}
 		else if (isType(TYPE_LAUNCH_CONFIG)) {
 			return ((LaunchNode)getParent()).getLaunchConfigurationType();
 		}
 		return (ILaunchConfigurationType)getProperty(PROPERTY_LAUNCH_CONFIG_TYPE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.model.ModelNode#getName()
 	 */
 	@Override
 	public String getName() {
 		if (isType(TYPE_ROOT)) {
 			return "Launches"; //$NON-NLS-1$
 		}
 		else if (isType(TYPE_LAUNCH_CONFIG_TYPE)) {
 			return getLaunchConfigurationType().getName();
 		}
 		else if (isType(TYPE_LAUNCH_CONFIG)) {
 			return getLaunchConfiguration().getName();
 		}
 		return super.getName();
 	}
 
 	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#hashCode()
	 */
	@Override
	public int hashCode() {
		if (isType(TYPE_LAUNCH_CONFIG_TYPE) && getLaunchConfigurationType() != null) {
			return getLaunchConfigurationType().hashCode();
		}
		else if (isType(TYPE_LAUNCH_CONFIG) && getLaunchConfiguration() != null) {
			return getLaunchConfiguration().hashCode();
		}
	    return super.hashCode();
	}

	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof LaunchNode && isType(((LaunchNode)obj).getStringProperty(PROPERTY_TYPE))) {
 			if (getModel().getModelRoot().equals(((LaunchNode)obj).getModel().getModelRoot())) {
 				if (isType(TYPE_LAUNCH_CONFIG_TYPE)) {
 					return getLaunchConfigurationType().equals(((LaunchNode)obj).getLaunchConfigurationType());
 				}
 				else if (isType(TYPE_LAUNCH_CONFIG)) {
 					return getLaunchConfiguration().equals(((LaunchNode)obj).getLaunchConfiguration());
 				}
 			}
 		}
 		return super.equals(obj);
 	}
 
 	/**
 	 * Check if the launch config node is valid for the given launch mode.
 	 * @param mode The launch mode or <code>null</code> to check for all supported modes;
 	 * @return <code>true</code> if the node is valid for the the given launch mode (or all supported modes if mode is <code>null</code>).
 	 */
 	public boolean isValidFor(String mode) {
 		if (isType(TYPE_LAUNCH_CONFIG)) {
 			if (getLaunchConfigurationType() == null) {
 				return false;
 			}
 			List<String> modes;
 			if (mode != null && mode.trim().length() > 0) {
 				modes = new ArrayList<String>();
 				modes.add(mode);
 			}
 			else {
 				modes = Arrays.asList(LaunchConfigHelper.getLaunchConfigTypeModes(getLaunchConfigurationType(), false));
 			}
 			for (String m : modes) {
 				if (!getLaunchConfigurationType().supportsMode(m)) {
 					return false;
 				}
 				ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(getLaunchConfigurationType(), m);
 				if (delegate != null) {
 					try {
 						delegate.validate(mode, getLaunchConfiguration());
 					}
 					catch (Exception e) {
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
