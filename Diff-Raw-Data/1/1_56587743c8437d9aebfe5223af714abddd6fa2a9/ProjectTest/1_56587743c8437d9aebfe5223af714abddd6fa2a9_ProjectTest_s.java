 
 package com.project;
 
 import com.project.bbdd.ConnectionManager;
 import com.project.entities.Project;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
import com.project.models.ActiveRecord;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProjectTest {
     
     private EntityManager em;
     
     Project project1;
     Project project2;
     
     
     @Before
     public void emptyTable() throws SQLException{ 
         
         Connection conn = ConnectionManager.getMySQLConnection();
         try {
             Statement stmt1 = conn.createStatement();
             Statement stmt2 = conn.createStatement();
             Statement stmt3 = conn.createStatement();
             Statement stmt4 = conn.createStatement();
             Statement stmt5 = conn.createStatement();
             stmt4.execute( "SET foreign_key_checks  = 0" );
             stmt1.execute( "TRUNCATE TABLE employees" );
             stmt2.execute( "TRUNCATE TABLE tasks" );
             stmt3.execute( "TRUNCATE TABLE projects" );
             stmt5.execute( "SET foreign_key_checks  = 1" );
         } catch ( Exception e ) {
             System.err.println( "Error Truncating" + e.getMessage());
         } finally {
             conn.close();
         }
         
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("managementProjectPU");
         this.em = emf.createEntityManager();
         
         this.project1 = new Project("Management Project", "Online Management Project");
         this.project1.setId(1);
         
         this.project2 = new Project("Academy", "Management Academies in Saragossa");
         this.project2.setId(2);       
     }
     
     @Test
     public void test_create() throws SQLException, ClassNotFoundException{      
        // Creamos el usuario en la BBDD y comprobamos que todo va bien
        this.project1.create(this.em);       
        // Deberia tratarse del 1
        Assert.assertNotNull(Project.findById(this.em, this.project1.getId())); 
     }
     
    @Test
     public void test_findByID() throws SQLException, ClassNotFoundException{
         this.project1.create(this.em); 
         this.project2.create(this.em); 
         
         Project project = Project.findById(this.em, this.project1.getId());
         Assert.assertEquals(project, this.project1);
                 
         project = Project.findById(this.em, this.project2.getId());
         Assert.assertEquals(project, this.project2);                 
     }
     
     @Test
     public void test_findUserByPage() throws SQLException, ClassNotFoundException{
         this.project1.create(this.em); 
         this.project2.create(this.em);
         
         List<Project> projects = Project.findByPage(this.em, 1, 2);
         
         List<Project> expectedAProjects = new ArrayList<Project>();
         expectedAProjects.add(this.project1);
         expectedAProjects.add(this.project2);
         
         Assert.assertEquals(expectedAProjects, projects);
     }
     
     @Test
     public void test_count() throws SQLException, ClassNotFoundException{
         this.project1.create(this.em); 
         this.project2.create(this.em);
         
         long count = Project.count(this.em);
         
         Assert.assertEquals(count, 2);
     }    
     
     @Test
     public void test_update() throws SQLException, ClassNotFoundException{
         this.project1.create(this.em); 
         this.project2.create(this.em);
         
         Project project = this.project1;
         project.setName(this.project1.getName() + Math.round(Math.random()));
         project.update(this.em);
         Project expectedProject = Project.findById(this.em, this.project1.getId());
         Assert.assertEquals(project, expectedProject);
     }
     
     @Test
     public void test_delete() throws SQLException, ClassNotFoundException{
         this.project1.create(this.em); 
         this.project2.create(this.em);
         
         // Eliminamos el primer usuario, por ejemplo
         this.project1.delete(this.em);
         Assert.assertNull(Project.findById(this.em, this.project1.getId()));
     }   
 }
