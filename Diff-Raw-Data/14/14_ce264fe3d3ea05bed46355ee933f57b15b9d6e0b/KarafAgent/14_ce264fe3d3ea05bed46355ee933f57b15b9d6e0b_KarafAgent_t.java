 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.agent.karaf;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.felix.karaf.features.FeaturesService;
 import org.apache.felix.karaf.gshell.admin.AdminService;
 import org.apache.felix.karaf.gshell.admin.Instance;
 import org.fusesource.cloudmix.agent.Bundle;
 import org.fusesource.cloudmix.agent.Feature;
 import org.fusesource.cloudmix.agent.FeatureList;
 import org.fusesource.cloudmix.agent.InstallerAgent;
 import org.fusesource.cloudmix.common.dto.AgentDetails;
 import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
 import org.fusesource.cloudmix.common.dto.ProvisioningAction;
 import org.fusesource.cloudmix.common.util.FileUtils;
 
 import static org.fusesource.cloudmix.agent.karaf.ConfigAdminHelper.merge;
 
 public class KarafAgent extends InstallerAgent {
 
     public static final String FEATURES_REPOSITORIES = "featuresRepositories";
    public static final String FEATURES_BOOT = "featuresBoot";
 
     protected static final String VM_PROP_SMX_HOME = "servicemix.home";
 
     protected static final String AGENT_WORK_DIR = File.separator + "data" + File.separator + "cloudmix";
     protected static final String AGENT_PROPS_PATH_SUFFIX = "agent.properties";
 
     private static final Log LOGGER = LogFactory.getLog(KarafAgent.class);
 
     private static final String FEATURE_FILE_KEY = KarafAgent.class.getName() + ".featureFile";
 
     private static final String SMX4_CONTAINER_TYPE = "smx4";
     private static final String JBI_TYPE = "jbi";
 
     private static final String[] SMX4_PACKAGE_TYPES = {
         "osgi", "jbi"
     };
 
     private static final String JBI_URL_PREFIX = "jbi:";
 
     private FeaturesService featuresService;
 
     private AdminService adminService;
 
     public void setFeaturesService(FeaturesService featuresService) {
         this.featuresService = featuresService;
     }
 
     public void setAdminService(AdminService adminService) {
         this.adminService = adminService;
     }
 
     @Override
     public String getDetailsPropertyFilePath() {
         if (propertyFilePath == null) {
             propertyFilePath = defaultWorkFile(AGENT_PROPS_PATH_SUFFIX);
         }
         return propertyFilePath;
     }
 
     @Override
     protected boolean validateAgent() {
         return featuresService != null && getWorkDirectory() != null;
     }
 
     @Override
     protected void installFeatures(ProvisioningAction action, String credentials, String resource)
         throws Exception {
         LOGGER.info("Installing CloudMix feature " + resource);
         Feature feat = new Feature(action.getFeature());
 
         if (resource.startsWith("karaf:")) {
             String name = getInstanceName(feat.getName());
             Instance instance = adminService.createInstance(name, 0, null);
             configureInstance(instance, resource);
             instance.start(null);
         } else {
             if (resource.startsWith("scan-features:")) {
                 resource = resource.substring("scan-features:".length());
             }
             int idx = resource.indexOf("!/");
             if (idx > 1) {
                 String repo = resource.substring(0, idx);
                 String feature = resource.substring(idx + 2);
                 URI repoUri = new URI(repo);
                 LOGGER.info("Adding feature repository " + repoUri);
                 featuresService.addRepository(repoUri);
                 LOGGER.info("Adding feature: " + feature);
                 featuresService.installFeature(feature);
             }
         }
         addAgentFeature(feat);
     }
 
     private void configureInstance(Instance instance, String resource) throws IOException {
         File config = new File(instance.getLocation(), "etc" + File.separator
                                                        + "org.apache.felix.karaf.features.cfg");
         Map<String, String> properties = new HashMap<String, String>();
         String[] parts = resource.split(" ");
         if (parts.length > 2) {
             properties.put(FEATURES_BOOT, parts[2]);
         }
         if (parts.length > 1) {
             properties.put(FEATURES_REPOSITORIES, parts[1]);
         }
         merge(config, properties);
     }
 
     @Override
     protected void installFeature(org.fusesource.cloudmix.agent.Feature feature,
                                   List<ConfigurationUpdate> featureCfgOverrides) {
 
         boolean success = false;
         URI reposUri = null;
         File featuresFile = null;
         try {
 
             featuresFile = File.createTempFile("features_", ".xml", getWorkDirectory());
             FileWriter writer = new FileWriter(featuresFile);
             writer.write(generateSMX4FeatureDoc(feature.getFeatureList()));
             writer.close();
             LOGGER.info("Wrote features document to " + featuresFile);
 
             reposUri = featuresFile.toURI();
             LOGGER.info("Adding features repository " + reposUri);
             featuresService.removeRepository(reposUri);
             featuresService.addRepository(reposUri);
             super.installFeature(feature, featureCfgOverrides);
 
             try {
                 featuresService.installFeature(feature.getName());
 
                 // TODO: check featuresService.listInstalledFeatures()?
             } catch (Exception e) {
                 LOGGER.error("Error installing feature " + feature, e);
                 LOGGER.debug(e);
             }
 
             success = true;
             LOGGER.info("installation of feature " + feature.getName() + " successful");
             feature.getAgentProperties().put(FEATURE_FILE_KEY, featuresFile);
 
         } catch (Exception e) {
             LOGGER.error("Error installing feature " + feature + ", exception " + e);
             LOGGER.debug(e);
         } finally {
             if (!success) {
                 LOGGER.warn("installFeature failed");
                 try {
                     if (reposUri != null) {
                         featuresService.removeRepository(reposUri);
                     }
                 } catch (Throwable t) {
                     LOGGER.warn("error removing repository " + reposUri, t);
                 }
                 if (featuresFile != null) {
                     featuresFile.delete();
                 }
             }
         }
     }
 
     @Override
     protected void uninstallFeature(Feature feature) {
         LOGGER.info("Uninstalling CloudMix feature " + feature);
 
         Instance instance = adminService == null ? null : adminService.getInstance(getInstanceName(feature
             .getName()));
         if (instance != null) {
             try {
                 instance.stop();
                 instance.destroy();
             } catch (Exception e) {
                 LOGGER.warn("Unable to stop/destroy a Karaf instance: " + e.getMessage(), e);
             }
         } else {
             String featureName = feature.getName();
             removeFeatureId(featureName);
             try {
                 featuresService.uninstallFeature(featureName);
                 // TODO: check featuresService.listInstalledFeatures()?
                 super.uninstallFeature(feature);
 
                 File featuresFile = (File)feature.getAgentProperties().get(FEATURE_FILE_KEY);
                 if (featuresFile == null) {
                     LOGGER.error("Cannot find features file for feature " + featureName);
                 } else {
                     URI reposUri = featuresFile.toURI();
                     LOGGER.info("Removing features repository " + reposUri);
                     featuresService.removeRepository(reposUri);
 
                     LOGGER.info("Deleting features file " + featuresFile);
                     featuresFile.delete();
                 }
                 feature.getAgentProperties().remove(FEATURE_FILE_KEY);
 
             } catch (Exception e) {
                 LOGGER.error("Error uninstalling feature " + featureName + ", exception " + e);
                 e.printStackTrace();
                 LOGGER.debug(e);
             }
         }
     }
 
     public String generateSMX4FeatureDoc(FeatureList fl) {
         StringBuilder sb = new StringBuilder().append("<features>\n");
         for (Feature feature : fl.getAllFeatures()) {
             sb.append("  <feature name=\"").append(feature.getName()).append("\">\n");
 
             for (String pn : feature.getPropertyNames()) {
                 sb.append("    <config name=\"").append(pn).append("\">\n");
 
                 Properties props = feature.getProperties(pn);
                 for (Object o : props.keySet()) {
                     sb.append("      ").append(o).append(" = ").append(props.get(o)).append("\n");
                 }
                 sb.append("    </config>\n");
             }
 
             Map<String, Bundle> unorderedBundles = new HashMap<String, Bundle>();
             for (Bundle b : feature.getBundles()) {
                 unorderedBundles.put(b.getUri(), b);
             }
 
             List<Bundle> orderedBundles = new ArrayList<Bundle>();
             for (Bundle b : feature.getBundles()) {
                 doOrderBundlesBasedOnTheirInterDependencies(b, orderedBundles, unorderedBundles);
             }
 
             for (Bundle b : orderedBundles) {
                 sb.append("    <bundle>");
                 String type = b.getType();
                 if (JBI_TYPE.equals(type)) {
                     sb.append(JBI_URL_PREFIX);
                 }
                 sb.append(b.getUri()).append("</bundle>\n");
             }
 
             sb.append("  </feature>\n");
         }
         sb.append("</features>\n");
         return sb.toString();
     }
 
     private void doOrderBundlesBasedOnTheirInterDependencies(Bundle b, List<Bundle> orderedBundles,
                                                              Map<String, Bundle> unOrderedBundles) {
         if (b == null) {
             return;
         }
 
         for (String depUri : b.getDepUris()) {
             doOrderBundlesBasedOnTheirInterDependencies(unOrderedBundles.get(depUri), orderedBundles,
                                                         unOrderedBundles);
         }
 
         String uri = b.getUri();
         if (unOrderedBundles.get(uri) != null) {
             orderedBundles.add(b);
             unOrderedBundles.remove(uri);
         }
     }
 
     @Override
     protected boolean installBundle(org.fusesource.cloudmix.agent.Feature feature, Bundle bundle) {
         return true;
     }
 
     @Override
     protected boolean uninstallBundle(Feature feature, Bundle bundle) {
         return false;
     }
 
     @Override
     public AgentDetails updateAgentDetails() {
         AgentDetails rc = super.updateAgentDetails();
 
         try {
             getClient().updateAgentDetails(getAgentId(), getAgentDetails());
         } catch (URISyntaxException e) {
             LOGGER.info("Problem updating agent information ", e);
             e.printStackTrace();
         }
         return rc;
     }
 
     @Override
     public void init() throws Exception {
 
         File dir = getWorkDirectory();
         if (dir != null) {
             if (!dir.exists()) {
                 FileUtils.createDirectory(dir);
             }
         }
 
         if (getContainerType() == null) {
             setContainerType(SMX4_CONTAINER_TYPE);
         }
         if (getSupportPackageTypes() == null || getSupportPackageTypes().length == 0) {
             setSupportPackageTypes(SMX4_PACKAGE_TYPES);
         }
 
         super.init();
     }
 
     private String defaultWorkFile(String fileName) {
 
         // Default work files based off the ServiceMix data directory.
 
         String path = System.getProperty(VM_PROP_SMX_HOME);
         if (path == null) {
             LOGGER.error("cannot determing ServiceMix home directory");
             return null;
         }
 
         path += AGENT_WORK_DIR;
         if (fileName != null) {
             path += File.separator + fileName;
         }
 
         LOGGER.info("using work file " + path);
         return path;
     }
 
     public String getInstanceName(String feature) {
         return feature.replaceAll(":", "_");
     }
    
    @Override
    public String getBaseHref() {
    	return "**unavailable**";
    }
 }
