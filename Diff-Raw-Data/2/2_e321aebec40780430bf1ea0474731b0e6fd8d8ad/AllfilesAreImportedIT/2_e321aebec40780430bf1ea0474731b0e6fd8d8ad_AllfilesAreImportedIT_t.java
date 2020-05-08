 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.sdm4.vitamin.integrationtest;
 
 import com.mysql.jdbc.Driver;
 import dk.nsi.sdm4.testutils.StatuspageChecker;
 import dk.nsi.sdm4.testutils.TestDbConfiguration;
 import dk.nsi.sdm4.vitamin.config.VitaminimporterApplicationConfig;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.context.annotation.Primary;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.SimpleDriverDataSource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.sql.DataSource;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Udfører en import på integrationstest vagrant-vm'en og tjekker derefter databasen
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 @ContextConfiguration(classes = {AllfilesAreImportedIT.TestConfiguration.class})
 public class AllfilesAreImportedIT {
 	private static final int MAX_RETRIES=10;
 
 	@Configuration
 	@Import({VitaminimporterApplicationConfig.class, TestDbConfiguration.class})
 	static class TestConfiguration {
 		@Value("${test.mysql.port}")
 		private int mysqlPort;
 		private String db_username = "root";
 		private String db_password = "papkasse";
 
 		@Bean
 		@Primary
 		public DataSource dataSourceTalkingToRealDatabase() throws Exception {
 			String jdbcUrlPrefix = "jdbc:mysql://127.0.0.1:" + mysqlPort + "/";
 			return new SimpleDriverDataSource(new Driver(), jdbcUrlPrefix + "sdm_warehouse", db_username, db_password);
 		}
 	}
 
 	@Autowired
 	JdbcTemplate jdbcTemplate;
 
 	@Test
 	public void afterImportDatabaseContainsTheExpectedNumberOfEntities() throws Exception {
 		StatuspageChecker checker = new StatuspageChecker("vitaminimporter");
 
 		StatuspageChecker.StatuspageResult lastResult = null;
 		for (int i = 0; i < MAX_RETRIES; i++) {
 			lastResult = checker.fetchStatusPage();
 
 			assertEquals(200, lastResult.status);
 
 			if (lastResult.responseBody.contains("SUCCESS")) {
 				assertDbContainsExpectedRows();
 				return; // alt er godt
 			}
 
 			Thread.sleep(5*1000);
 		}
 
 		fail("Status page did not contain SUCCESS after " + MAX_RETRIES + (lastResult != null ? ", text was " + lastResult.responseBody : ""));
 	}
 
 	private void assertDbContainsExpectedRows() {
 		assertEquals("VitaminGrunddata", 194, jdbcTemplate.queryForInt("SELECT COUNT(*) from VitaminGrunddata"));
		assertEquals("VitaminFirmadata", 71, jdbcTemplate.queryForInt("SELECT COUNT(*) from VitaminFirmadata"));
 		assertEquals("VitaminUdgaaedeNavne", 141, jdbcTemplate.queryForInt("SELECT COUNT(*) from VitaminUdgaaedeNavne"));
 		assertEquals("VitaminIndholdsstoffer", 228, jdbcTemplate.queryForInt("SELECT COUNT(*) from VitaminIndholdsstoffer"));
 
 
 	}
 }
