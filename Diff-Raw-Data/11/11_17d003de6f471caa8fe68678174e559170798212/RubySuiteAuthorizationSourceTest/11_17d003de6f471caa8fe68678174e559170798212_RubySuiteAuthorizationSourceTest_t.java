 package edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal;
 
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
 import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
 import org.jruby.embed.EmbedEvalUnit;
 import org.jruby.embed.LocalContextScope;
 import org.jruby.embed.PathType;
 import org.jruby.embed.ScriptingContainer;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 import static edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal.SuiteUserCollectionMatcher.suiteUsers;
 import static gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 @RunWith(JUnit4.class)
 public class RubySuiteAuthorizationSourceTest {
     private RubySuiteAuthorizationSource source;
 
     @Before
     public void before() throws Exception {
         ScriptingContainer scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
         // force use of embedded home even when JRUBY_HOME is set
         scriptingContainer.setHomeDirectory("classpath:/META-INF/jruby.home");
         EmbedEvalUnit parse = scriptingContainer.parse(PathType.CLASSPATH, "test_source.rb");
         if (parse == null) {
             throw new RuntimeException("Could not find the test source script");
         }
         parse.run();
 
         source = new RubySuiteAuthorizationSource(scriptingContainer);
     }
 
     ////// USER MAPPING
 
     public SuiteUser alice() {
         return source.getUser("alice", SuiteUserRoleLevel.ROLES_AND_SCOPES);
     }
 
     public SuiteUser betty() {
         return source.getUser("betty", SuiteUserRoleLevel.ROLES_AND_SCOPES);
     }
 
     @Test
     public void itMapsUsernameToUsername() throws Exception {
         assertThat(alice().getUsername(), is("alice"));
     }
 
     @Test
     public void itMapsFirstNameToFirstName() throws Exception {
         assertThat(alice().getFirstName(), is("Alice"));
     }
 
     @Test
     public void itMapsLastNameToLastName() throws Exception {
         assertThat(alice().getLastName(), is("Anderson"));
     }
 
     @Test
     public void itMapsIdToId() throws Exception {
         assertThat(alice().getId(), is(4));
     }
 
     @Test
    public void itMapsEmailAddressToEmailAddress() throws Exception {
        assertThat(alice().getEmailAddress(), is("alice@example.org"));
    }

    @Test
     public void itMapsAccountEndDateToAccountEndDate() throws Exception {
         Date actual = alice().getAccountEndDate();
         Calendar cal = Calendar.getInstance();
         cal.setTime(actual);
 
         assertThat(cal.get(Calendar.YEAR), is(2050));
         assertThat(cal.get(Calendar.MONTH), is(Calendar.MAY));
         assertThat(cal.get(Calendar.DAY_OF_MONTH), is(24));
     }
 
     @Test
     public void itCanMapAUserWithoutAnAccountEndDate() throws Exception {
         assertThat(betty().getAccountEndDate(), is(nullValue()));
     }
 
     @Test
     public void itMapsAnUnscopedRoleThatIsTrueToAMembership() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.SYSTEM_ADMINISTRATOR);
         assertThat(actual, is(not(nullValue())));
     }
 
     @Test
     public void itMapsASiteScopedRoleThatIsTrueToAnAllSitesMembership() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.USER_ADMINISTRATOR);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.isAllSites(), is(true));
     }
 
     @Test
     public void itMapsASiteScopedRoleWithSpecificSitesToAMembership() throws Exception {
         SuiteRoleMembership actual =
             alice().getRoleMemberships().get(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.getSiteIdentifiers(), is(Arrays.asList("IL036", "MN702")));
     }
 
     @Test
     public void itMapsASiteScopedRoleThatIsExplicitlyTrueToAnAllSitesMembership() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.STUDY_CREATOR);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.isAllSites(), is(true));
     }
 
     @Test
     public void itIgnoresStudyBitsOnASiteScopedRole() throws Exception {
         SuiteRoleMembership actual =
             alice().getRoleMemberships().get(SuiteRole.STUDY_QA_MANAGER);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.getSiteIdentifiers(), is(Arrays.asList("TN423")));
     }
 
     @Test
     public void itMapsASiteAndStudyScopedRoleThatIsTrueToAnAllEverythingMembership() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.DATA_READER);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.isAllSites(), is(true));
         assertThat(actual.isAllStudies(), is(true));
     }
 
     @Test
     public void itMapsASiteAndStudyScopedRoleWithAllSitesAndSomeStudiesAppropriately() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.DATA_ANALYST);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.isAllSites(), is(true));
         assertThat(actual.getStudyIdentifiers(), is(Arrays.asList("A", "B", "G")));
     }
 
     @Test
     public void itMapsASiteAndStudyScopedRoleWithSomeSitesAndAllStudiesAppropriately() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.REGISTRAR);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.getSiteIdentifiers(), is(Arrays.asList("CA504", "LA000")));
         assertThat(actual.isAllStudies(), is(true));
     }
 
     @Test
     public void itMapsASiteAndStudyScopedRoleWithSomeSitesAndSomeStudiesAppropriately() throws Exception {
         SuiteRoleMembership actual = alice().getRoleMemberships().get(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.getSiteIdentifiers(), is(Arrays.asList("KA333")));
         assertThat(actual.getStudyIdentifiers(), is(Arrays.asList("B", "L")));
     }
 
     ////// INTERFACE MAPPING
 
     @Test
     public void itReturnsAnExistingUserFromGetUser() throws Exception {
         SuiteUser actual = source.getUser("alice", SuiteUserRoleLevel.NONE);
         assertThat(actual, is(not(nullValue())));
     }
 
     @Test
     public void itReturnsNullForANonexistentUserFromGetUser() throws Exception {
         SuiteUser actual = source.getUser("alex", SuiteUserRoleLevel.NONE);
         assertThat(actual, is(nullValue()));
     }
 
     @Test
     public void itReturnsAnExistingUserFromGetUserById() throws Exception {
         SuiteUser actual = source.getUser(4, SuiteUserRoleLevel.ROLES);
         assertThat(actual, is(not(nullValue())));
         assertThat(actual.getUsername(), is("alice"));
     }
 
     @Test
     public void itReturnsNullFromGetUserByIdForANonexistentId() throws Exception {
         SuiteUser actual = source.getUser(7, SuiteUserRoleLevel.ROLES);
         assertThat(actual, is(nullValue()));
     }
 
     @Test
     public void itFindsAllMatchingUsersByRole() throws Exception {
         Collection<SuiteUser> actual = source.getUsersByRole(SuiteRole.USER_ADMINISTRATOR);
         assertThat(actual, is(suiteUsers("alice", "betty")));
     }
 
     @Test
     public void itReturnsAnEmptyListForNoMatchingUsersByRole() throws Exception {
         assertThat(source.getUsersByRole(SuiteRole.AE_REPORTER).size(), is(0));
     }
 
     @Test
     public void itReturnsAnEmptyListForNilMatchingUsers() throws Exception {
         assertThat(source.getUsersByRole(SuiteRole.LAB_IMPACT_CALENDAR_NOTIFIER).size(), is(0));
     }
 
     @Test
     public void itSearchesUsersByUsername() throws Exception {
         Collection<SuiteUser> actual = source.searchUsers(forUsernameSubstring("t"));
         assertThat(actual, is(suiteUsers("betty", "cat")));
     }
 
     @Test
     public void itSearchesUsersByFirstName() throws Exception {
         Collection<SuiteUser> actual = source.searchUsers(forFirstNameSubstring("th"));
         assertThat(actual, is(suiteUsers("cat")));
     }
 
     @Test
     public void itSearchesUsersByLastName() throws Exception {
         Collection<SuiteUser> actual = source.searchUsers(forLastNameSubstring("SON"));
         assertThat(actual, is(suiteUsers("alice", "betty")));
     }
 
     @Test
     public void itSearchesByMultipleCriteria() throws Exception {
         SuiteUserSearchOptions opts = new SuiteUserSearchOptions();
         opts.setUsernameSubstring("e");
         opts.setLastNameSubstring("chen");
         Collection<SuiteUser> actual = source.searchUsers(opts);
         assertThat(actual, is(suiteUsers("alice", "betty", "cat")));
     }
 
     @Test
     public void itCanSearchForEveryone() throws Exception {
         Collection<SuiteUser> actual = source.searchUsers(allUsers());
         assertThat(actual, is(suiteUsers("alice", "betty", "cat")));
     }
 
     @Test
     public void itReturnsNothingWhenSourceSearchReturnsNil() throws Exception {
         Collection<SuiteUser> actual = source.searchUsers(forUsernameSubstring("__NIL__"));
         assertThat(actual.size(), is(0));
     }
 }
