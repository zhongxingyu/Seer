 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.uff.ic.labgc.versioning;
 
 import br.uff.ic.labgc.core.VersionedDir;
 import br.uff.ic.labgc.core.VersionedFile;
 import br.uff.ic.labgc.core.VersionedItem;
 import br.uff.ic.labgc.exception.ApplicationException;
 import br.uff.ic.labgc.exception.IncorrectPasswordException;
 import br.uff.ic.labgc.exception.VersioningCanNotCreateDirException;
 import br.uff.ic.labgc.exception.VersioningProjectAlreadyExistException;
 import br.uff.ic.labgc.exception.VersioningUserNotFoundException;
 import br.uff.ic.labgc.storage.ConfigurationItem;
 import br.uff.ic.labgc.storage.Project;
 import br.uff.ic.labgc.storage.ProjectDAO;
 import br.uff.ic.labgc.storage.ProjectUser;
 import br.uff.ic.labgc.storage.ProjectUserDAO;
 import br.uff.ic.labgc.storage.ProjectUserId;
 import br.uff.ic.labgc.storage.Revision;
 import br.uff.ic.labgc.storage.RevisionDAO;
 import br.uff.ic.labgc.storage.User;
 import br.uff.ic.labgc.storage.UserDAO;
 import br.uff.ic.labgc.storage.util.ObjectNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jokerfvd
  */
 public class VersioningJUnitTest {
     private static UserDAO userDAO = new UserDAO();
     private static ProjectDAO projectDAO = new ProjectDAO();
     private static ProjectUserDAO projectUserDAO = new ProjectUserDAO();
     private static RevisionDAO revisionDAO = new RevisionDAO();
     private static Versioning versioning = new Versioning();
     
     public VersioningJUnitTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     // @Test
     // public void hello() {}
     
     @Test
     public void testLogin() {   
         try {
             String token = versioning.login("projeto1", "username1", "password1");
             assertTrue("Token OK:", token.equals("nvfdovhfdoivbiofdvf"));
         } catch (ObjectNotFoundException ex) {
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse("Erro 1",true);
         } catch (IncorrectPasswordException ex) {
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
             assertFalse("Erro 2",true);
         }
     
     }
     
     @Test
     public void testGetRevision() { 
         String token = "nvfdovhfdoivbiofdvf";
         VersionedDir vd = versioning.getRevision("1.0", token);
         assertTrue("Revision criado:",vd.getSize() != 0);
         
     }
     
     @Test
     public void testgetVersionedFile() {
         try {
             String token = "nvfdovhfdoivbiofdvf";
             byte content[] = versioning.getVersionedFileContent("vnfdovh9e0h0", token);
             assertTrue(content.length == 10);
        } catch (ApplicationException ex) {
             assertFalse("Erro 1",true);
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     @Test
     public void testGenerateHash() {
         String plaintext = "your text here";
         VersionedFile vf = new VersionedFile();
         vf.setContent(plaintext.getBytes());
 
             String hash = vf.getHash() ;
             assertTrue("assert 1", hash.length() == 32);
 
         
         plaintext = "ABIUBIUDBCIBDIUBIUBIUCDIUCVIUGCIUVCIVSCIVCUVCUICVSIUCVSIUC"
                 + "CVUYfh489fy45hf858g9489fhriefeiovb4398fh89hf589fh45f89r5hf8f"
                 + "h498g45f9gr4f8r0fvhrfvj9rtgjrt09gj9045gg09fu509fy4509fj45f09"
                 + "vj094jvdfiohv984y4f845h5frekojhoifhrtiotgiotrhgiorthgf094f59"
                 + "h4f5h8045y845fh45hf5h89yf94hff94hf48fhg89f5h9g5094hf4gf4h0h0"
                 + "hv45f8480fg8945gf8945gf8945gf894g5f89h45f8945hfhf9h4f94fhf9h";
             vf.setContent(plaintext.getBytes());
             hash = vf.getHash();
             assertTrue("assert 2", hash.equals("7d61195bbf636134f074399f5982727a"));
 
     }
     
     /**
      * testando adicionar um projeto com 1 arquivo
      */
     @Test
     public void testAddProject() { 
         VersionedDir vd = new VersionedDir();
         vd.setAuthor("Autor10");
         vd.setCommitMessage("msg10");
         vd.setName("projeto10");
         
         VersionedFile vf = new VersionedFile();
         vf.setAuthor(vd.getAuthor());
         vf.setCommitMessage(vd.getCommitMessage());
         vf.setName("arquivo10.txt");
         vf.setContent("iabadabadu".getBytes());
         
         vd.addItem(vf);
         try {
             versioning.addProject(vd, "username1");
             
             Project project = projectDAO.getByName("projeto10");
             assertNotNull(project);
         } catch (VersioningProjectAlreadyExistException ex) {
             fail("VersioningProjectAlreadyExistException");
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (VersioningUserNotFoundException ex) {
             fail("VersioningUserNotFoundException");
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (VersioningCanNotCreateDirException ex) {
             fail("VersioningCanNotCreateDirException");
             Logger.getLogger(VersioningJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         
     }
 }
