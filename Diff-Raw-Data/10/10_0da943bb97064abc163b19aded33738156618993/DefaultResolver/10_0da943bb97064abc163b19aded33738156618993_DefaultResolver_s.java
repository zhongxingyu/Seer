 /**
  *
  * Copyright 2007-2009 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.papoose.core.resolver;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Version;
 
 import org.papoose.core.BundleGeneration;
 import org.papoose.core.FragmentGeneration;
 import org.papoose.core.FrameworkExtensionGeneration;
 import org.papoose.core.Generation;
 import org.papoose.core.Papoose;
 import org.papoose.core.PapooseException;
 import org.papoose.core.VersionRange;
 import org.papoose.core.Wire;
 import org.papoose.core.descriptions.ExportDescription;
 import org.papoose.core.descriptions.ImportDescription;
 import org.papoose.core.descriptions.RequireDescription;
 import org.papoose.core.descriptions.Resolution;
 import org.papoose.core.spi.Resolver;
 import org.papoose.core.spi.Solution;
 import static org.papoose.core.util.Assert.assertTrue;
 import org.papoose.core.util.ResolverUtils;
 import org.papoose.core.util.Util;
 
 
 /**
  * @version $Revision$ $Date$
  */
 public class DefaultResolver implements Resolver
 {
     private final static String CLASS_NAME = DefaultResolver.class.getName();
     private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
     private final Object lock = new Object();
     private final Set<Generation> bundles = new HashSet<Generation>();
     private final Map<String, List<BundleGeneration>> indexByPackage = new HashMap<String, List<BundleGeneration>>();
     private Papoose framework;
     private String[] bootDelegates;
 
 
     /**
      * {@inheritDoc}
      */
     public void start(Papoose framework) throws PapooseException
     {
         LOGGER.entering(CLASS_NAME, "start", framework);
 
         if (framework == null)
         {
             LOGGER.warning("Framework cannot be null");
             throw new IllegalArgumentException("Framework cannot be null");
         }
 
         synchronized (lock)
         {
             if (this.framework != null)
             {
                 LOGGER.warning("Framework has already started");
                 throw new IllegalStateException("Framework has already started");
             }
 
             this.framework = framework;
 
             String bootDelegateString = (String) framework.getProperty(Constants.FRAMEWORK_BOOTDELEGATION);
             bootDelegates = (bootDelegateString == null ? new String[]{ } : bootDelegateString.split(","));
 
             for (int i = 0; i < bootDelegates.length; i++)
             {
                 bootDelegates[i] = bootDelegates[i].trim();
                 if (bootDelegates[i].endsWith(".*")) bootDelegates[i] = bootDelegates[i].substring(0, bootDelegates[i].length() - 1);
             }
         }
 
         LOGGER.exiting(CLASS_NAME, "start");
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop()
     {
         LOGGER.entering(CLASS_NAME, "stop");
 
         synchronized (lock)
         {
             if (this.framework == null)
             {
                 LOGGER.warning("Framework has already stopped");
                 throw new IllegalStateException("Framework has already stopped");
             }
 
             bundles.clear();
             indexByPackage.clear();
             bootDelegates = null;
             framework = null;
         }
 
         LOGGER.exiting(CLASS_NAME, "stop");
     }
 
     /**
      * {@inheritDoc}
      */
     public void added(Generation generation)
     {
         LOGGER.entering(CLASS_NAME, "added", generation);
 
         synchronized (lock)
         {
             if (this.framework == null)
             {
                 IllegalStateException ise = new IllegalStateException("Framework has not started");
                 LOGGER.warning("Framework has not started");
                 LOGGER.throwing(CLASS_NAME, "stop", ise);
                 throw ise;
             }
 
             bundles.add(generation);
 
             if (generation instanceof BundleGeneration)
             {
                 LOGGER.finest("Bundle is an regular bundleGeneration");
 
                 for (ExportDescription exportDescription : generation.getArchiveStore().getExportDescriptions())
                 {
                     LOGGER.log(Level.FINEST, "Indexing", exportDescription);
 
                     for (String packageName : exportDescription.getPackageNames())
                     {
                         List<BundleGeneration> bundleList = indexByPackage.get(packageName);
                         if (bundleList == null) indexByPackage.put(packageName, bundleList = new ArrayList<BundleGeneration>());
 
                         bundleList.add((BundleGeneration) generation);
                     }
                 }
             }
         }
 
         LOGGER.exiting(CLASS_NAME, "added");
     }
 
     /**
      * {@inheritDoc}
      */
     public void removed(Generation generation)
     {
         LOGGER.entering(CLASS_NAME, "removed", generation);
 
         synchronized (lock)
         {
             if (this.framework == null)
             {
                 IllegalStateException ise = new IllegalStateException("Framework has not started");
                 LOGGER.warning("Framework has not started");
                 LOGGER.throwing(CLASS_NAME, "stop", ise);
                 throw ise;
             }
 
             bundles.remove(generation);
 
             if (generation instanceof BundleGeneration)
             {
                 LOGGER.finest("Bundle is a regular bundleGeneration");
 
                 BundleGeneration bundle = (BundleGeneration) generation;
                 for (ExportDescription exportDescription : bundle.getArchiveStore().getExportDescriptions())
                 {
                     for (String packageName : exportDescription.getPackageNames())
                     {
                         List<BundleGeneration> bundleList = indexByPackage.get(packageName);
                         if (bundleList == null) continue;
 
                         bundleList.remove(bundle);
 
                         if (bundleList.isEmpty()) indexByPackage.remove(packageName);
                     }
                 }
             }
         }
 
         LOGGER.exiting(CLASS_NAME, "removed");
     }
 
     /**
      * {@inheritDoc}
      */
     public Set<Solution> resolve(Generation generation) throws BundleException
     {
         LOGGER.entering(CLASS_NAME, "resolve", generation);
 
         if (generation == null) throw new IllegalArgumentException("Bundle cannot be null");
 
         synchronized (lock)
         {
             if (!bundles.contains(generation)) throw new IllegalArgumentException("Bundle does not belong to this framework instance");
             if (generation.getState() != Bundle.INSTALLED) throw new BundleException("Bundle not in INSTALLED STATE");
             if (framework == null) throw new IllegalStateException("Framework has not started");
 
             Set<Candidate> canonicalSet = ResolverUtils.collectCanonicalSet(bundles);
 
             CheckPoint result = null;
             try
             {
                 result = doResolve(new CheckPoint(generation, canonicalSet));
             }
             catch (IncompatibleException ie)
             {
                 LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
             }
 
             if (result == null) throw new BundleException("No consistent solution set found for " + generation.getBundleController());
 
             Set<Solution> solutions = extractSolutions(result);
 
             LOGGER.exiting(CLASS_NAME, "resolve", solutions);
 
             return solutions;
         }
     }
 
     public Set<Solution> resolve(BundleGeneration bundleGeneration, ImportDescription importDescription) throws BundleException
     {
         LOGGER.entering(CLASS_NAME, "resolve", new Object[]{ bundleGeneration, importDescription });
 
         if (bundleGeneration == null) throw new IllegalArgumentException("Bundle cannot be null");
         if (importDescription == null) throw new IllegalArgumentException("ImportDescription cannot be null");
 
         synchronized (lock)
         {
             if (!bundles.contains(bundleGeneration)) throw new IllegalArgumentException("Bundle does not belong to this framework instance");
             if ((bundleGeneration.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) != 0) throw new BundleException("Bundle not already resolved");
             if (framework == null) throw new IllegalStateException("Framework has not started");
 
             Set<Candidate> canonicalSet = ResolverUtils.collectCanonicalSet(bundles);
             CheckPoint result = null;
 
             try
             {
                 result = doResolveBundle(new CheckPoint(bundleGeneration, importDescription, canonicalSet));
             }
             catch (IncompatibleException ie)
             {
                 LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
             }
 
             if (result == null) throw new BundleException("No consistent solution set found");
 
             result.resolveCompleted();
 
             Set<Solution> solutions = extractSolutions(result);
 
             LOGGER.exiting(CLASS_NAME, "resolve", solutions);
 
             return solutions;
         }
     }
 
     private static Set<Solution> extractSolutions(CheckPoint result)
     {
         Set<Solution> solutions = new HashSet<Solution>();
 
         for (BoundHost candidateBundle : result.getResolved())
         {
             Set<Wire> wires = new HashSet<Wire>();
 
             for (CandidateWiring candidateWiring : candidateBundle.getCandidateWirings())
             {
                 wires.add(new Wire(candidateWiring.getPackageName(), candidateWiring.getExportDescription(), (BundleGeneration) candidateWiring.getCandidate().getGeneration()));
             }
 
             List<Solution.RequiredBundleWrapper> requiredBundles = new ArrayList<Solution.RequiredBundleWrapper>();
 
             for (RequiredBundleWrapper requiredBundle : candidateBundle.getCandidateRequiredBundles())
             {
                 BundleGeneration bundleGeneration = requiredBundle.getBundleGeneration();
 
                 for (ExportDescription description : bundleGeneration.getArchiveStore().getExportDescriptions())
                 {
                     for (String packageName : description.getPackageNames())
                     {
                         Wire wire = new Wire(packageName, description, bundleGeneration);
                         requiredBundles.add(new Solution.RequiredBundleWrapper(wire, requiredBundle.isReExport()));
                     }
                 }
             }
 
             List<FragmentGeneration> fragments = new ArrayList<FragmentGeneration>(candidateBundle.getFragments().size());
 
             for (FragmentGeneration boundFragment : candidateBundle.getFragments()) fragments.add(boundFragment);
 
             solutions.add(new Solution(candidateBundle.getBundleGeneration(), fragments, wires, requiredBundles));
         }
 
         return solutions;
     }
 
     /**
      * Dispatches and builds bound candidates
      * TODO: This is probably a recursion point as we pull in other unresolved bundles
      *
      * @param checkPoint current state in search space
      * @return a checkpoint with a set of solutions
      */
     private CheckPoint doResolve(CheckPoint checkPoint)
     {
         LOGGER.entering(CLASS_NAME, "doResolve", checkPoint);
 
         assert Thread.holdsLock(lock);
 
         List<UnBound> unResolved = checkPoint.getUnResolved();
 
         CheckPoint result = checkPoint;
         if (!unResolved.isEmpty())
         {
             UnBound unbound = unResolved.remove(0);
             Generation generation = unbound.getToBeResolved();
 
             if (generation instanceof BundleGeneration)
             {
                 BundleGeneration host = (BundleGeneration) generation;
                 List<FragmentGeneration> availableFragments = ResolverUtils.collectAvailableFragments(host, checkPoint.getUnused());
 
                 for (List<FragmentGeneration> fragments : Util.combinations(availableFragments))
                 {
                     try
                     {
                         result = doResolveBundle(checkPoint.newCheckPoint(host, fragments));
                     }
                     catch (IncompatibleException ie)
                     {
                         LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
                     }
 
                     if (result != null) break;
                 }
             }
             else if (generation instanceof FragmentGeneration)
             {
                 FragmentGeneration fragmentGeneration = (FragmentGeneration) generation;
                 List<Candidate> hostCandidates = ResolverUtils.collectAvailableHosts((FragmentGeneration) generation);
 
                 if (hostCandidates.isEmpty()) return null;
 
                 for (Candidate hostCandidate : hostCandidates)
                 {
                     if (hostCandidate instanceof Resolved)
                     {
                         try
                         {
                             Resolved resolvedHost = (Resolved) hostCandidate;
                             result = doResolveBundle(checkPoint.newCheckPoint(resolvedHost, fragmentGeneration));
                         }
                         catch (IncompatibleException ie)
                         {
                             LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
                         }
                     }
                     else if (hostCandidate instanceof UnBound)
                     {
                         UnBound unBound = (UnBound) hostCandidate;
 
                         try
                         {
                             result = doResolveBundle(checkPoint.newCheckPoint(unBound, fragmentGeneration));
                         }
                         catch (IncompatibleException ie)
                         {
                             LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
                         }
                     }
                     else
                     {
                         try
                         {
                             BoundHost bound = (BoundHost) hostCandidate;
                             result = doResolveBundle(checkPoint.newCheckPoint(bound, fragmentGeneration));
                         }
                         catch (IncompatibleException ie)
                         {
                             LOGGER.log(Level.FINEST, "Incompatible collection of host and fragments", ie);
                         }
                     }
 
                     if (result != null) break;
                 }
             }
             else if (generation instanceof FrameworkExtensionGeneration)
             {
             }
 
             if (result != null) result.resolveCompleted();
         }
 
         LOGGER.exiting(CLASS_NAME, "doResolve", result);
 
         return result;
     }
 
     /**
      * Iterate through the set of unresolved candidates
      *
      * @param checkPoint the checkpoint to iterate agaisnt
      * @return a checkpoint with a set of solutions or <code>null</code> if no solutions can be found
      */
     private CheckPoint doResolveBundle(CheckPoint checkPoint)
     {
         assert checkPoint != null;
         assert checkPoint.getResolving() != null;
 
         while ((checkPoint = resolveRequiredBundles(checkPoint)) != null && !checkPoint.isDone())
         {
             checkPoint = checkPoint.nextBundle();
         }
 
         return checkPoint;
     }
 
     private CheckPoint resolveRequiredBundles(CheckPoint checkPoint)
     {
         List<RequireDescription> requireDescriptions = checkPoint.getResolving().getRequireDescriptions();
 
         if (!requireDescriptions.isEmpty())
         {
             CheckPoint result = null;
             RequireDescription requireDescription = requireDescriptions.remove(0);
 
             for (CandidateBundle candidate : ResolverUtils.collectEligibleBundlesFromUsed(requireDescription, checkPoint))
             {
                 if (candidate instanceof Resolved)
                 {
                     try
                     {
                         Resolved resolvedHost = (Resolved) candidate;
                         result = doResolve(checkPoint.newCheckPointUsed(resolvedHost, requireDescription));
                     }
                     catch (IncompatibleException ie)
                     {
                         LOGGER.log(Level.FINEST, "Incompatible collection of host and required bundle", ie);
                     }
                 }
                 else if (candidate instanceof UnBound)
                 {
                     assert false;
                 }
                 else
                 {
                     try
                     {
                         BoundHost bound = (BoundHost) candidate;
                         result = doResolve(checkPoint.newCheckPoint(bound, requireDescription));
                     }
                     catch (IncompatibleException ie)
                     {
                         LOGGER.log(Level.FINEST, "Incompatible collection of host and required bundle", ie);
                     }
                 }
 
                 if (result != null) return result;
             }
 
             for (Candidate candidate : ResolverUtils.collectEligibleBundlesFromUnused(requireDescription, checkPoint))
             {
                 if (candidate instanceof Resolved)
                 {
                     try
                     {
                         Resolved resolvedHost = (Resolved) candidate;
                         result = doResolve(checkPoint.newCheckPointUnused(resolvedHost, requireDescription));
                     }
                     catch (IncompatibleException ie)
                     {
                         LOGGER.log(Level.FINEST, "Incompatible collection of host and required bundle", ie);
                     }
                 }
                 else if (candidate instanceof UnBound)
                 {
                     UnBound unBound = (UnBound) candidate;
                     result = doResolve(checkPoint.newCheckPoint(unBound, requireDescription));
                 }
                 else
                 {
                     assert false;
                 }
 
                 if (result != null) return result;
             }
 
             if (requireDescription.getResolution() == Resolution.MANDATORY) return null;
         }
 
         return resolveWires(checkPoint);
     }
 
     private CheckPoint resolveWires(CheckPoint checkPoint)
     {
         assert checkPoint != null;
         assert checkPoint.getResolving() != null;
 
         List<ImportDescriptionWrapper> imports = checkPoint.getResolving().getImports();
 
         if (!imports.isEmpty())
         {
             ImportDescriptionWrapper targetImport = imports.remove(0);
 
             boolean bootDelegate = false;
 
             String packageName = targetImport.getPackageName();
             for (String delegate : bootDelegates)
             {
                 if ((delegate.endsWith(".") && packageName.regionMatches(0, delegate, 0, delegate.length() - 1)) || packageName.equals(delegate))
                 {
                     bootDelegate = true;
                     break;
                 }
             }
 
             if (bootDelegate)
             {
                 CheckPoint result = resolveWires(checkPoint.newCheckPoint(targetImport));
 
                 if (result != null) return result;
             }
             else
             {
                 boolean importExportOverlap = false;
 
                 found:
                 for (ExportDescriptionWrapper exportDescriptionWrapper : checkPoint.getResolving().getExports())
                 {
                     for (String exportPackage : exportDescriptionWrapper.getExportDescription().getPackageNames())
                     {
                         if (targetImport.getPackageName().equals(exportPackage))
                         {
                             importExportOverlap = true;
                             break found;
                         }
                     }
                 }
 
                 for (ExportDescriptionWrapper candidateExport : ResolverUtils.collectEligibleExportsFromUsed(targetImport, checkPoint))
                 {
                     if (matches(targetImport, candidateExport))
                     {
                         Set<CandidateWiring> impliedCandidateWirings = collectImpliedConstraints(candidateExport.getExportDescription(), candidateExport.getCandidate());
 
                         if (isConsistent(checkPoint.getUsed(), impliedCandidateWirings))
                         {
                             // add a wire to the bundle being resolved
                             CandidateWiring candidateWiring = new CandidateWiring(targetImport.getPackageName(), candidateExport.getExportDescription(), candidateExport.getCandidate());
 
                             CheckPoint result = resolveWires(checkPoint.newCheckPoint(candidateWiring));
 
                             if (result != null)
                             {
                                 if (importExportOverlap) result.getResolving().removeExport(targetImport.getPackageName());
                                 return result;
                             }
                         }
                     }
                 }
 
                 for (ExportDescriptionWrapper candidateExport : ResolverUtils.collectEligibleExportsFromUnused(targetImport, checkPoint))
                 {
                     if (matches(targetImport, candidateExport))
                     {
                         Candidate candidate = candidateExport.getCandidate();
                         if (candidate instanceof Resolved)
                         {
                             Set<CandidateWiring> impliedCandidateWirings = collectImpliedConstraints(candidateExport.getExportDescription(), candidate);
 
                             if (isConsistent(checkPoint.getUsed(), impliedCandidateWirings))
                             {
                                 CandidateWiring candidateWiring = new CandidateWiring(targetImport.getPackageName(), candidateExport.getExportDescription(), candidateExport.getCandidate());
                                 Resolved resolved = (Resolved) candidate;
 
                                 // add a wire to the bundle being resolved
                                 // move resolved to used
                                 CheckPoint result = resolveWires(checkPoint.newCheckPoint(candidateWiring, resolved));
 
                                 if (result != null)
                                 {
                                     if (importExportOverlap) assertTrue(result.getResolving().removeExport(targetImport.getPackageName()));
                                     return result;
                                 }
                             }
                         }
                         else
                         {
                             CandidateWiring candidateWiring = new CandidateWiring(targetImport.getPackageName(), candidateExport.getExportDescription(), candidateExport.getCandidate());
                             UnBound unBound = (UnBound) candidate;
 
                             // add a wire to the bundle being resolved
                             // move the unbound to to be resolved
                             // add the unbound to used
                             CheckPoint result = resolveWires(checkPoint.newCheckPoint(candidateWiring, unBound));
 
                             if (result != null)
                             {
                                 if (importExportOverlap) assertTrue(result.getResolving().removeExport(targetImport.getPackageName()));
                                 return result;
                             }
                         }
                     }
                 }
 
                 for (ExportDescriptionWrapper candidateExport : ResolverUtils.collectEligibleExportsFromUnresolved(targetImport, checkPoint))
                 {
                     if (matches(targetImport, candidateExport))
                     {
                         Candidate candidate = candidateExport.getCandidate();
                         if (candidate instanceof Resolved)
                         {
                             assert false;
                         }
                         else
                         {
                             CandidateWiring candidateWiring = new CandidateWiring(targetImport.getPackageName(), candidateExport.getExportDescription(), candidateExport.getCandidate());
 
                             // add a wire to the bundle being resolved
                             // move the unbound to to be resolved
                             // add the unbound to used
                             CheckPoint result = resolveWires(checkPoint.newCheckPoint(candidateWiring));
 
                             if (result != null)
                             {
                                 if (importExportOverlap) assertTrue(result.getResolving().removeExport(targetImport.getPackageName()));
                                 return result;
                             }
                         }
                     }
                 }
 
                 if (importExportOverlap)
                 {
                     CheckPoint result = resolveWires(checkPoint.newCheckPoint(targetImport));
 
                     if (result != null) return result;
                 }
             }
 
             if (targetImport.isMandatory())
             {
                if (packageName.startsWith("javax.") || packageName.startsWith("org.osgi."))
                {
                    if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Unable to find a wire for " + packageName + ".  Adding it to org.osgi.framework.system.packages or org.osgi.framework.bootdelegation might solve this problem");
                }
 
                 return null;
             }
             else
             {
                 return resolveWires(checkPoint);
             }
         }
         else if (!checkPoint.isDone())
         {
             return doResolve(checkPoint);
         }
 
         return checkPoint;
     }
 
     static final Set<CandidateBundle> BUNDLES_SCANNED = new HashSet<CandidateBundle>();
 
     /**
      * Collect the implied constraints for a particular export for a particular
      * bundle.  The implied constraints are obtained by following the "uses"
      * directive.
      *
      * @param exportDescription the export description used to obtain package names
      * @param candidate         the starting bundle
      * @return the set of implied constraints
      */
     private Set<CandidateWiring> collectImpliedConstraints(ExportDescription exportDescription, Candidate candidate)
     {
         assert Thread.holdsLock(lock);
 
         try
         {
             return doCollectImpliedConstraints(exportDescription, candidate);
         }
         finally
         {
             BUNDLES_SCANNED.clear();
         }
     }
 
     private Set<CandidateWiring> doCollectImpliedConstraints(ExportDescription exportDescription, Candidate candidate)
     {
         assert Thread.holdsLock(lock);
 
         Set<CandidateWiring> result = new HashSet<CandidateWiring>();
 
         if (candidate instanceof CandidateBundle) return result;
 
         CandidateBundle candidateBundle = (CandidateBundle) candidate;
 
         if (BUNDLES_SCANNED.contains(candidateBundle))
         {
             return result;
         }
         else
         {
             BUNDLES_SCANNED.add(candidateBundle);
         }
 
         for (String packageName : exportDescription.getUses())
         {
             for (CandidateWiring wire : candidateBundle.getWirings())
             {
                 if (packageName.equals(wire.getPackageName()))
                 {
                     result.add(wire);
 
                     result.addAll(doCollectImpliedConstraints(wire.getExportDescription(), wire.getCandidate()));
                 }
             }
         }
 
         return result;
     }
 
     private static boolean isConsistent(Set<CandidateBundle> used, Set<CandidateWiring> impliedCandidateWirings)
     {
         Map<String, CandidateWiring> implied = new HashMap<String, CandidateWiring>(impliedCandidateWirings.size());
 
         for (CandidateWiring wire : impliedCandidateWirings) implied.put(wire.getPackageName(), wire);
 
         for (CandidateBundle candidate : used)
         {
             for (CandidateWiring candidateWiring : candidate.getWirings())
             {
                 String packageName = candidateWiring.getPackageName();
                 if (implied.containsKey(packageName) && candidate.getBundleGeneration() != implied.get(packageName).getCandidate().getGeneration()) return false;
             }
         }
 
         return true;
     }
 
     @SuppressWarnings({ "deprecation" })
     private static boolean matches(ImportDescriptionWrapper targetImport, ExportDescriptionWrapper candidateExport)
     {
         String importPackage = targetImport.getPackageName();
         Map<String, Object> importParameters = targetImport.getImportDescription().getParameters();
         ExportDescription exportDescription = candidateExport.getExportDescription();
         Map<String, Object> exportParameters = exportDescription.getParameters();
 
         for (String exportPackage : exportDescription.getPackageNames())
         {
             if (importPackage.equals(exportPackage))
             {
                 if (importParameters.containsKey(Constants.VERSION_ATTRIBUTE))
                 {
                     VersionRange range = (VersionRange) importParameters.get(Constants.VERSION_ATTRIBUTE);
                     if (!range.includes((Version) exportParameters.get(Constants.VERSION_ATTRIBUTE))) return false;
                 }
                 if (importParameters.containsKey(Constants.PACKAGE_SPECIFICATION_VERSION))
                 {
                     VersionRange range = (VersionRange) importParameters.get(Constants.PACKAGE_SPECIFICATION_VERSION);
                     if (!range.includes((Version) exportParameters.get(Constants.VERSION_ATTRIBUTE))) return false;
                 }
                 if (importParameters.containsKey(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE))
                 {
                     String symbolicName = (String) importParameters.get(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE);
                     if (!symbolicName.equals(candidateExport.getCandidate().getGeneration().getSymbolicName())) return false;
                 }
                 if (importParameters.containsKey(Constants.BUNDLE_VERSION_ATTRIBUTE))
                 {
                     VersionRange range = (VersionRange) importParameters.get(Constants.BUNDLE_VERSION_ATTRIBUTE);
                     if (!range.includes(candidateExport.getCandidate().getGeneration().getVersion())) return false;
                 }
 
                 Set<String> importKeys = new HashSet<String>(importParameters.keySet());
                 importKeys.removeAll(Arrays.asList(Constants.VERSION_ATTRIBUTE, Constants.PACKAGE_SPECIFICATION_VERSION, Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, Constants.BUNDLE_VERSION_ATTRIBUTE, Constants.MANDATORY_DIRECTIVE));
                 importKeys.removeAll(exportDescription.getMandatory());
 
                 for (String importKey : importKeys)
                 {
                     if (exportParameters.containsKey(importKey) && !importParameters.get(importKey).equals(exportParameters.get(importKey))) return false;
                 }
 
                 for (String mandatoryExportKey : exportDescription.getMandatory())
                 {
                     if (!importParameters.containsKey(mandatoryExportKey)) return false;
 
                     if (!exportParameters.get(mandatoryExportKey).equals(importParameters.get(mandatoryExportKey))) return false;
                 }
 
                 return true;
             }
         }
 
         return false;
     }
 }
