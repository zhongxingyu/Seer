 package org.motechproject.carereporting.service;
 
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.carereporting.domain.DashboardEntity;
 import org.motechproject.carereporting.domain.ReportEntity;
 import org.motechproject.carereporting.domain.UserEntity;
 import org.motechproject.carereporting.domain.dto.DashboardPositionDto;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:testContext.xml")
 public class DashboardServiceIT extends AbstractTransactionalJUnit4SpringContextTests {
 
     @Autowired
     private DashboardService dashboardService;
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private ReportService reportService;
 
     @Autowired
     private SessionFactory sessionFactory;
 
    private final static int EXPECTED_DASHBOARDS_ALL = 6;
     private final static String DASHBOARD_NAME = "DASHBOARD_TEST_1";
     private final static Short DASHBOARD_TAB_POSITION = 99;
     private final static Short DASHBOARD_NEW_TAB_POSITION = 98;
     private final static Integer REPORT_ID = 1;
     private final static Integer USER_ID = 1;
     private final static Short EXPECTED_TAB_POSITION = 5;
 
     private static Integer newDashboardId;
 
     @Before
     public void setupAuthentication() {
         List<GrantedAuthority> authorities = new ArrayList<>();
         authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_REPORTS"));
         authorities.add(new SimpleGrantedAuthority("CAN_MANAGE_SYSTEM_USERS"));
         SecurityContextHolder.getContext().setAuthentication(
                 new UsernamePasswordAuthenticationToken("principal", "credentials", authorities));
     }
 
     @After
     public void cleanup() {
         deleteCreatedDashboard();
     }
 
     private void deleteCreatedDashboard() {
         if (newDashboardId == null) {
             return;
         }
 
         Query query = sessionFactory.getCurrentSession()
                 .createQuery("delete DashboardEntity where id = :dashboardId");
         query.setParameter("dashboardId", newDashboardId);
 
         newDashboardId = null;
     }
 
     @Test
     public void testGetAllDashboards() {
         Set<DashboardEntity> dashboardEntities = dashboardService.getAllDashboards();
 
         assertNotNull(dashboardEntities);
         assertEquals(EXPECTED_DASHBOARDS_ALL, dashboardEntities.size());
     }
 
     @Test
     public void testGetDashboardByName() {
         createDashboard();
 
         DashboardEntity dashboardEntity = dashboardService.getDashboardByName(DASHBOARD_NAME);
 
         assertNotNull(dashboardEntity);
         assertEquals(newDashboardId, dashboardEntity.getId());
         assertEquals(DASHBOARD_NAME, dashboardEntity.getName());
     }
 
     @Test
     public void testCreateNewDashboard() {
         createDashboard();
     }
 
     @Test
     public void testGetTabPositionForNewDashboard() {
         Short tabPosition = dashboardService.getTabPositionForNewDashboard();
 
         assertNotNull(tabPosition);
         assertEquals(EXPECTED_TAB_POSITION, tabPosition);
     }
 
     @Test
     public void testSaveDashboardPositions() {
         DashboardEntity dashboardEntity = createDashboard();
         dashboardEntity.setTabPosition(DASHBOARD_NEW_TAB_POSITION);
 
         List<DashboardPositionDto> dashboardPositionDtos = new ArrayList<>();
         dashboardPositionDtos.add(new DashboardPositionDto(
                 DASHBOARD_NAME, Integer.valueOf(DASHBOARD_NEW_TAB_POSITION.toString())));
 
         dashboardService.saveDashboardsPositions(dashboardPositionDtos);
         dashboardEntity = dashboardService.getDashboardByName(DASHBOARD_NAME);
 
         assertNotNull(dashboardEntity);
         assertEquals(DASHBOARD_NAME, dashboardEntity.getName());
         assertEquals(DASHBOARD_NEW_TAB_POSITION, dashboardEntity.getTabPosition());
     }
 
     private DashboardEntity createDashboard() {
         UserEntity userEntity = userService.getUserById(USER_ID);
 
         assertNotNull(userEntity);
         assertEquals(USER_ID, userEntity.getId());
 
         Set<UserEntity> userEntities = new LinkedHashSet<>();
         userEntities.add(userEntity);
 
         ReportEntity reportEntity = reportService.getReportById(REPORT_ID);
         Set<ReportEntity> reportEntities = new LinkedHashSet<>();
         reportEntities.add(reportEntity);
 
         assertNotNull(reportEntity);
         assertEquals(REPORT_ID, reportEntity.getId());
 
         DashboardEntity dashboardEntity = new DashboardEntity(
                 DASHBOARD_NAME, DASHBOARD_TAB_POSITION, userEntities);
         Set<DashboardEntity> dashboardEntities = new LinkedHashSet<>();
         dashboardEntities.add(dashboardEntity);
 
         dashboardEntity.setReports(reportEntities);
         dashboardService.createNewDashboard(dashboardEntity);
 
         newDashboardId = dashboardEntity.getId();
         dashboardEntity = dashboardService.getDashboardByName(DASHBOARD_NAME);
 
         assertNotNull(dashboardEntity);
         assertEquals(newDashboardId, dashboardEntity.getId());
         assertEquals(DASHBOARD_NAME, dashboardEntity.getName());
         assertEquals(DASHBOARD_TAB_POSITION, dashboardEntity.getTabPosition());
 
         return dashboardEntity;
     }
 }
