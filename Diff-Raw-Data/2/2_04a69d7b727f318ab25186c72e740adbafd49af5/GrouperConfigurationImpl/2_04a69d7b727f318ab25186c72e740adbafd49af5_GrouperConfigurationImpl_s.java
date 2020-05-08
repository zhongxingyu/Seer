 /*
 
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.grouper;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.commons.osgi.OsgiUtil;
 import org.osgi.service.cm.ConfigurationException;
 import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 
 @Service
 @Component(metatype = true)
 /**
  * @inheritDoc
  */
 public class GrouperConfigurationImpl implements GrouperConfiguration {
 
 	private static final Logger log = LoggerFactory.getLogger(GrouperConfigurationImpl.class);
 
 	// Configurable via the ConfigAdmin services.
 	private static final String DEFAULT_URL = "http://localhost:9090/grouper-ws/servicesRest";
 	@Property(value = DEFAULT_URL)
 	public static final String PROP_URL = "grouper.url";
 
 	private static final String DEFAULT_WS_VERSION = "1_7_000";
 	@Property(value = DEFAULT_WS_VERSION)
 	public static final String PROP_WS_VERSION = "grouper.ws_version";
 
 	private static final String DEFAULT_USERNAME = "GrouperSystem";
 	@Property(value = DEFAULT_USERNAME)
 	public static final String PROP_USERNAME = "grouper.username";
 
 	private static final String DEFAULT_PASSWORD = "abc123";
 	@Property(value = DEFAULT_PASSWORD)
 	public static final String PROP_PASSWORD = "grouper.password";
 
 	// HTTP Timeout in milliseconds
 	private static final String DEFAULT_TIMEOUT = "5000";
 	@Property(value = DEFAULT_TIMEOUT)
 	public static final String PROP_TIMEOUT = "grouper.httpTimeout";
 
 	private static final String DEFAULT_IGNORED_USER = "grouper-admin";
 	@Property(value = DEFAULT_IGNORED_USER)
 	public static final String PROP_IGNORED_USER = "grouper.ignoredUser";
 
 	private static final String[] DEFAULT_IGNORED_GROUP_PATTERN = {"administrators"};
 	@Property(value = { "administrators" }, cardinality = 9999)
 	public static final String PROP_IGNORED_GROUP_PATTERN = "grouper.ignoredGroupsPatterns";
 
 	// TODO: A better way to generate the default list.
 	private static final String[] DEFAULT_PSEUDO_GROUP_SUFFIXES =
 		{"-manager", "-ta", "-lecturer", "-student", "-member"};
 	@Property(value = {"-manager", "-ta", "-lecturer", "-student", "-member"}, cardinality = 9999)
 	public static final String PROP_PSEUDO_GROUP_SUFFIXES = "grouper.psuedoGroup.suffixes";
 
 	private static final String DEFAULT_CONTACTS_STEM = "edu:apps:sakaioae:users";
 	@Property(value = DEFAULT_CONTACTS_STEM)
 	public static final String PROP_CONTACTS_STEM = "grouper.nameprovider.contacts.stem";
 
 	private static final String DEFAULT_SIMPLEGROUPS_STEM = "edu:apps:sakaioae:groups:adhoc";
 	@Property(value = DEFAULT_SIMPLEGROUPS_STEM)
 	public static final String PROP_SIMPLEGROUPS_STEM = "grouper.nameprovider.simplegroups.stem";
 
 	private static final String DEFAULT_COURSES_STEM = "edu:apps:sakaioae:courses:adhoc";
	@Property(value = DEFAULT_SIMPLEGROUPS_STEM)
 	public static final String PROP_COURSES_STEM = "grouper.nameprovider.courses.stem";
 
 	private static final String[] DEFAULT_GROUPER_GROUP_TYPES = {"addIncludeExclude"};
 	@Property(value = {"addIncludeExclude"}, cardinality = 9999)
 	public static final String PROP_GROUPER_GROUP_TYPES = "grouper.groupTypes";
 
 	private static final String[] DEFAULT_EXTENSION_OVERRIDES = new String[0];
 	@Property(value = {}, cardinality = 9999)
 	public static final String PROP_EXTENSION_OVERRIDES = "grouper.extension.overrides";
 
 	private static final boolean DEFAULT_DELETES_ENABLED = true;
 	@Property(boolValue = DEFAULT_DELETES_ENABLED)
 	public static final String PROP_DELETES_ENABLED = "grouper.enable.deletes";
 
 	// Grouper configuration.
 	private URL url;
 	private String username;
 	private String password;
 	private String contactsStem;
 
 	private String simpleGroupsStem;
 	private String coursesStem;
 
 	// GrouperWS
 	private String wsVersion;
 	private int httpTimeout;
 
 	// Ignore events caused by this user
 	private String ignoredUser;
 	// Ignore groups that match these regexs
 	private String[] ignoredGroupPatterns;
 
 	// Suffixes that indicate these are sakai internal groups
 	private String[] pseudoGroupSuffixes;
 
 	// Grouper group types for newly created groups.
 	private Set<String> groupTypes;
 
 	private Map<String, String> extensionOverrides;
 
 	private boolean deletesEnabled;
 
 
 	// -------------------------- Configuration Admin --------------------------
 	/**
 	 * Copy in the configuration from the config admin service.
 	 *
 	 * Called by the Configuration Admin service when a new configuration is
 	 * detected in the web console or a config file.
 	 *
 	 * @see org.osgi.service.cm.ManagedService#updated
 	 */
 	@Activate
 	@Modified
 	public void updated(Map<?, ?> props) throws ConfigurationException {
 		try {
 			url = new URL(OsgiUtil.toString(props.get(PROP_URL), DEFAULT_URL));
 		} catch (MalformedURLException mfe) {
 			throw new ConfigurationException(PROP_URL, mfe.getMessage(), mfe);
 		}
 		username  = OsgiUtil.toString(props.get(PROP_USERNAME), DEFAULT_USERNAME);
 		password  = OsgiUtil.toString(props.get(PROP_PASSWORD), DEFAULT_PASSWORD);
 
 		contactsStem = cleanStem(OsgiUtil.toString(props.get(PROP_CONTACTS_STEM),DEFAULT_CONTACTS_STEM));
 		simpleGroupsStem = cleanStem(OsgiUtil.toString(props.get(PROP_SIMPLEGROUPS_STEM),DEFAULT_SIMPLEGROUPS_STEM));
 		coursesStem = cleanStem(OsgiUtil.toString(props.get(PROP_COURSES_STEM),DEFAULT_COURSES_STEM));
 
 		wsVersion = OsgiUtil.toString(props.get(PROP_WS_VERSION), DEFAULT_WS_VERSION);
 		httpTimeout = OsgiUtil.toInteger(props.get(PROP_TIMEOUT), Integer.parseInt(DEFAULT_TIMEOUT));
 
 		ignoredUser = OsgiUtil.toString(props.get(PROP_IGNORED_USER),DEFAULT_IGNORED_USER);
 		ignoredGroupPatterns = OsgiUtil.toStringArray(props.get(PROP_IGNORED_GROUP_PATTERN), DEFAULT_IGNORED_GROUP_PATTERN);
 		pseudoGroupSuffixes = OsgiUtil.toStringArray(props.get(PROP_PSEUDO_GROUP_SUFFIXES), DEFAULT_PSEUDO_GROUP_SUFFIXES);
 
 		deletesEnabled = OsgiUtil.toBoolean(props.get(PROP_DELETES_ENABLED), DEFAULT_DELETES_ENABLED);
 
 		groupTypes = new HashSet<String>();
 		for (String gt: OsgiUtil.toStringArray(props.get(PROP_GROUPER_GROUP_TYPES),
 				DEFAULT_GROUPER_GROUP_TYPES)){
 			groupTypes.add(gt);
 		}
 
 		Builder<String, String> extentionOverridesBuilder = ImmutableMap.builder();
 		for (String exO: OsgiUtil.toStringArray(props.get(PROP_EXTENSION_OVERRIDES), DEFAULT_EXTENSION_OVERRIDES)){
 			String[] split = exO.split(":");
 			if (split.length == 2){
 				extentionOverridesBuilder.put(split[0], split[1]);
 			}
 		}
 		extensionOverrides = extentionOverridesBuilder.build();
 
 		log.debug("Configured!");
 	}
 
 	private String cleanStem(String stem){
 		if (stem != null && stem.endsWith(":")){
 			stem = stem.substring(0, stem.length() - 1);
 		}
 		return stem;
 	}
 
 	public URL getUrl() {
 		return url;
 	}
 
 	public String getWsVersion() {
 		return wsVersion;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getRestWsUrlString() {
 		return url + "/" + wsVersion;
 	}
 
 	public int getHttpTimeout() {
 		return httpTimeout;
 	}
 
 	public String getIgnoredUserId() {
 		return ignoredUser;
 	}
 
 	public String[] getIgnoredGroups() {
 		return ignoredGroupPatterns;
 	}
 
 	public String[] getPseudoGroupSuffixes(){
 		return pseudoGroupSuffixes;
 	}
 
 	public String getContactsStem(){
 		return contactsStem;
 	}
 
 	public String getSimpleGroupsStem() {
 		return simpleGroupsStem;
 	}
 
 	public String getCoursesStem() {
 		return coursesStem;
 	}
 
 	public Set<String> getGroupTypes() {
 		return groupTypes;
 	}
 
 	public Map<String, String> getExtensionOverrides() {
 		return extensionOverrides;
 	}
 
 	public boolean getDeletesEnabled(){
 		return deletesEnabled;
 	}
 }
