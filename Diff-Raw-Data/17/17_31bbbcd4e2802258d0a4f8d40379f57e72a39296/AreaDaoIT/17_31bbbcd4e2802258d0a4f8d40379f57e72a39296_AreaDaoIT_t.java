 package org.motechproject.carereporting.dao;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:testContext.xml")
 public class AreaDaoIT extends AbstractTransactionalJUnit4SpringContextTests {
 
     @Autowired
     private AreaDao areaDao;
 
    private static final int PARENT_AREA_ID = 1;
     private static final int EXPECTED_CHILD_AREAS = 4;
     private static final int EXPECTED_DIRECT_CHILD_AREAS = 4;
     private static final int LEVEL_ID = 1;
     private static final int EXPECTED_AREAS_BY_LEVEL_ID = 1;
 
     @Test
     public void testFindAllChildAreasByParentAreaId() {
         Set<AreaEntity> areaEntityList = areaDao.getAllChildAreasByParentAreaId(PARENT_AREA_ID);
 
         assertNotNull(areaEntityList);
         assertEquals(EXPECTED_CHILD_AREAS, areaEntityList.size());
     }
 
     @Test
     public void testFindDirectChildAreas() {
         Set<AreaEntity> areaEntityList = areaDao.getDirectChildAreas(PARENT_AREA_ID);
 
         assertNotNull(areaEntityList);
         assertEquals(EXPECTED_DIRECT_CHILD_AREAS, areaEntityList.size());
     }
 
     @Test
     public void testFindAreasByLevelId() {
         Set<AreaEntity> areaEntityList = areaDao.getAreasByLevelId(LEVEL_ID);
 
         assertNotNull(areaEntityList);
         assertEquals(EXPECTED_AREAS_BY_LEVEL_ID, areaEntityList.size());
     }
 }
