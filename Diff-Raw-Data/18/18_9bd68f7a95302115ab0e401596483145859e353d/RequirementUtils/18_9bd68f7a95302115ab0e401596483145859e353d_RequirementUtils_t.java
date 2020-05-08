 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.engine.internal;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.util.LogUtils;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.metadata.expression.ExpressionFactory;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 @SuppressWarnings("deprecation")
 public class RequirementUtils {
 
 	private static final VersionRange ANY_VERSION = VersionRange.emptyRange;
 
 	public static IRequirement[] createAllAvailableVersionsRequirements(List<IInstallableUnit> ius,
			final IMatchExpression<IInstallableUnit> filter) {
 		Map<String, Set<Version>> versionMap = new HashMap<String, Set<Version>>();
 		Map<String, Set<IMatchExpression<IInstallableUnit>>> filterMap = new HashMap<String, Set<IMatchExpression<IInstallableUnit>>>();
 		for(IInstallableUnit iu : ius) {
 			Set<Version> versionSet = versionMap.get(iu.getId());
 			if(versionSet == null) {
 				versionMap.put(iu.getId(), versionSet = new HashSet<Version>());
 			}
 			Set<IMatchExpression<IInstallableUnit>> filterSet = filterMap.get(iu.getId());
 			if(filterSet == null) {
 				filterMap.put(iu.getId(), filterSet = new HashSet<IMatchExpression<IInstallableUnit>>());
 			}
 
 			versionSet.add(iu.getVersion());
 			filterSet.add(iu.getFilter());
 		}
 
 		IRequirement[] requirements = new IRequirement[versionMap.size()];
 
 		int i = 0;
 		for(Map.Entry<String, Set<Version>> iuEntry : versionMap.entrySet()) {
 			String name = iuEntry.getKey();
 			String namespace = IInstallableUnit.NAMESPACE_IU_ID;
 
 			IMatchExpression<IInstallableUnit> inheritedFilter = null;
 
 			for(IMatchExpression<IInstallableUnit> iuFilter : filterMap.get(name)) {
 				if(inheritedFilter == null)
 					inheritedFilter = iuFilter;
 				else if(!inheritedFilter.equals(iuFilter)) {
 					LogUtils.info("More than one filter definition found on %s; using an empty filter", name);
 					inheritedFilter = null;
 					break;
 				}
 			}
 
			IMatchExpression<IInstallableUnit> requirementFilter = filter;
 			if(inheritedFilter != null) {
				if(requirementFilter == null)
					requirementFilter = inheritedFilter;
 				else {
 					Object[] inheritedFilterParams = inheritedFilter.getParameters();
 					Object[] filterParams = filter.getParameters();
 					Object[] compoundParams = new Object[inheritedFilterParams.length + filterParams.length];
 					System.arraycopy(inheritedFilterParams, 0, compoundParams, 0, inheritedFilterParams.length);
 					System.arraycopy(filterParams, 0, compoundParams, inheritedFilterParams.length, filterParams.length);
					requirementFilter = ExpressionFactory.INSTANCE.matchExpression(
						ExpressionFactory.INSTANCE.and(inheritedFilter, filter), compoundParams);
 				}
 			}
 
 			// TODO Use this to activate the "version enumeration" policy workaround
 			// requirements[i++] = new MultiRangeRequirement(name, namespace, iuEntry.getValue(), null, filter);
 
			requirements[i++] = MetadataFactory.createRequirement(
				namespace, name, ANY_VERSION, requirementFilter, false, false);
 		}
 
 		return requirements;
 	}
 
 	public static IRequirement createMultiRangeRequirement(MetadataRepository mdr, IRequirement req) {
 		Set<Version> matchingVersions = new HashSet<Version>();
 		String name = null;
 		String namespace = IInstallableUnit.NAMESPACE_IU_ID;
 		for(IInstallableUnit iu : mdr.getInstallableUnits()) {
 			if(req.isMatch(iu)) {
 				matchingVersions.add(iu.getVersion());
 
 				if(name == null)
 					name = iu.getId();
 				else if(!name.equals(iu.getId()))
 					throw new RuntimeException("Requirement must contain strict name filter");
 
 			}
 		}
 
 		return new MultiRangeRequirement(name, namespace, matchingVersions, null, req.getFilter());
 	}
 
 	/**
 	 * Retrieves IU name from a requirement
 	 * 
 	 * @param req
 	 * @return the name or null if this method is unable to figure it out
 	 */
 	public static String getName(IRequirement req) {
 		if(req instanceof IRequiredCapability)
 			return ((IRequiredCapability) req).getName();
 		else if(req instanceof MultiRangeRequirement)
 			return ((MultiRangeRequirement) req).getName();
 
 		throw new RuntimeException("Unable to extrace IU name from requirement of class " + req.getClass().getName());
 	}
 
 	/**
 	 * @param req1
 	 * @param req2
 	 * @return
 	 */
 	public static IRequirement versionUnion(IRequirement req1, IRequirement req2) {
 		if(req1 instanceof MultiRangeRequirement && req2 instanceof MultiRangeRequirement) {
 			MultiRangeRequirement vreq1 = (MultiRangeRequirement) req1;
 			MultiRangeRequirement vreq2 = (MultiRangeRequirement) req2;
 
 			if(!vreq1.getName().equals(vreq2.getName()))
 				throw new RuntimeException(
 					"Unable to create a version union of expressions with different name requests");
 			if(!vreq1.getNamespace().equals(vreq2.getNamespace()))
 				throw new RuntimeException(
 					"Unable to create a version union of expressions with different namespace requests");
 			IMatchExpression<IInstallableUnit> f1 = vreq1.getFilter();
 			IMatchExpression<IInstallableUnit> f2 = vreq2.getFilter();
 
 			if(f1 != null && !f1.equals(f2) || f1 == null && f2 != null)
 				throw new RuntimeException("Unable to create a version union of expressions with different filters");
 
 			Set<Version> allVersions = new HashSet<Version>();
 			allVersions.addAll(vreq1.getVersions());
 			allVersions.addAll(vreq2.getVersions());
 
 			return new MultiRangeRequirement(vreq1.getName(), vreq1.getNamespace(), allVersions, null, f1);
 		}
 
 		throw new RuntimeException("Unable to create a version union of expressions: " + req1 + ", " + req2);
 	}
 }
