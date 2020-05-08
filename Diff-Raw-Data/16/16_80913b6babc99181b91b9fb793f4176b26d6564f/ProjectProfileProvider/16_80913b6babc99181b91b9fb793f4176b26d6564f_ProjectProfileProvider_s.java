 package org.modelversioning.emfprofile.registry.internal;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.modelversioning.emfprofile.Profile;
 import org.modelversioning.emfprofile.registry.IProfileProvider;
 
 public class ProjectProfileProvider implements IProfileProvider {
 
 	private IProject project;
 	private Resource profileResource;
 	private Profile profile;
 	private String profileName;
 	private String profileNsURI;
 
 	public ProjectProfileProvider(IProject project, Resource profileResource) {
 		super();
 		this.project = project;
 		this.profileResource = profileResource;
 		initialize();
 	}
 
 	private void initialize() {
 		if (profileResource == null) {
 			throw new IllegalArgumentException("Resource is null");
 		}
 		if (profileResource.getContents().size() < 0) {
 			throw new IllegalArgumentException("Resource is emtpy");
 		}
		EObject eObject = profileResource.getContents().get(0);
		if (eObject instanceof Profile) {
			initialize((Profile) eObject);
 		} else {
 			throw new IllegalArgumentException("Resource contains no profile");
 		}
 	}
 
 	private void initialize(Profile profile) {
 		this.profile = profile;
 		this.profileName = profile.getName();
 		this.profileNsURI = profile.getNsURI();
 	}
 
 	public IProject getProject() {
 		return project;
 	}
 
 	@Override
 	public Profile getProfile() {
 		return profile;
 	}
 
 	@Override
 	public String getProfileNsURI() {
 		return profileNsURI;
 	}
 
 	@Override
 	public String getProfileName() {
 		return profileName;
 	}
 
 	@Override
 	public ProfileLocationType getProfileLocationType() {
 		return ProfileLocationType.WORKSPACE;
 	}
 
 }
