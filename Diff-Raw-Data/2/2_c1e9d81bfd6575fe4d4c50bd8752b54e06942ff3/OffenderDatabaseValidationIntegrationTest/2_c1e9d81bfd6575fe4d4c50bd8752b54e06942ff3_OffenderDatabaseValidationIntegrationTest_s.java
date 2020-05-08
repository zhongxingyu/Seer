 package com.bfds.ach.calc.offenderValidation.batch;
 
 
 import com.ach.batch.test.AbstractSingleJobExecutionIntegrationTest;
 import com.bfds.ach.calc.Constants;
 import com.bfds.ach.calc.util.DatabaseUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import javax.sql.DataSource;
 import java.sql.SQLException;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({"classpath:META-INF/spring/ach-batch-test.xml", "classpath:META-INF/spring/calc-batch-validation-offender.xml"})
 @TestExecutionListeners({com.ach.test.AchTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
 public class OffenderDatabaseValidationIntegrationTest extends AbstractSingleJobExecutionIntegrationTest {
 
     @Autowired
     private Job offenderValidationJob;
 
     @Autowired
     private DatabaseUtils databaseUtils;
 
     @Autowired
     private DataSource dataSource;
 
     @Autowired
     private DataSource offenderDataSource;
 
     private JdbcTemplate jdbcTemplate;
 
 
     private static final String VALIDATION_FAIL_STATUS = "Fail";
 
     private static final String VALIDATION_PASS_STATUS = "Pass";
 
     private static final String PROCESS_STATUS_REVIEW = "Validation Review";
 
     private static final String OFFENDER_CHECK_STATUS ="OFFENDER CHECK PASS";
 
 
     public void beforeAllTests() {
         databaseUtils.createTestDate(OffenderDatabaseValidationIntegrationTest.class);
        databaseUtils.executeScriptFile("com/bfds.ach/calc/offenderValidation/batch/offender-sqlserver.sql", offenderDataSource);
     }
 
     public void afterAllTests() {
        databaseUtils.deleteTestDate(OffenderDatabaseValidationIntegrationTest.class);
        databaseUtils.executeScriptFile("com/bfds/ach/calc/offenderValidation/batch/offender-delete-sqlserver.sql", offenderDataSource);
     }
 
     @Override
     public void before() throws Exception {
         super.before();
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     protected Job geJOb() {
         return offenderValidationJob;
     }
 
     @Override
     protected JobParameters getJobParameters() {
         return new JobParametersBuilder()
                 .addString(Constants.PROCESS_STATUS, "OFFENDER CHECK")
                 .addString(Constants.SUCCESS_PROCESS_STATUS, "OFFENDER CHECK PASS")
                 .addString(Constants.BATCH_RUN_ID, System.currentTimeMillis()+"")
                 .toJobParameters();
     }
 
     /**
      * All claimants that have match in OffenderDatabase  should have status as Fail
      *
      */
 
     @Test
     public void verifyClaimantsInOffenderDB()throws SQLException{
         List<Long> claimIds = jdbcTemplate.queryForList("select claimant_claim_id from claimant_validation where status = '"+VALIDATION_FAIL_STATUS+"'", Long.class);
         assertThat(claimIds).containsOnly(1L, 2L, 3L, 4L, 5L, 9L,10L);
     }
 
     /**
      * All claimants that does not have  match in OffenderDatabase  should have status as Pass
      *
      */
     @Test
     public void verifyClaimantsNotInOffenderDB()throws SQLException{
         List<Long> claimIds = jdbcTemplate.queryForList("select claimant_claim_id from claimant_validation where status = '"+VALIDATION_PASS_STATUS+"'", Long.class);
         assertThat(claimIds).containsOnly(6L,7L,8L);
     }
 
     /**
      *      All invalid claims must have Validation_Review  for process_status
      *
      */
     @Test
     public void verifyClaimProcessStatusForFail() throws SQLException{
         List<Long> claimIds = jdbcTemplate.queryForList("select id from claimant_claim cc where cc.process_status = '"+PROCESS_STATUS_REVIEW+"'", Long.class);
         assertThat(claimIds).containsOnly(1L, 2L, 3L, 4L, 5L, 9L,10L);
 
     }
 
     /**
      *      All invalid claims must have OFFENDER CHECK PASS  for process_status
      *
      */
     @Test
     public void verifyClaimProcessStatusForPass() throws SQLException{
         List<Long> claimIds = jdbcTemplate.queryForList("select id from claimant_claim cc where cc.process_status = '"+OFFENDER_CHECK_STATUS+"'", Long.class);
         assertThat(claimIds).containsOnly(6L,7L,8L);
 
     }
 }
