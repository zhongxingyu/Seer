 package org.fiteagle.core.config;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class InterfaceConfiguration {
   
   
 
 private FiteaglePreferences preferences;
   
   private static InterfaceConfiguration interfaceConfig;
   private Configurator configurator ;
   private String DEFAULT_URN_PREFIX = "urn:publicid:IDN";
  private String DEFAULT_DOMAIN = "fiteagle";
   private String DEFAULT_AM_URN = DEFAULT_URN_PREFIX+"+"+DEFAULT_DOMAIN+"+"+"authority+am";
   private String DEFAULT_SA_URN = DEFAULT_URN_PREFIX+"+"+DEFAULT_DOMAIN+"+"+"authority+sa";
  private String DEFAULT_HOSTHAME = "fiteagle.org";
  private String DEFAULT_AM_URL = "https://fuseco.fokus.fraunhofer.de/api/sfa/am/v3";
   
 //  private String DEFAULT_TESTBED_DESCRIPTION = "FUSECO Playground. The independent testbed for FUture SEamless COmmunication";
 //  private String DEFAULT_TESTBED_HOMEPAGE="https://fuseco.fokus.fraunhofer.de";
 //  private String DEFAULT_TESTBED_PICTURE = "http://www.fokus.fraunhofer.de/en/fokus_testbeds/fuseco_playground/_logos/FUSECO_Playground_Logo.jpg";
 ////  private HashMap<String, String> DEFAULT_ENDORSED_TOOLS = new HashMap<String, String>();
 //  private String DEFAULT_ENDORSED_TOOL_NAMES="Fed4FIRE portal, Flack, Omni, SFI, Zabbix";
 //  private String DEFAULT_ENDORSED_TOOL_LOGOS = "https://fed4fire.myslice.info/templates/myslice/images/myslice-logo.png, http://www.protogeni.net/logo.jpg, http://trac.gpolab.bbn.com/gcf/chrome/common/geni/header.gif, http://opensfa.info/images/logo_big.png, https://www.zabbix.org/mw/images/b/bf/Zabbix_logo.png";//TODO: set the links to the logos
 //  private String DEFAULT_ENDORSED_TOOL_HOMEPAGES = "https://fed4fire.myslice.info/, http://www.protogeni.net/ProtoGeni/wiki/Flack, http://trac.gpolab.bbn.com/gcf/wiki/Omni, http://opensfa.info/, https://www.zabbix.org";//TODO: set the links to the homepages
 //  private String DEFAULT_ENDORSED_TOOL_VERSIONS = "1.1, 0, 2.2.1, 0, 2.0.6";//TODO: set the versions
   
   private String DEFAULT_TESTBED_DESCRIPTION = "Please set testbed description over java preferences";
   private String DEFAULT_TESTBED_HOMEPAGE="please set testbed homepage over java preferences";
   private String DEFAULT_TESTBED_PICTURE = "please set testbed picture over java preferences";
 //  private HashMap<String, String> DEFAULT_ENDORSED_TOOLS = new HashMap<String, String>();
   private String DEFAULT_ENDORSED_TOOL_NAMES="Please set tool names over java preferences (seperated by comma)";
   private String DEFAULT_ENDORSED_TOOL_LOGOS = "Pleas set tool logos over java preferences (seperated by comma)";//TODO: set the links to the logos
   private String DEFAULT_ENDORSED_TOOL_HOMEPAGES = "Please set your tool homepages over java preferences (seperated by comma)";//TODO: set the links to the homepages
   private String DEFAULT_ENDORSED_TOOL_VERSIONS = "Please set your tool versions over java preferences (seperated by comma)";//TODO: set the versions
   
   private InterfaceConfiguration(){
     
    preferences = new FiteaglePreferencesXML(getClass());
     if(preferences.get("am_urn")== null)
       preferences.put("am_urn", DEFAULT_AM_URN);
     
    if(preferences.get("hosthame") == null)
      preferences.put("hostname", DEFAULT_HOSTHAME);
     
     if(preferences.get("am_url") == null)
       preferences.put("am_url", DEFAULT_AM_URL);
     
 
     if(preferences.get("domain") == null)
       preferences.put("domain", DEFAULT_DOMAIN);
     
 
     if(preferences.get("urn_prefix") == null)
       preferences.put("urn_prefix", DEFAULT_URN_PREFIX);
     
     if(preferences.get("sa_urn")==null){
       preferences.put("sa_urn", DEFAULT_SA_URN);
     }
     
     if(preferences.get("testbed_description")==null){
         preferences.put("testbed_description", DEFAULT_TESTBED_DESCRIPTION);
     }
     
     if (preferences.get("testbed_homepage")==null) {
     	preferences.put("testbed_homepage", DEFAULT_TESTBED_HOMEPAGE);
 	}
     
     if (preferences.get("testbed_picture")==null) {
     	preferences.put("testbed_picture", DEFAULT_TESTBED_PICTURE);
 	}
     
     if (preferences.get("endorsed_tool_names")==null) {
     	preferences.put("endorsed_tool_names", DEFAULT_ENDORSED_TOOL_NAMES);
 	}
     
     if (preferences.get("endorsed_tool_logos")==null) {
     	preferences.put("endorsed_tool_logos", DEFAULT_ENDORSED_TOOL_LOGOS);
 	}
     
     if (preferences.get("endorsed_tool_homepages")==null) {
     	preferences.put("endorsed_tool_homepages", DEFAULT_ENDORSED_TOOL_HOMEPAGES);
 	}
     
     if (preferences.get("endorsed_tool_versions")==null) {
     	preferences.put("endorsed_tool_versions", DEFAULT_ENDORSED_TOOL_VERSIONS);
 	}
     
     
    configurator = new Configurator();
   };
   
   
   public static InterfaceConfiguration getInstance(){
     if(interfaceConfig == null)
         interfaceConfig = new InterfaceConfiguration();
     
     return interfaceConfig;
   }
   
   
   public String getCommitVersion(){
     return configurator.getCommitVersion();
   }
   
   
   public String getAM_URN(){
     return preferences.get("am_urn");
   }
   
   public String getHostname(){
     return preferences.get("hostname");
   }
   
   public String getAM_URL(){
     return preferences.get("am_url");
   }
   
   public String getDomain(){
     return preferences.get("domain");
   }
   
   public String getURN_Prefix(){
     return preferences.get("urn_prefix");
   }
   
   public String getSA_URN(){
     return preferences.get("sa_urn");
   }
   
   public String getTestbed_description(){
 	  return preferences.get("testbed_description");
   }
   
   public String getTestbed_homepage(){
 	  return preferences.get("testbed_homepage");
   }
   
   public String getTestbed_picture(){
 	  return preferences.get("testbed_picture");
   }
   
   public String getEndorsed_tool_names(){
 	  return preferences.get("endorsed_tool_names");
   }
   
   public String getEndorsed_tool_logos(){
 	  return preferences.get("endorsed_tool_logos");
   }
   
   public String getEndorsed_tool_homepages(){
 	  return preferences.get("endorsed_tool_homepages");
   }
   
   public String getEndorsed_tool_versions(){
 	  return preferences.get("endorsed_tool_versions");
   }
   
 }
