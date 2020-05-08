 package com.worthsoln.test.repository;
 
 import com.worthsoln.patientview.model.Tenancy;
 import com.worthsoln.repository.TenancyDao;
 import org.junit.Test;
 
 import javax.inject.Inject;
 import java.util.Date;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class TenancyDaoTest extends BaseDaoTest {
 
     @Inject
     private TenancyDao tenancyDao;
 
     @Test
     public void testAddGetTenancy() {
 
         Tenancy tenancy = new Tenancy();
        tenancy.setCreated(new Date());
        tenancy.setUpdated(new Date());
         tenancy.setName("Tenant 1");
 
         tenancyDao.save(tenancy);
 
         Tenancy checkTenancy = tenancyDao.get(tenancy.getId());
 
         assertTrue("Invalid id for new tenancy", checkTenancy.getId() > 0);
         assertEquals("Name not persisted", tenancy.getName(), checkTenancy.getName());
     }
 }
