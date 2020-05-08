 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package commonInfrastructure.menu.managedbean;
 
 import ACMS.entity.OverbookingQuotaEntity;
 import ACMS.entity.PriceEntity;
 import ACMS.entity.ReservationEntity;
 import ACMS.entity.RoomServiceEntity;
 import ACMS.session.OverbookingSessionBean;
 import ACMS.session.PriceSessionBean;
 import ACMS.session.ReservationSessionBean;
 import ACMS.session.RoomServiceSessionBean;
 import ACMS.session.RoomSessionBean;
 import CRMS.entity.MemberEntity;
 import CRMS.session.MemberSessionBean;
 import ERMS.entity.EmployeeEntity;
 import ERMS.entity.FunctionalityEntity;
 import ERMS.entity.RoleEntity;
 import ERMS.session.EPasswordHashSessionBean;
 import ERMS.session.EmployeeSessionBean;
 import ERMS.session.FunctionalitySessionBean;
 import ERMS.session.RoleSessionBean;
 import FBMS.entity.RestaurantEntity;
 import java.io.Serializable;
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 /**
  *
  * @author Ser3na
  */
 @ManagedBean
 @RequestScoped
 public class initializationManagedBean implements Serializable {
     @EJB
     private PriceSessionBean priceSessionBean;
     
     //    @PersistenceContext
 //    private EntityManager em;
     @EJB
     private OverbookingSessionBean overbookingSessionBean;
     @EJB
     private MemberSessionBean memberSessionBean;
     @EJB
     private ReservationSessionBean reservationSessionBean;
     @EJB
     private RoomSessionBean roomSessionBean;
     @EJB
     private RoomServiceSessionBean roomServiceSessionBean;
     @EJB
     private EmployeeSessionBean employeeSessionBean = new EmployeeSessionBean();
     @EJB
     private RoleSessionBean roleSessionBean;
     @EJB
     private EPasswordHashSessionBean ePasswordHashSessionBean;
     @EJB
     private FunctionalitySessionBean functionalitySessionBean;
    
     private EmployeeEntity employee;
     private RoleEntity role;
     private ReservationEntity reservation;
     private MemberEntity member;
     private FunctionalityEntity functionality;
     private RestaurantEntity restaurant;
     private OverbookingQuotaEntity overbookingQuota;
     private RoomServiceEntity roomService;
     private PriceEntity price;
     
 //    private MemberEntity member;
 
     @PostConstruct
     public void init() {
         FacesContext.getCurrentInstance().getExternalContext().getSession(true);
     }
 
     public EmployeeEntity getEmployee() {
         return employee;
     }
 
     public void setEmployee(EmployeeEntity employee) {
         this.employee = employee;
     }
 
     public RoleEntity getRole() {
         return role;
     }
 
     public void setRole(RoleEntity role) {
         this.role = role;
     }
 
     public void addMessage(String summary) {
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
         FacesContext.getCurrentInstance().addMessage(null, message);
     }
 
     public void createSuperAdmin() {
         System.out.println("go to create super admin");
 
         functionality = new FunctionalityEntity();
         functionality.setFuncName("addRole");
         functionality.setFuncDescription("access right to addRole page");
         functionalitySessionBean.addFunctionality(functionality);
         
         
         role = new RoleEntity();
         role.setRoleId(10);
         role.setRoleName("SuperAdmin");
         role.addFunctionality(functionality);
         System.out.println("Create role :" + role.getRoleName());
 
         employee = new EmployeeEntity();
         employee.setEmployeeId("A0000"); //business assumption: maximum employee number 9999
         employee.setEmployeeName("SuperAdmin");
         employee.setEmployeePassword(ePasswordHashSessionBean.hashPassword("A0000"));
         System.out.println("finished hashing");
         employee.setEmployeeEmail("is3102.it09@gmail.com");
         employee.addRole(role);
         employee.setIsFirstTimeLogin(false);
         System.out.println("Create employee :" + employee.getEmployeeId() + "," + employee.getEmployeeName() + "," + employee.getEmployeePassword());
 
         try {
             System.out.println("Saving Super Admin....");
 
             employeeSessionBean.addEmployee(employee);
             System.out.println("Super Admin saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding admin", ""));
             return;
         }
         System.out.println("Insert Employee into database");
 
         addMessage("Super Admin Created!");
     }
 
     public void createSystemUser() {
         System.out.println("go to create ACMS user");
 
         role = new RoleEntity();
         role.setRoleId(20);
         role.setRoleName("ACMSAdmin");
         System.out.println("Create role :" + role.getRoleName());
 
         employee = new EmployeeEntity();
         employee.setEmployeeId("B0000"); //business assumption: maximum employee number 9999
         employee.setEmployeeName("ACMSAdmin");
         employee.setEmployeePassword(ePasswordHashSessionBean.hashPassword("B0000"));
         System.out.println("finished hashing");
         employee.addRole(role);
         employee.setIsFirstTimeLogin(false);
         System.out.println("Create employee :" + employee.getEmployeeId() + "," + employee.getEmployeeName() + "," + employee.getEmployeePassword());
 
         try {
             System.out.println("Saving ACMSAdmin....");
             employeeSessionBean.addEmployee(employee);
             System.out.println("ACMSAdmin saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding admin", ""));
             return;
         }
         System.out.println("Insert Employee into database");
         addMessage("ACMSAdmin Created!");
     }
 
     public void createReservation() {
         System.out.println("go to create hotel reservation page...");
         Date cidate = new Date(2014, 10, 1);
         Date codate = new Date(2014, 10, 6);
 
         reservation = new ReservationEntity();
         reservation.setRcName("Diana");
         System.out.println("create reservation: welcome " + reservation.getRcName());
         reservation.setRcEmail("diana-wang@yahoo.com");
         reservation.setRcHP("65-81801380");
         reservation.setRcCreditCardNo("1230000045600000");
         reservation.setReservationCorporate("Credit Suisse");
         reservation.setRcCheckInDate(cidate);
         reservation.setRcCheckOutDate(codate);
         reservation.setReservationRoomType("Deluxe");
         reservation.setReservationHotelNo(1);
         reservation.setReservationRoomCount(3);
         reservation.setReservationGuestCount(6);
         reservation.setRcMember(member);
 
         try {
             System.out.println("Saving hotel reservation....");
 
             reservationSessionBean.addReservation(reservation);
             System.out.println("Hotel Reservation saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding reservation", ""));
             return;
         }
         System.out.println("Insert Reservation into database");
 
         addMessage("Reservation Created!");
     }
     
     public void createFBMSAdmin() {
         System.out.println("go to create FBMS page");
 
         role = new RoleEntity();
         role.setRoleId(50);
         role.setRoleName("FBMSAdmin");
         System.out.println("Create role :" + role.getRoleName());
 
         employee = new EmployeeEntity();
         employee.setEmployeeId("E0000"); //business assumption: maximum employee number 9999
         employee.setEmployeeName("FBMSAdmin");
         employee.setEmployeePassword(ePasswordHashSessionBean.hashPassword("E0000"));
         System.out.println("finished hashing");
         employee.addRole(role);
         employee.setIsFirstTimeLogin(false);
         System.out.println("Create employee :" + employee.getEmployeeId() + "," + employee.getEmployeeName() + "," + employee.getEmployeePassword());
 
         try {
             System.out.println("Saving FBMSAdmin....");
             employeeSessionBean.addEmployee(employee);
             System.out.println("FBMSAdmin saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding admin", ""));
             return;
         }
         System.out.println("Insert Employee into database");
         addMessage("FBMSAdmin Created!");
     }
     
     public void createCRMSAdmin() {
         System.out.println("go to create CRMS page");
 
         role = new RoleEntity();
         role.setRoleId(80);
         role.setRoleName("CRMSAdmin");
         System.out.println("Create role :" + role.getRoleName());
 
         employee = new EmployeeEntity();
         employee.setEmployeeId("H0000"); //business assumption: maximum employee number 9999
         employee.setEmployeeName("CRMSAdmin");
         employee.setEmployeePassword(ePasswordHashSessionBean.hashPassword("H0000"));
         System.out.println("finished hashing");
         employee.addRole(role);
         employee.setIsFirstTimeLogin(false);
         System.out.println("Create employee :" + employee.getEmployeeId() + "," + employee.getEmployeeName() + "," + employee.getEmployeePassword());
 
         try {
             System.out.println("Saving CRMSAdmin....");
             employeeSessionBean.addEmployee(employee);
             System.out.println("CRMSAdmin saved.....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding admin", ""));
             return;
         }
         System.out.println("Insert Employee into database");
         addMessage("CRMSAdmin Created!");
     }
     
     public void createMember() {
         System.err.println("go to create member page...");       
         Date qqdate = new Date(91,02,11);
         
         member = new MemberEntity();
         member.setMemberEmail("xinqi_wang@yahoo.com");
         member.setMemberPassword("ABCabc123");
         member.setMemberName("Diana");
         System.out.println("Create a new member: welcome! " + member.getMemberName());
         member.setMemberHP("92728760");
         member.setNationality("China");
         member.setMemberDob(qqdate);
         member.setGender("Female");
         member.setMaritalStatus("Single");
         member.setIsVIP(false);
         member.setIsSubscriber(true);
         member.setSecurityQuestion("What is your mother's original surname?");
         member.setAnswer("Wang");
         member.setPreferences("to be set");
         
         try {
             System.out.println("Creating new member....");
             memberSessionBean.addMember(member);
             System.out.println("Member created....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding member", ""));
             return;
         }
         System.err.println("Insert Dayanqi member into database");
        addMessage("Member Created!");
     }
     
     public void createVIP(){
         System.err.println("go to create VIP page...");       
         Date bowendate = new Date(90,10,8);
         
         member = new MemberEntity();
         member.setMemberEmail("bowen@nus.edu.sg");
         member.setMemberPassword("ABCabc123");
         member.setMemberName("Bowen");
         System.out.println("Create a new member: welcome! " + member.getMemberName());
         member.setMemberHP("92728760");
         member.setNationality("China");
         member.setMemberDob(bowendate);
         member.setGender("Female");
         member.setMaritalStatus("Single");
         member.setIsVIP(true);
         member.setIsSubscriber(true);
         member.setSecurityQuestion("What is your mother's original surname?");
         member.setAnswer("Zheng");
         member.setPreferences("to be set");
         member.setPoint(10000);
         member.setCoin(200);
         
         try {
             System.out.println("Creating new member....");
             memberSessionBean.addMember(member);
             System.out.println("Member created....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding member", ""));
             return;
         }
         System.err.println("Insert Bowen VIP into database");
        addMessage("VIP member Created!");
     }
 
     public void createRoom() {
         try {
             System.err.println("Insert room started.....");
             price = new PriceEntity();
             price.setPriceType("deluxe");
             price.setPrice(485.3);
             priceSessionBean.createPrice(price);
             price = new PriceEntity();
             price.setPriceType("superior");
             price.setPrice(380.3);
             priceSessionBean.createPrice(price);
             roomSessionBean.createTestRoom(1,1,1,"deluxe","available");
             roomSessionBean.createTestRoom(1,1,2,"deluxe", "available");
             roomSessionBean.createTestRoom(1,1,3,"superior","available");
             roomSessionBean.createTestRoom(1,1,4,"superior","available");
             roomSessionBean.createTestRoom(1,1,5,"superior","available");
             /*
             RoomEntity room1 = new RoomEntity();
             room1.setRoomId(1, 1, 1);
             room1.setRoomType("deluxe");
             room1.setRoomStatus("available");
             rmSessionBean.createTestRoom(room1);
             RoomEntity room2 = new RoomEntity();
             room2.setRoomId(1, 1, 2);
             room2.setRoomType("deluxe");
             room2.setRoomStatus("available");
             rmSessionBean.createTestRoom(room2);
             RoomEntity room3 = new RoomEntity();
             room3.setRoomId(1, 1, 3);
             room3.setRoomType("deluxe");
             room3.setRoomStatus("reserved");
             rmSessionBean.createTestRoom(room3);
             RoomEntity room4 = new RoomEntity();
             room4.setRoomId(1, 1, 4);
             room4.setRoomType("deluxe");
             room4.setRoomStatus("occupied");
             rmSessionBean.createTestRoom(room4);
             *//*
             Query query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,1,'Deluxe');");
             query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,2,'Deluxe');");
             query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,3,'Deluxe');");
             query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,4,'Deluxe');");
             query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,5,'Deluxe');");
             query = em.createQuery("INSERT INTO roomentity(ROOMEHOTEL,ROOMLEVEL,ROOMNO,ROOMTYPE)\n"
                     + "VALUES (1,1,6,'Deluxe');");
                     */
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room to Orchard Hotel", ""));
             return;
         }
         System.out.println("Insert room into database");
         addMessage("Room Created!");
     }
       
     public void createFunctionalities(){
         functionality = new FunctionalityEntity();
         functionality.setFuncName("addFunctionality");
         functionality.setFuncDescription("access right to addFunctionality page");
         
         try {
             System.out.println("Creating new functionality....");
             functionalitySessionBean.addFunctionality(functionality);
             System.out.println("Functionality created....");
         } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding functionality", ""));
             return;
         }
         System.err.println("Insert systemMsg functionality into database");
         addMessage("Functionality Created!");
     }
     
     public void createOverbooking(){
         overbookingQuota = new OverbookingQuotaEntity();
         overbookingQuota.setOverbookingId(1);
         overbookingQuota.setRoomType("deluxe");
         overbookingQuota.setQuota(0);
         overbookingQuota.setCompensation1(105);
         overbookingQuota.setCompensation2(485.3);
         
         try {
             System.err.println("Initiating the overbooking entity...");
             overbookingSessionBean.initOverbooking(overbookingQuota);
             System.out.println("Overbooking record initiated");
         }catch (Exception e) {
              FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when initiating overbooking", ""));
             return;
         }
         System.err.println("Initiating overbooking entity into database");
     }
     
     public void createRmService(){
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 1....");
         
         roomService.setRoomServiceName("Laundry");
         roomService.setRoomServicePrice(0);
         roomService.setCategory("free service");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 2....");
         
         roomService.setRoomServiceName("Housekeeping");
         roomService.setRoomServicePrice(0);
         roomService.setCategory("free service");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 3....");
         
         roomService.setRoomServiceName("TV Channel Subscription 1");
         roomService.setRoomServicePrice(19.9);
         roomService.setCategory("charged service");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 4....");
         
         roomService.setRoomServiceName("TV Channel Subscription 2");
         roomService.setRoomServicePrice(49.9);
         roomService.setCategory("charged service");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         
          roomService = new RoomServiceEntity();
         System.out.println("Creating room service 5....");
         
         roomService.setRoomServiceName("Custard Puff");
         roomService.setRoomServicePrice(5.4);
         roomService.setCategory("food");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 6....");
         
         roomService.setRoomServiceName("Chocolate Puff");
         roomService.setRoomServicePrice(5.4);
         roomService.setCategory("food");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
         roomService = new RoomServiceEntity();
         System.out.println("Creating room service 7....");
         
         roomService.setRoomServiceName("Thai Pineapple Rice");
         roomService.setRoomServicePrice(10);
         roomService.setCategory("food");
         
         try {
             System.out.println(roomService.getRoomServiceName());
             System.out.println(roomService.getRoomServicePrice());
             roomServiceSessionBean.addRoomService(roomService);
             System.err.println("roomService added");
         }catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding room service", ""));
             return;
         }
     }
     //Add new test cases below!!!!!!!!!
     
     public void initialize(){
         createSuperAdmin();
         createSystemUser();
         createMember();
         createVIP();
         createReservation();
         createFBMSAdmin();
         createCRMSAdmin();
         createRoom();
         createFunctionalities();
         createOverbooking();
         createRmService(); 
         
         addMessage("Initialization succeed!");
     }
 
 }
