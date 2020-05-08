 package ru.skalodrom_rf;
 
 import org.joda.time.LocalDate;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 import ru.skalodrom_rf.dao.ProfileDao;
 import ru.skalodrom_rf.dao.SkalodromDao;
 import ru.skalodrom_rf.model.Profile;
 import ru.skalodrom_rf.model.Skalodrom;
 import ru.skalodrom_rf.model.Time;
 
 import javax.annotation.Resource;
 import java.util.List;
 
 /**.*/
 
 @RunWith(SpringJUnit4ClassRunner.class)
 
 @ContextConfiguration(locations={"classpath:applicationContext.xml"})
 
 
 public class ProfileTest {
 
     @Resource
     ProfileDao profileDao;
     @Resource
     SkalodromDao skalodromDao;
 
     @Test @Transactional
     public void testConstraint(){
        final Skalodrom skalodrom = skalodromDao.findAll().get(1);
         final List<Profile> profileList = profileDao.findByScalodromAndDate(skalodrom, new LocalDate(), Time.DAY);
         Assert.assertEquals(1,profileList.size());
     }
 }
