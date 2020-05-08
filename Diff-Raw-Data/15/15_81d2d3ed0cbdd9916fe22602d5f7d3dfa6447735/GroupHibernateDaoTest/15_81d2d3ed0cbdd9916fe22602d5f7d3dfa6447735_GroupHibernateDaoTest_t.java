 package org.jtalks.poulpe.model.dao.hibernate;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.model.fixtures.TestFixtures;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 import static org.testng.Assert.*;
 import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
 
 /**
  * @author Leonid Kazancev
  */
 @ContextConfiguration(locations = {"classpath:/org/jtalks/poulpe/model/entity/applicationContext-dao.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
 @Transactional
 public class GroupHibernateDaoTest extends AbstractTransactionalTestNGSpringContextTests {
     static final String NO_FILTER = "";
 
     @Autowired
     private GroupDao dao;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private Session session;
 
     @BeforeMethod
     public void setUp() throws Exception {
         session = sessionFactory.getCurrentSession();
     }
 
     @Test
     public void testSave() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
         Group savedGroup = (Group) session.get(Group.class, group.getId());
 
         assertReflectionEquals(group, savedGroup);
     }
 
     @Test
     public void testSaveIdGeneration() {
         Group group = TestFixtures.group();
         long initialId = 0;
         group.setId(initialId);
 
         saveAndEvict(group);
 
         assertNotSame(group.getId(), initialId, "ID is not created");
     }
 
     @Test
     public void testGetById() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         Group actual = dao.get(group.getId());
         assertReflectionEquals(actual, group);
     }
 
 
     @Test
     public void testGetAll() {
         Group group0 = TestFixtures.group();
         saveAndEvict(group0);
 
         Group group1 = TestFixtures.group();
         saveAndEvict(group1);
 
         List<Group> actual = dao.getAll();
        assertEquals(actual.size(), 2);
         assertReflectionEquals(actual.get(0), group0);
         assertReflectionEquals(actual.get(1), group1);
     }
 
     @Test
     public void testGetByNameContains() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         List<Group> actual = dao.getByNameContains(group.getName());
        assertEquals(actual.size(), 1);
         assertReflectionEquals(actual.get(0), group);
     }
 
     @Test
     public void testGetByNameContainsWithEmptyName() {
         Group group = TestFixtures.group();
         saveAndEvict(group);
 
         group = TestFixtures.group();
         saveAndEvict(group);
 
         List<Group> actual = dao.getByNameContains(NO_FILTER);
         List<Group> all = dao.getAll();
         assertEquals(actual, all);
     }
 
     @Test
     public void testGetByName() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         List<Group> actual = dao.getByName(group.getName());
         assertEquals(actual.size(), 1);
         assertReflectionEquals(actual.get(0), group);
     }
 
     @Test
     public void testGetByNameFailWithEmptyString() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         List<Group> actual = dao.getByName(NO_FILTER);
         assertEquals(actual.size(), 0);
     }
 
     @Test
     public void testGetByNameLowerCase() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         List<Group> actual = dao.getByName(group.getName().toLowerCase());
         assertEquals(actual.size(), 1);
         assertReflectionEquals(actual.get(0), group);
     }
 
     @Test
     public void testGetByNameUpperCase() {
         Group group = TestFixtures.group();
 
         saveAndEvict(group);
 
         List<Group> actual = dao.getByName(group.getName().toUpperCase());
         assertEquals(actual.size(), 1);
         assertReflectionEquals(actual.get(0), group);
     }
 
 
     @Test
     public void getGetUsersCount() {
         int count = 5;
         Group group = TestFixtures.groupWithUsers();
         saveAndEvict(group);
 
         int actual = dao.get(group.getId()).getUsers().size();
         assertEquals(actual, count);
     }
 
     @Test
     public void testGetModeratingBranches() {
         PoulpeBranch branch = TestFixtures.branch();
         Group group = new Group("name", "description");
         branch.setModeratorsGroup(group);
         saveAndEvict(branch);
 
         List<PoulpeBranch> actual = dao.getModeratingBranches(group);
         assertEquals(actual.get(0), branch);
     }
 
     @Test
     public void testGetModeratingBranchesNotModeratorGroup() {
         Group group = TestFixtures.group();
         saveAndEvict(group);
 
         List<PoulpeBranch> actual = dao.getModeratingBranches(group);
         assertEquals(actual.size(), 0);
     }
     
     @Test
     public void testDeleteGroup(){
         Group group = TestFixtures.group();
         saveAndEvict(group);
 
         dao.delete(group);
         Group actual = dao.get(group.getId());
         assertNull(actual);
     }
 
 
     private void saveAndEvict(PoulpeBranch branch) {
         saveAndEvict(branch.getModeratorsGroup());
         PoulpeSection section = TestFixtures.section();
         branch.setSection(section);
         session.save(section);
         session.save(branch);
         session.flush();
         session.clear();
     }
 
     private void saveAndEvict(PoulpeUser user) {
         session.save(user);
         session.flush();
         session.clear();
     }
 
     private void saveAndEvict(Group group) {
         saveAndEvict((Iterable<PoulpeUser>) (Object) group.getUsers());
         session.save(group);
         session.flush();
         session.clear();
     }
 
     private void saveAndEvict(Iterable<PoulpeUser> users) {
         for (PoulpeUser user : users) {
             saveAndEvict(user);
         }
     }
 }
