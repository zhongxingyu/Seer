 package eu.ydp.ldapgroups.resources;
 
 import com.sun.jersey.api.NotFoundException;
 import com.yammer.dropwizard.testing.ResourceTest;
 import eu.ydp.ldapgroups.GroupPropertyMatcher;
 import eu.ydp.ldapgroups.dao.GroupDao;
 import eu.ydp.ldapgroups.entity.Group;
 import org.hamcrest.MatcherAssert;
 import org.hamcrest.Matchers;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 import javax.ws.rs.core.MediaType;
 import java.util.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class GroupResourceTest extends ResourceTest {
     Group group1;
     Group group2;
 
     @Inject
     GroupDao dao;
     @Inject
     GroupResource resource;
 
     static public final String API_BASE = "/groups";
 
     @Override
     protected void setUpResources() throws Exception {
         group1 = new Group.Builder().name("group1").dateModified(new Date(0)).members().build();
         group2 = new Group.Builder().name("group2").dateModified(new Date(0)).members("ala", "kot").build();
 
         addResource(resource);
     }
 
     @After
     public void tearDown() throws Exception {
         for(Group group : dao.findAll()) {
             dao.delete(group.getName());
         }
     }
 
     @Test
     public void shouldListGroupsReturnAllGroups() throws Exception {
         setEntities(group1, group2);
 
         Group[] groups = client().resource(API_BASE)
                 .get(Group[].class);
 
         System.out.println("Groups: "+groups);
         MatcherAssert.assertThat(Arrays.asList(groups), Matchers.containsInAnyOrder(
                 (List) groupMatchers(group1, group2)));
     }
 
     private List groupMatchers(Group... groups) {
         List<GroupPropertyMatcher> matchers = new ArrayList<GroupPropertyMatcher>(groups.length);
         for (Group group : groups) {
             matchers.add(new GroupPropertyMatcher(group));
         }
         return matchers;
     }
 
     @Test
     public void shouldCreateGroupRememberDataAndReturnUpdatedObject() throws Exception {
         Group group = client().resource(API_BASE)
                 .entity(group1, MediaType.APPLICATION_JSON_TYPE)
                 .post(Group.class);
 
         MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
         MatcherAssert.assertThat(group.getDateModified(), Matchers.notNullValue());
     }
 
     @Test
     public void shouldGetGroupReturnExitingObject() throws Exception {
         setEntities(group1);
 
         Group group = client().resource(API_BASE + "/" + group1.getName())
                 .get(Group.class);
 
         MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
         MatcherAssert.assertThat(group.getMembers(), Matchers.equalTo(group1.getMembers()));
     }
 
     @Test(expected = NotFoundException.class)
     public void shouldGetGroupThrowNotFoundExceptionOnNonExistingObject() throws Exception {
         resource.getGroup("non existing group");
     }
 
     @Test
     public void setGroupShouldUpdateMembersIfGroupExists() throws Exception {
         setEntities(group1.clone());
 
         Group modifiedGroup = new Group.Builder(group1).members("parasol").build();
         Group group = client().resource(API_BASE+"/"+group1.getName())
                 .entity(modifiedGroup, MediaType.APPLICATION_JSON_TYPE)
                 .put(Group.class);
 
         MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
         MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
         MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
     }
 
     @Test
     public void setGroupShouldCreateGroupIfGroupNotExists() throws Exception {
         Group modifiedGroup = new Group.Builder(group1).members("parasol").build();
         Group group = client().resource(API_BASE+"/"+group1.getName())
                 .entity(modifiedGroup, MediaType.APPLICATION_JSON_TYPE)
                 .put(Group.class);
 
         MatcherAssert.assertThat(group.getName(), Matchers.equalTo(group1.getName()));
         MatcherAssert.assertThat(group.getMembers(), Matchers.containsInAnyOrder("parasol"));
         MatcherAssert.assertThat(group.getDateModified(), Matchers.greaterThan(group1.getDateModified()));
     }
 
     @Test
     public void shouldDeleteRemoveExistingObject() throws Exception {
         setEntities(group1);
 
         client().resource(API_BASE+"/"+group1.getName())
                .entity(Arrays.asList("parasol"), MediaType.APPLICATION_JSON_TYPE)
                 .delete();
 
         Group[] groups = client().resource(API_BASE)
                 .get(Group[].class);
         MatcherAssert.assertThat(groups, Matchers.emptyArray());
     }
 
     @Test(expected = NotFoundException.class)
     public void shouldDeleteThrowNotFoundExceptionOnNonExistingObject() throws Exception {
         resource.deleteGroup(group1.getName());
     }
 
     public void setEntities(Group... entities) {
         for (Group entity : entities) {
             dao.create(entity);
         }
     }
 }
