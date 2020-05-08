 package org.kevoree.resolver;
 
 import org.kevoree.log.Log;
 import org.kevoree.resolver.api.MavenArtefact;
 import org.kevoree.resolver.api.MavenVersionResult;
 import org.kevoree.resolver.util.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 21/05/13
  * Time: 11:50
  */
 public class MavenResolver {
 
     public MavenResolver() {
         if (System.getProperty("o") != null || System.getProperty("offline") != null) {
             Log.info("Maven resolver is in offline mode");
         }
     }
 
     private String basePath = System.getProperty("user.home").toString() + File.separator + ".m2" + File.separator + "repository";
     private static final String SNAPSHOT_VERSION_END = "-SNAPSHOT";
     private MavenArtefactDownloader downloader = new MavenArtefactDownloader();
     private MavenVersionResolver versionResolver = new MavenVersionResolver();
 
     public void setBasePath(String basePath) {
         this.basePath = basePath;
     }
 
     private StringBuilder getArtefactLocalBasePath(MavenArtefact artefact) {
         StringBuilder builder = new StringBuilder();
         builder.append(basePath);
         builder.append(File.separator);
         builder.append(artefact.getGroup().replace(".", File.separator));
         builder.append(File.separator);
         builder.append(artefact.getName());
         builder.append(File.separator);
         builder.append(artefact.getVersion());
         builder.append(File.separator);
         return builder;
     }
 
     public File resolve(String url, Set<String> urls) {
         //URL like mvn:groupID:artID:version[:ext]
         String[] parts = url.split(":");
         if (parts.length == 4 || parts.length == 5) {
             if (parts.length == 5) {
                 return resolve(parts[1], parts[2], parts[3], parts[4], urls);
             } else {
                 return resolve(parts[1], parts[2], parts[3], "jar", urls);
             }
         } else {
             Log.error("Malformed url : " + url);
             return null;
         }
     }
 
 
     public SortedSet<String> listVersion(String group, String name, String extension, Set<String> paramURLS) {
         Set<String> urls = paramURLS;
         if (System.getProperty("o") != null || System.getProperty("offline") != null) {
             urls = new HashSet<String>(); //in offline mode we don't consider any urls
         }
         final MavenArtefact artefact = new MavenArtefact();
         artefact.setGroup(group);
         artefact.setName(name);
         artefact.setVersion("latest");
         artefact.setExtension(extension);
 
         final Comparator<String> comparator = new Comparator<String>() {
             @Override
             public int compare(String o1, String o2) {
                 String result = MavenVersionComparator.max(o1,o2);
                 if(o1.equals(o2)){
                     return 0;
                 }
                 if(result.equals(o1)){
                     return -1;
                 }
                 if(result.equals(o2)){
                     return 1;
                 }
                 return -1;
             }
         };
         SortedSet<String> sortedResult = new TreeSet<String>(comparator);
         sortedResult.addAll(versionResolver.listVersions(artefact, basePath, basePath, true));
         for (String url : urls) {
             sortedResult.addAll(versionResolver.listVersions(artefact, basePath, url, false));
         }
         return sortedResult;
     }
 
 
     public File resolve(String group, String name, String versionAsked, String extension, Set<String> paramURLS) {
 
         Set<String> urls = paramURLS;
         if (System.getProperty("o") != null || System.getProperty("offline") != null) {
             urls = new HashSet<String>(); //in offline mode we don't consider any urls
         }
 
         final MavenArtefact artefact = new MavenArtefact();
         artefact.setGroup(group);
         artefact.setName(name);
         artefact.setVersion(versionAsked);
         artefact.setExtension(extension);
 
         if (artefact.getVersion().equalsIgnoreCase("release") || artefact.getVersion().equalsIgnoreCase("latest")) {
             String bestVersion = artefact.getVersion();
             for (String url : urls) {
                 String vremoteSaved = versionResolver.foundRelevantVersion(artefact, basePath, url, false);
                 bestVersion = MavenVersionComparator.max(artefact.getVersion(), vremoteSaved);
             }
             artefact.setVersion(bestVersion);
             String vlocalSaved = versionResolver.foundRelevantVersion(artefact, basePath, basePath, true);
             artefact.setVersion(MavenVersionComparator.max(artefact.getVersion(), vlocalSaved));
             ExecutorService pool = Executors.newCachedThreadPool();
             List<Callable<String>> callables = new ArrayList<Callable<String>>();
             for (final String url : urls) {
                 callables.add(new AsyncVersionFiller(versionResolver, artefact, basePath, url));
             }
             try {
                 List<Future<String>> results = pool.invokeAll(callables);
                 for (Future<String> vFut : results) {
                     try {
                         artefact.setVersion(MavenVersionComparator.max(artefact.getVersion(), vFut.get()));
                     } catch (ExecutionException e) {
                         e.printStackTrace();
                     }
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             pool.shutdown();
         }
 
         if (!artefact.getVersion().endsWith(SNAPSHOT_VERSION_END)) {
             //Try from local cache first
             StringBuilder basePathBuilder = getArtefactLocalBasePath(artefact);
             basePathBuilder.append(name);
             basePathBuilder.append("-");
             basePathBuilder.append(artefact.getVersion());
             basePathBuilder.append(".");
             basePathBuilder.append(extension);
             File targetReleaseFile = new File(basePathBuilder.toString());
             if (targetReleaseFile.exists()) {
                 return targetReleaseFile;
             }
             //release case
             for (String url : urls) {
                 if (downloader.download(targetReleaseFile, url, artefact, extension, null, false)) {
                     return targetReleaseFile;
                 }
             }
             //No url reply, return not found result
             return null;
         } else {
             //snapshot case
             List<MavenVersionResult> versions = new ArrayList<MavenVersionResult>();
             MavenVersionResult localVersion;
             try {
                 localVersion = versionResolver.resolveVersion(artefact, basePath, false);
                 if (localVersion != null) {
                     versions.add(localVersion);
                 }
             } catch (IOException e) {
                 //not found remotely, ignore it
             }
             try {
                 localVersion = versionResolver.resolveVersion(artefact, basePath, true);
                 if (localVersion != null) {
                     versions.add(localVersion);
                 }
             } catch (IOException e) {
                 //not found locally, ignore it
             }
 
             ExecutorService pool = Executors.newCachedThreadPool();
             ArrayList<AsyncVersionResolver> resolvers = new ArrayList<AsyncVersionResolver>();
             for (String url : urls) {
                 resolvers.add(new AsyncVersionResolver(artefact, url, versionResolver));
             }
             try {
                 List<Future<MavenVersionResult>> results = pool.invokeAll(resolvers);
                 for (Future<MavenVersionResult> r : results) {
                     if (r != null) {
                         try {
                             MavenVersionResult interRes = r.get();
                             if (interRes != null) {
                                 versions.add(interRes);
                             }
                         } catch (ExecutionException e) {
                             e.printStackTrace();
                         }
                     }
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             pool.shutdown();
 
             if (versions.isEmpty()) {
                 //not version at all , try simply the file with -SNAPSHOT extension
                 StringBuilder basePathBuilderSnapshot = getArtefactLocalBasePath(artefact);
                 basePathBuilderSnapshot.append(name);
                 basePathBuilderSnapshot.append("-");
                 basePathBuilderSnapshot.append(artefact.getVersion());
                 basePathBuilderSnapshot.append(".");
                 basePathBuilderSnapshot.append(extension);
                 File snapshotFile = new File(basePathBuilderSnapshot.toString());
                 if (snapshotFile.exists()) {
                     return snapshotFile;
                 } else {
                     Log.error("No metadata file found for {}/{}/{}", group, name, artefact.getVersion());
                     return null;
                 }
             } else {
                 MavenVersionResult bestVersion = null;
                 for (MavenVersionResult loopVersion : versions) {
                     if (bestVersion == null || bestVersion.isPrior(loopVersion)) {
                         bestVersion = loopVersion;
                     }
                 }
                 if (bestVersion != null) {
                     String preresolvedVersion = bestVersion.getValue();
                     String firstPartVersion = artefact.getVersion().replace("SNAPSHOT", "");
                     if (!preresolvedVersion.startsWith(firstPartVersion)) {
                         preresolvedVersion = firstPartVersion + bestVersion.getValue();
                     }
                     if (bestVersion.getUrl_origin().equals(basePath)) {
                         //resolve locally
                         StringBuilder basePathBuilderSnapshot = getArtefactLocalBasePath(artefact);
                         basePathBuilderSnapshot.append(name);
                         basePathBuilderSnapshot.append("-");
                         if (!bestVersion.isNotDeployed()) {
                             basePathBuilderSnapshot.append(preresolvedVersion);
                         } else {//TAKE directly -snapshot file
                             basePathBuilderSnapshot.append(artefact.getVersion());
                         }
                         basePathBuilderSnapshot.append(".");
                         basePathBuilderSnapshot.append(extension);
                         File snapshotFile = new File(basePathBuilderSnapshot.toString());
                         if (snapshotFile.exists()) {
                             return snapshotFile;
                         } else {
                             //This is really bad... try to get remotely
                             //remove meta local file ?
                             //TODO
                             MavenVersionResult bestRemoteVersion = null;
                             for (MavenVersionResult loopVersion : versions) {
                                 if ((!loopVersion.getUrl_origin().equals(basePath)) && (bestRemoteVersion == null || bestRemoteVersion.isPrior(loopVersion))) {
                                     bestRemoteVersion = loopVersion;
                                 }
                             }
                             String preresolvedVersion2 = bestRemoteVersion.getValue();
                             String firstPartVersion2 = artefact.getVersion().replace("SNAPSHOT", "");
                             if (!preresolvedVersion2.startsWith(firstPartVersion2)) {
                                 preresolvedVersion2 = firstPartVersion2 + bestRemoteVersion.getValue();
                             }
                             /*artefact.getVersion().replace("SNAPSHOT", "");
                             preresolvedVersion2 = preresolvedVersion2 + bestRemoteVersion.getTimestamp();
                             if (bestRemoteVersion.getBuildNumber() != null) {
                                 preresolvedVersion2 = preresolvedVersion2 + "-";
                                 preresolvedVersion2 = preresolvedVersion2 + bestRemoteVersion.getBuildNumber();
                             }*/
                             //Ok try on all urls, meta file has been download but bot the artefact :(
                             for (String url : urls) {
                                 if (downloader.download(snapshotFile, url, artefact, extension, preresolvedVersion2, false)) {
                                     //download the metafile
                                     Log.info("File resolved remotly, download metafile");
                                    File newMetaFile = new File(snapshotFile.getAbsolutePath().substring(0, snapshotFile.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + MavenVersionResolver.metaFile);
                                     downloader.download(newMetaFile, url, artefact, extension, preresolvedVersion2, true);
                                     return snapshotFile;
                                 }
                             }
                             System.err.println(">" + bestVersion);
                             return null;
                         }
                     } else {
                         //try to see if its localy cached
                         StringBuilder basePathBuilderSnapshot = getArtefactLocalBasePath(artefact);
                         basePathBuilderSnapshot.append(name);
                         basePathBuilderSnapshot.append("-");
                         basePathBuilderSnapshot.append(preresolvedVersion);
                         basePathBuilderSnapshot.append(".");
                         basePathBuilderSnapshot.append(extension);
                         File targetSnapshotFile = new File(basePathBuilderSnapshot.toString());
                         if (targetSnapshotFile.exists()) {
                             return targetSnapshotFile;
                         } else {
                             if (downloader.download(targetSnapshotFile, bestVersion.getUrl_origin(), artefact, extension, preresolvedVersion, false)) {
                                 Log.info("File resolved remotely, download metaFile");
                                 //download the metafile
                                 String targetPath = targetSnapshotFile.getAbsolutePath();
                                 if (targetPath.contains(File.separator)) {
                                     targetPath = targetPath.substring(0, targetPath.lastIndexOf(File.separator));
                                 }
                                 targetPath = targetPath + File.separator + MavenVersionResolver.metaFile;
                                 File newMetaFile = new File(targetPath);
                                 downloader.download(newMetaFile, bestVersion.getUrl_origin(), artefact, extension, preresolvedVersion, true);
                                 return targetSnapshotFile;
                             }
                             //not found
                             Log.info("Not resolved {} from {} : {}/{}/{}", preresolvedVersion, bestVersion.getUrl_origin(), group, name, artefact.getVersion());
                             return null;
                         }
                     }
 
                 } else {
                     Log.error("Not best version are found for {}/{}/{}", group, name, artefact.getVersion());
                     return null;
                 }
             }
         }
     }
 }
