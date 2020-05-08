 /*******************************************************************************
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 package org.eclipse.b3.aggregator.engine;
 
 import static java.lang.String.format;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Category;
 import org.eclipse.b3.aggregator.Configuration;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.ExclusionRule;
 import org.eclipse.b3.aggregator.InstallableUnitType;
 import org.eclipse.b3.aggregator.MapRule;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.ValidConfigurationsRule;
 import org.eclipse.b3.aggregator.engine.internal.RequirementUtils;
 import org.eclipse.b3.aggregator.util.InstallableUnitUtils;
 import org.eclipse.b3.aggregator.util.LogUtils;
 import org.eclipse.b3.aggregator.util.MonitorUtils;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.metadata.expression.ExpressionFactory;
 import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
 import org.eclipse.equinox.p2.metadata.expression.IExpression;
 import org.eclipse.equinox.p2.publisher.AbstractPublisherAction;
 import org.eclipse.equinox.p2.publisher.IPublisherInfo;
 import org.eclipse.equinox.p2.publisher.IPublisherResult;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.osgi.framework.Filter;
 
 /**
  * This action creates the feature that contains all features and bundles that are listed in the build contributions.
  * 
  * @see Builder#ALL_CONTRIBUTED_CONTENT_FEATURE
  */
 public class VerificationFeatureAction extends AbstractPublisherAction {
 	static class RepositoryRequirement {
 		MappedRepository repository;
 
 		IRequirement requirement;
 
 		boolean explicit;
 
 		public boolean equals(Object o) {
 			if(!(o instanceof RepositoryRequirement))
 				return false;
 
 			RepositoryRequirement other = (RepositoryRequirement) o;
 			return other.repository.equals(repository) && other.requirement.equals(requirement)
 					&& other.explicit == explicit;
 		}
 	}
 
 	private static Filter createFilter(List<Configuration> configs) {
 		List<Configuration> enabledConfigs = getEnabledConfigs(configs);
 
 		if(!(enabledConfigs == null || enabledConfigs.isEmpty())) {
 			StringBuilder filterBld = new StringBuilder();
 			if(enabledConfigs.size() > 1)
 				filterBld.append("(|");
 
 			for(Configuration config : enabledConfigs) {
 				filterBld.append("(&(osgi.os=");
 				filterBld.append(config.getOperatingSystem().getLiteral());
 				filterBld.append(")(osgi.ws=");
 				filterBld.append(config.getWindowSystem().getLiteral());
 				filterBld.append(")(osgi.arch=");
 				filterBld.append(config.getArchitecture().getLiteral());
 				filterBld.append("))");
 			}
 			if(enabledConfigs.size() > 1)
 				filterBld.append(')');
 			return ExpressionUtil.parseLDAP(filterBld.toString());
 		}
 		return null;
 	}
 
 	private static List<Configuration> getEnabledConfigs(List<Configuration> configs) {
 		List<Configuration> enabledConfigs = new ArrayList<Configuration>();
 
 		for(Configuration config : configs)
 			if(config.isEnabled())
 				enabledConfigs.add(config);
 
 		return enabledConfigs;
 	}
 
 	private final Builder builder;
 
 	private final IMetadataRepository mdr;
 
 	public VerificationFeatureAction(Builder builder, IMetadataRepository mdr) {
 		this.builder = builder;
 		this.mdr = mdr;
 	}
 
 	@Override
 	public IStatus perform(IPublisherInfo publisherInfo, IPublisherResult results, IProgressMonitor monitor) {
 		InstallableUnitDescription iu = new MetadataFactory.InstallableUnitDescription();
 		iu.setId(Builder.ALL_CONTRIBUTED_CONTENT_FEATURE);
 		iu.setVersion(Builder.ALL_CONTRIBUTED_CONTENT_VERSION);
 		iu.setProperty(InstallableUnitDescription.PROP_TYPE_GROUP, Boolean.TRUE.toString());
 		iu.addProvidedCapabilities(Collections.singletonList(createSelfCapability(iu.getId(), iu.getVersion())));
 
 		Map<String, Set<RepositoryRequirement>> required = new HashMap<String, Set<RepositoryRequirement>>();
 
 		boolean errorsFound = false;
 		List<Contribution> contribs = builder.getAggregator().getContributions();
 		SubMonitor subMon = SubMonitor.convert(monitor, 2 + contribs.size());
 		try {
 			Set<String> explicit = new HashSet<String>();
 			for(Contribution contrib : builder.getAggregator().getContributions(true)) {
 				ArrayList<String> errors = new ArrayList<String>();
 				for(MappedRepository repository : contrib.getRepositories(true)) {
 					List<IInstallableUnit> allIUs;
 
 					try {
 						allIUs = ResourceUtils.getMetadataRepository(repository).getInstallableUnits();
 					}
 					catch(CoreException e) {
 						errors.add(e.getMessage());
 						continue;
 					}
 
 					if(repository.isMapExclusive()) {
 						for(MappedUnit mu : repository.getUnits(true)) {
 							if(mu instanceof Category) {
								addCategoryContent(mu.resolveAsSingleton(true), repository, allIUs, required, errors,
 										explicit);
 								continue;
 							}
 							addRequirementFor(repository, mu.getRequiredCapability(), required, errors, explicit, true);
 						}
 					}
 					else {
 						// Verify that all products and features can be installed.
 						//
 						List<MapRule> mapRules = repository.getMapRules();
 						Map<Filter, List<IInstallableUnit>> preSelectedIUs = new HashMap<Filter, List<IInstallableUnit>>();
 						allIUs: for(IInstallableUnit riu : allIUs) {
 							// We assume that all groups that are not categories are either products or
 							// features.
 							//
 							InstallableUnitType riuType = InstallableUnitUtils.getType(riu);
 							if(riuType == InstallableUnitType.PRODUCT || riuType == InstallableUnitType.FEATURE) {
 								Filter filter = null;
 								for(MapRule rule : mapRules) {
 									if(riu.getId().equals(rule.getName())
 											&& rule.getVersionRange().isIncluded(riu.getVersion())) {
 										if(rule instanceof ExclusionRule) {
 											builder.addMappingExclusion(repository);
 											continue allIUs;
 										}
 										if(rule instanceof ValidConfigurationsRule)
 											filter = createFilter(((ValidConfigurationsRule) rule).getValidConfigurations());
 									}
 								}
 								List<IInstallableUnit> units = preSelectedIUs.get(filter);
 								if(units == null)
 									preSelectedIUs.put(filter, units = new ArrayList<IInstallableUnit>());
 								units.add(riu);
 							}
 						}
 
 						for(Map.Entry<Filter, List<IInstallableUnit>> entry : preSelectedIUs.entrySet())
 							for(IRequirement req : RequirementUtils.createAllAvailableVersionsRequirements(
 									entry.getValue(), entry.getKey()))
 								addRequirementFor(repository, req, required, errors, explicit, false);
 					}
 				}
 				if(errors.size() > 0) {
 					errorsFound = true;
 					builder.sendEmail(contrib, errors);
 				}
 				MonitorUtils.worked(subMon, 1);
 			}
 			if(errorsFound)
 				return new Status(IStatus.ERROR, Engine.PLUGIN_ID, "Features without repositories");
 
 			Set<IRequirement> rcList = new HashSet<IRequirement>();
 			for(Set<RepositoryRequirement> rcSet : required.values())
 				for(RepositoryRequirement req : rcSet)
 					rcList.add(req.requirement);
 
 			iu.setRequiredCapabilities(rcList.toArray(new IRequirement[rcList.size()]));
 
 			InstallableUnitDescription pdePlatform = new MetadataFactory.InstallableUnitDescription();
 			pdePlatform.setId(Builder.PDE_TARGET_PLATFORM_NAME);
 			pdePlatform.setVersion(Version.emptyVersion);
 			pdePlatform.addProvidedCapabilities(Collections.singletonList(MetadataFactory.createProvidedCapability(
 					Builder.PDE_TARGET_PLATFORM_NAMESPACE, pdePlatform.getId(), pdePlatform.getVersion())));
 
 			mdr.addInstallableUnits(new IInstallableUnit[] { MetadataFactory.createInstallableUnit(iu),
 					MetadataFactory.createInstallableUnit(pdePlatform) });
 			return Status.OK_STATUS;
 		}
 		finally {
 			MonitorUtils.done(subMon);
 		}
 	}
 
 	private void addCategoryContent(IInstallableUnit category, MappedRepository repository,
 			List<IInstallableUnit> allIUs, Map<String, Set<RepositoryRequirement>> required, List<String> errors,
 			Set<String> explicit) {
 		// We don't map categories verbatim here. They are added elsewhere. We do
 		// map their contents though.
 		requirements: for(IRequirement rc : category.getRequiredCapabilities()) {
 			for(IInstallableUnit riu : allIUs) {
 				if(riu.satisfies(rc)) {
 					if("true".equalsIgnoreCase(riu.getProperty(InstallableUnitDescription.PROP_TYPE_CATEGORY))) {
 						// Nested category
 						addCategoryContent(riu, repository, allIUs, required, errors, explicit);
 						continue requirements;
 					}
 
 					addRequirementFor(repository, riu, rc.getFilter(), required, errors, explicit, false);
 					continue requirements;
 				}
 			}
 
 			// Categorized IU is not found
 			//
 			String error = format("Category %s includes a requirement for %s that cannot be fulfilled",
 					category.getId(), rc);
 			errors.add(error);
 			LogUtils.error(error);
 		}
 	}
 
 	private void addRequirementFor(MappedRepository mr, IInstallableUnit iu, Filter filter,
 			Map<String, Set<RepositoryRequirement>> requirements, List<String> errors, Set<String> explicit,
 			boolean isExplicit) {
 		String id = iu.getId();
 		Version v = iu.getVersion();
 		VersionRange range = null;
 		if(!Version.emptyVersion.equals(v))
 			range = new VersionRange(v, true, v, true);
 
 		Filter iuFilter = filter;
 		Filter origFilter = iu.getFilter();
 		if(origFilter != null) {
 			if(filter != null)
 				iuFilter = ExpressionFactory.INSTANCE.filterExpression(ExpressionFactory.INSTANCE.and(
 						(IExpression) origFilter, (IExpression) filter));
 		}
 		IRequiredCapability rc = MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, id, range,
 				iuFilter, false, false);
 		IRequirement req = RequirementUtils.createMultiRangeRequirement(mr.getMetadataRepository(), rc);
 
 		addRequirementFor(mr, req, requirements, errors, explicit, isExplicit);
 	}
 
 	private void addRequirementFor(MappedRepository mr, IRequirement rc,
 			Map<String, Set<RepositoryRequirement>> requirements, List<String> errors, Set<String> explicit,
 			boolean isExplicit) {
 
 		String id = RequirementUtils.getName(rc);
 		RepositoryRequirement rq = new RepositoryRequirement();
 		rq.repository = mr;
 		rq.requirement = rc;
 		rq.explicit = isExplicit;
 
 		Set<RepositoryRequirement> repoReqs = requirements.get(id);
 		if(repoReqs == null)
 			requirements.put(id, repoReqs = new HashSet<RepositoryRequirement>());
 
 		repoReqs.add(rq);
 
 		// if just 1 requirement exists for this ID, there's nothing to do
 		if(repoReqs.size() == 1) {
 			if(isExplicit)
 				explicit.add(id);
 			return;
 		}
 
 		// Mark all involved repositories as affected (they can't be mapped verbatim any more)
 		for(RepositoryRequirement req : repoReqs)
 			builder.addMappingExclusion(req.repository);
 
 		if(explicit.contains(id)) {
 			if(!isExplicit) {
 				LogUtils.debug("%s excluded since it is already explicitly mapped", rc.getMatches());
 				// Remove the new one
 				repoReqs.remove(rq);
 			}
 			else {
 				String error;
 				boolean filtersAllTheSame = true;
 				for(RepositoryRequirement req : repoReqs) {
 					if(rc.getFilter() != null && !rc.getFilter().equals(req.requirement.getFilter())
 							|| rc.getFilter() == null && req.requirement.getFilter() != null) {
 						filtersAllTheSame = false;
 						break;
 					}
 				}
 
 				if(!filtersAllTheSame) {
 					error = format("%s is explicitly mapped more than once but with different configurations", id);
 					errors.add(error);
 
 					LogUtils.error(error);
 				}
 			}
 			return;
 		}
 
 		if(isExplicit) {
 			Iterator<RepositoryRequirement> itor = repoReqs.iterator();
 			while(itor.hasNext()) {
 				RepositoryRequirement req = itor.next();
 				if(req.equals(rq))
 					continue;
 
 				LogUtils.debug("%s excluded since it is explicitly mapped", req.requirement);
 				itor.remove();
 			}
 
 			explicit.add(id);
 			return;
 		}
 
 		// all mappings are implicit so far, then we replace requirements by unions of implicit requirements
 		// let's find the original one first (there should be only one shared requirement instance at the moment)
 		IRequirement orig = null;
 		for(RepositoryRequirement req : repoReqs) {
 			if(req.equals(rq))
 				continue;
 			orig = req.requirement;
 			break;
 		}
 
 		IRequirement union = RequirementUtils.versionUnion(orig, rq.requirement);
 
 		for(RepositoryRequirement req : repoReqs)
 			req.requirement = union;
 	}
 }
