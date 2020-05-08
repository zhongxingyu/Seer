 package models;
 
 import javax.persistence.PersistenceException;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.Fixtures;
 import play.test.UnitTest;
 
 public final class CompetencyTest extends UnitTest {
 
     private Level level1;
     private Level level2;
     private Level level3;

     private CompetencyGroup group;
 
     @Before
     public void setUp() {
         Fixtures.deleteAllModels();
 
         level1 = new Level("Level I", "Level I description");
         level1.save();
 
         level2 = new Level("Level II", "Level II description");
         level2.save();
 
         level3 = new Level("Level III", "Level III description");
         level3.save();
 
         group = new CompetencyGroup("Group 1", "Group 1 description", "Group 1 resources");
         group.save();
     }
 
     @Test
     public void testSaveAndFindByTitle() {
         Competency competency = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency.save();
 
         Competency found = Competency.findByTitle("Competency 1");
 
         Assert.assertNotNull(found);
         Assert.assertEquals("Competency 1", found.title);
         Assert.assertEquals("competency-1", found.sanitizedTitle);
         Assert.assertEquals("Competency 1 description", found.description);
         Assert.assertEquals("Competency 1 resources", found.resources);
         Assert.assertNotNull(found.level);
         Assert.assertEquals("Level I", found.level.title);
         Assert.assertNotNull(found.competencyGroup);
         Assert.assertEquals("Group 1", found.competencyGroup.title);
     }
 
     @Test (expected = PersistenceException.class)
     public void testCreateUnsuccessfullWithoutRequiredTitle() {
         Competency competency = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency.title = null;
         competency.save();
     }
 
     @Test (expected = PersistenceException.class)
     public void testCreateUnsuccessfullWithoutRequiredSanitizedTitle() {
         Competency competency = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency.sanitizedTitle = null;
         competency.save();
     }
 
     @Test (expected = PersistenceException.class)
     public void testCreateUnsuccessfullWithoutRequiredPlacement() {
         Competency competency = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency.placement = null;
         competency.save();
     }
 
     @Test (expected = PersistenceException.class)
     public void testShouldNotAllowItselfAsPreRequisite() {
         Competency competency = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency.sanitizedTitle = null;
         competency.save();
 
         competency.prerequisites.add(competency);
         competency.save();
     }
 
     @Test (expected = PersistenceException.class)
     public void testShouldNotAllowUnsavedPreRequisite() {
         Competency competency1 = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
 
         Competency competency2 = new Competency("Competency 2", "Competency 2 description", group, level2, "Competency 2 resources");
         competency2.prerequisites.add(competency1);
         competency2.save();
     }
 
     @Test
     public void testCompetencyRelationships() {
         Competency competency1 = new Competency("Competency 1", "Competency 1 description", group, level1, "Competency 1 resources");
         competency1.save();
 
         Competency competency2 = new Competency("Competency 2", "Competency 2 description", group, level2, "Competency 2 resources");
         competency2.prerequisites.add(competency1);
         competency2.save();
 
         Competency competency3 = new Competency("Competency 3", "Competency 3 description", group, level3, "Competency 3 resources");
         competency3.prerequisites.add(competency2);
         competency3.save();
 
         Competency found = Competency.findByTitle("Competency 2");
         Assert.assertNotNull(found);
         Assert.assertEquals("Competency 2",found.title);
         Assert.assertNotNull(competency2.prerequisites);
         Assert.assertEquals(1, competency2.prerequisites.size());
         Assert.assertEquals("Competency 1", competency2.prerequisites.iterator().next().title);
 
         found = Competency.findByTitle("Competency 3");
         Assert.assertNotNull(found);
         Assert.assertEquals("Competency 3",found.title);
         Assert.assertNotNull(competency3.prerequisites);
         Assert.assertEquals(1, competency3.prerequisites.size());
         Assert.assertEquals("Competency 2", competency3.prerequisites.iterator().next().title);
     }
 }
