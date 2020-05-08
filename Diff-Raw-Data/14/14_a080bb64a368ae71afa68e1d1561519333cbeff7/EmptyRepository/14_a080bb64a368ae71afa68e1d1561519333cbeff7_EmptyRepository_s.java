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
 
 package org.eclipse.virgo.kernel.services.repository.internal;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.virgo.repository.Query;
 import org.eclipse.virgo.repository.Repository;
 import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
 import org.eclipse.virgo.util.osgi.VersionRange;
 
 /**
  * TODO Document EmptyRepository
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * TODO Document concurrent semantics of EmptyRepository
  * 
  */
 public class EmptyRepository implements Repository {
 
     private static final Query EMPTY_QUERY = new EmptyQuery();
 
     /**
      * {@inheritDoc}
      */
     public Query createQuery(String key, String value) {
         return EMPTY_QUERY;
     }
 
     /**
      * {@inheritDoc}
      */
     public Query createQuery(String key, String value, Map<String, Set<String>> properties) {
         return EMPTY_QUERY;
     }
 
     /**
      * {@inheritDoc}
      */
     public RepositoryAwareArtifactDescriptor get(String type, String name, VersionRange versionRange) {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getName() {
         return "empty";
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop() {
     }
 
     private static final class EmptyQuery implements Query {
 
         /**
          * {@inheritDoc}
          */
         public Query addFilter(String name, String value) {
             return this;
         }
 
         /**
          * {@inheritDoc}
          */
         public Query addFilter(String name, String value, Map<String, Set<String>> properties) {
             return this;
         }
 
         /**
          * {@inheritDoc}
          */
         public Set<RepositoryAwareArtifactDescriptor> run() {
             return Collections.<RepositoryAwareArtifactDescriptor> emptySet();
         }
     }
 }
