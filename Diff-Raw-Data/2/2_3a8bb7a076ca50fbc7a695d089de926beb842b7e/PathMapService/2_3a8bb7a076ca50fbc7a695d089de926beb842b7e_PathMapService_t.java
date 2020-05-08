 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.launch.core.internal.services;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.services.IPathMap.PathMapRule;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.services.AbstractService;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.interfaces.IPathMapGeneratorService;
 import org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService;
 import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
 
 /**
  * Path map service implementation.
  */
 public class PathMapService extends AbstractService implements IPathMapService {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService#getPathMap(java.lang.Object)
 	 */
     @Override
 	public PathMapRule[] getPathMap(Object context) {
     	Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(context);
 
 		PathMapRule[] rules = null;
 		List<PathMapRule> rulesList = new ArrayList<PathMapRule>();
 
 		// Get the launch configuration for that peer model
 		ILaunchConfiguration config = (ILaunchConfiguration) Platform.getAdapterManager().getAdapter(context, ILaunchConfiguration.class);
 		if (config == null) {
 			config = (ILaunchConfiguration) Platform.getAdapterManager().loadAdapter(context, "org.eclipse.debug.core.ILaunchConfiguration"); //$NON-NLS-1$
 		}
 
 		if (config != null) {
 			try {
 				String path_map_cfg = config.getAttribute(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.ATTR_PATH_MAP, ""); //$NON-NLS-1$
 				rulesList.addAll(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.parsePathMapAttribute(path_map_cfg));
 
 				path_map_cfg = config.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, ""); //$NON-NLS-1$
 				rulesList.addAll(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.parseSourceLocatorMemento(path_map_cfg));
 			} catch (CoreException e) { /* ignored on purpose */ }
 		}
 
         IPathMapGeneratorService generator = ServiceManager.getInstance().getService(context, IPathMapGeneratorService.class);
         if (generator != null) {
         	PathMapRule[] generatedRules = generator.getPathMap(context);
         	if (generatedRules != null && generatedRules.length > 0) {
         		rulesList.addAll(Arrays.asList(generatedRules));
         	}
         }
 
 		if (!rulesList.isEmpty()) {
 	        int cnt = 0;
 	        String id = getClientID();
 	        for (PathMapRule r : rulesList) r.getProperties().put(IPathMap.PROP_ID, id + "/" + cnt++); //$NON-NLS-1$
 			rules = rulesList.toArray(new PathMapRule[rulesList.size()]);
 		}
 
 		return rules;
 	}
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService#addPathMap(java.lang.Object, java.lang.String, java.lang.String)
      */
     @Override
     public PathMapRule addPathMap(Object context, String source, String destination) {
     	Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(context);
 		Assert.isNotNull(source);
 		Assert.isNotNull(destination);
 
 		PathMapRule rule = null;
 		List<PathMapRule> rulesList = new ArrayList<PathMapRule>();
 
 		// Get the launch configuration for that peer model
 		ILaunchConfigurationWorkingCopy config = (ILaunchConfigurationWorkingCopy) Platform.getAdapterManager().getAdapter(context, ILaunchConfigurationWorkingCopy.class);
 		if (config == null) {
 			config = (ILaunchConfigurationWorkingCopy) Platform.getAdapterManager().loadAdapter(context, "org.eclipse.debug.core.ILaunchConfigurationWorkingCopy"); //$NON-NLS-1$
 		}
 
 		if (config != null) {
 			populatePathMapRulesList(config, rulesList);
 
 			// Find an existing path map rule for the given source and destination
 			for (PathMapRule candidate : rulesList) {
 				if (source.equals(candidate.getSource()) && destination.equals(candidate.getDestination())) {
 					rule = candidate;
 					break;
 				}
 			}
 
 			// If not matching path map rule exist, create a new one
 			if (rule == null) {
 				Map<String, Object> props = new LinkedHashMap<String, Object>();
				props.put(IPathMap.PROP_SOURCE, source);
				props.put(IPathMap.PROP_DESTINATION, destination);
 				rule = new org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.PathMapRule(props);
 				rulesList.add(rule);
 
 				// Update the launch configuration
 				updateLaunchConfiguration(config, rulesList);
 
 		        // Apply the path map
 		        applyPathMap(context, new Callback() {
 		        	@Override
 		        	protected void internalDone(Object caller, IStatus status) {
 		        		if (status != null && Platform.inDebugMode()) {
 		        			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 		        		}
 		        	}
 		        });
 			}
 		}
 
         return rule;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService#removePathMap(java.lang.Object, org.eclipse.tcf.services.IPathMap.PathMapRule)
      */
     @Override
     public void removePathMap(final Object context, final PathMapRule rule) {
     	Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
 		Assert.isNotNull(context);
 		Assert.isNotNull(rule);
 
 		List<PathMapRule> rulesList = new ArrayList<PathMapRule>();
 
 		// Get the launch configuration for that peer model
 		ILaunchConfigurationWorkingCopy config = (ILaunchConfigurationWorkingCopy) Platform.getAdapterManager().getAdapter(context, ILaunchConfigurationWorkingCopy.class);
 		if (config == null) {
 			config = (ILaunchConfigurationWorkingCopy) Platform.getAdapterManager().loadAdapter(context, "org.eclipse.debug.core.ILaunchConfigurationWorkingCopy"); //$NON-NLS-1$
 		}
 
 		if (config != null) {
 			populatePathMapRulesList(config, rulesList);
 
 			// Remove the given rule from the list of present
 			if (rulesList.remove(rule)) {
 				// Update the launch configuration
 				updateLaunchConfiguration(config, rulesList);
 
 		        // Apply the path map
 		        applyPathMap(context, new Callback() {
 		        	@Override
 		        	protected void internalDone(Object caller, IStatus status) {
 		        		if (status != null && Platform.inDebugMode()) {
 		        			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 		        		}
 		        	}
 		        });
 			}
 		}
     }
 
     /**
      * Populate the given path map rules list from the given launch configuration.
      *
      * @param config The launch configuration. Must not be <code>null</code>.
      * @param rulesList The path map rules list. Must not be <code>null</code>.
      */
     private void populatePathMapRulesList(ILaunchConfiguration config, List<PathMapRule> rulesList) {
     	Assert.isNotNull(config);
     	Assert.isNotNull(rulesList);
 
 		try {
 			String path_map_cfg = config.getAttribute(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.ATTR_PATH_MAP, ""); //$NON-NLS-1$
 			String path_map_cfgV1 = config.getAttribute(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.ATTR_PATH_MAP + "V1", ""); //$NON-NLS-1$ //$NON-NLS-2$
 
 			rulesList.addAll(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.parsePathMapAttribute(path_map_cfgV1));
 
 	        int i = -1;
 	        for (PathMapRule candidate : org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.parsePathMapAttribute(path_map_cfg)) {
 	            if (rulesList.contains(candidate)) {
 	                i = rulesList.indexOf(candidate);
 	            } else {
 	            	rulesList.add(++i, candidate);
 	            }
 	        }
 		} catch (CoreException e) { /* ignored on purpose */ }
     }
 
     /**
      * Write back the given path map rules list to the given launch configuration.
      *
      * @param config The launch configuration. Must not be <code>null</code>.
      * @param rulesList The path map rules list. Must not be <code>null</code>.
      */
     private void updateLaunchConfiguration(ILaunchConfigurationWorkingCopy config, List<PathMapRule> rulesList) {
     	Assert.isNotNull(config);
     	Assert.isNotNull(rulesList);
 
 		// Update the launch configuration
         for (PathMapRule candidate : rulesList) {
             candidate.getProperties().remove(IPathMap.PROP_ID);
         }
 
         StringBuilder bf = new StringBuilder();
         StringBuilder bf1 = new StringBuilder();
 
         for (PathMapRule candidate : rulesList) {
         	if (!(candidate instanceof org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.PathMapRule)) continue;
 
             boolean enabled = true;
             if (candidate.getProperties().containsKey("Enabled")) { //$NON-NLS-1$
                 enabled = Boolean.parseBoolean(candidate.getProperties().get("Enabled").toString()); //$NON-NLS-1$
             }
             if (enabled) {
                 candidate.getProperties().remove("Enabled"); //$NON-NLS-1$
                 bf.append(candidate.toString());
             }
             bf1.append(candidate.toString());
         }
 
         if (bf.length() == 0) {
             config.removeAttribute(TCFLaunchDelegate.ATTR_PATH_MAP);
         } else {
             config.setAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, bf.toString());
         }
 
         if (bf1.length() == 0) {
             config.removeAttribute(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.ATTR_PATH_MAP + "V1"); //$NON-NLS-1$
         } else {
             config.setAttribute(org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate.ATTR_PATH_MAP + "V1", bf1.toString()); //$NON-NLS-1$
         }
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService#applyPathMap(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
      */
     @Override
     public void applyPathMap(final Object context, final ICallback callback) {
     	Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
     	Assert.isNotNull(context);
     	Assert.isNotNull(callback);
 
     	IPeer peer = context instanceof IPeer ? (IPeer)context : null;
     	if (peer == null && context instanceof IPeerModel) peer = ((IPeerModel)context).getPeer();
     	if (peer == null && context instanceof IPeerModelProvider && ((IPeerModelProvider)context).getPeerModel() != null) peer = ((IPeerModelProvider)context).getPeerModel().getPeer();
 
     	if (peer != null) {
 			final IChannel channel = Tcf.getChannelManager().getChannel(peer);
 			if (channel != null && IChannel.STATE_OPEN == channel.getState()) {
 				// Channel is open -> Have to update the path maps
 
 				// Get the configured path mappings. This must be called from
 				// outside the runnable as getPathMap(...) must be called from
 				// outside of the TCF dispatch thread.
 				final PathMapRule[] configuredMap = getPathMap(context);
 
 				if (configuredMap != null && configuredMap.length > 0) {
 					// Create the runnable which set the path map
 					Runnable runnable = new Runnable() {
 						@Override
 						public void run() {
 							final IPathMap svc = channel.getRemoteService(IPathMap.class);
 							if (svc != null) {
 								// Get the old path maps first. Keep path map rules not coming from us
 								svc.get(new IPathMap.DoneGet() {
 									@Override
 									public void doneGet(IToken token, Exception error, PathMapRule[] map) {
 										// Merge the maps to a new list
 										List<PathMapRule> rules = new ArrayList<PathMapRule>();
 
 										if (map != null && map.length > 0) {
 											for (PathMapRule rule : map) {
 												if (rule.getID() == null || !rule.getID().startsWith(getClientID())) {
 													rules.add(rule);
 												}
 											}
 										}
 
 										rules.addAll(Arrays.asList(configuredMap));
 										if (!rules.isEmpty()) {
 											svc.set(rules.toArray(new PathMapRule[rules.size()]), new IPathMap.DoneSet() {
 												@Override
 												public void doneSet(IToken token, Exception error) {
 													callback.done(PathMapService.this, StatusHelper.getStatus(error));
 												}
 											});
 										} else {
 											callback.done(PathMapService.this, Status.OK_STATUS);
 										}
 									}
 								});
 							} else {
 								callback.done(PathMapService.this, Status.OK_STATUS);
 							}
 						}
 					};
 
 					Protocol.invokeLater(runnable);
 				} else {
 		    		callback.done(PathMapService.this, Status.OK_STATUS);
 				}
 			} else {
 	    		callback.done(PathMapService.this, Status.OK_STATUS);
 			}
     	} else {
     		callback.done(PathMapService.this, Status.OK_STATUS);
     	}
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tcf.core.interfaces.IPathMapService#getClientID()
      */
     @SuppressWarnings("restriction")
     @Override
     public String getClientID() {
         return org.eclipse.tcf.internal.debug.Activator.getClientID();
     }
 }
