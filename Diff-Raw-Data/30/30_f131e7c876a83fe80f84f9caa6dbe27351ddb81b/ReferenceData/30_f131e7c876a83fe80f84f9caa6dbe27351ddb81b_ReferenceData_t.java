 /*
  * Copyright © 2013 VillageReach. All Rights Reserved. This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
  *
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.performancetesting;
 
 import lombok.Getter;
 import org.openlmis.core.domain.*;
 import org.openlmis.performancetesting.builder.*;
 import org.openlmis.performancetesting.dao.*;
 import org.openlmis.rnr.domain.ProgramRnrTemplate;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
 import static java.lang.Integer.valueOf;
 import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
 import static org.apache.commons.lang.math.RandomUtils.nextBoolean;
 import static org.openlmis.performancetesting.Properties.*;
 import static org.openlmis.performancetesting.Utils.periodEndDate;
 import static org.openlmis.performancetesting.Utils.periodStartDate;
 import static org.openlmis.rnr.domain.RnrStatus.RELEASED;
 
 public class ReferenceData {
 
   private FacilityBuilder facilityBuilder = new FacilityBuilder();
   private ProgramRnrTemplateBuilder programRnrTemplateBuilder = new ProgramRnrTemplateBuilder();
   private UserBuilder userBuilder = new UserBuilder();
   private SupervisoryNodeBuilder supervisoryNodeBuilder = new SupervisoryNodeBuilder();
   private RequisitionGroupBuilder requisitionGroupBuilder = new RequisitionGroupBuilder();
   private ProcessingScheduleBuilder scheduleBuilder = new ProcessingScheduleBuilder();
   private SupplyLineBuilder supplyLineBuilder = new SupplyLineBuilder();
   private ProductData productData = new ProductData();

   private ProgramData programData = new ProgramData();
   private RequisitionData requisitionData = new RequisitionData();
   private RoleRightData roleRightData = new RoleRightData();

   private FacilityDAO facilityDAO;
   private ProgramRnrTemplateDAO programRnrTemplateDAO;
   private UserDAO userDAO;
   private SupervisoryNodeDAO supervisoryNodeDAO;
   private RequisitionGroupDAO requisitionGroupDAO;
   private ProcessingScheduleDAO processingScheduleDAO;
   private SupplyLineDAO supplyLineDAO;
  ExecutorService service = Executors.newFixedThreadPool(Properties.NO_OF_PARALLEL_THREADS);
 
   @Getter
   private final ArrayList<ProcessingPeriod> periodList = new ArrayList<>();
 
   @Getter
   private List<Program> programList;
 
   private List<FacilityType> facilityTypes = new ArrayList<>();
 
   private ArrayList<Role> rolesList = new ArrayList<>();
   List<GeographicLevel> zoneLevels = new ArrayList<>();
   private Vendor vendor;
   private ProcessingSchedule monthlySchedule;
   private ProcessingSchedule quarterlySchedule;
 
   @Getter
   private List<ProductForm> productForms = new ArrayList<>();
 
   @Getter
   private List<DosageUnit> dosageUnits = new ArrayList<>();
 
   @Getter
   private List<ProductCategory> productCategories = new ArrayList<>();
 
   private Map<Program, List<Integer>> programProductCountMap = new HashMap<>();
 
   public ReferenceData() {
     ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext-performance.xml");
     facilityDAO = (FacilityDAO) ctx.getBean("facilityDAO");
     programRnrTemplateDAO = (ProgramRnrTemplateDAO) ctx.getBean("programRnrTemplateDAO");
     userDAO = (UserDAO) ctx.getBean("userDAO");
     supervisoryNodeDAO = (SupervisoryNodeDAO) ctx.getBean("supervisoryNodeDAO");
     requisitionGroupDAO = (RequisitionGroupDAO) ctx.getBean("requisitionGroupDAO");
     processingScheduleDAO = (ProcessingScheduleDAO) ctx.getBean("processingScheduleDAO");
     supplyLineDAO = (SupplyLineDAO) ctx.getBean("supplyLineDAO");
 
   }
 
   public void init() {
     setupVendors();
     dosageUnits = productData.setupDosageUnits();
     productCategories = productData.setupProductCategories();
     productForms = productData.setupProductForms();
     programList = programData.setupPrograms(programProductCountMap);
     rolesList = roleRightData.setupRoleRights();
     requisitionData.setupLossesAndAdjustmentTypes();
 
     insertFacilityTypes();
     insertZoneLevels();
     insertGeoZones();
     insertSchedulesAndPeriods();
     insertRnrTemplate();
 
   }
 
   public void setupProducts() throws InterruptedException {
     int createdProductCounter = 0;
     List<Thread> threads = new ArrayList<>();
     for (final Program program : programProductCountMap.keySet()) {
       final int fullSupplyProductCount = programProductCountMap.get(program).get(0);
       final int nonFullSupplyProductCount = programProductCountMap.get(program).get(1);
       createdProductCounter += (fullSupplyProductCount + nonFullSupplyProductCount);
 
       Thread thread1 = new Thread() {
         public void run() {
           insertProductAndItsMappings(fullSupplyProductCount, program, true);
           insertProductAndItsMappings(nonFullSupplyProductCount, program, false);
         }
       };
       threads.add(thread1);
       thread1.start();
 
       Thread thread2 = new Thread() {
         public void run() {
           insertProductAndItsMappings(nonFullSupplyProductCount, program, false);
         }
       };
       threads.add(thread2);
       thread2.start();
     }
 
     for (; createdProductCounter < TOTAL_NO_OF_PRODUCTS; createdProductCounter++) {
       ProductForm productForm = productForms.get(valueOf(randomNumeric(3)) % productForms.size());
       DosageUnit dosageUnit = dosageUnits.get(valueOf(randomNumeric(3)) % dosageUnits.size());
       ProductCategory category = productCategories.get(valueOf(randomNumeric(3)) % productCategories.size());
 
       productData.insertProduct(productForm, dosageUnit, category, nextBoolean());
     }
 
     for (Thread thread : threads) {
       thread.join();
     }
   }
 
   private void insertProductAndItsMappings(int fullSupplyProductCount, Program program, Boolean fullSupply) {
     for (int i = 0; i < fullSupplyProductCount; i++) {
       ProductForm productForm = productForms.get(valueOf(randomNumeric(3)) % productForms.size());
       DosageUnit dosageUnit = dosageUnits.get(valueOf(randomNumeric(3)) % dosageUnits.size());
       ProductCategory category = productCategories.get(valueOf(randomNumeric(3)) % productCategories.size());
 
       Product product = productData.insertProduct(productForm, dosageUnit, category, fullSupply);
       ProgramProduct programProduct = productData.insertProgramProduct(program, product);
       for (FacilityType facilityType : facilityTypes) {
         productData.insertFacilityApprovedProduct(facilityType, programProduct);
       }
     }
 
   }
 
 
   public void insertFacilityAndUsers() {
 
 
     //TODO insert facility operator
     FacilityOperator facilityOperator = facilityBuilder.createFacilityOperator();
 
     List<Facility> facilityList = new ArrayList<>();
     for (int facilityCounter = 0; facilityCounter < STATES_PER_COUNTRY * DISTRICT_PER_STATE * FACILITIES_PER_DISTRICT; facilityCounter++) {
       GeographicZone geoZone = facilityDAO.getDistrictZone(facilityCounter % (STATES_PER_COUNTRY * DISTRICT_PER_STATE));
       FacilityType facilityType = facilityTypes.get(facilityCounter % NO_OF_FACILITY_TYPES);
       final Facility facility = insertFacility(geoZone, facilityType, facilityOperator);
       facilityList.add(facility);
       insertPairOfUserWithCreateAndAuthorizeRights(facility);
 
       if ((facilityCounter + 1) % FACILITIES_PER_REQUISITION_GROUP == 0) {
         final SupervisoryNode supervisoryNode = insertSupervisoryNodePair(facility);
         final RequisitionGroup requisitionGroup = insertRequisitionGroup(supervisoryNode);
         insertRequisitionGroupMember(requisitionGroup, facilityList);
         createApproverAtSupervisoryNodes(supervisoryNode);
         for (final Program program : programList) {
           final List<Facility> facilities = facilityList;
          Thread thread = new Thread() {
             public void run() {
               insertProgramsSupported(program, facilities);
               insertSupplyLine(supervisoryNode, program, facility);
               insertRequisitionGroupProgramSchedule(requisitionGroup, program, monthlySchedule, facility);
               createRequisitions(facilities, program, facility, supervisoryNode);
             }
          };
          service.execute(thread);
 
         }
         facilityList = new ArrayList<>();
       }
     }
    service.shutdown();
   }
 
   private void insertProgramsSupported(Program program, List<Facility> facilities) {
     for (Facility facility : facilities) {
       programData.setupProgramSupported(program, facility);
     }
   }
 
   private void createRequisitions(List<Facility> facilities, Program program, Facility supplyingFacility, SupervisoryNode supervisoryNode) {
     for (int facilityIndex = 0; facilityIndex < facilities.size(); facilityIndex++) {
 
       requisitionData.createRequisition(periodList.subList(STARTING_PERIOD_MONTH, ENDING_PERIOD_MONTH),
           facilities.get(facilityIndex), program, null, supplyingFacility, RELEASED);
 
       requisitionData.createRequisition(facilityIndex, periodList.subList(ENDING_PERIOD_MONTH, ENDING_PERIOD_MONTH + 1),
           facilities.get(facilityIndex), program, supervisoryNode, supplyingFacility);
     }
   }
 
   private void createApproverAtSupervisoryNodes(SupervisoryNode supervisoryNode) {
     User user1 = userBuilder.createUser(null, vendor);
     User user2 = userBuilder.createUser(null, vendor);
     userDAO.insertUser(user1);
     userDAO.insertUser(user2);
     Role lmu = rolesList.get(3);
     Role medicalOfficer = rolesList.get(5);
 
     for (Program program : programList) {
       RoleAssignment roleAssignment1 = userBuilder.createRoleAssignment(program, user1, lmu, supervisoryNode);
       userDAO.insertRoleAssignment(roleAssignment1);
 
       RoleAssignment roleAssignment2 = userBuilder.createRoleAssignment(program, user2, medicalOfficer, supervisoryNode.getParent());
       userDAO.insertRoleAssignment(roleAssignment2);
     }
   }
 
   private RequisitionGroupProgramSchedule insertRequisitionGroupProgramSchedule(RequisitionGroup requisitionGroup,
                                                                                 Program program,
                                                                                 ProcessingSchedule monthlySchedule,
                                                                                 Facility facility) {
     RequisitionGroupProgramSchedule rgProgramSchedule = requisitionGroupBuilder.createRGProgramSchedule(requisitionGroup, program, monthlySchedule, facility);
     requisitionGroupDAO.insertRequisitionGroupProgramSchedule(rgProgramSchedule);
     return rgProgramSchedule;
   }
 
   private SupplyLine insertSupplyLine(SupervisoryNode supervisoryNode, Program program, Facility facility) {
     SupplyLine supplyLine = supplyLineBuilder.createSupplyLine(supervisoryNode, program, facility);
 
     supplyLineDAO.insertSupplyLine(supplyLine);
     return supplyLine;
   }
 
   private RequisitionGroup insertRequisitionGroup(SupervisoryNode supervisoryNode) {
     RequisitionGroup requisitionGroup = requisitionGroupBuilder.createRequisitionGroup(supervisoryNode);
     requisitionGroupDAO.insertRequisitionGroup(requisitionGroup);
     return requisitionGroup;
   }
 
   private void insertRequisitionGroupMember(RequisitionGroup requisitionGroup, List<Facility> facilityList) {
     for (Facility facility : facilityList) {
       RequisitionGroupMember rgMember = requisitionGroupBuilder.createRequisitionGroupMember(requisitionGroup, facility);
       requisitionGroupDAO.insertRequisitionMember(rgMember);
     }
   }
 
   private void insertSchedulesAndPeriods() {
     monthlySchedule = scheduleBuilder.createSchedule("MONTHLY", "monthly");
     processingScheduleDAO.insertSchedule(monthlySchedule);
 
     quarterlySchedule = scheduleBuilder.createSchedule("QUARTERLY", "QUARTERLY");
     processingScheduleDAO.insertSchedule(quarterlySchedule);
 
     for (int year = 2011, yearNumber = 0; year < 2013; year++, yearNumber++) {
       for (int month = 1; month <= 12; month++) {
         Date periodStartDate = periodStartDate(year, month);
         Date periodEndDate = periodEndDate(year, month);
         ProcessingPeriod period = scheduleBuilder.createPeriod(month + yearNumber * 12, periodStartDate, periodEndDate, monthlySchedule);
         periodList.add(period);
       }
     }
 
     for (ProcessingPeriod period : periodList) {
       processingScheduleDAO.insertPeriod(period);
     }
   }
 
   private SupervisoryNode insertSupervisoryNodePair(Facility facility) {
     SupervisoryNode parentNode = supervisoryNodeBuilder.createSupervisoryNode(facility, null);
     supervisoryNodeDAO.insertSupervisoryNode(parentNode);
 
     SupervisoryNode childNode = supervisoryNodeBuilder.createSupervisoryNode(facility, parentNode);
     supervisoryNodeDAO.insertSupervisoryNode(childNode);
 
     return childNode;
   }
 
   public void setupVendors() {
     vendor = new Vendor("openLmis", true);
     userDAO.insertVendor(vendor);
   }
 
   private void insertPairOfUserWithCreateAndAuthorizeRights(Facility facility) {
     Role storeInCharge = rolesList.get(2);
     Role facilityHead = rolesList.get(4);
 
     User user1 = userBuilder.createUser(facility, vendor);
     userDAO.insertUser(user1);
 
     User user2 = userBuilder.createUser(facility, vendor);
     userDAO.insertUser(user2);
 
     for (Program program : programList) {
       userDAO.insertRoleAssignment(userBuilder.createRoleAssignment(program, user1, storeInCharge, null));
       userDAO.insertRoleAssignment(userBuilder.createRoleAssignment(program, user2, facilityHead, null));
     }
   }
 
 
   private void insertRnrTemplate() {
     for (Program program : programList) {
       ProgramRnrTemplate rnrTemplate = programRnrTemplateBuilder.createProgramRnrTemplate(program);
       programRnrTemplateDAO.insertRnrTemplate(rnrTemplate);
     }
   }
 
 
   private void insertZoneLevels() {
     zoneLevels.add(facilityBuilder.createGeoLevel("Country", 1));
     zoneLevels.add(facilityBuilder.createGeoLevel("State", 2));
     zoneLevels.add(facilityBuilder.createGeoLevel("District", 3));
 
     for (GeographicLevel level : zoneLevels) {
       facilityDAO.insertGeoLevel(level);
     }
   }
 
   private void insertGeoZones() {
     GeographicZone nullZone = new GeographicZone();
     GeographicZone country = facilityBuilder.createGeographicZone(zoneLevels.get(0), nullZone);
     facilityDAO.insertGeoZone(country);
     for (int stateCounter = 0; stateCounter < STATES_PER_COUNTRY; stateCounter++) {
       GeographicZone state = facilityBuilder.createGeographicZone(zoneLevels.get(1), country);
       facilityDAO.insertGeoZone(state);
 
       for (int districtCounter = 0; districtCounter < DISTRICT_PER_STATE; districtCounter++) {
         GeographicZone district = facilityBuilder.createGeographicZone(zoneLevels.get(2), state);
         facilityDAO.insertGeoZone(district);
       }
     }
   }
 
   private Facility insertFacility(GeographicZone geoZone, FacilityType facilityType, FacilityOperator facilityOperator) {
 
     Facility facility = facilityBuilder.createFacility(geoZone, facilityType, facilityOperator);
     facilityDAO.insertFacility(facility);
 
     return facility;
   }
 
   private void insertFacilityTypes() {
     for (int counter = 0; counter < NO_OF_FACILITY_TYPES; counter++) {
       FacilityType facilityType = facilityBuilder.createFacilityType();
       facilityDAO.insertFacilityType(facilityType);
       facilityTypes.add(facilityType);
     }
   }
 
   public void setupAdmins() {
     User admin = userBuilder.createUser(null, vendor);
     userDAO.insertUser(admin);
     RoleAssignment roleAssignment1 = userBuilder.createRoleAssignment(null, admin, rolesList.get(0), null);
     userDAO.insertRoleAssignment(roleAssignment1);
 
     User lmuInCharge = userBuilder.createUser(null, vendor);
     userDAO.insertUser(lmuInCharge);
     RoleAssignment roleAssignment2 = userBuilder.createRoleAssignment(null, lmuInCharge, rolesList.get(1), null);
     userDAO.insertRoleAssignment(roleAssignment2);
   }
 }
