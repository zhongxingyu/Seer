 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.userregion.internal.quasi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.ExportPackageDescription;
 import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.osgi.service.resolver.ResolverError;
 import org.eclipse.osgi.service.resolver.State;
 import org.eclipse.osgi.service.resolver.VersionConstraint;
 import org.eclipse.osgi.service.resolver.VersionRange;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.UsesAnalyser;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.UsesAnalyser.AnalysedUsesConflict;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 
 
 /**
  * Helper class that analyses resolution failures and generates a human-readable failure description.
  * <p/>
  * 
  * <strong>Concurrent Semantics</strong><br/>
  * 
  * Threadsafe.
  * 
  */
 public final class StandardResolutionFailureDetective implements ResolutionFailureDetective {
 
     private final UsesAnalyser usesAnalyser = new UsesAnalyser();
 
     private final PlatformAdmin platformAdmin;
 
     /**
      * Constructor for a new {@link ResolutionFailureDetective}.
      * 
      * @param platformAdmin the {@link org.eclipse.osgi.launch.Equinox Equinox} {@link PlatformAdmin} service.
      */
     public StandardResolutionFailureDetective(PlatformAdmin platformAdmin) {
         this.platformAdmin = platformAdmin;
     }
 
     /**
      * Generates a description of all the resolver errors for the supplied {@link Bundle} in the supplied {@link State}.
      */
     public String generateFailureDescription(State state, BundleDescription bundleDescription, ResolverErrorsHolder resolverErrorsHolder) {
 
         StringBuilder sb = new StringBuilder();
         sb.append("Cannot resolve: ").append(bundleDescription.getSymbolicName()).append("\n");
 
         // these resolver errors are for all unresolved bundles in the state:
         ResolverError[] resolverErrors = gatherResolverErrors(bundleDescription, state);
         resolverErrorsHolder.setResolverErrors(resolverErrors);
         if (resolverErrors.length > 0) {
             indent(sb, 1);
             sb.append("Resolver report:\n");
 
             for (ResolverError resolverError : resolverErrors) {
                 indent(sb, 2);
                 formatResolverError(resolverError, sb, state);
                 sb.append("\n");
             }
         } else {
             VersionConstraint[] unsatisfiedLeaves = this.platformAdmin.getStateHelper().getUnsatisfiedLeaves(
                 new BundleDescription[] { bundleDescription });
             if (unsatisfiedLeaves.length > 0) {
                 indent(sb, 1);
                 sb.append("Unsatisfied leaf constraints:\n");
 
                 for (VersionConstraint versionConstraint : unsatisfiedLeaves) {
                     if (!isOptional(versionConstraint)) {
                         indent(sb, 2);
                         formatConstraint(versionConstraint, sb);
                         sb.append("\n");
                     }
                 }
             }
         }
 
         return sb.toString();
 
     }
 
     private ResolverError[] gatherResolverErrors(BundleDescription bundleDescription, State state) {
         Set<ResolverError> resolverErrors = new LinkedHashSet<ResolverError>();
         Collections.addAll(resolverErrors, state.getResolverErrors(bundleDescription));
         BundleDescription[] bundles = state.getBundles();
         for (BundleDescription bd : bundles) {
             if (!bd.isResolved()) {
                 Collections.addAll(resolverErrors, state.getResolverErrors(bd));
             }
         }
         return resolverErrors.toArray(new ResolverError[resolverErrors.size()]);
     }
 
     /**
      * Finds the member of <code>candidates</code> that is the nearest match to <code>match</code>.
      * 
      * @param match the string to match against.
      * @param candidates the candidates to search.
      * @return the nearest match.
      */
     private String nearestMatch(String match) {
         Set<String> candidates = gatherExports();
         int nearestDistance = Integer.MAX_VALUE;
         String nearestMatch = null;
 
         for (String candidate : candidates) {
             int distance = calculateStringDistance(match, candidate);
             if (distance < nearestDistance) {
                 nearestDistance = distance;
                 nearestMatch = candidate;
             }
         }
 
         return nearestMatch;
     }
 
     /**
      * Calculate the distance between the given two Strings according to the Levenshtein algorithm.
      * 
      * @param s1 the first String
      * @param s2 the second String
      * @return the distance value
      */
     private final static int calculateStringDistance(String s1, String s2) {
         if (s1.isEmpty()) {
             return s2.length();
         }
         if (s2.isEmpty()) {
             return s1.length();
         }
         
         final int s2len = s2.length();
         final int s1len = s1.length();
       
         int d[][] = new int[s1len + 1][s2len + 1];
 
         for (int i = 0; i <= s1len; i++) {
             d[i][0] = i;
         }
         for (int j = 0; j <= s2len; j++) {
             d[0][j] = j;
         }
 
         for (int i = 1; i <= s1len; i++) {
             char s_i = s1.charAt(i - 1);
             for (int j = 1; j <= s2len; j++) {
                 int cost;
                 char t_j = s2.charAt(j - 1);
                 if (Character.toLowerCase(s_i) == Character.toLowerCase(t_j)) {
                     cost = 0;
                 } else {
                     cost = 1;
                 }
                 d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
             }
         }
 
         return d[s1len][s2len];
     }
 
     /**
      * Gathers all the package exports.
      * 
      * @return the exported packages.
      */
     private Set<String> gatherExports() {
         State state = this.platformAdmin.getState(false);
         ExportPackageDescription[] exportedPackages = state.getExportedPackages();
         Set<String> exports = new HashSet<String>(exportedPackages.length);
         for (ExportPackageDescription epd : exportedPackages) {
             exports.add(epd.getName());
         }
         return exports;
     }
 
     private void formatResolverError(ResolverError resolverError, StringBuilder sb, State state) {
         if (resolverError.getType() == ResolverError.IMPORT_PACKAGE_USES_CONFLICT) {
             formatUsesConflict(resolverError, sb, state);
         } else if (resolverError.getType() == ResolverError.MISSING_FRAGMENT_HOST) {
             formatMissingFragment(resolverError, sb);
         } else if (resolverError.getType() == ResolverError.FRAGMENT_CONFLICT) {
             formatFragmentConflict(resolverError, sb, state);
         } else {
             formatBasicResolverError(resolverError, sb);
         }
     }
 
     private void formatBasicResolverError(ResolverError resolverError, StringBuilder sb) {
         sb.append(this.getTypeDescription(resolverError.getType()));
        formatResolverErrorData(resolverError, sb);
        formatResolverErrorUnsatisfiedConstraint(resolverError, sb);
    }

    private void formatResolverErrorUnsatisfiedConstraint(ResolverError resolverError, StringBuilder sb) {
         VersionConstraint unsatisfiedConstraint = resolverError.getUnsatisfiedConstraint();
         if(unsatisfiedConstraint != null){
             formatMissingConstraintWithAttributes(resolverError, sb, unsatisfiedConstraint);
         } else {
             sb.append(" In bundle <").append(resolverError.getBundle()).append(">");
         }
     }
 
     private void formatMissingFragment(ResolverError resolverError, StringBuilder sb) {
         sb.append(this.getTypeDescription(resolverError.getType()));
        sb.append(" The affected fragment is ").append(resolverError.getBundle()).append(".");
        formatResolverErrorData(resolverError, sb);
        formatResolverErrorUnsatisfiedConstraint(resolverError, sb);
    }

    private void formatResolverErrorData(ResolverError resolverError, StringBuilder sb) {
        String data = resolverError.getData();
        if (data != null) {
            sb.append(" Resolver error data <").append(data).append(">.");
        }
     }
 
     private void formatUsesConflict(ResolverError resolverError, StringBuilder sb, State state) {
         VersionConstraint unsatisfiedConstraint = resolverError.getUnsatisfiedConstraint();
         BundleDescription bundle = resolverError.getBundle();
         sb.append("Uses violation: <").append(unsatisfiedConstraint)
         .append("> in bundle <").append(bundle).append("[").append(bundle.getBundleId()).append("]").append(">\n");
         
         AnalysedUsesConflict[] usesConflicts = this.usesAnalyser.getUsesConflicts(state, resolverError);            
         if (usesConflicts==null || usesConflicts.length==0) {
             indent(sb,3);
             sb.append(" Resolver reported uses conflict for import");
             formatConstraintAttributes(sb, unsatisfiedConstraint);
         } else {
             formatConflictsFound(sb, usesConflicts);
         }
     }
 
     private void formatMissingConstraintWithAttributes(ResolverError resolverError, StringBuilder sb, VersionConstraint unsatisfiedConstraint) {
         sb.append(" Caused by missing constraint in bundle <").append(resolverError.getBundle()).append(">\n");
         indent(sb, 3); sb.append(" constraint: <").append(unsatisfiedConstraint).append(">");
 
         formatConstraintAttributes(sb, unsatisfiedConstraint);
     }
 
     private void formatConstraintAttributes(StringBuilder sb, VersionConstraint unsatisfiedConstraint) {
         if (unsatisfiedConstraint instanceof ImportPackageSpecification) {
             ImportPackageSpecification importPackageSpecification = (ImportPackageSpecification) unsatisfiedConstraint;
             formatConstrainedBundleAttributes(sb, importPackageSpecification);
             Map<?,?> attributes = importPackageSpecification.getAttributes();
             if (attributes!=null && !attributes.isEmpty()) {
                 sb.append("\n"); 
                 indent(sb, 3); sb.append("with attributes ").append(attributes).append("\n");
             }
         }
     }
     
     private void formatConstrainedBundleAttributes(StringBuilder sb, ImportPackageSpecification importPackageSpecification) {
         String bundleSymbolicName = importPackageSpecification.getBundleSymbolicName();
         if (null!=bundleSymbolicName) {
             sb.append(" constrained to bundle <").append(bundleSymbolicName).append(">");
             VersionRange versionRange = importPackageSpecification.getBundleVersionRange();
             if (null!=versionRange) {
                 sb.append(" constrained bundle version range \"").append(versionRange).append("\"");
             }
         }
     }
 
     private void formatConflictsFound(StringBuilder sb, AnalysedUsesConflict[] usesConflicts) {
         indent(sb, 3);
         sb.append("Found conflicts:\n");
         for (AnalysedUsesConflict conflict : usesConflicts) {
             for (String line : conflict.getConflictStatement()) {
                 indent(sb,4); sb.append(line).append("\n");
             }
         }
     }
 
     private void formatFragmentConflict(ResolverError resolverError, StringBuilder sb, State state) {
         formatMissingFragment(resolverError, sb);
         List<BundleDescription> possibleHosts = findPossibleHosts(resolverError.getUnsatisfiedConstraint().getBundle().getHost(), state);
         if (!possibleHosts.isEmpty()) {
             sb.append("\n");
             indent(sb, 3);
             sb.append("Possible hosts:\n");
             for (BundleDescription possibleHost : possibleHosts) {
                 indent(sb, 4);
                 sb.append(possibleHost).append(" ").append(possibleHost.isResolved() ? "(resolved)" : "(not resolved)").append("\n");
             }
             indent(sb, 3);
             sb.append("Constraint conflict:\n");
             indent(sb, 4);
             sb.append(resolverError.getUnsatisfiedConstraint());
         }
     }
     
     private List<BundleDescription> findPossibleHosts(VersionConstraint hostSpecification, State state) {
         List<BundleDescription> possibleHosts = new ArrayList<BundleDescription>();
                 
         BundleDescription[] bundles = state.getBundles(hostSpecification.getName());
         if (bundles != null) {
             for (BundleDescription bundle : bundles) {
                 if (hostSpecification.getVersionRange().isIncluded(bundle.getVersion())) {
                     possibleHosts.add(bundle);
                 }
             }
         }
         
         return possibleHosts;
     }
 
 
     private void formatConstraint(VersionConstraint versionConstraint, StringBuilder sb) {
         String constraintInformation = versionConstraint.toString();
         String bundleInQuestion = versionConstraint.getBundle().toString();
         sb.append("Bundle: ").append(bundleInQuestion).append(" - ").append(constraintInformation);
         if (versionConstraint instanceof ImportPackageSpecification) {
             sb.append("\n");
             indent(sb, 3);
             sb.append("Did you mean: '").append(nearestMatch(versionConstraint.getName())).append("'?");
         }
     }
 
     private boolean isOptional(VersionConstraint versionConstraint) {
         if (versionConstraint instanceof ImportPackageSpecification) {
             ImportPackageSpecification ips = (ImportPackageSpecification) versionConstraint;
             return !ImportPackageSpecification.RESOLUTION_STATIC.equals(ips.getDirective(Constants.RESOLUTION_DIRECTIVE));
         }
         return false;
     }
 
     /**
      * Indent the supplied {@link StringBuilder} to the supplied <code>level</code>.
      * 
      * @param out the <code>StringBuilder</code> to indent.
      * @param level the indentation level.
      */
     private void indent(StringBuilder out, int level) {
         for (int n = 0; n < level; n++) {
             out.append("    ");
         }
     }
     
     /**
      * Provides a human readable string for any type of equinox resolver error.
      * Errors are defined in {@link org.eclipse.osgi.service.resolver.ResolverError} 
      * 
      * @param type
      * @return
      */
     private String getTypeDescription(int type) {
         switch(type){
             case ResolverError.MISSING_IMPORT_PACKAGE: return "An Import-Package could not be resolved.";
        
             case ResolverError.MISSING_REQUIRE_BUNDLE: return "A Require-Bundle could not be resolved.";
 
             case ResolverError.MISSING_FRAGMENT_HOST: return "A Fragment-Host could not be resolved.";
 
             case ResolverError.SINGLETON_SELECTION: return "The bundle could not be resolved because another singleton bundle was selected.";
 
             case ResolverError.FRAGMENT_CONFLICT: return "The fragment could not be resolved because of a constraint conflict with a host, possibly because the host is already resolved.";
 
             case ResolverError.IMPORT_PACKAGE_USES_CONFLICT: return "An Import-Package could not be resolved because of a uses directive conflict.";
             
             case ResolverError.REQUIRE_BUNDLE_USES_CONFLICT: return "A Require-Bundle could not be resolved because of a uses directive conflict.";
           
             case ResolverError.IMPORT_PACKAGE_PERMISSION: return "An Import-Package could not be resolved because the importing bundle does not have the correct permissions to import the package.";
 
             case ResolverError.EXPORT_PACKAGE_PERMISSION: return "An Import-Package could not be resolved because no exporting bundle has the correct permissions to export the package.";
 
             case ResolverError.REQUIRE_BUNDLE_PERMISSION: return "A Require-Bundle could not be resolved because the requiring bundle does not have the correct permissions to require the bundle.";
 
             case ResolverError.PROVIDE_BUNDLE_PERMISSION: return "A Require-Bundle could not be resolved because no bundle with the required symbolic name has the correct permissions to provied the required symbolic name.";
 
             case ResolverError.HOST_BUNDLE_PERMISSION: return "A Fragment-Host could not be resolved because no bundle with the required symbolic name has the correct permissions to host a fragment.";
 
             case ResolverError.FRAGMENT_BUNDLE_PERMISSION: return "A Fragment-Host could not be resolved because the fragment bundle does not have the correct permissions to be a fragment.";
 
             case ResolverError.PLATFORM_FILTER: return "A bundle could not be resolved because a platform filter did not match the runtime environment.";
 
             case ResolverError.MISSING_EXECUTION_ENVIRONMENT: return "A bundle could not be resolved because the required execution enviroment did not match the runtime environment.";
 
             case ResolverError.MISSING_GENERIC_CAPABILITY: return "A bundle could not be resolved because the required generic capability could not be resolved.";
 
             case ResolverError.NO_NATIVECODE_MATCH: return "A bundle could not be resolved because no match was found for the native code specification.";
 
             case ResolverError.INVALID_NATIVECODE_PATHS: return "A bundle could not be resolved because the matching native code paths are invalid.";
 
             case ResolverError.DISABLED_BUNDLE: return "A bundle could not be resolved because the bundle was disabled.";
             
             default: return "Unknown Error.";   
         }
     }
 }
