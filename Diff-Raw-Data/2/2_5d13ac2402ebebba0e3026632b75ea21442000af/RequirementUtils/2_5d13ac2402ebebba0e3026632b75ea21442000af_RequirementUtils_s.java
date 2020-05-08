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
 import org.eclipse.buckminster.osgi.filter.Filter;
 import org.eclipse.buckminster.osgi.filter.FilterFactory;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.metadata.expression.ExpressionFactory;
 import org.eclipse.equinox.internal.p2.metadata.expression.LDAPFilter;
 import org.eclipse.equinox.internal.p2.metadata.expression.MatchExpression;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
 import org.eclipse.equinox.p2.metadata.expression.IFilterExpression;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 import org.osgi.framework.InvalidSyntaxException;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 @SuppressWarnings("deprecation")
 public class RequirementUtils {
 
 	private static final VersionRange ANY_VERSION = VersionRange.emptyRange;
 
 	public static IRequirement[] createAllAvailableVersionsRequirements(List<IInstallableUnit> ius,
 			final IMatchExpression<IInstallableUnit> filter) {
 
 		Map<String, Set<IInstallableUnit>> iuMap = new HashMap<String, Set<IInstallableUnit>>();
 		for(IInstallableUnit iu : ius) {
 			Set<IInstallableUnit> iuSet = iuMap.get(iu.getId());
 			if(iuSet == null)
 				iuMap.put(iu.getId(), iuSet = new HashSet<IInstallableUnit>());
 			iuSet.add(iu);
 		}
 
 		IRequirement[] requirements = new IRequirement[iuMap.size()];
 
 		int i = 0;
 		for(Map.Entry<String, Set<IInstallableUnit>> iuEntry : iuMap.entrySet()) {
 			String name = iuEntry.getKey();
 			IMatchExpression<IInstallableUnit> requirementFilter = filter;
 			for(IInstallableUnit iu : iuEntry.getValue()) {
 				IMatchExpression<IInstallableUnit> iuFilter = iu.getFilter();
 				if(iuFilter == null)
 					continue;
 				if(requirementFilter == null) {
 					requirementFilter = iuFilter;
 					continue;
 				}
 				if(requirementFilter.equals(iuFilter))
 					continue;
 
 				// TODO: See if merge produces one of the filters. If so, use that
 
 				Object[] p1 = iuFilter.getParameters();
 				Object[] p2 = requirementFilter.getParameters();
 				if(p1.length == 1 && p2.length == 1 && p1[0] instanceof LDAPFilter && p2[0] instanceof LDAPFilter) {
 					// Attempt an LDAPFilter merge.
 					LDAPFilter f1 = (LDAPFilter) p1[0];
 					LDAPFilter f2 = (LDAPFilter) p2[0];
 					try {
 						// Perform buckminster style LDAP merge
 						Filter bf1 = FilterFactory.newInstance(f1.toString());
 						Filter bf2 = FilterFactory.newInstance(f2.toString());
 						Filter t1 = bf1.addFilterWithOr(bf2);
 						IFilterExpression expr = ExpressionUtil.parseLDAP(t1.toString());
 						requirementFilter = ExpressionUtil.getFactory().matchExpression(
 							((MatchExpression<IInstallableUnit>) iuFilter).getOperand(), new Object[] { expr });
 						continue;
 					}
 					catch(InvalidSyntaxException e) {
 						// Should never happen, but just in case...
 						throw new RuntimeException(e);
 					}
 				}
 				Object[] compoundParams = new Object[p1.length + p2.length];
 				System.arraycopy(p1, 0, compoundParams, 0, p1.length);
 				System.arraycopy(p2, 0, compoundParams, p1.length, p2.length);
 				requirementFilter = ExpressionFactory.INSTANCE.matchExpression(
 					ExpressionFactory.INSTANCE.or(iuFilter, requirementFilter), compoundParams);
 			}
 
 			// TODO Use this to activate the "version enumeration" policy workaround
 			// requirements[i++] = new MultiRangeRequirement(name, namespace, iuEntry.getValue(), null, filter);
 
 			requirements[i++] = MetadataFactory.createRequirement(
 				IInstallableUnit.NAMESPACE_IU_ID, name, ANY_VERSION, requirementFilter, false, false);
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
