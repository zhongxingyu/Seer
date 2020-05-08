 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.core.bundle;
 
 import static org.sourcepit.common.manifest.osgi.ParameterType.DIRECTIVE;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.constraints.NotNull;
 
 import org.eclipse.emf.common.util.EList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sourcepit.common.manifest.osgi.BundleManifest;
 import org.sourcepit.common.manifest.osgi.BundleManifestFactory;
 import org.sourcepit.common.manifest.osgi.PackageExport;
 import org.sourcepit.common.manifest.osgi.PackageImport;
 import org.sourcepit.common.manifest.osgi.Parameter;
 import org.sourcepit.common.manifest.osgi.ParameterType;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.common.manifest.osgi.VersionRange;
 import org.sourcepit.osgify.core.ee.AccessRule;
 import org.sourcepit.osgify.core.ee.ExecutionEnvironment;
 import org.sourcepit.osgify.core.ee.ExecutionEnvironmentService;
 import org.sourcepit.osgify.core.model.context.BundleCandidate;
 import org.sourcepit.osgify.core.model.context.BundleReference;
 import org.sourcepit.osgify.core.model.java.JavaResourceBundle;
 
 import com.google.common.collect.Multimap;
 
 /**
  * @author Bernd Vogt <bernd.vogt@sourcepit.org>
  */
 @Named
 public class PackageImportAppender
 {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageImportAppender.class);
 
    private ExecutionEnvironmentService execEnvService;
 
    private ReferencedPackagesService packagesService;
 
    @Inject
    public PackageImportAppender(ExecutionEnvironmentService execEnvService, ReferencedPackagesService packagesService)
    {
       this.execEnvService = execEnvService;
       this.packagesService = packagesService;
    }
 
    public void append(@NotNull BundleCandidate bundle)
    {
       final BundleManifest manifest = bundle.getManifest();
 
       final Map<String, PackageReference> determinePackagesToImport = determinePackagesToImport(bundle.getContent(),
          manifest);
 
       final List<String> importPackages = new ArrayList<String>(determinePackagesToImport.keySet());
       Collections.sort(importPackages);
 
       for (String packageName : importPackages)
       {
          final ExportDescription exportDescription = packagesService.determineExporter(bundle, packageName);
          if (exportDescription != null)
          {
             if (exportDescription.isPackageOfExecutionEnvironment()
                && exportDescription.getAccessRule() == AccessRule.DISCOURAGED)
             {
                LOGGER.warn("Skipping import of Execution Environment specific package {}",
                   exportDescription.getPackageName());
             }
             else
             {
                final PackageImport packageImport = BundleManifestFactory.eINSTANCE.createPackageImport();
                packageImport.getPackageNames().add(packageName);
                packageImport.setVersion(determineVersionRange(bundle, determinePackagesToImport.get(packageName)));
 
                final boolean optional = isOptional(bundle, packageName);
                if (optional)
                {
                   Parameter parameter = BundleManifestFactory.eINSTANCE.createParameter();
                  parameter.setName("optional");
                   parameter.setType(ParameterType.DIRECTIVE);
                  parameter.setValue(String.valueOf(optional));
                   packageImport.getParameters().add(parameter);
                }
 
                manifest.getImportPackage(true).add(packageImport);
             }
          }
       }
    }
 
    private VersionRange determineVersionRange(BundleCandidate bundle, PackageReference packageReference)
    {
       final ExportDescription exportDescription = packagesService.determineExporter(bundle,
          packageReference.requiredPackage);
       if (exportDescription != null)
       {
          return determineVersionRange(exportDescription, packageReference.demandedByPublicPackages,
             packageReference.demandedByInternalPackages);
       }
       return null;
    }
 
    public enum DemanderRole
    {
       PROVIDER, FRIEND, CONSUMER;
    }
 
    private static VersionRange determineVersionRange(final ExportDescription exportDescription,
       boolean demandedByPublicPackages, boolean demandedByInternalPackages)
    {
       final PackageExport packageExport = exportDescription.getPackageExport();
 
       if (exportDescription.isPackageOfExecutionEnvironment())
       {
          return null;
       }
       else
       {
          final boolean isSelfReference = exportDescription.isSelfReference();
 
          final DemanderRole role = determineRoleOfDemandingBundle(isSelfReference, demandedByPublicPackages,
             demandedByInternalPackages, exportDescription.getPackageName(),
             isInternalPackage(exportDescription.getPackageExport()));
 
          if (DemanderRole.FRIEND == role)
          {
             addFriendDirective(packageExport, exportDescription.getImportingBundle().getSymbolicName());
          }
 
          final Version packageVersion = packageExport.getVersion();
          if (packageVersion == null || Version.EMPTY_VERSION.equals(packageVersion))
          {
             return null;
          }
 
          final VersionRange referenceRange = isSelfReference ? null : exportDescription.getReferenceToExporter()
             .getVersionRange();
 
          return determineImportVersionRange(referenceRange, packageVersion, role);
       }
    }
 
 
    public static VersionRange determineImportVersionRange(VersionRange referenceRange, Version packageVersion,
       DemanderRole role)
    {
       Version l, h;
       if (referenceRange == null)
       {
          l = packageVersion;
          h = packageVersion;
       }
       else
       {
          if (!referenceRange.includes(packageVersion))
          {
             Version rl = referenceRange.getLowVersion();
             rl = new Version(rl.getMajor(), rl.getMinor(), -1);
             if (!new VersionRange(rl, referenceRange.isLowInclusive(), referenceRange.getHighVersion(),
                referenceRange.isHighInclusive()).includes(packageVersion))
             {
                referenceRange = new VersionRange(packageVersion, true, packageVersion, true);
             }
          }
          else if (referenceRange.getHighVersion() != null)
          {
             return referenceRange;
          }
 
          Version rl = referenceRange.getLowVersion();
          Version rh = referenceRange.getHighVersion();
          if (rh == null)
          {
             rh = rl;
          }
 
          l = min(rl, packageVersion);
          h = max(rh, packageVersion);
       }
 
       final VersionRange roleRange;
       switch (role)
       {
          case CONSUMER :
             roleRange = toConsumerRange(l, h);
             break;
          case FRIEND :
             roleRange = toFriendRange(l, h);
             break;
          case PROVIDER :
             roleRange = toProviderRange(l, h);
             break;
          default :
             throw new IllegalStateException();
       }
 
       return trimQualifiers(roleRange);
    }
 
    static VersionRange trimQualifiers(VersionRange range)
    {
       if (range != null)
       {
          final Version low = trimQualifier(range.getLowVersion());
          final Version high = trimQualifier(range.getHighVersion());
          return new VersionRange(low, range.isLowInclusive(), high, range.isHighInclusive());
       }
       return null;
    }
 
    private static Version trimQualifier(Version version)
    {
       if (version != null && version.getQualifier().length() > 0)
       {
          final int minor = version.getMinor();
          final int micro = version.getMicro();
          return new Version(version.getMajor(), minor == 0 && micro == 0 ? -1 : minor, micro == 0 ? -1 : micro);
       }
       return version;
    }
 
    private static VersionRange toProviderRange(Version lv, Version hv)
    {
       return new VersionRange(lv, true, new Version(hv.getMajor(), hv.getMinor() + 1, -1), false);
    }
 
    private static VersionRange toFriendRange(Version lv, Version hv)
    {
       return new VersionRange(lv, true, new Version(hv.getMajor(), hv.getMinor() + 1, -1), false);
    }
 
    private static VersionRange toConsumerRange(Version lv, Version hv)
    {
       return new VersionRange(lv, true, new Version(hv.getMajor() + 1, -1, -1), false);
    }
 
    private static Version max(Version v1, Version v2)
    {
       return v1.compareTo(v2) > 0 ? v1 : v2;
    }
 
    private static Version min(Version v1, Version v2)
    {
       return v1.compareTo(v2) < 0 ? v1 : v2;
    }
 
    private static void addFriendDirective(PackageExport packageExport, String symbolicName)
    {
       Parameter xInternal = packageExport.getParameter("x-internal");
       if (xInternal != null)
       {
          packageExport.getParameters().remove(xInternal);
       }
 
       Parameter xFriends = packageExport.getParameter("x-friends");
       if (xFriends == null)
       {
          xFriends = BundleManifestFactory.eINSTANCE.createParameter();
          xFriends.setName("x-friends");
          xFriends.setType(DIRECTIVE);
          xFriends.setQuoted(true);
          packageExport.getParameters().add(xFriends);
       }
 
       String value = xFriends.getValue();
       if (value == null)
       {
          value = symbolicName;
       }
       else
       {
          value = ", " + symbolicName;
       }
       xFriends.setValue(value);
    }
 
    private static DemanderRole determineRoleOfDemandingBundle(boolean isSelfReference,
       boolean demandedByPublicPackages, boolean demandedByInternalPackages, String packageName, boolean internal)
    {
       if (internal)
       {
          return isSelfReference ? DemanderRole.PROVIDER : DemanderRole.FRIEND;
       }
       else if (isSelfReference && demandedByInternalPackages)
       {
          return DemanderRole.PROVIDER;
       }
       else
       {
          return DemanderRole.CONSUMER;
       }
    }
 
    private boolean isOptional(BundleCandidate bundle, String packageName)
    {
       final ExportDescription exportDescription = packagesService.determineExporter(bundle, packageName);
       if (exportDescription != null)
       {
          if (exportDescription.isSelfReference())
          {
             return false;
          }
          else if (exportDescription.isPackageOfExecutionEnvironment())
          {
             return false;
          }
          else
          {
             final BundleReference referenceToExporter = exportDescription.getReferenceToExporter();
             return referenceToExporter.isOptional();
          }
       }
       return true;
    }
 
    private Map<String, PackageReference> determinePackagesToImport(JavaResourceBundle jBundle, BundleManifest manifest)
    {
       final Map<String, PackageReference> refs = new LinkedHashMap<String, PackageImportAppender.PackageReference>();
       addRequiredPackages(refs, jBundle, manifest);
       addOwnPackages(manifest, refs);
       filterExecutionEnvironmentPackages(refs, manifest.getBundleRequiredExecutionEnvironment());
       return refs;
    }
 
    private void addOwnPackages(BundleManifest manifest, final Map<String, PackageReference> refs)
    {
       final EList<PackageExport> exportPackage = manifest.getExportPackage();
       if (exportPackage != null)
       {
          for (PackageExport packageExport : exportPackage)
          {
             final boolean internalPackage = isInternalPackage(packageExport);
             for (String packageName : packageExport.getPackageNames())
             {
                PackageReference ref = refs.get(packageName);
                if (ref == null)
                {
                   ref = new PackageReference();
                   ref.requiredPackage = packageName;
                }
 
                if (internalPackage)
                {
                   ref.demandedByInternalPackages = true;
                }
                else
                {
                   ref.demandedByPublicPackages = true;
                }
             }
          }
       }
    }
 
    private void addRequiredPackages(final Map<String, PackageReference> refs, JavaResourceBundle jBundle,
       BundleManifest manifest)
    {
       final Multimap<String, String> requiredToDemandingPackages = packagesService
          .getRequiredToDemandingPackages(jBundle);
 
       final Set<String> internalPackages = new HashSet<String>();
       final Set<String> publicPackages = new HashSet<String>();
       collectExportedPackages(manifest, internalPackages, publicPackages);
 
       for (Entry<String, Collection<String>> entry : requiredToDemandingPackages.asMap().entrySet())
       {
          final PackageReference ref = new PackageReference();
          ref.requiredPackage = entry.getKey();
          for (String demandingPackage : entry.getValue())
          {
             if (publicPackages.contains(demandingPackage))
             {
                ref.demandedByPublicPackages = true;
             }
             else if (internalPackages.contains(demandingPackage))
             {
                ref.demandedByInternalPackages = true;
             }
          }
          refs.put(ref.requiredPackage, ref);
       }
    }
 
    private static void collectExportedPackages(BundleManifest manifest, Set<String> internalPackages,
       Set<String> publicPackages)
    {
       EList<PackageExport> exportPackage = manifest.getExportPackage();
       if (exportPackage != null)
       {
          for (PackageExport packageExport : exportPackage)
          {
             if (isInternalPackage(packageExport))
             {
                internalPackages.addAll(packageExport.getPackageNames());
             }
             else
             {
                publicPackages.addAll(packageExport.getPackageNames());
             }
          }
       }
    }
 
    private static boolean isInternalPackage(PackageExport packageExport)
    {
       final Parameter parameter = packageExport.getParameter("x-internal");
       return parameter != null && "true".equals(parameter.getValue());
    }
 
    private void filterExecutionEnvironmentPackages(Map<String, PackageReference> refs,
       EList<String> executionEnvironments)
    {
       if (executionEnvironments != null)
       {
          for (String execEnvId : executionEnvironments)
          {
             final ExecutionEnvironment executionEnvironment = execEnvService.getExecutionEnvironment(execEnvId);
             if (executionEnvironment == null)
             {
                LOGGER.warn("Unknow execution environment {}", execEnvId);
             }
             else
             {
                for (String packageName : executionEnvironment.getPackages())
                {
                   refs.remove(packageName);
                }
             }
          }
       }
    }
 
    private static class PackageReference
    {
       String requiredPackage;
 
       boolean demandedByInternalPackages, demandedByPublicPackages;
    }
 }
