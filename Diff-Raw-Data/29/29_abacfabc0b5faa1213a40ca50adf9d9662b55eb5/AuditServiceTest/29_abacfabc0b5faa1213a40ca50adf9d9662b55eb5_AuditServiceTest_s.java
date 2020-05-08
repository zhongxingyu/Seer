 package com.chariot.lunchlearn.testingtalk.service;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 
 public class AuditServiceTest {
 
   private AuditServiceImpl auditService;
 
   // collaborator
   private AuditRepositoryStub stub;
 
   @Before
   public void setUp() {
     auditService = new AuditServiceImpl();
     stub = new AuditRepositoryStub();
     auditService.setAuditRepository(stub);
   }
 
   @Test
   public void testAddAuditRecord() {
     // this fails, we start here...
     auditService.auditActivity(
         this.getClass(),
         "do something");
 
    assertThat(1, equalTo(stub.getDatabase().size()));
   }
 }
