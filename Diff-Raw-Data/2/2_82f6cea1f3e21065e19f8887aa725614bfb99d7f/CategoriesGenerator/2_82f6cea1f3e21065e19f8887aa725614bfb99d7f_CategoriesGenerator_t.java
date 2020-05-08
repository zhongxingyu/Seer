 package org.eclipse.b3.aggregator.engine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.Category;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.ExclusionRule;
 import org.eclipse.b3.aggregator.Feature;
 import org.eclipse.b3.aggregator.MapRule;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.p2.InstallableUnit;
 import org.eclipse.b3.aggregator.p2.MetadataRepository;
 import org.eclipse.b3.aggregator.p2.P2Factory;
 import org.eclipse.b3.aggregator.p2.ProvidedCapability;
 import org.eclipse.b3.aggregator.p2.RequiredCapability;
 import org.eclipse.b3.aggregator.p2.impl.InstallableUnitImpl;
 import org.eclipse.b3.aggregator.p2.impl.ProvidedCapabilityImpl;
 import org.eclipse.b3.aggregator.util.LogUtils;
 import org.eclipse.b3.aggregator.util.MonitorUtils;
 import org.eclipse.b3.aggregator.util.TimeUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.internal.provisional.p2.metadata.Version;
 import org.eclipse.equinox.internal.provisional.p2.metadata.VersionRange;
 import org.eclipse.equinox.internal.provisional.p2.metadata.VersionedId;
 
 public class CategoriesGenerator extends BuilderPhase {
 	private static void assignCategoryVersion(InstallableUnitImpl category) {
 		List<VersionedId> includedBundles = new ArrayList<VersionedId>();
 		List<VersionedId> includedFeatures = new ArrayList<VersionedId>();
 		for(RequiredCapability cap : category.getRequiredCapabilityList()) {
 			VersionRange range = cap.getRange();
 			Version version;
 			if(range == null)
 				version = Version.emptyVersion;
 			else
 				version = range.getMinimum();
 			VersionedId vn = new VersionedId(cap.getName(), version);
 			if(cap.getName().endsWith(Builder.FEATURE_GROUP_SUFFIX))
 				includedFeatures.add(vn);
 			else
 				includedBundles.add(vn);
 		}
 
 		VersionSuffixGenerator suffixGen = new VersionSuffixGenerator();
 		Version catVersion = Version.createOSGi(0, 0, 0, suffixGen.generateSuffix(includedFeatures, includedBundles));
 		category.setVersion(catVersion);
 		List<ProvidedCapability> providedCaps = category.getProvidedCapabilityList();
 		providedCaps.clear();
 		ProvidedCapabilityImpl providedCap = (ProvidedCapabilityImpl) P2Factory.eINSTANCE.createProvidedCapability();
 		providedCap.setName(category.getId());
 		providedCap.setNamespace(IInstallableUnit.NAMESPACE_IU_ID);
 		providedCap.setVersion(catVersion);
 		providedCaps.add(providedCap);
 	}
 
 	public CategoriesGenerator(Builder builder) {
 		super(builder);
 	}
 
 	@Override
 	public void run(IProgressMonitor monitor) throws CoreException {
 		long start = TimeUtils.getNow();
 		MonitorUtils.begin(monitor, 10);
 		String info = "Starting generation of categories";
 		MonitorUtils.subTask(monitor, info);
 		LogUtils.info(info);
 		try {
 			List<InstallableUnit> results = new ArrayList<InstallableUnit>();
 			Aggregator aggregator = getBuilder().getAggregator();
 			for(CustomCategory category : aggregator.getCustomCategories())
 				results.add(createCategoryIU(category));
 
 			MonitorUtils.worked(monitor, 5);
 			for(Contribution contrib : aggregator.getContributions(true))
 				for(MappedRepository repo : contrib.getRepositories(true))
 					results.addAll(getRepositoryCategories(repo));
 
 			results = normalizeCategories(results);
 			getBuilder().setCategoryIUs(results);
 		}
 		finally {
 			MonitorUtils.done(monitor);
 		}
 		LogUtils.info("Done. Took %s", TimeUtils.getFormattedDuration(start)); //$NON-NLS-1$
 	}
 
 	private InstallableUnit createCategoryIU(CustomCategory category) {
 		P2Factory factory = P2Factory.eINSTANCE;
 		InstallableUnitImpl cat = (InstallableUnitImpl) factory.createInstallableUnit();
 		cat.setSingleton(true);
 		String categoryId = category.getIdentifier();
 		cat.setId(categoryId);
 
 		Map<String, String> props = cat.getPropertyMap().map();
 		props.put(IInstallableUnit.PROP_NAME, category.getLabel());
 		props.put(IInstallableUnit.PROP_DESCRIPTION, category.getDescription());
 		props.put(IInstallableUnit.PROP_TYPE_CATEGORY, "true"); //$NON-NLS-1$
 
 		List<Feature> features = category.getFeatures();
 		List<RequiredCapability> rcs = cat.getRequiredCapabilityList();
 		List<VersionedId> includedBundles = new ArrayList<VersionedId>();
 		List<VersionedId> includedFeatures = new ArrayList<VersionedId>();
 		for(Feature feature : features) {
 			if(!feature.isBranchEnabled())
 				continue;
 
 			rcs.add((RequiredCapability) feature.getRequiredCapability());
 
 			VersionedId vn = new VersionedId(feature.getName(), feature.getVersionRange().getMinimum());
 			if(vn.getId().endsWith(Builder.FEATURE_GROUP_SUFFIX))
 				includedFeatures.add(vn);
 			else
 				includedBundles.add(vn);
 		}
 
 		// Add self capability
 		ProvidedCapabilityImpl pc = (ProvidedCapabilityImpl) factory.createProvidedCapability();
 		pc.setName(categoryId);
 		pc.setNamespace(IInstallableUnit.NAMESPACE_IU_ID);
 
 		VersionSuffixGenerator suffixGen = new VersionSuffixGenerator();
 		Version catVersion = Version.createOSGi(0, 0, 0, suffixGen.generateSuffix(includedFeatures, includedBundles));
 		pc.setVersion(catVersion);
 		cat.setVersion(catVersion);
 		cat.getProvidedCapabilityList().add(pc);
 		return cat;
 	}
 
 	private List<InstallableUnit> getRepositoryCategories(MappedRepository repo) {
 		Builder builder = getBuilder();
 		ArrayList<InstallableUnit> categoryIUs = new ArrayList<InstallableUnit>();
 		if(repo.isMapExclusive()) {
 			for(Category category : repo.getCategories()) {
 				if(category.isEnabled()) {
 					IInstallableUnit iu = category.resolveAsSingleton();
 					if(builder.isTopLevelCategory(iu))
 						categoryIUs.add((InstallableUnit) iu);
 				}
 			}
 		}
 		else {
 			List<MapRule> mapRules = repo.getMapRules();
 			allIUs: for(InstallableUnit iu : repo.getMetadataRepository().getInstallableUnits()) {
 				if(builder.isTopLevelCategory(iu)) {
 					for(MapRule mapRule : mapRules)
 						if(mapRule instanceof ExclusionRule && iu.getId().equals(mapRule.getName())
 								&& mapRule.getVersionRange().isIncluded(iu.getVersion()))
 							continue allIUs;
 					categoryIUs.add(iu);
 				}
 			}
 		}
 
 		// Add all categories from this repository.
 		//
 		String categoryPrefix = StringUtils.trimmedOrNull(repo.getCategoryPrefix());
 		if(categoryPrefix != null) {
 			// All requirements for categories must be renamed.
 			//
 			String idSuffix = makeValidIUSuffix(categoryPrefix);
 			StringBuilder prefixConcat = new StringBuilder();
 			prefixConcat.append(categoryPrefix);
 			prefixConcat.append(' ');
 			int prefixLen = prefixConcat.length();
 			int idx = categoryIUs.size();
 			while(--idx >= 0) {
 				InstallableUnit iu = categoryIUs.get(idx);
 				InstallableUnitImpl renamedIU = (InstallableUnitImpl) InstallableUnitImpl.importToModel(iu);
 				prefixConcat.setLength(prefixLen);
 				prefixConcat.append(iu.getProperty(IInstallableUnit.PROP_NAME));
 				renamedIU.getPropertyMap().map().put(IInstallableUnit.PROP_NAME, prefixConcat.toString());
 				renamedIU.setId(iu.getId() + idSuffix);
 				assignCategoryVersion(renamedIU);
 				categoryIUs.set(idx, renamedIU);
 			}
 		}
 		return categoryIUs;
 	}
 
 	private String makeValidIUSuffix(String categoryPrefix) {
 		int top = categoryPrefix.length();
 		StringBuilder bld = new StringBuilder(top);
 		for(int idx = 0; idx < top; ++idx) {
 			char c = categoryPrefix.charAt(idx);
 			if(c >= 'A' && c <= 'Z')
 				bld.append(c + ('a' - 'A'));
 			else if(c == '-' || c == '.' || c == '_' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z'))
 				bld.append(c);
 			else if(c == ' ')
 				bld.append('_');
 		}
 		return bld.toString();
 	}
 
 	private List<InstallableUnit> normalizeCategories(List<InstallableUnit> categoryIUs) {
 		Map<IRequiredCapability, IRequiredCapability> replacementMap = getBuilder().getReplacementMap();
 		Map<String, List<RequiredCapability>> map = new HashMap<String, List<RequiredCapability>>();
 		Map<String, InstallableUnit> catMap = new HashMap<String, InstallableUnit>();
 		for(InstallableUnit category : categoryIUs) {
 			String name = category.getProperty(IInstallableUnit.PROP_NAME);
 			List<RequiredCapability> caps = map.get(name);
 			if(caps == null) {
 				caps = new ArrayList<RequiredCapability>();
 				map.put(name, caps);
 			}
 
 			for(RequiredCapability cap : category.getRequiredCapabilityList()) {
 				// If this is an exact version range, then check if the appointed
 				// version has been excluded. If it has, replace it if its replacement
 				// can be found.
 				//
 				VersionRange range = cap.getRange();
 				if(range != null && range.getMinimum().equals(range.getMaximum())) {
 					if(replacementMap.containsKey(cap)) {
 						IRequiredCapability newRq = replacementMap.get(cap);
 						if(newRq == null)
 							continue;
						cap = InstallableUnitImpl.importToModel(newRq);
 					}
 				}
 				caps.add(cap);
 			}
 
 			InstallableUnit oldCat = catMap.put(name, category);
 			if(oldCat == null)
 				continue;
 
 			// We keep the category with the longest description
 			//
 			String oldDesc = oldCat.getProperty(IInstallableUnit.PROP_DESCRIPTION);
 			if(oldDesc == null) {
 				// Toss the old one
 				tossCategory(oldCat);
 				continue;
 			}
 
 			String newDesc = category.getProperty(IInstallableUnit.PROP_DESCRIPTION);
 			if(newDesc == null || newDesc.length() < oldDesc.length()) {
 				// Retain old and throw away new.
 				tossCategory(category);
 				catMap.put(name, oldCat);
 			}
 			else {
 				if(category.getVersion().compareTo(oldCat.getVersion()) > 0)
 					// Toss the old one
 					tossCategory(oldCat);
 			}
 		}
 
 		List<InstallableUnit> normalized = new ArrayList<InstallableUnit>();
 		for(InstallableUnit category : catMap.values()) {
 			String name = category.getProperty(IInstallableUnit.PROP_NAME);
 			List<RequiredCapability> newCaps = map.get(name);
 			List<RequiredCapability> origCaps = category.getRequiredCapabilityList();
 			if(origCaps.size() == newCaps.size() && origCaps.containsAll(newCaps)) {
 				// This category passed through normalization without change
 				normalized.add(category);
 				continue;
 			}
 
 			InstallableUnitImpl newCategory = (InstallableUnitImpl) P2Factory.eINSTANCE.createInstallableUnit();
 			newCategory.setId(category.getId());
 			newCategory.getPropertyMap().addAll(category.getPropertyMap());
 			newCategory.getRequiredCapabilityList().addAll(newCaps);
 			assignCategoryVersion(newCategory);
 			tossCategory(category);
 			normalized.add(newCategory);
 		}
 		return normalized;
 	}
 
 	/**
 	 * Ensure that mapped repositories that reference the given category IU are not mapped verbatim.
 	 * 
 	 * @param category
 	 *            The category IU that will be excluded
 	 */
 	private void tossCategory(InstallableUnit category) {
 		MetadataRepository parent = (MetadataRepository) ((EObject) category).eContainer();
 		Builder builder = getBuilder();
 		for(Contribution contrib : builder.getAggregator().getContributions(true)) {
 			for(MappedRepository mappedRepo : contrib.getRepositories(true)) {
 				if(mappedRepo.getMetadataRepository() == parent && builder.isMapVerbatim(mappedRepo)) {
 					LogUtils.debug("Excluding %s from verbatim mapping since category %s has been normalized",
 							mappedRepo.getLocation(), category.getProperty(IInstallableUnit.PROP_NAME));
 					builder.addMappingExclusion(mappedRepo, MetadataFactory.createRequiredCapability(
 							IInstallableUnit.NAMESPACE_IU_ID, category.getId(), new VersionRange(category.getVersion(),
 									true, category.getVersion(), true), null, false, false), null);
 				}
 			}
 		}
 	}
 }
