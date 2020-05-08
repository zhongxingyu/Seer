 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.engine.maven;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MavenItem;
 import org.eclipse.b3.aggregator.MavenMapping;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.p2.maven.POM;
 import org.eclipse.b3.p2.maven.pom.DependenciesType;
 import org.eclipse.b3.p2.maven.pom.Dependency;
 import org.eclipse.b3.p2.maven.pom.License;
 import org.eclipse.b3.p2.maven.pom.LicensesType;
 import org.eclipse.b3.p2.maven.pom.Model;
 import org.eclipse.b3.p2.maven.pom.Parent;
 import org.eclipse.b3.p2.maven.pom.PomFactory;
 import org.eclipse.b3.p2.maven.util.VersionUtil;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.metadata.InstallableUnit;
 import org.eclipse.equinox.internal.p2.metadata.RequiredCapability;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.ICopyright;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitFragment;
 import org.eclipse.equinox.p2.metadata.ILicense;
 import org.eclipse.equinox.p2.metadata.IProvidedCapability;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.ITouchpointData;
 import org.eclipse.equinox.p2.metadata.ITouchpointType;
 import org.eclipse.equinox.p2.metadata.IUpdateDescriptor;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 
 /**
  * @author Filip Hrbek (filip.hrbek@cloudsmith.com)
  * 
  */
 // Implement parent subtraction on all relevant getters (as an example, dependencies are implemented that way)
 // However, repo optimization is not implemented yet, so it does not make much sense to do it now
 
 public class InstallableUnitMapping implements IInstallableUnit {
 	public enum Type {
 		TOP, GROUP, IU, PROXY;
 	}
 
 	private static final Version DUMMY_VERSION = Version.parseVersion("1");
 
 	private static MavenItem map(String id, List<MavenMapping> mappings) throws CoreException {
 		MavenItem item = null;
 
 		for(MavenMapping mapping : mappings)
 			if((item = mapping.map(id)) != null)
 				return item;
 
 		StringBuilder mappingDescriptor = new StringBuilder();
 		boolean first = true;
 		for(MavenMapping mapping : mappings) {
 			if(first)
 				first = false;
 			else
 				mappingDescriptor.append(',');
 			mappingDescriptor.append(mapping.toString());
 		}
 
 		throw ExceptionUtils.fromMessage(
 			"Unable to map IU to maven artifact: id=%s, mappings=%s", id, mappingDescriptor.toString());
 	}
 
 	private Type type;
 
 	private IInstallableUnit installableUnit;
 
 	private List<MavenMapping> mappings;
 
 	private MavenItem mapped;
 
 	private InstallableUnitMapping parent;
 
 	private List<InstallableUnitMapping> children = new ArrayList<InstallableUnitMapping>();
 
 	private List<InstallableUnitMapping> siblings = new ArrayList<InstallableUnitMapping>();
 
 	private boolean transientFlag;
 
 	private IArtifactKey mainArtifact;
 
 	private Contribution contribution;
 
 	public InstallableUnitMapping() {
 		this((String) null);
 	}
 
 	public InstallableUnitMapping(Contribution contribution, IInstallableUnit iu) {
 		this(contribution, iu, Collections.<MavenMapping> emptyList());
 	}
 
 	public InstallableUnitMapping(Contribution contribution, IInstallableUnit iu, List<MavenMapping> mappings) {
 		this.contribution = contribution;
 		this.mappings = new ArrayList<MavenMapping>(mappings.size() + 1);
 		this.mappings.addAll(mappings);
 		this.mappings.add(MavenMapping.DEFAULT_MAPPING);
 
 		for(MavenMapping mapping : mappings)
 			if(mapping.getStatus().getCode() != StatusCode.OK)
 				throw new RuntimeException("Invalid maven mapping: " + mapping.toString());
 
 		switch(iu.getArtifacts().size()) {
 			case 1:
 				mainArtifact = iu.getArtifacts().iterator().next();
 				// no break here, we really want to continue initialization
 			case 0:
 				type = Type.IU;
 				installableUnit = iu;
 				break;
 			default:
 				// We have more than one artifact - we need to make a proxy depending on the artifacts
 				InstallableUnitOverrider proxy = new InstallableUnitOverrider(iu);
 				List<IRequirement> dependencies = new ArrayList<IRequirement>(iu.getArtifacts().size());
 				int idx = 0;
 				for(IArtifactKey artifact : iu.getArtifacts()) {
 					String genId = artifact.getId() + ".artifact-" + (idx + 1);
 					dependencies.add(new RequiredCapability(NAMESPACE_IU_ID, genId, new VersionRange(
 						iu.getVersion(), true, iu.getVersion(), true), null, false, false));
 					InstallableUnitOverrider sibling = new InstallableUnitOverrider(iu);
 					sibling.overrideId(genId);
 					sibling.overrideArtifacts(Collections.singletonList(artifact));
 
 					siblings.add(new InstallableUnitMapping(contribution, sibling));
 				}
 
 				proxy.overrideRequirements(dependencies);
 				proxy.overrideArtifacts(Collections.<IArtifactKey> emptyList());
 				installableUnit = proxy;
 		}
 	}
 
 	public InstallableUnitMapping(String name) {
 		String groupId = name;
 		if(groupId == null)
 			groupId = "_top";
 		String artifactId = name == null
 				? "_common"
 				: "_group-common";
 
 		type = name == null
 				? Type.TOP
 				: Type.GROUP;
 
 		InstallableUnit installableUnit = new InstallableUnit();
 		installableUnit.setId(artifactId);
 		installableUnit.setVersion(DUMMY_VERSION);
 
 		this.installableUnit = installableUnit;
 		mapped = AggregatorFactory.eINSTANCE.createMavenItem();
 		mapped.setGroupId(groupId);
 		mapped.setArtifactId(artifactId);
 	}
 
 	public POM asPOM() throws CoreException {
 		POM pom = new POM();
 		Model model = pom.getProject();
 		if(parent != null && !parent.isTransient()) {
 			Parent newParent = PomFactory.eINSTANCE.createParent();
 			newParent.setGroupId(parent.map().getGroupId());
 			newParent.setArtifactId(parent.map().getArtifactId());
 			newParent.setVersion(parent.getVersion().toString());
 			model.setParent(newParent);
 		}
 		model.setModelVersion(POM.MODEL_VERSION);
 		model.setGroupId(map().getGroupId());
 		model.setArtifactId(map().getArtifactId());
 		if(getMainArtifact() == null)
 			model.setPackaging("pom");
 
 		if(getVersion() != null && !getVersion().equals(Version.emptyVersion))
 			model.setVersion(getVersionString());
 
 		Collection<IRequirement> requirements = getRequirements();
 		if(requirements.size() > 0) {
 			DependenciesType dependencies = PomFactory.eINSTANCE.createDependenciesType();
 			for(IRequirement req : requirements) {
 				if(!(req instanceof IRequiredCapability))
 					continue;
 
 				IRequiredCapability cap = (IRequiredCapability) req;
 				// Only dependencies on IUs and OSGi bundles are considered in maven
 				if(IInstallableUnit.NAMESPACE_IU_ID.equals(cap.getNamespace()) ||
 						"osgi.bundle".equals(cap.getNamespace())) {
 					Dependency dependency = PomFactory.eINSTANCE.createDependency();
 					dependencies.getDependency().add(dependency);
 
 					MavenItem dependencyMapping = map(cap.getName(), mappings);
 					dependency.setGroupId(dependencyMapping.getGroupId());
 					dependency.setArtifactId(dependencyMapping.getArtifactId());
 
 					if(cap.getRange() != null && !cap.getRange().equals(VersionRange.emptyRange)) {
 						StringBuilder versionRangeString = new StringBuilder();
 						Version low = cap.getRange().getMinimum();
 						Version high = cap.getRange().getMaximum();
 						if(cap.getRange().getIncludeMinimum() && Version.MAX_VERSION.equals(high)) {
							versionRangeString.append("[").append(VersionUtil.getVersionString(low)).append(",)");
 						}
 						else {
 							versionRangeString.append(cap.getRange().getIncludeMinimum()
 									? '['
 									: '(');
 							versionRangeString.append(VersionUtil.getVersionString(low));
 							versionRangeString.append(',');
 							versionRangeString.append(VersionUtil.getVersionString(high));
 							versionRangeString.append(cap.getRange().getIncludeMaximum()
 									? ']'
 									: ')');
 						}
 
 						dependency.setVersion(versionRangeString.toString());
 					}
 					else {
 						dependency.setVersion("[0.0,)");
 					}
 
 					if(cap.getMin() == 0)
 						dependency.setOptional(true);
 				}
 			}
 
 			// it is still possible that no dependency has been found since only dependencies on IUs and OSGi bundles
 			// are considered
 			// if no mavanizable dependency was found, don't create the dependencies section at all
 			if(dependencies.getDependency().size() > 0)
 				model.setDependencies(dependencies);
 		}
 
 		Map<String, String> iuProperties = new HashMap<String, String>(installableUnit.getProperties());
 		String name = extractProperty(iuProperties, IInstallableUnit.PROP_NAME);
 		String description = extractProperty(iuProperties, IInstallableUnit.PROP_DESCRIPTION);
 
 		if(name != null || description != null) {
 			if(name != null) {
 				if(description != null) {
 					name = name + "\n\n";
 				}
 			}
 			else
 				name = "";
 
 			if(description == null)
 				description = "";
 
 			model.setDescription(name + description);
 		}
 
 		LicensesType licenses = PomFactory.eINSTANCE.createLicensesType();
 
 		Collection<ILicense> iuLicenses = installableUnit.getLicenses();
 		if(iuLicenses.size() > 0) {
 			for(ILicense iuLicense : iuLicenses) {
 				License license = PomFactory.eINSTANCE.createLicense();
 				boolean licenseSet = false;
 				if(iuLicense.getLocation() != null) {
 					licenseSet = true;
 					license.setUrl(iuLicense.getLocation().toString());
 				}
 				if(iuLicense.getBody() != null) {
 					licenseSet = true;
 					license.setComments(iuLicense.getBody());
 				}
 				if(licenseSet)
 					licenses.getLicense().add(license);
 			}
 		}
 
 		ICopyright iuCopyright = installableUnit.getCopyright();
 		if(iuCopyright != null) {
 			License copyright = PomFactory.eINSTANCE.createLicense();
 			boolean copyrightSet = false;
 			if(iuCopyright.getLocation() != null) {
 				copyrightSet = true;
 				copyright.setUrl(iuCopyright.getLocation().toString());
 			}
 			if(iuCopyright.getBody() != null) {
 				copyrightSet = true;
 				copyright.setComments(iuCopyright.getBody());
 			}
 			if(copyrightSet)
 				licenses.getLicense().add(copyright);
 		}
 
 		if(licenses.getLicense().size() > 0)
 			model.setLicenses(licenses);
 
 		return pom;
 	}
 
 	public int compareTo(IInstallableUnit other) {
 		return installableUnit.compareTo(other);
 	}
 
 	private String extractProperty(Map<String, String> iuProperties, String key) {
 		String value = iuProperties.remove(key);
 
 		if(value != null) {
 			if(value.startsWith("%")) {
 				String localizedKey = "df_LT." + value.substring(1);
 				String localizedValue = iuProperties.remove(localizedKey);
 
 				if(localizedValue != null)
 					value = localizedValue;
 			}
 		}
 
 		return trimOrNull(value);
 	}
 
 	private String getArtifactFileName() throws CoreException {
 		return getFileName(null);
 	}
 
 	public Collection<IArtifactKey> getArtifacts() {
 		return installableUnit.getArtifacts();
 	}
 
 	public List<InstallableUnitMapping> getChildren() {
 		return children;
 	}
 
 	public final Contribution getContribution() {
 		return contribution;
 	}
 
 	public ICopyright getCopyright() {
 		return installableUnit.getCopyright();
 	}
 
 	public ICopyright getCopyright(String locale) {
 		return installableUnit.getCopyright(locale);
 	}
 
 	private String getFileName(String extension) throws CoreException {
 		String fileId = getId();
 		StringBuilder fileName = new StringBuilder(fileId);
 		fileName.append('-');
 		fileName.append(getVersionString());
 
 		if(extension == null) {
 			if(!(getMainArtifact() != null && "binary".equals(getMainArtifact().getClassifier()))) {
 				fileName.append(".jar");
 			}
 		}
 		else {
 			fileName.append('.');
 			fileName.append(extension);
 		}
 
 		return fileName.toString();
 	}
 
 	public IMatchExpression<IInstallableUnit> getFilter() {
 		return installableUnit.getFilter();
 	}
 
 	public Collection<IInstallableUnitFragment> getFragments() {
 		return installableUnit.getFragments();
 	}
 
 	public String getId() {
 		return installableUnit.getId();
 	}
 
 	public Collection<ILicense> getLicenses() {
 		return installableUnit.getLicenses();
 	}
 
 	public Collection<ILicense> getLicenses(String locale) {
 		return installableUnit.getLicenses(locale);
 	}
 
 	public IArtifactKey getMainArtifact() {
 		return mainArtifact;
 	}
 
 	public Collection<IRequirement> getMetaRequirements() {
 		return installableUnit.getMetaRequirements();
 	}
 
 	public InstallableUnitMapping getParent() {
 		return parent;
 	}
 
 	public String getPomName() throws CoreException {
 		return getFileName("pom");
 	}
 
 	public Map<String, String> getProperties() {
 		return installableUnit.getProperties();
 	}
 
 	public String getProperty(String key) {
 		return installableUnit.getProperty(key);
 	}
 
 	public String getProperty(String key, String locale) {
 		return installableUnit.getProperty(key, locale);
 	}
 
 	public Collection<IProvidedCapability> getProvidedCapabilities() {
 		return installableUnit.getProvidedCapabilities();
 	}
 
 	public String getRelativeFullPath() throws CoreException {
 		return getRelativePath() + "/" + getArtifactFileName();
 	}
 
 	public String getRelativePath() throws CoreException {
 		return map().getGroupId().replace('.', '/') + "/" + map().getArtifactId() + "/" + getVersionString();
 	}
 
 	public Collection<IRequirement> getRequirements() {
 		if(parent != null) {
 			Collection<IRequirement> myList = new ArrayList<IRequirement>();
 			Collection<IRequirement> parentList = parent.installableUnit.getRequirements();
 			for(IRequirement my : installableUnit.getRequirements())
 				if(!parentList.contains(my))
 					myList.add(my);
 
 			return myList;
 		}
 
 		return installableUnit.getRequirements();
 	}
 
 	public List<InstallableUnitMapping> getSiblings() {
 		return siblings;
 	}
 
 	public Collection<ITouchpointData> getTouchpointData() {
 		return installableUnit.getTouchpointData();
 	}
 
 	public ITouchpointType getTouchpointType() {
 		return installableUnit.getTouchpointType();
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public IUpdateDescriptor getUpdateDescriptor() {
 		return installableUnit.getUpdateDescriptor();
 	}
 
 	public Version getVersion() {
 		return installableUnit.getVersion();
 	}
 
 	public String getVersionString() {
 		return VersionUtil.getVersionString(getVersion());
 	}
 
 	public boolean isResolved() {
 		return installableUnit.isResolved();
 	}
 
 	public boolean isSingleton() {
 		return installableUnit.isSingleton();
 	}
 
 	public boolean isTransient() {
 		return transientFlag;
 	}
 
 	public MavenItem map() throws CoreException {
 		if(mapped != null)
 			return mapped;
 
 		return mapped = map(getId(), mappings);
 	}
 
 	public boolean satisfies(IRequiredCapability candidate) {
 		return installableUnit.satisfies(candidate);
 	}
 
 	public boolean satisfies(IRequirement candidate) {
 		return installableUnit.satisfies(candidate);
 	}
 
 	public void setParent(InstallableUnitMapping parent) {
 		if(this.parent != null)
 			this.parent.children.remove(this);
 		(this.parent = parent).children.add(this);
 	}
 
 	public void setTransient(boolean isTransient) {
 		transientFlag = isTransient;
 	}
 
 	private String trimOrNull(String value) {
 		if(value != null) {
 			value = value.trim();
 			if(value.length() == 0)
 				value = null;
 		}
 		return value;
 	}
 
 	public IInstallableUnit unresolved() {
 		return installableUnit.unresolved();
 	}
 }
