 /*
  * Copyright 2011 Exentes
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.exentes.maven.versions;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.*;
 import org.apache.maven.Maven;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Profile;
 import org.apache.maven.project.MavenProject;
 
 import java.util.IdentityHashMap;
 import java.util.Map;
 
 import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
 import static java.util.Collections.emptyList;
 
 /**
  * @author Sergey Tavanets
  */
 public class VersionMojoUtils {
 
     public static Predicate<Artifact> snapshotFilter =
             new Predicate<Artifact>() {
                 @Override
                 public boolean apply(Artifact input) {
                     return input.isSnapshot();
                 }
             };
 
    private static Function<MavenProject, Artifact> toProjectArtifactFunction =
            new Function<MavenProject, Artifact>() {
                @Override
                public Artifact apply(MavenProject from) {
                    return from.getArtifact();
                }
            };

     public static String projectArtifactKey(MavenProject project, Artifact artifact) {
         return project.getId() + "->" + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                 + artifact.getType() + (artifact.getClassifier() != null ? artifact.getClassifier() : "");
     }
 
     public static String projectDependencyKey(MavenProject project, Dependency dependency) {
         return project.getId() + "->" + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
                 + dependency.getType() + (dependency.getClassifier() != null ? dependency.getClassifier() : "");
     }
 
     private static Iterable<Artifact> filterSnapshots(Iterable<Artifact> artifacts) {
         return filter(artifacts, snapshotFilter);
     }
 
     public static SetMultimap<Artifact, MavenProject> allSnapshotProjectArtifacts(MavenSession session) {
         SetMultimap<Artifact, MavenProject> allSnapshotProjectArtifacts = HashMultimap.create();
 
         for (MavenProject project : session.getProjects()) {
            for (Artifact artifact : difference(
                    newHashSet(filterSnapshots(project.getDependencyArtifacts())),
                    newHashSet(transform(session.getProjects(), toProjectArtifactFunction)))) {
                 allSnapshotProjectArtifacts.put(artifact, project);
             }
         }
         return allSnapshotProjectArtifacts;
     }
 
     public static String releaseVersion(String snapshotVersion) {
         return snapshotVersion.replace("-SNAPSHOT", "");
     }
 
     private static Map<String, Dependency> allDependencyManagementMap(MavenSession session) {
         Map<String, Dependency> allDependencyManagementMap = Maps.newHashMap();
         for (MavenProject project : session.getProjects()) {
             for (Dependency dependency : concat(dependencyManagement(project), allActiveProfilesDependencyManagement(project))) {
                 allDependencyManagementMap.put(projectDependencyKey(project, dependency), dependency);
             }
         }
         return allDependencyManagementMap;
     }
 
     private static Iterable<Dependency> allActiveProfilesDependencyManagement(MavenProject project) {
         Iterable<Dependency> allDependencies = emptyList();
         for (Profile profile : project.getActiveProfiles()) {
             if (profile.getDependencyManagement() != null) {
                 allDependencies = concat(emptyIfNull(profile.getDependencyManagement().getDependencies()), allDependencies);
             }
         }
         return allDependencies;
     }
 
     private static Iterable<Dependency> allActiveProfilesDependencies(MavenProject project) {
         Iterable<Dependency> allDependencies = emptyList();
         for (Profile profile : project.getActiveProfiles()) {
             allDependencies = concat(emptyIfNull(profile.getDependencies()), allDependencies);
         }
         return allDependencies;
     }
 
     private static Iterable<Dependency> dependencyManagement(MavenProject project) {
         if (project.getOriginalModel().getDependencyManagement() != null) {
             return project.getOriginalModel().getDependencyManagement().getDependencies();
         } else {
             return emptyList();
         }
     }
 
     private static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
         if (iterable == null) {
             return emptyList();
         } else {
             return iterable;
         }
     }
 
     public static Map<String, Dependency> allDependenciesMap(MavenSession session) {
         Map<String, Dependency> allDependencyManagementMap = allDependencyManagementMap(session);
         Map<String, Dependency> allDependenciesMap = Maps.newHashMap();
         for (MavenProject project : session.getProjects()) {
             for (Dependency dependency : concat(emptyIfNull(project.getOriginalModel().getDependencies()), allActiveProfilesDependencies(project))) {
                 if (dependency.getVersion() != null) {
                     allDependenciesMap.put(projectDependencyKey(project, dependency), dependency);
                 } else {
                     // dependency version is not specified. Perhaps it is specified in a dependencyManagement section
                     // of the project of one of its parents
                     Dependency parentDependency = null;
                     MavenProject projectToCheck = project;
                     while (parentDependency == null && projectToCheck != null) {
                         parentDependency = allDependencyManagementMap.get(projectDependencyKey(projectToCheck, dependency));
                         if (parentDependency != null) {
                             allDependenciesMap.put(projectDependencyKey(project, dependency), parentDependency);
                         } else {
                             if (!projectToCheck.isExecutionRoot()) {
                                 projectToCheck = projectToCheck.getParent();
                             } else {
                                 projectToCheck = null;
                             }
                         }
                     }
                 }
             }
         }
         return allDependenciesMap;
     }
 
     public static Map<Object, MavenProject> origins(MavenSession session) {
         Map<Object, MavenProject> origins = new IdentityHashMap<Object, MavenProject>();
 
         for (MavenProject project : session.getProjects()) {
             if (project.getOriginalModel().getProperties() != null) {
                 origins.put(project.getOriginalModel().getProperties(), project);
             }
             for (Profile profile : project.getActiveProfiles()) {
                 if (profile.getProperties() != null) {
                     origins.put(profile.getProperties(), project);
                 }
             }
             for (Dependency dependency : concat(
                     emptyIfNull(project.getOriginalModel().getDependencies()),
                     dependencyManagement(project),
                     allActiveProfilesDependencies(project),
                     allActiveProfilesDependencyManagement(project))) {
                 origins.put(dependency, project);
             }
         }
         return origins;
     }
 
     public static boolean isPropertyPlaceholder(String value) {
         return value != null && value.startsWith("${");
     }
 
     public static String getPropertyName(String propertyPlaceholder) {
         return propertyPlaceholder.substring(2, propertyPlaceholder.length() - 1);
     }
 }
