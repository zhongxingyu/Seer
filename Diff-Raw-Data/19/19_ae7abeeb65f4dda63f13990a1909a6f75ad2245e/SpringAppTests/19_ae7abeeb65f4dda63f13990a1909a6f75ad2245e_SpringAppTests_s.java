 package com.spigit.citi;
 
import com.spigit.citi.service.TransactionMonitorService;
 import junit.framework.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:spring-config.xml")
 public class SpringAppTests {
     @Autowired
    private TransactionMonitorService transactionMonitorService;
 
     @Test
     public void testExecute()   {
        Assert.assertNotNull(transactionMonitorService);
     }
 }
