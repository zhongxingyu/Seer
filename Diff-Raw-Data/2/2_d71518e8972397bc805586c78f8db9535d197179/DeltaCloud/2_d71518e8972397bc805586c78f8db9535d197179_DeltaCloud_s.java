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
 package org.jboss.tools.deltacloud.core;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.security.storage.EncodingUtils;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.equinox.security.storage.StorageException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudAuthException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClient;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
 import org.jboss.tools.deltacloud.core.client.HardwareProfile;
 import org.jboss.tools.deltacloud.core.client.Image;
 import org.jboss.tools.deltacloud.core.client.Instance;
 import org.jboss.tools.deltacloud.core.client.Realm;
 
 public class DeltaCloud {
 	
 	private String name;
 	private String username;
 	private String url;
 	private String type;
 	private String lastKeyname = "";
 	private String lastImageId = "";
 	private DeltaCloudClient client;
 	private ArrayList<DeltaCloudInstance> instances;
 	private ArrayList<DeltaCloudImage> images;
 	private IImageFilter imageFilter;
 	private IInstanceFilter instanceFilter;
 	private Map<String, Job> actionJobs;
 	private Object imageLock = new Object();
 	private Object instanceLock = new Object();
 	private Object actionLock = new Object();
 	
 	ListenerList instanceListeners = new ListenerList();
 	ListenerList imageListeners = new ListenerList();
 	
 	public DeltaCloud(String name, String url, String username, String passwd) throws MalformedURLException {
 		this(name, url, username, passwd, null, false, IImageFilter.ALL_STRING, IInstanceFilter.ALL_STRING);
 	}
 
 	public DeltaCloud(String name, String url, String username, String passwd, 
 			String type, boolean persistent) throws MalformedURLException {
		this(name, url, username, passwd, null, persistent, IImageFilter.ALL_STRING, IInstanceFilter.ALL_STRING);
 	}
 
 	public DeltaCloud(String name, String url, String username, String passwd, 
 			String type, boolean persistent, 
 			String imageFilterRules, String instanceFilterRules) throws MalformedURLException {
 		this.client = new DeltaCloudClient(new URL(url), username, passwd); //$NON-NLS-1$
 		this.url = url;
 		this.name = name;
 		this.username = username;
 		this.type = type;
 		imageFilter = new ImageFilter();
 		try {
 			imageFilter.setRules(imageFilterRules);
 		} catch (PatternSyntaxException e) {
 			imageFilter.setRules(IImageFilter.ALL_STRING);
 		}
 		instanceFilter = new InstanceFilter();
 		try {
 			instanceFilter.setRules(instanceFilterRules);
 		} catch (PatternSyntaxException e) {
 			instanceFilter.setRules(IInstanceFilter.ALL_STRING);
 		}
 		if (persistent) {
 			storePassword(url, username, passwd);
 		}
 	}
 	
 	public void editCloud(String name, String url, String username, String passwd, String type) throws MalformedURLException {
 		this.client = new DeltaCloudClient(new URL(url + "/api"), username, passwd); //$NON-NLS-1$
 		this.url = url;
 		this.name = name;
 		this.username = username;
 		this.type = type;
 		storePassword(url, username, passwd);
 	}
 
 	private void storePassword(String url, String username, String passwd) {
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		String key = DeltaCloud.getPreferencesKey(url, username);
 		ISecurePreferences node = root.node(key);
 		try {
 			node.put("password", passwd, true /*encrypt*/);
 		} catch (StorageException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String getPreferencesKey(String url, String username) {
 		String key = "/org/jboss/tools/deltacloud/core/"; //$NON-NLS-1$
 		key += url + "/" + username; //$NON-NLS-1$
 		return EncodingUtils.encodeSlashes(key);
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getURL() {
 		return url;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public String getLastImageId() {
 		return lastImageId;
 	}
 	
 	public void setLastImageId(String lastImageId) {
 		this.lastImageId = lastImageId;
 	}
 	
 	public String getLastKeyname() {
 		return lastKeyname;
 	}
 	
 	public void setLastKeyname(String lastKeyname) {
 		this.lastKeyname = lastKeyname;
 	}
 	
 	public IInstanceFilter getInstanceFilter() {
 		return instanceFilter;
 	}
 	
 	public void createInstanceFilter(String ruleString) {
 		String rules = getInstanceFilter().toString();
 		if (IInstanceFilter.ALL_STRING.equals(ruleString))
 			instanceFilter = new AllInstanceFilter();
 		else {
 			instanceFilter = new InstanceFilter();
 			instanceFilter.setRules(ruleString);
 		}
 		if (!rules.equals(ruleString)) {
 			save();
 			notifyInstanceListListeners(getCurrInstances());
 		}
 	}
 	
 	public IImageFilter getImageFilter() {
 		return imageFilter;
 	}
 	
 	public void createImageFilter(String ruleString) {
 		String rules = getImageFilter().toString();
 		if (IImageFilter.ALL_STRING.equals(ruleString))
 			imageFilter = new AllImageFilter();
 		else {
 			imageFilter = new ImageFilter();
 			imageFilter.setRules(ruleString);
 		}
 		if (!rules.equals(ruleString)) {
 			save();
 			notifyImageListListeners(getCurrImages());
 		}
 	}
 	
 	public void loadChildren() {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				getImages();
 				getInstances();
 			}
 			
 		});
 		t.start();
 	}
 	
 	public void save() {
 		// Currently we have to save all clouds instead of just this one
 		DeltaCloudManager.getDefault().saveClouds();
 	}
 	
 	public void addInstanceListListener(IInstanceListListener listener) {
 		instanceListeners.add(listener);
 	}
 	
 	public void removeInstanceListListener(IInstanceListListener listener) {
 		instanceListeners.remove(listener);
 	}
 
 	public void notifyInstanceListListeners(DeltaCloudInstance[] array) {
 		Object[] listeners = instanceListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i)
 			((IInstanceListListener)listeners[i]).listChanged(this, array);
 	}
 	
 	public void addImageListListener(IImageListListener listener) {
 		imageListeners.add(listener);
 	}
 	
 	public void removeImageListListener(IImageListListener listener) {
 		imageListeners.remove(listener);
 	}
 	
 	public void notifyImageListListeners(DeltaCloudImage[] array) {
 		Object[] listeners = imageListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i)
 			((IImageListListener)listeners[i]).listChanged(this, array);
 	}
 
 	public Job getActionJob(String id) {
 		synchronized (actionLock) {
 			Job j = null;
 			if (actionJobs != null) {
 				return actionJobs.get(id);
 			}
 			return j;
 		}
 	}
 
 	public void registerActionJob(String id, Job j) {
 		synchronized (actionLock) {
 			if (actionJobs == null)
 				actionJobs = new HashMap<String, Job>();
 			actionJobs.put(id, j);
 		}
 	}
 
 	public void removeActionJob(String id, Job j) {
 		synchronized (actionLock) {
 			if (actionJobs != null && actionJobs.get(id) == j)
 				actionJobs.remove(id);
 		}
 	}
 	
 	public DeltaCloudInstance[] getInstances() {
 		synchronized (instanceLock) {
 			instances = new ArrayList<DeltaCloudInstance>();
 			try {
 				List<Instance> list = client.listInstances();
 				for (Iterator<Instance> i = list.iterator(); i.hasNext();) {
 					DeltaCloudInstance instance = new DeltaCloudInstance(i.next());
 					instances.add(instance);
 				}
 			} catch (DeltaCloudClientException e) {
 				Activator.log(e);
 			}
 			DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 			instanceArray = instances.toArray(instanceArray);
 			notifyInstanceListListeners(instanceArray);
 			return instanceArray;
 		}
 	}
 	
 	public DeltaCloudInstance[] getCurrInstances() {
 		synchronized (instanceLock) {
 			if (instances == null)
 				return getInstances();
 			DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 			instanceArray = instances.toArray(instanceArray);
 			return instanceArray;
 		}
 	}
 	
 	public DeltaCloudInstance[] destroyInstance(String instanceId) {
 		try {
 			client.destroyInstance(instanceId);
 			for (int i = 0; i < instances.size(); ++i) {
 				DeltaCloudInstance instance = instances.get(i);
 				if (instance.getId().equals(instanceId)) {
 					instances.remove(i);
 					break;
 				}
 			}
 		} catch (DeltaCloudClientException e) {
 			return null;
 		}
 		DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 		instanceArray = instances.toArray(instanceArray);
 		notifyInstanceListListeners(instanceArray);
 		return instanceArray;
 	}
 
 	public void createKey(String keyname, String keystoreLocation) throws DeltaCloudException {
 		try {
 			client.createKey(keyname, keystoreLocation);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 	
 	public void deleteKey(String keyname) throws DeltaCloudException {
 		try {
 			client.deleteKey(keyname);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	public void addReplaceInstance(DeltaCloudInstance instance) {
 		String instanceId = instance.getId();
 		if (instance != null) {
 			boolean found = false;
 			for (int i = 0; i < instances.size(); ++i) {
 				DeltaCloudInstance inst = instances.get(i);
 				if (inst.getId().equals(instanceId)) {
 					found = true;
 					instances.set(i, instance);
 				}
 			}
 			if (!found) {
 				instances.add(instance);
 			}
 			DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 			instanceArray = instances.toArray(instanceArray);
 			notifyInstanceListListeners(instanceArray);
 		}
 	}
 	
 	public DeltaCloudInstance refreshInstance(String instanceId) {
 		DeltaCloudInstance retVal = null;
 		try {
 			Instance instance = client.listInstances(instanceId);
 			retVal = new DeltaCloudInstance(instance);
 			for (int i = 0; i < instances.size(); ++i) {
 				DeltaCloudInstance inst = instances.get(i);
 				if (inst.getId().equals(instanceId)) {
 					// FIXME: remove BOGUS state when server fixes state problems
 					if (!(retVal.getState().equals(DeltaCloudInstance.BOGUS)) && !(inst.getState().equals(retVal.getState()))) {
 						instances.set(i, retVal);
 						DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 						instanceArray = instances.toArray(instanceArray);
 						notifyInstanceListListeners(instanceArray);
 						return retVal;
 					}
 				}
 			}
 		} catch (DeltaCloudClientException e) {
 			// will get here when a pending instance is being checked
 		}
 		return retVal;
 	}
 	
 	public boolean performInstanceAction(String instanceId, String action) throws DeltaCloudException {
 		try {
 			return client.performInstanceAction(instanceId, action);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	public DeltaCloudHardwareProfile[] getProfiles() {
 		ArrayList<DeltaCloudHardwareProfile> profiles = new ArrayList<DeltaCloudHardwareProfile>();
 		try {
 			List<HardwareProfile> list = client.listProfiles();
 			for (Iterator<HardwareProfile> i = list.iterator(); i.hasNext();) {
 				DeltaCloudHardwareProfile profile = new DeltaCloudHardwareProfile(i.next());
 				profiles.add(profile);
 			}
 		} catch (DeltaCloudClientException e) {
 			Activator.log(e);
 		}
 		DeltaCloudHardwareProfile[] profileArray = new DeltaCloudHardwareProfile[profiles.size()];
 		profileArray = profiles.toArray(profileArray);
 		return profileArray;
 	}
 	
 	public DeltaCloudImage[] getImages() {
 		synchronized (imageLock) {
 			images = new ArrayList<DeltaCloudImage>();
 			try {
 				List<Image> list = client.listImages();
 				for (Iterator<Image> i = list.iterator(); i.hasNext();) {
 					DeltaCloudImage image = new DeltaCloudImage(i.next());
 					images.add(image);
 				}
 			} catch (DeltaCloudClientException e) {
 				Activator.log(e);
 			}
 			DeltaCloudImage[] imageArray = new DeltaCloudImage[images.size()];
 			imageArray = images.toArray(imageArray);
 			notifyImageListListeners(imageArray);
 			return imageArray;
 		}
 	}
 
 	public DeltaCloudImage[] getCurrImages() {
 		synchronized(imageLock) {
 			if (images == null)
 				return getImages();
 			DeltaCloudImage[] imageArray = new DeltaCloudImage[images.size()];
 			imageArray = images.toArray(imageArray);
 			return imageArray;
 		}
 	}
 	
 	public DeltaCloudImage getImage(String imageId) {
 		DeltaCloudImage retVal = null;
 		try {
 			Image image = client.listImages(imageId);
 			retVal = new DeltaCloudImage(image);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// do nothing and return null
 		}
 		return retVal;
 	}
 	
 	public boolean testConnection() {
 		String instanceId = "madeupValue"; //$NON-NLS-1$
 		try {
 			client.listInstances(instanceId);
 			return true;
 		} catch (DeltaCloudAuthException e) {
 			return false;
 		} catch (DeltaCloudClientException e) {
 			return true;
 		}
 	}
 
 	public DeltaCloudRealm[] getRealms() {
 		ArrayList<DeltaCloudRealm> realms = new ArrayList<DeltaCloudRealm>();
 		try {
 			List<Realm> list = client.listRealms();
 			for (Iterator<Realm> i = list.iterator(); i.hasNext();) {
 				DeltaCloudRealm realm = new DeltaCloudRealm(i.next());
 				realms.add(realm);
 			}
 		} catch (DeltaCloudClientException e) {
 			Activator.log(e);
 		}
 		return realms.toArray(new DeltaCloudRealm[realms.size()]);
 	}
 
 	public DeltaCloudInstance createInstance(String name, String imageId, String realmId, String profileId,
 			String keyname, String memory, String storage) throws DeltaCloudException {
 		try {
 			Instance instance = null;
 			if (keyname != null) {
 				instance = client.createInstance(imageId, profileId, realmId, name, keyname, memory, storage);
 			} else {
 				instance = client.createInstance(imageId, profileId, realmId, name, memory, storage);
 			}
 			if (instance != null) {
 				DeltaCloudInstance newInstance = new DeltaCloudInstance(instance);
 				newInstance.setGivenName(name);
 				instances.add(newInstance);
 				DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 				instanceArray = instances.toArray(instanceArray);
 				notifyInstanceListListeners(instanceArray);
 				return newInstance;
 			}
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 		return null;
 	}
 }
