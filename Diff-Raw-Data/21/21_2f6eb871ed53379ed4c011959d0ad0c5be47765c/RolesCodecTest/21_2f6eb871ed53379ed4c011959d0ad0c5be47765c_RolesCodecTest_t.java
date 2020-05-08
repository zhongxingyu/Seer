 package net.codjo.security.gui.user;
 import net.codjo.security.common.message.ModelManager;
 import org.junit.Before;
 import org.junit.Test;
 
import static net.codjo.security.gui.user.MainFormTest.removeWindowsSpecificCarriageReturn;
import static net.codjo.security.gui.user.MainFormTest.resource;
import static net.codjo.util.file.FileUtil.loadContent;
 import static org.junit.Assert.assertEquals;
 /**
  *
  */
 public class RolesCodecTest {
     private RolesCodec rolesCodec = new RolesCodec();
     private ModelManager modelManager;
 
 
     @Before
     public void setUp() {
         modelManager = new ModelBuilder()
               .addRole("unitRole1")
               .addRole("unitRole2")
               .addRole("unitRole3")
               .addRole("unitRole4")
               .addRole("unitRole5")
               .addRoleComposite("functionRole1", "unitRole1", "unitRole2")
               .addRoleComposite("functionRole2", "unitRole2", "unitRole4")
               .addRoleComposite("functionRole3", "unitRole3")
               .addRoleComposite("functionRole4")
               .addRoleComposite("userRole1", "functionRole1", "functionRole2", "functionRole3")
               .addRoleComposite("userRole2", "functionRole1", "functionRole2", "unitRole2")
               .addRoleComposite("userRole3", "functionRole3", "unitRole1")
               .addRoleComposite("userRole4")
               .addRoleToUser("userRole1", "admin")
               .addRoleToUser("userRole2", "admin")
               .addRoleToUser("userRole3", "admin")
               .addRoleToUser("userRole1", "bob")
               .addRoleToUser("userRole2", "rebecca")
               .get();
     }
 
 
     @Test
     public void test_toCsv() throws Exception {
         String actual = rolesCodec.toCsv(modelManager);

        assertEquals(removeWindowsSpecificCarriageReturn(loadContent(resource("exportedRoles.csv"))),
                     removeWindowsSpecificCarriageReturn(actual));
     }
 }
