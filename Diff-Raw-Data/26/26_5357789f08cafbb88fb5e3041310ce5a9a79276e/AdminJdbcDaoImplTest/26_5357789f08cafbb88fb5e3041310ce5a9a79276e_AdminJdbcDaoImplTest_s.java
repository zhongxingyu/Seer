 package org.youthnet.debug.dao.jdbc.impl;
 
 import static junit.framework.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.youthnet.debug.dao.jdbc.JdbcDao;
 import org.youthnet.debug.dao.util.AdminJdbcTestUtil;
 import org.youthnet.debug.domain.common.UuidType;
 import org.youthnet.debug.domain.common.impl.UuidTypeImpl;
 
 import javax.annotation.Resource;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: karl
  * Date: 25-May-2010
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/admin-test-config.xml",
         "/config/spring/admin-hibernate-config.xml",
         "/config/spring/admin-jdbc-config.xml"})
 public class AdminJdbcDaoImplTest {
 
     @Resource(name = "adminJdbcTestUtil")
     private AdminJdbcTestUtil adminJdbcTestUtil;
 
     @Resource(name = "adminJdbcDaoImpl")
     private JdbcDao adminJdbcDao;
 
     @Before
     public void buildUp() {
         adminJdbcTestUtil.createCollective();
         adminJdbcTestUtil.createVuo();
         adminJdbcTestUtil.createUser();
         adminJdbcTestUtil.createRole();
         adminJdbcTestUtil.addRole();
     }
 
     @After
     public void tearDown() {
         adminJdbcTestUtil.removeRole();
         adminJdbcTestUtil.deleteRole();
         adminJdbcTestUtil.deleteUser();
         adminJdbcTestUtil.deleteVuo();
         adminJdbcTestUtil.deleteCollective();
     }
 
     @Test
     public void testGetTableNames() throws Exception {
         List<String> names = adminJdbcDao.getTableNames();
 
         assertNotNull("names list exist", names);
         assertTrue("names list contains values", names.size() > 0);
     }
 
     @Test
     public void testGetTableRows() throws Exception {
         List<Map <String, Object>> rows = adminJdbcDao.getTableRows("vuo");
         assertNotNull("get table rows", rows);
         assertTrue("table rows exist", rows.size() > 0);
         assertTrue("table rows id converted", rows.get(0).get("ID") instanceof UuidType);
     }
 
     @Test
     public void testGetRowById() throws Exception {
         Map <String, Object> row = adminJdbcDao.getRowById(UuidTypeImpl.fromString(adminJdbcTestUtil.getVUOID()));
         assertNotNull("get row by id", row);
         assertTrue("row by id exists", row.size() > 0);
        assertEquals("row id converted", UuidTypeImpl.class.getName(), row.get("ID").getClass().getName());
         assertEquals("correct vuo name", adminJdbcTestUtil.getVUONAME(), row.get("NAME"));
         assertEquals("correct vuo code", adminJdbcTestUtil.getVUOCODE(), row.get("VUOCODE"));
         assertNotNull("table name added", row.get("tableName"));
         assertEquals("table name correct", "VUO", row.get("tableName"));
 
         row = adminJdbcDao.getRowById(adminJdbcTestUtil.getROLEID());
         assertNotNull("get row by id", row);
         assertTrue("row by id exists", row.size() > 0);
        assertEquals("row id converted", BigDecimal.class.getName(), row.get("ID").getClass().getName());
         assertEquals("correct vuo name", adminJdbcTestUtil.getROLNAME(), row.get("NAME"));
         assertEquals("correct vuo code", adminJdbcTestUtil.getROLDESCRIPTION(), row.get("DESCRIPTION"));
         assertNotNull("table name added", row.get("tableName"));
         assertEquals("table name correct", "ROLE", row.get("tableName"));
     }
 
     @Test
     public void testGetRowByUuid() throws Exception {
         Map <String, Object> row = adminJdbcDao.getRowByUuidString(adminJdbcTestUtil.getVUOID());
         assertNotNull("get row by id", row);
         assertTrue("row by id exists", row.size() > 0);
         assertEquals("row id converted", UuidTypeImpl.class.getName(), row.get("ID").getClass().getName());
         assertEquals("correct vuo name", adminJdbcTestUtil.getVUONAME(), row.get("NAME"));
         assertEquals("correct vuo code", adminJdbcTestUtil.getVUOCODE(), row.get("VUOCODE"));
         assertNotNull("table name added", row.get("tableName"));
         assertEquals("table name correct", "VUO", row.get("tableName"));
     }
 
     @Test
     public void testGetRowByIdWithTable() throws Exception {
         Map <String, Object> row = adminJdbcDao.getRowById(UuidTypeImpl.fromString(adminJdbcTestUtil.getVUOID()), "vuo");
         assertNotNull("get row by id", row);
         assertTrue("row by id exists", row.size() > 0);
         assertEquals("row id converted", UuidTypeImpl.class.getName(), row.get("ID").getClass().getName());
         assertEquals("correct vuo name", adminJdbcTestUtil.getVUONAME(), row.get("NAME"));
         assertEquals("correct vuo code", adminJdbcTestUtil.getVUOCODE(), row.get("VUOCODE"));
     }
 }
