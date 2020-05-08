 /**
  * Copyright (c) 2012 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  */
 package org.modelversioning.emfprofile.application.registry.internal;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.modelversioning.emfprofile.IProfileFacade;
 import org.modelversioning.emfprofile.Profile;
 import org.modelversioning.emfprofile.Stereotype;
 import org.modelversioning.emfprofile.application.registry.ProfileApplicationDecorator;
 import org.modelversioning.emfprofile.impl.ProfileFacadeImpl;
 import org.modelversioning.emfprofileapplication.ProfileApplication;
 import org.modelversioning.emfprofileapplication.ProfileImport;
 import org.modelversioning.emfprofileapplication.StereotypeApplicability;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 import org.modelversioning.emfprofileapplication.impl.ProfileApplicationImpl;
 
 /**
  * @author <a href="mailto:becirb@gmail.com">Becir Basic</a>
  * 
  */
 public class ProfileApplicationDecoratorImpl extends ProfileApplicationImpl
 		implements ProfileApplicationDecorator {
 
 	private final ResourceSet resourceSet;
 	private final IProfileFacade facade;
 	private final ProfileApplication profileApplication;
 	private final IFile profileApplicationFile;
 	private boolean dirty = false;
 	private final Collection<Profile> profiles;
 
 	/**
 	 * Creates new profiles application which will be saved into file. At the
 	 * current state of implementation there will be only one profiles
 	 * application file pro applied profiles.
 	 * 
 	 * @param profileApplicationFile
 	 * @param profiles
 	 * @param resourceSet
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	public ProfileApplicationDecoratorImpl(IFile profileApplicationFile,
 			Collection<Profile> profiles, ResourceSet resourceSet)
 			throws CoreException, IOException {
 		this.profileApplicationFile = profileApplicationFile;
 		this.profiles = profiles;
 		this.resourceSet = resourceSet;
 		this.facade = createAndInitializeProfileFacade(profileApplicationFile,
 				profiles);
 		this.dirty = true;
 		this.profileApplication = facade
 				.findOrCreateProfileApplication(profiles.iterator().next());
 	}
 
 	/**
 	 * Loads a profiles application from file.
 	 * 
 	 * @param profileApplicationFile
 	 * @param resourceSet
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	public ProfileApplicationDecoratorImpl(IFile profileApplicationFile,
 			ResourceSet resourceSet) throws CoreException, IOException {
 		this.profileApplicationFile = profileApplicationFile;
 		this.resourceSet = resourceSet;
 		this.facade = loadProfileApplication(profileApplicationFile);
 		this.profiles = facade.getLoadedProfiles();
 		this.dirty = false;
 		if(facade.getProfileApplications().isEmpty())
 			throw new IOException("The file: " + profileApplicationFile.getName() + ", does not contain any profile applications.");
 		this.profileApplication = facade.getProfileApplications().get(0);
 	}
 
 	@Override
 	public boolean isDirty() {
 		return this.dirty;
 	}
 	
 	@Override
 	public void setDirty(boolean dirty){
 		this.dirty = dirty;
 	}
 
 	public IFile getProfileApplicationFile() {
 		return profileApplicationFile;
 	}
 
 	/**
 	 * Creates new profiles application
 	 * 
 	 * @param profileApplicationFile
 	 * @param profiles
 	 * @return
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private IProfileFacade createAndInitializeProfileFacade(
 			IFile profileApplicationFile, Collection<Profile> profiles)
 			throws CoreException, IOException {
 
 		IProfileFacade facade = createNewProfileFacade(profileApplicationFile);
 		for (Profile profile : profiles) {
 			facade.loadProfile(profile);
 		}
 		profileApplicationFile.refreshLocal(IFile.DEPTH_ZERO, new NullProgressMonitor());
 		return facade;
 	}
 
 	/**
 	 * Loads an existing profiles application.
 	 * 
 	 * @param workbenchPart
 	 *            to use.
 	 * @param profileApplicationFile
 	 *            to load.
 	 */
 	/**
 	 * Loads an existing profiles application.
 	 * 
 	 * @param profileApplicationFile
 	 *            to load.
 	 * @return
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private IProfileFacade loadProfileApplication(IFile profileApplicationFile)
 			throws CoreException, IOException {
 		profileApplicationFile.refreshLocal(IFile.DEPTH_ONE,
 				new NullProgressMonitor());
 		IProfileFacade facade = createNewProfileFacade(profileApplicationFile);
 		
 		return facade;
 	}
 
 	/**
 	 * Creates new instance of {@link IProfileFacade}
 	 * 
 	 * @param profileApplicationFile
 	 * @return
 	 * @throws IOException
 	 */
 	private IProfileFacade createNewProfileFacade(IFile profileApplicationFile)
 			throws IOException {
 		IProfileFacade facade = new ProfileFacadeImpl();
 		facade.setProfileApplicationFileAndInitializeResource(profileApplicationFile, resourceSet);
 		return facade;
 	}
 
 
 
 	@Override
 	public String getName() {
 		String result = "";
 		Collection<Profile> profiles = facade.getLoadedProfiles();
 		Iterator<Profile> iter = profiles.iterator();
 		while (iter.hasNext()) {
 			result += iter.next().getName();
 			if (iter.hasNext())
 				result += ", ";
 		}
 		return result
 				+ " - "
 				+ profileApplicationFile.getLocation().
 				makeRelativeTo(ResourcesPlugin.getWorkspace().getRoot().getLocation()).toString();
 	}
 	
 	@Override
 	public String getProfileName() {
 		return profiles.iterator().next().getName();
 	}
 
 
 	public void unload() {
 		facade.unloadProfile(null);
 		facade.unload();
 	}
 
 	@Override
 	public void save() throws IOException, CoreException {
 		facade.save();
 		dirty = false;
 	}
 
 	@Override
 	public Collection<? extends StereotypeApplicability> getApplicableStereotypes(
 			EObject eObject) {

		return facade.getApplicableStereotypes(eObject.eClass());
 	}
 
 	@Override
 	public StereotypeApplication applyStereotype(
 		StereotypeApplicability stereotypeApplicability, EObject eObject) {
 		StereotypeApplication result = facade.apply(
 				stereotypeApplicability, eObject);
 		dirty = true;
 		return result;
 	}
 	
 	@Override
 	public void addNestedEObject(EObject container, EReference eReference,
 			EObject eObject) {
 		facade.addNestedEObject(container, eReference, eObject);
 		dirty = true;
 	}
 	
 	@Override
 	public void removeEObject(EObject eObject) {
 		dirty = true;
 		facade.removeEObject(eObject);
 	}
 
 	@Override
 	public EList<StereotypeApplication> getStereotypeApplications() {
 		return profileApplication.getStereotypeApplications();
 	}
 
 	@Override
 	public EList<ProfileImport> getImportedProfiles() {
 		return profileApplication.getImportedProfiles();
 	}
 
 	@Override
 	public EList<StereotypeApplication> getStereotypeApplications(
 			EObject eObject) {
 		return profileApplication.getStereotypeApplications(eObject);
 	}
 
 	@Override
 	public EList<StereotypeApplication> getStereotypeApplications(
 			EObject eObject, Stereotype stereotype) {
 		return profileApplication
 				.getStereotypeApplications(eObject, stereotype);
 	}
 
 	@Override
 	public EList<EObject> getAnnotatedObjects() {
 		return profileApplication.getAnnotatedObjects();
 	}
 
 
 	@Override
 	public EClass eClass() {
 		return profileApplication.eClass();
 	}
 
 	@Override
 	public Resource eResource() {
 		return profileApplication.eResource();
 	}
 
 	@Override
 	public EObject eContainer() {
 		return profileApplication.eContainer();
 	}
 
 	@Override
 	public EStructuralFeature eContainingFeature() {
 		return profileApplication.eContainingFeature();
 	}
 
 	@Override
 	public EReference eContainmentFeature() {
 		return profileApplication.eContainmentFeature();
 	}
 
 	@Override
 	public EList<EObject> eContents() {
 		return profileApplication.eContents();
 	}
 
 	@Override
 	public TreeIterator<EObject> eAllContents() {
 		return profileApplication.eAllContents();
 	}
 
 	@Override
 	public boolean eIsProxy() {
 		return profileApplication.eIsProxy();
 	}
 
 	@Override
 	public EList<EObject> eCrossReferences() {
 		return profileApplication.eCrossReferences();
 	}
 
 	@Override
 	public Object eGet(EStructuralFeature feature) {
 		return profileApplication.eGet(feature);
 	}
 
 	@Override
 	public Object eGet(EStructuralFeature feature, boolean resolve) {
 		return profileApplication.eGet(feature, resolve);
 	}
 
 	@Override
 	public void eSet(EStructuralFeature feature, Object newValue) {
 		profileApplication.eSet(feature, newValue);
 	}
 
 	@Override
 	public boolean eIsSet(EStructuralFeature feature) {
 		return profileApplication.eIsSet(feature);
 	}
 
 	@Override
 	public void eUnset(EStructuralFeature feature) {
 		profileApplication.eUnset(feature);
 	}
 
 	@Override
 	public Object eInvoke(EOperation operation, EList<?> arguments)
 			throws InvocationTargetException {
 		return profileApplication.eInvoke(operation, arguments);
 	}
 
 	@Override
 	public EList<Adapter> eAdapters() {
 		return profileApplication.eAdapters();
 	}
 
 	@Override
 	public boolean eDeliver() {
 		return profileApplication.eDeliver();
 	}
 
 	@Override
 	public void eSetDeliver(boolean deliver) {
 		profileApplication.eSetDeliver(deliver);
 	}
 
 	@Override
 	public void eNotify(Notification notification) {
 		profileApplication.eNotify(notification);
 	}
 	
 	public ProfileApplication getProfileApplication(){
 		return profileApplication;
 	}
 	
 //	@Override
 //	public boolean equals(Object obj) {
 //		return profileApplication.equals(obj);
 //	}
 //	
 //	@Override
 //	public int hashCode() {
 //		return profileApplication.hashCode();
 //	}
 }
