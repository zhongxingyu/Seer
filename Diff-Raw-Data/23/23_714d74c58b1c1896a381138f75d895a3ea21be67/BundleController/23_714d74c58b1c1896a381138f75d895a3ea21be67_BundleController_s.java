 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.manager.controllers;
 
 import static org.oobium.utils.StringUtils.blank;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.app.controllers.HttpController;
 import org.oobium.manager.ManagerService;
 import org.oobium.manager.controllers.workers.InstallWorker;
 import org.oobium.manager.controllers.workers.StartWorker;
 import org.oobium.manager.controllers.workers.StopWorker;
 import org.oobium.manager.controllers.workers.UninstallWorker;
 import org.oobium.manager.controllers.workers.UpdateWorker;
import org.oobium.utils.StringUtils;
 import org.oobium.utils.json.JsonUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 public class BundleController extends HttpController {
 
 	@Override // POST/URL/[models]
 	public void create() throws Exception {
 		String location = param("location");
 		if(blank(location)) {
 			renderErrors("location not set");
 		} else {
 			// perform in a worker because locations are not necessarily local
 			InstallWorker worker = new InstallWorker(location);
 			worker.submitTo(ManagerService.class);
 			renderAccepted();
 		}
 	}
 	
 	@Override
 	// DELETE/URL/[models]/id
 	// DELETE/URL/[models]/name
 	// DELETE/URL/[models]/name_version
 	public void destroy() throws Exception {
 		Bundle[] bundles = getBundles();
 		if(!isRendered()) {
 			// perform in a worker because the bundle may need to be stopped first
 			UninstallWorker worker = new UninstallWorker(bundles);
 			worker.submitTo(ManagerService.class);
 			renderAccepted();
 		}
 	}
 
 	private Map<String, Object> getBundleMap(Bundle bundle) {
 		if(bundle != null) {
 			Map<String, Object> map = new LinkedHashMap<String, Object>();
 			map.put("id", bundle.getBundleId());
 			map.put("state", bundle.getState());
 			map.put("name", bundle.getSymbolicName());
 			map.put("version", bundle.getVersion());
 			map.put("location", bundle.getLocation());
 			return map;
 		} else {
 			return new HashMap<String, Object>(0);
 		}
 	}
 	
 	private Bundle[] getBundles() {
 		BundleContext context = ManagerService.context();
 
 		if(getId(int.class) > 0) {
 			Bundle bundle = context.getBundle(getId(int.class));
 			if(bundle != null) {
 				return new Bundle[] { bundle };
 			} else {
 				renderErrors("there is no bundle with id: " + getId());
 				return null;
 			}
 		}
 		
 		PackageAdmin admin = getPackageAdmin(context);
 		if(admin == null) {
 			renderErrors("cannot lookup bundles: PackageAdmin service not present");
 			return null;
 		}
 
 		String name = param("name");
 		if(name != null) {
 			String version = param("version");
 			Bundle[] bundles = admin.getBundles(name, version);
 			if(bundles == null) {
 				if(version == null) {
 					renderErrors("there are no bundles with name: " + name);
 				} else {
 					renderErrors("there is no bundle with name: " + name + "_" + version);
 				}
 				return null;
 			} else {
 				return bundles;
 			}
 		}
 
 		List<String> bstrings = JsonUtils.toStringList(param("bundles"));
 		if(bstrings != null) {
 			List<Bundle> bundles = new ArrayList<Bundle>();
 			for(String bstring : bstrings) {
 				Bundle[] ba = null;
 				try {
 					long id = Long.parseLong(bstring);
 					Bundle bundle = context.getBundle(id);
 					if(bundle == null) {
 						renderErrors("no bundle found for id: " + id);
 					} else {
 						bundles.add(bundle);
 					}
 				} catch (Exception e) {
 					String[] sa = bstring.split("_", 2);
 					ba = admin.getBundles(sa[0], (sa.length > 1) ? sa[1] : null);
 					if(ba == null) {
 						renderErrors("no bundles match: " + bstring);
 					} else {
 						bundles.addAll(Arrays.asList(ba));
 					}
 				}
 				if(isRendered()) {
 					return null;
 				}
 			}
 			return bundles.toArray(new Bundle[bundles.size()]);
 		}
 
 		return null;
 	}
 
 	private PackageAdmin getPackageAdmin(BundleContext context) {
 		ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
 		if(ref != null) {
 			return (PackageAdmin) context.getService(ref);
 		}
 		return null;
 	}
 	
 	@Override
 	public void handleRequest() throws Exception {
 		Bundle[] bundles = getBundles();
 		if(!isRendered()) {
 			BundleContext context = ManagerService.context();
 			context.addFrameworkListener(new RefreshListener(bundles));
 			getPackageAdmin(context).refreshPackages(getBundles());
 			renderAccepted();
 		}
 	}
 	
 	@Override // GET/URL/[models]/id
 	public void show() throws Exception {
 		Bundle bundle = ManagerService.context().getBundle(getId(int.class));
 		if(bundle == null) {
 			renderErrors("there is no bundle with id: " + getId());
 		} else {
 			render(JsonUtils.toJson(getBundleMap(bundle)));
 		}
 	}
 
 	@Override // GET/URL/[models]
 	public void showAll() throws Exception {
 		Bundle[] bundles = ManagerService.context().getBundles();
 		if(hasParam("name")) {
 			String name = param("name");
 			List<Bundle> list = new ArrayList<Bundle>();
 			for(Bundle bundle : bundles) {
 				if(bundle.getSymbolicName().equals(name)) {
 					list.add(bundle);
 				}
 			}
 			bundles = list.toArray(new Bundle[list.size()]);
 		}
 		StringBuilder sb = new StringBuilder();
 		sb.append('[');
 		if(wantsJS()) { sb.append('\n'); } else { sb.append("<br/>"); };
 		for(int i = 0; i < bundles.length; i++) {
 			if(i != 0) if(wantsJS()) { sb.append(",\n"); } else { sb.append(",<br/>"); };
 			sb.append(JsonUtils.toJson(getBundleMap(bundles[i])));
 		}
 		if(wantsJS()) { sb.append('\n'); } else { sb.append("<br/>"); };
 		sb.append(']');
 		render(sb.toString());
 	}
 
 	@Override // PUT/URL/[models]/id
 	public void update() throws Exception {
 		Bundle[] bundles = getBundles();
 		if(!isRendered()) {
 			int state = param("state", int.class);
 			if(state > 0) {
 				switch(state) {
 				case Bundle.ACTIVE: { // ACTIVATE
 					StartWorker worker = new StartWorker(bundles);
 					worker.submitTo(ManagerService.class);
 					renderAccepted();
 					break;
 				}
 				case Bundle.RESOLVED: { // DE-ACTIVATE
 					StopWorker worker = new StopWorker(bundles);
 					worker.submitTo(ManagerService.class);
 					renderAccepted();
 					break;
 				}
 				default:
 					renderErrors("unsupported state: " + state);
 					break;
 				}
 			}
 			
 			if(!isRendered()) {
 				String[] locations = param("location", String[].class);
				System.out.println("locations: " + StringUtils.asString(locations));
 				UpdateWorker worker = new UpdateWorker(bundles, locations);
 				worker.submitTo(ManagerService.class);
 				renderAccepted();
 			}
 		}
 	}
 	
 }
