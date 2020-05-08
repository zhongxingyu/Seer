 /*
  * $Id$
  * Copyright (c) 2004
  * Jukka Lindstrm
  */
 package com.laughingpanda.jira;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.ofbiz.core.entity.GenericValue;
 import org.ofbiz.core.entity.model.ModelEntity;
 
 import com.atlassian.configurable.ObjectConfigurationException;
 import com.atlassian.jira.config.properties.ApplicationProperties;
 import com.atlassian.jira.portal.PortletConfiguration;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.project.version.VersionManager;
 import com.atlassian.jira.security.PermissionManager;
 import com.laughingpanda.mocked.MockFactory;
 import com.laughingpanda.mocked.NullValues;
 import com.opensymphony.user.ProviderAccessor;
 import com.opensymphony.user.User;
 import com.opensymphony.user.Entity.Accessor;
 import com.opensymphony.user.provider.CredentialsProvider;
 
 public class ChartPortletTest extends TestCase {
 
     ChartPortlet portlet;
     private MockConfiguration config;
 
     private MockProviderAccessor accessor;
     private MockJiraAuthenticationContext authenticationContext;
     private User authenticatedUser;
 
     static abstract class MockVersion implements Version {
 
         public GenericValue project;
 
         public MockVersion() {
         }
 
         public Long getId() {
             return new Long(1);
         }
 
         public String getName() {
             return "TestVersion";
         }
 
         public Date getReleaseDate() {
             return new Date(0);
         }
 
         public boolean isArchived() {
             return false;
         }
 
         public boolean isReleased() {
             return false;
         }
 
         public GenericValue getProject() {
             return project;
         }
     }
 
     static abstract class MockProviderAccessor implements ProviderAccessor, CredentialsProvider, PermissionManager {
 
         public List<GenericValue> accessibleProjects = new LinkedList<GenericValue>();
 
         public CredentialsProvider getCredentialsProvider(String arg0) {
             return this;
         }
 
         public boolean load(String arg0, Accessor arg1) {
             return true;
         }
 
         public Collection getProjects(int permissionType, User user) {
             return accessibleProjects;
         }
 
         public boolean hasPermission(int permission, User user) {
             return false;
         }
 
     }
 
     static abstract class MockVersionManager implements VersionWorkloadHistoryManager, VersionManager {
 
         public Map<Long, Version> versions = new HashMap<Long, Version>();
 
         public MockVersionManager() {
         }
 
         public Version getVersion(Long id) {
             return versions.get(id);
         }
 
         public List<VersionWorkloadHistoryPoint> getWorkloadStartingFromMaxDateBeforeGivenDate(Long versionId, Date startDate) {
             return new LinkedList<VersionWorkloadHistoryPoint>();
         }
     }
 
     static abstract class MockConfiguration implements PortletConfiguration {
 
         private Map<String, String> properties = new HashMap<String, String>();
 
         public MockConfiguration() {
         }
 
         public boolean hasProperty(String property) throws ObjectConfigurationException {
             return properties.containsKey(property);
         }
 
         public Long getLongProperty(String property) throws ObjectConfigurationException {
             if (properties.containsKey(property)) return Long.parseLong(properties.get(property));
            return null;
         }
 
         public String getProperty(String property) throws ObjectConfigurationException {
             if (properties.containsKey(property)) return properties.get(property);
            return null;
         }
     }
 
     public void setUp() throws Exception {
         NullValues.useDefaultPrimitiveValues();
 
         authenticationContext = MockFactory.makeMock(MockJiraAuthenticationContext.class);
         accessor = MockFactory.makeMock(MockProviderAccessor.class);
         authenticatedUser = new User("name", accessor);
         authenticationContext.setUser(authenticatedUser);
         config = MockFactory.makeMock(MockConfiguration.class);
         MockVersionManager mock = MockFactory.makeMock(MockVersionManager.class);
         portlet = new ChartPortlet(authenticationContext, mock, mock, accessor, NullValues.makeMock(ApplicationProperties.class));
 
         config.properties.put("chart.width", "640");
         config.properties.put("chart.height", "400");
         config.properties.put("versionId", "1");
         config.properties.put("startDate", "2005-01-01");
 
         GenericValue project = new GenericValue(MockFactory.makeMock(ModelEntity.class));
         accessor.accessibleProjects.add(project);
 
         MockVersion version = MockFactory.makeMock(MockVersion.class);
         version.project = project;
 
         mock.versions.put(1l, version);
     }
 
     public void testNullConfiguration() {
         try {
             portlet.getVelocityParams(null);
             fail("Expected exception with null configuration.");
         } catch (IllegalArgumentException expected) {
         }
     }
 
     public void testBasic() {
         Map params = portlet.getVelocityParams(config);
         assertEquals("public1-2005-01-01-640x400.png", params.get("chartFilename"));
         assertEquals("public1-2005-01-01-640x400.png", params.get("imageMapName"));
         assertEquals("1", params.get("versionId"));
         assertFalse(params.containsKey("errorMessage"));
         assertEquals(true, params.get("loggedin"));
     }
 
     public void testUserHasNoRights() {
         accessor.accessibleProjects.clear();
         Map params = portlet.getVelocityParams(config);
         assertFalse(params.containsKey("chartFilename"));
         assertEquals("You don't have correct privileges to view this data.", params.get("errorMessage"));
     }
 
     public void testNoStartDateConfigured() {
         config.properties.remove("startDate");
         Map params = portlet.getVelocityParams(config);
         assertEquals("public1-1970-01-01-640x400.png", params.get("chartFilename"));
         assertEquals("public1-1970-01-01-640x400.png", params.get("imageMapName"));
         assertFalse(params.containsKey("errorMessage"));
     }
 
 }
