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
 
 package org.eclipse.virgo.kernel.deployer.model.internal;
 
 import java.io.File;
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.virgo.nano.deployer.api.core.DeployUriNormaliser;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.core.internal.StandardDeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateDeploymentIdentityException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateFileNameException;
 import org.eclipse.virgo.kernel.deployer.model.DuplicateLocationException;
 import org.eclipse.virgo.kernel.deployer.model.GCRoots;
 import org.eclipse.virgo.kernel.deployer.model.RuntimeArtifactModel;
 import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
 import org.eclipse.virgo.nano.serviceability.Assert;
 import org.eclipse.virgo.nano.serviceability.NonNull;
 
 /**
  * {@link StandardRuntimeArtifactModel} is the default {@link RuntimeArtifactModel} implementation.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is thread safe.
  * 
  */
 final class StandardRuntimeArtifactModel implements RuntimeArtifactModel, GCRoots {
 
     private static final String CLASH_MESSAGE_FORMAT = "The artifact %s at URI '%s' cannot be stored in the runtime artifact model as it clashes with the artifact %s which is already present.";
 
     private static final String URI_PATH_SEPARATOR = "/";
 
     private static final String SCHEME_FILE = "file";
 
     private final Object monitor = new Object();
 
     private final Map<URI, InstallArtifact> artifactByUri = new HashMap<URI, InstallArtifact>();
 
     private final Map<DeploymentIdentity, URI> uriByIdentity = new HashMap<DeploymentIdentity, URI>();
 
     private final Map<String, URI> uriByFileName = new HashMap<String, URI>();
 
     private final DeployUriNormaliser uriNormaliser;
 
     StandardRuntimeArtifactModel(DeployUriNormaliser uriNormaliser) {
         this.uriNormaliser = uriNormaliser;
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity add(@NonNull URI location, @NonNull InstallArtifact installArtifact) throws DuplicateFileNameException,
         DuplicateLocationException, DuplicateDeploymentIdentityException, DeploymentException {
 
         URI canonicalLocation = getCanonicalFileLocation(location);
 
         synchronized (this.monitor) {
 
             // Check the precondition and throw an exception if it is violated.
             checkLocation(canonicalLocation, installArtifact);
 
             String fileName = getFileName(canonicalLocation);
             checkFileName(canonicalLocation, installArtifact, fileName);
 
             DeploymentIdentity deploymentIdentity = getDeploymentIdentity(installArtifact);
             checkDeploymentIdentity(canonicalLocation, installArtifact, deploymentIdentity);
 
             // The precondition is true, so update the state. The invariants are preserved.
             updateState(canonicalLocation, installArtifact, fileName, deploymentIdentity);
 
             return deploymentIdentity;
         }
     }
 
     private void checkLocation(URI canonicalLocation, InstallArtifact installArtifact) throws DuplicateLocationException {
         if (this.artifactByUri.containsKey(canonicalLocation)) {
             InstallArtifact clashingArtifact = getArtifactByUri(canonicalLocation);
             throw new DuplicateLocationException(getClashMessage(canonicalLocation, installArtifact, clashingArtifact));
         }
     }
 
     private void checkFileName(URI location, InstallArtifact installArtifact, String fileName) throws DuplicateFileNameException {
         if (this.uriByFileName.containsKey(fileName)) {
             InstallArtifact clashingArtifact = getArtifactByUri(this.uriByFileName.get(fileName));
             throw new DuplicateFileNameException(getClashMessage(location, installArtifact, clashingArtifact));
         }
     }
 
     private void checkDeploymentIdentity(URI location, InstallArtifact installArtifact, DeploymentIdentity deploymentIdentity)
         throws DuplicateDeploymentIdentityException {
         if (this.uriByIdentity.containsKey(deploymentIdentity)) {
             InstallArtifact clashingArtifact = getArtifactByUri(this.uriByIdentity.get(deploymentIdentity));
             throw new DuplicateDeploymentIdentityException(getClashMessage(location, installArtifact, clashingArtifact));
         }
     }
 
     private String getClashMessage(URI location, InstallArtifact installArtifact, InstallArtifact clashingArtifact) {
         return String.format(CLASH_MESSAGE_FORMAT, installArtifact, location, clashingArtifact);
     }
 
     private DeploymentIdentity getDeploymentIdentity(@NonNull InstallArtifact installArtifact) {
         return new StandardDeploymentIdentity(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion().toString());
     }
 
     private String getFileName(@NonNull URI location) throws DeploymentException {
         URI normalisedLocation = this.uriNormaliser.normalise(location);
         String path = normalisedLocation.getPath();
        if (path.endsWith(URI_PATH_SEPARATOR)) {
             path = path.substring(0, path.length() - 1);
         }
        int separatorIndex = path.lastIndexOf(URI_PATH_SEPARATOR);
         return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
     }
 
     /**
      * {@inheritDoc}
      */
     public InstallArtifact get(@NonNull DeploymentIdentity deploymentIdentity) {
         synchronized (this.monitor) {
             URI location = this.uriByIdentity.get(deploymentIdentity);
             return location == null ? null : getArtifactByUri(location);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public InstallArtifact get(@NonNull URI location) {
         synchronized (this.monitor) {
             return getArtifactByUri(location);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public URI getLocation(DeploymentIdentity deploymentIdentity) {
         synchronized (this.monitor) {
             return this.uriByIdentity.get(deploymentIdentity);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DeploymentIdentity[] getDeploymentIdentities() {
         synchronized (this.monitor) {
             Set<DeploymentIdentity> deploymentIdentities = this.uriByIdentity.keySet();
             return deploymentIdentities.toArray(new DeploymentIdentity[deploymentIdentities.size()]);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public InstallArtifact delete(DeploymentIdentity deploymentIdentity) throws DeploymentException {
         synchronized (this.monitor) {
             URI location = this.uriByIdentity.get(deploymentIdentity);
 
             if (location == null) {
                 return null;
             }
 
             InstallArtifact installArtifact = getArtifactByUri(location);
             Assert.notNull(installArtifact,
                 "Broken invariant: artifactByUri is missing an entry for URI '%s' but this URI is present in uriByIdentity for '%s'", location,
                 deploymentIdentity);
             String fileName = getFileName(location);
 
             Assert.isTrue(this.uriByFileName.containsKey(fileName),
                 "Broken invariant: uriByFileName is missing an entry for file name '%s' but URI '%s' is present in uriByIdentity for '%s'", fileName,
                 location, deploymentIdentity);
             removeState(deploymentIdentity, location, fileName);
             return installArtifact;
         }
     }
 
     private void updateState(URI location, InstallArtifact installArtifact, String fileName, DeploymentIdentity deploymentIdentity) {
         this.artifactByUri.put(getCanonicalFileLocation(location), installArtifact);
         this.uriByIdentity.put(deploymentIdentity, location);
         this.uriByFileName.put(fileName, location);
     }
 
     private void removeState(DeploymentIdentity deploymentIdentity, URI location, String fileName) {
         this.artifactByUri.remove(getCanonicalFileLocation(location));
         this.uriByIdentity.remove(deploymentIdentity);
         this.uriByFileName.remove(fileName);
     }
 
     private InstallArtifact getArtifactByUri(URI uri) {
         return this.artifactByUri.get(getCanonicalFileLocation(uri));
     }
 
     private URI getCanonicalFileLocation(URI uri) {
         if (SCHEME_FILE.equals(uri.getScheme())) {
             File file = new File(uri);
             try {
                 String canonicalPath = file.getCanonicalPath();
                 // Remove trailing slashes as these are added or not, for a directory, depending on the existence of the
                 // directory.
                 if (canonicalPath.endsWith(File.separator)) {
                     canonicalPath = canonicalPath.substring(0, canonicalPath.length() - 1);
                 }
                 // Add leading forward slash if this is not already present, for example "C:\xxx"
                 if (!canonicalPath.startsWith(URI_PATH_SEPARATOR)) {
                     canonicalPath = URI_PATH_SEPARATOR + canonicalPath;
                 }
                 // Construct a file scheme URI with the given path. Note that we can't use File.toURI as its results for
                 // a directory depends on the existence of the directory.
                 return new URI("file", null, canonicalPath, null);
             } catch (Exception e) {
                 throw new RuntimeException("Failed to calculate canonical file URI for '" + uri + "'", e);
             }
         } else {
             return uri;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isGCRoot(InstallArtifact installArtifact) {
         synchronized (this.monitor) {
             return this.artifactByUri.containsValue(installArtifact);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Iterator<InstallArtifact> iterator() {
         synchronized (this.monitor) {
             Collection<InstallArtifact> roots = this.artifactByUri.values();
             return new HashSet<InstallArtifact>(roots).iterator();
         }
     }
 }
