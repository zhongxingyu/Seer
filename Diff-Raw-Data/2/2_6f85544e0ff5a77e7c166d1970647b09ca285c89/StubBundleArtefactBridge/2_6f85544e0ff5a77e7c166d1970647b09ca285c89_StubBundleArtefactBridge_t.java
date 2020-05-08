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
 
 package org.eclipse.virgo.kernel.artifact.bundle;
 
 import java.io.File;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.virgo.repository.ArtifactBridge;
 import org.eclipse.virgo.repository.ArtifactDescriptor;
 import org.eclipse.virgo.repository.Attribute;
 import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
 import org.eclipse.virgo.repository.internal.StandardAttribute;
 import org.osgi.framework.Version;
 
 /**
  * <p>
  * A Stub impl of Artefact Bridge, is pre-configured to return values from the test data set.
  * </p>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * This class is Threadsafe
  * 
  */
 final public class StubBundleArtefactBridge implements ArtifactBridge {
 
     private final Map<String, RepositoryAwareArtifactDescriptor> testArtefacts = new HashMap<String, RepositoryAwareArtifactDescriptor>();
 
     public StubBundleArtefactBridge() {
 
         testArtefacts.put(translate(TEST_FILE_URI), TEST_ARTEFACT);
         testArtefacts.put(translate(TEST_DIRECTORY_URI), TEST_DIRECTORY_ARTEFACT);
     }
 
     /**
      * {@inheritDoc}
      */
     public ArtifactDescriptor generateArtifactDescriptor(File artifact) {
         return this.testArtefacts.get(artifact.getName());
     }
 
     private String translate(URI uri) {
         return new File(uri).getName();
     }
 
     //TEST DATA
     
     private static final URI TEST_DIRECTORY_URI = URI.create("file:/src/test/resources/directories/y.different-" + new Version("1.2.3"));
 
     private static final Set<Attribute> TEST_ATTRIBUTE_SET_DIRECTORY = createAttributeSet("y.different", new Version("1.2.3"), TEST_DIRECTORY_URI);
 
     private final static RepositoryAwareArtifactDescriptor TEST_DIRECTORY_ARTEFACT = new StubRepositoryAwareArtifactDescriptor(TEST_DIRECTORY_URI, "y.different", new Version("1.2.3"), TEST_ATTRIBUTE_SET_DIRECTORY);
 
    private final static URI TEST_FILE_URI = new File(System.getProperty("user.home") + "/virgo-build-cache/ivy-cache/repository/org.apache.commons/com.springsource.org.apache.commons.dbcp/1.2.2.osgi/" + "com.springsource.org.apache.commons.dbcp-1.2.2.osgi.jar").toURI();
 
     private static final Set<Attribute> TEST_ATTRIBUTE_SET = createAttributeSet("com.springsource.org.apache.commons.dbcp", new Version("1.2.2.osgi"), TEST_FILE_URI);
 
     private final static RepositoryAwareArtifactDescriptor TEST_ARTEFACT = new StubRepositoryAwareArtifactDescriptor(TEST_FILE_URI, "com.springsource.org.apache.commons.dbcp", new Version("1.2.2.osgi"), TEST_ATTRIBUTE_SET);
 
     private static Set<Attribute> createAttributeSet(String name, Version version, URI uri) {
         Set<Attribute> attributes = new HashSet<Attribute>();
         attributes.add(new StandardAttribute("type", "test"));
         attributes.add(new StandardAttribute("name", name));
         attributes.add(new StandardAttribute("version", version.toString()));
         attributes.add(new StandardAttribute("uri", uri.toString()));
         return attributes;
     }
  
 }
