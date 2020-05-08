 package reevent.dao;
 
 import com.mysema.query.jpa.JPQLQuery;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import reevent.domain.Event;
 import reevent.domain.Location;
 import reevent.domain.QEvent;
 import reevent.domain.User;
 import reevent.util.LocationUtil;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 
 @Transactional
 @Repository
 public class EventDaoJpa extends EntityDaoBase<Event> implements EventDao {
 	QEvent $ = QEvent.event;
 
     @Autowired
     UserDao userDao;
 	
 	@Override
 	public boolean eventNameExists(String eventname) {
 		return queryByEventname(eventname).exists();
 	}
 
     @Override
     public List<Event> findByCreator(User user, Integer first, Integer count) {
         return query($, first, count).where($.createdBy.eq(user)).list($);
     }
 
     @Override
     public List<Event> findByCreator(String username, Integer first, Integer count) {
         return query($, first, count).where($.createdBy.username.eq(username)).list($);
     }
 
     @Override
     public List<Event> findNear(Location origin, double distance, int first, int count) {
         JPQLQuery q = query($, first, count);
 
         return near(q, origin, distance).list($);
     }
 
     JPQLQuery near(JPQLQuery q, Location origin, double distance) {
         Location[] b = LocationUtil.bounds(origin, distance);
 
         Double lng0 = b[0].getLongitude();
         Double lng1 = b[1].getLongitude();
         Double lat0 = b[0].getLatitude();
         Double lat1 = b[1].getLatitude();
 
         return q.where(
                 $.location.longitude.goe(min(lng0, lng1)),
                 $.location.longitude.loe(max(lng0, lng1)),
                 $.location.latitude.goe(min(lat0, lat1)),
                 $.location.latitude.loe(max(lat0, lat1)));
     }
 
     JPQLQuery nextDays(JPQLQuery q, int days) {
         Date start = new Date();
         System.out.println("start="+start);
         Calendar cal = Calendar.getInstance();
         cal.setTime(start);
         cal.add(Calendar.DATE, days);
         Date end = cal.getTime();
         System.out.println("end="+end);
 
         return q.where($.start.gt(start), $.start.lt(end));
     }
 
     static double DISTANCE = 50.0;
 
     @Override
     public List<? extends Event> findUpcoming(Location location, int first, int count) {
         JPQLQuery q = query($, first, count);
         if (location != null) {
             q = near(q, location, DISTANCE);
         }
        q = nextDays(q,30);
         return q.list($);
     }
 
     private JPQLQuery queryByEventname(String eventname) {
         return query().from($).where($.name.eq(eventname));
     }
 }
