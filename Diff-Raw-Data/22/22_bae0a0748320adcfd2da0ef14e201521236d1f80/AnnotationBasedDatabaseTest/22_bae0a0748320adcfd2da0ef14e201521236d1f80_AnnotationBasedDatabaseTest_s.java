 package com.seitenbau.testing.dbunit;
 
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.fest.assertions.Assertions;
import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.seitenbau.testing.dbunit.datasets.DefaultDataSet;
 import com.seitenbau.testing.dbunit.datasets.SampleDataSet;
 import com.seitenbau.testing.dbunit.model.JobsTable.RowBuilder_Jobs;
 import com.seitenbau.testing.dbunit.model.PersonDatabaseDataSet;
 import com.seitenbau.testing.dbunit.model.PersonsTable.RowBuilder_Persons;
 import com.seitenbau.testing.dbunit.model.TeamsTable.RowBuilder_Teams;
 import com.seitenbau.testing.dbunit.rule.DatabaseBefore;
 import com.seitenbau.testing.dbunit.rule.DatabasePrepare;
 import com.seitenbau.testing.dbunit.rule.DatabaseSetup;
 import com.seitenbau.testing.dbunit.rule.DatabaseTesterRule;
 import com.seitenbau.testing.personmanager.Person;
 import com.seitenbau.testing.personmanager.PersonManagerContext;
 import com.seitenbau.testing.personmanager.PersonService;
 import com.seitenbau.testing.util.Future;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes=PersonManagerContext.class)
 @DatabaseSetup(prepare = DefaultDataSet.class, assertNoModification = false)
 public class AnnotationBasedDatabaseTest
 {
 
   @Autowired
   PersonService sut;
 
   @Autowired
   DataSource dataSource;
 
   @Rule
   public DatabaseTesterRule dbTesterRule = new DatabaseTesterRule(new Future<DataSource>(){
     @Override
     public DataSource getFuture()
     {
       return dataSource;
     }
  });
 
   @Test
   public void allPersonsFromDefaultDataset() throws Exception
   {
     // prepare
     // execute
     List<Person> result = sut.findPersons();
     // verify
     Assertions.assertThat(result).hasSize(3);
   }
 
   @DatabaseBefore
   public void removeHansi(SampleDataSet sampleDataSet)
   {
     sampleDataSet.table_Persons.findWhere.firstName("Hansi").delete();
   }
 
   @Test
   @DatabaseSetup(prepare = SampleDataSet.class, assertNoModification = true)
   public void allPersonsFromSampleDatasetWithoutHansi() throws Exception
   {
     // prepare
     // execute
     List<Person> result = sut.findPersons();
     // verify
     Assertions.assertThat(result).hasSize(0);
   }
 
   @DatabaseBefore(id = "withRainer")
   public void addRainer(PersonDatabaseDataSet defaultDataSet)
   {
     RowBuilder_Jobs job = defaultDataSet.table_Jobs.insertRow().setTitle("Agile Tester").setDescription("Just agile.");
     RowBuilder_Teams team = defaultDataSet.table_Teams.insertRow().setTitle("Agile Experts").setDescription("Agile only.")
         .setMembersize(1);
     RowBuilder_Persons person = defaultDataSet.table_Persons.insertRow().setFirstName("Rainer").setName("Weinhold").setTeamId(team.getId());
     defaultDataSet.table_PersonJob.insertRow().setPersonId(person.getId()).setJobId(job.getId());
   }
 
  @Ignore("the sort order has to be set on the DatabaseTesteRule")
   @Test
   @DatabasePrepare("withRainer")
   @DatabaseSetup(prepare = DefaultDataSet.class, assertNoModification = true)
   public void getAllPersonsWithRainer()
   {
     // prepare
     // execute
     List<Person> result = sut.findPersons();
     // verify
     Assertions.assertThat(result).hasSize(4);
   }
 
 }
