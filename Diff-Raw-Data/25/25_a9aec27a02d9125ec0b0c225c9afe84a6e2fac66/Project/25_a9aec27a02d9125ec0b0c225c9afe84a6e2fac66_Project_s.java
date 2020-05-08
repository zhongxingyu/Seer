 /*******************************************************************************
  *
  * Copyright (c) 2004-2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Kohsuke Kawaguchi, Jorg Heymans, Stephen Connolly, Tom Huybrechts, Anton Kozak, Nikita Levyankov
  *
  *
  *******************************************************************************/ 
 
 package hudson.model;
 
 import hudson.Util;
 import hudson.diagnosis.OldDataMonitor;
 import hudson.tasks.Fingerprinter;
 import hudson.tasks.Maven;
 import hudson.tasks.Maven.ProjectWithMaven;
 import hudson.tasks.Maven.MavenInstallation;
 import org.eclipse.hudson.api.model.IProject;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Buildable software project.
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class Project<P extends Project<P, B>, B extends Build<P, B>>
         extends BaseBuildableProject<P, B>
         implements SCMedItem, Saveable, ProjectWithMaven, BuildableItemWithBuildWrappers, IProject {
 
     /**
      * Creates a new project.
      */
     public Project(ItemGroup parent, String name) {
         super(parent, name);
     }
 
     public AbstractProject<?, ?> asProject() {
         return this;
     }
 
     @Override
     protected Set<ResourceActivity> getResourceActivities() {
         final Set<ResourceActivity> activities = new HashSet<ResourceActivity>();
 
         activities.addAll(super.getResourceActivities());
         activities.addAll(Util.filter(getBuildersList(), ResourceActivity.class));
         activities.addAll(Util.filter(getPublishersList(), ResourceActivity.class));
         activities.addAll(Util.filter(getBuildWrappersList(), ResourceActivity.class));
 
         return activities;
     }
 
     @Override
     public boolean isFingerprintConfigured() {
         return getPublishersList().get(Fingerprinter.class) != null;
     }
 
     public MavenInstallation inferMavenInstallation() {
         Maven m = getBuildersList().get(Maven.class);
         if (m != null) {
             return m.getMaven();
         }
         return null;
     }
     /**
      * @deprecated since 2006-11-05. Left for legacy config file compatibility
      */
     @Deprecated
     private transient String slave;

    private Object readResolve() {
        if (slave != null) {
            OldDataMonitor.report(this, "1.60");
        }
        return this;
    }
 }
