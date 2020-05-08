 package ch.rotscher.mavenext;
 
 /**
  * Copyright 2012 Roger Brechbühl
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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 import org.apache.maven.MavenExecutionException;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.io.DefaultModelReader;
 import org.apache.maven.model.io.ModelReader;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
 
 /**
  * <p>
  * extension of {@link org.apache.maven.model.io.ModelReader} for overriding ${project.version} with the value given as a Java system property:
  * </p>
  * <p>
  * <code>-Dversion.override=A-VERSION</code><br />
  * <br />
  * other options are:<br />
  * <code>-Dversion.override.fail-on-error=[true | false]</code><br />
  * <code>-Dversion.override.check-snapshot-dependency=[true | false]</code><br />
  * </p>
  * <p>
  * This class helps releasing a project (from a maven point of view, that means not having "SNAPSHOT" in the version) without actually changing (and committing)
  * the pom.xml
  * </p>
  * <p>
  * Copyright by Roger Brechbühl, 2012
  * </p>
  * 
  * @author Roger Brechbühl
  * 
  */
 @Component(role = ModelReader.class, hint = "custom-version-model-reader")
 public class CustomVersionModelReader extends DefaultModelReader implements ModelReader {
 
     static final String MAVENEXT_RELEASE_VERSION = "version.override";
     static final String MAVENEXT_CHECK_SNAPSHOT_DEP_FAIL_ON_ERROR = "version.override.fail-on-error";
     static final String MAVENEXT_CHECK_SNAPSHOT_DEP = "version.override.check-snapshot-dependency";
 
     @Requirement
    private Logger logger = new ConsoleLogger();
 
     @Override
     public Model read(InputStream input, Map<String, ?> options) throws IOException {
         Model model = super.read(input, options);
         // this property is set from the command line with -Dversion.override
         String version = System.getProperty(MAVENEXT_RELEASE_VERSION);
         if (version == null || version.trim().length() < 1) {
             return model;
         }
 
         String currentGroupId = getGroupId(model);
         GroupIdOfRootPom groupId = GroupIdOfRootPom.getInstance(currentGroupId, logger);
 
         if (currentGroupId.startsWith(groupId.groupId)) {
             if (logger.isDebugEnabled()) {
                 logger.debug(String.format("changing version of %s to %s", model, version));
             }
             model.setVersion(version);
             Parent parent = model.getParent();
             if (parent != null && parent.getGroupId().startsWith(groupId.groupId)) {
                 logger.debug(String.format("changing version of parent  %s  to %s", parent, version));
                 parent.setVersion(version);
             }
         }
 
         Boolean checkSnapshotDependencies = Boolean.parseBoolean(System.getProperty(MAVENEXT_CHECK_SNAPSHOT_DEP));
         Boolean failOnError = Boolean.parseBoolean(System.getProperty(MAVENEXT_CHECK_SNAPSHOT_DEP_FAIL_ON_ERROR));
 
         if (checkSnapshotDependencies == Boolean.TRUE) {
             for (Dependency dep : model.getDependencies()) {
                 String depVersion = dep.getVersion();
                 if (depVersion != null && depVersion.contains("SNAPSHOT")) {
                     File pomFile = model.getPomFile();
                     String pomFilePath = "[path not available]";
                     if (pomFile != null) {
                         pomFilePath = pomFile.getPath();
                     }
                     String errorMsg = String.format("there is a snapshot dependency (%s) in %s (%s)", dep, model, pomFilePath);
                     if (failOnError == Boolean.TRUE) {
                        logger.warn(errorMsg);
                         // wrap in a IOException as this one is thrown in the signature
                         throw new IOException(new MavenExecutionException(errorMsg, model.getPomFile()));
                     } else {
                         logger.warn(errorMsg);
                     }
                 }
             }
         }
 
         return model;
     }
 
     /**
      * get the groupId of the given model. if not set then ask the parent
      * 
      * @param model
      * @return the groupId of the given module
      */
     private String getGroupId(Model model) {
         if (model.getGroupId() != null) {
             return model.getGroupId();
         }
 
         if (model.getParent() != null) {
             return model.getParent().getGroupId();
         }
 
         // TODO: return null if no groupId can be found!
         logger.warn(String.format(
                         "no groupId found for model %s (no clue why this happens :-(. But has no negative affect :-) Maybe this is the super pom!!??", model));
         return "n/A";
     }
 
     /**
      * set by the unit test.
      * 
      * @param logger
      *            the logger
      */
     void setLogger(Logger logger) {
         this.logger = logger;
     }
 
     /**
      * helper class to store the very first groupId which is the base groupId for all following modules:
      * 
      * the modules version is changed if the test <code>if module.groupId starts with GroupIdOfRootPom.groupId</code> is true
      * 
      */
     static class GroupIdOfRootPom {
         private static GroupIdOfRootPom INSTANCE;
 
         private final String groupId;
 
         private GroupIdOfRootPom(String groupId) {
             this.groupId = groupId;
         }
 
         public static GroupIdOfRootPom getInstance(String groupId, Logger logger) {
             if (INSTANCE == null) {
                 INSTANCE = new GroupIdOfRootPom(groupId);
                System.out.println("logger: " + logger);
                 logger.info(String
                        .format("initialize groupId to '%s', the version of all modules and dependencies starting with this groupdId will be changed!",
                                groupId));
             }
 
             return INSTANCE;
 
         }
     }
 
 }
