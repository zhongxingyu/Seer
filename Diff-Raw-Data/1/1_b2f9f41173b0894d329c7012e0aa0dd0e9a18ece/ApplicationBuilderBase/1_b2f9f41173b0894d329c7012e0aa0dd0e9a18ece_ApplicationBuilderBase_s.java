 /*
  * Copyright © 2013 Turkcell Teknoloji Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ttech.cordovabuild.domain.built;
 
 import com.ttech.cordovabuild.domain.application.ApplicationBuilt;
 import com.ttech.cordovabuild.domain.application.ApplicationFeature;
 import com.ttech.cordovabuild.domain.application.BuiltTarget;
 import com.ttech.cordovabuild.domain.application.BuiltType;
 import com.ttech.cordovabuild.domain.application.source.ApplicationSourceFactory;
 import com.ttech.cordovabuild.domain.asset.AssetRef;
 import com.ttech.cordovabuild.domain.asset.AssetService;
 import com.ttech.cordovabuild.infrastructure.FilesUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Date;
 import java.util.Set;
 
 public abstract class ApplicationBuilderBase implements ApplicationBuilder {
 
     private static Logger LOGGER = LoggerFactory
             .getLogger(ApplicationBuilderBase.class);
 
     private final Path buildPath;
     private final String createPath;
     private final ApplicationSourceFactory sourceFactory;
     protected final AssetService assetService;
     protected final BuiltType builtType;
     protected final ApplicationBuilt applicationBuilt;
 
 
     protected ApplicationBuilderBase(Path buildPath, String createPath, ApplicationSourceFactory sourceFactory, AssetService assetService, BuiltType builtType, ApplicationBuilt applicationBuilt) {
         this.buildPath = buildPath;
         this.createPath = createPath;
         this.sourceFactory = sourceFactory;
         this.assetService = assetService;
         this.builtType = builtType;
         this.applicationBuilt = applicationBuilt;
     }
 
     @Override
     public BuiltInfo buildApplication() {
         Date startDate = new Date();
         Path buildPath = createBuild(applicationBuilt);
         File buildPathFile = buildPath.toFile();
         Path webAssetPath = buildPath.resolve("www");
         try {
             LOGGER.info("removing web asset director {}", webAssetPath);
             FilesUtils.removeDirectory(webAssetPath);
         } catch (IOException e) {
             throw new PlatformBuiltException(e);
         }
         sourceFactory.createSource(applicationBuilt.getBuiltAssetRef(), webAssetPath);
         addPlatformSupport(builtType, buildPathFile);
         addFeatures(applicationBuilt.getBuiltConfig().getFeatures(), buildPathFile);
         buildPlatform(builtType, buildPathFile);
         return new BuiltInfo(buildPath, startDate, System.currentTimeMillis() - startDate.getTime(), builtType, applicationBuilt.getBuiltConfig().getApplicationName(), buildAsset(buildPath), BuiltTarget.Status.SUCCESS);
     }
 
     protected abstract AssetRef buildAsset(Path buildPath);
 
     private void buildPlatform(BuiltType builtType, File buildPath) {
         try {
             runProcess(buildPath, createPath, "build", builtType.getPlatformString());
         } catch (IOException e) {
             throw new PlatformBuiltException(e);
         } catch (InterruptedException e) {
             throw new PlatformBuiltException(e);
         }
     }
 
     private void addFeatures(Set<ApplicationFeature> features, File buildPath) {
         LOGGER.info("adding features with size {}", features.size());
         try {
             for (ApplicationFeature applicationFeature : features) {
                 addFeature(applicationFeature, buildPath);
             }
         } catch (IOException e) {
             throw new PlatformConfigurationException(e);
         } catch (InterruptedException e) {
             throw new PlatformConfigurationException(e);
         }
     }
 
     private void addFeature(ApplicationFeature applicationFeature, File buildPath) throws IOException, InterruptedException {
         for (String featureURI : applicationFeature.getCordovaURIs())
             runProcess(buildPath, createPath, "plugin", "add", featureURI);
     }
 
     private void addPlatformSupport(BuiltType builtType, File buildPath) {
         LOGGER.info("adding platform support for {}", builtType);
         try {
             int result = runProcess(buildPath, createPath, "platform", "add", builtType.getPlatformString());
             if (result != 0) {
                 throw new PlatformConfigurationException(result);
             }
         } catch (IOException e) {
             throw new PlatformConfigurationException(e);
         } catch (InterruptedException e) {
             throw new PlatformConfigurationException(e);
         }
     }
 
     private Path createBuild(ApplicationBuilt applicationBuilt) {
         LOGGER.info("starting to create application built path for {}", applicationBuilt.getId());
         Path templateDirectory = checkTemplateDirectory(applicationBuilt);
         try {
             int result = runProcess(buildPath.toFile(), createPath, "create", templateDirectory.getFileName().toString(), applicationBuilt.getBuiltConfig().getApplicationPackage(), applicationBuilt.getBuiltConfig().getApplicationName());
             if (result == 0) {
                 return templateDirectory;
             }
             throw new TemplateCreationException(result);
         } catch (IOException e) {
             throw new TemplateCreationException(e);
         } catch (InterruptedException e) {
             throw new TemplateCreationException(e);
         }
     }
 
     private int runProcess(File ownerPath, String... args) throws IOException, InterruptedException {
         ProcessBuilder pb = new ProcessBuilder(args);
         pb.directory(ownerPath);
         pb.inheritIO();
         Process p = pb.start();
         return p.waitFor();
     }
 
     private Path checkTemplateDirectory(ApplicationBuilt info) {
         Path templateDirectory = buildPath.resolve(info.getId().toString());
         if (Files.exists(templateDirectory)) {
             LOGGER.info("built directory {} exists", templateDirectory);
             try {
                 FilesUtils.removeDirectory(templateDirectory);
             } catch (IOException e) {
                 LOGGER.error("delete operation of {} filed", templateDirectory, e);
                 throw new TemplateCreationException(e);
             }
         }
         return templateDirectory;
     }
 }
