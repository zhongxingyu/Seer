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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.jboss.tools.deltacloud.core.client.API.Driver;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudAuthClientException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientImpl;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudNotFoundClientException;
 import org.jboss.tools.deltacloud.core.client.HardwareProfile;
 import org.jboss.tools.deltacloud.core.client.Image;
 import org.jboss.tools.deltacloud.core.client.Instance;
 import org.jboss.tools.deltacloud.core.client.InternalDeltaCloudClient;
 import org.jboss.tools.deltacloud.core.client.Key;
 import org.jboss.tools.deltacloud.core.client.Realm;
 import org.jboss.tools.internal.deltacloud.core.observable.ObservablePojo;
 
 /**
  * @author Jeff Jonston
  * @author André Dietisheim
  */
 public class DeltaCloud extends ObservablePojo {
 
 	public static final String PROP_INSTANCES = "instances";
 	public static final String PROP_INSTANCES_REMOVED = "instancesRemoved";
 	public static final String PROP_IMAGES = "images";
 	public static final String PROP_NAME = "name";
 
 	private String name;
 	private String username;
 	private String url;
 	private DeltaCloudDriver driver;
 	private String lastKeyname = "";
 	private String lastImageId = "";
 
 	private InternalDeltaCloudClient client;
 
 	private DeltaCloudImagesRepository imagesRepo;
 	private DeltaCloudInstancesRepository instanceRepo;
 
 	private IImageFilter imageFilter;
 	private IInstanceFilter instanceFilter;
 
 	private SecurePasswordStore passwordStore;
 	private Collection<IInstanceAliasMapping> instanceAliasMappings;
 
 	public static interface IInstanceStateMatcher {
 		public boolean matchesState(DeltaCloudInstance instance, DeltaCloudInstance.State instanceState);
 	}
 
 	public DeltaCloud(String name, String url, String username, String passwd) throws DeltaCloudException {
 		this(name, url, username, passwd, null);
 	}
 
 	public DeltaCloud(String name, String url, String username, String password, DeltaCloudDriver driver)
 			throws DeltaCloudException {
 		this(name, url, username, password, driver, IImageFilter.ALL_STRING, IInstanceFilter.ALL_STRING, new ArrayList<IInstanceAliasMapping>());
 	}
 
 	public DeltaCloud(String name, String url, String username, DeltaCloudDriver driver, String imageFilterRules,
 			String instanceFilterRules, Collection<IInstanceAliasMapping> instanceAliasMappings)
 			throws DeltaCloudException {
 		this(name, url, username, null, driver, imageFilterRules, instanceFilterRules, instanceAliasMappings);
 	}
 
 	public DeltaCloud(String name, String url, String username, String password, DeltaCloudDriver driver,
 			String imageFilterRules, String instanceFilterRules, Collection<IInstanceAliasMapping> instanceAliasMappings)
 			throws DeltaCloudException {
 		this.url = url;
 		this.name = name;
 		this.username = username;
 		this.driver = driver;
 		this.passwordStore = createSecurePasswordStore(name, username, password);
 		this.client = createClient(url, username, passwordStore.getPassword());
 		this.imageFilter = createImageFilter(imageFilterRules);
 		this.instanceFilter = createInstanceFilter(instanceFilterRules);
 		this.instanceAliasMappings = instanceAliasMappings;
 	}
 
 	public void update(String name, String url, String username, String password, DeltaCloudDriver driver)
 			throws DeltaCloudException {
 		this.driver = driver;
 
 		boolean nameChanged = updateName(name);
 		boolean connectionPropertiesChanged = updateConnectionProperties(url, username, password);
 
 		if (connectionPropertiesChanged) {
 			client = createClient(url, username, password);
 			loadChildren();
 		}
 
 		if (nameChanged || connectionPropertiesChanged) {
 			this.passwordStore.update(new DeltaCloudPasswordStorageKey(name, username), password);
 			// TODO: move to notification based approach
 			DeltaCloudManager.getDefault().saveClouds();
 		}
 
 	}
 
 	private boolean updateName(String name) {
 		if (equals(this.name, name)) {
 			return false;
 		}
 
 		setName(name);
 		return true;
 	}
 
 	private boolean updateConnectionProperties(String url, String username, String password) throws DeltaCloudException {
 		boolean changed = false;
 		if (!equals(this.url, url)) {
 			this.url = url;
 			changed = true;
 		}
 		if (!equals(this.username, username)) {
 			this.username = username;
 			changed = true;
 		}
 		if (!equals(this.passwordStore.getPassword(), password)) {
 			changed = true;
 		}
 
 		return changed;
 	}
 
 	private boolean equals(Object thisObject, Object thatObject) {
 		return (thisObject != null && thisObject.equals(thatObject))
 				|| (thatObject != null && thatObject.equals(thisObject))
 				|| (thisObject == null && thatObject == null);
 	}
 
 	protected SecurePasswordStore createSecurePasswordStore(String name2, String username2, String password) {
 		return new SecurePasswordStore(new DeltaCloudPasswordStorageKey(name, username), password);
 	}
 
 	protected InternalDeltaCloudClient createClient(String url, String username, String password)
 			throws DeltaCloudException {
 		try {
 			return new DeltaCloudClientImpl(url, username, password);
 		} catch (Exception e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not access cloud at {0}", url), e);
 		}
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
 
 	public String getPassword() throws DeltaCloudException {
 		return passwordStore.getPassword();
 	}
 
 	public DeltaCloudDriver getDriver() {
 		return driver;
 	}
 
 	public boolean isValid() {
 		return driver != null
 				&& driver != DeltaCloudDriver.UNKNOWN;
 	}
 
 	public String getLastImageId() {
 		return lastImageId;
 	}
 
 	public DeltaCloudImage getLastImage() throws DeltaCloudException {
 		return getImage(lastImageId);
 	}
 
 	public void setLastImageId(String lastImageId) {
 		this.lastImageId = lastImageId;
 	}
 
 	private void setName(String name) {
 		firePropertyChange(PROP_NAME, this.name, this.name = name);
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
 
 	public void updateInstanceFilter(String ruleString) throws Exception {
 		String rules = getInstanceFilter().toString();
 		instanceFilter = createInstanceFilter(ruleString);
 		if (!rules.equals(ruleString)) {
 			// TODO: remove notification with all instanceRepo, replace by
 			// notifying the changed instance
 			firePropertyChange(
 					PROP_INSTANCES, getInstancesRepository().get(), getInstancesRepository().get());
 			DeltaCloudManager.getDefault().saveClouds();
 		}
 	}
 
 	private IInstanceFilter createInstanceFilter(String ruleString) {
 		IInstanceFilter instanceFilter = null;
 		if (IInstanceFilter.ALL_STRING.equals(ruleString)) {
 			instanceFilter = new AllInstanceFilter(this);
 		} else {
 			try {
 				instanceFilter = new InstanceFilter(this);
 				instanceFilter.setRules(ruleString);
 			} catch (PatternSyntaxException e) {
 				instanceFilter.setRules(IInstanceFilter.ALL_STRING);
 			}
 		}
 		return instanceFilter;
 	}
 
 	public IImageFilter getImageFilter() {
 		return imageFilter;
 	}
 
 	public void updateImageFilter(String ruleString) throws Exception {
 		String rules = getImageFilter().toString();
 		this.imageFilter = createImageFilter(ruleString);
 		if (!rules.equals(ruleString)) {
 			// TODO: remove notification with all instanceRepo, replace by
 			// notifying the changed instance
 			firePropertyChange(PROP_IMAGES, getImagesRepository().get(), getImagesRepository().get());
 			// TODO: move to notification based approach
 			DeltaCloudManager.getDefault().saveClouds();
 		}
 	}
 
 	private IImageFilter createImageFilter(String ruleString) {
 		IImageFilter imageFilter = null;
 		if (IImageFilter.ALL_STRING.equals(ruleString)) {
 			imageFilter = new AllImageFilter(this);
 		} else {
 			try {
 				imageFilter = new ImageFilter(this);
 				imageFilter.setRules(ruleString);
 			} catch (PatternSyntaxException e) {
 				imageFilter.setRules(IImageFilter.ALL_STRING);
 			}
 		}
 		return imageFilter;
 	}
 
 	/**
 	 * Loads all children of this delta cloud instance (regardless if things
 	 * have already been loaded before). Catched and collects individual errors
 	 * that may occur and throws a multi exception.
 	 * 
 	 * @throws DeltaCloudException
 	 */
 	public void loadChildren() throws DeltaCloudException {
 		DeltaCloudMultiException multiException = new DeltaCloudMultiException(
 				MessageFormat.format("Could not load children of cloud {0}", getName()));
 		clearImages();
 		clearInstances();
 		try {
 			loadImages();
 		} catch (DeltaCloudException e) {
 			multiException.addError(e);
 		}
 		try {
 			loadInstances();
 		} catch (DeltaCloudException e) {
 			multiException.addError(e);
 		}
 
 		if (!multiException.isEmpty()) {
 			throw multiException;
 		}
 	}
 
 	public DeltaCloudInstance waitForState(String instanceId, final DeltaCloudInstance.State expectedState,
 			IProgressMonitor pm)
 			throws InterruptedException, DeltaCloudException {
 		IInstanceStateMatcher stateMatcher = new IInstanceStateMatcher() {
 
 			@Override
 			public boolean matchesState(DeltaCloudInstance instance, DeltaCloudInstance.State instanceState) {
 				return expectedState != null
 						&& expectedState.equals(instanceState);
 			}
 		};
 		return waitForState(instanceId, stateMatcher, pm);
 	}
 
 	public DeltaCloudInstance waitForState(String instanceId, IInstanceStateMatcher stateMatcher, IProgressMonitor pm)
 			throws InterruptedException, DeltaCloudException {
 		DeltaCloudInstance instance = getInstancesRepository().getById(instanceId);
 		if (instance != null) {
 			while (!pm.isCanceled()) {
 				if (stateMatcher.matchesState(instance, instance.getState())
 						|| instance.getState().equals(DeltaCloudInstance.State.TERMINATED)) {
 					return instance;
 				}
 				Thread.sleep(400);
 				instance = refreshInstance(instance);
 			}
 		}
 		return instance;
 	}
 
 	/**
 	 * Loads the instanceRepo from the server and stores them in this instance.
 	 * Furthermore listeners get informed.
 	 * 
 	 * @return the instanceRepo
 	 * @throws DeltaCloudException
 	 * 
 	 * @see #notifyInstanceListListeners(DeltaCloudInstance[])
 	 */
 	public void loadInstances() throws DeltaCloudException {
 		try {
 			clearInstances();
 			DeltaCloudInstancesRepository repo = getInstancesRepository();
 			DeltaCloudInstance[] oldInstances = repo.get();
 			List<Instance> instances = client.listInstances();
 			Collection<DeltaCloudInstance> deltaCloudInstances =
 					DeltaCloudInstanceFactory.create(instances, this, instanceAliasMappings);
 			repo.add(deltaCloudInstances);
 			// TODO: remove notification with all instanceRepo, replace by
 			// notifying the changed instance
 			firePropertyChange(PROP_INSTANCES, oldInstances, repo.get());
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(MessageFormat.format(
 					"Could not load instanceRepo of cloud {0}: {1}", getName(), e.getMessage()), e);
 		}
 	}
 
 	private void clearImages() {
 		if (imagesRepo != null) {
 			// TODO: remove notification with all instanceRepo, replace by
 			// notifying the changed instance
 			firePropertyChange(PROP_IMAGES, imagesRepo.get(), imagesRepo.clear());
 		}
 	}
 
 	public boolean imagesLoaded() {
 		return imagesRepo == null ? false : true;
 	}
 
 	public boolean instancesLoaded() {
 		return instanceRepo == null ? false : true;
 	}
 
 	private void clearInstances() {
 		// TODO: remove notification with all instanceRepo, replace by
 		// notifying the changed instance
 		firePropertyChange(PROP_INSTANCES, getInstancesRepository().get(), getInstancesRepository().clear());
 	}
 
 	private DeltaCloudInstancesRepository getInstancesRepository() {
 		if (instanceRepo == null) {
 			instanceRepo = new DeltaCloudInstancesRepository();
 		}
 		return instanceRepo;
 	}
 
 	/**
 	 * Gets the instanceRepo in async manner. The method does not return the
 	 * instanceRepo but notifies observers of the instanceRepo.
 	 * 
 	 * @throws DeltaCloudException
 	 */
 	public DeltaCloudInstance[] getInstances() throws DeltaCloudException {
 		if (instanceRepo == null) {
 			loadInstances();
 		}
 		return instanceRepo.get();
 	}
 
 	private DeltaCloudImagesRepository getImagesRepository() {
 		if (imagesRepo == null) {
 			imagesRepo = new DeltaCloudImagesRepository();
 		}
 		return imagesRepo;
 	}
 
 	public DeltaCloudImage[] getImages() throws DeltaCloudException {
 		if (imagesRepo == null) {
 			loadImages();
 		}
 		return imagesRepo.get();
 	}
 
 	/**
 	 * Gets an image for the given image id. In a first step, the local cache is
 	 * queried and if no image is found, the server is queried.
 	 * 
 	 * @param id
 	 *            the image id to match
 	 * @return the image that has the given id
 	 * @throws DeltaCloudException
 	 */
 	public DeltaCloudImage getImage(String id) throws DeltaCloudException {
 		DeltaCloudImage deltaCloudImage = getImagesRepository().getById(id);
 		if (deltaCloudImage == null) {
 			try {
 				Image image = client.listImages(id);
 				deltaCloudImage = DeltaCloudImageFactory.create(image, this);
 				imagesRepo.add(deltaCloudImage);
 			} catch (DeltaCloudClientException e) {
 				throw new DeltaCloudException(MessageFormat.format("Cloud not find image with id \"{0}\"", id), e);
 			}
 		}
 
 		return deltaCloudImage;
 	}
 
 	public DeltaCloudKey[] getKeys() throws DeltaCloudException {
 		List<DeltaCloudKey> keys = new ArrayList<DeltaCloudKey>();
 		try {
 			for (Key key : client.listKeys()) {
 				DeltaCloudKey deltaCloudKey = new DeltaCloudKey(key, this);
 				keys.add(deltaCloudKey);
 			}
 			return keys.toArray(new DeltaCloudKey[] {});
 		} catch (DeltaCloudClientException e) {
 			// TODO: internationalize strings
 			throw new DeltaCloudException(MessageFormat.format("Cloud not get keys from cloud \"{0}\"", getName()), e);
 		}
 	}
 
 	public DeltaCloudKey getKey(String keyId) throws DeltaCloudException {
 		try {
 			Key key = client.listKey(keyId);
 			return new DeltaCloudKey(key, this);
 		} catch (DeltaCloudClientException e) {
 			// TODO: internationalize strings
 			throw new DeltaCloudException(MessageFormat.format("Could not get key \"{0}\" from cloud \"{1}\"", keyId,
 					getName()), e);
 		}
 	}
 
 	public DeltaCloudKey createKey(String id) throws DeltaCloudException {
 		try {
 			Key key = client.createKey(id);
 			return new DeltaCloudKey(key, this);
 		} catch (DeltaCloudClientException e) {
 			// TODO: internationalize strings
 			throw new DeltaCloudException(
 					MessageFormat.format("Could not create key \"{0}\" on cloud \"{1}\"", id, getName()), e);
 		}
 	}
 
 	public void deleteKey(String keyname) throws DeltaCloudException {
 		try {
 			client.deleteKey(keyname);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	private DeltaCloudInstance refreshInstance(DeltaCloudInstance deltaCloudInstance) throws DeltaCloudException {
 		try {
 			DeltaCloudInstance[] instances = getInstancesRepository().get();
 			Instance newInstance = client.listInstances(deltaCloudInstance.getId());
 			deltaCloudInstance.setInstance(newInstance);
 			firePropertyChange(PROP_INSTANCES, instances, getInstancesRepository().get());
 			return deltaCloudInstance;
 		} catch (DeltaCloudClientException e) {
 			// TODO : internationalize strings
 			throw new DeltaCloudException(MessageFormat.format("Coud not refresh instance \"{0}\"",
 					deltaCloudInstance.getId()), e);
 		}
 	}
 
 	public boolean performInstanceAction(String instanceId, DeltaCloudInstance.Action action)
 			throws DeltaCloudException {
 		return performInstanceAction(getInstancesRepository().getById(instanceId), action);
 	}
 
 	protected boolean performInstanceAction(DeltaCloudInstance instance, DeltaCloudInstance.Action action)
 			throws DeltaCloudException {
 		try {
 			if (instance == null) {
 				return false;
 			}
 			DeltaCloudInstancesRepository repo = getInstancesRepository();
 			DeltaCloudInstance[] instances = repo.get();
 			boolean result = instance.performInstanceAction(action, client);
 			if (result) {
 				if (DeltaCloudInstance.Action.DESTROY.equals(action)) {
 					repo.remove(instance);
 					firePropertyChange(PROP_INSTANCES_REMOVED, null, instance);
 				}
 				// TODO: remove notification with all instanceRepo, replace by
 				// notifying the changed instance
 				firePropertyChange(PROP_INSTANCES, instances, repo.get());
 			}
 			return result;
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	public DeltaCloudHardwareProfile[] getProfiles() throws DeltaCloudException {
 		ArrayList<DeltaCloudHardwareProfile> profiles = new ArrayList<DeltaCloudHardwareProfile>();
 		try {
 			List<HardwareProfile> list = client.listProfiles();
 			for (Iterator<HardwareProfile> i = list.iterator(); i.hasNext();) {
 				DeltaCloudHardwareProfile profile = new DeltaCloudHardwareProfile(i.next());
 				profiles.add(profile);
 			}
 			return profiles.toArray(new DeltaCloudHardwareProfile[profiles.size()]);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not list profiles on cloud {0}", name), e);
 		}
 	}
 
 	/**
 	 * Loads the available images from the server and stores them locally.
 	 * Furthermore listeners get informed.
 	 * 
 	 * @return the delta cloud image[]
 	 * @throws DeltaCloudClientException
 	 * 
 	 * @see #notifyImageListListeners(DeltaCloudImage[])
 	 */
 	public void loadImages() throws DeltaCloudException {
 		try {
 			clearImages();
 			DeltaCloudImagesRepository repo = getImagesRepository();
 			DeltaCloudImage[] oldImages = repo.get();
 			Collection<DeltaCloudImage> deltaCloudImages = DeltaCloudImageFactory.create(client.listImages(), this);
 			repo.add(deltaCloudImages);
 			// TODO: remove notification with all instanceRepo, replace by
 			// notifying the changed instance
 			firePropertyChange(PROP_IMAGES, oldImages, repo.get());
 		} catch (DeltaCloudClientException e) {
 			clearImages();
 			throw new DeltaCloudException(
 					MessageFormat.format("Could not load images of cloud {0}: {1}", getName(), e.getMessage()), e);
 		}
 
 	}
 
 	public DeltaCloudRealm[] getRealms() throws DeltaCloudException {
 		ArrayList<DeltaCloudRealm> realms = new ArrayList<DeltaCloudRealm>();
 		try {
 			List<Realm> list = client.listRealms();
 			for (Iterator<Realm> i = list.iterator(); i.hasNext();) {
 				DeltaCloudRealm realm = new DeltaCloudRealm(i.next());
 				realms.add(realm);
 			}
 			return realms.toArray(new DeltaCloudRealm[realms.size()]);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not get realms for cloud {0}", name), e);
 		}
 	}
 
 	public DeltaCloudInstance createInstance(String name, String imageId, String realmId, String profileId,
 			String keyId, String memory, String storage) throws DeltaCloudException {
 		try {
 			Instance instance = null;
 			if (keyId != null) {
 				instance = client.createInstance(name, imageId, profileId, realmId, keyId, memory, storage);
 			} else {
 				instance = client.createInstance(name, imageId, profileId, realmId, memory, storage);
 			}
 			if (instance != null) {
 				DeltaCloudInstancesRepository repo = getInstancesRepository();
 				DeltaCloudInstance[] instances = repo.get();
 				DeltaCloudInstance deltaCloudInstance = DeltaCloudInstanceFactory.create(instance, this, name);
 				instanceAliasMappings.add(new InstanceAliasMapping(instance.getId(), name));
 				repo.add(deltaCloudInstance);
 				// TODO: remove notification with all instanceRepo, replace by
 				// notifying the changed instance
 				firePropertyChange(PROP_INSTANCES, instances, repo.get());
 				// TODO: move to notification based approach
 				DeltaCloudManager.getDefault().saveClouds();
 				return deltaCloudInstance;
 			}
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 		return null;
 	}
 
 	public void dispose() throws DeltaCloudException {
 		passwordStore.remove();
 	}
 
 	public String toString() {
 		return name;
 	}
 
 	public static DeltaCloudDriver getServerDriver(String url) throws DeltaCloudException {
 		try {
 			Driver driver = new DeltaCloudClientImpl(url).getServerType();
 			return DeltaCloudDriver.valueOf(driver);
 		} catch (Exception e) {
 			// TODO internationalize strings
 			throw new DeltaCloudException(
 					"Could not determine the driver of the server on url " + url, e);
 		}
 	}
 
 	/**
 	 * Tests the credentials defined in this DeltaCloud instance by connecting
 	 * to the server defined by the url in this instance. Returns
 	 * <code>true</code> if the credentials are valid, <code>false</code>
 	 * otherwise.
 	 * 
 	 * @return <code>true</code>, if successful
 	 * @throws DeltaCloudClientException
 	 *             if any other error occurs while trying to connect to the
 	 *             server
 	 */
 	public boolean testCredentials() throws DeltaCloudException {
 		String instanceId = "nonexistingInstance"; //$NON-NLS-1$
 		try {
 			client.listInstances(instanceId);
 			return true;
 		} catch (DeltaCloudNotFoundClientException e) {
 			return true;
 		} catch (DeltaCloudAuthClientException e) {
 			return false;
 		} catch (DeltaCloudClientException e) {
			throw new DeltaCloudException(MessageFormat.format("Could not connection to cloud \"{0}\" at \"{1}\"",
 					name, url), e);
 		}
 
 	}
 }
