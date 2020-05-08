 /*******************************************************************************
  * Copyright (c) 2010, 2012 SAP AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tycho.nexus.internal.plugin.cache;
 
 import org.apache.maven.artifact.repository.metadata.Snapshot;
 import org.apache.maven.artifact.repository.metadata.Versioning;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.sonatype.nexus.proxy.ItemNotFoundException;
 import org.sonatype.nexus.proxy.LocalStorageException;
 import org.sonatype.nexus.proxy.ResourceStoreRequest;
 import org.sonatype.nexus.proxy.repository.Repository;
 
 public class LatestVersionRequest extends ParsedRequest {
 
     /*
      * Example request path:
      * 
      * /org/eclipse/tycho/example/org.eclipse.tycho.example.p2repo/SNAPSHOT/org.eclipse.tycho.example
      * .p2repo-SNAPHOT-assembly.zip-unzip/
      * 
      * Example result for a snapshot:
      * 
      * /org/eclipse/tycho/example/org.eclipse.tycho.example.p2repo/0.1.0-SNAPSHOT/org.eclipse.tycho.
      * example.p2repo-0.1.0-20100505.133931-1-assembly.zip-unzip/
      * 
      * 
      * Example result for a release:
      * 
      * /org/eclipse/tycho/example/org.eclipse.tycho.example.p2repo/0.1.0/org.eclipse.tycho.example.
      * p2repo-0.1.0-assembly.zip-unzip/
      */
     final ResourceStoreRequest request;
     private final String groupArtifactPath;
     private final String artifactNameStart;
     private final String artifactNameEnd;
     private final VersionRange versionRange;
 
     public LatestVersionRequest(final ResourceStoreRequest request, final String groupArtifactPath,
             final String artifactNameStart, final String artifactNameEnd, final VersionRange versionRange) {
         this.request = request;
         this.groupArtifactPath = groupArtifactPath;
         this.artifactNameStart = artifactNameStart;
         this.artifactNameEnd = artifactNameEnd;
         this.versionRange = versionRange;
     }
 
     @Override
     ConversionResult resolve(final Repository repository) throws LocalStorageException {
         final String requestPath = request.getRequestPath();
         try {
             final Versioning versioning = getVersioning(repository, metadataPath(groupArtifactPath + "/"));
 
             final String selectedVersion = selectVersion(request, versioning, versionRange, true);
             final String latestVersionDirectory = groupArtifactPath + "/" + selectedVersion + "/";
 
             if (selectedVersion.endsWith("-SNAPSHOT")) {
                 return resolveSnapshot(repository, selectedVersion, latestVersionDirectory);
             }
 
             final String pathUpToVersion = latestVersionDirectory + artifactNameStart + "-" + selectedVersion;
             final String convertedPath = pathUpToVersion + artifactNameEnd;
             return new ConversionResult(requestPath, convertedPath, selectedVersion, pathUpToVersion);
         } catch (final ItemNotFoundException e) {
             return new ConversionResult(requestPath);
         }
     }
 
     private ConversionResult resolveSnapshot(final Repository repository, final String latestVersion,
             final String latestVersionDirectory) throws LocalStorageException, ItemNotFoundException {
         final Versioning snapshotVersioning = getVersioning(repository, metadataPath(latestVersionDirectory));
         final Snapshot current = snapshotVersioning.getSnapshot();
         final String latestTimestampVersion;
         if (current != null) {
             latestTimestampVersion = current.getTimestamp() + "-" + current.getBuildNumber();
         } else {
             throw new LocalStorageException(metadataPath(latestVersionDirectory)
                     + " does not contain current information in repository " + repository.getId());
         }
        final String latestVersionWithoutSnapshot = latestVersion.substring(0,
                latestVersion.length() - "-SNAPSHOT".length());
         final String pathUpToVersion = latestVersionDirectory + artifactNameStart + "-" + latestVersionWithoutSnapshot;
         final String convertedPath = pathUpToVersion + "-" + latestTimestampVersion + artifactNameEnd;
        return new ConversionResult(request.getRequestPath(), convertedPath, latestVersion + "-"
                 + latestTimestampVersion, pathUpToVersion);
     }
 
 }
