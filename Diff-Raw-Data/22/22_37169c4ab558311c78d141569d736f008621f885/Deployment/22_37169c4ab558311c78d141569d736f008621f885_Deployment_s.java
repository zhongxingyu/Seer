 /*
  * Copyright 2013 the original author or authors.
  * Copyright 2013 SorcerSoft.org.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sorcer.core.deploy;
 
 import java.io.File;
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import net.jini.id.Uuid;
 import sorcer.service.Arg;
 import sorcer.util.Sorcer;
 
 /**
  * Attributes related to signature based deployment.
  *
  * @author Mike Sobolewski
  * @author Dennis Reedy
  */
 public class Deployment implements Arg, Serializable {
     private static final long serialVersionUID = 1L;
 
     /**
      * There are three types of provisioning: per signature (SELF), for a
      * collection of signatures as a federation (all signatures in exertion with
      * Type.FED), and no provisioning (suspended provisioning) for a signature
      * (NONE)
      */
     public enum Type {
         SELF, FED, NONE
     }
     private Type type = Type.FED;
     
     /**
      * A service can be deployed with uniqueness. If a service is unique, a new instance is always
      * created. If a service is not unique, the following behavior comes into play:
      * <ul>
      *     <li>If the service is @{code Type.SELF} it will be checked to see if has already been deployed. If not
      *     deployed it will be created in it's own deployment. If found it will not be deployed.</li>
      *     <li>If the service is @{code Type.FED} it will be checked to see if has already been deployed. If not
      *     deployed it will be created as part of a deployment. If deployed, the number of instances will be
      *     incremented within it's current deployment.</li>
      * </ul>. If a service is not
      */
     public enum Unique {
         YES, NO
     }
     private Unique unique = Unique.NO;
 
     private int maxPerCybernode;
 
     private String name;
     private Uuid providerUuid;
     private int multiplicity = 1;
     private String[] codebaseJars;
     private String[] classpathJars;
 
     // serviceType and providerName are given in Signatures,
     // can be used for querying relevant Deployments
     // to be associated with signatures
     private String serviceType;
     private String providerName;
    private String impl = "ServiceTasker";
     private String websterUrl = Sorcer.getWebsterUrl();
     private String[] configs;
 
     // an idle time for un-provisioning
     private int idle = 0; /* Value is in minutes */
     public static final int DEFAULT_IDLE_TIME = 5;
 
     private Boolean fork;
     private String jvmArgs;
 
     public Deployment() {
     }
 
     public Deployment(final String... configs) {
         setConfigs(configs);
     }
 
     public void setConfigs(final String... configs) {
         for (int i = 0; i < configs.length; i++)
             if (!configs[i].startsWith("/"))
                 configs[i] = System.getenv("SORCER_HOME") + File.separatorChar + configs[i];
         this.configs = configs;
     }
 
     public String[] getConfigs() {
         return configs;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see sorcer.service.Arg#getName()
      */
     @Override
     public String getName() {
         return name;
     }
 
     public Uuid getProviderUuid() {
         return providerUuid;
     }
 
     public void setProviderUuid(final Uuid providerUuid) {
         this.providerUuid = providerUuid;
     }
 
     public int getMultiplicity() {
         return multiplicity;
     }
 
     public void setMultiplicity(final int multiplicity) {
         this.multiplicity = multiplicity;
     }
 
     public Integer getMaxPerCybernode() {
         return maxPerCybernode;
     }
 
     public void setMaxPerCybernode(int maxPerCybernode) {
         this.maxPerCybernode = maxPerCybernode;
     }
 
     public String[] getCodebaseJars() {
         return codebaseJars;
     }
 
     public void setCodebaseJars(final String[] dls) {
         for (int i = 0; i < dls.length; i++)
             if (!dls[i].startsWith("file://") || !dls[i].startsWith("http://"))
                 dls[i] = websterUrl + File.separatorChar + dls[i];
         this.codebaseJars = dls;
     }
 
     public String[] getClasspathJars() {
         return classpathJars;
     }
 
     public void setClasspathJars(final String[] jars) {
         this.classpathJars = jars;
     }
 
     public String getImpl() {
         return impl;
     }
 
     public void setImpl(final String impl) {
         this.impl = impl;
     }
 
     public String getServiceType() {
         return serviceType;
     }
 
     public void setServiceType(final String serviceType) {
         this.serviceType = serviceType;
     }
 
     public String getProviderName() {
         return providerName;
     }
 
     public void setProviderName(final String providerName) {
         this.providerName = providerName;
     }
 
     public String getWebsterUrl() {
         return websterUrl;
     }
 
     public void setWebsterUrl(final String websterUrl) {
         this.websterUrl = websterUrl;
     }
 
     public int getIdle() {
         return idle;
     }
 
     public void setIdle(final int idle) {
         this.idle = idle;
     }
 
     public void setIdle(final String idle) {
         this.idle = parseInt(idle);
     }
 
     public static int parseInt(String idle) {
 		String timeout = idle.trim();
 		int delay;
 		char last = timeout.charAt(timeout.length()-1);
 		if (last == 'h') {
 			delay = Integer.parseInt(timeout.substring(0, timeout.length()-1)) * 60;
 		} else if (last == 'd') {
 			delay = Integer.parseInt(timeout.substring(0, timeout.length()-1)) * 60 * 24;
 		} else {
 			delay = Integer.parseInt(timeout);
 		}
 		return delay;
 	}
 
     public Boolean getFork() {
         return fork;
     }
 
     public void setFork(final boolean fork) {
         this.fork = Boolean.valueOf(fork);
     }
 
     public String getJvmArgs() {
         return jvmArgs;
     }
 
     public void setJvmArgs(final String jvmArgs) {
         this.jvmArgs = jvmArgs;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(final Type type) {
         this.type = type;
     }
 
     public void setUnique(final Unique unique) {
         this.unique = unique;
     }
 
     public Unique getUnique() {
         return unique;
     }
     
     public static String createDeploymentID(final String ssb) throws NoSuchAlgorithmException {
     	MessageDigest md = MessageDigest.getInstance("MD5");
 		md.update(ssb.getBytes());
 		byte byteData[] = md.digest();
 		// convert the byte to hex
 		StringBuilder hexString = new StringBuilder();
         for (byte data : byteData) {
             String hex = Integer.toHexString(0xff & data);
             if (hex.length() == 1)
                 hexString.append('0');
             hexString.append(hex);
         }
 		return hexString.toString();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(impl).append("\n");
         sb.append(Arrays.toString(classpathJars)).append("\n");
         sb.append(Arrays.toString(codebaseJars)).append("\n");
         sb.append(Arrays.toString(configs)).append("\n");
         sb.append("URL: ").append(websterUrl).append("\n");
         sb.append("idle: ").append(idle).append("\n");
         sb.append("maintain: ").append(multiplicity).append("\n");
 
         return sb.toString();
     }
 }
