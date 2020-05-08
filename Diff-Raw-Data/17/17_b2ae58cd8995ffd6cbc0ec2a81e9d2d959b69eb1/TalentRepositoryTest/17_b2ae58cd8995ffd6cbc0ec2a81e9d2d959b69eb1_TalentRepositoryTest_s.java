 package org.sukrupa.student;
 
 import org.hibernate.SessionFactory;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
import org.sukrupa.app.config.AppConfigForTestsContextLoader;
 import org.sukrupa.platform.DatabaseHelper;
 
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AppConfigForTestsContextLoader.class)
 @Transactional
 public class TalentRepositoryTest {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private DatabaseHelper databaseHelper;
 
     @Test(expected = Exception.class)
     public void shouldNotAllowInsert() {
 	    Talent sport1 = new Talent("sport");
 	    Talent sport2 = new Talent("sport");
         databaseHelper.save(sport1, sport2);
     }
 
 }
