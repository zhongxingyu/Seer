 package com.github.kolorobot.icm.dashboard;
 
 import com.github.kolorobot.icm.config.TestDataSourceConfig;
 import com.github.kolorobot.icm.incident.Incident;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.MapAssert.entry;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = TestDataSourceConfig.class)
 public class IncidentCountsRepositoryTest {
 
     @Inject
     private JdbcTemplate jdbcTemplate;
 
     private IncidentCountsRepository incidentCountsRepository;
 
     @Before
     public void setUp() throws Exception {
         incidentCountsRepository = new IncidentCountsRepository(jdbcTemplate);
     }
 
     @Test
     public void incidentCounts() {
         IncidentCounts result = incidentCountsRepository.incidentCounts();
         assertThat(result.all()).as("Count all").isEqualTo(3);
         assertThat(result.createdToday()).as("Count by today").isEqualTo(0);
         assertThat(result.byStatus()).as("Count by status")
                .includes(entry(Incident.Status.NEW, 1))
                 .includes(entry(Incident.Status.CONFIRMED, 1))
                .includes(entry(Incident.Status.NOT_CONFIRMED, 1))
                 .includes(entry(Incident.Status.SOLVED, 0))
                 .includes(entry(Incident.Status.CLOSED, 0));
     }
 
     @Test
     public void auditCounts() {
         IncidentCounts result = incidentCountsRepository.auditCounts();
        assertThat(result.all()).as("Count all").isEqualTo(3);
         assertThat(result.createdToday()).as("Count by today").isEqualTo(0);
         assertThat(result.byStatus()).as("Count by status")
                 .includes(entry(Incident.Status.NEW, 0))
                .includes(entry(Incident.Status.CONFIRMED, 2))
                .includes(entry(Incident.Status.NOT_CONFIRMED, 1))
                 .includes(entry(Incident.Status.SOLVED, 0))
                 .includes(entry(Incident.Status.CLOSED, 0));
     }
 }
