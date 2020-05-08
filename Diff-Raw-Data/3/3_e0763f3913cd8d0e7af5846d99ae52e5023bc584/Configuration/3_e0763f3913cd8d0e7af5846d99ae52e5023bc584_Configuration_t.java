 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.hive.ptest.conf;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class Configuration {
   
   private static final Logger LOG = LoggerFactory
       .getLogger(Configuration.class);
 
 
   private final Set<Host> hosts;
   private final String privateKey;
   private final String workingDirectory;
   private final Context context;
   private String antArgs;
   private String antEnvOpts;
   private String repository;
   private String repositoryName;
   private String patch;
   private String javaHome;
   private String branch;
   
   @VisibleForTesting
   public Configuration(Context context) {
     this.context = context;
     hosts = Sets.newHashSet();
     for(String alias : Splitter.on(" ").omitEmptyStrings().split(context.getString("hosts", ""))) {
       Context hostContext = new Context(context.getSubProperties(
           Joiner.on(".").join("host", alias, "")));
       LOG.info("Processing host {}: {}", alias, hostContext.getParameters().toString());
       hosts.add(new Host(hostContext.getString("host"), hostContext.getString("user"),
           Iterables.toArray(Splitter.on(",").trimResults().split(hostContext.getString("localDirs")), String.class),
           hostContext.getInteger("threads")));
     }
     Preconditions.checkState(hosts.size() > 0, "no hosts specified");
     repository =  Preconditions.checkNotNull(context.getString("repository"), "repository").trim();
     repositoryName =  Preconditions.checkNotNull(context.getString("repositoryName"), "repositoryName").trim();
     privateKey =  Preconditions.checkNotNull(context.getString("privateKey"), "privateKey").trim();
     branch =  Preconditions.checkNotNull(context.getString("branch"), "branch").trim();
     workingDirectory =  Preconditions.checkNotNull(context.getString("workingDirectory"), 
         "workingDirectory").trim();
     antArgs =  Preconditions.checkNotNull(context.getString("antArgs"), "antArgs").trim();
     antEnvOpts =  context.getString("antEnvOpts", "").trim();
    javaHome =  context.getString("javaHome", "").trim();
    patch = Strings.nullToEmpty(null);
   }
   public Set<Host> getHosts() {
     return hosts;
   }
   public Context getContext() {
     return context;
   }
   public String getRepositoryName() {
     return repositoryName;
   }
   public String getRepository() {
     return repository;
   }
 
   public String getBranch() {
     return branch;
   }
 
   public String getPrivateKey() {
     return privateKey;
   }
   public String getWorkingDirectory() {
     return workingDirectory;
   }
   public String getAntArgs() {
     return antArgs;
   }
   public String getJavaHome() {
     return javaHome;
   }
   public String getPatch() {
     return patch;
   }
   public void setPatch(String patch) {
     this.patch = Strings.nullToEmpty(patch);
   }
   public void setRepository(String repository) {
     this.repository = Strings.nullToEmpty(repository);
   }
   public void setRepositoryName(String repositoryName) {
     this.repositoryName = Strings.nullToEmpty(repositoryName);
   }
   public void setBranch(String branch) {
     this.branch = Strings.nullToEmpty(branch);
   }
   public void setJavaHome(String javaHome) {
     this.javaHome = Strings.nullToEmpty(javaHome);
   }
   public void setAntArgs(String antArgs) {
     this.antArgs = Strings.nullToEmpty(antArgs);
   }  
   public String getAntEnvOpts() {
     return antEnvOpts;
   }
   public void setAntEnvOpts(String antEnvOpts) {
     this.antEnvOpts = Strings.nullToEmpty(antEnvOpts);
   }
   @Override
   public String toString() {
     return "Configuration [privateKey=" + privateKey + ", workingDirectory="
         + workingDirectory + ", context=" + context + ", antArgs=" + antArgs
         + ", antEnvOpts=" + antEnvOpts + ", repository=" + repository
         + ", repositoryName=" + repositoryName + ", patch=" + patch
         + ", javaHome=" + javaHome + ", branch=" + branch + "]";
   }
   public static Configuration fromInputStream(InputStream inputStream)
       throws IOException {
     Properties properties = new Properties();
     properties.load(inputStream);
     Context context = new Context(Maps.fromProperties(properties));
     return new Configuration(context);
   }
   public static Configuration fromFile(String file) throws IOException {
     return fromInputStream(new FileInputStream(file));
   }
 }
