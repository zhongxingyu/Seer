 /*
  * Copyright 2011 Andreas Sahlbach
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sahlbach.maven.delivery;
 
 import com.google.common.collect.Lists;
 import com.sahlbach.maven.delivery.upload.Uploader;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.components.interactivity.PrompterException;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResolutionException;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

 /**
  * User: Andreas Sahlbach
  * Date: 06.08.11
  * Time: 11:24
  */
 public class Upload extends AbstractSshRemoteJob {
 
     public final static String DEFAULT_FILE_MASK = "0644";
     public final static String DEFAULT_TYPE = "scp";
     /**
      * target directory on the remote server
      * @parameter
      * @required
      */
     private String targetDir;
 
     /**
      * local fileset to upload
      * @parameter
      */
     private Fileset fileset;
 
     /**
      * list of artifacts to upload. artifacts will be fetched from repository first
      * @parameter
      */
     private List<DeliveryArtifact> artifacts;
 
     /**
      * File mask to use for copied files. Default is 0644
      * (only works if no executable is defined. in case you want to use an external executable, you
      * need to setup ssh so that the file mask fits automagically or you need to correct the mask
      * via ssh yourself with an exec job)
      *
      * @parameter
      */
     private String fileMask;
 
     /**
      * Optional list of regexp to rename files during copy. The first regexp that matches will be used to rename the
      * file. The file path is excluding of this operation and the regexp match attempt.
      * @parameter
      */
    private List<RenameRegexp> renameRegexps = Lists.newArrayList();
 
     /**
      * merge with the given upload object
      * @param upload upload instance to merge in (overwrites local data)
      * @return the merged instance (this) for call chaining
      * @throws MojoExecutionException in case of merge conflicts
      */
     public Upload mergeWith(Upload upload) throws MojoExecutionException {
         super.mergeWith(upload);
         if(upload.getTargetDir() != null)
             setTargetDir(upload.getTargetDir());
         if(upload.getFileset() != null)
             setFileset(upload.getFileset());
         if(upload.getFileMask() != null)
             setFileMask(upload.getFileMask());
         if(upload.getArtifacts() != null)
             setArtifacts(upload.getArtifacts());
         if(upload.getRenameRegexps() != null) {
             if(getRenameRegexps() == null)
                 setRenameRegexps(Lists.newArrayList(upload.getRenameRegexps()));
             else
                 getRenameRegexps().addAll(0,upload.getRenameRegexps());
         }
         return this;
     }
 
     public void execute (DeliveryMojo mojo) throws MojoExecutionException, MojoFailureException {
         String type = getType() != null ? getType() : DEFAULT_TYPE;
         Uploader uploader = Uploader.createUploader(type.toLowerCase(), mojo.getLog());
         if(uploader == null) {
             throw new MojoExecutionException("Can't find Uploader of type " + getType());
         }
         List<File> filesToUpload = resolveFileset();
 
         if(artifacts != null)
             filesToUpload.addAll(resolveArtifacts(mojo));
 
         Map<File,String> filesWithTargetNames = calculateTargetNames(filesToUpload);
 
         uploader.uploadFiles(filesWithTargetNames, this, mojo);
     }
 
     private Map<File,String> calculateTargetNames (List<File> filesToUpload) {
         Map<File,String> filesWithTargetNames = new HashMap<File, String>(filesToUpload.size());
         for (File file : filesToUpload) {
             boolean renamed = false;
             for (RenameRegexp renameRegexp : renameRegexps) {
                 Matcher matcher = renameRegexp.getFromPattern().matcher(file.getName());
                 if(matcher.matches()) {
                     filesWithTargetNames.put(file,matcher.replaceAll(renameRegexp.getTo()));
                     renamed = true;
                     break;
                 }
             }
             if(!renamed) {
                 filesWithTargetNames.put(file,file.getName());
             }
         }
         return filesWithTargetNames;
     }
 
     public List<File> resolveArtifacts(DeliveryMojo mojo) throws MojoFailureException, MojoExecutionException {
 
         List<File> resultList = new ArrayList<File>(artifacts.size());
 
         for (DeliveryArtifact deliveryArtifact : artifacts) {
             if((deliveryArtifact.getVersion() == null || deliveryArtifact.isPromptForVersion()) && mojo.isInteractiveMode()) {
                 try {
                     promptForVersion(deliveryArtifact,mojo);
                 } catch (PrompterException e) {
                     throw new MojoExecutionException("Prompter exception.",e);
                 }
             }
             Artifact artifact;
             try {
                 artifact = new DefaultArtifact( deliveryArtifact.toString() );
             }
             catch ( IllegalArgumentException e ) {
                 throw new MojoFailureException( e.getMessage(), e );
             }
             ArtifactRequest request = new ArtifactRequest();
             request.setArtifact( artifact );
             request.setRepositories( mojo.getRemoteRepos() );
 
             mojo.getLog().debug( "Resolving artifact " + artifact + " from " + mojo.getRemoteRepos() );
 
             ArtifactResult result;
             try {
                 result = mojo.getRepoSystem().resolveArtifact(mojo.getRepoSession(), request);
             }
             catch ( ArtifactResolutionException e ) {
                 throw new MojoExecutionException( e.getMessage(), e );
             }
             mojo.getLog().info( "Resolved artifact " + artifact + " to " + result.getArtifact().getFile() + " from "
                                 + result.getRepository() );
             resultList.add(result.getArtifact().getFile());
         }
         return resultList;
     }
 
     private void promptForVersion(DeliveryArtifact deliveryArtifact, DeliveryMojo mojo) throws PrompterException {
         StringBuilder message = new StringBuilder("Enter the version of ")
                     .append(deliveryArtifact.getGroupId()).append(":")
                     .append(deliveryArtifact.getArtifactId()).append(":")
                     .append(deliveryArtifact.getExtension());
         if(deliveryArtifact.getClassifier() != null) {
             message.append(":").append(deliveryArtifact.getClassifier());
         }
         message.append(" that you want to deliver :");
         String defaultReply;
         if(deliveryArtifact.getVersion() != null) {
             defaultReply = deliveryArtifact.getVersion();
         } else {
             defaultReply = mojo.getProjectVersion();
         }
         deliveryArtifact.setVersion(mojo.getPrompter().prompt(message.toString(),defaultReply));
     }
 
     private List<File> resolveFileset () {
         String[] includedFiles = new String[0];
         if ( fileset != null ) {
             fileset.scan();
             includedFiles = fileset.getIncludedFiles();
         }
         List<File> result = new ArrayList<File>(includedFiles.length);
         for (String includedFile : includedFiles) {
             result.add(new File(fileset.getBasedir(), includedFile));
         }
         return result;
     }
 
     public String getTargetDir () {
         return targetDir;
     }
 
     public Fileset getFileset () {
         return fileset;
     }
 
     public List<DeliveryArtifact> getArtifacts () {
         return artifacts;
     }
 
     public String getFileMask () {
         return fileMask;
     }
 
     public List<RenameRegexp> getRenameRegexps () {
         return renameRegexps;
     }
 
     public void setRenameRegexps (List<RenameRegexp> renameRegexps) {
         this.renameRegexps = renameRegexps;
     }
 
     public void setTargetDir(String targetDir) {
         this.targetDir = targetDir;
     }
 
     public void setFileset(Fileset fileset) {
         this.fileset = fileset;
     }
 
     public void setArtifacts(List<DeliveryArtifact> artifacts) {
         this.artifacts = artifacts;
     }
 
     public void setFileMask(String fileMask) {
         this.fileMask = fileMask;
     }
 }
