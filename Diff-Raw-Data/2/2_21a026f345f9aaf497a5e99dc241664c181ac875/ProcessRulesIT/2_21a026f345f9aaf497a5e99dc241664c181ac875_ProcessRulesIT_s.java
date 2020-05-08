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
 package dk.nsi.haiba.lprimporter.integrationtest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 import org.springframework.transaction.annotation.Transactional;
 
 import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
 import dk.nsi.haiba.lprimporter.dao.LPRDAO;
 import dk.nsi.haiba.lprimporter.dao.impl.HAIBADAOImpl;
 import dk.nsi.haiba.lprimporter.dao.impl.LPRDAOImpl;
 import dk.nsi.haiba.lprimporter.model.haiba.Statistics;
 import dk.nsi.haiba.lprimporter.model.lpr.Administration;
 import dk.nsi.haiba.lprimporter.rules.LPRPrepareDataRule;
 import dk.nsi.haiba.lprimporter.rules.LPRRule;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional("haibaTransactionManager")
 @ContextConfiguration(loader = AnnotationConfigContextLoader.class)
 public class ProcessRulesIT {
 
     @Configuration
     @PropertySource("classpath:test.properties")
     @Import(LPRIntegrationTestConfiguration.class)
     static class ContextConfiguration {
         @Bean
         public HAIBADAO haibaDao() {
             return new HAIBADAOImpl();
         }
         @Bean
         public LPRDAO lprDao() {
             return new LPRDAOImpl();
         }
     }
 
     @Autowired
     JdbcTemplate jdbcTemplate;
     
     @Autowired
     @Qualifier("haibaJdbcTemplate")
     JdbcTemplate jdbc;
 
     @Autowired
 	HAIBADAO haibaDao;
 	
 	@Autowired
 	LPRDAO lprDao;
 
 	@Autowired
 	LPRPrepareDataRule lprPrepareDataRule;
 
 	String cpr;
 	long recordNummer0;
 	long recordNummer1;
 	long recordNummer2;
 	long recordNummer3;
 	long recordNummer4;
 	String sygehusCode0;
 	String afdelingsCode0;
 	String sygehusCode1;
 	String afdelingsCode1;
 	String sygehusCode2;
 	String afdelingsCode2;
 	String sygehusCode3;
 	String afdelingsCode3;
 	String sygehusCode4;
 	String afdelingsCode4;
 	DateTime in0;
 	DateTime out0;
 	DateTime in1;
 	DateTime out1;
 	DateTime in2;
 	DateTime out2;
 	DateTime in3;
 	DateTime out3;
 	DateTime in4;
 	DateTime out4;
 
 	String oprCode1;
 	String oprType1;
 	String extraOprCode1;
 	DateTime op1;
 
 	@Before
 	public void init() {
     	// Init Administration data
 		cpr = "1111111111";
     	recordNummer0 = 1233;
     	sygehusCode0 = "xxxx";
     	afdelingsCode0 = "yyy";
     	in0 = new DateTime(2019, 5, 3, 0, 0, 0);
     	out0 = new DateTime(2019, 5, 3, 0, 0, 0);
 		
 		recordNummer1 = 1234;
     	sygehusCode1 = "csgh";
     	afdelingsCode1 = "234";
     	in1 = new DateTime(2010, 5, 3, 0, 0, 0);
     	out1 = new DateTime(2010, 6, 4, 0, 0, 0);
 
     	recordNummer2 = 1235;
     	sygehusCode2 = "csgh";
     	afdelingsCode2 = "235";
     	in2 = new DateTime(2010, 5, 3, 0, 0, 0);
     	out2 = new DateTime(2010, 6, 4, 0, 0, 0);
 
     	recordNummer3 = 1236;
     	sygehusCode3 = "abcd";
     	afdelingsCode3 = "236";
     	in3 = new DateTime(2010, 8, 3, 0, 0, 0);
     	out3 = new DateTime(2010, 8, 10, 0, 0, 0);
 
     	recordNummer4 = 1237;
     	sygehusCode4 = "gggg";
     	afdelingsCode4 = "123";
     	in4 = new DateTime(2011, 8, 3, 0, 0, 0);
     	out4 = new DateTime(2011, 8, 10, 0, 0, 0);
 	}
 	
 	@After
 	public void cleanUp() {
     	// Spring junit doesn't support rollback for multiple transaction managers - roll back data manually
     	jdbcTemplate.execute("delete from T_ADM");
 	}
 	
 	@Test 
 	public void threeContactsWithSameInAndOutdateButDifferentDepartmentShouldResultInError() {
 		assertNotNull(lprPrepareDataRule);
 
     	sygehusCode3 = "csgh";
     	in3 = new DateTime(2010, 5, 3, 0, 0, 0);
     	out3 = new DateTime(2010, 6, 4, 0, 0, 0);
 		
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer3), cpr, sygehusCode3, afdelingsCode3, in3.toDate(), out3.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer4), cpr, sygehusCode4, afdelingsCode4, in4.toDate(), out4.toDate(), 0);
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 		
 		// Expect 2 errors logged
 		assertEquals(2, jdbc.queryForInt("select count(*) from RegelFejlbeskeder"));
 
 		// check updated status flag in T_ADM
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
 		assertEquals("FAILURE",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer3, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer4, String.class));
 		
 		assertEquals(2, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 	}
 	
 	@Test 
 	public void threeIdenticalContactsButDifferentInTimeShouldBeMergedToOneAdmission() {
 		assertNotNull(lprPrepareDataRule);
 		
 		sygehusCode0 = sygehusCode1;
 		sygehusCode2 = sygehusCode1;
 		afdelingsCode0 = afdelingsCode1;
 		afdelingsCode2 = afdelingsCode1;
     	in0 = new DateTime(2010, 5, 3, 9, 0, 0);
     	out0 = new DateTime(2010, 5, 3, 0, 0, 0);
     	in1 = new DateTime(2010, 5, 3, 10, 0, 0);
     	out1 = new DateTime(2010, 5, 3, 0, 0, 0);
     	in2 = new DateTime(2010, 5, 3, 11, 0, 0);
     	out2 = new DateTime(2010, 5, 3, 0, 0, 0);
 		
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
 
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
 
 		assertEquals(1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 	}
 	
 	/*
 	 * 3 overlapping contacts, One with same starttime and endtime but on another department  
 	 */
 	@Test 
 	public void overlappingContactsOneWith0TimeOnAnotherDepartment() {
 	   	in0 = new DateTime(2010, 6, 12, 14, 0, 0);
 	   	out0 = new DateTime(2010, 6, 17, 16, 0, 0);
 	   	in1 = new DateTime(2010, 6, 17, 16, 0, 0);
 	   	out1 = new DateTime(2010, 6, 17, 16, 0, 0);
 	   	in2 = new DateTime(2010, 6, 17, 16, 0, 0);
 	   	out2 = new DateTime(2010, 6, 17, 21, 0, 0);
 	   	afdelingsCode1 = "777";
 		sygehusCode1 = sygehusCode0;
 		afdelingsCode2 = afdelingsCode0;
 		sygehusCode2 = sygehusCode0;
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
 
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
 
 		assertEquals(3, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 	}
 	
 	/*
 	 * 2 overlapping contacts, One with same starttime and endtime but on another department  
 	 */
 	@Test 
 	public void twoOverlappingContactsOneWith0TimeOnAnotherDepartment() {
 	   	in0 = new DateTime(2010, 4, 20, 17, 0, 0);
 	   	out0 = new DateTime(2010, 4, 20, 17, 0, 0);
 	   	in1 = new DateTime(2010, 4, 20, 17, 0, 0);
 	   	out1 = new DateTime(2010, 4, 20, 19, 0, 0);
 	   	afdelingsCode1 = "777";
 		sygehusCode1 = sygehusCode0;
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
 
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 
 		assertEquals(2, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 	}
 	
 	@Test 
 	public void threeOverlappingContactsOneWith0TimeOnAnotherDepartment() {
 	   	in0 = new DateTime(2010, 4, 22, 1, 0, 0);
 	   	out0 = new DateTime(2010, 4, 25, 12, 0, 0);
 	   	afdelingsCode0 = "043";
 	   	sygehusCode0 = "8040";
 	   	in1 = new DateTime(2010, 4, 23, 20, 0, 0);
 	   	out1 = new DateTime(2010, 4, 25, 12, 0, 0);
 	   	afdelingsCode1 = "201";
 	   	sygehusCode1 = "8003";
 	   	in2 = new DateTime(2010, 4, 25, 12, 0, 0);
 	   	out2 = new DateTime(2010, 4, 29, 13, 0, 0);
 	   	afdelingsCode2 = "202";
 	   	sygehusCode2 = "8003";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
 
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
 
 		assertEquals(3, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 		assertEquals("2010-04-22 01:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode0, String.class));
 		assertEquals("2010-04-25 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode1, String.class));
 		assertEquals("2010-04-25 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode2, String.class));
 
 		assertEquals("2010-04-25 12:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode0, String.class));
 		assertEquals("2010-04-25 12:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode1, String.class));
 		assertEquals("2010-04-29 13:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ="+afdelingsCode2, String.class));
 		
 	}
 	
 	@Test 
 	public void fiveOverlappingContactsOneWith0TimeOnAnotherDepartment() {
 	   	in0 = new DateTime(2010, 6, 25, 17, 0, 0);
 	   	out0 = new DateTime(2010, 6, 25, 18, 0, 0);
 	   	afdelingsCode0 = "H90";
 	   	sygehusCode0 = "3800";
 	   	in1 = new DateTime(2010, 6, 25, 18, 0, 0);
 	   	out1 = new DateTime(2010, 6, 30, 8, 0, 0);
 	   	afdelingsCode1 = "H00";
 	   	sygehusCode1 = "3800";
 	   	in2 = new DateTime(2010, 6, 30, 19, 0, 0);
 	   	out2 = new DateTime(2010, 6, 30, 22, 0, 0);
 	   	afdelingsCode2 = "H00";
 	   	sygehusCode2 = "3800";
 	   	in3 = new DateTime(2010, 6, 30, 22, 0, 0);
 	   	out3 = new DateTime(2010, 6, 30, 22, 0, 0);
 	   	afdelingsCode3 = "A10";
 	   	sygehusCode3 = "3800";
 	   	in4 = new DateTime(2010, 6, 30, 22, 0, 0);
 	   	out4 = new DateTime(2010, 7, 01, 12, 0, 0);
 	   	afdelingsCode4 = "H00";
 	   	sygehusCode4 = "3800";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer0), cpr, sygehusCode0, afdelingsCode0, in0.toDate(), out0.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer1), cpr, sygehusCode1, afdelingsCode1, in1.toDate(), out1.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer2), cpr, sygehusCode2, afdelingsCode2, in2.toDate(), out2.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer3), cpr, sygehusCode3, afdelingsCode3, in3.toDate(), out3.toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)",
     			new Long(recordNummer4), cpr, sygehusCode4, afdelingsCode4, in4.toDate(), out4.toDate(), 0);
 
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer0, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer1, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer2, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer3, String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum ="+recordNummer4, String.class));
 
 		assertEquals(5, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 	}
 	
     @Test
     public void testForNegativeAdmissionTime() {
     	String cpr = "1003229";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(2924395640l), cpr, "3800", "B00", new DateTime(2010, 4, 20, 12, 0, 0).toDate(), new DateTime(2010, 4, 26, 13, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(2946985224l), cpr, "3800", "B0D", new DateTime(2010, 4, 26, 14, 0, 0).toDate(), new DateTime(2010, 4, 29, 0, 0, 0).toDate(), 2);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(3028094565l), cpr, "1301", "521", new DateTime(2010, 4, 12, 21, 14, 0).toDate(), new DateTime(2010, 4, 12, 22, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(3090869012l), cpr, "1301", "471", new DateTime(2010, 4, 12, 21, 30, 0).toDate(), new DateTime(2010, 4, 20, 11, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(3155646018l), cpr, "3800", "E3E", new DateTime(2010, 5, 25, 9, 45, 0).toDate(), new DateTime(2010, 7, 12, 0, 0, 0).toDate(), 2);
     	    	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 		// Expected 3 Indlaeggelser and 2 ambulantcontacts
 		assertEquals(3, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 		assertEquals(2, jdbc.queryForInt("select count(*) from AmbulantKontakt"));
 		
 		//Expected in and out dates
 		
 		assertEquals("2010-04-12 21:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ="+521, String.class));
 		assertEquals("2010-04-12 22:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ="+521, String.class));
 
 		assertEquals("2010-04-12 22:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ="+471, String.class));
 		assertEquals("2010-04-20 12:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ="+471, String.class));
 		
 		assertEquals("2010-04-20 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='B00'", String.class));
 		assertEquals("2010-04-26 13:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where afdelingskode ='B00'", String.class));
 		
 
     }
 	
     @Test
     public void testImportOfOverlappingContacts() {
     	// Found this as an error in the test env. where status wasn't updated for all rows.
     	String cpr = "1016214";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1950697931), cpr, "3800", "P61", new DateTime(2009, 4, 17, 0, 10, 0).toDate(), new DateTime(2009, 4, 18, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1950697952), cpr, "3800", "P61", new DateTime(2009, 4, 16, 14, 42, 0).toDate(), new DateTime(2009, 4, 18, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1950739455), cpr, "3800", "P6E", new DateTime(2009, 4, 17, 11, 00, 0).toDate(), new DateTime(2009, 4, 27, 0, 0, 0).toDate(), 2);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1950817871), cpr, "3800", "N91", new DateTime(2009, 4, 16, 15, 25, 0).toDate(), new DateTime(2009, 4, 17, 0, 0, 0).toDate(), 0);
     	    	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 		
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum =1950697931", String.class));
     	assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum =1950697952", String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum =1950739455", String.class));
 		assertEquals("SUCCESS",jdbcTemplate.queryForObject("select v_status from T_ADM where v_recnum =1950817871", String.class));
 	
 		assertEquals("expected import_dto is set in t_adm table", 4, jdbcTemplate.queryForInt("select count(*) from T_ADM where d_importdto is not null"));
     }
 
     @Test
     public void testMissingImports() {
     	// In the test env. these data seems to be missing?
     	String cpr = "1011052";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1559345303), cpr, "6007", "040", new DateTime(2009, 7, 16, 15, 30, 0).toDate(), new DateTime(2009, 7, 19, 11, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1567168495), cpr, "6008", "219", new DateTime(2009, 8, 13, 8, 0, 0).toDate(), new DateTime(2009, 8, 13, 0, 0, 0).toDate(), 2);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1567926885), cpr, "6007", "047", new DateTime(2009, 7, 19, 12, 00, 0).toDate(), new DateTime(2010, 4, 26, 0, 0, 0).toDate(), 2);
     	    	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 		
 		assertEquals("expected import_dto is set in t_adm table", 3, jdbcTemplate.queryForInt("select count(*) from T_ADM where d_importdto is not null"));
 		
 		assertEquals("expected 1 admission", 1, jdbc.queryForInt("select count(*) from Indlaeggelser"));
		assertEquals("expected 2 ambulant contacts", 2, jdbc.queryForInt("select count(*) from ambulantkontakt"));
     }
 
     @Test
     public void testSummertimeProblemWhenProcedureTimeIs0() {
     	String cpr = "1016214";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1854549558), cpr, "4202", "560", new DateTime(2009, 03, 18, 13, 0, 0).toDate(), new DateTime(2009, 3, 29, 7, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_KODER (v_recnum, c_kode, c_tilkode, c_kodeart, d_pdto, c_psgh, c_pafd, v_type) values (?, ?, ?, ?, ?, ?, ?,?)", 
     			new Long(1854549558), "BWDB01", null, null, new DateTime(2009, 03, 29, 0, 0, 0).toDate(), "4202", "560", "opr");
     	    	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 		
 		// expected admission enddate must be 2009-03-29 12:00:00
 		assertEquals("2009-03-29 12:00:00.0",jdbc.queryForObject("select Udskrivningsdatotid from Indlaeggelser where cpr ="+cpr, String.class));
     }
 
     @Test
     public void testErrorWithOverlappingContacts() {
     	String cpr = "100";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1754549558), cpr, "2000", "221", new DateTime(2009, 1, 8, 3, 45, 0).toDate(), new DateTime(2009, 1, 10, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1754549559), cpr, "2000", "221", new DateTime(2009, 1, 9, 13, 38, 0).toDate(), new DateTime(2009, 1, 10, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1754549560), cpr, "2000", "272", new DateTime(2009, 1, 10, 0, 1, 0).toDate(), new DateTime(2009, 1, 13, 16, 0, 0).toDate(), 0);
     	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		// 2 admissions are expected, due to connecting contacts from same hospital and department.
 		assertEquals("expected 2 admissions", 2, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 		
 		assertEquals("2009-01-08 03:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='221'", String.class));
 		assertEquals("2009-01-10 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='272'", String.class));
 		
     }
 
     @Test
     public void testErrorWithOverlappingContacts2() {
     	String cpr = "100";
 
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1654549558), cpr, "4200", "390", new DateTime(2009, 1, 30, 2, 5, 0).toDate(), new DateTime(2009, 1, 30, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1654549559), cpr, "4200", "020", new DateTime(2009, 1, 30, 4, 30, 0).toDate(), new DateTime(2009, 1, 30, 12, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1654549560), cpr, "4200", "360", new DateTime(2009, 1, 30, 10, 20, 0).toDate(), new DateTime(2009, 1, 30, 14, 0, 0).toDate(), 0);
     	jdbcTemplate.update("insert into T_ADM (v_recnum, v_cpr, c_sgh, c_afd, d_inddto, d_uddto, c_pattype) values (?, ?, ?, ?, ?, ?, ?)", 
     			new Long(1654549561), cpr, "4200", "270", new DateTime(2009, 1, 30, 12, 40, 0).toDate(), new DateTime(2009, 2, 3, 14, 0, 0).toDate(), 0);
     	
     	List<Administration> contactsByCPR = lprDao.getContactsByCPR(cpr);
 
 		lprPrepareDataRule.setContacts(contactsByCPR);
 		Statistics statistics = Statistics.getInstance();
 		
 		//Process rules
 		LPRRule next = lprPrepareDataRule.doProcessing(statistics);
 		
 		// Process rest of the rules and save admission
 		while(next != null) {
 			next = next.doProcessing(statistics);
 		}
 
 		// 2 admissions are expected, due to connecting contacts from same hospital and department.
 		assertEquals("expected 4 admissions", 4, jdbc.queryForInt("select count(*) from Indlaeggelser"));
 		
 		assertEquals("2009-01-30 02:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='390'", String.class));
 		assertEquals("2009-01-30 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='360'", String.class));
 		assertEquals("2009-01-30 12:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='020'", String.class));
 		assertEquals("2009-01-30 14:00:00.0",jdbc.queryForObject("select Indlaeggelsesdatotid from Indlaeggelser where afdelingskode ='270'", String.class));
 		
     }
 }
