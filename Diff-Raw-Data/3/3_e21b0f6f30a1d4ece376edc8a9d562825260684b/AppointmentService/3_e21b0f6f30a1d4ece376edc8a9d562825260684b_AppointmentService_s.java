 package org.mat.nounou.services;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.joda.time.DateTime;
 import org.mat.nounou.model.Appointment;
 import org.mat.nounou.model.Child;
 import org.mat.nounou.model.User;
 import org.mat.nounou.servlets.EntityManagerLoaderListener;
 import org.mat.nounou.util.Check;
 import org.mat.nounou.util.Constants;
 import org.mat.nounou.vo.AppointmentVO;
 import org.mat.nounou.vo.ReportVO;
 
 /**
  * Value Object for Appointment
  * AppointmentVO: mlecoutre
  * Date: 28/10/12
  * Time: 11:13
  */
 @Path("/appointments")
 @Produces(MediaType.APPLICATION_JSON)
 public class AppointmentService {
     @GET
     public List<AppointmentVO> get() {
         System.out.println("Appointment service");
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         TypedQuery<Appointment> query = em.createQuery("FROM Appointment", Appointment.class);
         List<AppointmentVO> apps = new ArrayList<AppointmentVO>();
         try {
             query.setMaxResults(Constants.MAX_RESULT);
             List<Appointment> appointments = query.getResultList();
             for (Appointment a : appointments) {
                 AppointmentVO appVo = populateAppoitmentVO(a);
                 apps.add(appVo);
             }
         } catch (NoResultException nre) {
             System.out.println("No appointment at this time");
         } finally {
             em.close();
         }
         return apps;
     }
 
     @GET
     @Path("/{appointmentId}")
     public AppointmentVO getByAppointmentId(@PathParam("appointmentId") Integer appointmentId) {
         System.out.println("Appointment service");
         if (Check.checkIsEmptyOrNull(appointmentId)) {
             System.out.printf("WARNING: Incorrect parameter for getByAppointmentId:%d\n", appointmentId);
             return null;
         }
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         TypedQuery<Appointment> query = em.createQuery("FROM Appointment WHERE appointmentId=:appointmentId", Appointment.class);
         query.setParameter("appointmentId", appointmentId);
         AppointmentVO appVo = null;
         try {
             Appointment a = query.getSingleResult();
             appVo = populateAppoitmentVO(a);
         } catch (NoResultException nre) {
             System.out.println("No appointment  with id:" + appointmentId);
         } finally {
             em.close();
         }
         return appVo;
     }
 
     /**
      * Used for reports
      *
      * @param accountId
      * @param searchType
      * @return
      */
     @GET
     @Path("/report/account/{accountId}/searchType/{searchType}")
     public ReportVO getLastAppointments(@PathParam("accountId") Integer accountId, @PathParam("searchType") String searchType) {
         System.out.println("getLastAppointments service");
         List<Double[]> data = new ArrayList<Double[]>();
         List<Double[]> dataRange = new ArrayList<Double[]>();
         //Check input parameters
         if (Check.checkIsEmptyOrNull(accountId) || Check.checkIsEmptyOrNull(searchType)) {
             System.out.printf("WARNING: Incorrect parameters accountId:%d, searchType:%s\n", accountId, searchType);
             return null;
         }
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         List<AppointmentVO> vos = new ArrayList<AppointmentVO>();
         long totalDuration = 0;
         try {
             StringBuffer buff = new StringBuffer("FROM Appointment WHERE accountId=:accountId");
 
             TypedQuery<Appointment> query = null;
             if ("last".equals(searchType)) {
                 query = em.createQuery(buff.toString(), Appointment.class);
             } else if ("currentMonth".equals(searchType)) {
                 buff.append(" AND MONTH(arrivalDate) = MONTH(CURRENT_DATE) ORDER BY arrivalDate DESC");
                 query = em.createQuery(buff.toString(), Appointment.class);
             } else if ("currentWeek".equals(searchType)) {
                 Calendar c = Calendar.getInstance();
                 c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                 Date d1 = c.getTime();
                 c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                 Date d2 = c.getTime();
                 buff.append(" AND  arrivalDate > :startDate AND arrivalDate < :endDate");
                 buff.append(" ORDER BY arrivalDate DESC");
                 query = em.createQuery(buff.toString(), Appointment.class);
                 query.setParameter("startDate", d1);
                 query.setParameter("endDate", d2);
             } else if ("prevMonth".equals(searchType)) {
 
             } else if ("lastWeek".equals(searchType)) {
                 Date d = new DateTime().minusWeeks(1).toDate();
                 Calendar c = Calendar.getInstance();
                 c.setTime(d);
                 c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                 Date d1 = c.getTime();
                 c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                 Date d2 = c.getTime();
                 buff.append(" AND  arrivalDate > :startDate AND arrivalDate < :endDate");
                 buff.append(" ORDER BY arrivalDate DESC");
                 query = em.createQuery(buff.toString(), Appointment.class);
                 query.setParameter("startDate", d1);
                 query.setParameter("endDate", d2);
 
             }
             query.setParameter("accountId", accountId);
 
             if ("last".equals(searchType)) {
                 query.setMaxResults(5);
             } else {
                 query.setMaxResults(31);
             }
             List<Appointment> appointments = query.getResultList();
             //populate list of appointments
             for (Appointment app : appointments) {
                 AppointmentVO vo = populateAppoitmentVO(app);
 
                 //calculate duration
                 if (app.getDepartureDate() != null && app.getArrivalDate() != null) {
                     long timeMilli = app.getDepartureDate().getTime() - app.getArrivalDate().getTime();
                     totalDuration = totalDuration + timeMilli;
                     vo.setDuration(TimeService.getDurationBreakdown(timeMilli));
                     //
                     Calendar c = Calendar.getInstance();
                     c.setTime(Constants.sdfDate.parse(Constants.sdfDate.format(app.getDepartureDate())));
                    data.add(new Double[]{new Double(c.getTime().getTime()), TimeService.getDecimalDurationBreakdown(timeMilli)});
                     dataRange.add(0, new Double[]{new Double(c.getTime().getTime()), TimeService.getDecimalTime(app.getArrivalDate()), TimeService.getDecimalTime(app.getDepartureDate())});
                 } else {
                     vo.setDuration("n/a");
                 }
                 vos.add(vo);
             }
 
         } catch (NoResultException nre) {
             System.out.printf("ERROR: No result found with accountId:%d, searchType: %s\n", accountId, searchType);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         ReportVO reportVO = new ReportVO();
         reportVO.setAppointments(vos);
         //calculate total duration
         reportVO.setData(data);
         reportVO.setDataRange(dataRange);
         reportVO.setReportTitle("Total duration");
         reportVO.setTotalDuration(TimeService.getDurationBreakdown(totalDuration));
 
         return reportVO;
     }
 
     private AppointmentVO populateAppoitmentVO(Appointment a) {
         AppointmentVO appVo = new AppointmentVO();
         appVo.setAppointmentId(a.getAppointmentId());
         if (a.getArrivalDate() != null)
             appVo.setArrivalDate(Constants.sdf.format(a.getArrivalDate()));
         if (a.getArrivalUser() != null) {
             appVo.setArrivalUserName(a.getArrivalUser().getFirstName().concat(" ").concat(a.getArrivalUser().getLastName()));
             appVo.setArrivalUserId(a.getArrivalUser().getUserId());
         }
         if (a.getDepartureUser() != null)
             appVo.setDepartureDate(Constants.sdf.format(a.getDepartureDate()));
         if (a.getDepartureUser() != null) {
             appVo.setDepartureUserName(a.getDepartureUser().getFirstName().concat(" ").concat(a.getDepartureUser().getLastName()));
             appVo.setDepartureUserId(a.getDepartureUser().getUserId());
         }
         appVo.setKidId(a.getChild().getChildId());
         appVo.setKidName(a.getChild().getFirstName());
 
         //TODO map Planned date when it will be available
         return appVo;
     }
 
     /**
      * Used for initialization of run  part
      *
      * @param userId userId
      * @return AppointmentVO
      */
     @GET
     @Path("/current/account/{accountId}/userId/{userId}")
     public AppointmentVO getCurrentAppointment(@PathParam("accountId") Integer accountId, @PathParam("userId") Integer userId) {
         //Check input parameters
         if (Check.checkIsEmptyOrNull(accountId) || Check.checkIsEmptyOrNull(userId)) {
             System.out.printf("WARNING: Incorrect parameters accountId:%d, userId:%d\n", accountId, userId);
             return null;
         }
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         User u = null;
         List<Child> children = null;
         try {
             //get Possible Children
             TypedQuery<Child> qChild = em.createQuery("FROM Child c WHERE c.account.accountId=:accountId", Child.class);
             qChild.setParameter("accountId", accountId);
             children = qChild.getResultList();
             //get User
             TypedQuery<User> qUser = em.createQuery("FROM User c WHERE userId=:userId", User.class);
             qUser.setParameter("userId", userId);
             u = qUser.getSingleResult();
         } catch (NoResultException nre) {
             System.out.printf("ERROR: No result found with parameters accountId:%d, userId:%d\n", accountId, userId);
             return null;
         } catch (Exception e) {
             System.out.printf("ERROR: Exception with parameters accountId:%d, userId:%d\n", accountId, userId);
             e.printStackTrace();
             return null;
         } finally {
             em.close();
         }
 
         AppointmentVO vo = new AppointmentVO();
         vo.setAccountId(accountId);
         vo.setCurrentUserId(userId);
 
         if (children.size() > 0) {
             //set the first of the list by default
             Child child = children.get(0);
             vo.setKidId(child.getChildId());
             vo.setKidName(child.getFirstName());
         }
 
         vo.setCurrentUserName(u.getFirstName().concat(" ").concat(u.getLastName()));
         //TODO check if we have an existing appointment today in db
 
         //TODO populate AppointmentVo wit app
 
         //else create a new appointment from scratch
         Date currentDate = new Date();
 
         String dateStr = Constants.sdf.format(currentDate);
         if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12) {
             //we consider that is the arrival
             vo.setArrivalDate(dateStr);
             vo.setDeclarationType("arrival");
         } else {
             //we consider that is the departure
             vo.setDepartureDate(dateStr);
             vo.setDeclarationType("departure");
         }
 
         return vo;
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public AppointmentVO registerAppointment(AppointmentVO appointment) {
         System.out.println("register appointment " + appointment);
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         Appointment entity = null;
         try {
 
             if (appointment.getAppointmentId() != null) {
                 //TODO check
                 TypedQuery<Appointment> qApp = em.createQuery("FROM Appointment a WHERE a.account.accountId=:accountId AND a.arrivalDate EQUALS CURRENT_DATE", Appointment.class);
                 qApp.setParameter("accountId", appointment.getAccountId());
                 Appointment app = qApp.getSingleResult();
 
             } else {
                 entity = new Appointment();
             }
 
             TypedQuery<Child> qChild = em.createQuery("FROM Child c WHERE childId=:childId", Child.class);
             qChild.setParameter("childId", appointment.getKidId());
             Child child = qChild.getSingleResult();
 
             TypedQuery<User> qUser = em.createQuery("FROM User c WHERE userId=:userId", User.class);
             qUser.setParameter("userId", appointment.getCurrentUserId());
             User u = qUser.getSingleResult();
             Date d = null;
             if ("departure".equals(appointment.getDeclarationType())) {
                 entity.setDepartureUser(u);
                 d = Constants.sdf.parse(appointment.getDepartureDate());
                 entity.setDepartureDate(d);
             } else if ("arrival".equals(appointment.getDeclarationType())) {
                 entity.setArrivalUser(u);
                 d = Constants.sdf.parse(appointment.getArrivalDate());
                 entity.setArrivalDate(d);
             } else if ("both".equals(appointment.getDeclarationType())) {
                 entity.setDepartureUser(u);
                 entity.setArrivalUser(u);
                 d = Constants.sdf.parse(appointment.getArrivalDate());
                 entity.setArrivalDate(d);
                 d = Constants.sdf.parse(appointment.getDepartureDate());
                 entity.setDepartureDate(d);
             } else {
                 System.out.println("ERROR: Unknown declaration type: " + appointment.getDeclarationType());
                 return null;
             }
             entity.setAccount(u.getAccount());
             entity.setChild(child);
             em.getTransaction().begin();
             em.persist(entity);
             em.getTransaction().commit();
             appointment.setAppointmentId(entity.getAppointmentId());
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
 
 
         return appointment;
     }
 
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/{appointmentId}")
     public AppointmentVO saveAppointment(@PathParam("appointmentId") Integer appointmentId, AppointmentVO appointment) {
         System.out.println("Update appointment " + appointment);
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
 
         try {
 
             TypedQuery<Appointment> qApp = em.createQuery("FROM Appointment a WHERE appointmentId=:appointmentId", Appointment.class);
             qApp.setParameter("appointmentId", appointmentId);
             Appointment app = qApp.getSingleResult();
 
 
             TypedQuery<Child> qChild = em.createQuery("FROM Child c WHERE childId=:childId", Child.class);
             qChild.setParameter("childId", appointment.getKidId());
             Child child = qChild.getSingleResult();
 
             TypedQuery<User> qUser = em.createQuery("FROM User c WHERE userId=:userId", User.class);
             qUser.setParameter("userId", appointment.getDepartureUserId());
             User du = qUser.getSingleResult();
 
             qUser.setParameter("userId", appointment.getArrivalUserId());
             User da = qUser.getSingleResult();
             Date d = null;
 
             app.setDepartureUser(du);
             app.setArrivalUser(da);
             d = Constants.sdf.parse(appointment.getArrivalDate());
             app.setArrivalDate(d);
             d = Constants.sdf.parse(appointment.getDepartureDate());
             app.setDepartureDate(d);
 
             app.setAccount(du.getAccount());
             app.setChild(child);
             em.getTransaction().begin();
             em.merge(app);
             em.getTransaction().commit();
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return appointment;
     }
 
 
     @GET
     @Path("/delete/{appointmentId}")
     public Response deleteById(@PathParam("appointmentId") Integer appointmentId) {
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             em.getTransaction().begin();
             Query query = em.createQuery("DELETE FROM Appointment WHERE appointmentId=:appointmentId");
             query.setParameter("appointmentId", appointmentId);
             query.executeUpdate();
             em.getTransaction().commit();
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return Response.ok().build();
     }
 
 
 }
