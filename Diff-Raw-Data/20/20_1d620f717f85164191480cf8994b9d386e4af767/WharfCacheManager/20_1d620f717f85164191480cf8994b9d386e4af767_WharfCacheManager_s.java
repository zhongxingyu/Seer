 /*
  *
  *  Copyright (C) 2010 JFrog Ltd.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * /
  */
 
 package org.jfrog.wharf.ivy.cache;
 
 
 import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.cache.*;
 import org.apache.ivy.core.module.descriptor.Artifact;
 import org.apache.ivy.core.module.descriptor.DefaultArtifact;
 import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
 import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
 import org.apache.ivy.core.module.id.ModuleRevisionId;
 import org.apache.ivy.core.module.id.ModuleRules;
 import org.apache.ivy.core.report.ArtifactDownloadReport;
 import org.apache.ivy.core.report.DownloadStatus;
 import org.apache.ivy.core.report.MetadataArtifactDownloadReport;
 import org.apache.ivy.core.resolve.ResolvedModuleRevision;
 import org.apache.ivy.core.settings.IvySettings;
 import org.apache.ivy.plugins.IvySettingsAware;
import org.apache.ivy.plugins.matcher.*;
 import org.apache.ivy.plugins.namespace.NameSpaceHelper;
 import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
 import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
 import org.apache.ivy.plugins.parser.ParserSettings;
 import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
 import org.apache.ivy.plugins.repository.ArtifactResourceResolver;
 import org.apache.ivy.plugins.repository.Resource;
 import org.apache.ivy.plugins.repository.ResourceDownloader;
 import org.apache.ivy.plugins.repository.ResourceHelper;
 import org.apache.ivy.plugins.resolver.AbstractResolver;
 import org.apache.ivy.plugins.resolver.ChainResolver;
 import org.apache.ivy.plugins.resolver.DependencyResolver;
 import org.apache.ivy.plugins.resolver.util.ResolvedResource;
 import org.apache.ivy.util.ChecksumHelper;
 import org.apache.ivy.util.FileUtil;
 import org.apache.ivy.util.Message;
 import org.jfrog.wharf.ivy.model.ArtifactMetadata;
 import org.jfrog.wharf.ivy.model.ModuleRevisionMetadata;
 import org.jfrog.wharf.ivy.model.WharfResolverMetadata;
 import org.jfrog.wharf.ivy.util.WharfUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * @author Tomer Cohen
  */
 public class WharfCacheManager implements RepositoryCacheManager, IvySettingsAware {
     private static final String DEFAULT_ARTIFACT_PATTERN =
             "[organisation]/[module](/[branch])/[resolverId]/[type]s/[artifact]-[revision](-[classifier])(.[ext])";
 
     private static final String DEFAULT_IVY_PATTERN =
             "[organisation]/[module](/[branch])/[resolverId]/ivy-[revision].xml";
 
     private static final int DEFAULT_MEMORY_CACHE_SIZE = 150;
 
     private IvySettings settings;
 
     private File basedir;
 
     private String name;
 
     private String changingPattern;
 
     private String changingMatcherName = PatternMatcher.EXACT_OR_REGEXP;
 
     private Boolean checkmodified;
 
     private ModuleRules/*<Long>*/ ttlRules = new ModuleRules();
 
     private Long defaultTTL = null;
 
     private ModuleDescriptorMemoryCache memoryModuleDescrCache;
 
     private ResolverHandler resolverHandler;
 
     private CacheMetadataHandler metadataHandler;
 
     public static WharfCacheManager newInstance(IvySettings ivySettings) {
         return newInstance(ivySettings, null, null);
     }
 
     public static WharfCacheManager newInstance(IvySettings ivySettings, String name, File baseDir) {
         WharfCacheManager result = new WharfCacheManager();
         if (name == null) {
             result.setName("wharf-cache");
         } else {
             result.setName(name);
         }
         result.setBasedir(baseDir);
         result.setSettings(ivySettings);
         return result;
     }
 
     public WharfCacheManager() {
     }
 
     public IvySettings getSettings() {
         return settings;
     }
 
     @Override
     public void setSettings(IvySettings settings) {
         this.settings = settings;
         settingsChanged();
     }
 
     private void settingsChanged() {
         metadataHandler = null;
         resolverHandler = null;
     }
 
     public CacheMetadataHandler getMetadataHandler() {
         if (metadataHandler == null) {
             metadataHandler = new CacheMetadataHandler(getBasedir(), settings);
         }
         return metadataHandler;
     }
 
     public ResolverHandler getResolverHandler() {
         if (resolverHandler == null) {
             resolverHandler = new ResolverHandler(getBasedir(), settings);
         }
         return resolverHandler;
     }
 
     public File getIvyFileInCache(ModuleRevisionId mrid, String resolverId) {
         Artifact artifact = DefaultArtifact.newIvyArtifact(mrid, null);
         artifact = ArtifactMetadata.fillResolverId(artifact, resolverId);
         String file = IvyPatternHelper.substitute(DEFAULT_IVY_PATTERN, artifact);
         return new File(getRepositoryCacheRoot(), file);
     }
 
     public File getBasedir() {
         if (basedir == null) {
             return settings.getDefaultRepositoryCacheBasedir();
         }
         return basedir;
     }
 
     public void setBasedir(File cache) {
         this.basedir = cache;
         settingsChanged();
     }
 
     public long getDefaultTTL() {
         if (defaultTTL == null) {
             defaultTTL = parseDuration(settings.getVariable("ivy.cache.ttl.default"));
         }
         return defaultTTL;
     }
 
     public void setDefaultTTL(long defaultTTL) {
         this.defaultTTL = defaultTTL;
     }
 
     public void setDefaultTTL(String defaultTTL) {
         this.defaultTTL = parseDuration(defaultTTL);
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getChangingMatcherName() {
         return changingMatcherName;
     }
 
     public void setChangingMatcher(String changingMatcherName) {
         this.changingMatcherName = changingMatcherName;
     }
 
     public String getChangingPattern() {
         return changingPattern;
     }
 
     public void setChangingPattern(String changingPattern) {
         this.changingPattern = changingPattern;
     }
 
     public void addTTL(Map attributes, PatternMatcher matcher, long duration) {
         ttlRules.defineRule(new MapMatcher(attributes, matcher), duration);
     }
 
     public void addConfiguredTtl(Map<String, String> attributes) {
         String duration = attributes.remove("duration");
         if (duration == null) {
             throw new IllegalArgumentException("'duration' attribute is mandatory for ttl");
         }
         String matcher = attributes.remove("matcher");
         addTTL(
                 attributes,
                 matcher == null ? ExactPatternMatcher.INSTANCE : settings.getMatcher(matcher),
                 parseDuration(duration));
     }
 
     public void setMemorySize(int size) {
         memoryModuleDescrCache = new ModuleDescriptorMemoryCache(size);
     }
 
     public ModuleDescriptorMemoryCache getMemoryCache() {
         if (memoryModuleDescrCache == null) {
             memoryModuleDescrCache = new ModuleDescriptorMemoryCache(DEFAULT_MEMORY_CACHE_SIZE);
         }
         return memoryModuleDescrCache;
     }
 
 
     private static final Pattern DURATION_PATTERN
             = Pattern.compile("(?:(\\d+)d)? ?(?:(\\d+)h)? ?(?:(\\d+)m)? ?(?:(\\d+)s)? ?(?:(\\d+)ms)?");
 
     private static final int MILLIS_IN_SECONDS = 1000;
     private static final int MILLIS_IN_MINUTES = 60 * MILLIS_IN_SECONDS;
     private static final int MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTES;
     private static final int MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;
 
     private long parseDuration(String duration) {
         if (duration == null) {
             return 0;
         }
         if ("eternal".equals(duration)) {
             return Long.MAX_VALUE;
         }
         java.util.regex.Matcher m = DURATION_PATTERN.matcher(duration);
         if (m.matches()) {
             //CheckStyle:MagicNumber| OFF
             int days = getGroupIntValue(m, 1);
             int hours = getGroupIntValue(m, 2);
             int minutes = getGroupIntValue(m, 3);
             int seconds = getGroupIntValue(m, 4);
             int millis = getGroupIntValue(m, 5);
             //CheckStyle:MagicNumber| ON
 
             return days * MILLIS_IN_DAY
                     + hours * MILLIS_IN_HOUR
                     + minutes * MILLIS_IN_MINUTES
                     + seconds * MILLIS_IN_SECONDS
                     + millis;
         } else {
             throw new IllegalArgumentException("invalid duration '"
                     + duration + "': it must match " + DURATION_PATTERN.pattern()
                     + " or 'eternal'");
         }
     }
 
     private int getGroupIntValue(java.util.regex.Matcher m, int groupNumber) {
         String g = m.group(groupNumber);
         return g == null || g.length() == 0 ? 0 : Integer.parseInt(g);
     }
 
     /**
      * True if this cache should check lastmodified date to know if ivy files are up to date.
      *
      * @return
      */
     public boolean isCheckmodified() {
         if (checkmodified == null) {
             if (getSettings() != null) {
                 String check = getSettings().getVariable("ivy.resolver.default.check.modified");
                 return check != null ? Boolean.valueOf(check) : false;
             } else {
                 return false;
             }
         } else {
             return checkmodified;
         }
     }
 
     public void setCheckmodified(boolean check) {
         checkmodified = check;
     }
 
     /**
      * True if this cache should use artifacts original location when possible, false if they should be copied to
      * cache.
      */
     public boolean isUseOrigin() {
         return false;
     }
 
     /**
      * Returns a File object pointing to where the artifact can be found on the local file system. This is usually in
      * the cache, but it can be directly in the repository if it is local and if the resolve has been done with
      * useOrigin = true
      */
     public File getArchiveFileInCache(Artifact artifact) {
         String resolverId = ArtifactMetadata.extractResolverId(artifact);
         if (resolverId == null || resolverId.isEmpty()) {
             throw new IllegalArgumentException("Artifact " + artifact.getId() + " does not have a resolver id");
         }
         ArtifactOrigin origin = getSavedArtifactOrigin(artifact);
         return getArchiveFileInCache(artifact, origin);
     }
 
     public File getStorageFile(String checksum) {
         checksum = WharfUtils.getCleanChecksum(checksum);
         return new File(getBasedir() + "/filestore", checksum.substring(0, 2) + "/" + checksum.substring(2, 4) + "/" +
                 checksum.substring(4, 6) + "/" + checksum);
     }
 
     private ArtifactMetadata findArtifactMetadata(Artifact artifact, ArtifactOrigin origin) {
         ModuleRevisionMetadata mrm =
                 getMetadataHandler().getModuleRevisionMetadata(artifact.getModuleRevisionId());
         if (mrm == null) {
             return null;
         }
         ArtifactMetadata artMd = new ArtifactMetadata(artifact, origin);
         for (ArtifactMetadata artifactMetadata : mrm.artifactMetadata) {
             if (artifactMetadata.equals(artMd)) {
                 return artifactMetadata;
             }
         }
         return null;
     }
 
     /**
      * Returns a File object pointing to where the artifact can be found on the local file system, using or not the
      * original location depending on the availability of origin information provided as parameter and the setting of
      * useOrigin. If useOrigin is false, this method will always return the file in the cache.
      */
     private File getArchiveFileInCache(Artifact artifact, ArtifactOrigin origin) {
         return new File(getRepositoryCacheRoot(), getArchivePathInCache(artifact, origin));
     }
 
     public String getArchivePathInCache(Artifact artifact) {
         return IvyPatternHelper.substitute(DEFAULT_ARTIFACT_PATTERN, artifact);
     }
 
     public String getArchivePathInCache(Artifact artifact, ArtifactOrigin origin) {
         String resolverId = ArtifactMetadata.extractResolverId(artifact, origin);
         if (resolverId == null || resolverId.isEmpty()) {
             throw new IllegalArgumentException("Artifact " + artifact.getId() + " or Artifact Origin " +
                     origin.toString() + " should have a resolver id");
         }
         if (isOriginalMetadataArtifact(artifact)) {
             return IvyPatternHelper.substitute(DEFAULT_IVY_PATTERN + ".original", artifact, origin);
         } else {
             return IvyPatternHelper.substitute(DEFAULT_ARTIFACT_PATTERN, artifact, origin);
         }
     }
 
     /**
      * Saves the information of which resolver was used to resolve a md, so that this info can be retrieve later (even
      * after a jvm restart) by getSavedArtResolverName(ModuleDescriptor md)
      *
      * @param md                   the module descriptor resolved
      * @param metadataResolverName the name of the resolver in ivy settings which found the module descriptor
      * @param artifactResolverName the name of the resolver in ivy settings that found the artifact
      */
     @Override
     public void saveResolvers(ModuleDescriptor md, String metadataResolverName, String artifactResolverName) {
         // In Wharf getting a resolver automatically saves it...
         DependencyResolver resolver = settings.getResolver(metadataResolverName);
         if (resolver != null) {
            resolverHandler.getResolver(resolver);
         }
         if (!metadataResolverName.equals(artifactResolverName)) {
             resolver = settings.getResolver(artifactResolverName);
             if (resolver != null) {
                resolverHandler.getResolver(resolver);
             }
         }
     }
 
     public void saveArtifactOrigin(Artifact artifact, ArtifactOrigin origin) {
         // should always be called with a lock on module metadata artifact
         ModuleRevisionId mrid = artifact.getModuleRevisionId();
         ModuleRevisionMetadata mrm = getMetadataHandler().getModuleRevisionMetadata(mrid);
         if (mrm == null) {
             mrm = new ModuleRevisionMetadata();
         }
         ArtifactMetadata artMd = new ArtifactMetadata(artifact, origin);
         mrm.artifactMetadata.add(artMd);
         getMetadataHandler().saveModuleRevisionMetadata(mrid, mrm);
     }
 
     private void removeSavedArtifactOrigin(Artifact artifact) {
         String resolverId = ArtifactMetadata.extractResolverId(artifact);
         if (resolverId == null || resolverId.isEmpty()) {
             // Something wrong... Cannot removed saved artifact which was not saved??
             Message.error("Trying to remove " + artifact +
                     " from saved cache metadata. But no resolverId associated with the artifact!");
         } else {
             // should always be called with a lock on module metadata artifact
             ModuleRevisionId mrid = artifact.getModuleRevisionId();
             ModuleRevisionMetadata metadata = getMetadataHandler().getModuleRevisionMetadata(mrid);
             if (metadata != null) {
                 ArtifactMetadata artMd = new ArtifactMetadata(artifact);
                 metadata.artifactMetadata.remove(artMd);
                 getMetadataHandler().saveModuleRevisionMetadata(mrid, metadata);
             } else {
                 Message.error("Trying to remove " + artifact + " from saved cache metadata. But no metadata found!");
             }
         }
     }
 
     /**
      * {@inheritDoc} Look in the list of saved artifact metadata and find the first resolverId present in IvySettings
      * Create an ArtifactOrigin out of the ArtifactMetadata. Make sure you can find the resolverId back from this
      * ArtifactOrigin object
      *
      * @param artifact
      * @return
      */
     @Override
     public ArtifactOrigin getSavedArtifactOrigin(Artifact artifact) {
         ModuleRevisionId mrid = artifact.getModuleRevisionId();
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.error("impossible to acquire lock for " + mrid);
             return ArtifactOrigin.unkwnown(artifact);
         }
         try {
             ModuleRevisionMetadata mrm = getMetadataHandler().getModuleRevisionMetadata(mrid);
             if (mrm == null) {
                 return ArtifactOrigin.unkwnown(artifact);
             }
             String resolverId = ArtifactMetadata.extractResolverId(artifact);
             if (resolverId == null || resolverId.isEmpty()) {
                 String artId = ArtifactMetadata.getArtId(artifact);
                 for (ArtifactMetadata artMd : mrm.artifactMetadata) {
                     if (artId.equals(artMd.id) && getResolverHandler().isActiveResolver(artMd.resolverId)) {
                         artifact = ArtifactMetadata.fillResolverId(artifact, artMd.resolverId);
                         return new ArtifactOrigin(artifact, artMd.local, artMd.location);
                     }
                 }
             } else {
                 ArtifactMetadata artMd = getMetadataHandler().getArtifactMetadata(artifact);
                 if (artMd != null) {
                     return new ArtifactOrigin(artifact, artMd.local, artMd.location);
                 }
             }
             // origin has not been specified or or no active resolver. return null
             return ArtifactOrigin.unkwnown(artifact);
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
         }
     }
 
     @Override
     public ResolvedModuleRevision findModuleInCache(DependencyDescriptor dd, ModuleRevisionId requestedRevisionId,
             CacheMetadataOptions options, String expectedResolver) {
         ModuleRevisionId mrid = requestedRevisionId;
         if (isCheckmodified(dd, requestedRevisionId, options)) {
             Message.verbose("don't use cache for " + mrid + ": checkModified=true");
             return null;
         }
         if (isChanging(dd, requestedRevisionId, options)) {
             Message.verbose("don't use cache for " + mrid + ": changing=true");
             return null;
         }
 
         DependencyResolver resolver = null;
         if (expectedResolver != null) {
             resolver = settings.getResolver(expectedResolver);
         }
         if (resolver == null) {
             String resolverName = settings.getResolverName(mrid);
             resolver = settings.getResolver(resolverName);
         }
         return doFindModuleInCache(mrid, options, resolver);
     }
 
     private ResolvedModuleRevision doFindModuleInCache(
             ModuleRevisionId mrid, CacheMetadataOptions options, DependencyResolver expectedResolver) {
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.error("impossible to acquire lock for " + mrid);
             return null;
         }
         boolean unlock = true;
         try {
             if (settings.getVersionMatcher().isDynamic(mrid)) {
                 String resolvedRevision = getResolvedRevision(mrid, options);
                 if (resolvedRevision != null) {
                     Message.verbose("found resolved revision in cache: "
                             + mrid + " => " + resolvedRevision);
 
                     // we have found another module in the cache, make sure we unlock
                     // the original module
                     getMetadataHandler().unlockMetadataArtifact(mrid);
                     mrid = ModuleRevisionId.newInstance(mrid, resolvedRevision);
 
                     // don't forget to request a lock on the new module!
                     if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
                         Message.error("impossible to acquire lock for " + mrid);
 
                         // we couldn't lock the new module, so no need to unlock it
                         unlock = false;
                         return null;
                     }
                 } else {
                     return null;
                 }
             }
             if (expectedResolver == null) {
                 expectedResolver = settings.getResolver(mrid);
             }
             WharfResolverMetadata wharfResolverMetadata = getResolverHandler().getResolver(expectedResolver);
             String expectedResolverId = wharfResolverMetadata.getId();
             File ivyFile = getIvyFileInCache(mrid, expectedResolverId);
             if (ivyFile.exists()) {
                 // found in cache !
                 try {
                     XmlModuleDescriptorParser parser = XmlModuleDescriptorParser.getInstance();
                     ModuleDescriptor depMD = getMdFromCache(parser, options, ivyFile);
                     Artifact artifact = depMD.getMetadataArtifact();
                     artifact = ArtifactMetadata.fillResolverId(artifact, expectedResolverId);
                     ArtifactMetadata artMd = getMetadataHandler().getArtifactMetadata(artifact);
                     DependencyResolver resolver = expectedResolver;
                     DependencyResolver artResolver = expectedResolver;
                     if (artMd != null && artMd.resolverId != artMd.artResolverId &&
                             getResolverHandler().isActiveResolver(artMd.artResolverId)) {
                         artResolver = settings.getResolver(getResolverHandler().getResolver(artMd.artResolverId).name);
                     }
                     Message.debug("\tfound ivy file in cache for " + mrid + " (resolved by "
                             + resolver.getName() + "): " + ivyFile);
                     if (expectedResolver == null || expectedResolver.getName().equals(resolver.getName())) {
                         MetadataArtifactDownloadReport madr = new MetadataArtifactDownloadReport(
                                 depMD.getMetadataArtifact());
                         madr.setDownloadStatus(DownloadStatus.NO);
                         madr.setSearched(false);
                         madr.setLocalFile(ivyFile);
                         madr.setSize(ivyFile.length());
                         madr.setArtifactOrigin(getSavedArtifactOrigin(depMD.getMetadataArtifact()));
                         return new ResolvedModuleRevision(resolver, artResolver, depMD, madr);
                     } else {
                         Message.debug(
                                 "found module in cache but with a different resolver: "
                                         + "discarding: " + mrid
                                         + "; expected resolver=" + expectedResolver
                                         + "; resolver=" + resolver.getName());
                     }
                 } catch (Exception e) {
                     // will try with resolver
                     Message.debug("\tproblem while parsing cached ivy file for: " + mrid + ": "
                             + e.getMessage());
                 }
             } else {
                 Message.debug("\tno ivy file in cache for " + mrid + ": tried " + ivyFile);
             }
         } finally {
             if (unlock) {
                 getMetadataHandler().unlockMetadataArtifact(mrid);
             }
         }
         return null;
     }
 
 
     private static class MyModuleDescriptorProvider implements ModuleDescriptorProvider {
 
         private final ModuleDescriptorParser mdParser;
         private final ParserSettings settings;
 
         public MyModuleDescriptorProvider(ModuleDescriptorParser mdParser, ParserSettings settings) {
             this.mdParser = mdParser;
             this.settings = settings;
         }
 
         @Override
         public ModuleDescriptor provideModule(ParserSettings ivySettings,
                 File descriptorURL, boolean validate) throws ParseException, IOException {
             return mdParser.parseDescriptor(settings, descriptorURL.toURI().toURL(), validate);
         }
     }
 
     private ModuleDescriptor getMdFromCache(XmlModuleDescriptorParser mdParser,
             CacheMetadataOptions options, File ivyFile)
             throws ParseException, IOException {
         ModuleDescriptorMemoryCache cache = getMemoryCache();
         ModuleDescriptorProvider mdProvider = new MyModuleDescriptorProvider(mdParser, settings);
         return cache.get(ivyFile, settings, options.isValidate(), mdProvider);
     }
 
     private ModuleDescriptor getStaledMd(ModuleDescriptorParser mdParser,
             CacheMetadataOptions options, File ivyFile, ParserSettings parserSettings)
             throws ParseException, IOException {
         ModuleDescriptorMemoryCache cache = getMemoryCache();
         ModuleDescriptorProvider mdProvider = new MyModuleDescriptorProvider(mdParser, parserSettings);
         return cache.getStale(ivyFile, settings, options.isValidate(), mdProvider);
     }
 
 
     private String getResolvedRevision(ModuleRevisionId mrid, CacheMetadataOptions options) {
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.error("impossible to acquire lock for " + mrid);
             return null;
         }
         try {
             String resolvedRevision = null;
             if (options.isForce()) {
                 Message.verbose("refresh mode: no check for cached resolved revision for " + mrid);
                 return null;
             }
             ModuleRevisionMetadata mrm = getMetadataHandler().getModuleRevisionMetadata(mrid);
             if (mrm != null) {
                 resolvedRevision = mrm.latestResolvedRevision;
             }
             if (resolvedRevision == null) {
                 Message.verbose(getName() + ": no cached resolved revision for " + mrid);
                 return null;
             }
             String resolvedTime = mrm.latestResolvedTime;
             if (resolvedTime == null) {
                 Message.verbose(getName()
                         + ": inconsistent or old cache: no cached resolved time for " + mrid);
                 saveResolvedRevision(mrid, resolvedRevision);
                 return resolvedRevision;
             }
             return resolvedRevision;
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
         }
     }
 
     @Override
     public void saveResolvedRevision(ModuleRevisionId mrid, String revision) {
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.error("impossible to acquire lock for " + mrid);
             return;
         }
         try {
             ModuleRevisionMetadata mrm = getMetadataHandler().getModuleRevisionMetadata(mrid);
             if (mrm == null) {
                 mrm = new ModuleRevisionMetadata();
             }
             if (!getSettings().getVersionMatcher().isDynamic(mrid)) {
                 mrm.latestResolvedRevision = revision;
                 mrm.latestResolvedTime = String.valueOf(System.currentTimeMillis());
             } else {
                 mrm.latestResolvedRevision = mrid.getRevision();
                 mrm.latestResolvedTime = String.valueOf(System.currentTimeMillis());
             }
             getMetadataHandler().saveModuleRevisionMetadata(mrid, mrm);
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
         }
     }
 
     public long getTTL(ModuleRevisionId mrid) {
         Long ttl = (Long) ttlRules.getRule(mrid);
         return ttl == null ? getDefaultTTL() : ttl;
     }
 
     public String toString() {
         return name;
     }
 
     @Deprecated
     public File getRepositoryCacheRoot() {
         return getBasedir();
     }
 
 
     @Override
     public ArtifactDownloadReport download(Artifact artifact, ArtifactResourceResolver resourceResolver,
             ResourceDownloader resourceDownloader, CacheDownloadOptions options) {
         final ArtifactDownloadReport adr = new ArtifactDownloadReport(artifact);
         boolean useOrigin = isUseOrigin();
 
         // TODO: see if we could lock on the artifact to download only, instead of the module
         // metadata artifact. We'd need to store artifact origin and is local in artifact specific
         // file to do so, or lock the metadata artifact only to update artifact origin, which would
         // mean acquiring nested locks, which can be a dangerous thing
         ModuleRevisionId mrid = artifact.getModuleRevisionId();
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             adr.setDownloadStatus(DownloadStatus.FAILED);
             adr.setDownloadDetails("impossible to get lock for " + mrid);
             return adr;
         }
         try {
             DownloadListener listener = options.getListener();
             if (listener != null) {
                 listener.needArtifact(this, artifact);
             }
             WharfResolverMetadata resolverMetadata;
             String resolverId = ArtifactMetadata.extractResolverId(artifact);
             if (resolverId == null || resolverId.isEmpty()) {
                 DependencyResolver callingResolver;
                 try {
                     Field field = resourceResolver.getClass().getDeclaredField("this$0");
                     field.setAccessible(true);
                     Object callingField = field.get(resourceResolver);
                     if (callingField instanceof ChainResolver) {
                         throw new IllegalStateException("Cannot be called from a chain resolver: " + callingField);
                     }
                     if (callingField instanceof DependencyResolver) {
                         callingResolver = (DependencyResolver) callingField;
                         resolverMetadata = getResolverHandler().getResolver(callingResolver);
                         artifact = ArtifactMetadata.fillResolverId(artifact, resolverMetadata.getId());
                     } else {
                         throw new IllegalStateException(
                                 "Calling download on wharf resolver not from a DependencyResolver: " + callingField);
                     }
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (NoSuchFieldException e) {
                     e.printStackTrace();
                 }
             } else {
                 resolverMetadata = getResolverHandler().getResolver(resolverId);
             }
             ArtifactOrigin origin = getSavedArtifactOrigin(artifact);
             // if we can use origin file, we just ask ivy for the file in cache, and it will
             // return the original one if possible. If we are not in useOrigin mode, we use the
             // getArchivePath method which always return a path in the actual cache
             File archiveFile = getArchiveFileInCache(artifact, origin);
 
             if (archiveFile.exists() && !options.isForce()) {
                 adr.setDownloadStatus(DownloadStatus.NO);
                 adr.setSize(archiveFile.length());
                 adr.setArtifactOrigin(origin);
                 adr.setLocalFile(archiveFile);
             } else {
                 long start = System.currentTimeMillis();
                 try {
                     ResolvedResource artifactRef = resourceResolver.resolve(artifact);
                     if (artifactRef != null) {
                         origin = new ArtifactOrigin(artifact, artifactRef.getResource().isLocal(),
                                 artifactRef.getResource().getName());
                         if (useOrigin && artifactRef.getResource().isLocal()) {
                             saveArtifactOrigin(artifact, origin);
                             archiveFile = getArchiveFileInCache(artifact, origin);
                             adr.setDownloadStatus(DownloadStatus.NO);
                             adr.setSize(archiveFile.length());
                             adr.setArtifactOrigin(origin);
                             adr.setLocalFile(archiveFile);
                         } else {
                             // refresh archive file now that we better now its origin
                             archiveFile = getArchiveFileInCache(artifact, origin);
                             if (ResourceHelper.equals(artifactRef.getResource(), archiveFile)) {
                                 throw new IllegalStateException("invalid settings for '"
                                         + resourceResolver
                                         + "': pointing repository to ivy cache is forbidden !");
                             }
                             if (listener != null) {
                                 listener.startArtifactDownload(this, artifactRef, artifact, origin);
                             }
                             Resource resource = artifactRef.getResource();
                             resourceDownloader.download(artifact, resource, archiveFile);
                             adr.setSize(archiveFile.length());
                             saveArtifactOrigin(artifact, origin);
                             ModuleRevisionMetadata metadata =
                                     getMetadataHandler().getModuleRevisionMetadata(artifact.getModuleRevisionId());
                             ArtifactMetadata artMd = getMetadataHandler().getArtifactMetadata(artifact);
                             //TODO: [by tc] we should not re-parse the file to get the checksums
                             if ((artMd.md5 == null || artMd.md5.isEmpty())) {
                                 artMd.md5 = ChecksumHelper.computeAsString(archiveFile, "md5");
                             }
                             if ((artMd.sha1 == null || artMd.sha1.isEmpty())) {
                                 artMd.sha1 = ChecksumHelper.computeAsString(archiveFile, "sha1");
                             }
                             metadata.artifactMetadata.remove(artMd);
                             metadata.artifactMetadata.add(artMd);
                             getMetadataHandler().saveModuleRevisionMetadata(artifact.getModuleRevisionId(), metadata);
                             adr.setDownloadTimeMillis(System.currentTimeMillis() - start);
                             adr.setDownloadStatus(DownloadStatus.SUCCESSFUL);
                             adr.setArtifactOrigin(origin);
                             adr.setLocalFile(archiveFile);
                         }
                     } else {
                         adr.setDownloadStatus(DownloadStatus.FAILED);
                         adr.setDownloadDetails(ArtifactDownloadReport.MISSING_ARTIFACT);
                         adr.setDownloadTimeMillis(System.currentTimeMillis() - start);
                     }
                 } catch (Exception ex) {
                     adr.setDownloadStatus(DownloadStatus.FAILED);
                     adr.setDownloadDetails(ex.getMessage());
                     adr.setDownloadTimeMillis(System.currentTimeMillis() - start);
                 }
             }
             if (listener != null) {
                 listener.endArtifactDownload(this, artifact, adr, archiveFile);
             }
             return adr;
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
         }
     }
 
     @Override
     public void originalToCachedModuleDescriptor(DependencyResolver resolver, ResolvedResource orginalMetadataRef,
             Artifact requestedMetadataArtifact, ResolvedModuleRevision rmr, ModuleDescriptorWriter writer) {
         ModuleDescriptor md = rmr.getDescriptor();
         WharfResolverMetadata wharfResolverMetadata = getResolverHandler().getResolver(resolver);
         Artifact originalMetadataArtifact = getOriginalMetadataArtifact(requestedMetadataArtifact);
         String resolverId = wharfResolverMetadata.getId();
         originalMetadataArtifact = ArtifactMetadata.fillResolverId(originalMetadataArtifact, resolverId);
         File mdFileInCache = getIvyFileInCache(md.getResolvedModuleRevisionId(), resolverId);
 
         ModuleRevisionId mrid = requestedMetadataArtifact.getModuleRevisionId();
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.warn("impossible to acquire lock for: " + mrid);
             return;
         }
         try {
             File originalFileInCache = getArchiveFileInCache(originalMetadataArtifact);
             writer.write(orginalMetadataRef, md, originalFileInCache, mdFileInCache);
             saveResolvers(md, resolver.getName(), resolver.getName());
 
             if (!md.isDefault()) {
                 rmr.getReport().setOriginalLocalFile(originalFileInCache);
             }
             rmr.getReport().setLocalFile(mdFileInCache);
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             Message.warn("impossible to put metadata file in cache: "
                     + (orginalMetadataRef == null
                     ? String.valueOf(md.getResolvedModuleRevisionId())
                     : String.valueOf(orginalMetadataRef))
                     + ". " + e.getClass().getName() + ": " + e.getMessage());
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
         }
     }
 
     @Override
     public ResolvedModuleRevision cacheModuleDescriptor(DependencyResolver resolver, final ResolvedResource mdRef,
             DependencyDescriptor dd, Artifact moduleArtifact, ResourceDownloader downloader,
             CacheMetadataOptions options) throws ParseException {
         Date cachedPublicationDate = null;
         ArtifactDownloadReport report;
         ModuleRevisionId mrid = moduleArtifact.getModuleRevisionId();
         if (!getMetadataHandler().lockMetadataArtifact(mrid)) {
             Message.error("impossible to acquire lock for " + mrid);
             return null;
         }
 
         BackupResourceDownloader backupDownloader = new BackupResourceDownloader(downloader);
 
         try {
             if (!moduleArtifact.isMetadata()) {
                 // the descriptor we are trying to cache is a default one, not much to do
                 // just make sure the old artifacts are deleted...
                 if (isChanging(dd, mrid, options)) {
                     long repoLastModified = mdRef.getLastModified();
                     Artifact transformedArtifact = NameSpaceHelper.transform(
                             moduleArtifact, options.getNamespace().getToSystemTransformer());
                     WharfResolverMetadata wharfResolverMetadata = getResolverHandler().getResolver(resolver);
                     // put into the artifact the resolverId as an extra attribute.
                     transformedArtifact =
                             ArtifactMetadata.fillResolverId(transformedArtifact, wharfResolverMetadata.getId());
                     // If there is a cached file for this dependency resolver => delete the file and metadata
                     ArtifactOrigin origin = getSavedArtifactOrigin(transformedArtifact);
                     File artFile = getArchiveFileInCache(transformedArtifact, origin);
                     if (artFile.exists() && repoLastModified > artFile.lastModified()) {
                         // artifacts have changed, they should be downloaded again
                         Message.verbose(mrid + " has changed: deleting old artifacts");
                         Message.debug("deleting " + artFile);
                         if (!artFile.delete()) {
                             Message.error("Couldn't delete outdated artifact from cache: " + artFile);
                             return null;
                         }
                         removeSavedArtifactOrigin(transformedArtifact);
                     }
                 }
                 return null;
             }
 
             // now let's see if we can find it in cache and if it is up to date
             ResolvedModuleRevision rmr = doFindModuleInCache(mrid, options, resolver);
             if (rmr != null) {
                 if (rmr.getDescriptor().isDefault() && rmr.getResolver() != resolver) {
                     Message.verbose("\t" + getName() + ": found revision in cache: " + mrid
                             + " (resolved by " + rmr.getResolver().getName()
                             + "): but it's a default one, maybe we can find a better one");
                 } else {
                     if (!isCheckmodified(dd, mrid, options) && !isChanging(dd, mrid, options)) {
                         Message.verbose("\t" + getName() + ": revision in cache: " + mrid);
                         rmr.getReport().setSearched(true);
                         return rmr;
                     }
                     long repLastModified = mdRef.getLastModified();
                     long cacheLastModified = rmr.getDescriptor().getLastModified();
                     if (!rmr.getDescriptor().isDefault() && repLastModified <= cacheLastModified) {
                         Message.verbose("\t" + getName() + ": revision in cache (not updated): "
                                 + mrid);
                         rmr.getReport().setSearched(true);
                         return rmr;
                     } else {
                         Message.verbose("\t" + getName() + ": revision in cache is not up to date: "
                                 + mrid);
                         if (isChanging(dd, mrid, options)) {
                             // ivy file has been updated, we should see if it has a new publication
                             // date to see if a new download is required (in case the dependency is
                             // a changing one)
                             cachedPublicationDate =
                                     rmr.getDescriptor().getResolvedPublicationDate();
                         }
                     }
                 }
             }
 
             Artifact originalMetadataArtifact = getOriginalMetadataArtifact(moduleArtifact);
             WharfResolverMetadata wharfResolverMetadata = getResolverHandler().getResolver(resolver);
             String resolverId = wharfResolverMetadata.getId();
             originalMetadataArtifact = ArtifactMetadata.fillResolverId(originalMetadataArtifact, resolverId);
             // now download module descriptor and parse it
             report = download(originalMetadataArtifact, new ArtifactResourceResolver() {
                 @Override
                 public ResolvedResource resolve(Artifact artifact) {
                     return mdRef;
                 }
             }, backupDownloader,
                     new CacheDownloadOptions().setListener(options.getListener()).setForce(true));
             Message.verbose("\t" + report);
 
             if (report.getDownloadStatus() == DownloadStatus.FAILED) {
                 Message.warn("problem while downloading module descriptor: " + mdRef.getResource()
                         + ": " + report.getDownloadDetails()
                         + " (" + report.getDownloadTimeMillis() + "ms)");
                 return null;
             }
 
             try {
                 ModuleDescriptorParser parser = ModuleDescriptorParserRegistry
                         .getInstance().getParser(mdRef.getResource());
                 ParserSettings parserSettings = settings;
                 if (resolver instanceof AbstractResolver) {
                     parserSettings = ((AbstractResolver) resolver).getParserSettings();
                 }
                 ModuleDescriptor md = getStaledMd(parser, options, report.getLocalFile(), parserSettings);
                 if (md == null) {
                     throw new IllegalStateException(
                             "module descriptor parser returned a null module descriptor, "
                                     + "which is not allowed. "
                                     + "parser=" + parser
                                     + "; parser class=" + parser.getClass().getName()
                                     + "; module descriptor resource=" + mdRef.getResource());
                 }
                 Message.debug("\t" + getName() + ": parsed downloaded md file for " + mrid
                         + "; parsed=" + md.getModuleRevisionId());
 
                 // check if we should delete old artifacts
                 boolean deleteOldArtifacts = false;
                 if (cachedPublicationDate != null
                         && !cachedPublicationDate.equals(md.getResolvedPublicationDate())) {
                     // artifacts have changed, they should be downloaded again
                     Message.verbose(mrid + " has changed: deleting old artifacts");
                     deleteOldArtifacts = true;
                 }
                 if (deleteOldArtifacts) {
                     String[] confs = md.getConfigurationsNames();
                     for (String conf : confs) {
                         Artifact[] arts = md.getArtifacts(conf);
                         for (Artifact art : arts) {
                             Artifact transformedArtifact =
                                     NameSpaceHelper.transform(art, options.getNamespace().getToSystemTransformer());
                             transformedArtifact = ArtifactMetadata.fillResolverId(transformedArtifact, resolverId);
                             ArtifactOrigin origin = getSavedArtifactOrigin(transformedArtifact);
                             File artFile = getArchiveFileInCache(transformedArtifact, origin);
                             if (artFile.exists()) {
                                 Message.debug("deleting " + artFile);
                                 if (!artFile.delete()) {
                                     // Old artifacts couldn't get deleted!
                                     // Restore the original ivy file so the next time we
                                     // resolve the old artifacts are deleted again
                                     backupDownloader.restore();
                                     Message.error("Couldn't delete outdated artifact from cache: " + artFile);
                                     return null;
                                 }
                             }
                             removeSavedArtifactOrigin(transformedArtifact);
                         }
                     }
                 } else if (isChanging(dd, mrid, options)) {
                     Message.verbose(mrid
                             + " is changing, but has not changed: will trust cached artifacts if any");
                 }
 
                 MetadataArtifactDownloadReport madr
                         = new MetadataArtifactDownloadReport(md.getMetadataArtifact());
                 madr.setSearched(true);
                 madr.setDownloadStatus(report.getDownloadStatus());
                 madr.setDownloadDetails(report.getDownloadDetails());
                 madr.setArtifactOrigin(report.getArtifactOrigin());
                 madr.setDownloadTimeMillis(report.getDownloadTimeMillis());
                 madr.setOriginalLocalFile(report.getLocalFile());
                 madr.setSize(report.getSize());
 
                 Artifact transformedMetadataArtifact = NameSpaceHelper.transform(
                         md.getMetadataArtifact(), options.getNamespace().getToSystemTransformer());
                 transformedMetadataArtifact = ArtifactMetadata.fillResolverId(transformedMetadataArtifact,
                         getResolverHandler().getResolver(resolver).getId());
                 saveArtifactOrigin(transformedMetadataArtifact, report.getArtifactOrigin());
 
                 return new ResolvedModuleRevision(resolver, resolver, md, madr);
             } catch (IOException ex) {
                 Message.warn("io problem while parsing ivy file: " + mdRef.getResource() + ": "
                         + ex.getMessage());
                 return null;
             }
         } finally {
             getMetadataHandler().unlockMetadataArtifact(mrid);
             backupDownloader.cleanUp();
         }
     }
 
     public Artifact getOriginalMetadataArtifact(Artifact moduleArtifact) {
         return DefaultArtifact.cloneWithAnotherType(moduleArtifact, moduleArtifact.getType() + ".original");
     }
 
     private boolean isOriginalMetadataArtifact(Artifact artifact) {
         return artifact.isMetadata() && artifact.getType().endsWith(".original");
     }
 
     private boolean isChanging(
             DependencyDescriptor dd, ModuleRevisionId requestedRevisionId,
             CacheMetadataOptions options) {
         return dd.isChanging()
                 || getChangingMatcher(options).matches(requestedRevisionId.getRevision());
     }
 
     private Matcher getChangingMatcher(CacheMetadataOptions options) {
         String changingPattern = options.getChangingPattern() != null
                 ? options.getChangingPattern() : this.changingPattern;
         if (changingPattern == null) {
             return NoMatcher.INSTANCE;
         }
         String changingMatcherName = options.getChangingMatcherName() != null
                 ? options.getChangingMatcherName() : this.changingMatcherName;
         PatternMatcher matcher = settings.getMatcher(changingMatcherName);
         if (matcher == null) {
             throw new IllegalStateException("unknown matcher '" + changingMatcherName
                     + "'. It is set as changing matcher in " + this);
         }
         return matcher.getMatcher(changingPattern);
     }
 
     private boolean isCheckmodified(DependencyDescriptor dd, ModuleRevisionId requestedRevisionId,
             CacheMetadataOptions options) {
         if (options.isCheckmodified() != null) {
             return options.isCheckmodified();
         }
         return isCheckmodified();
     }
 
     @Override
     public void clean() {
         FileUtil.forceDelete(getBasedir());
     }
 
     public void dumpSettings() {
         Message.verbose("\t" + getName());
         Message.debug("\t\tivyPattern: " + DEFAULT_IVY_PATTERN);
         Message.debug("\t\tartifactPattern: " + DEFAULT_ARTIFACT_PATTERN);
         Message.debug("\t\tlockingStrategy: " + getMetadataHandler().getLockStrategy().getName());
         Message.debug("\t\tchangingPattern: " + getChangingPattern());
         Message.debug("\t\tchangingMatcher: " + getChangingMatcherName());
     }
 
     private static final class BackupResourceDownloader implements ResourceDownloader {
 
         private final ResourceDownloader delegate;
         private File backup;
         private String originalPath;
 
         private BackupResourceDownloader(ResourceDownloader delegate) {
             this.delegate = delegate;
         }
 
         @Override
         public void download(Artifact artifact, Resource resource, File dest) throws IOException {
             // keep a copy of the original file
             if (dest.exists()) {
                 originalPath = dest.getAbsolutePath();
                 backup = new File(dest.getAbsolutePath() + ".backup");
                 FileUtil.copy(dest, backup, null, true);
             }
             /*  if (!(resource instanceof WharfUrlResource)) {
                 resource = new WharfUrlResource(resource);
             }*/
             delegate.download(artifact, resource, dest);
         }
 
         public void restore() throws IOException {
             if ((backup != null) && backup.exists()) {
                 File original = new File(originalPath);
                 FileUtil.copy(backup, original, null, true);
                 backup.delete();
             }
         }
 
         public void cleanUp() {
             if ((backup != null) && backup.exists()) {
                 backup.delete();
             }
         }
 
     }
 
 }
