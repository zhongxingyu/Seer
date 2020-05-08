 /**
  * 
  */
 package org.teagle.vcttool.control;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.teagle.vcttool.view.ConnectionEvent;
 import org.teagle.vcttool.view.ConnectionListener;
 import org.teagle.vcttool.view.ResourceInstanceListener;
 import org.teagle.vcttool.view.ResourceInstanceWidget;
 import org.teagle.vcttool.view.VctView;
 import org.teagle.vcttool.view.dialogs.ConfigurationDialog;
 import org.teagle.vcttool.view.dialogs.MessageDialog;
 
 import teagle.vct.model.ConfigParamAtomic;
 import teagle.vct.model.Configuration;
 import teagle.vct.model.ModelManager;
 import teagle.vct.model.Ptm;
 import teagle.vct.model.ResourceInstance;
 
 /**
  * @author sim
  *
  */
 public class ResourceInstanceController implements ResourceInstanceListener, ConnectionListener {
 	
 	private ResourceInstance resourceInstance;
 	private Ptm ptm;
 	
 	private Map<VctController, ResourceInstanceWidget> views = new HashMap<VctController, ResourceInstanceWidget>();
 	
 	public ResourceInstanceController(ResourceInstance resourceInstance) {
 		this.resourceInstance = resourceInstance;
 		
 		String ptmName = null;
 		int pos = resourceInstance.getCommonName().indexOf('.');
 		if (pos > 0) {
 			ptmName = resourceInstance.getCommonName().substring(0, pos);
 		}
 		
 		if (ptmName != null) {
 			List<? extends Ptm> ptms = ModelManager.getInstance().listPtms();
 			for (Ptm ptm : ptms) {
 				if (ptm.getCommonName().equals(ptmName)) {
 					this.ptm = ptm;
 					break;
 				}
 			}
 		}
 	}
 
 	public ResourceInstance getResourceInstance() {
 		return resourceInstance;
 	}
 	
 	public ResourceInstanceWidget getView(VctController parent) {
 		ResourceInstanceWidget view = views.get(parent);
 		if (view == null) {
 			view = new ResourceInstanceWidget(parent.getView(null));
 			views.put(parent, view);
 			
 			//ibo: needed by the Policy Engine
 			view.setResourceInstance(resourceInstance);
 
 			view.setName(resourceInstance.getCommonName());
 			view.setState(resourceInstance.getState().toString());
 			if (ptm != null) {
 				view.setPtmName(ptm.getCommonName());				
 			}
 			
 //			ConfigParam params = resourceInstance.getResourceSpec().getConfigurationParameters();
 //			if (params instanceof ConfigParamComposite) {
 //				for (ConfigParamAtomic atomic : ((ConfigParamComposite) params).getConfigurationParameters()) {
 //					if (atomic.getType().startsWith("reference")) {
 //						view.addConfigSource(atomic.getCommonName(), atomic, null);
 //					}
 //				}
 //			} else if (params instanceof ConfigParamAtomic) {
 //				view.addConfigSource(((ConfigParamAtomic)params).getCommonName(), params, null);
 //			}
 			
 			List<? extends ConfigParamAtomic> params = resourceInstance.getResourceSpec().getConfigurationParameters();
 			for (ConfigParamAtomic atomic : params){
 				if (atomic.getType().startsWith("reference")) {
 					view.addConfigSource(atomic.getCommonName(), atomic, null);
 				}
 			}
 			
 			String description = resourceInstance.getDescription();
 			view.setDescription(description != null && !description.isEmpty() ? description : "<no description>");
 
 			view.setData(this);
 			
 			view.addConnectionListener(this);
 			view.addResourceInstanceListener(this);
 			
 			view.setLocation(resourceInstance.getGeometry().getX(), resourceInstance.getGeometry().getX());
 			
 			view.pack(true);
 			view.layout(true, true);
 		}
 		
 		return view;
 	}
 	
 	public void setPtm(Ptm ptm) {
 		this.ptm = ptm;
 		for (ResourceInstanceWidget view : views.values()) {
 			view.setPtmName(ptm.getCommonName());
 		}		
 	}
 
 	public void onMoved(Point position) {
 		resourceInstance.getGeometry().setX(position.x);
 		resourceInstance.getGeometry().setY(position.y);		
 	}
 
 
 	public void onEditConfig(VctView vctView) {
 		ConfigurationDialog dialog = new ConfigurationDialog(vctView.getShell(), resourceInstance.getCommonName());
 		
 		List<? extends Configuration> configurations = resourceInstance.getConfigurations();
 		for (Configuration config : configurations) 
 		{
 			String name = config.getConfigParamAtomic().getCommonName();
 			String type = config.getConfigParamAtomic().getType();
 			String description = config.getConfigParamAtomic().getDescription();
 			
 			String defaultValue = config.getValue();
 			if (defaultValue == null || defaultValue.isEmpty()) {
 				defaultValue = config.getConfigParamAtomic().getDefaultValue();
 			}
 			if (type.equals("boolean")) {
 				dialog.addConfigurationParameter(name, Boolean.parseBoolean(defaultValue), description);
 			} else if (type.equals("string") || type.startsWith("int") || type.equals("float")) {
 				dialog.addConfigurationParameter(name, defaultValue, description);				
 			}
 		}
 
 		switch(dialog.show()) {
 		case SWT.OK:
 			for (Configuration config : resourceInstance.getConfigurations()) {
 				String name = config.getConfigParamAtomic().getCommonName();
 				String value = dialog.getConfiguratonParameter(name);
 				if (value != null) {
 					config.setValue(value);
 				}
 			}
 			
 			break;
 		case SWT.CANCEL:
 			break;
 		}
 	}
 
 
 	public void onConnectionNew(ConnectionEvent event) {
 		ResourceInstanceController targetControler = (ResourceInstanceController)event.target.getData();
 		
 		Object data = event.sourceLabel.getData();
 		if (data == null) {
 			for (VctController vctControler : views.keySet()) {
 				if (views.get(vctControler) == event.source) {
 					targetControler.getResourceInstance().setParentInstance(resourceInstance);
 					break;
 				}
 			}
 		} else {
 			ConfigParamAtomic param = (ConfigParamAtomic)data;
 			Configuration config = null;
 			for (Configuration cfg : resourceInstance.getConfigurations()) {
 				if (cfg.getConfigParamAtomic() == param) {
 					config = cfg;
 					break;
 				}
 			}
 			if (config == null) {
 				config = ModelManager.getInstance().createConfiguration(param);
 				resourceInstance.addConfiguration(config);
 			}
 			
 			String type = param.getType();
 			if (type.equals("reference")) {
 				config.setValue(targetControler.getResourceInstance().getCommonName());
 			} else if (type.equals("reference-array")) {
 				String[] entries = config.getValue().split(",");
 				String added = "";
 				String newValue = targetControler.getResourceInstance().getCommonName();
 				for (int i = 0; i < entries.length; i++) {
 					added += "," + entries[i];
					if (null != newValue && newValue.equals(entries[i])) {
 						newValue = null;
 					}
 				}
 				if (newValue != null) {
 					added += "," + newValue;
 					config.setValue(added);
 				}
 			}
 		}
 	}
 
 	
 	public void onConnectionDeleted(ConnectionEvent event) {
 		ResourceInstanceController targetControler = (ResourceInstanceController)event.target.getData();
 		Object data = event.sourceLabel.getData();
 		if (data == null) {
 			for (VctController vctControler : views.keySet()) {
 				if (views.get(vctControler) == event.source) {
 					targetControler.getResourceInstance().setParentInstance(null);
 				}
 			}
 		} else {
 			ConfigParamAtomic param = (ConfigParamAtomic)data;
 			String type = param.getType();
 			for (Configuration config : resourceInstance.getConfigurations()) {
 				if (config.getConfigParamAtomic() == param) {
 					if (param.isReference()) {
 						resourceInstance.removeConfiguration(config);
 					} else if (type.equals("reference-array")) {
 						String[] entries = config.getValue().split(",");
 						String added = "";
 						String removeValue = targetControler.getResourceInstance().getCommonName();
 						for (int i = 0; i < entries.length; i++) {
 							if (!removeValue.equals(entries[i])) {
 								added += "," + entries[i];								
 							}
 						}
 						if (!added.isEmpty()) {
 							config.setValue(added);							
 						} else {
 							resourceInstance.removeConfiguration(config);
 						}
 					}
 					break;
 				}
 			}				
 		}		
 	}
 
 	public void onHelp(VctView vctView) {
 		
 		String description = resourceInstance.getDescription();
 		if (description.equals("")){
 			new MessageDialog(vctView.getShell(), "No description available.");
 		}
 		else{
 			new MessageDialog(vctView.getShell(), description);
 		}
 		
 		
 	}
 	
 
 
 	public void onDelete(ResourceInstanceWidget widget) {
 		for (VctController vctControler : views.keySet()) {
 			if (views.get(vctControler) == widget) {
 				vctControler.removeResourceInstance(this);
 				vctControler.getView(null).removeResourceInstanceWidget(widget);
 			}
 		}
 	}
 
 	public List<Configuration> getReferenceConfigurations() {
 		List<Configuration> configurations = new ArrayList<Configuration>();
 		for (Configuration config : resourceInstance.getConfigurations()) {
 			if (config.isReference()) {
 				configurations.add(config);
 			}
 		}
 		
 		return configurations;
 	}
 
 }
