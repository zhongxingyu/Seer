 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.dao;
 
 import com.artivisi.training.domain.Permission;
 import com.artivisi.training.domain.Role;
import com.artivisi.training.domain.User;
 import java.io.FileInputStream;
 import java.sql.Connection;
 import javax.sql.DataSource;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author endy
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath:konfig-spring.xml")
 public class RoleDaoTest {
     @Autowired private RoleDao roleDao;
     @Autowired private DataSource dataSource;
     
     @Before
     public void resetIsiDatabase() throws Exception{
         Connection conn = dataSource.getConnection();
         
         DatabaseOperation.CLEAN_INSERT
                 .execute(new DatabaseConnection(conn), 
                 new FlatXmlDataSetBuilder()
                 .build(new FileInputStream("src/test/resources/sample-data.xml")));
         
         conn.close();
     }
     
     @Test
     public void testLazyLoading(){
         Role r = roleDao.cariById(1);
         Assert.assertNotNull(r);
         
         // daftar permission
         for(Permission p : r.getDaftarPermission()){
             System.out.println("Action : "+p.getAction());
         }
        
        // daftar user
        for(User u : r.getDaftarUser()){
            System.out.println("Username : "+u.getUsername());
        }
     }
 }
