 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.util;
 
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitPatch;
 import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
 import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.QueryUtil;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 public class SpecialQueries {
 
 	/**
 	 * match every {@link IInstallableUnit} that describes an OSGi bundle.
 	 */
 	private static final IMatchExpression<IInstallableUnit> bundles = ExpressionUtil.getFactory().matchExpression(
 		ExpressionUtil.parse("providedCapabilities.exists(p | p.namespace == 'osgi.bundle')")); //$NON-NLS-1$
 
 	/**
 	 * match every {@link IInstallableUnit} that describes a feature.
 	 */
 	private static final IMatchExpression<IInstallableUnit> features = ExpressionUtil.getFactory().matchExpression(
		ExpressionUtil.parse("name == '*.feature.group'")); //$NON-NLS-1$
 
 	public static IQuery<IInstallableUnit> createBundleQuery() {
 		return QueryUtil.createMatchQuery(bundles);
 	}
 
 	public static IQuery<IInstallableUnit> createFeatureQuery() {
 		return QueryUtil.createMatchQuery(features);
 	}
 
 	public static IQuery<IInstallableUnit> createPatchApplicabilityQuery(IInstallableUnitPatch patch) {
 		return QueryUtil.createMatchQuery(
 			"$0.exists(rcs | rcs.all(rc | this ~= rc))", (Object) patch.getApplicabilityScope());
 	}
 
 	public static IQuery<IInstallableUnit> createProductQuery() {
 		return QueryUtil.createIUPropertyQuery(InstallableUnitDescription.PROP_TYPE_GROUP, "true");
 	}
 }
