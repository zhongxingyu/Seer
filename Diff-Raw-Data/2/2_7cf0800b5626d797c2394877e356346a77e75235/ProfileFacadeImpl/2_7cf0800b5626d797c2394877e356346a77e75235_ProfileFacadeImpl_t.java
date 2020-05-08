 /**
  * <copyright>
  *
  * Copyright (c) 2010 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * </copyright>
  */
 
 package org.modelversioning.emfprofile.impl;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.ECollections;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.modelversioning.emfprofile.EMFProfilePlugin;
 import org.modelversioning.emfprofile.Extension;
 import org.modelversioning.emfprofile.IProfileFacade;
 import org.modelversioning.emfprofile.Profile;
 import org.modelversioning.emfprofile.Stereotype;
 import org.modelversioning.emfprofileapplication.ProfileApplication;
 import org.modelversioning.emfprofileapplication.ProfileImport;
 import org.modelversioning.emfprofileapplication.StereotypeApplicability;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 import org.modelversioning.emfprofileapplication.util.ProfileImportResolver;
 
 /**
  * Implements the {@link IProfileFacade}.
  * 
  * @author <a href="mailto:langer@big.tuwien.ac.at">Philip Langer</a>
  * 
  */
 public class ProfileFacadeImpl implements IProfileFacade {
 
 	private static final String STEREOTYPE_NOT_APPLICABLE = "Stereotype is not applicable to the object.";
 	private static final String STEREOTYPE_APP_RESOURCE_ERROR = "Specified resource for the "
 			+ "stereotype application is not set, null, or unloaded.";
 	/**
 	 * Currently loaded profiles.
 	 */
 	private EList<Profile> profiles = new BasicEList<Profile>();
 	/**
 	 * Currently loaded profile application resource.
 	 */
 	private Resource profileApplicationResource;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void makeApplicable(Profile profile) {
 		for (Stereotype stereotype : profile.getStereotypes()) {
 			if (!stereotype.getESuperTypes().contains(
 					STEREOTYPE_APPLICATION_ECLASS)) {
 				stereotype.getESuperTypes().add(STEREOTYPE_APPLICATION_ECLASS);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void save() throws IOException {
 		if (profileApplicationResource != null) {
 			if (requireTransaction()) {
 				TransactionalEditingDomain domain = getTransactionalEditingDomain();
 				doProfileApplicationResourceSave();
 				((BasicCommandStack) domain.getCommandStack()).saveIsDone();
 			} else {
 				doProfileApplicationResourceSave();
 			}
 		}
 	}
 
 	private void doProfileApplicationResourceSave() {
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
 				Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		new WorkspaceJob("Saving Profile Application") {
 			@Override
 			public IStatus runInWorkspace(IProgressMonitor monitor)
 					throws CoreException {
 				try {
 					profileApplicationResource.save(saveOptions);					
 				} catch (IOException e) {
 					return new Status(IStatus.ERROR, EMFProfilePlugin.ID,
 							e.getMessage(), e);
 				}
 				return new Status(IStatus.OK, EMFProfilePlugin.ID, "OK");
 			}
 		}.schedule();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void loadProfile(Profile profile) {
 		this.profiles.add(profile);
 		if (profileApplicationResource != null) {
 			profileApplicationResource.getResourceSet().getPackageRegistry()
 					.put(profile.getNsURI(), profile);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void unloadProfile(Profile profile) {
 		this.profiles.remove(profile);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void loadProfiles(Resource resource) {
 		for (EObject eObject : resource.getContents()) {
 			if (eObject instanceof Profile) {
 				this.loadProfile((Profile) eObject);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void loadProfiles(EList<Profile> profiles) {
 		this.profiles.addAll(profiles);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<Profile> getLoadedProfiles() {
 		return ECollections.unmodifiableEList(profiles);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws IOException
 	 */
 	@Override
 	public void setProfileApplicationResource(Resource resource)
 			throws IOException {
 		profileApplicationResource = resource;
 		if (!profileApplicationResource.isLoaded()) {
 			profileApplicationResource.load(Collections.emptyMap());
 		}
 
 		EcoreUtil.resolveAll(profileApplicationResource);
 
 		// resolve profile imports
 		EList<ProfileApplication> profileApplications = getProfileApplications(profileApplicationResource);
 		for (ProfileApplication application : profileApplications) {
 			for (final ProfileImport profileImport : application
 					.getImportedProfiles()) {
 				if (requireTransaction()) {
 					TransactionalEditingDomain domain = getTransactionalEditingDomain();
 					domain.getCommandStack().execute(
 							new RecordingCommand(domain) {
 								@Override
 								protected void doExecute() {
 									ProfileImportResolver.resolve(
 											profileImport,
 											profileApplicationResource
 													.getResourceSet());
 								}
 							});
 				} else {
 					ProfileImportResolver.resolve(profileImport,
 							profileApplicationResource.getResourceSet());
 				}
 				profiles.add(profileImport.getProfile());
 			}
 		}
 
 		for (Profile profile : this.profiles) {
 			this.profileApplicationResource.getResourceSet()
 					.getPackageRegistry().put(profile.getNsURI(), profile);
 		}
 	}
 
 	/**
 	 * Returns a list of {@link ProfileApplication ProfileApplications}
 	 * contained by the specified <code>resource</code>.
 	 * 
 	 * @param resource
 	 *            to get contained {@link ProfileApplication
 	 *            ProfileApplications}.
 	 * @return the list of {@link ProfileApplication ProfileApplications}.
 	 */
 	private EList<ProfileApplication> getProfileApplications(Resource resource) {
 		EList<ProfileApplication> profileApplications = new BasicEList<ProfileApplication>();
 		for (EObject eObject : resource.getContents()) {
 			if (eObject instanceof ProfileApplication) {
 				profileApplications.add((ProfileApplication) eObject);
 			}
 		}
 		return profileApplications;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<StereotypeApplicability> getApplicableStereotypes(EClass eClass) {
 		EList<StereotypeApplicability> stereotypeApplicabilities = new BasicEList<StereotypeApplicability>();
 		for (Profile profile : profiles) {
 			for (Stereotype stereotype : profile
 					.getApplicableStereotypes(eClass)) {
 				for (Extension extension : stereotype
 						.getApplicableExtensions(eClass)) {
 					stereotypeApplicabilities.add(createApplicableStereotype(
 							stereotype, extension));
 				}
 			}
 		}
 		return stereotypeApplicabilities;
 	}
 
 	private StereotypeApplicability createApplicableStereotype(
 			Stereotype stereotype, Extension extension) {
 		StereotypeApplicability stereotypeApplicability = EMF_PROFILE_APPLICATION_FACTORY
 				.createStereotypeApplicability();
 		stereotypeApplicability.setStereotype(stereotype);
 		stereotypeApplicability.setExtension(extension);
 		return stereotypeApplicability;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<StereotypeApplicability> getApplicableStereotypes(
 			EObject eObject) {
 		EList<StereotypeApplicability> stereotypeApplicabilities = getApplicableStereotypes(eObject
 				.eClass());
 
 		// check applicability for each
 		for (StereotypeApplicability stereotypeApplicability : new BasicEList<StereotypeApplicability>(
 				stereotypeApplicabilities)) {
 			if (!isApplicable(stereotypeApplicability.getStereotype(), eObject,
 					stereotypeApplicability.getExtension())) {
				stereotypeApplicabilities.remove(stereotypeApplicability);
 			}
 		}
 		return stereotypeApplicabilities;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isApplicable(Stereotype stereotype, EObject eObject) {
 		return stereotype.isApplicable(eObject,
 				extractAppliedExtensions(getAppliedStereotypes(eObject)));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isApplicable(Stereotype stereotype, EObject eObject,
 			Extension extension) {
 		return stereotype.isApplicable(eObject, extension,
 				extractAppliedExtensions(getAppliedStereotypes(eObject)));
 	}
 
 	private EList<Extension> extractAppliedExtensions(
 			EList<StereotypeApplication> appliedStereotypes) {
 		EList<Extension> appliedExtensions = new BasicEList<Extension>();
 		for (StereotypeApplication stereotypeApplication : appliedStereotypes) {
 			appliedExtensions.add(stereotypeApplication.getExtension());
 		}
 		return appliedExtensions;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public StereotypeApplication apply(
 			StereotypeApplicability stereotypeApplicability, EObject eObject) {
 		return apply(stereotypeApplicability.getStereotype(), eObject,
 				stereotypeApplicability.getExtension());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public StereotypeApplication apply(Stereotype stereotype, EObject eObject) {
 		Extension defaultExtension = getDefaultExtension(stereotype, eObject);
 		return apply(stereotype, eObject, defaultExtension);
 	}
 
 	private Extension getDefaultExtension(Stereotype stereotype, EObject eObject) {
 		EList<Extension> applicableExtensions = getApplicableExtensions(
 				stereotype, eObject);
 		if (applicableExtensions.size() > 0) {
 			return applicableExtensions.get(0);
 		} else {
 			throw new IllegalArgumentException(STEREOTYPE_NOT_APPLICABLE);
 		}
 	}
 
 	private EList<Extension> getApplicableExtensions(Stereotype stereotype,
 			EObject eObject) {
 		return stereotype.getApplicableExtensions(eObject,
 				extractAppliedExtensions(getAppliedStereotypes(eObject)));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public StereotypeApplication apply(Stereotype stereotype, EObject eObject,
 			Extension extension) {
 		if (!isApplicable(stereotype, eObject, extension)) {
 			throw new IllegalArgumentException(STEREOTYPE_NOT_APPLICABLE);
 		}
 		StereotypeApplication stereotypeApplication = createStereotypeApplication(stereotype);
 		setExtension(stereotypeApplication, extension);
 		apply(stereotypeApplication, eObject);
 		return stereotypeApplication;
 	}
 
 	private void setExtension(
 			final StereotypeApplication stereotypeApplication,
 			final Extension extension) {
 		if (requireTransaction()) {
 			TransactionalEditingDomain domain = getTransactionalEditingDomain();
 			domain.getCommandStack().execute(new RecordingCommand(domain) {
 				@Override
 				protected void doExecute() {
 					stereotypeApplication.setExtension(extension);
 				}
 			});
 		} else {
 			stereotypeApplication.setExtension(extension);
 		}
 	}
 
 	/**
 	 * Creates a new instance of of the specified {@link Stereotype}.
 	 * 
 	 * <p>
 	 * The created instance is {{@link #addToResource(StereotypeApplication)}
 	 * added} to the currently set resource.
 	 * 
 	 * @param stereotype
 	 *            to create instance for.
 	 * @return created instance.
 	 */
 	protected StereotypeApplication createStereotypeApplication(
 			Stereotype stereotype) {
 		final StereotypeApplication stereotypeInstance = (StereotypeApplication) stereotype
 				.getEPackage().getEFactoryInstance().create(stereotype);
 		final ProfileApplication profileApplication = findOrCreateProfileApplication(stereotype
 				.getProfile());
 		if (requireTransaction()) {
 			TransactionalEditingDomain domain = getTransactionalEditingDomain();
 			domain.getCommandStack().execute(new RecordingCommand(domain) {
 				@Override
 				protected void doExecute() {
 					profileApplication.getStereotypeApplications().add(
 							stereotypeInstance);
 				}
 			});
 		} else {
 			profileApplication.getStereotypeApplications().add(
 					stereotypeInstance);
 		}
 		return stereotypeInstance;
 	}
 
 	/**
 	 * Finds or creates a profile application for the specified
 	 * <code>profile</code>.
 	 * 
 	 * @param profile
 	 *            to find or create {@link ProfileApplication} for.
 	 * @return found or created {@link ProfileApplication}.
 	 */
 	private ProfileApplication findOrCreateProfileApplication(
 			final Profile profile) {
 		boolean found = false;
 		ProfileApplication profileApplication = null;
 		for (EObject eObject : profileApplicationResource.getContents()) {
 			if (eObject instanceof ProfileApplication) {
 				found = true;
 				profileApplication = (ProfileApplication) eObject;
 				final ProfileApplication finalProfileApplication = profileApplication;
 				if (!hasProfileImport(profileApplication, profile)) {
 					if (requireTransaction()) {
 						TransactionalEditingDomain domain = getTransactionalEditingDomain();
 						domain.getCommandStack().execute(
 								new RecordingCommand(domain) {
 									@Override
 									protected void doExecute() {
 										finalProfileApplication
 												.getImportedProfiles()
 												.add(createProfileImport(profile));
 									}
 								});
 					} else {
 						finalProfileApplication.getImportedProfiles().add(
 								createProfileImport(profile));
 					}
 				}
 				profileApplication = finalProfileApplication;
 			}
 		}
 		if (!found) {
 			profileApplication = createProfileApplication();
 			profileApplication.getImportedProfiles().add(
 					createProfileImport(profile));
 			addToResource(profileApplication);
 		}
 		return profileApplication;
 	}
 
 	/**
 	 * Creates an empty {@link ProfileApplication}.
 	 * 
 	 * @return the created {@link ProfileApplication}.
 	 */
 	private ProfileApplication createProfileApplication() {
 		return EMF_PROFILE_APPLICATION_FACTORY.createProfileApplication();
 	}
 
 	/**
 	 * Checks if the specified <code>profile</code> is already imported by the
 	 * supplied <code>profileApplication</code>.
 	 * 
 	 * @param profileApplication
 	 *            to search in.
 	 * @param profile
 	 *            to search for.
 	 * @return <code>true</code> if an import exists, otherwise
 	 *         <code>false</code>.
 	 */
 	private boolean hasProfileImport(ProfileApplication profileApplication,
 			Profile profile) {
 		for (ProfileImport profileImport : profileApplication
 				.getImportedProfiles()) {
 			try {
 				if (profileImport.getNsURI().equals(profile.getNsURI())) {
 					return true;
 				}
 			} catch (NullPointerException npe) {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a new {@link ProfileImport} for the supplied <code>profile</code>
 	 * .
 	 * 
 	 * @param profile
 	 *            to create {@link ProfileImport} for.
 	 * @return the created {@link ProfileImport}.
 	 */
 	private ProfileImport createProfileImport(Profile profile) {
 		ProfileImport profileImport = EMF_PROFILE_APPLICATION_FACTORY
 				.createProfileImport();
 		profileImport.setProfile(profile);
 		return profileImport;
 	}
 
 	/**
 	 * Applies the specified stereotype application to the specified object.
 	 * 
 	 * @param stereotypeApplication
 	 *            to be applied.
 	 * @param eObject
 	 *            to apply the stereotype application to.
 	 */
 	private void apply(final StereotypeApplication stereotypeApplication,
 			final EObject eObject) {
 		if (requireTransaction()) {
 			TransactionalEditingDomain domain = getTransactionalEditingDomain();
 			domain.getCommandStack().execute(new RecordingCommand(domain) {
 				@Override
 				protected void doExecute() {
 					stereotypeApplication.setAppliedTo(eObject);
 				}
 			});
 		} else {
 			stereotypeApplication.setAppliedTo(eObject);
 		}
 	}
 
 	/**
 	 * Adds the specified <code>eObject</code> to the currently set
 	 * {@link #profileApplicationResource}.
 	 * 
 	 * <p>
 	 * If currently no {@link #profileApplicationResource} is set, this method
 	 * throws an {@link IllegalArgumentException}.
 	 * </p>
 	 * 
 	 * @param eObject
 	 *            to be added.
 	 * 
 	 * @exception IllegalArgumentException
 	 *                if {@link #profileApplicationResource} is not
 	 *                {@link #setProfileApplicationResource(Resource) set}.
 	 */
 	protected void addToResource(final EObject eObject) {
 		if (profileApplicationResource == null
 				|| !profileApplicationResource.isLoaded()) {
 			throw new IllegalArgumentException(STEREOTYPE_APP_RESOURCE_ERROR);
 		} else {
 			if (!profileApplicationResource.getContents().contains(eObject)) {
 				if (requireTransaction()) {
 					TransactionalEditingDomain domain = getTransactionalEditingDomain();
 					domain.getCommandStack().execute(
 							new RecordingCommand(domain) {
 								@Override
 								protected void doExecute() {
 									profileApplicationResource.getContents()
 											.add(eObject);
 								}
 							});
 				} else {
 					profileApplicationResource.getContents().add(eObject);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<StereotypeApplication> getStereotypeApplications() {
 		EList<StereotypeApplication> stereotypeApplications = new BasicEList<StereotypeApplication>();
 		for (ProfileApplication profileApplication : getProfileApplications(profileApplicationResource)) {
 			stereotypeApplications.addAll(profileApplication
 					.getStereotypeApplications());
 		}
 		return ECollections.unmodifiableEList(stereotypeApplications);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<StereotypeApplication> getAppliedStereotypes(EObject eObject) {
 		EList<StereotypeApplication> stereotypeApplications = new BasicEList<StereotypeApplication>();
 		for (ProfileApplication profileApplication : getProfileApplications(profileApplicationResource)) {
 			stereotypeApplications.addAll(profileApplication
 					.getStereotypeApplications(eObject));
 		}
 		return ECollections.unmodifiableEList(stereotypeApplications);
 	}
 
 	/**
 	 * Returns all stereotypes currently applied to the specified
 	 * <code>eObject</code> (list of {@link StereotypeApplication}s) that are of
 	 * the type <code>stereotype</code>.
 	 * 
 	 * @param stereotype
 	 *            the stereotype to filter stereotype applications.
 	 * @param eObject
 	 *            to get applied stereotypes for.
 	 * @return the list of {@link StereotypeApplication}s.
 	 */
 	protected EList<StereotypeApplication> getAppliedStereotypes(
 			EObject eObject, Stereotype stereotype) {
 		EList<StereotypeApplication> stereotypeApplications = new BasicEList<StereotypeApplication>();
 		for (ProfileApplication profileApplication : getProfileApplications(profileApplicationResource)) {
 			stereotypeApplications.addAll(profileApplication
 					.getStereotypeApplications(eObject, stereotype));
 		}
 		return ECollections.unmodifiableEList(stereotypeApplications);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void removeStereotypeApplication(
 			final StereotypeApplication stereotypeApplication) {
 		if (requireTransaction()) {
 			TransactionalEditingDomain domain = getTransactionalEditingDomain();
 			domain.getCommandStack().execute(new RecordingCommand(domain) {
 				@Override
 				protected void doExecute() {
 					EcoreUtil.remove(stereotypeApplication);
 				}
 			});
 		} else {
 			EcoreUtil.remove(stereotypeApplication);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EList<EStructuralFeature> getStereotypeFeatures(Stereotype stereotype) {
 		EList<EStructuralFeature> features = new BasicEList<EStructuralFeature>();
 		for (EStructuralFeature feature : stereotype
 				.getEAllStructuralFeatures()) {
 			if (!STEREOTYPE_APPLICATION_APPLIED_TO_REFERENCE.equals(feature)) {
 				features.add(feature);
 			}
 		}
 		return features;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getTaggedValue(EObject stereotypeApplication,
 			EStructuralFeature taggedValue) {
 		return stereotypeApplication.eGet(taggedValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setTaggedValue(EObject stereotypeApplication,
 			EStructuralFeature taggedValue, Object newValue) {
 		if (requireTransaction()) {
 			TransactionalEditingDomain domain = getTransactionalEditingDomain();
 			Command command = domain.createCommand(SetCommand.class,
 					new CommandParameter(stereotypeApplication, taggedValue,
 							newValue));
 			domain.getCommandStack().execute(command);
 		} else {
 			stereotypeApplication.eSet(taggedValue, newValue);
 		}
 	}
 
 	/**
 	 * Specifies whether transaction aware modifications are required for
 	 * {@link #profileApplicationResource}.
 	 * 
 	 * @return <code>true</code>, if {@link #profileApplicationResource} is
 	 *         handled by a {@link TransactionalEditingDomain},
 	 *         <code>false</code> otherwise.
 	 */
 	private boolean requireTransaction() {
 		return getTransactionalEditingDomain() != null;
 	}
 
 	/**
 	 * Returns the {@link TransactionalEditingDomain} of the
 	 * {@link #profileApplicationResource}. If there is no
 	 * {@link TransactionalEditingDomain}, this method returns
 	 * <code>null.</code>
 	 * 
 	 * @return the {@link TransactionalEditingDomain} or <code>null</code>
 	 */
 	private TransactionalEditingDomain getTransactionalEditingDomain() {
 		return TransactionUtil.getEditingDomain(profileApplicationResource);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void unload() {
 		// noop
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Diagnostic validateAll(EObject currentlySelectedEObject) {
 		Map<String, EObject> context = createValidationContextMap(currentlySelectedEObject);
 		Diagnostic diagnostic = null;
 		for (ProfileApplication profileApplication : getProfileApplications(profileApplicationResource)) {
 			diagnostic = Diagnostician.INSTANCE.validate(profileApplication,
 					context);
 			if (Diagnostic.OK != diagnostic.getSeverity()) {
 				return diagnostic;
 			}
 		}
 		if (diagnostic != null) {
 			return diagnostic;
 		} else {
 			return EcoreUtil
 					.computeDiagnostic(profileApplicationResource, true);
 		}
 	}
 
 	private Map<String, EObject> createValidationContextMap(
 			EObject currentlySelectedEObject) {
 		Map<String, EObject> context = new HashMap<String, EObject>();
 		context.put("MODEL_OBJECT", currentlySelectedEObject);
 		return context;
 	}
 
 }
