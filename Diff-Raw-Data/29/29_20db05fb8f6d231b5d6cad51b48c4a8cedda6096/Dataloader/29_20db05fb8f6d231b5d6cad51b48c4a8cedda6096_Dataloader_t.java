 package ru.skalodrom_rf.dataloader;
 
 import org.joda.time.LocalDate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 import ru.skalodrom_rf.dao.ClimbTimeDao;
 import ru.skalodrom_rf.dao.PrefferedWeekDayDao;
 import ru.skalodrom_rf.dao.SkalodromDao;
 import ru.skalodrom_rf.dao.UserDao;
 import ru.skalodrom_rf.model.*;
 
 import javax.annotation.Resource;
 
 /**
  * Load initial data to database/
  */
 public class Dataloader {
     private static final Logger LOG= LoggerFactory.getLogger(Dataloader.class);
     @Resource
     UserDao userDao;
 
     @Resource
     SkalodromDao skalodromDao;
 
 
     @Resource
     PrefferedWeekDayDao prefferedWeekDayDao;
 
     @Resource
     ClimbTimeDao climbTimeDao;
 
     @Transactional
     public  void initializeDatabase(){
         LOG.debug("start database initialization");
         prefferedWeekDayDao.create(new WeekDay(1L,"monday"));
         final WeekDay tuesday = new WeekDay(2L, "tuesday");
         prefferedWeekDayDao.create(tuesday);
         prefferedWeekDayDao.create(new WeekDay(3L,"wednesday"));
         prefferedWeekDayDao.create(new WeekDay(4L,"thursday"));
         prefferedWeekDayDao.create(new WeekDay(5L,"friday"));
         prefferedWeekDayDao.create(new WeekDay(6L,"saturday"));
         prefferedWeekDayDao.create(new WeekDay(7L,"sunday"));
         final ClimbTime climbTime = new ClimbTime();
         climbTime.setDate(new LocalDate());
         climbTime.setTime(Time.DAY);
         climbTimeDao.create(climbTime);
 
 
 
         final User user = new User();
        user.setLogin("dima");
         user.getProfile().setFio("Дима");
 
         user.setPassword("");
         user.getProfile().setEmail("dima@rzhevskiy.info");
         user.getProfile().getWhenClimb().add(climbTime);
         user.getProfile().getPrefferedWeekDay().add(tuesday);
         userDao.create(user);
 
 
         final Skalodrom dds = new Skalodrom("ДДС");
         skalodromDao.create(dds);
 
         user.getProfile().getWhereClimb().add(dds);
         dds.getWhoClimb().add(user.getProfile());
         
         userDao.saveOrUpdate(user);
         skalodromDao.saveOrUpdate(dds);
 
         skalodromDao.create(new Skalodrom("Скаласити"));
         skalodromDao.create(new Skalodrom("Экстрим"));
         skalodromDao.create(new Skalodrom("Южная"));
         skalodromDao.create(new Skalodrom("Бауманская"));
         skalodromDao.create(new Skalodrom("Скалатория"));
 
         LOG.debug("database initialized");
 
 
     }
 }
